scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 207 31 121 minecraft:air if block 207 32 121 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 207 31 121 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 218 31 131 minecraft:air if block 218 32 131 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 218 31 131 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 222 31 117 minecraft:air if block 222 32 117 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 222 31 117 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 205 31 131 minecraft:air if block 205 32 131 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 205 31 131 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 211 31 127 minecraft:air if block 211 32 127 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 211 31 127 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 212 31 116 minecraft:air if block 212 32 116 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 212 31 116 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 216 31 122 minecraft:air if block 216 32 122 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 216 31 122 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 202 31 117 minecraft:air if block 202 32 117 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 202 31 117 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 206 31 121 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 206 31 121 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v3b93 ftm_brick 2
