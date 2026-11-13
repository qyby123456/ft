scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 121 151 148 minecraft:air if block 121 152 148 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 121 151 148 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 137 151 158 minecraft:air if block 137 152 158 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 137 151 158 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 134 151 147 minecraft:air if block 134 152 147 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 134 151 147 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 124 151 158 minecraft:air if block 124 152 158 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 124 151 158 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 128 151 152 minecraft:air if block 128 152 152 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 128 151 152 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 119 151 155 minecraft:air if block 119 152 155 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 119 151 155 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 127 151 146 minecraft:air if block 127 152 146 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 127 151 146 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 130 151 157 minecraft:air if block 130 152 157 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 130 151 157 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 122 151 148 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 122 151 148 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v0b34 ftm_brick 2
