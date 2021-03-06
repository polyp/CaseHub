package schedule.login;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.casehub.R;

public class LoginDialog extends DialogFragment {
	
	View view;
	OnLoginListener callback;
	
	// Username and password fields
	private EditText userText;
	private EditText passText;

	/**
	 * Define callback interface for communicating with MainActivity.
	 * Container Activity must implement this interface.
	 */
    public interface OnLoginListener {
        public void onScheduleLogin(String user, String pass);
    }
    
    /**
     * Makes sure that the container activity has implemented
     * the callback interface. If not, throws an exception.
     */
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        
        try {
            callback = (OnLoginListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnLoginListener");
        }
    }

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
				
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        // Inflate dialog_login view
        LayoutInflater inflater = getActivity().getLayoutInflater();
        view = inflater.inflate(R.layout.dialog_login, null);
        builder.setView(view);
        
        // Find layout elements
        userText = (EditText) view.findViewById(R.id.username);
		passText = (EditText) view.findViewById(R.id.password);
        
        builder.setTitle(R.string.login_prompt)
               .setPositiveButton(R.string.login_button, new DialogInterface.OnClickListener() {
            	   @Override
                   public void onClick(DialogInterface dialog, int id) {
            		   
                	   // Grab username and password
            		   String user = userText.getText().toString();
            		   String pass = passText.getText().toString();
            		               		   
            		   // Send fields to MainActivity through callback
            		   callback.onScheduleLogin(user, pass);

                   }
               })
               .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                       // User cancelled the dialog
                	   LoginDialog.this.getDialog().cancel();
                   }
               });;
        
        // Create the AlertDialog object and return it
        return builder.create();
    }

}

