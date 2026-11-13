package com.farmingtales.drinkanim.math;

/**
 * Held items whose hotkey action must only perform: the world, not the item, owns the
 * servings. `DrinkController` therefore never calls `useItem` for them, which keeps a
 * placeable dish from being placed in the middle of its own animation.
 */
public final class PresentationOnly {
    public static boolean active(DrinkProfile profile) {
        return LandNo1BottleAnimations.owns(profile)||PlateFoodAnimations.owns(profile);
    }
    private PresentationOnly() {}
}
