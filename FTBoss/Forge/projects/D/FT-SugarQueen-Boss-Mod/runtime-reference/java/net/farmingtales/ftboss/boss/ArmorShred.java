/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.server.MinecraftServer
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.ai.attributes.AttributeInstance
 *  net.minecraft.world.entity.ai.attributes.AttributeModifier
 *  net.minecraft.world.entity.ai.attributes.AttributeModifier$Operation
 *  net.minecraft.world.entity.ai.attributes.Attributes
 */
package net.farmingtales.ftboss.boss;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

public final class ArmorShred {
    public static final int MAX_STACKS = 5;
    public static final double PER_STACK = -5.0;
    public static final int DURATION_TICKS = 200;
    private static final UUID MOD_ID = UUID.fromString("6f1a2d3c-1111-4a2b-9c3d-0000000000a1");
    private static final Map<UUID, St> STATE = new HashMap<UUID, St>();

    private ArmorShred() {
    }

    public static int addStack(LivingEntity target, long now) {
        St st = STATE.computeIfAbsent(target.m_20148_(), k -> new St(0, 0L));
        st.stacks = Math.min(5, st.stacks + 1);
        st.expire = now + 200L;
        ArmorShred.apply(target, st.stacks);
        return st.stacks;
    }

    public static int stacks(LivingEntity target) {
        St st = STATE.get(target.m_20148_());
        return st == null ? 0 : st.stacks;
    }

    public static void clear(LivingEntity target) {
        STATE.remove(target.m_20148_());
        ArmorShred.apply(target, 0);
    }

    private static void apply(LivingEntity target, int stacks) {
        AttributeInstance attr = target.m_21051_(Attributes.f_22284_);
        if (attr == null) {
            return;
        }
        attr.m_22120_(MOD_ID);
        if (stacks > 0) {
            attr.m_22118_(new AttributeModifier(MOD_ID, "ftboss.armor_shred", -5.0 * (double)stacks, AttributeModifier.Operation.ADDITION));
        }
    }

    public static void tick(MinecraftServer server) {
        if (STATE.isEmpty() || server == null) {
            return;
        }
        long now = server.m_129783_().m_46467_();
        Iterator<Map.Entry<UUID, St>> it = STATE.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<UUID, St> en = it.next();
            ServerPlayer p = server.m_6846_().m_11259_(en.getKey());
            if (p != null && !p.m_6084_()) {
                ArmorShred.apply((LivingEntity)p, 0);
                it.remove();
                continue;
            }
            if (now < en.getValue().expire) continue;
            if (p != null) {
                ArmorShred.apply((LivingEntity)p, 0);
            }
            it.remove();
        }
    }

    public static void clearAll(MinecraftServer server) {
        if (STATE.isEmpty()) {
            return;
        }
        if (server != null) {
            for (UUID id : new ArrayList<UUID>(STATE.keySet())) {
                ServerPlayer p = server.m_6846_().m_11259_(id);
                if (p == null) continue;
                ArmorShred.apply((LivingEntity)p, 0);
            }
        }
        STATE.clear();
    }

    private static final class St {
        int stacks;
        long expire;

        St(int s, long e) {
            this.stacks = s;
            this.expire = e;
        }
    }
}

