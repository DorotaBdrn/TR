package com.hfad.travelersample2;

public class FindPlace {
    private String placeName, fullname, postImage;

    public FindPlace() {

    }

    public FindPlace(String placeName, String fullname, String postImage) {
        this.placeName = placeName;
        this.fullname = fullname;
        this.postImage = postImage;
    }

    public String getPlaceName() {
        return placeName;
    }

    public void setPlaceName(String placeName) {
        this.placeName = placeName;
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public String getPostImage() {
        return postImage;
    }

    public void setPostImage(String postImage) {
        this.postImage = postImage;
    }
}
