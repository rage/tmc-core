package fi.helsinki.cs.tmc.core.communication.serialization;

import fi.helsinki.cs.tmc.core.domain.Review;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class ReviewListParser {

    private static final Logger logger = LoggerFactory.getLogger(ReviewListParser.class);

    public List<Review> parseFromJson(String json) {
        if (json == null) {
            logger.warn("Attempted to parse null as json");
            throw new NullPointerException("Json string is null");
        }
        if (json.trim().isEmpty()) {
            logger.info("Attempted to parse empty string as json");
            throw new IllegalArgumentException("Empty input");
        }
        try {
            Gson gson =
                    new GsonBuilder()
                            .registerTypeAdapter(Date.class, new CustomDateDeserializer())
                            .create();

            Review[] reviews = gson.fromJson(json, Review[].class);
            return Arrays.asList(reviews);
        } catch (RuntimeException ex) {
            logger.warn("Failed to parse review list", ex);
            throw new RuntimeException("Failed to parse review list: " + ex.getMessage(), ex);
        }
    }
}
