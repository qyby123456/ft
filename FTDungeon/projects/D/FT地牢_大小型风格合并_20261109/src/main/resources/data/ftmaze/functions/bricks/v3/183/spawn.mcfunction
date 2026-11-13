scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 692 31 62 minecraft:air if block 692 32 62 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 692 31 62 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 709 31 54 minecraft:air if block 709 32 54 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 709 31 54 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 704 31 72 minecraft:air if block 704 32 72 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 704 31 72 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 696 31 52 minecraft:air if block 696 32 52 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 696 31 52 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 701 31 59 minecraft:air if block 701 32 59 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 701 31 59 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 689 31 70 minecraft:air if block 689 32 70 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 689 31 70 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 698 31 67 minecraft:air if block 698 32 67 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 698 31 67 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 707 31 64 minecraft:air if block 707 32 64 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 707 31 64 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 693 31 62 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 693 31 62 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v3b183 ftm_brick 2
