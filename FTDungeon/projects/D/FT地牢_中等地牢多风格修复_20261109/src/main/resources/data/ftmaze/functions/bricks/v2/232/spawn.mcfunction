scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1601 157 58 minecraft:air if block 1601 158 58 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1601 157 58 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1618 157 72 minecraft:air if block 1618 158 72 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1618 157 72 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1616 157 52 minecraft:air if block 1616 158 52 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1616 157 52 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1598 157 72 minecraft:air if block 1598 158 72 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1598 157 72 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1608 157 66 minecraft:air if block 1608 158 66 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1608 157 66 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1612 157 59 minecraft:air if block 1612 158 59 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1612 157 59 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1605 157 52 minecraft:air if block 1605 158 52 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1605 157 52 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1616 157 64 minecraft:air if block 1616 158 64 minecraft:air store success score #spawnok ftm_state run summon minecraft:skeleton 1616 157 64 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1602 157 58 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1602 157 58 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v2b232 ftm_brick 2
