package weathfold.partixel.api;

import com.google.common.base.Objects;
import org.lwjgl.opengl.GL11;

import java.nio.FloatBuffer;

public class PColor {

    /**
     * Lerp the color in RGBA space.
     */
    public static PColor lerp(PColor a, PColor b, float t, PColor result) {
        if (result == null) {
            result = new PColor(0, 0, 0, 0);
        }
        float u = 1 - t;
        result.a = u * a.a + t * b.a;
        result.r = u * a.r + t * b.r;
        result.g = u * a.g + t * b.g;
        result.b = u * a.b + t * b.b;
        return result;
    }

    public static PColor white() {
        return mono(1);
    }

    /**
     * @param hex Color's hex representation in RGB order
     */
    public static PColor hex3(int hex) {
        int r = hex & 0xFF;
        int g = (hex >>> 8) & 0xFF;
        int b = (hex >>> 16) & 0xFF;
        return rgb(r/255.0f, g/255.0f, b/255.0f);
    }

    /**
     * @param hex Color's hex representation in ARGB order
     */
    public static PColor hex4(int hex) {
        int r = hex & 0xFF;
        int g = (hex >>> 8) & 0xFF;
        int b = (hex >>> 16) & 0xFF;
        int a = (hex >>> 24) & 0xFF;
        return rgba(r/255.0f, g/255.0f, b/255.0f, a/255.0f);
    }

    public static PColor rgba(float r, float g, float b, float a) {
        return new PColor(r, g, b, a);
    }

    public static PColor rgb(float r, float g, float b) {
        return rgba(r, g, b, 1);
    }

    public static PColor mono(float lum) {
        return rgb(lum, lum, lum);
    }

    public float r, g, b, a;

    private PColor(float r, float g, float b, float a) {
        this.r = r;
        this.g = g;
        this.b = b;
        this.a = a;
    }

    public void load(PColor other) {
        r = other.r;
        g = other.g;
        b = other.b;
        a = other.a;
    }

    public void store(FloatBuffer buffer) {
        buffer.put(r).put(g).put(b).put(a);
    }

    public void legacyBind() {
        GL11.glColor4f(r, g, b, a);
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("r", r)
                .add("g", g)
                .add("b", b)
                .add("a", a).toString();
    }

}
