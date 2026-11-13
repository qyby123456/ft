scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1603 211 76 minecraft:air if block 1603 212 76 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1603 211 76 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1618 211 88 minecraft:air if block 1618 212 88 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1618 211 88 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1617 211 68 minecraft:air if block 1617 212 68 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1617 211 68 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1598 211 88 minecraft:air if block 1598 212 88 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1598 211 88 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1598 211 68 minecraft:air if block 1598 212 68 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1598 211 68 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1611 211 81 minecraft:air if block 1611 212 81 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1611 211 81 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1605 211 85 minecraft:air if block 1605 212 85 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1605 211 85 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1606 211 69 minecraft:air if block 1606 212 69 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1606 211 69 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1602 211 76 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1602 211 76 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v0b232 ftm_brick 2
