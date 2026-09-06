package SlayVTGame;
import java.util.*;

public class Card
{
    //~ Fields ................................................................
    private String name;
    private int cost;
    private String type;
    private CardEffect effect;
    //~ Constructors ..........................................................
    public Card(int num) {
        if (num == 1) {
            name = "Strike";
            cost = 1;
            type = "Attack";
            effect = new DamageEffect(6);
        } else {
            name = "Defend";
            cost = 1;
            type = "Skill";
            effect = new BlockEffect(5);
        }
    }
    //~Public  Methods ........................................................
    public String getName() {
        return name;
    }

    public int getCost() {
        return cost;
    }

    public void setCost(int cost) {
        this.cost = cost;
    }

    public String getType() {
        return type;
    }

    public String getEffect() {
        return effect.toString();
    }
    
    public void apply(Player player, Enemy enemy) {
        player.spendEnergy(cost);
        effect.apply(player, enemy);
    }
    
    public void apply(Player player, ArrayList<Enemy> enemies) {
        player.spendEnergy(cost);
        for(int i = 0; i < enemies.size(); i++) {
            Enemy temp = enemies.get(i);
            effect.apply(player, temp);
        }
    }
}
