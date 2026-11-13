import fs from 'node:fs/promises'
import path from 'node:path'
import { fileURLToPath } from 'node:url'
import { createHash } from 'node:crypto'

const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..')
const digest = bytes => createHash('sha256').update(bytes).digest('hex')
const read = async relative => JSON.parse(await fs.readFile(path.join(root, relative), 'utf8'))
const room = await read('room.json')
const sha256 = digest(await fs.readFile(path.join(root, 'room.nbt')))
const staticQa = await read('qa/static.json')
const nativeQa = await read('qa/forge-native.json')
const visualQa = await read('qa/visual.json')
for (const report of [room, staticQa, nativeQa, visualQa]) if (report.id !== 'zone-H03' || report.sha256 !== sha256) throw new Error('Stale identity or SHA-256')
if (staticQa.status !== 'PASS' || nativeQa.status !== 'PASS' || nativeQa.rotations.length !== 4 || nativeQa.ticks_observed !== 240) throw new Error('Incomplete static/native evidence')
const log = await fs.readFile(path.join(root, 'qa/forge-run.log'), 'utf8')
if (!log.includes('All 1 required tests passed') || !log.includes('BUILD SUCCESSFUL')) throw new Error('Forge did not finish successfully')
if (visualQa.views.length !== 10 || visualQa.page_errors.length) throw new Error('Incomplete visual evidence')
for (const view of visualQa.views) {
  if (digest(await fs.readFile(path.join(root, view.file))) !== view.sha256) throw new Error(`Screenshot hash changed ${view.file}`)
}
visualQa.status = 'PASS'
visualQa.manual_review = {
  status: 'PASS', method: 'Agent visual inspection of four-directions-sheet.png and full-resolution cutaway-north.png',
  findings: ['Four pillars are grounded and join ceiling beams; central and side routes remain open.', 'Altar, stairs, free-standing candle pedestals and two chained candle trays are visible across opposing sections.', 'Four full-shell views retain roof and walls, with the sole north doorway.', 'Two uncut interior views show the actual ceiling, candle trays, aisle emblem and altar/entrance.'],
  limitations: ['Lodestone software renders the purple wall banners as simple purple faces; native Forge independently confirms two gold pattern records in all eleven banner block entities.', 'Stone and gold trims are block-scale interpretations of the concept, not a pixel-identical reproduction.']
}
await fs.writeFile(path.join(root, 'qa/visual.json'), JSON.stringify(visualQa, null, 2) + '\n')
const qa = {
  id: room.id, status: 'PASS', sha256, reference_sha256: room.reference_sha256, size: room.size,
  minecraft: '1.20.1', data_version: 3465, forge: '47.4.10',
  static: { status: 'PASS', checks: staticQa.checks, explicit_records: staticQa.explicit_records, non_air_blocks: staticQa.non_air_blocks, details: 'qa/static.json' },
  native: { status: 'PASS', method: nativeQa.test, rotations: nativeQa.rotations, ticks_observed: 240, forced_neighbor_shape_and_event_updates: true, details: 'qa/forge-native.json', log: 'qa/forge-run.log', roundtrip: 'qa/native-roundtrip.nbt' },
  visual: { status: 'PASS', renderer: visualQa.renderer, source: 'room.nbt', internal_sections: 4, complete_shells: 4, intact_interiors: 2, sheet: 'previews/four-directions-sheet.png', details: 'qa/visual.json', cutaway_changes_nbt: false },
  scope: { deployed: false, official_files_modified: false, other_deliveries_modified: false, player_saves_modified: false, image_api_called: false, test_world: 'qa/forge-runtime/world: newly created isolated GameTest world only' },
  limits: [...nativeQa.limits, ...visualQa.manual_review.limitations],
  log_notes: ['First-run missing server.properties and corrected local config are retained in the raw log.', 'Forge version-check timeout, obsolete API warning and transient server behind-schedule warning are retained; required test and build both passed. These do not constitute client performance measurements.']
}
await fs.writeFile(path.join(root, 'qa.json'), JSON.stringify(qa, null, 2) + '\n')
await fs.writeFile(path.join(root, 'materials.json'), JSON.stringify({ id: room.id, sha256, explicit_records: staticQa.explicit_records, non_air_blocks: staticQa.non_air_blocks, individual_candles: staticQa.individual_candles, counts: staticQa.materials }, null, 2) + '\n')
const readme = `# H03 · 烛仪大厅 · 独立交付

四件套：room.nbt、room.json、preview.png、qa.json。仅写 deliveries/zone-H03；未部署、未接入大厅、未修改正式文件或其他房间，未打开玩家存档，未调用生图接口。

- 尺寸 29×16×35，Java 1.20.1 / DataVersion 3465，gzip、单调色板，16,240 条显式体积记录，非空气 ${staticQa.non_air_blocks} 格。
- 北入口脚位 [14,2,0]，内部安全点 [14,2,4]，朝北；入口净宽 5、净高 4，主路 X=12…16、Z=0…26 净高至少 3。坐标均为模板局部玩家脚位。
- 四根落地石柱与四边梁连接双层完整屋顶 Y=14/15；后部两排朝南台阶连接祭台安全落脚 [14,4,29]。
- 中央黑白仪式纹章、紫金地面镶嵌、11 面紫金纹章旗、36 组共 140 支点燃蜡烛、2 座十字烛盘与 10 格屋顶锚链。所有蜡烛均落在实心支撑上；祭台有完整底座。
- 地毯用齐平方块镶嵌表达，避免细地毯改变脚位；不加入烟雾实体、明火、流体、自动战利品或战斗机制。

验收：静态 QA PASS；Forge 47.4.10 独立 GameTest 四向 0/90/180/270° 实际 placeInWorld，强制邻接形状及事件更新后观察 240 tick，各向全部 16,240 条状态保持，135 个主路脚位通过真实碰撞，各向 ${nativeQa.rotations[0].reachable_ground_foot_positions} 个地面脚位可达，2,030 格屋顶核验，全部 canSurvive、旗帜 NBT、蜡烛与铁链支撑稳定，无掉落物。FakePlayer 使用真实 Entity.move 每向 580 个 0.1 格移动步，验证北入口到祭台上行与原路返回。

视觉证据：previews/four-directions-sheet.png 汇集四向内部剖视、四向完整外壳和两张完整屋顶内景。所有 PNG 由最终 room.nbt 经 Lodestone 渲染；剖切只修改内存视图，不修改 NBT。软件渲染不是整合包客户端截图。Lodestone 墙旗只显示简化紫面，Forge 已逐面验证两条金色图案记录。

参考图：${room.reference}

已核对批次 README.md、docs/manifest.json、docs/verify.json；逐条指纹证据见 qa/reference.json。

- PNG SHA-256：${room.reference_sha256}
- NBT SHA-256：${sha256}

复现：

1. node C:/Users/YOUR_USERNAME/.codex/skills/minecraft-builder-skill/scripts/run-build.mjs recipe.mjs --out-dir . --name room --data-version 3465 --compression gzip
2. node tools/audit.mjs
3. powershell -ExecutionPolicy Bypass -File tools/run-forge.ps1
4. node tools/serve-preview.mjs 5183（如端口占用会输出实际端口）
5. node tools/capture-preview.mjs http://127.0.0.1:实际端口/，重新视觉检查后 node tools/finalize.mjs

未来接入必须先检查整个 29×16×35 体积为空，包括显式空气位置；不能用模板清空既有建筑。旋转时同时变换入口/安全点/方块状态。首次成功放置后写独立持久生成标记，再次进入不覆盖玩家修改。独立服务器验收不等同于正式整合包客户端、多人、性能或核心连接路线验收。

工程证据：recipe.mjs、tools/、viewer/、native-test/src/；完整状态验收、日志、原生往返 NBT、材料和交付指纹清单均保留。qa/forge-runtime、qa/vite-cache、native-test/build 与 native-test/.gradle 是本房间隔离测试缓存，不是待部署资源。
`
await fs.writeFile(path.join(root, 'README.md'), readme)
const selected = ['room.nbt', 'room.json', 'preview.png', 'qa.json', 'materials.json', 'README.md', 'recipe.mjs', 'summary.json', 'qa/static.json', 'qa/reference.json', 'qa/visual.json', 'qa/forge-native.json', 'qa/forge-run.log', 'qa/native-roundtrip.nbt', 'native-test/build.gradle', 'native-test/settings.gradle', 'native-test/gradle.properties']
for (const folder of ['tools', 'viewer', 'previews', 'native-test/src', 'native-test/gradle']) {
  const scan = async relative => {
    for (const entry of await fs.readdir(path.join(root, relative), { withFileTypes: true })) {
      const nested = `${relative}/${entry.name}`
      if (entry.isDirectory()) await scan(nested)
      else selected.push(nested)
    }
  }
  await scan(folder)
}
const files = []
for (const relative of selected.sort()) {
  const content = await fs.readFile(path.join(root, relative))
  files.push({ path: relative, bytes: content.length, sha256: digest(content) })
}
await fs.writeFile(path.join(root, 'delivery-manifest.json'), JSON.stringify({ id: room.id, status: 'PASS', sha256, files, excluded: ['qa/forge-runtime', 'qa/vite-cache', 'native-test/build', 'native-test/.gradle'] }, null, 2) + '\n')
process.stdout.write(`H03_FINAL_PASS ${sha256} evidence_files=${files.length}\n`)
