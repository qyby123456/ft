package net.farmingtales.ftboss.boss.ai;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.farmingtales.ftboss.boss.ManorFight;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.skill.ManorDomainPacket;
import net.farmingtales.ftboss.util.ManorDamageRules;
import net.farmingtales.ftboss.util.ManorDomainEffectRules;
import net.farmingtales.ftboss.util.ManorDomainRules;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;
import net.neoforged.neoforge.event.tick.ServerTickEvent.Post;
import net.farmingtales.ftboss.event.NativeWeaponDamageEvent.BeforeHealth;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingHealEvent;
import net.farmingtales.ftboss.event.NativeWeaponDamageEvent.BeforeMitigation;
import net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerChangedDimensionEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent;
import net.neoforged.neoforge.event.level.LevelEvent.Unload;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.EventBusSubscriber.Bus;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(
    modid = "ftboss"
)
public final class ManorGroundSupport {
    private static final ResourceLocation ARMOR = ResourceLocation.fromNamespaceAndPath("ftboss", "manor_domain/a4c82f12-85d1-4c92-a7c6-e32a54f97511");
    private static final ResourceLocation SPEED = ResourceLocation.fromNamespaceAndPath("ftboss", "manor_domain/a4c82f12-85d1-4c92-a7c6-e32a54f97512");
    private static final ResourceLocation STABILITY = ResourceLocation.fromNamespaceAndPath("ftboss", "manor_domain/a4c82f12-85d1-4c92-a7c6-e32a54f97513");
    private static final ResourceLocation ATTACK = ResourceLocation.fromNamespaceAndPath("ftboss", "manor_domain/a4c82f12-85d1-4c92-a7c6-e32a54f97514");
    private static final ResourceLocation PLAYER_ARMOR = ResourceLocation.fromNamespaceAndPath("ftboss", "manor_domain/a4c82f12-85d1-4c92-a7c6-e32a54f97521");
    private static final ResourceLocation PLAYER_SPEED = ResourceLocation.fromNamespaceAndPath("ftboss", "manor_domain/a4c82f12-85d1-4c92-a7c6-e32a54f97522");
    private static final ResourceLocation PLAYER_ATTACK = ResourceLocation.fromNamespaceAndPath("ftboss", "manor_domain/a4c82f12-85d1-4c92-a7c6-e32a54f97524");
    private static final Map<UUID, ManorGroundSupport.State> ACTIVE = new HashMap<>();
    private static final Map<UUID, ManorGroundSupport.State> INTRODUCED = new HashMap<>();
    private static final Map<UUID, ManorGroundSupport.PlayerState> PLAYERS = new HashMap<>();
    private static final Map<UUID, Long> LEECH_AT = new HashMap<>();
    private static final List<ManorGroundSupport.PendingLeech> PENDING = new ArrayList<>();
    private static long clock;

    private ManorGroundSupport() {
    }

    private static boolean eligible(EasterBossEntity var0, UUID var1) {
        return var1 != null
            && ManorFight.hasActive(var1)
            && var0.isAlive()
            && !var0.isRemoved()
            && !var0.isMini()
            && !var0.isGiant()
            && ManorDomainRules.supports(var0.getVariant())
            && var0.getUUID().equals(ManorFight.currentBoss(var1))
            && !var0.isManorIntroHidden()
            && !var0.isNoAi();
    }

