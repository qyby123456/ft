scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1785 31 117 minecraft:air if block 1785 32 117 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1785 31 117 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1802 31 107 minecraft:air if block 1802 32 107 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1802 31 107 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1798 31 127 minecraft:air if block 1798 32 127 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1798 31 127 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1782 31 107 minecraft:air if block 1782 32 107 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1782 31 107 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1782 31 127 minecraft:air if block 1782 32 127 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1782 31 127 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1792 31 110 minecraft:air if block 1792 32 110 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1792 31 110 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1799 31 116 minecraft:air if block 1799 32 116 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1799 31 116 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1790 31 123 minecraft:air if block 1790 32 123 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1790 31 123 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1786 31 117 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1786 31 117 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v0b238 ftm_brick 2
