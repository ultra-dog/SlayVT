package SlayVTGame;
public class Player extends Character
{
    //~ Fields ................................................................
    private int energy;
    private int maxEnergy;
    private int gold;
    
    //~ Constructors ..........................................................
    public Player(String name, int maxHp) {
        super(name, maxHp);
        energy = 3;
        maxEnergy = 3;
        gold = 99;
    }
    //~Public  Methods ........................................................
    public int getEnergy() {
        return energy;
    }

    public void setEnergy(int energy) {
        this.energy = energy;
    }

    public int getMaxEnergy() {
        return maxEnergy;
    }

    public void setMaxEnergy(int maxEnergy) {
        this.maxEnergy = maxEnergy;
    }
    
    public void spendEnergy(int cost) {
        energy -= cost;
    }

    public int getGold()
    {
        return gold;
    }

    public void addGold(int amount)
    {
        if (amount < 0)
        {
            throw new IllegalArgumentException(
                "Gold amount must not be negative.");
        }
        gold += amount;
    }

    public boolean spendGold(int amount)
    {
        if (amount < 0)
        {
            throw new IllegalArgumentException(
                "Gold amount must not be negative.");
        }
        if (amount > gold)
        {
            return false;
        }
        gold -= amount;
        return true;
    }
}
