scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1828 151 117 minecraft:air if block 1828 152 117 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1828 151 117 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1845 151 108 minecraft:air if block 1845 152 108 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1845 151 108 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1841 151 127 minecraft:air if block 1841 152 127 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1841 151 127 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1832 151 107 minecraft:air if block 1832 152 107 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1832 151 107 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1838 151 114 minecraft:air if block 1838 152 114 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1838 151 114 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1825 151 126 minecraft:air if block 1825 152 126 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1825 151 126 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1833 151 123 minecraft:air if block 1833 152 123 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1833 151 123 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1844 151 119 minecraft:air if block 1844 152 119 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1844 151 119 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1829 151 117 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1829 151 117 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v3b240 ftm_brick 2
