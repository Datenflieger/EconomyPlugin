package de.datenflieger.coinSystem;

import de.datenflieger.coinSystem.commands.BaltopCommand;
import de.datenflieger.coinSystem.commands.MoneyCommand;
import de.datenflieger.coinSystem.commands.PayCommand;
import de.datenflieger.coinSystem.database.Database;
import de.datenflieger.coinSystem.listeners.PlayerDeathListener;
import de.datenflieger.coinSystem.listeners.PlayerFirstTime;
import de.datenflieger.coinSystem.placeholder.CoinSystemPlaceholder;
import de.datenflieger.coinSystem.utils.Messages;
import de.datenflieger.coinSystem.vault.VaultHook;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.SQLException;

public class CoinSystem extends JavaPlugin {
    private Database database;
    private Messages messages;
    private CoinSystemPlaceholder placeholderExpansion;

    @Override
    public void onEnable() {
        VaultHook.register(this);
        saveDefaultConfig();
        this.database = new Database(getConfig());
        this.messages = new Messages(this);
        FileConfiguration messages = getConfig();

        try {
            database.connect();
        } catch (SQLException e) {
            e.printStackTrace();
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        getCommand("baltop").setExecutor(new BaltopCommand(database, this.messages));
        getCommand("pay").setExecutor(new PayCommand(getDatabase(), this.messages));
        MoneyCommand moneyCommand = new MoneyCommand(database, this.messages);
        this.getCommand("money").setExecutor(moneyCommand);
        this.getCommand("money").setTabCompleter(moneyCommand);

        getServer().getPluginManager().registerEvents(new PlayerDeathListener(database, this.messages), this);
        getServer().getPluginManager().registerEvents(new PlayerFirstTime(database), this);
        new CoinSystemPlaceholder(database).register();
    }

    private Database getDatabase() {
        return database;
    }
}