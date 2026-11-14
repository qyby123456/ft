package net.farmingtales.ftboss.turret;

import java.util.UUID;
import net.farmingtales.ftboss.balance.ManorBalanceHooks;
import net.farmingtales.ftboss.boss.ManorFight;
import net.farmingtales.ftboss.boss.ManorFightMembers;
import net.farmingtales.ftboss.boss.ai.ZhuniPairCombat;
import net.farmingtales.ftboss.boss.ai.ZhuniPairRules;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** 竹鸟代理玄蓝入场，保留庄园当前 Boss 身份，避免误判击败或阶段跳过。 */
@Mod.EventBusSubscriber(modid = "ftboss")
public final class ZhuniStage {
    private static final String STATE = "ftboss.zhuniStage";
    private static final String BIRD = "ftboss.zhuniId";

    private ZhuniStage() {}

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void joined(EntityJoinLevelEvent event) {
        if (!(event.getEntity() instanceof EasterBossEntity boss) || event.getLevel().isClientSide()
                || boss.getVariant() != 6 || !ManorBalanceHooks.mainBoss(boss)
                || ManorFightMembers.fightId(boss) == null || ManorFightMembers.ownerId(boss) != null) return;
        CompoundTag data = boss.getPersistentData();
        if (!data.contains(STATE)) {
            data.putInt(STATE, 1);
            data.putBoolean("ftboss.zhuniOriginalAi", boss.isNoAi());
            data.putBoolean("ftboss.zhuniOriginalInvulnerable", boss.isInvulnerable());
            data.putBoolean("ftboss.zhuniOriginalInvisible", boss.isInvisible());
            data.putBoolean("ftboss.zhuniOriginalHidden", boss.isManorIntroHidden());
        }
        if (data.getInt(STATE) == 1) hide(boss);
    }

    private static void hide(EasterBossEntity boss) {
        boss.setNoAi(true);
        boss.setInvulnerable(true);
        boss.setInvisible(true);
        boss.setManorIntroHidden(true);
        if (ZhuniPairCombat.xuanlanDefeated(boss)) boss.noPhysics = true;
        syncBossBar(boss);
    }

    public static boolean waiting(EasterBossEntity boss) { return boss.getPersistentData().getInt(STATE) == 1; }

    public static void syncBossBar(EasterBossEntity boss) {
        if (!(boss.level() instanceof ServerLevel level) || !boss.isAlive()
                || net.farmingtales.ftboss.boss.HiddenBossIntro.isActing(boss.getUUID())) return;
        var bar = boss.getBossBar();
        var players = ManorBalanceHooks.players(boss, level);
        Entity entity = boss.getPersistentData().hasUUID(BIRD) ? level.getEntity(boss.getPersistentData().getUUID(BIRD)) : null;
        boolean xuanlanDead = ZhuniPairCombat.xuanlanDefeated(boss);
        boolean birdDead = ZhuniPairCombat.zhuniDefeated(boss);
        float birdHealth = entity instanceof ZhuniEntity bird ? bird.getHealth() : ZhuniPairRules.HEALTH;
        bar.setName(Component.literal("竹鸟 " + (birdDead ? "已阵亡" : (int) Math.ceil(birdHealth) + "/2000")
                + " · 玄蓝 " + (xuanlanDead ? "已阵亡" : (int) Math.ceil(boss.getHealth()) + "/2000")));
        bar.setProgress(ZhuniPairRules.progress(boss.getHealth(), birdHealth, xuanlanDead, birdDead));
        for (var player : java.util.List.copyOf(bar.getPlayers())) {
            if (!players.contains(player)) bar.removePlayer(player);
        }
        for (var player : players) bar.addPlayer(player);
        bar.setVisible(true);
    }

    public static void reveal(EasterBossEntity boss, ZhuniEntity bird) {
        if (!waiting(boss) || !boss.isAlive()) return;
        CompoundTag data = boss.getPersistentData();
        data.putInt(STATE, 2);
        boss.setNoAi(data.getBoolean("ftboss.zhuniOriginalAi"));
        boss.setInvulnerable(data.getBoolean("ftboss.zhuniOriginalInvulnerable"));
        boss.setInvisible(data.getBoolean("ftboss.zhuniOriginalInvisible"));
        boss.setManorIntroHidden(data.getBoolean("ftboss.zhuniOriginalHidden"));
        boss.getBossBar().setVisible(true);
        bird.revealedAt(boss.level().getGameTime());
        if (boss.level() instanceof ServerLevel level) {
            for (var player : ManorBalanceHooks.players(boss, level))
                player.sendSystemMessage(Component.literal("§a竹鸟振翅！§b玄蓝登场。"));
        }
    }

    public static boolean hold(EasterBossEntity boss) {
        if (!(boss.level() instanceof ServerLevel level) || boss.getVariant() != 6
                || !ManorBalanceHooks.mainBoss(boss) || !boss.isAlive() || ManorFightMembers.ownerId(boss) != null) return false;
        CompoundTag data = boss.getPersistentData();
        int state = data.getInt(STATE);
        ZhuniPairCombat.normalize(boss);
        if (ZhuniPairCombat.zhuniDefeated(boss)) {
            if (level.getGameTime() % 20 == 0) ZhuniPairCombat.replenish(boss, level);
            syncBossBar(boss);
            return false;
        }
        if (state == 0 && ManorFightMembers.fightId(boss) != null
                && ManorFight.hasActive(ManorFightMembers.fightId(boss))
                && !net.farmingtales.ftboss.boss.HiddenBossIntro.isActing(boss.getUUID())) {
            data.putInt(STATE, 1);
            data.putBoolean("ftboss.zhuniOriginalAi", boss.isNoAi());
            data.putBoolean("ftboss.zhuniOriginalInvulnerable", boss.isInvulnerable());
            data.putBoolean("ftboss.zhuniOriginalInvisible", boss.isInvisible());
            data.putBoolean("ftboss.zhuniOriginalHidden", boss.isManorIntroHidden());
            state = 1;
        }
        if (state != 1 && state != 2) return false;
        UUID fight = ManorFightMembers.fightId(boss);
        if (fight == null || !ManorFight.hasActive(fight)) return false;
        Entity existing = data.hasUUID(BIRD) ? level.getEntity(data.getUUID(BIRD)) : null;
        if (existing == null && data.hasUUID(BIRD)) return state == 1 || ZhuniPairCombat.xuanlanDefeated(boss);
        if (!(existing instanceof ZhuniEntity) || !boss.getUUID().equals(((ZhuniEntity) existing).ownerId())) {
            ZhuniEntity bird = new ZhuniEntity(XuanlanTurrets.BIRD, level);
            bird.setOwner(boss);
            bird.moveTo(boss.getX(), boss.getY() + 0.65, boss.getZ(), boss.getYRot(), 0);
            bird.revealedAt(level.getGameTime());
            ManorFightMembers.inherit(boss, bird);
            if (level.addFreshEntity(bird)) data.putUUID(BIRD, bird.getUUID());
        }
        if (ZhuniPairCombat.xuanlanDefeated(boss)) {
            hide(boss);
            return true;
        }
        if (state != 1) return false;
        hide(boss);
        return true;
    }
}
