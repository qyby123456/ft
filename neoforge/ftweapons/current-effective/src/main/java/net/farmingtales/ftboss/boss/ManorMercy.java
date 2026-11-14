package net.farmingtales.ftboss.boss;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.BooleanSupplier;
import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.UuidArgument;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.ClickEvent.Action;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

@EventBusSubscriber(
    modid = "ftboss"
)
public final class ManorMercy {
    public static final int ASK_EVERY = 10;
    private static final Map<ManorMercy.DeathKey, Integer> DEATHS = new HashMap<>();
    private static final Map<UUID, ManorMercy.Hit> LAST_HIT = new HashMap<>();
    private static final Map<UUID, ManorMercy.Offer> OFFERS = new HashMap<>();
    private static final ThreadLocal<Deque<Entity>> DAMAGE_OWNERS = ThreadLocal.withInitial(ArrayDeque::new);

    private ManorMercy() {
    }

    public static boolean withOwner(Entity var0, BooleanSupplier var1) {
        if (var0 == null) {
            return var1.getAsBoolean();
        } else {
            Deque var2 = DAMAGE_OWNERS.get();
            var2.push(var0);

            boolean var3;
            try {
                var3 = var1.getAsBoolean();
            } finally {
                var2.pop();
                if (var2.isEmpty()) {
                    DAMAGE_OWNERS.remove();
                }
            }

            return var3;
        }
    }

    public static Entity damageOwner() {
        return DAMAGE_OWNERS.get().peek();
    }

    private static ManorMercy.Hit source(ServerPlayer var0, DamageSource var1) {
        Entity var2 = var1.getEntity();
        if (var2 == null) {
            var2 = var1.getDirectEntity();
        }

        if (var2 == null) {
            var2 = damageOwner();
        }

        UUID var3 = ManorFight.fightId(var2);
        UUID var4 = ManorFight.currentBoss(var3);
        if (var3 != null && var4 != null) {
            ManorFight.participate(var2, var0);
            return new ManorMercy.Hit(var3, var4, var0.serverLevel().getGameTime());
        } else {
            return null;
        }
    }

    @SubscribeEvent(
        priority = EventPriority.LOWEST
    )
    public static void onDamage(LivingIncomingDamageEvent var0) {
        if (!var0.isCanceled() && !(var0.getAmount() <= 0.0F) && var0.getEntity() instanceof ServerPlayer var1) {
            ManorMercy.Hit var3 = source(var1, var0.getSource());
            if (var3 != null) {
                LAST_HIT.put(var1.getUUID(), var3);
            } else if (var0.getSource().getEntity() != null) {
                LAST_HIT.remove(var1.getUUID());
            }
        }
    }

