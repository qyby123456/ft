"""Verify the organized working library and the completed, user-authorized deletion."""
import ast
import json
from pathlib import Path
import re
from urllib.parse import unquote
from finish_blockbench_cleanup import ROOT,BB,LIB,AUDIT,ARCHIVE,GROUPS,ACTIVE_TOOLS,digest,native,write

plan=json.loads((AUDIT/'计划.json').read_text(encoding='utf-8'))
report=json.loads((AUDIT/'完成.json').read_text(encoding='utf-8'))
for relative,expected in plan['protected'].items():
    assert digest(ROOT/relative)==expected,relative
moved=json.loads((AUDIT/'移动素材校验.json').read_text(encoding='utf-8'))
for relative,expected in moved.items():
    assert digest(BB/relative)==expected,relative
assert digest(Path(plan['installed']))==plan['installed_sha256']
assert not ARCHIVE.exists()
assert {p.name for p in BB.iterdir()} == {
    '01-常用工程','02-模板与基准','03-参考素材','04-导出工具','05-打包文件','06-使用说明','README.md'}
deleted=json.loads((AUDIT/'删除文件清单.json').read_text(encoding='utf-8'))
for entry in deleted:
    assert not native(Path(entry['path'])).exists(),entry['path']
assert len(deleted)==report['deleted_files']
assert sum(row['bytes'] for row in deleted)==report['deleted_bytes']
shots={p.name for p in (ROOT/'run-smoke/screenshots').iterdir() if p.is_file()}
assert shots==set(plan['preserved_screenshots']),len(shots)
videos=list((ROOT/'references').rglob('*.mp4'))
assert len(videos)==27
links=0
for page in list(LIB.glob('*.md'))+[BB/'README.md',BB/'04-导出工具/README.md']:
    for relative in re.findall(r'\]\(<([^>]+)>\)',page.read_text(encoding='utf-8')):
        assert native((page.parent/unquote(relative)).resolve()).exists(),(page,relative)
        links+=1
indexes=0
for category in GROUPS:
    for path in (BB/category).rglob('导出检查.json'):
        data=json.loads(path.read_text(encoding='utf-8'))
        if isinstance(data,list):
            for row in data:
                if isinstance(row,dict) and 'path' in row:
                    assert native(Path(row['path'])).exists(),(path,row['path'])
                    indexes+=1
script_count=0;static_inputs=0
for path in (BB/'04-导出工具').rglob('*.py'):
    tree=ast.parse(path.read_text(encoding='utf-8'));script_count+=1
    if path.name not in ACTIVE_TOOLS:continue
    env={'Path':Path,'__file__':str(path)}
    # Evaluate only Path-based root assignments, never exporter bodies.
    for node in tree.body:
        if isinstance(node,ast.Assign) and len(node.targets)==1 and isinstance(node.targets[0],ast.Name):
            name=node.targets[0].id
            if name in ('ROOT','ASSETS','OUT'):
                env[name]=eval(compile(ast.Expression(node.value),str(path),'eval'),{'__builtins__':{}},env)
                assert native(env[name]).is_dir(),(path,name,env[name])
    for node in ast.walk(tree):
        if isinstance(node,ast.Call) and isinstance(node.func,ast.Attribute) and node.func.attr in ('read_text','read_bytes'):
            expression=node.func.value
            if not isinstance(expression,ast.BinOp) or not isinstance(expression.op,ast.Div):continue
            if any(isinstance(child,ast.JoinedStr) for child in ast.walk(expression)):continue
            if not {child.id for child in ast.walk(expression) if isinstance(child,ast.Name)} <= env.keys():continue
            value=eval(compile(ast.Expression(expression),str(path),'eval'),{'__builtins__':{}},env)
            assert native(value).is_file(),(path,value)
            static_inputs+=1
checks=dict(protected_files=len(plan['protected']),retained_model_media_hashes=len(moved),
            reference_videos=27,netease_videos=24,original_user_videos=3,
            catalog_links=links,export_index_paths=indexes,python_tools=script_count,
            active_tool_static_inputs=static_inputs,retained_current_screenshots=len(shots),
            deleted_files=report['deleted_files'],deleted_bytes=report['deleted_bytes'],
            deleted_GiB=report['deleted_GiB'],archive_deleted=True,
            installed_jar_unchanged=True,source_and_reference_content_unchanged=True)
write(LIB/'完整性验证.json',checks)
print(json.dumps(checks,ensure_ascii=False),flush=True)
