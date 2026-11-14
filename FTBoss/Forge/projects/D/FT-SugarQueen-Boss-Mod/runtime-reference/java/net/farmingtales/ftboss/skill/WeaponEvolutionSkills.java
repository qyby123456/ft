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
 *  net.minecraft.world.entity.ai.attributes.Attributes
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.phys.AABB
 *  net.minecraft.world.phys.Vec3
 *  net.minecraftforge.event.entity.player.PlayerEvent$Clone
 *  net.minecraftforge.registries.ForgeRegistries
 */
package net.farmingtales.ftboss.skill;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import net.farmingtales.ftboss.item.FoodWeaponEvolution;
import net.farmingtales.ftboss.item.FoodWeaponLifecycle;
import net.farmingtales.ftboss.item.FoodWeaponProgression;
import net.farmingtales.ftboss.item.FoodWeaponSkillRarity;
import net.farmingtales.ftboss.item.weapons.GourmetEvolutionWeapon;
import net.farmingtales.ftboss.skill.NonManorWeaponSkills;
import net.farmingtales.ftboss.skill.SpellVisuals;
import net.farmingtales.ftboss.util.SpellVisualRules;
import net.farmingtales.ftboss.util.WeaponEvolutionSkillPool;
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
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.registries.ForgeRegistries;

public final class WeaponEvolutionSkills {
    public static final String COOLDOWN_TAG = "FTBossEvolutionSkillCooldowns";
    private static final int REQUEST_INTERVAL = 4;
    private static final int MAX_TARGETS = 32;

    private WeaponEvolutionSkills() {
    }

    public static boolean supports(ItemStack stack) {
        return WeaponEvolutionSkills.state(stack).evolved();
    }

    public static void copyCooldownData(PlayerEvent.Clone event) {
        CompoundTag previous = event.getOriginal().getPersistentData();
        if (previous.m_128441_(COOLDOWN_TAG)) {
            event.getEntity().getPersistentData().m_128365_(COOLDOWN_TAG, (Tag)previous.m_128469_(COOLDOWN_TAG).m_6426_());
        }
    }

    private static Component skillName(State state, WeaponEvolutionSkillPool.Fusion fusion, boolean secondary) {
        if (state.stage() == 1) {
            return Component.m_237115_((String)state.first().translationKey());
        }
        return Component.m_237115_((String)(secondary ? fusion.secondaryTranslationKey() : fusion.primaryTranslationKey()));
    }

    private static Shape primaryShape(WeaponEvolutionSkillPool.Aspect first, WeaponEvolutionSkillPool.Aspect second) {
        WeaponEvolutionSkillPool.Aspect other;
        if (first == second) {
            return WeaponEvolutionSkills.fromBase(first.style);
        }
        WeaponEvolutionSkillPool.Aspect aspect = first == WeaponEvolutionSkillPool.Aspect.APPLE ? second : (other = second == WeaponEvolutionSkillPool.Aspect.APPLE ? first : null);
        if (other != null) {
            return WeaponEvolutionSkills.fromBase(other.style);
        }
        if (first == WeaponEvolutionSkillPool.Aspect.ARC || second == WeaponEvolutionSkillPool.Aspect.ARC) {
            return Shape.CHAIN;
        }
        if (first == WeaponEvolutionSkillPool.Aspect.SHEPHERD || second == WeaponEvolutionSkillPool.Aspect.SHEPHERD) {
            return Shape.CONE;
        }
        if (first.style == WeaponEvolutionSkillPool.BaseStyle.BURST || second.style == WeaponEvolutionSkillPool.BaseStyle.BURST) {
            return Shape.BURST;
        }
        if (first.style == WeaponEvolutionSkillPool.BaseStyle.NOVA || second.style == WeaponEvolutionSkillPool.BaseStyle.NOVA) {
            return Shape.NOVA;
        }
        return Shape.CONE;
    }

