/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.arguments.ArgumentType
 *  com.mojang.brigadier.builder.LiteralArgumentBuilder
 *  com.mojang.brigadier.context.CommandContext
 *  net.minecraft.advancements.Advancement
 *  net.minecraft.commands.CommandSourceStack
 *  net.minecraft.commands.Commands
 *  net.minecraft.commands.arguments.UuidArgument
 *  net.minecraft.network.chat.ClickEvent
 *  net.minecraft.network.chat.ClickEvent$Action
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.HoverEvent
 *  net.minecraft.network.chat.HoverEvent$Action
 *  net.minecraft.network.chat.MutableComponent
 *  net.minecraft.network.chat.Style
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.sounds.SoundEvent
 *  net.minecraft.sounds.SoundEvents
 *  net.minecraft.sounds.SoundSource
 *  net.minecraft.world.effect.MobEffectInstance
 *  net.minecraft.world.effect.MobEffects
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.Items
 *  net.minecraft.world.level.ItemLike
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.phys.Vec3
 *  net.minecraftforge.event.RegisterCommandsEvent
 *  net.minecraftforge.event.TickEvent$Phase
 *  net.minecraftforge.event.TickEvent$ServerTickEvent
 *  net.minecraftforge.event.server.ServerStoppingEvent
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber
 */
package net.farmingtales.ftboss.boss;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import java.util.UUID;
import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.boss.BossChallengePreference;
import net.farmingtales.ftboss.boss.HiddenBossIntro;
import net.farmingtales.ftboss.boss.ManorDifficulty;
import net.farmingtales.ftboss.boss.SummonGate;
import net.farmingtales.ftboss.sound.BossMusic;
import net.minecraft.advancements.Advancement;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.UuidArgument;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid="ftboss")
public final class HiddenBossChallenge {
    public static final double WARN_RADIUS = 96.0;
    public static final int TIMEOUT_TICKS = 1200;
    public static final int GIFT_APPLES = 64;
    public static final int ABSORPTION_POINTS = 400;
    public static final int ABSORPTION_TICKS = 12000;
    private static Offer pending = null;

    private HiddenBossChallenge() {
    }

    public static boolean awaiting() {
        return pending != null;
    }

    static boolean cancelForCreative(ServerLevel level) {
        if (pending == null || HiddenBossChallenge.pending.sl != level) {
            return false;
        }
        Offer offer = pending;
        pending = null;
        BossMusic.stopManorCue("\u521b\u9020\u9053\u5177\u53d6\u6d88\u6311\u6218");
        for (ServerPlayer player : level.m_6907_()) {
            if (!(player.m_20238_(offer.anchor) <= 9216.0)) continue;
            player.m_213846_((Component)Component.m_237113_((String)"\u00a77\u3010\u751c\u82f9\u679c\u5e84\u56ed\u3011\u672c\u6b21\u6311\u6218\u5df2\u88ab\u521b\u9020\u9053\u5177\u53d6\u6d88\uff0c\u539f\u786e\u8ba4\u6309\u94ae\u5931\u6548\u3002"));
        }
        return true;
    }

