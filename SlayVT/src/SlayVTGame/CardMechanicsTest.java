package SlayVTGame;

import java.util.ArrayList;

/** Run with: java -cp <compiled-classes> SlayVTGame.CardMechanicsTest */
public class CardMechanicsTest
{
    private static int checks;
    private static int failures;

    public static void main(String[] args)
    {
        testCardDefinitions();
        testHeatStrikeAndOverburn();
        testVoidFreeze();
        testRedHotForm();
        testHeatEndOfTurnBuffs();
        testHiddenScorch();
        testFrozenHeart();
        testScatterIceAndExhaustPile();
        testCardUpgrades();

        if (failures > 0)
        {
            throw new AssertionError(failures + " of " + checks
                + " card mechanic checks failed.");
        }

        System.out.println("PASS: " + checks + " card mechanic checks.");
    }

    private static void testCardDefinitions()
    {
        checkCard(CardLibrary.HEAT_STRIKE, 1, "Attack", false);
        checkCard(CardLibrary.SCORCH_PINCER, 1, "Skill", true);
        checkCard(CardLibrary.RED_HOT_FORM, 3, "Power", false);
        checkCard(CardLibrary.OVERBURN, 1, "Skill", false);
        checkCard(CardLibrary.HIDDEN_SCORCH, 2, "Attack", false);
        checkCard(CardLibrary.FROZEN_HEART, 0, "Skill", false);
        checkCard(CardLibrary.SCATTER_ICE, 2, "Skill", true);
        checkCard(CardLibrary.VOID_FREEZE, 1, "Skill", false);
    }

    private static void testHeatStrikeAndOverburn()
    {
        Player player = new Player("Player", 100);
        Enemy enemy = new Enemy("Enemy", 100, 0);

        new Card(CardLibrary.HEAT_STRIKE).apply(player, enemy);
        check("Heat Strike damage", 92, enemy.getHp());
        check("Heat Strike heat", 1, enemy.getBuffs().getTemperature());
        check("Heat Strike marks heat applied", true,
            player.getBuffs().wasHeatAppliedThisTurn());

        new Card(CardLibrary.OVERBURN).apply(player, enemy);
        check("Overburn applies four heat to hot enemy", 5,
            enemy.getBuffs().getTemperature());

        Player secondPlayer = new Player("Player", 100);
        Enemy secondEnemy = new Enemy("Enemy", 100, 0);
        new Card(CardLibrary.OVERBURN).apply(secondPlayer, secondEnemy);
        check("Overburn applies two heat to neutral enemy", 2,
            secondEnemy.getBuffs().getTemperature());
    }

    private static void testVoidFreeze()
    {
        Player player = new Player("Player", 100);
        Enemy enemy = new Enemy("Enemy", 100, 0);

        new Card(CardLibrary.VOID_FREEZE).apply(player, enemy);
        check("Void Freeze cold", -3, enemy.getBuffs().getTemperature());
        check("Void Freeze weak", 1, enemy.getBuffs().getWeakTurns());
    }

    private static void testRedHotForm()
    {
        Player player = new Player("Player", 100);
        Enemy enemy = new Enemy("Enemy", 100, 0);

        new Card(CardLibrary.RED_HOT_FORM).apply(player, (Enemy)null);
        check("Red Hot Form becomes a player buff", true,
            player.getBuffs().hasRedHotForm());

        player.setEnergy(3);
        new Card(CardLibrary.BREEZE).apply(player, enemy);
        check("Red Hot Form converts enemy cooling to heat", 2,
            enemy.getBuffs().getTemperature());

        player.getBuffs().endTurn();
        check("Red Hot Form persists between turns", true,
            player.getBuffs().hasRedHotForm());
    }

    private static void testHeatEndOfTurnBuffs()
    {
        Player player = new Player("Player", 100);
        Enemy enemy = new Enemy("Enemy", 100, 0);
        ArrayList<Enemy> enemies = enemies(enemy);
        BattleContext context = new BattleContext(
            player, enemies, new BattlePiles());

        player.getBuffs().enableRedHotForm();
        enemy.getBuffs().setTemperature(8);
        enemy.addBlock(10);
        new Card(CardLibrary.SCORCH_PINCER).apply(player, (Enemy)null);

        context.resolvePlayerEndOfTurn();
        check("Red Hot Form and Scorch Pincer multiply heat damage", 52,
            enemy.getHp());
        check("Heat end-of-turn damage is unblockable", 10,
            enemy.getBlock());
        check("Scorch Pincer expires at end of turn", 1,
            player.getBuffs().getTemporaryHeatMultiplier());
        check("Ability buff remains after temporary buff expires", true,
            player.getBuffs().hasRedHotForm());
    }

