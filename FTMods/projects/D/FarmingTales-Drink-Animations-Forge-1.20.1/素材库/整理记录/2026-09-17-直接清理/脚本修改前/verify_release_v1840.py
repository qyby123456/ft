from pathlib import Path
import json,hashlib,zipfile,re,xml.etree.ElementTree as ET
from research_letsdo import TARGETS

ROOT=Path(__file__).absolute().parent
BACKUP=ROOT/'backups/1.8.39-before-brewery-vinery'
ASSETS=ROOT/'src/main/resources/assets/farmingtales_drink_anim'
read=lambda p:json.loads(p.read_text(encoding='utf-8-sig'))
sha=lambda p:hashlib.sha256(p.read_bytes()).hexdigest()
baseline=read(BACKUP/'verification.json');profiles=read(ASSETS/'drink_profiles.json')
ids={ns+':'+name for ns,names in TARGETS.items() for name in names}
assert len(ids)==41 and len(profiles)==263
assert [p for p in profiles if p['id'] not in ids]==baseline['profiles']
assert {p['id'] for p in profiles if p['id'] in ids}==ids
foam=read(ASSETS/'beer_foam.json');assert len(foam['items'])==6
for p in profiles:
 if p['id'] not in ids:continue
 assert p['style']==('CUP' if p['id'] in foam['items'] else 'WINE')
 for name in [p['bodyModel'],p['capModel'],p['neckModel']]:
  m=read(ASSETS/f'models/{name}.json')
  for e in m['elements']:
   for face in e['faces'].values():assert face['texture'][1:] in m['textures']
  if p['id'] in foam['items'] and name==p['bodyModel']:
   assert all(max(e['from'][0],e['to'][0])<=11 for e in m['elements']),p['id']+' still has its handle'
 if p['style']=='WINE':
  cap=read(ASSETS/f"models/{p['capModel']}.json")['elements'][0]
  neck=read(ASSETS/f"models/{p['neckModel']}.json")['elements'][0]
  for axis in (0,2):assert neck['from'][axis]>cap['from'][axis] and neck['to'][axis]<cap['to'][axis]
for name in ['decorations','hand_volumes']:
 old=read(BACKUP/f'src/main/resources/assets/farmingtales_drink_anim/{name}.json')
 now=read(ASSETS/(name+'.json'));assert {k:v for k,v in now.items() if k not in ids}==old
for source in read(ROOT/'letsdo-sources.json')['jars']:assert sha(Path(source['path']))==source['sha256']
config=Path('Z:/config/farmingtales_drink_anim-client.toml');assert sha(config)==sha(BACKUP/config.name)
tests=[ET.parse(p).getroot() for p in (ROOT/'build/test-results/test').glob('TEST-*.xml')]
assert all(int(t.get('failures'))==int(t.get('errors'))==int(t.get('skipped'))==0 for t in tests)
count=sum(int(t.get('tests')) for t in tests);assert count==146
jar=ROOT/'build/libs/farmingtales_drink_anim-1.8.40.jar'
with zipfile.ZipFile(jar) as z:
 for p in (ROOT/'src/main/resources').rglob('*'):
  if p.is_file():assert z.read(p.relative_to(ROOT/'src/main/resources').as_posix())==p.read_bytes(),p
 meta=z.read('META-INF/mods.toml').decode();assert all(s in meta for s in ['version="1.8.40"','license="ARR"','authors="紫菜"'])
 assert not any('drinkanim/smoke/' in p for p in z.namelist())
runs={}
resolved_ids={'vinery:strad_wine','vinery:stal_wine','vinery:glowing_wine','vinery:aegis_wine'}
for name,total in [('slim',41),('wide',41),('slim-final',10),('slim-grip',6),('wide-grip',1),('slim-fixes',10),('wide-fixes',5)]:
 path=ROOT/f'preview/smoke-letsdo-{name}-1.8.40.log';s=path.read_text(encoding='utf-8-sig')
 assert f'CATALOG_SMOKE_COMPLETE {total}' in s and 'BUILD SUCCESSFUL' in s and 'SILENT_CLIENT' in s
 failures=[line for line in s.splitlines() if 'CATALOG_SMOKE FAIL' in line or 'DRINK_SMOKE FAIL' in line]
 if name in ('slim','wide'):
  assert len(failures)==5 and {re.search(r'CATALOG_SMOKE FAIL (\S+)',line)[1] for line in failures}==resolved_ids
 else:assert not failures,(name,failures)
 if name.endswith('-fixes'):
  for item in resolved_ids:
   assert f'CATALOG_SMOKE PASS {item} body neck and decoration textures are present' in s
   assert f'CATALOG_SMOKE PASS {item} drink consumes once and returns to idle' in s
 runs[name]=dict(path=str(path),sha256=sha(path),profiles=total,hidden=True,shaders=False,
  passed=not failures,historical_failures=failures,resolved_by=name+'-fixes' if failures else None)
assert 'enableShaders=false' in (ROOT/'run-smoke/config/oculus.properties').read_text()
folder=ROOT/'blockbench/葡园精酿酒水动画-v70';projects=read(folder/'导出检查.json');fit=read(ROOT/'preview/bb-fit-v70.json')
assert len(projects)==len(fit)==82 and {p['item'] for p in projects}==ids
archive=folder.with_suffix('.zip')
with zipfile.ZipFile(archive) as z:
 assert sum(p.endswith('.bbmodel') for p in z.namelist())==82
 for p in projects:
  path=Path(p['path']);assert z.read(path.name)==path.read_bytes();p['sha256']=sha(path)
  m=read(path);assert len(m['animations'])==13
  if p['item'] in foam['items']:assert sum(g['name'].startswith('foam_') for g in m['groups'])==36
visual=read(ROOT/'preview/letsdo-review-1.8.40.json')
for entry in visual['sheets']:assert sha(Path(entry['path']))==entry['sha256']
report=dict(version='1.8.40',sha256=sha(jar),unit_tests=count,profiles=profiles,added_profiles=sorted(ids),
 blockbench=projects,blockbench_fit=fit,blockbench_archives=[dict(path=str(archive),sha256=sha(archive))],
 client_runs=runs,client_retested=True,visual_review=visual,soundVolume=.8,inspectionVolumeMultiplier=1.7,
 liquidGainMultiplier=.5,switch_ticks=dict(draw=10,stow=9),configuration_sha256=sha(config),
 scope='35 Brewery/Vinery bottles and 6 Brewery beers. Bottle-specific closures and geometry; held beer mugs omit handles, use can-style body contact, and have a raised bounded animated foam layer. Previous profiles, source mods and sound settings retained.',
 inherited_catalog_evidence=str(BACKUP/'verification.json'))
(ROOT/'verification.json').write_text(json.dumps(report,ensure_ascii=False,indent=2),encoding='utf-8')
print('Verified 1.8.40:',count,'tests; 41 new drinks; 82 Blockbench projects;',sha(jar))
