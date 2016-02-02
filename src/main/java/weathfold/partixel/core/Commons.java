package weathfold.partixel.core;

import cpw.mods.fml.common.FMLCommonHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;

import java.io.InputStream;
import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL11.*;

/**
 * Unordered generic utils.
 */
public final class Commons {

    public static final boolean DEBUG = true;

    public static final Logger log = LogManager.getLogger("PartixelMod");

    public static void debug(Object obj) {
        if (DEBUG) {
            log.info("[DEBUG]" + obj);
        }
    }

    public static Minecraft mc() {
        return Minecraft.getMinecraft();
    }

    public static EntityClientPlayerMP player() {
        return mc().thePlayer;
    }

    public static boolean hasEnteredGame() {
        return mc().thePlayer != null;
    }

    public static boolean client() {
        return FMLCommonHandler.instance().getEffectiveSide().isClient();
    }

    public static void bindTexture(ResourceLocation texture) {
        mc().getTextureManager().bindTexture(texture);
    }

    public static InputStream getResourceStream(ResourceLocation res) {
        try {
            String domain = res.getResourceDomain(), path = res.getResourcePath();
            return Commons.class.getResourceAsStream("/assets/" + domain + "/" + path);
        } catch(Exception e) {
            throw new RuntimeException("Invalid resource " + res, e);
        }
    }

    public static void acquireModelMatrix(Matrix4f src) {
        acquireMatrix(GL_MODELVIEW_MATRIX, src);
    }

    public static void acquireProjMatrix(Matrix4f src) {
        acquireMatrix(GL_PROJECTION_MATRIX, src);
    }

    public static void acquireMatrix(int matrixType, Matrix4f src) {
        FloatBuffer buffer = BufferUtils.createFloatBuffer(16);
        glGetFloat(matrixType, buffer);
        // buffer.flip();

        src.load(buffer);
    }

    public static void lerp(Vector3f a, Vector3f b, float t, Vector3f dest) {
        float u = 1 - t;
        dest.x = a.x * u + b.x * t;
        dest.y = a.y * u + b.y * t;
        dest.z = a.z * u + b.z * t;
    }

    public static float lerp(float a, float b, float t) {
        return a * (1 - t) + b * t;
    }

    /**
     * Currently is a very naive approach... optimize later
     * a b c are matrices used, should be cached. result is finally in a.
     */
    public static void eularMatrix(float yaw, float pitch, float roll, Matrix4f a, Matrix4f b, Matrix4f c) {
        yaw = toRad(yaw);
        pitch = toRad(pitch);
        roll = toRad(roll);

        float sy = MathHelper.sin(yaw), cy = MathHelper.cos(sy),
                sp = MathHelper.sin(pitch), cp = MathHelper.cos(pitch),
                sr = MathHelper.sin(roll), cr = MathHelper.cos(roll);
        a.setIdentity();
        a.m00 = cr;
        a.m01 = -sr;
        a.m10 = sr;
        a.m11 = cr;

        b.setIdentity();
        b.m00 = cp;
        b.m02 = sp;
        b.m20 = -sp;
        b.m22 = cp;

        Matrix4f.mul(b, a, c);

        b.setIdentity();
        b.m11 = cy;
        b.m12 = -sy;
        b.m21 = sy;
        b.m22 = cy;

        Matrix4f.mul(b, c, a);
    }

    public static void transformMatrix(Matrix4f dest, Vector3f offset) {
        dest.setIdentity();
        dest.m03 = offset.x;
        dest.m13 = offset.y;
        dest.m23 = offset.z;
    }

    public static void scaleMatrix(Matrix4f dest, float scale) {
        dest.setIdentity();
        dest.m00 = dest.m11 = dest.m22 = scale;
    }

    public static float toRad(float angle) {
        return angle * (float) Math.PI / 180;
    }

    private Commons() {}

}
