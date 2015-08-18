package fi.helsinki.cs.tmc.core.communication;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import fi.helsinki.cs.tmc.core.CoreTestSettings;
import fi.helsinki.cs.tmc.core.domain.submission.SubmissionResult;
import fi.helsinki.cs.tmc.core.domain.submission.SubmissionResult.Status;
import fi.helsinki.cs.tmc.core.testhelpers.ExampleJson;

import org.junit.Before;
import org.junit.Test;

import org.mockito.Mockito;

public class SubmissionPollerTest {

    private SubmissionPoller submissionPoller;
    private String url =
            "https://tmc.mooc.fi/staging/submissions/1764.json?api_version=7&client=tmc_cli&client_version=1";
    private CoreTestSettings settings;
    private TmcApi tmcApi;

    @Before
    public void setup() {
        settings = new CoreTestSettings();
        settings.setUsername("chang");
        settings.setPassword("rajani");
        tmcApi = Mockito.mock(TmcApi.class);
        submissionPoller = new SubmissionPoller(tmcApi, 30);
    }

    @Test
    public void successfulSubmission() throws Exception {
        Mockito.when(tmcApi.getRawTextFrom(Mockito.anyString()))
                .thenReturn(ExampleJson.successfulSubmission);
        SubmissionResult output = submissionPoller.getSubmissionResult(url);
        assertFalse(output == null);
        assertEquals("2014-mooc-no-deadline", output.getCourse());
        assertEquals(Status.OK, output.getStatus());
    }

    @Test
    public void unsuccessfulSubmission() throws Exception {
        Mockito.when(tmcApi.getRawTextFrom(Mockito.anyString()))
                .thenReturn(ExampleJson.failedSubmission);
        SubmissionResult output = submissionPoller.getSubmissionResult(url);
        assertFalse(output == null);
        assertEquals("2014-mooc-no-deadline", output.getCourse());
        assertEquals(Status.FAIL, output.getStatus());
    }
}
