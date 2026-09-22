package SlayVTGame;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class ShopSystemTest
{
    private interface TestAction
    {
        void run();
    }

    private static int checks;
    private static int failures;

    public static void main(String[] args)
    {
        testPlayerGold();
        testDeckRemoval();
        testShopItemValidation();
        testSeededInventory();

        if (failures > 0)
        {
            throw new AssertionError(failures + " of " + checks
                + " shop checks failed.");
        }
        System.out.println("PASS: " + checks + " shop checks.");
    }

    private static void testShopItemValidation()
    {
        ShopItem item = new ShopItem(CardLibrary.STRIKE, 50);
        check("Item exposes its card type", "Strike",
            item.getCardType().getName());
        check("Item exposes its price", 50, item.getPrice());
        check("New item is available", false, item.isSold());
        checkThrows("Null item type is rejected", new TestAction()
        {
            public void run()
            {
                new ShopItem(null, 50);
            }
        });
        checkThrows("Non-positive item price is rejected", new TestAction()
        {
            public void run()
            {
                new ShopItem(CardLibrary.STRIKE, 0);
            }
        });
    }

    private static void testSeededInventory()
    {
        ArrayList<ShopItem> first =
            new ShopSystem(new Random(2114)).beginVisit();
        ArrayList<ShopItem> second =
            new ShopSystem(new Random(2114)).beginVisit();
        check("Shop offers five cards", 5, first.size());
        check("Seeded inventory is repeatable", inventorySignature(first),
            inventorySignature(second));

        int attacks = 0;
        int skills = 0;
        int powers = 0;
        Set<CardLibrary> unique = new HashSet<CardLibrary>();
        for (ShopItem item : first)
        {
            unique.add(item.getCardType());
            String type = item.getCardType().getType();
            if ("Attack".equals(type))
            {
                attacks++;
                check("Attack costs 50", 50, item.getPrice());
            }
            else if ("Skill".equals(type))
            {
                skills++;
                check("Skill costs 50", 50, item.getPrice());
            }
            else if ("Power".equals(type))
            {
                powers++;
                check("Power costs 75", 75, item.getPrice());
            }
        }
        check("Inventory has two Attacks", 2, attacks);
        check("Inventory has two Skills", 2, skills);
        check("Inventory has one Power", 1, powers);
        check("Inventory contains no duplicates", 5, unique.size());
    }

    private static String inventorySignature(ArrayList<ShopItem> items)
    {
        String signature = "";
        for (ShopItem item : items)
        {
            signature += item.getCardType().name() + ":"
                + item.getPrice() + ";";
        }
        return signature;
    }

    private static void testDeckRemoval()
    {
        Deck deck = new Deck(1);
        int initialSize = deck.size();
        Card selected = deck.getDeck()[0];
        check("Selected card is removed", true, deck.removeCard(selected));
        check("Removal decreases deck size", initialSize - 1, deck.size());
        check("The same card cannot be removed twice", false,
            deck.removeCard(selected));
        check("A foreign card reference is rejected", false,
            deck.removeCard(new Card(CardLibrary.STRIKE)));
        check("Null card removal is rejected", false, deck.removeCard(null));

        while (deck.size() > 1)
        {
            deck.removeCard(deck.getDeck()[0]);
        }
        Card lastCard = deck.getDeck()[0];
        check("The final card cannot be removed", false,
            deck.removeCard(lastCard));
        check("Final card remains in deck", 1, deck.size());
    }

    private static void testPlayerGold()
    {
        Player player = new Player("Player", 80);
        check("Player starts with 99 Gold", 99, player.getGold());
        check("Affordable spend succeeds", true, player.spendGold(50));
        check("Affordable spend deducts Gold", 49, player.getGold());
        check("Unaffordable spend fails", false, player.spendGold(50));
        check("Failed spend preserves Gold", 49, player.getGold());
        player.addGold(11);
        check("Gold can be added", 60, player.getGold());
        player.addGold(0);
        check("Adding zero is a no-op", 60, player.getGold());
        checkThrows("Negative gain is rejected", new TestAction()
        {
            public void run()
            {
                player.addGold(-1);
            }
        });
        checkThrows("Negative spend is rejected", new TestAction()
        {
            public void run()
            {
                player.spendGold(-1);
            }
        });
    }

    private static void check(String label, int expected, int actual)
    {
        checks++;
        if (expected != actual)
        {
            failures++;
            System.err.println(label + ": expected " + expected
                + ", got " + actual);
        }
    }

    private static void check(
        String label, boolean expected, boolean actual)
    {
        checks++;
        if (expected != actual)
        {
            failures++;
            System.err.println(label + ": expected " + expected
                + ", got " + actual);
        }
    }

    private static void check(
        String label, String expected, String actual)
    {
        checks++;
        if (!expected.equals(actual))
        {
            failures++;
            System.err.println(label + ": expected " + expected
                + ", got " + actual);
        }
    }

    private static void checkThrows(String label, TestAction action)
    {
        checks++;
        try
        {
            action.run();
            failures++;
            System.err.println(label + ": expected IllegalArgumentException");
        }
        catch (IllegalArgumentException expected)
        {
            // Expected validation failure.
        }
    }
}
