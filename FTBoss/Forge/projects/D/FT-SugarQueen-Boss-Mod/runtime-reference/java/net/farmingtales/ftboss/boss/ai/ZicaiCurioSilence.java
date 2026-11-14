/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.nbt.ListTag
 *  net.minecraft.nbt.Tag
 *  net.minecraft.network.chat.Component
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.util.RandomSource
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.entity.projectile.Projectile
 *  net.minecraft.world.item.ItemStack
 *  net.minecraftforge.event.TickEvent$Phase
 *  net.minecraftforge.event.TickEvent$PlayerTickEvent
 *  net.minecraftforge.event.entity.living.LivingDamageEvent
 *  net.minecraftforge.event.entity.living.LivingDeathEvent
 *  net.minecraftforge.event.entity.player.PlayerEvent$PlayerLoggedInEvent
 *  net.minecraftforge.event.entity.player.PlayerEvent$PlayerLoggedOutEvent
 *  net.minecraftforge.event.entity.player.PlayerEvent$PlayerRespawnEvent
 *  net.minecraftforge.event.server.ServerStoppedEvent
 *  net.minecraftforge.eventbus.api.Event$Result
 *  net.minecraftforge.eventbus.api.EventPriority
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber$Bus
 *  top.theillusivec4.curios.api.CuriosApi
 *  top.theillusivec4.curios.api.event.CurioEquipEvent
 *  top.theillusivec4.curios.api.type.capability.ICuriosItemHandler
 *  top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler
 *  top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler
 */
package net.farmingtales.ftboss.boss.ai;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.farmingtales.ftboss.boss.ManorMercy;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.util.ZicaiCurioSilenceRules;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.event.CurioEquipEvent;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;
import top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler;

@Mod.EventBusSubscriber(modid="ftboss", bus=Mod.EventBusSubscriber.Bus.FORGE)
public final class ZicaiCurioSilence {
    private static final String PENDING = "ftboss:ZicaiCurioSilence";
    private static final Map<UUID, Map<UUID, Long>> LAST_HITS = new HashMap<UUID, Map<UUID, Long>>();

    private ZicaiCurioSilence() {
    }

    private static long clock(ServerPlayer serverPlayer) {
        return serverPlayer.m_20194_().m_129783_().m_46467_();
    }

    private static ListTag pending(ServerPlayer serverPlayer) {
        return serverPlayer.getPersistentData().m_128469_("PlayerPersisted").m_128437_(PENDING, 10);
    }

    private static void save(ServerPlayer serverPlayer, ListTag listTag) {
        CompoundTag compoundTag = serverPlayer.getPersistentData();
        CompoundTag compoundTag2 = compoundTag.m_128469_("PlayerPersisted");
        if (listTag.isEmpty()) {
            compoundTag2.m_128473_(PENDING);
        } else {
            compoundTag2.m_128365_(PENDING, (Tag)listTag);
        }
        if (!compoundTag2.m_128456_()) {
            compoundTag.m_128365_("PlayerPersisted", (Tag)compoundTag2);
        } else {
            compoundTag.m_128473_("PlayerPersisted");
        }
    }

    private static ICuriosItemHandler inventory(ServerPlayer serverPlayer) {
        return CuriosApi.getCuriosInventory((LivingEntity)serverPlayer).resolve().orElse(null);
    }

    private static IDynamicStackHandler slot(ICuriosItemHandler iCuriosItemHandler, String string, int n) {
        if (iCuriosItemHandler == null) {
            return null;
        }
        ICurioStacksHandler iCurioStacksHandler = (ICurioStacksHandler)iCuriosItemHandler.getCurios().get(string);
        return iCurioStacksHandler != null && n >= 0 && n < iCurioStacksHandler.getStacks().getSlots() ? iCurioStacksHandler.getStacks() : null;
    }

    private static boolean same(CompoundTag compoundTag, String string, int n) {
        return compoundTag.m_128461_("Slot").equals(string) && compoundTag.m_128451_("Index") == n;
    }

    private static EasterBossEntity owner(LivingDamageEvent livingDamageEvent) {
        Object object;
        Entity entity = livingDamageEvent.getSource().m_7639_();
        if (entity == null) {
            entity = livingDamageEvent.getSource().m_7640_();
        }
        if (entity == null) {
            entity = ManorMercy.damageOwner();
        }
        int n = 0;
        while (entity instanceof Projectile) {
            object = (Projectile)entity;
            if (n >= 8) break;
            Entity entity2 = object.m_19749_();
            if (entity2 == entity) {
                return null;
            }
            entity = entity2;
            ++n;
        }
        return entity instanceof EasterBossEntity && ZicaiCurioSilenceRules.eligible(((EasterBossEntity)(object = (EasterBossEntity)entity)).getVariant(), ((EasterBossEntity)object).isMini(), ((EasterBossEntity)object).isGiant()) ? object : null;
    }

