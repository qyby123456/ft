/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.nbt.Tag
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.ItemStack
 *  net.minecraftforge.event.TickEvent$Phase
 *  net.minecraftforge.event.TickEvent$PlayerTickEvent
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber
 *  net.minecraftforge.registries.ForgeRegistries
 */
package net.farmingtales.ftboss.item;

import net.farmingtales.ftboss.util.NonManorComponentMigrationRules;
import net.farmingtales.ftboss.util.foundations.FourWeaponFoundationProfiles;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

@Mod.EventBusSubscriber(modid="ftboss")
public final class NonManorWeaponComponentMigration {
    private static final String ROOT = "FTBossFourWeaponFoundation";

    private NonManorWeaponComponentMigration() {
    }

    @SubscribeEvent
    public static void playerTick(TickEvent.PlayerTickEvent playerTickEvent) {
        Player player;
        if (playerTickEvent.phase != TickEvent.Phase.END || !((player = playerTickEvent.player) instanceof ServerPlayer)) {
            return;
        }
        ServerPlayer serverPlayer = (ServerPlayer)player;
        boolean bl = false;
        for (int i = 0; i < serverPlayer.m_150109_().m_6643_(); ++i) {
            CompoundTag compoundTag;
            ResourceLocation resourceLocation;
            ItemStack itemStack = serverPlayer.m_150109_().m_8020_(i);
            CompoundTag compoundTag2 = itemStack.m_41737_(ROOT);
            if (itemStack.m_41619_() || itemStack.m_41613_() != 1 || compoundTag2 == null || (resourceLocation = ForgeRegistries.ITEMS.getKey((Object)itemStack.m_41720_())) == null || !resourceLocation.toString().equals("ftboss:frost_queen_staff") && !resourceLocation.toString().equals("ftboss:barrel_crossbow") || (compoundTag = NonManorComponentMigrationRules.reconcile(compoundTag2, FourWeaponFoundationProfiles.examples().get(resourceLocation.toString()))) == null) continue;
            itemStack.m_41784_().m_128365_(ROOT, (Tag)compoundTag);
            bl = true;
        }
        if (bl) {
            serverPlayer.m_150109_().m_6596_();
            serverPlayer.f_36095_.m_38946_();
            if (serverPlayer.f_36096_ != serverPlayer.f_36095_) {
                serverPlayer.f_36096_.m_38946_();
            }
        }
    }
}

