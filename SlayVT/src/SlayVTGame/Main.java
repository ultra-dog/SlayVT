package SlayVTGame;

import static SlayVTGame.ToolClass.*;
import java.util.*;

public class Main
{
    private static Player player;
    private static Deck deck;
    private static ArrayList<Enemy> enemies = new ArrayList<Enemy>();
    private static FightSystem combat = new FightSystem();
    private static int floor = 0;
    private static final int TOTAL_FLOORS = 10;
    private static Random random = new Random();

    public static void main(String[] args)
    {
        String name = askName();

        int characterNum = askOption(
            "Please enter a number to choose your character: 1: Warrior",
            1, 1);

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

        boolean campfirePreviouslyOffered = false;
        boolean shopPreviouslyOffered = false;

        for (floor = 1; floor <= TOTAL_FLOORS; floor++)
        {
            println("\nFloor " + floor);

            if (floor == TOTAL_FLOORS)
            {
                askOption("Choose your next room:\n1: Boss", 1, 1);
                println("Boss Room");

                enemies.clear();
                enemies.add(new Enemy("Boss", 48, 10));

                if (combat.fight(player, enemies, deck))
                {
                    println("Victory!");
                }
                else
                {
                    println("You died.");
                }

                return;
            }

            String selectedRoom;

            if (floor == 1)
            {
                selectedRoom = "Monster";
                campfirePreviouslyOffered = false;
                shopPreviouslyOffered = false;
            }
            else if (floor == TOTAL_FLOORS - 1)
            {
                selectedRoom = "Campfire";
                campfirePreviouslyOffered = true;
                shopPreviouslyOffered = false;
            }
            else
            {
                ArrayList<String> availableRooms = new ArrayList<String>();
                availableRooms.add("Monster");
                availableRooms.add("Event");

                if (!campfirePreviouslyOffered
                    && floor != TOTAL_FLOORS - 2)
                {
                    availableRooms.add("Campfire");
                }

                if (!shopPreviouslyOffered)
                {
                    availableRooms.add("Shop");
                }

                Collections.shuffle(availableRooms, random);

                ArrayList<String> options = new ArrayList<String>(
                    availableRooms.subList(
                        0, randomInt(1, Math.min(3, availableRooms.size()))));

                String prompt = "Choose your next room:";

                for (int i = 0; i < options.size(); i++)
                {
                    prompt += "\n" + (i + 1) + ": " + options.get(i);
                }

                int choice = askOption(prompt, 1, options.size());
                selectedRoom = options.get(choice - 1);

                campfirePreviouslyOffered = options.contains("Campfire");
                shopPreviouslyOffered = options.contains("Shop");
            }

            switch (selectedRoom)
            {
                case "Monster":
                    enemies.clear();
                    enemies.add(new Enemy("Enemy 1", 12, 10));

                    if (combat.fight(player, enemies, deck))
                    {
                        println("Win");
                    }
                    else
                    {
                        println("Lose");
                        return;
                    }
                    break;

                case "Event":
                    println("You entered an Event Room.");
                    break;

                case "Campfire":
                    println("You entered a Campfire Room.");
                    break;

                case "Shop":
                    println("You entered a Shop Room.");
                    break;
            }
        }
    }
}