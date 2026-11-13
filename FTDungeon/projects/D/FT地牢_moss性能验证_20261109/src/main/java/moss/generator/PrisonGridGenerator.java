package moss.generator;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
public class PrisonGridGenerator
extends MultipartGridGenerator {
    private static final ResourceLocation SPAWN = PrisonGridGenerator.id("spawn/spawn");
    private static final ResourceLocation SPAWN_OUTSIDE_NEAR = PrisonGridGenerator.id("spawn/outside_near");
    private static final ResourceLocation SPAWN_OUTSIDE_FAR = PrisonGridGenerator.id("spawn/outside_far");
    private static final ResourceLocation STAIRS = PrisonGridGenerator.id("stairs");
    private static final ResourceLocation STRAIGHT = PrisonGridGenerator.id("straight");
    private static final ResourceLocation TURN = PrisonGridGenerator.id("turn");
    private static final ResourceLocation CURVE = PrisonGridGenerator.id("curve");
    private static final ResourceLocation TERMINAL = PrisonGridGenerator.id("terminal");
    private static final ResourceLocation PROMENADE = PrisonGridGenerator.id("promenade");

    public PrisonGridGenerator(int xPart, int zPart) {
        super(xPart, zPart);
    }

    @Override
    protected void addRooms(RandomSource random) {
        Vec3iCursor cursor = new Vec3iCursor(new Vec3i(32, 15, 32), Direction.SOUTH);
        Vec3iCursor spawnCursor = cursor.split();
        this.addRoom(PrisonGridGenerator.room((Vec3i)spawnCursor, SPAWN).offset(0, -2, 0).specialPoint(SpecialPointIds.ENTRANCE, new Vec3i(12, 1, 1), Rotation.NONE));
        this.addRoom(PrisonGridGenerator.room((Vec3i)spawnCursor.stepLeft(), SPAWN_OUTSIDE_NEAR).offset(0, -2, 0));
        this.addRoom(PrisonGridGenerator.room((Vec3i)spawnCursor.stepLeft(), SPAWN_OUTSIDE_FAR).offset(0, -2, 0));
        this.addFloor(random, cursor, 3);
    }

    private Vec3iCursor addCorridor(RandomSource random, Vec3iCursor cursor, boolean allowTurns, ResourceLocation end, int length) {
        for (int i = 0; i < length; ++i) {
            if (random.nextFloat() < 0.2f && allowTurns && i < length - 1 && i > 0) {
                boolean left = random.nextBoolean();
                if (left) {
                    this.addRoom(PrisonGridGenerator.room((Vec3i)cursor.forward(), CURVE).rotation(cursor.getRotation().getRotated(Rotation.COUNTERCLOCKWISE_90)));
                    for (int j = 0; j < random.nextInt(3); ++j) {
                        this.addRoom(PrisonGridGenerator.room((Vec3i)cursor.stepLeft(), STRAIGHT).rotation(cursor.getRotation().getRotated(Rotation.CLOCKWISE_90)));
                    }
                    this.addRoom(PrisonGridGenerator.room((Vec3i)cursor.stepLeft(), CURVE).rotation(cursor.getRotation().getRotated(Rotation.CLOCKWISE_90)));
                    continue;
                }
                this.addRoom(PrisonGridGenerator.room((Vec3i)cursor.forward(), CURVE).rotation(cursor.getRotation().getRotated(Rotation.CLOCKWISE_180)));
                for (int j = 0; j < random.nextInt(3); ++j) {
                    this.addRoom(PrisonGridGenerator.room((Vec3i)cursor.stepRight(), STRAIGHT).rotation(cursor.getRotation().getRotated(Rotation.COUNTERCLOCKWISE_90)));
                }
                this.addRoom(PrisonGridGenerator.room((Vec3i)cursor.stepRight(), CURVE).rotation(cursor.getRotation().getRotated(Rotation.NONE)));
                continue;
            }
            boolean turn = allowTurns && random.nextBoolean();
            Rotation rotation = random.nextBoolean() ? Rotation.NONE : Rotation.CLOCKWISE_180;
            this.addRoom(PrisonGridGenerator.room((Vec3i)cursor.forward(), turn ? TURN : STRAIGHT).rotation(rotation.getRotated(cursor.getRotation())));
            if (!turn) continue;
            this.addCorridor(random, cursor.split().turn(rotation).turnRight(), false, TERMINAL, random.nextIntBetweenInclusive(2, 4));
        }
        if (end != null) {
            GridPiecesGenerator.RoomData endRoom = PrisonGridGenerator.room((Vec3i)cursor.forward(), end).rotation(cursor.getRotation().getRotated(Rotation.CLOCKWISE_180));
            if (end == PROMENADE) {
                endRoom.specialPoint(SpecialPointIds.EXIT, new Vec3i(9, 0, 14), Rotation.CLOCKWISE_180);
            }
            this.addRoom(endRoom);
        }
        return cursor.split();
    }

    private void addFloor(RandomSource random, Vec3iCursor cursor, int index) {
        Vec3iCursor endCursor = this.addCorridor(random, cursor, true, null, random.nextIntBetweenInclusive(3, 5));
        endCursor.forward();
        if (index > 0) {
            Vec3iCursor newCursor = endCursor.split();
            this.addRoom(PrisonGridGenerator.room((Vec3i)newCursor.down(), STAIRS));
            this.addCorridor(random, newCursor.split(), true, TERMINAL, random.nextIntBetweenInclusive(0, 2));
            this.addFloor(random, newCursor.turn(Rotation.CLOCKWISE_180), index - 1);
            this.addCorridor(random, endCursor, true, TERMINAL, random.nextIntBetweenInclusive(0, 2));
        } else {
            this.addCorridor(random, endCursor.back(), true, PROMENADE, random.nextIntBetweenInclusive(0, 2));
        }
    }

    @Override
    public int getVersion() {
        return 1;
    }

    private static ResourceLocation id(String path) {
        return MineCells.createId("better_prison/" + path);
    }
}
