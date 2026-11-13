import json
from pathlib import Path
import audit
from review_batch_1105 import CASES,ROOT

COMPOSITION={
    2:'Registered image03 reference explicitly checked against actual NBT. Open sandstone single-chest courtyard, low stepped front entry, paired wall arches, pot niches and orange floor inlay retained. Wall detail and vegetation simplified; not claimed pixel-identical.',
    4:'Revised actual full view and root closeup reviewed against same reference. Asymmetric branched root flares now visibly connect major trunks to waterline and wall; all5 declared east branch endpoints in same101-block wood/root component. Exact140 root/log state changes only; chest water-island and lamps/furniture preserved. Blocky branches are an acknowledged approximation.',
    6:'Actual cutaway and exact source-cuboid translated niche closeup reviewed. Single low chest, two asymmetric lava pools, thick low horizontal niche, small wall lamp niche and wide/narrow exits retained. Safe shroomlight/glass approximates exposed reference heat detail, not a fire-spread test.',
    7:'Reference, actual oblique and axial cutaway reviewed: six independent columns/capitals, red banners, crystal bases, long central aisle, six small pools and elevated mushrooms. Tall side ledges and floor ornament are coarse; original reference lighting and purple fog not reproduced or certified.',
    8:'Reference and all4 actual cut interiors reviewed: eight mineral columns, white capitals and purple shafts, central patterned long aisle, entrance stair and side passages. Crystal branches and vault ornament simplified; no claim of concept-art lighting or emissive glass.'
}
STYLES={2:'sandstone-desert',4:'swamp-wetland',5:'coral-ocean',6:'volcanic-cavern',7:'fungal-grove',8:'crystal-geode'}
for group,image,label,expected,relative,candidate,preview in CASES:
    if group==3:
        continue
    folder=ROOT/label
    details=json.loads((folder/'independent-details.json').read_text(encoding='utf-8'))
    files=json.loads((folder/'file-audit.json').read_text(encoding='utf-8'))['groups'][0]
    assert audit.digest(audit.BASE/relative/candidate)==expected
    next_image=image+1
    next_name=f'S{group}-{next_image:02d}-'+('great-hall.png' if next_image==4 else 'corridor.png')
    next_path=audit.BASE/f'image2.5/nature-dungeon-styles-20261104/S{group}-{STYLES[group]}/out'/next_name
    assert next_path.is_file()
    result={'group':group,'image':image,'review_date':'2026-11-05','status':'ACCEPTED_FOR_SEQUENTIAL_PRODUCTION','nbt_sha256':expected,'size':files['size'],'composition':COMPOSITION[group],'independent_checks':details,'manifest_hashes_verified':[{'path':item['path'],'count':item['checked']} for item in files['manifests']],'world_ticks_interaction_runtime_rotations':'NOT_RUN; producer offline/native evidence not an actual world certification','deployment':'NOT_DEPLOYED_NOT_ACCEPTED_FOR_DEPLOYMENT','next_allowed':str(next_path),'next_independent_acceptance_required':True}
    if group==4:
        result['revision']='R1;previous rejected source evidence retained'
    (ROOT/f'S{group}-{image:02d}-ACCEPTANCE.json').write_text(json.dumps(result,ensure_ascii=False,indent=2),encoding='utf-8')

folder=ROOT/'S5-03-independent'
world=json.loads((folder/'world-independent.json').read_text(encoding='utf-8'))
details=json.loads((ROOT/'batch-1105-supplement.json').read_text(encoding='utf-8'))['S5']
result={'group':5,'image':3,'review_date':'2026-11-05','status':'ACCEPTED_FOR_SEQUENTIAL_PRODUCTION','nbt_sha256':world['sha256'],'size':[19,16,19],'composition':'Reference, actual full-water exterior and interior chest view reviewed. Small central lantern plinth, single empty chest wall niche, compact offset double doors and coral/kelp retained; not previous table/large coral mound. Vanilla branch and color approximations acknowledged. Lodestone water rendering is not game underwater fog.','independent_checks':details,'world_evidence_review':world,'external_water_contract':'Only source-water ocean or explicitly enclosed tank. Source-water ring8 blocks around source bounding box and cover8 above, solid Y0..2 bed, supported wet gate extensions3 blocks. NONE bounds X/Z -8..26,Y0..23; no dry corridor or finite exposed water cube. Rotate entire contract. Tested tank51x30x51 is artificial, not natural ocean placement.','failure_history':'Initial coral-fan support failure retained in worldtest;final worldtest-r2 is source-bound and passes. No image01 stair-waterlogging allowance used.','deployment':'NOT_DEPLOYED_NOT_ACCEPTED_FOR_DEPLOYMENT','world_review_boundary':'Independently decoded producer4x1200tick isolated Forge test captures, not another server execution or unlimited-time/integration guarantee.','next_allowed':str(audit.BASE/'image2.5/nature-dungeon-styles-20261104/S5-coral-ocean/out/S5-04-great-hall.png'),'next_independent_acceptance_required':True}
assert Path(result['next_allowed']).is_file()
(ROOT/'S5-03-ACCEPTANCE.json').write_text(json.dumps(result,ensure_ascii=False,indent=2),encoding='utf-8')

result={'group':3,'image':4,'review_date':'2026-11-05','status':'NEEDS_REFERENCE_COMPOSITION_REVISION','nbt_sha256':'37ad90011ed98a49c46494174dd3e1810e8c10c536ddab59ac0ae507fe99a8a3','passed':'39 hashes;vanilla registry/template;38 independent selected checks;22 body-route targets including duplicates;6clear supported connections;1995 roof columns;source-derived cutaway verified;no disconnected solids or fluid edges.','defect':'Full source nave view and oblique view show dominant unbroken3x3 spruce-log shafts Y7..17 and5x3 timber crown masses Y19..21 on all8 columns. Reference primarily reads as slender gray stone pillars with short timber bindings. Current large brown timber shafts/crowns dominate the axial silhouette, making an unfrozen timber hall rather than the reference stone colonnade. This is visible in full NBT, not caused solely by cutting or concept-art color grading.','requested':'Same image04 only: make eight column shafts predominantly gray stone with restrained short wood binding/support bands; reduce heavy timber crown masses while retaining gray capitals, rib rhythm, central/side passages, six ports, snow/ice accents and lamp survival. Provide full-source axial plus four interior/exterior views and revised hashes/regression checks. No need to add ordinary ice or melting snow layers under bright lanterns.','next_image':'S3-05_NOT_AUTHORIZED','world_ticks_and_deployment':'NOT_RUN'}
(ROOT/'S3-04-REVIEW.json').write_text(json.dumps(result,ensure_ascii=False,indent=2),encoding='utf-8')
print('Registered6 accepted candidates and1 same-image revision; no release messages sent by this script.')
