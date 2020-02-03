package com.ruinscraft.powder.command.subcommand;

import java.util.ArrayList;
import java.util.List;

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

public class CreatedCommand implements SubCommand {

	private String[] labels = {"created"};

	@Override
	public String[] getLabels() {
		return labels;
	}

	@Override
	public void command(Player player, String label, String[] args) {
		PowderHandler powderHandler = PowderPlugin.get().getPowderHandler();

		int page = 1;
		try {
			page = Integer.valueOf(args[1]);
		} catch (Exception e) { }

		PowderUtil.sendPrefixMessage(player, Message.CREATED_PREFIX, label);

		List<PowderTask> created =
				powderHandler.getCreatedPowderTasks(player);
		List<BaseComponent> createdText = new ArrayList<>();

		for (PowderTask powderTask : created) {
			boolean hasPermissionForPowder = false;
			String location = null;
			if (powderTask.getTracker().hasControl(player)) {
				hasPermissionForPowder = true;
				location = powderTask.getTracker().getFormattedLocation();
			}

			BaseComponent baseComponent = PowderUtil.setText(Message.CREATED, powderTask.getName(), location);

			if (player.hasPermission("powder.removeany") || 
					(player.hasPermission("powder.remove") && hasPermissionForPowder)) {
				baseComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
						new ComponentBuilder(PowderUtil.setString(
								Message.CREATED_HOVER, powderTask.getName(),
								powderTask.getPowder().getName()))
						.create()));
				baseComponent.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
						PowderUtil.setString(Message.CREATED_CLICK,
								label, powderTask.getName())));
			}
			createdText.add(baseComponent);
		}

		PowderUtil.paginateAndSend(player, createdText, " created ", page, 7, label);
	}

}
