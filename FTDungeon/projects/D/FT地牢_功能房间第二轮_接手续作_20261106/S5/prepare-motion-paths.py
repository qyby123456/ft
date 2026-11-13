import hashlib
import json
import subprocess
from pathlib import Path

root=Path(__file__).resolve().parent
qa=root/'qa/natural-movement'
qa.mkdir(exist_ok=True)
source=Path('D:/ft工程文件/FT地牢_独立复核_20261104_01a1a685/IndependentRouteAudit.java').read_text(encoding='utf-8')
source=source.replace('public final class IndependentRouteAudit','public final class MovementPathPlanner')
source=source.replace('Set<Node> visited = new HashSet<>();','Set<Node> visited = new HashSet<>();\n            Map<Node,Node> parents = new HashMap<>();')
source=source.replace('visited.add(next);','visited.add(next);\n                        parents.put(next,current);')
needle='List<Object> results = new ArrayList<>();'
source=source.replace(needle,needle+'\n            List<Object> paths = new ArrayList<>();')
needle='Node point = node(targetObject.getAsJsonArray("position"));'
source=source.replace(needle,needle+'''
                List<Object> path = new ArrayList<>();
                if(visited.contains(point)){
                    Node cursor=point;
                    while(cursor!=null){path.add(List.of(cursor.eastHalf/2.0,cursor.upHalf/2.0,cursor.southHalf/2.0));cursor=parents.get(cursor);}
                    java.util.Collections.reverse(path);
                }
                paths.add(Map.of("label",targetObject.get("label").getAsString(),"feet",path));
''')
source=source.replace('"targets", results,','"targets", results,"paths",paths,')
source=source.replace('independent-routes.json','planned-paths.json')
patch='*** Begin Patch\n*** Add File: '+(qa/'MovementPathPlanner.java').as_posix()+'\n'+''.join('+'+line+'\n' for line in source.splitlines())+'*** End Patch'
process=subprocess.run(['C:/Users/YOUR_USERNAME/AppData/Local/OpenAI/Codex/bin/b6c7fc9497179c98/codex.exe','--codex-run-as-apply-patch',patch],capture_output=True);assert process.returncode==0,process.stderr
classpath='D:/ft工程文件/FT地牢_独立复核_20261104_01a1a685/classes;'+';'.join(Path('D:/ft工程文件/FT地牢_第7组逐图重做_20261104/10-overview/qa/isolated-run-05/classpath.txt').read_text(encoding='utf-8-sig').strip().splitlines())
process=subprocess.run(['C:/Program Files/Java/jdk-25.0.2/bin/javac.exe','-encoding','UTF-8','-cp',classpath,'-d',str(qa),str(qa/'MovementPathPlanner.java')],capture_output=True);assert process.returncode==0,process.stderr
candidate=root/'merged/merged.nbt'
specifications=[{'group':901,'candidate':str(candidate),'sha256':hashlib.sha256(candidate.read_bytes()).hexdigest(),'swimming':False,'start':[10,9,23],'targets':[{'label':'finish','position':[14,5,69]},{'label':'ladder-base','position':[22,9,14]},{'label':'water-edge','position':[28,9,21]}]}]
(qa/'route-input.json').write_text(json.dumps(specifications,ensure_ascii=False,indent=2),encoding='utf-8')
(qa/'vanilla-block-tags.json').write_bytes((root/'S5-11/qa/vanilla-block-tags.json').read_bytes())
process=subprocess.run(['C:/Program Files/Java/jdk-25.0.2/bin/java.exe','-Xmx1536m','-cp',str(qa)+';'+classpath,'MovementPathPlanner',str(qa)],cwd=qa,capture_output=True,timeout=360)
(qa/'planner.log').write_bytes(process.stdout+process.stderr);assert process.returncode==0,process.stderr
print((qa/'planned-paths.json').stat().st_size)
