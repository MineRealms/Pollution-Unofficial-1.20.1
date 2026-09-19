# Pollution 无配方产出物品/方块审计

- 审计日期：2026-09-19
- 数据来源：`run/pollution-recipe-dump.txt`（服务器启动后从运行时 `RecipeManager` 全量转储）
- 审计脚本：`tools/audit_unobtainable.py`
- 物品注册总数（`pollution` 命名空间）：**3062**
- 无任何配方产出的物品数：**1450**
- 其中世界生成矿石：**1040**（可挖掘获得，仅无配方）
- 其余（无配方且非世界生成矿石）：**410**

## 分类统计

| 分类 | 数量 | 说明 |
| --- | ---: | --- |
| 机器/多方块 | 181 | `MetaMachineItem`，含单方块机器、多方块控制器、仓室 |
| 方块 | 1058 | 其中世界生成矿石 1040、结构/装饰/植物方块 18 |
| 物品 | 88 | 普通物品、材料部件、电路、饰品等 |
| 桶/流体容器 | 123 | GT 流体桶（`GTBucketItem`） |
| 其他 | 0 | 未归入以上分类 |
| 无对应物品的方块 | 1 | 注册了方块但无物品形态 |

## 说明与注意事项（Caveats）

1. **本表只统计“有配方产出”**：世界生成（矿石、植物）、生物掉落、结构箱子、任务奖励、JEI 隐藏物品等获取途径不计入配方。
2. **世界生成矿石 1040 项**：这些是 GT 材料矿块，靠挖矿获得，没有（也不需要）配方，属于正常现象。
3. **桶/流体容器**：`GTBucketItem` 没有配方产出；GT 的桶可右键流体源拾取，但 Pollution 的 GT 流体没有可放置的源方块，因此实际上只能创造模式获得。
4. **标签输出已展开**：GT 配方输出以 `Ingredient` 存储，转储时通过 `Ingredient.getItems()` 展开为具体物品，所以“标签产出的物品”已计入可制造。
5. **KubeJS 运行时改动已包含**：转储读取的是 `RecipeManager` 的最终状态。
6. 转储期间 `getResultItem`/输入展开异常：**0** 个。
7. **机器/仓室缺失多为移植未完成**：例如 Aspect Tank、Flux Muffler、Infused Fluid Hatch 全等级无配方；Mana 输入仓只有 1A 等级有配方，4A/16A/64A 与输出仓、无线仓全部缺失；部分多方块控制器（Magic Greenhouse、Magic Mega Turbine、Bot Distillery、Mana Plate、Node Fusion Reactor 等）无配方。
8. `pollution:test`、`pollution:test_item` 为调试/占位物品。
9. 大量材料部件（dust/plate/ingot 等）的缺失需要人工复核上游配方链，本表只保证“运行时确实没有配方产出”。
10. 名称解析使用的 GT 语言文件：`C:\Users\Administrator\.gradle\caches\modules-2\files-2.1\com.gregtechceu.gtceu\gtceu-1.20.1\7.5.3\2e9de016b74826d43f0d4e619a755e8ec0253784\gtceu-1.20.1-7.5.3.jar`。

## 机器/多方块（181）

| 注册名 (ID) | 英文名 | 中文名 | 备注 |
| --- | --- | --- | --- |
| `pollution:bot_distillery` | Terra Distillery | 泰拉蒸馏塔 | 无任何配方引用 |
| `pollution:ev_aspect_tank` | §5EV Aspect Tank | 超级源质缸 IV（§5EV§r） | 无任何配方引用 |
| `pollution:ev_flux_muffler` | §5EV Flux Muffler | 净化消声仓（§5EV§r） | 无任何配方引用 |
| `pollution:ev_infused_fluid_hatch` | §5EV Infused Fluid Hatch | 源质流体输入仓（§5EV§r） | 无任何配方引用 |
| `pollution:ev_mana_generator` | §5EV Mana Generator | 脉冲魔力发电机 | 无任何配方引用 |
| `pollution:ev_mana_input_hatch_16a` | §5EV Mana Input Hatch (16A) | 16A 魔力输入仓（§5EV§r） | 无任何配方引用 |
| `pollution:ev_mana_input_hatch_4a` | §5EV Mana Input Hatch (4A) | 4A 魔力输入仓（§5EV§r） | 无任何配方引用 |
| `pollution:ev_mana_input_hatch_64a` | §5EV Mana Input Hatch (64A) | 64A 魔力输入仓（§5EV§r） | 无任何配方引用 |
| `pollution:ev_mana_output_hatch_16a` | §5EV Mana Output Hatch (16A) | 16A 魔力输出仓（§5EV§r） | 无任何配方引用 |
| `pollution:ev_mana_output_hatch_1a` | §5EV Mana Output Hatch (1A) | 1A 魔力输出仓（§5EV§r） | 无任何配方引用 |
| `pollution:ev_mana_output_hatch_4a` | §5EV Mana Output Hatch (4A) | 4A 魔力输出仓（§5EV§r） | 无任何配方引用 |
| `pollution:ev_mana_output_hatch_64a` | §5EV Mana Output Hatch (64A) | 64A 魔力输出仓（§5EV§r） | 无任何配方引用 |
| `pollution:ev_wireless_mana_input_hatch_16a` | §5EV Wireless Mana Input Hatch (16A) | 无线 16A 魔力输入仓（§5EV§r） | 无任何配方引用 |
| `pollution:ev_wireless_mana_input_hatch_1a` | §5EV Wireless Mana Input Hatch (1A) | 无线 1A 魔力输入仓（§5EV§r） | 无任何配方引用 |
| `pollution:ev_wireless_mana_input_hatch_4a` | §5EV Wireless Mana Input Hatch (4A) | 无线 4A 魔力输入仓（§5EV§r） | 无任何配方引用 |
| `pollution:ev_wireless_mana_input_hatch_64a` | §5EV Wireless Mana Input Hatch (64A) | 无线 64A 魔力输入仓（§5EV§r） | 无任何配方引用 |
| `pollution:ev_wireless_mana_output_hatch_16a` | §5EV Wireless Mana Output Hatch (16A) | 无线 16A 魔力输出仓（§5EV§r） | 无任何配方引用 |
| `pollution:ev_wireless_mana_output_hatch_1a` | §5EV Wireless Mana Output Hatch (1A) | 无线 1A 魔力输出仓（§5EV§r） | 无任何配方引用 |
| `pollution:ev_wireless_mana_output_hatch_4a` | §5EV Wireless Mana Output Hatch (4A) | 无线 4A 魔力输出仓（§5EV§r） | 无任何配方引用 |
| `pollution:ev_wireless_mana_output_hatch_64a` | §5EV Wireless Mana Output Hatch (64A) | 无线 64A 魔力输出仓（§5EV§r） | 无任何配方引用 |
| `pollution:hv_aspect_tank` | §6HV Aspect Tank | 超级源质缸 III（§6HV§r） | 无任何配方引用 |
| `pollution:hv_flux_muffler` | §6HV Flux Muffler | 净化消声仓（§6HV§r） | 无任何配方引用 |
| `pollution:hv_infused_fluid_hatch` | §6HV Infused Fluid Hatch | 源质流体输入仓（§6HV§r） | 无任何配方引用 |
| `pollution:hv_mana_generator` | §6HV Mana Generator | 涡轮魔力发电机 | 无任何配方引用 |
| `pollution:hv_mana_input_hatch_16a` | §6HV Mana Input Hatch (16A) | 16A 魔力输入仓（§6HV§r） | 无任何配方引用 |
| `pollution:hv_mana_input_hatch_4a` | §6HV Mana Input Hatch (4A) | 4A 魔力输入仓（§6HV§r） | 无任何配方引用 |
| `pollution:hv_mana_input_hatch_64a` | §6HV Mana Input Hatch (64A) | 64A 魔力输入仓（§6HV§r） | 无任何配方引用 |
| `pollution:hv_mana_output_hatch_16a` | §6HV Mana Output Hatch (16A) | 16A 魔力输出仓（§6HV§r） | 无任何配方引用 |
| `pollution:hv_mana_output_hatch_1a` | §6HV Mana Output Hatch (1A) | 1A 魔力输出仓（§6HV§r） | 无任何配方引用 |
| `pollution:hv_mana_output_hatch_4a` | §6HV Mana Output Hatch (4A) | 4A 魔力输出仓（§6HV§r） | 无任何配方引用 |
| `pollution:hv_mana_output_hatch_64a` | §6HV Mana Output Hatch (64A) | 64A 魔力输出仓（§6HV§r） | 无任何配方引用 |
| `pollution:hv_wireless_mana_input_hatch_16a` | §6HV Wireless Mana Input Hatch (16A) | 无线 16A 魔力输入仓（§6HV§r） | 无任何配方引用 |
| `pollution:hv_wireless_mana_input_hatch_1a` | §6HV Wireless Mana Input Hatch (1A) | 无线 1A 魔力输入仓（§6HV§r） | 无任何配方引用 |
| `pollution:hv_wireless_mana_input_hatch_4a` | §6HV Wireless Mana Input Hatch (4A) | 无线 4A 魔力输入仓（§6HV§r） | 无任何配方引用 |
| `pollution:hv_wireless_mana_input_hatch_64a` | §6HV Wireless Mana Input Hatch (64A) | 无线 64A 魔力输入仓（§6HV§r） | 无任何配方引用 |
| `pollution:hv_wireless_mana_output_hatch_16a` | §6HV Wireless Mana Output Hatch (16A) | 无线 16A 魔力输出仓（§6HV§r） | 无任何配方引用 |
| `pollution:hv_wireless_mana_output_hatch_1a` | §6HV Wireless Mana Output Hatch (1A) | 无线 1A 魔力输出仓（§6HV§r） | 无任何配方引用 |
| `pollution:hv_wireless_mana_output_hatch_4a` | §6HV Wireless Mana Output Hatch (4A) | 无线 4A 魔力输出仓（§6HV§r） | 无任何配方引用 |
| `pollution:hv_wireless_mana_output_hatch_64a` | §6HV Wireless Mana Output Hatch (64A) | 无线 64A 魔力输出仓（§6HV§r） | 无任何配方引用 |
| `pollution:iv_aspect_tank` | §9IV Aspect Tank | 量子源质缸 I（§1IV§r） | 无任何配方引用 |
| `pollution:iv_flux_muffler` | §9IV Flux Muffler | 净化消声仓（§1IV§r） | 无任何配方引用 |
| `pollution:iv_infused_fluid_hatch` | §9IV Infused Fluid Hatch | 源质流体输入仓（§1IV§r） | 无任何配方引用 |
| `pollution:iv_mana_generator` | §9IV Mana Generator | 爆喷魔力发电机 | 无任何配方引用 |
| `pollution:iv_mana_input_hatch_16a` | §9IV Mana Input Hatch (16A) | 16A 魔力输入仓（§1IV§r） | 无任何配方引用 |
| `pollution:iv_mana_input_hatch_4a` | §9IV Mana Input Hatch (4A) | 4A 魔力输入仓（§1IV§r） | 无任何配方引用 |
| `pollution:iv_mana_input_hatch_64a` | §9IV Mana Input Hatch (64A) | 64A 魔力输入仓（§1IV§r） | 无任何配方引用 |
| `pollution:iv_mana_output_hatch_16a` | §9IV Mana Output Hatch (16A) | 16A 魔力输出仓（§1IV§r） | 无任何配方引用 |
| `pollution:iv_mana_output_hatch_1a` | §9IV Mana Output Hatch (1A) | 1A 魔力输出仓（§1IV§r） | 无任何配方引用 |
| `pollution:iv_mana_output_hatch_4a` | §9IV Mana Output Hatch (4A) | 4A 魔力输出仓（§1IV§r） | 无任何配方引用 |
| `pollution:iv_mana_output_hatch_64a` | §9IV Mana Output Hatch (64A) | 64A 魔力输出仓（§1IV§r） | 无任何配方引用 |
| `pollution:iv_wireless_mana_input_hatch_16a` | §9IV Wireless Mana Input Hatch (16A) | 无线 16A 魔力输入仓（§1IV§r） | 无任何配方引用 |
| `pollution:iv_wireless_mana_input_hatch_1a` | §9IV Wireless Mana Input Hatch (1A) | 无线 1A 魔力输入仓（§1IV§r） | 无任何配方引用 |
| `pollution:iv_wireless_mana_input_hatch_4a` | §9IV Wireless Mana Input Hatch (4A) | 无线 4A 魔力输入仓（§1IV§r） | 无任何配方引用 |
| `pollution:iv_wireless_mana_input_hatch_64a` | §9IV Wireless Mana Input Hatch (64A) | 无线 64A 魔力输入仓（§1IV§r） | 无任何配方引用 |
| `pollution:iv_wireless_mana_output_hatch_16a` | §9IV Wireless Mana Output Hatch (16A) | 无线 16A 魔力输出仓（§1IV§r） | 无任何配方引用 |
| `pollution:iv_wireless_mana_output_hatch_1a` | §9IV Wireless Mana Output Hatch (1A) | 无线 1A 魔力输出仓（§1IV§r） | 无任何配方引用 |
| `pollution:iv_wireless_mana_output_hatch_4a` | §9IV Wireless Mana Output Hatch (4A) | 无线 4A 魔力输出仓（§1IV§r） | 无任何配方引用 |
| `pollution:iv_wireless_mana_output_hatch_64a` | §9IV Wireless Mana Output Hatch (64A) | 无线 64A 魔力输出仓（§1IV§r） | 无任何配方引用 |
| `pollution:luv_aspect_tank` | §dLuV Aspect Tank | 量子源质缸 II（§dLuV§r） | 无任何配方引用 |
| `pollution:luv_flux_muffler` | §dLuV Flux Muffler | 净化消声仓（§dLuV§r） | 无任何配方引用 |
| `pollution:luv_infused_fluid_hatch` | §dLuV Infused Fluid Hatch | 源质流体输入仓（§dLuV§r） | 无任何配方引用 |
| `pollution:luv_mana_input_hatch_16a` | §dLuV Mana Input Hatch (16A) | 16A 魔力输入仓（§dLuV§r） | 无任何配方引用 |
| `pollution:luv_mana_input_hatch_4a` | §dLuV Mana Input Hatch (4A) | 4A 魔力输入仓（§dLuV§r） | 无任何配方引用 |
| `pollution:luv_mana_input_hatch_64a` | §dLuV Mana Input Hatch (64A) | 64A 魔力输入仓（§dLuV§r） | 无任何配方引用 |
| `pollution:luv_mana_output_hatch_16a` | §dLuV Mana Output Hatch (16A) | 16A 魔力输出仓（§dLuV§r） | 无任何配方引用 |
| `pollution:luv_mana_output_hatch_1a` | §dLuV Mana Output Hatch (1A) | 1A 魔力输出仓（§dLuV§r） | 无任何配方引用 |
| `pollution:luv_mana_output_hatch_4a` | §dLuV Mana Output Hatch (4A) | 4A 魔力输出仓（§dLuV§r） | 无任何配方引用 |
| `pollution:luv_mana_output_hatch_64a` | §dLuV Mana Output Hatch (64A) | 64A 魔力输出仓（§dLuV§r） | 无任何配方引用 |
| `pollution:luv_node_fusion_reactor` | LuV Node Fusion Reactor | LuV 节点聚变堆 | 无任何配方引用 |
| `pollution:luv_small_node_generator` | §dLuV Micro Starlight Node Reactor | 微缩星光节点反应堆MKI（§dLuV§r） | 无任何配方引用 |
| `pollution:luv_wireless_mana_input_hatch_16a` | §dLuV Wireless Mana Input Hatch (16A) | 无线 16A 魔力输入仓（§dLuV§r） | 无任何配方引用 |
| `pollution:luv_wireless_mana_input_hatch_1a` | §dLuV Wireless Mana Input Hatch (1A) | 无线 1A 魔力输入仓（§dLuV§r） | 无任何配方引用 |
| `pollution:luv_wireless_mana_input_hatch_4a` | §dLuV Wireless Mana Input Hatch (4A) | 无线 4A 魔力输入仓（§dLuV§r） | 无任何配方引用 |
| `pollution:luv_wireless_mana_input_hatch_64a` | §dLuV Wireless Mana Input Hatch (64A) | 无线 64A 魔力输入仓（§dLuV§r） | 无任何配方引用 |
| `pollution:luv_wireless_mana_output_hatch_16a` | §dLuV Wireless Mana Output Hatch (16A) | 无线 16A 魔力输出仓（§dLuV§r） | 无任何配方引用 |
| `pollution:luv_wireless_mana_output_hatch_1a` | §dLuV Wireless Mana Output Hatch (1A) | 无线 1A 魔力输出仓（§dLuV§r） | 无任何配方引用 |
| `pollution:luv_wireless_mana_output_hatch_4a` | §dLuV Wireless Mana Output Hatch (4A) | 无线 4A 魔力输出仓（§dLuV§r） | 无任何配方引用 |
| `pollution:luv_wireless_mana_output_hatch_64a` | §dLuV Wireless Mana Output Hatch (64A) | 无线 64A 魔力输出仓（§dLuV§r） | 无任何配方引用 |
| `pollution:lv_aspect_tank` | §7LV Aspect Tank | 超级源质缸 I（§7LV§r） | 无任何配方引用 |
| `pollution:lv_flux_muffler` | §7LV Flux Muffler | 净化消声仓（§7LV§r） | 无任何配方引用 |
| `pollution:lv_infused_fluid_hatch` | §7LV Infused Fluid Hatch | 源质流体输入仓（§7LV§r） | 无任何配方引用 |
| `pollution:lv_mana_generator` | §7LV Mana Generator | 基础魔力发电机 | 无任何配方引用 |
| `pollution:lv_mana_input_hatch_16a` | §7LV Mana Input Hatch (16A) | 16A 魔力输入仓（§7LV§r） | 无任何配方引用 |
| `pollution:lv_mana_input_hatch_4a` | §7LV Mana Input Hatch (4A) | 4A 魔力输入仓（§7LV§r） | 无任何配方引用 |
| `pollution:lv_mana_input_hatch_64a` | §7LV Mana Input Hatch (64A) | 64A 魔力输入仓（§7LV§r） | 无任何配方引用 |
| `pollution:lv_mana_output_hatch_16a` | §7LV Mana Output Hatch (16A) | 16A 魔力输出仓（§7LV§r） | 无任何配方引用 |
| `pollution:lv_mana_output_hatch_1a` | §7LV Mana Output Hatch (1A) | 1A 魔力输出仓（§7LV§r） | 无任何配方引用 |
| `pollution:lv_mana_output_hatch_4a` | §7LV Mana Output Hatch (4A) | 4A 魔力输出仓（§7LV§r） | 无任何配方引用 |
| `pollution:lv_mana_output_hatch_64a` | §7LV Mana Output Hatch (64A) | 64A 魔力输出仓（§7LV§r） | 无任何配方引用 |
| `pollution:lv_wireless_mana_input_hatch_16a` | §7LV Wireless Mana Input Hatch (16A) | 无线 16A 魔力输入仓（§7LV§r） | 无任何配方引用 |
| `pollution:lv_wireless_mana_input_hatch_1a` | §7LV Wireless Mana Input Hatch (1A) | 无线 1A 魔力输入仓（§7LV§r） | 无任何配方引用 |
| `pollution:lv_wireless_mana_input_hatch_4a` | §7LV Wireless Mana Input Hatch (4A) | 无线 4A 魔力输入仓（§7LV§r） | 无任何配方引用 |
| `pollution:lv_wireless_mana_input_hatch_64a` | §7LV Wireless Mana Input Hatch (64A) | 无线 64A 魔力输入仓（§7LV§r） | 无任何配方引用 |
| `pollution:lv_wireless_mana_output_hatch_16a` | §7LV Wireless Mana Output Hatch (16A) | 无线 16A 魔力输出仓（§7LV§r） | 无任何配方引用 |
| `pollution:lv_wireless_mana_output_hatch_1a` | §7LV Wireless Mana Output Hatch (1A) | 无线 1A 魔力输出仓（§7LV§r） | 无任何配方引用 |
| `pollution:lv_wireless_mana_output_hatch_4a` | §7LV Wireless Mana Output Hatch (4A) | 无线 4A 魔力输出仓（§7LV§r） | 无任何配方引用 |
| `pollution:lv_wireless_mana_output_hatch_64a` | §7LV Wireless Mana Output Hatch (64A) | 无线 64A 魔力输出仓（§7LV§r） | 无任何配方引用 |
| `pollution:magic_green_house` | Magic Greenhouse | 魔导温室 | 无任何配方引用 |
| `pollution:magic_mega_turbine` | Magic Mega Turbine | 特大源质要素轮机（§1IV§r） | 无任何配方引用 |
| `pollution:mana_plate` | Mana Plate | 魔力基板加速器 | 无任何配方引用 |
| `pollution:mv_aspect_tank` | §bMV Aspect Tank | 超级源质缸 II（§bMV§r） | 无任何配方引用 |
| `pollution:mv_flux_muffler` | §bMV Flux Muffler | 净化消声仓（§bMV§r） | 无任何配方引用 |
| `pollution:mv_infused_fluid_hatch` | §bMV Infused Fluid Hatch | 源质流体输入仓（§bMV§r） | 无任何配方引用 |
| `pollution:mv_mana_generator` | §bMV Mana Generator | 进阶魔力发电机 | 无任何配方引用 |
| `pollution:mv_mana_input_hatch_16a` | §bMV Mana Input Hatch (16A) | 16A 魔力输入仓（§bMV§r） | 无任何配方引用 |
| `pollution:mv_mana_input_hatch_4a` | §bMV Mana Input Hatch (4A) | 4A 魔力输入仓（§bMV§r） | 无任何配方引用 |
| `pollution:mv_mana_input_hatch_64a` | §bMV Mana Input Hatch (64A) | 64A 魔力输入仓（§bMV§r） | 无任何配方引用 |
| `pollution:mv_mana_output_hatch_16a` | §bMV Mana Output Hatch (16A) | 16A 魔力输出仓（§bMV§r） | 无任何配方引用 |
| `pollution:mv_mana_output_hatch_1a` | §bMV Mana Output Hatch (1A) | 1A 魔力输出仓（§bMV§r） | 无任何配方引用 |
| `pollution:mv_mana_output_hatch_4a` | §bMV Mana Output Hatch (4A) | 4A 魔力输出仓（§bMV§r） | 无任何配方引用 |
| `pollution:mv_mana_output_hatch_64a` | §bMV Mana Output Hatch (64A) | 64A 魔力输出仓（§bMV§r） | 无任何配方引用 |
| `pollution:mv_wireless_mana_input_hatch_16a` | §bMV Wireless Mana Input Hatch (16A) | 无线 16A 魔力输入仓（§bMV§r） | 无任何配方引用 |
| `pollution:mv_wireless_mana_input_hatch_1a` | §bMV Wireless Mana Input Hatch (1A) | 无线 1A 魔力输入仓（§bMV§r） | 无任何配方引用 |
| `pollution:mv_wireless_mana_input_hatch_4a` | §bMV Wireless Mana Input Hatch (4A) | 无线 4A 魔力输入仓（§bMV§r） | 无任何配方引用 |
| `pollution:mv_wireless_mana_input_hatch_64a` | §bMV Wireless Mana Input Hatch (64A) | 无线 64A 魔力输入仓（§bMV§r） | 无任何配方引用 |
| `pollution:mv_wireless_mana_output_hatch_16a` | §bMV Wireless Mana Output Hatch (16A) | 无线 16A 魔力输出仓（§bMV§r） | 无任何配方引用 |
| `pollution:mv_wireless_mana_output_hatch_1a` | §bMV Wireless Mana Output Hatch (1A) | 无线 1A 魔力输出仓（§bMV§r） | 无任何配方引用 |
| `pollution:mv_wireless_mana_output_hatch_4a` | §bMV Wireless Mana Output Hatch (4A) | 无线 4A 魔力输出仓（§bMV§r） | 无任何配方引用 |
| `pollution:mv_wireless_mana_output_hatch_64a` | §bMV Wireless Mana Output Hatch (64A) | 无线 64A 魔力输出仓（§bMV§r） | 无任何配方引用 |
| `pollution:node_washer` | Node Washer | 节点清洗机 | 无任何配方引用 |
| `pollution:pollution_multi_dan_de_life_on` | Life Activation Garden | 启命花园 | 无任何配方引用 |
| `pollution:source_charge` | Source Charge | 源质充能器 | 无任何配方引用 |
| `pollution:uhv_aspect_tank` | §4UHV Aspect Tank | 量子源质缸 V（§4UHV§r） | 无任何配方引用 |
| `pollution:uhv_flux_muffler` | §4UHV Flux Muffler | 净化消声仓（§4UHV§r） | 无任何配方引用 |
| `pollution:uhv_infused_fluid_hatch` | §4UHV Infused Fluid Hatch | 源质流体输入仓（§4UHV§r） | 无任何配方引用 |
| `pollution:uhv_mana_input_hatch_16a` | §4UHV Mana Input Hatch (16A) | 16A 魔力输入仓（§4UHV§r） | 无任何配方引用 |
| `pollution:uhv_mana_input_hatch_4a` | §4UHV Mana Input Hatch (4A) | 4A 魔力输入仓（§4UHV§r） | 无任何配方引用 |
| `pollution:uhv_mana_input_hatch_64a` | §4UHV Mana Input Hatch (64A) | 64A 魔力输入仓（§4UHV§r） | 无任何配方引用 |
| `pollution:uhv_mana_output_hatch_16a` | §4UHV Mana Output Hatch (16A) | 16A 魔力输出仓（§4UHV§r） | 无任何配方引用 |
| `pollution:uhv_mana_output_hatch_1a` | §4UHV Mana Output Hatch (1A) | 1A 魔力输出仓（§4UHV§r） | 无任何配方引用 |
| `pollution:uhv_mana_output_hatch_4a` | §4UHV Mana Output Hatch (4A) | 4A 魔力输出仓（§4UHV§r） | 无任何配方引用 |
| `pollution:uhv_mana_output_hatch_64a` | §4UHV Mana Output Hatch (64A) | 64A 魔力输出仓（§4UHV§r） | 无任何配方引用 |
| `pollution:uhv_small_node_generator` | §4UHV Micro Starlight Node Reactor | 微缩星光节点反应堆MKⅣ（§4UHV§r） | 无任何配方引用 |
| `pollution:uhv_wireless_mana_input_hatch_16a` | §4UHV Wireless Mana Input Hatch (16A) | 无线 16A 魔力输入仓（§4UHV§r） | 无任何配方引用 |
| `pollution:uhv_wireless_mana_input_hatch_1a` | §4UHV Wireless Mana Input Hatch (1A) | 无线 1A 魔力输入仓（§4UHV§r） | 无任何配方引用 |
| `pollution:uhv_wireless_mana_input_hatch_4a` | §4UHV Wireless Mana Input Hatch (4A) | 无线 4A 魔力输入仓（§4UHV§r） | 无任何配方引用 |
| `pollution:uhv_wireless_mana_input_hatch_64a` | §4UHV Wireless Mana Input Hatch (64A) | 无线 64A 魔力输入仓（§4UHV§r） | 无任何配方引用 |
| `pollution:uhv_wireless_mana_output_hatch_16a` | §4UHV Wireless Mana Output Hatch (16A) | 无线 16A 魔力输出仓（§4UHV§r） | 无任何配方引用 |
| `pollution:uhv_wireless_mana_output_hatch_1a` | §4UHV Wireless Mana Output Hatch (1A) | 无线 1A 魔力输出仓（§4UHV§r） | 无任何配方引用 |
| `pollution:uhv_wireless_mana_output_hatch_4a` | §4UHV Wireless Mana Output Hatch (4A) | 无线 4A 魔力输出仓（§4UHV§r） | 无任何配方引用 |
| `pollution:uhv_wireless_mana_output_hatch_64a` | §4UHV Wireless Mana Output Hatch (64A) | 无线 64A 魔力输出仓（§4UHV§r） | 无任何配方引用 |
| `pollution:uv_aspect_tank` | §3UV Aspect Tank | 量子源质缸 IV（§3UV§r） | 无任何配方引用 |
| `pollution:uv_flux_muffler` | §3UV Flux Muffler | 净化消声仓（§3UV§r） | 无任何配方引用 |
| `pollution:uv_infused_fluid_hatch` | §3UV Infused Fluid Hatch | 源质流体输入仓（§3UV§r） | 无任何配方引用 |
| `pollution:uv_mana_input_hatch_16a` | §3UV Mana Input Hatch (16A) | 16A 魔力输入仓（§3UV§r） | 无任何配方引用 |
| `pollution:uv_mana_input_hatch_4a` | §3UV Mana Input Hatch (4A) | 4A 魔力输入仓（§3UV§r） | 无任何配方引用 |
| `pollution:uv_mana_input_hatch_64a` | §3UV Mana Input Hatch (64A) | 64A 魔力输入仓（§3UV§r） | 无任何配方引用 |
| `pollution:uv_mana_output_hatch_16a` | §3UV Mana Output Hatch (16A) | 16A 魔力输出仓（§3UV§r） | 无任何配方引用 |
| `pollution:uv_mana_output_hatch_1a` | §3UV Mana Output Hatch (1A) | 1A 魔力输出仓（§3UV§r） | 无任何配方引用 |
| `pollution:uv_mana_output_hatch_4a` | §3UV Mana Output Hatch (4A) | 4A 魔力输出仓（§3UV§r） | 无任何配方引用 |
| `pollution:uv_mana_output_hatch_64a` | §3UV Mana Output Hatch (64A) | 64A 魔力输出仓（§3UV§r） | 无任何配方引用 |
| `pollution:uv_node_fusion_reactor` | UV Node Fusion Reactor | UV 节点聚变堆 | 无任何配方引用 |
| `pollution:uv_small_node_generator` | §3UV Micro Starlight Node Reactor | 微缩星光节点反应堆MKⅢ（§3UV§r） | 无任何配方引用 |
| `pollution:uv_wireless_mana_input_hatch_16a` | §3UV Wireless Mana Input Hatch (16A) | 无线 16A 魔力输入仓（§3UV§r） | 无任何配方引用 |
| `pollution:uv_wireless_mana_input_hatch_1a` | §3UV Wireless Mana Input Hatch (1A) | 无线 1A 魔力输入仓（§3UV§r） | 无任何配方引用 |
| `pollution:uv_wireless_mana_input_hatch_4a` | §3UV Wireless Mana Input Hatch (4A) | 无线 4A 魔力输入仓（§3UV§r） | 无任何配方引用 |
| `pollution:uv_wireless_mana_input_hatch_64a` | §3UV Wireless Mana Input Hatch (64A) | 无线 64A 魔力输入仓（§3UV§r） | 无任何配方引用 |
| `pollution:uv_wireless_mana_output_hatch_16a` | §3UV Wireless Mana Output Hatch (16A) | 无线 16A 魔力输出仓（§3UV§r） | 无任何配方引用 |
| `pollution:uv_wireless_mana_output_hatch_1a` | §3UV Wireless Mana Output Hatch (1A) | 无线 1A 魔力输出仓（§3UV§r） | 无任何配方引用 |
| `pollution:uv_wireless_mana_output_hatch_4a` | §3UV Wireless Mana Output Hatch (4A) | 无线 4A 魔力输出仓（§3UV§r） | 无任何配方引用 |
| `pollution:uv_wireless_mana_output_hatch_64a` | §3UV Wireless Mana Output Hatch (64A) | 无线 64A 魔力输出仓（§3UV§r） | 无任何配方引用 |
| `pollution:zpm_aspect_tank` | §cZPM Aspect Tank | 量子源质缸 III（§cZPM§r） | 无任何配方引用 |
| `pollution:zpm_flux_muffler` | §cZPM Flux Muffler | 净化消声仓（§cZPM§r） | 无任何配方引用 |
| `pollution:zpm_infused_fluid_hatch` | §cZPM Infused Fluid Hatch | 源质流体输入仓（§cZPM§r） | 无任何配方引用 |
| `pollution:zpm_mana_input_hatch_16a` | §cZPM Mana Input Hatch (16A) | 16A 魔力输入仓（§cZPM§r） | 无任何配方引用 |
| `pollution:zpm_mana_input_hatch_4a` | §cZPM Mana Input Hatch (4A) | 4A 魔力输入仓（§cZPM§r） | 无任何配方引用 |
| `pollution:zpm_mana_input_hatch_64a` | §cZPM Mana Input Hatch (64A) | 64A 魔力输入仓（§cZPM§r） | 无任何配方引用 |
| `pollution:zpm_mana_output_hatch_16a` | §cZPM Mana Output Hatch (16A) | 16A 魔力输出仓（§cZPM§r） | 无任何配方引用 |
| `pollution:zpm_mana_output_hatch_1a` | §cZPM Mana Output Hatch (1A) | 1A 魔力输出仓（§cZPM§r） | 无任何配方引用 |
| `pollution:zpm_mana_output_hatch_4a` | §cZPM Mana Output Hatch (4A) | 4A 魔力输出仓（§cZPM§r） | 无任何配方引用 |
| `pollution:zpm_mana_output_hatch_64a` | §cZPM Mana Output Hatch (64A) | 64A 魔力输出仓（§cZPM§r） | 无任何配方引用 |
| `pollution:zpm_node_fusion_reactor` | ZPM Node Fusion Reactor | ZPM 节点聚变堆 | 无任何配方引用 |
| `pollution:zpm_small_node_generator` | §cZPM Micro Starlight Node Reactor | 微缩星光节点反应堆MKⅡ（§cZPM§r） | 无任何配方引用 |
| `pollution:zpm_wireless_mana_input_hatch_16a` | §cZPM Wireless Mana Input Hatch (16A) | 无线 16A 魔力输入仓（§cZPM§r） | 无任何配方引用 |
| `pollution:zpm_wireless_mana_input_hatch_1a` | §cZPM Wireless Mana Input Hatch (1A) | 无线 1A 魔力输入仓（§cZPM§r） | 无任何配方引用 |
| `pollution:zpm_wireless_mana_input_hatch_4a` | §cZPM Wireless Mana Input Hatch (4A) | 无线 4A 魔力输入仓（§cZPM§r） | 无任何配方引用 |
| `pollution:zpm_wireless_mana_input_hatch_64a` | §cZPM Wireless Mana Input Hatch (64A) | 无线 64A 魔力输入仓（§cZPM§r） | 无任何配方引用 |
| `pollution:zpm_wireless_mana_output_hatch_16a` | §cZPM Wireless Mana Output Hatch (16A) | 无线 16A 魔力输出仓（§cZPM§r） | 无任何配方引用 |
| `pollution:zpm_wireless_mana_output_hatch_1a` | §cZPM Wireless Mana Output Hatch (1A) | 无线 1A 魔力输出仓（§cZPM§r） | 无任何配方引用 |
| `pollution:zpm_wireless_mana_output_hatch_4a` | §cZPM Wireless Mana Output Hatch (4A) | 无线 4A 魔力输出仓（§cZPM§r） | 无任何配方引用 |
| `pollution:zpm_wireless_mana_output_hatch_64a` | §cZPM Wireless Mana Output Hatch (64A) | 无线 64A 魔力输出仓（§cZPM§r） | 无任何配方引用 |

