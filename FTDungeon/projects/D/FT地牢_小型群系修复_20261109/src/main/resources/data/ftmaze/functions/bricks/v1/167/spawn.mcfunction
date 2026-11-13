scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 441 31 148 minecraft:air if block 441 32 148 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 441 31 148 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 456 31 163 minecraft:air if block 456 32 163 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 456 31 163 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 456 31 143 minecraft:air if block 456 32 143 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 456 31 143 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 440 31 163 minecraft:air if block 440 32 163 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 440 31 163 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 448 31 156 minecraft:air if block 448 32 156 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 448 31 156 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 458 31 153 minecraft:air if block 458 32 153 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 458 31 153 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 438 31 155 minecraft:air if block 438 32 155 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 438 31 155 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 446 31 143 minecraft:air if block 446 32 143 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 446 31 143 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 442 31 148 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 442 31 148 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v1b167 ftm_brick 2
