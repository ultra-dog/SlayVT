package SlayVTGame;
public class BlockEffect implements CardEffect
{
    //~ Fields ................................................................
    private int block; 
    private int rptCount;
    //~ Constructors ..........................................................
    public BlockEffect(int block) {
        this.block = block;
        rptCount = 1;
    }
    public BlockEffect(int block, int rptCount) {
        this.block = block;
        this.rptCount = rptCount;
    }
    //~Public  Methods ........................................................
    public void apply(Player player, Enemy enemy) {
        for (int i = 0; i < rptCount; i++) {
            player.addBlock(block);
        }
    }
    public String toString() {
        String discription = "Gain " + block + " block";
        if(rptCount > 1) {
            discription += (" " + rptCount + " times"); 
        }
        discription += (".");
        return discription;
    }
}
