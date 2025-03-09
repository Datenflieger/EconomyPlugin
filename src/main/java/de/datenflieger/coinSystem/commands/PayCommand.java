package de.datenflieger.coinSystem.commands;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import de.datenflieger.coinSystem.database.Database;
import de.datenflieger.coinSystem.utils.Messages;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PayCommand implements CommandExecutor {
    private final Database database;
    private final Messages messages;
    private final Map<UUID, Long> cooldowns = new HashMap<>();
    private static final long COOLDOWN_TIME = 2000; // 2 seconds in milliseconds

    public PayCommand(Database database, Messages messages) {
        this.database = database;
        this.messages = messages;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length != 2) {
            return false;
        }

        if (!(sender instanceof Player)) {
            sender.sendMessage(messages.getPrefix() + messages.getPlayerOnlyMessage());
            return true;
        }

        Player player = (Player) sender;
        
        // Check cooldown
        long currentTime = System.currentTimeMillis();
        if (cooldowns.containsKey(player.getUniqueId())) {
            long timeLeft = COOLDOWN_TIME - (currentTime - cooldowns.get(player.getUniqueId()));
            if (timeLeft > 0) {
                player.sendMessage(messages.getPrefix() + "§cBitte warte noch " + (timeLeft / 1000.0) + " Sekunden.");
                return true;
            }
        }
        cooldowns.put(player.getUniqueId(), currentTime);

        // Validate player name
        if (!args[0].matches("^[a-zA-Z0-9_]{3,16}$")) {
            player.sendMessage(messages.getPrefix() + messages.getErrorMessage());
            return true;
        }
        
        // Prevent self-payment
        if (args[0].equalsIgnoreCase(player.getName())) {
            player.sendMessage(messages.getPrefix() + "§cDu kannst dir selbst kein Geld überweisen!");
            return true;
        }
        
        OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
        double amount;

        // Sanitize and validate amount - only allow whole numbers
        String amountStr = args[1].replaceAll("[^0-9]", "");
        if (!amountStr.matches("^\\d+$")) {
            player.sendMessage(messages.getPrefix() + messages.getErrorMessage());
            return true;
        }

        try {
            amount = Double.parseDouble(amountStr);
        } catch (NumberFormatException e) {
            player.sendMessage(messages.getPrefix() + messages.getErrorMessage());
            return true;
        }

        if (amount <= 0) {
            player.sendMessage(messages.getPrefix() + messages.getNegativeAmountMessage());
            return true;
        }

        try {
            double playerBalance = database.getBalance(player.getUniqueId().toString());
            if (playerBalance < amount) {
                player.sendMessage(messages.getPrefix() + messages.getInsufficientFundsMessage(player.getName()));
                return true;
            }

            double targetBalance = database.getBalance(target.getUniqueId().toString());
            database.setBalance(player.getUniqueId().toString(), playerBalance - amount);
            database.setBalance(target.getUniqueId().toString(), targetBalance + amount);

            player.sendMessage(messages.getPrefix() + messages.getPaySuccessMessage(target.getName(), amount));
            if (target.isOnline()) {
                ((Player) target).sendMessage(messages.getPrefix() + messages.getPayReceivedMessage(player.getName(), amount));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            player.sendMessage(messages.getPrefix() + messages.getErrorMessage());
        }

        return true;
    }
}