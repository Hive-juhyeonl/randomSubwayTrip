// --- 1. 전역 변수 ---
const randomButton = document.getElementById('randomBtn');
const resultArea = document.getElementById('result-area');
const filterButton = document.getElementById('filterBtn');
const tabsContainer = document.getElementById('station-info-tabs');
const tabButtons = document.querySelectorAll('.tab-button');
const tabContent = document.getElementById('tab-content');
const filterModal = document.getElementById('filter-modal');
const closeModalBtn = document.getElementById('close-modal-btn');
const lineOptions = document.querySelector('.line-options'); 

let currentStationName = '';
let currentLineFilter = '전체';
let areLinesLoaded = false; 

// --- 2. "랜덤 뽑기" 버튼 이벤트 ---
randomButton.addEventListener('click', () => {
    resultArea.textContent = '역을 뽑는 중...'; // (이 부분은 textContent 유지)
    tabsContainer.style.display = 'none';
    tabContent.innerHTML = '';
    const apiUrl = `/api/stations/random?line=${currentLineFilter}`;

    fetch(apiUrl)
        .then(response => {
            if (!response.ok) throw new Error('API 호출에 실패했습니다.');
            return response.json();
        })
        .then(data => {
            currentStationName = data.name;

            // --- (이 부분이 수정되었습니다) ---
            // 1. textContent 대신 innerHTML을 사용합니다.
            // 2. data.name 뒤에 "역"을 추가합니다.
            // 3. 역 이름 부분을 <span class="station-name">으로 감싸서 CSS가 적용되게 합니다.
            resultArea.innerHTML = 
                `당신의 역은 [${data.line}] <span class="station-name">${currentStationName}역</span> 입니다!`;
            // --- (수정 끝) ---

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

// --- 3. 탭 버튼 클릭 이벤트 (동일) ---
tabButtons.forEach(button => {
    button.addEventListener('click', () => {
        tabButtons.forEach(btn => btn.classList.remove('active'));
        button.classList.add('active');
        const tabType = button.getAttribute('data-tab');
        loadTabContent(tabType);
    });
});

// --- 4. 탭 콘텐츠 로드 함수 (동일) ---
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

// --- 5. 필터 버튼 이벤트 (동일) ---
filterButton.addEventListener('click', () => {
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
                filterModal.style.display = 'flex';
            })
            .catch(error => {
                console.error('노선 목록 로드 실패:', error);
                alert('노선 목록을 불러오는 데 실패했습니다.');
            });
    } else {
        filterModal.style.display = 'flex';
    }
});

// --- 6. 모달 닫기 이벤트 (동일) ---
closeModalBtn.addEventListener('click', () => {
    filterModal.style.display = 'none';
});

filterModal.addEventListener('click', (event) => {
    if (event.target === filterModal) {
        filterModal.style.display = 'none';
    }
});

// --- 7. 모달 노선 버튼 클릭 이벤트 (동일) ---
lineOptions.addEventListener('click', (event) => {
    if (event.target.classList.contains('line-button')) {
        const selectedLine = event.target.getAttribute('data-line');
        currentLineFilter = selectedLine;
        filterButton.textContent = `필터 (${selectedLine})`;
        filterModal.style.display = 'none';
    }
});