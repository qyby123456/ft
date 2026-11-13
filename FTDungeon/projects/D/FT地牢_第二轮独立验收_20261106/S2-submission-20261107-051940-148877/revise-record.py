import json
from pathlib import Path
import sys

ROOT=Path(__file__).resolve().parent
folder=ROOT/sys.argv[1]
path=folder/'design.json'
design=json.loads(path.read_text(encoding='utf-8'))
design['revision']='R2 preliminary visual REQUEST_CHANGES remediation; previous evidence in archive-preliminary-v1'
design['architectural_remediation']=['内侧立体门柱、柱脚柱头、倒置楼梯拱券、半砖分层檐口','地面周边橙色边框与中央菱形纹样，原尺寸家具与路线目标保留']
replacements={
 'S2-11':[(2,'已补原版橙色菱形纹旗帜；书梯竖直贴架，独立梯子和旗帜近景证明；无斜梯与尘埃光束。')],
 'S2-13':[(0,'独立3×3黑石大锅包围原版半满水炼药锅，暖光菌光体代替火与熔岩；宽壁炉罩与烟囱完整。')],
 'S2-16':[(2,'后阶梯通向实际维护平台；新增平台路线目标，原五处目标不删改。')],
 'S2-18':[(2,'已补原版橙色菱形纹纪念旗，颅骨与骨块为实际原版方块。')],
 'S2-20':[(0,'伪装墙保留三格净宽常开密道；宝龛前隔墙降为一格矮界墙以展示箱子焦点，拉杆未接线。')]
}
for index,text in replacements.get(design['id'],[]):design['differences'][index]=text
if design['id']=='S2-15':design['differences'].append('R2补三面橙色菱形原版旗帜；靶为原版六面靶纹，无单独朝向属性。')
if design['id']=='S2-19':design['differences'].append('R2侧阶梯护墙降为随台阶递减护栏，柱间增加倒置楼梯拱形支撑；新增底层支撑近景。')
if design['id']=='S2-17':design['differences'].append('R2中央桌改四根木腿，移除实心侧台结构；木框通透，增加切石机与花盆工具意象近景。')
if design['id']=='S2-16' and not any(target['label']=='maintenance-platform' for target in design['targets']):design['targets'].append({'label':'maintenance-platform','position':[19,8,3]})
path.write_text(json.dumps(design,ensure_ascii=False,indent=2),encoding='utf-8')
