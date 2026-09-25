package SlayVTGame;

class FrontFormEffect implements CardEffect
{
    private final int triggers;

    FrontFormEffect(int triggers) { this.triggers = triggers; }

    public void apply(Player player, Enemy enemy)
    {
        player.getBuffs().addFrontFormTriggers(triggers);
    }

    public String toString()
    {
        return "The first " + triggers + " temperature reversal(s) each "
            + "turn deal double temperature difference damage.";
    }
}

class DroughtEffect implements CardEffect
{
    private final int percent;

    DroughtEffect(int percent) { this.percent = percent; }

    public void apply(Player player, Enemy enemy)
    {
        player.getBuffs().addDroughtBonus(percent);
    }

    public String toString()
    {
        return "Your Heat damage is increased by " + percent + "%.";
    }
}

class PhaseArmorEffect implements CardEffect
{
    private final int block;

    PhaseArmorEffect(int block) { this.block = block; }

    public void apply(Player player, Enemy enemy)
    {
        player.getBuffs().addPhaseArmor(block);
    }

    public String toString()
    {
        return "Whenever you trigger a temperature reversal, gain "
            + block + " Block.";
    }
}

class HeatExchangerEffect implements CardEffect
{
    public void apply(Player player, Enemy enemy)
    {
        player.getBuffs().enableHeatExchanger();
    }

    public String toString()
    {
        return "The first Cold-to-Heat reversal each turn draws 2 cards. "
            + "The first Heat-to-Cold reversal gains 1 Energy.";
    }
}

class GlacierCollapseEffect implements CardEffect
{
    private final int damage;

    GlacierCollapseEffect(int damage) { this.damage = damage; }

    public boolean requiresEnemyTarget() { return true; }

    public void apply(Player player, Enemy enemy)
    {
        int cold = Math.max(-enemy.getBuffs().getTemperature(), 0);
        new DamageEffect(damage + cold * 3).apply(player, enemy);
        if (cold > 0)
        {
            enemy.getBuffs().setTemperature(0);
        }
    }

    public String toString()
    {
        return "Deal " + damage + " damage, plus 3 damage for each "
            + "Cold on the target. Remove all its Cold.";
    }
}

class CoronalEruptionEffect implements CardEffect
{
    private final int damage;

    CoronalEruptionEffect(int damage) { this.damage = damage; }

    public boolean requiresEnemyTarget() { return true; }

    public void apply(Player player, Enemy enemy)
    {
        new DamageEffect(damage).apply(player, enemy);
        if (enemy.checkAlive())
        {
            enemy.takeUnblockableDamage(player.getBuffs()
                .getHeatEndTurnDamage(enemy.getBuffs().getTemperature()));
        }
    }

    public String toString()
    {
        return "Deal " + damage + " damage. Trigger the target's "
            + "Heat damage immediately.";
    }
}

class ForcedSeasonsEffect implements CardEffect
{
    public boolean requiresEnemyTarget() { return true; }

    public void apply(Player player, Enemy enemy)
    {
        TemperatureEffect.apply(player, enemy,
            -enemy.getBuffs().getTemperature(), false);
    }

    public void apply(BattleContext context, Enemy enemy)
    {
        TemperatureEffect.apply(context, enemy,
            -enemy.getBuffs().getTemperature(), false);
    }

    public String toString()
    {
        return "Reverse the target's temperature, preserving its stacks. "
            + "Trigger a temperature reversal as normal. Exhaust.";
    }
}

class ClimateShelterEffect implements CardEffect
{
    private final int multiplier;

    ClimateShelterEffect(int multiplier) { this.multiplier = multiplier; }

    public void apply(Player player, Enemy enemy)
    {
        player.addBlock(Math.abs(player.getBuffs().getTemperature())
            * multiplier);
    }

    public void apply(BattleContext context, Enemy enemy)
    {
        int total = Math.abs(context.getPlayer().getBuffs()
            .getTemperature());
        for (Enemy target : context.getEnemies())
        {
            if (target.checkAlive())
            {
                total += Math.abs(target.getBuffs().getTemperature());
            }
        }
        context.getPlayer().addBlock(total * multiplier);
    }

    public String toString()
    {
        return "Gain Block equal to " + multiplier + " times the sum "
            + "of absolute temperature stacks on all combatants. Exhaust.";
    }
}

class AllTemperatureEffect implements CardEffect
{
    private final int amount;

    AllTemperatureEffect(int amount) { this.amount = amount; }

    public boolean targetsAllEnemies() { return true; }

    public void apply(Player player, Enemy enemy)
    {
        if (enemy != null && enemy.checkAlive())
        {
            TemperatureEffect.apply(player, enemy, amount, true);
        }
    }

    public void apply(BattleContext context, Enemy ignored)
    {
        for (Enemy enemy : context.getEnemies())
        {
            if (enemy.checkAlive())
            {
                TemperatureEffect.apply(context, enemy, amount, true);
            }
        }
    }

    public String toString()
    {
        return "Apply " + Math.abs(amount) + (amount > 0 ? " Heat" : " Cold")
            + " to all enemies.";
    }
}

class ConditionalStatusEffect implements CardEffect
{
    private final boolean heat;
    private final int turns;

    ConditionalStatusEffect(boolean heat, int turns)
    {
        this.heat = heat;
        this.turns = turns;
    }

    public boolean requiresEnemyTarget() { return true; }

    public void apply(Player player, Enemy enemy)
    {
        int temperature = enemy.getBuffs().getTemperature();
        if (heat && temperature > 0)
        {
            enemy.getBuffs().addWeak(turns);
        }
        else if (!heat && temperature < 0)
        {
            enemy.getBuffs().addVulnerable(turns);
        }
    }

    public String toString()
    {
        return "If the target has " + (heat ? "Heat" : "Cold")
            + ", apply " + turns + (heat ? " Weak." : " Vulnerable.");
    }
}

class NoAcEffect implements CardEffect
{
    public void apply(Player player, Enemy enemy)
    {
        player.getBuffs().addNoAc();
    }

    public String toString()
    {
        return "At the start of each turn, apply 1 Heat to a random enemy.";
    }
}

class ColdAdaptationEffect implements CardEffect
{
    private final int block;

    ColdAdaptationEffect(int block) { this.block = block; }

    public void apply(Player player, Enemy enemy)
    {
        player.getBuffs().addColdAdaptation(block);
    }

    public String toString()
    {
        return "At the end of your turn, gain " + block
            + " Block for each enemy with Cold.";
    }
}
