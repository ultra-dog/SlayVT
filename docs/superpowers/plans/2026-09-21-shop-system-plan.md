# SlayVT Shop System Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Implement a tested text-based shop where a player spends Gold to buy cards or remove one card per visit.

**Architecture:** `Player` owns persistent Gold, `Deck` owns permanent-card mutation, `ShopItem` models one offer, and `ShopSystem` owns inventory, purchase/removal rules, and console interaction. Core mutations remain independent from `System.in` so seeded tests can verify randomness and transaction safety.

**Tech Stack:** Java 8, Java standard library only, Eclipse source layout, executable `main`-method tests.

**Spec:** `docs/superpowers/specs/2026-09-21-shop-system-design.md`

## Global Constraints

- Keep every Java source under `SlayVT/src/SlayVTGame` with package `SlayVTGame`.
- Remain compatible with Java 8 and add no external dependencies.
- Offer exactly five unique cards per visit: two Attacks, two Skills, and one Power.
- Charge 50 Gold for Attacks and Skills and 75 Gold for Powers.
- Start removal at 75 Gold, increase it by 25 only after successful removal, and allow it once per visit.
- Do not implement potions, relics, discounts, restocking, rarity, or battle rewards.
- Do not stage, overwrite, or commit the user's existing `SlayVT/.project` modification.
- Follow red-green-refactor: add the next behavior test, observe the expected failure, implement only that behavior, run the full suite, review the diff, then commit that task.

## File Structure

- Create `SlayVT/src/SlayVTGame/ShopItem.java`: one card offer and its sold state.
- Create `SlayVT/src/SlayVTGame/ShopSystemTest.java`: executable regression tests for all shop-domain behavior and the leave path.
- Modify `SlayVT/src/SlayVTGame/Player.java`: persistent Gold and atomic spending.
- Modify `SlayVT/src/SlayVTGame/Deck.java`: safe identity-based card removal and deck size.
- Modify `SlayVT/src/SlayVTGame/ShopSystem.java`: seeded inventory, purchases, removal service, and text UI.
- Modify `SlayVT/src/SlayVTGame/Main.java`: keep one shop for the run and open it from Shop rooms.

## Review Focus

- Unaffordable card purchase must not change Gold, deck size, or sold state; Task 4 pins this behavior.
- A sold offer must not be purchased twice; Task 4 pins this behavior.
- A foreign or stale `Card` reference must not remove an equal-looking deck card or charge Gold; Tasks 2 and 5 pin this behavior.
- The final deck card must remain and the player must not be charged; Tasks 2 and 5 pin this behavior.
- Starting a new visit must re-enable removal while preserving the increased run-wide price; Task 5 pins this behavior.

---

### Task 1: Add Persistent Player Gold

**Files:**
- Create: `SlayVT/src/SlayVTGame/ShopSystemTest.java`
- Modify: `SlayVT/src/SlayVTGame/Player.java`

**Interfaces:**
- Consumes: existing `Player(String name, int maxHp)`.
- Produces: `int getGold()`, `void addGold(int amount)`, and `boolean spendGold(int amount)`.

- [ ] **Step 1: Write the failing Gold tests**

Create `ShopSystemTest.java` with this initial content:

```java
package SlayVTGame;

public class ShopSystemTest
{
    private interface TestAction
    {
        void run();
    }

    private static int checks;
    private static int failures;

    public static void main(String[] args)
    {
        testPlayerGold();

        if (failures > 0)
        {
            throw new AssertionError(failures + " of " + checks
                + " shop checks failed.");
        }
        System.out.println("PASS: " + checks + " shop checks.");
    }

    private static void testPlayerGold()
    {
        Player player = new Player("Player", 80);
        check("Player starts with 99 Gold", 99, player.getGold());
        check("Affordable spend succeeds", true, player.spendGold(50));
        check("Affordable spend deducts Gold", 49, player.getGold());
        check("Unaffordable spend fails", false, player.spendGold(50));
        check("Failed spend preserves Gold", 49, player.getGold());
        player.addGold(11);
        check("Gold can be added", 60, player.getGold());
        player.addGold(0);
        check("Adding zero is a no-op", 60, player.getGold());
        checkThrows("Negative gain is rejected", new TestAction()
        {
            public void run()
            {
                player.addGold(-1);
            }
        });
        checkThrows("Negative spend is rejected", new TestAction()
        {
            public void run()
            {
                player.spendGold(-1);
            }
        });
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

    private static void checkThrows(String label, TestAction action)
    {
        checks++;
        try
        {
            action.run();
            failures++;
            System.err.println(label + ": expected IllegalArgumentException");
        }
        catch (IllegalArgumentException expected)
        {
            // Expected validation failure.
        }
    }
}
```

