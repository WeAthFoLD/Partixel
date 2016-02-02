package weathfold.partixel.core;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import weathfold.partixel.api.Pipeline;

@Mod(modid="partixel", name="Partixel", version="0.1")
public final class PartixelMod {

    @EventHandler
    public void preInit(FMLPreInitializationEvent evt) {
        Commons.log.info("PartixelMod is starting.");
    }

    @EventHandler
    public void init(FMLInitializationEvent evt) {
        Pipeline.instance.__init();
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent evt) {
    }

}
