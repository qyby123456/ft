import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.vertex.PoseStack;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.model.AnimationUtils;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.renderer.block.model.ItemTransform;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.cache.object.GeoCube;
import software.bernie.geckolib.cache.object.GeoQuad;
import software.bernie.geckolib.cache.object.GeoVertex;
import software.bernie.geckolib.loading.json.FormatVersion;
import software.bernie.geckolib.loading.json.raw.Bone;
import software.bernie.geckolib.loading.json.raw.Cube;
import software.bernie.geckolib.loading.json.raw.FaceUV;
import software.bernie.geckolib.loading.json.raw.MinecraftGeometry;
import software.bernie.geckolib.loading.json.raw.Model;
import software.bernie.geckolib.loading.json.raw.ModelProperties;
import software.bernie.geckolib.loading.json.raw.UVFaces;
import software.bernie.geckolib.loading.json.raw.UVUnion;
import software.bernie.geckolib.loading.object.BakedModelFactory;
import software.bernie.geckolib.loading.object.GeometryTree;
import software.bernie.geckolib.util.RenderUtils;

/** Checks exported geometry against the installed GeckoLib baker and matrix helpers. */
public final class GeckoGeometryCheck {
    private static final Gson JSON = new GsonBuilder()
            .registerTypeAdapter(Bone.class, Bone.deserializer())
            .registerTypeAdapter(Cube.class, Cube.deserializer())
            .registerTypeAdapter(FaceUV.class, FaceUV.deserializer())
            .registerTypeAdapter(UVFaces.class, UVFaces.deserializer())
            .registerTypeAdapter(UVUnion.class, UVUnion.deserializer())
            .registerTypeAdapter(ModelProperties.class, ModelProperties.deserializer()).create();
    private static final Map<String, GeoBone> BONES = new HashMap<>();
    private static double maximumError;
    private static long vertices;
    private static long uvVertices;

    private static double distance(Vector3f actual, JsonArray corner) {
        return Math.sqrt(Math.pow(actual.x-corner.get(0).getAsDouble(), 2)
                + Math.pow(actual.y-corner.get(1).getAsDouble(), 2)
                + Math.pow(actual.z-corner.get(2).getAsDouble(), 2));
    }

    private static void verifyUVs(GeoQuad face, PoseStack matrices, JsonObject expected) {
        int[] indices = new int[4];
        for (int index = 0; index < 4; index++) {
            Vector3f actual = matrices.m_85850_().m_252922_().transformPosition(new Vector3f(face.vertices()[index].position())).mul(-16,16,16);
            double error = Double.POSITIVE_INFINITY;
            for (int cornerIndex = 0; cornerIndex < 8; cornerIndex++) {
                double candidate = distance(actual, expected.getAsJsonArray("corners").get(cornerIndex).getAsJsonArray());
                if (candidate < error) {
                    error = candidate;
                    indices[index] = cornerIndex;
                }
            }
        }
        for (JsonElement element : expected.getAsJsonArray("faces")) {
            JsonObject candidate = element.getAsJsonObject();
            JsonArray expectedIndices = candidate.getAsJsonArray("indices");
            boolean match = true;
            for (int index : indices) if (!expectedIndices.contains(JSON.toJsonTree(index))) match = false;
            if (!match) continue;
            for (int index = 0; index < 4; index++) {
                for (int targetIndex = 0; targetIndex < 4; targetIndex++) {
                    if (expectedIndices.get(targetIndex).getAsInt() != indices[index]) continue;
                    JsonArray uv = candidate.getAsJsonArray("uv").get(targetIndex).getAsJsonArray();
                    GeoVertex vertex = face.vertices()[index];
                    if (Math.abs(vertex.texU()*256-uv.get(0).getAsDouble()) > 0.00001
                            || Math.abs(vertex.texV()*256-uv.get(1).getAsDouble()) > 0.00001)
                        throw new AssertionError("UV mismatch " + expected.get("bone") + " " + expected.get("index") + " " + face.direction() + " corner=" + indices[index]
                                + " actual=" + vertex.texU()*256 + "," + vertex.texV()*256 + " expected=" + uv);
                    uvVertices++;
                }
            }
            return;
        }
        throw new AssertionError("Unmatched native quad");
    }

