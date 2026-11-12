package com.pgtransformers.rtg;

import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.patryk3211.powergrid.electricity.battery.MultiBlockBatteryEntity;

import java.util.List;

public class RtgBlockEntity extends MultiBlockBatteryEntity implements IHaveGoggleInformation {
    public RtgBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void updateParameters() {
        if (sourceCoupling == null) return;
        int width = getWidth();
        sourceCoupling.setVoltage((double) RtgSpec.voltageForWidth(width));
        sourceCoupling.setResistance(RtgSpec.cellResistanceForWidth(width) / (float) Math.max(1, getSize()));
    }

    /** Always full: the charge is refilled every tick. */
    @Override
    public void electricalTick() {
        if (isController() && capacity <= 0.0) {
            capacity = getSpec().getMaxCharge() * (double) Math.max(1, getSize());
            energy = capacity;
        }
        super.electricalTick();
        if (isController()) energy = capacity;
    }

    /** Goggle readout for the whole stack, like Create's Fluid Container Info. */
    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        MultiBlockBatteryEntity c = getControllerBE();
        if (c == null) c = this; // controller is briefly unknown while the stack is being broken or rebuilt
        int w = Math.max(1, c.getWidth());
        int h = Math.max(1, c.getHeight());
        int size = Math.max(1, c.getSize());
        double volts = RtgSpec.voltageForWidth(w);
        double rInt = RtgSpec.cellResistanceForWidth(w) / size;
        double maxAmps = volts / rInt;
        double matchedKw = volts * volts / (4.0 * rInt) / 1000.0;
        tooltip.add(Component.literal("    ").append(Component.literal("Radioisotope Generator Info:").withStyle(ChatFormatting.GRAY)));
        row(tooltip, "Stack: ", w + " x " + w + " x " + h + "  (" + size + " blocks)", ChatFormatting.AQUA);
        row(tooltip, "Output: ", String.format("%.0f V", volts), ChatFormatting.BLUE);
        row(tooltip, "Short-circuit: ", String.format("%.0f A", maxAmps), ChatFormatting.GOLD);
        row(tooltip, "Max power: ", String.format("%.1f kW", matchedKw), ChatFormatting.GREEN);
        return true;
    }

    private static void row(List<Component> tooltip, String label, String value, ChatFormatting color) {
        tooltip.add(Component.literal("     ").append(Component.literal(label).withStyle(ChatFormatting.GRAY))
                .append(Component.literal(value).withStyle(color)));
    }
}
