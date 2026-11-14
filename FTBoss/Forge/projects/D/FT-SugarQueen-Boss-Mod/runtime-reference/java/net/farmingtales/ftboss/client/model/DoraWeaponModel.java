/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.multiplayer.ClientLevel
 *  net.minecraft.client.renderer.block.model.ItemOverrides
 *  net.minecraft.client.resources.model.BakedModel
 *  net.minecraft.client.resources.model.ModelResourceLocation
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.item.ItemStack
 *  net.minecraftforge.api.distmarker.Dist
 *  net.minecraftforge.client.event.ModelEvent$ModifyBakingResult
 *  net.minecraftforge.client.event.ModelEvent$RegisterAdditional
 *  net.minecraftforge.client.model.BakedModelWrapper
 *  net.minecraftforge.eventbus.api.EventPriority
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber$Bus
 *  org.jetbrains.annotations.Nullable
 */
package net.farmingtales.ftboss.client.model;

import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.client.model.BakedModelWrapper;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.Nullable;

@Mod.EventBusSubscriber(modid="ftboss", bus=Mod.EventBusSubscriber.Bus.MOD, value={Dist.CLIENT})
public final class DoraWeaponModel
extends BakedModelWrapper<BakedModel> {
    private static final int DORA_VARIANT = 2;
    private static final ModelResourceLocation MACHINE_GUN = new ModelResourceLocation("bren", "machine_gun", "inventory");
    private static final ResourceLocation LAUNCHER = new ResourceLocation("ftboss", "item/dora_nova_launcher_v1");
    private final ItemOverrides overrides;

    private DoraWeaponModel(final BakedModel original, final BakedModel launcher) {
        super(original);
        this.overrides = new ItemOverrides(){

            public BakedModel m_173464_(BakedModel model, ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity entity, int seed) {
                EasterBossEntity boss;
                boolean manorAppearance;
                boolean bl = manorAppearance = stack.m_41782_() && "dora".equals(stack.m_41783_().m_128461_("FTBossManorWeapon"));
                if (manorAppearance || entity instanceof EasterBossEntity && (boss = (EasterBossEntity)entity).getVariant() == 2) {
                    return launcher;
                }
                return original.m_7343_().m_173464_(original, stack, level, entity, seed);
            }
        };
    }

    @SubscribeEvent
    public static void registerModels(ModelEvent.RegisterAdditional event) {
        event.register(LAUNCHER);
    }

    @SubscribeEvent(priority=EventPriority.LOWEST)
    public static void bake(ModelEvent.ModifyBakingResult event) {
        BakedModel original = (BakedModel)event.getModels().get(MACHINE_GUN);
        if (original == null) {
            return;
        }
        BakedModel launcher = (BakedModel)event.getModels().get(LAUNCHER);
        if (launcher == null) {
            throw new IllegalStateException("Missing Dora nova launcher model: " + LAUNCHER);
        }
        event.getModels().put(MACHINE_GUN, new DoraWeaponModel(original, launcher));
    }

    public ItemOverrides m_7343_() {
        return this.overrides;
    }
}

