scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 21 31 147 minecraft:air if block 21 32 147 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 21 31 147 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 35 31 157 minecraft:air if block 35 32 157 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 35 31 157 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 32 31 145 minecraft:air if block 32 32 145 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 32 31 145 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 27 31 153 minecraft:air if block 27 32 153 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 27 31 153 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 22 31 147 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 22 31 147 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v0b149 ftm_brick 2
