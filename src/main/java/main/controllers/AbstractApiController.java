package main.controllers;

import java.util.HashMap;
import java.util.Map;

abstract class AbstractApiController {
    protected Map<String, Object> getSimpleResponse() {
        return getSimpleResponse(true, null);
    }

    protected Map<String, Object> getSimpleResponse(boolean success, String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("result", success);

        if (message != null) {
            response.put("error", message);
        }

        return response;
    }
}
