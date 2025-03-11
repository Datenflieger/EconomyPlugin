package de.datenflieger.coinSystem.placeholder;

import de.datenflieger.coinSystem.utils.Messages;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;

import de.datenflieger.coinSystem.database.Database;

import java.sql.SQLException;

public class CoinSystemPlaceholder extends PlaceholderExpansion {

    private final Database database;

    public CoinSystemPlaceholder(Database database) {
        this.database = database;
    }

    @Override
    public String getIdentifier() {
        return "coinsystem";
    }

    @Override
    public String getAuthor() {
        return "Datenflieger";
    }

    @Override
    public String getVersion() {
        return "1.0";
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onPlaceholderRequest(Player player, String identifier) {
        if (player == null) {
            return "";
        }

        if (identifier.equals("player_coins")) {
            double balance = 0;
            try {
                balance = database.getBalance(player.getUniqueId().toString());
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            return Messages.formatBalance(balance);
        }

        if (identifier.equals("server_economy")) {
            double totalBalance = 0;
            try {
                totalBalance = database.getServerWirtschaft();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            return Messages.formatBalance(totalBalance);
        }

        return null;
    }

}
