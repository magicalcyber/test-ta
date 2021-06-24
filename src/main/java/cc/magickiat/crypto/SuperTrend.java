package cc.magickiat.crypto;

import com.binance.api.client.domain.event.CandlestickEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.ta4j.core.Bar;
import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseBarSeriesBuilder;
import org.ta4j.core.indicators.ATRIndicator;
import org.ta4j.core.num.DecimalNum;
import org.ta4j.core.num.Num;

import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Date;
import java.util.Locale;

public class SuperTrend {
    private static final Logger logger = LoggerFactory.getLogger(TestSuperTrend.class);
    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
    private static final BarSeries series = new BaseBarSeriesBuilder().build();
    private final int ATR_PERIOD;
    private final Num FACTOR;

    private final DateTimeFormatter formatter =
            DateTimeFormatter.ofLocalizedDateTime(FormatStyle.FULL)
                    .withLocale(Locale.UK)
                    .withZone(ZoneId.systemDefault());

    private Num prevTrendUp = null;
    private Num prevTrendDown = null;
    private int prevTrend = 1;

    public SuperTrend(HistoryTrade historyTrade, int atrPeriod, int factor) {

        for (int i = 0; i < historyTrade.getDateList().size(); i++) {
            series.addBar(ZonedDateTime.ofInstant(
                    historyTrade.getDateList().get(i).toInstant(), ZoneId.of("Asia/Bangkok")),
                    historyTrade.getBdOpen().get(i),
                    historyTrade.getBdHigh().get(i),
                    historyTrade.getBdLow().get(i),
                    historyTrade.getBdClose().get(i),
                    historyTrade.getBdVolume().get(i));
        }

        ATR_PERIOD = atrPeriod;
        FACTOR = DecimalNum.valueOf(factor);
    }

    public TradeAction onCandlestickClose(CandlestickEvent event) {
        series.addBar(ZonedDateTime.ofInstant(
                new Date(event.getCloseTime()).toInstant(), ZoneId.of("Asia/Bangkok")),
                event.getOpen(),
                event.getHigh(),
                event.getLow(),
                event.getClose(),
                event.getVolume());

        if (series.getBarCount() < ATR_PERIOD) {
            return TradeAction.DO_NOTHING;
        }

        ATRIndicator atr = new ATRIndicator(series, ATR_PERIOD);

        Bar bar = series.getBar(series.getBarCount() - 1);
        Bar prevBar = series.getBar(series.getBarCount() - 2);

        // (High + Low) / 2
        Num hl2 = bar.getHighPrice().plus(bar.getLowPrice()).dividedBy(DecimalNum.valueOf(2));

        Num strPlusFactor = FACTOR.multipliedBy(atr.getValue(series.getBarCount() - 1));

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

            logger.info(formatter.format(bar.getEndTime()) + " ===> " + (trend == 1 ? "UP.." : "DOWN") + "\t" + trendValue);

            prevTrend = trend;
        }

        prevTrendUp = trendUp;
        prevTrendDown = trendDown;

        return TradeAction.DO_NOTHING;
    }


}
