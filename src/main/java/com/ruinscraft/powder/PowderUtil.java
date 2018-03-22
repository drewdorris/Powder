package com.ruinscraft.powder;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;

import com.ruinscraft.powder.objects.ChangedParticle;
import com.ruinscraft.powder.objects.Dust;
import com.ruinscraft.powder.objects.ParticleMap;
import com.ruinscraft.powder.objects.ParticleName;
import com.ruinscraft.powder.objects.PowderMap;
import com.ruinscraft.powder.objects.SoundEffect;

public class PowderUtil {

	public static double getDirLengthX(double rot, double spacing) {

		return (spacing * Math.cos(rot));

	}

	public static double getDirLengthZ(double rot, double spacing) {

		return (spacing * Math.sin(rot));

	}

	public static List<Integer> createParticles(final Player player, final PowderMap powderMap) {
		
		List<ParticleMap> particleMaps = powderMap.getMaps();

		List<Integer> tasks = new ArrayList<Integer>();

		for (ParticleMap particleMap : particleMaps) {

			final float spacing;
			if (particleMap.getSpacing() == 0) {
				spacing = powderMap.getDefaultSpacing();
			} else {
				spacing = particleMap.getSpacing();
			}

			long waitTime = particleMap.getTick();

			int task = Powder.getInstance().getServer()
					.getScheduler().scheduleSyncDelayedTask(Powder.getInstance(), new Runnable() {

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

							if (powderMap.hasPitch()) {
								playerPitch = (player.getLocation().getPitch() * (Math.PI / 180));
								distanceBetweenRowsY = ((Math.sin((Math.PI / 2) - playerPitch)) * spacing);
								distanceBetweenRowsXZ = ((Math.cos((Math.PI / 2) + playerPitch)) * spacing);
							} else {
								playerPitch = 0;
								distanceBetweenRowsY = spacing;
								distanceBetweenRowsXZ = 0;
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
									(amountToAddX * particleMap.getPlayerLeft()) + (moveWithPitchX * particleMap.getPlayerUp());
							final double startY = location.getY() + (distanceBetweenRowsY * particleMap.getPlayerUp());
							final double startZ = location.getZ() - 
									(amountToAddZ * particleMap.getPlayerLeft()) + (moveWithPitchZ * particleMap.getPlayerUp());

							double newX = startX;
							double newY = startY;
							double newZ = startZ;

							StringBuilder sb = new StringBuilder();

							boolean buildingAnInt = false;
							boolean lastObjectWasAnInt = false;
							double resultingInt = 0;

							int rowsDownSoFar = 0;

							for (Object object : particleMap.getMap()) {

								if (object instanceof String) {

									if (object.equals(".") || object.equals(",")) {
										newX = newX + amountToAddX;
										newZ = newZ + amountToAddZ;
										buildingAnInt = false;
										continue;
									}

									if (object.equals("{")) {
										buildingAnInt = true;
										continue;
									}

									if (object.equals("}")) {
										buildingAnInt = false;
										try {
											Integer.parseInt(sb.toString());
										} catch (Exception e) {
											Powder.getInstance().getLogger().log(Level.WARNING, "INVALID NUMBER AAA");
											sb.setLength(0);
											continue;
										}
									}

									if (buildingAnInt == true) {
										lastObjectWasAnInt = true;
										sb.append(object);
										continue;
									} else {
										lastObjectWasAnInt = false;
									}

									if ((buildingAnInt == false) && !(sb.length() == 0)) {

										rowsDownSoFar = 0;
										resultingInt = Integer.parseInt(sb.toString());

										newX = startX + (startARowX * resultingInt) + (moveBackWithPitchZ * resultingInt);
										newY = startY + (moveBackWithPitchY * resultingInt);
										newZ = startZ + (startARowZ * resultingInt) + (moveBackWithPitchX * resultingInt);

										sb.setLength(0);

										continue;

									}

									if (object.equals(";")) {

										if (lastObjectWasAnInt == true) {
											newY = startY;
										} else {
											rowsDownSoFar++;
											newX = startX + (startARowX * resultingInt) - (moveWithPitchX * rowsDownSoFar);
											newY = newY - distanceBetweenRowsY;
											newZ = startZ + (startARowZ * resultingInt) - (moveWithPitchZ * rowsDownSoFar);
										}
										continue;

									}

								}

								newX = newX + amountToAddX;
								newZ = newZ + amountToAddZ;
								lastObjectWasAnInt = false;

								if (object instanceof ChangedParticle) {
									ChangedParticle changedParticle = (ChangedParticle) object;
									if (changedParticle.getData() == null) {
										player.getWorld().spawnParticle(changedParticle.getParticle(), newX, newY, newZ, 0, 
												changedParticle.getXOff() / 255, changedParticle.getYOff() / 255, 
												changedParticle.getZOff() / 255, 1);
									} else {
										player.getWorld().spawnParticle(changedParticle.getParticle(), newX, newY, newZ, 1, 
												changedParticle.getXOff() / 255, changedParticle.getYOff() / 255,
												changedParticle.getZOff() / 255, (double) changedParticle.getData());
									}
									continue;
								}

								boolean success = false;
								for (ChangedParticle changedParticle : powderMap.getChangedParticles()) {
									if (changedParticle.getEnumName().equals(object)) {
										Particle particle = changedParticle.getParticle();
										if (changedParticle.getData() == null) {
											player.getWorld().spawnParticle(particle, newX, newY, newZ, 0, (changedParticle.getXOff() / 255), 
													changedParticle.getYOff() / 255, changedParticle.getZOff() / 255, 1);
										} else {
											player.getWorld().spawnParticle(particle, newX, newY, newZ, 1, (changedParticle.getXOff() / 255), 
													changedParticle.getYOff() / 255, changedParticle.getZOff() / 255, 
													(double) changedParticle.getData());
										}
										success = true;
										break;
									}
								}
								if (success) continue;

								String particleName;
								try {
									particleName = ParticleName.valueOf((String) object).getName();
								} catch (Exception e) {
									continue;
								}

								player.getWorld().spawnParticle(Particle.valueOf(particleName), newX, newY, newZ, 1, 0, 0, 0, 0);

							}

						}

					},waitTime);

			tasks.add(task);

		}

		return tasks;

	}

	public static List<Integer> createSounds(final Player player, final PowderMap map) {
		
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

}
