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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MediaExtractor {

    private List<String> excludePatterns = Arrays.asList(".*\\/vor-ort\\/.*", ".*\\/datei\\/.*", ".*con\\.arbeitsagentur\\.de.*", "^#.*", "^tel:.*", ".*e\\.video-cdn\\.net.*", ".*youtube-nocookie\\.com.*", ".*berufe\\.tv.*");
    private List<String> includeHosts = Arrays.asList("https://arbeitsagentur.de", "https://www.arbeitsagentur.de");

    public Set<String> extractMedia(byte[] payload) {
        Set<String> extractedLinks = new HashSet<>();
        String payloadStr = new String(payload);
        Pattern pattern = Pattern.compile("(media/./\\d{13})");
        Matcher matcher = pattern.matcher(payloadStr);
        while (matcher.find()) {
            String imgUrl =  matcher.group(1).startsWith("/") ? matcher.group(1) : "/" + matcher.group(1);
            extractedLinks.add(imgUrl);
        }

        Document doc = Jsoup.parse(payloadStr);
        Elements media = doc.select("[src]");
        Elements imports = doc.select("link[href]").not("[hreflang]");

        for (Element mediaElement : media) {
            String linkStr = mediaElement.attr("src").replaceAll("\r","").replaceAll("\n","").replaceAll("\t","");
            if(!checkExcludePattern(linkStr)) {
                continue;
            }

            linkStr = getValidURLPath(linkStr);

            if(!linkStr.isEmpty()) {
                extractedLinks.add(linkStr);
            }
        }

        for (Element importElement : imports) {
            String linkStr = importElement.attr("href").replaceAll("\r","").replaceAll("\n","").replaceAll("\t","");
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
