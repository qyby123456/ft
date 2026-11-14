/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.ParticleStatus
 *  net.minecraft.client.multiplayer.ClientLevel
 *  net.minecraft.client.renderer.RenderType
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.level.ClipContext
 *  net.minecraft.world.level.ClipContext$Block
 *  net.minecraft.world.level.ClipContext$Fluid
 *  net.minecraft.world.phys.AABB
 *  net.minecraft.world.phys.Vec3
 *  net.minecraftforge.api.distmarker.Dist
 *  net.minecraftforge.client.event.ClientPlayerNetworkEvent$LoggingOut
 *  net.minecraftforge.client.event.RenderLevelStageEvent
 *  net.minecraftforge.client.event.RenderLevelStageEvent$Stage
 *  net.minecraftforge.event.level.LevelEvent$Unload
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber
 *  org.joml.Matrix3f
 *  org.joml.Matrix4f
 *  org.joml.Quaternionf
 *  org.joml.Quaternionfc
 *  org.joml.Vector3f
 *  org.joml.Vector3fc
 */
package net.farmingtales.ftboss.client.xueli_combat;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.farmingtales.ftboss.client.xueli_combat.CombatAsset;
import net.farmingtales.ftboss.client.xueli_combat.CombatPose;
import net.farmingtales.ftboss.client.xueli_combat.CombatSafety;
import net.farmingtales.ftboss.combat.xueli.CombatPacket;
import net.farmingtales.ftboss.combat.xueli.CombatRules;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.ParticleStatus;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Quaternionfc;
import org.joml.Vector3f;
import org.joml.Vector3fc;

@Mod.EventBusSubscriber(modid="ftboss", value={Dist.CLIENT})
public final class XueliCombatRenderer {
    private static final Map<String, CombatAsset.Effect> ASSETS = CombatAsset.load();
    private static final RenderType LIGHT = RenderType.m_234338_((ResourceLocation)new ResourceLocation("ftboss", "xueli_combat/frost_arcana.png"));
    private static final List<Activation> ACTIVE = new ArrayList<Activation>();
    private static final Map<StampKey, Long> STAMPS = new HashMap<StampKey, Long>();
    private static ClientLevel world;

    private XueliCombatRenderer() {
    }

    private static void clear() {
        ACTIVE.clear();
        STAMPS.clear();
        world = null;
    }

    @SubscribeEvent
    public static void disconnect(ClientPlayerNetworkEvent.LoggingOut loggingOut) {
        XueliCombatRenderer.clear();
    }

    @SubscribeEvent
    public static void unload(LevelEvent.Unload unload) {
        if (unload.getLevel() == world) {
            XueliCombatRenderer.clear();
        }
    }

