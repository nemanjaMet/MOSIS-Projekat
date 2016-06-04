package mosis.projekat;

import android.graphics.Bitmap;
import android.media.Image;

import java.io.Serializable;

/**
 * Created by Neca on 2.6.2016..
 */
public class User implements Serializable {
    long ID;
    String username;
    String password;
    String name;
    String lastname;
    String phoneNumber;
    Bitmap image;

    private static final long serialVersionUID = 1L;

    public User(String username, String password)
    {
        this.username = username;
        this.password = password;
        this.name = "";
        this.lastname = "";
        this.phoneNumber = "";
        //this.image = null;
    }

    public void setUsername(String username)
    {
        this.username = username;
    }

    public String getUsername()
    {
        return username;
    }

    public void setPassword(String password)
    {
        this.password = password;
    }

    public String getPassword()
    {
        return password;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getName()
    {
        return name;
    }

    public void setLastName(String lastname)
    {
        this.lastname = lastname;
    }

    public String getLastname()
    {
        return lastname;
    }

    public void setPhoneNumber(String phoneNumber)
    {
        this.phoneNumber = phoneNumber;
    }

    public String getPhoneNumber()
    {
        return phoneNumber;
    }

    public void setImage(Bitmap image)
    {
        this.image = image;
    }

    public Bitmap getImage()
    {
        return image;
    }

    public long getID(){
        return ID;
    }

    public void setID(long ID){
        this.ID = ID;
    }
}
