from pathlib import Path

ROOT=Path(__file__).absolute().parent
source=(ROOT/'blockbench/export_cocktails_v56.py').read_text(encoding='utf-8')
source=source.replace('v56','v58').replace('1.8.27','1.8.28').replace('鸡尾酒动画-v58','杯装摇晃与杯底修复-v58')
source=source.replace("if profile['id'].startswith('farmingtales:'):continue", "if profile['style']!='CUP':continue")
(ROOT/'blockbench/export_cups_v58.py').write_text(source,encoding='utf-8')
(ROOT/'blockbench/使用说明-v58.txt').write_text('''杯装摇晃与杯底修复 v58 / MOD 1.8.28
作者：紫菜；协议：ARR。
55 款杯装饮品，两种手臂共 110 个项目，替换已有项目中的对应杯装饮品。
检视恢复前后往返摇杯，取消持续向后倾斜；保留液面防溢出限制及轻微相机反馈。
jerk 删除重复杯底；椰子鸡尾酒补上内底并保留外底，均使用原贴图。
取出 0.5 秒、收回 0.45 秒。十三条动画，含 camera、装饰物和液面预览。
从文件安装 cameras.js，关联 camera_first_person。
Blockbench 液面以 6×6 小柱近似；变色材质预览首帧，招牌鸡尾酒预览默认蓝色。
游戏保留动态配方颜色、逐帧变色及液体物理反馈。
''',encoding='utf-8')
verify=(ROOT/'preview/verify_bb_fit_v56.py').read_text(encoding='utf-8')
verify=verify.replace('v56','v58').replace('鸡尾酒动画-v58','杯装摇晃与杯底修复-v58')
(ROOT/'preview/verify_bb_fit_v58.py').write_text(verify,encoding='utf-8')
p=ROOT/'README.md'
s=p.read_text(encoding='utf-8').replace('沿用原贴图，液体和动作参数不变；对应 Blockbench 四个项目在杯底修复-v57.zip。',
    '沿用原贴图；55 款杯装检视取消持续后倾，加强前后往返摇杯，保留液体防溢出限制和轻微镜头反馈。对应两种手臂共 110 份 Blockbench 项目在杯装摇晃与杯底修复-v58.zip；取出 0.5 秒、收回 0.45 秒不变。')
p.write_text(s,encoding='utf-8')