## 方块（1058）

### 世界生成矿石（1040）

| 注册名 (ID) | 英文名 | 中文名 | 备注 |
| --- | --- | --- | --- |
| `pollution:amber_ore` | Amber Ore | 琥珀矿石 | 世界生成矿石（可挖掘获得） |
| `pollution:andesite_amber_ore` | andesite_amber_ore | andesite_amber_ore | 世界生成矿石（可挖掘获得） |
| `pollution:andesite_authority_lead_ore` | andesite_authority_lead_ore | andesite_authority_lead_ore | 世界生成矿石（可挖掘获得） |
| `pollution:andesite_dragonstone_ore` | andesite_dragonstone_ore | andesite_dragonstone_ore | 世界生成矿石（可挖掘获得） |
| `pollution:andesite_dumb_tin_ore` | andesite_dumb_tin_ore | andesite_dumb_tin_ore | 世界生成矿石（可挖掘获得） |
| `pollution:andesite_elven_elementium_ore` | andesite_elven_elementium_ore | andesite_elven_elementium_ore | 世界生成矿石（可挖掘获得） |
| `pollution:andesite_elven_quartz_ore` | andesite_elven_quartz_ore | andesite_elven_quartz_ore | 世界生成矿石（可挖掘获得） |
| `pollution:andesite_flame_coal_ore` | andesite_flame_coal_ore | andesite_flame_coal_ore | 世界生成矿石（可挖掘获得） |
| `pollution:andesite_infused_air_ore` | andesite_infused_air_ore | andesite_infused_air_ore | 世界生成矿石（可挖掘获得） |
| `pollution:andesite_infused_alchemy_ore` | andesite_infused_alchemy_ore | andesite_infused_alchemy_ore | 世界生成矿石（可挖掘获得） |
| `pollution:andesite_infused_alien_ore` | andesite_infused_alien_ore | andesite_infused_alien_ore | 世界生成矿石（可挖掘获得） |
| `pollution:andesite_infused_animal_ore` | andesite_infused_animal_ore | andesite_infused_animal_ore | 世界生成矿石（可挖掘获得） |
| `pollution:andesite_infused_armor_ore` | andesite_infused_armor_ore | andesite_infused_armor_ore | 世界生成矿石（可挖掘获得） |
| `pollution:andesite_infused_aura_ore` | andesite_infused_aura_ore | andesite_infused_aura_ore | 世界生成矿石（可挖掘获得） |
| `pollution:andesite_infused_cold_ore` | andesite_infused_cold_ore | andesite_infused_cold_ore | 世界生成矿石（可挖掘获得） |
| `pollution:andesite_infused_craft_ore` | andesite_infused_craft_ore | andesite_infused_craft_ore | 世界生成矿石（可挖掘获得） |
| `pollution:andesite_infused_crystal_ore` | andesite_infused_crystal_ore | andesite_infused_crystal_ore | 世界生成矿石（可挖掘获得） |
| `pollution:andesite_infused_dark_ore` | andesite_infused_dark_ore | andesite_infused_dark_ore | 世界生成矿石（可挖掘获得） |
| `pollution:andesite_infused_death_ore` | andesite_infused_death_ore | andesite_infused_death_ore | 世界生成矿石（可挖掘获得） |
| `pollution:andesite_infused_earth_ore` | andesite_infused_earth_ore | andesite_infused_earth_ore | 世界生成矿石（可挖掘获得） |
| `pollution:andesite_infused_energy_ore` | andesite_infused_energy_ore | andesite_infused_energy_ore | 世界生成矿石（可挖掘获得） |
| `pollution:andesite_infused_entropy_ore` | andesite_infused_entropy_ore | andesite_infused_entropy_ore | 世界生成矿石（可挖掘获得） |
| `pollution:andesite_infused_exchange_ore` | andesite_infused_exchange_ore | andesite_infused_exchange_ore | 世界生成矿石（可挖掘获得） |
| `pollution:andesite_infused_fire_ore` | andesite_infused_fire_ore | andesite_infused_fire_ore | 世界生成矿石（可挖掘获得） |
| `pollution:andesite_infused_fly_ore` | andesite_infused_fly_ore | andesite_infused_fly_ore | 世界生成矿石（可挖掘获得） |
| `pollution:andesite_infused_greed_ore` | andesite_infused_greed_ore | andesite_infused_greed_ore | 世界生成矿石（可挖掘获得） |
| `pollution:andesite_infused_human_ore` | andesite_infused_human_ore | andesite_infused_human_ore | 世界生成矿石（可挖掘获得） |
| `pollution:andesite_infused_instrument_ore` | andesite_infused_instrument_ore | andesite_infused_instrument_ore | 世界生成矿石（可挖掘获得） |
| `pollution:andesite_infused_life_ore` | andesite_infused_life_ore | andesite_infused_life_ore | 世界生成矿石（可挖掘获得） |
| `pollution:andesite_infused_light_ore` | andesite_infused_light_ore | andesite_infused_light_ore | 世界生成矿石（可挖掘获得） |
| `pollution:andesite_infused_magic_ore` | andesite_infused_magic_ore | andesite_infused_magic_ore | 世界生成矿石（可挖掘获得） |
| `pollution:andesite_infused_mechanics_ore` | andesite_infused_mechanics_ore | andesite_infused_mechanics_ore | 世界生成矿石（可挖掘获得） |
| `pollution:andesite_infused_metal_ore` | andesite_infused_metal_ore | andesite_infused_metal_ore | 世界生成矿石（可挖掘获得） |
| `pollution:andesite_infused_motion_ore` | andesite_infused_motion_ore | andesite_infused_motion_ore | 世界生成矿石（可挖掘获得） |
| `pollution:andesite_infused_order_ore` | andesite_infused_order_ore | andesite_infused_order_ore | 世界生成矿石（可挖掘获得） |
| `pollution:andesite_infused_plant_ore` | andesite_infused_plant_ore | andesite_infused_plant_ore | 世界生成矿石（可挖掘获得） |
| `pollution:andesite_infused_sense_ore` | andesite_infused_sense_ore | andesite_infused_sense_ore | 世界生成矿石（可挖掘获得） |
| `pollution:andesite_infused_soul_ore` | andesite_infused_soul_ore | andesite_infused_soul_ore | 世界生成矿石（可挖掘获得） |
| `pollution:andesite_infused_spatio_ore` | andesite_infused_spatio_ore | andesite_infused_spatio_ore | 世界生成矿石（可挖掘获得） |
| `pollution:andesite_infused_taint_ore` | andesite_infused_taint_ore | andesite_infused_taint_ore | 世界生成矿石（可挖掘获得） |
| `pollution:andesite_infused_tempus_ore` | andesite_infused_tempus_ore | andesite_infused_tempus_ore | 世界生成矿石（可挖掘获得） |
| `pollution:andesite_infused_thought_ore` | andesite_infused_thought_ore | andesite_infused_thought_ore | 世界生成矿石（可挖掘获得） |
| `pollution:andesite_infused_tinctura_ore` | andesite_infused_tinctura_ore | andesite_infused_tinctura_ore | 世界生成矿石（可挖掘获得） |
| `pollution:andesite_infused_trap_ore` | andesite_infused_trap_ore | andesite_infused_trap_ore | 世界生成矿石（可挖掘获得） |
| `pollution:andesite_infused_undead_ore` | andesite_infused_undead_ore | andesite_infused_undead_ore | 世界生成矿石（可挖掘获得） |
| `pollution:andesite_infused_void_ore` | andesite_infused_void_ore | andesite_infused_void_ore | 世界生成矿石（可挖掘获得） |
| `pollution:andesite_infused_water_ore` | andesite_infused_water_ore | andesite_infused_water_ore | 世界生成矿石（可挖掘获得） |
| `pollution:andesite_infused_weapon_ore` | andesite_infused_weapon_ore | andesite_infused_weapon_ore | 世界生成矿石（可挖掘获得） |
| `pollution:andesite_melt_gold_ore` | andesite_melt_gold_ore | andesite_melt_gold_ore | 世界生成矿石（可挖掘获得） |
| `pollution:andesite_pixie_dust_ore` | andesite_pixie_dust_ore | andesite_pixie_dust_ore | 世界生成矿石（可挖掘获得） |
| `pollution:andesite_pluto_zinc_ore` | andesite_pluto_zinc_ore | andesite_pluto_zinc_ore | 世界生成矿石（可挖掘获得） |
| `pollution:andesite_pyrargyrite_ore` | andesite_pyrargyrite_ore | andesite_pyrargyrite_ore | 世界生成矿石（可挖掘获得） |
| `pollution:andesite_scabyst_ore` | andesite_scabyst_ore | andesite_scabyst_ore | 世界生成矿石（可挖掘获得） |
| `pollution:authority_lead_ore` | Authority Lead Ore | 镇渊铅矿石 | 世界生成矿石（可挖掘获得） |
| `pollution:basalt_amber_ore` | basalt_amber_ore | basalt_amber_ore | 世界生成矿石（可挖掘获得） |
| `pollution:basalt_authority_lead_ore` | basalt_authority_lead_ore | basalt_authority_lead_ore | 世界生成矿石（可挖掘获得） |
| `pollution:basalt_dragonstone_ore` | basalt_dragonstone_ore | basalt_dragonstone_ore | 世界生成矿石（可挖掘获得） |
| `pollution:basalt_dumb_tin_ore` | basalt_dumb_tin_ore | basalt_dumb_tin_ore | 世界生成矿石（可挖掘获得） |
| `pollution:basalt_elven_elementium_ore` | basalt_elven_elementium_ore | basalt_elven_elementium_ore | 世界生成矿石（可挖掘获得） |
| `pollution:basalt_elven_quartz_ore` | basalt_elven_quartz_ore | basalt_elven_quartz_ore | 世界生成矿石（可挖掘获得） |
| `pollution:basalt_flame_coal_ore` | basalt_flame_coal_ore | basalt_flame_coal_ore | 世界生成矿石（可挖掘获得） |
| `pollution:basalt_infused_air_ore` | basalt_infused_air_ore | basalt_infused_air_ore | 世界生成矿石（可挖掘获得） |
| `pollution:basalt_infused_alchemy_ore` | basalt_infused_alchemy_ore | basalt_infused_alchemy_ore | 世界生成矿石（可挖掘获得） |
| `pollution:basalt_infused_alien_ore` | basalt_infused_alien_ore | basalt_infused_alien_ore | 世界生成矿石（可挖掘获得） |
| `pollution:basalt_infused_animal_ore` | basalt_infused_animal_ore | basalt_infused_animal_ore | 世界生成矿石（可挖掘获得） |
| `pollution:basalt_infused_armor_ore` | basalt_infused_armor_ore | basalt_infused_armor_ore | 世界生成矿石（可挖掘获得） |
| `pollution:basalt_infused_aura_ore` | basalt_infused_aura_ore | basalt_infused_aura_ore | 世界生成矿石（可挖掘获得） |
| `pollution:basalt_infused_cold_ore` | basalt_infused_cold_ore | basalt_infused_cold_ore | 世界生成矿石（可挖掘获得） |
| `pollution:basalt_infused_craft_ore` | basalt_infused_craft_ore | basalt_infused_craft_ore | 世界生成矿石（可挖掘获得） |
| `pollution:basalt_infused_crystal_ore` | basalt_infused_crystal_ore | basalt_infused_crystal_ore | 世界生成矿石（可挖掘获得） |
| `pollution:basalt_infused_dark_ore` | basalt_infused_dark_ore | basalt_infused_dark_ore | 世界生成矿石（可挖掘获得） |
| `pollution:basalt_infused_death_ore` | basalt_infused_death_ore | basalt_infused_death_ore | 世界生成矿石（可挖掘获得） |
| `pollution:basalt_infused_earth_ore` | basalt_infused_earth_ore | basalt_infused_earth_ore | 世界生成矿石（可挖掘获得） |
| `pollution:basalt_infused_energy_ore` | basalt_infused_energy_ore | basalt_infused_energy_ore | 世界生成矿石（可挖掘获得） |
| `pollution:basalt_infused_entropy_ore` | basalt_infused_entropy_ore | basalt_infused_entropy_ore | 世界生成矿石（可挖掘获得） |
| `pollution:basalt_infused_exchange_ore` | basalt_infused_exchange_ore | basalt_infused_exchange_ore | 世界生成矿石（可挖掘获得） |
| `pollution:basalt_infused_fire_ore` | basalt_infused_fire_ore | basalt_infused_fire_ore | 世界生成矿石（可挖掘获得） |
| `pollution:basalt_infused_fly_ore` | basalt_infused_fly_ore | basalt_infused_fly_ore | 世界生成矿石（可挖掘获得） |
| `pollution:basalt_infused_greed_ore` | basalt_infused_greed_ore | basalt_infused_greed_ore | 世界生成矿石（可挖掘获得） |
| `pollution:basalt_infused_human_ore` | basalt_infused_human_ore | basalt_infused_human_ore | 世界生成矿石（可挖掘获得） |
| `pollution:basalt_infused_instrument_ore` | basalt_infused_instrument_ore | basalt_infused_instrument_ore | 世界生成矿石（可挖掘获得） |
| `pollution:basalt_infused_life_ore` | basalt_infused_life_ore | basalt_infused_life_ore | 世界生成矿石（可挖掘获得） |
| `pollution:basalt_infused_light_ore` | basalt_infused_light_ore | basalt_infused_light_ore | 世界生成矿石（可挖掘获得） |
| `pollution:basalt_infused_magic_ore` | basalt_infused_magic_ore | basalt_infused_magic_ore | 世界生成矿石（可挖掘获得） |
| `pollution:basalt_infused_mechanics_ore` | basalt_infused_mechanics_ore | basalt_infused_mechanics_ore | 世界生成矿石（可挖掘获得） |
| `pollution:basalt_infused_metal_ore` | basalt_infused_metal_ore | basalt_infused_metal_ore | 世界生成矿石（可挖掘获得） |
| `pollution:basalt_infused_motion_ore` | basalt_infused_motion_ore | basalt_infused_motion_ore | 世界生成矿石（可挖掘获得） |
| `pollution:basalt_infused_order_ore` | basalt_infused_order_ore | basalt_infused_order_ore | 世界生成矿石（可挖掘获得） |
| `pollution:basalt_infused_plant_ore` | basalt_infused_plant_ore | basalt_infused_plant_ore | 世界生成矿石（可挖掘获得） |
| `pollution:basalt_infused_sense_ore` | basalt_infused_sense_ore | basalt_infused_sense_ore | 世界生成矿石（可挖掘获得） |
| `pollution:basalt_infused_soul_ore` | basalt_infused_soul_ore | basalt_infused_soul_ore | 世界生成矿石（可挖掘获得） |
| `pollution:basalt_infused_spatio_ore` | basalt_infused_spatio_ore | basalt_infused_spatio_ore | 世界生成矿石（可挖掘获得） |
| `pollution:basalt_infused_taint_ore` | basalt_infused_taint_ore | basalt_infused_taint_ore | 世界生成矿石（可挖掘获得） |
| `pollution:basalt_infused_tempus_ore` | basalt_infused_tempus_ore | basalt_infused_tempus_ore | 世界生成矿石（可挖掘获得） |
| `pollution:basalt_infused_thought_ore` | basalt_infused_thought_ore | basalt_infused_thought_ore | 世界生成矿石（可挖掘获得） |
| `pollution:basalt_infused_tinctura_ore` | basalt_infused_tinctura_ore | basalt_infused_tinctura_ore | 世界生成矿石（可挖掘获得） |
| `pollution:basalt_infused_trap_ore` | basalt_infused_trap_ore | basalt_infused_trap_ore | 世界生成矿石（可挖掘获得） |
| `pollution:basalt_infused_undead_ore` | basalt_infused_undead_ore | basalt_infused_undead_ore | 世界生成矿石（可挖掘获得） |
| `pollution:basalt_infused_void_ore` | basalt_infused_void_ore | basalt_infused_void_ore | 世界生成矿石（可挖掘获得） |
| `pollution:basalt_infused_water_ore` | basalt_infused_water_ore | basalt_infused_water_ore | 世界生成矿石（可挖掘获得） |
| `pollution:basalt_infused_weapon_ore` | basalt_infused_weapon_ore | basalt_infused_weapon_ore | 世界生成矿石（可挖掘获得） |
| `pollution:basalt_melt_gold_ore` | basalt_melt_gold_ore | basalt_melt_gold_ore | 世界生成矿石（可挖掘获得） |
| `pollution:basalt_pixie_dust_ore` | basalt_pixie_dust_ore | basalt_pixie_dust_ore | 世界生成矿石（可挖掘获得） |
| `pollution:basalt_pluto_zinc_ore` | basalt_pluto_zinc_ore | basalt_pluto_zinc_ore | 世界生成矿石（可挖掘获得） |
| `pollution:basalt_pyrargyrite_ore` | basalt_pyrargyrite_ore | basalt_pyrargyrite_ore | 世界生成矿石（可挖掘获得） |
| `pollution:basalt_scabyst_ore` | basalt_scabyst_ore | basalt_scabyst_ore | 世界生成矿石（可挖掘获得） |
| `pollution:blackstone_amber_ore` | blackstone_amber_ore | blackstone_amber_ore | 世界生成矿石（可挖掘获得） |
| `pollution:blackstone_authority_lead_ore` | blackstone_authority_lead_ore | blackstone_authority_lead_ore | 世界生成矿石（可挖掘获得） |
| `pollution:blackstone_dragonstone_ore` | blackstone_dragonstone_ore | blackstone_dragonstone_ore | 世界生成矿石（可挖掘获得） |
| `pollution:blackstone_dumb_tin_ore` | blackstone_dumb_tin_ore | blackstone_dumb_tin_ore | 世界生成矿石（可挖掘获得） |
| `pollution:blackstone_elven_elementium_ore` | blackstone_elven_elementium_ore | blackstone_elven_elementium_ore | 世界生成矿石（可挖掘获得） |
| `pollution:blackstone_elven_quartz_ore` | blackstone_elven_quartz_ore | blackstone_elven_quartz_ore | 世界生成矿石（可挖掘获得） |
| `pollution:blackstone_flame_coal_ore` | blackstone_flame_coal_ore | blackstone_flame_coal_ore | 世界生成矿石（可挖掘获得） |
| `pollution:blackstone_infused_air_ore` | blackstone_infused_air_ore | blackstone_infused_air_ore | 世界生成矿石（可挖掘获得） |
| `pollution:blackstone_infused_alchemy_ore` | blackstone_infused_alchemy_ore | blackstone_infused_alchemy_ore | 世界生成矿石（可挖掘获得） |
| `pollution:blackstone_infused_alien_ore` | blackstone_infused_alien_ore | blackstone_infused_alien_ore | 世界生成矿石（可挖掘获得） |
| `pollution:blackstone_infused_animal_ore` | blackstone_infused_animal_ore | blackstone_infused_animal_ore | 世界生成矿石（可挖掘获得） |
| `pollution:blackstone_infused_armor_ore` | blackstone_infused_armor_ore | blackstone_infused_armor_ore | 世界生成矿石（可挖掘获得） |
| `pollution:blackstone_infused_aura_ore` | blackstone_infused_aura_ore | blackstone_infused_aura_ore | 世界生成矿石（可挖掘获得） |
| `pollution:blackstone_infused_cold_ore` | blackstone_infused_cold_ore | blackstone_infused_cold_ore | 世界生成矿石（可挖掘获得） |
| `pollution:blackstone_infused_craft_ore` | blackstone_infused_craft_ore | blackstone_infused_craft_ore | 世界生成矿石（可挖掘获得） |
| `pollution:blackstone_infused_crystal_ore` | blackstone_infused_crystal_ore | blackstone_infused_crystal_ore | 世界生成矿石（可挖掘获得） |
| `pollution:blackstone_infused_dark_ore` | blackstone_infused_dark_ore | blackstone_infused_dark_ore | 世界生成矿石（可挖掘获得） |
| `pollution:blackstone_infused_death_ore` | blackstone_infused_death_ore | blackstone_infused_death_ore | 世界生成矿石（可挖掘获得） |
| `pollution:blackstone_infused_earth_ore` | blackstone_infused_earth_ore | blackstone_infused_earth_ore | 世界生成矿石（可挖掘获得） |
| `pollution:blackstone_infused_energy_ore` | blackstone_infused_energy_ore | blackstone_infused_energy_ore | 世界生成矿石（可挖掘获得） |
| `pollution:blackstone_infused_entropy_ore` | blackstone_infused_entropy_ore | blackstone_infused_entropy_ore | 世界生成矿石（可挖掘获得） |
| `pollution:blackstone_infused_exchange_ore` | blackstone_infused_exchange_ore | blackstone_infused_exchange_ore | 世界生成矿石（可挖掘获得） |
| `pollution:blackstone_infused_fire_ore` | blackstone_infused_fire_ore | blackstone_infused_fire_ore | 世界生成矿石（可挖掘获得） |
| `pollution:blackstone_infused_fly_ore` | blackstone_infused_fly_ore | blackstone_infused_fly_ore | 世界生成矿石（可挖掘获得） |
| `pollution:blackstone_infused_greed_ore` | blackstone_infused_greed_ore | blackstone_infused_greed_ore | 世界生成矿石（可挖掘获得） |
| `pollution:blackstone_infused_human_ore` | blackstone_infused_human_ore | blackstone_infused_human_ore | 世界生成矿石（可挖掘获得） |
| `pollution:blackstone_infused_instrument_ore` | blackstone_infused_instrument_ore | blackstone_infused_instrument_ore | 世界生成矿石（可挖掘获得） |
| `pollution:blackstone_infused_life_ore` | blackstone_infused_life_ore | blackstone_infused_life_ore | 世界生成矿石（可挖掘获得） |
| `pollution:blackstone_infused_light_ore` | blackstone_infused_light_ore | blackstone_infused_light_ore | 世界生成矿石（可挖掘获得） |
| `pollution:blackstone_infused_magic_ore` | blackstone_infused_magic_ore | blackstone_infused_magic_ore | 世界生成矿石（可挖掘获得） |
| `pollution:blackstone_infused_mechanics_ore` | blackstone_infused_mechanics_ore | blackstone_infused_mechanics_ore | 世界生成矿石（可挖掘获得） |
| `pollution:blackstone_infused_metal_ore` | blackstone_infused_metal_ore | blackstone_infused_metal_ore | 世界生成矿石（可挖掘获得） |
| `pollution:blackstone_infused_motion_ore` | blackstone_infused_motion_ore | blackstone_infused_motion_ore | 世界生成矿石（可挖掘获得） |
| `pollution:blackstone_infused_order_ore` | blackstone_infused_order_ore | blackstone_infused_order_ore | 世界生成矿石（可挖掘获得） |
| `pollution:blackstone_infused_plant_ore` | blackstone_infused_plant_ore | blackstone_infused_plant_ore | 世界生成矿石（可挖掘获得） |
| `pollution:blackstone_infused_sense_ore` | blackstone_infused_sense_ore | blackstone_infused_sense_ore | 世界生成矿石（可挖掘获得） |
| `pollution:blackstone_infused_soul_ore` | blackstone_infused_soul_ore | blackstone_infused_soul_ore | 世界生成矿石（可挖掘获得） |
| `pollution:blackstone_infused_spatio_ore` | blackstone_infused_spatio_ore | blackstone_infused_spatio_ore | 世界生成矿石（可挖掘获得） |
| `pollution:blackstone_infused_taint_ore` | blackstone_infused_taint_ore | blackstone_infused_taint_ore | 世界生成矿石（可挖掘获得） |
| `pollution:blackstone_infused_tempus_ore` | blackstone_infused_tempus_ore | blackstone_infused_tempus_ore | 世界生成矿石（可挖掘获得） |
| `pollution:blackstone_infused_thought_ore` | blackstone_infused_thought_ore | blackstone_infused_thought_ore | 世界生成矿石（可挖掘获得） |
| `pollution:blackstone_infused_tinctura_ore` | blackstone_infused_tinctura_ore | blackstone_infused_tinctura_ore | 世界生成矿石（可挖掘获得） |
| `pollution:blackstone_infused_trap_ore` | blackstone_infused_trap_ore | blackstone_infused_trap_ore | 世界生成矿石（可挖掘获得） |
| `pollution:blackstone_infused_undead_ore` | blackstone_infused_undead_ore | blackstone_infused_undead_ore | 世界生成矿石（可挖掘获得） |
| `pollution:blackstone_infused_void_ore` | blackstone_infused_void_ore | blackstone_infused_void_ore | 世界生成矿石（可挖掘获得） |
| `pollution:blackstone_infused_water_ore` | blackstone_infused_water_ore | blackstone_infused_water_ore | 世界生成矿石（可挖掘获得） |
| `pollution:blackstone_infused_weapon_ore` | blackstone_infused_weapon_ore | blackstone_infused_weapon_ore | 世界生成矿石（可挖掘获得） |
| `pollution:blackstone_melt_gold_ore` | blackstone_melt_gold_ore | blackstone_melt_gold_ore | 世界生成矿石（可挖掘获得） |
| `pollution:blackstone_pixie_dust_ore` | blackstone_pixie_dust_ore | blackstone_pixie_dust_ore | 世界生成矿石（可挖掘获得） |
| `pollution:blackstone_pluto_zinc_ore` | blackstone_pluto_zinc_ore | blackstone_pluto_zinc_ore | 世界生成矿石（可挖掘获得） |
| `pollution:blackstone_pyrargyrite_ore` | blackstone_pyrargyrite_ore | blackstone_pyrargyrite_ore | 世界生成矿石（可挖掘获得） |
| `pollution:blackstone_scabyst_ore` | blackstone_scabyst_ore | blackstone_scabyst_ore | 世界生成矿石（可挖掘获得） |
| `pollution:deepslate_amber_ore` | deepslate_amber_ore | deepslate_amber_ore | 世界生成矿石（可挖掘获得） |
| `pollution:deepslate_authority_lead_ore` | deepslate_authority_lead_ore | deepslate_authority_lead_ore | 世界生成矿石（可挖掘获得） |
| `pollution:deepslate_dragonstone_ore` | deepslate_dragonstone_ore | deepslate_dragonstone_ore | 世界生成矿石（可挖掘获得） |
| `pollution:deepslate_dumb_tin_ore` | deepslate_dumb_tin_ore | deepslate_dumb_tin_ore | 世界生成矿石（可挖掘获得） |
| `pollution:deepslate_elven_elementium_ore` | deepslate_elven_elementium_ore | deepslate_elven_elementium_ore | 世界生成矿石（可挖掘获得） |
| `pollution:deepslate_elven_quartz_ore` | deepslate_elven_quartz_ore | deepslate_elven_quartz_ore | 世界生成矿石（可挖掘获得） |
| `pollution:deepslate_flame_coal_ore` | deepslate_flame_coal_ore | deepslate_flame_coal_ore | 世界生成矿石（可挖掘获得） |
| `pollution:deepslate_infused_air_ore` | deepslate_infused_air_ore | deepslate_infused_air_ore | 世界生成矿石（可挖掘获得） |
| `pollution:deepslate_infused_alchemy_ore` | deepslate_infused_alchemy_ore | deepslate_infused_alchemy_ore | 世界生成矿石（可挖掘获得） |
| `pollution:deepslate_infused_alien_ore` | deepslate_infused_alien_ore | deepslate_infused_alien_ore | 世界生成矿石（可挖掘获得） |
| `pollution:deepslate_infused_animal_ore` | deepslate_infused_animal_ore | deepslate_infused_animal_ore | 世界生成矿石（可挖掘获得） |
| `pollution:deepslate_infused_armor_ore` | deepslate_infused_armor_ore | deepslate_infused_armor_ore | 世界生成矿石（可挖掘获得） |
| `pollution:deepslate_infused_aura_ore` | deepslate_infused_aura_ore | deepslate_infused_aura_ore | 世界生成矿石（可挖掘获得） |
| `pollution:deepslate_infused_cold_ore` | deepslate_infused_cold_ore | deepslate_infused_cold_ore | 世界生成矿石（可挖掘获得） |
| `pollution:deepslate_infused_craft_ore` | deepslate_infused_craft_ore | deepslate_infused_craft_ore | 世界生成矿石（可挖掘获得） |
| `pollution:deepslate_infused_crystal_ore` | deepslate_infused_crystal_ore | deepslate_infused_crystal_ore | 世界生成矿石（可挖掘获得） |
| `pollution:deepslate_infused_dark_ore` | deepslate_infused_dark_ore | deepslate_infused_dark_ore | 世界生成矿石（可挖掘获得） |
| `pollution:deepslate_infused_death_ore` | deepslate_infused_death_ore | deepslate_infused_death_ore | 世界生成矿石（可挖掘获得） |
| `pollution:deepslate_infused_earth_ore` | deepslate_infused_earth_ore | deepslate_infused_earth_ore | 世界生成矿石（可挖掘获得） |
| `pollution:deepslate_infused_energy_ore` | deepslate_infused_energy_ore | deepslate_infused_energy_ore | 世界生成矿石（可挖掘获得） |
| `pollution:deepslate_infused_entropy_ore` | deepslate_infused_entropy_ore | deepslate_infused_entropy_ore | 世界生成矿石（可挖掘获得） |
| `pollution:deepslate_infused_exchange_ore` | deepslate_infused_exchange_ore | deepslate_infused_exchange_ore | 世界生成矿石（可挖掘获得） |
| `pollution:deepslate_infused_fire_ore` | deepslate_infused_fire_ore | deepslate_infused_fire_ore | 世界生成矿石（可挖掘获得） |
| `pollution:deepslate_infused_fly_ore` | deepslate_infused_fly_ore | deepslate_infused_fly_ore | 世界生成矿石（可挖掘获得） |
| `pollution:deepslate_infused_greed_ore` | deepslate_infused_greed_ore | deepslate_infused_greed_ore | 世界生成矿石（可挖掘获得） |
| `pollution:deepslate_infused_human_ore` | deepslate_infused_human_ore | deepslate_infused_human_ore | 世界生成矿石（可挖掘获得） |
| `pollution:deepslate_infused_instrument_ore` | deepslate_infused_instrument_ore | deepslate_infused_instrument_ore | 世界生成矿石（可挖掘获得） |
| `pollution:deepslate_infused_life_ore` | deepslate_infused_life_ore | deepslate_infused_life_ore | 世界生成矿石（可挖掘获得） |
| `pollution:deepslate_infused_light_ore` | deepslate_infused_light_ore | deepslate_infused_light_ore | 世界生成矿石（可挖掘获得） |
| `pollution:deepslate_infused_magic_ore` | deepslate_infused_magic_ore | deepslate_infused_magic_ore | 世界生成矿石（可挖掘获得） |
| `pollution:deepslate_infused_mechanics_ore` | deepslate_infused_mechanics_ore | deepslate_infused_mechanics_ore | 世界生成矿石（可挖掘获得） |
| `pollution:deepslate_infused_metal_ore` | deepslate_infused_metal_ore | deepslate_infused_metal_ore | 世界生成矿石（可挖掘获得） |
| `pollution:deepslate_infused_motion_ore` | deepslate_infused_motion_ore | deepslate_infused_motion_ore | 世界生成矿石（可挖掘获得） |
| `pollution:deepslate_infused_order_ore` | deepslate_infused_order_ore | deepslate_infused_order_ore | 世界生成矿石（可挖掘获得） |
| `pollution:deepslate_infused_plant_ore` | deepslate_infused_plant_ore | deepslate_infused_plant_ore | 世界生成矿石（可挖掘获得） |
| `pollution:deepslate_infused_sense_ore` | deepslate_infused_sense_ore | deepslate_infused_sense_ore | 世界生成矿石（可挖掘获得） |
| `pollution:deepslate_infused_soul_ore` | deepslate_infused_soul_ore | deepslate_infused_soul_ore | 世界生成矿石（可挖掘获得） |
| `pollution:deepslate_infused_spatio_ore` | deepslate_infused_spatio_ore | deepslate_infused_spatio_ore | 世界生成矿石（可挖掘获得） |
| `pollution:deepslate_infused_taint_ore` | deepslate_infused_taint_ore | deepslate_infused_taint_ore | 世界生成矿石（可挖掘获得） |
| `pollution:deepslate_infused_tempus_ore` | deepslate_infused_tempus_ore | deepslate_infused_tempus_ore | 世界生成矿石（可挖掘获得） |
| `pollution:deepslate_infused_thought_ore` | deepslate_infused_thought_ore | deepslate_infused_thought_ore | 世界生成矿石（可挖掘获得） |
| `pollution:deepslate_infused_tinctura_ore` | deepslate_infused_tinctura_ore | deepslate_infused_tinctura_ore | 世界生成矿石（可挖掘获得） |
| `pollution:deepslate_infused_trap_ore` | deepslate_infused_trap_ore | deepslate_infused_trap_ore | 世界生成矿石（可挖掘获得） |
| `pollution:deepslate_infused_undead_ore` | deepslate_infused_undead_ore | deepslate_infused_undead_ore | 世界生成矿石（可挖掘获得） |
| `pollution:deepslate_infused_void_ore` | deepslate_infused_void_ore | deepslate_infused_void_ore | 世界生成矿石（可挖掘获得） |
| `pollution:deepslate_infused_water_ore` | deepslate_infused_water_ore | deepslate_infused_water_ore | 世界生成矿石（可挖掘获得） |
| `pollution:deepslate_infused_weapon_ore` | deepslate_infused_weapon_ore | deepslate_infused_weapon_ore | 世界生成矿石（可挖掘获得） |
| `pollution:deepslate_melt_gold_ore` | deepslate_melt_gold_ore | deepslate_melt_gold_ore | 世界生成矿石（可挖掘获得） |
| `pollution:deepslate_pixie_dust_ore` | deepslate_pixie_dust_ore | deepslate_pixie_dust_ore | 世界生成矿石（可挖掘获得） |
| `pollution:deepslate_pluto_zinc_ore` | deepslate_pluto_zinc_ore | deepslate_pluto_zinc_ore | 世界生成矿石（可挖掘获得） |
| `pollution:deepslate_pyrargyrite_ore` | deepslate_pyrargyrite_ore | deepslate_pyrargyrite_ore | 世界生成矿石（可挖掘获得） |
| `pollution:deepslate_scabyst_ore` | deepslate_scabyst_ore | deepslate_scabyst_ore | 世界生成矿石（可挖掘获得） |
| `pollution:diorite_amber_ore` | diorite_amber_ore | diorite_amber_ore | 世界生成矿石（可挖掘获得） |
| `pollution:diorite_authority_lead_ore` | diorite_authority_lead_ore | diorite_authority_lead_ore | 世界生成矿石（可挖掘获得） |
| `pollution:diorite_dragonstone_ore` | diorite_dragonstone_ore | diorite_dragonstone_ore | 世界生成矿石（可挖掘获得） |
| `pollution:diorite_dumb_tin_ore` | diorite_dumb_tin_ore | diorite_dumb_tin_ore | 世界生成矿石（可挖掘获得） |
| `pollution:diorite_elven_elementium_ore` | diorite_elven_elementium_ore | diorite_elven_elementium_ore | 世界生成矿石（可挖掘获得） |
| `pollution:diorite_elven_quartz_ore` | diorite_elven_quartz_ore | diorite_elven_quartz_ore | 世界生成矿石（可挖掘获得） |
| `pollution:diorite_flame_coal_ore` | diorite_flame_coal_ore | diorite_flame_coal_ore | 世界生成矿石（可挖掘获得） |
| `pollution:diorite_infused_air_ore` | diorite_infused_air_ore | diorite_infused_air_ore | 世界生成矿石（可挖掘获得） |
| `pollution:diorite_infused_alchemy_ore` | diorite_infused_alchemy_ore | diorite_infused_alchemy_ore | 世界生成矿石（可挖掘获得） |
| `pollution:diorite_infused_alien_ore` | diorite_infused_alien_ore | diorite_infused_alien_ore | 世界生成矿石（可挖掘获得） |
| `pollution:diorite_infused_animal_ore` | diorite_infused_animal_ore | diorite_infused_animal_ore | 世界生成矿石（可挖掘获得） |
| `pollution:diorite_infused_armor_ore` | diorite_infused_armor_ore | diorite_infused_armor_ore | 世界生成矿石（可挖掘获得） |
| `pollution:diorite_infused_aura_ore` | diorite_infused_aura_ore | diorite_infused_aura_ore | 世界生成矿石（可挖掘获得） |
| `pollution:diorite_infused_cold_ore` | diorite_infused_cold_ore | diorite_infused_cold_ore | 世界生成矿石（可挖掘获得） |
| `pollution:diorite_infused_craft_ore` | diorite_infused_craft_ore | diorite_infused_craft_ore | 世界生成矿石（可挖掘获得） |
| `pollution:diorite_infused_crystal_ore` | diorite_infused_crystal_ore | diorite_infused_crystal_ore | 世界生成矿石（可挖掘获得） |
| `pollution:diorite_infused_dark_ore` | diorite_infused_dark_ore | diorite_infused_dark_ore | 世界生成矿石（可挖掘获得） |
| `pollution:diorite_infused_death_ore` | diorite_infused_death_ore | diorite_infused_death_ore | 世界生成矿石（可挖掘获得） |
| `pollution:diorite_infused_earth_ore` | diorite_infused_earth_ore | diorite_infused_earth_ore | 世界生成矿石（可挖掘获得） |
| `pollution:diorite_infused_energy_ore` | diorite_infused_energy_ore | diorite_infused_energy_ore | 世界生成矿石（可挖掘获得） |
| `pollution:diorite_infused_entropy_ore` | diorite_infused_entropy_ore | diorite_infused_entropy_ore | 世界生成矿石（可挖掘获得） |
| `pollution:diorite_infused_exchange_ore` | diorite_infused_exchange_ore | diorite_infused_exchange_ore | 世界生成矿石（可挖掘获得） |
| `pollution:diorite_infused_fire_ore` | diorite_infused_fire_ore | diorite_infused_fire_ore | 世界生成矿石（可挖掘获得） |
| `pollution:diorite_infused_fly_ore` | diorite_infused_fly_ore | diorite_infused_fly_ore | 世界生成矿石（可挖掘获得） |
| `pollution:diorite_infused_greed_ore` | diorite_infused_greed_ore | diorite_infused_greed_ore | 世界生成矿石（可挖掘获得） |
| `pollution:diorite_infused_human_ore` | diorite_infused_human_ore | diorite_infused_human_ore | 世界生成矿石（可挖掘获得） |
| `pollution:diorite_infused_instrument_ore` | diorite_infused_instrument_ore | diorite_infused_instrument_ore | 世界生成矿石（可挖掘获得） |
| `pollution:diorite_infused_life_ore` | diorite_infused_life_ore | diorite_infused_life_ore | 世界生成矿石（可挖掘获得） |
| `pollution:diorite_infused_light_ore` | diorite_infused_light_ore | diorite_infused_light_ore | 世界生成矿石（可挖掘获得） |
| `pollution:diorite_infused_magic_ore` | diorite_infused_magic_ore | diorite_infused_magic_ore | 世界生成矿石（可挖掘获得） |
| `pollution:diorite_infused_mechanics_ore` | diorite_infused_mechanics_ore | diorite_infused_mechanics_ore | 世界生成矿石（可挖掘获得） |
| `pollution:diorite_infused_metal_ore` | diorite_infused_metal_ore | diorite_infused_metal_ore | 世界生成矿石（可挖掘获得） |
| `pollution:diorite_infused_motion_ore` | diorite_infused_motion_ore | diorite_infused_motion_ore | 世界生成矿石（可挖掘获得） |
| `pollution:diorite_infused_order_ore` | diorite_infused_order_ore | diorite_infused_order_ore | 世界生成矿石（可挖掘获得） |
| `pollution:diorite_infused_plant_ore` | diorite_infused_plant_ore | diorite_infused_plant_ore | 世界生成矿石（可挖掘获得） |
| `pollution:diorite_infused_sense_ore` | diorite_infused_sense_ore | diorite_infused_sense_ore | 世界生成矿石（可挖掘获得） |
| `pollution:diorite_infused_soul_ore` | diorite_infused_soul_ore | diorite_infused_soul_ore | 世界生成矿石（可挖掘获得） |
| `pollution:diorite_infused_spatio_ore` | diorite_infused_spatio_ore | diorite_infused_spatio_ore | 世界生成矿石（可挖掘获得） |
| `pollution:diorite_infused_taint_ore` | diorite_infused_taint_ore | diorite_infused_taint_ore | 世界生成矿石（可挖掘获得） |
| `pollution:diorite_infused_tempus_ore` | diorite_infused_tempus_ore | diorite_infused_tempus_ore | 世界生成矿石（可挖掘获得） |
| `pollution:diorite_infused_thought_ore` | diorite_infused_thought_ore | diorite_infused_thought_ore | 世界生成矿石（可挖掘获得） |
| `pollution:diorite_infused_tinctura_ore` | diorite_infused_tinctura_ore | diorite_infused_tinctura_ore | 世界生成矿石（可挖掘获得） |
| `pollution:diorite_infused_trap_ore` | diorite_infused_trap_ore | diorite_infused_trap_ore | 世界生成矿石（可挖掘获得） |
| `pollution:diorite_infused_undead_ore` | diorite_infused_undead_ore | diorite_infused_undead_ore | 世界生成矿石（可挖掘获得） |
| `pollution:diorite_infused_void_ore` | diorite_infused_void_ore | diorite_infused_void_ore | 世界生成矿石（可挖掘获得） |
| `pollution:diorite_infused_water_ore` | diorite_infused_water_ore | diorite_infused_water_ore | 世界生成矿石（可挖掘获得） |
| `pollution:diorite_infused_weapon_ore` | diorite_infused_weapon_ore | diorite_infused_weapon_ore | 世界生成矿石（可挖掘获得） |
| `pollution:diorite_melt_gold_ore` | diorite_melt_gold_ore | diorite_melt_gold_ore | 世界生成矿石（可挖掘获得） |
| `pollution:diorite_pixie_dust_ore` | diorite_pixie_dust_ore | diorite_pixie_dust_ore | 世界生成矿石（可挖掘获得） |
| `pollution:diorite_pluto_zinc_ore` | diorite_pluto_zinc_ore | diorite_pluto_zinc_ore | 世界生成矿石（可挖掘获得） |
| `pollution:diorite_pyrargyrite_ore` | diorite_pyrargyrite_ore | diorite_pyrargyrite_ore | 世界生成矿石（可挖掘获得） |
| `pollution:diorite_scabyst_ore` | diorite_scabyst_ore | diorite_scabyst_ore | 世界生成矿石（可挖掘获得） |
| `pollution:dragonstone_ore` | Dragonstone Ore | 龙石矿石 | 世界生成矿石（可挖掘获得） |
| `pollution:dumb_tin_ore` | Dumb Tin Ore | 哑泽锡矿石 | 世界生成矿石（可挖掘获得） |
| `pollution:elven_elementium_ore` | Elven Elementium Ore | 精灵元素矿石 | 世界生成矿石（可挖掘获得） |
| `pollution:elven_quartz_ore` | Elven Quartz Ore | 精灵石英矿石 | 世界生成矿石（可挖掘获得） |
| `pollution:endstone_amber_ore` | endstone_amber_ore | endstone_amber_ore | 世界生成矿石（可挖掘获得） |
| `pollution:endstone_authority_lead_ore` | endstone_authority_lead_ore | endstone_authority_lead_ore | 世界生成矿石（可挖掘获得） |
| `pollution:endstone_dragonstone_ore` | endstone_dragonstone_ore | endstone_dragonstone_ore | 世界生成矿石（可挖掘获得） |
| `pollution:endstone_dumb_tin_ore` | endstone_dumb_tin_ore | endstone_dumb_tin_ore | 世界生成矿石（可挖掘获得） |
| `pollution:endstone_elven_elementium_ore` | endstone_elven_elementium_ore | endstone_elven_elementium_ore | 世界生成矿石（可挖掘获得） |
| `pollution:endstone_elven_quartz_ore` | endstone_elven_quartz_ore | endstone_elven_quartz_ore | 世界生成矿石（可挖掘获得） |
| `pollution:endstone_flame_coal_ore` | endstone_flame_coal_ore | endstone_flame_coal_ore | 世界生成矿石（可挖掘获得） |
| `pollution:endstone_infused_air_ore` | endstone_infused_air_ore | endstone_infused_air_ore | 世界生成矿石（可挖掘获得） |
| `pollution:endstone_infused_alchemy_ore` | endstone_infused_alchemy_ore | endstone_infused_alchemy_ore | 世界生成矿石（可挖掘获得） |
| `pollution:endstone_infused_alien_ore` | endstone_infused_alien_ore | endstone_infused_alien_ore | 世界生成矿石（可挖掘获得） |
| `pollution:endstone_infused_animal_ore` | endstone_infused_animal_ore | endstone_infused_animal_ore | 世界生成矿石（可挖掘获得） |
| `pollution:endstone_infused_armor_ore` | endstone_infused_armor_ore | endstone_infused_armor_ore | 世界生成矿石（可挖掘获得） |
| `pollution:endstone_infused_aura_ore` | endstone_infused_aura_ore | endstone_infused_aura_ore | 世界生成矿石（可挖掘获得） |
| `pollution:endstone_infused_cold_ore` | endstone_infused_cold_ore | endstone_infused_cold_ore | 世界生成矿石（可挖掘获得） |
| `pollution:endstone_infused_craft_ore` | endstone_infused_craft_ore | endstone_infused_craft_ore | 世界生成矿石（可挖掘获得） |
| `pollution:endstone_infused_crystal_ore` | endstone_infused_crystal_ore | endstone_infused_crystal_ore | 世界生成矿石（可挖掘获得） |
| `pollution:endstone_infused_dark_ore` | endstone_infused_dark_ore | endstone_infused_dark_ore | 世界生成矿石（可挖掘获得） |
| `pollution:endstone_infused_death_ore` | endstone_infused_death_ore | endstone_infused_death_ore | 世界生成矿石（可挖掘获得） |
| `pollution:endstone_infused_earth_ore` | endstone_infused_earth_ore | endstone_infused_earth_ore | 世界生成矿石（可挖掘获得） |
| `pollution:endstone_infused_energy_ore` | endstone_infused_energy_ore | endstone_infused_energy_ore | 世界生成矿石（可挖掘获得） |
| `pollution:endstone_infused_entropy_ore` | endstone_infused_entropy_ore | endstone_infused_entropy_ore | 世界生成矿石（可挖掘获得） |
| `pollution:endstone_infused_exchange_ore` | endstone_infused_exchange_ore | endstone_infused_exchange_ore | 世界生成矿石（可挖掘获得） |
| `pollution:endstone_infused_fire_ore` | endstone_infused_fire_ore | endstone_infused_fire_ore | 世界生成矿石（可挖掘获得） |
| `pollution:endstone_infused_fly_ore` | endstone_infused_fly_ore | endstone_infused_fly_ore | 世界生成矿石（可挖掘获得） |
| `pollution:endstone_infused_greed_ore` | endstone_infused_greed_ore | endstone_infused_greed_ore | 世界生成矿石（可挖掘获得） |
| `pollution:endstone_infused_human_ore` | endstone_infused_human_ore | endstone_infused_human_ore | 世界生成矿石（可挖掘获得） |
| `pollution:endstone_infused_instrument_ore` | endstone_infused_instrument_ore | endstone_infused_instrument_ore | 世界生成矿石（可挖掘获得） |
| `pollution:endstone_infused_life_ore` | endstone_infused_life_ore | endstone_infused_life_ore | 世界生成矿石（可挖掘获得） |
| `pollution:endstone_infused_light_ore` | endstone_infused_light_ore | endstone_infused_light_ore | 世界生成矿石（可挖掘获得） |
| `pollution:endstone_infused_magic_ore` | endstone_infused_magic_ore | endstone_infused_magic_ore | 世界生成矿石（可挖掘获得） |
| `pollution:endstone_infused_mechanics_ore` | endstone_infused_mechanics_ore | endstone_infused_mechanics_ore | 世界生成矿石（可挖掘获得） |
| `pollution:endstone_infused_metal_ore` | endstone_infused_metal_ore | endstone_infused_metal_ore | 世界生成矿石（可挖掘获得） |
| `pollution:endstone_infused_motion_ore` | endstone_infused_motion_ore | endstone_infused_motion_ore | 世界生成矿石（可挖掘获得） |
| `pollution:endstone_infused_order_ore` | endstone_infused_order_ore | endstone_infused_order_ore | 世界生成矿石（可挖掘获得） |
| `pollution:endstone_infused_plant_ore` | endstone_infused_plant_ore | endstone_infused_plant_ore | 世界生成矿石（可挖掘获得） |
| `pollution:endstone_infused_sense_ore` | endstone_infused_sense_ore | endstone_infused_sense_ore | 世界生成矿石（可挖掘获得） |
| `pollution:endstone_infused_soul_ore` | endstone_infused_soul_ore | endstone_infused_soul_ore | 世界生成矿石（可挖掘获得） |
| `pollution:endstone_infused_spatio_ore` | endstone_infused_spatio_ore | endstone_infused_spatio_ore | 世界生成矿石（可挖掘获得） |
| `pollution:endstone_infused_taint_ore` | endstone_infused_taint_ore | endstone_infused_taint_ore | 世界生成矿石（可挖掘获得） |
| `pollution:endstone_infused_tempus_ore` | endstone_infused_tempus_ore | endstone_infused_tempus_ore | 世界生成矿石（可挖掘获得） |
| `pollution:endstone_infused_thought_ore` | endstone_infused_thought_ore | endstone_infused_thought_ore | 世界生成矿石（可挖掘获得） |
| `pollution:endstone_infused_tinctura_ore` | endstone_infused_tinctura_ore | endstone_infused_tinctura_ore | 世界生成矿石（可挖掘获得） |
| `pollution:endstone_infused_trap_ore` | endstone_infused_trap_ore | endstone_infused_trap_ore | 世界生成矿石（可挖掘获得） |
| `pollution:endstone_infused_undead_ore` | endstone_infused_undead_ore | endstone_infused_undead_ore | 世界生成矿石（可挖掘获得） |
| `pollution:endstone_infused_void_ore` | endstone_infused_void_ore | endstone_infused_void_ore | 世界生成矿石（可挖掘获得） |
| `pollution:endstone_infused_water_ore` | endstone_infused_water_ore | endstone_infused_water_ore | 世界生成矿石（可挖掘获得） |
| `pollution:endstone_infused_weapon_ore` | endstone_infused_weapon_ore | endstone_infused_weapon_ore | 世界生成矿石（可挖掘获得） |
| `pollution:endstone_melt_gold_ore` | endstone_melt_gold_ore | endstone_melt_gold_ore | 世界生成矿石（可挖掘获得） |
| `pollution:endstone_pixie_dust_ore` | endstone_pixie_dust_ore | endstone_pixie_dust_ore | 世界生成矿石（可挖掘获得） |
| `pollution:endstone_pluto_zinc_ore` | endstone_pluto_zinc_ore | endstone_pluto_zinc_ore | 世界生成矿石（可挖掘获得） |
| `pollution:endstone_pyrargyrite_ore` | endstone_pyrargyrite_ore | endstone_pyrargyrite_ore | 世界生成矿石（可挖掘获得） |
| `pollution:endstone_scabyst_ore` | endstone_scabyst_ore | endstone_scabyst_ore | 世界生成矿石（可挖掘获得） |
| `pollution:flame_coal_ore` | Flame Coal Ore | 固焰煤矿石 | 世界生成矿石（可挖掘获得） |
| `pollution:granite_amber_ore` | granite_amber_ore | granite_amber_ore | 世界生成矿石（可挖掘获得） |
| `pollution:granite_authority_lead_ore` | granite_authority_lead_ore | granite_authority_lead_ore | 世界生成矿石（可挖掘获得） |
| `pollution:granite_dragonstone_ore` | granite_dragonstone_ore | granite_dragonstone_ore | 世界生成矿石（可挖掘获得） |
| `pollution:granite_dumb_tin_ore` | granite_dumb_tin_ore | granite_dumb_tin_ore | 世界生成矿石（可挖掘获得） |
| `pollution:granite_elven_elementium_ore` | granite_elven_elementium_ore | granite_elven_elementium_ore | 世界生成矿石（可挖掘获得） |
| `pollution:granite_elven_quartz_ore` | granite_elven_quartz_ore | granite_elven_quartz_ore | 世界生成矿石（可挖掘获得） |
| `pollution:granite_flame_coal_ore` | granite_flame_coal_ore | granite_flame_coal_ore | 世界生成矿石（可挖掘获得） |
| `pollution:granite_infused_air_ore` | granite_infused_air_ore | granite_infused_air_ore | 世界生成矿石（可挖掘获得） |
| `pollution:granite_infused_alchemy_ore` | granite_infused_alchemy_ore | granite_infused_alchemy_ore | 世界生成矿石（可挖掘获得） |
| `pollution:granite_infused_alien_ore` | granite_infused_alien_ore | granite_infused_alien_ore | 世界生成矿石（可挖掘获得） |
| `pollution:granite_infused_animal_ore` | granite_infused_animal_ore | granite_infused_animal_ore | 世界生成矿石（可挖掘获得） |
| `pollution:granite_infused_armor_ore` | granite_infused_armor_ore | granite_infused_armor_ore | 世界生成矿石（可挖掘获得） |
| `pollution:granite_infused_aura_ore` | granite_infused_aura_ore | granite_infused_aura_ore | 世界生成矿石（可挖掘获得） |
| `pollution:granite_infused_cold_ore` | granite_infused_cold_ore | granite_infused_cold_ore | 世界生成矿石（可挖掘获得） |
| `pollution:granite_infused_craft_ore` | granite_infused_craft_ore | granite_infused_craft_ore | 世界生成矿石（可挖掘获得） |
| `pollution:granite_infused_crystal_ore` | granite_infused_crystal_ore | granite_infused_crystal_ore | 世界生成矿石（可挖掘获得） |
| `pollution:granite_infused_dark_ore` | granite_infused_dark_ore | granite_infused_dark_ore | 世界生成矿石（可挖掘获得） |
| `pollution:granite_infused_death_ore` | granite_infused_death_ore | granite_infused_death_ore | 世界生成矿石（可挖掘获得） |
| `pollution:granite_infused_earth_ore` | granite_infused_earth_ore | granite_infused_earth_ore | 世界生成矿石（可挖掘获得） |
| `pollution:granite_infused_energy_ore` | granite_infused_energy_ore | granite_infused_energy_ore | 世界生成矿石（可挖掘获得） |
| `pollution:granite_infused_entropy_ore` | granite_infused_entropy_ore | granite_infused_entropy_ore | 世界生成矿石（可挖掘获得） |
| `pollution:granite_infused_exchange_ore` | granite_infused_exchange_ore | granite_infused_exchange_ore | 世界生成矿石（可挖掘获得） |
| `pollution:granite_infused_fire_ore` | granite_infused_fire_ore | granite_infused_fire_ore | 世界生成矿石（可挖掘获得） |
| `pollution:granite_infused_fly_ore` | granite_infused_fly_ore | granite_infused_fly_ore | 世界生成矿石（可挖掘获得） |
| `pollution:granite_infused_greed_ore` | granite_infused_greed_ore | granite_infused_greed_ore | 世界生成矿石（可挖掘获得） |
| `pollution:granite_infused_human_ore` | granite_infused_human_ore | granite_infused_human_ore | 世界生成矿石（可挖掘获得） |
| `pollution:granite_infused_instrument_ore` | granite_infused_instrument_ore | granite_infused_instrument_ore | 世界生成矿石（可挖掘获得） |
| `pollution:granite_infused_life_ore` | granite_infused_life_ore | granite_infused_life_ore | 世界生成矿石（可挖掘获得） |
| `pollution:granite_infused_light_ore` | granite_infused_light_ore | granite_infused_light_ore | 世界生成矿石（可挖掘获得） |
| `pollution:granite_infused_magic_ore` | granite_infused_magic_ore | granite_infused_magic_ore | 世界生成矿石（可挖掘获得） |
| `pollution:granite_infused_mechanics_ore` | granite_infused_mechanics_ore | granite_infused_mechanics_ore | 世界生成矿石（可挖掘获得） |
| `pollution:granite_infused_metal_ore` | granite_infused_metal_ore | granite_infused_metal_ore | 世界生成矿石（可挖掘获得） |
| `pollution:granite_infused_motion_ore` | granite_infused_motion_ore | granite_infused_motion_ore | 世界生成矿石（可挖掘获得） |
| `pollution:granite_infused_order_ore` | granite_infused_order_ore | granite_infused_order_ore | 世界生成矿石（可挖掘获得） |
| `pollution:granite_infused_plant_ore` | granite_infused_plant_ore | granite_infused_plant_ore | 世界生成矿石（可挖掘获得） |
| `pollution:granite_infused_sense_ore` | granite_infused_sense_ore | granite_infused_sense_ore | 世界生成矿石（可挖掘获得） |
| `pollution:granite_infused_soul_ore` | granite_infused_soul_ore | granite_infused_soul_ore | 世界生成矿石（可挖掘获得） |
| `pollution:granite_infused_spatio_ore` | granite_infused_spatio_ore | granite_infused_spatio_ore | 世界生成矿石（可挖掘获得） |
| `pollution:granite_infused_taint_ore` | granite_infused_taint_ore | granite_infused_taint_ore | 世界生成矿石（可挖掘获得） |
| `pollution:granite_infused_tempus_ore` | granite_infused_tempus_ore | granite_infused_tempus_ore | 世界生成矿石（可挖掘获得） |
| `pollution:granite_infused_thought_ore` | granite_infused_thought_ore | granite_infused_thought_ore | 世界生成矿石（可挖掘获得） |
| `pollution:granite_infused_tinctura_ore` | granite_infused_tinctura_ore | granite_infused_tinctura_ore | 世界生成矿石（可挖掘获得） |
| `pollution:granite_infused_trap_ore` | granite_infused_trap_ore | granite_infused_trap_ore | 世界生成矿石（可挖掘获得） |
| `pollution:granite_infused_undead_ore` | granite_infused_undead_ore | granite_infused_undead_ore | 世界生成矿石（可挖掘获得） |
| `pollution:granite_infused_void_ore` | granite_infused_void_ore | granite_infused_void_ore | 世界生成矿石（可挖掘获得） |
| `pollution:granite_infused_water_ore` | granite_infused_water_ore | granite_infused_water_ore | 世界生成矿石（可挖掘获得） |
| `pollution:granite_infused_weapon_ore` | granite_infused_weapon_ore | granite_infused_weapon_ore | 世界生成矿石（可挖掘获得） |
| `pollution:granite_melt_gold_ore` | granite_melt_gold_ore | granite_melt_gold_ore | 世界生成矿石（可挖掘获得） |
| `pollution:granite_pixie_dust_ore` | granite_pixie_dust_ore | granite_pixie_dust_ore | 世界生成矿石（可挖掘获得） |
| `pollution:granite_pluto_zinc_ore` | granite_pluto_zinc_ore | granite_pluto_zinc_ore | 世界生成矿石（可挖掘获得） |
| `pollution:granite_pyrargyrite_ore` | granite_pyrargyrite_ore | granite_pyrargyrite_ore | 世界生成矿石（可挖掘获得） |
| `pollution:granite_scabyst_ore` | granite_scabyst_ore | granite_scabyst_ore | 世界生成矿石（可挖掘获得） |
| `pollution:gravel_amber_ore` | gravel_amber_ore | gravel_amber_ore | 世界生成矿石（可挖掘获得） |
| `pollution:gravel_authority_lead_ore` | gravel_authority_lead_ore | gravel_authority_lead_ore | 世界生成矿石（可挖掘获得） |
| `pollution:gravel_dragonstone_ore` | gravel_dragonstone_ore | gravel_dragonstone_ore | 世界生成矿石（可挖掘获得） |
| `pollution:gravel_dumb_tin_ore` | gravel_dumb_tin_ore | gravel_dumb_tin_ore | 世界生成矿石（可挖掘获得） |
| `pollution:gravel_elven_elementium_ore` | gravel_elven_elementium_ore | gravel_elven_elementium_ore | 世界生成矿石（可挖掘获得） |
| `pollution:gravel_elven_quartz_ore` | gravel_elven_quartz_ore | gravel_elven_quartz_ore | 世界生成矿石（可挖掘获得） |
| `pollution:gravel_flame_coal_ore` | gravel_flame_coal_ore | gravel_flame_coal_ore | 世界生成矿石（可挖掘获得） |
| `pollution:gravel_infused_air_ore` | gravel_infused_air_ore | gravel_infused_air_ore | 世界生成矿石（可挖掘获得） |
| `pollution:gravel_infused_alchemy_ore` | gravel_infused_alchemy_ore | gravel_infused_alchemy_ore | 世界生成矿石（可挖掘获得） |
| `pollution:gravel_infused_alien_ore` | gravel_infused_alien_ore | gravel_infused_alien_ore | 世界生成矿石（可挖掘获得） |
| `pollution:gravel_infused_animal_ore` | gravel_infused_animal_ore | gravel_infused_animal_ore | 世界生成矿石（可挖掘获得） |
| `pollution:gravel_infused_armor_ore` | gravel_infused_armor_ore | gravel_infused_armor_ore | 世界生成矿石（可挖掘获得） |
| `pollution:gravel_infused_aura_ore` | gravel_infused_aura_ore | gravel_infused_aura_ore | 世界生成矿石（可挖掘获得） |
| `pollution:gravel_infused_cold_ore` | gravel_infused_cold_ore | gravel_infused_cold_ore | 世界生成矿石（可挖掘获得） |
| `pollution:gravel_infused_craft_ore` | gravel_infused_craft_ore | gravel_infused_craft_ore | 世界生成矿石（可挖掘获得） |
| `pollution:gravel_infused_crystal_ore` | gravel_infused_crystal_ore | gravel_infused_crystal_ore | 世界生成矿石（可挖掘获得） |
| `pollution:gravel_infused_dark_ore` | gravel_infused_dark_ore | gravel_infused_dark_ore | 世界生成矿石（可挖掘获得） |
| `pollution:gravel_infused_death_ore` | gravel_infused_death_ore | gravel_infused_death_ore | 世界生成矿石（可挖掘获得） |
| `pollution:gravel_infused_earth_ore` | gravel_infused_earth_ore | gravel_infused_earth_ore | 世界生成矿石（可挖掘获得） |
| `pollution:gravel_infused_energy_ore` | gravel_infused_energy_ore | gravel_infused_energy_ore | 世界生成矿石（可挖掘获得） |
| `pollution:gravel_infused_entropy_ore` | gravel_infused_entropy_ore | gravel_infused_entropy_ore | 世界生成矿石（可挖掘获得） |
| `pollution:gravel_infused_exchange_ore` | gravel_infused_exchange_ore | gravel_infused_exchange_ore | 世界生成矿石（可挖掘获得） |
| `pollution:gravel_infused_fire_ore` | gravel_infused_fire_ore | gravel_infused_fire_ore | 世界生成矿石（可挖掘获得） |
| `pollution:gravel_infused_fly_ore` | gravel_infused_fly_ore | gravel_infused_fly_ore | 世界生成矿石（可挖掘获得） |
| `pollution:gravel_infused_greed_ore` | gravel_infused_greed_ore | gravel_infused_greed_ore | 世界生成矿石（可挖掘获得） |
| `pollution:gravel_infused_human_ore` | gravel_infused_human_ore | gravel_infused_human_ore | 世界生成矿石（可挖掘获得） |
| `pollution:gravel_infused_instrument_ore` | gravel_infused_instrument_ore | gravel_infused_instrument_ore | 世界生成矿石（可挖掘获得） |
| `pollution:gravel_infused_life_ore` | gravel_infused_life_ore | gravel_infused_life_ore | 世界生成矿石（可挖掘获得） |
| `pollution:gravel_infused_light_ore` | gravel_infused_light_ore | gravel_infused_light_ore | 世界生成矿石（可挖掘获得） |
| `pollution:gravel_infused_magic_ore` | gravel_infused_magic_ore | gravel_infused_magic_ore | 世界生成矿石（可挖掘获得） |
| `pollution:gravel_infused_mechanics_ore` | gravel_infused_mechanics_ore | gravel_infused_mechanics_ore | 世界生成矿石（可挖掘获得） |
| `pollution:gravel_infused_metal_ore` | gravel_infused_metal_ore | gravel_infused_metal_ore | 世界生成矿石（可挖掘获得） |
| `pollution:gravel_infused_motion_ore` | gravel_infused_motion_ore | gravel_infused_motion_ore | 世界生成矿石（可挖掘获得） |
| `pollution:gravel_infused_order_ore` | gravel_infused_order_ore | gravel_infused_order_ore | 世界生成矿石（可挖掘获得） |
| `pollution:gravel_infused_plant_ore` | gravel_infused_plant_ore | gravel_infused_plant_ore | 世界生成矿石（可挖掘获得） |
| `pollution:gravel_infused_sense_ore` | gravel_infused_sense_ore | gravel_infused_sense_ore | 世界生成矿石（可挖掘获得） |
| `pollution:gravel_infused_soul_ore` | gravel_infused_soul_ore | gravel_infused_soul_ore | 世界生成矿石（可挖掘获得） |
| `pollution:gravel_infused_spatio_ore` | gravel_infused_spatio_ore | gravel_infused_spatio_ore | 世界生成矿石（可挖掘获得） |
| `pollution:gravel_infused_taint_ore` | gravel_infused_taint_ore | gravel_infused_taint_ore | 世界生成矿石（可挖掘获得） |
| `pollution:gravel_infused_tempus_ore` | gravel_infused_tempus_ore | gravel_infused_tempus_ore | 世界生成矿石（可挖掘获得） |
| `pollution:gravel_infused_thought_ore` | gravel_infused_thought_ore | gravel_infused_thought_ore | 世界生成矿石（可挖掘获得） |
| `pollution:gravel_infused_tinctura_ore` | gravel_infused_tinctura_ore | gravel_infused_tinctura_ore | 世界生成矿石（可挖掘获得） |
| `pollution:gravel_infused_trap_ore` | gravel_infused_trap_ore | gravel_infused_trap_ore | 世界生成矿石（可挖掘获得） |
| `pollution:gravel_infused_undead_ore` | gravel_infused_undead_ore | gravel_infused_undead_ore | 世界生成矿石（可挖掘获得） |
| `pollution:gravel_infused_void_ore` | gravel_infused_void_ore | gravel_infused_void_ore | 世界生成矿石（可挖掘获得） |
| `pollution:gravel_infused_water_ore` | gravel_infused_water_ore | gravel_infused_water_ore | 世界生成矿石（可挖掘获得） |
| `pollution:gravel_infused_weapon_ore` | gravel_infused_weapon_ore | gravel_infused_weapon_ore | 世界生成矿石（可挖掘获得） |
| `pollution:gravel_melt_gold_ore` | gravel_melt_gold_ore | gravel_melt_gold_ore | 世界生成矿石（可挖掘获得） |
| `pollution:gravel_pixie_dust_ore` | gravel_pixie_dust_ore | gravel_pixie_dust_ore | 世界生成矿石（可挖掘获得） |
| `pollution:gravel_pluto_zinc_ore` | gravel_pluto_zinc_ore | gravel_pluto_zinc_ore | 世界生成矿石（可挖掘获得） |
| `pollution:gravel_pyrargyrite_ore` | gravel_pyrargyrite_ore | gravel_pyrargyrite_ore | 世界生成矿石（可挖掘获得） |
| `pollution:gravel_scabyst_ore` | gravel_scabyst_ore | gravel_scabyst_ore | 世界生成矿石（可挖掘获得） |
| `pollution:infused_air_ore` | Infused Air Ore | 气矿石 | 世界生成矿石（可挖掘获得） |
| `pollution:infused_alchemy_ore` | Infused Alchemy Ore | 炼金矿石 | 世界生成矿石（可挖掘获得） |
| `pollution:infused_alien_ore` | Infused Alien Ore | 异矿石 | 世界生成矿石（可挖掘获得） |
| `pollution:infused_animal_ore` | Infused Animal Ore | 造化矿石 | 世界生成矿石（可挖掘获得） |
| `pollution:infused_armor_ore` | Infused Armor Ore | 胄矿石 | 世界生成矿石（可挖掘获得） |
| `pollution:infused_aura_ore` | Infused Aura Ore | 灵矿石 | 世界生成矿石（可挖掘获得） |
| `pollution:infused_cold_ore` | Infused Cold Ore | 寒矿石 | 世界生成矿石（可挖掘获得） |
| `pollution:infused_craft_ore` | Infused Craft Ore | 创矿石 | 世界生成矿石（可挖掘获得） |
| `pollution:infused_crystal_ore` | Infused Crystal Ore | 晶矿石 | 世界生成矿石（可挖掘获得） |
| `pollution:infused_dark_ore` | Infused Dark Ore | 暗矿石 | 世界生成矿石（可挖掘获得） |
| `pollution:infused_death_ore` | Infused Death Ore | 死矿石 | 世界生成矿石（可挖掘获得） |
| `pollution:infused_earth_ore` | Infused Earth Ore | 土矿石 | 世界生成矿石（可挖掘获得） |
| `pollution:infused_energy_ore` | Infused Energy Ore | 能矿石 | 世界生成矿石（可挖掘获得） |
| `pollution:infused_entropy_ore` | Infused Entropy Ore | 熵矿石 | 世界生成矿石（可挖掘获得） |
| `pollution:infused_exchange_ore` | Infused Exchange Ore | 易矿石 | 世界生成矿石（可挖掘获得） |
| `pollution:infused_fire_ore` | Infused Fire Ore | 焱矿石 | 世界生成矿石（可挖掘获得） |
| `pollution:infused_fly_ore` | Infused Fly Ore | 羽矿石 | 世界生成矿石（可挖掘获得） |
| `pollution:infused_greed_ore` | Infused Greed Ore | 贪矿石 | 世界生成矿石（可挖掘获得） |
| `pollution:infused_human_ore` | Infused Human Ore | 物灵矿石 | 世界生成矿石（可挖掘获得） |
| `pollution:infused_instrument_ore` | Infused Instrument Ore | 器矿石 | 世界生成矿石（可挖掘获得） |
| `pollution:infused_life_ore` | Infused Life Ore | 生矿石 | 世界生成矿石（可挖掘获得） |
| `pollution:infused_light_ore` | Infused Light Ore | 光矿石 | 世界生成矿石（可挖掘获得） |
| `pollution:infused_magic_ore` | Infused Magic Ore | 魔矿石 | 世界生成矿石（可挖掘获得） |
| `pollution:infused_mechanics_ore` | Infused Mechanics Ore | 械矿石 | 世界生成矿石（可挖掘获得） |
| `pollution:infused_metal_ore` | Infused Metal Ore | 金属矿石 | 世界生成矿石（可挖掘获得） |
| `pollution:infused_motion_ore` | Infused Motion Ore | 动矿石 | 世界生成矿石（可挖掘获得） |
| `pollution:infused_order_ore` | Infused Order Ore | 序矿石 | 世界生成矿石（可挖掘获得） |
| `pollution:infused_plant_ore` | Infused Plant Ore | 草木矿石 | 世界生成矿石（可挖掘获得） |
| `pollution:infused_sense_ore` | Infused Sense Ore | 感矿石 | 世界生成矿石（可挖掘获得） |
| `pollution:infused_soul_ore` | Infused Soul Ore | 魂矿石 | 世界生成矿石（可挖掘获得） |
| `pollution:infused_spatio_ore` | Infused Spatio Ore | 空矿石 | 世界生成矿石（可挖掘获得） |
| `pollution:infused_taint_ore` | Infused Taint Ore | 秽矿石 | 世界生成矿石（可挖掘获得） |
| `pollution:infused_tempus_ore` | Infused Tempus Ore | 时矿石 | 世界生成矿石（可挖掘获得） |
| `pollution:infused_thought_ore` | Infused Thought Ore | 思矿石 | 世界生成矿石（可挖掘获得） |
| `pollution:infused_tinctura_ore` | Infused Tinctura Ore | 艺矿石 | 世界生成矿石（可挖掘获得） |
| `pollution:infused_trap_ore` | Infused Trap Ore | 缚矿石 | 世界生成矿石（可挖掘获得） |
| `pollution:infused_undead_ore` | Infused Undead Ore | 僵矿石 | 世界生成矿石（可挖掘获得） |
| `pollution:infused_void_ore` | Infused Void Ore | 虚矿石 | 世界生成矿石（可挖掘获得） |
| `pollution:infused_water_ore` | Infused Water Ore | 流矿石 | 世界生成矿石（可挖掘获得） |
| `pollution:infused_weapon_ore` | Infused Weapon Ore | 武矿石 | 世界生成矿石（可挖掘获得） |
| `pollution:marble_amber_ore` | marble_amber_ore | marble_amber_ore | 世界生成矿石（可挖掘获得） |
| `pollution:marble_authority_lead_ore` | marble_authority_lead_ore | marble_authority_lead_ore | 世界生成矿石（可挖掘获得） |
| `pollution:marble_dragonstone_ore` | marble_dragonstone_ore | marble_dragonstone_ore | 世界生成矿石（可挖掘获得） |
| `pollution:marble_dumb_tin_ore` | marble_dumb_tin_ore | marble_dumb_tin_ore | 世界生成矿石（可挖掘获得） |
| `pollution:marble_elven_elementium_ore` | marble_elven_elementium_ore | marble_elven_elementium_ore | 世界生成矿石（可挖掘获得） |
| `pollution:marble_elven_quartz_ore` | marble_elven_quartz_ore | marble_elven_quartz_ore | 世界生成矿石（可挖掘获得） |
| `pollution:marble_flame_coal_ore` | marble_flame_coal_ore | marble_flame_coal_ore | 世界生成矿石（可挖掘获得） |
| `pollution:marble_infused_air_ore` | marble_infused_air_ore | marble_infused_air_ore | 世界生成矿石（可挖掘获得） |
| `pollution:marble_infused_alchemy_ore` | marble_infused_alchemy_ore | marble_infused_alchemy_ore | 世界生成矿石（可挖掘获得） |
| `pollution:marble_infused_alien_ore` | marble_infused_alien_ore | marble_infused_alien_ore | 世界生成矿石（可挖掘获得） |
| `pollution:marble_infused_animal_ore` | marble_infused_animal_ore | marble_infused_animal_ore | 世界生成矿石（可挖掘获得） |
| `pollution:marble_infused_armor_ore` | marble_infused_armor_ore | marble_infused_armor_ore | 世界生成矿石（可挖掘获得） |
| `pollution:marble_infused_aura_ore` | marble_infused_aura_ore | marble_infused_aura_ore | 世界生成矿石（可挖掘获得） |
| `pollution:marble_infused_cold_ore` | marble_infused_cold_ore | marble_infused_cold_ore | 世界生成矿石（可挖掘获得） |
| `pollution:marble_infused_craft_ore` | marble_infused_craft_ore | marble_infused_craft_ore | 世界生成矿石（可挖掘获得） |
| `pollution:marble_infused_crystal_ore` | marble_infused_crystal_ore | marble_infused_crystal_ore | 世界生成矿石（可挖掘获得） |
| `pollution:marble_infused_dark_ore` | marble_infused_dark_ore | marble_infused_dark_ore | 世界生成矿石（可挖掘获得） |
| `pollution:marble_infused_death_ore` | marble_infused_death_ore | marble_infused_death_ore | 世界生成矿石（可挖掘获得） |
| `pollution:marble_infused_earth_ore` | marble_infused_earth_ore | marble_infused_earth_ore | 世界生成矿石（可挖掘获得） |
| `pollution:marble_infused_energy_ore` | marble_infused_energy_ore | marble_infused_energy_ore | 世界生成矿石（可挖掘获得） |
| `pollution:marble_infused_entropy_ore` | marble_infused_entropy_ore | marble_infused_entropy_ore | 世界生成矿石（可挖掘获得） |
| `pollution:marble_infused_exchange_ore` | marble_infused_exchange_ore | marble_infused_exchange_ore | 世界生成矿石（可挖掘获得） |
| `pollution:marble_infused_fire_ore` | marble_infused_fire_ore | marble_infused_fire_ore | 世界生成矿石（可挖掘获得） |
| `pollution:marble_infused_fly_ore` | marble_infused_fly_ore | marble_infused_fly_ore | 世界生成矿石（可挖掘获得） |
| `pollution:marble_infused_greed_ore` | marble_infused_greed_ore | marble_infused_greed_ore | 世界生成矿石（可挖掘获得） |
| `pollution:marble_infused_human_ore` | marble_infused_human_ore | marble_infused_human_ore | 世界生成矿石（可挖掘获得） |
| `pollution:marble_infused_instrument_ore` | marble_infused_instrument_ore | marble_infused_instrument_ore | 世界生成矿石（可挖掘获得） |
| `pollution:marble_infused_life_ore` | marble_infused_life_ore | marble_infused_life_ore | 世界生成矿石（可挖掘获得） |
| `pollution:marble_infused_light_ore` | marble_infused_light_ore | marble_infused_light_ore | 世界生成矿石（可挖掘获得） |
| `pollution:marble_infused_magic_ore` | marble_infused_magic_ore | marble_infused_magic_ore | 世界生成矿石（可挖掘获得） |
| `pollution:marble_infused_mechanics_ore` | marble_infused_mechanics_ore | marble_infused_mechanics_ore | 世界生成矿石（可挖掘获得） |
| `pollution:marble_infused_metal_ore` | marble_infused_metal_ore | marble_infused_metal_ore | 世界生成矿石（可挖掘获得） |
| `pollution:marble_infused_motion_ore` | marble_infused_motion_ore | marble_infused_motion_ore | 世界生成矿石（可挖掘获得） |
| `pollution:marble_infused_order_ore` | marble_infused_order_ore | marble_infused_order_ore | 世界生成矿石（可挖掘获得） |
| `pollution:marble_infused_plant_ore` | marble_infused_plant_ore | marble_infused_plant_ore | 世界生成矿石（可挖掘获得） |
| `pollution:marble_infused_sense_ore` | marble_infused_sense_ore | marble_infused_sense_ore | 世界生成矿石（可挖掘获得） |
| `pollution:marble_infused_soul_ore` | marble_infused_soul_ore | marble_infused_soul_ore | 世界生成矿石（可挖掘获得） |
| `pollution:marble_infused_spatio_ore` | marble_infused_spatio_ore | marble_infused_spatio_ore | 世界生成矿石（可挖掘获得） |
| `pollution:marble_infused_taint_ore` | marble_infused_taint_ore | marble_infused_taint_ore | 世界生成矿石（可挖掘获得） |
| `pollution:marble_infused_tempus_ore` | marble_infused_tempus_ore | marble_infused_tempus_ore | 世界生成矿石（可挖掘获得） |
| `pollution:marble_infused_thought_ore` | marble_infused_thought_ore | marble_infused_thought_ore | 世界生成矿石（可挖掘获得） |
| `pollution:marble_infused_tinctura_ore` | marble_infused_tinctura_ore | marble_infused_tinctura_ore | 世界生成矿石（可挖掘获得） |
| `pollution:marble_infused_trap_ore` | marble_infused_trap_ore | marble_infused_trap_ore | 世界生成矿石（可挖掘获得） |
| `pollution:marble_infused_undead_ore` | marble_infused_undead_ore | marble_infused_undead_ore | 世界生成矿石（可挖掘获得） |
| `pollution:marble_infused_void_ore` | marble_infused_void_ore | marble_infused_void_ore | 世界生成矿石（可挖掘获得） |
| `pollution:marble_infused_water_ore` | marble_infused_water_ore | marble_infused_water_ore | 世界生成矿石（可挖掘获得） |
| `pollution:marble_infused_weapon_ore` | marble_infused_weapon_ore | marble_infused_weapon_ore | 世界生成矿石（可挖掘获得） |
| `pollution:marble_melt_gold_ore` | marble_melt_gold_ore | marble_melt_gold_ore | 世界生成矿石（可挖掘获得） |
| `pollution:marble_pixie_dust_ore` | marble_pixie_dust_ore | marble_pixie_dust_ore | 世界生成矿石（可挖掘获得） |
| `pollution:marble_pluto_zinc_ore` | marble_pluto_zinc_ore | marble_pluto_zinc_ore | 世界生成矿石（可挖掘获得） |
| `pollution:marble_pyrargyrite_ore` | marble_pyrargyrite_ore | marble_pyrargyrite_ore | 世界生成矿石（可挖掘获得） |
| `pollution:marble_scabyst_ore` | marble_scabyst_ore | marble_scabyst_ore | 世界生成矿石（可挖掘获得） |
| `pollution:mars_amber_ore` | mars_amber_ore | mars_amber_ore | 世界生成矿石（可挖掘获得） |
| `pollution:mars_authority_lead_ore` | mars_authority_lead_ore | mars_authority_lead_ore | 世界生成矿石（可挖掘获得） |
| `pollution:mars_dragonstone_ore` | mars_dragonstone_ore | mars_dragonstone_ore | 世界生成矿石（可挖掘获得） |
| `pollution:mars_dumb_tin_ore` | mars_dumb_tin_ore | mars_dumb_tin_ore | 世界生成矿石（可挖掘获得） |
| `pollution:mars_elven_elementium_ore` | mars_elven_elementium_ore | mars_elven_elementium_ore | 世界生成矿石（可挖掘获得） |
| `pollution:mars_elven_quartz_ore` | mars_elven_quartz_ore | mars_elven_quartz_ore | 世界生成矿石（可挖掘获得） |
| `pollution:mars_flame_coal_ore` | mars_flame_coal_ore | mars_flame_coal_ore | 世界生成矿石（可挖掘获得） |
| `pollution:mars_infused_air_ore` | mars_infused_air_ore | mars_infused_air_ore | 世界生成矿石（可挖掘获得） |
| `pollution:mars_infused_alchemy_ore` | mars_infused_alchemy_ore | mars_infused_alchemy_ore | 世界生成矿石（可挖掘获得） |
| `pollution:mars_infused_alien_ore` | mars_infused_alien_ore | mars_infused_alien_ore | 世界生成矿石（可挖掘获得） |
| `pollution:mars_infused_animal_ore` | mars_infused_animal_ore | mars_infused_animal_ore | 世界生成矿石（可挖掘获得） |
| `pollution:mars_infused_armor_ore` | mars_infused_armor_ore | mars_infused_armor_ore | 世界生成矿石（可挖掘获得） |
| `pollution:mars_infused_aura_ore` | mars_infused_aura_ore | mars_infused_aura_ore | 世界生成矿石（可挖掘获得） |
| `pollution:mars_infused_cold_ore` | mars_infused_cold_ore | mars_infused_cold_ore | 世界生成矿石（可挖掘获得） |
| `pollution:mars_infused_craft_ore` | mars_infused_craft_ore | mars_infused_craft_ore | 世界生成矿石（可挖掘获得） |
| `pollution:mars_infused_crystal_ore` | mars_infused_crystal_ore | mars_infused_crystal_ore | 世界生成矿石（可挖掘获得） |
| `pollution:mars_infused_dark_ore` | mars_infused_dark_ore | mars_infused_dark_ore | 世界生成矿石（可挖掘获得） |
| `pollution:mars_infused_death_ore` | mars_infused_death_ore | mars_infused_death_ore | 世界生成矿石（可挖掘获得） |
| `pollution:mars_infused_earth_ore` | mars_infused_earth_ore | mars_infused_earth_ore | 世界生成矿石（可挖掘获得） |
| `pollution:mars_infused_energy_ore` | mars_infused_energy_ore | mars_infused_energy_ore | 世界生成矿石（可挖掘获得） |
| `pollution:mars_infused_entropy_ore` | mars_infused_entropy_ore | mars_infused_entropy_ore | 世界生成矿石（可挖掘获得） |
| `pollution:mars_infused_exchange_ore` | mars_infused_exchange_ore | mars_infused_exchange_ore | 世界生成矿石（可挖掘获得） |
| `pollution:mars_infused_fire_ore` | mars_infused_fire_ore | mars_infused_fire_ore | 世界生成矿石（可挖掘获得） |
| `pollution:mars_infused_fly_ore` | mars_infused_fly_ore | mars_infused_fly_ore | 世界生成矿石（可挖掘获得） |
| `pollution:mars_infused_greed_ore` | mars_infused_greed_ore | mars_infused_greed_ore | 世界生成矿石（可挖掘获得） |
| `pollution:mars_infused_human_ore` | mars_infused_human_ore | mars_infused_human_ore | 世界生成矿石（可挖掘获得） |
| `pollution:mars_infused_instrument_ore` | mars_infused_instrument_ore | mars_infused_instrument_ore | 世界生成矿石（可挖掘获得） |
| `pollution:mars_infused_life_ore` | mars_infused_life_ore | mars_infused_life_ore | 世界生成矿石（可挖掘获得） |
| `pollution:mars_infused_light_ore` | mars_infused_light_ore | mars_infused_light_ore | 世界生成矿石（可挖掘获得） |
| `pollution:mars_infused_magic_ore` | mars_infused_magic_ore | mars_infused_magic_ore | 世界生成矿石（可挖掘获得） |
| `pollution:mars_infused_mechanics_ore` | mars_infused_mechanics_ore | mars_infused_mechanics_ore | 世界生成矿石（可挖掘获得） |
| `pollution:mars_infused_metal_ore` | mars_infused_metal_ore | mars_infused_metal_ore | 世界生成矿石（可挖掘获得） |
| `pollution:mars_infused_motion_ore` | mars_infused_motion_ore | mars_infused_motion_ore | 世界生成矿石（可挖掘获得） |
| `pollution:mars_infused_order_ore` | mars_infused_order_ore | mars_infused_order_ore | 世界生成矿石（可挖掘获得） |
| `pollution:mars_infused_plant_ore` | mars_infused_plant_ore | mars_infused_plant_ore | 世界生成矿石（可挖掘获得） |
| `pollution:mars_infused_sense_ore` | mars_infused_sense_ore | mars_infused_sense_ore | 世界生成矿石（可挖掘获得） |
| `pollution:mars_infused_soul_ore` | mars_infused_soul_ore | mars_infused_soul_ore | 世界生成矿石（可挖掘获得） |
| `pollution:mars_infused_spatio_ore` | mars_infused_spatio_ore | mars_infused_spatio_ore | 世界生成矿石（可挖掘获得） |
| `pollution:mars_infused_taint_ore` | mars_infused_taint_ore | mars_infused_taint_ore | 世界生成矿石（可挖掘获得） |
| `pollution:mars_infused_tempus_ore` | mars_infused_tempus_ore | mars_infused_tempus_ore | 世界生成矿石（可挖掘获得） |
| `pollution:mars_infused_thought_ore` | mars_infused_thought_ore | mars_infused_thought_ore | 世界生成矿石（可挖掘获得） |
| `pollution:mars_infused_tinctura_ore` | mars_infused_tinctura_ore | mars_infused_tinctura_ore | 世界生成矿石（可挖掘获得） |
| `pollution:mars_infused_trap_ore` | mars_infused_trap_ore | mars_infused_trap_ore | 世界生成矿石（可挖掘获得） |
| `pollution:mars_infused_undead_ore` | mars_infused_undead_ore | mars_infused_undead_ore | 世界生成矿石（可挖掘获得） |
| `pollution:mars_infused_void_ore` | mars_infused_void_ore | mars_infused_void_ore | 世界生成矿石（可挖掘获得） |
| `pollution:mars_infused_water_ore` | mars_infused_water_ore | mars_infused_water_ore | 世界生成矿石（可挖掘获得） |
| `pollution:mars_infused_weapon_ore` | mars_infused_weapon_ore | mars_infused_weapon_ore | 世界生成矿石（可挖掘获得） |
| `pollution:mars_melt_gold_ore` | mars_melt_gold_ore | mars_melt_gold_ore | 世界生成矿石（可挖掘获得） |
| `pollution:mars_pixie_dust_ore` | mars_pixie_dust_ore | mars_pixie_dust_ore | 世界生成矿石（可挖掘获得） |
| `pollution:mars_pluto_zinc_ore` | mars_pluto_zinc_ore | mars_pluto_zinc_ore | 世界生成矿石（可挖掘获得） |
| `pollution:mars_pyrargyrite_ore` | mars_pyrargyrite_ore | mars_pyrargyrite_ore | 世界生成矿石（可挖掘获得） |
| `pollution:mars_scabyst_ore` | mars_scabyst_ore | mars_scabyst_ore | 世界生成矿石（可挖掘获得） |
| `pollution:melt_gold_ore` | Melt Gold Ore | 铄世金矿石 | 世界生成矿石（可挖掘获得） |
| `pollution:mercury_amber_ore` | mercury_amber_ore | mercury_amber_ore | 世界生成矿石（可挖掘获得） |
| `pollution:mercury_authority_lead_ore` | mercury_authority_lead_ore | mercury_authority_lead_ore | 世界生成矿石（可挖掘获得） |
| `pollution:mercury_dragonstone_ore` | mercury_dragonstone_ore | mercury_dragonstone_ore | 世界生成矿石（可挖掘获得） |
| `pollution:mercury_dumb_tin_ore` | mercury_dumb_tin_ore | mercury_dumb_tin_ore | 世界生成矿石（可挖掘获得） |
| `pollution:mercury_elven_elementium_ore` | mercury_elven_elementium_ore | mercury_elven_elementium_ore | 世界生成矿石（可挖掘获得） |
| `pollution:mercury_elven_quartz_ore` | mercury_elven_quartz_ore | mercury_elven_quartz_ore | 世界生成矿石（可挖掘获得） |
| `pollution:mercury_flame_coal_ore` | mercury_flame_coal_ore | mercury_flame_coal_ore | 世界生成矿石（可挖掘获得） |
| `pollution:mercury_infused_air_ore` | mercury_infused_air_ore | mercury_infused_air_ore | 世界生成矿石（可挖掘获得） |
| `pollution:mercury_infused_alchemy_ore` | mercury_infused_alchemy_ore | mercury_infused_alchemy_ore | 世界生成矿石（可挖掘获得） |
| `pollution:mercury_infused_alien_ore` | mercury_infused_alien_ore | mercury_infused_alien_ore | 世界生成矿石（可挖掘获得） |
| `pollution:mercury_infused_animal_ore` | mercury_infused_animal_ore | mercury_infused_animal_ore | 世界生成矿石（可挖掘获得） |
| `pollution:mercury_infused_armor_ore` | mercury_infused_armor_ore | mercury_infused_armor_ore | 世界生成矿石（可挖掘获得） |
| `pollution:mercury_infused_aura_ore` | mercury_infused_aura_ore | mercury_infused_aura_ore | 世界生成矿石（可挖掘获得） |
| `pollution:mercury_infused_cold_ore` | mercury_infused_cold_ore | mercury_infused_cold_ore | 世界生成矿石（可挖掘获得） |
| `pollution:mercury_infused_craft_ore` | mercury_infused_craft_ore | mercury_infused_craft_ore | 世界生成矿石（可挖掘获得） |
| `pollution:mercury_infused_crystal_ore` | mercury_infused_crystal_ore | mercury_infused_crystal_ore | 世界生成矿石（可挖掘获得） |
| `pollution:mercury_infused_dark_ore` | mercury_infused_dark_ore | mercury_infused_dark_ore | 世界生成矿石（可挖掘获得） |
| `pollution:mercury_infused_death_ore` | mercury_infused_death_ore | mercury_infused_death_ore | 世界生成矿石（可挖掘获得） |
| `pollution:mercury_infused_earth_ore` | mercury_infused_earth_ore | mercury_infused_earth_ore | 世界生成矿石（可挖掘获得） |
| `pollution:mercury_infused_energy_ore` | mercury_infused_energy_ore | mercury_infused_energy_ore | 世界生成矿石（可挖掘获得） |
| `pollution:mercury_infused_entropy_ore` | mercury_infused_entropy_ore | mercury_infused_entropy_ore | 世界生成矿石（可挖掘获得） |
| `pollution:mercury_infused_exchange_ore` | mercury_infused_exchange_ore | mercury_infused_exchange_ore | 世界生成矿石（可挖掘获得） |
| `pollution:mercury_infused_fire_ore` | mercury_infused_fire_ore | mercury_infused_fire_ore | 世界生成矿石（可挖掘获得） |
| `pollution:mercury_infused_fly_ore` | mercury_infused_fly_ore | mercury_infused_fly_ore | 世界生成矿石（可挖掘获得） |
| `pollution:mercury_infused_greed_ore` | mercury_infused_greed_ore | mercury_infused_greed_ore | 世界生成矿石（可挖掘获得） |
| `pollution:mercury_infused_human_ore` | mercury_infused_human_ore | mercury_infused_human_ore | 世界生成矿石（可挖掘获得） |
| `pollution:mercury_infused_instrument_ore` | mercury_infused_instrument_ore | mercury_infused_instrument_ore | 世界生成矿石（可挖掘获得） |
| `pollution:mercury_infused_life_ore` | mercury_infused_life_ore | mercury_infused_life_ore | 世界生成矿石（可挖掘获得） |
| `pollution:mercury_infused_light_ore` | mercury_infused_light_ore | mercury_infused_light_ore | 世界生成矿石（可挖掘获得） |
| `pollution:mercury_infused_magic_ore` | mercury_infused_magic_ore | mercury_infused_magic_ore | 世界生成矿石（可挖掘获得） |
| `pollution:mercury_infused_mechanics_ore` | mercury_infused_mechanics_ore | mercury_infused_mechanics_ore | 世界生成矿石（可挖掘获得） |
| `pollution:mercury_infused_metal_ore` | mercury_infused_metal_ore | mercury_infused_metal_ore | 世界生成矿石（可挖掘获得） |
| `pollution:mercury_infused_motion_ore` | mercury_infused_motion_ore | mercury_infused_motion_ore | 世界生成矿石（可挖掘获得） |
| `pollution:mercury_infused_order_ore` | mercury_infused_order_ore | mercury_infused_order_ore | 世界生成矿石（可挖掘获得） |
| `pollution:mercury_infused_plant_ore` | mercury_infused_plant_ore | mercury_infused_plant_ore | 世界生成矿石（可挖掘获得） |
| `pollution:mercury_infused_sense_ore` | mercury_infused_sense_ore | mercury_infused_sense_ore | 世界生成矿石（可挖掘获得） |
| `pollution:mercury_infused_soul_ore` | mercury_infused_soul_ore | mercury_infused_soul_ore | 世界生成矿石（可挖掘获得） |
| `pollution:mercury_infused_spatio_ore` | mercury_infused_spatio_ore | mercury_infused_spatio_ore | 世界生成矿石（可挖掘获得） |
| `pollution:mercury_infused_taint_ore` | mercury_infused_taint_ore | mercury_infused_taint_ore | 世界生成矿石（可挖掘获得） |
| `pollution:mercury_infused_tempus_ore` | mercury_infused_tempus_ore | mercury_infused_tempus_ore | 世界生成矿石（可挖掘获得） |
| `pollution:mercury_infused_thought_ore` | mercury_infused_thought_ore | mercury_infused_thought_ore | 世界生成矿石（可挖掘获得） |
| `pollution:mercury_infused_tinctura_ore` | mercury_infused_tinctura_ore | mercury_infused_tinctura_ore | 世界生成矿石（可挖掘获得） |
| `pollution:mercury_infused_trap_ore` | mercury_infused_trap_ore | mercury_infused_trap_ore | 世界生成矿石（可挖掘获得） |
| `pollution:mercury_infused_undead_ore` | mercury_infused_undead_ore | mercury_infused_undead_ore | 世界生成矿石（可挖掘获得） |
| `pollution:mercury_infused_void_ore` | mercury_infused_void_ore | mercury_infused_void_ore | 世界生成矿石（可挖掘获得） |
| `pollution:mercury_infused_water_ore` | mercury_infused_water_ore | mercury_infused_water_ore | 世界生成矿石（可挖掘获得） |
| `pollution:mercury_infused_weapon_ore` | mercury_infused_weapon_ore | mercury_infused_weapon_ore | 世界生成矿石（可挖掘获得） |
| `pollution:mercury_melt_gold_ore` | mercury_melt_gold_ore | mercury_melt_gold_ore | 世界生成矿石（可挖掘获得） |
| `pollution:mercury_pixie_dust_ore` | mercury_pixie_dust_ore | mercury_pixie_dust_ore | 世界生成矿石（可挖掘获得） |
| `pollution:mercury_pluto_zinc_ore` | mercury_pluto_zinc_ore | mercury_pluto_zinc_ore | 世界生成矿石（可挖掘获得） |
| `pollution:mercury_pyrargyrite_ore` | mercury_pyrargyrite_ore | mercury_pyrargyrite_ore | 世界生成矿石（可挖掘获得） |
| `pollution:mercury_scabyst_ore` | mercury_scabyst_ore | mercury_scabyst_ore | 世界生成矿石（可挖掘获得） |
| `pollution:moon_amber_ore` | moon_amber_ore | moon_amber_ore | 世界生成矿石（可挖掘获得） |
| `pollution:moon_authority_lead_ore` | moon_authority_lead_ore | moon_authority_lead_ore | 世界生成矿石（可挖掘获得） |
| `pollution:moon_dragonstone_ore` | moon_dragonstone_ore | moon_dragonstone_ore | 世界生成矿石（可挖掘获得） |
| `pollution:moon_dumb_tin_ore` | moon_dumb_tin_ore | moon_dumb_tin_ore | 世界生成矿石（可挖掘获得） |
| `pollution:moon_elven_elementium_ore` | moon_elven_elementium_ore | moon_elven_elementium_ore | 世界生成矿石（可挖掘获得） |
| `pollution:moon_elven_quartz_ore` | moon_elven_quartz_ore | moon_elven_quartz_ore | 世界生成矿石（可挖掘获得） |
| `pollution:moon_flame_coal_ore` | moon_flame_coal_ore | moon_flame_coal_ore | 世界生成矿石（可挖掘获得） |
| `pollution:moon_infused_air_ore` | moon_infused_air_ore | moon_infused_air_ore | 世界生成矿石（可挖掘获得） |
| `pollution:moon_infused_alchemy_ore` | moon_infused_alchemy_ore | moon_infused_alchemy_ore | 世界生成矿石（可挖掘获得） |
| `pollution:moon_infused_alien_ore` | moon_infused_alien_ore | moon_infused_alien_ore | 世界生成矿石（可挖掘获得） |
| `pollution:moon_infused_animal_ore` | moon_infused_animal_ore | moon_infused_animal_ore | 世界生成矿石（可挖掘获得） |
| `pollution:moon_infused_armor_ore` | moon_infused_armor_ore | moon_infused_armor_ore | 世界生成矿石（可挖掘获得） |
| `pollution:moon_infused_aura_ore` | moon_infused_aura_ore | moon_infused_aura_ore | 世界生成矿石（可挖掘获得） |
| `pollution:moon_infused_cold_ore` | moon_infused_cold_ore | moon_infused_cold_ore | 世界生成矿石（可挖掘获得） |
| `pollution:moon_infused_craft_ore` | moon_infused_craft_ore | moon_infused_craft_ore | 世界生成矿石（可挖掘获得） |
| `pollution:moon_infused_crystal_ore` | moon_infused_crystal_ore | moon_infused_crystal_ore | 世界生成矿石（可挖掘获得） |
| `pollution:moon_infused_dark_ore` | moon_infused_dark_ore | moon_infused_dark_ore | 世界生成矿石（可挖掘获得） |
| `pollution:moon_infused_death_ore` | moon_infused_death_ore | moon_infused_death_ore | 世界生成矿石（可挖掘获得） |
| `pollution:moon_infused_earth_ore` | moon_infused_earth_ore | moon_infused_earth_ore | 世界生成矿石（可挖掘获得） |
| `pollution:moon_infused_energy_ore` | moon_infused_energy_ore | moon_infused_energy_ore | 世界生成矿石（可挖掘获得） |
| `pollution:moon_infused_entropy_ore` | moon_infused_entropy_ore | moon_infused_entropy_ore | 世界生成矿石（可挖掘获得） |
| `pollution:moon_infused_exchange_ore` | moon_infused_exchange_ore | moon_infused_exchange_ore | 世界生成矿石（可挖掘获得） |
| `pollution:moon_infused_fire_ore` | moon_infused_fire_ore | moon_infused_fire_ore | 世界生成矿石（可挖掘获得） |
| `pollution:moon_infused_fly_ore` | moon_infused_fly_ore | moon_infused_fly_ore | 世界生成矿石（可挖掘获得） |
| `pollution:moon_infused_greed_ore` | moon_infused_greed_ore | moon_infused_greed_ore | 世界生成矿石（可挖掘获得） |
| `pollution:moon_infused_human_ore` | moon_infused_human_ore | moon_infused_human_ore | 世界生成矿石（可挖掘获得） |
| `pollution:moon_infused_instrument_ore` | moon_infused_instrument_ore | moon_infused_instrument_ore | 世界生成矿石（可挖掘获得） |
| `pollution:moon_infused_life_ore` | moon_infused_life_ore | moon_infused_life_ore | 世界生成矿石（可挖掘获得） |
| `pollution:moon_infused_light_ore` | moon_infused_light_ore | moon_infused_light_ore | 世界生成矿石（可挖掘获得） |
| `pollution:moon_infused_magic_ore` | moon_infused_magic_ore | moon_infused_magic_ore | 世界生成矿石（可挖掘获得） |
| `pollution:moon_infused_mechanics_ore` | moon_infused_mechanics_ore | moon_infused_mechanics_ore | 世界生成矿石（可挖掘获得） |
| `pollution:moon_infused_metal_ore` | moon_infused_metal_ore | moon_infused_metal_ore | 世界生成矿石（可挖掘获得） |
| `pollution:moon_infused_motion_ore` | moon_infused_motion_ore | moon_infused_motion_ore | 世界生成矿石（可挖掘获得） |
| `pollution:moon_infused_order_ore` | moon_infused_order_ore | moon_infused_order_ore | 世界生成矿石（可挖掘获得） |
| `pollution:moon_infused_plant_ore` | moon_infused_plant_ore | moon_infused_plant_ore | 世界生成矿石（可挖掘获得） |
| `pollution:moon_infused_sense_ore` | moon_infused_sense_ore | moon_infused_sense_ore | 世界生成矿石（可挖掘获得） |
| `pollution:moon_infused_soul_ore` | moon_infused_soul_ore | moon_infused_soul_ore | 世界生成矿石（可挖掘获得） |
| `pollution:moon_infused_spatio_ore` | moon_infused_spatio_ore | moon_infused_spatio_ore | 世界生成矿石（可挖掘获得） |
| `pollution:moon_infused_taint_ore` | moon_infused_taint_ore | moon_infused_taint_ore | 世界生成矿石（可挖掘获得） |
| `pollution:moon_infused_tempus_ore` | moon_infused_tempus_ore | moon_infused_tempus_ore | 世界生成矿石（可挖掘获得） |
| `pollution:moon_infused_thought_ore` | moon_infused_thought_ore | moon_infused_thought_ore | 世界生成矿石（可挖掘获得） |
| `pollution:moon_infused_tinctura_ore` | moon_infused_tinctura_ore | moon_infused_tinctura_ore | 世界生成矿石（可挖掘获得） |
| `pollution:moon_infused_trap_ore` | moon_infused_trap_ore | moon_infused_trap_ore | 世界生成矿石（可挖掘获得） |
| `pollution:moon_infused_undead_ore` | moon_infused_undead_ore | moon_infused_undead_ore | 世界生成矿石（可挖掘获得） |
| `pollution:moon_infused_void_ore` | moon_infused_void_ore | moon_infused_void_ore | 世界生成矿石（可挖掘获得） |
| `pollution:moon_infused_water_ore` | moon_infused_water_ore | moon_infused_water_ore | 世界生成矿石（可挖掘获得） |
| `pollution:moon_infused_weapon_ore` | moon_infused_weapon_ore | moon_infused_weapon_ore | 世界生成矿石（可挖掘获得） |
| `pollution:moon_melt_gold_ore` | moon_melt_gold_ore | moon_melt_gold_ore | 世界生成矿石（可挖掘获得） |
| `pollution:moon_pixie_dust_ore` | moon_pixie_dust_ore | moon_pixie_dust_ore | 世界生成矿石（可挖掘获得） |
| `pollution:moon_pluto_zinc_ore` | moon_pluto_zinc_ore | moon_pluto_zinc_ore | 世界生成矿石（可挖掘获得） |
| `pollution:moon_pyrargyrite_ore` | moon_pyrargyrite_ore | moon_pyrargyrite_ore | 世界生成矿石（可挖掘获得） |
| `pollution:moon_scabyst_ore` | moon_scabyst_ore | moon_scabyst_ore | 世界生成矿石（可挖掘获得） |
| `pollution:netherrack_amber_ore` | netherrack_amber_ore | netherrack_amber_ore | 世界生成矿石（可挖掘获得） |
| `pollution:netherrack_authority_lead_ore` | netherrack_authority_lead_ore | netherrack_authority_lead_ore | 世界生成矿石（可挖掘获得） |
| `pollution:netherrack_dragonstone_ore` | netherrack_dragonstone_ore | netherrack_dragonstone_ore | 世界生成矿石（可挖掘获得） |
| `pollution:netherrack_dumb_tin_ore` | netherrack_dumb_tin_ore | netherrack_dumb_tin_ore | 世界生成矿石（可挖掘获得） |
| `pollution:netherrack_elven_elementium_ore` | netherrack_elven_elementium_ore | netherrack_elven_elementium_ore | 世界生成矿石（可挖掘获得） |
| `pollution:netherrack_elven_quartz_ore` | netherrack_elven_quartz_ore | netherrack_elven_quartz_ore | 世界生成矿石（可挖掘获得） |
| `pollution:netherrack_flame_coal_ore` | netherrack_flame_coal_ore | netherrack_flame_coal_ore | 世界生成矿石（可挖掘获得） |
| `pollution:netherrack_infused_air_ore` | netherrack_infused_air_ore | netherrack_infused_air_ore | 世界生成矿石（可挖掘获得） |
| `pollution:netherrack_infused_alchemy_ore` | netherrack_infused_alchemy_ore | netherrack_infused_alchemy_ore | 世界生成矿石（可挖掘获得） |
| `pollution:netherrack_infused_alien_ore` | netherrack_infused_alien_ore | netherrack_infused_alien_ore | 世界生成矿石（可挖掘获得） |
| `pollution:netherrack_infused_animal_ore` | netherrack_infused_animal_ore | netherrack_infused_animal_ore | 世界生成矿石（可挖掘获得） |
| `pollution:netherrack_infused_armor_ore` | netherrack_infused_armor_ore | netherrack_infused_armor_ore | 世界生成矿石（可挖掘获得） |
| `pollution:netherrack_infused_aura_ore` | netherrack_infused_aura_ore | netherrack_infused_aura_ore | 世界生成矿石（可挖掘获得） |
| `pollution:netherrack_infused_cold_ore` | netherrack_infused_cold_ore | netherrack_infused_cold_ore | 世界生成矿石（可挖掘获得） |
| `pollution:netherrack_infused_craft_ore` | netherrack_infused_craft_ore | netherrack_infused_craft_ore | 世界生成矿石（可挖掘获得） |
| `pollution:netherrack_infused_crystal_ore` | netherrack_infused_crystal_ore | netherrack_infused_crystal_ore | 世界生成矿石（可挖掘获得） |
| `pollution:netherrack_infused_dark_ore` | netherrack_infused_dark_ore | netherrack_infused_dark_ore | 世界生成矿石（可挖掘获得） |
| `pollution:netherrack_infused_death_ore` | netherrack_infused_death_ore | netherrack_infused_death_ore | 世界生成矿石（可挖掘获得） |
| `pollution:netherrack_infused_earth_ore` | netherrack_infused_earth_ore | netherrack_infused_earth_ore | 世界生成矿石（可挖掘获得） |
| `pollution:netherrack_infused_energy_ore` | netherrack_infused_energy_ore | netherrack_infused_energy_ore | 世界生成矿石（可挖掘获得） |
| `pollution:netherrack_infused_entropy_ore` | netherrack_infused_entropy_ore | netherrack_infused_entropy_ore | 世界生成矿石（可挖掘获得） |
| `pollution:netherrack_infused_exchange_ore` | netherrack_infused_exchange_ore | netherrack_infused_exchange_ore | 世界生成矿石（可挖掘获得） |
| `pollution:netherrack_infused_fire_ore` | netherrack_infused_fire_ore | netherrack_infused_fire_ore | 世界生成矿石（可挖掘获得） |
| `pollution:netherrack_infused_fly_ore` | netherrack_infused_fly_ore | netherrack_infused_fly_ore | 世界生成矿石（可挖掘获得） |
| `pollution:netherrack_infused_greed_ore` | netherrack_infused_greed_ore | netherrack_infused_greed_ore | 世界生成矿石（可挖掘获得） |
| `pollution:netherrack_infused_human_ore` | netherrack_infused_human_ore | netherrack_infused_human_ore | 世界生成矿石（可挖掘获得） |
| `pollution:netherrack_infused_instrument_ore` | netherrack_infused_instrument_ore | netherrack_infused_instrument_ore | 世界生成矿石（可挖掘获得） |
| `pollution:netherrack_infused_life_ore` | netherrack_infused_life_ore | netherrack_infused_life_ore | 世界生成矿石（可挖掘获得） |
| `pollution:netherrack_infused_light_ore` | netherrack_infused_light_ore | netherrack_infused_light_ore | 世界生成矿石（可挖掘获得） |
| `pollution:netherrack_infused_magic_ore` | netherrack_infused_magic_ore | netherrack_infused_magic_ore | 世界生成矿石（可挖掘获得） |
| `pollution:netherrack_infused_mechanics_ore` | netherrack_infused_mechanics_ore | netherrack_infused_mechanics_ore | 世界生成矿石（可挖掘获得） |
| `pollution:netherrack_infused_metal_ore` | netherrack_infused_metal_ore | netherrack_infused_metal_ore | 世界生成矿石（可挖掘获得） |
| `pollution:netherrack_infused_motion_ore` | netherrack_infused_motion_ore | netherrack_infused_motion_ore | 世界生成矿石（可挖掘获得） |
| `pollution:netherrack_infused_order_ore` | netherrack_infused_order_ore | netherrack_infused_order_ore | 世界生成矿石（可挖掘获得） |
| `pollution:netherrack_infused_plant_ore` | netherrack_infused_plant_ore | netherrack_infused_plant_ore | 世界生成矿石（可挖掘获得） |
| `pollution:netherrack_infused_sense_ore` | netherrack_infused_sense_ore | netherrack_infused_sense_ore | 世界生成矿石（可挖掘获得） |
| `pollution:netherrack_infused_soul_ore` | netherrack_infused_soul_ore | netherrack_infused_soul_ore | 世界生成矿石（可挖掘获得） |
| `pollution:netherrack_infused_spatio_ore` | netherrack_infused_spatio_ore | netherrack_infused_spatio_ore | 世界生成矿石（可挖掘获得） |
| `pollution:netherrack_infused_taint_ore` | netherrack_infused_taint_ore | netherrack_infused_taint_ore | 世界生成矿石（可挖掘获得） |
| `pollution:netherrack_infused_tempus_ore` | netherrack_infused_tempus_ore | netherrack_infused_tempus_ore | 世界生成矿石（可挖掘获得） |
| `pollution:netherrack_infused_thought_ore` | netherrack_infused_thought_ore | netherrack_infused_thought_ore | 世界生成矿石（可挖掘获得） |
| `pollution:netherrack_infused_tinctura_ore` | netherrack_infused_tinctura_ore | netherrack_infused_tinctura_ore | 世界生成矿石（可挖掘获得） |
| `pollution:netherrack_infused_trap_ore` | netherrack_infused_trap_ore | netherrack_infused_trap_ore | 世界生成矿石（可挖掘获得） |
| `pollution:netherrack_infused_undead_ore` | netherrack_infused_undead_ore | netherrack_infused_undead_ore | 世界生成矿石（可挖掘获得） |
| `pollution:netherrack_infused_void_ore` | netherrack_infused_void_ore | netherrack_infused_void_ore | 世界生成矿石（可挖掘获得） |
| `pollution:netherrack_infused_water_ore` | netherrack_infused_water_ore | netherrack_infused_water_ore | 世界生成矿石（可挖掘获得） |
| `pollution:netherrack_infused_weapon_ore` | netherrack_infused_weapon_ore | netherrack_infused_weapon_ore | 世界生成矿石（可挖掘获得） |
| `pollution:netherrack_melt_gold_ore` | netherrack_melt_gold_ore | netherrack_melt_gold_ore | 世界生成矿石（可挖掘获得） |
| `pollution:netherrack_pixie_dust_ore` | netherrack_pixie_dust_ore | netherrack_pixie_dust_ore | 世界生成矿石（可挖掘获得） |
| `pollution:netherrack_pluto_zinc_ore` | netherrack_pluto_zinc_ore | netherrack_pluto_zinc_ore | 世界生成矿石（可挖掘获得） |
| `pollution:netherrack_pyrargyrite_ore` | netherrack_pyrargyrite_ore | netherrack_pyrargyrite_ore | 世界生成矿石（可挖掘获得） |
| `pollution:netherrack_scabyst_ore` | netherrack_scabyst_ore | netherrack_scabyst_ore | 世界生成矿石（可挖掘获得） |
| `pollution:pixie_dust_ore` | Pixie Dust Ore | 精灵尘矿石 | 世界生成矿石（可挖掘获得） |
| `pollution:pluto_zinc_ore` | Pluto Zinc Ore | 冥晶锌矿石 | 世界生成矿石（可挖掘获得） |
| `pollution:proxima_centauri_b_amber_ore` | proxima_centauri_b_amber_ore | proxima_centauri_b_amber_ore | 世界生成矿石（可挖掘获得） |
| `pollution:proxima_centauri_b_authority_lead_ore` | proxima_centauri_b_authority_lead_ore | proxima_centauri_b_authority_lead_ore | 世界生成矿石（可挖掘获得） |
| `pollution:proxima_centauri_b_dragonstone_ore` | proxima_centauri_b_dragonstone_ore | proxima_centauri_b_dragonstone_ore | 世界生成矿石（可挖掘获得） |
| `pollution:proxima_centauri_b_dumb_tin_ore` | proxima_centauri_b_dumb_tin_ore | proxima_centauri_b_dumb_tin_ore | 世界生成矿石（可挖掘获得） |
| `pollution:proxima_centauri_b_elven_elementium_ore` | proxima_centauri_b_elven_elementium_ore | proxima_centauri_b_elven_elementium_ore | 世界生成矿石（可挖掘获得） |
| `pollution:proxima_centauri_b_elven_quartz_ore` | proxima_centauri_b_elven_quartz_ore | proxima_centauri_b_elven_quartz_ore | 世界生成矿石（可挖掘获得） |
| `pollution:proxima_centauri_b_flame_coal_ore` | proxima_centauri_b_flame_coal_ore | proxima_centauri_b_flame_coal_ore | 世界生成矿石（可挖掘获得） |
| `pollution:proxima_centauri_b_infused_air_ore` | proxima_centauri_b_infused_air_ore | proxima_centauri_b_infused_air_ore | 世界生成矿石（可挖掘获得） |
| `pollution:proxima_centauri_b_infused_alchemy_ore` | proxima_centauri_b_infused_alchemy_ore | proxima_centauri_b_infused_alchemy_ore | 世界生成矿石（可挖掘获得） |
| `pollution:proxima_centauri_b_infused_alien_ore` | proxima_centauri_b_infused_alien_ore | proxima_centauri_b_infused_alien_ore | 世界生成矿石（可挖掘获得） |
| `pollution:proxima_centauri_b_infused_animal_ore` | proxima_centauri_b_infused_animal_ore | proxima_centauri_b_infused_animal_ore | 世界生成矿石（可挖掘获得） |
| `pollution:proxima_centauri_b_infused_armor_ore` | proxima_centauri_b_infused_armor_ore | proxima_centauri_b_infused_armor_ore | 世界生成矿石（可挖掘获得） |
| `pollution:proxima_centauri_b_infused_aura_ore` | proxima_centauri_b_infused_aura_ore | proxima_centauri_b_infused_aura_ore | 世界生成矿石（可挖掘获得） |
| `pollution:proxima_centauri_b_infused_cold_ore` | proxima_centauri_b_infused_cold_ore | proxima_centauri_b_infused_cold_ore | 世界生成矿石（可挖掘获得） |
| `pollution:proxima_centauri_b_infused_craft_ore` | proxima_centauri_b_infused_craft_ore | proxima_centauri_b_infused_craft_ore | 世界生成矿石（可挖掘获得） |
| `pollution:proxima_centauri_b_infused_crystal_ore` | proxima_centauri_b_infused_crystal_ore | proxima_centauri_b_infused_crystal_ore | 世界生成矿石（可挖掘获得） |
| `pollution:proxima_centauri_b_infused_dark_ore` | proxima_centauri_b_infused_dark_ore | proxima_centauri_b_infused_dark_ore | 世界生成矿石（可挖掘获得） |
| `pollution:proxima_centauri_b_infused_death_ore` | proxima_centauri_b_infused_death_ore | proxima_centauri_b_infused_death_ore | 世界生成矿石（可挖掘获得） |
| `pollution:proxima_centauri_b_infused_earth_ore` | proxima_centauri_b_infused_earth_ore | proxima_centauri_b_infused_earth_ore | 世界生成矿石（可挖掘获得） |
| `pollution:proxima_centauri_b_infused_energy_ore` | proxima_centauri_b_infused_energy_ore | proxima_centauri_b_infused_energy_ore | 世界生成矿石（可挖掘获得） |
| `pollution:proxima_centauri_b_infused_entropy_ore` | proxima_centauri_b_infused_entropy_ore | proxima_centauri_b_infused_entropy_ore | 世界生成矿石（可挖掘获得） |
| `pollution:proxima_centauri_b_infused_exchange_ore` | proxima_centauri_b_infused_exchange_ore | proxima_centauri_b_infused_exchange_ore | 世界生成矿石（可挖掘获得） |
| `pollution:proxima_centauri_b_infused_fire_ore` | proxima_centauri_b_infused_fire_ore | proxima_centauri_b_infused_fire_ore | 世界生成矿石（可挖掘获得） |
| `pollution:proxima_centauri_b_infused_fly_ore` | proxima_centauri_b_infused_fly_ore | proxima_centauri_b_infused_fly_ore | 世界生成矿石（可挖掘获得） |
| `pollution:proxima_centauri_b_infused_greed_ore` | proxima_centauri_b_infused_greed_ore | proxima_centauri_b_infused_greed_ore | 世界生成矿石（可挖掘获得） |
| `pollution:proxima_centauri_b_infused_human_ore` | proxima_centauri_b_infused_human_ore | proxima_centauri_b_infused_human_ore | 世界生成矿石（可挖掘获得） |
| `pollution:proxima_centauri_b_infused_instrument_ore` | proxima_centauri_b_infused_instrument_ore | proxima_centauri_b_infused_instrument_ore | 世界生成矿石（可挖掘获得） |
| `pollution:proxima_centauri_b_infused_life_ore` | proxima_centauri_b_infused_life_ore | proxima_centauri_b_infused_life_ore | 世界生成矿石（可挖掘获得） |
| `pollution:proxima_centauri_b_infused_light_ore` | proxima_centauri_b_infused_light_ore | proxima_centauri_b_infused_light_ore | 世界生成矿石（可挖掘获得） |
| `pollution:proxima_centauri_b_infused_magic_ore` | proxima_centauri_b_infused_magic_ore | proxima_centauri_b_infused_magic_ore | 世界生成矿石（可挖掘获得） |
| `pollution:proxima_centauri_b_infused_mechanics_ore` | proxima_centauri_b_infused_mechanics_ore | proxima_centauri_b_infused_mechanics_ore | 世界生成矿石（可挖掘获得） |
| `pollution:proxima_centauri_b_infused_metal_ore` | proxima_centauri_b_infused_metal_ore | proxima_centauri_b_infused_metal_ore | 世界生成矿石（可挖掘获得） |
| `pollution:proxima_centauri_b_infused_motion_ore` | proxima_centauri_b_infused_motion_ore | proxima_centauri_b_infused_motion_ore | 世界生成矿石（可挖掘获得） |
| `pollution:proxima_centauri_b_infused_order_ore` | proxima_centauri_b_infused_order_ore | proxima_centauri_b_infused_order_ore | 世界生成矿石（可挖掘获得） |
| `pollution:proxima_centauri_b_infused_plant_ore` | proxima_centauri_b_infused_plant_ore | proxima_centauri_b_infused_plant_ore | 世界生成矿石（可挖掘获得） |
| `pollution:proxima_centauri_b_infused_sense_ore` | proxima_centauri_b_infused_sense_ore | proxima_centauri_b_infused_sense_ore | 世界生成矿石（可挖掘获得） |
| `pollution:proxima_centauri_b_infused_soul_ore` | proxima_centauri_b_infused_soul_ore | proxima_centauri_b_infused_soul_ore | 世界生成矿石（可挖掘获得） |
| `pollution:proxima_centauri_b_infused_spatio_ore` | proxima_centauri_b_infused_spatio_ore | proxima_centauri_b_infused_spatio_ore | 世界生成矿石（可挖掘获得） |
| `pollution:proxima_centauri_b_infused_taint_ore` | proxima_centauri_b_infused_taint_ore | proxima_centauri_b_infused_taint_ore | 世界生成矿石（可挖掘获得） |
| `pollution:proxima_centauri_b_infused_tempus_ore` | proxima_centauri_b_infused_tempus_ore | proxima_centauri_b_infused_tempus_ore | 世界生成矿石（可挖掘获得） |
| `pollution:proxima_centauri_b_infused_thought_ore` | proxima_centauri_b_infused_thought_ore | proxima_centauri_b_infused_thought_ore | 世界生成矿石（可挖掘获得） |
| `pollution:proxima_centauri_b_infused_tinctura_ore` | proxima_centauri_b_infused_tinctura_ore | proxima_centauri_b_infused_tinctura_ore | 世界生成矿石（可挖掘获得） |
| `pollution:proxima_centauri_b_infused_trap_ore` | proxima_centauri_b_infused_trap_ore | proxima_centauri_b_infused_trap_ore | 世界生成矿石（可挖掘获得） |
| `pollution:proxima_centauri_b_infused_undead_ore` | proxima_centauri_b_infused_undead_ore | proxima_centauri_b_infused_undead_ore | 世界生成矿石（可挖掘获得） |
| `pollution:proxima_centauri_b_infused_void_ore` | proxima_centauri_b_infused_void_ore | proxima_centauri_b_infused_void_ore | 世界生成矿石（可挖掘获得） |
| `pollution:proxima_centauri_b_infused_water_ore` | proxima_centauri_b_infused_water_ore | proxima_centauri_b_infused_water_ore | 世界生成矿石（可挖掘获得） |
| `pollution:proxima_centauri_b_infused_weapon_ore` | proxima_centauri_b_infused_weapon_ore | proxima_centauri_b_infused_weapon_ore | 世界生成矿石（可挖掘获得） |
| `pollution:proxima_centauri_b_melt_gold_ore` | proxima_centauri_b_melt_gold_ore | proxima_centauri_b_melt_gold_ore | 世界生成矿石（可挖掘获得） |
| `pollution:proxima_centauri_b_pixie_dust_ore` | proxima_centauri_b_pixie_dust_ore | proxima_centauri_b_pixie_dust_ore | 世界生成矿石（可挖掘获得） |
| `pollution:proxima_centauri_b_pluto_zinc_ore` | proxima_centauri_b_pluto_zinc_ore | proxima_centauri_b_pluto_zinc_ore | 世界生成矿石（可挖掘获得） |
| `pollution:proxima_centauri_b_pyrargyrite_ore` | proxima_centauri_b_pyrargyrite_ore | proxima_centauri_b_pyrargyrite_ore | 世界生成矿石（可挖掘获得） |
| `pollution:proxima_centauri_b_scabyst_ore` | proxima_centauri_b_scabyst_ore | proxima_centauri_b_scabyst_ore | 世界生成矿石（可挖掘获得） |
| `pollution:pyrargyrite_ore` | Pyrargyrite Ore | 深红银矿石 | 世界生成矿石（可挖掘获得） |
| `pollution:red_granite_amber_ore` | red_granite_amber_ore | red_granite_amber_ore | 世界生成矿石（可挖掘获得） |
| `pollution:red_granite_authority_lead_ore` | red_granite_authority_lead_ore | red_granite_authority_lead_ore | 世界生成矿石（可挖掘获得） |
| `pollution:red_granite_dragonstone_ore` | red_granite_dragonstone_ore | red_granite_dragonstone_ore | 世界生成矿石（可挖掘获得） |
| `pollution:red_granite_dumb_tin_ore` | red_granite_dumb_tin_ore | red_granite_dumb_tin_ore | 世界生成矿石（可挖掘获得） |
| `pollution:red_granite_elven_elementium_ore` | red_granite_elven_elementium_ore | red_granite_elven_elementium_ore | 世界生成矿石（可挖掘获得） |
| `pollution:red_granite_elven_quartz_ore` | red_granite_elven_quartz_ore | red_granite_elven_quartz_ore | 世界生成矿石（可挖掘获得） |
| `pollution:red_granite_flame_coal_ore` | red_granite_flame_coal_ore | red_granite_flame_coal_ore | 世界生成矿石（可挖掘获得） |
| `pollution:red_granite_infused_air_ore` | red_granite_infused_air_ore | red_granite_infused_air_ore | 世界生成矿石（可挖掘获得） |
| `pollution:red_granite_infused_alchemy_ore` | red_granite_infused_alchemy_ore | red_granite_infused_alchemy_ore | 世界生成矿石（可挖掘获得） |
| `pollution:red_granite_infused_alien_ore` | red_granite_infused_alien_ore | red_granite_infused_alien_ore | 世界生成矿石（可挖掘获得） |
| `pollution:red_granite_infused_animal_ore` | red_granite_infused_animal_ore | red_granite_infused_animal_ore | 世界生成矿石（可挖掘获得） |
| `pollution:red_granite_infused_armor_ore` | red_granite_infused_armor_ore | red_granite_infused_armor_ore | 世界生成矿石（可挖掘获得） |
| `pollution:red_granite_infused_aura_ore` | red_granite_infused_aura_ore | red_granite_infused_aura_ore | 世界生成矿石（可挖掘获得） |
| `pollution:red_granite_infused_cold_ore` | red_granite_infused_cold_ore | red_granite_infused_cold_ore | 世界生成矿石（可挖掘获得） |
| `pollution:red_granite_infused_craft_ore` | red_granite_infused_craft_ore | red_granite_infused_craft_ore | 世界生成矿石（可挖掘获得） |
| `pollution:red_granite_infused_crystal_ore` | red_granite_infused_crystal_ore | red_granite_infused_crystal_ore | 世界生成矿石（可挖掘获得） |
| `pollution:red_granite_infused_dark_ore` | red_granite_infused_dark_ore | red_granite_infused_dark_ore | 世界生成矿石（可挖掘获得） |
| `pollution:red_granite_infused_death_ore` | red_granite_infused_death_ore | red_granite_infused_death_ore | 世界生成矿石（可挖掘获得） |
| `pollution:red_granite_infused_earth_ore` | red_granite_infused_earth_ore | red_granite_infused_earth_ore | 世界生成矿石（可挖掘获得） |
| `pollution:red_granite_infused_energy_ore` | red_granite_infused_energy_ore | red_granite_infused_energy_ore | 世界生成矿石（可挖掘获得） |
| `pollution:red_granite_infused_entropy_ore` | red_granite_infused_entropy_ore | red_granite_infused_entropy_ore | 世界生成矿石（可挖掘获得） |
| `pollution:red_granite_infused_exchange_ore` | red_granite_infused_exchange_ore | red_granite_infused_exchange_ore | 世界生成矿石（可挖掘获得） |
| `pollution:red_granite_infused_fire_ore` | red_granite_infused_fire_ore | red_granite_infused_fire_ore | 世界生成矿石（可挖掘获得） |
| `pollution:red_granite_infused_fly_ore` | red_granite_infused_fly_ore | red_granite_infused_fly_ore | 世界生成矿石（可挖掘获得） |
| `pollution:red_granite_infused_greed_ore` | red_granite_infused_greed_ore | red_granite_infused_greed_ore | 世界生成矿石（可挖掘获得） |
| `pollution:red_granite_infused_human_ore` | red_granite_infused_human_ore | red_granite_infused_human_ore | 世界生成矿石（可挖掘获得） |
| `pollution:red_granite_infused_instrument_ore` | red_granite_infused_instrument_ore | red_granite_infused_instrument_ore | 世界生成矿石（可挖掘获得） |
| `pollution:red_granite_infused_life_ore` | red_granite_infused_life_ore | red_granite_infused_life_ore | 世界生成矿石（可挖掘获得） |
| `pollution:red_granite_infused_light_ore` | red_granite_infused_light_ore | red_granite_infused_light_ore | 世界生成矿石（可挖掘获得） |
| `pollution:red_granite_infused_magic_ore` | red_granite_infused_magic_ore | red_granite_infused_magic_ore | 世界生成矿石（可挖掘获得） |
| `pollution:red_granite_infused_mechanics_ore` | red_granite_infused_mechanics_ore | red_granite_infused_mechanics_ore | 世界生成矿石（可挖掘获得） |
| `pollution:red_granite_infused_metal_ore` | red_granite_infused_metal_ore | red_granite_infused_metal_ore | 世界生成矿石（可挖掘获得） |
| `pollution:red_granite_infused_motion_ore` | red_granite_infused_motion_ore | red_granite_infused_motion_ore | 世界生成矿石（可挖掘获得） |
| `pollution:red_granite_infused_order_ore` | red_granite_infused_order_ore | red_granite_infused_order_ore | 世界生成矿石（可挖掘获得） |
| `pollution:red_granite_infused_plant_ore` | red_granite_infused_plant_ore | red_granite_infused_plant_ore | 世界生成矿石（可挖掘获得） |
| `pollution:red_granite_infused_sense_ore` | red_granite_infused_sense_ore | red_granite_infused_sense_ore | 世界生成矿石（可挖掘获得） |
| `pollution:red_granite_infused_soul_ore` | red_granite_infused_soul_ore | red_granite_infused_soul_ore | 世界生成矿石（可挖掘获得） |
| `pollution:red_granite_infused_spatio_ore` | red_granite_infused_spatio_ore | red_granite_infused_spatio_ore | 世界生成矿石（可挖掘获得） |
| `pollution:red_granite_infused_taint_ore` | red_granite_infused_taint_ore | red_granite_infused_taint_ore | 世界生成矿石（可挖掘获得） |
| `pollution:red_granite_infused_tempus_ore` | red_granite_infused_tempus_ore | red_granite_infused_tempus_ore | 世界生成矿石（可挖掘获得） |
| `pollution:red_granite_infused_thought_ore` | red_granite_infused_thought_ore | red_granite_infused_thought_ore | 世界生成矿石（可挖掘获得） |
| `pollution:red_granite_infused_tinctura_ore` | red_granite_infused_tinctura_ore | red_granite_infused_tinctura_ore | 世界生成矿石（可挖掘获得） |
| `pollution:red_granite_infused_trap_ore` | red_granite_infused_trap_ore | red_granite_infused_trap_ore | 世界生成矿石（可挖掘获得） |
| `pollution:red_granite_infused_undead_ore` | red_granite_infused_undead_ore | red_granite_infused_undead_ore | 世界生成矿石（可挖掘获得） |
| `pollution:red_granite_infused_void_ore` | red_granite_infused_void_ore | red_granite_infused_void_ore | 世界生成矿石（可挖掘获得） |
| `pollution:red_granite_infused_water_ore` | red_granite_infused_water_ore | red_granite_infused_water_ore | 世界生成矿石（可挖掘获得） |
| `pollution:red_granite_infused_weapon_ore` | red_granite_infused_weapon_ore | red_granite_infused_weapon_ore | 世界生成矿石（可挖掘获得） |
| `pollution:red_granite_melt_gold_ore` | red_granite_melt_gold_ore | red_granite_melt_gold_ore | 世界生成矿石（可挖掘获得） |
| `pollution:red_granite_pixie_dust_ore` | red_granite_pixie_dust_ore | red_granite_pixie_dust_ore | 世界生成矿石（可挖掘获得） |
| `pollution:red_granite_pluto_zinc_ore` | red_granite_pluto_zinc_ore | red_granite_pluto_zinc_ore | 世界生成矿石（可挖掘获得） |
| `pollution:red_granite_pyrargyrite_ore` | red_granite_pyrargyrite_ore | red_granite_pyrargyrite_ore | 世界生成矿石（可挖掘获得） |
| `pollution:red_granite_scabyst_ore` | red_granite_scabyst_ore | red_granite_scabyst_ore | 世界生成矿石（可挖掘获得） |
| `pollution:red_sand_amber_ore` | red_sand_amber_ore | red_sand_amber_ore | 世界生成矿石（可挖掘获得） |
| `pollution:red_sand_authority_lead_ore` | red_sand_authority_lead_ore | red_sand_authority_lead_ore | 世界生成矿石（可挖掘获得） |
| `pollution:red_sand_dragonstone_ore` | red_sand_dragonstone_ore | red_sand_dragonstone_ore | 世界生成矿石（可挖掘获得） |
| `pollution:red_sand_dumb_tin_ore` | red_sand_dumb_tin_ore | red_sand_dumb_tin_ore | 世界生成矿石（可挖掘获得） |
| `pollution:red_sand_elven_elementium_ore` | red_sand_elven_elementium_ore | red_sand_elven_elementium_ore | 世界生成矿石（可挖掘获得） |
| `pollution:red_sand_elven_quartz_ore` | red_sand_elven_quartz_ore | red_sand_elven_quartz_ore | 世界生成矿石（可挖掘获得） |
| `pollution:red_sand_flame_coal_ore` | red_sand_flame_coal_ore | red_sand_flame_coal_ore | 世界生成矿石（可挖掘获得） |
| `pollution:red_sand_infused_air_ore` | red_sand_infused_air_ore | red_sand_infused_air_ore | 世界生成矿石（可挖掘获得） |
| `pollution:red_sand_infused_alchemy_ore` | red_sand_infused_alchemy_ore | red_sand_infused_alchemy_ore | 世界生成矿石（可挖掘获得） |
| `pollution:red_sand_infused_alien_ore` | red_sand_infused_alien_ore | red_sand_infused_alien_ore | 世界生成矿石（可挖掘获得） |
| `pollution:red_sand_infused_animal_ore` | red_sand_infused_animal_ore | red_sand_infused_animal_ore | 世界生成矿石（可挖掘获得） |
| `pollution:red_sand_infused_armor_ore` | red_sand_infused_armor_ore | red_sand_infused_armor_ore | 世界生成矿石（可挖掘获得） |
| `pollution:red_sand_infused_aura_ore` | red_sand_infused_aura_ore | red_sand_infused_aura_ore | 世界生成矿石（可挖掘获得） |
| `pollution:red_sand_infused_cold_ore` | red_sand_infused_cold_ore | red_sand_infused_cold_ore | 世界生成矿石（可挖掘获得） |
| `pollution:red_sand_infused_craft_ore` | red_sand_infused_craft_ore | red_sand_infused_craft_ore | 世界生成矿石（可挖掘获得） |
| `pollution:red_sand_infused_crystal_ore` | red_sand_infused_crystal_ore | red_sand_infused_crystal_ore | 世界生成矿石（可挖掘获得） |
| `pollution:red_sand_infused_dark_ore` | red_sand_infused_dark_ore | red_sand_infused_dark_ore | 世界生成矿石（可挖掘获得） |
| `pollution:red_sand_infused_death_ore` | red_sand_infused_death_ore | red_sand_infused_death_ore | 世界生成矿石（可挖掘获得） |
| `pollution:red_sand_infused_earth_ore` | red_sand_infused_earth_ore | red_sand_infused_earth_ore | 世界生成矿石（可挖掘获得） |
| `pollution:red_sand_infused_energy_ore` | red_sand_infused_energy_ore | red_sand_infused_energy_ore | 世界生成矿石（可挖掘获得） |
| `pollution:red_sand_infused_entropy_ore` | red_sand_infused_entropy_ore | red_sand_infused_entropy_ore | 世界生成矿石（可挖掘获得） |
| `pollution:red_sand_infused_exchange_ore` | red_sand_infused_exchange_ore | red_sand_infused_exchange_ore | 世界生成矿石（可挖掘获得） |
| `pollution:red_sand_infused_fire_ore` | red_sand_infused_fire_ore | red_sand_infused_fire_ore | 世界生成矿石（可挖掘获得） |
| `pollution:red_sand_infused_fly_ore` | red_sand_infused_fly_ore | red_sand_infused_fly_ore | 世界生成矿石（可挖掘获得） |
| `pollution:red_sand_infused_greed_ore` | red_sand_infused_greed_ore | red_sand_infused_greed_ore | 世界生成矿石（可挖掘获得） |
| `pollution:red_sand_infused_human_ore` | red_sand_infused_human_ore | red_sand_infused_human_ore | 世界生成矿石（可挖掘获得） |
| `pollution:red_sand_infused_instrument_ore` | red_sand_infused_instrument_ore | red_sand_infused_instrument_ore | 世界生成矿石（可挖掘获得） |
| `pollution:red_sand_infused_life_ore` | red_sand_infused_life_ore | red_sand_infused_life_ore | 世界生成矿石（可挖掘获得） |
| `pollution:red_sand_infused_light_ore` | red_sand_infused_light_ore | red_sand_infused_light_ore | 世界生成矿石（可挖掘获得） |
| `pollution:red_sand_infused_magic_ore` | red_sand_infused_magic_ore | red_sand_infused_magic_ore | 世界生成矿石（可挖掘获得） |
| `pollution:red_sand_infused_mechanics_ore` | red_sand_infused_mechanics_ore | red_sand_infused_mechanics_ore | 世界生成矿石（可挖掘获得） |
| `pollution:red_sand_infused_metal_ore` | red_sand_infused_metal_ore | red_sand_infused_metal_ore | 世界生成矿石（可挖掘获得） |
| `pollution:red_sand_infused_motion_ore` | red_sand_infused_motion_ore | red_sand_infused_motion_ore | 世界生成矿石（可挖掘获得） |
| `pollution:red_sand_infused_order_ore` | red_sand_infused_order_ore | red_sand_infused_order_ore | 世界生成矿石（可挖掘获得） |
| `pollution:red_sand_infused_plant_ore` | red_sand_infused_plant_ore | red_sand_infused_plant_ore | 世界生成矿石（可挖掘获得） |
| `pollution:red_sand_infused_sense_ore` | red_sand_infused_sense_ore | red_sand_infused_sense_ore | 世界生成矿石（可挖掘获得） |
| `pollution:red_sand_infused_soul_ore` | red_sand_infused_soul_ore | red_sand_infused_soul_ore | 世界生成矿石（可挖掘获得） |
| `pollution:red_sand_infused_spatio_ore` | red_sand_infused_spatio_ore | red_sand_infused_spatio_ore | 世界生成矿石（可挖掘获得） |
| `pollution:red_sand_infused_taint_ore` | red_sand_infused_taint_ore | red_sand_infused_taint_ore | 世界生成矿石（可挖掘获得） |
| `pollution:red_sand_infused_tempus_ore` | red_sand_infused_tempus_ore | red_sand_infused_tempus_ore | 世界生成矿石（可挖掘获得） |
| `pollution:red_sand_infused_thought_ore` | red_sand_infused_thought_ore | red_sand_infused_thought_ore | 世界生成矿石（可挖掘获得） |
| `pollution:red_sand_infused_tinctura_ore` | red_sand_infused_tinctura_ore | red_sand_infused_tinctura_ore | 世界生成矿石（可挖掘获得） |
| `pollution:red_sand_infused_trap_ore` | red_sand_infused_trap_ore | red_sand_infused_trap_ore | 世界生成矿石（可挖掘获得） |
| `pollution:red_sand_infused_undead_ore` | red_sand_infused_undead_ore | red_sand_infused_undead_ore | 世界生成矿石（可挖掘获得） |
| `pollution:red_sand_infused_void_ore` | red_sand_infused_void_ore | red_sand_infused_void_ore | 世界生成矿石（可挖掘获得） |
| `pollution:red_sand_infused_water_ore` | red_sand_infused_water_ore | red_sand_infused_water_ore | 世界生成矿石（可挖掘获得） |
| `pollution:red_sand_infused_weapon_ore` | red_sand_infused_weapon_ore | red_sand_infused_weapon_ore | 世界生成矿石（可挖掘获得） |
| `pollution:red_sand_melt_gold_ore` | red_sand_melt_gold_ore | red_sand_melt_gold_ore | 世界生成矿石（可挖掘获得） |
| `pollution:red_sand_pixie_dust_ore` | red_sand_pixie_dust_ore | red_sand_pixie_dust_ore | 世界生成矿石（可挖掘获得） |
| `pollution:red_sand_pluto_zinc_ore` | red_sand_pluto_zinc_ore | red_sand_pluto_zinc_ore | 世界生成矿石（可挖掘获得） |
| `pollution:red_sand_pyrargyrite_ore` | red_sand_pyrargyrite_ore | red_sand_pyrargyrite_ore | 世界生成矿石（可挖掘获得） |
| `pollution:red_sand_scabyst_ore` | red_sand_scabyst_ore | red_sand_scabyst_ore | 世界生成矿石（可挖掘获得） |
| `pollution:sand_amber_ore` | sand_amber_ore | sand_amber_ore | 世界生成矿石（可挖掘获得） |
| `pollution:sand_authority_lead_ore` | sand_authority_lead_ore | sand_authority_lead_ore | 世界生成矿石（可挖掘获得） |
| `pollution:sand_dragonstone_ore` | sand_dragonstone_ore | sand_dragonstone_ore | 世界生成矿石（可挖掘获得） |
| `pollution:sand_dumb_tin_ore` | sand_dumb_tin_ore | sand_dumb_tin_ore | 世界生成矿石（可挖掘获得） |
| `pollution:sand_elven_elementium_ore` | sand_elven_elementium_ore | sand_elven_elementium_ore | 世界生成矿石（可挖掘获得） |
| `pollution:sand_elven_quartz_ore` | sand_elven_quartz_ore | sand_elven_quartz_ore | 世界生成矿石（可挖掘获得） |
| `pollution:sand_flame_coal_ore` | sand_flame_coal_ore | sand_flame_coal_ore | 世界生成矿石（可挖掘获得） |
| `pollution:sand_infused_air_ore` | sand_infused_air_ore | sand_infused_air_ore | 世界生成矿石（可挖掘获得） |
| `pollution:sand_infused_alchemy_ore` | sand_infused_alchemy_ore | sand_infused_alchemy_ore | 世界生成矿石（可挖掘获得） |
| `pollution:sand_infused_alien_ore` | sand_infused_alien_ore | sand_infused_alien_ore | 世界生成矿石（可挖掘获得） |
| `pollution:sand_infused_animal_ore` | sand_infused_animal_ore | sand_infused_animal_ore | 世界生成矿石（可挖掘获得） |
| `pollution:sand_infused_armor_ore` | sand_infused_armor_ore | sand_infused_armor_ore | 世界生成矿石（可挖掘获得） |
| `pollution:sand_infused_aura_ore` | sand_infused_aura_ore | sand_infused_aura_ore | 世界生成矿石（可挖掘获得） |
| `pollution:sand_infused_cold_ore` | sand_infused_cold_ore | sand_infused_cold_ore | 世界生成矿石（可挖掘获得） |
| `pollution:sand_infused_craft_ore` | sand_infused_craft_ore | sand_infused_craft_ore | 世界生成矿石（可挖掘获得） |
| `pollution:sand_infused_crystal_ore` | sand_infused_crystal_ore | sand_infused_crystal_ore | 世界生成矿石（可挖掘获得） |
| `pollution:sand_infused_dark_ore` | sand_infused_dark_ore | sand_infused_dark_ore | 世界生成矿石（可挖掘获得） |
| `pollution:sand_infused_death_ore` | sand_infused_death_ore | sand_infused_death_ore | 世界生成矿石（可挖掘获得） |
| `pollution:sand_infused_earth_ore` | sand_infused_earth_ore | sand_infused_earth_ore | 世界生成矿石（可挖掘获得） |
| `pollution:sand_infused_energy_ore` | sand_infused_energy_ore | sand_infused_energy_ore | 世界生成矿石（可挖掘获得） |
| `pollution:sand_infused_entropy_ore` | sand_infused_entropy_ore | sand_infused_entropy_ore | 世界生成矿石（可挖掘获得） |
| `pollution:sand_infused_exchange_ore` | sand_infused_exchange_ore | sand_infused_exchange_ore | 世界生成矿石（可挖掘获得） |
| `pollution:sand_infused_fire_ore` | sand_infused_fire_ore | sand_infused_fire_ore | 世界生成矿石（可挖掘获得） |
| `pollution:sand_infused_fly_ore` | sand_infused_fly_ore | sand_infused_fly_ore | 世界生成矿石（可挖掘获得） |
| `pollution:sand_infused_greed_ore` | sand_infused_greed_ore | sand_infused_greed_ore | 世界生成矿石（可挖掘获得） |
| `pollution:sand_infused_human_ore` | sand_infused_human_ore | sand_infused_human_ore | 世界生成矿石（可挖掘获得） |
| `pollution:sand_infused_instrument_ore` | sand_infused_instrument_ore | sand_infused_instrument_ore | 世界生成矿石（可挖掘获得） |
| `pollution:sand_infused_life_ore` | sand_infused_life_ore | sand_infused_life_ore | 世界生成矿石（可挖掘获得） |
| `pollution:sand_infused_light_ore` | sand_infused_light_ore | sand_infused_light_ore | 世界生成矿石（可挖掘获得） |
| `pollution:sand_infused_magic_ore` | sand_infused_magic_ore | sand_infused_magic_ore | 世界生成矿石（可挖掘获得） |
| `pollution:sand_infused_mechanics_ore` | sand_infused_mechanics_ore | sand_infused_mechanics_ore | 世界生成矿石（可挖掘获得） |
| `pollution:sand_infused_metal_ore` | sand_infused_metal_ore | sand_infused_metal_ore | 世界生成矿石（可挖掘获得） |
| `pollution:sand_infused_motion_ore` | sand_infused_motion_ore | sand_infused_motion_ore | 世界生成矿石（可挖掘获得） |
| `pollution:sand_infused_order_ore` | sand_infused_order_ore | sand_infused_order_ore | 世界生成矿石（可挖掘获得） |
| `pollution:sand_infused_plant_ore` | sand_infused_plant_ore | sand_infused_plant_ore | 世界生成矿石（可挖掘获得） |
| `pollution:sand_infused_sense_ore` | sand_infused_sense_ore | sand_infused_sense_ore | 世界生成矿石（可挖掘获得） |
| `pollution:sand_infused_soul_ore` | sand_infused_soul_ore | sand_infused_soul_ore | 世界生成矿石（可挖掘获得） |
| `pollution:sand_infused_spatio_ore` | sand_infused_spatio_ore | sand_infused_spatio_ore | 世界生成矿石（可挖掘获得） |
| `pollution:sand_infused_taint_ore` | sand_infused_taint_ore | sand_infused_taint_ore | 世界生成矿石（可挖掘获得） |
| `pollution:sand_infused_tempus_ore` | sand_infused_tempus_ore | sand_infused_tempus_ore | 世界生成矿石（可挖掘获得） |
| `pollution:sand_infused_thought_ore` | sand_infused_thought_ore | sand_infused_thought_ore | 世界生成矿石（可挖掘获得） |
| `pollution:sand_infused_tinctura_ore` | sand_infused_tinctura_ore | sand_infused_tinctura_ore | 世界生成矿石（可挖掘获得） |
| `pollution:sand_infused_trap_ore` | sand_infused_trap_ore | sand_infused_trap_ore | 世界生成矿石（可挖掘获得） |
| `pollution:sand_infused_undead_ore` | sand_infused_undead_ore | sand_infused_undead_ore | 世界生成矿石（可挖掘获得） |
| `pollution:sand_infused_void_ore` | sand_infused_void_ore | sand_infused_void_ore | 世界生成矿石（可挖掘获得） |
| `pollution:sand_infused_water_ore` | sand_infused_water_ore | sand_infused_water_ore | 世界生成矿石（可挖掘获得） |
| `pollution:sand_infused_weapon_ore` | sand_infused_weapon_ore | sand_infused_weapon_ore | 世界生成矿石（可挖掘获得） |
| `pollution:sand_melt_gold_ore` | sand_melt_gold_ore | sand_melt_gold_ore | 世界生成矿石（可挖掘获得） |
| `pollution:sand_pixie_dust_ore` | sand_pixie_dust_ore | sand_pixie_dust_ore | 世界生成矿石（可挖掘获得） |
| `pollution:sand_pluto_zinc_ore` | sand_pluto_zinc_ore | sand_pluto_zinc_ore | 世界生成矿石（可挖掘获得） |
| `pollution:sand_pyrargyrite_ore` | sand_pyrargyrite_ore | sand_pyrargyrite_ore | 世界生成矿石（可挖掘获得） |
| `pollution:sand_scabyst_ore` | sand_scabyst_ore | sand_scabyst_ore | 世界生成矿石（可挖掘获得） |
| `pollution:scabyst_ore` | Scabyst Ore | 痂壳晶矿石 | 世界生成矿石（可挖掘获得） |
| `pollution:tuff_amber_ore` | tuff_amber_ore | tuff_amber_ore | 世界生成矿石（可挖掘获得） |
| `pollution:tuff_authority_lead_ore` | tuff_authority_lead_ore | tuff_authority_lead_ore | 世界生成矿石（可挖掘获得） |
| `pollution:tuff_dragonstone_ore` | tuff_dragonstone_ore | tuff_dragonstone_ore | 世界生成矿石（可挖掘获得） |
| `pollution:tuff_dumb_tin_ore` | tuff_dumb_tin_ore | tuff_dumb_tin_ore | 世界生成矿石（可挖掘获得） |
| `pollution:tuff_elven_elementium_ore` | tuff_elven_elementium_ore | tuff_elven_elementium_ore | 世界生成矿石（可挖掘获得） |
| `pollution:tuff_elven_quartz_ore` | tuff_elven_quartz_ore | tuff_elven_quartz_ore | 世界生成矿石（可挖掘获得） |
| `pollution:tuff_flame_coal_ore` | tuff_flame_coal_ore | tuff_flame_coal_ore | 世界生成矿石（可挖掘获得） |
| `pollution:tuff_infused_air_ore` | tuff_infused_air_ore | tuff_infused_air_ore | 世界生成矿石（可挖掘获得） |
| `pollution:tuff_infused_alchemy_ore` | tuff_infused_alchemy_ore | tuff_infused_alchemy_ore | 世界生成矿石（可挖掘获得） |
| `pollution:tuff_infused_alien_ore` | tuff_infused_alien_ore | tuff_infused_alien_ore | 世界生成矿石（可挖掘获得） |
| `pollution:tuff_infused_animal_ore` | tuff_infused_animal_ore | tuff_infused_animal_ore | 世界生成矿石（可挖掘获得） |
| `pollution:tuff_infused_armor_ore` | tuff_infused_armor_ore | tuff_infused_armor_ore | 世界生成矿石（可挖掘获得） |
| `pollution:tuff_infused_aura_ore` | tuff_infused_aura_ore | tuff_infused_aura_ore | 世界生成矿石（可挖掘获得） |
| `pollution:tuff_infused_cold_ore` | tuff_infused_cold_ore | tuff_infused_cold_ore | 世界生成矿石（可挖掘获得） |
| `pollution:tuff_infused_craft_ore` | tuff_infused_craft_ore | tuff_infused_craft_ore | 世界生成矿石（可挖掘获得） |
| `pollution:tuff_infused_crystal_ore` | tuff_infused_crystal_ore | tuff_infused_crystal_ore | 世界生成矿石（可挖掘获得） |
| `pollution:tuff_infused_dark_ore` | tuff_infused_dark_ore | tuff_infused_dark_ore | 世界生成矿石（可挖掘获得） |
| `pollution:tuff_infused_death_ore` | tuff_infused_death_ore | tuff_infused_death_ore | 世界生成矿石（可挖掘获得） |
| `pollution:tuff_infused_earth_ore` | tuff_infused_earth_ore | tuff_infused_earth_ore | 世界生成矿石（可挖掘获得） |
| `pollution:tuff_infused_energy_ore` | tuff_infused_energy_ore | tuff_infused_energy_ore | 世界生成矿石（可挖掘获得） |
| `pollution:tuff_infused_entropy_ore` | tuff_infused_entropy_ore | tuff_infused_entropy_ore | 世界生成矿石（可挖掘获得） |
| `pollution:tuff_infused_exchange_ore` | tuff_infused_exchange_ore | tuff_infused_exchange_ore | 世界生成矿石（可挖掘获得） |
| `pollution:tuff_infused_fire_ore` | tuff_infused_fire_ore | tuff_infused_fire_ore | 世界生成矿石（可挖掘获得） |
| `pollution:tuff_infused_fly_ore` | tuff_infused_fly_ore | tuff_infused_fly_ore | 世界生成矿石（可挖掘获得） |
| `pollution:tuff_infused_greed_ore` | tuff_infused_greed_ore | tuff_infused_greed_ore | 世界生成矿石（可挖掘获得） |
| `pollution:tuff_infused_human_ore` | tuff_infused_human_ore | tuff_infused_human_ore | 世界生成矿石（可挖掘获得） |
| `pollution:tuff_infused_instrument_ore` | tuff_infused_instrument_ore | tuff_infused_instrument_ore | 世界生成矿石（可挖掘获得） |
| `pollution:tuff_infused_life_ore` | tuff_infused_life_ore | tuff_infused_life_ore | 世界生成矿石（可挖掘获得） |
| `pollution:tuff_infused_light_ore` | tuff_infused_light_ore | tuff_infused_light_ore | 世界生成矿石（可挖掘获得） |
| `pollution:tuff_infused_magic_ore` | tuff_infused_magic_ore | tuff_infused_magic_ore | 世界生成矿石（可挖掘获得） |
| `pollution:tuff_infused_mechanics_ore` | tuff_infused_mechanics_ore | tuff_infused_mechanics_ore | 世界生成矿石（可挖掘获得） |
| `pollution:tuff_infused_metal_ore` | tuff_infused_metal_ore | tuff_infused_metal_ore | 世界生成矿石（可挖掘获得） |
| `pollution:tuff_infused_motion_ore` | tuff_infused_motion_ore | tuff_infused_motion_ore | 世界生成矿石（可挖掘获得） |
| `pollution:tuff_infused_order_ore` | tuff_infused_order_ore | tuff_infused_order_ore | 世界生成矿石（可挖掘获得） |
| `pollution:tuff_infused_plant_ore` | tuff_infused_plant_ore | tuff_infused_plant_ore | 世界生成矿石（可挖掘获得） |
| `pollution:tuff_infused_sense_ore` | tuff_infused_sense_ore | tuff_infused_sense_ore | 世界生成矿石（可挖掘获得） |
| `pollution:tuff_infused_soul_ore` | tuff_infused_soul_ore | tuff_infused_soul_ore | 世界生成矿石（可挖掘获得） |
| `pollution:tuff_infused_spatio_ore` | tuff_infused_spatio_ore | tuff_infused_spatio_ore | 世界生成矿石（可挖掘获得） |
| `pollution:tuff_infused_taint_ore` | tuff_infused_taint_ore | tuff_infused_taint_ore | 世界生成矿石（可挖掘获得） |
| `pollution:tuff_infused_tempus_ore` | tuff_infused_tempus_ore | tuff_infused_tempus_ore | 世界生成矿石（可挖掘获得） |
| `pollution:tuff_infused_thought_ore` | tuff_infused_thought_ore | tuff_infused_thought_ore | 世界生成矿石（可挖掘获得） |
| `pollution:tuff_infused_tinctura_ore` | tuff_infused_tinctura_ore | tuff_infused_tinctura_ore | 世界生成矿石（可挖掘获得） |
| `pollution:tuff_infused_trap_ore` | tuff_infused_trap_ore | tuff_infused_trap_ore | 世界生成矿石（可挖掘获得） |
| `pollution:tuff_infused_undead_ore` | tuff_infused_undead_ore | tuff_infused_undead_ore | 世界生成矿石（可挖掘获得） |
| `pollution:tuff_infused_void_ore` | tuff_infused_void_ore | tuff_infused_void_ore | 世界生成矿石（可挖掘获得） |
| `pollution:tuff_infused_water_ore` | tuff_infused_water_ore | tuff_infused_water_ore | 世界生成矿石（可挖掘获得） |
| `pollution:tuff_infused_weapon_ore` | tuff_infused_weapon_ore | tuff_infused_weapon_ore | 世界生成矿石（可挖掘获得） |
| `pollution:tuff_melt_gold_ore` | tuff_melt_gold_ore | tuff_melt_gold_ore | 世界生成矿石（可挖掘获得） |
| `pollution:tuff_pixie_dust_ore` | tuff_pixie_dust_ore | tuff_pixie_dust_ore | 世界生成矿石（可挖掘获得） |
| `pollution:tuff_pluto_zinc_ore` | tuff_pluto_zinc_ore | tuff_pluto_zinc_ore | 世界生成矿石（可挖掘获得） |
| `pollution:tuff_pyrargyrite_ore` | tuff_pyrargyrite_ore | tuff_pyrargyrite_ore | 世界生成矿石（可挖掘获得） |
| `pollution:tuff_scabyst_ore` | tuff_scabyst_ore | tuff_scabyst_ore | 世界生成矿石（可挖掘获得） |
| `pollution:venus_amber_ore` | venus_amber_ore | venus_amber_ore | 世界生成矿石（可挖掘获得） |
| `pollution:venus_authority_lead_ore` | venus_authority_lead_ore | venus_authority_lead_ore | 世界生成矿石（可挖掘获得） |
| `pollution:venus_dragonstone_ore` | venus_dragonstone_ore | venus_dragonstone_ore | 世界生成矿石（可挖掘获得） |
| `pollution:venus_dumb_tin_ore` | venus_dumb_tin_ore | venus_dumb_tin_ore | 世界生成矿石（可挖掘获得） |
| `pollution:venus_elven_elementium_ore` | venus_elven_elementium_ore | venus_elven_elementium_ore | 世界生成矿石（可挖掘获得） |
| `pollution:venus_elven_quartz_ore` | venus_elven_quartz_ore | venus_elven_quartz_ore | 世界生成矿石（可挖掘获得） |
| `pollution:venus_flame_coal_ore` | venus_flame_coal_ore | venus_flame_coal_ore | 世界生成矿石（可挖掘获得） |
| `pollution:venus_infused_air_ore` | venus_infused_air_ore | venus_infused_air_ore | 世界生成矿石（可挖掘获得） |
| `pollution:venus_infused_alchemy_ore` | venus_infused_alchemy_ore | venus_infused_alchemy_ore | 世界生成矿石（可挖掘获得） |
| `pollution:venus_infused_alien_ore` | venus_infused_alien_ore | venus_infused_alien_ore | 世界生成矿石（可挖掘获得） |
| `pollution:venus_infused_animal_ore` | venus_infused_animal_ore | venus_infused_animal_ore | 世界生成矿石（可挖掘获得） |
| `pollution:venus_infused_armor_ore` | venus_infused_armor_ore | venus_infused_armor_ore | 世界生成矿石（可挖掘获得） |
| `pollution:venus_infused_aura_ore` | venus_infused_aura_ore | venus_infused_aura_ore | 世界生成矿石（可挖掘获得） |
| `pollution:venus_infused_cold_ore` | venus_infused_cold_ore | venus_infused_cold_ore | 世界生成矿石（可挖掘获得） |
| `pollution:venus_infused_craft_ore` | venus_infused_craft_ore | venus_infused_craft_ore | 世界生成矿石（可挖掘获得） |
| `pollution:venus_infused_crystal_ore` | venus_infused_crystal_ore | venus_infused_crystal_ore | 世界生成矿石（可挖掘获得） |
| `pollution:venus_infused_dark_ore` | venus_infused_dark_ore | venus_infused_dark_ore | 世界生成矿石（可挖掘获得） |
| `pollution:venus_infused_death_ore` | venus_infused_death_ore | venus_infused_death_ore | 世界生成矿石（可挖掘获得） |
| `pollution:venus_infused_earth_ore` | venus_infused_earth_ore | venus_infused_earth_ore | 世界生成矿石（可挖掘获得） |
| `pollution:venus_infused_energy_ore` | venus_infused_energy_ore | venus_infused_energy_ore | 世界生成矿石（可挖掘获得） |
| `pollution:venus_infused_entropy_ore` | venus_infused_entropy_ore | venus_infused_entropy_ore | 世界生成矿石（可挖掘获得） |
| `pollution:venus_infused_exchange_ore` | venus_infused_exchange_ore | venus_infused_exchange_ore | 世界生成矿石（可挖掘获得） |
| `pollution:venus_infused_fire_ore` | venus_infused_fire_ore | venus_infused_fire_ore | 世界生成矿石（可挖掘获得） |
| `pollution:venus_infused_fly_ore` | venus_infused_fly_ore | venus_infused_fly_ore | 世界生成矿石（可挖掘获得） |
| `pollution:venus_infused_greed_ore` | venus_infused_greed_ore | venus_infused_greed_ore | 世界生成矿石（可挖掘获得） |
| `pollution:venus_infused_human_ore` | venus_infused_human_ore | venus_infused_human_ore | 世界生成矿石（可挖掘获得） |
| `pollution:venus_infused_instrument_ore` | venus_infused_instrument_ore | venus_infused_instrument_ore | 世界生成矿石（可挖掘获得） |
| `pollution:venus_infused_life_ore` | venus_infused_life_ore | venus_infused_life_ore | 世界生成矿石（可挖掘获得） |
| `pollution:venus_infused_light_ore` | venus_infused_light_ore | venus_infused_light_ore | 世界生成矿石（可挖掘获得） |
| `pollution:venus_infused_magic_ore` | venus_infused_magic_ore | venus_infused_magic_ore | 世界生成矿石（可挖掘获得） |
| `pollution:venus_infused_mechanics_ore` | venus_infused_mechanics_ore | venus_infused_mechanics_ore | 世界生成矿石（可挖掘获得） |
| `pollution:venus_infused_metal_ore` | venus_infused_metal_ore | venus_infused_metal_ore | 世界生成矿石（可挖掘获得） |
| `pollution:venus_infused_motion_ore` | venus_infused_motion_ore | venus_infused_motion_ore | 世界生成矿石（可挖掘获得） |
| `pollution:venus_infused_order_ore` | venus_infused_order_ore | venus_infused_order_ore | 世界生成矿石（可挖掘获得） |
| `pollution:venus_infused_plant_ore` | venus_infused_plant_ore | venus_infused_plant_ore | 世界生成矿石（可挖掘获得） |
| `pollution:venus_infused_sense_ore` | venus_infused_sense_ore | venus_infused_sense_ore | 世界生成矿石（可挖掘获得） |
| `pollution:venus_infused_soul_ore` | venus_infused_soul_ore | venus_infused_soul_ore | 世界生成矿石（可挖掘获得） |
| `pollution:venus_infused_spatio_ore` | venus_infused_spatio_ore | venus_infused_spatio_ore | 世界生成矿石（可挖掘获得） |
| `pollution:venus_infused_taint_ore` | venus_infused_taint_ore | venus_infused_taint_ore | 世界生成矿石（可挖掘获得） |
| `pollution:venus_infused_tempus_ore` | venus_infused_tempus_ore | venus_infused_tempus_ore | 世界生成矿石（可挖掘获得） |
| `pollution:venus_infused_thought_ore` | venus_infused_thought_ore | venus_infused_thought_ore | 世界生成矿石（可挖掘获得） |
| `pollution:venus_infused_tinctura_ore` | venus_infused_tinctura_ore | venus_infused_tinctura_ore | 世界生成矿石（可挖掘获得） |
| `pollution:venus_infused_trap_ore` | venus_infused_trap_ore | venus_infused_trap_ore | 世界生成矿石（可挖掘获得） |
| `pollution:venus_infused_undead_ore` | venus_infused_undead_ore | venus_infused_undead_ore | 世界生成矿石（可挖掘获得） |
| `pollution:venus_infused_void_ore` | venus_infused_void_ore | venus_infused_void_ore | 世界生成矿石（可挖掘获得） |
| `pollution:venus_infused_water_ore` | venus_infused_water_ore | venus_infused_water_ore | 世界生成矿石（可挖掘获得） |
| `pollution:venus_infused_weapon_ore` | venus_infused_weapon_ore | venus_infused_weapon_ore | 世界生成矿石（可挖掘获得） |
| `pollution:venus_melt_gold_ore` | venus_melt_gold_ore | venus_melt_gold_ore | 世界生成矿石（可挖掘获得） |
| `pollution:venus_pixie_dust_ore` | venus_pixie_dust_ore | venus_pixie_dust_ore | 世界生成矿石（可挖掘获得） |
| `pollution:venus_pluto_zinc_ore` | venus_pluto_zinc_ore | venus_pluto_zinc_ore | 世界生成矿石（可挖掘获得） |
| `pollution:venus_pyrargyrite_ore` | venus_pyrargyrite_ore | venus_pyrargyrite_ore | 世界生成矿石（可挖掘获得） |
| `pollution:venus_scabyst_ore` | venus_scabyst_ore | venus_scabyst_ore | 世界生成矿石（可挖掘获得） |

