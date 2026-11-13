import hashlib
import json
import os
from pathlib import Path
import subprocess
import sys

sys.dont_write_bytecode = True
ROOT = Path(__file__).resolve().parent
OUTPUT = ROOT / 'runtime-assembly-r1'


def write(path, value):
    path.write_text(json.dumps(value, ensure_ascii=False, indent=2), encoding='utf-8')


def main():
    source = Path('D:/ft工程文件/FT地牢_独立复核_20261104_01a1a685/IndependentRouteAudit.java').read_text(encoding='utf-8')
    source = source.replace('public final class IndependentRouteAudit', 'public final class ProducerPathExport')
    source = source.replace('Set<Node> visited = new HashSet<>();', 'Set<Node> visited = new HashSet<>(); Map<Node,Node> previous = new HashMap<>();')
    source = source.replace('queue.add(next);', 'queue.add(next); previous.put(next,current);')
    original = 'results.add(Map.of("label", targetObject.get("label").getAsString(), "position", targetObject.get("position"), "standing_clear", stand(point), "reachable", visited.contains(point)));'
    replacement = 'List<Object> path = new ArrayList<>(); for(Node cursor=point;cursor!=null;cursor=previous.get(cursor))path.add(List.of(cursor.eastHalf/2.0,cursor.upHalf/2.0,cursor.southHalf/2.0)); java.util.Collections.reverse(path); results.add(Map.of("label",targetObject.get("label").getAsString(),"position",targetObject.get("position"),"standing_clear",stand(point),"reachable",visited.contains(point),"path",path));'
    assert original in source
    source = source.replace(original, replacement)
    planner = OUTPUT / 'path-planning'
    planner.mkdir(parents=True, exist_ok=True)
    (planner / 'ProducerPathExport.java').write_text(source, encoding='utf-8')
    design = json.loads((ROOT / 'assembly/design.json').read_text(encoding='utf-8'))
    candidate = ROOT / 'assembly' / design['nbt']
    digest = hashlib.sha256(candidate.read_bytes()).hexdigest()
    write(planner / 'route-input.json', [{'group': 2, 'candidate': str(candidate), 'sha256': digest, 'start': design['ports'][0]['feet'], 'swimming': False, 'targets': design['targets']}])
    classpath = (ROOT / '11-library/qa/classpath.txt').read_text(encoding='utf-8')
    javac = 'C:/Program Files/Common Files/Oracle/Java/javapath/javac.exe'
    java = 'C:/Program Files/Common Files/Oracle/Java/javapath/java.exe'
    result = subprocess.run([javac, '-encoding', 'UTF-8', '-cp', classpath, '-d', str(planner), str(planner / 'ProducerPathExport.java')], cwd=planner, capture_output=True)
    (planner / 'compile.log').write_bytes(result.stdout + result.stderr)
    assert result.returncode == 0
    result = subprocess.run([java, '-Xmx1536M', '-cp', str(planner) + os.pathsep + classpath, 'ProducerPathExport', str(planner)], cwd=planner, capture_output=True)
    (planner / 'planning.log').write_bytes(result.stdout + result.stderr)
    assert result.returncode == 0
    report = json.loads((planner / 'independent-routes.json').read_text(encoding='utf-8'))[0]
    tree = {}
    start = None
    for target in report['targets']:
        assert target['reachable'] and target['standing_clear']
        path = [tuple(point) for point in target['path']]
        start = path[0]
        for first, last in zip(path,path[1:]):
            if last not in tree.setdefault(first, []):
                tree[first].append(last)
    traversal = [start]
    def visit(point):
        for child in tree.get(point, []):
            traversal.append(child)
            visit(child)
            traversal.append(point)
    sys.setrecursionlimit(10000)
    visit(start)
    write(OUTPUT / 'assembly-input.json', {'candidate': str(candidate), 'sha256': digest, 'design': design, 'movement_route': traversal, 'movement_target_count': len(report['targets']), 'movement_scope': 'DFS of union of native half-grid collision paths from entrance to all 69 current declaration targets, returns to entrance. Planning is offline; runtime Player.travel verifies actual motion separately.'})
    print('ASSEMBLY_PATH_READY', digest, len(traversal), 'half-grid movement waypoints')


if __name__ == '__main__':
    main()
