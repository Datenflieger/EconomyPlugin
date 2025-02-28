package de.datenflieger.coinSystem;

import de.datenflieger.nevtroxapi.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.profile.PlayerProfile;
import org.bukkit.profile.PlayerTextures;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.InputStreamReader;
import java.net.URL;
import java.util.Base64;
import java.lang.reflect.Field;
import java.util.Base64;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;

public class BaltopCommand implements CommandExecutor, Listener {

    private final Database database;

    public BaltopCommand(Database database, Messages messages) {
        this.database = database;
        Bukkit.getPluginManager().registerEvents(this, Bukkit.getPluginManager().getPlugin("CoinSystem"));
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be used by players.");
            return true;
        }

        Player player = (Player) sender;
        Inventory gui = Bukkit.createInventory(null, 54, "§e§lCoins §8» §8Top 10 Gelder");

        // Gray pane background
        ItemStack grayPane = new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE)
                .setDisplayName("§f")
                .build();

        // Yellow pane for inner area
        ItemStack yellowPane = new ItemBuilder(Material.YELLOW_STAINED_GLASS_PANE)
                .setDisplayName("§f")
                .build();

        // Polished Blackstone Button for corners
        ItemStack cornerButton = new ItemBuilder(Material.POLISHED_BLACKSTONE_BUTTON)
                .setDisplayName("§f")
                .build();

        // Close button
        ItemStack closeButton = new ItemBuilder(Material.BARRIER)
                .setDisplayName("§4Menü schließen!")
                .setLore(Arrays.asList(" §8(§7*Linksklick§8)"))
                .build();

        // Fill background with gray panes
        for (int i = 0; i < 54; i++) {
            gui.setItem(i, grayPane);
        }

        // Set yellow panes in the middle area (excluding border)
        int[] yellowSlots = {
            10, 11, 12, 13, 14, 15, 16,
            19, 20, 21, 22, 23, 24, 25,
            28, 29, 30, 31, 32, 33, 34,
            37, 38, 39, 40, 41, 42, 43
        };
        for (int slot : yellowSlots) {
            gui.setItem(slot, yellowPane);
        }

        // Set corner buttons
        gui.setItem(0, cornerButton);  // Top left
        gui.setItem(8, cornerButton);  // Top right
        gui.setItem(45, cornerButton); // Bottom left
        gui.setItem(53, closeButton);  // Bottom right (close button)

        // Add top players
        List<PlayerBalance> topPlayers = getTopPlayers();

        if (topPlayers.size() > 0) {
            gui.setItem(13, createPlayerHead(topPlayers.get(0), 1, "§e§l"));
        }
        if (topPlayers.size() > 1) {
            gui.setItem(21, createPlayerHead(topPlayers.get(1), 2, "§f§l"));
        }
        if (topPlayers.size() > 2) {
            gui.setItem(23, createPlayerHead(topPlayers.get(2), 3, "§c§l"));
        }
        for (int i = 3; i < Math.min(10, topPlayers.size()); i++) {
            gui.setItem(28 + (i - 3), createPlayerHead(topPlayers.get(i), i + 1, "§a§l"));
        }

        player.openInventory(gui);
        return true;
    }

    private ItemStack createPlayerHead(PlayerBalance playerBalance, int rank, String rankColor) {
        // UUID aus der Datenbank
        String uuid = playerBalance.getPlayerName(); // Dies ist jetzt die UUID
        String playerName = null;
        
        try {
            // Versuche den Spielernamen über die Mojang API zu bekommen
            URL url = new URL("https://api.minetools.eu/profile/" + uuid.replace("-", ""));
            InputStreamReader reader = new InputStreamReader(url.openStream());
            JsonObject profile = JsonParser.parseReader(reader).getAsJsonObject();
            
            if (!profile.get("status").getAsString().equals("ERR")) {
                playerName = profile.get("name").getAsString();
            }
        } catch (Exception e) {
            // Wenn die API nicht funktioniert, versuche es über Bukkit
            OfflinePlayer player = Bukkit.getOfflinePlayer(UUID.fromString(uuid));
            if (player != null) {
                playerName = player.getName();
            }
        }

        // Fallback wenn kein Name gefunden wurde
        if (playerName == null) {
            playerName = "Unbekannt";
        }

        String displayName = "§7Platz " + rankColor + rank + ". §8→ " + rankColor + playerName;
        List<String> lore = new ArrayList<>();
        lore.add("§f");
        lore.add("§8→ §7mit §e" + playerBalance.getFormattedBalance() + " §7Coins§8.");

        ItemStack head = new ItemBuilder(Material.PLAYER_HEAD)
                .setDisplayName(displayName)
                .setLore(lore)
                .build();

        SkullMeta meta = (SkullMeta) head.getItemMeta();
        meta.setOwner(Bukkit.getOfflinePlayer(UUID.fromString(uuid)).getName());
        head.setItemMeta(meta);

        return head;
    }

    private List<PlayerBalance> getTopPlayers() {
        List<PlayerBalance> topPlayers = new ArrayList<>();
        try (Connection connection = database.getConnection();
             PreparedStatement stmt = connection.prepareStatement(
                     "SELECT uuid, balance FROM player_balance ORDER BY balance DESC LIMIT 10")) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                String uuid = rs.getString("uuid");
                double balance = rs.getDouble("balance");
                // Direkt die UUID als String übergeben
                topPlayers.add(new PlayerBalance(uuid, balance));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return topPlayers;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getView().getTitle().equals("§e§lCoins §8» §8Top 10 Gelder")) {
            event.setCancelled(true);
            
            // Handle close button click
            if (event.getCurrentItem() != null && 
                event.getCurrentItem().getType() == Material.BARRIER) {
                event.getWhoClicked().closeInventory();
            }
        }
    }
}