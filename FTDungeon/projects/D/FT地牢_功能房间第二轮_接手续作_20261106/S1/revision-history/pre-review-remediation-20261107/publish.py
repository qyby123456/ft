import hashlib
import json
import pathlib

ROOT=pathlib.Path(__file__).parent

def read(path):
    return json.loads(path.read_text(encoding='utf-8-sig'))

def save(path,value):
    path.write_text(json.dumps(value,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')

def sha(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()

def main():
    manifest=read(ROOT/'inheritance-manifest.json')
    changes=[entry['path'] for entry in manifest if sha(pathlib.Path(entry['path']))!=entry['sha256']]
    save(ROOT/'old-source-readonly-verification.json',{'files_verified':len(manifest),'changed_files':changes,'old_directory_unchanged':not changes})
    assert not changes
    merged=ROOT/'merged'
    candidate=merged/'s1-mossy-functional-11-20.nbt'
    digest=sha(candidate)
    integrity=read(merged/'integrity.json')
    render=read(merged/'render.json')
    check=read(merged/'self-check.json')
    routes=read(merged/'qa'/'independent-routes.json')[0]
    support=read(merged/'qa'/'independent-all-support.json')[0]
    native=read(merged/'qa'/'independent-native.json')[0]
    for evidence in [integrity,render,check]:
        assert evidence['source_sha256']==digest
    for evidence in [routes,support,native]:
        assert evidence['sha256']==digest
    assert routes['start_clear'] and all(target['reachable'] and target['standing_clear'] for target in routes['targets'])
    assert not support['failures'] and not support['unsupported']
    assert native['registry_properties']=='PASS' and native['template_roundtrip_block_count']=='PASS'
    assert integrity['all_block_states_equal'] and integrity['all_block_entity_payloads_equal'] and integrity['all_entity_payloads_equal']
    assert read(ROOT/'reproduction-report.json')['all_ten_byte_identical']
    assert not read(ROOT/'risk-summary.json')['findings']
    for chunk in integrity['chunks']:
        assert sha(pathlib.Path(chunk['file']))==chunk['sha256'] and max(chunk['size'])<=48
    delivery=read(ROOT/'delivery-index.json')
    for source,final in zip(integrity['rooms'],delivery['rooms']):
        assert source['id']==final['id'] and source['sha256']==final['sha256']
    limits=['Independent visual review and group acceptance pending.','No actual world placement, neighbor updates, gravity, lighting, fluid/random ticks, interactions or combat tests.','Door buttons and decorative levers are not certified mechanisms; delivered iron doors are open.','Armor stand equipment is verified in NBT; Lodestone cannot display these entities.','No ladder climbing or fence jumping physics certification.','Ponds are real water; no natural-tick or drowning certification.','Merged structure is an enclosed access gallery with ten preserved room branches; outside barrier is not a deployment or combat adaptation.']
    submission={'style':'S1','range':'11-20','status':'ready_for_independent_review_not_accepted','independent_acceptance':False,'review_thread':'01a1a685-045b-76d2-aa2b-2a3f24520603','source_directory':str(ROOT),'rooms':delivery['rooms'],'merged':{'candidate':str(candidate),'sha256':digest,'size':[492,34,74],'chunk_count':integrity['chunk_count'],'reassembled_records':integrity['reassembled_records'],'route_targets':len(routes['targets']),'all_route_targets_reachable':True,'nonair_support_checked':support['nonair_checked'],'support_failures':0,'support_unsupported':0,'integrity':str(merged/'integrity.json'),'render':str(merged/'render.json'),'qa':str(merged/'qa')},'reproduction':str(ROOT/'reproduction-report.json'),'inheritance':str(ROOT/'inheritance-manifest.json'),'old_source_verification':str(ROOT/'old-source-readonly-verification.json'),'risk_qa':str(ROOT/'risk-summary.json'),'limitations':limits,'written_to_formal_world':False}
    save(ROOT/'ready-for-review.json',submission)
    save(ROOT/'handoff-for-independent-review.json',submission)
    save(merged/'visual-review.json',{'source_sha256':digest,'reviewer':'maker_agent_not_independent','viewed':['complete.png','cutaway.png','atlas-11-13.png','atlas-14-17.png','atlas-18-20.png','connector-watch.png'],'status':'maker_visual_review_complete_waiting_independent_review','display_only_hidden_ranges':True,'limitations':limits})
    progress=read(ROOT/'progress.json')
    progress['merge']='maker_QA_pass_ready_for_independent_review'
    progress['current']='delivery_complete_pending_independent_review'
    progress['independent_acceptance']=False
    progress['merged_sha256']=digest
    progress['merged_route_targets']=len(routes['targets'])
    progress['remaining_checks']=limits
    save(ROOT/'progress.json',progress)
    print('Published independent-review handoff. Routes:',len(routes['targets']),'support states:',support['nonair_checked'],'old unchanged files:',len(manifest))

if __name__=='__main__':
    main()
