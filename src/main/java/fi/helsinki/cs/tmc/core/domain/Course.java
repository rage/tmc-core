package fi.helsinki.cs.tmc.core.domain;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;
import java.net.URI;

public class Course {

    private int id;
    private String name;

    private List<Exercise> exercises;

    @SerializedName("details_url")
    private URI detailsUrl;

    @SerializedName("unlock_url")
    private URI unlockUrl;

    @SerializedName("comet_url")
    private URI cometUrl;

    public URI getReviewsUrl() {
        return reviewsUrl;
    }

    public void setReviewsUrl(URI reviewsUrl) {
        this.reviewsUrl = reviewsUrl;
    }

    @SerializedName("spyware_urls")
    private List<URI> spywareUrls;

    @SerializedName("reviews_url")
    private URI reviewsUrl;

    private List<URI> unlockables;

    public Course() {
        this(null);
    }

    public Course(String name) {
        this.name = name;
        this.exercises = new ArrayList<>();
        this.unlockables = new ArrayList<>();
        this.spywareUrls = new ArrayList<>();
    }

    public List<URI> getSpywareUrls() {
        return spywareUrls;
    }

    public void setSpywareUrls(List<URI> spywareUrls) {
        this.spywareUrls = spywareUrls;
    }

    public List<Exercise> getExercises() {
        return exercises;
    }

    public void setExercises(List<Exercise> exercises) {
        this.exercises = exercises;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public URI getDetailsUrl() {
        return detailsUrl;
    }

    public void setDetailsUrl(URI detailsUrl) {
        this.detailsUrl = detailsUrl;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 79 * hash + this.id;
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Course other = (Course) obj;
        return this.id == other.id;
    }

    private boolean exercisesLoaded;

    public URI getUnlockUrl() {
        return unlockUrl;
    }

    public void setUnlockUrl(URI unlockUrl) {
        this.unlockUrl = unlockUrl;
    }

    public URI getCometUrl() {
        return cometUrl;
    }

    public void setCometUrl(URI cometUrl) {
        this.cometUrl = cometUrl;
    }

    public boolean isExercisesLoaded() {
        return exercisesLoaded;
    }

    public void setExercisesLoaded(boolean exercisesLoaded) {
        this.exercisesLoaded = exercisesLoaded;
    }

    public List<URI> getUnlockables() {
        return unlockables;
    }

    public void setUnlockables(List<URI> unlockables) {
        this.unlockables = unlockables;
    }

    @Override
    public String toString() {
        //TODO: this cannot return anything else until PreferencesPanel is fixed to not use
        //toString() to present Course objects
        return name;
    }
}
