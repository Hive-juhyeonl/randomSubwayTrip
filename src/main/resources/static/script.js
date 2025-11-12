// --- 1. 전역 변수 ---
const randomButton = document.getElementById('randomBtn');
const resultArea = document.getElementById('result-area');
const filterButton = document.getElementById('filterBtn');
const tabsContainer = document.getElementById('station-info-tabs');
const tabButtons = document.querySelectorAll('.tab-button');
const tabContent = document.getElementById('tab-content'); // 카드 목록이 들어갈 부모

// (수정) 필터 모달 요소
const filterModal = document.getElementById('filter-modal');
const closeModalBtn = document.getElementById('close-modal-btn');
const lineOptions = document.querySelector('.line-options'); 

// (추가) 장소 상세 모달 요소
const detailModal = document.getElementById('detail-modal');
const closeDetailModalBtn = document.getElementById('close-detail-modal-btn');
const detailImage = document.getElementById('detail-image');
const detailTitle = document.getElementById('detail-title');
const detailDescription = document.getElementById('detail-description');
const detailAddress = document.getElementById('detail-address');
const detailPhone = document.getElementById('detail-phone');
const detailKakaoLink = document.getElementById('detail-kakao-link');

let currentStationName = '';
let currentLineFilter = '전체';
let areLinesLoaded = false; 

// --- 2. "랜덤 뽑기" 버튼 이벤트 ---
randomButton.addEventListener('click', () => {
    resultArea.textContent = '역을 뽑는 중...';
    tabsContainer.style.display = 'none';
    tabContent.innerHTML = '';
    const apiUrl = `/api/stations/random?line=${currentLineFilter}`;

    fetch(apiUrl)
        .then(response => {
            if (!response.ok) {
                console.error('API 응답 실패:', response);
                throw new Error('API 호출에 실패했습니다.');
            }
            return response.json();
        })
        .then(data => {
            currentStationName = data.name;
            resultArea.innerHTML = 
                `당신의 역은 [${data.line}] <span class="station-name">${currentStationName}역</span> 입니다!`;
            
            tabsContainer.style.display = 'block';

            tabButtons.forEach(btn => btn.classList.remove('active'));
            const firstTab = tabButtons[0];
            firstTab.classList.add('active');
            loadTabContent(firstTab.getAttribute('data-tab'));
        })
        .catch(error => {
            console.error('오류 발생:', error);
            resultArea.textContent = '오류가 발생했습니다. 다시 시도해 주세요.';
        });
});

// --- 3. 탭 버튼 클릭 이벤트 (수정 없음, 동일) ---
tabButtons.forEach(button => {
    button.addEventListener('click', () => {
        tabButtons.forEach(btn => btn.classList.remove('active'));
        button.classList.add('active');
        const tabType = button.getAttribute('data-tab');
        loadTabContent(tabType);
    });
});

// --- 4. (대폭 수정) 탭 콘텐츠 로드 함수 (data-* 속성 추가) ---
function loadTabContent(tabType) {
    if (currentStationName === '') return;
    tabContent.innerHTML = '불러오는 중...'; 
    
    fetch(`/api/stations/${currentStationName}/${tabType}`)
        .then(response => {
            if (!response.ok) throw new Error('데이터 로드 실패');
            return response.json(); 
        })
        .then(data => {
            tabContent.innerHTML = '';
            if (data.length === 0) {
                tabContent.textContent = '표시할 정보가 없습니다.';
                return;
            }

            data.forEach(item => {
                const card = document.createElement('div');
                card.className = 'info-card'; 

                // --- (이 부분이 추가/수정됩니다) ---
                // 컨트롤러에서 받은 상세 정보를 HTML data- 속성으로 "숨겨둡니다".
                card.setAttribute('data-title', item.title);
                card.setAttribute('data-image-url', item.imageUrl);
                card.setAttribute('data-description', item.description);
                card.setAttribute('data-address', item.addressName);
                card.setAttribute('data-phone', item.phone);
                card.setAttribute('data-place-url', item.placeUrl);
                // --- (추가 끝) ---

                // 카드 내부에 보이는 부분
                card.innerHTML = `
                    <img src="${item.imageUrl}" alt="${item.title}" class="card-image">
                    <div class="card-content">
                        <h3 class="card-title">${item.title}</h3>
                        <p class="card-description">${item.description}</p>
                    </div>
                `;
                tabContent.appendChild(card);
            });
        })
        .catch(error => {
            console.error('탭 콘텐츠 로드 오류:', error);
            tabContent.innerHTML = '정보를 불러오는 데 실패했습니다.';
        });
}

// --- 5. (수정) 필터 버튼 이벤트 (CSS 클래스로 모달 제어) ---
filterButton.addEventListener('click', () => {
    // (수정) style.display 대신 CSS의 .visible 클래스 사용
    if (areLinesLoaded === false) {
        fetch('/api/stations/lines')
            .then(response => response.json())
            .then(lines => {
                lines.forEach(line => {
                    const button = document.createElement('button');
                    button.className = 'line-button';
                    button.setAttribute('data-line', line); 
                    button.textContent = line; 
                    lineOptions.appendChild(button);
                });
                areLinesLoaded = true;
                filterModal.classList.add('visible'); // (수정)
            })
            .catch(error => {
                console.error('노선 목록 로드 실패:', error);
                alert('노선 목록을 불러오는 데 실패했습니다.');
            });
    } else {
        filterModal.classList.add('visible'); // (수정)
    }
});

// --- 6. (수정) 모달 닫기 이벤트 (CSS 클래스로 제어) ---
closeModalBtn.addEventListener('click', () => {
    filterModal.classList.remove('visible'); // (수정)
});

filterModal.addEventListener('click', (event) => {
    if (event.target === filterModal) {
        filterModal.classList.remove('visible'); // (수정)
    }
});

// --- 7. (수정) 모달 노선 버튼 클릭 이벤트 (CSS 클래스로 제어) ---
lineOptions.addEventListener('click', (event) => {
    if (event.target.classList.contains('line-button')) {
        const selectedLine = event.target.getAttribute('data-line');
        currentLineFilter = selectedLine;
        filterButton.textContent = `필터 (${selectedLine})`;
        filterModal.classList.remove('visible'); // (수정)
    }
});


// --- 8. (이하 전체 추가) 장소 상세 모달 이벤트 ---

/**
 * (추가) 카드 클릭 시 상세 모달 열기 (이벤트 위임)
 */
tabContent.addEventListener('click', (event) => {
    const card = event.target.closest('.info-card');
    
    if (card) {
        const title = card.getAttribute('data-title');
        const imageUrl = card.getAttribute('data-image-url');
        const description = card.getAttribute('data-description');
        const address = card.getAttribute('data-address');
        const phone = card.getAttribute('data-phone');
        const placeUrl = card.getAttribute('data-place-url');
        
        detailTitle.textContent = title;
        detailImage.src = imageUrl;
        detailImage.alt = title; 
        detailDescription.textContent = description;
        detailAddress.textContent = address || '주소 정보 없음'; 
        detailPhone.textContent = phone || '전화번호 정보 없음'; 
        detailKakaoLink.href = placeUrl; 
        
        detailModal.classList.add('visible');
    }
});

/**
 * (추가) 상세 모달 닫기 버튼
 */
closeDetailModalBtn.addEventListener('click', () => {
    detailModal.classList.remove('visible');
});

/**
 * (추가) 상세 모달 배경 클릭 시 닫기
 */
detailModal.addEventListener('click', (event) => {
    if (event.target === detailModal) {
        detailModal.classList.remove('visible');
    }
});