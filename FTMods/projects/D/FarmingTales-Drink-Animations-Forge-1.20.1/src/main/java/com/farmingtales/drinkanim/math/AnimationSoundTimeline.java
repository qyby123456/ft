package com.farmingtales.drinkanim.math;

import java.util.List;

public final class AnimationSoundTimeline {
    public record Cue(int tick, String event, float volume, float pitch) {}

    private static final List<Cue> INSPECTION = List.of(
        new Cue(6, "inspect_lift", .40f, 1.0f),
        new Cue(16, "inspect_support", .34f, 1.02f),
        new Cue(22, "inspect_turn", .40f, 1.04f),
        new Cue(24, "inspect_liquid", .30f, 1.04f),
        new Cue(29, "inspect_settle", .16f, .96f),
        new Cue(38, "inspect_turn", .38f, 1.02f),
        new Cue(40, "inspect_liquid", .26f, 1.08f),
        new Cue(45, "inspect_settle", .14f, 1.03f),
        new Cue(54, "inspect_turn", .42f, 1.06f),
        new Cue(56, "inspect_liquid", .28f, 1.02f),
        new Cue(61, "inspect_settle", .17f, .99f),
        new Cue(70, "inspect_turn", .34f, 1.02f),
        new Cue(72, "inspect_liquid", .22f, 1.06f),
        new Cue(77, "inspect_settle", .12f, .94f),
        new Cue(88, "inspect_return", .32f, .98f)
    );

    public static List<Cue> inspection() { return INSPECTION; }
    private static final List<Cue> BEVERAGE_INSPECTION=List.of(
        new Cue(6,"inspect_lift",.40f,1),new Cue(16,"inspect_support",.34f,1.02f),
        new Cue(36,"inspect_turn",.30f,1.04f),new Cue(39,"inspect_liquid",.18f,1.05f),
        new Cue(47,"inspect_settle",.10f,1.03f),new Cue(63,"inspect_turn",.26f,1.06f),
        new Cue(66,"inspect_liquid",.16f,1.02f),new Cue(76,"inspect_settle",.09f,.98f),
        new Cue(89,"inspect_return",.30f,.98f));
    public static List<Cue> inspection(DrinkProfile profile) {
        if(BambooEggAnimations.owns(profile))return BambooEggAnimations.inspectionSounds();
        if(StickyRiceCakeAnimations.owns(profile))return StickyRiceCakeAnimations.inspectionSounds();
        if(PlateFoodAnimations.owns(profile))return PlateFoodAnimations.inspectionSounds();
        if(LandNo1BottleAnimations.owns(profile))return LandNo1BottleAnimations.inspectionSounds();
        if(CoconutAnimations.owns(profile))return List.of(
            new Cue(6,"inspect_lift",.36f,1),new Cue(18,"inspect_support",.24f,1.02f),
            new Cue(32,"inspect_turn",.24f,1.02f),new Cue(44,"inspect_liquid",.14f,1.04f),
            new Cue(70,"inspect_turn",.20f,1.02f),new Cue(83,"inspect_return",.28f,.98f));
        if(ServingAnimations.owns(profile))return List.of(
            new Cue(6,"inspect_lift",.36f,1),new Cue(18,"inspect_support",.22f,1.03f),
            new Cue(26,"inspect_liquid",.14f,1.05f),new Cue(43,"inspect_turn",.18f,1.02f),
            new Cue(68,"inspect_liquid",.12f,1.0f),new Cue(70,"inspect_settle",.10f,1.05f),
            new Cue(78,"inspect_turn",.22f,1.03f),new Cue(92,"inspect_return",.28f,.98f));
        if(profile.jam())return JamAnimations.inspectionSounds();
        if(CupAnimations.coffeePreview(profile))return List.of(
            new Cue(6,"inspect_lift",.36f,1),new Cue(26,"inspect_turn",.25f,1.04f),
            new Cue(35,"inspect_liquid",.16f,1.05f),new Cue(58,"inspect_settle",.09f,1.03f),
            new Cue(82,"inspect_return",.30f,.98f));
        if(ContainerAnimations.owns(profile))return ContainerAnimations.inspectionSounds(profile);
        return profile.wine()?INSPECTION:BEVERAGE_INSPECTION;
    }

    public static List<Cue> drinking(DrinkProfile profile,int useTicks) {
        if(BambooEggAnimations.owns(profile))return BambooEggAnimations.eatingSounds();
        if(StickyRiceCakeAnimations.owns(profile))return StickyRiceCakeAnimations.eatingSounds(useTicks);
        if(PlateFoodAnimations.owns(profile))return PlateFoodAnimations.eatingSounds(profile,useTicks);
        if(ServingAnimations.owns(profile))return ServingAnimations.sounds(profile,useTicks);
        if(!profile.jam())return drinking(useTicks);
        var cues=JamAnimations.eatingSounds(useTicks);
        if(!profile.sweetJam())return cues;
        return cues.stream().map(c->c.event().equals("jam_eat")
            ?new Cue(c.tick(),c.event(),c.volume()*1.35f,c.pitch()):c).toList();
    }
    public static List<Cue> drinking(int useTicks) {
        int first = BottleMotion.RAISE_TICKS + 1;
        int span = BottleMotion.sipTicks(useTicks) - 10;
        // The unchanged vanilla sample is 0.487 seconds. A small pitch lift
        // leaves a gap between all four sips, including the shortest drink.
        return List.of(
            new Cue(first, "gulp", .50f, 1.12f),
            new Cue(first + Math.round(span / 3f), "gulp", .54f, 1.10f),
            new Cue(first + Math.round(2 * span / 3f), "gulp", .50f, 1.14f),
            new Cue(first + span, "gulp", .46f, 1.10f)
        );
    }

    private AnimationSoundTimeline() {}
}