### 结构/装饰/植物方块（18）

| 注册名 (ID) | 英文名 | 中文名 | 备注 |
| --- | --- | --- | --- |
| `pollution:alfheim_dream_leaves` | Alfheim Dream Leaves | 梦之树叶 | 无任何配方引用 |
| `pollution:alfheim_elven_sand` | Alfheim Elven Sand | 精灵沙 | 无任何配方引用 |
| `pollution:alfheim_red_grape_0` | Alfheim Red Grape 0 | 红葡萄藤 | 无任何配方引用 |
| `pollution:alfheim_red_grape_1` | Alfheim Red Grape 1 | 红葡萄藤 | 无任何配方引用 |
| `pollution:alfheim_red_grape_2` | Alfheim Red Grape 2 | 红葡萄藤 | 无任何配方引用 |
| `pollution:alfheim_white_grape` | Alfheim White Grape | 白葡萄 | 无任何配方引用 |
| `pollution:eldritch_eye` | Eldritch Eye | 深渊之眼 | 无任何配方引用 |
| `pollution:flesh_flower` | Flesh Flower | 血肉之花 | 无任何配方引用 |
| `pollution:flesh_heart` | Flesh Heart | 心脏核心 | 无任何配方引用 |
| `pollution:flesh_leaves` | Flesh Leaves | 血肉树叶 | 无任何配方引用 |
| `pollution:flesh_plant` | Flesh Plant | 血肉植物 | 无任何配方引用 |
| `pollution:flesh_sapling` | Flesh Sapling | 血肉树苗 | 无任何配方引用 |
| `pollution:heart_fruit` | Heart Fruit | 心鸣果 | 无任何配方引用 |
| `pollution:mineral_extractor` | Mineral Extractor | 矿物提取器 | 无任何配方引用 |
| `pollution:polytetrafluoroethylene_pipe` | Polytetrafluoroethylene Pipe | 聚四氟乙烯管道 | 仅作为配方原料 |
| `pollution:rainbow_leaves` | Rainbow Leaves | 魔法彩虹树叶 | 无任何配方引用 |
| `pollution:rainbow_sapling` | Rainbow Sapling | 魔法彩虹树苗 | 无任何配方引用 |
| `pollution:tentacle` | Tentacle | 蠕动触手 | 无任何配方引用 |

