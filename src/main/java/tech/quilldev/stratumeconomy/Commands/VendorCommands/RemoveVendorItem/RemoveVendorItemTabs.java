package tech.quilldev.stratumeconomy.Commands.VendorCommands.RemoveVendorItem;

import com.google.inject.Inject;
import moe.quill.StratumCommon.Serialization.ISerializer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tech.quilldev.stratumeconomy.EconomyKeys;
import tech.quilldev.stratumeconomy.StratumEconomy;
import tech.quilldev.stratumeconomy.Vendors.VendorHelper;

import java.util.List;
import java.util.stream.Collectors;

public class RemoveVendorItemTabs implements TabCompleter {

    private final VendorHelper vendorHelper;
    private final ISerializer serializer;
    private final EconomyKeys economyKeys;

    @Inject
    public RemoveVendorItemTabs(VendorHelper vendorHelper, ISerializer serializer, EconomyKeys economyKeys) {
        this.vendorHelper = vendorHelper;
        this.serializer = serializer;
        this.economyKeys = economyKeys;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) {
            return vendorHelper.getVendorNames().stream().filter(name -> name.contains(args[0])).collect(Collectors.toList());
        }
        if (args.length == 2) {
            final var vendor = vendorHelper.getVendor(args[0]);
            if (vendor == null) return null;
            final var vendorData = vendor.getPersistentDataContainer();
            if (!vendorData.has(economyKeys.vendorInventory, PersistentDataType.BYTE_ARRAY)) return null;
            final var inventoryData = serializer.deserializeItemList(vendorData.get(economyKeys.vendorInventory, PersistentDataType.BYTE_ARRAY));
            if (inventoryData == null) return null;
            return inventoryData.stream().map(itm -> itm.getType().name().toUpperCase()).filter(mat -> mat.contains(args[1])).collect(Collectors.toList());
        }
        return null;
    }

}
