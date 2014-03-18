package groovesquid.gui;

import groovesquid.model.Album;
import groovesquid.model.Song;
import groovesquid.model.Playlist;
import groovesquid.*;
import groovesquid.Config.DownloadComplete;
import groovesquid.model.*;
import groovesquid.service.DownloadListener;
import groovesquid.service.PlayService;
import groovesquid.service.PlayServiceListener;
import groovesquid.service.Services;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.apache.commons.lang3.ArrayUtils;


/**
 *
 * @author Maino
 */

public class GUI extends PFrame {
    
    private ArrayList<String> autocompleteList = new ArrayList<String>();
    private String searchTextFieldOriginal;

    protected ImageIcon playIcon, playIconActive, pauseIcon, pauseIconActive, nextIcon, nextIconActive, previousIcon, previousIconActive;/*, minimizeButtonImage, minimizeButtonHoverImage, maximizeButtonImage, maximizeButtonHoverImage, closeButtonImage, closeButtonHoverImage, blueArrowSouth, smallBlueArrowSouth, blueArrowNorth, smallBlueArrowNorth, orangeArrowSouth, smallOrangeArrowSouth, orangeArrowNorth, smallOrangeArrowNorth, facebookIcon, twitterIcon;
    private Image blueButton, blueButtonHover, blueButtonPressed, orangeButton, orangeButtonHover, orangeButtonPressed, dividerImage;*/

    /**
     * Creates new form GUI
     */
    public GUI() {

    }
    
    protected void initGui() {
        // undecorated
        dispose();
        setUndecorated(true);
        
        // title
        setTitle("Groovesquid");

        // center screen
        setLocationRelativeTo(null);
        
        setVisible(true);
        
        // background fix
        getContentPane().setBackground(getBackground());
        
        // icon
        setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/groovesquid/gui/icon.png")));
        
        titleBarLabel.setIcon(new ImageIcon(getClass().getResource("/groovesquid/gui/titlebar.png")));
        
        // tables
        ((DownloadTableModel)downloadTable.getModel()).setSongDownloads(Main.getConfig().getDownloads());
        
        searchTable.getSelectionModel().addListSelectionListener(searchListSelectionListener);
        downloadTable.getSelectionModel().addListSelectionListener(downloadListSelectionListener);
        
        Services.getPlayService().setListener(playServiceListener);
    }
    
    ListSelectionListener downloadListSelectionListener = new ListSelectionListener(){
        public void valueChanged(ListSelectionEvent event) {
            if (!event.getValueIsAdjusting()) {
                int[] selectedRows = downloadTable.getSelectedRows();
                if(selectedRows.length > 0) {
                    removeFromListButton.setEnabled(true);
                    removeFromListButton.setText(Main.getLocaleString("REMOVE_FROM_LIST") + " (" + selectedRows.length + ")");
                    removeFromDiskButton.setEnabled(true);
                    removeFromDiskButton.setText(Main.getLocaleString("REMOVE_FROM_DISK") + " (" + selectedRows.length + ")");
                } else {
                    removeFromListButton.setEnabled(false);
                    removeFromListButton.setText(Main.getLocaleString("REMOVE_FROM_LIST"));
                    removeFromDiskButton.setEnabled(false);
                    removeFromDiskButton.setText(Main.getLocaleString("REMOVE_FROM_DISK"));
                }
            }
        }
    };
    
    ListSelectionListener searchListSelectionListener = new ListSelectionListener(){
        public void valueChanged(ListSelectionEvent event) {
            if (!event.getValueIsAdjusting()) {
                int[] selectedRows = searchTable.getSelectedRows();

                String playButtonText = Main.getLocaleString("PLAY");

                if(searchTable.getModel().getClass() == AlbumSearchTableModel.class || searchTable.getModel().getClass() == PlaylistSearchTableModel.class || searchTable.getModel().getClass() == ArtistSearchTableModel.class) {
                    playButtonText = Main.getLocaleString("SHOW_SONGS");
                }
                if(selectedRows.length > 0) {
                    downloadButton.setEnabled(true);
                    downloadButton.setText(Main.getLocaleString("DOWNLOAD") + " (" + selectedRows.length + ")");
                    playButton.setEnabled(true);
                    playButton.setText(playButtonText + " (" + selectedRows.length + ")");
                } else {
                    downloadButton.setEnabled(false);
                    downloadButton.setText(Main.getLocaleString("DOWNLOAD"));
                    playButton.setEnabled(false);
                    playButton.setText(playButtonText);
                }
            }
        }
    };
    
    private PlayServiceListener playServiceListener = new PlayServiceListener() {
        public void playbackStarted(Track track) {
            playPauseButton.setIcon(pauseIcon);
        }

        public void playbackPaused(Track track, int audioPosition) {
            playPauseButton.setIcon(playIcon);
        }

        public void playbackFinished(Track track, int audioPosition) {
            resetPlayInfo();
        }

        public void positionChanged(Track track, int audioPosition) {
            trackSlider.setValue(audioPosition / 1000);
            String currentPos = String.format("%02d:%02d", TimeUnit.MILLISECONDS.toMinutes(audioPosition), TimeUnit.MILLISECONDS.toSeconds(audioPosition) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(audioPosition)));
            currentDurationLabel.setText(currentPos);
        }

        public void exception(Track track, Exception ex) {

        }

