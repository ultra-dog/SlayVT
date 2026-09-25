package SlayVTGame;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

public class TestModeTest
{
    public static void main(String[] args) throws Exception
    {
        check("Normal mode starts on floor 1",
            Main.chooseStartingFloor(1) == 1);

        InputStream originalInput = System.in;
        try
        {
            System.setIn(new ByteArrayInputStream("9\n".getBytes("UTF-8")));
            check("Test mode can start at the chest",
                Main.chooseStartingFloor(2) == 9);

            System.setIn(new ByteArrayInputStream("15\n".getBytes("UTF-8")));
            check("Test mode can start at the boss",
                Main.chooseStartingFloor(2) == 15);
        }
        finally
        {
            System.setIn(originalInput);
        }

        System.out.println("PASS: test mode floor selection.");
    }

    private static void check(String label, boolean condition)
    {
        if (!condition)
        {
            throw new AssertionError(label);
        }
    }
}
