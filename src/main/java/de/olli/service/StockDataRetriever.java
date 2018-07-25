package de.olli.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import de.olli.model.Stock;
import de.olli.repository.StocksMongoDBReactiveRepository;
import org.assertj.core.util.VisibleForTesting;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.function.Predicate;

@Service
public class StockDataRetriever {

    private String stockUrlDaily;
    private String stockUrlFull;
    private WebClient webClient;
    private Environment environment;

    @Autowired
    public StockDataRetriever(@Value("${stock.url.daily}") String aplhaUrl, @Value("${api.key}") String apikey,
                        WebClient webClient, Environment environment) {
        this.stockUrlDaily = aplhaUrl + apikey;
        this.stockUrlFull = this.stockUrlDaily + "&outputsize=full";
        this.webClient = webClient;
        this.environment = environment;
    }

    @VisibleForTesting
    public Mono<Stock> getStockFromApi(String stockName, boolean full) {
        if (Arrays.stream(environment.getActiveProfiles()).anyMatch(Predicate.isEqual("offline"))) {
            try {
                InputStream stream = ClassLoader.getSystemResourceAsStream("mock_data.json");

                ObjectMapper mapper = new ObjectMapper();
                Stock stock = mapper.readValue(stream, Stock.class);
                return Mono.just(stock);
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        } else {
            return webClient.mutate().baseUrl(full ? stockUrlFull : stockUrlDaily).defaultUriVariables(ImmutableMap.of("stockId", stockName)).build()
                    .get()
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .bodyToMono(Stock.class);
        }
    }
}
