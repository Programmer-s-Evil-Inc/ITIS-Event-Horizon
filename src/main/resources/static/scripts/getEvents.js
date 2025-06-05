// Получаем элементы DOM
const eventsContainer = document.getElementById('events-container');

let searchTimeout = null;
const SEARCH_DELAY = 300;

// Функция загрузки и отображения событий
async function loadEvents(searchQuery = "") {
    try {
        const url = new URL('/api/events', window.location.origin);
        if(searchQuery.trim()) {
            url.searchParams.append('title', searchQuery);
        }

        const response = await fetch(url);

        if (!response.ok) {
            throw new Error(`HTTP error! Status: ${response.status}`);
        }

        const events = await response.json();
        renderEvents(events);
    } catch (error) {
        handleError(error);
    }
}

// Рендеринг всех событий
function renderEvents(events) {
    eventsContainer.innerHTML = events
        .map(event => {
            const card = createEventCard({
                ...event,
                id: BigInt(event.id).toString()
            });

            // Обертываем карточку в ссылку с модальными атрибутами
            const linkWrapper = document.createElement('a');
            linkWrapper.href = '#';
            linkWrapper.dataset.bsToggle = 'modal';
            linkWrapper.dataset.bsTarget = '#full-modal-event';

            linkWrapper.appendChild(card);
            return linkWrapper.outerHTML;
        })
        .join('');
}

// Рендеринг одной карточки события (без изменений)
function createEventCard(event) {
    const card = document.createElement('div');
    card.className = 'event-card card mb-4 rounded-5';

    const cardBody = document.createElement('div');
    cardBody.className = 'card-body rounded-5 position-relative';

    cardBody.style.background = `
        linear-gradient(rgba(0,0,0,0.45), rgba(0,0,0,0.45)),
        ${event.image_url
        ? `url('${event.image_url}') no-repeat center center/cover`
        : '#f0f0f0'
    }
    `;
    cardBody.style.height = '50vh';

    const title = document.createElement('h3');
    title.className = 'card-title';
    title.textContent = event.title;

    const date = document.createElement('p');
    date.className = 'mb-1';
    date.innerHTML = `<i class="far fa-calendar-alt me-2"></i>${formatDate(event.date)}`;

    const description = document.createElement('p');
    description.className = 'card-text';
    description.textContent = event.description;

    cardBody.append(title, date, description);
    card.appendChild(cardBody);

    return card;
}

// Остальные функции без изменений
function handleError(error) {
    console.error('Ошибка:', error);
    eventsContainer.innerHTML = `
        <div class="alert alert-danger">
            Ошибка загрузки: ${error.message}
        </div>
    `;
}

function formatDate(dateString) {
    const date = new Date(dateString);
    return date.toLocaleDateString('ru-RU', {
        day: 'numeric',
        month: 'long',
        year: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
    });
}

function handleSearchInput() {
    const searchTerm = document.getElementById('search-input').value.trim();
    clearTimeout(searchTimeout);
    searchTimeout = setTimeout(() => loadEvents(searchTerm), SEARCH_DELAY);
}

function handleFormSubmit(e) {
    e.preventDefault();
    clearTimeout(searchTimeout);
    const searchTerm = document.getElementById('search-input').value.trim();
    loadEvents(searchTerm);
}

// Инициализация
document.addEventListener('DOMContentLoaded', () => {
    loadEvents();

    const searchInput = document.getElementById('search-input');
    const searchForm = document.querySelector('.search-form');

    if (searchInput) searchInput.addEventListener('input', handleSearchInput);
    if (searchForm) searchForm.addEventListener('submit', handleFormSubmit);
});