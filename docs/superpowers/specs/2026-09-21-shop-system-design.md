# SlayVT Shop System Design

## Goal

Add a text-based shop room where the player can spend Gold to buy cards or remove one card from the permanent deck. The implementation should resemble the useful core of the *Slay the Spire 2* merchant while remaining small enough for the CS2114 project.

## Confirmed Scope

- The shop sells cards and provides card removal.
- Potions and relics are not part of this version.
- The player starts a run with 99 Gold.
- A shop visit offers five different cards: two Attacks, two Skills, and one Power.
- Attack and Skill cards cost 50 Gold; Power cards cost 75 Gold.
- A purchased card is added to the permanent deck and cannot be bought again during the same visit.
- Card removal may be used once per shop visit.
- Removal costs 75 Gold initially and increases by 25 Gold after each successful removal during the run.
- The last card in a deck cannot be removed.
- The shop uses injectable randomness so inventory tests are repeatable.

## Non-Goals

- Potions, relics, discounts, restocking, selling cards, card rarity, and Ascension price changes.
- Rebalancing cards, battle rewards, or the rest of the room-generation system.
- Refactoring unrelated battle, rest-site, temperature, or input code.

## Architecture

### `Player`

`Player` owns the player's Gold because Gold persists across rooms. The existing constructor will initialize Gold to 99. New methods will expose the balance, add a non-negative amount, and attempt an all-or-nothing purchase. Negative amounts will throw `IllegalArgumentException`; adding zero is a no-op, and an unaffordable purchase will return `false` without changing the balance.

Planned interface:

```java
public int getGold()
public void addGold(int amount)
public boolean spendGold(int amount)
```

### `Deck`

`Deck` remains the owner of permanent cards. It will expose its size and an identity-based removal operation. Removing `null`, a card not in the deck, or the final remaining card will fail without mutating the deck.

Planned interface:

```java
public int size()
public boolean removeCard(Card card)
```

The existing `addCard(CardLibrary)` method will be used for purchases.

### `ShopItem`

`ShopItem` represents one card offer. It stores a `CardLibrary` value, its Gold price, and whether it has been sold. The constructor rejects a null card type or non-positive price. `ShopSystem` is the only production class that marks an item sold.

Planned interface:

```java
public ShopItem(CardLibrary cardType, int price)
public CardLibrary getCardType()
public int getPrice()
public boolean isSold()
```

### `ShopSystem`

`ShopSystem` owns the current removal price and its random source. The no-argument constructor supplies normal runtime randomness; a package-visible or public constructor accepting `Random` supports deterministic tests.

It will:

1. Start a visit by resetting visit-only state and building a fresh five-card inventory without duplicate `CardLibrary` values.
2. Buy an available item only when the player can pay, then add the card and mark the item sold.
3. Remove at most one selected card per visit, charging only after all removal conditions pass.
4. Increase the run-wide removal price only after a successful removal.
5. Present the console menu and allow `0` to leave without spending Gold.

Planned core interface:

```java
public ShopSystem()
public ShopSystem(Random random)
public ArrayList<ShopItem> beginVisit()
public boolean purchaseCard(Player player, Deck deck, ShopItem item)
public boolean removeCard(Player player, Deck deck, Card card)
public int getRemovalCost()
public void open(Player player, Deck deck)
```

`beginVisit` will reset visit-only state, and `open` will call it before displaying the menu. Core purchase and removal methods will be independent of console input so they can be tested directly.

### `Main`

`Main` will keep one `ShopSystem` instance for the full run so removal-price increases persist. Selecting a Shop room will call `shop.open(player, deck)` instead of printing a placeholder.

## Data Flow

For a purchase, `ShopSystem` validates the arguments and item state, asks `Player` to spend the full price, calls `Deck.addCard`, then marks the item sold. If payment fails, the deck and item remain unchanged.

For removal, `ShopSystem` first checks visit availability, affordability, and deck eligibility. It then spends Gold, asks `Deck` to remove the selected card, marks removal used for that visit, and increases the next removal cost. Validation failure leaves Gold, deck contents, visit state, and future removal price unchanged.

## Error Handling

- Null collaborators and invalid numeric amounts throw `IllegalArgumentException`.
- Expected gameplay failures, including insufficient Gold, sold items, repeated removal in one visit, or an invalid removal target, return `false` and do not partially mutate state.
- Console choices continue to use `ToolClass.askOption`, which rejects non-integer and out-of-range input.
- The shop offers a leave option so a player is never forced to buy or remove a card.

## Testing

A new standalone `ShopSystemTest` will follow the project's existing executable-test style. Tests will first be run against missing behavior and observed failing before production implementation.

The test will cover:

- Starting Gold and all-or-nothing Gold spending.
- Negative Gold validation.
- Five unique offers with the required type distribution and prices using a seeded `Random`.
- Successful purchase updates Gold, deck size, and sold state exactly once.
- Insufficient Gold and repurchasing a sold item do not mutate state.
- Successful removal charges Gold, removes the selected card, and increases the next cost.
- A second removal during the same visit is rejected.
- Invalid targets and attempts to remove the final card do not charge Gold.
- Existing temperature, card mechanics, and source-layout tests continue to pass.

## Commit Boundary

The shop implementation will be committed in small, tested steps: player Gold, safe deck removal, inventory, purchases, removal service, and console integration. The user's existing `SlayVT/.project` modification will not be staged or committed.
