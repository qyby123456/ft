import net.farmingtales.ftboss.zheergenphantom.ErosionRules;
public final class ErosionStrengthOracleV1 {
    public static void main(String[] arguments){
        int checks=0;StringBuilder result=new StringBuilder();
        for(int index=-102;index<=102;index++){
            int level=index==-102?Integer.MIN_VALUE:index==102?Integer.MAX_VALUE:index;
            float expected=level<=0?0:level==1?0.45F:level==2?0.62F:level==3?0.78F:level==4?0.91F:1.0F;
            float actual=ErosionRules.strength(level);if(Float.floatToRawIntBits(actual)!=Float.floatToRawIntBits(expected))throw new AssertionError("OriginalStrength:"+level);checks++;result.append(level).append(':').append(Float.floatToRawIntBits(actual)).append(';');
        }
        System.out.println("checks="+checks+";values="+result);
    }
}
