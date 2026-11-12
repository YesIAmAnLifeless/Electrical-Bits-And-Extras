package com.pgtransformers;

import com.simibubi.create.foundation.block.IBE;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.patryk3211.powergrid.electricity.base.HorizontalElectricBlock;
import org.patryk3211.powergrid.electricity.base.TerminalBoundingBox;

/**
 * Custom Voltage Transformer. Four terminals; the output holds the voltage set with Create's slider.
 *  small (1x1): max 640 V.  large (2x1): max 2000 V.  huge (2x2, this block is the top-left): no limit.
 * Settings are made with the Create value slider only (hold right click on the front panel).
 */
public class ConverterBlock extends HorizontalElectricBlock implements IBE<ConverterBlockEntity> {
    public static final double SMALL_MAX_VOLTS = 640.0;
    public static final double LARGE_MAX_VOLTS = 2000.0;
    public static final int SMALL = 0, LARGE = 1, HUGE = 2;
    private static final VoxelShape SHAPE = box(0, 0, 0, 16, 14, 16);

    private static final int RED = 16726843;
    private static final int BLUE = 3899647;

    private static final TerminalBoundingBox[] TERMINALS = new TerminalBoundingBox[] {
            // 0: input +, 1: input -, 2: output +, 3: output -
            new TerminalBoundingBox(Component.translatable("terminal.pgtransformers.in_pos"), 9.5, 14, 3.5, 12.5, 16, 6.5).withColor(RED),
            new TerminalBoundingBox(Component.translatable("terminal.pgtransformers.in_neg"), 3.5, 14, 3.5, 6.5, 16, 6.5).withColor(BLUE),
            new TerminalBoundingBox(Component.translatable("terminal.pgtransformers.out_pos"), 9.5, 14, 9.5, 12.5, 16, 12.5).withColor(RED),
            new TerminalBoundingBox(Component.translatable("terminal.pgtransformers.out_neg"), 3.5, 14, 9.5, 6.5, 16, 12.5).withColor(BLUE)
    };

    private final int tier;

    public ConverterBlock(BlockBehaviour.Properties properties, int tier) {
        super(properties);
        this.tier = tier;
        setTerminalCollection(horizontalNorthTerminals(this, TERMINALS, SHAPE));
    }

    public int tier() {
        return tier;
    }

    public double maxVolts() {
        return tier == SMALL ? SMALL_MAX_VOLTS : tier == LARGE ? LARGE_MAX_VOLTS : ConverterBlockEntity.LIMIT;
    }

    /** Middle click gives the Custom Transformer Core, like Power Grid's transformers. */
    @Override
    public ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state) {
        return new ItemStack(PGTransformers.CUSTOM_CORE_ITEM.get());
    }

    /** Breaking the machine also removes its extra blocks. */
    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean moving) {
        super.onRemove(state, level, pos, newState, moving);
        if (tier != SMALL && !state.is(newState.getBlock())) {
            Direction f = state.getValue(HORIZONTAL_FACING);
            BlockPos side = pos.relative(f.getClockWise());
            clearExt(level, side);
            if (tier == HUGE) {
                clearExt(level, pos.below());
                clearExt(level, side.below());
            }
        }
    }

    private static void clearExt(Level level, BlockPos p) {
        if (level.getBlockState(p).is(PGTransformers.TRANSFORMER_EXT.get())) {
            level.setBlock(p, Blocks.AIR.defaultBlockState(), 3);
        }
    }

    @Override
    public Class<ConverterBlockEntity> getBlockEntityClass() {
        return ConverterBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends ConverterBlockEntity> getBlockEntityType() {
        return PGTransformers.CONVERTER_BE.get();
    }
}
