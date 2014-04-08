package com.ior.charityappior;

import static com.ior.charityappior.Utils.log;

import java.util.ArrayList;
import java.util.Locale;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.countrypicker.CountryPicker;
import com.countrypicker.CountryPickerListener;
import com.ior.charityapp.lang.ViewProcessor;
import com.ior.charityapp.models.Category;
import com.ior.charityappior.R;

/**
 * Created by android-dev on 03.09.13.
 */
public class ActivityProfile extends DialogActivity {
	private static final String PREFS_NAME = "default prefs";
	private static final String SP_KEY_PHONE = "phone";
	private static final String SP_KEY_CODE = "code";
	private static final String SP_KEY_COUNTRY = "country";
	private static final String SP_KEY_NAME = "sponsor name";
	String availableStatus ;
	int availableValue ;
	ArrayList<Category> mCategories = new ArrayList<Category>();
	ArrayList<String> mCategoryNames = new ArrayList<String>();
	ArrayList<Integer> mCategoryIds = new ArrayList<Integer>();

	TextView tvCode;
	EditText etCountry;
	EditText etName;
	TextView btSetSponsor, btSetSponsorHe;
	Button btSend, btSendHe;
	ListView mLvCategories;
	private AlertDialog mSetSponsorDialog, mSetUpdateProfileDialog;
	
