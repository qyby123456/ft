import hashlib
import json
import math
import zipfile
from datetime import datetime
from pathlib import Path

from audit_s8_submission import decode, load

review = Path(__file__).parent
root = review / 'S7-submission-20261107-102850-176442'
prior = review / 'S7-submission-20261107-083104-571437'
hooks = review / 'S7-hooks-supplement-20261107-1047'
for folder, count in ((root,828),(prior,542),(hooks,22)):
    records = load(folder / 'submission-snapshot.json')['files']
    assert len(records) == count
    for item in records:
        assert hashlib.sha256(Path(item['snapshot']).read_bytes()).hexdigest() == item['sha256']
digest = '906e63eea067ab01c4f0df2619102dd17a446b7c6858c732f4f03a6f3582de22'
ready = load(root / 'ready-for-review.json')
assert ready['assembly']['sha256'] == digest
assert hashlib.sha256((root / 'assembly/s7-fungal-functions-connected.nbt').read_bytes()).hexdigest() == digest
native = load(root / 'independent-qa/independent-native-frozen.json')
support = load(root / 'independent-qa/independent-all-support.json')
assert len(native) == len(support) == 11
assert all(item['registry_properties'] == item['template_roundtrip_block_count'] == 'PASS' for item in native)
assert all(not item['failures'] and not item['unsupported'] for item in support)
assert sum(item['nonair_checked'] for item in support) == 304769
for folder in ('independent-qa','independent-posttick-qa'):
    routes = load(root / folder / 'independent-routes.json')
    assert len(routes) == 11 and all(item['start_clear'] for item in routes)
    assert sum(len(item['targets']) for item in routes) == 1220
    assert all(target['reachable'] and target['standing_clear'] for item in routes for target in item['targets'])
payload = load(root / 'independent-qa/payload-entities-parts-barrier.json')
assert payload['sha256'] == digest and payload['part_count'] == 15
assert payload['barrier_cells'] == 60115 and payload['room_volume_cells_enclosed'] == 250431
assert payload['original_room_targets'] == 79 and payload['merged_declared_targets'] == 106
assert payload['entities_exact_translation_and_part_reconstruction'] == 3
assert payload['part_payload_reconstruction'] == 'EXACT'
reproduction = load(root / 'independent-qa/reproduction.json')
assert reproduction['all_26_outputs_binary_equal'] and not reproduction['candidate_nbt_used_as_recipe_input']
assert reproduction['part_entities_full_positions_equipment_and_payload_equal']
changes = load(root / 'independent-changes-inspection.json')
assert changes['all_prior_nonair_payloads_and_entities_retained'] and changes['barrier_set_exactly_unchanged']
assert len(changes['merged_changes']) == 12
updates = load(root / 'independent-qa/world-updates-inspection.json')
assert updates['all_11_type_payloads_retained'] and len(updates['rooms']) == 11
runtime = load(root / 'independent-runtime-inspection.json')
assert runtime['source_sha256'] == digest and runtime['eye_targets'] == 39 and runtime['door_control_checks'] == 12
assert len(runtime['natural_courses']) == 13 and all(item['passed'] for item in runtime['natural_courses'])
for mode in ('worldqa-proof','naturalqa-complete'):
    assert load(root / ('independent-' + mode + '-result.json'))['exit_code'] == 0
