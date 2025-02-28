package de.datenflieger.coinSystem;

import de.datenflieger.coinSystem.placeholder.CoinSystemPlaceholder;
import de.datenflieger.nevtroxapi.translate.NevtroxTranslate;
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

        Bukkit.getConsoleSender().sendMessage(NevtroxTranslate.translate(("§8§l---------------------------------")));
        Bukkit.getConsoleSender().sendMessage(NevtroxTranslate.translate(("&b&o<GRADIENT:00FA2F>NEVTROX</GRADIENT:003209> &8&o● &7&oCoin System")));
        Bukkit.getConsoleSender().sendMessage(NevtroxTranslate.translate(("&7Plugin by &aDatenflieger")));
        Bukkit.getConsoleSender().sendMessage(NevtroxTranslate.translate("§8§l---------------------------------"));
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