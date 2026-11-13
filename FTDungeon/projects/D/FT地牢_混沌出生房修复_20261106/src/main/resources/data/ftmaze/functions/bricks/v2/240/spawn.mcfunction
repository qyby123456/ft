scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1828 31 117 minecraft:air if block 1828 32 117 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1828 31 117 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1845 31 108 minecraft:air if block 1845 32 108 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1845 31 108 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1841 31 127 minecraft:air if block 1841 32 127 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1841 31 127 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1832 31 107 minecraft:air if block 1832 32 107 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1832 31 107 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1838 31 114 minecraft:air if block 1838 32 114 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1838 31 114 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1825 31 126 minecraft:air if block 1825 32 126 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1825 31 126 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1833 31 123 minecraft:air if block 1833 32 123 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1833 31 123 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1844 31 119 minecraft:air if block 1844 32 119 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 1844 31 119 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1829 31 117 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1829 31 117 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v2b240 ftm_brick 2
