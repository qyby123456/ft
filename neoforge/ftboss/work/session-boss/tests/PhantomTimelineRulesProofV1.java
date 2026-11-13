import java.util.*;
import net.farmingtales.ftboss.zheergenphantom.*;
public final class PhantomTimelineRulesProofV1 {
    private static int checks;
    private static void require(boolean condition){if(!condition)throw new AssertionError("TimelineRule:"+checks);checks++;}
    private static PhantomSnapshot snapshot(int delay,String skill){return new PhantomSnapshot(delay,skill,UUID.fromString("00000000-0000-0000-0000-000000000001"),8,240,8.9);}
    public static void main(String[] arguments){
        int[] inputs={Integer.MIN_VALUE,-10000,-1,0,19,20,21,84,99,100,101,10000,Integer.MAX_VALUE};
        int[] expected={20,20,20,20,20,20,21,84,99,100,100,100,100};
        for(int index=0;index<inputs.length;index++)require(PhantomTimeline.readableDelay(inputs[index])==expected[index]);
        PhantomTimeline timeline=new PhantomTimeline();var first=snapshot(10,"first");timeline.schedule(100,first);
        require(timeline.size()==1&&first.delay()==10);require(timeline.poll(119).isEmpty());require(timeline.poll(120).equals(List.of(first)));require(timeline.poll(121).isEmpty()&&timeline.size()==0);
        var earlier=snapshot(20,"earlier");var later=snapshot(100,"later");timeline.schedule(0,later);timeline.schedule(0,earlier);
        require(timeline.poll(19).isEmpty());require(timeline.poll(20).equals(List.of(earlier)));require(timeline.poll(99).isEmpty());require(timeline.poll(100).equals(List.of(later)));
        var same=snapshot(20,"same");timeline.schedule(0,later);timeline.schedule(80,earlier);timeline.schedule(80,same);
        require(timeline.poll(99).isEmpty());require(timeline.poll(100).equals(List.of(later,earlier,same)));require(timeline.poll(100).isEmpty()&&timeline.size()==0);
        timeline.schedule(100,earlier);timeline.schedule(0,later);require(timeline.poll(1000).equals(List.of(later,earlier)));require(timeline.size()==0&&timeline.poll(1001).isEmpty());
        System.out.println("{\"checks\":"+checks+",\"originalPublicDelayQueueStableOrderAndExactlyOnceRules\":true}");
    }
}
