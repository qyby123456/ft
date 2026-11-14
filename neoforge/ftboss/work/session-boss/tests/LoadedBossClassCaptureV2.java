package ftboss.nativeprobe;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.ProtectionDomain;
import java.util.Set;
public final class LoadedBossClassCaptureV2 {
    private static String actualName(byte[] bytes)throws Exception {
        try(DataInputStream stream=new DataInputStream(new ByteArrayInputStream(bytes))) {
            if(stream.readInt()!=0xCAFEBABE)throw new IllegalArgumentException("Invalid class header");
            stream.skipNBytes(4);Object[] constants=new Object[stream.readUnsignedShort()];
            for(int index=1;index<constants.length;index++) {
                int tag=stream.readUnsignedByte();
                switch(tag) {
                    case 1 -> constants[index]=stream.readUTF();
                    case 7 -> constants[index]=stream.readUnsignedShort();
                    case 3,4,9,10,11,12,17,18 -> stream.skipNBytes(4);
                    case 5,6 -> {stream.skipNBytes(8);index++;}
                    case 8,16,19,20 -> stream.skipNBytes(2);
                    case 15 -> stream.skipNBytes(3);
                    default -> throw new IllegalArgumentException("Unknown constant tag "+tag);
                }
            }
            stream.readUnsignedShort();int selected=stream.readUnsignedShort();
            return (String)constants[(Integer)constants[selected]];
        }
    }
    public static void premain(String argument,Instrumentation instrumentation)throws Exception {
        Path directory=Path.of(argument);Files.createDirectories(directory);
        Set<String> selected=Set.of("net/farmingtales/ftboss/entity/EasterBossEntity","net/farmingtales/ftboss/boss/ai/ManorKiting","net/farmingtales/ftboss/boss/ai/ManorFinalDamage","net/farmingtales/ftboss/boss/HiddenBossIntro","net/farmingtales/ftboss/boss/MeatBoss","net/minecraft/world/entity/LivingEntity");
        instrumentation.addTransformer(new ClassFileTransformer() {
            @Override public byte[] transform(ClassLoader loader,String suppliedName,Class<?> redefined,ProtectionDomain domain,byte[] bytes) {
                try {
                    String name=suppliedName==null?actualName(bytes):suppliedName;
                    if(selected.contains(name)) {
                        Path output=directory.resolve(name.replace('/','_')+".class");
                        if(!Files.exists(output)) {
                            Files.write(output,bytes,StandardOpenOption.CREATE_NEW);
                            Files.writeString(directory.resolve("capture-observations.tsv"),name+"\t"+(suppliedName==null?"name parsed from final class bytes":"supplied name")+"\t"+(loader==null?"bootstrap":loader.getClass().getName())+"\n",StandardOpenOption.CREATE,StandardOpenOption.APPEND);
                        }
                    }
                } catch(Exception failure) {failure.printStackTrace();}
                return null;
            }
        },false);
    }
}