- [ ] **Step 2: Run RED and confirm the missing Gold API is the cause**

Run from PowerShell:

```powershell
$buildDir = 'E:\For_the_first\.tmp-slayvt-shop-build'
if (Test-Path -LiteralPath $buildDir) { Remove-Item -LiteralPath $buildDir -Recurse -Force }
New-Item -ItemType Directory -Path $buildDir | Out-Null
$sources = Get-ChildItem -LiteralPath 'D:\Slay_vt\SlayVT\SlayVT\src\SlayVTGame' -Filter '*.java' | ForEach-Object { $_.FullName }
javac -d $buildDir $sources
```

Expected: compilation fails only because `Player.getGold`, `Player.addGold`, and `Player.spendGold` do not exist.

- [ ] **Step 3: Implement Gold in `Player`**

Add the field and constructor initialization:

```java
private int gold;

// In Player(String, int), after energy initialization:
gold = 99;
```

Add these methods:

```java
public int getGold()
{
    return gold;
}

public void addGold(int amount)
{
    if (amount < 0)
    {
        throw new IllegalArgumentException(
            "Gold amount must not be negative.");
    }
    gold += amount;
}

public boolean spendGold(int amount)
{
    if (amount < 0)
    {
        throw new IllegalArgumentException(
            "Gold amount must not be negative.");
    }
    if (amount > gold)
    {
        return false;
    }
    gold -= amount;
    return true;
}
```

- [ ] **Step 4: Run GREEN and the existing suite**

Run the compilation command above, followed by:

```powershell
java -cp $buildDir SlayVTGame.ShopSystemTest
java -cp $buildDir SlayVTGame.TemperatureTest
java -cp $buildDir SlayVTGame.CardMechanicsTest
java -cp $buildDir SlayVTGame.SourceLayoutTest 'D:\Slay_vt\SlayVT\SlayVT\src'
```

Expected: 9 shop checks, 64 temperature checks, 74 card checks, and the source-layout check pass.

- [ ] **Step 5: Review and commit Player Gold**

```powershell
git -c safe.directory='D:/Slay_vt/SlayVT' -C 'D:\Slay_vt\SlayVT' diff --check -- 'SlayVT/src/SlayVTGame/Player.java' 'SlayVT/src/SlayVTGame/ShopSystemTest.java'
git -c safe.directory='D:/Slay_vt/SlayVT' -C 'D:\Slay_vt\SlayVT' add -- 'SlayVT/src/SlayVTGame/Player.java' 'SlayVT/src/SlayVTGame/ShopSystemTest.java'
git -c safe.directory='D:/Slay_vt/SlayVT' -C 'D:\Slay_vt\SlayVT' commit -m 'feat: add player gold'
```

### Task 2: Add Safe Permanent-Deck Removal

**Files:**
- Modify: `SlayVT/src/SlayVTGame/Deck.java`
- Modify: `SlayVT/src/SlayVTGame/ShopSystemTest.java`

**Interfaces:**
- Consumes: existing `Card[] Deck.getDeck()`.
- Produces: `int Deck.size()` and `boolean Deck.removeCard(Card card)`.

- [ ] **Step 1: Add the failing deck-removal test**

Call `testDeckRemoval()` from `main`, then add:

```java
private static void testDeckRemoval()
{
    Deck deck = new Deck(1);
    int initialSize = deck.size();
    Card selected = deck.getDeck()[0];
    check("Selected card is removed", true, deck.removeCard(selected));
    check("Removal decreases deck size", initialSize - 1, deck.size());
    check("The same card cannot be removed twice", false,
        deck.removeCard(selected));
    check("A foreign card reference is rejected", false,
        deck.removeCard(new Card(CardLibrary.STRIKE)));
    check("Null card removal is rejected", false, deck.removeCard(null));

    while (deck.size() > 1)
    {
        deck.removeCard(deck.getDeck()[0]);
    }
    Card lastCard = deck.getDeck()[0];
    check("The final card cannot be removed", false,
        deck.removeCard(lastCard));
    check("Final card remains in deck", 1, deck.size());
}
```

