package com.ruinscraft.powder.command.subcommand;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bukkit.entity.Player;

import com.ruinscraft.powder.PowderHandler;
import com.ruinscraft.powder.PowderPlugin;
import com.ruinscraft.powder.command.SubCommand;
import com.ruinscraft.powder.model.Message;
import com.ruinscraft.powder.model.PowderTask;
import com.ruinscraft.powder.util.PowderUtil;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;

public class NearbyCommand implements SubCommand {

	private String[] labels = {"nearby", "near", "nearme"};

	@Override
	public String[] getLabels() {
		return labels;
	}

	@Override
	public void command(Player player, String label, String[] args) {
		PowderHandler powderHandler = PowderPlugin.get().getPowderHandler();

		// elements contained within a page of a list
		int pageLength = PowderPlugin.get().getConfig().getInt("pageLength");

		if (!(player.hasPermission("powder.nearby"))) {
			PowderUtil.sendPrefixMessage(player,
					Message.GENERAL_NO_PERMISSION, label, player.getName());
			return;
		}
		int range = 200;
		int page = 1;
		try {
			range = Integer.valueOf(args[1]);
			page = Integer.valueOf(args[2]);
		} catch (Exception e) { }

		PowderUtil.sendPrefixMessage(player, Message.NEARBY_PREFIX, label);
		Map<PowderTask, Integer> nearby =
				powderHandler.getNearbyPowderTasks(player.getLocation(), range);
		List<BaseComponent> nearbyText = new ArrayList<>();
		for (PowderTask powderTask : nearby.keySet()) {
			BaseComponent baseComponent = PowderUtil.setText(Message.NEARBY,
					powderTask.getName(), String.valueOf(nearby.get(powderTask)));
			boolean hasPermissionForPowder = false;
			if (powderTask.getTracker().hasControl(player)) hasPermissionForPowder = true;
			if (player.hasPermission("powder.removeany") || 
					(player.hasPermission("powder.remove") && hasPermissionForPowder)) {
				baseComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
						new ComponentBuilder(PowderUtil.setString(
								Message.NEARBY_HOVER, powderTask.getName(),
								powderTask.getPowder().getName()))
						.create()));
				baseComponent.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
						PowderUtil.setString(Message.NEARBY_CLICK,
								label, powderTask.getName())));
			}
			nearbyText.add(baseComponent);
		}
		PowderUtil.paginateAndSend(player, nearbyText, " nearby " + range + " ", page, pageLength, label);
	}

}
