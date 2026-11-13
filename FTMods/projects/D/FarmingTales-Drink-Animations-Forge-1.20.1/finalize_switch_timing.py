from pathlib import Path
ROOT=Path(__file__).resolve().parent
old='按指定时长统一切换动作：放回 0.5 秒（10 ticks），取出 0.8 秒（16 ticks），酒水、杯装饮品和酱罐相同。完整切换动画合计 1.3 秒。'
new='按指定时长统一切换动作：放回 0.45 秒（9 ticks），取出 0.5 秒（10 ticks），酒水、杯装饮品和酱罐相同。完整切换动画合计 0.95 秒。'
for file in ['README.md','blockbench/06-使用说明/使用说明-v54.txt','prepare_switch_timing_release.py']:
    p=ROOT/file;s=p.read_text(encoding='utf-8');assert old in s,file
    s=s.replace(old,new).replace('stow 0.5s, draw 0.8s','stow 0.45s, draw 0.5s')
    p.write_text(s,encoding='utf-8')
p=ROOT/'blockbench/04-导出工具/export_jar_family_v54.py'
p.write_text(p.read_text(encoding='utf-8').replace('拿出 0.8 秒、收起 0.5 秒','拿出 0.5 秒、收起 0.45 秒'),encoding='utf-8')
for file in ['verify_release_v1825.py','prepare_switch_timing_release.py']:
    p=ROOT/file;s=p.read_text(encoding='utf-8')
    s=s.replace("clips['draw']['length']==.8 and clips['put_away']['length']==.5", "clips['draw']['length']==.5 and clips['put_away']['length']==.45")
    s=s.replace('stow=10,draw=16,jar_draw=16','stow=9,draw=10,jar_draw=10')
    s=s.replace('10-tick stow and 16-tick draw','9-tick stow and 10-tick draw')
    s=s.replace('拿出 0.8 秒、收起 0.5 秒','拿出 0.5 秒、收起 0.45 秒')
    p.write_text(s,encoding='utf-8')
print('Final timing: stow 0.45 seconds, draw 0.5 seconds.')