        public void statusChanged(Track track) {
            if (track.getStatus() == Track.Status.ERROR) {
                resetPlayInfo();
                Services.getPlayService().stop();
            } else if(track.getStatus() == Track.Status.INITIALIZING) {
                updateCurrentlyPlayingTrack(track);
            } else if(track.getStatus() == Track.Status.DOWNLOADING) {
                trackSlider.setMaximum(track.getSong().getDuration().intValue());
                durationLabel.setText(track.getSong().getReadableDuration());
                //System.out.println(track.getTotalBytes() * 8 / track.getSong().getDuration() / 1000 + "KBP/S");
                
            } else if(track.getStatus() == Track.Status.FINISHED) {
                //currentlyPlayingLabel.setText(currentlyPlayingLabel.getText() + " (" + ((MemoryStore)track.getStore()) + "kbps)");
            }
        }

        public void downloadedBytesChanged(Track track) {

        }

        private void updateCurrentlyPlayingTrack(final Track track) {
            currentlyPlayingLabel.setText(String.format("<html><b>%s</b><br/><em>%s</em></html>", track.getSong().getName(), track.getSong().getArtist().getName()));
            trackSlider.setEnabled(true);
            trackSlider.setMaximum(track.getSong().getDuration().intValue());
            durationLabel.setText(track.getSong().getReadableDuration());
            
            SwingWorker<Image, Void> worker = new SwingWorker<Image, Void>(){
                @Override
                protected Image doInBackground() {
                    return Services.getSearchService().getLastFmCover(track.getSong());
                }

                @Override
                protected void done() {
                    try {
                        Image img = get();
                        if(img != null) {
                            img = img.getScaledInstance(albumCoverLabel.getSize().width, albumCoverLabel.getSize().height,  java.awt.Image.SCALE_SMOOTH ) ; 
                            albumCoverLabel.setIcon(new ImageIcon(img));
                        } else {
                            
                            albumCoverLabel.setIcon(null);
                        }
                    } catch (InterruptedException ex) {
                        Logger.getLogger(GUI.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (ExecutionException ex) {
                        Logger.getLogger(GUI.class.getName()).log(Level.SEVERE, null, ex);
                    }

                }
            };
            worker.execute();
        }
    };
    
    public void playButtonActionPerformed(java.awt.event.ActionEvent evt) {                                           
        int[] selectedRows = searchTable.getSelectedRows();

        if (searchTable.getModel().getClass() == SongSearchTableModel.class) {
            SongSearchTableModel model = (SongSearchTableModel) searchTable.getModel();
            List<Song> songs = new ArrayList<Song>();

            for(int selectedRow : selectedRows) {
                selectedRow = searchTable.convertRowIndexToModel(selectedRow);
                songs.add(model.getSongs().get(selectedRow));
            }
        
            play(songs);
        } else if (searchTable.getModel().getClass() == AlbumSearchTableModel.class) {
            searchTable.setEnabled(false);
            searchTypeComboBox.setEnabled(false);
            searchTextField.setEnabled(false);
            searchButton.setEnabled(false);

            AlbumSearchTableModel model = (AlbumSearchTableModel) searchTable.getModel();
            final List<Album> albums = new ArrayList<Album>();

            for(int selectedRow : selectedRows) {
                selectedRow = searchTable.convertRowIndexToModel(selectedRow);
                albums.add(model.getAlbums().get(selectedRow));
            }
            
            SwingWorker<List<Song>, Void> worker = new SwingWorker<List<Song>, Void>(){

                @Override
                protected List<Song> doInBackground() {
                    List<Song> songs = new ArrayList<Song>();
                    for(Album album : albums) {
                        songs.addAll(Services.getSearchService().searchSongsByAlbum(album));
                    }
                    return songs;
                }

                @Override
                protected void done() {
                    try {
                        searchTable.setModel(new SongSearchTableModel(get()));
                    } catch (InterruptedException ex) {
                        Logger.getLogger(GUI.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (ExecutionException ex) {
                        Logger.getLogger(GUI.class.getName()).log(Level.SEVERE, null, ex);
                    }

                    searchTable.setEnabled(true);
                    searchTypeComboBox.setEnabled(true);
                    searchTextField.setEnabled(true);
                    searchButton.setEnabled(true);
                }
            };
            worker.execute();
            
        } else if (searchTable.getModel().getClass() == PlaylistSearchTableModel.class) {
            searchTable.setEnabled(false);
            searchTypeComboBox.setEnabled(false);
            searchTextField.setEnabled(false);
            searchButton.setEnabled(false);

            PlaylistSearchTableModel model = (PlaylistSearchTableModel) searchTable.getModel();
            final List<Playlist> playlists = new ArrayList<Playlist>();

            for(int selectedRow : selectedRows) {
                selectedRow = searchTable.convertRowIndexToModel(selectedRow);
                playlists.add(model.getPlaylists().get(selectedRow));
            }
            
            SwingWorker<List<Song>, Void> worker = new SwingWorker<List<Song>, Void>(){

                @Override
                protected List<Song> doInBackground() {
                    List<Song> songs = new ArrayList<Song>();
                    for(Playlist playlist : playlists) {
                        songs.addAll(Services.getSearchService().searchSongsByPlaylist(playlist));
                    }
                    return songs;
                }

                @Override
                protected void done() {
                    try {
                        searchTable.setModel(new SongSearchTableModel(get()));
                    } catch (InterruptedException ex) {
                        Logger.getLogger(GUI.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (ExecutionException ex) {
                        Logger.getLogger(GUI.class.getName()).log(Level.SEVERE, null, ex);
                    }

                    searchTable.setEnabled(true);
                    searchTypeComboBox.setEnabled(true);
                    searchTextField.setEnabled(true);
                    searchButton.setEnabled(true);
                }
            };
            worker.execute();
            
        } else if (searchTable.getModel().getClass() == ArtistSearchTableModel.class) {
            searchTable.setEnabled(false);
            searchTypeComboBox.setEnabled(false);
            searchTextField.setEnabled(false);
            searchButton.setEnabled(false);

            ArtistSearchTableModel model = (ArtistSearchTableModel) searchTable.getModel();
            final List<Artist> artists = new ArrayList<Artist>();

            for(int selectedRow : selectedRows) {
                selectedRow = searchTable.convertRowIndexToModel(selectedRow);
                artists.add(model.getArtists().get(selectedRow));
            }
            
            SwingWorker<List<Song>, Void> worker = new SwingWorker<List<Song>, Void>(){

                @Override
                protected List<Song> doInBackground() {
                    List<Song> songs = new ArrayList<Song>();
                    for(Artist artist : artists) {
                        songs.addAll(Services.getSearchService().searchSongsByArtist(artist));
                    }
                    return songs;
                }

                @Override
                protected void done() {
                    try {
                        searchTable.setModel(new SongSearchTableModel(get()));
                    } catch (InterruptedException ex) {
                        Logger.getLogger(GUI.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (ExecutionException ex) {
                        Logger.getLogger(GUI.class.getName()).log(Level.SEVERE, null, ex);
                    }

                    searchTable.setEnabled(true);
                    searchTypeComboBox.setEnabled(true);
                    searchTextField.setEnabled(true);
                    searchButton.setEnabled(true);
                }
            };
            worker.execute();
        }
        
        searchTable.getSelectionModel().clearSelection();
    }
    
    private DownloadListener getDownloadListener(final DownloadTableModel downloadTableModel) {
        final DownloadListener downloadListener = new DownloadListener() {
            @Override
            public void downloadedBytesChanged(Track track) {
                int row = downloadTableModel.getSongDownloads().indexOf(track);
                if(row >= 0) {
                    downloadTableModel.fireTableCellUpdated(row, 5);
                }
            }

            public void statusChanged(Track track) {
                int row = downloadTableModel.getSongDownloads().indexOf(track);
                if(row >= 0) {
                    downloadTableModel.fireTableCellUpdated(row, 5);

                }
                downloadTableModel.updateSongDownloads();

                // fire download completed action
                if(track.getStatus() == Track.Status.FINISHED) {
                    if(Main.getConfig().getDownloadComplete() == DownloadComplete.OPEN_FILE.ordinal()) {
                        try {
                            // open file
                            Desktop.getDesktop().open(new File(track.getPath()));
                        } catch (IOException ex) {
                            Logger.getLogger(GUI.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    } else if(Main.getConfig().getDownloadComplete() == DownloadComplete.OPEN_DIRECTORY.ordinal()) {
                        try {
                            // open dir
                            Desktop.getDesktop().open(new File(track.getPath()).getParentFile());
                        } catch (IOException ex) {
                            Logger.getLogger(GUI.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                }
            }
        };
        return downloadListener;
    }

    public void downloadButtonActionPerformed(java.awt.event.ActionEvent evt) {                                               
        int[] selectedRows = searchTable.getSelectedRows();

        final DownloadTableModel downloadTableModel = (DownloadTableModel) downloadTable.getModel();
        for (int selectedRow : selectedRows) {
            // fix
            selectedRow = searchTable.convertRowIndexToModel(selectedRow);
            
            if (searchTable.getModel().getClass() == SongSearchTableModel.class) {
                SongSearchTableModel songSearchTableModel = (SongSearchTableModel) searchTable.getModel();
                Song song = songSearchTableModel.getSongs().get(selectedRow);
                downloadTableModel.addRow(0, Services.getDownloadService().download(song, getDownloadListener(downloadTableModel)));
            } else if (searchTable.getModel().getClass() == AlbumSearchTableModel.class) {
                AlbumSearchTableModel albumSearchTableModel = (AlbumSearchTableModel) searchTable.getModel();
                final Album album = albumSearchTableModel.getAlbums().get(selectedRow);
                SwingWorker<List<Song>, Void> worker = new SwingWorker<List<Song>, Void>(){

                    @Override
                    protected List<Song> doInBackground() {
                        return Services.getSearchService().searchSongsByAlbum(album);
                    }

                    @Override
                    protected void done() {
                        try {
                            Iterator<Song> iterator = get().iterator();
                            while (iterator.hasNext()) {
                                downloadTableModel.addRow(0, Services.getDownloadService().download(iterator.next(), getDownloadListener(downloadTableModel)));
                            }
                        } catch (InterruptedException ex) {
                            Logger.getLogger(GUI.class.getName()).log(Level.SEVERE, null, ex);
                        } catch (ExecutionException ex) {
                            Logger.getLogger(GUI.class.getName()).log(Level.SEVERE, null, ex);
                        }

                    }
                };
                worker.execute();

            } else if (searchTable.getModel().getClass() == PlaylistSearchTableModel.class) {
                PlaylistSearchTableModel playlistSearchTableModel = (PlaylistSearchTableModel) searchTable.getModel();
                final Playlist playlist = playlistSearchTableModel.getPlaylists().get(selectedRow);
                SwingWorker<List<Song>, Void> worker = new SwingWorker<List<Song>, Void>(){

                    @Override
                    protected List<Song> doInBackground() {
                        return Services.getSearchService().searchSongsByPlaylist(playlist);
                    }

                    @Override
                    protected void done() {
                        try {
                            Iterator<Song> iterator = get().iterator();
                            while (iterator.hasNext()) {
                                downloadTableModel.addRow(0, Services.getDownloadService().download(iterator.next(), getDownloadListener(downloadTableModel)));
                            }
                        } catch (InterruptedException ex) {
                            Logger.getLogger(GUI.class.getName()).log(Level.SEVERE, null, ex);
                        } catch (ExecutionException ex) {
                            Logger.getLogger(GUI.class.getName()).log(Level.SEVERE, null, ex);
                        }

                    }
                };
                worker.execute();

            }
        }
        searchTable.getSelectionModel().clearSelection();
    }                                              

    public void searchTableMousePressed(java.awt.event.MouseEvent evt) {                                         
        /*
         * if(evt.getClickCount() >= 2) { JTable table =
         * (JTable)evt.getSource(); Point p = evt.getPoint(); int row =
         * table.rowAtPoint(p); int col = table.columnAtPoint(p); String value =
         * (String)table.getValueAt(row,col); SearchTableModel model =
         * (SearchTableModel) searchTable.getModel(); Song song =
         * model.getSongs().get(row); new DownloadThread(song).start(); }
         */

    }                                        

    public void removeFromListButtonActionPerformed(java.awt.event.ActionEvent evt) {                                                     
        removeFromList(false);
    }

    public void removeFromDiskButtonActionPerformed(java.awt.event.ActionEvent evt) {                                                     
        int[] selectedRows = downloadTable.getSelectedRows();
        if (JOptionPane.showConfirmDialog(null, String.format(Main.getLocaleString("ALERT_REMOVE_FILES_FROM_DISK"), Integer.toString(selectedRows.length)), Main.getLocaleString("ALERT"), JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == 0) {
            removeFromList(true);
        }
    }

    public void retryFailedDownloadsButtonActionPerformed(java.awt.event.ActionEvent evt) {                                                     
        int[] selectedRows = downloadTable.getSelectedRows();
        DownloadTableModel model = (DownloadTableModel) downloadTable.getModel();
        for (int i = 0; i < selectedRows.length; i++) {
            int selectedRow = downloadTable.convertRowIndexToModel(selectedRows[i] - i);
            Track track = model.getSongDownloads().get(selectedRow);
            if(track.getStatus() == Track.Status.ERROR) {
                Services.getDownloadService().cancelDownload(track, false);
                final DownloadTableModel downloadTableModel = (DownloadTableModel) downloadTable.getModel();
                downloadTableModel.addRow(0, Services.getDownloadService().download(track.getSong(), getDownloadListener(downloadTableModel)));
            }
        }
        downloadTable.clearSelection();
    }
    
    public void searchTypeComboBoxActionPerformed(java.awt.event.ActionEvent evt) {                                                   
        // POPULAR
        if (searchTypeComboBox.getSelectedIndex() == 1) {
            searchTextField.setText("");
            // fire actionPerformed event at searchButton
            for (ActionListener a : searchButton.getActionListeners()) {
                a.actionPerformed(evt);
            }
        } else {
            searchTextField.setEnabled(true);
            searchTextField.requestFocus();
        }
    }                                                  

    public void searchButtonActionPerformed(java.awt.event.ActionEvent evt) {                                             
        searchTypeComboBox.setEnabled(false);
        searchTextField.setEnabled(false);
        searchButton.setEnabled(false);
        playButton.setText(Main.getLocaleString("PLAY"));

        // Songs
        if (searchTypeComboBox.getSelectedIndex() == 0) {
            SwingWorker<List<Song>, Void> worker = new SwingWorker<List<Song>, Void>(){

                @Override
                protected List<Song> doInBackground() {
                    return Services.getSearchService().searchSongsByQuery(searchTextField.getText());
                }

                @Override
                protected void done() {
                    try {
                        searchTable.setModel(new SongSearchTableModel(get()));
                    } catch (InterruptedException ex) {
                        Logger.getLogger(GUI.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (ExecutionException ex) {
                        Logger.getLogger(GUI.class.getName()).log(Level.SEVERE, null, ex);
                    }

                    searchTable.setEnabled(true);
                    searchTypeComboBox.setEnabled(true);
                    searchTextField.setEnabled(true);
                    searchButton.setEnabled(true);
                }
            };
            worker.execute();
        // Popular
        } else if (searchTypeComboBox.getSelectedIndex() == 1) {
            SwingWorker<List<Song>, Void> worker = new SwingWorker<List<Song>, Void>(){

                @Override
                protected List<Song> doInBackground() {
                    return Services.getSearchService().searchPopular();
                }

                @Override
                protected void done() {
                    try {
                        searchTable.setModel(new SongSearchTableModel(get()));
                    } catch (InterruptedException ex) {
                        Logger.getLogger(GUI.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (ExecutionException ex) {
                        Logger.getLogger(GUI.class.getName()).log(Level.SEVERE, null, ex);
                    }

                    searchTable.setEnabled(true);
                    searchTypeComboBox.setEnabled(true);
                    searchButton.setEnabled(true);
                }
            };
            worker.execute();
        // Albums
        } else if (searchTypeComboBox.getSelectedIndex() == 2) {
            SwingWorker<List<Album>, Void> worker = new SwingWorker<List<Album>, Void>(){

                @Override
                protected List<Album> doInBackground() {
                    return Services.getSearchService().searchAlbumsByQuery(searchTextField.getText());
                }

                @Override
                protected void done() {
                    try {
                        searchTable.setModel(new AlbumSearchTableModel(get()));
                    } catch (InterruptedException ex) {
                        Logger.getLogger(GUI.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (ExecutionException ex) {
                        Logger.getLogger(GUI.class.getName()).log(Level.SEVERE, null, ex);
                    }

                    searchTable.setEnabled(true);
                    searchTypeComboBox.setEnabled(true);
                    searchTextField.setEnabled(true);
                    searchButton.setEnabled(true);
                }
            };
            worker.execute();
        // Playlists
        } else if (searchTypeComboBox.getSelectedIndex() == 3) {
            SwingWorker<List<Playlist>, Void> worker = new SwingWorker<List<Playlist>, Void>(){

                @Override
                protected List<Playlist> doInBackground() {
                    return Services.getSearchService().searchPlaylistsByQuery(searchTextField.getText());
                }

                @Override
                protected void done() {
                    try {
                        searchTable.setModel(new PlaylistSearchTableModel(get()));
                    } catch (InterruptedException ex) {
                        Logger.getLogger(GUI.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (ExecutionException ex) {
                        Logger.getLogger(GUI.class.getName()).log(Level.SEVERE, null, ex);
                    }

                    searchTable.setEnabled(true);
                    searchTypeComboBox.setEnabled(true);
                    searchTextField.setEnabled(true);
                    searchButton.setEnabled(true);
                }
            };
            worker.execute();
            
        // Artists
        } else if (searchTypeComboBox.getSelectedIndex() == 4) {
            SwingWorker<List<Artist>, Void> worker = new SwingWorker<List<Artist>, Void>(){

                @Override
                protected List<Artist> doInBackground() {
                    return Services.getSearchService().searchArtistsByQuery(searchTextField.getText());
                }

                @Override
                protected void done() {
                    try {
                        searchTable.setModel(new ArtistSearchTableModel(get()));
                    } catch (InterruptedException ex) {
                        Logger.getLogger(GUI.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (ExecutionException ex) {
                        Logger.getLogger(GUI.class.getName()).log(Level.SEVERE, null, ex);
                    }

                    searchTable.setEnabled(true);
                    searchTypeComboBox.setEnabled(true);
                    searchTextField.setEnabled(true);
                    searchButton.setEnabled(true);
                }
            };
            worker.execute();
        }
        
    }                                            

    public void selectComboBoxActionPerformed(java.awt.event.ActionEvent evt) {                                               
        // All
        if(selectComboBox.getSelectedIndex() == 1) {
            DownloadTableModel model = (DownloadTableModel) downloadTable.getModel();
            downloadTable.setRowSelectionInterval(0, model.getRowCount() - 1);
        // Completed
        } else if(selectComboBox.getSelectedIndex() == 2) {
            DownloadTableModel model = (DownloadTableModel) downloadTable.getModel();
            for (int i = 0; i < model.getRowCount(); i++) {
                if(model.getSongDownloads().get(i).getProgress() == 100) {
                    if(downloadTable.getSelectedRows().length > 0) {
                        downloadTable.addRowSelectionInterval(i, i);
                    } else {
                        downloadTable.setRowSelectionInterval(i, i);
                    }
                }
            }
        // Failed
        } else if(selectComboBox.getSelectedIndex() == 3) {
            DownloadTableModel model = (DownloadTableModel) downloadTable.getModel();
            for (int i = 0; i < model.getRowCount(); i++) {
                if(model.getSongDownloads().get(i).getStatus() == Track.Status.ERROR) {
                    if(downloadTable.getSelectedRows().length > 0) {
                        downloadTable.addRowSelectionInterval(i, i);
                    } else {
                        downloadTable.setRowSelectionInterval(i, i);
                    }
                }
            }
        }
        selectComboBox.setSelectedIndex(0);
    }                                              

    public void formWindowClosing(java.awt.event.WindowEvent evt) {                                   
        if(Services.getDownloadService().areCurrentlyRunningDownloads()) {
            if(JOptionPane.showConfirmDialog(this, Main.getLocaleString("ALERT_DOWNLOADS_IN_PROGRESS"), Main.getLocaleString("ALERT"), JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == 0) {
                System.exit(0);
            }
        } else {
            System.exit(0);
        }
    }                                  

    public void searchTextFieldKeyReleased(java.awt.event.KeyEvent evt) {                                            
        if(Main.getConfig().getAutocompleteEnabled()) {
            if (evt.getKeyCode() >= KeyEvent.VK_A && evt.getKeyCode() <= KeyEvent.VK_Z) {
                SwingWorker<List<String>, Void> worker = new SwingWorker<List<String>, Void>() {

                    @Override
                    protected List<String> doInBackground() {
                        return Services.getSearchService().autocompleteByQuery(searchTextField.getText());
                    }

                    @Override
                    protected void done() {
                        try {
                            autocompleteList = (ArrayList<String>) get();
                            if(autocompleteList.size() > 0) {
                                String autocomplete = autocompleteList.get(0);
                                String autocompleteSub = autocomplete.substring(searchTextFieldOriginal.length());
                                String newText = searchTextFieldOriginal + autocompleteSub;
                                searchTextField.setText(newText);
                                searchTextField.select(autocompleteSub.length() + 1, newText.length() + 1);
                            } else {
                                searchTextFieldOriginal = searchTextField.getText();
                            }
                        } catch (InterruptedException ex) {
                            Logger.getLogger(GUI.class.getName()).log(Level.SEVERE, null, ex);
                        } catch (ExecutionException ex) {
                            Logger.getLogger(GUI.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                };
                worker.execute();
            }
        }
    }                                           

    public void searchTextFieldActionPerformed(java.awt.event.ActionEvent evt) {                                                
        /*((JSuggestField)searchTextField).fireEnterPressed();
        ((JSuggestField)searchTextField).hideSuggest();
        autocompleteList.clear();
        ((JSuggestField)searchTextField).setSuggestData(autocompleteList);*/
        // fire actionPerformed event at searchButton
        for (ActionListener a : searchButton.getActionListeners()) {
            a.actionPerformed(evt);
        }
    }                                               

    public void searchTextFieldComponentResized(java.awt.event.ComponentEvent evt) {                                                 
        ((JSuggestField)searchTextField).setSuggestWidth(searchTextField.getWidth());
    }                                                

    public void formMouseClicked(java.awt.event.MouseEvent evt) {                                  
        //((JSuggestField)searchTextField).hideSuggest();
    }                                 

    public void downloadTableKeyReleased(java.awt.event.KeyEvent evt) {                                          
        if(evt.getKeyCode() == KeyEvent.VK_DELETE) {
            int[] selectedRows = downloadTable.getSelectedRows();
            Object[] options = { Main.getLocaleString("REMOVE_FROM_LIST"), Main.getLocaleString("REMOVE_FROM_LIST_AND_DISK"), Main.getLocaleString("CANCEL") };
            int selectedValue = JOptionPane.showOptionDialog(this, String.format(Main.getLocaleString("ALERT_REMOVE_FROM_LIST_OR_DISK"), Integer.valueOf(selectedRows.length)), Main.getLocaleString("ALERT"), JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE, null, options, options[0]);
            if(selectedValue == 0) {
                removeFromList(false);
            } else if(selectedValue == 1) {
                removeFromList(true);
            } else {
                System.out.println(selectedValue);
            }
        }
    }                                         

    public void formKeyReleased(java.awt.event.KeyEvent evt) {                                 

    }                                

    public void playPauseButtonMousePressed(java.awt.event.MouseEvent evt) {
        if (Services.getPlayService().isPlaying()) {
            playPauseButton.setIcon(pauseIconActive);
        } else {
            playPauseButton.setIcon(playIconActive);
        }
    }

    public void playPauseButtonActionPerformed(java.awt.event.ActionEvent evt) {
        if (Services.getPlayService().isPaused()) {
            Services.getPlayService().resume();
        } else if (Services.getPlayService().isPlaying()) {
            Services.getPlayService().pause();
        } else {
            if (Services.getPlayService().getPlaylist().size() > 0) {
                Services.getPlayService().play();
            } else {
                playPauseButton.setIcon(playIcon);
            }
        }
    }
    
    public void nextButtonMousePressed(java.awt.event.MouseEvent evt) {
        nextButton.setIcon(nextIconActive);
    }

    public void nextButtonActionPerformed(java.awt.event.ActionEvent evt) {
        nextButton.setIcon(nextIcon);    
        if (Services.getPlayService().getCurrentSongIndex() < Services.getPlayService().getPlaylist().size() - 1) {
            Services.getPlayService().skipForward();
        } else {
            Services.getPlayService().clearPlaylist();
            resetPlayInfo();
        }
    }
    
    public void previousButtonMousePressed(java.awt.event.MouseEvent evt) {
        previousButton.setIcon(previousIconActive);
    }

    public void previousButtonActionPerformed(java.awt.event.ActionEvent evt) {
        previousButton.setIcon(previousIcon);
        Services.getPlayService().skipBackward();
    }

    public void trackSliderMouseDragged(java.awt.event.MouseEvent evt) {                                         
        /*
         * if(currentlyPlayingTrack != null) {
         * Services.getPlayService().setCurrentPosition(trackSlider.getValue());
         * System.out.println(trackSlider.getValue());
        }
         */
    }                                        

    public void closeButtonMouseClicked(java.awt.event.MouseEvent evt) {                                         
        for (WindowListener a : this.getWindowListeners()) {
            a.windowClosing(null);
        }
    }                                        

    public void maximizeButtonActionPerformed(java.awt.event.ActionEvent evt) {                                               
        if(getExtendedState() == JFrame.MAXIMIZED_BOTH) {
            setExtendedState(JFrame.NORMAL);
        } else {
            setExtendedState(JFrame.MAXIMIZED_BOTH);
        }
    }                                              

    public void minimizeButtonActionPerformed(java.awt.event.ActionEvent evt) {                                               
        setState(Frame.ICONIFIED);
    }                                              

    public void settingsButtonActionPerformed(java.awt.event.ActionEvent evt) {                                               
        Main.getSettings().setVisible(true);
    }                                              

    public void aboutButtonActionPerformed(java.awt.event.ActionEvent evt) {                                            
        Main.getAbout().setVisible(true);
    }                                           

    public void volumeSliderStateChanged(javax.swing.event.ChangeEvent evt) {                                          
         Services.getPlayService().setVolume(volumeSlider.getValue());
    }                                         

    public void removeFromListMenuItemActionPerformed(java.awt.event.ActionEvent evt) {                                                       
        removeFromList(false);
    }                                                      

    public void removeFromDiskMenuItemActionPerformed(java.awt.event.ActionEvent evt) {                                                       
        int[] selectedRows = downloadTable.getSelectedRows();
        if (JOptionPane.showConfirmDialog(null, String.format(Main.getLocaleString("ALERT_REMOVE_FILES_FROM_DISK"), Integer.toString(selectedRows.length)), Main.getLocaleString("ALERT"), JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == 0) {
            removeFromList(true);
        }
    }                                                      

    public void downloadTableMouseReleased(java.awt.event.MouseEvent evt) {                                            
        int r = downloadTable.rowAtPoint(evt.getPoint());
        if ((SwingUtilities.isRightMouseButton(evt) || evt.isControlDown()) && r >= 0 && r < downloadTable.getRowCount() && !ArrayUtils.contains(downloadTable.getSelectedRows(), r)) {
            downloadTable.setRowSelectionInterval(r, r);
        } else {
            //downloadTable.clearSelection();
        }

        int rowindex = downloadTable.getSelectedRow();
        if (rowindex < 0)
            return;
        if (evt.isPopupTrigger() && evt.getComponent() instanceof JTable) {
            downloadTablePopupMenu.show(evt.getComponent(), evt.getX(), evt.getY());
        }
    }                                           

    public void searchTableMouseReleased(java.awt.event.MouseEvent evt) {                                          
        int r = searchTable.rowAtPoint(evt.getPoint());
        if ((SwingUtilities.isRightMouseButton(evt) || evt.isControlDown()) && r >= 0 && r < searchTable.getRowCount() && !ArrayUtils.contains(searchTable.getSelectedRows(), r)) {
            searchTable.setRowSelectionInterval(r, r);
        } else {
            //searchTable.clearSelection();
        }

        int rowindex = searchTable.getSelectedRow();
        if (rowindex < 0)
            return;
        if (evt.isPopupTrigger() && evt.getComponent() instanceof JTable) {
            searchTablePopupMenu.show(evt.getComponent(), evt.getX(), evt.getY());
        }
    }                                         

    public void airPlayButtonMousePressed(java.awt.event.MouseEvent evt) {                                           
        airPlayPopupMenu.show(evt.getComponent(), evt.getX(), evt.getY());
    }                                          

    public void openDirectoryMenuItemActionPerformed(java.awt.event.ActionEvent evt) {                                                      
        int[] selectedRows = downloadTable.getSelectedRows();
        DownloadTableModel model = (DownloadTableModel) downloadTable.getModel();
        for (int i = 0; i < selectedRows.length; i++) {
            int selectedRow = downloadTable.convertRowIndexToModel(selectedRows[i] - i);
            Track track = model.getSongDownloads().get(selectedRow);
            try {
                // open dir
                Desktop.getDesktop().open(new File(track.getPath()).getParentFile());
            } catch (IOException ex) {
                Logger.getLogger(GUI.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        downloadTable.clearSelection();
    }                                                     

    public void openFileMenuItemActionPerformed(java.awt.event.ActionEvent evt) {                                                 
        int[] selectedRows = downloadTable.getSelectedRows();
        DownloadTableModel model = (DownloadTableModel) downloadTable.getModel();
        for (int i = 0; i < selectedRows.length; i++) {
            int selectedRow = downloadTable.convertRowIndexToModel(selectedRows[i] - i);
            Track track = model.getSongDownloads().get(selectedRow);
            try {
                // open file
                Desktop.getDesktop().open(new File(track.getPath()));
            } catch (IOException ex) {
                Logger.getLogger(GUI.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        downloadTable.clearSelection();
    }                                                

    public void facebookLabelMousePressed(java.awt.event.MouseEvent evt) {
        try {
            Desktop.getDesktop().browse(java.net.URI.create(((JLabel) evt.getSource()).getToolTipText()));
        } catch (IOException ex) {
            Logger.getLogger(About.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void donateLabelMousePressed(java.awt.event.MouseEvent evt) {        
        try {
            Desktop.getDesktop().browse(java.net.URI.create("http://groovesquid.com/#donate"));
        } catch (IOException ex) {
            Logger.getLogger(About.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void downloadMenuItemActionPerformed(java.awt.event.ActionEvent evt) {
        for (ActionListener a : downloadButton.getActionListeners()) {
            a.actionPerformed(evt);
        }
    }

    public void playMenuItemActionPerformed(java.awt.event.ActionEvent evt) {
        for (ActionListener a : playButton.getActionListeners()) {
            a.actionPerformed(evt);
        }
    }

    // variables
    protected javax.swing.JButton aboutButton;
    protected javax.swing.JButton airPlayButton;
    protected javax.swing.JPopupMenu airPlayPopupMenu;
    protected javax.swing.JLabel albumCoverLabel;
    protected javax.swing.JButton closeButton;
    protected javax.swing.JLabel currentDurationLabel;
    protected javax.swing.JLabel currentlyPlayingLabel;
    protected javax.swing.JLabel donateLabel;
    protected javax.swing.JButton downloadButton;
    protected javax.swing.JMenuItem downloadMenuItem;
    protected javax.swing.JPanel downloadPanel;
    protected javax.swing.JScrollPane downloadScrollPane;
    protected javax.swing.JTable downloadTable;
    protected javax.swing.JPopupMenu downloadTablePopupMenu;
    protected javax.swing.JLabel durationLabel;
    protected javax.swing.JLabel facebookLabel;
    protected javax.swing.JLabel jLabel2;
    protected javax.swing.JLabel jLabel3;
    protected javax.swing.JPanel jPanel1;
    protected javax.swing.JPanel jPanel2;
    protected javax.swing.JPanel jPanel3;
    protected javax.swing.JSplitPane splitPane;
    protected javax.swing.JButton maximizeButton;
    protected javax.swing.JButton minimizeButton;
    protected javax.swing.JMenuItem openDirectoryMenuItem;
    protected javax.swing.JMenuItem openFileMenuItem;
    protected javax.swing.JButton playButton;
    protected javax.swing.JMenuItem playMenuItem;
    protected javax.swing.JButton playPauseButton;
    protected javax.swing.JButton nextButton;
    protected javax.swing.JButton previousButton;
    protected javax.swing.JPanel playerPanel;
    protected javax.swing.JButton removeFromDiskButton;
    protected javax.swing.JMenuItem removeFromDiskMenuItem;
    protected javax.swing.JButton removeFromListButton;
    protected javax.swing.JMenuItem removeFromListMenuItem;
    protected javax.swing.JButton retryFailedDownloadsButton;
    protected javax.swing.JButton searchButton;
    protected javax.swing.JPanel searchPanel;
    protected javax.swing.JScrollPane searchScrollPane;
    protected javax.swing.JTable searchTable;
    protected javax.swing.JPopupMenu searchTablePopupMenu;
    protected javax.swing.JTextField searchTextField;
    protected javax.swing.JComboBox searchTypeComboBox;
    protected javax.swing.JComboBox selectComboBox;
    protected javax.swing.JButton settingsButton;
    protected javax.swing.JLabel titleBarLabel;
    protected javax.swing.JPanel titleBarPanel;
    protected javax.swing.JSlider trackSlider;
    protected javax.swing.JLabel twitterLabel;
    protected javax.swing.JLabel volumeOffLabel;
    protected javax.swing.JLabel volumeOnLabel;
    protected javax.swing.JSlider volumeSlider;
    // End of variables declaration

    private void removeFromList(boolean andDisk) {
        int[] selectedRows = downloadTable.getSelectedRows();
        DownloadTableModel model = (DownloadTableModel) downloadTable.getModel();
        for (int i = 0; i < selectedRows.length; i++) {
            int selectedRow = downloadTable.convertRowIndexToModel(selectedRows[i] - i);
            Track track = model.getSongDownloads().get(selectedRow);
            if(andDisk) {
                Services.getDownloadService().cancelDownload(track, true, true);
            } else {
                Services.getDownloadService().cancelDownload(track, false);
            }
            model.removeRow(selectedRow);
        }
        downloadTable.getSelectionModel().clearSelection();
    }
    
    
    public void play(List<Song> songs) {
        final Song song = songs.get(0);
        Track track = Services.getPlayService().getCurrentTrack();
        
        if(track != null && track.getSong().equals(song) && Services.getPlayService().isPaused()) {
            Services.getPlayService().resume();
        } else {
            if(Services.getPlayService().isPlaying()) {
                Object[] options = { Main.getLocaleString("PLAY_NOW"), Main.getLocaleString("ADD_TO_QUEUE"), Main.getLocaleString("CANCEL") };
                int selectedValue = JOptionPane.showOptionDialog(this, Main.getLocaleString("ALERT_PLAY_NOW_OR_QUEUE"), Main.getLocaleString("PLAY"), JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE, null, options, options[0]);
                if(selectedValue == 0) {
                    Services.getPlayService().add(songs, PlayService.AddMode.NOW);
                } else if(selectedValue == 1) {
                    Services.getPlayService().add(songs, PlayService.AddMode.NEXT);
                }
            } else {
                Services.getPlayService().add(songs, PlayService.AddMode.NOW);
            }
        }
    }
    
    public void resetPlayInfo() {
        currentlyPlayingLabel.setText("");
        playPauseButton.setIcon(playIcon);
        trackSlider.setValue(0);
        trackSlider.setEnabled(false);
        currentlyPlayingLabel.setText("");
        currentDurationLabel.setText("00:00");
        durationLabel.setText("00:00");
        albumCoverLabel.setIcon(null);
    }
    
    public JTextField getSearchTextField() {
        return searchTextField;
    }
    
    public JButton getSearchButton() {
        return searchButton;
    }
    
    public JTable getSearchTable() {
        return searchTable;
    }
    
    public JTable getDownloadTable() {
        return downloadTable;
    }

    public JComboBox getSearchTypeComboBox() {
        return searchTypeComboBox;
    }
    
    public JPopupMenu getAirPlayPopupMenu() {
        return airPlayPopupMenu;
    }

    public void initDone() {
        searchTypeComboBox.setEnabled(true);
        searchTextField.setText("");
        searchTextField.setEnabled(true);
        searchTextField.requestFocus();
        searchButton.setEnabled(true);
    }

    public void showError(String message) {
        JOptionPane.showMessageDialog(this, message, Main.getLocaleString("ERROR"), JOptionPane.ERROR_MESSAGE);
    }
    
}
