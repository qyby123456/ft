package net.farmingtales.ftboss.integ;

import io.redspace.ironsspellbooks.api.events.SpellOnCastEvent;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.magic.SpellSelectionManager.SelectionOption;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.CastSource;
import io.redspace.ironsspellbooks.api.spells.ISpellContainer;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.Map.Entry;
import net.farmingtales.ftboss.item.FoodWeaponRanged;
import net.farmingtales.ftboss.item.FourWeaponFoundationAdapter;
import net.farmingtales.ftboss.item.NativeActionObservation;
import net.farmingtales.ftboss.item.NativeWeaponTypes;
import net.farmingtales.ftboss.item.NativeActionObservation.Action;
import net.farmingtales.ftboss.item.NativeActionObservation.Invocation;
import net.farmingtales.ftboss.transaction.FoundationApprovedRules;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.IEventBus;

public final class NativeIronCastObservation {
    private static final Map<LivingEntity, NativeIronCastObservation.Selection> SELECTIONS = new WeakHashMap<>();
    private static final Map<MagicData, NativeIronCastObservation.Cast> CASTS = new WeakHashMap<>();
    private static final Map<LivingEntity, NativeIronCastObservation.Cast> LAST = new WeakHashMap<>();
    private static final Map<MagicData, NativeIronCastObservation.Trigger> TRIGGERS = new WeakHashMap<>();
    private static final Map<MagicData, LivingEntity> CASTERS = new WeakHashMap<>();
    private static final Map<LivingEntity, NativeIronCastObservation.Attempt> ATTEMPTS = new WeakHashMap<>();

    private NativeIronCastObservation() {
    }

    public static void selection(LivingEntity var0, SelectionOption var1) {
        if (var0 != null && !var0.level().isClientSide) {
            synchronized (SELECTIONS) {
                SELECTIONS.remove(var0);
            }

            if (var1 != null && var1.spellData != null) {
                try {
                    Invocation var11 = NativeActionObservation.current();
                    ItemStack var3 = var11 == null ? ItemStack.EMPTY : net.farmingtales.ftboss.item.NativeFoundationItemData.decode(var11.before());
                    ISpellContainer var4 = var3.isEmpty() ? null : ISpellContainer.get(var3.copy());
                    NativeIronCastObservation.Selection var5 = new NativeIronCastObservation.Selection(
                        var11 == null ? "" : var11.itemId(),
                        var1.slot,
                        var1.slotIndex,
                        var1.spellData.getSpell().getSpellId(),
                        var1.spellData.getLevel(),
                        var1.getCastSource().name(),
                        var4 == null ? 0 : var4.getActiveSpellCount(),
                        "SELECTED_OPTION_NOT_AUTHORITY"
                    );
                    synchronized (SELECTIONS) {
                        SELECTIONS.put(var0, var5);
                    }
                } catch (RuntimeException var9) {
                }
            }
        }
    }

    public static int effectiveTime(AbstractSpell var0, int var1, LivingEntity var2) {
        return var0.getEffectiveCastTime(var1, var2);
    }

    public static void bind(MagicData var0, AbstractSpell var1, LivingEntity var2, ItemStack var3, int var4, CastSource var5, String var6) {
        if (!var2.level().isClientSide) {
            try {
                String var7 = NativeWeaponTypes.staff(var3)
                    ? "STAFF"
                    : (NativeWeaponTypes.types(var3.getItem().getClass()).contains("io.redspace.ironsspellbooks.api.item.IScroll") ? "SCROLL" : "NON_STAFF");
                NativeIronCastObservation.Cast var8 = new NativeIronCastObservation.Cast(
                    NativeWeaponTypes.id(var3),
                    var7,
                    var5.name(),
                    var6,
                    var1.getSpellId(),
                    var4,
                    var0.getCastDurationRemaining(),
                    0,
                    0,
                    "STARTED",
                    0L,
                    "NATIVE_ONLY_OWNER_RECEIPT_REQUIRED"
                );
                synchronized (CASTS) {
                    CASTS.put(var0, var8);
                }

                Invocation var20;
                String var10000;
                label65: {
                    var20 = NativeActionObservation.current();
                    if (var2 instanceof ServerPlayer var11 && FourWeaponFoundationAdapter.applies(var3)) {
                        var10000 = new FoundationApprovedRules().fingerprint(var11, var3);
                        break label65;
                    }

                    var10000 = "";
                }

                String var10 = var10000;
                synchronized (TRIGGERS) {
                    TRIGGERS.put(var0, new NativeIronCastObservation.Trigger(var3.copy(), var20 == null ? null : var20.hand(), var10));
                }

                synchronized (CASTERS) {
                    CASTERS.put(var0, var2);
                }

                synchronized (LAST) {
                    LAST.put(var2, var8);
                }
            } catch (RuntimeException var19) {
                clear(var0);
            }
        }
    }

