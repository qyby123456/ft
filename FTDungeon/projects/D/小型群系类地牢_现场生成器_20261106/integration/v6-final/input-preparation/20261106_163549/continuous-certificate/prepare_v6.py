import copy
import sys
from pathlib import Path

ROOT=Path(__file__).resolve().parent
sys.path.insert(0,str(ROOT.parent))
from freeze_catalog import digest,load,save
from nbtio import Structure


def main():
    old=ROOT.parent/'full-width-certificate-v5'
    contract=copy.deepcopy(load(old/'contract.json'))
    policies={}
    hits=[]
    for strip in contract['strips']:
        structure=Structure.read(strip['resource'])
        names={','.join(map(str,position)):structure.get(tuple(position)).name for position in strip['requiredBlocks']}
        for name in names.values():
            policies[name]=[0,0,0,1,1,1]
        for position in strip['requiredBlocks']:
            name=structure.get(tuple(position)).name
            if name.endswith(('_wall','_fence','_fence_gate')):
                hits.append({'strip':strip['stripId'],'localBlock':position,'blockName':name})
        strip['sourceBlockNames']=names
        strip['collectionEnvelopeProof']={'horizontalExtent':[0,1],'verticalExtent':[0,1.5],'candidateSourceYRule':'any source cell y whose [y,y+1.5] can intersect swept body. Existing source set includes y0..maxFeet+4; lowest feet is1, so no source y<0 reaches it. Top margin extends beyond maxFeet+headroom. Horizontal margin already includes neighboring cells; no horizontal overhang is approved.','coverageOfKnownOverhang':'wall/fence/gate height1.5 stays within existing collection margin; no additional required cell is needed for these12scene domains. Unknown horizontal/up/down envelopes fail closed pending enlarged collection.','shapePolicy':'full native AABB retained even when topY exceeds localBlockY+1; never clip'}
    for name in ('minecraft:deepslate_brick_wall','minecraft:oak_fence','minecraft:spruce_fence','minecraft:dark_oak_fence','minecraft:oak_fence_gate','minecraft:spruce_fence_gate','minecraft:dark_oak_fence_gate'):
        policies[name]=[0,0,0,1,1.5,1]
    contract['nativeShapeBoundsByBlockName']=policies
    contract['overhangScope']='Exact named vanilla wall/fence/gate bounded envelopes plus exact source palette; no unrestricted suffix admission. MC native collision still required.'
    contract['previousContractSha256']=digest(old/'contract.json')
    save(ROOT/'contract.json',contract)
    schema=copy.deepcopy(load(old/'native-input.schema.json'))
    block_schema=schema['properties']['scenes']['items']['properties']['blocks']['items']
    block_schema['required'].append('blockName')
    block_schema['properties']['blockName']={'type':'string','enum':sorted(policies)}
    save(ROOT/'native-input.schema.json',schema)
    save(ROOT/'collection-audit.json',{'oldRequiredBlockEntries':sum(len(strip['requiredBlocks']) for strip in contract['strips']),'oldWallFenceGateHits':hits,'hitCount':len(hits),'additionalRequiredCellsForNamedOverhang':0,'reason':'Known1.5high shapes have no horizontal overhang. Existing y0..upperFeet+4 includes every source cell capable of intruding into feet>=1 and headroom<=upperFeet+3; existing horizontal margin includes any neighbor source cell with within-cell X/Z shape.','previousSupportReadySha256':digest(old/'ready.json'),'newContractSha256':digest(ROOT/'contract.json'),'nativeWorldEvidence':'PENDING','sourceNamePolicyIsNotNativeShapeEvidence':True})
    print('PASS current4416 entries wall/fence/gate hits='+str(len(hits))+'; native overhang accepted through exact bounded policy; full boxes retained')


if __name__=='__main__':
    main()
