package net.farmingtales.ftboss.youzi.motion;

import com.google.gson.JsonParser;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import net.farmingtales.ftboss.client.model.EasterBossModel;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.registries.BuiltInRegistries;
import org.apache.logging.log4j.LogManager;

public final class YouziCharacterAdapter {
    private static YouziCharacterPose pose;
    private static boolean failed;

    private static YouziCharacterPose pose() {
        if (pose == null && !failed) {
            try (InputStream var0 = YouziCharacterAdapter.class.getResourceAsStream("/assets/ftboss/youzi_motion/motion.json")) {
                if (var0 == null) {
                    throw new IllegalStateException("Missing independent Youzi character motion");
                }

                pose = new YouziCharacterPose(JsonParser.parseReader(new InputStreamReader(var0, StandardCharsets.UTF_8)).getAsJsonObject());
            } catch (Exception var5) {
                failed = true;
                LogManager.getLogger("YouziCharacterMotion").error("Youzi character motion unavailable; preserving original pose", (Throwable)var5);
            }

            return pose;
        } else {
            return pose;
        }
    }

    public static void apply(EasterBossModel var0, EasterBossEntity var1, float var2, float var3, float var4, float var5, float var6) {
        if (var1.getVariant() == 0 && var1.isAlive() && !var1.isPassenger()) {
            ResourceLocation var7 = BuiltInRegistries.ITEM.getKey(var1.getMainHandItem().getItem());
            YouziCharacterPose var8 = pose();
            if (var7 != null && var8 != null) {
                float var9 = Math.max(0.0F, Math.min(1.0F, var4 - (float)var1.tickCount));
                YouziBodyState.Motion var10 = YouziBodyState.sample(var1, var9, var8);
                if (var10 != null) {
                    Map var23 = var8.sample(var10.clip(), var10.phase(), var10.weight(), new YouziCharacterPose.Vector((double)var6, (double)var5, 0.0));
                    applyBones(var0, var23);
                } else if (!var1.isCastingLike() && var1.onGround()) {
                    String var11 = var8.forWeapon(var7.toString());
                    if (var11 != null) {
                        var1.getActionTicks();
                        if (0 <= 0) {
                            YouziCharacterPose.Vector var24 = var8.readyRotation(var11);
                            var0.rightArm.xRot = (float)Math.toRadians(var24.coordinateX()) + var0.rightArm.xRot * 0.25F;
                            var0.rightArm.zRot = (float)Math.toRadians(var24.coordinateZ());
                            var0.rightSleeve.copyFrom(var0.rightArm);
                        } else {
                            float var12 = var1.getCombatAnimationAge(var9);
                            float var13 = var1.getCombatAnimationAge(0.0F) + (float)var1.getActionTicks();
                            double var14 = (double)Math.max(0.0F, Math.min(1.0F, var12 / Math.max(1.0F, var13)));
                            double var16 = (double)var1.getActionProgress(var9);
                            double var18 = var16 > var14 + 1.0E-4 ? var8.impactPhase(var11) + (1.0 - var8.impactPhase(var11)) * var14 : var16;
                            double var20 = (double)Math.max(0.0F, Math.min(1.0F, var1.getCombatAnimationWeight(var9)));
                            Map var22 = var8.sample(var11, var18, var20, new YouziCharacterPose.Vector((double)var6, (double)var5, 0.0));
                            applyBones(var0, var22);
                        }
                    }
                }
            }
        }
    }

    private static void applyBones(EasterBossModel var0, Map<String, YouziCharacterPose.Bone> var1) {
        apply(var0.body, (YouziCharacterPose.Bone)var1.get("body"));
        apply(var0.head, (YouziCharacterPose.Bone)var1.get("head"));
        apply(var0.rightArm, (YouziCharacterPose.Bone)var1.get("rightArm"));
        apply(var0.leftArm, (YouziCharacterPose.Bone)var1.get("leftArm"));
        var0.hat.copyFrom(var0.head);
        var0.leftSleeve.copyFrom(var0.leftArm);
        var0.rightSleeve.copyFrom(var0.rightArm);
        var0.jacket.copyFrom(var0.body);
    }

    private static void apply(ModelPart var0, YouziCharacterPose.Bone var1) {
        var0.x = (float)var1.position().coordinateX();
        var0.y = (float)var1.position().coordinateY();
        var0.z = (float)var1.position().coordinateZ();
        var0.xRot = (float)Math.toRadians(var1.rotation().coordinateX());
        var0.yRot = (float)Math.toRadians(var1.rotation().coordinateY());
        var0.zRot = (float)Math.toRadians(var1.rotation().coordinateZ());
    }
}
