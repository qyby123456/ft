scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1602 91 115 minecraft:air if block 1602 92 115 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1602 91 115 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1618 91 129 minecraft:air if block 1618 92 129 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1618 91 129 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1617 91 109 minecraft:air if block 1617 92 109 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1617 91 109 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1598 91 129 minecraft:air if block 1598 92 129 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1598 91 129 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1608 91 124 minecraft:air if block 1608 92 124 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1608 91 124 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1614 91 118 minecraft:air if block 1614 92 118 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1614 91 118 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1598 91 109 minecraft:air if block 1598 92 109 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1598 91 109 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1607 91 110 minecraft:air if block 1607 92 110 minecraft:air store success score #spawnok ftm_state run summon minecraft:husk 1607 91 110 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1602 91 114 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1602 91 114 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v3b228 ftm_brick 2
