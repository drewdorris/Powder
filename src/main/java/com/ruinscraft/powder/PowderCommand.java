package com.ruinscraft.powder;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.ruinscraft.powder.objects.ChangedParticle;
import com.ruinscraft.powder.objects.Dust;
import com.ruinscraft.powder.objects.ParticleMap;
import com.ruinscraft.powder.objects.ParticleName;
import com.ruinscraft.powder.objects.PowderMap;
import com.ruinscraft.powder.objects.PowderTask;
import com.ruinscraft.powder.objects.SoundEffect;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

public class PowderCommand implements CommandExecutor {

	private List<Player> recentCommandSenders = new ArrayList<Player>();

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

		if (!(sender instanceof Player)) {
			sender.sendMessage(Powder.getInstance().getName() + " | " + Powder.getInstance().getDescription());
			return true;
		}

		Player player = (Player) sender;
		if (!(player.hasPermission("powder.command"))) {
			sendPrefixMessage(player, ChatColor.RED + "You don't have permission to use /" + label + ".", label);
			return false;
		}

		PowderHandler powderHandler = Powder.getInstance().getPowderHandler();
		
		int pageLength = Powder.getInstance().getConfig().getInt("pageLength");

		if (args.length < 1) {
			powderList(player, powderHandler, 1, pageLength, label);
			return false;
		}

		PowderMap map = powderHandler.getPowderMap(args[0]);

		if (map == null) {
			
			if (args[0].equals("help")) {
				
				helpMessage(player, label);
				return false;
				
			} else if (args[0].equals("reload")) {
				
				if (!(player.hasPermission("powder.reload"))) {
					sendPrefixMessage(player, ChatColor.RED + "You don't have permission to do this.", label);
					return false;
				}
				Powder.getInstance().handleConfig();
				sendPrefixMessage(player, ChatColor.GRAY + "Powder config.yml reloaded!", label);
				List<Player> playersDoneAlready = new ArrayList<Player>();
				for (PowderTask powderTask : powderHandler.getPowderTasks()) {
					if (playersDoneAlready.contains(powderTask.getPlayer())) {
						continue;
					}
					playersDoneAlready.add(powderTask.getPlayer());
					sendPrefixMessage(powderTask.getPlayer(), ChatColor.GRAY 
							+ "Your Powders were cancelled due to " + "a reload.", label);
				}
				return true;
				
			} else if (args[0].equals("*")) {
				
				if (args.length < 2) {
					sendPrefixMessage(player, ChatColor.GRAY 
							+ "Use '* cancel' to cancel all current active Powders.", label);
					return false;
				} else {
					if (powderHandler.getPowderTasks(player).isEmpty()) {
						sendPrefixMessage(player, ChatColor.RED + "There are no Powders currently active.", label);
						return false;
					}
					int amount = powderHandler.getPowderTasks(player).size();
					for (PowderTask powderTask : powderHandler.getPowderTasks(player)) {
						powderHandler.removePowderTask(powderTask);
					}
					sendPrefixMessage(player, ChatColor.GRAY + "Successfully cancelled all Powders! (" + 
							amount + " total)", label);
					return true;
				}
				
			} else if (args[0].equals("list")) {
				
				int page;
				try {
					page = Integer.valueOf(args[1]);
				} catch (Exception e) {
					page = 1;
				}
				powderList(player, powderHandler, page, pageLength, label);
				return false;
				
			} else {
				
				powderList(player, powderHandler, 1, pageLength, label);
				return false;
				
			}
			
		}

		if (!(player.hasPermission("powder.powder.*"))) {
			if (!(player.hasPermission("powder.powder." + map.getName().toLowerCase(Locale.US)))) {
				sendPrefixMessage(player, ChatColor.RED 
						+ "You don't have permission to use the Powder '" + map.getName() + "'.", label);
				return false;
			}
		}
		
		if (args.length > 1) {
			if (args[1].equalsIgnoreCase("cancel")) {
				boolean success = false;
				int taskAmount = powderHandler.getPowderTasks(player, map).size();
				for (PowderTask powderTask : powderHandler.getPowderTasks(player, map)) {
					powderHandler.removePowderTask(powderTask);
					success = true;
				}
				if (success) {
					sendPrefixMessage(player, ChatColor.GRAY + "Powder '" + map.getName() + "' cancelled! (" + 
							taskAmount + " total)", label);
				} else {
					sendPrefixMessage(player, ChatColor.RED 
							+ "There are no '" + map.getName() + "' Powders currently active.", label);
				}
			}
			return false;
		}
		
