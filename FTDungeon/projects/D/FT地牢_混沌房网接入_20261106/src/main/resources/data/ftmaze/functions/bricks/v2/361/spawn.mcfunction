scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 717 211 198 minecraft:air if block 717 212 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 717 211 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 707 211 202 minecraft:air if block 707 212 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 707 211 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 722 211 202 minecraft:air if block 722 212 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 722 211 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 710 211 198 minecraft:air if block 710 212 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 710 211 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 714 211 202 minecraft:air if block 714 212 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 714 211 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 711 211 201 minecraft:air if block 711 212 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 711 211 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 713 211 199 minecraft:air if block 713 212 199 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 713 211 199 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 718 211 201 minecraft:air if block 718 212 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 718 211 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 718 211 198 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 718 211 198 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v2b361 ftm_brick 2
