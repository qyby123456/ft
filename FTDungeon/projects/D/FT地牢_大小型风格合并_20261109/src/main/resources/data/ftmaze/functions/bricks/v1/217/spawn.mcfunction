scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1401 91 148 minecraft:air if block 1401 92 148 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1401 91 148 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1416 91 163 minecraft:air if block 1416 92 163 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1416 91 163 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1416 91 143 minecraft:air if block 1416 92 143 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1416 91 143 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1400 91 163 minecraft:air if block 1400 92 163 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1400 91 163 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1408 91 156 minecraft:air if block 1408 92 156 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1408 91 156 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1418 91 153 minecraft:air if block 1418 92 153 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1418 91 153 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1398 91 155 minecraft:air if block 1398 92 155 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1398 91 155 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1406 91 143 minecraft:air if block 1406 92 143 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1406 91 143 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1402 91 148 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1402 91 148 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v1b217 ftm_brick 2