		if (!(powderHandler.getPowderTasks(player, map).isEmpty())) {
			if (!(Powder.getInstance().getConfig().getBoolean("allowSamePowdersAtOneTime"))) {
				boolean success = false;
				for (PowderTask powderTask : powderHandler.getPowderTasks(player, map)) {
					powderHandler.removePowderTask(powderTask);
					success = true;
				}
				if (success) {
					sendPrefixMessage(player, ChatColor.GRAY + "Powder '" + map.getName() + "' cancelled!", label);
				} else {
					sendPrefixMessage(player, ChatColor.RED 
							+ "There are no active '" + map.getName() + "' Powders.", label);
				}
				return false;
			}
		}

		int maxSize = Powder.getInstance().getConfig().getInt("maxPowdersAtOneTime");
		if ((powderHandler.getPowderTasks(player).size() >= maxSize)) {
			sendPrefixMessage(player, 
					ChatColor.RED + "You already have " + maxSize + " Powders active!", label);
			for (PowderTask powderTask : powderHandler.getPowderTasks(player)) {
				TextComponent runningTaskText = new TextComponent(net.md_5.bungee.api.ChatColor.GRAY + "| " 
							+ net.md_5.bungee.api.ChatColor.ITALIC + powderTask.getMap().getName());
				runningTaskText.setHoverEvent( new HoverEvent( HoverEvent.Action.SHOW_TEXT, 
						new ComponentBuilder("'" + powderTask.getMap().getName() + "' is currently active. Click to cancel")
						.color(net.md_5.bungee.api.ChatColor.YELLOW).create() ) );
				runningTaskText.setClickEvent( new ClickEvent( ClickEvent.Action.RUN_COMMAND, 
						"/" + label + " " + powderTask.getMap().getName() + " cancel" ) );
				player.spigot().sendMessage(runningTaskText);
			}
			return false;
		}
		if (!(Powder.getInstance().getConfig().getBoolean("allowSamePowdersAtOneTime"))) {
			for (PowderTask powderTask : powderHandler.getPowderTasks(player)) {
				if (powderTask.getMap().equals(map)) {
					sendPrefixMessage(player, 
							ChatColor.RED + "'" + map.getName() + "' is already active!", label);
					return false;
				}
			}
		}

		// wait time between each command
		int waitTime = Powder.getInstance().getConfig().getInt("secondsBetweenPowderUsage");
		if (recentCommandSenders.contains(player)) {
			sendPrefixMessage(player, 
					ChatColor.RED + "Please wait " + waitTime + " seconds between using each Powder.", label);
			return false;
		}
		Powder.getInstance().getServer().getScheduler().scheduleSyncDelayedTask(Powder.getInstance(), new Runnable() {
			public void run() {
				recentCommandSenders.remove(player);
			}
		}, (waitTime * 20));
		recentCommandSenders.add(player);

		int task;
		List<Integer> tasks = new ArrayList<Integer>();

		if (map.isRepeating()) {

			task = Powder.getInstance().getServer().getScheduler().scheduleSyncRepeatingTask(Powder.getInstance(), new Runnable() {
				public void run() {
					tasks.addAll(createEverything(player, map, powderHandler));
				}
			}, 0L, map.getDelay());

			tasks.add(task);

		} else {
			tasks.addAll(createEverything(player, map, powderHandler));
		}
		
		tasks.addAll(createDusts(player, map, powderHandler));
		
		if (map.isRepeating() || map.getMaps().size() > 1 || !(map.getDusts().isEmpty())) {
			TextComponent particleSentText = new TextComponent(net.md_5.bungee.api.ChatColor.GRAY 
					+ "Powder '" + map.getName() + "' created! Click to cancel.");
			particleSentText.setHoverEvent( new HoverEvent( HoverEvent.Action.SHOW_TEXT, 
				new ComponentBuilder("Click to cancel '" + map.getName() + "'.")
				.color(net.md_5.bungee.api.ChatColor.YELLOW).create() ) );
			particleSentText.setClickEvent( new ClickEvent( ClickEvent.Action.RUN_COMMAND, 
				"/" + label + " " + map.getName() + " cancel" ) );
			sendPrefixMessage(player, particleSentText, label);
			if (new Random().nextInt(12) == 1) {
				sendPrefixMessage(player, ChatColor.GRAY + 
						"Tip: Cancel with '/" + label + " <Powder> cancel'.", label);
			}
		} else {
			sendPrefixMessage(player, net.md_5.bungee.api.ChatColor.GRAY 
					+ "Powder '" + map.getName() + "' created!", label);
		}

