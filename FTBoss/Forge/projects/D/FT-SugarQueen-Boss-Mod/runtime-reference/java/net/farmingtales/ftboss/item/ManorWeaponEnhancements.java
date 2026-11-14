/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.redspace.ironsspellbooks.api.events.SpellDamageEvent
 *  net.minecraft.ChatFormatting
 *  net.minecraft.core.particles.DustParticleOptions
 *  net.minecraft.core.particles.ParticleOptions
 *  net.minecraft.core.particles.ParticleTypes
 *  net.minecraft.core.particles.SimpleParticleType
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.network.chat.Component
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.EquipmentSlot
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.ai.attributes.AttributeModifier
 *  net.minecraft.world.entity.ai.attributes.AttributeModifier$Operation
 *  net.minecraft.world.entity.ai.attributes.Attributes
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.entity.projectile.Projectile
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.phys.Vec3
 *  net.minecraftforge.event.ItemAttributeModifierEvent
 *  net.minecraftforge.event.TickEvent$Phase
 *  net.minecraftforge.event.TickEvent$PlayerTickEvent
 *  net.minecraftforge.event.entity.living.LivingDamageEvent
 *  net.minecraftforge.event.entity.living.LivingHurtEvent
 *  net.minecraftforge.event.entity.player.ItemTooltipEvent
 *  net.minecraftforge.eventbus.api.EventPriority
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  net.minecraftforge.registries.ForgeRegistries
 *  org.joml.Vector3f
 */
package net.farmingtales.ftboss.item;

import io.redspace.ironsspellbooks.api.events.SpellDamageEvent;
import java.util.ArrayList;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import net.farmingtales.ftboss.entity.QingyiConfluenceTridentEntity;
import net.farmingtales.ftboss.item.ManorWeaponCatalog;
import net.farmingtales.ftboss.util.ManorWeaponBalanceRules;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
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

public final class ManorWeaponEnhancements {
    private static final Set<String> WEAPONS = ManorWeaponCatalog.ENTRIES.stream().map(ManorWeaponCatalog.Entry::item).collect(Collectors.toUnmodifiableSet());
    private static final UUID BONUS_ID = UUID.fromString("b18c0443-d0ae-45f6-bca5-34b396a73d8c");
    private static final String HIT_TIME = "ftboss:manor_weapon_fx_tick";

    private ManorWeaponEnhancements() {
    }

    public static String id(ItemStack stack) {
        ResourceLocation identifier = ForgeRegistries.ITEMS.getKey((Object)stack.m_41720_());
        return identifier == null ? "" : identifier.toString();
    }

    public static boolean isWeapon(ItemStack stack) {
        return !stack.m_41619_() && WEAPONS.contains(ManorWeaponEnhancements.id(stack));
    }

    @SubscribeEvent
    public static void attributes(ItemAttributeModifierEvent event) {
        if (event.getSlotType() != EquipmentSlot.MAINHAND || !ManorWeaponBalanceRules.attributeTarget(ManorWeaponEnhancements.isWeapon(event.getItemStack()), ManorWeaponEnhancements.id(event.getItemStack()))) {
            return;
        }
        ArrayList modifiers = new ArrayList(event.getModifiers().get((Object)Attributes.f_22281_));
        double original = modifiers.stream().filter(modifier -> modifier.m_22217_() == AttributeModifier.Operation.ADDITION).mapToDouble(AttributeModifier::m_22218_).sum();
        Optional<AttributeModifier> base = modifiers.stream().filter(modifier -> modifier.m_22217_() == AttributeModifier.Operation.ADDITION).findFirst();
        if (base.isPresent()) {
            AttributeModifier modifier2 = base.get();
            event.removeModifier(Attributes.f_22281_, modifier2);
            event.addModifier(Attributes.f_22281_, new AttributeModifier(modifier2.m_22209_(), modifier2.m_22214_(), modifier2.m_22218_() + ManorWeaponBalanceRules.attackBonus(original, ManorWeaponEnhancements.id(event.getItemStack())), AttributeModifier.Operation.ADDITION));
        } else {
            event.addModifier(Attributes.f_22281_, new AttributeModifier(BONUS_ID, "ftboss.manor_weapon", ManorWeaponBalanceRules.emptyHandBonus(ManorWeaponEnhancements.id(event.getItemStack())), AttributeModifier.Operation.ADDITION));
        }
    }

