package SlayVTGame;
public class Player extends Character
{
    //~ Fields ................................................................
    private int energy;
    private int maxEnergy;
    
    //~ Constructors ..........................................................
    public Player(String name, int maxHp) {
        super(name, maxHp);
        energy = 3;
        maxEnergy = 3;
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
}
