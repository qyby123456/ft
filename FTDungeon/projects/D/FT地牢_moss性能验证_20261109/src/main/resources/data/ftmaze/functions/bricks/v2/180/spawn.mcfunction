scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 694 211 98 minecraft:air if block 694 212 98 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 694 211 98 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 706 211 115 minecraft:air if block 706 212 115 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 706 211 115 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 687 211 112 minecraft:air if block 687 212 112 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 687 211 112 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 706 211 95 minecraft:air if block 706 212 95 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 706 211 95 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 698 211 108 minecraft:air if block 698 212 108 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 698 211 108 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 707 211 105 minecraft:air if block 707 212 105 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 707 211 105 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 687 211 101 minecraft:air if block 687 212 101 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 687 211 101 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 694 211 115 minecraft:air if block 694 212 115 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 694 211 115 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 694 211 99 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 694 211 99 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v2b180 ftm_brick 2
