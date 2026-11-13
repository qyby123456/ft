scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 441 91 148 minecraft:air if block 441 92 148 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 441 91 148 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 456 91 163 minecraft:air if block 456 92 163 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 456 91 163 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 456 91 143 minecraft:air if block 456 92 143 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 456 91 143 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 440 91 163 minecraft:air if block 440 92 163 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 440 91 163 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 448 91 156 minecraft:air if block 448 92 156 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 448 91 156 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 458 91 153 minecraft:air if block 458 92 153 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 458 91 153 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 438 91 155 minecraft:air if block 438 92 155 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 438 91 155 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 446 91 143 minecraft:air if block 446 92 143 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 446 91 143 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 442 91 148 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 442 91 148 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v2b167 ftm_brick 2
