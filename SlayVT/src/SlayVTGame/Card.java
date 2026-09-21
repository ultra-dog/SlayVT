package SlayVTGame;

import java.util.ArrayList;

public class Card
{
    private String name;
    private int cost;
    private String type;
    private CardEffect effect;
    private boolean exhaust;

    public Card(CardLibrary cardType)
    {
        name = cardType.getName();
        cost = cardType.getCost();
        type = cardType.getType();
        effect = cardType.createEffect();
        exhaust = cardType.isExhaust();
    }

    public String getName()
    {
        return name;
    }

    public int getCost()
    {
        return cost;
    }

    public void setCost(int cost)
    {
        if (cost < 0)
        {
            throw new IllegalArgumentException("Cost must not be negative.");
        }

        this.cost = cost;
    }

    public String getType()
    {
        return type;
    }

    public String getEffect()
    {
        return effect.toString();
    }

    public boolean isExhaust()
    {
        return exhaust;
    }

    public boolean requiresEnemyTarget()
    {
        return effect.requiresEnemyTarget();
    }

    public boolean targetsAllEnemies()
    {
        return effect.targetsAllEnemies();
    }

    public void apply(Player player, Enemy enemy)
    {
        ArrayList<Enemy> enemies = new ArrayList<Enemy>();
        if (enemy != null)
        {
            enemies.add(enemy);
        }
        apply(new CombatContext(player, enemies, new CombatPiles()), enemy);
    }

    public void apply(CombatContext context, Enemy enemy)
    {
        Player player = context.getPlayer();

        if (requiresEnemyTarget()
            && (enemy == null || !enemy.checkAlive()))
        {
            throw new IllegalArgumentException(
                "A living enemy target is required.");
        }

        if (player.getEnergy() < cost)
        {
            throw new IllegalStateException("Not enough energy.");
        }

        player.spendEnergy(cost);
        effect.apply(context, enemy);
    }

    // Explicit multi-target application; self effects execute only once.
    public void apply(Player player, ArrayList<Enemy> enemies)
    {
        CombatContext context = new CombatContext(
            player, enemies, new CombatPiles());

        if (targetsAllEnemies())
        {
            apply(context, (Enemy)null);
            return;
        }

        if (!requiresEnemyTarget())
        {
            apply(context, (Enemy)null);
            return;
        }

        if (enemies == null)
        {
            throw new IllegalArgumentException("Enemies must not be null.");
        }

        ArrayList<Enemy> targets = new ArrayList<Enemy>();

        for (Enemy enemy : enemies)
        {
            if (enemy != null && enemy.checkAlive())
            {
                targets.add(enemy);
            }
        }

        if (targets.isEmpty())
        {
            throw new IllegalArgumentException(
                "At least one living enemy target is required.");
        }

        if (player.getEnergy() < cost)
        {
            throw new IllegalStateException("Not enough energy.");
        }

        player.spendEnergy(cost);

        for (Enemy enemy : targets)
        {
            if (!player.checkAlive())
            {
                break;
            }

            if (enemy.checkAlive())
            {
                effect.apply(context, enemy);
            }
        }
    }
}
