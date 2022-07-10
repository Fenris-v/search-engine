package main.controllers;

import java.util.HashMap;
import java.util.Map;

abstract class AbstractApiController {
    protected Map<String, Object> getBoolResponse() {
        return getBoolResponse(true);
    }

    protected Map<String, Object> getBoolResponse(boolean success) {
        Map<String, Object> response = new HashMap<>();
        response.put("result", success);

        return response;
    }
}
