package com.pgtransformers.siren;

import com.pgtransformers.PGTransformers;
import com.simibubi.create.foundation.block.IBE;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.patryk3211.powergrid.electricity.base.HorizontalElectricBlock;
import org.patryk3211.powergrid.electricity.base.TerminalBoundingBox;

/** Industrial Siren: a very loud alarm bell that takes up to 500 V and burns out above that. */
public class IndustrialSirenBlock extends HorizontalElectricBlock implements IBE<IndustrialSirenBlockEntity> {
    public static final float MAX_VOLTS = 500.0f;
    /** Resistance in ohms (500 V -> 500 W). */
    public static final float RESISTANCE = 500.0f;

    private static final VoxelShape SHAPE = box(2, 0, 0, 14, 14, 16);
    private static final int RED = 16726843;
    private static final int BLUE = 3899647;

    private static final TerminalBoundingBox[] TERMINALS = new TerminalBoundingBox[] {
            new TerminalBoundingBox(Component.translatable("terminal.pgtransformers.siren_pos"), 9.5, 4, 14, 12.5, 7, 16).withColor(RED),
            new TerminalBoundingBox(Component.translatable("terminal.pgtransformers.siren_neg"), 3.5, 4, 14, 6.5, 7, 16).withColor(BLUE)
    };

    public IndustrialSirenBlock(BlockBehaviour.Properties properties) {
        super(properties);
        setTerminalCollection(horizontalNorthTerminals(this, TERMINALS, SHAPE));
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
    public Class<IndustrialSirenBlockEntity> getBlockEntityClass() {
        return IndustrialSirenBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends IndustrialSirenBlockEntity> getBlockEntityType() {
        return PGTransformers.SIREN_BE.get();
    }
}
