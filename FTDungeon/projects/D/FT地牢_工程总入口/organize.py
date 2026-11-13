import argparse
import csv
import hashlib
import io
import json
import re
from collections import Counter
from datetime import datetime
from pathlib import Path

ROOT = Path('D:/ft工程文件')
PORTAL = Path(__file__).resolve().parent
CATEGORIES = {
    '01_当前整合与生成器': '当前整合、独立候选与现场生成器组件。当前整合仍在进行，候选通过不等于整合通过或正式部署。',
    '02_功能房间制作': '第二轮接手续作及S1–S8直接入口。按原线程分工读写，原progress和ready保持权威。',
    '03_独立复核与验收': '独立审查入口及冻结证据。当前进度以原README、最终验收索引和精确冻结证据为准。',
    '04_源码模块': '地牢怪物、挑战、交易、展示台和装饰等源码模块。相同类名和模组ID不作为合并依据。',
    '05_房间素材与历史批次': '原房间、风格、参考、制作与返工批次。历史日期只表示来源批次，不表示无用或可删除。',
    '06_修复分支与工具': '出生、生成、门、实体及其它专用修复分支和辅助工具。保留独立构建和继承关系。',
    '07_交付包与效果': '交付压缩包、效果档案和外部参考模组。原验收状态和素材来源随原件保留。',
    '08_审计与恢复': '地牢缓存归档、逐批实际删除清单、完整性校验及恢复工具；各批回收量单独计量。',
}
MAIN = {
    'FT地牢_大小型风格合并_20261109': '较新的大小型和八风格整合、验收产物及全房独立重建入口',
    'FT地牢_通用风格整合_20261108': '11月8日通用风格整合来源；较新合并见大小型风格工程',
    'FT地牢_三生成器合并_20261106': '既有三生成器整合来源；保留后续修复基线',
    'FT地牢_通用风格生成器_20261107': '独立0.26.0风格候选及验收来源，非较新版本的正式安装入口',
    'FT地牢_苔藓古林生成器_20261107': '苔藓古林独立候选及接口来源；不能整目录覆盖较新整合版',
    '小型群系类地牢_现场生成器_20261106': '现场生成器core/runtime/catalog/integration分工工程',
}
REVIEW = {'FT地牢_第二轮独立验收_20261106', 'FT地牢_独立复核_20261104_01a1a685', 'FT地牢_八组合并总复核_20261106'}
STATUS_NAMES = {'progress.json', 'ready.json', 'ready-for-review.json', 'references.json', 'baseline.json', 'latest-submission.json', 'final-style-delivery.json', 'deployment.json', 'migration-A.json', 'migration-B.json', 'migration-C.json', 'migration-contract.json', 'S8-ACTUAL-VISUAL-REVIEW-20261108.json'}
TEXT_NAMES = {'README.md', 'WORK_ORDER.md', 'build.gradle', 'settings.gradle', 'gradle.properties', 'BUILD.ps1', '通用风格生成器.md', '通用风格测试说明.md', '接口合并说明.md'}

def now():
    return datetime.now().astimezone().isoformat()

def load(path):
    return json.loads(path.read_text(encoding='utf-8-sig'))

def write(path, text):
    assert path.resolve().is_relative_to(PORTAL)
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(text, encoding='utf-8')

def dump(path, value):
    write(path, json.dumps(value, ensure_ascii=False, indent=2) + chr(10))

def sha(path):
    with path.open('rb') as stream:
        return hashlib.file_digest(stream, 'sha256').hexdigest()

def link(path, label=None):
    return '[' + (label or path.name) + '](<' + path.as_posix() + '>)'

