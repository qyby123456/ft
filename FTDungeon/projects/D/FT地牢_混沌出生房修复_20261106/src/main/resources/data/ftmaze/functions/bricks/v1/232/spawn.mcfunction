scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1603 151 76 minecraft:air if block 1603 152 76 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1603 151 76 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1618 151 88 minecraft:air if block 1618 152 88 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1618 151 88 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1617 151 68 minecraft:air if block 1617 152 68 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1617 151 68 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1598 151 88 minecraft:air if block 1598 152 88 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1598 151 88 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1598 151 68 minecraft:air if block 1598 152 68 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1598 151 68 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1611 151 81 minecraft:air if block 1611 152 81 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1611 151 81 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1605 151 85 minecraft:air if block 1605 152 85 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1605 151 85 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1606 151 69 minecraft:air if block 1606 152 69 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1606 151 69 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1602 151 76 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1602 151 76 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v1b232 ftm_brick 2
