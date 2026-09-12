package SlayVTGame;
import static SlayVTGame.ToolClass.*;
import java.util.ArrayList;

public class FightSystem
{
    //~ Fields ................................................................
    private Player player;
    private Enemy[] enemies;
    private Deck deck;
    private static ArrayList<Card> hand = new ArrayList<Card>();
    private static ArrayList<Card> drawPile = new ArrayList<Card>();
    private static ArrayList<Card> discardPile = new ArrayList<Card>();
    //~ Constructors ..........................................................
    //~Public  Methods ........................................................
    public boolean fight(Player player, ArrayList<Enemy> enemies, Deck deck) {
        int turn = 1;
        boolean ifEnds = false;
        this.player = player;
        this.enemies = new Enemy[enemies.size()];
        for (int i = 0; i < enemies.size(); i++) {
            this.enemies[i] = enemies.get(i);
        }
        this.deck = deck;
        for (Card card : deck.getDeck())
        {
            drawPile.add(card);
        }
        while (player.checkAlive() && !ifEnds)
        {
            for (int i = 0; i < 5; i++)
            {
                drawCardFromPile(drawPile, hand, discardPile);
            }
            println("Turn " + turn);
            // Turn starts
            player.setBlock(0);
            player.setEnergy(player.getMaxEnergy());
            while(true)
            {
                printPlayer(player);
                printEnemies(enemies);
                println("");
                int opt = askOption(printOptions(hand),0,hand.size());
                if (opt == 0) // player choose end turn
                {
                    break;
                }
                Card chosenCard = hand.get(opt - 1);
                if (chosenCard.getCost() > player.getEnergy())
                {
                    println("You don't have enough Energy.");
                    continue;
                }
                if (chosenCard.getType().equals("Attack"))
                {
                    int enemyopt = askOption(printEnemyOption(enemies),0,enemies.size());
                    if (enemyopt == 0)
                    {
                        continue;
                    }
                    Enemy chosenEnemy = enemies.get(enemyopt - 1);
                    chosenCard.apply(player, chosenEnemy);
                }
                else if (chosenCard.getType().equals("Skill"))
                {
                    chosenCard.apply(player, enemies);
                }
                discardPile.add(chosenCard);
                hand.remove(opt - 1);
                // detect if ends
                for (Enemy e: enemies) {
                    ifEnds = true;
                    if (e.checkAlive()) {
                        ifEnds = false;
                    }
                }
                if (ifEnds) {
                    break;
                }
            }
            // Turn ends
            for (int i = 0; i < hand.size(); i++)
            {
                discardPile.add(hand.get(i));
            }
            hand.clear();
            if (ifEnds) {
                break;
            }
            for (int i = 0; i < enemies.size(); i++)
            {
                Enemy temp = enemies.get(i);
                player.takeDamage(temp.getDmg());
                temp.setBlock(0);
            }
            turn++;
        }
        return player.checkAlive();
    }
}
