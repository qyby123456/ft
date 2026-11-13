scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 99 151 97 minecraft:air if block 99 152 97 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 99 151 97 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 101 151 105 minecraft:air if block 101 152 105 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 101 151 105 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 99 151 98 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 99 151 98 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v3b28 ftm_brick 2