- [ ] **Step 2: Run RED**

Compile all source files. Expected: compilation fails because `Deck.size` and `Deck.removeCard` do not exist.

- [ ] **Step 3: Implement identity-based removal**

Add to `Deck`:

```java
public int size()
{
    return deck.size();
}

public boolean removeCard(Card card)
{
    if (card == null || deck.size() <= 1)
    {
        return false;
    }
    for (int i = 0; i < deck.size(); i++)
    {
        if (deck.get(i) == card)
        {
            deck.remove(i);
            return true;
        }
    }
    return false;
}
```

- [ ] **Step 4: Run GREEN and the full suite**

Compile, then run `ShopSystemTest`, `TemperatureTest`, `CardMechanicsTest`, and `SourceLayoutTest`. Expected shop total: 16 checks; all existing checks remain green.

- [ ] **Step 5: Review and commit deck removal**

```powershell
git -c safe.directory='D:/Slay_vt/SlayVT' -C 'D:\Slay_vt\SlayVT' diff --check -- 'SlayVT/src/SlayVTGame/Deck.java' 'SlayVT/src/SlayVTGame/ShopSystemTest.java'
git -c safe.directory='D:/Slay_vt/SlayVT' -C 'D:\Slay_vt\SlayVT' add -- 'SlayVT/src/SlayVTGame/Deck.java' 'SlayVT/src/SlayVTGame/ShopSystemTest.java'
git -c safe.directory='D:/Slay_vt/SlayVT' -C 'D:\Slay_vt\SlayVT' commit -m 'feat: add safe deck removal'
```

### Task 3: Add Shop Items and Seeded Inventory

**Files:**
- Create: `SlayVT/src/SlayVTGame/ShopItem.java`
- Modify: `SlayVT/src/SlayVTGame/ShopSystem.java`
- Modify: `SlayVT/src/SlayVTGame/ShopSystemTest.java`

**Interfaces:**
- Consumes: `CardLibrary.values()`, `CardLibrary.getType()`, and `java.util.Random`.
- Produces: `ShopItem` getters, package-visible `ShopItem.markSold()`, constructors `ShopSystem()` and `ShopSystem(Random)`, and `ArrayList<ShopItem> beginVisit()`.

- [ ] **Step 1: Add failing item and inventory tests**

Add these imports below the package declaration:

```java
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
```

Add these calls after `testDeckRemoval()` in `main`:

```java
testShopItemValidation();
testSeededInventory();
```

Then add:

```java
private static void testShopItemValidation()
{
    ShopItem item = new ShopItem(CardLibrary.STRIKE, 50);
    check("Item exposes its card type", "Strike",
        item.getCardType().getName());
    check("Item exposes its price", 50, item.getPrice());
    check("New item is available", false, item.isSold());
    checkThrows("Null item type is rejected", new TestAction()
    {
        public void run()
        {
            new ShopItem(null, 50);
        }
    });
    checkThrows("Non-positive item price is rejected", new TestAction()
    {
        public void run()
        {
            new ShopItem(CardLibrary.STRIKE, 0);
        }
    });
}

private static void testSeededInventory()
{
    ArrayList<ShopItem> first =
        new ShopSystem(new Random(2114)).beginVisit();
    ArrayList<ShopItem> second =
        new ShopSystem(new Random(2114)).beginVisit();
    check("Shop offers five cards", 5, first.size());
    check("Seeded inventory is repeatable", inventorySignature(first),
        inventorySignature(second));

    int attacks = 0;
    int skills = 0;
    int powers = 0;
    Set<CardLibrary> unique = new HashSet<CardLibrary>();
    for (ShopItem item : first)
    {
        unique.add(item.getCardType());
        String type = item.getCardType().getType();
        if ("Attack".equals(type))
        {
            attacks++;
            check("Attack costs 50", 50, item.getPrice());
        }
        else if ("Skill".equals(type))
        {
            skills++;
            check("Skill costs 50", 50, item.getPrice());
        }
        else if ("Power".equals(type))
        {
            powers++;
            check("Power costs 75", 75, item.getPrice());
        }
    }
    check("Inventory has two Attacks", 2, attacks);
    check("Inventory has two Skills", 2, skills);
    check("Inventory has one Power", 1, powers);
    check("Inventory contains no duplicates", 5, unique.size());
}

private static String inventorySignature(ArrayList<ShopItem> items)
{
    String signature = "";
    for (ShopItem item : items)
    {
        signature += item.getCardType().name() + ":" + item.getPrice() + ";";
    }
    return signature;
}
```

