package com.ruinscraft.powder.storage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.ruinscraft.powder.util.PowderUtil;
import com.zaxxer.hikari.HikariDataSource;

public class MySqlStorage implements SqlStorage {

	private final String powdersTable;
	private final HikariDataSource hikari;

	public MySqlStorage(String host, int port, String database, String username, String password, String powdersTable) {
		this.powdersTable = powdersTable;

		hikari = new HikariDataSource();
		hikari.setJdbcUrl("jdbc:mysql://" + host + ":" + port + "/" + database);
		hikari.setUsername(username);
		hikari.setPassword(password);
		hikari.setPoolName("powder-pool");
		hikari.setMaximumPoolSize(10);

		createTable();
	}

	@Override
	public void createTable() {
		try (Connection c = getConnection()) {
			String update = "CREATE TABLE IF NOT EXISTS " + powdersTable + " (uuid VARCHAR(36), powder VARCHAR(32));";
			
			try (PreparedStatement ps = c.prepareStatement(update)) {
				ps.executeUpdate();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public List<String> getEnabledPowders(UUID uuid) {
		List<String> powders = new ArrayList<>();

		try (Connection c = getConnection()) {
			String query = "SELECT * FROM " + powdersTable + " WHERE uuid = ?;";
			
			try (PreparedStatement ps = c.prepareStatement(query)) {
				ps.setString(1, uuid.toString());
				
				try (ResultSet rs = ps.executeQuery()) {
					while (rs.next()) {
						powders.add(rs.getString("powder"));
					}
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return powders;
	}

	@Override
	public void saveEnabledPowders(UUID uuid, List<String> powders) {
		try (Connection c = getConnection()) {
			String deleteQuery = "DELETE FROM " + powdersTable + " WHERE uuid = ?";
			String insertQuery = "INSERT INTO " + powdersTable + " (uuid, powder) VALUES (?, ?);";

			try (PreparedStatement delete = c.prepareStatement(deleteQuery)) {
				delete.setString(1, uuid.toString());
				delete.executeUpdate();
			}
			
			try (PreparedStatement insert = c.prepareStatement(insertQuery)) {
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
	public void saveAll() {
		try (Connection c = getConnection()) {
			String deleteQuery = "DELETE FROM " + powdersTable + " WHERE uuid = ?";
			String insertQuery = "INSERT INTO " + powdersTable + " (uuid, powder) VALUES (?, ?);";

			try (PreparedStatement delete = c.prepareStatement(deleteQuery)) {
				for (Player player : Bukkit.getOnlinePlayers()) {
					delete.setString(1, player.getUniqueId().toString());
					
					delete.addBatch();
				}
				
				delete.executeBatch();
			}
			
			try (PreparedStatement insert = c.prepareStatement(insertQuery)) {
				for (Player player : Bukkit.getOnlinePlayers()) {
					for (String powder : PowderUtil.getEnabledPowderNames(player.getUniqueId())) {
						insert.setString(1, player.getUniqueId().toString());
						insert.setString(2, powder);
						insert.addBatch();
					}
					
					insert.executeBatch();
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void loadAll() {
		try (Connection c = getConnection()) {
			String query = "SELECT * FROM " + powdersTable + " WHERE uuid = ?;";

			try (PreparedStatement ps = c.prepareStatement(query)) {
				for (Player player : Bukkit.getOnlinePlayers()) {
					ps.setString(1, player.getUniqueId().toString());

					try (ResultSet rs = ps.executeQuery()) {
						List<String> powders = new ArrayList<>();

						while (rs.next()) {
							powders.add(rs.getString("powder"));
						}

						for (String powder : powders) {
							PowderUtil.loadPowderFromName(player, powder);
						}
					}
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public Connection getConnection() throws SQLException {
		return hikari.getConnection();
	}

	@Override
	public void close() throws IOException {
		hikari.close();
	}

}
