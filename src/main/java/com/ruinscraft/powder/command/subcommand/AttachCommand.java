package com.ruinscraft.powder.command.subcommand;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import com.ruinscraft.powder.PowderHandler;
import com.ruinscraft.powder.PowderPlugin;
import com.ruinscraft.powder.command.SubCommand;
import com.ruinscraft.powder.integration.PlotSquaredHandler;
import com.ruinscraft.powder.integration.TownyHandler;
import com.ruinscraft.powder.model.Message;
import com.ruinscraft.powder.model.Powder;
import com.ruinscraft.powder.util.PowderUtil;

public class AttachCommand implements SubCommand {

	private List<Player> recentCommandSenders = new ArrayList<>();

	private String[] labels = {"attach"};

	@Override
	public String[] getLabels() {
		return labels;
	}

	@Override
	public void command(Player player, String label, String[] args) {
		PowderHandler powderHandler = PowderPlugin.get().getPowderHandler();

		if (!(player.hasPermission("powder.attach"))) {
			PowderUtil.sendPrefixMessage(player,
					Message.GENERAL_NO_PERMISSION, label, player.getName());
			return;
		}

		// do this before the loading of the powder and such
		if (!player.hasPermission("powder.attachany")) {
			// wait time between creating each Powder
			int waitTime = PowderPlugin.get().getConfig().getInt("secondsBetweenPowderUsage");
			// if they sent a command in the given wait time, don't do it
			if (recentCommandSenders.contains(player)) {
				PowderUtil.sendPrefixMessage(player, Message.POWDER_WAIT,
						label, player.getName(), args[0], String.valueOf(waitTime));
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
		}

		String powderName;
		Powder newPowder;
		try {
			powderName = args[1];
			newPowder = powderHandler.getPowder(powderName);
		} catch (Exception e) {
			PowderUtil.sendPrefixMessage(player,
					Message.ATTACH_SYNTAX, label, player.getName(), label);
			return;
		}

		boolean loop = false;
		try {
			String loopString = args[2];
			if (loopString.equalsIgnoreCase("loop")) loop = true;
		} catch (Exception e) { }

		if (newPowder == null) {
			PowderUtil.sendPrefixMessage(player,
					Message.ATTACH_UNKNOWN, label, player.getName(), powderName);
			return;
		}

		if (loop) newPowder = newPowder.loop();

		Entity entity = PowderUtil.getNearestEntityInSight(player, 7);
		if (entity == null) {
			PowderUtil.sendPrefixMessage(player,
					Message.ATTACH_NO_ENTITY, label, player.getName());
			return;
		}

		if (!player.hasPermission("powder.attachany")) {
			if (powderHandler.getPowderTasks(entity.getUniqueId()).size() > 0) {
				PowderUtil.sendPrefixMessage(player,
						Message.ATTACH_ONE_POWDER_PER_ENTITY, label, player.getName(), newPowder.getName());
				return;
			}

			if (PowderPlugin.get().hasPlotSquared()) {
				PlotSquaredHandler plotSquared = PowderPlugin.get().getPlotSquaredHandler();
				if (!plotSquared.checkLocation(newPowder, player)) {
					PowderUtil.sendPrefixMessage(player, 
							Message.ATTACH_PLOTSQUARED_NO_PLACE, label, player.getName(), newPowder.getName());
					return;
				}
			}
			if (PowderPlugin.get().hasTowny()) {
				TownyHandler towny = PowderPlugin.get().getTownyHandler();
				if (!towny.checkLocation(newPowder, player)) {
					PowderUtil.sendPrefixMessage(player, 
							Message.ATTACH_TOWNY_NO_PLACE, label, player.getName(), newPowder.getName());
					return;
				}
			}
			if (PowderPlugin.get().getMaxCreatedPowders() < powderHandler.getPowderTasks(player.getUniqueId()).size()) {
				PowderUtil.sendPrefixMessage(player, 
						Message.ATTACH_TOO_MANY_CREATED, label, player.getName(), newPowder.getName());
				return;
			}
		}

		if (entity instanceof Player && player.hasPermission("powder.attachany")) {
			newPowder.spawn((Player) entity);
			PowderUtil.sendPrefixMessage(player, Message.ATTACH_SUCCESS_PLAYER,
					label, player.getName(), powderName, entity.getName());
			return;
		} else if (entity instanceof LivingEntity) {
			LivingEntity livingEntity = (LivingEntity) entity;
			livingEntity.setRemoveWhenFarAway(false);
			newPowder.spawn(livingEntity, player);
		} else {
			newPowder.spawn(entity, player);
		}

		PowderUtil.sendPrefixMessage(player, Message.ATTACH_SUCCESS_ENTITY,
				label, player.getName(), powderName,
				PowderUtil.cleanEntityName(entity));
	}

}
