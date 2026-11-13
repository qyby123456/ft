scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 978 151 55 minecraft:air if block 978 152 55 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 978 151 55 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 991 151 70 minecraft:air if block 991 152 70 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 991 151 70 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 971 151 69 minecraft:air if block 971 152 69 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 971 151 69 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 982 151 65 minecraft:air if block 982 152 65 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 982 151 65 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 990 151 57 minecraft:air if block 990 152 57 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 990 151 57 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 971 151 58 minecraft:air if block 971 152 58 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 971 151 58 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 978 151 72 minecraft:air if block 978 152 72 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 978 151 72 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 985 151 52 minecraft:air if block 985 152 52 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 985 151 52 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 978 151 56 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 978 151 56 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v3b201 ftm_brick 2
