import hashlib
import json
import subprocess
import sys
import zipfile
from pathlib import Path
ROOT=Path(__file__).resolve().parents[3]
WORK=ROOT/'work/session-boss'
OUT=WORK/'reports/nonmanor-normal-admission-archive-v1-20261114'
def load(path):return json.loads(Path(path).read_text(encoding='utf-8-sig'))
def sha(path):
    with Path(path).open('rb') as stream:return hashlib.file_digest(stream,'sha256').hexdigest()
def save(path,value):
    with Path(path).open('x',encoding='utf-8') as stream:json.dump(value,stream,ensure_ascii=False,indent=2)
owner=load(ROOT/'WRITE_OWNER.json');assert owner['activeWriterThreadId']==load(ROOT/'WRITE_OWNER.claim')['threadId']=='01a1c43e-ae80-7390-b604-b43b4ab53f2f'
assert owner['weaponValidationPaused'] and owner['noFurtherFormalWritesOrDeployment'] and not owner['activeRuntimePids']
query="Get-CimInstance Win32_Process -Filter \"Name='java.exe' OR Name='javaw.exe'\" | Where-Object { $_.CommandLine -match 'session-boss|session-lease' } | Select-Object -ExpandProperty ProcessId";assert not subprocess.check_output(['powershell','-NoProfile','-Command',query],encoding='utf-8-sig').strip()
report_path=WORK/'reports/nonmanor-normal-native-v2-247a4b31.json';report=load(report_path)
assert report['actualNormalBossAdmissionBaselineAccepted'] and report['probe']['checks']==2189 and report['probe']['failures']==0 and report['terminal']['completeSave'] and report['terminal']['exitCode']==0
for name,expected in report['inputHashes'].items():assert sha(name)==expected,name
failed_path=WORK/'reports/nonmanor-normal-native-v1-8bdc8468.json';failed=load(failed_path);assert not failed['actualNormalBossAdmissionBaselineAccepted'] and failed['terminal']['processReaped']
for name,expected in failed['inputHashes'].items():assert sha(name)==expected,name
OUT.mkdir();seed=Path(report['attempt'])/'server/world';assert seed.is_dir()
seed_files={path.relative_to(seed).as_posix():{'bytes':path.stat().st_size,'sha256':sha(path)} for path in sorted(seed.rglob('*')) if path.is_file()}
save(OUT/'accepted-seed-world-manifest.json',{'seedReportRelativePath':report_path.relative_to(ROOT).as_posix(),'seedReportSha256':sha(report_path),'seedWorldRelativePath':seed.relative_to(ROOT).as_posix(),'allSourceNormalSaveAndExitChecksAccepted':True,'failedWorldAllowedAsSeed':False,'files':seed_files})
source_relatives=['work/session-boss/tools/dedup_closed_v18_common_mods_v1.py','work/session-boss/tools/dedup_archived_stage28_runtime_jars_v1.py','work/session-boss/tests/NonManorNormalBossProbeV1.java','work/session-boss/tests/NonManorNormalBossProbeV2.java','work/session-boss/tools/run_boss_nonmanor_normal_native_v1.py','work/session-boss/tools/run_boss_nonmanor_normal_native_v2.py','work/session-boss/tools/archive_boss_nonmanor_normal_admission_v1.py','work/session-boss/NONMANOR_NORMAL_BOSS_ADMISSION_20261114.md','github-sync/checkpoint-nonmanor-ordinary-entity-join-20261114.json']
paths={ROOT/relative for relative in source_relatives};paths.update([report_path,failed_path,OUT/'accepted-seed-world-manifest.json'])
for data in [report,failed]:paths.update(path for path in Path(data['attempt']).rglob('*') if path.is_file())
for relative in ['work/session-boss/reports/closed-v18-common-readonly-alias-v1-20261114','work/session-boss/reports/closed-stage28-runtime-jar-readonly-alias-v1-20261114','work/session-boss/reports/nonmanor-normal-native-preflight-v1-20261114']:paths.update(path for path in (ROOT/relative).rglob('*') if path.is_file())
base=WORK/'archives/boss-core-stage28-zicai-curio-scoped-evidence-v3-20261113.zip';assert sha(base)=='aca9e96def7887e0cf52637152df8922174326d5970376918d42b08bee864929'
with zipfile.ZipFile(base) as bundle:available=set(bundle.namelist())
rows={};unique={}
for path in sorted(paths):
    assert path.is_relative_to(ROOT) and not path.is_symlink();digest=sha(path);layer='base' if 'blobs/'+digest in available else 'self';rows[path.relative_to(ROOT).as_posix()]={'bytes':path.stat().st_size,'sha256':digest,'layer':layer}
    if layer=='self':unique[digest]=path
archive=WORK/'archives/boss-nonmanor-ordinary-admission-v1-20261114.zip'
with zipfile.ZipFile(archive,'x',compression=zipfile.ZIP_DEFLATED,compresslevel=6) as bundle:
    bundle.writestr('ARCHIVE_MANIFEST.json',json.dumps({'format':'ftboss-content-addressed-increment-v1','baseArchiveSha256':sha(base),'files':rows},ensure_ascii=False,indent=2).encode('utf-8'))
    for digest,path in sorted(unique.items()):bundle.write(path,'blobs/'+digest)
with (OUT/'archive-validation.log').open('xb') as stream:subprocess.run([sys.executable,'-B',str(WORK/'tools/restore_boss_zicai_curio_archive_v2.py'),str(archive),'--base',str(base)],stdout=stream,stderr=subprocess.STDOUT,check=True)
for relative,row in seed_files.items():assert (seed/relative).stat().st_size==row['bytes'] and sha(seed/relative)==row['sha256']
receipt={'archiveRelativePath':archive.relative_to(ROOT).as_posix(),'archiveSha256':sha(archive),'archiveBytes':archive.stat().st_size,'baseArchiveRelativePath':base.relative_to(ROOT).as_posix(),'baseArchiveSha256':sha(base),'paths':len(rows),'allReferencedPayloadHashesVerified':True,'acceptedNativeChecks':2189,'acceptedOrdinaryNoPlayerEntityCases':20,'sourceCandidateUnchanged':True,'seedWorldManifestRelativePath':(OUT/'accepted-seed-world-manifest.json').relative_to(ROOT).as_posix(),'seedWorldManifestSha256':sha(OUT/'accepted-seed-world-manifest.json'),'seedWorldFiles':len(seed_files),'seedWorldBytes':sum(row['bytes'] for row in seed_files.values()),'allOriginalsAndFailedV1EvidenceRetained':True,'fullPhysicalRestoreClaimed':False,'rawArtifactsRemoteRecoveryVerified':False,'newJvmReloadAccepted':False,'authenticatedSummonAccepted':False,'installable':False,'deployed':False}
save(OUT/'archive-receipt.json',receipt);print(json.dumps(receipt),flush=True)
