/*
 * Decompiled with CFR 0.152.
 */
package net.farmingtales.ftboss.util;

import java.util.Locale;

public final class FoodSocketFoodRules {
    private FoodSocketFoodRules() {
    }

    public static String legacyType(String string) {
        if (string == null || !string.matches("[a-z0-9_.-]+:[a-z0-9_./-]+")) {
            return null;
        }
        String string2 = string.substring(string.indexOf(58) + 1);
        if (FoodSocketFoodRules.word(string2, "cheese", "cheesy", "fromage")) {
            return "CHEESE";
        }
        if (FoodSocketFoodRules.word(string2, "long_island_iced_tea", "wine", "beer", "ale", "mead", "cider", "liquor", "vodka", "whiskey", "whisky", "brew", "sake", "rum", "stout", "grog", "champagne", "burgundy", "brandy", "bourbon", "absinthe", "tequila", "sangria", "cocktail", "lager", "kvass", "gin_tonic")) {
            return "WINE";
        }
        if (FoodSocketFoodRules.word(string2, "tea", "matcha", "oolong", "kombucha", "herbal_infusion")) {
            return "TEA";
        }
        if (FoodSocketFoodRules.word(string2, "apple", "applesauce")) {
            return "APPLE";
        }
        if (FoodSocketFoodRules.word(string2, "fish", "pufferfish", "salmon", "cod", "puffer", "tuna", "shrimp", "prawn", "crab", "lobster", "seafood", "squid", "bass", "clam", "mussel", "oyster", "eel", "lamprey", "roe", "kipper", "anchovy", "trout", "tilapia", "octopus", "sashimi", "nigiri", "futomaki", "kabayaki")) {
            return "FISH";
        }
        if (FoodSocketFoodRules.word(string2, "beef", "pork", "chicken", "mutton", "lamb", "rabbit", "meat", "turkey", "sausage", "ham", "bacon", "steak", "venison", "boar", "duck", "goose", "veal", "jerky", "offal", "meatball", "chop", "patty")) {
            return "MEAT";
        }
        if (FoodSocketFoodRules.word(string2, "coffee", "cocoa", "chili", "pepper", "spicy", "energy", "espresso", "latte", "cappuccino", "mocha", "cola") || string.equals("minecraft:redstone")) {
            return "ENERGY";
        }
        if (FoodSocketFoodRules.word(string2, "carrot", "potato", "beet", "beetroot", "cabbage", "tomato", "onion", "corn", "maize", "salad", "vegetable", "compost", "mushroom", "pumpkin", "leek", "lettuce", "turnip", "radish", "cucumber", "pickle", "pickles", "soybean", "tofu", "asparagus", "eggplant", "zucchini", "spinach", "broccoli", "pea", "peas", "bean", "kelp", "seaweed", "artichoke", "cauliflower")) {
            return "VEGETABLE";
        }
        if (FoodSocketFoodRules.word(string2, "sugar", "candy", "cake", "cookie", "honey", "chocolate", "sweet", "donut", "pie", "marshmallow", "custard", "gummy", "cupcake", "tart", "pastry", "croissant", "biscuit", "dango", "mochi", "popsicle", "ice_cream", "pudding", "jam", "jelly", "marmalade", "candied", "fudge", "truffle", "macaron")) {
            return "SWEET";
        }
        return null;
    }

