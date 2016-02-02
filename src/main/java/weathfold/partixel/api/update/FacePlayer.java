package weathfold.partixel.api.update;

import net.minecraft.entity.player.EntityPlayer;
import org.lwjgl.util.vector.Vector3f;
import weathfold.partixel.api.IParticleUpdater;
import weathfold.partixel.api.Particle;
import static weathfold.partixel.core.Commons.*;

public class FacePlayer implements IParticleUpdater {

    @Override
    public void onWake(Particle p) {
        set(p);
    }

    @Override
    public void onUpdate(Particle p) {
        set(p);
    }

    private void set(Particle p) {
        EntityPlayer player =  player();
        Vector3f pos = new Vector3f();
        Vector3f.add(p.position, p.system.position, pos);

        if (!p.system.localSpace()) {
            pos.x -= player.posX;
            pos.y -= player.posY;
            pos.z -= player.posZ;
        }

        float yaw = (float)(Math.toDegrees(Math.atan2(pos.x, pos.z)));
        float pitch =-(float)(Math.toDegrees(Math.atan2(pos.y, Math.sqrt(pos.x * pos.x + pos.z * pos.z))));

        p.rotation.y = yaw;
        p.rotation.x = pitch;
    }

}
