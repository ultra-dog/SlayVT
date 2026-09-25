# SlayVT 敌人遭遇系统实施计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 在 15 层文字游戏中实现 9 种不重复的普通敌人、2 种低概率精英和第 15 层钟楼 Boss，并让显示的敌人意图与实际行动一致。

**Architecture:** `EnemyMove` 封装共用招式，`Enemy` 保存招式进度及 Boss 阶段；`EnemyRoster` 创建具体敌人，`EnemyEncounterSystem` 管理本局抽取和精英房间概率。`BattleSystem` 只负责回合流程，玩家受到的温度效果补入现有温度结算。

**Tech Stack:** Java 8 标准库、Eclipse 现有目录结构、可直接运行的 `main` 测试；本机可用 `javac --release 8`。

**Spec:** `docs/superpowers/specs/2026-09-25-enemy-encounters-design.md`

## Global Constraints

- 所有 Java 文件放在 `SlayVT/src/SlayVTGame`，包名为 `SlayVTGame`，不增加依赖。
- 保留已有 `Enemy(String, int, int)` 和 `getDmg`／`setDmg`，不破坏现有卡牌及排版测试。
- 同一局每种普通敌人最多出现一次；三个阶段各 3 种，抽空后隐藏普通战选项。
- 精英只在 4–7、10–12 层以 15% 概率提供；有精英时另有非精英选项，每阶段最多选择一场精英。
- 第 9 层宝箱、第 14 层休息点、第 15 层唯一 Boss；其余非精英房间规则和卡牌奖励不变。
- 玩家正温度的基础热伤害阈值与敌人一致；玩家卡牌的进攻性热增幅不放大敌人造成的玩家热伤害。
- 每项任务先写失败测试、确认失败原因、最小实现、完整回归检查，再仅提交该任务文件。不要暂存或提交用户现有的 `SlayVT/.project` 改动；未经请求不推送。

新建的三个测试类各自使用以下断言辅助方法，不依赖外部测试框架：

```java
private static void check(String label, boolean passed)
{
    if (!passed) { throw new AssertionError(label); }
}
```

## File Structure

- 新建 `EnemyMove.java`：攻击、格挡、状态、温度与蓄力的共用招式及意图文字。
- 修改 `Enemy.java`：保存招式、推进回合、保留旧构造方法、处理半血阶段。
- 修改 `BattleSystem.java`、`ToolClass.java`：执行并显示同一个意图。
- 修改 `TemperatureEffect.java`、`Buffs.java`、`BattleContext.java`：敌人向玩家施加温度和玩家回合末热伤害。
- 新建 `EnemyRoster.java`：9 种普通敌人、2 种精英和 Boss 的唯一配置地点。
- 新建 `EnemyEncounterSystem.java`：可注入 `Random` 的房间选项和本局不重复抽取。
- 修改 `Main.java`：用遭遇系统替换占位敌人，保留其他房间入口与奖励流程。
- 新建 `EnemyMoveTest.java`、`EnemyRosterTest.java`、`EnemyEncounterSystemTest.java`；扩展 `TemperatureTest.java`。

## Review Focus

- 旧 `Enemy` 构造方法创建的测试敌人仍应显示并执行固定攻击；任务 1 的兼容性测试覆盖。
- 多段攻击遇到玩家死亡时不应继续打后续段数；任务 1 的战斗测试覆盖。
- 玩家 Red Hot Form 不应提高敌人施加的热伤害；任务 2 的温度测试覆盖。
- Boss 在玩家回合内过半血时，不应暗改已经预告的当前招式；任务 3 的阶段测试覆盖。
- 普通敌人池抽空及精英出现时，房间菜单仍应非空且保留非精英路径；任务 4 的选项测试覆盖。

---

### Task 1: 共用招式、真实意图及旧接口兼容

**Files:** 新建 `EnemyMove.java`、`EnemyMoveTest.java`；修改 `Enemy.java`、`BattleSystem.java`、`ToolClass.java`。

**Interfaces:** 产出 `EnemyMove.attack(int)`、`attackAndBlock(int,int)`、`block(int)`、`attackAndWeak(int,int)`、`multiAttack(int,int)`、`vulnerable(int)`、`charge(int,int)`、`growingAttack(int,int,int)`；`Enemy(String,int,EnemyMove...)`、`String getIntent()`（基础伤害）、`String getIntent(Player)`（当前状态修正后的伤害）、`void takeTurn(Player)`。`ToolClass` 保留原 `printEnemies(ArrayList<Enemy>)`、`printEnemyOption(ArrayList<Enemy>)`，并增加相应的 `Player` 参数重载。后续任务扩展温度招式及 Boss 构造方法。

- [ ] **Step 1: 写失败测试。** 在 `EnemyMoveTest.main` 中断言以下行为，并提供简单 `check(String, boolean)`，失败时抛 `AssertionError`：

