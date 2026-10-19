# 浮点类型向目标端推演规则

## 1. 目的与范围

本文定义源字段已推演为 `TapFloat` 或 `TapDouble` 时，如何从目标数据源的类型规格中选择目标类型。规则只约束这两种源 TapType 的向下游推演；其他 TapType 继续使用既有推演流程。

推演分为两个彼此独立的阶段：

1. **源端归类**：由源数据源规格把字段归类为 `TapFloat` 或 `TapDouble`。目标端选择不得反向改变源端归类。
2. **目标端选择**：依据源 TapType，按本规则限定的目标类型档位选择。不得把不同档位的候选放进同一个全局评分池。

例如，SQL Server 的 `real` 应先由源规格归类为 `TapFloat`；之后才应用 `TapFloat` 的目标选择规则。目标类型的名字或目标端评分不能把这个源字段改判为 `TapDouble`。

## 2. 术语

**目标映射项**：目标数据源类型规格中的一个类型表达式及其 Tap 映射配置。

**目标档位**：映射项根据其声明能力可解析到的 Tap 家族。固定映射由 `to` 声明；动态映射必须依据其明确声明的能力范围判定。例如，系数映射须检查 `coefficient` 中是否声明了 `TapFloat` 或 `TapDouble` 范围，不能仅因映射类名称含有 `Float`，或源字段是 `TapFloat`，就假定它属于某一档。

**档位存在**：目标规格中至少有一个可达映射项声明支持该档位。可达映射项必须同时满足 `queryOnly` 不为 `true` 且 `cannotWrite` 不为 `true`；未配置 `cannotWrite` 时按 `false` 处理。档位存在表示应优先尝试该档；如果该档没有任何可用候选，则记录警告并继续尝试下一档。

**可用候选**：映射项根据源 TapType 生成了具体目标表达式，表达式没有未展开的变量或可选片段，并且该表达式重新按目标规格解析后仍属于预期档位。

上述“可用候选”定义只适用于目标规格至少声明一个可达数值档位、并进入第 3 节严格档位选择的候选；第 7 节的 legacy 兼容路径不适用本定义。

## 3. 强制档位顺序

### 3.1 源类型为 `TapFloat`

依次处理以下档位：

1. `TapFloat`
2. `TapDouble`
3. `TapNumber`

对每个档位，先收集该档的全部目标映射项，再在本档候选中选最合适项。当前档位没有任何可用候选时，允许按顺序继续尝试下一档。

因此，只要目标规格中存在一个 `TapFloat` 映射项，就必须先尝试 `TapFloat` 档。若本档存在可用候选，最终结果必须来自 `TapFloat` 档；若本档所有候选均不可用，则记录 `warn` 并继续尝试 `TapDouble`，再按顺序尝试 `TapNumber`。所有已声明档位均没有可用候选时，才报告推演失败，且不得输出未展开模板或未经验证的兜底类型。

### 3.2 源类型为 `TapDouble`

依次处理以下档位：

1. `TapDouble`
2. `TapNumber`

`TapDouble` 源字段永远不得降到 `TapFloat`。只要目标规格存在 `TapDouble` 档映射项，就必须先尝试该档；本档无法生成可用候选时，记录 `warn` 并继续选择 `TapNumber`。两个档位均没有可用候选时，才报告推演失败。

### 3.3 档位隔离

不同档位之间不比较 `matchingScore`、`priority` 或其他评分。档位顺序先于所有候选评分：先尝试当前档位；只有当前档位没有可用候选时，才继续尝试下一档。评分只能用于同一档位内部的候选排序。

### 3.4 `findPossibleDataTypes` 的结果

`findPossibleDataTypes` 只记录最终选中档位中的可用候选，不把已跳过档位的候选混入当前结果。当前档位候选全部不可用时，记录 `warn` 并继续尝试下一档；如果所有已声明数值档位均没有可用候选，必须保留该字段的空条目并返回失败结果，不得写入任何未经验证的非 `null` 类型或字符串兜底类型。

