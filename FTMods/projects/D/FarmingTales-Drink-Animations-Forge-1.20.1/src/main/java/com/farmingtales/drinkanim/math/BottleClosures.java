package com.farmingtales.drinkanim.math;

import com.google.gson.JsonParser;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.HashMap;

/** Explicit opening mechanisms for the individually fitted Brewery/Vinery bottles. */
public final class BottleClosures {
    private static final Map<String,String> TYPES=load();
    private static Map<String,String> load() {
        try(var in=BottleClosures.class.getResourceAsStream("/assets/farmingtales_drink_anim/letsdo_closures.json")) {
            if(in==null)throw new IllegalStateException("Missing bottle closure definitions");
            var result=new HashMap<String,String>();
            JsonParser.parseReader(new InputStreamReader(in,StandardCharsets.UTF_8)).getAsJsonObject()
                .entrySet().forEach(e->result.put(e.getKey(),e.getValue().getAsString()));
            return Map.copyOf(result);
        } catch(java.io.IOException e) {throw new IllegalStateException(e);}
    }
    public static boolean waterCap(DrinkProfile p) {return switch(p.id()) {
        case "kaleidoscope_world_liquor:cool_tea", "kaleidoscope_world_liquor:sour_plum", "smc:ice_tea" -> true;
        default -> false;
    };}
    public static boolean screw(DrinkProfile p) {return waterCap(p)||LandNo1BottleAnimations.owns(p)||"screw".equals(TYPES.get(p.id()));}
    public static boolean stopper(DrinkProfile p) {return "stopper".equals(TYPES.get(p.id()));}
    public static boolean fixedBottle(DrinkProfile p) {return TYPES.containsKey(p.id());}
    public static boolean longCork(DrinkProfile p) {return fixedBottle(p)&&!screw(p);}
    public static float corkLength(DrinkProfile p) {return 3*(p.capPivot()[1]-p.mouthY())/16;}
    public static float corkClipBottom(DrinkProfile p,double lift) {
        return (float)Math.max(p.capPivot()[1]/16-corkLength(p),p.mouthY()/16-lift);
    }
    private BottleClosures() {}
}
