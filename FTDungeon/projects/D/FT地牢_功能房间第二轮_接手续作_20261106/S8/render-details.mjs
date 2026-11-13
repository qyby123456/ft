import fs from 'node:fs/promises'
import {spawnSync} from 'node:child_process'
const views=JSON.parse(await fs.readFile('closeups.json','utf8'))
for(const [room,cameras] of Object.entries(views)){
 const file=`${room}/cameras.json`
 await fs.writeFile(file,JSON.stringify(cameras,null,2))
 const result=spawnSync(process.execPath,['render.cjs',room,file],{stdio:'inherit',windowsHide:true})
 if(result.status!==0)throw Error(`Render failed ${room}`)
}
