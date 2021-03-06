package com.jukusoft.erp.lib.message.response;

import com.jukusoft.erp.lib.message.StatusCode;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.MessageCodec;
import io.vertx.core.json.JsonObject;

public class ApiResponseCodec implements MessageCodec<ApiResponse, ApiResponse> {

    @Override
    public void encodeToWire(Buffer buffer, ApiResponse res) {
        //create new json object
        JsonObject json = new JsonObject();

        if (res.data == null) {
            throw new NullPointerException("json data of api response '" + res.getClass().getSimpleName() + "' cannot be null.");
        }

        //put data
        json.put("event", res.eventName);
        json.put("data", res.data);
        json.put("ackID", res.ackID);
        json.put("statusCode", res.getStatusCode());
        json.put("cluster-message-id", res.getMessageID());
        json.put("external-id", res.getExternalID());
        json.put("session-id", res.getSessionID());
        json.put("type", res.getType());

        //encode json object to string
        String jsonToStr = json.toString();

        // Length of JSON: is NOT characters count
        int length = jsonToStr.getBytes().length;

        // Write data into given buffer
        buffer.appendInt(length);
        buffer.appendString(jsonToStr);
    }

    @Override
    public ApiResponse decodeFromWire(int position, Buffer buffer) {
        // My custom message starting from this *position* of buffer
        int _pos = position;

        // Length of JSON
        int length = buffer.getInt(_pos);

        // Get JSON string by it`s length
        // Jump 4 because getInt() == 4 bytes
        String jsonStr = buffer.getString(_pos+=4, _pos+=length);
        JsonObject json = new JsonObject(jsonStr);

        ApiResponse res = new ApiResponse(json.getLong("cluster-message-id"), json.getString("external-id"), json.getString("session-id"), json.getString("event"));

        res.eventName = json.getString("event");
        res.data = json.getJsonObject("data");
        res.ackID = json.getString("ackID");
        res.statusCode = StatusCode.getByString(json.getString("statusCode"));
        res.messageID = json.getLong("cluster-message-id");
        res.type = ApiResponse.getTypeByString(json.getString("type"));

        return res;
    }

    @Override
    public ApiResponse transform(ApiResponse apiResponse) {
        return apiResponse;
    }

    @Override
    public String name() {
        return this.getClass().getSimpleName();
    }

    @Override
    public byte systemCodecID() {
        return -1;
    }

}
