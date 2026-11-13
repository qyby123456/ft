scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 833 151 135 minecraft:air if block 833 152 135 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 833 151 135 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 850 151 148 minecraft:air if block 850 152 148 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 850 151 148 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 848 151 130 minecraft:air if block 848 152 130 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 848 151 130 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 830 151 148 minecraft:air if block 830 152 148 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 830 151 148 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 840 151 143 minecraft:air if block 840 152 143 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 840 151 143 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 838 151 129 minecraft:air if block 838 152 129 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 838 151 129 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 843 151 135 minecraft:air if block 843 152 135 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 843 151 135 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 848 151 140 minecraft:air if block 848 152 140 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 848 151 140 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 834 151 135 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 834 151 135 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v1b187 ftm_brick 2
