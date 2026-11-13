package net.farmingtales.ftboss.api;

import java.util.Objects;
import java.util.function.Consumer;
import net.farmingtales.ftboss.util.BossRelicRules.Kind;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.entity.player.PlayerEvent.Clone;

public final class OptionalWeaponModule {
    public interface Service {
        default int evolutionLevel(ItemStack stack) { return 0; }
        default int socketCount(ItemStack stack) { return 0; }
        default int socketVisualColor(ItemStack stack, int index) { return -1; }
        default int skillCooldown(ItemStack stack, int originalTicks) { return originalTicks; }
        default float activeSkillMultiplier(ItemStack stack) { return 1.0F; }
        default boolean supports(ItemStack stack) { return false; }
        default void cast(ServerPlayer player, ItemStack stack) {}
        default void copyCooldownData(Clone event) {}
        default void reward(Kind kind, Consumer<ItemStack> output) {}
    }

    private static final Service ABSENT = new Service() {};
    private static volatile Service service = ABSENT;

    private OptionalWeaponModule() {}

    public static synchronized void install(Service implementation) {
        Objects.requireNonNull(implementation);
        if (service != ABSENT) throw new IllegalStateException("Weapon module already installed");
        service = implementation;
    }

    public static int evolutionLevel(ItemStack stack) { return service.evolutionLevel(stack); }
    public static int socketCount(ItemStack stack) { return service.socketCount(stack); }
    public static int socketVisualColor(ItemStack stack, int index) { return service.socketVisualColor(stack, index); }
    public static int skillCooldown(ItemStack stack, int originalTicks) { return service.skillCooldown(stack, originalTicks); }
    public static float activeSkillMultiplier(ItemStack stack) { return service.activeSkillMultiplier(stack); }
    public static boolean supports(ItemStack stack) { return service.supports(stack); }
    public static void cast(ServerPlayer player, ItemStack stack) { service.cast(player, stack); }
    public static void copyCooldownData(Clone event) { service.copyCooldownData(event); }
    public static void reward(Kind kind, Consumer<ItemStack> output) { service.reward(kind, output); }
}
