package SlayVTGame;
import SlayVTGame.BuffEffect.BuffType;

public enum CardLibrary
{
    STRIKE("Strike", 1, "Attack", false),
    DEFEND("Defend", 1, "Skill", false),
    BREEZE("Breeze", 1, "Skill", false),
    BURN("Burn", 1, "Skill", false),
    HEAT_STRIKE("Heat Strike", 1, "Attack", false),
    SCORCH_PINCER("Scorch Pincer", 1, "Skill", true),
    RED_HOT_FORM("Red Hot Form", 3, "Power", false),
    OVERBURN("Overburn", 1, "Skill", false),
    HIDDEN_SCORCH("Hidden Scorch", 2, "Attack", false),
    FROZEN_HEART("Frozen Heart", 0, "Skill", false),
    SCATTER_ICE("Scatter Ice", 2, "Skill", true),
    VOID_FREEZE("Void Freeze", 1, "Skill", false);

    private final String name;
    private final int cost;
    private final String type;
    private final boolean exhaust;

    private CardLibrary(
        String name, int cost, String type, boolean exhaust)
    {
        this.name = name;
        this.cost = cost;
        this.type = type;
        this.exhaust = exhaust;
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

    public boolean isExhaust()
    {
        return exhaust;
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
            case HEAT_STRIKE:
                return new HeatStrikeEffect();
            case SCORCH_PINCER:
                return new ScorchPincerEffect();
            case RED_HOT_FORM:
                return new RedHotFormEffect();
            case OVERBURN:
                return new OverburnEffect();
            case HIDDEN_SCORCH:
                return new HiddenScorchEffect();
            case FROZEN_HEART:
                return new FrozenHeartEffect();
            case SCATTER_ICE:
                return new ScatterIceEffect();
            case VOID_FREEZE:
                return new VoidFreezeEffect();
            default:
                throw new IllegalStateException("Unknown card: " + this);
        }
    }
}
