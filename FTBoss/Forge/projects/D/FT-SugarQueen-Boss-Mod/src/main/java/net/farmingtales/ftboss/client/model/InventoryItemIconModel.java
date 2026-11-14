package net.farmingtales.ftboss.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.client.model.BakedModelWrapper;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

/** FT 物品栏独立图标；2026-10-26：仅分流 GUI 视角，保留手持动画、模型覆写与黑切原样。 */
@Mod.EventBusSubscriber(modid = "ftboss", bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class InventoryItemIconModel extends BakedModelWrapper<BakedModel> {
    static final String[] IDS = ("aged_wine_seal apple_material apple_stem barrel_crossbow barrel_crossbow_ready "
            + "benger_spawn_egg benger_whistle blooming_thorns blue_cheese butcher_fang_charm "
            + "butcher_material butcher_statue candy_cane capacitor_material charged_core_pendant "
            + "cheese_material cheese_moon_charm cheese_wheel_mace compost_crown compost_summon "
            + "dandan_end_rod_scythe dandan_scythe defect_claw defect_core diamond_bow "
            + "diamond_bow_pulling_0 diamond_bow_pulling_1 diamond_bow_pulling_2 diamond_crossbow dog_beating_stick "
            + "dora_lance dora_nova_launcher dora_nova_launcher_v1 dora_pulse_gun dora_smg_v1 "
            + "easter_egg_dandan easter_egg_duola easter_egg_gongdi easter_egg_ningmengmao easter_egg_qingyi "
            + "easter_egg_qizai easter_egg_wenxuanlan easter_egg_xueli easter_egg_youzi easter_egg_zheergen "
            + "easter_egg_zicai food_appraisal_monocle forbidden_apple frost_crown_charm frost_material "
            + "frost_queen_staff golden_apple_brooch golden_bow golden_bow_pulling_0 golden_bow_pulling_1 "
            + "golden_bow_pulling_2 golden_crossbow gourmet_blank_blade iron_bow iron_bow_pulling_0 "
            + "iron_bow_pulling_1 iron_bow_pulling_2 iron_crossbow lemoncat_skillet lemoncat_spatula "
            + "manor_loot_chest manor_stop_token maque_blessing_feather meat_heart music_disc_apple "
            + "music_disc_apple_p2 music_disc_benger music_disc_butcher music_disc_compost music_disc_compost_p2 "
            + "music_disc_defect music_disc_gingerbread music_disc_manor_battle music_disc_manor_cue music_disc_manor_dual "
            + "music_disc_manor_guignol music_disc_rat music_disc_saya music_disc_tavern music_disc_tide_fish "
            + "music_disc_tide_fish_p2 netherite_bow netherite_bow_pulling_0 netherite_bow_pulling_1 netherite_bow_pulling_2 "
            + "netherite_crossbow puffer_bat qingyi_confluence_trident qizai_prayer_staff rainbow_spore_charm "
            + "rat_bait shadow_sword shepherd_material shepherd_whistle_charm silver_key "
            + "spore_material statue statue_apple statue_benger statue_blank "
            + "statue_butcher statue_compost statue_defect statue_rat_swarm statue_saya "
            + "statue_sugar_queen statue_tide_fish statue_wine_barrel statue_youzi stone_bow "
            + "stone_bow_pulling_0 stone_bow_pulling_1 stone_bow_pulling_2 stone_crossbow sugar_queen_crystal "
            + "tea_heart_pendant tea_material tea_seed thorn_boots thorn_chestplate "
            + "thorn_crown thorn_leggings tide_banquet tide_material tide_pearl "
            + "tide_pearl_pendant tide_trident turkey_legs wenxuanlan_flywheel wine_bottle_mace "
            + "wine_charm wine_material wooden_bow wooden_bow_pulling_0 wooden_bow_pulling_1 "
            + "wooden_bow_pulling_2 wooden_crossbow xuanlan_calibrum_v2 xuanlan_crescendum_v2 xueli_rolling_pin "
            + "xueli_skillet").split(" ");
    private static final String[] CROSSBOWS = {"wooden", "stone", "iron", "golden", "diamond", "netherite", "barrel"};
    private final BakedModel icon;
    private final ItemOverrides overrides;

    InventoryItemIconModel(BakedModel original, BakedModel icon, Map<BakedModel, BakedModel> icons,
                           Map<String, BakedModel> chargedIcons) {
        super(original);
        this.icon = icon;
        this.overrides = new ItemOverrides() {
            @Override
            public BakedModel resolve(BakedModel model, ItemStack stack, @Nullable ClientLevel level,
                                      @Nullable LivingEntity entity, int seed) {
                BakedModel resolved = original.getOverrides().resolve(original, stack, level, entity, seed);
                if (resolved == null) return null;
                if (resolved instanceof InventoryItemIconModel) return resolved;
                BakedModel selected = icons.getOrDefault(resolved, icon);
                if (CrossbowItem.isCharged(stack)) {
                    ResourceLocation identity = ForgeRegistries.ITEMS.getKey(stack.getItem());
                    if (identity != null && identity.getNamespace().equals("ftboss")) {
                        selected = chargedIcons.getOrDefault(identity.getPath(), selected);
                    }
                }
                return resolved == original && selected == icon ? InventoryItemIconModel.this
                        : new InventoryItemIconModel(resolved, selected, icons, chargedIcons);
            }
        };
    }

    static ResourceLocation iconId(String identity) {
        return new ResourceLocation("ftboss", "item/inventory_redraw/" + identity);
    }

    @SubscribeEvent
    public static void registerIcons(ModelEvent.RegisterAdditional event) {
        for (String identity : IDS) event.register(iconId(identity));
        for (String material : CROSSBOWS) event.register(iconId(material + "_crossbow_charged"));
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void bake(ModelEvent.ModifyBakingResult event) {
        wrapModels(event.getModels());
    }

    static void wrapModels(Map<ResourceLocation, BakedModel> models) {
        Map<BakedModel, BakedModel> icons = new IdentityHashMap<>();
        Map<String, BakedModel> chargedIcons = new HashMap<>();
        Map<ResourceLocation, BakedModel> selected = new HashMap<>();
        for (String identity : IDS) {
            BakedModel icon = models.get(iconId(identity));
            if (icon == null) throw new IllegalStateException("Missing FT inventory icon: " + identity);
            for (ResourceLocation key : new ResourceLocation[] {new ModelResourceLocation("ftboss", identity, "inventory"),
                    new ResourceLocation("ftboss", "item/" + identity)}) {
                BakedModel original = models.get(key);
                if (original != null) {
                    icons.put(original, icon);
                    selected.put(key, original);
                }
            }
        }
        for (String material : CROSSBOWS) {
            String identity = material + "_crossbow";
            BakedModel icon = models.get(iconId(identity + "_charged"));
            if (icon == null) throw new IllegalStateException("Missing charged FT inventory icon: " + identity);
            chargedIcons.put(identity, icon);
        }
        selected.forEach((key, original) -> models.put(key,
                new InventoryItemIconModel(original, icons.get(original), icons, chargedIcons)));
    }

    @Override
    public ItemOverrides getOverrides() { return this.overrides; }

    @Override
    public BakedModel applyTransform(ItemDisplayContext context, PoseStack pose, boolean leftHand) {
        return context == ItemDisplayContext.GUI ? this.icon.applyTransform(context, pose, leftHand)
                : this.originalModel.applyTransform(context, pose, leftHand);
    }
}
