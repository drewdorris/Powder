package com.ruinscraft.powder.storage;

import com.ruinscraft.powder.util.PowderUtil;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class MySQLStorage implements SQLStorage {

	private HikariDataSource dataSource;

	private String create_table;
	private String query_powders;
	private String insert_powder;
	private String delete_powders;

	private String insert_start_column;
	private String insert_loop_column;

	public MySQLStorage(String host, int port,
			String database, String username, String password, String powdersTable) {
		create_table = "CREATE TABLE IF NOT EXISTS " +
				powdersTable + " (uuid VARCHAR(36), powder VARCHAR(32));";
		query_powders = "SELECT * FROM " + powdersTable + " WHERE uuid = ?;";
		insert_powder = "INSERT INTO " + powdersTable + " (uuid, powder) VALUES (?, ?);";
		delete_powders = "DELETE FROM " + powdersTable + " WHERE uuid = ?";

		dataSource = new HikariDataSource();

		dataSource.setJdbcUrl("jdbc:mysql://" + host + ":" + port + "/" + database);
		dataSource.setUsername(username);
		dataSource.setPassword(password);
		dataSource.setPoolName("powder-pool");
		dataSource.setMaximumPoolSize(3);
		dataSource.setConnectionTimeout(3000);
		dataSource.setLeakDetectionThreshold(3000);

		createTable();

		// check if start/loop columns are in the table; if not, add them

		insert_start_column = "ALTER TABLE " + powdersTable + " ADD COLUMN start INT";
		insert_loop_column = "ALTER TABLE " + powdersTable + " ADD COLUMN loop BOOL";
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

			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					powders.add(rs.getString("powder"));
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return powders;
	}

	@Override
	public void save(UUID uuid) {
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
		if (uuids.isEmpty()) {
			return;
		}
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
	public Map<UUID, List<String>> getBatch(Collection<UUID> uuids) {
		Map<UUID, List<String>> batch = new HashMap<>();
		if (uuids.isEmpty()) {
			return batch;
		}

		try (Connection c = getConnection();
				PreparedStatement ps = c.prepareStatement(query_powders)) {
			for (UUID uuid : uuids) {
				List<String> powders = new ArrayList<>();

				ps.setString(1, uuid.toString());

				try (ResultSet rs = ps.executeQuery()) {
					while (rs.next()) {
						powders.add(rs.getString("powder"));
					}
				}

				batch.put(uuid, powders);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return batch;
	}

	@Override
	public void close() {
		dataSource.close();
	}

	@Override
	public Connection getConnection() throws SQLException {
		return dataSource.getConnection();
	}

}
