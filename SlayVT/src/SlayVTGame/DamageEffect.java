package SlayVTGame;
public class DamageEffect implements CardEffect
{
    //~ Fields ................................................................
    private int dmg; 
    private int hitCount;
    //~ Constructors ..........................................................
    public DamageEffect(int dmg) {
        this.dmg = dmg;
        hitCount = 1;
    }
    public DamageEffect(int dmg, int hitCount) {
        this.dmg = dmg;
        this.hitCount = hitCount;
    }
    //~Public  Methods ........................................................
    public void apply(Player player, Enemy enemy) {
        enemy.takeDamage(dmg);
    }
    public String toString() {
        String discription = "Deal " + dmg + " damage";
        if(hitCount > 1) {
            discription += (" " + hitCount + " times"); 
        }
        discription += (".");
        return discription;
    }
}
