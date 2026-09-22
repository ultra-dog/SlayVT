package SlayVTGame;
import static SlayVTGame.ToolClass.*;

import java.util.*;

public final class ToolClass
{
    public static String title(String text)
    {
        return "\n========================================\n"
            + text.toUpperCase(Locale.ROOT)
            + "\n========================================";
    }

    public static String choiceLine(int number, String text)
    {
        return "  " + number + ": " + text;
    }

    public static String detailLine(String text)
    {
        StringBuilder result = new StringBuilder("     ");
        int lineLength = 5;
        for (String word : text.split("\\s+"))
        {
            if (lineLength > 5 && lineLength + word.length() + 1 > 72)
            {
                result.append("\n     ");
                lineLength = 5;
            }
            else if (lineLength > 5)
            {
                result.append(' ');
                lineLength++;
            }
            result.append(word);
            lineLength += word.length();
        }
        return result.toString();
    }

    public static void print(String txt)
    {
        System.out.print(txt);
    }


    public static void println(String txt)
    {
        System.out.println(txt);
    }
    
    public static int randomInt(int min, int max) {
        return (int)(Math.random() * (max - min + 1)) + min;
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
        println("\nYOU: " + player.getName());
        println("  HP: " + player.getHp() + "/" + player.getMaxHp()
            + "  |  Block: " + player.getBlock()
            + "  |  Energy: " + player.getEnergy() + "/"
            + player.getMaxEnergy() + "  |  Gold: " + player.getGold());
        String statuses = player.getBuffs().toString();
        if (player.checkAlive() && !"None".equals(statuses))
        {
            println("  Status: " + statuses);
        }
    }


    public static void printEnemies(ArrayList<Enemy> enemies)
    {
        println("\nENEMIES");
        for (int i = 0; i < enemies.size(); i++)
        {
            Enemy temp = enemies.get(i);
            println(choiceLine(i + 1, temp.getName()
                + "  |  HP: " + temp.getHp() + "/" + temp.getMaxHp()
                + "  |  Intent: " + temp.getDmg() + " damage"));
            String statuses = temp.getBuffs().toString();
            if (temp.checkAlive() && !"None".equals(statuses))
            {
                println("     Status: " + statuses);
            }
        }
    }


    public static String printOptions(ArrayList<Card> hand)
    {
        String txt = "\nYOUR HAND (" + hand.size() + ")\n";
        for (int i = 0; i < hand.size(); i++)
        {
            Card temp = hand.get(i);
            txt += choiceLine(i + 1, temp.getName() + " ["
                + temp.getCost() + " Energy]") + "\n"
                + detailLine(temp.getEffect()) + "\n";
        }
        txt += "\n" + choiceLine(0, "End turn");
        return txt;
    }


    public static String printEnemyOption(ArrayList<Enemy> enemies)
    {
        String txt = "\nCHOOSE TARGET\n";
        for (int i = 0; i < enemies.size(); i++)
        {
            Enemy temp = enemies.get(i);
            txt += choiceLine(i + 1, temp.getName() + "  |  HP: "
                + temp.getHp() + "/" + temp.getMaxHp()
                + "  |  Intent: " + temp.getDmg() + " damage") + "\n";
        }
        txt += "\n" + choiceLine(0, "Cancel");
        return txt;
    }


    public static String askName()
    {
        String name = "";
        while (true)
        {
            println(title("SLAY VT") + "\nEnter your name:");
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
            print("\n> ");
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
