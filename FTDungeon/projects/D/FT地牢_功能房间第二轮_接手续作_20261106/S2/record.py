import hashlib
import json
from pathlib import Path
import sys

ROOT = Path(__file__).resolve().parent


def digest(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()


def main():
    folder = ROOT / sys.argv[1]
    design = json.loads((folder / 'design.json').read_text(encoding='utf-8'))
    check = json.loads((folder / 'qa/self-check.json').read_text(encoding='utf-8'))
    render = json.loads((folder / 'qa/render.json').read_text(encoding='utf-8'))
    source = json.loads((ROOT / 'references.json').read_text(encoding='utf-8'))
    reference = next(entry for entry in source['references'] if entry['id'] == design['id'])
    assert digest(Path(reference['image_path'])) == reference['sha256']
    sha256 = digest(folder / design['nbt'])
    assert check['source_sha256'] == render['source_sha256'] == sha256
    review = {'source_sha256': sha256, 'reference_sha256': reference['sha256'], 'reference_prompt_sha256': digest(Path(reference['prompt_path'])), 'visual_review': sys.argv[2], 'status': 'PRODUCER_SELF_CHECK_ONLY_PENDING_INDEPENDENT_REVIEW', 'views': {view['id']: digest(folder / (view['id']+'.png')) for view in render['views']}, 'differences': design['differences']}
    (folder / 'qa/visual-review.json').write_text(json.dumps(review, ensure_ascii=False, indent=2), encoding='utf-8')
    lines = [f"# {design['id']} 制作方交付", '', f"参考实际观察：{design['reference_observed']}", '', f"NBT：{design['nbt']}；尺寸 {design['size']}；Java 1.20.1 / DataVersion 3465。", f"SHA-256：`{sha256}`", '', '## 实际接口（局部整数脚点；人体中心 x/z +0.5）']
    lines.extend(f"- {port['side']}：{port['feet']}，净宽 {port['width']} / 净高 {port['clearHeight']}" for port in design['ports'])
    lines.extend(['', '## 与图差异', *('- '+item for item in design['differences']), '', '## 可复跑', f"- node C:/Users/YOUR_USERNAME/.codex/skills/minecraft-builder-skill/scripts/run-build.mjs \"{folder / 'recipe.mjs'}\" --out-dir \"{folder}\" --json", f"- C:/Users/YOUR_USERNAME/.cache/codex-runtimes/codex-primary-runtime/dependencies/python/python.exe \"{ROOT / 'check.py'}\" {folder.name}", f"- node \"{ROOT / 'render.cjs'}\" \"{folder}\"", '', '真实 Lodestone 完整 / 剖切 / 内景预览见 PNG；剖切只在显示副本执行，NBT完整。', 'qa/inventory.json 包含方块与实体载荷。qa/commands.json 记录原生调用；每次 Java -Xmx1536M，在当前 qa 工作目录运行，未运行共享脚本。', '制作方自检：原版注册属性、模板读取、所选脆弱装饰支撑、0.6×1.8 离线碰撞路线、空库存、静态风险。', '未执行真实世界放置、邻居/随机/液体 tick、动态照明、战斗或生存通关；不等于独立验收。', '', '## 视觉检查', sys.argv[2]])
    (folder / 'README.md').write_text('\n'.join(lines)+'\n', encoding='utf-8')
    records = []
    for item in sorted(ROOT.glob('*/qa/visual-review.json')):
        value = json.loads(item.read_text(encoding='utf-8'))
        records.append({'folder': item.parent.parent.name, 'candidate_sha256': value['source_sha256'], 'status': value['status']})
    progress = {'style': 'S2', 'completed_self_checked': len(records), 'total': 10, 'current': design['id'], 'rooms': records, 'next': 'Next reference after current preview and self-check; assemble only after ten rooms.', 'independent_acceptance': 'NOT_RUN', 'writes_scope': str(ROOT), 'no_saves_mods_writes': True}
    (ROOT / 'progress.json').write_text(json.dumps(progress, ensure_ascii=False, indent=2), encoding='utf-8')
    (ROOT / 'README.md').write_text('# S2 砂岩荒漠 第二轮功能房间\n\n逐图查看→独立制作→原生自检→真实Lodestone预览→看图记录。\n\n制作方自检完成：'+str(len(records))+'/10；均待独立复核。\n\n'+'\n'.join('- '+entry['folder']+' / '+entry['candidate_sha256'] for entry in records)+'\n\n完整流程与接口在各房间 README.md、design.json。未部署正式世界，未改 saves/mods。十张齐备后才合并。\n', encoding='utf-8')
    print(design['id'], 'recorded', len(records))


if __name__ == '__main__':
    main()
