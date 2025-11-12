package com.randomsubwaytrip.rst; // (본인 패키지 경로에 맞게 수정)

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.Random;

@RestController
@RequestMapping("/api/stations") // 이 컨트롤러의 모든 API는 /api/stations로 시작합니다.
public class StationController {

    // (임시) 테스트를 위한 역 목록. (나중에 DB에서 가져와야 함)
    private static final List<Map<String, String>> STATIONS = List.of(
            Map.of("name", "강남역", "line", "2호선"),
            Map.of("name", "홍대입구역", "line", "2호선"),
            Map.of("name", "서울역", "line", "1호선"),
            Map.of("name", "명동역", "line", "4호선"),
            Map.of("name", "잠실역", "line", "2호선")
    );

    /**
     * "랜덤 역 뽑기" 버튼이 호출할 API
     * (임시 목록에서 역 1개를 랜덤으로 반환)
     */
    @GetMapping("/random") // GET /api/stations/random 요청을 처리합니다.
    public Map<String, String> getRandomStation() {
        // 0부터 (역 개수 - 1) 사이의 랜덤한 숫자 1개를 뽑습니다.
        Random rand = new Random();
        int index = rand.nextInt(STATIONS.size());

        // 해당 인덱스의 역 정보를 JSON으로 반환합니다.
        return STATIONS.get(index);
    }

}