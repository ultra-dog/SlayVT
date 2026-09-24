package SlayVTGame;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;

public class CardRewardSystemTest
{
    public static void main(String[] args) throws Exception
    {
        for (int seed = 0; seed < 100; seed++)
        {
            ArrayList<CardLibrary> choices =
                new CardRewardSystem(new Random(seed)).generateChoices();
            check("Exactly three reward cards", choices.size() == 3);
            check("Reward cards do not repeat",
                new HashSet<CardLibrary>(choices).size() == 3);
            check("Strike is not a reward",
                !choices.contains(CardLibrary.STRIKE));
            check("Defend is not a reward",
                !choices.contains(CardLibrary.DEFEND));
        }

        CardLibrary secondChoice =
            new CardRewardSystem(new Random(7)).generateChoices().get(1);
        Deck deck = new Deck(1);
        int originalSize = deck.size();
        InputStream savedIn = System.in;
        PrintStream savedOut = System.out;
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try
        {
            System.setIn(new ByteArrayInputStream("2\n".getBytes("UTF-8")));
            System.setOut(new PrintStream(output, true, "UTF-8"));
            new CardRewardSystem(new Random(7)).offer(deck);
        }
        finally
        {
            System.setIn(savedIn);
            System.setOut(savedOut);
        }
        check("Selecting a reward adds exactly one card",
            deck.size() == originalSize + 1);
        check("Selected card is added to the deck",
            deck.getDeck()[originalSize].getName()
                .equals(secondChoice.getName()));
        check("Reward menu shows three numbered options",
            output.toString("UTF-8").contains("CARD REWARD")
                && output.toString("UTF-8").contains("  1: ")
                && output.toString("UTF-8").contains("  2: ")
                && output.toString("UTF-8").contains("  3: "));
        System.out.println("PASS: card reward checks.");
    }

    private static void check(String label, boolean condition)
    {
        if (!condition)
        {
            throw new AssertionError(label);
        }
    }
}
