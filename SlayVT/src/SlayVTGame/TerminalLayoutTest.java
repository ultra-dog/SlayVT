package SlayVTGame;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Random;

public class TerminalLayoutTest
{
    public static void main(String[] args) throws Exception
    {
        check("Title separates screens",
            ToolClass.title("Shop").startsWith("\n")
                && ToolClass.title("Shop").contains("SHOP"));
        check("Choices are indented", "  3: Strike".equals(
            ToolClass.choiceLine(3, "Strike")));

        String detail = ToolClass.detailLine(new Card(
            CardLibrary.RED_HOT_FORM).getEffect());
        for (String line : detail.split("\n"))
        {
            check("Long effects fit narrow terminals", line.length() <= 72);
            check("Effect lines have a left margin", line.startsWith("     "));
        }

        ArrayList<Card> hand = new ArrayList<Card>();
        hand.add(new Card(CardLibrary.STRIKE));
        String handMenu = ToolClass.printOptions(hand);
        check("Hand has a section label", handMenu.contains("YOUR HAND"));
        check("Card cost stays on the name line",
            handMenu.contains("1: Strike [1 Energy]"));
        check("Card effect has its own line",
            handMenu.contains("\n     Deal 6 damage."));
        check("End turn remains available",
            handMenu.contains("0: End turn"));

        InputStream savedIn = System.in;
        PrintStream savedOut = System.out;
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try
        {
            System.setOut(new PrintStream(output, true, "UTF-8"));
            Player player = new Player("Tester", 80);
            ToolClass.printPlayer(player);
            ArrayList<Enemy> enemies = new ArrayList<Enemy>();
            enemies.add(new Enemy("Dummy", 20, 5));
            ToolClass.printEnemies(enemies);
            System.setIn(new ByteArrayInputStream(
                "0\n".getBytes("UTF-8")));
            new ShopSystem(new Random(5)).open(player, new Deck(1));
        }
        finally
        {
            System.setIn(savedIn);
            System.setOut(savedOut);
        }
        String screen = output.toString("UTF-8");
        check("Player status includes Gold",
            screen.contains("Gold: 99"));
        check("Empty status is omitted",
            !screen.contains("Status: None"));
        check("Enemy section is labeled",
            screen.contains("ENEMIES"));
        check("Shop separates cards and service",
            screen.contains("CARDS") && screen.contains("SERVICES"));
        check("Shop keeps leave choice",
            screen.contains("0: Leave shop"));

        output.reset();
        try
        {
            System.setOut(new PrintStream(output, true, "UTF-8"));
            System.setIn(new ByteArrayInputStream("1\n"
                .getBytes("UTF-8")));
            new RestSystem().rest(new Player("Tester", 80), new Deck(1));
        }
        finally
        {
            System.setIn(savedIn);
            System.setOut(savedOut);
        }
        check("Rest menu is labeled",
            output.toString("UTF-8").contains("REST SITE")
                && output.toString("UTF-8").contains("CHOOSE AN ACTION"));
        System.out.println("PASS: terminal layout checks.");
    }

    private static void check(String label, boolean condition)
    {
        if (!condition)
        {
            throw new AssertionError(label);
        }
    }
}