    public static void ask(ServerLevel sl, Player chooser, Vec3 anchor, Vec3 from, String ... lines) {
        ServerPlayer cs;
        if (sl == null) {
            return;
        }
        if (!SummonGate.isAllowedDim((Level)sl)) {
            FTBossMod.LOGGER.info("[FTBoss][Challenge] \u300c{}\u300d\u4e0d\u662f\u4e3b\u4e16\u754c -> \u4e0d\u5f39\u9690\u85cf Boss \u6311\u6218\u786e\u8ba4", (Object)SummonGate.dimName((Level)sl));
            return;
        }
        long now = sl.m_46467_();
        UUID chooserId = chooser == null ? null : chooser.m_20148_();
        String who = chooser == null ? "?" : chooser.m_7755_().getString();
        pending = new Offer(sl, anchor, from, lines, now + 1200L, chooserId, who);
        if (chooser instanceof ServerPlayer) {
            ServerPlayer serverPlayer = (ServerPlayer)chooser;
            HiddenBossChallenge.pending.difficulty = BossChallengePreference.selected((Player)serverPlayer);
        }
        FTBossMod.LOGGER.info("[FTBoss][Challenge] \u5df2\u5f39\u51fa\u9690\u85cf Boss \u6311\u6218\u786e\u8ba4(60 \u79d2\u5185\u7531 {} \u9009\u62e9), \u951a\u70b9 {},{},{}", new Object[]{who, (int)anchor.f_82479_, (int)anchor.f_82480_, (int)anchor.f_82481_});
        BossMusic.beginManorPhase(sl, anchor);
        boolean sent = false;
        for (ServerPlayer p : sl.m_6907_()) {
            if (p.m_20275_(anchor.f_82479_, anchor.f_82480_, anchor.f_82481_) > 9216.0) continue;
            if (chooserId == null || chooserId.equals(p.m_20148_())) {
                HiddenBossChallenge.sendPrompt(p, pending);
                sent = true;
            } else {
                HiddenBossChallenge.sendNotice(p, who);
            }
            sl.m_6263_(null, p.m_20185_(), p.m_20186_(), p.m_20189_(), (SoundEvent)SoundEvents.f_12210_.m_203334_(), SoundSource.MASTER, 1.0f, 0.7f);
        }
        if (!sent && chooserId != null && sl.m_7654_() != null && (cs = sl.m_7654_().m_6846_().m_11259_(chooserId)) != null) {
            HiddenBossChallenge.sendPrompt(cs, pending);
        }
    }

    private static Component difficultyDetails() {
        return Component.m_237113_((String)("\u00a74" + ManorDifficulty.IMPOSSIBLE.label() + "\n\u00a7c\u8be5\u96be\u5ea6\u51e0\u4e4e\u65e0\u6cd5\u901a\u8fc7\u800c\u4e14\u4e0d\u4f1a\u83b7\u53d6\u6210\u5c31\n\u00a77\u542f\u7528\u9650\u4f24\u3001\u6307\u5b9a\u589e\u76ca\u7981\u7528\u4e0e\u53cd\u9003\u8bfe\u89c4\u5219\n\u00a77\u4fdd\u7559\u5e38\u89c4\u6389\u843d\u4e0e\u80dc\u5229\u5956\u52b1\uff0c\u4e0d\u53d1\u653e\u5e84\u56ed\u6210\u5c31\u6216\u901a\u5173\u5934\u8854"));
    }

    private static Component choice(String label, String command, Component details) {
        return Component.m_237113_((String)label).m_130948_(Style.f_131099_.m_131142_(new ClickEvent(ClickEvent.Action.RUN_COMMAND, command)).m_131144_(new HoverEvent(HoverEvent.Action.f_130831_, (Object)details)));
    }

