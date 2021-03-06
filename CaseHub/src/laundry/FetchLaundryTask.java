package laundry;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import laundry.LaundryFragment.LaundryCallback;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.casehub.R;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.util.Log;

/**
 * Fetches laundry information from Case eSuds.
 */
public class FetchLaundryTask extends AsyncTask<String, Void, ArrayList<LaundryMachine>> {
	
	Context context;
	LaundryCallback callback;
	DefaultHttpClient client;
	ProgressDialog dialog;
	
	List<Exception> exceptions = new ArrayList<Exception>();
	
	boolean hasDialog;
	
	private int houseId;
	
	private static final String ESUDS_STATUS_URL = "http://case-asi.esuds.net/RoomStatus/machineStatus.i?bottomLocationId=";
	private static final String STATUS_ROW_SELECTOR = ".room_status tr";
	private static final int MACHINE_NUM_INDEX = 1;
	private static final int MACHINE_TYPE_INDEX = 2;
	private static final int MACHINE_STATUS_INDEX = 3;
	private static final int MACHINE_MIN_INDEX = 4;
	
	/**
	 * Task has progress dialog by default, but it can be removed to run in background.
	 */
	public FetchLaundryTask(Context context, LaundryCallback callback, int houseId) {
		this(context, callback, houseId, true);
	}
	
	public FetchLaundryTask(Context context, LaundryCallback callback, int houseId, boolean hasDialog) {
		this.context = context;
		this.callback = callback;
		this.hasDialog = hasDialog;
		
		/*
		 * The house IDs used in the eSuds Ajax calls are exactly one greater
		 * than the IDs used in ShowRoomStatus.i
		 * 
		 * EXCEPT for 1401 (Hitchcock), which has a call to ID 1403 for some reason.
		 */
		if (houseId == 1401)
			this.houseId = houseId + 2;
		else {
			this.houseId = houseId + 1;
		}
	}
	
	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		
		if (hasDialog) {
			dialog = new ProgressDialog(context);
			dialog.setMessage("Fetching washer/dryer times...");
			dialog.show();
		}
	}

	@Override
	protected ArrayList<LaundryMachine> doInBackground(String... params) {
		
		client = new DefaultHttpClient();
		ArrayList<LaundryMachine> machines = new ArrayList<LaundryMachine>();

		try {
			String html = getLaundryTimes();
			machines = parseLaundryTimes(html);
		} catch (ParseException e) {
			exceptions.add(e);
		} catch (IOException e) {
			exceptions.add(e);
		}
				
		return machines;
	
	}
	
	@Override
	protected void onPostExecute(ArrayList<LaundryMachine> machines) {
		super.onPostExecute(machines);
        callback.onTaskDone(machines);   
        
        if (hasDialog) {
        	dialog.dismiss();
        }
        
        for (Exception e : exceptions) {
        	Log.e("CASEHUB", "exception", e);
        }
        
        if (!exceptions.isEmpty()) {
        	AlertDialog.Builder builder = new AlertDialog.Builder(context);
        	builder.setTitle("Error")
            	.setMessage("Failed to fetch laundry times. Check your internet connection.")
            	.setPositiveButton(R.string.okay, new DialogInterface.OnClickListener() {
			           public void onClick(DialogInterface dialog, int id) {
			               // User clicked OK button
			           }
			       });
        	AlertDialog dialog = builder.create();
        	dialog.show();
        }
	}
		
	/*
	 * Retrieves laundry times for a specific house
	 */
	private String getLaundryTimes() throws ParseException, IOException {
		
		BasicCookieStore cookieStore = new BasicCookieStore();
	    client.setCookieStore(cookieStore);

	    // GET page	    
		HttpGet get = new HttpGet(ESUDS_STATUS_URL + houseId);
		HttpResponse loginGetResult = client.execute(get);
		HttpEntity entity = loginGetResult.getEntity();
		
		return EntityUtils.toString(entity, "UTF-8");
	}
	
	private ArrayList<LaundryMachine> parseLaundryTimes(String html) {
				
		ArrayList<LaundryMachine> machines = new ArrayList<LaundryMachine>();
		Document doc = Jsoup.parse(html);
				
		// Get table
		Elements statusTableRows = doc.select(STATUS_ROW_SELECTOR);
				
		// Extract useful rows
		int machineNumber;
		int minutesLeft;
		String type;
		String status;
		for (Element row : statusTableRows) {
			
			Elements columns = row.select("td");
			if (columns.size() < 4) {
				continue;
			}
			
			machineNumber = Integer.parseInt(columns.get(MACHINE_NUM_INDEX).text());
			type = columns.get(MACHINE_TYPE_INDEX).text();
			status = columns.get(MACHINE_STATUS_INDEX).text();
			
			String minuteString = columns.get(MACHINE_MIN_INDEX).text();
			minuteString = minuteString.replaceAll("\u00A0", ""); // get rid of pesky &nbsp

			if (minuteString != null && minuteString.trim().length() > 0) {
				minutesLeft = Integer.parseInt(minuteString);
			} else {
				minutesLeft = -1;
			}
				
			LaundryMachine machine = new LaundryMachine(machineNumber, minutesLeft, type, status);
			machines.add(machine);
		}
		
		return machines;
	}
	
}