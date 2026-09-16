package SlayVTGame;

public interface CardEffect
{
    void apply(Player player, Enemy enemy);

    // Self-targeted effects may receive null as the enemy.
    default boolean requiresEnemyTarget()
    {
        return false;
    }
}