    private static void sendPrompt(ServerPlayer player, Offer offer) {
        boolean unlocked = !offer.finalConfirmation && ManorDifficulty.unlocked(player);
        boolean finale = offer.difficulty == ManorDifficulty.IMPOSSIBLE;
        player.m_213846_((Component)Component.m_237113_((String)(offer.finalConfirmation ? "\u00a76\u00a7l\u3010\u6700\u7ec8\u786e\u8ba4 2/2\u3011\u00a7r\u00a7f\u5f53\u524d\u96be\u5ea6\uff1a" + offer.difficulty.label() : "\u00a76\u00a7l\u3010\u751c\u82f9\u679c\u5e84\u56ed\u3011\u00a7r\u00a7f\u9009\u62e9\u96be\u5ea6\uff081/2\uff09")));
        if (offer.finalConfirmation) {
            if (finale) {
                player.m_213846_((Component)Component.m_237113_((String)"\u00a7c\u8be5\u96be\u5ea6\u51e0\u4e4e\u65e0\u6cd5\u901a\u8fc7\u800c\u4e14\u4e0d\u4f1a\u83b7\u53d6\u6210\u5c31"));
                player.m_213846_((Component)Component.m_237113_((String)"\u00a77\u9650\u4f24 \u00b7 \u6307\u5b9a\u589e\u76ca\u7981\u7528 \u00b7 \u53cd\u9003\u8bfe \u00a78[\u60ac\u6d6e\u67e5\u770b\u89c4\u5219]").m_130938_(style -> style.m_131144_(new HoverEvent(HoverEvent.Action.f_130831_, (Object)HiddenBossChallenge.difficultyDetails()))));
            }
            player.m_213846_((Component)Component.m_237113_((String)"\u00a77\u786e\u8ba4\u540e\u53d1\u653e\u8865\u7ed9\u5e76\u5f00\u59cb\u6f14\u51fa\uff1a\u00a7f\u91d1\u82f9\u679c \u00d764\u00a77\u3001\u00a7f\u4f24\u5bb3\u5438\u6536 400 \u70b9\uff0810 \u5206\u949f\uff09"));
        } else {
            player.m_213846_((Component)Component.m_237113_((String)"\u00a77\u9ad8\u5f3a\u5ea6\u8fde\u7eed\u6218\u6597\uff0c\u5efa\u8bae\u88c5\u5907\u4e0e\u8865\u7ed9\u9f50\u5168\u540e\u6311\u6218\u3002"));
            if (unlocked) {
                player.m_213846_((Component)Component.m_237113_((String)("\u00a7c" + ManorDifficulty.IMPOSSIBLE.label() + "\uff1a\u8be5\u96be\u5ea6\u51e0\u4e4e\u65e0\u6cd5\u901a\u8fc7\u800c\u4e14\u4e0d\u4f1a\u83b7\u53d6\u6210\u5c31")));
            }
        }
        String command = "/ftboss challenge " + (offer.finalConfirmation ? "confirm " : (finale ? "finale " : "normal ")) + offer.token;
        String primaryLabel = offer.finalConfirmation ? "\u00a7a\u00a7l[ \u786e\u8ba4\u5f00\u6218 2/2 ]" : (finale ? "\u00a74\u00a7l[ " + ManorDifficulty.IMPOSSIBLE.label() + " 1/2 \u00b7 \u77f3\u50cf\u9884\u8bbe ]" : "\u00a7a\u00a7l[ \u666e\u901a\u96be\u5ea6 1/2 \u00b7 \u77f3\u50cf\u9884\u8bbe ]");
        MutableComponent buttons = Component.m_237113_((String)"\u00a77\u8bf7\u9009\u62e9\uff1a ").m_7220_(HiddenBossChallenge.choice(primaryLabel, command, (Component)Component.m_237113_((String)(offer.finalConfirmation ? "\u00a77\u7acb\u5373\u53d1\u653e\u8865\u7ed9\uff0c\u5f00\u59cb\u5165\u573a\u6f14\u51fa" : "\u00a77\u4f7f\u7528\u77f3\u50cf\u9884\u8bbe\u96be\u5ea6\u8fdb\u5165\u6700\u7ec8\u786e\u8ba4\uff1b\u6b64\u65f6\u4e0d\u5f00\u6218\uff0c\u4e5f\u4e0d\u53d1\u653e\u8865\u7ed9"))));
        if (!offer.finalConfirmation) {
            buttons.m_7220_((Component)Component.m_237113_((String)"  "));
            if (finale) {
                buttons.m_7220_(HiddenBossChallenge.choice("\u00a7a[ \u4e34\u65f6\u6539\u9009\u666e\u901a\u96be\u5ea6 ]", "/ftboss challenge normal " + offer.token, (Component)Component.m_237113_((String)"\u00a77\u53ea\u4fee\u6539\u672c\u6b21\u5e84\u56ed\u6311\u6218\uff0c\u4e0d\u6539\u53d8\u77f3\u50cf\u9884\u8bbe")));
            } else if (unlocked) {
                buttons.m_7220_(HiddenBossChallenge.choice("\u00a74\u00a7l[ " + ManorDifficulty.IMPOSSIBLE.label() + " 1/2 ]", "/ftboss challenge finale " + offer.token, HiddenBossChallenge.difficultyDetails()));
            } else {
                buttons.m_7220_((Component)Component.m_237113_((String)("\u00a78[ " + ManorDifficulty.IMPOSSIBLE.label() + "\u00b7\u672a\u89e3\u9501 ]")).m_130938_(style -> style.m_131144_(new HoverEvent(HoverEvent.Action.f_130831_, (Object)Component.m_237113_((String)"\u00a77\u5b8c\u6574\u901a\u5173\u666e\u901a\u5e84\u56ed\u6311\u6218\u540e\u89e3\u9501")))));
            }
        }
        buttons.m_7220_((Component)Component.m_237113_((String)"  ")).m_7220_(HiddenBossChallenge.choice("\u00a7c[ \u653e\u5f03 ]", "/ftboss giveup " + offer.token, (Component)Component.m_237113_((String)"\u00a77\u53d6\u6d88\u672c\u6b21\u6311\u6218\uff0c\u4e0d\u53ec\u5524 Boss")));
        player.m_213846_((Component)buttons);
        HiddenBossChallenge.sendSurrenderReminder(player);
        player.m_213846_((Component)Component.m_237113_((String)"\u00a78\u4ec5\u89e6\u53d1\u8005\u53ef\u9009\uff1b\u6bcf\u6b65 60 \u79d2\uff0c\u8d85\u65f6\u53d6\u6d88\u3002"));
    }

