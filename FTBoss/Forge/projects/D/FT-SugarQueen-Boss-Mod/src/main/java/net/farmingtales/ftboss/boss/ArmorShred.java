package net.farmingtales.ftboss.boss;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

/**
 * 【护甲侵蚀】(2026-09-12 v24 用户定案)。
 *
 * <p>功底(彩蛋 Boss #3)用**黑色切割者**命中玩家 -> 叠 1 层: **每层 -5 护甲, 最多 5 层(-25)**,
 * 每层持续 {@value #DURATION_TICKS} tick(10 秒), 再被打就刷新时长。
 *
 * <p>原版没有"破甲"这种效果, 所以直接用 {@link AttributeModifier} 改玩家的 {@code Attributes.ARMOR};
 * 过期清理由庄园战场每 tick 调 {@link #tick(MinecraftServer)} 完成(功底死了也不会残留)。
 */
public final class ArmorShred {

    public static final int MAX_STACKS = 5;
    /** 每层扣多少护甲(负数) */
    public static final double PER_STACK = -5.0;
    public static final int DURATION_TICKS = 10 * 20;
    /** 固定 UUID, 每次叠层只是改这一个修饰符的数值 */
    private static final UUID MOD_ID = UUID.fromString("6f1a2d3c-1111-4a2b-9c3d-0000000000a1");

    private static final class St {
        int stacks;
        long expire;
        St(int s, long e) { this.stacks = s; this.expire = e; }
    }

    private static final Map<UUID, St> STATE = new HashMap<>();

    private ArmorShred() {}

    /** 叠一层(命中时调用), 返回叠完后的层数(1..5) */
    public static int addStack(LivingEntity target, long now) {
        St st = STATE.computeIfAbsent(target.getUUID(), k -> new St(0, 0L));
        st.stacks = Math.min(MAX_STACKS, st.stacks + 1);
        st.expire = now + DURATION_TICKS;
        apply(target, st.stacks);
        return st.stacks;
    }

    public static int stacks(LivingEntity target) {
        St st = STATE.get(target.getUUID());
        return st == null ? 0 : st.stacks;
    }

    /** 清掉某位玩家的破甲(战斗结束/玩家死亡时用) */
    public static void clear(LivingEntity target) {
        STATE.remove(target.getUUID());
        apply(target, 0);
    }

    private static void apply(LivingEntity target, int stacks) {
        var attr = target.getAttribute(Attributes.ARMOR);
        if (attr == null) return;
        attr.removeModifier(MOD_ID);
        if (stacks > 0) {
            attr.addTransientModifier(new AttributeModifier(MOD_ID, "ftboss.armor_shred",
                    PER_STACK * stacks, AttributeModifier.Operation.ADDITION));
        }
    }

    /** 过期清理(每 tick 跑一次; 由 ManorFight.pumpAll 调) ——
     *  v112 修复: 原来只在"庄园战场里有 Boss"时才跑, 战斗一结束/功底一死就再也不清,
     *  玩家身上那 -25 护甲会一直留着(会话内永久)。另外顺手处理"玩家已死"的残留。 */
    public static void tick(MinecraftServer server) {
        if (STATE.isEmpty() || server == null) return;
        long now = server.overworld().getGameTime();
        Iterator<Map.Entry<UUID, St>> it = STATE.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<UUID, St> en = it.next();
            Player p = server.getPlayerList().getPlayer(en.getKey());
            /* 玩家在线但已死(正在死亡动画/等待重生): 立刻清掉, 免得带着 -25 甲复活判定 */
            if (p != null && !p.isAlive()) {
                apply(p, 0);
                it.remove();
                continue;
            }
            if (now < en.getValue().expire) continue;
            if (p != null) apply(p, 0);
            it.remove();
        }
    }

    /** v112: 全清(庄园战结束/服务器停止这种"整批收尾"的场合用) */
    public static void clearAll(MinecraftServer server) {
        if (STATE.isEmpty()) return;
        if (server != null) {
            for (UUID id : new java.util.ArrayList<>(STATE.keySet())) {
                Player p = server.getPlayerList().getPlayer(id);
                if (p != null) apply(p, 0);
            }
        }
        STATE.clear();
    }
}
