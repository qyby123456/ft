package net.farmingtales.ftboss.util;

import java.util.Locale;

/** 食物羁绊分类纯规则；2026-10-21 补齐水果、乳品、蛋类、酒名及标签，保留旧分类优先级。 */
public final class FoodSocketFoodRules {
    private FoodSocketFoodRules() {}

    public static String legacyType(String id) {
        if (id == null || !id.matches("[a-z0-9_.-]+:[a-z0-9_./-]+")) return null;
        String path = id.substring(id.indexOf(':') + 1);
        if (word(path, "cheese", "cheesy", "fromage")) return "CHEESE";
        if (word(path, "long_island_iced_tea", "wine", "beer", "ale", "mead", "cider", "liquor", "vodka",
                "whiskey", "whisky", "brew", "sake", "rum", "stout", "grog", "champagne", "burgundy", "brandy",
                "bourbon", "absinthe", "tequila", "sangria", "cocktail", "lager", "kvass", "gin_tonic")) return "WINE";
        if (word(path, "tea", "matcha", "oolong", "kombucha", "herbal_infusion")) return "TEA";
        if (word(path, "apple", "applesauce")) return "APPLE";
        if (word(path, "fish", "pufferfish", "salmon", "cod", "puffer", "tuna", "shrimp", "prawn", "crab", "lobster",
                "seafood", "squid", "bass", "clam", "mussel", "oyster", "eel", "lamprey", "roe", "kipper",
                "anchovy", "trout", "tilapia", "octopus", "sashimi", "nigiri", "futomaki", "kabayaki")) return "FISH";
        if (word(path, "beef", "pork", "chicken", "mutton", "lamb", "rabbit", "meat", "turkey", "sausage",
                "ham", "bacon", "steak", "venison", "boar", "duck", "goose", "veal", "jerky", "offal",
                "meatball", "chop", "patty")) return "MEAT";
        if (word(path, "coffee", "cocoa", "chili", "pepper", "spicy", "energy", "espresso", "latte",
                "cappuccino", "mocha", "cola") || id.equals("minecraft:redstone")) return "ENERGY";
        if (word(path, "carrot", "potato", "beet", "beetroot", "cabbage", "tomato", "onion", "corn", "maize", "salad",
                "vegetable", "compost", "mushroom", "pumpkin", "leek", "lettuce", "turnip", "radish", "cucumber",
                "pickle", "pickles", "soybean", "tofu", "asparagus", "eggplant", "zucchini", "spinach", "broccoli", "pea", "peas",
                "bean", "kelp", "seaweed", "artichoke", "cauliflower")) return "VEGETABLE";
        if (word(path, "sugar", "candy", "cake", "cookie", "honey", "chocolate", "sweet", "donut", "pie",
                "marshmallow", "custard", "gummy", "cupcake", "tart", "pastry", "croissant", "biscuit", "dango",
                "mochi", "popsicle", "ice_cream", "pudding", "jam", "jelly", "marmalade", "candied", "fudge",
                "truffle", "macaron")) return "SWEET";
        return null;
    }

