package com.pgtransformers.rtg;

import com.pgtransformers.PGTransformers;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import org.patryk3211.powergrid.electricity.battery.BatteryBlock;
import org.patryk3211.powergrid.electricity.battery.BatterySpec;
import org.patryk3211.powergrid.electricity.battery.MultiBlockBatteryEntity;

public class RtgBlock extends BatteryBlock {
    public RtgBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public BatterySpec getSpec() {
        return RtgSpec.INSTANCE;
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public BlockEntityType<MultiBlockBatteryEntity> getBlockEntityType() {
        return (BlockEntityType) PGTransformers.RTG_BE.get();
    }
}
