import hashlib,json,subprocess
from pathlib import Path
ROOT=Path(__file__).resolve().parents[3];WORK=ROOT/'work/session-boss';OUT=WORK/'reports/tea-original-revive-predicate-v1-20261114'
def load(path):return json.loads(Path(path).read_text(encoding='utf-8-sig'))
def sha(path):return hashlib.sha256(Path(path).read_bytes()).hexdigest()
assert not OUT.exists();OUT.mkdir();source=WORK/'tests/TeaOriginalRevivePredicateAuditV1.java';original=ROOT/'reference/forge-current.jar';candidate=WORK/'build/boss-tea-original-finale-v1-20261114/ftboss-boss-core-1.21.1-stage44-original-tea-finale-NOT-INSTALLABLE.jar';mapping=WORK/'build/boss-tea-finale-map-v2-20261114/mapping-proof.json';native=WORK/'reports/tea-original-finale-native-v1-32e77ed2.json';data=load(native);actual=Path(data['attempt'])/'captured-defined-classes/net_farmingtales_ftboss_entity_TeaTreeBossEntity.class';context=load(WORK/'build/boss-manor-damage-context-v1-20261114/compile-proof-command.json');cp=str(candidate)+';'+context[context.index('-cp')+1];java=Path('C:/Program Files/Zulu/zulu-21/bin');classes=OUT/'compiled-classes';classes.mkdir();inputs={str(p):sha(p) for p in [Path(__file__),source,original,candidate,mapping,native,actual]}
commands=[[str(java/'javac.exe'),'-proc:none','--release','21','-encoding','UTF-8','-cp',cp,'-d',str(classes),str(source)],[str(java/'java.exe'),'-Dfile.encoding=UTF-8','-cp',str(classes)+';'+cp,'TeaOriginalRevivePredicateAuditV1',str(original),str(candidate),str(mapping),str(actual)]]
proof=None
for index,command in enumerate(commands):
 result=subprocess.run(command,capture_output=True,timeout=90);raw=result.stdout+result.stderr
 try:text=raw.decode('utf-8')
 except UnicodeDecodeError:text=raw.decode('gb18030')
 (OUT/f'step-{index}.txt').write_text(text,encoding='utf-8');assert result.returncode==0,text[-1800:]
 if index:proof=json.loads(text)
p=data['probe'];deaths=p['teaNativeDeaths'];assert len(deaths)==1;death=deaths[0];assert not death['canceled'] and death['realPlayerSource'] and not death['state']['alive'] and death['state']['health']==0 and not death['state']['secondLifeUsed'];assert p['failures']==1 and 'Original Tea second life is naturally consumed' in p['failure'];assert data['terminal']['clientProcessReaped'] and data['terminal']['integratedWorldCompleteSave'] and data['terminal']['inputHashesUnchanged'];assert all(sha(path)==value for path,value in inputs.items());proof.update({'inputHashes':inputs,'actualNativeZeroHealthDeath':death,'firstObserverAssertionIncorrect':True,'firstFailedAttemptPreserved':True,'candidateUnchanged':True,'resourcesUploaded':False,'fullBossMigrationComplete':False});(OUT/'receipt.json').write_text(json.dumps(proof,ensure_ascii=False,indent=2)+'\n',encoding='utf-8');print(json.dumps({k:v for k,v in proof.items() if k not in ['inputHashes','actualNativeZeroHealthDeath']}),flush=True)
