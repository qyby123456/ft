package net.farmingtales.ftboss.item;

import net.farmingtales.ftboss.util.ManorWeaponRules;
import net.farmingtales.ftboss.util.ManorWeaponBalanceRules;
import io.redspace.ironsspellbooks.api.events.SpellDamageEvent;
import net.farmingtales.ftboss.entity.QingyiConfluenceTridentEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.ItemAttributeModifierEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public final class ManorWeaponEnhancements {
    private static final Set<String> WEAPONS = ManorWeaponCatalog.ENTRIES.stream()
            .map(ManorWeaponCatalog.Entry::item).collect(Collectors.toUnmodifiableSet());
    private static final UUID BONUS_ID = UUID.fromString("b18c0443-d0ae-45f6-bca5-34b396a73d8c");
    private static final String HIT_TIME = "ftboss:manor_weapon_fx_tick";

    private ManorWeaponEnhancements() {}

    public static String id(ItemStack stack) {
        ResourceLocation identifier = ForgeRegistries.ITEMS.getKey(stack.getItem());
        return identifier == null ? "" : identifier.toString();
    }

    public static boolean isWeapon(ItemStack stack) {
        return !stack.isEmpty() && WEAPONS.contains(id(stack));
    }

    @SubscribeEvent
    public static void attributes(ItemAttributeModifierEvent event) {
        if (event.getSlotType() != EquipmentSlot.MAINHAND
                || !ManorWeaponBalanceRules.attributeTarget(isWeapon(event.getItemStack()), id(event.getItemStack()))) return;
        var modifiers = new ArrayList<>(event.getModifiers().get(Attributes.ATTACK_DAMAGE));
        double original = modifiers.stream().filter(modifier -> modifier.getOperation() == AttributeModifier.Operation.ADDITION)
                .mapToDouble(AttributeModifier::getAmount).sum();
        var base = modifiers.stream().filter(modifier -> modifier.getOperation() == AttributeModifier.Operation.ADDITION).findFirst();
        if (base.isPresent()) {
            AttributeModifier modifier = base.get();
            event.removeModifier(Attributes.ATTACK_DAMAGE, modifier);
            event.addModifier(Attributes.ATTACK_DAMAGE, new AttributeModifier(modifier.getId(), modifier.getName(),
                    modifier.getAmount() + ManorWeaponBalanceRules.attackBonus(original, id(event.getItemStack())), AttributeModifier.Operation.ADDITION));
        } else {
            event.addModifier(Attributes.ATTACK_DAMAGE, new AttributeModifier(BONUS_ID, "ftboss.manor_weapon",
                    ManorWeaponBalanceRules.emptyHandBonus(id(event.getItemStack())), AttributeModifier.Operation.ADDITION));
        }
    }

    private static String projectileWeapon(Entity direct) {
        if (!(direct instanceof Projectile)) return "";
        ResourceLocation type = ForgeRegistries.ENTITY_TYPES.getKey(direct.getType());
        if (type == null || !type.getNamespace().equals("tridents_n_stuff")) return "";
        String item = "tridents_n_stuff:" + type.getPath().replaceFirst("_entity$", "");
        return WEAPONS.contains(item) ? item : "";
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void tridentDamage(LivingHurtEvent event) {
        if (!(event.getSource().getEntity() instanceof ServerPlayer) || event.getAmount() <= 0) return;
        if (event.getSource().getDirectEntity() instanceof QingyiConfluenceTridentEntity) {
            event.setAmount(event.getAmount() * ManorWeaponBalanceRules.DAMAGE_SCALE);
        } else if (!projectileWeapon(event.getSource().getDirectEntity()).isEmpty()) {
            event.setAmount(event.getAmount() * ManorWeaponRules.TRIDENT_THROW_MULTIPLIER * ManorWeaponBalanceRules.DAMAGE_SCALE);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void staffSpellDamage(SpellDamageEvent event) {
        if (event.getAmount() <= 0 || !(event.getSpellDamageSource().getEntity() instanceof ServerPlayer player)) return;
        if (id(player.getMainHandItem()).equals("ftboss:qizai_prayer_staff")
                || id(player.getOffhandItem()).equals("ftboss:qizai_prayer_staff")) {
            event.setAmount(event.getAmount() * ManorWeaponBalanceRules.DAMAGE_SCALE);
        }
    }

    @SubscribeEvent
    public static void heldFx(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.player instanceof ServerPlayer player)
                || !player.isAlive() || player.isSpectator() || player.isInvisible() || player.tickCount % 8 != 0) return;
        emitHeld(player, player.getMainHandItem(), 1);
        emitHeld(player, player.getOffhandItem(), -1);
    }

    private static void emitHeld(ServerPlayer player, ItemStack weapon, int side) {
        if (!isWeapon(weapon)) return;
        Vec3 forward = Vec3.directionFromRotation(0, player.getYRot());
        Vec3 right = new Vec3(-forward.z, 0, forward.x).scale(side * 0.38);
        Vec3 point = player.position().add(forward.scale(0.65)).add(right).add(0, 0.95, 0);
        double phase = player.tickCount * 0.18;
        player.serverLevel().sendParticles(dust(id(weapon), weapon), point.x + Math.cos(phase) * 0.12,
                point.y + Math.sin(phase) * 0.16, point.z, 2, 0.04, 0.05, 0.04, 0);
        if (player.tickCount % 16 == 0) player.serverLevel().sendParticles(ParticleTypes.END_ROD,
                point.x, point.y, point.z, 1, 0.04, 0.04, 0.04, 0.005);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void impactFx(LivingDamageEvent event) {
        if (event.getAmount() <= 0 || !(event.getSource().getEntity() instanceof ServerPlayer player)) return;
        ItemStack weapon = player.getMainHandItem();
        String projectile = projectileWeapon(event.getSource().getDirectEntity());
        if (projectile.isEmpty() && !isWeapon(weapon)) return;
        var target = event.getEntity();
        long now = player.level().getGameTime();
        var data = target.getPersistentData();
        if (data.contains(HIT_TIME) && now - data.getLong(HIT_TIME) < 4) return;
        data.putLong(HIT_TIME, now);
        String identifier = projectile.isEmpty() ? id(weapon) : projectile;
        player.serverLevel().sendParticles(dust(identifier, weapon), target.getX(), target.getY() + target.getBbHeight() * 0.55,
                target.getZ(), 12, target.getBbWidth() * 0.4, 0.28, target.getBbWidth() * 0.4, 0.02);
        player.serverLevel().sendParticles(accent(identifier), target.getX(), target.getY() + 0.7, target.getZ(),
                4, 0.15, 0.2, 0.15, 0.08);
    }

    private static SimpleParticleType accent(String identifier) {
        return switch (identifier) {
            case "ftboss:black_cleaver" -> ParticleTypes.CRIMSON_SPORE;
            case "ftboss:shadow_sword", "tridents_n_stuff:the_desecrator" -> ParticleTypes.REVERSE_PORTAL;
            case "ftboss:dora_nova_launcher", "ftboss:dora_pulse_gun", "ftboss:dora_lance", "tridents_n_stuff:stormseeker" -> ParticleTypes.ELECTRIC_SPARK;
            case "ftboss:lemoncat_skillet", "ftboss:lemoncat_spatula", "ftboss:maque_blessing_feather" -> ParticleTypes.HAPPY_VILLAGER;
            case "ftboss:puffer_bat", "tridents_n_stuff:echo_of_the_deep" -> ParticleTypes.BUBBLE_POP;
            case "ftboss:xueli_skillet", "ftboss:xueli_rolling_pin" -> ParticleTypes.SMALL_FLAME;
            default -> ParticleTypes.END_ROD;
        };
    }

    private static DustParticleOptions dust(String identifier, ItemStack weapon) {
        int rgb = switch (identifier) {
            case "ftboss:black_cleaver" -> 0xCF173E;
            case "ftboss:shadow_sword", "tridents_n_stuff:the_desecrator" -> 0x9E55EF;
            case "ftboss:dandan_scythe" -> 0xF1DDAC;
            case "ftboss:statue_youzi", "ftboss:maque_blessing_feather" -> 0xFFD76A;
            case "ftboss:dora_nova_launcher", "ftboss:dora_pulse_gun", "ftboss:dora_lance" -> 0xFF81CC;
            case "ftboss:lemoncat_skillet", "ftboss:lemoncat_spatula" -> 0xD5F36C;
            case "ftboss:wenxuanlan_flywheel" -> weapon.hasTag() && weapon.getTag().getBoolean("XuanlanWhite") ? 0xE2F4FF : 0x63F4BB;
            case "ftboss:puffer_bat" -> 0xA5E65B;
            case "ftboss:xueli_skillet", "ftboss:xueli_rolling_pin" -> 0xFFAF64;
            case "tridents_n_stuff:stormseeker" -> 0x8DE3FF;
            case "tridents_n_stuff:echo_of_the_deep" -> 0x42DAD0;
            default -> 0xCBD0FF;
        };
        return new DustParticleOptions(new Vector3f(((rgb >> 16) & 255) / 255F,
                ((rgb >> 8) & 255) / 255F, (rgb & 255) / 255F), 1.0F);
    }

    @SubscribeEvent
    public static void tooltip(ItemTooltipEvent event) {
        if (isWeapon(event.getItemStack())) event.getToolTip().add(Component.translatable("tooltip.ftboss.manor_weapon.enhanced")
                .withStyle(ChatFormatting.GOLD));
    }
}
