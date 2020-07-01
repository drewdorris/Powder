package com.ruinscraft.powder.command;

import com.ruinscraft.powder.PowderHandler;
import com.ruinscraft.powder.PowderPlugin;
import com.ruinscraft.powder.command.subcommand.ActiveCommand;
import com.ruinscraft.powder.command.subcommand.ArrowCommand;
import com.ruinscraft.powder.command.subcommand.AttachCommand;
import com.ruinscraft.powder.command.subcommand.CancelCommand;
import com.ruinscraft.powder.command.subcommand.CategoriesCommand;
import com.ruinscraft.powder.command.subcommand.CategoryCommand;
import com.ruinscraft.powder.command.subcommand.StationCommand;
import com.ruinscraft.powder.command.subcommand.CreatedCommand;
import com.ruinscraft.powder.command.subcommand.HelpCommand;
import com.ruinscraft.powder.command.subcommand.ListCommand;
import com.ruinscraft.powder.command.subcommand.NearbyCommand;
import com.ruinscraft.powder.command.subcommand.ReloadCommand;
import com.ruinscraft.powder.command.subcommand.RemoveCommand;
import com.ruinscraft.powder.command.subcommand.SearchCommand;
import com.ruinscraft.powder.model.Message;
import com.ruinscraft.powder.model.Powder;
import com.ruinscraft.powder.model.PowderTask;
import com.ruinscraft.powder.util.PowderUtil;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.*;

public class PowderCommand implements CommandExecutor, TabCompleter {

	private List<Player> recentCommandSenders = new ArrayList<>();

	private Set<SubCommand> subCommands = new HashSet<>(Arrays.asList(
			new HelpCommand(), new ReloadCommand(), new ListCommand(), 
			new CancelCommand(), new ActiveCommand(), new CategoriesCommand(), new CategoryCommand(),
			new SearchCommand(), new AttachCommand(), new StationCommand(), new CreatedCommand(),
			new RemoveCommand(), new NearbyCommand(), new ArrowCommand()));

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		// if console
		if (!(sender instanceof Player)) {
			try {
				if (args[0].equals("reload")) {
					// reload
					if (!PowderPlugin.isLoading()) {
						PowderPlugin.get().getServer().getScheduler()
						.runTaskAsynchronously(PowderPlugin.get(), () -> {
							PowderPlugin.get().reload();
						});
					} else {
						PowderPlugin.info(
								"Can't reload while already loading!");
					}
				} else {
					PowderUtil.sendMainConsoleMessage();
				}
			} catch (Exception e) {
				PowderUtil.sendMainConsoleMessage();
			}
			return true;
		}

		Player player = (Player) sender;

		if (PowderPlugin.isLoading()) {
			PowderUtil.sendPrefixMessage(player, 
					Message.GENERAL_LOADING, label, player.getName());
			return false;
		}

		// if no permission for using the command itself
		if (!(player.hasPermission("powder.command"))) {
			PowderUtil.sendPrefixMessage(player,
					Message.GENERAL_NO_PERMISSION, label, player.getName());
			return false;
		}

		PowderHandler powderHandler = PowderPlugin.get().getPowderHandler();

		// elements contained within a page of a list
		int pageLength = PowderPlugin.get().getConfig().getInt("pageLength");

		// if no args, lists categories if categories enabled, else list all Powders
		if (args.length < 1) {
			if (powderHandler.categoriesEnabled()) {
				PowderUtil.listCategories(
						player, powderHandler.getCategories(),
						" categories ", 1, pageLength, label);
			} else {
				PowderUtil.listPowders(
						player, powderHandler.getPowders(),
						" list ", 1, pageLength, label);
			}
			return false;
		}

		Powder powder = powderHandler.getPowder(args[0]);

		if (powder == null) {
			// handle any subcommands, if the first argument is a subcommand
			for (SubCommand subCommand : this.subCommands) {
				if (eq(args[0], subCommand)) {
					subCommand.command(player, label, args);
					return true;
				}
			}
			// else, do this
			if (powderHandler.categoriesEnabled()) {
				if (powderHandler.getCategory(args[0]) != null) {
					if (args.length > 1) {
						if (powderHandler.getPowder(args[1]) != null) {
							powder = powderHandler.getPowder(args[1]);
							boolean loop = false;
							if (args.length > 2) {
								if (args[2].equalsIgnoreCase("loop")) {
									loop = true;
								}
							}
							// if no permission for the specific Powder
							if (!PowderUtil.hasPermission(player, powder)) {
								PowderUtil.sendPrefixMessage(player, Message.POWDER_NO_PERMISSION,
										label, args[0]);
								return false;
							}
							spawnPowderThroughCommand(powder, player, label, args[0], loop);
							return true;
						}
					}
					PowderUtil.listPowders(player,
							powderHandler.getPowdersFromCategory(args[0]),
							" category " + args[0] + " ", 1, pageLength, label);
					return true;
				} else {
					PowderUtil.sendPrefixMessage(player,
							Message.CATEGORY_UNKNOWN, label, player.getName(), args[0]);
					PowderUtil.listCategories(player,
							powderHandler.getSimilarCategories(args[0]),
							" category " + args[0] + " ", 1, pageLength, label);
				}
			} else {
				PowderUtil.sendPrefixMessage(player,
						Message.LIST_UNKNOWN_POWDER, label, player.getName(), args[0]);
				PowderUtil.listPowders(player, powderHandler.getSimilarPowders(args[0]),
						" list ", 1, pageLength, label);
				return false;
			}
			return true;
		}

