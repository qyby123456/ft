import json
from pathlib import Path
import audit

ROOT=Path(__file__).resolve().parent
cases=[(2,4,'S2-04-independent','sandstone-desert','Reference and actual north/south interior cuts reviewed: six sandstone columns, long diamond-pattern floor, two side aisles, wall arches, hanging lamps and orange bands. Nine banner entities retain explicit patterns even when renderer does not show them. Front portion intentionally open; not certified as full roof.'),(7,5,'S7-05-independent','fungal-grove','Reference and complete-source eye-level view reviewed: straight unbranched fungus corridor, repeated mushroom-stem lamp columns, small wall/base crystals and shallow edge pools. Brighter/checkered vanilla decoration acknowledged; no purple fog or concept-art glow guarantee.'),(8,5,'S8-05-independent','crystal-geode','Reference and complete-source eye-level image reviewed: straight unbranched crystalline corridor, regular white/purple wall piers, hanging lanterns and corner clusters. Hanging crystals and vault curvature are coarser than concept; complete roof is present, not a cutaway masquerading as source.')]
for group,image,label,style,composition in cases:
    folder=ROOT/label
    details=json.loads((folder/'independent-details.json').read_text(encoding='utf-8'))
    files=json.loads((folder/'file-audit.json').read_text(encoding='utf-8'))['groups'][0]
    assert audit.digest(Path(files['candidate']))==details['sha256']
    next_image=image+1
    next_name=f'S{group}-{next_image:02d}-'+('corridor.png' if next_image==5 else 'corner.png')
    next_path=audit.BASE/f'image2.5/nature-dungeon-styles-20261104/S{group}-{style}/out'/next_name
    assert next_path.is_file()
    result={'group':group,'image':image,'review_date':'2026-11-05','status':'ACCEPTED_FOR_SEQUENTIAL_PRODUCTION','nbt_sha256':details['sha256'],'size':details['size'],'composition':composition,'independent_checks':details,'verified_manifest_entries':[item['checked'] for item in files['manifests'] if item['checked']],'scope':'Selected static survival and offline player-body routes only. Producer all-point or rotated tests separately scoped. World ticks,actual light/interaction and gameplay NOT_RUN.','next_allowed':str(next_path),'next_independent_acceptance_required':True,'deployment':'NOT_DEPLOYED_NOT_ACCEPTED_FOR_DEPLOYMENT'}
    (ROOT/f'S{group}-{image:02d}-ACCEPTANCE.json').write_text(json.dumps(result,ensure_ascii=False,indent=2),encoding='utf-8')
print('3 accepted records written; dispatch remains explicit tool action')
