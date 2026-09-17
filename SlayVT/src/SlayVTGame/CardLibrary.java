package SlayVTGame;
import SlayVTGame.BuffEffect.BuffType;

public enum CardLibrary
{
    STRIKE("Strike", 1, "Attack"),
    DEFEND("Defend", 1, "Skill"),
    BREEZE("Breeze", 1, "Skill"),
    BURN("Burn", 1, "Skill");

    private final String name;
    private final int cost;
    private final String type;

    private CardLibrary(String name, int cost, String type)
    {
        this.name = name;
        this.cost = cost;
        this.type = type;
    }

    public String getName()
    {
        return name;
    }

    public int getCost()
    {
        return cost;
    }

    public String getType()
    {
        return type;
    }

    public CardEffect createEffect()
    {
        switch (this)
        {
            case STRIKE:
                return new DamageEffect(6);
            case DEFEND:
                return new BlockEffect(5);
            case BREEZE:
                return new BuffEffect(BuffType.TEMPERATURE, -2);
            case BURN:
                return new BuffEffect(BuffType.TEMPERATURE, 2);
            default:
                throw new IllegalStateException("Unknown card: " + this);
        }
    }
}