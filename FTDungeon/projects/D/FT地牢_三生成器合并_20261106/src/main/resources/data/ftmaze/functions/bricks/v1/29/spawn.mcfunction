scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 94 151 122 minecraft:air if block 94 152 122 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 94 151 122 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 105 151 132 minecraft:air if block 105 152 132 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 105 151 132 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 109 151 118 minecraft:air if block 109 152 118 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 109 151 118 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 90 151 132 minecraft:air if block 90 152 132 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 90 151 132 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 99 151 116 minecraft:air if block 99 152 116 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 99 151 116 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 97 151 129 minecraft:air if block 97 152 129 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 97 151 129 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 102 151 124 minecraft:air if block 102 152 124 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 102 151 124 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 108 151 126 minecraft:air if block 108 152 126 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 108 151 126 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 94 151 121 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 94 151 121 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v1b29 ftm_brick 2
