package SlayVTGame;

import static SlayVTGame.ToolClass.*;
import java.util.ArrayList;

public class FightSystem
{
    private final CombatPiles piles = new CombatPiles();

    public boolean fight(
        Player player, ArrayList<Enemy> enemies, Deck deck)
    {
        piles.initialize(deck);

        player.getBuffs().clear();
        player.setBlock(0);

        for (Enemy enemy : enemies)
        {
            enemy.getBuffs().clear();
            enemy.setBlock(0);
        }

        CombatContext context = new CombatContext(player, enemies, piles);

        int turn = 1;

        while (player.checkAlive() && hasLivingEnemies(enemies))
        {
            println("Turn " + turn);

            player.getBuffs().startTurn();
            player.setBlock(0);
            player.setEnergy(player.getMaxEnergy());

            while (piles.getHand().size() < 5)
            {
                if (piles.getDrawPile().isEmpty()
                    && piles.getDiscardPile().isEmpty())
                {
                    break;
                }

                piles.drawToHand(1);
            }

            while (player.checkAlive() && hasLivingEnemies(enemies))
            {
                printPlayer(player);
                printEnemies(enemies);
                println("");

                int option = askOption(
                    printOptions(piles.mutableHand()),
                    0,
                    piles.getHand().size());

                if (option == 0)
                {
                    break;
                }

                Card chosenCard = piles.getHand().get(option - 1);

                if (chosenCard.getCost() > player.getEnergy())
                {
                    println("You don't have enough Energy.");
                    continue;
                }

                if (chosenCard.targetsAllEnemies())
                {
                    chosenCard.apply(context, (Enemy)null);
                }
                else if (chosenCard.requiresEnemyTarget())
                {
                    ArrayList<Enemy> targets = new ArrayList<Enemy>();

                    for (Enemy enemy : enemies)
                    {
                        if (enemy.checkAlive())
                        {
                            targets.add(enemy);
                        }
                    }

                    int targetOption = askOption(
                        printEnemyOption(targets), 0, targets.size());

                    if (targetOption == 0)
                    {
                        continue;
                    }

                    chosenCard.apply(
                        context, targets.get(targetOption - 1));
                }
                else
                {
                    chosenCard.apply(context, (Enemy)null);
                }

                piles.movePlayedCard(chosenCard);
            }

            piles.discardHand();

            if (!player.checkAlive() || !hasLivingEnemies(enemies))
            {
                break;
            }

            context.resolvePlayerEndOfTurn();

            for (Enemy enemy : enemies)
            {
                if (!player.checkAlive())
                {
                    break;
                }

                if (!enemy.checkAlive())
                {
                    continue;
                }

                enemy.getBuffs().startTurn();
                enemy.setBlock(0);

                int damage = Buffs.calculateDamage(
                    enemy.getDmg(), enemy.getBuffs(), player.getBuffs());

                player.takeDamage(damage);
                enemy.getBuffs().endTurn();
            }

            turn++;
        }

        boolean won = player.checkAlive() && !hasLivingEnemies(enemies);

        piles.clear();

        player.setBlock(0);
        player.getBuffs().clear();

        for (Enemy enemy : enemies)
        {
            enemy.getBuffs().clear();
        }

        return won;
    }

    private boolean hasLivingEnemies(ArrayList<Enemy> enemies)
    {
        for (Enemy enemy : enemies)
        {
            if (enemy.checkAlive())
            {
                return true;
            }
        }

        return false;
    }
}