    private static String projectileWeapon(Entity direct) {
        if (!(direct instanceof Projectile)) {
            return "";
        }
        ResourceLocation type = ForgeRegistries.ENTITY_TYPES.getKey((Object)direct.m_6095_());
        if (type == null || !type.m_135827_().equals("tridents_n_stuff")) {
            return "";
        }
        String item = "tridents_n_stuff:" + type.m_135815_().replaceFirst("_entity$", "");
        return WEAPONS.contains(item) ? item : "";
    }

    @SubscribeEvent(priority=EventPriority.HIGH)
    public static void tridentDamage(LivingHurtEvent livingHurtEvent) {
        if (!(livingHurtEvent.getSource().m_7639_() instanceof ServerPlayer) || livingHurtEvent.getAmount() <= 0.0f) {
            return;
        }
        if (livingHurtEvent.getSource().m_7640_() instanceof QingyiConfluenceTridentEntity) {
            livingHurtEvent.setAmount(livingHurtEvent.getAmount() * 2.0f);
        } else if (!ManorWeaponEnhancements.projectileWeapon(livingHurtEvent.getSource().m_7640_()).isEmpty()) {
            livingHurtEvent.setAmount(livingHurtEvent.getAmount() * 6.0f);
        }
    }

    @SubscribeEvent
    public static void heldFx(TickEvent.PlayerTickEvent event) {
        ServerPlayer player;
        Player player2;
        if (event.phase != TickEvent.Phase.END || !((player2 = event.player) instanceof ServerPlayer) || !(player = (ServerPlayer)player2).m_6084_() || player.m_5833_() || player.m_20145_() || player.f_19797_ % 8 != 0) {
            return;
        }
        ManorWeaponEnhancements.emitHeld(player, player.m_21205_(), 1);
        ManorWeaponEnhancements.emitHeld(player, player.m_21206_(), -1);
    }

    private static void emitHeld(ServerPlayer player, ItemStack weapon, int side) {
        if (!ManorWeaponEnhancements.isWeapon(weapon)) {
            return;
        }
        Vec3 forward = Vec3.m_82498_((float)0.0f, (float)player.m_146908_());
        Vec3 right = new Vec3(-forward.f_82481_, 0.0, forward.f_82479_).m_82490_((double)side * 0.38);
        Vec3 point = player.m_20182_().m_82549_(forward.m_82490_(0.65)).m_82549_(right).m_82520_(0.0, 0.95, 0.0);
        double phase = (double)player.f_19797_ * 0.18;
        player.m_284548_().m_8767_((ParticleOptions)ManorWeaponEnhancements.dust(ManorWeaponEnhancements.id(weapon), weapon), point.f_82479_ + Math.cos(phase) * 0.12, point.f_82480_ + Math.sin(phase) * 0.16, point.f_82481_, 2, 0.04, 0.05, 0.04, 0.0);
        if (player.f_19797_ % 16 == 0) {
            player.m_284548_().m_8767_((ParticleOptions)ParticleTypes.f_123810_, point.f_82479_, point.f_82480_, point.f_82481_, 1, 0.04, 0.04, 0.04, 0.005);
        }
    }

    @SubscribeEvent(priority=EventPriority.LOWEST)
    public static void impactFx(LivingDamageEvent event) {
        Entity entity;
        if (event.getAmount() <= 0.0f || !((entity = event.getSource().m_7639_()) instanceof ServerPlayer)) {
            return;
        }
        ServerPlayer player = (ServerPlayer)entity;
        ItemStack weapon = player.m_21205_();
        String projectile = ManorWeaponEnhancements.projectileWeapon(event.getSource().m_7640_());
        if (projectile.isEmpty() && !ManorWeaponEnhancements.isWeapon(weapon)) {
            return;
        }
        LivingEntity target = event.getEntity();
        long now = player.m_9236_().m_46467_();
        CompoundTag data = target.getPersistentData();
        if (data.m_128441_(HIT_TIME) && now - data.m_128454_(HIT_TIME) < 4L) {
            return;
        }
        data.m_128356_(HIT_TIME, now);
        String identifier = projectile.isEmpty() ? ManorWeaponEnhancements.id(weapon) : projectile;
        player.m_284548_().m_8767_((ParticleOptions)ManorWeaponEnhancements.dust(identifier, weapon), target.m_20185_(), target.m_20186_() + (double)target.m_20206_() * 0.55, target.m_20189_(), 12, (double)target.m_20205_() * 0.4, 0.28, (double)target.m_20205_() * 0.4, 0.02);
        player.m_284548_().m_8767_((ParticleOptions)ManorWeaponEnhancements.accent(identifier), target.m_20185_(), target.m_20186_() + 0.7, target.m_20189_(), 4, 0.15, 0.2, 0.15, 0.08);
    }

