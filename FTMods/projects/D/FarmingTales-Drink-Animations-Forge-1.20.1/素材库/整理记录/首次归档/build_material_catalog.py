"""Make a Chinese, linked material catalog and repair stale reference-only paths."""
from pathlib import Path
from collections import Counter
import csv
import json
import os
import re
import shutil
from organize_material_library import ROOT, LIBRARY, ARCHIVE, files, sha, write_json, native

def link(path, label=None):
    path = Path(path)
    assert native(path).exists(), path
    relative = os.path.relpath(path, LIBRARY).replace('\\','/')
    return f'[{label or path.name}](<{relative}>)'

def page(name, title, lines):
    target = LIBRARY / name
    target.write_text('# ' + title + '\n\n' + '\n'.join(lines) + '\n', encoding='utf-8')

updates = []
def save_edit(path, text):
    before = path.read_text(encoding='utf-8')
    if before == text:
        return
    backup = ARCHIVE / 'index-before' / path.relative_to(ROOT)
    backup.parent.mkdir(parents=True, exist_ok=True)
    assert not backup.exists(), f'Already edited: {path}'
    shutil.copy2(path, backup)
    old = sha(path)
    path.write_text(text, encoding='utf-8')
    updates.append(dict(path=path.relative_to(ROOT).as_posix(),before_sha256=old,after_sha256=sha(path)))

def repair_paths():
    old_project = 'Z:/workspace/projects/' + ROOT.name + '/'
    def convert(value):
        if isinstance(value, dict):
            return {k: convert(v) for k,v in value.items()}
        if isinstance(value, list):
            return [convert(v) for v in value]
        if isinstance(value, str):
            normalized = value.replace('\\','/')
            if normalized.startswith(old_project):
                target = ROOT / normalized[len(old_project):]
                assert target.exists(), target
                return str(target)
        return value
    for path in (ROOT / 'references').rglob('*.json'):
        data = json.loads(path.read_text(encoding='utf-8-sig'))
        fixed = convert(data)
        if fixed != data:
            save_edit(path,json.dumps(fixed,ensure_ascii=False,indent=2)+'\n')
    path = ROOT / 'references/2026-09-13/index.json'
    data = json.loads(path.read_text(encoding='utf-8'))
    for video in data['videos']:
        video['original_file'] = video['file']
        video['file'] = str(ROOT / 'references/original-videos' / Path(video['file']).name)
        video['pages'] = [name.replace('\\','/') for name in video['pages']]
    save_edit(path,json.dumps(data,ensure_ascii=False,indent=2)+'\n')
    path = ROOT / 'references/2026-09-13/逐秒索引.md'
    text = path.read_text(encoding='utf-8').replace('\\','/')
    for video in data['videos']:
        old = video['original_file'].replace('\\','/')
        text = text.replace(old, f"[项目内原视频](<../original-videos/{Path(video['file']).name}>)")
    save_edit(path,text)
    path = ROOT / 'index_animation_references.py'
    text = path.read_text(encoding='utf-8')
    text = text.replace("FFMPEG='Z:/ffmpeg/bin/ffmpeg.exe';FFPROBE='Z:/ffmpeg/bin/ffprobe.exe'",
                        "FFMPEG=str(ROOT.parents[2]/'ffmpeg/bin/ffmpeg.exe');FFPROBE=str(ROOT.parents[2]/'ffmpeg/bin/ffprobe.exe')")
    text = text.replace("path=Path('E:/zhuomian')/(name+'.mp4')", "path=ROOT/'references/original-videos'/(name+'.mp4')")
    save_edit(path,text)
    for name in ('netease-hotbar-2026-09-15','netease-snacks-2026-09-15'):
        path = ROOT / 'references' / name / 'review_frames.py'
        save_edit(path,path.read_text(encoding='utf-8').replace("ffmpeg='Z:/ffmpeg/bin/ffmpeg.exe'",
                                                              "ffmpeg=str(root.parents[4]/'ffmpeg/bin/ffmpeg.exe')"))
    write_json(LIBRARY / '索引路径修正.json',updates)

