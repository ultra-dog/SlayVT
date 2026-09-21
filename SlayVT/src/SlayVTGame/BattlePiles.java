package SlayVTGame;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BattlePiles
{
    private final ArrayList<Card> hand = new ArrayList<Card>();
    private final ArrayList<Card> drawPile = new ArrayList<Card>();
    private final ArrayList<Card> discardPile = new ArrayList<Card>();
    private final ArrayList<Card> exhaustPile = new ArrayList<Card>();

    public void initialize(Deck deck)
    {
        clear();
        for (Card card : deck.getDeck())
        {
            drawPile.add(card);
        }
    }

    public void drawToHand(int amount)
    {
        if (amount < 0)
        {
            throw new IllegalArgumentException(
                "Draw amount must not be negative.");
        }

        for (int i = 0; i < amount; i++)
        {
            if (drawPile.isEmpty())
            {
                if (discardPile.isEmpty())
                {
                    break;
                }
                drawPile.addAll(discardPile);
                discardPile.clear();
            }

            int index = (int)(Math.random() * drawPile.size());
            hand.add(drawPile.remove(index));
        }
    }

    public void movePlayedCard(Card card)
    {
        hand.remove(card);
        if (card.isExhaust())
        {
            exhaustPile.add(card);
        }
        else
        {
            discardPile.add(card);
        }
    }

    public void discardHand()
    {
        for (int i = hand.size() - 1; i >= 0; i--)
        {
            Card card = hand.get(i);
            if (!card.isRetain())
            {
                discardPile.add(card);
                hand.remove(i);
            }
        }
    }

    public List<Card> getHand()
    {
        return Collections.unmodifiableList(hand);
    }

    public List<Card> getDrawPile()
    {
        return Collections.unmodifiableList(drawPile);
    }

    public List<Card> getDiscardPile()
    {
        return Collections.unmodifiableList(discardPile);
    }

    public List<Card> getExhaustPile()
    {
        return Collections.unmodifiableList(exhaustPile);
    }

    ArrayList<Card> mutableHand()
    {
        return hand;
    }

    public void clear()
    {
        hand.clear();
        drawPile.clear();
        discardPile.clear();
        exhaustPile.clear();
    }
}
