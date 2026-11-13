package eclipse.generator;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
public class Vec3iCursor
extends BlockPos.MutableBlockPos {
    private Direction direction = Direction.SOUTH;

    public Vec3iCursor(Vec3i pos, Direction direction) {
        super(pos.getX(), pos.getY(), pos.getZ());
        this.direction = direction;
    }

    public Vec3iCursor(Vec3i pos) {
        super(pos.getX(), pos.getY(), pos.getZ());
    }

    public Vec3iCursor split() {
        return new Vec3iCursor((Vec3i)this, this.direction);
    }

    public Vec3iCursor turnLeft() {
        this.direction = this.direction.getCounterClockWise();
        return this;
    }

    public Vec3iCursor turnRight() {
        this.direction = this.direction.getClockWise();
        return this;
    }

    public Vec3iCursor turn(Rotation rotation) {
        this.direction = rotation.rotate(this.direction);
        return this;
    }

    public Vec3iCursor stepLeft() {
        this.move(this.direction.getCounterClockWise());
        return this;
    }

    public Vec3iCursor stepRight() {
        this.move(this.direction.getClockWise());
        return this;
    }

    public Vec3iCursor forward() {
        this.move(this.direction);
        return this;
    }

    public Vec3iCursor back() {
        this.move(this.direction.getOpposite());
        return this;
    }

    public Vec3iCursor up() {
        this.move(0, 1, 0);
        return this;
    }

    public Vec3iCursor down() {
        this.move(0, -1, 0);
        return this;
    }

    public Rotation getRotation() {
        return switch (this.direction) {
            case SOUTH -> Rotation.CLOCKWISE_180;
            case WEST -> Rotation.COUNTERCLOCKWISE_90;
            case EAST -> Rotation.CLOCKWISE_90;
            default -> Rotation.NONE;
        };
    }
}
