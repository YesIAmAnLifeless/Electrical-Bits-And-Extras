package com.pgtransformers;

import com.simibubi.create.content.processing.sequenced.SequencedAssemblyItem;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import com.pgtransformers.client.ClientSetup;
import com.pgtransformers.rtg.RtgBlock;
import com.pgtransformers.rtg.RtgBlockEntity;
import com.pgtransformers.rtg.RtgClient;
import com.pgtransformers.rtg.RtgItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Blocks;
import org.patryk3211.powergrid.config.ThermalValues;
import java.util.function.DoubleSupplier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.loading.FMLEnvironment;
import org.patryk3211.powergrid.electricity.wire.WireItem;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import com.pgtransformers.siren.IndustrialSirenBlock;
import com.pgtransformers.siren.IndustrialSirenBlockEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;

import java.util.function.Supplier;

@Mod(PGTransformers.MODID)
public class PGTransformers {
    public static final String MODID = "pgtransformers";

    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MODID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, MODID);
    public static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);

    public static final DeferredRegister<SoundEvent> SOUNDS =
            DeferredRegister.create(Registries.SOUND_EVENT, MODID);
    public static final DeferredHolder<SoundEvent, SoundEvent> SIREN_SOUND =
            SOUNDS.register("industrial_siren", () -> SoundEvent.createVariableRangeEvent(
                    ResourceLocation.fromNamespaceAndPath(MODID, "industrial_siren")));

    private static BlockBehaviour.Properties props() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .strength(2.0f, 6.0f)
                .sound(SoundType.METAL)
                .noOcclusion()
                .requiresCorrectToolForDrops();
    }

    public static final DeferredBlock<ConverterBlock> VOLTAGE_TRANSFORMER =
            BLOCKS.register("voltage_transformer", () -> new ConverterBlock(props(), ConverterBlock.SMALL));
    public static final DeferredBlock<ConverterBlock> LARGE_VOLTAGE_TRANSFORMER =
            BLOCKS.register("large_voltage_transformer", () -> new ConverterBlock(props(), ConverterBlock.LARGE));
    public static final DeferredBlock<ConverterBlock> HUGE_VOLTAGE_TRANSFORMER =
            BLOCKS.register("huge_voltage_transformer", () -> new ConverterBlock(props(), ConverterBlock.HUGE));
    public static final DeferredBlock<TransformerExtBlock> TRANSFORMER_EXT =
            BLOCKS.register("large_voltage_transformer_ext", () -> new TransformerExtBlock(props().noLootTable()));
    public static final DeferredBlock<TransformerCoreBlock> CUSTOM_CORE =
            BLOCKS.register("custom_transformer_core", () -> new TransformerCoreBlock(props()));

    public static final DeferredItem<?> CUSTOM_CORE_ITEM = ITEMS.registerSimpleBlockItem(CUSTOM_CORE);

    // --- Unbreakable Switch: MV switch geometry, no voltage/current limit, blast-proof ---
    public static final DeferredBlock<UnbreakableSwitchBlock> UNBREAKABLE_SWITCH =
            BLOCKS.register("unbreakable_switch", () -> new UnbreakableSwitchBlock(
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.COLOR_PURPLE)
                            // To make it literally unminable like bedrock, change 2.0f to -1.0f.
                            .strength(2.0f, 3_600_000.0f)
                            .sound(SoundType.METAL)
                            .noOcclusion()));
    public static final DeferredItem<?> UNBREAKABLE_SWITCH_ITEM = ITEMS.registerSimpleBlockItem(UNBREAKABLE_SWITCH);

    // --- RTG (stackable, never-depleting battery-style generator) ---
    public static final DeferredBlock<RtgBlock> RTG = BLOCKS.registerBlock("rtg", RtgBlock::new,
            BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK).strength(4.0f, 8.0f)
                    .sound(SoundType.NETHERITE_BLOCK).requiresCorrectToolForDrops());
    public static final DeferredItem<RtgItem> RTG_ITEM =
            ITEMS.register("rtg", () -> new RtgItem(RTG.get(), new Item.Properties()));
    public static final Supplier<BlockEntityType<RtgBlockEntity>> RTG_BE =
            BLOCK_ENTITIES.register("rtg", () -> BlockEntityType.Builder.of(
                    (pos, state) -> new RtgBlockEntity(PGTransformers.RTG_BE.get(), pos, state), RTG.get()).build(null));

    // --- Materials for the Custom Transformer Core (sequenced assembly: copper -> gold -> iron sheets) ---
    public static final DeferredItem<SequencedAssemblyItem> INCOMPLETE_RTG =
            ITEMS.register("incomplete_rtg", () -> new SequencedAssemblyItem(new Item.Properties()));
    public static final DeferredItem<SequencedAssemblyItem> INCOMPLETE_TRANSFORMER_CORE =
            ITEMS.register("incomplete_transformer_core", () -> new SequencedAssemblyItem(new Item.Properties()));

    // --- Ultimate Multimeter (no voltage/current limit) ---
    public static final DeferredItem<UltimateMultimeterItem> ULTIMATE_MULTIMETER =
            ITEMS.register("ultimate_multimeter",
                    () -> new UltimateMultimeterItem(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC)));

    // --- Heavy cables. Their stats live in data/pgtransformers/powergrid/wire_types/*.json
    //     and they are added to the powergrid:light_wires tag, so every terminal that takes
    //     copper or gold wire takes these too. ---
    public static final DeferredItem<WireItem> HEAVY_CABLE_300A =
            ITEMS.register("heavy_cable_300a", () -> new WireItem(new Item.Properties()));
    public static final DeferredItem<WireItem> HEAVY_CABLE_1000A =
            ITEMS.register("heavy_cable_1000a", () -> new WireItem(new Item.Properties()));

    // --- Industrial Siren: loud alarm, 500 V max, burns out above that ---
    public static final DeferredBlock<IndustrialSirenBlock> INDUSTRIAL_SIREN =
            BLOCKS.register("industrial_siren", () -> new IndustrialSirenBlock(
                    BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_RED)
                            .strength(2.0f, 6.0f).sound(SoundType.METAL).noOcclusion()));
    public static final DeferredItem<?> INDUSTRIAL_SIREN_ITEM = ITEMS.registerSimpleBlockItem(INDUSTRIAL_SIREN);
    public static final Supplier<BlockEntityType<IndustrialSirenBlockEntity>> SIREN_BE =
            BLOCK_ENTITIES.register("industrial_siren", () -> BlockEntityType.Builder.of(
                    IndustrialSirenBlockEntity::new, INDUSTRIAL_SIREN.get()).build(null));

    public static final Supplier<BlockEntityType<ConverterBlockEntity>> CONVERTER_BE =
            BLOCK_ENTITIES.register("converter", () ->
                    BlockEntityType.Builder.of(ConverterBlockEntity::new,
                            VOLTAGE_TRANSFORMER.get(), LARGE_VOLTAGE_TRANSFORMER.get(), HUGE_VOLTAGE_TRANSFORMER.get()).build(null));

    public static final Supplier<BlockEntityType<UnbreakableSwitchBlockEntity>> UNBREAKABLE_SWITCH_BE =
            BLOCK_ENTITIES.register("unbreakable_switch", () ->
                    BlockEntityType.Builder.of(UnbreakableSwitchBlockEntity::new,
                            UNBREAKABLE_SWITCH.get()).build(null));

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> TAB =
            TABS.register("main", () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.pgtransformers"))
                    .icon(() -> new ItemStack(CUSTOM_CORE_ITEM.get()))
                    .displayItems((params, out) -> {
                        out.accept(CUSTOM_CORE_ITEM.get());
                        out.accept(RTG_ITEM.get());
                        out.accept(UNBREAKABLE_SWITCH_ITEM.get());
                        out.accept(ULTIMATE_MULTIMETER.get());
                        out.accept(HEAVY_CABLE_300A.get());
                        out.accept(HEAVY_CABLE_1000A.get());
                        out.accept(INDUSTRIAL_SIREN_ITEM.get());
                    })
                    .build());

    public PGTransformers(IEventBus modBus) {
        BLOCKS.register(modBus);
        ITEMS.register(modBus);
        BLOCK_ENTITIES.register(modBus);
        TABS.register(modBus);
        SOUNDS.register(modBus);
        ThermalValues.register(new ThermalValues.Provider() {
            @Override
            public DoubleSupplier getPower(net.minecraft.world.level.block.Block block) {
                return block == RTG.get() ? () -> 400.0 : null;
            }

            @Override
            public DoubleSupplier getMass(net.minecraft.world.level.block.Block block) {
                return block == RTG.get() ? () -> 2.0 : null;
            }
        });
        if (FMLEnvironment.dist == Dist.CLIENT) {
            RtgClient.init();
            ClientSetup.init(modBus);
        }
    }
}
