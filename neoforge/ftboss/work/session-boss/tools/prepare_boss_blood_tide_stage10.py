import difflib
import hashlib
import json
import subprocess
from pathlib import Path

ROOT=Path(__file__).resolve().parents[3]
WORK=ROOT/'work/session-boss'
PATCH='C:/Users/qyby/AppData/Local/OpenAI/Codex/bin/b6c7fc9497179c98/codex.exe'

def create(path,text):
    assert not path.exists(),path
    lines=text.splitlines()
    for offset in range(0,len(lines),32):
        if offset==0:
            patch='*** Begin Patch\n*** Add File: '+path.relative_to(ROOT).as_posix()+'\n'
        else:
            patch='*** Begin Patch\n*** Update File: '+path.relative_to(ROOT).as_posix()+'\n@@\n'
            patch+=''.join(' '+line+'\n' for line in lines[max(0,offset-8):offset])
        patch+=''.join('+'+line+'\n' for line in lines[offset:offset+32])
        if offset:patch+='*** End of File\n'
        patch+='*** End Patch\n'
        subprocess.run([PATCH,'--codex-run-as-apply-patch',patch],check=True,stdout=subprocess.DEVNULL)
    assert path.read_text(encoding='utf-8')=='\n'.join(lines)+'\n'

def replace(text,old,new,count=1):
    assert text.count(old)==count,(old,text.count(old))
    return text.replace(old,new)

