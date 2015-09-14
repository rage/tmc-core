package fi.helsinki.cs.tmc.core.domain;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;
import java.util.Objects;

/**
 * A pair (course name, exercise name).
 */
public final class ExerciseKey {
    public final String courseName;
    public final String exerciseName;

    public ExerciseKey(String courseName, String exerciseName) {
        this.courseName = courseName;
        this.exerciseName = exerciseName;
    }

    @Override
    public boolean equals(Object obj) {
        if (!obj instanceof ExerciseKey) return false;
        ExerciseKey that = (ExerciseKey) obj;
        return Objects.equals(this.courseName, that.courseName)
                && Objects.equals(this.exerciseName, that.exerciseName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(courseName, exerciseName);
    }

    @Override
    public String toString() {
        return courseName + "/" + exerciseName;
    }

    public static class GsonAdapter
            implements JsonSerializer<ExerciseKey>, JsonDeserializer<ExerciseKey> {
        @Override
        public JsonElement serialize(ExerciseKey key, Type type, JsonSerializationContext jsc) {
            return new JsonPrimitive(key.toString());
        }

        @Override
        public ExerciseKey deserialize(JsonElement je, Type type, JsonDeserializationContext jdc)
                throws JsonParseException {
            String[] parts = je.getAsString().split("/", 2);
            if (parts.length != 2) {
                throw new JsonParseException(
                        "Invalid ExerciseKey representation: \"" + je.getAsString() + "\"");
            }
            return new ExerciseKey(parts[0], parts[1]);
        }
    }
}
