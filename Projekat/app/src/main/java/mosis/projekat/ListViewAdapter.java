package mosis.projekat;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Neca on 1.7.2016..
 */
public class ListViewAdapter extends BaseAdapter {



    public ArrayList<HashMap<String, String>> list;
    Activity activity;
    TextView txtFirst;
    TextView txtSecond;
    TextView txtThird;

    public ListViewAdapter(Activity activity,ArrayList<HashMap<String, String>> list){
        super();
        this.activity=activity;
        this.list=list;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        LayoutInflater inflater=activity.getLayoutInflater();



        if(convertView == null){

            convertView=inflater.inflate(R.layout.column_row, null);

            txtFirst=(TextView) convertView.findViewById(R.id.id_number_score_list);
            txtSecond=(TextView) convertView.findViewById(R.id.username_score_list);
            txtThird=(TextView) convertView.findViewById(R.id.points_score_list);

        }

        HashMap<String, String> map=list.get(position);
        txtFirst.setText(map.get(ActivityList.FIRST_COLUMN));
        txtSecond.setText(map.get(ActivityList.SECOND_COLUMN));
        txtThird.setText(map.get(ActivityList.THIRD_COLUMN));

        return convertView;

    }
}
