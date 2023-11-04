package group.aelysium.rustyconnector.plugin.velocity.lib.family.ranked_family.players;

import de.gesundkrank.jskills.Rating;

public class ScoreCard {
    protected String game;
    protected Rating rating;

    protected ScoreCard(String gameName, Rating rating) {
        this.game = gameName;
        this.rating = rating;
    }

    public String game() {
        return game;
    }

    public Rating rating() {
        return rating;
    }

    public static ScoreCard create(String gameName, Rating rating) {
        return new ScoreCard(gameName, rating);
    }
    public static ScoreCard create(String gameName) {
        return new ScoreCard(gameName, new Rating(20, 3));
    }
}