hook_run = load(hooks / 'independent-run.json')
assert hook_run['exit_code'] == 0 and hook_run['frozen_files_unchanged'] == 22
hook_runtime = Path(hook_run['runtime'])
automatic = load(hook_runtime / 'automatic-hooks-report.json')
assert automatic['candidate_sha256'] == digest
assert automatic['entry_natural_tick_teleport'] and automatic['return_natural_tick_teleport']
assert automatic['remote_stale_trigger_rejected_naturally'] and automatic['remote_disabled_trigger_rejected']
assert automatic['entry_enabled_naturally_tick'] < automatic['entry_arrival_tick'] < automatic['return_arrival_tick']
assert all(automatic[key] == 0 for key in ('manual_load_calls','manual_tick_calls','manual_enable_calls'))
package = root / 'installation/fts7-datapack.zip'
assert hashlib.sha256(package.read_bytes()).hexdigest() == automatic['package_sha256']
assert package.read_bytes() == (hook_runtime / 'server/world/datapacks/fts7-datapack.zip').read_bytes()
with zipfile.ZipFile(package) as archive:
    assert archive.testzip() is None
    assert json.loads(archive.read('data/minecraft/tags/functions/load.json'))['values'] == ['fts7:load']
    assert json.loads(archive.read('data/minecraft/tags/functions/tick.json'))['values'] == ['fts7:tick']
    for index, part in enumerate(ready['assembly']['parts']):
        assert hashlib.sha256(archive.read(f'data/fts7/structures/part_{index:02}.nbt')).hexdigest() == part['sha256']
data, blocks = decode(root / 'assembly/s7-fungal-functions-connected.nbt')
layout = load(root / 'assembly/assembly.json')
corridor_samples = 0
for link in layout['links']:
    centers = link['centerline_xz']
    assert centers[0] == [link['start'][0],link['start'][2]] and centers[-1] == [link['end'][0],link['end'][2]]
    for step, center in enumerate(centers):
        previous, following = centers[max(0,step-1)], centers[min(len(centers)-1,step+1)]
        direction = [following[axis]-center[axis] if following != center else center[axis]-previous[axis] for axis in (0,1)]
        assert sum(abs(value) for value in direction) == 1
        foot = link['start'][1] + round((link['end'][1]-link['start'][1])*2*step/max(1,len(centers)-1))/2
        for across in (-1,0,1):
            for height in range(4):
                point = (center[0]-direction[1]*across, math.ceil(foot)+height, center[1]+direction[0]*across)
                assert blocks.get(point, {'state':{'Name':'minecraft:air'}})['state']['Name'] == 'minecraft:air', point
                corridor_samples += 1
result = {'group':'S7','status':'ACCEPTED_FOR_SECOND_ROUND_ROOM_SCOPE','accepted':True,'completed_at':datetime.now().isoformat(),'source_sha256':digest,'frozen_file_counts':{'v6':828,'v4_inheritance':542,'automatic_hooks':22},'rooms':10,'parts':15,'reproduced_exact_nbt':26,'native_support_checks':304769,'pre_tick_route_targets':1220,'post_tick_route_targets':1220,'corridor_three_wide_four_high_air_samples':corridor_samples,'preserved_original_targets':79,'barrier_cells':60115,'functional_eye_use_targets':39,'door_control_checks':12,'natural_courses':13,'hooks_package_sha256':automatic['package_sha256'],'requirements':{'ten_distinct_reference_rooms_and_real_lodestone_views':'Prior actual visual review retained; exact12air-only local access changes and current15/16 images inspected','complete_payload_recipe_reproduction':'26 exact NBT; ten source payloads and3 entities preserved; ≤48 parts','registry_support_clearance_routes':'Actual vanilla audits and current pre/post1220 plus direct3x4corridor scan','world_safety':'Eleven exact1200tick producer captures independently classified; zero type loss, only native face/door/default properties and empty furnace RecipesUsed','functional_access_and_required_natural_movement':'Parent39actual eyes/use,12door controls,13natural courses','install_entry_return_barriers':'Parent full1969040clear/place check plus exactZIP natural hooks permission0 delayed enter/return,remote rejection','delivery_documentation':'Frozen installation README and package; requirements limits recorded'},'limits':['Unrotated finite Forge1.20.1 isolated server tests; no full production pack or live client/network certification','Natural movement courses reset between courses; eyes reposition to reachable standing points, not continuous whole-map client play','Whole1969040placement volume must be cleared even though sparse part envelopes total1324800','No combat/spawner/boss/reward or production deployment acceptance'], 'production_deployed':False}
(root / 'ACCEPTANCE.json').write_text(json.dumps(result, ensure_ascii=False, indent=2), encoding='utf-8')
print(json.dumps(result, ensure_ascii=False, indent=2))
