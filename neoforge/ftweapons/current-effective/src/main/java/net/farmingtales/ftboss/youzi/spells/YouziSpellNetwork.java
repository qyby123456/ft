package net.farmingtales.ftboss.youzi.spells;

import io.redspace.ironsspellbooks.api.magic.MagicData;
import net.farmingtales.ftboss.integ.spells.BossSpellTuning;
import net.farmingtales.ftboss.skill.SpellVisualPacket;
import net.neoforged.neoforge.network.PacketDistributor;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.EventBusSubscriber.Bus;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

@EventBusSubscriber(
    modid = "ftboss",
    bus = Bus.MOD
)
public final class YouziSpellNetwork {
    private static final String VERSION = "1";

    private static boolean registered;

    @SubscribeEvent
    public static void setup(RegisterPayloadHandlersEvent event) {
        register(event);
    }

    public static synchronized void register(RegisterPayloadHandlersEvent event) {
        if (!registered) {
            event.registrar("1").optional().playToClient(YouziSpellPacket.TYPE, YouziSpellPacket.STREAM_CODEC, YouziSpellPacket::handle);
            registered = true;
        }
    }

    public static void emit(ServerLevel var0, SpellVisualPacket var1) {
        if (registered && var1.valid() && supports(var1.spell().id)) {
            if (var0.getEntity(var1.owner()) instanceof LivingEntity var3 && var3.isAlive()) {
                MagicData var4 = MagicData.getPlayerMagicData(var3);
                long var5 = var0.getGameTime();
                int var7 = var4.isCasting() ? Math.max(1, var4.getCastDuration()) : 0;
                long var8 = var7 == 0 ? var5 : Math.max(0L, var5 - (long)var7 + (long)var4.getCastDurationRemaining());
                YouziSpellPacket var10 = new YouziSpellPacket(var1, var5, var8, var7, BossSpellTuning.radiusScale(var3));

                for (ServerPlayer var12 : var0.players()) {
                    if (var12.connection != null && var12.connection.hasChannel(YouziSpellPacket.TYPE)) {
                        double var13 = Math.min(
                            var12.distanceToSqr(var1.start().x, var1.start().y, var1.start().z), var12.distanceToSqr(var1.end().x, var1.end().y, var1.end().z)
                        );
                        if (var13 <= 4096.0) {
                            PacketDistributor.sendToPlayer(var12, var10);
                        }
                    }
                }

                return;
            }
        }
    }

    private static boolean supports(String var0) {
        return var0.equals("effigy_volley") || var0.equals("effigy_ward") || var0.equals("effigy_procession");
    }
}
