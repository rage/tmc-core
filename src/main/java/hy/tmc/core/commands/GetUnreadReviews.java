package hy.tmc.core.commands;

import hy.tmc.core.communication.updates.ReviewHandler;
import hy.tmc.core.configuration.TmcSettings;
import hy.tmc.core.domain.Course;
import hy.tmc.core.domain.Review;
import hy.tmc.core.exceptions.TmcCoreException;

import java.io.IOException;
import java.util.List;


public class GetUnreadReviews extends Command<List<Review>>{

    private final Course course;
    private final ReviewHandler handler;

    public GetUnreadReviews(Course course, ReviewHandler handler, TmcSettings settings) {
        super(settings);
        this.handler = handler;
        this.course = course;
    }

    @Override
    public void checkData() throws TmcCoreException {
        if (handler == null) {
            throw new TmcCoreException("reviewHandler not given");
        }
    }

    @Override
    public List<Review> call() throws Exception {
        return handler.getNewObjects(course);
    }

}
