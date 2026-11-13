scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 835 151 162 minecraft:air if block 835 152 162 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 835 151 162 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 850 151 146 minecraft:air if block 850 152 146 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 850 151 146 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 830 151 146 minecraft:air if block 830 152 146 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 830 151 146 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 850 151 164 minecraft:air if block 850 152 164 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 850 151 164 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 840 151 151 minecraft:air if block 840 152 151 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 840 151 151 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 843 151 159 minecraft:air if block 843 152 159 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 843 151 159 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 830 151 156 minecraft:air if block 830 152 156 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 830 151 156 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 848 151 154 minecraft:air if block 848 152 154 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 848 151 154 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 835 151 161 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 835 151 161 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v2b187 ftm_brick 2
