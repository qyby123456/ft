scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 666 151 198 minecraft:air if block 666 152 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 666 151 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 656 151 202 minecraft:air if block 656 152 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 656 151 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 671 151 202 minecraft:air if block 671 152 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 671 151 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 659 151 198 minecraft:air if block 659 152 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 659 151 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 663 151 202 minecraft:air if block 663 152 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 663 151 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 660 151 201 minecraft:air if block 660 152 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 660 151 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 662 151 199 minecraft:air if block 662 152 199 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 662 151 199 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 667 151 201 minecraft:air if block 667 152 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 667 151 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 667 151 198 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 667 151 198 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v2b320 ftm_brick 2
