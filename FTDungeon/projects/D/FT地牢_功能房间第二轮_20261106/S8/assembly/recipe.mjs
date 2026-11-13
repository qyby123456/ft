import fs from 'node:fs'
import {fileURLToPath} from 'node:url'
export default function build(api){
 const directory=new URL('./',import.meta.url),layout=JSON.parse(fs.readFileSync(new URL('layout.json',directory),'utf8'))
 return {structure:api.lodestone.Structure.fromNbt(api.lodestone.NbtFile.read(fs.readFileSync(new URL('s8-functional-combined.nbt',directory))).root),metadata:layout.metadata}
}
