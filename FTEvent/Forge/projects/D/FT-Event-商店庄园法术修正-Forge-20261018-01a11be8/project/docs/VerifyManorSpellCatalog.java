import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class VerifyManorSpellCatalog {
    public static void main(String[] arguments) throws Exception {
        Class<?> kindType = Class.forName("net.farmingtales.ftboss.util.ManorSpellRules$Kind");
        Object[] kinds = kindType.getEnumConstants();
        if (kinds == null || kinds.length == 0) throw new IllegalStateException("Manor spell catalog is empty");
        Set<String> unique = new HashSet<>();
        List<String> quotedIds = new ArrayList<>();
        for (Object kind : kinds) {
            String path = (String) kindType.getField("id").get(kind);
            if (!path.matches("[a-z0-9_/.-]+") || !unique.add(path)) {
                throw new IllegalStateException("Invalid or duplicate manor spell ID: " + path);
            }
            quotedIds.add("\"ftboss:" + path + "\"");
        }
        System.out.println("[" + String.join(",", quotedIds) + "]");
    }
}
