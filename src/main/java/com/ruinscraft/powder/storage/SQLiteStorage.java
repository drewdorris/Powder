package com.ruinscraft.powder.storage;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.ruinscraft.powder.PowderPlugin;
import com.ruinscraft.powder.util.PowderUtil;

public class SQLiteStorage implements SQLStorage {

	private static final String POWDERS_TABLE = "powders";

	private File dbFile;
	private Connection connection;

	private static String create_table = "CREATE TABLE IF NOT EXISTS " + 
			POWDERS_TABLE + " (uuid VARCHAR(36), powder VARCHAR(32));";
	private static String query_powders = "SELECT * FROM " + POWDERS_TABLE + " WHERE uuid = ?;";
	private static String insert_powder = "INSERT INTO " + POWDERS_TABLE + " (uuid, powder) VALUES (?, ?);";
	private static String delete_powders = "DELETE FROM " + POWDERS_TABLE + " WHERE uuid = ?";

	public SQLiteStorage(File dbFile) {
		this.dbFile = dbFile;

		/* Try to create the DB file if it doesn't exist */
		try {
			this.dbFile.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
		}

		/* Ensure SQLite is supported on the platform */
		try {
			Class.forName("org.sqlite.JDBC");
		} catch (ClassNotFoundException e) {
			PowderPlugin.getInstance().getLogger().warning("SQLite unsupported on this server platform");
		}

		/* Create the table */
		createTable();
	}

	@Override
	public List<String> get(UUID uuid) {
		List<String> powders = new ArrayList<>();

		try (PreparedStatement ps = getConnection().prepareStatement(query_powders)) {
			ps.setString(1, uuid.toString());

			ResultSet rs = ps.executeQuery();

			while (rs.next()) {
				powders.add(rs.getString("powder"));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return powders;
	}

	@Override
	public void save(UUID uuid, List<String> powders) {
		try (PreparedStatement delete = getConnection().prepareStatement(delete_powders);
				PreparedStatement insert = getConnection().prepareStatement(insert_powder)) {
			delete.setString(1, uuid.toString());
			delete.execute();

			for (String powder : powders) {
				insert.setString(1, uuid.toString());
				insert.setString(2, powder);
				insert.addBatch();
			}
			insert.executeBatch();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public Map<UUID, List<String>> getBatch(Collection<UUID> uuids) {
		Map<UUID, List<String>> batch = new HashMap<>();
		if (uuids.isEmpty()) {
			return batch;
		}

		try (PreparedStatement ps = getConnection().prepareStatement(query_powders)) {
			for (UUID uuid : uuids) {
				List<String> powders = new ArrayList<>();

				ps.setString(1, uuid.toString());

				ResultSet rs = ps.executeQuery();

				while (rs.next()) {
					powders.add(rs.getString("powder"));
				}

				batch.put(uuid, powders);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return batch;
	}

	@Override
	public void saveBatch(Collection<UUID> uuids) {
		if (uuids.isEmpty()) {
			return;
		}
		try (PreparedStatement delete = getConnection().prepareStatement(delete_powders);
				PreparedStatement insert = getConnection().prepareStatement(insert_powder)) {

			for (UUID uuid : uuids) {
				delete.setString(1, uuid.toString());
				delete.execute();

				for (String powder : PowderUtil.getEnabledPowderNames(uuid)) {
					insert.setString(1, uuid.toString());
					insert.setString(2, powder);
					insert.addBatch();
				}
				insert.executeBatch();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void close() {
		if (connection != null) {
			try {
				connection.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void createTable() {
		try (PreparedStatement create = getConnection().prepareStatement(create_table)) {
			create.execute();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public Connection getConnection() throws SQLException {
		if (connection == null || connection.isClosed()) {
			connection = DriverManager.getConnection("jdbc:sqlite:" + dbFile);
		}

		return connection;
	}

}
