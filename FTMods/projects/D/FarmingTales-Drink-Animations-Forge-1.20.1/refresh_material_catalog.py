"""Refresh catalog links for the physical Blockbench layout after authorized cleanup."""
from pathlib import Path
import csv
import json
import os
import re
from finish_blockbench_cleanup import ROOT,BB,LIB,AUDIT,GROUPS,files,native,write

def link(path,label=None,base=LIB):
    assert native(path).exists(),path
    return f'[{label or path.name}](<{os.path.relpath(path,base).replace(chr(92),"/")}>)'

def page(path,title,lines):
    path.write_text('# '+title+'\n\n'+'\n'.join(lines)+'\n',encoding='utf-8')

result=json.loads((AUDIT/'完成.json').read_text(encoding='utf-8'))
lines=['已按用途整理文件夹；打开工程目录内的 `.bbmodel` 即可编辑。','',
       '| 目录 | 内容 |','| --- | --- |']
descriptions={'01-常用工程':'酒水、酱罐、鸡尾酒、纸鸢与葡园精酿的常用导出工程',
              '02-模板与基准':'导出所用手臂模板、已确认的模型和动作基准',
              '03-参考素材':'用户持握稿、木勺画风与木勺模型草稿',
              '04-导出工具':'当前导出脚本、相机插件及算法参考脚本',
              '05-打包文件':'保留工程对应的 ZIP 包','06-使用说明':'保留版本的使用说明'}
for category,description in descriptions.items():
    lines.append(f'| {link(BB/category,base=BB)} | {description} |')
lines += ['', '这些是各版本的编辑底稿；当前游戏效果以项目运行资源为准。',
          '纸鸢 v68 是完整工程，v69 是光 S 补充；不要只使用补充包。','',
          '旧工程、旧测试截图及上一轮历史归档已按要求直接删除。',
          link(LIB/'README.md','返回素材库',base=BB)]
page(BB/'README.md','Blockbench 素材目录',lines)

details=['这里的工程已经实际移动到按用途划分的子目录。',
         '后续酒液、装饰和新增物品的运行效果以当前项目资源为准。','']
for category,names in GROUPS.items():
    details += ['## '+category[3:],'','| 工程或素材 | BBModel 数量 |','| --- | ---: |']
    for name in names:
        folder=BB/category/name
        count=sum(path.suffix=='.bbmodel' for path in files(folder))
        details.append(f'| {link(folder)} | {count} |')
    details += ['']
details += ['## 工具与打包','',f'- {link(BB/"04-导出工具")}',f'- {link(BB/"05-打包文件")}',
            f'- {link(BB/"06-使用说明")}','','## 贴图与源模型对照','']
for path in sorted((ROOT/'preview').iterdir()):
    if path.is_file() and 'source' in path.name and path.suffix in ('.png','.jpg','.json'):
        details.append('- '+link(path))
details += ['- '+link(ROOT/'preview/cocktail-research'),'- '+link(ROOT/'preview/papercraft-source')]
page(LIB/'04-模型与贴图.md','模型、贴图与编辑工程',details)

sounds=LIB/'05-声音与技术参考.md'
text=sounds.read_text(encoding='utf-8').replace('../blockbench/camera-source','../blockbench/04-导出工具/camera-source')
text=text.replace('所有生成脚本保留在原位置。运行老版本脚本如需历史工程或逐帧图，可先按归档页恢复对应目录。',
                  '导出工具已移入 blockbench/04-导出工具，素材路径同步修正。旧版脚本只保留少量源代码供算法参考，其旧工程和逐帧图已删除。')
sounds.write_text(text,encoding='utf-8')
old=LIB/'06-历史归档.md'
if old.exists():old.unlink()
page(LIB/'06-清理记录.md','清理结果',[
    f'按用户要求直接删除 **{result["deleted_files"]:,} 个旧工程文件和测试图**，文件总大小 **{result["deleted_GiB"]:.2f} GiB**。','',
    '上一轮历史归档的工程与截图已删除，不再提供恢复入口。只保留清理清单和少量索引修改记录。','',
    '保留全部 24 段网易实录、3 段原始用户参考视频、抽帧与观察笔记，以及 103 张近期修复验证图。','',
    link(LIB/'整理结果.json','清理结果')+' · '+link(AUDIT/'删除文件清单.json','删除文件清单')+' · '+link(AUDIT/'脚本路径更新.json','工具路径调整')])
