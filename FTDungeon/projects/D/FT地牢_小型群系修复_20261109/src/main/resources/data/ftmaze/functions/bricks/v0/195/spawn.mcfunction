scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 833 215 29 minecraft:air if block 833 216 29 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 833 215 29 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 850 215 19 minecraft:air if block 850 216 19 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 850 215 19 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 846 215 39 minecraft:air if block 846 216 39 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 846 215 39 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 830 215 19 minecraft:air if block 830 216 19 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 830 215 19 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 830 215 39 minecraft:air if block 830 216 39 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 830 215 39 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 840 215 22 minecraft:air if block 840 216 22 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 840 215 22 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 847 215 28 minecraft:air if block 847 216 28 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 847 215 28 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 838 215 35 minecraft:air if block 838 216 35 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 838 215 35 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 834 215 29 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 834 215 29 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v0b195 ftm_brick 2