```java
Player player = new Player("Tester", 80);
Enemy legacy = new Enemy("Legacy", 20, 5);
check("legacy intent", legacy.getIntent(player).contains("5 damage"));
legacy.takeTurn(player);
check("legacy attack", player.getHp() == 75);
legacy.setDmg(7);
check("legacy setter changes intent", legacy.getIntent(player).contains("7 damage"));

Enemy guard = new Enemy("Guard", 30,
    EnemyMove.attackAndBlock(3, 5), EnemyMove.multiAttack(4, 2));
guard.takeTurn(player);
check("guard gains block", guard.getBlock() == 5);
check("next intent is two hits", guard.getIntent(player).contains("2 hits"));
guard.takeTurn(player);
check("old block clears", guard.getBlock() == 0);

CountingPlayer fragile = new CountingPlayer("Fragile", 3);
Enemy striker = new Enemy("Striker", 20, EnemyMove.multiAttack(4, 2));
striker.takeTurn(fragile);
check("stops after lethal hit", fragile.getHp() == 0);
check("only one hit was applied", fragile.hits == 1);
check("one action advances once", striker.getIntent(fragile).contains("2 hits"));

Player target = new Player("Target", 80);
Enemy weakened = new Enemy("Weakened", 20, EnemyMove.attack(10));
weakened.getBuffs().addWeak(1);
target.getBuffs().addVulnerable(1);
check("intent includes status modifiers",
    weakened.getIntent(target).contains("11 damage"));
weakened.takeTurn(target);
check("executed damage matches intent", target.getHp() == 69);

Player statusTarget = new Player("Status", 80);
Enemy ghost = new Enemy("Ghost", 20,
    EnemyMove.attackAndWeak(3, 1), EnemyMove.vulnerable(2));
ghost.takeTurn(statusTarget);
statusTarget.getBuffs().startTurn();
check("weak lasts through player turn", statusTarget.getBuffs().isWeak());
statusTarget.getBuffs().endTurn();
check("weak expires after player turn", !statusTarget.getBuffs().isWeak());
```

测试文件中加入以下辅助类，以便观察致死后是否仍执行第二段攻击：

```java
private static class CountingPlayer extends Player
{
    int hits;
    CountingPlayer(String name, int hp) { super(name, hp); }
    @Override public void takeDamage(int amount)
    {
        hits++;
        super.takeDamage(amount);
    }
}
```

- [ ] **Step 2: 确认红灯。** 在仓库根目录用 PowerShell 运行 `javac --release 8 -encoding UTF-8 -d SlayVT/bin (Get-ChildItem SlayVT/src/SlayVTGame -Filter *.java).FullName`；预期只因新增类型或方法尚不存在而编译失败。
- [ ] **Step 3: 最小实现。** `EnemyMove` 保存基础伤害、段数、格挡、虚弱、易伤、蓄力提示和可选伤害成长。其工厂方法返回独立对象；`describe` 用现有 `Buffs.calculateDamage` 生成可读意图，`execute` 按“格挡、逐段攻击、施加状态”执行，玩家死亡时停止后续攻击。`Enemy.takeTurn` 集中调用 `buffs.startTurn()`、清除自身旧格挡、执行当前招式、`buffs.endTurn()`、推进索引；旧构造方法包装单个攻击招式，`setDmg` 同步替换该招式。示例核心代码：

```java
public void takeTurn(Player player)
{
    getBuffs().startTurn();
    setBlock(0);
    moves[moveIndex].execute(this, player);
    getBuffs().endTurn();
    moveIndex = (moveIndex + 1) % moves.length;
}
```

`BattleSystem` 的敌人循环调用 `enemy.takeTurn(player)`，不再重复清格挡或直接读 `getDmg()`；`ToolClass` 的两个敌人列表增加带 `Player` 参数的重载，并改用 `enemy.getIntent(player)`。`BattleSystem` 调用新重载；保留旧重载以兼容 `TerminalLayoutTest`，旧重载调用 `enemy.getIntent()` 显示基础伤害。单独预览不推进招式。
- [ ] **Step 4: 确认绿灯与回归。** 重新编译，运行 `java -cp SlayVT/bin SlayVTGame.EnemyMoveTest`，再运行已有 `TemperatureTest`、`CardMechanicsTest`、`ShopSystemTest`、`CardRewardSystemTest`、`TerminalLayoutTest`、`SourceLayoutTest`；每个进程退出码须为 0。
- [ ] **Step 5: 审核并提交。** `git diff --check`、`git diff --stat`，只暂存本任务文件，提交 `Add shared enemy moves and intent execution`。

### Task 2: 敌人对玩家的温度效果

**Files:** 修改 `EnemyMove.java`、`TemperatureEffect.java`、`Buffs.java`、`BattleContext.java`、`BattleSystem.java`、`TemperatureTest.java`。

