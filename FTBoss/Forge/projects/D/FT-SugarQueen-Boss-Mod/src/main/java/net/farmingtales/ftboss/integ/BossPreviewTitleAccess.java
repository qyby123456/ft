package net.farmingtales.ftboss.integ;

import java.util.Optional;
import net.farmingtales.ftboss.util.BossPreviewRules;
import net.minecraft.world.entity.player.Player;

/** 只认 Titles 当前佩戴头衔；2026-10-12：不扫描已解锁列表、昵称或背包。 */
public final class BossPreviewTitleAccess {
    private BossPreviewTitleAccess() {}

    public static boolean wearingManorTitle(Player player) {
        try {
            Class<?> manager = Class.forName("aurilux.titles.common.core.TitleManager");
            Object capability = manager.getMethod("getCapability", Player.class).invoke(null, player);
            if (capability == null) return false;
            Object resolved = capability.getClass().getMethod("resolve").invoke(capability);
            Object holder = resolved instanceof Optional<?> optional ? optional.orElse(null) : null;
            if (holder == null) return false;
            Object selected = holder.getClass().getMethod("getDisplayTitle").invoke(holder);
            if (selected == null) return false;
            Object identifier = selected.getClass().getMethod("getID").invoke(selected);
            return identifier != null && BossPreviewRules.MANOR_TITLE.equals(identifier.toString());
        } catch (ReflectiveOperationException | LinkageError exception) {
            return false;
        }
    }
}
