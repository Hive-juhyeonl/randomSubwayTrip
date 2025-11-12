package com.randomsubwaytrip.rst;

import com.fasterxml.jackson.databind.JsonNode;
import com.randomsubwaytrip.rst.domain.Station;
import com.randomsubwaytrip.rst.domain.StationRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private final StationRepository stationRepository;
    private final RestTemplate restTemplate;

    @Value("${seoul.api.key}")
    private String seoulApiKey;

    public DataInitializer(StationRepository stationRepository, RestTemplate restTemplate) {
        this.stationRepository = stationRepository;
        this.restTemplate = restTemplate;
    }

    @Override
    public void run(String... args) throws Exception {
        
        if (stationRepository.count() > 0) {
            System.out.println("[DataInitializer] DB에 이미 데이터가 있습니다. 초기화를 건너뜁니다.");
            return;
        }

        System.out.println("[DataInitializer] DB 초기화를 시작합니다. (서울시 API 호출)");
        List<Station> stationsToSave = new ArrayList<>();
        
        for (int lineNum = 1; lineNum <= 9; lineNum++) {
            
            String lineName = String.format("%02d호선", lineNum);

            // --- (이 부분이 수정되었습니다) ---
            String baseUrl = "http://openapi.seoul.go.kr:8088";
            
            URI apiUrl = UriComponentsBuilder
                    .fromUriString(baseUrl)
                    // (수정) {lineName}을 경로에서 제거
                    .path("/{apiKey}/json/SearchSTNBySubwayLineInfo/1/1000/") 
                    // (수정) 쿼리 파라미터로 LINE_NUM 추가
                    .queryParam("LINE_NUM", lineName) 
                    .buildAndExpand(seoulApiKey) // {apiKey} 값만 채움
                    .toUri();
            // --- (수정 끝) ---

            try {
                JsonNode response = restTemplate.getForObject(apiUrl, JsonNode.class);

                // API 키가 틀렸거나 서비스가 잘못되었을 때의 최상위 에러
                JsonNode rootResult = response.path("RESULT");
                if (!rootResult.isMissingNode()) {
                    System.err.println("[DataInitializer] API 오류 (Root): " + rootResult.path("MESSAGE").asText());
                    continue; // 다음 호선으로
                }

                // 정상 응답이지만, 그 안의 RESULT 확인 (예: "해당하는 데이터가 없습니다")
                JsonNode mainNode = response.path("SearchSTNBySubwayLineInfo");
                JsonNode dataResult = mainNode.path("RESULT");
                if (!dataResult.path("CODE").asText().equals("INFO-000")) {
                     System.err.println("[DataInitializer] API 오류: " + dataResult.path("MESSAGE").asText());
                     continue; // 다음 호선으로
                }
                
                JsonNode rows = mainNode.path("row");

                if (rows.isMissingNode() || !rows.isArray() || rows.size() == 0) {
                    System.out.println("[DataInitializer] " + lineName + " 데이터를 찾을 수 없습니다 (row is empty).");
                    continue; 
                }

                for (JsonNode row : rows) {
                    String stationName = row.path("STATION_NM").asText();
                    String line = row.path("LINE_NUM").asText(); 

                    if (stationName != null && !stationName.isEmpty()) {
                        stationsToSave.add(new Station(stationName, line));
                    }
                }
                 System.out.println("[DataInitializer] " + lineName + " 데이터 로드 성공.");

            } catch (Exception e) {
                System.err.println("[DataInitializer] API 호출 중 오류 발생 (호선: " + lineName + "): " + e.getMessage());
            }
        }

        stationRepository.saveAll(stationsToSave);
        System.out.println("[DataInitializer] 총 " + stationsToSave.size() + "개의 역 정보를 DB에 저장했습니다.");
    }
}