package weathfold.partixel.core.render.impl;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import org.lwjgl.BufferUtils;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;
import weathfold.partixel.api.Particle;
import weathfold.partixel.api.ParticleSystem;
import weathfold.partixel.core.ShaderProgram;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.Collection;

import static org.lwjgl.opengl.GL13.glActiveTexture;
import static weathfold.partixel.core.Commons.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.opengl.GL31.*;
import static org.lwjgl.opengl.GL33.*;

public class ModernGLImpl implements RenderImpl {

    final Matrix4f
            modelMatrix = new Matrix4f(),
            projMatrix = new Matrix4f();

    final int
        fbytes = Float.BYTES,
        isize = 22,
        ibytes = fbytes*isize;

    final Shader shader = new Shader();

    int vao;
    int vbo, ibo;

    int instanceVBO;

    private Vector3f v(float x, float y, float z) {
        return new Vector3f(x, y, z);
    }

    @Override
    public void init() {
        vao = glGenVertexArrays();
        vbo = glGenBuffers();
        ibo = glGenBuffers();
        instanceVBO = glGenBuffers();

        FloatBuffer vertBuffer = BufferUtils.createFloatBuffer(5 * 4);
        vertBuffer.put(new float[] { // A standard quad
                -0.5f, 0.5f, 0.3f, 0f, 0f,
                -0.5f, -0.5f, 0.3f, 0f, 1f,
                0.5f, -0.5f, 0.3f, 1f, 1f,
                0.5f, 0.5f, 0.3f, 1f, 0f
        });
        vertBuffer.flip();

        ByteBuffer indBuffer = BufferUtils.createByteBuffer(6);
        byte[] indices = new byte[] {
            0, 1, 2, 0, 2, 3
        };
        indBuffer.put(indices);
        indBuffer.flip();

        shader.init();

        glBindVertexArray(vao);
        {
            glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ibo);
            glBufferData(GL_ELEMENT_ARRAY_BUFFER, indBuffer, GL_STATIC_DRAW);

            glBindBuffer(GL_ARRAY_BUFFER, vbo);
            glBufferData(GL_ARRAY_BUFFER, vertBuffer, GL_STATIC_DRAW);

            glVertexAttribPointer(0, 3, GL_FLOAT, false, fbytes*5, 0);
            glEnableVertexAttribArray(0);

            glVertexAttribPointer(1, 2, GL_FLOAT, false, fbytes*5, fbytes*3);
            glEnableVertexAttribArray(1);

            glBindBuffer(GL_ARRAY_BUFFER, instanceVBO);
            // iUVBegin
            glVertexAttribPointer(2, 2, GL_FLOAT, false, ibytes, 0);
            glVertexAttribDivisor(2, 1);
            glEnableVertexAttribArray(2);

            // iColor
            glVertexAttribPointer(3, 4, GL_FLOAT, false, ibytes, fbytes*2);
            glVertexAttribDivisor(3, 1);
            glEnableVertexAttribArray(3);

            // iTransform row 0
            glVertexAttribPointer(4, 4, GL_FLOAT, false, ibytes, fbytes*6);
            glVertexAttribDivisor(4, 1);
            glEnableVertexAttribArray(4);

            // iTransform row 1
            glVertexAttribPointer(5, 4, GL_FLOAT, false, ibytes, fbytes*10);
            glVertexAttribDivisor(5, 1);
            glEnableVertexAttribArray(5);

            // iTransform row 2
            glVertexAttribPointer(6, 4, GL_FLOAT, false, ibytes, fbytes*14);
            glVertexAttribDivisor(6, 1);
            glEnableVertexAttribArray(6);

            // iTransform row 3
            glVertexAttribPointer(7, 4, GL_FLOAT, false, ibytes, fbytes*18);
            glVertexAttribDivisor(7, 1);
            glEnableVertexAttribArray(7);

            glBindBuffer(GL_ARRAY_BUFFER, 0);

            glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ibo);
        }
        glBindVertexArray(0);

        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
    }

    @Override
    public void render(ParticleSystem system, Collection<Particle> particles, RenderWorldLastEvent context) {
        final Matrix4f a = new Matrix4f(), b = new Matrix4f(), c = new Matrix4f();
        final float partialTicks = context.partialTicks;
        final boolean local = system.localSpace();
        final int rows = system.rows(), cols = system.cols();
        final float uSpan = 1.0f / system.cols(), vSpan = 1.0f / system.rows();
        final EntityPlayer player = player();

        float dx, dy, dz;
        if (local) {
            dx = dy = dz = 0;
        } else {
            dx = -lerp((float) player.lastTickPosX, (float) player.posX, partialTicks);
            dy = -lerp((float) player.lastTickPosY, (float) player.posY, partialTicks);
            dz = -lerp((float) player.lastTickPosZ, (float) player.posZ, partialTicks);
        }
        dx += system.position.x;
        dy += system.position.y;
        dz += system.position.z;

        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        acquireProjMatrix(projMatrix);
        acquireModelMatrix(modelMatrix);

        shader.useProgram();

        // Update uniform
        shader.updateMatrices(projMatrix, modelMatrix);
        shader.updateTexSpan(uSpan, vSpan);

        // Update instance buffer
        {
            Vector3f tempPosition = new Vector3f(),
                tempRotation = new Vector3f();
            FloatBuffer instanceBuffer = BufferUtils.createFloatBuffer(isize * particles.size());
            for (Particle p : particles) {
                lerp(p.lastTickPosition, p.position, partialTicks, tempPosition);
                tempPosition.x += dx;
                tempPosition.y += dy;
                tempPosition.z += dz;

                lerp(p.lastTickRotation, p.rotation, partialTicks, tempRotation);

                int row = p.frame / cols,
                        col = p.frame - row * cols;

                // a = Rot
                eularMatrix(tempRotation.x, tempRotation.y, tempRotation.z, a, b, c);
                // b = Scale
                scaleMatrix(b, p.size);
                // c = Scale * Rot
                Matrix4f.mul(a, b, c);
                // b = Trans
                transformMatrix(b, tempPosition);
                // a = Trans * Scale * Rot
                Matrix4f.mul(c, b, a);

                // iUVBegin
                instanceBuffer.put((float) row / rows);
                instanceBuffer.put((float) col / cols);

                // iColor
                p.color.store(instanceBuffer);

                // iTransform
                a.storeTranspose(instanceBuffer);
            }

            instanceBuffer.flip();
            glBindBuffer(GL_ARRAY_BUFFER, instanceVBO);
            glBufferData(GL_ARRAY_BUFFER, instanceBuffer, GL_DYNAMIC_DRAW);
            glBindBuffer(GL_ARRAY_BUFFER, 0);
        }
        //

        glBindVertexArray(vao);

        glActiveTexture(GL_TEXTURE0);
        bindTexture(system.texture());

        glDisable(GL_CULL_FACE);

        glDrawElementsInstanced(GL_TRIANGLES, 6, GL_UNSIGNED_BYTE, 0, particles.size());

        glEnable(GL_CULL_FACE);

        glBindVertexArray(0);

        glUseProgram(0);
    }
}

class Shader extends ShaderProgram {

    private int posWVPMatrix, posUVSpan;

    public void init() {
        linkShader(new ResourceLocation("partixel:shaders/particle.vert"), GL_VERTEX_SHADER);
        linkShader(new ResourceLocation("partixel:shaders/particle.frag"), GL_FRAGMENT_SHADER);

        compile();

        if (!isValid()) {
            throw new RuntimeException("Compilation failure");
        }

        int programID = getProgramID();
        posWVPMatrix = glGetUniformLocation(programID, "uWVPMatrix");
        posUVSpan = glGetUniformLocation(programID, "uUVSize");

        int posTexture = glGetUniformLocation(programID, "uTexture");
        useProgram();

        glUniform1i(posTexture, 0);

        glUseProgram(0);
    }

    public void updateMatrices(Matrix4f proj, Matrix4f model) {
        Matrix4f wvp = Matrix4f.mul(proj, model, null);

        FloatBuffer buffer = BufferUtils.createFloatBuffer(16);

        wvp.store(buffer);
        buffer.flip();
        glUniformMatrix4(posWVPMatrix, false, buffer);
    }

    public void updateTexSpan(float uspan, float vspan) {
        glUniform2f(posUVSpan, uspan, vspan);
    }

}
