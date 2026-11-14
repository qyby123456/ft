import argparse
from datetime import datetime, timedelta, timezone
import hashlib
import json
from pathlib import Path
import shutil
import zipfile


ROOT = Path(__file__).resolve().parents[1]
SKIP_PARTS = {'__pycache__', 'node_modules', '.git', '.gradle', 'out', '_archive'}


def digest(content):
    return hashlib.sha256(content).hexdigest()


def save_json(path, value):
    path.write_text(json.dumps(value, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')


def pack(path, files):
    manifest = {}
    with zipfile.ZipFile(path, 'x', compression=zipfile.ZIP_DEFLATED, compresslevel=6) as archive:
        for name, source in sorted(files.items()):
            content = source.read_bytes() if isinstance(source, Path) else source
            archive.writestr(name, content)
            manifest[name] = {'sha256': digest(content), 'bytes': len(content)}
    with zipfile.ZipFile(path) as archive:
        if archive.testzip() is not None:
            raise RuntimeError('CRC verification failed: ' + str(path))
        if len(archive.namelist()) != len(set(archive.namelist())):
            raise RuntimeError('Duplicate export entries')
        for name, expected in manifest.items():
            if digest(archive.read(name)) != expected['sha256']:
                raise RuntimeError('Export checksum mismatch: ' + name)
    return manifest


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--output', type=Path)
    arguments = parser.parse_args()
    work = ROOT / '_work/spark-YlP6kNivxD-20261027'
    receipt = json.loads((work / 'deployment-receipt.json').read_text(encoding='utf-8'))
    live = Path(receipt['target'])
    installed = live.read_bytes()
    if digest(installed) != receipt['candidateSha256']:
        raise RuntimeError('Installed runtime changed; review the new baseline before export')
    zone = timezone(timedelta(hours=8))
    stamp = datetime.now(zone).strftime('%Y%m%d-%H%M%S')
    output = (arguments.output or ROOT / ('output/FTBoss-src-' + stamp)).resolve()
    output.relative_to(ROOT / 'output')
    output.mkdir(parents=True, exist_ok=False)
    source_files = {'src/' + path.relative_to(ROOT / 'src').as_posix(): path
                    for path in (ROOT / 'src').rglob('*') if path.is_file()}
    for name in source_files:
        if '..' in Path(name).parts or '\\' in name:
            raise RuntimeError('Unsafe source path: ' + name)
    source_manifest = pack(output / 'src.zip', source_files)
    delta = {}
    resource_differences = []
    source_only = []
    with zipfile.ZipFile(live) as archive:
        entries = {entry.filename: entry for entry in archive.infolist() if not entry.is_dir()}
        suspect = []
        for name in entries:
            parts = Path(name).parts
            if (name.startswith(('/', '\\')) or '..' in parts or '\\' in name
                    or any(part in {'.git', '.gradle', '__pycache__', '_work', '_backup', '__MACOSX'} for part in parts)
                    or name.lower().endswith(('.bak', '.old', '.tmp', '.orig', '.rej', '.log', '.java', '.ps1', '.py', '.bbmodel'))):
                suspect.append(name)
        if suspect:
            raise RuntimeError('Runtime development/unsafe entries need manual review: ' + repr(suspect))
        runtime_resources = {name for name in entries if name.startswith(('assets/', 'data/'))
                             or name in {'META-INF/mods.toml', 'ftboss.mixins.json', 'ftboss.refmap.json', 'pack.mcmeta'}}
        for name in sorted(runtime_resources):
            source = ROOT / 'src/main/resources' / name
            content = archive.read(name)
            if not source.is_file() or digest(source.read_bytes()) != digest(content):
                delta['runtime-resources/' + name] = content
                resource_differences.append({'path': name, 'sourceExists': source.is_file(), 'runtimeSha256': digest(content)})
        for path in (ROOT / 'src/main/resources').rglob('*'):
            if path.is_file() and path.relative_to(ROOT / 'src/main/resources').as_posix() not in entries:
                source_only.append(path.relative_to(ROOT / 'src/main/resources').as_posix())
        runtime_classes = {name[:-6] for name in entries if name.startswith('net/farmingtales/ftboss/')
                           and name.endswith('.class') and '$' not in name}
        source_classes = {path.relative_to(ROOT / 'src/main/java').as_posix()[:-5]
                          for path in (ROOT / 'src/main/java').rglob('*.java')}
        missing_classes = sorted(runtime_classes - source_classes)
        source_only_classes = sorted(source_classes - runtime_classes)
        mod_audit = {'entries': len(archive.infolist()), 'duplicateEntries': len(archive.namelist()) - len(set(archive.namelist())),
                     'developmentOrUnsafeEntries': suspect, 'runtimeResourceCount': len(runtime_resources),
                     'sourceResourceDifferences': resource_differences, 'sourceOnlyResourcesPreserved': source_only}
    delta_manifest = pack(output / 'runtime-resource-delta.zip', delta)
    support = {}
    for directory in ['tools', 'gradle', 'libs', 'art', 'ref', '_ref', 'runtime-reference']:
        for path in (ROOT / directory).rglob('*'):
            if not path.is_file() or any(part in SKIP_PARTS for part in path.relative_to(ROOT / directory).parts):
                continue
            if path.suffix.lower() in {'.pyc', '.bak', '.class', '.log'}:
                continue
            support['engineering-support/' + path.relative_to(ROOT).as_posix()] = path
    for name in ['build.gradle', 'settings.gradle', 'gradle.properties', 'build.bat', 'BUILD_CURRENT.ps1',
                 '.editorconfig', '.gitattributes', '.gitignore', 'AGENTS.md', 'CODE_STYLE.md']:
        support['engineering-support/' + name] = ROOT / name
    source_status = {
        'runtimeTopLevelClasses': len(runtime_classes), 'javaSourceFiles': len(source_classes),
        'runtimeTopLevelClassesWithoutMatchingSourcePath': missing_classes,
        'sourceClassesWithoutMatchingRuntimeTopLevelClass': source_only_classes,
        'fullSourceBuildVerified': False, 'decompiledReferencesAreNotBuildSources': True,
        'legacyBuildCurrentDoesNotReproduceThisRelease': True,
    }
    support['engineering-support/export-status.json'] = (json.dumps(source_status, ensure_ascii=False, indent=2) + '\n').encode('utf-8')
    for name in ['deployment-receipt.json', 'profile-summary.json', 'organization-receipt.json']:
        support['verification/' + name] = work / name
    support_manifest = pack(output / 'engineering-support.zip', support)
    (output / 'qys-bosses.jar').write_bytes(installed)
    for name, source in source_files.items():
        if digest(source.read_bytes()) != source_manifest[name]['sha256']:
            raise RuntimeError('Source changed while exporting: ' + str(source))
    for name, source in support.items():
        if isinstance(source, Path) and digest(source.read_bytes()) != support_manifest[name]['sha256']:
            raise RuntimeError('Support input changed while exporting: ' + str(source))
    if digest(live.read_bytes()) != digest(installed):
        raise RuntimeError('Installed runtime changed while exporting')
    save_json(output / 'source-manifest.json', source_manifest)
    save_json(output / 'runtime-resource-manifest.json', delta_manifest)
    save_json(output / 'engineering-manifest.json', support_manifest)
    save_json(output / 'mod-file-audit.json', mod_audit)
    save_json(output / 'source-status.json', source_status)
    readme = f'''# FTBoss 当前源码导出与文件整理

- 生成时间：{datetime.now(zone).isoformat()}。
- 主工程：`{ROOT}`。
- 本轮 Spark：[YlP6kNivxD](https://spark.lucko.me/YlP6kNivxD)。
- 当前已部署运行包 SHA256：`{digest(installed)}`。

## 取件

1. `src.zip`：主工程 `src/` 的逐文件原样快照，共 {len(source_manifest)} 个文件，包含 Java 源码和资源；无缓存、历史构建树或 Git 数据。
2. `qys-bosses.jar`：当前已安装成品，未经二次功能删改。
3. `engineering-support.zip`：构建配置、依赖、工具、模型编辑资料及既有运行时参考；其中反编译参考不属于 Gradle 源集。
4. `runtime-resource-delta.zip`：源码资源与当前运行包不同或缺少的 {len(delta_manifest)} 个条目，仅供核对当前运行资源；没有覆盖主工程的待发布内容。
5. 三份 `*-manifest.json`：逐文件 SHA256；`mod-file-audit.json` 与 `source-status.json` 说明差异和构建边界。

## 优化与验收

原始字段鉴权的调用栈由三段流操作改为一次筛选。保留跳过首帧、钩子过滤、四个有效帧上限、原始访问者与观察者的精确顺序及所有原有授权分支；找到两个帧后停止，不缓存授权结果。

65,461 组场景、196,385 条差分断言通过，直接执行原包和新包提取的筛选方法。完整运行类 `-Xverify:all`、原方法保留检查及两种 ZIP 检查通过。典型路径消费帧数 6 → 4；这不是实机 MSPT 提速百分比。尚未进行优化后的游戏内复测。

## 文件整理范围

运行包 {mod_audit['entries']} 个条目中，无重复项、越界路径和开发备份脚本；没有通过删除技能、资源或兼容类来缩小包。主工程过期根目录源码压缩包、Python 字节码缓存和旧日志已按哈希归档，可恢复。没有改动其他聊天的任务目录、Git 历史、存档、配置或文档库。

源码中另有 {len(source_only)} 个运行包未包含的资源，保持原样，不因“整理”擅自删除。

## 重要构建边界

当前运行包有 {len(runtime_classes)} 个顶层类，主源码路径有 {len(source_classes)} 个 Java 文件；其中 {len(missing_classes)} 个运行顶层类没有对应源码路径。类清单不是全量可编译性的证明。

**本导出不能声称是已经恢复完成的全量可编译源码工程。** 当前工程长期存在源码和运行字节码分叉，反编译文件只是参考。`BUILD_CURRENT.ps1` 仍对应其旧固定基线，不会复现本次成品；不要用未经验收的全量 Gradle 产物覆盖本次运行包。

本轮补丁可由 `tools/build_spark_raw_exposure.ps1` 从受原类 SHA256 约束的部署前基线重建。补丁已应用的包会拒绝重复应用；部署前原包保留在本轮 `deployment-receipt.json` 指定位置。

`docs impact: none`：此次优化不改变玩法、注册、存档或网络合同，未写入 `docs/**`。
'''
    (output / 'README.md').write_text(readme, encoding='utf-8')
    checksums = {path.name: digest(path.read_bytes()) for path in output.iterdir() if path.is_file()}
    save_json(output / 'SHA256.json', checksums)
    result = {'status': 'PASS', 'output': str(output), 'sourceFiles': len(source_manifest),
              'supportFiles': len(support_manifest), 'resourceDifferences': len(delta_manifest),
              'runtimeSourceGaps': len(missing_classes), 'runtimeSha256': digest(installed),
              'fullSourceBuildVerified': False, 'crcAndSha256Verified': True}
    save_json(work / 'source-export-receipt.json', result)
    print(json.dumps(result, ensure_ascii=True, indent=2), flush=True)


if __name__ == '__main__':
    main()
