scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 692 211 23 minecraft:air if block 692 212 23 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 692 211 23 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 709 211 38 minecraft:air if block 709 212 38 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 709 211 38 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 708 211 18 minecraft:air if block 708 212 18 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 708 211 18 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 689 211 38 minecraft:air if block 689 212 38 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 689 211 38 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 699 211 32 minecraft:air if block 699 212 32 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 699 211 32 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 702 211 24 minecraft:air if block 702 212 24 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 702 211 24 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 697 211 18 minecraft:air if block 697 212 18 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 697 211 18 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 707 211 29 minecraft:air if block 707 212 29 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 707 211 29 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 693 211 23 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 693 211 23 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v2b186 ftm_brick 2
