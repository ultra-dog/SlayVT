package SlayVTGame;

import static SlayVTGame.ToolClass.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public class CardRewardSystem
{
    private final Random random;

    public CardRewardSystem()
    {
        this(new Random());
    }

    public CardRewardSystem(Random random)
    {
        if (random == null)
        {
            throw new IllegalArgumentException(
                "Random source must not be null.");
        }
        this.random = random;
    }

    public ArrayList<CardLibrary> generateChoices()
    {
        ArrayList<CardLibrary> available = new ArrayList<CardLibrary>();
        for (CardLibrary card : CardLibrary.values())
        {
            if (card != CardLibrary.STRIKE
                && card != CardLibrary.DEFEND)
            {
                available.add(card);
            }
        }
        Collections.shuffle(available, random);
        return new ArrayList<CardLibrary>(available.subList(0, 3));
    }

    public void offer(Deck deck)
    {
        ArrayList<CardLibrary> choices = generateChoices();
        String prompt = title("CARD REWARD")
            + "\nChoose one card to add to your deck:";
        for (int i = 0; i < choices.size(); i++)
        {
            Card card = new Card(choices.get(i));
            prompt += "\n\n" + choiceLine(i + 1,
                card.getName() + " [" + card.getCost() + " Energy]")
                + "\n" + detailLine(card.getEffect());
        }
        int choice = askOption(prompt, 1, choices.size());
        CardLibrary selected = choices.get(choice - 1);
        deck.addCard(selected);
        println("\nAdded " + selected.getName() + " to your deck.");
    }
}
