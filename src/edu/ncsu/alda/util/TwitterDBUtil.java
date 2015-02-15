package edu.ncsu.alda.util;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.net.HttpURLConnection;
import java.io.DataOutputStream;
import java.io.InputStream;

import org.apache.commons.httpclient.util.URIUtil;
import org.apache.commons.lang3.StringUtils;

import twitter4j.JSONException;
import twitter4j.JSONObject;
import twitter4j.JSONArray;

import edu.ncsu.alda.core.AggregateEmotion;

public class TwitterDBUtil {

	static Map<String, AggregateEmotion> timeAggregatedMap = new HashMap<String, AggregateEmotion>();

	public static void generateCSVFile(String filename) {
		try {
			FileWriter writer = new FileWriter(filename);

			writer.append("TimePeriod");
			writer.append(',');
			writer.append("BJP Positive");
			writer.append(',');
			writer.append("BJP Negative");
			writer.append(',');
			writer.append("BJP Neutral");
			writer.append(',');
			writer.append("INC Positive");
			writer.append(',');
			writer.append("INC Negative");
			writer.append(',');
			writer.append("INC Neutral");
			writer.append(',');
			writer.append("AAP Positive");
			writer.append(',');
			writer.append("AAP Negative");
			writer.append(',');
			writer.append("AAP Neutral");
			writer.append('\n');

			for (Map.Entry<String, AggregateEmotion> entry : timeAggregatedMap
					.entrySet()) {
				AggregateEmotion ae = entry.getValue();
				writer.append(ae.printAll());
			}

			writer.flush();
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void queryLokSabhaDB(String date_pattern) {
		try {
			// Set up a connection and exectute a query
			Connection con = ConnectionFactory.getInstance().getConnection();

			Statement stmt = con.createStatement(
					java.sql.ResultSet.TYPE_FORWARD_ONLY,
					java.sql.ResultSet.CONCUR_READ_ONLY);

			stmt.setFetchSize(Integer.MIN_VALUE);

			String query = "SELECT creation_time,user_id,text "
					+ "FROM tweet_status_details "
					+ "WHERE creation_time LIKE '" + date_pattern + "'";

			Date date = new Date();
			System.out.println("Query start " + date.toString());

			ResultSet result = stmt.executeQuery(query);

			date = new Date();
			System.out.println("Query complete " + date.toString());

			// Do sentiment analysis in all tweets
			int bulk = 5000;
			int i = 0;
			int tuple_count = 0;
			String[] timeStamps = new String[bulk];
			long[] userIds = new long[bulk];
			Integer[] partyIDs = new Integer[bulk];
			String tweet_text = null;
			Integer partyID = -1;
			JSONArray tweetsArray = new JSONArray();
			List<JSONObject> tweetList = new ArrayList<JSONObject>();
			JSONObject request = null;
			boolean end = false;

			while (!end) {
				try {
					if (i % 3000 == 0) {
						if (tuple_count != 0)
							System.out.println(i + " : "
									+ timeStamps[tuple_count - 1]);
						else
							System.out.println(i);

					}
					i++;

					end = !result.next();

					if (end == true)
						System.out.println("End of tuples : " + i);

					// Collect 'bulk' tweets
					if (tuple_count < bulk && end == false) {
						tweet_text = new String(result.getString(3));
						partyID = getPartyID(tweet_text);

						if (partyID != -1) {
							timeStamps[tuple_count] = result.getString(1);
							userIds[tuple_count] = result.getLong(2);
							partyIDs[tuple_count] = partyID;

							tweetList.add(new JSONObject());
							tweetList.get(tuple_count).put("text", tweet_text);
							tweetsArray.put(tweetList.get(tuple_count));
							tuple_count++;
						}
					} else if (tuple_count > 0) // Analyze after collecting
												// 'bulk' tweets
					{
						JSONObject sentiments = new JSONObject();

						try {
							request = new JSONObject();
							request.put("data", tweetsArray);
							tweetsArray = new JSONArray();
							tweetList.removeAll(tweetList);
							System.out.println("Bulk : " + tuple_count);
							sentiments = getSentiment(request);
						} catch (UnsupportedEncodingException e) {
							System.err.println("UnsupportedEncodingException "
									+ e);
							continue;
						} catch (IOException e) {
							System.err.println("IOException " + e);
							continue;
						} catch (JSONException e) {
							System.err.println("JSONException " + e);
							tuple_count = 0;
							continue;
						}

						JSONArray senti_array = sentiments.getJSONArray("data");
						for (int k = 0; k < tuple_count; k++) {
							updateAll(
									timeStamps[k],
									userIds[k],
									partyIDs[k],
									Integer.parseInt(senti_array
											.getJSONObject(k).get("polarity")
											.toString()));
						}

						tuple_count = 0;
					}
				} catch (Exception e) {
					System.err.println("Main Try Block " + i + " "
							+ tuple_count + " " + e);
					tuple_count = 0;
					continue;
				}
			}
			stmt.close();
			con.close();
		} catch (SQLException ex) {
			System.err.println("SQL Exception");
		}
	}

	private static void updateAll(String timeStamp, long userId,
			Integer partyID, int sentiment) {
		String time = timeStamp.split(":")[0];
		AggregateEmotion ae;
		if (timeAggregatedMap.containsKey(time)) {
			ae = timeAggregatedMap.get(time);
		} else {
			ae = new AggregateEmotion();
		}

		ae.setTimeInterval(time);
		if (partyID == 1) {
			if (sentiment == 0)
				ae.incrementBjpNegative();
			else if (sentiment == 4)
				ae.incrementBjpPositive();
			else if (sentiment == 2)
				ae.incrementBjpNeutral();
		} else if (partyID == 2) {
			if (sentiment == 0)
				ae.incrementCongressNegative();
			else if (sentiment == 4)
				ae.incrementCongressPositive();
			else if (sentiment == 2)
				ae.incrementCongressNeutral();
		} else if (partyID == 3) {
			if (sentiment == 0)
				ae.incrementAapNegative();
			else if (sentiment == 4)
				ae.incrementAapPositive();
			else if (sentiment == 2)
				ae.incrementAapNeutral();
		}

		timeAggregatedMap.put(time, ae);
	}

	private static Integer getPartyID(String tweet) {
		// BJP = 1
		// INC = 2
		// AAP = 3
		// -1 if none or many parties included in tweet
		List<Integer> partyIDs = new ArrayList<Integer>();
		if (StringUtils.containsIgnoreCase(tweet, "BJP")
				|| StringUtils.containsIgnoreCase(tweet, "modi")
				|| StringUtils.containsIgnoreCase(tweet, "namo")
				|| StringUtils.containsIgnoreCase(tweet, "advani")) {
			partyIDs.add(1);
		}
		if (StringUtils.containsIgnoreCase(tweet, "Gandhi")
				|| StringUtils.containsIgnoreCase(tweet, "pappu")
				|| StringUtils.containsIgnoreCase(tweet, "raga")
				|| (StringUtils.containsIgnoreCase(tweet, "congress") && (StringUtils
						.containsIgnoreCase(tweet, "india")
						|| StringUtils.containsIgnoreCase(tweet, "loksabha")
						|| StringUtils.containsIgnoreCase(tweet, "rahul") || StringUtils
							.containsIgnoreCase(tweet, "sonia")))) {
			partyIDs.add(2);
		}
		if (StringUtils.containsIgnoreCase(tweet, "AAP")
				|| StringUtils.containsIgnoreCase(tweet, "Kejriwal")
				|| StringUtils.containsIgnoreCase(tweet, "ak49")
				|| StringUtils.containsIgnoreCase(tweet, "arvind")) {
			partyIDs.add(3);
		}

		if (partyIDs.size() == 1)
			return partyIDs.get(0);
		else
			return -1;
	}

	// Thanks to :
	// http://stackoverflow.com/questions/2586975/how-to-use-curl-in-java
	// http://www.xyzws.com/Javafaq/how-to-use-httpurlconnection-post-data-to-web-server/139
	private static JSONObject getSentiment(JSONObject tweets)
			throws UnsupportedEncodingException, IOException, JSONException {

		URL url = new URL(
				URIUtil.encodeQuery("http://www.sentiment140.com/api/bulkClassifyJson"));

		String request_str = tweets.toString();
		int tries = 0;
		while (tries < 2) {
			tries++;

			HttpURLConnection connection = (HttpURLConnection) url
					.openConnection();
			connection.setRequestMethod("POST");
			connection.setRequestProperty("Content-Type", "application/json");
			connection.setRequestProperty("Content-Length",
					"" + Integer.toString(request_str.length()));
			connection.setRequestProperty("Content-Language", "en-US");

			connection.setUseCaches(false);
			connection.setDoInput(true);
			connection.setDoOutput(true);

			System.out.println("Request size : " + request_str.length());

			// Send request
			DataOutputStream wr = new DataOutputStream(
					connection.getOutputStream());
			wr.writeBytes(request_str);
			wr.flush();
			wr.close();

			// Get Response
			InputStream is = connection.getInputStream();
			BufferedReader rd = new BufferedReader(new InputStreamReader(is));
			String line;
			StringBuffer json_string = new StringBuffer();
			while ((line = rd.readLine()) != null) {
				json_string.append(line);
				json_string.append('\r');
			}
			rd.close();

			System.out.println("Response size : " + json_string.length());

			if (json_string.length() != 0) {
				// Form the json response object
				JSONObject response = null;
				try {
					response = new JSONObject(json_string.toString());
				} catch (Exception e) {
					throw e;
				}

				return response;
			}
		}

		return null;
	}

	public static void main(String[] args) {
		if (args.length != 2) {
			System.out.println("Invalid command line arguments");
			System.out.println("Example: 2014-04-01* 2014-04-01.csv");
			return;
		}

		System.out.println(args[0] + " " + args[1]);

		Date date = new Date();
		System.out.println("Job start " + date.toString());

		queryLokSabhaDB(args[0]);

		System.out.println("Generating " + args[1]);
		generateCSVFile(args[1]);
		System.out.println("Generated");

		date = new Date();
		System.out.println("Job end " + date.toString());
	}
}
