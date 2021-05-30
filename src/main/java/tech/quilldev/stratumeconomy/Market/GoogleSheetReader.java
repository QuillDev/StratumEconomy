package tech.quilldev.stratumeconomy.Market;

import org.bukkit.Material;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

public class GoogleSheetReader {

    public HashMap<Material, MarketItem> loadMarketDataFromUrl(String sheetId, String sheetName) {

        return new HashMap<>() {{
            //Create the URL we will read the sheet from
            final var url = String.format("https://docs.google.com/spreadsheets/d/%s/gviz/tq?tqx=out:csv&sheet=%s", sheetId, sheetName);

            //Create a buffered input stream that we will read data into
            final BufferedInputStream inputStream;
            try {
                inputStream = new BufferedInputStream(new URL(url).openStream());
                byte[] dataBuffer = new byte[1024];

                final var scanner = new Scanner(inputStream);
                scanner.nextLine(); //SKIP THE FIRST LINE BECAUSE IT'S GARBAGE INFO
                while (scanner.hasNextLine()) {
                    final var nextLine = scanner.nextLine();
                    //Get only the values as a list
                    final var values = Arrays.stream(
                            //Cut up the string into acceptable bits
                            nextLine.strip()
                                    .replace("\"", "")
                                    .split(","))
                            .filter(string -> !string.isEmpty()) //Filter out any empty strings
                            .collect(Collectors.toCollection(ArrayList::new));

                    //If we've hit the empty data sets, return
                    if (values.size() < 4) {
                        break;
                    }

                    final var material = Material.valueOf(values.get(0));
                    final var buyString = Double.parseDouble(values.get(1));
                    final var sellString = Double.parseDouble(values.get(2));
                    final var isStratumString = Boolean.parseBoolean(values.get(3));
                    putIfAbsent(material, new MarketItem(material, buyString, sellString, isStratumString));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }};
    }
}
