scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 693 151 198 minecraft:air if block 693 152 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 693 151 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 683 151 202 minecraft:air if block 683 152 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 683 151 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 698 151 202 minecraft:air if block 698 152 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 698 151 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 686 151 198 minecraft:air if block 686 152 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 686 151 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 690 151 202 minecraft:air if block 690 152 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 690 151 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 687 151 201 minecraft:air if block 687 152 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 687 151 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 689 151 199 minecraft:air if block 689 152 199 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 689 151 199 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 694 151 201 minecraft:air if block 694 152 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 694 151 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 694 151 198 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 694 151 198 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v0b345 ftm_brick 2
