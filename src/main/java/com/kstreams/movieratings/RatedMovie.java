package com.kstreams.movieratings;

public record RatedMovie(long id, String title, int releaseYear, double rating) {
}