scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 70 31 158 minecraft:air if block 70 32 158 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 70 31 158 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 81 31 155 minecraft:air if block 81 32 155 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 81 31 155 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 66 31 166 minecraft:air if block 66 32 166 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 66 31 166 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 75 31 164 minecraft:air if block 75 32 164 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 75 31 164 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 63 31 155 minecraft:air if block 63 32 155 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 63 31 155 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 74 31 154 minecraft:air if block 74 32 154 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 74 31 154 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 69 31 163 minecraft:air if block 69 32 163 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 69 31 163 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 77 31 157 minecraft:air if block 77 32 157 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 77 31 157 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 71 31 158 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 71 31 158 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v0b74 ftm_brick 2
