package SlayVTGame;

public class DamageEffect implements CardEffect
{
    private final int dmg;
    private final int hitCount;

    public DamageEffect(int dmg)
    {
        this(dmg, 1);
    }

    public DamageEffect(int dmg, int hitCount)
    {
        if (dmg < 0 || hitCount < 1)
        {
            throw new IllegalArgumentException(
                "Damage must be non-negative and hit count must be positive.");
        }

        this.dmg = dmg;
        this.hitCount = hitCount;
    }

    @Override
    public boolean requiresEnemyTarget()
    {
        return true;
    }

    @Override
    public void apply(Player player, Enemy enemy)
    {
        if (enemy == null)
        {
            throw new IllegalArgumentException("An enemy target is required.");
        }

        for (int i = 0;
            i < hitCount && player.checkAlive() && enemy.checkAlive();
            i++)
        {
            int damage = Buffs.calculateDamage(
                dmg, player.getBuffs(), enemy.getBuffs());

            enemy.takeDamage(damage);
        }
    }

    @Override
    public String toString()
    {
        String description = "Deal " + dmg + " damage";

        if (hitCount > 1)
        {
            description += " " + hitCount + " times";
        }

        return description + ".";
    }
}