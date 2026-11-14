/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.nbt.Tag
 *  net.minecraft.network.chat.Component
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.sounds.SoundEvent
 *  net.minecraft.sounds.SoundEvents
 *  net.minecraft.sounds.SoundSource
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.effect.MobEffect
 *  net.minecraft.world.effect.MobEffectInstance
 *  net.minecraft.world.effect.MobEffects
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.OwnableEntity
 *  net.minecraft.world.entity.ai.attributes.Attributes
 *  net.minecraft.world.entity.animal.horse.AbstractHorse
 *  net.minecraft.world.entity.decoration.ArmorStand
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.ClipContext
 *  net.minecraft.world.level.ClipContext$Block
 *  net.minecraft.world.level.ClipContext$Fluid
 *  net.minecraft.world.phys.AABB
 *  net.minecraft.world.phys.BlockHitResult
 *  net.minecraft.world.phys.HitResult$Type
 *  net.minecraft.world.phys.Vec3
 *  net.minecraftforge.event.entity.player.PlayerEvent$Clone
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  net.minecraftforge.registries.ForgeRegistries
 */
package net.farmingtales.ftboss.skill;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Optional;
import net.farmingtales.ftboss.item.FoodWeaponEvolution;
import net.farmingtales.ftboss.item.FoodWeaponLifecycle;
import net.farmingtales.ftboss.skill.WeaponEvolutionSkills;
import net.farmingtales.ftboss.skill.WeaponFx;
import net.farmingtales.ftboss.skill.WineMaceCombat;
import net.farmingtales.ftboss.util.NonManorWeaponRules;
import net.farmingtales.ftboss.util.WeaponFxRules;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;

public final class NonManorWeaponSkills {
    private NonManorWeaponSkills() {
    }

    @SubscribeEvent
    public static void clonePlayer(PlayerEvent.Clone event) {
        WeaponEvolutionSkills.copyCooldownData(event);
        CompoundTag previous = event.getOriginal().getPersistentData();
        if (previous.m_128441_("FTBossNonManorWeaponSkills")) {
            event.getEntity().getPersistentData().m_128365_("FTBossNonManorWeaponSkills", (Tag)previous.m_128469_("FTBossNonManorWeaponSkills").m_6426_());
        }
    }

    private static boolean execute(ServerPlayer player, NonManorWeaponRules.Skill skill) {
        ServerLevel level = player.m_284548_();
        if (skill == NonManorWeaponRules.Skill.ARC) {
            return NonManorWeaponSkills.chain(player, skill);
        }
        if (skill == NonManorWeaponRules.Skill.WINE) {
            return WineMaceCombat.begin(player);
        }
        Vec3 origin = skill.shape == NonManorWeaponRules.Shape.BURST ? NonManorWeaponSkills.aim(player, skill.range) : player.m_20182_().m_82520_(0.0, 0.4, 0.0);
        int hits = 0;
        if (skill.shape != NonManorWeaponRules.Shape.SELF) {
            double radius = skill.shape == NonManorWeaponRules.Shape.BURST ? skill.radius : skill.range;
            ArrayList<LivingEntity> candidates = new ArrayList<LivingEntity>(level.m_6443_(LivingEntity.class, new AABB(origin, origin).m_82400_(radius + 1.0), target -> NonManorWeaponSkills.canTarget(player, target)));
            candidates.sort(Comparator.comparingDouble(target -> target.m_20238_(origin)));
            for (LivingEntity target2 : candidates) {
                Vec3 direction;
                Vec3 position = target2.m_20182_().m_82520_(0.0, Math.min(0.5, (double)(target2.m_20206_() / 2.0f)), 0.0);
                double distanceSquared = position.m_82557_(origin);
                if (!NonManorWeaponRules.within(distanceSquared, radius) || skill.shape == NonManorWeaponRules.Shape.CONE && !NonManorWeaponRules.inCone(distanceSquared, (direction = target2.m_20191_().m_82399_().m_82546_(player.m_146892_()).m_82541_()).m_82526_(player.m_20154_()), radius) || skill.shape == NonManorWeaponRules.Shape.BURST && !NonManorWeaponSkills.clearLine(level, player, origin, target2.m_20191_().m_82399_())) continue;
                if (NonManorWeaponSkills.strike(player, target2, skill, 0, origin)) {
                    ++hits;
                }
                if (hits < skill.maxTargets) continue;
                break;
            }
        }
        switch (skill) {
            case MEAT: {
                player.m_5634_(NonManorWeaponRules.meatHealing(hits));
                break;
            }
            case THORNS: {
                if (hits <= 0) break;
                NonManorWeaponSkills.effect((LivingEntity)player, MobEffects.f_19605_, 100, 0, player);
                break;
            }
            case CHEESE: {
                NonManorWeaponSkills.effect((LivingEntity)player, MobEffects.f_19596_, 120, 1, player);
                break;
            }
            case APPLE: {
                player.m_21195_(MobEffects.f_19614_);
                player.m_5634_(6.0f);
                NonManorWeaponSkills.effect((LivingEntity)player, MobEffects.f_19605_, 100, 0, player);
                NonManorWeaponSkills.effect((LivingEntity)player, MobEffects.f_19596_, 100, 0, player);
                break;
            }
            case TIDE: {
                NonManorWeaponSkills.effect((LivingEntity)player, MobEffects.f_19606_, 120, 0, player);
                NonManorWeaponSkills.effect((LivingEntity)player, MobEffects.f_19608_, 240, 0, player);
                break;
            }
        }
        NonManorWeaponSkills.visual(player, skill, origin);
        return true;
    }

