package cc.magickiat.crypto;

import java.io.File;
import java.io.FileNotFoundException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Scanner;

public class HistoryTrade {

    private final List<Date> dateList = new ArrayList<>();
    private final double[] high;
    private final double[] low;
    private final double[] close;
    private final double[] volume;
    private final double[] open;

    private final List<BigDecimal> bdOpen;
    private final List<BigDecimal> bdHigh;
    private final List<BigDecimal> bdLow;
    private final List<BigDecimal> bdClose;
    private final List<BigDecimal> bdVolume;

    public double[] getVolume() {
        return volume;
    }

    public double[] getOpen() {
        return open;
    }

    public List<BigDecimal> getBdOpen() {
        return bdOpen;
    }

    public List<BigDecimal> getBdVolume() {
        return bdVolume;
    }

    public HistoryTrade(String historyFile) throws FileNotFoundException {

        bdHigh = new ArrayList<>();
        bdLow = new ArrayList<>();
        bdClose = new ArrayList<>();
        bdVolume = new ArrayList<>();
        bdOpen = new ArrayList<>();

        try (Scanner scanner = new Scanner(new File(historyFile))) {
            while (scanner.hasNext()) {
                String line = scanner.nextLine();
                String[] data = line.split(",");

                Date date = new Date(Long.parseLong(data[0]));
                BigDecimal o = new BigDecimal(data[1]);
                BigDecimal h = new BigDecimal(data[2]);
                BigDecimal l = new BigDecimal(data[3]);
                BigDecimal c = new BigDecimal(data[4]);
                BigDecimal v = new BigDecimal(data[5]);

                dateList.add(date);
                bdOpen.add(o);
                bdHigh.add(h);
                bdLow.add(l);
                bdClose.add(c);
                bdVolume.add(v);
            }
        }

        open = getDouble(bdOpen);
        high = getDouble(bdHigh);
        low = getDouble(bdLow);
        close = getDouble(bdClose);
        volume = getDouble(bdVolume);
    }


    private static double[] getDouble(List<BigDecimal> doubleList) {
        double[] result = new double[doubleList.size()];
        for (int i = 0; i < doubleList.size(); i++) {
            result[i] = doubleList.get(i).doubleValue();
        }
        return result;
    }

    public List<Date> getDateList() {
        return new ArrayList<>(dateList);
    }

    public double[] getHigh() {
        return high;
    }

    public double[] getLow() {
        return low;
    }

    public double[] getClose() {
        return close;
    }

    public List<BigDecimal> getBdHigh() {
        return bdHigh;
    }

    public List<BigDecimal> getBdLow() {
        return bdLow;
    }

    public List<BigDecimal> getBdClose() {
        return bdClose;
    }
}
