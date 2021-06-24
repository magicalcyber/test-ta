package cc.magickiat.crypto;

import com.binance.api.client.BinanceApiClientFactory;
import com.binance.api.client.BinanceApiRestClient;
import com.binance.api.client.domain.market.Candlestick;
import com.binance.api.client.domain.market.CandlestickInterval;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.List;

public class DownloadCandlestickHistory {
    public static void main(String[] args) throws FileNotFoundException {
        // Prepare Binance client
        BinanceApiClientFactory factory = BinanceApiClientFactory.newInstance(
                System.getenv("BINANCE_API_KEY"), System.getenv("BINANCE_API_SECRET"));
        BinanceApiRestClient restClient = factory.newRestClient();

//        List<Candlestick> candlestickBars = restClient.getCandlestickBars("BTCBUSD",
//                CandlestickInterval.FIVE_MINUTES, 1000, null, null);
        List<Candlestick> candlestickBars = restClient.getCandlestickBars("BTCUSDT",
                CandlestickInterval.HOURLY);
        candlestickBars.remove(candlestickBars.size() - 1);

        System.out.println("Found: " + candlestickBars.size());

        File output = new File("output", "btcusdt-1h.csv");
        System.out.println("Begin write to file ");
        try (PrintWriter writer = new PrintWriter(output)) {
            for (Candlestick bar : candlestickBars) {
                String data = String.format("%s,%s,%s,%s,%s,%s", bar.getCloseTime(), bar.getOpen(), bar.getHigh(), bar.getLow(), bar.getClose(), bar.getVolume());
                writer.println(data);
            }
            writer.flush();
        }
        System.out.println("Write file complete");
    }
}
