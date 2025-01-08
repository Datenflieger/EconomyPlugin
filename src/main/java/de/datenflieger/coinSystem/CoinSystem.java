package de.datenflieger.coinSystem;

import de.datenflieger.coinSystem.placeholder.CoinSystemPlaceholder;
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

        try {
            database.connect();
        } catch (SQLException e) {
            e.printStackTrace();
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        getCommand("baltop").setExecutor(new BaltopCommand(database, messages));
        getCommand("pay").setExecutor(new PayCommand(getDatabase(), messages));
        MoneyCommand moneyCommand = new MoneyCommand(database, messages);
        this.getCommand("money").setExecutor(moneyCommand);
        this.getCommand("money").setTabCompleter(moneyCommand);

        getServer().getPluginManager().registerEvents(new PlayerFirstTime(database), this);
        new CoinSystemPlaceholder(database).register();
    }

    private Database getDatabase() {
        return database;
    }
}
