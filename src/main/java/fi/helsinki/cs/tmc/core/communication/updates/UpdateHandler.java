package fi.helsinki.cs.tmc.core.communication.updates;

import fi.helsinki.cs.tmc.core.communication.TmcApi;
import fi.helsinki.cs.tmc.core.domain.Course;

import java.util.ArrayList;
import java.util.List;

public abstract class UpdateHandler<T> {

    protected TmcApi tmcApi;

    public UpdateHandler(TmcApi tmcApi) {
        this.tmcApi = tmcApi;
    }

    protected abstract boolean isNew(T object);

    public abstract List<T> fetchFromServer(Course course) throws Exception;

    public List<T> getNewObjects(Course course) throws Exception {
        return filterNew(fetchFromServer(course));
    }

    private List<T> filterNew(List<T> objects) {
        List<T> newObjects = new ArrayList<>();
        for (T object : objects) {
            if (isNew(object)) {
                newObjects.add(object);
            }
        }
        return newObjects;
    }
}
