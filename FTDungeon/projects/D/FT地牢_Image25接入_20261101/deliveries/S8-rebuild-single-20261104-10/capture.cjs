const fs = require('node:fs')
const path = require('node:path')
const crypto = require('node:crypto')
const { chromium } = require('C:/Users/YOUR_USERNAME/.cache/codex-runtimes/codex-primary-runtime/dependencies/node/node_modules/playwright')

const hash = bytes => crypto.createHash('sha256').update(bytes).digest('hex')

async function main() {
  const browser = await chromium.launch({ channel: 'msedge', headless: true, args: ['--use-angle=swiftshader', '--enable-unsafe-swiftshader'] })
  const scenarios = [
    {name:'complete-exterior-front',position:[80,62,93],target:[33,4,36]},
    {name:'complete-exterior-reverse',position:[-10,62,-20],target:[33,4,36]},
    {name:'inspection-overview-front',cut:true,position:[78,80,92],target:[33,2,35]},
    {name:'inspection-overview-reverse',cut:true,position:[-10,80,-20],target:[33,2,35]},
    {name:'complete-courtyard-eye',position:[33.5,3.62,70.5],target:[33.5,6,60]},
    {name:'complete-entry-eye',position:[33.5,3.62,62.5],target:[33.5,5,54]},
    {name:'complete-hall-eye',position:[33.5,3.62,49.5],target:[33.5,5.5,38]},
    {name:'complete-hall-reverse',position:[33.5,10,32.5],target:[33.5,4,48]},
    {name:'complete-sanctuary-eye',position:[33.5,5.62,21.5],target:[33.5,8,7]},
    {name:'complete-sanctuary-reverse',position:[39.5,11,6.5],target:[31,5,21]},
    {name:'complete-pool-eye',position:[15.5,3.62,44.5],target:[10.5,4.5,37.5]},
    {name:'complete-study-eye',position:[11.5,3.62,22.5],target:[9,3,16]},
    {name:'complete-archive-eye',position:[56.5,3.62,20.5],target:[56.5,4,9]},
    {name:'complete-archive-containers',position:[56.5,6,14.5],target:[56.5,3,9.5]},
    {name:'complete-bent-corridor-eye',position:[53.5,3.62,28.5],target:[53.5,3.62,34.5]},
    {name:'complete-east-cell-eye',position:[52.5,3.62,50.5],target:[56.5,5,46.5]},
    {name:'complete-sanctuary-stairs',position:[33.5,3.62,29.5],target:[33.5,5,24]},
    {name:'complete-archive-stairs',position:[50.5,3.62,17.5],target:[45.5,5,17.5]}
  ]
  const records = []
  try {
    for (const scenario of scenarios) {
      const relative = scenario.cut ? 'views/inspection-roofs-only.nbt' : 'incoming/S8R_IMG10/overview.nbt'
      const bytes = fs.readFileSync(path.join(__dirname,relative))
      const page = await browser.newPage({viewport:{width:1500,height:1100}})
      const errors = []
      page.on('pageerror',error=>errors.push(error.message))
      await page.route('**/src/main.js',async route=>{
        const response=await route.fetch()
        const source=(await response.text()).replace('position: vec3.fromValues(inverse[12], inverse[13], inverse[14]),',`position: vec3.fromValues(${scenario.position.join(',')}),`).replace('target: this.center,',`target: vec3.fromValues(${scenario.target.join(',')}),`)
        if(!source.includes(`position: vec3.fromValues(${scenario.position.join(',')}),`))throw new Error('Camera injection failed')
        await route.fulfill({response,body:source})
      })
      await page.route('**/__minecraft_builder__/build.nbt',route=>route.fulfill({status:200,contentType:'application/octet-stream',body:bytes}))
      await page.goto(`http://127.0.0.1:5610/?nbt=%2F__minecraft_builder__%2Fbuild.nbt&name=S8-10-${scenario.name}`,{waitUntil:'networkidle',timeout:180000})
      await page.waitForFunction(()=>document.getElementById('loading').classList.contains('hidden'),{timeout:180000})
      await page.evaluate(()=>new Promise(resolve=>requestAnimationFrame(()=>requestAnimationFrame(resolve))))
      const served=await page.evaluate(async()=>Array.from(new Uint8Array(await crypto.subtle.digest('SHA-256',await(await fetch('/__minecraft_builder__/build.nbt')).arrayBuffer()))).map(value=>value.toString(16).padStart(2,'0')).join(''))
      if(served!==hash(bytes)||errors.length)throw new Error('Wrong source or renderer errors '+errors.join(';'))
      const screenshot=`views/${scenario.name}.png`
      await page.screenshot({path:path.join(__dirname,screenshot)})
      records.push({name:scenario.name,nbt:relative,served_sha256:served,screenshot,screenshot_sha256:hash(fs.readFileSync(path.join(__dirname,screenshot))),camera_position:scenario.position,camera_target:scenario.target,geometry:scenario.cut?'DECLARED_ROOF_ONLY_DERIVATIVE':'COMPLETE_FINAL_SOURCE_NO_GEOMETRY_EDITS',page_errors:errors})
      await page.close()
      process.stdout.write(scenario.name+'\n')
    }
  } finally {
    await browser.close()
  }
  fs.writeFileSync(path.join(__dirname,'qa/lodestone.json'),JSON.stringify({status:'PASS_HASHED_WEBGL_LOADS',renderer:'Bundled Lodestone ThreeStructureRenderer, actual WebGL screenshots, fixed recorded cameras; no gameplay claim',records},null,2))
}

main().catch(error=>{process.stderr.write(String(error));process.exitCode=1})
