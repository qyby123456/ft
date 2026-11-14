package net.farmingtales.ftboss.client.render.bloom;

import java.io.IOException;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL14;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL21;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL33;

public final class SpellBloomGpu implements AutoCloseable {
    @FunctionalInterface
    public interface ShaderSource {
        String read(String name) throws IOException;
    }

    private final ShaderSource shaders;
    private final int[] framebuffers = new int[5];
    private final int[] textures = new int[5];
    private int extractProgram, blurProgram, compositeProgram, vertexArray;
    private int width, height, sceneFramebuffer;
    private int[] sceneViewport;
    private boolean capturing;

    public SpellBloomGpu(ShaderSource shaders) {
        this.shaders = shaders;
    }

    public boolean begin(SpellBloomSettings settings) throws IOException {
        if (capturing || !settings.enabled() || settings.strength() <= 0) return false;
        State state = new State();
        if (state.drawFramebuffer == 0 || state.viewport[2] <= 0 || state.viewport[3] <= 0) return false;
        try {
            float scale = Math.min(settings.resolutionScale(), Math.min(1600F / state.viewport[2], 900F / state.viewport[3]));
            int targetWidth = Math.max(1, Math.round(state.viewport[2] * scale));
            int targetHeight = Math.max(1, Math.round(state.viewport[3] * scale));
            ensure(targetWidth, targetHeight);
            sceneFramebuffer = state.drawFramebuffer;
            sceneViewport = state.viewport.clone();
            capture(0);
            capturing = true;
            return true;
        } finally {
            state.restore();
        }
    }

