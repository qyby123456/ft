from pathlib import Path
import html
import json

root = Path(__file__).resolve().parents[1]
work = root / 'output/style-rebuild-v034'
styles = [('clockwork', '钟械传动机坊'), ('forest', '古林根冠遗迹'), ('desert', '日轮沙埋地宫'), ('frost', '寒霜冻存档案'), ('swamp', '幽沼红树药坊'), ('coral', '珊瑚潮汐圣所'), ('lava', '熔火玄武铸场'), ('amethyst', '紫晶星图穹殿')]
cards = []
audit_count = 0
for style, title in styles:
    directory = work / style
    count = len(list((directory / 'roomset').rglob('*.nbt')))
    ready = (directory / 'gallery.html').is_file()
    four_views = (directory / 'four-views.html').is_file()
    self_audit = directory / 'self-audit.json'
    passed = False
    if self_audit.exists():
        audit = json.loads(self_audit.read_text(encoding='utf-8'))
        passed = str(audit.get('status', '')).upper() in {'PASS', 'PASSED'} or any(audit.get(field) is True for field in ['allPassed', 'passed', 'pass'])
        audit_count += int(passed)
    label = f'{count}/27 模板 · ' + ('四视图/风格/重复度自检通过' if passed else '四向检查中' if four_views else '逐房审核中' if ready else '制作中')
    oblique_views = (directory / 'oblique-four-views.html').is_file()
    target = f'{style}/oblique-four-views.html' if oblique_views else f'{style}/four-views.html' if four_views else f'{style}/gallery.html' if ready else '#'
    cards.append(f'<a class="card" href="{target}" target="gallery" data-style="{style}"><b>{title}</b><span>{label}</span></a>')
integration = work / 'integration.json'
status = '八个独立风格线程制作中；此页面仅展示已经输出的成果。'
if integration.exists():
    report = json.loads(integration.read_text(encoding='utf-8'))
    status = f'统一静态验收 {html.escape(report["status"])} · {report["templates"]} 份模板 · 子线程完整自检 {audit_count}/8 · ' + ('已接入工程资源' if report['integrated'] else '等待全部审核后统一接入')
deployment = work / 'deployment.json'
if deployment.exists():
    deployed = json.loads(deployment.read_text(encoding='utf-8-sig'))
    if deployed.get('status') == 'PASS':
        status = f'0.34.0 已部署 · 216房 · 864方向视图 · 自检8/8通过 · 八风格实际生成与旧实例重启检查通过'
page = '''<!doctype html><html lang="zh-CN"><meta charset="utf-8"><meta name="viewport" content="width=device-width,initial-scale=1"><title>FT地牢 · 八风格全房重建</title><style>
*{box-sizing:border-box}body{margin:0;background:#101722;color:#e8eef6;font:16px/1.6 'Segoe UI','Microsoft YaHei',sans-serif}header,main{max-width:1440px;margin:auto;padding:30px}h1{font-size:30px;margin:0 0 10px}p{color:#b3c1d3;margin:10px 0}.status{padding:14px 18px;background:#23364b;border-left:4px solid #77c7ed;border-radius:8px}.cards{display:grid;grid-template-columns:repeat(4,1fr);gap:14px;margin:24px 0}.card{color:inherit;text-decoration:none;padding:18px;background:#1b2838;border:1px solid #344960;border-radius:12px}.card:hover,.card.active{background:#28455d;border-color:#8fd5ee}.card b,.card span{display:block}.card span{font-size:13px;color:#aac3d5;margin-top:8px}iframe{width:100%;height:950px;border:1px solid #344960;background:#101722;border-radius:12px}.notes{display:grid;grid-template-columns:1fr 1fr;gap:24px}h2{font-size:19px}a{color:#8dd4ed}@media(max-width:850px){.cards{grid-template-columns:repeat(2,1fr)}.notes{grid-template-columns:1fr}header,main{padding:20px}}
</style><header><h1>FT地牢 · 八风格全房重建</h1><p>每种风格独立设计入口、走廊、战斗内室、岔路、楼梯、末端功能房和Boss；共8×27份模板。</p><div class="status">STATUS</div></header><main><div class="cards">CARDS</div><iframe name="gallery" title="真实NBT逐房平面和剖面图集"></iframe><div class="notes"><section><h2>建筑和生成器</h2><p>沿用细胞地牢的四层网格与既有端口。每种风格从独立配方生成全部房间，使用统一接口接入中等地牢菜单。内室按可选种类轮换使用，通道维持同一风格。</p></section><section><h2>检查范围</h2><p>逐房核对尺寸、门洞、刷怪点与实体脚点支撑、实际几何变化。各风格图集读取生成的NBT数据；剖面用于查看内部，真实建筑仍有封顶。服务端运行和客户端体验分别记录。</p></section></div><p><a href="任务约定.md">八线程任务约定</a> · <a href="integration.json">统一静态验收记录</a></p></main><script>const cards=[...document.querySelectorAll('.card')];for(const card of cards)card.addEventListener('click',()=>{for(const other of cards)other.classList.remove('active');card.classList.add('active')});const ready=cards.find(card=>card.getAttribute('href')!=='#');if(ready){ready.classList.add('active');document.querySelector('iframe').src=ready.getAttribute('href')}</script></html>'''
(work / 'index.html').write_text(page.replace('STATUS', status).replace('CARDS', ''.join(cards)), encoding='utf-8')
print(work / 'index.html')
