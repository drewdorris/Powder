package com.ruinscraft.powder.command;

import org.bukkit.entity.Player;

public interface SubCommand {

	public String[] getLabels();

	public void command(Player player, String label, String[] args);

}
