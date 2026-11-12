package com.pgtransformers.client;

import com.pgtransformers.PGTransformers;
import com.simibubi.create.foundation.item.render.SimpleCustomRenderer;
import com.pgtransformers.ponder.PGTPonderPlugin;
import net.createmod.ponder.foundation.PonderIndex;
import net.minecraft.world.item.Item;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import org.patryk3211.powergrid.equipment.multimeter.MultimeterItemRenderer;

/** Client only. Gives the Ultimate Multimeter the same animated needle model as Power Grid's multimeter. */
public final class ClientSetup {
    private ClientSetup() {}

    public static void init(IEventBus modBus) {
        modBus.addListener(ClientSetup::registerExtensions);
        modBus.addListener(ClientSetup::clientSetup);
    }

    private static void clientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> PonderIndex.addPlugin(new PGTPonderPlugin()));
    }

    private static void registerExtensions(RegisterClientExtensionsEvent event) {
        Item item = PGTransformers.ULTIMATE_MULTIMETER.get();
        event.registerItem(SimpleCustomRenderer.create(item, new MultimeterItemRenderer()), item);
    }
}
