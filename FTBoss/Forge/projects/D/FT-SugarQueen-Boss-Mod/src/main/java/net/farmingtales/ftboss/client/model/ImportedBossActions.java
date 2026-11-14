package net.farmingtales.ftboss.client.model;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;
import net.farmingtales.ftboss.entity.MeatMountainEntity;
import net.farmingtales.ftboss.entity.TeaTreeBossEntity;
import net.minecraft.client.model.geom.ModelPart;
import org.joml.Quaternionf;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public final class ImportedBossActions {
    private static final Map<String, Map<String, Clip>> CLIPS = new HashMap<>();
    private static final Map<ModelPart, Boolean> UNARMED = new WeakHashMap<>();
    private static final Map<SayaPlayerModel, Map<String, Matrix4f>> SAYA_POSES = new WeakHashMap<>();

    private record Bind(String parent, Vector3f pivot) {}
    private record Clip(float length, Map<String, Map<String, float[][]>> bones, Map<String, Bind> skeleton) {}

    private ImportedBossActions() {}

    private static Clip clip(String boss, String action) {
        return CLIPS.computeIfAbsent(boss, ImportedBossActions::load).get(action);
    }

    private static Map<String, Clip> load(String boss) {
        String path = "/assets/ftboss/boss_actions/imported_" + boss + ".json";
        try (var stream = ImportedBossActions.class.getResourceAsStream(path)) {
            if (stream == null) throw new IllegalStateException("Missing imported animation " + path);
            JsonObject document = JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8)).getAsJsonObject();
            Map<String, Clip> result = new HashMap<>();
            for (var entry : document.entrySet()) {
                JsonObject animation = entry.getValue().getAsJsonObject();
                Map<String, Map<String, float[][]>> bones = new HashMap<>();
                for (var bone : animation.getAsJsonObject("bones").entrySet()) {
                    Map<String, float[][]> channels = new HashMap<>();
                    for (var channel : bone.getValue().getAsJsonObject().entrySet()) {
                        JsonArray frames = channel.getValue().getAsJsonArray();
                        float[][] values = new float[frames.size()][5];
                        for (int frame = 0; frame < frames.size(); frame++) {
                            for (int component = 0; component < 5; component++) {
                                values[frame][component] = frames.get(frame).getAsJsonArray().get(component).getAsFloat();
                            }
                        }
                        channels.put(channel.getKey(), values);
                    }
                    bones.put(bone.getKey(), channels);
                }
                Map<String, Bind> skeleton = new HashMap<>();
                for (var bone : animation.getAsJsonObject("skeleton").entrySet()) {
                    JsonObject bind = bone.getValue().getAsJsonObject();
                    JsonArray origin = bind.getAsJsonArray("origin");
                    skeleton.put(bone.getKey(), new Bind(bind.get("parent").getAsString(),
                            new Vector3f(-origin.get(0).getAsFloat(), 24 - origin.get(1).getAsFloat(), origin.get(2).getAsFloat())));
                }
                result.put(entry.getKey(), new Clip(animation.get("length").getAsFloat(), bones, skeleton));
            }
            return result;
        } catch (Exception failure) {
            throw new IllegalStateException("Cannot load imported Boss motion " + boss, failure);
        }
    }

    public static float[] sample(float[][] frames, float time) {
        if (time <= frames[0][0]) return new float[]{frames[0][1], frames[0][2], frames[0][3]};
        int last = frames.length - 1;
        if (time >= frames[last][0]) return new float[]{frames[last][1], frames[last][2], frames[last][3]};
        int index = 0;
        while (index < last - 1 && time >= frames[index + 1][0]) index++;
        float[] before = frames[index], after = frames[index + 1];
        float alpha = (time - before[0]) / (after[0] - before[0]);
        float[] values = new float[3];
        for (int axis = 0; axis < 3; axis++) {
            float start = before[axis + 1], end = after[axis + 1];
            if (before[4] == 1 || after[4] == 1) {
                float previous = index > 0 ? frames[index - 1][axis + 1] : start;
                float next = index + 2 <= last ? frames[index + 2][axis + 1] : end;
                float firstTangent = (end - previous) * 0.5F;
                float secondTangent = (next - start) * 0.5F;
                float squared = alpha * alpha;
                values[axis] = (2 * start - 2 * end + firstTangent + secondTangent) * squared * alpha
                        + (-3 * start + 3 * end - 2 * firstTangent - secondTangent) * squared + firstTangent * alpha + start;
            } else {
                values[axis] = start + (end - start) * alpha;
            }
        }
        return values;
    }

    private static void apply(ModelPart part, Map<String, float[][]> channels, float time) {
        if (channels == null) return;
        for (var channel : channels.entrySet()) {
            float[] values = sample(channel.getValue(), time);
            switch (channel.getKey()) {
                case "position" -> { part.x -= values[0]; part.y -= values[1]; part.z += values[2]; }
                case "rotation" -> {
                    part.xRot -= (float) Math.toRadians(values[0]);
                    part.yRot -= (float) Math.toRadians(values[1]);
                    part.zRot += (float) Math.toRadians(values[2]);
                }
                case "scale" -> { part.xScale *= values[0]; part.yScale *= values[1]; part.zScale *= values[2]; }
                default -> throw new IllegalStateException("Unknown motion channel " + channel.getKey());
            }
        }
    }

    private static Map<String, ModelPart> butcherParts(ModelPart root) {
        Map<String, ModelPart> parts = new HashMap<>();
        parts.put("root", root);
        ModelPart body = root.getChild("body");
        parts.put("body", body);
        for (String name : new String[]{"head", "apron_l", "apron_r"}) parts.put(name, body.getChild(name));
        for (String side : new String[]{"l", "r"}) {
            ModelPart arm = body.getChild("arm_" + side), forearm = arm.getChild("forearm_" + side);
            parts.put("arm_" + side, arm);
            parts.put("forearm_" + side, forearm);
            parts.put("hand_" + side, forearm.getChild("hand_" + side));
            ModelPart leg = root.getChild("leg_" + side), shin = leg.getChild("shin_" + side);
            parts.put("leg_" + side, leg);
            parts.put("shin_" + side, shin);
            parts.put("boot_" + side, shin.getChild("boot_" + side));
        }
        return parts;
    }

    public static boolean butcher(ModelPart root, MeatMountainEntity entity, float ageInTicks) {
        boolean firstPhase = entity.getButcherPhase() == 1;
        UNARMED.put(root, firstPhase);
        root.getAllParts().forEach(part -> part.xScale = part.yScale = part.zScale = 1);
        if (!firstPhase || !entity.isAlive()) return false;
        String action = entity.getButcherAction();
        if (action.isEmpty()) return false;
        float elapsed = entity.getButcherActionAge(ageInTicks - entity.tickCount);
        int duration = net.farmingtales.ftboss.boss.ai.ButcherMotionTiming.duration(1, action, 4);
        poseButcher(root, "backhand".equals(action) ? "技能" : "攻击", elapsed / duration);
        return true;
    }

    public static void poseButcher(ModelPart root, String action, float progress) {
        Clip motion = clip("butcher", action);
        float time = motion.length * Math.max(0, Math.min(1, progress));
        root.getAllParts().forEach(part -> { part.resetPose(); part.xScale = part.yScale = part.zScale = 1; });
        Map<String, ModelPart> parts = butcherParts(root);
        motion.bones.forEach((name, channels) -> apply(parts.get(name), channels, time));
        Vector3f ground = new Vector3f(0, -24 * root.yScale, 0).rotate(new Quaternionf().rotationZYX(root.zRot, root.yRot, root.xRot));
        root.x += ground.x;
        root.y += 24 + ground.y;
        root.z += ground.z;
    }

    public static boolean hideWeapon(ModelPart root, PoseStack pose) {
        if (!Boolean.TRUE.equals(UNARMED.get(root))) return false;
        pose.scale(0, 0, 0);
        return true;
    }

    public static boolean saya(SayaPlayerModel model, TeaTreeBossEntity entity, float ageInTicks) {
        SAYA_POSES.remove(model);
        for (ModelPart part : new ModelPart[]{model.head, model.body, model.rightArm, model.leftArm, model.rightLeg, model.leftLeg,
                model.hat, model.jacket, model.rightSleeve, model.leftSleeve, model.rightPants, model.leftPants}) {
            part.xScale = part.yScale = part.zScale = 1;
        }
        if (!entity.isAlive()) return false;
        var signature = entity.signatureCastAction();
        var nativeAction = entity.nativeAction();
        String action;
        float elapsed, duration;
        if (signature.active()) {
            if ("signature_self_cast".equals(signature.name())) return false;
            action = "技能";
            elapsed = signature.elapsed(ageInTicks - entity.tickCount);
            duration = signature.duration();
        } else if (nativeAction.active()) {
            action = "melee".equals(nativeAction.name()) ? "攻击" : "技能";
            elapsed = nativeAction.elapsed(ageInTicks - entity.tickCount);
            duration = nativeAction.duration();
        } else {
            return false;
        }
        Clip motion = clip("saya", action);
        float time = motion.length * Math.max(0, Math.min(1, elapsed / Math.max(1, duration)));
        Map<String, ModelPart> parts = Map.of("head", model.head, "body", model.body, "right_arm", model.rightArm,
                "left_arm", model.leftArm, "right_leg", model.rightLeg, "left_leg", model.leftLeg);
        for (ModelPart part : parts.values()) { part.resetPose(); part.xScale = part.yScale = part.zScale = 1; }
        Map<String, Matrix4f> skins = sayaMatrices(action, time / motion.length);
        SAYA_POSES.put(model, skins);
        model.hat.copyFrom(model.head);
        model.jacket.copyFrom(model.body);
        model.rightSleeve.copyFrom(model.rightArm);
        model.leftSleeve.copyFrom(model.leftArm);
        model.rightPants.copyFrom(model.rightLeg);
        model.leftPants.copyFrom(model.leftLeg);
        return true;
    }

    public static Map<String, Matrix4f> sayaMatrices(String action, float progress) {
        Clip motion = clip("saya", action);
        float time = motion.length * Math.max(0, Math.min(1, progress));
        Map<String, Matrix4f> world = new HashMap<>(), skins = new HashMap<>();
        for (String name : motion.skeleton.keySet()) {
            Matrix4f matrix = world(motion, name, time, world);
            Vector3f pivot = motion.skeleton.get(name).pivot;
            skins.put(name, new Matrix4f(matrix).translate(-pivot.x, -pivot.y, -pivot.z));
        }
        return skins;
    }

    private static Matrix4f world(Clip clip, String name, float time, Map<String, Matrix4f> cache) {
        if (cache.containsKey(name)) return cache.get(name);
        Bind bind = clip.skeleton.get(name);
        Vector3f offset = new Vector3f(bind.pivot);
        Matrix4f matrix = new Matrix4f();
        if (!bind.parent.isEmpty()) {
            offset.sub(clip.skeleton.get(bind.parent).pivot);
            matrix.set(world(clip, bind.parent, time, cache));
        }
        ModelPart delta = new ModelPart(java.util.List.of(), Map.of());
        apply(delta, clip.bones.get(name), time);
        matrix.translate(offset.x + delta.x, offset.y + delta.y, offset.z + delta.z)
                .rotateZYX(delta.zRot, delta.yRot, delta.xRot).scale(delta.xScale, delta.yScale, delta.zScale);
        cache.put(name, matrix);
        return matrix;
    }

    public static boolean renderSaya(SayaPlayerModel model, PoseStack pose, VertexConsumer buffer, int light, int overlay,
                                     float red, float green, float blue, float alpha) {
        Map<String, Matrix4f> skins = SAYA_POSES.get(model);
        if (skins == null) return false;
        Map<String, ModelPart> parts = Map.ofEntries(Map.entry("head", model.head), Map.entry("hat", model.hat),
                Map.entry("body", model.body), Map.entry("jacket", model.jacket),
                Map.entry("right_arm", model.rightArm), Map.entry("right_sleeve", model.rightSleeve),
                Map.entry("left_arm", model.leftArm), Map.entry("left_sleeve", model.leftSleeve),
                Map.entry("right_leg", model.rightLeg), Map.entry("right_pants", model.rightPants),
                Map.entry("left_leg", model.leftLeg), Map.entry("left_pants", model.leftPants));
        for (var entry : parts.entrySet()) {
            Matrix4f skin = new Matrix4f().scaling(1.0F / 16).mul(skins.get(entry.getKey())).scale(16);
            pose.pushPose();
            pose.mulPoseMatrix(skin);
            pose.last().normal().mul(new Matrix3f(skin).invert().transpose());
            entry.getValue().render(pose, buffer, light, overlay, red, green, blue, alpha);
            pose.popPose();
        }
        return true;
    }
}
