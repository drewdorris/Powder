package com.ruinscraft.powder.storage;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.ruinscraft.powder.PowderPlugin;
import com.ruinscraft.powder.util.PowderUtil;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class JSONStorage implements Storage {

	private static final Gson GSON = new Gson();
	private static final JsonParser PARSER = new JsonParser();

	private File jsonFile;

	public JSONStorage(File jsonFile) {
		this.jsonFile = jsonFile;

		/* Try to create the JSON file if it doesn't exist */
		try {
			if (this.jsonFile.createNewFile()) {
				try (FileWriter writer = new FileWriter(jsonFile)) {
					GSON.toJson(new Object(), writer);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
			PowderPlugin.warning("Failed to create JSON file");
			return;
		}

		/* Ensure GSON is supported on the platform */
		try {
			Class.forName("com.google.gson.Gson");
		} catch (ClassNotFoundException e) {
			PowderPlugin.warning("JSON (Gson) unsupported on this server platform");
			return;
		}
	}

	@Override
	public List<String> get(UUID uuid) {
		JsonObject root = getRoot();

		if (!root.has(uuid.toString())) {
			return new ArrayList<>();
		}

		Node node = GSON.fromJson(getRoot().get(uuid.toString()), Node.class);

		return Arrays.asList(node.powders);
	}

	@Override
	public void save(UUID uuid) {
		JsonObject root = getRoot();

		List<String> powders = PowderUtil.getEnabledPowderNames(uuid);

		Node node = new Node(powders.stream().toArray(String[]::new));

		root.add(uuid.toString(), GSON.toJsonTree(node));

		try (FileWriter writer = new FileWriter(jsonFile)) {
			GSON.toJson(root, writer);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public Map<UUID, List<String>> getBatch(Collection<UUID> uuids) {
		Map<UUID, List<String>> batch = new HashMap<>();

		if (uuids.isEmpty()) {
			return batch;
		}

		JsonObject root = getRoot();

		for (UUID uuid : uuids) {
			if (!root.has(uuid.toString())) {
				continue;
			}

			Node node = GSON.fromJson(getRoot().get(uuid.toString()), Node.class);

			batch.put(uuid, Arrays.asList(node.powders));
		}

		return batch;
	}

	@Override
	public void saveBatch(Collection<UUID> uuids) {
		JsonObject root = getRoot();

		for (UUID uuid : uuids) {
			List<String> powders = PowderUtil.getEnabledPowderNames(uuid);

			Node node = new Node(powders.stream().toArray(String[]::new));

			root.add(uuid.toString(), GSON.toJsonTree(node));
		}

		try (FileWriter writer = new FileWriter(jsonFile)) {
			GSON.toJson(root, writer);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void close() {
	}

	private JsonObject getRoot() {
		try (FileReader fileReader = new FileReader(jsonFile)) {
			return PARSER.parse(fileReader).getAsJsonObject();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	private final class Node {
		public final String[] powders;

		public Node(String[] powders) {
			this.powders = powders;
		}
	}

}
