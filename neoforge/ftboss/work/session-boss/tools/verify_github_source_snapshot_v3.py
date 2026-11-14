import argparse
import hashlib
import json
import os
from pathlib import Path
import re
import subprocess
ROOT=Path(__file__).resolve().parents[3]
WORK=ROOT/'work/session-boss'
SENSITIVE_KEYS={'apikey','accesstoken','refreshtoken','clientsecret','password','passwd','privatekey','authorization','cookie','sessionid','sessiontoken','signedsession','account','username','email'}
PATTERNS=[('private-key',re.compile(r'-----BEGIN (?:RSA |EC |DSA |OPENSSH |ENCRYPTED )?PRIVATE KEY-----')),('provider-token',re.compile(r'(?:sk-[A-Za-z0-9_-]{20,}|gh[pousr]_[A-Za-z0-9]{20,}|github_pat_[A-Za-z0-9_]{20,}|AKIA[0-9A-Z]{16})')),('inline-url-credential',re.compile(r'https?://[^\s/]+:[^\s/@]+@')),('jwt-shaped-value',re.compile(r'eyJ[A-Za-z0-9_-]{15,}\.[A-Za-z0-9_-]{15,}\.[A-Za-z0-9_-]{15,}')),('quoted-sensitive-assignment',re.compile(r"""(?ix)(?<![A-Za-z0-9_])["']?(?:api[_-]?key|access[_-]?token|refresh[_-]?token|client[_-]?secret|password|passwd|private[_-]?key|authorization|session[_-]?token)["']?\s*[:=]\s*["']([^"'\r\n]{8,})["']"""))]
FORBIDDEN_PARTS={'build','cache','.gradle','.idea','.vscode','.venv','venv','__pycache__','node_modules','classes','logs','tmp','temp','server','game','saves','world','runtimes','runtime','isolated-home','isolated-tmp','natives','libraries','archives','releases','reports','reference'}
FORBIDDEN_EXTENSIONS={'.jar','.zip','.7z','.rar','.class','.dll','.exe','.mca','.mcr','.nbt','.dat','.dat_old','.hprof','.jfr','.pem','.key','.p12','.pfx','.jks','.keystore'}
def sha(data):return hashlib.sha256(data).hexdigest()
def save(path,value):
    with path.open('x',encoding='utf-8') as stream:json.dump(value,stream,ensure_ascii=False,indent=2)
def scan_text(text,json_document=False):
    found=[]
    for number,line in enumerate(text.splitlines(),1):
        for category,pattern in PATTERNS:
            if pattern.search(line):found.append({'line':number,'category':category})
    def walk(value,location):
        if isinstance(value,dict):
            for key,child in value.items():
                normalized=re.sub('[^a-z0-9]','',str(key).lower())
                if normalized in SENSITIVE_KEYS and isinstance(child,str) and len(child.strip())>=8:found.append({'jsonPath':location+'/'+str(key),'category':'sensitive-json-string'})
                walk(child,location+'/'+str(key))
        elif isinstance(value,list):
            for index,child in enumerate(value):walk(child,location+'/'+str(index))
    if json_document:
        try:walk(json.loads(text),'')
        except json.JSONDecodeError:found.append({'category':'invalid-json-requires-review'})
    return found
controls=[('provider', 'sk-'+'A'*28,False,True),('github', 'gh'+'p_'+'A'*30,False,True),('private-header','-----BEGIN '+'PRIVATE KEY-----',False,True),('json-quoted-key',json.dumps({'api_key':'A'*20}),True,True),('json-nested',json.dumps({'nested':[{'refresh_token':'B'*20}]}),True,True),('json-account',json.dumps({'account':'C'*12}),True,True),('url','https:'+'//user:'+('D'*12)+'@example.invalid/',False,True),('jwt','eyJ'+'E'*20+'.'+'F'*20+'.'+'G'*20,False,True),('assignment',''.join(('pass','word',' = ',chr(34),'H'*12,chr(34))),False,True),('environment-reference','api_key = os.environ.get('+chr(34)+'DEDICATED_KEY'+chr(34)+')',False,False),('zero-offline-token',json.dumps({'access_token':'0'}),True,False),('public-hash',json.dumps({'sha256':'a'*64}),True,False),('safe-code','public final class Boss {}',False,False)]
control_rows=[]
for label,text,is_json,expected in controls:
    actual=bool(scan_text(text,is_json));assert actual==expected,label;control_rows.append({'name':label,'detected':actual,'expected':expected})
