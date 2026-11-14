/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.farmingtales.armamentariumguard.ArmamentariumTerrainActions
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.InteractionResultHolder
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.entity.projectile.AbstractArrow
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemCooldowns
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.Level
 */
package net.farmingtales.ftboss.item;

import com.farmingtales.armamentariumguard.ArmamentariumTerrainActions;
import java.util.ArrayList;
import java.util.List;
import net.farmingtales.ftboss.item.NativeEvergaleCarrier;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemCooldowns;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public final class NativeEvergaleBridge {
    private static final ThreadLocal<RawAction> CURRENT = new ThreadLocal();
    private static volatile boolean disabled;

    private NativeEvergaleBridge() {
    }

    public static boolean isEvergale(Item item) {
        return item != null && item.getClass().getName().equals("net.gqstavo.armamentarium.item.EvergaleWandItem");
    }

    public static boolean isAirStrike(AbstractArrow abstractArrow) {
        return abstractArrow != null && abstractArrow.getClass().getName().equals("net.gqstavo.armamentarium.entity.AirStrikeEntity");
    }

    static boolean observationDisabled() {
        return disabled;
    }

    static void observationFailed(RawAction rawAction, Throwable throwable) {
        disabled = true;
        if (rawAction != null && rawAction.observationFailure == null) {
            rawAction.observationFailure = throwable;
        }
    }

    private static RawAction open(Site site, Item item, ItemStack itemStack, LivingEntity livingEntity, Level level, InteractionHand interactionHand) {
        if (disabled) {
            return null;
        }
        RawAction rawAction = null;
        try {
            rawAction = CURRENT.get();
            if (rawAction != null) {
                rawAction.nested = true;
            }
            CURRENT.remove();
            RawAction rawAction2 = new RawAction(site, item, itemStack, livingEntity, level, interactionHand, rawAction);
            CURRENT.set(rawAction2);
            return rawAction2;
        }
        catch (Throwable throwable) {
            NativeEvergaleBridge.observationFailed(rawAction, throwable);
            try {
                if (rawAction == null) {
                    CURRENT.remove();
                } else {
                    CURRENT.set(rawAction);
                }
            }
            catch (Throwable throwable2) {
                NativeEvergaleBridge.observationFailed(rawAction, throwable2);
            }
            throw throwable;
        }
    }

    private static RawAction currentSwing() {
        RawAction rawAction = disabled ? null : CURRENT.get();
        return rawAction != null && rawAction.site == Site.SWING && !rawAction.closed ? rawAction : null;
    }

    public static InteractionResultHolder<ItemStack> use(Item item, ItemStack itemStack, Level level, Player player, InteractionHand interactionHand) {
        boolean bl = false;
        try {
            bl = NativeEvergaleBridge.isEvergale(item);
        }
        catch (Throwable throwable) {
            NativeEvergaleBridge.observationFailed(null, throwable);
        }
        if (!bl) {
            return item.m_7203_(level, player, interactionHand);
        }
        RawAction rawAction = null;
        try {
            rawAction = NativeEvergaleBridge.open(Site.USE, item, itemStack, (LivingEntity)player, level, interactionHand);
        }
        catch (Throwable throwable) {
            NativeEvergaleBridge.observationFailed(rawAction, throwable);
        }
        if (rawAction != null) {
            rawAction.originalEntered = true;
        }
        try {
            InteractionResultHolder interactionResultHolder = item.m_7203_(level, player, interactionHand);
            if (rawAction != null) {
                rawAction.originalReturn = interactionResultHolder;
                rawAction.originalReturned = true;
            }
            InteractionResultHolder interactionResultHolder2 = interactionResultHolder;
            return interactionResultHolder2;
        }
        catch (Throwable throwable) {
            if (rawAction != null) {
                rawAction.originalFailure = throwable;
            }
            throw throwable;
        }
        finally {
            NativeEvergaleBridge.close(rawAction);
        }
    }

    public static boolean swing(Item item, ItemStack itemStack, LivingEntity livingEntity) {
        boolean bl = false;
        try {
            bl = NativeEvergaleBridge.isEvergale(item);
        }
        catch (Throwable throwable) {
            NativeEvergaleBridge.observationFailed(null, throwable);
        }
        if (!bl) {
            return item.onEntitySwing(itemStack, livingEntity);
        }
        RawAction rawAction = null;
        try {
            rawAction = NativeEvergaleBridge.open(Site.SWING, item, itemStack, livingEntity, null, null);
        }
        catch (Throwable throwable) {
            NativeEvergaleBridge.observationFailed(rawAction, throwable);
        }
        if (rawAction != null) {
            rawAction.originalEntered = true;
        }
        try {
            boolean bl2 = item.onEntitySwing(itemStack, livingEntity);
            if (rawAction != null) {
                rawAction.originalReturn = bl2;
                rawAction.originalReturned = true;
            }
            boolean bl3 = bl2;
            return bl3;
        }
        catch (Throwable throwable) {
            if (rawAction != null) {
                rawAction.originalFailure = throwable;
            }
            throw throwable;
        }
        finally {
            NativeEvergaleBridge.close(rawAction);
        }
    }

    public static void setOwner(AbstractArrow abstractArrow, Entity entity) {
        RawOwnerAssignment rawOwnerAssignment = null;
        try {
            RawAction rawAction = NativeEvergaleBridge.currentSwing();
            if (rawAction != null) {
                rawOwnerAssignment = new RawOwnerAssignment(rawAction, abstractArrow, entity);
                rawAction.owners.add(rawOwnerAssignment);
            }
        }
        catch (Throwable throwable) {
            NativeEvergaleBridge.observationFailed(rawOwnerAssignment == null ? null : rawOwnerAssignment.action, throwable);
        }
        if (rawOwnerAssignment != null) {
            rawOwnerAssignment.originalEntered = true;
        }
        try {
            abstractArrow.m_5602_(entity);
            if (rawOwnerAssignment != null) {
                rawOwnerAssignment.originalReturned = true;
            }
        }
        catch (Throwable throwable) {
            if (rawOwnerAssignment != null) {
                rawOwnerAssignment.originalFailure = throwable;
            }
            throw throwable;
        }
    }

    public static boolean spawn(Level level, Entity entity) {
        RawSpawn rawSpawn = null;
        try {
            RawAction rawAction = NativeEvergaleBridge.currentSwing();
            if (rawAction != null) {
                rawSpawn = new RawSpawn(rawAction, level, entity);
                rawAction.spawns.add(rawSpawn);
            }
        }
        catch (Throwable throwable) {
            NativeEvergaleBridge.observationFailed(rawSpawn == null ? null : rawSpawn.action, throwable);
        }
        if (rawSpawn != null) {
            rawSpawn.originalEntered = true;
        }
        try {
            boolean bl = ArmamentariumTerrainActions.spawnSkillEntity((Level)level, (Entity)entity);
            if (rawSpawn != null) {
                rawSpawn.originalReturn = bl;
                rawSpawn.originalReturned = true;
            }
            return bl;
        }
        catch (Throwable throwable) {
            if (rawSpawn != null) {
                rawSpawn.originalFailure = throwable;
            }
            throw throwable;
        }
    }

    public static void cooldown(ItemCooldowns itemCooldowns, Item item, int n) {
        RawCooldown rawCooldown = null;
        try {
            RawAction rawAction = NativeEvergaleBridge.currentSwing();
            if (rawAction != null) {
                rawCooldown = new RawCooldown(rawAction, itemCooldowns, item, n);
                rawAction.cooldowns.add(rawCooldown);
            }
        }
        catch (Throwable throwable) {
            NativeEvergaleBridge.observationFailed(rawCooldown == null ? null : rawCooldown.action, throwable);
        }
        if (rawCooldown != null) {
            rawCooldown.originalEntered = true;
        }
        try {
            itemCooldowns.m_41524_(item, n);
            if (rawCooldown != null) {
                rawCooldown.originalReturned = true;
            }
        }
        catch (Throwable throwable) {
            if (rawCooldown != null) {
                rawCooldown.originalFailure = throwable;
            }
            throw throwable;
        }
    }

    private static void close(RawAction rawAction) {
        if (rawAction == null) {
            return;
        }
        try {
            if (CURRENT.get() != rawAction) {
                NativeEvergaleBridge.observationFailed(rawAction, new IllegalStateException("Evergale raw action nesting changed"));
            }
            if (rawAction.previous == null) {
                CURRENT.remove();
            } else {
                CURRENT.set(rawAction.previous);
            }
        }
        catch (Throwable throwable) {
            NativeEvergaleBridge.observationFailed(rawAction, throwable);
        }
        finally {
            rawAction.closed = true;
        }
        try {
            NativeEvergaleBridge.retainOriginalCarriers(rawAction);
        }
        catch (Throwable throwable) {
            NativeEvergaleBridge.observationFailed(rawAction, throwable);
        }
    }

    private static void retainOriginalCarriers(RawAction rawAction) {
        if (disabled || rawAction.site != Site.SWING || rawAction.nested || !rawAction.closed || !rawAction.originalReturned || rawAction.originalFailure != null || rawAction.observationFailure != null) {
            return;
        }
        for (RawSpawn rawSpawn : rawAction.spawns) {
            AbstractArrow abstractArrow;
            Object object;
            if (!rawSpawn.originalReturned || !rawSpawn.originalReturn || !((object = rawSpawn.originalEntity) instanceof AbstractArrow) || !NativeEvergaleBridge.isAirStrike(abstractArrow = (AbstractArrow)object) || !(abstractArrow instanceof NativeEvergaleCarrier)) continue;
            NativeEvergaleCarrier nativeEvergaleCarrier = (NativeEvergaleCarrier)abstractArrow;
            object = null;
            Object object2 = rawAction.owners.iterator();
            while (object2.hasNext()) {
                RawOwnerAssignment rawOwnerAssignment = object2.next();
                if (rawOwnerAssignment.originalReceiver != abstractArrow || rawOwnerAssignment.originalOwner != rawAction.originalActor || !rawOwnerAssignment.originalReturned || rawOwnerAssignment.originalFailure != null) continue;
                object = rawOwnerAssignment;
            }
            if (object == null) continue;
            rawSpawn.carry = object2 = new RawCarry(rawAction, rawSpawn, (RawOwnerAssignment)object);
            try {
                nativeEvergaleCarrier.ftboss$observeEvergaleCarry((RawCarry)object2);
                rawSpawn.fieldObservedSame = nativeEvergaleCarrier.ftboss$originalEvergaleCarry() == object2;
            }
            catch (Throwable throwable) {
                rawSpawn.fieldFailure = throwable;
                NativeEvergaleBridge.observationFailed(rawAction, throwable);
            }
        }
    }

    public static final class RawAction {
        final Site site;
        final Item originalReceiver;
        final ItemStack originalStack;
        final LivingEntity originalActor;
        final Level originalLevel;
        final InteractionHand originalHand;
        final Thread originalThread = Thread.currentThread();
        final RawAction previous;
        final List<RawOwnerAssignment> owners = new ArrayList<RawOwnerAssignment>();
        final List<RawSpawn> spawns = new ArrayList<RawSpawn>();
        final List<RawCooldown> cooldowns = new ArrayList<RawCooldown>();
        boolean originalEntered;
        boolean originalReturned;
        Object originalReturn;
        Throwable originalFailure;
        Throwable observationFailure;
        boolean nested;
        boolean closed;

        private RawAction(Site site, Item item, ItemStack itemStack, LivingEntity livingEntity, Level level, InteractionHand interactionHand, RawAction rawAction) {
            this.site = site;
            this.originalReceiver = item;
            this.originalStack = itemStack;
            this.originalActor = livingEntity;
            this.originalLevel = level;
            this.originalHand = interactionHand;
            this.previous = rawAction;
            this.nested = rawAction != null;
        }
    }

    static enum Site {
        USE,
        SWING;

    }

    public static final class RawOwnerAssignment {
        final RawAction action;
        final AbstractArrow originalReceiver;
        final Entity originalOwner;
        boolean originalEntered;
        boolean originalReturned;
        Throwable originalFailure;

        private RawOwnerAssignment(RawAction rawAction, AbstractArrow abstractArrow, Entity entity) {
            this.action = rawAction;
            this.originalReceiver = abstractArrow;
            this.originalOwner = entity;
        }
    }

    public static final class RawSpawn {
        final RawAction action;
        final Level originalLevel;
        final Entity originalEntity;
        boolean originalEntered;
        boolean originalReturned;
        boolean originalReturn;
        Throwable originalFailure;
        RawCarry carry;
        boolean fieldObservedSame;
        Throwable fieldFailure;

        private RawSpawn(RawAction rawAction, Level level, Entity entity) {
            this.action = rawAction;
            this.originalLevel = level;
            this.originalEntity = entity;
        }
    }

    public static final class RawCooldown {
        final RawAction action;
        final ItemCooldowns originalReceiver;
        final Item originalItem;
        final int originalTicks;
        boolean originalEntered;
        boolean originalReturned;
        Throwable originalFailure;

        private RawCooldown(RawAction rawAction, ItemCooldowns itemCooldowns, Item item, int n) {
            this.action = rawAction;
            this.originalReceiver = itemCooldowns;
            this.originalItem = item;
            this.originalTicks = n;
        }
    }

    public static final class RawCarry {
        final RawAction action;
        final RawSpawn spawn;
        final RawOwnerAssignment owner;

        private RawCarry(RawAction rawAction, RawSpawn rawSpawn, RawOwnerAssignment rawOwnerAssignment) {
            this.action = rawAction;
            this.spawn = rawSpawn;
            this.owner = rawOwnerAssignment;
        }
    }
}

