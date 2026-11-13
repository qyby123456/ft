scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 97 32 186 minecraft:air if block 97 33 186 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 97 32 186 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 109 32 175 minecraft:air if block 109 33 175 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 109 32 175 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 91 32 175 minecraft:air if block 91 33 175 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 91 32 175 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 108 32 190 minecraft:air if block 108 33 190 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 108 32 190 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 100 32 177 minecraft:air if block 100 33 177 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 100 32 177 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 91 32 190 minecraft:air if block 91 33 190 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 91 32 190 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 103 32 183 minecraft:air if block 103 33 183 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 103 32 183 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 92 32 182 minecraft:air if block 92 33 182 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 92 32 182 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 98 32 186 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 98 32 186 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v3b79 ftm_brick 2
