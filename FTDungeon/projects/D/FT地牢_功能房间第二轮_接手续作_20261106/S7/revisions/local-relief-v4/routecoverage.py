import json
import shutil
import subprocess
import sys
from pathlib import Path

sys.dont_write_bytecode = True
ROOT = Path(__file__).resolve().parent
sys.path.insert(0,str(ROOT))
from qa import OLD, save

assembly = json.loads((ROOT/'assembly/assembly.json').read_text(encoding='utf-8'))
original = json.loads((ROOT/'assembly/qa/route-input.json').read_text(encoding='utf-8'))[0]
targets = list(original['targets'])
for link_number, link in enumerate(assembly['links']):
    path = link['centerline_xz']
    for step, center in enumerate(path):
        previous = path[max(0,step-1)]
        following = path[min(len(path)-1,step+1)]
        delta_east = following[0]-center[0] if following!=center else center[0]-previous[0]
        delta_south = following[1]-center[1] if following!=center else center[1]-previous[1]
        foot = link['start'][1]+round((link['end'][1]-link['start'][1])*2*step/max(1,len(path)-1))/2
        for across in [-1,0,1]:
            targets.append({'label':f'link-{link_number+1}-step-{step}-strip-{across}','position':[center[0]-delta_south*across,foot,center[1]+delta_east*across]})
output = ROOT/'assembly/qa/full-width-routes'
output.mkdir(exist_ok=True)
save(output/'route-input.json',[{**original,'targets':targets}])
shutil.copyfile(OLD/'vanilla-block-tags.json',output/'vanilla-block-tags.json')
dependencies = Path(r'D:\ft工程文件\霜雪花园地牢_20261101\build\classpath\runData_minecraftClasspath.txt').read_text(encoding='utf-8').splitlines()
dependencies = [entry for entry in dependencies if entry and 'minecraft_user_repo' not in entry]
classpath = ';'.join([str(OLD/'classes'),r'D:\ft工程文件\锈铜宝库地牢_20261101\qa\vanilla-1.20.1-mapped.jar']+dependencies)
command = [r'C:\Program Files\Common Files\Oracle\Java\javapath\java.exe','-Xmx1536m','-Djava.awt.headless=true','-cp',classpath,'IndependentRouteAudit',str(output)]
result = subprocess.run(command,cwd=output,capture_output=True,text=True,encoding='utf-8',errors='replace',timeout=180)
(output/'run.log').write_text(result.stdout+result.stderr,encoding='utf-8')
if result.returncode:
    raise RuntimeError('Route invocation failed')
report = json.loads((output/'independent-routes.json').read_text(encoding='utf-8'))[0]
failures = [target for target in report['targets'] if not target['reachable'] or not target['standing_clear']]
save(output/'summary.json',{'sha256':assembly['sha256'],'targets':len(targets),'failures':failures,'start_clear':report['start_clear'],'status':'SELF_CHECK_PASS' if not failures and report['start_clear'] else 'NEEDS_FIX','scope':'Every connector centerline and three-wide perpendicular strip at each longitudinal step, plus original room goals; static 0.6x1.8 native collision. Author-executed, not independent approval.'})
print(len(targets),'targets',len(failures),'failures',flush=True)
if failures:
    raise SystemExit(2)
