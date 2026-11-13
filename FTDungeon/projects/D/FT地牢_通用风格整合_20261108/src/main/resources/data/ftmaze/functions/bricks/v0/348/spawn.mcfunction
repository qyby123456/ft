scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 819 151 198 minecraft:air if block 819 152 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 819 151 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 809 151 202 minecraft:air if block 809 152 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 809 151 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 824 151 202 minecraft:air if block 824 152 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 824 151 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 812 151 198 minecraft:air if block 812 152 198 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 812 151 198 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 816 151 202 minecraft:air if block 816 152 202 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 816 151 202 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 813 151 201 minecraft:air if block 813 152 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 813 151 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 815 151 199 minecraft:air if block 815 152 199 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 815 151 199 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 820 151 201 minecraft:air if block 820 152 201 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 820 151 201 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 820 151 198 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 820 151 198 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v0b348 ftm_brick 2
