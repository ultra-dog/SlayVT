package SlayVTGame;

import java.util.ArrayList;
import java.util.List;

import static SlayVTGame.ToolClass.println;

public class EnemyMove
{
    private int damage;
    private final int hits;
    private final int block;
    private final int weakTurns;
    private final int vulnerableTurns;
    private final int nextDamage;
    private final int growth;
    private final int maxDamage;

    private EnemyMove(int damage, int hits, int block, int weakTurns,
        int vulnerableTurns, int nextDamage, int growth, int maxDamage)
    {
        this.damage = damage;
        this.hits = hits;
        this.block = block;
        this.weakTurns = weakTurns;
        this.vulnerableTurns = vulnerableTurns;
        this.nextDamage = nextDamage;
        this.growth = growth;
        this.maxDamage = maxDamage;
    }

    public static EnemyMove attack(int damage)
    {
        return new EnemyMove(damage, 1, 0, 0, 0, 0, 0, damage);
    }

    public static EnemyMove attackAndBlock(int damage, int block)
    {
        return new EnemyMove(damage, 1, block, 0, 0, 0, 0, damage);
    }

    public static EnemyMove block(int block)
    {
        return new EnemyMove(0, 0, block, 0, 0, 0, 0, 0);
    }

    public static EnemyMove attackAndWeak(int damage, int turns)
    {
        return new EnemyMove(damage, 1, 0, turns, 0, 0, 0, damage);
    }

    public static EnemyMove multiAttack(int damage, int hits)
    {
        return new EnemyMove(damage, hits, 0, 0, 0, 0, 0, damage);
    }

    public static EnemyMove vulnerable(int turns)
    {
        return new EnemyMove(0, 0, 0, 0, turns, 0, 0, 0);
    }

    public static EnemyMove charge(int block, int nextDamage)
    {
        return new EnemyMove(0, 0, block, 0, 0, nextDamage, 0, 0);
    }

    public static EnemyMove growingAttack(int start, int step, int cap)
    {
        return new EnemyMove(start, 1, 0, 0, 0, 0, step, cap);
    }

    public int getBaseDamage()
    {
        return damage;
    }

    public String describe(Enemy enemy, Player player)
    {
        List<String> parts = new ArrayList<String>();
        if (block > 0)
        {
            parts.add(block + " Block");
        }
        if (hits > 0)
        {
            int shownDamage = player == null ? damage
                : Buffs.calculateDamage(damage, enemy.getBuffs(),
                    player.getBuffs());
            parts.add(shownDamage + " damage"
                + (hits > 1 ? " x " + hits + " (" + hits + " hits)" : ""));
        }
        if (weakTurns > 0)
        {
            parts.add("Weak " + weakTurns);
        }
        if (vulnerableTurns > 0)
        {
            parts.add("Vulnerable " + vulnerableTurns);
        }
        if (nextDamage > 0)
        {
            parts.add("Charge: " + nextDamage + " damage next turn");
        }
        return join(parts);
    }

    public void execute(Enemy enemy, Player player)
    {
        if (block > 0)
        {
            enemy.addBlock(block);
            println("  " + enemy.getName() + " gains " + block + " Block.");
        }
        for (int hit = 0; hit < hits && player.checkAlive(); hit++)
        {
            int finalDamage = Buffs.calculateDamage(damage,
                enemy.getBuffs(), player.getBuffs());
            println("  " + enemy.getName() + " attacks for "
                + finalDamage + " damage.");
            player.takeDamage(finalDamage);
        }
        if (player.checkAlive() && weakTurns > 0)
        {
            player.getBuffs().addWeak(weakTurns);
            println("  " + enemy.getName() + " applies Weak "
                + weakTurns + ".");
        }
        if (player.checkAlive() && vulnerableTurns > 0)
        {
            player.getBuffs().addVulnerable(vulnerableTurns);
            println("  " + enemy.getName() + " applies Vulnerable "
                + vulnerableTurns + ".");
        }
        if (nextDamage > 0)
        {
            println("  " + enemy.getName() + " charges for "
                + nextDamage + " damage next turn.");
        }
        if (growth > 0 && hits > 0)
        {
            damage = Math.min(maxDamage, damage + growth);
        }
    }

    private String join(List<String> parts)
    {
        StringBuilder result = new StringBuilder();
        for (String part : parts)
        {
            if (result.length() > 0)
            {
                result.append(" + ");
            }
            result.append(part);
        }
        return result.toString();
    }
}
