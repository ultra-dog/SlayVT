package SlayVTGame;

class HeatStrikeEffect implements CardEffect
{
    private final int damage;
    private final int heat;

    public HeatStrikeEffect(int damage, int heat)
    {
        this.damage = damage;
        this.heat = heat;
    }

    @Override
    public boolean requiresEnemyTarget()
    {
        return true;
    }

    @Override
    public void apply(Player player, Enemy enemy)
    {
        new DamageEffect(damage).apply(player, enemy);
        if (enemy.checkAlive())
        {
            TemperatureEffect.apply(player, enemy, heat, true);
        }
    }

    @Override
    public void apply(BattleContext context, Enemy enemy)
    {
        new DamageEffect(damage).apply(context.getPlayer(), enemy);
        if (enemy.checkAlive())
        {
            TemperatureEffect.apply(context, enemy, heat, true);
        }
    }

    @Override
    public String toString()
    {
        return "Deal " + damage + " damage. Apply " + heat + " Heat.";
    }
}

class ScorchPincerEffect implements CardEffect
{
    private final int multiplier;

    public ScorchPincerEffect(int multiplier)
    {
        this.multiplier = multiplier;
    }

    @Override
    public void apply(Player player, Enemy enemy)
    {
        player.getBuffs().multiplyHeatEndTurnDamage(multiplier);
    }

    @Override
    public String toString()
    {
        return "Multiply Heat's end-of-turn damage by " + multiplier
            + " this turn. Exhaust.";
    }
}

class RedHotFormEffect implements CardEffect
{
    @Override
    public void apply(Player player, Enemy enemy)
    {
        player.getBuffs().enableRedHotForm();
    }

    @Override
    public String toString()
    {
        return "Heat deals 3x damage at 8-15 stacks and 4x damage at "
            + "16+ stacks. Cards that apply Cold to enemies apply the same "
            + "amount of Heat instead.";
    }
}

class OverburnEffect implements CardEffect
{
    private final int hotAmount;
    private final int otherAmount;

    public OverburnEffect(int hotAmount, int otherAmount)
    {
        this.hotAmount = hotAmount;
        this.otherAmount = otherAmount;
    }

    @Override
    public boolean requiresEnemyTarget()
    {
        return true;
    }

    @Override
    public void apply(Player player, Enemy enemy)
    {
        int amount = enemy.getBuffs().getTemperature() > 0
            ? hotAmount : otherAmount;
        TemperatureEffect.apply(player, enemy, amount, true);
    }

    @Override
    public void apply(BattleContext context, Enemy enemy)
    {
        int amount = enemy.getBuffs().getTemperature() > 0
            ? hotAmount : otherAmount;
        TemperatureEffect.apply(context, enemy, amount, true);
    }

    @Override
    public String toString()
    {
        return "If the enemy has Heat, apply " + hotAmount
            + " Heat. Otherwise, apply " + otherAmount + " Heat.";
    }
}

class HiddenScorchEffect implements CardEffect
{
    private final int damage;

    public HiddenScorchEffect(int damage)
    {
        this.damage = damage;
    }

    @Override
    public void apply(Player player, Enemy enemy)
    {
        if (enemy != null)
        {
            new DamageEffect(damage).apply(player, enemy);
        }
    }

    @Override
    public void apply(BattleContext context, Enemy ignored)
    {
        Player player = context.getPlayer();
        for (Enemy enemy : context.getEnemies())
        {
            if (enemy.checkAlive())
            {
                new DamageEffect(damage).apply(player, enemy);
            }
        }

        if (player.getBuffs().wasHeatAppliedThisTurn())
        {
            player.setEnergy(player.getEnergy() + 2);
        }
    }

    @Override
    public boolean targetsAllEnemies()
    {
        return true;
    }

    @Override
    public String toString()
    {
        return "Deal " + damage + " damage to all enemies. If you applied "
            + "Heat this turn, gain 2 Energy.";
    }
}

class FrozenHeartEffect implements CardEffect
{
    private final int energy;

    public FrozenHeartEffect(int energy)
    {
        this.energy = energy;
    }

    @Override
    public void apply(Player player, Enemy enemy)
    {
        TemperatureEffect.apply(player, player, -2, false);
        player.setEnergy(player.getEnergy() + energy);
    }

    @Override
    public void apply(BattleContext context, Enemy enemy)
    {
        TemperatureEffect.apply(context, context.getPlayer(), -2, false);
        context.getPlayer().setEnergy(context.getPlayer().getEnergy()
            + energy);
        context.drawCards(1);
    }

    @Override
    public String toString()
    {
        return "Apply 2 Cold to yourself. Gain " + energy
            + " Energy. Draw 1 card.";
    }
}

class ScatterIceEffect implements CardEffect
{
    @Override
    public boolean requiresEnemyTarget()
    {
        return true;
    }

    @Override
    public void apply(Player player, Enemy enemy)
    {
        // A single target has no other enemies to receive the spread.
    }

    @Override
    public void apply(BattleContext context, Enemy target)
    {
        int cold = Math.max(-target.getBuffs().getTemperature(), 0);
        int spreadAmount = cold / 2;

        if (spreadAmount == 0)
        {
            return;
        }

        for (Enemy enemy : context.getEnemies())
        {
            if (enemy != target && enemy.checkAlive())
            {
                TemperatureEffect.apply(
                    context, enemy, -spreadAmount, true);
            }
        }
    }

    @Override
    public String toString()
    {
        return "Apply half the target's Cold, rounded down, to all other "
            + "enemies. Exhaust.";
    }
}

class VoidFreezeEffect implements CardEffect
{
    private final int cold;
    private final int weak;

    public VoidFreezeEffect(int cold, int weak)
    {
        this.cold = cold;
        this.weak = weak;
    }

    @Override
    public boolean requiresEnemyTarget()
    {
        return true;
    }

    @Override
    public void apply(Player player, Enemy enemy)
    {
        TemperatureEffect.apply(player, enemy, -cold, true);
        if (enemy.checkAlive())
        {
            enemy.getBuffs().addWeak(weak);
        }
    }

    @Override
    public void apply(BattleContext context, Enemy enemy)
    {
        TemperatureEffect.apply(context, enemy, -cold, true);
        if (enemy.checkAlive())
        {
            enemy.getBuffs().addWeak(weak);
        }
    }

    @Override
    public String toString()
    {
        return "Apply " + cold + " Cold and " + weak + " Weak.";
    }
}
