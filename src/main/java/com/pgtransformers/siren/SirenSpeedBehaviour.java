package com.pgtransformers.siren;

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

import java.util.List;

/**
 * Create's slider for the siren's pitch / speed (one control: the loop is resampled, so a higher
 * pitch is also a faster wail, a lower pitch a slower one). The slider is 0..100 and maps
 * exponentially onto 0.5x .. 2.0x, so 50 is exactly normal speed. Loudness is NOT set here: that is the voltage.
 */
public class SirenSpeedBehaviour extends ScrollValueBehaviour {
    public static final int MAX = 100;
    public static final int DEFAULT = 50;

    private final IndustrialSirenBlockEntity siren;

    public SirenSpeedBehaviour(Component label, IndustrialSirenBlockEntity be) {
        super(label, be, new SidePanel());
        this.siren = be;
    }

    /** Slider position (0..100) -> playback rate (0.5 .. 2.0). */
    public static float toPitch(int slider) {
        int s = Math.max(0, Math.min(MAX, slider));
        return (float) (0.5 * Math.pow(4.0, s / (double) MAX));
    }

    @Override
    public ValueSettingsBoard createBoard(Player player, BlockHitResult hitResult) {
        return new ValueSettingsBoard(label, MAX, 10, List.of(Component.literal("Pitch")),
                new ValueSettingsFormatter(this::formatSettings));
    }

    @Override
    public void setValueSettings(Player player, ValueSettings settings, boolean ctrlDown) {
        int v = Math.max(0, Math.min(MAX, settings.value()));
        if (v == siren.getSpeed()) return;
        siren.setSpeed(v);
        playFeedbackSound(this);
    }

    @Override
    public ValueSettings getValueSettings() {
        return new ValueSettings(0, siren.getSpeed());
    }

    public MutableComponent formatSettings(ValueSettings settings) {
        return Component.literal(String.format("%.2fx", toPitch(settings.value())));
    }

    @Override
    public String formatValue() {
        return String.format("%.2fx", toPitch(siren.getSpeed()));
    }

    @Override
    public int getValue() {
        return siren.getSpeed();
    }

    @Override
    public String getClipboardKey() {
        return "PGSirenSpeed";
    }

    /** Value box on the right-hand side of the body (the front, with the horn, faces away from the terminals). */
    public static class SidePanel extends ValueBoxTransform.Sided {
        @Override
        protected Vec3 getSouthLocation() {
            return new Vec3(8 / 16.0, 7 / 16.0, 13.5 / 16.0);
        }

        @Override
        protected boolean isSideActive(BlockState state, Direction direction) {
            return direction == state.getValue(HorizontalElectricBlock.HORIZONTAL_FACING).getClockWise();
        }
    }
}
