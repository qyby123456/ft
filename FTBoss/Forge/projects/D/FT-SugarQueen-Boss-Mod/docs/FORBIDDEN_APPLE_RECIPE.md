# 禁果：八种不同苹果食品（2026-09-19）

与其他食物 Boss 召唤物一致：工作台 3×3 有序配方，八种指定食品各 1 份，中心 1 个金币锅，产出 1 个 `ftboss:forbidden_apple`。禁果仍用于召唤苹果爵士，召唤逻辑不变。

| 左 | 中 | 右 |
| --- | --- | --- |
| 蜜饯苹果 | 苹果派切片（农夫乐事） | 苹果纸杯蛋糕 |
| 蜜渍苹果 | **金币锅** | 苹果果酱（整合包） |
| 苹果果汁（整合包） | 苹果泥（葡园酒香） | 苹果炖（丰饶食记） |

| 配方符号 | 物品 ID | 来源 |
| --- | --- | --- |
| A | `bountifulfares:candied_apple` | 丰饶食记 |
| B | `farmersdelight:apple_pie_slice` | 农夫乐事 |
| C | `bakery:apple_cupcake` | 馥郁烘焙 |
| D | `create:honeyed_apple` | 机械动力 |
| # | `farmingtales:gold_bucket` | 整合包金币锅 |
| E | `farmingtales:apple_jam_1` | 整合包自制食品 |
| F | `farmingtales:apple_juice_1` | 整合包自制饮料 |
| G | `vinery:apple_mash` | 葡园酒香，整合包烹饪锅配方 |
| H | `bountifulfares:apple_stew` | 丰饶食记，整合包烹饪锅配方 |

## 匹配规则

- 类型为 `minecraft:crafting_shaped`，形状 `ABC / D#E / FGH`，每格只接受指定物品，不是八格共享候选列表。
- 与原版有序配方一致，允许整体左右镜像；不允许任意换位、缺料、重复食品替代其他食品，2×2 背包合成也不能完成。
- 材料不满足时不产生配方结果，不扣除材料；取消旧的合成后清空产物事件处理。
- JEI 可按标准配方展示和填充，不依赖自定义配方序列化器。旧 `apple_products` 标签和 `distinct_shaped` 注册保留兼容，但不参与禁果制作。
- 不再使用普通苹果、金苹果充当食品格；不接受其他模组的同名苹果派切片、苹果果汁或苹果果酱替代。

## 配方缺失的实际原因与修正

- `kubejs/server_scripts/recipes/vinery/remove_grapes.js` 将 `vinery:apple_juice` 列为移除物品，并执行 `event.remove({ input: id })`。因此任何使用它的配方（包括旧禁果配方）都会在 KubeJS 配方事件中被删除，即使 JSON 解析成功、材料注册存在、JAR 已正确部署。
- `recipes/crafting/tabremove/tabremove.js` 还移除了 `brewinandchewin:apple_jelly`、`farmersdelight:apple_cider`、`bountifulfares:apple_compote_jar` 的产出配方。它们存在于模组 JAR，不代表在此整合包里仍可正常制作。
- 本次只替换禁果的四格材料，不修改或绕过整合包禁用规则，不另加同名 KubeJS 配方，不使用加载后强行补配方的处理。
- 整合包苹果果酱在 `startup_scripts/winemaking/jam.js` 注册，`cookpot.js` 的 `jamRecipe` 制作；苹果果汁在 `startup_scripts/winemaking/juice.js` 注册，`pouring.js` / Create 灌装制作。
- 苹果泥由 `pressing_and_vinery.js` 的 `farmingtales:vinery/apple_mash_from_cooking_pot` 制作；苹果炖由 `cookpot.js` 的烹饪锅配方制作。
- 早期将缺失归因于自定义序列化器或缓存，未排除整合包脚本干预，不能视为已证实的原因。本次先用原脚本复现旧配方被删除，再验证新配方不触发清理。

## 验证方式

- `node tools/verification/check_forbidden_apple_recipe.cjs`：固定八种物品、九格布局、参照配方、产出及移除事后拦截的静态校验；旧入口 `check_forbidden_apple.cjs` 转到同一校验。
- `node tools/verification/check_forbidden_apple_pack.cjs [整合包实例目录]`：回放实际安装的葡园清理、食品配方清理、标签清理、JEI 隐藏脚本，捕获输入/输出删除规则并检查禁果。旧版四种材料作为反例，必须复现苹果汁导致整条配方删除，新配方不得使用这些脚本停用/隐藏/清标签的材料；另回放三份制作脚本，确认四种替代食品都有实际产出配方，并检查两种自定义食品的启动注册。无本地整合包时明确跳过；显式指定的目录不存在则失败。
- `ForbiddenAppleRecipeTests`：读取实际 JSON，将外部模组材料一对一映射到九种原版测试物品，再调用真正的原版配方解析、匹配和网络序列化。覆盖正常、镜像、缺料、重复、换位、错误中心、2×2、批量材料和合成事件后结果保留；不会把测试替代材料写入正式资源。
- 物品注册、资源和整合包实际制作入口另外核对；不再把“JAR 内有模型/原始配方”视为在整合包可用的充分条件。脚本回放使用捕获 API 调用的 Node 测试环境，最小 GameTest 使用替代材料，两者都不等于完整客户端/JEI 实测。
