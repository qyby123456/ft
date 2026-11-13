package com.farmingtales.drinkanim.math;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AnimationSoundTimelineTest {
    @Test void fourCompleteSipsFitEverySupportedUseDuration() {
        for(int duration=1;duration<=200;duration++) {
            var cues=AnimationSoundTimeline.drinking(duration);
            assertEquals(4,cues.size());
            double precedingEnd=BottleMotion.RAISE_TICKS;
            for(var cue:cues) {
                assertEquals("gulp",cue.event());
                assertTrue(cue.tick()>precedingEnd,"No overlapping or cut off drink samples");
                precedingEnd=cue.tick()+20*.486531/cue.pitch();
            }
            assertTrue(precedingEnd<BottleMotion.RAISE_TICKS+BottleMotion.sipTicks(duration),
                "Last sound must finish while the bottle is still raised");
        }
    }

    @Test void inspectionFoleyDoesNotOpenOrDrinkTheBottle() {
        var cues=AnimationSoundTimeline.inspection();
        assertEquals(4,cues.stream().filter(c->c.event().equals("inspect_turn")).count());
        assertEquals(4,cues.stream().filter(c->c.event().equals("inspect_settle")).count());
        assertTrue(cues.stream().allMatch(c->c.event().startsWith("inspect_")&&c.tick()>0
            &&c.tick()<BottleMotion.INSPECT_TICKS&&c.volume()>0&&c.volume()<=.5f));
        for(int i=1;i<cues.size();i++)assertTrue(cues.get(i).tick()>cues.get(i-1).tick());
    }
}
