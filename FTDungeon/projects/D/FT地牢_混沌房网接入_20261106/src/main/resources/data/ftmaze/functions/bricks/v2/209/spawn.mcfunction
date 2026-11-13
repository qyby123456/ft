scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1169 91 98 minecraft:air if block 1169 92 98 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1169 91 98 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1184 91 115 minecraft:air if block 1184 92 115 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1184 91 115 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1166 91 114 minecraft:air if block 1166 92 114 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1166 91 114 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1184 91 95 minecraft:air if block 1184 92 95 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1184 91 95 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1176 91 107 minecraft:air if block 1176 92 107 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1176 91 107 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1185 91 105 minecraft:air if block 1185 92 105 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1185 91 105 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1165 91 104 minecraft:air if block 1165 92 104 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1165 91 104 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1178 91 99 minecraft:air if block 1178 92 99 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1178 91 99 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1169 91 99 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1169 91 99 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v2b209 ftm_brick 2