    @SubscribeEvent(priority=EventPriority.LOWEST)
    public static void onDamage(LivingDamageEvent livingDamageEvent) {
        ServerPlayer serverPlayer;
        Object object;
        if (livingDamageEvent.isCanceled() || livingDamageEvent.getAmount() <= 0.0f || !((object = livingDamageEvent.getEntity()) instanceof ServerPlayer) || (serverPlayer = (ServerPlayer)object).m_7500_() || serverPlayer.m_5833_()) {
            return;
        }
        object = ZicaiCurioSilence.owner(livingDamageEvent);
        if (object == null) {
            return;
        }
        long l = ZicaiCurioSilence.clock(serverPlayer);
        Map map = LAST_HITS.computeIfAbsent(serverPlayer.m_20148_(), uUID -> new HashMap());
        Long l2 = map.put(object.m_20148_(), l);
        if (l2 != null && l2 == l) {
            return;
        }
        ZicaiCurioSilence.silence(serverPlayer, l);
    }

    private static void silence(ServerPlayer serverPlayer, long l) {
        int n;
        IDynamicStackHandler iDynamicStackHandler;
        ICuriosItemHandler iCuriosItemHandler = ZicaiCurioSilence.inventory(serverPlayer);
        if (iCuriosItemHandler == null) {
            return;
        }
        ListTag listTag = ZicaiCurioSilence.pending(serverPlayer);
        ArrayList<Choice> arrayList = new ArrayList<Choice>();
        for (Map.Entry object2 : iCuriosItemHandler.getCurios().entrySet()) {
            iDynamicStackHandler = ((ICurioStacksHandler)object2.getValue()).getStacks();
            for (int i = 0; i < iDynamicStackHandler.getSlots(); ++i) {
                ItemStack itemStack = iDynamicStackHandler.getStackInSlot(i);
                if (itemStack.m_41619_()) continue;
                arrayList.add(new Choice((String)object2.getKey(), i, itemStack, null));
            }
        }
        for (n = 0; n < listTag.size(); ++n) {
            ItemStack itemStack;
            CompoundTag compoundTag = listTag.m_128728_(n);
            iDynamicStackHandler = ZicaiCurioSilence.slot(iCuriosItemHandler, compoundTag.m_128461_("Slot"), compoundTag.m_128451_("Index"));
            if (iDynamicStackHandler == null || !iDynamicStackHandler.getStackInSlot(compoundTag.m_128451_("Index")).m_41619_() || !ZicaiCurioSilenceRules.active(l, compoundTag.m_128454_("Until")) || (itemStack = ItemStack.m_41712_((CompoundTag)compoundTag.m_128469_("Stack"))).m_41619_()) continue;
            arrayList.add(new Choice(compoundTag.m_128461_("Slot"), compoundTag.m_128451_("Index"), itemStack, compoundTag));
        }
        n = ZicaiCurioSilenceRules.choose(arrayList.size(), arg_0 -> ((RandomSource)serverPlayer.m_217043_()).m_188503_(arg_0));
        if (n < 0) {
            return;
        }
        Choice choice = (Choice)arrayList.get(n);
        iDynamicStackHandler = choice.pending();
        if (iDynamicStackHandler == null) {
            iDynamicStackHandler = new CompoundTag();
            iDynamicStackHandler.m_128359_("Slot", choice.slot());
            iDynamicStackHandler.m_128405_("Index", choice.index());
            iDynamicStackHandler.m_128365_("Stack", (Tag)choice.stack().m_41739_(new CompoundTag()));
            listTag.add((Object)iDynamicStackHandler);
        }
        iDynamicStackHandler.m_128356_("Until", ZicaiCurioSilenceRules.expires(l));
        ZicaiCurioSilence.save(serverPlayer, listTag);
        if (choice.pending() == null) {
            ((ICurioStacksHandler)iCuriosItemHandler.getCurios().get(choice.slot())).getStacks().setStackInSlot(choice.index(), ItemStack.f_41583_);
        }
        serverPlayer.m_5661_((Component)Component.m_237113_((String)"\u00a75\u7d2b\u83dc \u00a77\u4f7f ").m_7220_(choice.stack().m_41786_()).m_130946_("\u00a77 \u6c89\u9ed8 5 \u79d2"), true);
    }

