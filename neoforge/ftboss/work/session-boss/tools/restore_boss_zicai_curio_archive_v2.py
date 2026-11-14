import argparse
import hashlib
import json
import re
import shutil
import zipfile
from pathlib import Path,PurePosixPath

parser=argparse.ArgumentParser()
parser.add_argument('archive',type=Path)
parser.add_argument('--base',type=Path,required=True)
parser.add_argument('--output',type=Path)
arguments=parser.parse_args()
with arguments.base.open('rb') as stream:base_sha=hashlib.file_digest(stream,'sha256').hexdigest()
with zipfile.ZipFile(arguments.archive) as delta,zipfile.ZipFile(arguments.base) as base:
    manifest=json.loads(delta.read('ARCHIVE_MANIFEST.json'))
    assert manifest['format']=='ftboss-content-addressed-increment-v1' and manifest['baseArchiveSha256']==base_sha
    files=manifest['files'];unique={}
    for name,row in files.items():
        relative=PurePosixPath(name)
        assert not relative.is_absolute() and '..' not in relative.parts and ':' not in name and chr(92) not in name
        digest=row['sha256'];size=row['bytes'];layer=row['layer']
        assert re.fullmatch('[0-9a-f]{64}',digest) and isinstance(size,int) and 0<=size<=1024**3 and layer in ['self','base']
        key=(layer,digest);assert key not in unique or unique[key]==size;unique[key]=size
    assert len(delta.namelist())==len(set(delta.namelist()))
    assert set(delta.namelist())=={'ARCHIVE_MANIFEST.json',*('blobs/'+digest for layer,digest in unique if layer=='self')}
    for (layer,digest),size in unique.items():
        bundle=delta if layer=='self' else base;checksum=hashlib.sha256();total=0
        with bundle.open('blobs/'+digest) as stream:
            while chunk:=stream.read(1024*1024):checksum.update(chunk);total+=len(chunk)
        assert total==size and checksum.hexdigest()==digest
    if arguments.output is not None:
        destination=arguments.output.resolve();assert not destination.exists(),'Restore only into a new directory;never overwrite active source orformal instance'
        destination.mkdir(parents=True)
        for name,row in files.items():
            target=destination.joinpath(*PurePosixPath(name).parts);assert target.resolve().is_relative_to(destination)
            target.parent.mkdir(parents=True,exist_ok=True);bundle=delta if row['layer']=='self' else base
            with bundle.open('blobs/'+row['sha256']) as source,target.open('xb') as output:shutil.copyfileobj(source,output)
            with target.open('rb') as stream:assert hashlib.file_digest(stream,'sha256').hexdigest()==row['sha256']
print(json.dumps({'files':len(files),'uniqueReferencedBlobs':len(unique),'allPathSizeAndPayloadHashesVerified':True,'baseArchiveSha256':base_sha,'restored':arguments.output is not None,'standaloneWithoutBase':False}))
