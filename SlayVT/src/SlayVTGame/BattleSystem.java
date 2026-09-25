package SlayVTGame;

import static SlayVTGame.ToolClass.*;
import java.util.ArrayList;

public class BattleSystem
{
    private final BattlePiles piles = new BattlePiles();

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

        BattleContext context = new BattleContext(player, enemies, piles);

        int turn = 1;

        while (player.checkAlive() && hasLivingEnemies(enemies))
        {
            println(title("TURN " + turn));

            player.getBuffs().startTurn();
            player.setBlock(0);
            player.setEnergy(player.getMaxEnergy());
            context.resolvePlayerTurnStart();

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
                printEnemies(enemies, player);

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
                    println("\nNot enough Energy for "
                        + chosenCard.getName() + ".");
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
                        printEnemyOption(targets, player), 0,
                        targets.size());
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

            if (!player.checkAlive() || !hasLivingEnemies(enemies))
            {
                break;
            }

            println("\nENEMY TURN");

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

                enemy.takeTurn(player);
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
