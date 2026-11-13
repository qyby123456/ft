from pathlib import Path
import zipfile
ROOT=Path(__file__).absolute().parent
instructions='''农场物语-饮用与检视动画 1.8.2 / 酒水扩展-v32

53 种饮品 × Steve / 纤细手臂，共 106 个工程。
每个工程含 idle、draw、put_away、inspect、drink、walk、sprint、jump、place、preview_complete。
安装 cameras.js，在动画预览中关联 camera_first_person 查看第一人称。

所有杯装饮用姿势降低，手与杯子共同移动。葡萄酒杯的举杯时间由 1.9 秒缩短到 0.95 秒；软木塞酒瓶保留开塞时间，最后抬瓶阶段加快一倍。检视时长、吞咽时长及四声吞咽保持不变。
place 是正常放置的小幅挥手，约 0.3 秒，有剩余时持续持握，不再收下后重新掏出。put_away 仍为切换物品时的收回。
杯内液面限制为最多 12 度，并按剩余酒量和杯口空间进一步限制；视角上下 25 度以外不再增加重力倾斜。液体约 72% 不透明，冰块约 68% 不透明。叶子、丝带正反面分别显示，避免同一位置反复绘制。

手臂、容器、装饰和 camera 来自同一运行时代码的 60 fps 采样，经贝塞尔关键帧精简。Blockbench 液体是酒量代理，透明度可预览；真实裁切液面、玩家移动惯性和瓶内手部遮挡由游戏计算。
V 自定义食用，G 检视，可改键；普通右键饮用保持原版。声音保持上一版本。
'''
(ROOT/'blockbench/使用说明-v32.txt').write_text(instructions,encoding='utf-8')
s=(ROOT/'blockbench/export_drinks_v31.py').read_text(encoding='utf-8').replace('v31','v32').replace('1.8.1','1.8.2')
s=s.replace('def tex(location):','def tex(location,opacity=255):\n            key=location+str(opacity)')
s=s.replace('if location in texture_ids:return texture_ids[location]','if key in texture_ids:return texture_ids[key]')
s=s.replace('index=len(textures);texture_ids[location]=index','''if opacity<255:
                im=im.convert('RGBA');im.putalpha(im.getchannel('A').point(lambda a:round(a*opacity/255)))
                encoded=io.BytesIO();im.save(encoded,format='PNG');raw=encoded.getvalue()
            index=len(textures);texture_ids[key]=index''')
s=s.replace("uuid=uid(item+location)","uuid=uid(item+key)")
s=s.replace('shift=(0,0,0)):', 'shift=(0,0,0),opacity=255):')
s=s.replace("index=tex(texture_map[value['texture'][1:]])","index=tex(texture_map[value['texture'][1:]],opacity)")
s=s.replace("part['name'],model['textures'])","part['name'],model['textures'],opacity=173 if part['kind']=='ICE' else 255)")
s=s.replace("cube(e,name+'_volume',name,{'0':location})","cube(e,name+'_volume',name,{'0':location},opacity=184)")
(ROOT/'blockbench/export_drinks_v32.py').write_text(s,encoding='utf-8')
s=(ROOT/'preview/verify_bb_fit_v31.py').read_text(encoding='utf-8').replace('v31','v32')
(ROOT/'preview/verify_bb_fit_v32.py').write_text(s,encoding='utf-8')
readme='# 农场物语-饮用与检视动画 1.8.2\n\nMinecraft 1.20.1 / Forge 47.4.10 客户端独立模组。\n\n'+instructions.split('\n',1)[1]+'''

模型及纹理源文件与 1.8.1 一致，透明度和重叠面处理仅作用于本模组第一人称渲染，不改变世界里其他模组的方块渲染。
液体与冰块共用图集和透明排序批次，避免液体遮住冰块。原瓶身负尺寸结构及杯底完整保留。

验证记录见 verification.json：包括单元测试、关闭光影并在窗口创建前隐藏的隔离客户端、两种手臂和 Blockbench 回读拟合。完整整合包与用户存档未启动。原版右键、物品消耗和音频行为另有客户端检查。
旧版 JAR、配置及源码备份：backups/1.8.1-before-surface-fix。安装后重启游戏生效。
'''
(ROOT/'README.md').write_text(readme,encoding='utf-8')
print('Prepared v32 exporter and release notes.')