## 4. 档位内的候选选择

每个档位内必须对所有映射项进行评估，不能用以 `priority` 为唯一键的容器覆盖同优先级候选。

`queryOnly=true` 或 `cannotWrite=true` 的映射项均不可作为目标候选，也不参与档位存在判定。`queryOnly` 继续用于源端字段类型归类等既有流程；本规则在目标端选择时同时检查这两个标志。

候选排序只在**同一档位**内进行，顺序如下：

1. `matchingScore` 越高越优。
2. 分数相同则较小的 `priority` 越优。
3. `priority` 未配置或同值时，按目标 `datatypes` 中类型定义的先后顺序选择。`priority` 是可选项，不得因其缺失而拒绝推演。

同档同分同优先级时，类型定义顺序就是确定的决胜规则；不得再用表达式字典序或运行时 map 的分组遍历顺序替代。

## 5. 严格档位候选的表达式生成与校验

以下校验仅约束第 3 节中已声明目标档位的严格候选。每个候选必须分别完成以下校验，全部通过后才能参与本档排序；第 7 节的 legacy 兼容路径不执行这些校验：

1. 映射项能根据源 TapType 生成目标表达式。
2. 结果非空，且结果状态成功。
3. 结果是完整、具体的目标表达式，不得残留 `$precision`、`$scale` 等变量，也不得残留未处理的可选片段标记。
4. 使用目标数据源的表达式匹配规格重新解析该结果。
5. 重新解析所得 TapType 必须属于当前档。特别地，`TapFloat` 和 `TapDouble` 虽然在 Java 类型层级上属于 `TapNumber`，但不能因此被当作 `TapNumber` 档候选。

不能通过校验的候选必须从本档候选排序中排除。排除候选不会改变档位是否存在，但如果本档候选全部被排除，则允许按既定顺序继续尝试下一档。

### 5.1 浮点目标表达式中的 `precision`/`scale`

目标映射项可以使用带可选参数组的表达式，例如 `Float32[($precision,$scale)]`。当前实现按源 `TapType` 上已经生效的 `precision`/`scale` 值生成目标表达式，而不是按这些值是否由源字段显式提供来判断。参数按以下规则处理：

1. 当源 `TapType` 的 `precision` 和 `scale` 都为 `null` 时，按无属性处理，移除整个可选参数组，结果为 `Float32` 等无参数表达式。该判断基于 `TapType` 当前保存的属性值，不表示源字段没有显式属性。
2. 当前 `TapType` 只保存其中一个非空属性时，保留已有属性，并为缺失属性补值。补值沿用现有数值映射规则，优先级为 `preferPrecision`/`preferScale`、`defaultPrecision`/`defaultScale`，最后使用对应范围上限。
3. 当前 `TapType` 同时保存两个非空属性时，两个属性都按已保存的值展开。
4. 展开或移除参数组后，结果仍必须满足本节的完整表达式校验，并重新解析为当前目标档位；不能保留 `$precision`、`$scale` 或可选片段标记。
5. `precision`/`scale` 的参数关系合法性（例如是否要求 `scale <= precision`）由具体目标映射自行声明/验证；通用浮点 fallback 不擅自截断、删除或拒绝未声明为非法的参数组合。

`TapFloat`/`TapDouble` 的构造函数会预先写入有效精度（分别为 `7`/`15`），而 `scale` 通常由目标映射的默认规则补齐。因此，即使源字段没有显式 `precision`/`scale`，当前结果也可能包含这些生效值，例如 `new TapFloat()` 配合带参数的 `Float32` 映射会生成 `Float32(7,8)`。这就是当前规则和测试的预期行为；属性来源标记尚未纳入模型，不能据此把模型默认值当作“源字段无属性”并移除参数组。若未来引入属性来源标记并改变该语义，必须同步更新本节规则、实现和测试。

## 6. SQL Server 到 MySQL 示例

