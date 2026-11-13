scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1601 95 29 minecraft:air if block 1601 96 29 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1601 95 29 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1618 95 19 minecraft:air if block 1618 96 19 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1618 95 19 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1614 95 39 minecraft:air if block 1614 96 39 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1614 95 39 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1598 95 19 minecraft:air if block 1598 96 19 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1598 95 19 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1598 95 39 minecraft:air if block 1598 96 39 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1598 95 39 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1608 95 22 minecraft:air if block 1608 96 22 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1608 95 22 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1615 95 28 minecraft:air if block 1615 96 28 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1615 95 28 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1606 95 35 minecraft:air if block 1606 96 35 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1606 95 35 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1602 95 29 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1602 95 29 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v3b235 ftm_brick 2
