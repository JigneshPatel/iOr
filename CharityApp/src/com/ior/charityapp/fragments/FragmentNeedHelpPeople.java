package com.ior.charityapp.fragments;

import static com.ior.charityappior.Utils.log;
import static java.lang.Thread.sleep;

import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.ior.charityapp.models.Category;
import com.ior.charityapp.models.Helper;
import com.ior.charityappior.ActivityNeedHelp;
import com.ior.charityappior.DialogActivity;
import com.ior.charityappior.R;
import com.ior.charityappior.Request;
import com.ior.charityappior.Utils;

/**
 * Created by android-dev on 27.08.13.
 */
@SuppressLint("ValidFragment")
public class FragmentNeedHelpPeople extends ParentFragment {
    private final Category mCategory;
    private int startDistValue ;
    private String descMsg ;
    private final ArrayList<Helper> mPeople;
    private static final int SEC = 1000;
	private static final int WAIT_TIME = 30;
	int nextDistance ;
	Button btCustomSearch ;
	AlertDialog mSearchPeopleDialog;
	ListView lvPeople ;
	String requestestIdValue ;
    AsyncTask<Void, Integer, Void> mSearchPeopleTask;
    @SuppressLint("ValidFragment")
	public FragmentNeedHelpPeople(Category category, final int startDist, final String message ,ArrayList<Helper> people, String requestId) {
        super();
        mCategory = category;
        startDistValue = startDist ;
        descMsg = message ;
        mPeople = people;
        requestestIdValue = requestId ;
        log("startDistValue", "==" + startDistValue) ;
        log("descMsg", "==" + descMsg) ;
        
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_need_help_peoples, container, false);
        setupView(root);
        return root;

    }

    private void setupView(View root) {

        TextView tvHeader = (TextView)root.findViewById(R.id.tvHeaderPeople);
        tvHeader.append(" \"" + mCategory.mName+ "\"" );


            lvPeople = (ListView) root.findViewById(R.id.lvPeople);
            lvPeople.setAdapter(new PeopleAdapter(getActivity(),R.layout.people_list_item, mPeople));
            lvPeople.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    ((DialogActivity)getActivity()).showDialog(mPeople.get(position).mName,mCategory.mName, mPeople.get(position).mPhoneNumber);
                }
            });
            
            btCustomSearch = (Button) root.findViewById(R.id.btCustomSearch) ;
            btCustomSearch.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					//((ActivityNeedHelp) getActivity()).searchPeopel(0, 1, 0,"sds") ;
					
					int newDistance = Utils.getNewDistance(startDistValue);
					log("startDistValue", "==" + startDistValue) ;
					log("newDistance", "==" + newDistance) ;
					//mSearchPeopleTask = new SearchPeopleTask(getActivity(), startDistValue, newDistance, 0, false, descMsg).execute();
					
					if(descMsg == null || descMsg.length() == 0) {
						mSearchPeopleTask = new SearchPeopleTask(getActivity(), 0, newDistance, 0, false, "").execute();
					} else {
						mSearchPeopleTask = new SearchPeopleTask(getActivity(), 0, newDistance, 0, true, descMsg).execute();
					}
				}
			}) ;
            
            nextDistance = Utils.getNewDistance(startDistValue);
            btCustomSearch.setText( String.format(stringPicker.getString("search_next_dist_helper"), nextDistance)) ;
    }
    
    class SearchPeopleTask extends AsyncTask<Void, Integer, Void> {
		ArrayList<Helper> people;
		int mMinRadius;
		int mMaxRadius;
		int mId;
		Context mContext;
		boolean mIsPushSearch;
		String mDescriptionValue;

		SearchPeopleTask(Context context, int minRadius, int maxRadius, int id,
				boolean isPushSearch, String description) {
			log("fds", "SearchPeopleTask == " + description);
			log("fds", "minRadius == " + minRadius);
			log("fds", "maxRadius == " + maxRadius);
			
			mMinRadius = minRadius;
			mMaxRadius = maxRadius;
			mId = id;
			mContext = context;
			mIsPushSearch = isPushSearch;
			//mDescriptionValue = description; 
			if(description == null || description.length() == 0) {
				mDescriptionValue = "";
			}else{
				mDescriptionValue = description;
			}
			
		}

		@Override
		protected void onProgressUpdate(final Integer... values) {
			log("dfs", "onProgressUpdate");
		
			getActivity().runOnUiThread(new Runnable() {
				@Override
				public void run() {
					((DialogActivity) getActivity()).updateProgressDialogMessage(values[0]);
				}
			});

		}

		@Override
		protected Void doInBackground(Void... params) {
			log("fds", "doInBackground");
			if (mIsPushSearch) {
				log("fds", "mIsPushSearch=true");
				
				String id = Request.sendMessage(getActivity(), mMinRadius, mMaxRadius, mDescriptionValue, "1", requestestIdValue);
				if (id == null)
					return null;
				mId = Integer.valueOf(id);
				if (mId == 0) {
					people = new ArrayList<Helper>();
				} else {
					try {
						for (int i = WAIT_TIME; i > 0; i--) {
							onProgressUpdate(i);
							sleep(SEC);
						}
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					people = Request.getRequestHelpers(mContext, mId);
					//setSelectedCategory(new Category(-1, mDescription));
				}
			} else {
				people = Request.getPeople(mContext, mMinRadius, mMaxRadius,
						mId);
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void v) {
			((DialogActivity) getActivity()).closeProgressDialog();
			if (people != null) {
				if (people.size() == 0) {
					int newDistance = Utils.getNewDistance(mMaxRadius);
					if (newDistance != 1) {
						
						showSearchPeopleDialog(mMaxRadius, newDistance, mId,
								mIsPushSearch, mDescriptionValue);
					} else {
						startDistValue = 0 ;
						 btCustomSearch.setText( String.format(stringPicker.getString("search_next_dist_helper"), startDistValue)) ;
						showNoResultDialog();
					}
				} else {
					int newDistances = Utils.getNewDistance(mMaxRadius);
					 btCustomSearch.setText( String.format(stringPicker.getString("search_next_dist_helper"), newDistances)) ;
						
					lvPeople.invalidateViews();
					//setFragment(new FragmentNeedHelpPeople(mSelectedCategory, people));
				}
			} else {
				((DialogActivity) getActivity()).showErrorToast();
			}
		}
	}

	private void showNoResultDialog() {
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
		alertDialogBuilder
				.setMessage(stringPicker.getString("no_results_text"))
				.setCancelable(true);
		AlertDialog alertDialog = alertDialogBuilder.create();
		alertDialog.show();
	}

	public AlertDialog showSearchPeopleDialog(final int oldDistance,
			final int newDistance, final int categoryId,
			final boolean isPushMessage, final String description) {
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());

		/*
		 * alertDialogBuilder.setTitle(getString(R.string.no_people_text,
		 * oldDistance));
		 */

		alertDialogBuilder.setTitle(String.format(
				stringPicker.getString("no_people_text"), oldDistance));
		/*
		 * alertDialogBuilder .setMessage( isPushMessage ? getString(
		 * R.string.search_to_push_range, newDistance) :
		 * getString(R.string.search_range_text, newDistance))
		 */
		 btCustomSearch.setText( String.format(stringPicker.getString("search_next_dist_helper"), newDistance)) ;
         
		alertDialogBuilder
				.setMessage(
						isPushMessage ? String.format(
								stringPicker.getString("search_to_push_range"),
								newDistance) : String.format(
								stringPicker.getString("search_range_text"),
								newDistance))
				.setCancelable(false)
				.setNegativeButton(stringPicker.getString("mlt_yes"),
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								startDistValue = newDistance;
								mSearchPeopleTask = new SearchPeopleTask(
										getActivity(), oldDistance,
										newDistance, categoryId, isPushMessage,
										description).execute();
								mSearchPeopleDialog.cancel();
								((DialogActivity) getActivity()).showProgressDialog();
							}
						})
				.setPositiveButton(stringPicker.getString("mlt_no"),
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								dialog.cancel();
							}
						});

		AlertDialog alertDialog = alertDialogBuilder.create();

		alertDialog.show();
		mSearchPeopleDialog = alertDialog;
		return alertDialog;
	}
}