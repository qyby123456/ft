const {chromium}=require('C:/Users/YOUR_USERNAME/.cache/codex-runtimes/codex-primary-runtime/dependencies/node/node_modules/playwright')
const fs=require('node:fs')
const path=require('node:path')
const crypto=require('node:crypto')
const root=__dirname
const hash=bytes=>crypto.createHash('sha256').update(bytes).digest('hex')
const expected=hash(fs.readFileSync(path.join(root,'nbt/s5-10-coral-ocean-complex.nbt')))
const views=[
  {name:'full-water-overview-south',port:5721,center:[32.5,8,32.5],distance:98,pitch:.72,yaw:-.82},
  {name:'full-water-overview-north',port:5721,center:[32.5,8,32.5],distance:98,pitch:.72,yaw:2.32},
  {name:'full-water-plan',port:5721,center:[32.5,5,32.5],distance:92,pitch:1.43,yaw:0},
  {name:'full-water-great-hall',port:5721,center:[46,8,6],distance:25,pitch:.17,yaw:0},
  {name:'full-water-sanctum',port:5721,center:[15,4,52],distance:10,pitch:.5,yaw:0},
  {name:'full-water-library',port:5721,center:[12,5,5],distance:17,pitch:.22,yaw:0},
  {name:'full-water-treasure',port:5721,center:[54,5,46],distance:13,pitch:.25,yaw:0},
  {name:'full-water-corridor',port:5721,center:[27,5,18],distance:12,pitch:.1,yaw:0},
  {name:'full-water-courtyard',port:5721,center:[10,4.5,28],distance:9,pitch:.2,yaw:-1.5708},
  {name:'full-water-entrance',port:5721,center:[36,6,63],distance:13,pitch:.15,yaw:0},
  {name:'water-hidden-GEOMETRY-ONLY',port:5722,center:[32.5,7,32.5],distance:98,pitch:.78,yaw:-.82}
]
async function main(){
  const browser=await chromium.launch({headless:true,args:['--enable-webgl','--use-angle=swiftshader','--enable-unsafe-swiftshader']})
  const results=[]
  try{
    for(const view of views){
      const page=await browser.newPage({viewport:{width:1600,height:1200}})
      const errors=[]
      page.on('pageerror',error=>errors.push(error.message))
      await page.route('**/src/main.js',async route=>{
        const response=await route.fetch()
        let body=await response.text()
        body=body.replace('this.xRotation = 0.62',`this.xRotation = ${view.pitch}`).replace('this.yRotation = 0.82',`this.yRotation = ${view.yaw}`)
        body=body.replace('const center = vec3.fromValues(size[0] / 2, Math.max(1, size[1] / 2.5), size[2] / 2)',`const center = vec3.fromValues(${view.center.join(',')})`).replace('Math.max(18, Math.max(...size) * 1.9)',String(view.distance))
        await route.fulfill({response,body})
      })
      await page.goto(`http://127.0.0.1:${view.port}/?nbt=%2F__minecraft_builder__%2Fbuild.nbt&name=S5-10-${view.name}`,{waitUntil:'domcontentloaded'})
      await page.waitForSelector('#loading.hidden',{state:'attached',timeout:180000})
      const served=await page.request.get(`http://127.0.0.1:${view.port}/__minecraft_builder__/build.nbt`)
      const servedHash=hash(await served.body())
      if(view.port===5721&&servedHash!==expected)throw Error('Preview source mismatch')
      await page.waitForTimeout(1200)
      await page.screenshot({path:path.join(root,`qa/${view.name}.png`)})
      results.push({...view,errors,served_sha256:servedHash,stats:await page.locator('#build-stats').innerText()})
      await page.close()
    }
  }finally{await browser.close()}
  fs.writeFileSync(path.join(root,'qa/preview.json'),JSON.stringify({nbt_sha256:expected,status:results.every(view=>!view.errors.length)?'PASS_RENDER_ONLY':'FAIL',method:'Actual whole source NBT, camera-only browser response overrides; last view explicitly GEOMETRY_ONLY',views:results},null,2))
}
main().catch(error=>{console.error(error);process.exitCode=1})
