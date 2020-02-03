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

public class ActiveCommand implements SubCommand {

	private String[] labels = {"active"};

	@Override
	public String[] getLabels() {
		return labels;
	}

	@Override
	public void command(Player player, String label, String[] args) {
		PowderHandler powderHandler = PowderPlugin.get().getPowderHandler();

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
			String powderName = powderTask.getPowder().getName();
			BaseComponent baseComponent = PowderUtil.setTextHoverAndClick(Message.ACTIVE_POWDER,
					Message.ACTIVE_POWDER_HOVER, Message.ACTIVE_POWDER_CLICK,
					player.getName(), label, powderName);
			textComponents.add(baseComponent);
		}
		PowderUtil.paginateAndSend(player, textComponents, " active ", page, 7, label);
	}

}
