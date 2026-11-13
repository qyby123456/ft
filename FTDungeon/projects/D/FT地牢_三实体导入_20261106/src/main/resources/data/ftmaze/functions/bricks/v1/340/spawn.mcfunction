scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 876 211 198 minecraft:air if block 876 212 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 876 211 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 866 211 202 minecraft:air if block 866 212 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 866 211 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 881 211 202 minecraft:air if block 881 212 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 881 211 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 869 211 198 minecraft:air if block 869 212 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 869 211 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 873 211 202 minecraft:air if block 873 212 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 873 211 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 870 211 201 minecraft:air if block 870 212 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 870 211 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 872 211 199 minecraft:air if block 872 212 199 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 872 211 199 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 877 211 201 minecraft:air if block 877 212 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 877 211 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 877 211 198 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 877 211 198 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v1b340 ftm_brick 2
