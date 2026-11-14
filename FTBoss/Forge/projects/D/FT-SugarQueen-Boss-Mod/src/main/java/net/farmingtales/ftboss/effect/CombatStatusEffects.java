package net.farmingtales.ftboss.effect;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegisterEvent;

/** 九种共享状态的独立注册入口。2026-10-26：不依赖保留版 FTBossMod 构造函数。 */
@Mod.EventBusSubscriber(modid = "ftboss", bus = Mod.EventBusSubscriber.Bus.MOD)
public final class CombatStatusEffects {
    public static final CombatStatusEffect ARMOR_BREAK = new CombatStatusEffect("armor_break", 0xB8BEC7);
    public static final CombatStatusEffect BONE_FRACTURE = new CombatStatusEffect("bone_fracture", 0xD6C6A1);
    public static final CombatStatusEffect GRIEVOUS_WOUND = new CombatStatusEffect("grievous_wound", 0xA52D42);
    public static final CombatStatusEffect STUN = new CombatStatusEffect("stun", 0xF0D256);
    public static final CombatStatusEffect ROOTED = new CombatStatusEffect("rooted", 0x788D58);
    public static final CombatStatusEffect IGNITE = new CombatStatusEffect("ignite", 0xF49A35);
    public static final CombatStatusEffect AIRBORNE = new CombatStatusEffect("airborne", 0xA2D4E5);
    public static final CombatStatusEffect FLICKERING_DARKNESS = new CombatStatusEffect("flickering_darkness", 0x64507D);
    public static final CombatStatusEffect VALOR = new CombatStatusEffect("valor", 0xE6B94E);

    private CombatStatusEffects() {}

    public static CombatStatusEffect[] all() {
        return new CombatStatusEffect[]{ARMOR_BREAK, BONE_FRACTURE, GRIEVOUS_WOUND, STUN, ROOTED,
                IGNITE, AIRBORNE, FLICKERING_DARKNESS, VALOR};
    }

    @SubscribeEvent
    public static void register(RegisterEvent event) {
        event.register(ForgeRegistries.Keys.MOB_EFFECTS, helper -> {
            for (CombatStatusEffect effect : all()) helper.register(new ResourceLocation("ftboss", effect.id()), effect);
        });
    }
}
