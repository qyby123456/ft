import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

public final class ManorWeaponFixtures {
    public static void main(String[] arguments) throws Exception {
        Path root = Path.of(arguments[0]);
        Map<String, String> sources = Map.ofEntries(
                Map.entry("net/minecraft/world/entity/Entity", "package net.minecraft.world.entity; public class Entity {}"),
                Map.entry("net/minecraft/world/entity/LivingEntity", "package net.minecraft.world.entity; public class LivingEntity extends Entity {}"),
                Map.entry("net/minecraft/world/entity/LightningBolt", """
                        package net.minecraft.world.entity;
                        public class LightningBolt extends Entity {
                            private float damage = 5.0F;
                            public float getDamage() { return damage; }
                            public void setDamage(float damage) { this.damage = damage; }
                        }
                        """),
                Map.entry("net/farmingtales/ftboss/entity/QingyiConfluenceTridentEntity", """
                        package net.farmingtales.ftboss.entity;
                        public class QingyiConfluenceTridentEntity extends net.minecraft.world.entity.Entity {}
                        """),
                Map.entry("net/minecraft/world/item/ItemStack", """
                        package net.minecraft.world.item;
                        public class ItemStack {
                            public final String identifier;
                            public ItemStack(String identifier) { this.identifier = identifier; }
                        }
                        """),
                Map.entry("net/minecraft/server/level/ServerPlayer", """
                        package net.minecraft.server.level;
                        import net.minecraft.world.item.ItemStack;
                        public class ServerPlayer extends net.minecraft.world.entity.LivingEntity {
                            public ItemStack main = new ItemStack("");
                            public ItemStack off = new ItemStack("");
                            public ItemStack m_21205_() { return main; }
                            public ItemStack m_21206_() { return off; }
                        }
                        """),
                Map.entry("net/minecraft/world/entity/EquipmentSlot", "package net.minecraft.world.entity; public enum EquipmentSlot { MAINHAND, OFFHAND, HEAD }"),
                Map.entry("net/minecraft/world/entity/ai/attributes/Attribute", "package net.minecraft.world.entity.ai.attributes; public class Attribute {}"),
                Map.entry("net/minecraft/world/entity/ai/attributes/Attributes", """
                        package net.minecraft.world.entity.ai.attributes;
                        public class Attributes {
                            public static final Attribute f_22281_ = new Attribute();
                            public static final Attribute SPEED = new Attribute();
                        }
                        """),
                Map.entry("net/minecraft/world/entity/ai/attributes/AttributeModifier", """
                        package net.minecraft.world.entity.ai.attributes;
                        import java.util.UUID;
                        public class AttributeModifier {
                            public enum Operation { ADDITION, MULTIPLY_BASE, MULTIPLY_TOTAL }
                            public final UUID identifier;
                            public final String name;
                            public final double amount;
                            public final Operation operation;
                            public AttributeModifier(UUID identifier, String name, double amount, Operation operation) {
                                this.identifier = identifier; this.name = name; this.amount = amount; this.operation = operation;
                            }
                            public UUID m_22209_() { return identifier; }
                            public String m_22214_() { return name; }
                            public double m_22218_() { return amount; }
                            public Operation m_22217_() { return operation; }
                        }
                        """),
                Map.entry("net/minecraftforge/event/ItemAttributeModifierEvent", """
                        package net.minecraftforge.event;
                        import com.google.common.collect.HashMultimap;
                        import com.google.common.collect.Multimap;
                        import net.minecraft.world.entity.EquipmentSlot;
                        import net.minecraft.world.item.ItemStack;
                        import net.minecraft.world.entity.ai.attributes.Attribute;
                        import net.minecraft.world.entity.ai.attributes.AttributeModifier;
                        public class ItemAttributeModifierEvent {
                            public final Multimap<Attribute, AttributeModifier> modifiers = HashMultimap.create();
                            public final ItemStack stack;
                            public final EquipmentSlot slot;
                            public ItemAttributeModifierEvent(ItemStack stack, EquipmentSlot slot) { this.stack = stack; this.slot = slot; }
                            public ItemStack getItemStack() { return stack; }
                            public EquipmentSlot getSlotType() { return slot; }
                            public Multimap<Attribute, AttributeModifier> getModifiers() { return modifiers; }
                            public boolean removeModifier(Attribute attribute, AttributeModifier modifier) { return modifiers.remove(attribute, modifier); }
                            public boolean addModifier(Attribute attribute, AttributeModifier modifier) { return modifiers.put(attribute, modifier); }
                        }
                        """),
                Map.entry("net/minecraft/world/damagesource/DamageSource", """
                        package net.minecraft.world.damagesource;
                        import net.minecraft.world.entity.Entity;
                        public class DamageSource {
                            public final Entity owner;
                            public final Entity direct;
                            public DamageSource(Entity owner, Entity direct) { this.owner = owner; this.direct = direct; }
                            public Entity m_7639_() { return owner; }
                            public Entity m_7640_() { return direct; }
                        }
                        """),
                Map.entry("io/redspace/ironsspellbooks/damage/SpellDamageSource", """
                        package io.redspace.ironsspellbooks.damage;
                        import net.minecraft.world.entity.Entity;
                        public class SpellDamageSource extends net.minecraft.world.damagesource.DamageSource {
                            public SpellDamageSource(Entity owner) { super(owner, owner); }
                        }
                        """),
                Map.entry("io/redspace/ironsspellbooks/api/events/SpellDamageEvent", """
                        package io.redspace.ironsspellbooks.api.events;
                        import io.redspace.ironsspellbooks.damage.SpellDamageSource;
                        public class SpellDamageEvent {
                            public float amount;
                            public final SpellDamageSource source;
                            public SpellDamageEvent(float amount, SpellDamageSource source) { this.amount = amount; this.source = source; }
                            public float getAmount() { return amount; }
                            public void setAmount(float amount) { this.amount = amount; }
                            public SpellDamageSource getSpellDamageSource() { return source; }
                        }
                        """),
                Map.entry("net/minecraftforge/event/entity/living/LivingHurtEvent", """
                        package net.minecraftforge.event.entity.living;
                        import net.minecraft.world.damagesource.DamageSource;
                        public class LivingHurtEvent {
                            public float amount;
                            public final DamageSource source;
                            public LivingHurtEvent(float amount, DamageSource source) { this.amount = amount; this.source = source; }
                            public float getAmount() { return amount; }
                            public void setAmount(float amount) { this.amount = amount; }
                            public DamageSource getSource() { return source; }
                        }
                        """),
                Map.entry("net/minecraftforge/eventbus/api/EventPriority", "package net.minecraftforge.eventbus.api; public enum EventPriority { HIGH, NORMAL }"),
                Map.entry("net/minecraftforge/eventbus/api/SubscribeEvent", """
                        package net.minecraftforge.eventbus.api;
                        import java.lang.annotation.Retention;
                        import java.lang.annotation.RetentionPolicy;
                        @Retention(RetentionPolicy.RUNTIME)
                        public @interface SubscribeEvent { EventPriority priority() default EventPriority.NORMAL; }
                        """),
                Map.entry("net/farmingtales/ftboss/item/ManorWeaponEnhancements", """
                        package net.farmingtales.ftboss.item;
                        import net.minecraft.world.item.ItemStack;
                        import net.minecraft.server.level.ServerPlayer;
                        import net.minecraftforge.eventbus.api.SubscribeEvent;
                        import net.minecraftforge.eventbus.api.EventPriority;
                        import io.redspace.ironsspellbooks.api.events.SpellDamageEvent;
                        public class ManorWeaponEnhancements {
                            private static final java.util.UUID BONUS_ID = java.util.UUID.fromString("b18c0443-d0ae-45f6-bca5-34b396a73d8c");
                            public static String id(ItemStack stack) { return stack.identifier; }
                            public static boolean isWeapon(ItemStack stack) {
                                return !stack.identifier.equals("ftboss:qingyi_confluence_trident")
                                        && (net.farmingtales.ftboss.util.ManorWeaponBalanceRules.doubled(stack.identifier)
                                        || stack.identifier.equals("ftboss:black_cleaver") || stack.identifier.equals("ftboss:statue_youzi"));
                            }
                            private static String projectileWeapon(net.minecraft.world.entity.Entity direct) {
                                return direct instanceof TestProjectile projectile ? projectile.identifier : "";
                            }
                            @SubscribeEvent(priority = EventPriority.HIGH)
                            public static void tridentDamage(net.minecraftforge.event.entity.living.LivingHurtEvent event) {
                                if (!(event.getSource().m_7639_() instanceof ServerPlayer) || event.getAmount() <= 0) return;
                                if (event.getSource().m_7640_() instanceof net.farmingtales.ftboss.entity.QingyiConfluenceTridentEntity) {
                                    event.setAmount(event.getAmount() * 2.0F);
                                } else if (!projectileWeapon(event.getSource().m_7640_()).isEmpty()) event.setAmount(event.getAmount() * 6.0F);
                            }
                            @SubscribeEvent(priority = EventPriority.HIGH)
                            public static void staffSpellDamage(SpellDamageEvent event) {
                                if (event.getAmount() <= 0 || !(event.getSpellDamageSource().m_7639_() instanceof ServerPlayer player)) return;
                                if (id(player.m_21205_()).equals("ftboss:qizai_prayer_staff")
                                        || id(player.m_21206_()).equals("ftboss:qizai_prayer_staff")) event.setAmount(event.getAmount() * 2.0F);
                            }
                        }
                        """),
                Map.entry("net/farmingtales/ftboss/item/TestProjectile", """
                        package net.farmingtales.ftboss.item;
                        public class TestProjectile extends net.minecraft.world.entity.Entity {
                            public final String identifier;
                            public TestProjectile(String identifier) { this.identifier = identifier; }
                        }
                        """));
        for (var source : sources.entrySet()) {
            Path file = root.resolve(source.getKey() + ".java");
            Files.createDirectories(file.getParent());
            Files.writeString(file, source.getValue() + "\n");
        }
    }
}
