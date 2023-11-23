package form;

import java.time.LocalDateTime;
import java.util.ArrayList;

import org.tinylog.Logger;

import io.javalin.http.Context;
import io.javalin.http.Handler;

public class AppHandler implements Handler {
	private RouteResponse state = RouteResponse.DEFAULT;
	private ArrayList<String> db = new ArrayList<String>();

	public AppHandler(RouteResponse state) {
		this.state = state;
	}

	@Override
	public void handle(Context ctx) throws Exception {

		Logger.info("processing incoming request: " + state);
		if (state.compareTo(RouteResponse.HTML) == 0) {
			db.add(LocalDateTime.now() + "");

			// TODO Ausgabe in HTML verbessern: informativer und visuell angemessener
			// Gestalten
			String res = "";
			for (String s : db) {
				res += (s + " ");
			}

			ctx.html("<h2>Software Engineering 1</h2><hp>Request at " + LocalDateTime.now() + "</p><p>" + res + "</p>");
		} else {

			// ctx.json(new Patient("Mike", "Constance"));
		}
	}

}

class Patient {

	private static int counter = 1;

	public int patientennummer;
	public String firstName;
	public String lastName;
	public int age;
	public String gender;
	public String diagnosis;

	// Konstruktor ohne Patientennummer-Parameter
	public Patient(String firstName, String lastName, int age, String gender, String diagnosis) {
		this.patientennummer = counter++;
		this.firstName = firstName;
		this.lastName = lastName;
		this.age = age;
		this.gender = gender;
		this.diagnosis = diagnosis;
	}

	// Getter für die Patientennummer
	public int getPatientennummer() {
		return patientennummer;
	}

	// Weitere Getter für die anderen Attribute
	public String getFirstName() {
		return firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public int getAge() {
		return age;
	}

	public String getGender() {
		return gender;
	}

	public String getDiagnosis() {
		return diagnosis;
	}
}
