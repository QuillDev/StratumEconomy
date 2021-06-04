package tech.quilldev.stratumeconomy.Vendors;


import com.google.inject.Inject;
import com.google.inject.Singleton;
import moe.quill.StratumCommon.Serialization.ISerializer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import tech.quilldev.stratumeconomy.Market.EconomyManager;
import tech.quilldev.stratumeconomy.EconomyKeys;
import tech.quilldev.stratumeconomy.Market.MarketItem;

import java.text.NumberFormat;
import java.util.*;
import java.util.stream.Collectors;


@Singleton
public class VendorHelper {


    //Setup money formatting stuff
    private final Locale locale = new Locale("en", "US");
    private final NumberFormat moneyFormatter = NumberFormat.getCurrencyInstance(locale);

    private final EconomyManager economyManager;
    private final ISerializer serializer;
    private final EconomyKeys economyKeys;

    @Inject
    public VendorHelper(EconomyManager economyManager, ISerializer serializer, EconomyKeys economyKeys) {
        this.economyManager = economyManager;
        this.serializer = serializer;
        this.economyKeys = economyKeys;
    }

    /**
     * Get all vendors in the world currently
     *
     * @return a list of vendors
     */
    public ArrayList<Entity> getVendors() {
        ArrayList<Entity> vendors = new ArrayList<>();

        for (final var world : Bukkit.getWorlds()) {
            for (final var entity : world.getEntities()) {
                if (!entity.getPersistentDataContainer().has(economyKeys.vendorKey, PersistentDataType.BYTE_ARRAY)) {
                    continue;
                }
                vendors.add(entity);
            }
        }
        return vendors;
    }

    public Entity getVendor(String name) {
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
    public List<String> getVendorNames() {
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
    public boolean addDynamicItem(Entity entity, MarketItem marketItem) {
        //Verify that they are a valid vendor
        if (entity == null) return false;
        final var entityData = entity.getPersistentDataContainer();
        if (!entityData.has(economyKeys.vendorKey, PersistentDataType.BYTE_ARRAY)) return false;

        //Add item and write new data to inventory
        final var itemToAdd = new ItemStack(marketItem.getMaterial());

        //Set the data keys we need
        final var itemMeta = itemToAdd.getItemMeta();
        itemMeta.getPersistentDataContainer()
                .set(economyKeys.isDynamic, PersistentDataType.BYTE_ARRAY, serializer.serializeBoolean(true));
        itemToAdd.setItemMeta(itemMeta);

        //If they don't have a vendor inventory key yet, give them one!
        if (!entityData.has(economyKeys.vendorInventory, PersistentDataType.BYTE_ARRAY)) {
            entityData.set(economyKeys.vendorInventory, PersistentDataType.BYTE_ARRAY, serializer.serializeItemList(
                    new ArrayList<>(Collections.singletonList(itemToAdd))
            ));
            return true;
        }

        //Get inventory data
        final var vendorInventoryBytes = entityData.get(economyKeys.vendorInventory, PersistentDataType.BYTE_ARRAY);
        final var vendorInventory = serializer.deserializeItemList(vendorInventoryBytes);
        if (vendorInventory == null) return false; //make sure data is good

        //Add the item to the vendor inventory
        vendorInventory.add(itemToAdd);
        entityData.set(economyKeys.vendorInventory, PersistentDataType.BYTE_ARRAY, serializer.serializeItemList(vendorInventory));
        return true;
    }

    /**
     * Set the lore for the given items buy/sell price
     *
     * @param itemStack of the item to update
     * @param keepLore  whether to keep the lore
     */
    public void setPriceLore(ItemStack itemStack, boolean keepLore) {
        final var baseItem = getBaseItem(itemStack);
        final var itemMeta = itemStack.getItemMeta();
        if (baseItem == null) return;
        final var baseItemMeta = baseItem.getItemMeta();
        final var dataContainer = baseItemMeta.getPersistentDataContainer();
        if (!dataContainer.has(economyKeys.isDynamic, PersistentDataType.BYTE_ARRAY)) {
            System.out.println("not dynamic");
            return;
        }

        final var marketItem = economyManager.getMarketItem(itemStack.getType());
        if (marketItem == null) {
            System.out.println("Couldn't find market item for type " + itemStack.getType());
            return;
        }
        //Get the lore
        final var lore = new ArrayList<Component>();
        lore.add(Component.text("Buy: ").append(Component.text(moneyFormatter.format(marketItem.getBuyPrice()))).color(TextColor.color(0xD21B)));
        lore.add(Component.text("Sell: ").append(Component.text(moneyFormatter.format(marketItem.getSellPrice()))).color(TextColor.color(0xFF5C4B)));
        //If keep lore is on we keep the lore
        if (keepLore) {
            //If there is no lore, just make it an empty list
            if (itemMeta.lore() != null) {
                lore.addAll(itemMeta.lore());
            }
        }

        itemMeta.lore(lore);
        itemStack.setItemMeta(itemMeta);
    }

    public boolean isVendorView(InventoryView view) {
        if (!view.getType().equals(InventoryType.PLAYER)) return false;
        final var holder = (Entity) view.getTopInventory().getHolder();
        if (holder == null) return false;
        return holder.getPersistentDataContainer().has(economyKeys.vendorKey, PersistentDataType.BYTE_ARRAY);
    }

    /**
     * Get the base item back from a vendor item
     *
     * @param item to get data from
     * @return the vendor item
     */
    public ItemStack getBaseItem(ItemStack item) {
        //Get the item meta
        final var itemMeta = item.getItemMeta();
        if (itemMeta == null) return null;
        //Check if the item has the correct data
        final var itemData = itemMeta.getPersistentDataContainer();
        if (!itemData.has(economyKeys.baseItemKey, PersistentDataType.BYTE_ARRAY)) return null;
        //Return the deserialized item
        return serializer.deserializeItemStack(
                itemData.get(economyKeys.baseItemKey, PersistentDataType.BYTE_ARRAY)
        );
    }

    public ItemStack getQueryCopy(ItemStack query) {
        final var item = query.clone();
        final var meta = item.getItemMeta();
        if (meta == null) return item;
        final var data = meta.getPersistentDataContainer();
        data.remove(economyKeys.isDynamic);
        data.remove(economyKeys.buyPriceKey);
        data.remove(economyKeys.sellPriceKey);
        item.setItemMeta(meta);
        return item;
    }

    public boolean removeDynamicItem(Entity vendor, Material material) {
        final var vendorData = vendor.getPersistentDataContainer();
        final var vendorInventoryBytes = vendorData.get(economyKeys.vendorInventory, PersistentDataType.BYTE_ARRAY);
        final var vendorInventory = serializer.deserializeItemList(vendorInventoryBytes);
        if (vendorInventory == null) return false;

        for (final var item : vendorInventory) {
            if (!item.getType().equals(material)) continue;
            final var itemMeta = item.getItemMeta();
            if (itemMeta == null) continue;
            final var itemData = itemMeta.getPersistentDataContainer();
            if (!itemData.has(economyKeys.isDynamic, PersistentDataType.BYTE_ARRAY)) continue;
            vendorInventory.remove(item);
            vendorData.set(economyKeys.vendorInventory, PersistentDataType.BYTE_ARRAY, serializer.serializeItemList(vendorInventory));
            return true;
        }

        return true;
    }
}
