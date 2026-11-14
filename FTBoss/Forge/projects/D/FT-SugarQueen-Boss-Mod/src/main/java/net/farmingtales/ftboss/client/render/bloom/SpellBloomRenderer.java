package net.farmingtales.ftboss.client.render.bloom;

import com.google.gson.Gson;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLPaths;
import org.slf4j.Logger;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;

@Mod.EventBusSubscriber(modid = "ftboss", value = Dist.CLIENT)
public final class SpellBloomRenderer {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final SpellBloomGpu GPU = new SpellBloomGpu(SpellBloomRenderer::shader);
    private static SpellBloomSettings settings = SpellBloomSettings.defaults();
    private static boolean settingsLoaded, failed, firstPerson, announced;
    private static int passes;

    private SpellBloomRenderer() {}

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void frame(RenderLevelStageEvent event) {
        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_PARTICLES) passes = 0;
    }

    public static boolean begin(RenderLevelStageEvent event, boolean active) {
        if (!active || failed || event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES
                || Minecraft.getInstance().level == null || !RenderSystem.isOnRenderThread()) return false;
        loadSettings();
        if (!settings.enabled() || settings.strength() <= 0 || passes >= settings.maxPasses()) return false;
        if (!settings.allowShaderPacks() && shaderPackActive()) return false;
        if (GL11.glGetInteger(GL30.GL_DRAW_FRAMEBUFFER_BINDING) != Minecraft.getInstance().getMainRenderTarget().frameBufferId) return false;
        try {
            firstPerson = !event.getCamera().isDetached();
            boolean started = GPU.begin(settings);
            if (started) {
                passes++;
                if (!announced) {
                    LOGGER.info("[FTBoss] Spell soft bloom initialized: strength={}, radius={}, threshold={}",
                            settings.strength(), settings.radius(), settings.threshold());
                    announced = true;
                }
            }
            return started;
        } catch (IOException | RuntimeException exception) {
            disable(exception);
            return false;
        }
    }

    public static void end(boolean started) {
        if (!started) return;
        try {
            GPU.finish(settings, firstPerson);
        } catch (RuntimeException exception) {
            disable(exception);
        }
    }

    private static String shader(String name) throws IOException {
        ResourceLocation location = new ResourceLocation("ftboss", "shaders/soft_bloom/" + name);
        try (var input = Minecraft.getInstance().getResourceManager().getResourceOrThrow(location).open()) {
            return new String(input.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private static void loadSettings() {
        if (settingsLoaded) return;
        settingsLoaded = true;
        var path = FMLPaths.CONFIGDIR.get().resolve("ftboss-spell-bloom.json");
        if (!Files.isRegularFile(path)) return;
        try (var input = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            SpellBloomSettings parsed = new Gson().fromJson(input, SpellBloomSettings.class);
            if (parsed != null) settings = parsed.sanitized();
        } catch (IOException | RuntimeException exception) {
            LOGGER.warn("[FTBoss] Invalid spell bloom config; using safe defaults", exception);
            settings = SpellBloomSettings.defaults();
        }
    }

    private static boolean shaderPackActive() {
        return ShaderPackProbe.active();
    }

    private static void disable(Exception exception) {
        failed = true;
        LOGGER.warn("[FTBoss] Spell soft bloom disabled; original rendering retained", exception);
        try { GPU.close(); } catch (RuntimeException cleanupFailure) { LOGGER.debug("Spell bloom cleanup failed", cleanupFailure); }
    }

    public static void reset() {
        Runnable reset = () -> {
            GPU.close();
            passes = 0;
            settings = SpellBloomSettings.defaults();
            settingsLoaded = failed = announced = false;
        };
        if (RenderSystem.isOnRenderThread()) reset.run(); else RenderSystem.recordRenderCall(reset::run);
    }

    @SubscribeEvent
    public static void logout(ClientPlayerNetworkEvent.LoggingOut event) {
        reset();
    }
}
