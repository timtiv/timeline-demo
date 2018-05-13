package com.tim.cubo.model;

import android.os.Parcel;
import android.os.Parcelable;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.tim.cubo.TimeConverter;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Alert implements Parcelable {

    @JsonProperty("alert_id")
    private String id;
    @JsonProperty("video")
    private String videoUrl;
    @JsonProperty("ts")
    private long ts;

    public Alert() {
    }

    protected Alert(Parcel in) {
        id = in.readString();
        videoUrl = in.readString();
        ts = in.readLong();
    }

    public static final Creator<Alert> CREATOR = new Creator<Alert>() {
        @Override
        public Alert createFromParcel(Parcel in) {
            return new Alert(in);
        }

        @Override
        public Alert[] newArray(int size) {
            return new Alert[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(videoUrl);
        dest.writeLong(ts);
    }

    public String getId() {
        return id;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public long getTs() {
        return TimeConverter.adjustTimestamp(ts);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Alert alert = (Alert) o;

        return id.equals(alert.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
