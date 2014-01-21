package com.ior.charityapp.fragments;

/**
 * Created by android-dev on 09.09.13.
 */

import static com.ior.charityappior.Utils.log;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.widget.FacebookDialog;
import com.facebook.widget.FacebookDialog.ShareDialogBuilder;
import com.google.android.gms.plus.PlusShare;
import com.ior.charityapp.invite.DeviceEmailListActivity;
import com.ior.charityapp.invite.PhoneFriendListActivity;
import com.ior.charityapp.lang.StringPicker;
import com.ior.charityapp.lang.ViewProcessor;
import com.ior.charityappior.ActivityMain;
import com.ior.charityappior.R;
import com.ior.charityappior.Request;

public class AboutFragment extends ParentFragment implements OnClickListener {

	private static final String ARG_POSITION = "position";

	private int position;
	private StringPicker stringPicker;

	private String[] strings_about;
	Button changeLagnBtn;
	TextView tvText;
	LinearLayout languageLayout;
	private RadioButton radioLangButton, radioEngBtn, radioHeBtn;
	private RadioGroup radioLangGroup;
	private View root;
	private View inviteLayout;
	String tempLang, langVal;
	String languageResponse;
	private ProgressDialog progressDialog;
	int selectedRadioBtn;

	public static AboutFragment newInstance(int position) {
		log("About View", "position === " + position);

		AboutFragment f = new AboutFragment();
		Bundle b = new Bundle();
		b.putInt(ARG_POSITION, position);
		f.setArguments(b);
		return f;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		stringPicker = new StringPicker(getActivity(), getLanguageFileName());
		// strings_about = new String[] { stringPicker.getString("description"),
		// stringPicker.getString("concept"),
		// "Change Language Text",
		// stringPicker.getString("mlt_invite_friends")
		// };
		strings_about = new String[] {
				stringPicker.getString("description"),
				stringPicker.getString("concept"),
				"Change Language Text",
				stringPicker.getString("mlt_invite_friends") };

		position = getArguments().getInt(ARG_POSITION);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		root = inflater.inflate(R.layout.fragment_about, container, false);
		tvText = (TextView) root.findViewById(R.id.tvAbout);
		languageLayout = (LinearLayout) root
				.findViewById(R.id.changePassLayout);
		changeLagnBtn = (Button) root.findViewById(R.id.btnDisplay);
		radioLangGroup = (RadioGroup) root.findViewById(R.id.radioLanguage);
		radioEngBtn = (RadioButton) root.findViewById(R.id.radioEnglish);
		radioHeBtn = (RadioButton) root.findViewById(R.id.radioHebrew);
		inviteLayout = root.findViewById(R.id.layout_invite);

		if (getActivity()
				.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
				.getString("lang", "").equalsIgnoreCase("English")) {
			radioLangGroup.check(radioEngBtn.getId());
			changeLagnBtn.setText("Change Language");
		} else if (getActivity()
				.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
				.getString("lang", "").equalsIgnoreCase("Hebrew")) {
			radioLangGroup.check(radioHeBtn.getId());
			changeLagnBtn.setText("החלף שפה");
		}

		languageLayout.setVisibility(View.GONE);
		log("About View", "position === " + position);
		String descText = strings_about[position];
		Spannable spanText = new SpannableString(strings_about[position]);
		log("About View", "spanText === " + descText);

		// "Change Language Text"
		if (descText.equalsIgnoreCase("Change Language Text")) {
			tvText.setVisibility(View.GONE);
			languageLayout.setVisibility(View.VISIBLE);

			changeLagnBtn.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View arg0) {
					// get selected radio button from radioGroup
					int selectedId = radioLangGroup.getCheckedRadioButtonId();

					// find the radiobutton by returned id
					radioLangButton = (RadioButton) root
							.findViewById(selectedId);
					langVal = (String) radioLangButton.getTag();
					if (langVal.length() > 0) {

						if (langVal.equalsIgnoreCase("0")) {
							tempLang = "English";
							progressDialog = ProgressDialog.show(getActivity(),
									"", "Sending request");
						} else {
							tempLang = "Hebrew";
							progressDialog = ProgressDialog.show(getActivity(),
									"", "שולח בקשה");
						}
						progressDialog.show();
						new changeLanguage().execute();
					}

				}

			});

		} else if (position == 3) {
			inviteLayout.setVisibility(View.VISIBLE);
			View fb = inviteLayout.findViewById(R.id.vg_fb);
			View phone = inviteLayout.findViewById(R.id.vg_phone);
			//View plus = inviteLayout.findViewById(R.id.vg_plus);
			View gmail = inviteLayout.findViewById(R.id.vg_gmail);

			fb.setOnClickListener(this);
			phone.setOnClickListener(this);
			//plus.setOnClickListener(this);
			gmail.setOnClickListener(this);
		} else {
			changeLagnBtn.setVisibility(View.GONE);
			tvText.setText(spanText);
		}

		return root;

		/*
		 * LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT,
		 * LayoutParams.MATCH_PARENT);
		 * 
		 * FrameLayout fl = new FrameLayout(getActivity());
		 * fl.setLayoutParams(params);
		 * 
		 * final int margin = (int)
		 * TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8,
		 * getResources() .getDisplayMetrics());
		 * 
		 * TextView v = new TextView(getActivity()); params.setMargins(margin,
		 * margin, margin, margin); v.setLayoutParams(params);
		 * v.setLayoutParams(params); v.setGravity(Gravity.CENTER);
		 * v.setBackgroundResource(R.drawable.background_card);
		 * v.setText(getResources().getString(R.string.about)); fl.addView(v);
		 * return fl;
		 */
	}

	private class changeLanguage extends AsyncTask<String, Void, Integer> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}

		@Override
		protected Integer doInBackground(String... params) {
			changeLanguageSync();
			return 1;
		}

		@Override
		protected void onPostExecute(Integer result) {
			super.onPostExecute(result);

			// ((ActivityMain) getActivity()).closeProgressDialog();
			if (languageResponse == null) {

				if (progressDialog.isShowing()) {
					progressDialog.dismiss();
				}

			} else {

				if (progressDialog.isShowing()) {
					progressDialog.dismiss();
				}

				getActivity()
				.getSharedPreferences("app_settings",
						Context.MODE_PRIVATE).edit()
						.putString("lang", tempLang).commit();

				try {
					ViewProcessor.process(getActivity(),
							(ViewGroup) getActivity().getWindow()
							.getDecorView().getRootView(),
							getLanguageFileName());
				} catch (Exception ex) {
					ex.printStackTrace();
				}

				Intent intent = new Intent(getActivity(), ActivityMain.class);
				intent.putExtra("ChangeLanguage", true);
				startActivity(intent);

			}
		}

	}

	private void changeLanguageSync() {

		String passLang = "";
		if (tempLang.equalsIgnoreCase("English")) {
			passLang = "";
		} else {
			passLang = "he";
		}

		// log("Token Key", getActivity().getSharedPreferences("app_settings",
		// Context.MODE_PRIVATE).getString("TOKEN_KEY", null));
		String TokenValue = getActivity().getSharedPreferences("app_settings",
				Context.MODE_PRIVATE).getString("TOKEN_KEY", null);

		languageResponse = Request.updateLanguage(getActivity(), passLang);
	}

	/* invitation panel logic */

	private UiLifecycleHelper uiHelper;
	private Session.StatusCallback callback = new Session.StatusCallback() {
		@Override
		public void call(Session session, SessionState state,
				Exception exception) {
		}
	};

	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		uiHelper = new UiLifecycleHelper(getActivity(), callback);
		uiHelper.onCreate(savedInstanceState);
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.vg_fb) {
			post();

			// Intent i = new Intent(this, FacebookInviteActivity.class);
			// i.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
			// startActivity(i);
		} else if (v.getId() == R.id.vg_phone) {
			Intent i = new Intent(getActivity(), PhoneFriendListActivity.class);
			i.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
			startActivity(i);
		/*} else if (v.getId() == R.id.vg_plus) {
			plus();*/
		} else if (v.getId() == R.id.vg_gmail) {
			Intent i = new Intent(getActivity(), DeviceEmailListActivity.class);
			i.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
			startActivity(i);
		}
	}

	private void plus() {
		// Launch the Google+ share dialog with attribution to your app.
//		Intent shareIntent = new PlusShare.Builder(getActivity())
//		.setType("text/plain")
//		.setText("iOr")
//		.setContentUrl(
//				Uri.parse("https://play.google.com/store/apps/details?id="
//						+ getActivity().getPackageName())).getIntent();
//
//		startActivityForResult(shareIntent, 0);
		

		new AsyncTask<String, String, String>(){
			String invitetext;

			protected void onPreExecute() {

			}

			@Override
			protected String doInBackground(String... params) {
				if(!isCancelled()){
					invitetext = Request
							.getInviteText(getActivity());
				}
				return null;
			}

			protected void onPostExecute(String result) {
				if(!isCancelled()){
					Intent shareIntent = new PlusShare.Builder(getActivity())
					.setType("text/plain")
					.setText(invitetext).getIntent();
					startActivityForResult(shareIntent, 0);
				}
			}
		}.execute("");

	}

	private void post() {
		new AsyncTask<String, String, String>(){
			String invitetext;

			protected void onPreExecute() {
				if (FacebookDialog.canPresentShareDialog(getActivity(), FacebookDialog.ShareDialogFeature.SHARE_DIALOG)==false) {
					cancel(true);
					Toast.makeText(getActivity(), "You do not have Facebook app installed on your device.", Toast.LENGTH_SHORT).show();
				}
			}

			@Override
			protected String doInBackground(String... params) {
				if(!isCancelled()){
					invitetext = Request
							.getInviteText(getActivity());
				}
				return null;
			}

			protected void onPostExecute(String result) {
				if(!isCancelled()){
					ShareDialogBuilder builder = new ShareDialogBuilder(getActivity());
					builder.setApplicationName("iOr");
					builder.setLink("https://play.google.com/store/apps/details?id="
							+ getActivity().getPackageName());
					builder.setDescription(invitetext +"");
					FacebookDialog dialog = builder.build();

					uiHelper.trackPendingDialogCall(dialog.present());
				}
			}
		}.execute("");

	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		uiHelper.onActivityResult(requestCode, resultCode, data,
				new FacebookDialog.Callback() {
			@Override
			public void onError(FacebookDialog.PendingCall pendingCall,
					Exception error, Bundle data) {
				Log.e("Activity",
						String.format("Error: %s", error.toString()));
			}

			@Override
			public void onComplete(
					FacebookDialog.PendingCall pendingCall, Bundle data) {
				Log.i("Activity", "Success!");
			}
		});
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		uiHelper.onSaveInstanceState(outState);
	}

	@Override
	public void onResume() {
		super.onResume();
		uiHelper.onResume();
	}

	@Override
	public void onPause() {
		super.onPause();
		uiHelper.onPause();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		uiHelper.onDestroy();
	}
}