    public static void accept(CombatPacket combatPacket) {
        ClientLevel clientLevel = Minecraft.m_91087_().f_91073_;
        if (clientLevel == null || combatPacket == null || !combatPacket.valid() || !combatPacket.dimension().equals((Object)clientLevel.m_46472_().m_135782_())) {
            return;
        }
        if (world != clientLevel) {
            XueliCombatRenderer.clear();
            world = clientLevel;
        }
        long l = clientLevel.m_46467_();
        int n = (combatPacket.kind() == 6 || combatPacket.kind() == 11) && !combatPacket.guards().isEmpty() ? combatPacket.guards().get(0).entityId() : combatPacket.ownerId();
        StampKey stampKey2 = new StampKey(combatPacket.owner(), combatPacket.kind(), n);
        Long l2 = STAMPS.get(stampKey2);
        if (l2 != null && !CombatRules.fresh(combatPacket.stamp(), l2)) {
            return;
        }
        Long l3 = STAMPS.get(new StampKey(combatPacket.owner(), combatPacket.kind() == 0 ? 1 : (combatPacket.kind() == 3 ? 4 : (combatPacket.kind() == 7 ? 8 : (combatPacket.kind() == 9 ? 10 : -1))), combatPacket.ownerId()));
        if (l3 != null && combatPacket.stamp() <= l3) {
            return;
        }
        STAMPS.put(stampKey2, combatPacket.stamp());
        ACTIVE.removeIf(activation -> activation.expired(l));
        if (combatPacket.kind() == 1) {
            ACTIVE.removeIf(activation -> activation.packet.owner().equals(combatPacket.owner()) && activation.packet.kind() == 0);
        }
        if (combatPacket.kind() == 4) {
            ACTIVE.removeIf(activation -> activation.packet.owner().equals(combatPacket.owner()) && activation.packet.kind() == 3);
        }
        if (combatPacket.kind() == 8) {
            ACTIVE.removeIf(activation -> activation.packet.owner().equals(combatPacket.owner()) && activation.packet.kind() == 7);
        }
        if (combatPacket.kind() == 10) {
            ACTIVE.removeIf(activation -> activation.packet.owner().equals(combatPacket.owner()) && activation.packet.kind() == 9);
        }
        if (CombatRules.persistent(combatPacket.kind()) || combatPacket.kind() == 6 || combatPacket.kind() == 11) {
            for (Activation activation2 : ACTIVE) {
                if (!activation2.packet.owner().equals(combatPacket.owner()) || activation2.packet.kind() != combatPacket.kind() || activation2.targetId() != n) continue;
                activation2.packet = combatPacket;
                activation2.received = l;
                if (!CombatRules.persistent(combatPacket.kind())) {
                    activation2.born = Math.min(l, combatPacket.stamp());
                }
                return;
            }
        }
        ACTIVE.add(new Activation(combatPacket, l));
        while (ACTIVE.size() > 64) {
            ACTIVE.remove(0);
        }
        if (STAMPS.size() > 256) {
            STAMPS.keySet().removeIf(stampKey -> ACTIVE.stream().noneMatch(activation -> activation.packet.owner().equals(stampKey.owner())));
        }
    }

    public static CompoundTag cue(EasterBossEntity easterBossEntity) {
        return XueliCombatRenderer.filterLegacyCue(easterBossEntity.getSkillCue(), easterBossEntity);
    }

    public static CompoundTag filterLegacyCue(CompoundTag compoundTag, EasterBossEntity easterBossEntity) {
        if (world != null && easterBossEntity.getVariant() == 10) {
            for (Activation activation : ACTIVE) {
                if (!activation.packet.owner().equals(easterBossEntity.m_20148_()) || activation.packet.kind() != 3 && activation.packet.kind() != 4 && activation.packet.kind() != 7 && activation.packet.kind() != 8 || activation.expired(world.m_46467_())) continue;
                return new CompoundTag();
            }
        }
        return compoundTag;
    }

    private static Entity entity(ClientLevel clientLevel, int n, UUID uUID) {
        Entity entity = clientLevel.m_6815_(n);
        return entity != null && entity.m_20148_().equals(uUID) && entity.m_6084_() && !entity.m_213877_() ? entity : null;
    }

    private static Vec3 position(Entity entity, float f) {
        return new Vec3(entity.f_19790_ + (entity.m_20185_() - entity.f_19790_) * (double)f, entity.f_19791_ + (entity.m_20186_() - entity.f_19791_) * (double)f, entity.f_19792_ + (entity.m_20189_() - entity.f_19792_) * (double)f);
    }

    private static double[] vector(Vec3 vec3) {
        return new double[]{vec3.f_82479_, vec3.f_82480_, vec3.f_82481_};
    }