**Interfaces:** 产出 `EnemyMove.attackAndTemperature(int,int)`、`temperatureCharge(int,int)`、`TemperatureEffect.applyEnemy(Enemy,Player,int)`、`Buffs.getBaseHeatEndTurnDamage(int)`；玩家热伤害由 `BattleContext.resolvePlayerEndOfTurn()` 结算。

- [ ] **Step 1: 写失败测试。** 在 `TemperatureTest` 增加：玩家温度 `-2` 时敌人施加 `+2`，反转伤害为 12 且温度变为 `+2`；敌人虚弱、玩家易伤和格挡影响反转；玩家正温度 `+8` 在回合末造成 16 点无视格挡伤害；即便玩家启用 Red Hot Form，受到的基础热伤害仍是 16。示例断言：

```java
Player heated = new Player("Heated", 80);
Enemy reactor = new Enemy("Reactor", 20, 0);
heated.getBuffs().setTemperature(-2);
TemperatureEffect.applyEnemy(reactor, heated, 2);
check("enemy reversal damage", 68, heated.getHp());
check("enemy reversal temperature", 2,
    heated.getBuffs().getTemperature());
heated.getBuffs().setTemperature(0);
heated.getBuffs().setTemperature(8);
heated.getBuffs().enableRedHotForm();
BattleContext context = new BattleContext(heated,
    new java.util.ArrayList<Enemy>(), new BattlePiles());
context.resolvePlayerEndOfTurn();
check("incoming heat ignores offensive form", 52, heated.getHp());

Player guarded = new Player("Guarded", 80);
guarded.getBuffs().setTemperature(-2);
guarded.getBuffs().addVulnerable(1);
guarded.addBlock(4);
reactor.getBuffs().addWeak(1);
TemperatureEffect.applyEnemy(reactor, guarded, 2);
// floor(12 * 0.75 * 1.5) = 13; four block absorbs four.
check("modified reversal respects block", 71, guarded.getHp());
```

- [ ] **Step 2: 确认红灯。** 运行 Task 1 的编译命令；预期因 `applyEnemy` 尚不存在而失败。
- [ ] **Step 3: 最小实现。** `applyEnemy` 调用玩家的 `setTemperature`，对非零反转差值用 `Buffs.calculateDamage(diff * 3, enemyBuffs, playerBuffs)` 后调用 `player.takeDamage`。把现有热阈值公式提取为 `Buffs.getBaseHeatEndTurnDamage(int)`：热量低于 8 时伤害为热量值，8–15 时为 2 倍，16 及以上为 3 倍。原有 `getHeatEndTurnDamage` 在此基础上继续应用 Red Hot Form 的阈值额外倍率及临时增幅，确保玩家卡牌对敌人的旧数值不变；玩家受到的热伤害只调用基础方法。`resolvePlayerEndOfTurn` 在状态 `endTurn` 前对玩家执行 `takeUnblockableDamage`。若此伤害致死，`BattleSystem` 不进入敌人回合。温度招式通过 `applyEnemy` 执行；若攻击已杀死玩家，不再额外施加温度。
- [ ] **Step 4: 确认绿灯与回归。** 编译并运行 Task 1 所列全部测试，重点检查原有敌人温度卡牌数值没有改变。
- [ ] **Step 5: 审核并提交。** `git diff --check`，只暂存本任务文件，提交 `Apply enemy temperature to player`。

### Task 3: 敌人名单、成长攻击和半血 Boss

**Files:** 新建 `EnemyRoster.java`、`EnemyRosterTest.java`；修改 `Enemy.java`、`EnemyMove.java`。

**Interfaces:** 产出 `EnemyRoster.normalPool(int floor)`，返回新的 `ArrayList<Enemy>`；`earlyElite()`、`lateElite()`、`boss()` 返回新敌人。`Enemy` 新增 `Enemy(String,int,EnemyMove[],EnemyMove[],int)`，最后一个参数为进入第二阶段的生命值阈值。

- [ ] **Step 1: 写失败测试。** `EnemyRosterTest` 对 1、5、10 层各检查恰好 3 个不同名字，对第 9 层检查空普通池；核对主要数值和招式。令 Boss 第一次行动后生命值降到 45，确认当前预告仍是第一阶段蓄力，执行该招后才转为第二阶段蓄力；令熬夜暗影连续行动，攻击意图依次为 7、9、11、13、13。示例：

```java
Enemy boss = EnemyRoster.boss();
Player player = new Player("Tester", 200);
boss.takeTurn(player);                 // First-phase A8.
boss.takeDamage(45);                  // HP becomes 45.
check("current move stays advertised",
    boss.getIntent(player).contains("16"));
boss.takeTurn(player);                 // First-phase charge.
check("next preview starts second phase",
    boss.getIntent(player).contains("18"));
```