    private static double[] vector(JsonObject object, String name, double fallback) {
        JsonArray values = object.getAsJsonArray(name);
        if (values == null) return new double[] { fallback, fallback, fallback };
        return new double[] { values.get(0).getAsDouble(), values.get(1).getAsDouble(), values.get(2).getAsDouble() };
    }

    private static void index(GeoBone bone) {
        BONES.put(bone.getName(), bone);
        for (GeoBone child : bone.getChildBones()) index(child);
    }

    private static void transform(PoseStack matrices, GeoBone bone) {
        if (bone.getParent() != null) transform(matrices, bone.getParent());
        RenderUtils.prepMatrixForBone(matrices, bone);
    }

    private static void verifyVertices(JsonObject sample) {
        for (JsonElement entry : sample.getAsJsonArray("cubes")) {
            JsonObject expected = entry.getAsJsonObject();
            GeoBone bone = BONES.get(expected.get("bone").getAsString());
            GeoCube cube = bone.getCubes().get(expected.get("index").getAsInt());
            PoseStack matrices = new PoseStack();
            transform(matrices, bone);
            RenderUtils.translateToPivotPoint(matrices, cube);
            RenderUtils.rotateMatrixAroundCube(matrices, cube);
            RenderUtils.translateAwayFromPivotPoint(matrices, cube);
            for (GeoQuad face : cube.quads()) {
                if (face == null) continue;
                if (sample.get("name").getAsString().equals("closed")) verifyUVs(face, matrices, expected);
                for (GeoVertex vertex : face.vertices()) {
                    Vector3f actual = matrices.m_85850_().m_252922_().transformPosition(new Vector3f(vertex.position()));
                    actual.mul(-16, 16, 16);
                    double error = Double.POSITIVE_INFINITY;
                    for (JsonElement point : expected.getAsJsonArray("corners")) {
                        JsonArray corner = point.getAsJsonArray();
                        error = Math.min(error, distance(actual, corner));
                    }
                    maximumError = Math.max(maximumError, error);
                    if (error > 0.00005) throw new AssertionError(sample.get("name") + " " + expected.get("bone") + " vertex error=" + error);
                    vertices++;
                }
            }
        }
    }

    private static void applyItem(PoseStack matrices, JsonObject display, boolean leftHand) {
        double[] rotation = vector(display, "rotation", 0);
        double[] translation = vector(display, "translation", 0);
        double[] scale = vector(display, "scale", 1);
        ItemTransform item = new ItemTransform(new Vector3f((float) rotation[0], (float) rotation[1], (float) rotation[2]),
                new Vector3f((float) translation[0]/16, (float) translation[1]/16, (float) translation[2]/16),
                new Vector3f((float) scale[0], (float) scale[1], (float) scale[2]));
        item.m_111763_(leftHand, matrices);
        matrices.m_85837_(-0.5, -0.5, -0.5);
        matrices.m_252880_(0.5f, 0.51f, 0.5f);
        matrices.m_85837_(0, -0.01, 0);
    }

    private static Vector3f itemPoint(JsonObject display, boolean leftHand, Vector3f point) {
        PoseStack matrices = new PoseStack();
        applyItem(matrices, display, leftHand);
        return matrices.m_85850_().m_252922_().transformPosition(point);
    }

