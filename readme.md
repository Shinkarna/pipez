# Pipez Community Edition

[English](#english) | [中文](#中文)

---

<a name="english"></a>
## English

**Pipez Community Edition** is a community-maintained fork of [Pipez](https://github.com/henkelmax/pipez) — a Minecraft mod that adds simple, highly configurable pipes for item, fluid, energy, and gas transport. This fork focuses on **server-side performance optimization**, **Mekanism multiblock compatibility**, and **visual improvements**.

### Pipe Types

- Item Pipes
- Fluid Pipes
- Energy Pipes
- Gas Pipes (requires [Mekanism](https://www.curseforge.com/minecraft/mc-mods/mekanism))
- Universal Pipes (all four types in one block)

### Original Features

- Highly configurable filter system (whitelist/blacklist, tag filtering, NBT matching)
- Three redstone modes (ignore / on when powered / off when powered)
- Four distribution modes (round robin / nearest first / furthest first / random)
- Five upgrade tiers (Basic → Improved → Advanced → Ultimate → Infinity)
- Fully customizable transfer speeds via server config
- Disconnectable with wrenches
- Copyable upgrade configurations

### Community Edition Optimizations

####  Architecture (v1.0.1)
- **Lightweight connection objects** — Capabilities queried directly each tick (not cached on ephemeral Connection objects), eliminating massive GC pressure during network rebuilds
- **BFS data structure fix** — Replaced O(n²) HashMap-as-queue with O(n) ArrayDeque + HashSet for pipe network traversal
- **Sorted connection cache** — Per-side/per-type/per-distribution sorted connection lists cached until topology changes, eliminating redundant stream-sort-collect every tick
- **Filter for-each** — Replaced dual Stream pipelines in `canInsert()` with single-pass for-each loops
- **Reduced `markPipesDirty` triggers** — Only invalidate pipe network when directional connections actually change (not on WATERLOGGED or HAS_DATA updates)
- **Eliminated double block update** — `setExtracting()` now uses single `sendBlockUpdated` instead of two `setBlockAndUpdate` calls

####   Item Transfer (v1.0.2 – v1.0.5)
- **Slot index cache** — Remembers last successful extract slot per side, skips already-empty slots on subsequent ticks
- **Filter result cache** — Item-type → allow/deny HashMap, O(1) lookup after first filter match
- **Batch extraction** — For `insertOrdered` (nearest/furthest/random): one simulated extract + one batch insert + one real extract = **3 API calls per tick** (regardless of item count)
- **Batch distribution** — For `insertEqually` (round-robin): pre-calculates per-destination allocation, batch inserts with waterfall redistribution for leftovers. Reduces API calls from O(items × destinations) to O(slots × destinations)

#### ⏱️ Idle Backoff (v1.0.6)
- **Exponential backoff** — When a pipe side has nothing to transfer, it skips ticks with increasing delay (2 → 4 → 8 → 16 → 32 ticks max). On successful extraction, the delay halves immediately. Configurable via server config.

####   Mekanism Fix (v1.0.1)
- **Dummy capability handlers** — Prevents Pipez from caching "no capability" on unformed Mekanism multiblocks (boilers, turbines, reactors, etc.)
- **Auto-reconnect on multiblock formation** — Forces pipe network cache invalidation when any Mekanism multiblock structure assembles

####   Visual
- **Pretty Pipez textures** by the community — higher-resolution pipe models with connector details
- **Classic texture pack** — original textures included as an optional built-in resource pack (enable in Resource Packs menu)

### Performance Data

Testing setup: 6400 items/tick, round-robin distribution, Mekanism Creative Bins, Forge 47.4.20.

| Version | Pipe tick overhead | vs Original |
|---------|:---:|:---:|
| Original Pipez 1.2.26 | 2600 μs/t | baseline |
| Pipez CE v1.0.1 (architecture) | 1300 μs/t | **-50%** |
| Pipez CE v1.0.3 (filter + slot cache) | 1060 μs/t | -59% |
| Pipez CE v1.0.5 (batch distribution) | ~400 μs/t | -85% |
| **Pipez CE v1.0.6 (backoff + all above)** | **109 μs/t** | **-96%** |

*Measured with Spark Profiler and Observable. 24× improvement over original.*

### ⚠️ Testing Disclaimer

Tests were conducted primarily with **Mekanism Creative Bins** (single-slot item containers) in round-robin and nearest-first modes. The following scenarios have **not** been exhaustively tested and may behave differently:

- Fluid pipes under high throughput
- Energy pipes with multiple destinations
- Gas/chemical pipes (Mekanism)
- Large-scale multi-slot inventories (AE2 interfaces, drawers, etc.)
- Extreme pipe networks (10,000+ connected pipes)

**Use in production at your own discretion. Report issues on GitHub.**

### Dependencies

- Minecraft 1.20.1, Forge 47.2.0+
- All external mods (Mekanism, JEI, Jade, The One Probe) are **optional** — the mod works standalone

### Links

- [GitHub](https://github.com/Shinkarna/pipez)

### Credits

- **NekoGan** — Pipez Community Edition author, performance optimizations
- **Max Henkel** ([henkelmax](https://github.com/henkelmax)) — original Pipez mod
- **AI-Assisted Development** — optimizations implemented with [Claude Code](https://claude.ai/code) powered by **DeepSeek v4 Pro**
- **Pretty Pipez** — community texture pack ([CurseForge](https://www.curseforge.com/minecraft/texture-packs/pretty-pipez))
- **MekanismPipezFix** by [yuuki1293](https://github.com/yuuki1293/PipezMekanismFix) — Mekanism multiblock compatibility fix
- **PipezLagFix** by [AlmanaX21](https://github.com/Almana-mc/PipezLagFix) — exponential backoff concept

### License

This fork is a derivative work. See the original Pipez license.

---

<a name="中文"></a>
## 中文

**Pipez Community Edition（管道社区版）** 是 [Pipez](https://github.com/henkelmax/pipez) 的社区维护分支。在原版基础上深度优化了**服务端性能**、修复了 **Mekanism 多方块兼容性**，并改进了**视觉效果**。

### 管道类型

- 物品管道 / 流体管道 / 能量管道
- 气体管道（需安装 Mekanism）
- 通用管道（四种合一）

### 原版功能

- 高度可配置的过滤器系统：黑白名单、Tag 过滤、NBT 精确/模糊/忽略匹配
- 三种红石模式、四种分配模式（轮询/最近优先/最远优先/随机）
- 五级升级卡（基础→进阶→高级→终极→无限）
- 通过服务器配置文件完全自定义传输速率
- 扳手断开/重连、升级卡配置可复制

### 社区版优化

####  架构层 (v1.0.1)
- **Connection 轻量化** — 移除 Connection 上的 LazyOptional 缓存，改为每 tick 直接查询 Capability，大规模减少管道网络重建时的 GC 压力
- **BFS 数据结构修复** — HashMap 模拟队列 (O(n²)) → ArrayDeque + HashSet (O(n))
- **排序连接缓存** — 每面/每类型/每分配模式的排序结果缓存，消除每 tick 的 stream-sort-collect
- **过滤器 for-each** — canInsert() 中双 Stream pipeline → 单次 for-each 遍历
- **减少 markPipesDirty 触发** — 只在方向连接属性实际变化时失效管道网络（不再因 WATERLOGGED 等无关属性触发）
- **消除双重方块更新** — setExtracting() 用单次 sendBlockUpdated 替代两次 setBlockAndUpdate

####  物品传输层 (v1.0.2 – v1.0.5)
- **槽位索引缓存** — 记住上次成功提取的槽位，下一 tick 直接跳过空槽位
- **过滤器结果缓存** — 物品类型→通过/拒绝 的 HashMap，首次匹配后 O(1) 命中
- **批量提取** — 最近/最远/随机模式：一次模拟提取 + 一次批量插入 + 一次真实提取 = **每 tick 仅 3 次 API 调用**（与物品数量无关）
- **批量分配** — 轮询模式：预计算每个目标的分配量，批量插入 + 瀑布式重分配剩余。API 调用从 O(物品数×目标数) 降至 O(槽位数×目标数)

#### ⏱️ 空闲退避 (v1.0.6)
- **指数退避** — 管道无物品可传输时，跳过 tick 的间隔逐步增加（2→4→8→16→32 tick 上限）。一旦有物品成功提取，延迟立即减半。可通过服务器配置调整

####  Mekanism 修复 (v1.0.1)
- **假人 Capability** — 防止 Pipez 在 Mekanism 多方块未成形时缓存"无 Capability"的否定结果
- **多方块成形自动重连** — Mekanism 多方块结构组装完成时强制管道网络刷新

####  视觉
- **Pretty Pipez 高清纹理** — 更高分辨率的管道模型，带连接器细节
- **经典纹理资源包** — 原版纹理作为可选资源包内置（在资源包菜单中启用）

### 性能数据

测试环境：6400 物品/tick，轮询分配，Mekanism 创造箱柜，Forge 47.4.20。

| 版本 | 管道 tick 开销 | 对比原版 |
|---------|:---:|:---:|
| 原版 Pipez 1.2.26 | 2600 μs/t | 基准 |
| Pipez CE v1.0.1（架构优化） | 1300 μs/t | **-50%** |
| Pipez CE v1.0.3（过滤器+槽位缓存） | 1060 μs/t | -59% |
| Pipez CE v1.0.5（批量分配） | ~400 μs/t | -85% |
| **Pipez CE v1.0.6（退避+全部优化）** | **109 μs/t** | **-96%** |

*使用 Spark Profiler 和 Observable 测量。较原版提升 24 倍。*

### ⚠️ 测试声明

测试主要使用 **Mekanism 创造箱柜**（单槽位容器）在轮询和最近优先模式下进行。以下场景**尚未**充分测试，实际表现可能不同：

- 流体管道高吞吐量传输
- 能量管道多目标分配
- 气体/化学品管道（Mekanism）
- 大规模多槽位容器（AE2 接口、抽屉等）
- 超大型管道网络（10000+ 连接管道）

**生产环境使用请自行评估风险，发现问题请在 GitHub 提交 Issue。**

### 依赖

- Minecraft 1.20.1, Forge 47.2.0+
- 所有外部模组（Mekanism、JEI、Jade、The One Probe）均为**可选依赖**，模组可独立运行

### 链接

- [GitHub](https://github.com/Shinkarna/pipez)

### 致谢

- **NekoGan** — Pipez Community Edition 作者，性能优化实现
- **Max Henkel** ([henkelmax](https://github.com/henkelmax)) — 原版 Pipez 作者
- **AI 辅助开发** — 优化使用 [Claude Code](https://claude.ai/code) 完成，模型为 **DeepSeek v4 Pro**
- **Pretty Pipez** — 社区高清材质包 ([CurseForge](https://www.curseforge.com/minecraft/texture-packs/pretty-pipez))
- **MekanismPipezFix** — [yuuki1293](https://github.com/yuuki1293/PipezMekanismFix)，Mekanism 多方块兼容修复
- **PipezLagFix** — [AlmanaX21](https://github.com/Almana-mc/PipezLagFix)，指数退避方案

### 许可证

本分支为衍生作品，遵循原版 Pipez 许可证。
