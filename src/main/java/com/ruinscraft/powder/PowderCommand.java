package com.ruinscraft.particle;

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

import com.ruinscraft.particle.objects.ParticleMap;
import com.ruinscraft.particle.objects.ParticleName;
import com.ruinscraft.particle.objects.ParticleTask;
import com.ruinscraft.particle.objects.SoundEffect;

public class PrtCommand implements CommandExecutor {

	BukkitScheduler scheduler = RCParticle.getInstance().getServer().getScheduler();

	private List<Player> recentCommandSenders = new ArrayList<Player>();

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String bleh, String[] args) {

		if (!(sender instanceof Player)) {
			return false;
		}

		Player player = (Player) sender;

		ParticleHandler phandler = RCParticle.getInstance().getParticleHandler();

		if (args.length < 1) {
			particleList(player, phandler);
			return false;
		}

		if (args[0].equals("reload")) {
			RCParticle.getInstance().reloadConfig();
			RCParticle.getInstance().handleStuff();
			player.sendMessage(RCParticle.PREFIX + 
					ChatColor.GRAY + "Particle config.yml reloaded!");
			return true;
		}

		ParticleMap map = phandler.getParticleMap(args[0]);

		if (map == null) {
			particleList(player, phandler);
			return false;
		}

		if (!(player.hasPermission("rcp.effect." + map.getName()))) {
			player.sendMessage(RCParticle.PREFIX + 
					ChatColor.RED + "You don't have permission for this particle.");
			return false;
		}

		if (args.length > 1) {
			if (args[1].equalsIgnoreCase("cancel")) {
				player.sendMessage(RCParticle.PREFIX + 
						ChatColor.GRAY + "Particles '" + map.getName() + "' cancelled!");
				for (ParticleTask ptask : phandler.getParticleTasks(player, map)) {
					phandler.removeParticleTask(ptask);
				}
			}
			return false;
		}

		if (phandler.getParticleTasks(player).size() >= 3) {
			player.sendMessage(RCParticle.PREFIX + 
					ChatColor.RED + "You already have 3 particles in use!");
			for (ParticleTask ptask : phandler.getParticleTasks(player)) {
				player.sendMessage(ChatColor.GRAY + "| " + ChatColor.ITALIC + ptask.getMap().getName());
			}
			return false;
		}

		// 5 sec between each command
		if (recentCommandSenders.contains(player)) {
			player.sendMessage(RCParticle.PREFIX + 
					ChatColor.RED + "Please wait 5 seconds between using each particle.");
			return false;
		}
		scheduler.scheduleSyncDelayedTask(RCParticle.getInstance(), new Runnable() {

			public void run() {
				recentCommandSenders.remove(player);
			}

		}, 100L);
		recentCommandSenders.add(player);

		int task;
		List<Integer> tasks = new ArrayList<Integer>();
		
		if (map.isRepeating()) {
			
			task = scheduler.scheduleSyncRepeatingTask(RCParticle.getInstance(), new Runnable() {
				public void run() {
					createEverything(player, map);
				}
			}, 0L, map.getDelay());
			
			tasks.add(task);
			
		} else {
			
			createEverything(player, map);
			
		}

		ParticleTask ptask = new ParticleTask(player, tasks, map);
		phandler.addParticleTask(ptask);

		return true;

	}

	public void particleList(Player player, ParticleHandler phandler) {

		player.sendMessage(ChatColor.RED + "Please send a valid particle name " + 
							ChatColor.GRAY + "(/prt <particle>)" + ChatColor.RED + ":");
		// change this to a textcomponent when appropriate
		for (ParticleMap pmap : phandler.getParticleMaps()) {
			if (!(player.hasPermission("rcp.effect." + pmap.getName()))) {
				continue;
			}
			player.sendMessage(ChatColor.GRAY + "| " + ChatColor.ITALIC + pmap.getName());
		}
		if (phandler.getParticleTasks(player).isEmpty()) {
			return;
		}
		player.sendMessage(ChatColor.RED + "Running particles:");
		for (ParticleTask ptask : phandler.getParticleTasks(player)) {
			player.sendMessage(ChatColor.GRAY + "| " + ChatColor.ITALIC + ptask.getMap().getName());
		}
		if (!phandler.getParticleTasks(player).isEmpty()) {
			player.sendMessage(ChatColor.RED + "Use " + ChatColor.GRAY + "/prt <particle> cancel" +
					ChatColor.RED + " to cancel a particle.");
		}
		return;

	}
	
	public void createEverything(Player player, ParticleMap map) {
		createParticles(player, map);
		for (SoundEffect sound : map.getSounds()) {
			createSound(player, sound);
		}
		if (map.isRepeating() || map.getStringMaps().size() > 1) {
			Random random = new Random();
			if (random.nextInt(5) == 1) {
				player.sendMessage(RCParticle.PREFIX + ChatColor.GRAY + 
						"Tip: Use '/prt <particle> cancel' to cancel a particle.");
			}
		}
	}

	public void createParticles(final Player player, final ParticleMap map) {

		ParticleHandler phandler = RCParticle.getInstance().getParticleHandler();
		
		final float spacing = map.getSpacing();

		for (SoundEffect sound : map.getSounds()) {
			createSound(player, sound);
		}

		List<String> smaps = map.getStringMaps();

		for (String smap : smaps) {

			long waitTime = 0;

			try {
				waitTime = Long.parseLong(smap.substring(0, smap.indexOf(";")));
			} catch (Exception e) { }

			int task = scheduler.scheduleSyncDelayedTask(RCParticle.getInstance(), new Runnable() {

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
								RCParticle.getInstance().getLogger().log(Level.WARNING, "INVALID NUMBER AAA");
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
						for (ParticleTask task : phandler.getParticleTasks(player, map)) {
							phandler.removeParticleTask(task);
						}
					}

				}

			},waitTime);
			
			boolean trigger = false;
			for (ParticleTask ptask : phandler.getParticleTasks(player)) {
				if (map.equals(ptask.getMap())) {
					ptask.addTask(task);
					trigger = true;
				}
			}
			if (trigger == false) {
				List<Integer> tasks = new ArrayList<Integer>();
				tasks.add(task);
				ParticleTask ptask = new ParticleTask(player, tasks, map);
				phandler.addParticleTask(ptask);
			}

		}

	}

	public void createSound(Player player, SoundEffect sound) {

		scheduler.scheduleSyncDelayedTask(RCParticle.getInstance(), new Runnable() {

			@Override
			public void run() {

				player.getWorld().playSound(player.getLocation(), sound.getSound(),
						sound.getVolume(), sound.getPitch());

			}

		},((long) sound.getWaitTime()));

	}

	public double getDirLengthX(double rot, double spacing) {

		return (spacing * Math.cos(rot));

	}

	public double getDirLengthZ(double rot, double spacing) {

		return (spacing * Math.sin(rot));

	}

}
