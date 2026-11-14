package net.farmingtales.ftboss.qingyilight.client;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.qingyilight.LightGeometry;
import net.farmingtales.ftboss.qingyilight.LightPacket;
import net.farmingtales.ftboss.qingyilight.LightQueue;
import net.farmingtales.ftboss.qingyilight.LightRules;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public final class LightClient {
    private static final LightQueue QUEUE = new LightQueue();
    private static final Map<UUID, ArrayDeque<Vec3>> HISTORY = new HashMap<>();
    private static ClientLevel activeWorld;

    private LightClient() {
    }

    private static void synchronize(ClientLevel var0) {
        if (var0 != activeWorld) {
            QUEUE.clear();
            HISTORY.clear();
            activeWorld = var0;
        }
    }

    public static void accept(LightPacket var0) {
        ClientLevel var1 = Minecraft.getInstance().level;
        synchronize(var1);
        if (var1 != null && var1.dimension().location().equals(var0.dimension())) {
            QUEUE.accept(var0, var1.getGameTime());
        }
    }

    private static boolean validOwner(ClientLevel var0, LightPacket var1) {
        Entity var2 = var0.getEntity(var1.ownerId());
        if (var2 == null) {
            return true;
        } else {
            if (var2 instanceof EasterBossEntity var3
                && var3.getUUID().equals(var1.owner())
                && var3.getVariant() == 5
                && !var3.isMini()
                && var3.isAlive()
                && !var3.isNoAi()) {
                return true;
            }

            return false;
        }
    }

    public static CompoundTag legacyCue(EasterBossEntity var0) {
        return filterLegacyCue(var0.getSkillCue(), var0);
    }

    public static CompoundTag filterLegacyCue(CompoundTag var0, EasterBossEntity var1) {
        ClientLevel var2 = Minecraft.getInstance().level;
        synchronize(var2);
        return var2 != null
                && var1.getVariant() == 5
                && QUEUE.snapshot(var2.getGameTime())
                    .stream()
                    .anyMatch(
                        var2x -> var2x.owner().equals(var1.getUUID())
                                && var2x.kind() <= 3
                                && var2x.phase() == (var0.getBoolean("released") ? 1 : 0)
                                && var2x.born() == var0.getLong("start")
                                && Math.abs(var2x.radius() - var0.getDouble("radius")) < 0.001
                    )
            ? new CompoundTag()
            : var0;
    }

    public static void tick() {
        ClientLevel var0 = Minecraft.getInstance().level;
        synchronize(var0);
        if (var0 != null) {
            QUEUE.removeIf(
                var1x -> !validOwner(var0, var1x)
                        || var1x.entityId() >= 0
                            && (
                                var0.getEntity(var1x.entityId()) == null && var0.getGameTime() - var1x.born() > 40L
                                    || var0.getEntity(var1x.entityId()) != null
                                        && (!var0.getEntity(var1x.entityId()).getUUID().equals(var1x.entity()) || var0.getEntity(var1x.entityId()).isRemoved())
                            )
            );
            List<LightPacket> var1 = QUEUE.snapshot(var0.getGameTime());
            HISTORY.keySet().removeIf(var1x -> var1.stream().noneMatch(var1xx -> var1x.equals(var1xx.entity())));

            for (LightPacket var3 : var1) {
                if (var3.entityId() >= 0 && validOwner(var0, var3)) {
                    Entity var4 = var0.getEntity(var3.entityId());
                    if (var4 != null && var4.getUUID().equals(var3.entity()) && (HISTORY.containsKey(var3.entity()) || HISTORY.size() < 48)) {
                        ArrayDeque var5 = HISTORY.computeIfAbsent(var3.entity(), var0x -> new ArrayDeque<>());
                        Vec3 var6 = var4.position();
                        if (!var5.isEmpty() && ((Vec3)var5.getLast()).distanceToSqr(var6) > 16.0) {
                            var5.clear();
                        }

                        if (var5.isEmpty() || ((Vec3)var5.getLast()).distanceToSqr(var6) > 1.0E-6) {
                            var5.addLast(var6);
                        }

                        while (var5.size() > 7) {
                            var5.removeFirst();
                        }
                    }
                }
            }
        }
    }

    public static List<LightClient.Draw> scenes(float var0) {
        ClientLevel var1 = Minecraft.getInstance().level;
        synchronize(var1);
        ArrayList var2 = new ArrayList();
        if (var1 == null) {
            return var2;
        } else {
            Iterator var3 = QUEUE.snapshot(var1.getGameTime()).iterator();

            while (true) {
                LightPacket var4;
                double var5;
                Vec3 var7;
                Vec3 var8;
                while (true) {
                    while (true) {
                        if (!var3.hasNext()) {
                            return var2;
                        }

                        var4 = (LightPacket)var3.next();
                        if (validOwner(var1, var4)) {
                            var5 = (double)((float)var1.getGameTime() + var0 - (float)var4.born());
                            if (!(var5 < 0.0)) {
                                var7 = var4.origin();
                                var8 = var4.direction();
                                Entity var9 = var1.getEntity(var4.ownerId());
                                if (var4.kind() != 4 && var4.kind() != 9) {
                                    break;
                                }

                                if (var9 != null) {
                                    var7 = var9.getPosition(var0);
                                    break;
                                }
                            }
                        }
                    }

                    if (var4.entityId() < 0) {
                        break;
                    }

                    Entity var10 = var1.getEntity(var4.entityId());
                    if (var10 != null && var10.getUUID().equals(var4.entity()) && !var10.isRemoved()) {
                        var7 = var10.getPosition(var0);
                        var8 = var10.getDeltaMovement().normalize();
                        break;
                    }
                }

                LightGeometry.Frame var18 = LightGeometry.frame(var4.kind(), var4.phase(), var5, var4.duration(), var4.radius(), var4.pulse());
                var2.add(new LightClient.Draw(var4, var7, var8, var18));
                ArrayDeque<Vec3> var11 = HISTORY.get(var4.entity());
                if (var11 != null && var11.size() >= 2) {
                    Vec3[] var12 = var11.toArray(Vec3[]::new);
                    ArrayList var13 = new ArrayList();

                    for (int var14 = 1; var14 < var12.length; var14++) {
                        if (LightRules.trailSegment(var12[var14].distanceToSqr(var12[var14 - 1]))) {
                            ArrayList var15 = new ArrayList();
                            Vec3 var16 = var12[var14 - 1].subtract(var7);
                            Vec3 var17 = var12[var14].subtract(var7);
                            LightGeometry.prism(
                                var15,
                                new double[]{var16.x, var16.y, var16.z},
                                new double[]{var17.x, var17.y, var17.z},
                                0.012 + 0.008 * (double)var14 / (double)var12.length
                            );
                            var13.add(
                                new LightGeometry.Layer(
                                    false,
                                    "actual_entity_history",
                                    0.12 + 0.38 * (double)var14 / (double)var12.length,
                                    var4.kind() == 8 ? 12687320 : 9562611,
                                    var15
                                )
                            );
                        }
                    }

                    var2.add(new LightClient.Draw(var4, var7, new Vec3(0.0, 0.0, 1.0), new LightGeometry.Frame(var13)));
                }
            }
        }
    }

    public static record Draw(LightPacket packet, Vec3 anchor, Vec3 direction, LightGeometry.Frame geometry) {
    }
}
