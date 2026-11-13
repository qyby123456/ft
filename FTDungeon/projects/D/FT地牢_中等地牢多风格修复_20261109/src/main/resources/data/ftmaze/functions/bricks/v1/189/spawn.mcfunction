scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 785 151 98 minecraft:air if block 785 152 98 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 785 151 98 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 800 151 115 minecraft:air if block 800 152 115 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 800 151 115 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 782 151 114 minecraft:air if block 782 152 114 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 782 151 114 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 800 151 95 minecraft:air if block 800 152 95 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 800 151 95 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 792 151 107 minecraft:air if block 792 152 107 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 792 151 107 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 801 151 105 minecraft:air if block 801 152 105 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 801 151 105 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 781 151 104 minecraft:air if block 781 152 104 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 781 151 104 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 794 151 99 minecraft:air if block 794 152 99 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 794 151 99 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 785 151 99 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 785 151 99 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v1b189 ftm_brick 2