page(LIB/'README.md','FarmingTales-Drink-Animations 素材库',[
    '当前发布版本：1.8.59。素材、工程和工具已按用途整理。','',
    '| 用途 | 入口 |','| --- | --- |',
    f'| 当前模型、贴图、音效和配置 | {link(LIB/"01-正在使用.md","正在使用")} |',
    f'| 24 段网易实录、抽帧和观察笔记 | {link(LIB/"02-网易参考.md","网易参考")} |',
    f'| 3 段原始参考视频、逐秒索引和图像 | {link(LIB/"03-视频与图像参考.md","视频与图像")} |',
    f'| 常用工程、模板、工具和打包文件 | {link(BB/"README.md","Blockbench 目录")} / {link(LIB/"04-模型与贴图.md","素材明细")} |',
    f'| 拟音、音效候选、TACZ 与相机资料 | {link(LIB/"05-声音与技术参考.md","声音与技术参考")} |',
    f'| 旧工程和测试图删除记录 | {link(LIB/"06-清理记录.md","清理记录")} |','',
    f'已直接删除 {result["deleted_GiB"]:.2f} GiB 的旧工程和测试图。网易实录、视频参考及当前运行资源完整保留。'])
page(BB/'04-导出工具/README.md','导出工具',[
    '从项目根目录运行脚本；素材与输出目录已按新的分类布局调整。','',
    '| 任务 | 脚本 |','| --- | --- |',
    '| 普通酒水 | export_drinks_v54.py |','| 果酱基准 | export_jam_v54.py |',
    '| 全部酱罐 | export_jar_family_v54.py |','| 巧克力酱 | export_chocolate_v55.py |',
    '| 鸡尾酒 | export_cocktails_v56.py |','| 纸鸢完整工程 | export_papercraft_v68.py |',
    '| 光 S 补充 | export_guang_v69.py |','| Vinery / Brewery | export_letsdo_v70.py |','',
    '导出前先用项目 Gradle 任务生成 build/blockbench 中的采样数据。',
    'export_elbow_v14.py 提供公共拟合函数和手臂模板导出；camera-source 保留相机插件。',
    '旧版脚本目录只作算法参考。其历史工程和测试图已删除，不作为当前生成入口。'])
# Correct material paths in export indexes; editable model contents stay byte-identical.
mapping=json.loads((AUDIT/'计划.json').read_text(encoding='utf-8'))['moves']
updated=[]
def fix(value):
    if isinstance(value,dict):return {k:fix(v) for k,v in value.items()}
    if isinstance(value,list):return [fix(v) for v in value]
    if isinstance(value,str):
        normalized=value.replace('\\','/')
        marker='/blockbench/'
        if marker in normalized:
            suffix=normalized.split(marker,1)[1]
            for old,new in mapping.items():
                if suffix==old or suffix.startswith(old+'/'):
                    target=BB/new/suffix[len(old):].lstrip('/') if suffix!=old else BB/new
                    if native(target).exists():return str(target)
    return value
for parent in ('01-常用工程','02-模板与基准','03-参考素材'):
    for path in (BB/parent).rglob('导出检查.json'):
        data=json.loads(path.read_text(encoding='utf-8'));fixed=fix(data)
        if fixed!=data:write(path,fixed);updated.append(path.relative_to(ROOT).as_posix())
write(AUDIT/'工程索引路径更新.json',updated)
entries=[]
for folder in ('references','src/main/resources','blockbench'):
    for path in files(ROOT/folder):
        entries.append(dict(category=folder,path=path.relative_to(ROOT).as_posix(),bytes=native(path).stat().st_size))
with (LIB/'素材清单.csv').open('w',encoding='utf-8-sig',newline='') as stream:
    writer=csv.DictWriter(stream,fieldnames=['category','path','bytes']);writer.writeheader();writer.writerows(entries)
print(f'Refreshed Blockbench layout, seven catalog pages, {len(updated)} export indexes and {len(entries)} material entries.')
