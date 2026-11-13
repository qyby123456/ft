package net.farmingtales.chunkdrops;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

final class DropReport {
    private static final DateTimeFormatter TIMESTAMP = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss")
            .withZone(ZoneOffset.UTC);

    static Path write(DropScanner.Snapshot snapshot, Path directory) throws IOException {
        Files.createDirectories(directory);
        Path file = Files.createTempFile(directory, "chunkdrops-" + TIMESTAMP.format(Instant.now()) + "-", ".csv");
        try (BufferedWriter writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
            writer.write('\ufeff');
            writer.write("dimension,chunk_x,chunk_z,entity_uuid,item_id,item_count,x,y,z\n");
            for (DropScanner.ChunkDrops chunk : snapshot.chunks()) {
                for (DropScanner.Drop drop : chunk.drops()) {
                    writer.write(String.join(",", chunk.key().dimension(), Integer.toString(chunk.key().x()),
                            Integer.toString(chunk.key().z()), drop.uuid().toString(), drop.itemId(),
                            Integer.toString(drop.count()), Double.toString(drop.x()), Double.toString(drop.y()),
                            Double.toString(drop.z())));
                    writer.newLine();
                }
            }
        }
        return file;
    }
}
