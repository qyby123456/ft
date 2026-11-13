scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1021 211 198 minecraft:air if block 1021 212 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1021 211 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1011 211 202 minecraft:air if block 1011 212 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1011 211 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1026 211 202 minecraft:air if block 1026 212 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1026 211 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1014 211 198 minecraft:air if block 1014 212 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1014 211 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1018 211 202 minecraft:air if block 1018 212 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1018 211 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1015 211 201 minecraft:air if block 1015 212 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1015 211 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1017 211 199 minecraft:air if block 1017 212 199 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1017 211 199 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1022 211 201 minecraft:air if block 1022 212 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1022 211 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1022 211 198 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1022 211 198 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v3b354 ftm_brick 2