假设源字段 `real` 已由 SQL Server 源规格推演为 `TapFloat`，目标 MySQL 规格中同时定义了：

- 一个或多个映射到 `TapFloat` 的类型表达式，包括 `float` 及带参数的 float 表达式；
- 映射到 `TapDouble` 的 `double` 类型；
- 映射到 `TapNumber` 的数值类型。

目标选择必须先只考虑所有 `TapFloat` 映射项。`double` 不能因为评分更高而越过 `TapFloat` 档。参数化表达式若仍输出为 `float($precision,$scale)`，不是可用候选；该结果不得进入字段推演结果。如果所有 `TapFloat` 候选均不可用，则记录 `warn` 并继续尝试 `TapDouble`，最终从第一个存在可用候选的档位中选出最合适项。

若目标规格没有任何可用的 `TapFloat` 候选，或 `TapFloat` 候选全部不可用，则继续尝试 `TapDouble`；若 `TapDouble` 也没有可用候选，再尝试 `TapNumber`。同理，`TapDouble` 源字段从 `TapDouble` 开始，本档候选全部不可用时才尝试 `TapNumber`。

## 7. 无档位声明时的 legacy 兼容路径

当目标规格中不存在任何可达的 `TapFloat`、`TapDouble` 或 `TapNumber` 档位声明时，严格档位选择不适用。`calculateFloatingPointBestTypeMapping` 返回 `null`，之后继续沿用既有的自定义 codec、全局评分映射和字符串兜底流程；这是有意保留的兼容性行为。

legacy `calculateBestTypeMapping` 只执行既有的 `queryOnly`/`cannotWrite` 可选性过滤，不执行 `isUsableFloatingPointCandidate` 的模板检查、目标档位重解析或档位归属校验。因此，一个既未声明 `singlePrecision` 也未声明 `doublePrecision` 的旧 `TapFloatingPointMapping`，如果其 `fromTapType` 返回了包含 `$precision` 等标记的表达式，该结果仍按 legacy 兼容路径保留。这是预期行为，不构成对第 5 节严格候选校验或第 8 节严格路径验收条件的违反。

不得在本规则范围内为未声明档位的映射推断目标档位，也不得将严格候选校验强行套用到该兼容路径。若未来要求 legacy 路径也拒绝未展开模板，必须单独修改本节契约并补充兼容性方案。

## 8. 验收条件

- `TapFloat` 只会按 `TapFloat → TapDouble → TapNumber` 的档位顺序选择；`TapDouble` 只会按 `TapDouble → TapNumber` 的顺序选择。
- 只要当前档存在可用候选，低档就不能因评分、优先级或遍历顺序而胜出；当前档候选全部渲染失败时，必须记录 `warn` 并继续尝试下一档。
- 对已声明档位的严格选择路径，返回的目标表达式可被目标规格重新解析为预期档位，且不包含未展开模板；无档位声明时按第 7 节 legacy 兼容路径处理，不适用该保证。
- 在已声明档位的严格路径中，当 `TapType` 的 `precision` 和 `scale` 都为 `null` 时移除可选参数组；部分属性缺失时按既有默认值规则补齐，不能生成带未展开参数的表达式。对于 `TapFloat`/`TapDouble`，模型构造函数写入的有效精度属于当前可用属性，因此无显式源属性时仍可能生成类似 `Float32(7,8)` 的带参数表达式；无档位声明时按第 7 节处理。
- priority 未配置时，同档候选按 `datatypes` 定义顺序稳定决胜。
- 档位扫描与候选选择均排除 `queryOnly=true` 或 `cannotWrite=true` 的映射项；未配置 `cannotWrite` 时按 `false` 处理。
- 所有已声明数值档均无可用候选时，返回失败结果并保留该字段的空 `findPossibleDataTypes` 条目，不写入未经验证的兜底类型。
- 非 `TapFloat`/`TapDouble` 源类型的推演结果保持原有行为。
