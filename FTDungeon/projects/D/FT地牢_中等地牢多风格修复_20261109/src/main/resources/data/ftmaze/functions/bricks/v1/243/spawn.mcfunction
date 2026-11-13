scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1828 211 72 minecraft:air if block 1828 212 72 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1828 211 72 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1845 211 86 minecraft:air if block 1845 212 86 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1845 211 86 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1843 211 66 minecraft:air if block 1843 212 66 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1843 211 66 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1825 211 86 minecraft:air if block 1825 212 86 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1825 211 86 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1835 211 80 minecraft:air if block 1835 212 80 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1835 211 80 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1839 211 73 minecraft:air if block 1839 212 73 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1839 211 73 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1832 211 66 minecraft:air if block 1832 212 66 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1832 211 66 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1843 211 78 minecraft:air if block 1843 212 78 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1843 211 78 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1829 211 72 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1829 211 72 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v1b243 ftm_brick 2
