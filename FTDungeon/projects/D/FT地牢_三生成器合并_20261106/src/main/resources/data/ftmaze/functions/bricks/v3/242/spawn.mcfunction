scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1785 151 71 minecraft:air if block 1785 152 71 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1785 151 71 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1802 151 86 minecraft:air if block 1802 152 86 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1802 151 86 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1801 151 66 minecraft:air if block 1801 152 66 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1801 151 66 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1782 151 86 minecraft:air if block 1782 152 86 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1782 151 86 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1792 151 80 minecraft:air if block 1792 152 80 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1792 151 80 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1795 151 72 minecraft:air if block 1795 152 72 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1795 151 72 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1790 151 66 minecraft:air if block 1790 152 66 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1790 151 66 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1800 151 77 minecraft:air if block 1800 152 77 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1800 151 77 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1786 151 71 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1786 151 71 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v3b242 ftm_brick 2
