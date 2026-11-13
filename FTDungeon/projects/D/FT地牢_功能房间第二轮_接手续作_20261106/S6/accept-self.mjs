import fs from 'node:fs/promises'
import path from 'node:path'
import {createHash} from 'node:crypto'
import assert from 'node:assert/strict'
const root=path.resolve(process.argv[2])
const group=path.dirname(root)
const read=async relative=>JSON.parse(await fs.readFile(path.join(root,relative),'utf8'))
const summary=await read('summary.json')
const design=await read('design.json')
const sha256=createHash('sha256').update(await fs.readFile(summary.nbtPath)).digest('hex')
const native=(await read('qa/independent-native-frozen.json'))[0]
const routes=(await read('qa/independent-routes.json'))[0]
const staticReport=await read('qa/static.json')
const render=await read('qa/render.json')
for(const report of [native,routes]) assert.equal(report.sha256,sha256)
for(const report of [staticReport,render]) assert.equal(report.source_sha256,sha256)
assert.equal(native.registry_properties,'PASS')
assert.equal(native.template_roundtrip_block_count,'PASS')
assert.equal(native.survival_failures.length,0)
assert.equal(staticReport.failures.length,0)
assert(routes.start_clear&&routes.targets.every(target=>target.standing_clear&&target.reachable))
const note=process.argv[3]
assert(note,'Supply actual visual inspection note after opening preview image')
const result={id:design.id,source_sha256:sha256,status:'self_checked_pending_independent_review',independent_acceptance:false,visually_inspected:['cutaway.png','complete.png'],visual_note:note,candidate_history:[{sha256,changes:'Current candidate; prior failures, if any, retained in qa/history.'}],actual_tests:['Lodestone NBT roundtrip','Vanilla 1.20.1 registry and properties','Vanilla StructureTemplate load/save count','Static supported decoration canSurvive','Native 0.6x1.8 collision route sampling','Full-cube lava enclosure and conservative combustible margin','Real Lodestone rendered source hash binding'],not_tested:['Live world placement','Fluid and random fire ticks','Survival gameplay','Independent acceptance'],differences:design.differences}
await fs.writeFile(path.join(root,'qa/self-review.json'),JSON.stringify(result,null,2))
const progress=JSON.parse(await fs.readFile(path.join(group,'progress.json'),'utf8'))
progress.completed=[...new Set([...(progress.completed??progress.revised_self_checked??[]),design.id])]
progress.revised_self_checked=progress.completed.filter(id=>id!=='S6-99')
progress.inherited_only=(progress.inherited_only??[]).filter(id=>id!==design.id)
progress.current=design.id
progress.last_candidate_sha256=sha256
progress.next=Number(design.id.split('-')[1])<20?'View next reference image and prompt before building.':'All ten rooms self-checked; design new connected assembly.'
await fs.writeFile(path.join(group,'progress.json'),JSON.stringify(progress,null,2))
await fs.writeFile(path.join(root,'README.md'),`# ${design.id}\n\n状态：自检完成，待独立复核；不是生存通关验收。\n\nNBT：${path.basename(summary.nbtPath)}；尺寸 ${summary.size.join(' × ')}；SHA256 ${sha256}。\n\n## 真实接口\n${design.ports.map(port=>`- ${port.side}: 脚点 ${port.feet.join(',')}，净宽${port.width}，净高${port.clearHeight}`).join('\n')}\n\n## 视觉核对\n${note}\n\n## 与概念图差异\n${design.differences.map(detail=>'- '+detail).join('\n')}\n\n## 复现\n使用本组lib.mjs与recipe.mjs，技能scripts/run-build.mjs，参数 --data-version 3465。随后运行本组audit.mjs、native.ps1、render.cjs。所有QA哈希绑定当前NBT；原工具报告文件名含independent仅为兼容，不代表独立验收。\n`)
console.log(design.id,'self-review recorded',sha256)
