import java.net.*;
import java.nio.file.*;
import java.util.*;
public final class BossOriginalMaterialRulesProofV1 {
    public static void main(String[] args)throws Exception{
        Map<String,String> expected=Map.of("FROST","frost_material","BUTCHER","butcher_material","TEA","tea_material","WINE","wine_material","RAT","cheese_material","DEFECT","capacitor_material","COMPOST","spore_material","APPLE","apple_material","BENGER","shepherd_material","TIDE","tide_material");int checks=0;
        try(URLClassLoader loader=new URLClassLoader(new URL[]{Path.of(args[0]).toUri().toURL()},ClassLoader.getPlatformClassLoader())){
            Class<?> kind=Class.forName("net.farmingtales.ftboss.util.BossRelicRules$Kind",true,loader),rules=Class.forName("net.farmingtales.ftboss.util.WeaponBossMaterialRules",true,loader);var id=rules.getMethod("itemId",kind);var count=rules.getMethod("count",kind);Map<String,String> actual=new LinkedHashMap<>();
            for(Object value:kind.getEnumConstants()){checks++;if(!Integer.valueOf(1).equals(count.invoke(null,value)))throw new AssertionError("Original per-kind material count");actual.put(((Enum<?>)value).name(),(String)id.invoke(null,value));}
            checks++;if(!actual.equals(expected))throw new AssertionError("Original ten IDs differ");checks++;if(!Integer.valueOf(0).equals(count.invoke(null,new Object[]{null})))throw new AssertionError("Original null count");
            System.out.println("checks="+checks+";originalForgeMaterialIds="+actual+";originalCountEach=1;originalNullCount=0;privateGameplayOrRewardInvocation=false;");
        }
    }
}