    public static String type(String id, boolean edible, Iterable<String> tags) {
        String legacy = legacyType(id);
        if (legacy != null) return legacy;
        if (!edible || id == null || !id.matches("[a-z0-9_.-]+:[a-z0-9_./-]+")) return null;
        String path = id.substring(id.indexOf(':') + 1);
        if (word(path, "cheesecake", "fondue", "cheddar", "gouda", "ricotta", "mozzarella", "gorgonzola", "raclette")) return "CHEESE";
        if (word(path, "liqueur", "wheatbeer", "butterbeer", "eiswein", "daiginjo", "sherry", "maotai", "dassai",
                "carignan", "riesling", "sauvignon", "gin", "margarita", "pina_colada", "kwas", "strongbow")) return "WINE";
        if (word(path, "rooibos", "yerba_mate", "chamomile", "hibiscus_infusion")) return "TEA";
        if (word(path, "frappuccino", "mozhao", "mozhao_p", "vitamin_drink", "guarana")) return "ENERGY";
        if (word(path, "catfish", "bluefish", "herring", "sardine", "mackerel", "croaker", "uni", "urchin", "surimi",
                "calamari", "ceviche", "moqueca", "california_roll", "buddha_jumps_over_the_wall")) return "FISH";
        if (word(path, "bushmeat", "boarchop", "meatballs", "ribs", "spare_ribs", "bunny", "donkey", "dodo", "lizard_tail",
                "frog", "froglegs", "frog_leg", "mid_wing", "yakitori", "tartare", "goulash", "kabobs", "kebab",
                "egg", "eggs", "omelet", "omelette", "mayonnaise")) return "MEAT";
        if (word(path, "brownie", "waffle", "pancake", "pancakes", "mousse", "tiramisu", "cream_puff", "scone", "scones",
                "taosu", "paofu", "panettone", "mooncake", "icebergcream", "icecream", "taffy", "caramel", "syrup",
                "sundae", "fondant", "mizuyokan", "daifuku", "yokan", "croissants", "chocolates")) return "SWEET";
        if (word(path, "milk", "milkshake", "yogurt", "yoghurt", "butter", "cream", "kefir")) return "CHEESE";
        if (word(path, "potatoes", "tomatoes", "vegetables", "mushrooms", "sauerkraut", "ratatouille", "gazpacho", "salsa",
                "amaranth", "houttuynia", "ginger", "bamboo_shoot", "lotus_root", "edamame", "kernels", "canola", "douzhi")) return "VEGETABLE";
        if (word(path, "fruit", "fruits", "berry", "berries", "strawberry", "strawberries", "blueberry", "blueberries",
                "raspberry", "raspberries", "blackberry", "blackberries", "elderberry", "elderberries", "cherry", "cherries",
                "grape", "grapes", "banana", "orange", "lemon", "lime", "peach", "pear", "plum", "pineapple", "coconut",
                "watermelon", "melon", "pomegranate", "lucuma", "starfruit", "mango", "kiwi", "lychee", "date", "dates",
                "persimmon", "apricot", "fig", "figs", "spongekin")) return "APPLE";
        String tagged = taggedType(tags);
        return tagged == null ? "FOOD" : tagged;
    }

    private static String taggedType(Iterable<String> tags) {
        if (tags == null) return null;
        String selected = null;
        int priority = Integer.MAX_VALUE;
        for (String tag : tags) {
            if (tag == null || !tag.matches("(forge|c|diet|minecraft):[a-z0-9_./-]+")) continue;
            String path = tag.substring(tag.indexOf(':') + 1).replace('/', '_');
            String type = null;
            int rank = Integer.MAX_VALUE;
            if (word(path, "cheese", "cheeses", "milk", "dairy")) { type = "CHEESE"; rank = 0; }
            else if (word(path, "raw_fishes", "cooked_fishes", "raw_fish", "cooked_fish", "fish", "fishes", "seafood")) { type = "FISH"; rank = 1; }
            else if (word(path, "raw_meats", "cooked_meats", "raw_meat", "cooked_meat", "meat", "meats", "eggs")) { type = "MEAT"; rank = 2; }
            else if (word(path, "tea", "teas")) { type = "TEA"; rank = 3; }
            else if (word(path, "wine", "wines", "beer", "beers", "alcohol", "alcoholic")) { type = "WINE"; rank = 4; }
            else if (word(path, "fruit", "fruits", "berries")) { type = "APPLE"; rank = 5; }
            else if (word(path, "vegetable", "vegetables")) { type = "VEGETABLE"; rank = 6; }
            else if (word(path, "sweets", "candies", "desserts")) { type = "SWEET"; rank = 7; }
            if (rank < priority) { selected = type; priority = rank; }
        }
        return selected;
    }

    private static boolean word(String path, String... words) {
        String normalized = "_" + path.toLowerCase(Locale.ROOT).replace('/', '_').replace('-', '_').replace('.', '_') + "_";
        for (String word : words) if (normalized.contains("_" + word + "_")) return true;
        return false;
    }
}
