package SlayVTGame;

import static SlayVTGame.ToolClass.*;
import java.util.ArrayList;

public class RestSystem
{
    public void rest(Player player, Deck deck) {
        int restOption =
            askOption(title("REST SITE") + "\nHP: " + player.getHp()
                + "/" + player.getMaxHp() + "\n\nCHOOSE AN ACTION\n"
                + choiceLine(1, "Rest - Heal "
                    + (int)Math.floor(player.getMaxHp() * 0.3) + " HP")
                + "\n" + choiceLine(2, "Smith - Upgrade a card"),
                1, 2);
        switch (restOption) {
            case 1:
                player.heal((int)Math.floor(player.getMaxHp() * 0.3));
                println("\nRested. HP: " + player.getHp() + "/"
                    + player.getMaxHp() + ".");
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
                    println("\nThere are no cards that can be upgraded.");
                    break;
                }

                String prompt = title("SMITH")
                    + "\n\nCHOOSE A CARD TO UPGRADE";
                for (int i = 0; i < upgradeableCards.size(); i++)
                {
                    Card card = upgradeableCards.get(i);
                    prompt += "\n" + choiceLine(i + 1, card.getName()
                        + " [" + card.getCost() + " Energy]")
                        + "\n" + detailLine(card.getEffect());
                }

                int cardOption = askOption(
                    prompt, 1, upgradeableCards.size());
                Card selectedCard = upgradeableCards.get(cardOption - 1);
                upgradeCard(selectedCard);
                println("\nUpgraded " + selectedCard.getName() + ".");
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
