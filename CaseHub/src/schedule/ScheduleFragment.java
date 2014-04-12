package schedule;

import java.security.InvalidParameterException;
import java.util.ArrayList;

import org.joda.time.LocalTime;

import android.app.DialogFragment;
import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.casehub.R;

public class ScheduleFragment extends Fragment {
	
	/**
	 * Sets first and last hours displayed in Schedule view; used to determine
	 * placement of events.
	 * 
	 * To change the first/last hours, both these constants and the layout must
	 * be updated.
	 */
	public static final int FIRST_HOUR = 7;
	public static final int LAST_HOUR = 21;
	
	/**
	 * ActionBar item IDs
	 */
	public static final int REFRESH_ID = 1;
	public static final int SILENT_ID = 2;
	
	/**
	 * Preferences filename to track whether user has logged in
	 */
	public static final String LOGIN_PREF = "LoginPrefsFile";
	public static final String LOGGED_IN = "hasLoggedIn";
	
	private ScheduleDBHelper dbHelper;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_schedule, container, false);
	}
	
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		
		dbHelper = new ScheduleDBHelper();
				
		// Check if user has logged in previously
		SharedPreferences settings = getActivity().getSharedPreferences(LOGIN_PREF, 0);
		boolean hasLoggedIn = settings.getBoolean(LOGGED_IN, false);

		if (!hasLoggedIn) {
			
			// Show login dialog
			DialogFragment loginDialog = new LoginDialogFragment();
			loginDialog.show(getFragmentManager(), "login");
			
		} else {
			/* Display schedule from database */
			ArrayList<ScheduleEvent> events = dbHelper.getSchedule();
			
			for (ScheduleEvent event : events) {
				displayEvent(event);
			}
			
		}
		
		placeTimeLine();
		
		super.onViewCreated(view, savedInstanceState);
		
	}
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
	    MenuItem item = menu.add(0, REFRESH_ID, 10, R.string.schedule_refresh);
	    item.setIcon(R.drawable.ic_action_refresh).setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

	    switch (item.getItemId()) {
	        case REFRESH_ID:
	        	// Show login dialog
				DialogFragment loginDialog = new LoginDialogFragment();
				loginDialog.show(getFragmentManager(), "login");
	            return true;
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}

	/**
	 * Places the line indicating the current time.
	 * TODO: Call whenever fragment is opened.
	 */
	public void placeTimeLine() {
		LocalTime now = LocalTime.now();
		int minutes = (now.getHourOfDay() * 60) + now.getMinuteOfHour();
		LinearLayout timeLine = (LinearLayout) getActivity().findViewById(R.id.current_time);
		
		// If current time within schedule hours
		if (FIRST_HOUR*60 < minutes && minutes < LAST_HOUR*60) {
			
			// Set timeline margin
			RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) timeLine.getLayoutParams();
			params.topMargin = dpToPixels(minutes - (FIRST_HOUR*60));
			
		} else {
			// Hide timeline
			timeLine.setVisibility(LinearLayout.GONE);
		}
		
	}
	
	/**
	 * Add events to the database and display in schedule.
	 */
	public void addEvents(ArrayList<ScheduleEvent> events) {
		
		for (ScheduleEvent event : events) {
			dbHelper.addEvent(event);
			displayEvent(event);
		}
		
	}
	
	/**
	 * Deletes all schedule information from the event table
	 */
	public void clearSchedule() {
		dbHelper.clearSchedule();
	}
	
	/*
	 * Displays a schedule event.
	 */
	private void displayEvent(ScheduleEvent event) {
				
		int height = event.getDuration();
		int topMargin = event.getStartMinutes() - (FIRST_HOUR * 60);

		if (height < 1) {
			throw new InvalidParameterException("Error: Event duration must be at least 1 minute.");
		}
		if (topMargin < 0) {
			throw new InvalidParameterException("Error: Events cannot start before hour " + FIRST_HOUR);
		}
		if ((height + topMargin) > LAST_HOUR * 60) {
			throw new InvalidParameterException("Error: Events cannot end after hour " + LAST_HOUR);
		}

		// Grab event layout template
		LayoutInflater inflater = (LayoutInflater) getActivity()
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		LinearLayout layout = (LinearLayout) inflater.inflate(
				R.layout.template_event_layout, null);

		// Set event layout template dimensions
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		params.height = dpToPixels(height - 1);
		params.setMargins(0, dpToPixels(topMargin), 0, 0);
		layout.setLayoutParams(params);

		// For each day of the week this event occurs, add to layout
		int layoutId;
		LinearLayout eventLayout;

		RelativeLayout parentLayout;

		// Clone layout by inflating template
		eventLayout = (LinearLayout) inflater.inflate(R.layout.template_event_layout, null);
		eventLayout.setLayoutParams(params);

		// Set event text values
		TextView name = (TextView) eventLayout.findViewWithTag("name");
		TextView time = (TextView) eventLayout.findViewWithTag("time");
		TextView location = (TextView) eventLayout.findViewWithTag("location");

		name.setText(event.getName());
		time.setText(event.getTimeString());
		location.setText(event.getLocation());

		// Get parent layout
		String day = event.getDay().getString();
		layoutId = getResources().getIdentifier(day, "id", this.getActivity().getPackageName());
		parentLayout = (RelativeLayout) getView().findViewById(layoutId);

		// Add new event layout to parent layout
		parentLayout.addView(eventLayout);
	}

	/*
	 * Converts dp to pixels, as dp cannot be set directly at runtime.
	 * Used for setting layout parameters.
	 */
	private int dpToPixels(int dp) {
		DisplayMetrics metrics = getActivity().getResources().getDisplayMetrics();
		
		float fpixels = metrics.density * dp;
		int pixels = (int) (fpixels + 0.5f);
		
		return pixels;

	}
	
}

