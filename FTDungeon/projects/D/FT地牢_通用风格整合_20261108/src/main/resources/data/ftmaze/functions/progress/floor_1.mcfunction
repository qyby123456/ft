execute store success score #open ftm_state run fill 289 151 221 291 154 221 minecraft:air
execute if score #open ftm_state matches 1 run scoreboard players set #floor1 ftm_state 1
execute if score #open ftm_state matches 1 run tellraw @a[tag=ftmaze.inside] {"text":"本层两位区域守卫已击败，楼梯封印解除。","color":"gold"}
