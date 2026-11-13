import fs from 'node:fs'
import path from 'node:path'
import {fileURLToPath} from 'node:url'
import {createHash} from 'node:crypto'
const root=path.dirname(fileURLToPath(import.meta.url))
const rooms=fs.readdirSync(root).filter(name=>/^\d\d-/.test(name)).sort().map(folder=>{
  const directory=path.join(root,folder)
  const summary=JSON.parse(fs.readFileSync(path.join(directory,'summary.json')))
  const bytes=fs.readFileSync(summary.nbtPath)
  const sha256=createHash('sha256').update(bytes).digest('hex')
  const review=JSON.parse(fs.readFileSync(path.join(directory,'qa/self-review.json')))
  const revised=fs.existsSync(path.join(directory,'qa/history/inherited/recipe.mjs'))
  return {id:'S6-'+folder.slice(0,2),directory,candidate:summary.nbtPath,sha256,revised,self_review_matches:review.source_sha256===sha256,status:revised&&review.source_sha256===sha256?'REVISED_SELF_CHECKED_PENDING_INDEPENDENT':revised?'REVISION_IN_PROGRESS':'INHERITED_REQUIRES_REVISION',independent_acceptance:false}
})
const submission={group:'S6',stage:'PARTIAL_REVISION_REVIEW_ONLY',updated:new Date().toISOString(),rooms,assembly_status:'INHERITED_STALE_DO_NOT_ACCEPT_OR_DEPLOY',independent_acceptance:false,not_verified:['Native live placement and interactions','Fluid/fire/random ticks','Full gameplay','Final revised assembly and <=48 slices','Independent final visual acceptance'],inherited_source:path.join(root,'inheritance.json')}
fs.writeFileSync(path.join(root,'ready-for-review.json'),JSON.stringify(submission,null,2))
const revised=rooms.filter(room=>room.status==='REVISED_SELF_CHECKED_PENDING_INDEPENDENT')
const mergedDirectory=path.join(root,'assembly')
const mergedReviewFile=path.join(mergedDirectory,'qa/self-review.json')
if(revised.length===10&&fs.existsSync(mergedReviewFile)&&fs.existsSync(path.join(mergedDirectory,'qa/split-payload-verification.json'))) {
  const mergedSummary=JSON.parse(fs.readFileSync(path.join(mergedDirectory,'summary.json')))
  const mergedSha=hashFile(mergedSummary.nbtPath)
  const mergedReview=JSON.parse(fs.readFileSync(mergedReviewFile))
  const split=JSON.parse(fs.readFileSync(path.join(mergedDirectory,'qa/split-payload-verification.json')))
  if(mergedReview.source_sha256===mergedSha&&split.source_sha256===mergedSha) {
    submission.stage='FINAL_REVISED_CANDIDATES_PENDING_INDEPENDENT_REVIEW'
    submission.assembly_status='SELF_CHECKED_NOT_INDEPENDENTLY_ACCEPTED'
    submission.assembly={candidate:mergedSummary.nbtPath,sha256:mergedSha,size:mergedSummary.size,slices:split.pieces.length,slice_manifest:path.join(mergedDirectory,'qa/split-payload-verification.json'),block_entities:split.pieces.reduce((sum,piece)=>sum+piece.block_entities,0),routes:58,barriers:split.barrier_union_shell.actual}
    submission.not_verified=['Native live placement and all six cell door interactions','Fluid/fire/random ticks, water/lava neighbor updates','Ladder climbing, jump obstacles and all workstation interactions','Survival/combat gameplay','Independent final visual acceptance and group release']
    fs.writeFileSync(path.join(root,'ready-for-review.json'),JSON.stringify(submission,null,2))
  }
}
fs.writeFileSync(path.join(root,'README.md'),`# S6 火山熔岩第二轮接手续作\n\n仅新S6为写入范围，旧S6只读。继承文件哈希见inheritance.json，旧QA仅历史；未将复制的PASS当新验收。\n\n当前整改并自检完成：${revised.map(room=>room.id).join('、')}。逐房候选与状态见ready-for-review.json；该清单仅供阶段独立复核，不构成整组交付或放行。\n\n旧assembly随着源整改已经失效，禁止部署。16—20等剩余房间完成后重建合并、48格以内分片并反拼实体载荷。20仍是藏宝密道。\n\n每房recipe.mjs由技能run-build.mjs导出，audit.mjs、native.ps1及render.cjs在新房目录运行，DataVersion3465。全非空气支撑另由只读IndependentAllSupportAudit在新房qa运行。所有报告必须匹配当前NBT哈希。\n\n真实Lodestone截图与近景保存在各房，剖切只影响渲染副本。门控操作、液体自然刻和生存交互尚未验证；无正式saves/mods/config/KubeJS写入，无付费生图。\n`)
console.log(JSON.stringify(rooms.map(room=>({id:room.id,status:room.status}))))
function hashFile(filename) {return createHash('sha256').update(fs.readFileSync(filename)).digest('hex')}
