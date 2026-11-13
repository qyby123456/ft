scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 867 151 198 minecraft:air if block 867 152 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 867 151 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 857 151 202 minecraft:air if block 857 152 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 857 151 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 872 151 202 minecraft:air if block 872 152 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 872 151 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 860 151 198 minecraft:air if block 860 152 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 860 151 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 864 151 202 minecraft:air if block 864 152 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 864 151 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 861 151 201 minecraft:air if block 861 152 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 861 151 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 863 151 199 minecraft:air if block 863 152 199 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 863 151 199 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 868 151 201 minecraft:air if block 868 152 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 868 151 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 868 151 198 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 868 151 198 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v1b380 ftm_brick 2
