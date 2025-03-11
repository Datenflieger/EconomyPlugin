package de.datenflieger.coinSystem.commands;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import de.datenflieger.coinSystem.database.Database;
import de.datenflieger.coinSystem.utils.Messages;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class MoneyCommand implements CommandExecutor, TabCompleter {
    private final Database database;
    private final Messages messages;

    public MoneyCommand(Database database, Messages messages) {
        this.database = database;
        this.messages = messages;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            if (sender instanceof Player player) {
                double totalEconomy = 0;
                try {
                    totalEconomy = database.getServerWirtschaft();
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
                try {
                    double balance = database.getBalance(player.getUniqueId().toString());
                    player.sendMessage(messages.getPrefix() + messages.getBalanceMessage(balance));
                    player.sendMessage(messages.getPrefix() + messages.getEconomyMessage(totalEconomy));
                } catch (SQLException e) {
                    e.printStackTrace();
                    player.sendMessage(messages.getPrefix() + messages.getErrorMessage());
                }
            } else {
                sender.sendMessage(messages.getPrefix() + messages.getPlayerOnlyMessage());
            }
            return true;
        }

        if (args.length == 1) {
            if (sender.hasPermission("coinsystem.view.others")) {
                OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
                try {
                    double balance = database.getBalance(target.getUniqueId().toString());
                    sender.sendMessage(messages.getPrefix() + messages.getViewOthersBalanceMessage(target.getName(), balance));
                } catch (SQLException e) {
                    e.printStackTrace();
                    sender.sendMessage(messages.getPrefix() + messages.getErrorMessage());
                }
            } else {
                sender.sendMessage(messages.getPrefix() + messages.getNoPermissionMessage());
            }
            return true;
        }

        if (args.length == 2 && (args[0].equalsIgnoreCase("set") || args[0].equalsIgnoreCase("give") || args[0].equalsIgnoreCase("take"))) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage(messages.getPrefix() + messages.getPlayerOnlyMessage());
                return true;
            }

            try {
                double amount = Double.parseDouble(args[1]);
                double balance = database.getBalance(player.getUniqueId().toString());

                if (args[0].equalsIgnoreCase("set")) {
                    database.setBalance(player.getUniqueId().toString(), amount);
                    player.sendMessage(messages.getPrefix() + messages.getSetMessage(player.getName(), amount));
                } else if (args[0].equalsIgnoreCase("give")) {
                    database.setBalance(player.getUniqueId().toString(), balance + amount);
                    player.sendMessage(messages.getPrefix() + messages.getGiveMessage(player.getName(), amount));
                } else if (args[0].equalsIgnoreCase("take")) {
                    if (balance >= amount) {
                        database.setBalance(player.getUniqueId().toString(), balance - amount);
                        player.sendMessage(messages.getPrefix() + messages.getTakeMessage(player.getName(), amount));
                    } else {
                        player.sendMessage(messages.getPrefix() + messages.getInsufficientFundsMessage(player.getName()));
                    }
                }
            } catch (NumberFormatException | SQLException e) {
                e.printStackTrace();
                player.sendMessage(messages.getPrefix() + messages.getErrorMessage());
            }
            return true;
        }

        if (args.length == 3 && args[0].equalsIgnoreCase("set")) {
            if (sender.hasPermission("coinsystem.set")) {
                OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
                try {
                    double amount = Double.parseDouble(args[2]);
                    database.setBalance(target.getUniqueId().toString(), amount);
                    sender.sendMessage(messages.getPrefix() + messages.getSetMessage(target.getName(), amount));
                } catch (NumberFormatException | SQLException e) {
                    e.printStackTrace();
                    sender.sendMessage(messages.getPrefix() + messages.getErrorMessage());
                }
            } else {
                sender.sendMessage(messages.getPrefix() + messages.getNoPermissionMessage());
            }
            return true;
        }

        if (args.length == 3 && args[0].equalsIgnoreCase("give")) {
            if (sender.hasPermission("coinsystem.give")) {
                OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
                try {
                    double amount = Double.parseDouble(args[2]);
                    double balance = database.getBalance(target.getUniqueId().toString());
                    database.setBalance(target.getUniqueId().toString(), balance + amount);
                    sender.sendMessage(messages.getPrefix() + messages.getGiveMessage(target.getName(), amount));
                } catch (NumberFormatException | SQLException e) {
                    e.printStackTrace();
                    sender.sendMessage(messages.getPrefix() + messages.getErrorMessage());
                }
            } else {
                sender.sendMessage(messages.getPrefix() + messages.getNoPermissionMessage());
            }
            return true;
        }

        if (args.length == 3 && args[0].equalsIgnoreCase("take")) {
            if (sender.hasPermission("coinsystem.take")) {
                OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
                try {
                    double amount = Double.parseDouble(args[2]);
                    double balance = database.getBalance(target.getUniqueId().toString());
                    if (balance >= amount) {
                        database.setBalance(target.getUniqueId().toString(), balance - amount);
                        sender.sendMessage(messages.getPrefix() + messages.getTakeMessage(target.getName(), amount));
                    } else {
                        sender.sendMessage(messages.getPrefix() + messages.getInsufficientFundsMessage(target.getName()));
                    }
                } catch (NumberFormatException | SQLException e) {
                    e.printStackTrace();
                    sender.sendMessage(messages.getPrefix() + messages.getErrorMessage());
                }
            } else {
                sender.sendMessage(messages.getPrefix() + messages.getNoPermissionMessage());
            }
            return true;
        }

        return false;
    }


    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> subCommands = Arrays.asList("set", "give", "take");
            List<String> completions = new ArrayList<>();
            if (sender.hasPermission("coinsystem.view.others")) {
                completions.addAll(Arrays.stream(Bukkit.getOfflinePlayers())
                        .map(OfflinePlayer::getName)
                        .collect(Collectors.toList()));
            }
            completions.addAll(subCommands);
            return completions.stream()
                    .filter(s -> s.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        } else if (args.length == 2 && Arrays.asList("set", "give", "take").contains(args[0].toLowerCase())) {
            return Arrays.stream(Bukkit.getOfflinePlayers())
                    .map(OfflinePlayer::getName)
                    .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
        }
        return null;
    }
}