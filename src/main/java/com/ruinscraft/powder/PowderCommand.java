package com.ruinscraft.powder;

import java.util.ArrayList;
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
import org.bukkit.scheduler.BukkitScheduler;

import com.ruinscraft.powder.objects.ParticleName;
import com.ruinscraft.powder.objects.PowderMap;
import com.ruinscraft.powder.objects.PowderTask;
import com.ruinscraft.powder.objects.SoundEffect;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

public class PowderCommand implements CommandExecutor {

	BukkitScheduler scheduler = Powder.getInstance().getServer().getScheduler();

	private List<Player> recentCommandSenders = new ArrayList<Player>();

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

		if (!(sender instanceof Player)) {
			sender.sendMessage(Powder.getInstance().getName() + " | " + Powder.getInstance().getDescription());
			return true;
		}

		Player player = (Player) sender;
		
		if (!(player.hasPermission("powder.command"))) {
			player.sendMessage(Powder.PREFIX + 
					ChatColor.RED + "You don't have permission to use /" + label + ".");
		}

		PowderHandler powderHandler = Powder.getInstance().getPowderHandler();

		if (args.length < 1) {
			powderList(player, powderHandler, label);
			return false;
		}

		if (args[0].equals("reload")) {
			if (!(player.hasPermission("powder.reload"))) {
				player.sendMessage(Powder.PREFIX + 
						ChatColor.RED + "You don't have permission to do this.");
				return false;
			}
			Powder.getInstance().reloadConfig();
			Powder.getInstance().handleConfig();
			player.sendMessage(Powder.PREFIX + 
					ChatColor.GRAY + "Powder config.yml reloaded!");
			return true;
		}

		PowderMap map = powderHandler.getPowderMap(args[0]);

		if (map == null) {
			powderList(player, powderHandler, label);
			return false;
		}

