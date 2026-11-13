scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 149 31 96 minecraft:air if block 149 32 96 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 149 31 96 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 163 31 105 minecraft:air if block 163 32 105 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 163 31 105 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 160 31 93 minecraft:air if block 160 32 93 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 160 31 93 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 153 31 103 minecraft:air if block 153 32 103 minecraft:air store success score #spawnok ftm_state run summon minecraft:stray 153 31 103 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 150 31 96 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 150 31 96 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v0b84 ftm_brick 2
