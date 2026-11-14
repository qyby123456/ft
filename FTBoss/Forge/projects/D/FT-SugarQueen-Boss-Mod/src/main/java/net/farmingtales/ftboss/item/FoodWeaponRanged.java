package net.farmingtales.ftboss.item;

import com.google.common.collect.MapMaker;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.util.FoodWeaponBuildRules.Material;
import net.farmingtales.ftboss.util.FoodWeaponBuildRules.Part;
import net.farmingtales.ftboss.util.FoodWeaponFamilyRules;
import net.farmingtales.ftboss.util.FoodWeaponFamilyRules.Kind;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** 远程武器蓄力与施法快照；2026-10-13 副手和离手命中不借主手材料，成功施法后结算真实耐久。 */
@Mod.EventBusSubscriber(modid = FTBossMod.MOD_ID)
public final class FoodWeaponRanged {
    private static final Map<LivingEntity, ChargeState> CHARGES = new MapMaker().weakKeys().makeMap();
    private static final ThreadLocal<CastState> CAST = new ThreadLocal<>();
    private static final int SNAPSHOT_VERSION = 1;
    private static final String PROC_TIMERS = "FTBossRangedProcTimers";
    private static final String PENDING_BOW_SHOT = "FTBossPendingBowShot";
    private record ChargeState(ItemStack weapon, int remaining, double carry, double speed) {}
    private record CastState(ServerPlayer player, CompoundTag snapshot) {}

    private FoodWeaponRanged() {}

    @SubscribeEvent
    public static void beginCharge(LivingEntityUseItemEvent.Start event) {
        synchronized (CHARGES) { CHARGES.remove(event.getEntity()); }
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void charge(LivingEntityUseItemEvent.Tick event) {
        if (BowChargeProgress.full(event)) return;
        ItemStack weapon = event.getItem();
        if (!(weapon.getItem() instanceof BowItem) || weapon.getTagElement(WeaponMaterialBuild.ROOT) == null
                && !FourWeaponFoundationAdapter.applies(weapon)
                || FoodWeaponLifecycle.isBroken(weapon) || event.getDuration() <= 0) return;
        ChargeState state;
        synchronized (CHARGES) { state = CHARGES.get(event.getEntity()); }
        boolean fresh = state == null || state.weapon() != weapon || event.getDuration() > state.remaining();
        double carry = fresh ? 0.0 : state.carry();
        double speed = fresh ? BowGrowthCombat.chargeMultiplier(weapon) : state.speed();
        var charge = FoodWeaponFamilyRules.chargeStep(event.getDuration(), speed, carry);
        event.setDuration(charge.remaining());
        synchronized (CHARGES) {
            CHARGES.put(event.getEntity(), new ChargeState(weapon, charge.remaining(), charge.carry(), speed));
        }
    }

    @SubscribeEvent
    public static void endCharge(LivingEntityUseItemEvent.Stop event) {
        synchronized (CHARGES) { CHARGES.remove(event.getEntity()); }
    }

    @SubscribeEvent
    public static void finishCharge(LivingEntityUseItemEvent.Finish event) {
        synchronized (CHARGES) { CHARGES.remove(event.getEntity()); }
    }

    public static CompoundTag capture(ServerPlayer player, ItemStack weapon) {
        if (weapon.isEmpty() || FoodWeaponFamilyBuild.kind(weapon) == Kind.MELEE
                || FoodWeaponLifecycle.isBroken(weapon)) return new CompoundTag();
        FoodWeaponLifecycle.ensureTracked(weapon);
        CompoundTag data = weapon.getTagElement(WeaponMaterialBuild.ROOT);
        CompoundTag snapshot = data == null ? new CompoundTag() : data.copy();
        snapshot.putInt("SnapshotVersion", SNAPSHOT_VERSION);
        snapshot.putUUID("Owner", player.getUUID());
        snapshot.putString("Family", FoodWeaponFamilyBuild.kind(weapon).name());
        snapshot.putDouble("ProjectileDamage", FoodWeaponFamilyBuild.damageMultiplier(weapon));
        snapshot.putDouble("LaunchSpeed", FoodWeaponFamilyBuild.rangeMultiplier(weapon));
        snapshot.put("Weapon", weapon.save(new CompoundTag()));
        snapshot.putString("NativeProfile", FoodWeaponBossPassive.profile(weapon));
        WeaponMaterialPowers.snapshot(weapon, snapshot);
        return snapshot;
    }

    public static void launch(Entity entity, CompoundTag snapshot) {
        if (!(entity instanceof Projectile projectile) || !(projectile.getOwner() instanceof ServerPlayer owner)
                || !owned(snapshot, owner) || entity.getPersistentData().contains(WeaponMaterialBuild.ROOT, Tag.TAG_COMPOUND)) return;
        entity.getPersistentData().put(WeaponMaterialBuild.ROOT, snapshot.copy());
        RangedHybridCombat.launch(entity, snapshot);
    }

    public static void prepareBowShot(Entity entity, CompoundTag snapshot) {
        if (!(entity instanceof AbstractArrow projectile) || !(projectile.getOwner() instanceof ServerPlayer owner)
                || !owned(snapshot, owner) || !snapshot.getString("Family").equals(Kind.BOW.name())
                || entity.getPersistentData().contains(WeaponMaterialBuild.ROOT, Tag.TAG_COMPOUND)
                || entity.getPersistentData().contains(PENDING_BOW_SHOT, Tag.TAG_COMPOUND)) return;
        entity.getPersistentData().put(PENDING_BOW_SHOT, snapshot.copy());
    }

    public static void launchPreparedBow(Entity entity) {
        CompoundTag snapshot = entity.getPersistentData().getCompound(PENDING_BOW_SHOT);
        entity.getPersistentData().remove(PENDING_BOW_SHOT);
        launch(entity, snapshot);
    }

    public static void projectileJoin(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide || !(event.getEntity() instanceof Projectile projectile)
                || !(projectile.getOwner() instanceof ServerPlayer player)
                || projectile.getPersistentData().contains(WeaponMaterialBuild.ROOT, Tag.TAG_COMPOUND)) return;
        CastState casting = CAST.get();
        if (casting != null && casting.player() == player) {
            launch(projectile, casting.snapshot());
            return;
        }
    }

