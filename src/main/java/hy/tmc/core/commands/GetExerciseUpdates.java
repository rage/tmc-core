package hy.tmc.core.commands;

import hy.tmc.core.communication.updates.ExerciseUpdateHandler;
import hy.tmc.core.configuration.TmcSettings;
import hy.tmc.core.domain.Course;
import hy.tmc.core.domain.Exercise;
import hy.tmc.core.exceptions.TmcCoreException;

import java.util.List;


public class GetExerciseUpdates extends Command<List<Exercise>> {

    private final Course course;
    private final ExerciseUpdateHandler handler;

    public GetExerciseUpdates(Course course, ExerciseUpdateHandler handler, TmcSettings settings) {
        super(settings);
        this.course = course;
        this.handler = handler;
    }

    @Override
    public void checkData() throws TmcCoreException {
        if (handler == null) {
            throw new TmcCoreException("updatehandler must be given");
        }
    }

    @Override
    public List<Exercise> call() throws Exception {
        return handler.getNewObjects(course);
    }

}
