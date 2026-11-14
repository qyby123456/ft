/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.Gson
 *  com.mojang.blaze3d.systems.RenderSystem
 *  com.mojang.logging.LogUtils
 *  net.minecraft.client.Minecraft
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraftforge.api.distmarker.Dist
 *  net.minecraftforge.client.event.ClientPlayerNetworkEvent$LoggingOut
 *  net.minecraftforge.client.event.RenderLevelStageEvent
 *  net.minecraftforge.client.event.RenderLevelStageEvent$Stage
 *  net.minecraftforge.eventbus.api.EventPriority
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber
 *  net.minecraftforge.fml.loading.FMLPaths
 *  org.lwjgl.opengl.GL11
 *  org.slf4j.Logger
 */
package net.farmingtales.ftboss.client.render.bloom;

import com.google.gson.Gson;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.logging.LogUtils;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import net.farmingtales.ftboss.client.render.bloom.SpellBloomGpu;
import net.farmingtales.ftboss.client.render.bloom.SpellBloomSettings;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLPaths;
import org.lwjgl.opengl.GL11;
import org.slf4j.Logger;

@Mod.EventBusSubscriber(modid="ftboss", value={Dist.CLIENT})
public final class SpellBloomRenderer {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final SpellBloomGpu GPU = new SpellBloomGpu(SpellBloomRenderer::shader);
    private static SpellBloomSettings settings = SpellBloomSettings.defaults();
    private static boolean settingsLoaded;
    private static boolean failed;
    private static boolean firstPerson;
    private static boolean announced;
    private static int passes;

    private SpellBloomRenderer() {
    }

    @SubscribeEvent(priority=EventPriority.HIGHEST)
    public static void frame(RenderLevelStageEvent renderLevelStageEvent) {
        if (renderLevelStageEvent.getStage() == RenderLevelStageEvent.Stage.AFTER_PARTICLES) {
            passes = 0;
        }
    }

    public static boolean begin(RenderLevelStageEvent renderLevelStageEvent, boolean bl) {
        if (!bl || failed || renderLevelStageEvent.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES || Minecraft.m_91087_().f_91073_ == null || !RenderSystem.isOnRenderThread()) {
            return false;
        }
        SpellBloomRenderer.loadSettings();
        if (!settings.enabled() || settings.strength() <= 0.0f || passes >= settings.maxPasses()) {
            return false;
        }
        if (!settings.allowShaderPacks() && SpellBloomRenderer.shaderPackActive()) {
            return false;
        }
        if (GL11.glGetInteger((int)36006) != Minecraft.m_91087_().m_91385_().f_83920_) {
            return false;
        }
        try {
            firstPerson = !renderLevelStageEvent.getCamera().m_90594_();
            boolean bl2 = GPU.begin(settings);
            if (bl2) {
                ++passes;
                if (!announced) {
                    LOGGER.info("[FTBoss] Spell soft bloom initialized: strength={}, radius={}, threshold={}", new Object[]{Float.valueOf(settings.strength()), Float.valueOf(settings.radius()), Float.valueOf(settings.threshold())});
                    announced = true;
                }
            }
            return bl2;
        }
        catch (IOException | RuntimeException exception) {
            SpellBloomRenderer.disable(exception);
            return false;
        }
    }

    public static void end(boolean bl) {
        if (!bl) {
            return;
        }
        try {
            GPU.finish(settings, firstPerson);
        }
        catch (RuntimeException runtimeException) {
            SpellBloomRenderer.disable(runtimeException);
        }
    }

    private static String shader(String string) throws IOException {
        ResourceLocation resourceLocation = new ResourceLocation("ftboss", "shaders/soft_bloom/" + string);
        try (InputStream inputStream = Minecraft.m_91087_().m_91098_().m_215593_(resourceLocation).m_215507_();){
            String string2 = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
            return string2;
        }
    }

    private static void loadSettings() {
        if (settingsLoaded) {
            return;
        }
        settingsLoaded = true;
        Path path = FMLPaths.CONFIGDIR.get().resolve("ftboss-spell-bloom.json");
        if (!Files.isRegularFile(path, new LinkOption[0])) {
            return;
        }
        try (BufferedReader bufferedReader = Files.newBufferedReader(path, StandardCharsets.UTF_8);){
            SpellBloomSettings spellBloomSettings = (SpellBloomSettings)new Gson().fromJson((Reader)bufferedReader, SpellBloomSettings.class);
            if (spellBloomSettings != null) {
                settings = spellBloomSettings.sanitized();
            }
        }
        catch (IOException | RuntimeException exception) {
            LOGGER.warn("[FTBoss] Invalid spell bloom config; using safe defaults", (Throwable)exception);
            settings = SpellBloomSettings.defaults();
        }
    }

    private static boolean shaderPackActive() {
        for (String string : new String[]{"net.irisshaders.iris.api.v0.IrisApi", "net.coderbot.iris.api.v0.IrisApi"}) {
            try {
                Class<?> clazz = Class.forName(string, false, SpellBloomRenderer.class.getClassLoader());
                Object object = clazz.getMethod("getInstance", new Class[0]).invoke(null, new Object[0]);
                return (Boolean)clazz.getMethod("isShaderPackInUse", new Class[0]).invoke(object, new Object[0]);
            }
            catch (ClassNotFoundException classNotFoundException) {
            }
            catch (LinkageError | ReflectiveOperationException throwable) {
                return true;
            }
        }
        return false;
    }

    private static void disable(Exception exception) {
        failed = true;
        LOGGER.warn("[FTBoss] Spell soft bloom disabled; original rendering retained", (Throwable)exception);
        try {
            GPU.close();
        }
        catch (RuntimeException runtimeException) {
            LOGGER.debug("Spell bloom cleanup failed", (Throwable)runtimeException);
        }
    }

    public static void reset() {
        Runnable runnable = () -> {
            GPU.close();
            passes = 0;
            settings = SpellBloomSettings.defaults();
            announced = false;
            failed = false;
            settingsLoaded = false;
        };
        if (RenderSystem.isOnRenderThread()) {
            runnable.run();
        } else {
            RenderSystem.recordRenderCall(runnable::run);
        }
    }

    @SubscribeEvent
    public static void logout(ClientPlayerNetworkEvent.LoggingOut loggingOut) {
        SpellBloomRenderer.reset();
    }
}