		// after this, first argument is clearly a Powder

		// if no permission for the specific Powder
		if (!PowderUtil.hasPermission(player, powder)) {
			PowderUtil.sendPrefixMessage(player, Message.POWDER_NO_PERMISSION,
					label, args[0]);
			return false;
		}

		boolean loop = false;

		// if another argument after the Powder name
		if (args.length > 1) {
			if (args[1].equalsIgnoreCase("cancel")) {
				int taskAmount =
						powderHandler.getPowderTasks(player.getUniqueId(), powder).size();
				// cancel if exists
				if (powder.cancel(player.getUniqueId())) {
					PowderUtil.sendPrefixMessage(player, Message.POWDER_CANCEL_SUCCESS,
							label, player.getName(), powder.getName(), String.valueOf(taskAmount));
				} else {
					PowderUtil.sendPrefixMessage(player, Message.POWDER_CANCEL_FAILURE,
							label, player.getName(), powder.getName());
				}
				return false;
			} else if (args[1].equalsIgnoreCase("loop")) {
				loop = true;
			}
		}

		// if this Powder is already running for the player
		if (!(powderHandler.getPowderTasks(player.getUniqueId(), powder).isEmpty())) {
			// if multiple uses of one Powder are not allowed, cancel it
			if (!(PowderPlugin.get().getConfig()
					.getBoolean("allowSamePowdersAtOneTime"))) {
				if (powder.cancel(player.getUniqueId())) {
					PowderUtil.sendPrefixMessage(player, Message.POWDER_CANCEL_SINGLE_SUCCESS,
							label, player.getName(), args[0]);
				} else {
					PowderUtil.sendPrefixMessage(player, Message.POWDER_CANCEL_SINGLE_FAILURE,
							label, player.getName(), args[0]);
				}
				return false;
			}
		}

		// if player has maxPowdersAtOneTime, don't do it
		int maxSize = PowderPlugin.get().getConfig().getInt("maxPowdersAtOneTime");
		if ((powderHandler.getPowderTasks(player.getUniqueId()).size() >= maxSize)) {
			PowderUtil.sendPrefixMessage(player, Message.POWDER_MAX_PREFIX,
					label, player.getName(), args[0], String.valueOf(maxSize));
			List<BaseComponent> texts = new ArrayList<>();
			for (PowderTask powderTask : powderHandler.getPowderTasks(player.getUniqueId())) {
				String powderName = powderTask.getPowder().getName();
				BaseComponent baseComponent = PowderUtil.setTextHoverAndClick(Message.ACTIVE_POWDER,
						Message.ACTIVE_POWDER_HOVER, Message.ACTIVE_POWDER_CLICK,
						player.getName(), label, powderName);
				texts.add(baseComponent);
			}
			PowderUtil.paginateAndSend(player, texts, "/" + label + " " +
					powder.getName(), 1, pageLength, label);
			return false;
		}

