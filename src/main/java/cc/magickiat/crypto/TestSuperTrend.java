package cc.magickiat.crypto;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.ta4j.core.Bar;
import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseBarSeriesBuilder;
import org.ta4j.core.indicators.ATRIndicator;
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

public class TestSuperTrend {
    private static final Logger logger = LoggerFactory.getLogger(TestSuperTrend.class);
    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);

    public static void main(String[] args) throws FileNotFoundException {
        BarSeries series = new BaseBarSeriesBuilder().withName("BTCUSDT").build();
        HistoryTrade historyTrade = new HistoryTrade("output/btcusdt-1h.csv");

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


        final int ATR_PERIOD = 10;
        final Num FACTOR = DecimalNum.valueOf(3);

        ATRIndicator atr = new ATRIndicator(series, ATR_PERIOD);

        Num prevTrendUp = null;
        Num prevTrendDown = null;
        int prevTrend = 1;
        for (int i = 1; i < dateList.size(); i++) {
            Bar bar = series.getBar(i);
            Bar prevBar = series.getBar(i - 1);

            Num hl2 = bar.getHighPrice().plus(bar.getLowPrice()).dividedBy(DecimalNum.valueOf(2));
            Num strPlusFactor = FACTOR.multipliedBy(atr.getValue(i));

            Num up = hl2.minus(strPlusFactor);
            Num down = hl2.plus(strPlusFactor);

            // --- trend up ---
            Num trendUp;
            if (prevTrendUp != null && prevBar.getClosePrice().isGreaterThan(prevTrendUp)) {
                if (up.isGreaterThan(prevTrendUp)) {
                    trendUp = up;
                } else {
                    trendUp = prevTrendUp;
                }
            } else {
                trendUp = up;
            }

            // --- trend down ---
            Num trendDown;
            if (prevTrendDown != null && prevBar.getClosePrice().isLessThan(prevTrendDown)) {
                if (down.isLessThan(prevTrendDown)) {
                    trendDown = down;
                } else {
                    trendDown = prevTrendDown;
                }
            } else {
                trendDown = down;
            }

            // --- trend ---

            if (prevTrendDown != null) {
                int trend;
                if (bar.getClosePrice().isGreaterThan(prevTrendDown)) {
                    trend = 1;
                } else if (bar.getClosePrice().isLessThan(prevTrendUp)) {
                    trend = -1;
                } else {
                    trend = prevTrend;
                }

                Num trendValue;
                if (trend == 1) {
                    trendValue = trendUp;
                } else {
                    trendValue = trendDown;
                }

                boolean buySignal = prevTrend == -1 && trend == 1;
                boolean sellSignal = prevTrend == 1 && trend == -1;

                logger.info(sdf.format(dateList.get(i)) + " ===> "
                        + (trend == 1 ? "UP.." : "DOWN")
                        + "\t" + trendValue
                        + (buySignal ? "\t<<< Stop SELL and begin BUY!!" : sellSignal ? "\t<<<Stop BUY and begin SELL!!!" : ""));

                prevTrend = trend;
            }

            prevTrendUp = trendUp;
            prevTrendDown = trendDown;

        }
    }
}
