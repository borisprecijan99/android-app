package pmf.rma.voiceassistant.services.http;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface GoogleKnowledgeGraphSearchApi {
    String BASE_URL = "https://kgsearch.googleapis.com/";

    @GET(value = "v1/entities:search?key=AIzaSyDLwwdIVAvEsfeSjHAWK-AanAsar6ZaqqQ&indent=True&languages=sr")
    Call<ResponseBody> getResult(@Query(value = "query") String query);
}
