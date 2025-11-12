package com.randomsubwaytrip.rst.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface StationRepository extends JpaRepository<Station, Long> {

    /**
     * "line"으로 역 목록을 찾는 커스텀 메소드
     */
    List<Station> findByLine(String line);

    /**
     * DB에서 중복을 제거한(DISTINCT) 노선 이름('line')을
     * 오름차순(ORDER BY)으로 정렬하여 모두 조회합니다.
     */
    @Query("SELECT DISTINCT s.line FROM Station s ORDER BY s.line ASC")
    List<String> findDistinctLines();
}