scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 133 151 93 minecraft:air if block 133 152 93 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 133 151 93 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 119 151 100 minecraft:air if block 119 152 100 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 119 151 100 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 122 151 90 minecraft:air if block 122 152 90 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 122 151 90 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 137 151 102 minecraft:air if block 137 152 102 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 137 151 102 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 126 151 96 minecraft:air if block 126 152 96 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 126 151 96 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 129 151 88 minecraft:air if block 129 152 88 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 129 151 88 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 128 151 102 minecraft:air if block 128 152 102 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 128 151 102 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 131 151 98 minecraft:air if block 131 152 98 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 131 151 98 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 134 151 93 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 134 151 93 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v1b32 ftm_brick 2
