scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 867 211 198 minecraft:air if block 867 212 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 867 211 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 857 211 202 minecraft:air if block 857 212 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 857 211 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 872 211 202 minecraft:air if block 872 212 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 872 211 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 860 211 198 minecraft:air if block 860 212 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 860 211 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 864 211 202 minecraft:air if block 864 212 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 864 211 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 861 211 201 minecraft:air if block 861 212 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 861 211 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 863 211 199 minecraft:air if block 863 212 199 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 863 211 199 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 868 211 201 minecraft:air if block 868 212 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 868 211 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 868 211 198 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 868 211 198 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v0b376 ftm_brick 2
