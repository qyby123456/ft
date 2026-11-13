package com.farmingtales.drinkanim.client;

import com.farmingtales.drinkanim.math.DrinkProfile;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;

/** Use the held item's own color provider, including per-stack cocktail recipes. */
public final class LiquidTint {
    private static final Map<String,Integer> INDICES=load();
    private static Map<String,Integer> load() {
        try(var in=LiquidTint.class.getResourceAsStream("/assets/farmingtales_drink_anim/liquid_tints.json")) {
            if(in==null)throw new IllegalStateException("Missing liquid tint definitions");
            return Map.copyOf(new Gson().fromJson(new InputStreamReader(in,StandardCharsets.UTF_8),
                new TypeToken<Map<String,Integer>>(){}.getType()));
        } catch(java.io.IOException e) {throw new IllegalStateException(e);}
    }
    public static int color(DrinkProfile profile,ItemStack stack) {
        Integer tint=INDICES.get(profile.id());
        if(tint==null)return 0xFFFFFF;
        return Minecraft.getInstance().getItemColors().getColor(stack,tint)&0xFFFFFF;
    }
    private LiquidTint() {}
}
