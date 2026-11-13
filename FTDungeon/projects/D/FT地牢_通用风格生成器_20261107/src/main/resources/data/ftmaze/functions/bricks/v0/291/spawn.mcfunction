scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 191 31 293 minecraft:air if block 191 32 293 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 191 31 293 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 184 31 302 minecraft:air if block 184 32 302 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 184 31 302 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 195 31 301 minecraft:air if block 195 32 301 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 195 31 301 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 184 31 294 minecraft:air if block 184 32 294 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 184 31 294 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 188 31 298 minecraft:air if block 188 32 298 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 188 31 298 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 189 31 303 minecraft:air if block 189 32 303 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 189 31 303 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 193 31 297 minecraft:air if block 193 32 297 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 193 31 297 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 183 31 298 minecraft:air if block 183 32 298 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 183 31 298 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 191 31 294 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 191 31 294 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v0b291 ftm_brick 2
