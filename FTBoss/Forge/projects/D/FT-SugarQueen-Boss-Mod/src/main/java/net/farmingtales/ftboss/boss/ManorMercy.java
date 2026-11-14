package net.farmingtales.ftboss.boss;

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
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.server.ServerLifecycleHooks;

@Mod.EventBusSubscriber(modid = FTBossMod.MOD_ID)
public final class ManorMercy {
    public static final int ASK_EVERY = 10;
    private record DeathKey(UUID player, UUID fight, UUID boss) {}
    private record Hit(UUID fight, UUID boss, long time) {}
    private record Offer(DeathKey key, UUID token, long expires) {}
    private static final Map<DeathKey, Integer> DEATHS = new HashMap<>();
    private static final Map<UUID, Hit> LAST_HIT = new HashMap<>();
    private static final Map<UUID, Offer> OFFERS = new HashMap<>();
    private static final ThreadLocal<Deque<Entity>> DAMAGE_OWNERS = ThreadLocal.withInitial(ArrayDeque::new);

    private ManorMercy() {}

    public static boolean withOwner(Entity owner, BooleanSupplier damage) {
        if (owner == null) return damage.getAsBoolean();
        Deque<Entity> owners = DAMAGE_OWNERS.get();
        owners.push(owner);
        try { return damage.getAsBoolean(); }
        finally {
            owners.pop();
            if (owners.isEmpty()) DAMAGE_OWNERS.remove();
        }
    }

    public static Entity damageOwner() {
        return DAMAGE_OWNERS.get().peek();
    }

