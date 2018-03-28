package com.ruinscraft.powder.storage;

import java.util.List;
import java.util.UUID;

public interface Storage {

	List<String> getEnabledPowders(UUID uuid);
	
	void saveEnabledPowders(UUID uuid, List<String> powders);
	
}
