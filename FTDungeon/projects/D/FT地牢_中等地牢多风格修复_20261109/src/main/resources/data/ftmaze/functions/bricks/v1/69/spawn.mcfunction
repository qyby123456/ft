scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 207 91 121 minecraft:air if block 207 92 121 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 207 91 121 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 218 91 131 minecraft:air if block 218 92 131 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 218 91 131 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 222 91 117 minecraft:air if block 222 92 117 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 222 91 117 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 205 91 131 minecraft:air if block 205 92 131 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 205 91 131 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 211 91 127 minecraft:air if block 211 92 127 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 211 91 127 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 212 91 116 minecraft:air if block 212 92 116 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 212 91 116 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 216 91 122 minecraft:air if block 216 92 122 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 216 91 122 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 202 91 117 minecraft:air if block 202 92 117 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 202 91 117 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 206 91 121 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 206 91 121 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v1b69 ftm_brick 2
