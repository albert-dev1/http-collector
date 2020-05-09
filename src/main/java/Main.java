import crawlercommons.sitemaps.UnknownFormatException;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException, UnknownFormatException {

        final String HOST = "https://www.sitemaps.org";
        String sitemapUrl = "https://www.sitemaps.org/sitemap.xml";


        Crawler crawler = Crawler.builder()
                .committer(new FilesystemCommitter())
                .linkextractor(new LinkExtractor())
                .mediaExtractor(new MediaExtractor())
                .mainDomain(HOST)
                .sitemapUrl(sitemapUrl)
                .build();

        crawler.crawl();
    }

}
