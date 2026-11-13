import shutil
import subprocess
from pathlib import Path

root=Path(__file__).resolve().parent
sourceqa=root/'qa/natural-movement'
qa=root/'qa/natural-detours'
qa.mkdir(exist_ok=False)
shutil.copytree(sourceqa/'native-test/src',qa/'native-test/src')
for filename in ['build.gradle','settings.gradle','gradle.properties']:shutil.copyfile(sourceqa/'native-test'/filename,qa/'native-test'/filename)
for filename in ['run.ps1','planned-paths.json']:shutil.copyfile(sourceqa/filename,qa/filename)
source=(sourceqa/'native-test/src/main/java/net/farmingtales/s5waterqa/WaterTests.java').read_text(encoding='utf-8')
start=source.index('    static class Controller{')
end=source.index('    @GameTest',start)
controller='''    static class Controller{
        final MotionPlayer actor;final ServerLevel level;final GameTestHelper helper;final List<Vec3> ladder=new ArrayList<>(),water=new ArrayList<>();List<Vec3> path;
        final List<Object> frames=new ArrayList<>(),events=new ArrayList<>();String phase="to_ladder";int cursor=0,ticks=0,phaseTicks=0,stuck=0,ladderTicks=0,waterTicks=0;Vec3 previous;boolean done=false;
        Controller(ServerLevel level,GameTestHelper helper)throws Exception{
            this.level=level;this.helper=helper;actor=new MotionPlayer(level);
            JsonArray paths=JsonParser.parseString(Files.readString(root().resolve("planned-paths.json"))).getAsJsonArray().get(0).getAsJsonObject().getAsJsonArray("paths");
            for(int routeIndex:new int[]{1,2})for(JsonElement value:paths.get(routeIndex).getAsJsonObject().getAsJsonArray("feet")){
                JsonArray coordinate=value.getAsJsonArray();(routeIndex==1?ladder:water).add(new Vec3(2048+coordinate.get(0).getAsDouble(),120+coordinate.get(1).getAsDouble(),1024+coordinate.get(2).getAsDouble()));
            }
            path=ladder;Vec3 spawn=path.get(0);actor.setPos(spawn.x,spawn.y,spawn.z);previous=actor.position();events.add(Map.of("event","initial_spawn_only","feet",relative(previous)));
        }
        void transition(String next){events.add(Map.of("event",next,"tick",ticks,"feet",relative(actor.position())));phase=next;phaseTicks=0;cursor=0;stuck=0;}
        List<Vec3> reverse(List<Vec3> route){List<Vec3> copy=new ArrayList<>(route);Collections.reverse(copy);return copy;}
        void save(boolean passed,String reason)throws Exception{
            Map<String,Object> report=new LinkedHashMap<>();report.put("sha256","e0c71315c81727e81cd15011eda9a0f5da8624387c59b8b9764c42d8d740397e");report.put("passed",passed);report.put("reason",reason);report.put("frames",frames);report.put("events",events);report.put("ticks",ticks);report.put("phase",phase);report.put("ladder_ticks",ladderTicks);report.put("water_ticks",waterTicks);report.put("scope","Producer isolated native 20Hz world: Entity.baseTick plus Player.aiStep once per game tick. One initial spawn only, horizontal input and ordinary jump intent, native ladder collision climbing, native gravity descent and water jump. No position/velocity injection or disabled gravity. Dry entry-to-ladder, ladder up/down, return entry, shallow-water entry/exit and return entry continuously with one actor; no live client or oxygen survival certification.");
            Files.writeString(root().resolve("motion-results.json"),new GsonBuilder().setPrettyPrinting().create().toJson(report));
        }
        void tick(){
            if(done)return;
            try{
                ticks++;phaseTicks++;Vec3 target=actor.position();boolean jump=false;float eastInput=0,southInput=0;
                if(phase.equals("climb")){
                    southInput=-0.8f;
                    if(actor.getY()>=134){transition("descend");southInput=0;}
                }else if(phase.equals("descend")){
                    if(actor.getY()<=129.05&&phaseTicks>10){path=reverse(ladder);transition("return_from_ladder");}
                }else if(phase.equals("enter_water")){
                    target=new Vec3(2076.5,128,1043.5);
                    if(actor.isInWater()&&Math.hypot(actor.getX()-target.x,actor.getZ()-target.z)<0.2){transition("water_hold");}
                }else if(phase.equals("water_hold")){
                    if(phaseTicks>=20){transition("exit_water");}
                }else if(phase.equals("exit_water")){
                    target=water.get(water.size()-1);jump=actor.isInWater();
                    if(!actor.isInWater()&&actor.getY()>=128.95&&Math.hypot(actor.getX()-target.x,actor.getZ()-target.z)<0.18){path=reverse(water);transition("return_from_water");}
                }else{
                    while(cursor<path.size()&&Math.hypot(actor.getX()-path.get(cursor).x,actor.getZ()-path.get(cursor).z)<0.16&&Math.abs(actor.getY()-path.get(cursor).y)<0.6)cursor++;
                    if(cursor>=path.size()){
                        if(phase.equals("to_ladder"))transition("climb");
                        else if(phase.equals("return_from_ladder")){path=water;transition("to_water");}
                        else if(phase.equals("to_water"))transition("enter_water");
                        else if(phase.equals("return_from_water")){done=true;save(ladderTicks>10&&waterTicks>10,"Completed ladder and water detours continuously");if(ladderTicks>10&&waterTicks>10)helper.succeed();else helper.fail("Missing ladder/water states");return;}
                    }else target=path.get(cursor);
                }
                if(!phase.equals("climb")&&!phase.equals("descend")&&!phase.equals("water_hold")){
                    double east=target.x-actor.getX(),south=target.z-actor.getZ(),distance=Math.hypot(east,south),amplitude=Math.min(1,distance/0.35);
                    eastInput=(float)(east/Math.max(distance,0.0001)*amplitude);southInput=(float)(south/Math.max(distance,0.0001)*amplitude);
                }
                actor.motionTick(eastInput,southInput,jump);if(actor.onClimbable())ladderTicks++;if(actor.isInWater())waterTicks++;
                double moved=actor.position().distanceTo(previous);stuck=moved<0.003?stuck+1:0;
                frames.add(Map.of("tick",ticks,"phase",phase,"feet",relative(actor.position()),"velocity",List.of(actor.getDeltaMovement().x,actor.getDeltaMovement().y,actor.getDeltaMovement().z),"on_ground",actor.onGround(),"horizontal_collision",actor.horizontalCollision,"ladder",actor.onClimbable(),"water",actor.isInWater(),"jump_intent",jump));previous=actor.position();
                if(stuck>150||ticks>5000){done=true;save(false,"Stalled "+phase+" target "+relative(target));helper.fail("Native detour stalled");}else if(ticks%200==0)save(false,"Running");
            }catch(Exception failure){done=true;try{save(false,failure.toString());}catch(Exception ignored){}helper.fail(failure.toString());}
        }
    }
'''
source=source[:start]+controller+source[end:]
source=source.replace('timeoutTicks=12500','timeoutTicks=5500').replace('continuousRoundTrip','continuousLadderAndWater')
target=qa/'native-test/src/main/java/net/farmingtales/s5waterqa/WaterTests.java'
patch='*** Begin Patch\n*** Delete File: '+target.as_posix()+'\n*** Add File: '+target.as_posix()+'\n'+''.join('+'+line+'\n' for line in source.splitlines())+'*** End Patch'
process=subprocess.run(['C:/Users/YOUR_USERNAME/AppData/Local/OpenAI/Codex/bin/b6c7fc9497179c98/codex.exe','--codex-run-as-apply-patch',patch],capture_output=True);assert process.returncode==0,process.stderr
print(qa)
