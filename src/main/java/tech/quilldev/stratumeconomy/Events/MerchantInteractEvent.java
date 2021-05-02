package tech.quilldev.stratumeconomy.Events;

import net.kyori.adventure.text.Component;
import org.apache.commons.lang.SerializationUtils;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import tech.quilldev.stratumeconomy.MerchantAttributes;

import java.util.ArrayList;
import java.util.Collection;

public class MerchantInteractEvent implements Listener {

    @EventHandler
    public void merchantInteract(PlayerInteractEntityEvent event) {
        final var entity = event.getRightClicked();
        if (!(entity instanceof Villager)) return; //If the entity is not a villager, we don't care
        final var data = entity.getPersistentDataContainer();
        if (data.getKeys().size() <= 0) return; //if they have no special data, return
        if (!data.has(MerchantAttributes.merchantKey, PersistentDataType.INTEGER))
            return; //if they don't have the merchant key, return
        event.setCancelled(true);
        //if the name is null, return
        final var name = data.get(MerchantAttributes.merchantNameKey, PersistentDataType.STRING);
        if (name == null) return;
        final var player = event.getPlayer();

        final var gui = Bukkit.createInventory(
                player,
                9 * 4,
                Component.text(name + "'s Shop"));

        final var inventoryBytes = data.get(MerchantAttributes.merchantInventoryDataKey, PersistentDataType.BYTE_ARRAY);
        if (inventoryBytes == null) return;
        final var items = MerchantAttributes.deserializeItemList(inventoryBytes);
        if (items == null) return;
        items.forEach(gui::addItem);
        player.openInventory(gui);
    }

}