    private static void verifyGrip(JsonObject display) {
        for (boolean leftHand : new boolean[] { false, true }) {
            String side = leftHand ? "lefthand" : "righthand";
            JsonObject firstPerson = display.getAsJsonObject("firstperson_" + side);
            Vector3f firstGrip = itemPoint(firstPerson, leftHand, new Vector3f());
            Vector3f firstDirection = itemPoint(firstPerson, leftHand, new Vector3f(0, 1, 0)).sub(firstGrip).normalize();
            if (Math.abs(firstDirection.x) > 0.00001f || Math.abs(firstDirection.y-1) > 0.00001f || Math.abs(firstDirection.z) > 0.00001f)
                throw new AssertionError(side + " first-person shaft must be vertical in camera space: " + firstDirection);
            Vector3f firstEdge = itemPoint(firstPerson, leftHand, new Vector3f(1, 0, 0)).sub(firstGrip).normalize();
            if (firstEdge.z > -0.7f) throw new AssertionError(side + " cutting edge faces sideways instead of forward: " + firstEdge);
        }
        Vector3f right = itemPoint(display.getAsJsonObject("firstperson_righthand"), false, new Vector3f(0, 1, 0));
        Vector3f left = itemPoint(display.getAsJsonObject("firstperson_lefthand"), true, new Vector3f(0, 1, 0));
        if (Math.abs(right.x+left.x) > 0.00001f || Math.abs(right.y-left.y) > 0.00001f || Math.abs(right.z-left.z) > 0.00001f)
            throw new AssertionError("First-person left/right transforms are not mirrored");
    }

    private static JsonArray verifyPlayerHold(JsonObject checks, boolean enforce) throws Exception {
        JsonArray results = new JsonArray();
        for (boolean slim : new boolean[] { false, true }) {
            for (boolean leftHand : new boolean[] { false, true }) {
                for (int sample = -1; sample < 8; sample++) {
                    ModelPart root = LayerDefinition.m_171565_(PlayerModel.m_170825_(CubeDeformation.f_171458_, slim), 64, 64).m_171564_();
                    PlayerModel<LivingEntity> player = new PlayerModel<>(root, slim);
                    player.f_102815_ = HumanoidModel.ArmPose.ITEM;
                    player.f_102816_ = HumanoidModel.ArmPose.ITEM;
                    ModelPart arm = leftHand ? player.f_102812_ : player.f_102811_;
                    var poseArm = HumanoidModel.class.getDeclaredMethod(leftHand ? "m_102878_" : "m_102875_", LivingEntity.class);
                    poseArm.setAccessible(true);
                    poseArm.invoke(player, new Object[] { null });
                    if (sample >= 0) AnimationUtils.m_170341_(arm, sample*15, leftHand ? -1 : 1);
                    PoseStack palmMatrices = new PoseStack();
                    arm.m_104299_(palmMatrices);
                    float palmCenterX = (leftHand ? 1 : -1)*(slim ? 0.5f : 1)/16;
                    Vector3f palmCenter = palmMatrices.m_85850_().m_252922_().transformPosition(new Vector3f(palmCenterX, 8f/16, 0));
                    Vector3f palm = palmMatrices.m_85850_().m_252922_().transformPosition(
                            new Vector3f(palmCenterX+(leftHand ? 1.4f : -1.4f)/16, 8f/16, 0));
                    PoseStack matrices = new PoseStack();
                    player.m_6002_(leftHand ? HumanoidArm.LEFT : HumanoidArm.RIGHT, matrices);
                    matrices.m_252781_(new Quaternionf().rotationX((float) -Math.PI/2));
                    matrices.m_252781_(new Quaternionf().rotationY((float) Math.PI));
                    matrices.m_85837_((leftHand ? -1 : 1)/16.0, 0.125, -0.625);
                    applyItem(matrices, checks.getAsJsonObject("display").getAsJsonObject(
                            "thirdperson_" + (leftHand ? "lefthand" : "righthand")), leftHand);
                    Matrix4f matrix = matrices.m_85850_().m_252922_();
                    Vector3f grip = matrix.transformPosition(new Vector3f());
                    Vector3f shaft = matrix.transformDirection(new Vector3f(0, 1, 0)).normalize();
                    Vector3f edge = matrix.transformDirection(new Vector3f(1, 0, 0)).normalize();
                    float error = grip.distance(palm)*16;
                    JsonObject result = new JsonObject();
                    result.addProperty("model", slim ? "slim" : "classic");
                    result.addProperty("hand", leftHand ? "left" : "right");
                    result.addProperty("idle_bob_tick", sample < 0 ? -1 : sample*15);
                    result.addProperty("palm_error_model_units", error);
                    result.addProperty("palm_center_distance_model_units", grip.distance(palmCenter)*16);
                    result.add("shaft_direction", JSON.toJsonTree(new float[] { shaft.x, shaft.y, shaft.z }));
                    result.add("cutting_direction", JSON.toJsonTree(new float[] { edge.x, edge.y, edge.z }));
                    result.add("item_matrix", JSON.toJsonTree(matrix.get(new float[16])));
                    result.add("palm", JSON.toJsonTree(new float[] { palm.x, palm.y, palm.z }));
                    results.add(result);
                    if (enforce && (error > 0.06f || grip.distance(palmCenter)*16 > (slim ? 1.5f : 2f)
                            || (sample < 0 && (shaft.distance(new Vector3f(0, -1, 0)) > 0.00001f
                            || edge.distance(new Vector3f(0, 0, -1)) > 0.00001f))))
                        throw new AssertionError("Player hand chain: grip must meet palm, shaft upright, cutting edge forward: " + result);
                    if (sample < 0) result.add("scene", handScene(checks, root, matrix));
                }
            }
        }
        return results;
    }