def catalog():
    report = json.loads((LIBRARY / '整理结果.json').read_text(encoding='utf-8'))
    plan = json.loads((LIBRARY / '整理计划.json').read_text(encoding='utf-8'))
    profiles = json.loads((ROOT / 'src/main/resources/assets/farmingtales_drink_anim/drink_profiles.json').read_text(encoding='utf-8'))
    aliases = json.loads((ROOT / 'src/main/resources/assets/farmingtales_drink_anim/aged_aliases.json').read_text(encoding='utf-8'))
    assets = ROOT / 'src/main/resources/assets/farmingtales_drink_anim'
    # Runtime material remains at its build path; this catalog is the single browsing entry.
    page('01-正在使用.md','正在使用的资源',[
        '当前发布版本为 **1.8.59**。这里链接到项目的实际资源，修改时从对应原位置编辑。','',
        '| 内容 | 位置 |','| --- | --- |',
        f'| 模型 | {link(assets / "models")} |',f'| 贴图 | {link(assets / "textures")} |',
        f'| 音效 | {link(assets / "sounds")} |',f'| 声音事件 | {link(assets / "sounds.json")} |',
        f'| {len(profiles)} 种普通物品动画配置 | {link(assets / "drink_profiles.json")} |',
        f'| {len(aliases)} 种陈化物品映射 | {link(assets / "aged_aliases.json")} |',
        f'| 动画代码 | {link(ROOT / "src/main/java")} |',
        f'| 当前发布说明 | {link(ROOT / "release-1.8.59.txt")} |',
        f'| 当前验证记录 | {link(ROOT / "verification-1.8.59.json")} |','',
        '素材来源记录：','']+[f'- {link(path)}' for path in sorted(ROOT.glob('*sources.json'))])
    hotbar = ROOT / 'references/netease-hotbar-2026-09-15'
    snacks = ROOT / 'references/netease-snacks-2026-09-15'
    names = ['白顶方块食物（原记录未确认名称）','香煎骑士牛排','珍珠咕噜肉','水果拼盘','苔原风味刺身',
             '黏液饭','黄金沙拉','第 8 格食物','第 9 格食物']
    progress = json.loads((hotbar / 'progress.json').read_text(encoding='utf-8'))
    for i in (7,8):
        names[i] = progress['completed'][f'slot{i+1:02d}']['name']
    lines = ['保留 **24 段网易实录**，以及全部抽帧、联系表和观察记录。','',
             '## 九格物品：食用与检视','', '| 物品 | 食用录像 | 检视录像 | 抽帧与观察 |', '| --- | --- | --- | --- |']
    for i,name in enumerate(names,1):
        stem = f'slot{i:02d}'
        lines.append(f'| {name} | {link(hotbar/(stem+"-eat.mp4"),"播放")} | {link(hotbar/(stem+"-inspect.mp4"),"播放")} | {link(hotbar/(stem+"-eat"),"食用抽帧")} / {link(hotbar/(stem+"-inspect"),"检视抽帧")} |')
    lines += ['',link(hotbar / 'progress.json','九格观察和视频校验记录'),'','## 六段专项参考','',
              '| 动作 | 录像 | 抽帧 |','| --- | --- | --- |']
    titles = ['串类食物：食用','串类食物：检视','滚轮切换','春卷：食用','黏液饭：食用','黏液饭：检视']
    for title,video in zip(titles,sorted(snacks.glob('*.mp4'))):
        lines.append(f'| {title} | {link(video,"播放")} | {link(video.with_suffix(""),"逐帧目录")} |')
    lines += ['',link(snacks/'观察笔记.md','六段动作观察笔记'),'',
              link(ROOT/'references/netease-breathing-observation-2026-09-15.md','网易呼吸观察'),'',
              link(ROOT/'references/netease-eating-observation-2026-09-15.md','网易食用观察')]
    page('02-网易参考.md','网易参考素材',lines)
    lines = ['三段原始用户视频已完整复制进项目并校验 SHA256。桌面原文件保留。','',
             '| 原始视频 | 时长 |','| --- | --- |']
    for video in report['imported_videos']:
        lines.append(f'| {link(ROOT/video["path"])} | {video["duration_seconds"]:.2f} 秒 |')
    lines += ['',link(ROOT/'references/2026-09-13/逐秒索引.md','三段视频逐秒索引（1288 帧）'),'',
              '| 专题 | 资料 |','| --- | --- |']
    for name,title in [('2026-09-13','完整索引与动作参考'),('2026-09-14-water','饮水、跑步与抛接'),
                       ('2026-09-14-hmi-soup','汤与连续抬杯'),('2026-09-15-garnish','果切、冰棍与巧克力'),
                       ('2026-09-15-snack-video','食物送入口与每口回落'),('2026-09-15-spoon','勺子动作')]:
        lines.append(f'| {title} | {link(ROOT/"references"/name,"打开资料")} |')
    lines += ['', '其他视频片段与用户图像参考：','']
    for name in ('jam-reference','user-video-101221','gameplay-20260913-055100-every-second'):
        lines.append(f'- {link(ROOT/"preview"/name)}')
    for path in sorted((ROOT/'preview').iterdir()):
        if path.is_file() and re.search(r'reference.*\.(?:png|jpg)$|^user-.*\.png$',path.name):
            lines.append(f'- {link(path)}')
    page('03-视频与图像参考.md','视频与图像参考',lines)
    lines = ['保留最近一代编辑底稿、原始持握模板、用户调整稿和有用的旧版基准。',
             '这些工程是各版本导出的编辑素材；后续酒液、装饰和新增酒品修正以当前项目资源为准。','',
             '| 编辑工程 | BBModel 数量 | 用途 |','| --- | ---: | --- |']
    descriptions = {'酒水扩展-v54':'普通酒水最近一代完整导出','酱罐动画-v54':'129 种酱罐的两种手臂',
                    '纸鸢酒水动画-v68':'纸鸢完整编辑底稿','纸鸢酒水动画-v69':'光 S 修正补充',
                    '葡园精酿酒水动画-v70':'Vinery / Brewery 编辑底稿','酒水扩展-v20':'模型恢复基准',
                    '酒水扩展-v34':'原杯装检视基准','上古水果果酱-v49':'已确认果酱动作基准',
                    '肘部联动与原创音效-v14':'导出工具使用的手臂和相机模板'}
    for path in sorted((ROOT/'blockbench').iterdir()):
        if path.is_dir() and path.name != '__pycache__':
            n=sum(f.suffix=='.bbmodel' for f in files(path))
            lines.append(f'| {link(path)} | {n} | {descriptions.get(path.name,"造型、动作或工具参考")} |')
    lines += ['','贴图与源模型对照：','']
    for path in sorted((ROOT/'preview').iterdir()):
        if path.is_file() and 'source' in path.name and path.suffix in ('.png','.jpg','.json'):
            lines.append(f'- {link(path)}')
    lines += [f'- {link(ROOT/"preview/cocktail-research","鸡尾酒结构研究")}',
              f'- {link(ROOT/"preview/papercraft-source","纸鸢原素材")}',
              f'- {link(ROOT/"model-reference-v20.json","v20 模型基准记录")}']
    page('04-模型与贴图.md','模型、贴图与编辑工程',lines)
    lines = ['| 内容 | 位置 |','| --- | --- |',
             f'| 当前使用音效 | {link(assets/"sounds")} |',
             f'| 原创拟音底稿 | {link(ROOT/"preview/original-foley")} |',
             f'| 声音候选 | {link(ROOT/"preview/sound-candidates-1.3.10")} |',
             f'| 开罐声音原素材 | {link(ROOT/"preview/can-open-physical-source.ogg")} |',
             f'| 动作与 TACZ 参考记录 | {link(ROOT/"REFERENCE.md")} |',
             f'| TACZ 手部参考 | {link(ROOT/"preview/tacz-hand-reference.txt")} |',
             f'| TACZ 渲染参考 | {link(ROOT/"preview/tacz-render-helper-reference.txt")} |',
             f'| Blockbench 相机插件源 | {link(ROOT/"blockbench/camera-source")} |','',
             '所有生成脚本保留在原位置。运行老版本脚本如需历史工程或逐帧图，可先按归档页恢复对应目录。']
    page('05-声音与技术参考.md','声音与技术参考',lines)
    by_category = Counter()
    sizes = Counter()
    for action in plan['actions']:
        by_category[action['category']] += action['files']
        sizes[action['category']] += action['bytes']
    labels = {'superseded_blockbench':'历史 Blockbench 工程及原 ZIP',
              'historical_motion_frames':'旧客户端逐帧动作截图','historical_render_comparison':'旧渲染问题对照图'}
    lines = [f'归档位置：{link(ARCHIVE,"打开历史归档")}。','',
             '本轮采用同盘移动，保留原文件内容和时间。没有永久删除文件，也不把移出主库的大小计算为磁盘释放量。','',
             '| 内容 | 文件数 | 大小 |','| --- | ---: | ---: |']
    for key in by_category:
        lines.append(f'| {labels[key]} | {by_category[key]} | {sizes[key]/2**30:.2f} GiB |')
    lines += ['', '恢复方法：在归档目录运行下面命令，先预览，再加 `--apply` 执行；已有文件不会被覆盖。','',
              '```powershell','python restore_material_archive.py --prefix "blockbench/酒水扩展-v21"',
              'python restore_material_archive.py --prefix "blockbench/酒水扩展-v21" --apply','```','',
              '恢复截图可指定完整原文件名，或用 `run-smoke/screenshots` 恢复全部已归档旧逐帧图。','',
              link(LIBRARY/'整理结果.json','整理结果')+' · '+link(LIBRARY/'整理计划.json','迁移清单')+' · '+link(LIBRARY/'归档文件清单.json','逐文件校验记录')]
    page('06-历史归档.md','历史归档与恢复',lines)
    page('README.md','FarmingTales-Drink-Animations 素材库',[
        '整理日期：2026-09-17；当前发布版本：1.8.59。','',
        '| 用途 | 入口 |','| --- | --- |',
        f'| 正在使用的模型、贴图、音效和配置 | {link(LIBRARY/"01-正在使用.md","正在使用")} |',
        f'| 24 段网易实录、逐帧图与观察笔记 | {link(LIBRARY/"02-网易参考.md","网易参考")} |',
        f'| 3 段原始参考视频、逐秒索引和用户图像 | {link(LIBRARY/"03-视频与图像参考.md","视频与图像参考")} |',
        f'| 常用 Blockbench 工程、手臂模板与贴图对照 | {link(LIBRARY/"04-模型与贴图.md","模型与贴图")} |',
        f'| 拟音原稿、音效候选、TACZ 与相机资料 | {link(LIBRARY/"05-声音与技术参考.md","声音与技术参考")} |',
        f'| 历史工程和旧测试截图 | {link(LIBRARY/"06-历史归档.md","历史归档与恢复")} |','',
        f'完整保留原参考库 {report["original_reference_files_preserved"]} 个文件，另收录 3 段原视频。',
        f'将 {report["archived_files"]:,} 个历史产物（{report["archived_bytes"]/2**30:.2f} GiB）移出主库归档。','',
        '以后把新增原视频放入 `references/original-videos`；新观察按专题放入 `references`，保留来源、时间段和笔记。',
        '运行资源继续由 `src/main/resources` 管理；旧工程或批量测试图移入历史归档时同时保留恢复清单。'])
    root_readme = ROOT / 'README.md'
    save_edit(root_readme, '# 素材库入口\n\n[打开整理后的素材库](素材库/README.md)。当前发布版本：1.8.59；历史说明见下文。\n\n'+root_readme.read_text(encoding='utf-8'))
    write_json(LIBRARY/'索引路径修正.json',updates)
    entries=[]
    for folder in ('references','src/main/resources','blockbench'):
        for path in files(ROOT/folder):
            entries.append(dict(category=folder,path=path.relative_to(ROOT).as_posix(),bytes=native(path).stat().st_size))
    with (LIBRARY/'素材清单.csv').open('w',encoding='utf-8-sig',newline='') as stream:
        writer=csv.DictWriter(stream,fieldnames=['category','path','bytes']);writer.writeheader();writer.writerows(entries)
    print(f'Catalog built: 7 pages, {len(entries)} material entries, {len(updates)} path/index repairs.',flush=True)

if __name__ == '__main__':
    repair_paths()
    catalog()
