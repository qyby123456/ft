scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 65 31 148 minecraft:air if block 65 32 148 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 65 31 148 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 81 31 158 minecraft:air if block 81 32 158 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 81 31 158 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 78 31 147 minecraft:air if block 78 32 147 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 78 31 147 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 68 31 158 minecraft:air if block 68 32 158 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 68 31 158 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 72 31 152 minecraft:air if block 72 32 152 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 72 31 152 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 63 31 155 minecraft:air if block 63 32 155 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 63 31 155 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 71 31 146 minecraft:air if block 71 32 146 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 71 31 146 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 74 31 157 minecraft:air if block 74 32 157 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 74 31 157 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 66 31 148 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 66 31 148 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v3b74 ftm_brick 2
