package weathfold.partixeltest;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import net.minecraft.util.ResourceLocation;
import weathfold.partixel.api.*;
import weathfold.partixel.api.update.FacePlayer;

import java.util.Random;

@Mod(modid="partixel_test", name="PartixelTest", version="0.1", dependencies="required-after:partixel")
public class TestMod {

    Random rand = new Random();

    private float uni() {
        return 2 * (rand.nextFloat() - 0.5f);
    }

    @EventHandler
    public void init(FMLInitializationEvent evt) {


        PColor color1 = PColor.hex3(0xff4d4d),
                color2 = PColor.hex4(0x4db2ff);

        ParticleSystem system = ParticleSystem.builder()
                .texture(new ResourceLocation("pxtest:textures/test.png"))
                .process(new FacePlayer())
                .process(new IParticleUpdater() {
                    @Override
                    public void onWake(Particle p) {
                        p.size = 0.4f + rand.nextFloat() * 0.4f;
                        PColor.lerp(color1, color2, rand.nextFloat(), p.color);
                        p.color.a = 0.5f + rand.nextFloat() * 0.5f;
                    }
                })
                .build();

        Pipeline.instance.register(system);

        for (int i = 0; i < 30000; ++i) {
            float y = 50 + uni() * 40;
            float x = uni() * 100;
            float z = uni() * 100;

            system.spawn().position.set(x, y, z);
        }
    }

}
