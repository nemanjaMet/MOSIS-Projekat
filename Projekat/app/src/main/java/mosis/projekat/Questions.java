package mosis.projekat;

/**
 * Created by Neca on 24.6.2016..
 */
public class Questions {
    //long ID; // ako zatreba
    String ID;
    String Category;
    String Questions;
    String CorrectAnswers;
    String WrongAnswers;
    String LongitudeLatitude;
    String LongitudeCategory;
    String LatitudeCategory;
    //String CategoryLongLat;
    String CreatedUser;

    public Questions()
    {
        this.ID = "";
        this.Category = "";
        this.Questions = "";
        this.CorrectAnswers = "";
        this.WrongAnswers = "";
        this.LongitudeLatitude = "";
        this.LongitudeCategory = "";
        this.LatitudeCategory = "";
    }

    public void setCategory(String category)
    {
        this.Category = category;
    }

    public String getCategory()
    {
        return this.Category;
    }

    public void setQuestions(String questions)
    {
        this.Questions = questions;
    }

    public String getQuestions()
    {
        return this.Questions;
    }

    public void setCorrectAnswers(String correctAnswers)
    {
        this.CorrectAnswers = correctAnswers;
    }

    public String getCorrectAnswers()
    {
        return this.CorrectAnswers;
    }

    public void setWrongAnswers(String wrongAnswers)
    {
        this.WrongAnswers = wrongAnswers;
    }

    public String getWrongAnswers()
    {
        return this.WrongAnswers;
    }

    public void setLongitudeLatitude(String longitudeLatitude)
    {
        this.LongitudeLatitude = longitudeLatitude;
    }

    public String getLongitudeLatitude()
    {
        return this.LongitudeLatitude;
    }

    /*public void setCategoryLongLat(String categoryLongLat)
    {
        this.CategoryLongLat = categoryLongLat;
    }

    public String getCategoryLongLat()
    {
        return this.CategoryLongLat;
    }*/

    public void setCreatedUser(String createdUser)
    {
        this.CreatedUser = createdUser;
    }

    public String getCreatedUser()
    {
        return this.CreatedUser;
    }

    public void setLongitudeCategory(String longitudeCategory)
    {
        this.LongitudeCategory = longitudeCategory;
    }

    public String getLongitudeCategory()
    {
        return this.LongitudeCategory;
    }

    public void setLatitudeCategory(String latitudeCategory)
    {
        this.LatitudeCategory = latitudeCategory;
    }

    public String getLatitudeCategory()
    {
        return this.LatitudeCategory;
    }

    public void setID(String id)
    {
        this.ID = id;
    }

    public String getID()
    {
        return this.ID;
    }

}
