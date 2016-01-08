package com.ui.gridview;

import android.net.Uri;

public class PhotoData {
    private long mId;
    private Uri mUri;

    public void setId(long id) {
        mId = id;
    }

    public long getId() {
        return mId;
    }

    public void setUri(Uri uri) {
        mUri = uri;
    }

    public Uri getUri() {
        return mUri;
    }
}
