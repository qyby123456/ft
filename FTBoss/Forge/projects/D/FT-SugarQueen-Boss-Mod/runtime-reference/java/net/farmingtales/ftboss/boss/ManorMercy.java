/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.arguments.ArgumentType
 *  com.mojang.brigadier.builder.LiteralArgumentBuilder
 *  com.mojang.brigadier.context.CommandContext
 *  net.minecraft.commands.CommandSourceStack
 *  net.minecraft.commands.Commands
 *  net.minecraft.commands.arguments.UuidArgument
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.nbt.ListTag
 *  net.minecraft.nbt.Tag
 *  net.minecraft.network.chat.ClickEvent
 *  net.minecraft.network.chat.ClickEvent$Action
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.MutableComponent
 *  net.minecraft.server.MinecraftServer
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.damagesource.DamageSource
 *  net.minecraft.world.damagesource.DamageTypes
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraftforge.event.RegisterCommandsEvent
 *  net.minecraftforge.event.entity.living.LivingDamageEvent
 *  net.minecraftforge.event.entity.living.LivingDeathEvent
 *  net.minecraftforge.eventbus.api.EventPriority
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber
 *  net.minecraftforge.server.ServerLifecycleHooks
 */
package net.farmingtales.ftboss.boss;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.BooleanSupplier;
import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.boss.ManorFight;
import net.farmingtales.ftboss.boss.ManorFightData;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.UuidArgument;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.server.ServerLifecycleHooks;

@Mod.EventBusSubscriber(modid="ftboss")
public final class ManorMercy {
    public static final int ASK_EVERY = 10;
    private static final Map<DeathKey, Integer> DEATHS = new HashMap<DeathKey, Integer>();
    private static final Map<UUID, Hit> LAST_HIT = new HashMap<UUID, Hit>();
    private static final Map<UUID, Offer> OFFERS = new HashMap<UUID, Offer>();
    private static final ThreadLocal<Deque<Entity>> DAMAGE_OWNERS = ThreadLocal.withInitial(ArrayDeque::new);

