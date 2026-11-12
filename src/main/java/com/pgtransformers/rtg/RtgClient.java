package com.pgtransformers.rtg;

import com.pgtransformers.PGTransformers;
import com.simibubi.create.CreateClient;
import com.simibubi.create.foundation.block.connected.CTModel;
import net.minecraft.resources.ResourceLocation;

public final class RtgClient {
    private RtgClient() {}

    public static void init() {
        RtgCTBehaviour behaviour = new RtgCTBehaviour();
        CreateClient.MODEL_SWAPPER.getCustomBlockModels().register(
                ResourceLocation.fromNamespaceAndPath(PGTransformers.MODID, "rtg"),
                model -> new CTModel(model, behaviour));
    }
}
