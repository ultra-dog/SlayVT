package SlayVTGame;

public class Enemy extends Character
{
    private int dmg;
    private final EnemyMove[] moves;
    private boolean legacy;
    private int moveIndex;

    public Enemy(String name, int maxHp, int dmg)
    {
        this(name, maxHp, EnemyMove.attack(dmg));
        this.dmg = dmg;
        legacy = true;
    }

    public Enemy(String name, int maxHp, EnemyMove... moves)
    {
        super(name, maxHp);
        if (moves == null || moves.length == 0)
        {
            throw new IllegalArgumentException("Enemy needs at least one move.");
        }
        this.moves = moves.clone();
        dmg = moves[0].getBaseDamage();
        legacy = false;
    }

    public int getDmg()
    {
        return dmg;
    }

    public void setDmg(int dmg)
    {
        this.dmg = dmg;
        if (legacy)
        {
            moves[0] = EnemyMove.attack(dmg);
        }
    }

    public String getIntent()
    {
        return moves[moveIndex].describe(this, null);
    }

    public String getIntent(Player player)
    {
        return moves[moveIndex].describe(this, player);
    }

    public void takeTurn(Player player)
    {
        getBuffs().startTurn();
        setBlock(0);
        moves[moveIndex].execute(this, player);
        getBuffs().endTurn();
        moveIndex = (moveIndex + 1) % moves.length;
        dmg = moves[moveIndex].getBaseDamage();
    }
}
