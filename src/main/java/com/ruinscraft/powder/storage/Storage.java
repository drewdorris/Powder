package com.ruinscraft.powder.storage;

import java.io.Closeable;
import java.util.List;
import java.util.UUID;

public interface Storage extends Closeable {

	List<String> getEnabledPowders(UUID uuid);

	void saveEnabledPowders(UUID uuid, List<String> powders);

	void saveAll();

	void loadAll();

}
