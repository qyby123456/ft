scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1828 31 72 minecraft:air if block 1828 32 72 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1828 31 72 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1845 31 86 minecraft:air if block 1845 32 86 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1845 31 86 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1843 31 66 minecraft:air if block 1843 32 66 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1843 31 66 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1825 31 86 minecraft:air if block 1825 32 86 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1825 31 86 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1835 31 80 minecraft:air if block 1835 32 80 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1835 31 80 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1839 31 73 minecraft:air if block 1839 32 73 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1839 31 73 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1832 31 66 minecraft:air if block 1832 32 66 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1832 31 66 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1843 31 78 minecraft:air if block 1843 32 78 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1843 31 78 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1829 31 72 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1829 31 72 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v0b243 ftm_brick 2
