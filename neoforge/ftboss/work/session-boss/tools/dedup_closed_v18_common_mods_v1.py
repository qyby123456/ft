import hashlib
import json
import os
import shutil
import stat
import subprocess
import sys
import zipfile
from pathlib import Path
ROOT=Path(__file__).resolve().parents[3]
WORK=ROOT/'work/session-boss'
OUT=WORK/'reports/closed-v18-common-readonly-alias-v1-20261114'
CACHE=WORK/'cache/common-mods-sha256-v1'
def load(path):return json.loads(Path(path).read_text(encoding='utf-8-sig'))
def sha(path):
    with Path(path).open('rb') as stream:return hashlib.file_digest(stream,'sha256').hexdigest()
def save(path,value):
    with Path(path).open('x',encoding='utf-8') as stream:json.dump(value,stream,ensure_ascii=False,indent=2)
owner=load(ROOT/'WRITE_OWNER.json');assert owner['activeWriterThreadId']==load(ROOT/'WRITE_OWNER.claim')['threadId']=='01a1c43e-ae80-7390-b604-b43b4ab53f2f'
assert owner['weaponValidationPaused'] and owner['noFurtherFormalWritesOrDeployment'] and not owner['activeRuntimePids']
query="Get-CimInstance Win32_Process -Filter \"Name='java.exe' OR Name='javaw.exe'\" | Where-Object { $_.CommandLine -match 'session-boss|session-lease' } | Select-Object -ExpandProperty ProcessId"
assert not subprocess.check_output(['powershell','-NoProfile','-Command',query],encoding='utf-8-sig').strip()
report_path=WORK/'reports/zicai-curio-client-v18-candidate-a56d7243.json';report=load(report_path)
assert report['terminal']['exitCode']==0 and report['terminal']['clientProcessReaped'] and report['probe']['failures']==1
for name,expected in report['inputHashes'].items():assert sha(name)==expected,name
archive_receipt=WORK/'reports/github-attended-reload-v18-diagnostic-publication-v1-20261114/archive-receipt.json';receipt=load(archive_receipt)
archive=ROOT/receipt['archiveRelativePath'];base=ROOT/receipt['baseArchiveRelativePath'];assert sha(archive)==receipt['archiveSha256'] and sha(base)==receipt['baseArchiveSha256']
OUT.mkdir()
with (OUT/'archive-full-payload-validation.log').open('xb') as stream:subprocess.run([sys.executable,'-B',str(WORK/'tools/restore_boss_zicai_curio_archive_v2.py'),str(archive),'--base',str(base)],stdout=stream,stderr=subprocess.STDOUT,check=True)
with zipfile.ZipFile(archive) as bundle:manifest=load_manifest=json.loads(bundle.read('ARCHIVE_MANIFEST.json'))['files']
mods=Path(report['attempt'])/'game/mods';rows={}
for path in sorted(mods.glob('*.jar')):
    digest=sha(path);cached=CACHE/(digest+'.jar')
    if not cached.is_file() or path.stat().st_ino==cached.stat().st_ino:continue
    relative=path.relative_to(ROOT).as_posix();row=manifest[relative]
    assert path.resolve().is_relative_to((WORK/'build').resolve()) and not path.is_symlink() and not cached.is_symlink()
    assert row['sha256']==digest==sha(cached) and row['bytes']==path.stat().st_size==cached.stat().st_size
    assert path.stat().st_nlink==1 and not (cached.stat().st_mode&stat.S_IWRITE)
    rows[relative]={'sha256':digest,'bytes':row['bytes'],'cacheRelativePath':cached.relative_to(ROOT).as_posix(),'beforeFileId':path.stat().st_ino}
assert len(rows)==27 and sum(row['bytes'] for row in rows.values())==157100824
save(OUT/'plan.json',{'onlyClosedArchivedV18CommonImmutableModCopies':True,'allCandidateAndCacheBytesVerified':True,'failedWorldAndEveryInputPathRetained':True,'newInstancesMustIndependentlyCopy':True,'nativeGameProcesses':[],'archiveReceiptSha256':sha(archive_receipt),'paths':rows})
free_before=shutil.disk_usage(ROOT).free
for relative,row in rows.items():
    path=ROOT/relative;cached=ROOT/row['cacheRelativePath'];assert sha(path)==sha(cached)==row['sha256']
    temporary=path.with_name(path.name+'.v18-readonly-alias.tmp');assert not temporary.exists()
    os.link(cached,temporary);assert sha(temporary)==row['sha256']
    readonly=not(path.stat().st_mode&stat.S_IWRITE)
    if readonly:os.chmod(path,stat.S_IREAD|stat.S_IWRITE)
    try:os.replace(temporary,path)
    except BaseException:
        if readonly and path.exists():os.chmod(path,stat.S_IREAD)
        save(OUT/'partial-failure.json',{'currentPath':relative,'mustInspectActualAliasesBeforeAnyRetry':True});raise
    assert path.stat().st_ino==cached.stat().st_ino and sha(path)==row['sha256']
    os.chmod(cached,stat.S_IREAD)
    with (OUT/'journal.jsonl').open('a',encoding='utf-8') as stream:stream.write(json.dumps({'path':relative,'sha256':row['sha256'],'afterFileId':path.stat().st_ino,'allPathsRetained':True})+'\n')
for name,expected in report['inputHashes'].items():assert sha(name)==expected,name
result={'aliasesCreated':len(rows),'logicalDuplicateBytes':sum(row['bytes'] for row in rows.values()),'freeBytesBefore':free_before,'freeBytesAfter':shutil.disk_usage(ROOT).free,'allNativeFrozenInputsUnchanged':True,'allOriginalPathsRetained':True,'allAliasesReadonly':True,'canonicalSourcesWorldsAndFormalInstancesNotModified':True,'largeArtifactRemoteRecoveryClaimed':False,'newInstancesMustIndependentlyCopy':True}
save(OUT/'receipt.json',result);print(json.dumps(result),flush=True)
