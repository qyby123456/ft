import hashlib
import json
import os
from pathlib import Path
import subprocess
import sys

sys.dont_write_bytecode = True
ROOT = Path(__file__).resolve().parent
OUTPUT = ROOT / 'qa/interaction-range-current'
sys.path.insert(0, 'C:/Users/YOUR_USERNAME/AppData/Roaming/.minecraft/versions/FarmingTales_ForgeⅡ v1.5.2/output/dungeon-expansion-20261101/tools')
from nbtio import Structure, plain


def read(path):
    return json.loads(path.read_text(encoding='utf-8'))


def write(path, value):
    path.write_text(json.dumps(value, ensure_ascii=False, indent=2), encoding='utf-8')


def digest(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()


def main():
    OUTPUT.mkdir(parents=True, exist_ok=True)
    source = ROOT / 'assembly/s2-desert-functional-combined.nbt'
    after_path = ROOT / 'runtime-assembly-r1/assembly-after.nbt'
    original = Structure.read(source)
    after = Structure.read(after_path)
    runtime = read(ROOT / 'runtime-assembly-r1/assembly-runtime.json')
    assert digest(source) == runtime['source_sha256']
    fixtures = []
    for container in runtime['containers']:
        position = container['position']
        fixtures.append({'label': 'container-' + '-'.join(map(str, position)), 'kind': 'container', 'position': position, 'block': original.get(tuple(position)).name})
    for door in runtime['doors']:
        east, up, south = door['position']
        matches = [position for position, block in original.blocks.items() if block.name == 'minecraft:lever' and abs(position[0]-east) <= 1 and position[1] == up+1 and abs(position[2]-south) <= 1]
        assert len(matches) == 1
        position = matches[0]
        fixtures.append({'label': 'door-lever-' + '-'.join(map(str, position)), 'kind': 'door_lever', 'position': position, 'door_position': door['position'], 'block': 'minecraft:lever'})
    assert len(fixtures) == 40
    specification = read(ROOT / 'assembly/qa/route-input.json')[0]
    inputs = []
    for label, candidate in [('current_source', source), ('real_server_posttick', after_path)]:
        inputs.append({**specification, 'candidate': str(candidate), 'sha256': digest(candidate), 'snapshot_label': label, 'fixtures': fixtures})
    write(OUTPUT / 'route-input.json', inputs)
    java = Path('D:/ft工程文件/FT地牢_独立复核_20261104_01a1a685/IndependentRouteAudit.java').read_text(encoding='utf-8')
    java = java.replace('public final class IndependentRouteAudit', 'public final class ProducerInteractionAudit')
    java = java.replace('0.299', '0.300001').replace('1.7999', '1.800001')
    java = java.replace('private static Map<BlockPos, Cell> cells;', 'private static Map<BlockPos, Cell> cells; private static Map<BlockPos,BlockState> worldStates;')
    java = java.replace('cells = new HashMap<>();', 'cells = new HashMap<>(); worldStates = new HashMap<>();')
    java = java.replace('String name = BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString();', 'String name = BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString(); if(!state.isAir())worldStates.put(position,state);')
    java = java.replace('Set<Node> visited = new HashSet<>();', 'Set<Node> visited = new HashSet<>(); Map<Node,Node> previous = new HashMap<>();')
    java = java.replace('queue.add(next);', 'queue.add(next); previous.put(next,current);')
    marker = '            reports.add(Map.of('
    assert marker in java
    addition = '''
            net.minecraft.world.level.BlockGetter getter = new net.minecraft.world.level.BlockGetter(){
                public BlockState getBlockState(BlockPos position){return worldStates.getOrDefault(position,net.minecraft.world.level.block.Blocks.AIR.defaultBlockState());}
                public net.minecraft.world.level.material.FluidState getFluidState(BlockPos position){return getBlockState(position).getFluidState();}
                public net.minecraft.world.level.block.entity.BlockEntity getBlockEntity(BlockPos position){return null;}
                public int getHeight(){return size[1];}
                public int getMinBuildHeight(){return 0;}
            };
            List<Object> interactionResults = new ArrayList<>();
            for(var fixtureElement:specification.getAsJsonArray("fixtures")){
                JsonObject fixture=fixtureElement.getAsJsonObject();var coordinate=fixture.getAsJsonArray("position");
                BlockPos target=new BlockPos(coordinate.get(0).getAsInt(),coordinate.get(1).getAsInt(),coordinate.get(2).getAsInt());
                BlockState state=getter.getBlockState(target);
                List<AABB> outlines=state.getShape(getter,target).toAabbs();
                List<Node> stations=visited.stream().filter(point -> {
                    double east=point.eastHalf/2.0-target.getX()-0.5, up=point.upHalf/2.0+1.62-target.getY()-0.5,south=point.southHalf/2.0-target.getZ()-0.5;
                    return east*east+up*up+south*south<36;
                }).sorted(java.util.Comparator.comparingDouble(point -> {
                    double east=point.eastHalf/2.0-target.getX()-0.5,up=point.upHalf/2.0+1.62-target.getY()-0.5,south=point.southHalf/2.0-target.getZ()-0.5;
                    return east*east+up*up+south*south;
                })).toList();
                Map<String,Object> evidence=new java.util.LinkedHashMap<>();evidence.put("fixture",fixture);evidence.put("pass",false);
                search:for(Node station:stations){
                    for(double offsetEast:new double[]{0,-0.19,0.19})for(double offsetSouth:new double[]{0,-0.19,0.19}){
                    double feetEast=station.eastHalf/2.0+offsetEast,feetUp=station.upHalf/2.0,feetSouth=station.southHalf/2.0+offsetSouth;
                    boolean fineStepClear=true;
                    for(int sample=0;sample<=10;sample++){
                        double fraction=sample/10.0,east=station.eastHalf/2.0+offsetEast*fraction,south=station.southHalf/2.0+offsetSouth*fraction;
                        if(!clear(body(east,feetUp,south))||!supported(east,feetUp,south)){fineStepClear=false;break;}
                    }
                    if(!fineStepClear)continue;
                    for(double jumpRise:fixture.get("label").getAsString().equals("container-197-11-93")?new double[]{0,0.9}:new double[]{0}){
                    if(!clear(body(feetEast,feetUp+jumpRise,feetSouth)))continue;
                    var eye=new net.minecraft.world.phys.Vec3(feetEast,feetUp+1.62+jumpRise,feetSouth);
                    for(AABB box:outlines){
                        double centerEast=(box.minX+box.maxX)/2,centerUp=(box.minY+box.maxY)/2,centerSouth=(box.minZ+box.maxZ)/2;
                        double[][] aims={{box.minX+0.001,centerUp,centerSouth},{box.maxX-0.001,centerUp,centerSouth},{centerEast,box.minY+0.001,centerSouth},{centerEast,box.maxY-0.001,centerSouth},{centerEast,centerUp,box.minZ+0.001},{centerEast,centerUp,box.maxZ-0.001}};
                        for(double[] aim:aims){
                            var end=new net.minecraft.world.phys.Vec3(target.getX()+aim[0],target.getY()+aim[1],target.getZ()+aim[2]);
                            if(eye.distanceTo(end)>4.5)continue;
                            net.minecraft.world.phys.BlockHitResult hit=null;double nearest=Double.POSITIVE_INFINITY;
                            for(BlockPos probe:BlockPos.betweenClosed(BlockPos.containing(Math.min(eye.x,end.x)-1,Math.min(eye.y,end.y)-1,Math.min(eye.z,end.z)-1),BlockPos.containing(Math.max(eye.x,end.x)+1,Math.max(eye.y,end.y)+1,Math.max(eye.z,end.z)+1))){
                                var candidateHit=getter.getBlockState(probe).getShape(getter,probe,net.minecraft.world.phys.shapes.CollisionContext.empty()).clip(eye,end,probe.immutable());
                                if(candidateHit!=null&&eye.distanceTo(candidateHit.getLocation())<nearest){hit=candidateHit;nearest=eye.distanceTo(candidateHit.getLocation());}
                            }
                            if(hit==null || !hit.getBlockPos().equals(target))continue;
                            List<Object> path=new ArrayList<>();for(Node cursor=station;cursor!=null;cursor=previous.get(cursor))path.add(List.of(cursor.eastHalf/2.0,cursor.upHalf/2.0,cursor.southHalf/2.0));java.util.Collections.reverse(path);
                            if(offsetEast!=0||offsetSouth!=0)path.add(List.of(feetEast,feetUp,feetSouth));
                            evidence.put("requires_jump",jumpRise>0);evidence.put("planned_jump_rise",jumpRise);evidence.put("interaction_feet",List.of(feetEast,feetUp+jumpRise,feetSouth));
                            evidence.put("pass",true);evidence.put("standing_feet",List.of(feetEast,feetUp,feetSouth));evidence.put("standing_clear_and_supported",true);evidence.put("fine_step_sweep_samples",11);evidence.put("eye",List.of(eye.x,eye.y,eye.z));evidence.put("hit_point",List.of(hit.getLocation().x,hit.getLocation().y,hit.getLocation().z));evidence.put("hit_face",hit.getDirection().toString());evidence.put("eye_to_hit_distance",eye.distanceTo(hit.getLocation()));evidence.put("native_outline_first_hit_is_fixture",true);evidence.put("path_from_entrance",path);break search;
                        }
                    }
                    }
                    }
                }
                interactionResults.add(evidence);
            }
            Path interactions=root.resolve(specification.get("snapshot_label").getAsString()+"-interactions.json");
            Files.writeString(interactions,new GsonBuilder().setPrettyPrinting().create().toJson(Map.of("source_sha256",specification.get("sha256").getAsString(),"merged_candidate_sha256",input.get(0).getAsJsonObject().get("sha256").getAsString(),"reachable_half_grid_positions",visited.size(),"maximum_allowed_eye_hit_distance",4.5,"standing_eye_height",1.62,"fixtures",interactionResults,"scope","Producer offline native 1.20.1 collision BFS and native VoxelShape.clip for OUTLINE shapes, contextual neighbor states supplied, nearest hit across all cells intersecting segment bounds, fluids ignored. Empty CollisionContext used instead of null Entity ClipContext. All stations connected to entrance with full 0.6x1.8 standing path, optional final offsets up to 0.2 per horizontal axis checked for support and body clearance at 11 sweep samples. Fixed survival eye 1.62 and vanilla survival 4.5-block ray distance; first outline hit must equal fixture. Not a client click, server movement reenactment or independent acceptance.")));
'''
    java = java.replace(marker, addition + marker)
    java_path = OUTPUT / 'ProducerInteractionAudit.java'
    java_path.write_text(java, encoding='utf-8')
    classpath = (ROOT / 'assembly/qa/classpath.txt').read_text(encoding='utf-8')
    commands = [(['C:/Program Files/Common Files/Oracle/Java/javapath/javac.exe', '-encoding', 'UTF-8', '-cp', classpath, '-d', str(OUTPUT), str(java_path)], 'compile'), (['C:/Program Files/Common Files/Oracle/Java/javapath/java.exe', '-Xmx1536M', '-cp', str(OUTPUT)+os.pathsep+classpath, 'ProducerInteractionAudit', str(OUTPUT)], 'audit')]
    for command, label in commands:
        result = subprocess.run(command, cwd=OUTPUT, capture_output=True, text=True, encoding='utf-8', errors='replace', timeout=300, creationflags=subprocess.CREATE_NO_WINDOW)
        (OUTPUT / (label + '.log')).write_text(result.stdout + result.stderr, encoding='utf-8')
        write(OUTPUT / (label + '-execution.json'), {'command': command, 'exit_code': result.returncode})
        assert result.returncode == 0, result.stdout + result.stderr
    for label in ['current_source', 'real_server_posttick']:
        results = read(OUTPUT / (label+'-interactions.json'))
        failures = [fixture for fixture in results['fixtures'] if not fixture['pass']]
        print(label, 'passed', len(results['fixtures'])-len(failures), 'failed', len(failures))
        if failures:
            print(json.dumps(failures, ensure_ascii=False))
    changes = []
    for position in original.block_entities.keys() | after.block_entities.keys():
        before_payload = plain(original.block_entities[position]) if position in original.block_entities else None
        after_payload = plain(after.block_entities[position]) if position in after.block_entities else None
        if before_payload == after_payload:
            continue
        if before_payload is None:
            assert original.get(position).name == 'minecraft:lectern' and after_payload == {'id': 'minecraft:lectern'}
            classification = 'native_empty_lectern_entity'
        else:
            assert all(after_payload.get(key) == value for key, value in before_payload.items())
            additions = {key: value for key, value in after_payload.items() if key not in before_payload}
            if before_payload['id'] == 'minecraft:decorated_pot':
                assert additions == {'sherds': ['minecraft:brick'] * 4}
                classification = 'native_default_four_undecorated_brick_sherds'
            else:
                assert before_payload['id'] == 'minecraft:furnace' and additions == {'CookTime': 0, 'BurnTime': 0, 'CookTimeTotal': 0, 'RecipesUsed': {}}
                classification = 'native_default_idle_furnace_fields'
        changes.append({'position': position, 'block': original.get(position).name, 'before': before_payload, 'after': after_payload, 'classification': classification, 'inventory_config_text_changed': False})
    from collections import Counter
    assert len(changes) == 30
    write(OUTPUT / 'block-entity-differences.json', {'source_sha256': digest(source), 'after_sha256': digest(after_path), 'raw_difference_count': 30, 'classification_counts': dict(Counter(change['classification'] for change in changes)), 'original_payload_fields_changed': 0, 'inventory_config_text_changes': 0, 'changes': changes, 'scope': 'Exact decoded raw payload comparison; current original fields must be identical. Only native pot defaults, idle furnace defaults, and absent source lectern becoming empty native entity are allowed.'})


if __name__ == '__main__':
    main()
