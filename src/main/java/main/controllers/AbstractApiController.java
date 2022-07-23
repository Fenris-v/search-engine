package main.controllers;

import java.util.HashMap;
import java.util.Map;

abstract class AbstractApiController {
    protected Map<String, Object> getResponse() {
        return getResponse(true, null);
    }

    protected Map<String, Object> getResponse(boolean success, String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("result", success);

        if (message != null) {
            response.put("error", message);
        }

        return response;
    }
}
