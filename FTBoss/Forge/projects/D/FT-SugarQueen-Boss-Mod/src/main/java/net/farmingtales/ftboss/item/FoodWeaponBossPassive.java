package net.farmingtales.ftboss.item;

import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.item.FoodWeaponEvolution.WeaponDetailView;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Boss 系列原生武器的额外固有被动；2026-10-12，不占进化、祝福或携带技能位置。 */
@Mod.EventBusSubscriber(modid = FTBossMod.MOD_ID)
public final class FoodWeaponBossPassive {
    private static final int PROC_COOLDOWN_TICKS = 60;
    private static final int EFFECT_DURATION_TICKS = 40;
    private static final String TIMER_ROOT = "FTBossIntrinsicPassiveTimers";
    private static final String PROJECTILE_PROFILE = "FTBossIntrinsicProjectileProfile";

    private FoodWeaponBossPassive() {}

    public static String profile(ItemStack weapon) {
        return FoodWeaponEvolution.isNativeWeapon(weapon) ? FoodWeaponEvolution.firstProfileId(weapon) : "";
    }

    public static WeaponDetailView detail(ItemStack weapon) {
        String profile = profile(weapon);
        return profile.isEmpty() ? null : new WeaponDetailView(Component.translatable("screen.ftboss.skills.boss_passive"),
                Component.translatable("passive.ftboss.boss_weapon." + profile),
                Component.translatable("passive.ftboss.boss_weapon." + profile + ".description"));
    }

    private static boolean ready(ServerPlayer player, String profile) {
        long now = player.serverLevel().getServer().overworld().getGameTime();
        CompoundTag timers = player.getPersistentData().getCompound(TIMER_ROOT);
        long previous = timers.getLong(profile);
        if (timers.contains(profile) && now >= previous && now - previous < PROC_COOLDOWN_TICKS) return false;
        timers.putLong(profile, now);
        player.getPersistentData().put(TIMER_ROOT, timers);
        return true;
    }

    @SubscribeEvent
    public static void projectile(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide || !(event.getEntity() instanceof Projectile projectile)
                || !(projectile.getOwner() instanceof ServerPlayer player) || projectile.getPersistentData().contains(PROJECTILE_PROFILE)) return;
        CompoundTag snapshot = projectile.getPersistentData().getCompound(WeaponMaterialBuild.ROOT);
        if (!snapshot.hasUUID("Owner") || !snapshot.getUUID("Owner").equals(player.getUUID())) return;
        projectile.getPersistentData().putString(PROJECTILE_PROFILE, snapshot.getString("NativeProfile"));
    }

    private static String attackProfile(ServerPlayer player, DamageSource source) {
        if (source.getDirectEntity() == player) return profile(FoodWeaponRanged.sourceWeapon(source, player));
        if (source.getDirectEntity() instanceof Projectile projectile && projectile.getOwner() == player) {
            return projectile.getPersistentData().getString(PROJECTILE_PROFILE);
        }
        return "";
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void attack(LivingHurtEvent event) {
        if (event.getAmount() <= 0 || !(event.getSource().getEntity() instanceof ServerPlayer player)
                || !player.isAlive() || player.isSpectator() || !(event.getEntity() instanceof Monster target)
                || target.isAlliedTo(player) || event.getSource().getDirectEntity() != player
                && !(event.getSource().getDirectEntity() instanceof Projectile)) return;
        String profile = attackProfile(player, event.getSource());
        if (profile.equals("benger") && target.getHealth() <= target.getMaxHealth() * 0.30F) {
            event.setAmount(event.getAmount() * 1.10F);
            return;
        }
        if (!profile.equals("sugar") && !profile.equals("butcher") && !profile.equals("saya")
                && !profile.equals("wine") && !profile.equals("defect")) return;
        if (!ready(player, profile)) return;
        switch (profile) {
            case "sugar" -> target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, EFFECT_DURATION_TICKS, 0), player);
            case "butcher" -> player.heal(1.0F);
            case "saya" -> target.addEffect(new MobEffectInstance(MobEffects.POISON, EFFECT_DURATION_TICKS, 0), player);
            case "wine" -> target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, EFFECT_DURATION_TICKS, 0), player);
            case "defect" -> event.setAmount(event.getAmount() + 1.0F);
            default -> {}
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void defend(LivingHurtEvent event) {
        if (event.getAmount() <= 0 || !(event.getEntity() instanceof ServerPlayer player)
                || !player.isAlive() || player.isSpectator()) return;
        String profile = profile(player.getMainHandItem());
        if (profile.equals("compost")) event.setAmount(event.getAmount() * 0.92F);
        if (profile.equals("tide") && event.getSource().getDirectEntity() instanceof AbstractArrow) {
            event.setAmount(event.getAmount() * 0.90F);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void kill(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof Monster target) || !(event.getSource().getEntity() instanceof ServerPlayer player)
                || !player.isAlive() || player.isSpectator() || target.isAlliedTo(player)
                || net.farmingtales.ftboss.boss.ai.LemonSummonRewards.suppressed(target)) return;
        String profile = attackProfile(player, event.getSource());
        if (!profile.equals("rat") && !profile.equals("apple") || !ready(player, profile)) return;
        if (profile.equals("rat")) player.getFoodData().eat(1, 0.0F);
        else player.heal(1.0F);
    }

    @SubscribeEvent
    public static void tooltip(ItemTooltipEvent event) {
        WeaponDetailView detail = detail(event.getItemStack());
        if (detail == null) return;
        event.getToolTip().add(detail.category().copy().append(Component.literal(": ")).append(detail.title()).withStyle(ChatFormatting.GOLD));
        event.getToolTip().add(detail.description().copy().withStyle(ChatFormatting.GRAY));
    }
}