    private static boolean owned(CompoundTag snapshot, ServerPlayer player) {
        return snapshot.getInt("SnapshotVersion") == SNAPSHOT_VERSION && snapshot.hasUUID("Owner")
                && snapshot.getUUID("Owner").equals(player.getUUID()) && snapshot.contains("Weapon", Tag.TAG_COMPOUND);
    }

    public static CompoundTag materialSnapshot(DamageSource source, ServerPlayer player) {
        Entity direct = source.getDirectEntity();
        if (direct instanceof Projectile projectile && projectile.getOwner() == player) {
            CompoundTag snapshot = direct.getPersistentData().getCompound(WeaponMaterialBuild.ROOT);
            if (direct instanceof AbstractArrow && snapshot.getString("Family").equals(Kind.BOW.name())
                    && !source.is(DamageTypeTags.IS_PROJECTILE)) return new CompoundTag();
            return owned(snapshot, player) ? snapshot : new CompoundTag();
        }
        CastState casting = CAST.get();
        if (direct == player && magicSource(source) && casting != null && casting.player() == player) {
            return casting.snapshot();
        }
        return new CompoundTag();
    }

    private static boolean magicSource(DamageSource source) {
        if (source.is(DamageTypes.MAGIC) || source.is(DamageTypes.INDIRECT_MAGIC)) return true;
        for (Class<?> type = source.getClass(); type != null; type = type.getSuperclass()) {
            if (type.getName().equals("io.redspace.ironsspellbooks.damage.SpellDamageSource")) return true;
        }
        return false;
    }

    public static List<Material> attackMaterials(DamageSource source, ServerPlayer player) {
        CompoundTag snapshot = materialSnapshot(source, player);
        if (snapshot.isEmpty()) return List.of();
        List<Material> result = new ArrayList<>();
        for (Part part : Part.values()) result.add(Material.byId(snapshot.getString(part.name())));
        return List.copyOf(result);
    }

    public static ItemStack sourceWeapon(DamageSource source, ServerPlayer player) {
        CompoundTag snapshot = materialSnapshot(source, player);
        if (!snapshot.isEmpty()) return ItemStack.of(snapshot.getCompound("Weapon"));
        return source.getDirectEntity() == player && source.is(DamageTypes.PLAYER_ATTACK)
                && !magicSource(source)
                ? player.getMainHandItem() : ItemStack.EMPTY;
    }

    public static void prepareHitTimers(ServerPlayer player, DamageSource source, CompoundTag data) {
        if (materialSnapshot(source, player).isEmpty()) return;
        CompoundTag timers = player.getPersistentData().getCompound(PROC_TIMERS);
        for (String key : timers.getAllKeys()) {
            if (key.startsWith("Last") && timers.contains(key, Tag.TAG_LONG)) {
                data.putLong(key, Math.max(data.getLong(key), timers.getLong(key)));
            }
        }
    }

    public static void saveHitTimers(ServerPlayer player, DamageSource source, CompoundTag data) {
        if (materialSnapshot(source, player).isEmpty()) return;
        CompoundTag timers = player.getPersistentData().getCompound(PROC_TIMERS);
        for (String key : data.getAllKeys()) {
            if (key.startsWith("Last") && data.contains(key, Tag.TAG_LONG)) {
                timers.putLong(key, Math.max(data.getLong(key), timers.getLong(key)));
            }
        }
        player.getPersistentData().put(PROC_TIMERS, timers);
    }

    public static boolean canCast(ServerPlayer player, ItemStack weapon) {
        if (weapon == null || FoodWeaponFamilyBuild.kind(weapon) != Kind.STAFF) return true;
        return (player.getMainHandItem() == weapon || player.getOffhandItem() == weapon)
                && !FoodWeaponLifecycle.isBroken(weapon);
    }

    public static void withStaffCast(ServerPlayer player, ItemStack weapon, int wearCost, Runnable action) {
        boolean staff = weapon != null && FoodWeaponFamilyBuild.kind(weapon) == Kind.STAFF;
        if (staff && !canCast(player, weapon)) return;
        CastState previous = CAST.get();
        if (staff) CAST.set(new CastState(player, capture(player, weapon)));
        else CAST.remove();
        try {
            action.run();
            if (staff) {
                InteractionHand hand = player.getMainHandItem() == weapon ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
                weapon.hurtAndBreak(Math.max(1, Math.min(8, wearCost)), player, holder -> holder.broadcastBreakEvent(hand));
                player.getInventory().setChanged();
                player.containerMenu.broadcastChanges();
            }
        } finally {
            if (previous == null) CAST.remove();
            else CAST.set(previous);
        }
    }
}
