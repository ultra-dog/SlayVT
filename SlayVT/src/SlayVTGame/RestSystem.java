package SlayVTGame;

import static SlayVTGame.ToolClass.*;
import java.util.ArrayList;

public class RestSystem
{
    public void rest(Player player, Deck deck) {
        int restOption =
            askOption("Please enter a number to choose your action:\n"
                + "1: Rest: Heal for 30% of you Max HP ("
                + (int)Math.floor(player.getMaxHp() * 0.3) + ").\n"
                + "2: Smith: Upgrade a card in your Deck.", 1, 2);
        switch (restOption) {
            case 1:
                player.heal((int)Math.floor(player.getMaxHp() * 0.3));
                break;
            case 2:
                ArrayList<Card> upgradeableCards =
                    new ArrayList<Card>();
                for (Card card : deck.getDeck())
                {
                    if (card.canUpgrade())
                    {
                        upgradeableCards.add(card);
                    }
                }

                if (upgradeableCards.isEmpty())
                {
                    println("There are no cards that can be upgraded.");
                    break;
                }

                String prompt = "Choose a card to upgrade:";
                for (int i = 0; i < upgradeableCards.size(); i++)
                {
                    Card card = upgradeableCards.get(i);
                    prompt += "\n" + (i + 1) + ": " + card.getName()
                        + " (Cost " + card.getCost() + ") "
                        + card.getEffect();
                }

                int cardOption = askOption(
                    prompt, 1, upgradeableCards.size());
                Card selectedCard = upgradeableCards.get(cardOption - 1);
                upgradeCard(selectedCard);
                println("Upgraded " + selectedCard.getName() + ".");
                break;
        }
    }

    public boolean upgradeCard(Card card)
    {
        if (card == null)
        {
            throw new IllegalArgumentException("Card must not be null.");
        }
        return card.upgrade();
    }
}
