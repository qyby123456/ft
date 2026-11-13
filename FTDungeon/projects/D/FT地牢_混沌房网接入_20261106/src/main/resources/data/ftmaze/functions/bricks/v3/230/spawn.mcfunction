scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1651 91 115 minecraft:air if block 1651 92 115 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1651 91 115 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1666 91 129 minecraft:air if block 1666 92 129 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1666 91 129 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1665 91 109 minecraft:air if block 1665 92 109 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1665 91 109 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1648 91 129 minecraft:air if block 1648 92 129 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1648 91 129 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1657 91 123 minecraft:air if block 1657 92 123 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1657 91 123 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1662 91 117 minecraft:air if block 1662 92 117 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1662 91 117 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1655 91 109 minecraft:air if block 1655 92 109 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1655 91 109 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1647 91 120 minecraft:air if block 1647 92 120 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1647 91 120 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1651 91 114 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1651 91 114 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v3b230 ftm_brick 2
