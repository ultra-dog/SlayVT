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
