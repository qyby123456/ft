scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 838 151 167 minecraft:air if block 838 152 167 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 838 151 167 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 841 151 159 minecraft:air if block 841 152 159 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 841 151 159 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 843 151 171 minecraft:air if block 843 152 171 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 843 151 171 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 837 151 161 minecraft:air if block 837 152 161 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 837 151 161 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 837 151 167 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 837 151 167 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v1b326 ftm_brick 2
