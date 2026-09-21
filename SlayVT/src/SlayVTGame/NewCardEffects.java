package SlayVTGame;

class HeatStrikeEffect implements CardEffect
{
    @Override
    public boolean requiresEnemyTarget()
    {
        return true;
    }

    @Override
    public void apply(Player player, Enemy enemy)
    {
        new DamageEffect(8).apply(player, enemy);
        if (enemy.checkAlive())
        {
            TemperatureEffect.apply(player, enemy, 1, true);
        }
    }

    @Override
    public String toString()
    {
        return "Deal 8 damage. Apply 1 Heat.";
    }
}

class ScorchPincerEffect implements CardEffect
{
    @Override
    public void apply(Player player, Enemy enemy)
    {
        player.getBuffs().multiplyHeatEndTurnDamage(2);
    }

    @Override
    public String toString()
    {
        return "Double Heat's end-of-turn damage this turn. Exhaust.";
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
    @Override
    public boolean requiresEnemyTarget()
    {
        return true;
    }

    @Override
    public void apply(Player player, Enemy enemy)
    {
        int amount = enemy.getBuffs().getTemperature() > 0 ? 4 : 2;
        TemperatureEffect.apply(player, enemy, amount, true);
    }

    @Override
    public String toString()
    {
        return "If the enemy has Heat, apply 4 Heat. Otherwise, apply 2 Heat.";
    }
}

class HiddenScorchEffect implements CardEffect
{
    @Override
    public void apply(Player player, Enemy enemy)
    {
        if (enemy != null)
        {
            new DamageEffect(12).apply(player, enemy);
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
                new DamageEffect(12).apply(player, enemy);
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
        return "Deal 12 damage to all enemies. If you applied Heat this "
            + "turn, gain 2 Energy.";
    }
}

class FrozenHeartEffect implements CardEffect
{
    @Override
    public void apply(Player player, Enemy enemy)
    {
        TemperatureEffect.apply(player, player, -2, false);
        player.setEnergy(player.getEnergy() + 2);
    }

    @Override
    public void apply(BattleContext context, Enemy enemy)
    {
        apply(context.getPlayer(), enemy);
        context.drawCards(1);
    }

    @Override
    public String toString()
    {
        return "Apply 2 Cold to yourself. Gain 2 Energy. Draw 1 card.";
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
                    context.getPlayer(), enemy, -spreadAmount, true);
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
    @Override
    public boolean requiresEnemyTarget()
    {
        return true;
    }

    @Override
    public void apply(Player player, Enemy enemy)
    {
        TemperatureEffect.apply(player, enemy, -3, true);
        if (enemy.checkAlive())
        {
            enemy.getBuffs().addWeak(1);
        }
    }

    @Override
    public String toString()
    {
        return "Apply 3 Cold and 1 Weak.";
    }
}
