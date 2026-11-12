package com.pgtransformers;

import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.state.BlockState;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import org.patryk3211.powergrid.electricity.base.ElectricBlockEntity;
import org.patryk3211.powergrid.electricity.base.IElectricEntity;
import org.patryk3211.powergrid.electricity.sim.node.FloatingNode;
import org.patryk3211.powergrid.electricity.sim.node.TransformerCoupling;

import java.util.List;

/**
 * An ideal transformer (Power Grid's own TransformerCoupling, the same one the Variac uses)
 * whose ratio is re-tuned every tick so that the OUTPUT holds the value you typed:
 *
 *
 * Because it is a real transformer, power is conserved: input current = ratio * output current.
 * The output side is floating (isolated from the input), like any Power Grid transformer.
 */
public class ConverterBlockEntity extends ElectricBlockEntity implements IHaveGoggleInformation {
    public static final double LIMIT = 1.0e9;

    private static final float COUPLING_RESISTANCE = 1.0e-4f;
    private static final double MIN_INPUT_VOLTAGE = 0.05;  // below this the input is considered "dead" and we hold
    private static final double MAX_RATIO = 10_000.0;

    private double target = 0.0;
    private float ratio = 1.0f;

    private double lastVin, lastVout;

    private FloatingNode inP, inN, outP, outN;
    private TransformerCoupling coupling;

    public ConverterBlockEntity(BlockPos pos, BlockState state) {
        super(PGTransformers.CONVERTER_BE.get(), pos, state);
    }

    private double maxVolts() {
        return getBlockState().getBlock() instanceof ConverterBlock b ? b.maxVolts() : ConverterBlock.SMALL_MAX_VOLTS;
    }

    private int tier() {
        return getBlockState().getBlock() instanceof ConverterBlock b ? b.tier() : ConverterBlock.SMALL;
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        super.addBehaviours(behaviours);
        behaviours.add(new TargetValueBehaviour(
                Component.translatable("gui.pgtransformers.enter_volts"), this, tier()));
    }

    @Override
    public void lazyTick() {
        super.lazyTick();
        // readouts are measured on the server; push them to clients so goggles show real numbers
        if (level != null && !level.isClientSide) sendData();
    }

    @Override
    public void buildCircuit(IElectricEntity.CircuitBuilder builder) {
        builder.setTerminalCount(4);
        inP = builder.terminalNode(0);
        inN = builder.terminalNode(1);
        outP = builder.terminalNode(2);
        outN = builder.terminalNode(3);

        // secondary voltage = ratio * primary voltage  (primary = in+/in-, secondary = out+/out-)
        coupling = builder.couple(ratio, COUPLING_RESISTANCE, inP, inN, outP, outN);
    }

    @Override
    public void electricalTick() {
        super.electricalTick();
        if (coupling == null || inP == null) return;

        if (level != null && level.isClientSide) return;
        double vin = inP.getVoltage() - inN.getVoltage();
        double vout = outP.getVoltage() - outN.getVoltage();
        lastVin = vin;
        lastVout = vout;

        if (Math.abs(vin) < MIN_INPUT_VOLTAGE) return; // no input power, nothing to regulate

        double desired = clamp(Math.abs(target) / vin, -MAX_RATIO, MAX_RATIO);
        double newRatio = ratio + (desired - ratio) * 0.5; // smooth so it doesn't ring

        if (Math.abs(newRatio - ratio) > 1.0e-4 * Math.max(1.0, Math.abs(ratio))) {
            ratio = (float) newRatio;
            coupling.setRatio(ratio);
        }
    }

    private static double clamp(double v, double lo, double hi) {
        return Math.max(lo, Math.min(hi, v));
    }

    public double getTarget() {
        return target;
    }

    /** Server side. */
    public void setTarget(double newTarget) {
        double max = maxVolts();
        target = clamp(newTarget, -max, max);
        setChanged();
        if (level != null && !level.isClientSide) {
            sendData();
        }
    }

    @Override
    protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(tag, registries, clientPacket);
        if (tag.contains("Target")) target = tag.getDouble("Target");
        if (tag.contains("Ratio")) {
            ratio = tag.getFloat("Ratio");
            if (coupling != null) coupling.setRatio(ratio);
        }
        if (clientPacket) {
            lastVin = tag.getDouble("Vin");
            lastVout = tag.getDouble("Vout");
        }
    }

    @Override
    protected void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.write(tag, registries, clientPacket);
        tag.putDouble("Target", target);
        tag.putFloat("Ratio", ratio);
        if (clientPacket) {
            tag.putDouble("Vin", lastVin);
            tag.putDouble("Vout", lastVout);
        }
    }

    @Override
    public void writeSafe(CompoundTag tag, HolderLookup.Provider registries) {
        super.writeSafe(tag, registries);
        tag.putDouble("Target", target);
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        tooltip.add(Component.literal("    ").append(Component.translatable(
                tier() == ConverterBlock.HUGE ? "block.pgtransformers.huge_voltage_transformer"
                        : tier() == ConverterBlock.LARGE ? "block.pgtransformers.large_voltage_transformer"
                        : "block.pgtransformers.voltage_transformer")));
        line(tooltip, "gui.pgtransformers.target", fmt(Math.abs(target)) + " V", ChatFormatting.BLUE);
        line(tooltip, "gui.pgtransformers.input", fmt(Math.abs(lastVin)) + " V", ChatFormatting.AQUA);
        line(tooltip, "gui.pgtransformers.output", fmt(Math.abs(lastVout)) + " V", ChatFormatting.GOLD);
        if (tier() != ConverterBlock.HUGE) line(tooltip, "gui.pgtransformers.limit", fmt(maxVolts()) + " V", ChatFormatting.RED);
        return true;
    }

    private static void line(List<Component> tooltip, String key, String value, ChatFormatting color) {
        tooltip.add(Component.literal("    ")
                .append(Component.translatable(key).withStyle(ChatFormatting.GRAY))
                .append(Component.literal(" " + value).withStyle(color)));
    }

    private static String fmt(double v) {
        return String.format("%.2f", v);
    }
}
