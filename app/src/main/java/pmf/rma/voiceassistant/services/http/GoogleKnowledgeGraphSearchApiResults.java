package pmf.rma.voiceassistant.services.http;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class GoogleKnowledgeGraphSearchApiResults {
    public class Context {
        @SerializedName("EntitySearchResult")
        @Expose
        public String entitySearchResult;
        @SerializedName("kg")
        @Expose
        public String kg;
        @SerializedName("detailedDescription")
        @Expose
        public String detailedDescription;
        @SerializedName("goog")
        @Expose
        public String goog;
        @SerializedName("@vocab")
        @Expose
        public String vocab;
        @SerializedName("resultScore")
        @Expose
        public String resultScore;
    }

    public class DetailedDescription {
        @SerializedName("url")
        @Expose
        public String url;
        @SerializedName("license")
        @Expose
        public String license;
        @SerializedName("articleBody")
        @Expose
        public String articleBody;
    }

    public class Result {
        @SerializedName("@id")
        @Expose
        public String id;
        @SerializedName("name")
        @Expose
        public String name;
        @SerializedName("description")
        @Expose
        public String description;
        @SerializedName("detailedDescription")
        @Expose
        public DetailedDescription detailedDescription;
        @SerializedName("@type")
        @Expose
        public List<String> type = null;
        @SerializedName("url")
        @Expose
        public String url;
    }

    public class ItemListElement {
        @SerializedName("result")
        @Expose
        public Result result;
        @SerializedName("@type")
        @Expose
        public String type;
        @SerializedName("resultScore")
        @Expose
        public int resultScore;
    }

    @SerializedName("@context")
    @Expose
    private Context context;
    @SerializedName("@type")
    @Expose
    private String type;
    @SerializedName("itemListElement")
    @Expose
    public List<ItemListElement> itemListElement = null;
}
