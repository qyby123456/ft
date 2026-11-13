scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1652 151 23 minecraft:air if block 1652 152 23 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1652 151 23 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1669 151 38 minecraft:air if block 1669 152 38 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1669 151 38 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1668 151 18 minecraft:air if block 1668 152 18 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1668 151 18 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1649 151 38 minecraft:air if block 1649 152 38 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1649 151 38 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1659 151 32 minecraft:air if block 1659 152 32 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1659 151 32 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1662 151 24 minecraft:air if block 1662 152 24 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1662 151 24 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1657 151 18 minecraft:air if block 1657 152 18 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1657 151 18 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1667 151 29 minecraft:air if block 1667 152 29 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1667 151 29 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1653 151 23 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1653 151 23 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v2b236 ftm_brick 2
