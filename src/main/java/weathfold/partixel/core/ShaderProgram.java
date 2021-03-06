package weathfold.partixel.core;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

import static weathfold.partixel.core.Commons.*;

/**
 * A simple GL Shader Program wrapper.
 * @author WeAthFolD
 */
@SideOnly(Side.CLIENT)
public class ShaderProgram {

    static Map<ResourceLocation, Integer> loadedShaders = new HashMap<>();
    public static void releaseResources() {
        for(Integer e : loadedShaders.values())
            glDeleteShader(e);
        loadedShaders.clear();
    }

    private boolean compiled = false;
    private boolean valid = false;
    private int programID;
    private List<Integer> attachedShaders = new ArrayList<>();

    public ShaderProgram() {
        programID = glCreateProgram();
    }

    public void linkShader(ResourceLocation location, int type) {
        if (!checkCapability())
            return;

        try {
            int shaderID;

            boolean loaded;
            if(loadedShaders.containsKey(location)) {
                shaderID = loadedShaders.get(location);
                loaded = true;
            } else {
                String str = IOUtils.toString(getResourceStream(location));
                shaderID = glCreateShader(type);
                glShaderSource(shaderID, str);
                glCompileShader(shaderID);

                int successful = glGetShaderi(shaderID, GL_COMPILE_STATUS);
                if(successful == GL_FALSE) {
                    String errmsg = glGetShaderInfoLog(shaderID, glGetShaderi(shaderID, GL_INFO_LOG_LENGTH));
                    log.error("Error when linking shader '" + location + "'. code: " + successful + ", Error string: \n" + errmsg);
                    loaded = false;
                } else {
                    loaded = true;
                }
            }

            if (loaded) {
                attachedShaders.add(shaderID);
                glAttachShader(programID, shaderID);
            }
        } catch (IOException e) {
            log.error("Didn't find shader " + location, e);
            throw new RuntimeException();
        }
    }

    public int getProgramID() {
        return programID;
    }

    public void useProgram() {
        if(compiled && valid) {
            glUseProgram(programID);
        } else if (!compiled) {
            log.error("Trying to use a uncompiled program");
            throw new RuntimeException();
        } // not valid, ignore the shader usage
    }

    public int getUniformLocation(String name) {
        return glGetUniformLocation(getProgramID(), name);
    }

    public void compile() {
        if (!checkCapability()) {
            compiled = true;
            return;
        }

        if(compiled) {
           log.error("Trying to compile shader " + this + " twice.");
            throw new RuntimeException();
        }

        glLinkProgram(programID);

        for(Integer i : attachedShaders)
            glDetachShader(programID, i);
        attachedShaders = null;

        int status = glGetProgrami(programID, GL_LINK_STATUS);
        if(status == GL_FALSE) {
            String errmsg = glGetProgramInfoLog(programID, glGetProgrami(programID, GL_INFO_LOG_LENGTH));
            log.error("Error when linking program #" + programID + ". Error code: " + status + ", Error string: ");
            log.error(errmsg);
            valid = false;
        } else {
            valid = true;
        }

        compiled = true;
    }

    public boolean isValid() {
        return valid;
    }

    private boolean checkCapability() {
        String versionShort = GL11.glGetString(GL11.GL_VERSION).trim().substring(0, 3);
        return "2.1".compareTo(versionShort) <= 0;
    }

    /**
     * Get the src of a shader in lambdalib namespace.
     */
    public static ResourceLocation getShader(String name) {
        return new ResourceLocation("lambdalib:shaders/" + name);
    }

}
