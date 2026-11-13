scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1651 211 115 minecraft:air if block 1651 212 115 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1651 211 115 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1666 211 129 minecraft:air if block 1666 212 129 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1666 211 129 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1665 211 109 minecraft:air if block 1665 212 109 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1665 211 109 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1648 211 129 minecraft:air if block 1648 212 129 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1648 211 129 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1657 211 123 minecraft:air if block 1657 212 123 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1657 211 123 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1662 211 117 minecraft:air if block 1662 212 117 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1662 211 117 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1655 211 109 minecraft:air if block 1655 212 109 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1655 211 109 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1647 211 120 minecraft:air if block 1647 212 120 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1647 211 120 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1651 211 114 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1651 211 114 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v0b230 ftm_brick 2