    static boolean canTarget(ServerPlayer player, LivingEntity target) {
        AbstractHorse horse;
        OwnableEntity owned;
        if (target == player || !target.m_6084_() || target.m_213877_() || target.m_5833_() || target instanceof ArmorStand || player.m_7307_((Entity)target) || target.m_7307_((Entity)player) || player.m_20365_((Entity)target) || !player.m_142582_((Entity)target)) {
            return false;
        }
        if (target instanceof OwnableEntity && (owned = (OwnableEntity)target).m_21805_() != null) {
            return false;
        }
        if (target instanceof AbstractHorse && (horse = (AbstractHorse)target).m_30614_()) {
            return false;
        }
        if (target instanceof Player) {
            Player other = (Player)target;
            return !other.m_7500_() && !other.m_5833_() && player.m_284548_().m_7654_().m_129799_() && player.m_7099_(other);
        }
        return true;
    }

    static boolean clearLine(ServerLevel level, ServerPlayer player, Vec3 start, Vec3 end) {
        return level.m_45547_(new ClipContext(start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, (Entity)player)).m_6662_() == HitResult.Type.MISS;
    }

    static Vec3 aim(ServerPlayer player, double range) {
        Vec3 start = player.m_146892_();
        Vec3 end = start.m_82549_(player.m_20154_().m_82490_(range));
        BlockHitResult block = player.m_284548_().m_45547_(new ClipContext(start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, (Entity)player));
        end = block.m_82450_();
        double nearest = start.m_82557_(end);
        for (LivingEntity target : player.m_284548_().m_6443_(LivingEntity.class, new AABB(start, end).m_82400_(0.3), candidate -> NonManorWeaponSkills.canTarget(player, candidate))) {
            Optional intersection = target.m_20191_().m_82400_(0.15).m_82371_(start, end);
            if (!intersection.isPresent() || !(start.m_82557_((Vec3)intersection.get()) < nearest)) continue;
            end = (Vec3)intersection.get();
            nearest = start.m_82557_(end);
        }
        return end.m_82546_(player.m_20154_().m_82490_(0.15));
    }

    private static boolean chain(ServerPlayer player, NonManorWeaponRules.Skill skill) {
        Vec3 impact = NonManorWeaponSkills.aim(player, skill.range);
        LivingEntity current = player.m_284548_().m_6443_(LivingEntity.class, new AABB(impact, impact).m_82400_(0.65), target -> NonManorWeaponSkills.canTarget(player, target)).stream().min(Comparator.comparingDouble(target -> target.m_20191_().m_82399_().m_82557_(impact))).orElse(null);
        if (current == null || !NonManorWeaponRules.within(player.m_146892_().m_82557_(current.m_20191_().m_82399_()), skill.range)) {
            return false;
        }
        HashSet<Integer> visited = new HashSet<Integer>();
        Vec3 previous = player.m_146892_();
        for (int index = 0; index < skill.maxTargets && current != null; ++index) {
            visited.add(current.m_19879_());
            Vec3 center = current.m_20191_().m_82399_();
            WeaponFx.beam(player.m_284548_(), previous, center, WeaponFxRules.Theme.ARC);
            NonManorWeaponSkills.strike(player, current, skill, index, previous);
            Vec3 lastCenter = center;
            previous = center;
            current = player.m_284548_().m_6443_(LivingEntity.class, new AABB(center, center).m_82400_(skill.radius), target -> !visited.contains(target.m_19879_()) && NonManorWeaponSkills.canTarget(player, target) && NonManorWeaponRules.within(target.m_20191_().m_82399_().m_82557_(lastCenter), skill.radius) && NonManorWeaponRules.within(player.m_146892_().m_82557_(target.m_20191_().m_82399_()), skill.range) && NonManorWeaponSkills.clearLine(player.m_284548_(), player, lastCenter, target.m_20191_().m_82399_())).stream().min(Comparator.comparingDouble(target -> target.m_20191_().m_82399_().m_82557_(lastCenter))).orElse(null);
        }
        NonManorWeaponSkills.sound(player, SoundEvents.f_12521_, 0.45f, 1.6f);
        return true;
    }