		// spawn it!
		spawnPowderThroughCommand(powder, player, label, args[0], loop);
		return true;
	}

	public boolean eq(String arg, SubCommand command) {
		for (String label : command.getLabels()) {
			if (arg.equalsIgnoreCase(label)) return true;
		}
		return false;
	}

	public void spawnPowderThroughCommand(Powder powder, Player player, String label, String arg, boolean loop) {
		// wait time between creating each Powder
		int waitTime = PowderPlugin.get().getConfig().getInt("secondsBetweenPowderUsage");
		// if they sent a command in the given wait time, don't do it
		if (recentCommandSenders.contains(player)) {
			PowderUtil.sendPrefixMessage(player, Message.POWDER_WAIT,
					label, player.getName(), arg, String.valueOf(waitTime));
			return;
		}
		// if there's a wait time between using each Powder
		if (waitTime > 0) {
			// add user to this list of recent command senders for the given amount of time
			PowderPlugin.get().getServer().getScheduler()
			.scheduleSyncDelayedTask(PowderPlugin.get(), () -> {
				recentCommandSenders.remove(player);
			}, (waitTime * 20));
			recentCommandSenders.add(player);
		}

		if (loop) powder = powder.loop();

		// spawn the Powder
		powder.spawn(player);

		PowderUtil.sendPrefixMessage(player, PowderUtil.setTextHoverAndClick(
				Message.POWDER_CREATED, Message.POWDER_CREATED_HOVER,
				Message.POWDER_CREATED_CLICK, player.getName(),
				label, powder.getName()), label);
		if (new Random().nextInt(12) == 1) {
			PowderUtil.sendPrefixMessage(player, Message.POWDER_CREATED_TIP,
					label, player.getName(), label);
		}
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
		List<String> strings = new ArrayList<>();

		if (PowderPlugin.isLoading()) return strings;

		if (!(sender instanceof Player)) return strings;
		Player player = (Player) sender;

		if (args.length == 1) {
			for (SubCommand subCommand : this.subCommands) {
				for (String label : subCommand.getLabels()) {
					if (label.toLowerCase().startsWith(args[0].toLowerCase())) {
						strings.add(label);
					}
				}
			}
			for (String category : PowderPlugin.get().getPowderHandler().getCategories().keySet()) {
				if (category.toLowerCase().startsWith(args[0].toLowerCase())) {
					strings.add(category);
				}
			}
			for (Powder powder : PowderPlugin.get().getPowderHandler().getPowders()) {
				if (powder.getName().toLowerCase().startsWith(args[0].toLowerCase())) {
					if (!PowderUtil.hasPermission(player, powder)) continue;
					strings.add(powder.getName());
				}
			}
		} else if (args.length == 2) {
			if (args[0].equalsIgnoreCase("category")) {
				for (String category : PowderPlugin.get().getPowderHandler().getCategories().keySet()) {
					if (category.toLowerCase().startsWith(args[1].toLowerCase())) {
						strings.add(category);
					}
				}
			} else if (args[0].equalsIgnoreCase("attach")) {
				for (Powder powder : PowderPlugin.get().getPowderHandler().getPowders()) {
					if (powder.getName().toLowerCase().startsWith(args[1].toLowerCase())) {
						if (!PowderUtil.hasPermission(player, powder)) continue;
						strings.add(powder.getName());
					}
				}
			} else if (args[0].equalsIgnoreCase("remove")) {
				for (PowderTask task : PowderPlugin.get().getPowderHandler().getPowderTasks(player.getUniqueId())) {
					if (task.getName().toLowerCase().startsWith(args[1].toLowerCase())) {
						strings.add(task.getName());
					}
				}
			} else if (args[0].equalsIgnoreCase("cancel")) {
				for (PowderTask task : PowderPlugin.get().getPowderHandler().getPowderTasks(player.getUniqueId())) {
					if (task.getPowder().getName().toLowerCase().startsWith(args[1].toLowerCase())) {
						strings.add(task.getPowder().getName());
					}
					if (task.getName().toLowerCase().startsWith(args[1].toLowerCase())) {
						strings.add(task.getName());
					}
				}
			} else if (args[0].equalsIgnoreCase("arrow")) {
				String[] subCommands = { "trail", "hit", "removetrail", "removehit" };
				for (String subCommand : subCommands) {
					if (subCommand.startsWith(args[1].toLowerCase())) strings.add(subCommand);
				}
			} else if (args[0].equalsIgnoreCase("station")) {
				for (Powder powder : PowderPlugin.get().getPowderHandler().getPowders()) {
					if (powder.getName().toLowerCase().startsWith(args[1].toLowerCase())) {
						if (!PowderUtil.hasPermission(player, powder)) continue;
						strings.add(powder.getName());
					}
				}
			} else {
				for (Powder powder : PowderPlugin.get().getPowderHandler().getPowders()) {
					for (String category : powder.getCategories()) {
						if (category.equalsIgnoreCase(args[0])) {
							if (powder.getName().toLowerCase().startsWith(args[1].toLowerCase())) {
								if (!PowderUtil.hasPermission(player, powder)) continue;
								strings.add(powder.getName());
							}
						}
					}
				}
			}
		} else if (args.length == 3) {
			if (args[0].equalsIgnoreCase("arrow")) {
				if (args[1].equalsIgnoreCase("hit") || args[1].equalsIgnoreCase("trail")) {
					for (Powder powder : PowderPlugin.get().getPowderHandler().getPowders()) {
						if (powder.getName().toLowerCase().startsWith(args[2].toLowerCase())) {
							if (!PowderUtil.hasPermission(player, powder)) continue;
							strings.add(powder.getName());
						}
					}
				}
			}
		} else if (args.length == 4) {
			if (args[0].equalsIgnoreCase("arrow")) {
				if (args[1].equalsIgnoreCase("hit") || args[1].equalsIgnoreCase("trail")) {
					strings.add("loop");
				}
			}
		} else {
			return strings;
		}

		return strings;
	}

}
