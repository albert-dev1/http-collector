import java.net.URL;

public interface Committer {
    void commit(URL url, byte[] payload, String mediaType);
}