		if (!(player.hasPermission("powder.powder." + map.getName()))) {
			player.sendMessage(Powder.PREFIX + 
					ChatColor.RED + "You don't have permission to use the Powder '" + map.getName() + "'.");
			return false;
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
					player.sendMessage(Powder.PREFIX + 
							ChatColor.GRAY + "Powder '" + map.getName() + "' cancelled! (" + 
							taskAmount + " total)");
				} else {
					player.sendMessage(Powder.PREFIX + 
							ChatColor.GRAY + "There are no '" + map.getName() + "' Powders currently active.");
				}
			}
			return false;
		}

		int maxSize = Powder.getInstance().getConfig().getInt("maxPowdersAtOneTime");
		if ((powderHandler.getPowderTasks(player).size() >= maxSize)) {
			player.sendMessage(Powder.PREFIX + 
					ChatColor.RED + "You already have " + maxSize + " Powders active!");
			for (PowderTask powderTask : powderHandler.getPowderTasks(player)) {
				player.sendMessage(ChatColor.GRAY + "| " + ChatColor.ITALIC + powderTask.getMap().getName());
			}
			return false;
		}

		// wait time between each command
		int waitTime = Powder.getInstance().getConfig().getInt("secondsBetweenPowderUsage");
		if (recentCommandSenders.contains(player)) {
			player.sendMessage(Powder.PREFIX + 
					ChatColor.RED + "Please wait " + waitTime + " seconds between using each Powder.");
			return false;
		}
		scheduler.scheduleSyncDelayedTask(Powder.getInstance(), new Runnable() {
			public void run() {
				recentCommandSenders.remove(player);
			}
		}, (waitTime * 20));
		recentCommandSenders.add(player);

		int task;
		List<Integer> tasks = new ArrayList<Integer>();

		if (map.isRepeating()) {

			task = scheduler.scheduleSyncRepeatingTask(Powder.getInstance(), new Runnable() {
				public void run() {
					tasks.addAll(createEverything(player, map, powderHandler));
				}
			}, 0L, map.getDelay());

			tasks.add(task);

		} else {
			tasks.addAll(createEverything(player, map, powderHandler));
		}
		
		if (map.isRepeating() || map.getStringMaps().size() > 1) {
			if (new Random().nextInt(6) == 1) {
				player.sendMessage(Powder.PREFIX + ChatColor.GRAY + 
						"Tip: Use '/" + label + " <powder> cancel' to cancel a Powder.");
			}
		}

		PowderTask powderTask = new PowderTask(player, tasks, map);
		powderHandler.addPowderTask(powderTask);

		return true;

	}

	public static void powderList(Player player, PowderHandler phandler, String label) {

		player.sendMessage(ChatColor.RED + "Please send a valid Powder name " + 
				ChatColor.GRAY + "(/" + label +  " <powder>)" + ChatColor.RED + ":");
		// change this to a textcomponent when appropriate
		TextComponent powderMaps = new TextComponent("    ");
		powderMaps.setColor(net.md_5.bungee.api.ChatColor.GRAY);
		for (PowderMap powderMap : phandler.getPowderMaps()) {
			TextComponent powderMapText = new TextComponent(powderMap.getName());
			if (!(player.hasPermission("rcp.powder." + powderMap.getName().toLowerCase(Locale.US)))) {
				powderMapText.setColor(net.md_5.bungee.api.ChatColor.DARK_GRAY);
				powderMapText.setHoverEvent( new HoverEvent( HoverEvent.Action.SHOW_TEXT, 
						new ComponentBuilder("You don't have permission to use '" + powderMap.getName() + "'.")
						.color(net.md_5.bungee.api.ChatColor.RED).create() ) );
			} else if (!(phandler.getPowderTasks(player, powderMap).isEmpty())) {
				powderMapText.setColor(net.md_5.bungee.api.ChatColor.GREEN);
				powderMapText.setHoverEvent( new HoverEvent( HoverEvent.Action.SHOW_TEXT, 
						new ComponentBuilder("'" + powderMap.getName() + "' is currently active. Click to cancel")
						.color(net.md_5.bungee.api.ChatColor.YELLOW).create() ) );
				powderMapText.setClickEvent( new ClickEvent( ClickEvent.Action.RUN_COMMAND, 
						"/" + label + " " + powderMap.getName() + " cancel" ) );
			} else {
				powderMapText.setColor(net.md_5.bungee.api.ChatColor.GRAY);
				powderMapText.setHoverEvent( new HoverEvent( HoverEvent.Action.SHOW_TEXT, 
						new ComponentBuilder("Click to use '" + powderMap.getName() + "'.")
						.color(net.md_5.bungee.api.ChatColor.GRAY).create() ) );
				powderMapText.setClickEvent( new ClickEvent( ClickEvent.Action.RUN_COMMAND, 
						"/" + label + " " + powderMap.getName()) );
			}
			powderMaps.addExtra(powderMapText);
			powderMaps.addExtra("    ");
		}
		player.spigot().sendMessage(powderMaps);
		if (phandler.getPowderTasks(player).isEmpty()) {
			return;
		}
		/*/
		player.sendMessage(ChatColor.RED + "Running Powders:");
		for (PowderTask ptask : phandler.getPowderTasks(player)) {
			player.sendMessage(ChatColor.GRAY + "| " + ChatColor.ITALIC + ptask.getMap().getName());
		}
		if (!phandler.getPowderTasks(player).isEmpty()) {
			player.sendMessage(ChatColor.RED + "Use " + ChatColor.GRAY + "'/" + label + " <powder> cancel'" +
					ChatColor.RED + " to cancel a Powder.");
		}
		*/

	}

	public static List<Integer> createEverything(final Player player, final PowderMap map, PowderHandler powderHandler) {

		List<Integer> tasks = new ArrayList<>();
		tasks.addAll(createParticles(player, map, powderHandler));
		tasks.addAll(createSounds(player, map, powderHandler));
		tasks.addAll(createDusts(player, map, powderHandler));
		return tasks;
		
	}

	public static List<Integer> createParticles(final Player player, final PowderMap map, PowderHandler powderHandler) {

		final float spacing = map.getSpacing();

		List<String> smaps = map.getStringMaps();
		
		List<Integer> tasks = new ArrayList<Integer>();

		for (String smap : smaps) {

			long waitTime = 0;

			try {
				waitTime = Long.parseLong(smap.substring(0, smap.indexOf(";")));
			} catch (Exception e) { }

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

							if (map.getPitch()) {
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
									(fx * map.getPlayerLeft()) + (gx * map.getPlayerUp());
							final double sy = location.getY() + (ydist * map.getPlayerUp());
							final double sz = location.getZ() - 
									(fz * map.getPlayerLeft()) + (gz * map.getPlayerUp());

							double ssx = sx;
							double ssy = sy;
							double ssz = sz;

							StringBuilder sb = new StringBuilder();
							double result = 0;
							boolean buildAnInt = false;
							boolean lastCharInt = false;
							int downSoFar = 0;

							for (char a : smap.toCharArray()) {

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
								try {
									pname = ParticleName.valueOf(aa).getName();
								} catch (Exception e) {
									continue;
								}

								player.getWorld().spawnParticle(Particle.valueOf(pname), ssx, ssy, ssz, 3, null);

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
		
		// stuff here
		
		return tasks;
		
	}

	public static double getDirLengthX(double rot, double spacing) {

		return (spacing * Math.cos(rot));

	}

	public static double getDirLengthZ(double rot, double spacing) {

		return (spacing * Math.sin(rot));

	}
	
	/*/
	public static void addToTask(Player player, PowderMap map, PowderHandler powderHandler, Integer task) {
		boolean trigger = false;
		for (PowderTask ptask : powderHandler.getPowderTasks(player)) {
			if (map.equals(ptask.getMap())) {
				ptask.addTask(task);
				trigger = true;
			}
		}
		if (trigger == false) {
			List<Integer> tasks = new ArrayList<Integer>();
			tasks.add(task);
			PowderTask ptask = new PowderTask(player, tasks, map);
			powderHandler.addPowderTask(ptask);
		}
	}
	*/

}
