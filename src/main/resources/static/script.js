// "오늘 어디 갈까?" 버튼(id: randomBtn)을 찾습니다.
const randomButton = document.getElementById('randomBtn');
        
// 결과 표시 영역(id: result-area)을 찾습니다.
const resultArea = document.getElementById('result-area');

// 버튼 클릭 이벤트를 등록합니다.
randomButton.addEventListener('click', () => {
    // 버튼을 누르면 이 함수가 실행됩니다.
    
    // 1. 결과 영역을 "뽑는 중..."으로 바꿉니다.
    resultArea.textContent = '역을 뽑는 중...';

    // 2. Spring Boot 컨트롤러 API(/api/stations/random)를 호출합니다.
    fetch('/api/stations/random')
        .then(response => {
            // 3. API 서버가 응답하면, 그 내용을 JSON 객체로 변환합니다.
            if (!response.ok) {
                throw new Error('API 호출에 실패했습니다.');
            }
            return response.json();
        })
        .then(data => {
            // 4. JSON 변환이 성공하면, 화면에 역 정보를 표시합니다.
            // (data 객체 예: { "name": "강남역", "line": "2호선" })
            resultArea.textContent = 
                `당신의 역은 [${data.line}] ${data.name} 입니다!`;
        })
        .catch(error => {
            // 5. 중간에 오류가 발생하면, 에러 메시지를 표시합니다.
            console.error('오류 발생:', error);
            resultArea.textContent = '오류가 발생했습니다. 다시 시도해 주세요.';
        });
});

// (참고) 필터 버튼은 아직 아무 기능도 하지 않습니다.
const filterButton = document.getElementById('filterBtn');
filterButton.addEventListener('click', () => {
    alert('필터 기능은 아직 개발 중입니다.');
});