    public static void tick(EasterBossEntity var0) {
        if (var0.level() instanceof ServerLevel var1) {
            UUID var8 = ManorFight.fightId(var0);
            Vec3 var3 = var8 == null ? null : ManorFight.anchorOf(var8);
            if (eligible(var0, var8) && var3 != null) {
                ManorGroundSupport.State var4 = ACTIVE.get(var0.getUUID());
                if (var4 == null
                    || var4.variant != var0.getVariant()
                    || var4.level != var1
                    || !var4.fight.equals(var8)
                    || var4.anchor.distanceToSqr(var3) > 0.01) {
                    stop(var1, var0);
                    BlockHitResult var5 = var1.clip(new ClipContext(var3.add(0.0, 0.5, 0.0), var3.add(0.0, -8.0, 0.0), Block.COLLIDER, Fluid.NONE, var0));
                    Vec3 var6 = var5.getType() == Type.MISS ? var3 : var5.getLocation();
                    var4 = new ManorGroundSupport.State(var1, var0, var8, var3, var6);
                    var4.healAt = var1.getGameTime() + 100L;
                    ACTIVE.put(var0.getUUID(), var4);
                }

                var4.seen = clock;
                boolean var9 = contains(var4, var0);
                if (var4.inside != var9 || var1.getGameTime() >= var4.refresh) {
                    var4.inside = var9;
                    updateAttributes(var0, var9);
                    send(var4, var4.variant);

                    for (ServerPlayer var7 : var1.players()) {
                        if (var7.distanceToSqr(var4.center) <= 9216.0 && ManorLivingTargets.valid(var0, var7)) {
                            announce(var4, var7);
                        }
                    }

                    var4.refresh = var1.getGameTime() + 20L;
                }

                if (var9 && var4.variant == 11 && var1.getGameTime() >= var4.healAt) {
                    float var11 = ManorDomainEffectRules.prayer(var0.getHealth(), var0.getMaxHealth(), false);
                    if (var11 > 0.0F) {
                        var0.heal(var11);
                    }

                    var4.healAt = var1.getGameTime() + 100L;
                }
            } else {
                stop(var1, var0);
            }
        }
    }

    private static boolean contains(ManorGroundSupport.State var0, Entity var1) {
        if (var1.level() != var0.level) {
            return false;
        } else {
            Vec3 var2 = var1.position().subtract(var0.boss.position());
            return ManorDomainRules.contains(var2.x, var2.y, var2.z);
        }
    }

    private static boolean live(ManorGroundSupport.State var0) {
        return clock - var0.seen <= 2L && eligible(var0.boss, var0.fight);
    }

    private static ManorGroundSupport.State state(EasterBossEntity var0) {
        ManorGroundSupport.State var1 = ACTIVE.get(var0.getUUID());
        return var1 != null && live(var1) && contains(var1, var0) ? var1 : null;
    }

    private static ManorGroundSupport.State playerDomain(ServerPlayer var0) {
        ManorGroundSupport.State var1 = null;
        double var2 = Double.POSITIVE_INFINITY;

        for (ManorGroundSupport.State var5 : ACTIVE.values()) {
            if (live(var5) && contains(var5, var0) && ManorLivingTargets.valid(var5.boss, var0)) {
                double var6 = var0.distanceToSqr(var5.boss.position());
                if (var6 < var2 || var6 == var2 && var1 != null && var5.boss.getUUID().compareTo(var1.boss.getUUID()) < 0) {
                    var1 = var5;
                    var2 = var6;
                }
            }
        }

        return var1;
    }

    private static void updateAttributes(EasterBossEntity var0, boolean var1) {
        int var2 = var0.getVariant();
        modifier(var0, Attributes.ARMOR, ARMOR, var1 ? ManorDomainRules.armor(var2) : 0.0, Operation.ADD_VALUE);
        modifier(var0, Attributes.MOVEMENT_SPEED, SPEED, var1 ? ManorDomainEffectRules.bossSpeed(var2) : 0.0, Operation.ADD_MULTIPLIED_TOTAL);
        modifier(var0, Attributes.KNOCKBACK_RESISTANCE, STABILITY, var1 ? ManorDomainRules.knockbackResistance(var2) : 0.0, Operation.ADD_VALUE);
        modifier(var0, Attributes.ATTACK_DAMAGE, ATTACK, var1 ? ManorDomainRules.attack(var2) : 0.0, Operation.ADD_MULTIPLIED_TOTAL);
    }

