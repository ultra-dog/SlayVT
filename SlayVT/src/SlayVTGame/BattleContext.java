package SlayVTGame;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BattleContext
{
    private final Player player;
    private final ArrayList<Enemy> enemies;
    private final BattlePiles piles;

    public BattleContext(
        Player player, ArrayList<Enemy> enemies, BattlePiles piles)
    {
        if (player == null || enemies == null || piles == null)
        {
            throw new IllegalArgumentException(
                "Combat context values must not be null.");
        }

        this.player = player;
        this.enemies = enemies;
        this.piles = piles;
    }

    public Player getPlayer()
    {
        return player;
    }

    public List<Enemy> getEnemies()
    {
        return Collections.unmodifiableList(enemies);
    }

    public BattlePiles getPiles()
    {
        return piles;
    }

    public void drawCards(int amount)
    {
        piles.drawToHand(amount);
    }

    public void resolvePlayerEndOfTurn()
    {
        for (Enemy enemy : enemies)
        {
            if (!enemy.checkAlive())
            {
                continue;
            }

            int heat = Math.max(enemy.getBuffs().getTemperature(), 0);
            int damage = player.getBuffs().getHeatEndTurnDamage(heat);
            enemy.takeUnblockableDamage(damage);
        }

        int playerHeat = Math.max(player.getBuffs().getTemperature(), 0);
        int playerHeatDamage =
            Buffs.getBaseHeatEndTurnDamage(playerHeat);
        player.takeUnblockableDamage(playerHeatDamage);

        player.getBuffs().endTurn();
    }
}
