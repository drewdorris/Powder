package com.ruinscraft.powder;

import com.google.common.collect.Iterables;
import com.ruinscraft.powder.model.Message;
import com.ruinscraft.powder.model.Powder;
import com.ruinscraft.powder.model.PowderTask;
import com.ruinscraft.powder.model.tracker.StationaryTracker;
import com.ruinscraft.powder.util.PowderUtil;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.*;

public class PowderCommand implements CommandExecutor, TabCompleter {

	private List<Player> recentCommandSenders = new ArrayList<>();

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

		// if the first argument given is not a Powder
		if (powder == null) {
			if (args[0].equalsIgnoreCase("help")) {
				int page;
				try {
					page = Integer.valueOf(args[1]);
				} catch (Exception e) {
					page = 1;
				}
				PowderUtil.helpMessage(player, label, page);
				return false;
			} else if (args[0].equalsIgnoreCase("reload")) {
				if (!(player.hasPermission("powder.reload"))) {
					PowderUtil.sendPrefixMessage(player,
							Message.GENERAL_NO_PERMISSION, label, player.getName());
					return false;
				}
				if (PowderPlugin.isLoading()) {
					PowderUtil.sendPrefixMessage(player, Message.LOADING_ALREADY,
							label, player.getName());
				} else {
					PowderPlugin.get().getServer().getScheduler()
					.runTaskAsynchronously(PowderPlugin.get(), () -> {
						PowderPlugin.get().reload();
					});
				}
				return true;
			} else if (args[0].equalsIgnoreCase("*")) {
				if (args.length < 2) {
					PowderUtil.sendPrefixMessage(player, Message.STAR_USE_CANCEL,
							label, player.getName());
					return false;
				} else {
					// cancel all Powders
					if (powderHandler.getPowderTasks(player.getUniqueId()).isEmpty()) {
						PowderUtil.sendPrefixMessage(player,
								Message.STAR_NO_ACTIVE, label, player.getName());
						return false;
					}
					int amount = PowderUtil.cancelAllPowders(player.getUniqueId());
					PowderUtil.sendPrefixMessage(player, Message.STAR_CANCEL_SUCCESS,
							label, player.getName(), String.valueOf(amount));
					return true;
				}
				// list Powders, not by category
			} else if (args[0].equalsIgnoreCase("list")) {
				int page;
				try {
					page = Integer.valueOf(args[1]);
				} catch (Exception e) {
					page = 1;
				}
				PowderUtil.listPowders(player, powderHandler.getPowders(),
						" list ", page, pageLength, label);
				return false;
				// list all categories
			} else if (args[0].equalsIgnoreCase("cancel")) {
				String powderTaskName;
				try {
					powderTaskName = args[1];
				} catch (Exception e) {
					PowderUtil.sendPrefixMessage(player,
							Message.CANCEL_SPECIFY, label, player.getName());
					return false;
				}
				if (powderHandler.getPowder(powderTaskName) != null) {
					Set<PowderTask> powderTasks = powderHandler.getPowderTasks(
							player.getUniqueId(),
							powderHandler.getPowder(powderTaskName));
					if (powderTasks.isEmpty()) {
						PowderUtil.sendPrefixMessage(player,
								Message.CANCEL_NO_ACTIVE, label, player.getName(),
								powderTaskName);
						return false;
					} else {
						powderTaskName = Iterables.get(powderTasks, 0).getName();
					}
				}
				PowderTask powderTask = powderHandler.getPowderTask(powderTaskName);
				if (!player.hasPermission("powder.cancel") &&
						!powderTask.getUUIDsIfExist().contains(player.getUniqueId())) {
					PowderUtil.sendPrefixMessage(player, Message.CANCEL_FAILURE,
							label, player.getName(), powderTaskName);
					return false;
				}
				if (powderTask == null) {
					PowderUtil.sendPrefixMessage(player, Message.CANCEL_UNKNOWN_SPECIFY,
							label, player.getName(), powderTaskName);
					return false;
				}
				if (powderHandler.cancelPowderTask(powderTask)) {
					PowderUtil.sendPrefixMessage(player, Message.CANCEL_SUCCESS,
							label, player.getName(), powderTaskName);
				} else {
					PowderUtil.sendPrefixMessage(player, Message.CANCEL_FAILURE,
							label, player.getName(), powderTaskName);
				}
				return false;
				// list all categories
			} else if (args[0].equalsIgnoreCase("active")) {
				PowderUtil.sendPrefixMessage(player,
						Message.ACTIVE_PREFIX, label, player.getName());
				int page;
				try {
					page = Integer.valueOf(args[1]);
				} catch (Exception e) {
					page = 1;
				}
				List<BaseComponent> textComponents = new ArrayList<>();
				for (PowderTask powderTask : powderHandler.getPowderTasks(player.getUniqueId())) {
					String powderName = "null";
					for (Powder taskPowder : powderTask.getPowders().keySet()) {
						powderName = taskPowder.getName();
						break;
					}
					BaseComponent baseComponent = PowderUtil.setTextHoverAndClick(Message.ACTIVE_POWDER,
							Message.ACTIVE_POWDER_HOVER, Message.ACTIVE_POWDER_CLICK,
							player.getName(), label, powderName);
					textComponents.add(baseComponent);
				}
				PowderUtil.paginateAndSend(player, textComponents, " active ", page, 7, label);
				return false;
				// list Powders by category
			} else if (args[0].equalsIgnoreCase("categories")) {
				if (!powderHandler.categoriesEnabled()) {
					PowderUtil.sendPrefixMessage(player,
							Message.CATEGORIES_NOT_ENABLED, label, player.getName());
					PowderUtil.listPowders(player, powderHandler.getPowders(),
							" list ", 1, pageLength, label);
					return false;
				}
				int page;
				try {
					page = Integer.valueOf(args[1]);
				} catch (Exception e) {
					page = 1;
				}
				PowderUtil.listCategories(player, powderHandler.getCategories(),
						" categories ", page, pageLength, label);
				return false;
				// list Powders by category
			} else if (args[0].equalsIgnoreCase("category")) {
				if (!powderHandler.categoriesEnabled()) {
					PowderUtil.sendPrefixMessage(player,
							Message.CATEGORIES_NOT_ENABLED, label, player.getName());
					PowderUtil.listPowders(player, powderHandler.getPowders(),
							" list ", 1, pageLength, label);
					return false;
				}
				String category;
				int page;
				try {
					category = args[1];
				} catch (Exception e) {
					PowderUtil.sendPrefixMessage(player,
							Message.CATEGORY_SYNTAX, label, player.getName(), label);
					return false;
				}
				try {
					page = Integer.valueOf(args[2]);
				} catch (Exception e) {
					page = 1;
				}
				// list categories with similar names if given category does not exist
				if (powderHandler.getCategory(category) == null) {
					PowderUtil.sendPrefixMessage(player,
							Message.CATEGORY_UNKNOWN, label, player.getName(), category);
					PowderUtil.listCategories(player, powderHandler.getSimilarCategories(category),
							" category " + category + " ", page, pageLength, label);
					// else, list Powders by category
				} else {
					String correctCategory = powderHandler.getCategory(category);
					PowderUtil.listPowders(player,
							powderHandler.getPowdersFromCategory(correctCategory),
							" category " + category + " ", page, pageLength, label);
				}
				return true;
				// search by Powder name
			} else if (args[0].equalsIgnoreCase("search")) {
				String search;
				int page;
				try {
					search = args[1];
				} catch (Exception e) {
					PowderUtil.sendPrefixMessage(player,
							Message.SEARCH_SYNTAX, label, player.getName(), label);
					return false;
				}
				try {
					page = Integer.valueOf(args[2]);
				} catch (Exception e) {
					page = 1;
				}
				PowderUtil.listPowders(player, powderHandler.getSimilarPowders(search),
						" search " + search + " ", page, pageLength, label);
				return false;
			} else if (args[0].equalsIgnoreCase("attach")) {
				if (!(player.hasPermission("powder.attach"))) {
					PowderUtil.sendPrefixMessage(player,
							Message.GENERAL_NO_PERMISSION, label, player.getName());
					return false;
				}
				String powderName;
				Powder newPowder;
				try {
					powderName = args[1];
					newPowder = powderHandler.getPowder(powderName);
				} catch (Exception e) {
					PowderUtil.sendPrefixMessage(player,
							Message.ATTACH_SYNTAX, label, player.getName(), label);
					return false;
				}
				if (newPowder == null) {
					PowderUtil.sendPrefixMessage(player,
							Message.ATTACH_UNKNOWN, label, player.getName(), powderName);
					return false;
				}
				Entity entity = PowderUtil.getNearestEntityInSight(player, 7);
				if (entity == null) {
					PowderUtil.sendPrefixMessage(player,
							Message.ATTACH_NO_ENTITY, label, player.getName());
					return false;
				}
				if (entity instanceof Player) {
					newPowder.spawn((Player) entity);
					PowderUtil.sendPrefixMessage(player, Message.ATTACH_SUCCESS_PLAYER,
							label, player.getName(), powderName, entity.getName());
					return true;
				} else if (entity instanceof LivingEntity) {
					LivingEntity livingEntity = (LivingEntity) entity;
					livingEntity.setRemoveWhenFarAway(false);
					newPowder.spawn(livingEntity);
				} else {
					newPowder.spawn(entity);
				}
				PowderUtil.sendPrefixMessage(player, Message.ATTACH_SUCCESS_ENTITY,
						label, player.getName(), powderName,
						PowderUtil.cleanEntityName(entity));
				return true;
			} else if (args[0].equalsIgnoreCase("create")) {
				if (!(player.hasPermission("powder.create"))) {
					PowderUtil.sendPrefixMessage(player,
							Message.GENERAL_NO_PERMISSION, label, player.getName());
					return false;
				}
				String name;
				String powderName;
				Powder newPowder;
				try {
					name = args[1];
					powderName = args[2];
					newPowder = powderHandler.getPowder(powderName);
				} catch (Exception e) {
					PowderUtil.sendPrefixMessage(player, Message.CREATE_SYNTAX,
							label, player.getName(), label);
					return false;
				}
				if (newPowder == null) {
					PowderUtil.sendPrefixMessage(player, Message.CREATE_UNKNOWN, label,
							player.getName(), powderName);
					return false;
				}
				if (!(player.hasPermission("powder.createany"))) {
					if (PowderPlugin.get().hasTowny()) {
						if (!PowderPlugin.get().getTownyHandler().checkLocation(newPowder, player)) {
							PowderUtil.sendPrefixMessage(player, Message.CREATE_TOWNY_NO_PLACE, label,
									player.getName(), powderName);
							return false;
						}
					}
					if (PowderPlugin.get().hasPlotSquared()) {
						if (!PowderPlugin.get().getPlotSquaredHandler().checkLocation(newPowder, player)) {
							PowderUtil.sendPrefixMessage(player, Message.CREATE_PLOTSQUARED_NO_PLACE, label,
									player.getName(), powderName);
							return false;
						}
					}
				}
				if (args.length > 3) {
					if (args[3].equalsIgnoreCase("loop")) {
						newPowder = newPowder.loop();
					}
				}
				if (!(powderHandler.getPowderTask(name) == null)) {
					PowderUtil.sendPrefixMessage(player, Message.CREATE_ALREADY_EXISTS,
							label, player.getName(), name, powderName);
					return false;
				}
				newPowder.spawn(name, player.getLocation(), player.getUniqueId());
				PowderUtil.sendPrefixMessage(player, Message.CREATE_SUCCESS, label,
						player.getName(), powderName, name);
				return true;
			} else if (args[0].equalsIgnoreCase("addto")) {
				if (!(player.hasPermission("powder.addto"))) {
					PowderUtil.sendPrefixMessage(player,
							Message.GENERAL_NO_PERMISSION, label, player.getName());
					return false;
				}
				String name;
				String powderName;
				Powder newPowder;
				try {
					name = args[1];
					powderName = args[2];
					newPowder = powderHandler.getPowder(powderName);
				} catch (Exception e) {
					PowderUtil.sendPrefixMessage(player, Message.ADDTO_SYNTAX, label,
							player.getName(), label);
					return false;
				}
				if ((powderHandler.getPowderTask(name) == null)) {
					PowderUtil.sendPrefixMessage(player, Message.ADDTO_DOES_NOT_EXIST,
							label, player.getName(), name);
					return false;
				}
				if (newPowder == null) {
					PowderUtil.sendPrefixMessage(player, Message.ADDTO_UNKNOWN,
							label, player.getName(), powderName);
					return false;
				}
				PowderTask powderTask = powderHandler.getPowderTask(name);
				if (powderTask.addPowder(newPowder,
						new StationaryTracker(player.getLocation(), player.getUniqueId()))) {
					PowderUtil.sendPrefixMessage(player, Message.ADDTO_SUCCESS,
							label, player.getName(), powderName, name);
				} else {
					PowderUtil.sendPrefixMessage(player, Message.ADDTO_FAILURE,
							label, player.getName(), powderName, name);
					return false;
				}
				return true;
			} else if (args[0].equalsIgnoreCase("removefrom")) {
				if (!(player.hasPermission("powder.removefrom"))) {
					PowderUtil.sendPrefixMessage(player,
							Message.GENERAL_NO_PERMISSION, label, player.getName());
					return false;
				}
				String name;
				String powderName;
				Powder newPowder;
				try {
					name = args[1];
					powderName = args[2];
					newPowder = powderHandler.getPowder(powderName);
				} catch (Exception e) {
					PowderUtil.sendPrefixMessage(player, Message.REMOVEFROM_SYNTAX,
							label, player.getName(), label);
					return false;
				}
				if ((powderHandler.getPowderTask(name) == null)) {
					PowderUtil.sendPrefixMessage(player, Message.REMOVEFROM_DOES_NOT_EXIST,
							label, player.getName(), name);
					return false;
				}
				if (newPowder == null) {
					PowderUtil.sendPrefixMessage(player, Message.REMOVEFROM_UNKNOWN,
							label, player.getName(), powderName);
					return false;
				}
				PowderTask powderTask = powderHandler.getPowderTask(name);
				if (powderTask.removePowder(newPowder)) {
					PowderUtil.sendPrefixMessage(player, Message.REMOVEFROM_SUCCESS,
							label, player.getName(), powderName, name);
				} else {
					PowderUtil.sendPrefixMessage(player, Message.REMOVEFROM_FAILURE,
							label, player.getName(), powderName, name);
					return false;
				}
				return true;
			} else if (args[0].equalsIgnoreCase("remove")) {
				if (!(player.hasPermission("powder.remove"))) {
					PowderUtil.sendPrefixMessage(player,
							Message.GENERAL_NO_PERMISSION, label, player.getName());
					return false;
				}
				if (args.length < 2) {
					PowderUtil.sendPrefixMessage(player, Message.REMOVE_SYNTAX,
							label, player.getName(), label);
					return false;
				}
				if (args[1].equalsIgnoreCase("user")) {
					Player powderUser;
					try {
						powderUser = Bukkit.getPlayer(args[2]);
					} catch (Exception e) {
						PowderUtil.sendPrefixMessage(player, Message.REMOVE_INVALID_PLAYER,
								label, player.getName());
						return false;
					}
					if (powderUser == null) {
						PowderUtil.sendPrefixMessage(player, Message.REMOVE_INVALID_PLAYER,
								label, player.getName());
						return false;
					}
					if (powderHandler.cancelPowderTasks(
							powderHandler.getPowderTasks(powderUser.getUniqueId()))) {
						if (!(powderUser.equals(player))) {
							PowderUtil.sendPrefixMessage(powderUser, Message.REMOVE_USER_REMOVED_BY,
									label, player.getName(), powderUser.getName());
						}
						PowderUtil.sendPrefixMessage(player, Message.REMOVE_USER_REMOVE_SUCCESS,
								label, player.getName(), powderUser.getName());
					} else {
						PowderUtil.sendPrefixMessage(player, Message.REMOVE_USER_REMOVE_FAILURE,
								label, player.getName(), powderUser.getName());
						return false;
					}
					return true;
				} else {
					String name;
					try {
						name = args[1];
					} catch (Exception e) {
						PowderUtil.sendPrefixMessage(player, Message.REMOVE_NO_USER_SYNTAX,
								label, player.getName(), label);
						return false;
					}
					if (powderHandler.getPowderTask(name) == null) {
						PowderUtil.sendPrefixMessage(player,
								Message.REMOVE_NO_USER_DOES_NOT_EXIST,
								label, player.getName(), name);
						return false;
					}
					if (powderHandler.cancelPowderTask(powderHandler.getPowderTask(name))) {
						PowderUtil.sendPrefixMessage(player, Message.REMOVE_NO_USER_SUCCESS,
								label, player.getName(), name);
					} else {
						PowderUtil.sendPrefixMessage(player, Message.REMOVE_NO_USER_FAILURE,
								label, player.getName(), name);
						return false;
					}
					return true;
				}
			} else if (args[0].equalsIgnoreCase("nearby") || args[0].equalsIgnoreCase("near")) {
				if (!(player.hasPermission("powder.nearby"))) {
					PowderUtil.sendPrefixMessage(player,
							Message.GENERAL_NO_PERMISSION, label, player.getName());
					return false;
				}
				int range = 200;
				int page = 1;
				try {
					range = Integer.valueOf(args[1]);
					page = Integer.valueOf(args[2]);
				} catch (Exception e) {
				}
				PowderUtil.sendPrefixMessage(player, Message.NEARBY_PREFIX, label);
				Map<PowderTask, Integer> nearby =
						powderHandler.getNearbyPowderTasks(player.getLocation(), range);
				List<BaseComponent> nearbyText = new ArrayList<>();
				for (PowderTask powderTask : nearby.keySet()) {
					BaseComponent baseComponent = PowderUtil.setText(Message.NEARBY,
							powderTask.getName(), String.valueOf(nearby.get(powderTask)));
					if (player.hasPermission("powder.remove")) {
						Set<Powder> taskPowders = powderTask.getPowders().keySet();
						StringBuilder stringBuilder = new StringBuilder();
						for (Powder taskPowder : taskPowders) {
							stringBuilder.append(taskPowder.getName());
							if (!(Iterables.get(taskPowders,
									taskPowders.size() - 1) == taskPowder)) {
								stringBuilder.append(", ");
							}
						}
						baseComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
								new ComponentBuilder(PowderUtil.setString(
										Message.NEARBY_HOVER, powderTask.getName(),
										stringBuilder.toString()))
								.create()));
						baseComponent.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
								PowderUtil.setString(Message.NEARBY_CLICK,
										label, powderTask.getName())));
					}
					nearbyText.add(baseComponent);
				}
				PowderUtil.paginateAndSend(player, nearbyText, " nearby " + range + " ", page, 7, label);
				return true;
			} else {
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
				String powderName = "null";
				for (Powder taskPowder : powderTask.getPowders().keySet()) {
					powderName = taskPowder.getName();
				}
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

		if (args.length == 1) {
			for (String category : PowderPlugin.get().getPowderHandler().getCategories().keySet()) {
				if (category.toLowerCase().startsWith(args[0].toLowerCase())) {
					strings.add(category);
				}
			}
			if ("category".startsWith(args[0].toLowerCase())) {
				strings.add("Category");
			}
		} else if (args.length == 2) {
			if (args[0].equalsIgnoreCase("category")) {
				for (String category : PowderPlugin.get().getPowderHandler().getCategories().keySet()) {
					if (category.toLowerCase().startsWith(args[1].toLowerCase())) {
						strings.add(category);
					}
				}
			} else {
				for (Powder powder : PowderPlugin.get().getPowderHandler().getPowders()) {
					for (String category : powder.getCategories()) {
						if (category.equalsIgnoreCase(args[0])) {
							if (powder.getName().toLowerCase().startsWith(args[1])) {
								strings.add(powder.getName());
							}
						}
					}
				}
			}
		} else {
			return strings;
		}

		return strings;
	}

}
