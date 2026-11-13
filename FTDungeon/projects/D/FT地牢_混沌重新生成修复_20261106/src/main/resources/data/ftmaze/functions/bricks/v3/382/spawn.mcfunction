scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1119 91 198 minecraft:air if block 1119 92 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1119 91 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1109 91 202 minecraft:air if block 1109 92 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1109 91 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1124 91 202 minecraft:air if block 1124 92 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1124 91 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1112 91 198 minecraft:air if block 1112 92 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1112 91 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1116 91 202 minecraft:air if block 1116 92 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1116 91 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1113 91 201 minecraft:air if block 1113 92 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1113 91 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1115 91 199 minecraft:air if block 1115 92 199 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1115 91 199 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1120 91 201 minecraft:air if block 1120 92 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1120 91 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1120 91 198 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1120 91 198 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v3b382 ftm_brick 2
