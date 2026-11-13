import fs from 'node:fs'
import path from 'node:path'
import {fileURLToPath} from 'node:url'
import {createHash} from 'node:crypto'
import assert from 'node:assert/strict'
const root=path.dirname(fileURLToPath(import.meta.url))
const read=relative=>JSON.parse(fs.readFileSync(path.join(root,relative),'utf8'))
const hash=filename=>createHash('sha256').update(fs.readFileSync(filename)).digest('hex')
const write=(relative,value)=>fs.writeFileSync(path.join(root,relative),JSON.stringify(value,null,2)+'\n')
const live=read('assembly/access/runtime-result.json')
assert.equal(live.status,'PASS')
assert(fs.readFileSync(path.join(root,'qa/runtime/access-run.log'),'utf8').includes('BUILD SUCCESSFUL'))
const contract=read('assembly/access/contract.json')
assert.equal(live.source_sha256,contract.source_sha256)
const pack=path.join(root,'assembly/access/datapack')
const testedFiles=[]
function bind(directory) {
  for(const entry of fs.readdirSync(directory,{withFileTypes:true})) {
    const filename=path.join(directory,entry.name)
    if(entry.isDirectory()) bind(filename)
    else {
      const relative=path.relative(pack,filename).replaceAll('\\','/')
      if(relative.startsWith('data/fts6_access/')) {
        const loaded=path.join(root,'qa/runtime/src/main/resources',relative)
        assert.equal(hash(filename),hash(loaded),'Pack differs from tested resource '+relative)
      }
      testedFiles.push({path:relative,sha256:hash(filename),bytes:fs.statSync(filename).size})
    }
  }
}
bind(pack)
const zip=path.join(root,'assembly/access/fts6-access-mc1.20.1.zip')
assert(fs.existsSync(zip),'Create distributable zip first')
const packaged=read('assembly/access/package.json')
assert.equal(packaged.sha256,hash(zip))
assert.equal(packaged.zip_roundtrip,'PASS')
assert.deepEqual(packaged.files.map(record=>record.path).sort(),testedFiles.map(record=>record.path).sort())
for(const record of packaged.files) assert.equal(hash(path.join(pack,record.path)),record.sha256)
assert.deepEqual(JSON.parse(fs.readFileSync(path.join(pack,'data/minecraft/tags/functions/load.json'),'utf8')).values,['fts6_access:load'])
assert.deepEqual(JSON.parse(fs.readFileSync(path.join(pack,'data/minecraft/tags/functions/tick.json'),'utf8')).values,['fts6_access:tick'])
await import('./finalize-review2.mjs')
const ready=read('ready-for-review.json')
assert.equal(ready.assembly.sha256,live.source_sha256)
ready.updated=new Date().toISOString()
ready.external_access={method:contract.method,datapack:zip,datapack_sha256:hash(zip),contract:path.join(root,'assembly/access/contract.json'),runtime:path.join(root,'assembly/access/runtime-result.json'),review_status:'IMPLEMENTED_AND_SELF_TESTED_PENDING_INDEPENDENT_REVIEW',physical_barrier_aperture:false,barriers_preserved:47553}
ready.open_review_items=['独立方复核本次授权传送接入包、隔离测试与剩余自然刻证据；制作方不自行放行。']
ready.not_verified=ready.not_verified.filter(detail=>!detail.startsWith('外部世界到封闭屏障内'))
ready.not_verified.push('授权传送接入的真实网络客户端流程、实际场地道路衔接、多实例或旋转部署。')
write('ready-for-review.json',ready)
const capture=path.join(root,'assembly/access/after-access-zone.nbt')
write('qa/external-access.json',{...contract,source_sha256:ready.assembly.sha256,implementation_status:'IMPLEMENTED_SELF_TESTED_PENDING_INDEPENDENT',runtime:live,post_tick_capture:capture,post_tick_sha256:hash(capture),pack_files:testedFiles,zip:zip,zip_sha256:hash(zip),native_install_tested:true,source_barrier_positions_preserved:47553,source_block_types_verified:252421,independent_acceptance:false,formal_instance_modified:false})
const audit=read('qa/delivery-audit.json');audit.external_access='Authorized native triggers tested in isolated Forge GameTestServer';audit.not_verified=ready.not_verified;write('qa/delivery-audit.json',audit)
const progress=read('progress.json');progress.status='revised_candidates_and_external_access_pending_independent_review';progress.next='Freeze refreshed manifest; independently review the implemented access datapack and isolation evidence. No formal deployment.';progress.not_verified=ready.not_verified;write('progress.json',progress)
let readme=fs.readFileSync(path.join(root,'README.md'),'utf8')
readme=readme.replace('审查意见3的外部步行接入仍未关闭。','外部接入现以授权传送数据包实现并通过隔离自检，待独立复核。')
readme=readme.replace('status.mjs与finalize.mjs均转入该脚本。','finalize-access.mjs在其基础上核接入包并更新最终清单；status.mjs与finalize.mjs均转入finalize-access.mjs。')
readme=readme.replace('内部入口脚点为2,4,36，其与外部之间仍有封闭屏障。未认证步行外部入口，未实现传送机制；不得据此部署或宣布整组放行。','内部脚点和封闭屏障保持；外侧接入台通过授权trigger进入，内侧入口trigger返回。实际安装命令、逐玩家授权、坐标公式及复现见assembly/access/README.md，授权/拒绝/往返/普通移动防穿越的隔离结果见assembly/access/runtime-result.json。该方案不在屏障上开步行洞；尚未独立放行。')
readme=readme.replace(/- 外部世界到封闭屏障内的步行接入[^\n]*\n/,'')
fs.writeFileSync(path.join(root,'README.md'),readme+'\n外部接入更新：assembly/access/fts6-access-mc1.20.1.zip可安装数据包包含20个原样分片、接入台安装函数、授权进入及自愿返回。主候选SHA256保持不变。当前仅单实例、未旋转overworld；入口marker的区块保持加载。\n')
fs.appendFileSync(path.join(root,'qa/revision-response.md'),'\n## 外部接入后续补充（当前有效）\n\n上文第3项中“外部接入未实现”的状态已被本补充替代：现交付assembly/access授权传送数据包，主屏障不打洞。native place-template实际安装20片后逐记录核252421个方块类型及47553个屏障位置。权限0玩家通过trigger执行授权入场与返回；未授权与离开激活半径的伪造请求被拒绝；撤销授权后仍可正常返回，普通Entity.move不能穿过西侧屏障。详qa/external-access.json及assembly/access/runtime-result.json、README.md。此为制作方隔离自检，待独立方确认是否满足接入要求。\n')
const excludedNames=new Set(['history','history-firstpass','.gradle','build','world-isolated'])
const artifacts=[];const excluded=[]
function visit(directory) {
  for(const entry of fs.readdirSync(directory,{withFileTypes:true}).sort((left,right)=>left.name.localeCompare(right.name))) {
    const filename=path.join(directory,entry.name);const relative=path.relative(root,filename).replaceAll('\\','/')
    if(entry.isDirectory()) {if(excludedNames.has(entry.name)) excluded.push({path:relative,reason:entry.name.startsWith('history')?'Superseded evidence':'Regenerable build/cache or isolated world; sources, logs and captures included'});else visit(filename)}
    else if(relative!=='delivery-manifest.json') artifacts.push({path:relative,absolute:filename,sha256:hash(filename),bytes:fs.statSync(filename).size})
  }
}
visit(root)
const manifest=read('delivery-manifest.json')
write('delivery-manifest.json',{...manifest,created:ready.updated,artifacts,excluded,open_review_items:ready.open_review_items})
for(const record of artifacts) assert.equal(hash(record.absolute),record.sha256)
console.log(JSON.stringify({access:'PASS',source_sha256:ready.assembly.sha256,artifacts:artifacts.length,zip_sha256:hash(zip),manifest_sha256:hash(path.join(root,'delivery-manifest.json')),accepted:false}))
