import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.farmingtales.ftboss.client.render.CleaverMotionData;
import net.farmingtales.ftboss.client.render.CleaverMotionPlayback;

import java.nio.file.Files;
import java.nio.file.Path;

/** 黑切客户端纯逻辑专项检查；2026-09-16 对比离线关键帧、循环、半途反向和独立物品状态。 */
public final class CleaverRuntimeCheck {
    private static int assertions;

    private static void near(double actual, double expected, String label) {
        assertions++;
        if (!Double.isFinite(actual) || Math.abs(actual - expected) > 0.00002) throw new AssertionError(label + ": " + actual + " != " + expected);
    }

    private static void vector(CleaverMotionData.Vec actual, JsonElement expected, boolean scaling) {
        double fallback = scaling ? 1 : 0;
        near(actual.axisX(), expected == null ? fallback : expected.getAsJsonArray().get(0).getAsDouble(), "vector X");
        near(actual.axisY(), expected == null ? fallback : expected.getAsJsonArray().get(1).getAsDouble(), "vector Y");
        near(actual.axisZ(), expected == null ? fallback : expected.getAsJsonArray().get(2).getAsDouble(), "vector Z");
    }

    private static void frame(CleaverMotionData.Frame actual, CleaverMotionData.Frame expected, String label) {
        for (String bone : CleaverMotionData.BONES) {
            var first = actual.bone(bone);
            var second = expected.bone(bone);
            near(first.rotation().axisX(), second.rotation().axisX(), label);
            near(first.rotation().axisY(), second.rotation().axisY(), label);
            near(first.rotation().axisZ(), second.rotation().axisZ(), label);
            near(first.scale().axisX(), second.scale().axisX(), label);
            near(first.scale().axisY(), second.scale().axisY(), label);
            near(first.scale().axisZ(), second.scale().axisZ(), label);
        }
        for (String effect : CleaverMotionData.EFFECTS) near(actual.effect(effect), expected.effect(effect), label);
    }

    private static void bounded(CleaverMotionData.Frame pose, CleaverMotionData data) {
        for (String name : CleaverMotionData.WINGS) {
            double sign = Math.signum(data.sample("blood_idle", 0).bone(name).rotation().axisZ());
            double angle = pose.bone(name).rotation().axisZ() * sign;
            if (angle < -0.00001 || angle > 11.00001) throw new AssertionError("Wing angle exceeded range: " + angle);
            assertions++;
        }
        near(pose.bone("weapon").scale().axisX(), 1, "Rigid grip");
        var core = pose.bone("ember_core");
        if (core.scale().axisX() < 0.85 || core.scale().axisX() > 1.2 || Math.abs(core.rotation().axisZ()) > 6) {
            throw new AssertionError("Core response accumulated across interrupted transitions");
        }
        for (String effect : CleaverMotionData.EFFECTS) {
            double strength = pose.effect(effect);
            if (!Double.isFinite(strength) || strength < 0 || strength > 1.15) throw new AssertionError("Invalid effect strength " + strength);
            assertions++;
        }
    }

    public static void main(String[] arguments) throws Exception {
        Path assets = Path.of(arguments[0]);
        CleaverMotionData data;
        try (var animation = Files.newBufferedReader(assets.resolve("black_cleaver.animation.json"));
             var effects = Files.newBufferedReader(assets.resolve("black_cleaver.effects.json"))) {
            data = new CleaverMotionData(animation, effects);
        }
        JsonObject expected;
        try (var reader = Files.newBufferedReader(Path.of(arguments[1]))) {
            expected = JsonParser.parseReader(reader).getAsJsonObject();
        }
        int samples = 0;
        for (JsonElement entry : expected.getAsJsonArray("samples")) {
            JsonObject test = entry.getAsJsonObject();
            var pose = data.sample(test.get("clip").getAsString(), test.get("time").getAsDouble());
            JsonObject bones = test.getAsJsonObject("pose");
            for (String name : CleaverMotionData.BONES) {
                JsonObject bone = bones.has(name) ? bones.getAsJsonObject(name) : new JsonObject();
                vector(pose.bone(name).rotation(), bone.get("rotation"), false);
                vector(pose.bone(name).scale(), bone.get("scale"), true);
            }
            for (String effect : CleaverMotionData.EFFECTS) near(pose.effect(effect), test.getAsJsonObject("effects").get(effect).getAsDouble(), "Effect export parity");
            samples++;
        }
        CleaverMotionPlayback playback = new CleaverMotionPlayback(data, false);
        playback.target(true);
        for (int tick = 0; tick < 28; tick++) {
            for (float partial : new float[]{0, 0.25f, 0.75f}) {
                frame(playback.sample(partial, false), data.sample("blood_enter", (tick + partial) / 20.0), "Entry export parity");
                bounded(playback.sample(partial, false), data);
            }
            playback.tick();
        }
        frame(playback.sample(0, false), data.sample("blood_idle", 0), "Entry to idle");
        for (int tick = 0; tick < 48; tick++) playback.tick();
        frame(playback.sample(0, false), data.sample("blood_idle", 0), "Loop seam");
        frame(playback.sample(0, true), data.sample("blood_idle", 0), "Attack suppresses idle heartbeat");
        frame(data.skill(playback.sample(0, true), "blood_slam", 0.38, 1), data.sample("blood_slam", 0.38), "Strike parity");
        frame(data.sample("blood_slam", 0.65), data.sample("blood_recover", 0), "Strike recovery seam");
        frame(data.sample("blood_recover", 0.3), data.sample("blood_idle", 0), "Recovery idle seam");
        CleaverMotionPlayback other = new CleaverMotionPlayback(data, false);
        for (int tick = 0; tick < 300; tick++) {
            if (tick % 7 == 0) {
                var before = playback.sample(0, false);
                playback.target(tick % 14 == 0);
                frame(playback.sample(0, false), before, "Interrupted transition continuity");
            }
            var beforeRead = playback.sample(0.4f, false);
            for (int draw = 0; draw < 12; draw++) frame(playback.sample(0.4f, false), beforeRead, "Render does not advance time");
            bounded(beforeRead, data);
            bounded(data.skill(beforeRead, "blood_slam", (tick % 13) / 20.0, playback.amount(0.4f)), data);
            frame(other.sample(0, false), data.sample("idle", 0), "Independent item");
            playback.tick();
        }
        playback.target(false);
        for (int tick = 0; tick < 28; tick++) playback.tick();
        near(playback.amount(0), 0, "Exit amount");
        frame(playback.sample(0, false), data.sample("idle", 0), "Exit closed");
        String report = "{\"status\":\"PASS\",\"offline_parity_samples\":" + samples + ",\"assertions\":" + assertions
                + ",\"checks\":[\"exported keyframe parity\",\"entry and loop seams\",\"skill recovery\",\"mid-transition reversal\",\"per-item isolation\",\"render-rate independence\"],\"game_tested\":false}\n";
        Files.writeString(Path.of(arguments[2]), report);
        System.out.println(report);
    }
}
