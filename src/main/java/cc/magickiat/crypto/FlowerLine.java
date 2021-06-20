package cc.magickiat.crypto;

import com.tictactec.ta.lib.Core;

import java.io.FileNotFoundException;
import java.util.Date;
import java.util.List;

public class FlowerLine extends BaseIndicator {

    public static void main(String[] args) throws FileNotFoundException {
        int bbPeriod = 21;
        double bbDeviations = 1.0;
        int atrPeriod = 5;

        final Core taCore = new Core();

        HistoryTrade historyTrade = new HistoryTrade("output/btcbusd-5m.csv");

        List<Date> dates = historyTrade.getDateList();
        double[] close = historyTrade.getClose();
        double[] high = historyTrade.getHigh();
        double[] low = historyTrade.getLow();

        double[] atr = getAtr(high, low, close, atrPeriod);
        double[] sma = getSma(historyTrade.getClose(), bbPeriod);
        double[] stDev = getStDev(historyTrade.getClose(), bbPeriod, bbDeviations);


        double[] bbUpper = getBbUpper(true, sma, stDev, bbPeriod, bbDeviations);
        double[] bbLower = getBbUpper(false, sma, stDev, bbPeriod, bbDeviations);

        double[] trendLines = new double[close.length];
        double[] iTrends = new double[close.length];

        for (int i = 0; i < dates.size(); i++) {

            int bbSignal = close[i] > bbUpper[i] ? 1
                    : close[i] < bbLower[i] ? -1 : 0;

            double prevTrendLine = -1;
            if (i > 0) {
                prevTrendLine = trendLines[i - 1];
            }
            double trendLine = 0;

            if (bbSignal == 1) {
                trendLine = low[i] - atr[i];
                if (trendLine < prevTrendLine) {
                    trendLine = prevTrendLine;
                }
            }

            if (bbSignal == -1) {
                trendLine = high[i] + atr[i];
                if (trendLine > prevTrendLine) {
                    trendLine = prevTrendLine;
                }
            }

            if (bbSignal == 0) {
                trendLine = prevTrendLine;
            }

            trendLines[i] = trendLine;

            // --------------------
            double iTrend = -1;
            if (i > 0) {
                iTrend = iTrends[i - 1];
            }

            if (trendLine > iTrend) {
                iTrend = 1;
            } else {
                iTrend = -1;
            }
            iTrends[i] = iTrend;

            boolean buySignal = false;
            if (i > 0 && iTrends[i - 1] == -1 && iTrend == 1) {
                buySignal = true;
            }

            boolean sellSignal = false;
            if (i > 0 && iTrends[i - 1] == 1 && iTrend == -1) {
                sellSignal = true;
            }

            // --------------------
            System.out.println(SDF.format(dates.get(i)) + " ===> signal = " + bbSignal + ", trendLine = " + DF.format(trendLine));
            if (sellSignal) {
                System.out.println("\t>> SELL!");
            }
            if (buySignal) {
                System.out.println("\t>> BUY!!");
            }
        }
    }

    private static double[] getBbUpper(boolean upperBand, double[] sma, double[] stDev, int bbPeriod, double bbDeviations) {
        double[] output = new double[sma.length];

        for (int i = 0; i < sma.length; i++) {
            double aSma = sma[i];
            double aStdDev = stDev[i];

            double result;
            if (upperBand) {
                result = aSma + aStdDev * bbDeviations;
            } else {
                result = aSma - aStdDev * bbDeviations;
            }
            output[i] = result;
        }

        return output;
    }
}
