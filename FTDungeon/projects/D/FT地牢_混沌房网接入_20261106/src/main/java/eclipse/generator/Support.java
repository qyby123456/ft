package eclipse.generator;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Rotation;

final class MineCells {
    static ResourceLocation createId(String path) { return new ResourceLocation("minecells", path); }
}

final class SpecialPointIds {
    static final String ENTRANCE = "entrance";
    static final String EXIT = "exit";
}

final class GridPiecesGenerator {
    static final class RoomData {
        Vec3i pos;
        ResourceLocation poolId;
        Rotation rotation = Rotation.NONE;
        Vec3i offset = Vec3i.ZERO;
        String special;
        Vec3i specialOffset;
        Rotation specialFacing;

        RoomData(Vec3i position, ResourceLocation pool) { pos = position; poolId = pool; }
        RoomData rotation(Rotation value) { rotation = value; return this; }
        RoomData offset(int positionX, int positionY, int positionZ) {
            offset = new Vec3i(positionX, positionY, positionZ);
            return this;
        }
        RoomData specialPoint(String identifier, Vec3i position, Rotation facing) {
            special = identifier; specialOffset = position; specialFacing = facing; return this;
        }
    }
}

abstract class MultipartGridGenerator {
    final List<GridPiecesGenerator.RoomData> rooms = new ArrayList<>();
    MultipartGridGenerator(int xPart, int zPart) {}
    protected abstract void addRooms(RandomSource random);
    public abstract int getVersion();
    protected static GridPiecesGenerator.RoomData room(Vec3i position, ResourceLocation pool) {
        return new GridPiecesGenerator.RoomData(position, pool);
    }
    protected void addRoom(GridPiecesGenerator.RoomData value) {
        GridPiecesGenerator.RoomData copy = new GridPiecesGenerator.RoomData(
            new Vec3i(value.pos.getX(), value.pos.getY(), value.pos.getZ()), value.poolId);
        copy.rotation = value.rotation;
        copy.offset = value.offset;
        copy.special = value.special;
        copy.specialOffset = value.specialOffset;
        copy.specialFacing = value.specialFacing;
        rooms.add(copy);
    }
}
