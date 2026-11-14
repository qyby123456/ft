package net.farmingtales.ftboss.item;

import java.util.Map;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.item.TridentItem;

public final class NativeProjectileBoundaryRules {
    private static final String OPERATION = "Lcom/llamalad7/mixinextras/injector/wrapoperation/Operation;";
    private static final String STACK = "Lnet/minecraft/world/item/ItemStack;";
    private static final String LEVEL = "Lnet/minecraft/world/level/Level;";
    private static final String LIVING = "Lnet/minecraft/world/entity/LivingEntity;";
    private static final String ARROW = "Lnet/minecraft/world/entity/projectile/AbstractArrow;";
    private static final String WEAR = "(" + STACK + "I" + LIVING + "Lnet/minecraft/world/entity/EquipmentSlot;" + OPERATION + ")V";
    private static final String ADD = "(" + LEVEL + "Lnet/minecraft/world/entity/Entity;" + OPERATION + ")Z";
    private static final Map<String, String> PROJECTILE = Map.of(
        "factory", "(Lnet/minecraft/world/item/ProjectileWeaponItem;" + ARROW + STACK + STACK + OPERATION + ")" + ARROW,
        "ammoFactory", "(Lnet/minecraft/world/item/ArrowItem;" + LEVEL + STACK + LIVING + STACK + OPERATION + ")" + ARROW,
        "wear", WEAR,
        "add", "(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/Entity;" + OPERATION + ")Z"
    );
    private static final Map<String, String> TRIDENT = Map.of(
        "factory", "(" + LEVEL + LIVING + STACK + OPERATION + ")Lnet/minecraft/world/entity/projectile/ThrownTrident;",
        "wear", WEAR,
        "add", ADD
    );
    private static final String APPLIED = "(" + STACK + "I" + OPERATION + "I" +
        "Lnet/minecraft/server/level/ServerLevel;" + LIVING + "Ljava/util/function/Consumer;)V";

    private NativeProjectileBoundaryRules() {
    }

    public static boolean originalSite(Class<?> declaring, String method, String descriptor, String kind, String site) {
        Map<String, String> expected;
        if (("BOW".equals(kind) || "CROSSBOW".equals(kind)) && declaring == ProjectileWeaponItem.class) {
            expected = PROJECTILE;
        } else if ("TRIDENT".equals(kind) && declaring == TridentItem.class) {
            expected = TRIDENT;
        } else {
            return false;
        }
        return method != null && method.endsWith("ftboss$v6$" + site) && descriptor != null && descriptor.equals(expected.get(site));
    }

    public static boolean appliedSite(Class<?> declaring, String method, String descriptor) {
        return declaring == ItemStack.class && method != null && method.endsWith("ftboss$v6$applied") && APPLIED.equals(descriptor);
    }

    public static boolean addBeforeWear(String kind) {
        return "BOW".equals(kind) || "CROSSBOW".equals(kind);
    }
}
