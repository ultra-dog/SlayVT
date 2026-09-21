package SlayVTGame;

import static SlayVTGame.ToolClass.*;

public class RestSystem
{
    public void rest(Player player, Deck deck) {
        int restOption = 
            askOption("Please enter a number to choose your action:\n"
                + "1: Rest: Heal for 30% of you Max HP ("
                + (int)Math.floor(player.getMaxHp() * 0.3) + ").\n"
                + "2: Smith: Upgrade a card in your Deck.", 1, 2);
        switch (restOption) {
            case 1:
                player.heal((int)Math.floor(player.getMaxHp() * 0.3));
                break;
            case 2:
                // Keep for upgrade algorithms.
                break;
        }
    }
}
