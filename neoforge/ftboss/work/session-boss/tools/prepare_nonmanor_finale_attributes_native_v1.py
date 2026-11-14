from pathlib import Path
import ast
ROOT=Path(__file__).resolve().parents[3]
WORK=ROOT/'work/session-boss'
h=WORK/'tools/prepare_boss_blood_tide_stage10.py';ns={'__file__':str(h),'__name__':'create_only'};exec(compile(h.read_text(encoding='utf-8').split('probe=(WORK/')[0],str(h),'exec'),ns);create=ns['create']
def replace(text,old,new):
    assert text.count(old)==1,(old[:100],text.count(old));return text.replace(old,new)
s=(WORK/'tests/ManorEarnedFinaleWithZhuniNativeV2.java').read_text(encoding='utf-8').replace('ManorEarnedFinaleWithZhuniNativeV2','NonManorFinaleAttributesNativeV1').replace('actual-earned-finale-with-zhuni.json','actual-nonmanor-finale-attributes.json')
s=replace(s,'    private static LivingEntity attackTarget;',"""    private static final Map<String,Double> admittedBases=new LinkedHashMap<>();
    private static final Map<String,List<Map<String,Object>>> admittedModifiers=new LinkedHashMap<>();
    private static final net.minecraft.resources.ResourceLocation BASE_ADDED=net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("ftboss_probe","earned_finale_multiplied_base");
    private static final net.minecraft.resources.ResourceLocation TOTAL_ADDED=net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("ftboss_probe","earned_finale_multiplied_total");
    private static final net.minecraft.resources.ResourceLocation HEALTH_ADDED=net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("ftboss_probe","earned_finale_health_addition");
    private static boolean finaleCase(){return baselineIndex==2||baselineIndex==3||baselineIndex==5;}
    private static boolean addedCase(){return baselineIndex==1||baselineIndex==3||baselineIndex>=4;}
    private static String attributeId(net.minecraft.world.entity.ai.attributes.AttributeInstance instance){return net.minecraft.core.registries.BuiltInRegistries.ATTRIBUTE.getKey(instance.getAttribute().value()).toString();}
    private static List<Map<String,Object>> modifiers(net.minecraft.world.entity.ai.attributes.AttributeInstance instance){return instance.getModifiers().stream().map(modifier->Map.<String,Object>of("id",modifier.id().toString(),"amount",modifier.amount(),"operation",modifier.operation().name())).toList();}
    private static void rememberAdmitted(net.minecraft.world.entity.ai.attributes.AttributeInstance instance){if(instance!=null){admittedBases.put(attributeId(instance),instance.getBaseValue());admittedModifiers.put(attributeId(instance),modifiers(instance));}}
    private static LivingEntity attackTarget;""")