    private static void sendSurrenderReminder(ServerPlayer player) {
        player.m_213846_((Component)Component.m_237113_((String)"\u00a77\u540c\u4e00\u5e84\u56ed Boss \u4e0b\u8fde\u7eed\u6b7b\u4ea1 \u00a7e10 \u6b21\u00a77\u53ef\u9009\u62e9\u7ee7\u7eed/\u6295\u964d\uff1b\u00a7e\u4ec5\u53ec\u5524\u8005\u00a77\u53ef\u51b3\u5b9a\u6574\u573a\u6295\u964d\uff0c\u6295\u964d\u4e0d\u7b97\u901a\u5173\u3001\u4e0d\u53d1\u80dc\u5229\u5b9d\u7bb1\u3002").m_130938_(style -> style.m_131144_(new HoverEvent(HoverEvent.Action.f_130831_, (Object)Component.m_237113_((String)"\u00a77\u9009\u62e9\u7ee7\u7eed\u4f1a\u6e05\u96f6\u8be5 Boss \u7684\u6b7b\u4ea1\u8ba1\u6570")))));
    }

    private static void sendNotice(ServerPlayer player, String chooserName) {
        player.m_213846_((Component)Component.m_237113_((String)("\u00a76\u3010\u751c\u82f9\u679c\u5e84\u56ed\u3011\u00a77\u7b49\u5f85\u89e6\u53d1\u8005 \u00a7e" + chooserName + " \u00a77\u9009\u62e9\u96be\u5ea6\u5e76\u786e\u8ba4\u5f00\u6218\uff0c\u5176\u4ed6\u73a9\u5bb6\u65e0\u9700\u64cd\u4f5c\u3002")));
        HiddenBossChallenge.sendSurrenderReminder(player);
    }

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent e) {
        e.getDispatcher().register((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.m_82127_((String)"ftboss").then(((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.m_82127_((String)"challenge").executes(ctx -> HiddenBossChallenge.accept(((CommandSourceStack)ctx.getSource()).m_81375_(), null, false))).then(Commands.m_82129_((String)"confirmation", (ArgumentType)UuidArgument.m_113850_()).executes(ctx -> HiddenBossChallenge.accept(((CommandSourceStack)ctx.getSource()).m_81375_(), UuidArgument.m_113853_((CommandContext)ctx, (String)"confirmation"), false)))).then(Commands.m_82127_((String)"normal").then(Commands.m_82129_((String)"confirmation", (ArgumentType)UuidArgument.m_113850_()).executes(ctx -> HiddenBossChallenge.accept(((CommandSourceStack)ctx.getSource()).m_81375_(), UuidArgument.m_113853_((CommandContext)ctx, (String)"confirmation"), false, ManorDifficulty.NORMAL))))).then(Commands.m_82127_((String)"impossible").then(Commands.m_82129_((String)"confirmation", (ArgumentType)UuidArgument.m_113850_()).executes(ctx -> HiddenBossChallenge.accept(((CommandSourceStack)ctx.getSource()).m_81375_(), UuidArgument.m_113853_((CommandContext)ctx, (String)"confirmation"), false, ManorDifficulty.IMPOSSIBLE))))).then(Commands.m_82127_((String)"finale").then(Commands.m_82129_((String)"confirmation", (ArgumentType)UuidArgument.m_113850_()).executes(ctx -> HiddenBossChallenge.accept(((CommandSourceStack)ctx.getSource()).m_81375_(), UuidArgument.m_113853_((CommandContext)ctx, (String)"confirmation"), false, ManorDifficulty.IMPOSSIBLE))))).then(Commands.m_82127_((String)"confirm").then(Commands.m_82129_((String)"confirmation", (ArgumentType)UuidArgument.m_113850_()).executes(ctx -> HiddenBossChallenge.accept(((CommandSourceStack)ctx.getSource()).m_81375_(), UuidArgument.m_113853_((CommandContext)ctx, (String)"confirmation"), true)))))).then(((LiteralArgumentBuilder)Commands.m_82127_((String)"giveup").executes(ctx -> HiddenBossChallenge.decline(((CommandSourceStack)ctx.getSource()).m_81375_(), null))).then(Commands.m_82129_((String)"confirmation", (ArgumentType)UuidArgument.m_113850_()).executes(ctx -> HiddenBossChallenge.decline(((CommandSourceStack)ctx.getSource()).m_81375_(), UuidArgument.m_113853_((CommandContext)ctx, (String)"confirmation"))))));
    }

    @SubscribeEvent
    public static void onServerStopping(ServerStoppingEvent event) {
        pending = null;
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent e) {
        if (e.phase != TickEvent.Phase.END || pending == null) {
            return;
        }
        ServerLevel sl = HiddenBossChallenge.pending.sl;
        if (sl.m_46467_() < HiddenBossChallenge.pending.expireTick) {
            if (HiddenBossChallenge.pending.chooser != null && sl.m_7654_() != null && sl.m_7654_().m_6846_().m_11259_(HiddenBossChallenge.pending.chooser) == null) {
                Offer gone = pending;
                pending = null;
                BossMusic.stopManorCue("\u89e6\u53d1\u8005\u79bb\u7ebf");
                for (ServerPlayer p : sl.m_6907_()) {
                    if (p.m_20275_(gone.anchor.f_82479_, gone.anchor.f_82480_, gone.anchor.f_82481_) > 9216.0) continue;
                    p.m_213846_((Component)Component.m_237113_((String)("\u00a77\u3010\u751c\u82f9\u679c\u5e84\u56ed\u3011\u89e6\u53d1\u8005 \u00a7e" + gone.chooserName + " \u00a77\u5df2\u79bb\u7ebf \u2014\u2014 \u672c\u6b21\u6311\u6218\u786e\u8ba4\u4f5c\u5e9f\u3002")));
                }
                FTBossMod.LOGGER.info("[FTBoss][Challenge] \u89e6\u53d1\u8005 {} \u5df2\u79bb\u7ebf, \u6311\u6218\u786e\u8ba4\u4f5c\u5e9f", (Object)gone.chooserName);
            }
            return;
        }
        Offer dead = pending;
        pending = null;
        BossMusic.stopManorCue("60 \u79d2\u672a\u9009\u62e9, \u8d85\u65f6");
        for (ServerPlayer p : sl.m_6907_()) {
            if (p.m_20275_(dead.anchor.f_82479_, dead.anchor.f_82480_, dead.anchor.f_82481_) > 9216.0) continue;
            p.m_213846_((Component)Component.m_237113_((String)"\u00a77\u3010\u751c\u82f9\u679c\u5e84\u56ed\u3011\u00a78\u6311\u6218\u786e\u8ba4\u5df2\u8d85\u65f6(60 \u79d2\u672a\u9009\u62e9) \u2014\u2014 \u672c\u6b21\u4e0d\u4f1a\u5f00\u59cb\u3002"));
        }
        FTBossMod.LOGGER.info("[FTBoss][Challenge] \u6311\u6218\u786e\u8ba4\u8d85\u65f6, \u53d6\u6d88");
    }

    private static Offer currentOffer(ServerPlayer p, UUID token) {
        Offer o = pending;
        if (o == null) {
            p.m_213846_((Component)Component.m_237113_((String)"\u00a77\u3010\u751c\u82f9\u679c\u5e84\u56ed\u3011\u73b0\u5728\u6ca1\u6709\u5f85\u786e\u8ba4\u7684\u6311\u6218\u3002"));
            return null;
        }
        if (o.chooser != null && !o.chooser.equals(p.m_20148_())) {
            p.m_213846_((Component)Component.m_237113_((String)("\u00a77\u3010\u751c\u82f9\u679c\u5e84\u56ed\u3011\u672c\u573a\u5f69\u86cb\u7531 \u00a7e" + o.chooserName + " \u00a77\u89e6\u53d1 \u2014\u2014 \u00a7c\u53ea\u80fd\u7531\u4ed6\u6765\u9009\u62e9\u00a77\u3002")));
            return null;
        }
        if (o.sl.m_46467_() >= o.expireTick) {
            pending = null;
            BossMusic.stopManorCue("\u6311\u6218\u786e\u8ba4\u8d85\u65f6");
            p.m_213846_((Component)Component.m_237113_((String)"\u00a77\u3010\u751c\u82f9\u679c\u5e84\u56ed\u3011\u6311\u6218\u786e\u8ba4\u5df2\u8d85\u65f6\uff0c\u672c\u6b21\u4e0d\u4f1a\u5f00\u59cb\u3002"));
            return null;
        }
        if (token != null && !token.equals(o.token)) {
            p.m_213846_((Component)Component.m_237113_((String)"\u00a77\u3010\u751c\u82f9\u679c\u5e84\u56ed\u3011\u65e7\u786e\u8ba4\u6309\u94ae\u5df2\u5931\u6548\uff0c\u8bf7\u4f7f\u7528\u672c\u6b21\u6700\u65b0\u7684\u6309\u94ae\u3002"));
            return null;
        }
        return o;
    }

    private static int accept(ServerPlayer p, UUID token, boolean finalConfirmation) {
        return HiddenBossChallenge.accept(p, token, finalConfirmation, null);
    }

    private static int accept(ServerPlayer p, UUID token, boolean finalConfirmation, ManorDifficulty selection) {
        ManorDifficulty difficulty;
        Offer o = HiddenBossChallenge.currentOffer(p, token);
        if (o == null) {
            return 0;
        }
        if (selection != null && o.finalConfirmation) {
            HiddenBossChallenge.sendPrompt(p, o);
            return 0;
        }
        ManorDifficulty manorDifficulty = difficulty = selection == null ? o.difficulty : selection;
        if (difficulty == ManorDifficulty.IMPOSSIBLE && !ManorDifficulty.unlocked(p)) {
            p.m_213846_((Component)Component.m_237113_((String)("\u00a7c\u3010" + ManorDifficulty.IMPOSSIBLE.label() + "\u00b7\u672a\u89e3\u9501\u3011\u00a77\u8bf7\u5148\u5b8c\u6574\u901a\u5173\u666e\u901a\u5e84\u56ed\u6311\u6218\u3002")));
            return 0;
        }
        if (!p.m_6084_() || p.m_5833_() || p.m_284548_() != o.sl) {
            p.m_213846_((Component)Component.m_237113_((String)"\u00a7c\u3010\u751c\u82f9\u679c\u5e84\u56ed\u3011\u8bf7\u4ee5\u5b58\u6d3b\u7684\u975e\u65c1\u89c2\u72b6\u6001\u8fd4\u56de\u89e6\u53d1\u7ef4\u5ea6\uff0c\u518d\u786e\u8ba4\u6311\u6218\u3002"));
            return 0;
        }
        if (!SummonGate.allow((Level)o.sl, (Player)p, "\u9690\u85cf Boss(\u751c\u82f9\u679c\u5e84\u56ed)")) {
            pending = null;
            return 0;
        }
        if (finalConfirmation) {
            if (!o.finalConfirmation || token == null) {
                p.m_213846_((Component)Component.m_237113_((String)"\u00a77\u3010\u751c\u82f9\u679c\u5e84\u56ed\u3011\u8bf7\u5148\u5b8c\u6210\u7b2c\u4e00\u6b21\u786e\u8ba4\uff0c\u518d\u70b9\u51fb\u6700\u65b0\u7684\u3010\u786e\u8ba4\u5f00\u6218 2/2\u3011\u3002"));
                return 0;
            }
        } else {
            if (!o.finalConfirmation) {
                o.chooser = p.m_20148_();
                o.chooserName = p.m_7755_().getString();
                o.finalConfirmation = true;
                o.difficulty = difficulty;
                o.token = UUID.randomUUID();
                o.expireTick = o.sl.m_46467_() + 1200L;
                FTBossMod.LOGGER.info("[FTBoss][Challenge] {} \u5b8c\u6210\u9996\u6b21\u786e\u8ba4\uff0c\u7b49\u5f85\u7b2c\u4e8c\u6b21\u786e\u8ba4", (Object)p.m_7755_().getString());
                HiddenBossChallenge.sendPrompt(p, o);
                return 1;
            }
            HiddenBossChallenge.sendPrompt(p, o);
            return 0;
        }
        pending = null;
        HiddenBossChallenge.giftToPlayers(o.sl, o.anchor);
        HiddenBossIntro.start(o.sl, o.anchor, o.from, 9, p.m_20148_(), o.difficulty, o.lines);
        if (o.difficulty == ManorDifficulty.NORMAL) {
            HiddenBossChallenge.grantAdvancement(p, "easter_egg_memory");
        }
        for (ServerPlayer q : o.sl.m_6907_()) {
            if (q.m_20275_(o.anchor.f_82479_, o.anchor.f_82480_, o.anchor.f_82481_) > 9216.0) continue;
            q.m_213846_((Component)Component.m_237113_((String)("\u00a76\u3010\u751c\u82f9\u679c\u5e84\u56ed\u00b7" + o.difficulty.title() + "\u3011\u00a7f\u6311\u6218\u5f00\u59cb\uff01" + (o.difficulty == ManorDifficulty.IMPOSSIBLE ? "\u00a7c\u8be5\u96be\u5ea6\u51e0\u4e4e\u65e0\u6cd5\u901a\u8fc7\u800c\u4e14\u4e0d\u4f1a\u83b7\u53d6\u6210\u5c31" : "\u00a77\u795d\u4f60\u597d\u8fd0\u3002"))));
        }
        FTBossMod.LOGGER.info("[FTBoss][Challenge] {} \u5b8c\u6210\u4e24\u6b21\u786e\u8ba4 -> \u5f00\u59cb\u5165\u573a\u6f14\u51fa\uff0c\u96be\u5ea6={}", (Object)p.m_7755_().getString(), (Object)o.difficulty.id());
        return 1;
    }

    private static void grantAdvancement(ServerPlayer player, String id) {
        try {
            Advancement advancement = player.f_8924_.m_129889_().m_136041_(new ResourceLocation("ftboss", id));
            if (advancement == null) {
                FTBossMod.LOGGER.warn("[FTBoss][\u8fdb\u5ea6] \u627e\u4e0d\u5230\u8fdb\u5ea6 {}:{}", (Object)"ftboss", (Object)id);
                return;
            }
            player.m_8960_().m_135988_(advancement, "triggered");
            FTBossMod.LOGGER.info("[FTBoss][\u8fdb\u5ea6] {} \u89e3\u9501\u8fdb\u5ea6 {}({})", new Object[]{player.m_7755_().getString(), advancement.m_138320_().m_14977_().getString(), id});
        }
        catch (Throwable t) {
            FTBossMod.LOGGER.warn("[FTBoss][\u8fdb\u5ea6] \u53d1\u653e {} \u5931\u8d25: {}", (Object)id, (Object)t.toString());
        }
    }

    private static int decline(ServerPlayer p, UUID token) {
        Offer o = HiddenBossChallenge.currentOffer(p, token);
        if (o == null) {
            return 0;
        }
        pending = null;
        BossMusic.stopManorCue("\u73a9\u5bb6\u653e\u5f03");
        for (ServerPlayer q : o.sl.m_6907_()) {
            if (q.m_20275_(o.anchor.f_82479_, o.anchor.f_82480_, o.anchor.f_82481_) > 9216.0) continue;
            q.m_213846_((Component)Component.m_237113_((String)"\u00a77\u3010\u751c\u82f9\u679c\u5e84\u56ed\u3011\u5df2\u653e\u5f03\u672c\u6b21\u6311\u6218\u3002"));
        }
        FTBossMod.LOGGER.info("[FTBoss][Challenge] {} \u653e\u5f03\u4e86\u9690\u85cf Boss \u6311\u6218", (Object)p.m_7755_().getString());
        return 1;
    }

    private static void giftToPlayers(ServerLevel sl, Vec3 anchor) {
        int n = 0;
        for (ServerPlayer p : sl.m_6907_()) {
            if (p.m_20275_(anchor.f_82479_, anchor.f_82480_, anchor.f_82481_) > 9216.0) continue;
            ItemStack apples = new ItemStack((ItemLike)Items.f_42436_, 64);
            if (!p.m_150109_().m_36054_(apples)) {
                p.m_36176_(apples, false);
            }
            p.m_7292_(new MobEffectInstance(MobEffects.f_19617_, 12000, 99, false, true, true));
            p.m_5661_((Component)Component.m_237113_((String)"\u00a7a\u3010\u751c\u82f9\u679c\u5e84\u56ed\u3011\u8865\u7ed9: \u00a7f\u91d1\u82f9\u679c x64 \u00a77+ \u00a7f\u4f24\u5bb3\u5438\u6536 400 \u70b9(10 \u5206\u949f)"), false);
            ++n;
        }
        FTBossMod.LOGGER.info("[FTBoss][Challenge] \u5df2\u53d1\u653e\u6311\u6218\u8865\u7ed9: {} \u540d\u73a9\u5bb6(\u91d1\u82f9\u679c x{}, \u4f24\u5bb3\u5438\u6536 {} \u70b9 {} \u5206\u949f)", new Object[]{n, 64, 400, 10});
    }

    private static final class Offer {
        final ServerLevel sl;
        final Vec3 anchor;
        final Vec3 from;
        final String[] lines;
        long expireTick;
        UUID chooser;
        String chooserName;
        UUID token = UUID.randomUUID();
        boolean finalConfirmation;
        ManorDifficulty difficulty = ManorDifficulty.NORMAL;

        Offer(ServerLevel sl, Vec3 anchor, Vec3 from, String[] lines, long expireTick, UUID chooser, String chooserName) {
            this.sl = sl;
            this.anchor = anchor;
            this.from = from;
            this.lines = lines;
            this.expireTick = expireTick;
            this.chooser = chooser;
            this.chooserName = chooserName;
        }
    }
}

