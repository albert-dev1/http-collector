import lombok.Data;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Data
public class MetaUtil {
    private HttpURLConnection con;
    private boolean isRedirect = false;
    private URL url;
    private Redirect redirect;
    private String mediaType;

    public MetaUtil(URL url) {

        this.url = url;
        try {
            con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            con.setInstanceFollowRedirects(false);
            int status = con.getResponseCode();
            if (status == HttpURLConnection.HTTP_MOVED_TEMP
                    || status == HttpURLConnection.HTTP_MOVED_PERM) {
                this.redirect = new Redirect(url.getPath(), urlEncode(con.getHeaderField("Location")), status);
                this.isRedirect = true;
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String urlEncode(String location) {
        location = location.startsWith("/") ? location.substring(1) : location;
        return URLEncoder.encode(location, StandardCharsets.UTF_8);
    }

    public byte[] getPayload() throws IOException {
        return isRedirect ? null :IOUtils.toByteArray(this.url);
    }


    public String getMediaType() {
        this.mediaType = isRedirect ? "" : con.getHeaderField("Content-Type");
        return this.mediaType;
    }

    public Redirect getRedirect() {
        return this.redirect;
    }


}
