package SlayVTGame;

import static SlayVTGame.ToolClass.*;
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
                    + " (" + item.getPrice() + " Gold) "
                    + card.getEffect();
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
                    println("Purchased "
                        + item.getCardType().getName() + ".");
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
            if (type.equals(cardType.getType())
                && cardType != CardLibrary.STRIKE
                && cardType != CardLibrary.DEFEND)
            {
                cards.add(cardType);
            }
        }
        return cards;
    }
}
