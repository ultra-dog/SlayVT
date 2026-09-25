package SlayVTGame;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;

public class EventSystemTest
{
    private static int checks;

    public static void main(String[] args) throws Exception
    {
        testUniqueEventsAndRoomFallback();
        testDoorsAndAroma();
        testGoldAndHp();
        testCardsAndCosts();
        testEventPresentation();
        System.out.println("PASS: " + checks + " event checks.");
    }

    private static void testUniqueEventsAndRoomFallback()
    {
        Player player = new Player("Meteorologist", 80);
        Deck deck = new Deck(1);
        EventSystem events = new EventSystem(new Random(4));
        HashSet<EventSystem.EventType> seen =
            new HashSet<EventSystem.EventType>();
        while (events.hasAvailableEvent(player, deck))
        {
            yes("event never repeats",
                seen.add(events.drawEvent(player, deck)));
        }
        equal("all six events can occur", 6, seen.size());
        yes("used event pool is exhausted",
            !events.hasAvailableEvent(player, deck));
        ArrayList<String> rooms = new EnemyEncounterSystem(
            new Random(1)).roomOptions(4, false, false, false, false);
        yes("no Event room after exhaustion", !rooms.contains("Event"));
        yes("other rooms remain", !rooms.isEmpty());

        Deck upgraded = new Deck(1);
        for (Card card : upgraded.getDeck()) card.upgrade();
        EventSystem limited = new EventSystem(new Random(2));
        while (limited.hasAvailableEvent(player, upgraded))
        {
            EventSystem.EventType type = limited.drawEvent(player,
                upgraded);
            yes("unusable deck event is skipped",
                type != EventSystem.EventType.DOORS
                    && type != EventSystem.EventType.AROMA);
        }
    }

    private static void testDoorsAndAroma()
    {
        EventSystem events = new EventSystem(new Random(0));
        Player player = new Player("Meteorologist", 80);
        Deck deck = new Deck(1);
        yes("light upgrades two", events.applyChoice(
            EventSystem.EventType.DOORS, 1, player, deck, null, null));
        int upgraded = 0;
        for (Card card : deck.getDeck())
        {
            if (card.isUpgraded()) upgraded++;
        }
        equal("exactly two upgraded", 2, upgraded);

        Card removed = deck.getDeck()[0];
        int size = deck.size();
        yes("dark removes selected card", events.applyChoice(
            EventSystem.EventType.DOORS, 2, player, deck, removed, null));
        equal("deck shrinks by one", size - 1, deck.size());
        yes("foreign card cannot be removed", !events.applyChoice(
            EventSystem.EventType.DOORS, 2, player, deck,
            new Card(CardLibrary.STRIKE), null));

        Card toUpgrade = null;
        for (Card card : deck.getDeck())
        {
            if (card.canUpgrade())
            {
                toUpgrade = card;
                break;
            }
        }
        yes("workshop upgrades chosen card", events.applyChoice(
            EventSystem.EventType.AROMA, 1, player, deck,
            toUpgrade, null));
        yes("chosen card is upgraded", toUpgrade.isUpgraded());

        Card toTransform = deck.getDeck()[1];
        CardLibrary original = toTransform.getCardType();
        size = deck.size();
        yes("workshop transforms chosen card", events.applyChoice(
            EventSystem.EventType.AROMA, 2, player, deck,
            toTransform, null));
        equal("transform preserves deck size", size, deck.size());
        Card replacement = deck.getDeck()[deck.size() - 1];
        yes("transform changes card type",
            replacement.getCardType() != original);
        yes("transform excludes starter cards",
            replacement.getCardType().getRarity() != CardRarity.BASIC);
    }

    private static void testGoldAndHp()
    {
        EventSystem events = new EventSystem(new Random(1));
        Player player = new Player("Meteorologist", 80);
        Deck deck = new Deck(1);
        yes("group path succeeds", events.applyChoice(
            EventSystem.EventType.MAZE, 1, player, deck, null, null));
        equal("group path gold", 134, player.getGold());
        yes("solo path succeeds", events.applyChoice(
            EventSystem.EventType.MAZE, 2, player, deck, null, null));
        equal("solo path gold", 209, player.getGold());
        equal("solo path HP loss", 72, player.getHp());

        player.takeUnblockableDamage(60);
        yes("pond heals", events.applyChoice(
            EventSystem.EventType.BATHS, 1, player, deck, null, null));
        equal("pond healing", 24, player.getHp());
        yes("pond max HP option", events.applyChoice(
            EventSystem.EventType.BATHS, 2, player, deck, null, null));
        equal("max HP increases five", 85, player.getMaxHp());
        equal("max HP option also loses eight HP", 21, player.getHp());

        Player fragile = new Player("Fragile", 80);
        fragile.takeUnblockableDamage(72);
        int gold = fragile.getGold();
        yes("lethal solo choice is rejected", !events.applyChoice(
            EventSystem.EventType.MAZE, 2, fragile, deck, null, null));
        yes("lethal immersion is rejected", !events.applyChoice(
            EventSystem.EventType.BATHS, 2, fragile, deck, null, null));
        equal("rejected choice preserves HP", 8, fragile.getHp());
        equal("rejected choice preserves Gold", gold, fragile.getGold());
        equal("rejected choice preserves Max HP", 80,
            fragile.getMaxHp());
    }

