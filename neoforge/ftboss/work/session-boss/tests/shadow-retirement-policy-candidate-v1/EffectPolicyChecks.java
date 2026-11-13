package ftboss.clientfxprobe;
import java.util.*;
import net.farmingtales.ftboss.entity.*;
import net.farmingtales.ftboss.effect.FTEffects;
import net.farmingtales.ftboss.zheergenphantom.ErosionFilter;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.*;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Cow;
public final class EffectPolicyChecks {
    private static void require(boolean condition,String label){if(!condition)throw new AssertionError(label);}
    private static Cow cow(ServerLevel level){Cow animal=EntityType.COW.create(level);require(animal!=null,"ActualPolicyCowFactory");animal.setNoAi(true);animal.setNoGravity(true);animal.moveTo(12,70,8,0,0);require(level.addFreshEntity(animal),"ActualPolicyCowAdmission");return animal;}
    public static List<Map<String,Object>> verify(ServerLevel level){
        List<Map<String,Object>> rows=new ArrayList<>();
        List<Holder<MobEffect>> effects=new ArrayList<>(List.of(MobEffects.BLINDNESS,MobEffects.DARKNESS,FTEffects.SHADOW_BLIND,MobEffects.MOVEMENT_SLOWDOWN,MobEffects.MOVEMENT_SPEED));effects.add(null);
        for(int variant:new int[]{0,1,4,5,6,7,8,9})for(int form=0;form<(variant==7?3:1);form++){
            EasterBossEntity actor=FTEntities.EASTER_BOSS.get().create(level);require(actor!=null,"ActualRegisteredPolicyBossFactory");actor.setVariant(variant);actor.setMini(form==1);actor.setGiant(form==2);actor.setNoAi(true);actor.setNoGravity(true);actor.moveTo(8,70,8,0,0);require(level.addFreshEntity(actor),"ActualRegisteredPolicyBossAdmission");
            for(int index=0;index<effects.size();index++){boolean wanted=variant==7&&index<3;boolean actual=ErosionFilter.suppress(actor,effects.get(index));require(actual==wanted,"OriginalPublicSuppressPolicy:"+variant+":"+form+":"+index);rows.add(Map.of("variant",variant,"form",form,"effectIndex",index,"actualSuppressed",actual,"originalSuppressed",wanted));}actor.discard();
        }
        Cow actor=cow(level);for(int index=0;index<effects.size();index++){require(!ErosionFilter.suppress(actor,effects.get(index)),"OriginalNonBossSuppressionControl");rows.add(Map.of("nonBoss",true,"effectIndex",index,"actualSuppressed",false,"originalSuppressed",false));}actor.discard();
        Cow absent=cow(level);require(ErosionFilter.strength(absent,MobEffects.MOVEMENT_SPEED)==0,"OriginalAbsentEffectStrength");rows.add(Map.of("strengthAbsent",true,"actual",0,"original",0));absent.discard();
        float[] expected={0.45F,0.62F,0.78F,0.91F,1.0F,1.0F,1.0F,1.0F};for(int amplifier=0;amplifier<expected.length;amplifier++){Cow target=cow(level);target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED,120,amplifier));float actual=ErosionFilter.strength(target,MobEffects.MOVEMENT_SPEED);require(actual==expected[amplifier],"OriginalPublicStrengthAmplifierClamp");rows.add(Map.of("amplifier",amplifier,"actual",actual,"original",expected[amplifier]));target.discard();}
        require(rows.size()==75,"FullPublicOriginalPolicyMatrix");return rows;
    }
}
