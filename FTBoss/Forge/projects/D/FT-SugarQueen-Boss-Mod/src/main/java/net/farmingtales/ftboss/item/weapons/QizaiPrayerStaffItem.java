package net.farmingtales.ftboss.item.weapons;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import net.farmingtales.ftboss.FTBossMod;
import net.minecraft.ChatFormatting;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Vector3f;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

@Mod.EventBusSubscriber(modid = FTBossMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class QizaiPrayerStaffItem extends io.redspace.ironsspellbooks.item.CastingItem implements GeoItem {
    private static final double BASE_SPELL_POWER_BONUS = 0.5;
    private static final double BASE_MAX_MANA_BONUS = 300.0;
    private static final UUID SPELL_POWER_UUID = UUID.fromString("d07468dc-f8c6-40ba-9dbc-a97cfdb29a55");
    private static final UUID MAX_MANA_UUID = UUID.fromString("7633e078-e7df-49cc-92fb-d9a806f361ca");
    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("animation.qizai_staff.idle");
    private static final RawAnimation CHARGE = RawAnimation.begin().thenPlayAndHold("animation.qizai_staff.charge");
    private static final RawAnimation RELEASE = RawAnimation.begin().thenPlay("animation.qizai_staff.release");
    private static final DustParticleOptions CRIMSON_STAR =
            new DustParticleOptions(new Vector3f(0.96F, 0.08F, 0.30F), 1.05F);
    private static final DustParticleOptions GOLD_STAR =
            new DustParticleOptions(new Vector3f(1.00F, 0.66F, 0.20F), 0.82F);
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private Multimap<Attribute, AttributeModifier> modifiers;

    public QizaiPrayerStaffItem() {
        super(new Properties().stacksTo(1).rarity(Rarity.EPIC).fireResistant());
        GeoItem.registerSyncedAnimatable(this);
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(EquipmentSlot slot) {
        if (slot != EquipmentSlot.MAINHAND) return super.getDefaultAttributeModifiers(slot);
        if (modifiers == null) {
            modifiers = ImmutableMultimap.<Attribute, AttributeModifier>builder()
                    .putAll(super.getDefaultAttributeModifiers(slot))
                    .put(AttributeRegistry.SPELL_POWER.get(), new AttributeModifier(SPELL_POWER_UUID,
                            "Qizai staff spell power", BASE_SPELL_POWER_BONUS, AttributeModifier.Operation.ADDITION))
                    .put(AttributeRegistry.MAX_MANA.get(), new AttributeModifier(MAX_MANA_UUID,
                            "Qizai staff max mana", BASE_MAX_MANA_BONUS, AttributeModifier.Operation.ADDITION))
                    .build();
        }
        return modifiers;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "prayer", 0, state -> state.setAndContinue(IDLE))
                .triggerableAnim("charge", CHARGE).triggerableAnim("release", RELEASE));
    }

    public static void animate(net.minecraft.world.entity.LivingEntity holder, boolean charge) {
        if (!(holder.level() instanceof net.minecraft.server.level.ServerLevel level)) return;
        ItemStack stack = holder.getMainHandItem();
        if (stack.getItem() instanceof QizaiPrayerStaffItem staff) {
            staff.triggerAnim(holder, GeoItem.getOrAssignId(stack, level), "prayer", charge ? "charge" : "release");
        }
    }

    @Override
    public boolean onEntityItemUpdate(ItemStack stack, ItemEntity entity) {
        if (entity.level() instanceof ServerLevel level && entity.getAge() % 8 == 0) {
            double phase = entity.getAge() * 0.18;
            for (int side = 0; side < 2; side++) {
                double angle = phase + side * Math.PI;
                double radius = 0.24;
                level.sendParticles(side == 0 ? CRIMSON_STAR : GOLD_STAR,
                        entity.getX() + Math.cos(angle) * radius,
                        entity.getY() + 0.28 + Math.sin(phase * 0.55) * 0.045,
                        entity.getZ() + Math.sin(angle) * radius,
                        1, 0.0, 0.0, 0.0, 0.0);
            }
            if (entity.getAge() % 16 == 0) {
                level.sendParticles(ParticleTypes.END_ROD,
                        entity.getX(), entity.getY() + 0.36, entity.getZ(),
                        1, 0.10, 0.12, 0.10, 0.01);
            }
        }
        return false;
    }

    @SubscribeEvent
    public static void onStaffPickup(EntityItemPickupEvent event) {
        if (event.isCanceled()
                || !(event.getItem().getItem().getItem() instanceof QizaiPrayerStaffItem)
                || !(event.getEntity().level() instanceof ServerLevel level)) return;

        ItemEntity item = event.getItem();
        double x = item.getX();
        double y = item.getY() + 0.62;
        double z = item.getZ();
        double facing = Math.toRadians(event.getEntity().getYRot());
        drawPickupStar(level, x, y, z, facing);
        level.sendParticles(ParticleTypes.END_ROD, x, y, z,
                14, 0.30, 0.38, 0.30, 0.055);
        level.sendParticles(ParticleTypes.ENCHANT, x, y, z,
                22, 0.42, 0.48, 0.42, 0.13);
        level.playSound(null, item.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME,
                SoundSource.PLAYERS, 0.95F, 1.22F + event.getEntity().getRandom().nextFloat() * 0.18F);
    }

    private static void drawPickupStar(ServerLevel level, double x, double y, double z, double facing) {
        double rightX = Math.cos(facing);
        double rightZ = Math.sin(facing);
        for (int edge = 0; edge < 10; edge++) {
            double fromAngle = -Math.PI / 2.0 + edge * Math.PI / 5.0;
            double toAngle = -Math.PI / 2.0 + (edge + 1) * Math.PI / 5.0;
            double fromRadius = (edge & 1) == 0 ? 0.58 : 0.25;
            double toRadius = ((edge + 1) & 1) == 0 ? 0.58 : 0.25;
            double fromHorizontal = Math.cos(fromAngle) * fromRadius;
            double fromVertical = Math.sin(fromAngle) * fromRadius;
            double toHorizontal = Math.cos(toAngle) * toRadius;
            double toVertical = Math.sin(toAngle) * toRadius;
            for (int sample = 0; sample < 2; sample++) {
                double progress = sample / 2.0;
                double horizontal = fromHorizontal + (toHorizontal - fromHorizontal) * progress;
                double vertical = fromVertical + (toVertical - fromVertical) * progress;
                level.sendParticles(CRIMSON_STAR,
                        x + rightX * horizontal, y + vertical, z + rightZ * horizontal,
                        1, 0.0, 0.0, 0.0, 0.0);
            }
            if ((edge & 1) == 0) {
                level.sendParticles(GOLD_STAR,
                        x + rightX * fromHorizontal, y + fromVertical, z + rightZ * fromHorizontal,
                        1, 0.0, 0.0, 0.0, 0.0);
            }
        }
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() { return cache; }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> lines, TooltipFlag flag) {
        super.appendHoverText(stack, level, lines, flag);
        lines.add(Component.translatable("tooltip.ftboss.qizai_prayer_staff").withStyle(ChatFormatting.LIGHT_PURPLE));
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private BlockEntityWithoutLevelRenderer renderer;
            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (renderer == null) renderer = new net.farmingtales.ftboss.client.render.QizaiStaffRenderer();
                return renderer;
            }
        });
    }
}
