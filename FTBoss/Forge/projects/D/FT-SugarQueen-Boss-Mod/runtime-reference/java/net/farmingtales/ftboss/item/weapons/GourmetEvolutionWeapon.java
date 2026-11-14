/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.ChatFormatting
 *  net.minecraft.core.particles.ParticleOptions
 *  net.minecraft.core.particles.ParticleTypes
 *  net.minecraft.network.chat.Component
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.sounds.SoundEvents
 *  net.minecraft.sounds.SoundSource
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.item.ItemEntity
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.TooltipFlag
 *  net.minecraft.world.level.Level
 *  net.minecraftforge.event.entity.player.EntityItemPickupEvent
 */
package net.farmingtales.ftboss.item.weapons;

import java.util.List;
import net.farmingtales.ftboss.item.FoodWeaponEvolution;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;

public interface GourmetEvolutionWeapon {
    public void triggerEvolution(ServerPlayer var1, ItemStack var2);

    public void triggerSocket(ServerPlayer var1, ItemStack var2);

    public void triggerActive(ServerPlayer var1, ItemStack var2);

    public static void animateEvolution(ServerPlayer holder, ItemStack stack) {
        Item item = stack.m_41720_();
        if (item instanceof GourmetEvolutionWeapon) {
            GourmetEvolutionWeapon weapon = (GourmetEvolutionWeapon)item;
            weapon.triggerEvolution(holder, stack);
        }
    }

    public static void animateSocket(ServerPlayer holder, ItemStack stack) {
        Item item = stack.m_41720_();
        if (item instanceof GourmetEvolutionWeapon) {
            GourmetEvolutionWeapon weapon = (GourmetEvolutionWeapon)item;
            weapon.triggerSocket(holder, stack);
        }
    }

    public static void animateActive(ServerPlayer holder, ItemStack stack) {
        Item item = stack.m_41720_();
        if (item instanceof GourmetEvolutionWeapon) {
            GourmetEvolutionWeapon weapon = (GourmetEvolutionWeapon)item;
            weapon.triggerActive(holder, stack);
        }
    }

    public static void inventoryParticles(ItemStack stack, Level level, Entity holder, boolean selected) {
        ServerLevel server;
        block3: {
            block2: {
                if (!selected || !(level instanceof ServerLevel)) break block2;
                server = (ServerLevel)level;
                if (holder.f_19797_ % 12 == 0) break block3;
            }
            return;
        }
        int evolution = FoodWeaponEvolution.evolutionLevel(stack);
        server.m_8767_((ParticleOptions)(evolution > 0 ? ParticleTypes.f_123810_ : ParticleTypes.f_123809_), holder.m_20185_(), holder.m_20186_() + 0.9, holder.m_20189_(), 1, 0.22 + (double)evolution * 0.015, 0.3, 0.22 + (double)evolution * 0.015, 0.01);
    }

    public static void droppedParticles(ItemStack stack, ItemEntity entity) {
        ServerLevel server;
        block3: {
            block2: {
                Level level = entity.m_9236_();
                if (!(level instanceof ServerLevel)) break block2;
                server = (ServerLevel)level;
                if (entity.m_32059_() % 6 == 0) break block3;
            }
            return;
        }
        int evolution = FoodWeaponEvolution.evolutionLevel(stack);
        server.m_8767_((ParticleOptions)(evolution > 0 ? ParticleTypes.f_123810_ : ParticleTypes.f_123809_), entity.m_20185_(), entity.m_20186_() + 0.28, entity.m_20189_(), 1 + evolution / 3, 0.18, 0.22, 0.18, 0.015);
    }

    public static void playPickupEffect(EntityItemPickupEvent event) {
        Level level;
        if (event.isCanceled() || !(event.getItem().m_32055_().m_41720_() instanceof GourmetEvolutionWeapon) || !((level = event.getEntity().m_9236_()) instanceof ServerLevel)) {
            return;
        }
        ServerLevel level2 = (ServerLevel)level;
        ItemEntity item = event.getItem();
        level2.m_8767_((ParticleOptions)ParticleTypes.f_123810_, item.m_20185_(), item.m_20186_() + 0.45, item.m_20189_(), 26, 0.45, 0.55, 0.45, 0.09);
        level2.m_8767_((ParticleOptions)ParticleTypes.f_123809_, item.m_20185_(), item.m_20186_() + 0.45, item.m_20189_(), 36, 0.55, 0.65, 0.55, 0.18);
        level2.m_5594_(null, item.m_20183_(), SoundEvents.f_144243_, SoundSource.PLAYERS, 0.85f, 0.8f + event.getEntity().m_217043_().m_188501_() * 0.35f);
    }

    public static void appendSharedTooltip(ItemStack stack, Level level, List<Component> lines, TooltipFlag flag) {
        lines.add((Component)Component.m_237115_((String)"tooltip.ftboss.gourmet.creative_only").m_130940_(ChatFormatting.DARK_PURPLE));
        lines.add((Component)Component.m_237115_((String)"tooltip.ftboss.gourmet.zero_base").m_130940_(ChatFormatting.GRAY));
        lines.add((Component)Component.m_237110_((String)"tooltip.ftboss.gourmet.capacity", (Object[])new Object[]{30, 8}).m_130940_(ChatFormatting.AQUA));
        lines.add((Component)Component.m_237115_((String)"tooltip.ftboss.gourmet.use").m_130940_(ChatFormatting.DARK_GRAY));
    }
}

