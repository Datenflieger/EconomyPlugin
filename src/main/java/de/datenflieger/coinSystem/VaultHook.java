package de.datenflieger.coinSystem;

import net.milkbowl.vault.economy.AbstractEconomy;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.ServicePriority;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VaultHook extends AbstractEconomy {
    private final CoinSystem plugin;
    private final Database database;
    private final Map<String, Double> balances = new HashMap<>();

    public VaultHook(CoinSystem plugin, Database database) {
        this.plugin = plugin;
        this.database = database;
        Bukkit.getServicesManager().register(net.milkbowl.vault.economy.Economy.class, this, plugin, ServicePriority.Highest);
    }

    @Override
    public boolean isEnabled() {
        return plugin.isEnabled();
    }

    @Override
    public String getName() {
        return "CoinSystem";
    }

    @Override
    public boolean hasBankSupport() {
        return false;
    }

    @Override
    public int fractionalDigits() {
        return 2;
    }

    @Override
    public String format(double amount) {
        if (amount >= 1000) {
            return String.format("%.0fk Coins", amount / 1000);
        } else {
            return String.format("%.0f Coins", amount);
        }
    }


    @Override
    public String currencyNamePlural() {
        return "Coins";
    }

    @Override
    public String currencyNameSingular() {
        return "Coin";
    }

    @Override
    public boolean hasAccount(OfflinePlayer player) {
        try {
            return database.getBalance(player.getUniqueId().toString()) >= 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean hasAccount(String playerName) {
        OfflinePlayer player = Bukkit.getOfflinePlayer(playerName);
        return hasAccount(player);
    }

    @Override
    public boolean hasAccount(String playerName, String worldName) {
        return hasAccount(playerName);
    }

    @Override
    public double getBalance(OfflinePlayer player) {
        try {
            return database.getBalance(player.getUniqueId().toString());
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }

    @Override
    public double getBalance(String playerName) {
        OfflinePlayer player = Bukkit.getOfflinePlayer(playerName);
        return getBalance(player);
    }

    @Override
    public double getBalance(String playerName, String worldName) {
        return getBalance(playerName);
    }

    @Override
    public boolean has(String playerName, double amount) {
        return this.hasByName(playerName, amount);
    }

    @Override
    public boolean has(String playerName, String worldName, double amount) {
        return this.hasByName(playerName, amount);
    }


    private boolean hasByName(String playerName, double amount) {
        System.out.println("has() detected! Map: " + this.balances);
        return this.balances.getOrDefault(playerName, 0.0) >= amount;
    }

    @Override
    public EconomyResponse withdrawPlayer(String playerName, double amount) {
        return this.withdrawPlayer(playerName, null, amount);
    }

    @Override
    public EconomyResponse withdrawPlayer(OfflinePlayer player, double amount) {
        try {
            double balance = database.getBalance(player.getUniqueId().toString());
            if (balance >= amount) {
                database.setBalance(player.getUniqueId().toString(), balance - amount);
                return new EconomyResponse(amount, balance - amount, EconomyResponse.ResponseType.SUCCESS, null);
            } else {
                return new EconomyResponse(0, balance, EconomyResponse.ResponseType.FAILURE, "Insufficient funds");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return new EconomyResponse(0, 0, EconomyResponse.ResponseType.FAILURE, e.getMessage());
        }
    }

    @Override
    public EconomyResponse depositPlayer(String playerName, double amount) {
        return this.depositPlayer(playerName, null, amount);
    }

    @Override
    public EconomyResponse depositPlayer(OfflinePlayer player, double amount) {
        try {
            double balance = database.getBalance(player.getUniqueId().toString());
            database.setBalance(player.getUniqueId().toString(), balance + amount);
            return new EconomyResponse(amount, balance + amount, EconomyResponse.ResponseType.SUCCESS, null);
        } catch (SQLException e) {
            e.printStackTrace();
            return new EconomyResponse(0, 0, EconomyResponse.ResponseType.FAILURE, e.getMessage());
        }
    }

    @Override
    public EconomyResponse withdrawPlayer(String playerName, String worldName, double amount) {
        OfflinePlayer player = Bukkit.getOfflinePlayer(playerName);
        return withdrawPlayer(player, amount);
    }

    @Override
    public EconomyResponse depositPlayer(String playerName, String worldName, double amount) {
        OfflinePlayer player = Bukkit.getOfflinePlayer(playerName);
        return depositPlayer(player, amount);
    }

    @Override
    public EconomyResponse createBank(String s, String s1) {
        return null;
    }

    @Override
    public EconomyResponse deleteBank(String s) {
        return null;
    }

    @Override
    public EconomyResponse bankBalance(String s) {
        return null;
    }

    @Override
    public EconomyResponse bankHas(String s, double v) {
        return null;
    }

    @Override
    public EconomyResponse bankWithdraw(String s, double v) {
        return null;
    }

    @Override
    public EconomyResponse bankDeposit(String s, double v) {
        return null;
    }

    @Override
    public EconomyResponse isBankOwner(String s, String s1) {
        return null;
    }

    @Override
    public EconomyResponse isBankMember(String s, String s1) {
        return null;
    }

    @Override
    public List<String> getBanks() {
        return List.of();
    }

    @Override
    public boolean createPlayerAccount(String s) {
        return false;
    }

    @Override
    public boolean createPlayerAccount(String s, String s1) {
        return false;
    }

    public static void register(CoinSystem plugin) {
        Bukkit.getServicesManager().register(Economy.class, new VaultHook(plugin, new Database(plugin.getConfig())), plugin, ServicePriority.Normal);
    }
}