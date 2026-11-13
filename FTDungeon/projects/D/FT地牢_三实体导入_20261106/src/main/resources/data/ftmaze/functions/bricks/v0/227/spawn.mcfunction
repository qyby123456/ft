scoreboard players set #brickok ftm_state 0
scoreboard players set #spawnok ftm_state 0
execute if block 1603 211 162 minecraft:air if block 1603 212 162 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1603 211 162 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1618 211 146 minecraft:air if block 1618 212 146 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1618 211 146 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1598 211 146 minecraft:air if block 1598 212 146 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1598 211 146 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1618 211 164 minecraft:air if block 1618 212 164 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1618 211 164 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1608 211 151 minecraft:air if block 1608 212 151 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1608 211 151 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1611 211 159 minecraft:air if block 1611 212 159 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1611 211 159 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1598 211 156 minecraft:air if block 1598 212 156 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1598 211 156 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
scoreboard players set #spawnok ftm_state 0
execute if block 1616 211 154 minecraft:air if block 1616 212 154 minecraft:air store success score #spawnok ftm_state run summon minecraft:zombie 1616 211 154 {Tags:["ftmaze.brickmob"],PersistenceRequired:1b,CanPickUpLoot:0b}
scoreboard players operation #brickok ftm_state += #spawnok ftm_state
execute if score #brickok ftm_state matches 1.. if block 1603 211 161 ftdungeonmobs:dungeon_spawn_point[spent=false] run setblock 1603 211 161 ftdungeonmobs:dungeon_spawn_point[spent=true]
execute if score #brickok ftm_state matches 1.. run scoreboard players set #v0b227 ftm_brick 2
