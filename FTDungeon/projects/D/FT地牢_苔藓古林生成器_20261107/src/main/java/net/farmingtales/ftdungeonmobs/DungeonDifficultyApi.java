package net.farmingtales.ftdungeonmobs;

import com.mojang.logging.LogUtils;
import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import org.slf4j.Logger;

public final class DungeonDifficultyApi {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String NBT_KEY = "FTDungeonDifficulty";
    private static final UUID HEALTH_ID = modifierId("health");
    private static final UUID ATTACK_ID = modifierId("attack");
    private static final UUID ARMOR_ID = modifierId("armor");
    private static final UUID MAGIC_ARMOR_ID = modifierId("magic_armor");
    private static final UUID SPELL_POWER_ID = modifierId("spell_power");

    private DungeonDifficultyApi() {}

    public static void setTier(DungeonSkeletonEntity entity, int tier) {
        DungeonDifficultyConfig.validateTier(tier);
        apply(entity, tier, false, entity.difficultyOverrides(), DungeonDifficultyConfig.settings());
    }

    public static void useAutomaticTier(DungeonSkeletonEntity entity) {
        DungeonDifficultyConfig.Settings settings = DungeonDifficultyConfig.settings();
        apply(entity, automaticTier(entity, settings), true, entity.difficultyOverrides(), settings);
    }

    public static void setOverrides(DungeonSkeletonEntity entity, DifficultyProfile overrides) {
        Objects.requireNonNull(overrides, "overrides");
        validateOverrides(overrides);
        DungeonDifficultyConfig.Settings settings = DungeonDifficultyConfig.settings();
        apply(entity, resolveTier(entity, settings), entity.automaticDifficulty(), overrides, settings);
    }

    public static void clearOverrides(DungeonSkeletonEntity entity) {
        setOverrides(entity, DifficultyProfile.IDENTITY);
    }

    public static DifficultyProfile getOverrides(DungeonSkeletonEntity entity) {
        requireServer(entity);
        return entity.difficultyOverrides();
    }

    public static int getTier(DungeonSkeletonEntity entity) {
        refreshIfNeeded(entity);
        return entity.storedDifficultyTier();
    }

    public static Snapshot inspect(DungeonSkeletonEntity entity) {
        refreshIfNeeded(entity);
        DifficultyProfile profile = DungeonDifficultyConfig.settings()
                .profile(entity.variant(), entity.storedDifficultyTier()).combine(entity.difficultyOverrides());
        return new Snapshot(entity.storedDifficultyTier(), entity.automaticDifficulty(), profile,
                entity.getHealth(), entity.getMaxHealth(), entity.getAttributeValue(Attributes.ATTACK_DAMAGE),
                entity.getAttributeValue(Attributes.ARMOR), entity.getAttributeValue(AttributeRegistry.SPELL_RESIST.get()),
                entity.getAttributeValue(AttributeRegistry.SPELL_POWER.get()));
    }

    public static void refresh(DungeonSkeletonEntity entity) {
        DungeonDifficultyConfig.Settings settings = DungeonDifficultyConfig.settings();
        apply(entity, resolveTier(entity, settings), entity.automaticDifficulty(), entity.difficultyOverrides(), settings);
    }

    public static int refreshAll(MinecraftServer server) {
        if (!server.isSameThread()) {
            throw new IllegalStateException("Difficulty changes must run on the server thread.");
        }
        int count = 0;
        for (ServerLevel level : server.getAllLevels()) {
            for (Entity entity : level.getAllEntities()) {
                if (entity instanceof DungeonSkeletonEntity skeleton) {
                    refresh(skeleton);
                    count++;
                }
            }
        }
        return count;
    }

    public static int reloadConfig(MinecraftServer server) {
        if (!server.isSameThread()) {
            throw new IllegalStateException("Difficulty config must be reloaded on the server thread.");
        }
        DungeonDifficultyConfig.reloadFromDisk();
        return refreshAll(server);
    }