- [ ] **Step 2: Run RED**

Compile all files. Expected: compilation fails because `ShopItem` and both `ShopSystem` constructors and `beginVisit` are missing.

- [ ] **Step 3: Implement `ShopItem`**

Create:

```java
package SlayVTGame;

public class ShopItem
{
    private final CardLibrary cardType;
    private final int price;
    private boolean sold;

    public ShopItem(CardLibrary cardType, int price)
    {
        if (cardType == null)
        {
            throw new IllegalArgumentException(
                "Card type must not be null.");
        }
        if (price <= 0)
        {
            throw new IllegalArgumentException("Price must be positive.");
        }
        this.cardType = cardType;
        this.price = price;
        sold = false;
    }

    public CardLibrary getCardType()
    {
        return cardType;
    }

    public int getPrice()
    {
        return price;
    }

    public boolean isSold()
    {
        return sold;
    }

    void markSold()
    {
        sold = true;
    }
}
```

- [ ] **Step 4: Implement seeded inventory in `ShopSystem`**

Replace the empty class with:

```java
package SlayVTGame;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public class ShopSystem
{
    private final Random random;
    private int removalCost;
    private boolean removalUsedThisVisit;

    public ShopSystem()
    {
        this(new Random());
    }

    public ShopSystem(Random random)
    {
        if (random == null)
        {
            throw new IllegalArgumentException(
                "Random source must not be null.");
        }
        this.random = random;
        removalCost = 75;
    }

    public ArrayList<ShopItem> beginVisit()
    {
        removalUsedThisVisit = false;
        ArrayList<CardLibrary> attacks = cardsOfType("Attack");
        ArrayList<CardLibrary> skills = cardsOfType("Skill");
        ArrayList<CardLibrary> powers = cardsOfType("Power");
        Collections.shuffle(attacks, random);
        Collections.shuffle(skills, random);
        Collections.shuffle(powers, random);

        ArrayList<ShopItem> inventory = new ArrayList<ShopItem>();
        inventory.add(new ShopItem(attacks.get(0), 50));
        inventory.add(new ShopItem(attacks.get(1), 50));
        inventory.add(new ShopItem(skills.get(0), 50));
        inventory.add(new ShopItem(skills.get(1), 50));
        inventory.add(new ShopItem(powers.get(0), 75));
        return inventory;
    }

    private ArrayList<CardLibrary> cardsOfType(String type)
    {
        ArrayList<CardLibrary> cards = new ArrayList<CardLibrary>();
        for (CardLibrary cardType : CardLibrary.values())
        {
            if (type.equals(cardType.getType()))
            {
                cards.add(cardType);
            }
        }
        return cards;
    }
}
```

- [ ] **Step 5: Run GREEN and the full suite**

Compile and run all four executable tests. Expected: inventory is repeatable, contains the specified distribution, and every existing test remains green.

- [ ] **Step 6: Review and commit inventory**

```powershell
git -c safe.directory='D:/Slay_vt/SlayVT' -C 'D:\Slay_vt\SlayVT' diff --check -- 'SlayVT/src/SlayVTGame/ShopItem.java' 'SlayVT/src/SlayVTGame/ShopSystem.java' 'SlayVT/src/SlayVTGame/ShopSystemTest.java'
git -c safe.directory='D:/Slay_vt/SlayVT' -C 'D:\Slay_vt\SlayVT' add -- 'SlayVT/src/SlayVTGame/ShopItem.java' 'SlayVT/src/SlayVTGame/ShopSystem.java' 'SlayVT/src/SlayVTGame/ShopSystemTest.java'
git -c safe.directory='D:/Slay_vt/SlayVT' -C 'D:\Slay_vt\SlayVT' commit -m 'feat: add seeded shop inventory'
```

