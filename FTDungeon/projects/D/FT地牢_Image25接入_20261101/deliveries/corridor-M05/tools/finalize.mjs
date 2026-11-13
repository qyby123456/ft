import fs from 'node:fs/promises'
import path from 'node:path'
import { fileURLToPath } from 'node:url'
import { createHash } from 'node:crypto'

const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..')
const digest = bytes => createHash('sha256').update(bytes).digest('hex')
const readJson = async relative => JSON.parse(await fs.readFile(path.join(root, relative), 'utf8'))
const room = await readJson('room.json')
const sha256 = digest(await fs.readFile(path.join(root, 'room.nbt')))
if (room.sha256 !== sha256) throw new Error('Room hash mismatch')
const reports = await Promise.all(['qa/static.json', 'qa/forge-native.json', 'qa/visual.json'].map(readJson))
for (const report of reports) {
  if (report.status !== 'PASS' || report.sha256 !== sha256 || report.id !== room.id) throw new Error('Missing or stale PASS evidence')
}
const [staticQa, nativeQa, visualQa] = reports
if (nativeQa.rotations.length !== 4 || nativeQa.ticks_observed !== 240 || visualQa.views.length !== 9) throw new Error('Incomplete direction/tick evidence')
const log = await fs.readFile(path.join(root, 'qa/forge-run.log'), 'utf8')
if (!log.includes('All 1 required tests passed') || !log.includes('BUILD SUCCESSFUL')) throw new Error('Forge run did not finish successfully')
const qa = {
  id: room.id, status: 'PASS', sha256, reference_sha256: room.reference_sha256,
  size: room.size, data_version: 3465, minecraft: '1.20.1', forge: '47.4.10',
  static_checks: staticQa.checks,
  native: {
    status: 'PASS', method: nativeQa.test, rotations: nativeQa.rotations.map(entry => entry.rotation),
    ticks_observed: 240, forced_neighbor_shape_and_event_updates: true,
    preserved_records_per_rotation: 9120, straight_route_positions_per_rotation: 200,
    reachable_foot_positions_per_rotation: 260, roof_blocks_per_rotation: 1520,
    no_dropped_items: true, no_fluid: true, attachments_can_survive: true,
    complete_properties_registry_validation_and_native_roundtrip: true,
    details: 'qa/forge-native.json', log: 'qa/forge-run.log'
  },
  visual: { status: 'PASS', renderer: visualQa.renderer, source: 'room.nbt', complete_exterior_views: 4, interior_views: 4, optional_cutaway_views: 1, sheet: 'previews/four-directions-sheet.png', details: 'qa/visual.json', cutaway_changes_final_nbt: false },
  scope: { deployed: false, core_source_modified: false, player_saves_modified: false, image_api_called: false, test_world: 'qa/forge-runtime/world (new isolated disposable QA world only)' },
  limits: nativeQa.limits,
  native_log_notes: ['The first isolated startup creates its own config and server.properties; missing-file startup messages are retained in the raw log.', 'The GameTest log warns that a spawned server was temporarily behind schedule; this is not a client FPS/performance measurement.', 'Forge/Gradle deprecation notices and renderer default-pack warnings do not replace the required test-pass assertions.']
}
await fs.writeFile(path.join(root, 'qa.json'), JSON.stringify(qa, null, 2) + '\n')
await fs.writeFile(path.join(root, 'materials.json'), JSON.stringify({ id: room.id, sha256, non_air_blocks: staticQa.non_air_blocks, block_records: staticQa.explicit_records, counts: staticQa.materials, note: 'Bed and door counts are block halves; ten full beds and ten full doors.' }, null, 2) + '\n')
const readme = `# M05 · 灰石铁狱直廊（独立交付）

## 交付结果

- 尺寸 **19 × 12 × 40**（X/Y/Z），长度 **40 格 / 2.5 区块**；中型、一字贯通，沿 Z 轴无转弯、无分岔、无高低阶梯。
- 主路 X=7…11，玩家脚高 Y=2，Z=0…39；**净宽 5 格，最低净高 3 格**。两端均为开放石拱，无门阻挡；双层完整屋顶 Y=10、11。
- 北入口 **[9,2,0]**，内部安全点 **[9,2,3]**，南出口 **[9,2,39]**；坐标为模板局部整数脚位，地板位于 Y=1。
- 两侧各 5 间封闭牢房，保留铁栏、10 扇完整铁门、10 张灰床、10 只空炼药锅与 10 个墙锚镣环；5 盏吊灯由 10 节竖链连接实心屋顶。
- 石砖、少量苔石/裂石、重复阶拱、暖橙柱灯与地脚光带对应参考图。安全处理：以橙色玻璃覆盖菌光体模拟熔岩辉光，**不含真实熔岩、水或火**。

## 文件

- \`room.nbt\`：gzip、Java 1.20.1 / DataVersion 3465、单调色板、9,120 条显式体积记录（其中非空气 5,980 格）。
- \`room.json\`：入口/出口、长度、通路、参考图指纹、接入约束及结构 SHA-256。
- \`preview.png\`：从最终 NBT 渲染的向南主视角；不是概念图或整合包游戏截图。
- \`previews/four-directions-sheet.png\`：北/南/西/东四向完整外景 + 四向内景；同目录附 8 张 1536×1024 原图与 \`cutaway.png\`。
- \`qa.json\`、\`qa/static.json\`、\`qa/forge-native.json\`、\`qa/visual.json\`、\`qa/forge-run.log\`：逐层真实验收证据。
- \`recipe.mjs\` / \`tools/\` / \`viewer/\` / \`native-test/\`：Minecraft Builder 配方、审计与截图脚本、Lodestone 预览及独立 Forge 测试源码。

## 验收范围

**PASS**：最终 NBT 指纹一致；原版方块注册表及完整状态属性合法；Minecraft StructureTemplate 原生加载/往返；真实 \`placeInWorld\` 的 0°、90°、180°、270° 四向放置；强制邻接 shape/事件更新后继续观察 **240 tick**。每向全部 9,120 条状态保持，200 个主路脚位通过真实碰撞检测，260 个可达脚位不进入封闭牢房，1,520 格屋顶核验通过，吊灯/铁链/门/床/墙钩/铁栏稳定，无掉落物和流体。

这是 **独立 Forge 47.4.10 GameTest 服务器验收 + Lodestone 软件渲染**，不冒充整合包客户端实机截图、帧率/多人验收或大厅已接入。测试只新建本目录 \`qa/forge-runtime/world\`；没有打开或修改任何玩家存档，没有部署模组，没有改核心或其他房间。

## 参考与指纹

- 参考：${room.reference}
- PNG SHA-256：\`${room.reference_sha256}\`
- NBT SHA-256：\`${sha256}\`
- 已对照本批 \`README.md\`、\`manifest.json\`、\`SPEC.md\`、\`out/corridor_index.json\` 的 M05 条目：prison-cells / intact / lava-glow / broken-arches。按“完好”状态保留完整壳体，以阶拱轮廓表达图中石拱节奏，不制造通路坍塌或悬空件。

## 复现与预览

1. 在本目录执行 \`node C:/Users/YOUR_USERNAME/.codex/skills/minecraft-builder-skill/scripts/run-build.mjs recipe.mjs --out-dir . --name room --data-version 3465 --compression gzip\`。
2. 执行 \`node tools/audit.mjs\`（结构若改变须重新跑 Forge 和截图，旧证据不再有效）。
3. 执行 \`powershell -ExecutionPolicy Bypass -File tools/run-forge.ps1\`，只启动本目录独立 GameTest；依赖本机已有 ForgeGradle 6.0.54 / Forge 47.4.10 / Java 17 缓存。
4. 执行 \`node tools/serve-preview.mjs\`，打开其输出的 localhost 地址。可选北南东西内/外景与剖切；剖切仅修改内存显示，不写回 NBT。
5. 执行 \`node tools/capture-preview.mjs\` 后执行 \`node tools/finalize.mjs\` 更新证据。脚本使用现有捆绑 Playwright、Sharp 和 Edge，不新装全局依赖。

## 接入注意

本次仅交付候选，不部署或注册。未来接入前必须先检测整个 **19×12×40** 体积是否空闲，包括显式空气区域；不可用结构模板清除已有建筑。端口为北/南，旋转时同时转换端口和方块状态。初次施工成功后写独立持久生成标记；重复进入不得覆盖玩家修改。10 间牢房是封闭装饰空间，无自动战利品、预置生物或功能性监狱逻辑。
`
await fs.writeFile(path.join(root, 'README.md'), readme)
const selected = ['room.nbt', 'room.json', 'preview.png', 'qa.json', 'materials.json', 'README.md', 'recipe.mjs', 'summary.json', 'qa/static.json', 'qa/forge-native.json', 'qa/visual.json', 'qa/forge-run.log', 'qa/native-roundtrip.nbt']
for (const folder of ['previews', 'tools', 'viewer', 'native-test/src', 'native-test/gradle']) {
  const scan = async relative => {
    for (const entry of await fs.readdir(path.join(root, relative), { withFileTypes: true })) {
      const nested = `${relative}/${entry.name}`
      if (entry.isDirectory()) await scan(nested)
      else selected.push(nested)
    }
  }
  await scan(folder)
}
selected.push('native-test/build.gradle', 'native-test/settings.gradle', 'native-test/gradle.properties')
const files = []
for (const relative of selected.sort()) {
  const content = await fs.readFile(path.join(root, relative))
  files.push({ path: relative, bytes: content.length, sha256: digest(content) })
}
await fs.writeFile(path.join(root, 'delivery-manifest.json'), JSON.stringify({ id: room.id, status: 'PASS', sha256, files, excluded_from_portable_bundle: ['qa/forge-runtime (disposable test world and runtime logs)', 'qa/vite-cache', 'native-test/build', 'native-test/.gradle'] }, null, 2) + '\n')
process.stdout.write(`M05_FINAL_PASS sha256=${sha256} files=${files.length}\n`)
