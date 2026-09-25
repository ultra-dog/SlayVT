package SlayVTGame;

/** Shared implementation for the game's non-basic card effects. */
public class CardEffect
{
    private final CardLibrary cardType;
    private final boolean upgraded;

    protected CardEffect()
    {
        cardType = null;
        upgraded = false;
    }

    CardEffect(CardLibrary cardType, boolean upgraded)
    {
        if (cardType == null)
        {
            throw new IllegalArgumentException("Card type must not be null.");
        }
        this.cardType = cardType;
        this.upgraded = upgraded;
    }

    public void apply(Player player, Enemy enemy)
    {
        applyCard(player, null, enemy);
    }

    public void apply(BattleContext context, Enemy enemy)
    {
        if (cardType == null)
        {
            // Basic damage, block and buff subclasses supply apply(Player).
            apply(context.getPlayer(), enemy);
            return;
        }
        applyCard(context.getPlayer(), context, enemy);
    }

    private void applyCard(Player player, BattleContext context, Enemy enemy)
    {
        if (cardType == null)
        {
            throw new IllegalStateException("No card effect was selected.");
        }
        switch (cardType)
        {
            case HEAT_STRIKE:
                new DamageEffect(value(8, 10)).apply(player, enemy);
                if (enemy.checkAlive())
                {
                    applyTemperature(player, context, enemy,
                        value(1, 2), true);
                }
                break;

            case SCORCH_PINCER:
                player.getBuffs().multiplyHeatEndTurnDamage(value(2, 3));
                break;

            case RED_HOT_FORM:
                player.getBuffs().enableRedHotForm();
                break;

            case OVERBURN:
                applyTemperature(player, context, enemy,
                    enemy.getBuffs().getTemperature() > 0
                        ? value(4, 5) : value(2, 3), true);
                break;

            case HIDDEN_SCORCH:
                if (context == null)
                {
                    if (enemy != null)
                    {
                        new DamageEffect(value(12, 15))
                            .apply(player, enemy);
                    }
                }
                else
                {
                    for (Enemy target : context.getEnemies())
                    {
                        if (target.checkAlive())
                        {
                            new DamageEffect(value(12, 15))
                                .apply(player, target);
                        }
                    }
                    if (player.getBuffs().wasHeatAppliedThisTurn())
                    {
                        player.setEnergy(player.getEnergy() + 2);
                    }
                }
                break;

            case FROZEN_HEART:
                applyTemperature(player, context, player, -2, false);
                player.setEnergy(player.getEnergy() + value(2, 3));
                if (context != null) context.drawCards(1);
                break;

            case SCATTER_ICE:
                if (context != null)
                {
                    int cold = Math.max(-enemy.getBuffs().getTemperature(), 0);
                    int spread = cold / 2;
                    for (Enemy target : context.getEnemies())
                    {
                        if (target != enemy && target.checkAlive()
                            && spread > 0)
                        {
                            applyTemperature(player, context, target,
                                -spread, true);
                        }
                    }
                }
                break;

            case VOID_FREEZE:
                applyTemperature(player, context, enemy, -3, true);
                if (enemy.checkAlive())
                {
                    enemy.getBuffs().addWeak(value(1, 2));
                }
                break;

            case FRONT_FORM:
                player.getBuffs().addFrontFormTriggers(value(1, 2));
                break;

            case DROUGHT:
                player.getBuffs().addDroughtBonus(value(25, 50));
                break;

            case PHASE_ARMOR:
                player.getBuffs().addPhaseArmor(value(4, 5));
                break;

            case HEAT_EXCHANGER:
                player.getBuffs().enableHeatExchanger();
                break;

            case GLACIER_COLLAPSE:
                int cold = Math.max(-enemy.getBuffs().getTemperature(), 0);
                new DamageEffect(value(12, 16) + cold * 3)
                    .apply(player, enemy);
                if (cold > 0) enemy.getBuffs().setTemperature(0);
                break;

            case CORONAL_ERUPTION:
                new DamageEffect(value(12, 16)).apply(player, enemy);
                if (enemy.checkAlive())
                {
                    enemy.takeUnblockableDamage(player.getBuffs()
                        .getHeatEndTurnDamage(
                            enemy.getBuffs().getTemperature()));
                }
                break;

            case FORCED_SEASONS:
                applyTemperature(player, context, enemy,
                    -enemy.getBuffs().getTemperature(), false);
                break;

            case CLIMATE_SHELTER:
                int total = Math.abs(player.getBuffs().getTemperature());
                if (context != null)
                {
                    for (Enemy target : context.getEnemies())
                    {
                        if (target.checkAlive())
                        {
                            total += Math.abs(target.getBuffs()
                                .getTemperature());
                        }
                    }
                }
                player.addBlock(total * value(3, 5));
                break;

            case BLAZING_SUN:
            case BITING_WIND:
                int amount = cardType == CardLibrary.BLAZING_SUN
                    ? value(6, 8) : -value(6, 8);
                if (context != null)
                {
                    for (Enemy target : context.getEnemies())
                    {
                        if (target.checkAlive())
                        {
                            applyTemperature(player, context, target,
                                amount, true);
                        }
                    }
                }
                else if (enemy != null && enemy.checkAlive())
                {
                    applyTemperature(player, null, enemy, amount, true);
                }
                break;

            case SUNBURN:
                if (enemy.getBuffs().getTemperature() > 0)
                {
                    enemy.getBuffs().addWeak(value(2, 3));
                }
                break;

            case FROSTBITE:
                if (enemy.getBuffs().getTemperature() < 0)
                {
                    enemy.getBuffs().addVulnerable(value(2, 3));
                }
                break;

            case NO_AC:
                player.getBuffs().addNoAc();
                break;

            case COLD_ADAPTATION:
                player.getBuffs().addColdAdaptation(value(4, 6));
                break;

            default:
                throw new IllegalStateException("Unknown card: " + cardType);
        }
    }

