scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1601 215 29 minecraft:air if block 1601 216 29 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1601 215 29 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1618 215 19 minecraft:air if block 1618 216 19 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1618 215 19 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1614 215 39 minecraft:air if block 1614 216 39 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1614 215 39 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1598 215 19 minecraft:air if block 1598 216 19 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1598 215 19 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1598 215 39 minecraft:air if block 1598 216 39 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1598 215 39 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1608 215 22 minecraft:air if block 1608 216 22 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1608 215 22 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1615 215 28 minecraft:air if block 1615 216 28 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1615 215 28 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1606 215 35 minecraft:air if block 1606 216 35 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1606 215 35 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1602 215 29 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1602 215 29 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v0b235 ftm_brick 2
