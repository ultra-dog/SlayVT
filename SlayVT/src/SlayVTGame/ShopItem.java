package SlayVTGame;

public class ShopItem
{
    private final CardLibrary cardType;
    private final int price;
    private boolean sold;

    public ShopItem(CardLibrary cardType, int price)
    {
        if (cardType == null)
        {
            throw new IllegalArgumentException(
                "Card type must not be null.");
        }
        if (price <= 0)
        {
            throw new IllegalArgumentException("Price must be positive.");
        }
        this.cardType = cardType;
        this.price = price;
        sold = false;
    }

    public CardLibrary getCardType()
    {
        return cardType;
    }

    public int getPrice()
    {
        return price;
    }

    public boolean isSold()
    {
        return sold;
    }

    void markSold()
    {
        sold = true;
    }
}
