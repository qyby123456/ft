scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 105 31 177 minecraft:air if block 105 32 177 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 105 31 177 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 91 31 184 minecraft:air if block 91 32 184 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 91 31 184 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 94 31 174 minecraft:air if block 94 32 174 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 94 31 174 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 109 31 186 minecraft:air if block 109 32 186 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 109 31 186 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 98 31 180 minecraft:air if block 98 32 180 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 98 31 180 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 101 31 172 minecraft:air if block 101 32 172 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 101 31 172 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 100 31 186 minecraft:air if block 100 32 186 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 100 31 186 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 103 31 182 minecraft:air if block 103 32 182 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 103 31 182 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 106 31 177 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 106 31 177 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v1b79 ftm_brick 2
