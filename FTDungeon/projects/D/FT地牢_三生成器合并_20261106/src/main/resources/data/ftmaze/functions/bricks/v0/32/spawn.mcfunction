scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 122 151 94 minecraft:air if block 122 152 94 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 122 151 94 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 133 151 104 minecraft:air if block 133 152 104 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 133 151 104 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 137 151 90 minecraft:air if block 137 152 90 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 137 151 90 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 118 151 104 minecraft:air if block 118 152 104 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 118 151 104 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 127 151 88 minecraft:air if block 127 152 88 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 127 151 88 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 125 151 101 minecraft:air if block 125 152 101 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 125 151 101 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 130 151 96 minecraft:air if block 130 152 96 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 130 151 96 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 136 151 98 minecraft:air if block 136 152 98 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 136 151 98 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 122 151 93 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 122 151 93 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v0b32 ftm_brick 2
