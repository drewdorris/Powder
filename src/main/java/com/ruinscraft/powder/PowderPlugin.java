package com.ruinscraft.powder;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import com.ruinscraft.powder.models.Powder;
import com.ruinscraft.powder.storage.MySqlStorage;
import com.ruinscraft.powder.storage.Storage;
import com.ruinscraft.powder.util.YamlUtil;
import com.ruinscraft.powder.util.PowderUtil;

import net.md_5.bungee.api.ChatColor;

public class PowderPlugin extends JavaPlugin {

	private static PowderPlugin instance;
	private static PowderHandler powderHandler;

	private FileConfiguration config;
	private List<FileConfiguration> powderConfigs;

	private Storage storage;

	public static PowderPlugin getInstance() {
		return instance;
	}

	public void onEnable() {
		instance = this;

		config = YamlUtil.loadConfig();

		enableStorage();

		// load all powders async & load users' powders if storage is enabled
		PowderPlugin.getInstance().getServer().getScheduler()
		.runTaskAsynchronously(PowderPlugin.getInstance(), () -> {
			loadPowdersFromSources();

			// load all saved powders from db if enabled
			if (useStorage()) {
				PowderUtil.loadPowdersForOnline();
			}
		});

		getCommand("powder").setExecutor(new PowderCommand());
		getServer().getPluginManager().registerEvents(new PlayerListener(), this);
	}

	public void onDisable() {
		if (useStorage()) {
			// save all current users' powders before disabling
			PowderUtil.savePowdersForOnline();
		}

		// delete all tasks & powders
		powderHandler.clearEverything();

		instance = null;
	}

	public void reload() {
		config = YamlUtil.loadConfig();

		if (useStorage()) {
			PowderUtil.savePowdersForOnline();
		}

		enableStorage();
		loadPowdersFromSources();

		if (useStorage()) {
			PowderUtil.loadPowdersForOnline();
		} else {
			for (UUID uuid : PowderPlugin
					.getInstance().getPowderHandler().getAllPowderTaskUsers()) {
				PowderUtil.sendPrefixMessage(Bukkit.getPlayer(uuid), PowderUtil.INFO 
						+ "Your Powders were cancelled due to a reload.", "powder");
			}
		}
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

	public List<FileConfiguration> getPowderConfigs() {
		return powderConfigs;
	}

	public Storage getStorage() {
		return storage;
	}

	public boolean useStorage() {
		return getStorage() != null;
	}

	public void enableStorage() {
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

	public void loadPowderConfigs() {
		// list of configuration files that contain Powders
		powderConfigs = new ArrayList<FileConfiguration>();

		BufferedReader reader;

		for (String urlName : config.getStringList("powderSources")) {

			FileConfiguration powderConfig;
			URL url = PowderUtil.readURL(urlName);
			File file;
			// if a file is from a path, load from within data folder
			if (!urlName.contains("/")) {

				file = new File(getDataFolder(), urlName);
				if (!file.exists()) {
					getLogger().warning("Failed to load config file '" + urlName + "'.");
					continue;
				}
				powderConfig = YamlConfiguration.loadConfiguration(file);

				// else, load from URL
			} else if (url != null) {

				InputStream stream = PowderUtil.getInputStreamFromURL(url);

				if (stream == null) {
					continue;
				}

				reader = new BufferedReader(new InputStreamReader(stream));
				powderConfig = YamlConfiguration.loadConfiguration(reader);

			} else {
				getLogger().warning("Failed to load config file '" + urlName + "'.");
				continue;
			}

			powderConfigs.add(powderConfig);

		}

		// if powders.yml is listed as a source but doesn't exist, create it
		File defaultPowderConfig = new File(getDataFolder(), "powders.yml");
		if (!defaultPowderConfig.exists() && 
				config.getStringList("powderSources").contains("powders.yml")) {
			getLogger().info("powders.yml not found but listed as a source, creating!");
			saveResource("powders.yml", false);
			FileConfiguration powderConfig = 
					YamlConfiguration.loadConfiguration(defaultPowderConfig);
			powderConfigs.add(powderConfig);
		}
	}

	public void loadPowdersFromSources() {
		// load source yaml files
		powderConfigs = YamlUtil.loadPowderConfigs();

		// remove all existing tasks/Powders
		cleanHandlers();

		// prefix for all messages with prefixes
		PowderUtil.PREFIX = PowderUtil.color(config.getString("prefix", "&7[&9Powder&7] "));

		String c = "colors.";
		PowderUtil.INFO = 
				ChatColor.valueOf(config.getString(c + "info", "GRAY"));
		PowderUtil.HIGHLIGHT = 
				ChatColor.valueOf(config.getString(c + "highlight", "RED"));
		PowderUtil.HIGHLIGHT_TWO = 
				ChatColor.valueOf(config.getString(c + "highlight_two", "GREEN"));
		PowderUtil.HIGHLIGHT_THREE = 
				ChatColor.valueOf(config.getString(c + "highlight_three", "YELLOW"));
		PowderUtil.NO_PERM = 
				ChatColor.valueOf(config.getString(c + "no_perm", "DARK_GRAY"));
		PowderUtil.WARNING = 
				ChatColor.valueOf(config.getString(c + "warning", "RED"));

		// handle categories if enabled
		powderHandler.setIfCategoriesEnabled(config.getBoolean("categoriesEnabled", false));

		YamlUtil.reloadCategories();

		// alert online players of the reload
		getLogger().info("Loading Powders...");
		for (Player player : Bukkit.getOnlinePlayers()) {
			if (player.hasPermission("powder.reload")) {
				PowderUtil.sendPrefixMessage(player,
						PowderUtil.INFO + "Loading Powders...", "powder");
			}
		}

		List<String> powderNames = new ArrayList<>();
		for (FileConfiguration powderConfig : powderConfigs) {
			for (String s : powderConfig.getConfigurationSection("powders").getKeys(false)) {
				Powder powder = YamlUtil.loadPowderFromConfig(powderConfig, s);
				if (powder != null) {
					getPowderHandler().addPowder(powder);
					powderNames.add(powder.getName());
				}
			}
		}

		// do this again to load {total} parameter
		YamlUtil.reloadCategories();

		String powderAmount = String.valueOf(powderNames.size());

		// alert console of the Powders loaded
		StringBuilder msg = new StringBuilder();
		msg.append("Loaded Powders: ");
		for (String powderName : powderNames) {
			if (powderNames.get(powderNames.size() - 1).equals(powderName)) {
				msg.append(powderName + ". " + powderAmount + " total!");
			} else {
				msg.append(powderName + ", ");
			}
		}
		getLogger().info(msg.toString());

		// alert online players with permission of the Powders loaded
		for (Player player : Bukkit.getOnlinePlayers()) {
			if (player.hasPermission("powder.reload")) {
				PowderUtil.sendPrefixMessage(
						player, PowderUtil.INFO + 
						"All Powders loaded! (" + powderAmount + " total)", "powder");
			}
		}
	}

}
