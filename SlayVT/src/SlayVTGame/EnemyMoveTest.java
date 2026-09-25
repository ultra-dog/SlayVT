package SlayVTGame;

public class EnemyMoveTest
{
    private static int checks;

    public static void main(String[] args)
    {
        Player player = new Player("Tester", 80);
        Enemy legacy = new Enemy("Legacy", 20, 5);
        check("legacy intent", legacy.getIntent(player).contains("5 damage"));
        legacy.takeTurn(player);
        check("legacy attack", player.getHp() == 75);
        legacy.setDmg(7);
        check("legacy setter changes intent",
            legacy.getIntent(player).contains("7 damage"));

        Enemy guard = new Enemy("Guard", 30,
            EnemyMove.attackAndBlock(3, 5), EnemyMove.multiAttack(4, 2));
        guard.takeTurn(player);
        check("guard gains block", guard.getBlock() == 5);
        check("next intent is two hits",
            guard.getIntent(player).contains("2 hits"));
        guard.takeTurn(player);
        check("old block clears", guard.getBlock() == 0);

        CountingPlayer fragile = new CountingPlayer("Fragile", 3);
        Enemy striker = new Enemy("Striker", 20,
            EnemyMove.multiAttack(4, 2));
        striker.takeTurn(fragile);
        check("stops after lethal hit", fragile.getHp() == 0);
        check("only one hit was applied", fragile.hits == 1);
        check("one action advances once",
            striker.getIntent(fragile).contains("2 hits"));

        Player target = new Player("Target", 80);
        Enemy weakened = new Enemy("Weakened", 20, EnemyMove.attack(10));
        weakened.getBuffs().addWeak(1);
        target.getBuffs().addVulnerable(1);
        check("intent includes status modifiers",
            weakened.getIntent(target).contains("11 damage"));
        weakened.takeTurn(target);
        check("executed damage matches intent", target.getHp() == 69);

        Player statusTarget = new Player("Status", 80);
        Enemy ghost = new Enemy("Ghost", 20,
            EnemyMove.attackAndWeak(3, 1), EnemyMove.vulnerable(2));
        ghost.takeTurn(statusTarget);
        statusTarget.getBuffs().startTurn();
        check("weak lasts through player turn",
            statusTarget.getBuffs().isWeak());
        statusTarget.getBuffs().endTurn();
        check("weak expires after player turn",
            !statusTarget.getBuffs().isWeak());

        ghost.takeTurn(statusTarget);
        check("vulnerable is applied",
            statusTarget.getBuffs().getVulnerableTurns() == 2);
        statusTarget.getBuffs().startTurn();
        statusTarget.getBuffs().endTurn();
        check("vulnerable remains for next attack",
            statusTarget.getBuffs().getVulnerableTurns() == 1);

        Player chargedTarget = new Player("Charged", 80);
        Enemy charger = new Enemy("Charger", 20,
            EnemyMove.charge(4, 11), EnemyMove.attack(11));
        check("charge previews next hit",
            charger.getIntent(chargedTarget).contains("11 damage"));
        charger.takeTurn(chargedTarget);
        check("charge does not attack", chargedTarget.getHp() == 80);
        check("charge can grant block", charger.getBlock() == 4);

        Player growingTarget = new Player("Growing", 100);
        Enemy growing = new Enemy("Growing", 20,
            EnemyMove.growingAttack(7, 2, 13));
        int[] expectedDamage = {7, 9, 11, 13, 13};
        for (int damage : expectedDamage)
        {
            check("growing intent " + damage,
                growing.getIntent(growingTarget).contains(
                    damage + " damage"));
            growing.takeTurn(growingTarget);
        }

        System.out.println("PASS: " + checks + " enemy move checks.");
    }

    private static void check(String label, boolean passed)
    {
        checks++;
        if (!passed)
        {
            throw new AssertionError(label);
        }
    }

    private static class CountingPlayer extends Player
    {
        int hits;

        CountingPlayer(String name, int hp)
        {
            super(name, hp);
        }

        @Override
        public void takeDamage(int amount)
        {
            hits++;
            super.takeDamage(amount);
        }
    }
}
