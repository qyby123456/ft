/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.model.geom.ModelPart
 *  net.minecraft.client.multiplayer.ClientLevel
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.server.packs.resources.PreparableReloadListener
 *  net.minecraft.server.packs.resources.ResourceManager
 *  net.minecraft.server.packs.resources.SimplePreparableReloadListener
 *  net.minecraft.util.profiling.ProfilerFiller
 *  net.minecraft.world.entity.HumanoidArm
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.Level
 *  net.minecraftforge.api.distmarker.Dist
 *  net.minecraftforge.client.event.RegisterClientReloadListenersEvent
 *  net.minecraftforge.event.level.LevelEvent$Unload
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber$Bus
 *  net.minecraftforge.registries.ForgeRegistries
 *  org.apache.logging.log4j.LogManager
 */
package net.farmingtales.ftboss.client.qizaiaction;

import java.io.BufferedReader;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import net.farmingtales.ftboss.client.model.EasterBossModel;
import net.farmingtales.ftboss.client.qizaiaction.QizaiActionClock;
import net.farmingtales.ftboss.client.qizaiaction.QizaiActionRig;
import net.farmingtales.ftboss.client.qizairun.QizaiRunClient;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.logging.log4j.LogManager;

public final class QizaiActionClient
extends SimplePreparableReloadListener<QizaiActionRig> {
    private static volatile QizaiActionRig loaded;
    private static final Map<UUID, QizaiActionClock> actors;
    private static ClientLevel scene;

    public static boolean attached(EasterBossEntity easterBossEntity) {
        ResourceLocation resourceLocation = ForgeRegistries.ITEMS.getKey((Object)easterBossEntity.m_21205_().m_41720_());
        return loaded != null && QizaiActionClock.eligible(easterBossEntity.getVariant(), easterBossEntity.m_6084_(), easterBossEntity.m_213877_(), resourceLocation == null ? "" : resourceLocation.toString());
    }

    protected QizaiActionRig m_5944_(ResourceManager resourceManager, ProfilerFiller profilerFiller) {
        QizaiActionRig qizaiActionRig;
        block8: {
            BufferedReader bufferedReader = resourceManager.m_215597_(new ResourceLocation("ftboss", "qizai_actions/caster_actions.json"));
            try {
                qizaiActionRig = QizaiActionRig.read(bufferedReader);
                if (bufferedReader == null) break block8;
            }
            catch (Throwable throwable) {
                try {
                    if (bufferedReader != null) {
                        try {
                            bufferedReader.close();
                        }
                        catch (Throwable throwable2) {
                            throwable.addSuppressed(throwable2);
                        }
                    }
                    throw throwable;
                }
                catch (Exception exception) {
                    LogManager.getLogger((String)"QizaiActions").error("Action data unavailable; preserving original humanoid pose", (Throwable)exception);
                    return null;
                }
            }
            bufferedReader.close();
        }
        return qizaiActionRig;
    }

    protected void m_5787_(QizaiActionRig qizaiActionRig, ResourceManager resourceManager, ProfilerFiller profilerFiller) {
        loaded = qizaiActionRig;
        actors.clear();
    }

    public static void apply(EasterBossModel easterBossModel, EasterBossEntity easterBossEntity, float f, float f2, float f3, float f4, float f5) {
        Object object;
        Object object2;
        boolean bl;
        QizaiRunClient.prepare(easterBossModel, easterBossEntity, f, f2, f3, f4, f5);
        QizaiActionRig qizaiActionRig = loaded;
        if (qizaiActionRig == null || easterBossEntity.getVariant() != 11 || !easterBossEntity.m_6084_() || easterBossEntity.m_213877_()) {
            QizaiRunClient.finish(easterBossModel, easterBossEntity);
            return;
        }
        ItemStack itemStack = easterBossEntity.m_21205_();
        ResourceLocation resourceLocation = ForgeRegistries.ITEMS.getKey((Object)itemStack.m_41720_());
        if (!QizaiActionClock.eligible(easterBossEntity.getVariant(), easterBossEntity.m_6084_(), easterBossEntity.m_213877_(), resourceLocation == null ? "" : resourceLocation.toString())) {
            actors.remove(easterBossEntity.m_20148_());
            QizaiRunClient.finish(easterBossModel, easterBossEntity);
            return;
        }
        Level level = easterBossEntity.m_9236_();
        if (!(level instanceof ClientLevel)) {
            QizaiRunClient.finish(easterBossModel, easterBossEntity);
            return;
        }
        ClientLevel clientLevel = (ClientLevel)level;
        if (scene != clientLevel) {
            actors.clear();
            scene = clientLevel;
        }
        double d = Math.max(0.0f, Math.min(1.0f, f3 - (float)easterBossEntity.f_19797_));
        double d2 = (double)clientLevel.m_46467_() + d;
        CompoundTag compoundTag = easterBossEntity.getSkillCue();
        int n = compoundTag.m_128451_("qizai");
        QizaiActionClock.Selection selection = QizaiActionClock.select(new QizaiActionClock.Cue(n, compoundTag.m_128454_("start"), compoundTag.m_128451_("duration"), compoundTag.m_128471_("released"), compoundTag.m_128451_("volley"), compoundTag.m_128454_("windupStart"), compoundTag.m_128451_("windupDuration"), compoundTag.m_128451_("elapsed"), compoundTag.m_128451_("round"), compoundTag.m_128451_("trackTicks"), compoundTag.m_128451_("lockTicks"), compoundTag.m_128451_("fireGap"), compoundTag.m_128451_("activeTicks"), compoundTag.m_128451_("warningTicks"), compoundTag.m_128451_("roundStart")), d2);
        QizaiActionClock qizaiActionClock2 = actors.get(easterBossEntity.m_20148_());
        boolean bl2 = bl = n >= 0 && n <= 10;
        if (selection == null && bl && !easterBossEntity.isCastingLike() && easterBossEntity.getCombatAnimationWeight((float)d) <= 0.01f) {
            object2 = easterBossEntity.getQizaiArsenalCue();
            if (object2.m_128425_("push", 10)) {
                object = object2.m_128469_("push");
                selection = QizaiActionClock.auxiliary(102, object.m_128454_("start"), object.m_128471_("released"), d2);
            }
            if (selection == null) {
                object = object2.m_128437_("salvos", 10);
                for (int i = Math.min(4, object.size()) - 1; i >= 0 && selection == null; --i) {
                    selection = QizaiActionClock.auxiliary(101, object.m_128728_(i).m_128454_("start"), false, d2);
                }
            }
        }
        if (!(selection != null || bl && (!easterBossEntity.isCastingLike() && !(easterBossEntity.getCombatAnimationWeight((float)d) > 0.01f) || qizaiActionClock2 != null && qizaiActionClock2.canRecover(d2)))) {
            actors.remove(easterBossEntity.m_20148_());
            QizaiRunClient.finish(easterBossModel, easterBossEntity);
            return;
        }
        if (selection == null && f2 > 0.025f) {
            actors.remove(easterBossEntity.m_20148_());
            QizaiRunClient.finish(easterBossModel, easterBossEntity);
            return;
        }
        actors.values().removeIf(qizaiActionClock -> qizaiActionClock.expired(d2));
        if (!actors.containsKey(easterBossEntity.m_20148_()) && actors.size() >= 128) {
            actors.remove(actors.keySet().iterator().next());
        }
        object2 = actors.computeIfAbsent(easterBossEntity.m_20148_(), uUID -> new QizaiActionClock());
        object = ((QizaiActionClock)object2).sample(qizaiActionRig, selection, d2);
        QizaiActionRig.Pose pose = QizaiActionRig.pose((double[])object, easterBossEntity.m_5737_() == HumanoidArm.LEFT, Math.toRadians(f4), Math.toRadians(f5));
        ModelPart[] modelPartArray = new ModelPart[]{easterBossModel.f_102808_, easterBossModel.f_102810_, easterBossModel.f_102811_, easterBossModel.f_102812_, easterBossModel.f_102813_, easterBossModel.f_102814_};
        for (int i = 0; i < modelPartArray.length; ++i) {
            if (i >= 4 && f2 > 0.025f) continue;
            ModelPart modelPart = modelPartArray[i];
            modelPart.f_104200_ = (float)pose.position()[i][0];
            modelPart.f_104201_ = (float)pose.position()[i][1];
            modelPart.f_104202_ = (float)pose.position()[i][2];
            modelPart.f_104203_ = (float)pose.rotation()[i][0];
            modelPart.f_104204_ = (float)pose.rotation()[i][1];
            modelPart.f_104205_ = (float)pose.rotation()[i][2];
        }
        easterBossModel.f_102809_.m_104315_(easterBossModel.f_102808_);
        easterBossModel.f_103378_.m_104315_(easterBossModel.f_102810_);
        easterBossModel.f_103375_.m_104315_(easterBossModel.f_102811_);
        easterBossModel.f_103374_.m_104315_(easterBossModel.f_102812_);
        easterBossModel.f_103377_.m_104315_(easterBossModel.f_102813_);
        easterBossModel.f_103376_.m_104315_(easterBossModel.f_102814_);
        QizaiRunClient.finish(easterBossModel, easterBossEntity);
    }

    static {
        actors = new LinkedHashMap<UUID, QizaiActionClock>();
    }

    @Mod.EventBusSubscriber(modid="ftboss", value={Dist.CLIENT})
    public static final class Events {
        private Events() {
        }

        @SubscribeEvent
        public static void unloaded(LevelEvent.Unload unload) {
            if (unload.getLevel() == scene) {
                actors.clear();
                scene = null;
            }
        }
    }

    @Mod.EventBusSubscriber(modid="ftboss", value={Dist.CLIENT}, bus=Mod.EventBusSubscriber.Bus.MOD)
    public static final class Setup {
        private Setup() {
        }

        @SubscribeEvent
        public static void reload(RegisterClientReloadListenersEvent registerClientReloadListenersEvent) {
            registerClientReloadListenersEvent.registerReloadListener((PreparableReloadListener)new QizaiActionClient());
        }
    }
}

