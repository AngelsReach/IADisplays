package com.vergilprime.iadisplays;

import com.vergilprime.iadisplays.controllers.DisplaysController;
import com.vergilprime.iadisplays.listeners.PlayerEvents;
import dev.lone.itemsadder.api.Events.ItemsAdderLoadDataEvent;
import org.bukkit.event.EventHandler;
import org.mineacademy.fo.plugin.SimplePlugin;

public final class IADisplays extends SimplePlugin {
	public DisplaysController displaysController;

	@Override
	public void onPluginStart() {
		// Plugin startup logic
	}

	@EventHandler
	public void onItemsAdderLoadDataEvent(ItemsAdderLoadDataEvent event) {
		displaysController = new DisplaysController(this);
		registerEvents(new PlayerEvents(this));
	}
}
