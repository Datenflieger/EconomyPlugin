package de.datenflieger.coinSystem;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

import java.sql.SQLException;

public class PlayerDeathListener implements Listener {

    private final Database database;
    private final Messages messages;

    public PlayerDeathListener(Database database, Messages messages) {
        this.database = database;
        this.messages = messages;
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        String playerUUID = event.getEntity().getUniqueId().toString();
        try {
            double currentBalance = database.getBalance(playerUUID);
            double lossAmount = currentBalance * 0.20;
            double newBalance = currentBalance - lossAmount;
            database.setBalance(playerUUID, newBalance);

            String prefix = messages.getPrefix();
            String formattedLossAmount = Messages.formatBalance(lossAmount);

            String broadcastMessageTemplate = messages.getBroadcastDeathMessage();
            if (broadcastMessageTemplate != null) {
                String broadcastMessage = broadcastMessageTemplate
                        .replace("{player}", event.getEntity().getName())
                        .replace("{amount}", formattedLossAmount);

                Bukkit.broadcastMessage(prefix + broadcastMessage);

                for (Player player : Bukkit.getOnlinePlayers()) {
                    player.playSound(player.getLocation(), Sound.BLOCK_END_PORTAL_SPAWN, 1.0f, 1.0f);
                }
            } else {
                event.getEntity().sendMessage("\u00a7cDie Broadcast-Nachricht konnte nicht gefunden werden.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            event.getEntity().sendMessage("\u00a7cEs gab ein Problem beim Aktualisieren deines Kontostands.");
        }
    }
}
