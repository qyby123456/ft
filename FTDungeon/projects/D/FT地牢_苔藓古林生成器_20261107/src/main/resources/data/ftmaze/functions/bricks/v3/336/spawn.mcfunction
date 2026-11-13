scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 876 151 198 minecraft:air if block 876 152 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 876 151 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 866 151 202 minecraft:air if block 866 152 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 866 151 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 881 151 202 minecraft:air if block 881 152 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 881 151 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 869 151 198 minecraft:air if block 869 152 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 869 151 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 873 151 202 minecraft:air if block 873 152 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 873 151 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 870 151 201 minecraft:air if block 870 152 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 870 151 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 872 151 199 minecraft:air if block 872 152 199 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 872 151 199 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 877 151 201 minecraft:air if block 877 152 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 877 151 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 877 151 198 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 877 151 198 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v3b336 ftm_brick 2
