package net.farmingtales.ftboss.item;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.StackWalker.Option;
import java.lang.StackWalker.StackFrame;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.HexFormat;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.TreeSet;
import java.util.UUID;
import java.util.WeakHashMap;
import java.util.function.Consumer;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ProjectileWeaponItem;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import net.farmingtales.ftboss.integ.FoodWeaponTridentCarrierReader;
import net.farmingtales.ftboss.item.FoodWeaponEchoQueue.Entry;
import net.farmingtales.ftboss.item.FoodWeaponEchoQueue.Key;
import net.farmingtales.ftboss.item.FoodWeaponEvolution.Profile;
import net.farmingtales.ftboss.item.FoodWeaponExpectedTrace.Value;
import net.farmingtales.ftboss.item.NativeEvergaleBridge.RawAction;
import net.farmingtales.ftboss.item.NativeEvergaleBridge.RawCooldown;
import net.farmingtales.ftboss.item.NativeEvergaleBridge.RawOwnerAssignment;
import net.farmingtales.ftboss.item.NativeEvergaleBridge.RawSpawn;
import net.farmingtales.ftboss.item.NativeEvergaleBridge.Site;
import net.farmingtales.ftboss.item.NativeExpectedFieldViews.Stack;
import net.farmingtales.ftboss.skill.WeaponEvolutionSkills;
import net.farmingtales.ftboss.transaction.FoundationApprovedRules;
import net.farmingtales.ftboss.util.FoodSocketAttributeRules;
import net.farmingtales.ftboss.util.FoodWeaponBuildRules;
import net.farmingtales.ftboss.util.FoodWeaponLifecycleRules;
import net.farmingtales.ftboss.util.FoodWeaponBuildRules.Part;
import net.farmingtales.ftboss.util.FoodWeaponBuildRules.Wear;
import net.farmingtales.ftboss.util.FoodWeaponFamilyRules.Kind;
import net.farmingtales.ftboss.util.WeaponEvolutionSkillPool.Aspect;
import net.minecraft.nbt.ByteArrayTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.LongArrayTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.NumericTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.SpectralArrow;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpectralArrowItem;
import net.minecraft.world.item.TridentItem;
import net.minecraft.world.level.Level;
import net.minecraft.core.registries.BuiltInRegistries;

public final class FoodWeaponSourceLedger {
    public static final int VERSION = 3;
    public static final String IDENTITY_ROOT = "FTBossWeaponLifecycle";
    private static final int MAX_CLAIMS = 4096;
    private static final int MAX_CANONICAL_DEPTH = 64;
    private static final int MAX_CANONICAL_NODES = 65536;
    private static final int MAX_CANONICAL_BYTES = 1048576;
    private static final long SOURCE_LIFETIME_TICKS = 1200L;
    private static final int MAX_ACTIVE_ROOT_PROJECTIONS = 4096;
    private static final FoodWeaponSourceLedger.InteractionRoot[] ACTIVE_ROOT_PROJECTIONS = new FoodWeaponSourceLedger.InteractionRoot[4096];
    private static final IdentityHashMap<RawAction, FoodWeaponSourceLedger.NativeEvergaleAction> EVERGALE_ACTIONS = new IdentityHashMap<>();
    private static final IdentityHashMap<NativeEvergaleImpact.RawImpact, FoodWeaponSourceLedger.NativeEvergaleInteraction> EVERGALE_IMPACTS = new IdentityHashMap<>();
    private static final int EVERGALE_SCOPE_LIMIT = 4096;
    private static long ownerFieldGeneration;
    private static final Map<MinecraftServer, FoodWeaponSourceLedger.ServerState> SERVERS = Collections.synchronizedMap(new WeakHashMap<>());
    private static final ThreadLocal<FoodWeaponSourceLedger.TicketState> TRIDENT_TICKET = new ThreadLocal<>();
    private static final ThreadLocal<FoodWeaponSourceLedger.NativeOperation> NATIVE_OPERATION = new ThreadLocal<>();
    private static final ThreadLocal<Integer> NATIVE_DEPTH = ThreadLocal.withInitial(() -> 0);
    private static final StackWalker NATIVE_CALLER = StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE);
    private static final String NATIVE_PURPOSE = "NATIVE_PROJECTILE";
    private static final String NATIVE_HURT_PURPOSE = "NATIVE_CARRIER_HURT";
    private static final String MEAT_HURT_PURPOSE = "NATIVE_MEAT_ACTUAL_HURT";
    private static final String IRON_HURT_PURPOSE = "NATIVE_IRON_ACTUAL_HURT";
    private static final ThreadLocal<FoodWeaponSourceLedger.NativeIronCallbackToken> PRIVATE_IRON_CALLBACK = new ThreadLocal<>();
    private static final ThreadLocal<FoodWeaponSourceLedger.NativeHurtScope> PRIVATE_NATIVE_HURT = new ThreadLocal<>();
    private static final ThreadLocal<FoodWeaponSourceLedger.NativeIronUseToken> PRIVATE_IRON_USE = new ThreadLocal<>();
    private static final ThreadLocal<FoodWeaponSourceLedger.NativeIngressToken> PRIVATE_INGRESS = new ThreadLocal<>();
    private static final ThreadLocal<FoodWeaponSourceLedger.NativeWearToken> PRIVATE_INGRESS_WEAR = new ThreadLocal<>();
    private static final ThreadLocal<FoodWeaponSourceLedger.TridentTicketBefore> TRIDENT_BEFORE = new ThreadLocal<>();
    private static final int MAX_INTERACTION_DEPTH = 16;
    private static final float PENDING_ECHO_RAW_AMOUNT = 2.0F;

    private static boolean retireOriginalIssuerRoot(FoodWeaponSourceLedger.NativeIssueWindow var0) {
        if (!var0.unchanged()) {
            return false;
        } else {
            FoodWeaponSourceLedger.InteractionRoot var1 = var0.projection;
            if (var1 != null) {
                retireInteraction(var1);
                if (var1.ownerState.current == var1) {
                    setOwnerInteraction(var1.ownerState, null);
                }
            }

            var0.originalRetired = true;
            return var0.unchanged();
        }
    }

    public static FoodWeaponSourceLedger.NativeIssuerCustody nativeIssuerCustody() {
        return new FoodWeaponSourceLedger.NativeIssuerCustody();
    }

    public static synchronized void nativeIssuerTransferred(FoodWeaponSourceLedger.NativeIssuerCustody var0, FoodWeaponSourceLedger.NativeIngressToken var1) {
        if (var0 != null && var0.ingress == var1) {
            var0.cut.transfer(var1);
        }
    }

    public static synchronized void nativeIssuerTransferred(FoodWeaponSourceLedger.NativeIssuerCustody var0, FoodWeaponSourceLedger.NativeIronUseToken var1) {
        if (var0 != null && var0.use == var1) {
            var0.cut.transfer(var1);
        }
    }

    public static synchronized void nativeIssuerTransferred(
        FoodWeaponSourceLedger.NativeIssuerCustody var0, FoodWeaponSourceLedger.NativeIronCallbackToken var1
    ) {
        if (var0 != null && var0.callback == var1) {
            var0.cut.transfer(var1);
        }
    }

    public static synchronized void nativeIssuerTransferred(FoodWeaponSourceLedger.NativeIssuerCustody var0, FoodWeaponSourceLedger.NativeWearToken var1) {
        if (var0 != null && var0.wear == var1) {
            var0.cut.transfer(var1);
        }
    }

    public static synchronized void nativeIssuerTransferred(FoodWeaponSourceLedger.NativeIssuerCustody var0, FoodWeaponSourceLedger.NativeCarrierToken var1) {
        if (var0 != null && var0.carrier == var1) {
            var0.cut.transfer(var1);
        }
    }

    public static synchronized void nativeIssuerTransferred(FoodWeaponSourceLedger.NativeIssuerCustody var0, FoodWeaponSourceLedger.NativeCapabilityToken var1) {
        if (var0 != null && var0.capability == var1) {
            var0.cut.transfer(var1);
        }
    }

    private static void closeOwnedExpectation(FoodWeaponSourceLedger.InteractionRoot var0) {
        if (var0.ownsExpectation && var0.expectation != null) {
            var0.expectation.closed = true;
            var0.expectation.cursor.close();
        }
    }

    private static void revokeUnreturnedInteraction(FoodWeaponSourceLedger.InteractionRoot var0) {
        if (var0 != null && var0.thread == Thread.currentThread()) {
            removeRootProjection(var0);
            closeOwnedExpectation(var0);
            var0.retired = true;
            var0.closed = true;
            if (var0.writerPublication != null) {
                closeOwnerWriterPublication(var0.writerPublication);
            }

            FoodWeaponSourceLedger.OriginalInteractionBinding var1 = var0.current;

            for (int var2 = 16; var1 != null && var2 > 0; var2--) {
                var1.closed = true;
                var1 = var1.parent;
            }

            var0.current = null;
            if (var0.ownerState.current == var0) {
                var0.ownerState.interactionChange = var0;
                var0.ownerState.current = null;
            }
        }
    }

    private static void revokeUnreturnedHurt(FoodWeaponSourceLedger.NativeHurtScope var0) {
        if (var0 != null && var0.thread == Thread.currentThread()) {
            var0.closed = true;
            revokeUnreturnedInteraction(var0.interaction);
            if (PRIVATE_NATIVE_HURT.get() == var0) {
                PRIVATE_NATIVE_HURT.remove();
            }
        }
    }

    public static synchronized void closeNativeIssuerCustody(FoodWeaponSourceLedger.NativeIssuerCustody var0) {
        if (var0 != null && var0.cut.close()) {
            if (var0.ingress != null) {
                FoodWeaponSourceLedger.NativeIngressToken var1 = var0.ingress;
                var1.invalid = true;
                var1.expectation.closed = true;
                var1.expectation.cursor.close();
                FoodWeaponSourceLedger.NativeHurtScope var2 = PRIVATE_NATIVE_HURT.get();
                if (var2 != null && (var2.earlyIngress == var1 || var2.receipt != null && var2.receipt.ingress == var1)) {
                    revokeUnreturnedHurt(var2);
                }

                if (PRIVATE_INGRESS.get() == var1) {
                    PRIVATE_INGRESS.remove();
                }

                FoodWeaponSourceLedger.NativeWearToken var3 = PRIVATE_INGRESS_WEAR.get();
                if (var3 != null && var3.ingress == var1) {
                    PRIVATE_INGRESS_WEAR.remove();
                }
            }

            if (var0.use != null) {
                var0.use.invalid = true;
                if (PRIVATE_IRON_USE.get() == var0.use) {
                    PRIVATE_IRON_USE.remove();
                }
            }

            if (var0.callback != null) {
                var0.callback.closed = true;
                var0.callback.expectation.closed = true;
                var0.callback.expectation.cursor.close();
                FoodWeaponSourceLedger.NativeHurtScope var4 = PRIVATE_NATIVE_HURT.get();
                if (var4 != null && var4.ironCallback == var0.callback) {
                    revokeUnreturnedHurt(var4);
                }

                if (PRIVATE_IRON_CALLBACK.get() == var0.callback) {
                    PRIVATE_IRON_CALLBACK.remove();
                }
            }

            if (var0.carrier != null) {
                var0.carrier.actor = false;
                var0.carrier.added = false;
                var0.carrier.image = null;
            }

            if (var0.capability != null) {
                var0.capability.canceled = true;
            }

            if (var0.wear != null) {
                var0.wear.ingress.invalid = true;
                if (PRIVATE_INGRESS_WEAR.get() == var0.wear) {
                    PRIVATE_INGRESS_WEAR.remove();
                }
            }
        }
    }

    private static void closeNativeFailurePublication(FoodWeaponSourceLedger.OwnerWriterPublication var0, FoodWeaponSourceLedger.InteractionRoot var1) {
        if (var0 != null && var0.binding.root == var1) {
            var0.closed = true;
            if (var0.selection != null) {
                var0.selection.revoke();
            }

            var0.observation.close();
            FoodWeaponSourceLedger.OwnerWriterStep var2 = var0.expectation.active;
            if (var2 != null && var2.publication == var0) {
                var2.closed = true;
                var0.expectation.active = null;
            }

            FoodWeaponSourceLedger.OwnerPlayerWriter var3 = var0.playerWriter;
            if (var3 != null && var3.publication == var0) {
                var3.closed = true;
                var3.cursor.close();
                if (var3.active != null) {
                    var3.active.closed = true;
                }

                var3.active = null;
            }

            FoodWeaponSourceLedger.OwnerRepairObservation var4 = var0.repairObservation;
            if (var4 != null && var4.publication == var0) {
                var4.closed = true;
                var4.trace.close();
                var4.observation.close();
            }
        }
    }

    private static void closeNativeFailureRoot(
        FoodWeaponSourceLedger.InteractionRoot var0,
        FoodWeaponSourceLedger.WeaponExpectation var1,
        FoodWeaponSourceLedger.OwnerWriterPublication var2,
        FoodWeaponSourceLedger.OriginalInteractionBinding var3
    ) {
        if (var0 != null) {
            var0.closed = true;
            var0.retired = true;
            var0.readCut.close();
            var0.interactionChange = var0;
            if (var1 != null && var0.expectation == var1 && var0.ownsExpectation) {
                var1.closed = true;
                var1.cursor.close();
            }

            closeNativeFailurePublication(var2, var0);
            if (var0.writerPublication != var2) {
                closeNativeFailurePublication(var0.writerPublication, var0);
            }

            FoodWeaponSourceLedger.OriginalInteractionBinding var4 = var0.current;

            for (int var5 = 16; var4 != null && var5 > 0 && var4.root == var0; var5--) {
                var4.closed = true;
                var4 = var4.parent;
            }

            if (var3 != null && var3.root == var0) {
                var3.closed = true;
            }

            var0.current = null;
            removeRootProjection(var0);
            if (var0.ownerState.current == var0) {
                var0.ownerState.interactionChange = var0;
                var0.ownerState.current = null;
            }
        }
    }

    private static void closeNativeObservationRoots(FoodWeaponSourceLedger.NativeObservationCut var0) {
        for (FoodWeaponSourceLedger.NativeRootObservation var1 = var0.roots; var1 != null; var1 = var1.nextCut) {
            closeNativeFailureRoot(var1.root, var1.expectation, var1.publication, var1.binding);
        }
    }

    public static synchronized FoodWeaponSourceLedger.NativeObservationCut beginNativeObservation(Object var0) {
        if (var0 == null) {
            return null;
        } else {
            FoodWeaponSourceLedger.NativeObservationCut var1 = new FoodWeaponSourceLedger.NativeObservationCut(var0, null);

            for (int var2 = 0; var2 < 4096; var2++) {
                FoodWeaponSourceLedger.InteractionRoot var3 = ACTIVE_ROOT_PROJECTIONS[var2];
                if (rootReadReceiver(var3, var0)) {
                    boolean var4 = false;

                    for (FoodWeaponSourceLedger.NativeRootObservation var5 = var3.nativeObservations; var5 != null; var5 = var5.nextRoot) {
                        if (var5.cut.receiver == var0) {
                            var4 = true;
                            break;
                        }
                    }

                    FoodWeaponSourceLedger.NativeRootObservation var6 = new FoodWeaponSourceLedger.NativeRootObservation(var1, var3);
                    var1.roots = var6;
                    if (!var4) {
                        var3.nativeObservations = var6;
                    }
                }
            }

            return var1;
        }
    }

    public static synchronized void bindNativeObservationFrame(FoodWeaponSourceLedger.NativeObservationCut var0, Object var1, NativeObservationFrame var2) {
        if (var0 != null && var0.receiver == var1) {
            if (!var0.frameBound && var2 != null && var2.receiver() == var1 && var2.context() == var1) {
                var0.frameBound = true;
                var0.frame = var2;
                if (!(var1 instanceof NativeObservationCarrier) || var2.failed() || var0.failure.failed) {
                    closeNativeObservationRoots(var0);
                    var0.failure.fail(var2, 0, var2.observationFailure(0));
                }
            } else {
                closeNativeObservationRoots(var0);
                var0.failure.fail(var2, -1, null);
            }
        }
    }

    public static synchronized FoodWeaponSourceLedger.NativeObservationCut beginNativeObservation(Object var0, NativeObservationFrame var1) {
        FoodWeaponSourceLedger.NativeObservationCut var2 = beginNativeObservation(var0);
        bindNativeObservationFrame(var2, var0, var1);
        return var2;
    }

    public static synchronized void nativeObservationFailed(
        FoodWeaponSourceLedger.NativeObservationCut var0, Object var1, NativeObservationFrame var2, int var3, Throwable var4
    ) {
        if (var0 != null && var0.receiver == var1 && (var2 == null || var0.frame == var2)) {
            closeNativeObservationRoots(var0);
            var0.failure.fail(var2, var3, var4);
        }
    }

    public static synchronized void nativeObservationOriginalFailed(
        FoodWeaponSourceLedger.NativeObservationCut var0, Object var1, NativeObservationFrame var2, Throwable var3
    ) {
        if (var0 != null && var0.receiver == var1 && (var2 == null || var0.frame == var2)) {
            closeNativeObservationRoots(var0);
            var0.failure.failures.original(var3);
        }
    }

    public static synchronized Throwable nativeObservationFailure(FoodWeaponSourceLedger.NativeObservationCut var0, int var1) {
        return var0 == null ? null : var0.failure.failures.failure(var1);
    }

    private static boolean nativeObservationReadLive(FoodWeaponSourceLedger.InteractionRoot var0) {
        for (FoodWeaponSourceLedger.NativeRootObservation var1 = var0.nativeObservations; var1 != null; var1 = var1.nextRoot) {
            if (var1.cut.failure.failed || var1.cut.frame == null || var1.cut.frame.failed()) {
                closeNativeFailureRoot(var0, var1.expectation, var1.publication, var1.binding);
                return false;
            }
        }

        FoodWeaponSourceLedger.NativeProjectileObservation var2 = var0.nativeProjectileObservation;
        if (var2 == null || !var2.failure.failed && (var2.frame == null || !var2.frame.failed())) {
            return !var0.closed && !var0.retired && !var0.readCut.closed && (var0.expectation == null || !var0.expectation.closed);
        } else {
            closeNativeFailureRoot(var0, var2.expectation, var2.publication, var2.binding);
            return false;
        }
    }

    static synchronized FoodWeaponSourceLedger.NativeProjectileObservation observeNativeProjectileImpact(NativeVanillaImpact.RawImpact var0) {
        return var0 == null ? null : new FoodWeaponSourceLedger.NativeProjectileObservation(var0);
    }

    private static void closeNativeProjectileObservation(FoodWeaponSourceLedger.NativeProjectileObservation var0) {
        if (var0 != null) {
            closeNativeFailureRoot(var0.root, var0.expectation, var0.publication, var0.binding);
            if (var0.scope != null) {
                var0.scope.closed = true;
            }
        }
    }

    static synchronized void nativeProjectileObservationFailed(
        FoodWeaponSourceLedger.NativeProjectileObservation var0,
        NativeVanillaImpact.RawImpact var1,
        FoodWeaponSourceLedger.NativeProjectileInteraction var2,
        NativeObservationFrame var3,
        int var4,
        Throwable var5
    ) {
        if (var0 != null && var0.call == var1 && (var2 == null || var0.scope == var2)) {
            closeNativeProjectileObservation(var0);
            if (var3 != null && var3.receiver() == var1.carrier && var3.context() == var1) {
                var0.frame = var3;
                var0.failure.failures.original(var3.originalFailure());
            }

            var0.failure.fail(var3, var4, var5);
        }
    }

    static synchronized void nativeProjectileOriginalFailed(
        FoodWeaponSourceLedger.NativeProjectileObservation var0, NativeVanillaImpact.RawImpact var1, Throwable var2
    ) {
        if (var0 != null && var0.call == var1) {
            closeNativeProjectileObservation(var0);
            var0.failure.fail(var1, -1, null);
            var0.failure.failures.original(var2);
        }
    }

    static synchronized Throwable nativeProjectileObservationFailure(FoodWeaponSourceLedger.NativeProjectileObservation var0, int var1) {
        if (var0 == null) {
            return null;
        } else {
            return var1 == 0 && var0.openingFailure != null
                ? var0.openingFailure
                : (var1 == 3 && var0.closingFailure != null ? var0.closingFailure : var0.failure.failures.failure(var1));
        }
    }

    static synchronized Throwable nativeProjectileOriginalFailure(FoodWeaponSourceLedger.NativeProjectileObservation var0) {
        return var0 == null ? null : var0.failure.failures.original;
    }

    static synchronized NativeObservationFrame nativeProjectileObservationFrame(FoodWeaponSourceLedger.NativeProjectileObservation var0) {
        return var0 == null ? null : var0.frame;
    }

    private static CompoundTag wolfControllerValues(CompoundTag var0, UUID var1, UUID var2, CompoundTag var3, long var4) {
        CompoundTag var6 = new CompoundTag();
        var6.putUUID("PackToken", var2);
        var6.putUUID("PackWeapon", var1);
        var6.putUUID("PackWeaponId", var0.getUUID("WeaponId"));
        var6.putString("PackHand", var0.getString("OriginalHand"));
        var6.putString("PackDimension", var0.getString("Dimension"));
        var6.put("PackFingerprint", var3.copy());
        var6.put("PackSource", var0.copy());
        var6.putLong("PackUntil", var4 + 600L);
        var6.putLong("PackCooldown", var4 + 400L);
        return var6;
    }

    private static CompoundTag playerWriterActualImage(FoodWeaponSourceLedger.OwnerPlayerWriter var0) {
        CompoundTag var1 = new CompoundTag();
        var1.put("Persistent", var0.persistent.copy());
        var1.put("Target", var0.target.copy());
        var1.put("Source", var0.source.copy());
        return var1;
    }

    private static boolean ownerPlayerWriterLive(FoodWeaponSourceLedger.OwnerPlayerWriter var0) {
        return var0 != null
            && !var0.closed
            && var0.publication.playerWriter == var0
            && var0.transition == var0.publication.binding.root.interactionChange
            && var0.selection == var0.publication.selection
            && ownerWriterSelectionLive(var0.publication)
            && var0.selection == var0.publication.selection
            && !var0.closed
            && var0.publication.playerWriter == var0
            && var0.publication.expectation.active == null;
    }

    static FoodWeaponSourceLedger.OwnerPlayerWriter beginOwnerRangedWriter(
        FoodWeaponSourceLedger.OwnerWriterPublication var0, Object var1, Object var2, CompoundTag var3, CompoundTag var4, CompoundTag var5
    ) {
        FoodWeaponSourceLedger.WriterPlanningCut var6 = writerPlanningCut(var0);
        if (writerCaller(FoodWeaponRanged.class)
            && ownerWriterSelectionLive(var0)
            && writerPlanningUnchanged(var0, var6)
            && var0.commit == var1
            && var0.leaf == var2
            && var0.playerWriter == null
            && var0.expectation.active == null
            && var0.repairObservation == null
            && var3 != null
            && var4 != null
            && var5 != null) {
            Object var7 = var0.binding.root.interactionChange;
            Tag var8 = var3.get("FTBossRangedProcTimers");
            boolean var9 = var8 instanceof CompoundTag;
            if (var9 ? var8 == var4 : var4.isEmpty()) {
                FoodWeaponSourceLedger.PlayerImagePlan var10 = new FoodWeaponSourceLedger.PlayerImagePlan(
                    false, var3, var4, var5, var9, var5 == var4, var5 == var3, null
                );
                if (!canonicalInputValid(var10.image())) {
                    return null;
                } else {
                    FoodWeaponSourceLedger.OwnerPlayerWriter var11 = new FoodWeaponSourceLedger.OwnerPlayerWriter(var0, var3, var4, var5, var10, var6.selection);
                    if (ownerWriterSelectionLive(var0)
                        && writerPlanningUnchanged(var0, var6)
                        && var11.selection == var6.selection
                        && var7 == var0.binding.root.interactionChange
                        && var0.playerWriter == null
                        && var0.expectation.active == null
                        && var0.repairObservation == null) {
                        var0.playerWriter = var11;
                        return var11;
                    } else {
                        return null;
                    }
                }
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    static FoodWeaponSourceLedger.OwnerPlayerWriter beginOwnerPackController(
        FoodWeaponSourceLedger.OwnerWriterPublication var0,
        FoodSocketPowers.PreparedPack var1,
        FoodWeaponFinalDamage.PublishingCleanup var2,
        CompoundTag var3,
        CompoundTag var4,
        UUID var5,
        UUID var6,
        CompoundTag var7
    ) {
        FoodWeaponSourceLedger.WriterPlanningCut var8 = writerPlanningCut(var0);
        if (writerCaller(FoodSocketPowers.class)
            && ownerWriterSelectionLive(var0)
            && writerPlanningUnchanged(var0, var8)
            && var0.playerWriter == null
            && var0.expectation.active == null
            && var0.repairObservation == null
            && var3 != null
            && var4 != null
            && var5 != null
            && var6 != null
            && var7 != null
            && FoodWeaponFinalDamage.matchesPackPublishing(var1, var0.receipt, var2)
            && FoodSocketPowers.matchesOwnerPackParameters(
                var1, var2, var0.receipt, var3, var4, var5, var6, var7, var0.binding.snapshot, var0.binding.root.tick
            )) {
            Object var9 = var0.binding.root.interactionChange;
            if (!var3.getCompound("FTBossFoodSocketPowers").equals(var4)) {
                return null;
            } else {
                CompoundTag var10 = wolfControllerValues(var0.binding.snapshot, var5, var6, var7, var0.binding.root.tick);
                FoodWeaponSourceLedger.PlayerImagePlan var11 = new FoodWeaponSourceLedger.PlayerImagePlan(true, var3, var4, var4, false, true, false, var10);
                if (canonicalInputValid(var11.image()) && canonicalInputValid(var10)) {
                    FoodWeaponSourceLedger.OwnerPlayerWriter var12 = new FoodWeaponSourceLedger.OwnerPlayerWriter(var0, var3, var4, var4, var11, var8.selection);
                    if (ownerWriterSelectionLive(var0)
                        && writerPlanningUnchanged(var0, var8)
                        && var12.selection == var8.selection
                        && var9 == var0.binding.root.interactionChange
                        && FoodWeaponFinalDamage.matchesPackPublishing(var1, var0.receipt, var2)
                        && writerPlanningUnchanged(var0, var8)
                        && var0.playerWriter == null
                        && var0.expectation.active == null
                        && var0.repairObservation == null) {
                        var12.packPrepared = var1;
                        var12.packCleanup = var2;
                        var0.playerWriter = var12;
                        return var12;
                    } else {
                        return null;
                    }
                } else {
                    return null;
                }
            }
        } else {
            return null;
        }
    }

    static FoodWeaponSourceLedger.OwnerPlayerStep planOwnerPlayerKey(FoodWeaponSourceLedger.OwnerPlayerWriter var0, int var1, String var2, Tag var3) {
        return planOwnerPlayerArgument(
            var0,
            var1,
            var0 == null ? null : var0.target,
            var2,
            new FoodWeaponSourceLedger.PlayerWriterArgument(FoodWeaponSourceLedger.PlayerWriterArgumentKind.TAG, var3, null, null, 0L)
        );
    }

    static FoodWeaponSourceLedger.OwnerPlayerStep planOwnerPlayerUuidKey(
        FoodWeaponSourceLedger.OwnerPlayerWriter var0, int var1, CompoundTag var2, String var3, UUID var4
    ) {
        return planOwnerPlayerArgument(
            var0, var1, var2, var3, new FoodWeaponSourceLedger.PlayerWriterArgument(FoodWeaponSourceLedger.PlayerWriterArgumentKind.UUID, null, var4, null, 0L)
        );
    }

    static FoodWeaponSourceLedger.OwnerPlayerStep planOwnerPlayerStringKey(
        FoodWeaponSourceLedger.OwnerPlayerWriter var0, int var1, CompoundTag var2, String var3, String var4
    ) {
        return planOwnerPlayerArgument(
            var0,
            var1,
            var2,
            var3,
            new FoodWeaponSourceLedger.PlayerWriterArgument(FoodWeaponSourceLedger.PlayerWriterArgumentKind.STRING, null, null, var4, 0L)
        );
    }

    static FoodWeaponSourceLedger.OwnerPlayerStep planOwnerPlayerLongKey(
        FoodWeaponSourceLedger.OwnerPlayerWriter var0, int var1, CompoundTag var2, String var3, long var4
    ) {
        return planOwnerPlayerArgument(
            var0,
            var1,
            var2,
            var3,
            new FoodWeaponSourceLedger.PlayerWriterArgument(FoodWeaponSourceLedger.PlayerWriterArgumentKind.LONG, null, null, null, var4)
        );
    }

    private static FoodWeaponSourceLedger.OwnerPlayerStep planOwnerPlayerArgument(
        FoodWeaponSourceLedger.OwnerPlayerWriter var0, int var1, CompoundTag var2, String var3, FoodWeaponSourceLedger.PlayerWriterArgument var4
    ) {
        FoodWeaponSourceLedger.WriterPlanningCut var5 = var0 == null ? null : writerPlanningCut(var0.publication);
        Object var6 = var0 == null ? null : var0.cursor.position();
        int var7 = var0 == null ? -1 : var0.plan.next;
        if (var0 != null
            && writerCaller(var0.plan.wolf ? FoodSocketPowers.class : FoodWeaponRanged.class)
            && ownerPlayerWriterLive(var0)
            && writerPlanningUnchanged(var0.publication, var5)
            && var0.selection == var5.selection
            && var0.active == null
            && var1 == var7
            && var2 == (var4.kind == FoodWeaponSourceLedger.PlayerWriterArgumentKind.ROOT ? var0.persistent : var0.target)
            && (var4.kind != FoodWeaponSourceLedger.PlayerWriterArgumentKind.ROOT || var4.tag == var0.target)) {
            if (!var0.cursor.matches(canonical(playerWriterActualImage(var0)))) {
                return null;
            } else {
                FoodWeaponSourceLedger.PlayerImageDelta var8 = var4.kind == FoodWeaponSourceLedger.PlayerWriterArgumentKind.ROOT
                    ? var0.plan.root(var1)
                    : var0.plan.key(var1, var3);
                if (var8 != null && var8.key.equals(var3) && var4.expected(var8.value)) {
                    FoodWeaponSourceLedger.OwnerPlayerStep var9 = new FoodWeaponSourceLedger.OwnerPlayerStep(var0, var8, var4, var2, var5.selection);
                    if (ownerPlayerWriterLive(var0)
                        && writerPlanningUnchanged(var0.publication, var5)
                        && var0.selection == var5.selection
                        && var9.selection == var5.selection
                        && var0.active == null
                        && var6 == var0.cursor.position()
                        && var7 == var0.plan.next) {
                        var0.active = var9;
                        return var9;
                    } else {
                        return null;
                    }
                } else {
                    return null;
                }
            }
        } else {
            return null;
        }
    }

    static FoodWeaponSourceLedger.OwnerPlayerStep planOwnerPlayerRoot(
        FoodWeaponSourceLedger.OwnerPlayerWriter var0, int var1, CompoundTag var2, CompoundTag var3
    ) {
        return planOwnerPlayerArgument(
            var0,
            var1,
            var2,
            var0 == null ? null : var0.plan.rootKey,
            new FoodWeaponSourceLedger.PlayerWriterArgument(FoodWeaponSourceLedger.PlayerWriterArgumentKind.ROOT, var3, null, null, 0L)
        );
    }

    public static void ownerPlayerPutEntered(FoodWeaponSourceLedger.OwnerPlayerStep var0, CompoundTag var1, String var2, Tag var3) {
        if (var0 != null && var0.argument.originalTag(var3) && var0.argument.expected(var0.delta.value)) {
            ownerPlayerArgumentEntered(var0, var1, var2);
        }
    }

    public static void ownerPlayerUuidEntered(FoodWeaponSourceLedger.OwnerPlayerStep var0, CompoundTag var1, String var2, UUID var3) {
        if (var0 != null && var0.argument.originalUuid(var3)) {
            ownerPlayerArgumentEntered(var0, var1, var2);
        }
    }

    public static void ownerPlayerStringEntered(FoodWeaponSourceLedger.OwnerPlayerStep var0, CompoundTag var1, String var2, String var3) {
        if (var0 != null && var0.argument.originalString(var3)) {
            ownerPlayerArgumentEntered(var0, var1, var2);
        }
    }

    public static void ownerPlayerLongEntered(FoodWeaponSourceLedger.OwnerPlayerStep var0, CompoundTag var1, String var2, long var3) {
        if (var0 != null && var0.argument.originalLong(var3)) {
            ownerPlayerArgumentEntered(var0, var1, var2);
        }
    }

    private static void ownerPlayerArgumentEntered(FoodWeaponSourceLedger.OwnerPlayerStep var0, CompoundTag var1, String var2) {
        if (var0 != null
            && !var0.closed
            && !var0.entered
            && var0.writer.active == var0
            && var0.selection == var0.writer.selection
            && ownerPlayerWriterLive(var0.writer)
            && var1 == var0.receiver
            && var1 == (var0.delta.rootWrite ? var0.writer.persistent : var0.writer.target)
            && var0.delta.key.equals(var2)) {
            byte[] var3 = canonical(playerWriterActualImage(var0.writer));
            if (ownerPlayerWriterLive(var0.writer)
                && var0.selection == var0.writer.selection
                && !var0.closed
                && var0.writer.active == var0
                && var1 == var0.receiver
                && var0.position == var0.writer.cursor.position()
                && var0.delta.ordinal == var0.writer.plan.next
                && var0.writer.cursor.matches(var3)) {
                var0.entered = true;
            }
        }
    }

    public static void ownerPlayerPutReturned(FoodWeaponSourceLedger.OwnerPlayerStep var0, CompoundTag var1) {
        if (var0 != null && !var0.closed) {
            boolean var2 = false;

            try {
                if (!var0.entered
                    || var0.selection != var0.writer.selection
                    || !ownerPlayerWriterLive(var0.writer)
                    || var0.writer.active != var0
                    || var1 != var0.receiver
                    || var1 != (var0.delta.rootWrite ? var0.writer.persistent : var0.writer.target)) {
                    return;
                }

                byte[] var3 = canonical(playerWriterActualImage(var0.writer));
                Tag var4 = var0.delta.rootWrite ? var0.writer.persistent.get(var0.writer.plan.rootKey) : null;
                if (var0.selection != var0.writer.selection
                    || !ownerPlayerWriterLive(var0.writer)
                    || var0.closed
                    || var0.writer.active != var0
                    || var0.delta.ordinal != var0.writer.plan.next
                    || var0.delta.rootWrite && var4 != var0.writer.target
                    || !var0.writer.cursor.advance(var0.position, var0.before, var0.planned, var3)) {
                    return;
                }

                var2 = var0.writer.plan.accept(var0.delta);
                if (var2) {
                    var0.writer.publication.binding.root.readCut.change();
                }
            } finally {
                var0.closed = true;
                if (var0.writer.active == var0) {
                    var0.writer.active = null;
                }

                if (!var2) {
                    closeOwnerPlayerWriter(var0.writer);
                }
            }
        }
    }

    public static synchronized void closeOwnerPlayerStep(FoodWeaponSourceLedger.OwnerPlayerStep var0) {
        if (var0 != null && !var0.closed) {
            var0.closed = true;
            if (var0.writer.active == var0) {
                var0.writer.active = null;
            }

            closeOwnerPlayerWriter(var0.writer);
        }
    }

    static void finishOwnerPackWriter(
        FoodWeaponSourceLedger.OwnerPlayerWriter var0, FoodSocketPowers.PreparedPack var1, FoodWeaponFinalDamage.PublishingCleanup var2
    ) {
        if (var0 != null
            && writerCaller(FoodSocketPowers.class)
            && var0.packPrepared == var1
            && var0.packCleanup == var2
            && FoodWeaponFinalDamage.matchesPackPublicationCleanup(var1, var2)) {
            closeOwnerPlayerWriter(var0);
        }
    }

    public static synchronized void closeOwnerPlayerWriter(FoodWeaponSourceLedger.OwnerPlayerWriter var0) {
        if (var0 != null && !var0.closed) {
            var0.closed = true;
            var0.cursor.close();
            if (var0.active != null) {
                var0.active.closed = true;
                var0.active = null;
            }

            if (!var0.plan.published && var0.publication.selection == var0.selection && var0.publication.playerWriter == var0) {
                var0.publication.closed = true;
            }
        }
    }

    static synchronized void ownerPublicationObservationFailed(FoodWeaponSourceLedger.OwnerWriterPublication var0, int var1, Throwable var2) {
        if (var0 != null && var0.thread == Thread.currentThread()) {
            var0.closed = true;
            if (var0.binding.root.writerPublication == var0 && var0.binding.root.expectation == var0.expectation) {
                var0.binding.root.readCut.change();
            }

            try {
                var0.observation.record(var1, var2);
            } finally {
                closeOwnerWriterPublication(var0);
            }
        }
    }

    static synchronized Throwable ownerPublicationObservationFailure(FoodWeaponSourceLedger.OwnerWriterPublication var0, int var1) {
        return var0 != null && var0.thread == Thread.currentThread() ? var0.observation.failure(var1) : null;
    }

    static synchronized void ownerPublicationOriginalFailed(FoodWeaponSourceLedger.OwnerWriterPublication var0, Throwable var1) {
        if (var0 != null && var0.thread == Thread.currentThread()) {
            var0.closed = true;
            if (var0.binding.root.writerPublication == var0 && var0.binding.root.expectation == var0.expectation) {
                var0.binding.root.readCut.change();
            }

            try {
                var0.observation.original(var1);
            } finally {
                closeOwnerWriterPublication(var0);
            }
        }
    }

    static synchronized Throwable ownerPublicationOriginalFailure(FoodWeaponSourceLedger.OwnerWriterPublication var0) {
        return var0 != null && var0.thread == Thread.currentThread() ? var0.observation.original : null;
    }

    private static void revokeOwnerWriterSelection(FoodWeaponSourceLedger.OwnerWriterPublication var0, FoodWeaponSourceLedger.WriterSelection var1) {
        if (var1 != null) {
            var1.revoke();
            if (var0.selection == var1) {
                var0.closed = true;
                if (var0.binding.root.writerPublication == var0 && var0.binding.root.expectation == var0.expectation) {
                    var0.binding.root.readCut.change();
                }

                FoodWeaponSourceLedger.OwnerWriterStep var2 = var0.expectation.active;
                if (var2 != null && var2.publication == var0 && var2.selection == var1) {
                    var2.closed = true;
                    var0.expectation.active = null;
                }

                FoodWeaponSourceLedger.OwnerPlayerWriter var3 = var0.playerWriter;
                if (var3 != null && var3.selection == var1) {
                    closeOwnerPlayerWriter(var3);
                }
            }
        }
    }

    public static synchronized void ownerWriterObservationFailed(FoodWeaponSourceLedger.OwnerWriterStep var0, int var1, Throwable var2) {
        if (var0 != null && var0.publication.thread == Thread.currentThread()) {
            if (var0.publication.failedWriterStep == null) {
                var0.publication.failedWriterStep = var0;
            }

            revokeOwnerWriterSelection(var0.publication, var0.selection);

            try {
                var0.selection.observationFailed(var0.observation, var1, var2);
            } finally {
                closeOwnerWriterStep(var0);
            }
        }
    }

    public static synchronized void ownerWriterOriginalFailed(FoodWeaponSourceLedger.OwnerWriterStep var0, Throwable var1) {
        if (var0 != null && var0.publication.thread == Thread.currentThread()) {
            if (var0.publication.failedWriterStep == null) {
                var0.publication.failedWriterStep = var0;
            }

            revokeOwnerWriterSelection(var0.publication, var0.selection);

            try {
                var0.selection.originalFailed(var0.observation, var1);
            } finally {
                closeOwnerWriterStep(var0);
            }
        }
    }

    public static synchronized Throwable ownerWriterOriginalFailure(FoodWeaponSourceLedger.OwnerWriterStep var0) {
        return var0 != null && var0.publication.thread == Thread.currentThread() ? var0.observation.original : null;
    }

    public static synchronized Throwable ownerWriterObservationFailure(FoodWeaponSourceLedger.OwnerWriterStep var0, int var1) {
        return var0 != null && var0.publication.thread == Thread.currentThread() ? var0.observation.failure(var1) : null;
    }

    public static synchronized void ownerPlayerWriterObservationFailed(FoodWeaponSourceLedger.OwnerPlayerWriter var0, int var1, Throwable var2) {
        if (var0 != null && var0.publication.thread == Thread.currentThread()) {
            if (var0.publication.failedPlayerWriter == null) {
                var0.publication.failedPlayerWriter = var0;
            }

            revokeOwnerWriterSelection(var0.publication, var0.selection);

            try {
                var0.selection.observationFailed(var0.observation, var1, var2);
            } finally {
                closeOwnerPlayerWriter(var0);
            }
        }
    }

    public static synchronized Throwable ownerPlayerWriterObservationFailure(FoodWeaponSourceLedger.OwnerPlayerWriter var0, int var1) {
        return var0 != null && var0.publication.thread == Thread.currentThread() ? var0.observation.failure(var1) : null;
    }

    public static synchronized void ownerPlayerWriterOriginalFailed(FoodWeaponSourceLedger.OwnerPlayerWriter var0, Throwable var1) {
        if (var0 != null && var0.publication.thread == Thread.currentThread()) {
            if (var0.publication.failedPlayerWriter == null) {
                var0.publication.failedPlayerWriter = var0;
            }

            revokeOwnerWriterSelection(var0.publication, var0.selection);

            try {
                var0.selection.originalFailed(var0.observation, var1);
            } finally {
                closeOwnerPlayerWriter(var0);
            }
        }
    }

    public static synchronized Throwable ownerPlayerWriterOriginalFailure(FoodWeaponSourceLedger.OwnerPlayerWriter var0) {
        return var0 != null && var0.publication.thread == Thread.currentThread() ? var0.observation.original : null;
    }

    static synchronized FoodWeaponSourceLedger.OwnerPlayerWriter ownerFailedPlayerWriter(FoodWeaponSourceLedger.OwnerWriterPublication var0) {
        return var0 != null && var0.thread == Thread.currentThread() ? var0.failedPlayerWriter : null;
    }

    public static synchronized void ownerPlayerObservationFailed(FoodWeaponSourceLedger.OwnerPlayerStep var0, int var1, Throwable var2) {
        if (var0 != null && var0.writer.publication.thread == Thread.currentThread()) {
            if (var0.writer.publication.failedPlayerStep == null) {
                var0.writer.publication.failedPlayerStep = var0;
            }

            revokeOwnerWriterSelection(var0.writer.publication, var0.selection);

            try {
                var0.selection.observationFailed(var0.observation, var1, var2);
            } finally {
                closeOwnerPlayerStep(var0);
            }
        }
    }

    public static synchronized void ownerPlayerOriginalFailed(FoodWeaponSourceLedger.OwnerPlayerStep var0, Throwable var1) {
        if (var0 != null && var0.writer.publication.thread == Thread.currentThread()) {
            if (var0.writer.publication.failedPlayerStep == null) {
                var0.writer.publication.failedPlayerStep = var0;
            }

            revokeOwnerWriterSelection(var0.writer.publication, var0.selection);

            try {
                var0.selection.originalFailed(var0.observation, var1);
            } finally {
                closeOwnerPlayerStep(var0);
            }
        }
    }

    public static synchronized Throwable ownerPlayerOriginalFailure(FoodWeaponSourceLedger.OwnerPlayerStep var0) {
        return var0 != null && var0.writer.publication.thread == Thread.currentThread() ? var0.observation.original : null;
    }

    public static synchronized void ownerRepairOriginalFailed(FoodWeaponSourceLedger.OwnerRepairObservation var0, Throwable var1) {
        if (var0 != null && var0.publication.thread == Thread.currentThread()) {
            if (var0.publication.failedRepairObservation == null) {
                var0.publication.failedRepairObservation = var0;
            }

            revokeOwnerWriterSelection(var0.publication, var0.selection);

            try {
                var0.selection.originalFailed(var0.observation, var1);
            } finally {
                closeOwnerRepairObservation(var0);
            }
        }
    }

    public static synchronized Throwable ownerRepairOriginalFailure(FoodWeaponSourceLedger.OwnerRepairObservation var0) {
        return var0 != null && var0.publication.thread == Thread.currentThread() ? var0.observation.original : null;
    }

    public static synchronized Throwable ownerPlayerObservationFailure(FoodWeaponSourceLedger.OwnerPlayerStep var0, int var1) {
        return var0 != null && var0.writer.publication.thread == Thread.currentThread() ? var0.observation.failure(var1) : null;
    }

    static synchronized FoodWeaponSourceLedger.OwnerWriterStep ownerFailedWriterStep(FoodWeaponSourceLedger.OwnerWriterPublication var0) {
        return var0 != null && var0.thread == Thread.currentThread() ? var0.failedWriterStep : null;
    }

    static synchronized FoodWeaponSourceLedger.OwnerPlayerStep ownerFailedPlayerStep(FoodWeaponSourceLedger.OwnerWriterPublication var0) {
        return var0 != null && var0.thread == Thread.currentThread() ? var0.failedPlayerStep : null;
    }

    static synchronized FoodWeaponSourceLedger.OwnerRepairObservation ownerFailedRepairObservation(FoodWeaponSourceLedger.OwnerWriterPublication var0) {
        return var0 != null && var0.thread == Thread.currentThread() ? var0.failedRepairObservation : null;
    }

    static synchronized boolean ownerRepairAcceptedIntentLive(Object var0, OwnerFieldExpectedAuthority.Prepared var1) {
        if (var0 instanceof FoodWeaponSourceLedger.OwnerWriterPublication var2
            && var2.fieldPreparation == var1
            && var1 != null
            && var1.issuer == var2
            && originalPublicationRootLive(var2)) {
            return true;
        }

        return false;
    }

    static boolean ownerRepairScopedWeaponLive(UUID var0) {
        FoodWeaponSourceLedger.NativeHurtScope var1;
        synchronized (FoodWeaponSourceLedger.class) {
            var1 = PRIVATE_NATIVE_HURT.get();
            if (var1 == null
                || var1.thread != Thread.currentThread()
                || var1.closed
                || !var1.snapshot.hasUUID("WeaponId")
                || !var1.snapshot.getUUID("WeaponId").equals(var0)) {
                return false;
            }
        }

        if (!OwnerRepairGenerationAuthority.snapshotAllowed(var1.snapshot)) {
            return false;
        } else {
            synchronized (FoodWeaponSourceLedger.class) {
                return PRIVATE_NATIVE_HURT.get() == var1 && !var1.closed;
            }
        }
    }

    static boolean ownerRepairScopedWeaponLive(UUID var0, CompoundTag var1, ServerPlayer var2) {
        if (var0 != null
            && var1 != null
            && var2 != null
            && server(var2).isSameThread()
            && var1.hasUUID("Owner")
            && var1.getUUID("Owner").equals(var2.getUUID())
            && var1.hasUUID("WeaponId")
            && var1.getUUID("WeaponId").equals(var0)) {
            FoodWeaponSourceLedger.OriginalInteractionBinding var3 = currentInteraction(var1, var2);
            if (var3 == null) {
                return false;
            } else {
                FoodWeaponSourceLedger.InteractionRoot var4 = var3.root;
                FoodWeaponSourceLedger.WeaponExpectation var5;
                Object var6;
                ItemStack var7;
                OwnerLineageRevocationStore.Lease var8;
                synchronized (FoodWeaponSourceLedger.class) {
                    if (var3.closed
                        || var4.player != var2
                        || var4.thread != Thread.currentThread()
                        || var4.closed
                        || var4.retired
                        || var4.readCut.closed
                        || var4.current != var3
                        || var4.server != server(var2)
                        || SERVERS.get(var4.server) != var4.serverState
                        || var4.serverState.closed
                        || var4.ownerState.closed
                        || var4.ownerState.current != var4
                        || var4.serverState.ownerEpochs.get(var2.getUUID()) != var4.ownerState
                        || var4.serverState.epoch != var4.serverEpoch
                        || var4.ownerState.epoch != var4.ownerEpoch
                        || var4.projectionSlot < 0
                        || var4.projectionSlot >= ACTIVE_ROOT_PROJECTIONS.length
                        || ACTIVE_ROOT_PROJECTIONS[var4.projectionSlot] != var4
                        || var4.expectation == null
                        || var4.expectation.closed) {
                        return false;
                    }

                    var5 = var4.expectation;
                    var7 = var5.actual;
                    var6 = var5.repairGeneration;
                    var8 = OwnerFullRepairGuard.lease(var7);
                    if (var8 == null || !var8.weapon.equals(var0) || !var8.owner.equals(var2.getUUID())) {
                        return false;
                    }
                }

                if (OwnerRepairGenerationAuthority.matches(var7, var6) && OwnerLineageRevocationStore.liveLease(var8) && originalBindingAncestryLive(var3)) {
                    synchronized (FoodWeaponSourceLedger.class) {
                        return !var3.closed
                            && var4.player == var2
                            && var4.thread == Thread.currentThread()
                            && !var4.closed
                            && !var4.retired
                            && !var4.readCut.closed
                            && var4.current == var3
                            && var4.server == server(var2)
                            && var4.server.isSameThread()
                            && SERVERS.get(var4.server) == var4.serverState
                            && !var4.serverState.closed
                            && !var4.ownerState.closed
                            && var4.ownerState.current == var4
                            && var4.serverState.ownerEpochs.get(var2.getUUID()) == var4.ownerState
                            && var4.serverState.epoch == var4.serverEpoch
                            && var4.ownerState.epoch == var4.ownerEpoch
                            && var4.projectionSlot >= 0
                            && var4.projectionSlot < ACTIVE_ROOT_PROJECTIONS.length
                            && ACTIVE_ROOT_PROJECTIONS[var4.projectionSlot] == var4
                            && var4.expectation == var5
                            && !var5.closed
                            && var5.repairGeneration == var6
                            && var3.snapshot != null
                            && var3.snapshot.equals(var1)
                            && var8.weapon.equals(var0)
                            && var8.owner.equals(var2.getUUID())
                            && OwnerRepairGenerationAuthority.identityMatches(var7, var6, var8)
                            && OwnerFullRepairGuard.lease(var7) == var8;
                    }
                } else {
                    return false;
                }
            }
        } else {
            return false;
        }
    }

    static OwnerRepairGenerationAuthority.Intent prepareOwnerStatueRepair(FoodWeaponSourceLedger.OwnerWriterPublication var0, ItemStack var1, int var2) {
        OwnerFieldExpectedAuthority.Prepared var3;
        byte[] var4;
        synchronized (FoodWeaponSourceLedger.class) {
            if (!writerCaller(FoodWeaponRepair.class)
                || var0 == null
                || var0.thread != Thread.currentThread()
                || var0.expectation.actual != var1
                || var0.fieldPreparation == null
                || !OwnerFieldExpectedAuthority.covered(var0.fieldPreparation)) {
                return null;
            }

            var3 = var0.fieldPreparation;
            var4 = var0.expectation.cursor.expected;
        }

        return OwnerRepairGenerationAuthority.prepare(var0, var1, var3, var4, var2);
    }

    static void ownerStatueRepairEntered(OwnerRepairGenerationAuthority.Intent var0, ItemStack var1) {
        OwnerRepairGenerationAuthority.entered(var0, var1);
    }

    static void ownerStatueRepairFieldsObserved(OwnerRepairGenerationAuthority.Intent var0, ItemStack var1, int var2, byte[] var3) {
        OwnerRepairGenerationAuthority.observed(var0, var1, var2, var3);
    }

    static boolean ownerStatueRepairCompleted(OwnerRepairGenerationAuthority.Intent var0, ItemStack var1, boolean var2) {
        return OwnerRepairGenerationAuthority.completed(var0, var1, var2);
    }

    static void ownerStatueRepairFailed(OwnerRepairGenerationAuthority.Intent var0, Throwable var1) {
        OwnerRepairGenerationAuthority.failed(var0, var1);
    }

    private static boolean evergaleRawCaller(Class<?> var0, String var1) {
        return StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE)
            .walk(
                var2 -> var2.filter(var0xx -> var0xx.getDeclaringClass() != FoodWeaponSourceLedger.class)
                        .findFirst()
                        .map(var2x -> var2x.getDeclaringClass() == var0 && var2x.getMethodName().equals(var1))
                        .orElse(false)
            );
    }

    public static synchronized FoodWeaponSourceLedger.NativeEvergaleAction nativeEvergaleActionEntered(RawAction var0) {
        if (var0 == null) {
            return null;
        } else {
            FoodWeaponSourceLedger.NativeEvergaleAction var1 = EVERGALE_ACTIONS.get(var0);
            if (var1 != null) {
                var1.rejected = true;
                var1.refusal = "DUPLICATE_RAW_ACTION";
                return var1;
            } else if (EVERGALE_ACTIONS.size() == 4096) {
                return null;
            } else {
                FoodWeaponSourceLedger.NativeEvergaleAction var2 = new FoodWeaponSourceLedger.NativeEvergaleAction(var0);
                EVERGALE_ACTIONS.put(var0, var2);
                var2.rejected = true;
                var2.refusal = "FULL_S01_SWORD_SOURCE_AND_PAID_ISSUER_NOT_WIRED";
                boolean var3 = var0.site == Site.SWING
                    ? evergaleRawCaller(NativeEvergaleBridge.class, "swing")
                    : evergaleRawCaller(NativeEvergaleBridge.class, "use");
                if (!var3
                    || var0.originalThread != var2.thread
                    || var0.closed
                    || var0.originalEntered
                    || var0.nested
                    || var0.previous != null
                    || var0.originalStack == null
                    || var0.originalActor == null
                    || var0.originalReceiver == null
                    || !NativeEvergaleBridge.isEvergale(var0.originalReceiver)
                    || var0.site == Site.SWING && var0.originalHand != null) {
                    var2.refusal = "RAW_ACTION_BOUNDARY_OR_IDENTITY_REJECTED";
                }

                return var2;
            }
        }
    }

    public static synchronized void nativeEvergaleActionClosed(FoodWeaponSourceLedger.NativeEvergaleAction var0, RawAction var1) {
        if (var0 != null && var0.thread == Thread.currentThread() && !var0.closed) {
            var0.closed = true;
            var0.rejected = true;
            boolean var2 = EVERGALE_ACTIONS.get(var0.original) == var0;
            if (var2) {
                EVERGALE_ACTIONS.remove(var0.original);
            }

            if (var0.original == var1 && var2 && evergaleRawCaller(NativeEvergaleBridge.class, "close")) {
                var0.originalFailure = var1.originalFailure;
                var0.observationFailure = var1.observationFailure;
                if (var1.closed
                    && var1.originalEntered
                    && var1.originalReturned
                    && var1.originalFailure == null
                    && var1.observationFailure == null
                    && !var1.nested
                    && var1.site == Site.SWING) {
                    for (RawSpawn var4 : var1.spawns) {
                        if (var4 != null
                            && var4.action == var1
                            && var4.originalEntered
                            && var4.originalReturned
                            && var4.originalReturn
                            && var4.originalFailure == null
                            && var4.fieldFailure == null
                            && var4.fieldObservedSame
                            && var4.originalLevel == var1.originalLevel) {
                            Entity var6 = var4.originalEntity;
                            if (var6 instanceof AbstractArrow) {
                                AbstractArrow var5 = (AbstractArrow)var6;
                                if (NativeEvergaleBridge.isAirStrike(var5) && var4.carry != null && var4.carry.action == var1 && var4.carry.spawn == var4) {
                                    RawOwnerAssignment var9 = var4.carry.owner;
                                    if (var9 != null
                                        && var9.action == var1
                                        && var9.originalReceiver == var4.originalEntity
                                        && var9.originalOwner == var1.originalActor
                                        && var9.originalEntered
                                        && var9.originalReturned
                                        && var9.originalFailure == null
                                        && var1.owners.contains(var9)) {
                                        for (RawCooldown var8 : var1.cooldowns) {
                                            if (var8 != null
                                                && var8.action == var1
                                                && var8.originalEntered
                                                && var8.originalReturned
                                                && var8.originalFailure == null
                                                && var8.originalItem == var1.originalReceiver
                                                && var8.originalReceiver != null
                                                && var8.originalTicks > 0) {
                                                var0.witnesses.add(new FoodWeaponSourceLedger.NativeEvergaleSpawnWitness(var4, var9, var8));
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                var0.refusal = "FOREIGN_SAVED_CLOSE";
            }
        }
    }

    public static synchronized FoodWeaponSourceLedger.NativeEvergaleInteraction nativeEvergaleImpactEntered(NativeEvergaleImpact.RawImpact var0) {
        if (var0 == null) {
            return null;
        } else {
            FoodWeaponSourceLedger.NativeEvergaleInteraction var1 = EVERGALE_IMPACTS.get(var0);
            if (var1 != null) {
                var1.rejected = true;
                var1.refusal = "DUPLICATE_RAW_IMPACT";
                return var1;
            } else if (EVERGALE_IMPACTS.size() == 4096) {
                return null;
            } else {
                FoodWeaponSourceLedger.NativeEvergaleInteraction var2 = new FoodWeaponSourceLedger.NativeEvergaleInteraction(var0);
                EVERGALE_IMPACTS.put(var0, var2);
                var2.rejected = true;
                var2.refusal = "OWNER_ACCEPTED_PAID_FIELD_AND_MELEE_PROJECTION_NOT_WIRED";
                if (!evergaleRawCaller(NativeEvergaleImpact.class, "hurt")
                    || var0.originalThread != var2.thread
                    || var0.closed
                    || var0.originalEntered
                    || var0.originalCarrier == null
                    || var0.originalTarget == null
                    || var0.originalSource == null
                    || !Float.isFinite(var0.originalAmount)
                    || var0.originalAmount <= 0.0F
                    || Float.floatToRawIntBits(var0.originalAmount) != var0.originalAmountBits
                    || !NativeEvergaleBridge.isAirStrike(var0.originalCarrier)) {
                    var2.refusal = "RAW_IMPACT_BOUNDARY_OR_IDENTITY_REJECTED";
                }

                return var2;
            }
        }
    }

    public static synchronized void nativeEvergaleImpactClosed(FoodWeaponSourceLedger.NativeEvergaleInteraction var0, NativeEvergaleImpact.RawImpact var1) {
        if (var0 != null && var0.thread == Thread.currentThread() && !var0.closed) {
            var0.closed = true;
            var0.rejected = true;
            boolean var2 = EVERGALE_IMPACTS.get(var0.original) == var0;
            if (var2) {
                EVERGALE_IMPACTS.remove(var0.original);
            }

            if (var0.original == var1 && var2 && evergaleRawCaller(NativeEvergaleImpact.class, "hurt")) {
                var0.originalFailure = var1.originalFailure;
                var0.observationFailure = var1.observationFailure;
                if (!var1.closed || !var1.originalEntered || !var1.originalReturned) {
                    var0.refusal = "INCOMPLETE_ORIGINAL_IMPACT";
                }
            } else {
                var0.refusal = "FOREIGN_SAVED_IMPACT_CLOSE";
            }
        }
    }

    private static synchronized long nextOwnerFieldGeneration() {
        if (ownerFieldGeneration == Long.MAX_VALUE) {
            throw new IllegalStateException("Owner field generation exhausted");
        } else {
            return ++ownerFieldGeneration;
        }
    }

    static boolean ownerFieldPreparedEpochs(Object var0, MinecraftServer var1, Object var2, Object var3) {
        FoodWeaponSourceLedger.OwnerWriterPublication var4;
        FoodWeaponSourceLedger.WeaponExpectation var5;
        OwnerLineageRevocationStore.Lease var6;
        Object var7;
        synchronized (FoodWeaponSourceLedger.class) {
            label63: {
                if (var0 instanceof FoodWeaponSourceLedger.OwnerWriterPublication var9 && !var9.closed && var9.thread == Thread.currentThread()) {
                    var4 = var9;
                    if (!preparedEpochIdentity(var9, var1, var2, var3)) {
                        return false;
                    }

                    var5 = var9.expectation;
                    var7 = var5.repairGeneration;
                    var6 = OwnerFullRepairGuard.lease(var5.actual);
                    break label63;
                }

                return false;
            }
        }

        if (OwnerRepairGenerationAuthority.matches(var5.actual, var7) && OwnerFullRepairGuard.permits(var5.actual)) {
            synchronized (FoodWeaponSourceLedger.class) {
                return !var4.closed
                    && var4.thread == Thread.currentThread()
                    && var4.expectation == var5
                    && !var5.closed
                    && var5.repairGeneration == var7
                    && OwnerRepairGenerationAuthority.identityMatches(var5.actual, var7, var6)
                    && preparedEpochIdentity(var4, var1, var2, var3);
            }
        } else {
            return false;
        }
    }

    static synchronized OwnerFieldExpectedAuthority.Prepared ownerFieldPrepared(Object var0) {
        if (var0 instanceof FoodWeaponSourceLedger.OwnerWriterPublication var1 && !var1.closed && var1.thread == Thread.currentThread()) {
            return var1.fieldPreparation;
        }

        return null;
    }

    static boolean ownerNativeLeafAccepted(OwnerFieldExpectedAuthority.Prepared var0, Object var1, Object var2, Object var3) {
        return var0 != null
                && var0.issuer instanceof FoodWeaponSourceLedger.OwnerWriterPublication var4
                && var4.fieldPreparation == var0
                && var4.thread == Thread.currentThread()
                && !var4.closed
                && var4.receipt != null
                && var3 instanceof OwnerNativeLeafFields
            ? FoodWeaponFinalDamage.acceptsOwnerReserveLeaf(var0, var1, var2, var3)
            : false;
    }

    public static OwnerTimerSemanticAuthority.LeafObservation ownerTimerLeafEntered(Object var0, Object var1, Object var2) {
        if (writerCaller(FoodWeaponHitPlan.class)
            && var0 instanceof FoodWeaponHitPlan.FinalEffectsCommit var3
            && var1 instanceof FoodWeaponFinalDamage.ChildHurtBridge var4) {
            FoodWeaponOriginalSequence.Session var5 = FoodWeaponFinalDamage.originalHitPlanSequence(var3, var4);
            if (var5 != null
                && var5.original.atLeaf(var2)
                && var5.lineage.issuer instanceof FoodWeaponSourceLedger.OwnerWriterPublication var6
                && var6.fieldSequenceBinding != null
                && var6.fieldSequenceBinding.sequence == var5
                && var6.fieldPreparation != null
                && var6.fieldPreparation.lineage == var5.lineage
                && OwnerFieldExpectedAuthority.unchanged(var6.fieldPreparation)) {
                return OwnerTimerSemanticAuthority.enter(var5, var3, var2);
            }

            return null;
        }

        return null;
    }

    public static OwnerNativeSemanticAuthority.Admission ownerNativePutEntered(Object var0, Object var1, Object var2, Tag var3) {
        if (writerCaller(FoodWeaponHitPlan.class)
            && var0 instanceof FoodWeaponHitPlan.FinalEffectsCommit var4
            && var1 instanceof FoodWeaponFinalDamage.ChildHurtBridge var5) {
            FoodWeaponOriginalSequence.Session var6 = FoodWeaponFinalDamage.originalHitPlanSequence(var4, var5);
            if (var6 != null
                && var6.original.atLeaf(var2)
                && var6.lineage.issuer instanceof FoodWeaponSourceLedger.OwnerWriterPublication var7
                && var7.fieldSequenceBinding != null
                && var7.fieldSequenceBinding.sequence == var6
                && var7.fieldPreparation != null
                && var7.fieldPreparation.lineage == var6.lineage
                && OwnerFieldExpectedAuthority.unchanged(var7.fieldPreparation)) {
                return OwnerNativeSemanticAuthority.admit(var6, var7.fieldPreparation.nativePuts.get(var2), var2, var3);
            }

            return null;
        }

        return null;
    }

    public static void ownerOriginalPublicationAssigned(Object var0, Object var1) {
        boolean var2 = StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE)
            .walk(
                var0x -> var0x.skip(1L)
                        .findFirst()
                        .map(
                            var0xx -> var0xx.getDeclaringClass() == FoodWeaponFinalDamage.class
                                    && var0xx.getMethodName().contains("ftboss$ownerOriginalPublicationAssigned")
                        )
                        .orElse(false)
            );
        if (var2
            && var1 instanceof FoodWeaponSourceLedger.OwnerWriterPublication var3
            && !var3.fieldAdmissionAttempted
            && var3.thread == Thread.currentThread()
            && var3.binding.root.writerPublication == var3
            && !var3.closed) {
            var3.fieldAdmissionAttempted = true;
            OwnerFieldExpectedAuthority.Prepared var4 = prepareOwnerFieldExpectedSequence(var3, var0);
            if (var4 == null) {
                return;
            }

            try {
                OwnerFieldExpectedAuthority.quoteTimers(var4, var0, var3.expectation.actual, var3.binding.root.player, var3.binding.source);
            } catch (Throwable var6) {
                OwnerFieldExpectedAuthority.unsupported(var4);
            }

            if (!sealOwnerFieldExpectedSequence(var3, var4)) {
                return;
            }

            bindOwnerFieldExpectedSequence(var3, OwnerFieldExpectedAuthority.session(var4), OwnerFieldExpectedAuthority.coverage(var4));
            return;
        }
    }

    static OwnerFieldExpectedAuthority.Prepared prepareOwnerFieldExpectedSequence(FoodWeaponSourceLedger.OwnerWriterPublication var0, Object var1) {
        if (writerCaller(FoodWeaponFinalDamage.class)
            && var0 != null
            && var0.thread == Thread.currentThread()
            && var0.fieldSequenceBinding == null
            && var0.fieldPreparation == null
            && !var0.closed
            && !var0.expectation.closed
            && var1 != null
            && !var0.binding.closed
            && !var0.binding.root.closed
            && !var0.binding.root.retired) {
            FoodWeaponSourceLedger.InteractionRoot var2 = var0.binding.root;
            if (!projectedCurrentBindingLive(var0.binding)) {
                return null;
            } else {
                synchronized (FoodWeaponSourceLedger.class) {
                    if (var0.closed
                        || var0.thread != Thread.currentThread()
                        || var0.binding.closed
                        || var0.fieldSequenceBinding != null
                        || var0.fieldPreparation != null
                        || var2.thread != Thread.currentThread()
                        || var2.closed
                        || var2.retired
                        || var2.readCut.closed
                        || var2.current != var0.binding
                        || var2.writerPublication != var0
                        || var2.expectation != var0.expectation
                        || var0.expectation.closed
                        || var2.serverState.closed
                        || var2.ownerState.closed
                        || var2.ownerState.current != var2
                        || !var2.server.isSameThread()
                        || SERVERS.get(var2.server) != var2.serverState
                        || var2.serverState.ownerEpochs.get(var2.player.getUUID()) != var2.ownerState
                        || var2.serverState.epoch != var2.serverEpoch
                        || var2.ownerState.epoch != var2.ownerEpoch
                        || var2.projectionSlot < 0
                        || var2.projectionSlot >= ACTIVE_ROOT_PROJECTIONS.length
                        || ACTIVE_ROOT_PROJECTIONS[var2.projectionSlot] != var2
                        || !OwnerRepairGenerationAuthority.identityMatches(
                            var0.expectation.actual, var0.expectation.repairGeneration, OwnerFullRepairGuard.lease(var0.expectation.actual)
                        )) {
                        return null;
                    }
                }

                FoodWeaponOriginalSequence.Lineage var8 = FoodWeaponOriginalSequence.ownerLineage(
                    var0.binding.root, var0.expectation.cursor.position(), var0, var0.receipt, var0.binding.childCall, var1, var0.fieldGeneration
                );
                Entity var4 = var0.binding.source instanceof OwnerFieldSourceAccess var5 ? var5.ftboss$ownerFieldDirectEntity() : null;
                if (!(var0.binding.source instanceof OwnerFieldSourceAccess)) {
                    return null;
                } else {
                    OwnerFieldExpectedAuthority.Prepared var9 = OwnerFieldExpectedAuthority.prepare(
                        var0,
                        var0.binding.root.player,
                        var4,
                        var8,
                        var0.expectation.cursor.expected,
                        var0.expectation.actual,
                        var0.binding.source,
                        var0.binding.root.server,
                        var0.binding.root.serverEpoch,
                        var0.binding.root.ownerEpoch
                    );
                    var0.fieldPreparation = var9;
                    return var9;
                }
            }
        } else {
            return null;
        }
    }

    static boolean sealOwnerFieldExpectedSequence(FoodWeaponSourceLedger.OwnerWriterPublication var0, OwnerFieldExpectedAuthority.Prepared var1) {
        return writerCaller(FoodWeaponFinalDamage.class)
            && var0 != null
            && var0.thread == Thread.currentThread()
            && var0.fieldPreparation == var1
            && OwnerFieldExpectedAuthority.seal(var1);
    }

    static boolean bindOwnerFieldExpectedSequence(
        FoodWeaponSourceLedger.OwnerWriterPublication var0, FoodWeaponOriginalSequence.Session var1, FoodWeaponOriginalSequence.CoverageReceipt var2
    ) {
        if (writerCaller(FoodWeaponFinalDamage.class)
            && var0 != null
            && var1 != null
            && var0.thread == Thread.currentThread()
            && var0.fieldSequenceBinding == null
            && !var0.closed
            && !var0.binding.closed
            && !var0.binding.root.closed
            && !var0.binding.root.retired
            && var0.binding.root.writerPublication == var0) {
            OwnerFieldExpectedAuthority.Prepared var3 = var0.fieldPreparation;
            if (var3 != null && OwnerFieldExpectedAuthority.session(var3) == var1 && var3.issuer == var0) {
                FoodWeaponOriginalSequence.Lineage var4 = var1.lineage;
                if (var4.ownerRoot != var0.binding.root
                    || var4.ownerSelection != var0.expectation.cursor.position()
                    || var4.issuer != var0
                    || var4.acceptedCallback != var0.receipt
                    || var4.childScope != var0.binding.childCall
                    || var4.generation != var0.fieldGeneration) {
                    return false;
                } else if (!FoodWeaponExpectedTrace.same(
                    FoodWeaponExpectedFieldsCodec.decodeD084(var0.expectation.cursor.expected), var1.expected.before().weapon
                )) {
                    return false;
                } else {
                    FoodWeaponSourceLedger.OwnerFieldSequenceBinding var5 = new FoodWeaponSourceLedger.OwnerFieldSequenceBinding(var0, var1, var3);
                    var0.fieldSequenceBinding = var5;
                    Throwable var6 = null;

                    try {
                        OwnerFieldExpectedAuthority.Prepared var7 = OwnerFieldExpectedAuthority.matching(var0, var1, var2);
                        var5.extraAuthority = var7 == var3 && var1.bindRegisteredCoverage(var2);
                    } catch (Throwable var13) {
                        var5.extraAuthority = false;
                        var6 = var13;
                    } finally {
                        OwnerFieldExpectedAuthority.bound(var3);
                    }

                    if (!var5.extraAuthority && (var6 != null || !OwnerFieldExpectedAuthority.pending(var3))) {
                        var1.interferenceObserved(var4.ownerSelection, var4.generation, var6);
                    }

                    boolean var15 = false;

                    try {
                        var15 = FoodWeaponFinalDamage.bindOwnerOriginalSequence(var0.receipt, var1);
                    } catch (Throwable var12) {
                        var5.extraAuthority = false;
                        var1.interferenceObserved(var4.ownerSelection, var4.generation, var12);
                        var15 = var1.original.continuing(var4.publication);
                    }

                    if (!var15) {
                        var5.extraAuthority = false;
                        var5.terminal = true;
                        OwnerFieldExpectedAuthority.retire(var3);
                    }

                    return var15;
                }
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    static boolean tryInstallOwnerCoverage(FoodWeaponOriginalSequence.Session var0) {
        if (var0 != null && var0.lineage.issuer instanceof FoodWeaponSourceLedger.OwnerWriterPublication var1) {
            FoodWeaponSourceLedger.OwnerFieldSequenceBinding var4 = var1.fieldSequenceBinding;
            if (var4 == null
                || var4.sequence != var0
                || var4.publication != var1
                || var4.terminal
                || var4.thread != Thread.currentThread()
                || var1.closed
                || var1.fieldPreparation != var4.prepared) {
                return false;
            } else if (var4.extraAuthority) {
                return var0.extraAuthorityRetained();
            } else {
                FoodWeaponOriginalSequence.CoverageReceipt var3 = OwnerFieldExpectedAuthority.refreshCoverage(var4.prepared);
                if (var3 == null) {
                    return false;
                } else {
                    var4.extraAuthority = var0.bindRegisteredCoverage(var3);
                    return var4.extraAuthority;
                }
            }
        } else {
            return false;
        }
    }

    static synchronized boolean ownerFieldOriginalContinues(FoodWeaponSourceLedger.OwnerWriterPublication var0, FoodWeaponOriginalSequence.Session var1) {
        if (var0 != null && var1 != null) {
            FoodWeaponSourceLedger.OwnerFieldSequenceBinding var2 = var0.fieldSequenceBinding;
            return var2 != null
                && var2.thread == Thread.currentThread()
                && !var2.terminal
                && var2.publication == var0
                && var2.sequence == var1
                && var1.lineage.issuer == var0
                && var1.original.continuing(var1.lineage.publication);
        } else {
            return false;
        }
    }

    static synchronized void retireOwnerFieldExtraAuthority(
        FoodWeaponSourceLedger.OwnerWriterPublication var0, FoodWeaponOriginalSequence.Session var1, Throwable var2
    ) {
        if (var0 != null && var1 != null) {
            FoodWeaponSourceLedger.OwnerFieldSequenceBinding var3 = var0.fieldSequenceBinding;
            if (var3 != null && var3.thread == Thread.currentThread() && var3.sequence == var1) {
                var3.extraAuthority = false;
                var1.interferenceObserved(var1.lineage.ownerSelection, var1.lineage.generation, var2);
                if (var3.selection != null) {
                    var3.selection.revoked = true;
                }
            }
        }
    }

    static synchronized void terminalOwnerFieldOriginal(FoodWeaponSourceLedger.OwnerWriterPublication var0, FoodWeaponOriginalSequence.Session var1) {
        if (var0 != null && var1 != null) {
            FoodWeaponSourceLedger.OwnerFieldSequenceBinding var2 = var0.fieldSequenceBinding;
            if (var2 != null && var2.thread == Thread.currentThread() && var2.sequence == var1) {
                var2.terminal = true;
                var2.extraAuthority = false;
                var1.original.terminal();
                OwnerFieldExpectedAuthority.retire(var2.prepared);
            }
        }
    }

    static OwnerFullRepairGuard.Cut beginOwnerFullRepair(FoodWeaponSourceLedger.OwnerWriterPublication var0, Object var1, Object var2, ItemStack var3, int var4) {
        OwnerFieldExpectedAuthority.Prepared var6 = null;
        byte[] var7 = null;
        boolean var5;
        synchronized (FoodWeaponSourceLedger.class) {
            var5 = var0 != null && var0.thread == Thread.currentThread() && var0.expectation.actual == var3;
            if (var5) {
                var0.fullRepairAttempted = true;
            }

            if (var5 && writerCaller(FoodWeaponHitPlan.class) && var0.commit == var1 && var0.leaf == var2 && var0.fieldPreparation != null) {
                var6 = var0.fieldPreparation;
                var7 = var0.expectation.cursor.expected;
            }
        }

        OwnerRepairGenerationAuthority.Intent var15 = var6 == null ? null : OwnerRepairGenerationAuthority.prepareReduction(var0, var3, var6, var7, var4);
        OwnerFullRepairGuard.Cut var9 = OwnerFullRepairGuard.enter(var3, null, var4);
        boolean var10;
        synchronized (FoodWeaponSourceLedger.class) {
            var10 = var5 && writerCaller(FoodWeaponHitPlan.class) && var0.commit == var1 && var0.leaf == var2;
            if (var10) {
                OwnerFullRepairGuard.planned(var9, var15);
            }
        }

        if (!var10) {
            OwnerFullRepairGuard.close(var9);
        }

        return var9;
    }

    public static void ownerFullRepairFieldsObserved(OwnerFullRepairGuard.Cut var0, ItemStack var1, int var2, byte[] var3) {
        if (writerCaller(FoodWeaponHitPlan.class)) {
            OwnerFullRepairGuard.fieldsObserved(var0, var1, var2, var3);
        }
    }

    static synchronized boolean ownerFullRepairAttempted(FoodWeaponSourceLedger.OwnerWriterPublication var0) {
        return var0 != null && var0.thread == Thread.currentThread() && var0.fullRepairAttempted;
    }

    public static void ownerFullRepairReadReturned(OwnerFullRepairGuard.Cut var0, ItemStack var1, int var2, int var3) {
        OwnerFullRepairGuard.readReturned(var0, var1, var2, var3);
    }

    public static void ownerFullRepairSetterEntered(OwnerFullRepairGuard.Cut var0, ItemStack var1, int var2) {
        OwnerFullRepairGuard.setterEntered(var0, var1, var2);
    }

    public static void ownerFullRepairReturned(OwnerFullRepairGuard.Cut var0) {
        OwnerFullRepairGuard.returned(var0);
    }

    public static void ownerFullRepairOriginalFailed(OwnerFullRepairGuard.Cut var0, Throwable var1) {
        OwnerFullRepairGuard.failed(var0, var1, false);
    }

    public static void ownerFullRepairObservationFailed(OwnerFullRepairGuard.Cut var0, Throwable var1) {
        OwnerFullRepairGuard.failed(var0, var1, true);
    }

    public static synchronized Throwable ownerFullRepairFailure(OwnerFullRepairGuard.Cut var0, boolean var1) {
        return OwnerFullRepairGuard.failure(var0, var1);
    }

    public static void closeOwnerFullRepair(OwnerFullRepairGuard.Cut var0) {
        OwnerFullRepairGuard.close(var0);
    }

    public static void ownerFullRepairObserveAlias(OwnerFullRepairGuard.Cut var0, Object var1, Object var2, boolean var3) {
        if (var0 != null) {
            OwnerFullRepairGuard.alias(var0, var1, var2, var3);
        }
    }

    public static void ownerFullRepairObserveMutation(OwnerFullRepairGuard.Cut var0, Object var1, int var2) {
        if (var0 != null) {
            OwnerFullRepairGuard.changed(var0, var1, var2);
        }
    }

    private static boolean originalRepairCaller() {
        return NATIVE_CALLER.walk(
            var0 -> var0.filter(var0x -> var0x.getDeclaringClass() != FoodWeaponSourceLedger.class)
                    .findFirst()
                    .map(
                        var0x -> var0x.getDeclaringClass().getNestHost() == FoodWeaponHitPlan.class
                                && var0x.getDeclaringClass().getName().endsWith("$RepairLeaf")
                                && var0x.getMethodName().startsWith("ftboss$ownerRepair")
                    )
                    .orElse(false)
        );
    }

    private static Value originalRepairRawFields(ItemStack var0) {
        if ((Object)var0 instanceof Stack var1 && var1.ftboss$rawStackCount() == 1 && var1.ftboss$rawStackCaps() == null && var1.ftboss$rawStackItem() != null) {
            ResourceLocation var2 = BuiltInRegistries.ITEM.getKey(var1.ftboss$rawStackItem());
            if (var2 == null) {
                return null;
            }

            CompoundTag var3 = var1.ftboss$rawStackTag();
            if (var3 == null) {
                return FoodWeaponExpectedTrace.compound(
                    new String[]{"Count", "id"},
                    new Value[]{FoodWeaponExpectedTrace.number(1, (long)var1.ftboss$rawStackCount()), FoodWeaponExpectedTrace.string(var2.toString())}
                );
            }

            return FoodWeaponExpectedTrace.compound(
                new String[]{"Count", "id", "tag"},
                new Value[]{
                    FoodWeaponExpectedTrace.number(1, (long)var1.ftboss$rawStackCount()),
                    FoodWeaponExpectedTrace.string(var2.toString()),
                    NativeExpectedRawFields.observe(var3)
                }
            );
        }

        return null;
    }

    public static FoodWeaponSourceLedger.OriginalRepairCall ownerOriginalRepairEntered(Object var0, Object var1, Object var2, ItemStack var3) {
        if (!originalRepairCaller()) {
            return null;
        } else {
            FoodWeaponHitPlan.OriginalRepairFacts var10000;
            label62: {
                if (var0 instanceof FoodWeaponHitPlan.FinalEffectsCommit var6 && var1 instanceof FoodWeaponFinalDamage.ChildHurtBridge var5) {
                    var10000 = FoodWeaponHitPlan.originalRepairFacts(var6, var5, var2);
                    break label62;
                }

                var10000 = null;
            }

            FoodWeaponOriginalSequence.Session var17;
            FoodWeaponHitPlan.OriginalRepairFacts var4;
            label57: {
                var4 = var10000;
                if (var0 instanceof FoodWeaponHitPlan.FinalEffectsCommit var7 && var1 instanceof FoodWeaponFinalDamage.ChildHurtBridge var13) {
                    var17 = FoodWeaponFinalDamage.originalHitPlanSequence(var7, var13);
                    break label57;
                }

                var17 = null;
            }

            FoodWeaponOriginalSequence.Session var12 = var17;
            FoodWeaponSourceLedger.OwnerWriterPublication var14;
            synchronized (FoodWeaponSourceLedger.class) {
                var14 = var12 != null
                        && var12.lineage.issuer instanceof FoodWeaponSourceLedger.OwnerWriterPublication var8
                        && var8.thread == Thread.currentThread()
                        && !var8.closed
                        && var8.fieldSequenceBinding != null
                        && var8.fieldSequenceBinding.sequence == var12
                        && var4 != null
                        && var4.weapon() == var3
                        && var12.original.atLeaf(var2)
                    ? var8
                    : null;
            }

            OwnerFullRepairGuard.Cut var16 = beginOwnerFullRepair(var14, var0, var2, var3, var4 == null ? 0 : var4.reduction());
            return new FoodWeaponSourceLedger.OriginalRepairCall(var0, var1, var2, var3, var4, var16);
        }
    }

    public static synchronized void ownerOriginalRepairGetterReturned(FoodWeaponSourceLedger.OriginalRepairCall var0, ItemStack var1, int var2) {
        if (originalRepairCaller() && var0 != null && !var0.closed && var0.thread == Thread.currentThread() && var0.actual == var1 && var0.phase == 0) {
            var0.getterFields = originalRepairRawFields(var1);
            var0.getterValue = var2;
            var0.phase = 1;
        }
    }

    public static void ownerOriginalRepairSetterEntered(FoodWeaponSourceLedger.OriginalRepairCall var0, ItemStack var1, int var2) {
        if (originalRepairCaller() && var0 != null && !var0.closed && var0.thread == Thread.currentThread()) {
            FoodWeaponHitPlan.OriginalRepairFacts var3 = var0.facts;
            if (var0.actual == var1
                && var0.phase == 1
                && var3 != null
                && var3.weapon() == var1
                && var3.originalReadReturned()
                && var3.originalSetterEntered()
                && var3.originalDamage() == var0.getterValue
                && var3.requestedDamage() == var2
                && var3.observerFailure() == null
                && var3.originalFailure() == null) {
                OwnerFullRepairGuard.readReturned(var0.cut, var1, var0.getterValue, var2);
                OwnerFullRepairGuard.fieldsValueObserved(var0.cut, var1, 0, var0.getterFields);
                OwnerFullRepairGuard.setterEntered(var0.cut, var1, var2);
                var0.phase = 2;
            } else {
                OwnerFullRepairGuard.close(var0.cut);
                var0.closed = true;
            }
        }
    }

    public static void ownerOriginalRepairSetterReturned(FoodWeaponSourceLedger.OriginalRepairCall var0, ItemStack var1) {
        Object var3 = null;
        boolean var2;
        synchronized (FoodWeaponSourceLedger.class) {
            if (!originalRepairCaller() || var0 == null || var0.closed || var0.thread != Thread.currentThread()) {
                return;
            }

            var2 = var0.actual == var1 && var0.phase == 2 && var0.facts.originalFailure() == null && var0.facts.observerFailure() == null;
        }

        try {
            if (var2) {
                var3 = originalRepairRawFields(var1);
                OwnerFullRepairGuard.fieldsValueObserved(var0.cut, var1, 1, (Value)var3);
                OwnerFullRepairGuard.returned(var0.cut);
                synchronized (FoodWeaponSourceLedger.class) {
                    var0.phase = 3;
                }
            }
        } finally {
            try {
                OwnerFullRepairGuard.close(var0.cut);
            } finally {
                synchronized (FoodWeaponSourceLedger.class) {
                    var0.closed = true;
                }
            }
        }
    }

    public static void ownerOriginalRepairFailed(FoodWeaponSourceLedger.OriginalRepairCall var0, Throwable var1, boolean var2) {
        if (originalRepairCaller() && var0 != null && !var0.closed && var0.thread == Thread.currentThread()) {
            try {
                OwnerFullRepairGuard.failed(var0.cut, var1, !var2);
            } finally {
                var0.closed = true;
            }
        }
    }

    static FoodWeaponSourceLedger.OwnerRepairObservation beginOwnerRepairObservation(
        FoodWeaponSourceLedger.OwnerWriterPublication var0, Object var1, Object var2, ItemStack var3, int var4, int var5, int var6
    ) {
        beginOwnerFullRepair(var0, var1, var2, var3, var5);
        FoodWeaponSourceLedger.WriterPlanningCut var7 = writerPlanningCut(var0);
        if (writerCaller(FoodWeaponHitPlan.class)
            && ownerWriterSelectionLive(var0)
            && writerPlanningUnchanged(var0, var7)
            && var7.selection.commit == var1
            && var7.selection.leaf == var2
            && var0.expectation.actual == var3
            && var0.expectation.active == null
            && var0.repairObservation == null
            && var0.playerWriter == null
            && var6 == Math.max(0, var4 - var5)) {
            FoodWeaponSourceLedger.OwnerRepairObservation var8 = new FoodWeaponSourceLedger.OwnerRepairObservation(var0, var7, var3, var4, var5, var6);
            if (ownerWriterSelectionLive(var0)
                && writerPlanningUnchanged(var0, var7)
                && var0.expectation.active == null
                && var0.playerWriter == null
                && var0.repairObservation == null) {
                var0.repairObservation = var8;
                return var8;
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    public static synchronized void ownerRepairOriginalPhase(
        FoodWeaponSourceLedger.OwnerRepairObservation var0, ItemStack var1, int var2, Object var3, int var4
    ) {
        if (var0 != null
            && !var0.closed
            && var0.publication.thread == Thread.currentThread()
            && var0.actual == var1
            && var0.publication.repairObservation == var0
            && writerPlanningUnchanged(var0.publication, var0.planning)) {
            if (!var0.trace.record(var2, var3, var4)) {
                closeOwnerRepairObservation(var0);
            } else {
                var0.publication.binding.root.readCut.change();
            }
        }
    }

    public static synchronized void ownerRepairObservationFailed(FoodWeaponSourceLedger.OwnerRepairObservation var0, int var1, Throwable var2) {
        if (var0 != null && var0.publication.thread == Thread.currentThread()) {
            if (var0.publication.failedRepairObservation == null) {
                var0.publication.failedRepairObservation = var0;
            }

            revokeOwnerWriterSelection(var0.publication, var0.selection);

            try {
                var0.selection.observationFailed(var0.observation, var1, var2);
            } finally {
                closeOwnerRepairObservation(var0);
            }
        }
    }

    public static synchronized Throwable ownerRepairObservationFailure(FoodWeaponSourceLedger.OwnerRepairObservation var0, int var1) {
        return var0 != null && var0.publication.thread == Thread.currentThread() ? var0.observation.failure(var1) : null;
    }

    public static synchronized void closeOwnerRepairObservation(FoodWeaponSourceLedger.OwnerRepairObservation var0) {
        if (var0 != null && !var0.closed && var0.publication.thread == Thread.currentThread()) {
            var0.closed = true;
            var0.trace.close();
            var0.observation.close();
            if (var0.publication.repairObservation == var0) {
                var0.publication.repairObservation = null;
                if (var0.publication.selection == var0.selection) {
                    var0.publication.binding.root.readCut.change();
                    var0.publication.closed = true;
                }
            }
        }
    }

    private static FoodWeaponSourceLedger.WriterPlanningCut writerPlanningCut(FoodWeaponSourceLedger.OwnerWriterPublication var0) {
        return var0 == null
            ? null
            : new FoodWeaponSourceLedger.WriterPlanningCut(
                var0.selection, var0.expectation.cursor.position(), var0.binding.root.interactionChange, var0.stepOrdinal
            );
    }

    private static boolean writerPlanningUnchanged(FoodWeaponSourceLedger.OwnerWriterPublication var0, FoodWeaponSourceLedger.WriterPlanningCut var1) {
        return var0 != null
            && var1 != null
            && var1.matches(var0.selection, var0.expectation.cursor.position(), var0.binding.root.interactionChange, var0.stepOrdinal)
            && var1.selection.matches(var0.producer, var0.producerOrdinal, var0.commit, var0.leaf, var0.leafOrdinal);
    }

    private static boolean writerPublicationUnchanged(FoodWeaponSourceLedger.OwnerWriterPublication var0, FoodWeaponSourceLedger.WriterPlanningCut var1) {
        if (var0 == null
            || var1 == null
            || !var1.samePublication(var0.selection, var0.expectation.cursor.position(), var0.binding.root.interactionChange, var0.stepOrdinal)) {
            return false;
        } else {
            return var1.selection != null
                ? var1.selection.matches(var0.producer, var0.producerOrdinal, var0.commit, var0.leaf, var0.leafOrdinal)
                : var0.producer == null
                    && var0.commit == null
                    && var0.leaf == null
                    && var0.producerOrdinal == -1
                    && var0.leafOrdinal == -1
                    && var0.stepOrdinal == -1;
        }
    }

    static boolean ownerWriterPublicationContinuation(FoodWeaponSourceLedger.OwnerWriterPublication var0) {
        FoodWeaponSourceLedger.WriterPlanningCut var1 = writerPlanningCut(var0);
        return writerCaller(FoodWeaponFinalDamage.class)
            && ownerWriterPublicationLive(var0)
            && writerPublicationUnchanged(var0, var1)
            && FoodWeaponFinalDamage.matchesOwnerWriterPublication(var0.receipt, var0.binding, var0.batch, var0.admission, var0.result)
            && ownerWriterPublicationLive(var0)
            && writerPublicationUnchanged(var0, var1);
    }

    private static boolean projectedBindingLive(FoodWeaponSourceLedger.OriginalInteractionBinding var0) {
        return var0 != null
            && (
                var0.parent == null && var0.depth == 1 && var0.childCall == null && projectedCurrentBindingLive(var0)
                    || var0.parent != null && var0.root.current == var0 && originalBindingAncestryLive(var0)
            );
    }

    private static boolean projectedCurrentBindingLive(FoodWeaponSourceLedger.OriginalInteractionBinding var0) {
        FoodWeaponSourceLedger.WeaponExpectation var1;
        Object var2;
        OwnerLineageRevocationStore.Lease var3;
        synchronized (FoodWeaponSourceLedger.class) {
            if (var0 == null || var0.closed || var0.root.thread != Thread.currentThread()) {
                return false;
            }

            var1 = var0.root.expectation;
            var2 = var1 == null ? null : var1.repairGeneration;
            var3 = var1 == null ? null : OwnerFullRepairGuard.lease(var1.actual);
        }

        if (var1 != null && !OwnerRepairGenerationAuthority.matches(var1.actual, var2)) {
            return false;
        } else {
            synchronized (FoodWeaponSourceLedger.class) {
                FoodWeaponSourceLedger.InteractionRoot var5 = var0.root;
                return !var0.closed
                    && var5.expectation == var1
                    && (
                        var1 == null
                            || !var1.closed && var1.repairGeneration == var2 && OwnerRepairGenerationAuthority.identityMatches(var1.actual, var2, var3)
                    )
                    && !var5.closed
                    && !var5.retired
                    && nativeObservationReadLive(var5)
                    && !var5.serverState.closed
                    && !var5.ownerState.closed
                    && !var5.readCut.closed
                    && var5.thread == Thread.currentThread()
                    && var5.ownerState.current == var5
                    && var5.current == var0
                    && SERVERS.get(var5.server) == var5.serverState
                    && var5.serverState.epoch == var5.serverEpoch
                    && var5.ownerState.epoch == var5.ownerEpoch
                    && var5.projectionSlot >= 0
                    && ACTIVE_ROOT_PROJECTIONS[var5.projectionSlot] == var5;
            }
        }
    }

    private static synchronized boolean installRootProjection(FoodWeaponSourceLedger.InteractionRoot var0) {
        if (!var0.closed && !var0.retired && !var0.readCut.closed && (var0.expectation == null || !var0.expectation.closed)) {
            for (int var1 = 0; var1 < 4096; var1++) {
                if (ACTIVE_ROOT_PROJECTIONS[var1] == null) {
                    ACTIVE_ROOT_PROJECTIONS[var1] = var0;
                    var0.projectionSlot = var1;
                    return true;
                }
            }

            return false;
        } else {
            return false;
        }
    }

    private static synchronized void removeRootProjection(FoodWeaponSourceLedger.InteractionRoot var0) {
        var0.readCut.close();
        var0.readAliases.clear();
        if (var0.projectionSlot >= 0 && ACTIVE_ROOT_PROJECTIONS[var0.projectionSlot] == var0) {
            ACTIVE_ROOT_PROJECTIONS[var0.projectionSlot] = null;
        }

        var0.projectionSlot = -1;
    }

    private static synchronized FoodWeaponSourceLedger.InteractionRoot projectedRootForPlayer(ServerPlayer var0) {
        for (int var1 = 0; var1 < 4096; var1++) {
            FoodWeaponSourceLedger.InteractionRoot var2 = ACTIVE_ROOT_PROJECTIONS[var1];
            if (var2 != null
                && var2.player == var0
                && !var2.closed
                && !var2.retired
                && !var2.serverState.closed
                && !var2.ownerState.closed
                && var2.ownerState.current == var2) {
                return var2;
            }
        }

        return null;
    }

    private static FoodWeaponSourceLedger.PrimaryReadWindow beginPrimaryRead(DamageSource var0, ServerPlayer var1) {
        for (int var2 = 0; var2 < 4096; var2++) {
            FoodWeaponSourceLedger.InteractionRoot var3;
            FoodWeaponSourceLedger.OriginalInteractionBinding var4;
            synchronized (FoodWeaponSourceLedger.class) {
                var3 = ACTIVE_ROOT_PROJECTIONS[var2];
                var4 = var3 == null ? null : var3.current;
                if (var4 == null || var3.player != var1 || var4.source != var0) {
                    continue;
                }
            }

            if (projectedBindingLive(var4)) {
                synchronized (FoodWeaponSourceLedger.class) {
                    if (ACTIVE_ROOT_PROJECTIONS[var2] == var3
                        && var3.current == var4
                        && var3.thread == Thread.currentThread()
                        && !var3.closed
                        && !var3.retired
                        && !var3.readCut.closed) {
                        return new FoodWeaponSourceLedger.PrimaryReadWindow(var3);
                    }
                }
            }
        }

        return null;
    }

    private static void setOwnerInteraction(FoodWeaponSourceLedger.OwnerState var0, FoodWeaponSourceLedger.InteractionRoot var1) {
        var0.interactionChange = new Object();
        var0.current = var1;
    }

    public static synchronized void invalidateOwnerReadProjection(ServerPlayer var0) {
        for (int var1 = 0; var1 < 4096; var1++) {
            FoodWeaponSourceLedger.InteractionRoot var2 = ACTIVE_ROOT_PROJECTIONS[var1];
            if (var2 != null && var2.player == var0) {
                var2.readCut.change();
            }
        }
    }

    private static boolean rootReadReceiver(FoodWeaponSourceLedger.InteractionRoot var0, Object var1) {
        if (var0 != null && var1 != null) {
            if (var0.player != var1 && var0.server != var1 && (var0.expectation == null || var0.expectation.actual != var1) && !var0.readAliases.contains(var1)
                )
             {
                FoodWeaponSourceLedger.OriginalInteractionBinding var2 = var0.current;

                for (int var3 = 16; var2 != null && var3 > 0; var3--) {
                    if (var2.source == var1 || var2.target == var1) {
                        return true;
                    }

                    var2 = var2.parent;
                }

                return false;
            } else {
                return true;
            }
        } else {
            return false;
        }
    }

    public static synchronized void observeOriginalReadAlias(Object var0, Object var1) {
        if (var1 != null) {
            for (int var2 = 0; var2 < 4096; var2++) {
                FoodWeaponSourceLedger.InteractionRoot var3 = ACTIVE_ROOT_PROJECTIONS[var2];
                if (rootReadReceiver(var3, var0) && !var3.readAliases.bind(var1)) {
                    var3.readCut.close();
                }
            }
        }
    }

    public static synchronized void invalidateRawReadProjection(Object var0) {
        for (int var1 = 0; var1 < 4096; var1++) {
            FoodWeaponSourceLedger.InteractionRoot var2 = ACTIVE_ROOT_PROJECTIONS[var1];
            if (rootReadReceiver(var2, var0)) {
                var2.readCut.change();
            }
        }
    }

    public static synchronized void invalidateProviderReadProjection(Object var0) {
        invalidateRawReadProjection(var0);
    }

    public static synchronized void invalidateTagReadProjection(Tag var0) {
        invalidateRawReadProjection(var0);
    }

    public static synchronized void invalidateServerReadProjection(MinecraftServer var0) {
        for (int var1 = 0; var1 < 4096; var1++) {
            FoodWeaponSourceLedger.InteractionRoot var2 = ACTIVE_ROOT_PROJECTIONS[var1];
            if (var2 != null && var2.server == var0) {
                var2.readCut.change();
            }
        }
    }

    public static synchronized void invalidateSourceReadProjection(DamageSource var0) {
        for (int var1 = 0; var1 < 4096; var1++) {
            FoodWeaponSourceLedger.InteractionRoot var2 = ACTIVE_ROOT_PROJECTIONS[var1];
            if (rootReadReceiver(var2, var0)) {
                var2.readCut.change();
            }
        }
    }

    public static synchronized void invalidateTargetReadProjection(Entity var0) {
        for (int var1 = 0; var1 < 4096; var1++) {
            FoodWeaponSourceLedger.InteractionRoot var2 = ACTIVE_ROOT_PROJECTIONS[var1];
            if (rootReadReceiver(var2, var0)) {
                var2.readCut.change();
            }
        }
    }

    public static synchronized void invalidateWeaponReadProjection(ItemStack var0) {
        for (int var1 = 0; var1 < 4096; var1++) {
            FoodWeaponSourceLedger.InteractionRoot var2 = ACTIVE_ROOT_PROJECTIONS[var1];
            if (var2 != null && var2.expectation != null && var2.expectation.actual == var0) {
                var2.readCut.change();
            }
        }
    }

    private static boolean expectationMatches(FoodWeaponSourceLedger.WeaponExpectation var0, ItemStack var1) {
        if (var0 != null
            && !var0.closed
            && var0.actual == var1
            && var0.active == null
            && OwnerFullRepairGuard.permits(var1)
            && OwnerRepairGenerationAuthority.matches(var1, var0.repairGeneration)) {
            Object var2 = var0.cursor.position();
            byte[] var3 = canonical(NativeFoundationItemData.image(var1));
            return !var0.closed
                && OwnerRepairGenerationAuthority.matches(var1, var0.repairGeneration)
                && OwnerFullRepairGuard.permits(var1)
                && var2 == var0.cursor.position()
                && var0.active == null
                && var0.cursor.matches(var3);
        } else {
            return false;
        }
    }

    private static boolean writerCaller(Class<?> var0) {
        return NATIVE_CALLER.walk(
            var1 -> var1.filter(var0xx -> var0xx.getDeclaringClass() != FoodWeaponSourceLedger.class)
                    .findFirst()
                    .map(var1x -> var1x.getDeclaringClass().getNestHost() == var0)
                    .orElse(false)
        );
    }

    static FoodWeaponSourceLedger.OwnerWriterPublication openOwnerWriterPublication(
        FoodWeaponSourceLedger.OriginalInteractionBinding var0,
        FoodWeaponSourceLedger.AttemptBatchQuote var1,
        FoodWeaponSourceLedger.AttemptBatchAdmission var2,
        FoodWeaponSourceLedger.AttemptBatchCommit var3,
        FoodWeaponFinalDamage.OffenseReceipt var4
    ) {
        if (writerCaller(FoodWeaponFinalDamage.class)
            && projectedBindingLive(var0)
            && var1 != null
            && var2 != null
            && var4 != null
            && var1.binding == var0
            && var3 != null
            && var3.batch == var1
            && var2.preparation.result == var3
            && var2.preparation.state == var0.root.serverState
            && (
                var3.status == FoodWeaponSourceLedger.AttemptBatchCommitStatus.COMMITTED
                    || var3.status == FoodWeaponSourceLedger.AttemptBatchCommitStatus.ALREADY_COMMITTED
            )
            && var0.root.expectation != null
            && !var0.root.expectation.closed
            && (
                var0.root.writerPublication == null
                    || var0.parent != null && originalBindingAncestryLive(var0) && var0.root.writerPublication == acceptedOriginalPublication(var0.parent)
            )) {
            if (!FoodWeaponFinalDamage.matchesOwnerWriterPublication(var4, var0, var1, var2, var3)) {
                return null;
            } else {
                synchronized (FoodWeaponSourceLedger.class) {
                    if (var0.root.thread == Thread.currentThread()
                        && !var0.closed
                        && var0.root.current == var0
                        && var0.root.expectation != null
                        && !var0.root.expectation.closed
                        && (
                            var0.root.writerPublication == null
                                || var0.parent != null && var0.root.writerPublication == acceptedOriginalPublication(var0.parent)
                        )) {
                        FoodWeaponSourceLedger.BatchPreparation var6 = var2.preparation;
                        if (var6.state.claims != (var6.replacement == null ? var6.originalClaims : var6.replacement)) {
                            return null;
                        } else {
                            FoodWeaponSourceLedger.OwnerWriterPublication var7 = new FoodWeaponSourceLedger.OwnerWriterPublication(var0, var1, var2, var3, var4);
                            var0.root.writerPublication = var7;
                            return var7;
                        }
                    } else {
                        return null;
                    }
                }
            }
        } else {
            return null;
        }
    }

    static boolean selectOwnerWriterLeaf(FoodWeaponSourceLedger.OwnerWriterPublication var0, Object var1, int var2, Object var3, Object var4, int var5) {
        FoodWeaponSourceLedger.WriterPlanningCut var6 = writerPlanningCut(var0);
        FoodWeaponSourceLedger.WriterSelection var7 = var0 == null ? null : var0.selection;
        if (!writerCaller(FoodWeaponFinalDamage.class)
            || !ownerWriterPublicationContinuation(var0)
            || !writerPublicationUnchanged(var0, var6)
            || var1 == null
            || var3 == null
            || var4 == null
            || var2 < var0.producerOrdinal
            || var2 == var0.producerOrdinal && (var1 != var0.producer || var5 <= var0.leafOrdinal)
            || var5 < 0
            || var0.expectation.active != null
            || var0.repairObservation != null) {
            return false;
        } else if (FoodWeaponFinalDamage.matchesOwnerWriterSelection(var0.receipt, var1, var2, var3, var4, var5)
            && ownerWriterPublicationContinuation(var0)
            && writerPublicationUnchanged(var0, var6)
            && var0.selection == var7
            && var0.expectation.active == null
            && var0.repairObservation == null) {
            if (var0.playerWriter != null) {
                if (!var0.playerWriter.plan.published) {
                    return false;
                }

                closeOwnerPlayerWriter(var0.playerWriter);
                var0.playerWriter = null;
            }

            if (ownerWriterPublicationLive(var0) && writerPublicationUnchanged(var0, var6)) {
                FoodWeaponSourceLedger.WriterSelection var8 = new FoodWeaponSourceLedger.WriterSelection(var1, var2, var3, var4, var5);
                var0.selection = var8;
                var0.producer = var1;
                var0.producerOrdinal = var2;
                var0.commit = var3;
                var0.leaf = var4;
                var0.leafOrdinal = var5;
                var0.stepOrdinal = -1;
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    private static boolean ownerWriterSelectionLive(FoodWeaponSourceLedger.OwnerWriterPublication var0) {
        FoodWeaponSourceLedger.WriterPlanningCut var1 = writerPlanningCut(var0);
        if (ownerWriterPublicationLive(var0) && writerPlanningUnchanged(var0, var1)) {
            FoodWeaponSourceLedger.WriterSelection var2 = var1.selection;
            return FoodWeaponFinalDamage.matchesOwnerWriterPublication(var0.receipt, var0.binding, var0.batch, var0.admission, var0.result)
                && writerPlanningUnchanged(var0, var1)
                && FoodWeaponFinalDamage.matchesOwnerWriterSelection(
                    var0.receipt, var2.producer, var2.producerOrdinal, var2.commit, var2.leaf, var2.leafOrdinal
                )
                && ownerWriterPublicationLive(var0)
                && writerPlanningUnchanged(var0, var1);
        } else {
            return false;
        }
    }

    private static boolean ownerWriterPublicationLive(FoodWeaponSourceLedger.OwnerWriterPublication var0) {
        return var0 != null
            && (
                var0.fieldSequenceBinding == null
                    || var0.fieldSequenceBinding.extraAuthority && OwnerFieldExpectedAuthority.covered(var0.fieldSequenceBinding.prepared)
            )
            && !var0.closed
            && var0.thread == Thread.currentThread()
            && projectedBindingLive(var0.binding)
            && !var0.expectation.closed
            && var0.binding.root.expectation == var0.expectation
            && var0.binding.root.writerPublication == var0
            && !var0.closed
            && (var0.selection == null || !var0.selection.revoked);
    }

    static FoodWeaponSourceLedger.OwnerWriterStep planOwnerTimerWriter(
        FoodWeaponSourceLedger.OwnerWriterPublication var0, Object var1, Object var2, int var3, String var4, String var5, Tag var6
    ) {
        FoodWeaponSourceLedger.WriterPlanningCut var7 = writerPlanningCut(var0);
        Object var8 = var0 == null ? null : var0.binding.root.interactionChange;
        Object var9 = var0 == null ? null : var0.expectation.cursor.position();
        int var10 = var0 == null ? -1 : var0.stepOrdinal;
        if (writerCaller(FoodWeaponHitPlan.class)
            && ownerWriterSelectionLive(var0)
            && writerPlanningUnchanged(var0, var7)
            && var7.selection.commit == var1
            && var7.selection.leaf == var2
            && var0.commit == var1
            && var0.leaf == var2
            && var3 == var0.stepOrdinal + 1
            && var6 != null
            && var5 != null
            && var5.length() <= 1024
            && (var4 == null || var4.length() <= 1024)
            && var0.expectation.active == null
            && var0.repairObservation == null
            && expectationMatches(var0.expectation, var0.expectation.actual)) {
            if (var1 instanceof FoodWeaponHitPlan.FinalEffectsCommit var11
                && var0.fieldPreparation != null
                && var0.fieldSequenceBinding != null
                && var0.fieldPreparation.lineage == var0.fieldSequenceBinding.sequence.lineage
                && OwnerFieldExpectedAuthority.session(var0.fieldPreparation) == var0.fieldSequenceBinding.sequence) {
                FoodWeaponHitPlan.TimerSemanticStep var12 = FoodWeaponHitPlan.timerSemanticAtOriginal(var11, var2, var0, var3, var5);
                FoodWeaponHitPlan.TimerSemanticStep var13 = FoodWeaponHitPlan.timerSemanticAtSessionOriginal(var0.fieldSequenceBinding.sequence, var2, var5);
                if (var12 != null
                    && var12 == var13
                    && OwnerFieldExpectedAuthority.timerWriterExpected(var0.fieldPreparation, var0.fieldSequenceBinding.sequence, var2, var12)
                    && Objects.equals(var4, FoodWeaponEvolution.planHitTimerProfile((Profile)var12.projection().program().profile()))) {
                    Tag var14 = OwnerTimerSemanticAuthority.materialize(var12.value());
                    if (var14 == null) {
                        return null;
                    }

                    CompoundTag var15 = projectTimerImage(var0.expectation.expected, var4, var5, var14);
                    if (!canonicalInputValid(var15)) {
                        return null;
                    }

                    FoodWeaponSourceLedger.OwnerWriterStep var16 = new FoodWeaponSourceLedger.OwnerWriterStep(
                        var0,
                        FoodWeaponSourceLedger.OwnerWriterKind.EVOLUTION_TIMER,
                        var15,
                        projectTimerEntryImage(var0.expectation.expected, var4),
                        var5,
                        var14,
                        null,
                        var7.selection
                    );
                    if (ownerWriterSelectionLive(var0)
                        && writerPlanningUnchanged(var0, var7)
                        && var16.selection == var7.selection
                        && var0.binding.root.interactionChange == var8
                        && var0.expectation.cursor.position() == var9
                        && var0.stepOrdinal == var10
                        && var0.expectation.active == null
                        && var0.repairObservation == null) {
                        var0.stepOrdinal = var3;
                        var0.expectation.active = var16;
                        return var16;
                    }

                    return null;
                }

                return null;
            }

            return null;
        } else {
            return null;
        }
    }

    static FoodWeaponSourceLedger.OwnerWriterStep planOwnerWolfTokenWriter(
        FoodWeaponSourceLedger.OwnerWriterPublication var0, Object var1, Object var2, int var3, UUID var4
    ) {
        FoodWeaponSourceLedger.WriterPlanningCut var5 = writerPlanningCut(var0);
        Object var6 = var0 == null ? null : var0.binding.root.interactionChange;
        Object var7 = var0 == null ? null : var0.expectation.cursor.position();
        int var8 = var0 == null ? -1 : var0.stepOrdinal;
        if (writerCaller(FoodSocketPowers.class)
            && ownerWriterSelectionLive(var0)
            && writerPlanningUnchanged(var0, var5)
            && var5.selection.commit == var1
            && var5.selection.leaf == var2
            && var0.commit == var1
            && var0.leaf == var2
            && var4 != null
            && var3 == var0.stepOrdinal + 1
            && var0.expectation.active == null
            && var0.repairObservation == null
            && expectationMatches(var0.expectation, var0.expectation.actual)) {
            CompoundTag var9 = projectWolfTokenImage(var0.expectation.expected, var4);
            if (!canonicalInputValid(var9)) {
                return null;
            } else {
                FoodWeaponSourceLedger.OwnerWriterStep var10 = new FoodWeaponSourceLedger.OwnerWriterStep(
                    var0,
                    FoodWeaponSourceLedger.OwnerWriterKind.WOLF_TOKEN,
                    var9,
                    projectWolfEntryImage(var0.expectation.expected),
                    "FTBossFoodSocketWeaponToken",
                    null,
                    var4,
                    var5.selection
                );
                if (ownerWriterSelectionLive(var0)
                    && writerPlanningUnchanged(var0, var5)
                    && var10.selection == var5.selection
                    && var0.binding.root.interactionChange == var6
                    && var0.expectation.cursor.position() == var7
                    && var0.stepOrdinal == var8
                    && var0.expectation.active == null
                    && var0.repairObservation == null) {
                    var0.stepOrdinal = var3;
                    var0.expectation.active = var10;
                    return var10;
                } else {
                    return null;
                }
            }
        } else {
            return null;
        }
    }

    private static CompoundTag projectTimerImage(CompoundTag var0, String var1, String var2, Tag var3) {
        return NativeSourceLedgerItemData.projectTimer(var0, var1, var2, var3);
    }

    private static CompoundTag projectWolfTokenImage(CompoundTag var0, UUID var1) {
        return NativeSourceLedgerItemData.projectWolfToken(var0, var1);
    }

    private static CompoundTag projectTimerEntryImage(CompoundTag var0, String var1) {
        return NativeSourceLedgerItemData.projectTimerEntry(var0, var1);
    }

    private static CompoundTag projectWolfEntryImage(CompoundTag var0) {
        return NativeSourceLedgerItemData.projectWolfEntry(var0);
    }

    private static CompoundTag plannedCompound(CompoundTag var0, String var1) {
        if (var0.contains(var1, 10)) {
            return var0.getCompound(var1);
        } else {
            CompoundTag var2 = new CompoundTag();
            var0.put(var1, var2);
            return var2;
        }
    }

    private static boolean ownerWriterStepLive(FoodWeaponSourceLedger.OwnerWriterStep var0) {
        return var0 != null
                && !var0.closed
                && ownerWriterPublicationLive(var0.publication)
                && var0.publication.selection == var0.selection
                && var0.publication.expectation.active == var0
                && var0.publication.stepOrdinal == var0.ordinal
                && var0.position == var0.publication.expectation.cursor.position()
                && var0.transition == var0.publication.binding.root.interactionChange
            ? ownerWriterSelectionLive(var0.publication)
                && !var0.closed
                && var0.publication.selection == var0.selection
                && var0.selection
                    .matches(
                        var0.publication.producer,
                        var0.publication.producerOrdinal,
                        var0.publication.commit,
                        var0.publication.leaf,
                        var0.publication.leafOrdinal
                    )
                && var0.publication.expectation.active == var0
                && var0.publication.stepOrdinal == var0.ordinal
                && var0.position == var0.publication.expectation.cursor.position()
                && var0.transition == var0.publication.binding.root.interactionChange
            : false;
    }

    private static boolean originalUuidArgument(UUID var0, UUID var1) {
        return var0 != null && var0 == var1;
    }

    public static void ownerWriterUuidEntered(FoodWeaponSourceLedger.OwnerWriterStep var0, ItemStack var1, CompoundTag var2, String var3, UUID var4) {
        if (ownerWriterStepLive(var0)
            && var0.kind == FoodWeaponSourceLedger.OwnerWriterKind.WOLF_TOKEN
            && !var0.entered
            && var0.publication.expectation.actual == var1
            && var2 != null
            && var0.key.equals(var3)
            && originalUuidArgument(var0.originalUuid, var4)) {
            CompoundTag var5 = NativeSourceLedgerItemData.rawCustomDataForObservation(var1);
            byte[] var6 = canonical(NativeFoundationItemData.image(var1));
            if (var5 == var2 && Arrays.equals(var0.entryImage, var6) && ownerWriterStepLive(var0)) {
                var0.receiver = var2;
                var0.entered = true;
            }
        }
    }

    public static void ownerWriterUuidReturned(FoodWeaponSourceLedger.OwnerWriterStep var0, ItemStack var1, CompoundTag var2, String var3, UUID var4) {
        if (var0 != null
            && var0.kind == FoodWeaponSourceLedger.OwnerWriterKind.WOLF_TOKEN
            && var0.key.equals(var3)
            && originalUuidArgument(var0.originalUuid, var4)) {
            ownerWriterPutReturned(var0, var1, var2);
        }
    }

    public static synchronized void closeOwnerWriterUuidStep(FoodWeaponSourceLedger.OwnerWriterStep var0) {
        closeOwnerWriterStep(var0);
    }

    public static void ownerWriterPutEntered(FoodWeaponSourceLedger.OwnerWriterStep var0, ItemStack var1, CompoundTag var2, String var3, Tag var4) {
        if (var0 != null
            && !var0.closed
            && !var0.entered
            && var0.kind == FoodWeaponSourceLedger.OwnerWriterKind.EVOLUTION_TIMER
            && var0.value != null
            && ownerWriterStepLive(var0)
            && var0.transition == var0.publication.binding.root.interactionChange
            && var0.publication.expectation.active == var0
            && var0.publication.expectation.actual == var1
            && var2 != null
            && var0.key.equals(var3)
            && var4 != null
            && Arrays.equals(canonical(var0.value), canonical(var4))) {
            Object var5 = var0.publication.binding.root.interactionChange;
            CompoundTag var6 = NativeSourceLedgerItemData.rawCustomDataForObservation(var1);
            if (var6 != null
                && (var0.kind == FoodWeaponSourceLedger.OwnerWriterKind.WOLF_TOKEN ? var6 == var2 : var6.get("FTBossFoodEvolution") == var2)
                && Arrays.equals(var0.entryImage, canonical(NativeFoundationItemData.image(var1)))
                && ownerWriterStepLive(var0)
                && !var0.closed
                && var0.publication.expectation.active == var0
                && var0.transition == var0.publication.binding.root.interactionChange
                && var5 == var0.publication.binding.root.interactionChange
                && var0.position == var0.publication.expectation.cursor.position()) {
                var0.receiver = var2;
                var0.entered = true;
            }
        }
    }

    public static void ownerWriterPutReturned(FoodWeaponSourceLedger.OwnerWriterStep var0, ItemStack var1, CompoundTag var2) {
        if (var0 != null && !var0.closed) {
            boolean var3 = false;

            try {
                if (!var0.entered
                    || var2 != var0.receiver
                    || !ownerWriterStepLive(var0)
                    || var0.publication.expectation.active != var0
                    || var0.publication.expectation.actual != var1
                    || var0.transition != var0.publication.binding.root.interactionChange) {
                    return;
                }

                FoodWeaponSourceLedger.WeaponExpectation var4 = var0.publication.expectation;
                byte[] var5 = canonical(NativeFoundationItemData.image(var1));
                if (!ownerWriterStepLive(var0)
                    || var0.closed
                    || var4.active != var0
                    || var0.transition != var0.publication.binding.root.interactionChange
                    || !var4.cursor.advance(var0.position, var0.before, var0.after, var5)) {
                    return;
                }

                var4.expected = var0.planned;
                var3 = true;
                var0.publication.binding.root.readCut.change();
            } finally {
                if (!var3 && var0.publication.selection == var0.selection && var0.publication.expectation.active == var0) {
                    var0.publication.closed = true;
                }

                var0.closed = true;
                if (var0.publication.expectation.active == var0) {
                    var0.publication.expectation.active = null;
                }
            }
        }
    }

    public static synchronized void closeOwnerWriterStep(FoodWeaponSourceLedger.OwnerWriterStep var0) {
        if (var0 != null && !var0.closed) {
            var0.closed = true;
            if (var0.publication.selection == var0.selection && var0.publication.expectation.active == var0) {
                var0.publication.closed = true;
            }

            if (var0.publication.expectation.active == var0) {
                var0.publication.expectation.active = null;
            }
        }
    }

    static boolean ownerWriterContinuation(FoodWeaponSourceLedger.OwnerWriterPublication var0) {
        return ownerWriterSelectionLive(var0)
            && var0.expectation.active == null
            && var0.repairObservation == null
            && (var0.playerWriter == null || var0.playerWriter.plan.published);
    }

    static synchronized void closeOwnerWriterPublication(FoodWeaponSourceLedger.OwnerWriterPublication var0) {
        if (var0 != null) {
            var0.closed = true;
            var0.observation.close();
            if (var0.binding.root.writerPublication == var0) {
                FoodWeaponSourceLedger.OwnerWriterPublication var1 = var0.parentPublication;
                var0.binding.root.writerPublication = var1 != null && !var1.closed && var1.binding.root == var0.binding.root ? var1 : null;
            }

            if (var0.repairObservation != null) {
                closeOwnerRepairObservation(var0.repairObservation);
            }

            if (var0.playerWriter != null) {
                closeOwnerPlayerWriter(var0.playerWriter);
            }

            if (var0.expectation.active != null && var0.expectation.active.publication == var0) {
                closeOwnerWriterStep(var0.expectation.active);
            }
        }
    }

    private static FoodWeaponSourceLedger.BatchPreparation rejectedPreparation(FoodWeaponSourceLedger.AttemptBatchCommitStatus var0) {
        return new FoodWeaponSourceLedger.BatchPreparation(null, null, rejectedBatch(var0));
    }

    public static void ownerTridentOriginalCopy(ThrownTrident var0, ItemStack var1, ItemStack var2) {
        boolean var3 = NATIVE_CALLER.walk(
            var0x -> var0x.skip(1L)
                    .findFirst()
                    .map(var0xx -> var0xx.getDeclaringClass() == AbstractArrow.class && var0xx.getMethodName().contains("ftboss$ownerTridentOriginalCopy"))
                    .orElse(false)
        );
        if (var3 && var0 != null && var1 != null && var2 != null && var2 != var1 && var0 instanceof OwnerTridentCarrierAccess var4) {
            FoodWeaponSourceLedger.NativeOperation var5 = NATIVE_OPERATION.get();
            FoodWeaponSourceLedger.TicketState var6 = TRIDENT_TICKET.get();
            Object var7 = liveNative(var5)
                    && var5.kind == FoodWeaponSourceLedger.NativeKind.TRIDENT
                    && var5.weapon == var1
                    && var5.current != null
                    && var5.current.worn
                ? var5.current
                : (var6 != null && var6.actualWeapon() == var1 ? var6 : null);
            if (var7 != null) {
                var4.ftboss$ownerInstallTridentLineage(new FoodWeaponSourceLedger.TridentCarrierLineage(var0, var1, var2, var7));
            }
        }
    }

    private static FoodWeaponSourceLedger.TridentCarrierLineage tridentCarrierLineage(
        ThrownTrident var0, ItemStack var1, FoodWeaponSourceLedger.TridentCarrierLineage var2
    ) {
        return var2 != null && !var2.observationRejected && var2.entity == var0 && var2.copied == var1 && var1 != null ? var2 : null;
    }

    private static void rejectTridentExtra(
        ThrownTrident var0,
        FoodWeaponSourceLedger.TridentCarrierLineage var1,
        FoodWeaponSourceLedger.NativeReceipt var2,
        FoodWeaponSourceLedger.NativePaidWeapon var3,
        FoodWeaponSourceLedger.TicketState var4,
        FoodWeaponSourceLedger.TridentTicketBefore var5,
        Throwable var6
    ) {
        if (var1 != null
            && var1.entity == var0
            && (var2 != null ? var1.origin == var2 : (var4 != null ? var1.origin == var4 : var3 == null || var1.origin == var3.receipt))) {
            var1.observationRejected = true;
            if (var1.observationFailure == null) {
                var1.observationFailure = var6;
            }
        }

        if (var2 != null && var2.operation.thread == Thread.currentThread() && (var2.entity == null || var2.entity == var0)) {
            var2.materialRejected = true;
            var2.consumed = true;
            var2.operation.invalid = true;
        }

        if (var3 != null && var3.carrier == var0 && var3.receipt.operation.thread == Thread.currentThread()) {
            var3.expectation.closed = true;
            var3.receipt.materialRejected = true;
            var3.receipt.operation.invalid = true;
        }

        if (var4 != null && var5 != null && var5.ticket == var4.ticket() && var5.actual == var4.actualWeapon() && var5.thread == Thread.currentThread()) {
            var5.observationRejected = true;
            if (TRIDENT_BEFORE.get() == var5) {
                TRIDENT_BEFORE.remove();
            }

            if (TRIDENT_TICKET.get() == var4) {
                TRIDENT_TICKET.remove();
            }
        }
    }

    private static boolean tridentRawMatchesExpected(ItemStack var0, CompoundTag var1) {
        return var1 != null && FoodWeaponExpectedTrace.same(NativeExpectedRawFields.observe(var1), originalRepairRawFields(var0));
    }

    private static synchronized UUID ownerEpoch(FoodWeaponSourceLedger.ServerState var0, UUID var1) {
        FoodWeaponSourceLedger.OwnerState var2 = var0.ownerEpochs.get(var1);
        return var2 == null ? null : var2.epoch;
    }

    private static synchronized FoodWeaponSourceLedger.OwnerState ensureOwnerState(FoodWeaponSourceLedger.ServerState var0, UUID var1) {
        return var0.ownerEpochs.computeIfAbsent(var1, var0x -> new FoodWeaponSourceLedger.OwnerState(UUID.randomUUID()));
    }

    private static boolean interactionRootLive(FoodWeaponSourceLedger.InteractionRoot var0, ServerPlayer var1) {
        FoodWeaponSourceLedger.WeaponExpectation var2;
        Object var3;
        OwnerLineageRevocationStore.Lease var4;
        synchronized (FoodWeaponSourceLedger.class) {
            if (var0 == null || var1 == null || var0.player != var1 || var0.thread != Thread.currentThread()) {
                return false;
            }

            var2 = var0.expectation;
            var3 = var2 == null ? null : var2.repairGeneration;
            var4 = var2 == null ? null : OwnerFullRepairGuard.lease(var2.actual);
        }

        if (var2 != null && !OwnerRepairGenerationAuthority.matches(var2.actual, var3)) {
            return false;
        } else {
            synchronized (FoodWeaponSourceLedger.class) {
                if (var0.expectation == var2
                    && (
                        var2 == null
                            || !var2.closed && var2.repairGeneration == var3 && OwnerRepairGenerationAuthority.identityMatches(var2.actual, var3, var4)
                    )
                    && !var0.retired
                    && !var0.closed
                    && nativeObservationReadLive(var0)
                    && var0.server == server(var1)
                    && var0.server.isSameThread()
                    && var0.tick == clock(var1)
                    && !var1.isRemoved()
                    && var1.isAlive()
                    && !var1.isSpectator()) {
                    FoodWeaponSourceLedger.ServerState var6 = SERVERS.get(var0.server);
                    return var6 == var0.serverState
                        && !var6.closed
                        && !var0.ownerState.closed
                        && var6.epoch.equals(var0.serverEpoch)
                        && var6.ownerEpochs.get(var1.getUUID()) == var0.ownerState
                        && var0.ownerState.epoch.equals(var0.ownerEpoch)
                        && var0.ownerState.current == var0;
                } else {
                    return false;
                }
            }
        }
    }

    private static boolean interactionMatches(FoodWeaponSourceLedger.OriginalInteractionBinding var0, CompoundTag var1, ServerPlayer var2) {
        return var0 != null && var0.childCall != null
            ? var0.root.player == var2
                && var0.root.current == var0
                && var0.snapshot != null
                && var1 != null
                && var0.snapshot.equals(var1)
                && originalBindingAncestryLive(var0)
            : var0 != null
                && !var0.closed
                && var0.childCall == null
                && var0.snapshot != null
                && var0.depth > 0
                && var0.depth <= 16
                && interactionRootLive(var0.root, var2)
                && var0.root.current == var0
                && var1 != null
                && var0.snapshot.equals(var1)
                && (var0.root.nativeProjectileObservation != null
                    ? nativeProjectileInteractionViewAllows(var0, var1, var2)
                    : var1.hasUUID("NativeHurtId")
                    ? matchesNativeHurt(var0.source, var2, var0.target, var1)
                    : FoodWeaponRanged.interactionViewAllows(var2, var1, var0.source, var0.target));
    }

    private static boolean nativeProjectileInteractionViewAllows(
        FoodWeaponSourceLedger.OriginalInteractionBinding binding, CompoundTag snapshot, ServerPlayer player
    ) {
        FoodWeaponSourceLedger.NativeProjectileObservation observation = binding.root.nativeProjectileObservation;
        FoodWeaponSourceLedger.NativeProjectileInteraction scope = observation == null ? null : observation.scope;
        NativeVanillaImpact.RawImpact call = observation == null ? null : observation.call;
        return scope != null
            && call != null
            && !scope.closed
            && !observation.finished
            && !observation.failure.failed
            && scope.call == call
            && scope.observation == observation
            && scope.root == binding.root
            && scope.binding == binding
            && observation.root == binding.root
            && observation.binding == binding
            && observation.expectation == binding.root.expectation
            && binding.source == call.source
            && binding.target == call.target
            && binding.rawAmountBits == call.rawAmountBits
            && call.source.getEntity() == player
            && call.source.getDirectEntity() == call.carrier
            && call.source.is(DamageTypeTags.IS_PROJECTILE)
            && call.carrier.getOwner() == player
            && !call.carrier.isRemoved()
            && call.carrier.level() == player.level()
            && call.target.level() == player.level()
            && player.serverLevel().getEntity(call.carrier.getUUID()) == call.carrier
            && player.serverLevel().getEntity(call.target.getUUID()) == call.target
            && "NATIVE_PROJECTILE".equals(snapshot.getString("DamagePurpose"))
            && snapshot.hasUUID("NativeReceiptId")
            && snapshot.hasUUID("NativeEntityId")
            && snapshot.getUUID("NativeEntityId").equals(call.carrier.getUUID())
            && binding.root.readCut.acceptedImage != null
            && snapshot.equals(binding.root.readCut.acceptedImage)
            && binding.root.readCut.matches(binding.root.readCut.acceptedImage);
    }

    static FoodWeaponSourceLedger.OriginalInteractionBinding capturePrimaryInteractionBinding(
        CompoundTag var0, ServerPlayer var1, DamageSource var2, Entity var3
    ) {
        if (var0 != null && var1 != null && var2 != null && var3 != null && server(var1).isSameThread()) {
            for (int var4 = 0; var4 < 4096; var4++) {
                FoodWeaponSourceLedger.InteractionRoot var5;
                FoodWeaponSourceLedger.OriginalInteractionBinding var6;
                synchronized (FoodWeaponSourceLedger.class) {
                    var5 = ACTIVE_ROOT_PROJECTIONS[var4];
                    var6 = var5 == null ? null : var5.current;
                    if (var6 == null || var5.thread != Thread.currentThread() || var5.player != var1 || var6.source != var2 || var6.target != var3) {
                        continue;
                    }
                }

                if (projectedBindingLive(var6)) {
                    synchronized (FoodWeaponSourceLedger.class) {
                        if (ACTIVE_ROOT_PROJECTIONS[var4] == var5
                            && var5.current == var6
                            && !var5.closed
                            && !var5.retired
                            && !var6.closed
                            && (
                                var6.childCall != null
                                    ? var6.snapshot != null
                                        && var6.snapshot.equals(var0)
                                        && var6.parent != null
                                        && var6.parent.root == var5
                                        && var6.parent.childCall == var6.childCall
                                        && var6.parent.snapshot != null
                                        && var6.parent.snapshot.equals(var6.snapshot)
                                    : var5.readCut.matches(var0)
                            )) {
                            return var6;
                        }
                    }
                }
            }

            return null;
        } else {
            return null;
        }
    }

    static boolean matchesCleanupInteractionIdentity(
        FoodWeaponSourceLedger.OriginalInteractionBinding var0, FoodWeaponSourceLedger.OriginalInteractionBinding var1, ServerPlayer var2
    ) {
        if (var0 != null && var0 == var1 && var0.childCall != null) {
            return var0.root.player == var2 && var0.root.current == var0 && var0.snapshot != null && originalBindingAncestryLive(var0);
        } else if (var0 != null
            && var0 == var1
            && var2 != null
            && !var0.closed
            && var0.parent == null
            && var0.depth == 1
            && var0.childCall == null
            && var0.snapshot != null
            && !var0.snapshot.isEmpty()) {
            FoodWeaponSourceLedger.InteractionRoot var3 = var0.root;
            if (var3 != null
                && var3.player == var2
                && !var3.retired
                && !var3.closed
                && var3.current == var0
                && var3.thread == Thread.currentThread()
                && var3.server == server(var2)
                && var3.server.isSameThread()
                && var3.tick == clock(var2)) {
                FoodWeaponSourceLedger.ServerState var4 = SERVERS.get(var3.server);
                return var4 == var3.serverState
                    && var4.epoch.equals(var3.serverEpoch)
                    && var4.ownerEpochs.get(var2.getUUID()) == var3.ownerState
                    && var3.ownerState.epoch.equals(var3.ownerEpoch)
                    && var3.ownerState.current == var3;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    private static FoodWeaponSourceLedger.OriginalInteractionBinding currentInteraction(CompoundTag var0, ServerPlayer var1) {
        if (var1 == null) {
            return null;
        } else {
            FoodWeaponSourceLedger.ServerState var2 = SERVERS.get(server(var1));
            FoodWeaponSourceLedger.OwnerState var3 = var2 == null ? null : var2.ownerEpochs.get(var1.getUUID());
            FoodWeaponSourceLedger.InteractionRoot var4 = var3 == null ? null : var3.current;
            FoodWeaponSourceLedger.OriginalInteractionBinding var5 = var4 == null ? null : var4.current;
            return interactionMatches(var5, var0, var1) ? var5 : null;
        }
    }

    static FoodWeaponSourceLedger.OriginalInteractionBinding originalInteractionBinding(FoodWeaponSourceLedger.AttemptBatchQuote var0) {
        return var0 == null ? null : var0.binding;
    }

    static boolean matchesOriginalInteraction(FoodWeaponSourceLedger.OriginalInteractionBinding var0, CompoundTag var1, ServerPlayer var2) {
        return interactionMatches(var0, var1, var2) && (var0.childCall != null && originalBindingAncestryLive(var0) || attemptSourceValid(var1, var2));
    }

    private static boolean pendingEchoLifecycleCaller() {
        return NATIVE_CALLER.walk(
            var0 -> var0.filter(var0x -> var0x.getDeclaringClass() != FoodWeaponSourceLedger.class)
                    .findFirst()
                    .map(
                        var0x -> var0x.getDeclaringClass() == FoodSocketAttributePowers.class
                                && var0x.getMethodName().equals("damagePendingEcho")
                                && var0x.getDescriptor()
                                    .equals(
                                        "(Lnet/farmingtales/ftboss/item/FoodWeaponEchoQueue$Entry;Lnet/minecraft/nbt/CompoundTag;Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/world/entity/LivingEntity;J)V"
                                    )
                    )
                    .orElse(false)
        );
    }

    private static boolean pendingEchoRawHurtCaller() {
        return NATIVE_CALLER.walk(
            var0 -> var0.filter(var0x -> var0x.getDeclaringClass() != FoodWeaponSourceLedger.class)
                    .findFirst()
                    .map(
                        var0x -> var0x.getDeclaringClass() == FoodSocketAttributePowers.class
                                && var0x.getMethodName().equals("hurtPendingEchoRaw")
                                && var0x.getDescriptor()
                                    .equals(
                                        "(Lnet/farmingtales/ftboss/item/FoodSocketAttributePowers$PendingEchoDispatch;Lnet/farmingtales/ftboss/item/FoodWeaponSourceLedger$PendingEchoInteraction;)Z"
                                    )
                    )
                    .orElse(false)
        );
    }

    static FoodWeaponSourceLedger.PendingEchoInteraction beginPendingEchoInteraction(
        FoodSocketAttributePowers.PendingEchoDispatch var0, ServerPlayer var1, LivingEntity var2, DamageSource var3, float var4
    ) {
        if (pendingEchoLifecycleCaller()
            && var1 != null
            && var2 != null
            && var3 != null
            && Float.floatToRawIntBits(var4) == Float.floatToRawIntBits(2.0F)
            && var3.getEntity() == var1
            && var3.getDirectEntity() == null
            && var3.is(FoodWeaponDamageSources.SECONDARY)
            && var2.level() == var1.level()
            && var1.serverLevel().getEntity(var2.getUUID()) == var2
            && FoodSocketAttributePowers.matchesPendingEchoDispatch(var0, var1, var2, var3, var4)) {
            CompoundTag var5 = FoodSocketAttributePowers.pendingEchoDispatchSource(var0);
            if (var5 != null && !var5.isEmpty() && originalHandContinuousReadOnly(var5, var1)) {
                ItemStack var6 = FoodSocketAttributePowers.originalPendingEchoWeapon(var0);
                if (var6 == null) {
                    return null;
                } else {
                    FoodWeaponSourceLedger.InteractionRoot var7 = beginExternalInteraction(
                        var1, FoodWeaponSourceLedger.RootSite.DELAYED_ECHO, var3, var2, var5, var4, var6, null
                    );
                    return var7 == null ? null : new FoodWeaponSourceLedger.PendingEchoInteraction(var0, var7, var1, var2, var3, var4);
                }
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    private static boolean pendingEchoInteractionLive(FoodWeaponSourceLedger.PendingEchoInteraction var0) {
        if (var0 != null
            && !var0.closed
            && var0.dispatch != null
            && var0.root.site == FoodWeaponSourceLedger.RootSite.DELAYED_ECHO
            && interactionRootLive(var0.root, var0.player)) {
            FoodWeaponSourceLedger.OriginalInteractionBinding var1 = var0.binding;
            return var1 != null
                && !var1.closed
                && var1.root == var0.root
                && var0.root.current == var1
                && var1.parent == null
                && var1.depth == 1
                && var1.childCall == null
                && var1.source == var0.source
                && var1.target == var0.target
                && var1.rawAmountBits == Float.floatToRawIntBits(var0.rawAmount)
                && var1.snapshot != null
                && !var1.snapshot.isEmpty()
                && (
                    var0.lineage == null
                        ? originalHandContinuousReadOnly(var1.snapshot, var0.player)
                        : var0.lineage.dispatched && var0.lineage.snapshot.equals(var1.snapshot) && pendingEchoHandContinuous(var0.lineage, var0.player)
                );
        } else {
            return false;
        }
    }

    private static boolean pendingEchoOperandsMatch(
        FoodWeaponSourceLedger.PendingEchoInteraction var0,
        FoodSocketAttributePowers.PendingEchoDispatch var1,
        ServerPlayer var2,
        LivingEntity var3,
        DamageSource var4,
        float var5
    ) {
        return var0 != null
            && var0.dispatch == var1
            && var0.player == var2
            && var0.target == var3
            && var0.source == var4
            && Float.floatToRawIntBits(var0.rawAmount) == Float.floatToRawIntBits(var5)
            && Float.floatToRawIntBits(var5) == Float.floatToRawIntBits(2.0F);
    }

    static boolean hurtPendingEchoInteraction(
        FoodWeaponSourceLedger.PendingEchoInteraction var0,
        FoodSocketAttributePowers.PendingEchoDispatch var1,
        ServerPlayer var2,
        LivingEntity var3,
        DamageSource var4,
        float var5
    ) {
        if (pendingEchoRawHurtCaller()
            && pendingEchoOperandsMatch(var0, var1, var2, var3, var4, var5)
            && !var0.hurtEntered
            && !var0.hurtActive
            && pendingEchoInteractionLive(var0)) {
            var0.hurtEntered = true;
            var0.hurtActive = true;

            boolean var7;
            try {
                boolean var6 = var3.hurt(var4, var5);
                var0.hurtFinished = true;
                var7 = var6;
            } finally {
                var0.hurtActive = false;
            }

            return var7;
        } else {
            return false;
        }
    }

    static boolean matchesPendingEchoInteraction(
        FoodWeaponSourceLedger.PendingEchoInteraction var0,
        FoodSocketAttributePowers.PendingEchoDispatch var1,
        ServerPlayer var2,
        LivingEntity var3,
        DamageSource var4,
        float var5
    ) {
        return pendingEchoLifecycleCaller()
            && pendingEchoOperandsMatch(var0, var1, var2, var3, var4, var5)
            && var0.hurtEntered
            && var0.hurtFinished
            && !var0.hurtActive
            && pendingEchoInteractionLive(var0);
    }

    static synchronized void closePendingEchoInteraction(FoodWeaponSourceLedger.PendingEchoInteraction var0) {
        if (pendingEchoLifecycleCaller() && var0 != null && !var0.closed && var0.root.thread == Thread.currentThread() && var0.root.server.isSameThread()) {
            var0.closed = true;
            endInteraction(var0.root);
        }
    }

    private static boolean nativeProjectileImpactCaller(NativeVanillaImpact.RawImpact var0) {
        if (var0 != null && var0.carrier != null) {
            boolean var1 = var0.site == NativeVanillaImpact.OriginalSite.TRIDENT;
            if (var1 == (var0.carrier instanceof ThrownTrident) && (var1 || var0.site == NativeVanillaImpact.OriginalSite.ARROW)) {
                Class<?> var2 = var1 ? ThrownTrident.class : AbstractArrow.class;
                String var3 = var1 ? "tridentHurt" : "arrowHurt";
                String var4 = var1 ? "ftboss$nativeVanillaTridentRawHurt" : "ftboss$nativeVanillaArrowRawHurt";
                String var5 = var1 ? "ThrownTrident" : "AbstractArrow";
                return NATIVE_CALLER.walk(
                    var4x -> {
                        String originalEntry = var3;
                        String originalProjectileClass = var5;
                        Class<?> originalHost = var2;
                        String originalRedirect = var4;
                        boolean tridentRoute = originalHost == ThrownTrident.class;
                        int requiredFrames = tridentRoute ? 4 : 5;
                        List var5x = var4x.filter(var0xx -> var0xx.getDeclaringClass() != FoodWeaponSourceLedger.class).limit(requiredFrames).toList();
                        if (var5x.size() != requiredFrames) {
                            return false;
                        } else {
                            StackFrame var6 = (StackFrame)var5x.get(0);
                            StackFrame var7 = (StackFrame)var5x.get(1);
                            StackFrame var8 = (StackFrame)var5x.get(tridentRoute ? 2 : 3);
                            StackFrame var9 = (StackFrame)var5x.get(tridentRoute ? 3 : 4);
                            StackFrame arrowWrapper = (StackFrame)var5x.get(2);
                            return var6.getDeclaringClass() == NativeVanillaImpact.class
                                && var6.getMethodName().equals("hurt")
                                && var6.getDescriptor()
                                    .equals(
                                        "(Lnet/farmingtales/ftboss/item/NativeVanillaImpact$OriginalSite;Lnet/minecraft/world/entity/projectile/AbstractArrow;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/damagesource/DamageSource;F)Z"
                                    )
                                && var7.getDeclaringClass() == NativeVanillaImpact.class
                                && var7.getMethodName().equals(originalEntry)
                                && var7.getDescriptor()
                                    .equals(
                                        "(Lnet/minecraft/world/entity/projectile/"
                                            + originalProjectileClass
                                            + ";Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/damagesource/DamageSource;F)Z"
                                    )
                                && (tridentRoute || arrowWrapper.getDeclaringClass() == NativeEvergaleImpact.class
                                    && arrowWrapper.getMethodName().equals("arrowHurt")
                                    && arrowWrapper.getDescriptor().equals("(Lnet/minecraft/world/entity/projectile/AbstractArrow;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/damagesource/DamageSource;F)Z"))
                                && var8.getDeclaringClass() == originalHost
                                && (var8.getMethodName().equals(originalRedirect) || var8.getMethodName().endsWith("$" + originalRedirect))
                                && var8.getDescriptor().equals("(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/damagesource/DamageSource;F)Z")
                                && var9.getDeclaringClass() == originalHost
                                && var9.getMethodName().equals("onHitEntity")
                                && var9.getDescriptor().equals("(Lnet/minecraft/world/phys/EntityHitResult;)V");
                        }
                    }
                );
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    static FoodWeaponSourceLedger.NativeProjectileInteraction beginNativeProjectileImpact(NativeVanillaImpact.RawImpact var0) {
        return beginNativeProjectileImpact(var0, observeNativeProjectileImpact(var0));
    }

    static FoodWeaponSourceLedger.NativeProjectileInteraction beginNativeProjectileImpact(
        NativeVanillaImpact.RawImpact var0, FoodWeaponSourceLedger.NativeProjectileObservation var1
    ) {
        if (var1 != null && var1.call == var0 && !var1.begun && !var1.finished && !var1.failure.failed) {
            var1.begun = true;

            try {
                if (nativeProjectileImpactCaller(var0)
                    && !var0.closed()
                    && !var0.originalEntered()
                    && var0.target != null
                    && var0.source != null
                    && Float.isFinite(var0.amount)
                    && !(var0.amount <= 0.0F)
                    && var0.rawAmountBits == Float.floatToRawIntBits(var0.amount)
                    && var0.carrier.getOwner() instanceof ServerPlayer var2
                    && !var2.level().isClientSide
                    && server(var2).isSameThread()
                    && var0.carrier.level() == var2.level()
                    && var0.target.level() == var2.level()
                    && !var0.carrier.isRemoved()
                    && var2.serverLevel().getEntity(var0.carrier.getUUID()) == var0.carrier
                    && var0.source.getDirectEntity() == var0.carrier
                    && var0.source.getEntity() == var2
                    && var0.source.is(DamageTypeTags.IS_PROJECTILE)) {
                    CompoundTag var11 = resolveReadOnly(var0.source, var2);
                    if (!var11.isEmpty()
                        && "NATIVE_PROJECTILE".equals(var11.getString("DamagePurpose"))
                        && var11.hasUUID("NativeReceiptId")
                        && var11.hasUUID("NativeEntityId")
                        && var11.getUUID("NativeEntityId").equals(var0.carrier.getUUID())) {
                        ItemStack var4 = NativeFoundationItemData.decode(var11.getCompound("Weapon"));
                        String var5 = var11.getString("NativeEntry");
                        FoodWeaponSourceLedger.RootSite var6;
                        if (var0.site == NativeVanillaImpact.OriginalSite.TRIDENT) {
                            if (!"TRIDENT".equals(var5) || nativeKind(var4.getItem(), var4) != FoodWeaponSourceLedger.NativeKind.TRIDENT) {
                                return null;
                            }

                            var6 = FoodWeaponSourceLedger.RootSite.NATIVE_TRIDENT_IMPACT;
                        } else {
                            if (var0.carrier.getClass() != Arrow.class && var0.carrier.getClass() != SpectralArrow.class) {
                                return null;
                            }

                            if ("BOW".equals(var5) && nativeKind(var4.getItem(), var4) == FoodWeaponSourceLedger.NativeKind.BOW) {
                                var6 = FoodWeaponSourceLedger.RootSite.NATIVE_BOW_IMPACT;
                            } else {
                                if (!"CROSSBOW".equals(var5) || nativeKind(var4.getItem(), var4) != FoodWeaponSourceLedger.NativeKind.CROSSBOW) {
                                    return null;
                                }

                                var6 = FoodWeaponSourceLedger.RootSite.NATIVE_CROSSBOW_IMPACT;
                            }
                        }

                        FoodWeaponSourceLedger.NativePaidWeapon var7 = NativeVanillaImpact.originalOwnerPaidWeapon(var0);
                        if (var7 != null
                            && var7.carrier == var0.carrier
                            && var7.receipt.entity == var0.carrier
                            && var7.receipt.operation.player == var2
                            && var7.receipt.consumed
                            && var7.receipt.added
                            && var7.receipt.worn
                            && !var7.receipt.operation.invalid
                            && var7.receipt.token.equals(var11.getUUID("NativeReceiptId"))
                            && expectationMatches(var7.expectation, var7.receipt.operation.weapon)) {
                            FoodWeaponSourceLedger.InteractionRoot var8 = beginExternalInteraction(
                                var2, var6, var0.source, var0.target, var11, var0.amount, null, var7.expectation, var1
                            );
                            if (var8 == null) {
                                return null;
                            } else {
                                FoodWeaponSourceLedger.NativeProjectileInteraction var9 = new FoodWeaponSourceLedger.NativeProjectileInteraction(
                                    var0, var8, var1
                                );
                                var1.scope = var9;
                                return var9;
                            }
                        } else {
                            return null;
                        }
                    } else {
                        return null;
                    }
                } else {
                    return null;
                }
            } catch (Throwable var10) {
                var1.openingFailure = var10;
                closeNativeProjectileObservation(var1);
                var1.failure.fail(var0, 0, var10);
                throw var10;
            }
        } else {
            return null;
        }
    }

    static synchronized void closeNativeProjectileImpact(FoodWeaponSourceLedger.NativeProjectileInteraction var0, NativeVanillaImpact.RawImpact var1) {
        if (var0 != null && var0.observation != null) {
            closeNativeProjectileImpact(var0, var1, var0.observation);
        } else if (var0 != null
            && !var0.closed
            && var0.call == var1
            && nativeProjectileImpactCaller(var1)
            && var0.root.thread == Thread.currentThread()
            && var0.root.server.isSameThread()) {
            if (var0.binding.root == var0.root
                && var0.binding.source == var1.source
                && var0.binding.target == var1.target
                && var0.binding.rawAmountBits == var1.rawAmountBits) {
                var0.closed = true;
                endInteraction(var0.root);
            }
        }
    }

    static synchronized void closeNativeProjectileImpact(
        FoodWeaponSourceLedger.NativeProjectileInteraction var0, NativeVanillaImpact.RawImpact var1, FoodWeaponSourceLedger.NativeProjectileObservation var2
    ) {
        if (var2 != null && var2.call == var1 && (var0 == null || var2.scope == var0)) {
            if (!var2.finished && (var0 == null || !var0.closed)) {
                var2.finished = true;
                boolean var3 = false;

                try {
                    if (var0 == null
                        || var0.call != var1
                        || !nativeProjectileImpactCaller(var1)
                        || var0.root.thread != Thread.currentThread()
                        || !var0.root.server.isSameThread()) {
                        return;
                    }

                    if (var0.binding.root != var0.root
                        || var0.binding.source != var1.source
                        || var0.binding.target != var1.target
                        || var0.binding.rawAmountBits != var1.rawAmountBits) {
                        return;
                    }

                    var0.closed = true;
                    endInteraction(var0.root);
                    var3 = true;
                } catch (Throwable var8) {
                    var2.closingFailure = var8;
                    closeNativeProjectileObservation(var2);
                    var2.failure.fail(var1, 3, var8);
                    throw var8;
                } finally {
                    var2.finished = true;
                    if (!var3) {
                        closeNativeProjectileObservation(var2);
                    }
                }
            }
        }
    }

    private static FoodWeaponSourceLedger.OwnerWriterPublication acceptedOriginalPublication(FoodWeaponSourceLedger.OriginalInteractionBinding var0) {
        int var1 = 0;

        while (var0 != null && var1 < 16) {
            FoodWeaponSourceLedger.OwnerWriterPublication var2 = var0.acceptedPublication;
            if (var2 != null && var2.binding == var0 && originalPublicationRootLive(var2)) {
                return var2;
            }

            var1++;
            var0 = var0.parent;
        }

        return null;
    }

    private static boolean originalPublicationRootLive(FoodWeaponSourceLedger.OwnerWriterPublication var0) {
        if (var0 != null && !var0.closed && var0.thread == Thread.currentThread() && var0.fieldSequenceBinding != null) {
            FoodWeaponSourceLedger.OwnerFieldSequenceBinding var1 = var0.fieldSequenceBinding;
            FoodWeaponSourceLedger.InteractionRoot var2 = var0.binding.root;
            FoodWeaponOriginalSequence.Lineage var3 = var1.sequence.lineage;
            return !var1.terminal
                && var1.publication == var0
                && var1.thread == Thread.currentThread()
                && !var2.closed
                && !var2.serverState.closed
                && !var2.ownerState.closed
                && var2.thread == Thread.currentThread()
                && var2.ownerState.current == var2
                && var2.serverState.epoch == var2.serverEpoch
                && var2.ownerState.epoch == var2.ownerEpoch
                && var2.server.isSameThread()
                && SERVERS.get(var2.server) == var2.serverState
                && var3.ownerRoot == var2
                && var3.issuer == var0
                && var3.acceptedCallback == var0.receipt
                && var3.generation == var0.fieldGeneration
                && var1.sequence.original.continuing(var3.publication);
        } else {
            return false;
        }
    }

    private static boolean originalBindingAncestryLive(FoodWeaponSourceLedger.OriginalInteractionBinding var0) {
        if (var0 != null && !var0.closed && var0.depth > 0 && var0.depth <= 16) {
            FoodWeaponSourceLedger.OwnerWriterPublication var1 = acceptedOriginalPublication(var0);
            if (var1 != null && var1.binding.root == var0.root) {
                FoodWeaponSourceLedger.OriginalInteractionBinding var2 = var0.root.current;
                boolean var3 = false;

                for (int var4 = 0; var2 != null && var4 < 16; var2 = var2.parent) {
                    if (var2.closed || var2.root != var0.root) {
                        return false;
                    }

                    if (var2 == var0) {
                        var3 = true;
                        break;
                    }

                    var4++;
                }

                if (!var3) {
                    return false;
                } else if (var0.childCall == null) {
                    return var0.snapshot != null;
                } else {
                    FoodWeaponSourceLedger.FinalListedChildScope var5 = var0.acceptedChildScope;
                    return var5 != null
                        && var5.binding == var0
                        && var5.call == var0.childCall
                        && !var5.closed
                        && var5.hurtEntered
                        && var5.hurtActive
                        && var5.parent == var0.parent
                        && var5.thread == Thread.currentThread()
                        && var5.originalConsumerChild != null
                        && FoodWeaponFinalDamage.matchesAcceptedOriginalChild(var5.call);
                }
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    private static boolean acceptedOriginalEchoPublication(
        FoodWeaponFinalDamage.OffenseReceipt var0,
        FoodWeaponSourceLedger.AttemptBatchQuote var1,
        FoodWeaponSourceLedger.AttemptBatchAdmission var2,
        List<FoodWeaponSourceLedger.AttemptRequest> var3,
        List<FoodWeaponSourceLedger.TargetRequest> var4,
        ItemStack var5,
        CompoundTag var6
    ) {
        FoodWeaponSourceLedger.OriginalInteractionBinding var7 = originalInteractionBinding(var1);
        FoodWeaponSourceLedger.OwnerWriterPublication var8 = acceptedOriginalPublication(var7);
        return var8 != null
            && var8.binding == var7
            && var8.receipt == var0
            && var8.batch == var1
            && var8.admission == var2
            && var8.result.status() == FoodWeaponSourceLedger.AttemptBatchCommitStatus.COMMITTED
            && var8.expectation.actual == var5
            && var2.preparation.result == var8.result
            && var8.result.batch == var1
            && matchesAttemptRequests(var1, var3, var4)
            && var7.snapshot != null
            && var7.snapshot.equals(var6)
            && originalBindingAncestryLive(var7);
    }

    private static boolean pendingEchoOfferCaller() {
        return NATIVE_CALLER.walk(
            var0 -> var0.filter(var0x -> var0x.getDeclaringClass() != FoodWeaponSourceLedger.class)
                    .findFirst()
                    .map(
                        var0x -> var0x.getDeclaringClass() == FoodSocketAttributePowers.class
                                && var0x.getMethodName().equals("commitPreparedOffense")
                                && var0x.getDescriptor()
                                    .equals(
                                        "(Lnet/farmingtales/ftboss/item/FoodSocketAttributePowers$PreparedOffense;FLnet/farmingtales/ftboss/item/FoodWeaponFinalDamage$OffenseReceipt;)Lnet/farmingtales/ftboss/item/FoodSocketAttributePowers$OffenseCommit;"
                                    )
                    )
                    .orElse(false)
        );
    }

    static FoodWeaponSourceLedger.PendingEchoLineage issuePendingEchoLineage(
        FoodSocketAttributePowers.PreparedOffense var0,
        FoodSocketAttributePowers.OffensePlan var1,
        FoodWeaponFinalDamage.OffenseReceipt var2,
        float var3,
        FoodWeaponSourceLedger.AttemptBatchQuote var4,
        FoodWeaponSourceLedger.AttemptBatchAdmission var5,
        List<FoodWeaponSourceLedger.AttemptRequest> var6,
        List<FoodWeaponSourceLedger.TargetRequest> var7,
        Entry<?> var8,
        ServerPlayer var9,
        LivingEntity var10,
        DamageSource var11,
        long var12,
        double var14
    ) {
        boolean var16 = var0 != null
            && var1 != null
            && var2 != null
            && var8 != null
            && FoodSocketAttributePowers.matchesOriginalPendingEchoOffer(var0, var1, var2, var3, var8, var9, var10, var11, var12, var14)
            && acceptedOriginalEchoPublication(var2, var4, var5, var6, var7, var1.actualWeapon(), var1.snapshot());
        if (pendingEchoOfferCaller()
            && var0 != null
            && var1 != null
            && var2 != null
            && var8 != null
            && var8.key() != null
            && var8.value() != null
            && var9 != null
            && var10 != null
            && var11 != null
            && server(var9).isSameThread()
            && clock(var9) == var1.quotedAt()
            && Float.isFinite(var3)
            && !(var3 <= 0.0F)
            && Double.isFinite(var14)
            && !(var14 < 0.0)
            && FoodSocketAttributePowers.matchesPreparedOffense(var0, var1, var4, var5, var6, var7)
            && (var16 || var2.matchesBatch(var4, var5, var6, var7) && var2.matches(var1, var9, var11, var1.quotedAt(), var3))) {
            if (!var16) {
                FoodWeaponSourceLedger.AttemptResult var17 = var2.attempt(FoodWeaponSourceLedger.AttemptDomain.SERIES_ECHO).orElse(null);
                if (var17 == null
                    || var17.domain() != FoodWeaponSourceLedger.AttemptDomain.SERIES_ECHO
                    || var17.commit() != FoodWeaponSourceLedger.AttemptCommit.COMMITTED_SUCCESS) {
                    return null;
                }
            }

            CompoundTag var20 = var1.snapshot();
            Key var18 = var8.key();
            if (var9.getUUID().equals(var18.owner())
                && var1.weaponId().equals(var18.weapon())
                && var1.attackId().equals(var18.attack())
                && var1.originalHand().name().equals(var18.originalHand())
                && var9.getItemInHand(var1.originalHand()) == var1.actualWeapon()
                && var10.level() == var9.level()
                && !var10.isRemoved()
                && var9.serverLevel().getEntity(var10.getUUID()) == var10
                && var11.getEntity() == var9
                && var12 == FoodSocketAttributeRules.deadline(var1.quotedAt(), 12)
                && (
                    var16
                        || var20.equals(resolveReadOnly(var11, var9))
                            && matchesOriginalInteraction(originalInteractionBinding(var4), var20, var9)
                            && originalHandContinuousReadOnly(var20, var9)
                )) {
                FoodWeaponSourceLedger.OriginalInteractionBinding var19 = originalInteractionBinding(var4);
                return var19 != null && (var19.source == null || var19.source == var11) && (var19.target == null || var19.target == var10)
                    ? new FoodWeaponSourceLedger.PendingEchoLineage(
                        var8, server(var9), var20, var10.getUUID(), var1.quotedAt(), var12, var14, var1.actualWeapon()
                    )
                    : null;
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    private static boolean pendingEchoSignedImageValid(FoodWeaponSourceLedger.PendingEchoLineage var0, ServerPlayer var1) {
        if (var0 != null && var1 != null && var0.server == server(var1) && var0.purpose.equals(var0.snapshot.getString("DamagePurpose"))) {
            CompoundTag var2 = var0.snapshot;
            if (var1 != null
                && var2 != null
                && var2.getInt("SnapshotVersion") == 3
                && canonicalInputValid(var2)
                && var2.getString("ServerSignature").matches("[0-9a-f]{64}")
                && var2.hasUUID("Owner")
                && var2.getUUID("Owner").equals(var1.getUUID())
                && var2.hasUUID("AttackId")
                && var2.hasUUID("WeaponId")
                && var2.hasUUID("ServerEpoch")
                && var2.hasUUID("OwnerEpoch")
                && !var2.getString("MaterialFingerprint").isEmpty()
                && var2.contains("Weapon", 10)
                && var2.contains("CapturedAt", 4)
                && var2.contains("ExpiresAt", 4)
                && var2.getString("Dimension").equals(var1.level().dimension().location().toString())
                && (
                    var2.getString("OriginalHand").equals(InteractionHand.MAIN_HAND.name())
                        || var2.getString("OriginalHand").equals(InteractionHand.OFF_HAND.name())
                )
                && !var2.contains("Hand")
                && !var2.contains("NativeProfile")) {
                FoodWeaponSourceLedger.ServerState var3 = SERVERS.get(server(var1));
                if (var3 != null && OwnerRepairGenerationAuthority.snapshotAllowed(var2)) {
                    long var4 = clock(var1);
                    long var6 = var2.getLong("CapturedAt");
                    long var8 = var2.getLong("ExpiresAt");
                    if (var2.getUUID("ServerEpoch").equals(var3.epoch)
                        && var2.getUUID("OwnerEpoch").equals(ownerEpoch(var3, var1.getUUID()))
                        && var4 >= var3.latestTick
                        && var6 >= 0L
                        && var6 <= 9223372036854774607L
                        && var8 == var6 + 1200L
                        && var4 >= var6
                        && var4 < var8) {
                        String var10 = var2.getString("Family");
                        String var11 = var2.getString("SourceKind");
                        boolean var12 = var11.equals("TRIDENT") && var10.equals("MELEE") && var2.getBoolean("TridentLaunchAuthorized");
                        if (var12 || var11.equals(var10) && (var10.equals("BOW") || var10.equals("STAFF") || var10.equals("MELEE"))) {
                            String var13 = signature(var2, var3);
                            if (!var13.isEmpty()
                                && MessageDigest.isEqual(
                                    var2.getString("ServerSignature").getBytes(StandardCharsets.US_ASCII), var13.getBytes(StandardCharsets.US_ASCII)
                                )) {
                                ItemStack var14 = NativeFoundationItemData.decode(var2.getCompound("Weapon"));
                                return var14.isEmpty()
                                        || var14.getCount() != 1
                                        || !var2.getUUID("WeaponId").equals(FoodWeaponLifecycle.weaponId(var14))
                                        || !boundProfile(var14).equals(var2.getString("BoundProfile"))
                                        || !FoodWeaponLifecycle.repairProfile(var14).equals(var2.getString("RepairProfile"))
                                        || !materialFingerprint(var14).equals(var2.getString("MaterialFingerprint"))
                                        || !socketFingerprint(var14, var10).equals(var2.getString("SocketFingerprint"))
                                        || !foundationContinuous(var2, var1, var14)
                                    ? false
                                    : !var12 || isTrident(var14) && (!var2.getString("BoundProfile").isEmpty() || nativeFoundation(var14));
                            } else {
                                return false;
                            }
                        } else {
                            return false;
                        }
                    } else {
                        return false;
                    }
                } else {
                    return false;
                }
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    private static boolean pendingEchoHandContinuous(FoodWeaponSourceLedger.PendingEchoLineage var0, ServerPlayer var1) {
        if (!pendingEchoSignedImageValid(var0, var1)) {
            return false;
        } else {
            CompoundTag var2 = var0.snapshot;
            InteractionHand var3 = InteractionHand.valueOf(var2.getString("OriginalHand"));
            ItemStack var4 = var1.getItemInHand(var3);
            ItemStack var5 = NativeFoundationItemData.decode(var2.getCompound("Weapon"));
            ResourceLocation var6 = var4.isEmpty() ? null : BuiltInRegistries.ITEM.getKey(var4.getItem());
            ResourceLocation var7 = var5.isEmpty() ? null : BuiltInRegistries.ITEM.getKey(var5.getItem());
            return var4 == var0.originalActual
                && !var4.isEmpty()
                && var4.getCount() == 1
                && !var5.isEmpty()
                && var5.getCount() == 1
                && var4.getItem() == var5.getItem()
                && var6 != null
                && var6.equals(var7)
                && FoodWeaponFamilyBuild.kind(var4).name().equals(var2.getString("Family"))
                && !FoodWeaponLifecycle.isBroken(var4)
                && var2.getUUID("WeaponId").equals(FoodWeaponLifecycle.weaponId(var4))
                && boundProfile(var4).equals(var2.getString("BoundProfile"))
                && FoodWeaponLifecycle.repairProfile(var4).equals(var2.getString("RepairProfile"))
                && materialFingerprint(var4).equals(var2.getString("MaterialFingerprint"))
                && socketFingerprint(var4, var2.getString("Family")).equals(var2.getString("SocketFingerprint"))
                && foundationContinuous(var2, var1, var4);
        }
    }

    private static boolean pendingEchoLineageMatches(
        FoodWeaponSourceLedger.PendingEchoLineage var0, Entry<?> var1, CompoundTag var2, ServerPlayer var3, UUID var4, long var5, double var7
    ) {
        return var0 != null
            && var1 != null
            && var2 != null
            && var4 != null
            && var0.offeredEntry.key() == var1.key()
            && var0.offeredEntry.value() == var1.value()
            && var0.target.equals(var4)
            && var0.snapshot.equals(var2)
            && var0.dueAt == var5
            && var5 == FoodSocketAttributeRules.deadline(var0.offeredAt, 12)
            && Double.doubleToRawLongBits(var0.rangeSquared) == Double.doubleToRawLongBits(var7)
            && pendingEchoHandContinuous(var0, var3);
    }

    static boolean pendingEchoSourceContinuous(
        FoodWeaponSourceLedger.PendingEchoLineage var0, Entry<?> var1, CompoundTag var2, ServerPlayer var3, UUID var4, long var5, double var7
    ) {
        return var0 != null && !var0.dispatched && pendingEchoLineageMatches(var0, var1, var2, var3, var4, var5, var7);
    }

    static FoodWeaponSourceLedger.PendingEchoInteraction beginDurablePendingEchoInteraction(
        FoodWeaponSourceLedger.PendingEchoLineage var0,
        Entry<?> var1,
        FoodSocketAttributePowers.PendingEchoDispatch var2,
        ServerPlayer var3,
        LivingEntity var4,
        DamageSource var5,
        float var6
    ) {
        FoodWeaponSourceLedger.InteractionRoot var7 = projectedRootForPlayer(var3);
        Object var8 = var7 == null ? null : var7.ownerState.interactionChange;
        Object var9 = var7 == null ? null : var7.interactionChange;
        if (pendingEchoLifecycleCaller()
            && var0 != null
            && !var0.dispatched
            && var3 != null
            && var4 != null
            && var5 != null
            && Float.floatToRawIntBits(var6) == Float.floatToRawIntBits(2.0F)
            && var5.getEntity() == var3
            && var5.getDirectEntity() == null
            && var5.is(FoodWeaponDamageSources.SECONDARY)
            && var4.level() == var3.level()
            && var3.serverLevel().getEntity(var4.getUUID()) == var4
            && FoodSocketAttributePowers.matchesPendingEchoDispatch(var2, var3, var4, var5, var6)) {
            CompoundTag var10 = FoodSocketAttributePowers.pendingEchoDispatchSource(var2);
            long var11 = clock(var3);
            if (var11 >= var0.dueAt
                && var11 - var0.dueAt <= 20L
                && var4.distanceToSqr(var3) <= var0.rangeSquared
                && pendingEchoLineageMatches(var0, var1, var10, var3, var4.getUUID(), var0.dueAt, var0.rangeSquared)
                && !var3.isRemoved()
                && var3.isAlive()
                && !var3.isSpectator()
                && server(var3).isSameThread()) {
                FoodWeaponSourceLedger.ServerState var13 = SERVERS.get(server(var3));
                FoodWeaponSourceLedger.OwnerState var14 = var13 == null ? null : var13.ownerEpochs.get(var3.getUUID());
                if (var14 != null
                    && !var14.closed
                    && projectedRootForPlayer(var3) == var7
                    && (var7 == null || var14 == var7.ownerState && var14.interactionChange == var8 && var7.interactionChange == var9)) {
                    var0.dispatched = true;
                    FoodWeaponSourceLedger.InteractionRoot var15 = var14.current;
                    Object var16 = var14.interactionChange;
                    if (var15 != null) {
                        retireInteraction(var15);
                    }

                    FoodWeaponSourceLedger.InteractionRoot var17 = new FoodWeaponSourceLedger.InteractionRoot(
                        var14, var13, var3, FoodWeaponSourceLedger.RootSite.DELAYED_ECHO
                    );
                    boolean var18 = false;

                    FoodWeaponSourceLedger.PendingEchoInteraction var21;
                    try {
                        var17.current = new FoodWeaponSourceLedger.OriginalInteractionBinding(var17, null, var5, var4, var0.snapshot, var6, null);
                        CompoundTag var19 = NativeFoundationItemData.image(var0.originalActual);
                        if (!canonicalInputValid(var19)) {
                            revokeUnreturnedInteraction(var17);
                            return null;
                        }

                        var17.expectation = new FoodWeaponSourceLedger.WeaponExpectation(var0.originalActual, var19);
                        var17.ownsExpectation = true;
                        if (var14.current != var15 || var14.interactionChange != var16 || var14.closed || var13.closed || !installRootProjection(var17)) {
                            retireInteraction(var17);
                            return null;
                        }

                        setOwnerInteraction(var14, var17);
                        FoodWeaponSourceLedger.PendingEchoInteraction var20 = new FoodWeaponSourceLedger.PendingEchoInteraction(
                            var2, var17, var3, var4, var5, var6, var0
                        );
                        var18 = true;
                        var21 = var20;
                    } finally {
                        if (!var18) {
                            revokeUnreturnedInteraction(var17);
                        }
                    }

                    return var21;
                } else {
                    return null;
                }
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    private static FoodWeaponSourceLedger.InteractionRoot beginExternalInteraction(
        ServerPlayer var0, FoodWeaponSourceLedger.RootSite var1, DamageSource var2, Entity var3, CompoundTag var4, float var5
    ) {
        return beginExternalInteraction(var0, var1, var2, var3, var4, var5, null, null);
    }

    private static FoodWeaponSourceLedger.InteractionRoot beginExternalInteraction(
        ServerPlayer var0,
        FoodWeaponSourceLedger.RootSite var1,
        DamageSource var2,
        Entity var3,
        CompoundTag var4,
        float var5,
        ItemStack var6,
        FoodWeaponSourceLedger.WeaponExpectation var7
    ) {
        return beginExternalInteraction(var0, var1, var2, var3, var4, var5, var6, var7, null);
    }

    private static FoodWeaponSourceLedger.InteractionRoot beginExternalInteraction(
        ServerPlayer var0,
        FoodWeaponSourceLedger.RootSite var1,
        DamageSource var2,
        Entity var3,
        CompoundTag var4,
        float var5,
        ItemStack var6,
        FoodWeaponSourceLedger.WeaponExpectation var7,
        FoodWeaponSourceLedger.NativeProjectileObservation var8
    ) {
        if (var7 != null && var7.closed) {
            return null;
        } else {
            FoodWeaponSourceLedger.InteractionRoot var9 = projectedRootForPlayer(var0);
            Object var10 = var9 == null ? null : var9.ownerState.interactionChange;
            Object var11 = var9 == null ? null : var9.interactionChange;
            if (var0 != null
                && var4 != null
                && !var4.isEmpty()
                && !var0.level().isClientSide
                && server(var0).isSameThread()
                && !var0.isRemoved()
                && var0.isAlive()
                && !var0.isSpectator()
                && validReadOnly(var4, var0)) {
                FoodWeaponSourceLedger.ServerState var12 = SERVERS.get(server(var0));
                FoodWeaponSourceLedger.OwnerState var13 = var12 == null ? null : var12.ownerEpochs.get(var0.getUUID());
                if (var13 != null
                    && !var13.closed
                    && projectedRootForPlayer(var0) == var9
                    && (var9 == null || var13 == var9.ownerState && var13.interactionChange == var10 && var9.interactionChange == var11)) {
                    FoodWeaponSourceLedger.InteractionRoot var14 = var13.current;
                    Object var15 = var13.interactionChange;
                    if (var14 != null) {
                        retireInteraction(var14);
                    }

                    FoodWeaponSourceLedger.InteractionRoot var16 = new FoodWeaponSourceLedger.InteractionRoot(var13, var12, var0, var1);
                    if (var8 != null) {
                        var8.root = var16;
                        var8.expectation = var7;
                        var16.nativeProjectileObservation = var8;
                        var16.expectation = var7;
                    }

                    boolean var17 = false;

                    FoodWeaponSourceLedger.InteractionRoot var23;
                    try {
                        var16.current = new FoodWeaponSourceLedger.OriginalInteractionBinding(var16, null, var2, var3, var4, var5, null);
                        if (var8 != null) {
                            var8.binding = var16.current;
                        }

                        if (var7 != null) {
                            var16.expectation = var7;
                        } else if (var6 != null) {
                            CompoundTag var18 = NativeFoundationItemData.image(var6);
                            if (!canonicalInputValid(var18)) {
                                revokeUnreturnedInteraction(var16);
                                return null;
                            }

                            var16.expectation = new FoodWeaponSourceLedger.WeaponExpectation(var6, var18);
                            var16.ownsExpectation = true;
                        }

                        if (var16.closed
                            || var16.retired
                            || var16.expectation != null && var16.expectation.closed
                            || var8 != null && var8.failure.failed
                            || var13.current != var14
                            || var13.interactionChange != var15
                            || var13.closed
                            || var12.closed
                            || !installRootProjection(var16)) {
                            retireInteraction(var16);
                            return null;
                        }

                        setOwnerInteraction(var13, var16);
                        var17 = true;
                        var23 = var16;
                    } finally {
                        if (!var17) {
                            if (var8 != null) {
                                closeNativeProjectileObservation(var8);
                            }

                            revokeUnreturnedInteraction(var16);
                        }
                    }

                    return var23;
                } else {
                    return null;
                }
            } else {
                return null;
            }
        }
    }

    private static synchronized void retireInteraction(FoodWeaponSourceLedger.InteractionRoot var0) {
        ArrayList<FoodWeaponSourceLedger.OriginalInteractionBinding> var1 = new ArrayList<>();
        removeRootProjection(var0);
        closeOwnedExpectation(var0);
        var0.interactionChange = new Object();
        if (var0.writerPublication != null) {
            closeOwnerWriterPublication(var0.writerPublication);
        }

        var0.retired = true;
        var0.closed = true;
        FoodWeaponSourceLedger.OriginalInteractionBinding var2 = var0.current;

        for (int var3 = 16; var2 != null && var3 > 0; var3--) {
            if (!var2.closed) {
                var1.add(var2);
            }

            var2.closed = true;
            var2 = var2.parent;
        }

        var0.current = null;

        for (FoodWeaponSourceLedger.OriginalInteractionBinding var4 : var1) {
            FoodWeaponFinalDamage.retireOriginalCapture(var4);
        }
    }

    private static synchronized void retireOwnerInteraction(ServerPlayer var0) {
        if (var0 != null && server(var0).isSameThread()) {
            FoodWeaponSourceLedger.ServerState var1 = SERVERS.get(server(var0));
            FoodWeaponSourceLedger.OwnerState var2 = var1 == null ? null : var1.ownerEpochs.get(var0.getUUID());
            if (var2 != null && var2.current != null) {
                FoodWeaponSourceLedger.InteractionRoot var3 = var2.current;
                retireInteraction(var3);
                if (var2.current == var3) {
                    setOwnerInteraction(var2, null);
                }
            }
        }
    }

    private static synchronized void endInteraction(FoodWeaponSourceLedger.InteractionRoot var0) {
        if (var0 != null && var0.thread == Thread.currentThread() && var0.server.isSameThread()) {
            retireInteraction(var0);
            if (var0.ownerState.current == var0) {
                setOwnerInteraction(var0.ownerState, null);
            }
        }
    }

    private static void endOriginalInteractionBinding(FoodWeaponSourceLedger.OriginalInteractionBinding var0) {
        if (var0 != null && var0.root.thread == Thread.currentThread() && var0.root.server.isSameThread()) {
            boolean var1 = !var0.closed;
            var0.closed = true;
            if (var0.root.current == var0) {
                var0.root.readCut.change();
                var0.root.interactionChange = new Object();
                FoodWeaponSourceLedger.OriginalInteractionBinding var2 = var0.parent;
                var0.root.current = var2 == null
                        || var2.closed
                        || !interactionRootLive(var0.root, var0.root.player) && acceptedOriginalPublication(var2) == null
                    ? null
                    : var2;
            }

            if (var1) {
                FoodWeaponFinalDamage.retireOriginalCapture(var0);
            }
        }
    }

    private static FoodWeaponSourceLedger.InteractionRoot beginNativeImpactInteraction(FoodWeaponSourceLedger.NativeHurtScope var0, float var1) {
        if (var0 != null
            && var0.interaction == null
            && nativeHurtLive(var0)
            && Float.isFinite(var1)
            && !(var1 <= 0.0F)
            && Float.floatToRawIntBits(var0.snapshot.getFloat("NativeAmount")) == Float.floatToRawIntBits(var1)) {
            FoodWeaponSourceLedger.RootSite var2 = var0.ironCallback != null
                ? FoodWeaponSourceLedger.RootSite.NATIVE_IRON_IMPACT
                : (var0.earlyIngress != null ? FoodWeaponSourceLedger.RootSite.NATIVE_MEAT_IMPACT : FoodWeaponSourceLedger.RootSite.NATIVE_CARRIER_IMPACT);
            return beginExternalInteraction(var0.player, var2, var0.source, var0.target, var0.snapshot, var1, null, var0.expectation);
        } else {
            return null;
        }
    }

    private static synchronized void closeNativeHurtScope(FoodWeaponSourceLedger.NativeHurtScope var0) {
        if (var0 != null && var0.thread == Thread.currentThread() && server(var0.player).isSameThread()) {
            var0.closed = true;
            endInteraction(var0.interaction);
            if (PRIVATE_NATIVE_HURT.get() == var0) {
                PRIVATE_NATIVE_HURT.remove();
            }
        }
    }

    private static boolean genericCastCaller() {
        return NATIVE_CALLER.walk(
            var0 -> var0.filter(var0x -> var0x.getDeclaringClass() != FoodWeaponSourceLedger.class)
                    .findFirst()
                    .map(
                        var0x -> var0x.getDeclaringClass() == WeaponEvolutionSkills.class
                                && var0x.getMethodName().equals("cast")
                                && var0x.getDescriptor().equals("(Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/world/item/ItemStack;)V")
                    )
                    .orElse(false)
        );
    }

    public static FoodWeaponSourceLedger.InteractionRoot beginGenericInteraction(ServerPlayer var0, ItemStack var1, CompoundTag var2) {
        return genericCastCaller()
                && var0 != null
                && var1 == var0.getMainHandItem()
                && var2 != null
                && "GENERIC_J".equals(var2.getString("DamagePurpose"))
                && attemptSourceValid(var2, var0)
            ? beginExternalInteraction(var0, FoodWeaponSourceLedger.RootSite.GENERIC_CAST, null, null, var2, Float.NaN, var1, null)
            : null;
    }

    public static synchronized void endGenericInteraction(FoodWeaponSourceLedger.InteractionRoot var0) {
        if (genericCastCaller() && var0 != null && var0.site == FoodWeaponSourceLedger.RootSite.GENERIC_CAST) {
            endInteraction(var0);
        }
    }

    private static boolean finalChildCaller() {
        return NATIVE_CALLER.walk(
            var0 -> var0.filter(var0x -> var0x.getDeclaringClass() != FoodWeaponSourceLedger.class)
                    .findFirst()
                    .map(
                        var0x -> var0x.getDeclaringClass() == FoodWeaponFinalDamage.class
                                && var0x.getMethodName().equals("hurtListedChild")
                                && var0x.getDescriptor()
                                    .equals(
                                        "(Lnet/farmingtales/ftboss/item/FoodWeaponFinalDamage$ChildHurtBridge;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/damagesource/DamageSource;F)Z"
                                    )
                    )
                    .orElse(false)
        );
    }

    static FoodWeaponSourceLedger.FinalListedChildScope enterFinalListedChild(FoodWeaponFinalDamage.ChildHurtCall var0) {
        if (finalChildCaller() && var0 != null && FoodWeaponFinalDamage.matchesOwnerChildCall(var0)) {
            FoodWeaponSourceLedger.OriginalInteractionBinding var1 = FoodWeaponFinalDamage.ownerChildOriginalBinding(var0);
            DamageSource var2 = FoodWeaponFinalDamage.ownerChildSource(var0);
            LivingEntity var3 = FoodWeaponFinalDamage.ownerChildTarget(var0);
            float var4 = FoodWeaponFinalDamage.ownerChildAmount(var0);
            FoodWeaponConsumerSequence.OriginalChild var5 = FoodWeaponFinalDamage.originalConsumerChild(var0);
            FoodWeaponSourceLedger.OwnerWriterPublication var6 = var5 == null ? null : acceptedOriginalPublication(var1);
            boolean var7 = var5 != null && var6 != null && FoodWeaponFinalDamage.matchesAcceptedOriginalChild(var0) && originalBindingAncestryLive(var1);
            Object var8 = var1 == null ? null : var1.root.interactionChange;
            if (var1 != null
                && var2 != null
                && var3 != null
                && Float.isFinite(var4)
                && !(var4 <= 0.0F)
                && var1.depth < 16
                && (var7 || interactionMatches(var1, var1.snapshot, var1.root.player) && attemptSourceValid(var1.snapshot, var1.root.player))
                && !var3.isRemoved()
                && var3.level() == var1.root.player.level()
                && var1.root.player.serverLevel().getEntity(var3.getUUID()) == var3) {
                FoodWeaponSourceLedger.FinalListedChildScope var9 = new FoodWeaponSourceLedger.FinalListedChildScope(var0, var1, var2, var3, var4);
                var9.originalConsumerChild = var5;
                var9.originalPublisher = var6;
                if ((var7 || projectedCurrentBindingLive(var1)) && (!var7 || originalBindingAncestryLive(var1)) && var1.root.interactionChange == var8) {
                    var9.root.readCut.change();
                    var9.root.interactionChange = new Object();
                    var9.root.current = var9.binding;
                    return var9;
                } else {
                    return null;
                }
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    private static boolean matchesActiveFinalListedChild(FoodWeaponSourceLedger.FinalListedChildScope var0, DamageSource var1, LivingEntity var2, float var3) {
        return var0 != null
                && !var0.closed
                && var0.hurtEntered
                && var0.hurtActive
                && var0.thread == Thread.currentThread()
                && (var0.originalConsumerChild != null ? originalPublicationRootLive(var0.originalPublisher) : interactionRootLive(var0.root, var0.root.player))
                && var0.root.current == var0.binding
                && !var0.binding.closed
                && !var0.parent.closed
                && var0.parent.root == var0.root
                && var0.binding.parent == var0.parent
                && var0.binding.childCall == var0.call
                && var0.binding.snapshot != null
                && var0.parent.snapshot != null
                && var0.binding.snapshot.equals(var0.parent.snapshot)
                && var0.binding.depth == var0.parent.depth + 1
                && var0.binding.depth <= 16
                && var1 == var0.source
                && var2 == var0.target
                && var1 == var0.binding.source
                && var2 == var0.binding.target
                && Float.isFinite(var3)
                && !(var3 <= 0.0F)
                && !var2.isRemoved()
                && var2.level() == var0.root.player.level()
                && var0.root.player.serverLevel().getEntity(var2.getUUID()) == var2
                && Float.floatToRawIntBits(var3) == var0.binding.rawAmountBits
                && Float.floatToRawIntBits(var3) == Float.floatToRawIntBits(var0.rawAmount)
                && FoodWeaponFinalDamage.matchesOwnerChildCall(var0.call)
            ? FoodWeaponFinalDamage.ownerChildOriginalBinding(var0.call) == var0.parent
                && FoodWeaponFinalDamage.ownerChildSource(var0.call) == var1
                && FoodWeaponFinalDamage.ownerChildTarget(var0.call) == var2
                && Float.floatToRawIntBits(FoodWeaponFinalDamage.ownerChildAmount(var0.call)) == Float.floatToRawIntBits(var3)
            : false;
    }

    static boolean hurtFinalListedChild(FoodWeaponSourceLedger.FinalListedChildScope var0) {
        if (finalChildCaller() && var0 != null && !var0.closed && !var0.hurtEntered && var0.thread == Thread.currentThread() && var0.root.server.isSameThread()
            )
         {
            var0.hurtEntered = true;
            var0.hurtActive = true;

            boolean var1;
            try {
                if (matchesActiveFinalListedChild(var0, var0.source, var0.target, var0.rawAmount)) {
                    return var0.target.hurt(var0.source, var0.rawAmount);
                }

                var1 = false;
            } finally {
                var0.hurtActive = false;
            }

            return var1;
        } else {
            return false;
        }
    }

    static void leaveFinalListedChild(FoodWeaponSourceLedger.FinalListedChildScope var0) {
        if (finalChildCaller() && var0 != null && !var0.closed && !var0.hurtActive && var0.thread == Thread.currentThread() && var0.root.server.isSameThread()) {
            var0.closed = true;
            endOriginalInteractionBinding(var0.binding);
        }
    }

    private FoodWeaponSourceLedger() {
    }

    private static FoodWeaponSourceLedger.ServerState state(MinecraftServer var0) {
        FoodWeaponSourceLedger.ServerState var1;
        synchronized (FoodWeaponSourceLedger.class) {
            var1 = SERVERS.computeIfAbsent(var0, FoodWeaponSourceLedger.ServerState::new);
        }

        var1.initializeStore(var0);
        return var1;
    }

    private static MinecraftServer server(ServerPlayer var0) {
        return var0.serverLevel().getServer();
    }

    public static long clock(ServerPlayer var0) {
        return server(var0).overworld().getGameTime();
    }

    private static FoodWeaponSourceLedger.NativeKind nativeKind(Item var0, ItemStack var1) {
        String var2 = FourWeaponFoundationAdapter.registryId(var1);
        if (var0 != var1.getItem()) {
            return null;
        } else if (var0.getClass() == BowItem.class && var2.equals("minecraft:bow")) {
            return FoodWeaponSourceLedger.NativeKind.BOW;
        } else if (var0.getClass() == CrossbowItem.class && var2.equals("minecraft:crossbow")) {
            return FoodWeaponSourceLedger.NativeKind.CROSSBOW;
        } else {
            return var0.getClass() == TridentItem.class && var2.equals("minecraft:trident") ? FoodWeaponSourceLedger.NativeKind.TRIDENT : null;
        }
    }

    private static FoodWeaponSourceLedger.NativeOperation beginNative(Item var0, ItemStack var1, Level var2, LivingEntity var3, InteractionHand var4) {
        FoodWeaponSourceLedger.NativeKind var5 = nativeKind(var0, var1);
        if (var5 != null
            && !var2.isClientSide
            && var3 instanceof ServerPlayer var6
            && var6.level() == var2
            && var4 != null
            && var6.getItemInHand(var4) == var1
            && var1.getCount() == 1
            && server(var6).isSameThread()
            && var1.getDamageValue() < var1.getMaxDamage() - 1
            && (NativeSourceLedgerItemData.rawCustomDataForObservation(var1) == null || canonicalInputValid(NativeSourceLedgerItemData.rawCustomDataForObservation(var1)))
            && !FoodWeaponMaterialFingerprint.foundationSnapshot(var1).isEmpty()
            && !foundationRules(var6, var1).isEmpty()) {
            OwnerNativeSemanticAuthority.NativeWear initialization = OwnerNativeSemanticAuthority.nativeLifecycleBefore(var1, var6);
            boolean initialized = false;
            try {
                FoodWeaponLifecycle.ensureTracked(var1);
                initialized = true;
            } finally {
                OwnerNativeSemanticAuthority.nativeLifecycleReturned(initialization, var1, var6, initialized);
            }
            if (!canonicalInputValid(NativeFoundationItemData.image(var1))) {
                return null;
            }

            ensureOwnerState(state(server(var6)), var6.getUUID());
            retireOwnerInteraction(var6);
            return new FoodWeaponSourceLedger.NativeOperation(var5, var6, var1, var4);
        }

        return null;
    }

    private static boolean liveNative(FoodWeaponSourceLedger.NativeOperation var0) {
        return var0 != null
            && OwnerRepairGenerationAuthority.matches(var0.weapon, var0.repairGeneration)
            && !var0.invalid
            && OwnerFullRepairGuard.permits(var0.weapon)
            && var0.thread == Thread.currentThread()
            && var0.player.isAlive()
            && var0.dimension.equals(var0.player.level().dimension().location().toString())
            && var0.server == server(var0.player)
            && var0.tick == clock(var0.player)
            && var0.epoch.equals(state(var0.server).epoch)
            && Objects.equals(var0.ownerEpoch, ownerEpoch(state(var0.server), var0.player.getUUID()));
    }

    private static boolean nativeCaller(FoodWeaponSourceLedger.NativeKind var0, String var1) {
        return NATIVE_CALLER.walk(frames -> frames
            .filter(frame -> frame.getDeclaringClass() != FoodWeaponSourceLedger.class)
            .findFirst()
            .map(frame -> NativeProjectileBoundaryRules.originalSite(frame.getDeclaringClass(), frame.getMethodName(), frame.getDescriptor(), var0.name(), var1))
            .orElse(false));
    }

    private static boolean nativeMaterialCaller(String var0, String var1) {
        return NATIVE_CALLER.walk(
            var2 -> var2.filter(var0xx -> var0xx.getDeclaringClass() != FoodWeaponSourceLedger.class)
                    .findFirst()
                    .map(
                        var2x -> var2x.getDeclaringClass() == FoodWeaponRanged.class
                                && var2x.getMethodName().equals(var0)
                                && var2x.getDescriptor().equals(var1)
                    )
                    .orElse(false)
        );
    }

    private static boolean nativeMaterialCaptureCaller() {
        return nativeMaterialCaller(
            "captureBound",
            "(Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/world/item/ItemStack;Lnet/farmingtales/ftboss/util/FoodWeaponFamilyRules$Kind;Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/nbt/CompoundTag;"
        );
    }

    private static boolean nativeMaterialLaunchCaller() {
        return nativeMaterialCaller("launch", "(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/nbt/CompoundTag;)V");
    }

    private static boolean nativeMaterialOperationCurrent(FoodWeaponSourceLedger.NativeOperation var0) {
        return var0 != null
            && !var0.invalid
            && var0.thread == Thread.currentThread()
            && NATIVE_OPERATION.get() == var0
            && NATIVE_DEPTH.get() == 1
            && (var0.kind == FoodWeaponSourceLedger.NativeKind.BOW || var0.kind == FoodWeaponSourceLedger.NativeKind.CROSSBOW);
    }

    public static synchronized FoodWeaponSourceLedger.NativeMaterialCapture nativeMaterialCaptureEntered(
        ServerPlayer var0, ItemStack var1, InteractionHand var2
    ) {
        FoodWeaponSourceLedger.NativeOperation var3 = NATIVE_OPERATION.get();
        if (var3 != null
            && var3.thread == Thread.currentThread()
            && var3.player == var0
            && var3.weapon == var1
            && var3.hand == var2
            && var3.kind != FoodWeaponSourceLedger.NativeKind.TRIDENT) {
            var3.materialAttempted = true;
            if (!var3.materialRejected && var3.materialCapture == null) {
                try {
                    if (nativeMaterialCaptureCaller() && nativeMaterialOperationCurrent(var3)) {
                        FoodWeaponSourceLedger.NativeMaterialCapture var4 = new FoodWeaponSourceLedger.NativeMaterialCapture(var3);
                        var3.materialCapture = var4;
                        return var4;
                    } else {
                        var3.materialRejected = true;
                        return null;
                    }
                } catch (Throwable var5) {
                    var3.materialRejected = true;
                    throw var5;
                }
            } else {
                var3.materialRejected = true;
                return null;
            }
        } else {
            return null;
        }
    }

    public static synchronized void nativeMaterialCaptureFailed(FoodWeaponSourceLedger.NativeMaterialCapture var0, Throwable var1) {
        if (var0 != null && var0.operation.thread == Thread.currentThread()) {
            var0.failed = true;
            var0.operation.materialRejected = true;
            if (var0.originalFailure == null) {
                var0.originalFailure = var1;
            }
        }
    }

    private static boolean nativeMaterialPendingCaller() {
        return nativeMaterialCaller("prepareBowShot", "(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/nbt/CompoundTag;)V");
    }

    private static boolean nativeMaterialPendingReadCaller() {
        return nativeMaterialCaller("launchPreparedBow", "(Lnet/minecraft/world/entity/Entity;)V");
    }

    private static boolean nativeMaterialTransferCurrent(FoodWeaponSourceLedger.NativeMaterialTransfer var0) {
        if (var0 == null) {
            return false;
        } else {
            FoodWeaponSourceLedger.NativeReceipt var1 = var0.receipt;
            FoodWeaponSourceLedger.NativeOperation var2 = var1.operation;
            return !var0.rejected
                && !var1.materialRejected
                && !var2.materialRejected
                && nativeMaterialOperationCurrent(var2)
                && var2.current == var1
                && var1.materialTransfer == var0
                && var1.entity == var0.entity
                && var0.capture == var2.materialCapture
                && !var0.capture.failed
                && var0.capture.image != null
                && var0.capturedReturn == var0.capture.originalReturnedSnapshot;
        }
    }

    private static void rejectNativeMaterialTransfer(FoodWeaponSourceLedger.NativeMaterialTransfer var0) {
        if (var0 != null && var0.receipt.operation.thread == Thread.currentThread()) {
            var0.rejected = true;
            var0.receipt.materialRejected = true;
            FoodWeaponSourceLedger.NativeMaterialPublication var1 = var0.receipt.material;
            if (var1 != null && var1.transfer == var0) {
                var1.phase = FoodWeaponSourceLedger.NativeMaterialPhase.REJECTED;
                if (var1.paid != null) {
                    var1.paid.expectation.closed = true;
                }
            }
        }
    }

    public static synchronized void nativeMaterialTransferFailed(FoodWeaponSourceLedger.NativeMaterialTransfer var0, Throwable var1) {
        rejectNativeMaterialTransfer(var0);
        if (var0 != null && var0.receipt.operation.thread == Thread.currentThread() && var0.originalFailure == null) {
            var0.originalFailure = var1;
        }
    }

    public static synchronized void nativeMaterialTransferObservationFailed(FoodWeaponSourceLedger.NativeMaterialTransfer var0, Throwable var1) {
        rejectNativeMaterialTransfer(var0);
        if (var0 != null && var0.receipt.operation.thread == Thread.currentThread() && var0.observationFailure == null) {
            var0.observationFailure = var1;
        }
    }

    public static synchronized FoodWeaponSourceLedger.NativeMaterialTransfer nativeMaterialPendingEntered(Entity var0, CompoundTag var1) {
        FoodWeaponSourceLedger.NativeOperation var2 = NATIVE_OPERATION.get();
        FoodWeaponSourceLedger.NativeReceipt var3 = var2 == null ? null : var2.current;
        if (var2 != null && var2.thread == Thread.currentThread() && var2.kind == FoodWeaponSourceLedger.NativeKind.BOW && var3 != null && var3.entity == var0) {
            var3.materialAttempted = true;

            try {
                FoodWeaponSourceLedger.NativeMaterialCapture var4 = var2.materialCapture;
                if (nativeMaterialPendingCaller()
                    && nativeMaterialOperationCurrent(var2)
                    && !var2.materialRejected
                    && !var3.materialRejected
                    && var3.materialTransfer == null
                    && var4 != null
                    && !var4.failed
                    && var4.image != null
                    && var4.originalReturnedSnapshot == var1
                    && var1 != null
                    && !var3.added
                    && !var3.consumed) {
                    FoodWeaponSourceLedger.NativeMaterialTransfer var5 = new FoodWeaponSourceLedger.NativeMaterialTransfer(var3, var4, var0);
                    var3.materialTransfer = var5;
                    return var5;
                } else {
                    var3.materialRejected = true;
                    return null;
                }
            } catch (Throwable var6) {
                var3.materialRejected = true;
                throw var6;
            }
        } else {
            return null;
        }
    }

    public static synchronized void nativeMaterialPendingCopied(
        FoodWeaponSourceLedger.NativeMaterialTransfer var0, CompoundTag var1, String var2, CompoundTag var3, CompoundTag var4
    ) {
        if (var0 != null && var0.receipt.operation.thread == Thread.currentThread()) {
            try {
                if (nativeMaterialPendingCaller()
                    && nativeMaterialTransferCurrent(var0)
                    && var0.pendingCopy == null
                    && var0.receipt.operation.kind == FoodWeaponSourceLedger.NativeKind.BOW
                    && var0.capturedReturn == var3
                    && var4 != null
                    && var4 != var3
                    && var1 != null
                    && var1.getClass() == CompoundTag.class
                    && "FTBossPendingBowShot".equals(var2)) {
                    var0.pendingReceiver = var1;
                    var0.pendingKey = var2;
                    var0.pendingCopy = var4;
                    if (!OwnerNativeMaterialImage.freeze(var4).same(var0.capture.image) || !nativeMaterialTransferCurrent(var0)) {
                        rejectNativeMaterialTransfer(var0);
                    }
                } else {
                    rejectNativeMaterialTransfer(var0);
                }
            } catch (Throwable var6) {
                var0.observationFailure = var6;
                rejectNativeMaterialTransfer(var0);
                throw var6;
            }
        }
    }

    public static synchronized void nativeMaterialPendingReturned(FoodWeaponSourceLedger.NativeMaterialTransfer var0, Tag var1) {
        if (var0 != null && var0.receipt.operation.thread == Thread.currentThread()) {
            if (nativeMaterialPendingCaller() && nativeMaterialTransferCurrent(var0) && !var0.pendingReturned && var0.pendingCopy != null && var1 == null) {
                var0.pendingReturned = true;
            } else {
                rejectNativeMaterialTransfer(var0);
            }
        }
    }

    public static synchronized FoodWeaponSourceLedger.NativeMaterialTransfer nativeMaterialPendingReadEntered(Entity var0) {
        FoodWeaponSourceLedger.NativeOperation var1 = NATIVE_OPERATION.get();
        FoodWeaponSourceLedger.NativeReceipt var2 = var1 == null ? null : var1.current;
        if (var1 != null && var1.thread == Thread.currentThread() && var1.kind == FoodWeaponSourceLedger.NativeKind.BOW && var2 != null && var2.entity == var0) {
            var2.materialAttempted = true;
            FoodWeaponSourceLedger.NativeMaterialTransfer var3 = var2.materialTransfer;

            try {
                if (nativeMaterialPendingReadCaller()
                    && nativeMaterialTransferCurrent(var3)
                    && var3.pendingReturned
                    && !var3.pendingReadEntered
                    && !var3.pendingTaken) {
                    var3.pendingReadEntered = true;
                    return var3;
                } else {
                    var2.materialRejected = true;
                    rejectNativeMaterialTransfer(var3);
                    return null;
                }
            } catch (Throwable var5) {
                var2.materialRejected = true;
                rejectNativeMaterialTransfer(var3);
                throw var5;
            }
        } else {
            return null;
        }
    }

    public static synchronized void nativeMaterialPendingTaken(
        FoodWeaponSourceLedger.NativeMaterialTransfer var0, CompoundTag var1, String var2, CompoundTag var3, CompoundTag var4
    ) {
        if (var0 != null && var0.receipt.operation.thread == Thread.currentThread()) {
            if (nativeMaterialPendingReadCaller()
                && nativeMaterialTransferCurrent(var0)
                && var0.pendingReadEntered
                && var0.pendingReturned
                && !var0.pendingTaken
                && var0.pendingReceiver == var1
                && var0.pendingReceiver == var4
                && var0.pendingKey.equals(var2)
                && var0.pendingCopy == var3) {
                var0.pendingTaken = true;
            } else {
                rejectNativeMaterialTransfer(var0);
            }
        }
    }

    public static synchronized FoodWeaponSourceLedger.NativeMaterialTransfer nativeMaterialLaunchEntered(Entity var0, CompoundTag var1) {
        FoodWeaponSourceLedger.NativeOperation var2 = NATIVE_OPERATION.get();
        FoodWeaponSourceLedger.NativeReceipt var3 = var2 == null ? null : var2.current;
        if (var2 != null
            && var2.thread == Thread.currentThread()
            && var2.kind != FoodWeaponSourceLedger.NativeKind.TRIDENT
            && var3 != null
            && var3.entity == var0) {
            var3.materialAttempted = true;
            FoodWeaponSourceLedger.NativeMaterialTransfer var4 = var3.materialTransfer;

            try {
                FoodWeaponSourceLedger.NativeMaterialCapture var5 = var2.materialCapture;
                if (nativeMaterialLaunchCaller()
                    && nativeMaterialOperationCurrent(var2)
                    && !var2.materialRejected
                    && !var3.materialRejected
                    && var5 != null
                    && !var5.failed
                    && var5.image != null
                    && var3.worn
                    && !var3.added
                    && !var3.consumed) {
                    if (var2.kind == FoodWeaponSourceLedger.NativeKind.CROSSBOW) {
                        if (var4 != null || var5.originalReturnedSnapshot != var1 || var1 == null) {
                            var3.materialRejected = true;
                            return null;
                        }

                        var4 = new FoodWeaponSourceLedger.NativeMaterialTransfer(var3, var5, var0);
                        var3.materialTransfer = var4;
                    } else if (!nativeMaterialTransferCurrent(var4) || !var4.pendingTaken || var4.pendingCopy != var1) {
                        var3.materialRejected = true;
                        return null;
                    }

                    if (var4.launchEntered) {
                        rejectNativeMaterialTransfer(var4);
                        return null;
                    } else {
                        var4.launchEntered = true;
                        var4.launchSource = var1;
                        return var4;
                    }
                } else {
                    var3.materialRejected = true;
                    return null;
                }
            } catch (Throwable var6) {
                var3.materialRejected = true;
                rejectNativeMaterialTransfer(var4);
                throw var6;
            }
        } else {
            return null;
        }
    }

    private static boolean nativeMaterialProvenanceMatches(FoodWeaponSourceLedger.NativeMaterialPublication var0) {
        if (var0 != null && var0.transfer != null) {
            FoodWeaponSourceLedger.NativeMaterialTransfer var1 = var0.transfer;
            FoodWeaponSourceLedger.NativeReceipt var2 = var0.receipt;
            FoodWeaponSourceLedger.NativeMaterialCapture var3 = var0.capture;
            if (!var1.rejected
                && !var2.materialRejected
                && !var2.operation.materialRejected
                && !var3.failed
                && var1.receipt == var2
                && var2.materialTransfer == var1
                && var1.capture == var3
                && var3 == var2.operation.materialCapture
                && var1.entity == var0.entity
                && var1.capturedReturn == var3.originalReturnedSnapshot
                && var1.launchEntered
                && var0.originalSnapshot == var1.launchSource
                && var0.originalPutValue == var1.launchCopy
                && var1.launchCopy != null
                && var1.launchCopy != var1.launchSource) {
                return var2.operation.kind == FoodWeaponSourceLedger.NativeKind.BOW
                    ? var1.pendingReturned
                        && var1.pendingReadEntered
                        && var1.pendingTaken
                        && var1.launchSource == var1.pendingCopy
                        && var1.pendingCopy != var1.capturedReturn
                    : var2.operation.kind == FoodWeaponSourceLedger.NativeKind.CROSSBOW && var1.launchSource == var1.capturedReturn;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    public static synchronized void nativeMaterialCaptureReturned(ServerPlayer var0, ItemStack var1, InteractionHand var2, CompoundTag var3) {
        FoodWeaponSourceLedger.NativeOperation var4 = NATIVE_OPERATION.get();
        if (var4 != null
            && var4.thread == Thread.currentThread()
            && var4.player == var0
            && var4.weapon == var1
            && var4.hand == var2
            && var4.kind != FoodWeaponSourceLedger.NativeKind.TRIDENT) {
            var4.materialAttempted = true;
            FoodWeaponSourceLedger.NativeMaterialCapture var5 = var4.materialCapture;

            try {
                if (nativeMaterialCaptureCaller()
                    && nativeMaterialOperationCurrent(var4)
                    && !var4.materialRejected
                    && var5 != null
                    && !var5.failed
                    && var5.originalReturnedSnapshot == null
                    && var3 != null) {
                    var5.originalReturnedSnapshot = var3;
                    OwnerNativeMaterialImage var6 = OwnerNativeMaterialImage.freeze(var3);
                    CompoundTag var7 = var6.image();
                    if (nativeMaterialOperationCurrent(var4)
                        && !var4.materialRejected
                        && var7.hasUUID("Owner")
                        && var7.hasUUID("WeaponId")
                        && var7.hasUUID("ServerEpoch")
                        && var7.hasUUID("OwnerEpoch")
                        && var4.epoch.equals(var7.getUUID("ServerEpoch"))
                        && var4.ownerEpoch.equals(var7.getUUID("OwnerEpoch"))
                        && var4.hand.name().equals(var7.getString("OriginalHand"))
                        && var4.dimension.equals(var7.getString("Dimension"))
                        && var7.getLong("CapturedAt") == var4.tick
                        && "BOW".equals(var7.getString("Family"))
                        && "BOW".equals(var7.getString("SourceKind"))
                        && !var7.contains("DamagePurpose")
                        && var7.getString("ServerSignature").matches("[0-9a-f]{64}")) {
                        var5.image = var6;
                    } else {
                        var5.failed = true;
                        var4.materialRejected = true;
                    }
                } else {
                    var4.materialRejected = true;
                    if (var5 != null) {
                        var5.failed = true;
                    }
                }
            } catch (Throwable var8) {
                var4.materialRejected = true;
                if (var5 != null) {
                    var5.failed = true;
                }

                throw var8;
            }
        }
    }

    public static synchronized FoodWeaponSourceLedger.NativeMaterialPublication nativeMaterialPutEntered(
        Entity var0, CompoundTag var1, String var2, CompoundTag var3, CompoundTag var4
    ) {
        FoodWeaponSourceLedger.NativeOperation var5 = NATIVE_OPERATION.get();
        FoodWeaponSourceLedger.NativeReceipt var6 = var5 == null ? null : var5.current;
        if (var5 != null
            && var5.thread == Thread.currentThread()
            && var5.kind != FoodWeaponSourceLedger.NativeKind.TRIDENT
            && var6 != null
            && var6.entity == var0) {
            var6.materialAttempted = true;
            var6.materialRejected = true;
        }

        return null;
    }

    public static synchronized FoodWeaponSourceLedger.NativeMaterialPublication nativeMaterialPutEntered(
        FoodWeaponSourceLedger.NativeMaterialTransfer var0, Entity var1, CompoundTag var2, String var3, CompoundTag var4, CompoundTag var5
    ) {
        if (var0 != null && var0.receipt.operation.thread == Thread.currentThread()) {
            FoodWeaponSourceLedger.NativeReceipt var6 = var0.receipt;
            var6.materialAttempted = true;
            FoodWeaponSourceLedger.NativeMaterialPublication var7 = null;

            try {
                if (nativeMaterialLaunchCaller()
                    && nativeMaterialTransferCurrent(var0)
                    && var0.launchEntered
                    && var0.entity == var1
                    && var0.launchSource == var4
                    && var0.launchCopy == null
                    && var6.worn
                    && !var6.added
                    && !var6.consumed
                    && var6.material == null
                    && var2 != null
                    && var2.getClass() == CompoundTag.class
                    && "FTBossWeaponMaterials".equals(var3)
                    && var5 != null
                    && var5 != var4
                    && var5 != var0.capturedReturn
                    && var5 != var0.pendingCopy
                    && var1 != null
                    && (var1.getClass() == Arrow.class || var1.getClass() == SpectralArrow.class)
                    && var1 instanceof OwnerNativeMaterialEntityIdentity var8) {
                    var0.launchCopy = var5;
                    var7 = new FoodWeaponSourceLedger.NativeMaterialPublication(
                        var6, var0.capture, var1, var2, var3, var4, var5, var8.ftboss$ownerMaterialEntityIdentity()
                    );
                    var7.transfer = var0;
                    var6.material = var7;
                    var7.image = OwnerNativeMaterialImage.freeze(var5);
                    if (var7.entityId != null
                        && var7.image.same(var0.capture.image)
                        && nativeMaterialTransferCurrent(var0)
                        && nativeMaterialProvenanceMatches(var7)) {
                        return var7;
                    } else {
                        rejectNativeMaterialTransfer(var0);
                        return null;
                    }
                } else {
                    rejectNativeMaterialTransfer(var0);
                    return null;
                }
            } catch (Throwable var9) {
                var0.observationFailure = var9;
                if (var7 != null) {
                    var7.observationFailure = var9;
                }

                rejectNativeMaterialTransfer(var0);
                throw var9;
            }
        } else {
            return null;
        }
    }

    public static synchronized void nativeMaterialPutReturned(FoodWeaponSourceLedger.NativeMaterialPublication var0, Tag var1) {
        if (var0 != null && var0.receipt.operation.thread == Thread.currentThread()) {
            try {
                FoodWeaponSourceLedger.NativeReceipt var2 = var0.receipt;
                if (nativeMaterialLaunchCaller()
                    && var0.phase == FoodWeaponSourceLedger.NativeMaterialPhase.OPEN
                    && nativeMaterialProvenanceMatches(var0)
                    && nativeMaterialTransferCurrent(var0.transfer)
                    && var2.material == var0
                    && var2.entity == var0.entity
                    && var2.worn
                    && !var2.added
                    && !var2.consumed
                    && var0.image != null
                    && var1 == null) {
                    var0.phase = FoodWeaponSourceLedger.NativeMaterialPhase.RETURNED;
                } else {
                    var0.phase = FoodWeaponSourceLedger.NativeMaterialPhase.REJECTED;
                    rejectNativeMaterialTransfer(var0.transfer);
                }
            } catch (Throwable var3) {
                var0.observationFailure = var3;
                var0.phase = FoodWeaponSourceLedger.NativeMaterialPhase.REJECTED;
                rejectNativeMaterialTransfer(var0.transfer);
                throw var3;
            }
        }
    }

    public static synchronized void nativeMaterialPutFailed(FoodWeaponSourceLedger.NativeMaterialPublication var0, Throwable var1) {
        if (var0 != null && var1 != null && var0.receipt.operation.thread == Thread.currentThread()) {
            var0.originalFailure = var1;
            var0.phase = FoodWeaponSourceLedger.NativeMaterialPhase.REJECTED;
            rejectNativeMaterialTransfer(var0.transfer);
            if (var0.paid != null) {
                var0.paid.expectation.closed = true;
            }
        }
    }

    public static synchronized void nativeMaterialObservationFailed(FoodWeaponSourceLedger.NativeMaterialPublication var0, Throwable var1) {
        if (var0 != null && var1 != null && var0.receipt.operation.thread == Thread.currentThread()) {
            var0.observationFailure = var1;
            var0.phase = FoodWeaponSourceLedger.NativeMaterialPhase.REJECTED;
            rejectNativeMaterialTransfer(var0.transfer);
            if (var0.paid != null) {
                var0.paid.expectation.closed = true;
            }
        }
    }

    private static void finishNativeMaterialPaid(FoodWeaponSourceLedger.NativeReceipt var0, CompoundTag var1, CompoundTag var2) {
        FoodWeaponSourceLedger.NativeMaterialPublication var3 = var0.material;
        FoodWeaponSourceLedger.NativeOperation var4 = var0.operation;
        if (!var4.materialRejected
            && !var0.materialRejected
            && nativeMaterialProvenanceMatches(var3)
            && var3.phase == FoodWeaponSourceLedger.NativeMaterialPhase.RETURNED
            && var3.receipt == var0
            && var3.entity == var0.entity
            && var3.receiver == var2
            && "FTBossWeaponMaterials".equals(var3.key)
            && var3.capture == var4.materialCapture
            && !var3.capture.failed
            && var3.image != null
            && nativeMaterialOperationCurrent(var4)
            && var4.current != null
            && var0.added
            && var0.worn
            && !var0.consumed) {
            var3.phase = FoodWeaponSourceLedger.NativeMaterialPhase.CLAIMED;
            var0.consumed = true;
            FoodWeaponSourceLedger.NativePaidWeapon var5 = null;

            try {
                CompoundTag var6 = var3.image.image();
                if (!var0.rules.equals(var6.getString("FoundationApprovedRules"))) {
                    var3.phase = FoodWeaponSourceLedger.NativeMaterialPhase.REJECTED;
                    return;
                }

                CompoundTag var7 = OwnerNativeMaterialImage.freeze(var0.unsigned).image();

                for (String var9 : var7.getAllKeys()) {
                    var6.put(var9, var7.get(var9));
                }

                var6.putString("NativeEntry", var4.kind.name());
                var6.putUUID("NativeReceiptId", var0.token);
                var6.putUUID("NativeEntityId", var3.entityId);
                var6.putUUID("AttackId", var0.token);
                var6.putString("OwnerMaterialProduction", var3.image.digest());
                var6.remove("ServerSignature");
                FoodWeaponSourceLedger.ServerState var12 = SERVERS.get(var4.server);
                if (var12 == null || !var4.epoch.equals(var12.epoch) || var4.tick < var12.latestTick || !damageAuthorized(var6)) {
                    var3.phase = FoodWeaponSourceLedger.NativeMaterialPhase.REJECTED;
                    return;
                }

                if (!OwnerRepairGenerationAuthority.stamp(var4.weapon, var6, var12.revocations)) {
                    var3.phase = FoodWeaponSourceLedger.NativeMaterialPhase.REJECTED;
                    return;
                }

                String var13 = signature(var6, var12);
                if (var13.isEmpty()) {
                    var3.phase = FoodWeaponSourceLedger.NativeMaterialPhase.REJECTED;
                    return;
                }

                var6.putString("ServerSignature", var13);
                if (!OwnerFullRepairGuard.bind(var4.weapon, var6.getUUID("WeaponId"))) {
                    var3.phase = FoodWeaponSourceLedger.NativeMaterialPhase.REJECTED;
                    return;
                }

                OwnerNativeMaterialImage var10 = OwnerNativeMaterialImage.freeze(var6);
                var5 = new FoodWeaponSourceLedger.NativePaidWeapon(var0, var1, var3, var10);
                var3.paid = var5;
                NativeVanillaImpact.bindOwnerPaidWeapon(var0.entity, var5);
                var3.phase = nativeMaterialOperationCurrent(var4)
                        && !var3.capture.failed
                        && var3.phase == FoodWeaponSourceLedger.NativeMaterialPhase.CLAIMED
                        && SERVERS.get(var4.server) == var12
                        && var4.epoch.equals(var12.epoch)
                        && var12.latestTick <= var4.tick
                    ? FoodWeaponSourceLedger.NativeMaterialPhase.PUBLISHED
                    : FoodWeaponSourceLedger.NativeMaterialPhase.REJECTED;
                if (var3.phase == FoodWeaponSourceLedger.NativeMaterialPhase.PUBLISHED) {
                    var12.latestTick = var4.tick;
                } else {
                    var5.expectation.closed = true;
                }
            } catch (Throwable var11) {
                var3.phase = FoodWeaponSourceLedger.NativeMaterialPhase.REJECTED;
                var3.observationFailure = var11;
                if (var5 != null) {
                    var5.expectation.closed = true;
                }
            }
        }
    }

    private static CompoundTag nativeMaterialPaidSnapshot(FoodWeaponSourceLedger.NativePaidWeapon var0, Projectile var1, ServerPlayer var2) {
        FoodWeaponSourceLedger.NativeMaterialPublication var3 = var0.material;
        FoodWeaponSourceLedger.NativeReceipt var4 = var0.receipt;
        return OwnerRepairGenerationAuthority.matches(var4.operation.weapon, var0.expectation.repairGeneration)
                && OwnerFullRepairGuard.permits(var4.operation.weapon)
                && !var4.operation.materialRejected
                && !var4.materialRejected
                && nativeMaterialProvenanceMatches(var3)
                && var3.phase == FoodWeaponSourceLedger.NativeMaterialPhase.PUBLISHED
                && var3.paid == var0
                && var4.material == var3
                && var3.receipt == var4
                && !var3.capture.failed
                && var3.entity == var1
                && var4.entity == var1
                && var0.carrier == var1
                && var4.operation.player == var2
                && var4.operation.thread == Thread.currentThread()
                && !var4.operation.invalid
                && var4.consumed
                && var4.worn
                && var4.added
                && var0.combined != null
                && !var0.expectation.closed
            ? var0.combined.image()
            : new CompoundTag();
    }

    private static void finishNative(FoodWeaponSourceLedger.NativeOperation var0, boolean var1) {
        if (var1 && liveNative(var0) && var0.current != null && var0.current.after != null) {
            CompoundTag var2 = NativeFoundationItemData.image(var0.weapon);
            if (canonicalInputValid(var2)) {
                CompoundTag var3 = var2.copy();
                CompoundTag var4 = var0.current.after.copy();
                if (var0.kind == FoodWeaponSourceLedger.NativeKind.CROSSBOW) {
                    if (!NativeSourceLedgerItemData.uncharged(var0.player.registryAccess(), var2)) {
                        return;
                    }

                    for (CompoundTag var7 : List.of(var2, var4)) {
                        NativeSourceLedgerItemData.removeChargedFromComparison(var7);
                    }
                }

                if (var2.equals(var4)) {
                    for (FoodWeaponSourceLedger.NativeReceipt var14 : var0.receipts) {
                        if (var14.added
                            && var14.worn
                            && !var14.consumed
                            && var14.entity != null
                            && var14.after != null
                            && var14.unsigned != null
                            && var14.entity.level() == var0.player.level()) {
                            Entity var8 = var14.entity;
                            if (var8 instanceof Projectile) {
                                Projectile var15 = (Projectile)var8;
                                if (var15.getOwner() == var0.player) {
                                    CompoundTag var16 = var14.entity.getPersistentData();
                                    boolean var9 = var16.contains("FTBossWeaponMaterials");
                                    if (!var0.materialAttempted && !var14.materialAttempted && var14.material == null) {
                                        if (!var9) {
                                            var14.consumed = true;
                                            ItemStack var10 = NativeFoundationItemData.decode(var14.after.copy());
                                            if (var14.after.equals(NativeFoundationItemData.image(var10)) && var14.rules.equals(foundationRules(var0.player, var10))) {
                                                CompoundTag var11 = decorateNative(var0.player, var10, var14.unsigned, var0.hand, var14);
                                                if (!var11.isEmpty() && valid(var11, var0.player)) {
                                                    FoodWeaponSourceLedger.NativePaidWeapon var12 = new FoodWeaponSourceLedger.NativePaidWeapon(var14, var3);
                                                    var14.imageFieldInitialized = OwnerFieldExpectedAuthority.nativeProjectileImage(var14.entity, var14.entity.getPersistentData(), var11);
                                                    NativeVanillaImpact.bindOwnerPaidWeapon(var14.entity, var12);
                                                }
                                            }
                                        }
                                    } else if (var9 && !var0.materialRejected && !var14.materialRejected) {
                                        finishNativeMaterialPaid(var14, var3, var16);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public static void nativeRelease(Item var0, ItemStack var1, Level var2, LivingEntity var3, int var4) {
        int var5 = NATIVE_DEPTH.get();
        NATIVE_DEPTH.set(var5 + 1);
        FoodWeaponSourceLedger.NativeOperation var6 = NATIVE_OPERATION.get();
        if (var6 != null) {
            var6.invalid = true;
        }

        FoodWeaponSourceLedger.NativeOperation var7 = null;
        NATIVE_OPERATION.remove();
        boolean var8 = false;

        try {
            if (var5 == 0) {
                var7 = beginNative(var0, var1, var2, var3, var3.getUsedItemHand());
            }

            if (var7 != null) {
                NATIVE_OPERATION.set(var7);
            }

            var0.releaseUsing(var1, var2, var3, var4);
            var8 = true;
        } finally {
            try {
                finishNative(var7, var8);
            } finally {
                NATIVE_OPERATION.remove();
                if (var5 == 0) {
                    NATIVE_DEPTH.remove();
                } else {
                    NATIVE_DEPTH.set(var5);
                }
            }
        }
    }

    public static InteractionResultHolder<ItemStack> nativeUse(Item var0, ItemStack var1, Level var2, Player var3, InteractionHand var4) {
        int var5 = NATIVE_DEPTH.get();
        NATIVE_DEPTH.set(var5 + 1);
        FoodWeaponSourceLedger.NativeOperation var6 = NATIVE_OPERATION.get();
        if (var6 != null) {
            var6.invalid = true;
        }

        FoodWeaponSourceLedger.NativeOperation var7 = null;
        NATIVE_OPERATION.remove();
        boolean var8 = false;

        InteractionResultHolder var10;
        try {
            if (var5 == 0) {
                var7 = beginNative(var0, var1, var2, var3, var4);
            }

            if (var7 != null) {
                NATIVE_OPERATION.set(var7);
            }

            InteractionResultHolder var9 = var0.use(var2, var3, var4);
            var8 = true;
            var10 = var9;
        } finally {
            try {
                finishNative(var7, var8);
            } finally {
                NATIVE_OPERATION.remove();
                if (var5 == 0) {
                    NATIVE_DEPTH.remove();
                } else {
                    NATIVE_DEPTH.set(var5);
                }
            }
        }

        return var10;
    }

    private static void bindNativeArrow(AbstractArrow var0, FoodWeaponSourceLedger.NativeKind var1, ItemStack var2) {
        FoodWeaponSourceLedger.NativeOperation var3 = NATIVE_OPERATION.get();
        if (liveNative(var3)
            && var3.kind == var1
            && var3.weapon == var2
            && nativeCaller(var1, "factory")
            && var0.getOwner() == var3.player
            && var0.level() == var3.player.level()
            && !var0.getPersistentData().contains("FTBossWeaponMaterials")
            && (var0.getClass() == Arrow.class || var0.getClass() == SpectralArrow.class)) {
            if (var3.factoryEntity != var0) {
                var3.invalid = true;
            } else if (var3.current != null && !var3.current.added) {
                var3.invalid = true;
            } else {
                FoodWeaponSourceLedger.NativeReceipt var4 = new FoodWeaponSourceLedger.NativeReceipt(var3, var0);
                var3.current = var4;
                var3.receipts.add(var4);
            }
        }
    }

    public static AbstractArrow nativeCustomArrow(ProjectileWeaponItem item, AbstractArrow arrow, ItemStack ammunition, ItemStack weapon, Operation<AbstractArrow> original) {
        AbstractArrow result = original.call(item, arrow, ammunition, weapon);
        FoodWeaponSourceLedger.NativeKind kind = nativeKind(item, weapon);
        if (kind != null && kind != FoodWeaponSourceLedger.NativeKind.TRIDENT && approvedAmmunition(ammunition)) {
            bindNativeArrow(result, kind, weapon);
        }
        return result;
    }

    private static boolean approvedAmmunition(ItemStack var0) {
        Item var1 = var0.getItem();
        return (var1.getClass() == ArrowItem.class || var1.getClass() == SpectralArrowItem.class)
            && List.of("minecraft:arrow", "minecraft:tipped_arrow", "minecraft:spectral_arrow").contains(FourWeaponFoundationAdapter.registryId(var0));
    }

    public static AbstractArrow nativeArrowFactory(ArrowItem var0, Level var1, ItemStack var2, LivingEntity var3, ItemStack weapon, Operation<AbstractArrow> original) {
        AbstractArrow var4 = original.call(var0, var1, var2, var3, weapon);
        FoodWeaponSourceLedger.NativeOperation var5 = NATIVE_OPERATION.get();
        if (liveNative(var5)
            && var5.kind != FoodWeaponSourceLedger.NativeKind.TRIDENT
            && var5.weapon == weapon
            && nativeCaller(var5.kind, "ammoFactory")
            && var0 == var2.getItem()
            && approvedAmmunition(var2)
            && var3 == var5.player
            && var1 == var5.player.level()) {
            var5.factoryEntity = var4;
        }

        return var4;
    }

    public static AbstractArrow nativeCrossbowArrow(AbstractArrow var0, ItemStack var1, ItemStack var2) {
        if (approvedAmmunition(var2)) {
            bindNativeArrow(var0, FoodWeaponSourceLedger.NativeKind.CROSSBOW, var1);
        }

        return var0;
    }

    public static ThrownTrident nativeTridentFactory(Level var0, LivingEntity var1, ItemStack var2, Operation<ThrownTrident> original) {
        FoodWeaponSourceLedger.NativeOperation var3 = NATIVE_OPERATION.get();
        FoodWeaponSourceLedger.NativeReceipt var4 = var3 == null ? null : var3.current;
        ThrownTrident var5 = original.call(var0, var1, var2);
        FoodWeaponSourceLedger.TridentCarrierLineage var6 = null;

        try {
            if (var3 == null
                || var4 == null
                || var3.kind != FoodWeaponSourceLedger.NativeKind.TRIDENT
                || var3.weapon != var2
                || var3.player != var1
                || var3.thread != Thread.currentThread()
                || NATIVE_OPERATION.get() != var3
                || var3.current != var4
                || !liveNative(var3)
                || !nativeCaller(FoodWeaponSourceLedger.NativeKind.TRIDENT, "factory")
                || !var4.worn
                || var4.entity != null) {
                return var5;
            }

            var6 = var5 instanceof OwnerTridentCarrierAccess var7 ? var7.ftboss$ownerTridentLineage() : null;
            ItemStack var9 = FoodWeaponTridentCarrierReader.readOriginalItem(var5);
            if (var5.getOwner() == var1 && tridentCarrierLineage(var5, var9, var6) != null && var6.input == var2 && var6.origin == var4 && isTrident(var9)) {
                var4.entity = var5;
            }
        } catch (Throwable var8) {
            rejectTridentExtra(var5, var6, var4, null, null, null, var8);
        }

        return var5;
    }

    public static void nativeWear(ItemStack var0, int var1, LivingEntity var2, EquipmentSlot var3, Operation<Void> original) {
        FoodWeaponSourceLedger.NativeOperation var4 = NATIVE_OPERATION.get();
        FoodWeaponSourceLedger.NativeReceipt var5 = null;
        if (liveNative(var4)
            && var0 == var4.weapon
            && var2 == var4.player
            && var1 == 1
            && nativeCaller(var4.kind, "wear")
            && var3 == LivingEntity.getSlotForHand(var4.hand)
            && var4.player.getItemInHand(var4.hand) == var0) {
            if (var4.kind == FoodWeaponSourceLedger.NativeKind.TRIDENT && var4.current == null) {
                var4.current = new FoodWeaponSourceLedger.NativeReceipt(var4, null);
                var4.receipts.add(var4.current);
            }

            if (var4.current != null && !var4.current.worn && !var4.current.wearing) {
                var5 = var4.current;
                var5.wearing = true;
            } else {
                var4.invalid = true;
            }
        }

        OwnerNativeSemanticAuthority.NativeWear nativeInput = var5 == null ? null
            : OwnerNativeSemanticAuthority.nativeWearBefore(var5, var0, var5.before, var1);
        boolean var6 = false;
        Throwable var7 = null;

        try {
            original.call(var0, var1, var2, var3);
            var6 = true;
        } catch (Throwable var22) {
            var7 = var22;
            throw var22;
        } finally {
            try {
                if (var5 != null) {
                    var5.wearing = false;

                    try {
                        if (var6 && liveNative(var4) && authorizedWear(var5, var0, var1)) {
                            var5.after = NativeFoundationItemData.image(var0).copy();
                            var5.unsigned = new CompoundTag();
                            var5.unsigned.putString("Family", FoodWeaponFamilyBuild.kind(var0).name());
                            var5.unsigned.putString("DamagePurpose", "NATIVE_PROJECTILE");
                            FoodWeaponDamageScaling.freezeFactors(var0, var5.unsigned, 1.0, 1.0, 1.0);
                            var5.worn = true;
                        } else {
                            var4.invalid = true;
                        }
                    } catch (LinkageError | IllegalArgumentException var23) {
                        var4.invalid = true;
                    } finally {
                        OwnerNativeSemanticAuthority.nativeWearReturned(nativeInput, var0, var6 && var5.worn, var5.writes, var5.requestedDamage);
                    }
                }
            } catch (Throwable var24) {
                Throwable var11 = var24;
                if (var4 != null) {
                    var4.invalid = true;
                }

                if (var7 == null) {
                    throw var24;
                }

                if (var24 != var7) {
                    try {
                        var7.addSuppressed(var11);
                    } catch (Throwable var21) {
                    }
                }
            }
        }
    }

    public static void nativeAppliedDamage(ItemStack var0, int var1, int var2, LivingEntity var4, Operation<Void> original) {
        FoodWeaponSourceLedger.NativeOperation var5 = NATIVE_OPERATION.get();
        FoodWeaponSourceLedger.NativeReceipt var6 = var5 == null ? null : var5.current;
        boolean var7 = NATIVE_CALLER.walk(
            var0x -> var0x.filter(var0xx -> var0xx.getDeclaringClass() != FoodWeaponSourceLedger.class)
                    .findFirst()
                    .map(var0xx -> NativeProjectileBoundaryRules.appliedSite(var0xx.getDeclaringClass(), var0xx.getMethodName(), var0xx.getDescriptor()))
                    .orElse(false)
        );
        if (liveNative(var5) && var7 && var6 != null && var6.wearing && var0 == var5.weapon && var4 == var5.player) {
            var6.writes++;
            var6.requestedDamage = var1;
            if (var6.writes > 1) {
                var5.invalid = true;
            }
        }

        FoodWeaponSourceLedger.NativeWearToken var8 = PRIVATE_INGRESS_WEAR.get();
        if (var7 && var8 != null && !var8.returned && ingressLive(var8.ingress) && var0 == var8.ingress.actual && var4 == var8.ingress.player) {
            var8.writes++;
            var8.requested = var1;
            if (var8.writes > 1) {
                var8.ingress.invalid = true;
            }
        }

        try {
            if (var7) {
                OwnerStatueWearAdmission.damage(var0, var1, var2);
            } else {
                OwnerStatueWearAdmission.mutationNoticed(var0, null, 44);
            }
        } catch (Throwable var10) {
        }

        original.call(var0, var1);
    }

    public static boolean nativeAdd(Level var0, Entity var1, Operation<Boolean> original) {
        FoodWeaponSourceLedger.NativeOperation var2 = NATIVE_OPERATION.get();
        FoodWeaponSourceLedger.NativeReceipt var3 = var2 == null ? null : var2.current;
        boolean var4 = liveNative(var2)
            && nativeCaller(var2.kind, "add")
            && var3 != null
            && (NativeProjectileBoundaryRules.addBeforeWear(var2.kind.name()) ? !var3.worn && !var3.wearing : var3.worn)
            && var3.entity == var1
            && !var3.added
            && !var3.consumed
            && var0 == var2.player.level();
        boolean var5;
        if (var4) var2.adding = true;
        try {
            var5 = original.call(var0, var1);
        } finally {
            if (var4) var2.adding = false;
        }
        if (var4 && liveNative(var2)) {
            var3.added = var5;
            if (!var5) {
                var2.invalid = true;
            }
        } else if (var2 != null) {
            var2.invalid = true;
        }

        return var5;
    }

    private static boolean ironPhysical(ItemStack var0) {
        if (var0 != null && !var0.isEmpty() && var0.getCount() == 1 && FoodWeaponUseBridge.kind(var0) == Kind.STAFF) {
            for (Class var1 = var0.getItem().getClass(); var1 != null; var1 = var1.getSuperclass()) {
                if (var1.getName().equals("io.redspace.ironsspellbooks.item.CastingItem")) {
                    return true;
                }
            }

            return false;
        } else {
            return false;
        }
    }

    private static boolean ironBoundary(String var0, String var1, String var2, String var3, String var4) {
        return NATIVE_CALLER.walk(
            var5 -> {
                List var6 = var5.filter(var0xx -> var0xx.getDeclaringClass() != FoodWeaponSourceLedger.class).limit(2L).toList();
                return var6.size() == 2
                    && ((StackFrame)var6.get(0)).getClassName().equals(var0)
                    && (List.of("io.redspace.ironsspellbooks.player.ServerPlayerEvents", "io.redspace.ironsspellbooks.api.spells.AbstractSpell").contains(var0)
                        ? NativeWeaponIngressSites.matches((StackFrame)var6.get(0), var0, var1, var2)
                        : ((StackFrame)var6.get(0)).getMethodName().equals(var1))
                    && ((StackFrame)var6.get(0)).getDescriptor().equals(var2)
                    && ((StackFrame)var6.get(1)).getDeclaringClass() == ((StackFrame)var6.get(0)).getDeclaringClass()
                    && ((StackFrame)var6.get(1)).getMethodName().equals(var3)
                    && ((StackFrame)var6.get(1)).getDescriptor().equals(var4);
            }
        );
    }

    private static boolean ironLive(FoodWeaponSourceLedger.NativeIronUseToken var0) {
        if (var0 != null
            && OwnerRepairGenerationAuthority.matches(var0.actual, var0.repairGeneration)
            && !var0.invalid
            && OwnerFullRepairGuard.permits(var0.actual)
            && var0.thread == Thread.currentThread()
            && !var0.player.isRemoved()
            && var0.player.isAlive()
            && !var0.player.isSpectator()
            && !var0.player.level().isClientSide
            && server(var0.player).isSameThread()
            && var0.player.getItemInHand(var0.hand) == var0.actual
            && ironPhysical(var0.actual)
            && !FoodWeaponLifecycle.isBroken(var0.actual)) {
            FoodWeaponSourceLedger.ServerState var1 = SERVERS.get(server(var0.player));
            long var2 = clock(var0.player);
            return var1 == var0.state
                && Objects.equals(ownerEpoch(var1, var0.player.getUUID()), var0.ownerEpoch)
                && var0.dimension.equals(var0.player.level().dimension().location().toString())
                && var0.tick >= 0L
                && var0.tick <= 9223372036854774607L
                && var2 >= var0.tick
                && var2 < var0.tick + 1200L
                && var2 >= var1.latestTick
                && var0.rules.equals(foundationRules(var0.player, var0.actual));
        } else {
            return false;
        }
    }

    public static FoodWeaponSourceLedger.NativeIronUseToken nativeIronUseBegin(ItemStack var0, Player var1, InteractionHand var2) {
        return nativeIronUseBegin(var0, var1, var2, null);
    }

    public static FoodWeaponSourceLedger.NativeIronUseToken nativeIronUseBegin(
        ItemStack var0, Player var1, InteractionHand var2, FoodWeaponSourceLedger.NativeIssuerCustody var3
    ) {
        FoodWeaponSourceLedger.NativeIssueWindow var4 = new FoodWeaponSourceLedger.NativeIssueWindow(var1 instanceof ServerPlayer var5 ? var5 : null);
        String var15 = "(Lnet/neoforged/neoforge/event/entity/player/PlayerInteractEvent$RightClickItem;)V";
        boolean var6 = ironBoundary(
            "io.redspace.ironsspellbooks.player.ServerPlayerEvents",
            "ftboss$v9$ironUseBegin",
            "(Lnet/neoforged/neoforge/event/entity/player/PlayerInteractEvent$RightClickItem;Lnet/farmingtales/ftboss/integ/NativeIronExecution$UseFrame;)V",
            "onUseItem",
            var15
        );
        if (!var6
            || !(var1 instanceof ServerPlayer var7)
            || var2 == null
            || var7.getItemInHand(var2) != var0
            || !ironPhysical(var0)
            || FoodWeaponLifecycle.weaponId(var0) == null
            || FoodWeaponLifecycle.isBroken(var0)
            || !canonicalInputValid(NativeFoundationItemData.image(var0))
            || FoodWeaponMaterialFingerprint.foundationSnapshot(var0).isEmpty()
            || foundationRules(var7, var0).isEmpty()
            || var7.level().isClientSide
            || !var7.isAlive()
            || var7.isRemoved()
            || var7.isSpectator()
            || !server(var7).isSameThread()) {
            return null;
        }

        if (!var4.unchanged()) {
            return null;
        } else {
            FoodWeaponSourceLedger.NativeIronUseToken var8 = PRIVATE_IRON_USE.get();
            if (var8 != null) {
                var8.invalid = true;
                PRIVATE_IRON_USE.remove();
                return null;
            } else if (PRIVATE_INGRESS.get() == null && NATIVE_OPERATION.get() == null && NATIVE_DEPTH.get() == 0) {
                FoodWeaponSourceLedger.NativeIronUseToken var9 = new FoodWeaponSourceLedger.NativeIronUseToken(var7, var0, var2);
                if (!retireOriginalIssuerRoot(var4)) {
                    return null;
                } else if (!var4.unchanged()) {
                    return null;
                } else if (var3 != null && !var3.cut.reserve(var9)) {
                    return null;
                } else {
                    if (var3 != null) {
                        var3.use = var9;
                    }

                    boolean var10 = false;

                    FoodWeaponSourceLedger.NativeIronUseToken var11;
                    try {
                        PRIVATE_IRON_USE.set(var9);
                        var10 = true;
                        var11 = var9;
                    } finally {
                        if (!var10) {
                            if (var3 != null) {
                                closeNativeIssuerCustody(var3);
                            } else {
                                var9.invalid = true;
                                if (PRIVATE_IRON_USE.get() == var9) {
                                    PRIVATE_IRON_USE.remove();
                                }
                            }
                        }
                    }

                    return var11;
                }
            } else {
                return null;
            }
        }
    }

    public static synchronized void nativeIronUseEnd(FoodWeaponSourceLedger.NativeIronUseToken var0) {
        if (var0 != null && var0.thread == Thread.currentThread() && PRIVATE_IRON_USE.get() == var0) {
            var0.invalid = true;
            PRIVATE_IRON_USE.remove();
        }
    }

    public static FoodWeaponSourceLedger.NativeIronCastToken nativeIronAttemptBegin(
        Object var0, ItemStack var1, int var2, Player var3, Object var4, String var5
    ) {
        String var6 = "(Lnet/minecraft/world/item/ItemStack;ILnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/player/Player;Lio/redspace/ironsspellbooks/api/spells/CastSource;ZLjava/lang/String;)Z";
        boolean var7 = ironBoundary(
            "io.redspace.ironsspellbooks.api.spells.AbstractSpell",
            "ftboss$v9$ironAttemptBegin",
            var6.substring(0, var6.indexOf(41)) + "Lorg/spongepowered/asm/mixin/injection/callback/CallbackInfoReturnable;)V",
            "attemptInitiateCast",
            var6
        );
        FoodWeaponSourceLedger.NativeIronUseToken var8 = PRIVATE_IRON_USE.get();
        if (var7
            && ironLive(var8)
            && var3 == var8.player
            && var1 == var8.actual
            && var8.tick == clock(var8.player)
            && var0 != null
            && var4 != null
            && var4.getClass().getName().equals("io.redspace.ironsspellbooks.api.spells.CastSource")
            && var4 instanceof Enum var9
            && List.of("SPELLBOOK", "SCROLL", "SWORD", "MOB", "COMMAND", "NONE").contains(var9.name())
            && var2 > 0
            && var5 != null
            && !var5.isBlank()
            && var5.length() <= 256) {
            return new FoodWeaponSourceLedger.NativeIronCastToken(var8, var0, var4, var5, var2);
        }

        return null;
    }

    public static synchronized void nativeIronAttemptReturned(FoodWeaponSourceLedger.NativeIronCastToken var0) {
        cancelNativeIron(var0);
    }

    public static void nativeIronAttemptReturned(FoodWeaponSourceLedger.NativeIronCastToken var0, Object var1) {
        String var2 = "(Lnet/minecraft/world/item/ItemStack;ILnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/player/Player;Lio/redspace/ironsspellbooks/api/spells/CastSource;ZLjava/lang/String;)Z";
        if (var0 != null
            && ironBoundary(
                "io.redspace.ironsspellbooks.api.spells.AbstractSpell",
                "ftboss$v9$ironAttemptReturn",
                var2.substring(0, var2.indexOf(41)) + "Lorg/spongepowered/asm/mixin/injection/callback/CallbackInfoReturnable;)V",
                "attemptInitiateCast",
                var2
            )) {
            boolean var3;
            try {
                var3 = var1 != null
                    && var1.getClass().getName().equals("org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable")
                    && Boolean.TRUE.equals(var1.getClass().getMethod("getReturnValue").invoke(var1));
            } catch (RuntimeException | ReflectiveOperationException var5) {
                var3 = false;
            }

            if (var3 && ironLive(var0.use) && !var0.accepted && !var0.invalid && var0.use.tick == clock(var0.use.player)) {
                var0.accepted = true;
                if (PRIVATE_IRON_USE.get() == var0.use) {
                    PRIVATE_IRON_USE.remove();
                }
            } else {
                cancelNativeIron(var0);
            }
        }
    }

    public static FoodWeaponSourceLedger.NativeIronPaymentToken nativeIronPostEvent(
        FoodWeaponSourceLedger.NativeIronCastToken var0, Object var1, Object var2, Object var3, Object var4, int var5, int var6, float var7
    ) {
        String var8 = "(Lnet/minecraft/world/level/Level;ILnet/minecraft/server/level/ServerPlayer;Lio/redspace/ironsspellbooks/api/spells/CastSource;Z)V";
        return var0 != null
                && ironBoundary(
                    "io.redspace.ironsspellbooks.api.spells.AbstractSpell",
                    "ftboss$v9$ironPost",
                    "(Lnet/neoforged/bus/api/IEventBus;Lnet/neoforged/bus/api/Event;Lnet/minecraft/world/level/Level;ILnet/minecraft/server/level/ServerPlayer;Lio/redspace/ironsspellbooks/api/spells/CastSource;Z)Lnet/neoforged/bus/api/Event;",
                    "castSpell",
                    var8
                )
                && ironLive(var0.use)
                && var0.accepted
                && !var0.invalid
                && var1 == var0.spell
                && var2 == var0.castSource
                && var3 != null
                && var3.getClass().getName().equals("io.redspace.ironsspellbooks.api.magic.MagicData")
                && var4 != null
                && var4.getClass().getName().equals("io.redspace.ironsspellbooks.api.events.SpellOnCastEvent")
                && var5 > 0
                && var6 >= 0
                && Float.isFinite(var7)
                && !(var7 < 0.0F)
            ? new FoodWeaponSourceLedger.NativeIronPaymentToken(var0, var3, var4, var5, var6, var7)
            : null;
    }

    public static void nativeIronManaReturned(FoodWeaponSourceLedger.NativeIronPaymentToken var0, Object var1, float var2) {
        String var3 = "(Lnet/minecraft/world/level/Level;ILnet/minecraft/server/level/ServerPlayer;Lio/redspace/ironsspellbooks/api/spells/CastSource;Z)V";
        if (var0 != null
            && ironBoundary(
                "io.redspace.ironsspellbooks.api.spells.AbstractSpell",
                "ftboss$v9$ironMana",
                "(Lio/redspace/ironsspellbooks/api/magic/MagicData;F)V",
                "castSpell",
                var3
            )) {
            if (!var0.mana
                && !var0.returned
                && !var0.invalid
                && var0.magicData == var1
                && ironLive(var0.cast.use)
                && var0.tick == clock(var0.cast.use.player)
                && Float.isFinite(var2)
                && !(var2 < 0.0F)
                && Float.compare(var2, Math.max(0.0F, var0.manaBefore - (float)var0.manaCost)) == 0) {
                var0.mana = true;
                var0.manaAfter = var2;
            } else {
                var0.invalid = true;
            }
        }
    }

    public static void nativeIronCastCallbackReturned(FoodWeaponSourceLedger.NativeIronPaymentToken var0, Object var1, Object var2, Object var3, int var4) {
        String var5 = "(Lnet/minecraft/world/level/Level;ILnet/minecraft/server/level/ServerPlayer;Lio/redspace/ironsspellbooks/api/spells/CastSource;Z)V";
        if (var0 != null
            && ironBoundary(
                "io.redspace.ironsspellbooks.api.spells.AbstractSpell",
                "ftboss$v9$ironCast",
                "(Lio/redspace/ironsspellbooks/api/spells/AbstractSpell;Lnet/minecraft/world/level/Level;ILnet/minecraft/world/entity/LivingEntity;Lio/redspace/ironsspellbooks/api/spells/CastSource;Lio/redspace/ironsspellbooks/api/magic/MagicData;)V",
                "castSpell",
                var5
            )) {
            if (var0.mana
                && !var0.callback
                && !var0.returned
                && !var0.invalid
                && var0.cast.spell == var1
                && var0.magicData == var2
                && var0.cast.castSource == var3
                && var0.level == var4
                && ironLive(var0.cast.use)
                && var0.tick == clock(var0.cast.use.player)) {
                var0.callback = true;
            } else {
                var0.invalid = true;
            }
        }
    }

    public static FoodWeaponSourceLedger.NativeIronReceipt nativeIronCastReturned(FoodWeaponSourceLedger.NativeIronPaymentToken var0) {
        String var1 = "(Lnet/minecraft/world/level/Level;ILnet/minecraft/server/level/ServerPlayer;Lio/redspace/ironsspellbooks/api/spells/CastSource;Z)V";
        if (var0 == null
            || !ironBoundary(
                "io.redspace.ironsspellbooks.api.spells.AbstractSpell",
                "ftboss$v9$ironCastReturn",
                var1.substring(0, var1.indexOf(41)) + "Lorg/spongepowered/asm/mixin/injection/callback/CallbackInfo;)V",
                "castSpell",
                var1
            )) {
            return null;
        } else if (var0.mana
            && var0.callback
            && !var0.returned
            && !var0.invalid
            && !var0.cast.invalid
            && ironLive(var0.cast.use)
            && var0.tick == clock(var0.cast.use.player)) {
            CompoundTag var2 = new CompoundTag();
            var2.putInt("NativeIronIngressVersion", 1);
            var2.putUUID("AttackId", var0.cast.action);
            var2.putUUID("Owner", var0.cast.use.player.getUUID());
            var2.putUUID("WeaponId", FoodWeaponLifecycle.weaponId(var0.cast.use.actual));
            var2.putUUID("ServerEpoch", var0.cast.use.state.epoch);
            var2.putUUID("OwnerEpoch", var0.cast.use.ownerEpoch);
            var2.putString("OriginalHand", var0.cast.use.hand.name());
            var2.putString("Dimension", var0.cast.use.dimension);
            var2.putString("FoundationApprovedRules", var0.cast.use.rules);
            var2.putString("CastSource", var0.cast.source);
            var2.putString("Slot", var0.cast.slot);
            var2.putString("SpellClass", var0.cast.spell.getClass().getName());
            var2.putInt("SelectedLevel", var0.cast.selectedLevel);
            var2.putInt("EffectiveLevel", var0.level);
            var2.putInt("EffectiveManaCost", var0.manaCost);
            var2.putLong("CapturedAt", var0.tick);
            var2.put("Weapon", NativeFoundationItemData.image(var0.cast.use.actual));
            String var3 = signature(var2, var0.cast.use.state);
            if (var3.isEmpty()) {
                var0.invalid = true;
                return null;
            } else {
                var0.returned = true;
                return new FoodWeaponSourceLedger.NativeIronReceipt(var0, var2, var3);
            }
        } else {
            var0.invalid = true;
            return null;
        }
    }

    public static boolean matchesNativeIron(
        FoodWeaponSourceLedger.NativeIronReceipt var0, Object var1, Object var2, Object var3, String var4, ServerPlayer var5
    ) {
        if (var0 != null
            && !var0.payment.invalid
            && var0.payment.returned
            && !var0.payment.cast.invalid
            && var5 == var0.payment.cast.use.player
            && ironLive(var0.payment.cast.use)
            && var0.payment.cast.spell == var1
            && var0.payment.magicData == var2
            && var0.payment.cast.castSource == var3
            && var0.payment.cast.slot.equals(var4)
            && var0.image.getUUID("WeaponId").equals(FoodWeaponLifecycle.weaponId(var0.payment.cast.use.actual))) {
            String var6 = signature(var0.image, var0.payment.cast.use.state);
            return !var6.isEmpty() && MessageDigest.isEqual(var0.proof.getBytes(StandardCharsets.US_ASCII), var6.getBytes(StandardCharsets.US_ASCII));
        } else {
            return false;
        }
    }

    public static synchronized void cancelNativeIron(FoodWeaponSourceLedger.NativeIronCastToken var0) {
        if (var0 != null) {
            var0.invalid = true;
            var0.use.invalid = true;
            if (PRIVATE_IRON_USE.get() == var0.use) {
                PRIVATE_IRON_USE.remove();
            }

            FoodWeaponSourceLedger.NativeIronCallbackToken var1 = PRIVATE_IRON_CALLBACK.get();
            if (var1 != null && var1.payment.cast == var0) {
                var1.closed = true;
                PRIVATE_IRON_CALLBACK.remove();
                FoodWeaponSourceLedger.NativeHurtScope var2 = PRIVATE_NATIVE_HURT.get();
                if (var2 != null && var2.ironCallback == var1) {
                    closeNativeHurtScope(var2);
                }
            }
        }
    }

    public static FoodWeaponSourceLedger.NativeCapabilityToken nativeIngressRushReturned(Object var0) {
        return nativeIngressRushReturned(var0, null);
    }

    public static FoodWeaponSourceLedger.NativeCapabilityToken nativeIngressRushReturned(Object var0, FoodWeaponSourceLedger.NativeIssuerCustody var1) {
        FoodWeaponSourceLedger.NativeIssueWindow var2 = new FoodWeaponSourceLedger.NativeIssueWindow(null);
        FoodWeaponSourceLedger.NativeIngressToken var3 = PRIVATE_INGRESS.get();
        String var4 = "com.github.L_Ender.cataclysm.capabilities.RenderRushCapability$RenderRushCapabilityImp";
        if (ironBoundary(var4, "ftboss$v9$rushReturn", "(ZLorg/spongepowered/asm/mixin/injection/callback/CallbackInfo;)V", "setRush", "(Z)V")
            && ingressLive(var3)
            && var3.kind == FoodWeaponSourceLedger.NativeIngressKind.SOUL
            && !var3.returned
            && var3.tick == clock(var3.player)
            && var0 != null
            && var0.getClass().getName().equals(var4)
            && var3.capability == null) {
            FoodWeaponSourceLedger.NativeCapabilityToken var5 = new FoodWeaponSourceLedger.NativeCapabilityToken(var3, var0);
            if (!var2.unchanged()) {
                return null;
            } else if (var1 != null && !var1.cut.reserve(var5)) {
                return null;
            } else {
                if (var1 != null) {
                    var1.capability = var5;
                }

                boolean var6 = false;

                FoodWeaponSourceLedger.NativeCapabilityToken var7;
                try {
                    var3.capability = var5;
                    var6 = true;
                    var7 = var5;
                } finally {
                    if (!var6) {
                        if (var1 != null) {
                            closeNativeIssuerCustody(var1);
                        } else {
                            var5.canceled = true;
                        }
                    }
                }

                return var7;
            }
        } else {
            return null;
        }
    }

    public static boolean matchesNativeCapability(
        FoodWeaponSourceLedger.NativeIngressReceipt var0, FoodWeaponSourceLedger.NativeCapabilityToken var1, Object var2, LivingEntity var3
    ) {
        return var1 != null
            && var0 != null
            && var1.ingress == var0.ingress
            && var1.capability == var2
            && !var1.canceled
            && var3 == var1.ingress.player
            && matchesNativeIngress(var0, var1.ingress.player);
    }

    public static synchronized void cancelNativeCapability(FoodWeaponSourceLedger.NativeCapabilityToken var0) {
        if (var0 != null) {
            var0.canceled = true;
            cancelNativeIngress(var0.ingress);
        }
    }

    private static FoodWeaponSourceLedger.NativeIngressKind ingressKind(ItemStack var0) {
        if (var0 != null && !var0.isEmpty() && var0.getCount() == 1) {
            String var1 = var0.getItem().getClass().getName();
            String var2 = FourWeaponFoundationAdapter.registryId(var0);

            for (FoodWeaponSourceLedger.NativeIngressKind var6 : FoodWeaponSourceLedger.NativeIngressKind.values()) {
                if (var6.itemClass.equals(var1) && var6.registryId.equals(var2)) {
                    return var6;
                }
            }

            return null;
        } else {
            return null;
        }
    }

    private static boolean ingressEntry(FoodWeaponSourceLedger.NativeIngressKind var0, String var1, String var2) {
        return switch (var0) {
            case ENDER, MEAT -> (var1.equals("onUseTick") || var1.equals("m_5929_"))
            && var2.equals("(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;I)V");
            case SOUL, CORAL_SPEAR, CORAL_BARDICHE -> (var1.equals("releaseUsing") || var1.equals("m_5551_"))
            && var2.equals("(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/LivingEntity;I)V");
            case TIDAL -> var1.equals("onLeftClick") && var2.equals("(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/LivingEntity;)Z");
            case ANCIENT -> var1.equals("onLeftClick") && var2.equals("(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/LivingEntity;)Z")
            || (var1.equals("hurtEnemy") || var1.equals("m_7579_"))
                && var2.equals("(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/entity/LivingEntity;)Z");
        };
    }

    private static boolean nativeIngressHandler202(StackFrame frame, String originalName, String entry) {
        if (frame.getMethodName().equals(originalName)) {
            return true;
        }
        if (!entry.equals("onUseTick")
            || !List.of("ftboss$v9$nativeBegin", "ftboss$v9$nativeReturn").contains(originalName)
            || !frame.getMethodName().matches("handler\\$[a-z0-9]{6,16}\\$" + java.util.regex.Pattern.quote(originalName))) {
            return false;
        }
        try {
            java.lang.reflect.Method method = frame.getDeclaringClass().getDeclaredMethod(frame.getMethodName(), frame.getMethodType().parameterArray());
            org.spongepowered.asm.mixin.transformer.meta.MixinMerged merged = method.getDeclaredAnnotation(org.spongepowered.asm.mixin.transformer.meta.MixinMerged.class);
            return merged != null && merged.mixin().equals("net.farmingtales.ftboss.mixin.NativeUseTickIngressMixin");
        } catch (LinkageError | SecurityException | ReflectiveOperationException failure) {
            return false;
        }
    }
    private static boolean ingressBoundary(FoodWeaponSourceLedger.NativeIngressKind var0, String var1, String var2, String var3) {
        return NATIVE_CALLER.walk(
            var4 -> {
                List var5 = var4.filter(var0xx -> var0xx.getDeclaringClass() != FoodWeaponSourceLedger.class).limit(2L).toList();
                if (var5.size() != 2) {
                    return false;
                } else {
                    StackFrame var6 = (StackFrame)var5.get(0);
                    StackFrame var7 = (StackFrame)var5.get(1);
                    String var8 = var3.endsWith("Z")
                        ? "Lorg/spongepowered/asm/mixin/injection/callback/CallbackInfoReturnable;"
                        : "Lorg/spongepowered/asm/mixin/injection/callback/CallbackInfo;";
                    String var9 = var3.substring(0, var3.indexOf(41)) + var8 + ")V";
                    return var6.getClassName().equals(var0.itemClass)
                        && nativeIngressHandler202(var6, "ftboss$v9$native" + var1, var2)
                        && var6.getDescriptor().equals(var9)
                        && var7.getDeclaringClass() == var6.getDeclaringClass()
                        && var7.getMethodName().equals(var2)
                        && var7.getDescriptor().equals(var3)
                        && ingressEntry(var0, var2, var3);
                }
            }
        );
    }

    private static boolean ingressLive(FoodWeaponSourceLedger.NativeIngressToken var0) {
        if (var0 != null
            && OwnerRepairGenerationAuthority.matches(var0.actual, var0.repairGeneration)
            && !var0.invalid
            && OwnerFullRepairGuard.permits(var0.actual)
            && var0.thread == Thread.currentThread()
            && var0.player != null
            && !var0.player.isRemoved()
            && var0.player.isAlive()
            && !var0.player.isSpectator()
            && !var0.player.level().isClientSide
            && server(var0.player).isSameThread()) {
            FoodWeaponSourceLedger.ServerState var1 = SERVERS.get(server(var0.player));
            long var2 = clock(var0.player);
            return var1 == var0.state
                && Objects.equals(ownerEpoch(var1, var0.player.getUUID()), var0.ownerEpoch)
                && var0.dimension.equals(var0.player.level().dimension().location().toString())
                && var0.tick >= 0L
                && var0.tick <= 9223372036854774607L
                && var2 >= var0.tick
                && var2 < var0.tick + 1200L
                && var2 >= var1.latestTick;
        } else {
            return false;
        }
    }

    public static FoodWeaponSourceLedger.NativeIngressToken nativeIngressBegin(
        ItemStack var0, LivingEntity var1, InteractionHand var2, String var3, String var4
    ) {
        return nativeIngressBegin(var0, var1, var2, var3, var4, null);
    }

    public static FoodWeaponSourceLedger.NativeIngressToken nativeIngressBegin(
        ItemStack var0, LivingEntity var1, InteractionHand var2, String var3, String var4, FoodWeaponSourceLedger.NativeIssuerCustody var5
    ) {
        FoodWeaponSourceLedger.NativeIssueWindow var6 = new FoodWeaponSourceLedger.NativeIssueWindow(var1 instanceof ServerPlayer var7 ? var7 : null);
        FoodWeaponSourceLedger.NativeIngressKind var16 = ingressKind(var0);
        if (var16 == null
            || !(var1 instanceof ServerPlayer var8)
            || var2 == null
            || !ingressBoundary(var16, "Begin", var3, var4)
            || var8.getItemInHand(var2) != var0
            || !var8.isAlive()
            || var8.isRemoved()
            || var8.isSpectator()
            || var8.level().isClientSide
            || !server(var8).isSameThread()
            || FoodWeaponLifecycle.isBroken(var0)
            || FoodWeaponLifecycle.weaponId(var0) == null
            || !canonicalInputValid(NativeFoundationItemData.image(var0))
            || FoodWeaponMaterialFingerprint.foundationSnapshot(var0).isEmpty()
            || foundationRules(var8, var0).isEmpty()) {
            return null;
        }

        if (!var6.unchanged()) {
            return null;
        } else {
            FoodWeaponSourceLedger.NativeIngressToken var9 = PRIVATE_INGRESS.get();
            if (var9 != null) {
                var9.invalid = true;
                PRIVATE_INGRESS.remove();
                PRIVATE_INGRESS_WEAR.remove();
                return null;
            } else if (NATIVE_DEPTH.get() == 0 && NATIVE_OPERATION.get() == null) {
                FoodWeaponSourceLedger.NativeIngressToken var10 = new FoodWeaponSourceLedger.NativeIngressToken(var16, var8, var0, var2, var3, var4);
                if (!retireOriginalIssuerRoot(var6)) {
                    return null;
                } else if (!var6.unchanged()) {
                    return null;
                } else if (var5 != null && !var5.cut.reserve(var10)) {
                    return null;
                } else {
                    if (var5 != null) {
                        var5.ingress = var10;
                    }

                    boolean var11 = false;

                    FoodWeaponSourceLedger.NativeIngressToken var12;
                    try {
                        PRIVATE_INGRESS.set(var10);
                        var11 = true;
                        var12 = var10;
                    } finally {
                        if (!var11) {
                            if (var5 != null) {
                                closeNativeIssuerCustody(var5);
                            } else {
                                var10.invalid = true;
                                var10.expectation.closed = true;
                                var10.expectation.cursor.close();
                                if (PRIVATE_INGRESS.get() == var10) {
                                    PRIVATE_INGRESS.remove();
                                }
                            }
                        }
                    }

                    return var12;
                }
            } else {
                return null;
            }
        }
    }

    private static boolean ingressWearBoundary(String var0) {
        return NATIVE_CALLER.walk(
            var1 -> {
                List var2 = var1.filter(var0xx -> var0xx.getDeclaringClass() != FoodWeaponSourceLedger.class).limit(2L).toList();
                if (var2.size() != 2) {
                    return false;
                } else {
                    StackFrame var3 = (StackFrame)var2.get(0);
                    StackFrame var4 = (StackFrame)var2.get(1);
                    return var3.getDeclaringClass() == ItemStack.class
                        && NativeWeaponIngressSites.matches(var3, "net.minecraft.world.item.ItemStack", "ftboss$v9$nativeWear" + var0, "(ILnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/entity/EquipmentSlot;Lorg/spongepowered/asm/mixin/injection/callback/CallbackInfo;)V")
                        && var3.getDescriptor()
                            .equals(
                                "(ILnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/entity/EquipmentSlot;Lorg/spongepowered/asm/mixin/injection/callback/CallbackInfo;)V"
                            )
                        && var4.getDeclaringClass() == ItemStack.class
                        && var4.getMethodName().equals("hurtAndBreak")
                        && var4.getDescriptor().equals("(ILnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/entity/EquipmentSlot;)V");
                }
            }
        );
    }

    public static FoodWeaponSourceLedger.NativeWearToken nativeIngressWearBegin(ItemStack var0, int var1, LivingEntity var2) {
        return nativeIngressWearBegin(var0, var1, var2, null);
    }

    public static FoodWeaponSourceLedger.NativeWearToken nativeIngressWearBegin(
        ItemStack var0, int var1, LivingEntity var2, FoodWeaponSourceLedger.NativeIssuerCustody var3
    ) {
        FoodWeaponSourceLedger.NativeIssueWindow var4 = new FoodWeaponSourceLedger.NativeIssueWindow(var2 instanceof ServerPlayer var5 ? var5 : null);
        FoodWeaponSourceLedger.NativeIngressToken var12 = PRIVATE_INGRESS.get();
        if (ingressWearBoundary("Begin")
            && ingressLive(var12)
            && !var12.returned
            && var12.tick == clock(var12.player)
            && var0 == var12.actual
            && var2 == var12.player
            && var1 == 1
            && var12.kind.wear != 0
            && var12.player.getItemInHand(var12.hand) == var0
            && PRIVATE_INGRESS_WEAR.get() == null) {
            FoodWeaponSourceLedger.NativeWearToken var6 = new FoodWeaponSourceLedger.NativeWearToken(var12);
            if (!var4.unchanged()) {
                return null;
            } else if (var3 != null && !var3.cut.reserve(var6)) {
                return null;
            } else {
                if (var3 != null) {
                    var3.wear = var6;
                }

                boolean var7 = false;

                FoodWeaponSourceLedger.NativeWearToken var8;
                try {
                    PRIVATE_INGRESS_WEAR.set(var6);
                    var7 = true;
                    var8 = var6;
                } finally {
                    if (!var7) {
                        if (var3 != null) {
                            closeNativeIssuerCustody(var3);
                        } else {
                            var6.ingress.invalid = true;
                            if (PRIVATE_INGRESS_WEAR.get() == var6) {
                                PRIVATE_INGRESS_WEAR.remove();
                            }
                        }
                    }
                }

                return var8;
            }
        } else {
            if (var12 != null) {
                var12.invalid = true;
            }

            return null;
        }
    }

    public static void nativeIngressWearReturned(FoodWeaponSourceLedger.NativeWearToken var0, ItemStack var1, int var2, LivingEntity var3) {
        if (ingressWearBoundary("Return")) {
            FoodWeaponSourceLedger.NativeWearToken var4 = PRIVATE_INGRESS_WEAR.get();
            if (var0 != null
                && var4 == var0
                && !var0.returned
                && var1 == var0.ingress.actual
                && var3 == var0.ingress.player
                && var2 == 1
                && ingressLive(var0.ingress)
                && var0.ingress.tick == clock(var0.ingress.player)) {
                var0.returned = true;
                if (authorizedWear(var0.ingress.player, var0.before, var0.rules, var1, var2, var0.writes, var0.requested)) {
                    var0.ingress.wears++;
                } else {
                    var0.ingress.invalid = true;
                }

                PRIVATE_INGRESS_WEAR.remove();
            } else {
                if (var4 != null) {
                    var4.ingress.invalid = true;
                }

                PRIVATE_INGRESS_WEAR.remove();
            }
        }
    }

    private static boolean ingressAddedBoundary() {
        return NATIVE_CALLER.walk(
            var0 -> {
                List var1 = var0.filter(var0x -> var0x.getDeclaringClass() != FoodWeaponSourceLedger.class).limit(2L).toList();
                if (var1.size() != 2) {
                    return false;
                } else {
                    StackFrame var2 = (StackFrame)var1.get(0);
                    StackFrame var3 = (StackFrame)var1.get(1);
                    return var2.getDeclaringClass() == ServerLevel.class
                        && (var2.getMethodName().equals("nativeAdded") || var2.getMethodName().equals("nativeAddedSrg"))
                        && var2.getDescriptor()
                            .equals("(Lnet/minecraft/world/entity/Entity;Lorg/spongepowered/asm/mixin/injection/callback/CallbackInfoReturnable;)V")
                        && var3.getDeclaringClass() == var2.getDeclaringClass()
                        && (var3.getMethodName().equals("addFreshEntity") || var3.getMethodName().equals("m_7967_"))
                        && var3.getDescriptor().equals("(Lnet/minecraft/world/entity/Entity;)Z");
                }
            }
        );
    }

    private static boolean ingressCarrier(FoodWeaponSourceLedger.NativeIngressKind var0, Entity var1) {
        if (var1 == null) {
            return false;
        } else {
            String var2 = var1.getClass().getName();

            return switch (var0) {
                case ENDER -> var2.equals("net.gqstavo.armamentarium.entity.MiteArrowEntity")
                || var2.equals("net.gqstavo.armamentarium.entity.MitePelletEntity");
                case MEAT -> false;
                case SOUL -> var2.equals("com.github.L_Ender.cataclysm.entity.projectile.Phantom_Halberd_Entity");
                case CORAL_SPEAR -> var2.equals("com.github.L_Ender.cataclysm.entity.projectile.ThrownCoral_Spear_Entity");
                case CORAL_BARDICHE -> var2.equals("com.github.L_Ender.cataclysm.entity.projectile.ThrownCoral_Bardiche_Entity");
                case TIDAL -> var2.equals("com.github.L_Ender.cataclysm.entity.projectile.Tidal_Tentacle_Entity");
                case ANCIENT -> var2.equals("com.github.L_Ender.cataclysm.entity.projectile.Sandstorm_Projectile");
            };
        }
    }

    private static boolean ingressSpawnOrigin(FoodWeaponSourceLedger.NativeIngressToken var0, Entity var1) {
        return var0 != null && var1 != null
            ? NATIVE_CALLER.walk(
                var2 -> {
                    List var3 = var2.filter(var0xx -> var0xx.getDeclaringClass() != FoodWeaponSourceLedger.class).limit(6L).toList();
                    if (var3.size() < 3) {
                        return false;
                    } else {
                        StackFrame var4 = (StackFrame)var3.get(2);
                        if (var4.getDeclaringClass().getClassLoader() != var0.actual.getItem().getClass().getClassLoader()) {
                            return false;
                        } else {
                            byte var5 = 2;

                            boolean var6 = switch (var0.kind) {
                                case ENDER -> {
                                    var5 = 3;
                                    String var7 = var1.getClass().getName();
                                    String var8 = "(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/util/RandomSource;FDI)L"
                                        + var7.replace('.', '/')
                                        + ";";
                                    yield var4.getDeclaringClass() == var1.getClass()
                                        && var4.getMethodName().equals("shoot")
                                        && var4.getDescriptor().equals(var8);
                                }
                                case MEAT -> false;
                                case SOUL -> {
                                    var5 = 4;
                                    yield var4.getClassName().equals(var0.kind.itemClass)
                                        && var4.getMethodName().equals("spawnHalberd")
                                        && var4.getDescriptor().equals("(DDDDFILnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/LivingEntity;)V")
                                        && var3.size() > 3
                                        && ((StackFrame)var3.get(3)).getDeclaringClass() == var4.getDeclaringClass()
                                        && ((StackFrame)var3.get(3)).getMethodName().equals("StrikeWindmillHalberd")
                                        && ((StackFrame)var3.get(3))
                                            .getDescriptor()
                                            .equals("(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/LivingEntity;IIDDDI)V");
                                }
                                case CORAL_SPEAR, CORAL_BARDICHE -> true;
                                case TIDAL -> {
                                    var5 = 3;
                                    yield var4.getClassName().equals(var0.kind.itemClass)
                                        && var4.getMethodName().equals("launchTendonsAt")
                                        && var4.getDescriptor()
                                            .equals(
                                                "(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/entity/Entity;)Z"
                                            );
                                }
                                case ANCIENT -> {
                                    var5 = 3;
                                    yield var4.getClassName().equals(var0.kind.itemClass)
                                        && var4.getMethodName().equals("launchTornado")
                                        && var4.getDescriptor().equals("(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/LivingEntity;)Z");
                                }
                            };
                            return var6
                                && var3.size() > var5
                                && ((StackFrame)var3.get(var5)).getClassName().equals(var0.kind.itemClass)
                                && ((StackFrame)var3.get(var5)).getMethodName().equals(var0.entry)
                                && ((StackFrame)var3.get(var5)).getDescriptor().equals(var0.descriptor);
                        }
                    }
                }
            )
            : false;
    }

    public static FoodWeaponSourceLedger.NativeCarrierToken nativeIngressAdded(Entity var0) {
        return nativeIngressAdded(var0, null);
    }

    public static FoodWeaponSourceLedger.NativeCarrierToken nativeIngressAdded(Entity var0, FoodWeaponSourceLedger.NativeIssuerCustody var1) {
        FoodWeaponSourceLedger.NativeIssueWindow var2 = new FoodWeaponSourceLedger.NativeIssueWindow(null);
        FoodWeaponSourceLedger.NativeIngressToken var3 = PRIVATE_INGRESS.get();
        if (ingressAddedBoundary()
            && ingressLive(var3)
            && !var3.returned
            && var3.tick == clock(var3.player)
            && ingressCarrier(var3.kind, var0)
            && ingressSpawnOrigin(var3, var0)
            && var0.level() == var3.player.level()
            && !var0.isRemoved()
            && var3.player.serverLevel().getEntity(var0.getUUID()) == var0) {
            FoodWeaponSourceLedger.NativeCarrierToken var4 = var3.carriers.stream().filter(var1x -> var1x.carrier == var0).findFirst().orElse(null);
            if (var4 != null && var4.added) {
                return null;
            } else {
                boolean var5 = var4 == null;
                if (var5) {
                    var4 = new FoodWeaponSourceLedger.NativeCarrierToken(var3, var0);
                }

                if (!var2.unchanged()) {
                    return null;
                } else if (var1 != null && !var1.cut.reserve(var4)) {
                    return null;
                } else {
                    if (var1 != null) {
                        var1.carrier = var4;
                    }

                    boolean var6 = false;

                    FoodWeaponSourceLedger.NativeCarrierToken var7;
                    try {
                        if (var5) {
                            var3.carriers.add(var4);
                        }

                        var4.added = true;
                        var6 = true;
                        var7 = var4;
                    } finally {
                        if (!var6) {
                            if (var1 != null) {
                                closeNativeIssuerCustody(var1);
                            } else {
                                var4.actor = false;
                                var4.added = false;
                                var4.image = null;
                            }
                        }
                    }

                    return var7;
                }
            }
        } else {
            return null;
        }
    }

    public static FoodWeaponSourceLedger.NativeCarrierToken nativeIngressActorAtCreation(Entity var0, LivingEntity var1) {
        return nativeIngressActorAtCreation(var0, var1, null);
    }

    public static FoodWeaponSourceLedger.NativeCarrierToken nativeIngressActorAtCreation(
        Entity var0, LivingEntity var1, FoodWeaponSourceLedger.NativeIssuerCustody var2
    ) {
        FoodWeaponSourceLedger.NativeIssueWindow var3 = new FoodWeaponSourceLedger.NativeIssueWindow(var1 instanceof ServerPlayer var4 ? var4 : null);
        FoodWeaponSourceLedger.NativeIngressToken var12 = PRIVATE_INGRESS.get();
        String var5 = "com.github.L_Ender.cataclysm.entity.projectile.Phantom_Halberd_Entity";
        if (ironBoundary(
                var5,
                "ftboss$afterCaster",
                "(Lnet/minecraft/world/entity/LivingEntity;Lorg/spongepowered/asm/mixin/injection/callback/CallbackInfo;)V",
                "setCaster",
                "(Lnet/minecraft/world/entity/LivingEntity;)V"
            )
            && ingressLive(var12)
            && !var12.returned
            && var12.kind == FoodWeaponSourceLedger.NativeIngressKind.SOUL
            && var1 == var12.player
            && ingressCarrier(var12.kind, var0)
            && var0.level() == var1.level()
            && !var0.isRemoved()
            && !var12.carriers.stream().anyMatch(var1x -> var1x.carrier == var0)) {
            FoodWeaponSourceLedger.NativeCarrierToken var6 = new FoodWeaponSourceLedger.NativeCarrierToken(var12, var0);
            if (!var3.unchanged()) {
                return null;
            } else if (var2 != null && !var2.cut.reserve(var6)) {
                return null;
            } else {
                if (var2 != null) {
                    var2.carrier = var6;
                }

                boolean var7 = false;

                FoodWeaponSourceLedger.NativeCarrierToken var8;
                try {
                    var6.actor = true;
                    var12.carriers.add(var6);
                    var7 = true;
                    var8 = var6;
                } finally {
                    if (!var7) {
                        if (var2 != null) {
                            closeNativeIssuerCustody(var2);
                        } else {
                            var6.actor = false;
                            var6.added = false;
                            var6.image = null;
                        }
                    }
                }

                return var8;
            }
        } else {
            return null;
        }
    }

    public static FoodWeaponSourceLedger.NativeCarrierToken nativeIngressChild(
        FoodWeaponSourceLedger.NativeIngressReceipt var0, FoodWeaponSourceLedger.NativeCarrierToken var1, Entity var2, UUID var3
    ) {
        return nativeIngressChild(var0, var1, var2, var3, null);
    }

    public static FoodWeaponSourceLedger.NativeCarrierToken nativeIngressChild(
        FoodWeaponSourceLedger.NativeIngressReceipt var0,
        FoodWeaponSourceLedger.NativeCarrierToken var1,
        Entity var2,
        UUID var3,
        FoodWeaponSourceLedger.NativeIssuerCustody var4
    ) {
        FoodWeaponSourceLedger.NativeIssueWindow var5 = new FoodWeaponSourceLedger.NativeIssueWindow(var0 == null ? null : var0.ingress.player);
        if (matchesNativeIngress(var0, var1, var0 == null ? null : var0.ingress.player)
            && var1.ingress.kind == FoodWeaponSourceLedger.NativeIngressKind.TIDAL
            && var2 != var1.carrier
            && ingressCarrier(FoodWeaponSourceLedger.NativeIngressKind.TIDAL, var2)
            && var2.level() == var1.carrier.level()
            && !var2.isRemoved()
            && var1.ingress.player.getUUID().equals(var3)) {
            boolean var6 = NATIVE_CALLER.walk(
                var0x -> {
                    List var1x = var0x.filter(var0xx -> var0xx.getDeclaringClass() != FoodWeaponSourceLedger.class).limit(3L).toList();
                    return var1x.size() == 3
                        && ((StackFrame)var1x.get(0)).getClassName().equals("net.farmingtales.ftboss.item.NativeSpecialCaller")
                        && ((StackFrame)var1x.get(0)).getMethodName().equals("tentacleChild")
                        && ((StackFrame)var1x.get(0))
                            .getDescriptor()
                            .equals("(Lnet/minecraft/world/entity/Entity;Ljava/util/UUID;Lnet/minecraft/world/entity/Entity;)V")
                        && ((StackFrame)var1x.get(1)).getClassName().equals("com.github.L_Ender.cataclysm.entity.projectile.Tidal_Tentacle_Entity")
                        && ((StackFrame)var1x.get(1)).getMethodName().equals("ftboss$childActor")
                        && ((StackFrame)var1x.get(1)).getDescriptor().equals("(Ljava/lang/Object;Ljava/util/UUID;)V")
                        && ((StackFrame)var1x.get(2)).getDeclaringClass() == ((StackFrame)var1x.get(1)).getDeclaringClass()
                        && ((StackFrame)var1x.get(2)).getMethodName().equals("createChain")
                        && ((StackFrame)var1x.get(2)).getDescriptor().equals("(Lnet/minecraft/world/entity/Entity;)V");
                }
            );
            if (var6 && !var1.ingress.carriers.stream().anyMatch(var1x -> var1x.carrier == var2)) {
                FoodWeaponSourceLedger.NativeCarrierToken var7 = new FoodWeaponSourceLedger.NativeCarrierToken(var1.ingress, var2);
                if (!var5.unchanged()) {
                    return null;
                } else if (var4 != null && !var4.cut.reserve(var7)) {
                    return null;
                } else {
                    if (var4 != null) {
                        var4.carrier = var7;
                    }

                    boolean var8 = false;

                    FoodWeaponSourceLedger.NativeCarrierToken var9;
                    try {
                        var7.actor = true;
                        var7.image = var1.image.copy();
                        var1.ingress.carriers.add(var7);
                        var8 = true;
                        var9 = var7;
                    } finally {
                        if (!var8) {
                            if (var4 != null) {
                                closeNativeIssuerCustody(var4);
                            } else {
                                var7.actor = false;
                                var7.added = false;
                                var7.image = null;
                            }
                        }
                    }

                    return var9;
                }
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    public static void nativeIngressChildAdded(FoodWeaponSourceLedger.NativeCarrierToken var0) {
        if (var0 != null
            && !var0.added
            && ingressAddedBoundary()
            && ingressLive(var0.ingress)
            && var0.ingress.returned
            && var0.ingress.kind == FoodWeaponSourceLedger.NativeIngressKind.TIDAL
            && var0.actor
            && var0.image != null
            && !var0.carrier.isRemoved()
            && var0.carrier.level() == var0.ingress.player.level()
            && var0.ingress.player.serverLevel().getEntity(var0.entityId) == var0.carrier) {
            var0.added = true;
        }
    }

    public static void nativeIngressActor(FoodWeaponSourceLedger.NativeCarrierToken var0, Entity var1, LivingEntity var2) {
        if (var0 != null && var0.carrier == var1 && !var0.ingress.returned && ingressLive(var0.ingress) && var2 == var0.ingress.player) {
            boolean var3 = NATIVE_CALLER.walk(
                var0x -> {
                    List var1x = var0x.filter(var0xx -> var0xx.getDeclaringClass() != FoodWeaponSourceLedger.class).limit(2L).toList();
                    return var1x.size() == 2
                        && ((StackFrame)var1x.get(0)).getClassName().equals("com.github.L_Ender.cataclysm.entity.projectile.Phantom_Halberd_Entity")
                        && ((StackFrame)var1x.get(0)).getMethodName().equals("ftboss$afterCaster")
                        && ((StackFrame)var1x.get(0))
                            .getDescriptor()
                            .equals("(Lnet/minecraft/world/entity/LivingEntity;Lorg/spongepowered/asm/mixin/injection/callback/CallbackInfo;)V")
                        && ((StackFrame)var1x.get(1)).getDeclaringClass() == ((StackFrame)var1x.get(0)).getDeclaringClass()
                        && ((StackFrame)var1x.get(1)).getMethodName().equals("setCaster")
                        && ((StackFrame)var1x.get(1)).getDescriptor().equals("(Lnet/minecraft/world/entity/LivingEntity;)V");
                }
            );
            if (var3) {
                var0.actor = true;
            }
        }
    }

    public static void nativeIngressCreator(FoodWeaponSourceLedger.NativeCarrierToken var0, Entity var1, UUID var2) {
        if (var0 != null && var0.carrier == var1 && !var0.ingress.returned && ingressLive(var0.ingress) && var0.ingress.player.getUUID().equals(var2)) {
            boolean var3 = NATIVE_CALLER.walk(
                var0x -> {
                    List var1x = var0x.filter(var0xx -> var0xx.getDeclaringClass() != FoodWeaponSourceLedger.class).limit(2L).toList();
                    return var1x.size() == 2
                        && ((StackFrame)var1x.get(0)).getClassName().equals("com.github.L_Ender.cataclysm.entity.projectile.Tidal_Tentacle_Entity")
                        && ((StackFrame)var1x.get(0)).getMethodName().equals("ftboss$afterCreator")
                        && ((StackFrame)var1x.get(0))
                            .getDescriptor()
                            .equals("(Ljava/util/UUID;Lorg/spongepowered/asm/mixin/injection/callback/CallbackInfo;)V")
                        && ((StackFrame)var1x.get(1)).getDeclaringClass() == ((StackFrame)var1x.get(0)).getDeclaringClass()
                        && ((StackFrame)var1x.get(1)).getMethodName().equals("setCreatorEntityUUID")
                        && ((StackFrame)var1x.get(1)).getDescriptor().equals("(Ljava/util/UUID;)V");
                }
            );
            if (var3) {
                var0.actor = true;
            }
        }
    }

    public static FoodWeaponSourceLedger.NativeIngressReceipt nativeIngressReturned(FoodWeaponSourceLedger.NativeIngressToken var0) {
        if (var0 != null && ingressBoundary(var0.kind, "Return", var0.entry, var0.descriptor)) {
            try {
                int var1 = var0.kind == FoodWeaponSourceLedger.NativeIngressKind.ANCIENT && !var0.entry.equals("onLeftClick") ? 2 : var0.kind.wear;
                if (PRIVATE_INGRESS.get() != var0
                    || !ingressLive(var0)
                    || var0.returned
                    || var0.tick != clock(var0.player)
                    || PRIVATE_INGRESS_WEAR.get() != null
                    || var0.wears != var1
                    || var0.kind != FoodWeaponSourceLedger.NativeIngressKind.CORAL_SPEAR
                        && var0.kind != FoodWeaponSourceLedger.NativeIngressKind.CORAL_BARDICHE
                        && var0.player.getItemInHand(var0.hand) != var0.actual
                    || var0.actual.getCount() != 1
                    || FoodWeaponLifecycle.isBroken(var0.actual)
                    || !var0.rules.equals(foundationRules(var0.player, var0.actual))
                    || var0.kind != FoodWeaponSourceLedger.NativeIngressKind.MEAT
                        && var0.carriers.isEmpty()
                        && (var0.kind != FoodWeaponSourceLedger.NativeIngressKind.SOUL || var0.capability == null || var0.capability.canceled)) {
                    return null;
                } else {
                    for (FoodWeaponSourceLedger.NativeCarrierToken var15 : var0.carriers) {
                        if (!var15.actor
                            || !var15.added
                            || var15.carrier.isRemoved()
                            || var15.carrier.level() != var0.player.level()
                            || !var15.entityId.equals(var15.carrier.getUUID())
                            || var0.player.serverLevel().getEntity(var15.entityId) != var15.carrier) {
                            return null;
                        }
                    }

                    if (var0.kind.wear != 0 || expectationMatches(var0.expectation, var0.actual)) {
                        CompoundTag var13 = NativeFoundationItemData.image(var0.actual).copy();
                        CompoundTag var3 = new CompoundTag();
                        var3.putInt("NativeIngressVersion", 1);
                        var3.putString("NativeEntry", var0.kind.name());
                        var3.putString("NativeCaller", var0.kind.itemClass);
                        var3.putString("NativeMethod", var0.entry);
                        var3.putString("NativeDescriptor", var0.descriptor);
                        var3.putUUID("AttackId", var0.action);
                        var3.putUUID("Owner", var0.player.getUUID());
                        var3.putUUID("WeaponId", FoodWeaponLifecycle.weaponId(var0.actual));
                        var3.putUUID("ServerEpoch", var0.state.epoch);
                        var3.putUUID("OwnerEpoch", var0.ownerEpoch);
                        var3.putString("OriginalHand", var0.hand.name());
                        var3.putString("Dimension", var0.dimension);
                        var3.putString("FoundationApprovedRules", var0.rules);
                        var3.putLong("CapturedAt", var0.tick);
                        var3.putLong("ExpiresAt", var0.tick + 1200L);
                        var3.putInt("NativeWearInvocations", var0.wears);
                        var3.put("Before", var0.before.copy());
                        var3.put("After", var13.copy());
                        ListTag var4 = new ListTag();

                        for (FoodWeaponSourceLedger.NativeCarrierToken var6 : var0.carriers) {
                            CompoundTag var7 = new CompoundTag();
                            var7.putUUID("Entity", var6.entityId);
                            var7.putString("Class", var6.carrier.getClass().getName());
                            var4.add(var7);
                        }

                        var3.put("Carriers", var4);
                        if (var0.capability != null) {
                            var3.putString("CapabilityClass", var0.capability.capability.getClass().getName());
                        }

                        String var17 = signature(var3, var0.state);
                        if (!var17.isEmpty()) {
                            FoodWeaponSourceLedger.NativeIngressReceipt var19 = new FoodWeaponSourceLedger.NativeIngressReceipt(var0, var3, var17);
                            var0.returned = true;

                            for (FoodWeaponSourceLedger.NativeCarrierToken var8 : var0.carriers) {
                                var8.image = var13.copy();
                            }

                            return var19;
                        } else {
                            return null;
                        }
                    } else {
                        return null;
                    }
                }
            } finally {
                if (!var0.returned) {
                    var0.invalid = true;
                }

                if (PRIVATE_INGRESS.get() == var0) {
                    PRIVATE_INGRESS.remove();
                }

                PRIVATE_INGRESS_WEAR.remove();
            }
        } else {
            return null;
        }
    }

    public static boolean matchesNativeIngress(FoodWeaponSourceLedger.NativeIngressReceipt var0, ServerPlayer var1) {
        if (var0 != null
            && var1 == var0.ingress.player
            && var0.ingress.returned
            && ingressLive(var0.ingress)
            && var0.ingress.actual.getCount() == 1
            && !FoodWeaponLifecycle.isBroken(var0.ingress.actual)
            && var0.ingress.rules.equals(foundationRules(var1, var0.ingress.actual))) {
            FoodWeaponSourceLedger.NativeIngressToken var2 = var0.ingress;
            boolean var3 = var2.kind == FoodWeaponSourceLedger.NativeIngressKind.CORAL_SPEAR
                || var2.kind == FoodWeaponSourceLedger.NativeIngressKind.CORAL_BARDICHE;
            if (!var3 && var1.getItemInHand(var2.hand) != var2.actual) {
                return false;
            } else if (!FoodWeaponLifecycle.weaponId(var2.actual).equals(var0.image.getUUID("WeaponId"))) {
                return false;
            } else {
                ItemStack var4 = NativeFoundationItemData.decode(var0.image.getCompound("After"));
                if (var4.getItem() == var2.actual.getItem()
                    && FoodWeaponLifecycle.repairProfile(var4).equals(FoodWeaponLifecycle.repairProfile(var2.actual))
                    && materialFingerprint(var4).equals(materialFingerprint(var2.actual))
                    && socketFingerprint(var4, FoodWeaponFamilyBuild.kind(var4).name())
                        .equals(socketFingerprint(var2.actual, FoodWeaponFamilyBuild.kind(var4).name()))) {
                    String var5 = signature(var0.image, var2.state);
                    return !var5.isEmpty() && MessageDigest.isEqual(var0.proof.getBytes(StandardCharsets.US_ASCII), var5.getBytes(StandardCharsets.US_ASCII));
                } else {
                    return false;
                }
            }
        } else {
            return false;
        }
    }

    public static boolean matchesNativeIngress(
        FoodWeaponSourceLedger.NativeIngressReceipt var0, FoodWeaponSourceLedger.NativeCarrierToken var1, ServerPlayer var2
    ) {
        if (matchesNativeIngress(var0, var2)
            && var1 != null
            && var0.ingress == var1.ingress
            && var1.actor
            && var1.added
            && var1.image != null
            && !var1.carrier.isRemoved()
            && var1.carrier.level() == var2.level()
            && var1.entityId.equals(var1.carrier.getUUID())
            && var2.serverLevel().getEntity(var1.entityId) == var1.carrier) {
            if (var1.carrier instanceof Projectile var3 && var3.getOwner() != var2) {
                return false;
            }

            return true;
        } else {
            return false;
        }
    }

    public static boolean nativeCarrierHurt(
        FoodWeaponSourceLedger.NativeIngressReceipt var0, FoodWeaponSourceLedger.NativeCarrierToken var1, Entity var2, DamageSource var3, float var4
    ) {
        FoodWeaponSourceLedger.NativeHurtScope var5 = PRIVATE_NATIVE_HURT.get();
        if (var5 != null) {
            closeNativeHurtScope(var5);
            return var2.hurt(var3, var4);
        } else {
            FoodWeaponSourceLedger.NativeHurtScope var6 = beginNativeCarrierHurt(var0, var1, var2, var3, var4);
            if (var6 == null) {
                return var2.hurt(var3, var4);
            } else {
                boolean var7;
                try {
                    var7 = var2.hurt(var3, var4);
                } finally {
                    closeNativeHurtScope(var6);
                }

                return var7;
            }
        }
    }

    private static FoodWeaponSourceLedger.NativeHurtScope beginNativeCarrierHurt(
        FoodWeaponSourceLedger.NativeIngressReceipt var0, FoodWeaponSourceLedger.NativeCarrierToken var1, Entity var2, DamageSource var3, float var4
    ) {
        FoodWeaponSourceLedger.NativeIssueWindow var5 = new FoodWeaponSourceLedger.NativeIssueWindow(var0 == null ? null : var0.ingress.player);
        if (var0 != null
            && var1 != null
            && var2 != null
            && var3 != null
            && var3.getClass() == DamageSource.class
            && Float.isFinite(var4)
            && !(var4 <= 0.0F)
            && matchesNativeIngress(var0, var1, var0.ingress.player)
            && nativeHurtCaller(var0.ingress.kind, var1.carrier)
            && !var2.isRemoved()
            && var2.level() == var0.ingress.player.level()
            && var0.ingress.player.serverLevel().getEntity(var2.getUUID()) == var2
            && var3.getDirectEntity() == var1.carrier
            && var3.getEntity() == var0.ingress.player
            && FoodWeaponRanged.allowsWeaponEffects(var3)) {
            String var6 = var3.typeHolder().unwrapKey().map(var0x -> var0x.location().toString()).orElse("");
            if (!var6.isEmpty()
                && (
                    var0.ingress.kind != FoodWeaponSourceLedger.NativeIngressKind.ENDER
                        || var6.equals("armamentarium:cacophony") && Float.compare(var4, 8.0F) == 0
                )) {
                CompoundTag var7 = nativeHurtSnapshot(var0, var1, var2, var3, var4);
                if (var7.isEmpty()) {
                    return null;
                } else {
                    FoodWeaponSourceLedger.NativeHurtScope var8 = new FoodWeaponSourceLedger.NativeHurtScope(var0, var1, var2, var3, var7);
                    if (!var5.unchanged()) {
                        return null;
                    } else {
                        boolean var9 = false;

                        Object var10;
                        try {
                            PRIVATE_NATIVE_HURT.set(var8);
                            if (!validReadOnly(var7, var8.player)) {
                                closeNativeHurtScope(var8);
                                return null;
                            }

                            var8.interaction = beginNativeImpactInteraction(var8, var4);
                            if (var8.interaction != null) {
                                var9 = true;
                                return var8;
                            }

                            closeNativeHurtScope(var8);
                            var10 = null;
                        } finally {
                            if (!var9) {
                                revokeUnreturnedHurt(var8);
                            }
                        }

                        return (FoodWeaponSourceLedger.NativeHurtScope)var10;
                    }
                }
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    private static boolean nativeHurtCaller(FoodWeaponSourceLedger.NativeIngressKind var0, Entity var1) {
        return NATIVE_CALLER.walk(
            var2 -> {
                List var3 = var2.filter(var0xx -> var0xx.getDeclaringClass() != FoodWeaponSourceLedger.class).limit(3L).toList();
                if (var3.size() != 3) {
                    return false;
                } else {
                    StackFrame var4 = (StackFrame)var3.get(0);
                    StackFrame var5 = (StackFrame)var3.get(1);
                    StackFrame var6 = (StackFrame)var3.get(2);
                    if (var4.getClassName().equals("net.farmingtales.ftboss.item.NativeSpecialCaller")
                        && var4.getDeclaringClass().getClassLoader() == FoodWeaponSourceLedger.class.getClassLoader()
                        && var5.getDeclaringClass().getClassLoader() == var1.getClass().getClassLoader()
                        && var5.getDeclaringClass() == var6.getDeclaringClass()) {
                        String var7 = "(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/damagesource/DamageSource;F)Z";
                        if (var0 == FoodWeaponSourceLedger.NativeIngressKind.ENDER) {
                            String var8;
                            if (var1.getClass().getName().equals("net.gqstavo.armamentarium.entity.MiteArrowEntity")) {
                                var8 = "net.gqstavo.armamentarium.procedures.MiteArrowProjectileHitsLivingEntityProcedure";
                            } else {
                                if (!var1.getClass().getName().equals("net.gqstavo.armamentarium.entity.MitePelletEntity")) {
                                    return false;
                                }

                                var8 = "net.gqstavo.armamentarium.procedures.MitePelletProjectileHitsLivingEntityProcedure";
                            }

                            return var4.getMethodName().equals("miteHurt")
                                && var4.getDescriptor().equals(var7)
                                && var5.getClassName().equals(var8)
                                && var5.getMethodName().equals("ftboss$exactMiteHurt")
                                && var5.getDescriptor().equals(var7)
                                && var6.getMethodName().equals("execute")
                                && var6.getDescriptor().equals("(Lnet/minecraft/world/level/LevelAccessor;DDDLnet/minecraft/world/entity/Entity;)V");
                        } else if (!var4.getMethodName().equals("delayedHurt")
                            || !var4.getDescriptor()
                                .equals(
                                    "(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/damagesource/DamageSource;F)Z"
                                )
                            || var5.getDeclaringClass() != var1.getClass()) {
                            return false;
                        } else {
                            return var0 == FoodWeaponSourceLedger.NativeIngressKind.SOUL
                                ? var5.getMethodName().equals("ftboss$halberdNativeHurt")
                                    && var5.getDescriptor()
                                        .equals("(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/damagesource/DamageSource;F)Z")
                                    && var6.getMethodName().equals("damage")
                                    && var6.getDescriptor().equals("(Lnet/minecraft/world/entity/LivingEntity;)V")
                                : var0 == FoodWeaponSourceLedger.NativeIngressKind.TIDAL
                                    && var5.getMethodName().equals("ftboss$tentacleNativeHurt")
                                    && var5.getDescriptor().equals(var7)
                                    && var6.getMethodName().equals("m_8119_")
                                    && var6.getDescriptor().equals("()V");
                        }
                    } else {
                        return false;
                    }
                }
            }
        );
    }

    private static CompoundTag nativeHurtSnapshot(
        FoodWeaponSourceLedger.NativeIngressReceipt var0, FoodWeaponSourceLedger.NativeCarrierToken var1, Entity var2, DamageSource var3, float var4
    ) {
        FoodWeaponSourceLedger.NativeIngressToken var5 = var0.ingress;
        CompoundTag var6 = new CompoundTag();
        var6.putString("DamagePurpose", "NATIVE_CARRIER_HURT");
        var6.putString("NativeEntry", var5.kind.name());
        var6.putUUID("NativeReceiptId", var5.action);
        var6.putString("NativeIngressProof", var0.proof);
        var6.putString("NativeHurtPhase", "ACTUAL_HURT_ADMISSIBLE");
        var6.putString("NativeOuterPhase", "NORMAL_COMPLETED");
        return signActualHurt(
            var5.player,
            NativeFoundationItemData.decode(var1.image),
            var5.hand,
            var5.action,
            var5.state,
            var5.ownerEpoch,
            var5.tick,
            var5.dimension,
            var5.rules,
            var6,
            var2,
            var3,
            var4
        );
    }

    private static CompoundTag signActualHurt(
        ServerPlayer var0,
        ItemStack var1,
        InteractionHand var2,
        UUID var3,
        FoodWeaponSourceLedger.ServerState var4,
        UUID var5,
        long var6,
        String var8,
        String var9,
        CompoundTag var10,
        Entity var11,
        DamageSource var12,
        float var13
    ) {
        String var14 = FoodWeaponFamilyBuild.kind(var1).name();
        String var15 = materialFingerprint(var1);
        String var16 = var12.typeHolder().unwrapKey().map(var0x -> var0x.location().toString()).orElse("");
        if (!var1.isEmpty()
            && var1.getCount() == 1
            && !var15.isEmpty()
            && !var16.isEmpty()
            && List.of("BOW", "STAFF", "MELEE").contains(var14)
            && var12.getDirectEntity() != null) {
            CompoundTag var17 = var10.copy();
            var17.putString("Family", var14);

            try {
                FoodWeaponDamageScaling.freezeFactors(var1, var17, 1.0, 1.0, 1.0);
            } catch (IllegalArgumentException var19) {
                return new CompoundTag();
            }

            if (!Double.isFinite(FoodWeaponDamageScaling.residual(var17))) {
                return new CompoundTag();
            } else {
                var17.putInt("SnapshotVersion", 3);
                var17.putUUID("Owner", var0.getUUID());
                var17.putUUID("AttackId", var3);
                var17.putUUID("WeaponId", FoodWeaponLifecycle.weaponId(var1));
                var17.putUUID("ServerEpoch", var4.epoch);
                var17.putUUID("OwnerEpoch", var5);
                var17.putString("OriginalHand", var2.name());
                var17.putString("Dimension", var8);
                var17.putLong("CapturedAt", var6);
                var17.putLong("ExpiresAt", var6 + 1200L);
                var17.putString("BoundProfile", boundProfile(var1));
                var17.putString("RepairProfile", FoodWeaponLifecycle.repairProfile(var1));
                var17.putString("SourceKind", var14);
                var17.putUUID("NativeEntityId", var12.getDirectEntity().getUUID());
                var17.putUUID("NativeHurtId", UUID.randomUUID());
                var17.putUUID("NativeTargetId", var11.getUUID());
                var17.putLong("NativeHurtAt", clock(var0));
                var17.putString("NativeSourceClass", var12.getClass().getName());
                var17.putString("NativeDamageType", var16);
                var17.putFloat("NativeAmount", var13);
                var17.put("Weapon", NativeFoundationItemData.image(var1));
                var17.putString("MaterialFingerprint", var15);
                var17.putString("SocketFingerprint", socketFingerprint(var1, var14));
                var17.put("FoundationSnapshot", FoodWeaponMaterialFingerprint.foundationSnapshot(var1));
                var17.putString("FoundationApprovedRules", var9);
                if (var17.getCompound("FoundationSnapshot").isEmpty() || !canonicalInputValid(var17)) {
                    return new CompoundTag();
                } else if (!OwnerRepairGenerationAuthority.stamp(var1, var17, var4.revocations)) {
                    return new CompoundTag();
                } else {
                    String var18 = signature(var17, var4);
                    if (var18.isEmpty()) {
                        return new CompoundTag();
                    } else {
                        var17.putString("ServerSignature", var18);
                        return !OwnerFullRepairGuard.bind(var1, var17.getUUID("WeaponId")) ? new CompoundTag() : var17;
                    }
                }
            }
        } else {
            return new CompoundTag();
        }
    }

    private static boolean nativeHurtLive(FoodWeaponSourceLedger.NativeHurtScope var0) {
        if (var0 == null
            || !OwnerRepairGenerationAuthority.snapshotAllowed(var0.snapshot)
            || var0.closed
            || PRIVATE_NATIVE_HURT.get() != var0
            || var0.thread != Thread.currentThread()
            || var0.tick != clock(var0.player)
            || !var0.player.isAlive()
            || var0.player.isSpectator()
            || var0.target.isRemoved()
            || var0.target.level() != var0.player.level()
            || !var0.snapshot.getUUID("NativeTargetId").equals(var0.target.getUUID())
            || var0.player.serverLevel().getEntity(var0.target.getUUID()) != var0.target
            || var0.source.getEntity() != var0.player
            || (var0.ironCallback == null ? var0.source.getClass() != DamageSource.class : !ironActualSource(var0.ironCallback, var0.source))
            || var0.source.getDirectEntity() != (var0.carrier == null ? var0.player : var0.carrier.carrier)
            || !var0.source.typeHolder().unwrapKey().map(var1 -> var1.location().toString().equals(var0.snapshot.getString("NativeDamageType"))).orElse(false)
            || !FoodWeaponRanged.allowsWeaponEffects(var0.source)) {
            return false;
        } else if (var0.ironCallback != null) {
            return ironCallbackReady(var0.ironCallback);
        } else {
            return var0.earlyIngress == null ? matchesNativeIngress(var0.receipt, var0.carrier, var0.player) : meatHurtReady(var0.earlyIngress);
        }
    }

    static void revokeActualHurt(DamageSource var0, ServerPlayer var1) {
        FoodWeaponSourceLedger.NativeHurtScope var2 = PRIVATE_NATIVE_HURT.get();
        if (var2 != null && var2.source == var0 && var2.player == var1) {
            closeNativeHurtScope(var2);
        }
    }

    public static boolean matchesNativeHurt(DamageSource var0, ServerPlayer var1, Entity var2, CompoundTag var3) {
        FoodWeaponSourceLedger.NativeHurtScope var4 = PRIVATE_NATIVE_HURT.get();
        return var4 != null
            && var0 == var4.source
            && var1 == var4.player
            && var2 == var4.target
            && var3 != null
            && var3.equals(var4.snapshot)
            && nativeHurtLive(var4)
            && validReadOnly(var3, var1);
    }

    public static boolean nativeMeatActualHurt(Entity var0, DamageSource var1, float var2) {
        FoodWeaponSourceLedger.NativeHurtScope var3 = PRIVATE_NATIVE_HURT.get();
        if (var3 != null) {
            closeNativeHurtScope(var3);
            return var0.hurt(var1, var2);
        } else {
            FoodWeaponSourceLedger.NativeHurtScope var4 = beginMeatActualHurt(var0, var1, var2);
            if (var4 == null) {
                return var0.hurt(var1, var2);
            } else {
                boolean var5;
                try {
                    var5 = var0.hurt(var1, var2);
                } finally {
                    closeNativeHurtScope(var4);
                }

                return var5;
            }
        }
    }

    private static FoodWeaponSourceLedger.NativeHurtScope beginMeatActualHurt(Entity var0, DamageSource var1, float var2) {
        FoodWeaponSourceLedger.NativeIssueWindow var3 = new FoodWeaponSourceLedger.NativeIssueWindow(null);
        FoodWeaponSourceLedger.NativeIngressToken var4 = PRIVATE_INGRESS.get();
        if (meatHurtReady(var4)
            && var0 != null
            && var1 != null
            && var1.getClass() == DamageSource.class
            && Float.isFinite(var2)
            && !(var2 <= 0.0F)
            && meatHurtCaller(var4)
            && !var0.isRemoved()
            && var0.level() == var4.player.level()
            && var4.player.serverLevel().getEntity(var0.getUUID()) == var0
            && var1.getDirectEntity() == var4.player
            && var1.getEntity() == var4.player
            && FoodWeaponRanged.allowsWeaponEffects(var1)) {
            CompoundTag var5 = new CompoundTag();
            var5.putString("DamagePurpose", "NATIVE_MEAT_ACTUAL_HURT");
            var5.putString("NativeEntry", "MEAT");
            var5.putString("NativeCostSchedule", "ORIGINAL_ZERO_WEAR");
            var5.putString("NativeHurtPhase", "ACTUAL_HURT_ADMISSIBLE");
            var5.putString("NativeOuterPhase", "PENDING_NOT_NORMAL_COMPLETED");
            CompoundTag var6 = signActualHurt(
                var4.player, var4.actual, var4.hand, var4.action, var4.state, var4.ownerEpoch, var4.tick, var4.dimension, var4.rules, var5, var0, var1, var2
            );
            if (var6.isEmpty()) {
                return null;
            } else {
                FoodWeaponSourceLedger.NativeHurtScope var7 = new FoodWeaponSourceLedger.NativeHurtScope(var4, var0, var1, var6);
                if (!var3.unchanged()) {
                    return null;
                } else {
                    boolean var8 = false;

                    Object var9;
                    try {
                        PRIVATE_NATIVE_HURT.set(var7);
                        if (!validReadOnly(var6, var7.player)) {
                            closeNativeHurtScope(var7);
                            return null;
                        }

                        var7.interaction = beginNativeImpactInteraction(var7, var2);
                        if (var7.interaction != null) {
                            var8 = true;
                            return var7;
                        }

                        closeNativeHurtScope(var7);
                        var9 = null;
                    } finally {
                        if (!var8) {
                            revokeUnreturnedHurt(var7);
                        }
                    }

                    return (FoodWeaponSourceLedger.NativeHurtScope)var9;
                }
            }
        } else {
            return null;
        }
    }

    private static boolean meatHurtReady(FoodWeaponSourceLedger.NativeIngressToken var0) {
        return var0 != null
            && var0.kind == FoodWeaponSourceLedger.NativeIngressKind.MEAT
            && !var0.returned
            && ingressLive(var0)
            && PRIVATE_INGRESS.get() == var0
            && PRIVATE_INGRESS_WEAR.get() == null
            && var0.wears == 0
            && var0.tick == clock(var0.player)
            && var0.actual.getCount() == 1
            && !FoodWeaponLifecycle.isBroken(var0.actual)
            && var0.player.getItemInHand(var0.hand) == var0.actual
            && var0.player.isUsingItem()
            && var0.player.getUseItem() == var0.actual
            && var0.player.getUsedItemHand() == var0.hand
            && var0.rules.equals(foundationRules(var0.player, var0.actual))
            && expectationMatches(var0.expectation, var0.actual);
    }

    private static boolean meatHurtCaller(FoodWeaponSourceLedger.NativeIngressToken var0) {
        return NATIVE_CALLER.walk(
            var1 -> {
                List var2 = var1.filter(var0xx -> var0xx.getDeclaringClass() != FoodWeaponSourceLedger.class).limit(2L).toList();
                return var2.size() == 2
                    && ((StackFrame)var2.get(0)).getClassName().equals(var0.kind.itemClass)
                    && ((StackFrame)var2.get(0)).getMethodName().equals("nativeActualShredderHit")
                    && ((StackFrame)var2.get(0))
                        .getDescriptor()
                        .equals(
                            "(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/damagesource/DamageSource;FLnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;I)Z"
                        )
                    && ((StackFrame)var2.get(1)).getDeclaringClass() == ((StackFrame)var2.get(0)).getDeclaringClass()
                    && ((StackFrame)var2.get(1)).getMethodName().equals(var0.entry)
                    && ((StackFrame)var2.get(1)).getDescriptor().equals(var0.descriptor);
            }
        );
    }

    static boolean actualHurtSnapshot(CompoundTag var0) {
        FoodWeaponSourceLedger.NativeHurtScope var1 = PRIVATE_NATIVE_HURT.get();
        return var1 != null && var0 != null && var0.equals(var1.snapshot);
    }

    public static FoodWeaponSourceLedger.NativeIronCallbackToken nativeIronCallbackBegin(
        FoodWeaponSourceLedger.NativeIronPaymentToken var0, Object var1, LivingEntity var2, Object var3, Object var4, int var5
    ) {
        return nativeIronCallbackBegin(var0, var1, var2, var3, var4, var5, null);
    }

    public static FoodWeaponSourceLedger.NativeIronCallbackToken nativeIronCallbackBegin(
        FoodWeaponSourceLedger.NativeIronPaymentToken var0,
        Object var1,
        LivingEntity var2,
        Object var3,
        Object var4,
        int var5,
        FoodWeaponSourceLedger.NativeIssuerCustody var6
    ) {
        FoodWeaponSourceLedger.NativeIssueWindow var7 = new FoodWeaponSourceLedger.NativeIssueWindow(var0 == null ? null : var0.cast.use.player);
        String var8 = "(Lnet/minecraft/world/level/Level;ILnet/minecraft/server/level/ServerPlayer;Lio/redspace/ironsspellbooks/api/spells/CastSource;Z)V";
        if (!ironBoundary(
            "io.redspace.ironsspellbooks.api.spells.AbstractSpell",
            "ftboss$v9$ironCast",
            "(Lio/redspace/ironsspellbooks/api/spells/AbstractSpell;Lnet/minecraft/world/level/Level;ILnet/minecraft/world/entity/LivingEntity;Lio/redspace/ironsspellbooks/api/spells/CastSource;Lio/redspace/ironsspellbooks/api/magic/MagicData;)V",
            "castSpell",
            var8
        )) {
            return null;
        } else if (!var7.unchanged()) {
            return null;
        } else {
            FoodWeaponSourceLedger.NativeIronCallbackToken var9 = PRIVATE_IRON_CALLBACK.get();
            if (var9 != null) {
                nativeIronCallbackClose(var9);
                return null;
            } else if (var0 != null
                && !var0.invalid
                && var0.mana
                && !var0.callback
                && !var0.returned
                && !var0.cast.invalid
                && var0.cast.accepted
                && ironLive(var0.cast.use)
                && var0.tick == clock(var0.cast.use.player)
                && var0.cast.spell == var1
                && var0.cast.use.player == var2
                && var0.magicData == var3
                && var0.cast.castSource == var4
                && var0.level == var5
                && Float.isFinite(var0.manaAfter)
                && Float.compare(var0.manaAfter, Math.max(0.0F, var0.manaBefore - (float)var0.manaCost)) == 0) {
                FoodWeaponSourceLedger.NativeIronCallbackToken var10 = new FoodWeaponSourceLedger.NativeIronCallbackToken(var0);
                if (!var7.unchanged()) {
                    return null;
                } else if (var6 != null && !var6.cut.reserve(var10)) {
                    return null;
                } else {
                    if (var6 != null) {
                        var6.callback = var10;
                    }

                    boolean var11 = false;

                    FoodWeaponSourceLedger.NativeIronCallbackToken var12;
                    try {
                        PRIVATE_IRON_CALLBACK.set(var10);
                        var11 = true;
                        var12 = var10;
                    } finally {
                        if (!var11) {
                            if (var6 != null) {
                                closeNativeIssuerCustody(var6);
                            } else {
                                var10.closed = true;
                                var10.expectation.closed = true;
                                var10.expectation.cursor.close();
                                if (PRIVATE_IRON_CALLBACK.get() == var10) {
                                    PRIVATE_IRON_CALLBACK.remove();
                                }
                            }
                        }
                    }

                    return var12;
                }
            } else {
                return null;
            }
        }
    }

    public static void nativeIronCallbackReturned(FoodWeaponSourceLedger.NativeIronCallbackToken var0) {
        if (var0 != null) {
            String var1 = "(Lnet/minecraft/world/level/Level;ILnet/minecraft/server/level/ServerPlayer;Lio/redspace/ironsspellbooks/api/spells/CastSource;Z)V";
            if (ironBoundary(
                "io.redspace.ironsspellbooks.api.spells.AbstractSpell",
                "ftboss$v9$ironCast",
                "(Lio/redspace/ironsspellbooks/api/spells/AbstractSpell;Lnet/minecraft/world/level/Level;ILnet/minecraft/world/entity/LivingEntity;Lio/redspace/ironsspellbooks/api/spells/CastSource;Lio/redspace/ironsspellbooks/api/magic/MagicData;)V",
                "castSpell",
                var1
            )) {
                var0.normal = PRIVATE_IRON_CALLBACK.get() == var0
                    && !var0.closed
                    && var0.thread == Thread.currentThread()
                    && var0.tick == clock(var0.payment.cast.use.player)
                    && var0.payment.callback
                    && !var0.payment.invalid
                    && !var0.payment.returned
                    && !var0.payment.cast.invalid
                    && ironLive(var0.payment.cast.use);
                nativeIronCallbackClose(var0);
            }
        }
    }

    public static synchronized void nativeIronCallbackClose(FoodWeaponSourceLedger.NativeIronCallbackToken var0) {
        if (var0 != null) {
            var0.expectation.closed = true;
            var0.expectation.cursor.close();
            var0.closed = true;
            if (PRIVATE_IRON_CALLBACK.get() == var0) {
                PRIVATE_IRON_CALLBACK.remove();
            }

            FoodWeaponSourceLedger.NativeHurtScope var1 = PRIVATE_NATIVE_HURT.get();
            if (var1 != null && var1.ironCallback == var0) {
                closeNativeHurtScope(var1);
            }

            if (!var0.normal) {
                var0.payment.invalid = true;
                cancelNativeIron(var0.payment.cast);
            }
        }
    }

    private static boolean ironCallbackReady(FoodWeaponSourceLedger.NativeIronCallbackToken var0) {
        return var0 != null
            && !var0.closed
            && !var0.normal
            && PRIVATE_IRON_CALLBACK.get() == var0
            && var0.thread == Thread.currentThread()
            && var0.tick == clock(var0.payment.cast.use.player)
            && !var0.payment.invalid
            && var0.payment.mana
            && !var0.payment.callback
            && !var0.payment.returned
            && !var0.payment.cast.invalid
            && var0.payment.cast.accepted
            && ironLive(var0.payment.cast.use)
            && expectationMatches(var0.expectation, var0.payment.cast.use.actual)
            && Float.isFinite(var0.payment.manaAfter)
            && Float.compare(var0.payment.manaAfter, Math.max(0.0F, var0.payment.manaBefore - (float)var0.payment.manaCost)) == 0;
    }

    public static boolean nativeIronActualHurt(Entity var0, DamageSource var1, float var2) {
        FoodWeaponSourceLedger.NativeHurtScope var3 = PRIVATE_NATIVE_HURT.get();
        if (var3 != null) {
            closeNativeHurtScope(var3);
            return var0.hurt(var1, var2);
        } else {
            FoodWeaponSourceLedger.NativeHurtScope var4 = beginIronActualHurt(var0, var1, var2);
            if (var4 == null) {
                return var0.hurt(var1, var2);
            } else {
                boolean var5;
                try {
                    var5 = var0.hurt(var1, var2);
                } finally {
                    closeNativeHurtScope(var4);
                }

                return var5;
            }
        }
    }

    private static FoodWeaponSourceLedger.NativeHurtScope beginIronActualHurt(Entity var0, DamageSource var1, float var2) {
        FoodWeaponSourceLedger.NativeIssueWindow var3 = new FoodWeaponSourceLedger.NativeIssueWindow(null);
        FoodWeaponSourceLedger.NativeIronCallbackToken var4 = PRIVATE_IRON_CALLBACK.get();
        if (ironCallbackReady(var4)
            && var0 != null
            && var1 != null
            && Float.isFinite(var2)
            && !(var2 <= 0.0F)
            && ironActualHurtCaller(var4)
            && ironActualSource(var4, var1)) {
            FoodWeaponSourceLedger.NativeIronUseToken var5 = var4.payment.cast.use;
            if (!var0.isRemoved()
                && var0.level() == var5.player.level()
                && var5.player.serverLevel().getEntity(var0.getUUID()) == var0
                && var1.getDirectEntity() == var5.player
                && var1.getEntity() == var5.player
                && FoodWeaponRanged.allowsWeaponEffects(var1)) {
                CompoundTag var6 = new CompoundTag();
                var6.putString("DamagePurpose", "NATIVE_IRON_ACTUAL_HURT");
                var6.putString("NativeEntry", "IRON_PAID_ONCAST");
                var6.putString("NativeHurtPhase", "ACTUAL_HURT_ADMISSIBLE");
                var6.putString("NativeOuterPhase", "CALLBACK_ACTIVE_NOT_NORMAL_COMPLETED");
                var6.putString("NativeSpellClass", var4.payment.cast.spell.getClass().getName());
                var6.putString("NativeCastSource", var4.payment.cast.source);
                var6.putString("NativeCastSlot", var4.payment.cast.slot);
                var6.putInt("NativeSelectedLevel", var4.payment.cast.selectedLevel);
                var6.putInt("NativeEffectiveLevel", var4.payment.level);
                var6.putInt("NativeManaCost", var4.payment.manaCost);
                var6.putFloat("NativeManaBefore", var4.payment.manaBefore);
                var6.putFloat("NativeManaAfter", var4.payment.manaAfter);
                var6.putFloat("NativeManaDelta", var4.payment.manaBefore - var4.payment.manaAfter);
                var6.putLong("NativeTriggerTick", var5.tick);
                CompoundTag var7 = signActualHurt(
                    var5.player,
                    var5.actual,
                    var5.hand,
                    var4.payment.cast.action,
                    var5.state,
                    var5.ownerEpoch,
                    var4.tick,
                    var5.dimension,
                    var5.rules,
                    var6,
                    var0,
                    var1,
                    var2
                );
                if (var7.isEmpty()) {
                    return null;
                } else {
                    FoodWeaponSourceLedger.NativeHurtScope var8 = new FoodWeaponSourceLedger.NativeHurtScope(var4, var0, var1, var7);
                    if (!var3.unchanged()) {
                        return null;
                    } else {
                        boolean var9 = false;

                        Object var10;
                        try {
                            PRIVATE_NATIVE_HURT.set(var8);
                            if (!validReadOnly(var7, var8.player)) {
                                closeNativeHurtScope(var8);
                                return null;
                            }

                            var8.interaction = beginNativeImpactInteraction(var8, var2);
                            if (var8.interaction != null) {
                                var9 = true;
                                return var8;
                            }

                            closeNativeHurtScope(var8);
                            var10 = null;
                        } finally {
                            if (!var9) {
                                revokeUnreturnedHurt(var8);
                            }
                        }

                        return (FoodWeaponSourceLedger.NativeHurtScope)var10;
                    }
                }
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    private static boolean ironActualSource(FoodWeaponSourceLedger.NativeIronCallbackToken var0, DamageSource var1) {
        if (var1.getClass().getName().equals("io.redspace.ironsspellbooks.damage.SpellDamageSource")
            && var1.getClass().getClassLoader() == var0.payment.cast.spell.getClass().getClassLoader()) {
            try {
                return var1.getClass().getMethod("spell").invoke(var1) == var0.payment.cast.spell;
            } catch (RuntimeException | ReflectiveOperationException var3) {
                return false;
            }
        } else {
            return false;
        }
    }

    private static boolean ironActualHurtCaller(FoodWeaponSourceLedger.NativeIronCallbackToken var0) {
        return NATIVE_CALLER.walk(
            var1 -> {
                List var2 = var1.filter(var0xx -> var0xx.getDeclaringClass() != FoodWeaponSourceLedger.class).limit(32L).toList();
                if (var2.size() >= 3
                    && ((StackFrame)var2.get(0)).getClassName().equals("io.redspace.ironsspellbooks.damage.DamageSources")
                    && ((StackFrame)var2.get(0)).getDeclaringClass() == ((StackFrame)var2.get(1)).getDeclaringClass()
                    && ((StackFrame)var2.get(0)).getDeclaringClass().getClassLoader() == var0.payment.cast.spell.getClass().getClassLoader()
                    && ((StackFrame)var2.get(1)).getMethodName().equals("applyDamage")
                    && ((StackFrame)var2.get(1))
                        .getDescriptor()
                        .equals("(Lnet/minecraft/world/entity/Entity;FLnet/minecraft/world/damagesource/DamageSource;)Z")) {
                    StackFrame var3 = (StackFrame)var2.get(0);
                    boolean var4 = NativeWeaponIngressSites.matches(var3, "io.redspace.ironsspellbooks.damage.DamageSources", "ftboss$v10$ironLivingHurt", "(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/damagesource/DamageSource;F)Z")
                        && var3.getDescriptor().equals("(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/damagesource/DamageSource;F)Z");
                    boolean var5 = NativeWeaponIngressSites.matches(var3, "io.redspace.ironsspellbooks.damage.DamageSources", "ftboss$v10$ironEntityHurt", "(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/damagesource/DamageSource;F)Z")
                        && var3.getDescriptor().equals("(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/damagesource/DamageSource;F)Z");
                    StackFrame var6 = (StackFrame)var2.get(2);
                    return (var4 || var5)
                        && var6.getMethodName().equals("onCast")
                        && var6.getDeclaringClass().isInstance(var0.payment.cast.spell)
                        && var6.getDescriptor()
                            .equals(
                                "(Lnet/minecraft/world/level/Level;ILnet/minecraft/world/entity/LivingEntity;Lio/redspace/ironsspellbooks/api/spells/CastSource;Lio/redspace/ironsspellbooks/api/magic/MagicData;)V"
                            );
                } else {
                    return false;
                }
            }
        );
    }

    public static synchronized void cancelNativeIngress(FoodWeaponSourceLedger.NativeIngressToken var0) {
        if (var0 != null) {
            var0.expectation.closed = true;
            var0.expectation.cursor.close();
            var0.invalid = true;
            FoodWeaponSourceLedger.NativeHurtScope var1 = PRIVATE_NATIVE_HURT.get();
            if (var1 != null && (var1.earlyIngress == var0 || var1.receipt != null && var1.receipt.ingress == var0)) {
                closeNativeHurtScope(var1);
            }

            if (PRIVATE_INGRESS.get() == var0) {
                PRIVATE_INGRESS.remove();
            }

            FoodWeaponSourceLedger.NativeWearToken var2 = PRIVATE_INGRESS_WEAR.get();
            if (var2 != null && var2.ingress == var0) {
                PRIVATE_INGRESS_WEAR.remove();
            }
        }
    }

    private static boolean authorizedWear(FoodWeaponSourceLedger.NativeReceipt var0, ItemStack var1, int var2) {
        return authorizedWear(var0.operation.player, var0.before, var0.rules, var1, var2, var0.writes, var0.requestedDamage);
    }

    private static boolean authorizedWear(ServerPlayer var0, CompoundTag var1, String var2, ItemStack var3, int var4, int var5, int var6) {
        if ((NativeSourceLedgerItemData.rawCustomDataForObservation(var3) == null || canonicalInputValid(NativeSourceLedgerItemData.rawCustomDataForObservation(var3))) && canonicalInputValid(NativeFoundationItemData.image(var3))) {
            ItemStack var7 = NativeFoundationItemData.decode(var1);
            int var8 = var7.getDamageValue();
            int var9 = var3.getDamageValue();
            if (var3.getCount() == 1
                && var7.getItem() == var3.getItem()
                && var3.getMaxDamage() == var7.getMaxDamage()
                && var8 < var3.getMaxDamage() - 1
                && var9 >= var8
                && var9 < var3.getMaxDamage()
                && !FoodWeaponLifecycle.isBroken(var3)
                && !var2.isEmpty()
                && var2.equals(foundationRules(var0, var3))) {
                double var10 = NativeFoundationItemData.element(var7, "FTBossFourWeaponFoundation").getDouble("WearCarry");
                Wear var12 = FoodWeaponBuildRules.wear(var4, FourWeaponFoundationAdapter.combat(var7).wear(), var10);
                if (Double.doubleToLongBits(var12.carry()) == Double.doubleToLongBits(NativeFoundationItemData.element(var3, "FTBossFourWeaponFoundation").getDouble("WearCarry"))
                    && var9 - var8 <= var12.damage()
                    && var5 <= 1
                    && (var5 != 0 || var9 == var8)
                    && (var5 != 1 || var6 == var9)) {
                    CompoundTag var13 = var1.copy();
                    CompoundTag var14 = NativeFoundationItemData.image(var3).copy();
                    CompoundTag var15 = NativeSourceLedgerItemData.imageData(var13);
                    CompoundTag var16 = NativeSourceLedgerItemData.imageData(var14);
                    CompoundTag var17 = var15.getCompound("FTBossWeaponLifecycle");
                    CompoundTag var18 = var16.getCompound("FTBossWeaponLifecycle");
                    int var19 = FoodWeaponLifecycleRules.pendingLosses(var17.getInt("WearStep"), var9, var3.getMaxDamage());
                    int var20 = var19 > 0 ? FoodWeaponLifecycleRules.wearStep(var9, var3.getMaxDamage()) : var17.getInt("WearStep");
                    if (var18.getInt("WearStep") != var20) {
                        return false;
                    } else {
                        ListTag var21 = var15.getCompound("FTBossFoodEvolution").getList("Sockets", 10);
                        ListTag var22 = var16.getCompound("FTBossFoodEvolution").getList("Sockets", 10);
                        int var23 = Math.min(var19, var21.size());
                        ListTag var24 = var21.copy();

                        for (int var25 = 0; var25 < var23; var25++) {
                            var24.remove(var24.size() - 1);
                        }

                        if (var24.equals(var22)
                            && (var18.getInt("PendingMaterials") == var17.getInt("PendingMaterials") || var18.getInt("PendingMaterials") == 0)
                            && (var18.getInt("PendingSockets") == var17.getInt("PendingSockets") + var23 || var18.getInt("PendingSockets") == 0)) {
                            NativeSourceLedgerItemData.removeDamageFromComparison(var13);
                            NativeSourceLedgerItemData.removeDamageFromComparison(var14);
                            for (CompoundTag var26 : List.of(var15, var16)) {
                                var26.getCompound("FTBossFourWeaponFoundation").remove("WearCarry");
                                CompoundTag var27 = var26.getCompound("FTBossWeaponLifecycle");

                                for (String var29 : List.of("WearStep", "PendingMaterials", "PendingSockets")) {
                                    var27.remove(var29);
                                }

                                var26.getCompound("FTBossFoodEvolution").remove("Sockets");
                            }

                            return var13.equals(var14);
                        } else {
                            return false;
                        }
                    }
                } else {
                    return false;
                }
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    public static String boundProfile(ItemStack var0) {
        String var1 = NativeFoodWeaponEvolutionRead.repairProfileId(var0);
        return Aspect.fromId(var1) == null ? "" : var1;
    }

    public static String materialFingerprint(ItemStack var0) {
        return FoodWeaponMaterialFingerprint.create(var0);
    }

    private static String foundationRules(ServerPlayer var0, ItemStack var1) {
        if (!FourWeaponFoundationAdapter.applies(var1)) {
            return "";
        } else if (FoodWeaponMaterialFingerprint.foundationSnapshot(var1).isEmpty()) {
            return "";
        } else {
            try {
                String var2 = new FoundationApprovedRules().fingerprint(var0, var1);
                return var2 != null && !var2.isEmpty() && var2.length() <= 1024 ? var2 : "";
            } catch (LinkageError | IllegalArgumentException var3) {
                return "";
            }
        }
    }

    private static boolean foundationContinuous(CompoundTag var0, ServerPlayer var1, ItemStack var2) {
        if (!FourWeaponFoundationAdapter.applies(var2)) {
            return !var0.contains("FoundationSnapshot") && !var0.contains("FoundationApprovedRules");
        } else {
            CompoundTag var3 = FoodWeaponMaterialFingerprint.foundationSnapshot(var2);
            CompoundTag var4 = var0.getCompound("FoundationSnapshot");
            String var5 = foundationRules(var1, var2);
            return !var3.isEmpty()
                && !var5.isEmpty()
                && var5.equals(var0.getString("FoundationApprovedRules"))
                && var4.getBoolean("Valid")
                && !var4.getBoolean("BlocksUse")
                && var3.getString("Composition").equals(var4.getString("Composition"))
                && var3.getString("Rules").equals(var4.getString("Rules"))
                && var3.getString("ItemId").equals(var4.getString("ItemId"))
                && var3.getString("Profile").equals(var4.getString("Profile"))
                && var3.getString("Family").equals(var4.getString("Family"))
                && var3.getInt("Level") == var4.getInt("Level");
        }
    }

    private static boolean nativeFoundation(ItemStack var0) {
        return FourWeaponFoundationAdapter.applies(var0)
            && FourWeaponFoundationAdapter.read(var0).valid()
            && FourWeaponFoundationAdapter.read(var0).level() == 0
            && boundProfile(var0).isEmpty();
    }

    public static String socketFingerprint(ItemStack var0, String var1) {
        PrivateRead423 var2 = new PrivateRead423();
        CompoundTag var3 = NativeFoundationItemData.element(var0, "FTBossFoodEvolution");
        ListTag var4 = new ListTag();
        if (var3 != null) {
            for (Tag var6 : var3.getList("Sockets", 10)) {
                CompoundTag var7 = (CompoundTag)var6;
                PrivateRead423 var8 = new PrivateRead423();
                var8.putString("Type", var7.getString("Type"));
                var8.putString("Item", var7.getString("Item"));
                if (var7.contains("Tier", 3)) {
                    var8.putInt("Tier", var7.getInt("Tier"));
                }

                var4.add(var8.finish());
            }
        }

        var2.put("Sockets", var4);
        var2.putInt("Count", var4.size());
        var2.putString("Profile", boundProfile(var0));
        var2.putString("Family", var1);
        return fingerprint(var2.finish());
    }

    private static String fingerprint(Tag var0) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(canonical(var0)));
        } catch (IllegalArgumentException var2) {
            return "";
        } catch (NoSuchAlgorithmException var3) {
            throw new IllegalStateException(var3);
        }
    }

    public static boolean canonicalInputValid(Tag var0) {
        try {
            canonical(var0);
            return true;
        } catch (IllegalArgumentException var2) {
            return false;
        }
    }

    private static byte[] canonical(Tag var0) {
        ByteArrayOutputStream var1 = new ByteArrayOutputStream();

        try {
            byte[] var3;
            try (DataOutputStream var2 = new DataOutputStream(var1)) {
                var2.writeInt(1179932499);
                var2.writeInt(3);
                appendCanonical(var0, var2, 0, new int[]{0});
                var3 = var1.toByteArray();
            }

            return var3;
        } catch (IOException var7) {
            throw new IllegalStateException(var7);
        }
    }

    private static void canonicalString(String var0, DataOutputStream var1) throws IOException {
        if (var0.length() > 1048576) {
            throw new IllegalArgumentException("Owner string budget exceeded");
        } else {
            ByteBuffer var2;
            try {
                var2 = StandardCharsets.UTF_8
                    .newEncoder()
                    .onMalformedInput(CodingErrorAction.REPORT)
                    .onUnmappableCharacter(CodingErrorAction.REPORT)
                    .encode(CharBuffer.wrap(var0));
            } catch (CharacterCodingException var4) {
                throw new IllegalArgumentException("Owner malformed UTF-16", var4);
            }

            if (var2.remaining() > 1048576 - var1.size() - 4) {
                throw new IllegalArgumentException("Owner byte budget exceeded");
            } else {
                var1.writeInt(var2.remaining());

                while (var2.hasRemaining()) {
                    var1.writeByte(var2.get());
                }
            }
        }
    }

    private static void appendCanonical(Tag var0, DataOutputStream var1, int var2, int[] var3) throws IOException {
        if (var0 != null && var2 <= 64 && ++var3[0] <= 65536) {
            var1.writeByte(var0.getId());
            if (var0 instanceof CompoundTag var4) {
                if (var4.size() > 65536 - var3[0]) {
                    throw new IllegalArgumentException("Owner compound budget exceeded");
                }

                var1.writeInt(var4.size());

                Map<String, Tag> canonicalFields = ((NativeExpectedFieldViews.Compound)var4).ftboss$rawCompoundFields();
                for (String var12 : new TreeSet<>(canonicalFields.keySet())) {
                    canonicalString(var12, var1);
                    appendCanonical(canonicalFields.get(var12), var1, var2 + 1, var3);
                }
            } else if (var0 instanceof ListTag var5) {
                if (var5.size() > 65536 - var3[0]) {
                    throw new IllegalArgumentException("Owner list budget exceeded");
                }

                var1.writeByte(var5.getElementType());
                var1.writeInt(var5.size());

                for (Tag var21 : var5) {
                    appendCanonical(var21, var1, var2 + 1, var3);
                }
            } else if (var0 instanceof StringTag var6) {
                canonicalString(var6.getAsString(), var1);
            } else if (var0 instanceof NumericTag var7) {
                switch (var0.getId()) {
                    case 1:
                        var1.writeByte(var7.getAsByte());
                        break;
                    case 2:
                        var1.writeShort(var7.getAsShort());
                        break;
                    case 3:
                        var1.writeInt(var7.getAsInt());
                        break;
                    case 4:
                        var1.writeLong(var7.getAsLong());
                        break;
                    case 5:
                        var1.writeInt(Float.floatToRawIntBits(var7.getAsFloat()));
                        break;
                    case 6:
                        var1.writeLong(Double.doubleToRawLongBits(var7.getAsDouble()));
                        break;
                    default:
                        throw new IllegalArgumentException("Owner unknown numeric tag");
                }
            } else if (var0 instanceof ByteArrayTag var8) {
                byte[] var18 = ((NativeExpectedFieldViews.Bytes)var8).ftboss$rawBytes();
                if (var18.length > 1048576 - var1.size() - 4) {
                    throw new IllegalArgumentException("Owner array budget exceeded");
                }

                var1.writeInt(var18.length);
                var1.write(var18);
            } else if (var0 instanceof IntArrayTag var9) {
                int[] var19 = ((NativeExpectedFieldViews.Ints)var9).ftboss$rawInts();
                if (var19.length > (1048576 - var1.size() - 4) / 4) {
                    throw new IllegalArgumentException("Owner array budget exceeded");
                }

                var1.writeInt(var19.length);

                for (int var15 : var19) {
                    var1.writeInt(var15);
                }
            } else if (var0 instanceof LongArrayTag var10) {
                long[] var20 = ((NativeExpectedFieldViews.Longs)var10).ftboss$rawLongs();
                if (var20.length > (1048576 - var1.size() - 4) / 8) {
                    throw new IllegalArgumentException("Owner array budget exceeded");
                }

                var1.writeInt(var20.length);

                for (long var26 : var20) {
                    var1.writeLong(var26);
                }
            } else if (var0.getId() != 0) {
                throw new IllegalArgumentException("Owner unknown tag type");
            }

            if (var1.size() > 1048576) {
                throw new IllegalArgumentException("Owner byte budget exceeded");
            }
        } else {
            throw new IllegalArgumentException("Owner NBT complexity exceeded");
        }
    }

    public static CompoundTag decorate(ServerPlayer var0, ItemStack var1, CompoundTag var2, InteractionHand var3) {
        if (var2 != null && var2.contains("DamagePurpose")) {
            boolean var4 = NATIVE_CALLER.walk(
                var0x -> var0x.filter(var0xx -> var0xx.getDeclaringClass() != FoodWeaponSourceLedger.class)
                        .findFirst()
                        .map(var0xx -> var0xx.getDeclaringClass() == FoodWeaponDamageScaling.class && var0xx.getMethodName().equals("captureGeneric"))
                        .orElse(false)
            );
            if (!var4 || !"GENERIC_J".equals(var2.getString("DamagePurpose")) || !Double.isFinite(FoodWeaponDamageScaling.residual(var2))) {
                return new CompoundTag();
            }
        } else if (var2 != null && var2.contains("DamageAccountingVersion")) {
            return new CompoundTag();
        }

        return decorateNative(var0, var1, var2, var3, null);
    }

    private static CompoundTag decorateNative(
        ServerPlayer var0, ItemStack var1, CompoundTag var2, InteractionHand var3, FoodWeaponSourceLedger.NativeReceipt var4
    ) {
        if (var0 != null
            && var1 != null
            && OwnerFullRepairGuard.permits(var1)
            && var2 != null
            && !var2.isEmpty()
            && var3 != null
            && var1.getCount() == 1
            && canonicalInputValid(var2)
            && (NativeSourceLedgerItemData.rawCustomDataForObservation(var1) == null || canonicalInputValid(NativeSourceLedgerItemData.rawCustomDataForObservation(var1)))
            && FoodWeaponLifecycle.weaponId(var1) != null) {
            CompoundTag var5 = NativeFoundationItemData.image(var1);
            if (!canonicalInputValid(var5)) {
                return new CompoundTag();
            } else {
                String var6 = materialFingerprint(var1);
                if (var6.isEmpty()) {
                    return new CompoundTag();
                } else {
                    var2 = var2.copy();
                    var2.remove("FoundationSnapshot");
                    var2.remove("FoundationApprovedRules");
                    if (FourWeaponFoundationAdapter.applies(var1)) {
                        CompoundTag var7 = FoodWeaponMaterialFingerprint.foundationSnapshot(var1);
                        String var8 = foundationRules(var0, var1);
                        if (var7.isEmpty() || var8.isEmpty() || !FoodWeaponFamilyBuild.kind(var1).name().equals(var2.getString("Family"))) {
                            return new CompoundTag();
                        }

                        var2.put("FoundationSnapshot", var7.copy());
                        var2.putString("FoundationApprovedRules", var8);
                        var2.put("Weapon", var5.copy());
                    }

                    long var21 = clock(var0);
                    if (var21 >= 0L && var21 <= 9223372036854774607L) {
                        var2.remove("NativeProfile");
                        var2.remove("Hand");
                        var2.remove("TridentLaunchAuthorized");
                        var2.remove("PreWearEligible");
                        var2.remove("NativeEntry");
                        var2.remove("NativeReceiptId");
                        var2.remove("NativeEntityId");
                        var2.putInt("SnapshotVersion", 3);
                        var2.putUUID("Owner", var0.getUUID());
                        var2.putUUID("AttackId", UUID.randomUUID());
                        var2.putUUID("WeaponId", FoodWeaponLifecycle.weaponId(var1));
                        var2.putUUID("ServerEpoch", new UUID(0L, 0L));
                        var2.putUUID("OwnerEpoch", new UUID(0L, 0L));
                        var2.putString("OriginalHand", var3.name());
                        var2.putString("Dimension", var0.level().dimension().location().toString());
                        var2.putLong("CapturedAt", var21);
                        var2.putLong("ExpiresAt", var21 + 1200L);
                        var2.putString("BoundProfile", boundProfile(var1));
                        var2.putString("RepairProfile", FoodWeaponLifecycle.repairProfile(var1));
                        var2.putString("SourceKind", var2.getString("Family"));
                        if (var4 != null) {
                            var2.putString("NativeEntry", var4.operation.kind.name());
                            var2.putUUID("NativeReceiptId", var4.token);
                            var2.putUUID("NativeEntityId", var4.entity.getUUID());
                            if (var4.operation.kind == FoodWeaponSourceLedger.NativeKind.TRIDENT) {
                                var2.putString("SourceKind", "TRIDENT");
                                var2.putBoolean("TridentLaunchAuthorized", true);
                            }
                        }

                        var2.put("Weapon", var5);
                        var2.putString("MaterialFingerprint", var6);
                        var2.putString("SocketFingerprint", socketFingerprint(var1, var2.getString("Family")));
                        var2.putString("ServerSignature", "0".repeat(64));
                        if (!canonicalInputValid(var2)) {
                            return new CompoundTag();
                        } else {
                            FoodWeaponSourceLedger.ServerState var9 = state(server(var0));
                            UUID var10;
                            synchronized (FoodWeaponSourceLedger.class) {
                                if (SERVERS.get(server(var0)) != var9 || var9.closed || var21 < var9.latestTick) {
                                    return new CompoundTag();
                                }

                                var10 = ownerEpoch(var9, var0.getUUID());
                                if (var10 == null) {
                                    var10 = UUID.randomUUID();
                                }

                                var2.putUUID("ServerEpoch", var9.epoch);
                                var2.putUUID("OwnerEpoch", var10);
                            }

                            if (!OwnerRepairGenerationAuthority.stamp(var1, var2, var9.revocations)) {
                                return new CompoundTag();
                            } else {
                                synchronized (FoodWeaponSourceLedger.class) {
                                    UUID var12 = ownerEpoch(var9, var0.getUUID());
                                    if (SERVERS.get(server(var0)) != var9 || var9.closed || var21 < var9.latestTick || var12 != null && !var12.equals(var10)) {
                                        return new CompoundTag();
                                    }

                                    String var13 = signature(var2, var9);
                                    if (var13.isEmpty()) {
                                        return new CompoundTag();
                                    }

                                    var2.putString("ServerSignature", var13);
                                    FoodWeaponSourceLedger.OwnerState var14 = var9.ownerEpochs.get(var0.getUUID());
                                    if (var14 == null) {
                                        var9.ownerEpochs.put(var0.getUUID(), new FoodWeaponSourceLedger.OwnerState(var10));
                                    }
                                }

                                if (!OwnerFullRepairGuard.bind(var1, var2.getUUID("WeaponId"))) {
                                    return new CompoundTag();
                                } else {
                                    synchronized (FoodWeaponSourceLedger.class) {
                                        if (SERVERS.get(server(var0)) == var9
                                            && !var9.closed
                                            && var21 >= var9.latestTick
                                            && var10.equals(ownerEpoch(var9, var0.getUUID()))) {
                                            var9.latestTick = var21;
                                            return var2;
                                        } else {
                                            return new CompoundTag();
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        return new CompoundTag();
                    }
                }
            }
        } else {
            return new CompoundTag();
        }
    }

    public static boolean valid(CompoundTag var0, ServerPlayer var1) {
        if (!validReadOnly(var0, var1)) {
            return false;
        } else {
            synchronized (FoodWeaponSourceLedger.class) {
                FoodWeaponSourceLedger.ServerState var3 = SERVERS.get(server(var1));
                if (var3 != null
                    && !var3.closed
                    && var3.epoch.equals(var0.getUUID("ServerEpoch"))
                    && var0.getUUID("OwnerEpoch").equals(ownerEpoch(var3, var1.getUUID()))) {
                    var3.latestTick = clock(var1);
                    return true;
                } else {
                    return false;
                }
            }
        }
    }

    public static boolean validReadOnly(CompoundTag var0, ServerPlayer var1) {
        if (var1 != null
            && var0 != null
            && var0.getInt("SnapshotVersion") == 3
            && canonicalInputValid(var0)
            && var0.getString("ServerSignature").matches("[0-9a-f]{64}")
            && var0.hasUUID("Owner")
            && var0.getUUID("Owner").equals(var1.getUUID())
            && var0.hasUUID("AttackId")
            && var0.hasUUID("WeaponId")
            && var0.hasUUID("ServerEpoch")
            && var0.hasUUID("OwnerEpoch")
            && !var0.getString("MaterialFingerprint").isEmpty()
            && var0.contains("Weapon", 10)
            && var0.contains("CapturedAt", 4)
            && var0.contains("ExpiresAt", 4)
            && var0.getString("Dimension").equals(var1.level().dimension().location().toString())
            && (
                var0.getString("OriginalHand").equals(InteractionHand.MAIN_HAND.name())
                    || var0.getString("OriginalHand").equals(InteractionHand.OFF_HAND.name())
            )
            && !var0.contains("Hand")
            && !var0.contains("NativeProfile")) {
            FoodWeaponSourceLedger.ServerState var2;
            synchronized (FoodWeaponSourceLedger.class) {
                var2 = SERVERS.get(server(var1));
            }

            OwnerLineageRevocationStore.Lease var19 = OwnerFullRepairGuard.lease(var0);
            if (var2 == null || !OwnerRepairGenerationAuthority.snapshotAllowed(var0)) {
                return false;
            } else if (SERVERS.get(server(var1)) == var2 && !var2.closed) {
                long var4 = clock(var1);
                long var6 = var0.getLong("CapturedAt");
                long var8 = var0.getLong("ExpiresAt");
                if (var0.getUUID("ServerEpoch").equals(var2.epoch)
                    && var0.getUUID("OwnerEpoch").equals(ownerEpoch(var2, var1.getUUID()))
                    && var4 >= var2.latestTick
                    && var6 >= 0L
                    && var6 <= 9223372036854774607L
                    && var8 == var6 + 1200L
                    && var4 >= var6
                    && var4 < var8) {
                    String var10 = var0.getString("Family");
                    String var11 = var0.getString("SourceKind");
                    boolean var12 = var11.equals("TRIDENT") && var10.equals("MELEE") && var0.getBoolean("TridentLaunchAuthorized");
                    if (var12 || var11.equals(var10) && (var10.equals("BOW") || var10.equals("STAFF") || var10.equals("MELEE"))) {
                        String var13 = signature(var0, var2);
                        if (var13.isEmpty()
                            || !MessageDigest.isEqual(
                                var0.getString("ServerSignature").getBytes(StandardCharsets.US_ASCII), var13.getBytes(StandardCharsets.US_ASCII)
                            )) {
                            return false;
                        } else if (var0.contains("DamagePurpose") && !damageAuthorized(var0)) {
                            return false;
                        } else {
                            ItemStack var14 = NativeFoundationItemData.decode(var0.getCompound("Weapon"));
                            if (var14.isEmpty()
                                || var14.getCount() != 1
                                || !var0.getUUID("WeaponId").equals(FoodWeaponLifecycle.weaponId(var14))
                                || !boundProfile(var14).equals(var0.getString("BoundProfile"))
                                || !FoodWeaponLifecycle.repairProfile(var14).equals(var0.getString("RepairProfile"))
                                || !materialFingerprint(var14).equals(var0.getString("MaterialFingerprint"))
                                || !socketFingerprint(var14, var10).equals(var0.getString("SocketFingerprint"))
                                || !foundationContinuous(var0, var1, var14)) {
                                return false;
                            } else if (!var12 || isTrident(var14) && (!var0.getString("BoundProfile").isEmpty() || nativeFoundation(var14))) {
                                if (!OwnerRepairGenerationAuthority.snapshotAllowed(var0)) {
                                    return false;
                                } else {
                                    synchronized (FoodWeaponSourceLedger.class) {
                                        return SERVERS.get(server(var1)) == var2
                                            && !var2.closed
                                            && var4 == clock(var1)
                                            && var4 >= var2.latestTick
                                            && var0.getUUID("ServerEpoch").equals(var2.epoch)
                                            && var0.getUUID("OwnerEpoch").equals(ownerEpoch(var2, var1.getUUID()))
                                            && OwnerRepairGenerationAuthority.snapshotIdentity(var0, var19);
                                    }
                                }
                            } else {
                                return false;
                            }
                        }
                    } else {
                        return false;
                    }
                } else {
                    return false;
                }
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    private static String signature(CompoundTag var0, FoodWeaponSourceLedger.ServerState var1) {
        try {
            canonical(var0);
            CompoundTag var2 = var0.copy();
            var2.remove("ServerSignature");
            Mac var3 = Mac.getInstance("HmacSHA256");
            var3.init(new SecretKeySpec(var1.signingKey, "HmacSHA256"));
            return HexFormat.of().formatHex(var3.doFinal(canonical(var2)));
        } catch (IllegalArgumentException var4) {
            return "";
        } catch (GeneralSecurityException var5) {
            throw new IllegalStateException(var5);
        }
    }

    public static CompoundTag resolve(DamageSource var0, ServerPlayer var1) {
        CompoundTag var2 = resolveReadOnly(var0, var1);
        synchronized (FoodWeaponSourceLedger.class) {
            FoodWeaponSourceLedger.ServerState var4 = var1 == null ? null : SERVERS.get(server(var1));
            if (!var2.isEmpty() && var4 != null && !var4.closed && var2.getUUID("ServerEpoch").equals(var4.epoch)) {
                var4.latestTick = clock(var1);
            }

            return var2;
        }
    }

    public static CompoundTag resolveReadOnly(DamageSource var0, ServerPlayer var1) {
        FoodWeaponSourceLedger.PrimaryReadWindow var2 = beginPrimaryRead(var0, var1);
        boolean var3 = false;

        CompoundTag var18;
        try {
            CompoundTag var4 = resolveSourceUncached(var0, var1);
            if (var2 != null && !var4.isEmpty() && attemptSourceValid(var4, var1) && interactionRootLive(var2.root, var1) && projectedBindingLive(var2.binding)
                )
             {
                synchronized (FoodWeaponSourceLedger.class) {
                    if (readWindowIdentity(var2)) {
                        var3 = var2.root.readCut.accept(var2.transition, var4);
                    }
                }
            }

            var18 = var4;
        } finally {
            synchronized (FoodWeaponSourceLedger.class) {
                if (var2 != null && !var3 && var2.root.readCut.transition == var2.transition) {
                    var2.root.readCut.acceptedImage = null;
                }
            }
        }

        return var18;
    }

    private static CompoundTag resolveSourceUncached(DamageSource var0, ServerPlayer var1) {
        if (var0 != null && var1 != null && FoodWeaponRanged.allowsWeaponEffects(var0)) {
            FoodWeaponSourceLedger.NativeHurtScope var2 = PRIVATE_NATIVE_HURT.get();
            if (var2 != null && var2.source == var0 && var2.player == var1) {
                return nativeHurtLive(var2) && validReadOnly(var2.snapshot, var1) ? var2.snapshot.copy() : new CompoundTag();
            } else if (var0.getDirectEntity() == var1) {
                if (var0.getEntity() == var1 && var0.is(FoodWeaponDamageSources.PRIMARY)) {
                    CompoundTag var10 = FoodWeaponRanged.scopedSnapshotReadOnly(var0, var1);
                    return validReadOnly(var10, var1) && damageAuthorized(var10) && "GENERIC_J".equals(var10.getString("DamagePurpose"))
                        ? var10
                        : new CompoundTag();
                } else {
                    return new CompoundTag();
                }
            } else {
                if (var0.getDirectEntity() instanceof Projectile var3 && var3.getOwner() == var1) {
                    FoodWeaponSourceLedger.NativePaidWeapon var11 = var3 instanceof NativePaidWeaponCarrier var5 ? var5.ftboss$originalOwnerPaidWeapon() : null;
                    CompoundTag var12 = var11 != null && var11.material != null
                        ? nativeMaterialPaidSnapshot(var11, var3, var1)
                        : var3.getPersistentData().getCompound("FTBossWeaponMaterials");
                    if (validReadOnly(var12, var1)
                        && damageAuthorized(var12)
                        && "NATIVE_PROJECTILE".equals(var12.getString("DamagePurpose"))
                        && var12.hasUUID("NativeEntityId")
                        && var12.getUUID("NativeEntityId").equals(var3.getUUID())) {
                        if (!var12.getString("SourceKind").equals("TRIDENT")) {
                            if (!(var3 instanceof ThrownTrident) && !var12.getString("Family").equals("MELEE")) {
                                if (var12.getString("Family").equals("BOW") && !var0.is(DamageTypeTags.IS_PROJECTILE)) {
                                    return new CompoundTag();
                                }

                                return var12.copy();
                            }

                            return new CompoundTag();
                        }

                        if (var3 instanceof ThrownTrident var6
                            && var0.is(DamageTypeTags.IS_PROJECTILE)
                            && var6.level().dimension().location().toString().equals(var12.getString("Dimension"))) {
                            FoodWeaponSourceLedger.TridentCarrierLineage var7 = null;

                            try {
                                var7 = var6 instanceof OwnerTridentCarrierAccess var8 ? var8.ftboss$ownerTridentLineage() : null;
                                ItemStack var13 = FoodWeaponTridentCarrierReader.readOriginalItem(var6);
                                if (tridentCarrierLineage(var6, var13, var7) != null && (var11 == null || var7.origin == var11.receipt)) {
                                    if (isTrident(var13)
                                        && var12.getUUID("WeaponId").equals(FoodWeaponLifecycle.weaponId(var13))
                                        && boundProfile(var13).equals(var12.getString("BoundProfile"))
                                        && FoodWeaponLifecycle.repairProfile(var13).equals(var12.getString("RepairProfile"))
                                        && socketFingerprint(var13, "MELEE").equals(var12.getString("SocketFingerprint"))
                                        && materialFingerprint(var13).equals(var12.getString("MaterialFingerprint"))
                                        && tridentRawMatchesExpected(var13, var12.getCompound("Weapon"))) {
                                        return var12.copy();
                                    }

                                    return new CompoundTag();
                                }

                                return new CompoundTag();
                            } catch (Throwable var9) {
                                rejectTridentExtra(var6, var7, null, var11, null, null, var9);
                                return new CompoundTag();
                            }
                        }

                        return new CompoundTag();
                    }

                    return new CompoundTag();
                }

                return new CompoundTag();
            }
        } else {
            return new CompoundTag();
        }
    }

    private static boolean terminalDomain(String var0) {
        return var0.equals("PASSIVE_KILL") || var0.equals("MATERIAL_KILL") || var0.equals("KILL_FEAST") || var0.equals("SOCKET_KILL");
    }

    public static boolean claim(CompoundTag var0, ServerPlayer var1, UUID var2, FoodWeaponSourceLedger.EffectDomain var3, boolean var4, long var5) {
        if (var3 != null && terminalDomain(var3.name())) {
            return false;
        } else if (!var4 && var3 != null && var2 != null && var1 != null && var5 == clock(var1) && valid(var0, var1) && damageAuthorized(var0)) {
            if (!var0.contains("FoundationSnapshot") || !var0.getString("BoundProfile").isEmpty() && var0.getCompound("FoundationSnapshot").getInt("Level") > 0
                )
             {
                FoodWeaponSourceLedger.OriginalInteractionBinding var7 = currentInteraction(var0, var1);
                if (var7 == null) {
                    return false;
                } else {
                    FoodWeaponSourceLedger.ServerState var8 = state(server(var1));
                    synchronized (FoodWeaponSourceLedger.class) {
                        FoodWeaponSourceLedger.InteractionRoot var10 = var7.root;
                        FoodWeaponSourceLedger.OwnerState var11 = var10.ownerState;
                        if (SERVERS.get(server(var1)) == var8
                            && !var8.closed
                            && var10.serverState == var8
                            && !var10.closed
                            && !var10.retired
                            && var10.thread == Thread.currentThread()
                            && var10.current == var7
                            && !var7.closed
                            && var8.ownerEpochs.get(var1.getUUID()) == var11
                            && !var11.closed
                            && var11.current == var10
                            && var10.readCut.matches(var0)
                            && var7.snapshot.equals(var0)) {
                            var8.claims.entrySet().removeIf(var2x -> var5 >= var2x.getValue().expiresAt());
                            FoodWeaponSourceLedger.Claim var12 = new FoodWeaponSourceLedger.Claim(
                                var1.getUUID(), var0.getUUID("AttackId"), FoodWeaponSourceLedger.DomainKind.CLAIM, var3.ordinal(), null
                            );
                            if (!var8.claims.containsKey(var12) && var8.claims.size() < 4096) {
                                var8.claims.put(var12, new FoodWeaponSourceLedger.Commit(var0.getLong("ExpiresAt"), 1.0, true));
                                return true;
                            } else {
                                return false;
                            }
                        } else {
                            return false;
                        }
                    }
                }
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    private static boolean attemptSourceValid(CompoundTag var0, ServerPlayer var1) {
        return var1 != null
            && var1.isAlive()
            && !var1.isSpectator()
            && validReadOnly(var0, var1)
            && damageAuthorized(var0)
            && (!"GENERIC_J".equals(var0.getString("DamagePurpose")) || originalHandContinuousReadOnly(var0, var1))
            && (!var0.contains("FoundationSnapshot") || !var0.getString("BoundProfile").isEmpty() && var0.getCompound("FoundationSnapshot").getInt("Level") > 0);
    }

    private static FoodWeaponSourceLedger.Claim attemptKey(CompoundTag var0, ServerPlayer var1, FoodWeaponSourceLedger.AttemptDomain var2) {
        try {
            FoodWeaponSourceLedger.EffectDomain var3 = FoodWeaponSourceLedger.EffectDomain.valueOf(var2.name());
            return new FoodWeaponSourceLedger.Claim(var1.getUUID(), var0.getUUID("AttackId"), FoodWeaponSourceLedger.DomainKind.CLAIM, var3.ordinal(), null);
        } catch (IllegalArgumentException var4) {
            return new FoodWeaponSourceLedger.Claim(var1.getUUID(), var0.getUUID("AttackId"), FoodWeaponSourceLedger.DomainKind.ATTEMPT, var2.ordinal(), null);
        }
    }

    public static FoodWeaponSourceLedger.AttemptQuote quoteAttempt(CompoundTag var0, ServerPlayer var1, FoodWeaponSourceLedger.AttemptDomain var2, double var3) {
        if (var2 != null && !terminalDomain(var2.name()) && Double.isFinite(var3) && !(var3 < 0.0) && !(var3 > 1.0) && attemptSourceValid(var0, var1)) {
            FoodWeaponSourceLedger.OriginalInteractionBinding var5 = currentInteraction(var0, var1);
            if (var5 == null) {
                return new FoodWeaponSourceLedger.AttemptQuote(FoodWeaponSourceLedger.AttemptStatus.INVALID, var2, var3, Double.NaN, false, "", null);
            } else {
                FoodWeaponSourceLedger.ServerState var6;
                FoodWeaponSourceLedger.Commit var7;
                synchronized (FoodWeaponSourceLedger.class) {
                    var6 = SERVERS.get(server(var1));
                    if (var6 == null || var6.closed) {
                        return new FoodWeaponSourceLedger.AttemptQuote(FoodWeaponSourceLedger.AttemptStatus.INVALID, var2, var3, Double.NaN, false, "", null);
                    }

                    FoodWeaponSourceLedger.Claim var9 = attemptKey(var0, var1, var2);
                    var7 = var6.claims.get(var9);
                }

                if (var7 != null && Double.compare(var3, var7.chance()) != 0) {
                    return new FoodWeaponSourceLedger.AttemptQuote(FoodWeaponSourceLedger.AttemptStatus.INVALID, var2, var3, Double.NaN, false, "", null);
                } else {
                    CompoundTag var19 = new CompoundTag();
                    var19.putInt("QuoteVersion", 1);
                    var19.putString("Domain", var2.name());
                    var19.putUUID("Owner", var1.getUUID());
                    var19.putUUID("AttackId", var0.getUUID("AttackId"));
                    var19.putUUID("WeaponId", var0.getUUID("WeaponId"));
                    var19.putUUID("ServerEpoch", var0.getUUID("ServerEpoch"));
                    var19.putUUID("OwnerEpoch", var0.getUUID("OwnerEpoch"));
                    var19.putString("ActionSignature", var0.getString("ServerSignature"));
                    String var20 = signature(var19, var6);
                    if (var20.isEmpty()) {
                        return new FoodWeaponSourceLedger.AttemptQuote(FoodWeaponSourceLedger.AttemptStatus.INVALID, var2, var3, Double.NaN, false, "", null);
                    } else {
                        double var10 = (double)(Long.parseUnsignedLong(var20.substring(0, 16), 16) >>> 11) * 1.110223E-16F;
                        boolean var12 = var10 < var3;
                        var19.putDouble("Chance", var3);
                        String var13 = signature(var19, var6);
                        FoodWeaponSourceLedger.AttemptStatus var14 = var7 == null
                            ? FoodWeaponSourceLedger.AttemptStatus.AVAILABLE
                            : (var7.success() ? FoodWeaponSourceLedger.AttemptStatus.COMMITTED_SUCCESS : FoodWeaponSourceLedger.AttemptStatus.COMMITTED_FAILURE);
                        synchronized (FoodWeaponSourceLedger.class) {
                            return SERVERS.get(server(var1)) == var6
                                    && !var6.closed
                                    && !var5.closed
                                    && var5.root.current == var5
                                    && !var5.root.closed
                                    && !var5.root.retired
                                    && !var5.root.ownerState.closed
                                    && var5.root.ownerState.current == var5.root
                                ? new FoodWeaponSourceLedger.AttemptQuote(var14, var2, var3, var10, var7 == null ? var12 : var7.success(), var13, var5)
                                : new FoodWeaponSourceLedger.AttemptQuote(FoodWeaponSourceLedger.AttemptStatus.INVALID, var2, var3, Double.NaN, false, "", null);
                        }
                    }
                }
            }
        } else {
            return new FoodWeaponSourceLedger.AttemptQuote(FoodWeaponSourceLedger.AttemptStatus.INVALID, var2, var3, Double.NaN, false, "", null);
        }
    }

    public static FoodWeaponSourceLedger.AttemptQuote quoteAttempt(DamageSource var0, ServerPlayer var1, FoodWeaponSourceLedger.AttemptDomain var2, double var3) {
        return quoteAttempt(resolveReadOnly(var0, var1), var1, var2, var3);
    }

    public static FoodWeaponSourceLedger.AttemptBatchQuote quoteAttemptBatch(
        DamageSource var0, ServerPlayer var1, List<FoodWeaponSourceLedger.AttemptRequest> var2
    ) {
        return quoteAttemptBatch(resolveReadOnly(var0, var1), var1, var2, List.of());
    }

    public static FoodWeaponSourceLedger.AttemptBatchQuote quoteAttemptBatch(
        CompoundTag var0, ServerPlayer var1, List<FoodWeaponSourceLedger.AttemptRequest> var2
    ) {
        return quoteAttemptBatch(var0, var1, var2, List.of());
    }

    public static FoodWeaponSourceLedger.AttemptBatchQuote quoteAttemptBatch(
        DamageSource var0, ServerPlayer var1, List<FoodWeaponSourceLedger.AttemptRequest> var2, List<FoodWeaponSourceLedger.TargetRequest> var3
    ) {
        return quoteAttemptBatch(resolveReadOnly(var0, var1), var1, var2, var3);
    }

    private static FoodWeaponSourceLedger.AttemptBatchQuote invalidBatch() {
        return new FoodWeaponSourceLedger.AttemptBatchQuote(FoodWeaponSourceLedger.AttemptBatchStatus.INVALID, List.of(), List.of(), "", Long.MIN_VALUE, null);
    }

    private static FoodWeaponSourceLedger.Claim targetKey(CompoundTag var0, ServerPlayer var1, UUID var2, FoodWeaponSourceLedger.TargetEffectDomain var3) {
        return new FoodWeaponSourceLedger.Claim(var1.getUUID(), var0.getUUID("AttackId"), FoodWeaponSourceLedger.DomainKind.TARGET, var3.ordinal(), var2);
    }

    public static FoodWeaponSourceLedger.AttemptBatchQuote quoteAttemptBatch(
        CompoundTag var0, ServerPlayer var1, List<FoodWeaponSourceLedger.AttemptRequest> var2, List<FoodWeaponSourceLedger.TargetRequest> var3
    ) {
        if (var2 != null
            && var3 != null
            && (!var2.isEmpty() || !var3.isEmpty())
            && var2.size() <= FoodWeaponSourceLedger.AttemptDomain.values().length
            && var3.size() <= 4096
            && attemptSourceValid(var0, var1)) {
            FoodWeaponSourceLedger.OriginalInteractionBinding var4 = currentInteraction(var0, var1);
            if (var4 == null) {
                return invalidBatch();
            } else {
                ArrayList var5 = new ArrayList();
                EnumSet var6 = EnumSet.noneOf(FoodWeaponSourceLedger.AttemptDomain.class);
                int var7 = 0;

                for (FoodWeaponSourceLedger.AttemptRequest var9 : var2) {
                    if (var9 != null && var9.domain() != null && var6.add(var9.domain())) {
                        FoodWeaponSourceLedger.AttemptQuote var10 = quoteAttempt(var0, var1, var9.domain(), var9.chance());
                        if (var10.status() != FoodWeaponSourceLedger.AttemptStatus.INVALID && var10.binding == var4) {
                            if (var10.status() == FoodWeaponSourceLedger.AttemptStatus.AVAILABLE) {
                                var7++;
                            }

                            var5.add(var10);
                            continue;
                        }

                        return invalidBatch();
                    }

                    return invalidBatch();
                }

                Map<FoodWeaponSourceLedger.Claim, FoodWeaponSourceLedger.Commit> var18;
                synchronized (FoodWeaponSourceLedger.class) {
                    FoodWeaponSourceLedger.ServerState var17 = SERVERS.get(server(var1));
                    if (var17 == null || var17.closed) {
                        return invalidBatch();
                    }

                    var18 = Map.copyOf(var17.claims);
                }

                ArrayList var20 = new ArrayList();
                HashSet var11 = new HashSet();

                for (FoodWeaponSourceLedger.TargetRequest var13 : var3) {
                    if (var13 == null || var13.target() == null || var13.domain() == null) {
                        return invalidBatch();
                    }

                    FoodWeaponSourceLedger.Claim var14 = targetKey(var0, var1, var13.target(), var13.domain());
                    if (!var11.add(var14)) {
                        return invalidBatch();
                    }

                    boolean var15 = var18.containsKey(var14);
                    var20.add(
                        new FoodWeaponSourceLedger.TargetQuote(
                            var13.target(),
                            var13.domain(),
                            var15 ? FoodWeaponSourceLedger.TargetStatus.COMMITTED : FoodWeaponSourceLedger.TargetStatus.AVAILABLE
                        )
                    );
                    if (!var15) {
                        var7++;
                    }
                }

                long var21 = clock(var1);
                long var22 = var18.values().stream().filter(var2x -> var21 < var2x.expiresAt()).count();
                return new FoodWeaponSourceLedger.AttemptBatchQuote(
                    var22 + (long)var7 <= 4096L ? FoodWeaponSourceLedger.AttemptBatchStatus.READY : FoodWeaponSourceLedger.AttemptBatchStatus.CAPACITY_BLOCKED,
                    var5,
                    var20,
                    var0.getString("ServerSignature"),
                    var21,
                    var4
                );
            }
        } else {
            return invalidBatch();
        }
    }

    private static FoodWeaponSourceLedger.AttemptBatchCommit rejectedBatch(FoodWeaponSourceLedger.AttemptBatchCommitStatus var0) {
        return new FoodWeaponSourceLedger.AttemptBatchCommit(var0, List.of(), List.of(), null);
    }

    private static FoodWeaponSourceLedger.BatchPreparation prepareAttemptBatch(
        CompoundTag var0, ServerPlayer var1, FoodWeaponSourceLedger.AttemptBatchQuote var2, long var3
    ) {
        if (var2 != null
            && interactionMatches(var2.binding, var0, var1)
            && var2.status != FoodWeaponSourceLedger.AttemptBatchStatus.INVALID
            && (!var2.entries.isEmpty() || !var2.targets.isEmpty())
            && var1 != null
            && server(var1).isSameThread()
            && var3 == clock(var1)
            && var3 == var2.quotedAt
            && attemptSourceValid(var0, var1)
            && var0.getString("ServerSignature").equals(var2.actionSignature)) {
            FoodWeaponSourceLedger.ServerState var5;
            Map var6;
            LinkedHashMap<FoodWeaponSourceLedger.Claim, FoodWeaponSourceLedger.Commit> var7;
            long var8;
            synchronized (FoodWeaponSourceLedger.class) {
                var5 = SERVERS.get(server(var1));
                if (var5 == null || var5.closed) {
                    return rejectedPreparation(FoodWeaponSourceLedger.AttemptBatchCommitStatus.INVALID);
                }

                var6 = var5.claims;
                var7 = new LinkedHashMap(var6);
                var8 = var5.latestTick;
            }

            ArrayList<FoodWeaponSourceLedger.AttemptQuote> var27 = new ArrayList<>();
            EnumSet var11 = EnumSet.noneOf(FoodWeaponSourceLedger.AttemptDomain.class);
            boolean var12 = false;
            int var13 = 0;

            for (FoodWeaponSourceLedger.AttemptQuote var15 : var2.entries) {
                if (var15 != null && var15.status != FoodWeaponSourceLedger.AttemptStatus.INVALID && var15.domain != null && var11.add(var15.domain)) {
                    FoodWeaponSourceLedger.AttemptQuote var16 = quoteAttempt(var0, var1, var15.domain, var15.chance);
                    if (var16.status != FoodWeaponSourceLedger.AttemptStatus.INVALID && var16.proof.equals(var15.proof) && var16.success == var15.success) {
                        FoodWeaponSourceLedger.Commit var17 = (FoodWeaponSourceLedger.Commit)var7.get(attemptKey(var0, var1, var15.domain));
                        if (var17 == null || var17.expiresAt == var0.getLong("ExpiresAt") && var3 < var17.expiresAt) {
                            if (var16.status != var15.status) {
                                var12 = true;
                            }

                            if (var16.status == FoodWeaponSourceLedger.AttemptStatus.AVAILABLE) {
                                var13++;
                            }

                            var27.add(var16);
                            continue;
                        }

                        return rejectedPreparation(FoodWeaponSourceLedger.AttemptBatchCommitStatus.INVALID);
                    }

                    return rejectedPreparation(FoodWeaponSourceLedger.AttemptBatchCommitStatus.INVALID);
                }

                return rejectedPreparation(FoodWeaponSourceLedger.AttemptBatchCommitStatus.INVALID);
            }

            ArrayList<FoodWeaponSourceLedger.TargetQuote> var28 = new ArrayList<>();
            HashSet var29 = new HashSet();

            for (FoodWeaponSourceLedger.TargetQuote var33 : var2.targets) {
                if (var33 != null && var33.target != null && var33.domain != null && var33.status != null) {
                    FoodWeaponSourceLedger.Claim var18 = targetKey(var0, var1, var33.target, var33.domain);
                    if (!var29.add(var18)) {
                        return rejectedPreparation(FoodWeaponSourceLedger.AttemptBatchCommitStatus.INVALID);
                    }

                    FoodWeaponSourceLedger.Commit var19 = (FoodWeaponSourceLedger.Commit)var7.get(var18);
                    if (var19 == null
                        || var19.expiresAt == var0.getLong("ExpiresAt") && var3 < var19.expiresAt && Double.compare(var19.chance, 1.0) == 0 && var19.success) {
                        FoodWeaponSourceLedger.TargetStatus var20 = var19 != null
                            ? FoodWeaponSourceLedger.TargetStatus.COMMITTED
                            : FoodWeaponSourceLedger.TargetStatus.AVAILABLE;
                        if (var20 != var33.status) {
                            var12 = true;
                        }

                        if (var20 == FoodWeaponSourceLedger.TargetStatus.AVAILABLE) {
                            var13++;
                        }

                        var28.add(new FoodWeaponSourceLedger.TargetQuote(var33.target, var33.domain, var20));
                        continue;
                    }

                    return rejectedPreparation(FoodWeaponSourceLedger.AttemptBatchCommitStatus.INVALID);
                }

                return rejectedPreparation(FoodWeaponSourceLedger.AttemptBatchCommitStatus.INVALID);
            }

            if (var12 && var13 > 0) {
                return rejectedPreparation(FoodWeaponSourceLedger.AttemptBatchCommitStatus.CONFLICT);
            } else if (var13 == 0) {
                synchronized (FoodWeaponSourceLedger.class) {
                    return SERVERS.get(server(var1)) == var5 && !var5.closed && var5.claims == var6 && var5.latestTick == var8 && var5.claims.equals(var7)
                        ? new FoodWeaponSourceLedger.BatchPreparation(
                            var5,
                            null,
                            new FoodWeaponSourceLedger.AttemptBatchCommit(
                                FoodWeaponSourceLedger.AttemptBatchCommitStatus.ALREADY_COMMITTED,
                                var27.stream()
                                    .map(
                                        var0x -> new FoodWeaponSourceLedger.AttemptResult(
                                                var0x.domain,
                                                var0x.success
                                                    ? FoodWeaponSourceLedger.AttemptCommit.ALREADY_SUCCESS
                                                    : FoodWeaponSourceLedger.AttemptCommit.ALREADY_FAILURE
                                            )
                                    )
                                    .toList(),
                                var28.stream()
                                    .map(
                                        var0x -> new FoodWeaponSourceLedger.TargetResult(
                                                var0x.target, var0x.domain, FoodWeaponSourceLedger.TargetCommit.ALREADY_COMMITTED
                                            )
                                    )
                                    .toList(),
                                var2
                            )
                        )
                        : rejectedPreparation(FoodWeaponSourceLedger.AttemptBatchCommitStatus.CONFLICT);
                }
            } else {
                long var31 = var7.values().stream().filter(var2x -> var3 < var2x.expiresAt()).count();
                if (var31 + (long)var13 > 4096L) {
                    return rejectedPreparation(FoodWeaponSourceLedger.AttemptBatchCommitStatus.CAPACITY_BLOCKED);
                } else {
                    LinkedHashMap var34 = new LinkedHashMap();
                    var7.forEach((var3x, var4) -> {
                        if (var3 < var4.expiresAt()) {
                            var34.put(var3x, var4);
                        }
                    });
                    ArrayList var35 = new ArrayList();
                    ArrayList var36 = new ArrayList();

                    for (FoodWeaponSourceLedger.AttemptQuote var22 : var27) {
                        if (var22.status == FoodWeaponSourceLedger.AttemptStatus.AVAILABLE) {
                            var34.put(
                                attemptKey(var0, var1, var22.domain), new FoodWeaponSourceLedger.Commit(var0.getLong("ExpiresAt"), var22.chance, var22.success)
                            );
                            var35.add(
                                new FoodWeaponSourceLedger.AttemptResult(
                                    var22.domain,
                                    var22.success
                                        ? FoodWeaponSourceLedger.AttemptCommit.COMMITTED_SUCCESS
                                        : FoodWeaponSourceLedger.AttemptCommit.COMMITTED_FAILURE
                                )
                            );
                        } else {
                            var35.add(
                                new FoodWeaponSourceLedger.AttemptResult(
                                    var22.domain,
                                    var22.success ? FoodWeaponSourceLedger.AttemptCommit.ALREADY_SUCCESS : FoodWeaponSourceLedger.AttemptCommit.ALREADY_FAILURE
                                )
                            );
                        }
                    }

                    for (FoodWeaponSourceLedger.TargetQuote var39 : var28) {
                        if (var39.status == FoodWeaponSourceLedger.TargetStatus.AVAILABLE) {
                            var34.put(
                                targetKey(var0, var1, var39.target, var39.domain), new FoodWeaponSourceLedger.Commit(var0.getLong("ExpiresAt"), 1.0, true)
                            );
                            var36.add(new FoodWeaponSourceLedger.TargetResult(var39.target, var39.domain, FoodWeaponSourceLedger.TargetCommit.COMMITTED));
                        } else {
                            var36.add(
                                new FoodWeaponSourceLedger.TargetResult(var39.target, var39.domain, FoodWeaponSourceLedger.TargetCommit.ALREADY_COMMITTED)
                            );
                        }
                    }

                    FoodWeaponSourceLedger.AttemptBatchCommit var38 = new FoodWeaponSourceLedger.AttemptBatchCommit(
                        FoodWeaponSourceLedger.AttemptBatchCommitStatus.COMMITTED, var35, var36, var2
                    );
                    synchronized (FoodWeaponSourceLedger.class) {
                        return SERVERS.get(server(var1)) == var5 && !var5.closed && var5.claims == var6 && var5.latestTick == var8 && var5.claims.equals(var7)
                            ? new FoodWeaponSourceLedger.BatchPreparation(var5, var34, var38)
                            : rejectedPreparation(FoodWeaponSourceLedger.AttemptBatchCommitStatus.CONFLICT);
                    }
                }
            }
        } else {
            return rejectedPreparation(FoodWeaponSourceLedger.AttemptBatchCommitStatus.INVALID);
        }
    }

    public static FoodWeaponSourceLedger.AttemptBatchAdmission admitAttemptBatch(
        CompoundTag var0,
        ServerPlayer var1,
        FoodWeaponSourceLedger.AttemptBatchQuote var2,
        List<FoodWeaponSourceLedger.AttemptRequest> var3,
        List<FoodWeaponSourceLedger.TargetRequest> var4,
        long var5
    ) {
        return !matchesAttemptRequests(var2, var3, var4)
            ? new FoodWeaponSourceLedger.AttemptBatchAdmission(rejectedPreparation(FoodWeaponSourceLedger.AttemptBatchCommitStatus.INVALID))
            : new FoodWeaponSourceLedger.AttemptBatchAdmission(prepareAttemptBatch(var0, var1, var2, var5));
    }

    public static boolean matchesAttemptAdmission(
        CompoundTag var0, ServerPlayer var1, FoodWeaponSourceLedger.AttemptBatchQuote var2, FoodWeaponSourceLedger.AttemptBatchAdmission var3, long var4
    ) {
        if (var3 != null
            && var3.preparation.result.batch == var2
            && var1 != null
            && var2 != null
            && interactionMatches(var2.binding, var0, var1)
            && (
                var3.status == FoodWeaponSourceLedger.AttemptBatchAdmissionStatus.READY
                    || var3.status == FoodWeaponSourceLedger.AttemptBatchAdmissionStatus.ALREADY_COMMITTED
            )
            && server(var1).isSameThread()
            && var4 == clock(var1)
            && var4 == var2.quotedAt
            && attemptSourceValid(var0, var1)
            && var0.getString("ServerSignature").equals(var2.actionSignature)) {
            FoodWeaponSourceLedger.BatchPreparation var6 = var3.preparation;
            synchronized (FoodWeaponSourceLedger.class) {
                FoodWeaponSourceLedger.ServerState var8 = SERVERS.get(server(var1));
                return var8 != null
                    && !var8.closed
                    && var8 == var6.state
                    && var8.claims == var6.originalClaims
                    && var8.latestTick == var6.latestTick
                    && var8.claims.equals(var6.claimsImage);
            }
        } else {
            return false;
        }
    }

    private static synchronized FoodWeaponSourceLedger.AttemptBatchCommit publishPreparedBatch(FoodWeaponSourceLedger.BatchPreparation var0, long var1) {
        if (var0.state == null
            || !var0.state.closed
                && var0.state.claims == var0.originalClaims
                && var0.state.latestTick == var0.latestTick
                && var0.state.claims.equals(var0.claimsImage)) {
            if (var0.replacement != null) {
                var0.state.claims = var0.replacement;
                var0.state.latestTick = var1;
            }

            return var0.result;
        } else {
            return rejectedBatch(FoodWeaponSourceLedger.AttemptBatchCommitStatus.CONFLICT);
        }
    }

    public static FoodWeaponSourceLedger.AttemptBatchCommit commitAdmittedAttemptBatch(
        CompoundTag var0,
        ServerPlayer var1,
        FoodWeaponSourceLedger.AttemptBatchQuote var2,
        FoodWeaponSourceLedger.AttemptBatchAdmission var3,
        float var4,
        boolean var5,
        boolean var6,
        long var7
    ) {
        if (var5 || var6 || !Float.isFinite(var4) || var4 <= 0.0F) {
            return rejectedBatch(FoodWeaponSourceLedger.AttemptBatchCommitStatus.NOT_SETTLED);
        } else if (var3 == null) {
            return rejectedBatch(FoodWeaponSourceLedger.AttemptBatchCommitStatus.INVALID);
        } else if (var3.status != FoodWeaponSourceLedger.AttemptBatchAdmissionStatus.READY
            && var3.status != FoodWeaponSourceLedger.AttemptBatchAdmissionStatus.ALREADY_COMMITTED) {
            return var3.preparation.result;
        } else {
            return !matchesAttemptAdmission(var0, var1, var2, var3, var7)
                ? rejectedBatch(FoodWeaponSourceLedger.AttemptBatchCommitStatus.CONFLICT)
                : publishPreparedBatch(var3.preparation, var7);
        }
    }

    public static FoodWeaponSourceLedger.AttemptBatchCommit commitAttemptBatch(
        CompoundTag var0, ServerPlayer var1, FoodWeaponSourceLedger.AttemptBatchQuote var2, float var3, boolean var4, boolean var5, long var6
    ) {
        return !var4 && !var5 && Float.isFinite(var3) && !(var3 <= 0.0F)
            ? publishPreparedBatch(prepareAttemptBatch(var0, var1, var2, var6), var6)
            : rejectedBatch(FoodWeaponSourceLedger.AttemptBatchCommitStatus.NOT_SETTLED);
    }

    public static boolean matchesAttemptBatch(
        CompoundTag var0, ServerPlayer var1, FoodWeaponSourceLedger.AttemptBatchQuote var2, FoodWeaponSourceLedger.AttemptBatchCommit var3, long var4
    ) {
        if (var2 != null
            && interactionMatches(var2.binding, var0, var1)
            && var3 != null
            && var3.batch == var2
            && var1 != null
            && var4 == clock(var1)
            && var4 == var2.quotedAt
            && attemptSourceValid(var0, var1)
            && var2.actionSignature.equals(var0.getString("ServerSignature"))
            && (
                var3.status == FoodWeaponSourceLedger.AttemptBatchCommitStatus.COMMITTED
                    || var3.status == FoodWeaponSourceLedger.AttemptBatchCommitStatus.ALREADY_COMMITTED
            )
            && var3.entries.size() == var2.entries.size()
            && var3.targets.size() == var2.targets.size()) {
            EnumSet var6 = EnumSet.noneOf(FoodWeaponSourceLedger.AttemptDomain.class);
            boolean var7 = false;

            for (FoodWeaponSourceLedger.AttemptResult var9 : var3.entries) {
                if (var9 != null && var9.domain != null && var6.add(var9.domain)) {
                    FoodWeaponSourceLedger.AttemptQuote var10 = var2.entries.stream().filter(var1x -> var1x.domain == var9.domain).findFirst().orElse(null);
                    if (var10 == null) {
                        return false;
                    }

                    FoodWeaponSourceLedger.AttemptQuote var11 = quoteAttempt(var0, var1, var10.domain, var10.chance);
                    if (var11.status != FoodWeaponSourceLedger.AttemptStatus.INVALID
                        && var11.status != FoodWeaponSourceLedger.AttemptStatus.AVAILABLE
                        && var11.proof.equals(var10.proof)
                        && var11.success == var10.success) {
                        boolean var12 = var9.commit == FoodWeaponSourceLedger.AttemptCommit.COMMITTED_SUCCESS
                            || var9.commit == FoodWeaponSourceLedger.AttemptCommit.ALREADY_SUCCESS;
                        boolean var13 = var9.commit == FoodWeaponSourceLedger.AttemptCommit.COMMITTED_FAILURE
                            || var9.commit == FoodWeaponSourceLedger.AttemptCommit.ALREADY_FAILURE;
                        if ((var12 || var13) && var12 == var11.success) {
                            boolean var14 = var9.commit == FoodWeaponSourceLedger.AttemptCommit.COMMITTED_SUCCESS
                                || var9.commit == FoodWeaponSourceLedger.AttemptCommit.COMMITTED_FAILURE;
                            if (var14 && var10.status != FoodWeaponSourceLedger.AttemptStatus.AVAILABLE) {
                                return false;
                            }

                            var7 |= var14;
                            continue;
                        }

                        return false;
                    }

                    return false;
                }

                return false;
            }

            Map var18;
            synchronized (FoodWeaponSourceLedger.class) {
                FoodWeaponSourceLedger.ServerState var17 = SERVERS.get(server(var1));
                if (var17 == null || var17.closed) {
                    return false;
                }

                var18 = Map.copyOf(var17.claims);
            }

            HashSet var20 = new HashSet();

            for (FoodWeaponSourceLedger.TargetResult var22 : var3.targets) {
                if (var22 != null && var22.target != null && var22.domain != null) {
                    FoodWeaponSourceLedger.Claim var23 = targetKey(var0, var1, var22.target, var22.domain);
                    if (!var20.add(var23)) {
                        return false;
                    }

                    FoodWeaponSourceLedger.TargetQuote var24 = var2.targets
                        .stream()
                        .filter(var1x -> var1x.target.equals(var22.target) && var1x.domain == var22.domain)
                        .findFirst()
                        .orElse(null);
                    FoodWeaponSourceLedger.Commit var15 = (FoodWeaponSourceLedger.Commit)var18.get(var23);
                    if (var24 != null
                        && var15 != null
                        && var15.success
                        && Double.compare(var15.chance, 1.0) == 0
                        && var15.expiresAt == var0.getLong("ExpiresAt")) {
                        if (var22.commit == FoodWeaponSourceLedger.TargetCommit.COMMITTED) {
                            if (var24.status != FoodWeaponSourceLedger.TargetStatus.AVAILABLE) {
                                return false;
                            }

                            var7 = true;
                        } else if (var22.commit != FoodWeaponSourceLedger.TargetCommit.ALREADY_COMMITTED) {
                            return false;
                        }
                        continue;
                    }

                    return false;
                }

                return false;
            }

            return var7 == (var3.status == FoodWeaponSourceLedger.AttemptBatchCommitStatus.COMMITTED);
        } else {
            return false;
        }
    }

    public static boolean matchesAttemptRequests(
        FoodWeaponSourceLedger.AttemptBatchQuote var0, List<FoodWeaponSourceLedger.AttemptRequest> var1, List<FoodWeaponSourceLedger.TargetRequest> var2
    ) {
        if (var0 != null
            && var0.status != FoodWeaponSourceLedger.AttemptBatchStatus.INVALID
            && var1 != null
            && var2 != null
            && var1.size() == var0.entries.size()
            && var2.size() == var0.targets.size()) {
            EnumSet var3 = EnumSet.noneOf(FoodWeaponSourceLedger.AttemptDomain.class);

            for (FoodWeaponSourceLedger.AttemptRequest var5 : var1) {
                if (var5 != null && var5.domain != null && var3.add(var5.domain)) {
                    FoodWeaponSourceLedger.AttemptQuote var6 = var0.entries.stream().filter(var1x -> var1x.domain == var5.domain).findFirst().orElse(null);
                    if (var6 != null && Double.compare(var6.chance, var5.chance) == 0) {
                        continue;
                    }

                    return false;
                }

                return false;
            }

            HashSet var7 = new HashSet();

            for (FoodWeaponSourceLedger.TargetRequest var9 : var2) {
                if (var9 == null
                    || var9.target == null
                    || var9.domain == null
                    || !var7.add(var9)
                    || var0.targets.stream().noneMatch(var1x -> var1x.target.equals(var9.target) && var1x.domain == var9.domain)) {
                    return false;
                }
            }

            return true;
        } else {
            return false;
        }
    }

    public static FoodWeaponSourceLedger.AttemptCommit commitAttempt(
        CompoundTag var0, ServerPlayer var1, FoodWeaponSourceLedger.AttemptQuote var2, float var3, boolean var4, boolean var5, long var6
    ) {
        if (var2 != null && var1 != null && var0 != null) {
            if (interactionMatches(var2.binding, var0, var1)) {
                FoodWeaponSourceLedger.AttemptBatchQuote var8 = new FoodWeaponSourceLedger.AttemptBatchQuote(
                    FoodWeaponSourceLedger.AttemptBatchStatus.READY, List.of(var2), List.of(), var0.getString("ServerSignature"), var6, var2.binding
                );
                FoodWeaponSourceLedger.AttemptBatchCommit var9 = commitAttemptBatch(var0, var1, var8, var3, var4, var5, var6);
                if (var9.status == FoodWeaponSourceLedger.AttemptBatchCommitStatus.NOT_SETTLED) {
                    return FoodWeaponSourceLedger.AttemptCommit.NOT_SETTLED;
                } else {
                    return var9.entries.isEmpty() ? FoodWeaponSourceLedger.AttemptCommit.INVALID : var9.entries.get(0).commit();
                }
            } else {
                return !var4 && !var5 && Float.isFinite(var3) && !(var3 <= 0.0F)
                    ? FoodWeaponSourceLedger.AttemptCommit.INVALID
                    : FoodWeaponSourceLedger.AttemptCommit.NOT_SETTLED;
            }
        } else {
            return !var4 && !var5 && Float.isFinite(var3) && !(var3 <= 0.0F)
                ? FoodWeaponSourceLedger.AttemptCommit.INVALID
                : FoodWeaponSourceLedger.AttemptCommit.NOT_SETTLED;
        }
    }

    public static boolean claimSettledTarget(
        CompoundTag var0, ServerPlayer var1, UUID var2, FoodWeaponSourceLedger.TargetEffectDomain var3, float var4, boolean var5, boolean var6, long var7
    ) {
        if (var2 != null && var3 != null) {
            FoodWeaponSourceLedger.AttemptBatchQuote var9 = quoteAttemptBatch(
                var0, var1, List.of(), List.of(new FoodWeaponSourceLedger.TargetRequest(var2, var3))
            );
            FoodWeaponSourceLedger.AttemptBatchCommit var10 = commitAttemptBatch(var0, var1, var9, var4, var5, var6, var7);
            return !var10.targets.isEmpty() && var10.targets.get(0).commit() == FoodWeaponSourceLedger.TargetCommit.COMMITTED;
        } else {
            return false;
        }
    }

    private static boolean damageAuthorized(CompoundTag var0) {
        if (var0 != null && Double.isFinite(FoodWeaponDamageScaling.residual(var0))) {
            String var1 = var0.getString("DamagePurpose");
            if ("GENERIC_J".equals(var1)) {
                return true;
            } else if (!"NATIVE_CARRIER_HURT".equals(var1) && !"NATIVE_MEAT_ACTUAL_HURT".equals(var1) && !"NATIVE_IRON_ACTUAL_HURT".equals(var1)) {
                return "NATIVE_PROJECTILE".equals(var1)
                    && var0.hasUUID("NativeReceiptId")
                    && var0.hasUUID("NativeEntityId")
                    && List.of("BOW", "CROSSBOW", "TRIDENT").contains(var0.getString("NativeEntry"))
                    && Double.compare(var0.getDouble("RarityFactor"), 1.0) == 0
                    && Double.compare(var0.getDouble("AppliedMaterial"), 1.0) == 0
                    && Double.compare(var0.getDouble("AppliedEvolution"), 1.0) == 0
                    && Double.compare(var0.getDouble("AppliedRarity"), 1.0) == 0;
            } else {
                FoodWeaponSourceLedger.NativeHurtScope var2 = PRIVATE_NATIVE_HURT.get();
                return var2 != null
                    && var0.equals(var2.snapshot)
                    && nativeHurtLive(var2)
                    && Double.compare(var0.getDouble("RarityFactor"), 1.0) == 0
                    && Double.compare(var0.getDouble("AppliedMaterial"), 1.0) == 0
                    && Double.compare(var0.getDouble("AppliedEvolution"), 1.0) == 0
                    && Double.compare(var0.getDouble("AppliedRarity"), 1.0) == 0;
            }
        } else {
            return false;
        }
    }

    public static boolean originalHandContinuous(CompoundTag var0, ServerPlayer var1) {
        return !valid(var0, var1) ? false : originalHandContinuousReadOnly(var0, var1);
    }

    public static boolean originalHandContinuousReadOnly(CompoundTag var0, ServerPlayer var1) {
        if (!validReadOnly(var0, var1)) {
            return false;
        } else {
            InteractionHand var2 = InteractionHand.valueOf(var0.getString("OriginalHand"));
            ItemStack var3 = var1.getItemInHand(var2);
            ItemStack var4 = NativeFoundationItemData.decode(var0.getCompound("Weapon"));
            ResourceLocation var5 = var3.isEmpty() ? null : BuiltInRegistries.ITEM.getKey(var3.getItem());
            ResourceLocation var6 = var4.isEmpty() ? null : BuiltInRegistries.ITEM.getKey(var4.getItem());
            return !var3.isEmpty()
                && var3.getCount() == 1
                && !var4.isEmpty()
                && var4.getCount() == 1
                && var3.getItem() == var4.getItem()
                && var5 != null
                && var5.equals(var6)
                && FoodWeaponFamilyBuild.kind(var3).name().equals(var0.getString("Family"))
                && !FoodWeaponLifecycle.isBroken(var3)
                && var0.getUUID("WeaponId").equals(FoodWeaponLifecycle.weaponId(var3))
                && boundProfile(var3).equals(var0.getString("BoundProfile"))
                && FoodWeaponLifecycle.repairProfile(var3).equals(var0.getString("RepairProfile"))
                && materialFingerprint(var3).equals(var0.getString("MaterialFingerprint"))
                && socketFingerprint(var3, var0.getString("Family")).equals(var0.getString("SocketFingerprint"))
                && foundationContinuous(var0, var1, var3);
        }
    }

    public static boolean isTrident(ItemStack var0) {
        ResourceLocation var1 = var0.isEmpty() ? null : BuiltInRegistries.ITEM.getKey(var0.getItem());
        return var1 != null && var1.toString().equals("minecraft:trident");
    }

    private static FoodWeaponSourceLedger.SourceTicket issueTrident(ServerPlayer var0, ItemStack var1, InteractionHand var2) {
        if (var0 != null && var1 != null && OwnerFullRepairGuard.permits(var1) && (NativeSourceLedgerItemData.rawCustomDataForObservation(var1) == null || canonicalInputValid(NativeSourceLedgerItemData.rawCustomDataForObservation(var1)))) {
            CompoundTag var3 = NativeFoundationItemData.image(var1);
            if (canonicalInputValid(var3)
                && isTrident(var1)
                && var2 != null
                && var0.getItemInHand(var2) == var1
                && !FoodWeaponLifecycle.isBroken(var1)
                && var1.getDamageValue() < var1.getMaxDamage()
                && (!boundProfile(var1).isEmpty() || nativeFoundation(var1))) {
                if (FourWeaponFoundationAdapter.applies(var1) && foundationRules(var0, var1).isEmpty()) {
                    return null;
                } else {
                    FoodWeaponLifecycle.ensureTracked(var1);
                    UUID var4 = FoodWeaponLifecycle.weaponId(var1);
                    if (var4 == null) {
                        return null;
                    } else {
                        String var5 = materialFingerprint(var1);
                        if (var5.isEmpty()) {
                            return null;
                        } else {
                            FoodWeaponSourceLedger.ServerState var6 = state(server(var0));
                            long var7 = clock(var0);
                            synchronized (FoodWeaponSourceLedger.class) {
                                if (SERVERS.get(server(var0)) == var6 && !var6.closed && var7 >= var6.latestTick && var7 >= 0L && var7 != Long.MAX_VALUE) {
                                    var6.latestTick = var7;
                                    FoodWeaponSourceLedger.SourceTicket var10 = new FoodWeaponSourceLedger.SourceTicket(
                                        var0.getUUID(),
                                        var4,
                                        var2,
                                        boundProfile(var1),
                                        FoodWeaponLifecycle.repairProfile(var1),
                                        socketFingerprint(var1, "MELEE"),
                                        var5,
                                        UUID.randomUUID(),
                                        var0.level().dimension().location().toString(),
                                        var7,
                                        var7 + 1L,
                                        var6.epoch,
                                        ensureOwnerState(var6, var0.getUUID()).epoch,
                                        var1.getDamageValue(),
                                        var1.getMaxDamage()
                                    );

                                    try {
                                        if (tridentRawMatchesExpected(var1, var3)) {
                                            TRIDENT_BEFORE.set(new FoodWeaponSourceLedger.TridentTicketBefore(var10, var1, var3));
                                        }
                                    } catch (Throwable var13) {
                                        TRIDENT_BEFORE.remove();
                                    }

                                    return var10;
                                } else {
                                    return null;
                                }
                            }
                        }
                    }
                }
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    public static void releaseTrident(ServerPlayer var0, ItemStack var1, InteractionHand var2, Runnable var3) {
        FoodWeaponSourceLedger.TicketState var4 = TRIDENT_TICKET.get();
        FoodWeaponSourceLedger.TridentTicketBefore var5 = TRIDENT_BEFORE.get();
        TRIDENT_BEFORE.remove();
        FoodWeaponSourceLedger.SourceTicket var6 = issueTrident(var0, var1, var2);
        TRIDENT_TICKET.remove();
        if (var6 != null) {
            TRIDENT_TICKET.set(new FoodWeaponSourceLedger.TicketState(var6, var1, server(var0), foundationRules(var0, var1)));
        }

        try {
            FoodWeaponRanged.withSource(null, new CompoundTag(), () -> {
                var3.run();
                return null;
            });
        } finally {
            TRIDENT_TICKET.remove();
            TRIDENT_BEFORE.remove();
            if (var4 != null
                && (var5 == null || !var5.observationRejected)
                && var4.ticket().ownerEpoch().equals(ownerEpoch(state(var4.server()), var4.ticket().owner()))
                && var4.ticket().serverEpoch().equals(state(var4.server()).epoch)) {
                TRIDENT_TICKET.set(var4);
                if (var5 != null && var5.ticket == var4.ticket()) {
                    TRIDENT_BEFORE.set(var5);
                }
            }
        }
    }

    public static void attachTrident(ThrownTrident var0, LivingEntity var1, ItemStack var2) {
        if (NATIVE_DEPTH.get() <= 0) {
            FoodWeaponSourceLedger.TicketState var3 = TRIDENT_TICKET.get();
            FoodWeaponSourceLedger.TridentTicketBefore var4 = TRIDENT_BEFORE.get();
            if (TRIDENT_TICKET.get() == var3) {
                TRIDENT_TICKET.remove();
            }

            if (var3 != null
                && var1 instanceof ServerPlayer var5
                && var0.getOwner() == var5
                && var2 == var3.actualWeapon()
                && server(var5) == var3.server()
                && !var0.level().isClientSide) {
                FoodWeaponSourceLedger.SourceTicket var6 = var3.ticket();
                long var7 = clock(var5);
                if (var6.owner().equals(var5.getUUID())
                    && var7 >= var6.issuedAt()
                    && var7 < var6.expiresAt()
                    && var0.level().dimension().location().toString().equals(var6.dimension())) {
                    FoodWeaponSourceLedger.TridentCarrierLineage var11 = null;

                    ItemStack var9;
                    CompoundTag var10;
                    try {
                        var11 = var0 instanceof OwnerTridentCarrierAccess var12 ? var12.ftboss$ownerTridentLineage() : null;
                        var9 = FoodWeaponTridentCarrierReader.readOriginalItem(var0);
                        if (tridentCarrierLineage(var0, var9, var11) == null
                            || var11.origin != var3
                            || var11.input != var2
                            || var4 == null
                            || var4.observationRejected
                            || var4.ticket != var6
                            || var4.actual != var2
                            || var4.thread != Thread.currentThread()) {
                            return;
                        }

                        var10 = var4.expected.image();
                        if (!tridentRawMatchesExpected(var9, var10)) {
                            return;
                        }

                        if (!isTrident(var9)
                            || !var6.weaponId().equals(FoodWeaponLifecycle.weaponId(var9))
                            || !var6.profile().equals(boundProfile(var9))
                            || !var6.repairProfile().equals(FoodWeaponLifecycle.repairProfile(var9))
                            || !var6.socketFingerprint().equals(socketFingerprint(var9, "MELEE"))
                            || !var6.materialFingerprint().equals(materialFingerprint(var9))
                            || !var3.foundationRules().equals(foundationRules(var5, var9))) {
                            return;
                        }
                    } catch (Throwable var18) {
                        rejectTridentExtra(var0, var11, null, null, var3, var4, var18);
                        return;
                    }

                    CompoundTag var19 = new CompoundTag();
                    var19.putInt("SnapshotVersion", 3);
                    var19.putUUID("Owner", var6.owner());
                    var19.putUUID("WeaponId", var6.weaponId());
                    var19.putUUID("AttackId", var6.actionId());
                    var19.putUUID("ServerEpoch", var6.serverEpoch());
                    var19.putUUID("OwnerEpoch", var6.ownerEpoch());
                    var19.putString("OriginalHand", var6.originalHand().name());
                    var19.putString("Dimension", var6.dimension());
                    var19.putLong("CapturedAt", var6.issuedAt());
                    var19.putLong("ExpiresAt", var6.issuedAt() + 1200L);
                    var19.putString("Family", "MELEE");
                    var19.putString("SourceKind", "TRIDENT");
                    var19.putString("BoundProfile", var6.profile());
                    var19.putString("RepairProfile", var6.repairProfile());
                    var19.putString("SocketFingerprint", var6.socketFingerprint());
                    var19.putString("MaterialFingerprint", var6.materialFingerprint());
                    var19.putBoolean("TridentLaunchAuthorized", true);
                    var19.putInt("PreWearDamage", var6.preWearDamage());
                    var19.put("Weapon", var10);
                    if (FourWeaponFoundationAdapter.applies(var9)) {
                        CompoundTag var13 = FoodWeaponMaterialFingerprint.foundationSnapshot(var9);
                        if (var13.isEmpty() || var3.foundationRules().isEmpty()) {
                            return;
                        }

                        var19.put("FoundationSnapshot", var13.copy());
                        var19.putString("FoundationApprovedRules", var3.foundationRules());
                    }

                    CompoundTag var20 = NativeFoundationItemData.element(var9, "FTBossWeaponMaterials");
                    if (var20 != null) {
                        for (Part var17 : Part.values()) {
                            var19.putString(var17.name(), var20.getString(var17.name()));
                        }
                    }

                    WeaponMaterialPowers.snapshot(var9, var19);
                    if (!OwnerRepairGenerationAuthority.stamp(var2, var19, state(server(var5)).revocations)) {
                        return;
                    }

                    var19.putString("ServerSignature", signature(var19, state(server(var5))));
                    if (!OwnerFullRepairGuard.bind(var2, var6.weaponId())) {
                        return;
                    }

                    if (valid(var19, var5) && !var0.getPersistentData().contains("FTBossWeaponMaterials")) {
                        var0.getPersistentData().put("FTBossWeaponMaterials", var19);
                    }

                    return;
                }

                return;
            }
        }
    }

    public static void clear(ServerPlayer var0) {
        FoodWeaponSourceLedger.NativeIronCallbackToken var1 = PRIVATE_IRON_CALLBACK.get();
        if (var1 != null && var1.payment.cast.use.player == var0) {
            nativeIronCallbackClose(var1);
        }

        FoodWeaponSourceLedger.NativeHurtScope var2 = PRIVATE_NATIVE_HURT.get();
        if (var2 != null && var2.player == var0) {
            closeNativeHurtScope(var2);
        }

        FoodWeaponSourceLedger.NativeIngressToken var3 = PRIVATE_INGRESS.get();
        if (var3 != null && var3.player == var0) {
            cancelNativeIngress(var3);
        }

        FoodWeaponSourceLedger.NativeIronUseToken var4 = PRIVATE_IRON_USE.get();
        if (var4 != null && var4.player == var0) {
            var4.invalid = true;
            PRIVATE_IRON_USE.remove();
        }

        FoodWeaponSourceLedger.NativeOperation var5 = NATIVE_OPERATION.get();
        if (var5 != null && var5.player == var0) {
            var5.invalid = true;
            NATIVE_OPERATION.remove();
        }

        FoodWeaponSourceLedger.ServerState var6 = state(server(var0));
        synchronized (FoodWeaponSourceLedger.class) {
            for (int var8 = 0; var8 < 4096; var8++) {
                FoodWeaponSourceLedger.InteractionRoot var9 = ACTIVE_ROOT_PROJECTIONS[var8];
                if (var9 != null && var9.player == var0) {
                    retireInteraction(var9);
                }
            }

            if (SERVERS.get(server(var0)) == var6 && !var6.closed) {
                FoodWeaponSourceLedger.OwnerState var12 = var6.ownerEpochs.remove(var0.getUUID());
                if (var12 != null) {
                    var12.closed = true;
                    var12.interactionChange = new Object();
                    FoodWeaponSourceLedger.InteractionRoot var13 = var12.current;
                    if (var13 != null && !var13.closed) {
                        retireInteraction(var13);
                    }
                }

                var6.claims.keySet().removeIf(var1x -> var1x.owner().equals(var0.getUUID()));
                FoodWeaponSourceLedger.TicketState var14 = TRIDENT_TICKET.get();
                if (var14 != null && var14.ticket().owner().equals(var0.getUUID())) {
                    TRIDENT_TICKET.remove();
                }
            }
        }

        FoodWeaponRanged.clearOwnerScopes(var0);
    }

    public static void clearServer(MinecraftServer var0) {
        FoodWeaponSourceLedger.NativeIronCallbackToken var1 = PRIVATE_IRON_CALLBACK.get();
        if (var1 != null && server(var1.payment.cast.use.player) == var0) {
            nativeIronCallbackClose(var1);
        }

        FoodWeaponSourceLedger.NativeHurtScope var2 = PRIVATE_NATIVE_HURT.get();
        if (var2 != null && server(var2.player) == var0) {
            closeNativeHurtScope(var2);
        }

        FoodWeaponSourceLedger.NativeIngressToken var3 = PRIVATE_INGRESS.get();
        if (var3 != null && server(var3.player) == var0) {
            cancelNativeIngress(var3);
        }

        FoodWeaponSourceLedger.NativeIronUseToken var4 = PRIVATE_IRON_USE.get();
        if (var4 != null && server(var4.player) == var0) {
            var4.invalid = true;
            PRIVATE_IRON_USE.remove();
        }

        FoodWeaponSourceLedger.NativeOperation var5 = NATIVE_OPERATION.get();
        if (var5 != null && var5.server == var0) {
            var5.invalid = true;
            NATIVE_OPERATION.remove();
        }

        synchronized (FoodWeaponSourceLedger.class) {
            FoodWeaponSourceLedger.ServerState var7 = SERVERS.get(var0);
            if (var7 != null) {
                var7.closed = true;

                for (FoodWeaponSourceLedger.OwnerState var9 : var7.ownerEpochs.values()) {
                    if (var9.current != null) {
                        retireInteraction(var9.current);
                    }
                }
            }

            SERVERS.remove(var0);
            FoodWeaponSourceLedger.TicketState var12 = TRIDENT_TICKET.get();
            if (var12 != null && var12.server() == var0) {
                TRIDENT_TICKET.remove();
            }
        }

        FoodWeaponRanged.clearServerScopes(var0);
    }

    private static boolean readWindowIdentity(FoodWeaponSourceLedger.PrimaryReadWindow var0) {
        FoodWeaponSourceLedger.InteractionRoot var1 = var0.root;
        return var1.thread == Thread.currentThread()
            && !var1.closed
            && !var1.retired
            && var1.current == var0.binding
            && !var0.binding.closed
            && var1.ownerState.current == var1
            && !var1.ownerState.closed
            && !var1.serverState.closed
            && SERVERS.get(var1.server) == var1.serverState
            && var1.serverState.epoch == var1.serverEpoch
            && var1.ownerState.epoch == var1.ownerEpoch
            && var1.projectionSlot >= 0
            && ACTIVE_ROOT_PROJECTIONS[var1.projectionSlot] == var1
            && nativeObservationReadLive(var1)
            && (
                var1.expectation == null
                    ? var0.repairActual == null
                    : !var1.expectation.closed
                        && var1.expectation.actual == var0.repairActual
                        && var1.expectation.repairGeneration == var0.repairGeneration
                        && OwnerRepairGenerationAuthority.identityMatches(var0.repairActual, var0.repairGeneration, var0.repairLease)
            );
    }

    private static boolean preparedEpochIdentity(FoodWeaponSourceLedger.OwnerWriterPublication var0, MinecraftServer var1, Object var2, Object var3) {
        FoodWeaponSourceLedger.InteractionRoot var4 = var0.binding.root;
        return var4.server == var1
            && !var4.closed
            && !var4.retired
            && !var4.serverState.closed
            && !var4.ownerState.closed
            && var4.serverState.epoch == var2
            && var4.ownerState.epoch == var3
            && var4.serverEpoch == var2
            && var4.ownerEpoch == var3
            && SERVERS.get(var1) == var4.serverState
            && var4.ownerState.current == var4;
    }

    public static final class AttemptBatchAdmission {
        private final FoodWeaponSourceLedger.AttemptBatchAdmissionStatus status;
        private final FoodWeaponSourceLedger.BatchPreparation preparation;

        private AttemptBatchAdmission(FoodWeaponSourceLedger.BatchPreparation var1) {
            this.preparation = var1;

            this.status = switch (var1.result.status) {
                case COMMITTED -> FoodWeaponSourceLedger.AttemptBatchAdmissionStatus.READY;
                case ALREADY_COMMITTED -> FoodWeaponSourceLedger.AttemptBatchAdmissionStatus.ALREADY_COMMITTED;
                case CAPACITY_BLOCKED -> FoodWeaponSourceLedger.AttemptBatchAdmissionStatus.CAPACITY_BLOCKED;
                case CONFLICT -> FoodWeaponSourceLedger.AttemptBatchAdmissionStatus.CONFLICT;
                default -> FoodWeaponSourceLedger.AttemptBatchAdmissionStatus.INVALID;
            };
        }

        public FoodWeaponSourceLedger.AttemptBatchAdmissionStatus status() {
            return this.status;
        }
    }

    public static enum AttemptBatchAdmissionStatus {
        INVALID,
        READY,
        ALREADY_COMMITTED,
        CAPACITY_BLOCKED,
        CONFLICT;
    }

    public static final class AttemptBatchCommit {
        private final FoodWeaponSourceLedger.AttemptBatchCommitStatus status;
        private final List<FoodWeaponSourceLedger.AttemptResult> entries;
        private final List<FoodWeaponSourceLedger.TargetResult> targets;
        private final FoodWeaponSourceLedger.AttemptBatchQuote batch;

        private AttemptBatchCommit(
            FoodWeaponSourceLedger.AttemptBatchCommitStatus var1,
            List<FoodWeaponSourceLedger.AttemptResult> var2,
            List<FoodWeaponSourceLedger.TargetResult> var3,
            FoodWeaponSourceLedger.AttemptBatchQuote var4
        ) {
            this.status = var1;
            this.entries = List.copyOf(var2);
            this.targets = List.copyOf(var3);
            this.batch = var4;
        }

        public FoodWeaponSourceLedger.AttemptBatchCommitStatus status() {
            return this.status;
        }

        public List<FoodWeaponSourceLedger.AttemptResult> entries() {
            return this.entries;
        }

        public List<FoodWeaponSourceLedger.TargetResult> targets() {
            return this.targets;
        }

        public Optional<FoodWeaponSourceLedger.AttemptResult> attempt(FoodWeaponSourceLedger.AttemptDomain var1) {
            return this.entries.stream().filter(var1x -> var1 != null && var1x.domain == var1).findFirst();
        }

        public Optional<FoodWeaponSourceLedger.TargetResult> target(UUID var1, FoodWeaponSourceLedger.TargetEffectDomain var2) {
            return this.targets.stream().filter(var2x -> var1 != null && var2 != null && var2x.target.equals(var1) && var2x.domain == var2).findFirst();
        }
    }

    public static enum AttemptBatchCommitStatus {
        INVALID,
        NOT_SETTLED,
        CAPACITY_BLOCKED,
        CONFLICT,
        COMMITTED,
        ALREADY_COMMITTED;
    }

    public static final class AttemptBatchQuote {
        private final FoodWeaponSourceLedger.OriginalInteractionBinding binding;
        private final FoodWeaponSourceLedger.AttemptBatchStatus status;
        private final List<FoodWeaponSourceLedger.AttemptQuote> entries;
        private final List<FoodWeaponSourceLedger.TargetQuote> targets;
        private final String actionSignature;
        private final long quotedAt;

        private AttemptBatchQuote(
            FoodWeaponSourceLedger.AttemptBatchStatus var1,
            List<FoodWeaponSourceLedger.AttemptQuote> var2,
            List<FoodWeaponSourceLedger.TargetQuote> var3,
            String var4,
            long var5,
            FoodWeaponSourceLedger.OriginalInteractionBinding var7
        ) {
            this.binding = var7;
            this.status = var1;
            this.entries = List.copyOf(var2);
            this.targets = List.copyOf(var3);
            this.actionSignature = var4;
            this.quotedAt = var5;
        }

        public FoodWeaponSourceLedger.AttemptBatchStatus status() {
            return this.status;
        }

        public List<FoodWeaponSourceLedger.AttemptQuote> entries() {
            return this.entries;
        }

        public List<FoodWeaponSourceLedger.TargetQuote> targets() {
            return this.targets;
        }

        public long quotedAt() {
            return this.quotedAt;
        }
    }

    public static enum AttemptBatchStatus {
        INVALID,
        READY,
        CAPACITY_BLOCKED;
    }

    public static enum AttemptCommit {
        INVALID,
        NOT_SETTLED,
        ALREADY_FAILURE,
        ALREADY_SUCCESS,
        COMMITTED_FAILURE,
        COMMITTED_SUCCESS;
    }

    public static enum AttemptDomain {
        SERIES_ECHO,
        COLOR_REPEL,
        PROCESS_BURST,
        WOLF_PACK,
        PASSIVE_HIT,
        PASSIVE_KILL,
        MATERIAL_KILL,
        KILL_FEAST,
        SOCKET_KILL,
        BLESSING_CRIT,
        BLESSING_ABSORPTION,
        BLESSING_CHAIN,
        BLESSING_OVERLOAD,
        BLESSING_BURN,
        BLESSING_MARK,
        BLESSING_POISON,
        BLESSING_WITHER,
        BLESSING_ROOT,
        BLESSING_REGEN,
        BLESSING_CORK_RUSH,
        BLESSING_PRISMATIC,
        BLESSING_BRINE,
        SOCKET_WINE_CRIT,
        SOCKET_VEGETABLE_POISON,
        SOCKET_MEAT_HEAL,
        SOCKET_SWEET_GUARD,
        BLESSING_LIFESTEAL,
        BLESSING_REPAIR,
        BLESSING_EMERGENCY_HEART,
        BLESSING_FRENZY,
        EVOLUTION_FROST_BITE,
        EVOLUTION_SUGAR_RUSH,
        EVOLUTION_CANDY_GUARD,
        EVOLUTION_BLOOD_FEAST,
        EVOLUTION_CLEAVER_FORCE,
        EVOLUTION_HEAVY_IMPACT,
        EVOLUTION_THORN_VENOM,
        EVOLUTION_TEA_RECOVERY,
        EVOLUTION_ROOTED_GRIP,
        EVOLUTION_VINTAGE_FURY,
        EVOLUTION_TIPSY_GUARD,
        EVOLUTION_CORK_BURST,
        EVOLUTION_CHEESE_FRENZY,
        EVOLUTION_PLAGUE_BITE,
        EVOLUTION_SCAVENGER_FEAST,
        EVOLUTION_OVERLOAD_ARC,
        EVOLUTION_HIGH_VOLTAGE,
        EVOLUTION_MAGNETIC_LOCK,
        EVOLUTION_PRISMATIC_TOXIN,
        EVOLUTION_RECYCLE_GUARD,
        EVOLUTION_FERTILE_REGROWTH,
        EVOLUTION_HONEYCRISP_REBIRTH,
        EVOLUTION_GOLDEN_PEEL,
        EVOLUTION_ORCHARD_STRIKE,
        EVOLUTION_UNDERTOW,
        EVOLUTION_BRINE_BITE,
        EVOLUTION_TIDAL_RECOVERY,
        EVOLUTION_SHEPHERD_MIGHT,
        EVOLUTION_HUNTING_HOWL,
        EVOLUTION_PACK_FEAST;
    }

    public static final class AttemptQuote {
        private final FoodWeaponSourceLedger.OriginalInteractionBinding binding;
        private final FoodWeaponSourceLedger.AttemptStatus status;
        private final FoodWeaponSourceLedger.AttemptDomain domain;
        private final double chance;
        private final double sample;
        private final boolean success;
        private final String proof;

        private AttemptQuote(
            FoodWeaponSourceLedger.AttemptStatus var1,
            FoodWeaponSourceLedger.AttemptDomain var2,
            double var3,
            double var5,
            boolean var7,
            String var8,
            FoodWeaponSourceLedger.OriginalInteractionBinding var9
        ) {
            this.binding = var9;
            this.status = var1;
            this.domain = var2;
            this.chance = var3;
            this.sample = var5;
            this.success = var7;
            this.proof = var8;
        }

        public FoodWeaponSourceLedger.AttemptStatus status() {
            return this.status;
        }

        public FoodWeaponSourceLedger.AttemptDomain domain() {
            return this.domain;
        }

        public double chance() {
            return this.chance;
        }

        public double sample() {
            return this.sample;
        }

        public boolean success() {
            return this.success;
        }
    }

    public static record AttemptRequest(FoodWeaponSourceLedger.AttemptDomain domain, double chance) {
    }

    public static final class AttemptResult {
        private final FoodWeaponSourceLedger.AttemptDomain domain;
        private final FoodWeaponSourceLedger.AttemptCommit commit;

        private AttemptResult(FoodWeaponSourceLedger.AttemptDomain var1, FoodWeaponSourceLedger.AttemptCommit var2) {
            this.domain = var1;
            this.commit = var2;
        }

        public FoodWeaponSourceLedger.AttemptDomain domain() {
            return this.domain;
        }

        public FoodWeaponSourceLedger.AttemptCommit commit() {
            return this.commit;
        }
    }

    public static enum AttemptStatus {
        INVALID,
        AVAILABLE,
        COMMITTED_FAILURE,
        COMMITTED_SUCCESS;
    }

    private static final class BatchPreparation {
        private final FoodWeaponSourceLedger.ServerState state;
        private final Map<FoodWeaponSourceLedger.Claim, FoodWeaponSourceLedger.Commit> originalClaims;
        private final Map<FoodWeaponSourceLedger.Claim, FoodWeaponSourceLedger.Commit> claimsImage;
        private final long latestTick;
        private final Map<FoodWeaponSourceLedger.Claim, FoodWeaponSourceLedger.Commit> replacement;
        private final FoodWeaponSourceLedger.AttemptBatchCommit result;

        private BatchPreparation(
            FoodWeaponSourceLedger.ServerState var1,
            Map<FoodWeaponSourceLedger.Claim, FoodWeaponSourceLedger.Commit> var2,
            FoodWeaponSourceLedger.AttemptBatchCommit var3
        ) {
            this.state = var1;
            this.originalClaims = var1 == null ? null : var1.claims;
            this.claimsImage = var1 == null ? Map.of() : Map.copyOf(var1.claims);
            this.latestTick = var1 == null ? Long.MIN_VALUE : var1.latestTick;
            this.replacement = var2;
            this.result = var3;
        }
    }

    private static record Claim(UUID owner, UUID attack, FoodWeaponSourceLedger.DomainKind kind, int domain, UUID target) {
    }

    private static record Commit(long expiresAt, double chance, boolean success) {
    }

    private static enum DomainKind {
        CLAIM,
        ATTEMPT,
        TARGET;
    }

    public static enum EffectDomain {
        SERIES_ECHO,
        COLOR_REPEL,
        PROCESS_BURST,
        WOLF_PACK,
        PASSIVE_HIT,
        PASSIVE_KILL,
        MATERIAL_KILL,
        KILL_FEAST,
        SOCKET_KILL;
    }

    static final class FinalListedChildScope {
        private final FoodWeaponFinalDamage.ChildHurtCall call;
        private final FoodWeaponSourceLedger.OriginalInteractionBinding parent;
        private final FoodWeaponSourceLedger.OriginalInteractionBinding binding;
        private final FoodWeaponSourceLedger.InteractionRoot root;
        private final DamageSource source;
        private final LivingEntity target;
        private final float rawAmount;
        private final Thread thread;
        private FoodWeaponConsumerSequence.OriginalChild originalConsumerChild;
        private FoodWeaponSourceLedger.OwnerWriterPublication originalPublisher;
        private boolean hurtEntered;
        private boolean hurtActive;
        private boolean closed;

        private FinalListedChildScope(
            FoodWeaponFinalDamage.ChildHurtCall var1, FoodWeaponSourceLedger.OriginalInteractionBinding var2, DamageSource var3, LivingEntity var4, float var5
        ) {
            this.call = var1;
            this.parent = var2;
            this.root = var2.root;
            this.source = var3;
            this.target = var4;
            this.rawAmount = var5;
            this.thread = Thread.currentThread();
            this.binding = new FoodWeaponSourceLedger.OriginalInteractionBinding(this.root, var2, var3, var4, var2.snapshot, var5, var1);
            this.binding.acceptedChildScope = this;
        }
    }

    private static final class ImageCursor {
        private static final int BYTE_BUDGET = 1048576;
        private byte[] expected;
        private Object position = new Object();
        private volatile boolean closed;

        private ImageCursor(byte[] var1) {
            if (var1 != null && var1.length <= 1048576) {
                this.expected = (byte[])var1.clone();
            } else {
                throw new IllegalArgumentException("Image budget");
            }
        }

        private boolean matches(byte[] var1) {
            if (!this.closed && var1 != null && var1.length == this.expected.length) {
                for (int var2 = 0; var2 < this.expected.length; var2++) {
                    if (var1[var2] != this.expected[var2]) {
                        return false;
                    }
                }

                return true;
            } else {
                return false;
            }
        }

        private Object position() {
            return this.position;
        }

        private boolean advance(Object var1, byte[] var2, byte[] var3, byte[] var4) {
            if (!this.closed
                && var1 == this.position
                && var3 != null
                && var3.length <= 1048576
                && this.matches(var2)
                && var4 != null
                && var4.length == var3.length) {
                for (int var5 = 0; var5 < var3.length; var5++) {
                    if (var3[var5] != var4[var5]) {
                        return false;
                    }
                }

                this.expected = (byte[])var3.clone();
                this.position = new Object();
                return true;
            } else {
                return false;
            }
        }

        private void close() {
            this.closed = true;
        }
    }

    public static final class InteractionRoot {
        private final FoodWeaponSourceLedger.OwnerState ownerState;
        private final FoodWeaponSourceLedger.ServerState serverState;
        private final ServerPlayer player;
        private final MinecraftServer server;
        private final UUID serverEpoch;
        private final UUID ownerEpoch;
        private final Thread thread;
        private final long tick;
        private final FoodWeaponSourceLedger.RootSite site;
        private final FoodWeaponSourceLedger.ReadCut readCut = new FoodWeaponSourceLedger.ReadCut();
        private final FoodWeaponSourceLedger.ReadAliases readAliases = new FoodWeaponSourceLedger.ReadAliases();
        private volatile int projectionSlot = -1;
        private FoodWeaponSourceLedger.NativeRootObservation nativeObservations;
        private FoodWeaponSourceLedger.NativeProjectileObservation nativeProjectileObservation;
        private FoodWeaponSourceLedger.WeaponExpectation expectation;
        private boolean ownsExpectation;
        private volatile Object interactionChange = new Object();
        private volatile FoodWeaponSourceLedger.OwnerWriterPublication writerPublication;
        private volatile FoodWeaponSourceLedger.OriginalInteractionBinding current;
        private volatile boolean retired;
        private boolean closed;

        private InteractionRoot(
            FoodWeaponSourceLedger.OwnerState var1, FoodWeaponSourceLedger.ServerState var2, ServerPlayer var3, FoodWeaponSourceLedger.RootSite var4
        ) {
            this.ownerState = var1;
            this.serverState = var2;
            this.player = var3;
            this.server = FoodWeaponSourceLedger.server(var3);
            this.serverEpoch = var2.epoch;
            this.ownerEpoch = var1.epoch;
            this.thread = Thread.currentThread();
            this.tick = FoodWeaponSourceLedger.clock(var3);
            this.site = var4;
        }
    }

    private static final class IssuerCut {
        private final Thread thread = Thread.currentThread();
        private Object published;
        private boolean transferred;
        private volatile boolean closed;

        private boolean reserve(Object var1) {
            if (this.thread == Thread.currentThread() && !this.closed && this.published == null && var1 != null) {
                this.published = var1;
                return true;
            } else {
                return false;
            }
        }

        private boolean transfer(Object var1) {
            if (this.thread == Thread.currentThread() && !this.closed && !this.transferred && var1 != null && this.published == var1) {
                this.transferred = true;
                return true;
            } else {
                return false;
            }
        }

        private boolean close() {
            if (this.thread == Thread.currentThread() && !this.closed) {
                this.closed = true;
                return !this.transferred;
            } else {
                return false;
            }
        }
    }

    public static final class NativeCapabilityToken {
        private final FoodWeaponSourceLedger.NativeIngressToken ingress;
        private final Object capability;
        private boolean canceled;

        private NativeCapabilityToken(FoodWeaponSourceLedger.NativeIngressToken var1, Object var2) {
            this.ingress = var1;
            this.capability = var2;
        }
    }

    public static final class NativeCarrierToken {
        private final FoodWeaponSourceLedger.NativeIngressToken ingress;
        private final Entity carrier;
        private final UUID entityId;
        private boolean actor;
        private boolean added;
        private CompoundTag image;

        private NativeCarrierToken(FoodWeaponSourceLedger.NativeIngressToken var1, Entity var2) {
            super();
            boolean var10001;
            label12: {
                this.ingress = var1;
                this.carrier = var2;
                this.entityId = var2.getUUID();
                if (var2 instanceof Projectile var3 && var3.getOwner() == var1.player) {
                    var10001 = true;
                    break label12;
                }

                var10001 = false;
            }

            this.actor = var10001;
        }
    }

    public static final class NativeEvergaleAction {
        private final RawAction original;
        private final Thread thread = Thread.currentThread();
        private final ArrayList<FoodWeaponSourceLedger.NativeEvergaleSpawnWitness> witnesses = new ArrayList<>();
        private boolean rejected;
        private boolean closed;
        private Throwable originalFailure;
        private Throwable observationFailure;
        private String refusal;

        private NativeEvergaleAction(RawAction var1) {
            this.original = var1;
        }
    }

    public static final class NativeEvergaleInteraction {
        private final NativeEvergaleImpact.RawImpact original;
        private final Thread thread = Thread.currentThread();
        private boolean rejected;
        private boolean closed;
        private Throwable originalFailure;
        private Throwable observationFailure;
        private String refusal;

        private NativeEvergaleInteraction(NativeEvergaleImpact.RawImpact var1) {
            this.original = var1;
        }
    }

    private static final class NativeEvergaleSpawnWitness {
        private final RawSpawn spawn;
        private final RawOwnerAssignment owner;
        private final RawCooldown cooldown;

        private NativeEvergaleSpawnWitness(RawSpawn var1, RawOwnerAssignment var2, RawCooldown var3) {
            this.spawn = var1;
            this.owner = var2;
            this.cooldown = var3;
        }
    }

    private static final class NativeFailureRecord {
        private final Object original;
        private final FoodWeaponSourceLedger.ObserverFailures failures = new FoodWeaponSourceLedger.ObserverFailures();
        private Object firstFact;
        private boolean failed;

        private NativeFailureRecord(Object var1) {
            this.original = var1;
        }

        private void fail(Object var1, int var2, Throwable var3) {
            this.failed = true;
            if (this.firstFact == null) {
                this.firstFact = var1;
            }

            this.failures.record(var2, var3);
        }
    }

    private static final class NativeHurtScope {
        private final FoodWeaponSourceLedger.NativeIngressReceipt receipt;
        private final FoodWeaponSourceLedger.NativeCarrierToken carrier;
        private final FoodWeaponSourceLedger.NativeIngressToken earlyIngress;
        private final FoodWeaponSourceLedger.NativeIronCallbackToken ironCallback;
        private final Entity target;
        private final DamageSource source;
        private final ServerPlayer player;
        private final long tick;
        private final Thread thread;
        private final CompoundTag snapshot;
        private final FoodWeaponSourceLedger.WeaponExpectation expectation;
        private FoodWeaponSourceLedger.InteractionRoot interaction;
        private boolean closed;

        private NativeHurtScope(
            FoodWeaponSourceLedger.NativeIngressReceipt var1, FoodWeaponSourceLedger.NativeCarrierToken var2, Entity var3, DamageSource var4, CompoundTag var5
        ) {
            this.receipt = var1;
            this.carrier = var2;
            this.earlyIngress = null;
            this.ironCallback = null;
            this.target = var3;
            this.source = var4;
            this.player = var1.ingress.player;
            this.expectation = var1.ingress.expectation;
            this.tick = FoodWeaponSourceLedger.clock(this.player);
            this.thread = Thread.currentThread();
            this.snapshot = var5.copy();
        }

        private NativeHurtScope(FoodWeaponSourceLedger.NativeIngressToken var1, Entity var2, DamageSource var3, CompoundTag var4) {
            this.receipt = null;
            this.carrier = null;
            this.earlyIngress = var1;
            this.ironCallback = null;
            this.target = var2;
            this.source = var3;
            this.player = var1.player;
            this.expectation = var1.expectation;
            this.tick = FoodWeaponSourceLedger.clock(this.player);
            this.thread = Thread.currentThread();
            this.snapshot = var4.copy();
        }

        private NativeHurtScope(FoodWeaponSourceLedger.NativeIronCallbackToken var1, Entity var2, DamageSource var3, CompoundTag var4) {
            this.receipt = null;
            this.carrier = null;
            this.earlyIngress = null;
            this.ironCallback = var1;
            this.target = var2;
            this.source = var3;
            this.player = var1.payment.cast.use.player;
            this.expectation = var1.expectation;
            this.tick = FoodWeaponSourceLedger.clock(this.player);
            this.thread = Thread.currentThread();
            this.snapshot = var4.copy();
        }
    }

    private static enum NativeIngressKind {
        ENDER("net.gqstavo.armamentarium.item.EnderMaidenShortbowItem", "armamentarium:ender_maiden_shortbow", 1),
        MEAT("com.github.L_Ender.cataclysm.items.Meat_Shredder", "cataclysm:meat_shredder", 0),
        SOUL("com.github.L_Ender.cataclysm.items.Soul_Render", "cataclysm:soul_render", 0),
        TIDAL("com.github.L_Ender.cataclysm.items.Tidal_Claws", "cataclysm:tidal_claws", 0),
        ANCIENT("com.github.L_Ender.cataclysm.items.Ancient_Spear", "cataclysm:ancient_spear", 1),
        CORAL_SPEAR("com.github.L_Ender.cataclysm.items.Coral_Spear", "cataclysm:coral_spear", 1),
        CORAL_BARDICHE("com.github.L_Ender.cataclysm.items.Coral_Bardiche", "cataclysm:coral_bardiche", 1);

        private final String itemClass;
        private final String registryId;
        private final int wear;

        private NativeIngressKind(String var3, String var4, int var5) {
            this.itemClass = var3;
            this.registryId = var4;
            this.wear = var5;
        }
    }

    public static final class NativeIngressReceipt {
        private final FoodWeaponSourceLedger.NativeIngressToken ingress;
        private final CompoundTag image;
        private final String proof;

        private NativeIngressReceipt(FoodWeaponSourceLedger.NativeIngressToken var1, CompoundTag var2, String var3) {
            this.ingress = var1;
            this.image = var2.copy();
            this.proof = var3;
        }
    }

    public static final class NativeIngressToken {
        private final Object repairGeneration;
        private final FoodWeaponSourceLedger.NativeIngressKind kind;
        private final ServerPlayer player;
        private final ItemStack actual;
        private final InteractionHand hand;
        private final FoodWeaponSourceLedger.ServerState state;
        private final UUID ownerEpoch;
        private final UUID action = UUID.randomUUID();
        private final long tick;
        private final Thread thread = Thread.currentThread();
        private final String dimension;
        private final String entry;
        private final String descriptor;
        private final String rules;
        private final CompoundTag before;
        private final FoodWeaponSourceLedger.WeaponExpectation expectation;
        private final List<FoodWeaponSourceLedger.NativeCarrierToken> carriers = new ArrayList<>();
        private FoodWeaponSourceLedger.NativeCapabilityToken capability;
        private int wears;
        private boolean invalid;
        private boolean returned;

        private NativeIngressToken(
            FoodWeaponSourceLedger.NativeIngressKind var1, ServerPlayer var2, ItemStack var3, InteractionHand var4, String var5, String var6
        ) {
            this.kind = var1;
            this.player = var2;
            this.actual = var3;
            this.repairGeneration = OwnerRepairGenerationAuthority.current(var3);
            this.hand = var4;
            this.state = FoodWeaponSourceLedger.state(FoodWeaponSourceLedger.server(var2));
            this.ownerEpoch = FoodWeaponSourceLedger.ensureOwnerState(this.state, var2.getUUID()).epoch;
            this.tick = FoodWeaponSourceLedger.clock(var2);
            this.dimension = var2.level().dimension().location().toString();
            this.entry = var5;
            this.descriptor = var6;
            this.rules = FoodWeaponSourceLedger.foundationRules(var2, var3);
            this.before = NativeFoundationItemData.image(var3).copy();
            this.expectation = new FoodWeaponSourceLedger.WeaponExpectation(var3, this.before);
        }
    }

    public static final class NativeIronCallbackToken {
        private final FoodWeaponSourceLedger.NativeIronPaymentToken payment;
        private final Thread thread = Thread.currentThread();
        private final long tick;
        private final CompoundTag weapon;
        private final FoodWeaponSourceLedger.WeaponExpectation expectation;
        private boolean normal;
        private boolean closed;

        private NativeIronCallbackToken(FoodWeaponSourceLedger.NativeIronPaymentToken var1) {
            this.payment = var1;
            this.tick = FoodWeaponSourceLedger.clock(var1.cast.use.player);
            this.weapon = NativeFoundationItemData.image(var1.cast.use.actual).copy();
            this.expectation = new FoodWeaponSourceLedger.WeaponExpectation(var1.cast.use.actual, this.weapon);
        }
    }

    public static final class NativeIronCastToken {
        private final FoodWeaponSourceLedger.NativeIronUseToken use;
        private final Object spell;
        private final Object castSource;
        private final String source;
        private final String slot;
        private final int selectedLevel;
        private final UUID action = UUID.randomUUID();
        private boolean accepted;
        private boolean invalid;

        private NativeIronCastToken(FoodWeaponSourceLedger.NativeIronUseToken var1, Object var2, Object var3, String var4, int var5) {
            this.use = var1;
            this.spell = var2;
            this.castSource = var3;
            this.source = ((Enum)var3).name();
            this.slot = var4;
            this.selectedLevel = var5;
        }
    }

    public static final class NativeIronPaymentToken {
        private final FoodWeaponSourceLedger.NativeIronCastToken cast;
        private final Object magicData;
        private final Object event;
        private final int level;
        private final int manaCost;
        private final float manaBefore;
        private float manaAfter = Float.NaN;
        private final long tick;
        private boolean mana;
        private boolean callback;
        private boolean returned;
        private boolean invalid;

        private NativeIronPaymentToken(FoodWeaponSourceLedger.NativeIronCastToken var1, Object var2, Object var3, int var4, int var5, float var6) {
            this.cast = var1;
            this.magicData = var2;
            this.event = var3;
            this.level = var4;
            this.manaCost = var5;
            this.manaBefore = var6;
            this.tick = FoodWeaponSourceLedger.clock(var1.use.player);
        }
    }

    public static final class NativeIronReceipt {
        private final FoodWeaponSourceLedger.NativeIronPaymentToken payment;
        private final CompoundTag image;
        private final String proof;

        private NativeIronReceipt(FoodWeaponSourceLedger.NativeIronPaymentToken var1, CompoundTag var2, String var3) {
            this.payment = var1;
            this.image = var2.copy();
            this.proof = var3;
        }
    }

    public static final class NativeIronUseToken {
        private final Object repairGeneration;
        private final ServerPlayer player;
        private final ItemStack actual;
        private final InteractionHand hand;
        private final FoodWeaponSourceLedger.ServerState state;
        private final UUID ownerEpoch;
        private final long tick;
        private final String dimension;
        private final String rules;
        private final Thread thread = Thread.currentThread();
        private boolean invalid;

        private NativeIronUseToken(ServerPlayer var1, ItemStack var2, InteractionHand var3) {
            this.player = var1;
            this.actual = var2;
            this.repairGeneration = OwnerRepairGenerationAuthority.current(var2);
            this.hand = var3;
            this.state = FoodWeaponSourceLedger.state(FoodWeaponSourceLedger.server(var1));
            this.ownerEpoch = FoodWeaponSourceLedger.ensureOwnerState(this.state, var1.getUUID()).epoch;
            this.tick = FoodWeaponSourceLedger.clock(var1);
            this.dimension = var1.level().dimension().location().toString();
            this.rules = FoodWeaponSourceLedger.foundationRules(var1, var2);
        }
    }

    private static final class NativeIssueWindow {
        private final FoodWeaponSourceLedger.NativeIngressToken ingress = FoodWeaponSourceLedger.PRIVATE_INGRESS.get();
        private final FoodWeaponSourceLedger.NativeIronUseToken use = FoodWeaponSourceLedger.PRIVATE_IRON_USE.get();
        private final FoodWeaponSourceLedger.NativeIronCallbackToken callback = FoodWeaponSourceLedger.PRIVATE_IRON_CALLBACK.get();
        private final FoodWeaponSourceLedger.NativeWearToken wear = FoodWeaponSourceLedger.PRIVATE_INGRESS_WEAR.get();
        private final FoodWeaponSourceLedger.NativeHurtScope hurt = FoodWeaponSourceLedger.PRIVATE_NATIVE_HURT.get();
        private final FoodWeaponSourceLedger.NativeOperation operation = FoodWeaponSourceLedger.NATIVE_OPERATION.get();
        private final int depth = FoodWeaponSourceLedger.NATIVE_DEPTH.get();
        private final ServerPlayer player;
        private final FoodWeaponSourceLedger.InteractionRoot projection;
        private final Object ownerChange;
        private final Object rootChange;
        private boolean originalRetired;

        private NativeIssueWindow(ServerPlayer var1) {
            this.player = var1 != null
                ? var1
                : (
                    this.ingress != null
                        ? this.ingress.player
                        : (this.use != null ? this.use.player : (this.callback != null ? this.callback.payment.cast.use.player : null))
                );
            this.projection = FoodWeaponSourceLedger.projectedRootForPlayer(this.player);
            this.ownerChange = this.projection == null ? null : this.projection.ownerState.interactionChange;
            this.rootChange = this.projection == null ? null : this.projection.interactionChange;
        }

        private boolean unchanged() {
            return FoodWeaponSourceLedger.PRIVATE_INGRESS.get() == this.ingress
                && FoodWeaponSourceLedger.PRIVATE_IRON_USE.get() == this.use
                && FoodWeaponSourceLedger.PRIVATE_IRON_CALLBACK.get() == this.callback
                && FoodWeaponSourceLedger.PRIVATE_INGRESS_WEAR.get() == this.wear
                && FoodWeaponSourceLedger.PRIVATE_NATIVE_HURT.get() == this.hurt
                && FoodWeaponSourceLedger.NATIVE_OPERATION.get() == this.operation
                && FoodWeaponSourceLedger.NATIVE_DEPTH.get() == this.depth
                && (
                    this.originalRetired
                        ? FoodWeaponSourceLedger.projectedRootForPlayer(this.player) == null
                        : FoodWeaponSourceLedger.projectedRootForPlayer(this.player) == this.projection
                            && (
                                this.projection == null
                                    || this.projection.ownerState.interactionChange == this.ownerChange && this.projection.interactionChange == this.rootChange
                            )
                );
        }
    }

    public static final class NativeIssuerCustody {
        private final FoodWeaponSourceLedger.IssuerCut cut = new FoodWeaponSourceLedger.IssuerCut();
        private FoodWeaponSourceLedger.NativeIngressToken ingress;
        private FoodWeaponSourceLedger.NativeIronUseToken use;
        private FoodWeaponSourceLedger.NativeIronCallbackToken callback;
        private FoodWeaponSourceLedger.NativeWearToken wear;
        private FoodWeaponSourceLedger.NativeCarrierToken carrier;
        private FoodWeaponSourceLedger.NativeCapabilityToken capability;

        private NativeIssuerCustody() {
        }
    }

    private static enum NativeKind {
        BOW,
        CROSSBOW,
        TRIDENT;
    }

    public static final class NativeMaterialCapture {
        private final FoodWeaponSourceLedger.NativeOperation operation;
        private CompoundTag originalReturnedSnapshot;
        private Throwable originalFailure;
        private OwnerNativeMaterialImage image;
        private boolean failed;

        private NativeMaterialCapture(FoodWeaponSourceLedger.NativeOperation var1) {
            this.operation = var1;
        }
    }

    private static enum NativeMaterialPhase {
        OPEN,
        RETURNED,
        CLAIMED,
        PUBLISHED,
        REJECTED;
    }

    public static final class NativeMaterialPublication {
        private final FoodWeaponSourceLedger.NativeReceipt receipt;
        private final FoodWeaponSourceLedger.NativeMaterialCapture capture;
        private final Entity entity;
        private final CompoundTag receiver;
        private final String key;
        private final CompoundTag originalSnapshot;
        private final CompoundTag originalPutValue;
        private final UUID entityId;
        private FoodWeaponSourceLedger.NativeMaterialTransfer transfer;
        private OwnerNativeMaterialImage image;
        private FoodWeaponSourceLedger.NativeMaterialPhase phase = FoodWeaponSourceLedger.NativeMaterialPhase.OPEN;
        private FoodWeaponSourceLedger.NativePaidWeapon paid;
        private Throwable originalFailure;
        private Throwable observationFailure;

        private NativeMaterialPublication(
            FoodWeaponSourceLedger.NativeReceipt var1,
            FoodWeaponSourceLedger.NativeMaterialCapture var2,
            Entity var3,
            CompoundTag var4,
            String var5,
            CompoundTag var6,
            CompoundTag var7,
            UUID var8
        ) {
            this.receipt = var1;
            this.capture = var2;
            this.entity = var3;
            this.receiver = var4;
            this.key = var5;
            this.originalSnapshot = var6;
            this.originalPutValue = var7;
            this.entityId = var8;
        }
    }

    public static final class NativeMaterialTransfer {
        private final FoodWeaponSourceLedger.NativeReceipt receipt;
        private final FoodWeaponSourceLedger.NativeMaterialCapture capture;
        private final Entity entity;
        private final CompoundTag capturedReturn;
        private CompoundTag pendingReceiver;
        private String pendingKey;
        private CompoundTag pendingCopy;
        private boolean pendingReturned;
        private boolean pendingReadEntered;
        private boolean pendingTaken;
        private boolean launchEntered;
        private CompoundTag launchSource;
        private CompoundTag launchCopy;
        private Throwable originalFailure;
        private Throwable observationFailure;
        private boolean rejected;

        private NativeMaterialTransfer(FoodWeaponSourceLedger.NativeReceipt var1, FoodWeaponSourceLedger.NativeMaterialCapture var2, Entity var3) {
            this.receipt = var1;
            this.capture = var2;
            this.entity = var3;
            this.capturedReturn = var2.originalReturnedSnapshot;
        }
    }

    public static final class NativeObservationCut {
        private final Object receiver;
        private NativeObservationFrame frame;
        private boolean frameBound;
        private final Thread thread;
        private final FoodWeaponSourceLedger.NativeFailureRecord failure;
        private FoodWeaponSourceLedger.NativeRootObservation roots;

        private NativeObservationCut(Object var1, NativeObservationFrame var2) {
            this.receiver = var1;
            this.frame = var2;
            this.thread = Thread.currentThread();
            this.failure = new FoodWeaponSourceLedger.NativeFailureRecord(var1);
        }
    }

    private static final class NativeOperation {
        private final Object repairGeneration;
        private final FoodWeaponSourceLedger.NativeKind kind;
        private final ServerPlayer player;
        private final ItemStack weapon;
        private final InteractionHand hand;
        private final MinecraftServer server;
        private final UUID epoch;
        private final UUID ownerEpoch;
        private final Thread thread = Thread.currentThread();
        private final long tick;
        private final String dimension;
        private final List<FoodWeaponSourceLedger.NativeReceipt> receipts = new ArrayList<>();
        private FoodWeaponSourceLedger.NativeReceipt current;
        private AbstractArrow factoryEntity;
        private boolean adding;
        private boolean invalid;
        private FoodWeaponSourceLedger.NativeMaterialCapture materialCapture;
        private boolean materialAttempted;
        private boolean materialRejected;

        private NativeOperation(FoodWeaponSourceLedger.NativeKind var1, ServerPlayer var2, ItemStack var3, InteractionHand var4) {
            this.kind = var1;
            this.player = var2;
            this.weapon = var3;
            this.repairGeneration = OwnerRepairGenerationAuthority.current(var3);
            this.hand = var4;
            this.server = FoodWeaponSourceLedger.server(var2);
            this.epoch = FoodWeaponSourceLedger.state(this.server).epoch;
            this.ownerEpoch = FoodWeaponSourceLedger.ownerEpoch(FoodWeaponSourceLedger.state(this.server), var2.getUUID());
            this.tick = FoodWeaponSourceLedger.clock(var2);
            this.dimension = var2.level().dimension().location().toString();
        }
    }

    public static final class NativePaidWeapon {
        private final FoodWeaponSourceLedger.NativeReceipt receipt;
        private final Entity carrier;
        private final FoodWeaponSourceLedger.WeaponExpectation expectation;
        private FoodWeaponSourceLedger.NativeMaterialPublication material;
        private OwnerNativeMaterialImage combined;

        private NativePaidWeapon(FoodWeaponSourceLedger.NativeReceipt var1, CompoundTag var2) {
            this.receipt = var1;
            this.carrier = var1.entity;
            this.expectation = new FoodWeaponSourceLedger.WeaponExpectation(var1.operation.weapon, var2);
        }

        private NativePaidWeapon(
            FoodWeaponSourceLedger.NativeReceipt var1, CompoundTag var2, FoodWeaponSourceLedger.NativeMaterialPublication var3, OwnerNativeMaterialImage var4
        ) {
            this(var1, var2);
            this.material = var3;
            this.combined = var4;
        }
    }

    static final class NativeProjectileInteraction {
        private final NativeVanillaImpact.RawImpact call;
        private final FoodWeaponSourceLedger.InteractionRoot root;
        private final FoodWeaponSourceLedger.OriginalInteractionBinding binding;
        private final FoodWeaponSourceLedger.NativeProjectileObservation observation;
        private boolean closed;

        private NativeProjectileInteraction(NativeVanillaImpact.RawImpact var1, FoodWeaponSourceLedger.InteractionRoot var2) {
            this.call = var1;
            this.root = var2;
            this.binding = var2.current;
            this.observation = null;
        }

        private NativeProjectileInteraction(
            NativeVanillaImpact.RawImpact var1, FoodWeaponSourceLedger.InteractionRoot var2, FoodWeaponSourceLedger.NativeProjectileObservation var3
        ) {
            this.call = var1;
            this.root = var2;
            this.binding = var2.current;
            this.observation = var3;
        }
    }

    static final class NativeProjectileObservation {
        private final NativeVanillaImpact.RawImpact call;
        private final FoodWeaponSourceLedger.NativeFailureRecord failure;
        private FoodWeaponSourceLedger.InteractionRoot root;
        private volatile FoodWeaponSourceLedger.WeaponExpectation expectation;
        private FoodWeaponSourceLedger.OwnerWriterPublication publication;
        private FoodWeaponSourceLedger.OriginalInteractionBinding binding;
        private FoodWeaponSourceLedger.NativeProjectileInteraction scope;
        private NativeObservationFrame frame;
        private Throwable openingFailure;
        private Throwable closingFailure;
        private boolean begun;
        private boolean finished;

        private NativeProjectileObservation(NativeVanillaImpact.RawImpact var1) {
            this.call = var1;
            this.failure = new FoodWeaponSourceLedger.NativeFailureRecord(var1);
        }
    }

    private static final class NativeReceipt {
        private final UUID token = UUID.randomUUID();
        private final FoodWeaponSourceLedger.NativeOperation operation;
        private final CompoundTag before;
        private final String rules;
        private Entity entity;
        private CompoundTag after;
        private CompoundTag unsigned;
        private int requestedDamage = -1;
        private int writes;
        private boolean wearing;
        private boolean worn;
        private boolean added;
        private boolean consumed;
        private FoodWeaponSourceLedger.NativeMaterialPublication material;
        private boolean materialAttempted;
        private boolean materialRejected;
        private FoodWeaponSourceLedger.NativeMaterialTransfer materialTransfer;
        private boolean profileFieldInitialized;
        private boolean imageFieldInitialized;
        private boolean manorOwnerFieldInitialized;

        private NativeReceipt(FoodWeaponSourceLedger.NativeOperation var1, Entity var2) {
            this.operation = var1;
            this.entity = var2;
            this.before = NativeFoundationItemData.image(var1.weapon).copy();
            this.rules = FoodWeaponSourceLedger.foundationRules(var1.player, var1.weapon);
        }
    }

    private static final class NativeRootObservation {
        private final FoodWeaponSourceLedger.NativeObservationCut cut;
        private final FoodWeaponSourceLedger.InteractionRoot root;
        private final FoodWeaponSourceLedger.WeaponExpectation expectation;
        private final FoodWeaponSourceLedger.OwnerWriterPublication publication;
        private final FoodWeaponSourceLedger.OriginalInteractionBinding binding;
        private final FoodWeaponSourceLedger.NativeRootObservation nextCut;
        private final FoodWeaponSourceLedger.NativeRootObservation nextRoot;

        private NativeRootObservation(FoodWeaponSourceLedger.NativeObservationCut var1, FoodWeaponSourceLedger.InteractionRoot var2) {
            this.cut = var1;
            this.root = var2;
            this.expectation = var2.expectation;
            this.publication = var2.writerPublication;
            this.binding = var2.current;
            this.nextCut = var1.roots;
            this.nextRoot = var2.nativeObservations;
        }
    }

    public static final class NativeWearToken {
        private final FoodWeaponSourceLedger.NativeIngressToken ingress;
        private final CompoundTag before;
        private final String rules;
        private int writes;
        private int requested = -1;
        private boolean returned;

        private NativeWearToken(FoodWeaponSourceLedger.NativeIngressToken var1) {
            this.ingress = var1;
            this.before = NativeFoundationItemData.image(var1.actual).copy();
            this.rules = FoodWeaponSourceLedger.foundationRules(var1.player, var1.actual);
        }
    }

    private static final class ObserverFailures {
        private final Throwable[] phases = new Throwable[4];
        private Throwable original;
        private boolean closed;

        private void record(int var1, Throwable var2) {
            if (var1 >= 0 && var1 < this.phases.length && var2 != null && this.phases[var1] == null) {
                this.phases[var1] = var2;
            }
        }

        private Throwable failure(int var1) {
            return var1 >= 0 && var1 < this.phases.length ? this.phases[var1] : null;
        }

        private void original(Throwable var1) {
            if (this.original == null) {
                this.original = var1;
            }
        }

        private void close() {
            this.closed = true;
        }
    }

    static final class OriginalInteractionBinding {
        private final FoodWeaponSourceLedger.InteractionRoot root;
        private final FoodWeaponSourceLedger.OriginalInteractionBinding parent;
        private final int depth;
        private final DamageSource source;
        private final Entity target;
        private final CompoundTag snapshot;
        private final int rawAmountBits;
        private final FoodWeaponFinalDamage.ChildHurtCall childCall;
        private boolean closed;
        private FoodWeaponSourceLedger.OwnerWriterPublication acceptedPublication;
        private FoodWeaponSourceLedger.FinalListedChildScope acceptedChildScope;

        private OriginalInteractionBinding(
            FoodWeaponSourceLedger.InteractionRoot var1,
            FoodWeaponSourceLedger.OriginalInteractionBinding var2,
            DamageSource var3,
            Entity var4,
            CompoundTag var5,
            float var6,
            FoodWeaponFinalDamage.ChildHurtCall var7
        ) {
            this.root = var1;
            this.parent = var2;
            this.depth = var2 == null ? 1 : var2.depth + 1;
            this.source = var3;
            this.target = var4;
            this.snapshot = var5 == null ? null : var5.copy();
            this.rawAmountBits = Float.floatToRawIntBits(var6);
            this.childCall = var7;
        }
    }

    public static final class OriginalRepairCall {
        private final Object commit;
        private final Object bridge;
        private final Object leaf;
        private final ItemStack actual;
        private final FoodWeaponHitPlan.OriginalRepairFacts facts;
        private final OwnerFullRepairGuard.Cut cut;
        private final Thread thread = Thread.currentThread();
        private Value getterFields;
        private int getterValue;
        private int phase;
        private boolean closed;

        private OriginalRepairCall(
            Object var1, Object var2, Object var3, ItemStack var4, FoodWeaponHitPlan.OriginalRepairFacts var5, OwnerFullRepairGuard.Cut var6
        ) {
            this.commit = var1;
            this.bridge = var2;
            this.leaf = var3;
            this.actual = var4;
            this.facts = var5;
            this.cut = var6;
        }
    }

    private static final class OwnerFieldSequenceBinding {
        private final FoodWeaponSourceLedger.OwnerWriterPublication publication;
        private final FoodWeaponOriginalSequence.Session sequence;
        private final OwnerFieldExpectedAuthority.Prepared prepared;
        private final Thread thread = Thread.currentThread();
        private final FoodWeaponSourceLedger.WriterSelection selection;
        private boolean extraAuthority;
        private boolean terminal;

        private OwnerFieldSequenceBinding(
            FoodWeaponSourceLedger.OwnerWriterPublication var1, FoodWeaponOriginalSequence.Session var2, OwnerFieldExpectedAuthority.Prepared var3
        ) {
            this.publication = var1;
            this.sequence = var2;
            this.prepared = var3;
            this.selection = var1.selection;
        }
    }

    public static final class OwnerPlayerStep {
        private final FoodWeaponSourceLedger.OwnerPlayerWriter writer;
        private final FoodWeaponSourceLedger.WriterSelection selection;
        private final FoodWeaponSourceLedger.PlayerImageDelta delta;
        private final FoodWeaponSourceLedger.PlayerWriterArgument argument;
        private final CompoundTag receiver;
        private final Object position;
        private final byte[] before;
        private final byte[] planned;
        private final FoodWeaponSourceLedger.ObserverFailures observation = new FoodWeaponSourceLedger.ObserverFailures();
        private boolean entered;
        private boolean closed;

        private OwnerPlayerStep(FoodWeaponSourceLedger.OwnerPlayerWriter var1, FoodWeaponSourceLedger.PlayerImageDelta var2) {
            this(
                var1,
                var2,
                new FoodWeaponSourceLedger.PlayerWriterArgument(
                    var2.rootWrite ? FoodWeaponSourceLedger.PlayerWriterArgumentKind.ROOT : FoodWeaponSourceLedger.PlayerWriterArgumentKind.TAG,
                    (Tag)(var2.rootWrite ? var1.target : var2.value),
                    null,
                    null,
                    0L
                ),
                var2.rootWrite ? var1.persistent : var1.target,
                var1.selection
            );
        }

        private OwnerPlayerStep(
            FoodWeaponSourceLedger.OwnerPlayerWriter var1,
            FoodWeaponSourceLedger.PlayerImageDelta var2,
            FoodWeaponSourceLedger.PlayerWriterArgument var3,
            CompoundTag var4,
            FoodWeaponSourceLedger.WriterSelection var5
        ) {
            this.writer = var1;
            this.selection = var5;
            this.delta = var2;
            this.argument = var3;
            this.receiver = var4;
            this.position = var1.cursor.position();
            this.before = FoodWeaponSourceLedger.canonical(var1.plan.image());
            this.planned = FoodWeaponSourceLedger.canonical(var1.plan.image(var2.persistent, var2.target));
        }
    }

    public static final class OwnerPlayerWriter {
        private final FoodWeaponSourceLedger.OwnerWriterPublication publication;
        private final FoodWeaponSourceLedger.WriterSelection selection;
        private final FoodWeaponSourceLedger.ObserverFailures observation = new FoodWeaponSourceLedger.ObserverFailures();
        private final CompoundTag persistent;
        private final CompoundTag target;
        private final CompoundTag source;
        private final FoodWeaponSourceLedger.PlayerImagePlan plan;
        private final FoodWeaponSourceLedger.ImageCursor cursor;
        private final Object transition;
        private FoodSocketPowers.PreparedPack packPrepared;
        private FoodWeaponFinalDamage.PublishingCleanup packCleanup;
        private FoodWeaponSourceLedger.OwnerPlayerStep active;
        private boolean closed;

        private OwnerPlayerWriter(
            FoodWeaponSourceLedger.OwnerWriterPublication var1,
            CompoundTag var2,
            CompoundTag var3,
            CompoundTag var4,
            FoodWeaponSourceLedger.PlayerImagePlan var5
        ) {
            this(var1, var2, var3, var4, var5, var1.selection);
        }

        private OwnerPlayerWriter(
            FoodWeaponSourceLedger.OwnerWriterPublication var1,
            CompoundTag var2,
            CompoundTag var3,
            CompoundTag var4,
            FoodWeaponSourceLedger.PlayerImagePlan var5,
            FoodWeaponSourceLedger.WriterSelection var6
        ) {
            this.publication = var1;
            this.selection = var6;
            this.persistent = var2;
            this.target = var3;
            this.source = var4;
            this.plan = var5;
            this.cursor = new FoodWeaponSourceLedger.ImageCursor(FoodWeaponSourceLedger.canonical(var5.image()));
            this.transition = var1.binding.root.interactionChange;
        }
    }

    public static final class OwnerRepairObservation {
        private final FoodWeaponSourceLedger.OwnerWriterPublication publication;
        private final FoodWeaponSourceLedger.WriterSelection selection;
        private final ItemStack actual;
        private final FoodWeaponSourceLedger.WriterPlanningCut planning;
        private final FoodWeaponSourceLedger.RepairOriginalTrace trace;
        private final CompoundTag expectedBefore;
        private final Tag expectedForgeCaps;
        private final FoodWeaponSourceLedger.ObserverFailures observation = new FoodWeaponSourceLedger.ObserverFailures();
        private boolean closed;

        private OwnerRepairObservation(
            FoodWeaponSourceLedger.OwnerWriterPublication var1, FoodWeaponSourceLedger.WriterPlanningCut var2, ItemStack var3, int var4, int var5, int var6
        ) {
            this.publication = var1;
            this.selection = var2.selection;
            this.actual = var3;
            this.planning = var2;
            this.trace = new FoodWeaponSourceLedger.RepairOriginalTrace(var4, var5, var6);
            this.expectedBefore = var1.expectation.expected.copy();
            this.expectedForgeCaps = this.expectedBefore.get("ForgeCaps");
        }
    }

    private static final class OwnerState {
        private final UUID epoch;
        private volatile FoodWeaponSourceLedger.InteractionRoot current;
        private boolean closed;
        private volatile Object interactionChange = new Object();

        private OwnerState(UUID var1) {
            this.epoch = var1;
        }
    }

    static enum OwnerWriterKind {
        EVOLUTION_TIMER,
        WOLF_TOKEN;
    }

    static final class OwnerWriterPublication {
        private final FoodWeaponSourceLedger.OriginalInteractionBinding binding;
        private final FoodWeaponSourceLedger.AttemptBatchQuote batch;
        private final FoodWeaponSourceLedger.AttemptBatchAdmission admission;
        private final FoodWeaponSourceLedger.AttemptBatchCommit result;
        private final FoodWeaponFinalDamage.OffenseReceipt receipt;
        private final FoodWeaponSourceLedger.WeaponExpectation expectation;
        private final Thread thread;
        private final FoodWeaponSourceLedger.ObserverFailures observation = new FoodWeaponSourceLedger.ObserverFailures();
        private int producerOrdinal = -1;
        private int leafOrdinal = -1;
        private int stepOrdinal = -1;
        private FoodWeaponSourceLedger.WriterSelection selection;
        private Object producer;
        private Object commit;
        private Object leaf;
        private FoodWeaponSourceLedger.OwnerPlayerWriter playerWriter;
        private FoodWeaponSourceLedger.OwnerRepairObservation repairObservation;
        private FoodWeaponSourceLedger.OwnerWriterStep failedWriterStep;
        private FoodWeaponSourceLedger.OwnerPlayerStep failedPlayerStep;
        private FoodWeaponSourceLedger.OwnerPlayerWriter failedPlayerWriter;
        private FoodWeaponSourceLedger.OwnerRepairObservation failedRepairObservation;
        private final FoodWeaponSourceLedger.OwnerWriterPublication parentPublication;
        private boolean fieldAdmissionAttempted;
        private boolean fullRepairAttempted;
        private FoodWeaponSourceLedger.OwnerFieldSequenceBinding fieldSequenceBinding;
        private OwnerFieldExpectedAuthority.Prepared fieldPreparation;
        private final long fieldGeneration = FoodWeaponSourceLedger.nextOwnerFieldGeneration();
        private boolean closed;

        private OwnerWriterPublication(
            FoodWeaponSourceLedger.OriginalInteractionBinding var1,
            FoodWeaponSourceLedger.AttemptBatchQuote var2,
            FoodWeaponSourceLedger.AttemptBatchAdmission var3,
            FoodWeaponSourceLedger.AttemptBatchCommit var4,
            FoodWeaponFinalDamage.OffenseReceipt var5
        ) {
            this.binding = var1;
            this.parentPublication = var1.root.writerPublication;
            var1.acceptedPublication = this;
            this.batch = var2;
            this.admission = var3;
            this.result = var4;
            this.receipt = var5;
            this.expectation = var1.root.expectation;
            this.thread = Thread.currentThread();
        }
    }

    public static final class OwnerWriterStep {
        private final FoodWeaponSourceLedger.OwnerWriterPublication publication;
        private final FoodWeaponSourceLedger.OwnerWriterKind kind;
        private final FoodWeaponSourceLedger.WriterSelection selection;
        private final UUID originalUuid;
        private final int ordinal;
        private final Object position;
        private final Object transition;
        private final CompoundTag planned;
        private final byte[] before;
        private final byte[] entryImage;
        private final byte[] after;
        private final String key;
        private final Tag value;
        private CompoundTag receiver;
        private final FoodWeaponSourceLedger.ObserverFailures observation = new FoodWeaponSourceLedger.ObserverFailures();
        private boolean entered;
        private boolean closed;

        private OwnerWriterStep(
            FoodWeaponSourceLedger.OwnerWriterPublication var1,
            FoodWeaponSourceLedger.OwnerWriterKind var2,
            CompoundTag var3,
            CompoundTag var4,
            String var5,
            Tag var6
        ) {
            this(var1, var2, var3, var4, var5, var6, null, var1.selection);
        }

        private OwnerWriterStep(
            FoodWeaponSourceLedger.OwnerWriterPublication var1,
            FoodWeaponSourceLedger.OwnerWriterKind var2,
            CompoundTag var3,
            CompoundTag var4,
            String var5,
            Tag var6,
            UUID var7,
            FoodWeaponSourceLedger.WriterSelection var8
        ) {
            this.publication = var1;
            this.selection = var8;
            this.originalUuid = var7;
            this.ordinal = var1.stepOrdinal + 1;
            this.kind = var2;
            this.position = var1.expectation.cursor.position();
            this.transition = var1.binding.root.interactionChange;
            this.planned = var3;
            this.before = FoodWeaponSourceLedger.canonical(var1.expectation.expected);
            this.entryImage = FoodWeaponSourceLedger.canonical(var4);
            this.after = FoodWeaponSourceLedger.canonical(var3);
            this.key = var5;
            this.value = var6 == null ? null : var6.copy();
        }
    }

    static final class PendingEchoInteraction {
        private final FoodSocketAttributePowers.PendingEchoDispatch dispatch;
        private final FoodWeaponSourceLedger.PendingEchoLineage lineage;
        private final FoodWeaponSourceLedger.InteractionRoot root;
        private final FoodWeaponSourceLedger.OriginalInteractionBinding binding;
        private final ServerPlayer player;
        private final LivingEntity target;
        private final DamageSource source;
        private final float rawAmount;
        private boolean hurtEntered;
        private boolean hurtActive;
        private boolean hurtFinished;
        private boolean closed;

        private PendingEchoInteraction(
            FoodSocketAttributePowers.PendingEchoDispatch var1,
            FoodWeaponSourceLedger.InteractionRoot var2,
            ServerPlayer var3,
            LivingEntity var4,
            DamageSource var5,
            float var6
        ) {
            this(var1, var2, var3, var4, var5, var6, null);
        }

        private PendingEchoInteraction(
            FoodSocketAttributePowers.PendingEchoDispatch var1,
            FoodWeaponSourceLedger.InteractionRoot var2,
            ServerPlayer var3,
            LivingEntity var4,
            DamageSource var5,
            float var6,
            FoodWeaponSourceLedger.PendingEchoLineage var7
        ) {
            this.dispatch = var1;
            this.lineage = var7;
            this.root = var2;
            this.binding = var2.current;
            this.player = var3;
            this.target = var4;
            this.source = var5;
            this.rawAmount = var6;
        }
    }

    static final class PendingEchoLineage {
        private final Entry<?> offeredEntry;
        private final MinecraftServer server;
        private final ItemStack originalActual;
        private final CompoundTag snapshot;
        private final UUID target;
        private final long offeredAt;
        private final long dueAt;
        private final double rangeSquared;
        private final String purpose;
        private boolean dispatched;

        private PendingEchoLineage(Entry<?> var1, MinecraftServer var2, CompoundTag var3, UUID var4, long var5, long var7, double var9) {
            this(var1, var2, var3, var4, var5, var7, var9, null);
        }

        private PendingEchoLineage(Entry<?> var1, MinecraftServer var2, CompoundTag var3, UUID var4, long var5, long var7, double var9, ItemStack var11) {
            this.offeredEntry = var1;
            this.server = var2;
            this.originalActual = var11;
            this.snapshot = var3.copy();
            this.target = var4;
            this.offeredAt = var5;
            this.dueAt = var7;
            this.rangeSquared = var9;
            this.purpose = var3.getString("DamagePurpose");
        }
    }

    private static final class PlayerImageDelta {
        private final FoodWeaponSourceLedger.PlayerImagePlan plan;
        private final int ordinal;
        private final CompoundTag persistent;
        private final CompoundTag target;
        private final String key;
        private final Tag value;
        private final boolean rootWrite;

        private PlayerImageDelta(FoodWeaponSourceLedger.PlayerImagePlan var1, CompoundTag var2, CompoundTag var3, String var4, Tag var5, boolean var6) {
            this.plan = var1;
            this.ordinal = var1.next;
            this.persistent = var2;
            this.target = var3;
            this.key = var4;
            this.value = var5.copy();
            this.rootWrite = var6;
        }
    }

    private static final class PlayerImagePlan {
        private static final List<String> PACK_KEYS = List.of(
            "PackToken", "PackWeapon", "PackWeaponId", "PackHand", "PackDimension", "PackFingerprint", "PackSource", "PackUntil", "PackCooldown"
        );
        private final boolean wolf;
        private final String rootKey;
        private final boolean sourceAtTarget;
        private final boolean sourceAtPersistent;
        private final CompoundTag source;
        private final CompoundTag wolfValues;
        private CompoundTag persistent;
        private CompoundTag target;
        private boolean attached;
        private boolean published;
        private int next;

        private PlayerImagePlan(boolean var1, CompoundTag var2, CompoundTag var3, CompoundTag var4, boolean var5, boolean var6, boolean var7, CompoundTag var8) {
            this.wolf = var1;
            this.rootKey = var1 ? "FTBossFoodSocketPowers" : "FTBossRangedProcTimers";
            this.persistent = var2.copy();
            this.target = var3.copy();
            this.source = var4.copy();
            this.attached = var5;
            this.sourceAtTarget = var6;
            this.sourceAtPersistent = var7;
            this.wolfValues = var8 == null ? null : var8.copy();
        }

        private CompoundTag image(CompoundTag var1, CompoundTag var2) {
            CompoundTag var3 = new CompoundTag();
            var3.put("Persistent", var1.copy());
            var3.put("Target", var2.copy());
            var3.put("Source", (this.sourceAtTarget ? var2 : (this.sourceAtPersistent ? var1 : this.source)).copy());
            return var3;
        }

        private CompoundTag image() {
            return this.image(this.persistent, this.target);
        }

        private FoodWeaponSourceLedger.PlayerImageDelta key(int var1, String var2) {
            if (!this.published && var1 == this.next && var2 != null) {
                Tag var3;
                if (this.wolf) {
                    if (this.next >= PACK_KEYS.size() || !PACK_KEYS.get(this.next).equals(var2)) {
                        return null;
                    }

                    var3 = this.wolfValues.get(var2);
                } else {
                    CompoundTag var4 = this.sourceAtTarget ? this.target : (this.sourceAtPersistent ? this.persistent : this.source);
                    if (!var2.startsWith("Last") || !var4.contains(var2, 4)) {
                        return null;
                    }

                    var3 = LongTag.valueOf(Math.max(var4.getLong(var2), this.target.getLong(var2)));
                }

                if (var3 == null) {
                    return null;
                } else {
                    CompoundTag var6 = this.target.copy();
                    var6.put(var2, var3.copy());
                    CompoundTag var5 = this.persistent.copy();
                    if (this.attached) {
                        var5.put(this.rootKey, var6.copy());
                    }

                    return new FoodWeaponSourceLedger.PlayerImageDelta(this, var5, var6, var2, (Tag)var3, false);
                }
            } else {
                return null;
            }
        }

        private FoodWeaponSourceLedger.PlayerImageDelta root(int var1) {
            if (!this.published && var1 == this.next && (!this.wolf || this.next == PACK_KEYS.size())) {
                CompoundTag var2 = this.persistent.copy();
                var2.put(this.rootKey, this.target.copy());
                return new FoodWeaponSourceLedger.PlayerImageDelta(this, var2, this.target.copy(), this.rootKey, this.target, true);
            } else {
                return null;
            }
        }

        private boolean accept(FoodWeaponSourceLedger.PlayerImageDelta var1) {
            if (var1 != null && var1.plan == this && !this.published && var1.ordinal == this.next) {
                this.persistent = var1.persistent;
                this.target = var1.target;
                this.next++;
                if (var1.rootWrite) {
                    this.attached = true;
                    this.published = true;
                }

                return true;
            } else {
                return false;
            }
        }
    }

    private static final class PlayerWriterArgument {
        private final FoodWeaponSourceLedger.PlayerWriterArgumentKind kind;
        private final Tag tag;
        private final UUID uuid;
        private final String string;
        private final long number;

        private PlayerWriterArgument(FoodWeaponSourceLedger.PlayerWriterArgumentKind var1, Tag var2, UUID var3, String var4, long var5) {
            this.kind = var1;
            this.tag = var2;
            this.uuid = var3;
            this.string = var4;
            this.number = var5;
        }

        private boolean expected(Tag var1) {
            if (var1 == null) {
                return false;
            } else {
                return switch (this.kind) {
                    case TAG, ROOT -> this.tag != null && Arrays.equals(FoodWeaponSourceLedger.canonical(var1), FoodWeaponSourceLedger.canonical(this.tag));
                    case UUID -> this.uuid != null
                    && var1 instanceof IntArrayTag
                    && Arrays.equals(FoodWeaponSourceLedger.canonical(var1), FoodWeaponSourceLedger.canonical(NbtUtils.createUUID(this.uuid)));
                    case STRING -> this.string != null && var1 instanceof StringTag && this.string.equals(var1.getAsString());
                    case LONG -> {
                        if (var1 instanceof LongTag var2 && var2.getAsLong() == this.number) {
                            yield true;
                        }

                        yield false;
                    }
                };
            }
        }

        private boolean originalTag(Tag var1) {
            return (this.kind == FoodWeaponSourceLedger.PlayerWriterArgumentKind.TAG || this.kind == FoodWeaponSourceLedger.PlayerWriterArgumentKind.ROOT)
                && var1 != null
                && var1 == this.tag;
        }

        private boolean originalUuid(UUID var1) {
            return this.kind == FoodWeaponSourceLedger.PlayerWriterArgumentKind.UUID && var1 != null && var1 == this.uuid;
        }

        private boolean originalString(String var1) {
            return this.kind == FoodWeaponSourceLedger.PlayerWriterArgumentKind.STRING && this.string != null && this.string.equals(var1);
        }

        private boolean originalLong(long var1) {
            return this.kind == FoodWeaponSourceLedger.PlayerWriterArgumentKind.LONG && var1 == this.number;
        }
    }

    private static enum PlayerWriterArgumentKind {
        TAG,
        UUID,
        STRING,
        LONG,
        ROOT;
    }

    private static final class PrimaryReadWindow {
        private final Object repairActual;
        private final Object repairGeneration;
        private final OwnerLineageRevocationStore.Lease repairLease;
        private final FoodWeaponSourceLedger.InteractionRoot root;
        private final FoodWeaponSourceLedger.OriginalInteractionBinding binding;
        private final Object transition;

        private PrimaryReadWindow(FoodWeaponSourceLedger.InteractionRoot var1) {
            this.root = var1;
            this.binding = var1.current;
            this.transition = var1.readCut.begin();
            this.repairActual = var1.expectation == null ? null : var1.expectation.actual;
            this.repairGeneration = var1.expectation == null ? null : var1.expectation.repairGeneration;
            this.repairLease = OwnerFullRepairGuard.lease(this.repairActual);
        }
    }

    private static final class ReadAliases {
        private static final int LIMIT = 256;
        private final Object[] originals = new Object[256];
        private int count;
        private boolean saturated;

        private boolean contains(Object var1) {
            if (var1 == null) {
                return false;
            } else {
                for (int var2 = 0; var2 < this.count; var2++) {
                    if (this.originals[var2] == var1) {
                        return true;
                    }
                }

                return false;
            }
        }

        private boolean bind(Object var1) {
            if (var1 == null || this.saturated) {
                return false;
            } else if (this.contains(var1)) {
                return true;
            } else if (this.count == 256) {
                this.saturated = true;
                return false;
            } else {
                this.originals[this.count++] = var1;
                return true;
            }
        }

        private void clear() {
            for (int var1 = 0; var1 < this.count; var1++) {
                this.originals[var1] = null;
            }

            this.count = 0;
        }
    }

    private static final class ReadCut {
        private Object transition = new FoodWeaponSourceLedger.ReadGeneration();
        private Object acceptedImage;
        private volatile boolean closed;

        private Object begin() {
            if (this.closed) {
                return this.transition;
            } else {
                this.acceptedImage = null;
                this.transition = new FoodWeaponSourceLedger.ReadGeneration();
                return this.transition;
            }
        }

        private void change() {
            ((FoodWeaponSourceLedger.ReadGeneration)this.transition).invalid = true;
            this.acceptedImage = null;
        }

        private boolean accept(Object var1, Object var2) {
            if (!this.closed && var1 == this.transition && !((FoodWeaponSourceLedger.ReadGeneration)this.transition).invalid && var2 != null) {
                this.acceptedImage = var2;
                return true;
            } else {
                return false;
            }
        }

        private boolean matches(Object var1) {
            return !this.closed && !((FoodWeaponSourceLedger.ReadGeneration)this.transition).invalid && var1 != null && this.acceptedImage == var1;
        }

        private void close() {
            this.closed = true;
            this.acceptedImage = null;
            ((FoodWeaponSourceLedger.ReadGeneration)this.transition).invalid = true;
        }
    }

    private static final class ReadGeneration {
        private boolean invalid;
    }

    private static final class RepairOriginalTrace {
        private final int getterResult;
        private final int reduction;
        private final int requested;
        private int phase = -1;
        private final Object[] receivers = new Object[8];
        private final int[] arguments = new int[8];
        private boolean closed;

        private RepairOriginalTrace(int var1, int var2, int var3) {
            this.getterResult = var1;
            this.reduction = var2;
            this.requested = var3;
        }

        private boolean originalRequest() {
            return this.requested == Math.max(0, this.getterResult - this.reduction);
        }

        private boolean record(int var1, Object var2, int var3) {
            if (!this.closed && this.originalRequest() && var1 == this.phase + 1 && var1 < this.receivers.length && var2 != null) {
                this.receivers[var1] = var2;
                this.arguments[var1] = var3;
                this.phase = var1;
                return true;
            } else {
                return false;
            }
        }

        private void close() {
            this.closed = true;
        }
    }

    private static enum RootSite {
        GENERIC_CAST,
        NATIVE_CARRIER_IMPACT,
        NATIVE_MEAT_IMPACT,
        NATIVE_IRON_IMPACT,
        DELAYED_ECHO,
        NATIVE_BOW_IMPACT,
        NATIVE_CROSSBOW_IMPACT,
        NATIVE_TRIDENT_IMPACT;
    }

    private static final class ServerState {
        private final UUID epoch = UUID.randomUUID();
        private final byte[] signingKey = new byte[32];
        private final Map<UUID, FoodWeaponSourceLedger.OwnerState> ownerEpochs = Collections.synchronizedMap(new LinkedHashMap<>());
        private volatile Map<FoodWeaponSourceLedger.Claim, FoodWeaponSourceLedger.Commit> claims = new LinkedHashMap<>();
        private volatile long latestTick = Long.MIN_VALUE;
        private volatile boolean closed;
        private volatile OwnerLineageRevocationStore revocations;
        private boolean storeInitialized;

        private ServerState(MinecraftServer var1) {
            new SecureRandom().nextBytes(this.signingKey);
        }

        private void initializeStore(MinecraftServer var1) {
            synchronized (this) {
                if (!this.storeInitialized) {
                    OwnerLineageRevocationStore var3;
                    try {
                        var3 = OwnerLineageRevocationStore.open(var1);
                    } catch (IOException var6) {
                        var3 = null;
                    }

                    this.revocations = var3;
                    this.storeInitialized = true;
                }
            }
        }
    }

    public static record SourceTicket(
        UUID owner,
        UUID weaponId,
        InteractionHand originalHand,
        String profile,
        String repairProfile,
        String socketFingerprint,
        String materialFingerprint,
        UUID actionId,
        String dimension,
        long issuedAt,
        long expiresAt,
        UUID serverEpoch,
        UUID ownerEpoch,
        int preWearDamage,
        int maxDamage
    ) {
    }

    public static enum TargetCommit {
        ALREADY_COMMITTED,
        COMMITTED;
    }

    public static enum TargetEffectDomain {
        MATERIAL_DAMAGE,
        BLESSING_EXECUTE,
        BLESSING_FORTUNE,
        BLESSING_UNDERTOW,
        BLESSING_ISOLATION,
        BLESSING_HUNT,
        EVOLUTION_DAMAGE,
        EVOLUTION_KNOCKBACK,
        EVOLUTION_POISON,
        EVOLUTION_SLOW,
        EVOLUTION_WITHER,
        EVOLUTION_FIRE,
        EVOLUTION_WEAKEN;
    }

    public static final class TargetQuote {
        private final UUID target;
        private final FoodWeaponSourceLedger.TargetEffectDomain domain;
        private final FoodWeaponSourceLedger.TargetStatus status;

        private TargetQuote(UUID var1, FoodWeaponSourceLedger.TargetEffectDomain var2, FoodWeaponSourceLedger.TargetStatus var3) {
            this.target = var1;
            this.domain = var2;
            this.status = var3;
        }

        public UUID target() {
            return this.target;
        }

        public FoodWeaponSourceLedger.TargetEffectDomain domain() {
            return this.domain;
        }

        public FoodWeaponSourceLedger.TargetStatus status() {
            return this.status;
        }
    }

    public static record TargetRequest(UUID target, FoodWeaponSourceLedger.TargetEffectDomain domain) {
    }

    public static final class TargetResult {
        private final UUID target;
        private final FoodWeaponSourceLedger.TargetEffectDomain domain;
        private final FoodWeaponSourceLedger.TargetCommit commit;

        private TargetResult(UUID var1, FoodWeaponSourceLedger.TargetEffectDomain var2, FoodWeaponSourceLedger.TargetCommit var3) {
            this.target = var1;
            this.domain = var2;
            this.commit = var3;
        }

        public UUID target() {
            return this.target;
        }

        public FoodWeaponSourceLedger.TargetEffectDomain domain() {
            return this.domain;
        }

        public FoodWeaponSourceLedger.TargetCommit commit() {
            return this.commit;
        }
    }

    public static enum TargetStatus {
        AVAILABLE,
        COMMITTED;
    }

    private static record TicketState(FoodWeaponSourceLedger.SourceTicket ticket, ItemStack actualWeapon, MinecraftServer server, String foundationRules) {
    }

    public static final class TridentCarrierLineage {
        private final ThrownTrident entity;
        private final ItemStack input;
        private final ItemStack copied;
        private final Object origin;
        private boolean observationRejected;
        private Throwable observationFailure;

        private TridentCarrierLineage(ThrownTrident var1, ItemStack var2, ItemStack var3, Object var4) {
            this.entity = var1;
            this.input = var2;
            this.copied = var3;
            this.origin = var4;
        }
    }

    private static final class TridentTicketBefore {
        private final FoodWeaponSourceLedger.SourceTicket ticket;
        private final ItemStack actual;
        private final OwnerNativeMaterialImage expected;
        private final Thread thread = Thread.currentThread();
        private boolean observationRejected;

        private TridentTicketBefore(FoodWeaponSourceLedger.SourceTicket var1, ItemStack var2, CompoundTag var3) {
            this.ticket = var1;
            this.actual = var2;
            this.expected = OwnerNativeMaterialImage.freeze(var3);
        }
    }

    private static final class WeaponExpectation {
        private final ItemStack actual;
        private final Object repairGeneration;
        private CompoundTag expected;
        private final FoodWeaponSourceLedger.ImageCursor cursor;
        private volatile FoodWeaponSourceLedger.OwnerWriterStep active;
        private boolean closed;

        private WeaponExpectation(ItemStack var1, CompoundTag var2) {
            this.actual = var1;
            this.repairGeneration = OwnerRepairGenerationAuthority.current(var1);
            this.expected = var2.copy();
            this.cursor = new FoodWeaponSourceLedger.ImageCursor(FoodWeaponSourceLedger.canonical(this.expected));
        }
    }

    private static final class WriterPlanningCut {
        private final FoodWeaponSourceLedger.WriterSelection selection;
        private final Object position;
        private final Object transition;
        private final int ordinal;

        private WriterPlanningCut(FoodWeaponSourceLedger.WriterSelection var1, Object var2, Object var3, int var4) {
            this.selection = var1;
            this.position = var2;
            this.transition = var3;
            this.ordinal = var4;
        }

        private boolean samePublication(FoodWeaponSourceLedger.WriterSelection var1, Object var2, Object var3, int var4) {
            return var1 == this.selection
                && (this.selection == null || !this.selection.revoked)
                && var2 == this.position
                && var3 == this.transition
                && var4 == this.ordinal;
        }

        private boolean matches(FoodWeaponSourceLedger.WriterSelection var1, Object var2, Object var3, int var4) {
            return this.selection != null && this.samePublication(var1, var2, var3, var4);
        }
    }

    private static final class WriterSelection {
        private final Object producer;
        private final int producerOrdinal;
        private final Object commit;
        private final Object leaf;
        private final int leafOrdinal;
        private boolean revoked;

        private WriterSelection(Object var1, int var2, Object var3, Object var4, int var5) {
            this.producer = var1;
            this.producerOrdinal = var2;
            this.commit = var3;
            this.leaf = var4;
            this.leafOrdinal = var5;
        }

        private void revoke() {
            this.revoked = true;
        }

        private void observationFailed(FoodWeaponSourceLedger.ObserverFailures var1, int var2, Throwable var3) {
            this.revoke();
            var1.record(var2, var3);
        }

        private void originalFailed(FoodWeaponSourceLedger.ObserverFailures var1, Throwable var2) {
            this.revoke();
            var1.original(var2);
        }

        private boolean matches(Object var1, int var2, Object var3, Object var4, int var5) {
            return !this.revoked
                && this.producer == var1
                && this.producerOrdinal == var2
                && this.commit == var3
                && this.leaf == var4
                && this.leafOrdinal == var5;
        }
    }
    public static void nativePassiveProjectileProfile(AbstractArrow arrow, ServerPlayer player) {
        CompoundTag persistent = arrow.getPersistentData();
        String profile = FoodWeaponBossPassive.profile(player.getMainHandItem());
        NativeOperation operation = NATIVE_OPERATION.get();
        NativeReceipt receipt = operation == null ? null : operation.current;
        boolean caller = StackWalker.getInstance(java.lang.StackWalker.Option.RETAIN_CLASS_REFERENCE).walk(frames -> frames.skip(1L).findFirst()
            .map(frame -> frame.getDeclaringClass() == FoodWeaponBossPassive.class && frame.getMethodName().equals("projectile")
                && frame.getDescriptor().equals("(Lnet/neoforged/neoforge/event/entity/EntityJoinLevelEvent;)V")).orElse(false));
        if (caller && liveNative(operation) && operation.adding && receipt != null && receipt.entity == arrow
            && operation.player == player && arrow.getOwner() == player && arrow.level() == player.level()
            && !receipt.added && !receipt.consumed && !receipt.profileFieldInitialized) {
            receipt.profileFieldInitialized = OwnerFieldExpectedAuthority.nativeProjectileProfile(arrow, persistent, profile);
        } else {
            persistent.putString("FTBossIntrinsicProjectileProfile", profile);
        }
    }
    public static void nativeManorOwner(Entity owner, Entity child, UUID value) {
        CompoundTag persistent = child.getPersistentData();
        NativeOperation operation = NATIVE_OPERATION.get();
        NativeReceipt receipt = operation == null ? null : operation.current;
        boolean caller = StackWalker.getInstance(java.lang.StackWalker.Option.RETAIN_CLASS_REFERENCE).walk(frames -> frames.skip(1L).findFirst()
            .map(frame -> frame.getDeclaringClass() == net.farmingtales.ftboss.boss.ManorFightMembers.class
                && frame.getMethodName().equals("inherit") && frame.getDescriptor().equals("(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/entity/Entity;)V")).orElse(false));
        if (caller && liveNative(operation) && operation.adding && receipt != null && receipt.entity == child
            && operation.player == owner && child instanceof AbstractArrow arrow && arrow.getOwner() == owner
            && child.level() == owner.level() && !receipt.added && !receipt.consumed && !receipt.manorOwnerFieldInitialized) {
            receipt.manorOwnerFieldInitialized = OwnerFieldExpectedAuthority.nativeProjectileOwner(child, persistent, owner, value);
        } else {
            persistent.putUUID("ftboss_manor_owner", value);
        }
    }

    private static final class PrivateRead423 {
        private final java.util.LinkedHashMap<String, net.minecraft.nbt.Tag> fields = new java.util.LinkedHashMap<>();
        private void put(String key, net.minecraft.nbt.Tag value) { fields.put(key, value); }
        private void putString(String key, String value) { put(key, net.minecraft.nbt.StringTag.valueOf(value)); }
        private void putInt(String key, int value) { put(key, net.minecraft.nbt.IntTag.valueOf(value)); }
        private CompoundTag finish() {
            return net.farmingtales.ftboss.item.NativeExpectedFieldObservation.socketRecipeCompoundForRead423(fields);
        }
    }
}
