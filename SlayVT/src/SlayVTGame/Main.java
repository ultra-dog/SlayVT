package SlayVTGame;

import static SlayVTGame.ToolClass.*;
import java.util.*;

public class Main
{
    private static Player player;
    private static Deck deck;
    private static ArrayList<Enemy> enemies = new ArrayList<Enemy>();
    private static BattleSystem battle = new BattleSystem();
    private static int floor = 0;
    private static final int TOTAL_FLOORS = 15;
    private static final int CHEST_FLOORS = 9;

    public static void main(String[] args)
    {
        String name = askName();

        int characterNum = askOption(
            "Please enter a number to choose your character: 1: Warrior",
            1,
            1);

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

        boolean restSiteOffered = false;
        boolean shopOffered = false;
        boolean eliteOffered = false;

        for (floor = 1; floor <= TOTAL_FLOORS; floor++)
        {
            println("\nFloor " + floor);

            if (floor == TOTAL_FLOORS)
            {
                enemies.clear();
                enemies.add(new Enemy("Boss", 48, 10));

                if (battle.fight(player, enemies, deck))
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
            ArrayList<String> options = new ArrayList<String>();
            switch (floor) {
                case 1:
                    options.add("Monster");
                    break;
                case TOTAL_FLOORS - 1:
                    options.add("RestSite");
                    break;
                case TOTAL_FLOORS:
                    options.add("Boss");
                    break;
                case CHEST_FLOORS:
                    options.add("Chest");
                    break;
                default:
                    ArrayList<String> availableRooms = new ArrayList<String>();
                    availableRooms.add("Monster");
                    availableRooms.add("Event");
                    if (!restSiteOffered && floor != TOTAL_FLOORS - 2)
                    {
                        availableRooms.add("RestSite");
                    }
                    if (!shopOffered)
                    {
                        availableRooms.add("Shop");
                    }
                    if (!eliteOffered) {
                        availableRooms.add("Elite");
                    }
                    Collections.shuffle(availableRooms, new Random());
                    options = new ArrayList<String>(
                        availableRooms.subList(
                            0,
                            randomInt(1, Math.min(2, availableRooms.size()))));
            }

            String prompt = "Choose your next room:";

            for (int i = 0; i < options.size(); i++)
            {
                prompt += "\n" + (i + 1) + ": " + options.get(i);
            }

            int choice = askOption(prompt, 1, options.size());
            selectedRoom = options.get(choice - 1);

            restSiteOffered = options.contains("RestSite");
            shopOffered = options.contains("Shop");
            eliteOffered = options.contains("Elite");

            switch (selectedRoom)
            {
                case "Monster":
                    enemies.clear();
                    enemies.add(new Enemy("Enemy 1", 12, 10));

                    if (battle.fight(player, enemies, deck))
                    {
                        println("Win");
                    }
                    else
                    {
                        println("Lose");
                        return;
                    }
                    break;
                case "Elite":
                    enemies.clear();
                    enemies.add(new Enemy("Elite", 30, 10));

                    if (battle.fight(player, enemies, deck))
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

                case "RestSite":
                    println("You entered a Rest Site.");
                    break;

                case "Shop":
                    println("You entered a Shop Room.");
                    break;
                default:
                    println("Content not updated.");
                    break;
            }
        }
    }
}
