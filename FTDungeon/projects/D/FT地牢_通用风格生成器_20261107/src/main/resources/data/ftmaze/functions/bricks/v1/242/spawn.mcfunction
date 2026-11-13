scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1785 211 71 minecraft:air if block 1785 212 71 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1785 211 71 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1802 211 86 minecraft:air if block 1802 212 86 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1802 211 86 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1801 211 66 minecraft:air if block 1801 212 66 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1801 211 66 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1782 211 86 minecraft:air if block 1782 212 86 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1782 211 86 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1792 211 80 minecraft:air if block 1792 212 80 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1792 211 80 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1795 211 72 minecraft:air if block 1795 212 72 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1795 211 72 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1790 211 66 minecraft:air if block 1790 212 66 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1790 211 66 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1800 211 77 minecraft:air if block 1800 212 77 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1800 211 77 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1786 211 71 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1786 211 71 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v1b242 ftm_brick 2
