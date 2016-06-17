package mosis.projekat;

import android.graphics.Bitmap;

import java.io.Serializable;

/**
 * Created by Neca on 2.6.2016..
 */
public class User implements Serializable {
    long ID;
    String Username;
    String Password;
    String Name;
    String Lastname;
    String PhoneNumber;
    //Bitmap Image;
    String Image;
    String Created;
    String Longitude;
    String Latitude;

    private static final long serialVersionUID = 1L;

    public User()
    {
        this.Username = "";
        this.Password = "";
        this.Name = "";
        this.Lastname = "";
        this.PhoneNumber = "";
        //this.Image = null;
        this.Image = "";
        this.Created = "";
        this.Longitude = "";
        this.Latitude = "";
    }

    public User(String username)
    {
        this.Username = username;
        this.Password = "";
        this.Name = "";
        this.Lastname = "";
        this.PhoneNumber = "";
        //this.Image = null;
        this.Image = "";
        this.Created = "";
    }

    public void setUsername(String username)
    {
        this.Username = username;
    }

    public String getUsername()
    {
        return Username;
    }

    public void setPassword(String password)
    {
        this.Password = password;
    }

    public String getPassword()
    {
        return Password;
    }

    public void setName(String name)
    {
        this.Name = name;
    }

    public String getName()
    {
        return Name;
    }

    public void setLastName(String lastname)
    {
        this.Lastname = lastname;
    }

    public String getLastname()
    {
        return Lastname;
    }

    public void setPhoneNumber(String phoneNumber)
    {
        this.PhoneNumber = phoneNumber;
    }

    public String getPhoneNumber()
    {
        return PhoneNumber;
    }

    /*public void setImage(Bitmap image)
    {
        this.Image = image;
    }

    public Bitmap getImage()
    {
        return Image;
    }*/

    public void setImage(String image)
    {
        this.Image = image;
    }

    public String getImage()
    {
        return Image;
    }

    public void setCreated(String created)
    {
        this.Created = created;
    }

    public String getCreated()
    {
        return this.Created;
    }

    public long getID(){
        return ID;
    }

    public void setID(long ID){
        this.ID = ID;
    }

    public void setLongitude(String longitude)
    {
        this.Longitude = longitude;
    }

    public String getLongitude()
    {
        return this.Longitude;
    }

    public void setLatitude(String latitude)
    {
        this.Latitude = latitude;
    }

    public String getLatitude()
    {
        return this.Latitude;
    }
}
