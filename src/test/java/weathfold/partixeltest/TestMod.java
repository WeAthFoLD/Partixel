package weathfold.partixeltest;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.util.vector.Vector3f;
import weathfold.partixel.api.*;
import weathfold.partixel.api.update.FacePlayer;

import java.util.Random;

@Mod(modid="partixel_test", name="PartixelTest", version="0.1", dependencies="required-after:partixel")
public class TestMod {

    Random rand = new Random();

    private float uni() {
        return 2 * (rand.nextFloat() - 0.5f);
    }

    class Fun extends ParticleAttr {
        int deltaTime;
        Vector3f initPosition;

        public Fun(Particle p) { super(p); }
    }

    @EventHandler
    public void init(FMLInitializationEvent evt) {
        PColor color1 = PColor.hex3(0xff4d4d),
                color2 = PColor.hex4(0x4db2ff);

        ParticleSystem system = ParticleSystem.builder()
                .texture(new ResourceLocation("pxtest:textures/test.png"))
                .process(new FacePlayer())
                .requireAttr(Fun::new)
                .process(new IParticleUpdater() {

                    @Override
                    public void onWake(Particle p) {
                        p.size = 0.4f + rand.nextFloat() * 0.4f;
                        PColor.lerp(color1, color2, rand.nextFloat(), p.color);
                        p.color.a = 0.7f + rand.nextFloat() * 0.3f;

                        Fun dt = p.getAttr(Fun.class);
                        dt.deltaTime = rand.nextInt(5000);
                        dt.initPosition = new Vector3f(p.position);
                    }

                    @Override
                    public void onUpdate(Particle p) {
                        Fun attr = p.getAttr(Fun.class);

                        int cycle = 5000;
                        float time = ((System.currentTimeMillis() - attr.deltaTime) % cycle) / (float) cycle;

                        p.position.y = attr.initPosition.y +
                                0.4f * MathHelper.sin(time * 2 * (float) Math.PI);
                    }
                })
                .build();

        Pipeline.instance.register(system);

        for (int i = 0; i < 30000; ++i) {
            float y = 50 + uni() * 40;
            float x = uni() * 100;
            float z = uni() * 100;

            system.wakeNew().position.set(x, y, z);
        }
    }

}
