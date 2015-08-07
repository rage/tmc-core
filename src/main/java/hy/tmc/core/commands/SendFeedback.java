package hy.tmc.core.commands;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import hy.tmc.core.communication.HttpResult;
import hy.tmc.core.communication.UrlCommunicator;
import hy.tmc.core.configuration.TmcSettings;
import hy.tmc.core.exceptions.TmcCoreException;

import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;

public class SendFeedback extends Command<HttpResult> {

    private Map<String, String> answers;
    private String url;

    public SendFeedback(Map<String, String> answers, String url, TmcSettings settings) {
        super(settings);
        this.answers = answers;
        this.url = url;
    }

    @Override
    public void checkData() throws TmcCoreException, IOException {
        if (answers == null || url == null) {
            throw new TmcCoreException("must give answers and feedback url");
        }
    }

    @Override
    public HttpResult call() throws Exception {
        JsonArray feedbackAnswers = new JsonArray();

        for (Entry<String, String> e : answers.entrySet()) {
            JsonObject jsonAnswer = new JsonObject();
            jsonAnswer.addProperty("question_id", e.getKey());
            jsonAnswer.addProperty("answer", e.getValue());
            feedbackAnswers.add(jsonAnswer);
        }

        JsonObject req = new JsonObject();
        req.add("answers", feedbackAnswers);

        return new UrlCommunicator(settings).makePostWithJson(req, url);
    }
}
