scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 93 211 120 minecraft:air if block 93 212 120 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 93 211 120 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 109 211 130 minecraft:air if block 109 212 130 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 109 211 130 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 106 211 119 minecraft:air if block 106 212 119 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 106 211 119 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 96 211 130 minecraft:air if block 96 212 130 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 96 211 130 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 100 211 124 minecraft:air if block 100 212 124 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 100 211 124 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 91 211 127 minecraft:air if block 91 212 127 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 91 211 127 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 99 211 118 minecraft:air if block 99 212 118 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 99 211 118 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 102 211 129 minecraft:air if block 102 212 129 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 102 211 129 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 94 211 120 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 94 211 120 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v3b5 ftm_brick 2
