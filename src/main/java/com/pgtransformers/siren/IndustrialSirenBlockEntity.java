package com.pgtransformers.siren;

import com.pgtransformers.PGTransformers;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import org.patryk3211.powergrid.collections.ModdedSoundEvents;
import org.patryk3211.powergrid.electricity.base.ElectricBlockEntity;
import org.patryk3211.powergrid.electricity.base.IElectricEntity;
import org.patryk3211.powergrid.electricity.base.ThermalBehaviour;
import org.patryk3211.powergrid.electricity.sim.ElectricWire;

import java.util.List;

public class IndustrialSirenBlockEntity extends ElectricBlockEntity implements IHaveGoggleInformation {
    /** Below this it stays silent. Volume rises linearly up to MAX_VOLTS. */
    private static final double MIN_VOLTS = 12.0;
    /** Small tolerance so a 500 V transformer setting doesn't burn it by rounding. */
    private static final double BURN_VOLTS = IndustrialSirenBlock.MAX_VOLTS * 1.005;

    private ElectricWire wire;
    private int overTicks;
    private float volume;      // 0..1
    private float lastSent = -1;
    private double volts;
    private boolean soundActive;
    /** Pitch / speed slider, 0..100 (50 = normal). Synced to clients; loudness is the voltage. */
    private int speed = SirenSpeedBehaviour.DEFAULT;

    public IndustrialSirenBlockEntity(BlockPos pos, BlockState state) {
        super(PGTransformers.SIREN_BE.get(), pos, state);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        super.addBehaviours(behaviours);
        behaviours.add(new SirenSpeedBehaviour(Component.translatable("gui.pgtransformers.siren.speed"), this));
    }

    public int getSpeed() {
        return speed;
    }

    /** Playback rate used by the client sound: pitch and speed move together. */
    public float getPitch() {
        return SirenSpeedBehaviour.toPitch(speed);
    }

    public void setSpeed(int newSpeed) {
        speed = Math.max(0, Math.min(SirenSpeedBehaviour.MAX, newSpeed));
        setChanged();
        if (level != null && !level.isClientSide) sendData();
    }

    /** No thermal model: the only limit is the voltage. */
    @Override
    public ThermalBehaviour specifyThermalBehaviour() {
        return null;
    }

    @Override
    public float resistance() {
        return IndustrialSirenBlock.RESISTANCE;
    }

    @Override
    public float resistance(String name) {
        return IndustrialSirenBlock.RESISTANCE;
    }

    @Override
    public boolean isNoisy() {
        return true;
    }

    @Override
    public void buildCircuit(IElectricEntity.CircuitBuilder builder) {
        builder.setTerminalCount(2);
        wire = builder.connect(IndustrialSirenBlock.RESISTANCE, builder.terminalNode(0), builder.terminalNode(1));
    }

    @Override
    public void electricalTick() {
        super.electricalTick();
        if (level == null || level.isClientSide || wire == null) return;

        double v = Math.abs(wire.potentialDifference());
        volts = v;
        if (v > BURN_VOLTS) {
            if (++overTicks >= 3) {
                burnOut();
                return;
            }
        } else {
            overTicks = 0;
        }
        double f = (v - MIN_VOLTS) / (IndustrialSirenBlock.MAX_VOLTS - MIN_VOLTS);
        volume = (float) Math.max(0.0, Math.min(1.0, f));
    }

    private void burnOut() {
        if (level instanceof ServerLevel sl) {
            double x = worldPosition.getX() + 0.5, y = worldPosition.getY() + 0.7, z = worldPosition.getZ() + 0.5;
            sl.sendParticles(ParticleTypes.LARGE_SMOKE, x, y, z, 16, 0.25, 0.2, 0.25, 0.02);
            sl.sendParticles(ParticleTypes.FLAME, x, y, z, 8, 0.2, 0.1, 0.2, 0.03);
            ModdedSoundEvents.COMPONENT_EXPLODE.playOnServer(level, worldPosition);
        }
        level.destroyBlock(worldPosition, false); // burnt out: no drop
    }

    @Override
    public void lazyTick() {
        super.lazyTick();
        if (level != null && !level.isClientSide && Math.abs(volume - lastSent) > 0.01f) {
            lastSent = volume;
            sendData();
        }
    }

    // ---- client audio ----

    public float getVolume() {
        return volume;
    }

    public void setSoundActive(boolean active) {
        soundActive = active;
    }

    @Override
    public void tickAudio() {
        super.tickAudio();
        if (level != null && level.isClientSide && !soundActive && volume > 0.001f) {
            soundActive = true;
            SirenClient.play(this);
        }
    }

    // ---- persistence / sync ----

    @Override
    protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(tag, registries, clientPacket);
        if (tag.contains("Speed")) speed = tag.getInt("Speed");
        if (clientPacket) {
            volume = tag.getFloat("Volume");
            volts = tag.getDouble("Volts");
        }
    }

    @Override
    protected void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.write(tag, registries, clientPacket);
        tag.putInt("Speed", speed);
        if (clientPacket) {
            tag.putFloat("Volume", volume);
            tag.putDouble("Volts", volts);
        }
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        tooltip.add(Component.literal("    ").append(Component.translatable("block.pgtransformers.industrial_siren")));
        tooltip.add(Component.literal("    ")
                .append(Component.translatable("gui.pgtransformers.siren.voltage").withStyle(ChatFormatting.GRAY))
                .append(Component.literal(String.format(" %.1f V", volts)).withStyle(ChatFormatting.AQUA)));
        tooltip.add(Component.literal("    ")
                .append(Component.translatable("gui.pgtransformers.siren.speed").withStyle(ChatFormatting.GRAY))
                .append(Component.literal(String.format(" %.2fx", getPitch())).withStyle(ChatFormatting.GOLD)));
        tooltip.add(Component.literal("    ")
                .append(Component.translatable("gui.pgtransformers.siren.max").withStyle(ChatFormatting.GRAY))
                .append(Component.literal(" 500 V").withStyle(ChatFormatting.RED)));
        return true;
    }
}
