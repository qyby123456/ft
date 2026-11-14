package net.farmingtales.ftboss.boss;

import java.util.ArrayList;
import java.util.Set;
import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.boss.ai.BossDamageGate;
import net.farmingtales.ftboss.boss.ai.MinionBlessing;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

@Mod.EventBusSubscriber(modid = FTBossMod.MOD_ID)
public final class ManorImpossibleRules {
    public static final Set<String> BANNED_BUFF_SPELLS = Set.of("heartstop", "oakskin");
    /** 终结难度清掉的食物增益（三样全清）。 */
    public static final Set<String> BANNED_FOOD_BUFFS = Set.of(
            "kaleidoscope_cookery:satiated_shield", "farmersdelight:nourishment", "farm_and_charm:satiation");

    /**
     * ★ 2026-09-24 用户要求「禁用部分铁魔法」并点名「饱腹代偿」：**两档难度都清**这一样。
     *
     * <p>2026-09-20 曾按用户要求整体移除普通难度的禁食（见 {@code docs/MANOR_PLAYER_BUFFS_20260920.md}），
     * 本次是部分回滚：普通难度只清「饱腹代偿」{@code kaleidoscope_cookery:satiated_shield}，
     * 「滋养」「饱腹」以及其它食物增益照旧放行。终结难度仍是上面那三样全清。
     */
    public static final Set<String> SHARED_BANNED_FOOD_BUFFS = Set.of(
            "kaleidoscope_cookery:satiated_shield");

    private ManorImpossibleRules() {}

    public static boolean isAttack(DamageSource source) {
        return attackDifficulty(source) == ManorDifficulty.IMPOSSIBLE;
    }

    private static ManorDifficulty attackDifficulty(DamageSource source) {
        Entity owner = source.getEntity();
        if (owner == null) owner = source.getDirectEntity();
        if (owner == null) owner = ManorMercy.damageOwner();
        ManorDifficulty difficulty = null;
        for (int depth = 0; owner != null && depth < 8; depth++) {
            if (owner instanceof EasterBossEntity boss) {
                if (boss.isImpossibleChallenge()) return ManorDifficulty.IMPOSSIBLE;
                difficulty = ManorDifficulty.NORMAL;
            }
            var fight = ManorFightMembers.fightId(owner);
            if (fight != null) {
                difficulty = ManorFight.difficulty(fight);
                if (difficulty == ManorDifficulty.IMPOSSIBLE) return difficulty;
            }
            Entity parent = ManorFightMembers.owner(owner);
            if (parent == null && owner.level() instanceof ServerLevel level) {
                var identifier = MinionBlessing.ownerId(owner);
                if (identifier != null) parent = level.getEntity(identifier);
            }
            if (parent == owner) break;
            owner = parent;
        }
        return difficulty;
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onHurt(LivingHurtEvent event) {
        if (event.isCanceled() || !(event.getEntity() instanceof Player player) || player.level().isClientSide
                || !player.isAlive() || player.isCreative() || player.isSpectator()
                || !Float.isFinite(event.getAmount()) || event.getAmount() <= 0) return;
        ManorDifficulty difficulty = attackDifficulty(event.getSource());
        if (difficulty == null) return;
        float multiplier = difficulty == ManorDifficulty.NORMAL ? ManorDifficulty.NORMAL_DAMAGE_MULTIPLIER
                : ManorDifficulty.NORMAL_DAMAGE_MULTIPLIER * ManorDifficulty.IMPOSSIBLE_ATTACK_MULTIPLIER;
        event.setAmount(event.getAmount() * multiplier);
    }

    /**
     * 清掉玩家身上被禁的食物增益。★ 2026-09-24：普通难度也清「饱腹代偿」了，
     * 所以判据从「只有终结难度」放宽成「在庄园里打架（任一难度）」。
     */
    public static int purgeFoodBuffs(ServerPlayer player) {
        boolean impossible = ManorFight.isImpossibleFor(player);
        if (!impossible && !ManorFight.isPlayerFighting(player)) return 0;
        Set<String> banned = impossible ? BANNED_FOOD_BUFFS : SHARED_BANNED_FOOD_BUFFS;
        int removed = 0;
        for (var effect : new ArrayList<>(player.getActiveEffects())) {
            var key = ForgeRegistries.MOB_EFFECTS.getKey(effect.getEffect());
            if (key != null && banned.contains(key.toString()) && player.removeEffect(effect.getEffect())) removed++;
        }
        if (removed > 0) {
            ManorDifficulty difficulty = impossible ? ManorDifficulty.IMPOSSIBLE : ManorDifficulty.NORMAL;
            ManorBan.scold(player, difficulty.label() + "禁用的食物增益（" + removed + " 项）");
        }
        return removed;
    }

    @SubscribeEvent
    public static void onTick(LivingEvent.LivingTickEvent event) {
        if (event.getEntity().level().isClientSide) return;
        if (event.getEntity() instanceof EasterBossEntity boss && boss.isImpossibleChallenge()) BossDamageGate.tick(boss);
        if (event.getEntity() instanceof ServerPlayer player && player.tickCount % 10 == 0) purgeFoodBuffs(player);
    }
}
