import com.google.gson.GsonBuilder;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;

@net.neoforged.fml.common.Mod("ftboss_attrprobe")
public final class NonManorAttributeEngineV3 {
    public NonManorAttributeEngineV3() {
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(NonManorAttributeEngineV3::started);
    }

    private static void started(net.neoforged.neoforge.event.server.ServerStartedEvent event) {
        try {
            Files.writeString(Path.of("native-loader-context.json"),new GsonBuilder().setPrettyPrinting().create().toJson(Map.of("actualServerStartedEvent",true,"dedicatedServer",event.getServer().isDedicatedServer(),"actualPlayerCount",event.getServer().getPlayerList().getPlayerCount(),"neoforgeVersion",net.neoforged.fml.ModList.get().getModContainerById("neoforge").orElseThrow().getModInfo().getVersion().toString(),"actualBossEntryObserved",false)));
            main(new String[]{"actual-native-attribute-engine.json"});
        } catch (Throwable failure) {
            try { Files.writeString(Path.of("native-loader-failure.txt"),failure.toString()); }
            catch (java.io.IOException nested) { throw new IllegalStateException(nested); }
        }
    }

    private static final List<Object> rows=new ArrayList<>();
    private static int checks;

    private static void require(boolean condition, String message) {
        checks++;if (!condition) throw new AssertionError(message);
    }

    private static AttributeInstance attribute(double added, double multipliedBase, double multipliedTotal) {
        Holder<Attribute> type=Holder.direct(new RangedAttribute("attribute.ftboss_probe.differential",10.0,0.0,10000.0).setSyncable(true));
        AttributeInstance instance=new AttributeInstance(type,changed->{});instance.setBaseValue(10.0);
        if (added!=0.0) instance.addTransientModifier(new AttributeModifier(ResourceLocation.fromNamespaceAndPath("ftboss_probe","added"),added,AttributeModifier.Operation.ADD_VALUE));
        if (multipliedBase!=0.0) instance.addTransientModifier(new AttributeModifier(ResourceLocation.fromNamespaceAndPath("ftboss_probe","base"),multipliedBase,AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
        if (multipliedTotal!=0.0) instance.addTransientModifier(new AttributeModifier(ResourceLocation.fromNamespaceAndPath("ftboss_probe","total"),multipliedTotal,AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
        return instance;
    }

    private static void sample(String name, double added, double multipliedBase, double multipliedTotal, double expectedOriginal, double expectedCurrent) {
        AttributeInstance original=attribute(added,multipliedBase,multipliedTotal);
        AttributeInstance current=attribute(added,multipliedBase,multipliedTotal);
        require(Math.abs(original.getValue()-current.getValue())<0.000001,name+": fixture equality");
        original.setBaseValue(original.getAttribute().value().sanitizeValue(original.getBaseValue()*2.0));
        ResourceLocation id=ResourceLocation.fromNamespaceAndPath("ftboss","non_manor_finale_double");
        if (current.getModifier(id)==null) current.addPermanentModifier(new AttributeModifier(id,1.0,AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
        double actualOriginal=original.getValue();double actualCurrent=current.getValue();
        require(Math.abs(actualOriginal-expectedOriginal)<0.000001,name+": original native arithmetic");
        require(Math.abs(actualCurrent-expectedCurrent)<0.000001,name+": current native arithmetic");
        if (current.getModifier(id)==null) current.addPermanentModifier(new AttributeModifier(id,1.0,AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
        require(Math.abs(current.getValue()-actualCurrent)<0.000001,name+": current modifier idempotency");
        rows.add(Map.of("case",name,"originalBase",original.getBaseValue(),"currentBase",current.getBaseValue(),"originalNativeValue",actualOriginal,"currentNativeValue",actualCurrent,"equalEffectiveValue",Math.abs(actualOriginal-actualCurrent)<0.000001));
    }

    public static void main(String[] arguments) throws Exception {
        Map<String,Object> report=new LinkedHashMap<>();Throwable failed=null;
        try {
            net.minecraft.SharedConstants.tryDetectVersion();
            net.minecraft.server.Bootstrap.bootStrap();
            sample("no-preexisting-modifier",0.0,0.0,0.0,20.0,20.0);
            sample("preexisting-add-value",5.0,0.0,0.0,25.0,30.0);
            sample("preexisting-multiplied-base",0.0,0.25,0.0,25.0,25.0);
            sample("preexisting-multiplied-total",0.0,0.0,0.25,25.0,25.0);
            sample("preexisting-add-and-base",5.0,0.25,0.0,31.25,37.5);
            sample("preexisting-add-and-total",5.0,0.0,0.25,31.25,37.5);
        } catch (Throwable failure) { failed=failure;StringWriter trace=new StringWriter();failure.printStackTrace(new PrintWriter(trace));report.put("failure",trace.toString()); }
        report.put("checks",checks);report.put("failures",failed==null?0:1);report.put("cases",rows);
        report.put("actualAttributeInstanceCodeSource",AttributeInstance.class.getProtectionDomain().getCodeSource().getLocation().toString());
        report.put("scope","Actual NeoForge SDK vanilla attribute arithmetic only. Original and current operations are reproduced from frozen public bytecode; no Boss admission or integrated-server acceptance is claimed.");
        report.put("actualBossEntryObserved",false);report.put("difficultyOrUnlockStateModified",false);report.put("productChanged",false);
        Files.writeString(Path.of(arguments[0]),new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create().toJson(report));
        System.out.println("checks="+checks+" failures="+(failed==null?0:1));if (failed!=null) throw new AssertionError("Native attribute arithmetic probe failed; raw failure retained",failed);
    }
}
