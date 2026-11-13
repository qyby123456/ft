import hashlib
import json
from datetime import datetime
from pathlib import Path

review = Path('D:/ft工程文件/FT地牢_第二轮独立验收_20261106')
workspace = Path('C:/Users/YOUR_USERNAME/.codex/visualizations/2026/11/04/01a1a685-045b-76d2-aa2b-2a3f24520603')
gallery = workspace / 's8-independent-visual-review-20261107-1600'
candidate = workspace / 'preserved-review-snapshots/S8-interaction-revision-20261107-1355'
manifest = json.loads((gallery / 'visual-manifest.json').read_text(encoding='utf-8'))
observed = {
    'S8-13': ['S8-13-comparison.png', 'S8-13/shelf-ladder-access.png', 'S8-13/hearth-step-access.png'],
    'S8-16': ['S8-16-comparison.png', 'S8-16/well-step-bridge-access.png', 'S8-16/maintenance-step-access.png'],
    'S8-19': ['S8-19-comparison.png', 'S8-19/cutaway.png', 'S8-19/interior.png'],
    'S8-20': ['S8-20-comparison.png', 'S8-20/complete.png', 'S8-20/cutaway.png', 'S8-20/reverse.png'],
}
findings = {
    'S8-13': dict(status='PASS_SCOPED_VISUAL', observation='实际图可见灶体、吊锅、左右烟熏炉、备餐台和长桌凳；新梯沿木架立柱上行，两侧木平台与原货架材质一致。灶前深板岩踏步局部附着在炉台正面，未遮掉炉口或左右通路。与概念图有块级简化，但厨房主特征及紫晶/方解石/深板岩配色可辨，本次新增设施视觉项关闭。'),
    'S8-16': dict(status='PASS_SCOPED_VISUAL', observation='实际图可见中央苔石蓄水池、横木、链条吊锅、前方浅渠和后方维护台；新踏步贴井沿，短木桥在池边而非覆盖全井，后方壁架台阶与深板岩维护台连续。开放水面和吊物仍清楚，原房主要特征未被新增通路掩盖，本次新增设施视觉项关闭。'),
    'S8-19': dict(status='PASS_SCOPED_VISUAL', observation='高台外实际站立视图可见两侧壁不同距离的晶体、洞室顶面凸凹和层级地面；反向室内图尤其清楚地显示近景独立晶柱、中景侧柱及远端高台。不是只有贴墙纹样，随房源交付的近中远实体体量和可读纵深已存在。整体边界仍规则，但原先高台紧对平墙的核心问题已关闭。'),
    'S8-20': dict(status='REQUEST_CHANGES', observation='已实际看到箱体、金块、壁龛、两侧厚通道墙、台阶入口和零散晶体，旧缺箱疑点继续不成立。完整结构图仍呈大面积平坦矩形屋顶；前角新增紫晶体为宽扁方块叠层，屋顶中央未见参考图那种高低错落的晶体冠。剖切显示主空间由几块等宽直立方盒组成，外围岩壳局部台阶不足以读成参考的非规则阶梯岩体。要求局部补足上部岩壳起伏和成组不同高度晶体，不要求重做其余九房。'),
}
rooms = []
for room in manifest['rooms']:
    identifier = room['id']
    directory = gallery / identifier
    source = next(directory.glob('*.nbt'))
    assert hashlib.sha256(source.read_bytes()).hexdigest() == room['candidate_sha256']
    assert hashlib.sha256((directory / 'reference.png').read_bytes()).hexdigest() == room['reference_sha256']
    render = json.loads((directory / 'render-evidence.json').read_text(encoding='utf-8'))
    assert render['sha256'] == room['candidate_sha256']
    images = [dict(path=str(gallery / filename), sha256=hashlib.sha256((gallery / filename).read_bytes()).hexdigest()) for filename in observed[identifier]]
    rooms.append(dict(id=identifier, source_sha256=room['candidate_sha256'], reference_sha256=room['reference_sha256'], visual_inspected=True, observed_images=images, **findings[identifier]))
report = dict(group='S8', accepted=False, status='REQUEST_CHANGES_S8_20_ROCK_SHELL_CRYSTAL_CROWN', reviewed_at=datetime.now().isoformat(), source_sha256=manifest['source_sha256'], image_delivery_restored=True, previous_blocker_resolved=True, actual_observation_method='functions.view_image returned image content actually inspected in this resumed goal turn', rooms=rooms, preserved_technical_scope=str(candidate / 'completion-audit.json'), remaining=['S8-20局部补阶梯岩壳与不同高度顶部晶体冠；保留现有功能物、目标、20接口、门控、箱和原九房', '对精确新版重新冻结源与配方/合并/分片，核查改动相关技术证据并实际看新内外图；旧技术证据只在未变范围继承', '完整八组最终审计后方可写ACCEPTANCE并完成目标'], production_modified=False)
(review / 'S8-ACTUAL-VISUAL-REVIEW-20261108.json').write_text(json.dumps(report, ensure_ascii=False, indent=2), encoding='utf-8')
lines = ['# S8实际视觉复核：只退修20号局部', '', '本轮图像通道已恢复。已实际查看13张源绑定对照/细节图，不能再将旧无图像限制写作当前阻塞。总进度仍7/8，S8 accepted=false。', '', '当前合并源SHA-256：' + report['source_sha256'], '']
for room in rooms:
    lines.extend(['## ' + room['id'] + ' ' + room['status'], '', room['observation'], '', '对应房间SHA-256：' + room['source_sha256'], ''])
lines.extend(['## 局部修订约束', '', '仅在新版本目录修改20号岩壳和顶部晶体轮廓，保留原箱龛、台阶、拉杆、通路、家具、原目标与声明接口。13/16新增设施及19新版保持精确源哈希，其余六房不动。不要降低接口净空或把展示剖切当结构原样。', '', '旧6988技术证据仍然有效地证明旧源；不能自动转为新源全部通过。新版需要精确差异、配方重建、完整合并/分片/屏障/载荷及改动相关原生与世界证据。若改动仅装饰且不触及原路线/交互/流体空间，应给出坐标和依赖范围证明后继承，不无故重跑所有已完成测试。', '', '原图、13张实际观察图、来源哈希和结论均见同名JSON；源图未覆写。'])
(review / 'S8-ACTUAL-VISUAL-REVIEW-20261108.md').write_text(chr(10).join(lines), encoding='utf-8')
print(json.dumps(dict(status=report['status'], actual_images=sum(len(room['observed_images']) for room in rooms), passed_rooms=3, revision_rooms=['S8-20'], image_delivery_restored=True), ensure_ascii=False))
