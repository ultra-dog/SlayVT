package SlayVTGame;
import java.util.*;

public class Deck
{
    //~ Fields ................................................................
    private ArrayList<Card> deck;
    //~ Constructors ..........................................................
    public Deck(int characterNum) {
        deck = new ArrayList<Card>();
        switch (characterNum) {
            case 1:
                for(int i = 0; i < 5; i++) {
                    deck.add(new Card(1));
                    deck.add(new Card(2));
                }
                break;
            default:
                for(int i = 0; i < 5; i++) {
                    deck.add(new Card(1));
                    deck.add(new Card(2));
                }
                break;
        }
    }
    //~Public  Methods ........................................................
    public Card drawCard() {
        int cardNum = (int)(Math.random()*deck.size());
        return deck.get(cardNum);
    }
    
    public Card[] getDeck() {
        Card[] cards = new Card[deck.size()];
        for (int i = 0; i < cards.length; i++) {
            cards[i] = deck.get(i);
        }
        return cards;
    }
}
