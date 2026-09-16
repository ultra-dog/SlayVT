package SlayVTGame;

import static SlayVTGame.ToolClass.*;
import java.util.ArrayList;

public class FightSystem
{
    private final ArrayList<Card> hand = new ArrayList<Card>();
    private final ArrayList<Card> drawPile = new ArrayList<Card>();
    private final ArrayList<Card> discardPile = new ArrayList<Card>();

    public boolean fight(
        Player player, ArrayList<Enemy> enemies, Deck deck)
    {
        hand.clear();
        drawPile.clear();
        discardPile.clear();

        player.getBuffs().clear();
        player.setBlock(0);

        for (Enemy enemy : enemies)
        {
            enemy.getBuffs().clear();
            enemy.setBlock(0);
        }

        for (Card card : deck.getDeck())
        {
            drawPile.add(card);
        }

        int turn = 1;

        while (player.checkAlive() && hasLivingEnemies(enemies))
        {
            println("Turn " + turn);

            player.getBuffs().startTurn();
            player.setBlock(0);
            player.setEnergy(player.getMaxEnergy());

            while (hand.size() < 5)
            {
                if (drawPile.isEmpty() && discardPile.isEmpty())
                {
                    break;
                }

                drawCardFromPile(drawPile, hand, discardPile);
            }

            while (player.checkAlive() && hasLivingEnemies(enemies))
            {
                printPlayer(player);
                println("Your statuses: " + player.getBuffs());

                printEnemies(enemies);

                for (Enemy enemy : enemies)
                {
                    if (enemy.checkAlive())
                    {
                        println(enemy.getName() + " statuses: "
                            + enemy.getBuffs());
                    }
                }

                println("");

                int option = askOption(printOptions(hand), 0, hand.size());

                if (option == 0)
                {
                    break;
                }

                Card chosenCard = hand.get(option - 1);

                if (chosenCard.getCost() > player.getEnergy())
                {
                    println("You don't have enough Energy.");
                    continue;
                }

                if (chosenCard.requiresEnemyTarget())
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

                    chosenCard.apply(player, targets.get(targetOption - 1));
                }
                else
                {
                    chosenCard.apply(player, (Enemy)null);
                }

                discardPile.add(chosenCard);
                hand.remove(option - 1);
            }

            discardPile.addAll(hand);
            hand.clear();

            if (!player.checkAlive() || !hasLivingEnemies(enemies))
            {
                break;
            }

            player.getBuffs().endTurn();

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

        hand.clear();
        drawPile.clear();
        discardPile.clear();

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