    public static String type(String string, boolean bl, Iterable<String> iterable) {
        String string2 = FoodSocketFoodRules.legacyType(string);
        if (string2 != null) {
            return string2;
        }
        if (!bl || string == null || !string.matches("[a-z0-9_.-]+:[a-z0-9_./-]+")) {
            return null;
        }
        String string3 = string.substring(string.indexOf(58) + 1);
        if (FoodSocketFoodRules.word(string3, "cheesecake", "fondue", "cheddar", "gouda", "ricotta", "mozzarella", "gorgonzola", "raclette")) {
            return "CHEESE";
        }
        if (FoodSocketFoodRules.word(string3, "liqueur", "wheatbeer", "butterbeer", "eiswein", "daiginjo", "sherry", "maotai", "dassai", "carignan", "riesling", "sauvignon", "gin", "margarita", "pina_colada", "kwas", "strongbow")) {
            return "WINE";
        }
        if (FoodSocketFoodRules.word(string3, "rooibos", "yerba_mate", "chamomile", "hibiscus_infusion")) {
            return "TEA";
        }
        if (FoodSocketFoodRules.word(string3, "frappuccino", "mozhao", "mozhao_p", "vitamin_drink", "guarana")) {
            return "ENERGY";
        }
        if (FoodSocketFoodRules.word(string3, "catfish", "bluefish", "herring", "sardine", "mackerel", "croaker", "uni", "urchin", "surimi", "calamari", "ceviche", "moqueca", "california_roll", "buddha_jumps_over_the_wall")) {
            return "FISH";
        }
        if (FoodSocketFoodRules.word(string3, "bushmeat", "boarchop", "meatballs", "ribs", "spare_ribs", "bunny", "donkey", "dodo", "lizard_tail", "frog", "froglegs", "frog_leg", "mid_wing", "yakitori", "tartare", "goulash", "kabobs", "kebab", "egg", "eggs", "omelet", "omelette", "mayonnaise")) {
            return "MEAT";
        }
        if (FoodSocketFoodRules.word(string3, "brownie", "waffle", "pancake", "pancakes", "mousse", "tiramisu", "cream_puff", "scone", "scones", "taosu", "paofu", "panettone", "mooncake", "icebergcream", "icecream", "taffy", "caramel", "syrup", "sundae", "fondant", "mizuyokan", "daifuku", "yokan", "croissants", "chocolates")) {
            return "SWEET";
        }
        if (FoodSocketFoodRules.word(string3, "milk", "milkshake", "yogurt", "yoghurt", "butter", "cream", "kefir")) {
            return "CHEESE";
        }
        if (FoodSocketFoodRules.word(string3, "potatoes", "tomatoes", "vegetables", "mushrooms", "sauerkraut", "ratatouille", "gazpacho", "salsa", "amaranth", "houttuynia", "ginger", "bamboo_shoot", "lotus_root", "edamame", "kernels", "canola", "douzhi")) {
            return "VEGETABLE";
        }
        if (FoodSocketFoodRules.word(string3, "fruit", "fruits", "berry", "berries", "strawberry", "strawberries", "blueberry", "blueberries", "raspberry", "raspberries", "blackberry", "blackberries", "elderberry", "elderberries", "cherry", "cherries", "grape", "grapes", "banana", "orange", "lemon", "lime", "peach", "pear", "plum", "pineapple", "coconut", "watermelon", "melon", "pomegranate", "lucuma", "starfruit", "mango", "kiwi", "lychee", "date", "dates", "persimmon", "apricot", "fig", "figs", "spongekin")) {
            return "APPLE";
        }
        String string4 = FoodSocketFoodRules.taggedType(iterable);
        return string4 == null ? "FOOD" : string4;
    }

    private static String taggedType(Iterable<String> iterable) {
        if (iterable == null) {
            return null;
        }
        String string = null;
        int n = Integer.MAX_VALUE;
        for (String string2 : iterable) {
            if (string2 == null || !string2.matches("(forge|c|diet|minecraft):[a-z0-9_./-]+")) continue;
            String string3 = string2.substring(string2.indexOf(58) + 1).replace('/', '_');
            String string4 = null;
            int n2 = Integer.MAX_VALUE;
            if (FoodSocketFoodRules.word(string3, "cheese", "cheeses", "milk", "dairy")) {
                string4 = "CHEESE";
                n2 = 0;
            } else if (FoodSocketFoodRules.word(string3, "raw_fishes", "cooked_fishes", "raw_fish", "cooked_fish", "fish", "fishes", "seafood")) {
                string4 = "FISH";
                n2 = 1;
            } else if (FoodSocketFoodRules.word(string3, "raw_meats", "cooked_meats", "raw_meat", "cooked_meat", "meat", "meats", "eggs")) {
                string4 = "MEAT";
                n2 = 2;
            } else if (FoodSocketFoodRules.word(string3, "tea", "teas")) {
                string4 = "TEA";
                n2 = 3;
            } else if (FoodSocketFoodRules.word(string3, "wine", "wines", "beer", "beers", "alcohol", "alcoholic")) {
                string4 = "WINE";
                n2 = 4;
            } else if (FoodSocketFoodRules.word(string3, "fruit", "fruits", "berries")) {
                string4 = "APPLE";
                n2 = 5;
            } else if (FoodSocketFoodRules.word(string3, "vegetable", "vegetables")) {
                string4 = "VEGETABLE";
                n2 = 6;
            } else if (FoodSocketFoodRules.word(string3, "sweets", "candies", "desserts")) {
                string4 = "SWEET";
                n2 = 7;
            }
            if (n2 >= n) continue;
            string = string4;
            n = n2;
        }
        return string;
    }

    private static boolean word(String string, String ... stringArray) {
        String string2 = "_" + string.toLowerCase(Locale.ROOT).replace('/', '_').replace('-', '_').replace('.', '_') + "_";
        for (String string3 : stringArray) {
            if (!string2.contains("_" + string3 + "_")) continue;
            return true;
        }
        return false;
    }
}

