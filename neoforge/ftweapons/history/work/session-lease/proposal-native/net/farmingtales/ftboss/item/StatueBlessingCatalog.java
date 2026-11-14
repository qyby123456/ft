package net.farmingtales.ftboss.item;

import java.util.List;

public final class StatueBlessingCatalog {
    private StatueBlessingCatalog() {
    }

    public static List<StatueBlessingCatalog.Definition> pool(String profile) {
        return List.of();
    }

    public static StatueBlessingCatalog.Definition byId(String identity) {
        return null;
    }

    public static List<StatueBlessingCatalog.Definition> all() {
        return List.of();
    }

    static boolean isAdded(String identity) {
        return false;
    }

    public static record Definition(String id, String profile, String nameZh, String nameEn, String descriptionZh, String descriptionEn, String inspiration) {
    }
}
