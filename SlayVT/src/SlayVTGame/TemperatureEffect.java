package SlayVTGame;

public final class TemperatureEffect
{
    private TemperatureEffect()
    {
    }

    public static void apply(
        Player player,
        Character target,
        int amount,
        boolean convertCooling)
    {
        if (player == null || target == null)
        {
            throw new IllegalArgumentException(
                "Player and temperature target must not be null.");
        }

        int effectiveAmount = amount;
        if (convertCooling
            && player.getBuffs().hasRedHotForm()
            && amount < 0)
        {
            effectiveAmount = -amount;
        }

        int temperatureDifference =
            target.getBuffs().setTemperature(effectiveAmount);

        if (target.getBuffs().getTemperature() > 0)
        {
            player.getBuffs().markHeatAppliedThisTurn();
        }

        if (temperatureDifference > 0)
        {
            int finalDamage = Buffs.calculateDamage(
                temperatureDifference * 3,
                player.getBuffs(), target.getBuffs());
            target.takeDamage(finalDamage);
        }
    }

    public static void applyEnemy(Enemy enemy, Player player, int amount)
    {
        if (enemy == null || player == null)
        {
            throw new IllegalArgumentException(
                "Enemy and temperature target must not be null.");
        }

        int temperatureDifference =
            player.getBuffs().setTemperature(amount);
        if (temperatureDifference > 0)
        {
            int finalDamage = Buffs.calculateDamage(
                temperatureDifference * 3,
                enemy.getBuffs(), player.getBuffs());
            player.takeDamage(finalDamage);
        }
    }
}
