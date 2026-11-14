/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.nbt.ByteArrayTag
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.nbt.IntArrayTag
 *  net.minecraft.nbt.ListTag
 *  net.minecraft.nbt.LongArrayTag
 *  net.minecraft.nbt.LongTag
 *  net.minecraft.nbt.NbtUtils
 *  net.minecraft.nbt.NumericTag
 *  net.minecraft.nbt.StringTag
 *  net.minecraft.nbt.Tag
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.server.MinecraftServer
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.tags.DamageTypeTags
 *  net.minecraft.util.RandomSource
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.InteractionResultHolder
 *  net.minecraft.world.damagesource.DamageSource
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.entity.projectile.AbstractArrow
 *  net.minecraft.world.entity.projectile.Arrow
 *  net.minecraft.world.entity.projectile.Projectile
 *  net.minecraft.world.entity.projectile.SpectralArrow
 *  net.minecraft.world.entity.projectile.ThrownTrident
 *  net.minecraft.world.item.ArrowItem
 *  net.minecraft.world.item.BowItem
 *  net.minecraft.world.item.CrossbowItem
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.SpectralArrowItem
 *  net.minecraft.world.item.TridentItem
 *  net.minecraft.world.level.Level
 *  net.minecraftforge.registries.ForgeRegistries
 */
package net.farmingtales.ftboss.item;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
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
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import net.farmingtales.ftboss.integ.FoodWeaponTridentCarrierReader;
import net.farmingtales.ftboss.item.FoodSocketAttributePowers;
import net.farmingtales.ftboss.item.FoodSocketPowers;
import net.farmingtales.ftboss.item.FoodWeaponConsumerSequence;
import net.farmingtales.ftboss.item.FoodWeaponDamageScaling;
import net.farmingtales.ftboss.item.FoodWeaponDamageSources;
import net.farmingtales.ftboss.item.FoodWeaponEchoQueue;
import net.farmingtales.ftboss.item.FoodWeaponEvolution;
import net.farmingtales.ftboss.item.FoodWeaponExpectedFieldsCodec;
import net.farmingtales.ftboss.item.FoodWeaponExpectedTrace;
import net.farmingtales.ftboss.item.FoodWeaponFamilyBuild;
import net.farmingtales.ftboss.item.FoodWeaponFinalDamage;
import net.farmingtales.ftboss.item.FoodWeaponHitPlan;
import net.farmingtales.ftboss.item.FoodWeaponLifecycle;
import net.farmingtales.ftboss.item.FoodWeaponMaterialFingerprint;
import net.farmingtales.ftboss.item.FoodWeaponOriginalSequence;
import net.farmingtales.ftboss.item.FoodWeaponRanged;
import net.farmingtales.ftboss.item.FoodWeaponRepair;
import net.farmingtales.ftboss.item.FoodWeaponUseBridge;
import net.farmingtales.ftboss.item.FourWeaponFoundationAdapter;
import net.farmingtales.ftboss.item.NativeEvergaleBridge;
import net.farmingtales.ftboss.item.NativeEvergaleImpact;
import net.farmingtales.ftboss.item.NativeExpectedFieldViews;
import net.farmingtales.ftboss.item.NativeExpectedRawFields;
import net.farmingtales.ftboss.item.NativeObservationCarrier;
import net.farmingtales.ftboss.item.NativeObservationFrame;
import net.farmingtales.ftboss.item.NativePaidWeaponCarrier;
import net.farmingtales.ftboss.item.NativeVanillaImpact;
import net.farmingtales.ftboss.item.OwnerFieldExpectedAuthority;
import net.farmingtales.ftboss.item.OwnerFieldSourceAccess;
import net.farmingtales.ftboss.item.OwnerFullRepairGuard;
import net.farmingtales.ftboss.item.OwnerLineageRevocationStore;
import net.farmingtales.ftboss.item.OwnerNativeLeafFields;
import net.farmingtales.ftboss.item.OwnerNativeMaterialEntityIdentity;
import net.farmingtales.ftboss.item.OwnerNativeMaterialImage;
import net.farmingtales.ftboss.item.OwnerNativeSemanticAuthority;
import net.farmingtales.ftboss.item.OwnerRepairGenerationAuthority;
import net.farmingtales.ftboss.item.OwnerStatueWearAdmission;
import net.farmingtales.ftboss.item.OwnerTimerSemanticAuthority;
import net.farmingtales.ftboss.item.OwnerTridentCarrierAccess;
import net.farmingtales.ftboss.item.WeaponMaterialPowers;
import net.farmingtales.ftboss.skill.WeaponEvolutionSkills;
import net.farmingtales.ftboss.transaction.FoundationApprovedRules;
import net.farmingtales.ftboss.util.FoodSocketAttributeRules;
import net.farmingtales.ftboss.util.FoodWeaponBuildRules;
import net.farmingtales.ftboss.util.FoodWeaponFamilyRules;
import net.farmingtales.ftboss.util.FoodWeaponLifecycleRules;
import net.farmingtales.ftboss.util.WeaponEvolutionSkillPool;
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
import net.minecraftforge.registries.ForgeRegistries;

public final class FoodWeaponSourceLedger {
    public static final int VERSION = 3;
    public static final String IDENTITY_ROOT = "FTBossWeaponLifecycle";
    private static final int MAX_CLAIMS = 4096;
    private static final int MAX_CANONICAL_DEPTH = 64;
    private static final int MAX_CANONICAL_NODES = 65536;
    private static final int MAX_CANONICAL_BYTES = 0x100000;
    private static final long SOURCE_LIFETIME_TICKS = 1200L;
    private static final int MAX_ACTIVE_ROOT_PROJECTIONS = 4096;
    private static final InteractionRoot[] ACTIVE_ROOT_PROJECTIONS = new InteractionRoot[4096];
    private static final IdentityHashMap<NativeEvergaleBridge.RawAction, NativeEvergaleAction> EVERGALE_ACTIONS = new IdentityHashMap();
    private static final IdentityHashMap<NativeEvergaleImpact.RawImpact, NativeEvergaleInteraction> EVERGALE_IMPACTS = new IdentityHashMap();
    private static final int EVERGALE_SCOPE_LIMIT = 4096;
    private static long ownerFieldGeneration;
    private static final Map<MinecraftServer, ServerState> SERVERS;
    private static final ThreadLocal<TicketState> TRIDENT_TICKET;
    private static final ThreadLocal<NativeOperation> NATIVE_OPERATION;
    private static final ThreadLocal<Integer> NATIVE_DEPTH;
    private static final StackWalker NATIVE_CALLER;
    private static final String NATIVE_PURPOSE = "NATIVE_PROJECTILE";
    private static final String NATIVE_HURT_PURPOSE = "NATIVE_CARRIER_HURT";
    private static final String MEAT_HURT_PURPOSE = "NATIVE_MEAT_ACTUAL_HURT";
    private static final String IRON_HURT_PURPOSE = "NATIVE_IRON_ACTUAL_HURT";
    private static final ThreadLocal<NativeIronCallbackToken> PRIVATE_IRON_CALLBACK;
    private static final ThreadLocal<NativeHurtScope> PRIVATE_NATIVE_HURT;
    private static final ThreadLocal<NativeIronUseToken> PRIVATE_IRON_USE;
    private static final ThreadLocal<NativeIngressToken> PRIVATE_INGRESS;
    private static final ThreadLocal<NativeWearToken> PRIVATE_INGRESS_WEAR;
    private static final ThreadLocal<TridentTicketBefore> TRIDENT_BEFORE;
    private static final int MAX_INTERACTION_DEPTH = 16;
    private static final float PENDING_ECHO_RAW_AMOUNT = 2.0f;

    private static boolean retireOriginalIssuerRoot(NativeIssueWindow nativeIssueWindow) {
        if (!nativeIssueWindow.unchanged()) {
            return false;
        }
        InteractionRoot interactionRoot = nativeIssueWindow.projection;
        if (interactionRoot != null) {
            FoodWeaponSourceLedger.retireInteraction(interactionRoot);
            if (interactionRoot.ownerState.current == interactionRoot) {
                FoodWeaponSourceLedger.setOwnerInteraction(interactionRoot.ownerState, null);
            }
        }
        nativeIssueWindow.originalRetired = true;
        return nativeIssueWindow.unchanged();
    }

    public static NativeIssuerCustody nativeIssuerCustody() {
        return new NativeIssuerCustody();
    }

    public static synchronized void nativeIssuerTransferred(NativeIssuerCustody nativeIssuerCustody, NativeIngressToken nativeIngressToken) {
        if (nativeIssuerCustody != null && nativeIssuerCustody.ingress == nativeIngressToken) {
            nativeIssuerCustody.cut.transfer(nativeIngressToken);
        }
    }

    public static synchronized void nativeIssuerTransferred(NativeIssuerCustody nativeIssuerCustody, NativeIronUseToken nativeIronUseToken) {
        if (nativeIssuerCustody != null && nativeIssuerCustody.use == nativeIronUseToken) {
            nativeIssuerCustody.cut.transfer(nativeIronUseToken);
        }
    }

    public static synchronized void nativeIssuerTransferred(NativeIssuerCustody nativeIssuerCustody, NativeIronCallbackToken nativeIronCallbackToken) {
        if (nativeIssuerCustody != null && nativeIssuerCustody.callback == nativeIronCallbackToken) {
            nativeIssuerCustody.cut.transfer(nativeIronCallbackToken);
        }
    }

    public static synchronized void nativeIssuerTransferred(NativeIssuerCustody nativeIssuerCustody, NativeWearToken nativeWearToken) {
        if (nativeIssuerCustody != null && nativeIssuerCustody.wear == nativeWearToken) {
            nativeIssuerCustody.cut.transfer(nativeWearToken);
        }
    }

    public static synchronized void nativeIssuerTransferred(NativeIssuerCustody nativeIssuerCustody, NativeCarrierToken nativeCarrierToken) {
        if (nativeIssuerCustody != null && nativeIssuerCustody.carrier == nativeCarrierToken) {
            nativeIssuerCustody.cut.transfer(nativeCarrierToken);
        }
    }

    public static synchronized void nativeIssuerTransferred(NativeIssuerCustody nativeIssuerCustody, NativeCapabilityToken nativeCapabilityToken) {
        if (nativeIssuerCustody != null && nativeIssuerCustody.capability == nativeCapabilityToken) {
            nativeIssuerCustody.cut.transfer(nativeCapabilityToken);
        }
    }

    private static void closeOwnedExpectation(InteractionRoot interactionRoot) {
        if (interactionRoot.ownsExpectation && interactionRoot.expectation != null) {
            interactionRoot.expectation.closed = true;
            interactionRoot.expectation.cursor.close();
        }
    }

    private static void revokeUnreturnedInteraction(InteractionRoot interactionRoot) {
        if (interactionRoot == null || interactionRoot.thread != Thread.currentThread()) {
            return;
        }
        FoodWeaponSourceLedger.removeRootProjection(interactionRoot);
        FoodWeaponSourceLedger.closeOwnedExpectation(interactionRoot);
        interactionRoot.retired = true;
        interactionRoot.closed = true;
        if (interactionRoot.writerPublication != null) {
            FoodWeaponSourceLedger.closeOwnerWriterPublication(interactionRoot.writerPublication);
        }
        OriginalInteractionBinding originalInteractionBinding = interactionRoot.current;
        for (int i = 16; originalInteractionBinding != null && i > 0; --i) {
            originalInteractionBinding.closed = true;
            originalInteractionBinding = originalInteractionBinding.parent;
        }
        interactionRoot.current = null;
        if (interactionRoot.ownerState.current == interactionRoot) {
            interactionRoot.ownerState.interactionChange = interactionRoot;
            interactionRoot.ownerState.current = null;
        }
    }

    private static void revokeUnreturnedHurt(NativeHurtScope nativeHurtScope) {
        if (nativeHurtScope == null || nativeHurtScope.thread != Thread.currentThread()) {
            return;
        }
        nativeHurtScope.closed = true;
        FoodWeaponSourceLedger.revokeUnreturnedInteraction(nativeHurtScope.interaction);
        if (PRIVATE_NATIVE_HURT.get() == nativeHurtScope) {
            PRIVATE_NATIVE_HURT.remove();
        }
    }

    public static synchronized void closeNativeIssuerCustody(NativeIssuerCustody nativeIssuerCustody) {
        Object object;
        if (nativeIssuerCustody == null || !nativeIssuerCustody.cut.close()) {
            return;
        }
        if (nativeIssuerCustody.ingress != null) {
            NativeWearToken nativeWearToken;
            object = nativeIssuerCustody.ingress;
            ((NativeIngressToken)object).invalid = true;
            ((NativeIngressToken)object).expectation.closed = true;
            ((NativeIngressToken)object).expectation.cursor.close();
            NativeHurtScope nativeHurtScope = PRIVATE_NATIVE_HURT.get();
            if (nativeHurtScope != null && (nativeHurtScope.earlyIngress == object || nativeHurtScope.receipt != null && nativeHurtScope.receipt.ingress == object)) {
                FoodWeaponSourceLedger.revokeUnreturnedHurt(nativeHurtScope);
            }
            if (PRIVATE_INGRESS.get() == object) {
                PRIVATE_INGRESS.remove();
            }
            if ((nativeWearToken = PRIVATE_INGRESS_WEAR.get()) != null && nativeWearToken.ingress == object) {
                PRIVATE_INGRESS_WEAR.remove();
            }
        }
        if (nativeIssuerCustody.use != null) {
            nativeIssuerCustody.use.invalid = true;
            if (PRIVATE_IRON_USE.get() == nativeIssuerCustody.use) {
                PRIVATE_IRON_USE.remove();
            }
        }
        if (nativeIssuerCustody.callback != null) {
            nativeIssuerCustody.callback.closed = true;
            nativeIssuerCustody.callback.expectation.closed = true;
            nativeIssuerCustody.callback.expectation.cursor.close();
            object = PRIVATE_NATIVE_HURT.get();
            if (object != null && ((NativeHurtScope)object).ironCallback == nativeIssuerCustody.callback) {
                FoodWeaponSourceLedger.revokeUnreturnedHurt((NativeHurtScope)object);
            }
            if (PRIVATE_IRON_CALLBACK.get() == nativeIssuerCustody.callback) {
                PRIVATE_IRON_CALLBACK.remove();
            }
        }
        if (nativeIssuerCustody.carrier != null) {
            nativeIssuerCustody.carrier.actor = false;
            nativeIssuerCustody.carrier.added = false;
            nativeIssuerCustody.carrier.image = null;
        }
        if (nativeIssuerCustody.capability != null) {
            nativeIssuerCustody.capability.canceled = true;
        }
        if (nativeIssuerCustody.wear != null) {
            nativeIssuerCustody.wear.ingress.invalid = true;
            if (PRIVATE_INGRESS_WEAR.get() == nativeIssuerCustody.wear) {
                PRIVATE_INGRESS_WEAR.remove();
            }
        }
    }

    private static void closeNativeFailurePublication(OwnerWriterPublication ownerWriterPublication, InteractionRoot interactionRoot) {
        OwnerRepairObservation ownerRepairObservation;
        OwnerPlayerWriter ownerPlayerWriter;
        if (ownerWriterPublication == null || ownerWriterPublication.binding.root != interactionRoot) {
            return;
        }
        ownerWriterPublication.closed = true;
        if (ownerWriterPublication.selection != null) {
            ownerWriterPublication.selection.revoke();
        }
        ownerWriterPublication.observation.close();
        OwnerWriterStep ownerWriterStep = ownerWriterPublication.expectation.active;
        if (ownerWriterStep != null && ownerWriterStep.publication == ownerWriterPublication) {
            ownerWriterStep.closed = true;
            ownerWriterPublication.expectation.active = null;
        }
        if ((ownerPlayerWriter = ownerWriterPublication.playerWriter) != null && ownerPlayerWriter.publication == ownerWriterPublication) {
            ownerPlayerWriter.closed = true;
            ownerPlayerWriter.cursor.close();
            if (ownerPlayerWriter.active != null) {
                ownerPlayerWriter.active.closed = true;
            }
            ownerPlayerWriter.active = null;
        }
        if ((ownerRepairObservation = ownerWriterPublication.repairObservation) != null && ownerRepairObservation.publication == ownerWriterPublication) {
            ownerRepairObservation.closed = true;
            ownerRepairObservation.trace.close();
            ownerRepairObservation.observation.close();
        }
    }

    private static void closeNativeFailureRoot(InteractionRoot interactionRoot, WeaponExpectation weaponExpectation, OwnerWriterPublication ownerWriterPublication, OriginalInteractionBinding originalInteractionBinding) {
        if (interactionRoot == null) {
            return;
        }
        interactionRoot.closed = true;
        interactionRoot.retired = true;
        interactionRoot.readCut.close();
        interactionRoot.interactionChange = interactionRoot;
        if (weaponExpectation != null && interactionRoot.expectation == weaponExpectation && interactionRoot.ownsExpectation) {
            weaponExpectation.closed = true;
            weaponExpectation.cursor.close();
        }
        FoodWeaponSourceLedger.closeNativeFailurePublication(ownerWriterPublication, interactionRoot);
        if (interactionRoot.writerPublication != ownerWriterPublication) {
            FoodWeaponSourceLedger.closeNativeFailurePublication(interactionRoot.writerPublication, interactionRoot);
        }
        OriginalInteractionBinding originalInteractionBinding2 = interactionRoot.current;
        for (int i = 16; originalInteractionBinding2 != null && i > 0 && originalInteractionBinding2.root == interactionRoot; --i) {
            originalInteractionBinding2.closed = true;
            originalInteractionBinding2 = originalInteractionBinding2.parent;
        }
        if (originalInteractionBinding != null && originalInteractionBinding.root == interactionRoot) {
            originalInteractionBinding.closed = true;
        }
        interactionRoot.current = null;
        FoodWeaponSourceLedger.removeRootProjection(interactionRoot);
        if (interactionRoot.ownerState.current == interactionRoot) {
            interactionRoot.ownerState.interactionChange = interactionRoot;
            interactionRoot.ownerState.current = null;
        }
    }

    private static void closeNativeObservationRoots(NativeObservationCut nativeObservationCut) {
        NativeRootObservation nativeRootObservation = nativeObservationCut.roots;
        while (nativeRootObservation != null) {
            FoodWeaponSourceLedger.closeNativeFailureRoot(nativeRootObservation.root, nativeRootObservation.expectation, nativeRootObservation.publication, nativeRootObservation.binding);
            nativeRootObservation = nativeRootObservation.nextCut;
        }
    }

    public static synchronized NativeObservationCut beginNativeObservation(Object object) {
        if (object == null) {
            return null;
        }
        NativeObservationCut nativeObservationCut = new NativeObservationCut(object, null);
        for (int i = 0; i < 4096; ++i) {
            InteractionRoot interactionRoot = ACTIVE_ROOT_PROJECTIONS[i];
            if (!FoodWeaponSourceLedger.rootReadReceiver(interactionRoot, object)) continue;
            boolean bl = false;
            NativeRootObservation nativeRootObservation = interactionRoot.nativeObservations;
            while (nativeRootObservation != null) {
                if (nativeRootObservation.cut.receiver == object) {
                    bl = true;
                    break;
                }
                nativeRootObservation = nativeRootObservation.nextRoot;
            }
            nativeObservationCut.roots = nativeRootObservation = new NativeRootObservation(nativeObservationCut, interactionRoot);
            if (bl) continue;
            interactionRoot.nativeObservations = nativeRootObservation;
        }
        return nativeObservationCut;
    }

    public static synchronized void bindNativeObservationFrame(NativeObservationCut nativeObservationCut, Object object, NativeObservationFrame nativeObservationFrame) {
        if (nativeObservationCut == null || nativeObservationCut.receiver != object) {
            return;
        }
        if (nativeObservationCut.frameBound || nativeObservationFrame == null || nativeObservationFrame.receiver() != object || nativeObservationFrame.context() != object) {
            FoodWeaponSourceLedger.closeNativeObservationRoots(nativeObservationCut);
            nativeObservationCut.failure.fail(nativeObservationFrame, -1, null);
            return;
        }
        nativeObservationCut.frameBound = true;
        nativeObservationCut.frame = nativeObservationFrame;
        if (!(object instanceof NativeObservationCarrier) || nativeObservationFrame.failed() || nativeObservationCut.failure.failed) {
            FoodWeaponSourceLedger.closeNativeObservationRoots(nativeObservationCut);
            nativeObservationCut.failure.fail(nativeObservationFrame, 0, nativeObservationFrame.observationFailure(0));
        }
    }

    public static synchronized NativeObservationCut beginNativeObservation(Object object, NativeObservationFrame nativeObservationFrame) {
        NativeObservationCut nativeObservationCut = FoodWeaponSourceLedger.beginNativeObservation(object);
        FoodWeaponSourceLedger.bindNativeObservationFrame(nativeObservationCut, object, nativeObservationFrame);
        return nativeObservationCut;
    }

    public static synchronized void nativeObservationFailed(NativeObservationCut nativeObservationCut, Object object, NativeObservationFrame nativeObservationFrame, int n, Throwable throwable) {
        if (nativeObservationCut == null || nativeObservationCut.receiver != object || nativeObservationFrame != null && nativeObservationCut.frame != nativeObservationFrame) {
            return;
        }
        FoodWeaponSourceLedger.closeNativeObservationRoots(nativeObservationCut);
        nativeObservationCut.failure.fail(nativeObservationFrame, n, throwable);
    }

    public static synchronized void nativeObservationOriginalFailed(NativeObservationCut nativeObservationCut, Object object, NativeObservationFrame nativeObservationFrame, Throwable throwable) {
        if (nativeObservationCut == null || nativeObservationCut.receiver != object || nativeObservationFrame != null && nativeObservationCut.frame != nativeObservationFrame) {
            return;
        }
        FoodWeaponSourceLedger.closeNativeObservationRoots(nativeObservationCut);
        nativeObservationCut.failure.failures.original(throwable);
    }

    public static synchronized Throwable nativeObservationFailure(NativeObservationCut nativeObservationCut, int n) {
        return nativeObservationCut == null ? null : nativeObservationCut.failure.failures.failure(n);
    }

    private static boolean nativeObservationReadLive(InteractionRoot interactionRoot) {
        Object object = interactionRoot.nativeObservations;
        while (object != null) {
            if (((NativeRootObservation)object).cut.failure.failed || ((NativeRootObservation)object).cut.frame == null || ((NativeRootObservation)object).cut.frame.failed()) {
                FoodWeaponSourceLedger.closeNativeFailureRoot(interactionRoot, ((NativeRootObservation)object).expectation, ((NativeRootObservation)object).publication, ((NativeRootObservation)object).binding);
                return false;
            }
            object = ((NativeRootObservation)object).nextRoot;
        }
        object = interactionRoot.nativeProjectileObservation;
        if (object != null && (((NativeProjectileObservation)object).failure.failed || ((NativeProjectileObservation)object).frame != null && ((NativeProjectileObservation)object).frame.failed())) {
            FoodWeaponSourceLedger.closeNativeFailureRoot(interactionRoot, ((NativeProjectileObservation)object).expectation, ((NativeProjectileObservation)object).publication, ((NativeProjectileObservation)object).binding);
            return false;
        }
        return !interactionRoot.closed && !interactionRoot.retired && !interactionRoot.readCut.closed && (interactionRoot.expectation == null || !interactionRoot.expectation.closed);
    }

    static synchronized NativeProjectileObservation observeNativeProjectileImpact(NativeVanillaImpact.RawImpact rawImpact) {
        return rawImpact == null ? null : new NativeProjectileObservation(rawImpact);
    }

    private static void closeNativeProjectileObservation(NativeProjectileObservation nativeProjectileObservation) {
        if (nativeProjectileObservation == null) {
            return;
        }
        FoodWeaponSourceLedger.closeNativeFailureRoot(nativeProjectileObservation.root, nativeProjectileObservation.expectation, nativeProjectileObservation.publication, nativeProjectileObservation.binding);
        if (nativeProjectileObservation.scope != null) {
            nativeProjectileObservation.scope.closed = true;
        }
    }

    static synchronized void nativeProjectileObservationFailed(NativeProjectileObservation nativeProjectileObservation, NativeVanillaImpact.RawImpact rawImpact, NativeProjectileInteraction nativeProjectileInteraction, NativeObservationFrame nativeObservationFrame, int n, Throwable throwable) {
        if (nativeProjectileObservation == null || nativeProjectileObservation.call != rawImpact || nativeProjectileInteraction != null && nativeProjectileObservation.scope != nativeProjectileInteraction) {
            return;
        }
        FoodWeaponSourceLedger.closeNativeProjectileObservation(nativeProjectileObservation);
        if (nativeObservationFrame != null && nativeObservationFrame.receiver() == rawImpact.carrier && nativeObservationFrame.context() == rawImpact) {
            nativeProjectileObservation.frame = nativeObservationFrame;
            nativeProjectileObservation.failure.failures.original(nativeObservationFrame.originalFailure());
        }
        nativeProjectileObservation.failure.fail(nativeObservationFrame, n, throwable);
    }

    static synchronized void nativeProjectileOriginalFailed(NativeProjectileObservation nativeProjectileObservation, NativeVanillaImpact.RawImpact rawImpact, Throwable throwable) {
        if (nativeProjectileObservation == null || nativeProjectileObservation.call != rawImpact) {
            return;
        }
        FoodWeaponSourceLedger.closeNativeProjectileObservation(nativeProjectileObservation);
        nativeProjectileObservation.failure.fail(rawImpact, -1, null);
        nativeProjectileObservation.failure.failures.original(throwable);
    }

    static synchronized Throwable nativeProjectileObservationFailure(NativeProjectileObservation nativeProjectileObservation, int n) {
        if (nativeProjectileObservation == null) {
            return null;
        }
        return n == 0 && nativeProjectileObservation.openingFailure != null ? nativeProjectileObservation.openingFailure : (n == 3 && nativeProjectileObservation.closingFailure != null ? nativeProjectileObservation.closingFailure : nativeProjectileObservation.failure.failures.failure(n));
    }

    static synchronized Throwable nativeProjectileOriginalFailure(NativeProjectileObservation nativeProjectileObservation) {
        return nativeProjectileObservation == null ? null : nativeProjectileObservation.failure.failures.original;
    }

    static synchronized NativeObservationFrame nativeProjectileObservationFrame(NativeProjectileObservation nativeProjectileObservation) {
        return nativeProjectileObservation == null ? null : nativeProjectileObservation.frame;
    }

    private static CompoundTag wolfControllerValues(CompoundTag compoundTag, UUID uUID, UUID uUID2, CompoundTag compoundTag2, long l) {
        CompoundTag compoundTag3 = new CompoundTag();
        compoundTag3.m_128362_("PackToken", uUID2);
        compoundTag3.m_128362_("PackWeapon", uUID);
        compoundTag3.m_128362_("PackWeaponId", compoundTag.m_128342_("WeaponId"));
        compoundTag3.m_128359_("PackHand", compoundTag.m_128461_("OriginalHand"));
        compoundTag3.m_128359_("PackDimension", compoundTag.m_128461_("Dimension"));
        compoundTag3.m_128365_("PackFingerprint", (Tag)compoundTag2.m_6426_());
        compoundTag3.m_128365_("PackSource", (Tag)compoundTag.m_6426_());
        compoundTag3.m_128356_("PackUntil", l + 600L);
        compoundTag3.m_128356_("PackCooldown", l + 400L);
        return compoundTag3;
    }

    private static CompoundTag playerWriterActualImage(OwnerPlayerWriter ownerPlayerWriter) {
        CompoundTag compoundTag = new CompoundTag();
        compoundTag.m_128365_("Persistent", (Tag)ownerPlayerWriter.persistent.m_6426_());
        compoundTag.m_128365_("Target", (Tag)ownerPlayerWriter.target.m_6426_());
        compoundTag.m_128365_("Source", (Tag)ownerPlayerWriter.source.m_6426_());
        return compoundTag;
    }

    private static boolean ownerPlayerWriterLive(OwnerPlayerWriter ownerPlayerWriter) {
        return ownerPlayerWriter != null && !ownerPlayerWriter.closed && ownerPlayerWriter.publication.playerWriter == ownerPlayerWriter && ownerPlayerWriter.transition == ownerPlayerWriter.publication.binding.root.interactionChange && ownerPlayerWriter.selection == ownerPlayerWriter.publication.selection && FoodWeaponSourceLedger.ownerWriterSelectionLive(ownerPlayerWriter.publication) && ownerPlayerWriter.selection == ownerPlayerWriter.publication.selection && !ownerPlayerWriter.closed && ownerPlayerWriter.publication.playerWriter == ownerPlayerWriter && ownerPlayerWriter.publication.expectation.active == null;
    }

    static OwnerPlayerWriter beginOwnerRangedWriter(OwnerWriterPublication ownerWriterPublication, Object object, Object object2, CompoundTag compoundTag, CompoundTag compoundTag2, CompoundTag compoundTag3) {
        WriterPlanningCut writerPlanningCut = FoodWeaponSourceLedger.writerPlanningCut(ownerWriterPublication);
        if (!(FoodWeaponSourceLedger.writerCaller(FoodWeaponRanged.class) && FoodWeaponSourceLedger.ownerWriterSelectionLive(ownerWriterPublication) && FoodWeaponSourceLedger.writerPlanningUnchanged(ownerWriterPublication, writerPlanningCut) && ownerWriterPublication.commit == object && ownerWriterPublication.leaf == object2 && ownerWriterPublication.playerWriter == null && ownerWriterPublication.expectation.active == null && ownerWriterPublication.repairObservation == null && compoundTag != null && compoundTag2 != null && compoundTag3 != null)) {
            return null;
        }
        Object object3 = ownerWriterPublication.binding.root.interactionChange;
        Tag tag = compoundTag.m_128423_("FTBossRangedProcTimers");
        boolean bl = tag instanceof CompoundTag;
        if (bl ? tag != compoundTag2 : !compoundTag2.m_128456_()) {
            return null;
        }
        PlayerImagePlan playerImagePlan = new PlayerImagePlan(false, compoundTag, compoundTag2, compoundTag3, bl, compoundTag3 == compoundTag2, compoundTag3 == compoundTag, null);
        if (!FoodWeaponSourceLedger.canonicalInputValid((Tag)playerImagePlan.image())) {
            return null;
        }
        OwnerPlayerWriter ownerPlayerWriter = new OwnerPlayerWriter(ownerWriterPublication, compoundTag, compoundTag2, compoundTag3, playerImagePlan, writerPlanningCut.selection);
        if (!FoodWeaponSourceLedger.ownerWriterSelectionLive(ownerWriterPublication) || !FoodWeaponSourceLedger.writerPlanningUnchanged(ownerWriterPublication, writerPlanningCut) || ownerPlayerWriter.selection != writerPlanningCut.selection || object3 != ownerWriterPublication.binding.root.interactionChange || ownerWriterPublication.playerWriter != null || ownerWriterPublication.expectation.active != null || ownerWriterPublication.repairObservation != null) {
            return null;
        }
        ownerWriterPublication.playerWriter = ownerPlayerWriter;
        return ownerPlayerWriter;
    }

    static OwnerPlayerWriter beginOwnerPackController(OwnerWriterPublication ownerWriterPublication, FoodSocketPowers.PreparedPack preparedPack, FoodWeaponFinalDamage.PublishingCleanup publishingCleanup, CompoundTag compoundTag, CompoundTag compoundTag2, UUID uUID, UUID uUID2, CompoundTag compoundTag3) {
        WriterPlanningCut writerPlanningCut = FoodWeaponSourceLedger.writerPlanningCut(ownerWriterPublication);
        if (!(FoodWeaponSourceLedger.writerCaller(FoodSocketPowers.class) && FoodWeaponSourceLedger.ownerWriterSelectionLive(ownerWriterPublication) && FoodWeaponSourceLedger.writerPlanningUnchanged(ownerWriterPublication, writerPlanningCut) && ownerWriterPublication.playerWriter == null && ownerWriterPublication.expectation.active == null && ownerWriterPublication.repairObservation == null && compoundTag != null && compoundTag2 != null && uUID != null && uUID2 != null && compoundTag3 != null && FoodWeaponFinalDamage.matchesPackPublishing(preparedPack, ownerWriterPublication.receipt, publishingCleanup) && FoodSocketPowers.matchesOwnerPackParameters(preparedPack, publishingCleanup, ownerWriterPublication.receipt, compoundTag, compoundTag2, uUID, uUID2, compoundTag3, ownerWriterPublication.binding.snapshot, ownerWriterPublication.binding.root.tick))) {
            return null;
        }
        Object object = ownerWriterPublication.binding.root.interactionChange;
        if (!compoundTag.m_128469_("FTBossFoodSocketPowers").equals((Object)compoundTag2)) {
            return null;
        }
        CompoundTag compoundTag4 = FoodWeaponSourceLedger.wolfControllerValues(ownerWriterPublication.binding.snapshot, uUID, uUID2, compoundTag3, ownerWriterPublication.binding.root.tick);
        PlayerImagePlan playerImagePlan = new PlayerImagePlan(true, compoundTag, compoundTag2, compoundTag2, false, true, false, compoundTag4);
        if (!FoodWeaponSourceLedger.canonicalInputValid((Tag)playerImagePlan.image()) || !FoodWeaponSourceLedger.canonicalInputValid((Tag)compoundTag4)) {
            return null;
        }
        OwnerPlayerWriter ownerPlayerWriter = new OwnerPlayerWriter(ownerWriterPublication, compoundTag, compoundTag2, compoundTag2, playerImagePlan, writerPlanningCut.selection);
        if (!(FoodWeaponSourceLedger.ownerWriterSelectionLive(ownerWriterPublication) && FoodWeaponSourceLedger.writerPlanningUnchanged(ownerWriterPublication, writerPlanningCut) && ownerPlayerWriter.selection == writerPlanningCut.selection && object == ownerWriterPublication.binding.root.interactionChange && FoodWeaponFinalDamage.matchesPackPublishing(preparedPack, ownerWriterPublication.receipt, publishingCleanup) && FoodWeaponSourceLedger.writerPlanningUnchanged(ownerWriterPublication, writerPlanningCut) && ownerWriterPublication.playerWriter == null && ownerWriterPublication.expectation.active == null && ownerWriterPublication.repairObservation == null)) {
            return null;
        }
        ownerPlayerWriter.packPrepared = preparedPack;
        ownerPlayerWriter.packCleanup = publishingCleanup;
        ownerWriterPublication.playerWriter = ownerPlayerWriter;
        return ownerPlayerWriter;
    }

    static OwnerPlayerStep planOwnerPlayerKey(OwnerPlayerWriter ownerPlayerWriter, int n, String string, Tag tag) {
        return FoodWeaponSourceLedger.planOwnerPlayerArgument(ownerPlayerWriter, n, ownerPlayerWriter == null ? null : ownerPlayerWriter.target, string, new PlayerWriterArgument(PlayerWriterArgumentKind.TAG, tag, null, null, 0L));
    }

    static OwnerPlayerStep planOwnerPlayerUuidKey(OwnerPlayerWriter ownerPlayerWriter, int n, CompoundTag compoundTag, String string, UUID uUID) {
        return FoodWeaponSourceLedger.planOwnerPlayerArgument(ownerPlayerWriter, n, compoundTag, string, new PlayerWriterArgument(PlayerWriterArgumentKind.UUID, null, uUID, null, 0L));
    }

    static OwnerPlayerStep planOwnerPlayerStringKey(OwnerPlayerWriter ownerPlayerWriter, int n, CompoundTag compoundTag, String string, String string2) {
        return FoodWeaponSourceLedger.planOwnerPlayerArgument(ownerPlayerWriter, n, compoundTag, string, new PlayerWriterArgument(PlayerWriterArgumentKind.STRING, null, null, string2, 0L));
    }

    static OwnerPlayerStep planOwnerPlayerLongKey(OwnerPlayerWriter ownerPlayerWriter, int n, CompoundTag compoundTag, String string, long l) {
        return FoodWeaponSourceLedger.planOwnerPlayerArgument(ownerPlayerWriter, n, compoundTag, string, new PlayerWriterArgument(PlayerWriterArgumentKind.LONG, null, null, null, l));
    }

    private static OwnerPlayerStep planOwnerPlayerArgument(OwnerPlayerWriter ownerPlayerWriter, int n, CompoundTag compoundTag, String string, PlayerWriterArgument playerWriterArgument) {
        PlayerImageDelta playerImageDelta;
        int n2;
        WriterPlanningCut writerPlanningCut = ownerPlayerWriter == null ? null : FoodWeaponSourceLedger.writerPlanningCut(ownerPlayerWriter.publication);
        Object object = ownerPlayerWriter == null ? null : ownerPlayerWriter.cursor.position();
        int n3 = n2 = ownerPlayerWriter == null ? -1 : ownerPlayerWriter.plan.next;
        if (ownerPlayerWriter == null || !FoodWeaponSourceLedger.writerCaller(ownerPlayerWriter.plan.wolf ? FoodSocketPowers.class : FoodWeaponRanged.class) || !FoodWeaponSourceLedger.ownerPlayerWriterLive(ownerPlayerWriter) || !FoodWeaponSourceLedger.writerPlanningUnchanged(ownerPlayerWriter.publication, writerPlanningCut) || ownerPlayerWriter.selection != writerPlanningCut.selection || ownerPlayerWriter.active != null || n != n2 || compoundTag != (playerWriterArgument.kind == PlayerWriterArgumentKind.ROOT ? ownerPlayerWriter.persistent : ownerPlayerWriter.target) || playerWriterArgument.kind == PlayerWriterArgumentKind.ROOT && playerWriterArgument.tag != ownerPlayerWriter.target) {
            return null;
        }
        if (!ownerPlayerWriter.cursor.matches(FoodWeaponSourceLedger.canonical((Tag)FoodWeaponSourceLedger.playerWriterActualImage(ownerPlayerWriter)))) {
            return null;
        }
        PlayerImageDelta playerImageDelta2 = playerImageDelta = playerWriterArgument.kind == PlayerWriterArgumentKind.ROOT ? ownerPlayerWriter.plan.root(n) : ownerPlayerWriter.plan.key(n, string);
        if (playerImageDelta == null || !playerImageDelta.key.equals(string) || !playerWriterArgument.expected(playerImageDelta.value)) {
            return null;
        }
        OwnerPlayerStep ownerPlayerStep = new OwnerPlayerStep(ownerPlayerWriter, playerImageDelta, playerWriterArgument, compoundTag, writerPlanningCut.selection);
        if (!FoodWeaponSourceLedger.ownerPlayerWriterLive(ownerPlayerWriter) || !FoodWeaponSourceLedger.writerPlanningUnchanged(ownerPlayerWriter.publication, writerPlanningCut) || ownerPlayerWriter.selection != writerPlanningCut.selection || ownerPlayerStep.selection != writerPlanningCut.selection || ownerPlayerWriter.active != null || object != ownerPlayerWriter.cursor.position() || n2 != ownerPlayerWriter.plan.next) {
            return null;
        }
        ownerPlayerWriter.active = ownerPlayerStep;
        return ownerPlayerStep;
    }

    static OwnerPlayerStep planOwnerPlayerRoot(OwnerPlayerWriter ownerPlayerWriter, int n, CompoundTag compoundTag, CompoundTag compoundTag2) {
        return FoodWeaponSourceLedger.planOwnerPlayerArgument(ownerPlayerWriter, n, compoundTag, ownerPlayerWriter == null ? null : ownerPlayerWriter.plan.rootKey, new PlayerWriterArgument(PlayerWriterArgumentKind.ROOT, (Tag)compoundTag2, null, null, 0L));
    }

    public static void ownerPlayerPutEntered(OwnerPlayerStep ownerPlayerStep, CompoundTag compoundTag, String string, Tag tag) {
        if (ownerPlayerStep == null || !ownerPlayerStep.argument.originalTag(tag) || !ownerPlayerStep.argument.expected(ownerPlayerStep.delta.value)) {
            return;
        }
        FoodWeaponSourceLedger.ownerPlayerArgumentEntered(ownerPlayerStep, compoundTag, string);
    }

    public static void ownerPlayerUuidEntered(OwnerPlayerStep ownerPlayerStep, CompoundTag compoundTag, String string, UUID uUID) {
        if (ownerPlayerStep == null || !ownerPlayerStep.argument.originalUuid(uUID)) {
            return;
        }
        FoodWeaponSourceLedger.ownerPlayerArgumentEntered(ownerPlayerStep, compoundTag, string);
    }

    public static void ownerPlayerStringEntered(OwnerPlayerStep ownerPlayerStep, CompoundTag compoundTag, String string, String string2) {
        if (ownerPlayerStep == null || !ownerPlayerStep.argument.originalString(string2)) {
            return;
        }
        FoodWeaponSourceLedger.ownerPlayerArgumentEntered(ownerPlayerStep, compoundTag, string);
    }

    public static void ownerPlayerLongEntered(OwnerPlayerStep ownerPlayerStep, CompoundTag compoundTag, String string, long l) {
        if (ownerPlayerStep == null || !ownerPlayerStep.argument.originalLong(l)) {
            return;
        }
        FoodWeaponSourceLedger.ownerPlayerArgumentEntered(ownerPlayerStep, compoundTag, string);
    }

    private static void ownerPlayerArgumentEntered(OwnerPlayerStep ownerPlayerStep, CompoundTag compoundTag, String string) {
        if (ownerPlayerStep == null || ownerPlayerStep.closed || ownerPlayerStep.entered || ownerPlayerStep.writer.active != ownerPlayerStep || ownerPlayerStep.selection != ownerPlayerStep.writer.selection || !FoodWeaponSourceLedger.ownerPlayerWriterLive(ownerPlayerStep.writer) || compoundTag != ownerPlayerStep.receiver || compoundTag != (ownerPlayerStep.delta.rootWrite ? ownerPlayerStep.writer.persistent : ownerPlayerStep.writer.target) || !ownerPlayerStep.delta.key.equals(string)) {
            return;
        }
        byte[] byArray = FoodWeaponSourceLedger.canonical((Tag)FoodWeaponSourceLedger.playerWriterActualImage(ownerPlayerStep.writer));
        if (!FoodWeaponSourceLedger.ownerPlayerWriterLive(ownerPlayerStep.writer) || ownerPlayerStep.selection != ownerPlayerStep.writer.selection || ownerPlayerStep.closed || ownerPlayerStep.writer.active != ownerPlayerStep || compoundTag != ownerPlayerStep.receiver || ownerPlayerStep.position != ownerPlayerStep.writer.cursor.position() || ownerPlayerStep.delta.ordinal != ownerPlayerStep.writer.plan.next || !ownerPlayerStep.writer.cursor.matches(byArray)) {
            return;
        }
        ownerPlayerStep.entered = true;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void ownerPlayerPutReturned(OwnerPlayerStep ownerPlayerStep, CompoundTag compoundTag) {
        if (ownerPlayerStep == null || ownerPlayerStep.closed) {
            return;
        }
        boolean bl = false;
        try {
            Tag tag;
            if (!ownerPlayerStep.entered || ownerPlayerStep.selection != ownerPlayerStep.writer.selection || !FoodWeaponSourceLedger.ownerPlayerWriterLive(ownerPlayerStep.writer) || ownerPlayerStep.writer.active != ownerPlayerStep || compoundTag != ownerPlayerStep.receiver || compoundTag != (ownerPlayerStep.delta.rootWrite ? ownerPlayerStep.writer.persistent : ownerPlayerStep.writer.target)) {
                return;
            }
            byte[] byArray = FoodWeaponSourceLedger.canonical((Tag)FoodWeaponSourceLedger.playerWriterActualImage(ownerPlayerStep.writer));
            Tag tag2 = tag = ownerPlayerStep.delta.rootWrite ? ownerPlayerStep.writer.persistent.m_128423_(ownerPlayerStep.writer.plan.rootKey) : null;
            if (ownerPlayerStep.selection != ownerPlayerStep.writer.selection || !FoodWeaponSourceLedger.ownerPlayerWriterLive(ownerPlayerStep.writer) || ownerPlayerStep.closed || ownerPlayerStep.writer.active != ownerPlayerStep || ownerPlayerStep.delta.ordinal != ownerPlayerStep.writer.plan.next || ownerPlayerStep.delta.rootWrite && tag != ownerPlayerStep.writer.target || !ownerPlayerStep.writer.cursor.advance(ownerPlayerStep.position, ownerPlayerStep.before, ownerPlayerStep.planned, byArray)) {
                return;
            }
            bl = ownerPlayerStep.writer.plan.accept(ownerPlayerStep.delta);
            if (bl) {
                ownerPlayerStep.writer.publication.binding.root.readCut.change();
            }
        }
        finally {
            ownerPlayerStep.closed = true;
            if (ownerPlayerStep.writer.active == ownerPlayerStep) {
                ownerPlayerStep.writer.active = null;
            }
            if (!bl) {
                FoodWeaponSourceLedger.closeOwnerPlayerWriter(ownerPlayerStep.writer);
            }
        }
    }

    public static synchronized void closeOwnerPlayerStep(OwnerPlayerStep ownerPlayerStep) {
        if (ownerPlayerStep == null || ownerPlayerStep.closed) {
            return;
        }
        ownerPlayerStep.closed = true;
        if (ownerPlayerStep.writer.active == ownerPlayerStep) {
            ownerPlayerStep.writer.active = null;
        }
        FoodWeaponSourceLedger.closeOwnerPlayerWriter(ownerPlayerStep.writer);
    }

    static void finishOwnerPackWriter(OwnerPlayerWriter ownerPlayerWriter, FoodSocketPowers.PreparedPack preparedPack, FoodWeaponFinalDamage.PublishingCleanup publishingCleanup) {
        if (ownerPlayerWriter == null || !FoodWeaponSourceLedger.writerCaller(FoodSocketPowers.class) || ownerPlayerWriter.packPrepared != preparedPack || ownerPlayerWriter.packCleanup != publishingCleanup || !FoodWeaponFinalDamage.matchesPackPublicationCleanup(preparedPack, publishingCleanup)) {
            return;
        }
        FoodWeaponSourceLedger.closeOwnerPlayerWriter(ownerPlayerWriter);
    }

    public static synchronized void closeOwnerPlayerWriter(OwnerPlayerWriter ownerPlayerWriter) {
        if (ownerPlayerWriter == null || ownerPlayerWriter.closed) {
            return;
        }
        ownerPlayerWriter.closed = true;
        ownerPlayerWriter.cursor.close();
        if (ownerPlayerWriter.active != null) {
            ownerPlayerWriter.active.closed = true;
            ownerPlayerWriter.active = null;
        }
        if (!ownerPlayerWriter.plan.published && ownerPlayerWriter.publication.selection == ownerPlayerWriter.selection && ownerPlayerWriter.publication.playerWriter == ownerPlayerWriter) {
            ownerPlayerWriter.publication.closed = true;
        }
    }

    static synchronized void ownerPublicationObservationFailed(OwnerWriterPublication ownerWriterPublication, int n, Throwable throwable) {
        if (ownerWriterPublication == null || ownerWriterPublication.thread != Thread.currentThread()) {
            return;
        }
        ownerWriterPublication.closed = true;
        if (ownerWriterPublication.binding.root.writerPublication == ownerWriterPublication && ownerWriterPublication.binding.root.expectation == ownerWriterPublication.expectation) {
            ownerWriterPublication.binding.root.readCut.change();
        }
        try {
            ownerWriterPublication.observation.record(n, throwable);
        }
        finally {
            FoodWeaponSourceLedger.closeOwnerWriterPublication(ownerWriterPublication);
        }
    }

    static synchronized Throwable ownerPublicationObservationFailure(OwnerWriterPublication ownerWriterPublication, int n) {
        return ownerWriterPublication == null || ownerWriterPublication.thread != Thread.currentThread() ? null : ownerWriterPublication.observation.failure(n);
    }

    static synchronized void ownerPublicationOriginalFailed(OwnerWriterPublication ownerWriterPublication, Throwable throwable) {
        if (ownerWriterPublication == null || ownerWriterPublication.thread != Thread.currentThread()) {
            return;
        }
        ownerWriterPublication.closed = true;
        if (ownerWriterPublication.binding.root.writerPublication == ownerWriterPublication && ownerWriterPublication.binding.root.expectation == ownerWriterPublication.expectation) {
            ownerWriterPublication.binding.root.readCut.change();
        }
        try {
            ownerWriterPublication.observation.original(throwable);
        }
        finally {
            FoodWeaponSourceLedger.closeOwnerWriterPublication(ownerWriterPublication);
        }
    }

    static synchronized Throwable ownerPublicationOriginalFailure(OwnerWriterPublication ownerWriterPublication) {
        return ownerWriterPublication == null || ownerWriterPublication.thread != Thread.currentThread() ? null : ownerWriterPublication.observation.original;
    }

    private static void revokeOwnerWriterSelection(OwnerWriterPublication ownerWriterPublication, WriterSelection writerSelection) {
        OwnerPlayerWriter ownerPlayerWriter;
        OwnerWriterStep ownerWriterStep;
        if (writerSelection == null) {
            return;
        }
        writerSelection.revoke();
        if (ownerWriterPublication.selection != writerSelection) {
            return;
        }
        ownerWriterPublication.closed = true;
        if (ownerWriterPublication.binding.root.writerPublication == ownerWriterPublication && ownerWriterPublication.binding.root.expectation == ownerWriterPublication.expectation) {
            ownerWriterPublication.binding.root.readCut.change();
        }
        if ((ownerWriterStep = ownerWriterPublication.expectation.active) != null && ownerWriterStep.publication == ownerWriterPublication && ownerWriterStep.selection == writerSelection) {
            ownerWriterStep.closed = true;
            ownerWriterPublication.expectation.active = null;
        }
        if ((ownerPlayerWriter = ownerWriterPublication.playerWriter) != null && ownerPlayerWriter.selection == writerSelection) {
            FoodWeaponSourceLedger.closeOwnerPlayerWriter(ownerPlayerWriter);
        }
    }

    public static synchronized void ownerWriterObservationFailed(OwnerWriterStep ownerWriterStep, int n, Throwable throwable) {
        if (ownerWriterStep == null || ownerWriterStep.publication.thread != Thread.currentThread()) {
            return;
        }
        if (ownerWriterStep.publication.failedWriterStep == null) {
            ownerWriterStep.publication.failedWriterStep = ownerWriterStep;
        }
        FoodWeaponSourceLedger.revokeOwnerWriterSelection(ownerWriterStep.publication, ownerWriterStep.selection);
        try {
            ownerWriterStep.selection.observationFailed(ownerWriterStep.observation, n, throwable);
        }
        finally {
            FoodWeaponSourceLedger.closeOwnerWriterStep(ownerWriterStep);
        }
    }

    public static synchronized void ownerWriterOriginalFailed(OwnerWriterStep ownerWriterStep, Throwable throwable) {
        if (ownerWriterStep == null || ownerWriterStep.publication.thread != Thread.currentThread()) {
            return;
        }
        if (ownerWriterStep.publication.failedWriterStep == null) {
            ownerWriterStep.publication.failedWriterStep = ownerWriterStep;
        }
        FoodWeaponSourceLedger.revokeOwnerWriterSelection(ownerWriterStep.publication, ownerWriterStep.selection);
        try {
            ownerWriterStep.selection.originalFailed(ownerWriterStep.observation, throwable);
        }
        finally {
            FoodWeaponSourceLedger.closeOwnerWriterStep(ownerWriterStep);
        }
    }

    public static synchronized Throwable ownerWriterOriginalFailure(OwnerWriterStep ownerWriterStep) {
        return ownerWriterStep == null || ownerWriterStep.publication.thread != Thread.currentThread() ? null : ownerWriterStep.observation.original;
    }

    public static synchronized Throwable ownerWriterObservationFailure(OwnerWriterStep ownerWriterStep, int n) {
        return ownerWriterStep == null || ownerWriterStep.publication.thread != Thread.currentThread() ? null : ownerWriterStep.observation.failure(n);
    }

    public static synchronized void ownerPlayerWriterObservationFailed(OwnerPlayerWriter ownerPlayerWriter, int n, Throwable throwable) {
        if (ownerPlayerWriter == null || ownerPlayerWriter.publication.thread != Thread.currentThread()) {
            return;
        }
        if (ownerPlayerWriter.publication.failedPlayerWriter == null) {
            ownerPlayerWriter.publication.failedPlayerWriter = ownerPlayerWriter;
        }
        FoodWeaponSourceLedger.revokeOwnerWriterSelection(ownerPlayerWriter.publication, ownerPlayerWriter.selection);
        try {
            ownerPlayerWriter.selection.observationFailed(ownerPlayerWriter.observation, n, throwable);
        }
        finally {
            FoodWeaponSourceLedger.closeOwnerPlayerWriter(ownerPlayerWriter);
        }
    }

    public static synchronized Throwable ownerPlayerWriterObservationFailure(OwnerPlayerWriter ownerPlayerWriter, int n) {
        return ownerPlayerWriter == null || ownerPlayerWriter.publication.thread != Thread.currentThread() ? null : ownerPlayerWriter.observation.failure(n);
    }

    public static synchronized void ownerPlayerWriterOriginalFailed(OwnerPlayerWriter ownerPlayerWriter, Throwable throwable) {
        if (ownerPlayerWriter == null || ownerPlayerWriter.publication.thread != Thread.currentThread()) {
            return;
        }
        if (ownerPlayerWriter.publication.failedPlayerWriter == null) {
            ownerPlayerWriter.publication.failedPlayerWriter = ownerPlayerWriter;
        }
        FoodWeaponSourceLedger.revokeOwnerWriterSelection(ownerPlayerWriter.publication, ownerPlayerWriter.selection);
        try {
            ownerPlayerWriter.selection.originalFailed(ownerPlayerWriter.observation, throwable);
        }
        finally {
            FoodWeaponSourceLedger.closeOwnerPlayerWriter(ownerPlayerWriter);
        }
    }

    public static synchronized Throwable ownerPlayerWriterOriginalFailure(OwnerPlayerWriter ownerPlayerWriter) {
        return ownerPlayerWriter == null || ownerPlayerWriter.publication.thread != Thread.currentThread() ? null : ownerPlayerWriter.observation.original;
    }

    static synchronized OwnerPlayerWriter ownerFailedPlayerWriter(OwnerWriterPublication ownerWriterPublication) {
        return ownerWriterPublication == null || ownerWriterPublication.thread != Thread.currentThread() ? null : ownerWriterPublication.failedPlayerWriter;
    }

    public static synchronized void ownerPlayerObservationFailed(OwnerPlayerStep ownerPlayerStep, int n, Throwable throwable) {
        if (ownerPlayerStep == null || ownerPlayerStep.writer.publication.thread != Thread.currentThread()) {
            return;
        }
        if (ownerPlayerStep.writer.publication.failedPlayerStep == null) {
            ownerPlayerStep.writer.publication.failedPlayerStep = ownerPlayerStep;
        }
        FoodWeaponSourceLedger.revokeOwnerWriterSelection(ownerPlayerStep.writer.publication, ownerPlayerStep.selection);
        try {
            ownerPlayerStep.selection.observationFailed(ownerPlayerStep.observation, n, throwable);
        }
        finally {
            FoodWeaponSourceLedger.closeOwnerPlayerStep(ownerPlayerStep);
        }
    }

    public static synchronized void ownerPlayerOriginalFailed(OwnerPlayerStep ownerPlayerStep, Throwable throwable) {
        if (ownerPlayerStep == null || ownerPlayerStep.writer.publication.thread != Thread.currentThread()) {
            return;
        }
        if (ownerPlayerStep.writer.publication.failedPlayerStep == null) {
            ownerPlayerStep.writer.publication.failedPlayerStep = ownerPlayerStep;
        }
        FoodWeaponSourceLedger.revokeOwnerWriterSelection(ownerPlayerStep.writer.publication, ownerPlayerStep.selection);
        try {
            ownerPlayerStep.selection.originalFailed(ownerPlayerStep.observation, throwable);
        }
        finally {
            FoodWeaponSourceLedger.closeOwnerPlayerStep(ownerPlayerStep);
        }
    }

    public static synchronized Throwable ownerPlayerOriginalFailure(OwnerPlayerStep ownerPlayerStep) {
        return ownerPlayerStep == null || ownerPlayerStep.writer.publication.thread != Thread.currentThread() ? null : ownerPlayerStep.observation.original;
    }

    public static synchronized void ownerRepairOriginalFailed(OwnerRepairObservation ownerRepairObservation, Throwable throwable) {
        if (ownerRepairObservation == null || ownerRepairObservation.publication.thread != Thread.currentThread()) {
            return;
        }
        if (ownerRepairObservation.publication.failedRepairObservation == null) {
            ownerRepairObservation.publication.failedRepairObservation = ownerRepairObservation;
        }
        FoodWeaponSourceLedger.revokeOwnerWriterSelection(ownerRepairObservation.publication, ownerRepairObservation.selection);
        try {
            ownerRepairObservation.selection.originalFailed(ownerRepairObservation.observation, throwable);
        }
        finally {
            FoodWeaponSourceLedger.closeOwnerRepairObservation(ownerRepairObservation);
        }
    }

    public static synchronized Throwable ownerRepairOriginalFailure(OwnerRepairObservation ownerRepairObservation) {
        return ownerRepairObservation == null || ownerRepairObservation.publication.thread != Thread.currentThread() ? null : ownerRepairObservation.observation.original;
    }

    public static synchronized Throwable ownerPlayerObservationFailure(OwnerPlayerStep ownerPlayerStep, int n) {
        return ownerPlayerStep == null || ownerPlayerStep.writer.publication.thread != Thread.currentThread() ? null : ownerPlayerStep.observation.failure(n);
    }

    static synchronized OwnerWriterStep ownerFailedWriterStep(OwnerWriterPublication ownerWriterPublication) {
        return ownerWriterPublication == null || ownerWriterPublication.thread != Thread.currentThread() ? null : ownerWriterPublication.failedWriterStep;
    }

    static synchronized OwnerPlayerStep ownerFailedPlayerStep(OwnerWriterPublication ownerWriterPublication) {
        return ownerWriterPublication == null || ownerWriterPublication.thread != Thread.currentThread() ? null : ownerWriterPublication.failedPlayerStep;
    }

    static synchronized OwnerRepairObservation ownerFailedRepairObservation(OwnerWriterPublication ownerWriterPublication) {
        return ownerWriterPublication == null || ownerWriterPublication.thread != Thread.currentThread() ? null : ownerWriterPublication.failedRepairObservation;
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    static synchronized boolean ownerRepairAcceptedIntentLive(Object object, OwnerFieldExpectedAuthority.Prepared prepared) {
        if (!(object instanceof OwnerWriterPublication)) return false;
        OwnerWriterPublication ownerWriterPublication = (OwnerWriterPublication)object;
        if (ownerWriterPublication.fieldPreparation != prepared) return false;
        if (prepared == null) return false;
        if (prepared.issuer != ownerWriterPublication) return false;
        if (!FoodWeaponSourceLedger.originalPublicationRootLive(ownerWriterPublication)) return false;
        return true;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    static boolean ownerRepairScopedWeaponLive(UUID uUID) {
        Class<FoodWeaponSourceLedger> clazz = FoodWeaponSourceLedger.class;
        synchronized (FoodWeaponSourceLedger.class) {
            NativeHurtScope nativeHurtScope = PRIVATE_NATIVE_HURT.get();
            if (nativeHurtScope == null || nativeHurtScope.thread != Thread.currentThread() || nativeHurtScope.closed || !nativeHurtScope.snapshot.m_128403_("WeaponId") || !nativeHurtScope.snapshot.m_128342_("WeaponId").equals(uUID)) {
                // ** MonitorExit[var2_1] (shouldn't be in output)
                return false;
            }
            // ** MonitorExit[var2_1] (shouldn't be in output)
            if (!OwnerRepairGenerationAuthority.snapshotAllowed(nativeHurtScope.snapshot)) {
                return false;
            }
            clazz = FoodWeaponSourceLedger.class;
            synchronized (FoodWeaponSourceLedger.class) {
                // ** MonitorExit[var2_1] (shouldn't be in output)
                return PRIVATE_NATIVE_HURT.get() == nativeHurtScope && !nativeHurtScope.closed;
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    static boolean ownerRepairScopedWeaponLive(UUID uUID, CompoundTag compoundTag, ServerPlayer serverPlayer) {
        if (!(uUID != null && compoundTag != null && serverPlayer != null && FoodWeaponSourceLedger.server(serverPlayer).m_18695_() && compoundTag.m_128403_("Owner") && compoundTag.m_128342_("Owner").equals(serverPlayer.m_20148_()) && compoundTag.m_128403_("WeaponId") && compoundTag.m_128342_("WeaponId").equals(uUID))) {
            return false;
        }
        OriginalInteractionBinding originalInteractionBinding = FoodWeaponSourceLedger.currentInteraction(compoundTag, serverPlayer);
        if (originalInteractionBinding == null) {
            return false;
        }
        InteractionRoot interactionRoot = originalInteractionBinding.root;
        Class<FoodWeaponSourceLedger> clazz = FoodWeaponSourceLedger.class;
        synchronized (FoodWeaponSourceLedger.class) {
            if (originalInteractionBinding.closed || interactionRoot.player != serverPlayer || interactionRoot.thread != Thread.currentThread() || interactionRoot.closed || interactionRoot.retired || interactionRoot.readCut.closed || interactionRoot.current != originalInteractionBinding || interactionRoot.server != FoodWeaponSourceLedger.server(serverPlayer) || SERVERS.get(interactionRoot.server) != interactionRoot.serverState || interactionRoot.serverState.closed || interactionRoot.ownerState.closed || interactionRoot.ownerState.current != interactionRoot || interactionRoot.serverState.ownerEpochs.get(serverPlayer.m_20148_()) != interactionRoot.ownerState || interactionRoot.serverState.epoch != interactionRoot.serverEpoch || interactionRoot.ownerState.epoch != interactionRoot.ownerEpoch || interactionRoot.projectionSlot < 0 || interactionRoot.projectionSlot >= ACTIVE_ROOT_PROJECTIONS.length || ACTIVE_ROOT_PROJECTIONS[interactionRoot.projectionSlot] != interactionRoot || interactionRoot.expectation == null || interactionRoot.expectation.closed) {
                // ** MonitorExit[var9_5] (shouldn't be in output)
                return false;
            }
            WeaponExpectation weaponExpectation = interactionRoot.expectation;
            ItemStack itemStack = weaponExpectation.actual;
            Object object = weaponExpectation.repairGeneration;
            OwnerLineageRevocationStore.Lease lease = OwnerFullRepairGuard.lease(itemStack);
            if (lease == null || !lease.weapon.equals(uUID) || !lease.owner.equals(serverPlayer.m_20148_())) {
                // ** MonitorExit[var9_5] (shouldn't be in output)
                return false;
            }
            // ** MonitorExit[var9_5] (shouldn't be in output)
            if (!(OwnerRepairGenerationAuthority.matches(itemStack, object) && OwnerLineageRevocationStore.liveLease(lease) && FoodWeaponSourceLedger.originalBindingAncestryLive(originalInteractionBinding))) {
                return false;
            }
            clazz = FoodWeaponSourceLedger.class;
            synchronized (FoodWeaponSourceLedger.class) {
                // ** MonitorExit[var9_5] (shouldn't be in output)
                return !originalInteractionBinding.closed && interactionRoot.player == serverPlayer && interactionRoot.thread == Thread.currentThread() && !interactionRoot.closed && !interactionRoot.retired && !interactionRoot.readCut.closed && interactionRoot.current == originalInteractionBinding && interactionRoot.server == FoodWeaponSourceLedger.server(serverPlayer) && interactionRoot.server.m_18695_() && SERVERS.get(interactionRoot.server) == interactionRoot.serverState && !interactionRoot.serverState.closed && !interactionRoot.ownerState.closed && interactionRoot.ownerState.current == interactionRoot && interactionRoot.serverState.ownerEpochs.get(serverPlayer.m_20148_()) == interactionRoot.ownerState && interactionRoot.serverState.epoch == interactionRoot.serverEpoch && interactionRoot.ownerState.epoch == interactionRoot.ownerEpoch && interactionRoot.projectionSlot >= 0 && interactionRoot.projectionSlot < ACTIVE_ROOT_PROJECTIONS.length && ACTIVE_ROOT_PROJECTIONS[interactionRoot.projectionSlot] == interactionRoot && interactionRoot.expectation == weaponExpectation && !weaponExpectation.closed && weaponExpectation.repairGeneration == object && originalInteractionBinding.snapshot != null && originalInteractionBinding.snapshot.equals((Object)compoundTag) && lease.weapon.equals(uUID) && lease.owner.equals(serverPlayer.m_20148_()) && OwnerRepairGenerationAuthority.identityMatches(itemStack, object, lease) && OwnerFullRepairGuard.lease(itemStack) == lease;
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    static OwnerRepairGenerationAuthority.Intent prepareOwnerStatueRepair(OwnerWriterPublication ownerWriterPublication, ItemStack itemStack, int n) {
        Class<FoodWeaponSourceLedger> clazz = FoodWeaponSourceLedger.class;
        synchronized (FoodWeaponSourceLedger.class) {
            if (!FoodWeaponSourceLedger.writerCaller(FoodWeaponRepair.class) || ownerWriterPublication == null || ownerWriterPublication.thread != Thread.currentThread() || ownerWriterPublication.expectation.actual != itemStack || ownerWriterPublication.fieldPreparation == null || !OwnerFieldExpectedAuthority.covered(ownerWriterPublication.fieldPreparation)) {
                // ** MonitorExit[var5_3] (shouldn't be in output)
                return null;
            }
            OwnerFieldExpectedAuthority.Prepared prepared = ownerWriterPublication.fieldPreparation;
            byte[] byArray = ownerWriterPublication.expectation.cursor.expected;
            // ** MonitorExit[var5_3] (shouldn't be in output)
            return OwnerRepairGenerationAuthority.prepare(ownerWriterPublication, itemStack, prepared, byArray, n);
        }
    }

    static void ownerStatueRepairEntered(OwnerRepairGenerationAuthority.Intent intent, ItemStack itemStack) {
        OwnerRepairGenerationAuthority.entered(intent, itemStack);
    }

    static void ownerStatueRepairFieldsObserved(OwnerRepairGenerationAuthority.Intent intent, ItemStack itemStack, int n, byte[] byArray) {
        OwnerRepairGenerationAuthority.observed(intent, itemStack, n, byArray);
    }

    static boolean ownerStatueRepairCompleted(OwnerRepairGenerationAuthority.Intent intent, ItemStack itemStack, boolean bl) {
        return OwnerRepairGenerationAuthority.completed(intent, itemStack, bl);
    }

    static void ownerStatueRepairFailed(OwnerRepairGenerationAuthority.Intent intent, Throwable throwable) {
        OwnerRepairGenerationAuthority.failed(intent, throwable);
    }

    private static boolean evergaleRawCaller(Class<?> clazz, String string) {
        return StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).walk(stream -> stream.filter(stackFrame -> stackFrame.getDeclaringClass() != FoodWeaponSourceLedger.class).findFirst().map(stackFrame -> stackFrame.getDeclaringClass() == clazz && stackFrame.getMethodName().equals(string)).orElse(false));
    }

    public static synchronized NativeEvergaleAction nativeEvergaleActionEntered(NativeEvergaleBridge.RawAction rawAction) {
        boolean bl;
        if (rawAction == null) {
            return null;
        }
        NativeEvergaleAction nativeEvergaleAction = EVERGALE_ACTIONS.get(rawAction);
        if (nativeEvergaleAction != null) {
            nativeEvergaleAction.rejected = true;
            nativeEvergaleAction.refusal = "DUPLICATE_RAW_ACTION";
            return nativeEvergaleAction;
        }
        if (EVERGALE_ACTIONS.size() == 4096) {
            return null;
        }
        NativeEvergaleAction nativeEvergaleAction2 = new NativeEvergaleAction(rawAction);
        EVERGALE_ACTIONS.put(rawAction, nativeEvergaleAction2);
        nativeEvergaleAction2.rejected = true;
        nativeEvergaleAction2.refusal = "FULL_S01_SWORD_SOURCE_AND_PAID_ISSUER_NOT_WIRED";
        boolean bl2 = bl = rawAction.site == NativeEvergaleBridge.Site.SWING ? FoodWeaponSourceLedger.evergaleRawCaller(NativeEvergaleBridge.class, "swing") : FoodWeaponSourceLedger.evergaleRawCaller(NativeEvergaleBridge.class, "use");
        if (!bl || rawAction.originalThread != nativeEvergaleAction2.thread || rawAction.closed || rawAction.originalEntered || rawAction.nested || rawAction.previous != null || rawAction.originalStack == null || rawAction.originalActor == null || rawAction.originalReceiver == null || !NativeEvergaleBridge.isEvergale(rawAction.originalReceiver) || rawAction.site == NativeEvergaleBridge.Site.SWING && rawAction.originalHand != null) {
            nativeEvergaleAction2.refusal = "RAW_ACTION_BOUNDARY_OR_IDENTITY_REJECTED";
        }
        return nativeEvergaleAction2;
    }

    public static synchronized void nativeEvergaleActionClosed(NativeEvergaleAction nativeEvergaleAction, NativeEvergaleBridge.RawAction rawAction) {
        boolean bl;
        if (nativeEvergaleAction == null || nativeEvergaleAction.thread != Thread.currentThread() || nativeEvergaleAction.closed) {
            return;
        }
        nativeEvergaleAction.closed = true;
        nativeEvergaleAction.rejected = true;
        boolean bl2 = bl = EVERGALE_ACTIONS.get(nativeEvergaleAction.original) == nativeEvergaleAction;
        if (bl) {
            EVERGALE_ACTIONS.remove(nativeEvergaleAction.original);
        }
        if (nativeEvergaleAction.original != rawAction || !bl || !FoodWeaponSourceLedger.evergaleRawCaller(NativeEvergaleBridge.class, "close")) {
            nativeEvergaleAction.refusal = "FOREIGN_SAVED_CLOSE";
            return;
        }
        nativeEvergaleAction.originalFailure = rawAction.originalFailure;
        nativeEvergaleAction.observationFailure = rawAction.observationFailure;
        if (!rawAction.closed || !rawAction.originalEntered || !rawAction.originalReturned || rawAction.originalFailure != null || rawAction.observationFailure != null || rawAction.nested || rawAction.site != NativeEvergaleBridge.Site.SWING) {
            return;
        }
        for (NativeEvergaleBridge.RawSpawn rawSpawn : rawAction.spawns) {
            AbstractArrow abstractArrow;
            Object object;
            if (rawSpawn == null || rawSpawn.action != rawAction || !rawSpawn.originalEntered || !rawSpawn.originalReturned || !rawSpawn.originalReturn || rawSpawn.originalFailure != null || rawSpawn.fieldFailure != null || !rawSpawn.fieldObservedSame || rawSpawn.originalLevel != rawAction.originalLevel || !((object = rawSpawn.originalEntity) instanceof AbstractArrow) || !NativeEvergaleBridge.isAirStrike(abstractArrow = (AbstractArrow)object) || rawSpawn.carry == null || rawSpawn.carry.action != rawAction || rawSpawn.carry.spawn != rawSpawn || (object = rawSpawn.carry.owner) == null || object.action != rawAction || object.originalReceiver != rawSpawn.originalEntity || object.originalOwner != rawAction.originalActor || !object.originalEntered || !object.originalReturned || object.originalFailure != null || !rawAction.owners.contains(object)) continue;
            for (NativeEvergaleBridge.RawCooldown rawCooldown : rawAction.cooldowns) {
                if (rawCooldown == null || rawCooldown.action != rawAction || !rawCooldown.originalEntered || !rawCooldown.originalReturned || rawCooldown.originalFailure != null || rawCooldown.originalItem != rawAction.originalReceiver || rawCooldown.originalReceiver == null || rawCooldown.originalTicks <= 0) continue;
                nativeEvergaleAction.witnesses.add(new NativeEvergaleSpawnWitness(rawSpawn, (NativeEvergaleBridge.RawOwnerAssignment)object, rawCooldown));
            }
        }
    }

    public static synchronized NativeEvergaleInteraction nativeEvergaleImpactEntered(NativeEvergaleImpact.RawImpact rawImpact) {
        if (rawImpact == null) {
            return null;
        }
        NativeEvergaleInteraction nativeEvergaleInteraction = EVERGALE_IMPACTS.get(rawImpact);
        if (nativeEvergaleInteraction != null) {
            nativeEvergaleInteraction.rejected = true;
            nativeEvergaleInteraction.refusal = "DUPLICATE_RAW_IMPACT";
            return nativeEvergaleInteraction;
        }
        if (EVERGALE_IMPACTS.size() == 4096) {
            return null;
        }
        NativeEvergaleInteraction nativeEvergaleInteraction2 = new NativeEvergaleInteraction(rawImpact);
        EVERGALE_IMPACTS.put(rawImpact, nativeEvergaleInteraction2);
        nativeEvergaleInteraction2.rejected = true;
        nativeEvergaleInteraction2.refusal = "OWNER_ACCEPTED_PAID_FIELD_AND_MELEE_PROJECTION_NOT_WIRED";
        if (!FoodWeaponSourceLedger.evergaleRawCaller(NativeEvergaleImpact.class, "hurt") || rawImpact.originalThread != nativeEvergaleInteraction2.thread || rawImpact.closed || rawImpact.originalEntered || rawImpact.originalCarrier == null || rawImpact.originalTarget == null || rawImpact.originalSource == null || !Float.isFinite(rawImpact.originalAmount) || rawImpact.originalAmount <= 0.0f || Float.floatToRawIntBits(rawImpact.originalAmount) != rawImpact.originalAmountBits || !NativeEvergaleBridge.isAirStrike(rawImpact.originalCarrier)) {
            nativeEvergaleInteraction2.refusal = "RAW_IMPACT_BOUNDARY_OR_IDENTITY_REJECTED";
        }
        return nativeEvergaleInteraction2;
    }

    public static synchronized void nativeEvergaleImpactClosed(NativeEvergaleInteraction nativeEvergaleInteraction, NativeEvergaleImpact.RawImpact rawImpact) {
        boolean bl;
        if (nativeEvergaleInteraction == null || nativeEvergaleInteraction.thread != Thread.currentThread() || nativeEvergaleInteraction.closed) {
            return;
        }
        nativeEvergaleInteraction.closed = true;
        nativeEvergaleInteraction.rejected = true;
        boolean bl2 = bl = EVERGALE_IMPACTS.get(nativeEvergaleInteraction.original) == nativeEvergaleInteraction;
        if (bl) {
            EVERGALE_IMPACTS.remove(nativeEvergaleInteraction.original);
        }
        if (nativeEvergaleInteraction.original != rawImpact || !bl || !FoodWeaponSourceLedger.evergaleRawCaller(NativeEvergaleImpact.class, "hurt")) {
            nativeEvergaleInteraction.refusal = "FOREIGN_SAVED_IMPACT_CLOSE";
            return;
        }
        nativeEvergaleInteraction.originalFailure = rawImpact.originalFailure;
        nativeEvergaleInteraction.observationFailure = rawImpact.observationFailure;
        if (!(rawImpact.closed && rawImpact.originalEntered && rawImpact.originalReturned)) {
            nativeEvergaleInteraction.refusal = "INCOMPLETE_ORIGINAL_IMPACT";
        }
    }

    private static synchronized long nextOwnerFieldGeneration() {
        if (ownerFieldGeneration == Long.MAX_VALUE) {
            throw new IllegalStateException("Owner field generation exhausted");
        }
        return ++ownerFieldGeneration;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    static boolean ownerFieldPreparedEpochs(Object object, MinecraftServer minecraftServer, Object object2, Object object3) {
        Class<FoodWeaponSourceLedger> clazz = FoodWeaponSourceLedger.class;
        synchronized (FoodWeaponSourceLedger.class) {
            OwnerWriterPublication ownerWriterPublication;
            block12: {
                block11: {
                    if (!(object instanceof OwnerWriterPublication)) break block11;
                    ownerWriterPublication = (OwnerWriterPublication)object;
                    if (!ownerWriterPublication.closed && ownerWriterPublication.thread == Thread.currentThread()) break block12;
                }
                // ** MonitorExit[var8_4] (shouldn't be in output)
                return false;
            }
            OwnerWriterPublication ownerWriterPublication2 = ownerWriterPublication;
            if (!FoodWeaponSourceLedger.preparedEpochIdentity(ownerWriterPublication2, minecraftServer, object2, object3)) {
                // ** MonitorExit[var8_4] (shouldn't be in output)
                return false;
            }
            WeaponExpectation weaponExpectation = ownerWriterPublication2.expectation;
            Object object4 = weaponExpectation.repairGeneration;
            OwnerLineageRevocationStore.Lease lease = OwnerFullRepairGuard.lease(weaponExpectation.actual);
            // ** MonitorExit[var8_4] (shouldn't be in output)
            if (!OwnerRepairGenerationAuthority.matches(weaponExpectation.actual, object4) || !OwnerFullRepairGuard.permits(weaponExpectation.actual)) {
                return false;
            }
            clazz = FoodWeaponSourceLedger.class;
            synchronized (FoodWeaponSourceLedger.class) {
                // ** MonitorExit[var8_4] (shouldn't be in output)
                return !ownerWriterPublication2.closed && ownerWriterPublication2.thread == Thread.currentThread() && ownerWriterPublication2.expectation == weaponExpectation && !weaponExpectation.closed && weaponExpectation.repairGeneration == object4 && OwnerRepairGenerationAuthority.identityMatches(weaponExpectation.actual, object4, lease) && FoodWeaponSourceLedger.preparedEpochIdentity(ownerWriterPublication2, minecraftServer, object2, object3);
            }
        }
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    static synchronized OwnerFieldExpectedAuthority.Prepared ownerFieldPrepared(Object object) {
        if (!(object instanceof OwnerWriterPublication)) return null;
        OwnerWriterPublication ownerWriterPublication = (OwnerWriterPublication)object;
        if (ownerWriterPublication.closed) return null;
        if (ownerWriterPublication.thread != Thread.currentThread()) return null;
        OwnerFieldExpectedAuthority.Prepared prepared = ownerWriterPublication.fieldPreparation;
        return prepared;
    }

    static boolean ownerNativeLeafAccepted(OwnerFieldExpectedAuthority.Prepared prepared, Object object, Object object2, Object object3) {
        block3: {
            block2: {
                Object object4;
                if (prepared == null || !((object4 = prepared.issuer) instanceof OwnerWriterPublication)) break block2;
                OwnerWriterPublication ownerWriterPublication = (OwnerWriterPublication)object4;
                if (ownerWriterPublication.fieldPreparation == prepared && ownerWriterPublication.thread == Thread.currentThread() && !ownerWriterPublication.closed && ownerWriterPublication.receipt != null && object3 instanceof OwnerNativeLeafFields) break block3;
            }
            return false;
        }
        return FoodWeaponFinalDamage.acceptsOwnerReserveLeaf(prepared, object, object2, object3);
    }

    public static OwnerTimerSemanticAuthority.LeafObservation ownerTimerLeafEntered(Object object, Object object2, Object object3) {
        FoodWeaponOriginalSequence.Session session;
        FoodWeaponHitPlan.FinalEffectsCommit finalEffectsCommit;
        block7: {
            block6: {
                Object object4;
                block5: {
                    block4: {
                        if (!FoodWeaponSourceLedger.writerCaller(FoodWeaponHitPlan.class) || !(object instanceof FoodWeaponHitPlan.FinalEffectsCommit)) break block4;
                        finalEffectsCommit = (FoodWeaponHitPlan.FinalEffectsCommit)object;
                        if (object2 instanceof FoodWeaponFinalDamage.ChildHurtBridge) break block5;
                    }
                    return null;
                }
                FoodWeaponFinalDamage.ChildHurtBridge childHurtBridge = (FoodWeaponFinalDamage.ChildHurtBridge)object2;
                session = FoodWeaponFinalDamage.originalHitPlanSequence(finalEffectsCommit, childHurtBridge);
                if (session == null || !session.original.atLeaf(object3) || !((object4 = session.lineage.issuer) instanceof OwnerWriterPublication)) break block6;
                OwnerWriterPublication ownerWriterPublication = (OwnerWriterPublication)object4;
                if (ownerWriterPublication.fieldSequenceBinding != null && ownerWriterPublication.fieldSequenceBinding.sequence == session && ownerWriterPublication.fieldPreparation != null && ownerWriterPublication.fieldPreparation.lineage == session.lineage && OwnerFieldExpectedAuthority.unchanged(ownerWriterPublication.fieldPreparation)) break block7;
            }
            return null;
        }
        return OwnerTimerSemanticAuthority.enter(session, finalEffectsCommit, object3);
    }

    public static OwnerNativeSemanticAuthority.Admission ownerNativePutEntered(Object object, Object object2, Object object3, Tag tag) {
        OwnerWriterPublication ownerWriterPublication;
        FoodWeaponOriginalSequence.Session session;
        block7: {
            block6: {
                Object object4;
                FoodWeaponHitPlan.FinalEffectsCommit finalEffectsCommit;
                block5: {
                    block4: {
                        if (!FoodWeaponSourceLedger.writerCaller(FoodWeaponHitPlan.class) || !(object instanceof FoodWeaponHitPlan.FinalEffectsCommit)) break block4;
                        finalEffectsCommit = (FoodWeaponHitPlan.FinalEffectsCommit)object;
                        if (object2 instanceof FoodWeaponFinalDamage.ChildHurtBridge) break block5;
                    }
                    return null;
                }
                FoodWeaponFinalDamage.ChildHurtBridge childHurtBridge = (FoodWeaponFinalDamage.ChildHurtBridge)object2;
                session = FoodWeaponFinalDamage.originalHitPlanSequence(finalEffectsCommit, childHurtBridge);
                if (session == null || !session.original.atLeaf(object3) || !((object4 = session.lineage.issuer) instanceof OwnerWriterPublication)) break block6;
                ownerWriterPublication = (OwnerWriterPublication)object4;
                if (ownerWriterPublication.fieldSequenceBinding != null && ownerWriterPublication.fieldSequenceBinding.sequence == session && ownerWriterPublication.fieldPreparation != null && ownerWriterPublication.fieldPreparation.lineage == session.lineage && OwnerFieldExpectedAuthority.unchanged(ownerWriterPublication.fieldPreparation)) break block7;
            }
            return null;
        }
        return OwnerNativeSemanticAuthority.admit(session, ownerWriterPublication.fieldPreparation.nativePuts.get(object3), object3, tag);
    }

    public static void ownerOriginalPublicationAssigned(Object object, Object object2) {
        OwnerWriterPublication ownerWriterPublication;
        block8: {
            block7: {
                boolean bl = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).walk(stream -> stream.skip(1L).findFirst().map(stackFrame -> stackFrame.getDeclaringClass() == FoodWeaponFinalDamage.class && stackFrame.getMethodName().contains("ftboss$ownerOriginalPublicationAssigned")).orElse(false));
                if (!bl || !(object2 instanceof OwnerWriterPublication)) break block7;
                ownerWriterPublication = (OwnerWriterPublication)object2;
                if (!ownerWriterPublication.fieldAdmissionAttempted && ownerWriterPublication.thread == Thread.currentThread() && ownerWriterPublication.binding.root.writerPublication == ownerWriterPublication && !ownerWriterPublication.closed) break block8;
            }
            return;
        }
        ownerWriterPublication.fieldAdmissionAttempted = true;
        OwnerFieldExpectedAuthority.Prepared prepared = FoodWeaponSourceLedger.prepareOwnerFieldExpectedSequence(ownerWriterPublication, object);
        if (prepared == null) {
            return;
        }
        try {
            OwnerFieldExpectedAuthority.quoteTimers(prepared, object, ownerWriterPublication.expectation.actual, ownerWriterPublication.binding.root.player, ownerWriterPublication.binding.source);
        }
        catch (Throwable throwable) {
            OwnerFieldExpectedAuthority.unsupported(prepared);
        }
        if (!FoodWeaponSourceLedger.sealOwnerFieldExpectedSequence(ownerWriterPublication, prepared)) {
            return;
        }
        FoodWeaponSourceLedger.bindOwnerFieldExpectedSequence(ownerWriterPublication, OwnerFieldExpectedAuthority.session(prepared), OwnerFieldExpectedAuthority.coverage(prepared));
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    static OwnerFieldExpectedAuthority.Prepared prepareOwnerFieldExpectedSequence(OwnerWriterPublication ownerWriterPublication, Object object) {
        if (!FoodWeaponSourceLedger.writerCaller(FoodWeaponFinalDamage.class) || ownerWriterPublication == null || ownerWriterPublication.thread != Thread.currentThread() || ownerWriterPublication.fieldSequenceBinding != null || ownerWriterPublication.fieldPreparation != null || ownerWriterPublication.closed || ownerWriterPublication.expectation.closed || object == null || ownerWriterPublication.binding.closed || ownerWriterPublication.binding.root.closed || ownerWriterPublication.binding.root.retired) {
            return null;
        }
        InteractionRoot interactionRoot = ownerWriterPublication.binding.root;
        if (!FoodWeaponSourceLedger.projectedCurrentBindingLive(ownerWriterPublication.binding)) {
            return null;
        }
        Object object2 = FoodWeaponSourceLedger.class;
        synchronized (FoodWeaponSourceLedger.class) {
            Entity entity;
            Object object3;
            if (ownerWriterPublication.closed || ownerWriterPublication.thread != Thread.currentThread() || ownerWriterPublication.binding.closed || ownerWriterPublication.fieldSequenceBinding != null || ownerWriterPublication.fieldPreparation != null || interactionRoot.thread != Thread.currentThread() || interactionRoot.closed || interactionRoot.retired || interactionRoot.readCut.closed || interactionRoot.current != ownerWriterPublication.binding || interactionRoot.writerPublication != ownerWriterPublication || interactionRoot.expectation != ownerWriterPublication.expectation || ownerWriterPublication.expectation.closed || interactionRoot.serverState.closed || interactionRoot.ownerState.closed || interactionRoot.ownerState.current != interactionRoot || !interactionRoot.server.m_18695_() || SERVERS.get(interactionRoot.server) != interactionRoot.serverState || interactionRoot.serverState.ownerEpochs.get(interactionRoot.player.m_20148_()) != interactionRoot.ownerState || interactionRoot.serverState.epoch != interactionRoot.serverEpoch || interactionRoot.ownerState.epoch != interactionRoot.ownerEpoch || interactionRoot.projectionSlot < 0 || interactionRoot.projectionSlot >= ACTIVE_ROOT_PROJECTIONS.length || ACTIVE_ROOT_PROJECTIONS[interactionRoot.projectionSlot] != interactionRoot || !OwnerRepairGenerationAuthority.identityMatches(ownerWriterPublication.expectation.actual, ownerWriterPublication.expectation.repairGeneration, OwnerFullRepairGuard.lease(ownerWriterPublication.expectation.actual))) {
                // ** MonitorExit[var3_3] (shouldn't be in output)
                return null;
            }
            // ** MonitorExit[var3_3] (shouldn't be in output)
            object2 = FoodWeaponOriginalSequence.ownerLineage(ownerWriterPublication.binding.root, ownerWriterPublication.expectation.cursor.position(), ownerWriterPublication, ownerWriterPublication.receipt, ownerWriterPublication.binding.childCall, object, ownerWriterPublication.fieldGeneration);
            DamageSource damageSource = ownerWriterPublication.binding.source;
            if (damageSource instanceof OwnerFieldSourceAccess) {
                object3 = (OwnerFieldSourceAccess)damageSource;
                v0 = object3.ftboss$ownerFieldDirectEntity();
            } else {
                v0 = entity = null;
            }
            if (!(ownerWriterPublication.binding.source instanceof OwnerFieldSourceAccess)) {
                return null;
            }
            ownerWriterPublication.fieldPreparation = object3 = OwnerFieldExpectedAuthority.prepare(ownerWriterPublication, (Entity)ownerWriterPublication.binding.root.player, entity, (FoodWeaponOriginalSequence.Lineage)object2, ownerWriterPublication.expectation.cursor.expected, ownerWriterPublication.expectation.actual, ownerWriterPublication.binding.source, ownerWriterPublication.binding.root.server, ownerWriterPublication.binding.root.serverEpoch, ownerWriterPublication.binding.root.ownerEpoch);
            return object3;
        }
    }

    static boolean sealOwnerFieldExpectedSequence(OwnerWriterPublication ownerWriterPublication, OwnerFieldExpectedAuthority.Prepared prepared) {
        return FoodWeaponSourceLedger.writerCaller(FoodWeaponFinalDamage.class) && ownerWriterPublication != null && ownerWriterPublication.thread == Thread.currentThread() && ownerWriterPublication.fieldPreparation == prepared && OwnerFieldExpectedAuthority.seal(prepared);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    static boolean bindOwnerFieldExpectedSequence(OwnerWriterPublication ownerWriterPublication, FoodWeaponOriginalSequence.Session session, FoodWeaponOriginalSequence.CoverageReceipt coverageReceipt) {
        OwnerFieldSequenceBinding ownerFieldSequenceBinding;
        if (!FoodWeaponSourceLedger.writerCaller(FoodWeaponFinalDamage.class) || ownerWriterPublication == null || session == null || ownerWriterPublication.thread != Thread.currentThread() || ownerWriterPublication.fieldSequenceBinding != null || ownerWriterPublication.closed || ownerWriterPublication.binding.closed || ownerWriterPublication.binding.root.closed || ownerWriterPublication.binding.root.retired || ownerWriterPublication.binding.root.writerPublication != ownerWriterPublication) {
            return false;
        }
        OwnerFieldExpectedAuthority.Prepared prepared = ownerWriterPublication.fieldPreparation;
        if (prepared == null || OwnerFieldExpectedAuthority.session(prepared) != session || prepared.issuer != ownerWriterPublication) {
            return false;
        }
        FoodWeaponOriginalSequence.Lineage lineage = session.lineage;
        if (lineage.ownerRoot != ownerWriterPublication.binding.root || lineage.ownerSelection != ownerWriterPublication.expectation.cursor.position() || lineage.issuer != ownerWriterPublication || lineage.acceptedCallback != ownerWriterPublication.receipt || lineage.childScope != ownerWriterPublication.binding.childCall || lineage.generation != ownerWriterPublication.fieldGeneration) {
            return false;
        }
        if (!FoodWeaponExpectedTrace.same(FoodWeaponExpectedFieldsCodec.decodeD084(ownerWriterPublication.expectation.cursor.expected), session.expected.before().weapon)) {
            return false;
        }
        ownerWriterPublication.fieldSequenceBinding = ownerFieldSequenceBinding = new OwnerFieldSequenceBinding(ownerWriterPublication, session, prepared);
        Throwable throwable = null;
        try {
            OwnerFieldExpectedAuthority.Prepared prepared2 = OwnerFieldExpectedAuthority.matching(ownerWriterPublication, session, coverageReceipt);
            ownerFieldSequenceBinding.extraAuthority = prepared2 == prepared && session.bindRegisteredCoverage(coverageReceipt);
        }
        catch (Throwable throwable2) {
            ownerFieldSequenceBinding.extraAuthority = false;
            throwable = throwable2;
        }
        finally {
            OwnerFieldExpectedAuthority.bound(prepared);
        }
        if (!(ownerFieldSequenceBinding.extraAuthority || throwable == null && OwnerFieldExpectedAuthority.pending(prepared))) {
            session.interferenceObserved(lineage.ownerSelection, lineage.generation, throwable);
        }
        boolean bl = false;
        try {
            bl = FoodWeaponFinalDamage.bindOwnerOriginalSequence(ownerWriterPublication.receipt, session);
        }
        catch (Throwable throwable3) {
            ownerFieldSequenceBinding.extraAuthority = false;
            session.interferenceObserved(lineage.ownerSelection, lineage.generation, throwable3);
            bl = session.original.continuing(lineage.publication);
        }
        if (!bl) {
            ownerFieldSequenceBinding.extraAuthority = false;
            ownerFieldSequenceBinding.terminal = true;
            OwnerFieldExpectedAuthority.retire(prepared);
        }
        return bl;
    }

    static boolean tryInstallOwnerCoverage(FoodWeaponOriginalSequence.Session session) {
        Object object;
        if (session == null || !((object = session.lineage.issuer) instanceof OwnerWriterPublication)) {
            return false;
        }
        OwnerWriterPublication ownerWriterPublication = (OwnerWriterPublication)object;
        object = ownerWriterPublication.fieldSequenceBinding;
        if (object == null || ((OwnerFieldSequenceBinding)object).sequence != session || ((OwnerFieldSequenceBinding)object).publication != ownerWriterPublication || ((OwnerFieldSequenceBinding)object).terminal || ((OwnerFieldSequenceBinding)object).thread != Thread.currentThread() || ownerWriterPublication.closed || ownerWriterPublication.fieldPreparation != ((OwnerFieldSequenceBinding)object).prepared) {
            return false;
        }
        if (((OwnerFieldSequenceBinding)object).extraAuthority) {
            return session.extraAuthorityRetained();
        }
        FoodWeaponOriginalSequence.CoverageReceipt coverageReceipt = OwnerFieldExpectedAuthority.refreshCoverage(((OwnerFieldSequenceBinding)object).prepared);
        if (coverageReceipt == null) {
            return false;
        }
        ((OwnerFieldSequenceBinding)object).extraAuthority = session.bindRegisteredCoverage(coverageReceipt);
        return ((OwnerFieldSequenceBinding)object).extraAuthority;
    }

    static synchronized boolean ownerFieldOriginalContinues(OwnerWriterPublication ownerWriterPublication, FoodWeaponOriginalSequence.Session session) {
        if (ownerWriterPublication == null || session == null) {
            return false;
        }
        OwnerFieldSequenceBinding ownerFieldSequenceBinding = ownerWriterPublication.fieldSequenceBinding;
        return ownerFieldSequenceBinding != null && ownerFieldSequenceBinding.thread == Thread.currentThread() && !ownerFieldSequenceBinding.terminal && ownerFieldSequenceBinding.publication == ownerWriterPublication && ownerFieldSequenceBinding.sequence == session && session.lineage.issuer == ownerWriterPublication && session.original.continuing(session.lineage.publication);
    }

    static synchronized void retireOwnerFieldExtraAuthority(OwnerWriterPublication ownerWriterPublication, FoodWeaponOriginalSequence.Session session, Throwable throwable) {
        if (ownerWriterPublication == null || session == null) {
            return;
        }
        OwnerFieldSequenceBinding ownerFieldSequenceBinding = ownerWriterPublication.fieldSequenceBinding;
        if (ownerFieldSequenceBinding == null || ownerFieldSequenceBinding.thread != Thread.currentThread() || ownerFieldSequenceBinding.sequence != session) {
            return;
        }
        ownerFieldSequenceBinding.extraAuthority = false;
        session.interferenceObserved(session.lineage.ownerSelection, session.lineage.generation, throwable);
        if (ownerFieldSequenceBinding.selection != null) {
            ownerFieldSequenceBinding.selection.revoked = true;
        }
    }

    static synchronized void terminalOwnerFieldOriginal(OwnerWriterPublication ownerWriterPublication, FoodWeaponOriginalSequence.Session session) {
        if (ownerWriterPublication == null || session == null) {
            return;
        }
        OwnerFieldSequenceBinding ownerFieldSequenceBinding = ownerWriterPublication.fieldSequenceBinding;
        if (ownerFieldSequenceBinding == null || ownerFieldSequenceBinding.thread != Thread.currentThread() || ownerFieldSequenceBinding.sequence != session) {
            return;
        }
        ownerFieldSequenceBinding.terminal = true;
        ownerFieldSequenceBinding.extraAuthority = false;
        session.original.terminal();
        OwnerFieldExpectedAuthority.retire(ownerFieldSequenceBinding.prepared);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    static OwnerFullRepairGuard.Cut beginOwnerFullRepair(OwnerWriterPublication ownerWriterPublication, Object object, Object object2, ItemStack itemStack, int n) {
        OwnerFieldExpectedAuthority.Prepared prepared = null;
        byte[] byArray = null;
        Class<FoodWeaponSourceLedger> clazz = FoodWeaponSourceLedger.class;
        synchronized (FoodWeaponSourceLedger.class) {
            boolean bl;
            boolean bl2 = bl = ownerWriterPublication != null && ownerWriterPublication.thread == Thread.currentThread() && ownerWriterPublication.expectation.actual == itemStack;
            if (bl) {
                ownerWriterPublication.fullRepairAttempted = true;
            }
            if (bl && FoodWeaponSourceLedger.writerCaller(FoodWeaponHitPlan.class) && ownerWriterPublication.commit == object && ownerWriterPublication.leaf == object2 && ownerWriterPublication.fieldPreparation != null) {
                prepared = ownerWriterPublication.fieldPreparation;
                byArray = ownerWriterPublication.expectation.cursor.expected;
            }
            // ** MonitorExit[var8_7] (shouldn't be in output)
            clazz = prepared == null ? null : OwnerRepairGenerationAuthority.prepareReduction(ownerWriterPublication, itemStack, prepared, byArray, n);
            OwnerFullRepairGuard.Cut cut = OwnerFullRepairGuard.enter(itemStack, null, n);
            Class<FoodWeaponSourceLedger> clazz2 = FoodWeaponSourceLedger.class;
            synchronized (FoodWeaponSourceLedger.class) {
                boolean bl3;
                boolean bl4 = bl3 = bl && FoodWeaponSourceLedger.writerCaller(FoodWeaponHitPlan.class) && ownerWriterPublication.commit == object && ownerWriterPublication.leaf == object2;
                if (bl3) {
                    OwnerFullRepairGuard.planned(cut, clazz);
                }
                // ** MonitorExit[var11_11] (shouldn't be in output)
                if (!bl3) {
                    OwnerFullRepairGuard.close(cut);
                }
                return cut;
            }
        }
    }

    public static void ownerFullRepairFieldsObserved(OwnerFullRepairGuard.Cut cut, ItemStack itemStack, int n, byte[] byArray) {
        if (FoodWeaponSourceLedger.writerCaller(FoodWeaponHitPlan.class)) {
            OwnerFullRepairGuard.fieldsObserved(cut, itemStack, n, byArray);
        }
    }

    static synchronized boolean ownerFullRepairAttempted(OwnerWriterPublication ownerWriterPublication) {
        return ownerWriterPublication != null && ownerWriterPublication.thread == Thread.currentThread() && ownerWriterPublication.fullRepairAttempted;
    }

    public static void ownerFullRepairReadReturned(OwnerFullRepairGuard.Cut cut, ItemStack itemStack, int n, int n2) {
        OwnerFullRepairGuard.readReturned(cut, itemStack, n, n2);
    }

    public static void ownerFullRepairSetterEntered(OwnerFullRepairGuard.Cut cut, ItemStack itemStack, int n) {
        OwnerFullRepairGuard.setterEntered(cut, itemStack, n);
    }

    public static void ownerFullRepairReturned(OwnerFullRepairGuard.Cut cut) {
        OwnerFullRepairGuard.returned(cut);
    }

    public static void ownerFullRepairOriginalFailed(OwnerFullRepairGuard.Cut cut, Throwable throwable) {
        OwnerFullRepairGuard.failed(cut, throwable, false);
    }

    public static void ownerFullRepairObservationFailed(OwnerFullRepairGuard.Cut cut, Throwable throwable) {
        OwnerFullRepairGuard.failed(cut, throwable, true);
    }

    public static synchronized Throwable ownerFullRepairFailure(OwnerFullRepairGuard.Cut cut, boolean bl) {
        return OwnerFullRepairGuard.failure(cut, bl);
    }

    public static void closeOwnerFullRepair(OwnerFullRepairGuard.Cut cut) {
        OwnerFullRepairGuard.close(cut);
    }

    public static void ownerFullRepairObserveAlias(OwnerFullRepairGuard.Cut cut, Object object, Object object2, boolean bl) {
        if (cut == null) {
            return;
        }
        OwnerFullRepairGuard.alias(cut, object, object2, bl);
    }

    public static void ownerFullRepairObserveMutation(OwnerFullRepairGuard.Cut cut, Object object, int n) {
        if (cut == null) {
            return;
        }
        OwnerFullRepairGuard.changed(cut, object, n);
    }

    private static boolean originalRepairCaller() {
        return NATIVE_CALLER.walk(stream -> stream.filter(stackFrame -> stackFrame.getDeclaringClass() != FoodWeaponSourceLedger.class).findFirst().map(stackFrame -> stackFrame.getDeclaringClass().getNestHost() == FoodWeaponHitPlan.class && stackFrame.getDeclaringClass().getName().endsWith("$RepairLeaf") && stackFrame.getMethodName().startsWith("ftboss$ownerRepair")).orElse(false));
    }

    private static FoodWeaponExpectedTrace.Value originalRepairRawFields(ItemStack itemStack) {
        NativeExpectedFieldViews.Stack stack;
        ItemStack itemStack2 = itemStack;
        if (!(itemStack2 instanceof NativeExpectedFieldViews.Stack) || (stack = (NativeExpectedFieldViews.Stack)itemStack2).ftboss$rawStackCount() != 1 || stack.ftboss$rawStackCaps() != null || stack.ftboss$rawStackItem() == null) {
            return null;
        }
        itemStack2 = ForgeRegistries.ITEMS.getKey((Object)stack.ftboss$rawStackItem());
        if (itemStack2 == null) {
            return null;
        }
        CompoundTag compoundTag = stack.ftboss$rawStackTag();
        if (compoundTag == null) {
            return FoodWeaponExpectedTrace.compound(new String[]{"Count", "id"}, new FoodWeaponExpectedTrace.Value[]{FoodWeaponExpectedTrace.number(1, stack.ftboss$rawStackCount()), FoodWeaponExpectedTrace.string(itemStack2.toString())});
        }
        return FoodWeaponExpectedTrace.compound(new String[]{"Count", "id", "tag"}, new FoodWeaponExpectedTrace.Value[]{FoodWeaponExpectedTrace.number(1, stack.ftboss$rawStackCount()), FoodWeaponExpectedTrace.string(itemStack2.toString()), NativeExpectedRawFields.observe((Tag)compoundTag)});
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Unable to fully structure code
     */
    public static OriginalRepairCall ownerOriginalRepairEntered(Object var0, Object var1_1, Object var2_2, ItemStack var3_3) {
        if (!FoodWeaponSourceLedger.originalRepairCaller()) {
            return null;
        }
        if (!(var0 instanceof FoodWeaponHitPlan.FinalEffectsCommit)) ** GOTO lbl-1000
        var6_4 = (FoodWeaponHitPlan.FinalEffectsCommit)var0;
        if (var1_1 instanceof FoodWeaponFinalDamage.ChildHurtBridge) {
            var5_5 = (FoodWeaponFinalDamage.ChildHurtBridge)var1_1;
            v0 = FoodWeaponHitPlan.originalRepairFacts((FoodWeaponHitPlan.FinalEffectsCommit)var6_4, var5_5, var2_2);
        } else lbl-1000:
        // 2 sources

        {
            v0 = var4_6 = null;
        }
        if (!(var0 instanceof FoodWeaponHitPlan.FinalEffectsCommit)) ** GOTO lbl-1000
        var7_7 = (FoodWeaponHitPlan.FinalEffectsCommit)var0;
        if (var1_1 instanceof FoodWeaponFinalDamage.ChildHurtBridge) {
            var6_4 = (FoodWeaponFinalDamage.ChildHurtBridge)var1_1;
            v1 = FoodWeaponFinalDamage.originalHitPlanSequence((FoodWeaponHitPlan.FinalEffectsCommit)var7_7, (FoodWeaponFinalDamage.ChildHurtBridge)var6_4);
        } else lbl-1000:
        // 2 sources

        {
            v1 = null;
        }
        var5_5 = v1;
        var7_7 = FoodWeaponSourceLedger.class;
        synchronized (FoodWeaponSourceLedger.class) {
            if (var5_5 == null || !((var9_8 = var5_5.lineage.issuer) instanceof OwnerWriterPublication)) ** GOTO lbl-1000
            var8_9 = (OwnerWriterPublication)var9_8;
            if (var8_9.thread == Thread.currentThread() && !var8_9.closed && var8_9.fieldSequenceBinding != null && var8_9.fieldSequenceBinding.sequence == var5_5 && var4_6 != null && var4_6.weapon() == var3_3 && var5_5.original.atLeaf(var2_2)) {
                v2 = var8_9;
            } else lbl-1000:
            // 2 sources

            {
                v2 = null;
            }
            var6_4 = v2;
            // ** MonitorExit[var7_7] (shouldn't be in output)
            var7_7 = FoodWeaponSourceLedger.beginOwnerFullRepair((OwnerWriterPublication)var6_4, var0, var2_2, var3_3, var4_6 == null ? 0 : var4_6.reduction());
            return new OriginalRepairCall(var0, var1_1, var2_2, var3_3, var4_6, (OwnerFullRepairGuard.Cut)var7_7);
        }
    }

    public static synchronized void ownerOriginalRepairGetterReturned(OriginalRepairCall originalRepairCall, ItemStack itemStack, int n) {
        if (!FoodWeaponSourceLedger.originalRepairCaller() || originalRepairCall == null || originalRepairCall.closed || originalRepairCall.thread != Thread.currentThread() || originalRepairCall.actual != itemStack || originalRepairCall.phase != 0) {
            return;
        }
        originalRepairCall.getterFields = FoodWeaponSourceLedger.originalRepairRawFields(itemStack);
        originalRepairCall.getterValue = n;
        originalRepairCall.phase = 1;
    }

    public static void ownerOriginalRepairSetterEntered(OriginalRepairCall originalRepairCall, ItemStack itemStack, int n) {
        if (!FoodWeaponSourceLedger.originalRepairCaller() || originalRepairCall == null || originalRepairCall.closed || originalRepairCall.thread != Thread.currentThread()) {
            return;
        }
        FoodWeaponHitPlan.OriginalRepairFacts originalRepairFacts = originalRepairCall.facts;
        if (originalRepairCall.actual != itemStack || originalRepairCall.phase != 1 || originalRepairFacts == null || originalRepairFacts.weapon() != itemStack || !originalRepairFacts.originalReadReturned() || !originalRepairFacts.originalSetterEntered() || originalRepairFacts.originalDamage() != originalRepairCall.getterValue || originalRepairFacts.requestedDamage() != n || originalRepairFacts.observerFailure() != null || originalRepairFacts.originalFailure() != null) {
            OwnerFullRepairGuard.close(originalRepairCall.cut);
            originalRepairCall.closed = true;
            return;
        }
        OwnerFullRepairGuard.readReturned(originalRepairCall.cut, itemStack, originalRepairCall.getterValue, n);
        OwnerFullRepairGuard.fieldsValueObserved(originalRepairCall.cut, itemStack, 0, originalRepairCall.getterFields);
        OwnerFullRepairGuard.setterEntered(originalRepairCall.cut, itemStack, n);
        originalRepairCall.phase = 2;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    public static void ownerOriginalRepairSetterReturned(OriginalRepairCall originalRepairCall, ItemStack itemStack) {
        FoodWeaponExpectedTrace.Value value = null;
        Class<FoodWeaponSourceLedger> clazz = FoodWeaponSourceLedger.class;
        synchronized (FoodWeaponSourceLedger.class) {
            if (!FoodWeaponSourceLedger.originalRepairCaller() || originalRepairCall == null || originalRepairCall.closed || originalRepairCall.thread != Thread.currentThread()) {
                // ** MonitorExit[var4_3] (shouldn't be in output)
                return;
            }
            boolean bl = originalRepairCall.actual == itemStack && originalRepairCall.phase == 2 && originalRepairCall.facts.originalFailure() == null && originalRepairCall.facts.observerFailure() == null;
            // ** MonitorExit[var4_3] (shouldn't be in output)
            try {
                if (!bl) return;
                value = FoodWeaponSourceLedger.originalRepairRawFields(itemStack);
                OwnerFullRepairGuard.fieldsValueObserved(originalRepairCall.cut, itemStack, 1, value);
                OwnerFullRepairGuard.returned(originalRepairCall.cut);
                clazz = FoodWeaponSourceLedger.class;
                synchronized (FoodWeaponSourceLedger.class) {
                    originalRepairCall.phase = 3;
                    // ** MonitorExit[var4_3] (shouldn't be in output)
                    return;
                }
            }
            finally {
                try {
                    OwnerFullRepairGuard.close(originalRepairCall.cut);
                }
                finally {
                    clazz = FoodWeaponSourceLedger.class;
                    synchronized (FoodWeaponSourceLedger.class) {
                        originalRepairCall.closed = true;
                        // ** MonitorExit[var4_3] (shouldn't be in output)
                    }
                }
            }
        }
    }

    public static void ownerOriginalRepairFailed(OriginalRepairCall originalRepairCall, Throwable throwable, boolean bl) {
        if (!FoodWeaponSourceLedger.originalRepairCaller() || originalRepairCall == null || originalRepairCall.closed || originalRepairCall.thread != Thread.currentThread()) {
            return;
        }
        try {
            OwnerFullRepairGuard.failed(originalRepairCall.cut, throwable, !bl);
        }
        finally {
            originalRepairCall.closed = true;
        }
    }

    static OwnerRepairObservation beginOwnerRepairObservation(OwnerWriterPublication ownerWriterPublication, Object object, Object object2, ItemStack itemStack, int n, int n2, int n3) {
        FoodWeaponSourceLedger.beginOwnerFullRepair(ownerWriterPublication, object, object2, itemStack, n2);
        WriterPlanningCut writerPlanningCut = FoodWeaponSourceLedger.writerPlanningCut(ownerWriterPublication);
        if (!(FoodWeaponSourceLedger.writerCaller(FoodWeaponHitPlan.class) && FoodWeaponSourceLedger.ownerWriterSelectionLive(ownerWriterPublication) && FoodWeaponSourceLedger.writerPlanningUnchanged(ownerWriterPublication, writerPlanningCut) && writerPlanningCut.selection.commit == object && writerPlanningCut.selection.leaf == object2 && ownerWriterPublication.expectation.actual == itemStack && ownerWriterPublication.expectation.active == null && ownerWriterPublication.repairObservation == null && ownerWriterPublication.playerWriter == null && n3 == Math.max(0, n - n2))) {
            return null;
        }
        OwnerRepairObservation ownerRepairObservation = new OwnerRepairObservation(ownerWriterPublication, writerPlanningCut, itemStack, n, n2, n3);
        if (!FoodWeaponSourceLedger.ownerWriterSelectionLive(ownerWriterPublication) || !FoodWeaponSourceLedger.writerPlanningUnchanged(ownerWriterPublication, writerPlanningCut) || ownerWriterPublication.expectation.active != null || ownerWriterPublication.playerWriter != null || ownerWriterPublication.repairObservation != null) {
            return null;
        }
        ownerWriterPublication.repairObservation = ownerRepairObservation;
        return ownerRepairObservation;
    }

    public static synchronized void ownerRepairOriginalPhase(OwnerRepairObservation ownerRepairObservation, ItemStack itemStack, int n, Object object, int n2) {
        if (ownerRepairObservation == null || ownerRepairObservation.closed || ownerRepairObservation.publication.thread != Thread.currentThread() || ownerRepairObservation.actual != itemStack || ownerRepairObservation.publication.repairObservation != ownerRepairObservation || !FoodWeaponSourceLedger.writerPlanningUnchanged(ownerRepairObservation.publication, ownerRepairObservation.planning)) {
            return;
        }
        if (!ownerRepairObservation.trace.record(n, object, n2)) {
            FoodWeaponSourceLedger.closeOwnerRepairObservation(ownerRepairObservation);
            return;
        }
        ownerRepairObservation.publication.binding.root.readCut.change();
    }

    public static synchronized void ownerRepairObservationFailed(OwnerRepairObservation ownerRepairObservation, int n, Throwable throwable) {
        if (ownerRepairObservation == null || ownerRepairObservation.publication.thread != Thread.currentThread()) {
            return;
        }
        if (ownerRepairObservation.publication.failedRepairObservation == null) {
            ownerRepairObservation.publication.failedRepairObservation = ownerRepairObservation;
        }
        FoodWeaponSourceLedger.revokeOwnerWriterSelection(ownerRepairObservation.publication, ownerRepairObservation.selection);
        try {
            ownerRepairObservation.selection.observationFailed(ownerRepairObservation.observation, n, throwable);
        }
        finally {
            FoodWeaponSourceLedger.closeOwnerRepairObservation(ownerRepairObservation);
        }
    }

    public static synchronized Throwable ownerRepairObservationFailure(OwnerRepairObservation ownerRepairObservation, int n) {
        return ownerRepairObservation == null || ownerRepairObservation.publication.thread != Thread.currentThread() ? null : ownerRepairObservation.observation.failure(n);
    }

    public static synchronized void closeOwnerRepairObservation(OwnerRepairObservation ownerRepairObservation) {
        if (ownerRepairObservation == null || ownerRepairObservation.closed || ownerRepairObservation.publication.thread != Thread.currentThread()) {
            return;
        }
        ownerRepairObservation.closed = true;
        ownerRepairObservation.trace.close();
        ownerRepairObservation.observation.close();
        if (ownerRepairObservation.publication.repairObservation == ownerRepairObservation) {
            ownerRepairObservation.publication.repairObservation = null;
            if (ownerRepairObservation.publication.selection == ownerRepairObservation.selection) {
                ownerRepairObservation.publication.binding.root.readCut.change();
                ownerRepairObservation.publication.closed = true;
            }
        }
    }

    private static WriterPlanningCut writerPlanningCut(OwnerWriterPublication ownerWriterPublication) {
        return ownerWriterPublication == null ? null : new WriterPlanningCut(ownerWriterPublication.selection, ownerWriterPublication.expectation.cursor.position(), ownerWriterPublication.binding.root.interactionChange, ownerWriterPublication.stepOrdinal);
    }

    private static boolean writerPlanningUnchanged(OwnerWriterPublication ownerWriterPublication, WriterPlanningCut writerPlanningCut) {
        return ownerWriterPublication != null && writerPlanningCut != null && writerPlanningCut.matches(ownerWriterPublication.selection, ownerWriterPublication.expectation.cursor.position(), ownerWriterPublication.binding.root.interactionChange, ownerWriterPublication.stepOrdinal) && writerPlanningCut.selection.matches(ownerWriterPublication.producer, ownerWriterPublication.producerOrdinal, ownerWriterPublication.commit, ownerWriterPublication.leaf, ownerWriterPublication.leafOrdinal);
    }

    private static boolean writerPublicationUnchanged(OwnerWriterPublication ownerWriterPublication, WriterPlanningCut writerPlanningCut) {
        if (ownerWriterPublication == null || writerPlanningCut == null || !writerPlanningCut.samePublication(ownerWriterPublication.selection, ownerWriterPublication.expectation.cursor.position(), ownerWriterPublication.binding.root.interactionChange, ownerWriterPublication.stepOrdinal)) {
            return false;
        }
        if (writerPlanningCut.selection != null) {
            return writerPlanningCut.selection.matches(ownerWriterPublication.producer, ownerWriterPublication.producerOrdinal, ownerWriterPublication.commit, ownerWriterPublication.leaf, ownerWriterPublication.leafOrdinal);
        }
        return ownerWriterPublication.producer == null && ownerWriterPublication.commit == null && ownerWriterPublication.leaf == null && ownerWriterPublication.producerOrdinal == -1 && ownerWriterPublication.leafOrdinal == -1 && ownerWriterPublication.stepOrdinal == -1;
    }

    static boolean ownerWriterPublicationContinuation(OwnerWriterPublication ownerWriterPublication) {
        WriterPlanningCut writerPlanningCut = FoodWeaponSourceLedger.writerPlanningCut(ownerWriterPublication);
        return FoodWeaponSourceLedger.writerCaller(FoodWeaponFinalDamage.class) && FoodWeaponSourceLedger.ownerWriterPublicationLive(ownerWriterPublication) && FoodWeaponSourceLedger.writerPublicationUnchanged(ownerWriterPublication, writerPlanningCut) && FoodWeaponFinalDamage.matchesOwnerWriterPublication(ownerWriterPublication.receipt, ownerWriterPublication.binding, ownerWriterPublication.batch, ownerWriterPublication.admission, ownerWriterPublication.result) && FoodWeaponSourceLedger.ownerWriterPublicationLive(ownerWriterPublication) && FoodWeaponSourceLedger.writerPublicationUnchanged(ownerWriterPublication, writerPlanningCut);
    }

    private static boolean projectedBindingLive(OriginalInteractionBinding originalInteractionBinding) {
        return originalInteractionBinding != null && (originalInteractionBinding.parent == null && originalInteractionBinding.depth == 1 && originalInteractionBinding.childCall == null && FoodWeaponSourceLedger.projectedCurrentBindingLive(originalInteractionBinding) || originalInteractionBinding.parent != null && originalInteractionBinding.root.current == originalInteractionBinding && FoodWeaponSourceLedger.originalBindingAncestryLive(originalInteractionBinding));
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static boolean projectedCurrentBindingLive(OriginalInteractionBinding originalInteractionBinding) {
        Class<FoodWeaponSourceLedger> clazz = FoodWeaponSourceLedger.class;
        synchronized (FoodWeaponSourceLedger.class) {
            if (originalInteractionBinding == null || originalInteractionBinding.closed || originalInteractionBinding.root.thread != Thread.currentThread()) {
                // ** MonitorExit[var4_1] (shouldn't be in output)
                return false;
            }
            WeaponExpectation weaponExpectation = originalInteractionBinding.root.expectation;
            Object object = weaponExpectation == null ? null : weaponExpectation.repairGeneration;
            OwnerLineageRevocationStore.Lease lease = weaponExpectation == null ? null : OwnerFullRepairGuard.lease(weaponExpectation.actual);
            // ** MonitorExit[var4_1] (shouldn't be in output)
            if (weaponExpectation != null && !OwnerRepairGenerationAuthority.matches(weaponExpectation.actual, object)) {
                return false;
            }
            clazz = FoodWeaponSourceLedger.class;
            synchronized (FoodWeaponSourceLedger.class) {
                InteractionRoot interactionRoot = originalInteractionBinding.root;
                // ** MonitorExit[var4_1] (shouldn't be in output)
                return !originalInteractionBinding.closed && interactionRoot.expectation == weaponExpectation && (weaponExpectation == null || !weaponExpectation.closed && weaponExpectation.repairGeneration == object && OwnerRepairGenerationAuthority.identityMatches(weaponExpectation.actual, object, lease)) && !interactionRoot.closed && !interactionRoot.retired && FoodWeaponSourceLedger.nativeObservationReadLive(interactionRoot) && !interactionRoot.serverState.closed && !interactionRoot.ownerState.closed && !interactionRoot.readCut.closed && interactionRoot.thread == Thread.currentThread() && interactionRoot.ownerState.current == interactionRoot && interactionRoot.current == originalInteractionBinding && SERVERS.get(interactionRoot.server) == interactionRoot.serverState && interactionRoot.serverState.epoch == interactionRoot.serverEpoch && interactionRoot.ownerState.epoch == interactionRoot.ownerEpoch && interactionRoot.projectionSlot >= 0 && ACTIVE_ROOT_PROJECTIONS[interactionRoot.projectionSlot] == interactionRoot;
            }
        }
    }

    private static synchronized boolean installRootProjection(InteractionRoot interactionRoot) {
        if (interactionRoot.closed || interactionRoot.retired || interactionRoot.readCut.closed || interactionRoot.expectation != null && interactionRoot.expectation.closed) {
            return false;
        }
        for (int i = 0; i < 4096; ++i) {
            if (ACTIVE_ROOT_PROJECTIONS[i] != null) continue;
            FoodWeaponSourceLedger.ACTIVE_ROOT_PROJECTIONS[i] = interactionRoot;
            interactionRoot.projectionSlot = i;
            return true;
        }
        return false;
    }

    private static synchronized void removeRootProjection(InteractionRoot interactionRoot) {
        interactionRoot.readCut.close();
        interactionRoot.readAliases.clear();
        if (interactionRoot.projectionSlot >= 0 && ACTIVE_ROOT_PROJECTIONS[interactionRoot.projectionSlot] == interactionRoot) {
            FoodWeaponSourceLedger.ACTIVE_ROOT_PROJECTIONS[interactionRoot.projectionSlot] = null;
        }
        interactionRoot.projectionSlot = -1;
    }

    private static synchronized InteractionRoot projectedRootForPlayer(ServerPlayer serverPlayer) {
        for (int i = 0; i < 4096; ++i) {
            InteractionRoot interactionRoot = ACTIVE_ROOT_PROJECTIONS[i];
            if (interactionRoot == null || interactionRoot.player != serverPlayer || interactionRoot.closed || interactionRoot.retired || interactionRoot.serverState.closed || interactionRoot.ownerState.closed || interactionRoot.ownerState.current != interactionRoot) continue;
            return interactionRoot;
        }
        return null;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     * Converted monitor instructions to comments
     * Lifted jumps to return sites
     */
    private static PrimaryReadWindow beginPrimaryRead(DamageSource damageSource, ServerPlayer serverPlayer) {
        int n = 0;
        while (n < 4096) {
            OriginalInteractionBinding originalInteractionBinding;
            Class<FoodWeaponSourceLedger> clazz = FoodWeaponSourceLedger.class;
            // MONITORENTER : net.farmingtales.ftboss.item.FoodWeaponSourceLedger.class
            InteractionRoot interactionRoot = ACTIVE_ROOT_PROJECTIONS[n];
            OriginalInteractionBinding originalInteractionBinding2 = originalInteractionBinding = interactionRoot == null ? null : interactionRoot.current;
            if (originalInteractionBinding == null || interactionRoot.player != serverPlayer || originalInteractionBinding.source != damageSource) {
                // MONITOREXIT : clazz
            } else if (FoodWeaponSourceLedger.projectedBindingLive(originalInteractionBinding)) {
                clazz = FoodWeaponSourceLedger.class;
                // MONITORENTER : net.farmingtales.ftboss.item.FoodWeaponSourceLedger.class
                if (!(ACTIVE_ROOT_PROJECTIONS[n] != interactionRoot || interactionRoot.current != originalInteractionBinding || interactionRoot.thread != Thread.currentThread() || interactionRoot.closed || interactionRoot.retired || interactionRoot.readCut.closed)) {
                    // MONITOREXIT : clazz
                    return new PrimaryReadWindow(interactionRoot);
                }
                // MONITOREXIT : clazz
            }
            ++n;
        }
        return null;
    }

    private static void setOwnerInteraction(OwnerState ownerState, InteractionRoot interactionRoot) {
        ownerState.interactionChange = new Object();
        ownerState.current = interactionRoot;
    }

    public static synchronized void invalidateOwnerReadProjection(ServerPlayer serverPlayer) {
        for (int i = 0; i < 4096; ++i) {
            InteractionRoot interactionRoot = ACTIVE_ROOT_PROJECTIONS[i];
            if (interactionRoot == null || interactionRoot.player != serverPlayer) continue;
            interactionRoot.readCut.change();
        }
    }

    private static boolean rootReadReceiver(InteractionRoot interactionRoot, Object object) {
        if (interactionRoot == null || object == null) {
            return false;
        }
        if (interactionRoot.player == object || interactionRoot.server == object || interactionRoot.expectation != null && interactionRoot.expectation.actual == object || interactionRoot.readAliases.contains(object)) {
            return true;
        }
        OriginalInteractionBinding originalInteractionBinding = interactionRoot.current;
        for (int i = 16; originalInteractionBinding != null && i > 0; --i) {
            if (originalInteractionBinding.source == object || originalInteractionBinding.target == object) {
                return true;
            }
            originalInteractionBinding = originalInteractionBinding.parent;
        }
        return false;
    }

    public static synchronized void observeOriginalReadAlias(Object object, Object object2) {
        if (object2 == null) {
            return;
        }
        for (int i = 0; i < 4096; ++i) {
            InteractionRoot interactionRoot = ACTIVE_ROOT_PROJECTIONS[i];
            if (!FoodWeaponSourceLedger.rootReadReceiver(interactionRoot, object) || interactionRoot.readAliases.bind(object2)) continue;
            interactionRoot.readCut.close();
        }
    }

    public static synchronized void invalidateRawReadProjection(Object object) {
        for (int i = 0; i < 4096; ++i) {
            InteractionRoot interactionRoot = ACTIVE_ROOT_PROJECTIONS[i];
            if (!FoodWeaponSourceLedger.rootReadReceiver(interactionRoot, object)) continue;
            interactionRoot.readCut.change();
        }
    }

    public static synchronized void invalidateProviderReadProjection(Object object) {
        FoodWeaponSourceLedger.invalidateRawReadProjection(object);
    }

    public static synchronized void invalidateTagReadProjection(Tag tag) {
        FoodWeaponSourceLedger.invalidateRawReadProjection(tag);
    }

    public static synchronized void invalidateServerReadProjection(MinecraftServer minecraftServer) {
        for (int i = 0; i < 4096; ++i) {
            InteractionRoot interactionRoot = ACTIVE_ROOT_PROJECTIONS[i];
            if (interactionRoot == null || interactionRoot.server != minecraftServer) continue;
            interactionRoot.readCut.change();
        }
    }

    public static synchronized void invalidateSourceReadProjection(DamageSource damageSource) {
        for (int i = 0; i < 4096; ++i) {
            InteractionRoot interactionRoot = ACTIVE_ROOT_PROJECTIONS[i];
            if (!FoodWeaponSourceLedger.rootReadReceiver(interactionRoot, damageSource)) continue;
            interactionRoot.readCut.change();
        }
    }

    public static synchronized void invalidateTargetReadProjection(Entity entity) {
        for (int i = 0; i < 4096; ++i) {
            InteractionRoot interactionRoot = ACTIVE_ROOT_PROJECTIONS[i];
            if (!FoodWeaponSourceLedger.rootReadReceiver(interactionRoot, entity)) continue;
            interactionRoot.readCut.change();
        }
    }

    public static synchronized void invalidateWeaponReadProjection(ItemStack itemStack) {
        for (int i = 0; i < 4096; ++i) {
            InteractionRoot interactionRoot = ACTIVE_ROOT_PROJECTIONS[i];
            if (interactionRoot == null || interactionRoot.expectation == null || interactionRoot.expectation.actual != itemStack) continue;
            interactionRoot.readCut.change();
        }
    }

    private static boolean expectationMatches(WeaponExpectation weaponExpectation, ItemStack itemStack) {
        if (weaponExpectation == null || weaponExpectation.closed || weaponExpectation.actual != itemStack || weaponExpectation.active != null || !OwnerFullRepairGuard.permits(itemStack) || !OwnerRepairGenerationAuthority.matches(itemStack, weaponExpectation.repairGeneration)) {
            return false;
        }
        Object object = weaponExpectation.cursor.position();
        byte[] byArray = FoodWeaponSourceLedger.canonical((Tag)itemStack.m_41739_(new CompoundTag()));
        return !weaponExpectation.closed && OwnerRepairGenerationAuthority.matches(itemStack, weaponExpectation.repairGeneration) && OwnerFullRepairGuard.permits(itemStack) && object == weaponExpectation.cursor.position() && weaponExpectation.active == null && weaponExpectation.cursor.matches(byArray);
    }

    private static boolean writerCaller(Class<?> clazz) {
        return NATIVE_CALLER.walk(stream -> stream.filter(stackFrame -> stackFrame.getDeclaringClass() != FoodWeaponSourceLedger.class).findFirst().map(stackFrame -> stackFrame.getDeclaringClass().getNestHost() == clazz).orElse(false));
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    static OwnerWriterPublication openOwnerWriterPublication(OriginalInteractionBinding originalInteractionBinding, AttemptBatchQuote attemptBatchQuote, AttemptBatchAdmission attemptBatchAdmission, AttemptBatchCommit attemptBatchCommit, FoodWeaponFinalDamage.OffenseReceipt offenseReceipt) {
        if (!FoodWeaponSourceLedger.writerCaller(FoodWeaponFinalDamage.class) || !FoodWeaponSourceLedger.projectedBindingLive(originalInteractionBinding) || attemptBatchQuote == null || attemptBatchAdmission == null || offenseReceipt == null || attemptBatchQuote.binding != originalInteractionBinding || attemptBatchCommit == null || attemptBatchCommit.batch != attemptBatchQuote || attemptBatchAdmission.preparation.result != attemptBatchCommit || attemptBatchAdmission.preparation.state != originalInteractionBinding.root.serverState || attemptBatchCommit.status != AttemptBatchCommitStatus.COMMITTED && attemptBatchCommit.status != AttemptBatchCommitStatus.ALREADY_COMMITTED || originalInteractionBinding.root.expectation == null || originalInteractionBinding.root.expectation.closed || originalInteractionBinding.root.writerPublication != null && (originalInteractionBinding.parent == null || !FoodWeaponSourceLedger.originalBindingAncestryLive(originalInteractionBinding) || originalInteractionBinding.root.writerPublication != FoodWeaponSourceLedger.acceptedOriginalPublication(originalInteractionBinding.parent))) {
            return null;
        }
        if (!FoodWeaponFinalDamage.matchesOwnerWriterPublication(offenseReceipt, originalInteractionBinding, attemptBatchQuote, attemptBatchAdmission, attemptBatchCommit)) {
            return null;
        }
        Class<FoodWeaponSourceLedger> clazz = FoodWeaponSourceLedger.class;
        synchronized (FoodWeaponSourceLedger.class) {
            OwnerWriterPublication ownerWriterPublication;
            if (originalInteractionBinding.root.thread != Thread.currentThread() || originalInteractionBinding.closed || originalInteractionBinding.root.current != originalInteractionBinding || originalInteractionBinding.root.expectation == null || originalInteractionBinding.root.expectation.closed || originalInteractionBinding.root.writerPublication != null && (originalInteractionBinding.parent == null || originalInteractionBinding.root.writerPublication != FoodWeaponSourceLedger.acceptedOriginalPublication(originalInteractionBinding.parent))) {
                // ** MonitorExit[var5_5] (shouldn't be in output)
                return null;
            }
            BatchPreparation batchPreparation = attemptBatchAdmission.preparation;
            if (batchPreparation.state.claims != (batchPreparation.replacement == null ? batchPreparation.originalClaims : batchPreparation.replacement)) {
                // ** MonitorExit[var5_5] (shouldn't be in output)
                return null;
            }
            originalInteractionBinding.root.writerPublication = ownerWriterPublication = new OwnerWriterPublication(originalInteractionBinding, attemptBatchQuote, attemptBatchAdmission, attemptBatchCommit, offenseReceipt);
            // ** MonitorExit[var5_5] (shouldn't be in output)
            return ownerWriterPublication;
        }
    }

    static boolean selectOwnerWriterLeaf(OwnerWriterPublication ownerWriterPublication, Object object, int n, Object object2, Object object3, int n2) {
        WriterSelection writerSelection;
        WriterSelection writerSelection2;
        WriterPlanningCut writerPlanningCut = FoodWeaponSourceLedger.writerPlanningCut(ownerWriterPublication);
        WriterSelection writerSelection3 = writerSelection2 = ownerWriterPublication == null ? null : ownerWriterPublication.selection;
        if (!(FoodWeaponSourceLedger.writerCaller(FoodWeaponFinalDamage.class) && FoodWeaponSourceLedger.ownerWriterPublicationContinuation(ownerWriterPublication) && FoodWeaponSourceLedger.writerPublicationUnchanged(ownerWriterPublication, writerPlanningCut) && object != null && object2 != null && object3 != null && n >= ownerWriterPublication.producerOrdinal && (n != ownerWriterPublication.producerOrdinal || object == ownerWriterPublication.producer && n2 > ownerWriterPublication.leafOrdinal) && n2 >= 0 && ownerWriterPublication.expectation.active == null && ownerWriterPublication.repairObservation == null)) {
            return false;
        }
        if (!(FoodWeaponFinalDamage.matchesOwnerWriterSelection(ownerWriterPublication.receipt, object, n, object2, object3, n2) && FoodWeaponSourceLedger.ownerWriterPublicationContinuation(ownerWriterPublication) && FoodWeaponSourceLedger.writerPublicationUnchanged(ownerWriterPublication, writerPlanningCut) && ownerWriterPublication.selection == writerSelection2 && ownerWriterPublication.expectation.active == null && ownerWriterPublication.repairObservation == null)) {
            return false;
        }
        if (ownerWriterPublication.playerWriter != null) {
            if (!ownerWriterPublication.playerWriter.plan.published) {
                return false;
            }
            FoodWeaponSourceLedger.closeOwnerPlayerWriter(ownerWriterPublication.playerWriter);
            ownerWriterPublication.playerWriter = null;
        }
        if (!FoodWeaponSourceLedger.ownerWriterPublicationLive(ownerWriterPublication) || !FoodWeaponSourceLedger.writerPublicationUnchanged(ownerWriterPublication, writerPlanningCut)) {
            return false;
        }
        ownerWriterPublication.selection = writerSelection = new WriterSelection(object, n, object2, object3, n2);
        ownerWriterPublication.producer = object;
        ownerWriterPublication.producerOrdinal = n;
        ownerWriterPublication.commit = object2;
        ownerWriterPublication.leaf = object3;
        ownerWriterPublication.leafOrdinal = n2;
        ownerWriterPublication.stepOrdinal = -1;
        return true;
    }

    private static boolean ownerWriterSelectionLive(OwnerWriterPublication ownerWriterPublication) {
        WriterPlanningCut writerPlanningCut = FoodWeaponSourceLedger.writerPlanningCut(ownerWriterPublication);
        if (!FoodWeaponSourceLedger.ownerWriterPublicationLive(ownerWriterPublication) || !FoodWeaponSourceLedger.writerPlanningUnchanged(ownerWriterPublication, writerPlanningCut)) {
            return false;
        }
        WriterSelection writerSelection = writerPlanningCut.selection;
        return FoodWeaponFinalDamage.matchesOwnerWriterPublication(ownerWriterPublication.receipt, ownerWriterPublication.binding, ownerWriterPublication.batch, ownerWriterPublication.admission, ownerWriterPublication.result) && FoodWeaponSourceLedger.writerPlanningUnchanged(ownerWriterPublication, writerPlanningCut) && FoodWeaponFinalDamage.matchesOwnerWriterSelection(ownerWriterPublication.receipt, writerSelection.producer, writerSelection.producerOrdinal, writerSelection.commit, writerSelection.leaf, writerSelection.leafOrdinal) && FoodWeaponSourceLedger.ownerWriterPublicationLive(ownerWriterPublication) && FoodWeaponSourceLedger.writerPlanningUnchanged(ownerWriterPublication, writerPlanningCut);
    }

    private static boolean ownerWriterPublicationLive(OwnerWriterPublication ownerWriterPublication) {
        return ownerWriterPublication != null && (ownerWriterPublication.fieldSequenceBinding == null || ownerWriterPublication.fieldSequenceBinding.extraAuthority && OwnerFieldExpectedAuthority.covered(ownerWriterPublication.fieldSequenceBinding.prepared)) && !ownerWriterPublication.closed && ownerWriterPublication.thread == Thread.currentThread() && FoodWeaponSourceLedger.projectedBindingLive(ownerWriterPublication.binding) && !ownerWriterPublication.expectation.closed && ownerWriterPublication.binding.root.expectation == ownerWriterPublication.expectation && ownerWriterPublication.binding.root.writerPublication == ownerWriterPublication && !ownerWriterPublication.closed && (ownerWriterPublication.selection == null || !ownerWriterPublication.selection.revoked);
    }

    static OwnerWriterStep planOwnerTimerWriter(OwnerWriterPublication ownerWriterPublication, Object object, Object object2, int n, String string, String string2, Tag tag) {
        FoodWeaponHitPlan.FinalEffectsCommit finalEffectsCommit;
        int n2;
        Object object3;
        Object object4;
        WriterPlanningCut writerPlanningCut;
        block9: {
            block8: {
                writerPlanningCut = FoodWeaponSourceLedger.writerPlanningCut(ownerWriterPublication);
                object4 = ownerWriterPublication == null ? null : ownerWriterPublication.binding.root.interactionChange;
                object3 = ownerWriterPublication == null ? null : ownerWriterPublication.expectation.cursor.position();
                int n3 = n2 = ownerWriterPublication == null ? -1 : ownerWriterPublication.stepOrdinal;
                if (!FoodWeaponSourceLedger.writerCaller(FoodWeaponHitPlan.class) || !FoodWeaponSourceLedger.ownerWriterSelectionLive(ownerWriterPublication) || !FoodWeaponSourceLedger.writerPlanningUnchanged(ownerWriterPublication, writerPlanningCut) || writerPlanningCut.selection.commit != object || writerPlanningCut.selection.leaf != object2 || ownerWriterPublication.commit != object || ownerWriterPublication.leaf != object2 || n != ownerWriterPublication.stepOrdinal + 1 || tag == null || string2 == null || string2.length() > 1024 || string != null && string.length() > 1024 || ownerWriterPublication.expectation.active != null || ownerWriterPublication.repairObservation != null || !FoodWeaponSourceLedger.expectationMatches(ownerWriterPublication.expectation, ownerWriterPublication.expectation.actual)) {
                    return null;
                }
                if (!(object instanceof FoodWeaponHitPlan.FinalEffectsCommit)) break block8;
                finalEffectsCommit = (FoodWeaponHitPlan.FinalEffectsCommit)object;
                if (ownerWriterPublication.fieldPreparation != null && ownerWriterPublication.fieldSequenceBinding != null && ownerWriterPublication.fieldPreparation.lineage == ownerWriterPublication.fieldSequenceBinding.sequence.lineage && OwnerFieldExpectedAuthority.session(ownerWriterPublication.fieldPreparation) == ownerWriterPublication.fieldSequenceBinding.sequence) break block9;
            }
            return null;
        }
        FoodWeaponHitPlan.TimerSemanticStep timerSemanticStep = FoodWeaponHitPlan.timerSemanticAtOriginal(finalEffectsCommit, object2, ownerWriterPublication, n, string2);
        FoodWeaponHitPlan.TimerSemanticStep timerSemanticStep2 = FoodWeaponHitPlan.timerSemanticAtSessionOriginal(ownerWriterPublication.fieldSequenceBinding.sequence, object2, string2);
        if (timerSemanticStep == null || timerSemanticStep != timerSemanticStep2 || !OwnerFieldExpectedAuthority.timerWriterExpected(ownerWriterPublication.fieldPreparation, ownerWriterPublication.fieldSequenceBinding.sequence, object2, timerSemanticStep) || !Objects.equals(string, FoodWeaponEvolution.planHitTimerProfile((FoodWeaponEvolution.Profile)((Object)timerSemanticStep.projection().program().profile())))) {
            return null;
        }
        Tag tag2 = OwnerTimerSemanticAuthority.materialize(timerSemanticStep.value());
        if (tag2 == null) {
            return null;
        }
        CompoundTag compoundTag = FoodWeaponSourceLedger.projectTimerImage(ownerWriterPublication.expectation.expected, string, string2, tag2);
        if (!FoodWeaponSourceLedger.canonicalInputValid((Tag)compoundTag)) {
            return null;
        }
        OwnerWriterStep ownerWriterStep = new OwnerWriterStep(ownerWriterPublication, OwnerWriterKind.EVOLUTION_TIMER, compoundTag, FoodWeaponSourceLedger.projectTimerEntryImage(ownerWriterPublication.expectation.expected, string), string2, tag2, null, writerPlanningCut.selection);
        if (!FoodWeaponSourceLedger.ownerWriterSelectionLive(ownerWriterPublication) || !FoodWeaponSourceLedger.writerPlanningUnchanged(ownerWriterPublication, writerPlanningCut) || ownerWriterStep.selection != writerPlanningCut.selection || ownerWriterPublication.binding.root.interactionChange != object4 || ownerWriterPublication.expectation.cursor.position() != object3 || ownerWriterPublication.stepOrdinal != n2 || ownerWriterPublication.expectation.active != null || ownerWriterPublication.repairObservation != null) {
            return null;
        }
        ownerWriterPublication.stepOrdinal = n;
        ownerWriterPublication.expectation.active = ownerWriterStep;
        return ownerWriterStep;
    }

    static OwnerWriterStep planOwnerWolfTokenWriter(OwnerWriterPublication ownerWriterPublication, Object object, Object object2, int n, UUID uUID) {
        int n2;
        WriterPlanningCut writerPlanningCut = FoodWeaponSourceLedger.writerPlanningCut(ownerWriterPublication);
        Object object3 = ownerWriterPublication == null ? null : ownerWriterPublication.binding.root.interactionChange;
        Object object4 = ownerWriterPublication == null ? null : ownerWriterPublication.expectation.cursor.position();
        int n3 = n2 = ownerWriterPublication == null ? -1 : ownerWriterPublication.stepOrdinal;
        if (!(FoodWeaponSourceLedger.writerCaller(FoodSocketPowers.class) && FoodWeaponSourceLedger.ownerWriterSelectionLive(ownerWriterPublication) && FoodWeaponSourceLedger.writerPlanningUnchanged(ownerWriterPublication, writerPlanningCut) && writerPlanningCut.selection.commit == object && writerPlanningCut.selection.leaf == object2 && ownerWriterPublication.commit == object && ownerWriterPublication.leaf == object2 && uUID != null && n == ownerWriterPublication.stepOrdinal + 1 && ownerWriterPublication.expectation.active == null && ownerWriterPublication.repairObservation == null && FoodWeaponSourceLedger.expectationMatches(ownerWriterPublication.expectation, ownerWriterPublication.expectation.actual))) {
            return null;
        }
        CompoundTag compoundTag = FoodWeaponSourceLedger.projectWolfTokenImage(ownerWriterPublication.expectation.expected, uUID);
        if (!FoodWeaponSourceLedger.canonicalInputValid((Tag)compoundTag)) {
            return null;
        }
        OwnerWriterStep ownerWriterStep = new OwnerWriterStep(ownerWriterPublication, OwnerWriterKind.WOLF_TOKEN, compoundTag, FoodWeaponSourceLedger.projectWolfEntryImage(ownerWriterPublication.expectation.expected), "FTBossFoodSocketWeaponToken", null, uUID, writerPlanningCut.selection);
        if (!FoodWeaponSourceLedger.ownerWriterSelectionLive(ownerWriterPublication) || !FoodWeaponSourceLedger.writerPlanningUnchanged(ownerWriterPublication, writerPlanningCut) || ownerWriterStep.selection != writerPlanningCut.selection || ownerWriterPublication.binding.root.interactionChange != object3 || ownerWriterPublication.expectation.cursor.position() != object4 || ownerWriterPublication.stepOrdinal != n2 || ownerWriterPublication.expectation.active != null || ownerWriterPublication.repairObservation != null) {
            return null;
        }
        ownerWriterPublication.stepOrdinal = n;
        ownerWriterPublication.expectation.active = ownerWriterStep;
        return ownerWriterStep;
    }

    private static CompoundTag projectTimerImage(CompoundTag compoundTag, String string, String string2, Tag tag) {
        CompoundTag compoundTag2 = compoundTag.m_6426_();
        CompoundTag compoundTag3 = FoodWeaponSourceLedger.plannedCompound(compoundTag2, "tag");
        CompoundTag compoundTag4 = FoodWeaponSourceLedger.plannedCompound(compoundTag3, "FTBossFoodEvolution");
        if (string != null && !compoundTag4.m_128425_("Profile", 8)) {
            compoundTag4.m_128359_("Profile", string);
        }
        compoundTag4.m_128365_(string2, tag.m_6426_());
        return compoundTag2;
    }

    private static CompoundTag projectWolfTokenImage(CompoundTag compoundTag, UUID uUID) {
        CompoundTag compoundTag2 = compoundTag.m_6426_();
        FoodWeaponSourceLedger.plannedCompound(compoundTag2, "tag").m_128362_("FTBossFoodSocketWeaponToken", uUID);
        return compoundTag2;
    }

    private static CompoundTag projectTimerEntryImage(CompoundTag compoundTag, String string) {
        CompoundTag compoundTag2 = compoundTag.m_6426_();
        CompoundTag compoundTag3 = FoodWeaponSourceLedger.plannedCompound(FoodWeaponSourceLedger.plannedCompound(compoundTag2, "tag"), "FTBossFoodEvolution");
        if (string != null && !compoundTag3.m_128425_("Profile", 8)) {
            compoundTag3.m_128359_("Profile", string);
        }
        return compoundTag2;
    }

    private static CompoundTag projectWolfEntryImage(CompoundTag compoundTag) {
        CompoundTag compoundTag2 = compoundTag.m_6426_();
        FoodWeaponSourceLedger.plannedCompound(compoundTag2, "tag");
        return compoundTag2;
    }

    private static CompoundTag plannedCompound(CompoundTag compoundTag, String string) {
        if (compoundTag.m_128425_(string, 10)) {
            return compoundTag.m_128469_(string);
        }
        CompoundTag compoundTag2 = new CompoundTag();
        compoundTag.m_128365_(string, (Tag)compoundTag2);
        return compoundTag2;
    }

    private static boolean ownerWriterStepLive(OwnerWriterStep ownerWriterStep) {
        if (ownerWriterStep == null || ownerWriterStep.closed || !FoodWeaponSourceLedger.ownerWriterPublicationLive(ownerWriterStep.publication) || ownerWriterStep.publication.selection != ownerWriterStep.selection || ownerWriterStep.publication.expectation.active != ownerWriterStep || ownerWriterStep.publication.stepOrdinal != ownerWriterStep.ordinal || ownerWriterStep.position != ownerWriterStep.publication.expectation.cursor.position() || ownerWriterStep.transition != ownerWriterStep.publication.binding.root.interactionChange) {
            return false;
        }
        return FoodWeaponSourceLedger.ownerWriterSelectionLive(ownerWriterStep.publication) && !ownerWriterStep.closed && ownerWriterStep.publication.selection == ownerWriterStep.selection && ownerWriterStep.selection.matches(ownerWriterStep.publication.producer, ownerWriterStep.publication.producerOrdinal, ownerWriterStep.publication.commit, ownerWriterStep.publication.leaf, ownerWriterStep.publication.leafOrdinal) && ownerWriterStep.publication.expectation.active == ownerWriterStep && ownerWriterStep.publication.stepOrdinal == ownerWriterStep.ordinal && ownerWriterStep.position == ownerWriterStep.publication.expectation.cursor.position() && ownerWriterStep.transition == ownerWriterStep.publication.binding.root.interactionChange;
    }

    private static boolean originalUuidArgument(UUID uUID, UUID uUID2) {
        return uUID != null && uUID == uUID2;
    }

    public static void ownerWriterUuidEntered(OwnerWriterStep ownerWriterStep, ItemStack itemStack, CompoundTag compoundTag, String string, UUID uUID) {
        if (!FoodWeaponSourceLedger.ownerWriterStepLive(ownerWriterStep) || ownerWriterStep.kind != OwnerWriterKind.WOLF_TOKEN || ownerWriterStep.entered || ownerWriterStep.publication.expectation.actual != itemStack || compoundTag == null || !ownerWriterStep.key.equals(string) || !FoodWeaponSourceLedger.originalUuidArgument(ownerWriterStep.originalUuid, uUID)) {
            return;
        }
        CompoundTag compoundTag2 = itemStack.m_41783_();
        byte[] byArray = FoodWeaponSourceLedger.canonical((Tag)itemStack.m_41739_(new CompoundTag()));
        if (compoundTag2 != compoundTag || !Arrays.equals(ownerWriterStep.entryImage, byArray) || !FoodWeaponSourceLedger.ownerWriterStepLive(ownerWriterStep)) {
            return;
        }
        ownerWriterStep.receiver = compoundTag;
        ownerWriterStep.entered = true;
    }

    public static void ownerWriterUuidReturned(OwnerWriterStep ownerWriterStep, ItemStack itemStack, CompoundTag compoundTag, String string, UUID uUID) {
        if (ownerWriterStep == null || ownerWriterStep.kind != OwnerWriterKind.WOLF_TOKEN || !ownerWriterStep.key.equals(string) || !FoodWeaponSourceLedger.originalUuidArgument(ownerWriterStep.originalUuid, uUID)) {
            return;
        }
        FoodWeaponSourceLedger.ownerWriterPutReturned(ownerWriterStep, itemStack, compoundTag);
    }

    public static synchronized void closeOwnerWriterUuidStep(OwnerWriterStep ownerWriterStep) {
        FoodWeaponSourceLedger.closeOwnerWriterStep(ownerWriterStep);
    }

    public static void ownerWriterPutEntered(OwnerWriterStep ownerWriterStep, ItemStack itemStack, CompoundTag compoundTag, String string, Tag tag) {
        block5: {
            block4: {
                if (ownerWriterStep == null || ownerWriterStep.closed || ownerWriterStep.entered || ownerWriterStep.kind != OwnerWriterKind.EVOLUTION_TIMER || ownerWriterStep.value == null || !FoodWeaponSourceLedger.ownerWriterStepLive(ownerWriterStep) || ownerWriterStep.transition != ownerWriterStep.publication.binding.root.interactionChange || ownerWriterStep.publication.expectation.active != ownerWriterStep || ownerWriterStep.publication.expectation.actual != itemStack || compoundTag == null || !ownerWriterStep.key.equals(string) || tag == null || !Arrays.equals(FoodWeaponSourceLedger.canonical(ownerWriterStep.value), FoodWeaponSourceLedger.canonical(tag))) {
                    return;
                }
                Object object = ownerWriterStep.publication.binding.root.interactionChange;
                CompoundTag compoundTag2 = itemStack.m_41783_();
                if (compoundTag2 == null || (ownerWriterStep.kind != OwnerWriterKind.WOLF_TOKEN ? compoundTag2.m_128423_("FTBossFoodEvolution") != compoundTag : compoundTag2 != compoundTag)) break block4;
                if (Arrays.equals(ownerWriterStep.entryImage, FoodWeaponSourceLedger.canonical((Tag)itemStack.m_41739_(new CompoundTag()))) && FoodWeaponSourceLedger.ownerWriterStepLive(ownerWriterStep) && !ownerWriterStep.closed && ownerWriterStep.publication.expectation.active == ownerWriterStep && ownerWriterStep.transition == ownerWriterStep.publication.binding.root.interactionChange && object == ownerWriterStep.publication.binding.root.interactionChange && ownerWriterStep.position == ownerWriterStep.publication.expectation.cursor.position()) break block5;
            }
            return;
        }
        ownerWriterStep.receiver = compoundTag;
        ownerWriterStep.entered = true;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void ownerWriterPutReturned(OwnerWriterStep ownerWriterStep, ItemStack itemStack, CompoundTag compoundTag) {
        if (ownerWriterStep == null || ownerWriterStep.closed) {
            return;
        }
        boolean bl = false;
        try {
            if (!ownerWriterStep.entered || compoundTag != ownerWriterStep.receiver || !FoodWeaponSourceLedger.ownerWriterStepLive(ownerWriterStep) || ownerWriterStep.publication.expectation.active != ownerWriterStep || ownerWriterStep.publication.expectation.actual != itemStack || ownerWriterStep.transition != ownerWriterStep.publication.binding.root.interactionChange) {
                return;
            }
            WeaponExpectation weaponExpectation = ownerWriterStep.publication.expectation;
            byte[] byArray = FoodWeaponSourceLedger.canonical((Tag)itemStack.m_41739_(new CompoundTag()));
            if (!FoodWeaponSourceLedger.ownerWriterStepLive(ownerWriterStep) || ownerWriterStep.closed || weaponExpectation.active != ownerWriterStep || ownerWriterStep.transition != ownerWriterStep.publication.binding.root.interactionChange || !weaponExpectation.cursor.advance(ownerWriterStep.position, ownerWriterStep.before, ownerWriterStep.after, byArray)) {
                return;
            }
            weaponExpectation.expected = ownerWriterStep.planned;
            bl = true;
            ownerWriterStep.publication.binding.root.readCut.change();
        }
        finally {
            if (!bl && ownerWriterStep.publication.selection == ownerWriterStep.selection && ownerWriterStep.publication.expectation.active == ownerWriterStep) {
                ownerWriterStep.publication.closed = true;
            }
            ownerWriterStep.closed = true;
            if (ownerWriterStep.publication.expectation.active == ownerWriterStep) {
                ownerWriterStep.publication.expectation.active = null;
            }
        }
    }

    public static synchronized void closeOwnerWriterStep(OwnerWriterStep ownerWriterStep) {
        if (ownerWriterStep == null || ownerWriterStep.closed) {
            return;
        }
        ownerWriterStep.closed = true;
        if (ownerWriterStep.publication.selection == ownerWriterStep.selection && ownerWriterStep.publication.expectation.active == ownerWriterStep) {
            ownerWriterStep.publication.closed = true;
        }
        if (ownerWriterStep.publication.expectation.active == ownerWriterStep) {
            ownerWriterStep.publication.expectation.active = null;
        }
    }

    static boolean ownerWriterContinuation(OwnerWriterPublication ownerWriterPublication) {
        return FoodWeaponSourceLedger.ownerWriterSelectionLive(ownerWriterPublication) && ownerWriterPublication.expectation.active == null && ownerWriterPublication.repairObservation == null && (ownerWriterPublication.playerWriter == null || ownerWriterPublication.playerWriter.plan.published);
    }

    static synchronized void closeOwnerWriterPublication(OwnerWriterPublication ownerWriterPublication) {
        if (ownerWriterPublication == null) {
            return;
        }
        ownerWriterPublication.closed = true;
        ownerWriterPublication.observation.close();
        if (ownerWriterPublication.binding.root.writerPublication == ownerWriterPublication) {
            OwnerWriterPublication ownerWriterPublication2 = ownerWriterPublication.parentPublication;
            OwnerWriterPublication ownerWriterPublication3 = ownerWriterPublication.binding.root.writerPublication = ownerWriterPublication2 != null && !ownerWriterPublication2.closed && ownerWriterPublication2.binding.root == ownerWriterPublication.binding.root ? ownerWriterPublication2 : null;
        }
        if (ownerWriterPublication.repairObservation != null) {
            FoodWeaponSourceLedger.closeOwnerRepairObservation(ownerWriterPublication.repairObservation);
        }
        if (ownerWriterPublication.playerWriter != null) {
            FoodWeaponSourceLedger.closeOwnerPlayerWriter(ownerWriterPublication.playerWriter);
        }
        if (ownerWriterPublication.expectation.active != null && ownerWriterPublication.expectation.active.publication == ownerWriterPublication) {
            FoodWeaponSourceLedger.closeOwnerWriterStep(ownerWriterPublication.expectation.active);
        }
    }

    private static BatchPreparation rejectedPreparation(AttemptBatchCommitStatus attemptBatchCommitStatus) {
        return new BatchPreparation(null, null, FoodWeaponSourceLedger.rejectedBatch(attemptBatchCommitStatus));
    }

    public static void ownerTridentOriginalCopy(ThrownTrident thrownTrident, ItemStack itemStack, ItemStack itemStack2) {
        TicketState ticketState;
        boolean bl = NATIVE_CALLER.walk(stream -> stream.skip(1L).findFirst().map(stackFrame -> stackFrame.getDeclaringClass() == ThrownTrident.class && stackFrame.getMethodName().contains("ftboss$ownerTridentOriginalCopy")).orElse(false));
        if (!bl || thrownTrident == null || itemStack == null || itemStack2 == null || itemStack2 == itemStack || !(thrownTrident instanceof OwnerTridentCarrierAccess)) {
            return;
        }
        OwnerTridentCarrierAccess ownerTridentCarrierAccess = (OwnerTridentCarrierAccess)thrownTrident;
        NativeOperation nativeOperation = NATIVE_OPERATION.get();
        TicketState ticketState2 = TRIDENT_TICKET.get();
        Object object = FoodWeaponSourceLedger.liveNative(nativeOperation) && nativeOperation.kind == NativeKind.TRIDENT && nativeOperation.weapon == itemStack && nativeOperation.current != null && nativeOperation.current.worn ? nativeOperation.current : (ticketState = ticketState2 != null && ticketState2.actualWeapon() == itemStack ? ticketState2 : null);
        if (ticketState == null) {
            return;
        }
        ownerTridentCarrierAccess.ftboss$ownerInstallTridentLineage(new TridentCarrierLineage(thrownTrident, itemStack, itemStack2, ticketState));
    }

    private static TridentCarrierLineage tridentCarrierLineage(ThrownTrident thrownTrident, ItemStack itemStack, TridentCarrierLineage tridentCarrierLineage) {
        return tridentCarrierLineage != null && !tridentCarrierLineage.observationRejected && tridentCarrierLineage.entity == thrownTrident && tridentCarrierLineage.copied == itemStack && itemStack != null ? tridentCarrierLineage : null;
    }

    private static void rejectTridentExtra(ThrownTrident thrownTrident, TridentCarrierLineage tridentCarrierLineage, NativeReceipt nativeReceipt, NativePaidWeapon nativePaidWeapon, TicketState ticketState, TridentTicketBefore tridentTicketBefore, Throwable throwable) {
        if (tridentCarrierLineage != null && tridentCarrierLineage.entity == thrownTrident && (nativeReceipt != null ? tridentCarrierLineage.origin == nativeReceipt : (ticketState != null ? tridentCarrierLineage.origin == ticketState : nativePaidWeapon == null || tridentCarrierLineage.origin == nativePaidWeapon.receipt))) {
            tridentCarrierLineage.observationRejected = true;
            if (tridentCarrierLineage.observationFailure == null) {
                tridentCarrierLineage.observationFailure = throwable;
            }
        }
        if (nativeReceipt != null && nativeReceipt.operation.thread == Thread.currentThread() && (nativeReceipt.entity == null || nativeReceipt.entity == thrownTrident)) {
            nativeReceipt.materialRejected = true;
            nativeReceipt.consumed = true;
            nativeReceipt.operation.invalid = true;
        }
        if (nativePaidWeapon != null && nativePaidWeapon.carrier == thrownTrident && nativePaidWeapon.receipt.operation.thread == Thread.currentThread()) {
            nativePaidWeapon.expectation.closed = true;
            nativePaidWeapon.receipt.materialRejected = true;
            nativePaidWeapon.receipt.operation.invalid = true;
        }
        if (ticketState != null && tridentTicketBefore != null && tridentTicketBefore.ticket == ticketState.ticket() && tridentTicketBefore.actual == ticketState.actualWeapon() && tridentTicketBefore.thread == Thread.currentThread()) {
            tridentTicketBefore.observationRejected = true;
            if (TRIDENT_BEFORE.get() == tridentTicketBefore) {
                TRIDENT_BEFORE.remove();
            }
            if (TRIDENT_TICKET.get() == ticketState) {
                TRIDENT_TICKET.remove();
            }
        }
    }

    private static boolean tridentRawMatchesExpected(ItemStack itemStack, CompoundTag compoundTag) {
        return compoundTag != null && FoodWeaponExpectedTrace.same(NativeExpectedRawFields.observe((Tag)compoundTag), FoodWeaponSourceLedger.originalRepairRawFields(itemStack));
    }

    private static synchronized UUID ownerEpoch(ServerState serverState, UUID uUID) {
        OwnerState ownerState = serverState.ownerEpochs.get(uUID);
        return ownerState == null ? null : ownerState.epoch;
    }

    private static synchronized OwnerState ensureOwnerState(ServerState serverState, UUID uUID2) {
        return serverState.ownerEpochs.computeIfAbsent(uUID2, uUID -> new OwnerState(UUID.randomUUID()));
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static boolean interactionRootLive(InteractionRoot interactionRoot, ServerPlayer serverPlayer) {
        Class<FoodWeaponSourceLedger> clazz = FoodWeaponSourceLedger.class;
        synchronized (FoodWeaponSourceLedger.class) {
            if (interactionRoot == null || serverPlayer == null || interactionRoot.player != serverPlayer || interactionRoot.thread != Thread.currentThread()) {
                // ** MonitorExit[var5_2] (shouldn't be in output)
                return false;
            }
            WeaponExpectation weaponExpectation = interactionRoot.expectation;
            Object object = weaponExpectation == null ? null : weaponExpectation.repairGeneration;
            OwnerLineageRevocationStore.Lease lease = weaponExpectation == null ? null : OwnerFullRepairGuard.lease(weaponExpectation.actual);
            // ** MonitorExit[var5_2] (shouldn't be in output)
            if (weaponExpectation != null && !OwnerRepairGenerationAuthority.matches(weaponExpectation.actual, object)) {
                return false;
            }
            clazz = FoodWeaponSourceLedger.class;
            synchronized (FoodWeaponSourceLedger.class) {
                if (interactionRoot.expectation != weaponExpectation || weaponExpectation != null && (weaponExpectation.closed || weaponExpectation.repairGeneration != object || !OwnerRepairGenerationAuthority.identityMatches(weaponExpectation.actual, object, lease)) || interactionRoot.retired || interactionRoot.closed || !FoodWeaponSourceLedger.nativeObservationReadLive(interactionRoot) || interactionRoot.server != FoodWeaponSourceLedger.server(serverPlayer) || !interactionRoot.server.m_18695_() || interactionRoot.tick != FoodWeaponSourceLedger.clock(serverPlayer) || serverPlayer.m_213877_() || !serverPlayer.m_6084_() || serverPlayer.m_5833_()) {
                    // ** MonitorExit[var5_2] (shouldn't be in output)
                    return false;
                }
                // ** MonitorExit[var5_2] (shouldn't be in output)
                ServerState serverState = SERVERS.get(interactionRoot.server);
                return serverState == interactionRoot.serverState && !serverState.closed && !interactionRoot.ownerState.closed && serverState.epoch.equals(interactionRoot.serverEpoch) && serverState.ownerEpochs.get(serverPlayer.m_20148_()) == interactionRoot.ownerState && interactionRoot.ownerState.epoch.equals(interactionRoot.ownerEpoch) && interactionRoot.ownerState.current == interactionRoot;
            }
        }
    }

    private static boolean interactionMatches(OriginalInteractionBinding originalInteractionBinding, CompoundTag compoundTag, ServerPlayer serverPlayer) {
        if (originalInteractionBinding != null && originalInteractionBinding.childCall != null) {
            return originalInteractionBinding.root.player == serverPlayer && originalInteractionBinding.root.current == originalInteractionBinding && originalInteractionBinding.snapshot != null && compoundTag != null && originalInteractionBinding.snapshot.equals((Object)compoundTag) && FoodWeaponSourceLedger.originalBindingAncestryLive(originalInteractionBinding);
        }
        return originalInteractionBinding != null && !originalInteractionBinding.closed && originalInteractionBinding.childCall == null && originalInteractionBinding.snapshot != null && originalInteractionBinding.depth > 0 && originalInteractionBinding.depth <= 16 && FoodWeaponSourceLedger.interactionRootLive(originalInteractionBinding.root, serverPlayer) && originalInteractionBinding.root.current == originalInteractionBinding && compoundTag != null && originalInteractionBinding.snapshot.equals((Object)compoundTag) && FoodWeaponRanged.interactionViewAllows(serverPlayer, compoundTag, originalInteractionBinding.source, originalInteractionBinding.target);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     * Converted monitor instructions to comments
     * Lifted jumps to return sites
     */
    static OriginalInteractionBinding capturePrimaryInteractionBinding(CompoundTag compoundTag, ServerPlayer serverPlayer, DamageSource damageSource, Entity entity) {
        if (compoundTag == null) return null;
        if (serverPlayer == null) return null;
        if (damageSource == null) return null;
        if (entity == null) return null;
        if (!FoodWeaponSourceLedger.server(serverPlayer).m_18695_()) {
            return null;
        }
        int n = 0;
        while (n < 4096) {
            OriginalInteractionBinding originalInteractionBinding;
            Class<FoodWeaponSourceLedger> clazz = FoodWeaponSourceLedger.class;
            // MONITORENTER : net.farmingtales.ftboss.item.FoodWeaponSourceLedger.class
            InteractionRoot interactionRoot = ACTIVE_ROOT_PROJECTIONS[n];
            OriginalInteractionBinding originalInteractionBinding2 = originalInteractionBinding = interactionRoot == null ? null : interactionRoot.current;
            if (originalInteractionBinding == null || interactionRoot.thread != Thread.currentThread() || interactionRoot.player != serverPlayer || originalInteractionBinding.source != damageSource || originalInteractionBinding.target != entity) {
                // MONITOREXIT : clazz
            } else if (FoodWeaponSourceLedger.projectedBindingLive(originalInteractionBinding)) {
                clazz = FoodWeaponSourceLedger.class;
                // MONITORENTER : net.farmingtales.ftboss.item.FoodWeaponSourceLedger.class
                if (!(ACTIVE_ROOT_PROJECTIONS[n] != interactionRoot || interactionRoot.current != originalInteractionBinding || interactionRoot.closed || interactionRoot.retired || originalInteractionBinding.closed)) {
                    if (originalInteractionBinding.childCall != null) {
                        if (originalInteractionBinding.snapshot != null && originalInteractionBinding.snapshot.equals((Object)compoundTag) && originalInteractionBinding.parent != null && originalInteractionBinding.parent.root == interactionRoot && originalInteractionBinding.parent.childCall == originalInteractionBinding.childCall && originalInteractionBinding.parent.snapshot != null && originalInteractionBinding.parent.snapshot.equals((Object)originalInteractionBinding.snapshot)) {
                            return originalInteractionBinding;
                        }
                    } else if (interactionRoot.readCut.matches(compoundTag)) {
                        // MONITOREXIT : clazz
                        return originalInteractionBinding;
                    }
                }
                // MONITOREXIT : clazz
            }
            ++n;
        }
        return null;
    }

    static boolean matchesCleanupInteractionIdentity(OriginalInteractionBinding originalInteractionBinding, OriginalInteractionBinding originalInteractionBinding2, ServerPlayer serverPlayer) {
        if (originalInteractionBinding != null && originalInteractionBinding == originalInteractionBinding2 && originalInteractionBinding.childCall != null) {
            return originalInteractionBinding.root.player == serverPlayer && originalInteractionBinding.root.current == originalInteractionBinding && originalInteractionBinding.snapshot != null && FoodWeaponSourceLedger.originalBindingAncestryLive(originalInteractionBinding);
        }
        if (originalInteractionBinding == null || originalInteractionBinding != originalInteractionBinding2 || serverPlayer == null || originalInteractionBinding.closed || originalInteractionBinding.parent != null || originalInteractionBinding.depth != 1 || originalInteractionBinding.childCall != null || originalInteractionBinding.snapshot == null || originalInteractionBinding.snapshot.m_128456_()) {
            return false;
        }
        InteractionRoot interactionRoot = originalInteractionBinding.root;
        if (interactionRoot == null || interactionRoot.player != serverPlayer || interactionRoot.retired || interactionRoot.closed || interactionRoot.current != originalInteractionBinding || interactionRoot.thread != Thread.currentThread() || interactionRoot.server != FoodWeaponSourceLedger.server(serverPlayer) || !interactionRoot.server.m_18695_() || interactionRoot.tick != FoodWeaponSourceLedger.clock(serverPlayer)) {
            return false;
        }
        ServerState serverState = SERVERS.get(interactionRoot.server);
        return serverState == interactionRoot.serverState && serverState.epoch.equals(interactionRoot.serverEpoch) && serverState.ownerEpochs.get(serverPlayer.m_20148_()) == interactionRoot.ownerState && interactionRoot.ownerState.epoch.equals(interactionRoot.ownerEpoch) && interactionRoot.ownerState.current == interactionRoot;
    }

    private static OriginalInteractionBinding currentInteraction(CompoundTag compoundTag, ServerPlayer serverPlayer) {
        if (serverPlayer == null) {
            return null;
        }
        ServerState serverState = SERVERS.get(FoodWeaponSourceLedger.server(serverPlayer));
        OwnerState ownerState = serverState == null ? null : serverState.ownerEpochs.get(serverPlayer.m_20148_());
        InteractionRoot interactionRoot = ownerState == null ? null : ownerState.current;
        OriginalInteractionBinding originalInteractionBinding = interactionRoot == null ? null : interactionRoot.current;
        return FoodWeaponSourceLedger.interactionMatches(originalInteractionBinding, compoundTag, serverPlayer) ? originalInteractionBinding : null;
    }

    static OriginalInteractionBinding originalInteractionBinding(AttemptBatchQuote attemptBatchQuote) {
        return attemptBatchQuote == null ? null : attemptBatchQuote.binding;
    }

    static boolean matchesOriginalInteraction(OriginalInteractionBinding originalInteractionBinding, CompoundTag compoundTag, ServerPlayer serverPlayer) {
        return FoodWeaponSourceLedger.interactionMatches(originalInteractionBinding, compoundTag, serverPlayer) && (originalInteractionBinding.childCall != null && FoodWeaponSourceLedger.originalBindingAncestryLive(originalInteractionBinding) || FoodWeaponSourceLedger.attemptSourceValid(compoundTag, serverPlayer));
    }

    private static boolean pendingEchoLifecycleCaller() {
        return NATIVE_CALLER.walk(stream -> stream.filter(stackFrame -> stackFrame.getDeclaringClass() != FoodWeaponSourceLedger.class).findFirst().map(stackFrame -> stackFrame.getDeclaringClass() == FoodSocketAttributePowers.class && stackFrame.getMethodName().equals("damagePendingEcho") && stackFrame.getDescriptor().equals("(Lnet/farmingtales/ftboss/item/FoodWeaponEchoQueue$Entry;Lnet/minecraft/nbt/CompoundTag;Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/world/entity/LivingEntity;J)V")).orElse(false));
    }

    private static boolean pendingEchoRawHurtCaller() {
        return NATIVE_CALLER.walk(stream -> stream.filter(stackFrame -> stackFrame.getDeclaringClass() != FoodWeaponSourceLedger.class).findFirst().map(stackFrame -> stackFrame.getDeclaringClass() == FoodSocketAttributePowers.class && stackFrame.getMethodName().equals("hurtPendingEchoRaw") && stackFrame.getDescriptor().equals("(Lnet/farmingtales/ftboss/item/FoodSocketAttributePowers$PendingEchoDispatch;Lnet/farmingtales/ftboss/item/FoodWeaponSourceLedger$PendingEchoInteraction;)Z")).orElse(false));
    }

    static PendingEchoInteraction beginPendingEchoInteraction(FoodSocketAttributePowers.PendingEchoDispatch pendingEchoDispatch, ServerPlayer serverPlayer, LivingEntity livingEntity, DamageSource damageSource, float f) {
        if (!(FoodWeaponSourceLedger.pendingEchoLifecycleCaller() && serverPlayer != null && livingEntity != null && damageSource != null && Float.floatToRawIntBits(f) == Float.floatToRawIntBits(2.0f) && damageSource.m_7639_() == serverPlayer && damageSource.m_7640_() == null && damageSource.m_276093_(FoodWeaponDamageSources.SECONDARY) && livingEntity.m_9236_() == serverPlayer.m_9236_() && serverPlayer.m_284548_().m_8791_(livingEntity.m_20148_()) == livingEntity && FoodSocketAttributePowers.matchesPendingEchoDispatch(pendingEchoDispatch, serverPlayer, livingEntity, damageSource, f))) {
            return null;
        }
        CompoundTag compoundTag = FoodSocketAttributePowers.pendingEchoDispatchSource(pendingEchoDispatch);
        if (compoundTag == null || compoundTag.m_128456_() || !FoodWeaponSourceLedger.originalHandContinuousReadOnly(compoundTag, serverPlayer)) {
            return null;
        }
        ItemStack itemStack = FoodSocketAttributePowers.originalPendingEchoWeapon(pendingEchoDispatch);
        if (itemStack == null) {
            return null;
        }
        InteractionRoot interactionRoot = FoodWeaponSourceLedger.beginExternalInteraction(serverPlayer, RootSite.DELAYED_ECHO, damageSource, (Entity)livingEntity, compoundTag, f, itemStack, null);
        return interactionRoot == null ? null : new PendingEchoInteraction(pendingEchoDispatch, interactionRoot, serverPlayer, livingEntity, damageSource, f);
    }

    private static boolean pendingEchoInteractionLive(PendingEchoInteraction pendingEchoInteraction) {
        if (pendingEchoInteraction == null || pendingEchoInteraction.closed || pendingEchoInteraction.dispatch == null || pendingEchoInteraction.root.site != RootSite.DELAYED_ECHO || !FoodWeaponSourceLedger.interactionRootLive(pendingEchoInteraction.root, pendingEchoInteraction.player)) {
            return false;
        }
        OriginalInteractionBinding originalInteractionBinding = pendingEchoInteraction.binding;
        return originalInteractionBinding != null && !originalInteractionBinding.closed && originalInteractionBinding.root == pendingEchoInteraction.root && pendingEchoInteraction.root.current == originalInteractionBinding && originalInteractionBinding.parent == null && originalInteractionBinding.depth == 1 && originalInteractionBinding.childCall == null && originalInteractionBinding.source == pendingEchoInteraction.source && originalInteractionBinding.target == pendingEchoInteraction.target && originalInteractionBinding.rawAmountBits == Float.floatToRawIntBits(pendingEchoInteraction.rawAmount) && originalInteractionBinding.snapshot != null && !originalInteractionBinding.snapshot.m_128456_() && (pendingEchoInteraction.lineage == null ? FoodWeaponSourceLedger.originalHandContinuousReadOnly(originalInteractionBinding.snapshot, pendingEchoInteraction.player) : pendingEchoInteraction.lineage.dispatched && pendingEchoInteraction.lineage.snapshot.equals((Object)originalInteractionBinding.snapshot) && FoodWeaponSourceLedger.pendingEchoHandContinuous(pendingEchoInteraction.lineage, pendingEchoInteraction.player));
    }

    private static boolean pendingEchoOperandsMatch(PendingEchoInteraction pendingEchoInteraction, FoodSocketAttributePowers.PendingEchoDispatch pendingEchoDispatch, ServerPlayer serverPlayer, LivingEntity livingEntity, DamageSource damageSource, float f) {
        return pendingEchoInteraction != null && pendingEchoInteraction.dispatch == pendingEchoDispatch && pendingEchoInteraction.player == serverPlayer && pendingEchoInteraction.target == livingEntity && pendingEchoInteraction.source == damageSource && Float.floatToRawIntBits(pendingEchoInteraction.rawAmount) == Float.floatToRawIntBits(f) && Float.floatToRawIntBits(f) == Float.floatToRawIntBits(2.0f);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    static boolean hurtPendingEchoInteraction(PendingEchoInteraction pendingEchoInteraction, FoodSocketAttributePowers.PendingEchoDispatch pendingEchoDispatch, ServerPlayer serverPlayer, LivingEntity livingEntity, DamageSource damageSource, float f) {
        if (!FoodWeaponSourceLedger.pendingEchoRawHurtCaller() || !FoodWeaponSourceLedger.pendingEchoOperandsMatch(pendingEchoInteraction, pendingEchoDispatch, serverPlayer, livingEntity, damageSource, f) || pendingEchoInteraction.hurtEntered || pendingEchoInteraction.hurtActive || !FoodWeaponSourceLedger.pendingEchoInteractionLive(pendingEchoInteraction)) {
            return false;
        }
        pendingEchoInteraction.hurtEntered = true;
        pendingEchoInteraction.hurtActive = true;
        try {
            boolean bl = livingEntity.m_6469_(damageSource, f);
            pendingEchoInteraction.hurtFinished = true;
            boolean bl2 = bl;
            return bl2;
        }
        finally {
            pendingEchoInteraction.hurtActive = false;
        }
    }

    static boolean matchesPendingEchoInteraction(PendingEchoInteraction pendingEchoInteraction, FoodSocketAttributePowers.PendingEchoDispatch pendingEchoDispatch, ServerPlayer serverPlayer, LivingEntity livingEntity, DamageSource damageSource, float f) {
        return FoodWeaponSourceLedger.pendingEchoLifecycleCaller() && FoodWeaponSourceLedger.pendingEchoOperandsMatch(pendingEchoInteraction, pendingEchoDispatch, serverPlayer, livingEntity, damageSource, f) && pendingEchoInteraction.hurtEntered && pendingEchoInteraction.hurtFinished && !pendingEchoInteraction.hurtActive && FoodWeaponSourceLedger.pendingEchoInteractionLive(pendingEchoInteraction);
    }

    static synchronized void closePendingEchoInteraction(PendingEchoInteraction pendingEchoInteraction) {
        if (!FoodWeaponSourceLedger.pendingEchoLifecycleCaller() || pendingEchoInteraction == null || pendingEchoInteraction.closed || pendingEchoInteraction.root.thread != Thread.currentThread() || !pendingEchoInteraction.root.server.m_18695_()) {
            return;
        }
        pendingEchoInteraction.closed = true;
        FoodWeaponSourceLedger.endInteraction(pendingEchoInteraction.root);
    }

    private static boolean nativeProjectileImpactCaller(NativeVanillaImpact.RawImpact rawImpact) {
        boolean bl;
        if (rawImpact == null || rawImpact.carrier == null) {
            return false;
        }
        boolean bl2 = bl = rawImpact.site == NativeVanillaImpact.OriginalSite.TRIDENT;
        if (bl != rawImpact.carrier instanceof ThrownTrident || !bl && rawImpact.site != NativeVanillaImpact.OriginalSite.ARROW) {
            return false;
        }
        Class clazz = bl ? ThrownTrident.class : AbstractArrow.class;
        String string = bl ? "tridentHurt" : "arrowHurt";
        String string2 = bl ? "ftboss$nativeVanillaTridentRawHurt" : "ftboss$nativeVanillaArrowRawHurt";
        String string3 = bl ? "ThrownTrident" : "AbstractArrow";
        return NATIVE_CALLER.walk(stream -> {
            List<StackWalker.StackFrame> list = stream.filter(stackFrame -> stackFrame.getDeclaringClass() != FoodWeaponSourceLedger.class).limit(4L).toList();
            if (list.size() != 4) {
                return false;
            }
            StackWalker.StackFrame stackFrame2 = list.get(0);
            StackWalker.StackFrame stackFrame3 = list.get(1);
            StackWalker.StackFrame stackFrame4 = list.get(2);
            StackWalker.StackFrame stackFrame5 = list.get(3);
            return stackFrame2.getDeclaringClass() == NativeVanillaImpact.class && stackFrame2.getMethodName().equals("hurt") && stackFrame2.getDescriptor().equals("(Lnet/farmingtales/ftboss/item/NativeVanillaImpact$OriginalSite;Lnet/minecraft/world/entity/projectile/AbstractArrow;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/damagesource/DamageSource;F)Z") && stackFrame3.getDeclaringClass() == NativeVanillaImpact.class && stackFrame3.getMethodName().equals(string) && stackFrame3.getDescriptor().equals("(Lnet/minecraft/world/entity/projectile/" + string3 + ";Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/damagesource/DamageSource;F)Z") && stackFrame4.getDeclaringClass() == clazz && (stackFrame4.getMethodName().equals(string2) || stackFrame4.getMethodName().endsWith("$" + string2)) && stackFrame4.getDescriptor().equals("(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/damagesource/DamageSource;F)Z") && stackFrame5.getDeclaringClass() == clazz && stackFrame5.getMethodName().equals("m_5790_") && stackFrame5.getDescriptor().equals("(Lnet/minecraft/world/phys/EntityHitResult;)V");
        });
    }

    static NativeProjectileInteraction beginNativeProjectileImpact(NativeVanillaImpact.RawImpact rawImpact) {
        return FoodWeaponSourceLedger.beginNativeProjectileImpact(rawImpact, FoodWeaponSourceLedger.observeNativeProjectileImpact(rawImpact));
    }

    static NativeProjectileInteraction beginNativeProjectileImpact(NativeVanillaImpact.RawImpact rawImpact, NativeProjectileObservation nativeProjectileObservation) {
        if (nativeProjectileObservation == null || nativeProjectileObservation.call != rawImpact || nativeProjectileObservation.begun || nativeProjectileObservation.finished || nativeProjectileObservation.failure.failed) {
            return null;
        }
        nativeProjectileObservation.begun = true;
        try {
            NativeProjectileInteraction nativeProjectileInteraction;
            RootSite rootSite;
            ServerPlayer serverPlayer;
            Entity entity;
            block18: {
                block17: {
                    if (!FoodWeaponSourceLedger.nativeProjectileImpactCaller(rawImpact) || rawImpact.closed() || rawImpact.originalEntered() || rawImpact.target == null || rawImpact.source == null || !Float.isFinite(rawImpact.amount) || rawImpact.amount <= 0.0f || rawImpact.rawAmountBits != Float.floatToRawIntBits(rawImpact.amount) || !((entity = rawImpact.carrier.m_19749_()) instanceof ServerPlayer)) break block17;
                    serverPlayer = (ServerPlayer)entity;
                    if (!serverPlayer.m_9236_().f_46443_ && FoodWeaponSourceLedger.server(serverPlayer).m_18695_() && rawImpact.carrier.m_9236_() == serverPlayer.m_9236_() && rawImpact.target.m_9236_() == serverPlayer.m_9236_() && !rawImpact.carrier.m_213877_() && serverPlayer.m_284548_().m_8791_(rawImpact.carrier.m_20148_()) == rawImpact.carrier && rawImpact.source.m_7640_() == rawImpact.carrier && rawImpact.source.m_7639_() == serverPlayer && rawImpact.source.m_269533_(DamageTypeTags.f_268524_)) break block18;
                }
                return null;
            }
            entity = FoodWeaponSourceLedger.resolveReadOnly(rawImpact.source, serverPlayer);
            if (!(!entity.m_128456_() && NATIVE_PURPOSE.equals(entity.m_128461_("DamagePurpose")) && entity.m_128403_("NativeReceiptId") && entity.m_128403_("NativeEntityId") && entity.m_128342_("NativeEntityId").equals(rawImpact.carrier.m_20148_()))) {
                return null;
            }
            ItemStack itemStack = ItemStack.m_41712_((CompoundTag)entity.m_128469_("Weapon"));
            String string = entity.m_128461_("NativeEntry");
            if (rawImpact.site == NativeVanillaImpact.OriginalSite.TRIDENT) {
                if (!"TRIDENT".equals(string) || FoodWeaponSourceLedger.nativeKind(itemStack.m_41720_(), itemStack) != NativeKind.TRIDENT) {
                    return null;
                }
                rootSite = RootSite.NATIVE_TRIDENT_IMPACT;
            } else {
                if (rawImpact.carrier.getClass() != Arrow.class && rawImpact.carrier.getClass() != SpectralArrow.class) {
                    return null;
                }
                if ("BOW".equals(string) && FoodWeaponSourceLedger.nativeKind(itemStack.m_41720_(), itemStack) == NativeKind.BOW) {
                    rootSite = RootSite.NATIVE_BOW_IMPACT;
                } else if ("CROSSBOW".equals(string) && FoodWeaponSourceLedger.nativeKind(itemStack.m_41720_(), itemStack) == NativeKind.CROSSBOW) {
                    rootSite = RootSite.NATIVE_CROSSBOW_IMPACT;
                } else {
                    return null;
                }
            }
            NativePaidWeapon nativePaidWeapon = NativeVanillaImpact.originalOwnerPaidWeapon(rawImpact);
            if (!(nativePaidWeapon != null && nativePaidWeapon.carrier == rawImpact.carrier && nativePaidWeapon.receipt.entity == rawImpact.carrier && nativePaidWeapon.receipt.operation.player == serverPlayer && nativePaidWeapon.receipt.consumed && nativePaidWeapon.receipt.added && nativePaidWeapon.receipt.worn && !nativePaidWeapon.receipt.operation.invalid && nativePaidWeapon.receipt.token.equals(entity.m_128342_("NativeReceiptId")) && FoodWeaponSourceLedger.expectationMatches(nativePaidWeapon.expectation, nativePaidWeapon.receipt.operation.weapon))) {
                return null;
            }
            InteractionRoot interactionRoot = FoodWeaponSourceLedger.beginExternalInteraction(serverPlayer, rootSite, rawImpact.source, rawImpact.target, (CompoundTag)entity, rawImpact.amount, null, nativePaidWeapon.expectation, nativeProjectileObservation);
            if (interactionRoot == null) {
                return null;
            }
            nativeProjectileObservation.scope = nativeProjectileInteraction = new NativeProjectileInteraction(rawImpact, interactionRoot, nativeProjectileObservation);
            return nativeProjectileInteraction;
        }
        catch (Throwable throwable) {
            nativeProjectileObservation.openingFailure = throwable;
            FoodWeaponSourceLedger.closeNativeProjectileObservation(nativeProjectileObservation);
            nativeProjectileObservation.failure.fail(rawImpact, 0, throwable);
            throw throwable;
        }
    }

    static synchronized void closeNativeProjectileImpact(NativeProjectileInteraction nativeProjectileInteraction, NativeVanillaImpact.RawImpact rawImpact) {
        if (nativeProjectileInteraction != null && nativeProjectileInteraction.observation != null) {
            FoodWeaponSourceLedger.closeNativeProjectileImpact(nativeProjectileInteraction, rawImpact, nativeProjectileInteraction.observation);
            return;
        }
        if (nativeProjectileInteraction == null || nativeProjectileInteraction.closed || nativeProjectileInteraction.call != rawImpact || !FoodWeaponSourceLedger.nativeProjectileImpactCaller(rawImpact) || nativeProjectileInteraction.root.thread != Thread.currentThread() || !nativeProjectileInteraction.root.server.m_18695_()) {
            return;
        }
        if (nativeProjectileInteraction.binding.root != nativeProjectileInteraction.root || nativeProjectileInteraction.binding.source != rawImpact.source || nativeProjectileInteraction.binding.target != rawImpact.target || nativeProjectileInteraction.binding.rawAmountBits != rawImpact.rawAmountBits) {
            return;
        }
        nativeProjectileInteraction.closed = true;
        FoodWeaponSourceLedger.endInteraction(nativeProjectileInteraction.root);
    }

    static synchronized void closeNativeProjectileImpact(NativeProjectileInteraction nativeProjectileInteraction, NativeVanillaImpact.RawImpact rawImpact, NativeProjectileObservation nativeProjectileObservation) {
        if (nativeProjectileObservation == null || nativeProjectileObservation.call != rawImpact || nativeProjectileInteraction != null && nativeProjectileObservation.scope != nativeProjectileInteraction) {
            return;
        }
        if (nativeProjectileObservation.finished || nativeProjectileInteraction != null && nativeProjectileInteraction.closed) {
            return;
        }
        nativeProjectileObservation.finished = true;
        boolean bl = false;
        try {
            if (nativeProjectileInteraction == null || nativeProjectileInteraction.call != rawImpact || !FoodWeaponSourceLedger.nativeProjectileImpactCaller(rawImpact) || nativeProjectileInteraction.root.thread != Thread.currentThread() || !nativeProjectileInteraction.root.server.m_18695_()) {
                return;
            }
            if (nativeProjectileInteraction.binding.root != nativeProjectileInteraction.root || nativeProjectileInteraction.binding.source != rawImpact.source || nativeProjectileInteraction.binding.target != rawImpact.target || nativeProjectileInteraction.binding.rawAmountBits != rawImpact.rawAmountBits) {
                return;
            }
            nativeProjectileInteraction.closed = true;
            FoodWeaponSourceLedger.endInteraction(nativeProjectileInteraction.root);
            bl = true;
        }
        catch (Throwable throwable) {
            nativeProjectileObservation.closingFailure = throwable;
            FoodWeaponSourceLedger.closeNativeProjectileObservation(nativeProjectileObservation);
            nativeProjectileObservation.failure.fail(rawImpact, 3, throwable);
            throw throwable;
        }
        finally {
            nativeProjectileObservation.finished = true;
            if (!bl) {
                FoodWeaponSourceLedger.closeNativeProjectileObservation(nativeProjectileObservation);
            }
        }
    }

    private static OwnerWriterPublication acceptedOriginalPublication(OriginalInteractionBinding originalInteractionBinding) {
        for (int i = 0; originalInteractionBinding != null && i < 16; ++i) {
            OwnerWriterPublication ownerWriterPublication = originalInteractionBinding.acceptedPublication;
            if (ownerWriterPublication != null && ownerWriterPublication.binding == originalInteractionBinding && FoodWeaponSourceLedger.originalPublicationRootLive(ownerWriterPublication)) {
                return ownerWriterPublication;
            }
            originalInteractionBinding = originalInteractionBinding.parent;
        }
        return null;
    }

    private static boolean originalPublicationRootLive(OwnerWriterPublication ownerWriterPublication) {
        if (ownerWriterPublication == null || ownerWriterPublication.closed || ownerWriterPublication.thread != Thread.currentThread() || ownerWriterPublication.fieldSequenceBinding == null) {
            return false;
        }
        OwnerFieldSequenceBinding ownerFieldSequenceBinding = ownerWriterPublication.fieldSequenceBinding;
        InteractionRoot interactionRoot = ownerWriterPublication.binding.root;
        FoodWeaponOriginalSequence.Lineage lineage = ownerFieldSequenceBinding.sequence.lineage;
        return !ownerFieldSequenceBinding.terminal && ownerFieldSequenceBinding.publication == ownerWriterPublication && ownerFieldSequenceBinding.thread == Thread.currentThread() && !interactionRoot.closed && !interactionRoot.serverState.closed && !interactionRoot.ownerState.closed && interactionRoot.thread == Thread.currentThread() && interactionRoot.ownerState.current == interactionRoot && interactionRoot.serverState.epoch == interactionRoot.serverEpoch && interactionRoot.ownerState.epoch == interactionRoot.ownerEpoch && interactionRoot.server.m_18695_() && SERVERS.get(interactionRoot.server) == interactionRoot.serverState && lineage.ownerRoot == interactionRoot && lineage.issuer == ownerWriterPublication && lineage.acceptedCallback == ownerWriterPublication.receipt && lineage.generation == ownerWriterPublication.fieldGeneration && ownerFieldSequenceBinding.sequence.original.continuing(lineage.publication);
    }

    private static boolean originalBindingAncestryLive(OriginalInteractionBinding originalInteractionBinding) {
        if (originalInteractionBinding == null || originalInteractionBinding.closed || originalInteractionBinding.depth <= 0 || originalInteractionBinding.depth > 16) {
            return false;
        }
        OwnerWriterPublication ownerWriterPublication = FoodWeaponSourceLedger.acceptedOriginalPublication(originalInteractionBinding);
        if (ownerWriterPublication == null || ownerWriterPublication.binding.root != originalInteractionBinding.root) {
            return false;
        }
        OriginalInteractionBinding originalInteractionBinding2 = originalInteractionBinding.root.current;
        boolean bl = false;
        for (int i = 0; originalInteractionBinding2 != null && i < 16; ++i) {
            if (originalInteractionBinding2.closed || originalInteractionBinding2.root != originalInteractionBinding.root) {
                return false;
            }
            if (originalInteractionBinding2 == originalInteractionBinding) {
                bl = true;
                break;
            }
            originalInteractionBinding2 = originalInteractionBinding2.parent;
        }
        if (!bl) {
            return false;
        }
        if (originalInteractionBinding.childCall == null) {
            return originalInteractionBinding.snapshot != null;
        }
        FinalListedChildScope finalListedChildScope = originalInteractionBinding.acceptedChildScope;
        return finalListedChildScope != null && finalListedChildScope.binding == originalInteractionBinding && finalListedChildScope.call == originalInteractionBinding.childCall && !finalListedChildScope.closed && finalListedChildScope.hurtEntered && finalListedChildScope.hurtActive && finalListedChildScope.parent == originalInteractionBinding.parent && finalListedChildScope.thread == Thread.currentThread() && finalListedChildScope.originalConsumerChild != null && FoodWeaponFinalDamage.matchesAcceptedOriginalChild(finalListedChildScope.call);
    }

    private static boolean acceptedOriginalEchoPublication(FoodWeaponFinalDamage.OffenseReceipt offenseReceipt, AttemptBatchQuote attemptBatchQuote, AttemptBatchAdmission attemptBatchAdmission, List<AttemptRequest> list, List<TargetRequest> list2, ItemStack itemStack, CompoundTag compoundTag) {
        OriginalInteractionBinding originalInteractionBinding = FoodWeaponSourceLedger.originalInteractionBinding(attemptBatchQuote);
        OwnerWriterPublication ownerWriterPublication = FoodWeaponSourceLedger.acceptedOriginalPublication(originalInteractionBinding);
        return ownerWriterPublication != null && ownerWriterPublication.binding == originalInteractionBinding && ownerWriterPublication.receipt == offenseReceipt && ownerWriterPublication.batch == attemptBatchQuote && ownerWriterPublication.admission == attemptBatchAdmission && ownerWriterPublication.result.status() == AttemptBatchCommitStatus.COMMITTED && ownerWriterPublication.expectation.actual == itemStack && attemptBatchAdmission.preparation.result == ownerWriterPublication.result && ownerWriterPublication.result.batch == attemptBatchQuote && FoodWeaponSourceLedger.matchesAttemptRequests(attemptBatchQuote, list, list2) && originalInteractionBinding.snapshot != null && originalInteractionBinding.snapshot.equals((Object)compoundTag) && FoodWeaponSourceLedger.originalBindingAncestryLive(originalInteractionBinding);
    }

    private static boolean pendingEchoOfferCaller() {
        return NATIVE_CALLER.walk(stream -> stream.filter(stackFrame -> stackFrame.getDeclaringClass() != FoodWeaponSourceLedger.class).findFirst().map(stackFrame -> stackFrame.getDeclaringClass() == FoodSocketAttributePowers.class && stackFrame.getMethodName().equals("commitPreparedOffense") && stackFrame.getDescriptor().equals("(Lnet/farmingtales/ftboss/item/FoodSocketAttributePowers$PreparedOffense;FLnet/farmingtales/ftboss/item/FoodWeaponFinalDamage$OffenseReceipt;)Lnet/farmingtales/ftboss/item/FoodSocketAttributePowers$OffenseCommit;")).orElse(false));
    }

    static PendingEchoLineage issuePendingEchoLineage(FoodSocketAttributePowers.PreparedOffense preparedOffense, FoodSocketAttributePowers.OffensePlan offensePlan, FoodWeaponFinalDamage.OffenseReceipt offenseReceipt, float f, AttemptBatchQuote attemptBatchQuote, AttemptBatchAdmission attemptBatchAdmission, List<AttemptRequest> list, List<TargetRequest> list2, FoodWeaponEchoQueue.Entry<?> entry, ServerPlayer serverPlayer, LivingEntity livingEntity, DamageSource damageSource, long l, double d) {
        Object object;
        boolean bl;
        boolean bl2 = bl = preparedOffense != null && offensePlan != null && offenseReceipt != null && entry != null && FoodSocketAttributePowers.matchesOriginalPendingEchoOffer(preparedOffense, offensePlan, offenseReceipt, f, entry, serverPlayer, livingEntity, damageSource, l, d) && FoodWeaponSourceLedger.acceptedOriginalEchoPublication(offenseReceipt, attemptBatchQuote, attemptBatchAdmission, list, list2, offensePlan.actualWeapon(), offensePlan.snapshot());
        if (!(FoodWeaponSourceLedger.pendingEchoOfferCaller() && preparedOffense != null && offensePlan != null && offenseReceipt != null && entry != null && entry.key() != null && entry.value() != null && serverPlayer != null && livingEntity != null && damageSource != null && FoodWeaponSourceLedger.server(serverPlayer).m_18695_() && FoodWeaponSourceLedger.clock(serverPlayer) == offensePlan.quotedAt() && Float.isFinite(f) && !(f <= 0.0f) && Double.isFinite(d) && !(d < 0.0) && FoodSocketAttributePowers.matchesPreparedOffense(preparedOffense, offensePlan, attemptBatchQuote, attemptBatchAdmission, list, list2) && (bl || offenseReceipt.matchesBatch(attemptBatchQuote, attemptBatchAdmission, list, list2) && offenseReceipt.matches(offensePlan, serverPlayer, damageSource, offensePlan.quotedAt(), f)))) {
            return null;
        }
        if (!(bl || (object = (AttemptResult)offenseReceipt.attempt(AttemptDomain.SERIES_ECHO).orElse(null)) != null && ((AttemptResult)object).domain() == AttemptDomain.SERIES_ECHO && ((AttemptResult)object).commit() == AttemptCommit.COMMITTED_SUCCESS)) {
            return null;
        }
        object = offensePlan.snapshot();
        FoodWeaponEchoQueue.Key key = entry.key();
        if (!(serverPlayer.m_20148_().equals(key.owner()) && offensePlan.weaponId().equals(key.weapon()) && offensePlan.attackId().equals(key.attack()) && offensePlan.originalHand().name().equals(key.originalHand()) && serverPlayer.m_21120_(offensePlan.originalHand()) == offensePlan.actualWeapon() && livingEntity.m_9236_() == serverPlayer.m_9236_() && !livingEntity.m_213877_() && serverPlayer.m_284548_().m_8791_(livingEntity.m_20148_()) == livingEntity && damageSource.m_7639_() == serverPlayer && l == FoodSocketAttributeRules.deadline(offensePlan.quotedAt(), 12) && (bl || object.equals((Object)FoodWeaponSourceLedger.resolveReadOnly(damageSource, serverPlayer)) && FoodWeaponSourceLedger.matchesOriginalInteraction(FoodWeaponSourceLedger.originalInteractionBinding(attemptBatchQuote), (CompoundTag)object, serverPlayer) && FoodWeaponSourceLedger.originalHandContinuousReadOnly((CompoundTag)object, serverPlayer)))) {
            return null;
        }
        OriginalInteractionBinding originalInteractionBinding = FoodWeaponSourceLedger.originalInteractionBinding(attemptBatchQuote);
        if (originalInteractionBinding == null || originalInteractionBinding.source != null && originalInteractionBinding.source != damageSource || originalInteractionBinding.target != null && originalInteractionBinding.target != livingEntity) {
            return null;
        }
        return new PendingEchoLineage(entry, FoodWeaponSourceLedger.server(serverPlayer), (CompoundTag)object, livingEntity.m_20148_(), offensePlan.quotedAt(), l, d, offensePlan.actualWeapon());
    }

    private static boolean pendingEchoSignedImageValid(PendingEchoLineage pendingEchoLineage, ServerPlayer serverPlayer) {
        boolean bl;
        if (pendingEchoLineage == null || serverPlayer == null || pendingEchoLineage.server != FoodWeaponSourceLedger.server(serverPlayer) || !pendingEchoLineage.purpose.equals(pendingEchoLineage.snapshot.m_128461_("DamagePurpose"))) {
            return false;
        }
        CompoundTag compoundTag = pendingEchoLineage.snapshot;
        if (!(serverPlayer != null && compoundTag != null && compoundTag.m_128451_("SnapshotVersion") == 3 && FoodWeaponSourceLedger.canonicalInputValid((Tag)compoundTag) && compoundTag.m_128461_("ServerSignature").matches("[0-9a-f]{64}") && compoundTag.m_128403_("Owner") && compoundTag.m_128342_("Owner").equals(serverPlayer.m_20148_()) && compoundTag.m_128403_("AttackId") && compoundTag.m_128403_("WeaponId") && compoundTag.m_128403_("ServerEpoch") && compoundTag.m_128403_("OwnerEpoch") && !compoundTag.m_128461_("MaterialFingerprint").isEmpty() && compoundTag.m_128425_("Weapon", 10) && compoundTag.m_128425_("CapturedAt", 4) && compoundTag.m_128425_("ExpiresAt", 4) && compoundTag.m_128461_("Dimension").equals(serverPlayer.m_9236_().m_46472_().m_135782_().toString()) && (compoundTag.m_128461_("OriginalHand").equals(InteractionHand.MAIN_HAND.name()) || compoundTag.m_128461_("OriginalHand").equals(InteractionHand.OFF_HAND.name())) && !compoundTag.m_128441_("Hand") && !compoundTag.m_128441_("NativeProfile"))) {
            return false;
        }
        ServerState serverState = SERVERS.get(FoodWeaponSourceLedger.server(serverPlayer));
        if (serverState == null || !OwnerRepairGenerationAuthority.snapshotAllowed(compoundTag)) {
            return false;
        }
        long l = FoodWeaponSourceLedger.clock(serverPlayer);
        long l2 = compoundTag.m_128454_("CapturedAt");
        long l3 = compoundTag.m_128454_("ExpiresAt");
        if (!compoundTag.m_128342_("ServerEpoch").equals(serverState.epoch) || !compoundTag.m_128342_("OwnerEpoch").equals(FoodWeaponSourceLedger.ownerEpoch(serverState, serverPlayer.m_20148_())) || l < serverState.latestTick || l2 < 0L || l2 > 9223372036854774607L || l3 != l2 + 1200L || l < l2 || l >= l3) {
            return false;
        }
        String string = compoundTag.m_128461_("Family");
        String string2 = compoundTag.m_128461_("SourceKind");
        boolean bl2 = bl = string2.equals("TRIDENT") && string.equals("MELEE") && compoundTag.m_128471_("TridentLaunchAuthorized");
        if (!(bl || string2.equals(string) && (string.equals("BOW") || string.equals("STAFF") || string.equals("MELEE")))) {
            return false;
        }
        String string3 = FoodWeaponSourceLedger.signature(compoundTag, serverState);
        if (string3.isEmpty() || !MessageDigest.isEqual(compoundTag.m_128461_("ServerSignature").getBytes(StandardCharsets.US_ASCII), string3.getBytes(StandardCharsets.US_ASCII))) {
            return false;
        }
        ItemStack itemStack = ItemStack.m_41712_((CompoundTag)compoundTag.m_128469_("Weapon"));
        if (!(!itemStack.m_41619_() && itemStack.m_41613_() == 1 && compoundTag.m_128342_("WeaponId").equals(FoodWeaponLifecycle.weaponId(itemStack)) && FoodWeaponSourceLedger.boundProfile(itemStack).equals(compoundTag.m_128461_("BoundProfile")) && FoodWeaponLifecycle.repairProfile(itemStack).equals(compoundTag.m_128461_("RepairProfile")) && FoodWeaponSourceLedger.materialFingerprint(itemStack).equals(compoundTag.m_128461_("MaterialFingerprint")) && FoodWeaponSourceLedger.socketFingerprint(itemStack, string).equals(compoundTag.m_128461_("SocketFingerprint")) && FoodWeaponSourceLedger.foundationContinuous(compoundTag, serverPlayer, itemStack))) {
            return false;
        }
        return !bl || FoodWeaponSourceLedger.isTrident(itemStack) && (!compoundTag.m_128461_("BoundProfile").isEmpty() || FoodWeaponSourceLedger.nativeFoundation(itemStack));
    }

    private static boolean pendingEchoHandContinuous(PendingEchoLineage pendingEchoLineage, ServerPlayer serverPlayer) {
        if (!FoodWeaponSourceLedger.pendingEchoSignedImageValid(pendingEchoLineage, serverPlayer)) {
            return false;
        }
        CompoundTag compoundTag = pendingEchoLineage.snapshot;
        InteractionHand interactionHand = InteractionHand.valueOf((String)compoundTag.m_128461_("OriginalHand"));
        ItemStack itemStack = serverPlayer.m_21120_(interactionHand);
        ItemStack itemStack2 = ItemStack.m_41712_((CompoundTag)compoundTag.m_128469_("Weapon"));
        ResourceLocation resourceLocation = itemStack.m_41619_() ? null : ForgeRegistries.ITEMS.getKey((Object)itemStack.m_41720_());
        ResourceLocation resourceLocation2 = itemStack2.m_41619_() ? null : ForgeRegistries.ITEMS.getKey((Object)itemStack2.m_41720_());
        return itemStack == pendingEchoLineage.originalActual && !itemStack.m_41619_() && itemStack.m_41613_() == 1 && !itemStack2.m_41619_() && itemStack2.m_41613_() == 1 && itemStack.m_41720_() == itemStack2.m_41720_() && resourceLocation != null && resourceLocation.equals((Object)resourceLocation2) && FoodWeaponFamilyBuild.kind(itemStack).name().equals(compoundTag.m_128461_("Family")) && !FoodWeaponLifecycle.isBroken(itemStack) && compoundTag.m_128342_("WeaponId").equals(FoodWeaponLifecycle.weaponId(itemStack)) && FoodWeaponSourceLedger.boundProfile(itemStack).equals(compoundTag.m_128461_("BoundProfile")) && FoodWeaponLifecycle.repairProfile(itemStack).equals(compoundTag.m_128461_("RepairProfile")) && FoodWeaponSourceLedger.materialFingerprint(itemStack).equals(compoundTag.m_128461_("MaterialFingerprint")) && FoodWeaponSourceLedger.socketFingerprint(itemStack, compoundTag.m_128461_("Family")).equals(compoundTag.m_128461_("SocketFingerprint")) && FoodWeaponSourceLedger.foundationContinuous(compoundTag, serverPlayer, itemStack);
    }

    private static boolean pendingEchoLineageMatches(PendingEchoLineage pendingEchoLineage, FoodWeaponEchoQueue.Entry<?> entry, CompoundTag compoundTag, ServerPlayer serverPlayer, UUID uUID, long l, double d) {
        return pendingEchoLineage != null && entry != null && compoundTag != null && uUID != null && pendingEchoLineage.offeredEntry.key() == entry.key() && pendingEchoLineage.offeredEntry.value() == entry.value() && pendingEchoLineage.target.equals(uUID) && pendingEchoLineage.snapshot.equals((Object)compoundTag) && pendingEchoLineage.dueAt == l && l == FoodSocketAttributeRules.deadline(pendingEchoLineage.offeredAt, 12) && Double.doubleToRawLongBits(pendingEchoLineage.rangeSquared) == Double.doubleToRawLongBits(d) && FoodWeaponSourceLedger.pendingEchoHandContinuous(pendingEchoLineage, serverPlayer);
    }

    static boolean pendingEchoSourceContinuous(PendingEchoLineage pendingEchoLineage, FoodWeaponEchoQueue.Entry<?> entry, CompoundTag compoundTag, ServerPlayer serverPlayer, UUID uUID, long l, double d) {
        return pendingEchoLineage != null && !pendingEchoLineage.dispatched && FoodWeaponSourceLedger.pendingEchoLineageMatches(pendingEchoLineage, entry, compoundTag, serverPlayer, uUID, l, d);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    static PendingEchoInteraction beginDurablePendingEchoInteraction(PendingEchoLineage pendingEchoLineage, FoodWeaponEchoQueue.Entry<?> entry, FoodSocketAttributePowers.PendingEchoDispatch pendingEchoDispatch, ServerPlayer serverPlayer, LivingEntity livingEntity, DamageSource damageSource, float f) {
        OwnerState ownerState;
        Object object;
        InteractionRoot interactionRoot = FoodWeaponSourceLedger.projectedRootForPlayer(serverPlayer);
        Object object2 = interactionRoot == null ? null : interactionRoot.ownerState.interactionChange;
        Object object3 = object = interactionRoot == null ? null : interactionRoot.interactionChange;
        if (!FoodWeaponSourceLedger.pendingEchoLifecycleCaller() || pendingEchoLineage == null || pendingEchoLineage.dispatched || serverPlayer == null || livingEntity == null || damageSource == null || Float.floatToRawIntBits(f) != Float.floatToRawIntBits(2.0f) || damageSource.m_7639_() != serverPlayer || damageSource.m_7640_() != null || !damageSource.m_276093_(FoodWeaponDamageSources.SECONDARY) || livingEntity.m_9236_() != serverPlayer.m_9236_() || serverPlayer.m_284548_().m_8791_(livingEntity.m_20148_()) != livingEntity || !FoodSocketAttributePowers.matchesPendingEchoDispatch(pendingEchoDispatch, serverPlayer, livingEntity, damageSource, f)) {
            return null;
        }
        CompoundTag compoundTag = FoodSocketAttributePowers.pendingEchoDispatchSource(pendingEchoDispatch);
        long l = FoodWeaponSourceLedger.clock(serverPlayer);
        if (l < pendingEchoLineage.dueAt || l - pendingEchoLineage.dueAt > 20L || !(livingEntity.m_20280_((Entity)serverPlayer) <= pendingEchoLineage.rangeSquared) || !FoodWeaponSourceLedger.pendingEchoLineageMatches(pendingEchoLineage, entry, compoundTag, serverPlayer, livingEntity.m_20148_(), pendingEchoLineage.dueAt, pendingEchoLineage.rangeSquared) || serverPlayer.m_213877_() || !serverPlayer.m_6084_() || serverPlayer.m_5833_() || !FoodWeaponSourceLedger.server(serverPlayer).m_18695_()) {
            return null;
        }
        ServerState serverState = SERVERS.get(FoodWeaponSourceLedger.server(serverPlayer));
        OwnerState ownerState2 = ownerState = serverState == null ? null : serverState.ownerEpochs.get(serverPlayer.m_20148_());
        if (ownerState == null || ownerState.closed || FoodWeaponSourceLedger.projectedRootForPlayer(serverPlayer) != interactionRoot || interactionRoot != null && (ownerState != interactionRoot.ownerState || ownerState.interactionChange != object2 || interactionRoot.interactionChange != object)) {
            return null;
        }
        pendingEchoLineage.dispatched = true;
        InteractionRoot interactionRoot2 = ownerState.current;
        Object object4 = ownerState.interactionChange;
        if (interactionRoot2 != null) {
            FoodWeaponSourceLedger.retireInteraction(interactionRoot2);
        }
        InteractionRoot interactionRoot3 = new InteractionRoot(ownerState, serverState, serverPlayer, RootSite.DELAYED_ECHO);
        boolean bl = false;
        try {
            interactionRoot3.current = new OriginalInteractionBinding(interactionRoot3, null, damageSource, (Entity)livingEntity, pendingEchoLineage.snapshot, f, null);
            CompoundTag compoundTag2 = pendingEchoLineage.originalActual.m_41739_(new CompoundTag());
            if (!FoodWeaponSourceLedger.canonicalInputValid((Tag)compoundTag2)) {
                FoodWeaponSourceLedger.revokeUnreturnedInteraction(interactionRoot3);
                PendingEchoInteraction pendingEchoInteraction = null;
                return pendingEchoInteraction;
            }
            interactionRoot3.expectation = new WeaponExpectation(pendingEchoLineage.originalActual, compoundTag2);
            interactionRoot3.ownsExpectation = true;
            if (ownerState.current != interactionRoot2 || ownerState.interactionChange != object4 || ownerState.closed || serverState.closed || !FoodWeaponSourceLedger.installRootProjection(interactionRoot3)) {
                FoodWeaponSourceLedger.retireInteraction(interactionRoot3);
                PendingEchoInteraction pendingEchoInteraction = null;
                return pendingEchoInteraction;
            }
            FoodWeaponSourceLedger.setOwnerInteraction(ownerState, interactionRoot3);
            PendingEchoInteraction pendingEchoInteraction = new PendingEchoInteraction(pendingEchoDispatch, interactionRoot3, serverPlayer, livingEntity, damageSource, f, pendingEchoLineage);
            bl = true;
            PendingEchoInteraction pendingEchoInteraction2 = pendingEchoInteraction;
            return pendingEchoInteraction2;
        }
        finally {
            if (!bl) {
                FoodWeaponSourceLedger.revokeUnreturnedInteraction(interactionRoot3);
            }
        }
    }

    private static InteractionRoot beginExternalInteraction(ServerPlayer serverPlayer, RootSite rootSite, DamageSource damageSource, Entity entity, CompoundTag compoundTag, float f) {
        return FoodWeaponSourceLedger.beginExternalInteraction(serverPlayer, rootSite, damageSource, entity, compoundTag, f, null, null);
    }

    private static InteractionRoot beginExternalInteraction(ServerPlayer serverPlayer, RootSite rootSite, DamageSource damageSource, Entity entity, CompoundTag compoundTag, float f, ItemStack itemStack, WeaponExpectation weaponExpectation) {
        return FoodWeaponSourceLedger.beginExternalInteraction(serverPlayer, rootSite, damageSource, entity, compoundTag, f, itemStack, weaponExpectation, null);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static InteractionRoot beginExternalInteraction(ServerPlayer serverPlayer, RootSite rootSite, DamageSource damageSource, Entity entity, CompoundTag compoundTag, float f, ItemStack itemStack, WeaponExpectation weaponExpectation, NativeProjectileObservation nativeProjectileObservation) {
        OwnerState ownerState;
        Object object;
        if (weaponExpectation != null && weaponExpectation.closed) {
            return null;
        }
        InteractionRoot interactionRoot = FoodWeaponSourceLedger.projectedRootForPlayer(serverPlayer);
        Object object2 = interactionRoot == null ? null : interactionRoot.ownerState.interactionChange;
        Object object3 = object = interactionRoot == null ? null : interactionRoot.interactionChange;
        if (serverPlayer == null || compoundTag == null || compoundTag.m_128456_() || serverPlayer.m_9236_().f_46443_ || !FoodWeaponSourceLedger.server(serverPlayer).m_18695_() || serverPlayer.m_213877_() || !serverPlayer.m_6084_() || serverPlayer.m_5833_() || !FoodWeaponSourceLedger.validReadOnly(compoundTag, serverPlayer)) {
            return null;
        }
        ServerState serverState = SERVERS.get(FoodWeaponSourceLedger.server(serverPlayer));
        OwnerState ownerState2 = ownerState = serverState == null ? null : serverState.ownerEpochs.get(serverPlayer.m_20148_());
        if (ownerState == null || ownerState.closed || FoodWeaponSourceLedger.projectedRootForPlayer(serverPlayer) != interactionRoot || interactionRoot != null && (ownerState != interactionRoot.ownerState || ownerState.interactionChange != object2 || interactionRoot.interactionChange != object)) {
            return null;
        }
        InteractionRoot interactionRoot2 = ownerState.current;
        Object object4 = ownerState.interactionChange;
        if (interactionRoot2 != null) {
            FoodWeaponSourceLedger.retireInteraction(interactionRoot2);
        }
        InteractionRoot interactionRoot3 = new InteractionRoot(ownerState, serverState, serverPlayer, rootSite);
        if (nativeProjectileObservation != null) {
            nativeProjectileObservation.root = interactionRoot3;
            nativeProjectileObservation.expectation = weaponExpectation;
            interactionRoot3.nativeProjectileObservation = nativeProjectileObservation;
            interactionRoot3.expectation = weaponExpectation;
        }
        boolean bl = false;
        try {
            Object object5;
            interactionRoot3.current = new OriginalInteractionBinding(interactionRoot3, null, damageSource, entity, compoundTag, f, null);
            if (nativeProjectileObservation != null) {
                nativeProjectileObservation.binding = interactionRoot3.current;
            }
            if (weaponExpectation != null) {
                interactionRoot3.expectation = weaponExpectation;
            } else if (itemStack != null) {
                object5 = itemStack.m_41739_(new CompoundTag());
                if (!FoodWeaponSourceLedger.canonicalInputValid((Tag)object5)) {
                    FoodWeaponSourceLedger.revokeUnreturnedInteraction(interactionRoot3);
                    InteractionRoot interactionRoot4 = null;
                    return interactionRoot4;
                }
                interactionRoot3.expectation = new WeaponExpectation(itemStack, (CompoundTag)object5);
                interactionRoot3.ownsExpectation = true;
            }
            if (interactionRoot3.closed || interactionRoot3.retired || interactionRoot3.expectation != null && interactionRoot3.expectation.closed || nativeProjectileObservation != null && nativeProjectileObservation.failure.failed || ownerState.current != interactionRoot2 || ownerState.interactionChange != object4 || ownerState.closed || serverState.closed || !FoodWeaponSourceLedger.installRootProjection(interactionRoot3)) {
                FoodWeaponSourceLedger.retireInteraction(interactionRoot3);
                object5 = null;
                return object5;
            }
            FoodWeaponSourceLedger.setOwnerInteraction(ownerState, interactionRoot3);
            bl = true;
            object5 = interactionRoot3;
            return object5;
        }
        finally {
            if (!bl) {
                if (nativeProjectileObservation != null) {
                    FoodWeaponSourceLedger.closeNativeProjectileObservation(nativeProjectileObservation);
                }
                FoodWeaponSourceLedger.revokeUnreturnedInteraction(interactionRoot3);
            }
        }
    }

    private static synchronized void retireInteraction(InteractionRoot interactionRoot) {
        ArrayList<OriginalInteractionBinding> arrayList = new ArrayList<OriginalInteractionBinding>();
        FoodWeaponSourceLedger.removeRootProjection(interactionRoot);
        FoodWeaponSourceLedger.closeOwnedExpectation(interactionRoot);
        interactionRoot.interactionChange = new Object();
        if (interactionRoot.writerPublication != null) {
            FoodWeaponSourceLedger.closeOwnerWriterPublication(interactionRoot.writerPublication);
        }
        interactionRoot.retired = true;
        interactionRoot.closed = true;
        OriginalInteractionBinding originalInteractionBinding = interactionRoot.current;
        for (int i = 16; originalInteractionBinding != null && i > 0; --i) {
            if (!originalInteractionBinding.closed) {
                arrayList.add(originalInteractionBinding);
            }
            originalInteractionBinding.closed = true;
            originalInteractionBinding = originalInteractionBinding.parent;
        }
        interactionRoot.current = null;
        for (OriginalInteractionBinding originalInteractionBinding2 : arrayList) {
            FoodWeaponFinalDamage.retireOriginalCapture(originalInteractionBinding2);
        }
    }

    private static synchronized void retireOwnerInteraction(ServerPlayer serverPlayer) {
        OwnerState ownerState;
        if (serverPlayer == null || !FoodWeaponSourceLedger.server(serverPlayer).m_18695_()) {
            return;
        }
        ServerState serverState = SERVERS.get(FoodWeaponSourceLedger.server(serverPlayer));
        OwnerState ownerState2 = ownerState = serverState == null ? null : serverState.ownerEpochs.get(serverPlayer.m_20148_());
        if (ownerState != null && ownerState.current != null) {
            InteractionRoot interactionRoot = ownerState.current;
            FoodWeaponSourceLedger.retireInteraction(interactionRoot);
            if (ownerState.current == interactionRoot) {
                FoodWeaponSourceLedger.setOwnerInteraction(ownerState, null);
            }
        }
    }

    private static synchronized void endInteraction(InteractionRoot interactionRoot) {
        if (interactionRoot == null || interactionRoot.thread != Thread.currentThread() || !interactionRoot.server.m_18695_()) {
            return;
        }
        FoodWeaponSourceLedger.retireInteraction(interactionRoot);
        if (interactionRoot.ownerState.current == interactionRoot) {
            FoodWeaponSourceLedger.setOwnerInteraction(interactionRoot.ownerState, null);
        }
    }

    private static void endOriginalInteractionBinding(OriginalInteractionBinding originalInteractionBinding) {
        if (originalInteractionBinding == null || originalInteractionBinding.root.thread != Thread.currentThread() || !originalInteractionBinding.root.server.m_18695_()) {
            return;
        }
        boolean bl = !originalInteractionBinding.closed;
        originalInteractionBinding.closed = true;
        if (originalInteractionBinding.root.current == originalInteractionBinding) {
            originalInteractionBinding.root.readCut.change();
            originalInteractionBinding.root.interactionChange = new Object();
            OriginalInteractionBinding originalInteractionBinding2 = originalInteractionBinding.parent;
            OriginalInteractionBinding originalInteractionBinding3 = originalInteractionBinding.root.current = originalInteractionBinding2 != null && !originalInteractionBinding2.closed && (FoodWeaponSourceLedger.interactionRootLive(originalInteractionBinding.root, originalInteractionBinding.root.player) || FoodWeaponSourceLedger.acceptedOriginalPublication(originalInteractionBinding2) != null) ? originalInteractionBinding2 : null;
        }
        if (bl) {
            FoodWeaponFinalDamage.retireOriginalCapture(originalInteractionBinding);
        }
    }

    private static InteractionRoot beginNativeImpactInteraction(NativeHurtScope nativeHurtScope, float f) {
        if (nativeHurtScope == null || nativeHurtScope.interaction != null || !FoodWeaponSourceLedger.nativeHurtLive(nativeHurtScope) || !Float.isFinite(f) || f <= 0.0f || Float.floatToRawIntBits(nativeHurtScope.snapshot.m_128457_("NativeAmount")) != Float.floatToRawIntBits(f)) {
            return null;
        }
        RootSite rootSite = nativeHurtScope.ironCallback != null ? RootSite.NATIVE_IRON_IMPACT : (nativeHurtScope.earlyIngress != null ? RootSite.NATIVE_MEAT_IMPACT : RootSite.NATIVE_CARRIER_IMPACT);
        InteractionRoot interactionRoot = FoodWeaponSourceLedger.beginExternalInteraction(nativeHurtScope.player, rootSite, nativeHurtScope.source, nativeHurtScope.target, nativeHurtScope.snapshot, f, null, nativeHurtScope.expectation);
        return interactionRoot;
    }

    private static synchronized void closeNativeHurtScope(NativeHurtScope nativeHurtScope) {
        if (nativeHurtScope == null || nativeHurtScope.thread != Thread.currentThread() || !FoodWeaponSourceLedger.server(nativeHurtScope.player).m_18695_()) {
            return;
        }
        nativeHurtScope.closed = true;
        FoodWeaponSourceLedger.endInteraction(nativeHurtScope.interaction);
        if (PRIVATE_NATIVE_HURT.get() == nativeHurtScope) {
            PRIVATE_NATIVE_HURT.remove();
        }
    }

    private static boolean genericCastCaller() {
        return NATIVE_CALLER.walk(stream -> stream.filter(stackFrame -> stackFrame.getDeclaringClass() != FoodWeaponSourceLedger.class).findFirst().map(stackFrame -> stackFrame.getDeclaringClass() == WeaponEvolutionSkills.class && stackFrame.getMethodName().equals("cast") && stackFrame.getDescriptor().equals("(Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/world/item/ItemStack;)V")).orElse(false));
    }

    public static InteractionRoot beginGenericInteraction(ServerPlayer serverPlayer, ItemStack itemStack, CompoundTag compoundTag) {
        if (!(FoodWeaponSourceLedger.genericCastCaller() && serverPlayer != null && itemStack == serverPlayer.m_21205_() && compoundTag != null && "GENERIC_J".equals(compoundTag.m_128461_("DamagePurpose")) && FoodWeaponSourceLedger.attemptSourceValid(compoundTag, serverPlayer))) {
            return null;
        }
        return FoodWeaponSourceLedger.beginExternalInteraction(serverPlayer, RootSite.GENERIC_CAST, null, null, compoundTag, Float.NaN, itemStack, null);
    }

    public static synchronized void endGenericInteraction(InteractionRoot interactionRoot) {
        if (!FoodWeaponSourceLedger.genericCastCaller() || interactionRoot == null || interactionRoot.site != RootSite.GENERIC_CAST) {
            return;
        }
        FoodWeaponSourceLedger.endInteraction(interactionRoot);
    }

    private static boolean finalChildCaller() {
        return NATIVE_CALLER.walk(stream -> stream.filter(stackFrame -> stackFrame.getDeclaringClass() != FoodWeaponSourceLedger.class).findFirst().map(stackFrame -> stackFrame.getDeclaringClass() == FoodWeaponFinalDamage.class && stackFrame.getMethodName().equals("hurtListedChild") && stackFrame.getDescriptor().equals("(Lnet/farmingtales/ftboss/item/FoodWeaponFinalDamage$ChildHurtBridge;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/damagesource/DamageSource;F)Z")).orElse(false));
    }

    static FinalListedChildScope enterFinalListedChild(FoodWeaponFinalDamage.ChildHurtCall childHurtCall) {
        Object object;
        if (!FoodWeaponSourceLedger.finalChildCaller() || childHurtCall == null || !FoodWeaponFinalDamage.matchesOwnerChildCall(childHurtCall)) {
            return null;
        }
        OriginalInteractionBinding originalInteractionBinding = FoodWeaponFinalDamage.ownerChildOriginalBinding(childHurtCall);
        DamageSource damageSource = FoodWeaponFinalDamage.ownerChildSource(childHurtCall);
        LivingEntity livingEntity = FoodWeaponFinalDamage.ownerChildTarget(childHurtCall);
        float f = FoodWeaponFinalDamage.ownerChildAmount(childHurtCall);
        FoodWeaponConsumerSequence.OriginalChild originalChild = FoodWeaponFinalDamage.originalConsumerChild(childHurtCall);
        OwnerWriterPublication ownerWriterPublication = originalChild == null ? null : FoodWeaponSourceLedger.acceptedOriginalPublication(originalInteractionBinding);
        boolean bl = originalChild != null && ownerWriterPublication != null && FoodWeaponFinalDamage.matchesAcceptedOriginalChild(childHurtCall) && FoodWeaponSourceLedger.originalBindingAncestryLive(originalInteractionBinding);
        Object object2 = object = originalInteractionBinding == null ? null : originalInteractionBinding.root.interactionChange;
        if (originalInteractionBinding == null || damageSource == null || livingEntity == null || !Float.isFinite(f) || f <= 0.0f || originalInteractionBinding.depth >= 16 || !bl && (!FoodWeaponSourceLedger.interactionMatches(originalInteractionBinding, originalInteractionBinding.snapshot, originalInteractionBinding.root.player) || !FoodWeaponSourceLedger.attemptSourceValid(originalInteractionBinding.snapshot, originalInteractionBinding.root.player)) || livingEntity.m_213877_() || livingEntity.m_9236_() != originalInteractionBinding.root.player.m_9236_() || originalInteractionBinding.root.player.m_284548_().m_8791_(livingEntity.m_20148_()) != livingEntity) {
            return null;
        }
        FinalListedChildScope finalListedChildScope = new FinalListedChildScope(childHurtCall, originalInteractionBinding, damageSource, livingEntity, f);
        finalListedChildScope.originalConsumerChild = originalChild;
        finalListedChildScope.originalPublisher = ownerWriterPublication;
        if (!bl && !FoodWeaponSourceLedger.projectedCurrentBindingLive(originalInteractionBinding) || bl && !FoodWeaponSourceLedger.originalBindingAncestryLive(originalInteractionBinding) || originalInteractionBinding.root.interactionChange != object) {
            return null;
        }
        finalListedChildScope.root.readCut.change();
        finalListedChildScope.root.interactionChange = new Object();
        finalListedChildScope.root.current = finalListedChildScope.binding;
        return finalListedChildScope;
    }

    private static boolean matchesActiveFinalListedChild(FinalListedChildScope finalListedChildScope, DamageSource damageSource, LivingEntity livingEntity, float f) {
        if (finalListedChildScope == null || finalListedChildScope.closed || !finalListedChildScope.hurtEntered || !finalListedChildScope.hurtActive || finalListedChildScope.thread != Thread.currentThread() || !(finalListedChildScope.originalConsumerChild != null ? FoodWeaponSourceLedger.originalPublicationRootLive(finalListedChildScope.originalPublisher) : FoodWeaponSourceLedger.interactionRootLive(finalListedChildScope.root, finalListedChildScope.root.player)) || finalListedChildScope.root.current != finalListedChildScope.binding || finalListedChildScope.binding.closed || finalListedChildScope.parent.closed || finalListedChildScope.parent.root != finalListedChildScope.root || finalListedChildScope.binding.parent != finalListedChildScope.parent || finalListedChildScope.binding.childCall != finalListedChildScope.call || finalListedChildScope.binding.snapshot == null || finalListedChildScope.parent.snapshot == null || !finalListedChildScope.binding.snapshot.equals((Object)finalListedChildScope.parent.snapshot) || finalListedChildScope.binding.depth != finalListedChildScope.parent.depth + 1 || finalListedChildScope.binding.depth > 16 || damageSource != finalListedChildScope.source || livingEntity != finalListedChildScope.target || damageSource != finalListedChildScope.binding.source || livingEntity != finalListedChildScope.binding.target || !Float.isFinite(f) || f <= 0.0f || livingEntity.m_213877_() || livingEntity.m_9236_() != finalListedChildScope.root.player.m_9236_() || finalListedChildScope.root.player.m_284548_().m_8791_(livingEntity.m_20148_()) != livingEntity || Float.floatToRawIntBits(f) != finalListedChildScope.binding.rawAmountBits || Float.floatToRawIntBits(f) != Float.floatToRawIntBits(finalListedChildScope.rawAmount) || !FoodWeaponFinalDamage.matchesOwnerChildCall(finalListedChildScope.call)) {
            return false;
        }
        return FoodWeaponFinalDamage.ownerChildOriginalBinding(finalListedChildScope.call) == finalListedChildScope.parent && FoodWeaponFinalDamage.ownerChildSource(finalListedChildScope.call) == damageSource && FoodWeaponFinalDamage.ownerChildTarget(finalListedChildScope.call) == livingEntity && Float.floatToRawIntBits(FoodWeaponFinalDamage.ownerChildAmount(finalListedChildScope.call)) == Float.floatToRawIntBits(f);
    }

    static boolean hurtFinalListedChild(FinalListedChildScope finalListedChildScope) {
        if (!FoodWeaponSourceLedger.finalChildCaller() || finalListedChildScope == null || finalListedChildScope.closed || finalListedChildScope.hurtEntered || finalListedChildScope.thread != Thread.currentThread() || !finalListedChildScope.root.server.m_18695_()) {
            return false;
        }
        finalListedChildScope.hurtEntered = true;
        finalListedChildScope.hurtActive = true;
        try {
            if (!FoodWeaponSourceLedger.matchesActiveFinalListedChild(finalListedChildScope, finalListedChildScope.source, finalListedChildScope.target, finalListedChildScope.rawAmount)) {
                boolean bl = false;
                return bl;
            }
            boolean bl = finalListedChildScope.target.m_6469_(finalListedChildScope.source, finalListedChildScope.rawAmount);
            return bl;
        }
        finally {
            finalListedChildScope.hurtActive = false;
        }
    }

    static void leaveFinalListedChild(FinalListedChildScope finalListedChildScope) {
        if (!FoodWeaponSourceLedger.finalChildCaller() || finalListedChildScope == null || finalListedChildScope.closed || finalListedChildScope.hurtActive || finalListedChildScope.thread != Thread.currentThread() || !finalListedChildScope.root.server.m_18695_()) {
            return;
        }
        finalListedChildScope.closed = true;
        FoodWeaponSourceLedger.endOriginalInteractionBinding(finalListedChildScope.binding);
    }

    private FoodWeaponSourceLedger() {
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static ServerState state(MinecraftServer minecraftServer) {
        Class<FoodWeaponSourceLedger> clazz = FoodWeaponSourceLedger.class;
        synchronized (FoodWeaponSourceLedger.class) {
            ServerState serverState = SERVERS.computeIfAbsent(minecraftServer, ServerState::new);
            // ** MonitorExit[var2_1] (shouldn't be in output)
            serverState.initializeStore(minecraftServer);
            return serverState;
        }
    }

    private static MinecraftServer server(ServerPlayer serverPlayer) {
        return serverPlayer.m_284548_().m_7654_();
    }

    public static long clock(ServerPlayer serverPlayer) {
        return FoodWeaponSourceLedger.server(serverPlayer).m_129783_().m_46467_();
    }

    private static NativeKind nativeKind(Item item, ItemStack itemStack) {
        String string = FourWeaponFoundationAdapter.registryId(itemStack);
        if (item != itemStack.m_41720_()) {
            return null;
        }
        if (item.getClass() == BowItem.class && string.equals("minecraft:bow")) {
            return NativeKind.BOW;
        }
        if (item.getClass() == CrossbowItem.class && string.equals("minecraft:crossbow")) {
            return NativeKind.CROSSBOW;
        }
        if (item.getClass() == TridentItem.class && string.equals("minecraft:trident")) {
            return NativeKind.TRIDENT;
        }
        return null;
    }

    private static NativeOperation beginNative(Item item, ItemStack itemStack, Level level, LivingEntity livingEntity, InteractionHand interactionHand) {
        ServerPlayer serverPlayer;
        NativeKind nativeKind = FoodWeaponSourceLedger.nativeKind(item, itemStack);
        if (nativeKind == null || level.f_46443_ || !(livingEntity instanceof ServerPlayer) || (serverPlayer = (ServerPlayer)livingEntity).m_9236_() != level || interactionHand == null || serverPlayer.m_21120_(interactionHand) != itemStack || itemStack.m_41613_() != 1 || !FoodWeaponSourceLedger.server(serverPlayer).m_18695_() || itemStack.m_41773_() >= itemStack.m_41776_() - 1 || itemStack.m_41783_() != null && !FoodWeaponSourceLedger.canonicalInputValid((Tag)itemStack.m_41783_()) || FoodWeaponMaterialFingerprint.foundationSnapshot(itemStack).m_128456_() || FoodWeaponSourceLedger.foundationRules(serverPlayer, itemStack).isEmpty()) {
            return null;
        }
        FoodWeaponLifecycle.ensureTracked(itemStack);
        if (!FoodWeaponSourceLedger.canonicalInputValid((Tag)itemStack.m_41739_(new CompoundTag()))) {
            return null;
        }
        FoodWeaponSourceLedger.ensureOwnerState(FoodWeaponSourceLedger.state(FoodWeaponSourceLedger.server(serverPlayer)), serverPlayer.m_20148_());
        FoodWeaponSourceLedger.retireOwnerInteraction(serverPlayer);
        return new NativeOperation(nativeKind, serverPlayer, itemStack, interactionHand);
    }

    private static boolean liveNative(NativeOperation nativeOperation) {
        return nativeOperation != null && OwnerRepairGenerationAuthority.matches(nativeOperation.weapon, nativeOperation.repairGeneration) && !nativeOperation.invalid && OwnerFullRepairGuard.permits(nativeOperation.weapon) && nativeOperation.thread == Thread.currentThread() && nativeOperation.player.m_6084_() && nativeOperation.dimension.equals(nativeOperation.player.m_9236_().m_46472_().m_135782_().toString()) && nativeOperation.server == FoodWeaponSourceLedger.server(nativeOperation.player) && nativeOperation.tick == FoodWeaponSourceLedger.clock(nativeOperation.player) && nativeOperation.epoch.equals(FoodWeaponSourceLedger.state((MinecraftServer)nativeOperation.server).epoch) && Objects.equals(nativeOperation.ownerEpoch, FoodWeaponSourceLedger.ownerEpoch(FoodWeaponSourceLedger.state(nativeOperation.server), nativeOperation.player.m_20148_()));
    }

    private static boolean nativeCaller(NativeKind nativeKind, String string) {
        Class<BowItem> clazz = switch (nativeKind) {
            default -> throw new IncompatibleClassChangeError();
            case NativeKind.BOW -> BowItem.class;
            case NativeKind.CROSSBOW -> CrossbowItem.class;
            case NativeKind.TRIDENT -> TridentItem.class;
        };
        return NATIVE_CALLER.walk(stream -> stream.filter(stackFrame -> stackFrame.getDeclaringClass() != FoodWeaponSourceLedger.class).findFirst().map(stackFrame -> stackFrame.getDeclaringClass() == clazz && stackFrame.getMethodName().contains("ftboss$v6$" + string)).orElse(false));
    }

    private static boolean nativeMaterialCaller(String string, String string2) {
        return NATIVE_CALLER.walk(stream -> stream.filter(stackFrame -> stackFrame.getDeclaringClass() != FoodWeaponSourceLedger.class).findFirst().map(stackFrame -> stackFrame.getDeclaringClass() == FoodWeaponRanged.class && stackFrame.getMethodName().equals(string) && stackFrame.getDescriptor().equals(string2)).orElse(false));
    }

    private static boolean nativeMaterialCaptureCaller() {
        return FoodWeaponSourceLedger.nativeMaterialCaller("captureBound", "(Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/world/item/ItemStack;Lnet/farmingtales/ftboss/util/FoodWeaponFamilyRules$Kind;Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/nbt/CompoundTag;");
    }

    private static boolean nativeMaterialLaunchCaller() {
        return FoodWeaponSourceLedger.nativeMaterialCaller("launch", "(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/nbt/CompoundTag;)V");
    }

    private static boolean nativeMaterialOperationCurrent(NativeOperation nativeOperation) {
        return nativeOperation != null && !nativeOperation.invalid && nativeOperation.thread == Thread.currentThread() && NATIVE_OPERATION.get() == nativeOperation && NATIVE_DEPTH.get() == 1 && (nativeOperation.kind == NativeKind.BOW || nativeOperation.kind == NativeKind.CROSSBOW);
    }

    public static synchronized NativeMaterialCapture nativeMaterialCaptureEntered(ServerPlayer serverPlayer, ItemStack itemStack, InteractionHand interactionHand) {
        NativeOperation nativeOperation = NATIVE_OPERATION.get();
        if (nativeOperation == null || nativeOperation.thread != Thread.currentThread() || nativeOperation.player != serverPlayer || nativeOperation.weapon != itemStack || nativeOperation.hand != interactionHand || nativeOperation.kind == NativeKind.TRIDENT) {
            return null;
        }
        nativeOperation.materialAttempted = true;
        if (nativeOperation.materialRejected || nativeOperation.materialCapture != null) {
            nativeOperation.materialRejected = true;
            return null;
        }
        try {
            NativeMaterialCapture nativeMaterialCapture;
            if (!FoodWeaponSourceLedger.nativeMaterialCaptureCaller() || !FoodWeaponSourceLedger.nativeMaterialOperationCurrent(nativeOperation)) {
                nativeOperation.materialRejected = true;
                return null;
            }
            nativeOperation.materialCapture = nativeMaterialCapture = new NativeMaterialCapture(nativeOperation);
            return nativeMaterialCapture;
        }
        catch (Throwable throwable) {
            nativeOperation.materialRejected = true;
            throw throwable;
        }
    }

    public static synchronized void nativeMaterialCaptureFailed(NativeMaterialCapture nativeMaterialCapture, Throwable throwable) {
        if (nativeMaterialCapture == null || nativeMaterialCapture.operation.thread != Thread.currentThread()) {
            return;
        }
        nativeMaterialCapture.failed = true;
        nativeMaterialCapture.operation.materialRejected = true;
        if (nativeMaterialCapture.originalFailure == null) {
            nativeMaterialCapture.originalFailure = throwable;
        }
    }

    private static boolean nativeMaterialPendingCaller() {
        return FoodWeaponSourceLedger.nativeMaterialCaller("prepareBowShot", "(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/nbt/CompoundTag;)V");
    }

    private static boolean nativeMaterialPendingReadCaller() {
        return FoodWeaponSourceLedger.nativeMaterialCaller("launchPreparedBow", "(Lnet/minecraft/world/entity/Entity;)V");
    }

    private static boolean nativeMaterialTransferCurrent(NativeMaterialTransfer nativeMaterialTransfer) {
        if (nativeMaterialTransfer == null) {
            return false;
        }
        NativeReceipt nativeReceipt = nativeMaterialTransfer.receipt;
        NativeOperation nativeOperation = nativeReceipt.operation;
        return !nativeMaterialTransfer.rejected && !nativeReceipt.materialRejected && !nativeOperation.materialRejected && FoodWeaponSourceLedger.nativeMaterialOperationCurrent(nativeOperation) && nativeOperation.current == nativeReceipt && nativeReceipt.materialTransfer == nativeMaterialTransfer && nativeReceipt.entity == nativeMaterialTransfer.entity && nativeMaterialTransfer.capture == nativeOperation.materialCapture && !nativeMaterialTransfer.capture.failed && nativeMaterialTransfer.capture.image != null && nativeMaterialTransfer.capturedReturn == nativeMaterialTransfer.capture.originalReturnedSnapshot;
    }

    private static void rejectNativeMaterialTransfer(NativeMaterialTransfer nativeMaterialTransfer) {
        if (nativeMaterialTransfer == null || nativeMaterialTransfer.receipt.operation.thread != Thread.currentThread()) {
            return;
        }
        nativeMaterialTransfer.rejected = true;
        nativeMaterialTransfer.receipt.materialRejected = true;
        NativeMaterialPublication nativeMaterialPublication = nativeMaterialTransfer.receipt.material;
        if (nativeMaterialPublication != null && nativeMaterialPublication.transfer == nativeMaterialTransfer) {
            nativeMaterialPublication.phase = NativeMaterialPhase.REJECTED;
            if (nativeMaterialPublication.paid != null) {
                nativeMaterialPublication.paid.expectation.closed = true;
            }
        }
    }

    public static synchronized void nativeMaterialTransferFailed(NativeMaterialTransfer nativeMaterialTransfer, Throwable throwable) {
        FoodWeaponSourceLedger.rejectNativeMaterialTransfer(nativeMaterialTransfer);
        if (nativeMaterialTransfer != null && nativeMaterialTransfer.receipt.operation.thread == Thread.currentThread() && nativeMaterialTransfer.originalFailure == null) {
            nativeMaterialTransfer.originalFailure = throwable;
        }
    }

    public static synchronized void nativeMaterialTransferObservationFailed(NativeMaterialTransfer nativeMaterialTransfer, Throwable throwable) {
        FoodWeaponSourceLedger.rejectNativeMaterialTransfer(nativeMaterialTransfer);
        if (nativeMaterialTransfer != null && nativeMaterialTransfer.receipt.operation.thread == Thread.currentThread() && nativeMaterialTransfer.observationFailure == null) {
            nativeMaterialTransfer.observationFailure = throwable;
        }
    }

    public static synchronized NativeMaterialTransfer nativeMaterialPendingEntered(Entity entity, CompoundTag compoundTag) {
        NativeReceipt nativeReceipt;
        NativeOperation nativeOperation = NATIVE_OPERATION.get();
        NativeReceipt nativeReceipt2 = nativeReceipt = nativeOperation == null ? null : nativeOperation.current;
        if (nativeOperation == null || nativeOperation.thread != Thread.currentThread() || nativeOperation.kind != NativeKind.BOW || nativeReceipt == null || nativeReceipt.entity != entity) {
            return null;
        }
        nativeReceipt.materialAttempted = true;
        try {
            NativeMaterialTransfer nativeMaterialTransfer;
            NativeMaterialCapture nativeMaterialCapture = nativeOperation.materialCapture;
            if (!FoodWeaponSourceLedger.nativeMaterialPendingCaller() || !FoodWeaponSourceLedger.nativeMaterialOperationCurrent(nativeOperation) || nativeOperation.materialRejected || nativeReceipt.materialRejected || nativeReceipt.materialTransfer != null || nativeMaterialCapture == null || nativeMaterialCapture.failed || nativeMaterialCapture.image == null || nativeMaterialCapture.originalReturnedSnapshot != compoundTag || compoundTag == null || nativeReceipt.added || nativeReceipt.consumed) {
                nativeReceipt.materialRejected = true;
                return null;
            }
            nativeReceipt.materialTransfer = nativeMaterialTransfer = new NativeMaterialTransfer(nativeReceipt, nativeMaterialCapture, entity);
            return nativeMaterialTransfer;
        }
        catch (Throwable throwable) {
            nativeReceipt.materialRejected = true;
            throw throwable;
        }
    }

    public static synchronized void nativeMaterialPendingCopied(NativeMaterialTransfer nativeMaterialTransfer, CompoundTag compoundTag, String string, CompoundTag compoundTag2, CompoundTag compoundTag3) {
        if (nativeMaterialTransfer == null || nativeMaterialTransfer.receipt.operation.thread != Thread.currentThread()) {
            return;
        }
        try {
            if (!(FoodWeaponSourceLedger.nativeMaterialPendingCaller() && FoodWeaponSourceLedger.nativeMaterialTransferCurrent(nativeMaterialTransfer) && nativeMaterialTransfer.pendingCopy == null && nativeMaterialTransfer.receipt.operation.kind == NativeKind.BOW && nativeMaterialTransfer.capturedReturn == compoundTag2 && compoundTag3 != null && compoundTag3 != compoundTag2 && compoundTag != null && compoundTag.getClass() == CompoundTag.class && "FTBossPendingBowShot".equals(string))) {
                FoodWeaponSourceLedger.rejectNativeMaterialTransfer(nativeMaterialTransfer);
                return;
            }
            nativeMaterialTransfer.pendingReceiver = compoundTag;
            nativeMaterialTransfer.pendingKey = string;
            nativeMaterialTransfer.pendingCopy = compoundTag3;
            if (!OwnerNativeMaterialImage.freeze(compoundTag3).same(nativeMaterialTransfer.capture.image) || !FoodWeaponSourceLedger.nativeMaterialTransferCurrent(nativeMaterialTransfer)) {
                FoodWeaponSourceLedger.rejectNativeMaterialTransfer(nativeMaterialTransfer);
            }
        }
        catch (Throwable throwable) {
            nativeMaterialTransfer.observationFailure = throwable;
            FoodWeaponSourceLedger.rejectNativeMaterialTransfer(nativeMaterialTransfer);
            throw throwable;
        }
    }

    public static synchronized void nativeMaterialPendingReturned(NativeMaterialTransfer nativeMaterialTransfer, Tag tag) {
        if (nativeMaterialTransfer == null || nativeMaterialTransfer.receipt.operation.thread != Thread.currentThread()) {
            return;
        }
        if (!FoodWeaponSourceLedger.nativeMaterialPendingCaller() || !FoodWeaponSourceLedger.nativeMaterialTransferCurrent(nativeMaterialTransfer) || nativeMaterialTransfer.pendingReturned || nativeMaterialTransfer.pendingCopy == null || tag != null) {
            FoodWeaponSourceLedger.rejectNativeMaterialTransfer(nativeMaterialTransfer);
            return;
        }
        nativeMaterialTransfer.pendingReturned = true;
    }

    public static synchronized NativeMaterialTransfer nativeMaterialPendingReadEntered(Entity entity) {
        NativeReceipt nativeReceipt;
        NativeOperation nativeOperation = NATIVE_OPERATION.get();
        NativeReceipt nativeReceipt2 = nativeReceipt = nativeOperation == null ? null : nativeOperation.current;
        if (nativeOperation == null || nativeOperation.thread != Thread.currentThread() || nativeOperation.kind != NativeKind.BOW || nativeReceipt == null || nativeReceipt.entity != entity) {
            return null;
        }
        nativeReceipt.materialAttempted = true;
        NativeMaterialTransfer nativeMaterialTransfer = nativeReceipt.materialTransfer;
        try {
            if (!FoodWeaponSourceLedger.nativeMaterialPendingReadCaller() || !FoodWeaponSourceLedger.nativeMaterialTransferCurrent(nativeMaterialTransfer) || !nativeMaterialTransfer.pendingReturned || nativeMaterialTransfer.pendingReadEntered || nativeMaterialTransfer.pendingTaken) {
                nativeReceipt.materialRejected = true;
                FoodWeaponSourceLedger.rejectNativeMaterialTransfer(nativeMaterialTransfer);
                return null;
            }
            nativeMaterialTransfer.pendingReadEntered = true;
            return nativeMaterialTransfer;
        }
        catch (Throwable throwable) {
            nativeReceipt.materialRejected = true;
            FoodWeaponSourceLedger.rejectNativeMaterialTransfer(nativeMaterialTransfer);
            throw throwable;
        }
    }

    public static synchronized void nativeMaterialPendingTaken(NativeMaterialTransfer nativeMaterialTransfer, CompoundTag compoundTag, String string, CompoundTag compoundTag2, CompoundTag compoundTag3) {
        if (nativeMaterialTransfer == null || nativeMaterialTransfer.receipt.operation.thread != Thread.currentThread()) {
            return;
        }
        if (!(FoodWeaponSourceLedger.nativeMaterialPendingReadCaller() && FoodWeaponSourceLedger.nativeMaterialTransferCurrent(nativeMaterialTransfer) && nativeMaterialTransfer.pendingReadEntered && nativeMaterialTransfer.pendingReturned && !nativeMaterialTransfer.pendingTaken && nativeMaterialTransfer.pendingReceiver == compoundTag && nativeMaterialTransfer.pendingReceiver == compoundTag3 && nativeMaterialTransfer.pendingKey.equals(string) && nativeMaterialTransfer.pendingCopy == compoundTag2)) {
            FoodWeaponSourceLedger.rejectNativeMaterialTransfer(nativeMaterialTransfer);
            return;
        }
        nativeMaterialTransfer.pendingTaken = true;
    }

    public static synchronized NativeMaterialTransfer nativeMaterialLaunchEntered(Entity entity, CompoundTag compoundTag) {
        NativeReceipt nativeReceipt;
        NativeOperation nativeOperation = NATIVE_OPERATION.get();
        NativeReceipt nativeReceipt2 = nativeReceipt = nativeOperation == null ? null : nativeOperation.current;
        if (nativeOperation == null || nativeOperation.thread != Thread.currentThread() || nativeOperation.kind == NativeKind.TRIDENT || nativeReceipt == null || nativeReceipt.entity != entity) {
            return null;
        }
        nativeReceipt.materialAttempted = true;
        NativeMaterialTransfer nativeMaterialTransfer = nativeReceipt.materialTransfer;
        try {
            NativeMaterialCapture nativeMaterialCapture = nativeOperation.materialCapture;
            if (!FoodWeaponSourceLedger.nativeMaterialLaunchCaller() || !FoodWeaponSourceLedger.nativeMaterialOperationCurrent(nativeOperation) || nativeOperation.materialRejected || nativeReceipt.materialRejected || nativeMaterialCapture == null || nativeMaterialCapture.failed || nativeMaterialCapture.image == null || !nativeReceipt.worn || nativeReceipt.added || nativeReceipt.consumed) {
                nativeReceipt.materialRejected = true;
                return null;
            }
            if (nativeOperation.kind == NativeKind.CROSSBOW) {
                if (nativeMaterialTransfer != null || nativeMaterialCapture.originalReturnedSnapshot != compoundTag || compoundTag == null) {
                    nativeReceipt.materialRejected = true;
                    return null;
                }
                nativeReceipt.materialTransfer = nativeMaterialTransfer = new NativeMaterialTransfer(nativeReceipt, nativeMaterialCapture, entity);
            } else if (!FoodWeaponSourceLedger.nativeMaterialTransferCurrent(nativeMaterialTransfer) || !nativeMaterialTransfer.pendingTaken || nativeMaterialTransfer.pendingCopy != compoundTag) {
                nativeReceipt.materialRejected = true;
                return null;
            }
            if (nativeMaterialTransfer.launchEntered) {
                FoodWeaponSourceLedger.rejectNativeMaterialTransfer(nativeMaterialTransfer);
                return null;
            }
            nativeMaterialTransfer.launchEntered = true;
            nativeMaterialTransfer.launchSource = compoundTag;
            return nativeMaterialTransfer;
        }
        catch (Throwable throwable) {
            nativeReceipt.materialRejected = true;
            FoodWeaponSourceLedger.rejectNativeMaterialTransfer(nativeMaterialTransfer);
            throw throwable;
        }
    }

    private static boolean nativeMaterialProvenanceMatches(NativeMaterialPublication nativeMaterialPublication) {
        if (nativeMaterialPublication == null || nativeMaterialPublication.transfer == null) {
            return false;
        }
        NativeMaterialTransfer nativeMaterialTransfer = nativeMaterialPublication.transfer;
        NativeReceipt nativeReceipt = nativeMaterialPublication.receipt;
        NativeMaterialCapture nativeMaterialCapture = nativeMaterialPublication.capture;
        if (nativeMaterialTransfer.rejected || nativeReceipt.materialRejected || nativeReceipt.operation.materialRejected || nativeMaterialCapture.failed || nativeMaterialTransfer.receipt != nativeReceipt || nativeReceipt.materialTransfer != nativeMaterialTransfer || nativeMaterialTransfer.capture != nativeMaterialCapture || nativeMaterialCapture != nativeReceipt.operation.materialCapture || nativeMaterialTransfer.entity != nativeMaterialPublication.entity || nativeMaterialTransfer.capturedReturn != nativeMaterialCapture.originalReturnedSnapshot || !nativeMaterialTransfer.launchEntered || nativeMaterialPublication.originalSnapshot != nativeMaterialTransfer.launchSource || nativeMaterialPublication.originalPutValue != nativeMaterialTransfer.launchCopy || nativeMaterialTransfer.launchCopy == null || nativeMaterialTransfer.launchCopy == nativeMaterialTransfer.launchSource) {
            return false;
        }
        return nativeReceipt.operation.kind == NativeKind.BOW ? nativeMaterialTransfer.pendingReturned && nativeMaterialTransfer.pendingReadEntered && nativeMaterialTransfer.pendingTaken && nativeMaterialTransfer.launchSource == nativeMaterialTransfer.pendingCopy && nativeMaterialTransfer.pendingCopy != nativeMaterialTransfer.capturedReturn : nativeReceipt.operation.kind == NativeKind.CROSSBOW && nativeMaterialTransfer.launchSource == nativeMaterialTransfer.capturedReturn;
    }

    public static synchronized void nativeMaterialCaptureReturned(ServerPlayer serverPlayer, ItemStack itemStack, InteractionHand interactionHand, CompoundTag compoundTag) {
        NativeOperation nativeOperation = NATIVE_OPERATION.get();
        if (nativeOperation == null || nativeOperation.thread != Thread.currentThread() || nativeOperation.player != serverPlayer || nativeOperation.weapon != itemStack || nativeOperation.hand != interactionHand || nativeOperation.kind == NativeKind.TRIDENT) {
            return;
        }
        nativeOperation.materialAttempted = true;
        NativeMaterialCapture nativeMaterialCapture = nativeOperation.materialCapture;
        try {
            if (!FoodWeaponSourceLedger.nativeMaterialCaptureCaller() || !FoodWeaponSourceLedger.nativeMaterialOperationCurrent(nativeOperation) || nativeOperation.materialRejected || nativeMaterialCapture == null || nativeMaterialCapture.failed || nativeMaterialCapture.originalReturnedSnapshot != null || compoundTag == null) {
                nativeOperation.materialRejected = true;
                if (nativeMaterialCapture != null) {
                    nativeMaterialCapture.failed = true;
                }
                return;
            }
            nativeMaterialCapture.originalReturnedSnapshot = compoundTag;
            OwnerNativeMaterialImage ownerNativeMaterialImage = OwnerNativeMaterialImage.freeze(compoundTag);
            CompoundTag compoundTag2 = ownerNativeMaterialImage.image();
            if (!(FoodWeaponSourceLedger.nativeMaterialOperationCurrent(nativeOperation) && !nativeOperation.materialRejected && compoundTag2.m_128403_("Owner") && compoundTag2.m_128403_("WeaponId") && compoundTag2.m_128403_("ServerEpoch") && compoundTag2.m_128403_("OwnerEpoch") && nativeOperation.epoch.equals(compoundTag2.m_128342_("ServerEpoch")) && nativeOperation.ownerEpoch.equals(compoundTag2.m_128342_("OwnerEpoch")) && nativeOperation.hand.name().equals(compoundTag2.m_128461_("OriginalHand")) && nativeOperation.dimension.equals(compoundTag2.m_128461_("Dimension")) && compoundTag2.m_128454_("CapturedAt") == nativeOperation.tick && "BOW".equals(compoundTag2.m_128461_("Family")) && "BOW".equals(compoundTag2.m_128461_("SourceKind")) && !compoundTag2.m_128441_("DamagePurpose") && compoundTag2.m_128461_("ServerSignature").matches("[0-9a-f]{64}"))) {
                nativeMaterialCapture.failed = true;
                nativeOperation.materialRejected = true;
                return;
            }
            nativeMaterialCapture.image = ownerNativeMaterialImage;
        }
        catch (Throwable throwable) {
            nativeOperation.materialRejected = true;
            if (nativeMaterialCapture != null) {
                nativeMaterialCapture.failed = true;
            }
            throw throwable;
        }
    }

    public static synchronized NativeMaterialPublication nativeMaterialPutEntered(Entity entity, CompoundTag compoundTag, String string, CompoundTag compoundTag2, CompoundTag compoundTag3) {
        NativeReceipt nativeReceipt;
        NativeOperation nativeOperation = NATIVE_OPERATION.get();
        NativeReceipt nativeReceipt2 = nativeReceipt = nativeOperation == null ? null : nativeOperation.current;
        if (nativeOperation != null && nativeOperation.thread == Thread.currentThread() && nativeOperation.kind != NativeKind.TRIDENT && nativeReceipt != null && nativeReceipt.entity == entity) {
            nativeReceipt.materialAttempted = true;
            nativeReceipt.materialRejected = true;
        }
        return null;
    }

    public static synchronized NativeMaterialPublication nativeMaterialPutEntered(NativeMaterialTransfer nativeMaterialTransfer, Entity entity, CompoundTag compoundTag, String string, CompoundTag compoundTag2, CompoundTag compoundTag3) {
        if (nativeMaterialTransfer == null || nativeMaterialTransfer.receipt.operation.thread != Thread.currentThread()) {
            return null;
        }
        NativeReceipt nativeReceipt = nativeMaterialTransfer.receipt;
        nativeReceipt.materialAttempted = true;
        NativeMaterialPublication nativeMaterialPublication = null;
        try {
            if (!FoodWeaponSourceLedger.nativeMaterialLaunchCaller() || !FoodWeaponSourceLedger.nativeMaterialTransferCurrent(nativeMaterialTransfer) || !nativeMaterialTransfer.launchEntered || nativeMaterialTransfer.entity != entity || nativeMaterialTransfer.launchSource != compoundTag2 || nativeMaterialTransfer.launchCopy != null || !nativeReceipt.worn || nativeReceipt.added || nativeReceipt.consumed || nativeReceipt.material != null || compoundTag == null || compoundTag.getClass() != CompoundTag.class || !"FTBossWeaponMaterials".equals(string) || compoundTag3 == null || compoundTag3 == compoundTag2 || compoundTag3 == nativeMaterialTransfer.capturedReturn || compoundTag3 == nativeMaterialTransfer.pendingCopy || entity == null || entity.getClass() != Arrow.class && entity.getClass() != SpectralArrow.class || !(entity instanceof OwnerNativeMaterialEntityIdentity)) {
                FoodWeaponSourceLedger.rejectNativeMaterialTransfer(nativeMaterialTransfer);
                return null;
            }
            OwnerNativeMaterialEntityIdentity ownerNativeMaterialEntityIdentity = (OwnerNativeMaterialEntityIdentity)entity;
            nativeMaterialTransfer.launchCopy = compoundTag3;
            nativeMaterialPublication = new NativeMaterialPublication(nativeReceipt, nativeMaterialTransfer.capture, entity, compoundTag, string, compoundTag2, compoundTag3, ownerNativeMaterialEntityIdentity.ftboss$ownerMaterialEntityIdentity());
            nativeMaterialPublication.transfer = nativeMaterialTransfer;
            nativeReceipt.material = nativeMaterialPublication;
            nativeMaterialPublication.image = OwnerNativeMaterialImage.freeze(compoundTag3);
            if (!(nativeMaterialPublication.entityId != null && nativeMaterialPublication.image.same(nativeMaterialTransfer.capture.image) && FoodWeaponSourceLedger.nativeMaterialTransferCurrent(nativeMaterialTransfer) && FoodWeaponSourceLedger.nativeMaterialProvenanceMatches(nativeMaterialPublication))) {
                FoodWeaponSourceLedger.rejectNativeMaterialTransfer(nativeMaterialTransfer);
                return null;
            }
            return nativeMaterialPublication;
        }
        catch (Throwable throwable) {
            nativeMaterialTransfer.observationFailure = throwable;
            if (nativeMaterialPublication != null) {
                nativeMaterialPublication.observationFailure = throwable;
            }
            FoodWeaponSourceLedger.rejectNativeMaterialTransfer(nativeMaterialTransfer);
            throw throwable;
        }
    }

    public static synchronized void nativeMaterialPutReturned(NativeMaterialPublication nativeMaterialPublication, Tag tag) {
        if (nativeMaterialPublication == null || nativeMaterialPublication.receipt.operation.thread != Thread.currentThread()) {
            return;
        }
        try {
            NativeReceipt nativeReceipt = nativeMaterialPublication.receipt;
            if (!FoodWeaponSourceLedger.nativeMaterialLaunchCaller() || nativeMaterialPublication.phase != NativeMaterialPhase.OPEN || !FoodWeaponSourceLedger.nativeMaterialProvenanceMatches(nativeMaterialPublication) || !FoodWeaponSourceLedger.nativeMaterialTransferCurrent(nativeMaterialPublication.transfer) || nativeReceipt.material != nativeMaterialPublication || nativeReceipt.entity != nativeMaterialPublication.entity || !nativeReceipt.worn || nativeReceipt.added || nativeReceipt.consumed || nativeMaterialPublication.image == null || tag != null) {
                nativeMaterialPublication.phase = NativeMaterialPhase.REJECTED;
                FoodWeaponSourceLedger.rejectNativeMaterialTransfer(nativeMaterialPublication.transfer);
                return;
            }
            nativeMaterialPublication.phase = NativeMaterialPhase.RETURNED;
        }
        catch (Throwable throwable) {
            nativeMaterialPublication.observationFailure = throwable;
            nativeMaterialPublication.phase = NativeMaterialPhase.REJECTED;
            FoodWeaponSourceLedger.rejectNativeMaterialTransfer(nativeMaterialPublication.transfer);
            throw throwable;
        }
    }

    public static synchronized void nativeMaterialPutFailed(NativeMaterialPublication nativeMaterialPublication, Throwable throwable) {
        if (nativeMaterialPublication == null || throwable == null || nativeMaterialPublication.receipt.operation.thread != Thread.currentThread()) {
            return;
        }
        nativeMaterialPublication.originalFailure = throwable;
        nativeMaterialPublication.phase = NativeMaterialPhase.REJECTED;
        FoodWeaponSourceLedger.rejectNativeMaterialTransfer(nativeMaterialPublication.transfer);
        if (nativeMaterialPublication.paid != null) {
            nativeMaterialPublication.paid.expectation.closed = true;
        }
    }

    public static synchronized void nativeMaterialObservationFailed(NativeMaterialPublication nativeMaterialPublication, Throwable throwable) {
        if (nativeMaterialPublication == null || throwable == null || nativeMaterialPublication.receipt.operation.thread != Thread.currentThread()) {
            return;
        }
        nativeMaterialPublication.observationFailure = throwable;
        nativeMaterialPublication.phase = NativeMaterialPhase.REJECTED;
        FoodWeaponSourceLedger.rejectNativeMaterialTransfer(nativeMaterialPublication.transfer);
        if (nativeMaterialPublication.paid != null) {
            nativeMaterialPublication.paid.expectation.closed = true;
        }
    }

    private static void finishNativeMaterialPaid(NativeReceipt nativeReceipt, CompoundTag compoundTag, CompoundTag compoundTag2) {
        block11: {
            NativeMaterialPublication nativeMaterialPublication = nativeReceipt.material;
            NativeOperation nativeOperation = nativeReceipt.operation;
            if (nativeOperation.materialRejected || nativeReceipt.materialRejected || !FoodWeaponSourceLedger.nativeMaterialProvenanceMatches(nativeMaterialPublication) || nativeMaterialPublication.phase != NativeMaterialPhase.RETURNED || nativeMaterialPublication.receipt != nativeReceipt || nativeMaterialPublication.entity != nativeReceipt.entity || nativeMaterialPublication.receiver != compoundTag2 || !"FTBossWeaponMaterials".equals(nativeMaterialPublication.key) || nativeMaterialPublication.capture != nativeOperation.materialCapture || nativeMaterialPublication.capture.failed || nativeMaterialPublication.image == null || !FoodWeaponSourceLedger.nativeMaterialOperationCurrent(nativeOperation) || nativeOperation.current == null || !nativeReceipt.added || !nativeReceipt.worn || nativeReceipt.consumed) {
                return;
            }
            nativeMaterialPublication.phase = NativeMaterialPhase.CLAIMED;
            nativeReceipt.consumed = true;
            NativePaidWeapon nativePaidWeapon = null;
            try {
                String string2;
                CompoundTag compoundTag3 = nativeMaterialPublication.image.image();
                if (!nativeReceipt.rules.equals(compoundTag3.m_128461_("FoundationApprovedRules"))) {
                    nativeMaterialPublication.phase = NativeMaterialPhase.REJECTED;
                    return;
                }
                CompoundTag compoundTag4 = OwnerNativeMaterialImage.freeze(nativeReceipt.unsigned).image();
                for (String string2 : compoundTag4.m_128431_()) {
                    compoundTag3.m_128365_(string2, compoundTag4.m_128423_(string2));
                }
                compoundTag3.m_128359_("NativeEntry", nativeOperation.kind.name());
                compoundTag3.m_128362_("NativeReceiptId", nativeReceipt.token);
                compoundTag3.m_128362_("NativeEntityId", nativeMaterialPublication.entityId);
                compoundTag3.m_128362_("AttackId", nativeReceipt.token);
                compoundTag3.m_128359_("OwnerMaterialProduction", nativeMaterialPublication.image.digest());
                compoundTag3.m_128473_("ServerSignature");
                ServerState serverState = SERVERS.get(nativeOperation.server);
                if (serverState == null || !nativeOperation.epoch.equals(serverState.epoch) || nativeOperation.tick < serverState.latestTick || !FoodWeaponSourceLedger.damageAuthorized(compoundTag3)) {
                    nativeMaterialPublication.phase = NativeMaterialPhase.REJECTED;
                    return;
                }
                if (!OwnerRepairGenerationAuthority.stamp(nativeOperation.weapon, compoundTag3, serverState.revocations)) {
                    nativeMaterialPublication.phase = NativeMaterialPhase.REJECTED;
                    return;
                }
                string2 = FoodWeaponSourceLedger.signature(compoundTag3, serverState);
                if (string2.isEmpty()) {
                    nativeMaterialPublication.phase = NativeMaterialPhase.REJECTED;
                    return;
                }
                compoundTag3.m_128359_("ServerSignature", string2);
                if (!OwnerFullRepairGuard.bind(nativeOperation.weapon, compoundTag3.m_128342_("WeaponId"))) {
                    nativeMaterialPublication.phase = NativeMaterialPhase.REJECTED;
                    return;
                }
                OwnerNativeMaterialImage ownerNativeMaterialImage = OwnerNativeMaterialImage.freeze(compoundTag3);
                nativeMaterialPublication.paid = nativePaidWeapon = new NativePaidWeapon(nativeReceipt, compoundTag, nativeMaterialPublication, ownerNativeMaterialImage);
                NativeVanillaImpact.bindOwnerPaidWeapon(nativeReceipt.entity, nativePaidWeapon);
                NativeMaterialPhase nativeMaterialPhase = nativeMaterialPublication.phase = FoodWeaponSourceLedger.nativeMaterialOperationCurrent(nativeOperation) && !nativeMaterialPublication.capture.failed && nativeMaterialPublication.phase == NativeMaterialPhase.CLAIMED && SERVERS.get(nativeOperation.server) == serverState && nativeOperation.epoch.equals(serverState.epoch) && serverState.latestTick <= nativeOperation.tick ? NativeMaterialPhase.PUBLISHED : NativeMaterialPhase.REJECTED;
                if (nativeMaterialPublication.phase == NativeMaterialPhase.PUBLISHED) {
                    serverState.latestTick = nativeOperation.tick;
                } else {
                    nativePaidWeapon.expectation.closed = true;
                }
            }
            catch (Throwable throwable) {
                nativeMaterialPublication.phase = NativeMaterialPhase.REJECTED;
                nativeMaterialPublication.observationFailure = throwable;
                if (nativePaidWeapon == null) break block11;
                nativePaidWeapon.expectation.closed = true;
            }
        }
    }

    private static CompoundTag nativeMaterialPaidSnapshot(NativePaidWeapon nativePaidWeapon, Projectile projectile, ServerPlayer serverPlayer) {
        NativeMaterialPublication nativeMaterialPublication = nativePaidWeapon.material;
        NativeReceipt nativeReceipt = nativePaidWeapon.receipt;
        if (!OwnerRepairGenerationAuthority.matches(nativeReceipt.operation.weapon, nativePaidWeapon.expectation.repairGeneration) || !OwnerFullRepairGuard.permits(nativeReceipt.operation.weapon) || nativeReceipt.operation.materialRejected || nativeReceipt.materialRejected || !FoodWeaponSourceLedger.nativeMaterialProvenanceMatches(nativeMaterialPublication) || nativeMaterialPublication.phase != NativeMaterialPhase.PUBLISHED || nativeMaterialPublication.paid != nativePaidWeapon || nativeReceipt.material != nativeMaterialPublication || nativeMaterialPublication.receipt != nativeReceipt || nativeMaterialPublication.capture.failed || nativeMaterialPublication.entity != projectile || nativeReceipt.entity != projectile || nativePaidWeapon.carrier != projectile || nativeReceipt.operation.player != serverPlayer || nativeReceipt.operation.thread != Thread.currentThread() || nativeReceipt.operation.invalid || !nativeReceipt.consumed || !nativeReceipt.worn || !nativeReceipt.added || nativePaidWeapon.combined == null || nativePaidWeapon.expectation.closed) {
            return new CompoundTag();
        }
        return nativePaidWeapon.combined.image();
    }

    private static void finishNative(NativeOperation nativeOperation, boolean bl) {
        if (!bl || !FoodWeaponSourceLedger.liveNative(nativeOperation) || nativeOperation.current == null || nativeOperation.current.after == null) {
            return;
        }
        CompoundTag compoundTag = nativeOperation.weapon.m_41739_(new CompoundTag());
        if (!FoodWeaponSourceLedger.canonicalInputValid((Tag)compoundTag)) {
            return;
        }
        CompoundTag compoundTag2 = compoundTag.m_6426_();
        CompoundTag compoundTag3 = nativeOperation.current.after.m_6426_();
        if (nativeOperation.kind == NativeKind.CROSSBOW) {
            CompoundTag compoundTag4 = compoundTag.m_128469_("tag");
            if (compoundTag4.m_128471_("Charged") || !compoundTag4.m_128437_("ChargedProjectiles", 10).isEmpty()) {
                return;
            }
            for (CompoundTag compoundTag5 : List.of(compoundTag.m_128469_("tag"), compoundTag3.m_128469_("tag"))) {
                compoundTag5.m_128473_("Charged");
                compoundTag5.m_128473_("ChargedProjectiles");
            }
        }
        if (!compoundTag.equals((Object)compoundTag3)) {
            return;
        }
        for (NativeReceipt nativeReceipt : nativeOperation.receipts) {
            CompoundTag compoundTag6;
            Entity entity;
            CompoundTag compoundTag5;
            if (!nativeReceipt.added || !nativeReceipt.worn || nativeReceipt.consumed || nativeReceipt.entity == null || nativeReceipt.after == null || nativeReceipt.unsigned == null || nativeReceipt.entity.m_9236_() != nativeOperation.player.m_9236_() || !((entity = nativeReceipt.entity) instanceof Projectile) || (compoundTag5 = (Projectile)entity).m_19749_() != nativeOperation.player) continue;
            entity = nativeReceipt.entity.getPersistentData();
            boolean bl2 = entity.m_128441_("FTBossWeaponMaterials");
            if (nativeOperation.materialAttempted || nativeReceipt.materialAttempted || nativeReceipt.material != null) {
                if (!bl2 || nativeOperation.materialRejected || nativeReceipt.materialRejected) continue;
                FoodWeaponSourceLedger.finishNativeMaterialPaid(nativeReceipt, compoundTag2, (CompoundTag)entity);
                continue;
            }
            if (bl2) continue;
            nativeReceipt.consumed = true;
            ItemStack itemStack = ItemStack.m_41712_((CompoundTag)nativeReceipt.after.m_6426_());
            if (!nativeReceipt.after.equals((Object)itemStack.m_41739_(new CompoundTag())) || !nativeReceipt.rules.equals(FoodWeaponSourceLedger.foundationRules(nativeOperation.player, itemStack)) || (compoundTag6 = FoodWeaponSourceLedger.decorateNative(nativeOperation.player, itemStack, nativeReceipt.unsigned, nativeOperation.hand, nativeReceipt)).m_128456_() || !FoodWeaponSourceLedger.valid(compoundTag6, nativeOperation.player)) continue;
            NativePaidWeapon nativePaidWeapon = new NativePaidWeapon(nativeReceipt, compoundTag2);
            nativeReceipt.entity.getPersistentData().m_128365_("FTBossWeaponMaterials", (Tag)compoundTag6);
            NativeVanillaImpact.bindOwnerPaidWeapon(nativeReceipt.entity, nativePaidWeapon);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void nativeRelease(Item item, ItemStack itemStack, Level level, LivingEntity livingEntity, int n) {
        int n2 = NATIVE_DEPTH.get();
        NATIVE_DEPTH.set(n2 + 1);
        NativeOperation nativeOperation = NATIVE_OPERATION.get();
        if (nativeOperation != null) {
            nativeOperation.invalid = true;
        }
        NativeOperation nativeOperation2 = null;
        NATIVE_OPERATION.remove();
        boolean bl = false;
        try {
            if (n2 == 0) {
                nativeOperation2 = FoodWeaponSourceLedger.beginNative(item, itemStack, level, livingEntity, livingEntity.m_7655_());
            }
            if (nativeOperation2 != null) {
                NATIVE_OPERATION.set(nativeOperation2);
            }
            item.m_5551_(itemStack, level, livingEntity, n);
            bl = true;
        }
        catch (Throwable throwable) {
            try {
                FoodWeaponSourceLedger.finishNative(nativeOperation2, bl);
            }
            finally {
                NATIVE_OPERATION.remove();
                if (n2 == 0) {
                    NATIVE_DEPTH.remove();
                } else {
                    NATIVE_DEPTH.set(n2);
                }
            }
            throw throwable;
        }
        try {
            FoodWeaponSourceLedger.finishNative(nativeOperation2, bl);
        }
        finally {
            NATIVE_OPERATION.remove();
            if (n2 == 0) {
                NATIVE_DEPTH.remove();
            } else {
                NATIVE_DEPTH.set(n2);
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static InteractionResultHolder<ItemStack> nativeUse(Item item, ItemStack itemStack, Level level, Player player, InteractionHand interactionHand) {
        InteractionResultHolder interactionResultHolder;
        int n = NATIVE_DEPTH.get();
        NATIVE_DEPTH.set(n + 1);
        NativeOperation nativeOperation = NATIVE_OPERATION.get();
        if (nativeOperation != null) {
            nativeOperation.invalid = true;
        }
        NativeOperation nativeOperation2 = null;
        NATIVE_OPERATION.remove();
        boolean bl = false;
        try {
            if (n == 0) {
                nativeOperation2 = FoodWeaponSourceLedger.beginNative(item, itemStack, level, (LivingEntity)player, interactionHand);
            }
            if (nativeOperation2 != null) {
                NATIVE_OPERATION.set(nativeOperation2);
            }
            InteractionResultHolder interactionResultHolder2 = item.m_7203_(level, player, interactionHand);
            bl = true;
            interactionResultHolder = interactionResultHolder2;
        }
        catch (Throwable throwable) {
            try {
                FoodWeaponSourceLedger.finishNative(nativeOperation2, bl);
            }
            finally {
                NATIVE_OPERATION.remove();
                if (n == 0) {
                    NATIVE_DEPTH.remove();
                } else {
                    NATIVE_DEPTH.set(n);
                }
            }
            throw throwable;
        }
        try {
            FoodWeaponSourceLedger.finishNative(nativeOperation2, bl);
        }
        finally {
            NATIVE_OPERATION.remove();
            if (n == 0) {
                NATIVE_DEPTH.remove();
            } else {
                NATIVE_DEPTH.set(n);
            }
        }
        return interactionResultHolder;
    }

    private static void bindNativeArrow(AbstractArrow abstractArrow, NativeKind nativeKind, ItemStack itemStack) {
        NativeReceipt nativeReceipt;
        NativeOperation nativeOperation = NATIVE_OPERATION.get();
        if (!FoodWeaponSourceLedger.liveNative(nativeOperation) || nativeOperation.kind != nativeKind || nativeOperation.weapon != itemStack || !FoodWeaponSourceLedger.nativeCaller(nativeKind, "factory") || abstractArrow.m_19749_() != nativeOperation.player || abstractArrow.m_9236_() != nativeOperation.player.m_9236_() || abstractArrow.getPersistentData().m_128441_("FTBossWeaponMaterials") || abstractArrow.getClass() != Arrow.class && abstractArrow.getClass() != SpectralArrow.class) {
            return;
        }
        if (nativeKind == NativeKind.BOW && nativeOperation.factoryEntity != abstractArrow) {
            nativeOperation.invalid = true;
            return;
        }
        if (nativeOperation.current != null && !nativeOperation.current.added) {
            nativeOperation.invalid = true;
            return;
        }
        nativeOperation.current = nativeReceipt = new NativeReceipt(nativeOperation, (Entity)abstractArrow);
        nativeOperation.receipts.add(nativeReceipt);
    }

    public static AbstractArrow nativeBowArrow(BowItem bowItem, AbstractArrow abstractArrow, ItemStack itemStack) {
        AbstractArrow abstractArrow2 = bowItem.customArrow(abstractArrow);
        FoodWeaponSourceLedger.bindNativeArrow(abstractArrow2, NativeKind.BOW, itemStack);
        return abstractArrow2;
    }

    private static boolean approvedAmmunition(ItemStack itemStack) {
        Item item = itemStack.m_41720_();
        return (item.getClass() == ArrowItem.class || item.getClass() == SpectralArrowItem.class) && List.of("minecraft:arrow", "minecraft:tipped_arrow", "minecraft:spectral_arrow").contains(FourWeaponFoundationAdapter.registryId(itemStack));
    }

    public static AbstractArrow nativeArrowFactory(ArrowItem arrowItem, Level level, ItemStack itemStack, LivingEntity livingEntity) {
        AbstractArrow abstractArrow = arrowItem.m_6394_(level, itemStack, livingEntity);
        NativeOperation nativeOperation = NATIVE_OPERATION.get();
        if (FoodWeaponSourceLedger.liveNative(nativeOperation) && nativeOperation.kind == NativeKind.BOW && FoodWeaponSourceLedger.nativeCaller(NativeKind.BOW, "ammoFactory") && arrowItem == itemStack.m_41720_() && FoodWeaponSourceLedger.approvedAmmunition(itemStack) && livingEntity == nativeOperation.player && level == nativeOperation.player.m_9236_()) {
            nativeOperation.factoryEntity = abstractArrow;
        }
        return abstractArrow;
    }

    public static AbstractArrow nativeCrossbowArrow(AbstractArrow abstractArrow, ItemStack itemStack, ItemStack itemStack2) {
        if (FoodWeaponSourceLedger.approvedAmmunition(itemStack2)) {
            FoodWeaponSourceLedger.bindNativeArrow(abstractArrow, NativeKind.CROSSBOW, itemStack);
        }
        return abstractArrow;
    }

    public static ThrownTrident nativeTridentFactory(Level level, LivingEntity livingEntity, ItemStack itemStack) {
        NativeOperation nativeOperation = NATIVE_OPERATION.get();
        NativeReceipt nativeReceipt = nativeOperation == null ? null : nativeOperation.current;
        ThrownTrident thrownTrident = new ThrownTrident(level, livingEntity, itemStack);
        TridentCarrierLineage tridentCarrierLineage = null;
        try {
            TridentCarrierLineage tridentCarrierLineage2;
            OwnerTridentCarrierAccess ownerTridentCarrierAccess;
            if (!(nativeOperation != null && nativeReceipt != null && nativeOperation.kind == NativeKind.TRIDENT && nativeOperation.weapon == itemStack && nativeOperation.player == livingEntity && nativeOperation.thread == Thread.currentThread() && NATIVE_OPERATION.get() == nativeOperation && nativeOperation.current == nativeReceipt && FoodWeaponSourceLedger.liveNative(nativeOperation) && FoodWeaponSourceLedger.nativeCaller(NativeKind.TRIDENT, "factory") && nativeReceipt.worn && nativeReceipt.entity == null)) {
                return thrownTrident;
            }
            if (thrownTrident instanceof OwnerTridentCarrierAccess) {
                ownerTridentCarrierAccess = (OwnerTridentCarrierAccess)thrownTrident;
                tridentCarrierLineage2 = ownerTridentCarrierAccess.ftboss$ownerTridentLineage();
            } else {
                tridentCarrierLineage2 = null;
            }
            tridentCarrierLineage = tridentCarrierLineage2;
            ownerTridentCarrierAccess = FoodWeaponTridentCarrierReader.readOriginalItem(thrownTrident);
            if (thrownTrident.m_19749_() == livingEntity && FoodWeaponSourceLedger.tridentCarrierLineage(thrownTrident, (ItemStack)ownerTridentCarrierAccess, tridentCarrierLineage) != null && tridentCarrierLineage.input == itemStack && tridentCarrierLineage.origin == nativeReceipt && FoodWeaponSourceLedger.isTrident((ItemStack)ownerTridentCarrierAccess)) {
                nativeReceipt.entity = thrownTrident;
            }
        }
        catch (Throwable throwable) {
            FoodWeaponSourceLedger.rejectTridentExtra(thrownTrident, tridentCarrierLineage, nativeReceipt, null, null, null, throwable);
        }
        return thrownTrident;
    }

    public static void nativeWear(ItemStack itemStack, int n, LivingEntity livingEntity, Consumer<LivingEntity> consumer) {
        NativeOperation nativeOperation = NATIVE_OPERATION.get();
        NativeReceipt nativeReceipt = null;
        if (FoodWeaponSourceLedger.liveNative(nativeOperation) && itemStack == nativeOperation.weapon && livingEntity == nativeOperation.player && n == 1 && FoodWeaponSourceLedger.nativeCaller(nativeOperation.kind, "wear") && nativeOperation.player.m_21120_(nativeOperation.hand) == itemStack) {
            if (nativeOperation.kind == NativeKind.TRIDENT && nativeOperation.current == null) {
                nativeOperation.current = new NativeReceipt(nativeOperation, null);
                nativeOperation.receipts.add(nativeOperation.current);
            }
            if (nativeOperation.current != null && !nativeOperation.current.worn && !nativeOperation.current.wearing) {
                nativeReceipt = nativeOperation.current;
                nativeReceipt.wearing = true;
            } else {
                nativeOperation.invalid = true;
            }
        }
        boolean bl = false;
        Throwable throwable = null;
        try {
            itemStack.m_41622_(n, livingEntity, consumer);
            bl = true;
        }
        catch (Throwable throwable2) {
            throwable = throwable2;
            throw throwable2;
        }
        finally {
            block25: {
                try {
                    if (nativeReceipt == null) break block25;
                    nativeReceipt.wearing = false;
                    try {
                        if (bl && FoodWeaponSourceLedger.liveNative(nativeOperation) && FoodWeaponSourceLedger.authorizedWear(nativeReceipt, itemStack, n)) {
                            nativeReceipt.after = itemStack.m_41739_(new CompoundTag()).m_6426_();
                            nativeReceipt.unsigned = new CompoundTag();
                            nativeReceipt.unsigned.m_128359_("Family", FoodWeaponFamilyBuild.kind(itemStack).name());
                            nativeReceipt.unsigned.m_128359_("DamagePurpose", NATIVE_PURPOSE);
                            FoodWeaponDamageScaling.freezeFactors(itemStack, nativeReceipt.unsigned, 1.0, 1.0, 1.0);
                            nativeReceipt.worn = true;
                            break block25;
                        }
                        nativeOperation.invalid = true;
                    }
                    catch (IllegalArgumentException | LinkageError throwable3) {
                        nativeOperation.invalid = true;
                    }
                }
                catch (Throwable throwable4) {
                    if (nativeOperation != null) {
                        nativeOperation.invalid = true;
                    }
                    if (throwable == null) {
                        throw throwable4;
                    }
                    if (throwable4 == throwable) break block25;
                    try {
                        throwable.addSuppressed(throwable4);
                    }
                    catch (Throwable throwable5) {}
                }
            }
        }
    }

    public static void nativeAppliedDamage(ItemStack itemStack, int n, int n2, RandomSource randomSource, ServerPlayer serverPlayer) {
        NativeOperation nativeOperation = NATIVE_OPERATION.get();
        NativeReceipt nativeReceipt = nativeOperation == null ? null : nativeOperation.current;
        boolean bl = NATIVE_CALLER.walk(stream -> stream.filter(stackFrame -> stackFrame.getDeclaringClass() != FoodWeaponSourceLedger.class).findFirst().map(stackFrame -> stackFrame.getDeclaringClass() == ItemStack.class && stackFrame.getMethodName().contains("ftboss$v6$applied")).orElse(false));
        if (FoodWeaponSourceLedger.liveNative(nativeOperation) && bl && nativeReceipt != null && nativeReceipt.wearing && itemStack == nativeOperation.weapon && serverPlayer == nativeOperation.player) {
            ++nativeReceipt.writes;
            nativeReceipt.requestedDamage = n;
            if (nativeReceipt.writes > 1) {
                nativeOperation.invalid = true;
            }
        }
        NativeWearToken nativeWearToken = PRIVATE_INGRESS_WEAR.get();
        if (bl && nativeWearToken != null && !nativeWearToken.returned && FoodWeaponSourceLedger.ingressLive(nativeWearToken.ingress) && itemStack == nativeWearToken.ingress.actual && serverPlayer == nativeWearToken.ingress.player) {
            ++nativeWearToken.writes;
            nativeWearToken.requested = n;
            if (nativeWearToken.writes > 1) {
                nativeWearToken.ingress.invalid = true;
            }
        }
        try {
            if (bl) {
                OwnerStatueWearAdmission.damage(itemStack, n, n2);
            } else {
                OwnerStatueWearAdmission.mutationNoticed(itemStack, null, 44);
            }
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        itemStack.m_41721_(n);
    }

    public static boolean nativeAdd(Level level, Entity entity) {
        NativeOperation nativeOperation = NATIVE_OPERATION.get();
        NativeReceipt nativeReceipt = nativeOperation == null ? null : nativeOperation.current;
        boolean bl = FoodWeaponSourceLedger.liveNative(nativeOperation) && FoodWeaponSourceLedger.nativeCaller(nativeOperation.kind, "add") && nativeReceipt != null && nativeReceipt.worn && nativeReceipt.entity == entity && !nativeReceipt.added && !nativeReceipt.consumed && level == nativeOperation.player.m_9236_();
        boolean bl2 = level.m_7967_(entity);
        if (bl && FoodWeaponSourceLedger.liveNative(nativeOperation)) {
            nativeReceipt.added = bl2;
            if (!bl2) {
                nativeOperation.invalid = true;
            }
        } else if (nativeOperation != null) {
            nativeOperation.invalid = true;
        }
        return bl2;
    }

    private static boolean ironPhysical(ItemStack itemStack) {
        if (itemStack == null || itemStack.m_41619_() || itemStack.m_41613_() != 1 || FoodWeaponUseBridge.kind(itemStack) != FoodWeaponFamilyRules.Kind.STAFF) {
            return false;
        }
        for (Class<?> clazz = itemStack.m_41720_().getClass(); clazz != null; clazz = clazz.getSuperclass()) {
            if (!clazz.getName().equals("io.redspace.ironsspellbooks.item.CastingItem")) continue;
            return true;
        }
        return false;
    }

    private static boolean ironBoundary(String string, String string2, String string3, String string4, String string5) {
        return NATIVE_CALLER.walk(stream -> {
            List<StackWalker.StackFrame> list = stream.filter(stackFrame -> stackFrame.getDeclaringClass() != FoodWeaponSourceLedger.class).limit(2L).toList();
            return list.size() == 2 && list.get(0).getClassName().equals(string) && list.get(0).getMethodName().equals(string2) && list.get(0).getDescriptor().equals(string3) && list.get(1).getDeclaringClass() == list.get(0).getDeclaringClass() && list.get(1).getMethodName().equals(string4) && list.get(1).getDescriptor().equals(string5);
        });
    }

    private static boolean ironLive(NativeIronUseToken nativeIronUseToken) {
        if (nativeIronUseToken == null || !OwnerRepairGenerationAuthority.matches(nativeIronUseToken.actual, nativeIronUseToken.repairGeneration) || nativeIronUseToken.invalid || !OwnerFullRepairGuard.permits(nativeIronUseToken.actual) || nativeIronUseToken.thread != Thread.currentThread() || nativeIronUseToken.player.m_213877_() || !nativeIronUseToken.player.m_6084_() || nativeIronUseToken.player.m_5833_() || nativeIronUseToken.player.m_9236_().f_46443_ || !FoodWeaponSourceLedger.server(nativeIronUseToken.player).m_18695_() || nativeIronUseToken.player.m_21120_(nativeIronUseToken.hand) != nativeIronUseToken.actual || !FoodWeaponSourceLedger.ironPhysical(nativeIronUseToken.actual) || FoodWeaponLifecycle.isBroken(nativeIronUseToken.actual)) {
            return false;
        }
        ServerState serverState = SERVERS.get(FoodWeaponSourceLedger.server(nativeIronUseToken.player));
        long l = FoodWeaponSourceLedger.clock(nativeIronUseToken.player);
        return serverState == nativeIronUseToken.state && Objects.equals(FoodWeaponSourceLedger.ownerEpoch(serverState, nativeIronUseToken.player.m_20148_()), nativeIronUseToken.ownerEpoch) && nativeIronUseToken.dimension.equals(nativeIronUseToken.player.m_9236_().m_46472_().m_135782_().toString()) && nativeIronUseToken.tick >= 0L && nativeIronUseToken.tick <= 9223372036854774607L && l >= nativeIronUseToken.tick && l < nativeIronUseToken.tick + 1200L && l >= serverState.latestTick && nativeIronUseToken.rules.equals(FoodWeaponSourceLedger.foundationRules(nativeIronUseToken.player, nativeIronUseToken.actual));
    }

    public static NativeIronUseToken nativeIronUseBegin(ItemStack itemStack, Player player, InteractionHand interactionHand) {
        return FoodWeaponSourceLedger.nativeIronUseBegin(itemStack, player, interactionHand, null);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static NativeIronUseToken nativeIronUseBegin(ItemStack itemStack, Player player, InteractionHand interactionHand, NativeIssuerCustody nativeIssuerCustody) {
        ServerPlayer serverPlayer;
        NativeIssueWindow nativeIssueWindow;
        block18: {
            block17: {
                boolean bl;
                Object object;
                nativeIssueWindow = new NativeIssueWindow((ServerPlayer)(player instanceof ServerPlayer ? (object = (ServerPlayer)player) : null));
                object = "(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/InteractionResultHolder;";
                boolean bl2 = bl = FoodWeaponSourceLedger.ironBoundary("io.redspace.ironsspellbooks.item.CastingItem", "ftboss$v9$ironUseBegin", "(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/InteractionHand;Lorg/spongepowered/asm/mixin/injection/callback/CallbackInfoReturnable;)V", "use", (String)object) || FoodWeaponSourceLedger.ironBoundary("io.redspace.ironsspellbooks.item.CastingItem", "ftboss$v9$ironUseBegin", "(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/InteractionHand;Lorg/spongepowered/asm/mixin/injection/callback/CallbackInfoReturnable;)V", "m_7203_", (String)object);
                if (!bl || !(player instanceof ServerPlayer)) break block17;
                serverPlayer = (ServerPlayer)player;
                if (interactionHand != null && serverPlayer.m_21120_(interactionHand) == itemStack && FoodWeaponSourceLedger.ironPhysical(itemStack) && FoodWeaponLifecycle.weaponId(itemStack) != null && !FoodWeaponLifecycle.isBroken(itemStack) && FoodWeaponSourceLedger.canonicalInputValid((Tag)itemStack.m_41739_(new CompoundTag())) && !FoodWeaponMaterialFingerprint.foundationSnapshot(itemStack).m_128456_() && !FoodWeaponSourceLedger.foundationRules(serverPlayer, itemStack).isEmpty() && !serverPlayer.m_9236_().f_46443_ && serverPlayer.m_6084_() && !serverPlayer.m_213877_() && !serverPlayer.m_5833_() && FoodWeaponSourceLedger.server(serverPlayer).m_18695_()) break block18;
            }
            return null;
        }
        if (!nativeIssueWindow.unchanged()) {
            return null;
        }
        NativeIronUseToken nativeIronUseToken = PRIVATE_IRON_USE.get();
        if (nativeIronUseToken != null) {
            nativeIronUseToken.invalid = true;
            PRIVATE_IRON_USE.remove();
            return null;
        }
        if (PRIVATE_INGRESS.get() != null || NATIVE_OPERATION.get() != null || NATIVE_DEPTH.get() != 0) {
            return null;
        }
        NativeIronUseToken nativeIronUseToken2 = new NativeIronUseToken(serverPlayer, itemStack, interactionHand);
        if (!FoodWeaponSourceLedger.retireOriginalIssuerRoot(nativeIssueWindow)) {
            return null;
        }
        if (!nativeIssueWindow.unchanged()) {
            return null;
        }
        if (nativeIssuerCustody != null && !nativeIssuerCustody.cut.reserve(nativeIronUseToken2)) {
            return null;
        }
        if (nativeIssuerCustody != null) {
            nativeIssuerCustody.use = nativeIronUseToken2;
        }
        boolean bl = false;
        try {
            PRIVATE_IRON_USE.set(nativeIronUseToken2);
            bl = true;
            NativeIronUseToken nativeIronUseToken3 = nativeIronUseToken2;
            return nativeIronUseToken3;
        }
        finally {
            if (!bl) {
                if (nativeIssuerCustody != null) {
                    FoodWeaponSourceLedger.closeNativeIssuerCustody(nativeIssuerCustody);
                } else {
                    nativeIronUseToken2.invalid = true;
                    if (PRIVATE_IRON_USE.get() == nativeIronUseToken2) {
                        PRIVATE_IRON_USE.remove();
                    }
                }
            }
        }
    }

    public static synchronized void nativeIronUseEnd(NativeIronUseToken nativeIronUseToken) {
        if (nativeIronUseToken == null || nativeIronUseToken.thread != Thread.currentThread() || PRIVATE_IRON_USE.get() != nativeIronUseToken) {
            return;
        }
        nativeIronUseToken.invalid = true;
        PRIVATE_IRON_USE.remove();
    }

    public static NativeIronCastToken nativeIronAttemptBegin(Object object, ItemStack itemStack, int n, Player player, Object object2, String string) {
        NativeIronUseToken nativeIronUseToken;
        block3: {
            block2: {
                String string2 = "(Lnet/minecraft/world/item/ItemStack;ILnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/player/Player;Lio/redspace/ironsspellbooks/api/spells/CastSource;ZLjava/lang/String;)Z";
                boolean bl = FoodWeaponSourceLedger.ironBoundary("io.redspace.ironsspellbooks.api.spells.AbstractSpell", "ftboss$v9$ironAttemptBegin", string2.substring(0, string2.indexOf(41)) + "Lorg/spongepowered/asm/mixin/injection/callback/CallbackInfoReturnable;)V", "attemptInitiateCast", string2);
                nativeIronUseToken = PRIVATE_IRON_USE.get();
                if (!bl || !FoodWeaponSourceLedger.ironLive(nativeIronUseToken) || player != nativeIronUseToken.player || itemStack != nativeIronUseToken.actual || nativeIronUseToken.tick != FoodWeaponSourceLedger.clock(nativeIronUseToken.player) || object == null || object2 == null || !object2.getClass().getName().equals("io.redspace.ironsspellbooks.api.spells.CastSource") || !(object2 instanceof Enum)) break block2;
                Enum enum_ = (Enum)object2;
                if (List.of("SPELLBOOK", "SCROLL", "SWORD", "MOB", "COMMAND", "NONE").contains(enum_.name()) && n > 0 && string != null && !string.isBlank() && string.length() <= 256) break block3;
            }
            return null;
        }
        return new NativeIronCastToken(nativeIronUseToken, object, object2, string, n);
    }

    public static synchronized void nativeIronAttemptReturned(NativeIronCastToken nativeIronCastToken) {
        FoodWeaponSourceLedger.cancelNativeIron(nativeIronCastToken);
    }

    public static void nativeIronAttemptReturned(NativeIronCastToken nativeIronCastToken, Object object) {
        boolean bl;
        String string = "(Lnet/minecraft/world/item/ItemStack;ILnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/player/Player;Lio/redspace/ironsspellbooks/api/spells/CastSource;ZLjava/lang/String;)Z";
        if (nativeIronCastToken == null || !FoodWeaponSourceLedger.ironBoundary("io.redspace.ironsspellbooks.api.spells.AbstractSpell", "ftboss$v9$ironAttemptReturn", string.substring(0, string.indexOf(41)) + "Lorg/spongepowered/asm/mixin/injection/callback/CallbackInfoReturnable;)V", "attemptInitiateCast", string)) {
            return;
        }
        try {
            bl = object != null && object.getClass().getName().equals("org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable") && Boolean.TRUE.equals(object.getClass().getMethod("getReturnValue", new Class[0]).invoke(object, new Object[0]));
        }
        catch (ReflectiveOperationException | RuntimeException exception) {
            bl = false;
        }
        if (!bl || !FoodWeaponSourceLedger.ironLive(nativeIronCastToken.use) || nativeIronCastToken.accepted || nativeIronCastToken.invalid || nativeIronCastToken.use.tick != FoodWeaponSourceLedger.clock(nativeIronCastToken.use.player)) {
            FoodWeaponSourceLedger.cancelNativeIron(nativeIronCastToken);
            return;
        }
        nativeIronCastToken.accepted = true;
        if (PRIVATE_IRON_USE.get() == nativeIronCastToken.use) {
            PRIVATE_IRON_USE.remove();
        }
    }

    public static NativeIronPaymentToken nativeIronPostEvent(NativeIronCastToken nativeIronCastToken, Object object, Object object2, Object object3, Object object4, int n, int n2, float f) {
        String string = "(Lnet/minecraft/world/level/Level;ILnet/minecraft/server/level/ServerPlayer;Lio/redspace/ironsspellbooks/api/spells/CastSource;Z)V";
        if (!(nativeIronCastToken != null && FoodWeaponSourceLedger.ironBoundary("io.redspace.ironsspellbooks.api.spells.AbstractSpell", "ftboss$v9$ironPost", "(Lnet/minecraftforge/eventbus/api/IEventBus;Lnet/minecraftforge/eventbus/api/Event;Lnet/minecraft/world/level/Level;ILnet/minecraft/server/level/ServerPlayer;Lio/redspace/ironsspellbooks/api/spells/CastSource;Z)Z", "castSpell", string) && FoodWeaponSourceLedger.ironLive(nativeIronCastToken.use) && nativeIronCastToken.accepted && !nativeIronCastToken.invalid && object == nativeIronCastToken.spell && object2 == nativeIronCastToken.castSource && object3 != null && object3.getClass().getName().equals("io.redspace.ironsspellbooks.api.magic.MagicData") && object4 != null && object4.getClass().getName().equals("io.redspace.ironsspellbooks.api.events.SpellOnCastEvent") && n > 0 && n2 >= 0 && Float.isFinite(f) && !(f < 0.0f))) {
            return null;
        }
        return new NativeIronPaymentToken(nativeIronCastToken, object3, object4, n, n2, f);
    }

    public static void nativeIronManaReturned(NativeIronPaymentToken nativeIronPaymentToken, Object object, float f) {
        String string = "(Lnet/minecraft/world/level/Level;ILnet/minecraft/server/level/ServerPlayer;Lio/redspace/ironsspellbooks/api/spells/CastSource;Z)V";
        if (nativeIronPaymentToken == null || !FoodWeaponSourceLedger.ironBoundary("io.redspace.ironsspellbooks.api.spells.AbstractSpell", "ftboss$v9$ironMana", "(Lio/redspace/ironsspellbooks/api/magic/MagicData;F)V", "castSpell", string)) {
            return;
        }
        if (nativeIronPaymentToken.mana || nativeIronPaymentToken.returned || nativeIronPaymentToken.invalid || nativeIronPaymentToken.magicData != object || !FoodWeaponSourceLedger.ironLive(nativeIronPaymentToken.cast.use) || nativeIronPaymentToken.tick != FoodWeaponSourceLedger.clock(nativeIronPaymentToken.cast.use.player) || !Float.isFinite(f) || f < 0.0f || Float.compare(f, Math.max(0.0f, nativeIronPaymentToken.manaBefore - (float)nativeIronPaymentToken.manaCost)) != 0) {
            nativeIronPaymentToken.invalid = true;
            return;
        }
        nativeIronPaymentToken.mana = true;
        nativeIronPaymentToken.manaAfter = f;
    }

    public static void nativeIronCastCallbackReturned(NativeIronPaymentToken nativeIronPaymentToken, Object object, Object object2, Object object3, int n) {
        String string = "(Lnet/minecraft/world/level/Level;ILnet/minecraft/server/level/ServerPlayer;Lio/redspace/ironsspellbooks/api/spells/CastSource;Z)V";
        if (nativeIronPaymentToken == null || !FoodWeaponSourceLedger.ironBoundary("io.redspace.ironsspellbooks.api.spells.AbstractSpell", "ftboss$v9$ironCast", "(Lio/redspace/ironsspellbooks/api/spells/AbstractSpell;Lnet/minecraft/world/level/Level;ILnet/minecraft/world/entity/LivingEntity;Lio/redspace/ironsspellbooks/api/spells/CastSource;Lio/redspace/ironsspellbooks/api/magic/MagicData;)V", "castSpell", string)) {
            return;
        }
        if (!nativeIronPaymentToken.mana || nativeIronPaymentToken.callback || nativeIronPaymentToken.returned || nativeIronPaymentToken.invalid || nativeIronPaymentToken.cast.spell != object || nativeIronPaymentToken.magicData != object2 || nativeIronPaymentToken.cast.castSource != object3 || nativeIronPaymentToken.level != n || !FoodWeaponSourceLedger.ironLive(nativeIronPaymentToken.cast.use) || nativeIronPaymentToken.tick != FoodWeaponSourceLedger.clock(nativeIronPaymentToken.cast.use.player)) {
            nativeIronPaymentToken.invalid = true;
            return;
        }
        nativeIronPaymentToken.callback = true;
    }

    public static NativeIronReceipt nativeIronCastReturned(NativeIronPaymentToken nativeIronPaymentToken) {
        String string = "(Lnet/minecraft/world/level/Level;ILnet/minecraft/server/level/ServerPlayer;Lio/redspace/ironsspellbooks/api/spells/CastSource;Z)V";
        if (nativeIronPaymentToken == null || !FoodWeaponSourceLedger.ironBoundary("io.redspace.ironsspellbooks.api.spells.AbstractSpell", "ftboss$v9$ironCastReturn", string.substring(0, string.indexOf(41)) + "Lorg/spongepowered/asm/mixin/injection/callback/CallbackInfo;)V", "castSpell", string)) {
            return null;
        }
        if (!nativeIronPaymentToken.mana || !nativeIronPaymentToken.callback || nativeIronPaymentToken.returned || nativeIronPaymentToken.invalid || nativeIronPaymentToken.cast.invalid || !FoodWeaponSourceLedger.ironLive(nativeIronPaymentToken.cast.use) || nativeIronPaymentToken.tick != FoodWeaponSourceLedger.clock(nativeIronPaymentToken.cast.use.player)) {
            nativeIronPaymentToken.invalid = true;
            return null;
        }
        CompoundTag compoundTag = new CompoundTag();
        compoundTag.m_128405_("NativeIronIngressVersion", 1);
        compoundTag.m_128362_("AttackId", nativeIronPaymentToken.cast.action);
        compoundTag.m_128362_("Owner", nativeIronPaymentToken.cast.use.player.m_20148_());
        compoundTag.m_128362_("WeaponId", FoodWeaponLifecycle.weaponId(nativeIronPaymentToken.cast.use.actual));
        compoundTag.m_128362_("ServerEpoch", nativeIronPaymentToken.cast.use.state.epoch);
        compoundTag.m_128362_("OwnerEpoch", nativeIronPaymentToken.cast.use.ownerEpoch);
        compoundTag.m_128359_("OriginalHand", nativeIronPaymentToken.cast.use.hand.name());
        compoundTag.m_128359_("Dimension", nativeIronPaymentToken.cast.use.dimension);
        compoundTag.m_128359_("FoundationApprovedRules", nativeIronPaymentToken.cast.use.rules);
        compoundTag.m_128359_("CastSource", nativeIronPaymentToken.cast.source);
        compoundTag.m_128359_("Slot", nativeIronPaymentToken.cast.slot);
        compoundTag.m_128359_("SpellClass", nativeIronPaymentToken.cast.spell.getClass().getName());
        compoundTag.m_128405_("SelectedLevel", nativeIronPaymentToken.cast.selectedLevel);
        compoundTag.m_128405_("EffectiveLevel", nativeIronPaymentToken.level);
        compoundTag.m_128405_("EffectiveManaCost", nativeIronPaymentToken.manaCost);
        compoundTag.m_128356_("CapturedAt", nativeIronPaymentToken.tick);
        compoundTag.m_128365_("Weapon", (Tag)nativeIronPaymentToken.cast.use.actual.m_41739_(new CompoundTag()));
        String string2 = FoodWeaponSourceLedger.signature(compoundTag, nativeIronPaymentToken.cast.use.state);
        if (string2.isEmpty()) {
            nativeIronPaymentToken.invalid = true;
            return null;
        }
        nativeIronPaymentToken.returned = true;
        return new NativeIronReceipt(nativeIronPaymentToken, compoundTag, string2);
    }

    public static boolean matchesNativeIron(NativeIronReceipt nativeIronReceipt, Object object, Object object2, Object object3, String string, ServerPlayer serverPlayer) {
        if (nativeIronReceipt == null || nativeIronReceipt.payment.invalid || !nativeIronReceipt.payment.returned || nativeIronReceipt.payment.cast.invalid || serverPlayer != nativeIronReceipt.payment.cast.use.player || !FoodWeaponSourceLedger.ironLive(nativeIronReceipt.payment.cast.use) || nativeIronReceipt.payment.cast.spell != object || nativeIronReceipt.payment.magicData != object2 || nativeIronReceipt.payment.cast.castSource != object3 || !nativeIronReceipt.payment.cast.slot.equals(string) || !nativeIronReceipt.image.m_128342_("WeaponId").equals(FoodWeaponLifecycle.weaponId(nativeIronReceipt.payment.cast.use.actual))) {
            return false;
        }
        String string2 = FoodWeaponSourceLedger.signature(nativeIronReceipt.image, nativeIronReceipt.payment.cast.use.state);
        return !string2.isEmpty() && MessageDigest.isEqual(nativeIronReceipt.proof.getBytes(StandardCharsets.US_ASCII), string2.getBytes(StandardCharsets.US_ASCII));
    }

    public static synchronized void cancelNativeIron(NativeIronCastToken nativeIronCastToken) {
        NativeIronCallbackToken nativeIronCallbackToken;
        if (nativeIronCastToken == null) {
            return;
        }
        nativeIronCastToken.invalid = true;
        nativeIronCastToken.use.invalid = true;
        if (PRIVATE_IRON_USE.get() == nativeIronCastToken.use) {
            PRIVATE_IRON_USE.remove();
        }
        if ((nativeIronCallbackToken = PRIVATE_IRON_CALLBACK.get()) != null && nativeIronCallbackToken.payment.cast == nativeIronCastToken) {
            nativeIronCallbackToken.closed = true;
            PRIVATE_IRON_CALLBACK.remove();
            NativeHurtScope nativeHurtScope = PRIVATE_NATIVE_HURT.get();
            if (nativeHurtScope != null && nativeHurtScope.ironCallback == nativeIronCallbackToken) {
                FoodWeaponSourceLedger.closeNativeHurtScope(nativeHurtScope);
            }
        }
    }

    public static NativeCapabilityToken nativeIngressRushReturned(Object object) {
        return FoodWeaponSourceLedger.nativeIngressRushReturned(object, null);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static NativeCapabilityToken nativeIngressRushReturned(Object object, NativeIssuerCustody nativeIssuerCustody) {
        NativeIssueWindow nativeIssueWindow = new NativeIssueWindow(null);
        NativeIngressToken nativeIngressToken = PRIVATE_INGRESS.get();
        String string = "com.github.L_Ender.cataclysm.capabilities.RenderRushCapability$RenderRushCapabilityImp";
        if (!FoodWeaponSourceLedger.ironBoundary(string, "ftboss$v9$rushReturn", "(ZLorg/spongepowered/asm/mixin/injection/callback/CallbackInfo;)V", "setRush", "(Z)V") || !FoodWeaponSourceLedger.ingressLive(nativeIngressToken) || nativeIngressToken.kind != NativeIngressKind.SOUL || nativeIngressToken.returned || nativeIngressToken.tick != FoodWeaponSourceLedger.clock(nativeIngressToken.player) || object == null || !object.getClass().getName().equals(string) || nativeIngressToken.capability != null) {
            return null;
        }
        NativeCapabilityToken nativeCapabilityToken = new NativeCapabilityToken(nativeIngressToken, object);
        if (!nativeIssueWindow.unchanged()) {
            return null;
        }
        if (nativeIssuerCustody != null && !nativeIssuerCustody.cut.reserve(nativeCapabilityToken)) {
            return null;
        }
        if (nativeIssuerCustody != null) {
            nativeIssuerCustody.capability = nativeCapabilityToken;
        }
        boolean bl = false;
        try {
            nativeIngressToken.capability = nativeCapabilityToken;
            bl = true;
            NativeCapabilityToken nativeCapabilityToken2 = nativeCapabilityToken;
            return nativeCapabilityToken2;
        }
        finally {
            if (!bl) {
                if (nativeIssuerCustody != null) {
                    FoodWeaponSourceLedger.closeNativeIssuerCustody(nativeIssuerCustody);
                } else {
                    nativeCapabilityToken.canceled = true;
                }
            }
        }
    }

    public static boolean matchesNativeCapability(NativeIngressReceipt nativeIngressReceipt, NativeCapabilityToken nativeCapabilityToken, Object object, LivingEntity livingEntity) {
        return nativeCapabilityToken != null && nativeIngressReceipt != null && nativeCapabilityToken.ingress == nativeIngressReceipt.ingress && nativeCapabilityToken.capability == object && !nativeCapabilityToken.canceled && livingEntity == nativeCapabilityToken.ingress.player && FoodWeaponSourceLedger.matchesNativeIngress(nativeIngressReceipt, nativeCapabilityToken.ingress.player);
    }

    public static synchronized void cancelNativeCapability(NativeCapabilityToken nativeCapabilityToken) {
        if (nativeCapabilityToken == null) {
            return;
        }
        nativeCapabilityToken.canceled = true;
        FoodWeaponSourceLedger.cancelNativeIngress(nativeCapabilityToken.ingress);
    }

    private static NativeIngressKind ingressKind(ItemStack itemStack) {
        if (itemStack == null || itemStack.m_41619_() || itemStack.m_41613_() != 1) {
            return null;
        }
        String string = itemStack.m_41720_().getClass().getName();
        String string2 = FourWeaponFoundationAdapter.registryId(itemStack);
        for (NativeIngressKind nativeIngressKind : NativeIngressKind.values()) {
            if (!nativeIngressKind.itemClass.equals(string) || !nativeIngressKind.registryId.equals(string2)) continue;
            return nativeIngressKind;
        }
        return null;
    }

    private static boolean ingressEntry(NativeIngressKind nativeIngressKind, String string, String string2) {
        return switch (nativeIngressKind) {
            default -> throw new IncompatibleClassChangeError();
            case NativeIngressKind.ENDER, NativeIngressKind.MEAT -> {
                if ((string.equals("onUseTick") || string.equals("m_5929_")) && string2.equals("(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;I)V")) {
                    yield true;
                }
                yield false;
            }
            case NativeIngressKind.SOUL, NativeIngressKind.CORAL_SPEAR, NativeIngressKind.CORAL_BARDICHE -> {
                if ((string.equals("releaseUsing") || string.equals("m_5551_")) && string2.equals("(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/LivingEntity;I)V")) {
                    yield true;
                }
                yield false;
            }
            case NativeIngressKind.TIDAL -> {
                if (string.equals("onLeftClick") && string2.equals("(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/LivingEntity;)Z")) {
                    yield true;
                }
                yield false;
            }
            case NativeIngressKind.ANCIENT -> string.equals("onLeftClick") && string2.equals("(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/LivingEntity;)Z") || (string.equals("hurtEnemy") || string.equals("m_7579_")) && string2.equals("(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/entity/LivingEntity;)Z");
        };
    }

    private static boolean ingressBoundary(NativeIngressKind nativeIngressKind, String string, String string2, String string3) {
        return NATIVE_CALLER.walk(stream -> {
            List<StackWalker.StackFrame> list = stream.filter(stackFrame -> stackFrame.getDeclaringClass() != FoodWeaponSourceLedger.class).limit(2L).toList();
            if (list.size() != 2) {
                return false;
            }
            StackWalker.StackFrame stackFrame2 = list.get(0);
            StackWalker.StackFrame stackFrame3 = list.get(1);
            String string4 = string3.endsWith("Z") ? "Lorg/spongepowered/asm/mixin/injection/callback/CallbackInfoReturnable;" : "Lorg/spongepowered/asm/mixin/injection/callback/CallbackInfo;";
            String string5 = string3.substring(0, string3.indexOf(41)) + string4 + ")V";
            return stackFrame2.getClassName().equals(nativeIngressKind.itemClass) && stackFrame2.getMethodName().equals("ftboss$v9$native" + string) && stackFrame2.getDescriptor().equals(string5) && stackFrame3.getDeclaringClass() == stackFrame2.getDeclaringClass() && stackFrame3.getMethodName().equals(string2) && stackFrame3.getDescriptor().equals(string3) && FoodWeaponSourceLedger.ingressEntry(nativeIngressKind, string2, string3);
        });
    }

    private static boolean ingressLive(NativeIngressToken nativeIngressToken) {
        if (nativeIngressToken == null || !OwnerRepairGenerationAuthority.matches(nativeIngressToken.actual, nativeIngressToken.repairGeneration) || nativeIngressToken.invalid || !OwnerFullRepairGuard.permits(nativeIngressToken.actual) || nativeIngressToken.thread != Thread.currentThread() || nativeIngressToken.player == null || nativeIngressToken.player.m_213877_() || !nativeIngressToken.player.m_6084_() || nativeIngressToken.player.m_5833_() || nativeIngressToken.player.m_9236_().f_46443_ || !FoodWeaponSourceLedger.server(nativeIngressToken.player).m_18695_()) {
            return false;
        }
        ServerState serverState = SERVERS.get(FoodWeaponSourceLedger.server(nativeIngressToken.player));
        long l = FoodWeaponSourceLedger.clock(nativeIngressToken.player);
        return serverState == nativeIngressToken.state && Objects.equals(FoodWeaponSourceLedger.ownerEpoch(serverState, nativeIngressToken.player.m_20148_()), nativeIngressToken.ownerEpoch) && nativeIngressToken.dimension.equals(nativeIngressToken.player.m_9236_().m_46472_().m_135782_().toString()) && nativeIngressToken.tick >= 0L && nativeIngressToken.tick <= 9223372036854774607L && l >= nativeIngressToken.tick && l < nativeIngressToken.tick + 1200L && l >= serverState.latestTick;
    }

    public static NativeIngressToken nativeIngressBegin(ItemStack itemStack, LivingEntity livingEntity, InteractionHand interactionHand, String string, String string2) {
        return FoodWeaponSourceLedger.nativeIngressBegin(itemStack, livingEntity, interactionHand, string, string2, null);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static NativeIngressToken nativeIngressBegin(ItemStack itemStack, LivingEntity livingEntity, InteractionHand interactionHand, String string, String string2, NativeIssuerCustody nativeIssuerCustody) {
        ServerPlayer serverPlayer;
        NativeIssueWindow nativeIssueWindow;
        Object object;
        block18: {
            block17: {
                nativeIssueWindow = new NativeIssueWindow((ServerPlayer)(livingEntity instanceof ServerPlayer ? (object = (ServerPlayer)livingEntity) : null));
                object = FoodWeaponSourceLedger.ingressKind(itemStack);
                if (object == null || !(livingEntity instanceof ServerPlayer)) break block17;
                serverPlayer = (ServerPlayer)livingEntity;
                if (interactionHand != null && FoodWeaponSourceLedger.ingressBoundary((NativeIngressKind)((Object)object), "Begin", string, string2) && serverPlayer.m_21120_(interactionHand) == itemStack && serverPlayer.m_6084_() && !serverPlayer.m_213877_() && !serverPlayer.m_5833_() && !serverPlayer.m_9236_().f_46443_ && FoodWeaponSourceLedger.server(serverPlayer).m_18695_() && !FoodWeaponLifecycle.isBroken(itemStack) && FoodWeaponLifecycle.weaponId(itemStack) != null && FoodWeaponSourceLedger.canonicalInputValid((Tag)itemStack.m_41739_(new CompoundTag())) && !FoodWeaponMaterialFingerprint.foundationSnapshot(itemStack).m_128456_() && !FoodWeaponSourceLedger.foundationRules(serverPlayer, itemStack).isEmpty()) break block18;
            }
            return null;
        }
        if (!nativeIssueWindow.unchanged()) {
            return null;
        }
        NativeIngressToken nativeIngressToken = PRIVATE_INGRESS.get();
        if (nativeIngressToken != null) {
            nativeIngressToken.invalid = true;
            PRIVATE_INGRESS.remove();
            PRIVATE_INGRESS_WEAR.remove();
            return null;
        }
        if (NATIVE_DEPTH.get() != 0 || NATIVE_OPERATION.get() != null) {
            return null;
        }
        NativeIngressToken nativeIngressToken2 = new NativeIngressToken((NativeIngressKind)((Object)object), serverPlayer, itemStack, interactionHand, string, string2);
        if (!FoodWeaponSourceLedger.retireOriginalIssuerRoot(nativeIssueWindow)) {
            return null;
        }
        if (!nativeIssueWindow.unchanged()) {
            return null;
        }
        if (nativeIssuerCustody != null && !nativeIssuerCustody.cut.reserve(nativeIngressToken2)) {
            return null;
        }
        if (nativeIssuerCustody != null) {
            nativeIssuerCustody.ingress = nativeIngressToken2;
        }
        boolean bl = false;
        try {
            PRIVATE_INGRESS.set(nativeIngressToken2);
            bl = true;
            NativeIngressToken nativeIngressToken3 = nativeIngressToken2;
            return nativeIngressToken3;
        }
        finally {
            if (!bl) {
                if (nativeIssuerCustody != null) {
                    FoodWeaponSourceLedger.closeNativeIssuerCustody(nativeIssuerCustody);
                } else {
                    nativeIngressToken2.invalid = true;
                    nativeIngressToken2.expectation.closed = true;
                    nativeIngressToken2.expectation.cursor.close();
                    if (PRIVATE_INGRESS.get() == nativeIngressToken2) {
                        PRIVATE_INGRESS.remove();
                    }
                }
            }
        }
    }

    private static boolean ingressWearBoundary(String string) {
        return NATIVE_CALLER.walk(stream -> {
            List<StackWalker.StackFrame> list = stream.filter(stackFrame -> stackFrame.getDeclaringClass() != FoodWeaponSourceLedger.class).limit(2L).toList();
            if (list.size() != 2) {
                return false;
            }
            StackWalker.StackFrame stackFrame2 = list.get(0);
            StackWalker.StackFrame stackFrame3 = list.get(1);
            return stackFrame2.getDeclaringClass() == ItemStack.class && stackFrame2.getMethodName().equals("ftboss$v9$nativeWear" + string) && stackFrame2.getDescriptor().equals("(ILnet/minecraft/world/entity/LivingEntity;Ljava/util/function/Consumer;Lorg/spongepowered/asm/mixin/injection/callback/CallbackInfo;)V") && stackFrame3.getDeclaringClass() == ItemStack.class && (stackFrame3.getMethodName().equals("hurtAndBreak") || stackFrame3.getMethodName().equals("m_41622_")) && stackFrame3.getDescriptor().equals("(ILnet/minecraft/world/entity/LivingEntity;Ljava/util/function/Consumer;)V");
        });
    }

    public static NativeWearToken nativeIngressWearBegin(ItemStack itemStack, int n, LivingEntity livingEntity) {
        return FoodWeaponSourceLedger.nativeIngressWearBegin(itemStack, n, livingEntity, null);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static NativeWearToken nativeIngressWearBegin(ItemStack itemStack, int n, LivingEntity livingEntity, NativeIssuerCustody nativeIssuerCustody) {
        Object object;
        NativeIssueWindow nativeIssueWindow = new NativeIssueWindow((ServerPlayer)(livingEntity instanceof ServerPlayer ? (object = (ServerPlayer)livingEntity) : null));
        object = PRIVATE_INGRESS.get();
        if (!FoodWeaponSourceLedger.ingressWearBoundary("Begin") || !FoodWeaponSourceLedger.ingressLive((NativeIngressToken)object) || ((NativeIngressToken)object).returned || ((NativeIngressToken)object).tick != FoodWeaponSourceLedger.clock(((NativeIngressToken)object).player) || itemStack != ((NativeIngressToken)object).actual || livingEntity != ((NativeIngressToken)object).player || n != 1 || ((NativeIngressToken)object).kind.wear == 0 || ((NativeIngressToken)object).player.m_21120_(((NativeIngressToken)object).hand) != itemStack || PRIVATE_INGRESS_WEAR.get() != null) {
            if (object != null) {
                ((NativeIngressToken)object).invalid = true;
            }
            return null;
        }
        NativeWearToken nativeWearToken = new NativeWearToken((NativeIngressToken)object);
        if (!nativeIssueWindow.unchanged()) {
            return null;
        }
        if (nativeIssuerCustody != null && !nativeIssuerCustody.cut.reserve(nativeWearToken)) {
            return null;
        }
        if (nativeIssuerCustody != null) {
            nativeIssuerCustody.wear = nativeWearToken;
        }
        boolean bl = false;
        try {
            PRIVATE_INGRESS_WEAR.set(nativeWearToken);
            bl = true;
            NativeWearToken nativeWearToken2 = nativeWearToken;
            return nativeWearToken2;
        }
        finally {
            if (!bl) {
                if (nativeIssuerCustody != null) {
                    FoodWeaponSourceLedger.closeNativeIssuerCustody(nativeIssuerCustody);
                } else {
                    nativeWearToken.ingress.invalid = true;
                    if (PRIVATE_INGRESS_WEAR.get() == nativeWearToken) {
                        PRIVATE_INGRESS_WEAR.remove();
                    }
                }
            }
        }
    }

    public static void nativeIngressWearReturned(NativeWearToken nativeWearToken, ItemStack itemStack, int n, LivingEntity livingEntity) {
        if (!FoodWeaponSourceLedger.ingressWearBoundary("Return")) {
            return;
        }
        NativeWearToken nativeWearToken2 = PRIVATE_INGRESS_WEAR.get();
        if (nativeWearToken == null || nativeWearToken2 != nativeWearToken || nativeWearToken.returned || itemStack != nativeWearToken.ingress.actual || livingEntity != nativeWearToken.ingress.player || n != 1 || !FoodWeaponSourceLedger.ingressLive(nativeWearToken.ingress) || nativeWearToken.ingress.tick != FoodWeaponSourceLedger.clock(nativeWearToken.ingress.player)) {
            if (nativeWearToken2 != null) {
                nativeWearToken2.ingress.invalid = true;
            }
            PRIVATE_INGRESS_WEAR.remove();
            return;
        }
        nativeWearToken.returned = true;
        if (FoodWeaponSourceLedger.authorizedWear(nativeWearToken.ingress.player, nativeWearToken.before, nativeWearToken.rules, itemStack, n, nativeWearToken.writes, nativeWearToken.requested)) {
            ++nativeWearToken.ingress.wears;
        } else {
            nativeWearToken.ingress.invalid = true;
        }
        PRIVATE_INGRESS_WEAR.remove();
    }

    private static boolean ingressAddedBoundary() {
        return NATIVE_CALLER.walk(stream -> {
            List<StackWalker.StackFrame> list = stream.filter(stackFrame -> stackFrame.getDeclaringClass() != FoodWeaponSourceLedger.class).limit(2L).toList();
            if (list.size() != 2) {
                return false;
            }
            StackWalker.StackFrame stackFrame2 = list.get(0);
            StackWalker.StackFrame stackFrame3 = list.get(1);
            return !(stackFrame2.getDeclaringClass() != ServerLevel.class || !stackFrame2.getMethodName().equals("nativeAdded") && !stackFrame2.getMethodName().equals("nativeAddedSrg") || !stackFrame2.getDescriptor().equals("(Lnet/minecraft/world/entity/Entity;Lorg/spongepowered/asm/mixin/injection/callback/CallbackInfoReturnable;)V") || stackFrame3.getDeclaringClass() != stackFrame2.getDeclaringClass() || !stackFrame3.getMethodName().equals("addFreshEntity") && !stackFrame3.getMethodName().equals("m_7967_") || !stackFrame3.getDescriptor().equals("(Lnet/minecraft/world/entity/Entity;)Z"));
        });
    }

    private static boolean ingressCarrier(NativeIngressKind nativeIngressKind, Entity entity) {
        if (entity == null) {
            return false;
        }
        String string = entity.getClass().getName();
        return switch (nativeIngressKind) {
            default -> throw new IncompatibleClassChangeError();
            case NativeIngressKind.ENDER -> {
                if (string.equals("net.gqstavo.armamentarium.entity.MiteArrowEntity") || string.equals("net.gqstavo.armamentarium.entity.MitePelletEntity")) {
                    yield true;
                }
                yield false;
            }
            case NativeIngressKind.SOUL -> string.equals("com.github.L_Ender.cataclysm.entity.projectile.Phantom_Halberd_Entity");
            case NativeIngressKind.TIDAL -> string.equals("com.github.L_Ender.cataclysm.entity.projectile.Tidal_Tentacle_Entity");
            case NativeIngressKind.ANCIENT -> string.equals("com.github.L_Ender.cataclysm.entity.projectile.Sandstorm_Projectile");
            case NativeIngressKind.CORAL_SPEAR -> string.equals("com.github.L_Ender.cataclysm.entity.projectile.ThrownCoral_Spear_Entity");
            case NativeIngressKind.CORAL_BARDICHE -> string.equals("com.github.L_Ender.cataclysm.entity.projectile.ThrownCoral_Bardiche_Entity");
            case NativeIngressKind.MEAT -> false;
        };
    }

    private static boolean ingressSpawnOrigin(NativeIngressToken nativeIngressToken, Entity entity) {
        if (nativeIngressToken == null || entity == null) {
            return false;
        }
        return NATIVE_CALLER.walk(stream -> {
            List<StackWalker.StackFrame> list = stream.filter(stackFrame -> stackFrame.getDeclaringClass() != FoodWeaponSourceLedger.class).limit(6L).toList();
            if (list.size() < 3) {
                return false;
            }
            StackWalker.StackFrame stackFrame2 = list.get(2);
            if (stackFrame2.getDeclaringClass().getClassLoader() != nativeIngressToken.actual.m_41720_().getClass().getClassLoader()) {
                return false;
            }
            int n = 2;
            boolean bl = switch (nativeIngressToken.kind) {
                default -> throw new IncompatibleClassChangeError();
                case NativeIngressKind.ENDER -> {
                    n = 3;
                    String var7_6 = entity.getClass().getName();
                    String var8_7 = "(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/util/RandomSource;FDI)L" + var7_6.replace('.', '/') + ";";
                    if (stackFrame2.getDeclaringClass() == entity.getClass() && stackFrame2.getMethodName().equals("shoot") && stackFrame2.getDescriptor().equals(var8_7)) {
                        yield true;
                    }
                    yield false;
                }
                case NativeIngressKind.SOUL -> {
                    n = 4;
                    if (stackFrame2.getClassName().equals(nativeIngressToken.kind.itemClass) && stackFrame2.getMethodName().equals("spawnHalberd") && stackFrame2.getDescriptor().equals("(DDDDFILnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/LivingEntity;)V") && list.size() > 3 && list.get(3).getDeclaringClass() == stackFrame2.getDeclaringClass() && list.get(3).getMethodName().equals("StrikeWindmillHalberd") && list.get(3).getDescriptor().equals("(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/LivingEntity;IIDDDI)V")) {
                        yield true;
                    }
                    yield false;
                }
                case NativeIngressKind.TIDAL -> {
                    n = 3;
                    if (stackFrame2.getClassName().equals(nativeIngressToken.kind.itemClass) && stackFrame2.getMethodName().equals("launchTendonsAt") && stackFrame2.getDescriptor().equals("(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/entity/Entity;)Z")) {
                        yield true;
                    }
                    yield false;
                }
                case NativeIngressKind.ANCIENT -> {
                    n = 3;
                    if (stackFrame2.getClassName().equals(nativeIngressToken.kind.itemClass) && stackFrame2.getMethodName().equals("launchTornado") && stackFrame2.getDescriptor().equals("(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/LivingEntity;)Z")) {
                        yield true;
                    }
                    yield false;
                }
                case NativeIngressKind.CORAL_SPEAR, NativeIngressKind.CORAL_BARDICHE -> true;
                case NativeIngressKind.MEAT -> false;
            };
            return bl && list.size() > n && list.get(n).getClassName().equals(nativeIngressToken.kind.itemClass) && list.get(n).getMethodName().equals(nativeIngressToken.entry) && list.get(n).getDescriptor().equals(nativeIngressToken.descriptor);
        });
    }

    public static NativeCarrierToken nativeIngressAdded(Entity entity) {
        return FoodWeaponSourceLedger.nativeIngressAdded(entity, null);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static NativeCarrierToken nativeIngressAdded(Entity entity, NativeIssuerCustody nativeIssuerCustody) {
        boolean bl;
        NativeIssueWindow nativeIssueWindow = new NativeIssueWindow(null);
        NativeIngressToken nativeIngressToken = PRIVATE_INGRESS.get();
        if (!FoodWeaponSourceLedger.ingressAddedBoundary() || !FoodWeaponSourceLedger.ingressLive(nativeIngressToken) || nativeIngressToken.returned || nativeIngressToken.tick != FoodWeaponSourceLedger.clock(nativeIngressToken.player) || !FoodWeaponSourceLedger.ingressCarrier(nativeIngressToken.kind, entity) || !FoodWeaponSourceLedger.ingressSpawnOrigin(nativeIngressToken, entity) || entity.m_9236_() != nativeIngressToken.player.m_9236_() || entity.m_213877_() || nativeIngressToken.player.m_284548_().m_8791_(entity.m_20148_()) != entity) {
            return null;
        }
        NativeCarrierToken nativeCarrierToken2 = nativeIngressToken.carriers.stream().filter(nativeCarrierToken -> nativeCarrierToken.carrier == entity).findFirst().orElse(null);
        if (nativeCarrierToken2 != null && nativeCarrierToken2.added) {
            return null;
        }
        boolean bl2 = bl = nativeCarrierToken2 == null;
        if (bl) {
            nativeCarrierToken2 = new NativeCarrierToken(nativeIngressToken, entity);
        }
        if (!nativeIssueWindow.unchanged()) {
            return null;
        }
        if (nativeIssuerCustody != null && !nativeIssuerCustody.cut.reserve(nativeCarrierToken2)) {
            return null;
        }
        if (nativeIssuerCustody != null) {
            nativeIssuerCustody.carrier = nativeCarrierToken2;
        }
        boolean bl3 = false;
        try {
            if (bl) {
                nativeIngressToken.carriers.add(nativeCarrierToken2);
            }
            nativeCarrierToken2.added = true;
            bl3 = true;
            NativeCarrierToken nativeCarrierToken3 = nativeCarrierToken2;
            return nativeCarrierToken3;
        }
        finally {
            if (!bl3) {
                if (nativeIssuerCustody != null) {
                    FoodWeaponSourceLedger.closeNativeIssuerCustody(nativeIssuerCustody);
                } else {
                    nativeCarrierToken2.actor = false;
                    nativeCarrierToken2.added = false;
                    nativeCarrierToken2.image = null;
                }
            }
        }
    }

    public static NativeCarrierToken nativeIngressActorAtCreation(Entity entity, LivingEntity livingEntity) {
        return FoodWeaponSourceLedger.nativeIngressActorAtCreation(entity, livingEntity, null);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static NativeCarrierToken nativeIngressActorAtCreation(Entity entity, LivingEntity livingEntity, NativeIssuerCustody nativeIssuerCustody) {
        Object object;
        NativeIssueWindow nativeIssueWindow = new NativeIssueWindow((ServerPlayer)(livingEntity instanceof ServerPlayer ? (object = (ServerPlayer)livingEntity) : null));
        object = PRIVATE_INGRESS.get();
        String string = "com.github.L_Ender.cataclysm.entity.projectile.Phantom_Halberd_Entity";
        if (!FoodWeaponSourceLedger.ironBoundary(string, "ftboss$afterCaster", "(Lnet/minecraft/world/entity/LivingEntity;Lorg/spongepowered/asm/mixin/injection/callback/CallbackInfo;)V", "setCaster", "(Lnet/minecraft/world/entity/LivingEntity;)V") || !FoodWeaponSourceLedger.ingressLive((NativeIngressToken)object) || ((NativeIngressToken)object).returned || ((NativeIngressToken)object).kind != NativeIngressKind.SOUL || livingEntity != ((NativeIngressToken)object).player || !FoodWeaponSourceLedger.ingressCarrier(((NativeIngressToken)object).kind, entity) || entity.m_9236_() != livingEntity.m_9236_() || entity.m_213877_() || ((NativeIngressToken)object).carriers.stream().anyMatch(nativeCarrierToken -> nativeCarrierToken.carrier == entity)) {
            return null;
        }
        NativeCarrierToken nativeCarrierToken2 = new NativeCarrierToken((NativeIngressToken)object, entity);
        if (!nativeIssueWindow.unchanged()) {
            return null;
        }
        if (nativeIssuerCustody != null && !nativeIssuerCustody.cut.reserve(nativeCarrierToken2)) {
            return null;
        }
        if (nativeIssuerCustody != null) {
            nativeIssuerCustody.carrier = nativeCarrierToken2;
        }
        boolean bl = false;
        try {
            nativeCarrierToken2.actor = true;
            ((NativeIngressToken)object).carriers.add(nativeCarrierToken2);
            bl = true;
            NativeCarrierToken nativeCarrierToken3 = nativeCarrierToken2;
            return nativeCarrierToken3;
        }
        finally {
            if (!bl) {
                if (nativeIssuerCustody != null) {
                    FoodWeaponSourceLedger.closeNativeIssuerCustody(nativeIssuerCustody);
                } else {
                    nativeCarrierToken2.actor = false;
                    nativeCarrierToken2.added = false;
                    nativeCarrierToken2.image = null;
                }
            }
        }
    }

    public static NativeCarrierToken nativeIngressChild(NativeIngressReceipt nativeIngressReceipt, NativeCarrierToken nativeCarrierToken, Entity entity, UUID uUID) {
        return FoodWeaponSourceLedger.nativeIngressChild(nativeIngressReceipt, nativeCarrierToken, entity, uUID, null);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static NativeCarrierToken nativeIngressChild(NativeIngressReceipt nativeIngressReceipt, NativeCarrierToken nativeCarrierToken2, Entity entity, UUID uUID, NativeIssuerCustody nativeIssuerCustody) {
        NativeIssueWindow nativeIssueWindow = new NativeIssueWindow(nativeIngressReceipt == null ? null : nativeIngressReceipt.ingress.player);
        if (!FoodWeaponSourceLedger.matchesNativeIngress(nativeIngressReceipt, nativeCarrierToken2, nativeIngressReceipt == null ? null : nativeIngressReceipt.ingress.player) || nativeCarrierToken2.ingress.kind != NativeIngressKind.TIDAL || entity == nativeCarrierToken2.carrier || !FoodWeaponSourceLedger.ingressCarrier(NativeIngressKind.TIDAL, entity) || entity.m_9236_() != nativeCarrierToken2.carrier.m_9236_() || entity.m_213877_() || !nativeCarrierToken2.ingress.player.m_20148_().equals(uUID)) {
            return null;
        }
        boolean bl = NATIVE_CALLER.walk(stream -> {
            List<StackWalker.StackFrame> list = stream.filter(stackFrame -> stackFrame.getDeclaringClass() != FoodWeaponSourceLedger.class).limit(3L).toList();
            return list.size() == 3 && list.get(0).getClassName().equals("net.farmingtales.ftboss.item.NativeSpecialCaller") && list.get(0).getMethodName().equals("tentacleChild") && list.get(0).getDescriptor().equals("(Lnet/minecraft/world/entity/Entity;Ljava/util/UUID;Lnet/minecraft/world/entity/Entity;)V") && list.get(1).getClassName().equals("com.github.L_Ender.cataclysm.entity.projectile.Tidal_Tentacle_Entity") && list.get(1).getMethodName().equals("ftboss$childActor") && list.get(1).getDescriptor().equals("(Ljava/lang/Object;Ljava/util/UUID;)V") && list.get(2).getDeclaringClass() == list.get(1).getDeclaringClass() && list.get(2).getMethodName().equals("createChain") && list.get(2).getDescriptor().equals("(Lnet/minecraft/world/entity/Entity;)V");
        });
        if (!bl || nativeCarrierToken2.ingress.carriers.stream().anyMatch(nativeCarrierToken -> nativeCarrierToken.carrier == entity)) {
            return null;
        }
        NativeCarrierToken nativeCarrierToken3 = new NativeCarrierToken(nativeCarrierToken2.ingress, entity);
        if (!nativeIssueWindow.unchanged()) {
            return null;
        }
        if (nativeIssuerCustody != null && !nativeIssuerCustody.cut.reserve(nativeCarrierToken3)) {
            return null;
        }
        if (nativeIssuerCustody != null) {
            nativeIssuerCustody.carrier = nativeCarrierToken3;
        }
        boolean bl2 = false;
        try {
            nativeCarrierToken3.actor = true;
            nativeCarrierToken3.image = nativeCarrierToken2.image.m_6426_();
            nativeCarrierToken2.ingress.carriers.add(nativeCarrierToken3);
            bl2 = true;
            NativeCarrierToken nativeCarrierToken4 = nativeCarrierToken3;
            return nativeCarrierToken4;
        }
        finally {
            if (!bl2) {
                if (nativeIssuerCustody != null) {
                    FoodWeaponSourceLedger.closeNativeIssuerCustody(nativeIssuerCustody);
                } else {
                    nativeCarrierToken3.actor = false;
                    nativeCarrierToken3.added = false;
                    nativeCarrierToken3.image = null;
                }
            }
        }
    }

    public static void nativeIngressChildAdded(NativeCarrierToken nativeCarrierToken) {
        if (nativeCarrierToken == null || nativeCarrierToken.added || !FoodWeaponSourceLedger.ingressAddedBoundary() || !FoodWeaponSourceLedger.ingressLive(nativeCarrierToken.ingress) || !nativeCarrierToken.ingress.returned || nativeCarrierToken.ingress.kind != NativeIngressKind.TIDAL || !nativeCarrierToken.actor || nativeCarrierToken.image == null || nativeCarrierToken.carrier.m_213877_() || nativeCarrierToken.carrier.m_9236_() != nativeCarrierToken.ingress.player.m_9236_() || nativeCarrierToken.ingress.player.m_284548_().m_8791_(nativeCarrierToken.entityId) != nativeCarrierToken.carrier) {
            return;
        }
        nativeCarrierToken.added = true;
    }

    public static void nativeIngressActor(NativeCarrierToken nativeCarrierToken, Entity entity, LivingEntity livingEntity) {
        if (nativeCarrierToken == null || nativeCarrierToken.carrier != entity || nativeCarrierToken.ingress.returned || !FoodWeaponSourceLedger.ingressLive(nativeCarrierToken.ingress) || livingEntity != nativeCarrierToken.ingress.player) {
            return;
        }
        boolean bl = NATIVE_CALLER.walk(stream -> {
            List<StackWalker.StackFrame> list = stream.filter(stackFrame -> stackFrame.getDeclaringClass() != FoodWeaponSourceLedger.class).limit(2L).toList();
            return list.size() == 2 && list.get(0).getClassName().equals("com.github.L_Ender.cataclysm.entity.projectile.Phantom_Halberd_Entity") && list.get(0).getMethodName().equals("ftboss$afterCaster") && list.get(0).getDescriptor().equals("(Lnet/minecraft/world/entity/LivingEntity;Lorg/spongepowered/asm/mixin/injection/callback/CallbackInfo;)V") && list.get(1).getDeclaringClass() == list.get(0).getDeclaringClass() && list.get(1).getMethodName().equals("setCaster") && list.get(1).getDescriptor().equals("(Lnet/minecraft/world/entity/LivingEntity;)V");
        });
        if (bl) {
            nativeCarrierToken.actor = true;
        }
    }

    public static void nativeIngressCreator(NativeCarrierToken nativeCarrierToken, Entity entity, UUID uUID) {
        if (nativeCarrierToken == null || nativeCarrierToken.carrier != entity || nativeCarrierToken.ingress.returned || !FoodWeaponSourceLedger.ingressLive(nativeCarrierToken.ingress) || !nativeCarrierToken.ingress.player.m_20148_().equals(uUID)) {
            return;
        }
        boolean bl = NATIVE_CALLER.walk(stream -> {
            List<StackWalker.StackFrame> list = stream.filter(stackFrame -> stackFrame.getDeclaringClass() != FoodWeaponSourceLedger.class).limit(2L).toList();
            return list.size() == 2 && list.get(0).getClassName().equals("com.github.L_Ender.cataclysm.entity.projectile.Tidal_Tentacle_Entity") && list.get(0).getMethodName().equals("ftboss$afterCreator") && list.get(0).getDescriptor().equals("(Ljava/util/UUID;Lorg/spongepowered/asm/mixin/injection/callback/CallbackInfo;)V") && list.get(1).getDeclaringClass() == list.get(0).getDeclaringClass() && list.get(1).getMethodName().equals("setCreatorEntityUUID") && list.get(1).getDescriptor().equals("(Ljava/util/UUID;)V");
        });
        if (bl) {
            nativeCarrierToken.actor = true;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static NativeIngressReceipt nativeIngressReturned(NativeIngressToken nativeIngressToken) {
        if (nativeIngressToken == null || !FoodWeaponSourceLedger.ingressBoundary(nativeIngressToken.kind, "Return", nativeIngressToken.entry, nativeIngressToken.descriptor)) {
            return null;
        }
        try {
            Object object;
            Object object2;
            CompoundTag compoundTag;
            NativeCarrierToken nativeCarrierToken2;
            int n;
            int n2 = n = nativeIngressToken.kind == NativeIngressKind.ANCIENT && !nativeIngressToken.entry.equals("onLeftClick") ? 2 : nativeIngressToken.kind.wear;
            if (PRIVATE_INGRESS.get() != nativeIngressToken || !FoodWeaponSourceLedger.ingressLive(nativeIngressToken) || nativeIngressToken.returned || nativeIngressToken.tick != FoodWeaponSourceLedger.clock(nativeIngressToken.player) || PRIVATE_INGRESS_WEAR.get() != null || nativeIngressToken.wears != n || nativeIngressToken.kind != NativeIngressKind.CORAL_SPEAR && nativeIngressToken.kind != NativeIngressKind.CORAL_BARDICHE && nativeIngressToken.player.m_21120_(nativeIngressToken.hand) != nativeIngressToken.actual || nativeIngressToken.actual.m_41613_() != 1 || FoodWeaponLifecycle.isBroken(nativeIngressToken.actual) || !nativeIngressToken.rules.equals(FoodWeaponSourceLedger.foundationRules(nativeIngressToken.player, nativeIngressToken.actual)) || nativeIngressToken.kind != NativeIngressKind.MEAT && nativeIngressToken.carriers.isEmpty() && (nativeIngressToken.kind != NativeIngressKind.SOUL || nativeIngressToken.capability == null || nativeIngressToken.capability.canceled)) {
                NativeIngressReceipt nativeIngressReceipt = null;
                return nativeIngressReceipt;
            }
            for (NativeCarrierToken nativeCarrierToken2 : nativeIngressToken.carriers) {
                if (nativeCarrierToken2.actor && nativeCarrierToken2.added && !nativeCarrierToken2.carrier.m_213877_() && nativeCarrierToken2.carrier.m_9236_() == nativeIngressToken.player.m_9236_() && nativeCarrierToken2.entityId.equals(nativeCarrierToken2.carrier.m_20148_()) && nativeIngressToken.player.m_284548_().m_8791_(nativeCarrierToken2.entityId) == nativeCarrierToken2.carrier) continue;
                NativeIngressReceipt nativeIngressReceipt = null;
                return nativeIngressReceipt;
            }
            if (nativeIngressToken.kind.wear == 0 && !FoodWeaponSourceLedger.expectationMatches(nativeIngressToken.expectation, nativeIngressToken.actual)) {
                compoundTag = null;
                return compoundTag;
            }
            compoundTag = nativeIngressToken.actual.m_41739_(new CompoundTag()).m_6426_();
            nativeCarrierToken2 = new CompoundTag();
            nativeCarrierToken2.m_128405_("NativeIngressVersion", 1);
            nativeCarrierToken2.m_128359_("NativeEntry", nativeIngressToken.kind.name());
            nativeCarrierToken2.m_128359_("NativeCaller", nativeIngressToken.kind.itemClass);
            nativeCarrierToken2.m_128359_("NativeMethod", nativeIngressToken.entry);
            nativeCarrierToken2.m_128359_("NativeDescriptor", nativeIngressToken.descriptor);
            nativeCarrierToken2.m_128362_("AttackId", nativeIngressToken.action);
            nativeCarrierToken2.m_128362_("Owner", nativeIngressToken.player.m_20148_());
            nativeCarrierToken2.m_128362_("WeaponId", FoodWeaponLifecycle.weaponId(nativeIngressToken.actual));
            nativeCarrierToken2.m_128362_("ServerEpoch", nativeIngressToken.state.epoch);
            nativeCarrierToken2.m_128362_("OwnerEpoch", nativeIngressToken.ownerEpoch);
            nativeCarrierToken2.m_128359_("OriginalHand", nativeIngressToken.hand.name());
            nativeCarrierToken2.m_128359_("Dimension", nativeIngressToken.dimension);
            nativeCarrierToken2.m_128359_("FoundationApprovedRules", nativeIngressToken.rules);
            nativeCarrierToken2.m_128356_("CapturedAt", nativeIngressToken.tick);
            nativeCarrierToken2.m_128356_("ExpiresAt", nativeIngressToken.tick + 1200L);
            nativeCarrierToken2.m_128405_("NativeWearInvocations", nativeIngressToken.wears);
            nativeCarrierToken2.m_128365_("Before", (Tag)nativeIngressToken.before.m_6426_());
            nativeCarrierToken2.m_128365_("After", (Tag)compoundTag.m_6426_());
            ListTag listTag = new ListTag();
            Object object3 = nativeIngressToken.carriers.iterator();
            while (object3.hasNext()) {
                object2 = object3.next();
                object = new CompoundTag();
                object.m_128362_("Entity", ((NativeCarrierToken)object2).entityId);
                object.m_128359_("Class", ((NativeCarrierToken)object2).carrier.getClass().getName());
                listTag.add(object);
            }
            nativeCarrierToken2.m_128365_("Carriers", (Tag)listTag);
            if (nativeIngressToken.capability != null) {
                nativeCarrierToken2.m_128359_("CapabilityClass", nativeIngressToken.capability.capability.getClass().getName());
            }
            if (((String)(object3 = FoodWeaponSourceLedger.signature((CompoundTag)nativeCarrierToken2, nativeIngressToken.state))).isEmpty()) {
                object2 = null;
                return object2;
            }
            object2 = new NativeIngressReceipt(nativeIngressToken, (CompoundTag)nativeCarrierToken2, (String)object3);
            nativeIngressToken.returned = true;
            for (NativeCarrierToken nativeCarrierToken3 : nativeIngressToken.carriers) {
                nativeCarrierToken3.image = compoundTag.m_6426_();
            }
            object = object2;
            return object;
        }
        finally {
            if (!nativeIngressToken.returned) {
                nativeIngressToken.invalid = true;
            }
            if (PRIVATE_INGRESS.get() == nativeIngressToken) {
                PRIVATE_INGRESS.remove();
            }
            PRIVATE_INGRESS_WEAR.remove();
        }
    }

    public static boolean matchesNativeIngress(NativeIngressReceipt nativeIngressReceipt, ServerPlayer serverPlayer) {
        boolean bl;
        if (nativeIngressReceipt == null || serverPlayer != nativeIngressReceipt.ingress.player || !nativeIngressReceipt.ingress.returned || !FoodWeaponSourceLedger.ingressLive(nativeIngressReceipt.ingress) || nativeIngressReceipt.ingress.actual.m_41613_() != 1 || FoodWeaponLifecycle.isBroken(nativeIngressReceipt.ingress.actual) || !nativeIngressReceipt.ingress.rules.equals(FoodWeaponSourceLedger.foundationRules(serverPlayer, nativeIngressReceipt.ingress.actual))) {
            return false;
        }
        NativeIngressToken nativeIngressToken = nativeIngressReceipt.ingress;
        boolean bl2 = bl = nativeIngressToken.kind == NativeIngressKind.CORAL_SPEAR || nativeIngressToken.kind == NativeIngressKind.CORAL_BARDICHE;
        if (!bl && serverPlayer.m_21120_(nativeIngressToken.hand) != nativeIngressToken.actual) {
            return false;
        }
        if (!FoodWeaponLifecycle.weaponId(nativeIngressToken.actual).equals(nativeIngressReceipt.image.m_128342_("WeaponId"))) {
            return false;
        }
        ItemStack itemStack = ItemStack.m_41712_((CompoundTag)nativeIngressReceipt.image.m_128469_("After"));
        if (!(itemStack.m_41720_() == nativeIngressToken.actual.m_41720_() && FoodWeaponLifecycle.repairProfile(itemStack).equals(FoodWeaponLifecycle.repairProfile(nativeIngressToken.actual)) && FoodWeaponSourceLedger.materialFingerprint(itemStack).equals(FoodWeaponSourceLedger.materialFingerprint(nativeIngressToken.actual)) && FoodWeaponSourceLedger.socketFingerprint(itemStack, FoodWeaponFamilyBuild.kind(itemStack).name()).equals(FoodWeaponSourceLedger.socketFingerprint(nativeIngressToken.actual, FoodWeaponFamilyBuild.kind(itemStack).name())))) {
            return false;
        }
        String string = FoodWeaponSourceLedger.signature(nativeIngressReceipt.image, nativeIngressToken.state);
        return !string.isEmpty() && MessageDigest.isEqual(nativeIngressReceipt.proof.getBytes(StandardCharsets.US_ASCII), string.getBytes(StandardCharsets.US_ASCII));
    }

    public static boolean matchesNativeIngress(NativeIngressReceipt nativeIngressReceipt, NativeCarrierToken nativeCarrierToken, ServerPlayer serverPlayer) {
        Projectile projectile;
        if (!(FoodWeaponSourceLedger.matchesNativeIngress(nativeIngressReceipt, serverPlayer) && nativeCarrierToken != null && nativeIngressReceipt.ingress == nativeCarrierToken.ingress && nativeCarrierToken.actor && nativeCarrierToken.added && nativeCarrierToken.image != null && !nativeCarrierToken.carrier.m_213877_() && nativeCarrierToken.carrier.m_9236_() == serverPlayer.m_9236_() && nativeCarrierToken.entityId.equals(nativeCarrierToken.carrier.m_20148_()) && serverPlayer.m_284548_().m_8791_(nativeCarrierToken.entityId) == nativeCarrierToken.carrier)) {
            return false;
        }
        Entity entity = nativeCarrierToken.carrier;
        return !(entity instanceof Projectile) || (projectile = (Projectile)entity).m_19749_() == serverPlayer;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static boolean nativeCarrierHurt(NativeIngressReceipt nativeIngressReceipt, NativeCarrierToken nativeCarrierToken, Entity entity, DamageSource damageSource, float f) {
        NativeHurtScope nativeHurtScope = PRIVATE_NATIVE_HURT.get();
        if (nativeHurtScope != null) {
            FoodWeaponSourceLedger.closeNativeHurtScope(nativeHurtScope);
            return entity.m_6469_(damageSource, f);
        }
        NativeHurtScope nativeHurtScope2 = FoodWeaponSourceLedger.beginNativeCarrierHurt(nativeIngressReceipt, nativeCarrierToken, entity, damageSource, f);
        if (nativeHurtScope2 == null) {
            return entity.m_6469_(damageSource, f);
        }
        try {
            boolean bl = entity.m_6469_(damageSource, f);
            return bl;
        }
        finally {
            FoodWeaponSourceLedger.closeNativeHurtScope(nativeHurtScope2);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static NativeHurtScope beginNativeCarrierHurt(NativeIngressReceipt nativeIngressReceipt, NativeCarrierToken nativeCarrierToken, Entity entity, DamageSource damageSource, float f) {
        NativeIssueWindow nativeIssueWindow = new NativeIssueWindow(nativeIngressReceipt == null ? null : nativeIngressReceipt.ingress.player);
        if (nativeIngressReceipt == null || nativeCarrierToken == null || entity == null || damageSource == null || damageSource.getClass() != DamageSource.class || !Float.isFinite(f) || f <= 0.0f || !FoodWeaponSourceLedger.matchesNativeIngress(nativeIngressReceipt, nativeCarrierToken, nativeIngressReceipt.ingress.player) || !FoodWeaponSourceLedger.nativeHurtCaller(nativeIngressReceipt.ingress.kind, nativeCarrierToken.carrier) || entity.m_213877_() || entity.m_9236_() != nativeIngressReceipt.ingress.player.m_9236_() || nativeIngressReceipt.ingress.player.m_284548_().m_8791_(entity.m_20148_()) != entity || damageSource.m_7640_() != nativeCarrierToken.carrier || damageSource.m_7639_() != nativeIngressReceipt.ingress.player || !FoodWeaponRanged.allowsWeaponEffects(damageSource)) {
            return null;
        }
        String string = damageSource.m_269150_().m_203543_().map(resourceKey -> resourceKey.m_135782_().toString()).orElse("");
        if (string.isEmpty() || nativeIngressReceipt.ingress.kind == NativeIngressKind.ENDER && (!string.equals("armamentarium:cacophony") || Float.compare(f, 8.0f) != 0)) {
            return null;
        }
        CompoundTag compoundTag = FoodWeaponSourceLedger.nativeHurtSnapshot(nativeIngressReceipt, nativeCarrierToken, entity, damageSource, f);
        if (compoundTag.m_128456_()) {
            return null;
        }
        NativeHurtScope nativeHurtScope = new NativeHurtScope(nativeIngressReceipt, nativeCarrierToken, entity, damageSource, compoundTag);
        if (!nativeIssueWindow.unchanged()) {
            return null;
        }
        boolean bl = false;
        try {
            PRIVATE_NATIVE_HURT.set(nativeHurtScope);
            if (!FoodWeaponSourceLedger.validReadOnly(compoundTag, nativeHurtScope.player)) {
                FoodWeaponSourceLedger.closeNativeHurtScope(nativeHurtScope);
                NativeHurtScope nativeHurtScope2 = null;
                return nativeHurtScope2;
            }
            nativeHurtScope.interaction = FoodWeaponSourceLedger.beginNativeImpactInteraction(nativeHurtScope, f);
            if (nativeHurtScope.interaction == null) {
                FoodWeaponSourceLedger.closeNativeHurtScope(nativeHurtScope);
                NativeHurtScope nativeHurtScope3 = null;
                return nativeHurtScope3;
            }
            bl = true;
            NativeHurtScope nativeHurtScope4 = nativeHurtScope;
            return nativeHurtScope4;
        }
        finally {
            if (!bl) {
                FoodWeaponSourceLedger.revokeUnreturnedHurt(nativeHurtScope);
            }
        }
    }

    private static boolean nativeHurtCaller(NativeIngressKind nativeIngressKind, Entity entity) {
        return NATIVE_CALLER.walk(stream -> {
            List<StackWalker.StackFrame> list = stream.filter(stackFrame -> stackFrame.getDeclaringClass() != FoodWeaponSourceLedger.class).limit(3L).toList();
            if (list.size() != 3) {
                return false;
            }
            StackWalker.StackFrame stackFrame2 = list.get(0);
            StackWalker.StackFrame stackFrame3 = list.get(1);
            StackWalker.StackFrame stackFrame4 = list.get(2);
            if (!stackFrame2.getClassName().equals("net.farmingtales.ftboss.item.NativeSpecialCaller") || stackFrame2.getDeclaringClass().getClassLoader() != FoodWeaponSourceLedger.class.getClassLoader() || stackFrame3.getDeclaringClass().getClassLoader() != entity.getClass().getClassLoader() || stackFrame3.getDeclaringClass() != stackFrame4.getDeclaringClass()) {
                return false;
            }
            String string = "(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/damagesource/DamageSource;F)Z";
            if (nativeIngressKind == NativeIngressKind.ENDER) {
                String string2;
                if (entity.getClass().getName().equals("net.gqstavo.armamentarium.entity.MiteArrowEntity")) {
                    string2 = "net.gqstavo.armamentarium.procedures.MiteArrowProjectileHitsLivingEntityProcedure";
                } else if (entity.getClass().getName().equals("net.gqstavo.armamentarium.entity.MitePelletEntity")) {
                    string2 = "net.gqstavo.armamentarium.procedures.MitePelletProjectileHitsLivingEntityProcedure";
                } else {
                    return false;
                }
                return stackFrame2.getMethodName().equals("miteHurt") && stackFrame2.getDescriptor().equals(string) && stackFrame3.getClassName().equals(string2) && stackFrame3.getMethodName().equals("ftboss$exactMiteHurt") && stackFrame3.getDescriptor().equals(string) && stackFrame4.getMethodName().equals("execute") && stackFrame4.getDescriptor().equals("(Lnet/minecraft/world/level/LevelAccessor;DDDLnet/minecraft/world/entity/Entity;)V");
            }
            if (!stackFrame2.getMethodName().equals("delayedHurt") || !stackFrame2.getDescriptor().equals("(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/damagesource/DamageSource;F)Z") || stackFrame3.getDeclaringClass() != entity.getClass()) {
                return false;
            }
            if (nativeIngressKind == NativeIngressKind.SOUL) {
                return stackFrame3.getMethodName().equals("ftboss$halberdNativeHurt") && stackFrame3.getDescriptor().equals("(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/damagesource/DamageSource;F)Z") && stackFrame4.getMethodName().equals("damage") && stackFrame4.getDescriptor().equals("(Lnet/minecraft/world/entity/LivingEntity;)V");
            }
            return nativeIngressKind == NativeIngressKind.TIDAL && stackFrame3.getMethodName().equals("ftboss$tentacleNativeHurt") && stackFrame3.getDescriptor().equals(string) && stackFrame4.getMethodName().equals("m_8119_") && stackFrame4.getDescriptor().equals("()V");
        });
    }

    private static CompoundTag nativeHurtSnapshot(NativeIngressReceipt nativeIngressReceipt, NativeCarrierToken nativeCarrierToken, Entity entity, DamageSource damageSource, float f) {
        NativeIngressToken nativeIngressToken = nativeIngressReceipt.ingress;
        CompoundTag compoundTag = new CompoundTag();
        compoundTag.m_128359_("DamagePurpose", NATIVE_HURT_PURPOSE);
        compoundTag.m_128359_("NativeEntry", nativeIngressToken.kind.name());
        compoundTag.m_128362_("NativeReceiptId", nativeIngressToken.action);
        compoundTag.m_128359_("NativeIngressProof", nativeIngressReceipt.proof);
        compoundTag.m_128359_("NativeHurtPhase", "ACTUAL_HURT_ADMISSIBLE");
        compoundTag.m_128359_("NativeOuterPhase", "NORMAL_COMPLETED");
        return FoodWeaponSourceLedger.signActualHurt(nativeIngressToken.player, ItemStack.m_41712_((CompoundTag)nativeCarrierToken.image), nativeIngressToken.hand, nativeIngressToken.action, nativeIngressToken.state, nativeIngressToken.ownerEpoch, nativeIngressToken.tick, nativeIngressToken.dimension, nativeIngressToken.rules, compoundTag, entity, damageSource, f);
    }

    private static CompoundTag signActualHurt(ServerPlayer serverPlayer, ItemStack itemStack, InteractionHand interactionHand, UUID uUID, ServerState serverState, UUID uUID2, long l, String string, String string2, CompoundTag compoundTag, Entity entity, DamageSource damageSource, float f) {
        String string3 = FoodWeaponFamilyBuild.kind(itemStack).name();
        String string4 = FoodWeaponSourceLedger.materialFingerprint(itemStack);
        String string5 = damageSource.m_269150_().m_203543_().map(resourceKey -> resourceKey.m_135782_().toString()).orElse("");
        if (itemStack.m_41619_() || itemStack.m_41613_() != 1 || string4.isEmpty() || string5.isEmpty() || !List.of("BOW", "STAFF", "MELEE").contains(string3) || damageSource.m_7640_() == null) {
            return new CompoundTag();
        }
        CompoundTag compoundTag2 = compoundTag.m_6426_();
        compoundTag2.m_128359_("Family", string3);
        try {
            FoodWeaponDamageScaling.freezeFactors(itemStack, compoundTag2, 1.0, 1.0, 1.0);
        }
        catch (IllegalArgumentException illegalArgumentException) {
            return new CompoundTag();
        }
        if (!Double.isFinite(FoodWeaponDamageScaling.residual(compoundTag2))) {
            return new CompoundTag();
        }
        compoundTag2.m_128405_("SnapshotVersion", 3);
        compoundTag2.m_128362_("Owner", serverPlayer.m_20148_());
        compoundTag2.m_128362_("AttackId", uUID);
        compoundTag2.m_128362_("WeaponId", FoodWeaponLifecycle.weaponId(itemStack));
        compoundTag2.m_128362_("ServerEpoch", serverState.epoch);
        compoundTag2.m_128362_("OwnerEpoch", uUID2);
        compoundTag2.m_128359_("OriginalHand", interactionHand.name());
        compoundTag2.m_128359_("Dimension", string);
        compoundTag2.m_128356_("CapturedAt", l);
        compoundTag2.m_128356_("ExpiresAt", l + 1200L);
        compoundTag2.m_128359_("BoundProfile", FoodWeaponSourceLedger.boundProfile(itemStack));
        compoundTag2.m_128359_("RepairProfile", FoodWeaponLifecycle.repairProfile(itemStack));
        compoundTag2.m_128359_("SourceKind", string3);
        compoundTag2.m_128362_("NativeEntityId", damageSource.m_7640_().m_20148_());
        compoundTag2.m_128362_("NativeHurtId", UUID.randomUUID());
        compoundTag2.m_128362_("NativeTargetId", entity.m_20148_());
        compoundTag2.m_128356_("NativeHurtAt", FoodWeaponSourceLedger.clock(serverPlayer));
        compoundTag2.m_128359_("NativeSourceClass", damageSource.getClass().getName());
        compoundTag2.m_128359_("NativeDamageType", string5);
        compoundTag2.m_128350_("NativeAmount", f);
        compoundTag2.m_128365_("Weapon", (Tag)itemStack.m_41739_(new CompoundTag()));
        compoundTag2.m_128359_("MaterialFingerprint", string4);
        compoundTag2.m_128359_("SocketFingerprint", FoodWeaponSourceLedger.socketFingerprint(itemStack, string3));
        compoundTag2.m_128365_("FoundationSnapshot", (Tag)FoodWeaponMaterialFingerprint.foundationSnapshot(itemStack));
        compoundTag2.m_128359_("FoundationApprovedRules", string2);
        if (compoundTag2.m_128469_("FoundationSnapshot").m_128456_() || !FoodWeaponSourceLedger.canonicalInputValid((Tag)compoundTag2)) {
            return new CompoundTag();
        }
        if (!OwnerRepairGenerationAuthority.stamp(itemStack, compoundTag2, serverState.revocations)) {
            return new CompoundTag();
        }
        String string6 = FoodWeaponSourceLedger.signature(compoundTag2, serverState);
        if (string6.isEmpty()) {
            return new CompoundTag();
        }
        compoundTag2.m_128359_("ServerSignature", string6);
        if (!OwnerFullRepairGuard.bind(itemStack, compoundTag2.m_128342_("WeaponId"))) {
            return new CompoundTag();
        }
        return compoundTag2;
    }

    private static boolean nativeHurtLive(NativeHurtScope nativeHurtScope) {
        if (nativeHurtScope == null || !OwnerRepairGenerationAuthority.snapshotAllowed(nativeHurtScope.snapshot) || nativeHurtScope.closed || PRIVATE_NATIVE_HURT.get() != nativeHurtScope || nativeHurtScope.thread != Thread.currentThread() || nativeHurtScope.tick != FoodWeaponSourceLedger.clock(nativeHurtScope.player) || !nativeHurtScope.player.m_6084_() || nativeHurtScope.player.m_5833_() || nativeHurtScope.target.m_213877_() || nativeHurtScope.target.m_9236_() != nativeHurtScope.player.m_9236_() || !nativeHurtScope.snapshot.m_128342_("NativeTargetId").equals(nativeHurtScope.target.m_20148_()) || nativeHurtScope.player.m_284548_().m_8791_(nativeHurtScope.target.m_20148_()) != nativeHurtScope.target || nativeHurtScope.source.m_7639_() != nativeHurtScope.player || (nativeHurtScope.ironCallback != null ? !FoodWeaponSourceLedger.ironActualSource(nativeHurtScope.ironCallback, nativeHurtScope.source) : nativeHurtScope.source.getClass() != DamageSource.class) || nativeHurtScope.source.m_7640_() != (nativeHurtScope.carrier == null ? nativeHurtScope.player : nativeHurtScope.carrier.carrier) || !nativeHurtScope.source.m_269150_().m_203543_().map(resourceKey -> resourceKey.m_135782_().toString().equals(nativeHurtScope.snapshot.m_128461_("NativeDamageType"))).orElse(false).booleanValue() || !FoodWeaponRanged.allowsWeaponEffects(nativeHurtScope.source)) {
            return false;
        }
        if (nativeHurtScope.ironCallback != null) {
            return FoodWeaponSourceLedger.ironCallbackReady(nativeHurtScope.ironCallback);
        }
        return nativeHurtScope.earlyIngress == null ? FoodWeaponSourceLedger.matchesNativeIngress(nativeHurtScope.receipt, nativeHurtScope.carrier, nativeHurtScope.player) : FoodWeaponSourceLedger.meatHurtReady(nativeHurtScope.earlyIngress);
    }

    static void revokeActualHurt(DamageSource damageSource, ServerPlayer serverPlayer) {
        NativeHurtScope nativeHurtScope = PRIVATE_NATIVE_HURT.get();
        if (nativeHurtScope != null && nativeHurtScope.source == damageSource && nativeHurtScope.player == serverPlayer) {
            FoodWeaponSourceLedger.closeNativeHurtScope(nativeHurtScope);
        }
    }

    public static boolean matchesNativeHurt(DamageSource damageSource, ServerPlayer serverPlayer, Entity entity, CompoundTag compoundTag) {
        NativeHurtScope nativeHurtScope = PRIVATE_NATIVE_HURT.get();
        return nativeHurtScope != null && damageSource == nativeHurtScope.source && serverPlayer == nativeHurtScope.player && entity == nativeHurtScope.target && compoundTag != null && compoundTag.equals((Object)nativeHurtScope.snapshot) && FoodWeaponSourceLedger.nativeHurtLive(nativeHurtScope) && FoodWeaponSourceLedger.validReadOnly(compoundTag, serverPlayer);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static boolean nativeMeatActualHurt(Entity entity, DamageSource damageSource, float f) {
        NativeHurtScope nativeHurtScope = PRIVATE_NATIVE_HURT.get();
        if (nativeHurtScope != null) {
            FoodWeaponSourceLedger.closeNativeHurtScope(nativeHurtScope);
            return entity.m_6469_(damageSource, f);
        }
        NativeHurtScope nativeHurtScope2 = FoodWeaponSourceLedger.beginMeatActualHurt(entity, damageSource, f);
        if (nativeHurtScope2 == null) {
            return entity.m_6469_(damageSource, f);
        }
        try {
            boolean bl = entity.m_6469_(damageSource, f);
            return bl;
        }
        finally {
            FoodWeaponSourceLedger.closeNativeHurtScope(nativeHurtScope2);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static NativeHurtScope beginMeatActualHurt(Entity entity, DamageSource damageSource, float f) {
        NativeIssueWindow nativeIssueWindow = new NativeIssueWindow(null);
        NativeIngressToken nativeIngressToken = PRIVATE_INGRESS.get();
        if (!FoodWeaponSourceLedger.meatHurtReady(nativeIngressToken) || entity == null || damageSource == null || damageSource.getClass() != DamageSource.class || !Float.isFinite(f) || f <= 0.0f || !FoodWeaponSourceLedger.meatHurtCaller(nativeIngressToken) || entity.m_213877_() || entity.m_9236_() != nativeIngressToken.player.m_9236_() || nativeIngressToken.player.m_284548_().m_8791_(entity.m_20148_()) != entity || damageSource.m_7640_() != nativeIngressToken.player || damageSource.m_7639_() != nativeIngressToken.player || !FoodWeaponRanged.allowsWeaponEffects(damageSource)) {
            return null;
        }
        CompoundTag compoundTag = new CompoundTag();
        compoundTag.m_128359_("DamagePurpose", MEAT_HURT_PURPOSE);
        compoundTag.m_128359_("NativeEntry", "MEAT");
        compoundTag.m_128359_("NativeCostSchedule", "ORIGINAL_ZERO_WEAR");
        compoundTag.m_128359_("NativeHurtPhase", "ACTUAL_HURT_ADMISSIBLE");
        compoundTag.m_128359_("NativeOuterPhase", "PENDING_NOT_NORMAL_COMPLETED");
        CompoundTag compoundTag2 = FoodWeaponSourceLedger.signActualHurt(nativeIngressToken.player, nativeIngressToken.actual, nativeIngressToken.hand, nativeIngressToken.action, nativeIngressToken.state, nativeIngressToken.ownerEpoch, nativeIngressToken.tick, nativeIngressToken.dimension, nativeIngressToken.rules, compoundTag, entity, damageSource, f);
        if (compoundTag2.m_128456_()) {
            return null;
        }
        NativeHurtScope nativeHurtScope = new NativeHurtScope(nativeIngressToken, entity, damageSource, compoundTag2);
        if (!nativeIssueWindow.unchanged()) {
            return null;
        }
        boolean bl = false;
        try {
            PRIVATE_NATIVE_HURT.set(nativeHurtScope);
            if (!FoodWeaponSourceLedger.validReadOnly(compoundTag2, nativeHurtScope.player)) {
                FoodWeaponSourceLedger.closeNativeHurtScope(nativeHurtScope);
                NativeHurtScope nativeHurtScope2 = null;
                return nativeHurtScope2;
            }
            nativeHurtScope.interaction = FoodWeaponSourceLedger.beginNativeImpactInteraction(nativeHurtScope, f);
            if (nativeHurtScope.interaction == null) {
                FoodWeaponSourceLedger.closeNativeHurtScope(nativeHurtScope);
                NativeHurtScope nativeHurtScope3 = null;
                return nativeHurtScope3;
            }
            bl = true;
            NativeHurtScope nativeHurtScope4 = nativeHurtScope;
            return nativeHurtScope4;
        }
        finally {
            if (!bl) {
                FoodWeaponSourceLedger.revokeUnreturnedHurt(nativeHurtScope);
            }
        }
    }

    private static boolean meatHurtReady(NativeIngressToken nativeIngressToken) {
        return nativeIngressToken != null && nativeIngressToken.kind == NativeIngressKind.MEAT && !nativeIngressToken.returned && FoodWeaponSourceLedger.ingressLive(nativeIngressToken) && PRIVATE_INGRESS.get() == nativeIngressToken && PRIVATE_INGRESS_WEAR.get() == null && nativeIngressToken.wears == 0 && nativeIngressToken.tick == FoodWeaponSourceLedger.clock(nativeIngressToken.player) && nativeIngressToken.actual.m_41613_() == 1 && !FoodWeaponLifecycle.isBroken(nativeIngressToken.actual) && nativeIngressToken.player.m_21120_(nativeIngressToken.hand) == nativeIngressToken.actual && nativeIngressToken.player.m_6117_() && nativeIngressToken.player.m_21211_() == nativeIngressToken.actual && nativeIngressToken.player.m_7655_() == nativeIngressToken.hand && nativeIngressToken.rules.equals(FoodWeaponSourceLedger.foundationRules(nativeIngressToken.player, nativeIngressToken.actual)) && FoodWeaponSourceLedger.expectationMatches(nativeIngressToken.expectation, nativeIngressToken.actual);
    }

    private static boolean meatHurtCaller(NativeIngressToken nativeIngressToken) {
        return NATIVE_CALLER.walk(stream -> {
            List<StackWalker.StackFrame> list = stream.filter(stackFrame -> stackFrame.getDeclaringClass() != FoodWeaponSourceLedger.class).limit(2L).toList();
            return list.size() == 2 && list.get(0).getClassName().equals(nativeIngressToken.kind.itemClass) && list.get(0).getMethodName().equals("nativeActualShredderHit") && list.get(0).getDescriptor().equals("(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/damagesource/DamageSource;FLnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;I)Z") && list.get(1).getDeclaringClass() == list.get(0).getDeclaringClass() && list.get(1).getMethodName().equals(nativeIngressToken.entry) && list.get(1).getDescriptor().equals(nativeIngressToken.descriptor);
        });
    }

    static boolean actualHurtSnapshot(CompoundTag compoundTag) {
        NativeHurtScope nativeHurtScope = PRIVATE_NATIVE_HURT.get();
        return nativeHurtScope != null && compoundTag != null && compoundTag.equals((Object)nativeHurtScope.snapshot);
    }

    public static NativeIronCallbackToken nativeIronCallbackBegin(NativeIronPaymentToken nativeIronPaymentToken, Object object, LivingEntity livingEntity, Object object2, Object object3, int n) {
        return FoodWeaponSourceLedger.nativeIronCallbackBegin(nativeIronPaymentToken, object, livingEntity, object2, object3, n, null);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static NativeIronCallbackToken nativeIronCallbackBegin(NativeIronPaymentToken nativeIronPaymentToken, Object object, LivingEntity livingEntity, Object object2, Object object3, int n, NativeIssuerCustody nativeIssuerCustody) {
        NativeIssueWindow nativeIssueWindow = new NativeIssueWindow(nativeIronPaymentToken == null ? null : nativeIronPaymentToken.cast.use.player);
        String string = "(Lnet/minecraft/world/level/Level;ILnet/minecraft/server/level/ServerPlayer;Lio/redspace/ironsspellbooks/api/spells/CastSource;Z)V";
        if (!FoodWeaponSourceLedger.ironBoundary("io.redspace.ironsspellbooks.api.spells.AbstractSpell", "ftboss$v9$ironCast", "(Lio/redspace/ironsspellbooks/api/spells/AbstractSpell;Lnet/minecraft/world/level/Level;ILnet/minecraft/world/entity/LivingEntity;Lio/redspace/ironsspellbooks/api/spells/CastSource;Lio/redspace/ironsspellbooks/api/magic/MagicData;)V", "castSpell", string)) {
            return null;
        }
        if (!nativeIssueWindow.unchanged()) {
            return null;
        }
        NativeIronCallbackToken nativeIronCallbackToken = PRIVATE_IRON_CALLBACK.get();
        if (nativeIronCallbackToken != null) {
            FoodWeaponSourceLedger.nativeIronCallbackClose(nativeIronCallbackToken);
            return null;
        }
        if (nativeIronPaymentToken == null || nativeIronPaymentToken.invalid || !nativeIronPaymentToken.mana || nativeIronPaymentToken.callback || nativeIronPaymentToken.returned || nativeIronPaymentToken.cast.invalid || !nativeIronPaymentToken.cast.accepted || !FoodWeaponSourceLedger.ironLive(nativeIronPaymentToken.cast.use) || nativeIronPaymentToken.tick != FoodWeaponSourceLedger.clock(nativeIronPaymentToken.cast.use.player) || nativeIronPaymentToken.cast.spell != object || nativeIronPaymentToken.cast.use.player != livingEntity || nativeIronPaymentToken.magicData != object2 || nativeIronPaymentToken.cast.castSource != object3 || nativeIronPaymentToken.level != n || !Float.isFinite(nativeIronPaymentToken.manaAfter) || Float.compare(nativeIronPaymentToken.manaAfter, Math.max(0.0f, nativeIronPaymentToken.manaBefore - (float)nativeIronPaymentToken.manaCost)) != 0) {
            return null;
        }
        NativeIronCallbackToken nativeIronCallbackToken2 = new NativeIronCallbackToken(nativeIronPaymentToken);
        if (!nativeIssueWindow.unchanged()) {
            return null;
        }
        if (nativeIssuerCustody != null && !nativeIssuerCustody.cut.reserve(nativeIronCallbackToken2)) {
            return null;
        }
        if (nativeIssuerCustody != null) {
            nativeIssuerCustody.callback = nativeIronCallbackToken2;
        }
        boolean bl = false;
        try {
            PRIVATE_IRON_CALLBACK.set(nativeIronCallbackToken2);
            bl = true;
            NativeIronCallbackToken nativeIronCallbackToken3 = nativeIronCallbackToken2;
            return nativeIronCallbackToken3;
        }
        finally {
            if (!bl) {
                if (nativeIssuerCustody != null) {
                    FoodWeaponSourceLedger.closeNativeIssuerCustody(nativeIssuerCustody);
                } else {
                    nativeIronCallbackToken2.closed = true;
                    nativeIronCallbackToken2.expectation.closed = true;
                    nativeIronCallbackToken2.expectation.cursor.close();
                    if (PRIVATE_IRON_CALLBACK.get() == nativeIronCallbackToken2) {
                        PRIVATE_IRON_CALLBACK.remove();
                    }
                }
            }
        }
    }

    public static void nativeIronCallbackReturned(NativeIronCallbackToken nativeIronCallbackToken) {
        if (nativeIronCallbackToken == null) {
            return;
        }
        String string = "(Lnet/minecraft/world/level/Level;ILnet/minecraft/server/level/ServerPlayer;Lio/redspace/ironsspellbooks/api/spells/CastSource;Z)V";
        if (!FoodWeaponSourceLedger.ironBoundary("io.redspace.ironsspellbooks.api.spells.AbstractSpell", "ftboss$v9$ironCast", "(Lio/redspace/ironsspellbooks/api/spells/AbstractSpell;Lnet/minecraft/world/level/Level;ILnet/minecraft/world/entity/LivingEntity;Lio/redspace/ironsspellbooks/api/spells/CastSource;Lio/redspace/ironsspellbooks/api/magic/MagicData;)V", "castSpell", string)) {
            return;
        }
        nativeIronCallbackToken.normal = PRIVATE_IRON_CALLBACK.get() == nativeIronCallbackToken && !nativeIronCallbackToken.closed && nativeIronCallbackToken.thread == Thread.currentThread() && nativeIronCallbackToken.tick == FoodWeaponSourceLedger.clock(nativeIronCallbackToken.payment.cast.use.player) && nativeIronCallbackToken.payment.callback && !nativeIronCallbackToken.payment.invalid && !nativeIronCallbackToken.payment.returned && !nativeIronCallbackToken.payment.cast.invalid && FoodWeaponSourceLedger.ironLive(nativeIronCallbackToken.payment.cast.use);
        FoodWeaponSourceLedger.nativeIronCallbackClose(nativeIronCallbackToken);
    }

    public static synchronized void nativeIronCallbackClose(NativeIronCallbackToken nativeIronCallbackToken) {
        NativeHurtScope nativeHurtScope;
        if (nativeIronCallbackToken == null) {
            return;
        }
        nativeIronCallbackToken.expectation.closed = true;
        nativeIronCallbackToken.expectation.cursor.close();
        nativeIronCallbackToken.closed = true;
        if (PRIVATE_IRON_CALLBACK.get() == nativeIronCallbackToken) {
            PRIVATE_IRON_CALLBACK.remove();
        }
        if ((nativeHurtScope = PRIVATE_NATIVE_HURT.get()) != null && nativeHurtScope.ironCallback == nativeIronCallbackToken) {
            FoodWeaponSourceLedger.closeNativeHurtScope(nativeHurtScope);
        }
        if (!nativeIronCallbackToken.normal) {
            nativeIronCallbackToken.payment.invalid = true;
            FoodWeaponSourceLedger.cancelNativeIron(nativeIronCallbackToken.payment.cast);
        }
    }

    private static boolean ironCallbackReady(NativeIronCallbackToken nativeIronCallbackToken) {
        return nativeIronCallbackToken != null && !nativeIronCallbackToken.closed && !nativeIronCallbackToken.normal && PRIVATE_IRON_CALLBACK.get() == nativeIronCallbackToken && nativeIronCallbackToken.thread == Thread.currentThread() && nativeIronCallbackToken.tick == FoodWeaponSourceLedger.clock(nativeIronCallbackToken.payment.cast.use.player) && !nativeIronCallbackToken.payment.invalid && nativeIronCallbackToken.payment.mana && !nativeIronCallbackToken.payment.callback && !nativeIronCallbackToken.payment.returned && !nativeIronCallbackToken.payment.cast.invalid && nativeIronCallbackToken.payment.cast.accepted && FoodWeaponSourceLedger.ironLive(nativeIronCallbackToken.payment.cast.use) && FoodWeaponSourceLedger.expectationMatches(nativeIronCallbackToken.expectation, nativeIronCallbackToken.payment.cast.use.actual) && Float.isFinite(nativeIronCallbackToken.payment.manaAfter) && Float.compare(nativeIronCallbackToken.payment.manaAfter, Math.max(0.0f, nativeIronCallbackToken.payment.manaBefore - (float)nativeIronCallbackToken.payment.manaCost)) == 0;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static boolean nativeIronActualHurt(Entity entity, DamageSource damageSource, float f) {
        NativeHurtScope nativeHurtScope = PRIVATE_NATIVE_HURT.get();
        if (nativeHurtScope != null) {
            FoodWeaponSourceLedger.closeNativeHurtScope(nativeHurtScope);
            return entity.m_6469_(damageSource, f);
        }
        NativeHurtScope nativeHurtScope2 = FoodWeaponSourceLedger.beginIronActualHurt(entity, damageSource, f);
        if (nativeHurtScope2 == null) {
            return entity.m_6469_(damageSource, f);
        }
        try {
            boolean bl = entity.m_6469_(damageSource, f);
            return bl;
        }
        finally {
            FoodWeaponSourceLedger.closeNativeHurtScope(nativeHurtScope2);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static NativeHurtScope beginIronActualHurt(Entity entity, DamageSource damageSource, float f) {
        NativeIssueWindow nativeIssueWindow = new NativeIssueWindow(null);
        NativeIronCallbackToken nativeIronCallbackToken = PRIVATE_IRON_CALLBACK.get();
        if (!(FoodWeaponSourceLedger.ironCallbackReady(nativeIronCallbackToken) && entity != null && damageSource != null && Float.isFinite(f) && !(f <= 0.0f) && FoodWeaponSourceLedger.ironActualHurtCaller(nativeIronCallbackToken) && FoodWeaponSourceLedger.ironActualSource(nativeIronCallbackToken, damageSource))) {
            return null;
        }
        NativeIronUseToken nativeIronUseToken = nativeIronCallbackToken.payment.cast.use;
        if (entity.m_213877_() || entity.m_9236_() != nativeIronUseToken.player.m_9236_() || nativeIronUseToken.player.m_284548_().m_8791_(entity.m_20148_()) != entity || damageSource.m_7640_() != nativeIronUseToken.player || damageSource.m_7639_() != nativeIronUseToken.player || !FoodWeaponRanged.allowsWeaponEffects(damageSource)) {
            return null;
        }
        CompoundTag compoundTag = new CompoundTag();
        compoundTag.m_128359_("DamagePurpose", IRON_HURT_PURPOSE);
        compoundTag.m_128359_("NativeEntry", "IRON_PAID_ONCAST");
        compoundTag.m_128359_("NativeHurtPhase", "ACTUAL_HURT_ADMISSIBLE");
        compoundTag.m_128359_("NativeOuterPhase", "CALLBACK_ACTIVE_NOT_NORMAL_COMPLETED");
        compoundTag.m_128359_("NativeSpellClass", nativeIronCallbackToken.payment.cast.spell.getClass().getName());
        compoundTag.m_128359_("NativeCastSource", nativeIronCallbackToken.payment.cast.source);
        compoundTag.m_128359_("NativeCastSlot", nativeIronCallbackToken.payment.cast.slot);
        compoundTag.m_128405_("NativeSelectedLevel", nativeIronCallbackToken.payment.cast.selectedLevel);
        compoundTag.m_128405_("NativeEffectiveLevel", nativeIronCallbackToken.payment.level);
        compoundTag.m_128405_("NativeManaCost", nativeIronCallbackToken.payment.manaCost);
        compoundTag.m_128350_("NativeManaBefore", nativeIronCallbackToken.payment.manaBefore);
        compoundTag.m_128350_("NativeManaAfter", nativeIronCallbackToken.payment.manaAfter);
        compoundTag.m_128350_("NativeManaDelta", nativeIronCallbackToken.payment.manaBefore - nativeIronCallbackToken.payment.manaAfter);
        compoundTag.m_128356_("NativeTriggerTick", nativeIronUseToken.tick);
        CompoundTag compoundTag2 = FoodWeaponSourceLedger.signActualHurt(nativeIronUseToken.player, nativeIronUseToken.actual, nativeIronUseToken.hand, nativeIronCallbackToken.payment.cast.action, nativeIronUseToken.state, nativeIronUseToken.ownerEpoch, nativeIronCallbackToken.tick, nativeIronUseToken.dimension, nativeIronUseToken.rules, compoundTag, entity, damageSource, f);
        if (compoundTag2.m_128456_()) {
            return null;
        }
        NativeHurtScope nativeHurtScope = new NativeHurtScope(nativeIronCallbackToken, entity, damageSource, compoundTag2);
        if (!nativeIssueWindow.unchanged()) {
            return null;
        }
        boolean bl = false;
        try {
            PRIVATE_NATIVE_HURT.set(nativeHurtScope);
            if (!FoodWeaponSourceLedger.validReadOnly(compoundTag2, nativeHurtScope.player)) {
                FoodWeaponSourceLedger.closeNativeHurtScope(nativeHurtScope);
                NativeHurtScope nativeHurtScope2 = null;
                return nativeHurtScope2;
            }
            nativeHurtScope.interaction = FoodWeaponSourceLedger.beginNativeImpactInteraction(nativeHurtScope, f);
            if (nativeHurtScope.interaction == null) {
                FoodWeaponSourceLedger.closeNativeHurtScope(nativeHurtScope);
                NativeHurtScope nativeHurtScope3 = null;
                return nativeHurtScope3;
            }
            bl = true;
            NativeHurtScope nativeHurtScope4 = nativeHurtScope;
            return nativeHurtScope4;
        }
        finally {
            if (!bl) {
                FoodWeaponSourceLedger.revokeUnreturnedHurt(nativeHurtScope);
            }
        }
    }

    private static boolean ironActualSource(NativeIronCallbackToken nativeIronCallbackToken, DamageSource damageSource) {
        if (!damageSource.getClass().getName().equals("io.redspace.ironsspellbooks.damage.SpellDamageSource") || damageSource.getClass().getClassLoader() != nativeIronCallbackToken.payment.cast.spell.getClass().getClassLoader()) {
            return false;
        }
        try {
            return damageSource.getClass().getMethod("spell", new Class[0]).invoke(damageSource, new Object[0]) == nativeIronCallbackToken.payment.cast.spell;
        }
        catch (ReflectiveOperationException | RuntimeException exception) {
            return false;
        }
    }

    private static boolean ironActualHurtCaller(NativeIronCallbackToken nativeIronCallbackToken) {
        return NATIVE_CALLER.walk(stream -> {
            List<StackWalker.StackFrame> list = stream.filter(stackFrame -> stackFrame.getDeclaringClass() != FoodWeaponSourceLedger.class).limit(32L).toList();
            if (!(list.size() >= 3 && list.get(0).getClassName().equals("io.redspace.ironsspellbooks.damage.DamageSources") && list.get(0).getDeclaringClass() == list.get(1).getDeclaringClass() && list.get(0).getDeclaringClass().getClassLoader() == nativeIronCallbackToken.payment.cast.spell.getClass().getClassLoader() && list.get(1).getMethodName().equals("applyDamage") && list.get(1).getDescriptor().equals("(Lnet/minecraft/world/entity/Entity;FLnet/minecraft/world/damagesource/DamageSource;)Z"))) {
                return false;
            }
            StackWalker.StackFrame stackFrame2 = list.get(0);
            boolean bl = stackFrame2.getMethodName().equals("ftboss$v10$ironLivingHurt") && stackFrame2.getDescriptor().equals("(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/damagesource/DamageSource;F)Z");
            boolean bl2 = stackFrame2.getMethodName().equals("ftboss$v10$ironEntityHurt") && stackFrame2.getDescriptor().equals("(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/damagesource/DamageSource;F)Z");
            StackWalker.StackFrame stackFrame3 = list.get(2);
            return (bl || bl2) && stackFrame3.getMethodName().equals("onCast") && stackFrame3.getDeclaringClass().isInstance(nativeIronCallbackToken.payment.cast.spell) && stackFrame3.getDescriptor().equals("(Lnet/minecraft/world/level/Level;ILnet/minecraft/world/entity/LivingEntity;Lio/redspace/ironsspellbooks/api/spells/CastSource;Lio/redspace/ironsspellbooks/api/magic/MagicData;)V");
        });
    }

    public static synchronized void cancelNativeIngress(NativeIngressToken nativeIngressToken) {
        NativeWearToken nativeWearToken;
        if (nativeIngressToken == null) {
            return;
        }
        nativeIngressToken.expectation.closed = true;
        nativeIngressToken.expectation.cursor.close();
        nativeIngressToken.invalid = true;
        NativeHurtScope nativeHurtScope = PRIVATE_NATIVE_HURT.get();
        if (nativeHurtScope != null && (nativeHurtScope.earlyIngress == nativeIngressToken || nativeHurtScope.receipt != null && nativeHurtScope.receipt.ingress == nativeIngressToken)) {
            FoodWeaponSourceLedger.closeNativeHurtScope(nativeHurtScope);
        }
        if (PRIVATE_INGRESS.get() == nativeIngressToken) {
            PRIVATE_INGRESS.remove();
        }
        if ((nativeWearToken = PRIVATE_INGRESS_WEAR.get()) != null && nativeWearToken.ingress == nativeIngressToken) {
            PRIVATE_INGRESS_WEAR.remove();
        }
    }

    private static boolean authorizedWear(NativeReceipt nativeReceipt, ItemStack itemStack, int n) {
        return FoodWeaponSourceLedger.authorizedWear(nativeReceipt.operation.player, nativeReceipt.before, nativeReceipt.rules, itemStack, n, nativeReceipt.writes, nativeReceipt.requestedDamage);
    }

    private static boolean authorizedWear(ServerPlayer serverPlayer, CompoundTag compoundTag, String string, ItemStack itemStack, int n, int n2, int n3) {
        int n4;
        if (itemStack.m_41783_() != null && !FoodWeaponSourceLedger.canonicalInputValid((Tag)itemStack.m_41783_()) || !FoodWeaponSourceLedger.canonicalInputValid((Tag)itemStack.m_41739_(new CompoundTag()))) {
            return false;
        }
        ItemStack itemStack2 = ItemStack.m_41712_((CompoundTag)compoundTag);
        int n5 = itemStack2.m_41773_();
        int n6 = itemStack.m_41773_();
        if (itemStack.m_41613_() != 1 || itemStack2.m_41720_() != itemStack.m_41720_() || itemStack.m_41776_() != itemStack2.m_41776_() || n5 >= itemStack.m_41776_() - 1 || n6 < n5 || n6 >= itemStack.m_41776_() || FoodWeaponLifecycle.isBroken(itemStack) || string.isEmpty() || !string.equals(FoodWeaponSourceLedger.foundationRules(serverPlayer, itemStack))) {
            return false;
        }
        double d = itemStack2.m_41737_("FTBossFourWeaponFoundation").m_128459_("WearCarry");
        FoodWeaponBuildRules.Wear wear = FoodWeaponBuildRules.wear(n, FourWeaponFoundationAdapter.combat(itemStack2).wear(), d);
        if (Double.doubleToLongBits(wear.carry()) != Double.doubleToLongBits(itemStack.m_41737_("FTBossFourWeaponFoundation").m_128459_("WearCarry")) || n6 - n5 > wear.damage() || n2 > 1 || n2 == 0 && n6 != n5 || n2 == 1 && n3 != n6) {
            return false;
        }
        CompoundTag compoundTag2 = compoundTag.m_6426_();
        CompoundTag compoundTag3 = itemStack.m_41739_(new CompoundTag()).m_6426_();
        CompoundTag compoundTag4 = compoundTag2.m_128469_("tag");
        CompoundTag compoundTag5 = compoundTag3.m_128469_("tag");
        CompoundTag compoundTag6 = compoundTag4.m_128469_(IDENTITY_ROOT);
        CompoundTag compoundTag7 = compoundTag5.m_128469_(IDENTITY_ROOT);
        int n7 = FoodWeaponLifecycleRules.pendingLosses(compoundTag6.m_128451_("WearStep"), n6, itemStack.m_41776_());
        int n8 = n4 = n7 > 0 ? FoodWeaponLifecycleRules.wearStep(n6, itemStack.m_41776_()) : compoundTag6.m_128451_("WearStep");
        if (compoundTag7.m_128451_("WearStep") != n4) {
            return false;
        }
        ListTag listTag = compoundTag4.m_128469_("FTBossFoodEvolution").m_128437_("Sockets", 10);
        ListTag listTag2 = compoundTag5.m_128469_("FTBossFoodEvolution").m_128437_("Sockets", 10);
        int n9 = Math.min(n7, listTag.size());
        ListTag listTag3 = listTag.m_6426_();
        for (int i = 0; i < n9; ++i) {
            listTag3.remove(listTag3.size() - 1);
        }
        if (!listTag3.equals((Object)listTag2) || compoundTag7.m_128451_("PendingMaterials") != compoundTag6.m_128451_("PendingMaterials") && compoundTag7.m_128451_("PendingMaterials") != 0 || compoundTag7.m_128451_("PendingSockets") != compoundTag6.m_128451_("PendingSockets") + n9 && compoundTag7.m_128451_("PendingSockets") != 0) {
            return false;
        }
        for (CompoundTag compoundTag8 : List.of(compoundTag4, compoundTag5)) {
            compoundTag8.m_128473_("Damage");
            compoundTag8.m_128469_("FTBossFourWeaponFoundation").m_128473_("WearCarry");
            CompoundTag compoundTag9 = compoundTag8.m_128469_(IDENTITY_ROOT);
            for (String string2 : List.of("WearStep", "PendingMaterials", "PendingSockets")) {
                compoundTag9.m_128473_(string2);
            }
            compoundTag8.m_128469_("FTBossFoodEvolution").m_128473_("Sockets");
        }
        return compoundTag2.equals((Object)compoundTag3);
    }

    public static String boundProfile(ItemStack itemStack) {
        String string = FoodWeaponEvolution.repairProfileId(itemStack);
        return WeaponEvolutionSkillPool.Aspect.fromId(string) == null ? "" : string;
    }

    public static String materialFingerprint(ItemStack itemStack) {
        return FoodWeaponMaterialFingerprint.create(itemStack);
    }

    private static String foundationRules(ServerPlayer serverPlayer, ItemStack itemStack) {
        if (!FourWeaponFoundationAdapter.applies(itemStack)) {
            return "";
        }
        if (FoodWeaponMaterialFingerprint.foundationSnapshot(itemStack).m_128456_()) {
            return "";
        }
        try {
            String string = new FoundationApprovedRules().fingerprint(serverPlayer, itemStack);
            return string == null || string.isEmpty() || string.length() > 1024 ? "" : string;
        }
        catch (IllegalArgumentException | LinkageError throwable) {
            return "";
        }
    }

    private static boolean foundationContinuous(CompoundTag compoundTag, ServerPlayer serverPlayer, ItemStack itemStack) {
        if (!FourWeaponFoundationAdapter.applies(itemStack)) {
            return !compoundTag.m_128441_("FoundationSnapshot") && !compoundTag.m_128441_("FoundationApprovedRules");
        }
        CompoundTag compoundTag2 = FoodWeaponMaterialFingerprint.foundationSnapshot(itemStack);
        CompoundTag compoundTag3 = compoundTag.m_128469_("FoundationSnapshot");
        String string = FoodWeaponSourceLedger.foundationRules(serverPlayer, itemStack);
        return !compoundTag2.m_128456_() && !string.isEmpty() && string.equals(compoundTag.m_128461_("FoundationApprovedRules")) && compoundTag3.m_128471_("Valid") && !compoundTag3.m_128471_("BlocksUse") && compoundTag2.m_128461_("Composition").equals(compoundTag3.m_128461_("Composition")) && compoundTag2.m_128461_("Rules").equals(compoundTag3.m_128461_("Rules")) && compoundTag2.m_128461_("ItemId").equals(compoundTag3.m_128461_("ItemId")) && compoundTag2.m_128461_("Profile").equals(compoundTag3.m_128461_("Profile")) && compoundTag2.m_128461_("Family").equals(compoundTag3.m_128461_("Family")) && compoundTag2.m_128451_("Level") == compoundTag3.m_128451_("Level");
    }

    private static boolean nativeFoundation(ItemStack itemStack) {
        return FourWeaponFoundationAdapter.applies(itemStack) && FourWeaponFoundationAdapter.read(itemStack).valid() && FourWeaponFoundationAdapter.read(itemStack).level() == 0 && FoodWeaponSourceLedger.boundProfile(itemStack).isEmpty();
    }

    public static String socketFingerprint(ItemStack itemStack, String string) {
        CompoundTag compoundTag = new CompoundTag();
        CompoundTag compoundTag2 = itemStack.m_41737_("FTBossFoodEvolution");
        ListTag listTag = new ListTag();
        if (compoundTag2 != null) {
            for (Tag tag : compoundTag2.m_128437_("Sockets", 10)) {
                CompoundTag compoundTag3 = (CompoundTag)tag;
                CompoundTag compoundTag4 = new CompoundTag();
                compoundTag4.m_128359_("Type", compoundTag3.m_128461_("Type"));
                compoundTag4.m_128359_("Item", compoundTag3.m_128461_("Item"));
                if (compoundTag3.m_128425_("Tier", 3)) {
                    compoundTag4.m_128405_("Tier", compoundTag3.m_128451_("Tier"));
                }
                listTag.add((Object)compoundTag4);
            }
        }
        compoundTag.m_128365_("Sockets", (Tag)listTag);
        compoundTag.m_128405_("Count", listTag.size());
        compoundTag.m_128359_("Profile", FoodWeaponSourceLedger.boundProfile(itemStack));
        compoundTag.m_128359_("Family", string);
        return FoodWeaponSourceLedger.fingerprint((Tag)compoundTag);
    }

    private static String fingerprint(Tag tag) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(FoodWeaponSourceLedger.canonical(tag)));
        }
        catch (IllegalArgumentException illegalArgumentException) {
            return "";
        }
        catch (NoSuchAlgorithmException noSuchAlgorithmException) {
            throw new IllegalStateException(noSuchAlgorithmException);
        }
    }

    public static boolean canonicalInputValid(Tag tag) {
        try {
            FoodWeaponSourceLedger.canonical(tag);
            return true;
        }
        catch (IllegalArgumentException illegalArgumentException) {
            return false;
        }
    }

    private static byte[] canonical(Tag tag) {
        byte[] byArray;
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        DataOutputStream dataOutputStream = new DataOutputStream(byteArrayOutputStream);
        try {
            dataOutputStream.writeInt(1179932499);
            dataOutputStream.writeInt(3);
            FoodWeaponSourceLedger.appendCanonical(tag, dataOutputStream, 0, new int[]{0});
            byArray = byteArrayOutputStream.toByteArray();
        }
        catch (Throwable throwable) {
            try {
                try {
                    dataOutputStream.close();
                }
                catch (Throwable throwable2) {
                    throwable.addSuppressed(throwable2);
                }
                throw throwable;
            }
            catch (IOException iOException) {
                throw new IllegalStateException(iOException);
            }
        }
        dataOutputStream.close();
        return byArray;
    }

    private static void canonicalString(String string, DataOutputStream dataOutputStream) throws IOException {
        ByteBuffer byteBuffer;
        if (string.length() > 0x100000) {
            throw new IllegalArgumentException("Owner string budget exceeded");
        }
        try {
            byteBuffer = StandardCharsets.UTF_8.newEncoder().onMalformedInput(CodingErrorAction.REPORT).onUnmappableCharacter(CodingErrorAction.REPORT).encode(CharBuffer.wrap(string));
        }
        catch (CharacterCodingException characterCodingException) {
            throw new IllegalArgumentException("Owner malformed UTF-16", characterCodingException);
        }
        if (byteBuffer.remaining() > 0x100000 - dataOutputStream.size() - 4) {
            throw new IllegalArgumentException("Owner byte budget exceeded");
        }
        dataOutputStream.writeInt(byteBuffer.remaining());
        while (byteBuffer.hasRemaining()) {
            dataOutputStream.writeByte(byteBuffer.get());
        }
    }

    private static void appendCanonical(Tag tag, DataOutputStream dataOutputStream, int n, int[] nArray) throws IOException {
        block27: {
            block30: {
                block29: {
                    block28: {
                        block26: {
                            if (tag == null || n > 64 || (nArray[0] = nArray[0] + 1) > 65536) {
                                throw new IllegalArgumentException("Owner NBT complexity exceeded");
                            }
                            dataOutputStream.writeByte(tag.m_7060_());
                            if (!(tag instanceof CompoundTag)) break block26;
                            CompoundTag compoundTag = (CompoundTag)tag;
                            if (compoundTag.m_128440_() > 65536 - nArray[0]) {
                                throw new IllegalArgumentException("Owner compound budget exceeded");
                            }
                            dataOutputStream.writeInt(compoundTag.m_128440_());
                            for (String string : new TreeSet(compoundTag.m_128431_())) {
                                FoodWeaponSourceLedger.canonicalString(string, dataOutputStream);
                                FoodWeaponSourceLedger.appendCanonical(compoundTag.m_128423_(string), dataOutputStream, n + 1, nArray);
                            }
                            break block27;
                        }
                        if (!(tag instanceof ListTag)) break block28;
                        ListTag listTag = (ListTag)tag;
                        if (listTag.size() > 65536 - nArray[0]) {
                            throw new IllegalArgumentException("Owner list budget exceeded");
                        }
                        dataOutputStream.writeByte(listTag.m_7264_());
                        dataOutputStream.writeInt(listTag.size());
                        for (Tag tag2 : listTag) {
                            FoodWeaponSourceLedger.appendCanonical(tag2, dataOutputStream, n + 1, nArray);
                        }
                        break block27;
                    }
                    if (!(tag instanceof StringTag)) break block29;
                    StringTag stringTag = (StringTag)tag;
                    FoodWeaponSourceLedger.canonicalString(stringTag.m_7916_(), dataOutputStream);
                    break block27;
                }
                if (!(tag instanceof NumericTag)) break block30;
                NumericTag numericTag = (NumericTag)tag;
                switch (tag.m_7060_()) {
                    case 1: {
                        dataOutputStream.writeByte(numericTag.m_7063_());
                        break block27;
                    }
                    case 2: {
                        dataOutputStream.writeShort(numericTag.m_7053_());
                        break block27;
                    }
                    case 3: {
                        dataOutputStream.writeInt(numericTag.m_7047_());
                        break block27;
                    }
                    case 4: {
                        dataOutputStream.writeLong(numericTag.m_7046_());
                        break block27;
                    }
                    case 5: {
                        dataOutputStream.writeInt(Float.floatToRawIntBits(numericTag.m_7057_()));
                        break block27;
                    }
                    case 6: {
                        dataOutputStream.writeLong(Double.doubleToRawLongBits(numericTag.m_7061_()));
                        break block27;
                    }
                    default: {
                        throw new IllegalArgumentException("Owner unknown numeric tag");
                    }
                }
            }
            if (tag instanceof ByteArrayTag) {
                ByteArrayTag byteArrayTag = (ByteArrayTag)tag;
                byte[] byArray = byteArrayTag.m_128227_();
                if (byArray.length > 0x100000 - dataOutputStream.size() - 4) {
                    throw new IllegalArgumentException("Owner array budget exceeded");
                }
                dataOutputStream.writeInt(byArray.length);
                dataOutputStream.write(byArray);
            } else if (tag instanceof IntArrayTag) {
                IntArrayTag intArrayTag = (IntArrayTag)tag;
                int[] nArray2 = intArrayTag.m_128648_();
                if (nArray2.length > (0x100000 - dataOutputStream.size() - 4) / 4) {
                    throw new IllegalArgumentException("Owner array budget exceeded");
                }
                dataOutputStream.writeInt(nArray2.length);
                for (int n2 : nArray2) {
                    dataOutputStream.writeInt(n2);
                }
            } else if (tag instanceof LongArrayTag) {
                LongArrayTag longArrayTag = (LongArrayTag)tag;
                long[] lArray = longArrayTag.m_128851_();
                if (lArray.length > (0x100000 - dataOutputStream.size() - 4) / 8) {
                    throw new IllegalArgumentException("Owner array budget exceeded");
                }
                dataOutputStream.writeInt(lArray.length);
                for (long l : lArray) {
                    dataOutputStream.writeLong(l);
                }
            } else if (tag.m_7060_() != 0) {
                throw new IllegalArgumentException("Owner unknown tag type");
            }
        }
        if (dataOutputStream.size() > 0x100000) {
            throw new IllegalArgumentException("Owner byte budget exceeded");
        }
    }

    public static CompoundTag decorate(ServerPlayer serverPlayer, ItemStack itemStack, CompoundTag compoundTag, InteractionHand interactionHand) {
        boolean bl;
        if (compoundTag != null && compoundTag.m_128441_("DamagePurpose") ? !(bl = NATIVE_CALLER.walk(stream -> stream.filter(stackFrame -> stackFrame.getDeclaringClass() != FoodWeaponSourceLedger.class).findFirst().map(stackFrame -> stackFrame.getDeclaringClass() == FoodWeaponDamageScaling.class && stackFrame.getMethodName().equals("captureGeneric")).orElse(false)).booleanValue()) || !"GENERIC_J".equals(compoundTag.m_128461_("DamagePurpose")) || !Double.isFinite(FoodWeaponDamageScaling.residual(compoundTag)) : compoundTag != null && compoundTag.m_128441_("DamageAccountingVersion")) {
            return new CompoundTag();
        }
        return FoodWeaponSourceLedger.decorateNative(serverPlayer, itemStack, compoundTag, interactionHand, null);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static CompoundTag decorateNative(ServerPlayer serverPlayer, ItemStack itemStack, CompoundTag compoundTag, InteractionHand interactionHand, NativeReceipt nativeReceipt) {
        long l;
        if (serverPlayer == null || itemStack == null || !OwnerFullRepairGuard.permits(itemStack) || compoundTag == null || compoundTag.m_128456_() || interactionHand == null || itemStack.m_41613_() != 1 || !FoodWeaponSourceLedger.canonicalInputValid((Tag)compoundTag) || itemStack.m_41783_() != null && !FoodWeaponSourceLedger.canonicalInputValid((Tag)itemStack.m_41783_()) || FoodWeaponLifecycle.weaponId(itemStack) == null) {
            return new CompoundTag();
        }
        CompoundTag compoundTag2 = itemStack.m_41739_(new CompoundTag());
        if (!FoodWeaponSourceLedger.canonicalInputValid((Tag)compoundTag2)) {
            return new CompoundTag();
        }
        String string = FoodWeaponSourceLedger.materialFingerprint(itemStack);
        if (string.isEmpty()) {
            return new CompoundTag();
        }
        compoundTag = compoundTag.m_6426_();
        compoundTag.m_128473_("FoundationSnapshot");
        compoundTag.m_128473_("FoundationApprovedRules");
        if (FourWeaponFoundationAdapter.applies(itemStack)) {
            CompoundTag compoundTag3 = FoodWeaponMaterialFingerprint.foundationSnapshot(itemStack);
            String string2 = FoodWeaponSourceLedger.foundationRules(serverPlayer, itemStack);
            if (compoundTag3.m_128456_() || string2.isEmpty() || !FoodWeaponFamilyBuild.kind(itemStack).name().equals(compoundTag.m_128461_("Family"))) {
                return new CompoundTag();
            }
            compoundTag.m_128365_("FoundationSnapshot", (Tag)compoundTag3.m_6426_());
            compoundTag.m_128359_("FoundationApprovedRules", string2);
            compoundTag.m_128365_("Weapon", (Tag)compoundTag2.m_6426_());
        }
        if ((l = FoodWeaponSourceLedger.clock(serverPlayer)) < 0L || l > 9223372036854774607L) {
            return new CompoundTag();
        }
        compoundTag.m_128473_("NativeProfile");
        compoundTag.m_128473_("Hand");
        compoundTag.m_128473_("TridentLaunchAuthorized");
        compoundTag.m_128473_("PreWearEligible");
        compoundTag.m_128473_("NativeEntry");
        compoundTag.m_128473_("NativeReceiptId");
        compoundTag.m_128473_("NativeEntityId");
        compoundTag.m_128405_("SnapshotVersion", 3);
        compoundTag.m_128362_("Owner", serverPlayer.m_20148_());
        compoundTag.m_128362_("AttackId", UUID.randomUUID());
        compoundTag.m_128362_("WeaponId", FoodWeaponLifecycle.weaponId(itemStack));
        compoundTag.m_128362_("ServerEpoch", new UUID(0L, 0L));
        compoundTag.m_128362_("OwnerEpoch", new UUID(0L, 0L));
        compoundTag.m_128359_("OriginalHand", interactionHand.name());
        compoundTag.m_128359_("Dimension", serverPlayer.m_9236_().m_46472_().m_135782_().toString());
        compoundTag.m_128356_("CapturedAt", l);
        compoundTag.m_128356_("ExpiresAt", l + 1200L);
        compoundTag.m_128359_("BoundProfile", FoodWeaponSourceLedger.boundProfile(itemStack));
        compoundTag.m_128359_("RepairProfile", FoodWeaponLifecycle.repairProfile(itemStack));
        compoundTag.m_128359_("SourceKind", compoundTag.m_128461_("Family"));
        if (nativeReceipt != null) {
            compoundTag.m_128359_("NativeEntry", nativeReceipt.operation.kind.name());
            compoundTag.m_128362_("NativeReceiptId", nativeReceipt.token);
            compoundTag.m_128362_("NativeEntityId", nativeReceipt.entity.m_20148_());
            if (nativeReceipt.operation.kind == NativeKind.TRIDENT) {
                compoundTag.m_128359_("SourceKind", "TRIDENT");
                compoundTag.m_128379_("TridentLaunchAuthorized", true);
            }
        }
        compoundTag.m_128365_("Weapon", (Tag)compoundTag2);
        compoundTag.m_128359_("MaterialFingerprint", string);
        compoundTag.m_128359_("SocketFingerprint", FoodWeaponSourceLedger.socketFingerprint(itemStack, compoundTag.m_128461_("Family")));
        compoundTag.m_128359_("ServerSignature", "0".repeat(64));
        if (!FoodWeaponSourceLedger.canonicalInputValid((Tag)compoundTag)) {
            return new CompoundTag();
        }
        ServerState serverState = FoodWeaponSourceLedger.state(FoodWeaponSourceLedger.server(serverPlayer));
        Class<FoodWeaponSourceLedger> clazz = FoodWeaponSourceLedger.class;
        synchronized (FoodWeaponSourceLedger.class) {
            if (SERVERS.get(FoodWeaponSourceLedger.server(serverPlayer)) != serverState || serverState.closed || l < serverState.latestTick) {
                // ** MonitorExit[var11_11] (shouldn't be in output)
                return new CompoundTag();
            }
            UUID uUID = FoodWeaponSourceLedger.ownerEpoch(serverState, serverPlayer.m_20148_());
            if (uUID == null) {
                uUID = UUID.randomUUID();
            }
            compoundTag.m_128362_("ServerEpoch", serverState.epoch);
            compoundTag.m_128362_("OwnerEpoch", uUID);
            // ** MonitorExit[var11_11] (shouldn't be in output)
            if (!OwnerRepairGenerationAuthority.stamp(itemStack, compoundTag, serverState.revocations)) {
                return new CompoundTag();
            }
            clazz = FoodWeaponSourceLedger.class;
            synchronized (FoodWeaponSourceLedger.class) {
                UUID uUID2 = FoodWeaponSourceLedger.ownerEpoch(serverState, serverPlayer.m_20148_());
                if (SERVERS.get(FoodWeaponSourceLedger.server(serverPlayer)) != serverState || serverState.closed || l < serverState.latestTick || uUID2 != null && !uUID2.equals(uUID)) {
                    // ** MonitorExit[var11_11] (shouldn't be in output)
                    return new CompoundTag();
                }
                String string3 = FoodWeaponSourceLedger.signature(compoundTag, serverState);
                if (string3.isEmpty()) {
                    // ** MonitorExit[var11_11] (shouldn't be in output)
                    return new CompoundTag();
                }
                compoundTag.m_128359_("ServerSignature", string3);
                OwnerState ownerState = serverState.ownerEpochs.get(serverPlayer.m_20148_());
                if (ownerState == null) {
                    serverState.ownerEpochs.put(serverPlayer.m_20148_(), new OwnerState(uUID));
                }
                // ** MonitorExit[var11_11] (shouldn't be in output)
                if (!OwnerFullRepairGuard.bind(itemStack, compoundTag.m_128342_("WeaponId"))) {
                    return new CompoundTag();
                }
                clazz = FoodWeaponSourceLedger.class;
                synchronized (FoodWeaponSourceLedger.class) {
                    if (SERVERS.get(FoodWeaponSourceLedger.server(serverPlayer)) != serverState || serverState.closed || l < serverState.latestTick || !uUID.equals(FoodWeaponSourceLedger.ownerEpoch(serverState, serverPlayer.m_20148_()))) {
                        // ** MonitorExit[var11_11] (shouldn't be in output)
                        return new CompoundTag();
                    }
                    serverState.latestTick = l;
                    // ** MonitorExit[var11_11] (shouldn't be in output)
                    return compoundTag;
                }
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static boolean valid(CompoundTag compoundTag, ServerPlayer serverPlayer) {
        if (!FoodWeaponSourceLedger.validReadOnly(compoundTag, serverPlayer)) {
            return false;
        }
        Class<FoodWeaponSourceLedger> clazz = FoodWeaponSourceLedger.class;
        synchronized (FoodWeaponSourceLedger.class) {
            ServerState serverState = SERVERS.get(FoodWeaponSourceLedger.server(serverPlayer));
            if (serverState == null || serverState.closed || !serverState.epoch.equals(compoundTag.m_128342_("ServerEpoch")) || !compoundTag.m_128342_("OwnerEpoch").equals(FoodWeaponSourceLedger.ownerEpoch(serverState, serverPlayer.m_20148_()))) {
                // ** MonitorExit[var2_2] (shouldn't be in output)
                return false;
            }
            serverState.latestTick = FoodWeaponSourceLedger.clock(serverPlayer);
            // ** MonitorExit[var2_2] (shouldn't be in output)
            return true;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static boolean validReadOnly(CompoundTag compoundTag, ServerPlayer serverPlayer) {
        if (!(serverPlayer != null && compoundTag != null && compoundTag.m_128451_("SnapshotVersion") == 3 && FoodWeaponSourceLedger.canonicalInputValid((Tag)compoundTag) && compoundTag.m_128461_("ServerSignature").matches("[0-9a-f]{64}") && compoundTag.m_128403_("Owner") && compoundTag.m_128342_("Owner").equals(serverPlayer.m_20148_()) && compoundTag.m_128403_("AttackId") && compoundTag.m_128403_("WeaponId") && compoundTag.m_128403_("ServerEpoch") && compoundTag.m_128403_("OwnerEpoch") && !compoundTag.m_128461_("MaterialFingerprint").isEmpty() && compoundTag.m_128425_("Weapon", 10) && compoundTag.m_128425_("CapturedAt", 4) && compoundTag.m_128425_("ExpiresAt", 4) && compoundTag.m_128461_("Dimension").equals(serverPlayer.m_9236_().m_46472_().m_135782_().toString()) && (compoundTag.m_128461_("OriginalHand").equals(InteractionHand.MAIN_HAND.name()) || compoundTag.m_128461_("OriginalHand").equals(InteractionHand.OFF_HAND.name())) && !compoundTag.m_128441_("Hand") && !compoundTag.m_128441_("NativeProfile"))) {
            return false;
        }
        Object object = FoodWeaponSourceLedger.class;
        synchronized (FoodWeaponSourceLedger.class) {
            boolean bl;
            ServerState serverState = SERVERS.get(FoodWeaponSourceLedger.server(serverPlayer));
            // ** MonitorExit[var3_2] (shouldn't be in output)
            object = OwnerFullRepairGuard.lease(compoundTag);
            if (serverState == null || !OwnerRepairGenerationAuthority.snapshotAllowed(compoundTag)) {
                return false;
            }
            if (SERVERS.get(FoodWeaponSourceLedger.server(serverPlayer)) != serverState || serverState.closed) {
                return false;
            }
            long l = FoodWeaponSourceLedger.clock(serverPlayer);
            long l2 = compoundTag.m_128454_("CapturedAt");
            long l3 = compoundTag.m_128454_("ExpiresAt");
            if (!compoundTag.m_128342_("ServerEpoch").equals(serverState.epoch) || !compoundTag.m_128342_("OwnerEpoch").equals(FoodWeaponSourceLedger.ownerEpoch(serverState, serverPlayer.m_20148_())) || l < serverState.latestTick || l2 < 0L || l2 > 9223372036854774607L || l3 != l2 + 1200L || l < l2 || l >= l3) {
                return false;
            }
            String string = compoundTag.m_128461_("Family");
            String string2 = compoundTag.m_128461_("SourceKind");
            boolean bl2 = bl = string2.equals("TRIDENT") && string.equals("MELEE") && compoundTag.m_128471_("TridentLaunchAuthorized");
            if (!(bl || string2.equals(string) && (string.equals("BOW") || string.equals("STAFF") || string.equals("MELEE")))) {
                return false;
            }
            String string3 = FoodWeaponSourceLedger.signature(compoundTag, serverState);
            if (string3.isEmpty() || !MessageDigest.isEqual(compoundTag.m_128461_("ServerSignature").getBytes(StandardCharsets.US_ASCII), string3.getBytes(StandardCharsets.US_ASCII))) {
                return false;
            }
            if (compoundTag.m_128441_("DamagePurpose") && !FoodWeaponSourceLedger.damageAuthorized(compoundTag)) {
                return false;
            }
            ItemStack itemStack = ItemStack.m_41712_((CompoundTag)compoundTag.m_128469_("Weapon"));
            if (!(!itemStack.m_41619_() && itemStack.m_41613_() == 1 && compoundTag.m_128342_("WeaponId").equals(FoodWeaponLifecycle.weaponId(itemStack)) && FoodWeaponSourceLedger.boundProfile(itemStack).equals(compoundTag.m_128461_("BoundProfile")) && FoodWeaponLifecycle.repairProfile(itemStack).equals(compoundTag.m_128461_("RepairProfile")) && FoodWeaponSourceLedger.materialFingerprint(itemStack).equals(compoundTag.m_128461_("MaterialFingerprint")) && FoodWeaponSourceLedger.socketFingerprint(itemStack, string).equals(compoundTag.m_128461_("SocketFingerprint")) && FoodWeaponSourceLedger.foundationContinuous(compoundTag, serverPlayer, itemStack))) {
                return false;
            }
            if (bl && (!FoodWeaponSourceLedger.isTrident(itemStack) || compoundTag.m_128461_("BoundProfile").isEmpty() && !FoodWeaponSourceLedger.nativeFoundation(itemStack))) {
                return false;
            }
            if (!OwnerRepairGenerationAuthority.snapshotAllowed(compoundTag)) {
                return false;
            }
            Class<FoodWeaponSourceLedger> clazz = FoodWeaponSourceLedger.class;
            synchronized (FoodWeaponSourceLedger.class) {
                // ** MonitorExit[var15_13] (shouldn't be in output)
                return SERVERS.get(FoodWeaponSourceLedger.server(serverPlayer)) == serverState && !serverState.closed && l == FoodWeaponSourceLedger.clock(serverPlayer) && l >= serverState.latestTick && compoundTag.m_128342_("ServerEpoch").equals(serverState.epoch) && compoundTag.m_128342_("OwnerEpoch").equals(FoodWeaponSourceLedger.ownerEpoch(serverState, serverPlayer.m_20148_())) && OwnerRepairGenerationAuthority.snapshotIdentity(compoundTag, (OwnerLineageRevocationStore.Lease)object);
            }
        }
    }

    private static String signature(CompoundTag compoundTag, ServerState serverState) {
        try {
            FoodWeaponSourceLedger.canonical((Tag)compoundTag);
            CompoundTag compoundTag2 = compoundTag.m_6426_();
            compoundTag2.m_128473_("ServerSignature");
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(serverState.signingKey, "HmacSHA256"));
            return HexFormat.of().formatHex(mac.doFinal(FoodWeaponSourceLedger.canonical((Tag)compoundTag2)));
        }
        catch (IllegalArgumentException illegalArgumentException) {
            return "";
        }
        catch (GeneralSecurityException generalSecurityException) {
            throw new IllegalStateException(generalSecurityException);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static CompoundTag resolve(DamageSource damageSource, ServerPlayer serverPlayer) {
        CompoundTag compoundTag = FoodWeaponSourceLedger.resolveReadOnly(damageSource, serverPlayer);
        Class<FoodWeaponSourceLedger> clazz = FoodWeaponSourceLedger.class;
        synchronized (FoodWeaponSourceLedger.class) {
            ServerState serverState;
            ServerState serverState2 = serverState = serverPlayer == null ? null : SERVERS.get(FoodWeaponSourceLedger.server(serverPlayer));
            if (!compoundTag.m_128456_() && serverState != null && !serverState.closed && compoundTag.m_128342_("ServerEpoch").equals(serverState.epoch)) {
                serverState.latestTick = FoodWeaponSourceLedger.clock(serverPlayer);
            }
            // ** MonitorExit[var3_3] (shouldn't be in output)
            return compoundTag;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     * Converted monitor instructions to comments
     * Lifted jumps to return sites
     */
    public static CompoundTag resolveReadOnly(DamageSource damageSource, ServerPlayer serverPlayer) {
        Object object;
        PrimaryReadWindow primaryReadWindow = FoodWeaponSourceLedger.beginPrimaryRead(damageSource, serverPlayer);
        boolean bl = false;
        try {
            CompoundTag compoundTag = FoodWeaponSourceLedger.resolveSourceUncached(damageSource, serverPlayer);
            if (primaryReadWindow != null && !compoundTag.m_128456_() && FoodWeaponSourceLedger.attemptSourceValid(compoundTag, serverPlayer) && FoodWeaponSourceLedger.interactionRootLive(primaryReadWindow.root, serverPlayer) && FoodWeaponSourceLedger.projectedBindingLive(primaryReadWindow.binding)) {
                object = FoodWeaponSourceLedger.class;
                // MONITORENTER : net.farmingtales.ftboss.item.FoodWeaponSourceLedger.class
                if (FoodWeaponSourceLedger.readWindowIdentity(primaryReadWindow)) {
                    bl = primaryReadWindow.root.readCut.accept(primaryReadWindow.transition, compoundTag);
                }
                // MONITOREXIT : object
            }
            object = compoundTag;
            Class<FoodWeaponSourceLedger> clazz = FoodWeaponSourceLedger.class;
        }
        catch (Throwable throwable) {
            Class<FoodWeaponSourceLedger> clazz = FoodWeaponSourceLedger.class;
            // MONITORENTER : net.farmingtales.ftboss.item.FoodWeaponSourceLedger.class
            if (primaryReadWindow != null && !bl && primaryReadWindow.root.readCut.transition == primaryReadWindow.transition) {
                primaryReadWindow.root.readCut.acceptedImage = null;
            }
            // MONITOREXIT : clazz
            throw throwable;
        }
        if (primaryReadWindow != null && !bl && primaryReadWindow.root.readCut.transition == primaryReadWindow.transition) {
            primaryReadWindow.root.readCut.acceptedImage = null;
        }
        // MONITOREXIT : clazz
        return object;
    }

    private static CompoundTag resolveSourceUncached(DamageSource damageSource, ServerPlayer serverPlayer) {
        NativePaidWeaponCarrier nativePaidWeaponCarrier;
        Projectile projectile;
        block20: {
            ThrownTrident thrownTrident;
            Entity entity;
            block22: {
                block21: {
                    NativePaidWeapon nativePaidWeapon;
                    if (damageSource == null || serverPlayer == null || !FoodWeaponRanged.allowsWeaponEffects(damageSource)) {
                        return new CompoundTag();
                    }
                    NativeHurtScope nativeHurtScope = PRIVATE_NATIVE_HURT.get();
                    if (nativeHurtScope != null && nativeHurtScope.source == damageSource && nativeHurtScope.player == serverPlayer) {
                        return FoodWeaponSourceLedger.nativeHurtLive(nativeHurtScope) && FoodWeaponSourceLedger.validReadOnly(nativeHurtScope.snapshot, serverPlayer) ? nativeHurtScope.snapshot.m_6426_() : new CompoundTag();
                    }
                    if (damageSource.m_7640_() == serverPlayer) {
                        if (damageSource.m_7639_() != serverPlayer || !damageSource.m_276093_(FoodWeaponDamageSources.PRIMARY)) {
                            return new CompoundTag();
                        }
                        CompoundTag compoundTag = FoodWeaponRanged.scopedSnapshotReadOnly(damageSource, serverPlayer);
                        return FoodWeaponSourceLedger.validReadOnly(compoundTag, serverPlayer) && FoodWeaponSourceLedger.damageAuthorized(compoundTag) && "GENERIC_J".equals(compoundTag.m_128461_("DamagePurpose")) ? compoundTag : new CompoundTag();
                    }
                    entity = damageSource.m_7640_();
                    if (!(entity instanceof Projectile) || (projectile = (Projectile)entity).m_19749_() != serverPlayer) {
                        return new CompoundTag();
                    }
                    if (projectile instanceof NativePaidWeaponCarrier) {
                        nativePaidWeaponCarrier = (NativePaidWeaponCarrier)projectile;
                        nativePaidWeapon = nativePaidWeaponCarrier.ftboss$originalOwnerPaidWeapon();
                    } else {
                        nativePaidWeapon = null;
                    }
                    entity = nativePaidWeapon;
                    Object object = nativePaidWeaponCarrier = entity != null && entity.material != null ? FoodWeaponSourceLedger.nativeMaterialPaidSnapshot((NativePaidWeapon)entity, projectile, serverPlayer) : projectile.getPersistentData().m_128469_("FTBossWeaponMaterials");
                    if (!(FoodWeaponSourceLedger.validReadOnly((CompoundTag)nativePaidWeaponCarrier, serverPlayer) && FoodWeaponSourceLedger.damageAuthorized((CompoundTag)nativePaidWeaponCarrier) && NATIVE_PURPOSE.equals(nativePaidWeaponCarrier.m_128461_("DamagePurpose")) && nativePaidWeaponCarrier.m_128403_("NativeEntityId") && nativePaidWeaponCarrier.m_128342_("NativeEntityId").equals(projectile.m_20148_()))) {
                        return new CompoundTag();
                    }
                    if (!nativePaidWeaponCarrier.m_128461_("SourceKind").equals("TRIDENT")) break block20;
                    if (!(projectile instanceof ThrownTrident)) break block21;
                    thrownTrident = (ThrownTrident)projectile;
                    if (damageSource.m_269533_(DamageTypeTags.f_268524_) && thrownTrident.m_9236_().m_46472_().m_135782_().toString().equals(nativePaidWeaponCarrier.m_128461_("Dimension"))) break block22;
                }
                return new CompoundTag();
            }
            TridentCarrierLineage tridentCarrierLineage = null;
            try {
                TridentCarrierLineage tridentCarrierLineage2;
                OwnerTridentCarrierAccess ownerTridentCarrierAccess;
                if (thrownTrident instanceof OwnerTridentCarrierAccess) {
                    ownerTridentCarrierAccess = (OwnerTridentCarrierAccess)thrownTrident;
                    tridentCarrierLineage2 = ownerTridentCarrierAccess.ftboss$ownerTridentLineage();
                } else {
                    tridentCarrierLineage2 = null;
                }
                tridentCarrierLineage = tridentCarrierLineage2;
                ownerTridentCarrierAccess = FoodWeaponTridentCarrierReader.readOriginalItem(thrownTrident);
                if (FoodWeaponSourceLedger.tridentCarrierLineage(thrownTrident, (ItemStack)ownerTridentCarrierAccess, tridentCarrierLineage) == null || entity != null && tridentCarrierLineage.origin != entity.receipt) {
                    return new CompoundTag();
                }
                if (!(FoodWeaponSourceLedger.isTrident((ItemStack)ownerTridentCarrierAccess) && nativePaidWeaponCarrier.m_128342_("WeaponId").equals(FoodWeaponLifecycle.weaponId((ItemStack)ownerTridentCarrierAccess)) && FoodWeaponSourceLedger.boundProfile((ItemStack)ownerTridentCarrierAccess).equals(nativePaidWeaponCarrier.m_128461_("BoundProfile")) && FoodWeaponLifecycle.repairProfile((ItemStack)ownerTridentCarrierAccess).equals(nativePaidWeaponCarrier.m_128461_("RepairProfile")) && FoodWeaponSourceLedger.socketFingerprint((ItemStack)ownerTridentCarrierAccess, "MELEE").equals(nativePaidWeaponCarrier.m_128461_("SocketFingerprint")) && FoodWeaponSourceLedger.materialFingerprint((ItemStack)ownerTridentCarrierAccess).equals(nativePaidWeaponCarrier.m_128461_("MaterialFingerprint")) && FoodWeaponSourceLedger.tridentRawMatchesExpected((ItemStack)ownerTridentCarrierAccess, nativePaidWeaponCarrier.m_128469_("Weapon")))) {
                    return new CompoundTag();
                }
            }
            catch (Throwable throwable) {
                FoodWeaponSourceLedger.rejectTridentExtra(thrownTrident, tridentCarrierLineage, null, (NativePaidWeapon)entity, null, null, throwable);
                return new CompoundTag();
            }
            return nativePaidWeaponCarrier.m_6426_();
        }
        if (projectile instanceof ThrownTrident || nativePaidWeaponCarrier.m_128461_("Family").equals("MELEE")) {
            return new CompoundTag();
        }
        if (nativePaidWeaponCarrier.m_128461_("Family").equals("BOW") && !damageSource.m_269533_(DamageTypeTags.f_268524_)) {
            return new CompoundTag();
        }
        return nativePaidWeaponCarrier.m_6426_();
    }

    private static boolean terminalDomain(String string) {
        return string.equals("PASSIVE_KILL") || string.equals("MATERIAL_KILL") || string.equals("KILL_FEAST") || string.equals("SOCKET_KILL");
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static boolean claim(CompoundTag compoundTag, ServerPlayer serverPlayer, UUID uUID, EffectDomain effectDomain, boolean bl, long l) {
        if (effectDomain != null && FoodWeaponSourceLedger.terminalDomain(effectDomain.name())) {
            return false;
        }
        if (bl || effectDomain == null || uUID == null || serverPlayer == null || l != FoodWeaponSourceLedger.clock(serverPlayer) || !FoodWeaponSourceLedger.valid(compoundTag, serverPlayer) || !FoodWeaponSourceLedger.damageAuthorized(compoundTag)) {
            return false;
        }
        if (compoundTag.m_128441_("FoundationSnapshot") && (compoundTag.m_128461_("BoundProfile").isEmpty() || compoundTag.m_128469_("FoundationSnapshot").m_128451_("Level") <= 0)) {
            return false;
        }
        OriginalInteractionBinding originalInteractionBinding = FoodWeaponSourceLedger.currentInteraction(compoundTag, serverPlayer);
        if (originalInteractionBinding == null) {
            return false;
        }
        ServerState serverState = FoodWeaponSourceLedger.state(FoodWeaponSourceLedger.server(serverPlayer));
        Class<FoodWeaponSourceLedger> clazz = FoodWeaponSourceLedger.class;
        synchronized (FoodWeaponSourceLedger.class) {
            InteractionRoot interactionRoot = originalInteractionBinding.root;
            OwnerState ownerState = interactionRoot.ownerState;
            if (SERVERS.get(FoodWeaponSourceLedger.server(serverPlayer)) != serverState || serverState.closed || interactionRoot.serverState != serverState || interactionRoot.closed || interactionRoot.retired || interactionRoot.thread != Thread.currentThread() || interactionRoot.current != originalInteractionBinding || originalInteractionBinding.closed || serverState.ownerEpochs.get(serverPlayer.m_20148_()) != ownerState || ownerState.closed || ownerState.current != interactionRoot || !interactionRoot.readCut.matches(compoundTag) || !originalInteractionBinding.snapshot.equals((Object)compoundTag)) {
                // ** MonitorExit[var9_8] (shouldn't be in output)
                return false;
            }
            serverState.claims.entrySet().removeIf(entry -> l >= ((Commit)entry.getValue()).expiresAt());
            Claim claim = new Claim(serverPlayer.m_20148_(), compoundTag.m_128342_("AttackId"), DomainKind.CLAIM, effectDomain.ordinal(), null);
            if (serverState.claims.containsKey(claim) || serverState.claims.size() >= 4096) {
                // ** MonitorExit[var9_8] (shouldn't be in output)
                return false;
            }
            serverState.claims.put(claim, new Commit(compoundTag.m_128454_("ExpiresAt"), 1.0, true));
            // ** MonitorExit[var9_8] (shouldn't be in output)
            return true;
        }
    }

    private static boolean attemptSourceValid(CompoundTag compoundTag, ServerPlayer serverPlayer) {
        return serverPlayer != null && serverPlayer.m_6084_() && !serverPlayer.m_5833_() && FoodWeaponSourceLedger.validReadOnly(compoundTag, serverPlayer) && FoodWeaponSourceLedger.damageAuthorized(compoundTag) && (!"GENERIC_J".equals(compoundTag.m_128461_("DamagePurpose")) || FoodWeaponSourceLedger.originalHandContinuousReadOnly(compoundTag, serverPlayer)) && (!compoundTag.m_128441_("FoundationSnapshot") || !compoundTag.m_128461_("BoundProfile").isEmpty() && compoundTag.m_128469_("FoundationSnapshot").m_128451_("Level") > 0);
    }

    private static Claim attemptKey(CompoundTag compoundTag, ServerPlayer serverPlayer, AttemptDomain attemptDomain) {
        try {
            EffectDomain effectDomain = EffectDomain.valueOf(attemptDomain.name());
            return new Claim(serverPlayer.m_20148_(), compoundTag.m_128342_("AttackId"), DomainKind.CLAIM, effectDomain.ordinal(), null);
        }
        catch (IllegalArgumentException illegalArgumentException) {
            return new Claim(serverPlayer.m_20148_(), compoundTag.m_128342_("AttackId"), DomainKind.ATTEMPT, attemptDomain.ordinal(), null);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static AttemptQuote quoteAttempt(CompoundTag compoundTag, ServerPlayer serverPlayer, AttemptDomain attemptDomain, double d) {
        if (attemptDomain == null || FoodWeaponSourceLedger.terminalDomain(attemptDomain.name()) || !Double.isFinite(d) || d < 0.0 || d > 1.0 || !FoodWeaponSourceLedger.attemptSourceValid(compoundTag, serverPlayer)) {
            return new AttemptQuote(AttemptStatus.INVALID, attemptDomain, d, Double.NaN, false, "", null);
        }
        OriginalInteractionBinding originalInteractionBinding = FoodWeaponSourceLedger.currentInteraction(compoundTag, serverPlayer);
        if (originalInteractionBinding == null) {
            return new AttemptQuote(AttemptStatus.INVALID, attemptDomain, d, Double.NaN, false, "", null);
        }
        CompoundTag compoundTag2 = FoodWeaponSourceLedger.class;
        synchronized (FoodWeaponSourceLedger.class) {
            ServerState serverState = SERVERS.get(FoodWeaponSourceLedger.server(serverPlayer));
            if (serverState == null || serverState.closed) {
                // ** MonitorExit[var8_5] (shouldn't be in output)
                return new AttemptQuote(AttemptStatus.INVALID, attemptDomain, d, Double.NaN, false, "", null);
            }
            Object object = FoodWeaponSourceLedger.attemptKey(compoundTag, serverPlayer, attemptDomain);
            Commit commit = serverState.claims.get(object);
            // ** MonitorExit[var8_5] (shouldn't be in output)
            if (commit != null && Double.compare(d, commit.chance()) != 0) {
                return new AttemptQuote(AttemptStatus.INVALID, attemptDomain, d, Double.NaN, false, "", null);
            }
            compoundTag2 = new CompoundTag();
            compoundTag2.m_128405_("QuoteVersion", 1);
            compoundTag2.m_128359_("Domain", attemptDomain.name());
            compoundTag2.m_128362_("Owner", serverPlayer.m_20148_());
            compoundTag2.m_128362_("AttackId", compoundTag.m_128342_("AttackId"));
            compoundTag2.m_128362_("WeaponId", compoundTag.m_128342_("WeaponId"));
            compoundTag2.m_128362_("ServerEpoch", compoundTag.m_128342_("ServerEpoch"));
            compoundTag2.m_128362_("OwnerEpoch", compoundTag.m_128342_("OwnerEpoch"));
            compoundTag2.m_128359_("ActionSignature", compoundTag.m_128461_("ServerSignature"));
            object = FoodWeaponSourceLedger.signature(compoundTag2, serverState);
            if (((String)object).isEmpty()) {
                return new AttemptQuote(AttemptStatus.INVALID, attemptDomain, d, Double.NaN, false, "", null);
            }
            double d2 = (double)(Long.parseUnsignedLong(((String)object).substring(0, 16), 16) >>> 11) * (double)1.110223E-16f;
            boolean bl = d2 < d;
            compoundTag2.m_128347_("Chance", d);
            String string = FoodWeaponSourceLedger.signature(compoundTag2, serverState);
            AttemptStatus attemptStatus = commit == null ? AttemptStatus.AVAILABLE : (commit.success() ? AttemptStatus.COMMITTED_SUCCESS : AttemptStatus.COMMITTED_FAILURE);
            Class<FoodWeaponSourceLedger> clazz = FoodWeaponSourceLedger.class;
            synchronized (FoodWeaponSourceLedger.class) {
                if (SERVERS.get(FoodWeaponSourceLedger.server(serverPlayer)) != serverState || serverState.closed || originalInteractionBinding.closed || originalInteractionBinding.root.current != originalInteractionBinding || originalInteractionBinding.root.closed || originalInteractionBinding.root.retired || originalInteractionBinding.root.ownerState.closed || originalInteractionBinding.root.ownerState.current != originalInteractionBinding.root) {
                    // ** MonitorExit[var15_14] (shouldn't be in output)
                    return new AttemptQuote(AttemptStatus.INVALID, attemptDomain, d, Double.NaN, false, "", null);
                }
                // ** MonitorExit[var15_14] (shouldn't be in output)
                return new AttemptQuote(attemptStatus, attemptDomain, d, d2, commit == null ? bl : commit.success(), string, originalInteractionBinding);
            }
        }
    }

    public static AttemptQuote quoteAttempt(DamageSource damageSource, ServerPlayer serverPlayer, AttemptDomain attemptDomain, double d) {
        return FoodWeaponSourceLedger.quoteAttempt(FoodWeaponSourceLedger.resolveReadOnly(damageSource, serverPlayer), serverPlayer, attemptDomain, d);
    }

    public static AttemptBatchQuote quoteAttemptBatch(DamageSource damageSource, ServerPlayer serverPlayer, List<AttemptRequest> list) {
        return FoodWeaponSourceLedger.quoteAttemptBatch(FoodWeaponSourceLedger.resolveReadOnly(damageSource, serverPlayer), serverPlayer, list, List.of());
    }

    public static AttemptBatchQuote quoteAttemptBatch(CompoundTag compoundTag, ServerPlayer serverPlayer, List<AttemptRequest> list) {
        return FoodWeaponSourceLedger.quoteAttemptBatch(compoundTag, serverPlayer, list, List.of());
    }

    public static AttemptBatchQuote quoteAttemptBatch(DamageSource damageSource, ServerPlayer serverPlayer, List<AttemptRequest> list, List<TargetRequest> list2) {
        return FoodWeaponSourceLedger.quoteAttemptBatch(FoodWeaponSourceLedger.resolveReadOnly(damageSource, serverPlayer), serverPlayer, list, list2);
    }

    private static AttemptBatchQuote invalidBatch() {
        return new AttemptBatchQuote(AttemptBatchStatus.INVALID, List.of(), List.of(), "", Long.MIN_VALUE, null);
    }

    private static Claim targetKey(CompoundTag compoundTag, ServerPlayer serverPlayer, UUID uUID, TargetEffectDomain targetEffectDomain) {
        return new Claim(serverPlayer.m_20148_(), compoundTag.m_128342_("AttackId"), DomainKind.TARGET, targetEffectDomain.ordinal(), uUID);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static AttemptBatchQuote quoteAttemptBatch(CompoundTag compoundTag, ServerPlayer serverPlayer, List<AttemptRequest> list, List<TargetRequest> list2) {
        Object object;
        if (list == null || list2 == null || list.isEmpty() && list2.isEmpty() || list.size() > AttemptDomain.values().length || list2.size() > 4096 || !FoodWeaponSourceLedger.attemptSourceValid(compoundTag, serverPlayer)) {
            return FoodWeaponSourceLedger.invalidBatch();
        }
        OriginalInteractionBinding originalInteractionBinding = FoodWeaponSourceLedger.currentInteraction(compoundTag, serverPlayer);
        if (originalInteractionBinding == null) {
            return FoodWeaponSourceLedger.invalidBatch();
        }
        ArrayList<AttemptQuote> arrayList = new ArrayList<AttemptQuote>();
        EnumSet<AttemptDomain> enumSet = EnumSet.noneOf(AttemptDomain.class);
        int n = 0;
        for (AttemptRequest object2 : list) {
            if (object2 == null || object2.domain() == null || !enumSet.add(object2.domain())) {
                return FoodWeaponSourceLedger.invalidBatch();
            }
            object = FoodWeaponSourceLedger.quoteAttempt(compoundTag, serverPlayer, object2.domain(), object2.chance());
            if (((AttemptQuote)object).status() == AttemptStatus.INVALID || ((AttemptQuote)object).binding != originalInteractionBinding) {
                return FoodWeaponSourceLedger.invalidBatch();
            }
            if (((AttemptQuote)object).status() == AttemptStatus.AVAILABLE) {
                ++n;
            }
            arrayList.add((AttemptQuote)object);
        }
        object = FoodWeaponSourceLedger.class;
        synchronized (FoodWeaponSourceLedger.class) {
            ServerState serverState = SERVERS.get(FoodWeaponSourceLedger.server(serverPlayer));
            if (serverState == null || serverState.closed) {
                // ** MonitorExit[var10_11] (shouldn't be in output)
                return FoodWeaponSourceLedger.invalidBatch();
            }
            Map<Claim, Commit> map = Map.copyOf(serverState.claims);
            // ** MonitorExit[var10_11] (shouldn't be in output)
            object = new ArrayList<TargetQuote>();
            HashSet<Claim> hashSet = new HashSet<Claim>();
            for (TargetRequest targetRequest : list2) {
                if (targetRequest == null || targetRequest.target() == null || targetRequest.domain() == null) {
                    return FoodWeaponSourceLedger.invalidBatch();
                }
                Claim l2 = FoodWeaponSourceLedger.targetKey(compoundTag, serverPlayer, targetRequest.target(), targetRequest.domain());
                if (!hashSet.add(l2)) {
                    return FoodWeaponSourceLedger.invalidBatch();
                }
                boolean bl = map.containsKey(l2);
                object.add((TargetQuote)new TargetQuote(targetRequest.target(), targetRequest.domain(), bl ? TargetStatus.COMMITTED : TargetStatus.AVAILABLE));
                if (bl) continue;
                ++n;
            }
            long l = FoodWeaponSourceLedger.clock(serverPlayer);
            long l2 = map.values().stream().filter(commit -> l < commit.expiresAt()).count();
            return new AttemptBatchQuote(l2 + (long)n <= 4096L ? AttemptBatchStatus.READY : AttemptBatchStatus.CAPACITY_BLOCKED, (List<AttemptQuote>)arrayList, (List<TargetQuote>)object, compoundTag.m_128461_("ServerSignature"), l, originalInteractionBinding);
        }
    }

    private static AttemptBatchCommit rejectedBatch(AttemptBatchCommitStatus attemptBatchCommitStatus) {
        return new AttemptBatchCommit(attemptBatchCommitStatus, List.of(), List.of(), null);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static BatchPreparation prepareAttemptBatch(CompoundTag compoundTag, ServerPlayer serverPlayer, AttemptBatchQuote attemptBatchQuote, long l) {
        if (attemptBatchQuote == null || !FoodWeaponSourceLedger.interactionMatches(attemptBatchQuote.binding, compoundTag, serverPlayer) || attemptBatchQuote.status == AttemptBatchStatus.INVALID || attemptBatchQuote.entries.isEmpty() && attemptBatchQuote.targets.isEmpty() || serverPlayer == null || !FoodWeaponSourceLedger.server(serverPlayer).m_18695_() || l != FoodWeaponSourceLedger.clock(serverPlayer) || l != attemptBatchQuote.quotedAt || !FoodWeaponSourceLedger.attemptSourceValid(compoundTag, serverPlayer) || !compoundTag.m_128461_("ServerSignature").equals(attemptBatchQuote.actionSignature)) {
            return FoodWeaponSourceLedger.rejectedPreparation(AttemptBatchCommitStatus.INVALID);
        }
        Object object = FoodWeaponSourceLedger.class;
        synchronized (FoodWeaponSourceLedger.class) {
            Class<FoodWeaponSourceLedger> clazz;
            ArrayList<TargetResult> arrayList;
            Object object2;
            Object object3;
            Object l3;
            ServerState serverState = SERVERS.get(FoodWeaponSourceLedger.server(serverPlayer));
            if (serverState == null || serverState.closed) {
                // ** MonitorExit[object] (shouldn't be in output)
                return FoodWeaponSourceLedger.rejectedPreparation(AttemptBatchCommitStatus.INVALID);
            }
            Map<Claim, Commit> map = serverState.claims;
            LinkedHashMap<Claim, Commit> linkedHashMap = new LinkedHashMap<Claim, Commit>(map);
            long l2 = serverState.latestTick;
            // ** MonitorExit[object] (shouldn't be in output)
            object = new ArrayList();
            EnumSet<AttemptDomain> enumSet = EnumSet.noneOf(AttemptDomain.class);
            boolean bl = false;
            int n = 0;
            for (AttemptQuote object52 : attemptBatchQuote.entries) {
                if (object52 == null || object52.status == AttemptStatus.INVALID || object52.domain == null || !enumSet.add(object52.domain)) {
                    return FoodWeaponSourceLedger.rejectedPreparation(AttemptBatchCommitStatus.INVALID);
                }
                l3 = FoodWeaponSourceLedger.quoteAttempt(compoundTag, serverPlayer, object52.domain, object52.chance);
                if (((AttemptQuote)l3).status == AttemptStatus.INVALID || !((AttemptQuote)l3).proof.equals(object52.proof) || ((AttemptQuote)l3).success != object52.success) {
                    return FoodWeaponSourceLedger.rejectedPreparation(AttemptBatchCommitStatus.INVALID);
                }
                Commit commit2 = (Commit)linkedHashMap.get(FoodWeaponSourceLedger.attemptKey(compoundTag, serverPlayer, object52.domain));
                if (commit2 != null && (commit2.expiresAt != compoundTag.m_128454_("ExpiresAt") || l >= commit2.expiresAt)) {
                    return FoodWeaponSourceLedger.rejectedPreparation(AttemptBatchCommitStatus.INVALID);
                }
                if (((AttemptQuote)l3).status != object52.status) {
                    bl = true;
                }
                if (((AttemptQuote)l3).status == AttemptStatus.AVAILABLE) {
                    ++n;
                }
                object.add(l3);
            }
            ArrayList arrayList2 = new ArrayList();
            HashSet<Object> hashSet = new HashSet<Object>();
            for (TargetQuote targetQuote2 : attemptBatchQuote.targets) {
                if (targetQuote2 == null || targetQuote2.target == null || targetQuote2.domain == null || targetQuote2.status == null) {
                    return FoodWeaponSourceLedger.rejectedPreparation(AttemptBatchCommitStatus.INVALID);
                }
                object3 = FoodWeaponSourceLedger.targetKey(compoundTag, serverPlayer, targetQuote2.target, targetQuote2.domain);
                if (!hashSet.add(object3)) {
                    return FoodWeaponSourceLedger.rejectedPreparation(AttemptBatchCommitStatus.INVALID);
                }
                object2 = (Commit)linkedHashMap.get(object3);
                if (!(object2 == null || ((Commit)object2).expiresAt == compoundTag.m_128454_("ExpiresAt") && l < ((Commit)object2).expiresAt && Double.compare(((Commit)object2).chance, 1.0) == 0 && ((Commit)object2).success)) {
                    return FoodWeaponSourceLedger.rejectedPreparation(AttemptBatchCommitStatus.INVALID);
                }
                Object object4 = arrayList = object2 != null ? TargetStatus.COMMITTED : TargetStatus.AVAILABLE;
                if (arrayList != targetQuote2.status) {
                    bl = true;
                }
                if (arrayList == TargetStatus.AVAILABLE) {
                    ++n;
                }
                arrayList2.add(new TargetQuote(targetQuote2.target, targetQuote2.domain, (TargetStatus)((Object)arrayList)));
            }
            if (bl && n > 0) {
                return FoodWeaponSourceLedger.rejectedPreparation(AttemptBatchCommitStatus.CONFLICT);
            }
            if (n == 0) {
                l3 = FoodWeaponSourceLedger.class;
                synchronized (FoodWeaponSourceLedger.class) {
                    if (SERVERS.get(FoodWeaponSourceLedger.server(serverPlayer)) != serverState || serverState.closed || serverState.claims != map || serverState.latestTick != l2 || !serverState.claims.equals(linkedHashMap)) {
                        // ** MonitorExit[l3] (shouldn't be in output)
                        return FoodWeaponSourceLedger.rejectedPreparation(AttemptBatchCommitStatus.CONFLICT);
                    }
                    // ** MonitorExit[l3] (shouldn't be in output)
                    return new BatchPreparation(serverState, null, new AttemptBatchCommit(AttemptBatchCommitStatus.ALREADY_COMMITTED, object.stream().map(attemptQuote -> new AttemptResult(attemptQuote.domain, attemptQuote.success ? AttemptCommit.ALREADY_SUCCESS : AttemptCommit.ALREADY_FAILURE)).toList(), arrayList2.stream().map(targetQuote -> new TargetResult(targetQuote.target, targetQuote.domain, TargetCommit.ALREADY_COMMITTED)).toList(), attemptBatchQuote));
                }
            }
            long l4 = linkedHashMap.values().stream().filter(commit -> l < commit.expiresAt()).count();
            if (l4 + (long)n > 4096L) {
                return FoodWeaponSourceLedger.rejectedPreparation(AttemptBatchCommitStatus.CAPACITY_BLOCKED);
            }
            object3 = new LinkedHashMap();
            linkedHashMap.forEach((arg_0, arg_1) -> FoodWeaponSourceLedger.lambda$prepareAttemptBatch$77(l, (Map)object3, arg_0, arg_1));
            object2 = new ArrayList();
            arrayList = new ArrayList();
            Object object5 = object.iterator();
            while (object5.hasNext()) {
                clazz = (AttemptQuote)object5.next();
                if (((AttemptQuote)((Object)clazz)).status == AttemptStatus.AVAILABLE) {
                    object3.put(FoodWeaponSourceLedger.attemptKey(compoundTag, serverPlayer, ((AttemptQuote)((Object)clazz)).domain), new Commit(compoundTag.m_128454_("ExpiresAt"), ((AttemptQuote)((Object)clazz)).chance, ((AttemptQuote)((Object)clazz)).success));
                    object2.add(new AttemptResult(((AttemptQuote)((Object)clazz)).domain, ((AttemptQuote)((Object)clazz)).success ? AttemptCommit.COMMITTED_SUCCESS : AttemptCommit.COMMITTED_FAILURE));
                    continue;
                }
                object2.add(new AttemptResult(((AttemptQuote)((Object)clazz)).domain, ((AttemptQuote)((Object)clazz)).success ? AttemptCommit.ALREADY_SUCCESS : AttemptCommit.ALREADY_FAILURE));
            }
            object5 = arrayList2.iterator();
            while (object5.hasNext()) {
                clazz = (TargetQuote)object5.next();
                if (((TargetQuote)((Object)clazz)).status == TargetStatus.AVAILABLE) {
                    object3.put(FoodWeaponSourceLedger.targetKey(compoundTag, serverPlayer, ((TargetQuote)((Object)clazz)).target, ((TargetQuote)((Object)clazz)).domain), new Commit(compoundTag.m_128454_("ExpiresAt"), 1.0, true));
                    arrayList.add(new TargetResult(((TargetQuote)((Object)clazz)).target, ((TargetQuote)((Object)clazz)).domain, TargetCommit.COMMITTED));
                    continue;
                }
                arrayList.add(new TargetResult(((TargetQuote)((Object)clazz)).target, ((TargetQuote)((Object)clazz)).domain, TargetCommit.ALREADY_COMMITTED));
            }
            object5 = new AttemptBatchCommit(AttemptBatchCommitStatus.COMMITTED, (List<AttemptResult>)object2, arrayList, attemptBatchQuote);
            clazz = FoodWeaponSourceLedger.class;
            synchronized (FoodWeaponSourceLedger.class) {
                if (SERVERS.get(FoodWeaponSourceLedger.server(serverPlayer)) != serverState || serverState.closed || serverState.claims != map || serverState.latestTick != l2 || !serverState.claims.equals(linkedHashMap)) {
                    // ** MonitorExit[var22_26] (shouldn't be in output)
                    return FoodWeaponSourceLedger.rejectedPreparation(AttemptBatchCommitStatus.CONFLICT);
                }
                // ** MonitorExit[var22_26] (shouldn't be in output)
                return new BatchPreparation(serverState, (Map<Claim, Commit>)object3, (AttemptBatchCommit)object5);
            }
        }
    }

    public static AttemptBatchAdmission admitAttemptBatch(CompoundTag compoundTag, ServerPlayer serverPlayer, AttemptBatchQuote attemptBatchQuote, List<AttemptRequest> list, List<TargetRequest> list2, long l) {
        if (!FoodWeaponSourceLedger.matchesAttemptRequests(attemptBatchQuote, list, list2)) {
            return new AttemptBatchAdmission(FoodWeaponSourceLedger.rejectedPreparation(AttemptBatchCommitStatus.INVALID));
        }
        return new AttemptBatchAdmission(FoodWeaponSourceLedger.prepareAttemptBatch(compoundTag, serverPlayer, attemptBatchQuote, l));
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static boolean matchesAttemptAdmission(CompoundTag compoundTag, ServerPlayer serverPlayer, AttemptBatchQuote attemptBatchQuote, AttemptBatchAdmission attemptBatchAdmission, long l) {
        if (attemptBatchAdmission == null || attemptBatchAdmission.preparation.result.batch != attemptBatchQuote || serverPlayer == null || attemptBatchQuote == null || !FoodWeaponSourceLedger.interactionMatches(attemptBatchQuote.binding, compoundTag, serverPlayer) || attemptBatchAdmission.status != AttemptBatchAdmissionStatus.READY && attemptBatchAdmission.status != AttemptBatchAdmissionStatus.ALREADY_COMMITTED || !FoodWeaponSourceLedger.server(serverPlayer).m_18695_() || l != FoodWeaponSourceLedger.clock(serverPlayer) || l != attemptBatchQuote.quotedAt || !FoodWeaponSourceLedger.attemptSourceValid(compoundTag, serverPlayer) || !compoundTag.m_128461_("ServerSignature").equals(attemptBatchQuote.actionSignature)) {
            return false;
        }
        BatchPreparation batchPreparation = attemptBatchAdmission.preparation;
        Class<FoodWeaponSourceLedger> clazz = FoodWeaponSourceLedger.class;
        synchronized (FoodWeaponSourceLedger.class) {
            ServerState serverState = SERVERS.get(FoodWeaponSourceLedger.server(serverPlayer));
            // ** MonitorExit[var7_6] (shouldn't be in output)
            return serverState != null && !serverState.closed && serverState == batchPreparation.state && serverState.claims == batchPreparation.originalClaims && serverState.latestTick == batchPreparation.latestTick && serverState.claims.equals(batchPreparation.claimsImage);
        }
    }

    private static synchronized AttemptBatchCommit publishPreparedBatch(BatchPreparation batchPreparation, long l) {
        if (batchPreparation.state != null && (batchPreparation.state.closed || batchPreparation.state.claims != batchPreparation.originalClaims || batchPreparation.state.latestTick != batchPreparation.latestTick || !batchPreparation.state.claims.equals(batchPreparation.claimsImage))) {
            return FoodWeaponSourceLedger.rejectedBatch(AttemptBatchCommitStatus.CONFLICT);
        }
        if (batchPreparation.replacement != null) {
            batchPreparation.state.claims = batchPreparation.replacement;
            batchPreparation.state.latestTick = l;
        }
        return batchPreparation.result;
    }

    public static AttemptBatchCommit commitAdmittedAttemptBatch(CompoundTag compoundTag, ServerPlayer serverPlayer, AttemptBatchQuote attemptBatchQuote, AttemptBatchAdmission attemptBatchAdmission, float f, boolean bl, boolean bl2, long l) {
        if (bl || bl2 || !Float.isFinite(f) || f <= 0.0f) {
            return FoodWeaponSourceLedger.rejectedBatch(AttemptBatchCommitStatus.NOT_SETTLED);
        }
        if (attemptBatchAdmission == null) {
            return FoodWeaponSourceLedger.rejectedBatch(AttemptBatchCommitStatus.INVALID);
        }
        if (attemptBatchAdmission.status != AttemptBatchAdmissionStatus.READY && attemptBatchAdmission.status != AttemptBatchAdmissionStatus.ALREADY_COMMITTED) {
            return attemptBatchAdmission.preparation.result;
        }
        if (!FoodWeaponSourceLedger.matchesAttemptAdmission(compoundTag, serverPlayer, attemptBatchQuote, attemptBatchAdmission, l)) {
            return FoodWeaponSourceLedger.rejectedBatch(AttemptBatchCommitStatus.CONFLICT);
        }
        return FoodWeaponSourceLedger.publishPreparedBatch(attemptBatchAdmission.preparation, l);
    }

    public static AttemptBatchCommit commitAttemptBatch(CompoundTag compoundTag, ServerPlayer serverPlayer, AttemptBatchQuote attemptBatchQuote, float f, boolean bl, boolean bl2, long l) {
        if (bl || bl2 || !Float.isFinite(f) || f <= 0.0f) {
            return FoodWeaponSourceLedger.rejectedBatch(AttemptBatchCommitStatus.NOT_SETTLED);
        }
        return FoodWeaponSourceLedger.publishPreparedBatch(FoodWeaponSourceLedger.prepareAttemptBatch(compoundTag, serverPlayer, attemptBatchQuote, l), l);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static boolean matchesAttemptBatch(CompoundTag compoundTag, ServerPlayer serverPlayer, AttemptBatchQuote attemptBatchQuote, AttemptBatchCommit attemptBatchCommit, long l) {
        Object object;
        if (attemptBatchQuote == null || !FoodWeaponSourceLedger.interactionMatches(attemptBatchQuote.binding, compoundTag, serverPlayer) || attemptBatchCommit == null || attemptBatchCommit.batch != attemptBatchQuote || serverPlayer == null || l != FoodWeaponSourceLedger.clock(serverPlayer) || l != attemptBatchQuote.quotedAt || !FoodWeaponSourceLedger.attemptSourceValid(compoundTag, serverPlayer) || !attemptBatchQuote.actionSignature.equals(compoundTag.m_128461_("ServerSignature")) || attemptBatchCommit.status != AttemptBatchCommitStatus.COMMITTED && attemptBatchCommit.status != AttemptBatchCommitStatus.ALREADY_COMMITTED || attemptBatchCommit.entries.size() != attemptBatchQuote.entries.size() || attemptBatchCommit.targets.size() != attemptBatchQuote.targets.size()) {
            return false;
        }
        EnumSet<AttemptDomain> enumSet = EnumSet.noneOf(AttemptDomain.class);
        boolean bl = false;
        for (AttemptResult object2 : attemptBatchCommit.entries) {
            boolean targetQuote2;
            boolean claim;
            if (object2 == null || object2.domain == null || !enumSet.add(object2.domain)) {
                return false;
            }
            object = attemptBatchQuote.entries.stream().filter(attemptQuote -> attemptQuote.domain == attemptResult.domain).findFirst().orElse(null);
            if (object == null) {
                return false;
            }
            AttemptQuote attemptQuote2 = FoodWeaponSourceLedger.quoteAttempt(compoundTag, serverPlayer, ((AttemptQuote)object).domain, ((AttemptQuote)object).chance);
            if (attemptQuote2.status == AttemptStatus.INVALID || attemptQuote2.status == AttemptStatus.AVAILABLE || !attemptQuote2.proof.equals(((AttemptQuote)object).proof) || attemptQuote2.success != ((AttemptQuote)object).success) {
                return false;
            }
            boolean targetResult = object2.commit == AttemptCommit.COMMITTED_SUCCESS || object2.commit == AttemptCommit.ALREADY_SUCCESS;
            boolean bl2 = claim = object2.commit == AttemptCommit.COMMITTED_FAILURE || object2.commit == AttemptCommit.ALREADY_FAILURE;
            if (!targetResult && !claim || targetResult != attemptQuote2.success) {
                return false;
            }
            boolean bl3 = targetQuote2 = object2.commit == AttemptCommit.COMMITTED_SUCCESS || object2.commit == AttemptCommit.COMMITTED_FAILURE;
            if (targetQuote2 && ((AttemptQuote)object).status != AttemptStatus.AVAILABLE) {
                return false;
            }
            bl |= targetQuote2;
        }
        object = FoodWeaponSourceLedger.class;
        synchronized (FoodWeaponSourceLedger.class) {
            ServerState serverState = SERVERS.get(FoodWeaponSourceLedger.server(serverPlayer));
            if (serverState == null || serverState.closed) {
                // ** MonitorExit[var10_10] (shouldn't be in output)
                return false;
            }
            Map<Claim, Commit> map = Map.copyOf(serverState.claims);
            // ** MonitorExit[var10_10] (shouldn't be in output)
            object = new HashSet<Claim>();
            for (TargetResult targetResult : attemptBatchCommit.targets) {
                if (targetResult == null || targetResult.target == null || targetResult.domain == null) {
                    return false;
                }
                Claim claim = FoodWeaponSourceLedger.targetKey(compoundTag, serverPlayer, targetResult.target, targetResult.domain);
                if (!object.add(claim)) {
                    return false;
                }
                TargetQuote targetQuote2 = attemptBatchQuote.targets.stream().filter(targetQuote -> targetQuote.target.equals(targetResult.target) && targetQuote.domain == targetResult.domain).findFirst().orElse(null);
                Commit commit = map.get(claim);
                if (targetQuote2 == null || commit == null || !commit.success || Double.compare(commit.chance, 1.0) != 0 || commit.expiresAt != compoundTag.m_128454_("ExpiresAt")) {
                    return false;
                }
                if (targetResult.commit == TargetCommit.COMMITTED) {
                    if (targetQuote2.status != TargetStatus.AVAILABLE) {
                        return false;
                    }
                    bl = true;
                    continue;
                }
                if (targetResult.commit == TargetCommit.ALREADY_COMMITTED) continue;
                return false;
            }
            return bl == (attemptBatchCommit.status == AttemptBatchCommitStatus.COMMITTED);
        }
    }

    public static boolean matchesAttemptRequests(AttemptBatchQuote attemptBatchQuote, List<AttemptRequest> list, List<TargetRequest> list2) {
        if (attemptBatchQuote == null || attemptBatchQuote.status == AttemptBatchStatus.INVALID || list == null || list2 == null || list.size() != attemptBatchQuote.entries.size() || list2.size() != attemptBatchQuote.targets.size()) {
            return false;
        }
        EnumSet<AttemptDomain> enumSet = EnumSet.noneOf(AttemptDomain.class);
        for (AttemptRequest object : list) {
            if (object == null || object.domain == null || !enumSet.add(object.domain)) {
                return false;
            }
            AttemptQuote attemptQuote2 = attemptBatchQuote.entries.stream().filter(attemptQuote -> attemptQuote.domain == attemptRequest.domain).findFirst().orElse(null);
            if (attemptQuote2 != null && Double.compare(attemptQuote2.chance, object.chance) == 0) continue;
            return false;
        }
        HashSet hashSet = new HashSet();
        for (TargetRequest targetRequest : list2) {
            if (targetRequest != null && targetRequest.target != null && targetRequest.domain != null && hashSet.add(targetRequest) && !attemptBatchQuote.targets.stream().noneMatch(targetQuote -> targetQuote.target.equals(targetRequest.target) && targetQuote.domain == targetRequest.domain)) continue;
            return false;
        }
        return true;
    }

    public static AttemptCommit commitAttempt(CompoundTag compoundTag, ServerPlayer serverPlayer, AttemptQuote attemptQuote, float f, boolean bl, boolean bl2, long l) {
        if (attemptQuote == null || serverPlayer == null || compoundTag == null) {
            return bl || bl2 || !Float.isFinite(f) || f <= 0.0f ? AttemptCommit.NOT_SETTLED : AttemptCommit.INVALID;
        }
        if (!FoodWeaponSourceLedger.interactionMatches(attemptQuote.binding, compoundTag, serverPlayer)) {
            return bl || bl2 || !Float.isFinite(f) || f <= 0.0f ? AttemptCommit.NOT_SETTLED : AttemptCommit.INVALID;
        }
        AttemptBatchQuote attemptBatchQuote = new AttemptBatchQuote(AttemptBatchStatus.READY, List.of(attemptQuote), List.of(), compoundTag.m_128461_("ServerSignature"), l, attemptQuote.binding);
        AttemptBatchCommit attemptBatchCommit = FoodWeaponSourceLedger.commitAttemptBatch(compoundTag, serverPlayer, attemptBatchQuote, f, bl, bl2, l);
        if (attemptBatchCommit.status == AttemptBatchCommitStatus.NOT_SETTLED) {
            return AttemptCommit.NOT_SETTLED;
        }
        return attemptBatchCommit.entries.isEmpty() ? AttemptCommit.INVALID : attemptBatchCommit.entries.get(0).commit();
    }

    public static boolean claimSettledTarget(CompoundTag compoundTag, ServerPlayer serverPlayer, UUID uUID, TargetEffectDomain targetEffectDomain, float f, boolean bl, boolean bl2, long l) {
        if (uUID == null || targetEffectDomain == null) {
            return false;
        }
        AttemptBatchQuote attemptBatchQuote = FoodWeaponSourceLedger.quoteAttemptBatch(compoundTag, serverPlayer, List.of(), List.of(new TargetRequest(uUID, targetEffectDomain)));
        AttemptBatchCommit attemptBatchCommit = FoodWeaponSourceLedger.commitAttemptBatch(compoundTag, serverPlayer, attemptBatchQuote, f, bl, bl2, l);
        return !attemptBatchCommit.targets.isEmpty() && attemptBatchCommit.targets.get(0).commit() == TargetCommit.COMMITTED;
    }

    private static boolean damageAuthorized(CompoundTag compoundTag) {
        if (compoundTag == null || !Double.isFinite(FoodWeaponDamageScaling.residual(compoundTag))) {
            return false;
        }
        String string = compoundTag.m_128461_("DamagePurpose");
        if ("GENERIC_J".equals(string)) {
            return true;
        }
        if (NATIVE_HURT_PURPOSE.equals(string) || MEAT_HURT_PURPOSE.equals(string) || IRON_HURT_PURPOSE.equals(string)) {
            NativeHurtScope nativeHurtScope = PRIVATE_NATIVE_HURT.get();
            return nativeHurtScope != null && compoundTag.equals((Object)nativeHurtScope.snapshot) && FoodWeaponSourceLedger.nativeHurtLive(nativeHurtScope) && Double.compare(compoundTag.m_128459_("RarityFactor"), 1.0) == 0 && Double.compare(compoundTag.m_128459_("AppliedMaterial"), 1.0) == 0 && Double.compare(compoundTag.m_128459_("AppliedEvolution"), 1.0) == 0 && Double.compare(compoundTag.m_128459_("AppliedRarity"), 1.0) == 0;
        }
        return NATIVE_PURPOSE.equals(string) && compoundTag.m_128403_("NativeReceiptId") && compoundTag.m_128403_("NativeEntityId") && List.of("BOW", "CROSSBOW", "TRIDENT").contains(compoundTag.m_128461_("NativeEntry")) && Double.compare(compoundTag.m_128459_("RarityFactor"), 1.0) == 0 && Double.compare(compoundTag.m_128459_("AppliedMaterial"), 1.0) == 0 && Double.compare(compoundTag.m_128459_("AppliedEvolution"), 1.0) == 0 && Double.compare(compoundTag.m_128459_("AppliedRarity"), 1.0) == 0;
    }

    public static boolean originalHandContinuous(CompoundTag compoundTag, ServerPlayer serverPlayer) {
        if (!FoodWeaponSourceLedger.valid(compoundTag, serverPlayer)) {
            return false;
        }
        return FoodWeaponSourceLedger.originalHandContinuousReadOnly(compoundTag, serverPlayer);
    }

    public static boolean originalHandContinuousReadOnly(CompoundTag compoundTag, ServerPlayer serverPlayer) {
        if (!FoodWeaponSourceLedger.validReadOnly(compoundTag, serverPlayer)) {
            return false;
        }
        InteractionHand interactionHand = InteractionHand.valueOf((String)compoundTag.m_128461_("OriginalHand"));
        ItemStack itemStack = serverPlayer.m_21120_(interactionHand);
        ItemStack itemStack2 = ItemStack.m_41712_((CompoundTag)compoundTag.m_128469_("Weapon"));
        ResourceLocation resourceLocation = itemStack.m_41619_() ? null : ForgeRegistries.ITEMS.getKey((Object)itemStack.m_41720_());
        ResourceLocation resourceLocation2 = itemStack2.m_41619_() ? null : ForgeRegistries.ITEMS.getKey((Object)itemStack2.m_41720_());
        return !itemStack.m_41619_() && itemStack.m_41613_() == 1 && !itemStack2.m_41619_() && itemStack2.m_41613_() == 1 && itemStack.m_41720_() == itemStack2.m_41720_() && resourceLocation != null && resourceLocation.equals((Object)resourceLocation2) && FoodWeaponFamilyBuild.kind(itemStack).name().equals(compoundTag.m_128461_("Family")) && !FoodWeaponLifecycle.isBroken(itemStack) && compoundTag.m_128342_("WeaponId").equals(FoodWeaponLifecycle.weaponId(itemStack)) && FoodWeaponSourceLedger.boundProfile(itemStack).equals(compoundTag.m_128461_("BoundProfile")) && FoodWeaponLifecycle.repairProfile(itemStack).equals(compoundTag.m_128461_("RepairProfile")) && FoodWeaponSourceLedger.materialFingerprint(itemStack).equals(compoundTag.m_128461_("MaterialFingerprint")) && FoodWeaponSourceLedger.socketFingerprint(itemStack, compoundTag.m_128461_("Family")).equals(compoundTag.m_128461_("SocketFingerprint")) && FoodWeaponSourceLedger.foundationContinuous(compoundTag, serverPlayer, itemStack);
    }

    public static boolean isTrident(ItemStack itemStack) {
        ResourceLocation resourceLocation = itemStack.m_41619_() ? null : ForgeRegistries.ITEMS.getKey((Object)itemStack.m_41720_());
        return resourceLocation != null && resourceLocation.toString().equals("minecraft:trident");
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static SourceTicket issueTrident(ServerPlayer serverPlayer, ItemStack itemStack, InteractionHand interactionHand) {
        if (serverPlayer == null || itemStack == null || !OwnerFullRepairGuard.permits(itemStack) || itemStack.m_41783_() != null && !FoodWeaponSourceLedger.canonicalInputValid((Tag)itemStack.m_41783_())) {
            return null;
        }
        CompoundTag compoundTag = itemStack.m_41739_(new CompoundTag());
        if (!FoodWeaponSourceLedger.canonicalInputValid((Tag)compoundTag) || !FoodWeaponSourceLedger.isTrident(itemStack) || interactionHand == null || serverPlayer.m_21120_(interactionHand) != itemStack || FoodWeaponLifecycle.isBroken(itemStack) || itemStack.m_41773_() >= itemStack.m_41776_() || FoodWeaponSourceLedger.boundProfile(itemStack).isEmpty() && !FoodWeaponSourceLedger.nativeFoundation(itemStack)) {
            return null;
        }
        if (FourWeaponFoundationAdapter.applies(itemStack) && FoodWeaponSourceLedger.foundationRules(serverPlayer, itemStack).isEmpty()) {
            return null;
        }
        FoodWeaponLifecycle.ensureTracked(itemStack);
        UUID uUID = FoodWeaponLifecycle.weaponId(itemStack);
        if (uUID == null) {
            return null;
        }
        String string = FoodWeaponSourceLedger.materialFingerprint(itemStack);
        if (string.isEmpty()) {
            return null;
        }
        ServerState serverState = FoodWeaponSourceLedger.state(FoodWeaponSourceLedger.server(serverPlayer));
        long l = FoodWeaponSourceLedger.clock(serverPlayer);
        Class<FoodWeaponSourceLedger> clazz = FoodWeaponSourceLedger.class;
        synchronized (FoodWeaponSourceLedger.class) {
            if (SERVERS.get(FoodWeaponSourceLedger.server(serverPlayer)) != serverState || serverState.closed || l < serverState.latestTick || l < 0L || l == Long.MAX_VALUE) {
                // ** MonitorExit[var9_8] (shouldn't be in output)
                return null;
            }
            serverState.latestTick = l;
            SourceTicket sourceTicket = new SourceTicket(serverPlayer.m_20148_(), uUID, interactionHand, FoodWeaponSourceLedger.boundProfile(itemStack), FoodWeaponLifecycle.repairProfile(itemStack), FoodWeaponSourceLedger.socketFingerprint(itemStack, "MELEE"), string, UUID.randomUUID(), serverPlayer.m_9236_().m_46472_().m_135782_().toString(), l, l + 1L, serverState.epoch, FoodWeaponSourceLedger.ensureOwnerState((ServerState)serverState, (UUID)serverPlayer.m_20148_()).epoch, itemStack.m_41773_(), itemStack.m_41776_());
            try {
                if (FoodWeaponSourceLedger.tridentRawMatchesExpected(itemStack, compoundTag)) {
                    TRIDENT_BEFORE.set(new TridentTicketBefore(sourceTicket, itemStack, compoundTag));
                }
            }
            catch (Throwable throwable) {
                TRIDENT_BEFORE.remove();
            }
            return sourceTicket;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void releaseTrident(ServerPlayer serverPlayer, ItemStack itemStack, InteractionHand interactionHand, Runnable runnable) {
        TicketState ticketState = TRIDENT_TICKET.get();
        TridentTicketBefore tridentTicketBefore = TRIDENT_BEFORE.get();
        TRIDENT_BEFORE.remove();
        SourceTicket sourceTicket = FoodWeaponSourceLedger.issueTrident(serverPlayer, itemStack, interactionHand);
        TRIDENT_TICKET.remove();
        if (sourceTicket != null) {
            TRIDENT_TICKET.set(new TicketState(sourceTicket, itemStack, FoodWeaponSourceLedger.server(serverPlayer), FoodWeaponSourceLedger.foundationRules(serverPlayer, itemStack)));
        }
        try {
            FoodWeaponRanged.withSource(null, new CompoundTag(), () -> {
                runnable.run();
                return null;
            });
        }
        finally {
            TRIDENT_TICKET.remove();
            TRIDENT_BEFORE.remove();
            if (ticketState != null && (tridentTicketBefore == null || !tridentTicketBefore.observationRejected) && ticketState.ticket().ownerEpoch().equals(FoodWeaponSourceLedger.ownerEpoch(FoodWeaponSourceLedger.state(ticketState.server()), ticketState.ticket().owner())) && ticketState.ticket().serverEpoch().equals(FoodWeaponSourceLedger.state((MinecraftServer)ticketState.server()).epoch)) {
                TRIDENT_TICKET.set(ticketState);
                if (tridentTicketBefore != null && tridentTicketBefore.ticket == ticketState.ticket()) {
                    TRIDENT_BEFORE.set(tridentTicketBefore);
                }
            }
        }
    }

    public static void attachTrident(ThrownTrident thrownTrident, LivingEntity livingEntity, ItemStack itemStack) {
        CompoundTag compoundTag;
        CompoundTag compoundTag2;
        ItemStack itemStack2;
        OwnerTridentCarrierAccess ownerTridentCarrierAccess;
        ServerPlayer serverPlayer;
        TridentTicketBefore tridentTicketBefore;
        TicketState ticketState;
        block22: {
            block21: {
                if (NATIVE_DEPTH.get() > 0) {
                    return;
                }
                ticketState = TRIDENT_TICKET.get();
                tridentTicketBefore = TRIDENT_BEFORE.get();
                if (TRIDENT_TICKET.get() == ticketState) {
                    TRIDENT_TICKET.remove();
                }
                if (ticketState == null || !(livingEntity instanceof ServerPlayer)) break block21;
                serverPlayer = (ServerPlayer)livingEntity;
                if (thrownTrident.m_19749_() == serverPlayer && itemStack == ticketState.actualWeapon() && FoodWeaponSourceLedger.server(serverPlayer) == ticketState.server() && !thrownTrident.m_9236_().f_46443_) break block22;
            }
            return;
        }
        SourceTicket sourceTicket = ticketState.ticket();
        long l = FoodWeaponSourceLedger.clock(serverPlayer);
        if (!sourceTicket.owner().equals(serverPlayer.m_20148_()) || l < sourceTicket.issuedAt() || l >= sourceTicket.expiresAt() || !thrownTrident.m_9236_().m_46472_().m_135782_().toString().equals(sourceTicket.dimension())) {
            return;
        }
        TridentCarrierLineage tridentCarrierLineage = null;
        try {
            TridentCarrierLineage tridentCarrierLineage2;
            if (thrownTrident instanceof OwnerTridentCarrierAccess) {
                ownerTridentCarrierAccess = (OwnerTridentCarrierAccess)thrownTrident;
                tridentCarrierLineage2 = ownerTridentCarrierAccess.ftboss$ownerTridentLineage();
            } else {
                tridentCarrierLineage2 = null;
            }
            tridentCarrierLineage = tridentCarrierLineage2;
            itemStack2 = FoodWeaponTridentCarrierReader.readOriginalItem(thrownTrident);
            if (FoodWeaponSourceLedger.tridentCarrierLineage(thrownTrident, itemStack2, tridentCarrierLineage) == null || tridentCarrierLineage.origin != ticketState || tridentCarrierLineage.input != itemStack || tridentTicketBefore == null || tridentTicketBefore.observationRejected || tridentTicketBefore.ticket != sourceTicket || tridentTicketBefore.actual != itemStack || tridentTicketBefore.thread != Thread.currentThread()) {
                return;
            }
            compoundTag2 = tridentTicketBefore.expected.image();
            if (!FoodWeaponSourceLedger.tridentRawMatchesExpected(itemStack2, compoundTag2)) {
                return;
            }
            if (!(FoodWeaponSourceLedger.isTrident(itemStack2) && sourceTicket.weaponId().equals(FoodWeaponLifecycle.weaponId(itemStack2)) && sourceTicket.profile().equals(FoodWeaponSourceLedger.boundProfile(itemStack2)) && sourceTicket.repairProfile().equals(FoodWeaponLifecycle.repairProfile(itemStack2)) && sourceTicket.socketFingerprint().equals(FoodWeaponSourceLedger.socketFingerprint(itemStack2, "MELEE")) && sourceTicket.materialFingerprint().equals(FoodWeaponSourceLedger.materialFingerprint(itemStack2)) && ticketState.foundationRules().equals(FoodWeaponSourceLedger.foundationRules(serverPlayer, itemStack2)))) {
                return;
            }
        }
        catch (Throwable throwable) {
            FoodWeaponSourceLedger.rejectTridentExtra(thrownTrident, tridentCarrierLineage, null, null, ticketState, tridentTicketBefore, throwable);
            return;
        }
        ownerTridentCarrierAccess = new CompoundTag();
        ownerTridentCarrierAccess.m_128405_("SnapshotVersion", 3);
        ownerTridentCarrierAccess.m_128362_("Owner", sourceTicket.owner());
        ownerTridentCarrierAccess.m_128362_("WeaponId", sourceTicket.weaponId());
        ownerTridentCarrierAccess.m_128362_("AttackId", sourceTicket.actionId());
        ownerTridentCarrierAccess.m_128362_("ServerEpoch", sourceTicket.serverEpoch());
        ownerTridentCarrierAccess.m_128362_("OwnerEpoch", sourceTicket.ownerEpoch());
        ownerTridentCarrierAccess.m_128359_("OriginalHand", sourceTicket.originalHand().name());
        ownerTridentCarrierAccess.m_128359_("Dimension", sourceTicket.dimension());
        ownerTridentCarrierAccess.m_128356_("CapturedAt", sourceTicket.issuedAt());
        ownerTridentCarrierAccess.m_128356_("ExpiresAt", sourceTicket.issuedAt() + 1200L);
        ownerTridentCarrierAccess.m_128359_("Family", "MELEE");
        ownerTridentCarrierAccess.m_128359_("SourceKind", "TRIDENT");
        ownerTridentCarrierAccess.m_128359_("BoundProfile", sourceTicket.profile());
        ownerTridentCarrierAccess.m_128359_("RepairProfile", sourceTicket.repairProfile());
        ownerTridentCarrierAccess.m_128359_("SocketFingerprint", sourceTicket.socketFingerprint());
        ownerTridentCarrierAccess.m_128359_("MaterialFingerprint", sourceTicket.materialFingerprint());
        ownerTridentCarrierAccess.m_128379_("TridentLaunchAuthorized", true);
        ownerTridentCarrierAccess.m_128405_("PreWearDamage", sourceTicket.preWearDamage());
        ownerTridentCarrierAccess.m_128365_("Weapon", (Tag)compoundTag2);
        if (FourWeaponFoundationAdapter.applies(itemStack2)) {
            compoundTag = FoodWeaponMaterialFingerprint.foundationSnapshot(itemStack2);
            if (compoundTag.m_128456_() || ticketState.foundationRules().isEmpty()) {
                return;
            }
            ownerTridentCarrierAccess.m_128365_("FoundationSnapshot", (Tag)compoundTag.m_6426_());
            ownerTridentCarrierAccess.m_128359_("FoundationApprovedRules", ticketState.foundationRules());
        }
        if ((compoundTag = itemStack2.m_41737_("FTBossWeaponMaterials")) != null) {
            for (FoodWeaponBuildRules.Part part : FoodWeaponBuildRules.Part.values()) {
                ownerTridentCarrierAccess.m_128359_(part.name(), compoundTag.m_128461_(part.name()));
            }
        }
        WeaponMaterialPowers.snapshot(itemStack2, (CompoundTag)ownerTridentCarrierAccess);
        if (!OwnerRepairGenerationAuthority.stamp(itemStack, (CompoundTag)ownerTridentCarrierAccess, FoodWeaponSourceLedger.state((MinecraftServer)FoodWeaponSourceLedger.server((ServerPlayer)serverPlayer)).revocations)) {
            return;
        }
        ownerTridentCarrierAccess.m_128359_("ServerSignature", FoodWeaponSourceLedger.signature((CompoundTag)ownerTridentCarrierAccess, FoodWeaponSourceLedger.state(FoodWeaponSourceLedger.server(serverPlayer))));
        if (!OwnerFullRepairGuard.bind(itemStack, sourceTicket.weaponId())) {
            return;
        }
        if (FoodWeaponSourceLedger.valid((CompoundTag)ownerTridentCarrierAccess, serverPlayer) && !thrownTrident.getPersistentData().m_128441_("FTBossWeaponMaterials")) {
            thrownTrident.getPersistentData().m_128365_("FTBossWeaponMaterials", (Tag)ownerTridentCarrierAccess);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void clear(ServerPlayer serverPlayer) {
        NativeOperation nativeOperation;
        NativeIronUseToken nativeIronUseToken;
        NativeIngressToken nativeIngressToken;
        NativeHurtScope nativeHurtScope;
        NativeIronCallbackToken nativeIronCallbackToken = PRIVATE_IRON_CALLBACK.get();
        if (nativeIronCallbackToken != null && nativeIronCallbackToken.payment.cast.use.player == serverPlayer) {
            FoodWeaponSourceLedger.nativeIronCallbackClose(nativeIronCallbackToken);
        }
        if ((nativeHurtScope = PRIVATE_NATIVE_HURT.get()) != null && nativeHurtScope.player == serverPlayer) {
            FoodWeaponSourceLedger.closeNativeHurtScope(nativeHurtScope);
        }
        if ((nativeIngressToken = PRIVATE_INGRESS.get()) != null && nativeIngressToken.player == serverPlayer) {
            FoodWeaponSourceLedger.cancelNativeIngress(nativeIngressToken);
        }
        if ((nativeIronUseToken = PRIVATE_IRON_USE.get()) != null && nativeIronUseToken.player == serverPlayer) {
            nativeIronUseToken.invalid = true;
            PRIVATE_IRON_USE.remove();
        }
        if ((nativeOperation = NATIVE_OPERATION.get()) != null && nativeOperation.player == serverPlayer) {
            nativeOperation.invalid = true;
            NATIVE_OPERATION.remove();
        }
        ServerState serverState = FoodWeaponSourceLedger.state(FoodWeaponSourceLedger.server(serverPlayer));
        Class<FoodWeaponSourceLedger> clazz = FoodWeaponSourceLedger.class;
        synchronized (FoodWeaponSourceLedger.class) {
            Object object;
            for (int i = 0; i < 4096; ++i) {
                object = ACTIVE_ROOT_PROJECTIONS[i];
                if (object == null || ((InteractionRoot)object).player != serverPlayer) continue;
                FoodWeaponSourceLedger.retireInteraction((InteractionRoot)object);
            }
            if (SERVERS.get(FoodWeaponSourceLedger.server(serverPlayer)) == serverState && !serverState.closed) {
                OwnerState ownerState = serverState.ownerEpochs.remove(serverPlayer.m_20148_());
                if (ownerState != null) {
                    ownerState.closed = true;
                    ownerState.interactionChange = new Object();
                    object = ownerState.current;
                    if (object != null && !((InteractionRoot)object).closed) {
                        FoodWeaponSourceLedger.retireInteraction((InteractionRoot)object);
                    }
                }
                serverState.claims.keySet().removeIf(claim -> claim.owner().equals(serverPlayer.m_20148_()));
                object = TRIDENT_TICKET.get();
                if (object != null && ((TicketState)object).ticket().owner().equals(serverPlayer.m_20148_())) {
                    TRIDENT_TICKET.remove();
                }
            }
            // ** MonitorExit[var7_7] (shouldn't be in output)
            FoodWeaponRanged.clearOwnerScopes(serverPlayer);
            return;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void clearServer(MinecraftServer minecraftServer) {
        NativeOperation nativeOperation;
        NativeIronUseToken nativeIronUseToken;
        NativeIngressToken nativeIngressToken;
        NativeHurtScope nativeHurtScope;
        NativeIronCallbackToken nativeIronCallbackToken = PRIVATE_IRON_CALLBACK.get();
        if (nativeIronCallbackToken != null && FoodWeaponSourceLedger.server(nativeIronCallbackToken.payment.cast.use.player) == minecraftServer) {
            FoodWeaponSourceLedger.nativeIronCallbackClose(nativeIronCallbackToken);
        }
        if ((nativeHurtScope = PRIVATE_NATIVE_HURT.get()) != null && FoodWeaponSourceLedger.server(nativeHurtScope.player) == minecraftServer) {
            FoodWeaponSourceLedger.closeNativeHurtScope(nativeHurtScope);
        }
        if ((nativeIngressToken = PRIVATE_INGRESS.get()) != null && FoodWeaponSourceLedger.server(nativeIngressToken.player) == minecraftServer) {
            FoodWeaponSourceLedger.cancelNativeIngress(nativeIngressToken);
        }
        if ((nativeIronUseToken = PRIVATE_IRON_USE.get()) != null && FoodWeaponSourceLedger.server(nativeIronUseToken.player) == minecraftServer) {
            nativeIronUseToken.invalid = true;
            PRIVATE_IRON_USE.remove();
        }
        if ((nativeOperation = NATIVE_OPERATION.get()) != null && nativeOperation.server == minecraftServer) {
            nativeOperation.invalid = true;
            NATIVE_OPERATION.remove();
        }
        Class<FoodWeaponSourceLedger> clazz = FoodWeaponSourceLedger.class;
        synchronized (FoodWeaponSourceLedger.class) {
            ServerState serverState = SERVERS.get(minecraftServer);
            if (serverState != null) {
                serverState.closed = true;
                for (OwnerState ownerState : serverState.ownerEpochs.values()) {
                    if (ownerState.current == null) continue;
                    FoodWeaponSourceLedger.retireInteraction(ownerState.current);
                }
            }
            SERVERS.remove(minecraftServer);
            TicketState ticketState = TRIDENT_TICKET.get();
            if (ticketState != null && ticketState.server() == minecraftServer) {
                TRIDENT_TICKET.remove();
            }
            // ** MonitorExit[var6_6] (shouldn't be in output)
            FoodWeaponRanged.clearServerScopes(minecraftServer);
            return;
        }
    }

    private static boolean readWindowIdentity(PrimaryReadWindow primaryReadWindow) {
        InteractionRoot interactionRoot = primaryReadWindow.root;
        return interactionRoot.thread == Thread.currentThread() && !interactionRoot.closed && !interactionRoot.retired && interactionRoot.current == primaryReadWindow.binding && !primaryReadWindow.binding.closed && interactionRoot.ownerState.current == interactionRoot && !interactionRoot.ownerState.closed && !interactionRoot.serverState.closed && SERVERS.get(interactionRoot.server) == interactionRoot.serverState && interactionRoot.serverState.epoch == interactionRoot.serverEpoch && interactionRoot.ownerState.epoch == interactionRoot.ownerEpoch && interactionRoot.projectionSlot >= 0 && ACTIVE_ROOT_PROJECTIONS[interactionRoot.projectionSlot] == interactionRoot && FoodWeaponSourceLedger.nativeObservationReadLive(interactionRoot) && (interactionRoot.expectation == null ? primaryReadWindow.repairActual == null : !interactionRoot.expectation.closed && interactionRoot.expectation.actual == primaryReadWindow.repairActual && interactionRoot.expectation.repairGeneration == primaryReadWindow.repairGeneration && OwnerRepairGenerationAuthority.identityMatches(primaryReadWindow.repairActual, primaryReadWindow.repairGeneration, primaryReadWindow.repairLease));
    }

    private static boolean preparedEpochIdentity(OwnerWriterPublication ownerWriterPublication, MinecraftServer minecraftServer, Object object, Object object2) {
        InteractionRoot interactionRoot = ownerWriterPublication.binding.root;
        return interactionRoot.server == minecraftServer && !interactionRoot.closed && !interactionRoot.retired && !interactionRoot.serverState.closed && !interactionRoot.ownerState.closed && interactionRoot.serverState.epoch == object && interactionRoot.ownerState.epoch == object2 && interactionRoot.serverEpoch == object && interactionRoot.ownerEpoch == object2 && SERVERS.get(minecraftServer) == interactionRoot.serverState && interactionRoot.ownerState.current == interactionRoot;
    }

    private static /* synthetic */ void lambda$prepareAttemptBatch$77(long l, Map map, Claim claim, Commit commit) {
        if (l < commit.expiresAt()) {
            map.put(claim, commit);
        }
    }

    static {
        SERVERS = Collections.synchronizedMap(new WeakHashMap());
        TRIDENT_TICKET = new ThreadLocal();
        NATIVE_OPERATION = new ThreadLocal();
        NATIVE_DEPTH = ThreadLocal.withInitial(() -> 0);
        NATIVE_CALLER = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE);
        PRIVATE_IRON_CALLBACK = new ThreadLocal();
        PRIVATE_NATIVE_HURT = new ThreadLocal();
        PRIVATE_IRON_USE = new ThreadLocal();
        PRIVATE_INGRESS = new ThreadLocal();
        PRIVATE_INGRESS_WEAR = new ThreadLocal();
        TRIDENT_BEFORE = new ThreadLocal();
    }

    private static final class NativeIssueWindow {
        private final NativeIngressToken ingress = PRIVATE_INGRESS.get();
        private final NativeIronUseToken use = PRIVATE_IRON_USE.get();
        private final NativeIronCallbackToken callback = PRIVATE_IRON_CALLBACK.get();
        private final NativeWearToken wear = PRIVATE_INGRESS_WEAR.get();
        private final NativeHurtScope hurt = PRIVATE_NATIVE_HURT.get();
        private final NativeOperation operation = NATIVE_OPERATION.get();
        private final int depth = NATIVE_DEPTH.get();
        private final ServerPlayer player;
        private final InteractionRoot projection;
        private final Object ownerChange;
        private final Object rootChange;
        private boolean originalRetired;

        private NativeIssueWindow(ServerPlayer serverPlayer) {
            this.player = serverPlayer != null ? serverPlayer : (this.ingress != null ? this.ingress.player : (this.use != null ? this.use.player : (this.callback != null ? this.callback.payment.cast.use.player : null)));
            this.projection = FoodWeaponSourceLedger.projectedRootForPlayer(this.player);
            this.ownerChange = this.projection == null ? null : this.projection.ownerState.interactionChange;
            this.rootChange = this.projection == null ? null : this.projection.interactionChange;
        }

        private boolean unchanged() {
            return PRIVATE_INGRESS.get() == this.ingress && PRIVATE_IRON_USE.get() == this.use && PRIVATE_IRON_CALLBACK.get() == this.callback && PRIVATE_INGRESS_WEAR.get() == this.wear && PRIVATE_NATIVE_HURT.get() == this.hurt && NATIVE_OPERATION.get() == this.operation && NATIVE_DEPTH.get() == this.depth && (this.originalRetired ? FoodWeaponSourceLedger.projectedRootForPlayer(this.player) == null : FoodWeaponSourceLedger.projectedRootForPlayer(this.player) == this.projection && (this.projection == null || this.projection.ownerState.interactionChange == this.ownerChange && this.projection.interactionChange == this.rootChange));
        }
    }

    public static final class InteractionRoot {
        private final OwnerState ownerState;
        private final ServerState serverState;
        private final ServerPlayer player;
        private final MinecraftServer server;
        private final UUID serverEpoch;
        private final UUID ownerEpoch;
        private final Thread thread;
        private final long tick;
        private final RootSite site;
        private final ReadCut readCut = new ReadCut();
        private final ReadAliases readAliases = new ReadAliases();
        private volatile int projectionSlot = -1;
        private NativeRootObservation nativeObservations;
        private NativeProjectileObservation nativeProjectileObservation;
        private WeaponExpectation expectation;
        private boolean ownsExpectation;
        private volatile Object interactionChange = new Object();
        private volatile OwnerWriterPublication writerPublication;
        private volatile OriginalInteractionBinding current;
        private volatile boolean retired;
        private boolean closed;

        private InteractionRoot(OwnerState ownerState, ServerState serverState, ServerPlayer serverPlayer, RootSite rootSite) {
            this.ownerState = ownerState;
            this.serverState = serverState;
            this.player = serverPlayer;
            this.server = FoodWeaponSourceLedger.server(serverPlayer);
            this.serverEpoch = serverState.epoch;
            this.ownerEpoch = ownerState.epoch;
            this.thread = Thread.currentThread();
            this.tick = FoodWeaponSourceLedger.clock(serverPlayer);
            this.site = rootSite;
        }
    }

    private static final class OwnerState {
        private final UUID epoch;
        private volatile InteractionRoot current;
        private boolean closed;
        private volatile Object interactionChange = new Object();

        private OwnerState(UUID uUID) {
            this.epoch = uUID;
        }
    }

    public static final class NativeIssuerCustody {
        private final IssuerCut cut = new IssuerCut();
        private NativeIngressToken ingress;
        private NativeIronUseToken use;
        private NativeIronCallbackToken callback;
        private NativeWearToken wear;
        private NativeCarrierToken carrier;
        private NativeCapabilityToken capability;

        private NativeIssuerCustody() {
        }
    }

    public static final class NativeIngressToken {
        private final Object repairGeneration;
        private final NativeIngressKind kind;
        private final ServerPlayer player;
        private final ItemStack actual;
        private final InteractionHand hand;
        private final ServerState state;
        private final UUID ownerEpoch;
        private final UUID action = UUID.randomUUID();
        private final long tick;
        private final Thread thread = Thread.currentThread();
        private final String dimension;
        private final String entry;
        private final String descriptor;
        private final String rules;
        private final CompoundTag before;
        private final WeaponExpectation expectation;
        private final List<NativeCarrierToken> carriers = new ArrayList<NativeCarrierToken>();
        private NativeCapabilityToken capability;
        private int wears;
        private boolean invalid;
        private boolean returned;

        private NativeIngressToken(NativeIngressKind nativeIngressKind, ServerPlayer serverPlayer, ItemStack itemStack, InteractionHand interactionHand, String string, String string2) {
            this.kind = nativeIngressKind;
            this.player = serverPlayer;
            this.actual = itemStack;
            this.repairGeneration = OwnerRepairGenerationAuthority.current(itemStack);
            this.hand = interactionHand;
            this.state = FoodWeaponSourceLedger.state(FoodWeaponSourceLedger.server(serverPlayer));
            this.ownerEpoch = FoodWeaponSourceLedger.ensureOwnerState((ServerState)this.state, (UUID)serverPlayer.m_20148_()).epoch;
            this.tick = FoodWeaponSourceLedger.clock(serverPlayer);
            this.dimension = serverPlayer.m_9236_().m_46472_().m_135782_().toString();
            this.entry = string;
            this.descriptor = string2;
            this.rules = FoodWeaponSourceLedger.foundationRules(serverPlayer, itemStack);
            this.before = itemStack.m_41739_(new CompoundTag()).m_6426_();
            this.expectation = new WeaponExpectation(itemStack, this.before);
        }
    }

    private static final class IssuerCut {
        private final Thread thread = Thread.currentThread();
        private Object published;
        private boolean transferred;
        private volatile boolean closed;

        private IssuerCut() {
        }

        private boolean reserve(Object object) {
            if (this.thread != Thread.currentThread() || this.closed || this.published != null || object == null) {
                return false;
            }
            this.published = object;
            return true;
        }

        private boolean transfer(Object object) {
            if (this.thread != Thread.currentThread() || this.closed || this.transferred || object == null || this.published != object) {
                return false;
            }
            this.transferred = true;
            return true;
        }

        private boolean close() {
            if (this.thread != Thread.currentThread() || this.closed) {
                return false;
            }
            this.closed = true;
            return !this.transferred;
        }
    }

    public static final class NativeIronUseToken {
        private final Object repairGeneration;
        private final ServerPlayer player;
        private final ItemStack actual;
        private final InteractionHand hand;
        private final ServerState state;
        private final UUID ownerEpoch;
        private final long tick;
        private final String dimension;
        private final String rules;
        private final Thread thread = Thread.currentThread();
        private boolean invalid;

        private NativeIronUseToken(ServerPlayer serverPlayer, ItemStack itemStack, InteractionHand interactionHand) {
            this.player = serverPlayer;
            this.actual = itemStack;
            this.repairGeneration = OwnerRepairGenerationAuthority.current(itemStack);
            this.hand = interactionHand;
            this.state = FoodWeaponSourceLedger.state(FoodWeaponSourceLedger.server(serverPlayer));
            this.ownerEpoch = FoodWeaponSourceLedger.ensureOwnerState((ServerState)this.state, (UUID)serverPlayer.m_20148_()).epoch;
            this.tick = FoodWeaponSourceLedger.clock(serverPlayer);
            this.dimension = serverPlayer.m_9236_().m_46472_().m_135782_().toString();
            this.rules = FoodWeaponSourceLedger.foundationRules(serverPlayer, itemStack);
        }
    }

    public static final class NativeIronCallbackToken {
        private final NativeIronPaymentToken payment;
        private final Thread thread = Thread.currentThread();
        private final long tick;
        private final CompoundTag weapon;
        private final WeaponExpectation expectation;
        private boolean normal;
        private boolean closed;

        private NativeIronCallbackToken(NativeIronPaymentToken nativeIronPaymentToken) {
            this.payment = nativeIronPaymentToken;
            this.tick = FoodWeaponSourceLedger.clock(nativeIronPaymentToken.cast.use.player);
            this.weapon = nativeIronPaymentToken.cast.use.actual.m_41739_(new CompoundTag()).m_6426_();
            this.expectation = new WeaponExpectation(nativeIronPaymentToken.cast.use.actual, this.weapon);
        }
    }

    public static final class NativeWearToken {
        private final NativeIngressToken ingress;
        private final CompoundTag before;
        private final String rules;
        private int writes;
        private int requested = -1;
        private boolean returned;

        private NativeWearToken(NativeIngressToken nativeIngressToken) {
            this.ingress = nativeIngressToken;
            this.before = nativeIngressToken.actual.m_41739_(new CompoundTag()).m_6426_();
            this.rules = FoodWeaponSourceLedger.foundationRules(nativeIngressToken.player, nativeIngressToken.actual);
        }
    }

    public static final class NativeCarrierToken {
        private final NativeIngressToken ingress;
        private final Entity carrier;
        private final UUID entityId;
        private boolean actor;
        private boolean added;
        private CompoundTag image;

        private NativeCarrierToken(NativeIngressToken nativeIngressToken, Entity entity) {
            Projectile projectile;
            this.ingress = nativeIngressToken;
            this.carrier = entity;
            this.entityId = entity.m_20148_();
            this.actor = entity instanceof Projectile && (projectile = (Projectile)entity).m_19749_() == nativeIngressToken.player;
        }
    }

    public static final class NativeCapabilityToken {
        private final NativeIngressToken ingress;
        private final Object capability;
        private boolean canceled;

        private NativeCapabilityToken(NativeIngressToken nativeIngressToken, Object object) {
            this.ingress = nativeIngressToken;
            this.capability = object;
        }
    }

    private static final class WeaponExpectation {
        private final ItemStack actual;
        private final Object repairGeneration;
        private CompoundTag expected;
        private final ImageCursor cursor;
        private volatile OwnerWriterStep active;
        private boolean closed;

        private WeaponExpectation(ItemStack itemStack, CompoundTag compoundTag) {
            this.actual = itemStack;
            this.repairGeneration = OwnerRepairGenerationAuthority.current(itemStack);
            this.expected = compoundTag.m_6426_();
            this.cursor = new ImageCursor(FoodWeaponSourceLedger.canonical((Tag)this.expected));
        }
    }

    private static final class ImageCursor {
        private static final int BYTE_BUDGET = 0x100000;
        private byte[] expected;
        private Object position = new Object();
        private volatile boolean closed;

        private ImageCursor(byte[] byArray) {
            if (byArray == null || byArray.length > 0x100000) {
                throw new IllegalArgumentException("Image budget");
            }
            this.expected = (byte[])byArray.clone();
        }

        private boolean matches(byte[] byArray) {
            if (this.closed || byArray == null || byArray.length != this.expected.length) {
                return false;
            }
            for (int i = 0; i < this.expected.length; ++i) {
                if (byArray[i] == this.expected[i]) continue;
                return false;
            }
            return true;
        }

        private Object position() {
            return this.position;
        }

        private boolean advance(Object object, byte[] byArray, byte[] byArray2, byte[] byArray3) {
            if (this.closed || object != this.position || byArray2 == null || byArray2.length > 0x100000 || !this.matches(byArray) || byArray3 == null || byArray3.length != byArray2.length) {
                return false;
            }
            for (int i = 0; i < byArray2.length; ++i) {
                if (byArray2[i] == byArray3[i]) continue;
                return false;
            }
            this.expected = (byte[])byArray2.clone();
            this.position = new Object();
            return true;
        }

        private void close() {
            this.closed = true;
        }
    }

    static final class OwnerWriterPublication {
        private final OriginalInteractionBinding binding;
        private final AttemptBatchQuote batch;
        private final AttemptBatchAdmission admission;
        private final AttemptBatchCommit result;
        private final FoodWeaponFinalDamage.OffenseReceipt receipt;
        private final WeaponExpectation expectation;
        private final Thread thread;
        private final ObserverFailures observation = new ObserverFailures();
        private int producerOrdinal = -1;
        private int leafOrdinal = -1;
        private int stepOrdinal = -1;
        private WriterSelection selection;
        private Object producer;
        private Object commit;
        private Object leaf;
        private OwnerPlayerWriter playerWriter;
        private OwnerRepairObservation repairObservation;
        private OwnerWriterStep failedWriterStep;
        private OwnerPlayerStep failedPlayerStep;
        private OwnerPlayerWriter failedPlayerWriter;
        private OwnerRepairObservation failedRepairObservation;
        private final OwnerWriterPublication parentPublication;
        private boolean fieldAdmissionAttempted;
        private boolean fullRepairAttempted;
        private OwnerFieldSequenceBinding fieldSequenceBinding;
        private OwnerFieldExpectedAuthority.Prepared fieldPreparation;
        private final long fieldGeneration = FoodWeaponSourceLedger.nextOwnerFieldGeneration();
        private boolean closed;

        private OwnerWriterPublication(OriginalInteractionBinding originalInteractionBinding, AttemptBatchQuote attemptBatchQuote, AttemptBatchAdmission attemptBatchAdmission, AttemptBatchCommit attemptBatchCommit, FoodWeaponFinalDamage.OffenseReceipt offenseReceipt) {
            this.binding = originalInteractionBinding;
            this.parentPublication = originalInteractionBinding.root.writerPublication;
            originalInteractionBinding.acceptedPublication = this;
            this.batch = attemptBatchQuote;
            this.admission = attemptBatchAdmission;
            this.result = attemptBatchCommit;
            this.receipt = offenseReceipt;
            this.expectation = originalInteractionBinding.root.expectation;
            this.thread = Thread.currentThread();
        }
    }

    static final class OriginalInteractionBinding {
        private final InteractionRoot root;
        private final OriginalInteractionBinding parent;
        private final int depth;
        private final DamageSource source;
        private final Entity target;
        private final CompoundTag snapshot;
        private final int rawAmountBits;
        private final FoodWeaponFinalDamage.ChildHurtCall childCall;
        private boolean closed;
        private OwnerWriterPublication acceptedPublication;
        private FinalListedChildScope acceptedChildScope;

        private OriginalInteractionBinding(InteractionRoot interactionRoot, OriginalInteractionBinding originalInteractionBinding, DamageSource damageSource, Entity entity, CompoundTag compoundTag, float f, FoodWeaponFinalDamage.ChildHurtCall childHurtCall) {
            this.root = interactionRoot;
            this.parent = originalInteractionBinding;
            this.depth = originalInteractionBinding == null ? 1 : originalInteractionBinding.depth + 1;
            this.source = damageSource;
            this.target = entity;
            this.snapshot = compoundTag == null ? null : compoundTag.m_6426_();
            this.rawAmountBits = Float.floatToRawIntBits(f);
            this.childCall = childHurtCall;
        }
    }

    private static final class NativeHurtScope {
        private final NativeIngressReceipt receipt;
        private final NativeCarrierToken carrier;
        private final NativeIngressToken earlyIngress;
        private final NativeIronCallbackToken ironCallback;
        private final Entity target;
        private final DamageSource source;
        private final ServerPlayer player;
        private final long tick;
        private final Thread thread;
        private final CompoundTag snapshot;
        private final WeaponExpectation expectation;
        private InteractionRoot interaction;
        private boolean closed;

        private NativeHurtScope(NativeIngressReceipt nativeIngressReceipt, NativeCarrierToken nativeCarrierToken, Entity entity, DamageSource damageSource, CompoundTag compoundTag) {
            this.receipt = nativeIngressReceipt;
            this.carrier = nativeCarrierToken;
            this.earlyIngress = null;
            this.ironCallback = null;
            this.target = entity;
            this.source = damageSource;
            this.player = nativeIngressReceipt.ingress.player;
            this.expectation = nativeIngressReceipt.ingress.expectation;
            this.tick = FoodWeaponSourceLedger.clock(this.player);
            this.thread = Thread.currentThread();
            this.snapshot = compoundTag.m_6426_();
        }

        private NativeHurtScope(NativeIngressToken nativeIngressToken, Entity entity, DamageSource damageSource, CompoundTag compoundTag) {
            this.receipt = null;
            this.carrier = null;
            this.earlyIngress = nativeIngressToken;
            this.ironCallback = null;
            this.target = entity;
            this.source = damageSource;
            this.player = nativeIngressToken.player;
            this.expectation = nativeIngressToken.expectation;
            this.tick = FoodWeaponSourceLedger.clock(this.player);
            this.thread = Thread.currentThread();
            this.snapshot = compoundTag.m_6426_();
        }

        private NativeHurtScope(NativeIronCallbackToken nativeIronCallbackToken, Entity entity, DamageSource damageSource, CompoundTag compoundTag) {
            this.receipt = null;
            this.carrier = null;
            this.earlyIngress = null;
            this.ironCallback = nativeIronCallbackToken;
            this.target = entity;
            this.source = damageSource;
            this.player = nativeIronCallbackToken.payment.cast.use.player;
            this.expectation = nativeIronCallbackToken.expectation;
            this.tick = FoodWeaponSourceLedger.clock(this.player);
            this.thread = Thread.currentThread();
            this.snapshot = compoundTag.m_6426_();
        }
    }

    public static final class NativeIngressReceipt {
        private final NativeIngressToken ingress;
        private final CompoundTag image;
        private final String proof;

        private NativeIngressReceipt(NativeIngressToken nativeIngressToken, CompoundTag compoundTag, String string) {
            this.ingress = nativeIngressToken;
            this.image = compoundTag.m_6426_();
            this.proof = string;
        }
    }

    private static final class WriterSelection {
        private final Object producer;
        private final int producerOrdinal;
        private final Object commit;
        private final Object leaf;
        private final int leafOrdinal;
        private boolean revoked;

        private WriterSelection(Object object, int n, Object object2, Object object3, int n2) {
            this.producer = object;
            this.producerOrdinal = n;
            this.commit = object2;
            this.leaf = object3;
            this.leafOrdinal = n2;
        }

        private void revoke() {
            this.revoked = true;
        }

        private void observationFailed(ObserverFailures observerFailures, int n, Throwable throwable) {
            this.revoke();
            observerFailures.record(n, throwable);
        }

        private void originalFailed(ObserverFailures observerFailures, Throwable throwable) {
            this.revoke();
            observerFailures.original(throwable);
        }

        private boolean matches(Object object, int n, Object object2, Object object3, int n2) {
            return !this.revoked && this.producer == object && this.producerOrdinal == n && this.commit == object2 && this.leaf == object3 && this.leafOrdinal == n2;
        }
    }

    private static final class ObserverFailures {
        private final Throwable[] phases = new Throwable[4];
        private Throwable original;
        private boolean closed;

        private ObserverFailures() {
        }

        private void record(int n, Throwable throwable) {
            if (n < 0 || n >= this.phases.length || throwable == null || this.phases[n] != null) {
                return;
            }
            this.phases[n] = throwable;
        }

        private Throwable failure(int n) {
            return n < 0 || n >= this.phases.length ? null : this.phases[n];
        }

        private void original(Throwable throwable) {
            if (this.original == null) {
                this.original = throwable;
            }
        }

        private void close() {
            this.closed = true;
        }
    }

    public static final class OwnerWriterStep {
        private final OwnerWriterPublication publication;
        private final OwnerWriterKind kind;
        private final WriterSelection selection;
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
        private final ObserverFailures observation = new ObserverFailures();
        private boolean entered;
        private boolean closed;

        private OwnerWriterStep(OwnerWriterPublication ownerWriterPublication, OwnerWriterKind ownerWriterKind, CompoundTag compoundTag, CompoundTag compoundTag2, String string, Tag tag) {
            this(ownerWriterPublication, ownerWriterKind, compoundTag, compoundTag2, string, tag, null, ownerWriterPublication.selection);
        }

        private OwnerWriterStep(OwnerWriterPublication ownerWriterPublication, OwnerWriterKind ownerWriterKind, CompoundTag compoundTag, CompoundTag compoundTag2, String string, Tag tag, UUID uUID, WriterSelection writerSelection) {
            this.publication = ownerWriterPublication;
            this.selection = writerSelection;
            this.originalUuid = uUID;
            this.ordinal = ownerWriterPublication.stepOrdinal + 1;
            this.kind = ownerWriterKind;
            this.position = ownerWriterPublication.expectation.cursor.position();
            this.transition = ownerWriterPublication.binding.root.interactionChange;
            this.planned = compoundTag;
            this.before = FoodWeaponSourceLedger.canonical((Tag)ownerWriterPublication.expectation.expected);
            this.entryImage = FoodWeaponSourceLedger.canonical((Tag)compoundTag2);
            this.after = FoodWeaponSourceLedger.canonical((Tag)compoundTag);
            this.key = string;
            this.value = tag == null ? null : tag.m_6426_();
        }
    }

    public static final class OwnerPlayerWriter {
        private final OwnerWriterPublication publication;
        private final WriterSelection selection;
        private final ObserverFailures observation = new ObserverFailures();
        private final CompoundTag persistent;
        private final CompoundTag target;
        private final CompoundTag source;
        private final PlayerImagePlan plan;
        private final ImageCursor cursor;
        private final Object transition;
        private FoodSocketPowers.PreparedPack packPrepared;
        private FoodWeaponFinalDamage.PublishingCleanup packCleanup;
        private OwnerPlayerStep active;
        private boolean closed;

        private OwnerPlayerWriter(OwnerWriterPublication ownerWriterPublication, CompoundTag compoundTag, CompoundTag compoundTag2, CompoundTag compoundTag3, PlayerImagePlan playerImagePlan) {
            this(ownerWriterPublication, compoundTag, compoundTag2, compoundTag3, playerImagePlan, ownerWriterPublication.selection);
        }

        private OwnerPlayerWriter(OwnerWriterPublication ownerWriterPublication, CompoundTag compoundTag, CompoundTag compoundTag2, CompoundTag compoundTag3, PlayerImagePlan playerImagePlan, WriterSelection writerSelection) {
            this.publication = ownerWriterPublication;
            this.selection = writerSelection;
            this.persistent = compoundTag;
            this.target = compoundTag2;
            this.source = compoundTag3;
            this.plan = playerImagePlan;
            this.cursor = new ImageCursor(FoodWeaponSourceLedger.canonical((Tag)playerImagePlan.image()));
            this.transition = ownerWriterPublication.binding.root.interactionChange;
        }
    }

    public static final class OwnerPlayerStep {
        private final OwnerPlayerWriter writer;
        private final WriterSelection selection;
        private final PlayerImageDelta delta;
        private final PlayerWriterArgument argument;
        private final CompoundTag receiver;
        private final Object position;
        private final byte[] before;
        private final byte[] planned;
        private final ObserverFailures observation = new ObserverFailures();
        private boolean entered;
        private boolean closed;

        private OwnerPlayerStep(OwnerPlayerWriter ownerPlayerWriter, PlayerImageDelta playerImageDelta) {
            this(ownerPlayerWriter, playerImageDelta, new PlayerWriterArgument(playerImageDelta.rootWrite ? PlayerWriterArgumentKind.ROOT : PlayerWriterArgumentKind.TAG, (Tag)(playerImageDelta.rootWrite ? ownerPlayerWriter.target : playerImageDelta.value), null, null, 0L), playerImageDelta.rootWrite ? ownerPlayerWriter.persistent : ownerPlayerWriter.target, ownerPlayerWriter.selection);
        }

        private OwnerPlayerStep(OwnerPlayerWriter ownerPlayerWriter, PlayerImageDelta playerImageDelta, PlayerWriterArgument playerWriterArgument, CompoundTag compoundTag, WriterSelection writerSelection) {
            this.writer = ownerPlayerWriter;
            this.selection = writerSelection;
            this.delta = playerImageDelta;
            this.argument = playerWriterArgument;
            this.receiver = compoundTag;
            this.position = ownerPlayerWriter.cursor.position();
            this.before = FoodWeaponSourceLedger.canonical((Tag)ownerPlayerWriter.plan.image());
            this.planned = FoodWeaponSourceLedger.canonical((Tag)ownerPlayerWriter.plan.image(playerImageDelta.persistent, playerImageDelta.target));
        }
    }

    public static final class OwnerRepairObservation {
        private final OwnerWriterPublication publication;
        private final WriterSelection selection;
        private final ItemStack actual;
        private final WriterPlanningCut planning;
        private final RepairOriginalTrace trace;
        private final CompoundTag expectedBefore;
        private final Tag expectedForgeCaps;
        private final ObserverFailures observation = new ObserverFailures();
        private boolean closed;

        private OwnerRepairObservation(OwnerWriterPublication ownerWriterPublication, WriterPlanningCut writerPlanningCut, ItemStack itemStack, int n, int n2, int n3) {
            this.publication = ownerWriterPublication;
            this.selection = writerPlanningCut.selection;
            this.actual = itemStack;
            this.planning = writerPlanningCut;
            this.trace = new RepairOriginalTrace(n, n2, n3);
            this.expectedBefore = ownerWriterPublication.expectation.expected.m_6426_();
            this.expectedForgeCaps = this.expectedBefore.m_128423_("ForgeCaps");
        }
    }

    private static final class RepairOriginalTrace {
        private final int getterResult;
        private final int reduction;
        private final int requested;
        private int phase = -1;
        private final Object[] receivers = new Object[8];
        private final int[] arguments = new int[8];
        private boolean closed;

        private RepairOriginalTrace(int n, int n2, int n3) {
            this.getterResult = n;
            this.reduction = n2;
            this.requested = n3;
        }

        private boolean originalRequest() {
            return this.requested == Math.max(0, this.getterResult - this.reduction);
        }

        private boolean record(int n, Object object, int n2) {
            if (this.closed || !this.originalRequest() || n != this.phase + 1 || n >= this.receivers.length || object == null) {
                return false;
            }
            this.receivers[n] = object;
            this.arguments[n] = n2;
            this.phase = n;
            return true;
        }

        private void close() {
            this.closed = true;
        }
    }

    private static final class ReadCut {
        private Object transition = new ReadGeneration();
        private Object acceptedImage;
        private volatile boolean closed;

        private ReadCut() {
        }

        private Object begin() {
            if (this.closed) {
                return this.transition;
            }
            this.acceptedImage = null;
            this.transition = new ReadGeneration();
            return this.transition;
        }

        private void change() {
            ((ReadGeneration)this.transition).invalid = true;
            this.acceptedImage = null;
        }

        private boolean accept(Object object, Object object2) {
            if (this.closed || object != this.transition || ((ReadGeneration)this.transition).invalid || object2 == null) {
                return false;
            }
            this.acceptedImage = object2;
            return true;
        }

        private boolean matches(Object object) {
            return !this.closed && !((ReadGeneration)this.transition).invalid && object != null && this.acceptedImage == object;
        }

        private void close() {
            this.closed = true;
            this.acceptedImage = null;
            ((ReadGeneration)this.transition).invalid = true;
        }
    }

    public static final class NativeObservationCut {
        private final Object receiver;
        private NativeObservationFrame frame;
        private boolean frameBound;
        private final Thread thread;
        private final NativeFailureRecord failure;
        private NativeRootObservation roots;

        private NativeObservationCut(Object object, NativeObservationFrame nativeObservationFrame) {
            this.receiver = object;
            this.frame = nativeObservationFrame;
            this.thread = Thread.currentThread();
            this.failure = new NativeFailureRecord(object);
        }
    }

    private static final class NativeRootObservation {
        private final NativeObservationCut cut;
        private final InteractionRoot root;
        private final WeaponExpectation expectation;
        private final OwnerWriterPublication publication;
        private final OriginalInteractionBinding binding;
        private final NativeRootObservation nextCut;
        private final NativeRootObservation nextRoot;

        private NativeRootObservation(NativeObservationCut nativeObservationCut, InteractionRoot interactionRoot) {
            this.cut = nativeObservationCut;
            this.root = interactionRoot;
            this.expectation = interactionRoot.expectation;
            this.publication = interactionRoot.writerPublication;
            this.binding = interactionRoot.current;
            this.nextCut = nativeObservationCut.roots;
            this.nextRoot = interactionRoot.nativeObservations;
        }
    }

    private static final class NativeFailureRecord {
        private final Object original;
        private final ObserverFailures failures = new ObserverFailures();
        private Object firstFact;
        private boolean failed;

        private NativeFailureRecord(Object object) {
            this.original = object;
        }

        private void fail(Object object, int n, Throwable throwable) {
            this.failed = true;
            if (this.firstFact == null) {
                this.firstFact = object;
            }
            this.failures.record(n, throwable);
        }
    }

    static final class NativeProjectileObservation {
        private final NativeVanillaImpact.RawImpact call;
        private final NativeFailureRecord failure;
        private InteractionRoot root;
        private volatile WeaponExpectation expectation;
        private OwnerWriterPublication publication;
        private OriginalInteractionBinding binding;
        private NativeProjectileInteraction scope;
        private NativeObservationFrame frame;
        private Throwable openingFailure;
        private Throwable closingFailure;
        private boolean begun;
        private boolean finished;

        private NativeProjectileObservation(NativeVanillaImpact.RawImpact rawImpact) {
            this.call = rawImpact;
            this.failure = new NativeFailureRecord(rawImpact);
        }
    }

    static final class NativeProjectileInteraction {
        private final NativeVanillaImpact.RawImpact call;
        private final InteractionRoot root;
        private final OriginalInteractionBinding binding;
        private final NativeProjectileObservation observation;
        private boolean closed;

        private NativeProjectileInteraction(NativeVanillaImpact.RawImpact rawImpact, InteractionRoot interactionRoot) {
            this.call = rawImpact;
            this.root = interactionRoot;
            this.binding = interactionRoot.current;
            this.observation = null;
        }

        private NativeProjectileInteraction(NativeVanillaImpact.RawImpact rawImpact, InteractionRoot interactionRoot, NativeProjectileObservation nativeProjectileObservation) {
            this.call = rawImpact;
            this.root = interactionRoot;
            this.binding = interactionRoot.current;
            this.observation = nativeProjectileObservation;
        }
    }

    private static final class WriterPlanningCut {
        private final WriterSelection selection;
        private final Object position;
        private final Object transition;
        private final int ordinal;

        private WriterPlanningCut(WriterSelection writerSelection, Object object, Object object2, int n) {
            this.selection = writerSelection;
            this.position = object;
            this.transition = object2;
            this.ordinal = n;
        }

        private boolean samePublication(WriterSelection writerSelection, Object object, Object object2, int n) {
            return writerSelection == this.selection && (this.selection == null || !this.selection.revoked) && object == this.position && object2 == this.transition && n == this.ordinal;
        }

        private boolean matches(WriterSelection writerSelection, Object object, Object object2, int n) {
            return this.selection != null && this.samePublication(writerSelection, object, object2, n);
        }
    }

    private static final class PlayerImagePlan {
        private static final List<String> PACK_KEYS = List.of("PackToken", "PackWeapon", "PackWeaponId", "PackHand", "PackDimension", "PackFingerprint", "PackSource", "PackUntil", "PackCooldown");
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

        private PlayerImagePlan(boolean bl, CompoundTag compoundTag, CompoundTag compoundTag2, CompoundTag compoundTag3, boolean bl2, boolean bl3, boolean bl4, CompoundTag compoundTag4) {
            this.wolf = bl;
            this.rootKey = bl ? "FTBossFoodSocketPowers" : "FTBossRangedProcTimers";
            this.persistent = compoundTag.m_6426_();
            this.target = compoundTag2.m_6426_();
            this.source = compoundTag3.m_6426_();
            this.attached = bl2;
            this.sourceAtTarget = bl3;
            this.sourceAtPersistent = bl4;
            this.wolfValues = compoundTag4 == null ? null : compoundTag4.m_6426_();
        }

        private CompoundTag image(CompoundTag compoundTag, CompoundTag compoundTag2) {
            CompoundTag compoundTag3 = new CompoundTag();
            compoundTag3.m_128365_("Persistent", (Tag)compoundTag.m_6426_());
            compoundTag3.m_128365_("Target", (Tag)compoundTag2.m_6426_());
            compoundTag3.m_128365_("Source", (Tag)(this.sourceAtTarget ? compoundTag2 : (this.sourceAtPersistent ? compoundTag : this.source)).m_6426_());
            return compoundTag3;
        }

        private CompoundTag image() {
            return this.image(this.persistent, this.target);
        }

        private PlayerImageDelta key(int n, String string) {
            CompoundTag compoundTag;
            Tag tag;
            if (this.published || n != this.next || string == null) {
                return null;
            }
            if (this.wolf) {
                if (this.next >= PACK_KEYS.size() || !PACK_KEYS.get(this.next).equals(string)) {
                    return null;
                }
                tag = this.wolfValues.m_128423_(string);
            } else {
                CompoundTag compoundTag2 = this.sourceAtTarget ? this.target : (compoundTag = this.sourceAtPersistent ? this.persistent : this.source);
                if (!string.startsWith("Last") || !compoundTag.m_128425_(string, 4)) {
                    return null;
                }
                tag = LongTag.m_128882_((long)Math.max(compoundTag.m_128454_(string), this.target.m_128454_(string)));
            }
            if (tag == null) {
                return null;
            }
            compoundTag = this.target.m_6426_();
            compoundTag.m_128365_(string, tag.m_6426_());
            CompoundTag compoundTag3 = this.persistent.m_6426_();
            if (this.attached) {
                compoundTag3.m_128365_(this.rootKey, (Tag)compoundTag.m_6426_());
            }
            return new PlayerImageDelta(this, compoundTag3, compoundTag, string, tag, false);
        }

        private PlayerImageDelta root(int n) {
            if (this.published || n != this.next || this.wolf && this.next != PACK_KEYS.size()) {
                return null;
            }
            CompoundTag compoundTag = this.persistent.m_6426_();
            compoundTag.m_128365_(this.rootKey, (Tag)this.target.m_6426_());
            return new PlayerImageDelta(this, compoundTag, this.target.m_6426_(), this.rootKey, (Tag)this.target, true);
        }

        private boolean accept(PlayerImageDelta playerImageDelta) {
            if (playerImageDelta == null || playerImageDelta.plan != this || this.published || playerImageDelta.ordinal != this.next) {
                return false;
            }
            this.persistent = playerImageDelta.persistent;
            this.target = playerImageDelta.target;
            ++this.next;
            if (playerImageDelta.rootWrite) {
                this.attached = true;
                this.published = true;
            }
            return true;
        }
    }

    private static final class PlayerWriterArgument {
        private final PlayerWriterArgumentKind kind;
        private final Tag tag;
        private final UUID uuid;
        private final String string;
        private final long number;

        private PlayerWriterArgument(PlayerWriterArgumentKind playerWriterArgumentKind, Tag tag, UUID uUID, String string, long l) {
            this.kind = playerWriterArgumentKind;
            this.tag = tag;
            this.uuid = uUID;
            this.string = string;
            this.number = l;
        }

        private boolean expected(Tag tag) {
            if (tag == null) {
                return false;
            }
            return switch (this.kind) {
                default -> throw new IncompatibleClassChangeError();
                case PlayerWriterArgumentKind.TAG, PlayerWriterArgumentKind.ROOT -> {
                    if (this.tag != null && Arrays.equals(FoodWeaponSourceLedger.canonical(tag), FoodWeaponSourceLedger.canonical(this.tag))) {
                        yield true;
                    }
                    yield false;
                }
                case PlayerWriterArgumentKind.UUID -> {
                    if (this.uuid != null && tag instanceof IntArrayTag && Arrays.equals(FoodWeaponSourceLedger.canonical(tag), FoodWeaponSourceLedger.canonical((Tag)NbtUtils.m_129226_((UUID)this.uuid)))) {
                        yield true;
                    }
                    yield false;
                }
                case PlayerWriterArgumentKind.STRING -> {
                    if (this.string != null && tag instanceof StringTag && this.string.equals(tag.m_7916_())) {
                        yield true;
                    }
                    yield false;
                }
                case PlayerWriterArgumentKind.LONG -> {
                    LongTag var2_2;
                    yield tag instanceof LongTag && (var2_2 = (LongTag)tag).m_7046_() == this.number;
                }
            };
        }

        private boolean originalTag(Tag tag) {
            return (this.kind == PlayerWriterArgumentKind.TAG || this.kind == PlayerWriterArgumentKind.ROOT) && tag != null && tag == this.tag;
        }

        private boolean originalUuid(UUID uUID) {
            return this.kind == PlayerWriterArgumentKind.UUID && uUID != null && uUID == this.uuid;
        }

        private boolean originalString(String string) {
            return this.kind == PlayerWriterArgumentKind.STRING && this.string != null && this.string.equals(string);
        }

        private boolean originalLong(long l) {
            return this.kind == PlayerWriterArgumentKind.LONG && l == this.number;
        }
    }

    private static enum PlayerWriterArgumentKind {
        TAG,
        UUID,
        STRING,
        LONG,
        ROOT;

    }

    private static final class PlayerImageDelta {
        private final PlayerImagePlan plan;
        private final int ordinal;
        private final CompoundTag persistent;
        private final CompoundTag target;
        private final String key;
        private final Tag value;
        private final boolean rootWrite;

        private PlayerImageDelta(PlayerImagePlan playerImagePlan, CompoundTag compoundTag, CompoundTag compoundTag2, String string, Tag tag, boolean bl) {
            this.plan = playerImagePlan;
            this.ordinal = playerImagePlan.next;
            this.persistent = compoundTag;
            this.target = compoundTag2;
            this.key = string;
            this.value = tag.m_6426_();
            this.rootWrite = bl;
        }
    }

    private static final class ServerState {
        private final UUID epoch = UUID.randomUUID();
        private final byte[] signingKey = new byte[32];
        private final Map<UUID, OwnerState> ownerEpochs = Collections.synchronizedMap(new LinkedHashMap());
        private volatile Map<Claim, Commit> claims = new LinkedHashMap<Claim, Commit>();
        private volatile long latestTick = Long.MIN_VALUE;
        private volatile boolean closed;
        private volatile OwnerLineageRevocationStore revocations;
        private boolean storeInitialized;

        private ServerState(MinecraftServer minecraftServer) {
            new SecureRandom().nextBytes(this.signingKey);
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        private void initializeStore(MinecraftServer minecraftServer) {
            ServerState serverState = this;
            synchronized (serverState) {
                OwnerLineageRevocationStore ownerLineageRevocationStore;
                if (this.storeInitialized) {
                    return;
                }
                try {
                    ownerLineageRevocationStore = OwnerLineageRevocationStore.open(minecraftServer);
                }
                catch (IOException iOException) {
                    ownerLineageRevocationStore = null;
                }
                this.revocations = ownerLineageRevocationStore;
                this.storeInitialized = true;
            }
        }
    }

    public static final class NativeEvergaleAction {
        private final NativeEvergaleBridge.RawAction original;
        private final Thread thread = Thread.currentThread();
        private final ArrayList<NativeEvergaleSpawnWitness> witnesses = new ArrayList();
        private boolean rejected;
        private boolean closed;
        private Throwable originalFailure;
        private Throwable observationFailure;
        private String refusal;

        private NativeEvergaleAction(NativeEvergaleBridge.RawAction rawAction) {
            this.original = rawAction;
        }
    }

    private static final class NativeEvergaleSpawnWitness {
        private final NativeEvergaleBridge.RawSpawn spawn;
        private final NativeEvergaleBridge.RawOwnerAssignment owner;
        private final NativeEvergaleBridge.RawCooldown cooldown;

        private NativeEvergaleSpawnWitness(NativeEvergaleBridge.RawSpawn rawSpawn, NativeEvergaleBridge.RawOwnerAssignment rawOwnerAssignment, NativeEvergaleBridge.RawCooldown rawCooldown) {
            this.spawn = rawSpawn;
            this.owner = rawOwnerAssignment;
            this.cooldown = rawCooldown;
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

        private NativeEvergaleInteraction(NativeEvergaleImpact.RawImpact rawImpact) {
            this.original = rawImpact;
        }
    }

    private static final class OwnerFieldSequenceBinding {
        private final OwnerWriterPublication publication;
        private final FoodWeaponOriginalSequence.Session sequence;
        private final OwnerFieldExpectedAuthority.Prepared prepared;
        private final Thread thread = Thread.currentThread();
        private final WriterSelection selection;
        private boolean extraAuthority;
        private boolean terminal;

        private OwnerFieldSequenceBinding(OwnerWriterPublication ownerWriterPublication, FoodWeaponOriginalSequence.Session session, OwnerFieldExpectedAuthority.Prepared prepared) {
            this.publication = ownerWriterPublication;
            this.sequence = session;
            this.prepared = prepared;
            this.selection = ownerWriterPublication.selection;
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
        private FoodWeaponExpectedTrace.Value getterFields;
        private int getterValue;
        private int phase;
        private boolean closed;

        private OriginalRepairCall(Object object, Object object2, Object object3, ItemStack itemStack, FoodWeaponHitPlan.OriginalRepairFacts originalRepairFacts, OwnerFullRepairGuard.Cut cut) {
            this.commit = object;
            this.bridge = object2;
            this.leaf = object3;
            this.actual = itemStack;
            this.facts = originalRepairFacts;
            this.cut = cut;
        }
    }

    public static final class AttemptBatchQuote {
        private final OriginalInteractionBinding binding;
        private final AttemptBatchStatus status;
        private final List<AttemptQuote> entries;
        private final List<TargetQuote> targets;
        private final String actionSignature;
        private final long quotedAt;

        private AttemptBatchQuote(AttemptBatchStatus attemptBatchStatus, List<AttemptQuote> list, List<TargetQuote> list2, String string, long l, OriginalInteractionBinding originalInteractionBinding) {
            this.binding = originalInteractionBinding;
            this.status = attemptBatchStatus;
            this.entries = List.copyOf(list);
            this.targets = List.copyOf(list2);
            this.actionSignature = string;
            this.quotedAt = l;
        }

        public AttemptBatchStatus status() {
            return this.status;
        }

        public List<AttemptQuote> entries() {
            return this.entries;
        }

        public List<TargetQuote> targets() {
            return this.targets;
        }

        public long quotedAt() {
            return this.quotedAt;
        }
    }

    public static final class AttemptBatchAdmission {
        private final AttemptBatchAdmissionStatus status;
        private final BatchPreparation preparation;

        private AttemptBatchAdmission(BatchPreparation batchPreparation) {
            this.preparation = batchPreparation;
            this.status = switch (batchPreparation.result.status) {
                case AttemptBatchCommitStatus.COMMITTED -> AttemptBatchAdmissionStatus.READY;
                case AttemptBatchCommitStatus.ALREADY_COMMITTED -> AttemptBatchAdmissionStatus.ALREADY_COMMITTED;
                case AttemptBatchCommitStatus.CAPACITY_BLOCKED -> AttemptBatchAdmissionStatus.CAPACITY_BLOCKED;
                case AttemptBatchCommitStatus.CONFLICT -> AttemptBatchAdmissionStatus.CONFLICT;
                default -> AttemptBatchAdmissionStatus.INVALID;
            };
        }

        public AttemptBatchAdmissionStatus status() {
            return this.status;
        }
    }

    public static final class AttemptBatchCommit {
        private final AttemptBatchCommitStatus status;
        private final List<AttemptResult> entries;
        private final List<TargetResult> targets;
        private final AttemptBatchQuote batch;

        private AttemptBatchCommit(AttemptBatchCommitStatus attemptBatchCommitStatus, List<AttemptResult> list, List<TargetResult> list2, AttemptBatchQuote attemptBatchQuote) {
            this.status = attemptBatchCommitStatus;
            this.entries = List.copyOf(list);
            this.targets = List.copyOf(list2);
            this.batch = attemptBatchQuote;
        }

        public AttemptBatchCommitStatus status() {
            return this.status;
        }

        public List<AttemptResult> entries() {
            return this.entries;
        }

        public List<TargetResult> targets() {
            return this.targets;
        }

        public Optional<AttemptResult> attempt(AttemptDomain attemptDomain) {
            return this.entries.stream().filter(attemptResult -> attemptDomain != null && attemptResult.domain == attemptDomain).findFirst();
        }

        public Optional<TargetResult> target(UUID uUID, TargetEffectDomain targetEffectDomain) {
            return this.targets.stream().filter(targetResult -> uUID != null && targetEffectDomain != null && targetResult.target.equals(uUID) && targetResult.domain == targetEffectDomain).findFirst();
        }
    }

    private static final class ReadAliases {
        private static final int LIMIT = 256;
        private final Object[] originals = new Object[256];
        private int count;
        private boolean saturated;

        private ReadAliases() {
        }

        private boolean contains(Object object) {
            if (object == null) {
                return false;
            }
            for (int i = 0; i < this.count; ++i) {
                if (this.originals[i] != object) continue;
                return true;
            }
            return false;
        }

        private boolean bind(Object object) {
            if (object == null || this.saturated) {
                return false;
            }
            if (this.contains(object)) {
                return true;
            }
            if (this.count == 256) {
                this.saturated = true;
                return false;
            }
            this.originals[this.count++] = object;
            return true;
        }

        private void clear() {
            for (int i = 0; i < this.count; ++i) {
                this.originals[i] = null;
            }
            this.count = 0;
        }
    }

    private static final class PrimaryReadWindow {
        private final Object repairActual;
        private final Object repairGeneration;
        private final OwnerLineageRevocationStore.Lease repairLease;
        private final InteractionRoot root;
        private final OriginalInteractionBinding binding;
        private final Object transition;

        private PrimaryReadWindow(InteractionRoot interactionRoot) {
            this.root = interactionRoot;
            this.binding = interactionRoot.current;
            this.transition = interactionRoot.readCut.begin();
            this.repairActual = interactionRoot.expectation == null ? null : interactionRoot.expectation.actual;
            this.repairGeneration = interactionRoot.expectation == null ? null : interactionRoot.expectation.repairGeneration;
            this.repairLease = OwnerFullRepairGuard.lease(this.repairActual);
        }
    }

    private static final class BatchPreparation {
        private final ServerState state;
        private final Map<Claim, Commit> originalClaims;
        private final Map<Claim, Commit> claimsImage;
        private final long latestTick;
        private final Map<Claim, Commit> replacement;
        private final AttemptBatchCommit result;

        private BatchPreparation(ServerState serverState, Map<Claim, Commit> map, AttemptBatchCommit attemptBatchCommit) {
            this.state = serverState;
            this.originalClaims = serverState == null ? null : serverState.claims;
            this.claimsImage = serverState == null ? Map.of() : Map.copyOf(serverState.claims);
            this.latestTick = serverState == null ? Long.MIN_VALUE : serverState.latestTick;
            this.replacement = map;
            this.result = attemptBatchCommit;
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

    static enum OwnerWriterKind {
        EVOLUTION_TIMER,
        WOLF_TOKEN;

    }

    private static final class NativeOperation {
        private final Object repairGeneration;
        private final NativeKind kind;
        private final ServerPlayer player;
        private final ItemStack weapon;
        private final InteractionHand hand;
        private final MinecraftServer server;
        private final UUID epoch;
        private final UUID ownerEpoch;
        private final Thread thread = Thread.currentThread();
        private final long tick;
        private final String dimension;
        private final List<NativeReceipt> receipts = new ArrayList<NativeReceipt>();
        private NativeReceipt current;
        private AbstractArrow factoryEntity;
        private boolean invalid;
        private NativeMaterialCapture materialCapture;
        private boolean materialAttempted;
        private boolean materialRejected;

        private NativeOperation(NativeKind nativeKind, ServerPlayer serverPlayer, ItemStack itemStack, InteractionHand interactionHand) {
            this.kind = nativeKind;
            this.player = serverPlayer;
            this.weapon = itemStack;
            this.repairGeneration = OwnerRepairGenerationAuthority.current(itemStack);
            this.hand = interactionHand;
            this.server = FoodWeaponSourceLedger.server(serverPlayer);
            this.epoch = FoodWeaponSourceLedger.state((MinecraftServer)this.server).epoch;
            this.ownerEpoch = FoodWeaponSourceLedger.ownerEpoch(FoodWeaponSourceLedger.state(this.server), serverPlayer.m_20148_());
            this.tick = FoodWeaponSourceLedger.clock(serverPlayer);
            this.dimension = serverPlayer.m_9236_().m_46472_().m_135782_().toString();
        }
    }

    private record TicketState(SourceTicket ticket, ItemStack actualWeapon, MinecraftServer server, String foundationRules) {
    }

    private static enum NativeKind {
        BOW,
        CROSSBOW,
        TRIDENT;

    }

    private static final class NativeReceipt {
        private final UUID token = UUID.randomUUID();
        private final NativeOperation operation;
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
        private NativeMaterialPublication material;
        private boolean materialAttempted;
        private boolean materialRejected;
        private NativeMaterialTransfer materialTransfer;

        private NativeReceipt(NativeOperation nativeOperation, Entity entity) {
            this.operation = nativeOperation;
            this.entity = entity;
            this.before = nativeOperation.weapon.m_41739_(new CompoundTag()).m_6426_();
            this.rules = FoodWeaponSourceLedger.foundationRules(nativeOperation.player, nativeOperation.weapon);
        }
    }

    public static final class TridentCarrierLineage {
        private final ThrownTrident entity;
        private final ItemStack input;
        private final ItemStack copied;
        private final Object origin;
        private boolean observationRejected;
        private Throwable observationFailure;

        private TridentCarrierLineage(ThrownTrident thrownTrident, ItemStack itemStack, ItemStack itemStack2, Object object) {
            this.entity = thrownTrident;
            this.input = itemStack;
            this.copied = itemStack2;
            this.origin = object;
        }
    }

    public static final class NativePaidWeapon {
        private final NativeReceipt receipt;
        private final Entity carrier;
        private final WeaponExpectation expectation;
        private NativeMaterialPublication material;
        private OwnerNativeMaterialImage combined;

        private NativePaidWeapon(NativeReceipt nativeReceipt, CompoundTag compoundTag) {
            this.receipt = nativeReceipt;
            this.carrier = nativeReceipt.entity;
            this.expectation = new WeaponExpectation(nativeReceipt.operation.weapon, compoundTag);
        }

        private NativePaidWeapon(NativeReceipt nativeReceipt, CompoundTag compoundTag, NativeMaterialPublication nativeMaterialPublication, OwnerNativeMaterialImage ownerNativeMaterialImage) {
            this(nativeReceipt, compoundTag);
            this.material = nativeMaterialPublication;
            this.combined = ownerNativeMaterialImage;
        }
    }

    private static final class TridentTicketBefore {
        private final SourceTicket ticket;
        private final ItemStack actual;
        private final OwnerNativeMaterialImage expected;
        private final Thread thread = Thread.currentThread();
        private boolean observationRejected;

        private TridentTicketBefore(SourceTicket sourceTicket, ItemStack itemStack, CompoundTag compoundTag) {
            this.ticket = sourceTicket;
            this.actual = itemStack;
            this.expected = OwnerNativeMaterialImage.freeze(compoundTag);
        }
    }

    public record SourceTicket(UUID owner, UUID weaponId, InteractionHand originalHand, String profile, String repairProfile, String socketFingerprint, String materialFingerprint, UUID actionId, String dimension, long issuedAt, long expiresAt, UUID serverEpoch, UUID ownerEpoch, int preWearDamage, int maxDamage) {
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

    static final class PendingEchoInteraction {
        private final FoodSocketAttributePowers.PendingEchoDispatch dispatch;
        private final PendingEchoLineage lineage;
        private final InteractionRoot root;
        private final OriginalInteractionBinding binding;
        private final ServerPlayer player;
        private final LivingEntity target;
        private final DamageSource source;
        private final float rawAmount;
        private boolean hurtEntered;
        private boolean hurtActive;
        private boolean hurtFinished;
        private boolean closed;

        private PendingEchoInteraction(FoodSocketAttributePowers.PendingEchoDispatch pendingEchoDispatch, InteractionRoot interactionRoot, ServerPlayer serverPlayer, LivingEntity livingEntity, DamageSource damageSource, float f) {
            this(pendingEchoDispatch, interactionRoot, serverPlayer, livingEntity, damageSource, f, null);
        }

        private PendingEchoInteraction(FoodSocketAttributePowers.PendingEchoDispatch pendingEchoDispatch, InteractionRoot interactionRoot, ServerPlayer serverPlayer, LivingEntity livingEntity, DamageSource damageSource, float f, PendingEchoLineage pendingEchoLineage) {
            this.dispatch = pendingEchoDispatch;
            this.lineage = pendingEchoLineage;
            this.root = interactionRoot;
            this.binding = interactionRoot.current;
            this.player = serverPlayer;
            this.target = livingEntity;
            this.source = damageSource;
            this.rawAmount = f;
        }
    }

    static final class PendingEchoLineage {
        private final FoodWeaponEchoQueue.Entry<?> offeredEntry;
        private final MinecraftServer server;
        private final ItemStack originalActual;
        private final CompoundTag snapshot;
        private final UUID target;
        private final long offeredAt;
        private final long dueAt;
        private final double rangeSquared;
        private final String purpose;
        private boolean dispatched;

        private PendingEchoLineage(FoodWeaponEchoQueue.Entry<?> entry, MinecraftServer minecraftServer, CompoundTag compoundTag, UUID uUID, long l, long l2, double d) {
            this(entry, minecraftServer, compoundTag, uUID, l, l2, d, null);
        }

        private PendingEchoLineage(FoodWeaponEchoQueue.Entry<?> entry, MinecraftServer minecraftServer, CompoundTag compoundTag, UUID uUID, long l, long l2, double d, ItemStack itemStack) {
            this.offeredEntry = entry;
            this.server = minecraftServer;
            this.originalActual = itemStack;
            this.snapshot = compoundTag.m_6426_();
            this.target = uUID;
            this.offeredAt = l;
            this.dueAt = l2;
            this.rangeSquared = d;
            this.purpose = compoundTag.m_128461_("DamagePurpose");
        }
    }

    static final class FinalListedChildScope {
        private final FoodWeaponFinalDamage.ChildHurtCall call;
        private final OriginalInteractionBinding parent;
        private final OriginalInteractionBinding binding;
        private final InteractionRoot root;
        private final DamageSource source;
        private final LivingEntity target;
        private final float rawAmount;
        private final Thread thread;
        private FoodWeaponConsumerSequence.OriginalChild originalConsumerChild;
        private OwnerWriterPublication originalPublisher;
        private boolean hurtEntered;
        private boolean hurtActive;
        private boolean closed;

        private FinalListedChildScope(FoodWeaponFinalDamage.ChildHurtCall childHurtCall, OriginalInteractionBinding originalInteractionBinding, DamageSource damageSource, LivingEntity livingEntity, float f) {
            this.call = childHurtCall;
            this.parent = originalInteractionBinding;
            this.root = originalInteractionBinding.root;
            this.source = damageSource;
            this.target = livingEntity;
            this.rawAmount = f;
            this.thread = Thread.currentThread();
            this.binding = new OriginalInteractionBinding(this.root, originalInteractionBinding, damageSource, (Entity)livingEntity, originalInteractionBinding.snapshot, f, childHurtCall);
            this.binding.acceptedChildScope = this;
        }
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

    public static final class AttemptResult {
        private final AttemptDomain domain;
        private final AttemptCommit commit;

        private AttemptResult(AttemptDomain attemptDomain, AttemptCommit attemptCommit) {
            this.domain = attemptDomain;
            this.commit = attemptCommit;
        }

        public AttemptDomain domain() {
            return this.domain;
        }

        public AttemptCommit commit() {
            return this.commit;
        }
    }

    public static enum AttemptCommit {
        INVALID,
        NOT_SETTLED,
        ALREADY_FAILURE,
        ALREADY_SUCCESS,
        COMMITTED_FAILURE,
        COMMITTED_SUCCESS;

    }

    public static final class NativeMaterialCapture {
        private final NativeOperation operation;
        private CompoundTag originalReturnedSnapshot;
        private Throwable originalFailure;
        private OwnerNativeMaterialImage image;
        private boolean failed;

        private NativeMaterialCapture(NativeOperation nativeOperation) {
            this.operation = nativeOperation;
        }
    }

    public static final class NativeMaterialTransfer {
        private final NativeReceipt receipt;
        private final NativeMaterialCapture capture;
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

        private NativeMaterialTransfer(NativeReceipt nativeReceipt, NativeMaterialCapture nativeMaterialCapture, Entity entity) {
            this.receipt = nativeReceipt;
            this.capture = nativeMaterialCapture;
            this.entity = entity;
            this.capturedReturn = nativeMaterialCapture.originalReturnedSnapshot;
        }
    }

    public static final class NativeMaterialPublication {
        private final NativeReceipt receipt;
        private final NativeMaterialCapture capture;
        private final Entity entity;
        private final CompoundTag receiver;
        private final String key;
        private final CompoundTag originalSnapshot;
        private final CompoundTag originalPutValue;
        private final UUID entityId;
        private NativeMaterialTransfer transfer;
        private OwnerNativeMaterialImage image;
        private NativeMaterialPhase phase = NativeMaterialPhase.OPEN;
        private NativePaidWeapon paid;
        private Throwable originalFailure;
        private Throwable observationFailure;

        private NativeMaterialPublication(NativeReceipt nativeReceipt, NativeMaterialCapture nativeMaterialCapture, Entity entity, CompoundTag compoundTag, String string, CompoundTag compoundTag2, CompoundTag compoundTag3, UUID uUID) {
            this.receipt = nativeReceipt;
            this.capture = nativeMaterialCapture;
            this.entity = entity;
            this.receiver = compoundTag;
            this.key = string;
            this.originalSnapshot = compoundTag2;
            this.originalPutValue = compoundTag3;
            this.entityId = uUID;
        }
    }

    private static enum NativeMaterialPhase {
        OPEN,
        RETURNED,
        CLAIMED,
        PUBLISHED,
        REJECTED;

    }

    public static final class NativeIronCastToken {
        private final NativeIronUseToken use;
        private final Object spell;
        private final Object castSource;
        private final String source;
        private final String slot;
        private final int selectedLevel;
        private final UUID action = UUID.randomUUID();
        private boolean accepted;
        private boolean invalid;

        private NativeIronCastToken(NativeIronUseToken nativeIronUseToken, Object object, Object object2, String string, int n) {
            this.use = nativeIronUseToken;
            this.spell = object;
            this.castSource = object2;
            this.source = ((Enum)object2).name();
            this.slot = string;
            this.selectedLevel = n;
        }
    }

    public static final class NativeIronPaymentToken {
        private final NativeIronCastToken cast;
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

        private NativeIronPaymentToken(NativeIronCastToken nativeIronCastToken, Object object, Object object2, int n, int n2, float f) {
            this.cast = nativeIronCastToken;
            this.magicData = object;
            this.event = object2;
            this.level = n;
            this.manaCost = n2;
            this.manaBefore = f;
            this.tick = FoodWeaponSourceLedger.clock(nativeIronCastToken.use.player);
        }
    }

    public static final class NativeIronReceipt {
        private final NativeIronPaymentToken payment;
        private final CompoundTag image;
        private final String proof;

        private NativeIronReceipt(NativeIronPaymentToken nativeIronPaymentToken, CompoundTag compoundTag, String string) {
            this.payment = nativeIronPaymentToken;
            this.image = compoundTag.m_6426_();
            this.proof = string;
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

        private NativeIngressKind(String string2, String string3, int n2) {
            this.itemClass = string2;
            this.registryId = string3;
            this.wear = n2;
        }
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

    private record Claim(UUID owner, UUID attack, DomainKind kind, int domain, UUID target) {
    }

    private static enum DomainKind {
        CLAIM,
        ATTEMPT,
        TARGET;

    }

    private record Commit(long expiresAt, double chance, boolean success) {
    }

    public static final class AttemptQuote {
        private final OriginalInteractionBinding binding;
        private final AttemptStatus status;
        private final AttemptDomain domain;
        private final double chance;
        private final double sample;
        private final boolean success;
        private final String proof;

        private AttemptQuote(AttemptStatus attemptStatus, AttemptDomain attemptDomain, double d, double d2, boolean bl, String string, OriginalInteractionBinding originalInteractionBinding) {
            this.binding = originalInteractionBinding;
            this.status = attemptStatus;
            this.domain = attemptDomain;
            this.chance = d;
            this.sample = d2;
            this.success = bl;
            this.proof = string;
        }

        public AttemptStatus status() {
            return this.status;
        }

        public AttemptDomain domain() {
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

    public static enum AttemptStatus {
        INVALID,
        AVAILABLE,
        COMMITTED_FAILURE,
        COMMITTED_SUCCESS;

    }

    public static enum AttemptBatchStatus {
        INVALID,
        READY,
        CAPACITY_BLOCKED;

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

    public record AttemptRequest(AttemptDomain domain, double chance) {
    }

    public record TargetRequest(UUID target, TargetEffectDomain domain) {
    }

    public static final class TargetQuote {
        private final UUID target;
        private final TargetEffectDomain domain;
        private final TargetStatus status;

        private TargetQuote(UUID uUID, TargetEffectDomain targetEffectDomain, TargetStatus targetStatus) {
            this.target = uUID;
            this.domain = targetEffectDomain;
            this.status = targetStatus;
        }

        public UUID target() {
            return this.target;
        }

        public TargetEffectDomain domain() {
            return this.domain;
        }

        public TargetStatus status() {
            return this.status;
        }
    }

    public static enum TargetStatus {
        AVAILABLE,
        COMMITTED;

    }

    public static final class TargetResult {
        private final UUID target;
        private final TargetEffectDomain domain;
        private final TargetCommit commit;

        private TargetResult(UUID uUID, TargetEffectDomain targetEffectDomain, TargetCommit targetCommit) {
            this.target = uUID;
            this.domain = targetEffectDomain;
            this.commit = targetCommit;
        }

        public UUID target() {
            return this.target;
        }

        public TargetEffectDomain domain() {
            return this.domain;
        }

        public TargetCommit commit() {
            return this.commit;
        }
    }

    public static enum TargetCommit {
        ALREADY_COMMITTED,
        COMMITTED;

    }

    public static enum AttemptBatchAdmissionStatus {
        INVALID,
        READY,
        ALREADY_COMMITTED,
        CAPACITY_BLOCKED,
        CONFLICT;

    }

    private static final class ReadGeneration {
        private boolean invalid;

        private ReadGeneration() {
        }
    }
}

