package SlayVTGame;

import java.util.*;

public class Main
{
    private static Player player;
    private static Deck deck;
    private static ArrayList<Enemy> enemies = new ArrayList<Enemy>();
    private static ArrayList<Card> hand = new ArrayList<Card>();
    private static ArrayList<Card> drawPile = new ArrayList<Card>();
    private static ArrayList<Card> discardPile = new ArrayList<Card>();

    public static void main(String[] args)
    {
        Scanner input = new Scanner(System.in);
        println("Please enter your name: ");
        String name = input.nextLine();
        println("Please eneter number to choose your character: 1: zhanshige");
        int characterNum = input.nextInt();
        switch (characterNum)
        {
            case 1:
                player = new Player(name, 80);
                break;
            default:
                player = new Player(name, 80);
                break;
        }
        deck = new Deck(1);
        for(Card card: deck.getDeck()) {
            drawPile.add(card);
        }

        // Game Start-----------------------------------------------------------
        enemies.add(new Enemy("Enemy 1", 30, 10));
        int turn = 1;
        while (player.checkAlive() && enemies.get(0).checkAlive())
        {
            for (int i = 0; i < 5; i++) {
                drawCardFromPile(drawPile, hand, discardPile);
            }
            println("Turn " + turn);
            do
            {
                printPlayer(player);
                printEnemies(enemies);
                println("");
                printOptions(hand);
                Scanner option = new Scanner(System.in);
                int opt = option.nextInt();
                if (opt == 0) // player choose end turn
                {
                    break;
                }
                Card chosenCard = hand.get(opt - 1);
                if (chosenCard.getCost() > player.getEnergy())
                {
                    println("You don't have enough Energy.");
                    continue;
                }
                if (chosenCard.getType().equals("Attack"))
                {
                    printEnemyOption(enemies);
                    int enemyopt = option.nextInt();
                    if (enemyopt == 0)
                    {
                        continue;
                    }
                    Enemy chosenEnemy = enemies.get(enemyopt - 1);
                    chosenCard.apply(player, chosenEnemy);
                }
                else if (chosenCard.getType().equals("Skill"))
                {
                    chosenCard.apply(player, enemies);
                }
                discardPile.add(chosenCard);
                hand.remove(opt - 1);
            }
            while (enemies.get(0).checkAlive());
            // Turn ends
            for (int i = 0; i < hand.size(); i++) {
                discardPile.add(hand.get(i));
            }
            hand.clear();
            player.setEnergy(player.getMaxEnergy());
            for (int i = 0; i < enemies.size(); i++)
            {
                Enemy temp = enemies.get(i);
                player.takeDamage(temp.getDmg());
                temp.setBlock(0);
            }
            player.setBlock(0);
            turn++;
        }
        if (!player.checkAlive()) {
            println("Lost");
        } else {
            println("Won");
        }
    }

    public static void print(String txt)
    {
        System.out.print(txt);
    }


    public static void println(String txt)
    {
        System.out.println(txt);
    }


    public static void drawCardFromPile(ArrayList<Card> drawPile, ArrayList<Card> hand, ArrayList<Card> discardPile)
    {
        if (drawPile.size() <= 0) {
            drawCardFromDiscard(drawPile, discardPile);
        }
        int randindex = (int)(Math.random()*drawPile.size());
        hand.add(drawPile.get(randindex));
        drawPile.remove(randindex);
    }
    
    public static void drawCardFromDiscard(ArrayList<Card> drawPile, ArrayList<Card> discardPile)
    {
        if (discardPile.size() > 0) {
            for (int i = 0; i < discardPile.size(); i++) {
                drawPile.add(discardPile.get(i));
            }
            discardPile.clear();
        } else {
            println("You don't have enough card to draw.");
        }
    }


    public static void printPlayer(Player player)
    {
        print(
            player.getName() + " HP: " + player.getHp() + "/"
                + player.getMaxHp());
        if (player.getBlock() > 0)
        {
            print(" Block: " + player.getBlock());
        }
        println(
            " Energy: (" + player.getEnergy() + "/" + player.getMaxEnergy()
                + ")");
    }


    public static void printEnemies(ArrayList<Enemy> enemies)
    {
        for (int i = 0; i < enemies.size(); i++)
        {
            Enemy temp = enemies.get(i);
            println(
                temp.getName() + " HP: " + temp.getHp() + "/" + temp.getMaxHp()
                    + " intends to Hit " + temp.getDmg());
        }
    }


    public static void printOptions(ArrayList<Card> hand)
    {
        println("Please enter number to play your card:");
        for (int i = 0; i < hand.size(); i++)
        {
            Card temp = hand.get(i);
            println(
                "" + (i + 1) + ": " + temp.getName() + " (Cost "
                    + temp.getCost() + ") " + temp.getEffect());
        }
        println("0: End your turn.");
    }


    public static void printEnemyOption(ArrayList<Enemy> enemies)
    {
        println("Please enter number to choose your target:");
        for (int i = 0; i < enemies.size(); i++)
        {
            Enemy temp = enemies.get(i);
            println(
                "" + (i + 1) + ": " + temp.getName() + " intends to attack "
                    + temp.getDmg());
        }
        println("0: Cancel.");
    }
}
