import java.lang.reflect.Field;
import java.util.*;
import net.farmingtales.ftboss.util.*;
import net.farmingtales.ftboss.util.FoodWeaponBuildRules.Material;
import net.farmingtales.ftboss.util.foundations.*;

/** 环境和配方材料准入审计；只读取生产包中的纯规则。 */
public final class MaterialAudit {
    public static void main(String[] arguments) throws Exception {
        var identifiers = new TreeMap<String, Set<String>>();
        Field field = SwordCompatibilityRules.class.getDeclaredField("ITEM_VALUES");
        field.setAccessible(true);
        for (Object key : ((Map<?, ?>) field.get(null)).keySet()) add(identifiers, key.toString(), "sword-values");
        for (var definition : SwordCompatibilityRules.definitions().values()) {
            for (var piece : definition.budget()) add(identifiers, piece.itemId(), "sword-" + piece.origin());
            for (String item : WeaponCavityShapeRules.environment(definition.form().name().toLowerCase(Locale.ROOT), definition.weaponId())) add(identifiers, item, "sword-display");
        }
        for (var profile : FourWeaponFoundationProfiles.examples().values()) {
            for (String item : profile.items().keySet()) add(identifiers, item, "foundation-values");
            for (String item : WeaponCavityShapeRules.environment(profile.family(), profile.id())) add(identifiers, item, "foundation-display");
        }
        int missing = 0;
        for (var entry : identifiers.entrySet()) if (SwordCompatibilityRules.material(entry.getKey()) == Material.ORIGINAL) {
            System.out.println(entry.getKey() + " " + entry.getValue());
            missing++;
        }
        System.out.println("audited=" + identifiers.size() + " missing-global=" + missing);
    }
    private static void add(Map<String, Set<String>> values, String id, String source) { values.computeIfAbsent(id, ignored -> new TreeSet<>()).add(source); }
}
