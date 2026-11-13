import json
from pathlib import Path
import audit

ROOT=Path(__file__).resolve().parent
for group,style in [(5,'coral-ocean'),(6,'volcanic-cavern')]:
    folder=ROOT/f'S{group}-04-independent'
    details=json.loads((folder/'independent-details.json').read_text(encoding='utf-8'))
    result={'group':group,'image':4,'review_date':'2026-11-05','status':'ACCEPTED_FOR_SEQUENTIAL_PRODUCTION','nbt_sha256':details['sha256'],'independent_checks':details,'deployment':'NOT_DEPLOYED_NOT_ACCEPTED_FOR_DEPLOYMENT','next_allowed':str(audit.BASE/f'image2.5/nature-dungeon-styles-20261104/S{group}-{style}/out/S{group}-05-corridor.png'),'next_independent_acceptance_required':True}
    assert Path(result['next_allowed']).is_file()
    if group==5:
        result.update(size=[35,20,43],manifest_entries_verified=91,composition='Reference,actual full-water exterior and full-source axial view reviewed: six thin freestanding columns,wide central aisle,north high arch and side arches,perimeter coral beds. Terracotta mosaics approximate flags,not banner entities. Aisle flat rather than raised is explicit. No earlier table/central lamp mound reused.',world_evidence_review=json.loads((folder/'world-independent.json').read_text(encoding='utf-8')),integration='Source-water ring8 plus cover8 aboveY20..27,solid bedY0..2,4wet doors with supported3deep extensions mandatory;independently rechecked all4 settled captures. NONE required envelopeX-8..42,Z-8..50,Y0..27;closed artificial tank or proper source-water ocean,never drycorridors.',scope='Independent readback of producer4x1200tick isolatedForge evidence including12x104430point captures/source30100point mapping;not another server execution,natural ocean/gameplay deployment or unlimited-time guarantee.')
    else:
        result.update(size=[27,17,37],manifest_entries_verified=87,composition='Reference,actual interior and raised-arch oblique source-crop viewed: six columns,long central aisle,sealed side lava channels,red/black stone drapes and thick stepped far arch retained. Stone drapes are not cloth banner entities;archandcrack detail simplified.',scope='16selected static survival points,18offline body-route targets and4ports,999roof columns,exactcutaway/source-crop andsealedstaticboundary. World ticks/fire/interaction/dynamiclighting andruntime rotations NOT_RUN.')
    (ROOT/f'S{group}-04-ACCEPTANCE.json').write_text(json.dumps(result,ensure_ascii=False,indent=2),encoding='utf-8')
print('S5-04 and S6-04 accepted records ready for explicit dispatch')
