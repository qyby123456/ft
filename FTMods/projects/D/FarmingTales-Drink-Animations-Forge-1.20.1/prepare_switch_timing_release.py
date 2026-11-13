from pathlib import Path
ROOT=Path(__file__).resolve().parent
def read(p):return (ROOT/p).read_text(encoding='utf-8')
def write(p,s):(ROOT/p).write_text(s,encoding='utf-8')
notes='''1.8.25：按指定时长统一切换动作：放回 0.45 秒（9 ticks），取出 0.5 秒（10 ticks），酒水、杯装饮品和酱罐相同。完整切换动画合计 0.95 秒。保留果酱食用音量增益 35%；食用、检视、吃完补拿及四种动作变化保持原样。
Blockbench v54 同步更新 draw、put_away、preview_complete，其余轨迹沿用 v53。
'''
s=read('README.md').replace('动画 1.8.24','动画 1.8.25',1)
s=s.replace('Minecraft 1.20.1 / Forge 47.4.10 客户端独立模组。','Minecraft 1.20.1 / Forge 47.4.10 客户端独立模组。\n\n'+notes,1)
write('README.md',s)
write('blockbench/06-使用说明/使用说明-v54.txt',read('blockbench/使用说明-v52.txt').replace('1.8.23','1.8.25')+'\n'+notes)
for kind,folder in [('jam','上古水果果酱'),('drinks','酒水扩展')]:
    s=read(f'blockbench/export_{kind}_v53.py').replace('v53','v54').replace('1.8.24','1.8.25')
    s=s.replace(f"ROOT/f'{folder}-v52/{{item}}-{{label}}-v52.bbmodel'",f"ROOT/f'{folder}-v53/{{item}}-{{label}}-v53.bbmodel'")
    write(f'blockbench/export_{kind}_v54.py',s)
s=read('blockbench/04-导出工具/旧版脚本/export_jar_family_v53.py').replace('v53','v54').replace('1.8.24','1.8.25')
s=s.replace('拿出 0.5 秒、收起 0.3 秒','拿出 0.5 秒、收起 0.45 秒')
write('blockbench/04-导出工具/export_jar_family_v54.py',s)
write('blockbench/04-导出工具/package_timing_v54.py',read('blockbench/04-导出工具/旧版脚本/package_switch_v53.py').replace('v53','v54').replace('1.8.24','1.8.25'))
write('preview/verify_bb_fit_v54.py',read('preview/verify_bb_fit_v53.py').replace('v53','v54'))
s=read('install_release.ps1').replace('1.8.24','1.8.25').replace('v1824','v1825').replace('1.8.23-before-fast-switch','1.8.24-before-switch-tuning').replace('1.8.23','1.8.24')
write('install_release.ps1',s)
s=read('verify_release_v1824.py').replace('1.8.24','1.8.25').replace('1.8.23-before-fast-switch','1.8.24-before-switch-tuning').replace('1.8.23','1.8.24').replace('v53','v54')
s=s.replace("expected={'math/AnimationPlayback','math/AnimationSoundTimeline'}","expected={'math/AnimationPlayback'}")
s=s.replace("'slim':['orange_wine','ancient_fruit_jam_1'],\n    'wide':['orange_wine','ancient_fruit_jam_1']", "'slim':['orange_wine','ancient_fruit_jam_1']")
s=s.replace(".replace('v54','v52')",".replace('v54','v53')")
s=s.replace("clips['draw']['length']==(.5 if jar else .4) and clips['put_away']['length']==.3", "clips['draw']['length']==.5 and clips['put_away']['length']==.45")
s=s.replace('switch_ticks=dict(stow=6,draw=8,jar_draw=10)','switch_ticks=dict(stow=9,draw=10,jar_draw=10)')
s=s.replace('Faster food switching: 6-tick stow, 8-tick regular draw, 10-tick jar draw; fruit jam eating gain +35 percent.', 'User-specified food switching: 9-tick stow and 10-tick draw for every profile; prior fruit jam gain +35 percent preserved.')
s=s.replace('four hidden Forge cases across both arm shapes','two hidden Forge switching cases, with both arm shapes covered by math and export checks')
s=s.replace('only fruit jam cue gain increased','prior fruit jam cue gain preserved')
s=s.replace('both hidden clients.','hidden switching client.')
write('verify_release_v1825.py',s)
print('Prepared 1.8.25: stow 0.45s, draw 0.5s; Blockbench v54.')
