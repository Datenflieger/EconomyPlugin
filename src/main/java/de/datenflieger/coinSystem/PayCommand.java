package de.datenflieger.coinSystem;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.SQLException;

public class PayCommand implements CommandExecutor {
    private final Database database;
    private final Messages messages;

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
        OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
        double amount;

        try {
            amount = Double.parseDouble(args[1]);
        } catch (NumberFormatException e) {
            player.sendMessage(messages.getPrefix() + messages.getErrorMessage());
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