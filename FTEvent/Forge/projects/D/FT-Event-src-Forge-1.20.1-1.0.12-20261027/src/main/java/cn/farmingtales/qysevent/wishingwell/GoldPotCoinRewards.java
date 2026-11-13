package cn.farmingtales.qysevent.wishingwell;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class GoldPotCoinRewards {
    private static final String DEFAULT_RESOURCE = "/data/qys_event/wishing_well/gold_pot_coins.json";
    private static final List<Integer> AMOUNTS = List.of(1, 100, 1000, 10000, 100000, 1000000);

    public record Entry(int coins, int weight) {}

    private final List<Entry> entries;
    private final int totalWeight;

    private GoldPotCoinRewards(List<Entry> entries) {
        this.entries = List.copyOf(entries);
        int total = 0;
        for (Entry entry : entries) total = Math.addExact(total, entry.weight());
        this.totalWeight = total;
    }

    public static GoldPotCoinRewards load(Path file) throws IOException {
        if (!Files.exists(file)) {
            Files.createDirectories(file.getParent());
            try (InputStream source = defaultStream()) {
                Files.copy(source, file);
            }
        }
        try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            return read(reader);
        }
    }

    public static GoldPotCoinRewards defaults() {
        try (Reader reader = new InputStreamReader(defaultStream(), StandardCharsets.UTF_8)) {
            return read(reader);
        } catch (IOException error) {
            throw new IllegalStateException("Cannot read default gold-pot coin rewards", error);
        }
    }

    static GoldPotCoinRewards read(Reader reader) {
        List<Entry> entries = new ArrayList<>();
        for (JsonElement element : JsonParser.parseReader(reader).getAsJsonObject().getAsJsonArray("rewards")) {
            int coins = element.getAsJsonObject().get("coins").getAsBigDecimal().intValueExact();
            int weight = element.getAsJsonObject().get("weight").getAsBigDecimal().intValueExact();
            if (entries.size() >= AMOUNTS.size() || coins != AMOUNTS.get(entries.size()) || weight <= 0) {
                throw new IllegalArgumentException("Coin rewards must list 1, 100, 1000, 10000, 100000, 1000000 with positive integer weights");
            }
            entries.add(new Entry(coins, weight));
        }
        if (entries.size() != AMOUNTS.size()) throw new IllegalArgumentException("All six coin rewards are required");
        return new GoldPotCoinRewards(entries);
    }

    private static InputStream defaultStream() {
        InputStream source = GoldPotCoinRewards.class.getResourceAsStream(DEFAULT_RESOURCE);
        if (source == null) throw new IllegalStateException("Missing gold-pot coin reward defaults");
        return source;
    }

    public int totalWeight() {
        return totalWeight;
    }

    public int coinsForRoll(int ticket) {
        if (ticket < 0 || ticket >= totalWeight) throw new IllegalArgumentException("Invalid coin ticket");
        for (Entry entry : entries) {
            ticket -= entry.weight();
            if (ticket < 0) return entry.coins();
        }
        throw new IllegalStateException("Coin weights do not match their total");
    }

    public double expectedCoins() {
        long total = 0;
        for (Entry entry : entries) total += (long) entry.coins() * entry.weight();
        return (double) total / totalWeight;
    }
}
