scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1785 213 34 minecraft:air if block 1785 214 34 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1785 213 34 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1802 213 24 minecraft:air if block 1802 214 24 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1802 213 24 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1798 213 44 minecraft:air if block 1798 214 44 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1798 213 44 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1782 213 24 minecraft:air if block 1782 214 24 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1782 213 24 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1782 213 44 minecraft:air if block 1782 214 44 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1782 213 44 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1792 213 27 minecraft:air if block 1792 214 27 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1792 213 27 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1799 213 33 minecraft:air if block 1799 214 33 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1799 213 33 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1790 213 40 minecraft:air if block 1790 214 40 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1790 213 40 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1786 213 34 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1786 213 34 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v1b245 ftm_brick 2
