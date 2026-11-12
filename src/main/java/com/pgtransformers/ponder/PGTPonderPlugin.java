package com.pgtransformers.ponder;

import com.pgtransformers.PGTransformers;
import net.createmod.ponder.api.level.PonderLevel;
import net.createmod.ponder.api.registration.PonderPlugin;
import net.createmod.ponder.api.registration.PonderSceneRegistrationHelper;
import net.createmod.ponder.api.registration.PonderTagRegistrationHelper;
import net.createmod.ponder.api.registration.SharedTextRegistrationHelper;
import net.minecraft.resources.ResourceLocation;

/** Client only. Registers the Ponder scenes for the Custom Transformer Core. */
public class PGTPonderPlugin implements PonderPlugin {
    @Override
    public String getModId() {
        return PGTransformers.MODID;
    }

    @Override
    public void registerScenes(PonderSceneRegistrationHelper<ResourceLocation> helper) {
        helper.forComponents(PGTransformers.CUSTOM_CORE.getId())
                .addStoryBoard("small", PGTScenes::small)
                .addStoryBoard("large", PGTScenes::large)
                .addStoryBoard("huge", PGTScenes::huge)
                .addStoryBoard("slider", PGTScenes::slider);
    }

    @Override
    public void registerTags(PonderTagRegistrationHelper<ResourceLocation> helper) {
    }

    @Override
    public void registerSharedText(SharedTextRegistrationHelper helper) {
    }

    @Override
    public void onPonderLevelRestore(PonderLevel level) {
    }
}
