scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1147 91 198 minecraft:air if block 1147 92 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1147 91 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1137 91 202 minecraft:air if block 1137 92 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1137 91 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1152 91 202 minecraft:air if block 1152 92 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1152 91 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1140 91 198 minecraft:air if block 1140 92 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1140 91 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1144 91 202 minecraft:air if block 1144 92 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1144 91 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1141 91 201 minecraft:air if block 1141 92 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1141 91 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1143 91 199 minecraft:air if block 1143 92 199 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1143 91 199 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1148 91 201 minecraft:air if block 1148 92 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1148 91 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1148 91 198 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1148 91 198 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v2b357 ftm_brick 2
