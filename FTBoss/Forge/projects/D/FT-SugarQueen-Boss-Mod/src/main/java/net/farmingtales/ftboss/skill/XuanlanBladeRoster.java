package net.farmingtales.ftboss.skill;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import net.farmingtales.ftboss.entity.PlayerMoonChakramEntity;
import net.farmingtales.ftboss.util.XuanlanRules;
import net.minecraft.server.level.ServerPlayer;

public final class XuanlanBladeRoster {
    private static final String[] KEYS = createKeys();

    private XuanlanBladeRoster() {}

    private static String[] createKeys() {
        String[] keys = new String[XuanlanRules.MAX_ACTIVE_BLADES];
        keys[0] = "ftboss:XuanlanBlade";
        for (int index = 1; index < keys.length; index++) keys[index] = "ftboss:XuanlanBlade" + (index + 1);
        return keys;
    }

    private static boolean valid(ServerPlayer owner, PlayerMoonChakramEntity blade) {
        return blade.isAlive() && !blade.isRemoved() && blade.level() == owner.serverLevel()
                && blade.getOwner() != null && owner.getUUID().equals(blade.getOwner().getUUID())
                && owner.serverLevel().getEntity(blade.getUUID()) == blade;
    }

    public static int count(ServerPlayer owner) {
        var data = owner.getPersistentData();
        Set<UUID> seen = new HashSet<>();
        int count = 0;
        for (String key : KEYS) {
            if (!data.hasUUID(key)) {
                data.remove(key);
                continue;
            }
            UUID identifier = data.getUUID(key);
            if (seen.contains(identifier) || !(owner.serverLevel().getEntity(identifier) instanceof PlayerMoonChakramEntity blade)
                    || !valid(owner, blade)) {
                data.remove(key);
                continue;
            }
            seen.add(identifier);
            count++;
        }
        return count;
    }

    public static int slot(ServerPlayer owner, PlayerMoonChakramEntity blade) {
        if (!valid(owner, blade)) return -1;
        var data = owner.getPersistentData();
        for (int index = 0; index < KEYS.length; index++) {
            if (data.hasUUID(KEYS[index]) && data.getUUID(KEYS[index]).equals(blade.getUUID())) return index;
        }
        return -1;
    }

    public static int track(ServerPlayer owner, PlayerMoonChakramEntity blade) {
        if (!valid(owner, blade)) return -1;
        int count = count(owner);
        int existing = slot(owner, blade);
        if (existing >= 0) return existing;
        if (count >= XuanlanRules.MAX_ACTIVE_BLADES) return -1;
        for (int index = 0; index < KEYS.length; index++) {
            if (owner.getPersistentData().hasUUID(KEYS[index])) continue;
            owner.getPersistentData().putUUID(KEYS[index], blade.getUUID());
            return index;
        }
        return -1;
    }

    public static int forget(ServerPlayer owner, PlayerMoonChakramEntity blade) {
        int found = -1;
        var data = owner.getPersistentData();
        for (int index = 0; index < KEYS.length; index++) {
            if (!data.hasUUID(KEYS[index]) || !data.getUUID(KEYS[index]).equals(blade.getUUID())) continue;
            data.remove(KEYS[index]);
            if (found < 0) found = index;
        }
        return found;
    }
}
