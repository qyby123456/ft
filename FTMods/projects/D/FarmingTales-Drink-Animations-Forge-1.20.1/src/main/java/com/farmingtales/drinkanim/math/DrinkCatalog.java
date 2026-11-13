package com.farmingtales.drinkanim.math;

import com.google.gson.Gson;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

public final class DrinkCatalog {
    private static final Map<String,DrinkProfile> PROFILES=load();
    private static final Map<String,DrinkProfile> AGED=loadAged();
    public static final DrinkProfile ORANGE=PROFILES.get("farmingtales:orange_wine");
    public static DrinkProfile find(String id) {
        var exact=PROFILES.get(id);
        if(exact!=null||id==null)return exact;
        return AGED.get(id);
    }
    public static Map<String,DrinkProfile> aged() {return AGED;}
    public static Collection<DrinkProfile> all() {return PROFILES.values();}
    private static Map<String,DrinkProfile> loadAged() {
        try(var stream=DrinkCatalog.class.getResourceAsStream("/assets/farmingtales_drink_anim/aged_aliases.json")) {
            if(stream==null)throw new IllegalStateException("Missing aged drink aliases");
            var definitions=new Gson().fromJson(new InputStreamReader(stream,StandardCharsets.UTF_8),com.google.gson.JsonObject.class);
            var aliases=new LinkedHashMap<String,DrinkProfile>();
            for(var entry:definitions.entrySet()) {
                var base=PROFILES.get(entry.getValue().getAsString());
                if(base==null||PROFILES.containsKey(entry.getKey()))throw new IllegalStateException("Invalid aged alias "+entry.getKey());
                // Use the identical canonical object: ID-based closures, gestures and ornaments stay shared.
                aliases.put(entry.getKey(),base);
            }
            return Collections.unmodifiableMap(aliases);
        } catch(java.io.IOException ex) {throw new IllegalStateException("Cannot load aged drink aliases",ex);}
    }
    private static Map<String,DrinkProfile> load() {
        try(var stream=DrinkCatalog.class.getResourceAsStream("/assets/farmingtales_drink_anim/drink_profiles.json")) {
            if(stream==null)throw new IllegalStateException("Missing drink profiles");
            var map=new LinkedHashMap<String,DrinkProfile>();
            for(var profile:new Gson().fromJson(new InputStreamReader(stream,StandardCharsets.UTF_8),DrinkProfile[].class)) {
                if(map.put(profile.id(),profile)!=null)throw new IllegalStateException("Duplicate drink "+profile.id());
            }
            return Collections.unmodifiableMap(map);
        } catch(java.io.IOException ex) {throw new IllegalStateException("Cannot load drink profiles",ex);}
    }
    private DrinkCatalog() {}
}
