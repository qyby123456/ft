package com.farmingtales.drinkanim.client;

import net.minecraftforge.fml.ModList;
import java.lang.reflect.Method;

/** The optional FirstPerson API is accessed without bundling or editing that mod. */
public final class FirstPersonCompat {
    private static boolean checked, holding, previous;
    private static Method get, set;
    public static void update(boolean active) {
        if(!checked) {
            checked=true;
            if(ModList.get().isLoaded("firstperson")) try {
                Class<?> c=Class.forName("dev.tr7zw.firstperson.api.FirstPersonAPI");
                get=c.getMethod("isEnabled");set=c.getMethod("setEnabled",boolean.class);
            } catch(ReflectiveOperationException ignored) { get=null; }
        }
        if(get==null) return;
        try {
            if(active) {
                if(!holding) { previous=(boolean)get.invoke(null);holding=true; }
                set.invoke(null,false);
            } else if(holding) { set.invoke(null,previous);holding=false; }
        } catch(ReflectiveOperationException ignored) {get=null;holding=false;}
    }
}
