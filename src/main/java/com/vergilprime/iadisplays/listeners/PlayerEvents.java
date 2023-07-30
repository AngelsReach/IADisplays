package com.vergilprime.iadisplays.listeners;

import com.vergilprime.iadisplays.IADisplays;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Interaction;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class PlayerEvents implements Listener {
	IADisplays plugin;

	public PlayerEvents(IADisplays plugin) {
		this.plugin = plugin;
	}

	@EventHandler
	public void onPlayerInteractEvent(PlayerInteractEvent event) {
		Player player = event.getPlayer();
		// If the player is crouching and right clicking a block
		if (event.useInteractedBlock() == Event.Result.ALLOW && event.getAction() == Action.RIGHT_CLICK_BLOCK && player.isSneaking()) {
			// If the player is holding a display item
			ItemStack hand = event.getItem();
			ConfigurationSection itemConfig = plugin.displaysController.getItemDisplayConfig(hand);
			if (itemConfig != null) {
				// Get the block where the display will go
				Location location = event.getClickedBlock().getRelative(event.getBlockFace()).getLocation();

				boolean success = false;
				success = plugin.displaysController.placeDisplay(player, location, itemConfig, hand, event.getBlockFace());

				if (success) {
					event.setCancelled(true);
				}
			}
		}
	}

	@EventHandler
	public void onPlayerInteractEntityEvent(PlayerInteractEntityEvent event) {
		Player player = event.getPlayer();
		if (
				!event.isCancelled() &&
						event.getRightClicked() instanceof Interaction &&
						plugin.displaysController.isDisplay((Interaction) event.getRightClicked()) &&
						player.isSneaking() &&
						(player.getInventory().getItemInMainHand().getType() == Material.AIR || player.getInventory().getItemInMainHand() == null)
		) {
			plugin.displaysController.breakDisplay((Interaction) event.getRightClicked());
		}
	}
}