    private static JsonArray scenePoint(Vector3f point) {
        return JSON.toJsonTree(new float[] { point.x*16, -point.y*16, -point.z*16 }).getAsJsonArray();
    }

    private static JsonObject handScene(JsonObject checks, ModelPart root, Matrix4f item) {
        JsonArray body = new JsonArray();
        root.m_171309_(new PoseStack(), (pose, part, index, cube) -> {
            if (!java.util.Set.of("/head", "/body", "/right_arm", "/left_arm", "/right_leg", "/left_leg").contains(part)) return;
            JsonObject entry = new JsonObject();
            entry.addProperty("part", part);
            JsonArray corners = new JsonArray();
            for (float horizontal : new float[] { cube.f_104335_, cube.f_104338_ }) {
                for (float vertical : new float[] { cube.f_104336_, cube.f_104339_ }) {
                    for (float forward : new float[] { cube.f_104337_, cube.f_104340_ }) {
                        corners.add(scenePoint(pose.m_252922_().transformPosition(new Vector3f(horizontal, vertical, forward).div(16))));
                    }
                }
            }
            entry.add("corners", corners);
            body.add(entry);
        });
        JsonObject result = new JsonObject();
        result.add("body", body);
        for (JsonElement element : checks.getAsJsonArray("samples")) {
            JsonObject sample = element.getAsJsonObject();
            String name = sample.get("name").getAsString();
            if (!(name.equals("closed") || name.equals("blood_enter:1.4"))) continue;
            JsonArray cubes = new JsonArray();
            for (JsonElement cubeElement : sample.getAsJsonArray("cubes")) {
                JsonObject cube = cubeElement.getAsJsonObject();
                JsonObject entry = new JsonObject();
                entry.add("bone", cube.get("bone"));
                entry.add("index", cube.get("index"));
                JsonArray corners = new JsonArray();
                for (JsonElement corner : cube.getAsJsonArray("corners")) {
                    JsonArray point = corner.getAsJsonArray();
                    corners.add(scenePoint(item.transformPosition(new Vector3f(-point.get(0).getAsFloat(),
                            point.get(1).getAsFloat(), point.get(2).getAsFloat()).div(16))));
                }
                entry.add("corners", corners);
                cubes.add(entry);
            }
            result.add(name, cubes);
        }
        return result;
    }

