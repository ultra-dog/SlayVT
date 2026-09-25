package SlayVTGame;
import java.util.EnumSet;
import SlayVTGame.BuffEffect.BuffType;

public enum CardLibrary
{
    STRIKE("Strike", 1, "Attack", true),
    DEFEND("Defend", 1, "Skill", true),
    BREEZE("Breeze", 1, "Skill", true),
    BURN("Burn", 1, "Skill", true),
    HEAT_STRIKE("Heat Strike", 1, "Attack", true),
    SCORCH_PINCER("Scorch Pincer", 1, "Skill", true),
    RED_HOT_FORM("Red Hot Form", 3, "Power", true),
    OVERBURN("Overburn", 1, "Skill", true),
    HIDDEN_SCORCH("Hidden Scorch", 2, "Attack", true),
    FROZEN_HEART("Frozen Heart", 0, "Skill", true),
    SCATTER_ICE("Scatter Ice", 2, "Skill", true),
    VOID_FREEZE("Void Freeze", 1, "Skill", true),
    FRONT_FORM("Front Form", 3, "Power", true),
    DROUGHT("Drought", 2, "Power", true),
    PHASE_ARMOR("Phase Armor", 1, "Power", true),
    HEAT_EXCHANGER("Heat Exchanger", 1, "Power", true),
    GLACIER_COLLAPSE("Glacier Collapse", 2, "Attack", true),
    CORONAL_ERUPTION("Coronal Eruption", 2, "Attack", true),
    FORCED_SEASONS("Forced Seasons", 2, "Skill", true),
    CLIMATE_SHELTER("Climate Shelter", 2, "Skill", true),
    BLAZING_SUN("Blazing Sun", 3, "Skill", true),
    BITING_WIND("Biting Wind", 3, "Skill", true),
    SUNBURN("Sunburn", 0, "Skill", true),
    NO_AC("No AC", 1, "Power", true),
    FROSTBITE("Frostbite", 0, "Skill", true),
    COLD_ADAPTATION("Cold Adaptation", 1, "Power", true);

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
        if (this == HEAT_EXCHANGER && upgraded)
        {
            return 0;
        }
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

    public CardRarity getRarity()
    {
        switch (this)
        {
            case FRONT_FORM:
            case DROUGHT:
            case HEAT_EXCHANGER:
            case GLACIER_COLLAPSE:
            case CORONAL_ERUPTION:
            case FORCED_SEASONS:
            case CLIMATE_SHELTER:
            case BLAZING_SUN:
            case BITING_WIND:
                return CardRarity.RARE;
            case PHASE_ARMOR:
            case SUNBURN:
            case NO_AC:
            case FROSTBITE:
            case COLD_ADAPTATION:
                return CardRarity.UNCOMMON;
            case STRIKE:
            case DEFEND:
                return CardRarity.BASIC;
            default:
                return CardRarity.COMMON;
        }
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
                return new BuffEffect(BuffType.TEMPERATURE,
                    upgraded ? -3 : -2);
            case BURN:
                return new BuffEffect(BuffType.TEMPERATURE,
                    upgraded ? 3 : 2);
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
            case FRONT_FORM:
                return new FrontFormEffect(upgraded ? 2 : 1);
            case DROUGHT:
                return new DroughtEffect(upgraded ? 50 : 25);
            case PHASE_ARMOR:
                return new PhaseArmorEffect(upgraded ? 5 : 4);
            case HEAT_EXCHANGER:
                return new HeatExchangerEffect();
            case GLACIER_COLLAPSE:
                return new GlacierCollapseEffect(upgraded ? 16 : 12);
            case CORONAL_ERUPTION:
                return new CoronalEruptionEffect(upgraded ? 16 : 12);
            case FORCED_SEASONS:
                return new ForcedSeasonsEffect();
            case CLIMATE_SHELTER:
                return new ClimateShelterEffect(upgraded ? 5 : 3);
            case BLAZING_SUN:
                return new AllTemperatureEffect(upgraded ? 8 : 6);
            case BITING_WIND:
                return new AllTemperatureEffect(upgraded ? -8 : -6);
            case SUNBURN:
                return new ConditionalStatusEffect(true,
                    upgraded ? 3 : 2);
            case NO_AC:
                return new NoAcEffect();
            case FROSTBITE:
                return new ConditionalStatusEffect(false,
                    upgraded ? 3 : 2);
            case COLD_ADAPTATION:
                return new ColdAdaptationEffect(upgraded ? 6 : 4);
            default:
                throw new IllegalStateException("Unknown card: " + this);
        }
    }

    public EnumSet<CardKeyword> createKeywords(boolean upgraded)
    {
        EnumSet<CardKeyword> keywords =
            EnumSet.noneOf(CardKeyword.class);

        if (this == SCORCH_PINCER || this == SCATTER_ICE
            || this == FORCED_SEASONS || this == CLIMATE_SHELTER)
        {
            keywords.add(CardKeyword.EXHAUST);
        }

        if (this == RED_HOT_FORM && upgraded)
        {
            keywords.add(CardKeyword.RETAIN);
        }

        if (this == FORCED_SEASONS && upgraded)
        {
            keywords.add(CardKeyword.RETAIN);
        }

        if (this == NO_AC && upgraded)
        {
            keywords.add(CardKeyword.INNATE);
        }

        return keywords;
    }
}