### Task 4: Add Atomic Card Purchases

**Files:**
- Modify: `SlayVT/src/SlayVTGame/ShopSystem.java`
- Modify: `SlayVT/src/SlayVTGame/ShopSystemTest.java`

**Interfaces:**
- Consumes: `Player.spendGold`, `Deck.addCard`, and `ShopItem.markSold`.
- Produces: `boolean ShopSystem.purchaseCard(Player player, Deck deck, ShopItem item)`.

- [ ] **Step 1: Add failing purchase tests**

Call `testCardPurchases()` from `main`, then add:

```java
private static void testCardPurchases()
{
    ShopSystem shop = new ShopSystem(new Random(1));
    ArrayList<ShopItem> inventory = shop.beginVisit();
    ShopItem item = inventory.get(0);
    Player player = new Player("Player", 80);
    Deck deck = new Deck(1);
    int initialSize = deck.size();

    check("Affordable card purchase succeeds", true,
        shop.purchaseCard(player, deck, item));
    check("Purchase deducts exact price", 99 - item.getPrice(),
        player.getGold());
    check("Purchase adds one card", initialSize + 1, deck.size());
    check("Purchased offer is sold", true, item.isSold());
    check("Sold offer cannot be purchased twice", false,
        shop.purchaseCard(player, deck, item));
    check("Second purchase does not charge Gold", 99 - item.getPrice(),
        player.getGold());
    check("Second purchase does not add a card", initialSize + 1,
        deck.size());

    Player poorPlayer = new Player("Poor", 80);
    poorPlayer.spendGold(99);
    Deck poorDeck = new Deck(1);
    ShopItem expensive = new ShopItem(CardLibrary.RED_HOT_FORM, 75);
    int poorSize = poorDeck.size();
    check("Unaffordable purchase fails", false,
        shop.purchaseCard(poorPlayer, poorDeck, expensive));
    check("Unaffordable purchase preserves Gold", 0,
        poorPlayer.getGold());
    check("Unaffordable purchase preserves deck", poorSize,
        poorDeck.size());
    check("Unaffordable offer remains available", false,
        expensive.isSold());
}
```

Append these validation cases inside `testCardPurchases()`:

```java
checkThrows("Null purchase player is rejected", new TestAction()
{
    public void run()
    {
        shop.purchaseCard(null, deck, item);
    }
});
checkThrows("Null purchase deck is rejected", new TestAction()
{
    public void run()
    {
        shop.purchaseCard(player, null, item);
    }
});
checkThrows("Null purchase item is rejected", new TestAction()
{
    public void run()
    {
        shop.purchaseCard(player, deck, null);
    }
});
```

- [ ] **Step 2: Run RED**

Compile all files. Expected: compilation fails because `purchaseCard` does not exist.

- [ ] **Step 3: Implement all-or-nothing purchase**

Add to `ShopSystem`:

```java
public boolean purchaseCard(Player player, Deck deck, ShopItem item)
{
    if (player == null || deck == null || item == null)
    {
        throw new IllegalArgumentException(
            "Player, deck, and shop item must not be null.");
    }
    if (item.isSold() || !player.spendGold(item.getPrice()))
    {
        return false;
    }
    deck.addCard(item.getCardType());
    item.markSold();
    return true;
}
```

- [ ] **Step 4: Run GREEN and the full suite**

Compile and run all executable tests. Verify unsuccessful purchases leave all three pieces of state unchanged.

- [ ] **Step 5: Review and commit purchases**

```powershell
git -c safe.directory='D:/Slay_vt/SlayVT' -C 'D:\Slay_vt\SlayVT' diff --check -- 'SlayVT/src/SlayVTGame/ShopSystem.java' 'SlayVT/src/SlayVTGame/ShopSystemTest.java'
git -c safe.directory='D:/Slay_vt/SlayVT' -C 'D:\Slay_vt\SlayVT' add -- 'SlayVT/src/SlayVTGame/ShopSystem.java' 'SlayVT/src/SlayVTGame/ShopSystemTest.java'
git -c safe.directory='D:/Slay_vt/SlayVT' -C 'D:\Slay_vt\SlayVT' commit -m 'feat: add shop card purchases'
```

### Task 5: Add Once-Per-Visit Card Removal

