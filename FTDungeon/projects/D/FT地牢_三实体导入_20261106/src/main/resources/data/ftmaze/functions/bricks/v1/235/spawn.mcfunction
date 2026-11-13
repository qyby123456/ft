scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1601 155 29 minecraft:air if block 1601 156 29 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1601 155 29 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1618 155 19 minecraft:air if block 1618 156 19 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1618 155 19 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1614 155 39 minecraft:air if block 1614 156 39 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1614 155 39 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1598 155 19 minecraft:air if block 1598 156 19 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1598 155 19 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1598 155 39 minecraft:air if block 1598 156 39 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1598 155 39 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1608 155 22 minecraft:air if block 1608 156 22 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1608 155 22 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1615 155 28 minecraft:air if block 1615 156 28 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1615 155 28 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1606 155 35 minecraft:air if block 1606 156 35 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1606 155 35 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1602 155 29 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1602 155 29 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v1b235 ftm_brick 2
