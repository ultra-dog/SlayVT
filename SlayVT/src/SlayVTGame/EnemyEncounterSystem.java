package SlayVTGame;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public class EnemyEncounterSystem
{
    private final Random random;
    private final ArrayList<Enemy> early = EnemyRoster.normalPool(1);
    private final ArrayList<Enemy> middle = EnemyRoster.normalPool(5);
    private final ArrayList<Enemy> late = EnemyRoster.normalPool(10);
    private boolean earlyEliteSelected;
    private boolean lateEliteSelected;

    public EnemyEncounterSystem(Random random)
    {
        if (random == null)
        {
            throw new IllegalArgumentException("Random must not be null.");
        }
        this.random = random;
    }

    public boolean hasNormal(int floor)
    {
        ArrayList<Enemy> pool = poolFor(floor);
        return pool != null && !pool.isEmpty();
    }

    public Enemy drawNormal(int floor)
    {
        ArrayList<Enemy> pool = poolFor(floor);
        if (pool == null || pool.isEmpty())
        {
            throw new IllegalStateException("No normal enemy on floor "
                + floor + ".");
        }
        return pool.remove(random.nextInt(pool.size()));
    }

    public boolean shouldOfferElite(int floor, boolean previousOffered)
    {
        if (previousOffered)
        {
            return false;
        }
        boolean eligible = (floor >= 4 && floor <= 7
            && !earlyEliteSelected)
            || (floor >= 10 && floor <= 12 && !lateEliteSelected);
        return eligible && random.nextInt(100) < 15;
    }

    public Enemy drawElite(int floor)
    {
        if (floor >= 4 && floor <= 7 && !earlyEliteSelected)
        {
            earlyEliteSelected = true;
            return EnemyRoster.earlyElite();
        }
        if (floor >= 10 && floor <= 12 && !lateEliteSelected)
        {
            lateEliteSelected = true;
            return EnemyRoster.lateElite();
        }
        throw new IllegalStateException("No elite available on floor "
            + floor + ".");
    }

    public ArrayList<String> roomOptions(int floor, boolean restOffered,
        boolean shopOffered, boolean eliteOffered)
    {
        return roomOptions(floor, restOffered, shopOffered,
            eliteOffered, true);
    }

    public ArrayList<String> roomOptions(int floor, boolean restOffered,
        boolean shopOffered, boolean eliteOffered,
        boolean eventsAvailable)
    {
        ArrayList<String> available = new ArrayList<String>();
        if (hasNormal(floor))
        {
            available.add("Monster");
        }
        if (eventsAvailable)
        {
            available.add("Event");
        }
        if (!restOffered && floor != 13)
        {
            available.add("RestSite");
        }
        if (!shopOffered)
        {
            available.add("Shop");
        }

        if (available.isEmpty())
        {
            available.add("Shop");
        }

        if (shouldOfferElite(floor, eliteOffered))
        {
            ArrayList<String> options = new ArrayList<String>();
            options.add("Elite");
            options.add(available.get(random.nextInt(available.size())));
            Collections.shuffle(options, random);
            return options;
        }

        Collections.shuffle(available, random);
        int count = 1 + random.nextInt(Math.min(2, available.size()));
        return new ArrayList<String>(available.subList(0, count));
    }

    private ArrayList<Enemy> poolFor(int floor)
    {
        if (floor >= 1 && floor <= 4)
        {
            return early;
        }
        if (floor >= 5 && floor <= 8)
        {
            return middle;
        }
        if (floor >= 10 && floor <= 13)
        {
            return late;
        }
        return null;
    }
}
