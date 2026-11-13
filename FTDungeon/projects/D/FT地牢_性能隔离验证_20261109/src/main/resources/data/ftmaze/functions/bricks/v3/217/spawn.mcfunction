scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1401 31 148 minecraft:air if block 1401 32 148 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1401 31 148 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1416 31 163 minecraft:air if block 1416 32 163 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1416 31 163 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1416 31 143 minecraft:air if block 1416 32 143 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1416 31 143 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1400 31 163 minecraft:air if block 1400 32 163 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1400 31 163 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1408 31 156 minecraft:air if block 1408 32 156 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1408 31 156 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1418 31 153 minecraft:air if block 1418 32 153 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1418 31 153 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1398 31 155 minecraft:air if block 1398 32 155 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1398 31 155 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1406 31 143 minecraft:air if block 1406 32 143 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1406 31 143 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1402 31 148 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1402 31 148 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v3b217 ftm_brick 2
