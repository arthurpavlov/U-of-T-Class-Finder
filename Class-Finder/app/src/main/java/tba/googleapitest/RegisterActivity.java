package tba.googleapitest;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import static android.content.Intent.EXTRA_EMAIL;

public class RegisterActivity extends AppCompatActivity {
  private String password;

  /**
   * Create the instance for the activity for viewing flights.
   *
   * @param savedInstanceState The bundle for the instance
   */
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_register);

    // Receive the message and add the text in the email field to the email field in this activity
    Intent intent = getIntent();
    String message = intent.getStringExtra(EXTRA_EMAIL);
    EditText email = (EditText) findViewById(R.id.activity_register_et_email);
    email.setText(message);
  }

  /**
   * Login to the dashboard to gain access to the main functionalities of the application
   *
   * @param view The instance of the widget that was clicked.
   */
  public void loginToDashboard(View view) {
    // Check if pass and confirm pass match
    if (!checkPasswordMatch()) {
      return;
    }
    // Get the strings from all inputs available
    String email = ((EditText) findViewById(R.id.activity_register_et_email)).getText().toString();
    String first =
        ((EditText) findViewById(R.id.activity_register_et_first_name)).getText().toString();
    String last =
        ((EditText) findViewById(R.id.activity_register_et_last_name)).getText().toString();
    String address =
        ((EditText) findViewById(R.id.activity_register_et_address)).getText().toString();

    password = ((EditText) findViewById(R.id.activity_register_et_password)).getText().toString();


  }

  /**
   * Check if the password and confirm passwords match.
   *
   * @return If the password and confirm password match or not
   */
  public boolean checkPasswordMatch() {
    EditText password = (EditText) findViewById(R.id.activity_register_et_password);
    EditText conPass = (EditText) findViewById(R.id.activity_register_et_password_confirm);

    String pass1 = password.getText().toString();
    String pass2 = conPass.getText().toString();

    if (!pass1.equals(pass2)) {
      conPass.setTextColor(Color.RED);
      Toast.makeText(getApplicationContext(),
          "The entered password and confirm password do not match", Toast.LENGTH_SHORT).show();
      return false;
    } else {
      conPass.setTextColor(Color.BLACK);
      return true;
    }
  }
}
