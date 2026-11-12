package com.pgtransformers.siren;

import com.pgtransformers.PGTransformers;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundSource;

/**
 * Client only: looping siren. Loudness follows the supplied voltage; pitch/speed follow the block's slider.
 *
 * Minecraft clamps the gain of a single sound to 1.0 (a "volume" above 1 only stretches the audible range),
 * so one instance can never be louder than the file. To make 500 V genuinely painful we stack three
 * slightly detuned copies of the loop (they add up in the mixer and beat against each other), and play them
 * on the MASTER channel so the player's "Blocks" slider cannot turn them down; only the master volume can.
 *
 * To make it normal-behaved again, swap {@link #CHANNEL} for SoundSource.BLOCKS.
 */
public final class SirenClient {
    private SirenClient() {}

    private static final SoundSource CHANNEL = SoundSource.MASTER;

    /** Voltage fraction (0..1) at which each extra layer starts to fade in, and its pitch ratio. */
    private static final float[] LAYER_START = {0.0f, 0.25f, 0.55f};
    private static final float[] LAYER_PITCH = {1.0f, 1.006f, 0.994f};

    public static void play(IndustrialSirenBlockEntity be) {
        var sounds = Minecraft.getInstance().getSoundManager();
        for (int i = 0; i < LAYER_START.length; i++) {
            sounds.play(new Instance(be, i));
        }
    }

    private static final class Instance extends AbstractTickableSoundInstance {
        private final IndustrialSirenBlockEntity be;
        private final int layer;

        Instance(IndustrialSirenBlockEntity be, int layer) {
            super(PGTransformers.SIREN_SOUND.get(), CHANNEL, SoundInstance.createUnseededRandom());
            this.be = be;
            this.layer = layer;
            this.looping = true;
            this.delay = 0;
            this.volume = 1.0f;
            this.x = be.getBlockPos().getX() + 0.5;
            this.y = be.getBlockPos().getY() + 0.5;
            this.z = be.getBlockPos().getZ() + 0.5;
        }

        @Override
        public boolean canStartSilent() {
            return true;
        }

        @Override
        public void tick() {
            float v = be.getVolume();
            if (be.isRemoved() || v <= 0.0f) {
                if (layer == 0) be.setSoundActive(false);
                stop();
                return;
            }
            // Loud early, maximum at 500 V. Volume above 1 widens the audible range (~100 blocks at the top).
            float shaped = (float) Math.pow(v, 0.6);
            float range = 0.6f + 2.4f * shaped;
            // Extra layers fade in as the voltage climbs, so 500 V is three voices deep.
            float fade = Math.max(0.0f, Math.min(1.0f, (shaped - LAYER_START[layer]) / 0.25f));
            this.volume = Math.max(0.001f, range * fade);
            // Pitch and speed come from the slider only (0.5x..2.0x, the engine's limits); voltage is loudness.
            this.pitch = Math.max(0.5f, Math.min(2.0f, be.getPitch() * LAYER_PITCH[layer]));
        }
    }
}
