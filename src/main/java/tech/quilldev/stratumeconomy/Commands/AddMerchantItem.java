package tech.quilldev.stratumeconomy.Commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import tech.quilldev.stratumeconomy.MerchantAttributes;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;

public class AddMerchantItem implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) return true;
        if (args.length < 3) return true;
        final var player = ((Player) sender).getPlayer();
        if (player == null) return true;

        final var item = player.getInventory().getItemInMainHand();
        if (item.getType().equals(Material.AIR)) return true;

        final var name = args[0];
        final var buyPrice = Float.parseFloat(args[1]);
        final var sellPrice = Float.parseFloat(args[2]);

        if (Float.isNaN(buyPrice) || Float.isNaN(sellPrice)) return true;
        final var shopItem = new ItemStack(item);
        final var shopMeta = shopItem.getItemMeta();

        //Set the item data keys
        final var shopData = shopMeta.getPersistentDataContainer();
        shopData.set(MerchantAttributes.itemBuyKey, PersistentDataType.FLOAT, buyPrice);
        shopData.set(MerchantAttributes.itemSellKey, PersistentDataType.FLOAT, sellPrice);
        shopData.set(MerchantAttributes.itemDataKey, PersistentDataType.BYTE_ARRAY, item.serializeAsBytes());

        final var shopLore = new ArrayList<Component>(Arrays.asList(
                Component.text("[LEFT] Buy:")
                        .append(Component.space())
                        .append(Component.text(formatCurrency(buyPrice)))
                        .color(TextColor.color(0x12D22A)),
                Component.text("[RIGHT] Sell:")
                        .append(Component.space())
                        .append(Component.text(formatCurrency(sellPrice)))
                        .color(TextColor.color(0xFF4836)))
        );

        if (shopMeta.hasLore()) {
            shopLore.add(Component.text("Item Attributes").style(Style.style(TextDecoration.BOLD)));
            shopLore.addAll(shopItem.lore());
        }

        shopMeta.lore(shopLore);
        shopItem.setItemMeta(shopMeta);
        MerchantAttributes.addMerchantItem(name, shopItem);
        return false;
    }

    private String formatCurrency(float value) {
        var currencyString = NumberFormat.getCurrencyInstance().format(value);
        return currencyString.replaceAll("\\.00", "");
    }
}
