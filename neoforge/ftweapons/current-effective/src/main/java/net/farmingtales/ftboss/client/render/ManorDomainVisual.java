package net.farmingtales.ftboss.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.farmingtales.ftboss.client.render.bloom.SpellBloomRenderer;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.skill.ManorDomainPacket;
import net.farmingtales.ftboss.util.ManorDomainExpansion;
import net.farmingtales.ftboss.util.ManorDomainGeometryV2;
import net.farmingtales.ftboss.util.ManorCeremonyModel.Sink;
import net.farmingtales.ftboss.util.ManorDomainGeometryV2.Model;
import net.minecraft.client.Minecraft;
import net.minecraft.client.ParticleStatus;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent.LoggingOut;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent.Stage;
import net.neoforged.neoforge.client.event.ClientTickEvent.Post;
import net.neoforged.neoforge.event.level.LevelEvent.Unload;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.EventBusSubscriber.Bus;
import org.joml.Matrix4f;

@EventBusSubscriber(
    modid = "ftboss",
    value = {Dist.CLIENT}
)
public final class ManorDomainVisual {
    private static final Map<UUID, ManorDomainVisual.Show> ACTIVE = new HashMap<>();
    private static final Map<UUID, ManorDomainVisual.Show> RECENT = new HashMap<>();
    private static ClientLevel world;

    private ManorDomainVisual() {
    }

    public static void accept(ManorDomainPacket var0) {
        ClientLevel var1 = Minecraft.getInstance().level;
        if (var1 != null && var1.dimension().location().equals(var0.dimension())) {
            if (world != var1) {
                clear();
                world = var1;
            }

            if (ManorDomainGeometryV2.model(var0.variant()) == null) {
                ACTIVE.remove(var0.owner());
            } else {
                long var2 = var1.getGameTime();
                ManorDomainVisual.Show var4 = ACTIVE.get(var0.owner());
                if (var4 == null) {
                    var4 = RECENT.get(var0.owner());
                }

                long var5 = var4 != null && var4.packet.variant() == var0.variant() && var2 >= var4.updated && var2 - var4.updated <= 1200L
                    ? var4.started
                    : var2;
                if (ACTIVE.size() < 16 || ACTIVE.containsKey(var0.owner())) {
                    ManorDomainVisual.Show var7 = new ManorDomainVisual.Show(var0, var5, var2);
                    ACTIVE.put(var0.owner(), var7);
                    if (RECENT.size() < 64 || var4 != null) {
                        RECENT.put(var0.owner(), var7);
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public static void tick(Post var0) {
        ClientLevel var1 = Minecraft.getInstance().level;
        if (var1 == world && var1 != null) {
            long var2 = var1.getGameTime();
            ACTIVE.values().removeIf(var2x -> var2 < var2x.updated || var2 - var2x.updated > 65L);
            RECENT.values().removeIf(var2x -> var2 < var2x.updated || var2 - var2x.updated > 1200L);
        } else {
            clear();
        }

    }

    @SubscribeEvent
    public static void unload(Unload var0) {
        if (var0.getLevel() == world) {
            clear();
        }
    }

    @SubscribeEvent
    public static void logout(LoggingOut var0) {
        clear();
    }

    private static void clear() {
        ACTIVE.clear();
        RECENT.clear();
        world = null;
    }

    @SubscribeEvent
    public static void render(RenderLevelStageEvent var0) {
        boolean var1 = SpellBloomRenderer.begin(var0, ACTIVE.isEmpty() ^ true);

        try {
            ftboss$renderWithoutSoftBloom(var0);
        } finally {
            SpellBloomRenderer.end(var1);
        }
    }

    private static void ftboss$renderWithoutSoftBloom(RenderLevelStageEvent var0) {
        Minecraft var1 = Minecraft.getInstance();
        if (var0.getStage() == Stage.AFTER_PARTICLES && var1.level != null && !ACTIVE.isEmpty()) {
            Vec3 var2 = var0.getCamera().getPosition();
            BufferSource var3 = var1.renderBuffers().bufferSource();
            double var4 = (double)var1.level.getGameTime() + (var1.isPaused() ? 0.0 : (double)var0.getPartialTick().getGameTimeDeltaPartialTick(false));
            boolean var6 = var1.options.particles().get() != ParticleStatus.ALL;

            for (int var7 = 0; var7 < 2; var7++) {
                boolean var8 = var7 == 1;
                String var9 = var8 ? "glow" : "solid";
                VertexConsumer var10 = var3.getBuffer(var8 ? ManorDomainRenderType.GLOW : ManorDomainRenderType.SOLID);

                for (ManorDomainVisual.Show var12 : ACTIVE.values()) {
                    Vec3 var13 = var12.packet.center();
                    double var14 = var2.distanceToSqr(var13);
                    double var16 = var2.x - var13.x;
                    double var18 = var2.y - var13.y;
                    double var20 = var2.z - var13.z;
                    double var22 = 81.06666666666666;
                    double var24 = Math.max(0.0, Math.hypot(var16, var20) - var22);
                    if (!(var24 * var24 + var18 * var18 > 9216.0)
                        && (var0.getFrustum() == null || var0.getFrustum().isVisible(new AABB(var13.add(-var22, 0.0, -var22), var13.add(var22, 2.5, var22))))) {
                        Entity var26 = var1.level.getEntity(var12.packet.ownerId());
                        if (var26 instanceof EasterBossEntity) {
                            EasterBossEntity var27 = (EasterBossEntity)var26;
                            if (var27.getUUID().equals(var12.packet.owner())
                                && var27.isAlive()
                                && var27.getVariant() == var12.packet.variant()
                                && !var27.isManorIntroHidden()) {
                                Model var28 = ManorDomainGeometryV2.model(var12.packet.variant());
                                if (var28 != null) {
                                    double var29 = var4 - (double)var12.started;
                                    float var31 = (float)(
                                        ManorDomainExpansion.smooth(var29 / 6.0)
                                            * (1.0 - ManorDomainExpansion.smooth((var4 - (double)var12.updated - 40.0) / 25.0))
                                    );
                                    float var32 = var31 * (var12.packet.supported() ? 0.96F : 0.42F);
                                    if (!(var32 <= 0.001F)) {
                                        PoseStack var33 = var0.getPoseStack();
                                        var33.pushPose();

                                        try {
                                            var33.translate(var13.x - var2.x, var13.y - var2.y + 0.03, var13.z - var2.z);
                                            var33.scale(8.444445F, 1.0F, 8.444445F);
                                            Matrix4f var34 = var33.last().pose();
                                            Sink var35 = (var2x, var4x, var6x, var8x, var9x) -> var10.addVertex(var34, (float)var2x, (float)var4x, (float)var6x)
                                                    .setColor(
                                                        (float)(var8x >> 16 & 0xFF) / 255.0F,
                                                        (float)(var8x >> 8 & 0xFF) / 255.0F,
                                                        (float)(var8x & 0xFF) / 255.0F,
                                                        var9x
                                                    );
                                            boolean var36 = var14 > 2304.0 || var6;
                                            ManorDomainGeometryV2.emit(var28, var9, var29, var36, var32, var35);
                                            if (var8) {
                                                ManorDomainExpansion.emit(var28.variant, var28.glow, var28.accent, var29, var36, var32, var35);
                                            }
                                        } finally {
                                            var33.popPose();
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                var3.endBatch(var8 ? ManorDomainRenderType.GLOW : ManorDomainRenderType.SOLID);
            }
        }
    }

    private static record Show(ManorDomainPacket packet, long started, long updated) {
    }
}