**Files:**
- Modify: `SlayVT/src/SlayVTGame/ShopSystem.java`
- Modify: `SlayVT/src/SlayVTGame/ShopSystemTest.java`

**Interfaces:**
- Consumes: `Player.getGold`, `Player.spendGold`, `Player.addGold`, `Deck.getDeck`, `Deck.size`, and `Deck.removeCard`.
- Produces: `int ShopSystem.getRemovalCost()` and `boolean ShopSystem.removeCard(Player player, Deck deck, Card card)`.

- [ ] **Step 1: Add failing removal tests**

Call `testCardRemovalService()` from `main`, then add:

```java
private static void testCardRemovalService()
{
    ShopSystem shop = new ShopSystem(new Random(2));
    shop.beginVisit();
    Player player = new Player("Player", 80);
    Deck deck = new Deck(1);
    Card first = deck.getDeck()[0];
    int initialSize = deck.size();

    check("Initial removal costs 75", 75, shop.getRemovalCost());
    check("First removal succeeds", true,
        shop.removeCard(player, deck, first));
    check("Removal deducts Gold", 24, player.getGold());
    check("Removal changes deck size", initialSize - 1, deck.size());
    check("Next removal costs 100", 100, shop.getRemovalCost());

    Card second = deck.getDeck()[0];
    check("Second removal in one visit fails", false,
        shop.removeCard(player, deck, second));
    check("Rejected repeat does not charge", 24, player.getGold());
    check("Rejected repeat preserves deck", initialSize - 1, deck.size());

    shop.beginVisit();
    player.addGold(76);
    check("New visit permits removal", true,
        shop.removeCard(player, deck, second));
    check("New visit uses increased price", 0, player.getGold());
    check("Next successful price is 125", 125, shop.getRemovalCost());

    ShopSystem invalidShop = new ShopSystem(new Random(3));
    invalidShop.beginVisit();
    Player invalidPlayer = new Player("Player", 80);
    Deck invalidDeck = new Deck(1);
    int invalidSize = invalidDeck.size();
    check("Foreign card removal fails", false, invalidShop.removeCard(
        invalidPlayer, invalidDeck, new Card(CardLibrary.STRIKE)));
    check("Foreign card does not charge", 99, invalidPlayer.getGold());
    check("Foreign card preserves deck", invalidSize, invalidDeck.size());

    while (invalidDeck.size() > 1)
    {
        invalidDeck.removeCard(invalidDeck.getDeck()[0]);
    }
    Card finalCard = invalidDeck.getDeck()[0];
    check("Shop cannot remove final card", false,
        invalidShop.removeCard(invalidPlayer, invalidDeck, finalCard));
    check("Final-card rejection does not charge", 99,
        invalidPlayer.getGold());

    ShopSystem poorShop = new ShopSystem(new Random(4));
    poorShop.beginVisit();
    Player poorPlayer = new Player("Poor", 80);
    poorPlayer.spendGold(99);
    Deck poorDeck = new Deck(1);
    int poorSize = poorDeck.size();
    check("Unaffordable removal fails", false, poorShop.removeCard(
        poorPlayer, poorDeck, poorDeck.getDeck()[0]));
    check("Unaffordable removal preserves deck", poorSize,
        poorDeck.size());
    check("Failed removal preserves price", 75,
        poorShop.getRemovalCost());
}
```

Append these validation cases inside `testCardRemovalService()`:

```java
checkThrows("Null removal player is rejected", new TestAction()
{
    public void run()
    {
        invalidShop.removeCard(null, invalidDeck, finalCard);
    }
});
checkThrows("Null removal deck is rejected", new TestAction()
{
    public void run()
    {
        invalidShop.removeCard(invalidPlayer, null, finalCard);
    }
});
checkThrows("Null removal card is rejected", new TestAction()
{
    public void run()
    {
        invalidShop.removeCard(invalidPlayer, invalidDeck, null);
    }
});
```

- [ ] **Step 2: Run RED**

Compile all files. Expected: compilation fails because `getRemovalCost` and `ShopSystem.removeCard` do not exist.

- [ ] **Step 3: Implement transactional removal**

Add to `ShopSystem`:

