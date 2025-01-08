package de.datenflieger.coinSystem;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;


import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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
        Inventory gui = Bukkit.createInventory(null, 54, "Top 10 Gelder");

        ItemStack grayPane = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta paneMeta = grayPane.getItemMeta();
        paneMeta.setDisplayName("§f");
        grayPane.setItemMeta(paneMeta);

        int[] paneSlots = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 17, 18, 26, 27, 35, 36, 45, 46, 47, 48, 49, 50, 51, 52, 53};
        for (int slot : paneSlots) {
            gui.setItem(slot, grayPane);
        }

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
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) head.getItemMeta();

        // Abrufen der Textur von Mojang
        String texture = getPlayerTexture(playerBalance.getPlayerName());

        if (texture != null && !texture.isEmpty()) {
            // Setze die Textur, auch wenn der Spieler offline ist
            GameProfile profile = new GameProfile(UUID.randomUUID(), playerBalance.getPlayerName());
            profile.getProperties().put("textures", new Property("textures", texture));

            try {
                Field profileField = meta.getClass().getDeclaredField("profile");
                profileField.setAccessible(true);
                profileField.set(meta, profile);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        meta.setDisplayName("§7Platz " + rankColor + rank + ". §8→ " + rankColor + playerBalance.getPlayerName());
        List<String> lore = new ArrayList<>();
        lore.add("§f");
        lore.add("§8→ §7mit §e" + playerBalance.getFormattedBalance() + " §7Coins§8.");
        meta.setLore(lore);
        head.setItemMeta(meta);
        return head;
    }

    private String getPlayerTexture(String playerName) {
        String texture = null;
        try {
            URL url = new URL("https://api.mojang.com/users/profiles/minecraft/" + playerName);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            String jsonResponse = response.toString();
            String uuid = jsonResponse.split("\"id\":\"")[1].split("\"")[0];

            URL textureUrl = new URL("https://sessionserver.mojang.com/session/minecraft/profile/" + uuid);
            HttpURLConnection textureConnection = (HttpURLConnection) textureUrl.openConnection();
            textureConnection.setRequestMethod("GET");
            BufferedReader textureIn = new BufferedReader(new InputStreamReader(textureConnection.getInputStream()));
            StringBuilder textureResponse = new StringBuilder();
            while ((inputLine = textureIn.readLine()) != null) {
                textureResponse.append(inputLine);
            }
            textureIn.close();

            // Extrahieren der Textur-URL
            String textureUrlString = textureResponse.toString().split("\"value\":\"")[1].split("\"")[0];
            texture = textureUrlString;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return texture;
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
                String playerName = Bukkit.getOfflinePlayer(UUID.fromString(uuid)).getName();
                topPlayers.add(new PlayerBalance(playerName, balance));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return topPlayers;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getView().getTitle().equals("Top 10 Gelder")) {
            event.setCancelled(true);
        }
    }
}
