package weathfold.partixel.api;

import org.lwjgl.util.vector.Vector3f;

import java.util.*;

public final class Particle {

    public final ParticleSystem system;

    public final Vector3f position = new Vector3f();
    public final Vector3f velocity = new Vector3f();
    public final Vector3f rotation = new Vector3f(); // Rotation in euler angles
    public final PColor  color = PColor.white();
    public float    size;
    public int      frame;

    public boolean  disposed;

    public final Vector3f lastTickPosition = new Vector3f();
    public final Vector3f lastTickRotation = new Vector3f();

    private List<IParticleUpdater> updaterList = new ArrayList<>();
    Map<Class<? extends ParticleAttr>, ParticleAttr> attributes = new HashMap<>();

    Particle(ParticleSystem system) {
        this.system = system;
        reset();
    }

    public Particle process(IParticleUpdater updater) {
        updaterList.add(updater);
        return this;
    }

    @SuppressWarnings("unchecked")
    public <T extends ParticleAttr> T getAttr(Class<T> type) {
        T ret = (T) attributes.get(type);
        Objects.requireNonNull(ret);
        return ret;
    }

    void reset() {
        updaterList.clear();
        disposed = false;
        size = system.initSize();
    }

    void onWake() {
        for (IParticleUpdater updater : updaterList) {
            updater.onWake(this);
        }
    }

    void onUpdate() {
        lastTickPosition.set(position);
        lastTickRotation.set(rotation);

        for (IParticleUpdater updater : updaterList) {
            updater.onUpdate(this);
        }
    }

}