    private static void append(List<Surface> list, CombatAsset.Effect effect, Placement placement, double d, double d2, double[] dArray2, double[] dArray3, boolean bl, List<double[]> list2, int n) {
        CombatPose.State[] stateArray = (CombatPose.State[])effect.bones().stream().map(bone -> CombatPose.sample(bone.track(), d)).toArray(CombatPose.State[]::new);
        for (CombatAsset.Face face : effect.faces()) {
            List<CombatSafety.Corner> list3;
            if (list.size() >= n) {
                return;
            }
            CombatAsset.Bone bone2 = effect.bones().get(face.bone());
            CombatPose.State state = stateArray[face.bone()];
            double d3 = face.alpha() * state.alpha() * d2;
            if (d3 < 0.003) continue;
            ArrayList<CombatSafety.Corner> arrayList = new ArrayList<CombatSafety.Corner>();
            for (int i = 0; i < face.positions().length; ++i) {
                double[] dArray4 = CombatPose.place(face.positions()[i], bone2.pivot(), state);
                Vector3f vector3f = new Vector3f((float)(dArray4[0] * placement.horizontal()), (float)(dArray4[1] * placement.vertical()), (float)(dArray4[2] * placement.longitudinal())).rotate((Quaternionfc)placement.rotation());
                arrayList.add(new CombatSafety.Corner(new double[]{placement.root().f_82479_ + (double)vector3f.x, placement.root().f_82480_ + (double)vector3f.y, placement.root().f_82481_ + (double)vector3f.z}, face.uv()[i]));
            }
            if (list2.stream().anyMatch(dArray -> CombatSafety.bodyOverlap(arrayList, dArray)) || (list3 = CombatSafety.cut(arrayList, dArray2, dArray3, 0.55)).size() < 3) continue;
            double d4 = list3.stream().mapToDouble(corner -> CombatSafety.distance(corner.position(), dArray2)).average().orElse(0.0);
            if (bl) {
                d3 *= CombatSafety.visibility(list3.get(0).position(), dArray2, dArray3, true, false);
            }
            if (!(d3 > 0.003)) continue;
            list.add(new Surface(list3, d3, d4));
        }
    }

