package net.farmingtales.ftboss.api;

import net.farmingtales.ftboss.util.foundations.FoundationCatalogueCopies;
import net.minecraft.nbt.CompoundTag;

public final class SwordMaterialExtensions {
    private static final class FoodCatalogueRules {
        private static final CompoundTag SNAPSHOT = null;
    }

    private static CompoundTag foodCatalogueSnapshot() {
        return FoundationCatalogueCopies.copyCatalogue(FoodCatalogueRules.SNAPSHOT);
    }
}
