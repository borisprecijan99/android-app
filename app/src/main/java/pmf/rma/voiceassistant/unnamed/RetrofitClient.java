package pmf.rma.voiceassistant.unnamed;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    private static RetrofitClient instance = null;
    private final GoogleKnowledgeGraphSearchApi googleKnowledgeGraphSearchApi;

    private RetrofitClient() {
        Retrofit retrofit = new Retrofit.Builder().baseUrl(GoogleKnowledgeGraphSearchApi.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        googleKnowledgeGraphSearchApi = retrofit.create(GoogleKnowledgeGraphSearchApi.class);
    }

    public static synchronized RetrofitClient getInstance() {
        if (instance == null) {
            instance = new RetrofitClient();
        }
        return instance;
    }

    public GoogleKnowledgeGraphSearchApi getGoogleKnowledgeGraphSearchApi() {
        return googleKnowledgeGraphSearchApi;
    }
}
