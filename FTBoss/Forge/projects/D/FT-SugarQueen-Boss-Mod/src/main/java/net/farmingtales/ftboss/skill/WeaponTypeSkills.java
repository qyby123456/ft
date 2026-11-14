package net.farmingtales.ftboss.skill;

import java.util.Comparator;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;
import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.entity.BarrelCannonballEntity;
import net.farmingtales.ftboss.item.BarrelCrossbowItem;
import net.farmingtales.ftboss.item.BeerAmmo;
import net.farmingtales.ftboss.item.ArrowPassiveDamage;
import net.farmingtales.ftboss.item.FoodWeaponFamilyBuild;
import net.farmingtales.ftboss.item.FoodWeaponLifecycle;
import net.farmingtales.ftboss.item.FoodWeaponRanged;
import net.farmingtales.ftboss.item.FoodWeaponSkillRarity;
import net.farmingtales.ftboss.item.TideTridentItem;
import net.farmingtales.ftboss.util.NonManorWeaponRules.Skill;
import net.farmingtales.ftboss.util.WeaponEvolutionSkillPool.Aspect;
import net.farmingtales.ftboss.util.WeaponTypeSkillRules;
import net.farmingtales.ftboss.util.WeaponTypeSkillRules.Kind;
import net.farmingtales.ftboss.util.WeaponTypeSkillRules.Profile;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TridentItem;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

@Mod.EventBusSubscriber(modid = FTBossMod.MOD_ID)
public final class WeaponTypeSkills {
    private static final String ARROW_TAG = "FTBossTypeSkillArrow";
    private static final String STAFF_UNTIL = "FTBossStaffFocusUntil";
    private static final UUID FOCUS_POWER = UUID.fromString("701d93f1-dbe2-4735-9477-3f75d5b9402a");
    private static final UUID FOCUS_REGEN = UUID.fromString("dbe7abc3-c454-46db-8bca-3a337a0970ba");
    private static final Map<ServerPlayer, Volley> VOLLEYS = new WeakHashMap<>();

    private record Volley(ItemStack weapon, Aspect first, Aspect second, boolean secondary,
                          double power, int remaining, long nextShot) {}

    private WeaponTypeSkills() {}

    public static boolean executeNative(ServerPlayer player, Skill skill) {
        Aspect aspect = skill == Skill.BARREL ? Aspect.WINE : Aspect.TIDE;
        double power = net.farmingtales.ftboss.item.FoodWeaponEvolution.activeSkillMultiplier(player.getMainHandItem());
        return execute(player, player.getMainHandItem(), aspect, null, false, power,
                skill.flatDamage * power, skill.attackMultiplier * power);
    }

    public static Component failureMessage(ItemStack stack) {
        return switch (kind(stack)) {
            case BOW, CROSSBOW -> Component.literal(stack.getItem() instanceof BarrelCrossbowItem
                    ? "酒桶连射需要背包中有可用酒类弹药。" : "连射需要背包中有可用箭矢。");
            case STAFF -> Component.literal("当前玩家缺少铁魔法的法强或回蓝属性，无法获得奥术专注。");
            case TRIDENT -> Component.literal("前方空间不足，或引雷落点没有可命中的敌人。");
            case MELEE -> Component.translatable("message.ftboss.evolution.no_target");
        };
    }

    private static boolean lightning(ServerPlayer player, Aspect first, Aspect second, boolean secondary, double flat, double multiplier) {
        Vec3 impact = NonManorWeaponSkills.aim(player, secondary ? 18 : 14);
        int hits = 0;
        for (LivingEntity target : player.serverLevel().getEntitiesOfClass(LivingEntity.class,
                new AABB(impact, impact).inflate(secondary ? 3.0 : 2.0), candidate -> NonManorWeaponSkills.canTarget(player, candidate))) {
            if (NonManorWeaponSkills.clearLine(player.serverLevel(), player, player.getEyePosition(), target.getBoundingBox().getCenter())) {
                if (strike(player, target, first, second, secondary, flat, multiplier)) hits++;
            }
        }
        if (hits == 0) return false;
        WeaponEvolutionSkills.applyBoons(player, first, second, hits, secondary);
        LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(player.serverLevel());
        if (bolt != null) {
            bolt.moveTo(impact);
            bolt.setVisualOnly(true);
            player.serverLevel().addFreshEntity(bolt);
        }
        return true;
    }

