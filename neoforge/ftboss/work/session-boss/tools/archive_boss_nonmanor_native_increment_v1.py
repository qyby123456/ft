import hashlib
import json
import subprocess
import sys
import zipfile
from pathlib import Path
ROOT=Path(__file__).resolve().parents[3]
WORK=ROOT/'work/session-boss'
def load(path):return json.loads(Path(path).read_text(encoding='utf-8-sig'))
def sha(path):
    with Path(path).open('rb') as stream:return hashlib.file_digest(stream,'sha256').hexdigest()
def save(path,value):
    with Path(path).open('x',encoding='utf-8') as stream:json.dump(value,stream,ensure_ascii=False,indent=2)
configuration_path=Path(sys.argv[1]).resolve();configuration=load(configuration_path)
owner=load(ROOT/'WRITE_OWNER.json');assert owner['activeWriterThreadId']==load(ROOT/'WRITE_OWNER.claim')['threadId']=='01a1c43e-ae80-7390-b604-b43b4ab53f2f'
assert owner['weaponValidationPaused'] and owner['noFurtherFormalWritesOrDeployment'] and not owner['activeRuntimePids']
query="Get-CimInstance Win32_Process -Filter \"Name='java.exe' OR Name='javaw.exe'\" | Where-Object { $_.CommandLine -match 'session-boss|session-lease' } | Select-Object -ExpandProperty ProcessId"
assert not subprocess.check_output(['powershell','-NoProfile','-Command',query],encoding='utf-8-sig').strip()
OUT=ROOT/configuration['archiveReportDirectory'];OUT.mkdir()
paths={Path(__file__),configuration_path}
for relative in configuration['sources']:paths.add(ROOT/relative)
reports=[]
for relative in configuration['nativeReports']:
    path=ROOT/relative;data=load(path);terminal=data['terminal']
    assert terminal['exitCode']==0 and terminal['processReaped'] and terminal['completeSave'] and terminal['inputHashesUnchanged'] and terminal['criticalServerConfigurationPreserved'],relative
    for name,expected in data['inputHashes'].items():assert sha(name)==expected,name
    paths.add(path);paths.update(item for item in Path(data['attempt']).rglob('*') if item.is_file())
    reports.append({'relativePath':relative,'sha256':sha(path),'checks':data['probe']['checks'] if data.get('probe') else 0,'failures':data['probe']['failures'] if data.get('probe') else None,'ordinaryAdmissionAccepted':data.get('actualNormalBossAdmissionBaselineAccepted',False),'ordinaryReloadAccepted':data.get('ordinaryNoPlayerNewJvmEntityReloadAccepted',False)})
for relative in configuration['evidenceDirectories']:paths.update(path for path in (ROOT/relative).rglob('*') if path.is_file())
base=WORK/'archives/boss-core-stage28-zicai-curio-scoped-evidence-v3-20261113.zip';assert sha(base)=='aca9e96def7887e0cf52637152df8922174326d5970376918d42b08bee864929'
with zipfile.ZipFile(base) as bundle:available=set(bundle.namelist())
rows={};unique={}
for path in sorted(paths):
    assert path.is_relative_to(ROOT) and path.is_file() and not path.is_symlink(),path
    digest=sha(path);layer='base' if 'blobs/'+digest in available else 'self';rows[path.relative_to(ROOT).as_posix()]={'bytes':path.stat().st_size,'sha256':digest,'layer':layer}
    if layer=='self':unique[digest]=path
archive=ROOT/configuration['archiveRelativePath']
with zipfile.ZipFile(archive,'x',compression=zipfile.ZIP_DEFLATED,compresslevel=6) as bundle:
    bundle.writestr('ARCHIVE_MANIFEST.json',json.dumps({'format':'ftboss-content-addressed-increment-v1','baseArchiveSha256':sha(base),'files':rows},ensure_ascii=False,indent=2).encode('utf-8'))
    for digest,path in sorted(unique.items()):bundle.write(path,'blobs/'+digest)
with (OUT/'archive-validation.log').open('xb') as stream:subprocess.run([sys.executable,'-B',str(WORK/'tools/restore_boss_zicai_curio_archive_v2.py'),str(archive),'--base',str(base)],stdout=stream,stderr=subprocess.STDOUT,check=True)
for relative,row in rows.items():assert (ROOT/relative).stat().st_size==row['bytes'] and sha(ROOT/relative)==row['sha256'],relative
receipt={'archiveRelativePath':archive.relative_to(ROOT).as_posix(),'archiveSha256':sha(archive),'archiveBytes':archive.stat().st_size,'baseArchiveRelativePath':base.relative_to(ROOT).as_posix(),'baseArchiveSha256':sha(base),'paths':len(rows),'allReferencedPayloadHashesVerified':True,'allLocalInputPathsRehashedAfterArchive':True,'nativeReports':reports,'allOriginalsAndFailedEvidenceRetained':True,'fullPhysicalRestoreClaimed':False,'rawArtifactsRemoteRecoveryVerified':False,'productChanged':False,'authenticatedSummonAccepted':False,'curiosActualPlayerNewJvmLoginAccepted':False,'installable':False,'deployed':False}
save(OUT/'archive-receipt.json',receipt);print(json.dumps(receipt),flush=True)