    private static Shape fromBase(WeaponEvolutionSkillPool.BaseStyle style) {
        return switch (style) {
            default -> throw new IncompatibleClassChangeError();
            case WeaponEvolutionSkillPool.BaseStyle.SELF -> Shape.SELF;
            case WeaponEvolutionSkillPool.BaseStyle.CONE, WeaponEvolutionSkillPool.BaseStyle.DASH -> Shape.CONE;
            case WeaponEvolutionSkillPool.BaseStyle.NOVA -> Shape.NOVA;
            case WeaponEvolutionSkillPool.BaseStyle.BURST -> Shape.BURST;
            case WeaponEvolutionSkillPool.BaseStyle.CHAIN -> Shape.CHAIN;
        };
    }

    private static Outcome execute(ServerPlayer player, WeaponEvolutionSkillPool.Aspect first, WeaponEvolutionSkillPool.Aspect second, Shape shape, double flat, double multiplier, double range, double radius, int maxTargets, boolean ultimate, boolean ward, String visual, String accentVisual) {
        boolean supportive;
        if (shape == Shape.SELF) {
            WeaponEvolutionSkills.applyBoons(player, first, second, 0, ultimate || ward);
            Vec3 center = player.m_20182_().m_82520_(0.0, 0.1, 0.0);
            WeaponEvolutionSkills.show(player, visual, accentVisual, center, false);
            WeaponEvolutionSkills.sound(player, first);
            return new Outcome(true, 0, center);
        }
        if (shape == Shape.CHAIN) {
            return WeaponEvolutionSkills.chain(player, first, second, flat, multiplier, range, radius, maxTargets, ultimate, ward, visual, accentVisual);
        }
        if (shape == Shape.BEAM) {
            return WeaponEvolutionSkills.beam(player, first, second, flat, multiplier, range, maxTargets, ultimate, visual, accentVisual);
        }
        ServerLevel level = player.m_284548_();
        Vec3 origin = shape == Shape.BURST || shape == Shape.VORTEX ? NonManorWeaponSkills.aim(player, range) : player.m_20182_().m_82520_(0.0, 0.35, 0.0);
        double reach = shape == Shape.CONE ? range : radius;
        List<LivingEntity> targets = WeaponEvolutionSkills.targets(level, player, origin, reach + 1.0);
        int hits = 0;
        double damage = WeaponEvolutionSkills.damage(player, flat, multiplier);
        for (LivingEntity target : targets) {
            Vec3 center = target.m_20191_().m_82399_();
            double distanceSquared = center.m_82557_(origin);
            if (shape == Shape.CONE) {
                Vec3 direction = center.m_82546_(player.m_146892_()).m_82541_();
                if (!NonManorWeaponSkills.clearLine(level, player, player.m_146892_(), center) || !WeaponEvolutionSkills.within(distanceSquared, range) || direction.m_82526_(player.m_20154_()) < 0.32) {
                    continue;
                }
            } else if (!WeaponEvolutionSkills.within(distanceSquared, radius) || (shape == Shape.BURST || shape == Shape.VORTEX) && !NonManorWeaponSkills.clearLine(level, player, origin, center)) continue;
            if (shape == Shape.VORTEX) {
                WeaponEvolutionSkills.pullToward(target, origin, 0.75);
            }
            if (!target.m_6469_(player.m_269291_().m_269075_((Player)player), (float)damage)) continue;
            WeaponEvolutionSkills.applyTraits(target, player, first, second, origin, ultimate, shape == Shape.VORTEX);
            if (++hits < maxTargets) continue;
            break;
        }
        boolean bl = supportive = ward || first == WeaponEvolutionSkillPool.Aspect.APPLE || second == WeaponEvolutionSkillPool.Aspect.APPLE;
        if (hits == 0 && !supportive) {
            return new Outcome(false, 0, origin);
        }
        WeaponEvolutionSkills.applyBoons(player, first, second, hits, ultimate || ward);
        WeaponEvolutionSkills.show(player, visual, accentVisual, origin, ultimate);
        WeaponEvolutionSkills.sound(player, first);
        return new Outcome(true, hits, origin);
    }

