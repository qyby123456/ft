import copy
import hashlib
import json
import shutil
import subprocess
import zipfile
from itertools import combinations, product
from pathlib import Path
from audit_s8_submission import decode, load

workspace = Path('C:/Users/YOUR_USERNAME/.codex/visualizations/2026/11/04/01a1a685-045b-76d2-aa2b-2a3f24520603')
root = workspace / 'preserved-review-snapshots/S8-interaction-revision-20261107-1355'
current = root / 'access-revision-r4'
prior = workspace / 'preserved-review-snapshots/S8-submission-20261107-104351-507543'
revision = load(current / 'REVISION.json')
candidate = current / 'assembly/s8-functional-access-revision.nbt'
assert hashlib.sha256(candidate.read_bytes()).hexdigest() == revision['sha256']
merged, expected = decode(candidate)
rebuilt = {}
boxes = []
with zipfile.ZipFile(current / 's8_access_revision_install.zip') as package:
    assert package.testzip() is None
    for part in revision['parts']:
        source = current / 'assembly/parts' / Path(part['path']).name
        content = source.read_bytes()
        assert hashlib.sha256(content).hexdigest() == part['sha256']
        name = 'data/s8_takeover/structures/parts/' + source.name
        assert package.read(name) == content
        assert (current / 'deployment-pack' / name).read_bytes() == content
        data, blocks = decode(source)
        assert data['size'] == part['size'] and max(data['size']) <= 48 and not data['entities']
        assert len(blocks) == part['blocks']
        origin = part['origin']
        assert all(0 <= origin[axis] and origin[axis] + data['size'][axis] <= merged['size'][axis] for axis in range(3))
        boxes.append((origin, data['size']))
        for point, block in blocks.items():
            transformed = tuple(point[axis] + origin[axis] for axis in range(3))
            assert transformed not in rebuilt
            rebuilt[transformed] = block
    assert len([name for name in package.namelist() if name.endswith('.nbt')]) == 20
for first, second in combinations(boxes, 2):
    assert not all(max(first[0][axis], second[0][axis]) < min(first[0][axis] + first[1][axis], second[0][axis] + second[1][axis]) for axis in range(3))
assert len(boxes) == 20 and sum(size[0] * size[1] * size[2] for origin, size in boxes) == 1009200
assert rebuilt == expected and not merged['entities']
boundary = set()
for axis in range(3):
    other = [index for index in range(3) if index != axis]
    for edge, first, second in product((0, merged['size'][axis] - 1), range(merged['size'][other[0]]), range(merged['size'][other[1]])):
        point = [0, 0, 0]
        point[axis], point[other[0]], point[other[1]] = edge, first, second
        boundary.add(tuple(point))
assert len(boundary) == 90282
assert all(expected[point]['state']['Name'] == 'minecraft:barrier' for point in boundary)
old_input = load(prior / 'independent-qa/route-input.json')
new_input = load(current / 'assembly/qa/route-input.json')[0]
old_merged = old_input[-1]
changes = []
label_changes = []
assert new_input['start'] == old_merged['start'] and len(new_input['targets']) == len(old_merged['targets']) == 58
for before, after in zip(old_merged['targets'], new_input['targets']):
    if before['label'] != after['label']:
        label_changes.append(dict(original=before['label'], revised=after['label']))
        assert after['label'] == before['label'].replace('full-port center', 'portal center') or after['label'] == before['label'] + ' raised step landing'
    if before['position'] != after['position']:
        assert before['position'] == [108, 4, 20] and after['position'] == [108, 5, 20]
        changes.append(dict(original=before, revised=after))
assert len(changes) == 1
assert len(label_changes) == 21
qa = root / 'independent-current-qa'
qa.mkdir()
plans = []
for identifier in ('S8-13', 'S8-16'):
    original = next(plan for plan in old_input if Path(plan['candidate']).parent.name == identifier)
    plan = copy.deepcopy(original)
    source = next((current / identifier).glob('*.nbt'))
    plan['candidate'] = str(source)
    plan['sha256'] = hashlib.sha256(source.read_bytes()).hexdigest()
    module = next(module for module in revision['modules'] if module['id'] == identifier)
    original_feet = [value - offset for value, offset in zip([108, 4, 20], module['origin'])]
    for target in plan['targets']:
        if identifier == 'S8-13' and target['position'] == original_feet:
            target['position'][1] += 1
    plans.append(plan)
