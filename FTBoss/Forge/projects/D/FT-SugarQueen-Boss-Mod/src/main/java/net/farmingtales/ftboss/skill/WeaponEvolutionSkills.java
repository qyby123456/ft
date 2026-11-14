package net.farmingtales.ftboss.skill;

import net.farmingtales.ftboss.item.FoodWeaponEvolution;
import net.farmingtales.ftboss.util.WeaponEvolutionSkillPool;
import net.farmingtales.ftboss.util.WeaponEvolutionSkillPool.Aspect;
import net.farmingtales.ftboss.util.WeaponEvolutionSkillPool.Fusion;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.registries.ForgeRegistries;

/** Runtime executor for the ten first-stage skills and all 55 two-statue fusions. */
public final class WeaponEvolutionSkills {
    public static final String COOLDOWN_TAG = "FTBossEvolutionSkillCooldowns";
    private static final int REQUEST_INTERVAL = 4;
    private record Outcome(boolean success, int hits, Vec3 point) {}
    private record State(int stage, Aspect first, Aspect second) {
        private boolean evolved() { return stage > 0 && first != null; }
        private String pathId() {
            return stage < 2 || second == null ? first.id
                    : new WeaponEvolutionSkillPool.Pair(first, second).id();
        }
    }

    private WeaponEvolutionSkills() {}

    public static boolean supports(ItemStack stack) {
        return state(stack).evolved();
    }

    public static void cast(ServerPlayer player, ItemStack stack) {
        State state = state(stack);
        if (!state.evolved() || net.farmingtales.ftboss.item.FoodWeaponLifecycle.isBroken(stack)
                || !player.isAlive() || player.isRemoved() || player.isSpectator()
                || player.isSleeping() || player.isUsingItem()) return;
        ResourceLocation heldId = ForgeRegistries.ITEMS.getKey(stack.getItem());
        if (heldId == null || stack != player.getMainHandItem()) return;

        boolean secondary = state.stage() == 2 && player.isShiftKeyDown();
        Fusion fusion = state.stage() == 2
                ? WeaponEvolutionSkillPool.fusion(state.first(), state.second()) : null;
        if (state.stage() == 2 && fusion == null) return;

        long now = player.serverLevel().getServer().overworld().getGameTime();
        CompoundTag cooldowns = player.getPersistentData().getCompound(COOLDOWN_TAG);
        if (cooldowns.contains("request") && now >= cooldowns.getLong("request")
                && now - cooldowns.getLong("request") < REQUEST_INTERVAL) return;
        cooldowns.putLong("request", now);
        String key = heldId + "/" + state.pathId() + (secondary ? "/2" : "/1");
        int baseCooldown = state.stage() == 1 ? state.first().cooldownTicks
                : secondary ? fusion.secondaryCooldownTicks() : fusion.primaryCooldownTicks();
        int cooldown = FoodWeaponEvolution.skillCooldown(stack, baseCooldown);
        long deadline = cooldowns.getLong(key);
        if (deadline > now) {
            player.getPersistentData().put(COOLDOWN_TAG, cooldowns);
            player.displayClientMessage(Component.translatable("message.ftboss.evolution.cooldown",
                    skillName(state, fusion, secondary), (deadline - now + 19) / 20), true);
            return;
        }

        double skillPower = net.farmingtales.ftboss.item.FoodWeaponSkillRarity.activeView(stack).multiplier();
        Outcome outcome = state.stage() == 1 ? executeBase(player, state.first(), skillPower)
                : secondary ? executeSecondary(player, fusion, skillPower) : executePrimary(player, fusion, skillPower);
        if (!outcome.success()) {
            player.getPersistentData().put(COOLDOWN_TAG, cooldowns);
            player.displayClientMessage(WeaponTypeSkills.failureMessage(stack), true);
            return;
        }

        cooldowns.putLong(key, now + cooldown);
        player.getPersistentData().put(COOLDOWN_TAG, cooldowns);
        net.farmingtales.ftboss.item.weapons.GourmetEvolutionWeapon.animateActive(player, stack);
        int durability = state.stage() == 1 ? 3 : secondary ? 5 : 4;
        if (stack.isDamageableItem()) {
            stack.hurtAndBreak(durability, player,
                    holder -> holder.broadcastBreakEvent(InteractionHand.MAIN_HAND));
        }
        player.swing(InteractionHand.MAIN_HAND, true);
        player.displayClientMessage(Component.translatable("message.ftboss.evolution.cast",
                skillName(state, fusion, secondary), cooldown / 20), true);
    }

