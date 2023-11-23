package form;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.tinylog.Logger;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.javalin.Javalin;
import io.javalin.http.staticfiles.Location;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class FormHandlerServer {

	public static void main(String[] args) {
		// start your own server
		Javalin app = Javalin.create(config -> {
			config.staticFiles.add("/pub", Location.CLASSPATH);
		}).start(7001); // using port 7001

		final Map<String, Weather> weatherDataMap = new HashMap<>();
		ArrayList<Patient> db = new ArrayList<Patient>();

		// adding rest endpoints to server

		app.post("/patient", ctx -> {

			Patient patient = new Patient(ctx.formParam("firstName"), ctx.formParam("lastName"),
					Integer.parseInt(ctx.formParam("age")),
					ctx.formParam("gender"), ctx.formParam("diagnosis"));

			// Verarbeite die Formulardaten, z.B., speichere sie in einer Datenbank

			ctx.result("Patient erstellt: " + ctx.formParam("firstName") + " " + ctx.formParam("lastName"));

			db.add(patient);

			ObjectMapper objectMapper = new ObjectMapper();
			String patientJson = objectMapper.writeValueAsString(patient);

			ctx.result(patientJson);
		});

		app.get("/patient2", ctx -> {
			List<Patient> result = new ArrayList<>();
			String seachredLastName = ctx.formParam("lastName");

			Logger.info("Hello :" + seachredLastName);
			validateStringLength(seachredLastName);

			for (Patient patient : db) {
				if (patient.getLastName().equalsIgnoreCase(seachredLastName)) {
					result.add(patient);
					Logger.info(patient.getLastName());
				}

			}

			ObjectMapper objectMapper = new ObjectMapper();
			String patientJson = objectMapper.writeValueAsString(result);

			ctx.result(patientJson);
		});

		// Demo Rest Endpoint zur Abfrage des aktuellen Wetters in Konstanz
		app.get("/weather", cts -> {
			OkHttpClient client = new OkHttpClient();

			Request request = new Request.Builder()
					.url("http://api.weatherapi.com/v1/current.json?key=9a9379aa88784ab4b9f140957230811&q=Constance&aqi=no")
					.build();
			Response res = client.newCall(request).execute();

			ResponseBody responseBody = res.body();
			// Ergebnis vom Server von JSON nach Java umwandeln
			ObjectMapper objectMapper = new ObjectMapper();
			objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
			final String s = responseBody.string();
			Logger.info(1 + s);
			// Weather weather = objectMapper.readValue("{\"current\": { \"temp_c\":
			// \"sunny\"}}", Weather.class);

			// ... => Daten werden in eigener Wetter Klasse bereitgestellt
			Weather weather = objectMapper.readValue(s, Weather.class);
			Logger.info(res.code() + ": " + weather.current.temp_c);

			cts.result("" + weather.current.condition.text + " bei " + weather.current.temp_c);

		});

		// Demo Endpoint zur Abfrage des Wetters für beliebige Städte
		app.post("/weather2", cts -> {
			OkHttpClient client = new OkHttpClient();

			String city = cts.formParam("city");

			Request request = new Request.Builder()
					.url("http://api.weatherapi.com/v1/current.json?key=9a9379aa88784ab4b9f140957230811&q=" + city
							+ "&aqi=no")
					.build();
			Response res = client.newCall(request).execute();

			ResponseBody responseBody = res.body();

			ObjectMapper objectMapper = new ObjectMapper();
			objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
			final String s = responseBody.string();
			Logger.info(s);

			Weather weather = objectMapper.readValue(s, Weather.class);
			Logger.info(res.code() + ": " + weather.current.temp_c);

			// Ergebnis vom Service als Plain Text
			cts.result("" + weather.current.condition.text + " bei " + weather.current.temp_c
					+ " bei der Luftfeutchtigkeit : " + weather.current.humidity);
			weatherDataMap.put(city, weather);
		});

		/**
		 * Exception and Error Handling
		 */
		app.exception(Exception.class, (e, ctx) -> {
			Logger.error(e); // Log error to file
			ctx.status(404);
		}).error(404, ctx -> {
			ctx.result("Generic 404 message: " + ctx.url());
		});

		app.get("/getall", cts -> {
			cts.result(db.toString());
		});
	}

	/**
	 * @param value
	 * @throws InvalidStringLengthException
	 */
	private static void validateStringLength(String value) throws InvalidStringLengthException {
		if (value != null && value.length() < 2) {
			throw new InvalidStringLengthException();
		}
	}

	private static class InvalidStringLengthException extends Exception {
	}

}

/*
 * Datenklasse zum Wetter Handling
 * 
 */
class Weather {
	public Current current;

	public double lon;
	public double lat;
}

class Current {
	public String temp_c;
	public Condition condition;
	public int humidity;
}

class Condition {
	public String text;
}
