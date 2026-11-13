import com.google.gson.GsonBuilder;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

public final class GuardTable424 {
    private static final String PROOF = "net.farmingtales.ftboss.item.NativeCapabilityInstallationProof170";
    private static final String PREFIX = "net/farmingtales/ftboss/item/";

    private static final class GuardLoader extends ClassLoader {
        private final Path root;
        private final String changed;
        private final boolean missing;

        private GuardLoader(Path root,String changed,boolean missing) {
            super(GuardTable424.class.getClassLoader());
            this.root = root;
            this.changed = changed;
            this.missing = missing;
        }

        @Override
        protected Class<?> loadClass(String name,boolean resolve) throws ClassNotFoundException {
            if (!name.equals(PROOF)) return super.loadClass(name,resolve);
            synchronized (getClassLoadingLock(name)) {
                Class<?> type = findLoadedClass(name);
                if (type == null) try {
                    byte[] bytes = Files.readAllBytes(root.resolve(name.replace('.','/')+".class"));
                    type = defineClass(name,bytes,0,bytes.length);
                } catch (java.io.IOException failure) {throw new ClassNotFoundException(name,failure);}
                if (resolve) resolveClass(type);
                return type;
            }
        }

        @Override
        public InputStream getResourceAsStream(String name) {
            Path path = root.resolve(name);
            if (name.equals(changed) && missing) return null;
            if (!Files.isRegularFile(path)) return super.getResourceAsStream(name);
            try {
                byte[] bytes = Files.readAllBytes(path);
                if (name.equals(changed)) bytes[bytes.length-1] ^= 1;
                return new ByteArrayInputStream(bytes);
            } catch (java.io.IOException failure) {throw new IllegalStateException(failure);}
        }
    }

    private static Class<?> proof(Path root,String changed,boolean missing) throws Exception {
        return new GuardLoader(root,changed,missing).loadClass(PROOF);
    }

    private static void guards(Class<?> proof) throws Exception {
        Method method = proof.getDeclaredMethod("guards");
        method.setAccessible(true);
        method.invoke(null);
    }

    private static Map<String,String> bindings(Class<?> proof) throws Exception {
        Field field = proof.getDeclaredField("GUARDS");
        field.setAccessible(true);
        Map<?,?> original = (Map<?,?>)field.get(null);
        Map<String,String> result = new LinkedHashMap<>();
        for (var entry : original.entrySet()) result.put((String)entry.getKey(),(String)entry.getValue());
        return result;
    }

    public static void main(String[] arguments) throws Exception {
        Path parent = Path.of(arguments[0]), current = Path.of(arguments[1]);
        Class<?> parentProof = proof(parent,null,false), currentProof = proof(current,null,false);
        guards(parentProof);
        guards(currentProof);
        Map<String,String> before = bindings(parentProof), after = bindings(currentProof);
        if (before.size() != 200 || !after.keySet().containsAll(before.keySet()) || after.size() != 203) throw new AssertionError("Exact original 180 guard keys required");
        List<String> rebound = new ArrayList<>();
        for (String name : new TreeSet<>(before.keySet())) if (!before.get(name).equals(after.get(name))) rebound.add(name);
        if (!rebound.equals(List.of("FoodWeaponSourceLedger","NativeExpectedFieldObservation","NativeExpectedFieldObservation$Fact","NativeExpectedFieldObservation$ReadBuilder","NativeExpectedFieldObservation$ReadOps","NativeExpectedFieldObservation$ReadRegistries","OwnerFieldInstallationProof"))) throw new AssertionError("Unexpected binding change");
        List<Map<String,Object>> rejected = new ArrayList<>();
        for (String name : new TreeSet<>(after.keySet())) for (boolean missing : List.of(false,true)) {
            try {
                guards(proof(current,((name.startsWith("mixin/") || name.startsWith("util/") || name.startsWith("transaction/")) ? "net/farmingtales/ftboss/" : PREFIX)+name+".class",missing));
            } catch (InvocationTargetException failure) {
                Throwable cause = failure.getCause();
                if (!(cause instanceof IllegalStateException) || !cause.getMessage().equals("Actual strict physical guard changed: "+name)) throw failure;
                rejected.add(Map.of("guard",name,"missing",missing,"actualRejection",cause.getMessage()));
                continue;
            }
            throw new AssertionError("Actual guard accepted missing/changed resource: "+name);
        }
        if (rejected.size() != 406) throw new AssertionError("All guard counterexamples required");
        Map<String,Object> report = new LinkedHashMap<>();
        report.put("failures",0);
        report.put("parentAndCurrentActualGuardsAccepted",true);
        report.put("guardCount",after.size());
        report.put("onlyFourFenceBindingsChanged",rebound);
        report.put("actualRejectedCounterexamples",rejected);
        report.put("actualRejectedCounterexampleCount",rejected.size());
        report.put("privateGuardTableOrAuthorityWritten",false);
        report.put("fullInstallationProofOrRuntimeAcceptance",false);
        report.put("installable",false);
        Files.writeString(Path.of(arguments[2]),new GsonBuilder().setPrettyPrinting().create().toJson(report));
        System.out.println("actual guards=203 rejected=406 failures=0");
    }
}
