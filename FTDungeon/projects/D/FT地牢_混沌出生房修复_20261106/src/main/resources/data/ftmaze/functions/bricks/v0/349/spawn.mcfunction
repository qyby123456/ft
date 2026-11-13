scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 861 151 198 minecraft:air if block 861 152 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 861 151 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 851 151 202 minecraft:air if block 851 152 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 851 151 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 866 151 202 minecraft:air if block 866 152 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 866 151 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 854 151 198 minecraft:air if block 854 152 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 854 151 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 858 151 202 minecraft:air if block 858 152 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 858 151 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 855 151 201 minecraft:air if block 855 152 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 855 151 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 857 151 199 minecraft:air if block 857 152 199 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 857 151 199 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 862 151 201 minecraft:air if block 862 152 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 862 151 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 862 151 198 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 862 151 198 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v0b349 ftm_brick 2
