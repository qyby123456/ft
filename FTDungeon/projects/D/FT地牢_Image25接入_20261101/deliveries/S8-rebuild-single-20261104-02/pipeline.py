import collections
import hashlib
import importlib.util
import json
import os
from pathlib import Path
import shutil
import subprocess
import sys

from PIL import Image, ImageDraw, ImageFont

ROOT = Path(__file__).resolve().parent
ROOM = ROOT / 'incoming/S8R_IMG02'
PREVIOUS = ROOT.parent / 'S8-rebuild-single-20261104-01'
AUDIT = Path('D:/ft工程文件/FT地牢_独立复核_20261104_01a1a685')
SKILL = Path('C:/Users/YOUR_USERNAME/.codex/skills/minecraft-builder-skill')
REFERENCE = Path('D:/ft工程文件/image2.5/nature-dungeon-styles-20261104/S8-crystal-geode/out/S8-02-medium-room.png')
sys.path.insert(0, str(PREVIOUS))
from deliver import load


def digest(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()


def save(relative, value):
    target = ROOT / relative
    target.parent.mkdir(parents=True, exist_ok=True)
    target.write_text(json.dumps(value, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')


def run(label, argv, cwd=ROOT, env=None):
    result = subprocess.run([str(value) for value in argv], cwd=cwd, env=env, capture_output=True, text=True, encoding='utf-8', errors='replace', timeout=240, creationflags=subprocess.CREATE_NO_WINDOW)
    (ROOT / 'qa').mkdir(exist_ok=True)
    (ROOT / f'qa/{label}.log').write_text(result.stdout + result.stderr, encoding='utf-8')
    record = {'label': label, 'argv': [str(value) for value in argv], 'cwd': str(cwd), 'exit_code': result.returncode}
    with (ROOT / 'qa/commands.jsonl').open('a', encoding='utf-8') as stream:
        stream.write(json.dumps(record, ensure_ascii=False) + '\n')
    if result.returncode:
        raise RuntimeError(label + ': ' + result.stdout + result.stderr)
    print(result.stdout[-1200:])


def prepare():
    acceptance = json.loads((AUDIT / 'S8-01-ACCEPTANCE.json').read_text(encoding='utf-8'))
    assert acceptance['status'] == 'ACCEPTED_FOR_SEQUENTIAL_PRODUCTION' and acceptance['next_allowed'] == 'S8-02-medium-room.png only'
    assert digest(PREVIOUS / 'incoming/S8R_IMG01/room.nbt') == acceptance['nbt_sha256']
    run('build', ['node', SKILL / 'scripts/run-build.mjs', ROOT / 'recipe.mjs', '--out-dir', ROOM, '--data-version', '3465'], SKILL)
    targets = [{'label': label, 'position': position} for label, position in [('left-passage', [18, 2, 12]), ('right-passage', [6, 2, 12]), ('rear-arch', [14, 2, 23]), ('bookcase-front', [21, 2, 20]), ('chest-front', [7, 2, 19]), ('wooden-door-front', [5, 2, 20]), ('north', [12, 2, 0]), ('south', [14, 2, 24])]]
    connections = [{'role': 'entrance', 'facing': 'north', 'position': [12, 2, 0], 'width': 3, 'height': 3}, {'role': 'exit', 'facing': 'south', 'position': [14, 2, 24], 'width': 3, 'height': 3}]
    save('incoming/S8R_IMG02/room.json', {'id': 'S8R_IMG02', 'name': '低方台晶室与偏置后拱', 'size': [25, 15, 25], 'entrance': [12, 2, 0], 'interior': [12, 2, 3], 'facing': 'north', 'connections': connections, 'route_targets': targets, 'ornamental_doors_not_connectors': [{'blocks': [[5, 2, 22], [4, 2, 22]], 'opens_manually': True, 'default_open': False, 'purpose': 'small decorative storage recess; not runtime connector'}], 'reference': str(REFERENCE), 'reference_sha256': digest(REFERENCE), 'recipe': str(ROOT / 'recipe.mjs'), 'recipe_sha256': digest(ROOT / 'recipe.mjs'), 'sha256': digest(ROOM / 'room.nbt'), 'status': 'AWAITING_INDEPENDENT_ACCEPTANCE', 'previous_acceptance': str(AUDIT / 'S8-01-ACCEPTANCE.json'), 'mechanisms': [], 'runtime_slot': None, 'deployment': 'NOT_DEPLOYED'})
    data = load(ROOM / 'room.nbt')
    states = {tuple(block['pos']): data['palette'][block['state']] for block in data['blocks']}
    assert data['DataVersion'] == 3465 and data['size'] == [25, 15, 25] and len(states) == len(data['blocks']) == 9375
    assert all(0 <= position[0] < 25 and 0 <= position[1] < 15 and 0 <= position[2] < 25 for position in states)
    assert all(states[block_x, 14, block_z]['Name'] == 'minecraft:deepslate_tiles' for block_x in range(25) for block_z in range(25))
    assert all(states[block_x, 0, block_z]['Name'] != 'minecraft:air' for block_x in range(25) for block_z in range(25))
    assert not data.get('entities')
    forbidden = {'water', 'lava', 'fire', 'soul_fire', 'spawner', 'command_block', 'structure_block', 'jigsaw', 'powder_snow', 'magma_block', 'cobweb'}
    assert all(state['Name'].removeprefix('minecraft:') not in forbidden for state in states.values())
    for port in connections:
        block_x, feet_y, block_z = port['position']
        assert all(states[block_x + offset, feet_y - 1, block_z]['Name'] != 'minecraft:air' and all(states[block_x + offset, level, block_z]['Name'] == 'minecraft:air' for level in range(feet_y, feet_y + 3)) for offset in [-1, 0, 1])
    boundary_openings = []
    for position, state in states.items():
        block_x, block_y, block_z = position
        if block_y < 2 or state['Name'] != 'minecraft:air' or block_x not in (0,24) and block_z not in (0,24):
            continue
        declared = block_z == 0 and abs(block_x - 12) <= 2 and block_y <= 7 or block_z == 24 and abs(block_x - 14) <= 2 and block_y <= 7
        assert declared, 'Undeclared perimeter opening: ' + str(position)
        boundary_openings.append(position)
    occupied = {position for position, state in states.items() if state['Name'] != 'minecraft:air'}
    reached = {position for position in occupied if position[1] == 0}
    pending = list(reached)
    while pending:
        position = pending.pop()
        for delta in ((1,0,0),(-1,0,0),(0,1,0),(0,-1,0),(0,0,1),(0,0,-1)):
            adjacent = tuple(position[axis] + delta[axis] for axis in range(3))
            if adjacent in occupied and adjacent not in reached:
                reached.add(adjacent)
                pending.append(adjacent)
    assert reached == occupied, 'Disconnected decorative island'
    save('incoming/S8R_IMG02/materials.json', dict(sorted(collections.Counter(state['Name'] for state in states.values() if state['Name'] != 'minecraft:air').items())))
    save('qa/static.json', {'status': 'PASS', 'nbt_sha256': digest(ROOM / 'room.nbt'), 'full_volume_unique_in_bounds': True, 'roof_foundation_complete': True, 'two_3x3_dry_ports': True, 'all_perimeter_air_within_declared_port_envelopes': True, 'perimeter_air_positions': boundary_openings, 'storage_recess_has_closed_outer_back_wall': True, 'all_nonair_six_face_connected_to_foundation': True, 'no_entities_fluids_hazards_spawners_loot': True, 'scope': 'Static voxel checks, not physical structural simulation or world ticks'})


def native():
    template = AUDIT / 'S8-01-R2-supplement/CollisionRoutes.java'
    source = template.read_text(encoding='utf-8')
    source = source.replace('70 - node.halfZ', '50 - node.halfZ').replace('> 69', '> 49').replace('> 37', '> 25').replace('34-position.getZ()', '24-position.getZ()')
    source = source.replace('if (!hash.equals("ee50ce32006d15b24ae2b2932bbf4ebbef6683ae8b9ee33ca4781348ac0fa798"))', 'if (!hash.equals(metadata.get("sha256").getAsString()))')
    source = source.replace('throw new IllegalStateException("Wrong R2 NBT")', 'throw new IllegalStateException("Wrong S8-02 NBT")')
    source = source.replace('        List<BlockState> palette = new ArrayList<>();', '''        List<BlockState> palette = new ArrayList<>();
        if (data.getInt("DataVersion") != 3465) throw new IllegalStateException("DataVersion");
        for (int index = 0; index < paletteNbt.size(); index++) {
            CompoundTag value = paletteNbt.getCompound(index);
            var identifier = new net.minecraft.resources.ResourceLocation(value.getString("Name"));
            if (!identifier.getNamespace().equals("minecraft") || !BuiltInRegistries.BLOCK.containsKey(identifier)) throw new IllegalStateException("Invalid block " + identifier);
            var block = BuiltInRegistries.BLOCK.get(identifier);
            for (String key : value.getCompound("Properties").getAllKeys()) {
                var property = block.getStateDefinition().getProperty(key);
                if (property == null || property.getValue(value.getCompound("Properties").getString(key)).isEmpty()) throw new IllegalStateException("Invalid property " + key);
            }
        }''')
    source = source.replace('        Map<String, Node> targets = new LinkedHashMap<>();', '''        var structureTemplate = new net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate();
        structureTemplate.load(BuiltInRegistries.BLOCK.asLookup(), data);
        CompoundTag saved = structureTemplate.save(new CompoundTag());
        if (saved.getList("blocks", Tag.TAG_COMPOUND).size() != blocks.size()) throw new IllegalStateException("Roundtrip blocks changed");
        Map<String, Node> targets = new LinkedHashMap<>();''')
    source = source.replace('            Map<Node, Node> previous = new HashMap<>();', '''            final Map<BlockPos, BlockState> snapshot = states;
            net.minecraft.world.level.LevelReader view = (net.minecraft.world.level.LevelReader) java.lang.reflect.Proxy.newProxyInstance(net.minecraft.world.level.LevelReader.class.getClassLoader(), new Class<?>[]{net.minecraft.world.level.LevelReader.class}, (proxy, method, parameters) -> {
                String name = method.getName();
                if (name.equals("getBlockState")) return snapshot.getOrDefault((BlockPos)parameters[0], net.minecraft.world.level.block.Blocks.AIR.defaultBlockState());
                if (name.equals("getFluidState")) return snapshot.getOrDefault((BlockPos)parameters[0], net.minecraft.world.level.block.Blocks.AIR.defaultBlockState()).getFluidState();
                if (name.equals("getMinBuildHeight")) return 0;
                if (name.equals("getHeight") || name.equals("getMaxBuildHeight")) return 15;
                if (name.equals("toString")) return "S8-02 immutable static block snapshot";
                throw new UnsupportedOperationException(name);
            });
            List<Object> supportChecks = new ArrayList<>();
            for (var entry : states.entrySet()) {
                String identifier = BuiltInRegistries.BLOCK.getKey(entry.getValue().getBlock()).toString();
                if (identifier.endsWith("amethyst_cluster") || identifier.endsWith("lantern") || identifier.endsWith("_door")) {
                    boolean survives = entry.getValue().canSurvive(view, entry.getKey());
                    supportChecks.add(Map.of("block", identifier, "position", List.of(entry.getKey().getX(), entry.getKey().getY(), entry.getKey().getZ()), "canSurvive", survives));
                    if (!survives) throw new IllegalStateException("Unsupported fragile block " + entry.getKey());
                }
            }
            Map<Node, Node> previous = new HashMap<>();''')
    source = source.replace('"stairs", stairs));', '"stairs", stairs, "static_fragile_support", supportChecks));')
    source = source.replace('        report.put("source_nbt_sha256", hash);', '        report.put("source_nbt_sha256", hash);\n        report.put("native_palette_properties_template_roundtrip", "PASS");\n        report.put("size", List.of(25,15,25));')
    (ROOT / 'tools').mkdir(exist_ok=True)
    (ROOT / 'tools/CollisionRoutes.java').write_text(source, encoding='utf-8')
    java = Path('C:/Program Files/Common Files/Oracle/Java/javapath')
    mapped = Path('D:/ft工程文件/锈铜宝库地牢_20261101/qa/vanilla-1.20.1-mapped.jar')
    support = Path('D:/ft工程文件/霜雪花园地牢_20261101/build/classpath/runData_minecraftClasspath.txt')
    dependencies = [line for line in support.read_text(encoding='utf-8').splitlines() if line.strip() and 'minecraft_user_repo' not in line]
    classpath = os.pathsep.join([str(mapped), *dependencies])
    (ROOT / 'qa/classes').mkdir(parents=True, exist_ok=True)
    (ROOT / 'qa/classpath.txt').write_text(classpath, encoding='utf-8')
    run('native-compile', [java / 'javac.exe', '-encoding', 'UTF-8', '-classpath', classpath, '-d', ROOT / 'qa/classes', ROOT / 'tools/CollisionRoutes.java'])
    run('native-run', [java / 'java.exe', '-Xmx2G', '-Djava.awt.headless=true', '-classpath', str(ROOT / 'qa/classes') + os.pathsep + classpath, 'CollisionRoutes', ROOM / 'room.nbt', ROOM / 'room.json', ROOT / 'qa/native.json'])
    report = json.loads((ROOT / 'qa/native.json').read_text(encoding='utf-8'))
    assert report['status'] == 'COLLISION_ROUTE_SUPPLEMENT_PASS'
    save('qa/native-provenance.json', {'source_nbt_sha256': digest(ROOM / 'room.nbt'), 'generated_java_sha256': digest(ROOT / 'tools/CollisionRoutes.java'), 'generic_verifier_template_sha256': digest(template), 'mapped_jar_sha256': digest(mapped), 'classpath_source_sha256': digest(support), 'exact_commands': 'qa/commands.jsonl', 'classpath': 'qa/classpath.txt', 'state_rotation': 'square25 [x,y,z]->[24-z,y,x], body center -> [25-z,y,x]', 'geometry_reuse': False, 'scope': 'Native registry/properties/template roundtrip + collision sweep model + static canSurvive for crystals, lanterns, paired doors in all rotations. No living plants or fluid/tag survival dependency.'})


def previews():
    cuts = {
        'north': [([0,2,0],[24,13,1]),([1,13,1],[23,13,2])],
        'east': [([23,2,0],[24,13,24]),([22,13,1],[23,13,23])],
        'south': [([0,2,23],[24,13,24]),([1,13,22],[23,13,23])],
        'west': [([0,2,0],[1,13,24]),([1,13,1],[2,13,23])],
    }
    original = load(ROOM / 'room.nbt')
    original_states = {tuple(block['pos']): original['palette'][block['state']] for block in original['blocks']}
    records = []
    for direction, boxes in cuts.items():
        env = dict(os.environ, S8_INSPECTION_DIRECTION=direction)
        run('cutaway-' + direction, ['node', SKILL / 'scripts/run-build.mjs', ROOT / 'cutaway.mjs', '--out-dir', ROOT / 'views' / direction, '--data-version', '3465', '--no-block-state-updates'], SKILL, env)
        derived_path = ROOT / 'views' / direction / f'inspection-only-{direction}.nbt'
        derived = load(derived_path)
        actual = {tuple(block['pos']): derived['palette'][block['state']] for block in derived['blocks']}
        expected = dict(original_states)
        all_boxes = [([0,14,0],[24,14,24]), *boxes]
        for first, last in all_boxes:
            for block_x in range(first[0], last[0] + 1):
                for block_y in range(first[1], last[1] + 1):
                    for block_z in range(first[2], last[2] + 1):
                        expected[block_x, block_y, block_z] = {'Name': 'minecraft:air'}
        assert actual == expected
        records.append({'direction': direction, 'source_nbt_sha256': digest(ROOM / 'room.nbt'), 'derived_nbt': str(derived_path), 'derived_sha256': digest(derived_path), 'inclusive_air_cut_boxes': all_boxes, 'outside_cut_states_unchanged': True, 'candidate_eligible': False})
    save('qa/cutaway-provenance.json', {'status': 'PASS', 'records': records})
    run('lodestone-capture', ['node', ROOT / 'capture.cjs'])
    font = ImageFont.truetype('C:/Windows/Fonts/msyh.ttc', 19)
    for mode in ('interior', 'exterior'):
        sheet = Image.new('RGB', (1400, 1140), '#18212b')
        draw = ImageDraw.Draw(sheet)
        draw.text((12, 10), 'S8-02 / ' + mode + ' / Lodestone真实渲染；内部为独立方向剖切', font=font, fill='white')
        for index, direction in enumerate(cuts):
            with Image.open(ROOT / 'views' / direction / f'{mode}-{direction}.png') as image:
                sheet.paste(image.resize((700,540)), (index % 2 * 700, 45 + index // 2 * 545))
        sheet.save(ROOT / f'views/{mode}-four-views.png')
    shutil.copyfile(ROOT / 'views/north/interior-north.png', ROOM / 'preview.png')


def duplicates():
    import takeover_audit as checker
    import duplicate_check as previous_sources
    candidate = checker.describe(ROOM / 'room.nbt')
    roots = [*previous_sources.SOURCES, PREVIOUS]
    roots += [path for path in ROOT.parent.iterdir() if path.is_dir() and path.name.startswith(('nature', 'S6-redo', 'S7-redo'))]
    independent = json.loads((AUDIT / 'file-audit.json').read_text(encoding='utf-8'))
    targets = {Path(group['candidate']) for group in independent['groups']}
    for source in roots:
        if source.exists():
            for pattern in ('incoming/*/room.nbt', 'rooms/*/room.nbt', '01-large-chamber/incoming/*/room.nbt'):
                targets.update(source.glob(pattern))
    records = []
    errors = []
    pairs = []
    for target in sorted(targets):
        if target.resolve() == (ROOM / 'room.nbt').resolve():
            continue
        try:
            other = checker.describe(target)
            scores = [checker.iou(candidate['volume'][0], volume) for volume in other['volume']]
            best = max(range(4), key=scores.__getitem__)
            record = {'path': str(target), 'sha256': other['sha256'], 'size': other['size'], 'exact_material_independent_four_rotation_match': bool(candidate['exact'] & other['exact']), 'normalized_interior_solid_iou': round(scores[best], 4), 'dominant_floor_clearance_iou': round(max(checker.iou(candidate['ground'][0], mask) for mask in other['ground']), 4), 'best_rotation': best, 'topology': other['topology'], 'failure_comparison_only': any(name in str(target) for name in ('nature-b1-S8', 'nature-S8-sequential')), 'candidate_status_not_assigned_by_this_comparison': True}
            records.append(record)
            pairs.append((other, record))
        except Exception as error:
            errors.append({'path': str(target), 'error': str(error)})
    records.sort(key=lambda item: -item['normalized_interior_solid_iou'])
    exact = [item for item in records if item['exact_material_independent_four_rotation_match']]
    high = [item for item in records if item['normalized_interior_solid_iou'] >= 0.9]
    previous = next(item for item in records if item['path'] == str(PREVIOUS / 'incoming/S8R_IMG01/room.nbt'))
    save('qa/material-independent.json', {'status': 'SCREENING_PASS' if not exact and not high and not errors else 'NEEDS_REVIEW', 'source_nbt_sha256': candidate['sha256'], 'comparison_count': len(records), 'candidate_topology': candidate['topology'], 's8_01_direct_comparison': previous, 'exact_matches': exact, 'interior_iou_over_0_9': high, 'errors': errors, 'comparisons': records, 'method': 'Names/states discarded; exact occupancy four rotations; independently detected dominant floor; 31x31 floor clearance, 23x15x23 interior solid normalization excluding outer two cells/foundation/top; bounded obstacle component descriptors.', 'limitations': 'Occupancy screening threshold is not global topology/semantic uniqueness. Crystal core is reference-required; compared S8-01 has large round dais and high free-standing column enclosure, S8-02 uses low rectangular slab and embedded wall supports, bookcase/chest/door recess, offset two-port route.', 'other_groups_modified': False})
    pairs.sort(key=lambda pair: -pair[1]['normalized_interior_solid_iou'])
    panels = [(candidate, 0, 'S8-02 25x15x25'), *[(other, record['best_rotation'], Path(other['path']).parent.name) for other, record in pairs[:3]]]
    image = Image.new('RGB', (1200, 690), '#101923')
    draw = ImageDraw.Draw(image)
    font = ImageFont.truetype('C:/Windows/Fonts/msyh.ttc', 16)
    draw.text((15,10), '材质无关：上=主层净空，下=归一化内部固体高度；右侧为近邻只读对照', fill='white', font=font)
    for index, (item, turn, title) in enumerate(panels):
        left = index * 300 + 12
        draw.text((left,50), title, fill='white', font=font)
        for sample_x in range(31):
            for sample_z in range(31):
                draw.rectangle((left+sample_x*8,90+sample_z*8,left+sample_x*8+7,90+sample_z*8+7), fill='#a1cbd0' if (sample_x,sample_z) in item['ground'][turn] else '#303947')
        for sample_x in range(23):
            for sample_z in range(23):
                height = item['heights'][turn][sample_x,sample_z]
                draw.rectangle((left+sample_x*11,390+sample_z*11,left+sample_x*11+10,390+sample_z*11+10), fill=(35+height*13,40+height*10,45+height*8))
    image.save(ROOT / 'qa/material-independent.png')
    print('Compared',len(records),'S8-01 interior IoU',previous['normalized_interior_solid_iou'],'max',records[0]['normalized_interior_solid_iou'])
    assert not exact and not high and not errors


def review():
    report = json.loads((ROOT / 'qa/lodestone.json').read_text(encoding='utf-8'))
    save('qa/visual-review.json', {'status': 'PRODUCER_VISUAL_REVIEW_PASS_WITH_LIMITATIONS', 'independent_verdict': None, 'source_nbt_sha256': digest(ROOM / 'room.nbt'), 'reference_sha256': digest(REFERENCE), 'reviewed_images': [{'path': item['screenshot'], 'sha256': item['screenshot_sha256']} for item in report['records']], 'reviewed_contact_sheets': ['views/interior-four-views.png', 'views/exterior-four-views.png'], 'observed_composition': ['North cutaway: bookcase left rear, offset arch center-left, chest and wooden door on right rear, low rectangular crystal table.', 'East/west cutaways: side face of low platform and embedded wall supports; not a high circular altar or detached four-column enclosure.', 'South cutaway: opposite side of same low plinth with bookcase/door recess retained in foreground, northern entry visible behind.', 'All four exteriors retain roof and wall shell; only declared north/south openings exist.', 'Full-scale candidate has 25x15x25 volume; small core top Y7 against roof Y14, 7x5 base and 5x3 upper tier.'], 'reference_differences': ['Opaque cubic amethyst instead of translucent pointed crystal mesh and purple emissive bloom.', 'Ordinary lantern warm light instead of purple concept lamps.', 'Purple hanging vegetation represented by supported crystals, not an actual purple vine species.', 'Minecraft paired doors are two blocks high with a taller tinted-glass transom; not literal matching concept-art door proportions.', 'Floor diamond accent is an integer modeling interpretation; details simplified to vanilla blocks.'], 'production_iteration_notes': ['Adjusted screen-left/right convention before freeze: reflected X coordinates and door hinges, then regenerated all native/previews/duplicate evidence.', 'Closed ornamental storage recess rear wall before freeze; static checker now enumerates every perimeter air position and rejects undeclared openings.'], 'next_image_started': False, 'world_gameplay_claimed': False})
    comparison = Image.new('RGB', (1400, 620), '#141e2b')
    draw = ImageDraw.Draw(comparison)
    font = ImageFont.truetype('C:/Windows/Fonts/msyh.ttc', 20)
    draw.text((15,10), 'S8-02：左=参考图，右=候选NBT实际Lodestone剖视；非游戏截图', fill='white', font=font)
    with Image.open(REFERENCE) as reference:
        reference.thumbnail((700,550))
        comparison.paste(reference.convert('RGB'), (0,55))
    with Image.open(ROOM / 'preview.png') as actual:
        actual.thumbnail((700,550))
        comparison.paste(actual.convert('RGB'), (700,55))
    comparison.save(ROOT / 'qa/reference-comparison.png')


def package():
    metadata = json.loads((ROOM / 'room.json').read_text(encoding='utf-8'))
    native = json.loads((ROOT / 'qa/native.json').read_text(encoding='utf-8'))
    static = json.loads((ROOT / 'qa/static.json').read_text(encoding='utf-8'))
    repeat = json.loads((ROOT / 'qa/material-independent.json').read_text(encoding='utf-8'))
    browser = json.loads((ROOT / 'qa/lodestone.json').read_text(encoding='utf-8'))
    cutaways = json.loads((ROOT / 'qa/cutaway-provenance.json').read_text(encoding='utf-8'))
    visual = json.loads((ROOT / 'qa/visual-review.json').read_text(encoding='utf-8'))
    actual = digest(ROOM / 'room.nbt')
    assert actual == metadata['sha256'] == native['source_nbt_sha256'] == static['nbt_sha256'] == repeat['source_nbt_sha256']
    assert visual['source_nbt_sha256'] == actual
    assert metadata['recipe_sha256'] == digest(ROOT / 'recipe.mjs') and metadata['reference_sha256'] == digest(REFERENCE)
    assert native['status'] == 'COLLISION_ROUTE_SUPPLEMENT_PASS' and repeat['status'] == 'SCREENING_PASS'
    assert [turn['degrees_clockwise'] for turn in native['turns']] == [0,90,180,270]
    assert all(len(turn['routes']) == 9 and all(route['reachable_without_jump'] for route in turn['routes']) for turn in native['turns'])
    assert all(all(port['three_lanes_supported'] and port['three_block_headroom'] for port in turn['anchors']) for turn in native['turns'])
    assert len(browser['records']) == 8 and len(cutaways['records']) == 4
    for record in browser['records']:
        assert record['served_sha256'] == digest(ROOT / record['nbt'])
        assert record['screenshot_sha256'] == digest(ROOT / record['screenshot']) and not record['page_errors']
        with Image.open(ROOT / record['screenshot']) as image:
            image.verify()
    save('assembly.json', {'overall_size': [25,15,25], 'axis': '+X east, +Y up, +Z south', 'modules': [{'id': 'S8R_IMG02', 'nbt': str(ROOM / 'room.nbt'), 'sha256': actual, 'size': [25,15,25], 'offset': [0,0,0], 'local_origin': [0,0,0], 'rotation_pivot': [12,0,12], 'placement_order': 1, 'dependencies': [], 'connection_anchors': metadata['connections']}], 'split': False, 'rotation': 'clockwise [x,y,z]->[24-z,y,x]; rotate block states and all anchors; both port feet Y2', 'world_origin': None, 'overlap_policy': 'one module; full25x15x25 explicit-air volume must be checked before placement; reject occupied volume, do not clear world', 'candidate_nbt_only': str(ROOM / 'room.nbt'), 'inspection_nbt_not_deployable': True, 'world_placement_and_400ticks': 'NOT_RUN', 'deployment': 'NOT_DEPLOYED'})
    save('incoming/S8R_IMG02/qa.json', {'status': 'PRODUCER_CHECKS_PASS_AWAITING_INDEPENDENT_ACCEPTANCE', 'sha256': actual, 'native_report': str(ROOT / 'qa/native.json'), 'static_report': static, 'native_palette_roundtrip': 'PASS', 'native_static_fragile_checks_per_rotation': [len(turn['static_fragile_support']) for turn in native['turns']], 'four_rotations_nine_targets_each': True, 'swept_player_model': '0.6x1.8 body, half-grid <=0.5 steps, native shapes, reverse/full-block/low-ceiling controls; NOT Entity.move simulation', 'interface_count': 2, 'wooden_door_is_connector': False, 'actual_lodestone_webgl_views': 8, 'cutaway_report': str(ROOT / 'qa/cutaway-provenance.json'), 'duplicate_screening_comparisons': repeat['comparison_count'], 'duplicate_report': str(ROOT / 'qa/material-independent.json'), 'world_placement_four_rotations_400ticks': 'NOT_RUN', 'neighbor_updates_chunk_reload_forge_gameplay': 'NOT_RUN', 'deployment': 'NOT_DEPLOYED', 'independent_acceptance': None, 'next_image_started': False})
    save('registration-request.json', {'status': 'AWAITING_INDEPENDENT_ACCEPTANCE', 'producer_thread': '01a1a64f-3e44-7c72-ab6d-8d3efa9147a3', 'group': 8, 'reference_index': 2, 'reference': str(REFERENCE), 'reference_sha256': digest(REFERENCE), 'size': [25,15,25], 'nbt': str(ROOM / 'room.nbt'), 'sha256': actual, 'metadata': str(ROOM / 'room.json'), 'qa': str(ROOM / 'qa.json'), 'preview': str(ROOM / 'preview.png'), 'native_evidence': str(ROOT / 'qa/native.json'), 'repeat_evidence': str(ROOT / 'qa/material-independent.json'), 'views': str(ROOT / 'views'), 'not_run': ['world placement', '400tick simulation', 'Forge gameplay', 'runtime integration'], 'next_image': 'S8-03 NOT_AUTHORIZED_NOT_STARTED', 'independent_verdict': None, 'deployment': 'NOT_DEPLOYED'})
    save('checksums.json', {target.relative_to(ROOT).as_posix(): digest(target) for target in sorted(ROOT.rglob('*')) if target.is_file() and target.name != 'checksums.json' and '__pycache__' not in target.parts})
    print('PACKAGED S8-02', actual, 'AWAITING_INDEPENDENT_ACCEPTANCE')


if __name__ == '__main__':
    {'prepare': prepare, 'native': native, 'previews': previews, 'duplicates': duplicates, 'review': review, 'package': package}[sys.argv[1]]()
