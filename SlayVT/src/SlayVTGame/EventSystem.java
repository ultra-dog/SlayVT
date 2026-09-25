package SlayVTGame;

import static SlayVTGame.ToolClass.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

/** One-use events inspired by first-act choices, set around Virginia Tech. */
public class EventSystem
{
    public enum EventType
    {
        DOORS("Torgersen Bridge: Light and Shadow"),
        MAZE("Drillfield Detour"),
        BATHS("Duck Pond Respite"),
        LEECH("Newman Library Brainwave"),
        AROMA("Hokie Stone Workshop"),
        AUTOMATON("Goodwin Hall Machine");

        private final String title;

        EventType(String title) { this.title = title; }

        public String getTitle() { return title; }
    }

    private final Random random;
    private final ArrayList<EventType> remaining =
        new ArrayList<EventType>();

    public EventSystem() { this(new Random()); }

    public EventSystem(Random random)
    {
        if (random == null)
        {
            throw new IllegalArgumentException(
                "Random source must not be null.");
        }
        this.random = random;
        Collections.addAll(remaining, EventType.values());
    }

    public boolean hasAvailableEvent(Player player, Deck deck)
    {
        requireParticipants(player, deck);
        for (EventType type : remaining)
        {
            if (isEligible(type, deck)) return true;
        }
        return false;
    }

    public EventType drawEvent(Player player, Deck deck)
    {
        requireParticipants(player, deck);
        ArrayList<EventType> eligible = new ArrayList<EventType>();
        for (EventType type : remaining)
        {
            if (isEligible(type, deck)) eligible.add(type);
        }
        if (eligible.isEmpty())
        {
            throw new IllegalStateException("No unused event is available.");
        }
        EventType chosen = eligible.get(random.nextInt(eligible.size()));
        remaining.remove(chosen);
        return chosen;
    }

    public void visit(Player player, Deck deck)
    {
        EventType event = drawEvent(player, deck);
        while (true)
        {
            int choice = askOption(eventPrompt(event, player), 1, 2);
            Card selectedCard = null;
            CardLibrary selectedReward = null;
            if (event == EventType.DOORS && choice == 2)
            {
                selectedCard = chooseCard(deck, false);
            }
            else if (event == EventType.AROMA)
            {
                selectedCard = chooseCard(deck, choice == 1);
            }
            else if (event == EventType.LEECH && choice == 1)
            {
                selectedReward = chooseFiveCards();
            }
            if (applyChoice(event, choice, player, deck,
                selectedCard, selectedReward))
            {
                println("\n" + outcome(event, choice));
                return;
            }
            println("\nThat option is unavailable. Choose another.");
        }
    }

    // Separate from terminal input so outcomes can be tested directly.
    boolean applyChoice(EventType event, int choice, Player player, Deck deck,
        Card selectedCard, CardLibrary selectedReward)
    {
        requireParticipants(player, deck);
        if (event == null || choice < 1 || choice > 2)
        {
            throw new IllegalArgumentException("Invalid event choice.");
        }
        switch (event)
        {
            case DOORS:
                if (choice == 1) return upgradeTwoRandom(deck);
                return selectedCard != null && deck.removeCard(selectedCard);

            case MAZE:
                if (choice == 1)
                {
                    player.addGold(35);
                    return true;
                }
                if (player.getHp() <= 8) return false;
                player.takeUnblockableDamage(8);
                player.addGold(75);
                return true;

            case BATHS:
                if (choice == 1)
                {
                    player.heal(12);
                    return true;
                }
                if (player.getHp() <= 8) return false;
                player.gainMaxHp(5);
                player.takeUnblockableDamage(8);
                return true;

            case LEECH:
                if (choice == 2) return true;
                if (selectedReward == null
                    || selectedReward.getRarity() == CardRarity.BASIC)
                {
                    return false;
                }
                deck.addCard(selectedReward);
                return true;

            case AROMA:
                if (!containsCard(deck, selectedCard)) return false;
                if (choice == 1) return selectedCard.upgrade();
                return transformCard(deck, selectedCard);

            case AUTOMATON:
                if (choice == 1 && !player.spendGold(25)) return false;
                deck.addCard(randomCard(choice == 1 ? "Power" : null,
                    choice == 2, null));
                return true;

            default:
                throw new IllegalStateException("Unknown event.");
        }
    }

