package weathfold.partixel.api;

public interface IParticleUpdater {

    default void onWake(Particle p) {}
    default void onUpdate(Particle p) {}

}
