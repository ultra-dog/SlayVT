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
