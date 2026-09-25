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
        apply(null, player, target, amount, convertCooling);
    }

    public static void apply(
        BattleContext context,
        Character target,
        int amount,
        boolean convertCooling)
    {
        apply(context, context.getPlayer(), target, amount,
            convertCooling);
    }

    private static void apply(
        BattleContext context,
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

        int previousTemperature = target.getBuffs().getTemperature();
        int temperatureDifference =
            target.getBuffs().setTemperature(effectiveAmount);

        if (target.getBuffs().getTemperature() > 0)
        {
            player.getBuffs().markHeatAppliedThisTurn();
        }

        if (temperatureDifference > 0)
        {
            Buffs playerBuffs = player.getBuffs();
            int multiplier = playerBuffs.useFrontFormTrigger() ? 2 : 1;
            int finalDamage = Buffs.calculateDamage(
                temperatureDifference * 3 * multiplier,
                playerBuffs, target.getBuffs());
            target.takeDamage(finalDamage);
            player.addBlock(playerBuffs.getPhaseArmorBlock());

            if (playerBuffs.useHeatExchanger(previousTemperature < 0))
            {
                if (previousTemperature < 0)
                {
                    if (context != null)
                    {
                        context.drawCards(2);
                    }
                }
                else
                {
                    player.setEnergy(player.getEnergy() + 1);
                }
            }
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