    private static boolean strike(ServerPlayer player, LivingEntity target, NonManorWeaponRules.Skill skill, int chainIndex, Vec3 origin) {
        float damage = NonManorWeaponRules.damage(skill, player.m_21133_(Attributes.f_22281_), chainIndex) * FoodWeaponEvolution.activeSkillMultiplier(player.m_21205_());
        if (!target.m_6469_(player.m_269291_().m_269075_((Player)player), damage)) {
            return false;
        }
        if (!target.m_6084_()) {
            return true;
        }
        switch (skill) {
            case FROST: {
                NonManorWeaponSkills.effect(target, MobEffects.f_19597_, 80, 1, player);
                break;
            }
            case MEAT: {
                NonManorWeaponSkills.knockAway(target, origin, 0.8);
                break;
            }
            case THORNS: {
                NonManorWeaponSkills.effect(target, MobEffects.f_19597_, 60, 2, player);
                break;
            }
            case CHEESE: {
                NonManorWeaponSkills.effect(target, MobEffects.f_19613_, 100, 0, player);
                break;
            }
            case ARC: {
                NonManorWeaponSkills.effect(target, MobEffects.f_19619_, 60, 0, player);
                NonManorWeaponSkills.effect(target, MobEffects.f_19597_, 40, 1, player);
                break;
            }
            case COMPOST: {
                NonManorWeaponSkills.effect(target, MobEffects.f_19614_, 100, 1, player);
                break;
            }
            case HOWL: {
                NonManorWeaponSkills.effect(target, MobEffects.f_19613_, 100, 0, player);
                NonManorWeaponSkills.effect(target, MobEffects.f_19619_, 100, 0, player);
                NonManorWeaponSkills.knockAway(target, origin, 0.6);
                break;
            }
            case BARREL: {
                NonManorWeaponSkills.effect(target, MobEffects.f_19597_, 60, 1, player);
                NonManorWeaponSkills.knockAway(target, origin, 0.65);
                break;
            }
            case TIDE: {
                NonManorWeaponSkills.knockAway(target, origin, 0.8);
                break;
            }
        }
        return true;
    }

    private static void effect(LivingEntity target, MobEffect effect, int ticks, int amplifier, ServerPlayer player) {
        target.m_147207_(new MobEffectInstance(effect, ticks, amplifier), (Entity)player);
    }

    static void knockAway(LivingEntity target, Vec3 origin, double strength) {
        Vec3 direction = target.m_20182_().m_82546_(origin).m_82542_(1.0, 0.0, 1.0).m_82541_();
        double resistance = Math.max(0.0, Math.min(1.0, target.m_21133_(Attributes.f_22278_)));
        double magnitude = strength * (1.0 - resistance);
        if (direction.m_82556_() < 1.0E-6 || magnitude <= 0.0) {
            return;
        }
        target.m_5997_(direction.f_82479_ * magnitude, 0.18 * (1.0 - resistance), direction.f_82481_ * magnitude);
        target.f_19864_ = true;
    }

