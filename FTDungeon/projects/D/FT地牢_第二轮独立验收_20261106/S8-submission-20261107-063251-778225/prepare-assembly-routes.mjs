import fs from 'node:fs/promises'
const layout=JSON.parse(await fs.readFile('assembly/layout.json','utf8')),input=JSON.parse(await fs.readFile('assembly/qa/route-input.json','utf8'))
for(const module of layout.modules)for(const port of module.ports)input[0].targets.push({label:`${module.id}: ${port.side} portal center`,position:port.foot})
await fs.writeFile('assembly/qa/route-input.json',JSON.stringify(input,null,2))