    public void finish(SpellBloomSettings settings, boolean firstPerson) {
        if (!capturing) return;
        State state = new State();
        try {
            capture(1);
            prepare();
            pass(extractProgram, 2, textures[0], textures[1]);
            GL20.glUniform1f(uniform(extractProgram, "Threshold"), settings.threshold());
            draw();
            float spread = 1 + settings.radius() * 4;
            pass(blurProgram, 3, textures[2], 0);
            GL20.glUniform2f(uniform(blurProgram, "Step"), spread / width, 0);
            draw();
            pass(blurProgram, 4, textures[3], 0);
            GL20.glUniform2f(uniform(blurProgram, "Step"), 0, spread / height);
            draw();
            GL30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, sceneFramebuffer);
            GL11.glViewport(sceneViewport[0], sceneViewport[1], sceneViewport[2], sceneViewport[3]);
            GL20.glUseProgram(compositeProgram);
            bindTexture(0, textures[4]);
            GL20.glUniform1i(uniform(compositeProgram, "BloomSampler"), 0);
            GL20.glUniform1f(uniform(compositeProgram, "Strength"), settings.strength());
            GL20.glUniform1f(uniform(compositeProgram, "AimProtection"), firstPerson ? 1 : 0);
            GL20.glUniform1f(uniform(compositeProgram, "Aspect"), (float)sceneViewport[2] / sceneViewport[3]);
            GL11.glEnable(GL11.GL_BLEND);
            GL20.glBlendEquationSeparate(GL14.GL_FUNC_ADD, GL14.GL_FUNC_ADD);
            GL14.glBlendFuncSeparate(GL11.GL_ONE, GL11.GL_ONE, GL11.GL_ZERO, GL11.GL_ONE);
            draw();
        } finally {
            capturing = false;
            state.restore();
        }
    }

    private void ensure(int targetWidth, int targetHeight) throws IOException {
        if (width == targetWidth && height == targetHeight && extractProgram != 0) return;
        close();
        try {
            width = targetWidth;
            height = targetHeight;
            extractProgram = program("extract.fsh");
            blurProgram = program("blur.fsh");
            compositeProgram = program("composite.fsh");
            vertexArray = GL30.glGenVertexArrays();
            for (int index = 0; index < framebuffers.length; index++) {
                textures[index] = GL11.glGenTextures();
                bindTexture(0, textures[index]);
                GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
                GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
                GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL13.GL_CLAMP_TO_EDGE);
                GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL13.GL_CLAMP_TO_EDGE);
                GL15.glBindBuffer(GL21.GL_PIXEL_UNPACK_BUFFER, 0);
                GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL30.GL_RGBA16F, width, height, 0, GL11.GL_RGBA, GL11.GL_FLOAT, 0L);
                framebuffers[index] = GL30.glGenFramebuffers();
                GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, framebuffers[index]);
                GL30.glFramebufferTexture2D(GL30.GL_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT0, GL11.GL_TEXTURE_2D, textures[index], 0);
                if (GL30.glCheckFramebufferStatus(GL30.GL_FRAMEBUFFER) != GL30.GL_FRAMEBUFFER_COMPLETE) {
                    throw new IOException("Spell bloom framebuffer is incomplete");
                }
            }
        } catch (IOException | RuntimeException exception) {
            close();
            throw exception;
        }
    }

    private void capture(int target) {
        GL11.glDisable(GL11.GL_SCISSOR_TEST);
        GL11.glDisable(GL30.GL_FRAMEBUFFER_SRGB);
        GL30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, sceneFramebuffer);
        GL30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, framebuffers[target]);
        GL30.glBlitFramebuffer(sceneViewport[0], sceneViewport[1], sceneViewport[0] + sceneViewport[2],
                sceneViewport[1] + sceneViewport[3], 0, 0, width, height, GL11.GL_COLOR_BUFFER_BIT, GL11.GL_LINEAR);
    }

    private void prepare() {
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glDisable(GL11.GL_CULL_FACE);
        GL11.glDisable(GL11.GL_SCISSOR_TEST);
        GL11.glDisable(GL11.GL_STENCIL_TEST);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glDisable(GL30.GL_FRAMEBUFFER_SRGB);
        GL11.glDepthMask(false);
        GL11.glColorMask(true, true, true, true);
        GL30.glBindVertexArray(vertexArray);
    }

    private void pass(int program, int target, int first, int second) {
        GL30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, framebuffers[target]);
        GL11.glViewport(0, 0, width, height);
        GL20.glUseProgram(program);
        bindTexture(0, first);
        GL20.glUniform1i(uniform(program, "DiffuseSampler"), 0);
        if (second != 0) {
            bindTexture(1, second);
            GL20.glUniform1i(uniform(program, "AfterSampler"), 1);
        }
    }

    private static void bindTexture(int unit, int texture) {
        GL13.glActiveTexture(GL13.GL_TEXTURE0 + unit);
        if (GL.getCapabilities().OpenGL33) GL33.glBindSampler(unit, 0);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture);
    }

    private static void draw() {
        GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, 3);
    }

    private static int uniform(int program, String name) {
        return GL20.glGetUniformLocation(program, name);
    }

    private int program(String fragment) throws IOException {
        int vertex = 0, pixel = 0, program = 0;
        try {
            vertex = compile(GL20.GL_VERTEX_SHADER, shaders.read("fullscreen.vsh"));
            pixel = compile(GL20.GL_FRAGMENT_SHADER, shaders.read(fragment));
            program = GL20.glCreateProgram();
            GL20.glAttachShader(program, vertex);
            GL20.glAttachShader(program, pixel);
            GL20.glLinkProgram(program);
            if (GL20.glGetProgrami(program, GL20.GL_LINK_STATUS) == GL11.GL_FALSE) {
                throw new IOException("Spell bloom link failed: " + GL20.glGetProgramInfoLog(program));
            }
            return program;
        } catch (IOException | RuntimeException exception) {
            if (program != 0) GL20.glDeleteProgram(program);
            throw exception;
        } finally {
            if (vertex != 0) GL20.glDeleteShader(vertex);
            if (pixel != 0) GL20.glDeleteShader(pixel);
        }
    }

    private static int compile(int type, String source) throws IOException {
        int shader = GL20.glCreateShader(type);
        GL20.glShaderSource(shader, source);
        GL20.glCompileShader(shader);
        if (GL20.glGetShaderi(shader, GL20.GL_COMPILE_STATUS) == GL11.GL_FALSE) {
            String diagnostic = GL20.glGetShaderInfoLog(shader);
            GL20.glDeleteShader(shader);
            throw new IOException("Spell bloom shader failed: " + diagnostic);
        }
        return shader;
    }

    @Override
    public void close() {
        capturing = false;
        for (int index = 0; index < framebuffers.length; index++) {
            if (framebuffers[index] != 0) GL30.glDeleteFramebuffers(framebuffers[index]);
            if (textures[index] != 0) GL11.glDeleteTextures(textures[index]);
            framebuffers[index] = textures[index] = 0;
        }
        if (extractProgram != 0) GL20.glDeleteProgram(extractProgram);
        if (blurProgram != 0) GL20.glDeleteProgram(blurProgram);
        if (compositeProgram != 0) GL20.glDeleteProgram(compositeProgram);
        if (vertexArray != 0) GL30.glDeleteVertexArrays(vertexArray);
        extractProgram = blurProgram = compositeProgram = vertexArray = width = height = 0;
    }

    private static final class State {
        private final int program = GL11.glGetInteger(GL20.GL_CURRENT_PROGRAM);
        private final int vertexArray = GL11.glGetInteger(GL30.GL_VERTEX_ARRAY_BINDING);
        private final int readFramebuffer = GL11.glGetInteger(GL30.GL_READ_FRAMEBUFFER_BINDING);
        private final int drawFramebuffer = GL11.glGetInteger(GL30.GL_DRAW_FRAMEBUFFER_BINDING);
        private final int activeTexture = GL11.glGetInteger(GL13.GL_ACTIVE_TEXTURE);
        private final int unpackBuffer = GL11.glGetInteger(GL21.GL_PIXEL_UNPACK_BUFFER_BINDING);
        private final int[] viewport = new int[4], textureBindings = new int[2], samplerBindings = new int[2];
        private final boolean blend = GL11.glIsEnabled(GL11.GL_BLEND), depth = GL11.glIsEnabled(GL11.GL_DEPTH_TEST);
        private final boolean cull = GL11.glIsEnabled(GL11.GL_CULL_FACE), scissor = GL11.glIsEnabled(GL11.GL_SCISSOR_TEST);
        private final boolean stencil = GL11.glIsEnabled(GL11.GL_STENCIL_TEST), srgb = GL11.glIsEnabled(GL30.GL_FRAMEBUFFER_SRGB);
        private final boolean depthMask = GL11.glGetBoolean(GL11.GL_DEPTH_WRITEMASK);
        private final int sourceRgb = GL11.glGetInteger(GL14.GL_BLEND_SRC_RGB), destinationRgb = GL11.glGetInteger(GL14.GL_BLEND_DST_RGB);
        private final int sourceAlpha = GL11.glGetInteger(GL14.GL_BLEND_SRC_ALPHA), destinationAlpha = GL11.glGetInteger(GL14.GL_BLEND_DST_ALPHA);
        private final int equationRgb = GL11.glGetInteger(GL20.GL_BLEND_EQUATION_RGB), equationAlpha = GL11.glGetInteger(GL20.GL_BLEND_EQUATION_ALPHA);
        private final java.nio.ByteBuffer colorMask = org.lwjgl.BufferUtils.createByteBuffer(4);

        private State() {
            GL11.glGetIntegerv(GL11.GL_VIEWPORT, viewport);
            GL11.glGetBooleanv(GL11.GL_COLOR_WRITEMASK, colorMask);
            for (int unit = 0; unit < 2; unit++) {
                GL13.glActiveTexture(GL13.GL_TEXTURE0 + unit);
                textureBindings[unit] = GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D);
                if (GL.getCapabilities().OpenGL33) samplerBindings[unit] = GL11.glGetInteger(GL33.GL_SAMPLER_BINDING);
            }
            GL13.glActiveTexture(activeTexture);
        }

        private void restore() {
            GL20.glUseProgram(program);
            GL30.glBindVertexArray(vertexArray);
            GL30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, readFramebuffer);
            GL30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, drawFramebuffer);
            GL11.glViewport(viewport[0], viewport[1], viewport[2], viewport[3]);
            for (int unit = 0; unit < 2; unit++) {
                GL13.glActiveTexture(GL13.GL_TEXTURE0 + unit);
                GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureBindings[unit]);
                if (GL.getCapabilities().OpenGL33) GL33.glBindSampler(unit, samplerBindings[unit]);
            }
            GL13.glActiveTexture(activeTexture);
            GL15.glBindBuffer(GL21.GL_PIXEL_UNPACK_BUFFER, unpackBuffer);
            GL14.glBlendFuncSeparate(sourceRgb, destinationRgb, sourceAlpha, destinationAlpha);
            GL20.glBlendEquationSeparate(equationRgb, equationAlpha);
            toggle(GL11.GL_BLEND, blend);
            toggle(GL11.GL_DEPTH_TEST, depth);
            toggle(GL11.GL_CULL_FACE, cull);
            toggle(GL11.GL_SCISSOR_TEST, scissor);
            toggle(GL11.GL_STENCIL_TEST, stencil);
            toggle(GL30.GL_FRAMEBUFFER_SRGB, srgb);
            GL11.glDepthMask(depthMask);
            GL11.glColorMask(colorMask.get(0) != 0, colorMask.get(1) != 0, colorMask.get(2) != 0, colorMask.get(3) != 0);
        }

        private static void toggle(int capability, boolean enabled) {
            if (enabled) GL11.glEnable(capability); else GL11.glDisable(capability);
        }
    }
}
