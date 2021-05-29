package tech.quilldev.stratumeconomy.Vendors;

import net.kyori.adventure.text.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import tech.quilldev.stratumeconomy.EconomyKeys;
import tech.quilldev.stratumeconomy.Market.MarketItem;
import tech.quilldev.stratumeconomy.Serialization.StratumSerialization;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;


//TODO: This is prob very slow, if it causes issues come back to it
public class VendorHelper {

    /**
     * Get all vendors in the world currently
     *
     * @return a list of vendors
     */
    public static ArrayList<Entity> getVendors() {
        ArrayList<Entity> vendors = new ArrayList<>();

        for (final var world : Bukkit.getWorlds()) {
            for (final var entity : world.getEntities()) {
                if (!entity.getPersistentDataContainer().has(EconomyKeys.vendorKey, PersistentDataType.BYTE_ARRAY)) {
                    continue;
                }
                vendors.add(entity);
            }
        }
        return vendors;
    }

    public static Entity getVendor(String name) {
        //Find the vendor we're looking for
        for (final var vendor : getVendors()) {
            final var nameComponent = vendor.customName();
            if (nameComponent == null) continue;
            if (!((TextComponent) nameComponent).content().equalsIgnoreCase(name)) continue;
            return vendor;
        }

        return null;
    }

    /**
     * Get the names of all vendors currently on the server
     *
     * @return the list of vendor names
     */
    public static List<String> getVendorNames() {
        return getVendors().stream()
                .map(vendor -> {
                    final var vendorNameComponent = vendor.customName();
                    if (vendorNameComponent == null) return null;
                    return ((TextComponent) vendorNameComponent).content();
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * Add a dynamic market item to the vendor
     *
     * @param entity to assign the item to
     * @return whether the addition was successful or not
     */
    public static boolean addDynamicItem(Entity entity, MarketItem marketItem) {
        //Verify that they are a valid vendor
        if (entity == null) return false;
        final var entityData = entity.getPersistentDataContainer();
        if (!entityData.has(EconomyKeys.vendorKey, PersistentDataType.BYTE_ARRAY)) return false;

        //Add item and write new data to inventory
        final var itemToAdd = new ItemStack(marketItem.getMaterial());

        //Set the data keys we need
        final var itemMeta = itemToAdd.getItemMeta();
        itemMeta.getPersistentDataContainer()
                .set(EconomyKeys.isDynamic, PersistentDataType.BYTE_ARRAY, StratumSerialization.serializeBoolean(true));
        itemToAdd.setItemMeta(itemMeta);

        //If they don't have a vendor inventory key yet, give them one!
        if (!entityData.has(EconomyKeys.vendorInventory, PersistentDataType.BYTE_ARRAY)) {
            entityData.set(EconomyKeys.vendorInventory, PersistentDataType.BYTE_ARRAY, StratumSerialization.serializeItemList(
                    new ArrayList<>(Collections.singletonList(itemToAdd))
            ));
            return true;
        }

        //Get inventory data
        final var vendorInventoryBytes = entityData.get(EconomyKeys.vendorKey, PersistentDataType.BYTE_ARRAY);
        final var vendorInventory = StratumSerialization.deserializeItemList(vendorInventoryBytes);
        if (vendorInventory == null) return false; //make sure data is good

        //Add the item to the vendor inventory
        vendorInventory.add(itemToAdd);
        entityData.set(EconomyKeys.vendorKey, PersistentDataType.BYTE_ARRAY, StratumSerialization.serializeItemList(vendorInventory));
        return true;
    }
}
