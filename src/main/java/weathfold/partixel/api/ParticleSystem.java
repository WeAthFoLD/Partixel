package weathfold.partixel.api;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.util.vector.Vector3f;

import java.util.*;
import java.util.function.Supplier;
import static weathfold.partixel.core.Commons.*;
import static org.lwjgl.opengl.GL11.*;

public class ParticleSystem {

    public static class Builder {

        private final List<Function<Particle, ? extends ParticleAttr>> requiredAttr = new ArrayList<>();
        private final List<IParticleUpdater> updaters = new ArrayList<>();
        private boolean consistent = true;
        private int maxPoolSize = 2500;
        private ResourceLocation texture = new ResourceLocation("N/A");
        private int rows = 1, cols = 1, frames = 1;
        private float initSize = 1;
        private boolean localSpace = false;

        public <T extends ParticleAttr> Builder requireAttr(Function<Particle, T> supplier) {
            requiredAttr.add(supplier);
            return this;
        }

        public Builder process(IParticleUpdater updater) {
            updaters.add(updater);
            return this;
        }

        public Builder poolSize(int newPoolSize) {
            Preconditions.checkArgument(newPoolSize > 0 && newPoolSize < 20000);
            maxPoolSize = newPoolSize;
            return this;
        }

        public Builder nonConsistent() {
            consistent = false;
            return this;
        }

        public Builder texture(ResourceLocation texture) {
            this.texture = texture;
            return this;
        }

        public Builder animated(int rows, int cols, int frames) {
            this.rows = rows;
            this.cols = cols;
            this.frames = frames;
            return this;
        }

        public Builder initSize(float size) {
            initSize = size;
            return this;
        }

        public Builder localSpace() {
            localSpace = true;
            return this;
        }

        public ParticleSystem build() {
            ParticleSystem ps = new ParticleSystem();

            ps.maxPoolSize = maxPoolSize;
            ps.requiredAttr = requiredAttr;
            ps.updaters = updaters.toArray(new IParticleUpdater[updaters.size()]);
            ps.consistent = consistent;
            ps.texture = texture;
            ps.texRows = rows;
            ps.texCols = cols;
            ps.maxFrame = frames;
            ps.initSize = initSize;
            ps.localSpace = localSpace;

            return ps;
        }

    }

    public static Builder builder() {
        return new Builder();
    }

    // FINAL ATTRS
    private int maxPoolSize;
    private List<Function<Particle, ? extends ParticleAttr>> requiredAttr;
    private IParticleUpdater updaters[];
    private boolean consistent;
    private ResourceLocation texture;
    private float initSize;

    private int texRows, texCols;
    private int maxFrame;

    private boolean localSpace;
    //

    // Stupid java getters
    public int maxFrame() { return maxFrame; }
    public int rows() { return texRows; }
    public int cols() { return texCols; }
    public ResourceLocation texture() { return texture; }
    public float initSize() { return initSize; }
    public boolean localSpace() { return localSpace; }

    /**
     * Global offset of particles
     */
    public final Vector3f position = new Vector3f();

    private List<Particle> alive = new LinkedList<>();
    private List<Particle> pooled = new LinkedList<>();
    private List<Particle> toWake = new ArrayList<>();

    ParticleSystem() {}

    /**
     * @return A particle object not in use. Might be pooled or newly created.
     */
    public Particle idle() {
        if (pooled.isEmpty()) {
            return createParticle();
        } else {
            Particle ret = pooled.remove(0);
            ret.reset();
            return ret;
        }
    }

    /**
     * Quick alias for waking up an particle acquired by {@link #idle()} and returning it.
     */
    public Particle wakeNew() {
        Particle p = idle();
        wake(p);
        return p;
    }

    /**
     * Wake the given particle.
     * The particle will be made alive in the next tick. <br>
     *
     * If the particle isn't created with this ParticleSystem, or is already alive, yields undefined result.
     */
    public void wake(Particle p) {
        toWake.add(p);
    }

    private void __realWake(Particle p) {
        for (IParticleUpdater updater : updaters) {
            updater.onWake(p);
        }
        p.onWake();
    }

    public boolean isConsistent() {
        return consistent;
    }

    private void updateParticle(Particle p) {
        for (IParticleUpdater updater : updaters) {
            updater.onUpdate(p);
        }
        p.onUpdate();
    }

    private Particle createParticle() {
        Particle p = new Particle(this);

        // Construct attrs
        for (Function<Particle, ? extends ParticleAttr> sup : requiredAttr) {
            ParticleAttr attr = sup.apply(p);

            p.attributes.put(attr.getClass(), attr);
        }

        return p;
    }

    public void __tick() {
        // debug("Ticking");

        List<Particle> keptDead = new ArrayList<>();
        int keptDeadLeft = maxPoolSize - alive.size();

        { // Update and disposal
            Iterator<Particle> iter = alive.iterator();
            while (iter.hasNext()) {
                Particle p = iter.next();
                if (p.disposed) {
                    --keptDeadLeft;
                    if (keptDeadLeft > 0) {
                        keptDead.add(p);
                    }
                } else {
                    updateParticle(p);
                }
            }
        }
        { // wake particles
            for (Particle p : toWake) {
                __realWake(p);
            }
            alive.addAll(toWake);
            toWake.clear();
        }
        { // Pooling
            pooled.addAll(keptDead);
        }
    }

    Collection<Particle> getParticlesToDraw() {
        return alive;
    }
}
