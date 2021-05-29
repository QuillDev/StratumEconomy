package tech.quilldev.stratumeconomy.Commands.MarketCommands;

import org.bukkit.command.CommandExecutor;
import tech.quilldev.stratumeconomy.Market.MarketDataRetriever;

public abstract class MarketCommand implements CommandExecutor {

    protected final MarketDataRetriever marketDataRetriever;

    public MarketCommand(MarketDataRetriever marketDataRetriever) {
        this.marketDataRetriever = marketDataRetriever;
    }
}
