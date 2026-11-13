scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 685 31 198 minecraft:air if block 685 32 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 685 31 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 675 31 202 minecraft:air if block 675 32 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 675 31 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 690 31 202 minecraft:air if block 690 32 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 690 31 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 678 31 198 minecraft:air if block 678 32 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 678 31 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 682 31 202 minecraft:air if block 682 32 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 682 31 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 679 31 201 minecraft:air if block 679 32 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 679 31 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 681 31 199 minecraft:air if block 681 32 199 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 681 31 199 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 686 31 201 minecraft:air if block 686 32 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 686 31 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 686 31 198 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 686 31 198 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v1b349 ftm_brick 2
