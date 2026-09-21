package SlayVTGame;

public class BuffEffect implements CardEffect
{
    public enum BuffType
    {
        VULNERABLE,
        WEAK,
        TEMPERATURE
    }

    private final BuffType type;
    private final int amount;
    private final boolean targetEnemy;

    public BuffEffect(BuffType type, int amount)
    {
        this(type, amount, true);
    }

    public BuffEffect(BuffType type, int amount, boolean targetEnemy)
    {
        if (type == null)
        {
            throw new IllegalArgumentException("Buff type must not be null.");
        }

        if (type != BuffType.TEMPERATURE && amount < 0)
        {
            throw new IllegalArgumentException(
                "Duration must not be negative.");
        }

        this.type = type;
        this.amount = amount;
        this.targetEnemy = targetEnemy;
    }

    @Override
    public boolean requiresEnemyTarget()
    {
        return targetEnemy;
    }

    @Override
    public void apply(Player player, Enemy enemy)
    {
        if (targetEnemy && enemy == null)
        {
            throw new IllegalArgumentException("An enemy target is required.");
        }

        Character target = targetEnemy ? enemy : player;
        Buffs buffs = target.getBuffs();

        switch (type)
        {
            case VULNERABLE:
                buffs.addVulnerable(amount);
                break;

            case WEAK:
                buffs.addWeak(amount);
                break;

            case TEMPERATURE:
                TemperatureEffect.apply(player, target, amount, true);
                break;
        }
    }

    @Override
    public String toString()
    {
        String target = targetEnemy ? "the enemy" : "yourself";

        switch (type)
        {
            case VULNERABLE:
                return "Apply " + amount + " turn(s) of Vulnerable to "
                    + target + ".";

            case WEAK:
                return "Apply " + amount + " turn(s) of Weak to "
                    + target + ".";

            case TEMPERATURE:
                return "Set temperature to " + amount + " for " + target + ".";

            default:
                throw new IllegalStateException("Unknown buff type.");
        }
    }
}
