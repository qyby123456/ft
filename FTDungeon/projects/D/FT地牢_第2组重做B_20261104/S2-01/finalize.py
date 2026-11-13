import hashlib
import json
from pathlib import Path
import urllib.request

ROOT = Path(__file__).resolve().parent
TARGET = ROOT / 'incoming/G2B-01'


def save(path, value):
    path.write_text(json.dumps(value, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')


def main():
    metadata = json.loads((TARGET / 'room.json').read_text(encoding='utf-8'))
    native = json.loads((ROOT / 'qa/native-review.json').read_text(encoding='utf-8'))
    digest = hashlib.sha256((TARGET / 'room.nbt').read_bytes()).hexdigest()
    assert native['status'] == 'PASS' and native['rooms'][0]['sha256'] == digest == metadata['sha256']
    assert native['rooms'][0]['audited_targets'] == 9
    urls = {'full': 'http://127.0.0.1:5542/?nbt=%2F__minecraft_builder__%2Fbuild.nbt&name=G2B-01', 'cutaway_preview_only': 'http://127.0.0.1:5543/?nbt=%2F__minecraft_builder__%2Fbuild.nbt&name=PREVIEW_ONLY'}
    for port, path in [(5542, TARGET / 'room.nbt'), (5543, ROOT / 'preview-only/preview-only-not-for-placement.nbt')]:
        with urllib.request.urlopen(f'http://127.0.0.1:{port}/__minecraft_builder__/build.nbt', timeout=10) as response:
            assert hashlib.sha256(response.read()).hexdigest() == hashlib.sha256(path.read_bytes()).hexdigest()
    assembly = {'status': 'SINGLE_TEMPLATE_NO_SPLIT_NEEDED', 'nbt': str(TARGET / 'room.nbt'), 'size_xyz': [25, 14, 25], 'template_origin': [0, 0, 0], 'origin_semantics': 'Northwest bottom corner of bounding box; +X east, +Y up, +Z south', 'world_origin': None, 'bounds_local_inclusive': [[0, 0, 0], [24, 13, 24]], 'chunks': 'Not restricted to 16x16; crosses multiple chunks, actual count depends on world origin alignment', 'assembly_order': [{'step': 1, 'template': 'incoming/G2B-01/room.nbt', 'offset_from_origin': [0, 0, 0]}], 'connection_anchors': metadata['connections'], 'placement_preconditions': ['Coordinator assigns origin/rotation and slot.', 'Check whole volume for occupied blocks, entities, fluids and world border, including explicit air.', 'Reject unsafe overlap; do not clear or overwrite player structures.', 'Never place preview-only cutaway NBT.'], 'scale_from_reference': {'method': 'Visual proportion estimate, not exact image measurement', 'exterior_width': 25, 'interior_clear_span': 21, 'roof_y': 13, 'altar_base': [11, 11], 'altar_top': [7, 7], 'altar_feet_y': 4, 'doors': '5-wide by 4-high lower opening; stepped arch narrows toward crown at y7', 'comparison': 'Preserves low central plinth relative to tall surrounding walls; no one-chunk shrink', 'verification': 'Walk paths use actual native block collisions; visual match is a human-review candidate, not a perfect-replica claim'}}
    save(ROOT / 'assembly.json', assembly)
    qa = json.loads((TARGET / 'qa.json').read_text(encoding='utf-8'))
    qa.update({'status': 'SELF_CHECK_PASS_REGISTRATION_PENDING', 'native': native['rooms'][0], 'lodestone_served_hash_verified': True, 'world_placement': 'NOT_RUN', 'registration': 'PENDING_COORDINATOR_ACCEPTANCE'})
    save(TARGET / 'qa.json', qa)
    metadata['lodestone_previews'] = urls
    metadata['assembly'] = str(ROOT / 'assembly.json')
    save(TARGET / 'room.json', metadata)
    save(ROOT / 'registration.json', {'status': 'READY_FOR_SINGLE_IMAGE_REGISTRATION_NOT_ACCEPTED_YET', 'group': 2, 'image': 1, 'room_id': 'G2B-01', 'reference': metadata['reference'], 'reference_sha256': metadata['reference_sha256'], 'room': str(TARGET / 'room.nbt'), 'sha256': digest, 'size': metadata['size'], 'native': 'PASS: 9 collision-path targets and NBT roundtrip', 'previews': urls, 'assembly': str(ROOT / 'assembly.json'), 'duplicate_self_check': str(ROOT / 'duplicate-self-check.json'), 'next_image': 'S2-02 NOT_STARTED; current image must be registered and accepted first', 'deployment': 'NOT_DEPLOYED'})
    lines = ['# 第2组重做B：仅第1张交付', '', '状态：单张自检完成，等待协调登记验收；未开始、预生成或领取第2张。封存旧目录未改动，新构型从零编写。', '', '## 结构与尺寸', '', '- 实际结构：incoming/G2B-01/room.nbt；25×14×25，DataVersion 3465，8750条含空气记录。', '- 跨区块而非压进单一区块；室内跨度约21格，中央11×11三级台基、7×7日轮纹台面、四角矮灯。', '- 层高、跨度与比例来自参考图视觉估算，不冒充精确测量。保留完整屋顶，北入口为安全平接；细碎陶罐用原版块级近似。', '- 北入口[12,1,0]；南[12,1,24]、西[0,1,12]、东[24,1,12]，均为局部脚部坐标；下部5宽4高，拱顶逐级收窄。', '- 原点为包围盒西北底角[0,0,0]，世界坐标未分配；单个NBT足够，不需拆分。原点、锚点和拼装顺序见assembly.json。', '', '## 校验边界', '', '- PASS：实际Minecraft 1.20.1 NBT解码、方块注册表/属性校验、StructureTemplate读写往返。', '- PASS：原生碰撞寻路覆盖4个接口、祭坛顶及4个环行象限共9目标；498个可达位置。', '- PASS：完整屋顶、沙堆/灯/植被支撑、无刷怪/战利品/命令块；实际NBT渲染出内外四向图。', '- 重复度自检：对本地第3/7组18份实际模板进行不含材质的20×20障碍图四旋转比较，见duplicate-self-check.json。不是全项目唯一性认证，也不以相似率直接判定抄用。', '- 未执行：Forge世界放置、区块稳定性、实机游玩；未部署。登记验收仍待协调线程完成。', '', '## 预览', '', f'- [Lodestone完整结构]({urls["full"]})', f'- [Lodestone剖切，仅预览不可放置]({urls["cutaway_preview_only"]})', '', 'preview.png为实际NBT软件材质渲染，不是AI图或游戏截图。Lodestone服务的响应已逐一核对NBT哈希；preview-only目录严禁进入接入池。', '', '## 工作边界', '', '本轮只制作S2-01，不跨组、不批量套模板。参考图不是交付结构；ANALYSIS.md记录构图/路线/材质/地标解析。当前图正式验收前禁止继续下一张。', '', '旧结构和路线模板未作为构建输入；只复用通用NBT工具、材质渲染和注册表校验设施。首次原生检测因默认工作目录触及游戏日志遇到文件锁，已在本交付目录隔离工作目录重跑PASS（qa/native-run-isolated.log），未删除被占用日志。', '', f'结构SHA-256：{digest}', '']
    (ROOT / 'README.md').write_text('\n'.join(lines), encoding='utf-8')
    paths = sorted(path for path in ROOT.rglob('*') if path.is_file() and path.suffix in {'.nbt', '.png', '.json', '.py', '.mjs', '.java', '.md'})
    (ROOT / 'SHA256SUMS.txt').write_text('\n'.join(hashlib.sha256(path.read_bytes()).hexdigest() + '  ' + str(path.relative_to(ROOT)) for path in paths) + '\n', encoding='utf-8')
    print('G2B-01 SELF_CHECK_COMPLETE; REGISTRATION_PENDING; S2-02 NOT_STARTED')


if __name__ == '__main__':
    main()
