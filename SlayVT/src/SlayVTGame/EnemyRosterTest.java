package SlayVTGame;

import java.util.ArrayList;
import java.util.HashSet;

public class EnemyRosterTest
{
    private static int checks;

    public static void main(String[] args)
    {
        HashSet<String> names = new HashSet<String>();
        for (int floor : new int[] {1, 5, 10})
        {
            ArrayList<Enemy> pool = EnemyRoster.normalPool(floor);
            check("three normal enemies on floor " + floor,
                pool.size() == 3);
            for (Enemy enemy : pool)
            {
                names.add(enemy.getName());
            }
        }
        check("nine distinct normal names", names.size() == 9);
        check("chest floor has no normal pool",
            EnemyRoster.normalPool(9).isEmpty());
        check("final rest floor has no normal pool",
            EnemyRoster.normalPool(14).isEmpty());
        check("early elite is stronger",
            EnemyRoster.earlyElite().getMaxHp() == 50);
        check("late elite is stronger",
            EnemyRoster.lateElite().getMaxHp() == 54);

        Player target = new Player("Tester", 200);
        Enemy boss = EnemyRoster.boss();
        check("boss starts at 90 HP", boss.getMaxHp() == 90);
        check("first boss attack is previewed",
            boss.getIntent(target).contains("8 damage"));
        boss.takeTurn(target);
        boss.takeDamage(45);
        check("current move remains the old charge",
            boss.getIntent(target).contains("16 damage"));
        check("boss clearly previews the bell",
            boss.getIntent(target).contains("Bell"));
        boss.takeTurn(target);
        check("next preview enters stronger phase",
            boss.getIntent(target).contains("18 damage"));
        check("boss charge grants block", boss.getBlock() == 8);

        Enemy shade = find(
            EnemyRoster.normalPool(10), "All-Nighter Shade");
        check("shade starts at seven damage",
            shade.getIntent(target).contains("7 damage"));
        shade.takeTurn(target);
        check("shade grows by two damage",
            shade.getIntent(target).contains("9 damage"));

        System.out.println("PASS: " + checks + " enemy roster checks.");
    }

    private static Enemy find(ArrayList<Enemy> pool, String name)
    {
        for (Enemy enemy : pool)
        {
            if (enemy.getName().equals(name))
            {
                return enemy;
            }
        }
        throw new AssertionError("Missing enemy: " + name);
    }

    private static void check(String label, boolean passed)
    {
        checks++;
        if (!passed)
        {
            throw new AssertionError(label);
        }
    }
}
