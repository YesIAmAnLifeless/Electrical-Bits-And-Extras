package com.pgtransformers;

import com.simibubi.create.content.equipment.wrench.IWrenchable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.patryk3211.powergrid.electricity.base.HorizontalElectricBlock;

/**
 * Custom Transformer Core. Wrench it:
 *  - alone                         -> Small Custom Voltage Transformer (max 640 V)
 *  - two cores side by side (2x1) -> Large Custom Voltage Transformer (max 2000 V)
 *  - four cores in a wall (2x2)   -> Huge Custom Voltage Transformer (no limit)
 * The front faces the player. The machine's controls/terminals are on the top-left block.
 */
public class TransformerCoreBlock extends Block implements IWrenchable {
    public TransformerCoreBlock(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult onWrenched(BlockState state, UseOnContext ctx) {
        Level level = ctx.getLevel();
        BlockPos pos = ctx.getClickedPos();
        if (level.isClientSide) return InteractionResult.SUCCESS;

        Direction front = ctx.getHorizontalDirection().getOpposite(); // faces the player

        // 2x2 (wall of four cores) -> Huge
        for (Direction d : Direction.Plane.HORIZONTAL) {
            for (int v = -1; v <= 1; v += 2) {
                BlockPos a = pos, b = pos.relative(d), c = pos.above(v), e = b.above(v);
                if (!isCore(level, b) || !isCore(level, c) || !isCore(level, e)) continue;
                BlockPos t0 = v > 0 ? c : a;            // top-row cell above/at the clicked block
                BlockPos t1 = t0.relative(d);
                Direction f = front.getAxis() == d.getAxis() ? d.getCounterClockWise() : front;
                boolean t0IsMain = f.getClockWise() == d;
                BlockPos main = t0IsMain ? t0 : t1;
                BlockPos side = t0IsMain ? t1 : t0;
                level.setBlock(main, PGTransformers.HUGE_VOLTAGE_TRANSFORMER.get().defaultBlockState()
                        .setValue(HorizontalElectricBlock.HORIZONTAL_FACING, f), 3);
                ext(level, side, f, 1);
                ext(level, main.below(), f, 2);
                ext(level, side.below(), f, 3);
                done(level, pos, 0.8f);
                return InteractionResult.SUCCESS;
            }
        }

        // 2x1 -> Large
        for (Direction d : Direction.Plane.HORIZONTAL) {
            BlockPos other = pos.relative(d);
            if (!isCore(level, other)) continue;
            // the front must be perpendicular to d. main + front.cw == the radiator half.
            Direction f = front.getAxis() == d.getAxis() ? d.getCounterClockWise() : front;
            boolean posIsMain = f.getClockWise() == d;
            BlockPos main = posIsMain ? pos : other;
            BlockPos side = posIsMain ? other : pos;
            level.setBlock(main, PGTransformers.LARGE_VOLTAGE_TRANSFORMER.get().defaultBlockState()
                    .setValue(HorizontalElectricBlock.HORIZONTAL_FACING, f), 3);
            ext(level, side, f, 0);
            done(level, pos, 1.2f);
            return InteractionResult.SUCCESS;
        }

        // 1x1 -> Small
        level.setBlock(pos, PGTransformers.VOLTAGE_TRANSFORMER.get().defaultBlockState()
                .setValue(HorizontalElectricBlock.HORIZONTAL_FACING, front), 3);
        done(level, pos, 1.5f);
        return InteractionResult.SUCCESS;
    }

    private boolean isCore(Level level, BlockPos p) {
        return level.getBlockState(p).is(this);
    }

    private static void ext(Level level, BlockPos p, Direction f, int kind) {
        level.setBlock(p, PGTransformers.TRANSFORMER_EXT.get().defaultBlockState()
                .setValue(TransformerExtBlock.FACING, f).setValue(TransformerExtBlock.KIND, kind), 3);
    }

    private static void done(Level level, BlockPos p, float pitch) {
        level.playSound(null, p, SoundEvents.ANVIL_PLACE, SoundSource.BLOCKS, 0.6f, pitch);
    }
}
