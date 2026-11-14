/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.MobCategory
 *  net.minecraft.world.entity.decoration.ArmorStand
 *  net.minecraft.world.entity.monster.Enemy
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.ItemStack
 *  net.minecraftforge.common.MinecraftForge
 *  net.minecraftforge.event.entity.living.LivingHealEvent
 *  net.minecraftforge.event.entity.living.LivingHurtEvent
 *  net.minecraftforge.event.entity.player.AttackEntityEvent
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  net.minecraftforge.registries.ForgeRegistries
 */
package net.farmingtales.ftboss.integ.tridents;

import java.util.Set;
import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingHealEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;

public final class SanguineLifestealGuard {
    private static final String DUMMY_NAMESPACE = "dummmmmmy";
    private static final String TRIDENT_NAMESPACE = "tridents_n_stuff";
    private static final ResourceLocation SANGUINE_BOND = new ResourceLocation("tridents_n_stuff", "sanguine_bond");
    private static final String SANGUINE_NAME = "\u8840\u8165\u5951\u7ea6";
    private static final String STAMP_KEY = "ftboss:SanguineHitTick";
    private static final String HOSTILE_KEY = "ftboss:SanguineHitHostile";
    private static final String TARGET_KEY = "ftboss:SanguineHitTarget";
    private static final long WINDOW_TICKS = 1L;
    private static final Set<Float> SANGUINE_HEALS = Set.of(Float.valueOf(2.0f), Float.valueOf(4.0f), Float.valueOf(8.0f), Float.valueOf(16.0f), Float.valueOf(32.0f), Float.valueOf(2.5f), Float.valueOf(3.25f), Float.valueOf(4.75f), Float.valueOf(5.5f), Float.valueOf(6.25f));
    private static final String LOG_KEY = "ftboss:SanguineHealLog";
    private static final long LOG_THROTTLE_TICKS = 40L;

    private SanguineLifestealGuard() {
    }

    public static void register() {
        MinecraftForge.EVENT_BUS.register(SanguineLifestealGuard.class);
    }

    @SubscribeEvent
    public static void onAttack(AttackEntityEvent event) {
        try {
            Player player = event.getEntity();
            if (!(player instanceof ServerPlayer)) {
                return;
            }
            ServerPlayer player2 = (ServerPlayer)player;
            if (player2.m_9236_().f_46443_) {
                return;
            }
            Entity entity = event.getTarget();
            if (!(entity instanceof LivingEntity)) {
                return;
            }
            LivingEntity target = (LivingEntity)entity;
            if (!SanguineLifestealGuard.isSanguine(player2.m_21205_()) && !SanguineLifestealGuard.isSanguine(player2.m_21206_())) {
                return;
            }
            SanguineLifestealGuard.stamp(player2, target);
        }
        catch (Throwable t) {
            FTBossMod.LOGGER.warn("[FTBoss][\u8840\u8165\u5951\u7ea6] \u5438\u8840\u5224\u5b9a\u5f02\u5e38(\u5e73\u780d): {}", (Object)t.toString());
        }
    }

    @SubscribeEvent
    public static void onHurt(LivingHurtEvent event) {
        try {
            LivingEntity target = event.getEntity();
            if (target.m_9236_().f_46443_) {
                return;
            }
            Entity direct = event.getSource().m_7640_();
            if (direct == null || direct == event.getSource().m_7639_()) {
                return;
            }
            if (!TRIDENT_NAMESPACE.equals(SanguineLifestealGuard.namespaceOf(direct))) {
                return;
            }
            Entity entity = event.getSource().m_7639_();
            if (entity instanceof ServerPlayer) {
                ServerPlayer player = (ServerPlayer)entity;
                SanguineLifestealGuard.stamp(player, target);
            }
        }
        catch (Throwable t) {
            FTBossMod.LOGGER.warn("[FTBoss][\u8840\u8165\u5951\u7ea6] \u5438\u8840\u5224\u5b9a\u5f02\u5e38(\u6295\u63b7): {}", (Object)t.toString());
        }
    }

    @SubscribeEvent
    public static void onHeal(LivingHealEvent event) {
        try {
            LivingEntity livingEntity = event.getEntity();
            if (!(livingEntity instanceof ServerPlayer)) {
                return;
            }
            ServerPlayer player = (ServerPlayer)livingEntity;
            if (player.m_9236_().f_46443_) {
                return;
            }
            CompoundTag data = player.getPersistentData();
            long stamp = data.m_128454_(STAMP_KEY);
            if (stamp == 0L) {
                return;
            }
            long now = player.m_9236_().m_46467_();
            if (now < stamp || now - stamp > 1L) {
                return;
            }
            if (data.m_128471_(HOSTILE_KEY)) {
                return;
            }
            float amount = event.getAmount();
            if (!SANGUINE_HEALS.contains(Float.valueOf(amount))) {
                return;
            }
            event.setCanceled(true);
            if (now - data.m_128454_(LOG_KEY) >= 40L) {
                data.m_128356_(LOG_KEY, now);
                String name = data.m_128441_(TARGET_KEY) ? data.m_128461_(TARGET_KEY) : "\u975e\u654c\u5bf9\u76ee\u6807";
                FTBossMod.LOGGER.info("[FTBoss][\u8840\u8165\u5951\u7ea6] \u975e\u654c\u5bf9\u76ee\u6807\u4e0d\u5438\u8840: {} \u6253 {} \u53d6\u6d88\u56de\u590d {} \u70b9", new Object[]{player.m_7755_().getString(), name, Float.valueOf(amount)});
            }
        }
        catch (Throwable t) {
            FTBossMod.LOGGER.warn("[FTBoss][\u8840\u8165\u5951\u7ea6] \u5438\u8840\u5224\u5b9a\u5f02\u5e38(\u56de\u8840): {}", (Object)t.toString());
        }
    }

    private static void stamp(ServerPlayer player, LivingEntity target) {
        CompoundTag data = player.getPersistentData();
        data.m_128356_(STAMP_KEY, player.m_9236_().m_46467_());
        boolean hostile = SanguineLifestealGuard.isHostile(target);
        data.m_128379_(HOSTILE_KEY, hostile);
        if (hostile) {
            data.m_128473_(TARGET_KEY);
        } else {
            data.m_128359_(TARGET_KEY, target.m_7755_().getString());
        }
    }

    public static boolean isHostile(LivingEntity target) {
        if (target instanceof Player) {
            return false;
        }
        if (target instanceof ArmorStand) {
            return false;
        }
        if (DUMMY_NAMESPACE.equals(SanguineLifestealGuard.namespaceOf((Entity)target))) {
            return false;
        }
        if (target instanceof Enemy) {
            return true;
        }
        if (target instanceof EasterBossEntity) {
            return true;
        }
        return target.m_6095_().m_20674_() == MobCategory.MONSTER;
    }

    private static String namespaceOf(Entity entity) {
        ResourceLocation id = ForgeRegistries.ENTITY_TYPES.getKey((Object)entity.m_6095_());
        return id == null ? "" : id.m_135827_();
    }

    private static boolean isSanguine(ItemStack stack) {
        if (stack == null || stack.m_41619_()) {
            return false;
        }
        if (SANGUINE_BOND.equals((Object)ForgeRegistries.ITEMS.getKey((Object)stack.m_41720_()))) {
            return true;
        }
        return SANGUINE_NAME.equals(stack.m_41786_().getString().replace(" ", ""));
    }
}

