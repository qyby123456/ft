from pathlib import Path

ROOT=Path(__file__).resolve().parent
def write(path,text):
    (ROOT/path).write_text(text,encoding='utf-8')
for file in ['build.gradle','src/main/resources/META-INF/mods.toml']:
    write(file,(ROOT/file).read_text(encoding='utf-8').replace('1.8.21','1.8.22'))
notes='''1.8.22 为全部支持物品增加连续呼吸：手、容器和附件共享轻微起伏与侧摆，待机和食用均持续播放；切换动作不会重置呼吸相位。保持现有动作、材质和声音，呼吸不额外摇动世界镜头。参考网易版运行效果的运动特点，未复制其模型或动画。

Blockbench v51：酱罐动画-v51.zip（129 种 × 两种手臂）和酒水扩展-v51.zip（53 种 × 两种手臂）。idle 为完整 4 秒呼吸循环，preview_complete 使用贯穿整个演示的连续时钟；camera 保留既有动作。
'''
readme=(ROOT/'README.md').read_text(encoding='utf-8')
write('README.md',readme.replace('动画 1.8.21','动画 1.8.22',1).replace('Minecraft 1.20.1 / Forge 47.4.10 客户端独立模组。','Minecraft 1.20.1 / Forge 47.4.10 客户端独立模组。\n\n'+notes,1))
write('references/netease-breathing-observation-2026-09-15.md','''观察用户授权操作的网易版进食动画参考：G 食用，Y 检视；此键位仅属于参考游戏。
待机时手和盘子共同小幅上下起伏，略有侧摆，世界画面基本稳定。
食用时托盘手并未锁死，仍保留细微变化；动作期间和静止持握之间没有明显分段。
以约 0.5–0.6 秒间隔查看截图，未做精确周期或幅度测量，未保存参考视频。
本模组据此自行制作呼吸叠加层，不复制参考资源。幅度与 4 秒周期是本项目的原创调节值。
''')
for kind,old,folder in [('jam','49','上古水果果酱'),('drinks','34','酒水扩展')]:
    text=(ROOT/f'blockbench/export_{kind}_v{old}.py').read_text(encoding='utf-8')
    text=text.replace('v'+old,'v51').replace('runtime DrinkMotion and BottleRig 1.8.4','紫菜 / ARR / FarmingTales 1.8.22')
    text=text.replace("clips['preview_complete']=combine([clips['draw'],subclip(clips['idle'],0,.5),clips['inspect'],subclip(clips['idle'],0,.5),clips['put_away']])","clips['preview_complete']=copy.deepcopy(data['clips'][shape+'/preview_complete'])")
    text=text.replace('tolerance=.06','tolerance=.015').replace("continuous_euler([f[field]['quaternion'] for f in frames]),.30","continuous_euler([f[field]['quaternion'] for f in frames]),.08")
    if kind=='drinks':
        text=text.replace("    for shape,label in [('slim','纤细手臂'),('wide','Steve')]:","    if profile['style']=='JAM':continue\n    for shape,label in [('slim','纤细手臂'),('wide','Steve')]:")
    write(f'blockbench/export_{kind}_v51.py',text)
write('blockbench/使用说明-v51.txt','''农场物语-饮用与检视动画 1.8.22 / 紫菜 / ARR
从游戏共享骨架采样导出，新增持续的呼吸起伏；idle 为 4 秒无缝循环，preview_complete 贯穿拿出、待机、检视、收回，呼吸相位连续。
安装 cameras.js 后关联 camera_first_person 查看第一人称。保留原 camera 动作，呼吸只作用于前景手和物品，避免额外晃动世界。
Steve / 纤细手臂分别适配。游戏内 V 食用、G 检视，均可改键；普通右键保持原版。
液体的 Blockbench 表达是游戏裁切体积的近似；游戏使用连续裁切和手部遮挡。
''')
text=(ROOT/'blockbench/export_jar_family_v50.py').read_text(encoding='utf-8').replace('v50','v51').replace('v49','v51').replace('1.8.21','1.8.22')
text=text.replace('相机、握持、速度、音效及液体阻尼保持相同。','新增连续呼吸，握持、速度、音效及液体阻尼保持相同。idle 为 4 秒循环，preview_complete 呼吸连续。')
write('blockbench/export_jar_family_v51.py',text)
text=(ROOT/'install_release.ps1').read_text(encoding='utf-8').replace('1.8.21','1.8.22').replace('v1821','v1822').replace('1.8.20-before-jar-family','1.8.21-before-breathing').replace('1.8.20','1.8.21')
write('install_release.ps1',text)
print('Prepared release and v51 exporters.')