    private static void restore(ServerPlayer serverPlayer, boolean bl) {
        ListTag listTag = ZicaiCurioSilence.pending(serverPlayer);
        if (listTag.isEmpty()) {
            return;
        }
        long l = ZicaiCurioSilence.clock(serverPlayer);
        ICuriosItemHandler iCuriosItemHandler = ZicaiCurioSilence.inventory(serverPlayer);
        for (int i = listTag.size() - 1; i >= 0; --i) {
            CompoundTag compoundTag = listTag.m_128728_(i);
            if (!bl && ZicaiCurioSilenceRules.active(l, compoundTag.m_128454_("Until"))) continue;
            ItemStack itemStack = ItemStack.m_41712_((CompoundTag)compoundTag.m_128469_("Stack"));
            if (itemStack.m_41619_()) {
                listTag.remove(i);
                continue;
            }
            IDynamicStackHandler iDynamicStackHandler = ZicaiCurioSilence.slot(iCuriosItemHandler, compoundTag.m_128461_("Slot"), compoundTag.m_128451_("Index"));
            if (iDynamicStackHandler != null && iDynamicStackHandler.getStackInSlot(compoundTag.m_128451_("Index")).m_41619_()) {
                iDynamicStackHandler.setStackInSlot(compoundTag.m_128451_("Index"), itemStack);
                listTag.remove(i);
                continue;
            }
            serverPlayer.m_150109_().m_36054_(itemStack);
            if (itemStack.m_41619_()) {
                listTag.remove(i);
                continue;
            }
            compoundTag.m_128365_("Stack", (Tag)itemStack.m_41739_(new CompoundTag()));
        }
        ZicaiCurioSilence.save(serverPlayer, listTag);
    }

    @SubscribeEvent
    public static void onTick(TickEvent.PlayerTickEvent playerTickEvent) {
        Object object;
        if (playerTickEvent.phase != TickEvent.Phase.END || !((object = playerTickEvent.player) instanceof ServerPlayer)) {
            return;
        }
        ServerPlayer serverPlayer = (ServerPlayer)object;
        ZicaiCurioSilence.restore(serverPlayer, false);
        object = LAST_HITS.get(serverPlayer.m_20148_());
        if (object != null) {
            long l = ZicaiCurioSilence.clock(serverPlayer);
            object.values().removeIf(l2 -> l2 != l);
            if (object.isEmpty()) {
                LAST_HITS.remove(serverPlayer.m_20148_());
            }
        }
    }

    @SubscribeEvent(priority=EventPriority.LOWEST)
    public static void onEquip(CurioEquipEvent curioEquipEvent) {
        ServerPlayer serverPlayer;
        LivingEntity livingEntity;
        block4: {
            block3: {
                livingEntity = curioEquipEvent.getEntity();
                if (!(livingEntity instanceof ServerPlayer)) break block3;
                serverPlayer = (ServerPlayer)livingEntity;
                if (!curioEquipEvent.getSlotContext().cosmetic()) break block4;
            }
            return;
        }
        livingEntity = curioEquipEvent.getSlotContext();
        ListTag listTag = ZicaiCurioSilence.pending(serverPlayer);
        long l = ZicaiCurioSilence.clock(serverPlayer);
        for (int i = 0; i < listTag.size(); ++i) {
            CompoundTag compoundTag = listTag.m_128728_(i);
            if (!ZicaiCurioSilence.same(compoundTag, livingEntity.identifier(), livingEntity.index()) || !ZicaiCurioSilenceRules.active(l, compoundTag.m_128454_("Until"))) continue;
            curioEquipEvent.setResult(Event.Result.DENY);
            return;
        }
    }

    @SubscribeEvent(priority=EventPriority.HIGHEST)
    public static void onDeath(LivingDeathEvent livingDeathEvent) {
        LivingEntity livingEntity = livingDeathEvent.getEntity();
        if (livingEntity instanceof ServerPlayer) {
            ServerPlayer serverPlayer = (ServerPlayer)livingEntity;
            ZicaiCurioSilence.restore(serverPlayer, true);
        }
    }

    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent playerLoggedInEvent) {
        Player player = playerLoggedInEvent.getEntity();
        if (player instanceof ServerPlayer) {
            ServerPlayer serverPlayer = (ServerPlayer)player;
            ZicaiCurioSilence.restore(serverPlayer, true);
        }
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent playerLoggedOutEvent) {
        Player player = playerLoggedOutEvent.getEntity();
        if (player instanceof ServerPlayer) {
            ServerPlayer serverPlayer = (ServerPlayer)player;
            ZicaiCurioSilence.restore(serverPlayer, true);
            LAST_HITS.remove(serverPlayer.m_20148_());
        }
    }

    @SubscribeEvent
    public static void onRespawn(PlayerEvent.PlayerRespawnEvent playerRespawnEvent) {
        Player player = playerRespawnEvent.getEntity();
        if (player instanceof ServerPlayer) {
            ServerPlayer serverPlayer = (ServerPlayer)player;
            ZicaiCurioSilence.restore(serverPlayer, true);
        }
    }

    @SubscribeEvent
    public static void onStopped(ServerStoppedEvent serverStoppedEvent) {
        LAST_HITS.clear();
    }

    private record Choice(String slot, int index, ItemStack stack, CompoundTag pending) {
    }
}

