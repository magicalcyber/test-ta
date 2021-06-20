package cc.magickiat.crypto;

import com.tictactec.ta.lib.Core;
import com.tictactec.ta.lib.MInteger;
import com.tictactec.ta.lib.RetCode;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class BaseIndicator {

    protected static final Core TA_CORE = new Core();
    protected static final SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
    protected static final DecimalFormat DF = new DecimalFormat("###.00");

    protected static double[] getAtr(double[] high, double[] low, double[] close, int period) {
        int beginIndex = 0;
        int endIndex = close.length - 1;
        double[] tempOutput = new double[close.length];
        double[] output = new double[close.length];

        MInteger begin = new MInteger();
        MInteger length = new MInteger();

        TA_CORE.atr(beginIndex, endIndex, high, low, close, period, begin, length, tempOutput);

        for (int i = period; i < tempOutput.length; i++) {
            output[i] = tempOutput[i - begin.value];
        }

        return output;
    }

    protected static double[] getTr(double[] high, double[] low, double[] close, int period) {
        int beginIndex = 0;
        int endIndex = close.length - 1;
        double[] tempOutput = new double[close.length];
        double[] output = new double[close.length];

        MInteger begin = new MInteger();
        MInteger length = new MInteger();

        TA_CORE.trueRange(beginIndex, endIndex, high, low, close, begin, length, tempOutput);

        for (int i = period; i < tempOutput.length; i++) {
            output[i] = tempOutput[i - begin.value];
        }

        return output;
    }

    protected static double[] getSma(double[] closedPrices, int smaPeriod) {
        int beginIndex = 0;
        int endIndex = closedPrices.length - 1;
        double[] tempOutput = new double[closedPrices.length];
        double[] output = new double[closedPrices.length];

        MInteger begin = new MInteger();
        MInteger length = new MInteger();

        RetCode retCode = TA_CORE.ema(beginIndex, endIndex, closedPrices, smaPeriod, begin, length, tempOutput);
        if (retCode != RetCode.Success) {
            throw new RuntimeException("TA Lib error when calculate");
        }

        for (int i = smaPeriod; i < tempOutput.length; i++) {
            output[i] = tempOutput[i - begin.value];
        }

        return output;
    }

    protected static double[] getStDev(double[] closedPrices, int period, double normalDistribution) {
        int beginIndex = 0;
        int endIndex = closedPrices.length - 1;
        double[] tempOutput = new double[closedPrices.length];
        double[] output = new double[closedPrices.length];

        MInteger begin = new MInteger();
        MInteger length = new MInteger();

        RetCode retCode = TA_CORE.stdDev(beginIndex, endIndex, closedPrices, period, normalDistribution, begin, length, tempOutput);
        if (retCode != RetCode.Success) {
            throw new RuntimeException("TA Lib error when calculate");
        }

        for (int i = period; i < tempOutput.length; i++) {
            output[i] = tempOutput[i - begin.value];
        }

        return output;
    }
}
