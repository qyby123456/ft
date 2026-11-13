scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1651 151 115 minecraft:air if block 1651 152 115 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1651 151 115 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1666 151 129 minecraft:air if block 1666 152 129 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1666 151 129 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1665 151 109 minecraft:air if block 1665 152 109 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1665 151 109 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1648 151 129 minecraft:air if block 1648 152 129 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1648 151 129 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1657 151 123 minecraft:air if block 1657 152 123 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1657 151 123 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1662 151 117 minecraft:air if block 1662 152 117 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1662 151 117 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1655 151 109 minecraft:air if block 1655 152 109 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1655 151 109 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1647 151 120 minecraft:air if block 1647 152 120 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1647 151 120 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1651 151 114 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1651 151 114 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v1b230 ftm_brick 2
