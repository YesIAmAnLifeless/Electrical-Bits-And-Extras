package com.pgtransformers.rtg;

import com.pgtransformers.PGTransformers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.patryk3211.powergrid.electricity.battery.BatteryBlock;
import org.patryk3211.powergrid.electricity.battery.CustomConnectivityHandler;
import org.patryk3211.powergrid.electricity.battery.MultiBlockBatteryEntity;

/**
 * Like Create's fluid tanks: click the top or bottom of a 2x2 / 3x3 RTG with another RTG
 * and the whole new layer is placed at once (hold Shift to place a single block).
 */
public class RtgItem extends BlockItem {
    public RtgItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public InteractionResult place(BlockPlaceContext ctx) {
        InteractionResult result = super.place(ctx);
        if (result.consumesAction()) tryMultiPlace(ctx);
        return result;
    }

    private void tryMultiPlace(BlockPlaceContext ctx) {
        Player player = ctx.getPlayer();
        if (player == null || player.isShiftKeyDown()) return;
        Direction face = ctx.getClickedFace();
        if (!face.getAxis().isVertical()) return;
        ItemStack stack = ctx.getItemInHand();
        Level level = ctx.getLevel();
        BlockPos pos = ctx.getClickedPos();
        BlockPos placedOn = pos.relative(face.getOpposite());
        if (!BatteryBlock.isBattery(level.getBlockState(placedOn))) return;

        BlockEntity part = CustomConnectivityHandler.partAt(PGTransformers.RTG_BE.get(), RtgSpec.INSTANCE, level, placedOn);
        if (!(part instanceof MultiBlockBatteryEntity mb)) return;
        MultiBlockBatteryEntity controller = mb.getControllerBE();
        if (controller == null) return;
        int width = controller.getWidth();
        if (width == 1) return;

        BlockPos start = face == Direction.DOWN
                ? controller.getBlockPos().below()
                : controller.getBlockPos().above(controller.getHeight());
        if (start.getY() != pos.getY()) return;

        int toPlace = 0;
        for (int x = 0; x < width; x++) {
            for (int z = 0; z < width; z++) {
                BlockState s = level.getBlockState(start.offset(x, 0, z));
                if (BatteryBlock.isBattery(s)) continue;
                if (!s.canBeReplaced()) return;
                toPlace++;
            }
        }
        if (!player.isCreative() && stack.getCount() < toPlace) return;

        for (int x = 0; x < width; x++) {
            for (int z = 0; z < width; z++) {
                BlockPos p = start.offset(x, 0, z);
                if (BatteryBlock.isBattery(level.getBlockState(p))) continue;
                super.place(BlockPlaceContext.at(ctx, p, face));
            }
        }
    }
}
