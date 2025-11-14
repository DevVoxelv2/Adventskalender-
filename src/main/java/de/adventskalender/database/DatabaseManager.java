package de.adventskalender.database;

import de.adventskalender.AdventskalenderPlugin;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class DatabaseManager {

    private final AdventskalenderPlugin plugin;
    private HikariDataSource dataSource;

    public DatabaseManager(AdventskalenderPlugin plugin) {
        this.plugin = plugin;
    }

    public void initialize() {
        String dbType = plugin.getConfig().getString("database.type", "sqlite");
        
        HikariConfig config = new HikariConfig();
        
        if (dbType.equalsIgnoreCase("sqlite")) {
            config.setJdbcUrl("jdbc:sqlite:" + plugin.getDataFolder().getAbsolutePath() + "/adventskalender.db");
            config.setDriverClassName("org.sqlite.JDBC");
        } else {
            String host = plugin.getConfig().getString("database.host", "localhost");
            int port = plugin.getConfig().getInt("database.port", 3306);
            String database = plugin.getConfig().getString("database.database", "adventskalender");
            String username = plugin.getConfig().getString("database.username", "root");
            String password = plugin.getConfig().getString("database.password", "password");
            
            config.setJdbcUrl("jdbc:mysql://" + host + ":" + port + "/" + database);
            config.setUsername(username);
            config.setPassword(password);
            config.setDriverClassName("com.mysql.cj.jdbc.Driver");
        }
        
        config.setMaximumPoolSize(10);
        config.setMinimumIdle(2);
        config.setConnectionTimeout(30000);
        
        dataSource = new HikariDataSource(config);
        createTable();
    }

    private void createTable() {
        try (Connection connection = dataSource.getConnection()) {
            String sql = "CREATE TABLE IF NOT EXISTS opened_doors (" +
                    "uuid VARCHAR(36) NOT NULL, " +
                    "day INT NOT NULL, " +
                    "opened_at BIGINT NOT NULL, " +
                    "PRIMARY KEY (uuid, day)" +
                    ")";
            connection.createStatement().execute(sql);
        } catch (SQLException e) {
            plugin.getLogger().severe("Fehler beim Erstellen der Datenbanktabelle: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public boolean hasOpenedDoor(UUID uuid, int day) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT * FROM opened_doors WHERE uuid = ? AND day = ?")) {
            
            statement.setString(1, uuid.toString());
            statement.setInt(2, day);
            
            ResultSet resultSet = statement.executeQuery();
            return resultSet.next();
        } catch (SQLException e) {
            plugin.getLogger().severe("Fehler beim Prüfen der geöffneten Tür: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public void setDoorOpened(UUID uuid, int day) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "INSERT OR REPLACE INTO opened_doors (uuid, day, opened_at) VALUES (?, ?, ?)")) {
            
            statement.setString(1, uuid.toString());
            statement.setInt(2, day);
            statement.setLong(3, System.currentTimeMillis());
            
            statement.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().severe("Fehler beim Speichern der geöffneten Tür: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public void close() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }
}

