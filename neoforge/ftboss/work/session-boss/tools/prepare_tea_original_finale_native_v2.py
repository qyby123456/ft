from pathlib import Path
import ast
ROOT=Path(__file__).resolve().parents[3];WORK=ROOT/'work/session-boss'
h=WORK/'tools/prepare_boss_blood_tide_stage10.py';ns={'__file__':str(h),'__name__':'create_only'};exec(compile(h.read_text(encoding='utf-8').split('probe=(WORK/')[0],str(h),'exec'),ns);create=ns['create']
def replace(s,a,b):
    assert s.count(a)==1,(a[:90],s.count(a));return s.replace(a,b)
s=(WORK/'tests/TeaOriginalFinaleNativeV1.java').read_text(encoding='utf-8').replace('TeaOriginalFinaleNativeV1','TeaOriginalFinaleNativeV2').replace('tea-original-finale-native-v1','tea-original-finale-native-v2')
s=replace(s,'require(Boolean.TRUE.equals(teaField("secondLifeUsed")),"Original Tea second life is naturally consumed without observer writes");','require(!Boolean.TRUE.equals(teaField("secondLifeUsed"))&&!tea().isAlive()&&tea().getHealth()<=0.0F,"Original unchanged tryRevive requires isAlive and rejects native zero-health death; no forced resurrection");')
s=replace(s,'"originalSecondLifeConsumed",true','"originalZeroHealthRevivePredicateRetained",true')
s=s.replace('Actual uncanceled Tea death after original revival','Actual uncanceled Tea death with unchanged original zero-health revive refusal').replace('Real player must defeat original guards, second life and Tea','Real player must defeat original guards and Tea via unchanged original death predicate')
create(WORK/'tests/TeaOriginalFinaleNativeV2.java',s)
s=(WORK/'tools/run_tea_original_finale_native_v1.py').read_text(encoding='utf-8').replace('TeaOriginalFinaleNativeV1','TeaOriginalFinaleNativeV2').replace('tea-original-finale-native-v1','tea-original-finale-native-v2')
create(WORK/'tools/run_tea_original_finale_native_v2.py',s);ast.parse(s)
s=(WORK/'tools/audit_tea_finale_platform_mapping_v1.py').read_text(encoding='utf-8').replace('boss-tea-finale-map-v1-20261114','boss-tea-finale-map-v2-20261114')
s=replace(s,"'tickBossBar', 'dropRewards']}","'tickBossBar', 'dropRewards', 'tryRevive']}")
create(WORK/'tools/audit_tea_finale_platform_mapping_v2.py',s);ast.parse(s)
print('Created native V2 with source-backed original death predicate; product candidate unchanged')
