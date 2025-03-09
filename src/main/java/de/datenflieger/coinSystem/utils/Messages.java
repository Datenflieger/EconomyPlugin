package de.datenflieger.coinSystem.utils;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;

public class Messages {
    private final FileConfiguration config;

    public Messages(JavaPlugin plugin) {
        File file = new File(plugin.getDataFolder(), "messages.yml");
        if (!file.exists()) {
            plugin.saveResource("messages.yml", false);
        }
        this.config = YamlConfiguration.loadConfiguration(file);
    }

    public String getPrefix() {
        return config.getString("prefix");
    }

    public String getBalanceMessage(double balance) {
        return config.getString("balance").replace("%balance%", formatBalance(balance));
    }

    public String getErrorMessage() {
        return config.getString("error");
    }

    public String getPlayerOnlyMessage() {
        return config.getString("playerOnly");
    }

    public String getNoPermissionMessage() {
        return config.getString("noPermission");
    }

    public String getSetMessage(String playerName, double amount) {
        return config.getString("set").replace("%player%", playerName).replace("%amount%", String.valueOf(amount));
    }

    public String getGiveMessage(String playerName, double amount) {
        return config.getString("give").replace("%player%", playerName).replace("%amount%", String.valueOf(amount));
    }

    public String getTakeMessage(String playerName, double amount) {
        return config.getString("take").replace("%player%", playerName).replace("%amount%", String.valueOf(amount));
    }

    public String getInsufficientFundsMessage(String playerName) {
        return config.getString("insufficientFunds").replace("%player%", playerName);
    }

    public String getViewOthersBalanceMessage(String playerName, double balance) {
        return config.getString("viewOthersBalance").replace("%player%", playerName).replace("%balance%", formatBalance(balance));
    }

    public String getPaySuccessMessage(String playerName, double amount) {
        return config.getString("paySuccess").replace("%player%", playerName).replace("%amount%", String.valueOf(amount));
    }

    public String getPayReceivedMessage(String playerName, double amount) {
        return config.getString("payReceived").replace("%player%", playerName).replace("%amount%", String.valueOf(amount));
    }

    public String getEconomyMessage(double totalEconomy) {
        return config.getString("economy").replace("%economy%", formatBalance(totalEconomy));
    }

    public String getBroadcastDeathMessage() {
        return config.getString("broadcastDeathMessage");
    }

    public String getNegativeAmountMessage() {
        return "§cYou cannot send negative or zero amounts!";
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
}