parser=argparse.ArgumentParser();parser.add_argument('--git-dir',type=Path);parser.add_argument('--output',type=Path,required=True);parser.add_argument('--staged',action='store_true');parser.add_argument('--work-tree',type=Path);parser.add_argument('--source-prefix',default='');parser.add_argument('--reviewed-exceptions',type=Path);args=parser.parse_args();work_tree=args.work_tree.resolve() if args.work_tree else ROOT;output=args.output.resolve();assert output.is_relative_to((WORK/'reports').resolve()) and not output.exists();output.mkdir(parents=True)
git_dir=args.git_dir.resolve() if args.git_dir else None
if git_dir is None:
    result=subprocess.run(['git','rev-parse','--absolute-git-dir'],cwd=ROOT,capture_output=True);assert result.returncode==0,'Intended Git source repository is not initialized';git_dir=Path(result.stdout.decode('utf-8').strip())
assert (git_dir/'HEAD').is_file();environment=dict(os.environ);empty=output/'empty.gitconfig';empty.write_text('',encoding='utf-8');environment['GIT_CONFIG_NOSYSTEM']='1';environment['GIT_CONFIG_GLOBAL']=str(empty)
base=['git','--git-dir='+str(git_dir),'--work-tree='+str(work_tree),'-c','core.excludesfile='+str(empty)]
command=base+(['diff','--cached','--name-only','--diff-filter=ACMR','-z'] if args.staged else ['ls-files','--others','--exclude-standard','-z']);result=subprocess.run(command,cwd=ROOT,env=environment,capture_output=True);assert result.returncode==0;names=sorted(name.decode('utf-8') for name in result.stdout.split(b'\0') if name)
review_map={};reviewed_matches=[]
if args.reviewed_exceptions:
    review_path=args.reviewed_exceptions.resolve();assert review_path.is_relative_to(ROOT)
    for entry in json.loads(review_path.read_text(encoding='utf-8'))['exceptions']:
        key=(entry['path'],entry['sourceSha256'],entry['line'],entry['category']);assert key not in review_map;review_map[key]=entry['reason']
    assert all((path,'f'*64,line,category) not in review_map for path,digest,line,category in review_map),'Changed hash must fail closed'
    assert all((path,digest,line+1,category) not in review_map for path,digest,line,category in review_map),'Changed line must fail closed'
batch=None;index_objects={};snapshot_tree=None
if args.staged:
    result=subprocess.run(base+['write-tree'],cwd=ROOT,env=environment,capture_output=True);assert result.returncode==0;snapshot_tree=result.stdout.decode().strip()
    result=subprocess.run(base+['ls-files','--stage','-z'],cwd=ROOT,env=environment,capture_output=True);assert result.returncode==0
    for entry in result.stdout.split(b'\0'):
        if not entry:continue
        header,name=entry.split(b'\t',1);mode,object_id,stage=header.split();assert stage==b'0';index_objects[name.decode('utf-8')]=object_id.decode()
    assert set(names).issubset(index_objects)
    batch=subprocess.Popen(base+['cat-file','--batch'],cwd=ROOT,env=environment,stdin=subprocess.PIPE,stdout=subprocess.PIPE,stderr=subprocess.PIPE)