		PowderTask powderTask = new PowderTask(player, tasks, map);
		powderHandler.addPowderTask(powderTask);

		return true;

	}
	
	public static void helpMessage(Player player, String label) {
		
		sendPrefixMessage(player, ChatColor.GRAY + "Powder Help", label);
		player.sendMessage(ChatColor.GRAY + "| " + ChatColor.RED + "/powder (powder) " + ChatColor.GRAY + "- Use a Powder"); 
		player.sendMessage(ChatColor.GRAY + "| " + ChatColor.RED + "/powder (powder) cancel " + ChatColor.GRAY + "- Cancel a Powder"); 
		player.sendMessage(ChatColor.GRAY + "| " + ChatColor.RED + "/powder * cancel " + ChatColor.GRAY + "- Cancel all Powders"); 
		player.sendMessage(ChatColor.GRAY + "| " + ChatColor.RED + "/powder list [page] " + ChatColor.GRAY + "- List Powders by page");
		if (player.hasPermission("powder.reload")) {
			player.sendMessage(ChatColor.GRAY + "| " + ChatColor.RED + "/powder reload " + ChatColor.GRAY + "- Reload Powder"); 
		}
		player.sendMessage(ChatColor.GRAY + "It's also possible to " + ChatColor.RED + "click things in /" 
		+ label + ChatColor.GRAY + " to enable or cancel Powders. Click the prefix in a message to return to the menu."); 
		
	}
	
	public static void sendPrefixMessage(Player player, Object message, String label) {
		
		if (!(message instanceof String) && !(message instanceof BaseComponent)) {
			return;
		}
		if (message instanceof String) {
			String messageText = (String) message;
			message = new TextComponent(messageText);
		}
		
		BaseComponent fullMessage = new TextComponent();
		TextComponent prefix = new TextComponent(Powder.PREFIX);
		prefix.setHoverEvent( new HoverEvent( HoverEvent.Action.SHOW_TEXT, 
			new ComponentBuilder("/" + label).color(net.md_5.bungee.api.ChatColor.GRAY).create() ) );
		prefix.setClickEvent( new ClickEvent( ClickEvent.Action.RUN_COMMAND, "/" + label ) );
		fullMessage.addExtra(prefix);
		fullMessage.addExtra((TextComponent) message);
		
		player.spigot().sendMessage(fullMessage);
		
	}
	
	public static List<TextComponent> sortAlphabetically(List<TextComponent> powders) {
		
		List<String> names = new ArrayList<String>(powders.size()); 
		
		for (TextComponent powderName : powders) {
			names.add(powderName.getText());
		}
		
		Collections.sort(names, Collator.getInstance());
		List<TextComponent> newList = new ArrayList<TextComponent>(powders.size());
		
		for (String name : names) {
			for (TextComponent powderName : powders) {
				if (powderName.getText() == name) {
					newList.add(powderName);
					break;
				}
			}
		}
		
		return newList;
		
	}
	
	public static void paginate(Player player, List<TextComponent> list, int page, int pageLength, String label) {
		List<TextComponent> pageList = new ArrayList<TextComponent>();
		for (int i = 1; i <= pageLength; i++) {
			TextComponent current;
			try {
				current = list.get((page * pageLength) + i - pageLength - 1);
			} catch (Exception e) {
				break;
			}
			pageList.add(current);
			TextComponent combinedMessage = new TextComponent(net.md_5.bungee.api.ChatColor.GRAY + 
					"| " + net.md_5.bungee.api.ChatColor.RESET);;
			combinedMessage.addExtra(current);
			player.spigot().sendMessage(combinedMessage);
		}
		
		TextComponent leftArrow = new TextComponent("<<  ");
		leftArrow.setColor(net.md_5.bungee.api.ChatColor.RED);
		leftArrow.setClickEvent( new ClickEvent( ClickEvent.Action.RUN_COMMAND, 
				"/" + label + " list " + (page - 1) ) );
		leftArrow.setHoverEvent( new HoverEvent( HoverEvent.Action.SHOW_TEXT, 
				new ComponentBuilder("Previous Page")
				.color(net.md_5.bungee.api.ChatColor.RED).create() ) );
		
		TextComponent middle = new TextComponent("Page (click)");
		middle.setColor(net.md_5.bungee.api.ChatColor.RED);
		
		TextComponent rightArrow = new TextComponent("  >>");
		rightArrow.setColor(net.md_5.bungee.api.ChatColor.RED);
		rightArrow.setClickEvent( new ClickEvent( ClickEvent.Action.RUN_COMMAND, 
				"/" + label + " list " + (page + 1) ) );
		rightArrow.setHoverEvent( new HoverEvent( HoverEvent.Action.SHOW_TEXT, 
				new ComponentBuilder("Next Page")
				.color(net.md_5.bungee.api.ChatColor.RED).create() ) );
		
		TextComponent fullArrows = new TextComponent();
		if (pageList.contains(list.get(0)) && pageList.contains(list.get(list.size() - 1)) || 
				(!(pageList.contains(list.get(0))) && !(pageList.contains(list.get(list.size() - 1))))) {
			fullArrows.addExtra(leftArrow);
			fullArrows.addExtra(middle);
			fullArrows.addExtra(rightArrow);
		} else if (pageList.contains(list.get(0)) && !pageList.contains(list.get(list.size() - 1))) {
			fullArrows.addExtra(middle);
			fullArrows.addExtra(rightArrow);
		} else if (!pageList.contains(list.get(0)) && pageList.contains(list.get(list.size() - 1))) {
			fullArrows.addExtra(leftArrow);
			fullArrows.addExtra(middle);
		}
		player.spigot().sendMessage(fullArrows);
	}

	public static void powderList(Player player, PowderHandler phandler, int page, int pageLength, String label) {

		TextComponent helpPrefix = new TextComponent(net.md_5.bungee.api.ChatColor.GRAY + "Use " +
				net.md_5.bungee.api.ChatColor.RED + "/" + label +  " help" + net.md_5.bungee.api.ChatColor.GRAY + " for help.");
		helpPrefix.setHoverEvent( new HoverEvent( HoverEvent.Action.SHOW_TEXT, 
				new ComponentBuilder("/" + label + " help")
				.color(net.md_5.bungee.api.ChatColor.GRAY).create() ) );
		helpPrefix.setClickEvent( new ClickEvent( ClickEvent.Action.RUN_COMMAND, 
				"/" + label + " help" ) );
		sendPrefixMessage(player, helpPrefix, label);
		
		List<TextComponent> listOfPowders = new ArrayList<TextComponent>();
		List<TextComponent> activePowders = new ArrayList<TextComponent>();
		List<TextComponent> ableToPowders = new ArrayList<TextComponent>();
		List<TextComponent> noPermPowders = new ArrayList<TextComponent>();
		for (PowderMap powderMap : phandler.getPowderMaps()) {
			TextComponent powderMapText = new TextComponent(powderMap.getName());
			if (!(player.hasPermission("powder.powder." + powderMap.getName().toLowerCase(Locale.US)))) {
				if (powderMap.isHidden()) {
					continue;
				}
				powderMapText.setColor(net.md_5.bungee.api.ChatColor.DARK_GRAY);
				powderMapText.setHoverEvent( new HoverEvent( HoverEvent.Action.SHOW_TEXT, 
						new ComponentBuilder("You don't have permission to use '" + powderMap.getName() + "'.")
						.color(net.md_5.bungee.api.ChatColor.RED).create() ) );
				noPermPowders.add(powderMapText);
			} else if (!(phandler.getPowderTasks(player, powderMap).isEmpty())) {
				powderMapText.setColor(net.md_5.bungee.api.ChatColor.GREEN);
				powderMapText.setHoverEvent( new HoverEvent( HoverEvent.Action.SHOW_TEXT, 
						new ComponentBuilder("'" + powderMap.getName() + "' is currently active. Click to cancel")
						.color(net.md_5.bungee.api.ChatColor.GREEN).create() ) );
				powderMapText.setClickEvent( new ClickEvent( ClickEvent.Action.RUN_COMMAND, 
						"/" + label + " " + powderMap.getName() + " cancel" ) );
				activePowders.add(powderMapText);
			} else {
				powderMapText.setColor(net.md_5.bungee.api.ChatColor.GRAY);
				powderMapText.setHoverEvent( new HoverEvent( HoverEvent.Action.SHOW_TEXT, 
						new ComponentBuilder("Click to use '" + powderMap.getName() + "'.")
						.color(net.md_5.bungee.api.ChatColor.GRAY).create() ) );
				powderMapText.setClickEvent( new ClickEvent( ClickEvent.Action.RUN_COMMAND, 
						"/" + label + " " + powderMap.getName()) );
				ableToPowders.add(powderMapText);
			}
		}
		activePowders = sortAlphabetically(activePowders);
		ableToPowders = sortAlphabetically(ableToPowders);
		noPermPowders = sortAlphabetically(noPermPowders);
		listOfPowders.addAll(activePowders);
		listOfPowders.addAll(ableToPowders);
		listOfPowders.addAll(noPermPowders);
		paginate(player, listOfPowders, page, pageLength, label);

	}

	public static List<Integer> createEverything(final Player player, final PowderMap map, PowderHandler powderHandler) {

		List<Integer> tasks = new ArrayList<>();
		tasks.addAll(createParticles(player, map, powderHandler));
		tasks.addAll(createSounds(player, map, powderHandler));
		return tasks;
		
	}

	public static List<Integer> createParticles(final Player player, final PowderMap powderMap, PowderHandler powderHandler) {


		List<ParticleMap> particleMaps = powderMap.getMaps();
		
		List<Integer> tasks = new ArrayList<Integer>();

		for (ParticleMap particleMap : particleMaps) {
			
			final float spacing = particleMap.getSpacing();

			long waitTime = particleMap.getTick();

			int task = Powder.getInstance().getServer()
					.getScheduler().scheduleSyncDelayedTask(Powder.getInstance(), new Runnable() {

						@Override
						public void run() {

							if (!player.isOnline()) {
								return;
							}
							Location location = Bukkit.getPlayer(player.getName()).getEyeLocation();

							final double rot = ((player.getLocation().getYaw()) % 360) * (Math.PI / 180);
							final double pitch;

							final double ydist;
							final double xzdist;

							if (powderMap.hasPitch()) {
								pitch = (player.getLocation().getPitch() * (Math.PI / 180));
								ydist = ((Math.sin((Math.PI / 2) - pitch)) * spacing);
								xzdist = ((Math.cos((Math.PI / 2) + pitch)) * spacing);
							} else {
								pitch = 0;
								ydist = spacing;
								xzdist = 0;
							}

							// more doubles
							final double fx = getDirLengthX(rot, spacing);
							final double fz = getDirLengthZ(rot, spacing);
							final double rfx = getDirLengthX(rot + (Math.PI / 2), spacing);
							final double rfz = getDirLengthZ(rot + (Math.PI / 2), spacing);
							final double gx = getDirLengthX(rot - (Math.PI / 2), xzdist);
							final double gz = getDirLengthZ(rot - (Math.PI / 2), xzdist);
							final double rgx = getDirLengthX(rot, xzdist);
							final double rgz = getDirLengthZ(rot, xzdist);
							final double rgy = (Math.sin(0 - pitch) * spacing);

							final double sx = location.getX() - 
									(fx * particleMap.getPlayerLeft()) + (gx * particleMap.getPlayerUp());
							final double sy = location.getY() + (ydist * particleMap.getPlayerUp());
							final double sz = location.getZ() - 
									(fz * particleMap.getPlayerLeft()) + (gz * particleMap.getPlayerUp());

							double ssx = sx;
							double ssy = sy;
							double ssz = sz;

							StringBuilder sb = new StringBuilder();
							double result = 0;
							boolean buildAnInt = false;
							boolean lastCharInt = false;
							int downSoFar = 0;

							for (Object a : particleMap.getMap()) {

								String aa = String.valueOf(a);

								if (aa.equals(".")) {
									ssx = ssx + fx;
									ssz = ssz + fz;
									lastCharInt = false;
									continue;
								}

								if (aa.equals("{")) {
									buildAnInt = true;
									continue;
								}
								if (aa.equals("}")) {
									buildAnInt = false;
									try {
										Integer.parseInt(sb.toString());
									} catch (Exception e) {
										Powder.getInstance().getLogger().log(Level.WARNING, "INVALID NUMBER AAA");
										sb.setLength(0);
										continue;
									}
								}

								if (buildAnInt == true) {
									lastCharInt = true;
									sb.append(aa);
									continue;
								} else {
									lastCharInt = false;
								}

								if ((buildAnInt == false) && !(sb.length() == 0)) {

									downSoFar = 0;
									result = Integer.parseInt(sb.toString());

									ssy = sy + (rgy * result);

									ssx = sx + (rfx * result) + (rgz * result);
									ssz = sz + (rfz * result) + (rgx * result);

									sb.setLength(0);

									continue;

								}

								if (aa.equals(";")) {

									if (lastCharInt == true) {
										ssy = sy;
									} else {
										downSoFar++;
										ssx = sx + (rfx * result) - (gx * downSoFar);
										ssz = sz + (rfz * result) - (gz * downSoFar);
										ssy = ssy - ydist;
									}
									continue;

								}

								ssx = ssx + fx;
								ssz = ssz + fz;
								lastCharInt = false;

								String pname = null;
								boolean success = false;
								for (ChangedParticle changedParticle : powderMap.getChangedParticles()) {
									if (changedParticle.getEnumName().equals(aa)) {
										Particle particle = changedParticle.getParticle();
										if (changedParticle.getData() == null) {
											player.getWorld().spawnParticle(particle, ssx, ssy, ssz, 0, (changedParticle.getXOff() / 255), 
													changedParticle.getYOff() / 255, changedParticle.getZOff() / 255, 1);
										} else {
											player.getWorld().spawnParticle(particle, ssx, ssy, ssz, 1, (changedParticle.getXOff() / 255), 
													changedParticle.getYOff() / 255, changedParticle.getZOff() / 255, 
													(double) changedParticle.getData());
										}
										success = true;
										break;
									}
								}
								if (success) continue;
								try {
									pname = ParticleName.valueOf(aa).getName();
								} catch (Exception e) {
									continue;
								}

								player.getWorld().spawnParticle(Particle.valueOf(pname), ssx, ssy, ssz, 1, 0, 0, 0, 0);

							}

						}

					},waitTime);

			tasks.add(task);

		}
		
		return tasks;

	}

	public static List<Integer> createSounds(final Player player, final PowderMap map, PowderHandler powderHandler) {

		List<Integer> tasks = new ArrayList<Integer>();
		
		for (SoundEffect sound : map.getSounds()) {

			int task = Powder.getInstance().getServer().getScheduler()
					.scheduleSyncDelayedTask(Powder.getInstance(), new Runnable() {

						@Override
						public void run() {

							player.getWorld().playSound(player.getLocation(), sound.getSound(),
									sound.getVolume(), sound.getPitch());

						}

					},((long) sound.getWaitTime()));

			tasks.add(task);

		}
		
		return tasks;

	}
	
	public static List<Integer> createDusts(final Player player, final PowderMap map, PowderHandler powderHandler) {
		
		List<Integer> tasks = new ArrayList<Integer>();
		
		for (Dust dust : map.getDusts()) {
			
			// frequency is particles per min
			// translate to ticks
			long frequency = 1200 / dust.getFrequency();
			
			int task = Powder.getInstance().getServer().getScheduler()
					.scheduleSyncRepeatingTask(Powder.getInstance(), new Runnable() {
						
				public void run() {
					double radiusZoneX = (Math.random() - .5) * (2 * dust.getRadius());
					double radiusZoneZ = (Math.random() - .5) * (2 * dust.getRadius());
					double heightZone = (Math.random() - .5) * (2 * dust.getHeight());
					Location particleLocation = player.getLocation().add(radiusZoneX, heightZone + 1, radiusZoneZ);
					if (particleLocation.getBlock().isEmpty()) {
						boolean success = false;
						for (ChangedParticle changedParticle : map.getChangedParticles()) {
							if (changedParticle.getEnumName().equals(dust.getParticle())) {
								Particle particle = changedParticle.getParticle();
								if (changedParticle.getData() == null) {
									player.getWorld().spawnParticle(particle, particleLocation, 0, (changedParticle.getXOff() / 255), 
											changedParticle.getYOff() / 255, changedParticle.getZOff() / 255, 1);
								} else {
									player.getWorld().spawnParticle(particle, particleLocation, 1, (changedParticle.getXOff() / 255), 
											changedParticle.getYOff() / 255, changedParticle.getZOff() / 255, 
											(double) changedParticle.getData());
								}
								success = true;
								break;
							}
						}
						Particle particle;
						if (success) return;
						try {
							particle = Particle.valueOf(dust.getParticle());
						} catch (Exception e) {
							try {
								particle = Particle.valueOf(ParticleName.valueOf(dust.getParticle()).getName());
							} catch (Exception ee) {
								return;
							}
						}
						player.getWorld().spawnParticle(particle, particleLocation, 0, 0, 0, 0, 1);
					}
				}
				
			}, frequency, frequency);
			
			tasks.add(task);
			
		}
		
		return tasks;
		
	}

	public static double getDirLengthX(double rot, double spacing) {

		return (spacing * Math.cos(rot));

	}

	public static double getDirLengthZ(double rot, double spacing) {

		return (spacing * Math.sin(rot));

	}

}
