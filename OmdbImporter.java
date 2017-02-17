package com.mio;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 * This class provides an importer to get some info from OMDB api.
 *
 * @author Sergio Lafuente
 */
public class OmdbImporter {

    /** Total results found. */
    private int totalResults;
    /** List of movies. */
    private List<Movie> listMovies;
    /**
     * The source URL.
     */
    private String sourceURL = "http://www.omdbapi.com/?s=";
    /**
     * The final part of the URL.
     */
    private String endURL = "&r=xml";
    /**
     * The final part of the URL.
     */
    private String endPaginationURL = "&r=xml&page=";

    /**
     * Set total results.
     * @param numberResults number of results
     */
    public void setTotalResults(final int numberResults) {
        this.totalResults = numberResults;
    }

    /**
     * Main method for running as a standalone application.
     *
     * @param args the command line arguments.
     */
    public static void main(final String[] args) {
	 boolean status;
        String searchTerm= "";
        if (args.length == 0) {
            System.out.println("Error: you must enter one movie");
            status = false;
        } else {
            int parameters = args.length;
            if (parameters == 1) {
                searchTerm = args[0];
            } else {
                for (int i = 0; i < parameters; i++) {
                    searchTerm = searchTerm + args[i] + "+";
                }
            }
            //Remove the last '+'
            if (searchTerm.contains("+")) {
                searchTerm = searchTerm.substring(0, searchTerm.length() - 1);
            }
            System.out.println("searching for: " + searchTerm);
            OmdbImporter importer = new OmdbImporter();
            status = importer.run(searchTerm);
        }
        System.exit(status ? 0 : 1);
    }
/**
 * Start the execution of the program.
 * @param searchField term to be searched
 * @return true if exit without errors
 */
    private boolean run(final String searchField) {
        try {
            long t = 0;
            // First call to OMDB search api with no pagination
            listMovies = connectionOMDB(searchField, 0);

            // pagination
            System.out.println("Total results " + totalResults);
            int times = (totalResults / 10) + 1;
            System.out.println("CALLS :" + times);

            if (listMovies != null) {
                // Starts from 2 as the 1st one is the first call
                for (int i = 2; i <= times; i++) {
                    // Calls to OMDB search api with pagination
                    List<Movie> moviesToAdd = connectionOMDB(searchField, i);
                    // add movies retrieved to the final list
                    listMovies.addAll(moviesToAdd);
                }
            }

            System.out.println("run(): import completed in:[" + (System.currentTimeMillis() - t) + "ms]");

            Collections.sort(listMovies);

            for (Movie movie : listMovies) {

                System.out.println(movie.getTitle() + " [" + movie.getYear() + "] - " + movie.getPoster());
            }

            // Print out total result
            if (listMovies.size() > 1) {
                System.out.println("\n'" + searchField + "'" + " movies with poster URL => " + listMovies.size() + " results found");
            } else {
                System.out.println("\n'" + searchField + "'" + " movies with poster URL => " + listMovies.size() + " result found");
            }

            return true;
        } catch (IOException | SAXException exception) {
            System.out.println("run(): import failed" + exception);
            return false;
        }
    }

    /**
     * Connects to OMDB API and get info of the movies.
     * @param searchTerm term to be searched
     * @param pagination number of pages
     * @return List of movies found
     * @throws IOException when error occurs
     * @throws SAXException when error occurs
     */
    private List<Movie> connectionOMDB(final String searchTerm, final int pagination) throws IOException, SAXException {
        String finalUrl;
        if (pagination != 0) {
            // Call to OMDB api with pagination
            finalUrl = sourceURL.concat(searchTerm).concat(endPaginationURL).concat(String.valueOf(pagination));

        } else {
            // Call to OMDB api without pagination
            finalUrl = sourceURL.concat(searchTerm).concat(endURL);
        }
        URL url = new URL(finalUrl);

        System.out.println("searching movies: URL:[" + finalUrl + "]");

        try {
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(4000);
            connection.setReadTimeout(60000);
            connection.connect();

            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                throw new IOException("Unable to open connection: " + connection.getResponseMessage() + " code " + connection.getResponseCode());
            }

            OmdbHandler handler = new OmdbHandler();

            InputSource source = new InputSource(connection.getInputStream());
            source.setSystemId(url.toString());

            XMLReader reader = XMLReaderFactory.createXMLReader();
            reader.setContentHandler(handler);
            reader.parse(source);

            connection.disconnect();

            return handler.getMovies();

        } catch (IOException | SAXException e) {
            System.out.println("Error during connection, please try again :"  + e);
            return null;
        }

    }


    /**
     * This class provides a handler for parsing OMDB movies.
     */
    private class OmdbHandler extends DefaultHandler {

        /**
         * The movies parsed by the handler.
         */
        private List<Movie> movies;

        /**
         * The movie currently being parsed.
         */
        private Movie movie;

        private String title;
        private String year;
        private String posterUrl;

        private String totalResults;

        /**
         * The buffer.
         */
        private StringBuilder buf;

        /** The image literal String. */
        private static final String IMAGE = "Image";

        /**
         * {@inheritDoc}
         */
        @Override
        public void startDocument() throws SAXException {
            movies = new ArrayList<Movie>();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void startElement(final String uri, final String localName, final String qName, final Attributes atts) throws SAXException {
            if ("root".equals(localName)) {
                totalResults = atts.getValue("totalResults");
                if (totalResults != null) {
                    setTotalResults(Integer.parseInt(totalResults));
                }
            } else if ("result".equals(localName)) {
                movie = new Movie();
                title = atts.getValue("title");
                year = atts.getValue("year");
                posterUrl = atts.getValue("poster");
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void endElement(final String uri, final String localName, final String qName) throws SAXException {
            if ("result".equals(localName)) {
                movie.setTitle(title);
                movie.setYear(Integer.parseInt(year.substring(0, 4)));
                movie.setPoster(posterUrl);
                //Just add the movies with poster URL
                if (posterUrl != null) {
                    movie.setPoster(posterUrl);
                    movies.add(movie);
                }
                movie = null;
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void characters(final char[] ch, final int start, final int length) throws SAXException {
            if (buf != null) {
                buf.append(ch, start, length);
            }
        }

        /**
         * Get the movies parsed by the handler.
         * @return the movies parsed by the handler
         */
        public List<Movie> getMovies() {
            return movies;
        }

    }

}
