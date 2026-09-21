package SlayVTGame;

public interface CardEffect
{
    void apply(Player player, Enemy enemy);

    default void apply(CombatContext context, Enemy enemy)
    {
        apply(context.getPlayer(), enemy);
    }

    // Self-targeted effects may receive null as the enemy.
    default boolean requiresEnemyTarget()
    {
        return false;
    }

    default boolean targetsAllEnemies()
    {
        return false;
    }
}
