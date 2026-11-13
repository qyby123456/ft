scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1105 91 198 minecraft:air if block 1105 92 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1105 91 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1095 91 202 minecraft:air if block 1095 92 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1095 91 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1110 91 202 minecraft:air if block 1110 92 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1110 91 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1098 91 198 minecraft:air if block 1098 92 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1098 91 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1102 91 202 minecraft:air if block 1102 92 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1102 91 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1099 91 201 minecraft:air if block 1099 92 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1099 91 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1101 91 199 minecraft:air if block 1101 92 199 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1101 91 199 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1106 91 201 minecraft:air if block 1106 92 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1106 91 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1106 91 198 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1106 91 198 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v2b356 ftm_brick 2