    private String eventPrompt(EventType event, Player player)
    {
        String scene;
        String first;
        String second;
        switch (event)
        {
            case DOORS:
                scene = "At Torgersen Bridge, two archways catch the "
                    + "evening light. One sharpens memory; one lets it go.";
                first = "Take the light path - Upgrade 2 random cards.";
                second = "Take the shadow path - Remove 1 chosen card.";
                break;
            case MAZE:
                scene = "Rain turns the Drillfield paths into a maze. "
                    + "A student group offers a safe route.";
                first = "Join the group - Gain 35 Gold.";
                second = "Go alone - Lose 8 HP; gain 75 Gold.";
                break;
            case BATHS:
                scene = "A quiet stop by Duck Pond restores your breath. "
                    + "A longer dip promises lasting strength.";
                first = "Rest by the water - Heal 12 HP.";
                second = "Immerse - Gain 5 Max HP; lose 8 HP.";
                break;
            case LEECH:
                scene = "A late-night idea strikes in Newman Library. "
                    + "Five unfamiliar techniques fill the page.";
                first = "Study the notes - Choose 1 of 5 random cards.";
                second = "Close the book - Leave without a card.";
                break;
            case AROMA:
                scene = "Dust swirls in a Hokie Stone workshop. "
                    + "Your notes can be refined or rewritten.";
                first = "Refine notes - Upgrade 1 chosen card.";
                second = "Rewrite notes - Transform 1 chosen card.";
                break;
            case AUTOMATON:
                scene = "An old machine hums inside Goodwin Hall. "
                    + "Its two compartments hold different cards.";
                first = "Study the machine - Pay 25 Gold; gain a random Power.";
                second = "Touch the core - Gain a random 0-cost card.";
                break;
            default:
                throw new IllegalStateException("Unknown event.");
        }
        if ((event == EventType.MAZE || event == EventType.BATHS)
            && player.getHp() <= 8)
        {
            second += " [Unavailable: need at least 9 HP]";
        }
        if (event == EventType.AUTOMATON
            && player.getGold() < 25)
        {
            first += " [Unavailable: need 25 Gold]";
        }
        return title("EVENT - " + event.getTitle()) + "\n"
            + detailLine(scene) + "\n\nCHOOSE AN ACTION\n"
            + choiceLine(1, first) + "\n" + choiceLine(2, second);
    }

    private String outcome(EventType event, int choice)
    {
        switch (event)
        {
            case DOORS:
                return choice == 1 ? "Two cards were upgraded."
                    : "The chosen card was removed.";
            case MAZE:
                return choice == 1 ? "You gained 35 Gold."
                    : "You lost 8 HP and gained 75 Gold.";
            case BATHS:
                return choice == 1 ? "You healed 12 HP (up to full)."
                    : "You gained 5 Max HP and lost 8 HP.";
            case LEECH:
                return choice == 1 ? "The chosen card joined your deck."
                    : "You closed the book.";
            case AROMA:
                return choice == 1 ? "The chosen card was upgraded."
                    : "The chosen card was transformed.";
            case AUTOMATON:
                return choice == 1 ? "You bought a random Power."
                    : "You gained a random 0-cost card.";
            default:
                throw new IllegalStateException("Unknown event.");
        }
    }

    private Card chooseCard(Deck deck, boolean upgradeableOnly)
    {
        ArrayList<Card> cards = new ArrayList<Card>();
        for (Card card : deck.getDeck())
        {
            if (!upgradeableOnly || card.canUpgrade()) cards.add(card);
        }
        String prompt = title(upgradeableOnly
            ? "CHOOSE A CARD TO UPGRADE" : "CHOOSE A CARD");
        for (int i = 0; i < cards.size(); i++)
        {
            Card card = cards.get(i);
            prompt += "\n" + choiceLine(i + 1, card.getName())
                + "\n" + detailLine(card.getEffect());
        }
        return cards.get(askOption(prompt, 1, cards.size()) - 1);
    }

    private CardLibrary chooseFiveCards()
    {
        ArrayList<CardLibrary> choices =
            new CardRewardSystem(random).generateChoices(5);
        String prompt = title("CHOOSE A CARD")
            + "\nTake 1 of these 5 cards:";
        for (int i = 0; i < choices.size(); i++)
        {
            Card card = new Card(choices.get(i));
            prompt += "\n\n" + choiceLine(i + 1,
                card.getName() + " [" + card.getCost() + " Energy]")
                + "\n" + detailLine(card.getEffect());
        }
        return choices.get(askOption(prompt, 1, choices.size()) - 1);
    }

    private boolean upgradeTwoRandom(Deck deck)
    {
        ArrayList<Card> cards = upgradeable(deck);
        if (cards.size() < 2) return false;
        Collections.shuffle(cards, random);
        cards.get(0).upgrade();
        cards.get(1).upgrade();
        return true;
    }

    private boolean transformCard(Deck deck, Card selected)
    {
        if (deck.size() <= 1) return false;
        CardLibrary replacement = randomCard(null, false,
            selected.getCardType());
        if (!deck.removeCard(selected)) return false;
        deck.addCard(replacement);
        return true;
    }

    private CardLibrary randomCard(String type, boolean zeroCost,
        CardLibrary excluded)
    {
        ArrayList<CardLibrary> choices = new ArrayList<CardLibrary>();
        for (CardLibrary card : CardLibrary.values())
        {
            if (card.getRarity() != CardRarity.BASIC
                && card != excluded
                && (type == null || type.equals(card.getType()))
                && (!zeroCost || card.getCost() == 0))
            {
                choices.add(card);
            }
        }
        return choices.get(random.nextInt(choices.size()));
    }

    private boolean isEligible(EventType type, Deck deck)
    {
        if (type == EventType.DOORS)
        {
            return upgradeable(deck).size() >= 2 && deck.size() > 1;
        }
        if (type == EventType.AROMA)
        {
            return !upgradeable(deck).isEmpty() && deck.size() > 1;
        }
        return true;
    }

    private ArrayList<Card> upgradeable(Deck deck)
    {
        ArrayList<Card> cards = new ArrayList<Card>();
        for (Card card : deck.getDeck())
        {
            if (card.canUpgrade()) cards.add(card);
        }
        return cards;
    }

    private boolean containsCard(Deck deck, Card card)
    {
        for (Card existing : deck.getDeck())
        {
            if (existing == card) return true;
        }
        return false;
    }

    private static void requireParticipants(Player player, Deck deck)
    {
        if (player == null || deck == null)
        {
            throw new IllegalArgumentException(
                "Player and deck must not be null.");
        }
    }
}
