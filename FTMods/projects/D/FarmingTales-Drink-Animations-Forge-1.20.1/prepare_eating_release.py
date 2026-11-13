from pathlib import Path
ROOT=Path(__file__).resolve().parent
def update(path, old, new):
    p=ROOT/path
    s=p.read_text(encoding='utf-8')
    assert old in s,path
    p.write_text(s.replace(old,new),encoding='utf-8')
for path in ['build.gradle','src/main/resources/META-INF/mods.toml']:
    update(path,'1.8.22','1.8.23')
update('README.md','动画 1.8.22','动画 1.8.23')
update('README.md','Minecraft 1.20.1 / Forge 47.4.10 客户端独立模组。','''Minecraft 1.20.1 / Forge 47.4.10 客户端独立模组。

1.8.23：食用增加四种轻微动作变化，每四次遍历全部样式，相邻两次不重复。抬手弧线、每口间的调整和收手回稳有所区别；手、容器与附件共享变换，保持握持。食用增加轻微滞后的真实镜头跟随，额外俯仰不超过 0.32°，横滚不超过 0.045°。原有呼吸持续播放，检视幅度、开盖、消耗时机和声音保持原样。

Blockbench v52 包含 drink 和 drink_variant_2/3/4，可分别预览四种食用动作。全部来自游戏共用计算，包含 camera；Steve 与纤细手臂分别导出。
''')
for kind in ['jam','drinks','jar_family']:
    text=(ROOT/f'blockbench/export_{kind}_v51.py').read_text(encoding='utf-8').replace('v51','v52').replace('1.8.22','1.8.23')
    text=text.replace("'inspect','drink','walk'","'inspect','drink','drink_variant_2','drink_variant_3','drink_variant_4','walk'")
    text=text.replace('preview_complete 十条动画。','preview_complete，并增加 drink_variant_2/3/4，共十三条动画。')
    text=text.replace('新增连续呼吸，握持、速度、音效及液体阻尼保持相同。','增加四种食用细节及轻微相机跟随，连续呼吸保持；握持、速度、音效及液体阻尼保持相同。')
    (ROOT/f'blockbench/export_{kind}_v52.py').write_text(text,encoding='utf-8')
(ROOT/'blockbench/使用说明-v52.txt').write_text('''农场物语-饮用与检视动画 1.8.23 / 紫菜 / ARR
drink、drink_variant_2、drink_variant_3、drink_variant_4 对应四种食用变化：不同的抬手弧线、轻微调整和收手回稳。游戏每次食用选择一种，相邻动作不重复；不改开盖与消耗时间。
食用 camera 轻微滞后跟随手部。idle 保留连续 4 秒呼吸，检视 camera 幅度不变。
安装 cameras.js 后关联 camera_first_person 查看第一人称。Steve / 纤细手臂分别适配。
游戏内 V 食用、G 检视，均可改键；普通右键保持原版。
液体在 Blockbench 中近似表达，游戏使用连续裁切与手部遮挡。
''',encoding='utf-8')
text=(ROOT/'preview/verify_bb_fit_v51.py').read_text(encoding='utf-8').replace('v51','v52').replace('ten clips','thirteen clips')
(ROOT/'preview/verify_bb_fit_v52.py').write_text(text,encoding='utf-8')
text=(ROOT/'blockbench/04-导出工具/旧版脚本/package_breathing_v51.py').read_text(encoding='utf-8').replace('v51','v52').replace('1.8.22','1.8.23')
(ROOT/'blockbench/04-导出工具/旧版脚本/package_eating_v52.py').write_text(text,encoding='utf-8')
text=(ROOT/'install_release.ps1').read_text(encoding='utf-8').replace('1.8.22','1.8.23').replace('v1822','v1823').replace('1.8.21-before-breathing','1.8.22-before-eating-variation').replace('1.8.21','1.8.22')
(ROOT/'install_release.ps1').write_text(text,encoding='utf-8')
(ROOT/'references/netease-eating-observation-2026-09-15.md').write_text('''在用户授权的网易版客户端中观察：G 食用、Y 检视。这是参考游戏的键位，本模组仍为 V/G。
观察盘装食物与荷包紫颂烧的食用，并小幅转动视角。按约 0.5 秒间隔查看截图，没有保存视频或做精确周期测量。
食用有弧线抬起、角度调整、收回；托盘和支撑手也有轻微配合。背景跟随远小于前景动作。同一个动作内各次取食姿势有所不同，尚未确认参考模组是否随机选择整段动画。
短按 W、W+空格、D 未捕捉到明确完整步态，不能据此推断行走或跳跃细节。
本项目独立设计四种食用细节，用共用刚体变换维护握持；每口具有不同方向和强弱，镜头稍后跟随。参数为原创调节值，不复制参考资源。检视保持舒适的小幅镜头。
''',encoding='utf-8')
print('Prepared 1.8.23 and v52.')
