import hashlib
import html
import json
from pathlib import Path
from PIL import Image
from audit_s8_submission import load

workspace = Path('C:/Users/YOUR_USERNAME/.codex/visualizations/2026/11/04/01a1a685-045b-76d2-aa2b-2a3f24520603')
folder = workspace / 's8-independent-visual-review-20261107-1600'
manifest = load(folder / 'visual-manifest.json')
primary = {'S8-13': 'cutaway.png', 'S8-16': 'cutaway.png', 'S8-19': 'outlook-detail.png', 'S8-20': 'vault-detail.png'}
titles = {'S8-13': '厨房：新梯、两侧平台与灶前踏步', 'S8-16': '蓄水室：井沿踏步、维护桥与壁架', 'S8-19': '瞭望台：高台外晶洞纵深', 'S8-20': '密道藏宝：阶梯岩壳与顶部晶体'}
questions = {'S8-13': '核对新增设施与原厨房比例、材质和原功能关系；梯与平台是否融入架体，灶前踏步是否遮挡主要炉台。', 'S8-16': '核对井口、吊物、前渠与新增维护通路的关系；桥与踏步是否遮掩参考主要特征。', 'S8-19': '重点观察实际站立视点外是否具备近中远非平面洞室及晶体体量；不能仅凭贴墙纹样或换相机宣布原问题关闭。', 'S8-20': '重点观察岩壳、顶部晶体和机关—密道—箱龛的关系；宝箱本来存在，不再重复缺箱疑点。'}
sections = []
images = []
for room in manifest['rooms']:
    identifier = room['id']
    local = folder / identifier
    assert hashlib.sha256((local / 'reference.png').read_bytes()).hexdigest() == room['reference_sha256']
    source = next(local.glob('*.nbt'))
    assert hashlib.sha256(source.read_bytes()).hexdigest() == room['candidate_sha256']
    report = load(local / 'render-evidence.json')
    assert report['sha256'] == room['candidate_sha256']
    rendered = {view['mode'] + '.png' for view in report['views']}
    assert primary[identifier] in rendered
    for image in sorted(local.glob('*.png')):
        assert image.name == 'reference.png' or image.name in rendered
        with Image.open(image) as decoded:
            decoded.verify()
        with Image.open(image) as decoded:
            assert min(decoded.size) >= 900
            dimensions = list(decoded.size)
        images.append(dict(path=str(image), sha256=hashlib.sha256(image.read_bytes()).hexdigest(), size=dimensions))
    supplemental = ''.join('<figure><img loading=lazy src=' + identifier + '/' + name + ' alt=' + identifier + '><figcaption>' + html.escape(name) + '</figcaption></figure>' for name in sorted(rendered) if name != primary[identifier])
    sections.append('<section id=' + identifier + '><h2>' + html.escape(identifier + ' ' + titles[identifier]) + '</h2><p>' + html.escape(questions[identifier]) + '</p><div class=pair><figure><img src=' + identifier + '/reference.png alt=原始参考图><figcaption>原始参考概念图</figcaption></figure><figure><img src=' + identifier + '/' + primary[identifier] + ' alt=当前候选真实渲染><figcaption>当前候选真实渲染：' + primary[identifier] + '</figcaption></figure></div><p class=hash>房间源SHA-256：' + room['candidate_sha256'] + '</p><p><a href=' + identifier + '/reference-prompt.txt>原始参考提示词</a> · <a href=' + identifier + '/render-evidence.json>源匹配渲染记录</a></p><details><summary>展开完整结构、室内与其余细节图</summary><div class=pair>' + supplemental + '</div></details></section>')
document = '<!doctype html><html lang=zh-CN><meta charset=utf-8><meta name=viewport content=width=device-width,initial-scale=1><title>S8视觉复核待办</title><style>body{font:16px/1.6 system-ui,sans-serif;margin:0;background:#f5f4f8;color:#201d28}main{max-width:1560px;margin:auto;padding:28px}h1{font-size:28px}section{background:white;border:1px solid #ddd7e7;border-radius:12px;padding:22px;margin:28px 0}.notice{background:#fff2cf;padding:18px;border-left:4px solid #a46a00}.pair{display:grid;grid-template-columns:repeat(2,minmax(0,1fr));gap:16px}figure{margin:0}img{display:block;width:100%;height:auto;background:#181623}figcaption{padding:8px 0;font-size:14px}.hash{overflow-wrap:anywhere;font:13px/1.7 monospace;color:#605469}summary{cursor:pointer;padding:12px 0}a{color:#63409d}@media(max-width:850px){main{padding:12px}.pair{grid-template-columns:1fr}section{padding:12px}}</style><main><h1>S8当前候选：视觉对照待核</h1><p class=notice><strong>当前状态：未通过视觉验收，整组accepted=false，总进度7/8。</strong> 图像来源、解码和候选哈希已检查；看图工具只返回成功但未提供图像内容，因此这里没有编造视觉观察或自动批准。技术通过不能代替参考特征、比例与场景风格判断。</p><p>合并SHA-256：<code>' + manifest['source_sha256'] + '</code></p><p>13/16为本轮独立重渲染，19/20源与旧4d候选对应房间字节相同，沿用精确哈希绑定渲染。剖切仅修改显示副本；完整NBT保留墙顶。其余六房及旧观察记录见独立验收目录。</p><nav>' + ' · '.join('<a href=#' + identifier + '>' + identifier + '</a>' for identifier in primary) + '</nav>' + ''.join(sections) + '<footer><p>有限隔离测试，不是正式部署、客户端通关、战斗系统或生成器完成证明。</p><p><a href=visual-manifest.json>视觉来源清单</a> · <a href=image-integrity.json>图片完整性清单</a></p></footer></main></html>'
(folder / 'index.html').write_text(document, encoding='utf-8')
result = dict(accepted=False, visual_inspected=False, source_sha256=manifest['source_sha256'], images=images, html_sha256=hashlib.sha256((folder / 'index.html').read_bytes()).hexdigest(), checked='PNG decode/dimensions, reference hashes and current candidate render hashes only; no perceptual approval')
(folder / 'image-integrity.json').write_text(json.dumps(result, ensure_ascii=False, indent=2), encoding='utf-8')
print(folder / 'index.html')
print('SOURCE_BOUND_IMAGES', len(images), 'VISUAL_APPROVAL', False)