    private static void testCardsAndCosts()
    {
        EventSystem events = new EventSystem(new Random(3));
        Player player = new Player("Meteorologist", 80);
        Deck deck = new Deck(1);
        ArrayList<CardLibrary> offered =
            new CardRewardSystem(new Random(3)).generateChoices(5);
        equal("five distinct reward candidates", 5,
            new HashSet<CardLibrary>(offered).size());
        int size = deck.size();
        yes("five-card choice adds selected card", events.applyChoice(
            EventSystem.EventType.LEECH, 1, player, deck, null,
            CardLibrary.DROUGHT));
        equal("chosen reward adds one card", size + 1, deck.size());
        yes("reward matches chosen card", deck.getDeck()[size]
            .getCardType() == CardLibrary.DROUGHT);
        yes("starter card cannot be event reward", !events.applyChoice(
            EventSystem.EventType.LEECH, 1, player, deck, null,
            CardLibrary.STRIKE));
        yes("leaving does not add a card", events.applyChoice(
            EventSystem.EventType.LEECH, 2, player, deck, null, null));
        equal("leaving preserves deck", size + 1, deck.size());

        int gold = player.getGold();
        yes("machine sells a Power", events.applyChoice(
            EventSystem.EventType.AUTOMATON, 1, player, deck,
            null, null));
        equal("machine charges 25 Gold", gold - 25, player.getGold());
        yes("machine reward is a Power", "Power".equals(
            deck.getDeck()[deck.size() - 1].getType()));
        yes("core gives a zero-cost card", events.applyChoice(
            EventSystem.EventType.AUTOMATON, 2, player, deck,
            null, null));
        equal("core reward cost", 0,
            deck.getDeck()[deck.size() - 1].getCost());

        Player poor = new Player("Poor", 80);
        poor.spendGold(99);
        size = deck.size();
        yes("unaffordable Power is rejected", !events.applyChoice(
            EventSystem.EventType.AUTOMATON, 1, poor, deck,
            null, null));
        equal("failed purchase preserves deck", size, deck.size());
        yes("invalid event choice is rejected", throwsInvalidChoice(
            events, player, deck));
    }

    private static boolean throwsInvalidChoice(
        EventSystem events, Player player, Deck deck)
    {
        try
        {
            events.applyChoice(EventSystem.EventType.MAZE, 3,
                player, deck, null, null);
            return false;
        }
        catch (IllegalArgumentException expected)
        {
            return true;
        }
    }

    private static void testEventPresentation() throws Exception
    {
        // Index 1 in the initial pool selects the Drillfield event.
        EventSystem events = new EventSystem(new Random()
        {
            @Override
            public int nextInt(int bound) { return 1; }
        });
        Player player = new Player("Meteorologist", 80);
        InputStream originalIn = System.in;
        PrintStream originalOut = System.out;
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try
        {
            System.setIn(new ByteArrayInputStream("1\n".getBytes("UTF-8")));
            System.setOut(new PrintStream(output, true, "UTF-8"));
            events.visit(player, new Deck(1));
        }
        finally
        {
            System.setIn(originalIn);
            System.setOut(originalOut);
        }
        String text = output.toString("UTF-8");
        yes("VT event title shown", text.contains("DRILLFIELD DETOUR"));
        yes("short story shown", text.contains("Rain turns the Drillfield"));
        yes("two numbered choices shown",
            text.contains("  1: ") && text.contains("  2: "));
        yes("outcome shown", text.contains("You gained 35 Gold."));
        equal("UI choice applied", 134, player.getGold());
    }

    private static void equal(String label, int expected, int actual)
    {
        yes(label + " (expected " + expected + ", got " + actual + ")",
            expected == actual);
    }

    private static void yes(String label, boolean condition)
    {
        checks++;
        if (!condition) throw new AssertionError(label);
    }
}
