scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 694 31 98 minecraft:air if block 694 32 98 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 694 31 98 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 706 31 115 minecraft:air if block 706 32 115 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 706 31 115 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 687 31 112 minecraft:air if block 687 32 112 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 687 31 112 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 706 31 95 minecraft:air if block 706 32 95 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 706 31 95 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 698 31 108 minecraft:air if block 698 32 108 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 698 31 108 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 707 31 105 minecraft:air if block 707 32 105 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 707 31 105 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 687 31 101 minecraft:air if block 687 32 101 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 687 31 101 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 694 31 115 minecraft:air if block 694 32 115 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 694 31 115 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 694 31 99 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 694 31 99 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v3b180 ftm_brick 2
