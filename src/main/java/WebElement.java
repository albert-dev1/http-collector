import lombok.Builder;
import lombok.Data;
import org.apache.tika.mime.MimeType;
import org.apache.tika.mime.MimeTypeException;
import org.apache.tika.mime.MimeTypes;

import java.net.URL;
import java.util.Set;

@Data
@Builder
public class WebElement {

    private URL url;
    private Set<String> references;
    private Set<String> referenced;
    private String inSitemap;
    private byte[] payload;
    private String mediaType;
    private boolean commited;
    private Set<String> media;
    private Redirect redirect;
    private String fileExtension;


    public boolean equals(Object o) {
        return (o instanceof WebElement) && (((WebElement) o).getUrl()).equals(this.url);
    }

    public int hashCode() {
        return url.hashCode();
    }

    public static class WebElementBuilder {
        public WebElementBuilder mediaType(String mediaType) {
            this.mediaType = mediaType;
            this.fileExtension = getExtensionFromMediaType(mediaType);
            return this;
        }
    }
    public void setMediaType(String mediaType) {
        this.mediaType = mediaType;
        this.fileExtension = getExtensionFromMediaType(mediaType);
    }

    public static String getExtensionFromMediaType(String mediaType) {
        mediaType = mediaType.contains(";") ? mediaType.split(";")[0] : mediaType;
        MimeTypes allTypes = MimeTypes.getDefaultMimeTypes();
        try {
            MimeType type = allTypes.forName(mediaType);
            if("image/jpg".equals(type.getName())){
                type = allTypes.forName("image/jpeg");
            }
            return type.getExtension().isEmpty() ? ".html" : type.getExtension();
        } catch (MimeTypeException e) {
            return ".html";
        }
    }
}
