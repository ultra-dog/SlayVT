package SlayVTGame;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;

public class ShopSystemTest
{
    private interface TestAction
    {
        void run();
    }

    private static int checks;
    private static int failures;

    public static void main(String[] args) throws Exception
    {
        testPlayerGold();
        testDeckRemoval();
        testShopItemValidation();
        testSeededInventory();
        testCardPurchases();
        testCardRemovalService();
        testOpenCanLeave();

        if (failures > 0)
        {
            throw new AssertionError(failures + " of " + checks
                + " shop checks failed.");
        }
        System.out.println("PASS: " + checks + " shop checks.");
    }

    private static void testOpenCanLeave() throws Exception
    {
        InputStream originalIn = System.in;
        PrintStream originalOut = System.out;
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try
        {
            System.setIn(new ByteArrayInputStream(
                "0\n".getBytes("UTF-8")));
            System.setOut(new PrintStream(output, true, "UTF-8"));
            new ShopSystem(new Random(5)).open(
                new Player("Player", 80), new Deck(1));
        }
        finally
        {
            System.setIn(originalIn);
            System.setOut(originalOut);
        }
        String text = output.toString("UTF-8");
        check("Shop displays current Gold", true,
            text.contains("Gold: 99"));
        check("Shop displays removal price", true,
            text.contains("Remove a card (75 Gold)"));
        check("Shop offers a leave option", true,
            text.contains("0: Leave shop"));
    }

    private static void testCardRemovalService()
    {
        ShopSystem shop = new ShopSystem(new Random(2));
        shop.beginVisit();
        Player player = new Player("Player", 80);
        Deck deck = new Deck(1);
        Card first = deck.getDeck()[0];
        int initialSize = deck.size();

        check("Initial removal costs 75", 75, shop.getRemovalCost());
        check("First removal succeeds", true,
            shop.removeCard(player, deck, first));
        check("Removal deducts Gold", 24, player.getGold());
        check("Removal changes deck size", initialSize - 1, deck.size());
        check("Next removal costs 100", 100, shop.getRemovalCost());

        Card second = deck.getDeck()[0];
        check("Second removal in one visit fails", false,
            shop.removeCard(player, deck, second));
        check("Rejected repeat does not charge", 24, player.getGold());
        check("Rejected repeat preserves deck", initialSize - 1,
            deck.size());

        shop.beginVisit();
        player.addGold(76);
        check("New visit permits removal", true,
            shop.removeCard(player, deck, second));
        check("New visit uses increased price", 0, player.getGold());
        check("Next successful price is 125", 125, shop.getRemovalCost());

        ShopSystem invalidShop = new ShopSystem(new Random(3));
        invalidShop.beginVisit();
        Player invalidPlayer = new Player("Player", 80);
        Deck invalidDeck = new Deck(1);
        int invalidSize = invalidDeck.size();
        check("Foreign card removal fails", false, invalidShop.removeCard(
            invalidPlayer, invalidDeck, new Card(CardLibrary.STRIKE)));
        check("Foreign card does not charge", 99, invalidPlayer.getGold());
        check("Foreign card preserves deck", invalidSize,
            invalidDeck.size());

        while (invalidDeck.size() > 1)
        {
            invalidDeck.removeCard(invalidDeck.getDeck()[0]);
        }
        Card finalCard = invalidDeck.getDeck()[0];
        check("Shop cannot remove final card", false,
            invalidShop.removeCard(invalidPlayer, invalidDeck, finalCard));
        check("Final-card rejection does not charge", 99,
            invalidPlayer.getGold());

        ShopSystem poorShop = new ShopSystem(new Random(4));
        poorShop.beginVisit();
        Player poorPlayer = new Player("Poor", 80);
        poorPlayer.spendGold(99);
        Deck poorDeck = new Deck(1);
        int poorSize = poorDeck.size();
        check("Unaffordable removal fails", false, poorShop.removeCard(
            poorPlayer, poorDeck, poorDeck.getDeck()[0]));
        check("Unaffordable removal preserves deck", poorSize,
            poorDeck.size());
        check("Failed removal preserves price", 75,
            poorShop.getRemovalCost());

        checkThrows("Null removal player is rejected", new TestAction()
        {
            public void run()
            {
                invalidShop.removeCard(null, invalidDeck, finalCard);
            }
        });
        checkThrows("Null removal deck is rejected", new TestAction()
        {
            public void run()
            {
                invalidShop.removeCard(invalidPlayer, null, finalCard);
            }
        });
        checkThrows("Null removal card is rejected", new TestAction()
        {
            public void run()
            {
                invalidShop.removeCard(invalidPlayer, invalidDeck, null);
            }
        });
    }

    private static void testCardPurchases()
    {
        ShopSystem shop = new ShopSystem(new Random(1));
        ArrayList<ShopItem> inventory = shop.beginVisit();
        ShopItem item = inventory.get(0);
        Player player = new Player("Player", 80);
        Deck deck = new Deck(1);
        int initialSize = deck.size();

        check("Affordable card purchase succeeds", true,
            shop.purchaseCard(player, deck, item));
        check("Purchase deducts exact price", 99 - item.getPrice(),
            player.getGold());
        check("Purchase adds one card", initialSize + 1, deck.size());
        check("Purchased offer is sold", true, item.isSold());
        check("Sold offer cannot be purchased twice", false,
            shop.purchaseCard(player, deck, item));
        check("Second purchase does not charge Gold", 99 - item.getPrice(),
            player.getGold());
        check("Second purchase does not add a card", initialSize + 1,
            deck.size());

        Player poorPlayer = new Player("Poor", 80);
        poorPlayer.spendGold(99);
        Deck poorDeck = new Deck(1);
        ShopItem expensive = new ShopItem(CardLibrary.RED_HOT_FORM, 75);
        int poorSize = poorDeck.size();
        check("Unaffordable purchase fails", false,
            shop.purchaseCard(poorPlayer, poorDeck, expensive));
        check("Unaffordable purchase preserves Gold", 0,
            poorPlayer.getGold());
        check("Unaffordable purchase preserves deck", poorSize,
            poorDeck.size());
        check("Unaffordable offer remains available", false,
            expensive.isSold());

        checkThrows("Null purchase player is rejected", new TestAction()
        {
            public void run()
            {
                shop.purchaseCard(null, deck, item);
            }
        });
        checkThrows("Null purchase deck is rejected", new TestAction()
        {
            public void run()
            {
                shop.purchaseCard(player, null, item);
            }
        });
        checkThrows("Null purchase item is rejected", new TestAction()
        {
            public void run()
            {
                shop.purchaseCard(player, deck, null);
            }
        });
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
