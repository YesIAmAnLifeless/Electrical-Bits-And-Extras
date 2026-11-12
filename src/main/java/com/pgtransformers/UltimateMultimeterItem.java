package com.pgtransformers;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.patryk3211.powergrid.equipment.multimeter.MultimeterItem;

import java.util.List;
import java.util.Locale;

/**
 * Power Grid's multimeter with the range limits removed.
 *
 * The original reads multimeterVoltage / multimeterCurrent from the server config and
 * uses them in getText (over-range display), getDial (needle scale) and appendProperties
 * (tooltip). We override just those three. Everything else (probing terminals, wires,
 * HUD, needle renderer) is inherited, and Power Grid's own "instanceof MultimeterItem"
 * checks accept this subclass.
 */
public class UltimateMultimeterItem extends MultimeterItem {
    private static final String[] BIG = {"", "k", "M", "G", "T", "P", "E", "Z", "Y"};
    private static final String[] SMALL = {"", "m", "\u00b5", "n", "p"};

    public UltimateMultimeterItem(Item.Properties properties) {
        super(properties);
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }

    @Override
    public Component getText(Level level, Player player, ItemStack stack) {
        float value = getMeasurement(level, stack);
        boolean voltage = getMode(stack) == 0;
        String text = si(value, voltage ? "V" : "A");
        return Component.translatable(
                voltage ? "tooltip.pgtransformers.multimeter.voltage" : "tooltip.pgtransformers.multimeter.current",
                Component.literal(text).withStyle(voltage ? ChatFormatting.BLUE : ChatFormatting.YELLOW))
                .withStyle(ChatFormatting.GRAY);
    }

    /** Auto-ranging needle: scales to the next power of ten so it never pegs at the end stop. */
    @Override
    public float getDial(Level level, ItemStack stack) {
        float value = getMeasurement(level, stack);
        if (Float.isNaN(value)) return 0f;
        if (Float.isInfinite(value)) return Math.signum(value);
        double abs = Math.abs((double) value);
        if (abs == 0.0) return 0f;
        double range = Math.max(1.0, Math.pow(10.0, Math.ceil(Math.log10(abs))));
        double dial = value / range;
        return (float) Math.max(-1.0, Math.min(1.0, dial));
    }

    @Override
    public void appendProperties(ItemStack stack, Player player, List<Component> tooltip) {
        tooltip.add(Component.translatable("tooltip.pgtransformers.multimeter.max_voltage")
                .withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("tooltip.pgtransformers.multimeter.max_current")
                .withStyle(ChatFormatting.GRAY));
    }

    /** 1234.5 -> "1.23 kV", 0.002 -> "2.00 mV", works from pico to yotta. */
    static String si(double v, String unit) {
        if (Double.isNaN(v)) return "-- " + unit;
        if (Double.isInfinite(v)) return (v < 0 ? "-" : "") + "\u221e " + unit;
        double abs = Math.abs(v);
        if (abs < 1.0e-12) return "0.00 " + unit;
        double scaled = v;
        String prefix = "";
        if (abs >= 1000.0) {
            int i = 0;
            while (Math.abs(scaled) >= 1000.0 && i < BIG.length - 1) {
                scaled /= 1000.0;
                i++;
            }
            prefix = BIG[i];
        } else if (abs < 1.0) {
            int i = 0;
            while (Math.abs(scaled) < 1.0 && i < SMALL.length - 1) {
                scaled *= 1000.0;
                i++;
            }
            prefix = SMALL[i];
        }
        return String.format(Locale.ROOT, "%.2f %s%s", scaled, prefix, unit);
    }
}
