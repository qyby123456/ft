/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.lwjgl.BufferUtils
 *  org.lwjgl.opengl.GL
 *  org.lwjgl.opengl.GL11
 *  org.lwjgl.opengl.GL13
 *  org.lwjgl.opengl.GL14
 *  org.lwjgl.opengl.GL15
 *  org.lwjgl.opengl.GL20
 *  org.lwjgl.opengl.GL30
 *  org.lwjgl.opengl.GL33
 */
package net.farmingtales.ftboss.client.render.bloom;

import java.io.IOException;
import java.nio.ByteBuffer;
import net.farmingtales.ftboss.client.render.bloom.SpellBloomSettings;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL14;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL33;

public final class SpellBloomGpu
implements AutoCloseable {
    private final ShaderSource shaders;
    private final int[] framebuffers = new int[5];
    private final int[] textures = new int[5];
    private int extractProgram;
    private int blurProgram;
    private int compositeProgram;
    private int vertexArray;
    private int width;
    private int height;
    private int sceneFramebuffer;
    private int[] sceneViewport;
    private boolean capturing;

    public SpellBloomGpu(ShaderSource shaderSource) {
        this.shaders = shaderSource;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public boolean begin(SpellBloomSettings spellBloomSettings) throws IOException {
        if (this.capturing || !spellBloomSettings.enabled() || spellBloomSettings.strength() <= 0.0f) {
            return false;
        }
        State state = new State();
        if (state.drawFramebuffer == 0 || state.viewport[2] <= 0 || state.viewport[3] <= 0) {
            return false;
        }
        try {
            float f = Math.min(spellBloomSettings.resolutionScale(), Math.min(1600.0f / (float)state.viewport[2], 900.0f / (float)state.viewport[3]));
            int n = Math.max(1, Math.round((float)state.viewport[2] * f));
            int n2 = Math.max(1, Math.round((float)state.viewport[3] * f));
            this.ensure(n, n2);
            this.sceneFramebuffer = state.drawFramebuffer;
            this.sceneViewport = (int[])state.viewport.clone();
            this.capture(0);
            this.capturing = true;
            boolean bl = true;
            return bl;
        }
        finally {
            state.restore();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void finish(SpellBloomSettings spellBloomSettings, boolean bl) {
        if (!this.capturing) {
            return;
        }
        State state = new State();
        try {
            this.capture(1);
            this.prepare();
            this.pass(this.extractProgram, 2, this.textures[0], this.textures[1]);
            GL20.glUniform1f((int)SpellBloomGpu.uniform(this.extractProgram, "Threshold"), (float)spellBloomSettings.threshold());
            SpellBloomGpu.draw();
            float f = 1.0f + spellBloomSettings.radius() * 4.0f;
            this.pass(this.blurProgram, 3, this.textures[2], 0);
            GL20.glUniform2f((int)SpellBloomGpu.uniform(this.blurProgram, "Step"), (float)(f / (float)this.width), (float)0.0f);
            SpellBloomGpu.draw();
            this.pass(this.blurProgram, 4, this.textures[3], 0);
            GL20.glUniform2f((int)SpellBloomGpu.uniform(this.blurProgram, "Step"), (float)0.0f, (float)(f / (float)this.height));
            SpellBloomGpu.draw();
            GL30.glBindFramebuffer((int)36009, (int)this.sceneFramebuffer);
            GL11.glViewport((int)this.sceneViewport[0], (int)this.sceneViewport[1], (int)this.sceneViewport[2], (int)this.sceneViewport[3]);
            GL20.glUseProgram((int)this.compositeProgram);
            SpellBloomGpu.bindTexture(0, this.textures[4]);
            GL20.glUniform1i((int)SpellBloomGpu.uniform(this.compositeProgram, "BloomSampler"), (int)0);
            GL20.glUniform1f((int)SpellBloomGpu.uniform(this.compositeProgram, "Strength"), (float)spellBloomSettings.strength());
            GL20.glUniform1f((int)SpellBloomGpu.uniform(this.compositeProgram, "AimProtection"), (float)(bl ? 1.0f : 0.0f));
            GL20.glUniform1f((int)SpellBloomGpu.uniform(this.compositeProgram, "Aspect"), (float)((float)this.sceneViewport[2] / (float)this.sceneViewport[3]));
            GL11.glEnable((int)3042);
            GL20.glBlendEquationSeparate((int)32774, (int)32774);
            GL14.glBlendFuncSeparate((int)1, (int)1, (int)0, (int)1);
            SpellBloomGpu.draw();
        }
        finally {
            this.capturing = false;
            state.restore();
        }
    }

    private void ensure(int n, int n2) throws IOException {
        if (this.width == n && this.height == n2 && this.extractProgram != 0) {
            return;
        }
        this.close();
        try {
            this.width = n;
            this.height = n2;
            this.extractProgram = this.program("extract.fsh");
            this.blurProgram = this.program("blur.fsh");
            this.compositeProgram = this.program("composite.fsh");
            this.vertexArray = GL30.glGenVertexArrays();
            for (int i = 0; i < this.framebuffers.length; ++i) {
                this.textures[i] = GL11.glGenTextures();
                SpellBloomGpu.bindTexture(0, this.textures[i]);
                GL11.glTexParameteri((int)3553, (int)10241, (int)9729);
                GL11.glTexParameteri((int)3553, (int)10240, (int)9729);
                GL11.glTexParameteri((int)3553, (int)10242, (int)33071);
                GL11.glTexParameteri((int)3553, (int)10243, (int)33071);
                GL15.glBindBuffer((int)35052, (int)0);
                GL11.glTexImage2D((int)3553, (int)0, (int)34842, (int)this.width, (int)this.height, (int)0, (int)6408, (int)5126, (long)0L);
                this.framebuffers[i] = GL30.glGenFramebuffers();
                GL30.glBindFramebuffer((int)36160, (int)this.framebuffers[i]);
                GL30.glFramebufferTexture2D((int)36160, (int)36064, (int)3553, (int)this.textures[i], (int)0);
                if (GL30.glCheckFramebufferStatus((int)36160) == 36053) continue;
                throw new IOException("Spell bloom framebuffer is incomplete");
            }
        }
        catch (IOException | RuntimeException exception) {
            this.close();
            throw exception;
        }
    }

    private void capture(int n) {
        GL11.glDisable((int)3089);
        GL11.glDisable((int)36281);
        GL30.glBindFramebuffer((int)36008, (int)this.sceneFramebuffer);
        GL30.glBindFramebuffer((int)36009, (int)this.framebuffers[n]);
        GL30.glBlitFramebuffer((int)this.sceneViewport[0], (int)this.sceneViewport[1], (int)(this.sceneViewport[0] + this.sceneViewport[2]), (int)(this.sceneViewport[1] + this.sceneViewport[3]), (int)0, (int)0, (int)this.width, (int)this.height, (int)16384, (int)9729);
    }

    private void prepare() {
        GL11.glDisable((int)2929);
        GL11.glDisable((int)2884);
        GL11.glDisable((int)3089);
        GL11.glDisable((int)2960);
        GL11.glDisable((int)3042);
        GL11.glDisable((int)36281);
        GL11.glDepthMask((boolean)false);
        GL11.glColorMask((boolean)true, (boolean)true, (boolean)true, (boolean)true);
        GL30.glBindVertexArray((int)this.vertexArray);
    }

    private void pass(int n, int n2, int n3, int n4) {
        GL30.glBindFramebuffer((int)36009, (int)this.framebuffers[n2]);
        GL11.glViewport((int)0, (int)0, (int)this.width, (int)this.height);
        GL20.glUseProgram((int)n);
        SpellBloomGpu.bindTexture(0, n3);
        GL20.glUniform1i((int)SpellBloomGpu.uniform(n, "DiffuseSampler"), (int)0);
        if (n4 != 0) {
            SpellBloomGpu.bindTexture(1, n4);
            GL20.glUniform1i((int)SpellBloomGpu.uniform(n, "AfterSampler"), (int)1);
        }
    }

    private static void bindTexture(int n, int n2) {
        GL13.glActiveTexture((int)(33984 + n));
        if (GL.getCapabilities().OpenGL33) {
            GL33.glBindSampler((int)n, (int)0);
        }
        GL11.glBindTexture((int)3553, (int)n2);
    }

    private static void draw() {
        GL11.glDrawArrays((int)4, (int)0, (int)3);
    }

    private static int uniform(int n, String string) {
        return GL20.glGetUniformLocation((int)n, (CharSequence)string);
    }

    private int program(String string) throws IOException {
        int n = 0;
        int n2 = 0;
        int n3 = 0;
        try {
            n = SpellBloomGpu.compile(35633, this.shaders.read("fullscreen.vsh"));
            n2 = SpellBloomGpu.compile(35632, this.shaders.read(string));
            n3 = GL20.glCreateProgram();
            GL20.glAttachShader((int)n3, (int)n);
            GL20.glAttachShader((int)n3, (int)n2);
            GL20.glLinkProgram((int)n3);
            if (GL20.glGetProgrami((int)n3, (int)35714) == 0) {
                throw new IOException("Spell bloom link failed: " + GL20.glGetProgramInfoLog((int)n3));
            }
            int n4 = n3;
            return n4;
        }
        catch (IOException | RuntimeException exception) {
            if (n3 != 0) {
                GL20.glDeleteProgram((int)n3);
            }
            throw exception;
        }
        finally {
            if (n != 0) {
                GL20.glDeleteShader((int)n);
            }
            if (n2 != 0) {
                GL20.glDeleteShader((int)n2);
            }
        }
    }

    private static int compile(int n, String string) throws IOException {
        int n2 = GL20.glCreateShader((int)n);
        GL20.glShaderSource((int)n2, (CharSequence)string);
        GL20.glCompileShader((int)n2);
        if (GL20.glGetShaderi((int)n2, (int)35713) == 0) {
            String string2 = GL20.glGetShaderInfoLog((int)n2);
            GL20.glDeleteShader((int)n2);
            throw new IOException("Spell bloom shader failed: " + string2);
        }
        return n2;
    }

    @Override
    public void close() {
        this.capturing = false;
        for (int i = 0; i < this.framebuffers.length; ++i) {
            if (this.framebuffers[i] != 0) {
                GL30.glDeleteFramebuffers((int)this.framebuffers[i]);
            }
            if (this.textures[i] != 0) {
                GL11.glDeleteTextures((int)this.textures[i]);
            }
            this.textures[i] = 0;
            this.framebuffers[i] = 0;
        }
        if (this.extractProgram != 0) {
            GL20.glDeleteProgram((int)this.extractProgram);
        }
        if (this.blurProgram != 0) {
            GL20.glDeleteProgram((int)this.blurProgram);
        }
        if (this.compositeProgram != 0) {
            GL20.glDeleteProgram((int)this.compositeProgram);
        }
        if (this.vertexArray != 0) {
            GL30.glDeleteVertexArrays((int)this.vertexArray);
        }
        this.height = 0;
        this.width = 0;
        this.vertexArray = 0;
        this.compositeProgram = 0;
        this.blurProgram = 0;
        this.extractProgram = 0;
    }

    @FunctionalInterface
    public static interface ShaderSource {
        public String read(String var1) throws IOException;
    }

    private static final class State {
        private final int program = GL11.glGetInteger((int)35725);
        private final int vertexArray = GL11.glGetInteger((int)34229);
        private final int readFramebuffer = GL11.glGetInteger((int)36010);
        private final int drawFramebuffer = GL11.glGetInteger((int)36006);
        private final int activeTexture = GL11.glGetInteger((int)34016);
        private final int unpackBuffer = GL11.glGetInteger((int)35055);
        private final int[] viewport = new int[4];
        private final int[] textureBindings = new int[2];
        private final int[] samplerBindings = new int[2];
        private final boolean blend = GL11.glIsEnabled((int)3042);
        private final boolean depth = GL11.glIsEnabled((int)2929);
        private final boolean cull = GL11.glIsEnabled((int)2884);
        private final boolean scissor = GL11.glIsEnabled((int)3089);
        private final boolean stencil = GL11.glIsEnabled((int)2960);
        private final boolean srgb = GL11.glIsEnabled((int)36281);
        private final boolean depthMask = GL11.glGetBoolean((int)2930);
        private final int sourceRgb = GL11.glGetInteger((int)32969);
        private final int destinationRgb = GL11.glGetInteger((int)32968);
        private final int sourceAlpha = GL11.glGetInteger((int)32971);
        private final int destinationAlpha = GL11.glGetInteger((int)32970);
        private final int equationRgb = GL11.glGetInteger((int)32777);
        private final int equationAlpha = GL11.glGetInteger((int)34877);
        private final ByteBuffer colorMask = BufferUtils.createByteBuffer((int)4);

        private State() {
            GL11.glGetIntegerv((int)2978, (int[])this.viewport);
            GL11.glGetBooleanv((int)3107, (ByteBuffer)this.colorMask);
            for (int i = 0; i < 2; ++i) {
                GL13.glActiveTexture((int)(33984 + i));
                this.textureBindings[i] = GL11.glGetInteger((int)32873);
                if (!GL.getCapabilities().OpenGL33) continue;
                this.samplerBindings[i] = GL11.glGetInteger((int)35097);
            }
            GL13.glActiveTexture((int)this.activeTexture);
        }

        private void restore() {
            GL20.glUseProgram((int)this.program);
            GL30.glBindVertexArray((int)this.vertexArray);
            GL30.glBindFramebuffer((int)36008, (int)this.readFramebuffer);
            GL30.glBindFramebuffer((int)36009, (int)this.drawFramebuffer);
            GL11.glViewport((int)this.viewport[0], (int)this.viewport[1], (int)this.viewport[2], (int)this.viewport[3]);
            for (int i = 0; i < 2; ++i) {
                GL13.glActiveTexture((int)(33984 + i));
                GL11.glBindTexture((int)3553, (int)this.textureBindings[i]);
                if (!GL.getCapabilities().OpenGL33) continue;
                GL33.glBindSampler((int)i, (int)this.samplerBindings[i]);
            }
            GL13.glActiveTexture((int)this.activeTexture);
            GL15.glBindBuffer((int)35052, (int)this.unpackBuffer);
            GL14.glBlendFuncSeparate((int)this.sourceRgb, (int)this.destinationRgb, (int)this.sourceAlpha, (int)this.destinationAlpha);
            GL20.glBlendEquationSeparate((int)this.equationRgb, (int)this.equationAlpha);
            State.toggle(3042, this.blend);
            State.toggle(2929, this.depth);
            State.toggle(2884, this.cull);
            State.toggle(3089, this.scissor);
            State.toggle(2960, this.stencil);
            State.toggle(36281, this.srgb);
            GL11.glDepthMask((boolean)this.depthMask);
            GL11.glColorMask((this.colorMask.get(0) != 0 ? 1 : 0) != 0, (this.colorMask.get(1) != 0 ? 1 : 0) != 0, (this.colorMask.get(2) != 0 ? 1 : 0) != 0, (this.colorMask.get(3) != 0 ? 1 : 0) != 0);
        }

        private static void toggle(int n, boolean bl) {
            if (bl) {
                GL11.glEnable((int)n);
            } else {
                GL11.glDisable((int)n);
            }
        }
    }
}

