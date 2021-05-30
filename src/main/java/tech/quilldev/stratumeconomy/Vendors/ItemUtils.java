package tech.quilldev.stratumeconomy.Vendors;

import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;

import java.util.Collections;
import java.util.Set;

public class ItemUtils {

    /**
     * Item soft equals checks that items have the same material type, lore, and keys
     *
     * @param a material to compare
     * @param b material to compare
     * @return whether they soft equal each other
     */
    public static boolean itemSoftEquals(ItemStack a, ItemStack b) {
        if (!itemEqualsIgnoreKeys(a, b)) return false;

        //Get the keys of each
        final var aKeys = getAllKeys(a.clone());
        final var bKeys = getAllKeys(b.clone());

        return (aKeys.containsAll(bKeys) && bKeys.containsAll(aKeys));
    }

    /**
     * Item soft equals checks that items have the same material type, lore, and keys
     *
     * @param a material to compare
     * @param b material to compare
     * @return whether they soft equal each other
     */
    public static boolean itemEqualsIgnoreKeys(ItemStack a, ItemStack b) {
        final var cloneStackA = a.clone();
        final var cloneStackB = b.clone();

        //If they're not the same material return false
        if (!cloneStackA.getType().equals(cloneStackB.getType())) return false;
        final var aMeta = cloneStackA.getItemMeta();
        final var bMeta = cloneStackB.getItemMeta();

        //If they don't both have the same lore, return false
        if (!(aMeta.hasLore() == bMeta.hasLore())) return false;

        //If they both DO have lore check for equality
        if (aMeta.hasLore() && bMeta.hasLore()) {
            final var aLore = aMeta.lore();
            final var bLore = bMeta.lore();
            assert aLore != null;
            return aLore.equals(bLore);
        }

        return true;
    }

    public static Set<NamespacedKey> getAllKeys(ItemStack item) {
        final var itemMeta = item.getItemMeta();
        if (itemMeta == null) return Collections.emptySet();
        final var itemData = itemMeta.getPersistentDataContainer();
        return itemData.getKeys();
    }
}
