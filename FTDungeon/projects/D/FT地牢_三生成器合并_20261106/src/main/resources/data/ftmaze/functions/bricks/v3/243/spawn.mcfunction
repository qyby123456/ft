scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1828 151 72 minecraft:air if block 1828 152 72 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1828 151 72 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1845 151 86 minecraft:air if block 1845 152 86 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1845 151 86 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1843 151 66 minecraft:air if block 1843 152 66 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1843 151 66 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1825 151 86 minecraft:air if block 1825 152 86 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1825 151 86 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1835 151 80 minecraft:air if block 1835 152 80 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1835 151 80 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1839 151 73 minecraft:air if block 1839 152 73 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1839 151 73 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1832 151 66 minecraft:air if block 1832 152 66 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1832 151 66 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1843 151 78 minecraft:air if block 1843 152 78 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1843 151 78 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1829 151 72 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1829 151 72 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v3b243 ftm_brick 2
