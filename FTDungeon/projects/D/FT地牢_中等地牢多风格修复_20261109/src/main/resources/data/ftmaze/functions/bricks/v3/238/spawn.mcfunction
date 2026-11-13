scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1785 151 117 minecraft:air if block 1785 152 117 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1785 151 117 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1802 151 107 minecraft:air if block 1802 152 107 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1802 151 107 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1798 151 127 minecraft:air if block 1798 152 127 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1798 151 127 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1782 151 107 minecraft:air if block 1782 152 107 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1782 151 107 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1782 151 127 minecraft:air if block 1782 152 127 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1782 151 127 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1792 151 110 minecraft:air if block 1792 152 110 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1792 151 110 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1799 151 116 minecraft:air if block 1799 152 116 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1799 151 116 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1790 151 123 minecraft:air if block 1790 152 123 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1790 151 123 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1786 151 117 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1786 151 117 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v3b238 ftm_brick 2
