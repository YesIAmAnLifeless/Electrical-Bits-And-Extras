package com.pgtransformers;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import org.patryk3211.powergrid.electricity.electricswitch.MvSwitchBlock;
import org.patryk3211.powergrid.electricity.electricswitch.SwitchBlockEntity;

import java.util.List;

/**
 * Same size, shape, terminals, states and behaviour as Power Grid's MV Switch, but it cannot be
 * overloaded:
 *
 *  - maxVoltage is infinite, so the "open switch arcs over and welds shut" failure never triggers.
 *  - the entity has no thermal behaviour (see UnbreakableSwitchBlockEntity), so I^2*R heating can
 *    never trip or destroy it, whatever the current.
 *  - resistance is a fixed tiny constant. This is REQUIRED, not cosmetic: Power Grid's
 *    ResistanceValues.get(block) throws for blocks it doesn't know about.
 *  - blast resistance is bedrock-level and it is wither/dragon immune (see data tags).
 */
public class UnbreakableSwitchBlock extends MvSwitchBlock {
    /** Closed-switch resistance in ohms (same order as Power Grid's creative sources use). */
    public static final float RESISTANCE = 1.0e-4f;

    public UnbreakableSwitchBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.maxVoltage = Float.POSITIVE_INFINITY;
    }

    @Override
    public float getMaxVoltage() {
        return Float.POSITIVE_INFINITY;
    }

    @Override
    public float resistance() {
        return RESISTANCE;
    }

    @Override
    public float resistance(String name) {
        return RESISTANCE;
    }

    @Override
    public BlockEntityType<? extends SwitchBlockEntity> getBlockEntityType() {
        return PGTransformers.UNBREAKABLE_SWITCH_BE.get();
    }

    @Override
    public void appendProperties(ItemStack stack, Player player, List<Component> tooltip) {
        tooltip.add(Component.translatable("tooltip.pgtransformers.switch.max_voltage").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("tooltip.pgtransformers.switch.max_current").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("tooltip.pgtransformers.switch.indestructible").withStyle(ChatFormatting.DARK_PURPLE));
    }
}
