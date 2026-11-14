package net.farmingtales.ftboss.integ.tridents;

import com.mojang.serialization.Codec;
import java.util.Set;
import net.farmingtales.ftboss.FTBossMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public final class TridentIntegration {
    private static final Set<String> DISABLED_ICONS = Set.of(
            "icon_of_storms", "icon_of_evil", "icon_of_depths", "icon_of_stars", "icon_of_blood");
    private static final DeferredRegister<Codec<? extends IGlobalLootModifier>> LOOT_MODIFIERS =
            DeferredRegister.create(ForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, FTBossMod.MOD_ID);

    static {
        LOOT_MODIFIERS.register("remove_trident_icons", () -> TridentIconLootModifier.CODEC);
    }

    private TridentIntegration() {}

    public static void register(IEventBus modBus) {
        LOOT_MODIFIERS.register(modBus);
        modBus.addListener(EventPriority.LOWEST, TridentIntegration::filterCreativeContents);
        /* ★ 2026-09-24：血腥契约**只在对敌对生物造成伤害时吸血**（用户要求：先「去除血腥契约攻击假人上时
           给玩家的回复」，再明确成「血腥契约只在攻击敌对生物产生吸血」）。
           挂在这里是因为合并版的 FTBossMod 一定会调用本方法（FTBossMod.java 本身是被合并覆盖的，
           不能在那边加注册）；详见 SanguineLifestealGuard 头部。 */
        SanguineLifestealGuard.register();
    }

    public static boolean isDisabledIcon(ItemStack stack) {
        ResourceLocation id = ForgeRegistries.ITEMS.getKey(stack.getItem());
        return id != null && id.getNamespace().equals("tridents_n_stuff")
                && DISABLED_ICONS.contains(id.getPath());
    }

    private static void filterCreativeContents(BuildCreativeModeTabContentsEvent event) {
        var entries = event.getEntries().iterator();
        while (entries.hasNext()) {
            if (isDisabledIcon(entries.next().getKey())) entries.remove();
        }
    }
}
