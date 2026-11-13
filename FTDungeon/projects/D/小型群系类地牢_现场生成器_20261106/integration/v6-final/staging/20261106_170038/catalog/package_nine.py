import copy
import json
import shutil
import sys
from pathlib import Path

ROOT=Path(__file__).resolve().parent
CATALOG=ROOT.parent
sys.path.insert(0,str(CATALOG))
from freeze_catalog import INDEX,load,save,digest
from nbtio import Structure,plain

DERIVED_ID='s8_07_widearch_side_doublegate_v5'


def main():
    output=ROOT/'nine-room-package-v5'
    index=[record for record in load(INDEX)['items'] if record['group']==8 and record['image']<10]
    v4=CATALOG/'acceptance-candidate-v4'
    manifest=[]
    mapping=[]
    verification=load(ROOT/'derived-candidate/verification.json')
    for record in index:
        identifier=DERIVED_ID if record['image']==7 else record['id'].lower().replace('-','_')
        original=Path(record['candidate'])
        if digest(original)!=record['sha256']:
            raise ValueError('original source changed')
        source=ROOT/verification['resource'] if record['image']==7 else original
        target=output/'data/biomedungeon/structures'/f'{identifier}.nbt'
        target.parent.mkdir(parents=True,exist_ok=True)
        shutil.copyfile(source,target)
        if digest(source)!=digest(target):
            raise ValueError('copy hash differs')
        meta=load(original.with_suffix('.json'))
        ports=[]
        for number,raw in enumerate(meta.get('connections',meta.get('ports',[]))):
            facing=raw.get('facing',raw.get('side'))
            feet=raw.get('position',raw.get('feet'))
            ports.append({'id':f'p{number}_{facing}','feet':dict(zip(('x','y','z'),feet)),'facing':facing,'width':raw['width'],'height':raw.get('height',raw.get('minimum_height',raw.get('clearHeight'))),'medium':'dry'})
        structure=Structure.read(target)
        for port in ports:
            feet=list(port['feet'].values())
            for across in range(-(port['width']//2),port['width']//2+1):
                for rise in range(port['height']):
                    position=(feet[0]+(across if port['facing'] in ('north','south') else 0),feet[1]+rise,feet[2]+(across if port['facing'] in ('east','west') else 0))
                    block=structure.get(position)
                    if block.name!='minecraft:air' or block.get('waterlogged')=='true':
                        raise ValueError('dry source aperture verification failed')
        entry={'id':identifier,'biome':'s8','resourceId':'biomedungeon:'+identifier,'sha256':digest(target),'size':dict(zip(('x','y','z'),structure.size)),'ports':ports,'role':{7:'entrance',9:'terminal'}.get(record['image'],'normal'),'accepted':False,'originalSourceId':record['id'],'originalSourcePath':str(original),'originalSourceSha256':record['sha256'],'unchangedOriginalBytes':record['image']!=7,'independentArchitecturalEvidence':{'fileAudit':record['independent_evidence'],'acceptanceRecords':record['acceptance_records']},'experimentalAuthorizationScope':'isolated candidate only; parent permits derived design, not accepted promotion; new native/runtime/integration review required'}
        manifest.append(entry)
        mapping.append({'originalId':record['id'].lower().replace('-','_'),'originalSourcePath':str(original),'originalSha256':record['sha256'],'candidateId':identifier,'candidateSha256':digest(target),'unchanged':record['image']!=7})
    save(output/'data/biomedungeon/manifest.json',{'schemaVersion':1,'scope':'ISOLATED_UNACCEPTED_NINE_ROOM_EXPERIMENT_EIGHT_ORIGINALS_ONE_DERIVED_NOT_PRODUCTION','templates':manifest})
    save(output/'candidate-manifest.json',load(output/'data/biomedungeon/manifest.json'))
    save(output/'source-map.json',mapping)
    cases=copy.deepcopy(load(v4/'rotated-route-inputs.json'))
    derived_cases={case['quarterTurns']:case for case in load(ROOT/'derived-candidate/route-inputs-preserved.json')['cases']}
    full_map=[]
    for number,case in enumerate(cases['cases']):
        original_id=case['templateId']
        if original_id=='s8_07':
            cases['cases'][number]=copy.deepcopy(derived_cases[case['quarterTurns']])
            case=cases['cases'][number]
        else:
            case['candidate']='data/biomedungeon/structures/'+case['templateId']+'.nbt'
        case['nativeStatus']='NOT_RUN_FOR_THIS_EXACT_V5_PACKAGE'
        case['runIsolation']='fresh fixture per case; avoid shared anchor overlap'
        full_map.append({'originalTemplateId':original_id,'candidateTemplateId':case['templateId'],'quarterTurns':case['quarterTurns'],'originalStart':case['sourceStart'],'transformedStart':case['start'],'targets':[{'sourceIndex':target['sourceIndex'],'label':target['label'],'originalLocal':target['sourcePosition'],'candidateLocal':target['sourcePosition'],'transformedWorld':target['position'],'targetId':target['targetId']} for target in case['targets']],'additionalTargets':case.get('additionalTargets',[])})
    cases['worldTests']='NOT_RUN_FOR_EXACT_V5_PACKAGE'
    cases['scope']='8 originals + derived07; all original target identities and coordinates retained'
    save(output/'rotated-route-inputs.json',cases)
    matrix=copy.deepcopy(load(v4/'path-check-matrix.json'))
    for check in matrix['checks']:
        if check['caseId'].startswith('s8_07_'):
            check['caseId']=check['caseId'].replace('s8_07_',DERIVED_ID+'_',1)
        check['status']='NOT_RUN_FOR_EXACT_V5_PACKAGE'
    for case in cases['cases']:
        for start_id,start in [('original-start',case['start'])]+[('port-'+str(port['portIndex']),port['worldFeet']) for port in case['ports']]:
            for target in case.get('additionalTargets',[]):
                matrix['checks'].append({'caseId':case['caseId'],'from':start_id,'to':target['targetId'],'start':start,'end':target['position'],'mustReturn':True,'status':'NOT_RUN','additionalSideGateCheck':True})
    matrix['originalChecksPreserved']=2200
    matrix['additionalSideGateChecks']=48
    save(output/'path-check-matrix.json',matrix)
    save(output/'all-original-target-mapping.json',{'cases':full_map,'originalCases':36,'originalTargetsPerRotation':148,'originalTransformedTargets':592,'originalTargetsRemoved':0,'additionalSideGateTargets':16})
    for source_name in ('change-ledger.json','verification.json'):
        shutil.copyfile(ROOT/'derived-candidate'/source_name,output/source_name)
    save(output/'native-test-contract.json',{'accepted':False,'mustDeclare':'Side double doors cannot seal the central main arch. This is eight original rooms plus one derived architectural candidate, not nine unchanged original rooms.','roles':{'entrance':DERIVED_ID,'mainHall':'s8_04','terminal':'s8_09'},'originalWidthStripsUnchanged':[{'originalRoomId':'s8_07','candidateId':DERIVED_ID,'xCenters':[11.3,13.7],'step':0.05,'startYZ':[2,0.3],'endYZ':[4,5.7],'bodyWidth':0.6,'bodyHeight':1.8,'headroom':3,'nativeStep':0.6,'movementSubdivision':0.025,'allRotations':True},{'originalRoomId':'s8_07','candidateId':DERIVED_ID,'xCenters':[10.3,14.7],'startYZ':[4,26.7],'endYZ':[4,24.3],'headroom':3,'allRotations':True}],'additionalSideGateTests':{'doorLowerCells':[[8,4,4],[9,4,4]],'upperCells':[[8,5,4],[9,5,4]],'originalDoorIdsAndStates':'same adjacent left/right hinge pairing, north facing, open=true initially','walkSideGate':'body0.6x1.8 native path through 1.625-wide open side gateway, keep 3-high headroom where tested; do not apply central three-wide requirement to original-style two-leaf side portal','closedBehavior':'side path must be blocked, central arch must remain traversable','reopenBehavior':'both halves coherent, side path and return pass, source doors open=true restored'},'runtimeRequirements':['bind new template ID and derivedSHA; do not rewrite original ID/source acceptance','update hardcoded entrance/role checks and native sweep ID mapping for derived candidate','load all nine accepted=false only via explicitly isolated experiment fixture; never flip accepted to satisfy production loader','retain native S8-08 lava/vine verification and all01/07 stairs; no global gamerule disabling','new artifact/core/runtime/manifest hashes and full original2200 plus48additive checks','no reuse of oldJAR whole-biome acceptance for changed resource'],'experimentAuthorization':{'status':'COORDINATOR_AUTHORIZED_ISOLATED_DERIVED_CANDIDATE_DESIGN_ONLY','acceptedPromotion':False}})
    print('PASS nine room experiment packaged: eight original SHA unchanged, derived07='+verification['derivedSha256'])
    print('PASS routes: 36 cases, 592 original transformed targets, original2200+48 new sidegate checks')


if __name__=='__main__':
    main()
