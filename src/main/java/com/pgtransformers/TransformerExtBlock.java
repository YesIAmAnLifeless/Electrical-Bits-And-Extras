package com.pgtransformers;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.patryk3211.powergrid.electricity.base.HorizontalElectricBlock;

/** Passive radiator/conservator half of the Large Custom Voltage Transformer. Has no electronics. */
public class TransformerExtBlock extends Block {
    public static final net.minecraft.world.level.block.state.properties.DirectionProperty FACING =
            HorizontalDirectionalBlock.FACING;
    /** 0 = large's side, 1 = huge top-right, 2 = huge bottom-left, 3 = huge bottom-right */
    public static final IntegerProperty KIND = IntegerProperty.create("kind", 0, 3);
    private static final VoxelShape SHAPE = box(0, 0, 0, 16, 16, 16);

    public TransformerExtBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(KIND, 0));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, KIND);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
        return SHAPE;
    }

    private static BlockPos mainPos(BlockState state, BlockPos pos) {
        Direction ccw = state.getValue(FACING).getCounterClockWise();
        return switch (state.getValue(KIND)) {
            case 2 -> pos.above();
            case 3 -> pos.above().relative(ccw);
            default -> pos.relative(ccw);
        };
    }

    @Override
    public ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state) {
        return new ItemStack(PGTransformers.CUSTOM_CORE_ITEM.get());
    }

    /** Breaking the radiator drops/removes the whole machine. */
    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean moving) {
        super.onRemove(state, level, pos, newState, moving);
        if (!state.is(newState.getBlock())) {
            BlockPos main = mainPos(state, pos);
            BlockState ms = level.getBlockState(main);
            if (ms.getBlock() instanceof ConverterBlock
                    && ms.getValue(HorizontalElectricBlock.HORIZONTAL_FACING) == state.getValue(FACING)) {
                level.destroyBlock(main, !level.isClientSide);
            }
        }
    }
}
