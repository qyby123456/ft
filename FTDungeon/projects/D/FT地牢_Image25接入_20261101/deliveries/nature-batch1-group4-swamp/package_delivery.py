import base64
import hashlib
import html
import json
from pathlib import Path
from urllib.parse import urlencode
import urllib.request

ROOT = Path(__file__).resolve().parent


def main():
    manifest = json.loads((ROOT / 'manifest.json').read_text(encoding='utf-8'))
    native = json.loads((ROOT / 'qa/native-review.json').read_text(encoding='utf-8'))
    assert native['status'] == 'PASS' and len(native['rooms']) == 9
    previews = []
    rows = []
    materials = {}
    for room in manifest['rooms']:
        folder = ROOT / 'incoming' / room['id']
        for required in ('room.nbt', 'room.json', 'preview.png', 'qa.json', 'materials.json', 'interior-four-views.png', 'exterior.png'):
            assert (folder / required).is_file(), str(folder / required)
        nbt_url = 'data:application/octet-stream;base64,' + base64.b64encode((folder / 'room.nbt').read_bytes()).decode('ascii')
        url = 'http://127.0.0.1:19440/?' + urlencode({'nbt': nbt_url, 'name': room['id'] + ' ' + room['name']})
        payload = urllib.request.urlopen(nbt_url, timeout=10).read()
        assert hashlib.sha256(payload).hexdigest() == room['sha256']
        previews.append({'id': room['id'], 'url': url, 'embedded_nbt_sha256_matches': True})
        for name, count in json.loads((folder / 'materials.json').read_text(encoding='utf-8')).items():
            materials[name] = materials.get(name, 0) + count
        faces = ', '.join(exit['face'] for exit in room['exits']) or '无（终点）'
        rows.append('| ' + ' | '.join([room['id'], room['name'], '×'.join(map(str, room['size'])), str(room['entrance']), faces]) + ' |')
    assert urllib.request.urlopen('http://127.0.0.1:19440', timeout=10).status == 200
    (ROOT / 'lodestone-previews.json').write_text(json.dumps({'server': 'http://127.0.0.1:19440', 'gallery': 'http://127.0.0.1:19439/preview.html', 'scope': 'Official skill Lodestone viewer HTTP reachable; every embedded data-URL NBT hash verified against actual file. Not gameplay evidence.', 'rooms': previews}, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')
    links = ''.join(f'<a href="{html.escape(preview["url"])}" target="viewer">{html.escape(room["id"] + " · " + room["name"])}</a>' for room, preview in zip(manifest['rooms'], previews))
    gallery = '<!doctype html><html lang="zh-CN"><meta charset="utf-8"><title>第4组 · 沼泽湿地 NBT</title><style>body{margin:0;background:#101a20;color:#ede8d6;font:16px system-ui}header{padding:16px}nav{display:flex;flex-wrap:wrap;gap:8px;padding:0 16px 16px}a{color:#c0dfaa;padding:9px;background:#24352e;border-radius:6px}iframe{width:100%;height:78vh;border:0}</style><header><h2>首批第4组 · 9个实际 NBT 模块</h2><p>交互预览来自真实结构文件；外部显式连接点，不运行原模组拼图或处理器。拖动旋转、滚轮缩放；未部署游戏存档。</p></header><nav>' + links + '</nav><iframe name="viewer" src="' + html.escape(previews[0]['url']) + '"></iframe></html>'
    (ROOT / 'preview.html').write_text(gallery, encoding='utf-8')
    (ROOT / 'materials-total.json').write_text(json.dumps(dict(sorted(materials.items())), ensure_ascii=False, indent=2) + '\n', encoding='utf-8')
    text = '''# 首批第4组 · 沼泽湿地结构候选

本线程独立完成 9 个 Minecraft Java 1.20.1 vanilla structure gzip NBT，非 AI 图片。第10张仅为布局参考，不冒充第10个可放置模块。未触发生图 API，0 张半写入图片；未改 Mod JAR、配置或玩家存档。

## 权威输入与沿用/改造

- 解包总结：`D:/ft工程文件/结构参考_20261103/README.md`、`D:/ft工程文件/结构参考_20261103/LEARNING_NOTES.md`、`D:/ft工程文件/结构参考_20261103/SAMPLE_CATALOG.md`、`D:/ft工程文件/结构参考_20261103/EVIDENCE.md`。
- 原始证据来自 `D:/ft工程文件/结构参考_20261103/unpacked/better_strongholds/data/betterstrongholds/structures/`，回查同库 `nbt-details/better_strongholds/betterstrongholds/`。
- `source-evidence.json` 逐间列出真实 NBT、详情绝对路径与 SHA-256、原尺寸、原 palette 统计、原始完整 jigsaw 状态/NBT、功能沿用与明确改造。`read_evidence.py` 实际解析原件；`recipe.mjs` 直接从证据读取输出尺寸。
- 大房间/庭院以 `rooms/junction_md.nbt` 19×16×19 为尺度；中房间/前厅以 `rooms/armoury_md.nbt` 13×8×13 为尺度；小房间以 `rooms/crypt_sm.nbt` 7×8×7 为尺度；长厅以 `rooms/prison_lg.nbt` 19×8×19 的轴向通路和侧区为参考；宝库以 `rooms/treasure_room_lg.nbt` 19×16×19 为尺度。
- 直廊以 `hallways/hallway_2.nbt` 5×8×5 为重复单元，三段长加湿地边距形成9×8×19；转廊以 `hallways/hallway_turn.nbt` 5×8×5 为转向/墙口语义参考，扩大为13×8×13。不是对原件一比一复刻。
- 保留源结构 stone_bricks 基材；mossy_stone_bricks、mud_bricks、dark_oak_log、浅水、睡莲和蛙灯为本组主题改造，不能冒称原件材料。图片只负责湿地氛围，不作为尺寸或接口权威。
- 原 YUNG 拼图、门洞/墙封口池、处理器占位色、宝藏子插槽、奖励和刷怪机制不移植。本批用 `room.json` 显式连接点替代 jigsaw；新增/封闭/居中接口均在逐间证据中声明，不能说与原模组运行时直接兼容。

## 模块清单（XYZ，方块）

| ID | 名称 | 尺寸 | 北入口脚位 | 其它出口 |
| --- | --- | --- | --- | --- |
''' + '\n'.join(rows) + '''

## 接口与放置契约

- 第4组差异化详见 `DESIGN_IDENTITY.md`：01/04/08为浅水隔离的多折3格木栈道；08包含实际楼梯可达的抬高观察台。`qa/differentiation-audit.json` 记录与其它组候选快照的旋转/镜像干路掩码比对，非最终语义拓扑判定。

- 原点为模板包围盒最小XYZ；北为-z、南+z、东+x、西-x。所有入口脚位y=2，入口/出口均3宽×3高，地板为完整方块，校验全部开口脚位到北入口可达。
- `room.json` 包含尺寸、入口、内部安全点、出口、连接点、开口尺寸、占地轮廓、源证据和当前NBT哈希。各门正外侧仍需要连接模块提供干燥地面及净空，不能悬空接门。
- 旋转必须同时变换方块状态和全部坐标/面向。顺时针90°时 `(x,y,z) -> (D-1-z,y,x)`，尺寸 `(W,H,D)->(D,H,W)`，north->east->south->west。
- 除庭院外，最终NBT保留完整屋顶。剖视移除屋顶及近侧外墙仅发生在图片渲染；NBT不被裁切。
- 浅水有底与四周阻水，主路保持干燥；灯具/箱子有支撑，箱子空且无战利品表。树叶persistent防衰败。
- 本批稀疏NBT不会清空未记录坐标。放置前必须检查整个包围盒及外侧接口净空，碰到占用拒绝而非盲目清理。候选ID尚未占用正式槽位。

## 验证与预览

- `qa/static-review.json`：9/9 PASS，覆盖版本/尺寸/边界、禁用块、空容器、基础/屋顶、水密封、支撑、安全点、每个连接口3×3干燥连通。
- `qa/native-review.json`：9/9 PASS，Minecraft 1.20.1真实注册表/方块状态、原生碰撞路径、方块实体加载与StructureTemplate往返；也检查所有声明出口。
- 不是原生世界放置/邻居tick稳定性或玩家实机游玩证据；这些项目明确为NOT_RUN，需统一接入侧继续验收。
- 每间 `preview.png`、`interior-north/south/west/east.png`、`interior-four-views.png` 和 `exterior.png` 均从该间最终NBT生成；软件预览采用原版纹理及近似水/树叶染色，不是游戏截图。
- `preview.html` 是九间Lodestone交互预览入口，`lodestone-previews.json` 给出逐间URL；官方查看器HTTP可访问，每间以data-URL嵌入真实NBT，逐项解码校验SHA-256。Lodestone端口19440和静态画廊端口19439只绑定127.0.0.1。
- 每间 `materials.json` 和总表 `materials-total.json` 为实际NBT非空气方块清单。
- `qa/lodestone.json` 和每间 `lodestone-preview.png`（若已生成）为官方Lodestone查看器实际加载/截图证据；仍非游戏内截图。

## 布局说明

第10张整体图只作为组合氛围参考。本候选尚不提供已测试的整体连接施工图：北入口前厅 -> 直廊 -> 轴向大厅；大厅侧接中房间；大型三口大厅及庭院可承担分支；转廊接小室/宝库终点。实际原点、旋转、连接廊长度、碰撞排除、最终槽位由统一接入侧另行规划和验证，不把此文字当作已放置路线。

## 复现与边界

先运行 `read_evidence.py`，再用已安装minecraft-builder-skill的 `scripts/run-build.mjs` 逐个生成（进程环境SWAMP_ROOM=1..9，--name room --data-version 3465）。随后 `validate.py --native --render`。启动官方 `scripts/preview.mjs --nbt <第一间room.nbt> --port 19440 --no-open` 后运行 `package_delivery.py`。

仅写本组独立交付目录；不覆盖其它组、不部署、不声明已接入。原件仅本地研究，不推定再分发许可。
'''
    (ROOT / 'README.md').write_text(text, encoding='utf-8')
    files = {str(path.relative_to(ROOT)): hashlib.sha256(path.read_bytes()).hexdigest() for path in ROOT.rglob('*') if path.is_file() and path.name != 'checksums.json' and '__pycache__' not in str(path) and 'native-classes' not in str(path)}
    (ROOT / 'checksums.json').write_text(json.dumps(files, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')
    print('PACKAGED 9 modules; 9 embedded Lodestone NBT hashes verified; viewer HTTP reachable.')


if __name__ == '__main__':
    main()
