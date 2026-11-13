scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1828 151 30 minecraft:air if block 1828 152 30 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1828 151 30 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1845 151 44 minecraft:air if block 1845 152 44 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1845 151 44 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1843 151 25 minecraft:air if block 1843 152 25 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1843 151 25 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1825 151 44 minecraft:air if block 1825 152 44 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1825 151 44 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1835 151 38 minecraft:air if block 1835 152 38 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1835 151 38 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1833 151 25 minecraft:air if block 1833 152 25 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1833 151 25 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1838 151 30 minecraft:air if block 1838 152 30 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1838 151 30 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1843 151 35 minecraft:air if block 1843 152 35 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1843 151 35 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1829 151 30 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1829 151 30 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v3b246 ftm_brick 2
