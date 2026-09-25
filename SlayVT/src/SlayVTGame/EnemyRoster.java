package SlayVTGame;

import java.util.ArrayList;

public final class EnemyRoster
{
    private EnemyRoster()
    {
    }

    public static ArrayList<Enemy> normalPool(int floor)
    {
        ArrayList<Enemy> pool = new ArrayList<Enemy>();
        if (floor >= 1 && floor <= 4)
        {
            pool.add(new Enemy("Lost Freshman", 16,
                EnemyMove.attack(5), EnemyMove.attackAndBlock(3, 5)));
            pool.add(new Enemy("Dining Queue Ghost", 18,
                EnemyMove.attack(5), EnemyMove.attackAndWeak(3, 1)));
            pool.add(new Enemy("Library Wisp", 18,
                EnemyMove.attack(5), EnemyMove.block(6)));
        }
        else if (floor >= 5 && floor <= 8)
        {
            pool.add(new Enemy("Lab Spark", 24,
                EnemyMove.charge(0, 11), EnemyMove.attack(11)));
            pool.add(new Enemy("Drillfield Crow", 24,
                EnemyMove.multiAttack(4, 2), EnemyMove.attack(7)));
            pool.add(new Enemy("Lecture Hall Frost", 26,
                EnemyMove.attackAndTemperature(6, -2),
                EnemyMove.attack(8)));
        }
        else if (floor >= 10 && floor <= 13)
        {
            pool.add(new Enemy("Construction Golem", 34,
                EnemyMove.block(10), EnemyMove.attack(12)));
            pool.add(new Enemy("All-Nighter Shade", 32,
                EnemyMove.growingAttack(7, 2, 13)));
            pool.add(new Enemy("Exam Mimic", 32,
                EnemyMove.vulnerable(2), EnemyMove.attack(12)));
        }
        return pool;
    }

    public static Enemy earlyElite()
    {
        return new Enemy("Hokie Stone Guardian", 50,
            EnemyMove.block(10), EnemyMove.attack(13),
            EnemyMove.multiAttack(7, 2));
    }

    public static Enemy lateElite()
    {
        return new Enemy("Overloaded Reactor", 54,
            EnemyMove.temperatureCharge(1, 15),
            EnemyMove.attack(15), EnemyMove.attack(8));
    }

    public static Enemy boss()
    {
        EnemyMove[] firstPhase = {
            EnemyMove.attack(8), EnemyMove.bellCharge(8, 16),
            EnemyMove.attack(16)
        };
        EnemyMove[] secondPhase = {
            EnemyMove.bellCharge(8, 18), EnemyMove.attack(18)
        };
        return new Enemy("Burruss Bellkeeper", 90,
            firstPhase, secondPhase, 45);
    }
}
