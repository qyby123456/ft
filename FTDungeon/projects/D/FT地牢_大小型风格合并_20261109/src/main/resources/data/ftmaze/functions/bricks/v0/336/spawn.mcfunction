scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 876 31 198 minecraft:air if block 876 32 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 876 31 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 866 31 202 minecraft:air if block 866 32 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 866 31 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 881 31 202 minecraft:air if block 881 32 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 881 31 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 869 31 198 minecraft:air if block 869 32 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 869 31 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 873 31 202 minecraft:air if block 873 32 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 873 31 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 870 31 201 minecraft:air if block 870 32 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 870 31 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 872 31 199 minecraft:air if block 872 32 199 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 872 31 199 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 877 31 201 minecraft:air if block 877 32 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 877 31 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 877 31 198 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 877 31 198 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v0b336 ftm_brick 2
