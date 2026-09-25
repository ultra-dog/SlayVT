package SlayVTGame;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class CoreGameplayJUnitTest
{
    @Test
    public void damageUsesBlockBeforeHp()
    {
        Player player = new Player("Tester", 80);
        player.addBlock(5);
        player.takeDamage(8);
        assertEquals(0, player.getBlock());
        assertEquals(77, player.getHp());
    }

    @Test(expected = IllegalArgumentException.class)
    public void negativeDamageIsRejected()
    {
        new Player("Tester", 80).takeDamage(-1);
    }

    @Test
    public void shopPurchaseChargesGoldAndAddsCard()
    {
        Player player = new Player("Tester", 80);
        Deck deck = new Deck(1);
        ShopItem item = new ShopItem(CardLibrary.BREEZE, 50);

        assertTrue(new ShopSystem().purchaseCard(player, deck, item));
        assertEquals(49, player.getGold());
        assertEquals(13, deck.size());
        assertTrue(item.isSold());
    }

    @Test(expected = IllegalArgumentException.class)
    public void nullShopItemIsRejected()
    {
        new ShopSystem().purchaseCard(
            new Player("Tester", 80), new Deck(1), null);
    }

    @Test
    public void removingOwnedCardShrinksDeck()
    {
        Deck deck = new Deck(1);
        Card card = deck.getDeck()[0];

        assertTrue(deck.removeCard(card));
        assertEquals(11, deck.size());
    }

    @Test
    public void removingNullCardLeavesDeckUnchanged()
    {
        Deck deck = new Deck(1);

        assertFalse(deck.removeCard(null));
        assertEquals(12, deck.size());
    }
}