probe=(WORK/'tests/blood-brand-v2/BossBloodBrandRuntimeProbe.java').read_text(encoding='utf-8')
probe=probe.replace('BossBloodBrandRuntimeProbe','BossBloodTideRuntimeProbe').replace('butcher_blood_brand','butcher_blood_tide').replace('ftboss.bloodbrand.mode','ftboss.bloodtide.mode').replace('actual-blood-brand-probe.json','actual-blood-tide-probe.json')
probe=replace(probe,'private static final List<Map<String,Object>> incoming=new ArrayList<>();','private static final List<Map<String,Object>> incoming=new ArrayList<>();\n    private static final List<Map<String,Object>> allBloodDamages=new ArrayList<>();\n    private static final List<Long> brandStarts=new ArrayList<>();\n    private static boolean previouslyBrand;')
probe=replace(probe,'report.put("nativeBloodIncomingEvents",incoming);','report.put("nativeBloodIncomingEvents",incoming);\n        report.put("allBloodDamageEvents",allBloodDamages);\n        report.put("brandStarts",brandStarts);')
probe=replace(probe,'animal.moveTo(8.5+(index==1?2.0:(index==2?3.1:0.0)),70.0+(index==3?2.6:0.0),0.5,0.0F,0.0F);','animal.moveTo(index==2?0.5:6.5,70.0+(index==3?2.6:0.0),index==1?2.5:(index==2?6.5:0.5),0.0F,0.0F);')
probe=replace(probe,'starts=new ArrayList<>();','starts=new ArrayList<>();\n        brandStarts.clear();\n        previouslyBrand=false;')
probe=replace(probe,'double attack=owner.getAttributeValue(Attributes.ATTACK_DAMAGE);','if (!ACTION.equals(owner.getButcherAction())) return;\n        double attack=owner.getAttributeValue(Attributes.ATTACK_DAMAGE);')
probe=replace(probe,'16.0*(attack/5.5)','14.0*(attack/5.5)')
probe=replace(probe,'row.put("actualSourceIsButcher",event.getSource().getEntity()==owner);\n        damages.add(row);','row.put("actualSourceIsButcher",event.getSource().getEntity()==owner);\n        allBloodDamages.add(row);\n        if (ACTION.equals(owner.getButcherAction())) damages.add(row);')
probe=replace(probe,'double offset=index==1?2.0:(index==2?3.1:0.0);\n                double targetX=scenario==4?20.5:8.5;\n                animal.moveTo(targetX+offset+(index==0 && moveAway?4.0:0.0),70.0+(index==3?2.6:0.0),0.5,0.0F,0.0F);','double targetX=scenario==4?20.5:(index==2?0.5:6.5);\n                animal.moveTo(targetX,70.0+(index==3?2.6:0.0),index==1?2.5:(index==2 || index==0 && moveAway?6.5:0.5),0.0F,0.0F);')
probe=replace(probe,'boolean casting=ACTION.equals(owner.getButcherAction());','boolean branding="butcher_blood_brand".equals(owner.getButcherAction());\n            if (branding && !previouslyBrand) brandStarts.add(now);\n            previouslyBrand=branding;\n            boolean casting=ACTION.equals(owner.getButcherAction());')
probe=replace(probe,'int duration=scenario==0?370:(scenario==5?150:120);','int duration=scenario==0?490:(scenario==5?260:240);')
probe=replace(probe,'if (scenarioTicks>400)','if (scenarioTicks>600)')
probe=replace(probe,'row.put("castEnds",ends);','row.put("castEnds",ends);\n        row.put("brandStarts",new ArrayList<>(brandStarts));')
probe=replace(probe,'starts.getFirst()-firstTick==80,"OriginalFirstCastDelay80"','starts.getFirst()-firstTick==200,"OriginalFirstBrand80ThenTide200"')
probe=replace(probe,'"OriginalPerSkillCooldown240"','"OriginalTwoSkillRotationInterval240"')
probe=replace(probe,'ends.get(0)-starts.get(0)==40 && ends.get(1)-starts.get(1)==40,"OriginalWindupPlusRecovery40"','ends.get(0)-starts.get(0)==34 && ends.get(1)-starts.get(1)==34,"OriginalWindupPlusRecovery34"')
probe=replace(probe,'==28),"OriginalReleaseOnWindupTick28"','==22),"OriginalReleaseOnWindupTick22"')
start=probe.index('                float expectedTargetHealth=')
end=probe.index('            }\n        } else if',start)
probe=probe[:start]+'''                for (int index=0;index<animals.size();index++) {
                    final int targetIndex=index;
                    double total=allBloodDamages.stream().filter(hit->((Integer)hit.get("scenario"))==scenario && ((Integer)hit.get("targetIndex"))==targetIndex).mapToDouble(hit->((Float)hit.get("actualDamage")).doubleValue()).sum();
                    require(Math.abs(animals.get(index).getHealth()-(1000.0-total))<0.002,"AllNativeDamageMatchesNpcHealth"+index);
                }
                require(hits.stream().noneMatch(hit->((Integer)hit.get("targetIndex"))>=2),"OriginalTideConeAndVerticalExemptions");
                require(brandStarts.size()==2 && brandStarts.getFirst()-firstTick==80 && brandStarts.get(1)-brandStarts.getFirst()==240,"OriginalBloodBrandTimingPreserved");
'''+probe[end:]
probe=replace(probe,'report.put("actualBloodBrandMissingDemonstrated",!candidate && scenario==7);','report.put("actualBloodTideMissingDemonstrated",!candidate && scenario==7);')
probe=replace(probe,'report.put("bloodBrandFeatureAccepted",candidate && scenario==7);','report.put("bloodTideFeatureAccepted",candidate && scenario==7);')
create(WORK/'tests/blood-tide-v1/BossBloodTideRuntimeProbe.java',probe)
runner=(WORK/'tools/run_boss_blood_brand_probe_v2.py').read_text(encoding='utf-8')
runner=runner.replace('blood-brand-v2/BossBloodBrandRuntimeProbe.java','blood-tide-v1/BossBloodTideRuntimeProbe.java').replace('blood-brand-','blood-tide-').replace('ftboss.bloodbrand.mode','ftboss.bloodtide.mode').replace('blood-brand-probe','blood-tide-probe').replace('bloodBrandFeatureAccepted','bloodTideFeatureAccepted').replace('actualBloodBrandMissingDemonstrated','actualBloodTideMissingDemonstrated').replace('Blood Brand alone','Blood Tide with original Blood Brand regression')
create(WORK/'tools/run_boss_blood_tide_probe_v1.py',runner)
previous=json.loads((WORK/'releases/boss-core-stage09-blood-brand-20261112/deployment-receipt.json').read_text(encoding='utf-8-sig'))
jar=Path(previous['target'])
assert hashlib.sha256(jar.read_bytes()).hexdigest()=='85de7ad1daa2fc35663a80ac2a5d551b44797ffe8dc72b2d80d2dd832d4a36b0'
output=WORK/'build/boss-blood-tide-audit-20261112'
output.mkdir()
(output/'baseline-build-receipt.json').write_text(json.dumps({'jar':str(jar),'jarSha256':hashlib.sha256(jar.read_bytes()).hexdigest(),'proposal':str(WORK/'boss-core')},ensure_ascii=False,indent=2),encoding='utf-8')
print(json.dumps({'prepared':True,'baselineReceipt':str(output/'baseline-build-receipt.json'),'weaponFunctionalDevelopmentPaused':True}),flush=True)