    private static JsonObject firstPersonBounds(JsonObject sample, JsonObject display, boolean leftHand, double aspect) {
        double[] full = { Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY };
        double[] head = full.clone();
        double nearest = Double.POSITIVE_INFINITY;
        for (JsonElement cube : sample.getAsJsonArray("cubes")) {
            for (JsonElement element : cube.getAsJsonObject().getAsJsonArray("corners")) {
                JsonArray point = element.getAsJsonArray();
                Vector3f nativePoint = new Vector3f(-point.get(0).getAsFloat()/16, point.get(1).getAsFloat()/16, point.get(2).getAsFloat()/16);
                Vector3f camera = itemPoint(display, leftHand, nativePoint).add(leftHand ? -0.56f : 0.56f, -0.52f, -0.72f);
                nearest = Math.min(nearest, -camera.z);
                double tangent = Math.tan(Math.toRadians(70)/2);
                double screenX = 0.5+camera.x/(-camera.z*tangent*aspect)/2;
                double screenY = 0.5-camera.y/(-camera.z*tangent)/2;
                full[0] = Math.min(full[0], screenX);
                full[1] = Math.min(full[1], screenY);
                full[2] = Math.max(full[2], screenX);
                full[3] = Math.max(full[3], screenY);
                if (point.get(1).getAsDouble() < 7.1) continue;
                head[0] = Math.min(head[0], screenX);
                head[1] = Math.min(head[1], screenY);
                head[2] = Math.max(head[2], screenX);
                head[3] = Math.max(head[3], screenY);
            }
        }
        JsonObject result = new JsonObject();
        result.addProperty("pose", sample.get("name").getAsString());
        result.addProperty("hand", leftHand ? "left" : "right");
        result.addProperty("aspect", aspect);
        result.add("full_bounds_xyxy", JSON.toJsonTree(full));
        result.add("head_and_haft_bounds_xyxy", JSON.toJsonTree(head));
        result.addProperty("nearest_camera_distance_blocks", nearest);
        return result;
    }

    private static JsonArray verifyFirstPerson(JsonObject checks, boolean enforce) {
        JsonArray results = new JsonArray();
        for (JsonElement element : checks.getAsJsonArray("samples")) {
            JsonObject sample = element.getAsJsonObject();
            String name = sample.get("name").getAsString();
            if (!(name.equals("closed") || name.startsWith("blood_idle:") || name.equals("blood_enter:1.4"))) continue;
            for (boolean leftHand : new boolean[] { false, true }) {
                for (double aspect : new double[] { 16.0/9, 16.0/10 }) {
                    JsonObject display = checks.getAsJsonObject("display").getAsJsonObject("firstperson_" + (leftHand ? "lefthand" : "righthand"));
                    JsonObject bounds = firstPersonBounds(sample, display, leftHand, aspect);
                    results.add(bounds);
                    JsonArray head = bounds.getAsJsonArray("head_and_haft_bounds_xyxy");
                    if (enforce && (head.get(0).getAsDouble() < 0.025 || head.get(1).getAsDouble() < 0.05
                            || head.get(2).getAsDouble() > 0.975 || head.get(3).getAsDouble() > 1.05
                            || (leftHand ? head.get(2).getAsDouble() > 0.475 : head.get(0).getAsDouble() < 0.525)
                            || bounds.get("nearest_camera_distance_blocks").getAsDouble() < 0.1))
                        throw new AssertionError("First-person weapon clips the viewport or covers the crosshair: " + bounds);
                }
            }
        }
        return results;
    }

