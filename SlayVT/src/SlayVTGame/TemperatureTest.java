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
