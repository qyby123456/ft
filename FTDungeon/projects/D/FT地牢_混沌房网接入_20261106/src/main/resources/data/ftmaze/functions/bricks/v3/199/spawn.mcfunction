scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 977 151 98 minecraft:air if block 977 152 98 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 977 151 98 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 992 151 115 minecraft:air if block 992 152 115 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 992 151 115 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 974 151 114 minecraft:air if block 974 152 114 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 974 151 114 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 992 151 95 minecraft:air if block 992 152 95 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 992 151 95 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 984 151 107 minecraft:air if block 984 152 107 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 984 151 107 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 993 151 105 minecraft:air if block 993 152 105 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 993 151 105 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 973 151 104 minecraft:air if block 973 152 104 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 973 151 104 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 986 151 99 minecraft:air if block 986 152 99 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 986 151 99 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 977 151 99 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 977 151 99 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v3b199 ftm_brick 2
