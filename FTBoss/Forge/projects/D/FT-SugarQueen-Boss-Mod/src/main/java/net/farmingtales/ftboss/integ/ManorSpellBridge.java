package net.farmingtales.ftboss.integ;

import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.minecraft.world.entity.LivingEntity;

public final class ManorSpellBridge {
    private static Handler handler = (boss, target, spell) -> false;

    private ManorSpellBridge() {}

    public static void install(Handler replacement) {
        handler = replacement;
    }

    public static boolean replace(EasterBossEntity boss, LivingEntity target, String spell) {
        return handler.replace(boss, target, spell);
    }

    @FunctionalInterface
    public interface Handler {
        boolean replace(EasterBossEntity boss, LivingEntity target, String spell);
    }
}
