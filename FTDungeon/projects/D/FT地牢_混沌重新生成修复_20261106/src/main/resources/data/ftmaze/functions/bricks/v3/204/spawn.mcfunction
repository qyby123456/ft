scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 974 151 24 minecraft:air if block 974 152 24 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 974 151 24 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 991 151 38 minecraft:air if block 991 152 38 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 991 151 38 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 989 151 18 minecraft:air if block 989 152 18 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 989 151 18 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 971 151 38 minecraft:air if block 971 152 38 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 971 151 38 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 981 151 32 minecraft:air if block 981 152 32 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 981 151 32 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 985 151 25 minecraft:air if block 985 152 25 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 985 151 25 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 978 151 18 minecraft:air if block 978 152 18 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 978 151 18 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 989 151 30 minecraft:air if block 989 152 30 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 989 151 30 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 975 151 24 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 975 151 24 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v3b204 ftm_brick 2
