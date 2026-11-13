scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1025 151 135 minecraft:air if block 1025 152 135 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1025 151 135 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1042 151 148 minecraft:air if block 1042 152 148 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1042 151 148 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1040 151 130 minecraft:air if block 1040 152 130 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1040 151 130 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1022 151 148 minecraft:air if block 1022 152 148 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1022 151 148 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1032 151 143 minecraft:air if block 1032 152 143 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1032 151 143 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1030 151 129 minecraft:air if block 1030 152 129 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1030 151 129 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1035 151 135 minecraft:air if block 1035 152 135 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1035 151 135 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1040 151 140 minecraft:air if block 1040 152 140 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1040 151 140 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1026 151 135 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1026 151 135 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v3b197 ftm_brick 2