    public static void refreshIfNeeded(DungeonSkeletonEntity entity) {
        requireServer(entity);
        DungeonDifficultyConfig.Settings settings = DungeonDifficultyConfig.settings();
        int tier = resolveTier(entity, settings);
        if (entity.difficultyRevision() != settings.revision() || entity.storedDifficultyTier() != tier
                || (entity.automaticDifficulty() && entity.appliedWorldDifficulty() != entity.level().getDifficulty())) {
            apply(entity, tier, entity.automaticDifficulty(), entity.difficultyOverrides(), settings);
        }
    }

    public static void onEntityJoin(EntityJoinLevelEvent event) {
        if (event.getEntity() instanceof DungeonSkeletonEntity skeleton && event.getLevel() instanceof ServerLevel level) {
            MinecraftServer server = level.getServer();
            if (server.isSameThread()) {
                refresh(skeleton);
            } else {
                server.execute(() -> {
                    if (!skeleton.isRemoved()) {
                        refresh(skeleton);
                    }
                });
            }
        }
    }

    static void writeState(DungeonSkeletonEntity entity, CompoundTag tag) {
        CompoundTag state = new CompoundTag();
        state.putInt("Tier", entity.storedDifficultyTier());
        state.putBoolean("Automatic", entity.automaticDifficulty());
        DifficultyProfile profile = entity.difficultyOverrides();
        CompoundTag overrides = new CompoundTag();
        overrides.putDouble("HealthMultiplier", profile.healthMultiplier());
        overrides.putDouble("AttackMultiplier", profile.attackMultiplier());
        overrides.putDouble("ArmorBonus", profile.armorBonus());
        overrides.putDouble("MagicArmorBonus", profile.magicArmorBonus());
        overrides.putDouble("SpellPowerMultiplier", profile.spellPowerMultiplier());
        state.put("Overrides", overrides);
        tag.put(NBT_KEY, state);
    }

    static void readState(DungeonSkeletonEntity entity, CompoundTag tag) {
        CompoundTag state = tag.getCompound(NBT_KEY);
        int tier = state.getInt("Tier");
        if (tier != 0) {
            tier = Mth.clamp(tier, DungeonDifficultyConfig.MIN_TIER, DungeonDifficultyConfig.MAX_TIER);
        }
        boolean automatic = !state.contains("Tier", Tag.TAG_ANY_NUMERIC) || tier == 0 || state.getBoolean("Automatic");
        CompoundTag values = state.getCompound("Overrides");
        DifficultyProfile overrides;
        try {
            overrides = new DifficultyProfile(readDouble(values, "HealthMultiplier", 1),
                    readDouble(values, "AttackMultiplier", 1), readDouble(values, "ArmorBonus", 0),
                    readDouble(values, "MagicArmorBonus", 0), readDouble(values, "SpellPowerMultiplier", 1));
            validateOverrides(overrides);
        } catch (IllegalArgumentException exception) {
            LOGGER.warn("Discarding invalid FT Dungeon Mob difficulty overrides for {}: {}", entity.getUUID(), exception.getMessage());
            overrides = DifficultyProfile.IDENTITY;
        }
        entity.storeDifficultyState(tier, automatic, overrides, -1, null);
    }

    private static int resolveTier(DungeonSkeletonEntity entity, DungeonDifficultyConfig.Settings settings) {
        return entity.automaticDifficulty() || entity.storedDifficultyTier() == 0
                ? automaticTier(entity, settings) : entity.storedDifficultyTier();
    }

    private static int automaticTier(DungeonSkeletonEntity entity, DungeonDifficultyConfig.Settings settings) {
        requireServer(entity);
        java.util.OptionalInt dungeonTier = DungeonGateTravel.tierForLevel((ServerLevel) entity.level());
        if (dungeonTier.isPresent()) {
            return dungeonTier.getAsInt();
        }
        return ArenaDifficultyApi.findTier((ServerLevel) entity.level(), entity.position())
                .orElseGet(() -> settings.automaticTier(entity.level().getDifficulty()));
    }

