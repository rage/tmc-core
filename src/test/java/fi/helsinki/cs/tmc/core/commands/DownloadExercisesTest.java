package fi.helsinki.cs.tmc.core.commands;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyDouble;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

import fi.helsinki.cs.tmc.core.CoreTestSettings;
import fi.helsinki.cs.tmc.core.TmcCore;
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
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.apache.commons.io.FileUtils;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import org.mockito.Mockito;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DownloadExercisesTest {

    private File cache;
    private CoreTestSettings settings;
    private TmcApi tmcApi;
    private TmcCore core;

    @Rule public WireMockRule wireMockServer = new WireMockRule();

    @Before
    public void setup() throws IOException {
        settings = new CoreTestSettings();
        settings.setUsername("Bossman");
        settings.setPassword("Samu");
        cache = Paths.get("src", "test", "resources", "downloadtest.cache").toFile();
        cache.createNewFile();
        tmcApi = Mockito.mock(TmcApi.class);
        this.core = new TmcCore(settings);
    }

    @After
    public void tearDown() {
        cache.delete();
    }

    @Test(expected = TmcCoreException.class)
    public void settingsWithoutCurrentCourse() throws TmcCoreException, IOException, URISyntaxException {
        new DownloadExercises(new ArrayList<Exercise>(), settings).call();
    }

    @Test(expected = TmcCoreException.class)
    public void settingsWithoutCredentials() throws TmcCoreException, IOException, URISyntaxException {
        CoreTestSettings localSettings = new CoreTestSettings();
        localSettings.setCurrentCourse(
                new TmcApi(settings).getCourseFromString(ExampleJson.courseExample));
        new DownloadExercises(new ArrayList<Exercise>(), localSettings).call();
    }

    @Test
    public void writesChecksumsToFileIfCacheFileIsGiven() throws IOException, TmcCoreException, URISyntaxException {
        ExerciseDownloader downloader = Mockito.mock(ExerciseDownloader.class);
        Mockito.when(downloader.createCourseFolder(anyString(), anyString())).thenReturn("");
        Mockito.when(downloader.handleSingleExercise(any(Exercise.class), anyString()))
                .thenReturn(true);

        Course course = new Course();
        course.setName("test-course");
        course.setExercises(
                new ExerciseBuilder()
                        .withExercise("kissa", 2, "eujwuc")
                        .withExercise("asdf", 793, "alnwnec")
                        .withExercise("ankka", 88, "abcdefg")
                        .build());

        tmcApi = Mockito.mock(TmcApi.class);

        when(tmcApi.getCourse(anyInt())).thenReturn(Optional.of(course));

        DownloadExercises dl = new DownloadExercises(downloader, "", 8, cache, settings, tmcApi);
        dl.call();
        String json = FileUtils.readFileToString(cache);
        Gson gson = new Gson();
        Map<String, Map<String, String>> checksums;
        Type typeOfHashMap = new TypeToken<Map<String, Map<String, String>>>() {}.getType();
        checksums = gson.fromJson(json, typeOfHashMap);

        assertNotNull(checksums);
        assertTrue(checksums.containsKey("test-course"));
        assertTrue(checksums.get("test-course").containsKey("kissa"));
        assertTrue(checksums.get("test-course").containsKey("asdf"));
        assertEquals("eujwuc", checksums.get("test-course").get("kissa"));
        assertEquals("alnwnec", checksums.get("test-course").get("asdf"));
        assertEquals("abcdefg", checksums.get("test-course").get("ankka"));
    }

    @Test
    public void overwritesToCacheFileIfCacheFileHasBadContents()
            throws IOException, TmcCoreException, URISyntaxException {
        new FileWriter(cache).write(" asdfjljlkasdf ");

        ExerciseDownloader downloader = Mockito.mock(ExerciseDownloader.class);
        Mockito.when(downloader.createCourseFolder(anyString(), anyString())).thenReturn("");
        Mockito.when(downloader.handleSingleExercise(any(Exercise.class), anyString()))
                .thenReturn(true);

        Course course = new Course();
        course.setName("test-course");
        course.setExercises(
                new ExerciseBuilder()
                        .withExercise("kissa", 2, "eujwuc")
                        .withExercise("asdf", 793, "alnwnec")
                        .withExercise("ankka", 88, "abcdefg")
                        .build());

        tmcApi = Mockito.mock(TmcApi.class);

        Mockito.when(tmcApi.getCourse(anyInt())).thenReturn(Optional.of(course));

        DownloadExercises dl = new DownloadExercises(downloader, "", 8, cache, settings, tmcApi);
        dl.call();
        String json = FileUtils.readFileToString(cache);
        Gson gson = new Gson();
        Map<String, Map<String, String>> checksums;
        Type typeOfHashMap = new TypeToken<Map<String, Map<String, String>>>() {}.getType();
        checksums = gson.fromJson(json, typeOfHashMap);

        assertNotNull(checksums);
        assertTrue(checksums.containsKey("test-course"));
        assertTrue(checksums.get("test-course").containsKey("kissa"));
        assertTrue(checksums.get("test-course").containsKey("asdf"));
        assertTrue(checksums.get("test-course").containsKey("ankka"));

        assertEquals("eujwuc", checksums.get("test-course").get("kissa"));
        assertEquals("alnwnec", checksums.get("test-course").get("asdf"));
        assertEquals("abcdefg", checksums.get("test-course").get("ankka"));
    }

    @Test
    public void keepsOldChecksumsInTheCache() throws IOException, TmcCoreException, URISyntaxException {
        try (FileWriter writer = new FileWriter(cache)) {
            writer.write(
                    "{\"test-course\":{\"kissa\":\"qwerty\",\"asdf2\":\"aijw9\"},"
                            + "\"test-course2\":{\"ankka\":\"22222\"}}");
        }

        ExerciseDownloader mock = Mockito.mock(ExerciseDownloader.class);
        Mockito.when(mock.createCourseFolder(anyString(), anyString())).thenReturn("");
        Mockito.when(mock.handleSingleExercise(any(Exercise.class), anyString())).thenReturn(true);

        Course course = new Course();
        course.setName("test-course");
        course.setExercises(
                new ExerciseBuilder()
                        .withExercise("kissa", 2, "eujwuc")
                        .withExercise("asdf", 793, "alnwnec")
                        .withExercise("ankka", 88, "abcdefg")
                        .build());

        tmcApi = Mockito.mock(TmcApi.class);
        Mockito.when(tmcApi.getCourse(anyInt())).thenReturn(Optional.of(course));

        DownloadExercises dl = new DownloadExercises(mock, "", 8, cache, settings, tmcApi);
        dl.call();
        String json = FileUtils.readFileToString(cache);
        Type typeOfHashMap = new TypeToken<Map<String, Map<String, String>>>() {}.getType();
        Map<String, Map<String, String>> checksums = new Gson().fromJson(json, typeOfHashMap);

        assertNotNull(checksums);
        assertTrue(checksums.containsKey("test-course"));
        assertTrue(checksums.containsKey("test-course2"));
        assertTrue(checksums.get("test-course").containsKey("kissa"));
        assertTrue(checksums.get("test-course").containsKey("asdf"));
        assertTrue(checksums.get("test-course").containsKey("ankka"));
        assertEquals("eujwuc", checksums.get("test-course").get("kissa"));
        assertEquals("alnwnec", checksums.get("test-course").get("asdf"));
        assertEquals("aijw9", checksums.get("test-course").get("asdf2"));
        assertEquals("22222", checksums.get("test-course2").get("ankka"));
    }

    @Test
    public void downloadAllExercises() throws Exception {
        CoreTestSettings settings1 = createSettingsAndWiremock();
        core = new TmcCore(settings1);
        String folder = System.getProperty("user.dir") + "/testResources/";
        ListenableFuture<List<Exercise>> download =
                core.downloadExercises(folder, 35, null);

        List<Exercise> exercises = download.get();
        String exercisePath = folder + "2013_ohpeJaOhja/viikko1/Viikko1_001.Nimi";

        assertEquals(exercises.size(), 153);
        assertTrue(new File(exercisePath).exists());

        FileUtils.deleteDirectory(new File(exercisePath));
        assertFalse(new File(exercisePath).exists());
    }

    @Test
    public void testDowloadingWithProgress() throws Exception {
        CoreTestSettings settings1 = createSettingsAndWiremock();
        core = new TmcCore(settings1);
        ProgressObserver observerMock = Mockito.mock(ProgressObserver.class);
        String folder = System.getProperty("user.dir") + "/testResources/";
        ListenableFuture<List<Exercise>> download =
                core.downloadExercises(folder, 35, observerMock);
        List<Exercise> exercises = download.get();
        String exercisePath = folder + "2013_ohpeJaOhja/viikko1/Viikko1_001.Nimi";
        assertEquals(exercises.size(), 153);
        assertTrue(new File(exercisePath).exists());
        FileUtils.deleteDirectory(new File(exercisePath));
        assertFalse(new File(exercisePath).exists());

        Mockito.verify(observerMock, times(153)).progress(anyDouble(), anyString());
    }

    private CoreTestSettings createSettingsAndWiremock() throws URISyntaxException {
        CoreTestSettings settings1 = new CoreTestSettings();
        String serverAddress = "http://127.0.0.1:8080";
        settings1.setServerAddress(serverAddress);
        settings1.setUsername("test");
        settings1.setPassword("1234");
        wiremock(settings1.getUsername(), settings1.getPassword(), "35", serverAddress);
        return settings1;
    }

    private void wiremock(String username, String password, String courseId, String serverAddress) throws URISyntaxException {
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
                                                ExampleJson.allCoursesExample.replace(
                                                        "https://tmc.mooc.fi/staging",
                                                        serverAddress))));

        wireMockServer.stubFor(
                get(urlPathEqualTo("/courses/" + courseId + ".json"))
                        .willReturn(
                                aResponse()
                                        .withStatus(200)
                                        .withHeader("Content-Type", "text/json")
                                        .withBody(
                                                ExampleJson.courseExample
                                                        .replace(
                                                                "https://tmc.mooc.fi/staging",
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
}
