package de.datenflieger.coinSystem;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Database {
    private Connection connection;
    private final FileConfiguration config;

    public Database(FileConfiguration config) {
        this.config = config;
    }

    public void connect() throws SQLException {
        String type = config.getString("database.type");
        if ("mysql".equalsIgnoreCase(type)) {
            String host = config.getString("database.host");
            int port = config.getInt("database.port");
            String name = config.getString("database.name");
            String user = config.getString("database.user");
            String password = config.getString("database.password");

            connection = DriverManager.getConnection(
                    "jdbc:mysql://" + host + ":" + port + "/" + name + "?useSSL=false&autoReconnect=true", user, password);
        } else {
            connection = DriverManager.getConnection("jdbc:sqlite:coinsystem.db");
        }

        String createTableSQL = "CREATE TABLE IF NOT EXISTS player_balance (" +
                "uuid CHAR(36) PRIMARY KEY, " +
                "balance DECIMAL(18, 2))";

        try (PreparedStatement stmt = connection.prepareStatement(createTableSQL)) {
            stmt.executeUpdate();
        }
    }

    public String getFormattedBalance(String uuid) throws SQLException {
        double balance = getBalance(uuid); // Holt den aktuellen Kontostand
        return formatBalance(balance);     // Formatiert den Kontostand
    }

    public double getBalance(String uuid) throws SQLException {
        validateConnection();
        try (PreparedStatement stmt = connection.prepareStatement(
                "SELECT balance FROM player_balance WHERE uuid = ?")) {
            stmt.setString(1, uuid);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getDouble("balance");
            } else {
                return 0.0;
            }
        }
    }


    public static String formatBalance(double amount) {
        if (amount >= 1000000) {
            double million = amount / 1000000.0;
            return String.format("%.1fM", million);
        } else if (amount >= 1000) {
            double thousand = amount / 1000.0;
            return String.format("%.1fk", thousand);
        } else {
            return String.format("%.0f", amount);
        }
    }


    public void setBalance(String uuid, double balance) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(
                "INSERT INTO player_balance (uuid, balance) VALUES (?, ?) ON DUPLICATE KEY UPDATE balance = ?")) {
            stmt.setString(1, uuid);
            stmt.setDouble(2, balance);
            stmt.setDouble(3, balance);
            stmt.executeUpdate();
        }
    }

    public List<PlayerBalance> getTopPlayers() throws SQLException {
        List<PlayerBalance> topPlayers = new ArrayList<>();
        String query = "SELECT uuid, balance FROM player_balance ORDER BY balance DESC LIMIT 10";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                String uuid = rs.getString("uuid");
                double balance = rs.getDouble("balance");
                String playerName = getPlayerNameByUUID(uuid);
                topPlayers.add(new PlayerBalance(playerName, balance));
            }
        }

        return topPlayers;
    }

    public void validateConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connect();
        }
    }

    private String getPlayerNameByUUID(String uuid) {

        return Bukkit.getOfflinePlayer(UUID.fromString(uuid)).getName();
    }

    public Connection getConnection() throws SQLException {
        String type = config.getString("database.type");
        if ("mysql".equalsIgnoreCase(type)) {
            String host = config.getString("database.host");
            int port = config.getInt("database.port");
            String name = config.getString("database.name");
            String user = config.getString("database.user");
            String password = config.getString("database.password");
            validateConnection();
            return DriverManager.getConnection(
                    "jdbc:mysql://" + host + ":" + port + "/" + name, user, password);
        } else {
            return DriverManager.getConnection("jdbc:sqlite:coinsystem.db");
        }
    }

    public double getServerWirtschaft() throws SQLException {
        validateConnection();
        String query = "SELECT SUM(balance) AS total_balance FROM player_balance";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getDouble("total_balance");
            } else {
                return 0.0;
            }
        }
    }
}