    private static void testHiddenScorch()
    {
        Player player = new Player("Player", 100);
        Enemy first = new Enemy("First", 100, 0);
        Enemy second = new Enemy("Second", 100, 0);
        ArrayList<Enemy> enemies = enemies(first, second);
        BattleContext context = new BattleContext(
            player, enemies, new BattlePiles());

        TemperatureEffect.apply(player, first, 1, true);
        int energyBefore = player.getEnergy();
        new Card(CardLibrary.HIDDEN_SCORCH).apply(context, (Enemy)null);

        check("Hidden Scorch damages first enemy", 88, first.getHp());
        check("Hidden Scorch damages second enemy", 88, second.getHp());
        check("Hidden Scorch refunds energy after applying heat",
            energyBefore, player.getEnergy());
    }

    private static void testFrozenHeart()
    {
        Player player = new Player("Player", 100);
        ArrayList<Enemy> enemies = new ArrayList<Enemy>();
        BattlePiles piles = new BattlePiles();
        piles.initialize(new Deck(1));
        BattleContext context = new BattleContext(player, enemies, piles);

        player.getBuffs().enableRedHotForm();
        new Card(CardLibrary.FROZEN_HEART).apply(context, (Enemy)null);

        check("Frozen Heart applies cold to its owner", -2,
            player.getBuffs().getTemperature());
        check("Frozen Heart cooling ignores Red Hot Form", -2,
            player.getBuffs().getTemperature());
        check("Frozen Heart grants energy", 5, player.getEnergy());
        check("Frozen Heart draws a card", 1, piles.getHand().size());
    }

    private static void testScatterIceAndExhaustPile()
    {
        Player player = new Player("Player", 100);
        Enemy target = new Enemy("Target", 100, 0);
        Enemy other = new Enemy("Other", 100, 0);
        ArrayList<Enemy> enemies = enemies(target, other);
        BattlePiles piles = new BattlePiles();
        BattleContext context = new BattleContext(player, enemies, piles);
        Card scatterIce = new Card(CardLibrary.SCATTER_ICE);

        target.getBuffs().setTemperature(-5);
        scatterIce.apply(context, target);
        piles.movePlayedCard(scatterIce);

        check("Scatter Ice leaves its target unchanged", -5,
            target.getBuffs().getTemperature());
        check("Scatter Ice spreads half cold", -2,
            other.getBuffs().getTemperature());
        check("Exhaust card enters exhaust pile", 1,
            piles.getExhaustPile().size());
        check("Exhaust card does not enter discard pile", 0,
            piles.getDiscardPile().size());

        Card strike = new Card(CardLibrary.STRIKE);
        piles.movePlayedCard(strike);
        check("Normal card enters discard pile", 1,
            piles.getDiscardPile().size());
        check("Normal card does not enter exhaust pile", 1,
            piles.getExhaustPile().size());
    }

