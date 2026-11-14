package net.farmingtales.ftboss.boss.ai;

import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.boss.ManorFightMembers;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.living.LivingExperienceDropEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * Suppresses every item and experience drop from a boss directly summoned by Lemon Cat.
 *
 * <p>The marker lives in Forge persistent data so saving/reloading cannot turn a temporary
 * helper into a reward-bearing normal boss. Manual death rewards guard this same marker.
 */
public final class LemonSummonRewards {
    public static final String NO_REWARDS_TAG = "ftboss:lemon_summon_no_rewards";
    public static final String POST_SACRIFICE_HELPER_TAG = "ftboss:lemon_post_sacrifice_helper";
    /**
     * ★ 2026-09-21：直接把**召唤它的柠檬猫 UUID** 烙在助战身上。
     *
     * <p>为什么不能再靠 {@code ManorFightMembers.ownerId(helper)}：那套是"根主人"语义 ——
     * {@code inherit()} 写的是 {@code root == null ? owner.getUUID() : root}，只要柠檬猫自己身上
     * 有过 OWNER（被别的路径 inherit 过），助战的 OWNER 就会指向**更上面那一层**，
     * 于是死亡回调里 {@code level.getEntity(ownerId) instanceof EasterBossEntity} 直接落空，
     * 计数一次都不加 —— 玩家看到的就是"献祭完一直在召唤、打满三只也不停"
     * （实机日志：16:13:10 献祭，16:14:40 又召一只，16:15:03 助战被击杀却**没有**
     * "献祭后助战 Boss 被击败 1/3"那行）。烙自己的 UUID 是唯一不依赖链路的办法。
     */
    public static final String POST_SACRIFICE_OWNER_TAG = "ftboss:lemon_post_sacrifice_owner";
    private static final String DEFEAT_COUNTED_TAG = "ftboss:lemon_helper_defeat_counted";

    private LemonSummonRewards() {}

    public static void mark(Entity entity) {
        if (entity != null) entity.getPersistentData().putBoolean(NO_REWARDS_TAG, true);
    }

    public static boolean suppressed(Entity entity) {
        return entity != null && entity.getPersistentData().getBoolean(NO_REWARDS_TAG);
    }

    public static void markPostSacrificeHelper(Entity entity, java.util.UUID ownerId) {
        if (entity == null) return;
        entity.getPersistentData().putBoolean(POST_SACRIFICE_HELPER_TAG, true);
        if (ownerId != null) entity.getPersistentData().putUUID(POST_SACRIFICE_OWNER_TAG, ownerId);
    }

    /**
     * LOWEST runs after the food bosses' revive/totem handlers. A canceled first death therefore never consumes
     * one of Lemon Cat's three helper defeats; timeout and cleanup use discard(), so they never enter this event.
     */
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void countPostSacrificeHelperDefeat(LivingDeathEvent event) {
        Entity helper = event.getEntity();
        if (!helper.getPersistentData().getBoolean(POST_SACRIFICE_HELPER_TAG)
                || helper.getPersistentData().getBoolean(DEFEAT_COUNTED_TAG)
                || !(helper.level() instanceof ServerLevel level)) return;
        /* ★ 优先用召唤时烙下的 UUID；老存档没有这个 tag 时才退回原来的主人链路。 */
        java.util.UUID stamped = helper.getPersistentData().hasUUID(POST_SACRIFICE_OWNER_TAG)
                ? helper.getPersistentData().getUUID(POST_SACRIFICE_OWNER_TAG) : null;
        java.util.UUID ownerId = stamped != null ? stamped : ManorFightMembers.ownerId(helper);
        if (ownerId == null || !(level.getEntity(ownerId) instanceof EasterBossEntity lemon)) {
            FTBossMod.LOGGER.warn("[FTBoss][Lemon] 助战 {} 死亡但找不到召唤者(烙印={}, ownerId={}) -> 本次不计入三只",
                    helper.getName().getString(), stamped, ownerId);
            helper.getPersistentData().putBoolean(DEFEAT_COUNTED_TAG, true);   // 避免每次死亡都刷同一行
            return;
        }
        if (!lemon.onPostSacrificeHelperDefeated(helper)) return;
        helper.getPersistentData().putBoolean(DEFEAT_COUNTED_TAG, true);
        FTBossMod.LOGGER.info("[FTBoss][Lemon] 确认助战 Boss 真正死亡: {}", helper.getName().getString());
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void suppressDrops(LivingDropsEvent event) {
        if (!suppressed(event.getEntity())) return;
        event.getDrops().clear();
        event.setCanceled(true);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void suppressExperience(LivingExperienceDropEvent event) {
        if (suppressed(event.getEntity())) event.setDroppedExperience(0);
    }
}
