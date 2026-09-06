package SlayVTGame;

public class Character
{
    // ~ Fields ................................................................
    private String name;
    private int hp;
    private int maxHp;
    private int block;
    private boolean ifAlive;

    // ~ Constructors ..........................................................
    public Character(String name, int maxHp)
    {
        this.name = name;
        this.maxHp = maxHp;
        this.hp = maxHp;
        this.block = 0;
        this.ifAlive = true;
    }


    // ~Public Methods ........................................................
    public void takeDamage(int dmg)
    {
        if (dmg <= block)
        {
            block -= dmg;
        }
        else
        {
            dmg -= block;
            block = 0;
            hp -= dmg;
        }
    }
    
    public boolean checkAlive() {
        if(hp <= 0) {
            ifAlive = false;
        }else {
            ifAlive = true;
        }
        return ifAlive;
    }
    
    public String getName() {
        return name;
    }

    public int getHp() {
        return hp;
    }

    public int getMaxHp() {
        return maxHp;
    }

    public int getBlock() {
        return block;
    }
    
    public void addBlock(int block) {
        this.block += block;
    }
    
    public void setBlock(int block) {
        this.block = block;
    }

    public boolean getIfAlive() {
        return ifAlive;
    }
}
