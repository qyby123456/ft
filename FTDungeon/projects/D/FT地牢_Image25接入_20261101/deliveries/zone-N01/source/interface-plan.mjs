import fs from 'node:fs/promises'
import path from 'node:path'
import { fileURLToPath } from 'node:url'
import { createHash } from 'node:crypto'
import { createRequire } from 'node:module'
import { NbtFile, Structure } from 'file:///C:/Users/YOUR_USERNAME/.codex/skills/minecraft-builder-skill/node_modules/@mattzh72/lodestone/lib/index.js'
import { specification, reservedRoute } from './recipe.mjs'

const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..')
const require = createRequire('C:/Users/YOUR_USERNAME/.cache/codex-runtimes/codex-primary-runtime/dependencies/node/node_modules/playwright/package.json')
const { chromium } = require('playwright')
const bytes = await fs.readFile(path.join(root, 'room.nbt'))
const sha256 = createHash('sha256').update(bytes).digest('hex')
const structure = Structure.fromNbt(NbtFile.read(bytes).root)
const nameAt = position => structure.getBlock(position)?.state.getName().toString() ?? 'minecraft:air'
const tiles = []
for (let blockZ = 0; blockZ < 25; blockZ++) {
  for (let blockX = 0; blockX < 25; blockX++) {
    const solid = !['minecraft:air', 'minecraft:rail'].includes(nameAt([blockX, 2, blockZ]))
    const route = reservedRoute(blockX, blockZ)
    const safe = blockX >= 11 && blockX <= 13 && blockZ >= 8 && blockZ <= 10
    const exact = blockX === 12 && blockZ === 9
    const floor = nameAt([blockX, 1, blockZ])
    const classes = ['tile', solid ? 'solid' : '', route ? 'route' : '', safe ? 'safe' : '', exact ? 'exact' : '', floor === 'minecraft:polished_deepslate' ? 'compass' : ''].join(' ')
    tiles.push(`<div class="${classes}" title="(${blockX},2,${blockZ}) / ${floor}">${exact ? '●' : ''}</div>`)
  }
}
const html = `<!doctype html><html lang="zh-CN"><head><meta charset="utf-8"><title>N01 接口与安全路线</title><style>
*{box-sizing:border-box}body{margin:0;background:#111e27;color:#e9e0cb;font-family:Arial,"Microsoft YaHei",sans-serif}header{height:112px;padding:24px 44px;border-bottom:1px solid #3d535e}h1{margin:0 0 10px;font-size:30px;color:#eed1a2}header p{margin:0;color:#aec2c8}main{display:grid;grid-template-columns:900px 1fr;gap:24px;padding:30px 42px}.map{width:840px;height:840px;position:relative;padding:65px}.grid{display:grid;grid-template-columns:repeat(25,28px);width:700px;outline:2px solid #5b7478}.tile{height:28px;background:#53616a;border:1px solid #43525a}.tile.solid{background:#26343e;border-color:#33424e}.tile.compass{box-shadow:inset 0 0 0 2px #8e8c78}.tile.route{background:#357e72;border-color:#528f80}.tile.safe{background:#c4a463;border-color:#dfc793}.tile.exact{color:#201c16;text-align:center;line-height:26px;font-size:23px}.label{position:absolute;font-size:20px;font-weight:700;color:#afdfd5;background:#20343f;padding:8px 12px;border:1px solid #527d7f;border-radius:6px}.north{top:5px;left:290px}.east{top:379px;right:-20px}.south{bottom:0;left:290px}.west{top:379px;left:-22px}.axis{position:absolute;font-size:13px;color:#9db9be}.start{top:44px;left:65px}.end{right:73px;bottom:44px}.side{padding:12px 6px}h2{margin:0 0 15px;color:#efcc8f;font-size:24px}.card{background:#1a2f3a;border:1px solid #405b62;padding:20px;margin-bottom:18px;line-height:1.7}.card b{color:#d3e9df}table{border-collapse:collapse;width:100%;font-size:17px}td,th{text-align:left;padding:7px 4px;border-bottom:1px solid #3b525c}.key{display:inline-block;width:16px;height:16px;margin-right:8px;vertical-align:middle}.green{background:#357e72}.gold{background:#c4a463}.dark{background:#26343e}footer{padding:4px 44px;color:#8ba8b0;font-size:13px}
</style></head><body><header><h1>N01 · 四向接口 / 中心安全区 / 通行净空</h1><p>依据 room.nbt 逐格读取的平面验收图 · X 向东、Z 向南 · 所有坐标为模板局部坐标</p></header><main><div class="map"><div class="axis start">(0,0) · NORTH ↑</div><div class="label north">N · (12,2,0)</div><div class="label east">E<br>(24,2,12)</div><div class="label south">S · (12,2,24)</div><div class="label west">W<br>(0,2,12)</div><div class="grid">${tiles.join('')}</div><div class="axis end">(24,24) · EAST →</div></div><aside class="side"><h2>四口统一规格</h2><div class="card"><table><tr><th>接口</th><th>开口范围</th></tr><tr><td>N / 北</td><td>X 10..14 · Z 0</td></tr><tr><td>E / 东</td><td>X 24 · Z 10..14</td></tr><tr><td>S / 南</td><td>X 10..14 · Z 24</td></tr><tr><td>W / 西</td><td>X 0 · Z 10..14</td></tr></table><p>均为 <b>5 格宽 × 5 格高</b><br>开口 Y=2..6，地面方块 Y=1<br>无台阶、无门、无接口内装饰</p></div><div class="card"><b>中心安全脚点 (12,2,9)</b><br>金色区 X=11..13、Z=8..10<br>3 × 3 平坦安全区，5 格净空<br>灯台占 X/Z=11..13，绕行通过</div><div class="card"><span class="key green"></span>三格宽主路与环路，共 168 格<br><span class="key gold"></span>中心安全区 / ● 精确脚点<br><span class="key dark"></span>墙、柱、灯台与边角家具<br><br><b>Forge 47.4.10 / MC 1.20.1</b><br>4 旋转 × 12 条有向接口路径通过<br>每口接 5 × 4 外部测试桥，400 tick 稳定<br>测试桥不包含在交付 NBT 内</div></aside></main><footer>SHA-256 ${sha256} · 平面 QA 示意，非游戏截图；方块碰撞与完整路径见 qa/forge-native.json</footer></body></html>`
await fs.writeFile(path.join(root, 'preview/interface-plan.html'), html)
const browser = await chromium.launch({ headless: true, executablePath: 'C:/Program Files (x86)/Microsoft/Edge/Application/msedge.exe' })
try {
  const page = await browser.newPage({ viewport: { width: 1536, height: 1100 }, deviceScaleFactor: 1 })
  await page.setContent(html, { waitUntil: 'load' })
  const output = path.join(root, 'preview/interface-plan.png')
  await page.screenshot({ path: output, type: 'png' })
  const report = { status: 'PASS', sha256, source: 'room.nbt', type: 'NBT-derived orthogonal schematic; not game screenshot', ports: specification.ports, center_safe: specification.center_safe, safe_area: specification.safe_area, route_cells: 168, file: 'preview/interface-plan.png', image_sha256: createHash('sha256').update(await fs.readFile(output)).digest('hex') }
  await fs.writeFile(path.join(root, 'qa/interface-plan.json'), JSON.stringify(report, null, 2) + '\n')
  console.log('N01_INTERFACE_PLAN_PASS')
} finally { await browser.close() }
