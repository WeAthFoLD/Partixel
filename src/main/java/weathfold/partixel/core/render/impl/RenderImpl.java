package weathfold.partixel.core.render.impl;

import net.minecraftforge.client.event.RenderWorldLastEvent;
import weathfold.partixel.api.Particle;
import weathfold.partixel.api.ParticleSystem;

import java.util.Collection;

public interface RenderImpl {

    void init();
    void render(ParticleSystem system,
                Collection<Particle> particles,
                RenderWorldLastEvent context);

}
