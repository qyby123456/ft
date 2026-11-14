import hashlib
import json
import os
from pathlib import Path
import re
import subprocess
import urllib.error
import urllib.request
ROOT=Path(__file__).resolve().parents[3]
WORK=ROOT/'work/session-boss'
OUT=WORK/'reports/github-ft-remote-review-v1'
CHECKOUT=ROOT/'github-sync/repositories/ft'
URL='https://github.com/qyby123456/ft.git'
def save(name,value):
    with (OUT/name).open('x',encoding='utf-8') as stream:json.dump(value,stream,ensure_ascii=False,indent=2)
def scrub(text):
    text=re.sub(r'(?:sk-[A-Za-z0-9_-]{20,}|gh[pousr]_[A-Za-z0-9]{20,}|github_pat_[A-Za-z0-9_]{20,})','[REDACTED]',text)
    text=re.sub(r'https?://[^\s/]+:[^\s/@]+@','https://[REDACTED]@',text)
    return re.sub(r'(https?://[^\s?]+)\?[^\s]+',r'\1?[REDACTED_QUERY]',text)
assert not OUT.exists() and not CHECKOUT.exists();OUT.mkdir();CHECKOUT.parent.mkdir(parents=True,exist_ok=True);hooks=OUT/'empty-hooks';hooks.mkdir()
environment=dict(os.environ);environment.update({'GIT_TERMINAL_PROMPT':'0','GCM_INTERACTIVE':'Never','GIT_LFS_SKIP_SMUDGE':'1'})
def git(label,arguments,cwd=ROOT):
    command=['git','-c','core.hooksPath='+str(hooks),*arguments];result=subprocess.run(command,cwd=cwd,env=environment,capture_output=True,timeout=120)
    stdout=result.stdout.decode('utf-8',errors='replace');stderr=scrub(result.stderr.decode('utf-8',errors='replace'));save(label+'-terminal.json',{'command':command,'exitCode':result.returncode,'stdout':scrub(stdout),'stderr':stderr,'noCredentialsPrintedOrStored':True});return result.returncode,stdout,stderr
try:
    request=urllib.request.Request('https://api.github.com/repos/qyby123456/ft',headers={'User-Agent':'BossSourceMigrationAudit','Accept':'application/vnd.github+json'})
    with urllib.request.urlopen(request,timeout=30) as response:metadata=json.load(response)
    public={key:metadata.get(key) for key in ['full_name','private','default_branch','archived','disabled','size','visibility']};public['apiUnauthenticatedReadSucceeded']=True
except (urllib.error.URLError,TimeoutError) as failure:public={'apiUnauthenticatedReadSucceeded':False,'errorType':type(failure).__name__,'httpStatus':getattr(failure,'code',None)}
save('public-repository-metadata.json',public)
code,stdout,stderr=git('clone',['clone','--no-checkout',URL,str(CHECKOUT)]);assert code==0,'Cannot read supplied remote;see sanitized clone terminal receipt'
code,head,_=git('head',['-C',str(CHECKOUT),'rev-parse','HEAD']);assert code==0;head=head.strip()
code,remote_head,_=git('remote-head',['-C',str(CHECKOUT),'symbolic-ref','refs/remotes/origin/HEAD']);assert code==0
code,branches,_=git('branches',['-C',str(CHECKOUT),'for-each-ref','--format=%(refname)','refs/heads','refs/remotes']);assert code==0
code,history,_=git('history-count',['-C',str(CHECKOUT),'rev-list','--count','HEAD']);assert code==0
code,tree,_=git('tree',['-C',str(CHECKOUT),'ls-tree','-r','-l','HEAD']);assert code==0;tree_rows=[]
for line in tree.splitlines():
    left,_,name=line.partition('\t');parts=left.split();tree_rows.append({'path':name,'mode':parts[0],'type':parts[1],'object':parts[2],'bytes':int(parts[3]) if len(parts)>3 and parts[3].isdigit() else None})
save('existing-tree.json',tree_rows)
policy_files={}
for name in ['AGENTS.md','.gitignore','.gitattributes','README.md']:
    code,body,_=git('policy-'+name.replace('.','_'),['-C',str(CHECKOUT),'show','HEAD:'+name]);policy_files[name]={'exists':code==0,'utf8Bytes':len(body.encode('utf-8')) if code==0 else 0}
code,push_stdout,push_stderr=git('receive-pack-dry-run',['-C',str(CHECKOUT),'push','--dry-run','origin','HEAD:refs/heads/codex/neoforge-source-20261113'])
receipt={'repository':URL,'checkout':str(CHECKOUT),'defaultRemoteHead':remote_head.strip(),'head':head,'historyCommits':int(history.strip()),'branches':branches.splitlines(),'existingFiles':len(tree_rows),'existingTopLevelEntries':sorted({row['path'].split('/')[0] for row in tree_rows}),'existingLargeFilesOver20MiB':sum((row['bytes'] or 0)>20*1024*1024 for row in tree_rows),'policyFiles':policy_files,'publicMetadata':public,'receivePackDryRunAccepted':code==0,'actualRemoteWriteNotYetProven':True,'remoteUpdated':False,'newCommitCreated':False,'sourceProjectFormalInstancesUntouched':True,'weaponsPaused':True,'fullBossGoalStatus':'active'}
save('repository-review.json',receipt);print(json.dumps(receipt),flush=True)
