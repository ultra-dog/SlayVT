package SlayVTGame;
import java.util.*;

public class Buffs
{
    private int vulnerableTurns;
    private int weakTurns;
    private int temperature;

    private boolean redHotForm;
    private boolean heatAppliedThisTurn;
    private int temporaryHeatMultiplier = 1;

    private boolean vulnerableAtTurnStart;
    private boolean weakAtTurnStart;

    public void addVulnerable(int turns)
    {
        requireNonNegative(turns);
        vulnerableTurns += turns;
    }

    public void addWeak(int turns)
    {
        requireNonNegative(turns);
        weakTurns += turns;
    }

    public int getVulnerableTurns()
    {
        return vulnerableTurns;
    }

    public int getWeakTurns()
    {
        return weakTurns;
    }

    public boolean isVulnerable()
    {
        return vulnerableTurns > 0;
    }

    public boolean isWeak()
    {
        return weakTurns > 0;
    }

    public int getTemperature()
    {
        return temperature;
    }

    public void enableRedHotForm()
    {
        redHotForm = true;
    }

    public boolean hasRedHotForm()
    {
        return redHotForm;
    }

    public void markHeatAppliedThisTurn()
    {
        heatAppliedThisTurn = true;
    }

    public boolean wasHeatAppliedThisTurn()
    {
        return heatAppliedThisTurn;
    }

    public void multiplyHeatEndTurnDamage(int multiplier)
    {
        if (multiplier < 1)
        {
            throw new IllegalArgumentException(
                "Heat multiplier must be positive.");
        }

        long combined = (long)temporaryHeatMultiplier * multiplier;
        temporaryHeatMultiplier = combined > Integer.MAX_VALUE
            ? Integer.MAX_VALUE : (int)combined;
    }

    public int getTemporaryHeatMultiplier()
    {
        return temporaryHeatMultiplier;
    }

    public int getHeatEndTurnDamage(int heat)
    {
        int safeHeat = Math.max(heat, 0);
        int thresholdMultiplier = 1;

        if (safeHeat >= 16)
        {
            thresholdMultiplier = redHotForm ? 4 : 3;
        }
        else if (safeHeat >= 8)
        {
            thresholdMultiplier = redHotForm ? 3 : 2;
        }

        long damage = (long)safeHeat * thresholdMultiplier
            * temporaryHeatMultiplier;
        return damage > Integer.MAX_VALUE
            ? Integer.MAX_VALUE : (int)damage;
    }

    // Returns the temperature difference only for a hot/cold reversal.
    public int setTemperature(int temperature)
    {
        boolean oppositeTemperatures =
            (this.temperature > 0 && temperature < 0)
            || (this.temperature < 0 && temperature > 0);

        if (oppositeTemperatures)
        {
            int tempDiff = Math.abs(this.temperature - temperature);
            this.temperature = temperature;
            return tempDiff;
        }

        this.temperature = temperature == 0 ? 0 : this.temperature + temperature;
        return 0;
    }

    public static int calculateDamage(
        int baseDamage, Buffs attacker, Buffs target)
    {
        requireNonNegative(baseDamage);

        if (attacker == null || target == null)
        {
            throw new IllegalArgumentException("Buffs must not be null.");
        }

        double damage = baseDamage;

        if (attacker.isWeak())
        {
            damage *= 0.75;
        }

        if (target.isVulnerable())
        {
            damage *= 1.5;
        }

        return (int)Math.floor(damage);
    }
    
    public static int calculateDamage(
        int baseDamage, int hitCount, Buffs attacker, Buffs target)
    {
        requireNonNegative(baseDamage);

        if (attacker == null || target == null)
        {
            throw new IllegalArgumentException("Buffs must not be null.");
        }

        double damage = baseDamage;

        if (attacker.isWeak())
        {
            damage *= 0.75;
        }

        if (target.isVulnerable())
        {
            damage *= 1.5;
        }

        return (int)Math.floor(damage) * hitCount;
    }

    public void startTurn()
    {
        vulnerableAtTurnStart = isVulnerable();
        weakAtTurnStart = isWeak();
        heatAppliedThisTurn = false;
    }

    // Only statuses present at the start of the owner's turn lose duration.
    public void endTurn()
    {
        if (vulnerableAtTurnStart && vulnerableTurns > 0)
        {
            vulnerableTurns--;
        }

        if (weakAtTurnStart && weakTurns > 0)
        {
            weakTurns--;
        }

        vulnerableAtTurnStart = false;
        weakAtTurnStart = false;
        heatAppliedThisTurn = false;
        temporaryHeatMultiplier = 1;
    }

    public void clear()
    {
        vulnerableTurns = 0;
        weakTurns = 0;
        temperature = 0;
        redHotForm = false;
        heatAppliedThisTurn = false;
        temporaryHeatMultiplier = 1;
        vulnerableAtTurnStart = false;
        weakAtTurnStart = false;
    }

    @Override
    public String toString()
    {
        StringJoiner description = new StringJoiner(", ");

        if (isVulnerable())
        {
            description.add("Vulnerable: " + vulnerableTurns);
        }

        if (isWeak())
        {
            description.add("Weak: " + weakTurns);
        }

        if (temperature != 0)
        {
            description.add("Temperature: "
                + (temperature > 0 ? "+" : "") + temperature);
        }

        if (redHotForm)
        {
            description.add("Red Hot Form");
        }

        if (temporaryHeatMultiplier > 1)
        {
            description.add("Heat damage x" + temporaryHeatMultiplier);
        }

        return description.length() == 0
            ? "None" : description.toString();
    }

    private static void requireNonNegative(int amount)
    {
        if (amount < 0)
        {
            throw new IllegalArgumentException(
                "Amount must not be negative.");
        }
    }
}
