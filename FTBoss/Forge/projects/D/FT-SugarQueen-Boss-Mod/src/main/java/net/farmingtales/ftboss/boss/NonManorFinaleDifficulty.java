package net.farmingtales.ftboss.boss;

import java.util.ArrayList;
import java.util.List;
import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.entity.AppleBossEntity;
import net.farmingtales.ftboss.entity.BengerEntity;
import net.farmingtales.ftboss.entity.CompostBossEntity;
import net.farmingtales.ftboss.entity.DefectCoreEntity;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.entity.MeatMountainEntity;
import net.farmingtales.ftboss.entity.RatBossEntity;
import net.farmingtales.ftboss.entity.SugarQueenEntity;
import net.farmingtales.ftboss.entity.TeaTreeBossEntity;
import net.farmingtales.ftboss.entity.TideFishBossEntity;
import net.farmingtales.ftboss.entity.WineBarrelBossEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

/** Terminal-difficulty state shared by every regular (non-Manor) FT boss. */
@Mod.EventBusSubscriber(modid = FTBossMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class NonManorFinaleDifficulty {
    public static final double ATTRIBUTE_MULTIPLIER = 2.0D;
    public static final int CAST_SPEED_MULTIPLIER = 2;
    private static final String PREPARED = "ftboss:regular_finale_prepared";
    private static final String FINALE = "ftboss:regular_finale";
    private static final String BASE_PREFIX = "ftboss:regular_finale_base/";
    private static final String SCALING_BASE_HP = "ftboss_base_hp";

    private NonManorFinaleDifficulty() {}

    /** Marks newly summoned regular bosses from the closest summoning player's statue preference. */
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onJoin(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide
                || !(event.getLevel() instanceof ServerLevel level)
                || !(event.getEntity() instanceof LivingEntity boss) || !isRegularBoss(boss)) return;
        if (event.loadedFromDisk()) {
            // 兼容修复前已保存的终结 Boss：载入时补齐最大生命，并保留当前血量比例。
            ensureFinaleMaximumHealth(boss, false);
            return;
        }
        ServerPlayer summoner = null;
        double best = Double.MAX_VALUE;
        for (ServerPlayer candidate : level.getEntitiesOfClass(ServerPlayer.class,
                boss.getBoundingBox().inflate(16.0D), player -> player.isAlive() && !player.isSpectator())) {
            double distance = candidate.distanceToSqr(boss);
            if (distance < best) {
                best = distance;
                summoner = candidate;
            }
        }
        prepare(boss, summoner != null
                && BossChallengePreference.selected(summoner) == ManorDifficulty.IMPOSSIBLE);
    }

    /** Explicit entry used by summons with their own finale override (currently Saya's sneak summon). */
    public static void prepare(LivingEntity boss, boolean finale) {
        if (boss == null || boss.level().isClientSide || !isRegularBoss(boss)) return;
        CompoundTag data = boss.getPersistentData();
        if (data.getBoolean(PREPARED)) return;

        List<AttributeBase> bases = new ArrayList<>();
        // 与 1.21.1 的实机修复保持一致：MAX_HEALTH 不保证出现在同步属性集合中，必须显式加入。
        AttributeInstance maximumHealth = boss.getAttribute(Attributes.MAX_HEALTH);
        rememberBase(data, bases, maximumHealth);
        for (AttributeInstance instance : boss.getAttributes().getSyncableAttributes()) {
            if (instance == maximumHealth) continue;
            rememberBase(data, bases, instance);
        }
        data.putBoolean(PREPARED, true);
        data.putBoolean(FINALE, finale);

        // Keep Saya's legacy flag, boss-bar label and finale loot compatible with the unified state.
        if (boss instanceof TeaTreeBossEntity saya) saya.setFinaleChallenge(finale);
        if (finale) {
            for (AttributeBase entry : bases) {
                entry.instance().setBaseValue(entry.instance().getAttribute()
                        .sanitizeValue(entry.base() * ATTRIBUTE_MULTIPLIER));
            }
            boss.setHealth(boss.getMaxHealth());
            FTBossMod.LOGGER.info("[FTBoss][终结难度] {} 属性×2、施法速度×2", boss.getName().getString());
        }
    }

    private static void rememberBase(CompoundTag data, List<AttributeBase> bases, AttributeInstance instance) {
        if (instance == null) return;
        ResourceLocation id = ForgeRegistries.ATTRIBUTES.getKey(instance.getAttribute());
        if (id == null) return;
        String key = BASE_PREFIX + id;
        if (!data.contains(key)) data.putDouble(key, instance.getBaseValue());
        bases.add(new AttributeBase(instance, data.getDouble(key)));
    }

    private static void ensureFinaleMaximumHealth(LivingEntity boss, boolean fullHeal) {
        if (!isFinale(boss)) return;
        AttributeInstance maximumHealth = boss.getAttribute(Attributes.MAX_HEALTH);
        if (maximumHealth == null) return;
        ResourceLocation id = ForgeRegistries.ATTRIBUTES.getKey(maximumHealth.getAttribute());
        if (id == null) return;

        CompoundTag data = boss.getPersistentData();
        String key = BASE_PREFIX + id;
        if (!data.contains(key)) {
            // 旧版人数缩放已保存的 K_BASE_HP 是未乘人数的真实基准，可避免把存档时的多人倍率再翻倍。
            double base = data.contains(SCALING_BASE_HP)
                    ? data.getDouble(SCALING_BASE_HP) : maximumHealth.getBaseValue();
            data.putDouble(key, base);
        }
        double desired = maximumHealth.getAttribute().sanitizeValue(
                data.getDouble(key) * ATTRIBUTE_MULTIPLIER);
        float oldMaximum = boss.getMaxHealth();
        float ratio = oldMaximum > 0.0F ? boss.getHealth() / oldMaximum : 1.0F;
        if (Math.abs(maximumHealth.getBaseValue() - desired) > 0.01D) {
            maximumHealth.setBaseValue(desired);
            if (fullHeal) boss.setHealth(boss.getMaxHealth());
            else boss.setHealth(Math.max(1.0F, Math.min(boss.getMaxHealth(), ratio * boss.getMaxHealth())));
        }
    }

    public static boolean isFinale(LivingEntity boss) {
        return boss != null && (boss.getPersistentData().getBoolean(FINALE)
                || boss instanceof TeaTreeBossEntity saya && saya.isFinaleChallenge());
    }

    /** Halves wind-up, interval and cooldown while preserving a one-tick lower bound. */
    public static int acceleratedTicks(LivingEntity boss, int normalTicks) {
        if (normalTicks <= 0) return normalTicks;
        return isFinale(boss) ? Math.max(1, (normalTicks + CAST_SPEED_MULTIPLIER - 1)
                / CAST_SPEED_MULTIPLIER) : normalTicks;
    }

    public static boolean isRegularBoss(LivingEntity boss) {
        if (boss instanceof EasterBossEntity) return false;
        if (boss instanceof RatBossEntity rat) return !rat.isMinion();
        if (boss instanceof TeaTreeBossEntity saya) return !saya.isTeaMinion();
        return boss instanceof SugarQueenEntity || boss instanceof MeatMountainEntity
                || boss instanceof WineBarrelBossEntity || boss instanceof DefectCoreEntity
                || boss instanceof CompostBossEntity || boss instanceof AppleBossEntity
                || boss instanceof TideFishBossEntity || boss instanceof BengerEntity;
    }

    private record AttributeBase(AttributeInstance instance, double base) {}
}