```java
public int getRemovalCost()
{
    return removalCost;
}

public boolean removeCard(Player player, Deck deck, Card card)
{
    if (player == null || deck == null || card == null)
    {
        throw new IllegalArgumentException(
            "Player, deck, and card must not be null.");
    }
    if (removalUsedThisVisit || deck.size() <= 1
        || !containsIdentity(deck, card))
    {
        return false;
    }
    if (!player.spendGold(removalCost))
    {
        return false;
    }
    if (!deck.removeCard(card))
    {
        player.addGold(removalCost);
        return false;
    }
    removalUsedThisVisit = true;
    removalCost += 25;
    return true;
}

private boolean containsIdentity(Deck deck, Card target)
{
    for (Card card : deck.getDeck())
    {
        if (card == target)
        {
            return true;
        }
    }
    return false;
}
```

- [ ] **Step 4: Run GREEN and the full suite**

Compile and run all executable tests. Verify only a successful removal mutates Gold, deck, visit state, and future price.

- [ ] **Step 5: Review and commit removal service**

```powershell
git -c safe.directory='D:/Slay_vt/SlayVT' -C 'D:\Slay_vt\SlayVT' diff --check -- 'SlayVT/src/SlayVTGame/ShopSystem.java' 'SlayVT/src/SlayVTGame/ShopSystemTest.java'
git -c safe.directory='D:/Slay_vt/SlayVT' -C 'D:\Slay_vt\SlayVT' add -- 'SlayVT/src/SlayVTGame/ShopSystem.java' 'SlayVT/src/SlayVTGame/ShopSystemTest.java'
git -c safe.directory='D:/Slay_vt/SlayVT' -C 'D:\Slay_vt\SlayVT' commit -m 'feat: add shop card removal'
```

### Task 6: Add Console Interaction and Main Integration

**Files:**
- Modify: `SlayVT/src/SlayVTGame/ShopSystem.java`
- Modify: `SlayVT/src/SlayVTGame/ShopSystemTest.java`
- Modify: `SlayVT/src/SlayVTGame/Main.java`

**Interfaces:**
- Consumes: all earlier shop interfaces and `ToolClass.askOption`/`println`.
- Produces: `void ShopSystem.open(Player player, Deck deck)` and a live Shop-room path in `Main`.

- [ ] **Step 1: Add a failing leave-path integration test**

Add these imports below the existing imports:

```java
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
```

Change the test entry point and add the new call after the other test calls:

```java
public static void main(String[] args) throws Exception
{
    testPlayerGold();
    testDeckRemoval();
    testShopItemValidation();
    testSeededInventory();
    testCardPurchases();
    testCardRemovalService();
    testOpenCanLeave();

    if (failures > 0)
    {
        throw new AssertionError(failures + " of " + checks
            + " shop checks failed.");
    }
    System.out.println("PASS: " + checks + " shop checks.");
}
```

Then add:

```java
private static void testOpenCanLeave() throws Exception
{
    InputStream originalIn = System.in;
    PrintStream originalOut = System.out;
    ByteArrayOutputStream output = new ByteArrayOutputStream();
    try
    {
        System.setIn(new ByteArrayInputStream(
            "0\n".getBytes("UTF-8")));
        System.setOut(new PrintStream(output, true, "UTF-8"));
        new ShopSystem(new Random(5)).open(
            new Player("Player", 80), new Deck(1));
    }
    finally
    {
        System.setIn(originalIn);
        System.setOut(originalOut);
    }
    String text = output.toString("UTF-8");
    check("Shop displays current Gold", true, text.contains("Gold: 99"));
    check("Shop displays removal price", true,
        text.contains("Remove a card (75 Gold)"));
    check("Shop offers a leave option", true,
        text.contains("0: Leave shop"));
}
```

- [ ] **Step 2: Run RED**

Compile all files. Expected: compilation fails because `ShopSystem.open` does not exist.

- [ ] **Step 3: Implement the console menu**

Add `import static SlayVTGame.ToolClass.*;` and this method to `ShopSystem`:

