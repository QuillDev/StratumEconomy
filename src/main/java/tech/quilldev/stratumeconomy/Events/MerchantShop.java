package tech.quilldev.stratumeconomy.Events;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextColor;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import tech.quilldev.stratumeconomy.MerchantAttributes;

import java.util.Objects;

public class MerchantShop implements Listener {
    private final Economy economy;

    public MerchantShop(Economy economy) {
        this.economy = economy;
    }

    @EventHandler
    public void onShopClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        final var player = ((Player) event.getWhoClicked()).getPlayer();
        if (player == null) return;
        final var inventory = event.getClickedInventory();
        if (inventory == null) return;
        if (!inventory.getType().equals(InventoryType.CHEST)) return; //if it's not a chest leave
        final var inventoryName = ((TextComponent) event.getView().title()).content();
        if (!inventoryName.matches("(.*'s Shop)")) return;
        event.setCancelled(true); //Cancel random click events
        if (!event.getClickedInventory().equals(event.getView().getTopInventory())) return;

        //Get the clicked item & it's properties
        final var shopItem = event.getCurrentItem();
        if (shopItem == null) return;
        if (!shopItem.hasItemMeta()) return;
        final var data = shopItem.getItemMeta().getPersistentDataContainer();
        final var buyPrice = data.get(MerchantAttributes.itemBuyKey, PersistentDataType.FLOAT);
        final var sellPrice = data.get(MerchantAttributes.itemSellKey, PersistentDataType.FLOAT);
        final var itemBytes = data.get(MerchantAttributes.itemDataKey, PersistentDataType.BYTE_ARRAY);

        //Check if the player can buy the item
        if (buyPrice == null || itemBytes == null || sellPrice == null) return;
        final var item = ItemStack.deserializeBytes(itemBytes);
        final var balance = economy.getBalance(player); // get the balance of the player

        //get the player inventory
        final var playerInventory = player.getInventory();

        //Get the name of the item to use in messages
        var itemName = item.getItemMeta().displayName();
        if (itemName == null) {
            itemName = Component.text(Objects.requireNonNull(item.getI18NDisplayName()));
        }

        // PROCESS SELLING
        if (event.isRightClick()) {
            //Left click means selling the item
            if (!player.getInventory().contains(item)) {
                player.sendMessage(Component.text("You can't sell an item you don't have!")
                        .color(TextColor.color(0xF92D2D)));
                return;
            }

            //If we got here, they can sell it
            playerInventory.removeItem(item); //remove the item from their inventory
            economy.depositPlayer(player, sellPrice); //pay the player
            player.sendMessage(
                    Component.text("You sold your")
                            .append(Component.space())
                            .append(itemName)
                            .append(Component.space())
                            .append(Component.text("for"))
                            .append(Component.space())
                            .append(Component.text(sellPrice))
                            .append(Component.text("!"))
                            .color(TextColor.color(0x12D22A))
            );
        }

        //PROCESS BUYING
        if (event.isLeftClick()) {
            //If they cannot afford it, return out
            if (balance < buyPrice) {
                player.sendMessage(Component.text("You cannot afford this item.").color(TextColor.color(0xF92D2D)));
                return;
            }

            //Prevent players with full inventories from buying items
            if (player.getInventory().firstEmpty() == -1) {
                player.sendMessage(
                        Component.text("You cannot buy items with a full inventory!")
                                .color(TextColor.color(0xF92D2D)));
                return;
            }

            player.getInventory().addItem(item); //give them the item
            economy.withdrawPlayer(player, buyPrice); //take the money from the player
            player.sendMessage(
                    Component.text("You purchased")
                            .append(Component.space())
                            .append(itemName)
                            .append(Component.text("!"))
                            .color(TextColor.color(0x12D22A))
            );
        }
    }

    @EventHandler
    public void onShopDeposit(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        final var player = ((Player) event.getWhoClicked()).getPlayer();
        if (player == null) return;
        final var inventory = event.getInventory();
        if (!inventory.getType().equals(InventoryType.CHEST)) return; //if it's not a chest leave
        final var inventoryName = ((TextComponent) event.getView().title()).content();
        if (!inventoryName.matches("(.*'s Shop)")) return;
        event.setCancelled(true);
    }
}