    private static void visual(ServerPlayer player, NonManorWeaponRules.Skill skill, Vec3 origin) {
        ServerLevel level = player.m_284548_();
        WeaponFxRules.Theme theme = WeaponFxRules.forItem(skill.item);
        double radius = skill.shape == NonManorWeaponRules.Shape.SELF ? 1.2 : (skill.shape == NonManorWeaponRules.Shape.BURST ? skill.radius : skill.range);
        WeaponFx.send(level, skill.shape == NonManorWeaponRules.Shape.CONE ? WeaponFxRules.Kind.SWEEP : WeaponFxRules.Kind.BLAST, theme, origin, origin, radius, player.m_146908_());
        if (skill.shape == NonManorWeaponRules.Shape.BURST) {
            WeaponFx.beam(level, player.m_146892_(), origin, theme);
        }
        SoundEvent sound = switch (skill) {
            case NonManorWeaponRules.Skill.FROST -> SoundEvents.f_11983_;
            case NonManorWeaponRules.Skill.MEAT -> SoundEvents.f_11913_;
            case NonManorWeaponRules.Skill.THORNS, NonManorWeaponRules.Skill.APPLE -> SoundEvents.f_144243_;
            case NonManorWeaponRules.Skill.BARREL, NonManorWeaponRules.Skill.WINE -> SoundEvents.f_11911_;
            case NonManorWeaponRules.Skill.CHEESE -> SoundEvents.f_12321_;
            case NonManorWeaponRules.Skill.HOWL -> SoundEvents.f_12619_;
            case NonManorWeaponRules.Skill.COMPOST -> SoundEvents.f_12388_;
            case NonManorWeaponRules.Skill.TIDE -> SoundEvents.f_12517_;
            default -> SoundEvents.f_12317_;
        };
        NonManorWeaponSkills.sound(player, sound, 0.85f, 1.0f);
    }

    private static void sound(ServerPlayer player, SoundEvent sound, float volume, float pitch) {
        player.m_284548_().m_5594_(null, player.m_20183_(), sound, SoundSource.PLAYERS, volume, pitch);
    }

    public static void cast(ServerPlayer serverPlayer, ResourceLocation resourceLocation) {
        if (!serverPlayer.m_6084_() || serverPlayer.m_213877_() || serverPlayer.m_5833_() || serverPlayer.m_5803_() || serverPlayer.m_6117_()) {
            return;
        }
        ItemStack itemStack = serverPlayer.m_21205_();
        ResourceLocation resourceLocation2 = ForgeRegistries.ITEMS.getKey((Object)itemStack.m_41720_());
        if (itemStack.m_41619_() || resourceLocation2 == null || !resourceLocation2.equals((Object)resourceLocation) || FoodWeaponLifecycle.isBroken(itemStack)) {
            return;
        }
        if (WeaponEvolutionSkills.supports(itemStack)) {
            WeaponEvolutionSkills.cast(serverPlayer, itemStack);
            return;
        }
        NonManorWeaponRules.Skill skill = NonManorWeaponRules.forItem(resourceLocation2.toString());
        if (skill == null) {
            return;
        }
        long l = serverPlayer.m_284548_().m_7654_().m_129783_().m_46467_();
        CompoundTag compoundTag = serverPlayer.getPersistentData().m_128469_("FTBossNonManorWeaponSkills");
        if (compoundTag.m_128441_("request") && l >= compoundTag.m_128454_("request") && l - compoundTag.m_128454_("request") < 4L) {
            return;
        }
        compoundTag.m_128356_("request", l);
        serverPlayer.getPersistentData().m_128365_("FTBossNonManorWeaponSkills", (Tag)compoundTag);
        int n = NonManorWeaponRules.remaining(skill, l, compoundTag.m_128454_(skill.item));
        if (n > 0) {
            serverPlayer.m_5661_((Component)Component.m_237110_((String)"message.ftboss.non_manor_cooldown", (Object[])new Object[]{Component.m_237115_((String)skill.translationKey()), (n + 19) / 20}), true);
            return;
        }
        if (!NonManorWeaponSkills.execute(serverPlayer, skill)) {
            serverPlayer.m_5661_((Component)Component.m_237115_((String)"message.ftboss.non_manor_no_target"), true);
            return;
        }
        int n2 = FoodWeaponEvolution.skillCooldown(itemStack, skill.cooldownTicks);
        compoundTag.m_128356_(skill.item, l + (long)n2);
        itemStack.m_41622_(3, (LivingEntity)serverPlayer, holder -> holder.m_21190_(InteractionHand.MAIN_HAND));
        serverPlayer.m_21011_(InteractionHand.MAIN_HAND, true);
        serverPlayer.m_5661_((Component)Component.m_237110_((String)"message.ftboss.non_manor_cast", (Object[])new Object[]{Component.m_237115_((String)skill.translationKey()), n2 / 20}), true);
    }
}

