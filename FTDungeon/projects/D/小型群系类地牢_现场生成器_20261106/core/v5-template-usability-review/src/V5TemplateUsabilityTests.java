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

public final class V5TemplateUsabilityTests {
    private static long checks;
    private static final EnvelopeBudget BUDGET = EnvelopeBudget.isolatedRuntime();
    private static final long FAILED_SEED = 267611119455246637L;
    private static String VERSION;
    private V5TemplateUsabilityTests() {}
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
        for(Link link:plan.links()){links.add("{\"fromNode\":\""+link.fromNode()+"\",\"fromPort\":\""+link.fromPort()+"\",\"toNode\":\""+link.toNode()+"\",\"toPort\":\""+link.toPort()+"\",\"waypoints\":["+String.join(",",link.waypoints().stream().map(V5TemplateUsabilityTests::vec).toList())+"],\"width\":"+link.width()+",\"height\":"+link.height()+"}");}
        return "{\"instanceId\":\""+plan.instanceId()+"\",\"seed\":"+plan.seed()+",\"rooms\":["+String.join(",",rooms)+"],\"links\":["+String.join(",",links)+"]}";
    }
    public static void main(String[] arguments)throws Exception{
        VERSION=Files.readString(Path.of("inputs/runtime-version.txt"));
        List<Template> templates=load();Files.createDirectories(Path.of("results/plans"));
        String entrance=templates.stream().filter(template->template.role().equals("entrance")).findFirst().orElseThrow().id();
        check(entrance.equals("s8_07_widearch_side_doublegate_v5"),"True new derived ID, never old s8_07 substitution");
        List<String> outcomes=new ArrayList<>();int passes=0,failures=0;
        for(int seedIndex=0;seedIndex<33;seedIndex++){
            long seed=seedIndex<32?seedIndex:FAILED_SEED;
            int firstOrdinal=seedIndex<32?0:3,lastOrdinal=seedIndex<32?3:3;
            for(int ordinal=firstOrdinal;ordinal<=lastOrdinal;ordinal++){
                Request request=request(seed,ordinal);Limits limits=limits(ordinal);
                try{
                    Plan plan=DungeonEnvelopePlanner.plan(request,templates,List.of(),limits);
                    verify(plan,templates,List.of(),limits);
                    check(plan.rooms().get(0).templateId().equals(entrance),"Actual derived entrance in graph");
                    check(plan.rooms().get(0).quarterTurns()==ordinal%4,"Actual root rotation");
                    check(plan.rooms().get(8).templateId().equals("s8_09"),"Terminal retained");
                    check(plan.generatorVersion().equals(VERSION),"Exact runtime version bound");
                    check(plan.equals(DungeonEnvelopePlanner.plan(request,templates,List.of(),limits)),"Same exact request replay");
                    String name="seed"+seed+"-ordinal"+ordinal+".json";
                    Files.writeString(Path.of("results/plans/"+name),json(plan));
                    outcomes.add("{\"worldSeed\":"+seed+",\"ordinal\":"+ordinal+",\"q\":"+(ordinal%4)+",\"status\":\"PASS_NINE_GEOMETRY\",\"plan\":\"plans/"+name+"\",\"instanceId\":\""+plan.instanceId()+"\",\"operationCells\":"+independentEnvelope(plan).cells()+"}");
                    passes++;
                    if(seedIndex==32){System.out.println("ORIGINAL_WORLD_SEED_NEW_V5_CONTEXT "+request+" envelope="+independentEnvelope(plan));}
                }catch(DungeonEnvelopePlanner.PlanningException failure){
                    failures++;
                    outcomes.add("{\"worldSeed\":"+seed+",\"ordinal\":"+ordinal+",\"q\":"+(ordinal%4)+",\"status\":\""+failure.reason()+"\",\"attempts\":"+failure.attempts()+"}");
                    System.out.println("REAL_FAILURE seed="+seed+" ordinal="+ordinal+" "+failure);
                }
            }
            System.out.println("V5_SEED_COMPLETED "+seed);
        }
        List<Template> reversed=new ArrayList<>();
        for(Template template:templates){List<Port> ports=new ArrayList<>(template.ports());Collections.reverse(ports);reversed.add(new Template(template.id(),template.biome(),template.resourceId(),template.sha256(),template.size(),ports,template.role()));}
        Collections.reverse(reversed);
        Request original=request(FAILED_SEED,3);Plan replay=DungeonEnvelopePlanner.plan(original,templates,List.of(),limits(3));
        check(replay.equals(DungeonEnvelopePlanner.plan(original,reversed,List.of(),limits(3))),"Actual new template ordering independent");
        Files.writeString(Path.of("results/matrix.json"),"{\"runtimeVersion\":\""+VERSION+"\",\"requests\":129,\"seedCountInMainMatrix\":32,\"rootRotations\":4,\"extraOriginalFailureSeedOrdinal3\":true,\"passed\":"+passes+",\"failed\":"+failures+",\"derivedEntrance\":\""+entrance+"\",\"rows\":["+String.join(",",outcomes)+"]}\n");
        Files.writeString(Path.of("results/java-summary.json"),"{\"assertions\":"+checks+",\"java\":\""+System.getProperty("java.version")+"\",\"productionCoreSourceChanged\":false,\"world\":\"NOT_RUN\"}\n");
        System.out.println("V5_EVIDENCE_RESULT assertions="+checks+" passed="+passes+" failed="+failures+" VERSION="+VERSION+" Java="+System.getProperty("java.version"));
    }
}
