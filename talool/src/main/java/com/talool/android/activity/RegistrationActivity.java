package com.talool.android.activity;

import android.app.DialogFragment;
import android.content.Intent;
import android.graphics.drawable.ClipDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.MapBuilder;
import com.talool.android.MainActivity;
import com.talool.android.R;
import com.talool.android.dialog.DialogFactory;
import com.talool.android.dialog.TaloolDatePicker;
import com.talool.android.tasks.RegistrationTask;
import com.talool.android.util.TaloolUser;
import com.talool.api.thrift.CTokenAccess_t;
import com.talool.api.thrift.Customer_t;
import com.talool.api.thrift.ServiceException_t;
import com.talool.api.thrift.Sex_t;

import org.apache.thrift.TException;

import java.util.Calendar;
import java.util.Date;

public class RegistrationActivity extends TaloolActivity
{
	private EditText firstName;
	private EditText lastName;
	private EditText email;
	private EditText password;
    private EditText bd_fld;
    private RadioGroup radio_sex;
    private boolean isFemale = true;
    private Date bday;

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        if ((keyCode == KeyEvent.KEYCODE_BACK))
        {
            Log.d(this.getClass().getName(), "back button pressed");
            final Intent intent = new Intent(getApplicationContext(), WelcomeActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            startActivity(intent);
            finish();
            return true;
        }
        else
        {
            return super.onKeyDown(keyCode, event);
        }
    }

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.registration_layout);

		firstName = (EditText) findViewById(R.id.firstName);
		lastName = (EditText) findViewById(R.id.lastName);
		email = (EditText) findViewById(R.id.registrationEmail);
		password = (EditText) findViewById(R.id.registrationPassword);
        bd_fld = (EditText) findViewById(R.id.registrationBDay);
        radio_sex = (RadioGroup) findViewById(R.id.radio_sex);


		ClipDrawable firstName_bg = (ClipDrawable) firstName.getBackground();
		firstName_bg.setLevel(1500);
		ClipDrawable lastName_bg = (ClipDrawable) lastName.getBackground();
		lastName_bg.setLevel(1500);
		ClipDrawable username_bg = (ClipDrawable) email.getBackground();
		username_bg.setLevel(1500);
		ClipDrawable password_bg = (ClipDrawable) password.getBackground();
		password_bg.setLevel(1500);
        ClipDrawable bd_fld_bg = (ClipDrawable) bd_fld.getBackground();
        bd_fld_bg.setLevel(1500);
        ClipDrawable radio_sex_bg = (ClipDrawable) radio_sex.getBackground();
        radio_sex_bg.setLevel(1500);

        password.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                boolean handled = false;
                if (actionId == EditorInfo.IME_ACTION_NEXT) {
                    onBdayClick(v);
                    handled = true;
                }
                return handled;
            }
        });

	}

    public void onSexClicked(View view) {
        // Is the button now checked?
        boolean checked = ((RadioButton) view).isChecked();

        // Check which radio button was clicked
        switch(view.getId()) {
            case R.id.radio_male:
                if (checked)
                    isFemale = false;
                    break;
            case R.id.radio_female:
                if (checked)
                    isFemale = true;
                    break;
        }
    }

	public void onRegistrationClick(View view)
	{
		if (TextUtils.isEmpty(firstName.getText().toString()) ||
				TextUtils.isEmpty(lastName.getText().toString()) ||
				TextUtils.isEmpty(email.getText().toString()) ||
				TextUtils.isEmpty(password.getText().toString())||
                bday == null)
		{
			popupErrorMessage("All fields in the registration page are required");
		}
		else
		{
            Customer_t customer = new Customer_t(firstName.getText().toString(),
                    lastName.getText().toString(), email.getText().toString());
            customer.setBirthDate(bday.getTime());
            Sex_t sex = (isFemale)? Sex_t.F:Sex_t.M;
            customer.setSex(sex);

			RegistrationTask registerTask = new RegistrationTask(customer,
                    password.getText().toString(), this, client) {

                @Override
                protected void onPreExecute()
                {
                    df = DialogFactory.getProgressDialog();
                    df.show(getFragmentManager(), "dialog");
                }

                @Override
                protected void onPostExecute(CTokenAccess_t result)
                {
                    if (df != null && !df.isHidden())
                    {
                        df.dismiss();
                    }
                    if (errorMessage != null)
                    {
                        popupErrorMessage(errorMessage);
                    }
                    else
                    {
                        TaloolUser.get().setAccessToken(result);
                        Intent myDealsIntent = new Intent(getApplicationContext(), MainActivity.class);
                        myDealsIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(myDealsIntent);
                    }
                }
            };
			registerTask.execute(new String[] {});
		}

	}

    public void onBdayClick(View view)
    {
        TaloolDatePicker newFragment = new TaloolDatePicker("Birthdate"){
            @Override
            public void onDateSet(DatePicker view, int year, int month, int day) {
                Calendar c = Calendar.getInstance();
                c.set(Calendar.YEAR, year);
                c.set(Calendar.MONTH, month);
                c.set(Calendar.DATE, day);
                bday = c.getTime();

                String d = DateFormat.getDateFormat(RegistrationActivity.this).format(bday);
                bd_fld.setText(d);
            }
        };
        newFragment.show(getFragmentManager(), "datePicker");
    }

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		// getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	public void popupErrorMessage(String message)
	{
		EasyTracker easyTracker = EasyTracker.getInstance(this);

		easyTracker.send(MapBuilder
				.createException(message, true)
				.build()
				);

		if (df != null && !df.isHidden())
		{
			df.dismiss();
		}
		String title = getResources().getString(R.string.error_reg);
		String label = getResources().getString(R.string.retry);
		df = DialogFactory.getAlertDialog(title, message, label);
		df.show(getFragmentManager(), "dialog");
	}

	@Override
	public void onStart()
	{
		super.onStart();
		EasyTracker.getInstance(this).activityStart(this); // Add this method.
	}

	@Override
	public void onStop()
	{
		super.onStop();
		EasyTracker.getInstance(this).activityStop(this); // Add this method.
	}
}
