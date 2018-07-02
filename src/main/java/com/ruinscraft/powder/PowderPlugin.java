package com.ruinscraft.powder;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import com.ruinscraft.powder.model.Message;
import com.ruinscraft.powder.model.Powder;
import com.ruinscraft.powder.storage.MySqlStorage;
import com.ruinscraft.powder.storage.Storage;
import com.ruinscraft.powder.util.ConfigUtil;
import com.ruinscraft.powder.util.PowderUtil;

import net.md_5.bungee.api.chat.TextComponent;

public class PowderPlugin extends JavaPlugin {

	private static PowderPlugin instance;
	private static PowderHandler powderHandler;

	private FileConfiguration config;
	private int configVersion;
	private List<FileConfiguration> powderConfigs;
	private FileConfiguration createdPowders;

	private static Map<Message, TextComponent> messages;

	private Storage storage;

	private static boolean isLoading;

	private boolean fastMode;

	public static PowderPlugin getInstance() {
		return instance;
	}

	@Override
	public void onEnable() {
		instance = this;

		load();
	}

	@Override
	public void onDisable() {
		// delete all tasks & powders
		powderHandler.clearEverything();
		disableStorage();

		instance = null;
	}

	public synchronized void load() {
		isLoading = true;
		config = ConfigUtil.loadConfig();

		loadMessages();

		enableStorage();

		// load all powders async & load users' powders if storage is enabled
		PowderPlugin.getInstance().getServer().getScheduler()
		.runTaskAsynchronously(PowderPlugin.getInstance(), () -> {
			loadPowdersFromSources();
			// load all saved powders from db if enabled
			if (useStorage()) {
				PowderUtil.loadAllUUIDs();
			}
			this.createdPowders = ConfigUtil.loadCreatedPowders();
			isLoading = false;
		});

		getCommand("powder").setExecutor(new PowderCommand());
		getServer().getPluginManager().registerEvents(new EnvironmentListener(), this);
	}

	public synchronized void reload() {
		isLoading = true;
		config = ConfigUtil.loadConfig();

		if (!useStorage()) {
			for (UUID uuid : PowderPlugin
					.getInstance().getPowderHandler().getAllPowderTaskUsers()) {
				Player player = Bukkit.getPlayer(uuid);
				PowderUtil.sendPrefixMessage(player, 
						Message.LOADING_ALERT, "powder", player.getName());
			}
		}

		loadMessages();
		enableStorage();
		loadPowdersFromSources();

		PowderUtil.loadAllUUIDs();
		this.createdPowders = ConfigUtil.loadCreatedPowders();
		isLoading = false;
	}

	public static boolean isLoading() {
		return isLoading;
	}

	public boolean fastMode() {
		return fastMode;
	}

	public void setFastMode(boolean fastMode) {
		this.fastMode = fastMode;
	}

	// end all current tasks & reinitialize powderHandler
	public void cleanHandlers() {
		if (!(powderHandler == null)) {
			powderHandler.clearEverything();
		}
		powderHandler = new PowderHandler();
	}

	public PowderHandler getPowderHandler() {
		return powderHandler;
	}

	public int getConfigVersion() {
		return configVersion;
	}

	public void setConfigVersion(int configVersion) {
		this.configVersion = configVersion;
	}

	public List<FileConfiguration> getPowderConfigs() {
		return powderConfigs;
	}

	public FileConfiguration getCreatedPowdersFile() {
		return createdPowders;
	}

	public void setCreatedPowdersFile(FileConfiguration fileConfig) {
		this.createdPowders = fileConfig;
	}

	public Map<Message, TextComponent> getMessages() {
		return messages;
	}

	public void loadMessages() {
		messages = new HashMap<>();
		String fileName = config.getString("locale", "english_US.yml");
		File file = new File(getDataFolder() + "/locale", fileName);
		if (!file.exists()) {
			getLogger().warning("Locale '" + fileName +  "' not found, loading if exists!");
			saveResource("locale/" + fileName, false);
		}
		FileConfiguration locale = YamlConfiguration
				.loadConfiguration(file);

		for (Message message : Message.values()) {
			String actualMessage = locale.getString(message.name());
			if (actualMessage == null) {
				getLogger().warning("No message specified for '" + 
						message.name() + "' in '" + fileName + "'." + 
						" Is your locale or version of Powder outdated?");
				continue;
			}
			TextComponent textComponent = PowderUtil.format(PowderUtil.color(actualMessage));
			messages.put(message, textComponent);
		}
	}

