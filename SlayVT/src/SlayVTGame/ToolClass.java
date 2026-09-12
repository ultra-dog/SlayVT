package SlayVTGame;
import java.util.*;

public final class ToolClass
{
    public static void print(String txt)
    {
        System.out.print(txt);
    }


    public static void println(String txt)
    {
        System.out.println(txt);
    }


    public static void drawCardFromPile(
        ArrayList<Card> drawPile,
        ArrayList<Card> hand,
        ArrayList<Card> discardPile)
    {
        if (drawPile.size() <= 0)
        {
            drawCardFromDiscard(drawPile, discardPile);
        }
        int randindex = (int)(Math.random() * drawPile.size());
        hand.add(drawPile.get(randindex));
        drawPile.remove(randindex);
    }


    public static void drawCardFromDiscard(
        ArrayList<Card> drawPile,
        ArrayList<Card> discardPile)
    {
        if (discardPile.size() > 0)
        {
            for (int i = 0; i < discardPile.size(); i++)
            {
                drawPile.add(discardPile.get(i));
            }
            discardPile.clear();
        }
        else
        {
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


    public static String printOptions(ArrayList<Card> hand)
    {
        String txt = "";
        txt += "Please enter number to play your card:\n";
        for (int i = 0; i < hand.size(); i++)
        {
            Card temp = hand.get(i);
            txt += (i + 1) + ": " + temp.getName() + " (Cost "
                    + temp.getCost() + ") " + temp.getEffect() + "\n";
        }
        txt += "0: End your turn.";
        return txt;
    }


    public static String printEnemyOption(ArrayList<Enemy> enemies)
    {
        String txt = "";
        txt += "Please enter number to choose your target:\n";
        for (int i = 0; i < enemies.size(); i++)
        {
            Enemy temp = enemies.get(i);
            txt += 
                "" + (i + 1) + ": " + temp.getName() + " intends to attack "
                    + temp.getDmg() + "\n";
        }
        txt += "0: Cancel.";
        return txt;
    }


    public static String askName()
    {
        String name = "";
        while (true)
        {
            println("Please enter your name: ");
            try
            {
                name = new Scanner(System.in).nextLine();

            }
            catch (NullPointerException e)
            {
                println("Please enter a non-blank name.");
            }
            catch (InputMismatchException e)
            {
                println("Please enter a valid name.");
            }
            if (name.length() > 20)
            {
                println("Please enter a name less than 20 characters.");
            }
            else if (name.length() == 0)
            {
                println("Please enter a non-blank name.");
            }
            else
            {
                break;
            }
        }
        return name;
    }


    public static int askOption(String prompt, int min, int max)
    {
        int option = -1;
        while (true)
        {
            println(prompt);
            try
            {
                option = new Scanner(System.in).nextInt();
            }
            catch (NullPointerException e)
            {
                println("Please enter a number.");
                continue;
            }
            catch (InputMismatchException e)
            {
                println("Please enter a valid integer.");
                continue;
            }
            if (option < min)
            {
                println("Please enter an integer no less than " + min + ".");
            }
            else if (option > max)
            {
                println("Please enter an integer no more than " + max + ".");
            }
            else {
                break;
            }
        }
        return option;
    }
}
