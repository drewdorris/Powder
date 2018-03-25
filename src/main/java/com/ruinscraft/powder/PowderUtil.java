package com.ruinscraft.powder;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import com.ruinscraft.powder.objects.PowderParticle;
import com.ruinscraft.powder.objects.PowderTask;
import com.ruinscraft.powder.objects.Dust;
import com.ruinscraft.powder.objects.Layer;
import com.ruinscraft.powder.objects.ParticleMatrix;
import com.ruinscraft.powder.objects.Powder;
import com.ruinscraft.powder.objects.SoundEffect;

import net.md_5.bungee.api.ChatColor;

public class PowderUtil {

	public static String color(String msg) {
		return ChatColor.translateAlternateColorCodes('&', msg);
	}

	public static double getDirLengthX(double rot, double spacing) {
		return (spacing * Math.cos(rot));
	}

	public static double getDirLengthZ(double rot, double spacing) {
		return (spacing * Math.sin(rot));
	}

	public static List<Integer> createPowder(final Player player, final Powder powder) {

		int task;
		List<Integer> tasks = new ArrayList<Integer>();

		if (powder.isRepeating()) {

			task = PowderPlugin.getInstance().getServer().getScheduler().scheduleSyncRepeatingTask(PowderPlugin.getInstance(), new Runnable() {
				public void run() {
					tasks.addAll(PowderUtil.createParticles(player, powder));
					tasks.addAll(PowderUtil.createSounds(player, powder));
				}
			}, 0L, powder.getDelay());

			tasks.addAll(PowderUtil.createDusts(player, powder));

			tasks.add(task);

		} else {
			tasks.addAll(PowderUtil.createParticles(player, powder));
			tasks.addAll(PowderUtil.createSounds(player, powder));
			tasks.addAll(PowderUtil.createDusts(player, powder));
		}

		return tasks;

	}

	public static Boolean cancelPowder(final Player player, final Powder powder) {

		PowderHandler powderHandler = PowderPlugin.getInstance().getPowderHandler();
		boolean success = false;
		for (PowderTask powderTask : powderHandler.getPowderTasks(player, powder)) {
			powderHandler.removePowderTask(powderTask);
			success = true;
		}
		return success;

	}

	public static List<Integer> createParticles(final Player player, final Powder powder) {

		List<ParticleMatrix> particleMatrices = powder.getMatrices();

		List<Integer> tasks = new ArrayList<Integer>();

		for (ParticleMatrix particleMatrix : particleMatrices) {

			final float spacing;

			if (particleMatrix.getSpacing() == 0) {
				spacing = powder.getDefaultSpacing();
			} else {
				spacing = particleMatrix.getSpacing();
			}

			long waitTime = particleMatrix.getTick();

			int task = PowderPlugin.getInstance().getServer()
					.getScheduler().scheduleSyncDelayedTask(PowderPlugin.getInstance(), new Runnable() {

						@Override
						public void run() {

							if (!player.isOnline()) {
								return;
							}

							Location location = Bukkit.getPlayer(player.getName()).getEyeLocation();

							final double playerRotation = ((player.getLocation().getYaw()) % 360) * (Math.PI / 180);
							final double playerPitch;

							final double distanceBetweenRowsY;
							final double distanceBetweenRowsXZ;

							if (powder.hasPitch()) {
								playerPitch = (player.getLocation().getPitch() * (Math.PI / 180));
								distanceBetweenRowsY = ((Math.sin((Math.PI / 2) - playerPitch)) * spacing);
								distanceBetweenRowsXZ = ((Math.cos((Math.PI / 2) + playerPitch)) * spacing);
							} else {
								playerPitch = 0;
								distanceBetweenRowsY = spacing;
								distanceBetweenRowsXZ = 0;
							}

							int left;
							int up;
							if (particleMatrix.getPlayerLeft() == 0 && particleMatrix.getPlayerUp() == 0) {
								left = powder.getDefaultLeft();
								up = powder.getDefaultUp();
							} else {
								left = particleMatrix.getPlayerLeft();
								up = particleMatrix.getPlayerUp();
							}

							// more doubles
							final double amountToAddX = getDirLengthX(playerRotation, spacing);
							final double amountToAddZ = getDirLengthZ(playerRotation, spacing);
							final double startARowX = getDirLengthX(playerRotation + (Math.PI / 2), spacing);
							final double startARowZ = getDirLengthZ(playerRotation + (Math.PI / 2), spacing);
							final double moveWithPitchX = getDirLengthX(playerRotation - (Math.PI / 2), distanceBetweenRowsXZ);
							final double moveWithPitchZ = getDirLengthZ(playerRotation - (Math.PI / 2), distanceBetweenRowsXZ);
							final double moveBackWithPitchX = getDirLengthX(playerRotation, distanceBetweenRowsXZ);
							final double moveBackWithPitchY = (Math.sin(0 - playerPitch) * spacing);
							final double moveBackWithPitchZ = getDirLengthZ(playerRotation, distanceBetweenRowsXZ);

							final double startX = location.getX() - 
									(amountToAddX * left) + (moveWithPitchX * up);
							final double startY = location.getY() + (distanceBetweenRowsY * up);
							final double startZ = location.getZ() - 
									(amountToAddZ * left) + (moveWithPitchZ * up);

							double newX = startX;
							double newY = startY;
							double newZ = startZ;

							for (Layer layer : particleMatrix.getLayers()) {

								float position = layer.getPosition();
								newX = startX + (startARowX * position) + (moveBackWithPitchZ * position);
								newY = startY + (moveBackWithPitchY * position);
								newZ = startZ + (startARowZ * position) + (moveBackWithPitchX * position);

								int rowsDownSoFar = 0;
								for (List<PowderParticle> row : layer.getRows()) {

									for (PowderParticle powderParticle : row) {

										newX = newX + amountToAddX;
										newZ = newZ + amountToAddZ;

										if (powderParticle.getParticle() == null) {
											continue;
										}

										if (powderParticle.getData() == null) {
											if (powderParticle.getXOff() == null) {
												player.getWorld().spawnParticle(powderParticle.getParticle(), newX, newY, newZ, 1, 
														powderParticle.getXOff() / 255, powderParticle.getYOff() / 255, 
														powderParticle.getZOff() / 255, 1);
												continue;
											}
											player.getWorld().spawnParticle(powderParticle.getParticle(), newX, newY, newZ, 0, 
													powderParticle.getXOff() / 255, powderParticle.getYOff() / 255, 
													powderParticle.getZOff() / 255, 1);
										} else {
											player.getWorld().spawnParticle(powderParticle.getParticle(), newX, newY, newZ, 1, 
													powderParticle.getXOff() / 255, powderParticle.getYOff() / 255,
													powderParticle.getZOff() / 255, (double) powderParticle.getData());
										}

									}

									rowsDownSoFar++;
									newX = startX + (startARowX * position) - (moveWithPitchX * rowsDownSoFar);
									newY = newY - distanceBetweenRowsY;
									newZ = startZ + (startARowZ * position) - (moveWithPitchZ * rowsDownSoFar);

								}

							}

						}

					},waitTime);

			tasks.add(task);

		}

		return tasks;

	}

