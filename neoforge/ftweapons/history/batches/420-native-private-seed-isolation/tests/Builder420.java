import com.google.gson.GsonBuilder;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.lang.reflect.*;
import java.nio.file.*;
import java.security.MessageDigest;
import java.util.*;
import net.minecraft.nbt.*;
import net.farmingtales.ftboss.util.foundations.FoundationCanonicalDigest;

public final class Builder420 {
    private static byte[] binary(Tag value) throws Exception {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        value.write(new DataOutputStream(bytes));
        return bytes.toByteArray();
    }

    private static void equal(CompoundTag actual, CompoundTag expected, Tag payload) throws Exception {
        if (!Arrays.equals(binary(actual), binary(expected)) || actual.get("PaidMaterialDefinitions") != payload) {
            throw new AssertionError("Fingerprint output binary or payload identity differs");
        }
    }

    private static int builder(String name, boolean seeded) throws Exception {
        Class<?> type = Class.forName(name);
        Constructor<?> constructor = seeded ? type.getDeclaredConstructor(CompoundTag.class) : type.getDeclaredConstructor();
        constructor.setAccessible(true);
        Method integer = type.getDeclaredMethod("putInt", String.class, int.class);
        Method string = type.getDeclaredMethod("putString", String.class, String.class);
        Method tag = type.getDeclaredMethod("put", String.class, Tag.class);
        Method finish = type.getDeclaredMethod("finish");
        for (Method method : List.of(integer, string, tag, finish)) method.setAccessible(true);
        for (int index = 0; index < 128; index++) {
            CompoundTag seed = new CompoundTag();
            for (int key = 0; key < index; key++) seed.putInt("Key" + key, key);
            seed.putString("Profile", "existing");
            CompoundTag expected = seeded ? seed.copy() : new CompoundTag();
            CompoundTag privateSeed = seeded ? seed.copy() : null;
            byte[] privateSeedBefore = seeded ? binary(privateSeed) : null;
            Object draft = seeded ? constructor.newInstance(privateSeed) : constructor.newInstance();
            int number = index == 0 ? Integer.MIN_VALUE : index == 1 ? Integer.MAX_VALUE : index;
            integer.invoke(draft, "Level", number); expected.putInt("Level", number);
            string.invoke(draft, "Profile", "中文" + index); expected.putString("Profile", "中文" + index);
            CompoundTag payload = new CompoundTag(); payload.putInt("paid", index);
            tag.invoke(draft, "PaidMaterialDefinitions", payload); expected.put("PaidMaterialDefinitions", payload);
            string.invoke(draft, "Profile", "replacement" + index); expected.putString("Profile", "replacement" + index);
            Agent404.watched = payload; Agent404.reject = true; Agent404.writes = 0;
            CompoundTag actual;
            try { actual = (CompoundTag) finish.invoke(draft); } finally { Agent404.reject = false; }
            if (Agent404.writes != 0) throw new AssertionError("Referenced paid payload mutated");
            equal(actual, expected, payload);
            if (seeded && (actual == privateSeed || !Arrays.equals(privateSeedBefore, binary(privateSeed)))) throw new AssertionError("Private seed was reused or changed");
            if (!seed.getString("Profile").equals("existing")) throw new AssertionError("Original snapshot mutated");
            payload.putInt("paid", index + 1);
            equal(actual, expected, payload);
        }
        Agent404.watched = null;
        return 128;
    }

    public static void main(String[] arguments) throws Exception {
        int cases = 0;
        cases += builder("net.farmingtales.ftboss.util.foundations.FoundationAuthorRuleSnapshots$PrivateFingerprint416", false);
        cases += builder("net.farmingtales.ftboss.util.foundations.FoundationAuthorRuleSnapshots$PrivateFingerprint416", true);
        cases += builder("net.farmingtales.ftboss.util.foundations.FoundationCanonicalDigest$PrivateFingerprint416", false);
        cases += builder("net.farmingtales.ftboss.item.FoodWeaponMaterialFingerprint$PrivateFingerprint416", false);
        cases += builder("net.farmingtales.ftboss.item.FourWeaponFoundationAdapter$PrivateSnapshot407", false);
        for (int index = 0; index < 128; index++) {
            CompoundTag payload = new CompoundTag();
            payload.putInt("paid", index);
            CompoundTag expected = new CompoundTag();
            expected.putString("Domain", "author-rules" + index);
            expected.putInt("DigestVersion", 3);
            expected.put("Value", payload);
            String digest = HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(FoundationCanonicalDigest.canonical(expected)));
            if (!digest.equals(FoundationCanonicalDigest.fingerprint("author-rules" + index, payload))) throw new AssertionError("Canonical digest changed");
        }
        try { FoundationCanonicalDigest.fingerprint("author-rules", null); throw new AssertionError("Null digest accepted"); }
        catch (IllegalArgumentException expected) { if (!"null-digest-input".equals(expected.getMessage())) throw expected; }
        Method helper = Class.forName("net.farmingtales.ftboss.item.NativeExpectedFieldObservation")
            .getDeclaredMethod("fingerprintCompoundForRead416", CompoundTag.class, Map.class);
        int rejected = 0;
        CompoundTag owner = new CompoundTag(); owner.putInt("existing", 9);
        byte[] original = binary(owner);
        for (CompoundTag seed : Arrays.asList(null, owner)) {
            for (Map<String, Tag> fields : Arrays.<Map<String, Tag>>asList(null, new HashMap<>(), new LinkedHashMap<>())) {
                try { helper.invoke(null, seed, fields); throw new AssertionError("Foreign fingerprint output caller accepted"); }
                catch (InvocationTargetException denied) { if (!(denied.getCause() instanceof IllegalStateException)) throw denied; rejected++; }
            }
        }
        if (!Arrays.equals(original, binary(owner))) throw new AssertionError("Foreign seed changed on rejection");
        Files.writeString(Path.of(arguments[0]), new GsonBuilder().setPrettyPrinting().create().toJson(Map.of(
            "failures", 0, "builderBinaryAndPayloadIdentityCases", cases, "canonicalDigestParityCases", 128,
            "foreignCallerRejections", rejected, "nullDigestRejectionPreserved", true,
            "actualRuntimeTested", false, "installable", false)));
    }
}