    private static Outcome chain(ServerPlayer player, WeaponEvolutionSkillPool.Aspect first, WeaponEvolutionSkillPool.Aspect second, double flat, double multiplier, double range, double jumpRange, int maxTargets, boolean ultimate, boolean ward, String visual, String accentVisual) {
        boolean supportive;
        ServerLevel level = player.m_284548_();
        Vec3 impact = NonManorWeaponSkills.aim(player, range);
        LivingEntity current = WeaponEvolutionSkills.targets(level, player, impact, 1.0).stream().findFirst().orElse(null);
        boolean bl = supportive = ward || first == WeaponEvolutionSkillPool.Aspect.APPLE || second == WeaponEvolutionSkillPool.Aspect.APPLE;
        if (current == null) {
            if (!supportive) {
                return new Outcome(false, 0, impact);
            }
            WeaponEvolutionSkills.applyBoons(player, first, second, 0, true);
            WeaponEvolutionSkills.show(player, visual, accentVisual, player.m_20182_(), true);
            return new Outcome(true, 0, player.m_20182_());
        }
        HashSet<Integer> visited = new HashSet<Integer>();
        Vec3 previous = player.m_146892_();
        int hits = 0;
        int limit = Math.min(maxTargets, ultimate ? 7 : 5);
        double baseDamage = WeaponEvolutionSkills.damage(player, flat, multiplier);
        while (current != null && hits < limit) {
            visited.add(current.m_19879_());
            Vec3 center = current.m_20191_().m_82399_();
            SpellVisuals.beam(level, (LivingEntity)player, visual, previous, center);
            double attenuation = Math.max(0.55, 1.0 - (double)hits * 0.1);
            if (current.m_6469_(player.m_269291_().m_269075_((Player)player), (float)(baseDamage * attenuation))) {
                WeaponEvolutionSkills.applyTraits(current, player, first, second, previous, ultimate, false);
                ++hits;
            }
            Vec3 searchCenter = center;
            previous = center;
            current = WeaponEvolutionSkills.targets(level, player, center, Math.max(4.5, jumpRange)).stream().filter(candidate -> !visited.contains(candidate.m_19879_()) && NonManorWeaponSkills.clearLine(level, player, searchCenter, candidate.m_20191_().m_82399_())).min(Comparator.comparingDouble(candidate -> candidate.m_20238_(searchCenter))).orElse(null);
        }
        WeaponEvolutionSkills.applyBoons(player, first, second, hits, ultimate || ward);
        WeaponEvolutionSkills.show(player, accentVisual, null, impact, ultimate);
        WeaponEvolutionSkills.sound(player, WeaponEvolutionSkillPool.Aspect.ARC);
        return new Outcome(hits > 0 || supportive, hits, impact);
    }

    private static Outcome beam(ServerPlayer player, WeaponEvolutionSkillPool.Aspect first, WeaponEvolutionSkillPool.Aspect second, double flat, double multiplier, double range, int maxTargets, boolean ultimate, String visual, String accentVisual) {
        ServerLevel level = player.m_284548_();
        Vec3 start = player.m_146892_();
        Vec3 end = NonManorWeaponSkills.aim(player, range);
        AABB bounds = new AABB(start, end).m_82400_(2.0);
        ArrayList<LivingEntity> candidates = new ArrayList<LivingEntity>(level.m_6443_(LivingEntity.class, bounds, target -> NonManorWeaponSkills.canTarget(player, target)));
        candidates.sort(Comparator.comparingDouble(target -> WeaponEvolutionSkills.projection(start, end, target.m_20191_().m_82399_())));
        int hits = 0;
        double damage = WeaponEvolutionSkills.damage(player, flat, multiplier);
        for (LivingEntity target2 : candidates) {
            Vec3 center = target2.m_20191_().m_82399_();
            if (WeaponEvolutionSkills.distanceToSegmentSqr(center, start, end) > 2.25 || !NonManorWeaponSkills.clearLine(level, player, start, center) || !target2.m_6469_(player.m_269291_().m_269075_((Player)player), (float)damage)) continue;
            WeaponEvolutionSkills.applyTraits(target2, player, first, second, start, ultimate, false);
            if (++hits < maxTargets) continue;
            break;
        }
        if (hits == 0) {
            return new Outcome(false, 0, end);
        }
        SpellVisuals.beam(level, (LivingEntity)player, visual, start, end);
        WeaponEvolutionSkills.show(player, accentVisual, null, end, true);
        WeaponEvolutionSkills.applyBoons(player, first, second, hits, true);
        WeaponEvolutionSkills.sound(player, WeaponEvolutionSkillPool.Aspect.ARC);
        return new Outcome(true, hits, end);
    }

