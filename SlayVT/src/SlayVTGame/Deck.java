package SlayVTGame;
import java.util.*;

public class Deck
{
    private ArrayList<Card> deck;

    public Deck(int characterNum)
    {
        deck = new ArrayList<Card>();
        
        //initial deck
        for (int i = 0; i < 5; i++)
        {
            addCard(CardLibrary.STRIKE);
            addCard(CardLibrary.DEFEND);
        }
        addCard(CardLibrary.BURN);
        addCard(CardLibrary.BREEZE);
    }

    public void addCard(CardLibrary cardType)
    {
        deck.add(new Card(cardType));
    }

    public Card drawCard()
    {
        if (deck.isEmpty())
        {
            throw new IllegalStateException("The deck is empty.");
        }

        int cardNum = (int)(Math.random() * deck.size());
        return deck.get(cardNum);
    }

    public Card[] getDeck()
    {
        return deck.toArray(new Card[0]);
    }
}