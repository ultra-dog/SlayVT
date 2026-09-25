# SlayVT Enemy Encounters Design

## Goal and scope

Replace the placeholder fixed-attack enemies with a small, readable roster for the existing 15-floor text game: nine normal enemies, two rarer and stronger elites, and one creative boss on floor 15. A normal enemy may appear only once per run; a new run resets that history. Keep the existing card rewards and unrelated rooms unchanged.

The confirmed implementation approach is a shared set of moves, not one class per enemy or a name-based switch in `BattleSystem`. Enemy-applied temperature follows the same core rules as temperature on monsters.

## Floors and selection

- Floor 1 draws a normal enemy from the early pool. Floors 1-4 use the early pool, 5-8 the middle pool, and 10-13 the late pool. Floor 9 remains a chest, floor 14 a rest site, and floor 15 the sole boss.
- Each pool contains three normal definitions. Selecting a normal fight draws an unused definition from that floor's pool and removes it from the run's available set. A pool may be exhausted; then `Monster` is omitted from room choices until the next pool. This guarantees no repeated normal encounter without requiring twelve designs.
- Elites may be offered on floors 4-7 and 10-12. On each eligible floor, if the current elite band has not had a selected elite fight and the previous floor did not offer Elite, there is a 15% chance that Elite is shown as one option. An Elite offer always has a second non-elite option. At most one elite fight occurs in each band. Skipping an offer does not consume that band's elite fight.
- Normal room choices retain the existing one- or two-option text menu and all non-elite room rules. `Event` remains available, so exhaustion cannot leave an empty menu. Randomness must be injectable for deterministic tests.

## Enemy roster

Numbers below are initial implementation values, not a promise of final balance. `A` means attack damage before Weak/Vulnerable, `B` means block, `W` means Weak turns on the player, `V` means Vulnerable turns on the player, and `T` means temperature change on the player. A plus sign combines effects in one move; arrows show repeating move order.

| Pool | Enemy | HP | Repeating moves |
| --- | --- | ---: | --- |
| Early | Lost Freshman | 16 | A5 -> B5 + A3 |
| Early | Dining Queue Ghost | 18 | A5 -> A3 + W1 |
| Early | Library Wisp | 18 | A5 -> B6 |
| Middle | Lab Spark | 24 | Charge (no damage) -> A11 |
| Middle | Drillfield Crow | 24 | 2 hits of A4 -> A7 |
| Middle | Lecture Hall Frost | 26 | A6 + T-2 -> A8 |
| Late | Construction Golem | 34 | B10 -> A12 |
| Late | All-Nighter Shade | 32 | A7, gaining 2 damage after each attack, capped at A13 |
| Late | Exam Mimic | 32 | V2 -> A12 |
| Early elite | Hokie Stone Guardian | 50 | B10 -> A13 -> 2 hits of A7 |
| Late elite | Overloaded Reactor | 54 | T+1 + Charge -> A15 -> A8 |

The Exam Mimic applies two status turns because the current `Buffs` duration logic decrements one at the end of the player's intervening turn; its following attack must still see Vulnerable. Charge is an intent-bearing move and never deals hidden damage.

### Floor-15 boss: Burruss Bellkeeper

The boss has 90 HP and a visible bell countdown. Its first phase repeats `A8 -> B8 + Charge -> Bell Strike A16`. Once its HP is at or below 45, it changes to `B8 + Charge -> Bell Strike A18`. Crossing the threshold never silently replaces the move already displayed for the current player turn; the shorter cycle begins with the next preview. Charge explicitly previews the next bell strike. The boss does not summon other enemies or add new status types.

## Combat architecture and timing

- An enemy definition supplies name, HP, and a small ordered move sequence. Each `Enemy` instance owns its current sequence position and any per-instance damage growth or boss phase. Keep the existing `Enemy(String, int, int)` constructor for current tests and simple dummy enemies, treating it as a one-move repeating attack.
- A shared move can attack once or multiple times, grant block, apply existing Weak/Vulnerable, change temperature, and/or display Charge. `Enemy` exposes its current intent without advancing. `BattleSystem` executes exactly that intent once on the enemy turn, then advances the move; `ToolClass` displays the same intent before the player chooses cards.
- Weak/Vulnerable affect attack damage through the existing `Buffs.calculateDamage`. Hits resolve separately so block and HP change correctly. Enemy block lasts through the next player turn and is cleared at the start of that enemy's next action, matching current timing.
- Applying temperature to a player uses `Buffs.setTemperature` just as for a monster: same-sign values accumulate; an opposite-sign application replaces the temperature and deals three times the absolute difference as blockable reversal damage, modified by the acting enemy's Weak and the player's Vulnerable. Cold alone has no periodic damage.
- Positive temperature on the player deals end-of-player-turn unblockable heat damage using the same base thresholds as positive temperature on enemies. Player-card offensive heat multipliers, including Red Hot Form, continue to apply only to damage dealt to enemies, not to heat inflicted on the player by enemies. Combat-end cleanup still clears temperature and other temporary buffs.
- If heat or a move kills the player, the battle stops without executing later enemies. If the last enemy dies during the player turn, combat ends before any end-turn heat or enemy action, as it does today.

## Boundaries and verification

This change does not add relics, enemy groups, extra bosses, rewards, or new room types. It does not alter the user's existing `.project` change.

Tests will verify roster size and floor pools, no repeated normal enemy within one run, pool exhaustion, elite eligibility and deterministic offer rate, current intent matching the executed move, move-cycle and boss-phase transitions, status duration, multi-hit/block behavior, temperature stacking/reversal/heat on the player, and preservation of existing card and terminal-layout tests. A short interactive run will verify the text previews and floor-15 boss flow before the code commit.
