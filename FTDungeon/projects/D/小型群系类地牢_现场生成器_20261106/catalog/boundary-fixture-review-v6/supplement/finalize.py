import hashlib
import json
from pathlib import Path

ROOT=Path(__file__).resolve().parent
report=json.loads((ROOT/'feet-check.json').read_text('utf-8'))
if report['checks']!=352 or report['endpoints']!=704 or report['nonfiniteFeet'] or report['outsideVanillaOverworldCoordinateBounds']:
    raise ValueError('unexpected boundary endpoint evidence')
files=[{'path':path.name,'sha256':hashlib.sha256(path.read_bytes()).hexdigest()} for path in sorted(ROOT.iterdir()) if path.is_file() and path.name!='ready.json']
ready={'component':'catalog-boundary-fixture-feet-supplement','ready':True,'status':'READ_ONLY_DOUBLE_ENDPOINT_RECOMPUTATION_COMPLETE','nativeJVMStarted':False,'integrationFilesModified':False,'checks':352,'endpoints':704,'nonfiniteFeetCount':0,'outsideVanillaCoordinateBoundsCount':0,'customWorldBorderAndChunkState':'REQUIRES_NATIVE_FIXTURE_CHECK','rootCauseReference':'../findings.json','notification':'Coordinator explicitly authorized direct integration message; findings and finite endpoint evidence delivered','files':files}
(ROOT/'ready.json').write_text(json.dumps(ready,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
print('SEALED feet supplement; no nativePASS claim')
