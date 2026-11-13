const {chromium} = require('C:/Users/YOUR_USERNAME/.cache/codex-runtimes/codex-primary-runtime/dependencies/node/node_modules/playwright')
const fs = require('node:fs')
const crypto = require('node:crypto')
const path = require('node:path')
const root = __dirname
const nbt = path.join(root, 'nbt/s5-07-coral-gate-vestibule.nbt')
const hash = bytes => crypto.createHash('sha256').update(bytes).digest('hex')
const views = [
  {name:'full-water-exterior-southeast',port:5517,center:[15.5,8,17.5],distance:64,pitch:0.62,yaw:-0.82},
  {name:'full-water-exterior-northwest',port:5517,center:[15.5,8,17.5],distance:64,pitch:0.62,yaw:2.32},
  {name:'full-water-interior-axis',port:5517,center:[15,8,10],distance:19,pitch:0.12,yaw:0},
  {name:'full-water-interior-door',port:5517,center:[15,7,27],distance:14,pitch:0.08,yaw:Math.PI},
  {name:'water-hidden-GEOMETRY-ONLY',port:5518,center:[15.5,8,17.5],distance:64,pitch:0.62,yaw:-0.82},
]
async function main() {
  fs.mkdirSync(path.join(root,'qa'),{recursive:true})
  const expected = hash(fs.readFileSync(nbt))
  const browser = await chromium.launch({headless:true,args:['--enable-webgl','--use-angle=swiftshader','--enable-unsafe-swiftshader']})
  const results = []
  try {
    for(const view of views) {
      const page = await browser.newPage({viewport:{width:1440,height:1100}})
      const errors = []
      page.on('pageerror',error=>errors.push(error.message))
      await page.route('**/src/main.js',async route=>{
        const response=await route.fetch()
        let body=await response.text()
        body=body.replace('this.xRotation = 0.62',`this.xRotation = ${view.pitch}`).replace('this.yRotation = 0.82',`this.yRotation = ${view.yaw}`)
        body=body.replace('const center = vec3.fromValues(size[0] / 2, Math.max(1, size[1] / 2.5), size[2] / 2)',`const center = vec3.fromValues(${view.center.join(',')})`).replace('Math.max(18, Math.max(...size) * 1.9)',String(view.distance))
        await route.fulfill({response,body})
      })
      await page.goto(`http://127.0.0.1:${view.port}/?nbt=%2F__minecraft_builder__%2Fbuild.nbt&name=S5-07-${view.name}`,{waitUntil:'domcontentloaded'})
      await page.waitForSelector('#loading.hidden',{state:'attached',timeout:180000})
      const served=await page.request.get(`http://127.0.0.1:${view.port}/__minecraft_builder__/build.nbt`)
      const servedHash=hash(await served.body())
      if(view.port===5517&&servedHash!==expected)throw Error('Preview source mismatch')
      await page.waitForTimeout(1000)
      await page.screenshot({path:path.join(root,`qa/${view.name}.png`)})
      results.push({...view,errors,served_sha256:servedHash,stats:await page.locator('#build-stats').innerText()})
      await page.close()
    }
  }finally{await browser.close()}
  fs.writeFileSync(path.join(root,'qa/preview.json'),JSON.stringify({nbt_sha256:expected,status:results.every(view=>!view.errors.length)?'PASS_RENDER_ONLY':'FAIL',method:'Installed Lodestone viewer with temporary camera-only browser response overrides. Full-water views use actual NBT. One explicitly marked geometry-only derivative.',views:results},null,2))
}
main().catch(error=>{console.error(error);process.exitCode=1})




