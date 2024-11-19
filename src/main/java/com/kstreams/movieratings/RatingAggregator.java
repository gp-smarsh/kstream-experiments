package com.kstreams.movieratings;

public record RatingAggregator(int count, int sum) {
    public Double average() {
        return count == 0 ? 0 : (double) sum / count;
    }
}