    public static boolean postCost(IEventBus var0, Event var1, LivingEntity var2) {
        int var3 = var1 instanceof SpellOnCastEvent var4 ? var4.getManaCost() : 0;
        var0.post(var1);
        boolean var6 = var1 instanceof net.neoforged.bus.api.ICancellableEvent cancellable && cancellable.isCanceled();
        if (var1 instanceof SpellOnCastEvent var5) {
            update(MagicData.getPlayerMagicData(var2), var2, "COST_AFTER_NATIVE_BUS", var3, var5.getManaCost(), var5.getSpellLevel());
        }

        return var6;
    }

    private static void update(MagicData var0, LivingEntity var1, String var2, Integer var3, Integer var4) {
        update(var0, var1, var2, var3, var4, null);
    }

    private static void update(MagicData var0, LivingEntity var1, String var2, Integer var3, Integer var4, Integer var5) {
        try {
            synchronized (CASTS) {
                NativeIronCastObservation.Cast var6 = CASTS.get(var0);
                if (var6 == null || !var6.spellId().equals(var0.getCastingSpellId()) || !var6.source().equals(var0.getCastSource().name())) {
                    return;
                }

                NativeIronCastObservation.Cast var8 = new NativeIronCastObservation.Cast(
                    var6.actualItemId(),
                    var6.physicalKind(),
                    var6.source(),
                    var6.actualSlot(),
                    var6.spellId(),
                    var5 == null ? var6.effectiveLevel() : var5,
                    var6.effectiveTicks(),
                    var3 == null ? var6.manaBeforeEvent() : var3,
                    var4 == null ? var6.manaAfterEvent() : var4,
                    var2,
                    var6.callbacks() + 1L,
                    var6.authorization()
                );
                CASTS.put(var0, var8);
                synchronized (LAST) {
                    LAST.put(var1, var8);
                }
            }
        } catch (RuntimeException var14) {
        }
    }

    public static void callback(MagicData var0, LivingEntity var1, String var2, boolean var3, Runnable var4) {
        NativeIronCastObservation.Cast var5;
        synchronized (CASTS) {
            var5 = CASTS.get(var0);
        }

        boolean var26 = false;

        try {
            NativeIronCastObservation.Trigger var7;
            synchronized (TRIGGERS) {
                var7 = TRIGGERS.getOrDefault(var0, new NativeIronCastObservation.Trigger(ItemStack.EMPTY, null, ""));
            }

            if (var1 instanceof ServerPlayer var27 && !var7.stack().isEmpty() && FourWeaponFoundationAdapter.applies(var7.stack())) {
                try {
                    String var9 = new FoundationApprovedRules().fingerprint(var27, var7.stack());
                    boolean var10 = var7.hand() != null
                        && net.farmingtales.ftboss.item.NativeFoundationItemData.image(var27.getItemInHand(var7.hand())).equals(net.farmingtales.ftboss.item.NativeFoundationItemData.image(var7.stack()));
                    if (!var10 || var9 == null || !var9.equals(var7.rules())) {
                        clear(var0);
                        var7 = new NativeIronCastObservation.Trigger(ItemStack.EMPTY, null, "");
                    }
                } catch (RuntimeException var24) {
                    clear(var0);
                    var7 = new NativeIronCastObservation.Trigger(ItemStack.EMPTY, null, "");
                }
            }

            NativeActionObservation.invoke(var2.equals("SUCCESSFUL_ON_CAST") ? Action.CAST : Action.CALLBACK, var7.stack().copy(), var1, var7.hand(), () -> {
                var4.run();
                return null;
            });
            var26 = true;
            update(var0, var1, var2, null, null);
        } finally {
            if (var3) {
                if (var5 != null) {
                    synchronized (LAST) {
                        LAST.put(
                            var1,
                            new NativeIronCastObservation.Cast(
                                var5.actualItemId(),
                                var5.physicalKind(),
                                var5.source(),
                                var5.actualSlot(),
                                var5.spellId(),
                                var5.effectiveLevel(),
                                var5.effectiveTicks(),
                                var5.manaBeforeEvent(),
                                var5.manaAfterEvent(),
                                var26 ? var2 : "THREW_" + var2,
                                var5.callbacks() + 1L,
                                var5.authorization()
                            )
                        );
                    }
                }

                clear(var0);
            }
        }
    }

