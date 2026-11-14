import hashlib
import json
import os
import shutil
import stat
import subprocess
import zipfile
from pathlib import Path
ROOT=Path(__file__).resolve().parents[3]
WORK=ROOT/'work/session-boss'
OUT=WORK/'reports/closed-stage28-runtime-jar-readonly-alias-v1-20261114'
CACHE=WORK/'cache/archived-jars-sha256-v1'
EXPECTED='c45cc10ffb7b572d876e869b692a75496c88c337434ed7140225df298b7bb65e'
BASE_SHA='aca9e96def7887e0cf52637152df8922174326d5970376918d42b08bee864929'
def load(path):return json.loads(Path(path).read_text(encoding='utf-8-sig'))
def sha(path):
    with Path(path).open('rb') as stream:return hashlib.file_digest(stream,'sha256').hexdigest()
def save(path,value):
    with Path(path).open('x',encoding='utf-8') as stream:json.dump(value,stream,ensure_ascii=False,indent=2)
owner=load(ROOT/'WRITE_OWNER.json');assert owner['activeWriterThreadId']==load(ROOT/'WRITE_OWNER.claim')['threadId']=='01a1c43e-ae80-7390-b604-b43b4ab53f2f'
assert owner['weaponValidationPaused'] and owner['noFurtherFormalWritesOrDeployment'] and not owner['activeRuntimePids']
query="Get-CimInstance Win32_Process -Filter \"Name='java.exe' OR Name='javaw.exe'\" | Where-Object { $_.CommandLine -match 'session-boss|session-lease' } | Select-Object -ExpandProperty ProcessId"
assert not subprocess.check_output(['powershell','-NoProfile','-Command',query],encoding='utf-8-sig').strip()
base=WORK/'archives/boss-core-stage28-zicai-curio-scoped-evidence-v3-20261113.zip';assert sha(base)==BASE_SHA
archives={str(base):BASE_SHA}
for receipt_path in sorted((WORK/'reports').glob('github-*/archive-receipt.json')):
    receipt=load(receipt_path)
    if 'archiveRelativePath' not in receipt:continue
    archive=ROOT/receipt['archiveRelativePath'];assert archive.resolve().is_relative_to((WORK/'archives').resolve()) and sha(archive)==receipt['archiveSha256']
    archives[str(archive)]=receipt['archiveSha256']
rows={};payload_verified=set()
for archive_name,archive_hash in archives.items():
    with zipfile.ZipFile(archive_name) as bundle:
        manifest=json.loads(bundle.read('ARCHIVE_MANIFEST.json'))
        if manifest['format']=='ftboss-content-addressed-increment-v1':assert manifest['baseArchiveSha256']==BASE_SHA
        elif manifest['format']!='ftboss-content-addressed-evidence-v1':continue
        for relative,row in manifest['files'].items():
            if row['sha256']!=EXPECTED or '/game/mods/' not in relative or not relative.endswith('.jar') or not relative.startswith('work/session-boss/build/zicai-curio-client-'):continue
            path=ROOT/relative;assert path.resolve().is_relative_to((WORK/'build').resolve()) and not path.is_symlink() and sha(path)==EXPECTED and path.stat().st_size==row['bytes']
            layer=row.get('layer','self');payload_archive=Path(archive_name) if layer=='self' else base;assert layer in ['self','base']
            payload_key=(str(payload_archive),EXPECTED)
            if payload_key not in payload_verified:
                with zipfile.ZipFile(payload_archive) as payload_bundle:payload=payload_bundle.read('blobs/'+EXPECTED)
                assert hashlib.sha256(payload).hexdigest()==EXPECTED and len(payload)==row['bytes'];payload_verified.add(payload_key)
            rows[relative]={'sha256':EXPECTED,'bytes':row['bytes'],'bindingArchive':str(Path(archive_name).relative_to(ROOT)),'bindingArchiveSha256':archive_hash,'beforeFileId':path.stat().st_ino}
assert len(rows)>=2;OUT.mkdir();free_before=shutil.disk_usage(ROOT).free
save(OUT/'plan.json',{'onlyClosedArchivedStage28RuntimeBossJarCopies':True,'canonicalBuildJarFormalInstancesWorldsAndWeaponsExcluded':True,'verifiedSelectedArchivePayloads':len(payload_verified),'allPathsAndBytesMustRemain':True,'paths':rows})
cached=CACHE/(EXPECTED+'.jar');first=ROOT/next(iter(rows))
if not cached.exists():os.link(first,cached)
assert sha(cached)==EXPECTED
linked=0
for relative,row in rows.items():
    path=ROOT/relative
    if path.stat().st_ino!=cached.stat().st_ino:
        assert path.stat().st_nlink==1
        temporary=path.with_name(path.name+'.stage28-readonly-alias.tmp');assert not temporary.exists();os.link(cached,temporary);assert sha(temporary)==EXPECTED
        readonly=not(path.stat().st_mode&stat.S_IWRITE)
        if readonly:os.chmod(path,stat.S_IREAD|stat.S_IWRITE)
        try:os.replace(temporary,path)
        except BaseException:
            if readonly and path.exists():os.chmod(path,stat.S_IREAD)
            save(OUT/'partial-failure.json',{'path':relative,'inspectActualAliasesBeforeRetry':True});raise
        linked+=1
    os.chmod(cached,stat.S_IREAD);assert path.stat().st_ino==cached.stat().st_ino and sha(path)==EXPECTED
    with (OUT/'journal.jsonl').open('a',encoding='utf-8') as stream:stream.write(json.dumps({'path':relative,'sha256':EXPECTED,'fileId':path.stat().st_ino,'readonly':not(path.stat().st_mode&stat.S_IWRITE)})+'\n')
result={'retainedArchiveBoundRuntimePaths':len(rows),'duplicateInodesReplacedWithSameByteReadonlyAliases':linked,'logicalDuplicateBytesFreed':linked*first.stat().st_size,'freeBytesBefore':free_before,'freeBytesAfter':shutil.disk_usage(ROOT).free,'allPathsAndHashesPreserved':True,'formalInstancesCanonicalCandidateWorldsAndWeaponsUntouched':True,'remoteLargeArtifactRecoveryClaimed':False,'newInstancesMustIndependentlyCopy':True}
save(OUT/'receipt.json',result);print(json.dumps(result),flush=True)