    private static SimpleParticleType accent(String identifier) {
        return switch (identifier) {
            case "ftboss:black_cleaver" -> ParticleTypes.f_123784_;
            case "ftboss:shadow_sword", "tridents_n_stuff:the_desecrator" -> ParticleTypes.f_123789_;
            case "ftboss:dora_nova_launcher", "ftboss:dora_pulse_gun", "ftboss:dora_lance", "tridents_n_stuff:stormseeker" -> ParticleTypes.f_175830_;
            case "ftboss:lemoncat_skillet", "ftboss:lemoncat_spatula", "ftboss:maque_blessing_feather" -> ParticleTypes.f_123748_;
            case "ftboss:puffer_bat", "tridents_n_stuff:echo_of_the_deep" -> ParticleTypes.f_123772_;
            case "ftboss:xueli_skillet", "ftboss:xueli_rolling_pin" -> ParticleTypes.f_175834_;
            default -> ParticleTypes.f_123810_;
        };
    }

    private static DustParticleOptions dust(String identifier, ItemStack weapon) {
        int rgb = switch (identifier) {
            case "ftboss:black_cleaver" -> 13571902;
            case "ftboss:shadow_sword", "tridents_n_stuff:the_desecrator" -> 10376687;
            case "ftboss:dandan_scythe" -> 15850924;
            case "ftboss:statue_youzi", "ftboss:maque_blessing_feather" -> 16766826;
            case "ftboss:dora_nova_launcher", "ftboss:dora_pulse_gun", "ftboss:dora_lance" -> 16744908;
            case "ftboss:lemoncat_skillet", "ftboss:lemoncat_spatula" -> 14021484;
            case "ftboss:wenxuanlan_flywheel" -> {
                if (weapon.m_41782_() && weapon.m_41783_().m_128471_("XuanlanWhite")) {
                    yield 14873855;
                }
                yield 6550715;
            }
            case "ftboss:puffer_bat" -> 10872411;
            case "ftboss:xueli_skillet", "ftboss:xueli_rolling_pin" -> 16756580;
            case "tridents_n_stuff:stormseeker" -> 9298943;
            case "tridents_n_stuff:echo_of_the_deep" -> 4381392;
            default -> 13357311;
        };
        return new DustParticleOptions(new Vector3f((float)(rgb >> 16 & 0xFF) / 255.0f, (float)(rgb >> 8 & 0xFF) / 255.0f, (float)(rgb & 0xFF) / 255.0f), 1.0f);
    }

    @SubscribeEvent
    public static void tooltip(ItemTooltipEvent event) {
        if (ManorWeaponEnhancements.isWeapon(event.getItemStack())) {
            event.getToolTip().add(Component.m_237115_((String)"tooltip.ftboss.manor_weapon.enhanced").m_130940_(ChatFormatting.GOLD));
        }
    }

    @SubscribeEvent(priority=EventPriority.HIGH)
    public static void staffSpellDamage(SpellDamageEvent spellDamageEvent) {
        Entity entity;
        if (spellDamageEvent.getAmount() <= 0.0f || !((entity = spellDamageEvent.getSpellDamageSource().m_7639_()) instanceof ServerPlayer)) {
            return;
        }
        ServerPlayer serverPlayer = (ServerPlayer)entity;
        if (ManorWeaponEnhancements.id(serverPlayer.m_21205_()).equals("ftboss:qizai_prayer_staff") || ManorWeaponEnhancements.id(serverPlayer.m_21206_()).equals("ftboss:qizai_prayer_staff")) {
            spellDamageEvent.setAmount(spellDamageEvent.getAmount() * 2.0f);
        }
    }
}