    private static State state(ItemStack stack) {
        int stage = net.farmingtales.ftboss.item.FoodWeaponProgression.activeStage(stack);
        // The eight-stage Gourmet weapons keep every random passive, while J follows the most
        // recently evolved statue as one base active skill rather than entering a fake fusion.
        if (FoodWeaponEvolution.isGourmetEvolutionWeapon(stack) && stage > 0) stage = 1;
        Aspect first = Aspect.fromId(FoodWeaponEvolution.firstProfileId(stack));
        Aspect second = Aspect.fromId(FoodWeaponEvolution.secondProfileId(stack));
        // Compatibility for level-2 weapons created by the first progression build, before the
        // second statue id was persisted: treat those as a pure specialization.
        if (stage >= 2 && second == null) second = first;
        return new State(first == null ? 0 : stage, first, second);
    }

    public static void copyCooldownData(PlayerEvent.Clone event) {
        CompoundTag previous = event.getOriginal().getPersistentData();
        if (previous.contains(COOLDOWN_TAG)) {
            event.getEntity().getPersistentData().put(COOLDOWN_TAG,
                    previous.getCompound(COOLDOWN_TAG).copy());
        }
    }

    private static Component skillName(State state, Fusion fusion, boolean secondary) {
        if (state.stage() == 1) return Component.translatable(state.first().translationKey());
        return Component.translatable(secondary
                ? fusion.secondaryTranslationKey() : fusion.primaryTranslationKey());
    }

    private static Outcome executeBase(ServerPlayer player, Aspect aspect, double power) {
        return executeTyped(player, player.getMainHandItem(), aspect, null, false, power);
    }

    private static Outcome executePrimary(ServerPlayer player, Fusion fusion, double power) {
        return executeTyped(player, player.getMainHandItem(), fusion.pair().first(), fusion.pair().second(), false, power);
    }

    private static Outcome executeSecondary(ServerPlayer player, Fusion fusion, double power) {
        return executeTyped(player, player.getMainHandItem(), fusion.pair().first(), fusion.pair().second(), true, power);
    }

    private static Outcome executeTyped(ServerPlayer player, ItemStack stack, Aspect first, Aspect second,
                                        boolean secondary, double power) {
        double flat = first.flatDamage;
        double multiplier = first.attackMultiplier;
        if (second != null) {
            flat = (first.flatDamage + second.flatDamage) * (secondary ? 0.7 : 0.65) + (secondary ? 5.0 : 2.0);
            multiplier = (first.attackMultiplier + second.attackMultiplier) * (secondary ? 0.65 : 0.58) + (secondary ? 0.65 : 0.35);
            if (first == second) {
                flat *= 1.15;
                multiplier *= 1.15;
            }
        }
        if (flat == 0 && multiplier == 0) multiplier = 1.15;
        return new Outcome(WeaponTypeSkills.execute(player, stack, first, second, secondary, power,
                flat * power, multiplier * power), 0, player.position());
    }

    static void applyTraits(LivingEntity target, ServerPlayer player, Aspect first,
                                    Aspect second, Vec3 origin, boolean ultimate, boolean pulling) {
        applyTrait(target, player, first, origin, ultimate, pulling);
        if (second != null && second != first) applyTrait(target, player, second, origin, ultimate, pulling);
    }

