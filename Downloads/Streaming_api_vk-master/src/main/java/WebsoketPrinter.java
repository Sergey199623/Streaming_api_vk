import com.vk.api.sdk.client.TransportClient;
import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.ServiceActor;
import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import com.vk.api.sdk.httpclient.HttpTransportClient;
import com.vk.api.sdk.objects.streaming.responses.GetServerUrlResponse;
import com.vk.api.sdk.streaming.clients.StreamingEventHandler;
import com.vk.api.sdk.streaming.clients.VkStreamingApiClient;
import com.vk.api.sdk.streaming.clients.actors.StreamingActor;
import com.vk.api.sdk.streaming.exceptions.StreamingApiException;
import com.vk.api.sdk.streaming.exceptions.StreamingClientException;
import com.vk.api.sdk.streaming.objects.StreamingCallbackMessage;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.*;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class WebsoketPrinter {

    private static final RequestOptions COMMON_OPTIONS;

    static {
        RequestOptions.Builder builder = RequestOptions.DEFAULT.toBuilder();

        // The default cache size is 100 MB. Change it to 30 MB.
        builder.setHttpAsyncResponseConsumerFactory(
                new HttpAsyncResponseConsumerFactory
                        .HeapBufferedResponseConsumerFactory(30 * 1024 * 1024));
        COMMON_OPTIONS = builder.build();
    }

    private VkStreamingApiClient streamingClient;
    private StreamingActor streamingActor;  //был public
    private VkApiClient vkClient;

//    private static String tags = ", 1, 2, баррель, рубль, доллар, коронавирус, коронавирус, заболело, погибло, " +
//            "коронавирус, пандемия, эпидемия, болезнь, вирус, коронавирус, пандемия, эпидемия, болезнь, вирус, заражение, " +
//            "сидимдома, изоляция, самоизоляция, коронавирус, пандемия, эпидемия, болезнь, вирус, заражение, сидимдома, " +
//            "изоляция, самоизоляция, COVID, COVID-19, коронавирус, пандемия, эпидемия, болезнь, вирус, заражение, " +
//            "сидимдома, изоляция, самоизоляция, COVID, COVID-19, апокалипсис, короновирус, сидимдома, карантин, апрель]}}";

    WebsoketPrinter() {
        TransportClient transportClient = new HttpTransportClient();
        streamingClient = new VkStreamingApiClient(transportClient);
        vkClient = new VkApiClient(transportClient);
        streamingActor = new StreamingActor(this.getServerUrlResponse().getEndpoint(), this.getServerUrlResponse().getKey());
    }

    private GetServerUrlResponse getServerUrlResponse() {
        ConfigReader configReader = new ConfigReader();
        Integer appId = Integer.valueOf(configReader.getProp("appId"));
        String accessToken = configReader.getProp("accessToken");
        ServiceActor serviceActor = new ServiceActor(appId, accessToken);
        GetServerUrlResponse getServerUrlResponse = null;

        try {
            getServerUrlResponse = vkClient.streaming().getServerUrl(serviceActor).execute();
        } catch (ApiException | ClientException e) {
            e.printStackTrace();
        }
        return getServerUrlResponse;
    }

    public void print() {

        /**
         * @param []id - хранилище постов, каждый id = каждому посту (1 id = 1 пост);
         */

        try {
            final int[] id = {0};
            streamingClient.stream().get(streamingActor, new StreamingEventHandler() {
                @Override
                public void handle(StreamingCallbackMessage message) {

                    System.out.println("Сообщение от стриминга получено");
                    final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();

                    id[0]++;
                    credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials("elastic", "changeme"));

                    RestClientBuilder builder = RestClient.builder(new HttpHost("45.84.227.209", 9200))
                            .setHttpClientConfigCallback(new RestClientBuilder.HttpClientConfigCallback() {
                                @Override
                                public HttpAsyncClientBuilder customizeHttpClient(HttpAsyncClientBuilder httpClientBuilder) {
                                    return httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
                                }
                            });

                    try {

                        // Create a request.
                        RestHighLevelClient highClient = new RestHighLevelClient(builder);
                        Map<String, String> jsonMap = new HashMap<>();

                        jsonMap.put("Message", message.toString());

                        // index_name is the index name, type_name is the type name, and doc_id is the document ID.
                        IndexRequest indexRequest = new IndexRequest("stream", "_doc", String.valueOf(id[0])).source(jsonMap);
                        System.out.println(message.toString());
                        System.out.println("Передача сообщения на сервер");

                        // Run the following command in parallel and use the custom RequestOptions (COMMON_OPTIONS).
                        IndexResponse indexResponse = highClient.index(indexRequest, COMMON_OPTIONS);

                        highClient.close();

                    } catch (IOException ioException) {
                        ioException.printStackTrace();
                    }
                }
            }).execute();

            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
        }
    }

}
