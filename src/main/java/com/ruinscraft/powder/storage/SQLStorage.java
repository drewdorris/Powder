package com.ruinscraft.powder.storage;

import java.sql.Connection;
import java.sql.SQLException;

public interface SQLStorage extends Storage {

	void createTable();

	Connection getConnection() throws SQLException;

}
