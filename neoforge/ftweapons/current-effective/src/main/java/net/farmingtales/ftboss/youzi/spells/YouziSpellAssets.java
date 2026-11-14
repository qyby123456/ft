package net.farmingtales.ftboss.youzi.spells;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import net.farmingtales.ftboss.youzi.YouziHomingFlight.Parameters;
import org.apache.logging.log4j.LogManager;

public final class YouziSpellAssets {
    private static YouziSpellGeometry geometry;
    private static JsonObject flight;
    private static boolean failed;

    private static JsonObject read(String var0) throws Exception {
        JsonObject var2;
        try (InputStream var1 = YouziSpellAssets.class.getResourceAsStream("/assets/ftboss/youzi_spells/" + var0)) {
            if (var1 == null) {
                throw new IllegalStateException("Missing authored resource " + var0);
            }

            var2 = JsonParser.parseReader(new InputStreamReader(var1, StandardCharsets.UTF_8)).getAsJsonObject();
        }

        return var2;
    }

    public static synchronized YouziSpellGeometry geometry() {
        if (geometry == null && !failed) {
            try {
                geometry = new YouziSpellGeometry(read("models.json"), read("texture_layout.json"), read("safety.json"));
                flight = read("effigy_flight.json").getAsJsonObject("motion");
            } catch (Exception var1) {
                failed = true;
                LogManager.getLogger("YouziSpellRuntime").error("Authored spell resource invalid; preserving original spell implementation", (Throwable)var1);
                geometry = null;
            }

            return geometry;
        } else {
            return geometry;
        }
    }

    public static Parameters parameters(double var0) {
        if (geometry() == null) {
            throw new IllegalStateException("Missing verified flight configuration");
        } else {
            return new Parameters(
                flight.get("initialSpeed").getAsDouble(),
                flight.get("maximumSpeed").getAsDouble(),
                flight.get("accelerationSeconds").getAsDouble(),
                flight.get("maximumTurnDegreesPerSecond").getAsDouble(),
                flight.get("predictionSeconds").getAsDouble(),
                flight.get("maximumDistance").getAsDouble() * var0,
                flight.get("maximumLifetime").getAsDouble(),
                flight.get("fixedStepSeconds").getAsDouble(),
                flight.get("projectileRadius").getAsDouble() * var0
            );
        }
    }
}
