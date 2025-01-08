package com.driver;

import java.util.*;

import org.springframework.stereotype.Repository;

@Repository
public class SpotifyRepository {
    public HashMap<Artist, List<Album>> artistAlbumMap;
    public HashMap<Album, List<Song>> albumSongMap;
    public HashMap<Playlist, List<Song>> playlistSongMap;
    public HashMap<Playlist, List<User>> playlistListenerMap;
    public HashMap<User, Playlist> creatorPlaylistMap;
    public HashMap<User, List<Playlist>> userPlaylistMap;
    public HashMap<Song, List<User>> songLikeMap;

    public List<User> users;
    public List<Song> songs;
    public List<Playlist> playlists;
    public List<Album> albums;
    public List<Artist> artists;

    public SpotifyRepository(){
        //To avoid hitting apis multiple times, initialize all the hashmaps here with some dummy data
        artistAlbumMap = new HashMap<>();
        albumSongMap = new HashMap<>();
        playlistSongMap = new HashMap<>();
        playlistListenerMap = new HashMap<>();
        creatorPlaylistMap = new HashMap<>();
        userPlaylistMap = new HashMap<>();
        songLikeMap = new HashMap<>();

        users = new ArrayList<>();
        songs = new ArrayList<>();
        playlists = new ArrayList<>();
        albums = new ArrayList<>();
        artists = new ArrayList<>();
    }

    public User createUser(String name, String mobile) {
        if (name == null || name.isEmpty() || mobile == null || mobile.isEmpty()) {
            throw new IllegalArgumentException("Name and Mobile cannot be empty");
        }
        User user = new User(name, mobile);
        users.add(user);
        return user;
    }

    public Artist createArtist(String name) {
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("Name cannot be empty");
        }
        Artist artist = new Artist(name);
        artists.add(artist);
        return artist;
    }

    public Album createAlbum(String title, String artistName) {
        if (title == null || title.isEmpty()) {
            throw new IllegalArgumentException("Title cannot be empty");
        }
        if (artistName == null || artistName.isEmpty())
        {
            throw new IllegalArgumentException("Artist name cannot be empty");
        }

        Optional<Album> existingAlbum = albums.stream()
                .filter(album -> album.getTitle().equalsIgnoreCase(title))
                .findFirst();

        if (existingAlbum.isPresent()) {
            throw new IllegalArgumentException("Album already exists");
        }

        Artist artist = artists.stream()
                .filter(a -> a.getName().equalsIgnoreCase(artistName))
                .findFirst()
                .orElseGet(() -> {
                    Artist newArtist = new Artist(artistName);
                    artists.add(newArtist);
                    return newArtist;
                });

        Album album = new Album(title);
        albums.add(album);
        artistAlbumMap.computeIfAbsent(artist, k -> new ArrayList<>()).add(album);

        return album;
    }

    public Song createSong(String title, String albumName, int length) throws Exception{
        if (title == null || title.isEmpty() || albumName == null || albumName.isEmpty() || length <= 0) {
            throw new IllegalArgumentException("Invalid song details");
        }

        Album album = albums.stream()
                .filter(a -> a.getTitle().equalsIgnoreCase(albumName))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Album does not exist"));

        Song song = new Song(title, length);
        songs.add(song);
        albumSongMap.computeIfAbsent(album, k -> new ArrayList<>()).add(song);

        return song;
    }

    public Playlist createPlaylistOnLength(String mobile, String title, int length) throws Exception {
        User user = users.stream()
                .filter(u -> u.getMobile().equals(mobile))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("User does not exist"));

        Playlist playlist = new Playlist(title);
        playlists.add(playlist);

        List<Song> matchedSongs = songs.stream()
                .filter(song -> song.getLength() == length)
                .toList();

        playlistSongMap.put(playlist, matchedSongs);
        playlistListenerMap.put(playlist, new ArrayList<>(List.of(user)));

        return playlist;

    }

    public Playlist createPlaylistOnName(String mobile, String title, List<String> songTitles) throws Exception {
        User user = users.stream()
                .filter(u -> u.getMobile().equals(mobile))
                .findFirst()
                .orElseThrow(() -> new Exception("User does not exist"));

        Playlist playlist = new Playlist(title);
        playlists.add(playlist);

        List<Song> matchedSongs = songs.stream()
                .filter(song -> songTitles.contains(song.getTitle()))
                .toList();

        playlistSongMap.put(playlist, matchedSongs);
        playlistListenerMap.put(playlist, new ArrayList<>(List.of(user)));

        return playlist;
    }

    public Playlist findPlaylist(String mobile, String playlistTitle) throws Exception {
        User user = users.stream()
                .filter(u -> u.getMobile().equals(mobile))
                .findFirst()
                .orElseThrow(() -> new Exception("User does not exist"));

        Playlist playlist = playlists.stream()
                .filter(p -> p.getTitle().equalsIgnoreCase(playlistTitle))
                .findFirst()
                .orElseThrow(() -> new Exception("Playlist does not exist"));

        List<User> listeners = playlistListenerMap.getOrDefault(playlist, new ArrayList<>());
        if (!listeners.contains(user)) {
            listeners.add(user);
            playlistListenerMap.put(playlist, listeners);
        }

        return playlist;
    }

    public Song likeSong(String mobile, String songTitle) throws Exception {
        User user = users.stream()
                .filter(u -> u.getMobile().equals(mobile))
                .findFirst()
                .orElseThrow(() -> new Exception("User does not exist"));

        Song song = songs.stream()
                .filter(s -> s.getTitle().equalsIgnoreCase(songTitle))
                .findFirst()
                .orElseThrow(() -> new Exception("Song does not exist"));

        List<User> likedUsers = songLikeMap.getOrDefault(song, new ArrayList<>());
        if (!likedUsers.contains(user)) {
            likedUsers.add(user);
            song.setLikes(song.getLikes() + 1);
            songLikeMap.put(song, likedUsers);
        }

        return song;
    }

    public String mostPopularArtist() {
        return artists.stream()
                .max(Comparator.comparingInt(Artist::getLikes))
                .map(Artist::getName)
                .orElse("No artist found");
    }

    public String mostPopularSong() {
        return songs.stream()
                .max(Comparator.comparingInt(Song::getLikes))
                .map(Song::getTitle)
                .orElse("No song found");
    }
}
