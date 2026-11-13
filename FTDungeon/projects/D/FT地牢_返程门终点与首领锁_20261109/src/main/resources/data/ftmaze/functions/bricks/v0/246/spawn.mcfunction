scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1828 31 30 minecraft:air if block 1828 32 30 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1828 31 30 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1845 31 44 minecraft:air if block 1845 32 44 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1845 31 44 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1843 31 25 minecraft:air if block 1843 32 25 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1843 31 25 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1825 31 44 minecraft:air if block 1825 32 44 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1825 31 44 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1835 31 38 minecraft:air if block 1835 32 38 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1835 31 38 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1833 31 25 minecraft:air if block 1833 32 25 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1833 31 25 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1838 31 30 minecraft:air if block 1838 32 30 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1838 31 30 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1843 31 35 minecraft:air if block 1843 32 35 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1843 31 35 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1829 31 30 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1829 31 30 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v0b246 ftm_brick 2
