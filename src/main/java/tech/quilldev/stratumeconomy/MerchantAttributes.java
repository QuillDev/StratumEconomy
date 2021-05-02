package tech.quilldev.stratumeconomy;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.apache.commons.lang.SerializationUtils;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.Objects;
import java.util.stream.Collectors;

public class MerchantAttributes {

    public static ArrayList<Villager> merchants = new ArrayList<>();

    //Public Keys for merchants
    public static NamespacedKey merchantKey = null;
    public static NamespacedKey merchantNameKey = null;
    public static NamespacedKey merchantInventoryDataKey = null;

    //Public keys for merchant items
    public static NamespacedKey itemBuyKey = null;
    public static NamespacedKey itemSellKey = null;
    public static NamespacedKey itemDataKey = null;

    public static void init(Plugin plugin) {
        //Merchant Keys
        merchantKey = new NamespacedKey(plugin, "merchant");
        merchantNameKey = new NamespacedKey(plugin, "merchant_name");
        merchantInventoryDataKey = new NamespacedKey(plugin, "merchant_inventory_data");

        //Item Keys
        itemBuyKey = new NamespacedKey(plugin, "item_buy_price");
        itemSellKey = new NamespacedKey(plugin, "item_sell_price");
        itemDataKey = new NamespacedKey(plugin, "item_shop_data");

        final var villagers =
                Bukkit.getServer()
                        .getWorlds()
                        .stream()
                        .map(world -> world
                                .getEntities()
                                .stream()
                                .filter(MerchantAttributes::isMerchant)
                                .map(entity -> (Villager) entity)
                                .collect(Collectors.toList())
                        )
                        .reduce(((villagers1, villagers2) -> {
                            villagers1.addAll(villagers2);
                            return villagers1;
                        }))
                        .orElse(null);

        if (villagers == null) return;
        merchants.addAll(new ArrayList<>(villagers)
        );
    }

    public static void createMerchant(String name, Player player) {

        //If the name is already taken, don't create the villager
        if (merchants.stream().anyMatch(id -> Objects.equals(id.getCustomName(), name))) {
            player.sendMessage(
                    Component.text("The merchant name")
                            .append(Component.space())
                            .append(Component.text(name))
                            .append(Component.space())
                            .append(Component.text("is already taken."))
                            .color(TextColor.color(0xAC2934))
            );
            return;
        }

        //Get the player location
        final var location = player.getLocation();
        final var merchant = (Villager) player.getWorld().spawnEntity(location, EntityType.VILLAGER);
        merchant.setCustomName(name);
        merchant.setAI(false);
        merchant.setCustomNameVisible(true);
        merchant.setInvulnerable(true);
        merchant.setPersistent(true);

        final var inventoryBytes = serializeItemList(new ArrayList<>());
        final var data = merchant.getPersistentDataContainer();
        data.set(MerchantAttributes.merchantKey, PersistentDataType.INTEGER, 1);
        data.set(MerchantAttributes.merchantNameKey, PersistentDataType.STRING, name);
        data.set(MerchantAttributes.merchantInventoryDataKey, PersistentDataType.BYTE_ARRAY, inventoryBytes);
        merchant.setProfession(Villager.Profession.CARTOGRAPHER);

        player.sendMessage(
                Component.text("A merchant with the name")
                        .append(Component.space())
                        .append(Component.text(name))
                        .append(Component.space())
                        .append(Component.text("has been created!"))
                        .color(TextColor.color(0x11AC52))
        );

        merchants.add(merchant);
    }

    public static boolean isMerchant(Entity entity) {
        if (!(entity instanceof Villager)) return false;
        final var data = entity.getPersistentDataContainer();
        if (data.getKeys().size() <= 0) return false;
        return data.has(MerchantAttributes.merchantKey, PersistentDataType.INTEGER);
    }

    /**
     * Get a merchant from the given name
     *
     * @param name of the merchant
     * @return a villager (the merchant)
     */
    public static Villager getMerchant(String name) {
        return merchants.stream()
                .filter(villager -> Objects.requireNonNull(villager.getCustomName()).equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }

    /**
     * Get the items for the given merchant
     *
     * @param name of the merchant
     * @return the items to write
     */
    public static ArrayList<ItemStack> readMerchantItems(String name) {
        final var merchant = getMerchant(name);
        if (merchant == null) return null;
        final var invBytes = merchant.getPersistentDataContainer().get(merchantInventoryDataKey, PersistentDataType.BYTE_ARRAY);
        if (invBytes == null || invBytes.length == 0) return null;
        return deserializeItemList(invBytes);
    }

    /**
     * Add an item to the merchant
     *
     * @param name of the merchant to add the item to
     * @param item to add
     * @return whether adding the item was successful
     */
    public static boolean addMerchantItem(String name, ItemStack item) {
        final var items = readMerchantItems(name);
        System.out.println("null");
        if (items == null) return false;
        System.out.println("not null");
        items.add(item);
        return writeMerchantItems(name, items);
    }

    /**
     * Write items to the given merchant
     *
     * @param name  of the merchant
     * @param items to write to the merchant
     * @return whether the write was successful
     */
    public static boolean writeMerchantItems(String name, ArrayList<ItemStack> items) {
        final var merchant = getMerchant(name);
        if (merchant == null) return false;
        final var itemBytes = serializeItemList(items);
        merchant.getPersistentDataContainer().set(merchantInventoryDataKey, PersistentDataType.BYTE_ARRAY, itemBytes);
        return true;
    }

    /**
     * Serialize a list of items to a byte array
     *
     * @param itemStacks to serialize
     * @return the serialized item list
     */
    public static byte[] serializeItemList(ArrayList<ItemStack> itemStacks) {
        final var arrayOfItemByteArrays = itemStacks
                .stream()
                .map(ItemStack::serializeAsBytes)
                .collect(Collectors.toCollection(ArrayList::new));
        return SerializationUtils.serialize(arrayOfItemByteArrays);
    }

    /**
     * Deserialize the item list
     *
     * @param bytes to deserialize
     * @return the array list we got from the bytes
     */
    public static ArrayList<ItemStack> deserializeItemList(byte[] bytes) {
        final var byteList = (ArrayList<byte[]>) SerializationUtils.deserialize(bytes);
        if (byteList == null) return null;
        return byteList
                .stream()
                .map(ItemStack::deserializeBytes)
                .collect(Collectors.toCollection(ArrayList::new));
    }
}