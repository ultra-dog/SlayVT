package SlayVTGame;
import static SlayVTGame.ToolClass.*;
import java.util.*;

public class Main
{
    private static Player player;
    private static Deck deck;
    private static ArrayList<Enemy> enemies = new ArrayList<Enemy>();
    private static FightSystem combat = new FightSystem();

    public static void main(String[] args)
    {
        String name = askName();
        int characterNum = askOption(
            "Please eneter number to choose your character: 1: zhanshige",
            1,1);
        switch (characterNum)
        {
            case 1:
                player = new Player(name, 80);
                break;
            default:
                player = new Player(name, 80);
                break;
        }
        deck = new Deck(1);
        for(int i = 0; i < 3; i++) {
            enemies.clear();
            enemies.add(new Enemy("Enemy 1", 12, 10));
            if(combat.fight(player, enemies, deck)) {
                println("win");
            }else {
                println("lose");
            }
        }
        
    }


    
}
