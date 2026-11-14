import argparse
import hashlib
import json
import re
import shutil
import zipfile
from pathlib import Path, PurePosixPath

parser=argparse.ArgumentParser()
parser.add_argument('archive',type=Path)
parser.add_argument('--output',type=Path)
arguments=parser.parse_args()
with zipfile.ZipFile(arguments.archive) as bundle:
    manifest=json.loads(bundle.read('ARCHIVE_MANIFEST.json'))
    assert manifest['format']=='ftboss-content-addressed-evidence-v1'
    files=manifest['files']
    blobs={}
    for name,row in files.items():
        relative=PurePosixPath(name)
        assert not relative.is_absolute() and '..' not in relative.parts and ':' not in name and '\\' not in name,name
        digest=row['sha256'];size=row['bytes']
        assert re.fullmatch('[0-9a-f]{64}',digest) and isinstance(size,int) and 0<=size<=1024**3
        assert digest not in blobs or blobs[digest]==size
        blobs[digest]=size
    assert len(bundle.namelist())==len(set(bundle.namelist()))
    assert set(bundle.namelist())=={'ARCHIVE_MANIFEST.json',*('blobs/'+digest for digest in blobs)}
    for digest,size in blobs.items():
        checksum=hashlib.sha256();total=0
        with bundle.open('blobs/'+digest) as stream:
            while chunk:=stream.read(1024*1024):checksum.update(chunk);total+=len(chunk)
        assert total==size and checksum.hexdigest()==digest,digest
    if arguments.output is not None:
        destination=arguments.output.resolve()
        assert not destination.exists(),'Restore only into a new empty directory;never overwrite a source workspace orformal instance'
        destination.mkdir(parents=True)
        for name,row in files.items():
            target=destination.joinpath(*PurePosixPath(name).parts)
            assert target.resolve().is_relative_to(destination)
            target.parent.mkdir(parents=True,exist_ok=True)
            with bundle.open('blobs/'+row['sha256']) as source,target.open('xb') as output:shutil.copyfileobj(source,output)
            with target.open('rb') as restored:assert hashlib.file_digest(restored,'sha256').hexdigest()==row['sha256']
print(json.dumps({'archive':str(arguments.archive),'files':len(files),'uniqueBlobs':len(blobs),'allReferencedBlobsReadBackAndHashVerified':True,'restored':arguments.output is not None,'formalInstancesModified':False}))
