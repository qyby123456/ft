scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 125 152 158 minecraft:air if block 125 153 158 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 125 152 158 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 137 152 147 minecraft:air if block 137 153 147 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 137 152 147 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 119 152 147 minecraft:air if block 119 153 147 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 119 152 147 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 136 152 162 minecraft:air if block 136 153 162 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 136 152 162 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 128 152 149 minecraft:air if block 128 153 149 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 128 152 149 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 119 152 162 minecraft:air if block 119 153 162 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 119 152 162 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 131 152 155 minecraft:air if block 131 153 155 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 131 152 155 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 120 152 154 minecraft:air if block 120 153 154 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 120 152 154 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 126 152 158 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 126 152 158 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v2b34 ftm_brick 2
