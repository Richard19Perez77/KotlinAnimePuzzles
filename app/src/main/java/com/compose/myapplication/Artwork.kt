package com.compose.myapplication

/**
 * A class to hold a piece of artwork, this includes the title of artists and artwork as well as the link to the artist and an imageID from resources.
 * Created by Richard on 11/7/2015.
 */
class Artwork
/**
 * Constructor for the Artwork
 *
 * @param artworkTitle the title of the artwork
 * @param artistTitle  the title of the artist
 * @param artistURL    the link to the artwork or artists page
 * @param artworkID    a resource id
 */(
    @JvmField var titleOfArtwork: String,
    @JvmField var titleOfArtist: String,
    var urlOfArtist: String,
    @JvmField var imageID: Int
)