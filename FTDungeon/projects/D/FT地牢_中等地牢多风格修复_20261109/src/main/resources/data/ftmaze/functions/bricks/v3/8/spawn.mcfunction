scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 126 211 102 minecraft:air if block 126 212 102 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 126 211 102 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 137 211 99 minecraft:air if block 137 212 99 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 137 211 99 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 122 211 110 minecraft:air if block 122 212 110 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 122 211 110 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 131 211 108 minecraft:air if block 131 212 108 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 131 211 108 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 119 211 99 minecraft:air if block 119 212 99 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 119 211 99 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 130 211 98 minecraft:air if block 130 212 98 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 130 211 98 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 125 211 107 minecraft:air if block 125 212 107 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 125 211 107 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 133 211 101 minecraft:air if block 133 212 101 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 133 211 101 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 127 211 102 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 127 211 102 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v3b8 ftm_brick 2