    private static List<LivingEntity> targets(ServerLevel level, ServerPlayer player, Vec3 center, double radius) {
        ArrayList<LivingEntity> result = new ArrayList<LivingEntity>(level.m_6443_(LivingEntity.class, new AABB(center, center).m_82400_(radius), target -> NonManorWeaponSkills.canTarget(player, target)));
        result.sort(Comparator.comparingDouble(target -> target.m_20238_(center)));
        return result;
    }

    private static double damage(ServerPlayer player, double flat, double multiplier) {
        double attack = Math.max(0.0, player.m_21133_(Attributes.f_22281_));
        return Math.min(128.0, Math.max(0.0, flat + attack * multiplier));
    }

    private static void applyTraits(LivingEntity target, ServerPlayer player, WeaponEvolutionSkillPool.Aspect first, WeaponEvolutionSkillPool.Aspect second, Vec3 origin, boolean ultimate, boolean pulling) {
        WeaponEvolutionSkills.applyTrait(target, player, first, origin, ultimate, pulling);
        if (second != null && second != first) {
            WeaponEvolutionSkills.applyTrait(target, player, second, origin, ultimate, pulling);
        }
    }

    private static void applyTrait(LivingEntity target, ServerPlayer player, WeaponEvolutionSkillPool.Aspect aspect, Vec3 origin, boolean ultimate, boolean pulling) {
        int extra = ultimate ? 40 : 0;
        switch (aspect) {
            case SUGAR: {
                WeaponEvolutionSkills.effect(target, MobEffects.f_19597_, 80 + extra, ultimate ? 2 : 1, player);
                break;
            }
            case MEAT: {
                WeaponEvolutionSkills.effect(target, MobEffects.f_19613_, 80 + extra, ultimate ? 1 : 0, player);
                break;
            }
            case TEA: {
                WeaponEvolutionSkills.effect(target, MobEffects.f_19597_, 70 + extra, ultimate ? 2 : 1, player);
                break;
            }
            case WINE: {
                WeaponEvolutionSkills.effect(target, MobEffects.f_19604_, 100 + extra, 0, player);
                WeaponEvolutionSkills.effect(target, MobEffects.f_19613_, 60 + extra, 0, player);
                break;
            }
            case CHEESE: {
                WeaponEvolutionSkills.effect(target, MobEffects.f_19613_, 100 + extra, ultimate ? 1 : 0, player);
                break;
            }
            case SHEPHERD: {
                WeaponEvolutionSkills.effect(target, MobEffects.f_19619_, 100 + extra, 0, player);
                if (pulling) break;
                NonManorWeaponSkills.knockAway(target, origin, ultimate ? 1.05 : 0.7);
                break;
            }
            case ARC: {
                WeaponEvolutionSkills.effect(target, MobEffects.f_19619_, 80 + extra, 0, player);
                WeaponEvolutionSkills.effect(target, MobEffects.f_19597_, 50 + extra, 1, player);
                break;
            }
            case COMPOST: {
                WeaponEvolutionSkills.effect(target, MobEffects.f_19614_, 100 + extra, ultimate ? 2 : 1, player);
                break;
            }
            case APPLE: {
                WeaponEvolutionSkills.effect(target, MobEffects.f_19613_, 60 + extra, 0, player);
                break;
            }
            case TIDE: {
                if (pulling) {
                    WeaponEvolutionSkills.pullToward(target, origin, ultimate ? 1.0 : 0.65);
                    break;
                }
                NonManorWeaponSkills.knockAway(target, origin, ultimate ? 1.1 : 0.75);
            }
        }
    }

