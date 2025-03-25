package ce2team1.mentoview.utils.archive;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.util.HashMap;
import java.util.Map;

public class JsonUtils {

    private static final ObjectMapper mapper = new ObjectMapper()
            .enable(SerializationFeature.INDENT_OUTPUT)
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    // 직렬화 : JSON으로
    public static String toJson(Object obj) {
        try{
            return mapper.writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException("직렬화 오류",e);
        }
    }

    // 역직렬화 : Java로
    public static <T> T byJson(String json, Class<T> clazz) {
        try {
            return mapper.readValue(json, clazz);
        } catch (Exception e) {
            throw new RuntimeException("역직렬화 오류", e);
        }
    }

    // 에러 직렬화
    public static String errorToJson(int status, String errorMassage) {
        Map<Object, Object> errorResponse  = new HashMap<>();
        errorResponse.put("status", status);
        errorResponse.put("error", errorMassage);

        return toJson(errorResponse);
    }
}
