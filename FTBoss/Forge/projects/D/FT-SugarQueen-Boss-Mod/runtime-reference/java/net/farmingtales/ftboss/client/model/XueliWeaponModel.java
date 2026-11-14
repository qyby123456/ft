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
public final class XueliWeaponModel
extends BakedModelWrapper<BakedModel> {
    private static final int MODEL_DATA = 160417;
    private static final ResourceLocation SKILLET = new ResourceLocation("ftboss", "item/xueli/skillet");
    private static final ResourceLocation ROLLING_PIN = new ResourceLocation("ftboss", "item/xueli/rolling_pin");
    private final ItemOverrides overrides;

    private XueliWeaponModel(final BakedModel original, final BakedModel weapon) {
        super(original);
        this.overrides = new ItemOverrides(){

            public BakedModel m_173464_(BakedModel model, ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity entity, int seed) {
                boolean markedForPreview;
                EasterBossEntity boss;
                boolean heldByXueli = entity instanceof EasterBossEntity && (boss = (EasterBossEntity)entity).getVariant() == 10;
                boolean bl = markedForPreview = stack.m_41782_() && stack.m_41783_().m_128451_("CustomModelData") == 160417;
                if (heldByXueli || markedForPreview) {
                    return weapon;
                }
                return original.m_7343_().m_173464_(original, stack, level, entity, seed);
            }
        };
    }

    @SubscribeEvent
    public static void registerModels(ModelEvent.RegisterAdditional event) {
        event.register(SKILLET);
        event.register(ROLLING_PIN);
    }

    @SubscribeEvent(priority=EventPriority.LOWEST)
    public static void bake(ModelEvent.ModifyBakingResult event) {
        XueliWeaponModel.wrap(event, new ModelResourceLocation("farmersdelight", "skillet", "inventory"), SKILLET);
        XueliWeaponModel.wrap(event, new ModelResourceLocation("bakery", "rolling_pin", "inventory"), ROLLING_PIN);
    }

    private static void wrap(ModelEvent.ModifyBakingResult event, ModelResourceLocation itemId, ResourceLocation weaponId) {
        BakedModel original = (BakedModel)event.getModels().get(itemId);
        if (original == null) {
            return;
        }
        BakedModel weapon = (BakedModel)event.getModels().get(weaponId);
        if (weapon == null) {
            throw new IllegalStateException("Missing Xueli weapon model: " + weaponId);
        }
        event.getModels().put(itemId, new XueliWeaponModel(original, weapon));
    }

    public ItemOverrides m_7343_() {
        return this.overrides;
    }
}

