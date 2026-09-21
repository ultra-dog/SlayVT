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
        boolean convertCoolingWithRedHotForm)
    {
        if (player == null || target == null)
        {
            throw new IllegalArgumentException(
                "Player and temperature target must not be null.");
        }

        int effectiveAmount = amount;
        if (convertCoolingWithRedHotForm
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
}
