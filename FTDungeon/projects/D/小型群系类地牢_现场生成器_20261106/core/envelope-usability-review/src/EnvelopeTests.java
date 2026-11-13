import cn.ft.biomedungeon.core.next.DungeonYPlanner;
import cn.ft.biomedungeon.core.next.DungeonEnvelopePlanner;
import cn.ft.biomedungeon.core.next.DungeonEnvelopePlanner.EnvelopeBudget;
import cn.ft.biomedungeon.core.next.DungeonEnvelopePlanner.Limits;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import static cn.ft.biomedungeon.core.DungeonApi.*;
import static cn.ft.biomedungeon.core.DungeonGeometry.*;

public final class EnvelopeTests {
    private static long checks;
    private static final EnvelopeBudget BUDGET = EnvelopeBudget.isolatedRuntime();
    private static final long FAILED_SEED = 267611119455246637L;
    private static final String VERSION = "runtime-v4-unified-candidate1-planner-y-v2";
    private EnvelopeTests() {}
    private static void check(boolean condition, String message) {
        checks++;
        if (!condition) { throw new AssertionError(message); }
    }
    private static Request request(long seed, long ordinal) {
        return new Request(seed, "minecraft:overworld", new Vec3i(1000, 200, 1000), ordinal, VERSION, "s8", 9, 50000);
    }
    private static Limits limits(long ordinal) { return new Limits(164, 316, 64, true, Math.floorMod(ordinal, 4)); }
    private static List<Template> load() throws Exception {
        Map<String,List<String[]>> rows = new TreeMap<>();
        for (String line : Files.readAllLines(Path.of("inputs/real-nine-ports.tsv"))) {
            String[] fields = line.split("\t");
            rows.computeIfAbsent(fields[0], ignored -> new ArrayList<>()).add(fields);
        }
        List<Template> result = new ArrayList<>();
        for (List<String[]> room : rows.values()) {
            String[] first = room.get(0);
            List<Port> ports = new ArrayList<>();
            for (String[] row : room) {
                ports.add(new Port(row[8], new Vec3i(Integer.parseInt(row[9]),Integer.parseInt(row[10]),Integer.parseInt(row[11])),row[12],Integer.parseInt(row[13]),Integer.parseInt(row[14]),row[15]));
            }
            result.add(new Template(first[0],first[1],first[2],first[3],new Vec3i(Integer.parseInt(first[5]),Integer.parseInt(first[6]),Integer.parseInt(first[7])),ports,first[4]));
        }
        return result;
    }
    private static DungeonEnvelopePlanner.Envelope independentEnvelope(Plan plan) {
        int minX=Integer.MAX_VALUE,minY=Integer.MAX_VALUE,minZ=Integer.MAX_VALUE,maxX=Integer.MIN_VALUE,maxY=Integer.MIN_VALUE,maxZ=Integer.MIN_VALUE;
        List<Box> boxes = new ArrayList<>();
        plan.rooms().forEach(room -> boxes.add(room.bounds()));
        for (Link link : plan.links()) {
            for (Vec3i point : link.waypoints()) {
                boxes.add(new Box(new Vec3i(point.x()-8,point.y()-2,point.z()-8),new Vec3i(point.x()+9,point.y()+10,point.z()+9)));
            }
        }
        for(Box box:boxes) {
            minX=Math.min(minX,box.min().x());minY=Math.min(minY,box.min().y());minZ=Math.min(minZ,box.min().z());
            maxX=Math.max(maxX,box.maxExclusive().x());maxY=Math.max(maxY,box.maxExclusive().y());maxZ=Math.max(maxZ,box.maxExclusive().z());
        }
        long volume=(long)(maxX-minX+6)*(maxY-minY+6)*(maxZ-minZ+6);
        return new DungeonEnvelopePlanner.Envelope(new Box(new Vec3i(minX-3,minY-3,minZ-3),new Vec3i(maxX+3,maxY+3,maxZ+3)),volume,maxX-minX,maxY-minY,maxZ-minZ);
    }
    private static void verify(Plan plan,List<Template> templates,List<Box> occupied,Limits limits) {
        Map<String,Template> pool=new TreeMap<>(); templates.forEach(template->pool.put(template.id(),template));
        Map<String,PlacedRoom> nodes=new TreeMap<>(); Set<String> unique=new HashSet<>(); List<Box> all=new ArrayList<>();
        check(plan.rooms().size()==9 && plan.links().size()==8,"Always nine distinct originals, no scope reduction");
        for(PlacedRoom room:plan.rooms()) {
            check(unique.add(room.templateId()),"No repeated original");nodes.put(room.nodeId(),room);
            check(room.bounds().equals(bounds(pool.get(room.templateId()).size(),room.origin(),room.quarterTurns())),"Actual shifted rotated bounds");
            all.add(room.bounds());
        }
        check(unique.equals(pool.keySet()),"All nine original IDs retained");
        Set<String> reached=new HashSet<>();reached.add(plan.rooms().get(0).nodeId()); Set<String> used=new HashSet<>();
        for(Link link:plan.links()) {
            PlacedRoom from=nodes.get(link.fromNode()),to=nodes.get(link.toNode());
            Port source=pool.get(from.templateId()).ports().stream().filter(port->port.id().equals(link.fromPort())).findFirst().orElseThrow();
            Port target=pool.get(to.templateId()).ports().stream().filter(port->port.id().equals(link.toPort())).findFirst().orElseThrow();
            WorldPort sourceWorld=worldPort(pool.get(from.templateId()),source,from.origin(),from.quarterTurns());
            WorldPort targetWorld=worldPort(pool.get(to.templateId()),target,to.origin(),to.quarterTurns());
            check(source.width()==target.width()&&source.width()==link.width(),"Exact width3/5 compatibility");
            check(source.height()==target.height()&&source.medium().equals(target.medium()),"Height/medium exact");
            check(to.origin().y()==from.origin().y()+source.feet().y()-target.feet().y(),"Whole room Y alignment");
            check(sourceWorld.transverseMin()==targetWorld.transverseMin()&&sourceWorld.transverseMaxExclusive()==targetWorld.transverseMaxExclusive(),"True aperture alignment");
            check(sourceWorld.facing().equals(rotateFacing(targetWorld.facing(),2)),"Facing opposition");
            check(link.waypoints().equals(List.of(exteriorFeet(sourceWorld),exteriorFeet(targetWorld))),"True exterior feet");
            check(reached.contains(link.fromNode())&&reached.add(link.toNode()),"Global connected acyclic graph");
            check(used.add(link.fromNode()+"/"+link.fromPort())&&used.add(link.toNode()+"/"+link.toPort()),"No port reuse");
            all.addAll(corridorBounds(link));
        }
        check(reached.size()==9,"All nine geometrically returnable");
        int minY=Integer.MAX_VALUE,maxY=Integer.MIN_VALUE;
        for(int index=0;index<all.size();index++) {
            Box box=all.get(index);minY=Math.min(minY,box.min().y());maxY=Math.max(maxY,box.maxExclusive().y());
            for(int other=index+1;other<all.size();other++){check(!intersects(box,all.get(other)),"Full room/corridor shell separation");}
        }
        check(maxY-minY<=limits.maxGraphHeight(),"Full graph room/shell height span");
        var envelope=independentEnvelope(plan);
        check(envelope.equals(DungeonEnvelopePlanner.envelope(plan)),"Exact runtime operation envelope equation");
        check(DungeonEnvelopePlanner.fits(envelope,BUDGET),"All returned plans fit runtime span and volume budgets");
        check(envelope.bounds().min().y()>=limits.minY()&&envelope.bounds().maxExclusive().y()<=limits.maxYExclusive(),"Whole enclosure absolute height");
        for(Box box:occupied){check(!intersects(envelope.bounds(),box),"Even empty interior writes protected against occupied");}
    }
    private static String vec(Vec3i point){return "{\"x\":"+point.x()+",\"y\":"+point.y()+",\"z\":"+point.z()+"}";}
    private static String box(Box value){return "{\"min\":"+vec(value.min())+",\"maxExclusive\":"+vec(value.maxExclusive())+"}";}
    private static String json(Plan plan){
        List<String> rooms=new ArrayList<>(),links=new ArrayList<>();
        for(PlacedRoom room:plan.rooms()){rooms.add("{\"nodeId\":\""+room.nodeId()+"\",\"templateId\":\""+room.templateId()+"\",\"origin\":"+vec(room.origin())+",\"quarterTurns\":"+room.quarterTurns()+",\"bounds\":"+box(room.bounds())+"}");}
        for(Link link:plan.links()){links.add("{\"fromNode\":\""+link.fromNode()+"\",\"fromPort\":\""+link.fromPort()+"\",\"toNode\":\""+link.toNode()+"\",\"toPort\":\""+link.toPort()+"\",\"waypoints\":["+String.join(",",link.waypoints().stream().map(EnvelopeTests::vec).toList())+"],\"width\":"+link.width()+",\"height\":"+link.height()+"}");}
        return "{\"instanceId\":\""+plan.instanceId()+"\",\"seed\":"+plan.seed()+",\"rooms\":["+String.join(",",rooms)+"],\"links\":["+String.join(",",links)+"]}";
    }
    public static void main(String[] arguments)throws Exception{
        List<Template> templates=load();Files.createDirectories(Path.of("results/plans"));
        Request failed=request(FAILED_SEED,3);
        Plan old=DungeonYPlanner.plan(failed,templates,List.of(),new DungeonYPlanner.Limits(164,316,64,true,3));
        var oldEnvelope=independentEnvelope(old);
        check(oldEnvelope.innerSpanX()==271&&oldEnvelope.cells()==832939,"Reproduce exact failed ordinary installed request");
        Files.writeString(Path.of("results/old-exact-failure-plan.json"),json(old));
        Plan fixed=DungeonEnvelopePlanner.plan(failed,templates,List.of(),limits(3));
        verify(fixed,templates,List.of(),limits(3));Files.writeString(Path.of("results/fixed-exact-request-plan.json"),json(fixed));
        check(!old.rooms().equals(fixed.rooms()),"Same original world/ordinal, genuine new search not seed0 retry");
        System.out.println("EXACT_REQUEST fixed worldSeed="+FAILED_SEED+" ordinal=3 oldInnerX="+oldEnvelope.innerSpanX()+" oldCells="+oldEnvelope.cells()+" newEnvelope="+independentEnvelope(fixed));
        List<String> results=new ArrayList<>();int oldRefusals=0,newFailures=0,newPassed=0,oldSearchFailures=0;
        long[] seeds=new long[32];
        long[] edgeSeeds={FAILED_SEED,0,1,-1,42,8131559247039922076L,Long.MIN_VALUE,Long.MAX_VALUE};
        System.arraycopy(edgeSeeds,0,seeds,0,edgeSeeds.length);
        for(int index=8;index<32;index++){seeds[index]=index+100;}
        int requestCount=0;
        for(int seedIndex=0;seedIndex<seeds.length;seedIndex++){long seed=seeds[seedIndex];int ordinalCount=seedIndex<8?32:4;
            for(int ordinal=0;ordinal<ordinalCount;ordinal++){requestCount++;
            Request request=request(seed,ordinal);Limits limits=limits(ordinal);String oldStatus;
            try{Plan baseline=DungeonYPlanner.plan(request,templates,List.of(),new DungeonYPlanner.Limits(164,316,64,true,ordinal%4));var env=independentEnvelope(baseline);oldStatus=DungeonEnvelopePlanner.fits(env,BUDGET)?"FIT":"RUNTIME_ENVELOPE_REFUSAL";if(!oldStatus.equals("FIT")){oldRefusals++;}}
            catch(DungeonYPlanner.PlanningException failure){oldStatus="SEARCH_"+failure.reason();oldSearchFailures++;}
            String nextStatus;
            try{Plan next=DungeonEnvelopePlanner.plan(request,templates,List.of(),limits);verify(next,templates,List.of(),limits);nextStatus="FIT_NINE";newPassed++;
                Files.writeString(Path.of("results/plans/seed"+seed+"-ordinal"+ordinal+".json"),json(next));
            }catch(DungeonEnvelopePlanner.PlanningException failure){nextStatus="SEARCH_"+failure.reason();newFailures++;System.out.println("NEW_SEARCH_FAILURE seed="+seed+" ordinal="+ordinal+" "+failure);}
            results.add("{\"worldSeed\":"+seed+",\"ordinal\":"+ordinal+",\"rootQ\":"+(ordinal%4)+",\"v2\":\""+oldStatus+"\",\"v3\":\""+nextStatus+"\"}");
        }System.out.println("SEED_MATRIX seed="+seed+" completed="+ordinalCount+" ordinals nine originals each");}
        Files.writeString(Path.of("results/initial-matrix-evidence.json"),"{\"requests\":"+requestCount+",\"oldEnvelopeRefusals\":"+oldRefusals+",\"oldSearchFailures\":"+oldSearchFailures+",\"newPassed\":"+newPassed+",\"newFailures\":"+newFailures+",\"rows\":["+String.join(",",results)+"]}\n");
        check(newPassed>0&&newFailures==0,"All scoped new full-nine requests fit; preserve failures if any");
        List<Template> reversed=new ArrayList<>();
        for(Template template:templates){List<Port> ports=new ArrayList<>(template.ports());Collections.reverse(ports);reversed.add(new Template(template.id(),template.biome(),template.resourceId(),template.sha256(),template.size(),ports,template.role()));}Collections.reverse(reversed);
        check(fixed.equals(DungeonEnvelopePlanner.plan(failed,reversed,List.of(),limits(3))),"Stable template/port input ordering");
        check(fixed.equals(DungeonEnvelopePlanner.plan(failed,templates,List.of(),limits(3))),"Exact deterministic repeat");
        var exactEnvelope=independentEnvelope(fixed);
        EnvelopeBudget exactBudget=new EnvelopeBudget(exactEnvelope.innerSpanX(),exactEnvelope.innerSpanY(),exactEnvelope.innerSpanZ(),exactEnvelope.cells());
        check(DungeonEnvelopePlanner.fits(exactEnvelope,exactBudget),"Envelope equality allowed");
        check(!DungeonEnvelopePlanner.fits(exactEnvelope,new EnvelopeBudget(exactBudget.maxSpanX(),exactBudget.maxSpanY(),exactBudget.maxSpanZ(),exactBudget.maxCells()-1)),"Operation budget one cell over refused");
        check(!DungeonEnvelopePlanner.fits(exactEnvelope,new EnvelopeBudget(exactBudget.maxSpanX()-1,exactBudget.maxSpanY(),exactBudget.maxSpanZ(),exactBudget.maxCells())),"X span one cell over refused");
        check(!DungeonEnvelopePlanner.fits(exactEnvelope,new EnvelopeBudget(exactBudget.maxSpanX(),exactBudget.maxSpanY()-1,exactBudget.maxSpanZ(),exactBudget.maxCells())),"Y span one cell over refused");
        check(!DungeonEnvelopePlanner.fits(exactEnvelope,new EnvelopeBudget(exactBudget.maxSpanX(),exactBudget.maxSpanY(),exactBudget.maxSpanZ()-1,exactBudget.maxCells())),"Z span one cell over refused");
        try{DungeonEnvelopePlanner.plan(new Request(FAILED_SEED,"minecraft:overworld",failed.anchor(),3,VERSION,"s8",9,1),templates,List.of(),limits(3));throw new AssertionError("Budget ignored");}
        catch(DungeonEnvelopePlanner.PlanningException failure){check(failure.attempts()==1,"Single hard global attempt budget including restarts");}
        try{DungeonEnvelopePlanner.plan(failed,templates,List.of(),limits(3),new EnvelopeBudget(20,96,20,1500000));throw new AssertionError("Impossible envelope accepted");}
        catch(DungeonEnvelopePlanner.PlanningException failure){check(failure.reason()==DungeonEnvelopePlanner.FailureReason.EXHAUSTED&&failure.attempts()==50000,"Safe impossible bounds refusal with global50000 cap");}
        Template onlyTerminal=templates.get(8);
        try{DungeonEnvelopePlanner.plan(new Request(1,"minecraft:overworld",failed.anchor(),0,VERSION,"s8",2,100),List.of(onlyTerminal),List.of(),new Limits(164,316,64,false,0));throw new AssertionError("Terminal-only impossible graph accepted");}
        catch(DungeonEnvelopePlanner.PlanningException failure){check(failure.attempts()==0,"No-root search exits instead of unbounded zero-progress restart");}
        try{DungeonEnvelopePlanner.plan(failed,templates,List.of(),new Limits(201,316,64,true,3));throw new AssertionError("World-height region ignored");}
        catch(DungeonEnvelopePlanner.PlanningException failure){check(failure.reason()==DungeonEnvelopePlanner.FailureReason.EXHAUSTED,"Whole enclosure absoluteY protected");}
        Template original01=templates.get(0),original07=templates.get(6);
        Template onePort01=new Template(original01.id(),original01.biome(),original01.resourceId(),original01.sha256(),original01.size(),List.of(original01.ports().get(0)),"entry");
        Port originalWide=original07.ports().get(1);
        Template onePort07=new Template(original07.id(),original07.biome(),original07.resourceId(),original07.sha256(),original07.size(),List.of(originalWide),"terminal");
        Request pair=new Request(FAILED_SEED,"minecraft:overworld",failed.anchor(),3,VERSION,"s8",2,1000);
        try{DungeonEnvelopePlanner.plan(pair,List.of(onePort01,onePort07),List.of(),limits(3));throw new AssertionError("width3/5 coerced");}
        catch(DungeonEnvelopePlanner.PlanningException failure){check(failure.reason()==DungeonEnvelopePlanner.FailureReason.EXHAUSTED,"Explicit3/5 mismatch refused");}
        Port originalNarrow=original07.ports().get(0);
        Port wet=new Port(originalNarrow.id(),originalNarrow.feet(),originalNarrow.facing(),originalNarrow.width(),originalNarrow.height(),"wet");
        Template wet07=new Template(original07.id(),original07.biome(),original07.resourceId(),original07.sha256(),original07.size(),List.of(wet),"terminal");
        try{DungeonEnvelopePlanner.plan(pair,List.of(onePort01,wet07),List.of(),limits(3));throw new AssertionError("dry/wet coerced");}
        catch(DungeonEnvelopePlanner.PlanningException failure){check(failure.reason()==DungeonEnvelopePlanner.FailureReason.EXHAUSTED,"Explicit dry/wet mismatch refused");}
        try{DungeonEnvelopePlanner.plan(failed,templates,List.of(),new Limits(164,316,10,true,3));throw new AssertionError("Full graph height cap ignored");}
        catch(DungeonEnvelopePlanner.PlanningException failure){check(failure.reason()==DungeonEnvelopePlanner.FailureReason.EXHAUSTED,"Full graph height cap retained");}
        Plan alternatePolicy=DungeonEnvelopePlanner.plan(failed,templates,List.of(),limits(3),new EnvelopeBudget(256,96,256,1499999));
        check(!fixed.instanceId().equals(alternatePolicy.instanceId()),"Instance identity binds exact operation budget");
        try{new EnvelopeBudget(257,96,256,1500000);throw new AssertionError("Runtime span widened");}
        catch(IllegalArgumentException failure){check(true,"Caller may not widen frozen runtime span");}
        try{new EnvelopeBudget(256,96,256,1500001);throw new AssertionError("Runtime operation cap widened");}
        catch(IllegalArgumentException failure){check(true,"Caller may not widen frozen runtime operation budget");}
        Box envelope=independentEnvelope(fixed).bounds();
        Box corner=new Box(envelope.min(),new Vec3i(envelope.min().x()+1,envelope.min().y()+1,envelope.min().z()+1));
        Plan occupiedAware=DungeonEnvelopePlanner.plan(failed,templates,List.of(corner),limits(3));verify(occupiedAware,templates,List.of(corner),limits(3));
        Files.writeString(Path.of("results/occupied-enclosure-plan.json"),json(occupiedAware));
        var executor=java.util.concurrent.Executors.newFixedThreadPool(4);
        try{List<java.util.concurrent.Callable<Plan>> jobs=new ArrayList<>();for(int index=0;index<12;index++){jobs.add(()->DungeonEnvelopePlanner.plan(failed,templates,List.of(),limits(3)));}for(var result:executor.invokeAll(jobs)){check(fixed.equals(result.get()),"Concurrent deterministic repeat");}}finally{executor.shutdownNow();}
        Files.writeString(Path.of("results/seed-ordinal-matrix.json"),"{\"requests\":"+requestCount+",\"worldSeeds\":32,\"minimumOrdinalsPerSeed\":4,\"edgeSeedsWith32Ordinals\":8,\"oldEnvelopeRefusals\":"+oldRefusals+",\"oldSearchFailures\":"+oldSearchFailures+",\"newPassed\":"+newPassed+",\"newFailures\":"+newFailures+",\"rows\":["+String.join(",",results)+"]}\n");
        Files.writeString(Path.of("results/java-summary.json"),"{\"assertions\":"+checks+",\"java\":\""+System.getProperty("java.version")+"\",\"productionPromotion\":false,\"world\":\"NOT_RUN\"}\n");
        System.out.println("PASS ENVELOPE "+checks+" assertions; oldEnvelopeRefusals="+oldRefusals+" oldSearchFailures="+oldSearchFailures+" newNineFit="+newPassed+" newFailures="+newFailures+" Java="+System.getProperty("java.version"));
    }
}
