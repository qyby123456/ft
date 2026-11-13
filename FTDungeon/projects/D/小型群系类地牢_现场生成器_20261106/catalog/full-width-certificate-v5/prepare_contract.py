import hashlib
import json
import sys
from pathlib import Path

ROOT=Path(__file__).resolve().parent
CATALOG=ROOT.parent
sys.path.insert(0,str(CATALOG))
from freeze_catalog import digest,load,save


def main():
    package=CATALOG/'s8-07-doorway-review-v5/nine-room-package-v5'
    manifest=load(package/'candidate-manifest.json')
    selected={template['id']:template for template in manifest['templates']}
    strips=[]
    profiles=[('s8_01',[16.3,18.7],[0.3,5.7],1,3,[(0,1),(1,1.5),(1.5,2),(2,2.5),(2.5,3)]),('s8_07_widearch_side_doublegate_v5',[11.3,13.7],[0.3,5.7],2,4,[(0,2),(2,2.5),(2.5,3),(3,3.5),(3.5,4)]),('s8_07_widearch_side_doublegate_v5',[10.3,14.7],[24.3,26.7],4,4,[(24,4)])]
    for profile_index,(identifier,x_band,z_band,lower,upper,expected_steps) in enumerate(profiles):
        template=selected[identifier]
        for turns in range(4):
            # Conservative one-cell extra horizontal margin and vertical support/headspace.
            minimum=[int(x_band[0]-.3)-1,0,int(z_band[0]-.3)-1]
            maximum=[int(x_band[1]+.3)+2,upper+5,int(z_band[1]+.3)+2]
            size=list(template['size'].values())
            positions=[[position_x,position_y,position_z] for position_x in range(max(0,minimum[0]),min(size[0],maximum[0])) for position_y in range(max(0,minimum[1]),min(size[1],maximum[1])) for position_z in range(max(0,minimum[2]),min(size[2],maximum[2]))]
            strips.append({'stripId':identifier+f'_profile{profile_index}_r{turns}','templateId':identifier,'resourceSha256':template['sha256'],'resource':str(package/'data/biomedungeon/structures'/f'{identifier}.nbt'),'quarterTurns':turns,'centerX':x_band,'centerZ':z_band,'startFeetY':lower,'endFeetY':upper,'minFeetY':lower,'maxFeetY':upper,'bodyWidth':0.6,'bodyHeight':1.8,'headroom':3,'nativeStepHeight':0.6,'requiredBlocks':positions,'sourceSurfaceHintsOnly':{'surfaceZStartAndTopY':expected_steps,'expectedForwardCenterRiseZ':[round(pair[0]-.3,6) for pair in expected_steps[1:]],'purpose':'hints for native collection/step boundary replay only; NOT collision input or native proof'},'coordinateRule':'collect actual rotated world geometry then inverse-transform all box corners into original local axes. localBlock is original integer cell; boxes are absolute local positions (not cell-relative). Verify source states vs native rotated states and resourceSHA.','requiredNativeReplay':'both directions at every emitted support discontinuity; original complete strip endpoints and unchanged full width; support-envelope alone is not native step execution'})
    contract={'schemaVersion':1,'scope':'finite full-width static native-box coverage with native step boundary evidence still required','strips':strips,'nativeReturnPath':'integration writes native-shapes.json under integration ownership and invokes catalog script read-only; do not write into this frozen catalog support directory','exactness':'native box coordinates must be lossless decimal/rational grid values; Python arithmetic converts decimal strings to rational numbers; no epsilon inflation or shrink of requirements','excludedSideDoor':'side doublegate separately uses original narrow door route/interaction targets; cannot close central arch','frozenV5ReadySha256':digest(CATALOG/'s8-07-doorway-review-v5/ready.json')}
    save(ROOT/'contract.json',contract)
    box_schema={'type':'array','minItems':6,'maxItems':6,'items':{'type':'number'}}
    block_schema={'type':'object','required':['localBlock','nativeState','fluidEmpty','boxes'],'properties':{'localBlock':{'type':'array','minItems':3,'maxItems':3,'items':{'type':'integer'}},'nativeState':{'type':'string'},'fluidEmpty':{'const':True},'boxes':{'type':'array','items':box_schema}}}
    scene_schema={'type':'object','required':['stripId','resourceSha256','nativeContext','blocks'],'properties':{'stripId':{'type':'string'},'resourceSha256':{'type':'string','pattern':'^[a-f0-9]{64}$'},'nativeContext':{'const':'PLAYER_STANDING_COLLISION_CONTEXT'},'blocks':{'type':'array','items':block_schema}}}
    native_schema={'$schema':'https://json-schema.org/draft/2020-12/schema','title':'Native shape collection for catalog finite interval verifier','type':'object','required':['schemaVersion','collectionMode','contractSha256','coverageComplete','scenes'],'properties':{'schemaVersion':{'const':1},'collectionMode':{'const':'WORLD_NATIVE_VOXELSHAPE'},'contractSha256':{'type':'string','pattern':'^[a-f0-9]{64}$'},'coverageComplete':{'const':True},'scenes':{'type':'array','items':scene_schema}}}
    save(ROOT/'native-input.schema.json',native_schema)
    scene_schema['required'].append('quarterTurns')
    scene_schema['properties']['quarterTurns']={'type':'integer','minimum':0,'maximum':3}
    save(ROOT/'native-input.schema.json',native_schema)
    save(ROOT/'collection-summary.json',{'contractSha256':digest(ROOT/'contract.json'),'strips':len(strips),'requiredBlockEntries':sum(len(strip['requiredBlocks']) for strip in strips),'uniqueLocalCellsPerProfile':[{ 'profile':strip['stripId'],'count':len(strip['requiredBlocks'])} for strip in strips],'nativeWorldEvidence':'NOT_PROVIDED_YET','certificateProduced':'NONE_PENDING_NATIVE_DATA'})
    print('PASS prepared12strip contracts; native collision collection pending; contractSHA256='+digest(ROOT/'contract.json'))


if __name__=='__main__':
    main()
