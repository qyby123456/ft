import importlib.util
from pathlib import Path

ROOT=Path(__file__).resolve().parents[3]
WORK=ROOT/'work/session-boss'
spec=importlib.util.spec_from_file_location('preparation',WORK/'tools/prepare_boss_blood_tide_stage10.py')
module=importlib.util.module_from_spec(spec)
source=(WORK/'tools/prepare_boss_blood_tide_stage10.py').read_text(encoding='utf-8')
exec(compile(source[:source.index('probe=(WORK/')],str(spec.origin),'exec'),module.__dict__)
create=module.create
replace=module.replace
text=(WORK/'boss-core/src/main/java/net/farmingtales/ftboss/butcherskills/ButcherBloodBrand.java').read_text(encoding='utf-8')
text=replace(text,'import java.util.Map;','import java.util.Map;\nimport java.util.EnumMap;')
text=replace(text,'private static final ButcherSkillRules.Skill SKILL=ButcherSkillRules.Skill.BLOOD_BRAND;','private static final ButcherSkillRules.Skill[] CHOICES={ButcherSkillRules.Skill.BLOOD_BRAND,ButcherSkillRules.Skill.BLOOD_TIDE};')
text=replace(text,'if (SKILL.action.equals(action.name()))','if (action.name().startsWith("butcher_"))')
text=replace(text,'if (now<state.nextCast || now<state.ready || action.active())','if (now<state.nextCast || action.active())')
text=replace(text,'if (Math.sqrt(entity.distanceToSqr(target))>SKILL.castRange || Math.abs(target.getY()-entity.getY())>2.5) return false;','double targetDistance=Math.sqrt(entity.distanceToSqr(target));\n        ButcherSkillRules.Skill selected=null;\n        for (int offset=0;offset<CHOICES.length;offset++) {\n            int index=(state.rotation+offset)%CHOICES.length;\n            ButcherSkillRules.Skill choice=CHOICES[index];\n            if (now>=state.ready.getOrDefault(choice,0L) && targetDistance<=choice.castRange && Math.abs(target.getY()-entity.getY())<=2.5) {\n                selected=choice;\n                state.rotation=(index+1)%CHOICES.length;\n                break;\n            }\n        }\n        if (selected==null) return false;\n        state.skill=selected;')
text=text.replace('SKILL','state.skill')
text=replace(text,'state.center=target.position();','state.center=state.skill==ButcherSkillRules.Skill.BLOOD_BRAND?target.position():origin;')
text=replace(text,'state.ready=now+state.skill.cooldown;','state.ready.put(state.skill,now+state.skill.cooldown);')
text=replace(text,'if (ButcherSkillRules.inCircle(offsetX*offsetX+offsetZ*offsetZ,target.getY()-state.center.y,radius)) target.hurt(source,amount);','if (ButcherSkillRules.inCircle(offsetX*offsetX+offsetZ*offsetZ,target.getY()-state.center.y,radius)\n                && (state.skill!=ButcherSkillRules.Skill.BLOOD_TIDE || ButcherSkillRules.inCone(offsetX,offsetZ,state.forward.x,state.forward.z,radius,Math.sqrt(0.5)))) target.hurt(source,amount);')
text=replace(text,'long ready;','final EnumMap<ButcherSkillRules.Skill,Long> ready=new EnumMap<>(ButcherSkillRules.Skill.class);\n        int rotation;\n        ButcherSkillRules.Skill skill;')
path=WORK/'proposal-stage10-blood-tide/net/farmingtales/ftboss/butcherskills/ButcherBloodBrand.java'
create(path,text)
print('Prepared scoped Blood Tide controller; other skill families remain disabled',flush=True)
