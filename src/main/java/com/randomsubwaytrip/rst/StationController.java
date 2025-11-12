package com.randomsubwaytrip.rst;

import com.fasterxml.jackson.databind.JsonNode;
import com.randomsubwaytrip.rst.domain.Station;
import com.randomsubwaytrip.rst.domain.StationRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.HashMap; 

@RestController
@RequestMapping("/api/stations")
public class StationController {

    private final StationRepository stationRepository;
    private final RestTemplate restTemplate;

    @Value("${kakao.api.key}")
    private String kakaoApiKey;

    public StationController(StationRepository stationRepository, RestTemplate restTemplate) {
        this.stationRepository = stationRepository;
        this.restTemplate = restTemplate;
    }

    @GetMapping("/random")
    public Station getRandomStation(@RequestParam(required = false) String line) {
        List<Station> filteredStations;
        if (line != null && !line.isEmpty() && !line.equals("전체")) {
            filteredStations = stationRepository.findByLine(line);
        } else {
            filteredStations = stationRepository.findAll();
        }
        
        if (filteredStations.isEmpty()) {
            filteredStations = stationRepository.findAll(); 
            if (filteredStations.isEmpty()) {
                throw new RuntimeException("DB에 역 정보가 없습니다. DataInitializer를 확인하세요.");
            }
        }
        
        Random rand = new Random();
        int index = rand.nextInt(filteredStations.size());
        return filteredStations.get(index);
    }

    @GetMapping("/lines")
    public List<String> getLines() {
        return stationRepository.findDistinctLines();
    }

    
    // --- 탭 API 3개 ---
    @GetMapping("/{stationName}/play")
    public List<Map<String, String>> getPlayInfo(@PathVariable String stationName) {
        String query = stationName + " 놀거리";
        return searchKakao(query); 
    }

    @GetMapping("/{stationName}/see")
    public List<Map<String, String>> getSeeInfo(@PathVariable String stationName) {
        String query = stationName + " 볼거리";
        return searchKakao(query);
    }

    @GetMapping("/{stationName}/eat")
    public List<Map<String, String>> getEatInfo(@PathVariable String stationName) {
        String query = stationName + " 맛집"; 
        return searchKakao(query);
    }


    /**
     * 카카오 키워드 검색 API를 호출하는 공통 함수
     */
    private List<Map<String, String>> searchKakao(String query) {
        
        URI uri = UriComponentsBuilder
                .fromUriString("https://dapi.kakao.com/v2/local/search/keyword.json")
                .queryParam("query", query)
                .queryParam("size", 10) 
                .encode(StandardCharsets.UTF_8)
                .build()
                .toUri();

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "KakaoAK " + kakaoApiKey); 
        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<JsonNode> response = restTemplate.exchange(
                    uri,
                    HttpMethod.GET,
                    entity,
                    JsonNode.class
            );

            JsonNode documents = response.getBody().path("documents");
            List<Map<String, String>> results = new ArrayList<>();

            for (JsonNode doc : documents) {
                Map<String, String> item = new HashMap<>();
                
                String title = doc.path("place_name").asText();
                item.put("title", title);
                item.put("description", doc.path("category_name").asText());
                item.put("addressName", doc.path("address_name").asText());
                item.put("phone", doc.path("phone").asText("전화번호 정보 없음"));
                item.put("placeUrl", doc.path("place_url").asText()); 
                
                // (수정) 403 오류를 피하기 위해 다른 임시 이미지 서비스(placehold.co)로 변경
                String imageUrl = "https://placehold.co/300x200/EFEFEF/999?text=" + title.substring(0, Math.min(title.length(), 5));
                item.put("imageUrl", imageUrl);

                results.add(item);
            }
            return results;

        } catch (Exception e) {
            System.err.println("[Kakao API] 호출 중 오류 발생: " + e.getMessage());
            return List.of(); 
        }
    }
}