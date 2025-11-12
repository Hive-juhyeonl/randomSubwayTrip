package com.randomsubwaytrip.rst.domain; // (패키지 경로 확인)

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

// 1. 이 클래스가 DB 테이블(Entity)임을 선언합니다.
@Entity 
public class Station {

    // 2. 이 필드를 '기본 키(Primary Key)'로 지정합니다.
    @Id 
    // 3. (추가) ID 값을 DB가 자동으로 생성하도록(Auto-increment) 합니다.
    @GeneratedValue(strategy = GenerationType.IDENTITY) 
    private Long id;

    // 4. 'name'이라는 이름의 DB 컬럼을 만듭니다. (null이면 안 됨)
    @Column(nullable = false) 
    private String name;

    // 5. 'line'이라는 이름의 DB 컬럼을 만듭니다. (null이면 안 됨)
    @Column(nullable = false)
    private String line;


    // --- (JPA가 사용하기 위한 기본 생성자) ---
    protected Station() {
    }

    // --- (우리가 데이터를 넣을 때 사용할 생성자) ---
    public Station(String name, String line) {
        this.name = name;
        this.line = line;
    }

    // --- (Getter: 데이터를 꺼내기 위한 메소드) ---
    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getLine() {
        return line;
    }
}