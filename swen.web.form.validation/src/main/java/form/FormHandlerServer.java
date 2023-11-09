package form;

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

		// adding rest endpoints to server
		app.get("/patient", new AppHandler(RouteResponse.JSON));
		app.get("/patient2", new AppHandler(RouteResponse.HTML));

		/*
		 * TODO Implementieren Sie einen REST End point zum Erfassen von Patienten.
		 * Jeder Patient wird in einer *lokalen* Datenbank (Java-Map) abgespeichert. Die
		 * Patientennummer dient dabei als identizierende Information. 
		 * Der Rest End Point benutzt die POST HTTP-Methode. 
		 * 
		 * 
		 */

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

			cts.result("" + weather.current.temp_c + " bei " + weather.current.condition.text);

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
			cts.result("" + weather.current.temp_c + " bei " + weather.current.condition.text);

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
}

class Condition {
	public String text;
}
