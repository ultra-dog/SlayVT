package SlayVTGame;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;

public class EnemyEncounterSystemTest
{
    private static int checks;

    public static void main(String[] args)
    {
        EnemyEncounterSystem run = new EnemyEncounterSystem(roll(14));
        HashSet<String> names = new HashSet<String>();
        for (int i = 0; i < 3; i++)
        {
            names.add(run.drawNormal(1).getName());
        }
        check("three different early fights", names.size() == 3);
        check("early pool exhausted", !run.hasNormal(4));
        ArrayList<String> rooms = run.roomOptions(4,
            false, false, false);
        check("exhausted pool hides Monster", !rooms.contains("Monster"));
        check("room options remain available", !rooms.isEmpty());
        check("elite is paired with a safe choice",
            rooms.contains("Elite") && rooms.size() == 2);

        check("14 percent roll offers elite",
            new EnemyEncounterSystem(roll(14))
                .shouldOfferElite(4, false));
        check("15 percent roll does not offer elite",
            !new EnemyEncounterSystem(roll(15))
                .shouldOfferElite(4, false));
        check("early floor cannot offer elite",
            !run.shouldOfferElite(3, false));
        check("floor 8 cannot offer elite",
            !run.shouldOfferElite(8, false));
        check("floor 13 cannot offer elite",
            !run.shouldOfferElite(13, false));
        check("previous elite offer blocks another",
            !run.shouldOfferElite(5, true));
        check("early elite is selected",
            run.drawElite(4).getName().equals("Hokie Stone Guardian"));
        check("selected elite cannot recur in band",
            !run.shouldOfferElite(5, false));
        check("middle pool is independent",
            run.hasNormal(5) && run.drawNormal(5) != null);

        System.out.println("PASS: " + checks + " encounter checks.");
    }

    private static Random roll(final int eliteRoll)
    {
        return new Random()
        {
            @Override
            public int nextInt(int bound)
            {
                return bound == 100 ? eliteRoll : 0;
            }
        };
    }

    private static void check(String label, boolean passed)
    {
        checks++;
        if (!passed)
        {
            throw new AssertionError(label);
        }
    }
}