    private static void applyBoons(ServerPlayer player, WeaponEvolutionSkillPool.Aspect first, WeaponEvolutionSkillPool.Aspect second, int hits, boolean ultimate) {
        WeaponEvolutionSkills.applyBoon(player, first, hits, ultimate);
        if (second != null) {
            WeaponEvolutionSkills.applyBoon(player, second, hits, ultimate || second == first);
        }
    }

    private static void applyBoon(ServerPlayer player, WeaponEvolutionSkillPool.Aspect aspect, int hits, boolean ultimate) {
        int duration = ultimate ? 160 : 100;
        switch (aspect) {
            case SUGAR: {
                WeaponEvolutionSkills.effect((LivingEntity)player, MobEffects.f_19617_, duration, ultimate ? 1 : 0, player);
                break;
            }
            case MEAT: {
                player.m_5634_((float)Math.min(ultimate ? 10.0 : 6.0, Math.max(ultimate ? 2.0 : 0.0, (double)hits * (ultimate ? 1.5 : 1.0))));
                break;
            }
            case TEA: {
                WeaponEvolutionSkills.effect((LivingEntity)player, MobEffects.f_19605_, duration, ultimate ? 1 : 0, player);
                break;
            }
            case WINE: {
                WeaponEvolutionSkills.effect((LivingEntity)player, MobEffects.f_19600_, duration, ultimate ? 1 : 0, player);
                break;
            }
            case CHEESE: {
                WeaponEvolutionSkills.effect((LivingEntity)player, MobEffects.f_19596_, duration, ultimate ? 2 : 1, player);
                break;
            }
            case SHEPHERD: {
                WeaponEvolutionSkills.effect((LivingEntity)player, MobEffects.f_19596_, duration, ultimate ? 2 : 1, player);
                if (!ultimate) break;
                WeaponEvolutionSkills.effect((LivingEntity)player, MobEffects.f_19606_, 100, 0, player);
                break;
            }
            case ARC: {
                WeaponEvolutionSkills.effect((LivingEntity)player, MobEffects.f_19598_, duration, ultimate ? 2 : 1, player);
                break;
            }
            case COMPOST: {
                player.m_21195_(MobEffects.f_19614_);
                if (!ultimate) break;
                WeaponEvolutionSkills.effect((LivingEntity)player, MobEffects.f_19606_, 100, 0, player);
                break;
            }
            case APPLE: {
                player.m_21195_(MobEffects.f_19614_);
                player.m_21195_(MobEffects.f_19604_);
                player.m_21195_(MobEffects.f_19612_);
                player.m_5634_(ultimate ? 8.0f : 5.0f);
                WeaponEvolutionSkills.effect((LivingEntity)player, MobEffects.f_19605_, duration, ultimate ? 1 : 0, player);
                break;
            }
            case TIDE: {
                WeaponEvolutionSkills.effect((LivingEntity)player, MobEffects.f_19606_, duration, ultimate ? 1 : 0, player);
                WeaponEvolutionSkills.effect((LivingEntity)player, MobEffects.f_19608_, duration * 2, 0, player);
            }
        }
    }

    private static void show(ServerPlayer player, String visual, String accent, Vec3 point, boolean ultimate) {
        if (visual != null) {
            SpellVisuals.show(player.m_284548_(), (LivingEntity)player, visual, ultimate ? SpellVisualRules.Phase.PULSE : SpellVisualRules.Phase.RELEASE, point);
        }
        if (accent != null && !accent.equals(visual)) {
            SpellVisuals.show(player.m_284548_(), (LivingEntity)player, accent, SpellVisualRules.Phase.PULSE, point);
        }
    }

