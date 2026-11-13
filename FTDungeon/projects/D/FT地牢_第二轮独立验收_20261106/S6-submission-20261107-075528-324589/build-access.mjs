import fs from 'node:fs'
import path from 'node:path'
import {fileURLToPath} from 'node:url'
import {createHash} from 'node:crypto'
import assert from 'node:assert/strict'
const root=path.dirname(fileURLToPath(import.meta.url))
const output=path.join(root,'assembly/access')
const pack=path.join(output,'datapack')
const read=relative=>JSON.parse(fs.readFileSync(path.join(root,relative),'utf8'))
const hash=filename=>createHash('sha256').update(fs.readFileSync(filename)).digest('hex')
const split=read('assembly/qa/split-payload-verification.json')
assert.equal(split.source_sha256,'5b72d94c89871ebe9c262f92d6c00275ee0c8cfbf32343be96c9cd8e538f1336')
const write=(relative,content)=>{const filename=path.join(pack,relative);fs.mkdirSync(path.dirname(filename),{recursive:true});fs.writeFileSync(filename,content+'\n')}
const json=(relative,value)=>write(relative,JSON.stringify(value,null,2))
json('pack.mcmeta',{pack:{pack_format:15,description:'FT S6 sealed dungeon access: authorized entry and voluntary exit, MC 1.20.1'}})
json('data/minecraft/tags/functions/load.json',{values:['fts6_access:load']})
json('data/minecraft/tags/functions/tick.json',{values:['fts6_access:tick']})
const functions={
  load:['scoreboard objectives add fts6_enter trigger','scoreboard objectives add fts6_exit trigger'],
  install:['execute in minecraft:overworld unless entity @e[type=minecraft:marker,tag=fts6_origin] run function fts6_access:install_once'],
  install_once:[
    'forceload add ~ ~ ~206 ~179',
    ...split.pieces.map(piece=>`place template fts6_access:${path.basename(piece.file,'.nbt').replaceAll('-','_')} ~${piece.origin[0]} ~${piece.origin[1]} ~${piece.origin[2]}`),
    'fill ~-10 ~0 ~32 ~-1 ~3 ~40 minecraft:polished_blackstone_bricks',
    'fill ~-10 ~4 ~32 ~-1 ~7 ~40 minecraft:air',
    'fill ~-10 ~4 ~32 ~-10 ~5 ~40 minecraft:orange_stained_glass',
    'fill ~-1 ~4 ~32 ~-1 ~5 ~40 minecraft:orange_stained_glass',
    'fill ~-9 ~4 ~32 ~-2 ~5 ~32 minecraft:orange_stained_glass',
    'fill ~-9 ~4 ~40 ~-2 ~5 ~40 minecraft:orange_stained_glass',
    'fill ~-7 ~4 ~40 ~-4 ~5 ~40 minecraft:air',
    'setblock ~-6 ~3 ~36 minecraft:chiseled_polished_blackstone',
    'summon minecraft:marker ~ ~ ~ {Tags:["fts6_origin"]}',
    'summon minecraft:marker ~-5.5 ~4 ~36.5 {Tags:["fts6_lobby"]}',
    'forceload remove ~ ~ ~206 ~179',
    'forceload add ~ ~',
    'forceload add ~-6 ~36'
  ],
  tick:['execute as @a at @s if dimension minecraft:overworld run function fts6_access:player_tick'],
  player_tick:[
    'execute if score @s fts6_enter matches 1.. run function fts6_access:enter',
    'execute if score @s fts6_exit matches 1.. run function fts6_access:exit',
    'scoreboard players set @s fts6_enter 0',
    'scoreboard players set @s fts6_exit 0',
    'execute if entity @s[tag=fts6_authorized] at @e[type=minecraft:marker,tag=fts6_lobby,limit=1] if entity @s[distance=..2] run scoreboard players enable @s fts6_enter',
    'execute at @e[type=minecraft:marker,tag=fts6_origin,limit=1] positioned ~2.5 ~4 ~36.5 if entity @s[distance=..2] run scoreboard players enable @s fts6_exit'
  ],
  enter:['execute if dimension minecraft:overworld if entity @s[tag=fts6_authorized] at @e[type=minecraft:marker,tag=fts6_lobby,limit=1] if entity @s[distance=..2] at @e[type=minecraft:marker,tag=fts6_origin,limit=1] run tp @s ~2.5 ~4 ~36.5 -90 0'],
  exit:['execute if dimension minecraft:overworld at @e[type=minecraft:marker,tag=fts6_origin,limit=1] positioned ~2.5 ~4 ~36.5 if entity @s[distance=..2] at @e[type=minecraft:marker,tag=fts6_lobby,limit=1] run tp @s ~ ~ ~ 0 0']
}
for(const [name,lines] of Object.entries(functions)) write(`data/fts6_access/functions/${name}.mcfunction`,lines.join('\n'))
for(const piece of split.pieces) {
  assert.equal(hash(piece.file),piece.sha256)
  const filename=path.join(pack,'data/fts6_access/structures',path.basename(piece.file,'.nbt').replaceAll('-','_')+'.nbt')
  fs.mkdirSync(path.dirname(filename),{recursive:true});fs.copyFileSync(piece.file,filename)
}
const contract={source_sha256:split.source_sha256,method:'Authorized trigger-based teleport through unchanged closed barrier shell',minecraft:'1.20.1',pack_format:15,dimension:'minecraft:overworld',single_instance:true,orientation:'unrotated',placement_origin:'integer X Y Z',relative_external_lobby:[-5.5,4,36.5],relative_internal_arrival:[2.5,4,36.5],activation_radius:2,authorization_tag:'fts6_authorized',enter_command:'/trigger fts6_enter set 1',exit_command:'/trigger fts6_exit set 1',outside_platform_bounds:[[-10,0,32],[-1,7,40]],barrier_cells_expected:47553,entry_requires_authorization:true,exit_requires_authorization:false,operator_install_required:true,formal_instance_modified:false,not_supported:['Multiple instances per world','Rotation','Automatic terrain integration','Preventing creative/operator commands, other teleport mods or ender-pearl mechanics','Formal instance deployment or generator edits']}
fs.writeFileSync(path.join(output,'contract.json'),JSON.stringify(contract,null,2)+'\n')
const runtimeData=path.join(root,'qa/runtime/src/main/resources/data')
fs.cpSync(path.join(pack,'data/fts6_access'),path.join(runtimeData,'fts6_access'),{recursive:true})
console.log(JSON.stringify({source_sha256:split.source_sha256,pieces:split.pieces.length,pack,contract}))
