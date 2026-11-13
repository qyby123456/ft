scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 151 151 121 minecraft:air if block 151 152 121 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 151 151 121 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 162 151 131 minecraft:air if block 162 152 131 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 162 151 131 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 166 151 117 minecraft:air if block 166 152 117 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 166 151 117 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 149 151 131 minecraft:air if block 149 152 131 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 149 151 131 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 155 151 127 minecraft:air if block 155 152 127 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 155 151 127 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 156 151 116 minecraft:air if block 156 152 116 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 156 151 116 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 160 151 122 minecraft:air if block 160 152 122 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 160 151 122 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 146 151 117 minecraft:air if block 146 152 117 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 146 151 117 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 150 151 121 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 150 151 121 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v0b37 ftm_brick 2