    public static void main(String[] args) throws Exception {
        JsonObject geometry = JSON.fromJson(Files.readString(Path.of(args[0])), JsonObject.class)
                .getAsJsonArray("minecraft:geometry").get(0).getAsJsonObject();
        Bone[] rawBones = JSON.fromJson(geometry.get("bones"), Bone[].class);
        ModelProperties properties = JSON.fromJson(geometry.get("description"), ModelProperties.class);
        Model rawModel = new Model(FormatVersion.V_1_12_0, new MinecraftGeometry[] { new MinecraftGeometry(rawBones, null, properties) });
        BakedGeoModel baked = new BakedModelFactory.Builtin().constructGeoModel(GeometryTree.fromModel(rawModel));
        for (GeoBone bone : baked.topLevelBones()) index(bone);
        if (args.length > 3 && args[3].equals("dump-uv")) {
            for (GeoQuad face : BONES.get("handle").getCubes().get(0).quads()) {
                for (GeoVertex vertex : face.vertices()) System.out.println(face.direction() + " "
                        + new Vector3f(vertex.position()).mul(-16,16,16) + " uv=" + vertex.texU()*256 + "," + vertex.texV()*256);
            }
            return;
        }
        JsonObject checks = JSON.fromJson(Files.readString(Path.of(args[1])), JsonObject.class);
        if (args.length > 3 && args[3].equals("audit-hands")) {
            JsonArray audit = verifyPlayerHold(checks, false);
            Files.writeString(Path.of(args[2]), JSON.toJson(audit)+"\n");
            JsonObject summary = audit.get(0).getAsJsonObject().deepCopy();
            summary.remove("scene");
            System.out.println(summary);
            return;
        }
        if (args.length > 3 && args[3].equals("audit-display")) {
            JsonArray audit = verifyFirstPerson(checks, false);
            Files.writeString(Path.of(args[2]), JSON.toJson(audit)+"\n");
            System.out.println(audit.get(0));
            return;
        }
        for (JsonElement element : checks.getAsJsonArray("samples")) {
            JsonObject sample = element.getAsJsonObject();
            JsonObject pose = sample.getAsJsonObject("pose");
            for (Bone raw : rawBones) {
                GeoBone bone = BONES.get(raw.name());
                JsonObject state = pose.has(raw.name()) ? pose.getAsJsonObject(raw.name()) : new JsonObject();
                double[] rotation = vector(state, "rotation", 0);
                double[] scale = vector(state, "scale", 1);
                bone.setRotX((float) -Math.toRadians(raw.rotation()[0]+rotation[0]));
                bone.setRotY((float) -Math.toRadians(raw.rotation()[1]+rotation[1]));
                bone.setRotZ((float) Math.toRadians(raw.rotation()[2]+rotation[2]));
                bone.setScaleX((float) scale[0]);
                bone.setScaleY((float) scale[1]);
                bone.setScaleZ((float) scale[2]);
            }
            verifyVertices(sample);
        }
        verifyGrip(checks.getAsJsonObject("display"));
        JsonArray hands = verifyPlayerHold(checks, true);
        JsonArray viewports = verifyFirstPerson(checks, true);
        String result = "{\"status\":\"PASS\",\"native_geckolib\":\"4.8.4\",\"vertices\":" + vertices
                + ",\"maximum_error_model_units\":" + maximumError + ",\"uv_vertices\":" + uvVertices
                + ",\"native_item_transforms_checked\":true,\"upright_shaft_contexts\":4,\"shaft_axis_tolerance\":0.00001"
                + ",\"native_player_hand_samples\":" + hands.size() + ",\"palm_error_tolerance_model_units\":0.06,\"cutting_edge_forward_checked\":true"
                + ",\"first_person_viewport_samples\":" + viewports.size() + ",\"game_tested\":false}\n";
        Files.writeString(Path.of(args[2]).resolveSibling("view-validation.json"), JSON.toJson(viewports)+"\n");
        Files.writeString(Path.of(args[2]).resolveSibling("hand-validation.json"), JSON.toJson(hands)+"\n");
        Files.writeString(Path.of(args[2]), result);
        System.out.print(result);
    }
}
