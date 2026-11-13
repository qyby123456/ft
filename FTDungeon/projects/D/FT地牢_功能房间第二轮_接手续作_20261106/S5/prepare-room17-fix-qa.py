import hashlib
import json
import shutil
from pathlib import Path

root=Path(__file__).resolve().parent
qa=root/'qa/room17-storage-fix'
qa.mkdir(exist_ok=False)
native=qa/'native-test'
prior=root/'qa/upper-barrels-native/native-test'
shutil.copytree(prior/'src',native/'src')
for name in ['build.gradle','settings.gradle','gradle.properties']:shutil.copyfile(prior/name,native/name)
shutil.copyfile(root/'qa/world-revision2/run.ps1',qa/'run.ps1')
digest=hashlib.sha256((root/'merged/merged.nbt').read_bytes()).hexdigest()
source=native/'src/main/java/net/farmingtales/s5waterqa/WaterTests.java'
code=source.read_text(encoding='utf-8')
code=code.replace('eb8ac4bccc0829e591cd03310cd6bee221321a6724e822c9368e8c303b729bb7',digest)
code=code.replace('timeoutTicks=200','timeoutTicks=1500')
code=code.replace('../upper-barrels-halfgrid/selected-standing.json','selected-standing.json')
code=code.replace('if(passed)helper.succeed();else helper.fail("Missing usable target; see reach-results.json");','if(!passed)helper.fail("Missing usable target; see reach-results.json");')
insert='''
        level.getGameRules().getRule(net.minecraft.world.level.GameRules.RULE_RANDOMTICKING).set(3,level.getServer());
        BlockPos roomOrigin=new BlockPos(1024,120,1024);
        for(int chunkEast=63;chunkEast<=66;chunkEast++)for(int chunkSouth=63;chunkSouth<=66;chunkSouth++)level.setChunkForced(chunkEast,chunkSouth,true);
        for(BlockPos position:BlockPos.betweenClosed(roomOrigin.offset(-2,-1,-2),roomOrigin.offset(30,16,28)))level.setBlock(position,position.getY()<120?Blocks.PRISMARINE_BRICKS.defaultBlockState():Blocks.AIR.defaultBlockState(),Block.UPDATE_CLIENTS);
        CompoundTag roomData=NbtIo.readCompressed(root().resolve("../../S5-17/s5-17.nbt").normalize().toFile());
        StructureTemplate roomTemplate=new StructureTemplate();roomTemplate.load(BuiltInRegistries.BLOCK.asLookup(),roomData);roomTemplate.placeInWorld(level,roomOrigin,roomOrigin,new StructurePlaceSettings().setKnownShape(false),level.random,Block.UPDATE_ALL);
        Map<String,CompoundTag> expectedData=Map.of("merged",data,"S5-17",roomData);
        Map<String,BlockPos> worldOrigins=Map.of("merged",origin,"S5-17",roomOrigin);
        for(String identifier:expectedData.keySet()){
            ListTag dimensions=expectedData.get(identifier).getList("size",Tag.TAG_INT);StructureTemplate snapshot=new StructureTemplate();snapshot.fillFromWorld(level,worldOrigins.get(identifier),new Vec3i(dimensions.getInt(0),dimensions.getInt(1),dimensions.getInt(2)),false,null);NbtIo.writeCompressed(snapshot.save(new CompoundTag()),root().resolve(identifier+"-placed.nbt").toFile());
        }
        helper.runAtTickTime(1200,()->{
            try{
                Map<String,Object> report=new LinkedHashMap<>();boolean passed=true;
                for(String identifier:expectedData.keySet()){
                    CompoundTag expected=expectedData.get(identifier);ListTag palette=expected.getList("palette",Tag.TAG_COMPOUND);List<Object> changes=new ArrayList<>();BlockPos worldOrigin=worldOrigins.get(identifier);
                    for(Tag value:expected.getList("blocks",Tag.TAG_COMPOUND)){
                        CompoundTag entry=(CompoundTag)value;ListTag coordinate=entry.getList("pos",Tag.TAG_INT);BlockPos position=worldOrigin.offset(coordinate.getInt(0),coordinate.getInt(1),coordinate.getInt(2));BlockState wanted=NbtUtils.readBlockState(BuiltInRegistries.BLOCK.asLookup(),palette.getCompound(entry.getInt("state")));BlockState actual=level.getBlockState(position);if(!wanted.equals(actual))changes.add(Map.of("relative",position.subtract(worldOrigin).toShortString(),"expected",wanted.toString(),"actual",actual.toString()));
                    }
                    ListTag dimensions=expected.getList("size",Tag.TAG_INT);StructureTemplate snapshot=new StructureTemplate();snapshot.fillFromWorld(level,worldOrigin,new Vec3i(dimensions.getInt(0),dimensions.getInt(1),dimensions.getInt(2)),false,null);NbtIo.writeCompressed(snapshot.save(new CompoundTag()),root().resolve(identifier+"-after1200.nbt").toFile());
                    Path candidatePath=root().resolve(identifier.equals("merged")?"../../merged/merged.nbt":"../../S5-17/s5-17.nbt").normalize();String candidateHash=HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(candidatePath)));report.put(identifier,Map.of("sha256",candidateHash,"changes",changes,"completed_ticks",1200,"passed",changes.isEmpty()));passed&=changes.isEmpty();
                }
                report.put("passed",passed);report.put("randomTickSpeed",3);report.put("scope","Producer isolated Forge server; only changed source17 and changed merged placed with neighbor updates and natural 1200 ticks. Other nine source tests inherited by unchanged hash. Complete snapshots retained; payload comparisons performed separately.");Files.writeString(root().resolve("natural-results.json"),new GsonBuilder().setPrettyPrinting().create().toJson(report));if(passed)helper.succeed();else helper.fail("State changes after 1200 ticks");
            }catch(Exception failure){helper.fail(failure.toString());}
        });
'''
code=code.replace('        List<BlockPos> targets=new ArrayList<>();',insert+'\n        List<BlockPos> targets=new ArrayList<>();')
source.write_text(code,encoding='utf-8')
evidence=json.loads((root/'qa/interaction-reach-final/reach-results.json').read_text(encoding='utf-8'))
selected={','.join(map(str,entry['target'])):[entry['feet']] for entry in evidence['interactions'] if entry['passed']}
selected['156,11,81']=[[156.5,9,78.5],[155.5,9,78.5],[157.5,9,78.5]]
selected['159,11,81']=[[159.5,9,78.5],[158.5,9,78.5],[160.5,9,78.5]]
(qa/'selected-standing.json').write_text(json.dumps(selected,indent=2),encoding='utf-8')
print(json.dumps({'qa':str(qa),'sha256':digest}))