	EditText firstNameTxt, lastNameTxt, emailTxt, occupationTxt, addressTxt, 
	facebookIdTxt, twitterIdTxt, sponsorIdTxt, phoneNumber ;
	Button btAvailability ;
	LinearLayout IWantHelpEngBtn, IWantHelpHeBtn;
	SharedPreferences prefs;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_profile);

		// show title
		setTitle(stringPicker.getString("mlt_title_profile"));

		LinearLayout IWantHelpEngBtn = (LinearLayout) findViewById(R.id.IWantHelpEngVersion);
		LinearLayout IWantHelpHeBtn = (LinearLayout) findViewById(R.id.IWantHelpHeVersion);
		
		prefs = getSharedPreferences("app_settings", MODE_PRIVATE);
		
		if(prefs.getString("lang", "").equalsIgnoreCase("English")) {
			IWantHelpEngBtn.setVisibility(View.VISIBLE);
			IWantHelpHeBtn.setVisibility(View.GONE);
		}else {
			IWantHelpEngBtn.setVisibility(View.GONE);
			IWantHelpHeBtn.setVisibility(View.VISIBLE);
		}
		
		showProgressDialog();
		new AsyncTask<Void, Void, ArrayList<Category>>() {

			@Override
			protected ArrayList<Category> doInBackground(Void... params) {
				mCategoryIds = Request.getProfile(ActivityProfile.this);
				return mCategories = Request
						.getCategories(ActivityProfile.this);
			}

			@Override
			protected void onPostExecute(ArrayList<Category> result) {
				closeProgressDialog();
				if (mCategories == null) {
					showErrorToast();
				} else {
					setupViews();
				}
			}
		}.execute();
	}

	private void setupViews() {
		if(prefs.getString("lang", "").equalsIgnoreCase("English")) {
			btSetSponsor = (TextView) findViewById(R.id.btSetSponsor);
		}else{
			btSetSponsor = (TextView) findViewById(R.id.btSetSponsorHe);
		}
		
		SpannableString content = new SpannableString(stringPicker.getString("set_sponsor"));
		content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
		btSetSponsor.setText(content);
		btSetSponsor.setOnClickListener(new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			showSetSponsorDialog();
		}
		});
		
		for (Category category : mCategories) {
			mCategoryNames.add(category.mName);
			availableStatus = category.available ;
		}
		
		btAvailability = (Button) findViewById(R.id.btAvailability) ;
		
		if(availableStatus.equals("1")) {
			btAvailability.setText(stringPicker.getString("helper_notavailable"));
			availableValue = 0 ;
		}else if(availableStatus.equals("0")){
			btAvailability.setText(stringPicker.getString("helper_available"));
			availableValue = 1 ;
		}else{
			btAvailability.setText(stringPicker.getString("helper_notavailable"));
			availableValue = 0 ;
		}
		
		btAvailability.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				showProgressDialog();
				new AsyncTask<Void, Void, String>() {

					@Override
					protected String doInBackground(Void... params) {
						return Request.setAvailability(ActivityProfile.this, availableValue);
					}

					@Override
					protected void onPostExecute(String result) {
						closeProgressDialog();
						if (result == null) {
							showErrorToast();
						} else {
							//showSuccessDialog();
							//showUpdateProfileDialog() ;
							if(availableValue == 0) {
								btAvailability.setText(stringPicker.getString("helper_available"));
								availableValue = 1 ;
							}else{
								btAvailability.setText(stringPicker.getString("helper_notavailable"));
								availableValue = 0 ;
							}
						}
					}
				}.execute();
			}
		}) ;
		
		if(prefs.getString("lang", "").equalsIgnoreCase("English")) {
			btSend = (Button) findViewById(R.id.btSendEn);
		}else{
			btSend = (Button) findViewById(R.id.btSendHe);
		}
		btSend.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				showProgressDialog();
				new AsyncTask<Void, Void, String>() {

					@Override
					protected String doInBackground(Void... params) {
						ArrayList<Integer> checkedItems = getCheckedItems();
						SharedPreferences prefs = getSharedPreferences(
								PREFS_NAME, MODE_PRIVATE);

						String country = prefs.getString(SP_KEY_COUNTRY, "");
						if (!country.isEmpty()) {
							String code = prefs.getString(SP_KEY_CODE, "");
							String phone = prefs.getString(SP_KEY_PHONE, "");
							String name = prefs.getString(SP_KEY_NAME, "");
							String fullPhone;
							if (phone.isEmpty()) {
								fullPhone = "";
							} else {
								fullPhone = code.replace("+", "") + phone;
							}
							if (Request.changeSponsor(ActivityProfile.this,
									name, fullPhone) == null)
								return null;
						}
						return Request.updateProfile(ActivityProfile.this,
								checkedItems);
					}

					@Override
					protected void onPostExecute(String result) {
						closeProgressDialog();
						if (result == null) {
							showErrorToast();
						} else {
							//showSuccessDialog();
							showUpdateProfileDialog() ;
						}
					}
				}.execute();
			}
		});
		
		mLvCategories = (ListView) findViewById(R.id.lv_categories);
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_multiple_choice,
				mCategoryNames) {

			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				View view = super.getView(position, convertView, parent);
				view.setBackgroundResource(R.drawable.item_background_selector);
				return view;
			}
		};
		mLvCategories.setAdapter(adapter);
		if (mCategoryIds != null) {
			for (int i = 0; i < mCategories.size(); i++) {
				mLvCategories.setItemChecked(i,
						mCategoryIds.contains(mCategories.get(i).mCategoryId));
			}
		}
	}

	private void showUpdateProfileDialog() {
		AlertDialog.Builder builder;

		LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
		/*View layout = inflater.inflate(R.layout.update_profile,
				(ViewGroup) findViewById(R.id.layout_root));*/
		View layout = inflater.inflate(R.layout.update_profile, null);
		try {
			ViewProcessor.process(this, (ViewGroup) layout,
					getLanguageFileName());
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		builder = new AlertDialog.Builder(this);
		builder.setView(layout);
		builder.setCancelable(true);

		phoneNumber = (EditText) layout.findViewById(R.id.phoneNumber);
		firstNameTxt = (EditText) layout.findViewById(R.id.firstName);
		lastNameTxt = (EditText) layout.findViewById(R.id.lastName);
		emailTxt = (EditText) layout.findViewById(R.id.emailTxt);
		occupationTxt = (EditText) layout.findViewById(R.id.occupationTxt);
		addressTxt = (EditText) layout.findViewById(R.id.addressTxt);
		facebookIdTxt = (EditText) layout.findViewById(R.id.facebookIdTxt);
		twitterIdTxt = (EditText) layout.findViewById(R.id.twitterIdTxt);
		sponsorIdTxt = (EditText) layout.findViewById(R.id.sponsorIdTxt);
		
		tvCode = (TextView) layout.findViewById(R.id.tvCountryCode);
		etCountry = (EditText) layout.findViewById(R.id.etCountry);

		etCountry.setOnFocusChangeListener(new View.OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (hasFocus) {
					showCountryPicker();
				}
			}
		});
		etCountry.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				showCountryPicker();
			}
		});

		SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
		String country = prefs.getString(SP_KEY_COUNTRY, "");
		String code = prefs.getString(SP_KEY_CODE, "");
		String phone = prefs.getString(SP_KEY_PHONE, "");
		String name = prefs.getString(SP_KEY_NAME, "");

		if (!prefs.getString(SP_KEY_COUNTRY, "").isEmpty()) {
			etCountry.setText(country);
			tvCode.setText(code);
			//etName.setText(name);
			//etSponsorPhone.setText(phone);
		} else {
			TelephonyManager manager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
			// getNetworkCountryIso

			String countryID = manager.getSimCountryIso().toUpperCase();
			Locale locale = new Locale("en", countryID);

			etCountry.setText(locale.getDisplayCountry(new Locale("en", "US")));

			setCountry(countryID);
		}

		Button btNegative = (Button) layout.findViewById(R.id.btUpdateProfileCancel);
		// if button is clicked, close the custom dialog
		btNegative.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mSetUpdateProfileDialog.dismiss();
				log("XXX", "=="+"onClick cancel");
			}
		});
		Button btPositive = (Button) layout.findViewById(R.id.btUpdateProfileSet);
		// if button is clicked, close the custom dialog
		btPositive.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				//String sponsorPhone = etSponsorPhone.getText().toString();
				final String sponsorCode = tvCode.getText().toString();
				final String country = etCountry.getText().toString();
				
				if (firstNameTxt.getText().toString().isEmpty()) {
					ActivityProfile.this.showEmptyValueToast();
				} else {
					RegisterTask task = new RegisterTask();
					task.execute(firstNameTxt.getText().toString(), lastNameTxt.getText().toString(), emailTxt.getText().toString(), sponsorCode + phoneNumber.getText().toString(), occupationTxt.getText().toString(), addressTxt.getText().toString(), facebookIdTxt.getText().toString(), twitterIdTxt.getText().toString(), sponsorIdTxt.getText().toString());
					
					
//					//saveSponsorPhone(sponsorPhone, sponsorCode, country, sponsorName);
//					Thread th = new Thread() {
//						@Override
//						public void run() {
//							try {
//								Request.HelperRegistration(ActivityProfile.this, firstNameTxt.getText().toString(), lastNameTxt.getText().toString(), emailTxt.getText().toString(), sponsorCode + phoneNumber.getText().toString(), occupationTxt.getText().toString(), addressTxt.getText().toString(), facebookIdTxt.getText().toString(), twitterIdTxt.getText().toString(), sponsorIdTxt.getText().toString());
//							} catch(Exception ex) {
//								ex.printStackTrace();
//							}
//						}
//					};
//					th.start();
//					try {
//						th.join();
//					} catch (InterruptedException e) {
//						e.printStackTrace();
//					}
//					closeProgressDialog() ;
//					//Request.login(ActivityProfile.this, sponsorCode , phoneNumber.getText().toString()) ;
//					mSetUpdateProfileDialog.dismiss();
				}
				log("XXX", "onClick send");
			}
		}); 

		builder.create();
		mSetUpdateProfileDialog = builder.show();
	}
	
	public class RegisterTask extends AsyncTask<String, Object, Object>  {
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			// display progress dialog
			showProgressDialog(); 
		}
		
		@Override
		protected void onPostExecute(Object result) {
			super.onPostExecute(result);
			// dismiss progress dialog
			closeProgressDialog() ;
			mSetUpdateProfileDialog.dismiss();
		}

		@Override
		protected Object doInBackground(String... params) {
			try {
				Request.HelperRegistration(ActivityProfile.this, 
						params[0], 
						params[1],
						params[2],
						params[3],
						params[4],
						params[5],
						params[6],
						params[7],
						params[8]);
			} catch(Exception ex) {
				ex.printStackTrace();
			}
			return null;
		}
		
	}
	
	
	
	private void showSetSponsorDialog() {
		AlertDialog.Builder builder;

		LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
		View layout = inflater.inflate(R.layout.dialog_set_sponsor,
				(ViewGroup) findViewById(R.id.layout_root));
		try {
			ViewProcessor.process(this, (ViewGroup) layout,
					getLanguageFileName());
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		builder = new AlertDialog.Builder(this);
		builder.setView(layout);
		builder.setCancelable(true);

		final EditText etSponsorPhone = (EditText) layout
				.findViewById(R.id.etPhone);
		etName = (EditText) layout.findViewById(R.id.etName);
		tvCode = (TextView) layout.findViewById(R.id.tvCountryCode);
		etCountry = (EditText) layout.findViewById(R.id.etCountry);

		etCountry.setOnFocusChangeListener(new View.OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (hasFocus) {
					showCountryPicker();
				}
			}
		});
		etCountry.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				showCountryPicker();
			}
		});

		SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
		String country = prefs.getString(SP_KEY_COUNTRY, "");
		String code = prefs.getString(SP_KEY_CODE, "");
		String phone = prefs.getString(SP_KEY_PHONE, "");
		String name = prefs.getString(SP_KEY_NAME, "");

		if (!prefs.getString(SP_KEY_COUNTRY, "").isEmpty()) {
			etCountry.setText(country);
			tvCode.setText(code);
			etName.setText(name);
			etSponsorPhone.setText(phone);
		} else {
			TelephonyManager manager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
			// getNetworkCountryIso

			String countryID = manager.getSimCountryIso().toUpperCase();
			Locale locale = new Locale("en", countryID);

			etCountry.setText(locale.getDisplayCountry(new Locale("en", "US")));

			setCountry(countryID);
		}

		Button btNegative = (Button) layout.findViewById(R.id.btCancel);
		// if button is clicked, close the custom dialog
		btNegative.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mSetSponsorDialog.dismiss();
				log("XXX", "onClick cancel");
			}
		});
		Button btPositive = (Button) layout.findViewById(R.id.btSet);
		// if button is clicked, close the custom dialog
		btPositive.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				String sponsorPhone = etSponsorPhone.getText().toString();
				String sponsorCode = tvCode.getText().toString();
				String country = etCountry.getText().toString();
				String sponsorName = etName.getText().toString();

				if (sponsorName.isEmpty()) {
					ActivityProfile.this.showEmptyValueToast();
				} else {
					saveSponsorPhone(sponsorPhone, sponsorCode, country,
							sponsorName);
					mSetSponsorDialog.dismiss();
				}
				log("XXX", "onClick send");
			}
		});

		builder.create();
		mSetSponsorDialog = builder.show();
	}

	private void setCountry(String countryID) {
		log("setCountry", "countryID " + countryID);
		String[] countries = getResources()
				.getStringArray(R.array.CountryCodes);
		for (int i = 0; i < countries.length; i++) {
			String[] country = countries[i].split(",");
			if (country[1].trim().equals(countryID.trim())) {
				tvCode.setText("+" + country[0]);
				etCountry.setText(new Locale("en", countryID)
						.getDisplayCountry(new Locale("en", "US")));
				break;
			}
		}
	}

	private void showCountryPicker() {
		final CountryPicker picker = new CountryPicker();
		picker.setListener(new CountryPickerListener() {

			@Override
			public void onSelectCountry(String name, String code) {
				setCountry(code);
				picker.dismiss();
			}
		});
		picker.show(getSupportFragmentManager(),
				stringPicker.getString("mlt_country_picker"));
	}

	private void saveSponsorPhone(String sponsorPhone, String sponsorCode,
			String country, String name) {
		SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
		prefs.edit().putString(SP_KEY_PHONE, sponsorPhone).commit();
		prefs.edit().putString(SP_KEY_CODE, sponsorCode).commit();
		prefs.edit().putString(SP_KEY_COUNTRY, country).commit();
		prefs.edit().putString(SP_KEY_NAME, name).commit();
	}

	private void showSuccessDialog() {
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
		alertDialogBuilder.setMessage(
				stringPicker.getString("request_accepted")).setCancelable(true);
		AlertDialog alertDialog = alertDialogBuilder.create();
		alertDialog.show();
	}

	public ArrayList<Integer> getCheckedItems() {
		SparseBooleanArray checked = mLvCategories.getCheckedItemPositions();
		ArrayList<Integer> result = new ArrayList<Integer>();
		log("cheked", "checked size " + checked.size());
		for (int i = 0; i < checked.size(); i++) {
			log("cheked", i + " " + checked.get(i));
			if (checked.get(i)) {
				result.add(mCategories.get(i).mCategoryId);
				Log.v("categories", "checked " + i + " "
						+ mCategories.get(i).mCategoryId);
			} else {
				// the item is not checked, do something else
			}
		}
		return result;
	}

}