	public Storage getStorage() {
		return storage;
	}

	public boolean useStorage() {
		return getStorage() != null;
	}

	public void enableStorage() {
		disableStorage();
		// enable storage if enabled in configuration
		if (config.getBoolean("storage.mysql.use")) {
			String host = config.getString("storage.mysql.host");
			int port = config.getInt("storage.mysql.port");
			String database = config.getString("storage.mysql.database");
			String username = config.getString("storage.mysql.username");
			String password = config.getString("storage.mysql.password");
			String powdersTable = config.getString("storage.mysql.table");

			storage = new MySqlStorage(host, port, database, username, password, powdersTable);

			getLogger().info("Using MySQL storage");
		}
	}

	public void disableStorage() {
		if (storage != null) {
			storage.close();
			storage = null;
		}
	}

	public synchronized void loadPowdersFromSources() {
		// load source yaml files
		powderConfigs = ConfigUtil.loadPowderConfigs();

		// remove all existing tasks/Powders
		cleanHandlers();

		// handle categories if enabled
		powderHandler.setIfCategoriesEnabled(config.getBoolean("categoriesEnabled", false));

		// alert online players of the reload
		getLogger().info("Loading Powders...");
		for (Player player : Bukkit.getOnlinePlayers()) {
			if (player.hasPermission("powder.reload")) {
				PowderUtil.sendPrefixMessage(player,
						Message.LOADING_START, "powder", player.getName());
			}
		}

		ConfigUtil.reloadCategories();

		List<String> powderNames = new ArrayList<>();
		if (!fastMode()) {
			for (FileConfiguration powderConfig : powderConfigs) {
				for (String s : powderConfig.getConfigurationSection("powders").getKeys(false)) {
					Powder powder = null;
					try {
						powder = ConfigUtil.loadPowderFromConfig(powderConfig, s);
					} catch (Exception e) {
						getLogger().warning("Powder '" + s + 
								"' encountered an error and was not loaded:");
						e.printStackTrace();
						continue;
					}
					if (powder != null) {
						getPowderHandler().addPowder(powder);
						powderNames.add(powder.getName());
					}
				}
			}
		} else {
			for (FileConfiguration powderConfig : powderConfigs) {
				for (String s : powderConfig.getConfigurationSection("powders").getKeys(false)) {
					Powder powder = null;
					try {
						powder = ConfigUtil.loadPowderShellFromConfig(powderConfig, s);
					} catch (Exception e) {
						getLogger().warning("Powder '" + s + 
								"' encountered an error and was not loaded:");
						e.printStackTrace();
						continue;
					}
					if (powder != null) {
						getPowderHandler().addPowder(powder);
						powderNames.add(powder.getName());
					}
				}
			}
		}

		// do this again to load {total} parameter
		ConfigUtil.reloadCategories();

		String powderAmount = String.valueOf(powderNames.size());
		String niceTotal = powderAmount + " total!";

		// alert console of the Powders loaded
		StringBuilder msg = new StringBuilder();
		String loaded = "Loaded Powders: ";
		for (String powderName : powderNames) {
			if (powderNames.get(powderNames.size() - 1).equals(powderName)) {
				msg.append(powderName);
			} else {
				msg.append(powderName + ", ");
			}
		}
		if (config.getBoolean("listOfLoadedPowders", true)) {
			getLogger().info(loaded + msg.toString() + ". " + niceTotal);
		} else {
			getLogger().info("Loaded all Powders. " + niceTotal);
		}

		// alert online players with permission of the Powders loaded
		for (Player player : Bukkit.getOnlinePlayers()) {
			if (player.hasPermission("powder.reload")) {
				PowderUtil.sendPrefixMessage(
						player, Message.LOADING_FINISH, Message.LOADING_FINISH_HOVER, 
						"powder", player.getName(), powderAmount, msg.toString());
			}
		}
	}

}
