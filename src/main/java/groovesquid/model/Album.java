package groovesquid.model;

/**
 *
 * @author Maino
 */
public class Album {
    private String id;
    private String name;
    private Artist artist;
    private String imageUrl;
    
    public Album(Object id, Object name, Object artistId, Object artistName) {
        this.id = id.toString();
        this.name = name.toString().trim();
        this.artist = new Artist(artistId, artistName);
    }
    
    public Album(Object id, Object name, Artist artist) {
        this.id = id.toString();
        this.name = name.toString().trim();
        this.artist = artist;
    }
    
    public String getId() {
        return id;
    }
    
    public String getName() {
        return name;
    }
    
    public Artist getArtist() {
        return artist;
    }
    
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
    
    public String getImageUrl() {
        return imageUrl;
    }
}