### 无物品形态的方块（1）

| 注册名 (ID) | 备注 |
| --- | --- |
| `pollution:portal` | 技术方块（无对应物品，无法以物品形式获得） |

## 物品（88）

| 注册名 (ID) | 英文名 | 中文名 | 备注 |
| --- | --- | --- | --- |
| `pollution:astral_blood_catalyst` | Astral Blood Catalyst | 星辉血魔催化剂 | 无任何配方引用 |
| `pollution:astral_lens_advanced` | Astral Lens Advanced | 高级星辉透镜 | 仅作为配方原料 |
| `pollution:astral_lens_basic` | Astral Lens Basic | 基础星辉透镜 | 仅作为配方原料 |
| `pollution:astral_neural_bundle` | Astral Neural Bundle | 星辉神经束 | 无任何配方引用 |
| `pollution:astral_resonance_coil` | Astral Resonance Coil | 星辉谐振线圈 | 仅作为配方原料 |
| `pollution:attuned_crystal_wafer` | Attuned Crystal Wafer | 调谐晶体晶圆 | 无任何配方引用 |
| `pollution:baubles.water_ring` | Water Ring | 灵水指环 | 无任何配方引用 |
| `pollution:blood_circuit` | Blood Circuit | 原始培养物 | 仅作为配方原料 |
| `pollution:blood_circuit.0` | Blood Circuit 0 | 血肉电路（MV） | 无任何配方引用 |
| `pollution:blood_circuit.1` | Blood Circuit 1 | 血肉电路（HV） | 无任何配方引用 |
| `pollution:blood_circuit.10` | Blood Circuit 10 | 血肉电路（UXV） | 无任何配方引用 |
| `pollution:blood_circuit.11` | Blood Circuit 11 | 血肉电路（OpV） | 无任何配方引用 |
| `pollution:blood_circuit.12` | Blood Circuit 12 | 血肉电路（MAX） | 无任何配方引用 |
| `pollution:blood_circuit.2` | Blood Circuit 2 | 血肉电路（EV） | 无任何配方引用 |
| `pollution:blood_circuit.3` | Blood Circuit 3 | 血肉电路（IV） | 无任何配方引用 |
| `pollution:blood_circuit.4` | Blood Circuit 4 | 血肉电路（LuV） | 无任何配方引用 |
| `pollution:blood_circuit.5` | Blood Circuit 5 | 血肉电路（ZPM） | 无任何配方引用 |
| `pollution:blood_circuit.6` | Blood Circuit 6 | 血肉电路（UV） | 无任何配方引用 |
| `pollution:blood_circuit.7` | Blood Circuit 7 | 血肉电路（UHV） | 无任何配方引用 |
| `pollution:blood_circuit.8` | Blood Circuit 8 | 血肉电路（UEV） | 无任何配方引用 |
| `pollution:blood_circuit.9` | Blood Circuit 9 | 血肉电路（UIV） | 无任何配方引用 |
| `pollution:blood_circuit_supreme` | Blood Circuit Supreme | 星血计算模板 | 无任何配方引用 |
| `pollution:blood_circuit_ultimate` | Blood Circuit Ultimate | 人脑培养模板 | 无任何配方引用 |
| `pollution:blood_port` | Blood Port | 血肉接口 | 仅作为配方原料 |
| `pollution:causality_catalyst` | Causality Catalyst | 因果催化剂 | 无任何配方引用 |
| `pollution:celestial_calibration_core` | Celestial Calibration Core | 天体校准核心 | 仅作为配方原料 |
| `pollution:celestial_crystal_embryo` | Celestial Crystal Embryo | 天体晶体培养体 | 无任何配方引用 |
| `pollution:cogito_defibrillator` | Cogito Defibrillator | 我思除颤仪 | 无任何配方引用 |
| `pollution:constellation_data_wafer` | Constellation Data Wafer | 星座数据晶圆 | 无任何配方引用 |
| `pollution:cultivated_crystal` | Cultivated Crystal | 培育水晶 | 无任何配方引用 |
| `pollution:depleted_magic_core` | Depleted Magic Core | 耗竭魔导核心 | 无任何配方引用 |
| `pollution:devay_pill.1` | Devay Pill 1 | 理智回复药（1） | 无任何配方引用 |
| `pollution:devay_pill.10` | Devay Pill 10 | 理智回复药（10） | 无任何配方引用 |
| `pollution:devay_pill.20` | Devay Pill 20 | 理智回复药（20） | 无任何配方引用 |
| `pollution:devay_pill.5` | Devay Pill 5 | 理智回复药（5） | 无任何配方引用 |
| `pollution:devay_pill.empty` | Devay Pill Empty | 空理智回复药 | 无任何配方引用 |
| `pollution:endorphins_stabilizer` | Endorphins Stabilizer | 内啡肽稳定器 | 仅作为配方原料 |
| `pollution:filter.i` | Filter I | 空气过滤器滤芯 I | 无任何配方引用 |
| `pollution:filter.ii` | Filter Ii | 空气过滤器滤芯 II | 无任何配方引用 |
| `pollution:filter.iii` | Filter Iii | 空气过滤器滤芯 III | 仅作为配方原料 |
| `pollution:filter.iv` | Filter Iv | 空气过滤器滤芯 IV | 无任何配方引用 |
| `pollution:filter.v` | Filter V | 空气过滤器滤芯 V | 无任何配方引用 |
| `pollution:freeze_cooler` | Freeze Cooler | 寒冰冷冻器 | 无任何配方引用 |
| `pollution:harmonizing_rune_core` | Harmonizing Rune Core | 谐律符文核心 | 仅作为配方原料 |
| `pollution:heart_fruit_i` | Heart Fruit | 心鸣果 | 无任何配方引用 |
| `pollution:ips_human_brain` | Ips Human Brain | iPS重建人脑 | 无任何配方引用 |
| `pollution:living_magic_biofilm` | Living Magic Biofilm | 活体魔导生物膜 | 仅作为配方原料 |
| `pollution:lysosome_stabilizer` | Lysosome Stabilizer | 自噬稳定器 | 无任何配方引用 |
| `pollution:magic_circuit.ev` | Magic Circuit Ev | 蕴魔电路板 (§5EV§r) | 仅作为配方原料 |
| `pollution:magic_circuit.hv` | Magic Circuit Hv | 蕴魔电路板 (§6HV§r) | 仅作为配方原料 |
| `pollution:magic_circuit.iv` | Magic Circuit Iv | 蕴魔电路板 (§1IV§r) | 仅作为配方原料 |
| `pollution:magic_circuit.luv` | Magic Circuit Luv | 蕴魔电路板 (§dLuV§r) | 仅作为配方原料 |
| `pollution:magic_circuit.lv` | Magic Circuit Lv | 蕴魔电路板 (§7LV§r) | 仅作为配方原料 |
| `pollution:magic_circuit.max` | Magic Circuit Max | 蕴魔电路板（§c§lMAX§r） | 无任何配方引用 |
| `pollution:magic_circuit.mv` | Magic Circuit Mv | 蕴魔电路板 (§bMV§r) | 仅作为配方原料 |
| `pollution:magic_circuit.opv` | Magic Circuit Opv | 蕴魔电路板（§9OpV§r） | 无任何配方引用 |
| `pollution:magic_circuit.uev` | Magic Circuit Uev | 蕴魔电路板（§aUEV§r） | 无任何配方引用 |
| `pollution:magic_circuit.uhv` | Magic Circuit Uhv | 蕴魔电路板 (§4UHV§r) | 无任何配方引用 |
| `pollution:magic_circuit.uiv` | Magic Circuit Uiv | 蕴魔电路板（§2UIV§r） | 无任何配方引用 |
| `pollution:magic_circuit.ulv` | Magic Circuit Ulv | 蕴魔电路板 (§8ULV§r) | 无任何配方引用 |
| `pollution:magic_circuit.uv` | Magic Circuit Uv | 蕴魔电路板 (§3UV§r) | 仅作为配方原料 |
| `pollution:magic_circuit.uxv` | Magic Circuit Uxv | 蕴魔电路板（§eUXV§r） | 无任何配方引用 |
| `pollution:magic_circuit.zpm` | Magic Circuit Zpm | 蕴魔电路板 (§cZPM§r) | 仅作为配方原料 |
| `pollution:magic_circuit_board.max` | Magic Circuit Board Max | 魔法电路板（§c§lMAX§r） | 无任何配方引用 |
| `pollution:magic_circuit_board.opv` | Magic Circuit Board Opv | 魔法电路板（§9OpV§r） | 无任何配方引用 |
| `pollution:magic_circuit_board.uev` | Magic Circuit Board Uev | 魔法电路板（§aUEV§r） | 无任何配方引用 |
| `pollution:magic_circuit_board.uhv` | Magic Circuit Board Uhv | 魔法电路板（§4UHV§r） | 仅作为配方原料 |
| `pollution:magic_circuit_board.uiv` | Magic Circuit Board Uiv | 魔法电路板（§2UIV§r） | 无任何配方引用 |
| `pollution:magic_circuit_board.uv` | Magic Circuit Board Uv | 魔法电路板（§3UV§r） | 无任何配方引用 |
| `pollution:magic_circuit_board.uxv` | Magic Circuit Board Uxv | 魔法电路板（§eUXV§r） | 无任何配方引用 |
| `pollution:magic_circuit_board.zpm` | Magic Circuit Board Zpm | 魔法电路板（§cZPM§r） | 仅作为配方原料 |
| `pollution:magic_sweep` | Magic Sweep | 魔法扫帚 | 无任何配方引用 |
| `pollution:mitochondrion_power` | Mitochondrion Power | 线粒能源体 | 仅作为配方原料 |
| `pollution:needle_of_mystic_interpellation` | Needle Of Mystic Interpellation | 密契询唤针 | 无任何配方引用 |
| `pollution:packaged_aura_node` | Packaged Aura Node | 封装灵气节点 | 仅作为配方原料 |
| `pollution:pesticide.empty` | Pesticide Empty | 空杀虫剂 | 无任何配方引用 |
| `pollution:pesticide.full` | Pesticide Full | 杀虫剂 | 无任何配方引用 |
| `pollution:precision_rune_blank` | Precision Rune Blank | 精密符文坯 | 仅作为配方原料 |
| `pollution:primitive_meat` | Primitive Meat | 原始血肉团 | 无任何配方引用 |
| `pollution:primordial_star_blood_crystal` | Primordial Star Blood Crystal | 原初星血晶体 | 无任何配方引用 |
| `pollution:rat_brain` | Rat Brain | 鼠脑 | 仅作为配方原料 |
| `pollution:rock_crystal_seed` | Rock Crystal Seed | 岩石水晶晶种 | 无任何配方引用 |
| `pollution:silvered_glass_lens` | Silvered Glass Lens | 镀银玻璃透镜 | 仅作为配方原料 |
| `pollution:stone_of_philosopher_final` | Stone Of Philosopher Final | §5§l真·贤者之石§r | 无任何配方引用 |
| `pollution:tar_slime` | Tar Slime | 焦油史莱姆 | 仅作为配方原料 |
| `pollution:test` | Test | 测试物品 | 调试/占位物品；无任何配方引用 |
| `pollution:test_item` | Test Item | papa测试物品 | 调试/占位物品；无任何配方引用 |
| `pollution:vis_checker` | Vis Checker | 污染检测器 | 仅作为配方原料 |

