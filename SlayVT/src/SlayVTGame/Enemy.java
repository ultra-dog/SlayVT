package SlayVTGame;
public class Enemy extends Character
{
    //~ Fields ................................................................
    private int dmg; //Just fixed attack for now, let's do intent later
    
    //~ Constructors ..........................................................
    public Enemy(String name, int maxHp, int dmg) {
        super(name, maxHp);
        this.dmg = dmg;
    }
    //~Public  Methods ........................................................
    public int getDmg() {
        return dmg;
    }

    public void setDmg(int dmg) {
        this.dmg = dmg;
    }
}
