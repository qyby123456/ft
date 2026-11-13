scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 692 31 23 minecraft:air if block 692 32 23 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 692 31 23 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 709 31 38 minecraft:air if block 709 32 38 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 709 31 38 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 708 31 18 minecraft:air if block 708 32 18 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 708 31 18 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 689 31 38 minecraft:air if block 689 32 38 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 689 31 38 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 699 31 32 minecraft:air if block 699 32 32 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 699 31 32 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 702 31 24 minecraft:air if block 702 32 24 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 702 31 24 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 697 31 18 minecraft:air if block 697 32 18 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 697 31 18 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 707 31 29 minecraft:air if block 707 32 29 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 707 31 29 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 693 31 23 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 693 31 23 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v3b186 ftm_brick 2
