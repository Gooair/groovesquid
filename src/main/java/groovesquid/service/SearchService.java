package groovesquid.service;

import com.google.gson.Gson;
import groovesquid.Grooveshark;
import groovesquid.Main;
import groovesquid.model.Album;
import groovesquid.model.Artist;
import groovesquid.model.Playlist;
import groovesquid.model.Song;
import groovesquid.util.Utils;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.imageio.ImageIO;
import javax.swing.JOptionPane;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

/**
 *
 * @author Maino
 */
public class SearchService {

    private final static Logger log = Logger.getLogger(SearchService.class.getName());
    
    Gson gson = new Gson();

    public SearchService() {
        
    }
    
    public List<Song> searchSongs(final Album album) {
        List<Song> songs = new ArrayList<Song>();
        
        HashMap<String, Object>[] result = gson.fromJson(Grooveshark.sendRequest("albumGetAllSongs", new HashMap(){{
            put("albumID", album.getId());
        }}), AlbumResponse.class).getResult();

        for (HashMap<String, Object> hm : result) {
            songs.add(new Song(
                hm.get("SongID"),
                hm.get("Name"),
                hm.get("ArtistID"),
                hm.get("ArtistName"),
                hm.get("AlbumID"),
                hm.get("AlbumName"),
                hm.get("EstimateDuration"),
                hm.get("Year"),
                hm.get("TrackNum")
            ));
        }
        
        songs = filterDuplicateSongs(songs);
        
        return songs;
    }
    
    public List<Song> searchSongsByQuery(final String searchQuery) {
        List<Song> songs = new ArrayList<Song>();
        
        HashMap<String, Object>[] result = gson.fromJson(Grooveshark.sendRequest("getResultsFromSearch", new HashMap(){{
            put("query", searchQuery);
            put("type", new String[] {"Songs", "Artists", "Albums", "Playlists"});
            put("guts", "0");
            put("ppOverride", "false");
        }}), SearchResponse.class).getResult().getResult().getSongs();

        if(result.length < 1) {
            JOptionPane.showMessageDialog(Main.getGui(), "No search results for \"" + searchQuery + "\".");
        }

        for (HashMap<String, Object> hm : result) {
            songs.add(new Song(
                hm.get("SongID"),
                hm.get("SongName"),
                hm.get("ArtistID"),
                hm.get("ArtistName"),
                hm.get("AlbumID"),
                hm.get("AlbumName"),
                hm.get("EstimateDuration"),
                hm.get("Year"),
                hm.get("TrackNum")
            ));
        }

        return songs;
    }
    
    public List<Song> searchPopular() {
        final List<Song> songs = new ArrayList<Song>();

        HashMap<String, Object>[] result = gson.fromJson(Grooveshark.sendRequest("popularGetSongs", new HashMap(){{
            put("type", "daily");
        }}), PopularResponse.class).getResult().getSongs();

        if(result.length < 1) {
            JOptionPane.showMessageDialog(Main.getGui(), "No search results.");
        }

        for (HashMap<String, Object> hm : result) {
            songs.add(new Song(
                hm.get("SongID"), 
                hm.get("Name"),
                hm.get("ArtistID"),
                hm.get("ArtistName"),
                hm.get("AlbumID"),
                hm.get("AlbumName"),
                hm.get("EstimateDuration"),
                hm.get("Year"),
                hm.get("TrackNum")
            ));
        }
        
        return songs;
    }
    
    public List<Song> searchSongsByAlbum(final Album album) {
        List<Song> songs = new ArrayList<Song>();
        
        HashMap<String, Object>[] result = gson.fromJson(Grooveshark.sendRequest("albumGetAllSongs", new HashMap(){{
            put("albumID", album.getId());
        }}), AlbumResponse.class).getResult();

        for (HashMap<String, Object> hm : result) {
            songs.add(new Song(
                hm.get("SongID"),
                hm.get("Name"),
                hm.get("ArtistID"),
                hm.get("ArtistName"),
                hm.get("AlbumID"),
                hm.get("AlbumName"),
                hm.get("EstimateDuration"),
                hm.get("Year"),
                hm.get("TrackNum")
            ));
        }

        songs = filterDuplicateSongs(songs);
        
        return songs;
    }
    
