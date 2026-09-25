package SlayVTGame;

/** Run with: java -cp <compiled-classes> SlayVTGame.TemperatureTest */
public class TemperatureTest
{
    private static int checks;
    private static int failures;

    public static void main(String[] args)
    {
        // Initial temperature, applied temperature, final temperature, difference.
        int[][] transitions = {
            {0, 2, 2, 0}, {0, -2, -2, 0}, {0, 0, 0, 0},
            {3, 2, 5, 0}, {-3, -2, -5, 0},
            {3, -2, -2, 5}, {-3, 2, 2, 5},
            {2, -2, -2, 4}, {-2, 2, 2, 4},
            {3, 0, 0, 0}, {-3, 0, 0, 0}
        };

        for (int[] transition : transitions)
        {
            int initial = transition[0];
            int applied = transition[1];
            String label = initial + " -> apply " + applied;
            Buffs buffs = new Buffs();
            buffs.setTemperature(initial);
            check(label + " difference", transition[3],
                buffs.setTemperature(applied));
            check(label + " temperature", transition[2],
                buffs.getTemperature());

            Player player = new Player("Player", 100);
            Enemy enemy = new Enemy("Enemy", 100, 0);
            enemy.getBuffs().setTemperature(initial);
            applyTemperature(player, enemy, applied);
            check(label + " enemy HP", 100 - transition[3] * 3,
                enemy.getHp());
            check(label + " effect temperature", transition[2],
                enemy.getBuffs().getTemperature());
            check(label + " player HP", 100, player.getHp());
        }

        Player player = new Player("Player", 100);
        Enemy enemy = new Enemy("Enemy", 100, 0);
        applyTemperature(player, enemy, 2);
        applyTemperature(player, enemy, 2);
        applyTemperature(player, enemy, -2);
        check("Stacked heat to cold deals 18 damage", 82, enemy.getHp());
        applyTemperature(player, enemy, -2);
        applyTemperature(player, enemy, 2);
        check("Stacked cold to heat deals another 18 damage", 64,
            enemy.getHp());
        applyTemperature(player, enemy, 0);
        applyTemperature(player, enemy, -2);
        check("Reset to zero prevents shock on reapplication", 64,
            enemy.getHp());
        enemy.getBuffs().clear();
        applyTemperature(player, enemy, 2);
        check("Cleared buffs prevent shock on reapplication", 64,
            enemy.getHp());

        enemy = new Enemy("Enemy", 100, 0);
        enemy.getBuffs().setTemperature(3);
        enemy.addBlock(4);
        player.getBuffs().addWeak(1);
        enemy.getBuffs().addVulnerable(1);
        applyTemperature(player, enemy, -2);
        // floor(5 * 3 * 0.75 * 1.5) = 16 damage; block absorbs 4.
        check("Shock uses existing damage modifiers and block", 88,
            enemy.getHp());
        check("Shock consumes block", 0, enemy.getBlock());

        player = new Player("Player", 100);
        enemy = new Enemy("Enemy", 100, 0);
        new Card(CardLibrary.BURN).apply(player, enemy);
        check("Burn from zero causes no damage", 100, enemy.getHp());
        new Card(CardLibrary.BREEZE).apply(player, enemy);
        check("Breeze after Burn deals 12 damage", 88, enemy.getHp());
        check("Temperature cards spend their normal energy", 1,
            player.getEnergy());

        Player heated = new Player("Heated", 80);
        Enemy reactor = new Enemy("Reactor", 20, 0);
        heated.getBuffs().setTemperature(-2);
        TemperatureEffect.applyEnemy(reactor, heated, 2);
        check("Enemy reversal damage", 68, heated.getHp());
        check("Enemy reversal temperature", 2,
            heated.getBuffs().getTemperature());
        heated.getBuffs().setTemperature(0);
        heated.getBuffs().setTemperature(8);
        heated.getBuffs().enableRedHotForm();
        BattleContext context = new BattleContext(heated,
            new java.util.ArrayList<Enemy>(), new BattlePiles());
        context.resolvePlayerEndOfTurn();
        check("Incoming heat ignores offensive form", 52, heated.getHp());

        Player guarded = new Player("Guarded", 80);
        guarded.getBuffs().setTemperature(-2);
        guarded.getBuffs().addVulnerable(1);
        guarded.addBlock(4);
        reactor.getBuffs().addWeak(1);
        TemperatureEffect.applyEnemy(reactor, guarded, 2);
        check("Modified reversal respects block", 71, guarded.getHp());
        check("Reversal consumes block", 0, guarded.getBlock());

        Player veryHot = new Player("Very Hot", 100);
        veryHot.getBuffs().setTemperature(16);
        veryHot.addBlock(20);
        new BattleContext(veryHot, new java.util.ArrayList<Enemy>(),
            new BattlePiles()).resolvePlayerEndOfTurn();
        check("High heat has same threshold as monsters", 52,
            veryHot.getHp());
        check("Heat bypasses block", 20, veryHot.getBlock());

        Player cold = new Player("Cold", 80);
        cold.getBuffs().setTemperature(-3);
        new BattleContext(cold, new java.util.ArrayList<Enemy>(),
            new BattlePiles()).resolvePlayerEndOfTurn();
        check("Cold has no periodic damage", 80, cold.getHp());

        Player moveTarget = new Player("Move Target", 80);
        new Enemy("Frost", 20,
            EnemyMove.attackAndTemperature(6, -2)).takeTurn(moveTarget);
        check("Attack with cold damages player", 74, moveTarget.getHp());
        check("Attack with cold changes temperature", -2,
            moveTarget.getBuffs().getTemperature());
        Player chargeTarget = new Player("Charge Target", 80);
        new Enemy("Core", 20,
            EnemyMove.temperatureCharge(1, 15)).takeTurn(chargeTarget);
        check("Temperature charge does not attack", 80,
            chargeTarget.getHp());
        check("Temperature charge applies heat", 1,
            chargeTarget.getBuffs().getTemperature());

        if (failures > 0)
        {
            throw new AssertionError(failures + " of " + checks
                + " temperature checks failed.");
        }
        System.out.println("PASS: " + checks + " temperature checks.");
    }

    private static void applyTemperature(Player player, Enemy enemy, int amount)
    {
        new BuffEffect(BuffEffect.BuffType.TEMPERATURE, amount)
            .apply(player, enemy);
    }

    private static void check(String label, int expected, int actual)
    {
        checks++;
        if (expected != actual)
        {
            failures++;
            System.err.println(label + ": expected " + expected
                + ", got " + actual);
        }
    }
}