## 桶/流体容器（123）

| 注册名 (ID) | 英文名 | 中文名 | 备注 |
| --- | --- | --- | --- |
| `pollution:advanced_battery_hull_alloy_bucket` | Advanced Battery Hull Alloy Bucket | 进阶电池外壳合金桶 | GT 流体桶（无配方产出） |
| `pollution:advanced_substrate_bucket` | Advanced Substrate Bucket | 高阶奇术基底桶 | GT 流体桶（无配方产出） |
| `pollution:advanced_thaumic_superconductor_bucket` | Advanced Thaumic Superconductor Bucket | 高阶神秘超导体桶 | GT 流体桶（无配方产出） |
| `pollution:aertitanium_bucket` | Aertitanium Bucket | 律动钛桶 | GT 流体桶（无配方产出） |
| `pollution:aetheric_dark_steel_bucket` | Aetheric Dark Steel Bucket | 太虚玄钢桶 | GT 流体桶（无配方产出） |
| `pollution:alchemical_vapor_1_bucket` | Alchemical Vapor 1 Bucket | 一次升华蒸汽桶 | GT 流体桶（无配方产出） |
| `pollution:alchemical_vapor_2_bucket` | Alchemical Vapor 2 Bucket | 二次升华蒸汽桶 | GT 流体桶（无配方产出） |
| `pollution:alchemical_vapor_3_bucket` | Alchemical Vapor 3 Bucket | 三次升华蒸汽桶 | GT 流体桶（无配方产出） |
| `pollution:alchemical_vapor_4_bucket` | Alchemical Vapor 4 Bucket | 四次升华蒸汽桶 | GT 流体桶（无配方产出） |
| `pollution:alchemical_vapor_5_bucket` | Alchemical Vapor 5 Bucket | 五次升华蒸汽桶 | GT 流体桶（无配方产出） |
| `pollution:alchemical_vapor_6_bucket` | Alchemical Vapor 6 Bucket | 六次升华蒸汽桶 | GT 流体桶（无配方产出） |
| `pollution:aquasilver_bucket` | Aquasilver Bucket | 捩花银桶 | GT 流体桶（无配方产出） |
| `pollution:arcane_ink_bucket` | Arcane Ink Bucket | 奥术墨液桶 | GT 流体桶（无配方产出） |
| `pollution:authority_lead_bucket` | Authority Lead Bucket | 镇渊铅桶 | GT 流体桶（无配方产出） |
| `pollution:basic_battery_hull_alloy_bucket` | Basic Battery Hull Alloy Bucket | 基础电池外壳合金桶 | GT 流体桶（无配方产出） |
| `pollution:basic_substrate_bucket` | Basic Substrate Bucket | 通用奇术基底桶 | GT 流体桶（无配方产出） |
| `pollution:basic_thaumic_superconductor_bucket` | Basic Thaumic Superconductor Bucket | 初阶神秘超导体桶 | GT 流体桶（无配方产出） |
| `pollution:binding_metal_bucket` | Binding Metal Bucket | 缚束金属桶 | GT 流体桶（无配方产出） |
| `pollution:blackmansus_bucket` | Blackmansus Bucket | 黑魔素桶 | GT 流体桶（无配方产出） |
| `pollution:crude_lk_99_bucket` | Crude Lk 99 Bucket | LK-99 粗胚桶 | GT 流体桶（无配方产出） |
| `pollution:dimensional_transforming_agent_bucket` | Dimensional Transforming Agent Bucket | 维度转化剂桶 | GT 流体桶（无配方产出） |
| `pollution:dragon_pulse_fuel_bucket` | Dragon Pulse Fuel Bucket | 龙脉星轨燃剂桶 | GT 流体桶（无配方产出） |
| `pollution:dumb_tin_bucket` | Dumb Tin Bucket | 哑泽锡桶 | GT 流体桶（无配方产出） |
| `pollution:dumb_tin_plasma_bucket` | Dumb Tin Bucket | 哑泽锡桶 | GT 流体桶（无配方产出） |
| `pollution:elven_bucket` | Elven Bucket | 精灵素桶 | GT 流体桶（无配方产出） |
| `pollution:elven_elementium_bucket` | Elven Elementium Bucket | 精灵元素桶 | GT 流体桶（无配方产出） |
| `pollution:embryo_magic_water_bucket` | Embryo Magic Water Bucket | 胚胎魔水桶 | GT 流体桶（无配方产出） |
| `pollution:erich_aura_bucket` | Erich Aura Bucket | 血色灵气桶 | GT 流体桶（无配方产出） |
| `pollution:ethyl_silicate_bucket` | Ethyl Silicate Bucket | 硅酸乙酯桶 | GT 流体桶（无配方产出） |
| `pollution:existing_nexus_bucket` | Existing Nexus Bucket | 既存之枢桶 | GT 流体桶（无配方产出） |
| `pollution:fading_nexus_bucket` | Fading Nexus Bucket | 消逝之枢桶 | GT 流体桶（无配方产出） |
| `pollution:filth_water_bucket` | Filth Water Bucket | 污秽之水桶 | GT 流体桶（无配方产出） |
| `pollution:hydrazoic_acid_bucket` | Hydrazoic Acid Bucket | 叠氮酸桶 | GT 流体桶（无配方产出） |
| `pollution:hyper_substrate_bucket` | Hyper Substrate Bucket | 玄想奇术基底桶 | GT 流体桶（无配方产出） |
| `pollution:hyperdimensional_silver_bucket` | Hyperdimensional Silver Bucket | 超次元秘银桶 | GT 流体桶（无配方产出） |
| `pollution:hyperdimensional_silver_plasma_bucket` | Hyperdimensional Silver Bucket | 超次元秘银桶 | GT 流体桶（无配方产出） |
| `pollution:ignissteel_bucket` | Ignissteel Bucket | 残日钢桶 | GT 流体桶（无配方产出） |
| `pollution:iizunamaru_electrum_bucket` | Iizunamaru Electrum Bucket | 光风霁月琥珀金桶 | GT 流体桶（无配方产出） |
| `pollution:impure_hyperdimensional_silver_bucket` | Impure Hyperdimensional Silver Bucket | 超次元含杂秘银桶 | GT 流体桶（无配方产出） |
| `pollution:impure_mercuric_salt_solution_bucket` | Impure Mercuric Salt Solution Bucket | 含杂汞盐溶液桶 | GT 流体桶（无配方产出） |
| `pollution:impuremana_bucket` | Impuremana Bucket | 不纯魔力桶 | GT 流体桶（无配方产出） |
| `pollution:infernal_blaze_propellant_bucket` | Infernal Blaze Propellant Bucket | 焚天烈焰推进剂桶 | GT 流体桶（无配方产出） |
| `pollution:infused_air_bucket` | Infused Air Bucket | 气桶 | GT 流体桶（无配方产出） |
| `pollution:infused_alchemy_bucket` | Infused Alchemy Bucket | 炼金桶 | GT 流体桶（无配方产出） |
| `pollution:infused_alien_bucket` | Infused Alien Bucket | 异桶 | GT 流体桶（无配方产出） |
| `pollution:infused_animal_bucket` | Infused Animal Bucket | 造化桶 | GT 流体桶（无配方产出） |
| `pollution:infused_armor_bucket` | Infused Armor Bucket | 胄桶 | GT 流体桶（无配方产出） |
| `pollution:infused_aura_bucket` | Infused Aura Bucket | 灵桶 | GT 流体桶（无配方产出） |
| `pollution:infused_cold_bucket` | Infused Cold Bucket | 寒桶 | GT 流体桶（无配方产出） |
| `pollution:infused_craft_bucket` | Infused Craft Bucket | 创桶 | GT 流体桶（无配方产出） |
| `pollution:infused_crystal_bucket` | Infused Crystal Bucket | 晶桶 | GT 流体桶（无配方产出） |
| `pollution:infused_dark_bucket` | Infused Dark Bucket | 暗桶 | GT 流体桶（无配方产出） |
| `pollution:infused_death_bucket` | Infused Death Bucket | 死桶 | GT 流体桶（无配方产出） |
| `pollution:infused_earth_bucket` | Infused Earth Bucket | 土桶 | GT 流体桶（无配方产出） |
| `pollution:infused_energy_bucket` | Infused Energy Bucket | 能桶 | GT 流体桶（无配方产出） |
| `pollution:infused_entropy_bucket` | Infused Entropy Bucket | 熵桶 | GT 流体桶（无配方产出） |
| `pollution:infused_exchange_bucket` | Infused Exchange Bucket | 易桶 | GT 流体桶（无配方产出） |
| `pollution:infused_fire_bucket` | Infused Fire Bucket | 焱桶 | GT 流体桶（无配方产出） |
| `pollution:infused_fly_bucket` | Infused Fly Bucket | 羽桶 | GT 流体桶（无配方产出） |
| `pollution:infused_greed_bucket` | Infused Greed Bucket | 贪桶 | GT 流体桶（无配方产出） |
| `pollution:infused_human_bucket` | Infused Human Bucket | 物灵桶 | GT 流体桶（无配方产出） |
| `pollution:infused_instrument_bucket` | Infused Instrument Bucket | 器桶 | GT 流体桶（无配方产出） |
| `pollution:infused_life_bucket` | Infused Life Bucket | 生桶 | GT 流体桶（无配方产出） |
| `pollution:infused_light_bucket` | Infused Light Bucket | 光桶 | GT 流体桶（无配方产出） |
| `pollution:infused_magic_bucket` | Infused Magic Bucket | 魔桶 | GT 流体桶（无配方产出） |
| `pollution:infused_mechanics_bucket` | Infused Mechanics Bucket | 械桶 | GT 流体桶（无配方产出） |
| `pollution:infused_metal_bucket` | Infused Metal Bucket | 金属桶 | GT 流体桶（无配方产出） |
| `pollution:infused_motion_bucket` | Infused Motion Bucket | 动桶 | GT 流体桶（无配方产出） |
| `pollution:infused_order_bucket` | Infused Order Bucket | 序桶 | GT 流体桶（无配方产出） |
| `pollution:infused_plant_bucket` | Infused Plant Bucket | 草木桶 | GT 流体桶（无配方产出） |
| `pollution:infused_sense_bucket` | Infused Sense Bucket | 感桶 | GT 流体桶（无配方产出） |
| `pollution:infused_soul_bucket` | Infused Soul Bucket | 魂桶 | GT 流体桶（无配方产出） |
| `pollution:infused_spatio_bucket` | Infused Spatio Bucket | 空桶 | GT 流体桶（无配方产出） |
| `pollution:infused_taint_bucket` | Infused Taint Bucket | 秽桶 | GT 流体桶（无配方产出） |
| `pollution:infused_tempus_bucket` | Infused Tempus Bucket | 时桶 | GT 流体桶（无配方产出） |
| `pollution:infused_thought_bucket` | Infused Thought Bucket | 思桶 | GT 流体桶（无配方产出） |
| `pollution:infused_tinctura_bucket` | Infused Tinctura Bucket | 艺桶 | GT 流体桶（无配方产出） |
| `pollution:infused_trap_bucket` | Infused Trap Bucket | 缚桶 | GT 流体桶（无配方产出） |
| `pollution:infused_undead_bucket` | Infused Undead Bucket | 僵桶 | GT 流体桶（无配方产出） |
| `pollution:infused_void_bucket` | Infused Void Bucket | 虚桶 | GT 流体桶（无配方产出） |
| `pollution:infused_water_bucket` | Infused Water Bucket | 流桶 | GT 流体桶（无配方产出） |
| `pollution:infused_weapon_bucket` | Infused Weapon Bucket | 武桶 | GT 流体桶（无配方产出） |
| `pollution:keqinggold_bucket` | Keqinggold Bucket | 刻金桶 | GT 流体桶（无配方产出） |
| `pollution:keqinggold_plasma_bucket` | Keqinggold Bucket | 刻金桶 | GT 流体桶（无配方产出） |
| `pollution:magic_activated_ferrous_chloride_ethanol_solution_bucket` | Magic Activated Ferrous Chloride Ethanol Solution Bucket | 魔力激活氯化亚铁甲醇溶液桶 | GT 流体桶（无配方产出） |
| `pollution:magic_activated_iron_chloride_solution_bucket` | Magic Activated Iron Chloride Solution Bucket | 魔力激活氯化铁溶液桶 | GT 流体桶（无配方产出） |
| `pollution:magic_nitrobenzene_bucket` | Magic Nitrobenzene Bucket | 魔力抗爆焦化硝基苯桶 | GT 流体桶（无配方产出） |
| `pollution:magical_stannous_sulfate_solution_bucket` | Magical Stannous Sulfate Solution Bucket | 硫酸亚锡神秘溶液桶 | GT 流体桶（无配方产出） |
| `pollution:magical_superconductive_liquid_bucket` | Magical Superconductive Liquid Bucket | 灌魔超导液桶 | GT 流体桶（无配方产出） |
| `pollution:magical_tin_solution_bucket` | Magical Tin Solution Bucket | 神秘锡溶液桶 | GT 流体桶（无配方产出） |
| `pollution:melt_gold_bucket` | Melt Gold Bucket | 铄世金桶 | GT 流体桶（无配方产出） |
| `pollution:mercuric_salt_solution_bucket` | Mercuric Salt Solution Bucket | 神秘汞盐溶液桶 | GT 流体桶（无配方产出） |
| `pollution:molten_advanced_battery_hull_alloy_bucket` | molten_advanced_battery_hull_alloy_bucket | molten_advanced_battery_hull_alloy_bucket | GT 流体桶（无配方产出） |
| `pollution:molten_aertitanium_bucket` | molten_aertitanium_bucket | molten_aertitanium_bucket | GT 流体桶（无配方产出） |
| `pollution:molten_aquasilver_bucket` | molten_aquasilver_bucket | molten_aquasilver_bucket | GT 流体桶（无配方产出） |
| `pollution:molten_basic_battery_hull_alloy_bucket` | molten_basic_battery_hull_alloy_bucket | molten_basic_battery_hull_alloy_bucket | GT 流体桶（无配方产出） |
| `pollution:molten_crude_lk_99_bucket` | molten_crude_lk_99_bucket | molten_crude_lk_99_bucket | GT 流体桶（无配方产出） |
| `pollution:molten_ignissteel_bucket` | molten_ignissteel_bucket | molten_ignissteel_bucket | GT 流体桶（无配方产出） |
| `pollution:molten_ordolead_bucket` | molten_ordolead_bucket | molten_ordolead_bucket | GT 流体桶（无配方产出） |
| `pollution:molten_perditioaluminium_bucket` | molten_perditioaluminium_bucket | molten_perditioaluminium_bucket | GT 流体桶（无配方产出） |
| `pollution:molten_terracopper_bucket` | molten_terracopper_bucket | molten_terracopper_bucket | GT 流体桶（无配方产出） |
| `pollution:moonlight_resin_bucket` | Moonlight Resin Bucket | 月光树脂桶 | GT 流体桶（无配方产出） |
| `pollution:octine_bucket` | Octine Bucket | 炽炎铁桶 | GT 流体桶（无配方产出） |
| `pollution:oil_with_llp_bucket` | Oil With Llp Bucket | 含 LLP 油桶 | GT 流体桶（无配方产出） |
| `pollution:ordolead_bucket` | Ordolead Bucket | 司辰铅桶 | GT 流体桶（无配方产出） |
| `pollution:perditioaluminium_bucket` | Perditioaluminium Bucket | 无极铝桶 | GT 流体桶（无配方产出） |
| `pollution:pluto_zinc_bucket` | Pluto Zinc Bucket | 冥晶锌桶 | GT 流体桶（无配方产出） |
| `pollution:pure_tar_bucket` | Pure Tar Bucket | 纯净焦油桶 | GT 流体桶（无配方产出） |
| `pollution:purified_activated_ferrous_chloride_ethanol_solution_bucket` | Purified Activated Ferrous Chloride Ethanol Solution Bucket | 除杂激活氯化亚铁甲醇溶液桶 | GT 流体桶（无配方产出） |
| `pollution:pyrargyrite_bucket` | Pyrargyrite Bucket | 深红银桶 | GT 流体桶（无配方产出） |
| `pollution:rich_aura_bucket` | Rich Aura Bucket | 富集灵气桶 | GT 流体桶（无配方产出） |
| `pollution:scabyst_bucket` | Scabyst Bucket | 痂壳晶桶 | GT 流体桶（无配方产出） |
| `pollution:sentient_metal_bucket` | Sentient Metal Bucket | 感知金属桶 | GT 流体桶（无配方产出） |
| `pollution:starlight_pollen_bucket` | Starlight Pollen Bucket | 星光花粉桶 | GT 流体桶（无配方产出） |
| `pollution:starrymansus_bucket` | Starrymansus Bucket | 星魔素桶 | GT 流体桶（无配方产出） |
| `pollution:super_sticky_tar_bucket` | Super Sticky Tar Bucket | 超级黏性焦油桶 | GT 流体桶（无配方产出） |
| `pollution:syrmorite_bucket` | Syrmorite Bucket | 赛摩铜桶 | GT 流体桶（无配方产出） |
| `pollution:syrmorite_doped_magic_water_solution_bucket` | Syrmorite Doped Magic Water Solution Bucket | 赛摩铜掺杂魔水溶液桶 | GT 流体桶（无配方产出） |
| `pollution:terracopper_bucket` | Terracopper Bucket | 定坤铜桶 | GT 流体桶（无配方产出） |
| `pollution:unformed_embryo_magic_water_bucket` | Unformed Embryo Magic Water Bucket | 未成形胚胎魔水桶 | GT 流体桶（无配方产出） |
| `pollution:valonite_bucket` | Valonite Bucket | 法罗钠桶 | GT 流体桶（无配方产出） |
| `pollution:void_water_bucket` | Void Water Bucket | 虚空之水桶 | GT 流体桶（无配方产出） |
| `pollution:whitemansus_bucket` | Whitemansus Bucket | 白魔素桶 | GT 流体桶（无配方产出） |
