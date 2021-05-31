package tech.quilldev.stratumeconomy.Events;


import net.kyori.adventure.text.Component;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import tech.quilldev.stratumeconomy.Market.EconomyManager;
import tech.quilldev.stratumeconomy.EconomyKeys;
import tech.quilldev.stratumeconomy.Market.MarketDataRetriever;
import tech.quilldev.stratumeconomy.StratumEconomy;
import tech.quilldev.stratumeconomy.Vendors.VendorHelper;

import java.util.ArrayList;

public class VendorWindowListener implements Listener {

    private final MarketDataRetriever marketDataRetriever;
    private final EconomyManager economyManager;
    private final Economy economy;

    public VendorWindowListener(EconomyManager economyManager, Plugin plugin) {
        this.marketDataRetriever = economyManager.getMarketDataRetriever();
        this.economyManager = economyManager;
        this.economy = economyManager.getEconomy();
    }

    /**
     * Event for opening the vendor window
     *
     * @param event a player interact event
     */
    @EventHandler
    public void openVendorWindowEvent(PlayerInteractAtEntityEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return; //if it's the wrong hand event, ignore it
        final var entity = event.getRightClicked();
        final var entityData = entity.getPersistentDataContainer();
        if (!entityData.has(EconomyKeys.vendorKey, PersistentDataType.BYTE_ARRAY)) return;
        if (!entityData.has(EconomyKeys.vendorInventory, PersistentDataType.BYTE_ARRAY)) return;

        //Get the data for the given vendor inventory
        final var inventoryData = StratumEconomy.serializer.deserializeItemList(
                entityData.get(EconomyKeys.vendorInventory, PersistentDataType.BYTE_ARRAY)
        );
        if (inventoryData == null) return;
        //Get the player
        final var player = event.getPlayer();
        final var vendorWindowItems = new ArrayList<ItemStack>();

        //Set prices for dynamic items from their current market value
        for (final var item : inventoryData) {
            final var marketData = marketDataRetriever.getMarketItem(item);
            if (marketData == null) continue;
            final var marketItem = economyManager.getMarketItem(marketData.getMaterial());
            if (marketItem == null) continue;
            //Create the window item
            final var windowItem = item.clone(); //get a clone of the base item to use as a window item
            final var windowItemMeta = windowItem.getItemMeta(); //get the meta for the window item
            final var windowItemData = windowItemMeta.getPersistentDataContainer(); //get the data for the window item
            windowItemData.set(
                    EconomyKeys.baseItemKey,
                    PersistentDataType.BYTE_ARRAY,
                    StratumEconomy.serializer.serializeItemStack(item)
            ); //Write the base item to the window item
            windowItem.setItemMeta(windowItemMeta); //set the meta to the new meta we just made
            VendorHelper.setPriceLore(windowItem, false); //set the price on the item
            vendorWindowItems.add(windowItem); //add the item to the items to be drawn to the window
        }

        // Create an inventory that we'll use to render the inventory
        final var vendorWindow = Bukkit.createInventory((InventoryHolder) entity, InventoryType.PLAYER, Component.text(entity.getCustomName() + "'s Shop"));
        vendorWindowItems.forEach(vendorWindow::addItem);
        player.openInventory(vendorWindow);
    }

    @EventHandler
    public void processVendorClicks(InventoryClickEvent event) {
        if (!VendorHelper.isVendorView(event.getView())) return;
        event.setCancelled(true);
        processTransaction((Player) event.getWhoClicked(), event.getCurrentItem(), event.getClick());
    }

    public void processTransaction(Player player, ItemStack clicked, ClickType clickType) {
        try {
            if (clicked == null) return;
            final var baseItem = VendorHelper.getBaseItem(clicked);
            //Get the transaction type and quantity
            final boolean buy = (
                    clickType.equals(ClickType.SHIFT_LEFT)
                            || clickType.equals(ClickType.LEFT)
                            || clickType.equals(ClickType.DOUBLE_CLICK)
                            || clickType.equals(ClickType.WINDOW_BORDER_LEFT)
            );

            final int qty = (
                    clickType.equals(ClickType.LEFT)
                            || clickType.equals(ClickType.RIGHT)
                            || clickType.equals(ClickType.DOUBLE_CLICK)
            ) ? 1 : 64;

            //Get the query item
            assert baseItem != null;
            final var queryItem = VendorHelper.getQueryCopy(baseItem);
            queryItem.setAmount(qty);

            //Get the price of the transaction
            final var marketItem = economyManager.getMarketItem(clicked.getType());
            final float price = (buy) ?
                    marketItem.getBuyPrice()
                    : marketItem.getSellPrice();

            //Get the players inventory
            final var playerInventory = player.getInventory();
            //if the player doesn't have enough money cancel the transaction
            if (buy) {
                //If the player is broke, return out
                if (economy.getBalance(player) < price) {
                    player.sendMessage("You do not have enough money to complete this transaction!");
                    return;
                }

                //Make sure the player has enough space for the given item
                playerInventory.addItem(queryItem);
                //TODO: Make sure the player has enough space before hand
            } else {
                //If we got here we're processing a sell
                if (!playerInventory.containsAtLeast(queryItem, qty)) {
                    player.sendMessage("You do not have enough items to complete this transaction!");
                    return;
                }
                //Remove the items from the inventory
                playerInventory.removeItemAnySlot(queryItem);
            }

            //Send a verification message to the player
            player.sendMessage(String.format(
                    "You %s [%s]x%s for $%s!",
                    (buy) ? "purchased" : "sold",
                    clicked.getType(),
                    qty,
                    price
            ));

            //Adjust the players balance for that transaction
            if (buy) {
                economy.withdrawPlayer(player, price);
                economyManager.incrementBuy(clicked.getType(), qty);
            } else {

                //Deposit money to the player if they sold
                economy.depositPlayer(player, price);
                economyManager.incrementSell(clicked.getType(), qty);
            }
            System.out.printf("Processed Transaction : BUY:%s, ClickType:%s | BA:%s SA:%s%n",
                    buy,
                    clickType,
                    economyManager.getMarketDataFromCache(marketItem.getMaterial()).getBuyAmount(),
                    economyManager.getMarketDataFromCache(marketItem.getMaterial()).getSellAmount()
                    );
            updateMarketWindowEvents(marketItem.getMaterial());
        } catch (Exception e) {
            e.printStackTrace();
            player.sendMessage("An error occurred while processing this transaction!");
        }

    }

    /**
     * Event for updating market windows with new prices
     */
    public void updateMarketWindowEvents(Material material) {
        economyManager.updateMaterialPrice(material);
        Bukkit.getOnlinePlayers().forEach(player -> {
            final var view = player.getOpenInventory();
            if (!VendorHelper.isVendorView(view)) return;
            if (!view.getTopInventory().contains(material)) return;
            updateDynamicValues(material, view.getTopInventory());
        });
    }

    /**
     * Update the dynamic values of an item in the inventory
     *
     * @param inventory to update the dynamic values in
     */
    public void updateDynamicValues(Material material, Inventory inventory) {
        VendorHelper.setPriceLore(inventory.getItem(inventory.first(material)), false);
    }
}