    @SubscribeEvent(
        priority = EventPriority.LOWEST
    )
    public static void onDeath(LivingDeathEvent var0) {
        if (!var0.isCanceled() && var0.getEntity() instanceof ServerPlayer var1 && !var1.isSpectator()) {
            ManorMercy.Hit var10 = source(var1, var0.getSource());
            if (var10 == null
                && var0.getSource().getEntity() == null
                && var0.getSource().getDirectEntity() == null
                && !var0.getSource().is(DamageTypes.GENERIC_KILL)) {
                var10 = LAST_HIT.get(var1.getUUID());
            }

            LAST_HIT.remove(var1.getUUID());
            if (var10 != null && var1.serverLevel().getGameTime() - var10.time <= 200L && var10.boss.equals(ManorFight.currentBoss(var10.fight))) {
                ManorMercy.DeathKey var3 = new ManorMercy.DeathKey(var1.getUUID(), var10.fight, var10.boss);
                int var4 = DEATHS.merge(var3, 1, Integer::sum);
                persist();
                int var5 = ManorFight.currentVariant(var10.fight);
                String var6 = var5 >= 0 && var5 < EasterBossEntity.NAMES.length ? EasterBossEntity.NAMES[var5] : "庄园 Boss";
                FTBossMod.LOGGER.info("[FTBoss][ManorMercy] {} 在本场 {} 下死亡 {}/{}，战场={}", var1.getName().getString(), var6, var4, 10, var10.fight);
                if (var4 % 10 == 0) {
                    ManorMercy.Offer var7 = new ManorMercy.Offer(var3, UUID.randomUUID(), var1.serverLevel().getGameTime() + 12000L);
                    OFFERS.put(var1.getUUID(), var7);
                    MutableComponent var8 = Component.literal("§a[继续挑战]")
                        .withStyle(var1x -> var1x.withClickEvent(new ClickEvent(Action.RUN_COMMAND, "/ftbossmercy keep " + var7.token)));
                    var1.sendSystemMessage(Component.literal("§6【挑战询问】§e你已在本场「" + var6 + "」下倒下 " + var4 + " 次。"));
                    if (ManorFight.canSurrender(var1, var10.fight)) {
                        MutableComponent var9 = Component.literal("§c[放弃挑战]")
                            .withStyle(var1x -> var1x.withClickEvent(new ClickEvent(Action.RUN_COMMAND, "/ftbossmercy giveup " + var7.token)));
                        var1.sendSystemMessage(Component.literal("§7结束本场还是继续？ ").append(var9).append(" ").append(var8));
                    } else {
                        var1.sendSystemMessage(Component.literal("§7整场投降由召唤者决定。 ").append(var8));
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public static void onCommands(RegisterCommandsEvent var0) {
        LiteralArgumentBuilder var1 = Commands.literal("ftbossmercy");

        for (String var5 : new String[]{"giveup", "keep"}) {
            boolean var6 = var5.equals("giveup");
            var1.then(
                Commands.literal(var5)
                    .executes(var1x -> choose(var1x.getSource().getPlayerOrException(), null, var6))
                    .then(
                        Commands.argument("confirmation", UuidArgument.uuid())
                            .executes(var1x -> choose(var1x.getSource().getPlayerOrException(), UuidArgument.getUuid(var1x, "confirmation"), var6))
                    )
            );
        }

        var0.getDispatcher().register(var1);
    }

    private static int choose(ServerPlayer var0, UUID var1, boolean var2) {
        ManorMercy.Offer var3 = OFFERS.get(var0.getUUID());
        if (var3 == null
            || var1 != null && !var1.equals(var3.token)
            || var0.serverLevel().getGameTime() > var3.expires
            || !var3.key.boss.equals(ManorFight.currentBoss(var3.key.fight))) {
            var0.sendSystemMessage(Component.literal("§7【挑战询问】没有有效的本场确认，旧按钮已失效。"));
            return 0;
        } else if (var2 && !ManorFight.canSurrender(var0, var3.key.fight)) {
            var0.sendSystemMessage(Component.literal("§c只有本场召唤者可以结束整场挑战。"));
            return 0;
        } else if (var2 && !ManorFight.surrender(var0, var3.key.fight)) {
            return 0;
        } else {
            OFFERS.remove(var0.getUUID());
            DEATHS.remove(var3.key);
            persist();
            if (!var2) {
                var0.sendSystemMessage(Component.literal("§a【挑战询问】继续挑战，本 Boss 的死亡计数已清零。"));
            }

            return 1;
        }
    }

    public static void clearFight(UUID var0) {
        DEATHS.keySet().removeIf(var1 -> var1.fight.equals(var0));
        OFFERS.values().removeIf(var1 -> var1.key.fight.equals(var0));
        LAST_HIT.values().removeIf(var1 -> var1.fight.equals(var0));
        persist();
    }

    public static void reset() {
        DEATHS.clear();
        OFFERS.clear();
        LAST_HIT.clear();
        DAMAGE_OWNERS.remove();
    }

    public static void restore(ListTag var0) {
        for (Tag var2 : var0) {
            CompoundTag var3 = (CompoundTag)var2;
            if (var3.hasUUID("Player") && var3.hasUUID("Fight") && var3.hasUUID("Boss")) {
                DEATHS.put(new ManorMercy.DeathKey(var3.getUUID("Player"), var3.getUUID("Fight"), var3.getUUID("Boss")), Math.max(0, var3.getInt("Count")));
            }
        }
    }

    private static void persist() {
        MinecraftServer var0 = ServerLifecycleHooks.getCurrentServer();
        if (var0 != null) {
            ListTag var1 = new ListTag();
            DEATHS.forEach((var1x, var2) -> {
                CompoundTag var3 = new CompoundTag();
                var3.putUUID("Player", var1x.player);
                var3.putUUID("Fight", var1x.fight);
                var3.putUUID("Boss", var1x.boss);
                var3.putInt("Count", var2);
                var1.add(var3);
            });
            ManorFightData.get(var0).storeDeaths(var1);
        }
    }

    private static record DeathKey(UUID player, UUID fight, UUID boss) {
    }

    private static record Hit(UUID fight, UUID boss, long time) {
    }

    private static record Offer(ManorMercy.DeathKey key, UUID token, long expires) {
    }
}
