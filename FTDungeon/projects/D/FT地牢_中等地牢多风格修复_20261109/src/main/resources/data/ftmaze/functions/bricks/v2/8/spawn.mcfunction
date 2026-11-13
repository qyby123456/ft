scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 122 211 94 minecraft:air if block 122 212 94 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 122 211 94 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 135 211 104 minecraft:air if block 135 212 104 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 135 211 104 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 137 211 91 minecraft:air if block 137 212 91 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 137 211 91 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 119 211 102 minecraft:air if block 119 212 102 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 119 211 102 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 130 211 97 minecraft:air if block 130 212 97 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 130 211 97 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 127 211 90 minecraft:air if block 127 212 90 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 127 211 90 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 124 211 99 minecraft:air if block 124 212 99 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 124 211 99 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 136 211 98 minecraft:air if block 136 212 98 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 136 211 98 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 122 211 95 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 122 211 95 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v2b8 ftm_brick 2
