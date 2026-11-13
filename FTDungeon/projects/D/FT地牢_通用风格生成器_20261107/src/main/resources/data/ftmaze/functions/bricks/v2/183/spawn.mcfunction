scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 692 211 62 minecraft:air if block 692 212 62 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 692 211 62 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 709 211 54 minecraft:air if block 709 212 54 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 709 211 54 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 704 211 72 minecraft:air if block 704 212 72 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 704 211 72 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 696 211 52 minecraft:air if block 696 212 52 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 696 211 52 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 701 211 59 minecraft:air if block 701 212 59 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 701 211 59 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 689 211 70 minecraft:air if block 689 212 70 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 689 211 70 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 698 211 67 minecraft:air if block 698 212 67 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 698 211 67 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 707 211 64 minecraft:air if block 707 212 64 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 707 211 64 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 693 211 62 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 693 211 62 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v2b183 ftm_brick 2
