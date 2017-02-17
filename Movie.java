package com.mio;

/**
 * Encapsulates a OMDB Movie.
 *
 * @author Sergio Lafuente
 */
public class Movie implements Comparable<Movie> {

    /**
     * Constructor.
     */
    public Movie() {
    }
    /**
     * The title.
     */
    private String title;
    /**
     * The year.
     */
    private int year;
    /**
     * The posterUrl.
     */
    private String posterUrl;

    /**
     * Set the title of the movie.
     * @param titleMovie title of the movie
     */
    public void setTitle(final String titleMovie) {
        this.title = titleMovie;
    }

    /**
     * Get the title of the movie.
     * @return title
     */
    public String getTitle() {
        return title;
    }

    /**
     * Set the year of the movie.
     * @param yearMovie year of the movie
     */
    public void setYear(final int yearMovie) {
        this.year = yearMovie;
    }

    /**
     * Get the year of the movie.
     * @return year
     */
    public int getYear() {
        return year;
    }

    /**
     * Set the poster URL of the movie.
     * @param poster the url of the poster
     */
    public void setPoster(final String poster) {
        this.posterUrl = poster;
    }

    /**
     * Get the poster URL of the movie.
     * @return poster
     */
    public String getPoster() {
        return posterUrl;
    }

    @Override
    public int compareTo(final Movie m) {
        if (year < m.year) {
            return -1;
        }
        if (year > m.year) {
            return 1;
        }
        return 0;
    }

}