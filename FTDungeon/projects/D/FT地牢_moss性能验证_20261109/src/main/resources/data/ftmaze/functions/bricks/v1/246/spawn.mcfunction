scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1828 211 30 minecraft:air if block 1828 212 30 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1828 211 30 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1845 211 44 minecraft:air if block 1845 212 44 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1845 211 44 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1843 211 25 minecraft:air if block 1843 212 25 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1843 211 25 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1825 211 44 minecraft:air if block 1825 212 44 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1825 211 44 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1835 211 38 minecraft:air if block 1835 212 38 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1835 211 38 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1833 211 25 minecraft:air if block 1833 212 25 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1833 211 25 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1838 211 30 minecraft:air if block 1838 212 30 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1838 211 30 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1843 211 35 minecraft:air if block 1843 212 35 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1843 211 35 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1829 211 30 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1829 211 30 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v1b246 ftm_brick 2
