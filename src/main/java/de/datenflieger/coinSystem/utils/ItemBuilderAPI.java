package de.datenflieger.coinSystem.utils;

import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import net.kyori.adventure.text.Component;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;


public class ItemBuilderAPI {
    private final ItemStack itemStack;
    private final ItemMeta itemMeta;
    private final List<Component> lore;
    
    // Konstruktoren
    public ItemBuilderAPI(Material material, int amount) {
        this.itemStack = new ItemStack(material, amount);
        this.itemMeta = itemStack.getItemMeta();
        this.lore = new ArrayList<>();
    }

    public ItemBuilderAPI(ItemStack itemStack) {
        this.itemStack = itemStack;
        this.itemMeta = itemStack.getItemMeta();
        this.lore = new ArrayList<>();
    }

    // build() Methode
    public ItemStack build() {
        itemMeta.setLore(lore.stream().map(Component::toString).collect(Collectors.toList()));
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }

    // Name & Lore
    public ItemBuilderAPI setCustomModelData(int customModelData) {
        itemMeta.setCustomModelData(customModelData);
        return this;
    }

    public ItemBuilderAPI name(Component name) {
        itemMeta.setDisplayName(name.toString());
        return this;
    }

    public Component name() {
        return Component.text(itemMeta.getDisplayName());
    }

    public ItemBuilderAPI lore(Component... lore) {
        this.lore.clear();
        for (Component line : lore) {
            this.lore.add(line);
        }
        return this;
    }

    public ItemBuilderAPI addLore(Component... toAdd) {
        for (Component line : toAdd) {
            this.lore.add(line);
        }
        return this;
    }

    public ItemBuilderAPI addLastLore(Component component) {
        this.lore.add(component);
        return this;
    }

    public ItemBuilderAPI removeLastLore() {
        if (!this.lore.isEmpty()) {
            this.lore.remove(this.lore.size() - 1);
        }
        return this;
    }

    public ItemBuilderAPI loreIf(Supplier<Boolean> condition, List<Component> lore) {
        if (condition.get()) {
            this.lore.clear();
            this.lore.addAll(lore);
        }
        return this;
    }

    // ItemFlags & Eigenschaften
    public ItemBuilderAPI flags(ItemFlag... flags) {
        itemMeta.addItemFlags(flags);
        return this;
    }

    public ItemBuilderAPI removeFlags(ItemFlag... flags) {
        itemMeta.removeItemFlags(flags);
        return this;
    }
}
