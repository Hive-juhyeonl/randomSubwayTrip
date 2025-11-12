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
import java.util.HashMap; // (추가)

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

    // (수정 없음, 동일)
    @GetMapping("/random")
    public Station getRandomStation(@RequestParam(required = false) String line) {
        List<Station> filteredStations;
        if (line != null && !line.isEmpty() && !line.equals("전체")) {
            filteredStations = stationRepository.findByLine(line);
        } else {
            filteredStations = stationRepository.findAll();
        }
        
        // (추가) 만약 필터링된 역이 없다면 전체에서 뽑기 (예: "09호선"만 선택했는데 API 데이터가 비어있을 경우 방지)
        if (filteredStations.isEmpty()) {
            filteredStations = stationRepository.findAll();
            // DB가 비어있으면 앱이 멈추므로 방어 코드 추가
            if (filteredStations.isEmpty()) {
                // 이 경우를 대비해 DataInitializer가 DB를 채워야 함
                throw new RuntimeException("DB에 역 정보가 없습니다. DataInitializer를 확인하세요.");
            }
        }
        
        Random rand = new Random();
        int index = rand.nextInt(filteredStations.size());
        return filteredStations.get(index);
    }

    // (수정 없음, 동일)
    @GetMapping("/lines")
    public List<String> getLines() {
        return stationRepository.findDistinctLines();
    }

    
    // --- (이하 탭 API 3개는 searchKakao를 호출하므로 수정 없음) ---
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
     * (대폭 수정) 카카오 키워드 검색 API를 호출하는 공통 함수
     * @param query (예: "강남역 맛집")
     * @return 프론트엔드(script.js)가 원하는 카드 목록 형식으로 반환 (상세 정보 포함)
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
            
            // (수정) List<Map<String, String>> 대신 유연한 List<Map<String, Object>> 사용
            List<Map<String, String>> results = new ArrayList<>();

            for (JsonNode doc : documents) {
                // (수정) Map<String, String>으로 변경 (유연성을 위해)
                Map<String, String> item = new HashMap<>();
                
                // --- (이하 상세 정보 추가) ---
                String title = doc.path("place_name").asText();
                item.put("title", title);
                item.put("description", doc.path("category_name").asText());
                
                // (추가) 주소, 전화번호, 카카오맵 URL
                item.put("addressName", doc.path("address_name").asText());
                item.put("phone", doc.path("phone").asText("전화번호 정보 없음")); // 전화번호가 비어있을 경우 대비
                item.put("placeUrl", doc.path("place_url").asText()); // 카카오맵 링크
                
                // (임시 이미지)
                String imageUrl = "https://via.placeholder.com/300x200?text=" + title.substring(0, Math.min(title.length(), 5));
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