package net.farmingtales.ftdungeonmobs.generator;

import java.util.Random;

public final class ChaosCleanupSlicesTest {
    public static void main(String[] arguments) throws Exception {
        verify(new int[]{-17, -64, -17, 16, -40, 16});
        verify(new int[]{-16, 0, -16, -1, 7, -1});
        verify(new int[]{0, 319, 0, 0, 319, 0});
        Random random = new Random(0x46544348);
        for (int sample = 0; sample < 150; sample++) {
            int minimumX = random.nextInt(257) - 128;
            int minimumY = random.nextInt(180) - 64;
            int minimumZ = random.nextInt(257) - 128;
            verify(new int[]{minimumX, minimumY, minimumZ, minimumX + random.nextInt(40),
                    minimumY + random.nextInt(25), minimumZ + random.nextInt(40)});
        }
        expectInvalid(() -> ChaosCleanupSlices.slice(new int[]{0, 0, 0, 0, 0, 0}, -1));
        expectInvalid(() -> ChaosCleanupSlices.slice(new int[]{0, 0, 0, 0, 0, 0}, 1));
        expectInvalid(() -> ChaosCleanupSlices.count(new int[]{1, 0, 0, 0, 0, 0}));
        System.out.println("PASS ChaosCleanupSlices: 153 exhaustive boxes, exact coverage, no overlaps, chunk bounds, 2048 cap, invalid cursors");
        if (arguments.length > 0) { verifyProduction(java.nio.file.Path.of(arguments[0])); }
    }

    private static void verifyProduction(java.nio.file.Path path) throws Exception {
        String contents = java.nio.file.Files.readString(path);
        var matcher = java.util.regex.Pattern.compile("\\[(-?\\d+,){5}-?\\d+\\]").matcher(contents);
        long blocks = 0;
        int boxes = 0;
        int slices = 0;
        while (matcher.find()) {
            String raw = matcher.group();
            int[] box = java.util.Arrays.stream(raw.substring(1, raw.length() - 1).split(",")).mapToInt(Integer::parseInt).toArray();
            long expected = (long) (box[3] - box[0] + 1) * (box[4] - box[1] + 1) * (box[5] - box[2] + 1);
            long actual = 0;
            int count = ChaosCleanupSlices.count(box);
            for (int index = 0; index < count; index++) {
                int[] slice = ChaosCleanupSlices.slice(box, index);
                long volume = (long) (slice[3] - slice[0] + 1) * (slice[4] - slice[1] + 1) * (slice[5] - slice[2] + 1);
                require(volume > 0 && volume <= 2048, "Production slice volume");
                require((slice[0] >> 4) == (slice[3] >> 4) && (slice[2] >> 4) == (slice[5] >> 4), "Production cross-chunk slice");
                actual += volume;
            }
            require(actual == expected, "Production volume mismatch");
            blocks += actual;
            slices += count;
            boxes++;
        }
        require(boxes == 12824, "Expected all four deployed cleanup layouts");
        System.out.println("PASS production cleanup layouts: boxes=" + boxes + " slices=" + slices + " preserved_blocks=" + blocks);
    }

    private static void verify(int[] box) {
        int width = box[3] - box[0] + 1;
        int height = box[4] - box[1] + 1;
        int depth = box[5] - box[2] + 1;
        boolean[] covered = new boolean[width * height * depth];
        int visited = 0;
        for (int index = 0; index < ChaosCleanupSlices.count(box); index++) {
            int[] slice = ChaosCleanupSlices.slice(box, index);
            require((slice[0] >> 4) == (slice[3] >> 4) && (slice[2] >> 4) == (slice[5] >> 4), "Cross-chunk slice");
            require((slice[3] - slice[0] + 1) * (slice[4] - slice[1] + 1) * (slice[5] - slice[2] + 1) <= 2048, "Oversized slice");
            for (int blockX = slice[0]; blockX <= slice[3]; blockX++) {
                for (int blockY = slice[1]; blockY <= slice[4]; blockY++) {
                    for (int blockZ = slice[2]; blockZ <= slice[5]; blockZ++) {
                        require(blockX >= box[0] && blockX <= box[3] && blockY >= box[1] && blockY <= box[4]
                                && blockZ >= box[2] && blockZ <= box[5], "Out of bounds");
                        int cell = ((blockX - box[0]) * height + blockY - box[1]) * depth + blockZ - box[2];
                        require(!covered[cell], "Overlapping slice");
                        covered[cell] = true;
                        visited++;
                    }
                }
            }
        }
        require(visited == covered.length, "Missing cells");
    }

    private static void expectInvalid(Runnable action) {
        try { action.run(); throw new AssertionError("Invalid cleanup input accepted"); }
        catch (IllegalArgumentException expected) {}
    }

    private static void require(boolean condition, String message) {
        if (!condition) { throw new AssertionError(message); }
    }
}
