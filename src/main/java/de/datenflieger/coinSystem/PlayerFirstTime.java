package de.datenflieger.coinSystem;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.SQLException;

public class PlayerFirstTime implements Listener {

    private final Database database;

    public PlayerFirstTime(Database database) {
        this.database = database;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        String playerUUID = event.getPlayer().getUniqueId().toString();

        try {
            if (database.getBalance(playerUUID) == 0) {

                double startBalance = 1000.0;
                database.setBalance(playerUUID, startBalance);

            }
        } catch (SQLException e) {
            e.printStackTrace();
            event.getPlayer().sendMessage("§cEs gab ein Problem beim Abrufen deiner Coins.");
        }
    }
}
