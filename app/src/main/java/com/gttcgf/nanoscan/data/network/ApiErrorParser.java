package com.gttcgf.nanoscan.data.network;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public final class ApiErrorParser {
    private ApiErrorParser() {
    }

    public static String parse(String responseBody) {
        if (responseBody == null || responseBody.trim().isEmpty()) {
            return "";
        }
        try {
            JSONObject jsonObject = new JSONObject(responseBody);
            if (!jsonObject.has("errors")) {
                return responseBody;
            }
            JSONArray errors = jsonObject.getJSONArray("errors");
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < errors.length(); i++) {
                builder.append(errors.getString(i));
            }
            return builder.toString();
        } catch (JSONException exception) {
            return responseBody;
        }
    }
}