- [ ] **Step 2: 确认红灯。** 用 Task 1 的编译命令运行，预期因 `EnemyRoster` 不存在而失败。
- [ ] **Step 3: 最小实现。** `EnemyRoster.normalPool` 对楼层 1–4、5–8、10–13 返回规格中各自的 3 个新实例，其余楼层返回空列表；精英与 Boss 工厂严格按规格中的 HP、招式数值和循环顺序创建。`EnemyMove.growingAttack(7,2,13)` 在一次攻击结束后更新下一次的基础伤害。Boss 的当前阶段招式只在它执行完已显示的动作后检查阈值；首次触发时把索引设为第二阶段第一招，之后不再回退。
- [ ] **Step 4: 确认绿灯与回归。** 编译并运行 `EnemyRosterTest` 及 Task 1–2 的所有测试。
- [ ] **Step 5: 审核并提交。** 检查 12 个名字、数值与规格逐项对应，只暂存本任务文件，提交 `Add enemy roster and bellkeeper boss`。

### Task 4: 本局不重复抽取、精英概率与主流程接线

**Files:** 新建 `EnemyEncounterSystem.java`、`EnemyEncounterSystemTest.java`；修改 `Main.java`。

**Interfaces:** `EnemyEncounterSystem(Random)`、`boolean hasNormal(int floor)`、`Enemy drawNormal(int floor)`、`boolean shouldOfferElite(int floor,boolean previousOffered)`、`Enemy drawElite(int floor)`、`ArrayList<String> roomOptions(int floor,boolean restOffered,boolean shopOffered,boolean eliteOffered)`。`roomOptions` 只处理随机房间楼层，固定楼层仍由 `Main` 的原有分支处理。

- [ ] **Step 1: 写失败测试。** 使用 `Random` 测试替身（`nextInt(bound)` 固定返回 0 或 `bound - 1`）：从 1 层的池连续抽 3 次得到互异名字，第 4 次前 `hasNormal(4)` 为假；第 4 层的房间选项不含 `Monster`，但仍非空；在合格楼层强制 0 时显示 `Elite` 和一个非精英选项，强制最大值时不显示 `Elite`；对 `bound == 100` 分别返回 14、15 验证 15% 边界；第 3、8、13 层不显示精英；选择一次前期精英后，该阶段不能再次提供。示例：

```java
Random alwaysZero = new Random()
{
    @Override public int nextInt(int bound) { return 0; }
};
EnemyEncounterSystem run = new EnemyEncounterSystem(alwaysZero);
java.util.HashSet<String> names = new java.util.HashSet<String>();
for (int i = 0; i < 3; i++)
{
    names.add(run.drawNormal(1).getName());
}
check("three unique early enemies", names.size() == 3);
check("early pool exhausted", !run.hasNormal(4));
check("fallback rooms available",
    !run.roomOptions(4, false, false, false).isEmpty());
```

- [ ] **Step 2: 确认红灯。** 用 Task 1 的编译命令运行，预期因 `EnemyEncounterSystem` 不存在而失败。
- [ ] **Step 3: 最小实现。** 构造时为每段创建一次 `EnemyRoster.normalPool`；`drawNormal` 从当前池按 `Random.nextInt(size)` 移除并返回实例。`shouldOfferElite` 仅在规定楼层、该阶段未选精英、上一层未提供精英时计算 `random.nextInt(100) < 15`。`roomOptions` 先构建非精英候选（按现有休息／商店规则，普通池抽空时不加 `Monster`，始终加 `Event`）；若精英命中，返回 `Elite` 加随机一个非精英候选，否则沿用随机显示 1–2 个非精英候选。`drawElite` 标记所属阶段已选并返回相应精英。`Main` 在第 1 层、普通战、精英战、Boss 战分别使用遭遇系统和 `EnemyRoster.boss()`，原卡牌奖励及其他房间分支不变。
- [ ] **Step 4: 确认绿灯与全套验证。** `javac --release 8 -encoding UTF-8 -d SlayVT/bin (Get-ChildItem SlayVT/src/SlayVTGame -Filter *.java).FullName`；依次运行 `EnemyMoveTest`、`EnemyRosterTest`、`EnemyEncounterSystemTest`、`TemperatureTest`、`CardMechanicsTest`、`ShopSystemTest`、`CardRewardSystemTest`、`TerminalLayoutTest`、`SourceLayoutTest`，每个退出码为 0。再进行一次终端交互游玩，检查精英选项、意图文字、胜利后的卡牌奖励和第 15 层 Boss。
- [ ] **Step 5: 审核并提交。** `git diff --check`、`git status --short`，只暂存 `EnemyEncounterSystem.java`、其测试和 `Main.java`，提交 `Use unique enemies across fifteen floors`。确认 `SlayVT/.project` 仍未暂存；不自动推送。
