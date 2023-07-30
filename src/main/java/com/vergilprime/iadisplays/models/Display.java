package com.vergilprime.iadisplays.models;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class Display {
	private String world;
	private ItemStack item;
	private UUID hitbox;
	private List<UUID> itemDisplays;
	private List<UUID> textDisplays;

	public Display(String world, ItemStack item, UUID hitbox, List<UUID> itemDisplays, List<UUID> textDisplays) {
		this.world = world;
		this.item = item;
		this.hitbox = hitbox;
		this.itemDisplays = itemDisplays;
		this.textDisplays = textDisplays;
	}

	public ConfigurationSection toSave() {
		ConfigurationSection configurationSection = new YamlConfiguration();
		configurationSection.set("world", world);
		configurationSection.set("item", item);
		configurationSection.set("hitbox", hitbox.toString()); // Convert UUID to String
		configurationSection.set("itemDisplays", itemDisplays.stream().map(UUID::toString).collect(Collectors.toList())); // Convert each UUID to String
		configurationSection.set("textDisplays", textDisplays.stream().map(UUID::toString).collect(Collectors.toList())); // Convert each UUID to String
		return configurationSection;
	}

	public static Display fromSave(ConfigurationSection config) {
		String world = config.getString("world");
		ItemStack item = config.getItemStack("item");
		UUID hitbox = UUID.fromString(config.getString("hitbox")); // Convert String to UUID
		List<UUID> itemDisplays = ((List<String>) config.getStringList("itemDisplays")).stream().map(UUID::fromString).collect(Collectors.toList()); // Convert each String to UUID
		List<UUID> textDisplays = ((List<String>) config.getStringList("textDisplays")).stream().map(UUID::fromString).collect(Collectors.toList()); // Convert each String to UUID
		return new Display(world, item, hitbox, itemDisplays, textDisplays);
	}

	public ItemStack getItem() {
		return item;
	}

	public List<UUID> getItemDisplays() {
		return itemDisplays;
	}

	public List<UUID> getTextDisplays() {
		return textDisplays;
	}
}
