package SlayVTGame;

import java.util.StringJoiner;

public class Buffs
{
    private int vulnerableTurns;
    private int weakTurns;
    private int temperature;

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

    // Temperature effects and stacking rules are not implemented yet.
    public void setTemperature(int temperature)
    {
        this.temperature = temperature;
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

    public void startTurn()
    {
        vulnerableAtTurnStart = isVulnerable();
        weakAtTurnStart = isWeak();
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

        // Add end-of-turn temperature effects here later.
    }

    public void clear()
    {
        vulnerableTurns = 0;
        weakTurns = 0;
        temperature = 0;
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