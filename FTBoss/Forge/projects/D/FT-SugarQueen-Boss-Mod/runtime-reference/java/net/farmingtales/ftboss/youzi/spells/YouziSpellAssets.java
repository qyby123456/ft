/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonObject
 *  com.google.gson.JsonParser
 *  org.apache.logging.log4j.LogManager
 */
package net.farmingtales.ftboss.youzi.spells;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import net.farmingtales.ftboss.youzi.YouziHomingFlight;
import net.farmingtales.ftboss.youzi.spells.YouziSpellGeometry;
import org.apache.logging.log4j.LogManager;

public final class YouziSpellAssets {
    private static YouziSpellGeometry geometry;
    private static JsonObject flight;
    private static boolean failed;

    private static JsonObject read(String string) throws Exception {
        try (InputStream inputStream = YouziSpellAssets.class.getResourceAsStream("/assets/ftboss/youzi_spells/" + string);){
            if (inputStream == null) {
                throw new IllegalStateException("Missing authored resource " + string);
            }
            JsonObject jsonObject = JsonParser.parseReader((Reader)new InputStreamReader(inputStream, StandardCharsets.UTF_8)).getAsJsonObject();
            return jsonObject;
        }
    }

    public static synchronized YouziSpellGeometry geometry() {
        if (geometry != null || failed) {
            return geometry;
        }
        try {
            geometry = new YouziSpellGeometry(YouziSpellAssets.read("models.json"), YouziSpellAssets.read("texture_layout.json"), YouziSpellAssets.read("safety.json"));
            flight = YouziSpellAssets.read("effigy_flight.json").getAsJsonObject("motion");
        }
        catch (Exception exception) {
            failed = true;
            LogManager.getLogger((String)"YouziSpellRuntime").error("Authored spell resource invalid; preserving original spell implementation", (Throwable)exception);
            geometry = null;
        }
        return geometry;
    }

    public static YouziHomingFlight.Parameters parameters(double d) {
        if (YouziSpellAssets.geometry() == null) {
            throw new IllegalStateException("Missing verified flight configuration");
        }
        return new YouziHomingFlight.Parameters(flight.get("initialSpeed").getAsDouble(), flight.get("maximumSpeed").getAsDouble(), flight.get("accelerationSeconds").getAsDouble(), flight.get("maximumTurnDegreesPerSecond").getAsDouble(), flight.get("predictionSeconds").getAsDouble(), flight.get("maximumDistance").getAsDouble() * d, flight.get("maximumLifetime").getAsDouble(), flight.get("fixedStepSeconds").getAsDouble(), flight.get("projectileRadius").getAsDouble() * d);
    }
}

