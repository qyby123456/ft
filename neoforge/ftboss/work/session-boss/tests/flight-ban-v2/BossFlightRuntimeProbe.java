package ftboss.bossprobe;

import com.google.gson.GsonBuilder;
import com.mojang.authlib.GameProfile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.farmingtales.ftboss.boss.ai.ManorFlyBan;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.EventHooks;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@Mod("ftboss_bossprobe")
public final class BossFlightRuntimeProbe {
    private static ServerPlayer watchedPlayer;
    private static boolean restoreFlight;
    private static int preEvents;
    private static int postEvents;

    public BossFlightRuntimeProbe() {
        NeoForge.EVENT_BUS.addListener(BossFlightRuntimeProbe::started);
        NeoForge.EVENT_BUS.addListener(EventPriority.LOW, false, PlayerTickEvent.Pre.class, BossFlightRuntimeProbe::lateGrant);
        NeoForge.EVENT_BUS.addListener(EventPriority.LOW, false, PlayerTickEvent.Post.class, BossFlightRuntimeProbe::lateGrant);
    }

    private static void lateGrant(PlayerTickEvent event) {
        if (event.getEntity() != watchedPlayer) return;
        if (event instanceof PlayerTickEvent.Pre) preEvents++;
        if (event instanceof PlayerTickEvent.Post) postEvents++;
        if (restoreFlight) grantFlight(watchedPlayer);
    }

    private static void grantFlight(ServerPlayer player) {
        player.getAbilities().mayfly = true;
        player.getAbilities().flying = true;
        player.startFallFlying();
    }

    private static void require(boolean condition, String label, List<String> cases) {
        if (!condition) throw new AssertionError(label);
        cases.add(label);
    }

    private static void checkPhase(ServerPlayer player, boolean expectedFlight, String label, List<String> cases) {
        grantFlight(player);
        EventHooks.firePlayerTickPre(player);
        require(player.getAbilities().flying == expectedFlight, label + "PreAbilityFlight", cases);
        require(player.isFallFlying() == expectedFlight, label + "PreElytra", cases);
        require(player.getAbilities().mayfly, label + "PreOriginalMayflyPreserved", cases);
        grantFlight(player);
        EventHooks.firePlayerTickPost(player);
        require(player.getAbilities().flying == expectedFlight, label + "PostAbilityFlight", cases);
        require(player.isFallFlying() == expectedFlight, label + "PostElytra", cases);
        require(player.getAbilities().mayfly, label + "PostOriginalMayflyPreserved", cases);
    }

    private static void started(ServerStartedEvent event) {
        Map<String, Object> report = new LinkedHashMap<>();
        List<String> cases = new ArrayList<>();
        report.put("cases", cases);
        report.put("failures", 0);
        report.put("authenticatedMultiplayerAccepted", false);
        report.put("completeBattleAccepted", false);
        report.put("authorityWrittenByReflection", false);
        report.put("weaponCandidateValidated", false);
        report.put("clientRenderingAccepted", false);
        try {
            boolean candidate = "candidate".equals(System.getProperty("ftboss.flight.mode"));
            var server = event.getServer();
            var level = server.overworld();
            GameProfile profile = new GameProfile(UUID.fromString("b0550000-0000-0000-0000-000000000006"), "boss_flight_probe");
            ServerPlayer player = new ServerPlayer(server, level, profile, ClientInformation.createDefault());
            Connection connection = new Connection(PacketFlow.SERVERBOUND);
            player.connection = new ServerGamePacketListenerImpl(server, connection, player, CommonListenerCookie.createInitial(profile, false));
            watchedPlayer = player;
            player.moveTo(0.5, 70.0, 0.5, 0.0F, 0.0F);
            player.setItemSlot(EquipmentSlot.CHEST, new ItemStack(Items.ELYTRA));
            player.setGameMode(GameType.SURVIVAL);
            require(server.getPlayerList().getPlayer(profile.getId()) == null, "ScratchEntityNotAuthenticatedPlayer", cases);
            ManorFlyBan.clear(level);
            restoreFlight = false;
            checkPhase(player, true, "NoBanWindow", cases);
            ManorFlyBan.trigger(level, player.getX(), player.getY(), player.getZ());
            require(ManorFlyBan.active(player), "OriginalPublicWindowActive", cases);
            restoreFlight = true;
            checkPhase(player, !candidate, "LowPriorityForeignRegrant", cases);
            int beforePre = preEvents;
            int beforePost = postEvents;
            player.doTick();
            require(preEvents == beforePre + 1 && postEvents == beforePost + 1, "ActualNativePlayerTickEmitsBothPhases", cases);
            require(player.getAbilities().flying == !candidate, "ActualNativeTickTailFlightState", cases);
            require(player.isFallFlying() == !candidate, "ActualNativeTickTailElytraState", cases);
            require(player.getAbilities().mayfly, "ActualNativeTickOriginalMayflyPreserved", cases);
            restoreFlight = false;
            player.setGameMode(GameType.CREATIVE);
            require(!ManorFlyBan.active(player), "OriginalCreativeExemption", cases);
            checkPhase(player, true, "CreativeControl", cases);
            player.setGameMode(GameType.SPECTATOR);
            require(!ManorFlyBan.active(player), "OriginalSpectatorExemption", cases);
            checkPhase(player, true, "SpectatorControl", cases);
            player.setGameMode(GameType.SURVIVAL);
            player.moveTo(500.5, 70.0, 500.5, 0.0F, 0.0F);
            require(!ManorFlyBan.active(player), "OriginalOutOfRangeExemption", cases);
            checkPhase(player, true, "OutOfRangeControl", cases);
            ManorFlyBan.clear(level);
            require(!ManorFlyBan.active(player), "OriginalWindowCleanup", cases);
            try { Class.forName("net.farmingtales.ftboss.item.FoodWeaponFinalDamage"); throw new AssertionError("Paused weapon implementation loaded"); } catch (ClassNotFoundException absent) { cases.add("PausedWeaponImplementationAbsent"); }
            report.put("actualMissingOriginalTailFlightProtectionDemonstrated", !candidate);
            report.put("originalTailFlightProtectionRestored", candidate);
            report.put("actualNativePlayerTickBothPhasesAccepted", true);
            report.put("scratchUnauthenticatedEntityOnly", true);
            report.put("preEvents", preEvents);
            report.put("postEvents", postEvents);
        } catch (Throwable failure) {
            report.put("failures", 1);
            report.put("failure", failure.toString());
            failure.printStackTrace();
        } finally {
            restoreFlight = false;
            ManorFlyBan.clear(event.getServer().overworld());
            if (watchedPlayer != null) watchedPlayer.discard();
            watchedPlayer = null;
        }
        try {
            Files.writeString(Path.of("actual-flight-ban-probe.json"), new GsonBuilder().setPrettyPrinting().create().toJson(report));
        } catch (Exception failure) { throw new RuntimeException(failure); }
    }
}
