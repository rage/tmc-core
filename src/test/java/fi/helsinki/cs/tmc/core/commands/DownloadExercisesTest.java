package fi.helsinki.cs.tmc.core.commands;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyDouble;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import fi.helsinki.cs.tmc.core.CoreTestSettings;
import fi.helsinki.cs.tmc.core.TmcCore;
import fi.helsinki.cs.tmc.core.cache.ExerciseChecksumCache;
import fi.helsinki.cs.tmc.core.communication.ExerciseDownloader;
import fi.helsinki.cs.tmc.core.communication.TmcApi;
import fi.helsinki.cs.tmc.core.communication.UrlHelper;
import fi.helsinki.cs.tmc.core.communication.authorization.Authorization;
import fi.helsinki.cs.tmc.core.domain.Course;
import fi.helsinki.cs.tmc.core.domain.Exercise;
import fi.helsinki.cs.tmc.core.domain.ProgressObserver;
import fi.helsinki.cs.tmc.core.exceptions.TmcCoreException;
import fi.helsinki.cs.tmc.core.testhelpers.ExampleJson;
import fi.helsinki.cs.tmc.core.testhelpers.builders.ExerciseBuilder;

import com.github.tomakehurst.wiremock.junit.WireMockRule;

import com.google.common.base.Optional;
import com.google.common.util.concurrent.ListenableFuture;

import org.apache.commons.io.FileUtils;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import org.mockito.Mockito;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DownloadExercisesTest {

    private ExerciseChecksumCache cache;
    private CoreTestSettings settings;
    private TmcApi tmcApi;
    private TmcCore core;

    @Rule
    public WireMockRule wireMockServer = new WireMockRule(0);
    private String serverAddress = "http://127.0.0.1:";

    private CoreTestSettings createSettingsAndWiremock() throws URISyntaxException {
        CoreTestSettings settings1 = new CoreTestSettings();
        settings1.setServerAddress(serverAddress);
        settings1.setUsername("test");
        settings1.setPassword("1234");
        wiremock(settings1.getUsername(), settings1.getPassword(), "3", serverAddress);
        return settings1;
    }

    private void wiremock(String username, String password, String courseId, String serverAddress)
            throws URISyntaxException {
        String encodedCredentials = "Basic " + Authorization.encode(username + ":" + password);
        wireMockServer.stubFor(
                get(urlPathEqualTo("/user"))
                .withHeader("Authorization", equalTo(encodedCredentials))
                .willReturn(aResponse().withStatus(200)));

        wireMockServer.stubFor(
                get(urlPathEqualTo(new UrlHelper(settings).coursesExtension))
                .willReturn(
                        aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "text/json")
                        .withBody(
                                ExampleJson.allCoursesExample.replaceAll(
                                        "https://example.com/staging",
                                        serverAddress))));

        wireMockServer.stubFor(
                get(urlPathEqualTo("/courses/" + courseId + ".json"))
                .willReturn(
                        aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "text/json")
                        .withBody(
                                ExampleJson.courseExample
                                .replaceAll(
                                        "https://example.com/staging",
                                        serverAddress)
                                .replaceFirst("3", courseId))));

        wireMockServer.stubFor(
                get(urlMatching("/exercises/[0-9]+.zip"))
                .willReturn(
                        aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "text/json")
                        .withBodyFile("test.zip")));
    }

    @Before
    public void setup() throws IOException {
        serverAddress += wireMockServer.port();
        settings = new CoreTestSettings();
        settings.setUsername("Bossman");
        settings.setPassword("Samu");
        cache = mock(ExerciseChecksumCache.class);
        tmcApi = mock(TmcApi.class);
        this.core = new TmcCore(settings);
    }

    @Test(expected = TmcCoreException.class)
    public void settingsWithoutCurrentCourse() throws TmcCoreException, IOException {
        new DownloadExercises(settings, new ArrayList<Exercise>(), null, null).call();
    }

    @Test(expected = TmcCoreException.class)
    public void settingsWithoutCredentials()
            throws TmcCoreException, IOException, URISyntaxException {
        CoreTestSettings localSettings = new CoreTestSettings();
        localSettings.setCurrentCourse(
                new TmcApi(settings).getCourseFromString(ExampleJson.courseExample));
        new DownloadExercises(localSettings, new ArrayList<Exercise>(), null, null).call();
    }

    @Test
    public void writesChecksumsToCacheIfCacheFileIsGiven()
            throws IOException, TmcCoreException, URISyntaxException {
        ExerciseDownloader downloader = mock(ExerciseDownloader.class);
        Mockito.when(downloader.createCourseFolder(any(Path.class), anyString())).thenReturn(Paths.get("path"));
        Mockito.when(downloader.handleSingleExercise(any(Exercise.class), any(Path.class)))
                .thenReturn(true);

        Course course = new Course();
        course.setName("test-course");
        course.setExercises(
                new ExerciseBuilder()
                .withExercise("kissa", 2, "eujwuc")
                .withExercise("asdf", 793, "alnwnec")
                .withExercise("ankka", 88, "abcdefg")
                .build());

        tmcApi = mock(TmcApi.class);

        when(tmcApi.getCourse(anyInt())).thenReturn(Optional.of(course));

        new DownloadExercises(settings, Paths.get(""), 8, cache, null, downloader, tmcApi).call();
        verify(cache, times(1)).write(course.getExercises());
    }

    @Test
    public void downloadAllExercises() throws Exception {
        CoreTestSettings settings1 = createSettingsAndWiremock();
        core = new TmcCore(settings1);
        Path folder = Paths.get(System.getProperty("user.dir") + "/testResources/");
        ListenableFuture<List<Exercise>> download = core.downloadExercises(folder, 3, null);

        List<Exercise> exercises = download.get();
        Path exercisePath = folder.resolve("test-course/viikko1/Viikko1_001.Nimi");

        assertEquals(153, exercises.size());
        assertTrue(Files.exists(exercisePath));

        FileUtils.deleteDirectory(exercisePath.toFile());
        assertFalse(Files.exists(exercisePath));
    }

    @Test
    public void testDownloadingWithProgress() throws Exception {
        CoreTestSettings settings1 = createSettingsAndWiremock();
        core = new TmcCore(settings1);
        ProgressObserver observerMock = mock(ProgressObserver.class);
        Path folder = Paths.get(System.getProperty("user.dir") + "/testResources/");
        ListenableFuture<List<Exercise>> download
                = core.downloadExercises(folder, 3, observerMock);
        List<Exercise> exercises = download.get();
        Path exercisePath = folder.resolve("test-course/viikko1/Viikko1_001.Nimi");
        assertEquals(153, exercises.size());
        assertTrue(Files.exists(exercisePath));
        FileUtils.deleteDirectory(exercisePath.toFile());
        assertFalse(Files.exists(exercisePath));

        verify(observerMock, times(153)).progress(anyDouble(), anyString());
    }

    @After
    public void tearDown() {
        try {
            FileUtils.deleteDirectory(Paths.get(System.getProperty("user.dir")
                    + "/testResources/test-course").toFile());
        }
        catch (IOException ex) {
            Logger.getLogger(DownloadExercisesTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
