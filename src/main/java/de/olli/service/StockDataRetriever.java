package de.olli.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import de.olli.model.dto.PriceDto;
import de.olli.model.dto.StockDto;
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
    private String stockUrlIntraday;
    private String apiKey;
    private WebClient webClient;
    private Environment environment;

    @Autowired
    public StockDataRetriever(@Value("${stock.url.daily}") String aplhaUrl, @Value("${stock.url.intraday}") String aplhaUrlIntraday, @Value("${api.key}") String apikey,
                        WebClient webClient, Environment environment) {
        this.stockUrlDaily = aplhaUrl;
        this.stockUrlIntraday = aplhaUrlIntraday;
        this.apiKey = apikey;
        this.webClient = webClient;
        this.environment = environment;
    }

    public Mono<StockDto> getStockFromApi(String stockName, boolean full) {
        if (Arrays.stream(environment.getActiveProfiles()).anyMatch(Predicate.isEqual("offline"))) {
            try {
                InputStream stream = ClassLoader.getSystemResourceAsStream("mock_data.json");

                ObjectMapper mapper = new ObjectMapper();
                StockDto stockDto = mapper.readValue(stream, StockDto.class);
                return Mono.just(stockDto);
            } catch (IOException e) {
                e.printStackTrace();
                return Mono.empty();
            }
        } else {
            return webClient.mutate().baseUrl(stockUrlDaily).defaultUriVariables(
                    ImmutableMap.of("stockId", stockName,"outputSize", full ? "full" : "minimal", "apiKey", apiKey)
                ).build()
                    .get()
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .bodyToMono(StockDto.class);
        }
    }

    public Mono<PriceDto> getLatestPriceFromApi(String stockName) {
        return webClient.mutate().baseUrl(stockUrlIntraday).defaultUriVariables(
                ImmutableMap.of("stockId", stockName, "apiKey", apiKey)
            ).build()
                .get()
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(PriceDto.class);
    }
}