    private static void sound(ServerPlayer player, WeaponEvolutionSkillPool.Aspect aspect) {
        SoundEvent sound = switch (aspect) {
            default -> throw new IncompatibleClassChangeError();
            case WeaponEvolutionSkillPool.Aspect.SUGAR -> SoundEvents.f_11983_;
            case WeaponEvolutionSkillPool.Aspect.MEAT -> SoundEvents.f_11913_;
            case WeaponEvolutionSkillPool.Aspect.TEA, WeaponEvolutionSkillPool.Aspect.APPLE -> SoundEvents.f_144243_;
            case WeaponEvolutionSkillPool.Aspect.WINE -> SoundEvents.f_11911_;
            case WeaponEvolutionSkillPool.Aspect.CHEESE -> SoundEvents.f_12321_;
            case WeaponEvolutionSkillPool.Aspect.SHEPHERD -> SoundEvents.f_12619_;
            case WeaponEvolutionSkillPool.Aspect.ARC -> SoundEvents.f_12521_;
            case WeaponEvolutionSkillPool.Aspect.COMPOST -> SoundEvents.f_12388_;
            case WeaponEvolutionSkillPool.Aspect.TIDE -> SoundEvents.f_12517_;
        };
        player.m_284548_().m_5594_(null, player.m_20183_(), sound, SoundSource.PLAYERS, 0.9f, 1.0f);
    }

    private static void effect(LivingEntity target, MobEffect effect, int ticks, int amplifier, ServerPlayer source) {
        target.m_147207_(new MobEffectInstance(effect, ticks, amplifier), (Entity)source);
    }

    private static void pullToward(LivingEntity target, Vec3 origin, double strength) {
        Vec3 direction = origin.m_82546_(target.m_20182_()).m_82542_(1.0, 0.0, 1.0).m_82541_();
        double resistance = Math.max(0.0, Math.min(1.0, target.m_21133_(Attributes.f_22278_)));
        double magnitude = strength * (1.0 - resistance);
        if (direction.m_82556_() < 1.0E-6 || magnitude <= 0.0) {
            return;
        }
        target.m_5997_(direction.f_82479_ * magnitude, 0.08 * (1.0 - resistance), direction.f_82481_ * magnitude);
        target.f_19864_ = true;
    }

    private static boolean within(double distanceSquared, double range) {
        return Double.isFinite(distanceSquared) && distanceSquared >= 0.0 && Double.isFinite(range) && range >= 0.0 && distanceSquared <= range * range;
    }

    private static double projection(Vec3 start, Vec3 end, Vec3 point) {
        Vec3 segment = end.m_82546_(start);
        double lengthSquared = segment.m_82556_();
        return lengthSquared < 1.0E-8 ? 0.0 : Math.max(0.0, Math.min(1.0, point.m_82546_(start).m_82526_(segment) / lengthSquared));
    }

    private static double distanceToSegmentSqr(Vec3 point, Vec3 start, Vec3 end) {
        return point.m_82557_(start.m_165921_(end, WeaponEvolutionSkills.projection(start, end, point)));
    }