    private static Hit source(ServerPlayer player, net.minecraft.world.damagesource.DamageSource source) {
        Entity owner = source.getEntity();
        if (owner == null) owner = source.getDirectEntity();
        if (owner == null) owner = damageOwner();
        UUID fight = ManorFight.fightId(owner);
        UUID boss = ManorFight.currentBoss(fight);
        if (fight == null || boss == null) return null;
        ManorFight.participate(owner, player);
        return new Hit(fight, boss, player.serverLevel().getGameTime());
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onDamage(LivingDamageEvent event) {
        if (event.isCanceled() || event.getAmount() <= 0.0f || !(event.getEntity() instanceof ServerPlayer player)) return;
        Hit hit = source(player, event.getSource());
        if (hit != null) LAST_HIT.put(player.getUUID(), hit);
        else if (event.getSource().getEntity() != null) LAST_HIT.remove(player.getUUID());
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onDeath(LivingDeathEvent event) {
        if (event.isCanceled() || !(event.getEntity() instanceof ServerPlayer player) || player.isSpectator()) return;
        Hit hit = source(player, event.getSource());
        if (hit == null && event.getSource().getEntity() == null && event.getSource().getDirectEntity() == null
                && !event.getSource().is(net.minecraft.world.damagesource.DamageTypes.GENERIC_KILL)) {
            hit = LAST_HIT.get(player.getUUID());
        }
        LAST_HIT.remove(player.getUUID());
        if (hit == null || player.serverLevel().getGameTime() - hit.time > 200
                || !hit.boss.equals(ManorFight.currentBoss(hit.fight))) return;
        DeathKey key = new DeathKey(player.getUUID(), hit.fight, hit.boss);
        int deaths = DEATHS.merge(key, 1, Integer::sum);
        persist();
        int variant = ManorFight.currentVariant(hit.fight);
        String name = variant >= 0 && variant < EasterBossEntity.NAMES.length ? EasterBossEntity.NAMES[variant] : "庄园 Boss";
        FTBossMod.LOGGER.info("[FTBoss][ManorMercy] {} 在本场 {} 下死亡 {}/{}，战场={}",
                player.getName().getString(), name, deaths, ASK_EVERY, hit.fight);
        if (deaths % ASK_EVERY != 0) return;
        Offer offer = new Offer(key, UUID.randomUUID(), player.serverLevel().getGameTime() + 12000);
        OFFERS.put(player.getUUID(), offer);
        Component keep = Component.literal("§a[继续挑战]").withStyle(style -> style.withClickEvent(
                new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/ftbossmercy keep " + offer.token)));
        player.sendSystemMessage(Component.literal("§6【挑战询问】§e你已在本场「" + name + "」下倒下 " + deaths + " 次。"));
        if (ManorFight.canSurrender(player, hit.fight)) {
            Component give = Component.literal("§c[放弃挑战]").withStyle(style -> style.withClickEvent(
                    new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/ftbossmercy giveup " + offer.token)));
            player.sendSystemMessage(Component.literal("§7结束本场还是继续？ ").append(give).append(" ").append(keep));
        } else {
            player.sendSystemMessage(Component.literal("§7整场投降由召唤者决定。 ").append(keep));
        }
    }

    @SubscribeEvent
    public static void onCommands(RegisterCommandsEvent event) {
        var root = Commands.literal("ftbossmercy");
        for (String action : new String[]{"giveup", "keep"}) {
            boolean surrender = action.equals("giveup");
            root.then(Commands.literal(action)
                    .executes(context -> choose(context.getSource().getPlayerOrException(), null, surrender))
                    .then(Commands.argument("confirmation", UuidArgument.uuid()).executes(context -> choose(
                            context.getSource().getPlayerOrException(), UuidArgument.getUuid(context, "confirmation"), surrender))));
        }
        event.getDispatcher().register(root);
    }

    private static int choose(ServerPlayer player, UUID token, boolean surrender) {
        Offer offer = OFFERS.get(player.getUUID());
        if (offer == null || (token != null && !token.equals(offer.token))
                || player.serverLevel().getGameTime() > offer.expires
                || !offer.key.boss.equals(ManorFight.currentBoss(offer.key.fight))) {
            player.sendSystemMessage(Component.literal("§7【挑战询问】没有有效的本场确认，旧按钮已失效。"));
            return 0;
        }
        if (surrender && !ManorFight.canSurrender(player, offer.key.fight)) {
            player.sendSystemMessage(Component.literal("§c只有本场召唤者可以结束整场挑战。"));
            return 0;
        }
        if (surrender && !ManorFight.surrender(player, offer.key.fight)) return 0;
        OFFERS.remove(player.getUUID());
        DEATHS.remove(offer.key);
        persist();
        if (!surrender) player.sendSystemMessage(Component.literal("§a【挑战询问】继续挑战，本 Boss 的死亡计数已清零。"));
        return 1;
    }

    public static void clearFight(UUID fight) {
        DEATHS.keySet().removeIf(key -> key.fight.equals(fight));
        OFFERS.values().removeIf(offer -> offer.key.fight.equals(fight));
        LAST_HIT.values().removeIf(hit -> hit.fight.equals(fight));
        persist();
    }

    public static void reset() {
        DEATHS.clear();
        OFFERS.clear();
        LAST_HIT.clear();
        DAMAGE_OWNERS.remove();
    }

    public static void restore(ListTag records) {
        for (Tag entry : records) {
            CompoundTag record = (CompoundTag) entry;
            if (record.hasUUID("Player") && record.hasUUID("Fight") && record.hasUUID("Boss")) {
                DEATHS.put(new DeathKey(record.getUUID("Player"), record.getUUID("Fight"), record.getUUID("Boss")),
                        Math.max(0, record.getInt("Count")));
            }
        }
    }

    private static void persist() {
        var server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) return;
        ListTag records = new ListTag();
        DEATHS.forEach((key, count) -> {
            CompoundTag record = new CompoundTag();
            record.putUUID("Player", key.player);
            record.putUUID("Fight", key.fight);
            record.putUUID("Boss", key.boss);
            record.putInt("Count", count);
            records.add(record);
        });
        ManorFightData.get(server).storeDeaths(records);
    }
}
