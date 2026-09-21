package SlayVTGame;
import java.util.EnumSet;
import SlayVTGame.BuffEffect.BuffType;

public enum CardLibrary
{
    STRIKE("Strike", 1, "Attack", true),
    DEFEND("Defend", 1, "Skill", true),
    BREEZE("Breeze", 1, "Skill", false),
    BURN("Burn", 1, "Skill", false),
    HEAT_STRIKE("Heat Strike", 1, "Attack", true),
    SCORCH_PINCER("Scorch Pincer", 1, "Skill", true),
    RED_HOT_FORM("Red Hot Form", 3, "Power", true),
    OVERBURN("Overburn", 1, "Skill", true),
    HIDDEN_SCORCH("Hidden Scorch", 2, "Attack", true),
    FROZEN_HEART("Frozen Heart", 0, "Skill", true),
    SCATTER_ICE("Scatter Ice", 2, "Skill", true),
    VOID_FREEZE("Void Freeze", 1, "Skill", true);

    private final String name;
    private final int cost;
    private final String type;
    private final boolean canUpgrade;

    private CardLibrary(
        String name, int cost, String type, boolean canUpgrade)
    {
        this.name = name;
        this.cost = cost;
        this.type = type;
        this.canUpgrade = canUpgrade;
    }

    public String getName()
    {
        return name;
    }

    public int getCost()
    {
        return cost;
    }

    public int getCost(boolean upgraded)
    {
        if (this == SCATTER_ICE && upgraded)
        {
            return 1;
        }
        return cost;
    }

    public String getType()
    {
        return type;
    }

    public boolean canUpgrade()
    {
        return canUpgrade;
    }

    public CardEffect createEffect()
    {
        return createEffect(false);
    }

    public CardEffect createEffect(boolean upgraded)
    {
        switch (this)
        {
            case STRIKE:
                return new DamageEffect(upgraded ? 9 : 6);
            case DEFEND:
                return new BlockEffect(upgraded ? 8 : 5);
            case BREEZE:
                return new BuffEffect(BuffType.TEMPERATURE, -2);
            case BURN:
                return new BuffEffect(BuffType.TEMPERATURE, 2);
            case HEAT_STRIKE:
                return new HeatStrikeEffect(
                    upgraded ? 10 : 8,
                    upgraded ? 2 : 1);
            case SCORCH_PINCER:
                return new ScorchPincerEffect(upgraded ? 3 : 2);
            case RED_HOT_FORM:
                return new RedHotFormEffect();
            case OVERBURN:
                return new OverburnEffect(
                    upgraded ? 5 : 4,
                    upgraded ? 3 : 2);
            case HIDDEN_SCORCH:
                return new HiddenScorchEffect(upgraded ? 15 : 12);
            case FROZEN_HEART:
                return new FrozenHeartEffect(upgraded ? 3 : 2);
            case SCATTER_ICE:
                return new ScatterIceEffect();
            case VOID_FREEZE:
                return new VoidFreezeEffect(3, upgraded ? 2 : 1);
            default:
                throw new IllegalStateException("Unknown card: " + this);
        }
    }

    public EnumSet<CardKeyword> createKeywords(boolean upgraded)
    {
        EnumSet<CardKeyword> keywords =
            EnumSet.noneOf(CardKeyword.class);

        if (this == SCORCH_PINCER || this == SCATTER_ICE)
        {
            keywords.add(CardKeyword.EXHAUST);
        }

        if (this == RED_HOT_FORM && upgraded)
        {
            keywords.add(CardKeyword.RETAIN);
        }

        return keywords;
    }
}
