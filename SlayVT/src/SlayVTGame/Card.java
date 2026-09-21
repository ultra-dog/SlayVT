package SlayVTGame;

import java.util.ArrayList;
import java.util.EnumSet;

public class Card
{
    private final CardLibrary cardType;
    private String name;
    private int cost;
    private String type;
    private CardEffect effect;
    private EnumSet<CardKeyword> keywords;
    private boolean upgraded;

    public Card(CardLibrary cardType)
    {
        if (cardType == null)
        {
            throw new IllegalArgumentException("Card type must not be null.");
        }

        this.cardType = cardType;
        upgraded = false;
        rebuild();
    }

    public String getName()
    {
        return upgraded ? name + "+" : name;
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
        return keywords.contains(CardKeyword.EXHAUST);
    }

    public boolean isRetain()
    {
        return keywords.contains(CardKeyword.RETAIN);
    }

    public boolean isUpgraded()
    {
        return upgraded;
    }

    public boolean canUpgrade()
    {
        return !upgraded && cardType.canUpgrade();
    }

    public boolean upgrade()
    {
        if (!canUpgrade())
        {
            return false;
        }

        upgraded = true;
        rebuild();
        return true;
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
        apply(new BattleContext(player, enemies, new BattlePiles()), enemy);
    }

    public void apply(BattleContext context, Enemy enemy)
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
        BattleContext context = new BattleContext(
            player, enemies, new BattlePiles());

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

    private void rebuild()
    {
        name = cardType.getName();
        cost = cardType.getCost(upgraded);
        type = cardType.getType();
        effect = cardType.createEffect(upgraded);
        keywords = cardType.createKeywords(upgraded);
    }
}