    private void applyTemperature(Player player, BattleContext context,
        Character target, int amount, boolean convertCooling)
    {
        if (context == null)
        {
            TemperatureEffect.apply(player, target, amount,
                convertCooling);
        }
        else
        {
            TemperatureEffect.apply(context, target, amount,
                convertCooling);
        }
    }

    private int value(int base, int upgradedValue)
    {
        return upgraded ? upgradedValue : base;
    }

    // Self-targeted effects may receive null as the enemy.
    public boolean requiresEnemyTarget()
    {
        if (cardType == null) return false;
        switch (cardType)
        {
            case HEAT_STRIKE:
            case OVERBURN:
            case SCATTER_ICE:
            case VOID_FREEZE:
            case GLACIER_COLLAPSE:
            case CORONAL_ERUPTION:
            case FORCED_SEASONS:
            case SUNBURN:
            case FROSTBITE:
                return true;
            default:
                return false;
        }
    }

    public boolean targetsAllEnemies()
    {
        return cardType == CardLibrary.HIDDEN_SCORCH
            || cardType == CardLibrary.BLAZING_SUN
            || cardType == CardLibrary.BITING_WIND;
    }

    @Override
    public String toString()
    {
        if (cardType == null)
        {
            return super.toString();
        }
        switch (cardType)
        {
            case HEAT_STRIKE:
                return "Deal " + value(8, 10) + " damage. Apply "
                    + value(1, 2) + " Heat.";
            case SCORCH_PINCER:
                return "Multiply Heat's end-of-turn damage by "
                    + value(2, 3) + " this turn. Exhaust.";
            case RED_HOT_FORM:
                return "Heat deals 3x damage at 8-15 stacks and 4x "
                    + "damage at 16+ stacks. Cards that apply Cold to "
                    + "enemies apply the same amount of Heat instead.";
            case OVERBURN:
                return "If the enemy has Heat, apply " + value(4, 5)
                    + " Heat. Otherwise, apply " + value(2, 3) + " Heat.";
            case HIDDEN_SCORCH:
                return "Deal " + value(12, 15) + " damage to all enemies. "
                    + "If you applied Heat this turn, gain 2 Energy.";
            case FROZEN_HEART:
                return "Apply 2 Cold to yourself. Gain " + value(2, 3)
                    + " Energy. Draw 1 card.";
            case SCATTER_ICE:
                return "Apply half the target's Cold, rounded down, "
                    + "to all other enemies. Exhaust.";
            case VOID_FREEZE:
                return "Apply 3 Cold and " + value(1, 2) + " Weak.";
            case FRONT_FORM:
                return "The first " + value(1, 2)
                    + " temperature reversal(s) each turn deal double "
                    + "temperature difference damage.";
            case DROUGHT:
                return "Your Heat damage is increased by "
                    + value(25, 50) + "%.";
            case PHASE_ARMOR:
                return "Whenever you trigger a temperature reversal, gain "
                    + value(4, 5) + " Block.";
            case HEAT_EXCHANGER:
                return "The first Cold-to-Heat reversal each turn draws "
                    + "2 cards. The first Heat-to-Cold reversal gains "
                    + "1 Energy.";
            case GLACIER_COLLAPSE:
                return "Deal " + value(12, 16) + " damage, plus 3 damage "
                    + "for each Cold on the target. Remove all its Cold.";
            case CORONAL_ERUPTION:
                return "Deal " + value(12, 16) + " damage. Trigger the "
                    + "target's Heat damage immediately.";
            case FORCED_SEASONS:
                return "Reverse the target's temperature, preserving its "
                    + "stacks. Trigger a temperature reversal as normal. "
                    + "Exhaust.";
            case CLIMATE_SHELTER:
                return "Gain Block equal to " + value(3, 5)
                    + " times the sum of absolute temperature stacks on "
                    + "all combatants. Exhaust.";
            case BLAZING_SUN:
                return "Apply " + value(6, 8) + " Heat to all enemies.";
            case BITING_WIND:
                return "Apply " + value(6, 8) + " Cold to all enemies.";
            case SUNBURN:
                return "If the target has Heat, apply " + value(2, 3)
                    + " Weak.";
            case FROSTBITE:
                return "If the target has Cold, apply " + value(2, 3)
                    + " Vulnerable.";
            case NO_AC:
                return "At the start of each turn, apply 1 Heat to a "
                    + "random enemy.";
            case COLD_ADAPTATION:
                return "At the end of your turn, gain " + value(4, 6)
                    + " Block for each enemy with Cold.";
            default:
                throw new IllegalStateException("Unknown card: " + cardType);
        }
    }
}
