scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 149 31 293 minecraft:air if block 149 32 293 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 149 31 293 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 142 31 302 minecraft:air if block 142 32 302 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 142 31 302 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 153 31 301 minecraft:air if block 153 32 301 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 153 31 301 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 142 31 294 minecraft:air if block 142 32 294 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 142 31 294 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 146 31 298 minecraft:air if block 146 32 298 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 146 31 298 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 147 31 303 minecraft:air if block 147 32 303 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 147 31 303 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 151 31 297 minecraft:air if block 151 32 297 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 151 31 297 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 141 31 298 minecraft:air if block 141 32 298 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 141 31 298 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 149 31 294 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 149 31 294 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v2b288 ftm_brick 2
