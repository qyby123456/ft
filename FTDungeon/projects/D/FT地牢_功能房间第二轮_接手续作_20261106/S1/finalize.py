import hashlib
import json
import pathlib
import sys

ROOT = pathlib.Path(__file__).parent
sys.path.insert(0, r'C:\Users\YOUR_USERNAME\AppData\Roaming\.minecraft\versions\FarmingTales_ForgeⅡ v1.5.2\output\dungeon-expansion-20261101\tools')
from nbtio import Structure, plain

def sha(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()

def save(path, value):
    path.write_text(json.dumps(value, ensure_ascii=False, indent=2)+'\n', encoding='utf-8')

def main():
    progress = json.loads((ROOT/'progress.json').read_text(encoding='utf-8'))
    references = json.loads((ROOT/'references.json').read_text(encoding='utf-8'))['references']
    findings = []
    entries = []
    for folder in sorted(ROOT.glob('[12][0-9]-*')):
        design = json.loads((folder/'design.json').read_text(encoding='utf-8'))
        reference = next(reference for reference in references if reference['id']==design['id'])
        assert sha(pathlib.Path(reference['image_path']))==reference['sha256']
        candidate=next(folder.glob('*.nbt'))
        digest=sha(candidate)
        native=json.loads((folder/'qa'/'independent-native.json').read_text(encoding='utf-8'))[0]
        routes=json.loads((folder/'qa'/'independent-routes.json').read_text(encoding='utf-8'))[0]
        support=json.loads((folder/'qa'/'independent-all-support.json').read_text(encoding='utf-8'))[0]
        render=json.loads((folder/'render.json').read_text(encoding='utf-8'))
        for report in [native,routes,support]:
            assert report['sha256']==digest
        assert render['source_sha256']==digest
        assert not support['failures'] and not support['unsupported']
        assert native['registry_properties']=='PASS' and not native['survival_failures']
        assert all(target['reachable'] and target['standing_clear'] for target in routes['targets'])
        structure=Structure.read(candidate)
        water=[]
        leaks=[]
        for position,block in structure.blocks.items():
            if block.name!='minecraft:water':
                continue
            water.append(position)
            for offset in [(0,-1,0),(-1,0,0),(1,0,0),(0,0,-1),(0,0,1)]:
                neighbor=tuple(coordinate+delta for coordinate,delta in zip(position,offset))
                if structure.get(neighbor).name in ['minecraft:air','minecraft:cave_air','minecraft:void_air']:
                    leaks.append({'water':position,'exposed_neighbor':neighbor})
        world_path=ROOT/'worldqa-run4'/'world-report.json'
        executed=world_path.exists()
        risk={'source_sha256':digest,'water_blocks':len(water),'direct_air_flow_candidates':leaks,'fluid_tick_executed':executed,'world_evidence':str(world_path) if executed else None,'warnings':['World evidence scope: 1200 ServerLevel ticks, forced chunks, explicit neighbor/fluid scheduling; no human gameplay or per-cell random-tick coverage claim.','Buttons and containers exercised with FakePlayer use; vault lever remains decorative and unwired.','Lodestone does not render armor stand entities; runtime count and NBT equipment evidence are separate.','Fixed gold blocks are recoverable resources unless later deployment protects them.','No client rendering, drowning or combat certification.']}
        save(folder/'risk-qa.json',risk)
        if leaks:
            findings.append({'room':design['id'],'kind':'water_exposed_to_air','count':len(leaks)})
        viewed=['complete.png','cutaway.png','function-detail.png']
        if design['id']=='S1-19':
            viewed.append('watch-desk-detail.png')
        for image in viewed:
            assert (folder/image).is_file()
        record={'source_sha256':digest,'reviewer':'maker_agent_not_independent','status':'maker_visual_review_complete_waiting_independent_review','reference_sha256':reference['sha256'],'prompt_sha256':sha(pathlib.Path(reference['prompt_path'])),'viewed':viewed,'notes':design['reference_features'],'differences':design['differences'],'image_hashes':{image:sha(folder/image) for image in viewed}}
        save(folder/'visual-review.json',record)
        check=json.loads((folder/'self-check.json').read_text(encoding='utf-8'))
        assert check['source_sha256']==digest
        check.update(native_all_nonair_survival=True,nonair_checked=support['nonair_checked'])
        check['scope_limits']=['Full nonair canSurvive with real tags and brightness 15 is static support only.','Route shapes use EmptyBlockGetter; no ladder, jump or movement physics certification.','No actual world lighting, neighbor, fluid, random ticks or interactions.']
        if executed:
            check['separate_runtime_evidence']=str(world_path)
            check['not_executed']=['human client GUI and keyboard gameplay','combat','independent visual acceptance','per-cell random tick coverage certification']
            check['scope_limits']=['This file is static QA; separate world-report.json records actual ServerLevel ticks and interactions.','Static routes use EmptyBlockGetter; separate walk-report.json uses actual ServerLevel and Entity.move.','No client, drowning, jump, ladder or combat certification.']
        save(folder/'self-check.json',check)
        entry={'id':design['id'],'candidate':str(candidate),'sha256':digest,'ports':design['ports'],'routes':len(routes['targets']),'support_nonair':support['nonair_checked'],'recipe':str(folder/'recipe.mjs'),'reference':reference['image_path'],'qa_directory':str(folder/'qa'),'images':viewed,'maker_static':'passed','maker_visual':'reviewed_with_differences','independent_acceptance':False}
        entries.append(entry)
        progress['rooms'][design['id']].update(candidate=str(candidate),sha256=digest,visual_review=record['status'],maker_static_self_check='passed',next='independent review pending')
    save(ROOT/'risk-summary.json',{'findings':findings,'serverlevel_1200_ticks_executed':(ROOT/'worldqa-run4'/'world-report.json').exists(),'runtime_report':str(ROOT/'worldqa-run4'/'world-report.json'),'independent_acceptance':False})
    save(ROOT/'delivery-index.json',{'style':'S1','rooms':entries,'independent_acceptance':False,'inherited_evidence':'inheritance-manifest.json'})
    progress['current']='ten rooms final; merge QA pending'
    save(ROOT/'progress.json',progress)
    print('Verified ten current hashes, reports, reference hashes and renders. Water air exposure findings:',findings)

if __name__=='__main__':
    main()
