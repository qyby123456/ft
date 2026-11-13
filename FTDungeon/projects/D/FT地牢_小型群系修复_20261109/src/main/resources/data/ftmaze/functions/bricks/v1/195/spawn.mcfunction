scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 833 157 25 minecraft:air if block 833 158 25 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 833 157 25 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 850 157 17 minecraft:air if block 850 158 17 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 850 157 17 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 845 157 33 minecraft:air if block 845 158 33 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 845 157 33 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 838 157 16 minecraft:air if block 838 158 16 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 838 157 16 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 843 157 23 minecraft:air if block 843 158 23 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 843 157 23 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 830 157 33 minecraft:air if block 830 158 33 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 830 157 33 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 830 157 18 minecraft:air if block 830 158 18 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 830 157 18 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 838 157 30 minecraft:air if block 838 158 30 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 838 157 30 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 834 157 25 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 834 157 25 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v1b195 ftm_brick 2
