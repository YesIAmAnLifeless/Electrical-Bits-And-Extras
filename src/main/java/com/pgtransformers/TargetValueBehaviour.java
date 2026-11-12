package com.pgtransformers;

import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueBoxTransform;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueSettingsBehaviour.ValueSettings;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueSettingsBoard;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueSettingsFormatter;
import com.simibubi.create.foundation.blockEntity.behaviour.scrollValue.ScrollValueBehaviour;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.patryk3211.powergrid.electricity.base.HorizontalElectricBlock;

import java.util.ArrayList;
import java.util.List;

/**
 * Create's slider for the transformer's output voltage.
 *  small: 2 rows, 0-64 (x1) and 0-640 (x10)
 *  large: 3 rows, 0-100 (x1), 0-500 (x5), 0-2000 (x20)
 *  huge: 4 rows, up to 100 / 1 000 / 100 000 / 10 000 000
 * The real value is a double in the block entity; this class translates (row, slider) <-> value.
 */
public class TargetValueBehaviour extends ScrollValueBehaviour {
    private static final double[] MULT_SMALL = {1, 10};
    private static final String[] LABEL_SMALL = {"x1", "x10"};
    private static final double[] MULT_LARGE = {1, 5, 20};            // up to 2000 V
    private static final String[] LABEL_LARGE = {"x1", "x5", "x20"};
    private static final double[] MULT_HUGE = {1, 10, 1_000, 100_000};
    private static final String[] LABEL_HUGE = {"x1", "x10", "x1 000", "x100 000"};

    private final ConverterBlockEntity converter;
    private final double[] mult;
    private final String[] labels;
    private final int maxSlider;

    public TargetValueBehaviour(Component label, ConverterBlockEntity be, int tier) {
        super(label, be, new FrontPanel());
        this.converter = be;
        this.mult = tier == ConverterBlock.HUGE ? MULT_HUGE : tier == ConverterBlock.LARGE ? MULT_LARGE : MULT_SMALL;
        this.labels = tier == ConverterBlock.HUGE ? LABEL_HUGE : tier == ConverterBlock.LARGE ? LABEL_LARGE : LABEL_SMALL;
        this.maxSlider = tier == ConverterBlock.SMALL ? 64 : 100;
    }

    @Override
    public ValueSettingsBoard createBoard(Player player, BlockHitResult hitResult) {
        List<Component> rows = new ArrayList<>();
        for (String l : labels) rows.add(Component.literal(l));
        return new ValueSettingsBoard(label, maxSlider, 8, rows, new ValueSettingsFormatter(this::formatSettings));
    }

    @Override
    public void setValueSettings(Player player, ValueSettings settings, boolean ctrlDown) {
        int row = Math.max(0, Math.min(mult.length - 1, settings.row()));
        double value = Math.min(maxSlider, settings.value()) * mult[row];
        if (Math.abs(Math.abs(converter.getTarget()) - value) < 1e-9) return;
        converter.setTarget(value);
        playFeedbackSound(this);
    }

    @Override
    public ValueSettings getValueSettings() {
        double t = Math.abs(converter.getTarget());
        for (int r = 0; r < mult.length; r++) {
            if (t / mult[r] <= maxSlider + 1e-9 || r == mult.length - 1) {
                return new ValueSettings(r, (int) Math.min(maxSlider, Math.round(t / mult[r])));
            }
        }
        return new ValueSettings(0, 0);
    }

    public MutableComponent formatSettings(ValueSettings settings) {
        int row = Math.max(0, Math.min(mult.length - 1, settings.row()));
        return Component.literal(compact(settings.value() * mult[row]) + " V");
    }

    /** What the hover label of the value box shows (Create reads these, not our double). */
    @Override
    public String formatValue() {
        return compact(Math.abs(converter.getTarget())) + " V";
    }

    @Override
    public int getValue() {
        return (int) Math.min(Integer.MAX_VALUE, Math.round(Math.abs(converter.getTarget())));
    }

    @Override
    public String getClipboardKey() {
        return "PGTransformerTarget";
    }

    static String compact(double v) {
        if (v >= 1_000_000) return trim(v / 1_000_000) + "M";
        if (v >= 10_000) return trim(v / 1_000) + "k";
        return trim(v);
    }

    private static String trim(double v) {
        return v == Math.rint(v) ? Long.toString((long) v) : String.format("%.1f", v);
    }

    /** Value box sits on the front face of the block. */
    public static class FrontPanel extends ValueBoxTransform.Sided {
        @Override
        protected Vec3 getSouthLocation() {
            return new Vec3(8 / 16.0, 7 / 16.0, 13.5 / 16.0);
        }

        @Override
        protected boolean isSideActive(BlockState state, Direction direction) {
            return state.getValue(HorizontalElectricBlock.HORIZONTAL_FACING) == direction;
        }
    }
}
