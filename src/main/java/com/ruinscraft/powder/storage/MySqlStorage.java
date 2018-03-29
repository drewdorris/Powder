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
			PreparedStatement ps = c.prepareStatement("CREATE TABLE IF NOT EXISTS " + powdersTable + " (uuid VARCHAR(36), powder VARCHAR(32));");

			ps.executeUpdate();

			ps.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public List<String> getEnabledPowders(UUID uuid) {
		List<String> powders = new ArrayList<>();

		try (Connection c = getConnection()) {
			PreparedStatement ps = c.prepareStatement("SELECT * FROM " + powdersTable + " WHERE uuid = ?;");

			ps.setString(1, uuid.toString());

			ResultSet rs = ps.executeQuery();

			while (rs.next()) {
				powders.add(rs.getString("powder"));
			}

			rs.close();
			ps.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return powders;
	}

	@Override
	public void saveEnabledPowders(UUID uuid, List<String> powders) {
		try (Connection c = getConnection()) {
			PreparedStatement ps = null;

			ps = c.prepareStatement("DELETE FROM " + powdersTable + " WHERE uuid = ?");
			ps.setString(1, uuid.toString());

			ps.executeUpdate();

			ps.close();
			
			ps = c.prepareStatement("INSERT INTO " + powdersTable + " (uuid, powder) VALUES (?, ?);");
			
			for (String powder : powders) {
				ps.setString(1, uuid.toString());
				ps.setString(2, powder);
				ps.addBatch();
			}
			ps.executeBatch();
			
			ps.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void saveAll() {
		try (Connection c = getConnection()) {
			PreparedStatement ps = null;

			for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
				ps = c.prepareStatement("DELETE FROM " + powdersTable + " WHERE uuid = ?");
				ps.setString(1, onlinePlayer.getUniqueId().toString());

				ps.executeUpdate();

				ps.close();

				ps = c.prepareStatement("INSERT INTO " + powdersTable + " (uuid, powder) VALUES (?, ?);");
				
				for (String powder : PowderUtil.getEnabledPowderNames(onlinePlayer.getUniqueId())) {
					ps.setString(1, onlinePlayer.getUniqueId().toString());
					ps.setString(2, powder);
					ps.addBatch();
				}
				
				ps.executeBatch();
				
				ps.close();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void loadAll() {
		for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
			List<String> enabledPowders = getEnabledPowders(onlinePlayer.getUniqueId());

			for (String powderName : enabledPowders) {
				PowderUtil.loadPowderFromName(onlinePlayer, powderName);
			}
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
