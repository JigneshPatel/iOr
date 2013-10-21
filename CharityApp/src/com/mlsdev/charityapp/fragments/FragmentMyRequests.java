package com.mlsdev.charityapp.fragments;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.mlsdev.charityapp.ActivityRequests;
import com.mlsdev.charityapp.DialogActivity;
import com.mlsdev.charityapp.R;
import com.mlsdev.charityapp.Request;
import com.mlsdev.charityapp.models.Category;
import com.mlsdev.charityapp.models.Helper;
import com.mlsdev.charityapp.models.UserRequest;

import java.util.ArrayList;

/**
 * Created by android-dev on 12.09.13.
 */
public class FragmentMyRequests extends Fragment {

    public ArrayList<UserRequest> mRequests = new ArrayList<UserRequest>();
    RequestsAdapter mRequestsAdapter;

    public FragmentMyRequests(ArrayList<UserRequest> requests) {
        super();
        mRequests = requests;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_requests, container, false);
        setupView(root);
        return root;

    }

    private void setupView(View root) {
        ListView lvPeople = (ListView) root.findViewById(R.id.lv_requests);
        TextView tvNoRequests = (TextView) root.findViewById(R.id.tv_no_results);

        if(mRequests.isEmpty()){
            tvNoRequests.setVisibility(View.VISIBLE);
            lvPeople.setVisibility(View.GONE);
        } else {
            tvNoRequests.setVisibility(View.GONE);
            lvPeople.setVisibility(View.VISIBLE);
        }

        mRequestsAdapter = new RequestsAdapter(getActivity(), R.layout.list_item_request, mRequests);
        lvPeople.setAdapter(mRequestsAdapter);
        lvPeople.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                ((DialogActivity)getActivity()).showProgressDialog();
                new AsyncTask<Void, Void, ArrayList<Helper>>(){

                    @Override
                    protected ArrayList<Helper> doInBackground(Void... params) {
                        return Request.getRequestHelpers(getActivity(), Integer.valueOf(mRequests.get(position).getId()));
                    }

                    @Override
                    protected void onPostExecute(ArrayList<Helper> result) {
                        ((DialogActivity)getActivity()).closeProgressDialog();
                        if(result==null){
                            ((DialogActivity)getActivity()).showErrorToast();
                        } else {
                            //TO DO
                            ((ActivityRequests) getActivity()).
                                    setFragment(new FragmentNeedHelpPeople(new Category(0,mRequests.get(position).getDescription()),result));
                        }
                    }
                }.execute();
            }
        });
    }


    private class RequestsAdapter extends ArrayAdapter<UserRequest> {

        private Context mContext;
        private int mLayoutResourceId;
        private ArrayList<UserRequest> mData = new ArrayList<UserRequest>();


        public RequestsAdapter(Context context, int layoutResourceId, ArrayList<UserRequest> data) {
            super(context, layoutResourceId, data);
            this.mLayoutResourceId = layoutResourceId;
            this.mContext = context;
            this.mData = data;
        }


        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            View row = convertView;
            ItemHolder holder;
            if (row == null) {
                LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
                row = inflater.inflate(mLayoutResourceId, parent, false);
                holder = new ItemHolder();
                holder.tvName = (TextView)row.findViewById(R.id.tvName);
                holder.tvCount = (TextView)row.findViewById(R.id.tvCount);
                holder.ivClose = (ImageView)row.findViewById(R.id.ivClose);
                row.setTag(holder);
            } else {
                holder = (ItemHolder)row.getTag();
            }

            final UserRequest item = mData.get(position);
            holder.tvName.setText(item.getDescription());
            holder.tvCount.setText(item.getHelpersCount());
            holder.ivClose.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((DialogActivity)getActivity()).showProgressDialog();
                    new AsyncTask<Void, Void, String>(){

                        @Override
                        protected String doInBackground(Void... params) {
                            return Request.removeRequest(getActivity(), Integer.valueOf(mRequests.get(position).getId()));
                        }

                        @Override
                        protected void onPostExecute(String result) {
                            ((DialogActivity)getActivity()).closeProgressDialog();
                            if(result==null){
                                ((DialogActivity)getActivity()).showErrorToast();
                            } else {
                                mRequests.remove(position);
                                mRequestsAdapter.notifyDataSetChanged();
                            }
                        }
                    }.execute();
                }
            });
            return row;
        }
        class ItemHolder {
            TextView tvName;
            TextView tvCount;
            ImageView ivClose;
        }
    }
}