new_input['candidate'] = str(candidate)
plans.append(new_input)
(qa / 'route-input.json').write_text(json.dumps(plans, ensure_ascii=False, indent=2), encoding='utf-8')
shutil.copyfile(prior / 'independent-qa/vanilla-block-tags.json', qa / 'vanilla-block-tags.json')
result = dict(accepted=False, status='INDEPENDENT_CURRENT_PARTS_BOUNDARY_TARGETS_PASS', source_sha256=revision['sha256'], parts=20, full_partition_volume=1009200, reconstructed_records=len(rebuilt), boundary=90282, original_targets=38, extra_port_targets=20, explicit_waypoint_revision=changes, preserved_current_snapshot=len(load(root / 'submission-snapshot.json')['files']))
result['label_changes_without_target_deletion'] = label_changes
(root / 'independent-parts-targets.json').write_text(json.dumps(result, ensure_ascii=False, indent=2), encoding='utf-8')
print(json.dumps(result, ensure_ascii=False), flush=True)
destination = workspace / 's8-independent-revision-recipes-20261107-1600'
destination.mkdir()
base = destination / 'independent-prior-inputs'
(base / 'assembly').mkdir(parents=True)
layout = load(prior / 'assembly/layout.json')
generated = workspace / 's8-independent-recipes-20261107-1335'
assert load(prior / 'independent-reproduction.json')['all31_binary_equal']
for module in layout['modules']:
    identifier = module['id']
    source = generated / identifier / Path(module['source']).name
    assert hashlib.sha256(source.read_bytes()).hexdigest() == module['sha256']
    module['source'] = str(source)
(base / 'assembly/layout.json').write_text(json.dumps(layout, ensure_ascii=False, indent=2), encoding='utf-8')
shutil.copyfile(generated / 'assembly/s8-functional-combined.nbt', base / 'assembly/s8-functional-combined.nbt')
shutil.copytree(prior / 'deployment-plan', base / 'deployment-plan')
script = (current / 'build.mjs').read_text(encoding='utf-8')
original_root = 'D:/ft工程文件/FT地牢_功能房间第二轮_接手续作_20261106/S8'
assert script.count(original_root) == 1
script = script.replace(original_root, base.as_posix())
(destination / 'build.mjs').write_text(script, encoding='utf-8')
process = subprocess.run(['C:/Program Files/nodejs/node.exe', str(destination / 'build.mjs')], capture_output=True, timeout=240)
(destination / 'console.log').write_bytes(process.stdout + process.stderr)
assert process.returncode == 0, process.stderr.decode('utf-8', errors='replace')
outputs = [candidate, *sorted((current / 'assembly/parts').glob('*.nbt')), *sorted(current.glob('S8-*/*.nbt'))]
assert len(outputs) == 31
for source in outputs:
    assert source.read_bytes() == (destination / source.relative_to(current)).read_bytes(), source
for item in load(root / 'submission-snapshot.json')['files']:
    assert hashlib.sha256(Path(item['snapshot']).read_bytes()).hexdigest() == item['sha256']
result = dict(accepted=False, status='INDEPENDENT_CURRENT31NBT_BINARY_EXACT_FROM_PRIOR_PARENT_RECIPES_AND29PATCHES', source_sha256=revision['sha256'], outputs=31, directory=str(destination), all31_binary_equal=True, current_candidate_nbt_used_as_recipe_input=False, only_input_path_relocated=True)
(root / 'independent-current-reproduction.json').write_text(json.dumps(result, ensure_ascii=False, indent=2), encoding='utf-8')
print(json.dumps(result, ensure_ascii=False), flush=True)
