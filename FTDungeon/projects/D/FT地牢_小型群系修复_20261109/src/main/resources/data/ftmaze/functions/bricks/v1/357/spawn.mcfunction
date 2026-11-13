scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1021 31 198 minecraft:air if block 1021 32 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1021 31 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1011 31 202 minecraft:air if block 1011 32 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1011 31 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1026 31 202 minecraft:air if block 1026 32 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1026 31 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1014 31 198 minecraft:air if block 1014 32 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1014 31 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1018 31 202 minecraft:air if block 1018 32 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1018 31 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1015 31 201 minecraft:air if block 1015 32 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1015 31 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1017 31 199 minecraft:air if block 1017 32 199 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1017 31 199 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1022 31 201 minecraft:air if block 1022 32 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1022 31 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1022 31 198 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1022 31 198 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v1b357 ftm_brick 2
