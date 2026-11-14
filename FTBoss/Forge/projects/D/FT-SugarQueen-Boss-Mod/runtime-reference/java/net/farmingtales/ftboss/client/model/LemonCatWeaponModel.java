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
public final class LemonCatWeaponModel
extends BakedModelWrapper<BakedModel> {
    private static final ResourceLocation SKILLET = new ResourceLocation("ftboss", "item/lemoncat/skillet");
    private static final ResourceLocation SHOVEL = new ResourceLocation("ftboss", "item/lemoncat/kitchen_shovel");
    private final ItemOverrides overrides;

    private LemonCatWeaponModel(final BakedModel original, final BakedModel weapon) {
        super(original);
        this.overrides = new ItemOverrides(){

            public BakedModel m_173464_(BakedModel model, ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity entity, int seed) {
                if (stack.m_41782_() && stack.m_41783_().m_128451_("CustomModelData") == 160416) {
                    return weapon;
                }
                return original.m_7343_().m_173464_(original, stack, level, entity, seed);
            }
        };
    }

    @SubscribeEvent
    public static void registerModels(ModelEvent.RegisterAdditional event) {
        event.register(SKILLET);
        event.register(SHOVEL);
    }

    @SubscribeEvent(priority=EventPriority.LOWEST)
    public static void bake(ModelEvent.ModifyBakingResult event) {
        LemonCatWeaponModel.wrap(event, new ModelResourceLocation("farmersdelight", "skillet", "inventory"), SKILLET);
        LemonCatWeaponModel.wrap(event, new ModelResourceLocation("kaleidoscope_cookery", "kitchen_shovel", "inventory"), SHOVEL);
    }

    private static void wrap(ModelEvent.ModifyBakingResult event, ModelResourceLocation itemId, ResourceLocation weaponId) {
        BakedModel original = (BakedModel)event.getModels().get(itemId);
        if (original == null) {
            return;
        }
        BakedModel weapon = (BakedModel)event.getModels().get(weaponId);
        if (weapon == null) {
            throw new IllegalStateException("Missing Lemon Cat weapon model: " + weaponId);
        }
        event.getModels().put(itemId, new LemonCatWeaponModel(original, weapon));
    }

    public ItemOverrides m_7343_() {
        return this.overrides;
    }
}

