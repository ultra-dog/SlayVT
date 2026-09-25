package SlayVTGame;

import static SlayVTGame.ToolClass.*;
import java.util.*;

public class Main
{
    static final String CHARACTER_NAME = "Meteorologist";
    private static Player player;
    private static Deck deck;
    private static ArrayList<Enemy> enemies = new ArrayList<Enemy>();
    private static BattleSystem battle = new BattleSystem();
    private static RestSystem restSite = new RestSystem();
    private static ShopSystem shop = new ShopSystem();
    private static CardRewardSystem cardRewards = new CardRewardSystem();
    private static EventSystem events = new EventSystem();
    private static int floor = 0;
    private static final int TOTAL_FLOORS = 15;
    private static final int CHEST_FLOORS = 9;

    public static void main(String[] args)
    {
        int mode = askOption(
            title("GAME MODE") + "\n"
                + choiceLine(1, "Normal Game") + "\n"
                + choiceLine(2, "Test Mode"),
            1,
            2);
        int startingFloor = chooseStartingFloor(mode);
        String name = askName();

        int characterNum = askOption(
            title("CHOOSE YOUR CHARACTER") + "\n"
                + choiceLine(1, CHARACTER_NAME),
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
        EnemyEncounterSystem encounters =
            new EnemyEncounterSystem(new Random());

        boolean restSiteOffered = false;
        boolean shopOffered = false;
        boolean eliteOffered = false;

        for (floor = startingFloor; floor <= TOTAL_FLOORS; floor++)
        {
            println(title("FLOOR " + floor + " / " + TOTAL_FLOORS));
            println("HP: " + player.getHp() + "/" + player.getMaxHp()
                + "  |  Gold: " + player.getGold());

            if (floor == TOTAL_FLOORS)
            {
                enemies.clear();
                enemies.add(EnemyRoster.boss());

                if (battle.fight(player, enemies, deck))
                {
                    println(title("VICTORY"));
                }
                else
                {
                    println(title("GAME OVER"));
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
                    options = encounters.roomOptions(
                        floor, restSiteOffered, shopOffered, eliteOffered,
                        events.hasAvailableEvent(player, deck));
            }

            String prompt = "\nCHOOSE YOUR NEXT ROOM\n";

            for (int i = 0; i < options.size(); i++)
            {
                prompt += "\n" + choiceLine(i + 1, options.get(i));
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
                    enemies.add(encounters.drawNormal(floor));

                    if (battle.fight(player, enemies, deck))
                    {
                        println("\nBattle won.");
                        cardRewards.offer(deck);
                    }
                    else
                    {
                        println(title("GAME OVER"));
                        return;
                    }
                    break;
                case "Elite":
                    enemies.clear();
                    enemies.add(encounters.drawElite(floor));

                    if (battle.fight(player, enemies, deck))
                    {
                        println("\nBattle won.");
                        cardRewards.offer(deck);
                    }
                    else
                    {
                        println(title("GAME OVER"));
                        return;
                    }
                    break;

                case "Event":
                    events.visit(player, deck);
                    break;

                case "RestSite":
                    restSite.rest(player, deck);
                    break;

                case "Shop":
                    shop.open(player, deck);
                    break;
                case "Chest":
                    cardRewards.offer(deck, 4, "CHEST");
                    break;
                default:
                    println(title(selectedRoom)
                        + "\nContent not updated.");
                    break;
            }
        }
    }

    static int chooseStartingFloor(int mode)
    {
        if (mode == 1)
        {
            return 1;
        }
        return askOption(
            title("TEST MODE") + "\nChoose starting floor (1-15):",
            1,
            TOTAL_FLOORS);
    }
}
