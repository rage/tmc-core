package fi.helsinki.cs.tmc.core.spyware;

import fi.helsinki.cs.tmc.core.communication.HttpResult;
import fi.helsinki.cs.tmc.core.communication.UrlCommunicator;
import fi.helsinki.cs.tmc.core.configuration.TmcSettings;
import fi.helsinki.cs.tmc.core.domain.Course;
import fi.helsinki.cs.tmc.core.exceptions.TmcCoreException;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DiffSender {
    private TmcSettings settings;
    private UrlCommunicator urlCommunicator;

    public DiffSender(TmcSettings settings) {
        this.settings = settings;
        this.urlCommunicator = new UrlCommunicator(settings);
    }

    public DiffSender(UrlCommunicator urlCommunicator, TmcSettings settings) {
        this.settings = settings;
        this.urlCommunicator = urlCommunicator;
    }

    /**
     * Sends given byte-data to all URLs specified by course.
     *
     * @param diffs as byte-array
     * @param currentCourse tell all spywareUrls
     * @return all results
     */
    public List<HttpResult> sendToSpyware(byte[] diffs, Course currentCourse)
            throws TmcCoreException {
        List<URI> spywareUrls = currentCourse.getSpywareUrls();
        List<HttpResult> results = new ArrayList<>();
        for (URI url : spywareUrls) {
            results.add(sendToUrl(diffs, url));
        }
        return results;
    }

    /**
     * Sends diff-data to url.
     *
     * @param diffs as
     * @param url of destination
     * @return HttpResult from UrlCommunicator
     */
    public HttpResult sendToUrl(byte[] diffs, URI url) {
        try {
            return urlCommunicator.makePostWithByteArray(
                    url, diffs, createHeaders(), new HashMap<String, String>());
        } catch (IOException ex) {
            return new HttpResult(ex.getMessage(), 500, false);
        }
    }

    private Map<String, String> createHeaders() {
        Map<String, String> headers = new HashMap<>();
        headers.put("X-Tmc-Version", "1");
        headers.put("X-Tmc-Username", settings.getUsername());
        headers.put("X-Tmc-Password", settings.getPassword());
        return headers;
    }
}
