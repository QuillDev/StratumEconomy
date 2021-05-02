package tech.quilldev.stratumeconomy.Commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import tech.quilldev.stratumeconomy.MerchantAttributes;

public class SpawnMerchant implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) return true;
        if (args.length <= 0) return true;

        //Get the player
        final var player = ((Player) sender).getPlayer();
        if (player == null) return true;

        //Save the merchant's name
        final var name = args[0];

        if (name.length() <= 0) return true; //make sure the name is valid
        MerchantAttributes.createMerchant(name, player);
        return true;
    }
}