def classify(path):
    name = path.name
    if name.startswith('FT地牢_压缩去重_'):
        return '08_审计与恢复', '压缩去重归档、实际删除日志、保护校验及恢复工具'
    if name in MAIN:
        return '01_当前整合与生成器', MAIN[name]
    if name == 'FT地牢_功能房间第二轮_接手续作_20261106':
        return '02_功能房间制作', '当前S1–S8制作工作区；原位保留'
    if name in REVIEW:
        return '03_独立复核与验收', '独立复核与冻结证据；不迁移、不清理'
    if name.startswith('FT-Dungeon-'):
        role = '派生/专用源码分支；保留独立构建' if name.endswith(('-BossGate', '-DimensionDifficulty', '-Remodel')) else '独立源码模块；保留原项目名与构建入口'
        return '04_源码模块', role
    if path.is_file() and path.suffix.lower() in {'.zip', '.7z', '.jar'} or name == '地牢生成效果档案':
        role = '外部参考模组；不是FT地牢工程产物' if path.suffix.lower() == '.jar' else '交付包或效果记录；可部署性以原验收为准'
        return '07_交付包与效果', role
    if name.startswith('dungeon-block') or re.search('混沌|生成器接入|三实体导入|地牢门_|小型群系修复|中等地牢多风格修复|返程门终点', name):
        return '06_修复分支与工具', '特定修复/接入分支；保留原入口与历史证据'
    return '05_房间素材与历史批次', '房间、参考或历史批次；可能仍被配方和验收引用，原位保留'

def collect_evidence(paths):
    entries = []
    for path in sorted(set(paths)):
        if not path.is_file() or path.is_symlink() or path.stat().st_size > 2_000_000:
            continue
        data = path.read_bytes()
        statuses = {}
        if path.suffix.lower() == '.json':
            try:
                value = json.loads(data.decode('utf-8-sig'))
                if isinstance(value, dict):
                    statuses = {key: value[key] for key in ('status', 'state', 'accepted', 'deployed', 'sourceProject', 'candidate', 'directory') if key in value and isinstance(value[key], (str, bool, int, float, type(None)))}
            except (ValueError, UnicodeError):
                pass
        entries.append(dict(Path=str(path), Bytes=len(data), SHA256=hashlib.sha256(data).hexdigest(), Modified=datetime.fromtimestamp(path.stat().st_mtime).astimezone().isoformat(), RecordedFields=statuses))
    return entries

def finish():
    manifest = load(PORTAL / 'projects.json')
    shortcuts = load(PORTAL / 'shortcut-check.json')
    problems = []
    changes = []
    for item in manifest['Links']:
        if not Path(item['Target']).exists():
            problems.append(dict(Path=item['Target'], Problem='目标不存在'))
        if not Path(item['Shortcut']).is_file():
            problems.append(dict(Path=item['Shortcut'], Problem='快捷方式不存在'))
    for item in manifest['Evidence']:
        path = Path(item['Path'])
        if not path.is_file() or sha(path) != item['SHA256']:
            changes.append(item['Path'])
    for folder in load(PORTAL / 'FT地牢.code-workspace')['folders']:
        if not Path(folder['path']).is_dir():
            problems.append(dict(Path=folder['path'], Problem='工作区目录不存在'))
    assert shortcuts['Checked'] == len(manifest['Links']) and not shortcuts['Problems']
    assert not problems, problems
    receipt = dict(Status='分类入口、快捷方式和多工程工作区核验通过', Verified=now(), OriginalRootItems=len(manifest['Projects']), CategoryCounts=manifest['CategoryCounts'], ShortcutCount=len(manifest['Links']), ShortcutProblems=shortcuts['Problems'], WorkspaceFolders=len(load(PORTAL / 'FT地牢.code-workspace')['folders']), EvidenceFiles=len(manifest['Evidence']), SourceChangesDuringOrganization=changes, SourceFilesMoved=0, SourceFilesDeleted=0, SourceFilesWritten=0, NewFilesOnlyUnder=str(PORTAL), FullDiskReindex=False, OriginalPathsPreserved=True, RuntimeAcceptanceChanged=False, Note='状态为有时间戳的读取快照，活动线程继续更新时请刷新；目录整理不代表新版本已验收或部署。')
    dump(PORTAL / '整理核验.json', receipt)
    print(json.dumps(receipt, ensure_ascii=False))