    private static void playerAttributes(ServerPlayer var0, ManorGroundSupport.State var1) {
        int var2 = var1 == null ? -1 : var1.variant;
        modifier(var0, Attributes.ARMOR, PLAYER_ARMOR, ManorDomainEffectRules.playerArmor(var2), Operation.ADD_VALUE);
        modifier(var0, Attributes.MOVEMENT_SPEED, PLAYER_SPEED, ManorDomainEffectRules.playerSpeed(var2), Operation.ADD_MULTIPLIED_TOTAL);
        modifier(var0, Attributes.ATTACK_DAMAGE, PLAYER_ATTACK, ManorDomainEffectRules.playerAttack(var2), Operation.ADD_MULTIPLIED_TOTAL);
    }

    private static void modifier(LivingEntity var0, Holder<Attribute> var1, ResourceLocation var2, double var3, Operation var5) {
        AttributeInstance var6 = var0.getAttribute(var1);
        if (var6 != null) {
            AttributeModifier var7 = var6.getModifier(var2);
            if (var7 == null || var7.amount() != var3 || var7.operation() != var5) {
                var6.removeModifier(var2);
                if (var3 != 0.0) {
                    var6.addTransientModifier(new AttributeModifier(var2, var3, var5));
                }
            }
        }
    }

    private static void send(ManorGroundSupport.State var0, int var1) {
        ManorDomainPacket var2 = new ManorDomainPacket(var0.level.dimension().location(), var0.boss.getUUID(), var0.boss.getId(), var0.center, var1, var1 >= 0);
        PacketDistributor.sendToPlayersNear(var0.level, null, var0.center.x, var0.center.y, var0.center.z, 96.0, var2);
    }

    private static void announce(ManorGroundSupport.State var0, ServerPlayer var1) {
        if (var0.announced.add(var1.getUUID())) {
            String var2 = "§6领域展开：§e" + ManorDomainEffectRules.name(var0.variant);
            if (var1.connection != null) {
                var1.connection.send(new ClientboundSetTitlesAnimationPacket(10, 40, 10));
                var1.connection
                    .send(new ClientboundSetSubtitleTextPacket(Component.literal("§7" + ManorDomainEffectRules.kind(var0.variant) + " · Boss红圈内生效")));
                var1.connection.send(new ClientboundSetTitleTextPacket(Component.literal(var2)));
            }

            var1.sendSystemMessage(
                Component.literal(
                    var2
                        + " §7["
                        + ManorDomainEffectRules.kind(var0.variant)
                        + "] §f"
                        + ManorDomainEffectRules.description(var0.variant)
                        + " §7范围：Boss红圈内76格，随Boss移动，与展开图案范围无关；离圈解除领域属性。"
                )
            );
        }
    }

    private static void stop(ServerLevel var0, EasterBossEntity var1) {
        updateAttributes(var1, false);
        ManorGroundSupport.State var2 = ACTIVE.remove(var1.getUUID());
        if (var2 != null) {
            send(var2, -1);

            for (ManorGroundSupport.PlayerState var4 : new ArrayList<>(PLAYERS.values())) {
                if (var4.owner.equals(var1.getUUID())) {
                    clearPlayer(var4.player, false);
                }
            }
        }

        LEECH_AT.remove(var1.getUUID());
    }

    private static Entity actor(Entity var0) {
        return var0 instanceof Projectile var1 ? var1.getOwner() : var0;
    }

    private static boolean opponent(ManorGroundSupport.State var0, Entity var1, LivingEntity var2) {
        return (ManorLivingTargets.valid(var1, var2) && var0.fight.equals(ManorFight.fightId(var2))) & contains(var0, var2);
    }

    private static boolean behind(Entity var0, LivingEntity var1) {
        Vec3 var2 = var0.position().subtract(var1.position()).multiply(1.0, 0.0, 1.0).normalize();
        return var1.getLookAngle().dot(var2) < -0.35;
    }