    private ManorMercy() {
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static boolean withOwner(Entity owner, BooleanSupplier damage) {
        if (owner == null) {
            return damage.getAsBoolean();
        }
        Deque<Entity> owners = DAMAGE_OWNERS.get();
        owners.push(owner);
        try {
            boolean bl = damage.getAsBoolean();
            return bl;
        }
        finally {
            owners.pop();
            if (owners.isEmpty()) {
                DAMAGE_OWNERS.remove();
            }
        }
    }

    public static Entity damageOwner() {
        return DAMAGE_OWNERS.get().peek();
    }

    private static Hit source(ServerPlayer player, DamageSource source) {
        Entity owner = source.m_7639_();
        if (owner == null) {
            owner = source.m_7640_();
        }
        if (owner == null) {
            owner = ManorMercy.damageOwner();
        }
        UUID fight = ManorFight.fightId(owner);
        UUID boss = ManorFight.currentBoss(fight);
        if (fight == null || boss == null) {
            return null;
        }
        ManorFight.participate(owner, player);
        return new Hit(fight, boss, player.m_284548_().m_46467_());
    }

    @SubscribeEvent(priority=EventPriority.LOWEST)
    public static void onDamage(LivingDamageEvent event) {
        LivingEntity livingEntity;
        if (event.isCanceled() || event.getAmount() <= 0.0f || !((livingEntity = event.getEntity()) instanceof ServerPlayer)) {
            return;
        }
        ServerPlayer player = (ServerPlayer)livingEntity;
        Hit hit = ManorMercy.source(player, event.getSource());
        if (hit != null) {
            LAST_HIT.put(player.m_20148_(), hit);
        } else if (event.getSource().m_7639_() != null) {
            LAST_HIT.remove(player.m_20148_());
        }
    }

    @SubscribeEvent(priority=EventPriority.LOWEST)
    public static void onDeath(LivingDeathEvent event) {
        ServerPlayer player;
        LivingEntity livingEntity;
        if (event.isCanceled() || !((livingEntity = event.getEntity()) instanceof ServerPlayer) || (player = (ServerPlayer)livingEntity).m_5833_()) {
            return;
        }
        Hit hit = ManorMercy.source(player, event.getSource());
        if (hit == null && event.getSource().m_7639_() == null && event.getSource().m_7640_() == null && !event.getSource().m_276093_(DamageTypes.f_286979_)) {
            hit = LAST_HIT.get(player.m_20148_());
        }
        LAST_HIT.remove(player.m_20148_());
        if (hit == null || player.m_284548_().m_46467_() - hit.time > 200L || !hit.boss.equals(ManorFight.currentBoss(hit.fight))) {
            return;
        }
        DeathKey key = new DeathKey(player.m_20148_(), hit.fight, hit.boss);
        int deaths = DEATHS.merge(key, 1, Integer::sum);
        ManorMercy.persist();
        int variant = ManorFight.currentVariant(hit.fight);
        String name = variant >= 0 && variant < EasterBossEntity.NAMES.length ? EasterBossEntity.NAMES[variant] : "\u5e84\u56ed Boss";
        FTBossMod.LOGGER.info("[FTBoss][ManorMercy] {} \u5728\u672c\u573a {} \u4e0b\u6b7b\u4ea1 {}/{}\uff0c\u6218\u573a={}", new Object[]{player.m_7755_().getString(), name, deaths, 10, hit.fight});
        if (deaths % 10 != 0) {
            return;
        }
        Offer offer = new Offer(key, UUID.randomUUID(), player.m_284548_().m_46467_() + 12000L);
        OFFERS.put(player.m_20148_(), offer);
        MutableComponent keep = Component.m_237113_((String)"\u00a7a[\u7ee7\u7eed\u6311\u6218]").m_130938_(style -> style.m_131142_(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/ftbossmercy keep " + offer.token)));
        player.m_213846_((Component)Component.m_237113_((String)("\u00a76\u3010\u6311\u6218\u8be2\u95ee\u3011\u00a7e\u4f60\u5df2\u5728\u672c\u573a\u300c" + name + "\u300d\u4e0b\u5012\u4e0b " + deaths + " \u6b21\u3002")));
        if (ManorFight.canSurrender(player, hit.fight)) {
            MutableComponent give = Component.m_237113_((String)"\u00a7c[\u653e\u5f03\u6311\u6218]").m_130938_(style -> style.m_131142_(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/ftbossmercy giveup " + offer.token)));
            player.m_213846_((Component)Component.m_237113_((String)"\u00a77\u7ed3\u675f\u672c\u573a\u8fd8\u662f\u7ee7\u7eed\uff1f ").m_7220_((Component)give).m_130946_(" ").m_7220_((Component)keep));
        } else {
            player.m_213846_((Component)Component.m_237113_((String)"\u00a77\u6574\u573a\u6295\u964d\u7531\u53ec\u5524\u8005\u51b3\u5b9a\u3002 ").m_7220_((Component)keep));
        }
    }

    @SubscribeEvent
    public static void onCommands(RegisterCommandsEvent event) {
        LiteralArgumentBuilder root = Commands.m_82127_((String)"ftbossmercy");
        for (String action : new String[]{"giveup", "keep"}) {
            boolean surrender = action.equals("giveup");
            root.then(((LiteralArgumentBuilder)Commands.m_82127_((String)action).executes(context -> ManorMercy.choose(((CommandSourceStack)context.getSource()).m_81375_(), null, surrender))).then(Commands.m_82129_((String)"confirmation", (ArgumentType)UuidArgument.m_113850_()).executes(context -> ManorMercy.choose(((CommandSourceStack)context.getSource()).m_81375_(), UuidArgument.m_113853_((CommandContext)context, (String)"confirmation"), surrender))));
        }
        event.getDispatcher().register(root);
    }

    private static int choose(ServerPlayer player, UUID token, boolean surrender) {
        Offer offer = OFFERS.get(player.m_20148_());
        if (offer == null || token != null && !token.equals(offer.token) || player.m_284548_().m_46467_() > offer.expires || !offer.key.boss.equals(ManorFight.currentBoss(offer.key.fight))) {
            player.m_213846_((Component)Component.m_237113_((String)"\u00a77\u3010\u6311\u6218\u8be2\u95ee\u3011\u6ca1\u6709\u6709\u6548\u7684\u672c\u573a\u786e\u8ba4\uff0c\u65e7\u6309\u94ae\u5df2\u5931\u6548\u3002"));
            return 0;
        }
        if (surrender && !ManorFight.canSurrender(player, offer.key.fight)) {
            player.m_213846_((Component)Component.m_237113_((String)"\u00a7c\u53ea\u6709\u672c\u573a\u53ec\u5524\u8005\u53ef\u4ee5\u7ed3\u675f\u6574\u573a\u6311\u6218\u3002"));
            return 0;
        }
        if (surrender && !ManorFight.surrender(player, offer.key.fight)) {
            return 0;
        }
        OFFERS.remove(player.m_20148_());
        DEATHS.remove(offer.key);
        ManorMercy.persist();
        if (!surrender) {
            player.m_213846_((Component)Component.m_237113_((String)"\u00a7a\u3010\u6311\u6218\u8be2\u95ee\u3011\u7ee7\u7eed\u6311\u6218\uff0c\u672c Boss \u7684\u6b7b\u4ea1\u8ba1\u6570\u5df2\u6e05\u96f6\u3002"));
        }
        return 1;
    }

    public static void clearFight(UUID fight) {
        DEATHS.keySet().removeIf(key -> key.fight.equals(fight));
        OFFERS.values().removeIf(offer -> offer.key.fight.equals(fight));
        LAST_HIT.values().removeIf(hit -> hit.fight.equals(fight));
        ManorMercy.persist();
    }

    public static void reset() {
        DEATHS.clear();
        OFFERS.clear();
        LAST_HIT.clear();
        DAMAGE_OWNERS.remove();
    }

    public static void restore(ListTag records) {
        for (Tag entry : records) {
            CompoundTag record = (CompoundTag)entry;
            if (!record.m_128403_("Player") || !record.m_128403_("Fight") || !record.m_128403_("Boss")) continue;
            DEATHS.put(new DeathKey(record.m_128342_("Player"), record.m_128342_("Fight"), record.m_128342_("Boss")), Math.max(0, record.m_128451_("Count")));
        }
    }

    private static void persist() {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) {
            return;
        }
        ListTag records = new ListTag();
        DEATHS.forEach((key, count) -> {
            CompoundTag record = new CompoundTag();
            record.m_128362_("Player", key.player);
            record.m_128362_("Fight", key.fight);
            record.m_128362_("Boss", key.boss);
            record.m_128405_("Count", count.intValue());
            records.add((Object)record);
        });
        ManorFightData.get(server).storeDeaths(records);
    }

    private record Hit(UUID fight, UUID boss, long time) {
    }

    private record DeathKey(UUID player, UUID fight, UUID boss) {
    }

    private record Offer(DeathKey key, UUID token, long expires) {
    }
}