    private static boolean thrust(ServerPlayer player, Aspect first, Aspect second, boolean secondary, Profile profile,
                                  double flat, double multiplier) {
        Vec3 start = player.position();
        Vec3 direction = player.getLookAngle().normalize();
        Vec3 end = start;
        for (double distance = 0.25; distance <= profile.dashRange(); distance += 0.25) {
            Vec3 next = start.add(direction.scale(distance));
            if (!player.serverLevel().hasChunkAt(BlockPos.containing(next))
                    || !player.serverLevel().getWorldBorder().isWithinBounds(player.getBoundingBox().move(next.subtract(start)))
                    || !player.serverLevel().noCollision(player, player.getBoundingBox().move(next.subtract(start)))) break;
            end = next;
        }
        if (start.distanceToSqr(end) < 0.25) return false;
        Vec3 eye = player.getEyePosition();
        Vec3 tip = end.add(0, player.getEyeHeight(), 0);
        int hits = 0;
        for (LivingEntity target : player.serverLevel().getEntitiesOfClass(LivingEntity.class,
                player.getBoundingBox().expandTowards(end.subtract(start)).inflate(1.0), candidate -> NonManorWeaponSkills.canTarget(player, candidate))) {
            if (target.getBoundingBox().inflate(0.6).clip(eye, tip).isPresent()
                    && NonManorWeaponSkills.clearLine(player.serverLevel(), player, eye, target.getBoundingBox().getCenter())) {
                if (strike(player, target, first, second, secondary, flat, multiplier)) hits++;
            }
        }
        player.connection.teleport(end.x, end.y, end.z, player.getYRot(), player.getXRot());
        player.setDeltaMovement(Vec3.ZERO);
        player.connection.send(new ClientboundSetEntityMotionPacket(player));
        WeaponEvolutionSkills.applyBoons(player, first, second, hits, secondary);
        player.serverLevel().sendParticles(ParticleTypes.SPLASH, end.x, end.y + 0.5, end.z, 20, 0.4, 0.5, 0.4, 0.1);
        player.serverLevel().playSound(null, player.blockPosition(), SoundEvents.TRIDENT_RIPTIDE_1, SoundSource.PLAYERS, 1.0F, 1.0F);
        return true;
    }

