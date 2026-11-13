scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1785 31 71 minecraft:air if block 1785 32 71 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1785 31 71 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1802 31 86 minecraft:air if block 1802 32 86 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1802 31 86 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1801 31 66 minecraft:air if block 1801 32 66 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1801 31 66 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1782 31 86 minecraft:air if block 1782 32 86 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1782 31 86 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1792 31 80 minecraft:air if block 1792 32 80 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1792 31 80 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1795 31 72 minecraft:air if block 1795 32 72 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1795 31 72 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1790 31 66 minecraft:air if block 1790 32 66 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1790 31 66 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1800 31 77 minecraft:air if block 1800 32 77 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1800 31 77 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1786 31 71 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1786 31 71 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v0b242 ftm_brick 2