    public List<Song> searchSongsByPlaylist(final Playlist playlist) {
        List<Song> songs = new ArrayList<Song>();
        
        HashMap<String, Object>[] result = gson.fromJson(Grooveshark.sendRequest("playlistGetSongs", new HashMap(){{
            put("playlistID", playlist.getId());
        }}), PopularResponse.class).getResult().getSongs();

        for (HashMap<String, Object> hm : result) {
            songs.add(new Song(
                hm.get("SongID"),
                hm.get("Name"),
                hm.get("ArtistID"),
                hm.get("ArtistName"),
                hm.get("AlbumID"),
                hm.get("AlbumName"),
                hm.get("EstimateDuration"),
                hm.get("Year"),
                hm.get("TrackNum")
            ));
        }
        
        return songs;
    }
    
    
    public List<Song> searchSongsByArtist(final Artist artist) {
        List<Song> songs = new ArrayList<Song>();
        
        HashMap<String, Object>[] result = gson.fromJson(Grooveshark.sendRequest("artistGetArtistSongs", new HashMap(){{
            put("artistID", artist.getId());
        }}), AlbumResponse.class).getResult();

        for (HashMap<String, Object> hm : result) {
            songs.add(new Song(
                hm.get("SongID"),
                hm.get("Name"),
                hm.get("ArtistID"),
                hm.get("ArtistName"),
                hm.get("AlbumID"),
                hm.get("AlbumName"),
                hm.get("EstimateDuration"),
                hm.get("Year"),
                hm.get("TrackNum")
            ));
        }
        
        return songs;
    }

    public List<Album> searchAlbumsByQuery(final String searchQuery) {
        List<Album> albums = new ArrayList<Album>();

        if(searchQuery.contains("http://grooveshark.com/")) {
            Pattern p = Pattern.compile(".*/\\s*(.*)");
            Matcher m = p.matcher(searchQuery);
            if(m.find()) {
                final String albumID = m.group(1);
                Album album = searchAlbumByID(albumID);
                if(album != null) {
                    albums.add(album);
                    return albums;
                }
            }
        }
        
        if(Utils.isNumeric(searchQuery)) {
            Album album = searchAlbumByID(searchQuery);
            if(album != null) {
                albums.add(album);
                return albums;
            }
        }
        
        HashMap<String, Object>[] result = gson.fromJson(Grooveshark.sendRequest("getResultsFromSearch", new HashMap(){{
            put("query", searchQuery);
            put("type", new String[] {"Albums"});
            put("guts", "0");
            put("ppOverride", "false");
        }}), SearchResponse.class).getResult().getResult().getAlbums();

        if(result.length < 1) {
            JOptionPane.showMessageDialog(Main.getGui(), "No search results for \"" + searchQuery + "\".");
        }

        for (HashMap<String, Object> hm : result) {
            albums.add(new Album(
                hm.get("AlbumID"),
                hm.get("Name"),
                hm.get("ArtistID"),
                hm.get("ArtistName")
            ));
        }
        
        return albums;
    }
    
    public List<Playlist> searchPlaylistsByQuery(final String searchQuery) {
        List<Playlist> playlists = new ArrayList<Playlist>();
        
        if(searchQuery.contains("http://grooveshark.com/")) {
            Pattern p = Pattern.compile(".*/\\s*(.*)");
            Matcher m = p.matcher(searchQuery);
            if(m.find()) {
                final String playlistID = m.group(1);
                Playlist playlist = searchPlaylistByID(playlistID);
                if(playlist != null) {
                    playlists.add(playlist);
                    return playlists;
                }
            }
        }
        
        if(Utils.isNumeric(searchQuery)) {
            Playlist playlist = searchPlaylistByID(searchQuery);
            if(playlist != null) {
                playlists.add(playlist);
                return playlists;
            }
        }

        HashMap<String, Object>[] result = gson.fromJson(Grooveshark.sendRequest("getResultsFromSearch", new HashMap(){{
            put("query", searchQuery);
            put("type", new String[] {"Playlists"});
            put("guts", "0");
            put("ppOverride", "false");
        }}), SearchResponse.class).getResult().getResult().getPlaylists();

        if(result.length < 1) {
            JOptionPane.showMessageDialog(Main.getGui(), "No search results for \"" + searchQuery + "\".");
        }

        for (HashMap<String, Object> hm : result) {
            playlists.add(new Playlist(
                hm.get("PlaylistID"),
                hm.get("Name"),
                hm.get("FName"),
                hm.get("NumSongs")
            ));
        }
        
        return playlists;
    }
    
