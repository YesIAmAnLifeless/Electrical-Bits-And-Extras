package com.pgtransformers.rtg;

import org.patryk3211.powergrid.electricity.battery.BatterySpec;

/** Stackable RTG: 1 wide = 12 V, 2 wide = 120 V, 3 wide = 240 V. Never runs out. */
public final class RtgSpec implements BatterySpec {
    public static final RtgSpec INSTANCE = new RtgSpec();
    public static final float CAPACITY = 1000.0f;
    private static final float[] VOLTAGE = {12.0f, 120.0f, 240.0f};
    private static final float[] CELL_RESISTANCE = {1.0f, 6.0f, 18.0f};

    private RtgSpec() {}

    private static int idx(int width) {
        return Math.max(0, Math.min(VOLTAGE.length, width) - 1);
    }

    public static float voltageForWidth(int width) {
        return VOLTAGE[idx(width)];
    }

    public static float cellResistanceForWidth(int width) {
        return CELL_RESISTANCE[idx(width)];
    }

    @Override
    public float getInitialCharge() {
        return CAPACITY;
    }

    @Override
    public float getMaxCharge() {
        return CAPACITY;
    }

    @Override
    public float calculateVoltage(float charge) {
        return VOLTAGE[0];
    }

    @Override
    public float calculateResistance(float charge) {
        return CELL_RESISTANCE[0];
    }
}