files=[];issues=[];text_files=0;json_files=0;forbidden=[]
for name in names:
    path=(work_tree/name).resolve();assert path.is_relative_to(work_tree),'Out-of-workspace path';relative=path.relative_to(work_tree).as_posix();source_relative=relative
    if args.source_prefix:
        prefix=args.source_prefix.strip('/')+'/'
        assert relative.startswith(prefix),'Changed file outside authorized import prefix';source_relative=relative[len(prefix):]
    parts=set(part.lower() for part in Path(relative).parts)
    reasons=[]
    if path.is_symlink():reasons.append({'category':'symlink-requires-review'})
    if parts.intersection(FORBIDDEN_PARTS) or path.suffix.lower() in FORBIDDEN_EXTENSIONS or path.name.lower().startswith('.env') or re.search(r'(?i)credentials?|(?:^|[-_.])secrets?(?:[-_.]|$)',path.name):reasons.append({'category':'forbidden-ordinary-git-path'});forbidden.append(relative)
    if args.staged:
        batch.stdin.write((index_objects[relative]+'\n').encode());batch.stdin.flush();header=batch.stdout.readline().decode().strip().split();assert len(header)==3 and header[0]==index_objects[relative] and header[1]=='blob';size=int(header[2]);data=batch.stdout.read(size);assert len(data)==size and batch.stdout.read(1)==b'\n'
    else:
        assert path.is_file();data=path.read_bytes()
    if len(data)>20*1024*1024:reasons.append({'category':'exceeds-local20MiB-source-review-policy'})
    if b'\0' in data[:8192]:reasons.append({'category':'binary-requires-reviewed-external-artifact-storage'})
    else:
        try:
            text=data.decode('utf-8-sig');text_files+=1;is_json=path.suffix.lower()=='.json';json_files+=is_json;reasons.extend(scan_text(text,is_json))
        except UnicodeDecodeError:reasons.append({'category':'non-utf8-text-requires-review'})
    unreviewed=[]
    for reason in reasons:
        review_key=(source_relative,sha(data),reason.get('line'),reason['category'])
        if review_key in review_map:reviewed_matches.append({'path':relative,'sourceSha256':sha(data),'line':reason['line'],'category':reason['category'],'reviewReason':review_map[review_key]})
        else:unreviewed.append(reason)
    reasons=unreviewed
    row={'path':relative,'bytes':len(data),'sha256':sha(data),'issues':len(reasons),'contentSource':'actual-index-blob' if args.staged else 'actual-Git-eligible-worktree-file'};files.append(row)
    if reasons:issues.append({'path':relative,'sha256':row['sha256'],'findings':reasons})
    if not args.staged:assert sha(path.read_bytes())==row['sha256'],'Input changed during scan'
if batch is not None:
    batch.stdin.close();batch.wait(timeout=30);assert batch.returncode==0
    result=subprocess.run(base+['write-tree'],cwd=ROOT,env=environment,capture_output=True);assert result.returncode==0 and result.stdout.decode().strip()==snapshot_tree,'Git index changed during the full scan'
summary={'mode':'staged-index' if args.staged else 'actual-Git-candidate-worktree','candidateFiles':len(files),'textFilesScanned':text_files,'jsonFilesParsed':json_files,'filesWithIssues':len(issues),'forbiddenOrdinaryGitPaths':len(forbidden),'positiveAndNegativeDetectorControls':len(control_rows),'matchedSecretValuesIncludedInReports':False,'exactByteAndLineReviewedMetadataMatches':len(reviewed_matches),'reviewHashAndLineNegativeControls':2 if review_map else 0,'scanCoversEveryEnumeratedGitCandidate':len(files)==len(names),'patternScanOnlyNotCompletePublishClearance':True,'batchObjectReads':len(files) if args.staged else 0,'indexTreeStableBeforeAfter':True if args.staged else None,'indexTree':snapshot_tree,'commitsCreated':0,'pushesPerformed':0,'remoteRecoveryVerified':False}
save(output/'reviewed-metadata-matches.json',reviewed_matches);save(output/'detector-controls.json',control_rows);save(output/'source-manifest.json',{'files':files,'scope':summary['mode']});save(output/'findings.json',{'files':issues,'matchedSecretValuesIncluded':False});save(output/'verification.json',{**summary,'inputHashes':{str(Path(__file__).relative_to(ROOT)).replace(chr(92),'/'):sha(Path(__file__).read_bytes()),'.gitignore':sha((ROOT/'.gitignore').read_bytes()),'.gitattributes':sha((ROOT/'.gitattributes').read_bytes())}})
print(json.dumps(summary),flush=True)
assert not issues,'Unreviewed findings block staging/publication;only finding locations/categories were recorded'
