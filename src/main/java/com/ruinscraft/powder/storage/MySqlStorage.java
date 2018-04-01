package com.ruinscraft.powder.storage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.sql.DataSource;

import com.ruinscraft.powder.util.PowderUtil;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

public class MySqlStorage implements SqlStorage {

	private DataSource dataSource;
	private String powdersTable;

	private final String create_table = "CREATE TABLE IF NOT EXISTS " + powdersTable + " (uuid VARCHAR(36), powder VARCHAR(32));";
	private final String query_powders = "SELECT * FROM " + powdersTable + " WHERE uuid = ?;";
	private final String insert_powder = "INSERT INTO " + powdersTable + " (uuid, powder) VALUES (?, ?);";
	private final String delete_powders = "DELETE FROM " + powdersTable + " WHERE uuid = ?";

	public MySqlStorage(String host, int port, String database, String username, String password, String powdersTable) {
		this.powdersTable = powdersTable;

		HikariConfig hikariConfig = new HikariConfig();

		hikariConfig.setJdbcUrl("jdbc:mysql://" + host + ":" + port + "/" + database);
		hikariConfig.setUsername(username);
		hikariConfig.setPassword(password);
		hikariConfig.setPoolName("powder-pool");
		hikariConfig.setMaximumPoolSize(3);
		hikariConfig.setConnectionTimeout(3000);
		hikariConfig.setLeakDetectionThreshold(3000);

		dataSource = new HikariDataSource(hikariConfig);

		createTable();
	}

	@Override
	public void createTable() {
		try (Connection c = getConnection();
				PreparedStatement ps = c.prepareStatement(create_table)) {
			ps.execute();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public List<String> get(UUID uuid) {
		List<String> powders = new ArrayList<>();

		try (Connection c = getConnection();
				PreparedStatement ps = c.prepareStatement(query_powders)) {
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
		try (Connection c = getConnection();
				PreparedStatement delete = c.prepareStatement(delete_powders);
				PreparedStatement insert = c.prepareStatement(insert_powder)) {
			delete.setString(1, uuid.toString());
			delete.execute();

			for (String powder : PowderUtil.getEnabledPowderNames(uuid)) {
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
	public void saveBatch(Collection<UUID> uuids) {
		try (Connection c = getConnection();
				PreparedStatement delete = c.prepareStatement(delete_powders);
				PreparedStatement insert = c.prepareStatement(insert_powder)) {

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
	public Map<UUID, List<String>> getBatch (Collection<UUID> uuids) {
		Map<UUID, List<String>> batch = new HashMap<>();

		try (Connection c = getConnection();
				PreparedStatement ps = c.prepareStatement(query_powders)) {
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
	public Connection getConnection() throws SQLException {
		return dataSource.getConnection();
	}

}