    public static void cast(ServerPlayer serverPlayer, ItemStack itemStack) {
        int n;
        Outcome outcome;
        WeaponEvolutionSkillPool.Fusion fusion;
        State state = WeaponEvolutionSkills.state(itemStack);
        if (!state.evolved() || FoodWeaponLifecycle.isBroken(itemStack) || !serverPlayer.m_6084_() || serverPlayer.m_213877_() || serverPlayer.m_5833_() || serverPlayer.m_5803_() || serverPlayer.m_6117_()) {
            return;
        }
        ResourceLocation resourceLocation = ForgeRegistries.ITEMS.getKey((Object)itemStack.m_41720_());
        if (resourceLocation == null || itemStack != serverPlayer.m_21205_()) {
            return;
        }
        boolean bl = state.stage() == 2 && serverPlayer.m_6144_();
        WeaponEvolutionSkillPool.Fusion fusion2 = fusion = state.stage() == 2 ? WeaponEvolutionSkillPool.fusion(state.first(), state.second()) : null;
        if (state.stage() == 2 && fusion == null) {
            return;
        }
        long l = serverPlayer.m_284548_().m_7654_().m_129783_().m_46467_();
        CompoundTag compoundTag = serverPlayer.getPersistentData().m_128469_(COOLDOWN_TAG);
        if (compoundTag.m_128441_("request") && l >= compoundTag.m_128454_("request") && l - compoundTag.m_128454_("request") < 4L) {
            return;
        }
        compoundTag.m_128356_("request", l);
        String string = resourceLocation + "/" + state.pathId() + (bl ? "/2" : "/1");
        int n2 = state.stage() == 1 ? state.first().cooldownTicks : (bl ? fusion.secondaryCooldownTicks() : fusion.primaryCooldownTicks());
        int n3 = FoodWeaponEvolution.skillCooldown(itemStack, n2);
        long l2 = compoundTag.m_128454_(string);
        if (l2 > l) {
            serverPlayer.getPersistentData().m_128365_(COOLDOWN_TAG, (Tag)compoundTag);
            serverPlayer.m_5661_((Component)Component.m_237110_((String)"message.ftboss.evolution.cooldown", (Object[])new Object[]{WeaponEvolutionSkills.skillName(state, fusion, bl), (l2 - l + 19L) / 20L}), true);
            return;
        }
        double d = FoodWeaponSkillRarity.activeView(itemStack).multiplier();
        Outcome outcome2 = state.stage() == 1 ? WeaponEvolutionSkills.executeBase(serverPlayer, state.first(), d) : (outcome = bl ? WeaponEvolutionSkills.executeSecondary(serverPlayer, fusion, d) : WeaponEvolutionSkills.executePrimary(serverPlayer, fusion, d));
        if (!outcome.success()) {
            serverPlayer.getPersistentData().m_128365_(COOLDOWN_TAG, (Tag)compoundTag);
            serverPlayer.m_5661_((Component)Component.m_237115_((String)"message.ftboss.evolution.no_target"), true);
            return;
        }
        compoundTag.m_128356_(string, l + (long)n3);
        serverPlayer.getPersistentData().m_128365_(COOLDOWN_TAG, (Tag)compoundTag);
        GourmetEvolutionWeapon.animateActive(serverPlayer, itemStack);
        int n4 = state.stage() == 1 ? 3 : (n = bl ? 5 : 4);
        if (itemStack.m_41763_()) {
            itemStack.m_41622_(n, (LivingEntity)serverPlayer, holder -> holder.m_21190_(InteractionHand.MAIN_HAND));
        }
        serverPlayer.m_21011_(InteractionHand.MAIN_HAND, true);
        serverPlayer.m_5661_((Component)Component.m_237110_((String)"message.ftboss.evolution.cast", (Object[])new Object[]{WeaponEvolutionSkills.skillName(state, fusion, bl), n3 / 20}), true);
    }

    private static State state(ItemStack itemStack) {
        int n = FoodWeaponProgression.activeStage(itemStack);
        if (FoodWeaponEvolution.isGourmetEvolutionWeapon(itemStack) && n > 0) {
            n = 1;
        }
        WeaponEvolutionSkillPool.Aspect aspect = WeaponEvolutionSkillPool.Aspect.fromId(FoodWeaponEvolution.firstProfileId(itemStack));
        WeaponEvolutionSkillPool.Aspect aspect2 = WeaponEvolutionSkillPool.Aspect.fromId(FoodWeaponEvolution.secondProfileId(itemStack));
        if (n >= 2 && aspect2 == null) {
            aspect2 = aspect;
        }
        return new State(aspect == null ? 0 : n, aspect, aspect2);
    }

    private static Outcome executeBase(ServerPlayer serverPlayer, WeaponEvolutionSkillPool.Aspect aspect, double d) {
        Shape shape = switch (aspect.style) {
            default -> throw new IncompatibleClassChangeError();
            case WeaponEvolutionSkillPool.BaseStyle.SELF -> Shape.SELF;
            case WeaponEvolutionSkillPool.BaseStyle.CONE, WeaponEvolutionSkillPool.BaseStyle.DASH -> Shape.CONE;
            case WeaponEvolutionSkillPool.BaseStyle.NOVA -> Shape.NOVA;
            case WeaponEvolutionSkillPool.BaseStyle.BURST -> Shape.BURST;
            case WeaponEvolutionSkillPool.BaseStyle.CHAIN -> Shape.CHAIN;
        };
        return WeaponEvolutionSkills.execute(serverPlayer, aspect, null, shape, aspect.flatDamage * d, aspect.attackMultiplier * d, aspect.range, aspect.radius, 24, false, false, aspect.visual, null);
    }