    @SubscribeEvent
    public static void hurt(BeforeMitigation var0) {
        if (!var0.isCanceled() && Float.isFinite(var0.getAmount()) && !(var0.getAmount() <= 0.0F) && !var0.getEntity().level().isClientSide) {
            LivingEntity var1 = var0.getEntity();
            if (var1 instanceof EasterBossEntity var2) {
                ManorGroundSupport.State var4 = state(var2);
                if (var4 != null) {
                    var0.setAmount((float)((double)var0.getAmount() * ManorDomainEffectRules.incoming(var4.variant)));
                }
            } else if (var1 instanceof ServerPlayer var3) {
                ManorGroundSupport.State var14 = playerDomain(var3);
                if (var14 != null && var14.variant == 5) {
                    var0.setAmount((float)((double)var0.getAmount() * ManorDomainEffectRules.incoming(5)));
                }
            }

            Entity var11 = actor(var0.getSource().getEntity());
            if (var11 instanceof ServerPlayer var13) {
                ManorGroundSupport.State var16 = playerDomain(var13);
                if (var16 != null && opponent(var16, var13, var1)) {
                    boolean var17 = ManorDomainRules.rangedHit(
                        -1,
                        var0.getSource().getDirectEntity() != var13,
                        var0.getSource().getMsgId(),
                        var0.getSource().getClass().getName(),
                        (double)var13.distanceTo(var1)
                    );
                    var0.setAmount(
                        (float)(
                            (double)var0.getAmount()
                                * ManorDomainEffectRules.playerOutgoing(var16.variant, var17, (double)var13.distanceTo(var1), behind(var13, var1))
                        )
                    );
                }
            } else if (var11 instanceof EasterBossEntity var12) {
                ManorGroundSupport.State var15 = state(var12);
                if (var15 != null && ManorLivingTargets.valid(var12, var1) & contains(var15, var1)) {
                    double var5 = (double)var12.distanceTo(var1);
                    boolean var7 = ManorDomainRules.rangedHit(
                        var15.variant, var0.getSource().getDirectEntity() != var12, var0.getSource().getMsgId(), var0.getSource().getClass().getName(), var5
                    );
                    if (var7) {
                        var15.cadence++;
                    }

                    double var8 = ManorDomainRules.outgoing(
                        var15.variant, var7, var5, (double)(var1.getHealth() / Math.max(1.0F, var1.getMaxHealth())), behind(var12, var1), var15.cadence
                    );
                    var0.setAmount((float)((double)var0.getAmount() * var8));

                    boolean var10 = switch (var15.variant) {
                        case 1, 5, 6 -> var7;
                        default -> false;
                        case 4, 8, 10 -> !var7;
                    };
                    if (var10 && var15.level.getGameTime() >= var15.effectAt) {
                        var15.effectAt = var15.level.getGameTime() + 40L;
                        switch (var15.variant) {
                            case 1:
                                var1.addEffect(new MobEffectInstance(MobEffects.WITHER, 40, 0, false, true));
                            case 2:
                            case 3:
                            case 7:
                            case 9:
                            default:
                                break;
                            case 4:
                                var1.igniteForSeconds(3.0F);
                                break;
                            case 5:
                                var1.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 35, 0, false, true));
                                break;
                            case 6:
                                var1.knockback(0.35, var12.getX() - var1.getX(), var12.getZ() - var1.getZ());
                                break;
                            case 8:
                                var1.addEffect(new MobEffectInstance(MobEffects.POISON, 60, 0, false, true));
                                var1.knockback(0.45, var12.getX() - var1.getX(), var12.getZ() - var1.getZ());
                                break;
                            case 10:
                                var1.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 1, false, true));
                        }
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public static void heal(LivingHealEvent var0) {
        if (!var0.isCanceled() && var0.getEntity() instanceof ServerPlayer var1) {
            ManorGroundSupport.State var3 = playerDomain(var1);
            if (var3 != null) {
                var0.setAmount((float)((double)var0.getAmount() * ManorDomainEffectRules.healing(var3.variant)));
            }
        }
    }

    @SubscribeEvent(
        priority = EventPriority.LOWEST
    )
    public static void damage(BeforeHealth var0) {
        if (!var0.isCanceled() && Float.isFinite(var0.getAmount()) && !(var0.getAmount() <= 0.0F)) {
            Entity var1 = actor(var0.getSource().getEntity());
            ManorGroundSupport.State var2 = var1 instanceof EasterBossEntity var4
                ? state(var4)
                : (var1 instanceof ServerPlayer var3 ? playerDomain(var3) : null);
            if (var2 != null && var2.variant == 1 && var1 instanceof LivingEntity var5) {
                if (var1 instanceof ServerPlayer
                    ? opponent(var2, var1, var0.getEntity())
                    : ManorLivingTargets.valid(var1, var0.getEntity()) & contains(var2, var0.getEntity())) {
                    PENDING.add(new ManorGroundSupport.PendingLeech(var0, var5, var2.boss.getUUID(), var0.getEntity().getHealth()));
                }
            }
        }
    }

    private static void leech() {
        ArrayList<ManorGroundSupport.PendingLeech> var0 = new ArrayList<>(PENDING);
        PENDING.clear();

        for (ManorGroundSupport.PendingLeech var2 : var0) {
            ManorGroundSupport.State var3 = ACTIVE.get(var2.owner);
            ManorGroundSupport.State var4 = var2.actor instanceof EasterBossEntity var6
                ? state(var6)
                : (var2.actor instanceof ServerPlayer var5 ? playerDomain(var5) : null);
            if (var3 != null && var3 == var4 && !var2.event.isCanceled() && var2.actor.isAlive()) {
                if (var2.event.getEntity() instanceof EasterBossEntity var8 && !var8.isMini() && ManorDamageRules.ignored(var2.event.getAmount())) {
                    continue;
                }

                long var9 = clock;
                if (var9 >= LEECH_AT.getOrDefault(var2.actor.getUUID(), Long.MIN_VALUE)) {
                    float var12 = ManorDomainEffectRules.leech(var2.event.getAmount(), var2.availableHealth);
                    if (var12 > 0.0F) {
                        var2.actor.heal(var12);
                        LEECH_AT.put(var2.actor.getUUID(), var9 + 20L);
                    }
                }
            }
        }
    }

    private static void clearPlayer(ServerPlayer var0, boolean var1) {
        ManorGroundSupport.PlayerState var2 = PLAYERS.remove(var0.getUUID());
        playerAttributes(var0, null);
        if (var1 && var2 != null && var0.isAlive()) {
            var0.displayClientMessage(Component.literal("§a【庄园领域】§7已离开Boss红圈，领域属性解除。"), true);
        }
    }

    private static void updatePlayers() {
        Set<ServerPlayer> var0 = new HashSet<>();

        for (ManorGroundSupport.State var2 : ACTIVE.values()) {
            var0.addAll(var2.level.players());
        }

        for (ManorGroundSupport.PlayerState var8 : PLAYERS.values()) {
            var0.add(var8.player);
        }

        for (ServerPlayer var9 : var0) {
            ManorGroundSupport.State var3 = playerDomain(var9);
            if (var3 == null) {
                clearPlayer(var9, true);
            } else {
                ManorGroundSupport.PlayerState var4 = PLAYERS.get(var9.getUUID());
                if (var4 == null || var4.player != var9 || !var4.owner.equals(var3.boss.getUUID())) {
                    if (var4 != null) {
                        playerAttributes(var4.player, null);
                    }

                    var4 = new ManorGroundSupport.PlayerState(var9, var3.boss.getUUID(), var3.level.getGameTime() + 100L);
                    PLAYERS.put(var9.getUUID(), var4);
                    announce(var3, var9);
                    var9.displayClientMessage(
                        Component.literal("§6【领域生效】§e" + ManorDomainEffectRules.name(var3.variant) + " §7[" + ManorDomainEffectRules.kind(var3.variant) + "]"),
                        true
                    );
                }

                playerAttributes(var9, var3);
                if (var3.variant == 11 && var3.level.getGameTime() >= var4.healAt) {
                    float var5 = ManorDomainEffectRules.prayer(var9.getHealth(), var9.getMaxHealth(), true);
                    if (var5 > 0.0F) {
                        var9.heal(var5);
                    }

                    var4.healAt = var3.level.getGameTime() + 100L;
                }
            }
        }
    }

    @SubscribeEvent
    public static void death(LivingDeathEvent var0) {
        if (var0.getEntity() instanceof EasterBossEntity var1 && var1.level() instanceof ServerLevel var2) {
            stop(var2, var1);
            return;
        }

        if (var0.getEntity() instanceof ServerPlayer var3) {
            clearPlayer(var3, false);
        }
    }

    @SubscribeEvent
    public static void logout(PlayerLoggedOutEvent var0) {
        if (var0.getEntity() instanceof ServerPlayer var1) {
            clearPlayer(var1, false);
        }
    }

    @SubscribeEvent
    public static void changedDimension(PlayerChangedDimensionEvent var0) {
        if (var0.getEntity() instanceof ServerPlayer var1) {
            clearPlayer(var1, false);
        }
    }

    @SubscribeEvent
    public static void tickServer(Post var0) {
        for (ManorGroundSupport.State var2 : new ArrayList<>(ACTIVE.values())) {
            if (!live(var2)) {
                stop(var2.level, var2.boss);
            }
        }

        updatePlayers();
        leech();
        clock++;
        LEECH_AT.entrySet().removeIf(var0x -> var0x.getValue() <= clock);
        INTRODUCED.values()
            .removeIf(
                var0x -> !var0x.boss.isAlive()
                        || var0x.boss.isRemoved()
                        || !ManorFight.hasActive(var0x.fight)
                        || !var0x.boss.getUUID().equals(ManorFight.currentBoss(var0x.fight))
            );

    }

    @SubscribeEvent
    public static void unload(Unload var0) {
        for (ManorGroundSupport.State var2 : new ArrayList<>(ACTIVE.values())) {
            if (var2.level == var0.getLevel()) {
                stop(var2.level, var2.boss);
            }
        }

        INTRODUCED.values().removeIf(var1 -> var1.level == var0.getLevel());
    }

    @SubscribeEvent
    public static void stopped(ServerStoppedEvent var0) {
        for (ManorGroundSupport.State var2 : new ArrayList<>(ACTIVE.values())) {
            updateAttributes(var2.boss, false);
        }

        for (ManorGroundSupport.PlayerState var4 : new ArrayList<>(PLAYERS.values())) {
            clearPlayer(var4.player, false);
        }

        ACTIVE.clear();
        INTRODUCED.clear();
        PENDING.clear();
        LEECH_AT.clear();
        clock = 0L;
    }

    private static record PendingLeech(BeforeHealth event, LivingEntity actor, UUID owner, float availableHealth) {
    }

    private static final class PlayerState {
        final ServerPlayer player;
        final UUID owner;
        long healAt;

        PlayerState(ServerPlayer var1, UUID var2, long var3) {
            this.player = var1;
            this.owner = var2;
            this.healAt = var3;
        }
    }

    private static final class State {
        final ServerLevel level;
        final EasterBossEntity boss;
        final UUID fight;
        final Vec3 anchor;
        final Vec3 center;
        final int variant;
        final Set<UUID> announced;
        boolean inside;
        long seen;
        long refresh;
        long effectAt;
        long healAt;
        int cadence;

        State(ServerLevel var1, EasterBossEntity var2, UUID var3, Vec3 var4, Vec3 var5) {
            this.level = var1;
            this.boss = var2;
            this.fight = var3;
            this.anchor = var4;
            this.center = var5;
            this.variant = var2.getVariant();
            ManorGroundSupport.State var6 = ManorGroundSupport.INTRODUCED.get(var2.getUUID());
            this.announced = (Set<UUID>)(var6 != null && var6.variant == this.variant && var6.level == var1 && var6.fight.equals(var3)
                ? var6.announced
                : new HashSet<>());
            ManorGroundSupport.INTRODUCED.put(var2.getUUID(), this);
        }
    }
}
