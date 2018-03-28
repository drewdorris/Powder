package com.ruinscraft.powder.tasks;

import java.util.List;
import java.util.UUID;

import com.ruinscraft.powder.PowderPlugin;

public class SavePlayerToDatabaseTask implements Runnable {

	private final UUID uuid;
	private final List<String> enabledPowders;
	
	public SavePlayerToDatabaseTask(final UUID uuid, final List<String> enabledPowders) {
		this.uuid = uuid;
		this.enabledPowders = enabledPowders;
	}
	
	@Override
	public void run() {
		PowderPlugin.getInstance().getStorage().saveEnabledPowders(uuid, enabledPowders);
	}

}