```java
public void open(Player player, Deck deck)
{
    if (player == null || deck == null)
    {
        throw new IllegalArgumentException(
            "Player and deck must not be null.");
    }
    ArrayList<ShopItem> inventory = beginVisit();
    int removeOption = inventory.size() + 1;
    while (true)
    {
        String prompt = "Gold: " + player.getGold() + "\nShop:";
        for (int i = 0; i < inventory.size(); i++)
        {
            ShopItem item = inventory.get(i);
            Card card = new Card(item.getCardType());
            prompt += "\n" + (i + 1) + ": " + card.getName()
                + " (" + item.getPrice() + " Gold) " + card.getEffect();
            if (item.isSold())
            {
                prompt += " [SOLD]";
            }
        }
        prompt += "\n" + removeOption + ": Remove a card ("
            + removalCost + " Gold)";
        if (removalUsedThisVisit)
        {
            prompt += " [USED]";
        }
        prompt += "\n0: Leave shop";

        int choice = askOption(prompt, 0, removeOption);
        if (choice == 0)
        {
            return;
        }
        if (choice == removeOption)
        {
            openRemovalMenu(player, deck);
        }
        else
        {
            ShopItem item = inventory.get(choice - 1);
            if (purchaseCard(player, deck, item))
            {
                println("Purchased " + item.getCardType().getName() + ".");
            }
            else
            {
                println("Purchase failed.");
            }
        }
    }
}

private void openRemovalMenu(Player player, Deck deck)
{
    if (removalUsedThisVisit)
    {
        println("Card removal has already been used in this shop.");
        return;
    }
    Card[] cards = deck.getDeck();
    String prompt = "Choose a card to remove:";
    for (int i = 0; i < cards.length; i++)
    {
        prompt += "\n" + (i + 1) + ": " + cards[i].getName();
    }
    prompt += "\n0: Cancel";
    int choice = askOption(prompt, 0, cards.length);
    if (choice == 0)
    {
        return;
    }
    if (removeCard(player, deck, cards[choice - 1]))
    {
        println("Removed " + cards[choice - 1].getName() + ".");
    }
    else
    {
        println("Card removal failed.");
    }
}
```

- [ ] **Step 4: Connect one persistent shop in `Main`**

Add next to the existing `RestSystem` field:

```java
private static ShopSystem shop = new ShopSystem();
```

Replace the Shop placeholder branch with:

```java
case "Shop":
    println("You entered a Shop Room.");
    shop.open(player, deck);
    break;
```

- [ ] **Step 5: Run GREEN and final verification**

Compile every source file and run `ShopSystemTest`, `TemperatureTest`, `CardMechanicsTest`, and `SourceLayoutTest`. Then inspect:

```powershell
git -c safe.directory='D:/Slay_vt/SlayVT' -C 'D:\Slay_vt\SlayVT' diff --check
git -c safe.directory='D:/Slay_vt/SlayVT' -C 'D:\Slay_vt\SlayVT' diff -- 'SlayVT/.project'
git -c safe.directory='D:/Slay_vt/SlayVT' -C 'D:\Slay_vt\SlayVT' status --short
```

Expected: every test exits 0; `diff --check` has no whitespace errors; `.project` still contains only the user's original uncommitted change; only the three Task 6 Java files are pending.

- [ ] **Step 6: Commit console integration without `.project`**

```powershell
git -c safe.directory='D:/Slay_vt/SlayVT' -C 'D:\Slay_vt\SlayVT' add -- 'SlayVT/src/SlayVTGame/ShopSystem.java' 'SlayVT/src/SlayVTGame/ShopSystemTest.java' 'SlayVT/src/SlayVTGame/Main.java'
git -c safe.directory='D:/Slay_vt/SlayVT' -C 'D:\Slay_vt\SlayVT' diff --cached --check
git -c safe.directory='D:/Slay_vt/SlayVT' -C 'D:\Slay_vt\SlayVT' commit -m 'feat: connect interactive shop room'
```

### Final Branch Review

- [ ] Verify the commit series contains only the design, plan, and intended shop files:

```powershell
git -c safe.directory='D:/Slay_vt/SlayVT' -C 'D:\Slay_vt\SlayVT' log --oneline origin/main..HEAD
git -c safe.directory='D:/Slay_vt/SlayVT' -C 'D:\Slay_vt\SlayVT' diff --stat origin/main..HEAD
```

- [ ] Recompile from a clean temporary build directory and rerun all four executable tests immediately before the final report.

- [ ] Confirm `git status --short` shows only ` M SlayVT/.project`, proving the user's original file was not staged or committed.