    private static Outcome executePrimary(ServerPlayer serverPlayer, WeaponEvolutionSkillPool.Fusion fusion, double d) {
        WeaponEvolutionSkillPool.Aspect aspect = fusion.pair().first();
        WeaponEvolutionSkillPool.Aspect aspect2 = fusion.pair().second();
        double d2 = (aspect.flatDamage + aspect2.flatDamage) * 0.65 + 2.0;
        double d3 = (aspect.attackMultiplier + aspect2.attackMultiplier) * 0.58 + 0.35;
        if (aspect == aspect2) {
            d2 *= 1.15;
            d3 *= 1.15;
        }
        double d4 = Math.max(7.0, Math.max(aspect.range, aspect2.range));
        double d5 = Math.max(3.5, Math.max(aspect.radius, aspect2.radius) + 0.4);
        return WeaponEvolutionSkills.execute(serverPlayer, aspect, aspect2, WeaponEvolutionSkills.primaryShape(aspect, aspect2), d2 * d, d3 * d, d4, d5, 28, false, false, fusion.primaryVisual(), aspect2.visual);
    }

    private static Outcome executeSecondary(ServerPlayer serverPlayer, WeaponEvolutionSkillPool.Fusion fusion, double d) {
        WeaponEvolutionSkillPool.Aspect aspect = fusion.pair().first();
        WeaponEvolutionSkillPool.Aspect aspect2 = fusion.pair().second();
        double d2 = (aspect.flatDamage + aspect2.flatDamage) * 0.7 + 5.0;
        double d3 = (aspect.attackMultiplier + aspect2.attackMultiplier) * 0.65 + 0.65;
        if (aspect == aspect2) {
            d2 *= 1.15;
            d3 *= 1.15;
        }
        Shape shape = switch (fusion.secondaryStyle()) {
            default -> throw new IncompatibleClassChangeError();
            case WeaponEvolutionSkillPool.FusionStyle.BURST -> Shape.BURST;
            case WeaponEvolutionSkillPool.FusionStyle.NOVA, WeaponEvolutionSkillPool.FusionStyle.WARD -> Shape.NOVA;
            case WeaponEvolutionSkillPool.FusionStyle.CHAIN -> Shape.CHAIN;
            case WeaponEvolutionSkillPool.FusionStyle.DASH -> Shape.CONE;
            case WeaponEvolutionSkillPool.FusionStyle.VORTEX -> Shape.VORTEX;
            case WeaponEvolutionSkillPool.FusionStyle.BEAM -> Shape.BEAM;
        };
        boolean bl = fusion.secondaryStyle() == WeaponEvolutionSkillPool.FusionStyle.WARD;
        return WeaponEvolutionSkills.execute(serverPlayer, aspect, aspect2, shape, d2 * d, d3 * d, 12.0, 4.75, 32, true, bl, fusion.secondaryVisual(), aspect.ultimateVisual);
    }

    private record State(int stage, WeaponEvolutionSkillPool.Aspect first, WeaponEvolutionSkillPool.Aspect second) {
        private boolean evolved() {
            return this.stage > 0 && this.first != null;
        }

        private String pathId() {
            return this.stage < 2 || this.second == null ? this.first.id : new WeaponEvolutionSkillPool.Pair(this.first, this.second).id();
        }
    }

    private record Outcome(boolean success, int hits, Vec3 point) {
    }

    private static enum Shape {
        SELF,
        CONE,
        NOVA,
        BURST,
        CHAIN,
        BEAM,
        VORTEX;

    }
}

