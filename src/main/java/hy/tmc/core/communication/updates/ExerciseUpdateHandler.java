package hy.tmc.core.communication.updates;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import hy.tmc.core.communication.TmcJsonParser;
import hy.tmc.core.domain.Course;
import hy.tmc.core.domain.Exercise;
import hy.tmc.core.exceptions.TmcCoreException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;

public class ExerciseUpdateHandler extends UpdateHandler<Exercise> {

    private File cache;
    private Map<String, Map<String, String>> exerciseChecksums;

    public ExerciseUpdateHandler(File cacheFile, TmcJsonParser jsonParser) throws TmcCoreException {
        super(jsonParser);
        exerciseChecksums = new HashMap<>();
        if (cacheFile == null) {
            String errorMessage = "ExerciseUpdateHandler requires non-null cacheFile to function";
            throw new TmcCoreException(errorMessage);
        }
        this.cache = cacheFile;
    }

    @Override
    public List<Exercise> fetchFromServer(Course currentCourse) throws IOException {
        List<Exercise> exercises = jsonParser.getExercisesFromServer(currentCourse);
        readChecksumMap();
        if (exercises == null) {
            return Lists.newArrayList();
        }
        return exercises;
    }

    @Override
    protected boolean isNew(Exercise exercise) {
        if (exerciseChecksums.containsKey(exercise.getCourseName()) &&
                exerciseChecksums.get(exercise.getCourseName()).containsKey(exercise.getName())) {
            String earlierChecksum = exerciseChecksums.get(exercise.getCourseName()).get(exercise.getName());
            return ! exercise.getChecksum().equals(earlierChecksum);
        }
        return true;
    }

    protected void readChecksumMap() throws FileNotFoundException, IOException {
        String json = FileUtils.readFileToString(cache, Charset.forName("UTF-8"));
        Type typeOfMap = new TypeToken<Map<String, Map<String, String>>>() { }.getType();
        this.exerciseChecksums = new Gson().fromJson(json, typeOfMap);
        if (this.exerciseChecksums == null) {
            this.exerciseChecksums = new HashMap<>();
        }
    }
}
