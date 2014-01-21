package com.ior.charityapp.invite;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;

import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.widget.FacebookDialog;
import com.facebook.widget.FacebookDialog.ShareDialogBuilder;
import com.google.android.gms.plus.PlusShare;
import com.ior.charityappior.DialogActivity;
import com.ior.charityappior.R;

public class InviteActivity extends DialogActivity implements OnClickListener {

	private UiLifecycleHelper uiHelper;
	private Session.StatusCallback callback = new Session.StatusCallback() {
		@Override
		public void call(Session session, SessionState state,
				Exception exception) {
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_invite_friends);

		uiHelper = new UiLifecycleHelper(this, callback);
		uiHelper.onCreate(savedInstanceState);

		View fb = findViewById(R.id.vg_fb);
		View phone = findViewById(R.id.vg_phone);
		//View plus = findViewById(R.id.vg_plus);
		View gmail = findViewById(R.id.vg_gmail);

		fb.setOnClickListener(this);
		phone.setOnClickListener(this);
		//plus.setOnClickListener(this);
		gmail.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.vg_fb) {
			post();

			// Intent i = new Intent(this, FacebookInviteActivity.class);
			// i.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
			// startActivity(i);
		} else if (v.getId() == R.id.vg_phone) {
			Intent i = new Intent(this, PhoneFriendListActivity.class);
			i.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
			startActivity(i);
		/*} else if (v.getId() == R.id.vg_plus) {
			plus();*/
		} else if (v.getId() == R.id.vg_gmail) {
		}
	}

	private void plus() {
		// Launch the Google+ share dialog with attribution to your app.
		Intent shareIntent = new PlusShare.Builder(this)
				.setType("text/plain")
				.setText("iOr")
				.setContentUrl(
						Uri.parse("https://play.google.com/store/apps/details?id="
								+ getPackageName())).getIntent();

		startActivityForResult(shareIntent, 0);
	}

	private void post() {
		ShareDialogBuilder builder = new ShareDialogBuilder(this);
		builder.setApplicationName("iOr");
		builder.setLink("https://play.google.com/store/apps/details?id="
				+ getPackageName());
		FacebookDialog dialog = builder.build();

		uiHelper.trackPendingDialogCall(dialog.present());
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
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
	protected void onResume() {
		super.onResume();
		uiHelper.onResume();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		uiHelper.onSaveInstanceState(outState);
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