	public static List<Integer> createSounds(final Player player, final Powder powder) {

		List<Integer> tasks = new ArrayList<Integer>();

		for (SoundEffect sound : powder.getSoundEffects()) {

			int task = PowderPlugin.getInstance().getServer().getScheduler()
					.scheduleSyncDelayedTask(PowderPlugin.getInstance(), new Runnable() {

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

	public static List<Integer> createDusts(final Player player, final Powder powder) {

		List<Integer> tasks = new ArrayList<Integer>();

		for (Dust dust : powder.getDusts()) {

			// frequency is particles per min
			// translate to ticks if repeating
			long frequency;

			int task;
			if (dust.isSingleOccurrence()) {
				frequency = dust.getFrequency();
				task = PowderPlugin.getInstance().getServer().getScheduler()
						.scheduleSyncDelayedTask(PowderPlugin.getInstance(), new Runnable() {
							public void run() {
								spawnDust(player, dust);
							}
						}, frequency);
			} else {
				if (dust.getFrequency() == 0) {
					frequency = 0;
				} else {
					frequency = 1200 / dust.getFrequency();
				}
				task = PowderPlugin.getInstance().getServer().getScheduler()
						.scheduleSyncRepeatingTask(PowderPlugin.getInstance(), new Runnable() {
							public void run() {
								spawnDust(player, dust);
							}
						}, frequency, frequency);
			}
			tasks.add(task);

		}

		return tasks;

	}

	public static void spawnDust(final Player player, final Dust dust) {

		double radiusZoneX = (Math.random() - .5) * (2 * dust.getRadius());
		double radiusZoneZ = (Math.random() - .5) * (2 * dust.getRadius());
		double heightZone = (Math.random() - .5) * (2 * dust.getHeight());
		Location particleLocation = player.getLocation().add(radiusZoneX, heightZone + 1, radiusZoneZ);
		if (particleLocation.getBlock().isEmpty()) {
			PowderParticle powderParticle = dust.getPowderParticle();
			if (powderParticle.getData() == null) {
				player.getWorld().spawnParticle(powderParticle.getParticle(), particleLocation, 0, (powderParticle.getXOff() / 255), 
						powderParticle.getYOff() / 255, powderParticle.getZOff() / 255, 1);
			} else {
				player.getWorld().spawnParticle(powderParticle.getParticle(), particleLocation, 1, (powderParticle.getXOff() / 255), 
						powderParticle.getYOff() / 255, powderParticle.getZOff() / 255, 
						(double) powderParticle.getData());
			}
		}

	}

}
