/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.redspace.ironsspellbooks.api.events.SpellOnCastEvent
 *  io.redspace.ironsspellbooks.api.magic.MagicData
 *  io.redspace.ironsspellbooks.api.magic.SpellSelectionManager$SelectionOption
 *  io.redspace.ironsspellbooks.api.spells.AbstractSpell
 *  io.redspace.ironsspellbooks.api.spells.CastSource
 *  io.redspace.ironsspellbooks.api.spells.ISpellContainer
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.server.MinecraftServer
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.Level
 *  net.minecraftforge.eventbus.api.Event
 *  net.minecraftforge.eventbus.api.IEventBus
 */
package net.farmingtales.ftboss.integ;

import io.redspace.ironsspellbooks.api.events.SpellOnCastEvent;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.magic.SpellSelectionManager;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.CastSource;
import io.redspace.ironsspellbooks.api.spells.ISpellContainer;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import net.farmingtales.ftboss.item.FoodWeaponRanged;
import net.farmingtales.ftboss.item.FourWeaponFoundationAdapter;
import net.farmingtales.ftboss.item.NativeActionObservation;
import net.farmingtales.ftboss.item.NativeWeaponTypes;
import net.farmingtales.ftboss.transaction.FoundationApprovedRules;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.IEventBus;

public final class NativeIronCastObservation {
    private static final Map<LivingEntity, Selection> SELECTIONS = new WeakHashMap<LivingEntity, Selection>();
    private static final Map<MagicData, Cast> CASTS = new WeakHashMap<MagicData, Cast>();
    private static final Map<LivingEntity, Cast> LAST = new WeakHashMap<LivingEntity, Cast>();
    private static final Map<MagicData, Trigger> TRIGGERS = new WeakHashMap<MagicData, Trigger>();
    private static final Map<MagicData, LivingEntity> CASTERS = new WeakHashMap<MagicData, LivingEntity>();
    private static final Map<LivingEntity, Attempt> ATTEMPTS = new WeakHashMap<LivingEntity, Attempt>();