    public List<Artist> searchArtistsByQuery(final String searchQuery) {
        List<Artist> artists = new ArrayList<Artist>();
        
        if(searchQuery.contains("http://grooveshark.com/")) {
            Pattern p = Pattern.compile(".*/\\s*(.*)");
            Matcher m = p.matcher(searchQuery);
            if(m.find()) {
                final String artistID = m.group(1);
                Artist artist = searchArtistByID(artistID);
                if(artist != null) {
                    artists.add(artist);
                    return artists;
                }
            }
        }
        
        if(Utils.isNumeric(searchQuery)) {
            Artist artist = searchArtistByID(searchQuery);
            if(artist != null) {
                artists.add(artist);
                return artists;
            }
        }

        HashMap<String, Object>[] result = gson.fromJson(Grooveshark.sendRequest("getResultsFromSearch", new HashMap(){{
            put("query", searchQuery);
            put("type", new String[] {"Artists"});
            put("guts", "0");
            put("ppOverride", "false");
        }}), SearchResponse.class).getResult().getResult().getArtists();

        if(result.length < 1) {
            JOptionPane.showMessageDialog(Main.getGui(), "No search results for \"" + searchQuery + "\".");
        }

        for (HashMap<String, Object> hm : result) {
            artists.add(new Artist(
                hm.get("ArtistID"),
                hm.get("Name")
            ));
        }
        
        return artists;
    }
    
    public Playlist searchPlaylistByID(final String playlistID) {
        HashMap<String, Object> result = gson.fromJson(Grooveshark.sendRequest("getPlaylistByID", new HashMap(){{
            put("playlistID", playlistID);
        }}), PlaylistResponse.class).getResult();

        if(result.isEmpty() || result.get("PlaylistID").toString().startsWith("0")) {
            return null;
        }
        System.out.println("+++ " + result.get("PlaylistID"));
        Playlist playlist = new Playlist(
            playlistID,
            result.get("Name"),
            result.get("FName"),
            result.get("NumSongs") != null ? result.get("NumSongs") : result.get("SongCount") != null ? result.get("SongCount") : "0"
        );

        return playlist;
    }
    
    public Artist searchArtistByID(final String artistID) {
        HashMap<String, Object> result = gson.fromJson(Grooveshark.sendRequest("getArtistByID", new HashMap(){{
            put("artistID", artistID);
        }}), PlaylistResponse.class).getResult();

        if(result.isEmpty() || result.get("ArtistID").toString().startsWith("0")) {
            return null;
        }
        System.out.println("+++ " + result.get("ArtistID"));
        Artist artist = new Artist(
            artistID,
            result.get("Name")
        );

        return artist;
    }
    
    public Album searchAlbumByID(final String albumID) {
        HashMap<String, Object> result = gson.fromJson(Grooveshark.sendRequest("getAlbumByID", new HashMap(){{
            put("albumID", albumID);
        }}), PlaylistResponse.class).getResult();

        if(result.isEmpty() || result.get("AlbumID").toString().startsWith("0")) {
            return null;
        }

        Album album = new Album(
            result.get("AlbumID"),
            result.get("Name"),
            result.get("ArtistID"),
            result.get("ArtistName")
        );

        return album;
    }
    
    public Song searchSongByID(final String songID) {
        HashMap<String, Object> result = gson.fromJson(Grooveshark.sendRequest("getSongByID", new HashMap(){{
            put("songID", songID);
        }}), PlaylistResponse.class).getResult();

        if(result.isEmpty() || result.get("SongID").toString().startsWith("0")) {
            return null;
        }

        Song song = new Song(
            result.get("SongID"),
            result.get("Name"),
            result.get("ArtistID"),
            result.get("ArtistName"),
            result.get("AlbumID"),
            result.get("AlbumName"),
            result.get("EstimateDuration"),
            result.get("Year"),
            result.get("TrackNum")
        );

        return song;
    }
    
    public List<String> autocompleteByQuery(final String searchQuery) {
        List<String> suggestions = new ArrayList<String>();
        
        HashMap<String, Object>[] result = gson.fromJson(Grooveshark.sendRequest("getAutocomplete", new HashMap(){{
            put("query", searchQuery);
            put("type", "artist");
        }}), AutocompleteResponse.class).getResult();
        
        for (HashMap<String, Object> hm : result) {
            suggestions.add(hm.get("Name").toString().toLowerCase());
        }
        //suggestions.add(result[0].get("Name").toString().toLowerCase());
        
        return suggestions;
    }
    
    private List<Song> filterDuplicateSongs(List<Song> songs) {
        HashSet<Long> allTrackNums = new HashSet<Long>();
        ArrayList<Song> resultList = new ArrayList<Song>(songs.size());
        for (Song song : songs) {
            Long trackNum = song.getTrackNum();
            if (!allTrackNums.contains(trackNum)) {
                resultList.add(song);
                allTrackNums.add(trackNum);
            } else if (trackNum == null) {
                allTrackNums.add(trackNum);
            }
        }
        return resultList;
    }
    
