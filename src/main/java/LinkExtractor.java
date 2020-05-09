import lombok.Data;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
public class LinkExtractor {

    private List<String> excludePatterns = Arrays.asList(".*\\/vor-ort\\/.*", ".*\\/datei\\/.*", ".*con\\.arbeitsagentur\\.de.*", "^#.*", "^javascript.*", "^tel:.*");
    private List<String> includeHosts = Arrays.asList("https://arbeitsagentur.de", "https://www.arbeitsagentur.de");

    public Set<String> extractLinks(byte[] payload) {
        Set<String> extractedLinks = new HashSet<>();
        Document document = Jsoup.parse(new String(payload));
        Elements links = document.select("a");

        for (Element link : links) {
            String linkStr = link.attr("href").replaceAll("\r","").replaceAll("\n","").replaceAll("\t","");
            if(!checkExcludePattern(linkStr)) {
                continue;
            }

            linkStr = getValidURLPath(linkStr);

            if(!linkStr.isEmpty()) {
                extractedLinks.add(linkStr);
            }
        }

        return extractedLinks;
    }

    private String getValidURLPath(String linkStr)  {
        try {
            URL linkURL = new URL(linkStr);
            boolean isValid = false;
            for (String includeHost : includeHosts) {
                if(linkURL.getHost().contains(includeHost)) {
                    isValid = true;
                }
            }

            return isValid ? linkURL.getPath() : "";
        } catch (MalformedURLException e) {
            return linkStr.startsWith("/") ? linkStr : "/" + linkStr;
        }

    }

    private boolean checkExcludePattern(String link) {
        if(link.isEmpty()) {
            return false;
        }
        for (String excludePattern : this.excludePatterns) {

            if(link.matches(excludePattern)) {
                return false;
            }
        }

        return true;
    }
}