    public static void cast(AbstractSpell var0, Level var1, int var2, LivingEntity var3, CastSource var4, MagicData var5) {
        callback(var5, var3, "SUCCESSFUL_ON_CAST", false, () -> FoodWeaponRanged.withSource(null, new CompoundTag(), () -> {
                var0.onCast(var1, var2, var3, var4, var5);
                return null;
            }));
    }

    public static void attemptReturned(AbstractSpell var0, ItemStack var1, int var2, LivingEntity var3, CastSource var4, String var5, boolean var6) {
        if (var3 != null && !var3.level().isClientSide) {
            try {
                synchronized (ATTEMPTS) {
                    ATTEMPTS.put(var3, new NativeIronCastObservation.Attempt(NativeWeaponTypes.id(var1), var0.getSpellId(), var2, var4.name(), var5, var6));
                }
            } catch (RuntimeException var10) {
            }
        }
    }

    public static NativeIronCastObservation.Attempt attempt(LivingEntity var0) {
        synchronized (ATTEMPTS) {
            return ATTEMPTS.get(var0);
        }
    }

    public static void clear(MagicData var0) {
        synchronized (CASTS) {
            CASTS.remove(var0);
        }

        synchronized (TRIGGERS) {
            TRIGGERS.remove(var0);
        }

        LivingEntity var9;
        synchronized (CASTERS) {
            var9 = CASTERS.remove(var0);
        }

        if (var9 != null) {
            clearDiagnostics(var9);
        }
    }

    private static void clearDiagnostics(LivingEntity var0) {
        synchronized (SELECTIONS) {
            SELECTIONS.remove(var0);
        }

        synchronized (LAST) {
            LAST.remove(var0);
        }

        synchronized (ATTEMPTS) {
            ATTEMPTS.remove(var0);
        }
    }

    public static void clear(LivingEntity var0) {
        List<MagicData> var1;
        synchronized (CASTERS) {
            var1 = CASTERS.entrySet().stream().filter(var1x -> var1x.getValue() == var0).map(Entry::getKey).toList();
        }

        for (MagicData var3 : var1) {
            clear(var3);
        }

        clearDiagnostics(var0);
    }

    public static void clearServer(MinecraftServer var0) {
        HashSet<LivingEntity> var1;
        synchronized (CASTERS) {
            var1 = new HashSet<>(CASTERS.values());
        }

        for (LivingEntity var3 : var1) {
            if (var3.getServer() == var0) {
                clear(var3);
            }
        }
    }

    public static NativeIronCastObservation.Selection selection(LivingEntity var0) {
        synchronized (SELECTIONS) {
            return SELECTIONS.get(var0);
        }
    }

    public static NativeIronCastObservation.Cast last(LivingEntity var0) {
        synchronized (LAST) {
            return LAST.get(var0);
        }
    }

    public static record Attempt(String actualItemId, String spellId, int effectiveLevel, String source, String slot, boolean nativeAccepted) {
    }

    public static record Cast(
        String actualItemId,
        String physicalKind,
        String source,
        String actualSlot,
        String spellId,
        int effectiveLevel,
        int effectiveTicks,
        int manaBeforeEvent,
        int manaAfterEvent,
        String phase,
        long callbacks,
        String authorization
    ) {
    }

    public static record Selection(
        String triggerItemId,
        String selectedSlot,
        int selectedIndex,
        String spellId,
        int selectedLevel,
        String source,
        int triggerContainerSlots,
        String authorization
    ) {
    }

    private static record Trigger(ItemStack stack, InteractionHand hand, String rules) {
    }
}
