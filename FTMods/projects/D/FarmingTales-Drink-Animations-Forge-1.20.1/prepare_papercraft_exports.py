from pathlib import Path
ROOT=Path(__file__).absolute().parent
s=(ROOT/'blockbench/04-导出工具/export_cocktails_v56.py').read_text(encoding='utf-8')
s=s.replace('v56','v60').replace('1.8.27','1.8.30').replace('鸡尾酒动画-v60','纸鸢酒水动画-v60')
s=s.replace("if profile['id'].startswith('farmingtales:'):continue","if not profile['id'].startswith('papercraft_magic_decoration:'):continue")
(ROOT/'blockbench/04-导出工具/旧版脚本/export_papercraft_v60.py').write_text(s,encoding='utf-8')
(ROOT/'blockbench/使用说明-v60.txt').write_text('''纸鸢酒水动画 v60 / MOD 1.8.30
作者：紫菜；协议：ARR。
16 款饮品，每款 Steve 和纤细手臂各一份，共 32 个项目。
普通杯装沿用原摇杯检视；guang_s 使用易拉罐开罐动画。
外交官咖啡先吃原模型巧克力再喝；三款咖啡在食用时掏出原勺子，八次小口舀饮后收起。
勺子在手持、取出和检视时隐藏；液面逐次下降，杯底留少量余液。
原模型与贴图完整内嵌，手持时移除托盘和桌面摆件；原模组不变。
包含十三条动作、camera、液体和装饰物轨迹。closure 分组在特殊咖啡中代表勺子或巧克力。
取出 0.5 秒、放回 0.45 秒。普通右键保留原版；V 食用、G 检视，可改键。
安装 cameras.js 并关联 camera_first_person；液面预览为 6×6 柱体近似。
''',encoding='utf-8')
s=(ROOT/'preview/verify_bb_fit_v56.py').read_text(encoding='utf-8')
s=s.replace('v56','v60').replace('鸡尾酒动画-v60','纸鸢酒水动画-v60')
s=s.replace("drinks/{item}.json","drinks/papercraft_{item}.json")
(ROOT/'preview/verify_bb_fit_v60.py').write_text(s,encoding='utf-8')
print('Prepared 32-project Paper Kite export.')
