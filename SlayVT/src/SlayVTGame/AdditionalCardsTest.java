package SlayVTGame;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

/** Run with: java -cp <compiled-classes> SlayVTGame.AdditionalCardsTest */
public class AdditionalCardsTest
{
    private static int checks;

    public static void main(String[] args)
    {
        testDefinitionsAndAvailability();
        testReversalPowers();
        testHeatPowersAndAttacks();
        testSkills();
        testTurnPowersAndInnate();
        System.out.println("PASS: " + checks + " additional card checks.");
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

    private static ArrayList<Enemy> enemies(Enemy... values)
    {
        ArrayList<Enemy> result = new ArrayList<Enemy>();
        for (Enemy enemy : values) result.add(enemy);
        return result;
    }

    private static Card upgraded(CardLibrary type)
    {
        Card card = new Card(type);
        yes(type + " upgrades", card.upgrade());
        return card;
    }

    private static void equal(String label, int expected, int actual)
    {
        yes(label + " (expected " + expected + ", got " + actual + ")",
            expected == actual);
    }

    private static void yes(String label, boolean value)
    {
        checks++;
        if (!value) throw new AssertionError(label);
    }
}
