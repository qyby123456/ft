package net.farmingtales.ftboss.youzi.motion;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.skill.SpellVisualPacket;
import net.farmingtales.ftboss.util.SpellVisualRules.Phase;
import net.farmingtales.ftboss.youzi.spells.YouziSpellPacket;
import net.farmingtales.ftboss.youzi.spells.YouziSpellTiming;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent.LoggingOut;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.event.level.LevelEvent.Unload;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

@EventBusSubscriber(
    modid = "ftboss",
    value = {Dist.CLIENT}
)
public final class YouziBodyState {
    private static final Map<UUID, YouziBodyState.Active> ACTIVE = new LinkedHashMap<>();
    private static ClientLevel world;

    private static void synchronize() {
        ClientLevel var0 = Minecraft.getInstance().level;
        if (var0 != world) {
            ACTIVE.clear();
            world = var0;
        }
    }

    private static boolean owner(int var0, UUID var1) {
        return world != null
            && world.getEntity(var0) instanceof EasterBossEntity var2
            && var2.getVariant() == 0
            && var2.isAlive()
            && var2.getUUID().equals(var1);
    }

    private static void put(YouziBodyState.Active var0) {
        if (ACTIVE.size() >= 128 && !ACTIVE.containsKey(var0.actor())) {
            ACTIVE.remove(ACTIVE.keySet().iterator().next());
        }

        ACTIVE.put(var0.actor(), var0);
    }

    public static void summon(YouziBodyPacket var0) {
        synchronize();
        if (world != null
            && world.dimension().location().toString().equals(var0.dimension())
            && owner(var0.actorId(), var0.actor())
            && world.getGameTime() - var0.tick() <= 18L
            && world.getGameTime() >= var0.tick() - 2L) {
            YouziBodyState.Active var1 = ACTIVE.get(var0.actor());
            if (var1 == null || var1.refreshed() < var0.tick() && !var1.kind().equals("CHARGE")) {
                put(new YouziBodyState.Active(var0.actorId(), var0.actor(), "clone_summon", "SUMMON", var0.tick(), var0.tick(), 18));
            }
        }
    }

    public static void spell(YouziSpellPacket var0) {
        if (var0.visual().phase() != Phase.CHARGE || var0.castDuration() > 0) {
            synchronize();
            SpellVisualPacket var1 = var0.visual();
            if (world != null && world.dimension().location().equals(var1.dimension()) && owner(var1.ownerId(), var1.owner()) && var1.phase() != Phase.BEAM) {
                String var3 = var1.spell().id;

                String var2 = switch (var3) {
                    case "effigy_volley" -> "effigy_volley_cast";
                    case "effigy_ward" -> "effigy_ward_cast";
                    case "effigy_procession" -> "effigy_procession_cast";
                    default -> null;
                };
                if (var2 != null && YouziSpellTiming.eventFresh(world.getGameTime(), var0.sentTick(), var1.phase().name())) {
                    YouziBodyState.Active var5 = ACTIVE.get(var1.owner());
                    if (var5 == null || YouziSpellTiming.ordered(var0.sentTick(), var1.phase().name(), var5.refreshed(), var5.kind())) {
                        if (var1.phase() == Phase.PULSE) {
                            if (var5 != null && var5.clip().equals(var2) && var5.kind().equals("RELEASE")) {
                                put(
                                    new YouziBodyState.Active(
                                        var5.actorId(), var5.actor(), var5.clip(), var5.kind(), var5.started(), var0.sentTick(), var5.castDuration()
                                    )
                                );
                            }
                        } else {
                            boolean var6 = var1.phase() == Phase.CHARGE;
                            put(
                                new YouziBodyState.Active(
                                    var1.ownerId(),
                                    var1.owner(),
                                    var2,
                                    var6 ? "CHARGE" : "RELEASE",
                                    var6 ? var0.castStarted() : var0.sentTick(),
                                    var0.sentTick(),
                                    var0.castDuration()
                                )
                            );
                        }
                    }
                }
            }
        }
    }

    public static YouziBodyState.Motion sample(EasterBossEntity var0, float var1, YouziCharacterPose var2) {
        synchronize();
        YouziBodyState.Active var3 = ACTIVE.get(var0.getUUID());
        if (var3 != null && owner(var3.actorId(), var3.actor())) {
            double var4 = (double)((float)world.getGameTime() + var1);
            YouziBodyClock.Sample var6 = YouziBodyClock.sample(
                var3.kind(),
                var4 - (double)var3.started(),
                (double)var3.castDuration(),
                var2.duration(var3.clip()),
                var2.chargeEnd(var3.clip()),
                var4 - (double)var3.refreshed()
            );
            if (var6.expired()) {
                ACTIVE.remove(var3.actor());
                return null;
            } else {
                return new YouziBodyState.Motion(var3.clip(), var6.phase(), var6.weight());
            }
        } else {
            return null;
        }
    }

    @SubscribeEvent
    public static void tick(ClientTickEvent.Post var0) {
        synchronize();
        if (world != null) {
            ACTIVE.values()
                .removeIf(
                    var0x -> !owner(var0x.actorId(), var0x.actor())
                            || (var0x.kind().equals("CHARGE") ? world.getGameTime() - var0x.refreshed() > 8L : world.getGameTime() - var0x.started() > 100L)
                );
        }
    }

    @SubscribeEvent
    public static void unload(Unload var0) {
        if (var0.getLevel() == world) {
            ACTIVE.clear();
            world = null;
        }
    }

    @SubscribeEvent
    public static void logout(LoggingOut var0) {
        ACTIVE.clear();
        world = null;
    }

    private static record Active(int actorId, UUID actor, String clip, String kind, long started, long refreshed, int castDuration) {
    }

    public static record Motion(String clip, double phase, double weight) {
    }
}
