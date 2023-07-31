package com.compose.myapplication;

/**
 * A class to hold a piece of artwork, this includes the title of artists and artwork as well as the link to the artist and an imageID from resources.
 * Created by Richard on 11/7/2015.
 */
public class Artwork {

    public String titleOfArtwork;
    public String titleOfArtist;
    public String urlOfArtist;
    public int imageID;

    /**
     * Constructor for the Artwork
     *
     * @param artworkTitle the title of the artwork
     * @param artistTitle  the title of the artist
     * @param artistURL    the link to the artwork or artists page
     * @param artworkID    a resource id
     */
    public Artwork(String artworkTitle, String artistTitle, String artistURL, int artworkID) {
        titleOfArtwork = artworkTitle;
        titleOfArtist = artistTitle;
        urlOfArtist = artistURL;
        imageID = artworkID;
    }
}
