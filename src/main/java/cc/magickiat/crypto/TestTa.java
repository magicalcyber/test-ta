package cc.magickiat.crypto;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseBarSeriesBuilder;
import org.ta4j.core.indicators.ATRIndicator;
import org.ta4j.core.indicators.SMAIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.indicators.helpers.HighPriceIndicator;
import org.ta4j.core.indicators.helpers.LowPriceIndicator;
import org.ta4j.core.indicators.statistics.StandardDeviationIndicator;
import org.ta4j.core.num.DecimalNum;
import org.ta4j.core.num.Num;

import java.io.FileNotFoundException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TestTa {

    private static final Logger logger = LoggerFactory.getLogger(TestTa.class);
    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);


    public static void main(String[] args) throws FileNotFoundException {
        BarSeries series = new BaseBarSeriesBuilder().withName("BTCBUSD").build();
        HistoryTrade historyTrade = new HistoryTrade("output/btcbusd-5m.csv");

        List<Date> dateList = historyTrade.getDateList();

        List<BigDecimal> bdClose = historyTrade.getBdClose();
        List<BigDecimal> bdHigh = historyTrade.getBdHigh();
        List<BigDecimal> bdLow = historyTrade.getBdLow();
        List<BigDecimal> bdOpen = historyTrade.getBdOpen();
        List<BigDecimal> bdVolume = historyTrade.getBdVolume();

        for (int i = 0; i < dateList.size(); i++) {
            series.addBar(ZonedDateTime.ofInstant(dateList.get(i).toInstant(), ZoneId.of("Asia/Bangkok")),
                    bdOpen.get(i),
                    bdHigh.get(i),
                    bdLow.get(i),
                    bdClose.get(i),
                    bdVolume.get(i));
        }

        final int BB_PERIOD = 21;
        final int ATR_PERIOD = 5;
        final Num BB_DEVIATION = DecimalNum.valueOf(1.0);


        ClosePriceIndicator closePrice = new ClosePriceIndicator(series);
        LowPriceIndicator lowPrice = new LowPriceIndicator(series);
        HighPriceIndicator highPrice = new HighPriceIndicator(series);

        ATRIndicator atr = new ATRIndicator(series, ATR_PERIOD);

        SMAIndicator smaIndicator = new SMAIndicator(closePrice, BB_PERIOD);
        StandardDeviationIndicator stDevIndicator = new StandardDeviationIndicator(closePrice, BB_PERIOD);


        final int BB_BREAK_UPPER = 1;
        final int BB_BREAK_LOWER = -1;
        final int CLOSE_PRICE_IN_BB = 0;

        Num prevTrendLine = DecimalNum.valueOf(0);
        int prevTrend = 0;

        for (int i = 0; i < dateList.size(); i++) {

            // 1 - BBSignal
            Num close = series.getBar(i).getClosePrice();
            Num bbUpper = smaIndicator.getValue(i).plus(
                    stDevIndicator.getValue(i).multipliedBy(BB_DEVIATION));
            Num bbLower = smaIndicator.getValue(i).minus(
                    stDevIndicator.getValue(i).multipliedBy(BB_DEVIATION));

            int bbSignal;
            if (close.isGreaterThan(bbUpper)) {
                bbSignal = BB_BREAK_UPPER;
            } else if (close.isLessThan(bbLower)) {
                bbSignal = BB_BREAK_LOWER;
            } else {
                bbSignal = CLOSE_PRICE_IN_BB;
            }

            // 2 - trend line
            Num trendLine;
            if (bbSignal == BB_BREAK_UPPER) {
                trendLine = lowPrice.getValue(i).minus(atr.getValue(i));

                if (trendLine.isLessThan(prevTrendLine)) {
                    trendLine = prevTrendLine;
                }
            } else if (bbSignal == BB_BREAK_LOWER) {
                trendLine = highPrice.getValue(i).plus(atr.getValue(i));

                if (trendLine.isGreaterThan(prevTrendLine)) {
                    trendLine = prevTrendLine;
                }
            } else {
                trendLine = prevTrendLine;
            }

            // 3 - trend
            int trend = prevTrend;
            if (trendLine.isGreaterThan(prevTrendLine)) {
                trend = 1;
            }
            if (trendLine.isLessThan(prevTrendLine)) {
                trend = -1;
            }


            logger.info("\t\t\t>>trendLine = " + trendLine + ", prevTrendLine = " + prevTrendLine);
            logger.info("\t\t\t>>trend = " + trend + ", prev trend = " + prevTrend);

            if (prevTrend == -1 && trend == 1) {
                logger.info(sdf.format(dateList.get(i)) + " >>> BUY!!!!");
            }
            if (prevTrend == 1 && trend == -1) {
                logger.info(sdf.format(dateList.get(i)) + " >>> SELL!!!");
            }


            logger.info(String.format("%s ===> %s", sdf.format(dateList.get(i)), trendLine));

            prevTrendLine = trendLine;
            prevTrend = trend;
        }

    }
}
