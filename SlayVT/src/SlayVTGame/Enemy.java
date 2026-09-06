package SlayVTGame;
public class Enemy extends Character
{
    //~ Fields ................................................................
    private String name;
    private int hp;
    private int maxHp;
    private int block;
    private int dmg; //Just fixed attack for now, let's do intent later
    private boolean ifAlive;
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
