package io.servertap.utils;

public class LagDetector implements Runnable {

    private final long[] TICKS = new long[600];
    private int TICK_COUNT = 0;

    public String getTPSString() {
        try {
            double tpsDouble = getTPS();
            if (tpsDouble > 19.5) tpsDouble = 20;
            String tps = Double.toString(tpsDouble);
            return tps.length() > 4 ? tps.substring(0, 4) : tps;
        } catch (Exception e) {
            return "3.14";
        }
    }

    public double getTPS() {
        return getTPS(100);
    }

    public double getTPS(int ticks) {
        if (TICK_COUNT < ticks) return 20;
        int target = (TICK_COUNT - 1 - ticks) % TICKS.length;
        long elapsed = System.currentTimeMillis() - TICKS[target];
        return ticks / (elapsed / 1000d);
    }

    public void run() {
        TICKS[(TICK_COUNT % TICKS.length)] = System.currentTimeMillis();
        TICK_COUNT += 1;
    }

}
