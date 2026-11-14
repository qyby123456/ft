/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonParser
 *  net.minecraft.client.model.geom.ModelPart
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraftforge.registries.ForgeRegistries
 *  org.apache.logging.log4j.LogManager
 */
package net.farmingtales.ftboss.youzi.motion;

import com.google.gson.JsonParser;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import net.farmingtales.ftboss.client.model.EasterBossModel;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.youzi.motion.YouziBodyState;
import net.farmingtales.ftboss.youzi.motion.YouziCharacterPose;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.logging.log4j.LogManager;

public final class YouziCharacterAdapter {
    private static YouziCharacterPose pose;
    private static boolean failed;

    private static YouziCharacterPose pose() {
        if (pose != null || failed) {
            return pose;
        }
        try (InputStream inputStream = YouziCharacterAdapter.class.getResourceAsStream("/assets/ftboss/youzi_motion/motion.json");){
            if (inputStream == null) {
                throw new IllegalStateException("Missing independent Youzi character motion");
            }
            pose = new YouziCharacterPose(JsonParser.parseReader((Reader)new InputStreamReader(inputStream, StandardCharsets.UTF_8)).getAsJsonObject());
        }
        catch (Exception exception) {
            failed = true;
            LogManager.getLogger((String)"YouziCharacterMotion").error("Youzi character motion unavailable; preserving original pose", (Throwable)exception);
        }
        return pose;
    }

    public static void apply(EasterBossModel easterBossModel, EasterBossEntity easterBossEntity, float f, float f2, float f3, float f4, float f5) {
        if (easterBossEntity.getVariant() != 0 || !easterBossEntity.m_6084_() || easterBossEntity.m_20159_()) {
            return;
        }
        ResourceLocation resourceLocation = ForgeRegistries.ITEMS.getKey((Object)easterBossEntity.m_21205_().m_41720_());
        YouziCharacterPose youziCharacterPose = YouziCharacterAdapter.pose();
        if (resourceLocation == null || youziCharacterPose == null) {
            return;
        }
        float f6 = Math.max(0.0f, Math.min(1.0f, f3 - (float)easterBossEntity.f_19797_));
        YouziBodyState.Motion motion = YouziBodyState.sample(easterBossEntity, f6, youziCharacterPose);
        if (motion != null) {
            Map<String, YouziCharacterPose.Bone> map = youziCharacterPose.sample(motion.clip(), motion.phase(), motion.weight(), new YouziCharacterPose.Vector(f5, f4, 0.0));
            YouziCharacterAdapter.applyBones(easterBossModel, map);
            return;
        }
        if (easterBossEntity.isCastingLike() || !easterBossEntity.m_20096_()) {
            return;
        }
        String string = youziCharacterPose.forWeapon(resourceLocation.toString());
        if (string == null) {
            return;
        }
        easterBossEntity.getActionTicks();
        if (0 <= 0) {
            YouziCharacterPose.Vector vector = youziCharacterPose.readyRotation(string);
            easterBossModel.f_102811_.f_104203_ = (float)Math.toRadians(vector.coordinateX()) + easterBossModel.f_102811_.f_104203_ * 0.25f;
            easterBossModel.f_102811_.f_104205_ = (float)Math.toRadians(vector.coordinateZ());
            easterBossModel.f_103375_.m_104315_(easterBossModel.f_102811_);
            return;
        }
        float f7 = easterBossEntity.getCombatAnimationAge(f6);
        float f8 = easterBossEntity.getCombatAnimationAge(0.0f) + (float)easterBossEntity.getActionTicks();
        double d = Math.max(0.0f, Math.min(1.0f, f7 / Math.max(1.0f, f8)));
        double d2 = easterBossEntity.getActionProgress(f6);
        double d3 = d2 > d + 1.0E-4 ? youziCharacterPose.impactPhase(string) + (1.0 - youziCharacterPose.impactPhase(string)) * d : d2;
        double d4 = Math.max(0.0f, Math.min(1.0f, easterBossEntity.getCombatAnimationWeight(f6)));
        Map<String, YouziCharacterPose.Bone> map = youziCharacterPose.sample(string, d3, d4, new YouziCharacterPose.Vector(f5, f4, 0.0));
        YouziCharacterAdapter.applyBones(easterBossModel, map);
    }

    private static void applyBones(EasterBossModel easterBossModel, Map<String, YouziCharacterPose.Bone> map) {
        YouziCharacterAdapter.apply(easterBossModel.f_102810_, map.get("body"));
        YouziCharacterAdapter.apply(easterBossModel.f_102808_, map.get("head"));
        YouziCharacterAdapter.apply(easterBossModel.f_102811_, map.get("rightArm"));
        YouziCharacterAdapter.apply(easterBossModel.f_102812_, map.get("leftArm"));
        easterBossModel.f_102809_.m_104315_(easterBossModel.f_102808_);
        easterBossModel.f_103374_.m_104315_(easterBossModel.f_102812_);
        easterBossModel.f_103375_.m_104315_(easterBossModel.f_102811_);
        easterBossModel.f_103378_.m_104315_(easterBossModel.f_102810_);
    }

    private static void apply(ModelPart modelPart, YouziCharacterPose.Bone bone) {
        modelPart.f_104200_ = (float)bone.position().coordinateX();
        modelPart.f_104201_ = (float)bone.position().coordinateY();
        modelPart.f_104202_ = (float)bone.position().coordinateZ();
        modelPart.f_104203_ = (float)Math.toRadians(bone.rotation().coordinateX());
        modelPart.f_104204_ = (float)Math.toRadians(bone.rotation().coordinateY());
        modelPart.f_104205_ = (float)Math.toRadians(bone.rotation().coordinateZ());
    }
}