    private static void applyTrait(LivingEntity target, ServerPlayer player, Aspect aspect,
                                   Vec3 origin, boolean ultimate, boolean pulling) {
        int extra = ultimate ? 40 : 0;
        switch (aspect) {
            case SUGAR -> effect(target, MobEffects.MOVEMENT_SLOWDOWN, 80 + extra, ultimate ? 2 : 1, player);
            case MEAT -> effect(target, MobEffects.WEAKNESS, 80 + extra, ultimate ? 1 : 0, player);
            case TEA -> effect(target, MobEffects.MOVEMENT_SLOWDOWN, 70 + extra, ultimate ? 2 : 1, player);
            case WINE -> {
                effect(target, MobEffects.CONFUSION, 100 + extra, 0, player);
                effect(target, MobEffects.WEAKNESS, 60 + extra, 0, player);
            }
            case CHEESE -> effect(target, MobEffects.WEAKNESS, 100 + extra, ultimate ? 1 : 0, player);
            case SHEPHERD -> {
                effect(target, MobEffects.GLOWING, 100 + extra, 0, player);
                if (!pulling) NonManorWeaponSkills.knockAway(target, origin, ultimate ? 1.05 : 0.7);
            }
            case ARC -> {
                effect(target, MobEffects.GLOWING, 80 + extra, 0, player);
                effect(target, MobEffects.MOVEMENT_SLOWDOWN, 50 + extra, 1, player);
            }
            case COMPOST -> effect(target, MobEffects.POISON, 100 + extra, ultimate ? 2 : 1, player);
            case APPLE -> effect(target, MobEffects.WEAKNESS, 60 + extra, 0, player);
            case TIDE -> {
                if (pulling) pullToward(target, origin, ultimate ? 1.0 : 0.65);
                else NonManorWeaponSkills.knockAway(target, origin, ultimate ? 1.1 : 0.75);
            }
        }
    }

    static void applyBoons(ServerPlayer player, Aspect first, Aspect second,
                                   int hits, boolean ultimate) {
        applyBoon(player, first, hits, ultimate);
        if (second != null) applyBoon(player, second, hits, ultimate || second == first);
    }

    private static void applyBoon(ServerPlayer player, Aspect aspect, int hits, boolean ultimate) {
        int duration = ultimate ? 160 : 100;
        switch (aspect) {
            case SUGAR -> effect(player, MobEffects.ABSORPTION, duration, ultimate ? 1 : 0, player);
            case MEAT -> player.heal((float) Math.min(ultimate ? 10 : 6,
                    Math.max(ultimate ? 2 : 0, hits * (ultimate ? 1.5 : 1.0))));
            case TEA -> effect(player, MobEffects.REGENERATION, duration, ultimate ? 1 : 0, player);
            case WINE -> effect(player, MobEffects.DAMAGE_BOOST, duration, ultimate ? 1 : 0, player);
            case CHEESE -> effect(player, MobEffects.MOVEMENT_SPEED, duration, ultimate ? 2 : 1, player);
            case SHEPHERD -> {
                effect(player, MobEffects.MOVEMENT_SPEED, duration, ultimate ? 2 : 1, player);
                if (ultimate) effect(player, MobEffects.DAMAGE_RESISTANCE, 100, 0, player);
            }
            case ARC -> effect(player, MobEffects.DIG_SPEED, duration, ultimate ? 2 : 1, player);
            case COMPOST -> {
                player.removeEffect(MobEffects.POISON);
                if (ultimate) effect(player, MobEffects.DAMAGE_RESISTANCE, 100, 0, player);
            }
            case APPLE -> {
                player.removeEffect(MobEffects.POISON);
                player.removeEffect(MobEffects.CONFUSION);
                player.removeEffect(MobEffects.HUNGER);
                player.heal(ultimate ? 8 : 5);
                effect(player, MobEffects.REGENERATION, duration, ultimate ? 1 : 0, player);
            }
            case TIDE -> {
                effect(player, MobEffects.DAMAGE_RESISTANCE, duration, ultimate ? 1 : 0, player);
                effect(player, MobEffects.WATER_BREATHING, duration * 2, 0, player);
            }
        }
    }

    private static void effect(LivingEntity target, MobEffect effect, int ticks,
                               int amplifier, ServerPlayer source) {
        target.addEffect(new MobEffectInstance(effect, ticks, amplifier), source);
    }

    private static void pullToward(LivingEntity target, Vec3 origin, double strength) {
        Vec3 direction = origin.subtract(target.position()).multiply(1, 0, 1).normalize();
        double resistance = Math.max(0, Math.min(1,
                target.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE)));
        double magnitude = strength * (1 - resistance);
        if (direction.lengthSqr() < 1.0E-6 || magnitude <= 0) return;
        target.push(direction.x * magnitude, 0.08 * (1 - resistance), direction.z * magnitude);
        target.hurtMarked = true;
    }

}
