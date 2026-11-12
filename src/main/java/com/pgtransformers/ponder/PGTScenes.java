package com.pgtransformers.ponder;

import com.pgtransformers.PGTransformers;
import com.pgtransformers.TransformerExtBlock;
import com.simibubi.create.AllItems;
import com.simibubi.create.foundation.ponder.CreateSceneBuilder;
import net.createmod.catnip.math.Pointing;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.patryk3211.powergrid.electricity.base.HorizontalElectricBlock;

/** Ponder storyboards. Texts live in the code (and are mirrored in lang/en_us.json). */
public final class PGTScenes {
    private PGTScenes() {}

    private static BlockState machine(net.minecraft.world.level.block.Block b) {
        return b.defaultBlockState().setValue(HorizontalElectricBlock.HORIZONTAL_FACING, Direction.SOUTH);
    }

    private static BlockState ext(int kind) {
        return PGTransformers.TRANSFORMER_EXT.get().defaultBlockState()
                .setValue(TransformerExtBlock.FACING, Direction.SOUTH).setValue(TransformerExtBlock.KIND, kind);
    }

    private static void wrench(CreateSceneBuilder scene, SceneBuildingUtil util, BlockPos at) {
        scene.overlay().showControls(util.vector().topOf(at), Pointing.DOWN, 40)
                .rightClick().withItem(AllItems.WRENCH.asStack());
        scene.idle(25);
    }

    public static void small(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title("small_transformer", "Small Custom Voltage Transformer");
        scene.configureBasePlate(0, 0, 5);
        scene.world().showSection(util.select().layer(0), Direction.UP);
        scene.idle(5);
        BlockPos p = util.grid().at(2, 1, 2);
        scene.world().showSection(util.select().position(p), Direction.DOWN);
        scene.idle(10);
        scene.overlay().showText(70).text("The Custom Transformer Core is the only way to get a Custom Voltage Transformer")
                .attachKeyFrame().pointAt(util.vector().topOf(p)).placeNearTarget();
        scene.idle(80);
        wrench(scene, util, p);
        scene.world().replaceBlocks(util.select().position(p), machine(PGTransformers.VOLTAGE_TRANSFORMER.get()), true);
        scene.idle(10);
        scene.overlay().showText(80).text("Used on its own, a Wrench turns it into a Small Custom Voltage Transformer, which faces you")
                .attachKeyFrame().pointAt(util.vector().topOf(p)).placeNearTarget();
        scene.idle(90);
        scene.overlay().showText(80).text("The small one can output up to 640 V")
                .pointAt(util.vector().centerOf(p)).placeNearTarget();
        scene.idle(90);
    }

    public static void large(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title("large_transformer", "Large Custom Voltage Transformer");
        scene.configureBasePlate(0, 0, 5);
        scene.world().showSection(util.select().layer(0), Direction.UP);
        scene.idle(5);
        BlockPos a = util.grid().at(2, 1, 2), b = util.grid().at(1, 1, 2);
        scene.world().showSection(util.select().fromTo(b, a), Direction.DOWN);
        scene.idle(10);
        scene.overlay().showText(70).text("Place two cores next to each other")
                .attachKeyFrame().pointAt(util.vector().topOf(a)).placeNearTarget();
        scene.idle(80);
        wrench(scene, util, a);
        scene.world().replaceBlocks(util.select().position(a), machine(PGTransformers.LARGE_VOLTAGE_TRANSFORMER.get()), true);
        scene.world().replaceBlocks(util.select().position(b), ext(0), true);
        scene.idle(10);
        scene.overlay().showText(90).text("Wrenching one of them builds a Large Custom Voltage Transformer. Its controls and terminals are on the block with the panel")
                .attachKeyFrame().pointAt(util.vector().topOf(a)).placeNearTarget();
        scene.idle(100);
        scene.overlay().showText(80).text("The large one can output up to 2000 V")
                .pointAt(util.vector().topOf(b)).placeNearTarget();
        scene.idle(90);
    }

    public static void huge(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title("huge_transformer", "Huge Custom Voltage Transformer");
        scene.configureBasePlate(0, 0, 5);
        scene.world().showSection(util.select().layer(0), Direction.UP);
        scene.idle(5);
        BlockPos main = util.grid().at(2, 2, 2), side = util.grid().at(1, 2, 2);
        BlockPos down = util.grid().at(2, 1, 2), downSide = util.grid().at(1, 1, 2);
        scene.world().showSection(util.select().fromTo(1, 1, 2, 2, 2, 2), Direction.DOWN);
        scene.idle(10);
        scene.overlay().showText(70).text("Stack four cores into a 2x2 wall")
                .attachKeyFrame().pointAt(util.vector().topOf(main)).placeNearTarget();
        scene.idle(80);
        wrench(scene, util, main);
        scene.world().replaceBlocks(util.select().position(main), machine(PGTransformers.HUGE_VOLTAGE_TRANSFORMER.get()), true);
        scene.world().replaceBlocks(util.select().position(side), ext(1), true);
        scene.world().replaceBlocks(util.select().position(down), ext(2), true);
        scene.world().replaceBlocks(util.select().position(downSide), ext(3), true);
        scene.idle(10);
        scene.overlay().showText(90).text("A Wrench turns the wall into a Huge Custom Voltage Transformer. The controls and terminals are on the top block with the panel")
                .attachKeyFrame().pointAt(util.vector().topOf(main)).placeNearTarget();
        scene.idle(100);
        scene.overlay().showText(80).text("The huge one has no voltage limit")
                .pointAt(util.vector().centerOf(side)).placeNearTarget();
        scene.idle(90);
    }

    public static void slider(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title("transformer_slider", "Setting the output voltage");
        scene.configureBasePlate(0, 0, 5);
        scene.world().showSection(util.select().layer(0), Direction.UP);
        BlockPos p = util.grid().at(2, 1, 2);
        scene.world().replaceBlocks(util.select().position(p), machine(PGTransformers.VOLTAGE_TRANSFORMER.get()), false);
        scene.world().showSection(util.select().position(p), Direction.DOWN);
        scene.idle(15);
        Vec3 panel = util.vector().blockSurface(p, Direction.SOUTH);
        scene.overlay().showText(80).text("The output voltage is set with Create's slider on the front panel")
                .attachKeyFrame().pointAt(panel).placeNearTarget();
        scene.idle(90);
        scene.overlay().showControls(panel, Pointing.RIGHT, 50).rightClick();
        scene.idle(20);
        scene.overlay().showText(90).text("Hold right-click on the panel, drag along the bar and release. Each row is a bigger step")
                .pointAt(panel).placeNearTarget();
        scene.idle(100);
        scene.overlay().showText(90).text("Connect power to the input terminals and the output terminals will hold the voltage you chose")
                .attachKeyFrame().pointAt(util.vector().topOf(p)).placeNearTarget();
        scene.idle(100);
    }
}
