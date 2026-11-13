# 指定十二款重制 · 1.1.0

本次只处理原24款中的 **06–13、15、16、21、23**，保留原注册ID和编号，不增加方块，不覆盖旧版交付。

| 原编号 | ID | 重制方向 |
|---|---|---|
| 06 | prison_brick_slab | 保留原生台阶几何，重做上表面、底面、围边三种石刻材质，双层台阶也匹配新材质 |
| 07 | prison_fluted_pillar | 重塑柱基、柱冠和真实凹槽 |
| 08 | prison_cornice | 分层挑檐、成对支撑和负空间 |
| 09 | prison_supply_crate | 闭合凹板箱体、锁扣和护边 |
| 10 | prison_ironbound_barrel | 收口鼓腹轮廓、桶盖和铁箍 |
| 11 | prison_shackled_post | 刑柱比例和真实开口铁镣 |
| 12 | prison_weapon_rack | 可辨识兵刃、武器与支架分层 |
| 13 | prison_alchemy_shelf | 有瓶颈药瓶、书本与多层陈设 |
| 15 | prison_ossuary_niche | 支拱深龛、颅骨与束骨 |
| 16 | prison_warden_lectern | 账台比例、开卷账簿与书页 |
| 21 | prison_alchemical_lamp | 冷暖双胆、连通管桥和蒸馏仪器结构 |
| 23 | prison_rune_beacon | 束缚符文石碑、封环和分层基座 |

制作分为三个独立聊天；主聊天统一集成、重新导出并复验。重制贴图使用独立的 `remake-<lane>` 材质集，不修改旧共用贴图造成旁及变化。原始24款定义/图集保留在 `lanes/`，重制源位于 `revisions/`，以保持可复现。先运行各 `revisions/<lane>/make_assets.py`，再运行 `forge-project/tools/export_kit.py`。

本包仍包括全部24款，以便直接替换旧版模组，**同一实例不要同时安装1.0.0与1.1.0**。本次不自动安装、不修改正式FT工程、不打开游戏世界、不占用其他任务的Blockbench。

对照图 `preview/before-after-12.png` 来自两版实际导出JSON的同相机、同固定光照软件渲染，不是游戏截图。`qa/remake-scope.json`核对指定12款的变化和其余12款模型/贴图不变；完整检查结果以最终 `qa/` 报告为准。离线检查不等于实际游戏手持、光传播、模组互操作或FPS测试。