def index():
    assert PORTAL.parent == ROOT and PORTAL.name == 'FT地牢_工程总入口'
    paths = [path for path in ROOT.iterdir() if path != PORTAL and re.search('地牢|Dungeon|^dungeon-block', path.name) and 'NeoForge' not in path.name and 'Event' not in path.name]
    projects = []
    links = []
    evidence_paths = []
    for path in sorted(paths):
        category, role = classify(path)
        entries = []
        if path.is_dir():
            for location in [path, path / 'verification']:
                if location.is_dir():
                    for child in location.iterdir():
                        if child.is_file() and child.name in STATUS_NAMES | TEXT_NAMES:
                            entries.append(child)
            for child in ('core', 'runtime', 'catalog', 'integration', 'review'):
                location = path / child
                if location.is_dir():
                    entries.extend(location / name for name in ('README.md', 'progress.json', 'ready.json', 'ready-for-review.json', 'build.gradle') if (location / name).is_file())
        elif path.suffix.lower() in {'.md', '.json'}:
            entries.append(path)
        evidence_paths.extend(entries)
        projects.append(dict(Name=path.name, Path=str(path), Kind='目录' if path.is_dir() else '文件', Category=category, Role=role, Entrypoints=[str(item) for item in sorted(set(entries))]))
        links.append(dict(Target=str(path), Shortcut=str(PORTAL / category / (path.name + '.lnk')), Description=role))
    rooms = ROOT / 'FT地牢_功能房间第二轮_接手续作_20261106'
    for number in range(1, 9):
        room = rooms / ('S' + str(number))
        if room.is_dir():
            links.append(dict(Target=str(room), Shortcut=str(PORTAL / '02_功能房间制作' / (room.name + '_当前制作.lnk')), Description='直接进入本组原工作目录'))
            evidence_paths.extend(room / name for name in ('README.md', 'progress.json', 'ready-for-review.json', 'runtime-ready-for-review.json') if (room / name).is_file())
    audit = ROOT / '工程保守回收_20261108_低空间'
    supplemental = [('08_审计与恢复', audit / '地牢历史压缩', '地牢历史缓存归档与校验'), ('08_审计与恢复', audit / '恢复缓存.py', '缓存恢复工具'), ('08_审计与恢复', audit / '回收结果.md', '此前缓存回收报告'), ('03_独立复核与验收', ROOT / 'FT地牢_第二轮独立验收_20261106' / 'README.md', '当前独立复核总进度'), ('03_独立复核与验收', ROOT / 'FT地牢_第二轮独立验收_20261106' / 'S8-ACTUAL-VISUAL-REVIEW-20261108.md', 'S8最近实际视觉结论')]
    latest_audit = ROOT / 'FT地牢_压缩去重_20261110'
    supplemental.extend(('08_审计与恢复', latest_audit / filename, title) for filename, title in [('压缩清理结果.md', '20261110压缩清理结果'), ('恢复缓存.py', '20261110缓存恢复工具')])
    for category, target, title in supplemental:
        if target.exists():
            links.append(dict(Target=str(target), Shortcut=str(PORTAL / category / (title + '.lnk')), Description=title))
    evidence = collect_evidence(evidence_paths)
    counts = dict(Counter(item['Category'] for item in projects))
    generated = now()
    manifest = dict(Schema=1, Generated=generated, Scope=str(ROOT), Boundary='只新增总入口；分类范围为根目录地牢/Dungeon相关条目及明确的缓存审计链接，不递归全盘盘点，不修改或移动源工程。', Projects=projects, CategoryCounts=counts, Links=links, Evidence=evidence)
    dump(PORTAL / 'projects.json', manifest)
    dump(PORTAL / '原入口文件_SHA256.json', dict(Generated=generated, Files=evidence))
    output = io.StringIO(newline='')
    writer = csv.writer(output)
    writer.writerow(['分类', '工程或文件', '类型', '原始路径', '用途与状态', '入口文件数'])
    for item in projects:
        writer.writerow([item['Category'], item['Name'], item['Kind'], item['Path'], item['Role'], len(item['Entrypoints'])])
    write(PORTAL / '地牢工程清单.csv', chr(0xFEFF) + output.getvalue())
    workspace = [('大小型八风格整合', ROOT / 'FT地牢_大小型风格合并_20261109'), ('通用风格整合来源', ROOT / 'FT地牢_通用风格整合_20261108'), ('功能房间_S1至S8', rooms), ('独立复核_当前', ROOT / 'FT地牢_第二轮独立验收_20261106')]
    generator = ROOT / '小型群系类地牢_现场生成器_20261106'
    workspace.extend(('现场生成器_' + name, generator / name) for name in ('core', 'runtime', 'catalog', 'integration'))
    workspace.extend((name, ROOT / name) for name in ('FT-Dungeon-Mobs-Forge-1.20.1', 'FT-Dungeon-Challenges-Forge-1.20.1', 'FT-Dungeon-Loot-Stand-Forge-1.20.1', 'FT-Dungeon-Trading-Forge-1.20.1', 'FT-Dungeon-Prison-Decor-Forge-1.20.1'))
    excluded = {pattern: True for pattern in ('**/.gradle/**', '**/build/**', '**/run/**', '**/logs/**', '**/node_modules/**', '**/__pycache__/**')}
    dump(PORTAL / 'FT地牢.code-workspace', dict(folders=[dict(name=name, path=path.as_posix()) for name, path in workspace if path.is_dir()], settings={'search.exclude': excluded, 'files.watcherExclude': excluded}))
    for category, description in CATEGORIES.items():
        contents = ['# ' + category, '', description, '', '本目录的 .lnk 是 Windows 快捷方式，目标为原工程，不包含第二份源码。下面保留可直接打开的绝对路径。', '']
        for item in links:
            if Path(item['Shortcut']).parent.name == category:
                contents.append('- ' + link(Path(item['Target'])) + '：' + item['Description'])
        write(PORTAL / category / '分类说明.md', chr(10).join(contents) + chr(10))
    current = [
        '# 当前工作与版本', '', '索引时间：' + generated, '', '## 当前整合', '',
        '- ' + link(ROOT / 'FT地牢_大小型风格合并_20261109') + '：较新的大小型与八风格整合及全房独立重建入口；按对应版本验收记录确认范围。',
        '- ' + link(ROOT / 'FT地牢_通用风格整合_20261108') + '：较早通用风格整合及迁移来源。',
        '- ' + link(ROOT / 'FT地牢_通用风格生成器_20261107' / 'verification/final-style-delivery.json') + '：独立候选验收来源，不能替代较新版本验收。',
        '- ' + link(ROOT / 'FT地牢_三生成器合并_20261106') + '：较早三生成器和修复来源，保留原构建及部署记录。',
        '', '## 当前独立复核', '',
        '- ' + link(ROOT / 'FT地牢_第二轮独立验收_20261106' / 'README.md') + '：当前权威进度入口，旧阶段阻塞不代表当前状态。',
        '- ' + link(ROOT / 'FT地牢_第二轮独立验收_20261106' / 'FINAL-80-ROOM-ACCEPTANCE-20261109.md') + '：11月9日八组最终验收说明。',
        '- ' + link(ROOT / 'FT地牢_第二轮独立验收_20261106' / 'FINAL-80-ROOM-ACCEPTANCE-20261109.json') + '：80房精确源及逐项证据索引。',
        '', '## 最近缓存清理', '',
        '- ' + link(latest_audit / '压缩清理结果.md') + '：11月10日实际清理及回滚说明；不改变房间验收或部署状态。',
        '', '## 原文件中的状态字段快照', '',
    ]
    for item in evidence:
        if item['RecordedFields']:
            current.append('- ' + link(Path(item['Path'])) + '：`' + json.dumps(item['RecordedFields'], ensure_ascii=False) + '`；读取文件时间 ' + item['Modified'])
    current.extend(['', '状态快照用于定位，不合并不同作用域的验收结论。修改时间较新不代表正确、正式或已经部署；请打开对应原文件阅读完整范围。'])
    write(PORTAL / '当前工作与版本.md', chr(10).join(current) + chr(10))
    relationships = ['# 引用与依赖说明', '', '- 当前整合以三生成器整合来源和独立通用风格候选为输入，兼容修复由原实现线程推进；本次整理不做源码覆盖或类去重。', '- 功能房间当前制作目录与独立复核目录分别保留。ready是提交信号，独立ACCEPTANCE及实际复核记录决定是否放行。', '- 历史房间、配方、图片、NBT和冻结证据可能被绝对路径、SHA256清单或脚本引用，因此仍保留原路径。', '- 现场生成器的core/runtime/catalog/integration是分工组件，不能将某一个组件ready解释为全体完成。', '- FT-Dungeon-*模块和BossGate、DimensionDifficulty、Remodel派生工程保留各自build.gradle及模组资源，不能按同名文件直接覆盖合并。', '- 快捷方式只导航；编辑和构建仍作用于原目录。多工程工作区也直接指向原路径，没有复制依赖或生成第二份源码。', '- 回收与恢复证据沿用11月8日已核验的地牢缓存归档，本次新增目录整理没有删除源文件，也不重复计算空间收益。', '', '上述是基于现有目录、入口文件及当前任务状态的整理规则，不是完整的编译依赖分析；具体来源以各原工程baseline、工单、构建脚本及验收记录为准。']
    write(PORTAL / '引用与依赖.md', chr(10).join(relationships) + chr(10))
    readme = ['# FT 地牢工程总入口', '', '更新时间：' + generated, '', 'FT 地牢的工程、制作、复核、素材、交付和恢复入口集中在这里。分类收录 ' + str(len(projects)) + ' 个根目录条目；原工程路径及文件原位保留，正在进行的整合和复核可继续工作。', '', '## 优先打开', '', '- ' + link(PORTAL / 'FT地牢.code-workspace', 'FT地牢多工程工作区') + '：当前整合、S1–S8、独立复核、现场生成器四组件及主要源码模块。', '- ' + link(PORTAL / '当前工作与版本.md') + '：区分当前整合、已验收独立候选和待整改状态。', '- ' + link(PORTAL / '地牢工程清单.csv') + '：可排序筛选的完整分类表。', '- ' + link(PORTAL / 'projects.json') + '：机器清单、原路径、状态字段和入口SHA256。', '- ' + link(PORTAL / '引用与依赖.md') + '：整理边界与继承关系。', '', '## 分类', '', '| 目录 | 根目录条目数 | 用途 |', '| --- | ---: | --- |']
    for category, description in CATEGORIES.items():
        readme.append('| ' + link(PORTAL / category, category) + ' | ' + str(counts.get(category, 0)) + ' | ' + description + ' |')
    readme.extend(['', '每个分类中可双击Windows快捷方式进入原文件夹；分类说明同时提供原路径。快捷方式不是备份，不复制素材、构建缓存或源码。', '', '## 刷新与核验', '', '原线程会继续更新源文件，需要刷新时在PowerShell执行：', '', '```powershell', '& ' + chr(39) + str(PORTAL / '更新索引.ps1') + chr(39), '```', '', '脚本只重建此总入口下的索引和快捷方式，随后核对目标、工作区和入口SHA256。整理结果见 `整理核验.json`；若活动线程在整理过程中更改入口，会单独列出，不回写或覆盖该源文件。'])
    write(PORTAL / 'README.md', chr(10).join(readme) + chr(10))
    print(json.dumps(dict(Phase='索引生成完成', RootItems=len(projects), Categories=counts, Links=len(links), EvidenceFiles=len(evidence), SourceFilesChangedByOrganizer=0), ensure_ascii=False))

parser = argparse.ArgumentParser()
parser.add_argument('--finish', action='store_true')
args = parser.parse_args()
finish() if args.finish else index()