    private static void testCardUpgrades()
    {
        RestSystem restSystem = new RestSystem();

        Card strike = new Card(CardLibrary.STRIKE);
        check("Rest Site upgrades Strike", true,
            restSystem.upgradeCard(strike));
        check("Upgraded Strike name", "Strike+", strike.getName());
        Player strikePlayer = new Player("Player", 100);
        Enemy strikeEnemy = new Enemy("Enemy", 100, 0);
        strike.apply(strikePlayer, strikeEnemy);
        check("Upgraded Strike deals 9 damage", 91, strikeEnemy.getHp());
        check("A card cannot be upgraded twice", false, strike.upgrade());

        Card defend = upgraded(CardLibrary.DEFEND);
        Player defendPlayer = new Player("Player", 100);
        defend.apply(defendPlayer, (Enemy)null);
        check("Upgraded Defend gains 8 block", 8, defendPlayer.getBlock());

        Card heatStrike = upgraded(CardLibrary.HEAT_STRIKE);
        Player heatPlayer = new Player("Player", 100);
        Enemy heatEnemy = new Enemy("Enemy", 100, 0);
        heatStrike.apply(heatPlayer, heatEnemy);
        check("Upgraded Heat Strike deals 10 damage", 90,
            heatEnemy.getHp());
        check("Upgraded Heat Strike applies 2 Heat", 2,
            heatEnemy.getBuffs().getTemperature());

        Card scorchPincer = upgraded(CardLibrary.SCORCH_PINCER);
        Player scorchPlayer = new Player("Player", 100);
        scorchPincer.apply(scorchPlayer, (Enemy)null);
        check("Upgraded Scorch Pincer uses 3x multiplier", 3,
            scorchPlayer.getBuffs().getTemporaryHeatMultiplier());

        Card redHotForm = upgraded(CardLibrary.RED_HOT_FORM);
        check("Upgraded Red Hot Form gains Retain", true,
            redHotForm.isRetain());

        Card overburn = upgraded(CardLibrary.OVERBURN);
        Player overburnPlayer = new Player("Player", 100);
        Enemy overburnEnemy = new Enemy("Enemy", 100, 0);
        overburn.apply(overburnPlayer, overburnEnemy);
        check("Upgraded Overburn applies 3 Heat when neutral", 3,
            overburnEnemy.getBuffs().getTemperature());
        overburn.apply(overburnPlayer, overburnEnemy);
        check("Upgraded Overburn applies 5 Heat when hot", 8,
            overburnEnemy.getBuffs().getTemperature());

        Card hiddenScorch = upgraded(CardLibrary.HIDDEN_SCORCH);
        Player hiddenPlayer = new Player("Player", 100);
        Enemy first = new Enemy("First", 100, 0);
        Enemy second = new Enemy("Second", 100, 0);
        BattleContext hiddenContext = new BattleContext(
            hiddenPlayer, enemies(first, second), new BattlePiles());
        hiddenScorch.apply(hiddenContext, (Enemy)null);
        check("Upgraded Hidden Scorch damages first enemy", 85,
            first.getHp());
        check("Upgraded Hidden Scorch damages second enemy", 85,
            second.getHp());

        Card frozenHeart = upgraded(CardLibrary.FROZEN_HEART);
        Player frozenPlayer = new Player("Player", 100);
        BattlePiles frozenPiles = new BattlePiles();
        frozenPiles.initialize(new Deck(1));
        BattleContext frozenContext = new BattleContext(
            frozenPlayer, new ArrayList<Enemy>(), frozenPiles);
        frozenHeart.apply(frozenContext, (Enemy)null);
        check("Upgraded Frozen Heart grants 3 Energy", 6,
            frozenPlayer.getEnergy());
        check("Upgraded Frozen Heart still draws one card", 1,
            frozenPiles.getHand().size());

        Card scatterIce = upgraded(CardLibrary.SCATTER_ICE);
        check("Upgraded Scatter Ice costs 1", 1, scatterIce.getCost());
        check("Upgraded Scatter Ice remains Exhaust", true,
            scatterIce.isExhaust());

        Card voidFreeze = upgraded(CardLibrary.VOID_FREEZE);
        Player voidPlayer = new Player("Player", 100);
        Enemy voidEnemy = new Enemy("Enemy", 100, 0);
        voidFreeze.apply(voidPlayer, voidEnemy);
        check("Upgraded Void Freeze keeps 3 Cold", -3,
            voidEnemy.getBuffs().getTemperature());
        check("Upgraded Void Freeze applies 2 Weak", 2,
            voidEnemy.getBuffs().getWeakTurns());

        Card breeze = new Card(CardLibrary.BREEZE);
        check("Breeze cannot be upgraded", false, breeze.canUpgrade());
        check("Breeze rejects upgrade", false, breeze.upgrade());

        BattlePiles retainPiles = new BattlePiles();
        retainPiles.mutableHand().add(redHotForm);
        retainPiles.mutableHand().add(new Card(CardLibrary.STRIKE));
        retainPiles.discardHand();
        check("Retained card remains in hand", 1,
            retainPiles.getHand().size());
        check("Only non-Retain card is discarded", 1,
            retainPiles.getDiscardPile().size());
    }

    private static Card upgraded(CardLibrary cardType)
    {
        Card card = new Card(cardType);
        if (!card.upgrade())
        {
            throw new AssertionError("Card should be upgradeable: " + cardType);
        }
        return card;
    }

    private static ArrayList<Enemy> enemies(Enemy... values)
    {
        ArrayList<Enemy> enemies = new ArrayList<Enemy>();
        for (Enemy enemy : values)
        {
            enemies.add(enemy);
        }
        return enemies;
    }

    private static void checkCard(
        CardLibrary cardType, int cost, String type, boolean exhaust)
    {
        Card card = new Card(cardType);
        check(cardType + " cost", cost, card.getCost());
        check(cardType + " type", type, card.getType());
        check(cardType + " exhaust", exhaust, card.isExhaust());
    }

    private static void check(String label, int expected, int actual)
    {
        checks++;
        if (expected != actual)
        {
            failures++;
            System.err.println(label + ": expected " + expected
                + ", got " + actual);
        }
    }

    private static void check(
        String label, boolean expected, boolean actual)
    {
        checks++;
        if (expected != actual)
        {
            failures++;
            System.err.println(label + ": expected " + expected
                + ", got " + actual);
        }
    }

    private static void check(
        String label, String expected, String actual)
    {
        checks++;
        if (!expected.equals(actual))
        {
            failures++;
            System.err.println(label + ": expected " + expected
                + ", got " + actual);
        }
    }
}
