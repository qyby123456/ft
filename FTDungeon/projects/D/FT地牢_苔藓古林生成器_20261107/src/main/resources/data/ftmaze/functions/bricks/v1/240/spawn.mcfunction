scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1828 211 117 minecraft:air if block 1828 212 117 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1828 211 117 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1845 211 108 minecraft:air if block 1845 212 108 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1845 211 108 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1841 211 127 minecraft:air if block 1841 212 127 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1841 211 127 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1832 211 107 minecraft:air if block 1832 212 107 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1832 211 107 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1838 211 114 minecraft:air if block 1838 212 114 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1838 211 114 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1825 211 126 minecraft:air if block 1825 212 126 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1825 211 126 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1833 211 123 minecraft:air if block 1833 212 123 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1833 211 123 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1844 211 119 minecraft:air if block 1844 212 119 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1844 211 119 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1829 211 117 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1829 211 117 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v1b240 ftm_brick 2
