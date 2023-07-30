package com.vergilprime.iadisplays.controllers;

import com.vergilprime.iadisplays.IADisplays;
import com.vergilprime.iadisplays.models.Display;
import dev.lone.itemsadder.api.CustomStack;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class DisplaysController {
	private final IADisplays plugin;
	private final YamlConfiguration displaysYml = new YamlConfiguration();
	private final HashMap<UUID, Display> displays = new HashMap<>();
	private final List<UUID> toBeDeleted = new ArrayList<>();


	public DisplaysController(IADisplays plugin) {
		this.plugin = plugin;
		loadDisplays();
	}

	public ConfigurationSection getItemDisplayConfig(ItemStack stack) {
		CustomStack customStack = CustomStack.byItemStack(stack);
		if (customStack == null) {
			return null;
		}
		String namespacedId = customStack.getNamespacedID();
		String itemname = namespacedId.split(":", 2)[1];
		ConfigurationSection config = customStack.getConfig();
		if (config == null) {
			return null;
		}

		ConfigurationSection items = config.getConfigurationSection("items");
		if (items == null) {
			return null;
		}

		ConfigurationSection itemconfig = items.getConfigurationSection(itemname);
		if (itemconfig == null) {
			return null;
		}

		ConfigurationSection behaviours = itemconfig.getConfigurationSection("behaviours");
		if (behaviours == null) {
			return null;
		}

		ConfigurationSection iadisplay = behaviours.getConfigurationSection("iadisplay");
		if (iadisplay == null) {
			return null;
		}

		return iadisplay;
	}


	public boolean isDisplay(ItemStack stack) {
		CustomStack customStack = CustomStack.byItemStack(stack);
		if (customStack == null) {
			return false;
		}
		String namespacedId = customStack.getNamespacedID();
		String itemname = namespacedId.split(":", 1)[1];

		ConfigurationSection config = customStack.getConfig();
		if (config == null) {
			return false;
		}

		ConfigurationSection items = config.getConfigurationSection("items");
		if (items == null) {
			return false;
		}

		ConfigurationSection itemconfig = items.getConfigurationSection(itemname);
		if (itemconfig == null) {
			return false;
		}

		ConfigurationSection behaviours = itemconfig.getConfigurationSection("behaviors");
		if (behaviours == null) {
			return false;
		}

		ConfigurationSection iadisplay = behaviours.getConfigurationSection("iadisplay");
		if (iadisplay == null) {
			return false;
		}

		return true;
	}

	public boolean isDisplay(ItemDisplay itemDisplay) {
		ItemStack item = itemDisplay.getItemStack();
		if (item == null) {
			return false;
		}
		return isDisplay(item);
	}

	public boolean isDisplay(Interaction interaction) {
		return (displays.containsKey(interaction.getUniqueId()));
	}

	public boolean placeDisplay(Player player, Location location, ConfigurationSection itemConfig, ItemStack customStack, BlockFace blockFace) {
		ItemStack item = customStack;
		String world = location.getWorld().getName();
		List<UUID> itemDisplays = null;
		List<UUID> textDisplays = null;
		ConfigurationSection conf = null;
		if (blockFace == BlockFace.DOWN) {
			conf = itemConfig.getConfigurationSection("floor");
		} else if (blockFace == BlockFace.UP) {
			conf = itemConfig.getConfigurationSection("ceiling");
		}
		if (conf != null) {
			// Set the rotation of the item to the opposite of the player's rotation
			int rotationSnap = 90; // Default value
			if (conf.isInt("rotation_snap")) {
				rotationSnap = conf.getInt("rotation_snap");
			}
			int rotation = (int) Math.round((player.getLocation().getYaw() + 180) / rotationSnap) * rotationSnap;
			// If the configuration contains an entry for "snapdegrees", snap the rotation to the nearest multiple of that number
			// Spawn the item and store it so we can modify it immediately after.
			Double hitboxWidth = 0.7;
			Double hitboxHeight = 1.0;
			Location hitboxLocation = location.clone();
			if (conf.contains("hitbox")) {
				if (conf.getConfigurationSection("hitbox").isInt("yoffset") || conf.getConfigurationSection("hitbox").isDouble("yoffset"))
					hitboxLocation.add(0, conf.getConfigurationSection("hitbox").getDouble("y_offset"), 0);
				if (conf.getConfigurationSection("hitbox").isInt("width") || conf.getConfigurationSection("hitbox").isDouble("width"))
					hitboxWidth = (Double) conf.getConfigurationSection("hitbox").getDouble("width");
				if (conf.getConfigurationSection("hitbox").isInt("height") || conf.getConfigurationSection("hitbox").isDouble("height"))
					hitboxHeight = (Double) conf.getConfigurationSection("hitbox").getDouble("height");
			}
			Interaction hitboxEntity = (Interaction) location.getWorld().spawnEntity(hitboxLocation, EntityType.INTERACTION);
			UUID hitbox = hitboxEntity.getUniqueId();
			if (conf.isList("item_displays")) {
				List<ConfigurationSection> itemDisplayConfigs = (List<ConfigurationSection>) conf.getList("item_displays");
				itemDisplayConfigs.forEach(itemDisplayConfig -> {
					Location itemLocation = location.clone();
					if (itemDisplayConfig.isDouble("right_offset")) {
						//TODO: This needs to be relative to the direction of the overall display being placed
						itemLocation.add(itemDisplayConfig.getDouble("right_offset"), 0, 0);
					}
					if (itemDisplayConfig.isDouble("y_offset")) {
						itemLocation.add(0, itemDisplayConfig.getDouble("y_offset"), 0);
					}
					if (itemDisplayConfig.isDouble("back_offset")) {
						//TODO: This needs to be relative to the direction of the overall display being placed
						itemLocation.add(0, 0, itemDisplayConfig.getDouble("back_offset"));
					}
					CustomStack displayItem;
					if (itemDisplayConfig.isString("item")) {
						String itemName = itemDisplayConfig.getString("item");
						displayItem = CustomStack.getInstance(itemName);
						if (displayItem == null) {
							player.sendMessage(ChatColor.RED + "The item name " + itemName + " is invalid.");
						}

					} else {
						displayItem = CustomStack.byItemStack(item);
					}
					if (displayItem != null) {
						itemLocation.setYaw(rotation);
						ItemDisplay itemDisplay = (ItemDisplay) location.getWorld().spawnEntity(itemLocation, EntityType.ITEM_DISPLAY);
						itemDisplay.setItemStack(displayItem.getItemStack());
						itemDisplay.setPersistent(true);
						itemDisplay.setInvulnerable(true);
						itemDisplays.add(itemDisplay.getUniqueId());
					}
				});
			}
			if (conf.isList("text_displays")) {
				List<ConfigurationSection> textDisplayConfigs = (List<ConfigurationSection>) conf.getList("text_displays");
				textDisplayConfigs.forEach(textDisplayConfig -> {
					Location textLocation = location.clone();
					if (textDisplayConfig.isDouble("right_offset")) {
						//TODO: This needs to be relative to the direction of the overall display being placed
						textLocation.add(textDisplayConfig.getDouble("right_offset"), 0, 0);
					}
					if (textDisplayConfig.isDouble("y_offset")) {
						textLocation.add(0, textDisplayConfig.getDouble("y_offset"), 0);
					}
					if (textDisplayConfig.isDouble("back_offset")) {
						//TODO: This needs to be relative to the direction of the overall display being placed
						textLocation.add(0, 0, textDisplayConfig.getDouble("back_offset"));
					}
					String text = null;
					if (textDisplayConfig.isString("text")) {
						text = textDisplayConfig.getString("text");
						textLocation.setYaw(rotation);
						TextDisplay textDisplay = (TextDisplay) location.getWorld().spawnEntity(textLocation, EntityType.TEXT_DISPLAY);
						textDisplay.setText(text);
						textDisplay.setPersistent(true);
						textDisplay.setInvulnerable(true);
						textDisplays.add(textDisplay.getUniqueId());
					} else {
						player.sendMessage(ChatColor.RED + "The text must be defined for all text objects.");
					}
				});
			}

			displays.put(hitbox, new Display(world, item, hitbox, itemDisplays, textDisplays));
			saveDisplay(hitbox);
			return true;
		}
		return false;
	}

	public void breakDisplay(Interaction interaction) {
		UUID uuid = interaction.getUniqueId();
		Display display = displays.get(uuid);
		if (display != null) {
			ItemStack itemstack = display.getItem();
			Location location = interaction.getLocation();
			World world = location.getWorld();
			world.dropItem(location, itemstack);

			removeEntities(world, display.getItemDisplays(), ItemDisplay.class);
			removeEntities(world, display.getTextDisplays(), TextDisplay.class);

			if (!removeEntity(world, uuid, Interaction.class)) {
				toBeDeleted.add(uuid);
			}

			removeSavedDisplay(uuid);
		}
	}

	private boolean removeEntity(World world, UUID uuid, Class<? extends Entity> _class) {
		for (Entity entity : world.getEntitiesByClass(_class)) {
			if (entity.getUniqueId().equals(uuid)) {
				entity.remove();
				return true;
			}
		}
		return false;
	}

	private void removeEntities(World world, List<UUID> uuids, Class<? extends Entity> _class) {
		Iterator<UUID> iterator = uuids.iterator();
		while (iterator.hasNext()) {
			UUID uuid = iterator.next();
			if (removeEntity(world, uuid, _class)) {
				iterator.remove();
			} else {
				toBeDeleted.add(uuid);
			}
		}
	}


	public void loadDisplays() {
		plugin.saveResource("displays.yml", false);
		String path = plugin.getDataFolder() + "/displays.yml";
		try {
			displaysYml.load(path);
			displays.clear();
			for (String uuidString : displaysYml.getKeys(false)) {
				UUID uuid = UUID.fromString(uuidString);
				ConfigurationSection displayConfig = displaysYml.getConfigurationSection(uuidString);
				assert displayConfig != null;
				Display display = Display.fromSave(displayConfig);
				displays.put(uuid, display);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	void saveDisplay(UUID uuid) {
		String path = plugin.getDataFolder() + "/displays.yml";
		try {
			displaysYml.load(path);
			Display display = displays.get(uuid);
			ConfigurationSection displayConfig = display.toSave();
			displaysYml.set(uuid.toString(), displayConfig);
			displaysYml.save(path);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void removeSavedDisplay(UUID uuid) {
		displays.remove(uuid);
		String path = plugin.getDataFolder() + "/displays.yml";
		try {
			displaysYml.load(path);
			displaysYml.set(uuid.toString(), null);
			displaysYml.save(path);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