    private static void apply(DungeonSkeletonEntity entity, int tier, boolean automatic, DifficultyProfile overrides,
                              DungeonDifficultyConfig.Settings settings) {
        requireServer(entity);
        DifficultyProfile profile = settings.profile(entity.variant(), tier).combine(overrides);
        AttributeInstance health = attribute(entity, Attributes.MAX_HEALTH);
        AttributeInstance attack = attribute(entity, Attributes.ATTACK_DAMAGE);
        AttributeInstance armor = attribute(entity, Attributes.ARMOR);
        AttributeInstance magicArmor = attribute(entity, AttributeRegistry.SPELL_RESIST.get());
        AttributeInstance spellPower = attribute(entity, AttributeRegistry.SPELL_POWER.get());
        float previousHealth = entity.getHealth();
        double previousMaximum = entity.getMaxHealth();
        double healthFraction = previousMaximum > 0 ? Mth.clamp(previousHealth / previousMaximum, 0, 1) : 0;
        replaceModifier(health, HEALTH_ID, profile.healthMultiplier() - 1, AttributeModifier.Operation.MULTIPLY_TOTAL);
        replaceModifier(attack, ATTACK_ID, profile.attackMultiplier() - 1, AttributeModifier.Operation.MULTIPLY_TOTAL);
        replaceModifier(armor, ARMOR_ID, profile.armorBonus(), AttributeModifier.Operation.ADDITION);
        replaceModifier(magicArmor, MAGIC_ARMOR_ID, profile.magicArmorBonus(), AttributeModifier.Operation.ADDITION);
        replaceModifier(spellPower, SPELL_POWER_ID, profile.spellPowerMultiplier() - 1, AttributeModifier.Operation.MULTIPLY_TOTAL);
        if (Double.compare(previousMaximum, entity.getMaxHealth()) != 0) {
            entity.setHealth((float) (entity.getMaxHealth() * healthFraction));
        }
        entity.storeDifficultyState(tier, automatic, overrides, settings.revision(), entity.level().getDifficulty());
    }

    private static void replaceModifier(AttributeInstance attribute, UUID identifier, double amount, AttributeModifier.Operation operation) {
        attribute.removeModifier(identifier);
        if (amount != 0) {
            attribute.addPermanentModifier(new AttributeModifier(identifier, "ftdungeonmobs.difficulty", amount, operation));
        }
    }

    private static AttributeInstance attribute(DungeonSkeletonEntity entity, Attribute attribute) {
        return Objects.requireNonNull(entity.getAttribute(attribute), "Missing dungeon mob attribute: " + attribute.getDescriptionId());
    }

    private static void requireServer(DungeonSkeletonEntity entity) {
        if (!(entity.level() instanceof ServerLevel level) || !level.getServer().isSameThread()) {
            throw new IllegalStateException("Difficulty API must be called on the logical server thread.");
        }
    }

    private static double readDouble(CompoundTag values, String name, double fallback) {
        return values.contains(name, Tag.TAG_ANY_NUMERIC) ? values.getDouble(name) : fallback;
    }

    private static void validateOverrides(DifficultyProfile profile) {
        if (profile.healthMultiplier() < 0.05 || profile.healthMultiplier() > 100
                || profile.attackMultiplier() > 100 || profile.spellPowerMultiplier() > 100
                || profile.armorBonus() < -30 || profile.armorBonus() > 30
                || profile.magicArmorBonus() < -0.9 || profile.magicArmorBonus() > 99) {
            throw new IllegalArgumentException("Per-entity adjustments must use the same input ranges as the difficulty config.");
        }
    }

    private static UUID modifierId(String stat) {
        return UUID.nameUUIDFromBytes((FTDungeonMobs.ID + ":difficulty/" + stat).getBytes(StandardCharsets.UTF_8));
    }

    public record Snapshot(int tier, boolean automatic, DifficultyProfile profile, double health, double maxHealth,
                           double attackDamage, double physicalArmor, double magicResistance, double spellPower) {}
}
