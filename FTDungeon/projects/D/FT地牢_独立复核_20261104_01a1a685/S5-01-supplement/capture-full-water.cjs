const {chromium} = require('C:/Users/YOUR_USERNAME/.cache/codex-runtimes/codex-primary-runtime/dependencies/node/node_modules/playwright')
const fs = require('node:fs')
const crypto = require('node:crypto')
const path = require('node:path')
const nbt = 'D:/ft工程文件/FT地牢_第5组逐图重做_20261104/01-coral-chamber/nbt/s5-01-coral-chamber-redo.nbt'
const expected = '9450884a348eda61ea55f0f11a8dae87a0f7d5183312b1528d9a519449f6b9a2'
const hash = bytes => crypto.createHash('sha256').update(bytes).digest('hex')
const views = [
  {name:'exterior-southwest',center:[17.5,8.8,17.5],distance:61,pitch:0.62,yaw:0.82},
  {name:'exterior-northeast',center:[17.5,8.8,17.5],distance:61,pitch:0.62,yaw:3.96},
  {name:'interior-west-ring-north',center:[8,6,10],distance:12,pitch:0.12,yaw:0},
  {name:'interior-east-ring-north',center:[26,6,10],distance:12,pitch:0.12,yaw:0},
  {name:'interior-north-gate-south',center:[17,7,13],distance:9,pitch:0.05,yaw:Math.PI},
]
async function main() {
  if(hash(fs.readFileSync(nbt))!==expected) throw Error('Frozen NBT drift')
  const browser = await chromium.launch({headless:true,args:['--enable-webgl','--use-angle=swiftshader','--enable-unsafe-swiftshader']})
  const results = []
  try {
    for(const view of views) {
      const page = await browser.newPage({viewport:{width:1440,height:1100}})
      const errors = []
      page.on('pageerror',error=>errors.push(error.message))
      await page.route('**/src/main.js',async route=>{
        const response = await route.fetch()
        let body = await response.text()
        body = body.replace('this.xRotation = 0.62',`this.xRotation = ${view.pitch}`).replace('this.yRotation = 0.82',`this.yRotation = ${view.yaw}`)
        body = body.replace('const center = vec3.fromValues(size[0] / 2, Math.max(1, size[1] / 2.5), size[2] / 2)',`const center = vec3.fromValues(${view.center.join(',')})`)
        body = body.replace('Math.max(18, Math.max(...size) * 1.9)',String(view.distance))
        await route.fulfill({response,body})
      })
      await page.goto(`http://127.0.0.1:5275/?nbt=%2F__minecraft_builder__%2Fbuild.nbt&name=FULL-WATER-${view.name}`,{waitUntil:'domcontentloaded'})
      await page.waitForSelector('#loading.hidden',{state:'attached',timeout:180000})
      const served = await page.request.get('http://127.0.0.1:5275/__minecraft_builder__/build.nbt')
      if(hash(await served.body())!==expected) throw Error('Served NBT drift')
      await page.waitForTimeout(1000)
      await page.screenshot({path:path.join(__dirname,`${view.name}.png`)})
      results.push({...view,errors,stats:await page.locator('#build-stats').innerText()})
      await page.close()
    }
  } finally {await browser.close()}
  fs.writeFileSync(path.join(__dirname,'full-water-preview.json'),JSON.stringify({nbt_sha256:expected,status:results.every(view=>!view.errors.length)?'PASS_RENDER_ONLY':'FAIL',method:'Unmodified installed Lodestone viewer; transient browser response camera-only overrides. No NBT, geometry, water, material, lighting or installed skill edits.',views:results},null,2))
}
main().catch(error=>{console.error(error);process.exitCode=1})
