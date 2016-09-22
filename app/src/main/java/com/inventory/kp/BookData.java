package com.inventory.kp;

import java.util.ArrayList;

/**
 * Created by abhilasha.jain on 1/4/2016.
 */
public class BookData {

    private String mBookName;
    private String mBookDescription;
    private int mCost;
    private String mBookCover;
    private String mBookType;
    private String mDate;
    private String mTime;
    private ArrayList<String> mImagesPath  = new ArrayList<>();

    BookData() {
    }

    public void setBookName(String bookName) {
        mBookName = bookName;
    }
    public void setBookDescription(String mBookDescription) {
        this.mBookDescription = mBookDescription;
    }

    public void setCost(int mCost) {
        this.mCost = mCost;
    }

    public void setBookCover(String mBookCover) {
        this.mBookCover = mBookCover;
    }

    public void setBookType(String mBookType) {
        this.mBookType = mBookType;
    }

    public void setDate(String mDate) {
        this.mDate = mDate;
    }

    public void setTime(String mTime) {
        this.mTime = mTime;
    }

    public void setImagesPathList(ArrayList<String> mImagesPath) {
        this.mImagesPath = mImagesPath;
    }

    @Override
    public String toString() {
        return  "Bookname : " + mBookName +
                "BookDescription : " + mBookDescription +
                "Book Cost : " + mCost +
                "Book Cover : " + mBookCover +
                "Book Type : " + mBookType +
                "Date : " + mDate +
                "Time : " + mTime +
                "Images : " + mImagesPath;
    }

    public String writeToFileFormat(String separator) {
        return  mBookName + separator +
                mBookDescription + separator +
                mCost + separator +
                mBookCover + separator +
                mBookType + separator +
                mDate + separator +
                mTime + separator +
                mImagesPath + "\n";
    }
}
