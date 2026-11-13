import fs from 'node:fs/promises'
import path from 'node:path'
const root=process.cwd(),source='D:/ft工程文件/FT地牢_功能房间第二轮_接手续作_20261106/S2/runtime-qa-r4',out=path.join(root,process.argv[2]??'runtime-r1'),project=path.join(out,'native-test')
await fs.mkdir(project,{recursive:true})
if(await fs.stat(path.join(out,'server')).catch(()=>null))throw Error('Preserve old runtime; use a fresh attempt')
for(const name of ['build.gradle','settings.gradle','gradle.properties']){
 const text=(await fs.readFile(`${source}/native-test/${name}`,'utf8')).replaceAll('s2qa','s8qa').replaceAll('fts2','fts8').replaceAll('s2-takeover','s8-takeover')
 await fs.writeFile(path.join(project,name),text)
}
await fs.cp(`${source}/native-test/src/main/resources`,`${project}/src/main/resources`,{recursive:true})
await fs.cp(`${source}/native-test/src/main/resources/data/fts2qa`,`${project}/src/main/resources/data/fts8qa`,{recursive:true})
const mods=`${project}/src/main/resources/META-INF/mods.toml`
await fs.writeFile(mods,(await fs.readFile(mods,'utf8')).replaceAll('fts2','fts8').replaceAll('S2','S8'))
let java=await fs.readFile(`${source}/native-test/src/main/java/net/farmingtales/s2qa/RoomTests.java`,'utf8')
java=java.replaceAll('s2qa','s8qa').replaceAll('fts2','fts8').replaceAll('S2','S8')
const start=java.indexOf('    @GameTest(template="empty", batch="s2ladder"'),end=java.indexOf('    @GameTest(template="empty", batch="s2rooms"')
if(start<0||end<0)throw Error('Runtime source pattern changed')
java=java.slice(0,start)+java.slice(end)
java=java.replace('batch="s2rooms"','batch="s8rooms"').replace('timeoutTicks=5000','timeoutTicks=8000')
java=java.replace('List<Object> differences=new ArrayList<>()','List<Object> interactions=new ArrayList<>(); List<Object> differences=new ArrayList<>()')
const marker='                    if(actual.is(Blocks.IRON_DOOR)'
java=java.replace(marker,`                    if((actual.getBlock() instanceof DoorBlock && actual.getValue(DoorBlock.HALF)==DoubleBlockHalf.LOWER && !actual.is(Blocks.IRON_DOOR)) || actual.getBlock() instanceof LeverBlock || level.getBlockEntity(position) instanceof Container) {
                        player.moveTo(position.getX()+.5,position.getY(),position.getZ()+1.5,180,0);
                        var beforeState=level.getBlockState(position);
                        var outcome=beforeState.use(level,player,InteractionHand.MAIN_HAND,new BlockHitResult(Vec3.atCenterOf(position),Direction.SOUTH,position,false));
                        var afterState=level.getBlockState(position);
                        boolean menuOpened=player.containerMenu!=player.inventoryMenu;
                        player.closeContainer();
                        if(beforeState.getBlock() instanceof DoorBlock || beforeState.getBlock() instanceof LeverBlock)afterState.use(level,player,InteractionHand.MAIN_HAND,new BlockHitResult(Vec3.atCenterOf(position),Direction.SOUTH,position,false));
                        interactions.add(Map.of("position",coordinates(position.subtract(origin)),"block",beforeState.toString(),"result",outcome.toString(),"changed",!beforeState.equals(afterState),"container_menu_opened",menuOpened,"scope","Real BlockState.use with isolated FakePlayer; no network client input or reach/path certification"));
                    }
`+marker)
java=java.replace('report.put("source_sha256",digest);','report.put("interactions",interactions);report.put("source_sha256",digest);')
java=java.replace('position.offset(-1,1,-1),position.offset(1,1,1)','position.offset(-2,0,-2),position.offset(2,1,2)')
await fs.mkdir(`${project}/src/main/java/net/farmingtales/s8qa`,{recursive:true})
await fs.writeFile(`${project}/src/main/java/net/farmingtales/s8qa/RoomTests.java`,java)
const rooms=[]
for(let number=11;number<=20;number++){
 const directory=path.join(root,`S8-${number}`),inventory=JSON.parse(await fs.readFile(`${directory}/inventory.json`,'utf8'))
 const filename=(await fs.readdir(directory)).find(name=>name.endsWith('.nbt'))
 rooms.push({room:`S8-${number}`,candidate:path.join(directory,filename),sha256:inventory.sha256})
}
await fs.writeFile(`${out}/rooms.json`,JSON.stringify(rooms,null,2))
let run=await fs.readFile(`${source}/run.ps1`,'utf8');run=run.replaceAll('s2-new-isolated-qa-world','s8-new-isolated-qa-world')
await fs.writeFile(`${out}/run.ps1`,run)
console.log('Prepared isolated S8 runtime',out)
