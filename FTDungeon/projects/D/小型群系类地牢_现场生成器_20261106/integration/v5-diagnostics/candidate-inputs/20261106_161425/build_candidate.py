import copy
import sys
from pathlib import Path

ROOT=Path(__file__).resolve().parent
sys.path.insert(0,str(ROOT.parent))
from freeze_catalog import load,save,digest
from nbtio import Structure,state,plain


def main():
    review=load(ROOT/'geometry-review.json')
    plan=load(ROOT/'repair-proposal.json')
    source=Structure.read(review['source'])
    derived=copy.deepcopy(source)
    for edit in plan['edits']:
        position=tuple(edit['position'])
        if plain(source.get(position).as_tag())!=edit['before']:
            raise ValueError('before mismatch')
        after=edit['after']
        derived.set(position,state(after['Name'],**after.get('Properties',{})))
    output=ROOT/'derived-candidate/data/biomedungeon/structures/s8_07_widearch_side_doublegate_v5.nbt'
    derived.write(output)
    reread=Structure.read(output)
    original_doors=sorted((position,block) for position,block in source.blocks.items() if block.name.endswith('_door'))
    candidate_doors=sorted((position,block) for position,block in reread.blocks.items() if block.name.endswith('_door'))
    if len(original_doors)!=4 or len(candidate_doors)!=4:
        raise ValueError('door count not preserved')
    for position,block in original_doors:
        if reread.get((position[0]-3,position[1],position[2]))!=block:
            raise ValueError('paired doors not translated intact')
    for position,block in source.blocks.items():
        if block.name.endswith('_stairs') and reread.get(position)!=block:
            raise ValueError('stair changed')
        if position[1]<=3 and reread.get(position)!=block:
            raise ValueError('floor or height changed')
    if {position:plain(tag) for position,tag in source.block_entities.items()}!={position:plain(tag) for position,tag in reread.block_entities.items()}:
        raise ValueError('BE changed')
    if len(source.blocks)!=len(reread.blocks) or source.size!=reread.size:
        raise ValueError('volume changed')
    differences=[position for position,block in source.blocks.items() if reread.get(position)!=block]
    if set(differences)!={tuple(edit['position']) for edit in plan['edits']}:
        raise ValueError('unplanned edits')
    # All required original lane body envelopes fit the central arch above the platform.
    for position_x in (11,12,13):
        for position_y in (4,5,6):
            for position_z in (4,5):
                if reread.get((position_x,position_y,position_z)).name!='minecraft:air':
                    raise ValueError('central arch not clear')
    # Side doorway: retain original native door state orientation; open gap X8.1875..9.8125.
    for position_x in (8,9):
        for position_y in (4,5,6):
            if reread.get((position_x,position_y,5)).name!='minecraft:air':
                raise ValueError('side back path obstructed')
        if reread.get((position_x,3,4)).name not in {'minecraft:polished_deepslate','minecraft:amethyst_block'}:
            raise ValueError('side door missing floor')
    candidate_sha=digest(output)
    plan['status']='DERIVED_REVIEW_CANDIDATE_CREATED_UNACCEPTED'
    plan['derivedSha256']=candidate_sha
    plan['derivedHashReason']='Actual deterministic NBT bytes generated under derived-candidate; original remains unchanged.'
    save(ROOT/'repair-proposal.json',plan)
    evidence={'status':'OFFLINE_CHECKS_PASS_NATIVE_ACCEPTANCE_PENDING','accepted':False,'originalSha256':digest(review['source']),'derivedSha256':candidate_sha,'resource':output.relative_to(ROOT).as_posix(),'changeCount':len(differences),'explicitCells':len(reread.blocks),'size':list(reread.size),'preservedBE':len(source.block_entities),'preservedOriginalDoorHalves':len(original_doors),'stairCellsPreserved':sum(block.name.endswith('_stairs') for block in source.blocks.values()),'allCellsY0Through3Unchanged':True,'allOriginalTargetsPreserved':load(ROOT/'target-audit.json'),'nativeWorldRoutes':'NOT_RUN','offlineGeometryScope':'central arch cells and side return doorway support verified; original stair movement requires native re-run, no fabricated native pass','pairBehavior':'original adjacent left/right door states relocated together; can open/close side doorway; central arch remains open when side pair closes','requiredIndependentChecks':['original112 failing lanes four rotations with unchanged3-high sweep','all original36cases2200routes plus new side doorway route/open-close targets','side-door native canSurvive and coherent lower/upper interaction','all source furniture/BE and stairs readback','visual architectural approval of main arch/side doublegate design','exact new runtime/JAR/resource manifest binding and restart readback']}
    save(ROOT/'derived-candidate/verification.json',evidence)
    save(ROOT/'derived-candidate/change-ledger.json',{'originalSha256':evidence['originalSha256'],'derivedSha256':candidate_sha,'edits':plan['edits']})
    manifest=load(ROOT.parent/'expansion-v3/candidate-manifest.json')
    template=copy.deepcopy(next(template for template in manifest['templates'] if template['id']=='s8_07'))
    template.update({'id':'s8_07_widearch_side_doublegate_v5','resourceId':'biomedungeon:s8_07_widearch_side_doublegate_v5','sha256':candidate_sha,'accepted':False,'originalSha256':evidence['originalSha256'],'changeLedger':'change-ledger.json','nativeAcceptance':'PENDING','role':'entrance'})
    template.pop('expansionBlockers',None)
    save(ROOT/'derived-candidate/candidate-manifest.json',{'schemaVersion':1,'scope':'UNACCEPTED_DERIVED_REPAIR_FOR_ISOLATED_REVIEW_ONLY','templates':[template]})
    routes=load(ROOT.parent/'acceptance-candidate-v4/rotated-route-inputs.json')
    selected=copy.deepcopy([case for case in routes['cases'] if case['templateId']=='s8_07'])
    from importlib.util import spec_from_file_location,module_from_spec
    spec=spec_from_file_location('v4_transform',ROOT.parent/'acceptance-candidate-v4/prepare_v4.py')
    transform_module=module_from_spec(spec)
    spec.loader.exec_module(transform_module)
    for case in selected:
        case['originalSourceSha256']=case['sourceSha256']
        case['sourceSha256']=candidate_sha
        case['originalTemplateId']='s8_07'
        case['templateId']=template['id']
        case['candidate']='data/biomedungeon/structures/'+template['id']+'.nbt'
        case['caseId']=template['id']+'_r'+str(case['quarterTurns'])
        case['geometryStatus']='INPUT_ONLY_NOT_EXECUTED'
        case['additionalTargets']=[{'targetId':name,'sourcePosition':position,'position':transform_module.transform(position,case['quarterTurns'],case['origin']),'mustReturn':True,'purpose':'new side-pair interaction path; additive, never replaces original16 targets'} for name,position in [('sidegate-front',[9,4,3.5]),('sidegate-threshold',[9,4,4.5]),('sidegate-rear',[9,4,5.5]),('sidegate-rejoin',[9,4,6.5])]]
    save(ROOT/'derived-candidate/route-inputs-preserved.json',{'schemaVersion':1,'cases':selected,'originalTargetsPerRotation':16,'targetsRemoved':0,'additionalSideGateTargetsPerRotation':4})
    if digest(review['source'])!=review['sourceSha256']:
        raise ValueError('original modified')
    print('PASS actual derivedSHA256='+candidate_sha+' exactChanges='+str(len(differences))+' BE='+str(len(source.block_entities)))
    print('PASS original stairs, floors, paired doors, all16original targets per rotation preserved; nativeNOT_RUN accepted=false')


if __name__=='__main__':
    main()
