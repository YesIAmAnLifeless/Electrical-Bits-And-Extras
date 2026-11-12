package com.pgtransformers;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import org.patryk3211.powergrid.electricity.base.ThermalBehaviour;
import org.patryk3211.powergrid.electricity.electricswitch.SwitchBlockEntity;

public class UnbreakableSwitchBlockEntity extends SwitchBlockEntity {
    public UnbreakableSwitchBlockEntity(BlockPos pos, BlockState state) {
        super(PGTransformers.UNBREAKABLE_SWITCH_BE.get(), pos, state);
    }

    /** No thermal model at all: it can never overheat, so there is no current limit. */
    @Override
    public ThermalBehaviour specifyThermalBehaviour() {
        return null;
    }

    /** IElectricEntity.resistance() would look the block up in Power Grid's config and throw. */
    @Override
    public float resistance() {
        return UnbreakableSwitchBlock.RESISTANCE;
    }
}
