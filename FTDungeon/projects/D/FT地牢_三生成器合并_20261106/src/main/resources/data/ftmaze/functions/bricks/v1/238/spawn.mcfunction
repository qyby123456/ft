scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1785 211 117 minecraft:air if block 1785 212 117 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1785 211 117 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1802 211 107 minecraft:air if block 1802 212 107 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1802 211 107 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1798 211 127 minecraft:air if block 1798 212 127 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1798 211 127 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1782 211 107 minecraft:air if block 1782 212 107 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1782 211 107 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1782 211 127 minecraft:air if block 1782 212 127 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1782 211 127 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1792 211 110 minecraft:air if block 1792 212 110 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1792 211 110 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1799 211 116 minecraft:air if block 1799 212 116 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1799 211 116 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1790 211 123 minecraft:air if block 1790 212 123 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1790 211 123 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1786 211 117 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1786 211 117 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v1b238 ftm_brick 2
