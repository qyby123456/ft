scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1203 91 198 minecraft:air if block 1203 92 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1203 91 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1193 91 202 minecraft:air if block 1193 92 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1193 91 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1208 91 202 minecraft:air if block 1208 92 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1208 91 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1196 91 198 minecraft:air if block 1196 92 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1196 91 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1200 91 202 minecraft:air if block 1200 92 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1200 91 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1197 91 201 minecraft:air if block 1197 92 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1197 91 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1199 91 199 minecraft:air if block 1199 92 199 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1199 91 199 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1204 91 201 minecraft:air if block 1204 92 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1204 91 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1204 91 198 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1204 91 198 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v3b384 ftm_brick 2
