scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1601 157 25 minecraft:air if block 1601 158 25 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1601 157 25 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1618 157 17 minecraft:air if block 1618 158 17 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1618 157 17 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1613 157 33 minecraft:air if block 1613 158 33 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1613 157 33 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1606 157 16 minecraft:air if block 1606 158 16 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1606 157 16 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1611 157 23 minecraft:air if block 1611 158 23 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1611 157 23 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1598 157 33 minecraft:air if block 1598 158 33 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1598 157 33 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1598 157 18 minecraft:air if block 1598 158 18 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1598 157 18 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1606 157 30 minecraft:air if block 1606 158 30 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1606 157 30 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1602 157 25 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1602 157 25 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v2b235 ftm_brick 2