    private static boolean sweep(ServerPlayer player, ItemStack stack, Aspect first, Aspect second,
                                 boolean secondary, double flat, double multiplier) {
        boolean axe = stack.getItem() instanceof AxeItem;
        double range = axe ? 3.0 : secondary ? 4.0 : 3.5;
        int hits = 0;
        var targets = player.serverLevel().getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(range),
                candidate -> NonManorWeaponSkills.canTarget(player, candidate));
        targets.sort(Comparator.comparingDouble(player::distanceToSqr));
        for (LivingEntity target : targets) {
            Vec3 offset = target.getBoundingBox().getCenter().subtract(player.getEyePosition());
            if (offset.lengthSqr() > range * range || offset.normalize().dot(player.getLookAngle()) < (axe ? 0.5 : 0.0)
                    || !player.hasLineOfSight(target)) continue;
            if (strike(player, target, first, second, secondary, flat, multiplier * (axe ? 1.2 : 1.0))) {
                if (axe) NonManorWeaponSkills.knockAway(target, player.position(), 0.9);
                if (++hits >= 16) break;
            }
        }
        Vec3 effect = player.getEyePosition().add(player.getLookAngle().scale(1.5));
        player.serverLevel().sendParticles(ParticleTypes.SWEEP_ATTACK, effect.x, effect.y, effect.z, 1, 0, 0, 0, 0);
        player.serverLevel().playSound(null, player.blockPosition(), axe ? SoundEvents.PLAYER_ATTACK_STRONG : SoundEvents.PLAYER_ATTACK_SWEEP,
                SoundSource.PLAYERS, 1.0F, 1.0F);
        if (hits > 0) WeaponEvolutionSkills.applyBoons(player, first, second, hits, secondary);
        return hits > 0;
    }

    private static boolean strike(ServerPlayer player, LivingEntity target, Aspect first, Aspect second,
                                  boolean secondary, double flat, double multiplier) {
        float damage = (float) Math.min(128, Math.max(0, flat + player.getAttributeValue(Attributes.ATTACK_DAMAGE) * multiplier));
        if (!target.hurt(player.damageSources().playerAttack(player), damage)) return false;
        WeaponEvolutionSkills.applyTraits(target, player, first, second, player.position(), secondary, false);
        return true;
    }

    private static boolean volley(ServerPlayer player, ItemStack stack, Aspect first, Aspect second,
                                  boolean secondary, double power, Profile profile) {
        if (!shoot(player, stack, first, second, secondary, power, true)) return false;
        VOLLEYS.put(player, new Volley(stack, first, second, secondary, power,
                profile.shots() - 1, player.serverLevel().getGameTime() + profile.interval()));
        return true;
    }

    private static boolean shoot(ServerPlayer player, ItemStack weapon, Aspect first, Aspect second,
                                 boolean secondary, double power, boolean grantBoon) {
        if (weapon.getItem() instanceof BarrelCrossbowItem) return shootBarrel(player, weapon, first, second, secondary, power, grantBoon);
        ItemStack ammo = player.getProjectile(weapon);
        if (!(ammo.getItem() instanceof ArrowItem)) {
            ammo = ItemStack.EMPTY;
            for (ItemStack candidate : player.getInventory().items) {
                if (candidate.getItem() instanceof ArrowItem) {
                    ammo = candidate;
                    break;
                }
            }
        }
        if (ammo.isEmpty() && player.getAbilities().instabuild) ammo = new ItemStack(Items.ARROW);
        if (!(ammo.getItem() instanceof ArrowItem arrowItem)) return false;
        boolean crossbow = kind(weapon) == Kind.CROSSBOW;
        boolean infinite = player.getAbilities().instabuild || !crossbow && arrowItem.isInfinite(ammo, weapon, player);
        AbstractArrow arrow = arrowItem.createArrow(player.serverLevel(), ammo, player);
        if (weapon.getItem() instanceof BowItem bow) arrow = bow.customArrow(arrow);
        arrow = ArrowPassiveDamage.apply(arrow, weapon);
        int enchantment = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.POWER_ARROWS, weapon);
        if (!crossbow && enchantment > 0) arrow.setBaseDamage(arrow.getBaseDamage() + enchantment * 0.5 + 0.5);
        arrow.setBaseDamage(arrow.getBaseDamage() * (secondary ? 0.8 : 0.65) * power);
        arrow.setKnockback(EnchantmentHelper.getItemEnchantmentLevel(Enchantments.PUNCH_ARROWS, weapon));
        if (EnchantmentHelper.getItemEnchantmentLevel(Enchantments.FLAMING_ARROWS, weapon) > 0) arrow.setSecondsOnFire(100);
        if (crossbow) {
            arrow.setShotFromCrossbow(true);
            arrow.setPierceLevel((byte) Math.min(127, EnchantmentHelper.getItemEnchantmentLevel(Enchantments.PIERCING, weapon)
                    + (secondary ? 2 : 0)));
        }
        arrow.pickup = AbstractArrow.Pickup.DISALLOWED;
        arrow.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, crossbow ? 3.15F : 3.0F, 0.5F);
        tagProjectile(arrow, first, second, secondary, grantBoon);
        FoodWeaponRanged.launch(arrow, FoodWeaponRanged.capture(player, weapon));
        if (!player.serverLevel().addFreshEntity(arrow)) return false;
        if (!infinite) {
            ammo.shrink(1);
            if (ammo.isEmpty()) player.getInventory().removeItem(ammo);
        }
        player.serverLevel().playSound(null, player.blockPosition(),
                crossbow ? SoundEvents.CROSSBOW_SHOOT : SoundEvents.ARROW_SHOOT, SoundSource.PLAYERS, 0.8F, 1.1F);
        return true;
    }

    private static void tagProjectile(Projectile projectile, Aspect first, Aspect second, boolean secondary, boolean grantBoon) {
        CompoundTag traits = new CompoundTag();
        traits.putString("First", first.id);
        if (second != null) traits.putString("Second", second.id);
        traits.putBoolean("Secondary", secondary);
        traits.putBoolean("GrantBoon", grantBoon);
        projectile.getPersistentData().put(ARROW_TAG, traits);
    }

    private static boolean shootBarrel(ServerPlayer player, ItemStack weapon, Aspect first, Aspect second, boolean secondary, double power, boolean grantBoon) {
        ItemStack ammo = BeerAmmo.findBeer(player);
        if (ammo.isEmpty() && !player.getAbilities().instabuild) return false;
        Vec3 direction = player.getLookAngle();
        BarrelCannonballEntity shell = new BarrelCannonballEntity(player.serverLevel(), player);
        shell.setPos(player.getX() + direction.x * 0.7, player.getEyeY() - 0.2, player.getZ() + direction.z * 0.7);
        shell.setExplosionPower((float) (BarrelCannonballEntity.EXPLOSION_POWER * (secondary ? 0.8 : 0.65) * power));
        shell.shoot(direction.x, direction.y + 0.03, direction.z, 1.65F, 0.35F);
        tagProjectile(shell, first, second, secondary, grantBoon);
        FoodWeaponRanged.launch(shell, FoodWeaponRanged.capture(player, weapon));
        if (!player.serverLevel().addFreshEntity(shell)) return false;
        if (!player.getAbilities().instabuild) ammo.shrink(1);
        player.serverLevel().playSound(null, player.blockPosition(), SoundEvents.CROSSBOW_SHOOT, SoundSource.PLAYERS, 0.8F, 0.9F);
        return true;
    }

    @SubscribeEvent
    public static void tick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.player instanceof ServerPlayer player)) return;
        Volley volley = VOLLEYS.get(player);
        if (volley != null) {
            if (!player.isAlive() || player.isRemoved() || player.isSpectator() || player.isSleeping()
                    || player.getMainHandItem() != volley.weapon() || FoodWeaponLifecycle.isBroken(volley.weapon())) {
                VOLLEYS.remove(player);
            } else if (player.serverLevel().getGameTime() >= volley.nextShot()) {
                if (!shoot(player, volley.weapon(), volley.first(), volley.second(), volley.secondary(), volley.power(), false)
                        || volley.remaining() <= 1) VOLLEYS.remove(player);
                else VOLLEYS.put(player, new Volley(volley.weapon(), volley.first(), volley.second(), volley.secondary(),
                        volley.power(), volley.remaining() - 1, volley.nextShot() + WeaponTypeSkillRules.profile(volley.secondary(), volley.power()).interval()));
            }
        }
        if (player.getPersistentData().contains(STAFF_UNTIL)
                && (player.serverLevel().getGameTime() >= player.getPersistentData().getLong(STAFF_UNTIL)
                || kind(player.getMainHandItem()) != Kind.STAFF || FoodWeaponLifecycle.isBroken(player.getMainHandItem()))) clearFocus(player);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void impact(ProjectileImpactEvent event) {
        Projectile arrow = event.getProjectile();
        if (!(arrow.getOwner() instanceof ServerPlayer player)
                || !arrow.getPersistentData().contains(ARROW_TAG)
                || !(event.getRayTraceResult() instanceof EntityHitResult hit)
                || !(hit.getEntity() instanceof LivingEntity target)) return;
        if (!NonManorWeaponSkills.canTarget(player, target)) {
            event.setCanceled(true);
            arrow.discard();
            return;
        }
        target.invulnerableTime = 0;
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void arrowHit(LivingHurtEvent event) {
        if (event.getAmount() <= 0 || !(event.getSource().getDirectEntity() instanceof Projectile arrow)
                || !(arrow.getOwner() instanceof ServerPlayer player) || !NonManorWeaponSkills.canTarget(player, event.getEntity())) return;
        CompoundTag traits = arrow.getPersistentData().getCompound(ARROW_TAG);
        Aspect first = Aspect.fromId(traits.getString("First"));
        if (first == null) return;
        Aspect second = Aspect.fromId(traits.getString("Second"));
        boolean secondary = traits.getBoolean("Secondary");
        WeaponEvolutionSkills.applyTraits(event.getEntity(), player, first, second, player.position(), secondary, false);
        if (traits.getBoolean("GrantBoon")) {
            traits.remove("GrantBoon");
            WeaponEvolutionSkills.applyBoons(player, first, second, 1, secondary);
        }
    }

    private static boolean focus(ServerPlayer player, Profile profile) {
        Attribute spellPower = attribute("spell_power");
        Attribute manaRegen = attribute("mana_regen");
        if (spellPower == null || manaRegen == null || player.getAttribute(spellPower) == null
                || player.getAttribute(manaRegen) == null) return false;
        clearFocus(player);
        player.getAttribute(spellPower).addTransientModifier(new AttributeModifier(FOCUS_POWER, "Weapon focus", profile.spellPower(), AttributeModifier.Operation.MULTIPLY_TOTAL));
        player.getAttribute(manaRegen).addTransientModifier(new AttributeModifier(FOCUS_REGEN, "Weapon focus regeneration", profile.manaRegen(), AttributeModifier.Operation.MULTIPLY_TOTAL));
        player.getPersistentData().putLong(STAFF_UNTIL, player.serverLevel().getGameTime() + profile.duration());
        player.serverLevel().sendParticles(ParticleTypes.ENCHANT, player.getX(), player.getY() + 1, player.getZ(), 24, 0.5, 0.6, 0.5, 0.1);
        player.serverLevel().playSound(null, player.blockPosition(), SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.PLAYERS, 0.8F, 1.2F);
        return true;
    }

    private static Attribute attribute(String path) {
        return ForgeRegistries.ATTRIBUTES.getValue(new ResourceLocation("irons_spellbooks", path));
    }

    private static void clearFocus(ServerPlayer player) {
        Attribute spellPower = attribute("spell_power");
        Attribute manaRegen = attribute("mana_regen");
        if (spellPower != null && player.getAttribute(spellPower) != null) player.getAttribute(spellPower).removeModifier(FOCUS_POWER);
        if (manaRegen != null && player.getAttribute(manaRegen) != null) player.getAttribute(manaRegen).removeModifier(FOCUS_REGEN);
        player.getPersistentData().remove(STAFF_UNTIL);
    }

    @SubscribeEvent
    public static void logout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            VOLLEYS.remove(player);
            clearFocus(player);
        }
    }

    public static Kind kind(ItemStack stack) {
        if (stack.getItem() instanceof TridentItem || stack.getItem() instanceof TideTridentItem) return Kind.TRIDENT;
        if (stack.getItem() instanceof CrossbowItem) return Kind.CROSSBOW;
        return switch (FoodWeaponFamilyBuild.kind(stack)) {
            case BOW -> Kind.BOW;
            case STAFF -> Kind.STAFF;
            default -> Kind.MELEE;
        };
    }

    public static Component description(ItemStack stack, boolean secondary) {
        Profile profile = WeaponTypeSkillRules.profile(secondary, FoodWeaponSkillRarity.activeView(stack).multiplier());
        if (stack.getItem() instanceof BarrelCrossbowItem) return Component.literal("每0.2秒连射一颗原生爆炸酒桶，共"
                + profile.shots() + "颗；每发消耗一瓶背包酒类弹药（创造除外），不改动已装填弹药，切换武器中断，不破坏地形。");
        return Component.literal(switch (kind(stack)) {
            case BOW -> "向准星方向每0.2秒连射一箭，共" + profile.shots() + "箭，继承箭种、附魔与材料加成；每箭消耗箭矢（创造/无限除外），切换武器中断。";
            case CROSSBOW -> "向准星方向每0.2秒连射一弩箭，共" + profile.shots() + "箭" + (secondary ? "，额外穿透2个目标" : "")
                    + "；继承穿透附魔与材料加成，消耗背包箭矢，不改动已装填弹药，切换武器中断。";
            case TRIDENT -> "沿视线穿刺突进，最远" + (int) profile.dashRange()
                    + "格，命中路径上的敌人并附加进化属性；遇阻停下。含电弧进化时改为瞄准引雷，不破坏地形。";
            case STAFF -> String.format(Locale.ROOT, "奥术专注：持续%d秒，法术强度+%.0f%%、自然回蓝速度+%.0f%%；不代替法杖原本施法，切换为非法杖时结束，同类增益不叠加。",
                    profile.duration() / 20, profile.spellPower() * 100, profile.manaRegen() * 100);
            case MELEE -> stack.getItem() instanceof AxeItem
                    ? "近身重劈，对前方敌人造成武器攻击伤害并击退，附加进化属性；不再远程原地施法。"
                    : "顺着挥击横扫前方近战范围，伤害取自武器攻击并附加进化属性；不再远程原地施法。";
        });
    }

    public static boolean execute(ServerPlayer player, ItemStack stack, Aspect first, Aspect second,
                                  boolean secondary, double power, double flat, double multiplier) {
        Profile profile = WeaponTypeSkillRules.profile(secondary, power);
        return switch (kind(stack)) {
            case BOW, CROSSBOW -> volley(player, stack, first, second, secondary, power, profile);
            case STAFF -> focus(player, profile);
            case TRIDENT -> first == Aspect.ARC || second == Aspect.ARC
                    ? lightning(player, first, second, secondary, flat, multiplier)
                    : thrust(player, first, second, secondary, profile, flat, multiplier);
            case MELEE -> sweep(player, stack, first, second, secondary, flat, multiplier);
        };
    }
}
