package tech.quilldev.stratumeconomy.Events;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import tech.quilldev.stratumeconomy.EconomyKeys;
import tech.quilldev.stratumeconomy.Market.MarketDataRetriever;
import tech.quilldev.stratumeconomy.Serialization.StratumSerialization;

import java.util.ArrayList;

public class OpenVendorWindow implements Listener {

    public MarketDataRetriever marketDataRetriever;

    public OpenVendorWindow(MarketDataRetriever marketDataRetriever, Plugin plugin) {
        this.marketDataRetriever = marketDataRetriever;

        Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, this::updateMarketWindowEvents, 20L, 100L);
    }

    /**
     * Event for opening the vendor window
     *
     * @param event a player interact event
     */
    @EventHandler
    public void openVendorWindowEvent(PlayerInteractAtEntityEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;
        final var entity = event.getRightClicked();
        final var entityData = entity.getPersistentDataContainer();
        if (!entityData.has(EconomyKeys.vendorKey, PersistentDataType.BYTE_ARRAY)) return;
        if (!entityData.has(EconomyKeys.vendorInventory, PersistentDataType.BYTE_ARRAY)) return;

        //Get the data for the given vendor inventory
        final var inventoryData = StratumSerialization.deserializeItemList(
                entityData.get(EconomyKeys.vendorInventory, PersistentDataType.BYTE_ARRAY)
        );
        if (inventoryData == null) return;

        inventoryData.forEach(System.out::println);
        //Get the player
        final var player = event.getPlayer();
        final var vendorWindowItems = new ArrayList<ItemStack>();

        //Set prices for dynamic items from their current market value
        for (final var item : inventoryData) {
            final var marketData = marketDataRetriever.getMarketItem(item);
            if (marketData == null) continue;
            //Create the window item
            final var windowItem = item.clone();
            setBuySellLore(windowItem, marketData.getBuyPrice(), marketData.getSellPrice(), false);
            vendorWindowItems.add(windowItem);
        }

        //TODO: RENDER CUSTOM ITEMS IN THE VENDOR WINDOW
        final var vendorWindow = Bukkit.createInventory(player, InventoryType.PLAYER, Component.text(entity.getCustomName() + "'s Shop"));
        vendorWindowItems.forEach(vendorWindow::addItem);
        player.openInventory(vendorWindow);
    }

    /**
     * Event for updating market windows with new prices
     */
    public void updateMarketWindowEvents() {
        Bukkit.getOnlinePlayers().forEach(player -> {
            final var view = player.getOpenInventory();
            if (!view.getType().equals(InventoryType.PLAYER)) return;
            final var title = view.title();
            final var content = ((TextComponent) title).content();
            if (!content.contains("Shop")) return;
            updateDynamicValues(view.getTopInventory());
        });
    }

    /**
     * Update the dynamic values of an item in the inventory
     *
     * @param inventory to update the dynamic values in
     */
    public void updateDynamicValues(Inventory inventory) {
        for (final var slot : inventory.getContents()) {
            if (slot == null) continue;
            final var marketData = marketDataRetriever.getMarketItem(slot);
            if (marketData == null) continue;

            //Create the window item
            setBuySellLore(slot, marketData.getBuyPrice(), marketData.getSellPrice(), false);
        }
    }

    /**
     * Set the lore for the given items buy/sell price
     *
     * @param itemStack of the item to update
     * @param buy       price of teh item
     * @param sell      price of the item
     */
    public void setBuySellLore(ItemStack itemStack, double buy, double sell, boolean keepLore) {
        final var meta = itemStack.getItemMeta();
        //Get the lore
        final var lore = new ArrayList<Component>();
        lore.add(Component.text("Buy: ").append(Component.text(buy)).color(TextColor.color(0xD21B)));
        lore.add(Component.text("Sell: ").append(Component.text(sell)).color(TextColor.color(0xFF5C4B)));

        //If keep lore is on we keep the lore
        if (keepLore) {
            //If there is no lore, just make it an empty list
            if (meta.lore() != null) {
                lore.addAll(meta.lore());
            }
        }

        meta.lore(lore);
        itemStack.setItemMeta(meta);
    }
}
