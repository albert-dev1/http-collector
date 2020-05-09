import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import crawlercommons.sitemaps.SiteMap;
import crawlercommons.sitemaps.SiteMapParser;
import crawlercommons.sitemaps.SiteMapURL;
import crawlercommons.sitemaps.UnknownFormatException;
import lombok.Builder;
import lombok.Data;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

@Builder
@Data
public class Crawler {
    private Committer committer;
    private LinkExtractor linkextractor;
    private MediaExtractor mediaExtractor;
    private String mainDomain;
    private String sitemapUrl;
    private List<WebElement> webElementList;

    public void crawl() throws IOException, UnknownFormatException {
        webElementList = new ArrayList<>();
        SiteMap parsedSitemap = null;
        SiteMapParser siteMapParser = new SiteMapParser();

        if(sitemapUrl == null) {
            parsedSitemap = (SiteMap) siteMapParser.parseSiteMap(IOUtils.toByteArray(Main.class.getResource("sitemap.xml")), new URL(mainDomain + "/sitemap.xml"));
        } else {
            parsedSitemap = (SiteMap) siteMapParser.parseSiteMap(new URL(sitemapUrl));
        }

        processSitemap(parsedSitemap);

        Set<WebElement> consolidatedWebElements = processDependencies();

        String host = new URL(this.mainDomain).getHost();
        RedirectMapUtil.createFilesIfNotExists();
        consolidatedWebElements.stream()
                .filter(webElement -> webElement.getUrl().getHost().equals(host))
                .map(WebElement::getRedirect)
                .filter(redirect -> redirect!=null)
                .forEach(RedirectMapUtil::writeRedirect);

        write(consolidatedWebElements);
    }

    private Set<WebElement> processDependencies() throws MalformedURLException {
        Set<WebElement> webElementSet = new HashSet<>(webElementList);

        for (WebElement webElement : webElementList) {
            for (String reference : webElement.getReferences()) {
                webElementSet.add(WebElement.builder()
                        .url(new URL(reference))
                        .build());
            }
            for (String media : webElement.getMedia()) {
                webElementSet.add(WebElement.builder()
                        .url(new URL(media))
                        .build());
            }
        }
        List<List<WebElement>> urlLists = ListUtils.partition(new ArrayList<>(webElementSet), 10);
        List<Callable<String>> tasks = new ArrayList<>();

        for (List<WebElement> urlList : urlLists) {
            Callable<String> callableTask = () -> {
                urlList.forEach(this::commitAndClearPayload);
                return "Task's execution";
            };
            tasks.add(callableTask);
        }

        CrawlerExecutor crawlerExecutor = new CrawlerExecutor(tasks);
        try {
            crawlerExecutor.runBlocking();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }finally {
            crawlerExecutor.shutdown();
        }

        return webElementSet;
    }

    private void processSitemap(SiteMap parsedSitemap) {
        List<List<SiteMapURL>> urlLists = ListUtils.partition(new ArrayList<>(parsedSitemap.getSiteMapUrls()), 10);
        List<Callable<String>> tasks = new ArrayList<>();

        for (List<SiteMapURL> urlList : urlLists) {
            Callable<String> callableTask = () -> {
                urlList.forEach(siteMapURL -> {
                    WebElement webElement = WebElement.builder().url(siteMapURL.getUrl()).build();
                    webElementList.add(webElement);
                    webElement.setReferences(extractAndUpdateLinks(webElement));
                    webElement.setMedia(extractAndUpdateMedia(webElement));
                    commitAndClearPayload(webElement);
                });
                return "Task's execution";
            };
            tasks.add(callableTask);
        }

        CrawlerExecutor crawlerExecutor = new CrawlerExecutor(tasks);
        try {
            crawlerExecutor.runBlocking();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }finally {
            crawlerExecutor.shutdown();
        }
    }

    private void write(Set<WebElement> consolidatedWebElements) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        //Converting the Object to JSONString
        String jsonString = mapper.writeValueAsString(consolidatedWebElements);

        File file = new File("reports/report.json");
        file.getParentFile().mkdirs();
        file.createNewFile();
        try(FileOutputStream os = new FileOutputStream(file)) {
            os.write(jsonString.getBytes(), 0, jsonString.length());
        } catch (IOException e) {
            e.printStackTrace();
        }
        BeanToCSV.write(new ArrayList<>(consolidatedWebElements));
    }

    Set<String> extractAndUpdateMedia(WebElement webElement) {
        if(mediaExtractor == null) {
            return Collections.EMPTY_SET;
        }

        if (!updatePayload(webElement)) {
            return Collections.EMPTY_SET;
        }

        return mediaExtractor.extractMedia(webElement.getPayload()).stream().map(this::addHost).collect(Collectors.toSet());

    }

    private String addHost(String url) {
        return url.startsWith(mainDomain) ? url : mainDomain + url;
    }

    Set<String> extractAndUpdateLinks(WebElement webElement) {
        if(linkextractor == null) {
            return Collections.EMPTY_SET;
        }

        if (!updatePayload(webElement)) {
            return Collections.EMPTY_SET;
        }

        return linkextractor.extractLinks(webElement.getPayload()).stream().map(this::addHost).collect(Collectors.toSet());

    }

    void commitAndClearPayload(WebElement webElement) {

        if(committer == null || webElement.getUrl() == null || webElement.isCommited()) {
            return;
        }

        if (!updatePayload(webElement)) {
            return;
        }

        committer.commit(webElement.getUrl(), webElement.getPayload(), webElement.getFileExtension());
        webElement.setCommited(true);
        webElement.setPayload(null);

    }

    boolean updatePayload(WebElement webElement) {
        if (webElement.getPayload() == null) {
            try {
                MetaUtil metaUtil = new MetaUtil(webElement.getUrl());
                webElement.setMediaType(metaUtil.getMediaType());
                webElement.setRedirect(metaUtil.getRedirect());
                webElement.setPayload(metaUtil.getPayload());
                if (webElement.getPayload() == null) {
                    return false;
                }
            } catch (IOException e) {
                System.err.println(webElement.getUrl() + "not found");
                return false;
            }
        }
        return true;
    }
}

