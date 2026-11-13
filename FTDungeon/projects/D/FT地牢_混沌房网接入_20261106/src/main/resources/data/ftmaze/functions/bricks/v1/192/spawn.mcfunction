scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 833 157 58 minecraft:air if block 833 158 58 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 833 157 58 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 850 157 72 minecraft:air if block 850 158 72 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 850 157 72 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 848 157 52 minecraft:air if block 848 158 52 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 848 157 52 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 830 157 72 minecraft:air if block 830 158 72 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 830 157 72 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 840 157 66 minecraft:air if block 840 158 66 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 840 157 66 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 844 157 59 minecraft:air if block 844 158 59 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 844 157 59 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 837 157 52 minecraft:air if block 837 158 52 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 837 157 52 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 848 157 64 minecraft:air if block 848 158 64 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 848 157 64 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 834 157 58 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 834 157 58 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v1b192 ftm_brick 2
