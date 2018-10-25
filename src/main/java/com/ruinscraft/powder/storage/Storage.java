package com.ruinscraft.powder.storage;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface Storage {

	List<String> get(UUID uuid);

	void save(UUID uuid);

	Map<UUID, List<String>> getBatch(Collection<UUID> uuids);

	void saveBatch(Collection<UUID> uuids);

	void close();

}
