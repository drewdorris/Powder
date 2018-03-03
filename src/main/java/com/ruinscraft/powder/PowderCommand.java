package com.ruinscraft.powder;

import java.util.ArrayList;
import java.util.List;
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

		PowderHandler powderHandler = Powder.getInstance().getPowderHandler();

		if (args.length < 1) {
			powderList(player, powderHandler, label);
			return false;
		}

		if (args[0].equals("reload")) {
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

		if (!(player.hasPermission("rcp.effect." + map.getName()))) {
			player.sendMessage(Powder.PREFIX + 
					ChatColor.RED + "You don't have permission for this Powder.");
			return false;
		}

		if (args.length > 1) {
			if (args[1].equalsIgnoreCase("cancel")) {
				player.sendMessage(Powder.PREFIX + 
						ChatColor.GRAY + "Powders '" + map.getName() + "' cancelled!");
				for (PowderTask powderTask : powderHandler.getPowderTasks(player, map)) {
					powderHandler.removePowderTask(powderTask);
				}
			}
			return false;
		}

		if (powderHandler.getPowderTasks(player).size() >= 3) {
			player.sendMessage(Powder.PREFIX + 
					ChatColor.RED + "You already have 3 Powders in use!");
			for (PowderTask powderTask : powderHandler.getPowderTasks(player)) {
				player.sendMessage(ChatColor.GRAY + "| " + ChatColor.ITALIC + powderTask.getMap().getName());
			}
			return false;
		}

		// 5 sec between each command
		if (recentCommandSenders.contains(player)) {
			player.sendMessage(Powder.PREFIX + 
					ChatColor.RED + "Please wait 5 seconds between using each Powder.");
			return false;
		}
		scheduler.scheduleSyncDelayedTask(Powder.getInstance(), new Runnable() {

			public void run() {
				recentCommandSenders.remove(player);
			}

		}, 100L);
		recentCommandSenders.add(player);

		int task;
		List<Integer> tasks = new ArrayList<Integer>();
		
		if (map.isRepeating()) {
			
			task = scheduler.scheduleSyncRepeatingTask(Powder.getInstance(), new Runnable() {
				public void run() {
					createEverything(player, map, powderHandler, label);
				}
			}, 0L, map.getDelay());
			
			tasks.add(task);
			
		} else {
			
			createEverything(player, map, powderHandler, label);
			
		}

		PowderTask powderTask = new PowderTask(player, tasks, map);
		powderHandler.addPowderTask(powderTask);

		return true;

	}

	public void powderList(Player player, PowderHandler phandler, String label) {

		player.sendMessage(ChatColor.RED + "Please send a valid Powder name " + 
							ChatColor.GRAY + "(/" + label +  " <powder>)" + ChatColor.RED + ":");
		// change this to a textcomponent when appropriate
		StringBuilder powderMaps = new StringBuilder();
		powderMaps.append(ChatColor.BLACK + "| ");
		for (PowderMap pmap : phandler.getPowderMaps()) {
			if (!(player.hasPermission("rcp.effect." + pmap.getName()))) {
				powderMaps.append(ChatColor.DARK_GRAY + pmap.getName() + ChatColor.BLACK + " | ");
				continue;
			}
			powderMaps.append(ChatColor.GRAY + pmap.getName() + ChatColor.BLACK + " | ");
		}
		player.sendMessage(powderMaps.toString());
		if (phandler.getPowderTasks(player).isEmpty()) {
			return;
		}
		player.sendMessage(ChatColor.RED + "Running Powders:");
		for (PowderTask ptask : phandler.getPowderTasks(player)) {
			player.sendMessage(ChatColor.GRAY + "| " + ChatColor.ITALIC + ptask.getMap().getName());
		}
		if (!phandler.getPowderTasks(player).isEmpty()) {
			player.sendMessage(ChatColor.RED + "Use " + ChatColor.GRAY + "'/" + label + " <powder> cancel'" +
					ChatColor.RED + " to cancel a Powder.");
		}

	}
	
	public static void createEverything(Player player, PowderMap map, PowderHandler powderHandler, String label) {
		
		createParticles(player, map, powderHandler);
		for (SoundEffect sound : map.getSounds()) {
			createSound(player, map, sound, powderHandler);
		}
		if (map.isRepeating() || map.getStringMaps().size() > 1) {
			Random random = new Random();
			if (random.nextInt(8) == 1) {
				player.sendMessage(Powder.PREFIX + ChatColor.GRAY + 
						"Tip: Use '/" + label + " <powder> cancel' to cancel a Powder.");
			}
		}
	}

	public static void createParticles(final Player player, final PowderMap map, PowderHandler powderHandler) {
		
		final float spacing = map.getSpacing();

		List<String> smaps = map.getStringMaps();

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

					final double sx = location.getX() - (fx * map.getPlayerLeft()) + (gx * map.getPlayerUp());
					final double sy = location.getY() + (ydist * map.getPlayerUp());
					final double sz = location.getZ() - (fz * map.getPlayerLeft()) + (gz * map.getPlayerUp());

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

					if (!(map.isRepeating()) && (smaps.get(smaps.size() - 1) == smap)) {
						for (PowderTask task : powderHandler.getPowderTasks(player, map)) {
							powderHandler.removePowderTask(task);
						}
					}

				}

			},waitTime);
			
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

	}

	public static void createSound(Player player, PowderMap map, SoundEffect sound, PowderHandler phandler) {

		int task = Powder.getInstance().getServer()
			.getScheduler().scheduleSyncDelayedTask(Powder.getInstance(), new Runnable() {

			@Override
			public void run() {

				player.getWorld().playSound(player.getLocation(), sound.getSound(),
						sound.getVolume(), sound.getPitch());

			}

		},((long) sound.getWaitTime()));
		
		boolean trigger = false;
		for (PowderTask ptask : phandler.getPowderTasks(player)) {
			if (map.equals(ptask.getMap())) {
				ptask.addTask(task);
				trigger = true;
			}
		}
		if (trigger == false) {
			List<Integer> tasks = new ArrayList<Integer>();
			tasks.add(task);
			PowderTask ptask = new PowderTask(player, tasks, map);
			phandler.addPowderTask(ptask);
		}

	}

	public static double getDirLengthX(double rot, double spacing) {

		return (spacing * Math.cos(rot));

	}

	public static double getDirLengthZ(double rot, double spacing) {

		return (spacing * Math.sin(rot));

	}

}