    private NativeIronCastObservation() {
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void selection(LivingEntity livingEntity, SpellSelectionManager.SelectionOption selectionOption) {
        if (livingEntity == null || livingEntity.m_9236_().f_46443_) {
            return;
        }
        Object object = SELECTIONS;
        synchronized (object) {
            SELECTIONS.remove(livingEntity);
        }
        if (selectionOption == null || selectionOption.spellData == null) {
            return;
        }
        try {
            object = NativeActionObservation.current();
            ItemStack itemStack = object == null ? ItemStack.f_41583_ : ItemStack.m_41712_((CompoundTag)((NativeActionObservation.Invocation)object).before());
            ISpellContainer iSpellContainer = itemStack.m_41619_() ? null : ISpellContainer.get((ItemStack)itemStack.m_41777_());
            Selection selection = new Selection(object == null ? "" : ((NativeActionObservation.Invocation)object).itemId(), selectionOption.slot, selectionOption.slotIndex, selectionOption.spellData.getSpell().getSpellId(), selectionOption.spellData.getLevel(), selectionOption.getCastSource().name(), iSpellContainer == null ? 0 : iSpellContainer.getActiveSpellCount(), "SELECTED_OPTION_NOT_AUTHORITY");
            Map<LivingEntity, Selection> map = SELECTIONS;
            synchronized (map) {
                SELECTIONS.put(livingEntity, selection);
            }
        }
        catch (RuntimeException runtimeException) {
            // empty catch block
        }
    }

    public static int effectiveTime(AbstractSpell abstractSpell, int n, LivingEntity livingEntity) {
        return abstractSpell.getEffectiveCastTime(n, livingEntity);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Unable to fully structure code
     * Could not resolve type clashes
     */
    public static void bind(MagicData var0, AbstractSpell var1_1, LivingEntity var2_2, ItemStack var3_3, int var4_4, CastSource var5_5, String var6_6) {
        if (var2_2.m_9236_().f_46443_) {
            return;
        }
        try {
            var7_7 = NativeWeaponTypes.staff(var3_3) != false ? "STAFF" : (NativeWeaponTypes.types(var3_3.m_41720_().getClass()).contains("io.redspace.ironsspellbooks.api.item.IScroll") != false ? "SCROLL" : "NON_STAFF");
            var8_9 = new Cast(NativeWeaponTypes.id(var3_3), var7_7, var5_5.name(), var6_6, var1_1.getSpellId(), var4_4, var0.getCastDurationRemaining(), 0, 0, "STARTED", 0L, "NATIVE_ONLY_OWNER_RECEIPT_REQUIRED");
            var9_10 = NativeIronCastObservation.CASTS;
            synchronized (var9_10) {
                NativeIronCastObservation.CASTS.put(var0, var8_9);
            }
            var9_10 = NativeActionObservation.current();
            if (!(var2_2 instanceof ServerPlayer)) ** GOTO lbl-1000
            var11_13 /* !! */  = (ServerPlayer)var2_2;
            if (FourWeaponFoundationAdapter.applies(var3_3)) {
                v0 = new FoundationApprovedRules().fingerprint(var11_13 /* !! */ , var3_3);
            } else lbl-1000:
            // 2 sources

            {
                v0 = "";
            }
            var10_12 = v0;
            var11_13 /* !! */  = NativeIronCastObservation.TRIGGERS;
            synchronized (var11_13 /* !! */ ) {
                NativeIronCastObservation.TRIGGERS.put(var0, new Trigger(var3_3.m_41777_(), var9_10 == null ? null : var9_10.hand(), var10_12));
            }
            var11_13 /* !! */  = NativeIronCastObservation.CASTERS;
            synchronized (var11_13 /* !! */ ) {
                NativeIronCastObservation.CASTERS.put(var0, var2_2);
            }
            var11_13 /* !! */  = NativeIronCastObservation.LAST;
            synchronized (var11_13 /* !! */ ) {
                NativeIronCastObservation.LAST.put(var2_2, var8_9);
            }
        }
        catch (RuntimeException var7_8) {
            NativeIronCastObservation.clear(var0);
        }
    }

    public static boolean postCost(IEventBus iEventBus, Event event, LivingEntity livingEntity) {
        int n;
        if (event instanceof SpellOnCastEvent) {
            SpellOnCastEvent spellOnCastEvent = (SpellOnCastEvent)event;
            n = spellOnCastEvent.getManaCost();
        } else {
            n = 0;
        }
        int n2 = n;
        boolean bl = iEventBus.post(event);
        if (event instanceof SpellOnCastEvent) {
            SpellOnCastEvent spellOnCastEvent = (SpellOnCastEvent)event;
            NativeIronCastObservation.update(MagicData.getPlayerMagicData((LivingEntity)livingEntity), livingEntity, "COST_AFTER_NATIVE_BUS", n2, spellOnCastEvent.getManaCost(), spellOnCastEvent.getSpellLevel());
        }
        return bl;
    }

    private static void update(MagicData magicData, LivingEntity livingEntity, String string, Integer n, Integer n2) {
        NativeIronCastObservation.update(magicData, livingEntity, string, n, n2, null);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static void update(MagicData magicData, LivingEntity livingEntity, String string, Integer n, Integer n2, Integer n3) {
        try {
            Map<MagicData, Cast> map = CASTS;
            synchronized (map) {
                Cast cast = CASTS.get(magicData);
                if (cast == null || !cast.spellId().equals(magicData.getCastingSpellId()) || !cast.source().equals(magicData.getCastSource().name())) {
                    return;
                }
                Cast cast2 = new Cast(cast.actualItemId(), cast.physicalKind(), cast.source(), cast.actualSlot(), cast.spellId(), n3 == null ? cast.effectiveLevel() : n3.intValue(), cast.effectiveTicks(), n == null ? cast.manaBeforeEvent() : n.intValue(), n2 == null ? cast.manaAfterEvent() : n2.intValue(), string, cast.callbacks() + 1L, cast.authorization());
                CASTS.put(magicData, cast2);
                Map<LivingEntity, Cast> map2 = LAST;
                synchronized (map2) {
                    LAST.put(livingEntity, cast2);
                }
            }
        }
        catch (RuntimeException runtimeException) {
            // empty catch block
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void callback(MagicData magicData, LivingEntity livingEntity, String string, boolean bl, Runnable runnable) {
        block21: {
            block22: {
                Object object;
                Cast cast;
                Map<MagicData, Cast> map = CASTS;
                synchronized (map) {
                    cast = CASTS.get(magicData);
                }
                boolean bl2 = false;
                try {
                    Object object2 = TRIGGERS;
                    synchronized (object2) {
                        object = TRIGGERS.getOrDefault(magicData, new Trigger(ItemStack.f_41583_, null, ""));
                    }
                    if (livingEntity instanceof ServerPlayer) {
                        object2 = (ServerPlayer)livingEntity;
                        if (!((Trigger)object).stack().m_41619_() && FourWeaponFoundationAdapter.applies(((Trigger)object).stack())) {
                            try {
                                boolean bl3;
                                String string2 = new FoundationApprovedRules().fingerprint((ServerPlayer)object2, ((Trigger)object).stack());
                                boolean bl4 = bl3 = ((Trigger)object).hand() != null && object2.m_21120_(((Trigger)object).hand()).m_41739_(new CompoundTag()).equals((Object)((Trigger)object).stack().m_41739_(new CompoundTag()));
                                if (!bl3 || string2 == null || !string2.equals(((Trigger)object).rules())) {
                                    NativeIronCastObservation.clear(magicData);
                                    object = new Trigger(ItemStack.f_41583_, null, "");
                                }
                            }
                            catch (RuntimeException runtimeException) {
                                NativeIronCastObservation.clear(magicData);
                                object = new Trigger(ItemStack.f_41583_, null, "");
                            }
                        }
                    }
                    object2 = object;
                    NativeActionObservation.invoke(string.equals("SUCCESSFUL_ON_CAST") ? NativeActionObservation.Action.CAST : NativeActionObservation.Action.CALLBACK, ((Trigger)object2).stack().m_41777_(), livingEntity, ((Trigger)object2).hand(), () -> {
                        runnable.run();
                        return null;
                    });
                    bl2 = true;
                    NativeIronCastObservation.update(magicData, livingEntity, string, null, null);
                    if (!bl) break block21;
                    if (cast == null) break block22;
                    object = LAST;
                }
                catch (Throwable throwable) {
                    if (bl) {
                        if (cast != null) {
                            Map<LivingEntity, Cast> map2 = LAST;
                            synchronized (map2) {
                                LAST.put(livingEntity, new Cast(cast.actualItemId(), cast.physicalKind(), cast.source(), cast.actualSlot(), cast.spellId(), cast.effectiveLevel(), cast.effectiveTicks(), cast.manaBeforeEvent(), cast.manaAfterEvent(), (String)(bl2 ? string : "THREW_" + string), cast.callbacks() + 1L, cast.authorization()));
                            }
                        }
                        NativeIronCastObservation.clear(magicData);
                    }
                    throw throwable;
                }
                synchronized (object) {
                    LAST.put(livingEntity, new Cast(cast.actualItemId(), cast.physicalKind(), cast.source(), cast.actualSlot(), cast.spellId(), cast.effectiveLevel(), cast.effectiveTicks(), cast.manaBeforeEvent(), cast.manaAfterEvent(), (String)(bl2 ? string : "THREW_" + string), cast.callbacks() + 1L, cast.authorization()));
                }
            }
            NativeIronCastObservation.clear(magicData);
        }
    }

    public static void cast(AbstractSpell abstractSpell, Level level, int n, LivingEntity livingEntity, CastSource castSource, MagicData magicData) {
        NativeIronCastObservation.callback(magicData, livingEntity, "SUCCESSFUL_ON_CAST", false, () -> FoodWeaponRanged.withSource(null, new CompoundTag(), () -> {
            abstractSpell.onCast(level, n, livingEntity, castSource, magicData);
            return null;
        }));
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void attemptReturned(AbstractSpell abstractSpell, ItemStack itemStack, int n, LivingEntity livingEntity, CastSource castSource, String string, boolean bl) {
        if (livingEntity == null || livingEntity.m_9236_().f_46443_) {
            return;
        }
        try {
            Map<LivingEntity, Attempt> map = ATTEMPTS;
            synchronized (map) {
                ATTEMPTS.put(livingEntity, new Attempt(NativeWeaponTypes.id(itemStack), abstractSpell.getSpellId(), n, castSource.name(), string, bl));
            }
        }
        catch (RuntimeException runtimeException) {
            // empty catch block
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static Attempt attempt(LivingEntity livingEntity) {
        Map<LivingEntity, Attempt> map = ATTEMPTS;
        synchronized (map) {
            return ATTEMPTS.get(livingEntity);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void clear(MagicData magicData) {
        LivingEntity livingEntity = CASTS;
        synchronized (livingEntity) {
            CASTS.remove(magicData);
        }
        livingEntity = TRIGGERS;
        synchronized (livingEntity) {
            TRIGGERS.remove(magicData);
        }
        Map<MagicData, LivingEntity> map = CASTERS;
        synchronized (map) {
            livingEntity = CASTERS.remove(magicData);
        }
        if (livingEntity != null) {
            NativeIronCastObservation.clearDiagnostics(livingEntity);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static void clearDiagnostics(LivingEntity livingEntity) {
        Map<LivingEntity, Record> map = SELECTIONS;
        synchronized (map) {
            SELECTIONS.remove(livingEntity);
        }
        map = LAST;
        synchronized (map) {
            LAST.remove(livingEntity);
        }
        map = ATTEMPTS;
        synchronized (map) {
            ATTEMPTS.remove(livingEntity);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void clear(LivingEntity livingEntity) {
        List<MagicData> list;
        Map<MagicData, LivingEntity> map = CASTERS;
        synchronized (map) {
            list = CASTERS.entrySet().stream().filter(entry -> entry.getValue() == livingEntity).map(Map.Entry::getKey).toList();
        }
        for (MagicData magicData : list) {
            NativeIronCastObservation.clear(magicData);
        }
        NativeIronCastObservation.clearDiagnostics(livingEntity);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void clearServer(MinecraftServer minecraftServer) {
        HashSet<LivingEntity> hashSet;
        Map<MagicData, LivingEntity> map = CASTERS;
        synchronized (map) {
            hashSet = new HashSet<LivingEntity>(CASTERS.values());
        }
        for (LivingEntity livingEntity : hashSet) {
            if (livingEntity.m_20194_() != minecraftServer) continue;
            NativeIronCastObservation.clear(livingEntity);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static Selection selection(LivingEntity livingEntity) {
        Map<LivingEntity, Selection> map = SELECTIONS;
        synchronized (map) {
            return SELECTIONS.get(livingEntity);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static Cast last(LivingEntity livingEntity) {
        Map<LivingEntity, Cast> map = LAST;
        synchronized (map) {
            return LAST.get(livingEntity);
        }
    }

    public record Selection(String triggerItemId, String selectedSlot, int selectedIndex, String spellId, int selectedLevel, String source, int triggerContainerSlots, String authorization) {
    }

    public record Cast(String actualItemId, String physicalKind, String source, String actualSlot, String spellId, int effectiveLevel, int effectiveTicks, int manaBeforeEvent, int manaAfterEvent, String phase, long callbacks, String authorization) {
    }

    private record Trigger(ItemStack stack, InteractionHand hand, String rules) {
    }

    public record Attempt(String actualItemId, String spellId, int effectiveLevel, String source, String slot, boolean nativeAccepted) {
    }
}

