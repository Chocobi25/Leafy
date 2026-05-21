package com.chocobi.leafy.external.vworld.client;

import com.chocobi.leafy.external.vworld.dto.VWorldRegionResponse;
import com.chocobi.leafy.external.vworld.dto.VWorldRegionResponse.VWorldRegionItem;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.util.retry.Retry;

@Component
@RequiredArgsConstructor
@Slf4j
public class VWorldRegionClient {
    private static final String ADM_CODE_LIST_PATH = "/admCodeList";
    private static final String ADM_SI_LIST_PATH = "/admSiList";
    private static final String ADM_EMD_LIST_PATH = "/admDongList";
    private static final String ADM_REE_LIST_PATH = "/admReeList";
    private static final int PAGE_SIZE = 100;

    private final WebClient vWorldClient;

    @Value("${VWORLD_API_KEY}")
    private String apiKey;

    @Value("${SERVICE_DOMAIN_URL}")
    private String serviceDomain;

    public List<VWorldRegionItem> fetchSidos() {
        return fetchAllPages(ADM_CODE_LIST_PATH, null);
    }

    public List<VWorldRegionItem> fetchSigungus(String sidoCode) {
        return fetchAllPages(ADM_SI_LIST_PATH, sidoCode);
    }

    public List<VWorldRegionItem> fetchEmds(String sigunguCode) {
        return fetchAllPages(ADM_EMD_LIST_PATH, sigunguCode);
    }

    public List<VWorldRegionItem> fetchRees(String emdCode) {
        return fetchAllPages(ADM_REE_LIST_PATH, emdCode);
    }

    private List<VWorldRegionItem> fetchAllPages(String path, String admCode) {
        List<VWorldRegionItem> accumulated = new ArrayList<>();

        VWorldRegionResponse firstResponse = fetchPage(path, admCode, 1);
        List<VWorldRegionItem> firstPage = extractItems(firstResponse);

        if (firstPage.isEmpty()) {
            return accumulated;
        }

        accumulated.addAll(firstPage);

        int totalPages = calculateTotalPages(extractTotalCount(firstResponse));

        for (int pageNo = 2; pageNo <= totalPages; pageNo++) {
            VWorldRegionResponse response = fetchPage(path, admCode, pageNo);
            accumulated.addAll(extractItems(response));
        }

        return accumulated;
    }

    private int calculateTotalPages(int totalCount) {
        if (totalCount <= 0) {
            return 1;
        }

        return (int) Math.ceil((double) totalCount / PAGE_SIZE);
    }

    private List<VWorldRegionItem> extractItems(VWorldRegionResponse response) {
        if (response == null || response.getAdmVOList() == null || response.getAdmVOList().getAdmVOList() == null) {
            return List.of();
        }

        return response.getAdmVOList().getAdmVOList();
    }

    private int extractTotalCount(VWorldRegionResponse response) {
        if (response == null || response.getAdmVOList() == null || response.getAdmVOList().getTotalCount() == null) {
            return 0;
        }

        try {
            return Integer.parseInt(response.getAdmVOList().getTotalCount());
        } catch (NumberFormatException e) {
            log.warn("VWorld totalCount 파싱 실패: {}", response.getAdmVOList().getTotalCount());
            return 0;
        }
    }

    private VWorldRegionResponse fetchPage(String endpoint, String admCode, int pageNo) {
        UriComponentsBuilder uriBuilder = UriComponentsBuilder
                .fromPath(endpoint)
                .queryParam("key", apiKey)
                .queryParam("pageNo", pageNo)
                .queryParam("numOfRows", PAGE_SIZE)
                .queryParam("domain", serviceDomain)
                .queryParam("format", "json");

        if (admCode != null && !admCode.isEmpty()) {
            uriBuilder.queryParam("admCode", admCode);
        }

        return vWorldClient.get()
                .uri(uriBuilder.toUriString())
                .retrieve()
                .bodyToMono(VWorldRegionResponse.class)
                .retryWhen(Retry.backoff(3, Duration.ofSeconds(1)))
                .block();
    }
}
