package net.farmingtales.ftdungeonmobs;

import java.util.List;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.registries.RegistryObject;

public final class ImportedCreatures {
    public static final RegistryObject<EntityType<ImportedMonsterEntity>> GHOST_SCREAMER = monster("ghost_screamer", 0.9F, 2.9F);
    public static final RegistryObject<EntityType<ImportedMonsterEntity>> ORE_MONSTER = monster("ore_monster", 1.2F, 1.2F);
    public static final RegistryObject<EntityType<SkeletonMerchantEntity>> SKELETON_MERCHANT = FTDungeonMobs.ENTITIES.register("skeleton_merchant",
            () -> EntityType.Builder.<SkeletonMerchantEntity>of(SkeletonMerchantEntity::new, MobCategory.MISC)
                    .sized(0.7F, 2.25F).clientTrackingRange(10).build(FTDungeonMobs.ID + ":skeleton_merchant"));
    public static final RegistryObject<Item> GHOST_EGG = egg(GHOST_SCREAMER, 0xC8E9E5, 0x355B73);
    public static final RegistryObject<Item> ORE_EGG = egg(ORE_MONSTER, 0x666969, 0x60D9DF);
    public static final RegistryObject<Item> MERCHANT_EGG = egg(SKELETON_MERCHANT, 0xDAD1B8, 0x79563D);
    public static final List<RegistryObject<Item>> EGGS = List.of(GHOST_EGG, ORE_EGG, MERCHANT_EGG);

    public static void initialize() {
    }

    private static RegistryObject<EntityType<ImportedMonsterEntity>> monster(String name, float width, float height) {
        return FTDungeonMobs.ENTITIES.register(name, () -> EntityType.Builder.<ImportedMonsterEntity>of(ImportedMonsterEntity::new, MobCategory.MONSTER)
                .sized(width, height).clientTrackingRange(10).build(FTDungeonMobs.ID + ":" + name));
    }

    private static RegistryObject<Item> egg(RegistryObject<? extends EntityType<? extends net.minecraft.world.entity.Mob>> type, int primary, int secondary) {
        return FTDungeonMobs.ITEMS.register(type.getId().getPath() + "_spawn_egg", () -> new ForgeSpawnEggItem(type, primary, secondary, new Item.Properties()));
    }

    public static void attributes(EntityAttributeCreationEvent event) {
        event.put(GHOST_SCREAMER.get(), ImportedMonsterEntity.createAttributes(false).build());
        event.put(ORE_MONSTER.get(), ImportedMonsterEntity.createAttributes(true).build());
        event.put(SKELETON_MERCHANT.get(), Villager.createAttributes().build());
    }

    public static void replaceDungeonMerchant(LivingEvent.LivingTickEvent event) {
        if (!(event.getEntity().level() instanceof ServerLevel level) || !(event.getEntity() instanceof Villager villager)
                || villager instanceof SkeletonMerchantEntity
                || !(level.dimension().equals(DungeonGateTravel.DUNGEON)
                || net.farmingtales.ftdungeonmobs.generator.DungeonGenerators.all().stream().anyMatch(provider -> provider.ownsDimension(level)))) {
            return;
        }
        SkeletonMerchantEntity replacement = SKELETON_MERCHANT.get().create(level);
        if (replacement == null) {
            return;
        }
        CompoundTag saved = villager.saveWithoutId(new CompoundTag());
        replacement.load(saved);
        villager.discard();
        if (!level.addFreshEntity(replacement)) {
            villager.revive();
            level.addFreshEntity(villager);
        }
    }
}
