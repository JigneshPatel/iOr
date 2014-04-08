package com.ior.charityapp.models;

/**
 * Created by android-dev on 30.08.13.
 */
public class Category {
    public int mCategoryId;
    public String mName;
    public String available ;
    
    public Category(int id, String category, String availableFlag){
        mCategoryId = id;
        mName = category;
        available = availableFlag ;
    }

}