a=s.index('    private static void baselineJoin');b=s.index('    private static void startBaseline',a)
s=s[:a]+"""    private static void baselineJoin(EntityJoinLevelEvent event){
        if(!baselinePending||!(event.getLevel() instanceof ServerLevel)||!(event.getEntity() instanceof net.farmingtales.ftboss.entity.RatBossEntity boss)||boss.isMinion())return;
        require(!event.loadedFromDisk(),"Actual new regular Boss admission");require(baselineBoss==null,"One requested baseline Boss admitted");baselineBoss=boss;
        var attack=boss.getAttribute(Attributes.ATTACK_DAMAGE);require(attack!=null,"Actual regular Boss attack before product handler");
        if(addedCase())attack.addTransientModifier(new net.minecraft.world.entity.ai.attributes.AttributeModifier(ADDED,5.0D,net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_VALUE));
        if(baselineIndex>=4){attack.addTransientModifier(new net.minecraft.world.entity.ai.attributes.AttributeModifier(BASE_ADDED,0.25D,net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_MULTIPLIED_BASE));attack.addTransientModifier(new net.minecraft.world.entity.ai.attributes.AttributeModifier(TOTAL_ADDED,0.4D,net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));boss.getAttribute(Attributes.MAX_HEALTH).addTransientModifier(new net.minecraft.world.entity.ai.attributes.AttributeModifier(HEALTH_ADDED,125.0D,net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_VALUE));}
        admittedBases.clear();admittedModifiers.clear();rememberAdmitted(boss.getAttribute(Attributes.MAX_HEALTH));for(var instance:boss.getAttributes().getSyncableAttributes())rememberAdmitted(instance);
        events.add(Map.of("phase","actualRegularBossHighestJoin","case",baselineIndex,"uuid",boss.getUUID().toString(),"base",attack.getBaseValue(),"valueBeforeFinaleHandler",attack.getValue(),"preexistingAddValue",addedCase(),"selectedDifficulty",net.farmingtales.ftboss.boss.BossChallengePreference.selected(player).id(),"selectedBaseCount",admittedBases.size()));
    }
"""+s[b:]
s=replace(s,'boolean finale=baselineIndex>=2;','boolean finale=finaleCase();')
a=s.index('    private static void finishBaseline');b=s.index('    private static void serverTick',a)
s=s[:a]+"""    private static void finishBaseline(){
        var attack=baselineBoss.getAttribute(Attributes.ATTACK_DAMAGE);boolean finale=finaleCase(),added=addedCase();
        require(net.farmingtales.ftboss.boss.NonManorFinale.isFinale(baselineBoss)==finale,"Actual earned public preference admits expected difficulty");
        require(attack.getModifier(DOUBLE)==null,"No legacy total finale multiplier on actual attack");
        double reconstructedActual=reconstructed(attack,attack.getBaseValue(),false).getValue();require(Math.abs(reconstructedActual-attack.getValue())<0.001,"All actual modifier operations reconstructed");
        require((attack.getModifier(ADDED)!=null)==added,"Disclosed preexisting addition retained");
        if(added){var withoutAdded=reconstructed(attack,attack.getBaseValue(),false);withoutAdded.removeModifier(ADDED);require(Math.abs(withoutAdded.getValue()-attack.getValue())>0.001,"Missing actual addition rejected by negative control");attributeReconstructionNegativeControls++;}
        double originalBase=admittedBases.get(attributeId(attack));double originalExpected=reconstructed(attack,attack.getAttribute().value().sanitizeValue(originalBase*(finale?2:1)),true).getValue();
        require(Math.abs(attack.getValue()-originalExpected)<0.001,"Actual legitimate finale matches original sanitized base-only doubling with every other modifier");
        List<Map<String,Object>> selected=new ArrayList<>();var instances=new LinkedHashMap<String,net.minecraft.world.entity.ai.attributes.AttributeInstance>();var maximum=baselineBoss.getAttribute(Attributes.MAX_HEALTH);instances.put(attributeId(maximum),maximum);for(var instance:baselineBoss.getAttributes().getSyncableAttributes())instances.put(attributeId(instance),instance);
        for(var entry:admittedBases.entrySet()){
            var instance=instances.get(entry.getKey());require(instance!=null,"Every originally selected attribute remains present");double expectedBase=instance.getAttribute().value().sanitizeValue(entry.getValue()*(finale?2:1));require(Math.abs(instance.getBaseValue()-expectedBase)<0.000001,"All originally selected bases preserve normal or exactly double finale after forty natural ticks: "+entry.getKey());require(instance.getModifier(DOUBLE)==null,"No legacy finale multiplier on any selected attribute");
            for(var initial:admittedModifiers.get(entry.getKey())){var id=net.minecraft.resources.ResourceLocation.parse((String)initial.get("id"));var actual=instance.getModifier(id);require(actual!=null&&actual.amount()==(Double)initial.get("amount")&&actual.operation().name().equals(initial.get("operation")),"Every actual admission modifier preserved: "+id);}
            var attribute=new LinkedHashMap<String,Object>();attribute.put("id",entry.getKey());attribute.put("admittedBase",entry.getValue());attribute.put("base",instance.getBaseValue());attribute.put("expectedBase",expectedBase);attribute.put("actualValue",instance.getValue());attribute.put("modifiers",modifiers(instance));attribute.put("admittedModifiers",admittedModifiers.get(entry.getKey()));
            if(finale){String key="ftboss:regular_finale_base/"+entry.getKey();require(baselineBoss.getPersistentData().contains(key)&&baselineBoss.getPersistentData().getDouble(key)==entry.getValue(),"Original saved pre-finalization base retained exactly");attribute.put("savedOriginalBase",baselineBoss.getPersistentData().getDouble(key));}
            selected.add(attribute);
        }
        if(finale)require(baselineBoss.getPersistentData().getBoolean("ftboss:regular_finale_prepared")&&baselineBoss.getPersistentData().getBoolean("ftboss:regular_finale"),"Original preparation and finale state saved by product after earned admission");
        if(baselineIndex>=4){require(attack.getModifier(BASE_ADDED)!=null&&attack.getModifier(TOTAL_ADDED)!=null,"All three actual modifier operations coexist");require(maximum.getModifier(HEALTH_ADDED)!=null,"Actual health addition retained outside base doubling");require(Math.abs(baselineBoss.getMaxHealth()-maximum.getValue())<0.001,"Actual maximum health includes its unscaled addition");}
        double oldTotalAlgorithm=reconstructed(attack,originalBase,true).getValue()*(finale?2:1);
        if(finale)require(Math.abs(oldTotalAlgorithm-originalExpected)>0.001,"Previous total-multiplier product behavior is a detected negative control");
        var row=new LinkedHashMap<String,Object>();row.put("modifierSnapshot",modifiers(attack));row.put("reconstructedActualValue",reconstructedActual);row.put("case",baselineIndex);row.put("uuid",baselineBoss.getUUID().toString());row.put("finale",finale);row.put("preexistingAddValue",added);row.put("allThreeOperationsFixture",baselineIndex>=4);row.put("admittedAttackBase",originalBase);row.put("base",attack.getBaseValue());row.put("actualValue",attack.getValue());row.put("originalBaseDoublingExpectedValue",originalExpected);row.put("previousTotalDoublingValue",oldTotalAlgorithm);row.put("matchesOriginalBaseDoubling",true);row.put("selectedAttributes",selected);row.put("health",baselineBoss.getHealth());row.put("maximumHealth",baselineBoss.getMaxHealth());row.put("naturalTicksObserved",level.getGameTime()-baselineStarted);row.put("parallelSpells",net.farmingtales.ftboss.boss.NonManorFinale.parallelSpells(baselineBoss));row.put("acceleratedTicks99",net.farmingtales.ftboss.boss.NonManorFinale.acceleratedTicks(baselineBoss,99));attributes.add(row);System.out.println("ORIGINAL_FINALE_ATTRIBUTE_PARITY "+baselineIndex+" actual="+attack.getValue()+" original="+originalExpected);
        if(++baselineIndex==6){require(actualZhuniRevealHits==1,"Actual original Zhuni reveal on complete original normal fight");report.put("actualOriginalZhuniRevealViaPublicAttackAccepted",true);require(nextStageAfterRebirth,"Actual native canceled death and subsequent stage retained");report.put("baselineUsesAllActualAttributeModifiers",true);report.put("attributeReconstructionNegativeControls",attributeReconstructionNegativeControls);require(attributeReconstructionNegativeControls==4,"Four real preexisting-addition controls");report.put("actualEarnedUnlockAndFinaleAttributeBaselineAccepted",true);report.put("actualEligibleFinaleAddValueParityGapObserved",false);report.put("actualOriginalFinaleBaseAttributesAccepted",true);report.put("allThreeModifierOperationsAndHealthAdditionAccepted",true);report.put("savedOriginalBasesAndFortyTickIdempotenceAccepted",true);finish(null);}else startBaseline();
    }
"""+s[b:]
s=replace(s,'report.put("productChanged",false);','report.put("productChanged",true);')
assert s.count('.setHealth(')==s.count('player.setHealth(')==1
for forbidden in ['.putUUID(','.putBoolean(','.putString(','.getDeclaredMethod(','.invoke(','.recordConqueror(','.award(','.setInvulnerable(','.setNoAi(','.reveal(','.hold(']:assert forbidden not in s,forbidden
create(WORK/'tests/NonManorFinaleAttributesNativeV1.java',s)
a=(WORK/'tests/LoadedBossClassCaptureV12.java').read_text(encoding='utf-8').replace('LoadedBossClassCaptureV12','LoadedBossClassCaptureV13')
a=replace(a,'Set<String> selected=Set.of(', 'Set<String> selected=Set.of("net/farmingtales/ftboss/boss/NonManorFinaleAttributes","net/farmingtales/ftboss/boss/NonManorFinaleAttributes$AttributeBase",')
create(WORK/'tests/LoadedBossClassCaptureV13.java',a)
s=(WORK/'tools/run_earned_finale_with_zhuni_native_v2.py').read_text(encoding='utf-8').replace('ManorEarnedFinaleWithZhuniNativeV2','NonManorFinaleAttributesNativeV1').replace('LoadedBossClassCaptureV12','LoadedBossClassCaptureV13').replace('earned-finale-with-zhuni-native-v2','nonmanor-finale-attributes-native-v1').replace('actual-earned-finale-with-zhuni.json','actual-nonmanor-finale-attributes.json').replace('build/boss-manor-death-cancel-v5-20261114/candidate-build-receipt.json','build/boss-nonmanor-finale-attributes-v1-20261114/candidate-build-receipt.json').replace('b751ae8fbf5552342ae5e887cc5d7675b7fcdf995fcde1773dcc0dc32e691345','c63b17a20bc699ffdfb7b8ca3d985d0a2d3c4a53a2da769ea8fa54abac45f370').replace("'productChanged':False","'productChanged':True").replace('earned_finale_with_zhuni_terminal_audit','nonmanor_finale_base_attributes_terminal_audit').replace('latestEarnedFinaleWithZhuniNativeReceipt','latestNonManorFinaleBaseAttributesNativeReceipt')
s=replace(s,"probe.get('attributeReconstructionNegativeControls')==2","probe.get('attributeReconstructionNegativeControls')==4")
s=replace(s,"probe.get('actualEligibleFinaleAddValueParityGapObserved')","probe.get('actualOriginalFinaleBaseAttributesAccepted') and not probe.get('actualEligibleFinaleAddValueParityGapObserved') and probe.get('allThreeModifierOperationsAndHealthAdditionAccepted') and probe.get('savedOriginalBasesAndFortyTickIdempotenceAccepted')")
s=replace(s,"len(probe.get('attributeCases',[]))==4","len(probe.get('attributeCases',[]))==6")
s=replace(s,"'probe':probe,'terminal':terminal","'actualOriginalFinaleBaseAttributesAccepted':accepted,'probe':probe,'terminal':terminal")
s=replace(s,"'net_farmingtales_ftboss_skill_BossRelicDrops']","'net_farmingtales_ftboss_skill_BossRelicDrops','net_farmingtales_ftboss_boss_NonManorFinaleAttributes','net_farmingtales_ftboss_boss_NonManorFinaleAttributes$AttributeBase']")
ast.parse(s);create(WORK/'tools/run_nonmanor_finale_attributes_native_v1.py',s)
print('Created independently named native probe, runner, and observational class capture; six real admission cases.')
