package SlayVTGame;

public class Character
{
    protected String name;
    protected int hp;
    protected int maxHp;
    protected int block;
    protected Buffs buffs;
    protected boolean ifAlive;

    public Character(String name, int maxHp)
    {
        if (maxHp <= 0)
        {
            throw new IllegalArgumentException(
                "Maximum HP must be positive.");
        }

        this.name = name;
        this.maxHp = maxHp;
        hp = maxHp;
        block = 0;
        buffs = new Buffs();
        ifAlive = true;
    }

    // Damage modifiers are applied before calling this method.
    public void takeDamage(int damage)
    {
        if (damage < 0)
        {
            throw new IllegalArgumentException(
                "Damage must not be negative.");
        }

        int absorbed = Math.min(block, damage);
        block -= absorbed;
        hp = Math.max(0, hp - (damage - absorbed));
    }

    public void takeUnblockableDamage(int damage)
    {
        if (damage < 0)
        {
            throw new IllegalArgumentException(
                "Damage must not be negative.");
        }

        hp = Math.max(0, hp - damage);
    }
    
    public void heal(int healamount)
    {
        if (healamount < 0)
        {
            throw new IllegalArgumentException("Heal must not be negative.");
        }

        hp = Math.min(maxHp, hp + healamount);
    }

    public boolean checkAlive()
    {
        return hp > 0;
    }

    public String getName()
    {
        return name;
    }

    public int getHp()
    {
        return hp;
    }

    public int getMaxHp()
    {
        return maxHp;
    }

    public int getBlock()
    {
        return block;
    }

    public void addBlock(int amount)
    {
        if (amount < 0)
        {
            throw new IllegalArgumentException(
                "Block must not be negative.");
        }

        block += amount;
    }

    public void setBlock(int block)
    {
        if (block < 0)
        {
            throw new IllegalArgumentException(
                "Block must not be negative.");
        }

        this.block = block;
    }

    public boolean getIfAlive()
    {
        return checkAlive();
    }

    public Buffs getBuffs()
    {
        return buffs;
    }
}