    @SubscribeEvent
    public static void paint(RenderLevelStageEvent renderLevelStageEvent) {
        Object object;
        Object object2;
        int n;
        CombatPacket combatPacket;
        Activation activation2;
        AABB aABB;
        Object object32;
        if (renderLevelStageEvent.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES) {
            return;
        }
        Minecraft minecraft = Minecraft.m_91087_();
        ClientLevel clientLevel = minecraft.f_91073_;
        if (clientLevel == null || ACTIVE.isEmpty()) {
            return;
        }
        if (world != clientLevel) {
            XueliCombatRenderer.clear();
            return;
        }
        long l = clientLevel.m_46467_();
        ACTIVE.removeIf(activation -> activation.expired(l));
        if (ACTIVE.isEmpty()) {
            return;
        }
        float f = minecraft.m_91104_() ? 0.0f : renderLevelStageEvent.getPartialTick();
        Vec3 vec3 = renderLevelStageEvent.getCamera().m_90583_();
        double[] dArray = XueliCombatRenderer.vector(vec3);
        double d = Math.toRadians(renderLevelStageEvent.getCamera().m_90590_());
        double d2 = Math.toRadians(renderLevelStageEvent.getCamera().m_90589_());
        double[] dArray2 = new double[]{-Math.sin(d) * Math.cos(d2), -Math.sin(d2), Math.cos(d) * Math.cos(d2)};
        boolean bl = !renderLevelStageEvent.getCamera().m_90594_();
        ParticleStatus particleStatus = (ParticleStatus)minecraft.f_91066_.m_231929_().m_231551_();
        int n2 = particleStatus == ParticleStatus.MINIMAL ? 500 : (particleStatus == ParticleStatus.DECREASED ? 1200 : 2200);
        ArrayList<double[]> arrayList = new ArrayList<double[]>();
        for (Object object32 : clientLevel.m_104735_()) {
            if (!(object32 instanceof LivingEntity) || !(object32.m_20238_(vec3) < 36.0)) continue;
            aABB = object32.m_20191_().m_82400_(0.06);
            arrayList.add(new double[]{aABB.f_82288_, aABB.f_82289_, aABB.f_82290_, aABB.f_82291_, aABB.f_82292_, aABB.f_82293_});
        }
        ArrayList arrayList2 = new ArrayList();
        object32 = new ArrayList<Activation>(ACTIVE);
        object32.sort(Comparator.comparingDouble(activation -> activation.packet.origin().m_82557_(vec3)));
        aABB = object32.iterator();
        while (aABB.hasNext()) {
            double d3;
            double d4;
            Vec3 vec32;
            activation2 = (Activation)aABB.next();
            combatPacket = activation2.packet;
            int n3 = combatPacket.kind();
            n = n3 == 6 || n3 == 11 ? 1 : 0;
            object2 = n != 0 ? combatPacket.guards().get(0) : null;
            object = XueliCombatRenderer.entity(clientLevel, n != 0 ? ((CombatPacket.Anchor)object2).entityId() : combatPacket.ownerId(), n != 0 ? ((CombatPacket.Anchor)object2).uuid() : combatPacket.owner());
            if (object == null && !CombatRules.fixedGround(n3)) continue;
            Vec3 vec33 = vec32 = CombatRules.fixedGround(n3) ? combatPacket.origin() : XueliCombatRenderer.position((Entity)object, f);
            if (vec32.m_82557_(vec3) > 4096.0) continue;
            double d5 = (float)(l - activation2.born) + f;
            double d6 = (float)(l - activation2.received) + f;
            double d7 = CombatRules.time(n3, combatPacket.duration(), combatPacket.progress(), n3 == 9 ? (double)Math.max(0.0f, (float)(l - combatPacket.stamp()) + f) : d5);
            double d8 = CombatRules.persistent(n3) ? Math.min(1.0, ((double)combatPacket.duration() - d6) / 3.0) : Math.min(1.0, ((double)combatPacket.duration() - d5) / 2.0);
            CombatAsset.Effect effect = ASSETS.get(CombatRules.IDS[n3]);
            boolean bl2 = effect.anchor().equals("GROUND");
            double d9 = bl2 ? combatPacket.radius() : (d4 = n3 <= 2 ? combatPacket.radius() / 1.3 : 1.0);
            double d10 = bl2 ? 1.0 : (d3 = object == null ? 1.0 : Math.max(0.5, (double)object.m_20206_() / 1.8));
            if (n != 0 && object != null) {
                d4 = Math.max(1.0, (double)object.m_20205_() / Math.sqrt(2.0) / 0.52);
                d3 = n3 == 11 ? (double)object.m_20206_() / 1.8 : 1.0;
            }
            Quaternionf quaternionf = new Quaternionf().rotateY((float)Math.toRadians(combatPacket.yaw()));
            XueliCombatRenderer.append(arrayList2, effect, new Placement(vec32, quaternionf, d4, d3, d4), d7, d8, dArray, dArray2, bl, arrayList, n2);
            if (n3 == 0 && object != null) {
                for (CombatPacket.Anchor anchor : combatPacket.guards()) {
                    Vec3 vec34;
                    Vec3 vec35;
                    Vec3 vec36;
                    Vec3 vec37;
                    double d11;
                    Entity entity = XueliCombatRenderer.entity(clientLevel, anchor.entityId(), anchor.uuid());
                    if (entity == null || (d11 = (vec37 = (vec36 = clientLevel.m_45547_(new ClipContext(vec35 = XueliCombatRenderer.position(entity, f).m_82520_(0.0, Math.min(0.9, (double)entity.m_20206_() * 0.58), 0.0), vec34 = vec32.m_82520_(0.0, Math.min(1.05, (double)object.m_20206_() * 0.6), 0.0), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, entity)).m_82450_()).m_82546_(vec35)).m_82553_()) < 0.2 || d11 > 48.0) continue;
                    Quaternionf quaternionf2 = new Quaternionf().rotationTo((Vector3fc)new Vector3f(0.0f, 0.0f, 1.0f), (Vector3fc)new Vector3f((float)vec37.f_82479_, (float)vec37.f_82480_, (float)vec37.f_82481_).normalize());
                    XueliCombatRenderer.append(arrayList2, ASSETS.get("guard_thread"), new Placement(vec35, quaternionf2, 1.0, 1.0, d11), d7, d8, dArray, dArray2, bl, arrayList, n2);
                }
            }
            if (arrayList2.size() < n2) continue;
            break;
        }
        arrayList2.sort(Comparator.comparingDouble(Surface::depth).reversed());
        aABB = minecraft.m_91269_().m_110104_();
        activation2 = aABB.m_6299_(LIGHT);
        combatPacket = renderLevelStageEvent.getPoseStack().m_85850_().m_252922_();
        Matrix3f matrix3f = renderLevelStageEvent.getPoseStack().m_85850_().m_252943_();
        n = 0;
        object2 = arrayList2.iterator();
        while (object2.hasNext()) {
            object = (Surface)object2.next();
            for (int i = 1; i < ((Surface)object).corners().size() - 1 && n < n2; ++i) {
                CombatSafety.Corner corner = ((Surface)object).corners().get(0);
                CombatSafety.Corner corner2 = ((Surface)object).corners().get(i);
                CombatSafety.Corner corner3 = ((Surface)object).corners().get(i + 1);
                Vector3f vector3f = new Vector3f((float)(corner2.position()[0] - corner.position()[0]), (float)(corner2.position()[1] - corner.position()[1]), (float)(corner2.position()[2] - corner.position()[2])).cross((Vector3fc)new Vector3f((float)(corner3.position()[0] - corner.position()[0]), (float)(corner3.position()[1] - corner.position()[1]), (float)(corner3.position()[2] - corner.position()[2])));
                if ((double)vector3f.lengthSquared() < 1.0E-12) continue;
                vector3f.normalize();
                for (CombatSafety.Corner corner4 : List.of(corner, corner2, corner3, corner3)) {
                    int n4 = (int)Math.round(((Surface)object).alpha() * 255.0 * (bl ? CombatSafety.visibility(corner4.position(), dArray, dArray2, true, false) : 1.0));
                    activation2.m_252986_((Matrix4f)combatPacket, (float)(corner4.position()[0] - dArray[0]), (float)(corner4.position()[1] - dArray[1]), (float)(corner4.position()[2] - dArray[2])).m_6122_(255, 255, 255, Math.max(0, Math.min(255, n4))).m_7421_((float)corner4.uv()[0], (float)corner4.uv()[1]).m_86008_(655360).m_85969_(0xF000F0).m_252939_(matrix3f, vector3f.x, vector3f.y, vector3f.z).m_5752_();
                }
                ++n;
            }
        }
        aABB.m_109912_(LIGHT);
    }

    private record StampKey(UUID owner, int kind, int target) {
    }

    private static final class Activation {
        private CombatPacket packet;
        private long received;
        private long born;

        private Activation(CombatPacket combatPacket, long l) {
            this.packet = combatPacket;
            this.received = l;
            this.born = Math.min(l, combatPacket.stamp());
        }

        private boolean expired(long l) {
            return l - (CombatRules.persistent(this.packet.kind()) ? this.received : this.born) >= (long)this.packet.duration();
        }

        private int targetId() {
            return (this.packet.kind() == 6 || this.packet.kind() == 11) && !this.packet.guards().isEmpty() ? this.packet.guards().get(0).entityId() : this.packet.ownerId();
        }
    }

    private record Placement(Vec3 root, Quaternionf rotation, double horizontal, double vertical, double longitudinal) {
    }

    private record Surface(List<CombatSafety.Corner> corners, double alpha, double depth) {
    }
}

