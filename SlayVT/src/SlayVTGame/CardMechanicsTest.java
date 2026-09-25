package SlayVTGame;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

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
        testDefinitionsAndAvailability();
        testReversalPowers();
        testHeatPowersAndAttacks();
        testSkills();
        testTurnPowersAndInnate();
        testUnifiedEffectClass();

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

        Card breeze = upgraded(CardLibrary.BREEZE);
        Player breezePlayer = new Player("Player", 100);
        Enemy breezeEnemy = new Enemy("Enemy", 100, 0);
        breeze.apply(breezePlayer, breezeEnemy);
        check("Upgraded Breeze applies 3 Cold", -3,
            breezeEnemy.getBuffs().getTemperature());
        check("Upgraded Breeze cannot upgrade twice", false,
            breeze.canUpgrade());

        Card burn = upgraded(CardLibrary.BURN);
        Player burnPlayer = new Player("Player", 100);
        Enemy burnEnemy = new Enemy("Enemy", 100, 0);
        burn.apply(burnPlayer, burnEnemy);
        check("Upgraded Burn applies 3 Heat", 3,
            burnEnemy.getBuffs().getTemperature());

        BattlePiles retainPiles = new BattlePiles();
        retainPiles.mutableHand().add(redHotForm);
        retainPiles.mutableHand().add(new Card(CardLibrary.STRIKE));
        retainPiles.discardHand();
        check("Retained card remains in hand", 1,
            retainPiles.getHand().size());
        check("Only non-Retain card is discarded", 1,
            retainPiles.getDiscardPile().size());
    }

    private static void testDefinitionsAndAvailability()
    {
        CardLibrary[] cards = {
            CardLibrary.FRONT_FORM, CardLibrary.DROUGHT,
            CardLibrary.PHASE_ARMOR, CardLibrary.HEAT_EXCHANGER,
            CardLibrary.GLACIER_COLLAPSE, CardLibrary.CORONAL_ERUPTION,
            CardLibrary.FORCED_SEASONS, CardLibrary.CLIMATE_SHELTER,
            CardLibrary.BLAZING_SUN, CardLibrary.BITING_WIND,
            CardLibrary.SUNBURN, CardLibrary.NO_AC,
            CardLibrary.FROSTBITE, CardLibrary.COLD_ADAPTATION
        };
        Set<CardLibrary> rewards = new HashSet<CardLibrary>(
            new CardRewardSystem(new Random(3)).generateChoices(
                CardLibrary.values().length - 2));
        Set<CardLibrary> shopCards = new HashSet<CardLibrary>();
        for (int seed = 0; seed < 500; seed++)
        {
            for (ShopItem item :
                new ShopSystem(new Random(seed)).beginVisit())
            {
                shopCards.add(item.getCardType());
            }
        }
        for (CardLibrary type : cards)
        {
            Card card = new Card(type);
            yes(type + " reward", rewards.contains(type));
            yes(type + " shop inventory", shopCards.contains(type));
            yes(type + " English description", card.getEffect()
                .matches("\\p{ASCII}+"));
            yes(type + " upgrade", card.upgrade());
            yes(type + " upgraded name", card.getName().endsWith("+"));
            Deck deck = new Deck(1);
            deck.addCard(type);
            BattlePiles piles = new BattlePiles();
            piles.initialize(deck);
            yes(type + " can enter battle", piles.getDrawPile().contains(
                deck.getDeck()[deck.size() - 1])
                || piles.getHand().contains(deck.getDeck()[deck.size() - 1]));
            ShopSystem shop = new ShopSystem(new Random(1));
            Player buyer = new Player("Buyer", 100);
            ShopItem item = new ShopItem(type, 50);
            yes(type + " purchase", shop.purchaseCard(buyer, deck, item));
            yes(type + " added to deck", deck.getDeck()[deck.size() - 1]
                .getName().equals(type.getName()));
            shop.beginVisit();
            buyer.addGold(100);
            yes(type + " removable", shop.removeCard(buyer, deck,
                deck.getDeck()[deck.size() - 1]));
        }
        for (CardLibrary type : new CardLibrary[] {
            CardLibrary.PHASE_ARMOR, CardLibrary.SUNBURN,
            CardLibrary.NO_AC, CardLibrary.FROSTBITE,
            CardLibrary.COLD_ADAPTATION })
        {
            yes(type + " blue rarity",
                type.getRarity() == CardRarity.UNCOMMON);
        }
        for (CardLibrary type : new CardLibrary[] {
            CardLibrary.FRONT_FORM, CardLibrary.DROUGHT,
            CardLibrary.HEAT_EXCHANGER, CardLibrary.GLACIER_COLLAPSE,
            CardLibrary.CORONAL_ERUPTION, CardLibrary.FORCED_SEASONS,
            CardLibrary.CLIMATE_SHELTER, CardLibrary.BLAZING_SUN,
            CardLibrary.BITING_WIND })
        {
            yes(type + " gold rarity", type.getRarity() == CardRarity.RARE);
        }
    }

    private static void testReversalPowers()
    {
        Player player = player();
        Enemy enemy = enemy();
        BattleContext context = context(player, enemy);
        new Card(CardLibrary.FRONT_FORM).apply(context, null);
        new Card(CardLibrary.PHASE_ARMOR).apply(context, null);
        yes("powers visible as player buffs", player.getBuffs()
            .toString().contains("Front Form") && player.getBuffs()
            .toString().contains("Phase Armor"));
        enemy.getBuffs().setTemperature(2);
        TemperatureEffect.apply(context, enemy, -2, true);
        equal("first reversal doubled", 76, enemy.getHp());
        equal("phase armor gains block", 4, player.getBlock());
        TemperatureEffect.apply(context, enemy, 2, true);
        equal("second reversal normal", 64, enemy.getHp());
        equal("phase armor repeats", 8, player.getBlock());
        player.getBuffs().startTurn();
        TemperatureEffect.apply(context, enemy, -2, true);
        equal("form resets each turn", 40, enemy.getHp());

        Player upgradedPlayer = player();
        Enemy upgradedEnemy = enemy();
        BattleContext upgradedContext = context(upgradedPlayer,
            upgradedEnemy);
        upgraded(CardLibrary.FRONT_FORM).apply(upgradedContext, null);
        upgraded(CardLibrary.PHASE_ARMOR).apply(upgradedContext, null);
        upgradedEnemy.getBuffs().setTemperature(1);
        TemperatureEffect.apply(upgradedContext, upgradedEnemy, -1, true);
        TemperatureEffect.apply(upgradedContext, upgradedEnemy, 1, true);
        equal("upgraded form doubles twice", 76, upgradedEnemy.getHp());
        equal("upgraded armor gives five twice", 10,
            upgradedPlayer.getBlock());

        Player exchangerPlayer = player();
        Enemy exchangerEnemy = enemy();
        BattlePiles piles = new BattlePiles();
        piles.initialize(new Deck(1));
        BattleContext exchangerContext = new BattleContext(exchangerPlayer,
            enemies(exchangerEnemy), piles);
        new Card(CardLibrary.HEAT_EXCHANGER).apply(exchangerContext, null);
        exchangerEnemy.getBuffs().setTemperature(-1);
        TemperatureEffect.apply(exchangerContext, exchangerEnemy, 1, true);
        equal("cold to heat draws two", 2, piles.getHand().size());
        TemperatureEffect.apply(exchangerContext, exchangerEnemy, -1, true);
        equal("heat to cold gains energy", 20,
            exchangerPlayer.getEnergy());
        TemperatureEffect.apply(exchangerContext, exchangerEnemy, 1, true);
        equal("draw once per direction", 2, piles.getHand().size());
        exchangerPlayer.getBuffs().startTurn();
        TemperatureEffect.apply(exchangerContext, exchangerEnemy, -1, true);
        TemperatureEffect.apply(exchangerContext, exchangerEnemy, 1, true);
        equal("exchanger resets next turn", 4, piles.getHand().size());
        equal("upgraded exchanger costs zero", 0,
            upgraded(CardLibrary.HEAT_EXCHANGER).getCost());
    }

    private static void testHeatPowersAndAttacks()
    {
        Player player = player();
        Enemy enemy = enemy();
        BattleContext context = context(player, enemy);
        new Card(CardLibrary.DROUGHT).apply(context, null);
        enemy.getBuffs().setTemperature(4);
        context.resolvePlayerEndOfTurn();
        equal("drought increases burn by 25 percent", 95, enemy.getHp());
        player.getBuffs().startTurn();
        enemy.getBuffs().setTemperature(0);
        enemy.getBuffs().setTemperature(4);
        context.resolvePlayerEndOfTurn();
        equal("drought persists", 90, enemy.getHp());
        Player upgradedPlayer = player();
        Enemy upgradedEnemy = enemy();
        upgraded(CardLibrary.DROUGHT).apply(upgradedPlayer, (Enemy)null);
        upgradedEnemy.getBuffs().setTemperature(4);
        context(upgradedPlayer, upgradedEnemy).resolvePlayerEndOfTurn();
        equal("upgraded drought gives 50 percent", 94,
            upgradedEnemy.getHp());

        Player glacierPlayer = player();
        Enemy glacierEnemy = enemy();
        glacierEnemy.getBuffs().setTemperature(-3);
        new Card(CardLibrary.GLACIER_COLLAPSE).apply(
            glacierPlayer, glacierEnemy);
        equal("glacier bonus damage", 79, glacierEnemy.getHp());
        equal("glacier consumes cold", 0,
            glacierEnemy.getBuffs().getTemperature());
        upgraded(CardLibrary.GLACIER_COLLAPSE).apply(
            player(), glacierEnemy);
        equal("upgraded glacier base damage", 63, glacierEnemy.getHp());

        Player coronaPlayer = player();
        Enemy coronaEnemy = enemy();
        coronaEnemy.getBuffs().setTemperature(8);
        new Card(CardLibrary.CORONAL_ERUPTION).apply(
            coronaPlayer, coronaEnemy);
        equal("corona attack and immediate heat", 72,
            coronaEnemy.getHp());
        coronaEnemy.getBuffs().setTemperature(0);
        upgraded(CardLibrary.CORONAL_ERUPTION).apply(
            player(), coronaEnemy);
        equal("upgraded corona attack", 56, coronaEnemy.getHp());
    }

    private static void testSkills()
    {
        Player player = player();
        Enemy enemy = enemy();
        BattleContext context = context(player, enemy);
        enemy.getBuffs().setTemperature(2);
        Card seasons = new Card(CardLibrary.FORCED_SEASONS);
        seasons.apply(context, enemy);
        equal("seasons flips temperature", -2,
            enemy.getBuffs().getTemperature());
        equal("seasons triggers shock", 88, enemy.getHp());
        yes("seasons exhausts", seasons.isExhaust());
        yes("upgraded seasons retains",
            upgraded(CardLibrary.FORCED_SEASONS).isRetain());
        yes("upgraded seasons shows retain text",
            upgraded(CardLibrary.FORCED_SEASONS).getEffect()
                .contains("Retain."));

        Player shelterPlayer = player();
        Enemy first = enemy();
        Enemy second = enemy();
        shelterPlayer.getBuffs().setTemperature(-1);
        first.getBuffs().setTemperature(2);
        second.getBuffs().setTemperature(-3);
        BattleContext shelterContext = new BattleContext(shelterPlayer,
            enemies(first, second), new BattlePiles());
        new Card(CardLibrary.CLIMATE_SHELTER).apply(shelterContext, null);
        equal("shelter sums absolute stacks", 18,
            shelterPlayer.getBlock());
        upgraded(CardLibrary.CLIMATE_SHELTER).apply(
            shelterContext, null);
        equal("upgraded shelter multiplier", 48,
            shelterPlayer.getBlock());

        Player areaPlayer = player();
        Enemy hotOne = enemy();
        Enemy hotTwo = enemy();
        BattleContext area = new BattleContext(areaPlayer,
            enemies(hotOne, hotTwo), new BattlePiles());
        new Card(CardLibrary.BLAZING_SUN).apply(area, null);
        equal("sun heats first enemy", 6,
            hotOne.getBuffs().getTemperature());
        equal("sun heats second enemy", 6,
            hotTwo.getBuffs().getTemperature());
        new Card(CardLibrary.BITING_WIND).apply(area, null);
        equal("wind cools first enemy", -6,
            hotOne.getBuffs().getTemperature());
        equal("wind cools second enemy", -6,
            hotTwo.getBuffs().getTemperature());
        equal("area reversal damage", 64, hotOne.getHp());
        Player upgradedAreaPlayer = player();
        Enemy upgradedAreaEnemy = enemy();
        upgraded(CardLibrary.BLAZING_SUN).apply(
            context(upgradedAreaPlayer, upgradedAreaEnemy), null);
        equal("upgraded sun applies eight", 8,
            upgradedAreaEnemy.getBuffs().getTemperature());
        upgraded(CardLibrary.BITING_WIND).apply(
            context(upgradedAreaPlayer, upgradedAreaEnemy), null);
        equal("upgraded wind applies eight", -8,
            upgradedAreaEnemy.getBuffs().getTemperature());

        Player statusPlayer = player();
        Enemy statusEnemy = enemy();
        new Card(CardLibrary.SUNBURN).apply(statusPlayer, statusEnemy);
        equal("sunburn requires heat", 0,
            statusEnemy.getBuffs().getWeakTurns());
        statusEnemy.getBuffs().setTemperature(1);
        new Card(CardLibrary.SUNBURN).apply(statusPlayer, statusEnemy);
        upgraded(CardLibrary.SUNBURN).apply(statusPlayer, statusEnemy);
        equal("sunburn base and upgrade", 5,
            statusEnemy.getBuffs().getWeakTurns());
        statusEnemy.getBuffs().setTemperature(0);
        new Card(CardLibrary.FROSTBITE).apply(statusPlayer, statusEnemy);
        equal("frostbite requires cold", 0,
            statusEnemy.getBuffs().getVulnerableTurns());
        statusEnemy.getBuffs().setTemperature(-1);
        new Card(CardLibrary.FROSTBITE).apply(statusPlayer, statusEnemy);
        upgraded(CardLibrary.FROSTBITE).apply(statusPlayer, statusEnemy);
        equal("frostbite base and upgrade", 5,
            statusEnemy.getBuffs().getVulnerableTurns());
    }

    private static void testTurnPowersAndInnate()
    {
        Card innate = upgraded(CardLibrary.NO_AC);
        yes("upgraded no ac is innate", innate.isInnate());
        yes("upgraded no ac shows innate text",
            innate.getEffect().contains("Innate."));
        yes("base no ac is not innate",
            !new Card(CardLibrary.NO_AC).isInnate());
        Deck deck = new Deck(1);
        deck.addCard(CardLibrary.NO_AC);
        deck.getDeck()[deck.size() - 1].upgrade();
        BattlePiles piles = new BattlePiles();
        piles.initialize(deck);
        yes("innate starts in hand", piles.getHand().contains(
            deck.getDeck()[deck.size() - 1]));
        Player player = player();
        Enemy enemy = enemy();
        BattleContext context = new BattleContext(player, enemies(enemy),
            piles);
        innate.apply(context, null);
        player.getBuffs().startTurn();
        context.resolvePlayerTurnStart();
        equal("no ac heats random living enemy", 1,
            enemy.getBuffs().getTemperature());
        player.getBuffs().startTurn();
        context.resolvePlayerTurnStart();
        equal("no ac fires next turn", 2,
            enemy.getBuffs().getTemperature());

        Player coldPlayer = player();
        Enemy cold = enemy();
        Enemy neutral = enemy();
        cold.getBuffs().setTemperature(-2);
        BattleContext coldContext = new BattleContext(coldPlayer,
            enemies(cold, neutral), new BattlePiles());
        new Card(CardLibrary.COLD_ADAPTATION).apply(coldContext, null);
        coldContext.resolvePlayerEndOfTurn();
        equal("adaptation blocks for cold enemies", 4,
            coldPlayer.getBlock());
        upgraded(CardLibrary.COLD_ADAPTATION).apply(coldContext, null);
        coldPlayer.setBlock(0);
        coldContext.resolvePlayerEndOfTurn();
        equal("upgraded adaptation adds six", 10,
            coldPlayer.getBlock());
        coldPlayer.getBuffs().clear();
        yes("power buffs clear after battle",
            coldPlayer.getBuffs().getColdAdaptationBlock() == 0);
    }

    private static Player player()
    {
        Player player = new Player("Player", 100);
        player.setEnergy(20);
        return player;
    }

    private static Enemy enemy()
    {
        return new Enemy("Enemy", 100, 0);
    }

    private static BattleContext context(Player player, Enemy enemy)
    {
        return new BattleContext(player, enemies(enemy), new BattlePiles());
    }

    private static void equal(String label, int expected, int actual)
    {
        check(label, expected, actual);
    }

    private static void yes(String label, boolean value)
    {
        check(label, true, value);
    }

    private static void testUnifiedEffectClass()
    {
        for (CardLibrary type : CardLibrary.values())
        {
            if (type == CardLibrary.STRIKE
                || type == CardLibrary.DEFEND
                || type == CardLibrary.BREEZE
                || type == CardLibrary.BURN)
            {
                continue;
            }
            check(type + " uses shared CardEffect", true,
                type.createEffect().getClass() == CardEffect.class);
            check(type + " upgrade uses shared CardEffect", true,
                type.createEffect(true).getClass() == CardEffect.class);
        }
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
