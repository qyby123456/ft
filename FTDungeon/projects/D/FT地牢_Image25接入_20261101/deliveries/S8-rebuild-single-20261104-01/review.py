import json
import sys

from deliver import ROOT, SHARED, ROOM, ROOM_ID, module, save


def native():
    runner = module('native_runner', SHARED / 'run_native_review.py')
    original = (SHARED / 'VerifyDeliveries.java').read_text(encoding='utf-8')
    old_ids = '"L1", "L2", "M1", "M2", "S1", "S2", "L3", "L4", "S3"'
    assert old_ids in original
    source = original.replace(old_ids, json.dumps(ROOM_ID))
    line = '        int reachable = checkPath(stateMap, size, coordinate(metadata, "entrance"), coordinate(metadata, "interior"));'
    assert line in source
    source = source.replace(line, line + '\n        for (com.google.gson.JsonElement element : metadata.getAsJsonArray("route_targets")) {\n            checkPath(stateMap, size, coordinate(metadata, "entrance"), coordinate(element.getAsJsonObject(), "position"));\n        }')
    source = source.replace('BlockPos feet = entrance.offset(offset, 0, 0);', 'boolean side = entrance.getX() == 0 || entrance.getX() == size[0] - 1;\n            BlockPos feet = entrance.offset(side ? 0 : offset, 0, side ? offset : 0);')
    rotation_code = '''        List<Map<String, Object>> rotations = new ArrayList<>();
        Map<BlockPos, BlockState> rotatedStates = new LinkedHashMap<>(stateMap);
        int[] rotatedSize = size.clone();
        BlockPos rotatedEntrance = coordinate(metadata, "entrance");
        List<BlockPos> rotatedTargets = new ArrayList<>();
        rotatedTargets.add(coordinate(metadata, "interior"));
        for (com.google.gson.JsonElement element : metadata.getAsJsonArray("route_targets")) {
            rotatedTargets.add(coordinate(element.getAsJsonObject(), "position"));
        }
        for (int turn = 0; turn < 4; turn++) {
            for (BlockPos target : rotatedTargets) {
                checkPath(rotatedStates, rotatedSize, rotatedEntrance, target);
            }
            rotations.add(Map.of("degrees_clockwise", turn * 90, "status", "PASS", "checked_targets", rotatedTargets.size(), "entrance", List.of(rotatedEntrance.getX(), rotatedEntrance.getY(), rotatedEntrance.getZ())));
            Map<BlockPos, BlockState> nextStates = new LinkedHashMap<>();
            for (Map.Entry<BlockPos, BlockState> entry : rotatedStates.entrySet()) {
                BlockPos position = entry.getKey();
                nextStates.put(new BlockPos(rotatedSize[2] - 1 - position.getZ(), position.getY(), position.getX()), entry.getValue().rotate(net.minecraft.world.level.block.Rotation.CLOCKWISE_90));
            }
            List<BlockPos> nextTargets = new ArrayList<>();
            for (BlockPos target : rotatedTargets) {
                nextTargets.add(new BlockPos(rotatedSize[2] - 1 - target.getZ(), target.getY(), target.getX()));
            }
            rotatedEntrance = new BlockPos(rotatedSize[2] - 1 - rotatedEntrance.getZ(), rotatedEntrance.getY(), rotatedEntrance.getX());
            rotatedStates = nextStates;
            rotatedTargets = nextTargets;
            rotatedSize = new int[]{rotatedSize[2], rotatedSize[1], rotatedSize[0]};
        }
'''
    source = source.replace('        StructureTemplate template = new StructureTemplate();', rotation_code + '        StructureTemplate template = new StructureTemplate();')
    source = source.replace('        report.put("sha256", actualHash);', '        report.put("sha256", actualHash);\n        report.put("rotated_anchors_states_and_path_checks", rotations);\n        report.put("world_placement_and_400tick_stability", "NOT_RUN");')
    (ROOT / 'tools').mkdir(exist_ok=True)
    (ROOT / 'tools/VerifyDeliveries.java').write_text(source, encoding='utf-8')
    runner.ROOT = ROOT
    sys.argv = ['native', '--root', str(ROOT)]
    runner.main()


def render():
    from PIL import Image
    renderer = module('fourview', SHARED / 'review_four_views.py')
    renderer.REVIEW = ROOT / 'views'
    sys.argv = ['render', '--rooms', ROOM_ID, '--revision', 'single-01-r2', '--source-root', str(ROOT / 'incoming')]
    renderer.main()
    with Image.open(ROOT / 'views/single-01-r2' / ROOM_ID / 'interior-north.png') as image:
        image.save(ROOM / 'preview.png')


def assembly():
    from finalize_takeover import assembly as generate_assembly
    generate_assembly()



if __name__ == '__main__':
    {'native': native, 'render': render, 'assembly': assembly}[sys.argv[1]]()
