import hashlib
import json
import re
import subprocess
from pathlib import Path
ROOT=Path(__file__).resolve().parents[3]
WORK=ROOT/'work/session-boss'
OUT=WORK/'build/boss-gongdi-melee-audit-v1-20261113'
def sha(path):
    with Path(path).open('rb') as stream:return hashlib.file_digest(stream,'sha256').hexdigest()
def save(path,data):
    with path.open('x',encoding='utf-8') as stream:json.dump(data,stream,ensure_ascii=False,indent=2)
release=WORK/'releases/boss-core-stage17-gongdi-living-target-20261113';deployed=json.loads((release/'deployment-receipt.json').read_text(encoding='utf-8'));archived=json.loads((release/'archive-receipt.json').read_text(encoding='utf-8'))
current=release/'ftboss-boss-core-1.21.1-stage17-gongdi-living-target.jar';original=ROOT/'reference/forge-current.jar'
assert sha(current)==sha(deployed['target'])==deployed['deployedSha256']=='021de8b498baf3d215220ca2399e68471864872a7be75b775bb6ba3e588d7162'
assert sha(original)=='c1734a148606397f8b2c79fbcc5c45961a5ffb1923ea051756fd45d03dc7a942'
assert sha(archived['archive'])==archived['archiveSha256'] and archived['failures']==0
assert not OUT.exists();OUT.mkdir()
for label,path in [('original',original),('current',current)]:
    for short in ['entity.EasterBossEntity','boss.ArmorShred','util.GongdiCombatRules']:
        result=subprocess.run(['C:/Program Files/Zulu/zulu-21/bin/javap.exe','-p','-c','-constants','-classpath',str(path),'net.farmingtales.ftboss.'+short],capture_output=True,creationflags=subprocess.CREATE_NO_WINDOW);assert result.returncode==0
        (OUT/(label+'-'+short+'.javap.txt')).write_bytes(result.stdout)
        text=result.stdout.decode('gb18030',errors='replace').replace('\r','')
        if short=='entity.EasterBossEntity':
            blocks=re.split(r'(?=^  (?:public|private|protected|static) )',text,flags=re.M)
            selected=[value for value in blocks if value.startswith('  private void onWeaponHit') or value.startswith('  public boolean doHurtTarget') or value.startswith('  public boolean m_7327_') or value.startswith('  public void onSignatureCleaveHit')]
            (OUT/(label+'-selected-entry-methods.txt')).write_text('\n'.join(selected),encoding='utf-8')
save(OUT/'preflight.json',{'stage17Deployment':str(release/'deployment-receipt.json'),'stage17Archive':str(release/'archive-receipt.json'),'inputs':{str(path):sha(path) for path in [Path(__file__),current,original]},'sourceAuditOnly':True,'nativeBaselinePending':True,'noProductChange':True,'fullBossGoalActive':True,'weaponsPaused':True})
print(json.dumps({'audit':str(OUT),'stage17DeployedArchivedVerified':True,'nativeMeleeBaselinePending':True}),flush=True)
