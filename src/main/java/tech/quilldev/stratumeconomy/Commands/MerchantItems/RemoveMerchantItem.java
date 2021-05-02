package tech.quilldev.stratumeconomy.Commands.MerchantItems;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import tech.quilldev.stratumeconomy.MerchantAttributes;

public class RemoveMerchantItem implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) return true;
        if (!(args.length == 2)) return true;

        //get the player
        final var player = ((Player) sender).getPlayer();
        if (player == null) return true;

        //parse the arguments
        final var name = args[0];
        final var index = Integer.parseInt(args[1]);
        if (Float.isNaN(index)) return true;

        // Get the merchant
        final var success = MerchantAttributes.removeItem(name, index - 1);
        player.sendMessage((success ? "Succeeded" : "Failed") + " at removing item at index " + index + ".");
        return success;
    }
}
