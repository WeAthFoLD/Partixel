package weathfold.partixel.api;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent.PlayerChangedDimensionEvent;
import cpw.mods.fml.common.gameevent.TickEvent.ClientTickEvent;
import cpw.mods.fml.common.gameevent.TickEvent.Phase;
import cpw.mods.fml.common.network.FMLNetworkEvent.ClientDisconnectionFromServerEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.MinecraftForge;
import weathfold.partixel.core.render.impl.LegacyGLImpl;
import weathfold.partixel.core.render.impl.ModernGLImpl;
import weathfold.partixel.core.render.impl.RenderImpl;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import static org.lwjgl.opengl.GL11.*;
import static weathfold.partixel.core.Commons.*;

public enum Pipeline {
    instance;

    private List<ParticleSystem> systems = new LinkedList<>();

    private RenderImpl renderImpl;

    public void register(ParticleSystem system) {
        systems.add(system);
    }

    public void remove(ParticleSystem system) {
        systems.remove(system);
    }

    public void __init() {
        FMLCommonHandler.instance().bus().register(this);
        MinecraftForge.EVENT_BUS.register(this);

        // Choose render implementation
        String glVersion = glGetString(GL_VERSION);
        boolean modernSuccess = false;
        if ("3.0".compareTo(glVersion) <= 0) {
            renderImpl = new ModernGLImpl();
            modernSuccess = true;
        }

        if (!modernSuccess) {
            renderImpl = new LegacyGLImpl();
        }
    }

    private void checkConsistent() {
        Iterator<ParticleSystem> iter = systems.iterator();
        while (iter.hasNext()) {
            ParticleSystem sys = iter.next();
            if (!sys.isConsistent()) {
                onSystemRemoved(sys);
                iter.remove();
            }
        }
    }

    private void onSystemRemoved(ParticleSystem system) {
        // Preserved
    }

    private boolean init = false;

    @SubscribeEvent
    public void __render(RenderWorldLastEvent evt) {
        if (!init) {
            init = true;
            renderImpl.init();
        }
        for (ParticleSystem system : systems) {
            renderImpl.render(system, system.getParticlesToDraw(), evt);
        }
    }

    @SubscribeEvent
    public void __onClientTick(ClientTickEvent evt) {
        if (evt.phase == Phase.END && hasEnteredGame()) {
            for (ParticleSystem sys : systems) {
                sys.__tick();
            }
        }
    }

    @SubscribeEvent
    public void __onChangeDimension(PlayerChangedDimensionEvent evt) {
        if (client()) {
            checkConsistent();
        }
    }

    @SubscribeEvent
    public void __onDisconnect(ClientDisconnectionFromServerEvent evt) {
        checkConsistent();
    }

}
