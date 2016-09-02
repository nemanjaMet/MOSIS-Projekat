package mosis.projekat;

/**
 * Created by Neca on 5.7.2016..
 */
public class Device {
    private final String mName;
    private final String mAddress;
    private final boolean mPaired;

    public Device(String name, String address, boolean paired) {
        mName = name;
        mAddress = address;
        mPaired = paired;
    }

    public String getName() {
        return mName;
    }

    public String getAddress() {
        return mAddress;
    }

    public boolean isPaired(){
        return mPaired;
    }
}
