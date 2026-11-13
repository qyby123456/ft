scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 666 211 198 minecraft:air if block 666 212 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 666 211 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 656 211 202 minecraft:air if block 656 212 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 656 211 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 671 211 202 minecraft:air if block 671 212 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 671 211 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 659 211 198 minecraft:air if block 659 212 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 659 211 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 663 211 202 minecraft:air if block 663 212 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 663 211 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 660 211 201 minecraft:air if block 660 212 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 660 211 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 662 211 199 minecraft:air if block 662 212 199 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 662 211 199 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 667 211 201 minecraft:air if block 667 212 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 667 211 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 667 211 198 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 667 211 198 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v1b335 ftm_brick 2
