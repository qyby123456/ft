package com.farmingtales.drinkanim.math;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class DarkCuisineAnimationsTest {
    private static final DrinkProfile DISH=DrinkCatalog.find("kaleidoscope_cookery:dark_cuisine");
    private static final int USE_TICKS=32;

    @Test void reusesThePlacedDishAsAReversedFourStageHandheldModel() throws Exception {
        assertNotNull(DISH);
        assertEquals(DrinkProfile.Style.FOOD,DISH.style());
        assertTrue(PresentationOnly.active(DISH));
        assertEquals(3,PlateFoodAnimations.portions(DISH));
        assertEquals("animation/plates/dark_cuisine/level3",DISH.bodyModel());
        // The upstream inventory sprite stays exactly as the source mod ships it.
        assertNull(getClass().getResource("/assets/kaleidoscope_cookery/models/item/dark_cuisine.json"),
            "the original flat inventory icon must not be replaced");
        for(String model:PlateFoodAnimations.models(DISH))
            assertNotNull(getClass().getResource("/assets/farmingtales_drink_anim/models/"+model+".json"),model);
    }

    @Test void biteStagesRunFromFullDishToEmptyPlate() {
        assertEquals("animation/plates/dark_cuisine/level3",
            PlateFoodAnimations.bodyModel(DISH,BottleMotion.Action.DRINK,50,USE_TICKS,true));
        assertEquals("animation/plates/dark_cuisine/level2",
            PlateFoodAnimations.bodyModel(DISH,BottleMotion.Action.DRINK,60,USE_TICKS,true));
        assertEquals("animation/plates/dark_cuisine/level1",
            PlateFoodAnimations.bodyModel(DISH,BottleMotion.Action.DRINK,70,USE_TICKS,true));
        assertEquals("animation/plates/dark_cuisine/level0",
            PlateFoodAnimations.bodyModel(DISH,BottleMotion.Action.DRINK,80,USE_TICKS,true));
        assertEquals("animation/plates/dark_cuisine/level0",
            PlateFoodAnimations.bodyModel(DISH,BottleMotion.Action.DRINK,120,USE_TICKS,true));
        assertEquals("animation/plates/dark_cuisine/level3",
            PlateFoodAnimations.bodyModel(DISH,BottleMotion.Action.DRINK,135,USE_TICKS,true));
    }

    @Test void usesTheDishBiteCueAndKeepsTheSharedPresentationPath() {
        var cues=AnimationSoundTimeline.drinking(DISH,USE_TICKS);
        assertEquals(3,cues.stream().filter(c->c.event().equals("dark_cuisine_bite")).count());
        assertEquals(3,cues.stream().filter(c->c.event().equals("snack_swallow")).count());
        assertTrue(cues.stream().noneMatch(c->c.event().equals("sticky_rice_cake_bite")));
        assertEquals(BottleMotion.IDLE,DrinkMotion.sample(DISH,BottleMotion.Action.INSPECT,0,USE_TICKS,true));
        assertEquals(BottleMotion.IDLE,DrinkMotion.sample(DISH,BottleMotion.Action.INSPECT,104,USE_TICKS,true));
    }
}

