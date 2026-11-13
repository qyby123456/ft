package cn.farmingtales.qysevent;

import java.util.List;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;

public final class CoreFacingVerifier {
    private CoreFacingVerifier() {}

    public static void verify() {
        Direction[] directions = {Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST};
        Rotation[] rotations = {Rotation.NONE, Rotation.CLOCKWISE_90, Rotation.CLOCKWISE_180, Rotation.COUNTERCLOCKWISE_90};
        int assertions = 0;
        for (int directionIndex = 0; directionIndex < directions.length; directionIndex++) {
            for (int rotationIndex = 0; rotationIndex < rotations.length; rotationIndex++) {
                require(CoreFacingPolicy.rotate(directions[directionIndex], rotations[rotationIndex])
                        == directions[(directionIndex + rotationIndex) % directions.length], "Core rotation mismatch");
                assertions++;
            }
            for (Mirror mirror : Mirror.values()) {
                boolean flips = mirror == Mirror.LEFT_RIGHT && directionIndex % 2 == 0
                        || mirror == Mirror.FRONT_BACK && directionIndex % 2 == 1;
                require(CoreFacingPolicy.mirror(directions[directionIndex], mirror)
                        == directions[(directionIndex + (flips ? 2 : 0)) % directions.length], "Core mirror mismatch");
                assertions++;
            }
        }
        ResourceLocation core = new ResourceLocation("farmingtales", "wishing_well_core");
        require(CoreFacingPolicy.matches(core, true), "Core was excluded");
        require(!CoreFacingPolicy.matches(core, false), "Missing facing was accepted");
        require(!CoreFacingPolicy.matches(null, true), "Null key was accepted");
        assertions += 3;
        for (String id : List.of("minecraft:furnace", "minecraft:stone", "qys_event:oracle",
                "qys_event:weather_forecaster", "qys_event:wishing_well", "ftboss:oracle", "farmingtales:other")) {
            require(!CoreFacingPolicy.matches(new ResourceLocation(id), true), "Unrelated block affected: " + id);
            assertions++;
        }
        System.out.println("Core facing verified: " + assertions + " rotation, mirror and scope assertions.");
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message);
    }
}
