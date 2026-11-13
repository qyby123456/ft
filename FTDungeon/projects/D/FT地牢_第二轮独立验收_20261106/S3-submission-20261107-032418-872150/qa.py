import hashlib
import json
import os
from pathlib import Path
import subprocess
import sys
ROOT=Path(__file__).resolve().parent
OLD=Path('D:/ft工程文件/FT地牢_独立复核_20261104_01a1a685')
MAPPED=Path('D:/ft工程文件/锈铜宝库地牢_20261101/qa/vanilla-1.20.1-mapped.jar')
SUPPORT=Path('D:/ft工程文件/霜雪花园地牢_20261101/build/classpath/runData_minecraftClasspath.txt')
def digest(file):
    return hashlib.sha256(file.read_bytes()).hexdigest()
def main():
    room=ROOT/'rooms'/sys.argv[1]
    qa=room/'qa'
    qa.mkdir(exist_ok=True)
    design=json.loads((room/'design.json').read_text(encoding='utf-8'))
    candidate=room/(sys.argv[1]+'.nbt')
    checksum=digest(candidate)
    specification=dict(group=3 if sys.argv[1]=='s3-merged' else int(sys.argv[1].split('-')[-1]),candidate=str(candidate),sha256=checksum,swimming=False,start=design['start'],targets=design['targets'])
    (qa/'route-input.json').write_text(json.dumps([specification]),encoding='utf-8')
    (qa/'file-audit.json').write_text(json.dumps({'groups':[specification]}),encoding='utf-8')
    classpath=os.pathsep.join([str(OLD/'classes'),str(MAPPED),*[line for line in SUPPORT.read_text(encoding='utf-8').splitlines() if line.strip() and 'minecraft_user_repo' not in line]])
    commands=[]
    for entry in ['IndependentNativeAudit','IndependentRouteAudit']:
        command=['java','-Xmx1536m','-Djava.awt.headless=true','-cp',classpath,entry,str(qa)]
        commands.append(command)
        result=subprocess.run(command,cwd=qa,capture_output=True,text=True,encoding='utf-8',errors='replace',timeout=240,creationflags=subprocess.CREATE_NO_WINDOW)
        (qa/(entry+'.log')).write_text(result.stdout+'\n'+result.stderr,encoding='utf-8')
        if result.returncode:
            raise RuntimeError(entry+' failed: '+result.stderr[-1500:])
    native=json.loads((qa/'independent-native.json').read_text())[0]
    routes=json.loads((qa/'independent-routes.json').read_text())[0]
    cells=json.loads((room/'cells.json').read_text())
    inventory={}
    entities=[]
    for cell in cells:
        block=cell['state'].split('[')[0]
        inventory[block]=inventory.get(block,0)+1
        if cell['entity']:
            entities.append(cell)
            assert not cell['entity'].get('Items')
    passed=native['registry_properties']=='PASS' and native['template_roundtrip_block_count']=='PASS' and not native['survival_failures'] and not native['survival_unsupported'] and routes['start_clear'] and all(target['reachable'] for target in routes['targets'])
    report={'candidate_sha256':checksum,'self_check':'PASS' if passed else 'FAIL','independent_acceptance':'PENDING','inventory':inventory,'block_entities':entities,'fluid_or_lava':[cell for cell in cells if cell['state'].split('[')[0] in ['water','lava','powder_snow','ice']],'native_class_sha256':{entry:digest(OLD/'classes'/(entry+'.class')) for entry in ['IndependentNativeAudit','IndependentRouteAudit']},'commands':commands,'not_run':['世界放置与邻居更新','随机/流体tick','实际照明','生存/战斗验收'],'scope':'制作方复用只读原生工具的新候选检查，不是独立验收；碰撞限制见independent-routes.json'}
    (qa/'report.json').write_text(json.dumps(report,ensure_ascii=False,indent=2),encoding='utf-8')
    progress_path=ROOT/'progress.json'
    progress=json.loads(progress_path.read_text(encoding='utf-8')) if progress_path.exists() else {'style':'S3','rooms':{},'assembly':'NOT_STARTED'}
    progress['current']=sys.argv[1]
    progress['rooms'][sys.argv[1]]={'sha256':checksum,'self_check':report['self_check'],'visual_review':'PENDING','independent_acceptance':'PENDING'}
    progress_path.write_text(json.dumps(progress,ensure_ascii=False,indent=2),encoding='utf-8')
    print(json.dumps({'room':sys.argv[1],'self_check':report['self_check'],'native':native,'routes':routes},ensure_ascii=False,indent=2))
    if not passed:
        sys.exit(1)
if __name__=='__main__':
    main()