    public Image getLastFmCover(Song song) {
        // Album Info
        String url = "";
        try {
            url = "http://ws.audioscrobbler.com/2.0/?method=album.getinfo&api_key=3a7cb1cc7d5b8537eb05ec2e5fd7ae2a&format=json&artist=" + URLEncoder.encode(song.getArtist().getName(), "UTF-8") + "&album=" + URLEncoder.encode(song.getAlbum().getName(), "UTF-8");
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(SearchService.class.getName()).log(Level.SEVERE, null, ex);
        }
        String responseContent = null;
        BufferedImage img = null;
        HttpEntity httpEntity = null;
        try {
            HttpGet httpGet = new HttpGet(url);
            HttpClient httpClient = HttpClientBuilder.create().build();
            HttpResponse httpResponse = httpClient.execute(httpGet);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            httpEntity = httpResponse.getEntity();
            
            StatusLine statusLine = httpResponse.getStatusLine();
            int statusCode = statusLine.getStatusCode();
            if (statusCode == HttpStatus.SC_OK) {
                httpEntity.writeTo(baos);
            } else {
                throw new RuntimeException(url);
            }

            responseContent = baos.toString("UTF-8");

        } catch (Exception ex) {
            log.log(Level.SEVERE, null, ex);
        } finally {
            try {
                EntityUtils.consume(httpEntity);
            } catch (IOException ex) {
                log.log(Level.SEVERE, null, ex);
            }
        }
        
        LastFmAlbumResponse response = gson.fromJson(responseContent, LastFmAlbumResponse.class);
        String imageUrl = response.getAlbum().getImage()[1].get("#text"); // 64px
        
        if(imageUrl.isEmpty()) {
            return null;
        }
        
        try {
            img = ImageIO.read(new URL(imageUrl));
        } catch (IOException ex) {
            Logger.getLogger(SearchService.class.getName()).log(Level.SEVERE, null, ex);
        }

        return img;
    }
    
    class SearchResponse {
        private HashMap<String, Object> header;
        private Result result;

        public SearchResponse(HashMap<String, Object> header, Result result) {
            this.header = header;
            this.result = result;
        }

        public Result getResult() {
            return this.result;
        }

        class Result {
            private Result2 result;
            
            public Result2 getResult() {
                return this.result;
            }
            
            class Result2 {
                private HashMap<String, Object>[] Songs;
                private HashMap<String, Object>[] Artists;
                private HashMap<String, Object>[] Albums;
                private HashMap<String, Object>[] Playlists;

                public HashMap<String, Object>[] getSongs() {
                    return this.Songs;
                }
                
                public HashMap<String, Object>[] getArtists() {
                    return this.Artists;
                }
                
                public HashMap<String, Object>[] getAlbums() {
                    return this.Albums;
                }
                
                public HashMap<String, Object>[] getPlaylists() {
                    return this.Playlists;
                }
            }
        }
    }
    
    class PopularResponse {
        private HashMap<String, Object> header;
        private Result result;

        public PopularResponse(HashMap<String, Object> header, Result result) {
            this.header = header;
            this.result = result;
        }

        public Result getResult() {
            return this.result;
        }

        class Result {
            private HashMap<String, Object>[] Songs;

            public HashMap<String, Object>[] getSongs() {
                return this.Songs;
            }
        }
    }
    
    class AlbumResponse {
        private HashMap<String, Object> header;
        private HashMap<String, Object>[] result;

        public AlbumResponse(HashMap<String, Object> header, HashMap<String, Object>[] result) {
            this.header = header;
            this.result = result;
        }
        
        public HashMap<String, Object>[] getResult() {
            return result;
        }
    }
    
    class AutocompleteResponse {
        private HashMap<String, Object> header;
        private HashMap<String, Object>[] result;

        public AutocompleteResponse(HashMap<String, Object> header, HashMap<String, Object>[] result) {
            this.header = header;
            this.result = result;
        }

        public HashMap<String, Object>[] getResult() {
            return this.result;
        }
    }
    
    
    class PlaylistResponse {
        private HashMap<String, Object> header;
        private HashMap<String, Object> result;

        public PlaylistResponse(HashMap<String, Object> header, HashMap<String, Object> result) {
            this.header = header;
            this.result = result;
        }
        
        public HashMap<String, Object> getResult() {
            return result;
        }
    }
    
    class LastFmAlbumResponse {
        private LastFmAlbumResponse.Album album;
        
        public LastFmAlbumResponse(LastFmAlbumResponse.Album album) {
            this.album = album;
        }
        
        public LastFmAlbumResponse.Album getAlbum() {
            return album;
        }
        
        class Album {
            private HashMap<String, String>[] image;
            
            public HashMap<String, String>[] getImage() {
                return image;
            }
        }
        
    }
}
