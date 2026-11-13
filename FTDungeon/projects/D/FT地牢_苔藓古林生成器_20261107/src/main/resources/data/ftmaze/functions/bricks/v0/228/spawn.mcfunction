scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1602 211 115 minecraft:air if block 1602 212 115 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1602 211 115 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1618 211 129 minecraft:air if block 1618 212 129 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1618 211 129 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1617 211 109 minecraft:air if block 1617 212 109 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1617 211 109 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1598 211 129 minecraft:air if block 1598 212 129 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1598 211 129 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1608 211 124 minecraft:air if block 1608 212 124 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1608 211 124 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1614 211 118 minecraft:air if block 1614 212 118 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1614 211 118 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1598 211 109 minecraft:air if block 1598 212 109 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1598 211 109 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1607 211 110 minecraft:air if block 1607 212 110 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1607 211 110 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1602 211 114 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1602 211 114 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v0b228 ftm_brick 2
